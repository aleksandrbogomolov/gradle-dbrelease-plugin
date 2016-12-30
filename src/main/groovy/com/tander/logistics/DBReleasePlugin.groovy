package com.tander.logistics

import com.tander.logistics.tasks.BuildDBScriptTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by durov_an on 04.02.2016.
 *
 * Плагин для сборки релизов складской логистики
 */
class DBReleasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('dbrelease', DBReleaseExtension)

        def buildDBReleaseTask = project.task('buildDBRelease', type: BuildDBScriptTask)

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

        project.task('generateSQLTemplateFile') << {
            def templateFile = new File('install.tmpl')
            if (templateFile.exists()) {
                throw new Exception('Шаблон ' + templateFile.getAbsolutePath() + ' уже существует')
            } else {
                templateFile.write(this.getClass().getResource('/sql-script-templates/install_sql.tmpl').text)
            }
        }
        project.tasks.getByName("generateSQLTemplateFile").description = "Создание локальной версии шаблона install.tmpl"

    }

}
