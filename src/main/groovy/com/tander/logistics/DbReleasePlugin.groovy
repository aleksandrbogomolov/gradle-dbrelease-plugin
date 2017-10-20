package com.tander.logistics

import com.tander.logistics.tasks.AssemblyDbRelease
import com.tander.logistics.tasks.BuildDbReleaseTask
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
        dbRelease.isRelease = project.projectDir.toString().contains('releases')
        project.tasks.create('buildDbRelease', BuildDbReleaseTask)

        String projectName = project.getRootDir().getName()
        if (dbRelease.isRelease) {
            dbRelease.releaseVersion = projectName
        } else {
            def names = projectName.split("-")
            dbRelease.releaseVersion = "${names.last()}.${names[1].substring(2)}"
        }

        project.afterEvaluate {
            project.tasks.create('assembly', AssemblyDbRelease)
            dbRelease.init(project)
        }
    }
}
