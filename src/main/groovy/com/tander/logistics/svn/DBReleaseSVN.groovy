package com.tander.logistics.svn

import com.tander.logistics.core.DBRelease
import com.tander.logistics.core.SCMFile
import org.gradle.api.Project
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
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
class DBReleaseSVN extends DBRelease {
    SVNUtils svnUtils

    String INSTALL_URL_POSTFIX = "/install"
    String UNINSTALL_URL_POSTFIX = "/uninstall"

    DBReleaseSVN(Project project) {
        super(project)
        this.svnUtils = new SVNUtils(ext.user, ext.password.toCharArray())
        this.svnUtils = svnUtils
    }

    void setLastCommitInfo() {
        String fileURL
        SCMFileLogEntryHandler logEntryHandler = new SCMFileLogEntryHandler()

        scmFilesInstall.each { String fileName, SCMFile scmFile ->
            fileURL = currURL + fileName
            logEntryHandler.scmFile = scmFile
            svnUtils.doLog(currURL, SVNRevision.HEAD, 1, logEntryHandler)
        }
    }

    void setChangedFilesByDiff() {

        ISVNDiffStatusHandler diffStatusHandler = new ISVNDiffStatusHandler() {
            SCMFile scmFile

            @Override
            void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
                if (svnDiffStatus.getKind() == SVNNodeKind.FILE) {
                    scmFile = new SCMFile(svnDiffStatus.getPath())
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
        svnUtils.doDiffStatus(prevURL + INSTALL_URL_POSTFIX, SVNRevision.HEAD,
                currURL + INSTALL_URL_POSTFIX, SVNRevision.HEAD, diffStatusHandler)
        svnUtils.doDiffStatus(currURL + UNINSTALL_URL_POSTFIX, SVNRevision.HEAD,
                prevURL + UNINSTALL_URL_POSTFIX, SVNRevision.HEAD, diffStatusHandler)

        // и отсортируем полученные списки
        scmFilesInstall = scmFilesInstall.entrySet().sort(false, scmFileComparatorWildcard).collectEntries()
        scmFilesUninstall = scmFilesUninstall.entrySet().sort(false, scmFileComparatorWildcard).collectEntries()
    }

    void exportChangedFilesToDir() {

        ISVNEventHandler dispatcher = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent svnEvent, double v) throws SVNException {
                logger.lifecycle("exporting file " + svnEvent.getFile().toString())
            }

            @Override
            void checkCancelled() throws SVNCancelException {
            }
        }

        scmFilesInstall.each { String fileName, SCMFile scmFile ->
            svnUtils.doExport(scmFile.url, project.buildDir.path + '/script/install/' + scmFile.name, SVNRevision.HEAD, dispatcher)
        }
        scmFilesUninstall.each { String fileName, SCMFile scmFile ->
            svnUtils.doExport(scmFile.url, project.buildDir.path + '/script/uninstall/' + scmFile.name, SVNRevision.HEAD, dispatcher)
        }
    }

}
