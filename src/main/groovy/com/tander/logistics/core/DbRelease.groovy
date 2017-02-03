package com.tander.logistics.core

import com.tander.logistics.DbReleaseExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging


/**
 * Created by durov_an on 22.12.2016.
 */
abstract class DbRelease {
    protected Logger logger
    String RELEASE_PATH = 'dbrelease'
    File releaseDir

    Project project
    DbReleaseExtension ext

    String projectDir

    LinkedHashMap wildacards
    DbReleaseScript scriptInstall
    DbReleaseScript scriptUninstall

    DbRelease(Project project) {
        this.project = project
        this.ext = project.dbrelease
        this.wildacards = ext.sectionWildacards


        logger = Logging.getLogger(this.class)
        projectDir = project.projectDir

        releaseDir = new File(project.buildDir.getPath(), RELEASE_PATH)
        releaseDir.deleteDir()
//        releaseDir

        scriptInstall = new DbReleaseScript(ScriptType.stInstall, this, project)
        scriptUninstall = new DbReleaseScript(ScriptType.stUninstall, this, project)
    }

}
