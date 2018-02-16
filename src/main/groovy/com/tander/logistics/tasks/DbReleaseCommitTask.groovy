package com.tander.logistics.tasks

import com.tander.logistics.DbReleaseExtension
import com.tander.logistics.svn.SvnUtils
import com.tander.logistics.ui.UiUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.SVNRevision

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Created by bogomolov_av on 05.02.2018
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class DbReleaseCommitTask extends DefaultTask {

    private final DbReleaseExtension ext
    private final SvnUtils svnUtils

    private String distFilesDir = new File("${project.buildDir}/distfiles").path
    private String setEbuildsDir = new File("${project.buildDir}/repository/set").path
    private String oraEbuildsDir = new File("${project.buildDir}/repository/tander-tsdserver").path

    private String packageName
    private final String uninstall = "uninstall"
    private final String tbz = ".tbz"
    private final String ebuild = ".ebuild"

    DbReleaseCommitTask() {
        this.ext = project.extensions.findByName('dbrelease') as DbReleaseExtension
        this.svnUtils = new SvnUtils(ext)
    }

    @TaskAction
    void run() {
        packageName = ext.settings.get("ebuildName")

        if (ext.commitSettings) {
            boolean isCanceled
            String commitMessage

            (commitMessage, isCanceled) = UiUtils.promptCommitMessage("Please enter commit message:")

            if (isCanceled) {
                throw new Exception("Task canceled by user")
            }
            checkoutSVNDirectory()
            updateSVNFiles()
            List files = copyNewFiles()
            commit(files, commitMessage)
        }
    }

    void checkoutSVNDirectory() {
        svnUtils.doCheckout(distFilesDir, "${getCheckoutUrl("tbzPath")}", SVNDepth.EMPTY)
        svnUtils.doCheckout("$setEbuildsDir/$packageName", "${getCheckoutUrl("setEbuildPath")}", SVNDepth.EMPTY)
        svnUtils.doCheckout("$setEbuildsDir/$packageName-$uninstall", "${getCheckoutUrl("setUninstallEbuildPath")}", SVNDepth.EMPTY)
        svnUtils.doCheckout("$oraEbuildsDir/$packageName", "${getCheckoutUrl("ebuildPath")}", SVNDepth.EMPTY)
        svnUtils.doCheckout("$oraEbuildsDir/$packageName-$uninstall", "${getCheckoutUrl("uninstallEbuildPath")} ", SVNDepth.EMPTY)
    }

    void updateSVNFiles() {
        def tbzIns = "$packageName-${project.version}$tbz"
        def tbzUnins = "$packageName-$uninstall-${project.version}$tbz"
        def eblIns = "$packageName-${project.version}$ebuild"
        def eblUnins = "$packageName-$uninstall-${project.version}$ebuild"

        svnUtils.doUpdate("$distFilesDir/$tbzIns", SVNRevision.HEAD, null)
        svnUtils.doUpdate("$distFilesDir/$tbzUnins", SVNRevision.HEAD, null)

        svnUtils.doUpdate("$setEbuildsDir/$packageName/$eblIns", SVNRevision.HEAD, null)
        svnUtils.doUpdate("$setEbuildsDir/$packageName-$uninstall/$eblUnins", SVNRevision.HEAD, null)

        svnUtils.doUpdate("$oraEbuildsDir/$packageName/$eblIns", SVNRevision.HEAD, null)
        svnUtils.doUpdate("$oraEbuildsDir/$packageName-$uninstall/$eblUnins", SVNRevision.HEAD, null)
    }

    List<Path> copyNewFiles() {
        List<File> files = new ArrayList<>()
        files.addAll(new File("${project.buildDir}/distributions").listFiles().toList())
        files.addAll(new File("${setEbuildsDir.replace('repository', 'ebuilds')}/$packageName").listFiles().toList())
        files.addAll(new File("${setEbuildsDir.replace('repository', 'ebuilds')}/$packageName-$uninstall").listFiles().toList())
        files.addAll(new File("${oraEbuildsDir.replace('repository', 'ebuilds')}/$packageName").listFiles().toList())
        files.addAll(new File("${oraEbuildsDir.replace('repository', 'ebuilds')}/$packageName-$uninstall").listFiles().toList())

        List<Path> result = new ArrayList<>()

        for (File file in files) {
            result.add(Files.copy(file.toPath(), getDstPath(file), StandardCopyOption.REPLACE_EXISTING))
        }

        result
    }

    void commit(List<Path> paths, String message) {
        for (path in paths) {
            def file = new File(path.toString())
            try {
                svnUtils.doImport(file, SVNURL.parseURIEncoded(getCommitUrl(path.toString())), message)
            } catch (SVNException e) {
                svnUtils.doCommit(file, message)
            }
        }
    }

    private String getCheckoutUrl(String settingsKey) {
        "${getRepositoryUrl()}${ext.commitSettings.get(settingsKey)}"
    }

    private String getCommitUrl(String filePath) {
        "${getRepositoryUrl()}${replaceSlash(filePath - project.buildDir)}"
    }

    private String getRepositoryUrl() {
        return ext.isRelease ? ext.commitSettings.get("releaseUrl") : ext.commitSettings.get("testUrl")
    }

    private String replaceSlash(String src) {
        src.replaceAll('\\\\', '/')
    }

    private Path getDstPath(File file) {
        String srcPath = file.path
        if (srcPath.contains('tbz')) {
            Paths.get(srcPath.replace('distributions', 'distfiles'))
        } else {
            Paths.get(srcPath.replace('ebuilds', 'repository'))
        }
    }
}
