package com.tander.logistics.svn

import com.tander.logistics.core.IScmBranch
import com.tander.logistics.core.ScmBranch
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.wc.ISVNEventHandler
import org.tmatesoft.svn.core.wc.SVNEvent
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNWCUtil

/**
 * Created by durov_an on 17.01.2017.
 */
class SvnBranch extends ScmBranch implements IScmBranch{
    SvnUtils svnUtils

    SVNRevision revision
    String getInstallUrl() {
        return url + "/install"
    }

    String getUninstallUrl() {
        return url + "/uninstall"
    }

    @Override
    void export(String path) {

        ISVNEventHandler dispatcher = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent svnEvent, double v) throws SVNException {
                logger.lifecycle("exporting file " + svnEvent.getFile().toString())
            }

            @Override
            void checkCancelled() throws SVNCancelException {
            }
        }
//        updateClient.doExport(SVNURL.parseURIEncoded(svnURL), checkoutSVNBranch, revisionName, revisionName, null, true, SVNDepth.INFINITY);

        if (SVNWCUtil.isVersionedDirectory(new File(path))) {
            if (svnUtils.getWorkingDirectoryUrl(path) == url) {
                logger.lifecycle("update folder $path")
                svnUtils.doUpdate(path, revision, dispatcher)
            } else {
                throw new Exception("Target dir use different SVN URL. Clean build dir first.")
            }
        } else {
            File exportDir = new File(path)
            exportDir.deleteDir();
            logger.lifecycle("checkout URL $url to dir $path")
            svnUtils.doCheckout(url, path, revision, dispatcher)
        }
    }


    SvnBranch(SvnUtils svnUtils, String folderPath, String scmUrl, String revision) {
        this.svnUtils = svnUtils
    }

    @Override
    String getUrlFromFolder(String path) {
        if (SVNWCUtil.isVersionedDirectory(new File(path))) {
            return svnUtils.getWorkingDirectoryUrl(path)
        } else {
            throw new Exception("cant resolve SVN URL at folder: $path")
        }
    }


    @Override
    String getFirstRevision() {
        // получение первой ревизии в ветке
        long revisionNumber = 0
        SVNRevision firstRevision = SVNRevision.HEAD

        ISVNLogEntryHandler isvnLogEntryHandler = new ISVNLogEntryHandler() {
            @Override
            void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
//                logger.info("определена начальная ревизия в ветке $url:" + logEntry.getRevision() + ' ' + logEntry.getMessage())
                revisionNumber = logEntry.getRevision()
            }
        }

        svnUtils.doLog(url, SVNRevision.create(0), SVNRevision.HEAD, 1, isvnLogEntryHandler)
        if (firstRevision != 0) {
            firstRevision = SVNRevision.create(revisionNumber)
        }
        return firstRevision.number
    }

    @Override
    String getLastRevision() {
        // получение первой ревизии в ветке
        long revisionNumber = 0
        SVNRevision firstRevision = SVNRevision.HEAD

        ISVNLogEntryHandler isvnLogEntryHandler = new ISVNLogEntryHandler() {
            @Override
            void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
//                logger.info("определена начальная ревизия в ветке $url:" + logEntry.getRevision() + ' ' + logEntry.getMessage())
                revisionNumber = logEntry.getRevision()
            }
        }

        svnUtils.doLog(url, SVNRevision.HEAD, SVNRevision.create(0), 1, isvnLogEntryHandler)
        if (firstRevision != 0) {
            firstRevision = SVNRevision.create(revisionNumber)
        }
        return firstRevision.number
    }


}
