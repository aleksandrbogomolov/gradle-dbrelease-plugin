package com.tander.logistics.tasks

import com.tander.logistics.DbReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by bogomolov_av on 23.10.2017
 */
class EbuildTask extends DefaultTask {

    DbReleaseExtension ext

    EbuildTask() {
        group = "build"
        description = 'Generate ebuild file'
    }

    @TaskAction
    void run() {
        this.ext = project.extensions.findByName('dbrelease') as DbReleaseExtension
        File destinationDir = new File(project.buildDir, "ebuilds")
        destinationDir.mkdirs()
        StringBuilder template = new StringBuilder("")
        new File("${ext.settings.get('oraEbuildTemplate')}").eachLine { l ->
            if (l.contains('ora')) {
                l = l.substring(0, l.lastIndexOf('-') + 1) + project.version
            }
            template.append("$l\n")
        }
        new File(destinationDir, "${ext.settings.get('ebuildName')}-${project.version}.ebuild").write(template.toString(), "UTF-8")
    }
}
