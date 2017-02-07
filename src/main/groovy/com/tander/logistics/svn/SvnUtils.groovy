package com.tander.logistics.svn

import org.tmatesoft.svn.core.ISVNDirEntryHandler
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler
import org.tmatesoft.svn.core.wc.ISVNEventHandler
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNDiffClient
import org.tmatesoft.svn.core.wc.SVNInfo
import org.tmatesoft.svn.core.wc.SVNLogClient
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNUpdateClient
import org.tmatesoft.svn.core.wc.SVNWCClient
import org.tmatesoft.svn.core.wc.SVNWCUtil

/**
 * Created by durov_an on 01.04.2016.
 * для работы с SVN
 */

class SvnUtils {

    ISVNAuthenticationManager authManager
    SVNClientManager clientManager
    SVNRevision firstRevision

    SvnUtils(String username, char[] password) {
        DAVRepositoryFactory.setup()
        authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password)
//        authManager = SVNWCUtil.createDefaultAuthenticationManager(null, username, password, true)
//        authManager = SVNWCUtil.createDefaultAuthenticationManager()
//        println SVNWCUtil.getDefaultConfigurationDirectory().path
//        authManager.setAuthenticationProvider()
        firstRevision = SVNRevision.create(1)
        clientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(false), authManager)
    }


    def doExport(String svnURL, String dirPath, SVNRevision revision, ISVNEventHandler dispatcher) {
        SVNUpdateClient updateClient = new SVNUpdateClient(getAuthManager(), SVNWCUtil.createDefaultOptions(true))
        updateClient.setEventHandler(dispatcher)
        updateClient.setIgnoreExternals(true)
        updateClient.doExport(
                SVNURL.parseURIEncoded(svnURL),
                new File(dirPath),
                revision,
                revision,
                '',
                false,
                SVNDepth.INFINITY
        )
    }

    def doCheckout(String svnURL, String dirPath, SVNRevision revision, ISVNEventHandler dispatcher) {
        SVNUpdateClient updateClient = new SVNUpdateClient(getAuthManager(), SVNWCUtil.createDefaultOptions(true))
        updateClient.setEventHandler(dispatcher)
        updateClient.setIgnoreExternals(true)
        updateClient.doCheckout(
                SVNURL.parseURIEncoded(svnURL),
                new File(dirPath),
                revision,
                revision,
                SVNDepth.INFINITY,
                false)
    }

    def doUpdate(String dirPath, SVNRevision revision, ISVNEventHandler dispatcher) {
        SVNUpdateClient updateClient = new SVNUpdateClient(getAuthManager(), SVNWCUtil.createDefaultOptions(true))
        updateClient.setEventHandler(dispatcher)
        updateClient.setIgnoreExternals(true)
        updateClient.doUpdate(
                new File(dirPath),
                revision,
                SVNDepth.INFINITY,
                false,
                false)
    }

    def doLog(String svnUrl, SVNRevision startRevision, SVNRevision endRevision, long limit, ISVNLogEntryHandler isvnLogEntryHandler) {
        SVNLogClient logClient = clientManager.getLogClient()
//        SVNLogClient logClient = new SVNLogClient(getAuthManager(), SVNWCUtil.createDefaultOptions(true))
        logClient.doLog(
                SVNURL.parseURIEncoded(svnUrl),
                null,
                SVNRevision.UNDEFINED,
                startRevision,
                endRevision,
                true,
                true,
                limit,
                isvnLogEntryHandler)
    }

    def doDiffStatus(String prevSVNURL, SVNRevision prevSVNRevision, String curSVNURL, SVNRevision curSVNRevision, ISVNDiffStatusHandler diffStatusHandler) {
        SVNDiffClient diffClient = new SVNDiffClient(getAuthManager(), SVNWCUtil.createDefaultOptions(true))
        diffClient.doDiffStatus(
                SVNURL.parseURIEncoded(prevSVNURL),
                prevSVNRevision,
                SVNURL.parseURIEncoded(curSVNURL),
                curSVNRevision,
                SVNDepth.INFINITY,
                true,
                diffStatusHandler)
    }

    def doList(String svnURL, ISVNDirEntryHandler isvnDirEntryHandler) {
        SVNLogClient logClient = new SVNLogClient(getAuthManager(), SVNWCUtil.createDefaultOptions(true))
        logClient.doList(
                SVNURL.parseURIEncoded(svnURL),
                SVNRevision.HEAD,
                SVNRevision.HEAD,
                false,
                true,
                isvnDirEntryHandler)
    }

    String getWorkingDirectoryUrl(String dirPath) {
        SVNWCClient svnwcClient = new SVNWCClient(getAuthManager(), SVNWCUtil.createDefaultOptions(true))
        SVNInfo svnInfo = svnwcClient.doInfo(new File(dirPath), SVNRevision.WORKING)
        return svnInfo.getURL().toString()
    }

    static SVNRevision getSvnRevision(String revision) {
        switch (revision) {
            case 'HEAD':
                return SVNRevision.HEAD
                break
            case 'WORKING':
                return SVNRevision.WORKING
                break
            case 'PREVIOUS':
                return SVNRevision.PREVIOUS
                break
            case 'BASE':
                return SVNRevision.BASE
                break
            case 'COMMITTED':
                return SVNRevision.COMMITTED
                break
            case 'UNDEFINED':
                return SVNRevision.UNDEFINED
                break
            default:
                return SVNRevision.create(revision as long)
                break
        }
    }

}
