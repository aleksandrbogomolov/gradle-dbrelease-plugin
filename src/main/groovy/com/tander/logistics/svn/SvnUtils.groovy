package com.tander.logistics.svn

import com.tander.logistics.DbReleaseExtension
import org.gradle.api.Project
import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.*
import org.tmatesoft.svn.core.wc2.*

/**
 * Created by durov_an on 01.04.2016.
 * для работы с SVN
 */

class SvnUtils {

    ISVNAuthenticationManager authManager
    SVNClientManager clientManager
    SVNRevision firstRevision
    DbReleaseExtension ext

    SvnUtils(String username, char[] password, Project project) {
        this.ext = project.extensions.findByName('dbrelease') as DbReleaseExtension
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

    def doExportNew(File src, File dst, SVNRevision revision) {
        final SvnOperationFactory factory = new SvnOperationFactory()
        try {
            final SvnExport svnExport = factory.createExport()
            svnExport.setSource(SvnTarget.fromFile(src))
            svnExport.setSingleTarget(SvnTarget.fromFile(dst))
            svnExport.setRevision(revision)
            svnExport.run()
        } finally {
            factory.dispose()
        }
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

    def doCommit(File path, String message) {
        final SvnOperationFactory factory = new SvnOperationFactory()
        try {
            final SvnCommit svnCommit = factory.createCommit()
            svnCommit.setSingleTarget(SvnTarget.fromFile(path))
            svnCommit.setDepth(SVNDepth.INFINITY)
            svnCommit.setCommitMessage(message)
            svnCommit.run()
        } finally {
            factory.dispose()
        }
    }

    def doCheckout(String targetDir, String url, SVNDepth depth) {
        final SvnOperationFactory factory = new SvnOperationFactory()
        try {
            final SvnCheckout svnCheckout = factory.createCheckout()
            svnCheckout.setSingleTarget(SvnTarget.fromFile(new File(targetDir)))
            svnCheckout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)))
            svnCheckout.setRevision(SVNRevision.HEAD)
            svnCheckout.setDepth(depth)
            svnCheckout.run()
        } finally {
            factory.dispose()
        }
    }

    def doImport(File path, SVNURL svnurl, String message) {
        final SvnOperationFactory factory = new SvnOperationFactory()
        try {
            final SvnImport svnImport = factory.createImport()
            svnImport.setSource(path)
            svnImport.setSingleTarget(SvnTarget.fromURL(svnurl))
            svnImport.setCommitMessage(message)
            svnImport.run()
        } finally {
            factory.dispose()
        }
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
        def repoUrl = ext.getProjectProperty('ebuildPath')
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
