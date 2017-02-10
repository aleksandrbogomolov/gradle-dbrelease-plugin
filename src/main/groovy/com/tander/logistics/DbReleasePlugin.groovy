package com.tander.logistics

import com.tander.logistics.tasks.AssemblyDbRelease
import com.tander.logistics.tasks.BuildDbReleaseTask
import com.tander.logistics.tasks.TarDbRelease
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

        project.tasks.create('tarInstall', TarDbRelease)
        project.tasks.create('tarUninstall', TarDbRelease)

        project.tasks.create('assemblyDbRelease', AssemblyDbRelease)

        project.afterEvaluate {
            dbRelease.init(project)
        }

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
