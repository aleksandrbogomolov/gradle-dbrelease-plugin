package com.tander.logistics.tasks

import org.gradle.api.DefaultTask

/**
 * Created by durov_an on 09.02.2017.
 */
class AssemblyDbRelease extends DefaultTask {

    AssemblyDbRelease() {
        group = 'distribution'
        description = 'Generate tbz for install and uninstall DB release'
        dependsOn project.tasks.findByName("tarInstall"), project.tasks.findByName("tarUninstall")
    }
}
