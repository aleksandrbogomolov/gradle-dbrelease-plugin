package com.tander.logistics.svn

import com.tander.logistics.core.DbRelease
import com.tander.logistics.core.ScmFile
import com.tander.logistics.core.ScriptType
import org.gradle.api.Project
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.SVNNodeKind
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler
import org.tmatesoft.svn.core.wc.ISVNEventHandler
import org.tmatesoft.svn.core.wc.SVNDiffStatus
import org.tmatesoft.svn.core.wc.SVNEvent
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNStatusType

/**
 * Created by durov_an on 22.12.2016.
 */
class DbReleaseSvn extends DbRelease {
    SvnUtils svnUtils

    String INSTALL_URL_POSTFIX = "/install"
    String UNINSTALL_URL_POSTFIX = "/uninstall"
    SvnBranch currBranch
    SvnBranch prevBranch

    DbReleaseSvn(Project project) {
        super(project)
        this.svnUtils = new SvnUtils(ext.user, ext.password.toCharArray())

        currBranch = new SvnBranch(svnUtils, null, null, null)
        prevBranch = new SvnBranch(svnUtils, null, null, null)
        if (ext.currUrl) {
            currBranch.url = ext.currUrl
        } else {
            currBranch.url = currBranch.getUrlFromFolder(project.projectDir.toString())
        }

        if (ext.currRevision) {
            currBranch.revision = SVNRevision.create(ext.currRevision as long)
        } else {
            currBranch.revision = SVNRevision.create(currBranch.getLastRevision() as long)
//            currBranch.revision = SVNRevision.HEAD
        }

        if (ext.releaseVersion) {
            currBranch.version = ext.releaseVersion
        } else {
            currBranch.version = currBranch.getLastPathSegmentFromUrl()[0..29]
        }

        if (ext.prevUrl) {
            prevBranch.url = ext.prevUrl
            prevBranch.revision = SVNRevision.create(prevBranch.getLastRevision() as long)
//            prevBranch.revision = SVNRevision.HEAD
        } else {
            prevBranch.url = currBranch.url
            prevBranch.revision = SVNRevision.create(prevBranch.getFirstRevision() as long)
        }

        prevBranch.version = prevBranch.getLastPathSegmentFromUrl()

        if (ext.prevRevision) {
            prevBranch.revision = SVNRevision.create(ext.prevRevision as long)
        }

    }

    void setLastCommitInfo() {
        ScmFileLogEntryHandler logEntryHandler = new ScmFileLogEntryHandler()

        ISVNLogEntryHandler handler = new ISVNLogEntryHandler() {
            @Override
            void handleLogEntry(SVNLogEntry svnLogEntry) throws SVNException {
                logger.lifecycle(svnLogEntry.getMessage().toString())
            }
        }

        scmFilesInstall.each { String fileName, ScmFile scmFile ->
            logEntryHandler.scmFile = scmFile
            svnUtils.doLog(scmFile.url, currBranch.revision, svnUtils.firstRevision, 1, logEntryHandler)
        }
    }

    void setChangedFilesByDiff() {

        ISVNDiffStatusHandler diffStatusHandler = new ISVNDiffStatusHandler() {
            ScmFile scmFile

            @Override
            void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
                if (svnDiffStatus.getKind() == SVNNodeKind.FILE) {
                    scmFile = new ScmFile(svnDiffStatus.getPath())
                    scmFile.url = svnDiffStatus.getURL().toString()
                    if (svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                SVNStatusType.STATUS_DELETED,
                                                                SVNStatusType.STATUS_ADDED]) {
                        scmFile.checkWildcards(wildacards)
                    } else {
                        logger.warn(scmFile.name + "Uncorrected file status : " + svnDiffStatus.getModificationType().toString())
                    }

                    if (!(svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_DELETED])) {
                        scmFilesInstall[scmFile.name] = scmFile
                    }
                    if (!(svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_ADDED])) {
                        scmFilesUninstall[scmFile.name] = scmFile
                    }
                }
                logger.info(svnDiffStatus.getModificationType().toString() + ' ' + svnDiffStatus.getFile().toString())
            }
        }
        svnUtils.doDiffStatus(prevBranch.getInstallUrl(),
                prevBranch.revision,
                currBranch.getInstallUrl(),
                currBranch.revision,
                diffStatusHandler)
        svnUtils.doDiffStatus(currBranch.getUninstallUrl(),
                currBranch.revision,
                prevBranch.getUninstallUrl(),
                prevBranch.revision,
                diffStatusHandler)

        // и отсортируем полученные списки
        scmFilesInstall = scmFilesInstall.entrySet().sort(false, scmFileComparatorWildcard).collectEntries()
        scmFilesUninstall = scmFilesUninstall.entrySet().sort(false, scmFileComparatorWildcard).collectEntries()
    }

    void exportChangedFilesToDir() {

        ISVNEventHandler dispatcher = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent svnEvent, double v) throws SVNException {
//                logger.lifecycle("exporting file " + svnEvent.getFile().toString())
            }

            @Override
            void checkCancelled() throws SVNCancelException {
            }
        }

        scmFilesInstall.each { String fileName, ScmFile scmFile ->
            svnUtils.doExport(scmFile.url,
                    project.buildDir.path + '/install/' + scmFile.name,
                    currBranch.revision,
                    dispatcher)
        }
        scmFilesUninstall.each { String fileName, ScmFile scmFile ->
            svnUtils.doExport(scmFile.url,
                    project.buildDir.path + '/uninstall/' + scmFile.name,
                    prevBranch.revision,
                    dispatcher)
        }
    }

    String getStat() {
        String stat = "prompt [INFO] Statistics\n"
        def cnt = scmFilesInstall.countBy { it.value.wildcardsMatched }
        wildacards["section"].each {
            stat += "prompt ...[STAT][${it.toString()}] - ${cnt[it.toString()]}\n"
        }
        stat += "prompt [INFO] Statistics\n"
    }

    @Override
    LinkedHashMap makeBinding(ScriptType type) {
        LinkedHashMap binding = []

        binding.clear()

        binding["TMPL_LOG_VERSION"] = "${type.dirName}_log_${currBranch.version}.lst"
        binding["TMPL_DESC_VERSION"] = "${type.dirName} assembly ${currBranch.version}. Installing Software DC Oracle"
        binding["TMPL_CONFIG_CURRENT_VERSION"] = "${prevBranch.version}"
        binding["TMPL_CONFIG_NEW_VERSION"] = "${currBranch.version}"
        binding["TMPL_CONFIG_TASK"] = "${ext.buildTaskNumber}"
        binding["TMPL_CONFIG_ASSEMBLY"] = "${ext.taskNumber}"
        binding["TMPL_CONFIG_DATECREATED"] = "${new Date().format('dd.MM.yyyy HH:mm:ss')}"
        binding["TMPL_CONFIG_USERCREATED"] = "${ext.user}"
        binding["TMPL_CONFIG_REVISION"] = "${currBranch.revision.toString()}"
        binding["TMPL_CONFIG_CHECKVERS"] = "${ext.isCheckReleaseNumberNeeded}"
        binding["TMPL_CONFIG_UPDATEVERS"] = "${ext.isUpdateReleaseNumberNeeded}"
        binding["TMPL_CONFIG_RECOMPILING"] = "${scriptSections["TMPL_SCRIPT_AFTER_INSTALL"].toString().length() ? "1" : "0"}"
        binding["TMPL_CONFIG_LISTNODEBUGPACK"] = ""
        binding["TMPL_CONFIG_TOTALBLOCKS"] = "${scmFilesInstall.size()}"
        binding["TMPL_INFORMATION_SATISTICS"] = getStat()
        binding["TMPL_INFORMATION_CREATED"] = """
prompt BranchCurrent: ${currBranch.url} -revision: ${currBranch.revision}
prompt BranchPrevios: ${prevBranch.url} -revision: ${prevBranch.revision}

"""
        return binding
    }

}
