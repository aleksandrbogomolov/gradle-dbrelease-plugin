package com.tander.logistics.tasks

import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

/**
 * Created by durov_an on 09.02.2017.
 */
class DbReleaseTarTask extends Tar {

    DbReleaseTarTask() {
        dependsOn project.tasks.findByName("buildDbRelease")
        group = 'distribution'
        compression = Compression.BZIP2
        extension = "tbz"
        destinationDir = new File(project.buildDir, "distributions")
    }
}
