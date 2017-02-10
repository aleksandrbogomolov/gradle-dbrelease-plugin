package com.tander.logistics.tasks

import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

/**
 * Created by durov_an on 09.02.2017.
 */
class TarDbRelease extends Tar {
    String scriptType

    TarDbRelease() {
        dependsOn project.tasks.findByName("buildDbRelease")
        group = 'distribution'
        baseName = project.name + "-$scriptType"
        compression = Compression.BZIP2
        extension = "tbz"
        destinationDir = new File(project.buildDir, "distributions")
        into(scriptType) { from { "${project.buildDir}/dbrelease/${scriptType}" } }
        from { "${project.buildDir}/dbrelease/${scriptType}.sql" }
    }
}
