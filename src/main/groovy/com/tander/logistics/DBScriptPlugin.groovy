package com.tander.logistics

import com.tander.logistics.tasks.WhsBuildDBReleaseTask
import com.tander.logistics.utils.WhsUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

/**
 * Created by durov_an on 04.02.2016.
 *
 * Плагин для сборки релизов складской логистики
 */
class DBScriptPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('whsrelease', DBScriptExtension)

        def buildDBReleaseTask = project.task('buildDBRelease', type: WhsBuildDBReleaseTask)
        def buildFileReleaseTask = project.task('buildFileRelease') << {

        }

        def buildReleaseTask =  project.task('buildRelease').dependsOn(buildDBReleaseTask, buildFileReleaseTask)

//        def paludisPackageTask = project.task('paludisPackage', type: PaludisPackageTask)

        def makeInstallTarTask = project.task('makeInstallTar', type: Tar) {
            compression = Compression.BZIP2
            extension = 'tbz'
            baseName = "install-tartask"
            description "Create a .tar.gz artifact containing the service"
            from (project.buildDir.getAbsolutePath() + 'release/' + "install/")
            dependsOn buildDBReleaseTask
            inputs.dir new File(project.buildDir.getAbsolutePath() + 'release/' + "install/")
            outputs.dir new File(project.buildDir.getAbsolutePath() + 'distribution/')
        }

//        project.task('makeTar', type: Tar) {
//            compression = Compression.BZIP2
//            extension = 'tbz'
//            baseName = "${releaseNumber}-${scriptType}-tartask"
//            destinationDir = new File(project.buildDir.path+'/distribution')
//            from (project.buildDir.getAbsolutePath() + RELEASE_PATH + "${scriptType}/")
//        }

//        project.configure(project) {
//            apply plugin:'distribution'
//
//
//        }



//        project.tasks.create(name: 'compileTask', type: Tar) {
//            compression = Compression.BZIP2
//            description = "Compiles translated files into outputDir"
//            translatedFiles = fileTree(tasks.translateTask.outputs.files.singleFile) {
//                includes [ '**/*.m' ]
//                builtBy tasks.translateTask
//            }
//            outputDir = file("${buildDir}/compiledBinary")
//        }

        project.task('generateSQLTemplateFile') << {
            def templateFile = new File('install.tmpl')
            if (templateFile.exists()) {
                throw new Exception('Шаблон ' + templateFile.getAbsolutePath() + ' уже существует')
            } else {
                templateFile.write(this.getClass().getResource('/sql-script-templates/install_sql.tmpl').text)
            }
        }

        project.task('createTBZ') << {
            WhsUtils.CreateTarBZ(project.buildDir.getAbsolutePath() + '/release/install/',
                    project.buildDir.getAbsolutePath() + '/release/install' + '.tbz')
        }

        buildDBReleaseTask.description = "Сборка БД релиза"
        buildFileReleaseTask.description = "Сборка файлового релиза"
        project.tasks.getByName("generateSQLTemplateFile").description = "Создание локальной версии шаблона install.tmpl"
        project.tasks.getByName("createTBZ").description = "демо таск, для создания tbz файла"

    }

}
