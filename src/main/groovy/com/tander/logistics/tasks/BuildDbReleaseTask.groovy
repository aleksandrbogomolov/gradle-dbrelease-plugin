package com.tander.logistics.tasks

import com.tander.logistics.DbReleaseExtension
import com.tander.logistics.core.DbRelease
import com.tander.logistics.svn.DbReleaseSvn
import com.tander.logistics.ui.UiUtils
import groovy.swing.SwingBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

/**
 * Created by durov_an on 17.02.2016.
 *
 * Таск для сборки БД релиза.
 */

class BuildDbReleaseTask extends DefaultTask {

    DbReleaseExtension ext

    BuildDbReleaseTask() {
        group = "build"
        description = 'Generate install and uninstall DB release'
    }

    @TaskAction
    void run() {
        this.ext = project.extensions.findByName('dbrelease') as DbReleaseExtension

        if (project.hasProperty("domainPassword")) {
            ext.password = project.property("domainPassword")
        } else if (!ext.isTest) {
            ext.password = UiUtils.promptPassword(
                    "Please enter password",
                    "Please enter password for user $ext.user:")
        }
        if (ext.password.size() <= 0) {
            throw new InvalidUserDataException("You must enter a password to proceed.")
        }

        DbRelease dbRelease = new DbReleaseSvn(project)

        dbRelease.setChangedFilesByDiff()
        dbRelease.setLastCommitInfo()
        dbRelease.exportChangedFilesToDir()
        dbRelease.scriptInstall.assemblyScript()
        dbRelease.scriptUninstall.assemblyScript()

    }
}
