package com.tander.logistics

import com.tander.logistics.tasks.BuildDbReleaseTask
import com.tander.logistics.tasks.EbuildTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by durov_an on 04.02.2016.
 *
 * Плагин для сборки релизов складской логистики
 */
class DbReleasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        DbReleaseExtension dbRelease = project.extensions.create('dbrelease', DbReleaseExtension, project)
        dbRelease.isRelease = checkDirName(project.projectDir.toString())
        checkDirName(project.projectDir.toString())
        project.tasks.create('buildDbRelease', BuildDbReleaseTask)
        project.tasks.create('makeEbuild', EbuildTask)

        String projectName = project.getRootDir().getName()
        if (dbRelease.isRelease) {
            project.version = projectName
        } else {
            if (project.version == Project.DEFAULT_VERSION) {
                def names = projectName.split("-")
                project.version = "${names.last()}.${names[1].substring(2)}"
            }
        }

        project.afterEvaluate {
            dbRelease.init(project)
        }
    }

    boolean checkDirName(String dirName) {
        def projectName = dirName.split("\\\\")
        def regex = ~/\d*\.\d*\.\d*/
        return regex.matcher(projectName.last()).matches()
    }
}
