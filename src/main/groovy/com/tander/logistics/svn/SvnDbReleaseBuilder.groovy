package com.tander.logistics.svn

import com.tander.logistics.core.DbRelease
import com.tander.logistics.core.ScmFile
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNNodeKind
import org.tmatesoft.svn.core.wc.*

/**
 * Created by durov_an on 22.12.2016.
 */
class SvnDbReleaseBuilder extends DbRelease {

    SvnUtils svnUtils
    SvnBranch currBranch
    SvnBranch prevBranch

    List<ScmFile> notMatched = new ArrayList<>()

    SvnDbReleaseBuilder(Project project) {
        super(project)

        this.svnUtils = new SvnUtils(ext)

        currBranch = new SvnBranch(svnUtils)
        prevBranch = new SvnBranch(svnUtils)

        scriptInstall.currBranch = currBranch
        scriptInstall.prevBranch = prevBranch

        scriptUninstall.prevBranch = currBranch
        scriptUninstall.currBranch = prevBranch

        project.version = ext.getProjectProperty("newVersion") ?: project.version

        if (ext.currUrl) {
            currBranch.url = ext.currUrl
        } else {
            currBranch.url = currBranch.getUrlFromFolder(project.projectDir.toString())
        }

        if (ext.currRevision) {
            currBranch.revision = SVNRevision.create(ext.currRevision as long)
        } else {
            currBranch.revision = SVNRevision.create(currBranch.getLastRevision() as long)
        }

        currBranch.version = project.version

        if (ext.prevUrl) {
            prevBranch.url = ext.prevUrl
            prevBranch.revision = SVNRevision.create(prevBranch.getLastRevision() as long)
        } else {
            prevBranch.url = currBranch.url
            prevBranch.revision = SVNRevision.create(prevBranch.getFirstRevision() as long)
        }

        if (ext.prevRevision) {
            prevBranch.revision = SVNRevision.create(ext.prevRevision as long)
        }

        prevBranch.version = project.settings.get('previousVersion')
        if (!prevBranch.version) {
            if (ext.isRelease) {
                prevBranch.version = svnUtils.getPreviousVersionFromSet(currBranch.version, ext.ebuildUrl)
            } else {
                prevBranch.version = currBranch.version.take(currBranch.version.lastIndexOf("."))
            }
        }

        svnUtils.testConnection(currBranch.url)
    }

    void setLastCommitInfo() {
        SvnFileLogEntryHandler logEntryHandler = new SvnFileLogEntryHandler()

        logger.lifecycle("--------------- get revision info start ---------------")

        scriptInstall.scmFiles.each { String fileName, ScmFile scmFile ->
            logEntryHandler.scmFile = scmFile
            logEntryHandler.logger = logger
            svnUtils.doLog(scmFile.url, currBranch.revision, svnUtils.firstRevision, 1, logEntryHandler)
        }

        logger.lifecycle("--------------- get revision info finish ---------------")
    }

    void setChangedFilesByDiff() {
        ISVNDiffStatusHandler diffStatusHandler = new ISVNDiffStatusHandler() {
            ScmFile scmFile

            @Override
            void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
                boolean matched
                if (svnDiffStatus.getKind() == SVNNodeKind.FILE) {
                    scmFile = new ScmFile(svnDiffStatus.getPath())
                    scmFile.url = svnDiffStatus.getURL().toString()
                    if (scmFile.url.contains('uninstall')) {
                        scmFile.isUninstall = true
                    }
                    def inStatus = svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                           SVNStatusType.STATUS_DELETED,
                                                                           SVNStatusType.STATUS_ADDED]
                    if (inStatus) {
                        matched = scmFile.checkWildcards(schemaWildcards, sectionWildcards)
                    } else {
                        logger.warn(scmFile.name + " Uncorrected file status : " + svnDiffStatus
                                .getModificationType().toString())
                    }

                    if (matched && checkIfExcluded(scmFile)) {
                        if (svnDiffStatus.getModificationType() != SVNStatusType.STATUS_ADDED && !scmFile.isUninstall) {
                            scriptInstall.scmFiles[scmFile.name] = scmFile
                        }
                        if (svnDiffStatus.getModificationType() != SVNStatusType.STATUS_DELETED && !scmFile.isUninstall) {
                            scriptUninstall.scmFiles[scmFile.name] = scmFile
                            scmFile.url = "${prevBranch.url}/${svnDiffStatus.path}"
                        }
                        if (scmFile.isUninstall && svnDiffStatus.getModificationType() != SVNStatusType.STATUS_ADDED) {
                            scriptUninstall.scmFiles[scmFile.name] = scmFile
                        }
                    } else if (!matched && inStatus) {
                        if (checkIfExcluded(scmFile)) {
                            notMatched.add(scmFile)
                        }
                    }
                }
                logger.debug("${svnDiffStatus.getModificationType().toString()} " +
                        "${svnDiffStatus.getFile().toString()}")
            }

            /**
             * Проверяет находится ли проверяемый файл в списке исключенных
             * @param scmFile обрабатываемый файл
             * @return {@code true} если данного файла нет в списке исключенных иначе {@code false}
             */
            private boolean checkIfExcluded(ScmFile scmFile) {
                boolean isNotExclude = true
                for (exclude in ext.excludeFiles) {
                    if (FilenameUtils.wildcardMatch(scmFile.name, exclude)) {
                        isNotExclude = false
                    }
                }
                isNotExclude
            }
        }
        logger.lifecycle("--------------- diff start ---------------")
        svnUtils.doDiffStatus(currBranch.getUrl(),
                currBranch.revision,
                prevBranch.getUrl(),
                prevBranch.revision,
                diffStatusHandler)

        if (!notMatched.isEmpty()) {
            logger.warn("Not matched files:")
            notMatched.each { scmFile ->
                logger.warn(scmFile.name)
            }
            throw new Exception("Found files not matched by any wildcard, check project_settings.gradle")
        }

        logger.lifecycle(" files to install: " + scriptInstall.scmFiles.size())
        logger.lifecycle(" files to uninstall: " + scriptUninstall.scmFiles.size())
        logger.lifecycle("--------------- diff finish ---------------")

        if (scriptInstall.scmFiles.isEmpty() && scriptUninstall.scmFiles.isEmpty()) {
            throw new Exception('There is no data change found in project, please check, mb need do commit')
        }
    }

    void exportChangedFilesToDir() {
        logger.lifecycle("--------------- export start ---------------")
        ISVNEventHandler dispatcher = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent svnEvent, double v) throws SVNException {
                if (svnEvent.getAction() == SVNEventAction.UPDATE_COMPLETED) {
                    logger.debug(" export file " + svnEvent.getFile().toString())
                }
            }

            @Override
            void checkCancelled() throws SVNCancelException {
            }
        }

        scriptInstall.scmFiles.each { String fileName, ScmFile scmFile ->
            svnUtils.doExport(scmFile.url,
                    releaseDir.path + '/install/' + scmFile.name,
                    currBranch.revision,
                    dispatcher)
        }
        scriptUninstall.scmFiles.each { String fileName, ScmFile scmFile ->
            svnUtils.doExport(scmFile.url,
                    releaseDir.path + '/uninstall/' + scmFile.name,
                    scmFile.isUninstall ? currBranch.revision : prevBranch.revision,
                    dispatcher)
        }
        logger.lifecycle("--------------- export finish ---------------")
    }
}
