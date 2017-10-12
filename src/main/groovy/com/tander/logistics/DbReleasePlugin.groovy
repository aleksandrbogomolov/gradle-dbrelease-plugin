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
        project.tasks.create('buildDbRelease', BuildDbReleaseTask)

        project.afterEvaluate {
            project.tasks.create('assembly', AssemblyDbRelease)
            dbRelease.init(project)
        }
    }
}
