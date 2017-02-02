package com.tander.logistics.core

import com.tander.logistics.DbReleaseExtension
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging


/**
 * Created by durov_an on 22.12.2016.
 */
abstract class DbRelease {
    protected Logger logger

    Project project
    DbReleaseExtension ext

    String projectDir

    LinkedHashMap wildacards
    DbReleaseScript scriptInstall
    DbReleaseScript scriptUninstall

    DbRelease(Project project) {
        this.project = project
        this.ext = project.dbrelease
        this.wildacards = ext.wildacards

        logger = Logging.getLogger(this.class)
        projectDir = project.projectDir

        scriptInstall = new DbReleaseScript(ScriptType.stInstall, project)
        scriptUninstall = new DbReleaseScript(ScriptType.stUninstall, project)
    }

}
