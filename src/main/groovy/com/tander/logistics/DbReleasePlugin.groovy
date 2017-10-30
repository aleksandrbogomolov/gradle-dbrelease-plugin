package com.tander.logistics

import com.tander.logistics.util.FileUtils
import com.tander.logistics.tasks.DbReleaseBuildTask
import com.tander.logistics.tasks.DbReleaseEbuildTask
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

        String projectName = FileUtils.getProjectName(project)
        dbRelease.isRelease = FileUtils.checkByPattern(projectName, ~/\d+\.\d+\.\d+/)

        project.tasks.create('buildDbRelease', DbReleaseBuildTask)
        project.tasks.create('makeEbuild', DbReleaseEbuildTask)

        setProjectVersion(dbRelease, projectName, project)

        project.afterEvaluate {
            dbRelease.init(project)
        }
    }

    private void setProjectVersion(DbReleaseExtension dbRelease, String projectName, Project project) {
        if (dbRelease.isRelease) {
            project.version = projectName
        } else {
            if (project.version == Project.DEFAULT_VERSION) {
                def names = projectName.split("-")
                project.version = "${names.last()}.${names[1].substring(2)}"
            }
        }
    }
}
