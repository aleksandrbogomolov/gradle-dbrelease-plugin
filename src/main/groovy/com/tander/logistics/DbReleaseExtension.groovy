package com.tander.logistics

import org.gradle.api.Project

/**
 * Created by durov_an on 10.02.2016.
 *
 * Настройки плагина
 */
class DbReleaseExtension {

    Project project
    Boolean isTest = false
    String user = ''
    String password = ''

    String spprDeliveryNumber
    String releaseVersion
    String currUrl
    String prevUrl
    String currRevision
    String prevRevision
    String isMonopol
    boolean isRelease

    String isCheckReleaseNumberNeeded
    String isUpdateReleaseNumberNeeded
    String isUpdateRevisionNumberNeeded

    String dbReleaseTemplate
    String scmFileTemplate

    LinkedHashMap sectionWildcards

    void init(Project project) {
        this.project = project

        if (project.hasProperty("currURL")) {
            currUrl = project.property("currURL")
        }

        if (project.hasProperty("prevUrl")) {
            prevUrl = project.property("prevUrl")
        }

        if (project.hasProperty("currRevision")) {
            currRevision = project.property("currRevision")
        }

        if (project.hasProperty("prevRevision")) {
            prevRevision = project.property("prevRevision")
        }

        isMonopol = getProjectProperty('isMonopol') ?: '1'

        dbReleaseTemplate = getProjectProperty('dbReleaseTemplate')

        scmFileTemplate = getProjectProperty('scmFileTemplate')

        spprDeliveryNumber = getSpprDeliveryNumber()

        isCheckReleaseNumberNeeded = project.findProperty("isCheckReleaseNumberNeeded") ?: '1'

        isUpdateReleaseNumberNeeded = project.findProperty("isUpdateReleaseNumberNeeded") ?: '1'

        isUpdateRevisionNumberNeeded = project.findProperty("isUpdateRevisionNumberNeeded") ?: '1'

        if (project.hasProperty("domainUser")) {
            user = project.property("domainUser")
        }

        if (project.hasProperty("domainPassword")) {
            password = project.property("domainPassword")
        }

        if (!password) {
            password = ''
        }

        sectionWildcards = project.sectionWildcards
    }

    DbReleaseExtension(Project project) {
        this.project = project
    }

    String getSpprDeliveryNumber() {
        return project.projectDir.toString().contains('releases') ?
                project.settings.get('spprDeliveryNumber') :
                project.name.split('-')[1]
    }

    String getProjectProperty(String name) {
        return project.findProperty(name) ?: project.settings.get(name)
    }
}
