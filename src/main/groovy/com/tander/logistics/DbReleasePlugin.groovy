package com.tander.logistics

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
        project.extensions.create('dbrelease', DbReleaseExtension, project)
        project.tasks.create('buildDbRelease', BuildDbReleaseTask)

//        def makeInstallTarTask = project.task('makeInstallTar', type: Tar) {
//            compression = Compression.BZIP2
//            extension = 'tbz'
//            baseName = "install-tartask"
//            description "Create a .tar.gz artifact containing the service"
//            from (project.buildDir.getAbsolutePath() + 'release/' + "install/")
//            dependsOn buildDBReleaseTask
//            inputs.dir new File(project.buildDir.getAbsolutePath() + 'release/' + "install/")
//            outputs.dir new File(project.buildDir.getAbsolutePath() + 'distribution/')
//        }


    }

}
