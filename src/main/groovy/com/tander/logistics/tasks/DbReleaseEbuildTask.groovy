package com.tander.logistics.tasks

import com.tander.logistics.DbReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by bogomolov_av on 23.10.2017
 */
class DbReleaseEbuildTask extends DefaultTask {

    DbReleaseExtension ext

    DbReleaseEbuildTask() {
        group = "build"
        description = 'Generate ebuild file'
    }

    @TaskAction
    void run() {
        this.ext = project.extensions.findByName('dbrelease') as DbReleaseExtension
        File destinationDir = new File(project.buildDir, "ebuilds")
        destinationDir.mkdirs()
        StringBuilder template = new StringBuilder("")
        new File("${ext.getProjectProperty('oraEbuildTemplate')}").eachLine { line ->
            if (line.contains('ora')) {
                line = line.substring(0, line.lastIndexOf('-') + 1) + project.version
            }
            template.append("$line\n")
        }
        new File(destinationDir, "${ext.getProjectProperty('ebuildName')}-${project.version}.ebuild").write(template.toString(), "UTF-8")
    }
}
