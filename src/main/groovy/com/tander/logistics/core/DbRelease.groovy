package com.tander.logistics.core

import com.tander.logistics.DbReleaseExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Created by durov_an on 22.12.2016.
 */
abstract class DbRelease {

    Logger logger
    String RELEASE_PATH = 'dbrelease'
    File releaseDir

    Project project
    DbReleaseExtension ext

    String projectDir

    LinkedHashMap<String, ArrayList<ScmFile>> schemas
    LinkedHashMap wildcards
    DbTemplate scriptInstall
    DbTemplate scriptUninstall

    DbRelease(Project project) {
        this.project = project
        this.ext = project.extensions.findByName('dbrelease') as DbReleaseExtension
        this.schemas = ext.schemas
        this.wildcards = ext.sectionWildcards

        logger = Logging.getLogger(this.class)
        projectDir = project.projectDir

        releaseDir = new File(project.buildDir.getPath(), RELEASE_PATH)
        releaseDir.deleteDir()

        scriptInstall = new DbTemplate(ScriptType.stInstall, this, project)
        scriptUninstall = new DbTemplate(ScriptType.stUninstall, this, project)
    }
}
