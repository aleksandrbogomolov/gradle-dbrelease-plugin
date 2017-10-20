package com.tander.logistics.svn

import com.tander.logistics.core.DbRelease
import com.tander.logistics.core.ScmFile
import org.gradle.api.Project
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNNodeKind
import org.tmatesoft.svn.core.wc.*

/**
 * Created by durov_an on 22.12.2016.
 */
class DbReleaseSvn extends DbRelease {

    SvnUtils svnUtils
    SvnBranch currBranch
    SvnBranch prevBranch

    DbReleaseSvn(Project project) {
        super(project)

        this.svnUtils = new SvnUtils(ext.user, ext.password.toCharArray())

        currBranch = new SvnBranch(svnUtils, null, null, null)
        prevBranch = new SvnBranch(svnUtils, null, null, null)

        scriptInstall.currBranch = currBranch
        scriptInstall.prevBranch = prevBranch

        scriptUninstall.prevBranch = currBranch
        scriptUninstall.currBranch = prevBranch

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

        currBranch.version = ext.releaseVersion

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

        if (ext.isRelease) {
            prevBranch.version = project.settings.previousVersion
            if (!prevBranch.version) {
                prevBranch.version = svnUtils.getPreviousVersionFromSet(currBranch.version)
            }
        } else {
            prevBranch.version = currBranch.version.take(currBranch.version.lastIndexOf("."))
        }

        svnUtils.testConnection(currBranch.url)
    }

    void setLastCommitInfo() {
        ScmFileLogEntryHandler logEntryHandler = new ScmFileLogEntryHandler()

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
                if (svnDiffStatus.getKind() == SVNNodeKind.FILE) {
                    scmFile = new ScmFile(svnDiffStatus.getPath())
                    scmFile.url = svnDiffStatus.getURL().toString()
                    if (scmFile.url.contains('uninstall')) {
                        scmFile.isUninstall = true
                    }
                    if (svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                SVNStatusType.STATUS_DELETED,
                                                                SVNStatusType.STATUS_ADDED]) {
                        scmFile.checkWildcards(wildcards)
                    } else {
                        logger.warn(scmFile.name + " Uncorrected file status : " + svnDiffStatus.getModificationType().toString())
                    }

                    if (svnDiffStatus.getModificationType() != SVNStatusType.STATUS_DELETED && !scmFile.isUninstall) {
                        scriptInstall.scmFiles[scmFile.name] = scmFile
                    }
                    if (svnDiffStatus.getModificationType() != SVNStatusType.STATUS_ADDED || scmFile.isUninstall) {
                        scriptUninstall.scmFiles[scmFile.name] = scmFile
                    }
                }
                logger.info(svnDiffStatus.getModificationType().toString() + ' ' + svnDiffStatus.getFile().toString())
            }
        }
        logger.lifecycle("--------------- diff start ---------------")
        svnUtils.doDiffStatus(prevBranch.getUrl(),
                prevBranch.revision,
                currBranch.getUrl(),
                currBranch.revision,
                diffStatusHandler)
        logger.lifecycle(" files to install: " + scriptInstall.scmFiles.size())
        logger.lifecycle(" files to uninstall: " + scriptUninstall.scmFiles.size())
        logger.lifecycle("--------------- diff finish ---------------")

        // и отсортируем полученные списки
        scriptInstall.sortScmFiles()
        scriptUninstall.sortScmFiles()
    }

    void exportChangedFilesToDir() {
        logger.lifecycle("--------------- export start ---------------")
        ISVNEventHandler dispatcher = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent svnEvent, double v) throws SVNException {
                if (svnEvent.getAction() == SVNEventAction.UPDATE_COMPLETED) {
                    logger.lifecycle(" export file " + svnEvent.getFile().toString())
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
