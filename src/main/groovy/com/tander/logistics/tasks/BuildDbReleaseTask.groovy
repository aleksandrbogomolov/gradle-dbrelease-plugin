package com.tander.logistics.tasks

import com.tander.logistics.core.DbRelease
import com.tander.logistics.core.DbScriptTemplate
import com.tander.logistics.DbReleaseExtension
import com.tander.logistics.core.ScmFile
import com.tander.logistics.svn.DbReleaseSvn
import com.tander.logistics.core.FileUtils
import com.tander.logistics.core.ScriptType
import com.tander.logistics.svn.SvnUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.wc.ISVNEventHandler
import org.tmatesoft.svn.core.wc.SVNEvent
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNWCUtil

/**
 * Created by durov_an on 17.02.2016.
 *
 * Таск для сборки БД релиза.
 */


class BuildDbReleaseTask extends DefaultTask {

    String RELEASE_PATH = '/release/'

    BuildDbReleaseTask() {
        group = "build"
        description = 'Generate install and uninstall DB release'
    }

    @TaskAction
    void run() {
        File releaseDir = new File(project.buildDir.getPath() + RELEASE_PATH)
        releaseDir.deleteDir()

        DbRelease dbRelease = new DbReleaseSvn(project)

        dbRelease.setChangedFilesByDiff()
        dbRelease.setLastCommitInfo()
        dbRelease.exportChangedFilesToDir()
        dbRelease.scriptInstall.assemblyScript()
        dbRelease.scriptUninstall.assemblyScript()

    }
}
