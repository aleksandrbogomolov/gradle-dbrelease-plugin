package com.tander.logistics.tasks

import com.tander.logistics.DbReleaseExtension
import com.tander.logistics.core.DbRelease
import com.tander.logistics.svn.SvnDbReleaseBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by durov_an on 17.02.2016.
 *
 * Таск для сборки БД релиза.
 */

class DbReleaseBuildTask extends DefaultTask {

    DbReleaseExtension ext

    DbReleaseBuildTask() {
        group = "build"
        description = 'Generate install and uninstall DB release'
    }

    @TaskAction
    void run() {
        this.ext = project.extensions.findByName('dbrelease') as DbReleaseExtension
        DbRelease dbRelease = new SvnDbReleaseBuilder(project)
        dbRelease.setChangedFilesByDiff()
        dbRelease.exportChangedFilesToDir()
        dbRelease.setLastCommitInfo()
        logger.lifecycle("--------------- generate template start ---------------")
        dbRelease.scriptInstall.assemblyScript()
        dbRelease.scriptUninstall.assemblyScript()
        logger.lifecycle("--------------- generate template finish ---------------")
    }
}
