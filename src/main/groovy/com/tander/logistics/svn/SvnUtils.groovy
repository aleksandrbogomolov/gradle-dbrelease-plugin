package com.tander.logistics.svn

import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.*

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
        ISVNAuthenticationProvider provider = new SvnAuthProvider()
        authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password)
        authManager.setAuthenticationProvider(provider)
        clientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true), authManager)
        firstRevision = SVNRevision.create(1)
    }

    def doExport(String svnURL, String dirPath, SVNRevision revision, ISVNEventHandler dispatcher) throws SVNException {
        SVNUpdateClient updateClient = clientManager.getUpdateClient()
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
        SVNUpdateClient updateClient = clientManager.getUpdateClient()
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
        SVNUpdateClient updateClient = clientManager.getUpdateClient()
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
        SVNDiffClient diffClient = clientManager.getDiffClient()
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
        SVNLogClient logClient = clientManager.getLogClient()
        logClient.doList(
                SVNURL.parseURIEncoded(svnURL),
                SVNRevision.HEAD,
                SVNRevision.HEAD,
                false,
                true,
                isvnDirEntryHandler)
    }

    void testConnection(String svnUrl) {
        SVNURL url = new SVNURL(svnUrl, true)
        SVNRepository repository = SVNRepositoryFactory.create(url, null);
        repository.setAuthenticationManager(authManager);
        repository.testConnection()
    }

    String getWorkingDirectoryUrl(String dirPath) {

        SVNWCClient svnwcClient = clientManager.getWCClient()
        SVNInfo svnInfo = svnwcClient.doInfo(new File(dirPath), SVNRevision.WORKING)
        return svnInfo.getURL().toString()
    }

    /**
     * Достаем из директории set предыдущую версию,
     * @param currentVersion текущая версия
     * @return в зависимости от релиз это или патч возвращается номер предыдущего релиза или предыдущего патча
     */
    String getPreviousVersionFromSet(String currentVersion) {
        String result = currentVersion
        def repoUrl = "https://sources.corp.tander.ru/svn/real_out/pkg/repository/set/tomcatsrv-dc-ora"
        def repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repoUrl))
        repository.setAuthenticationManager(authManager)
        def regex = checkIsRelease(currentVersion) ? ~/\d*\.\d*\.0/ : ~/\d*\.\d*\.\d*/
        def dir = new ArrayList<SVNDirEntry>()
        repository.getDir(".", SVNRevision.HEAD.getNumber(), new SVNProperties(), dir)
        def versions = new ArrayList()
        versions.add(currentVersion)
        dir.each { f ->
            def name = f.getName() - ".ebuild"
            def version = name.split("-").last()
            if (regex.matcher(version).matches()) {
                versions.add(version)
            }
        }
        versions.sort()
        for (e in versions) {
            if (e == currentVersion) {
                return result
            } else {
                result = e
            }
        }
        return result
    }

    /**
     * Проверяем по текущей версии является ли данная сборка релизом или патчем
     * @param version текущая версия
     * @return code{true} если релиз или code{false} если патч
     */
    boolean checkIsRelease(String currentVersion) {
        return currentVersion.split("\\.").last() == "0"
    }
}
