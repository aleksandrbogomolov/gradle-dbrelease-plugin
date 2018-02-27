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

    String currUrl
    String prevUrl
    String currRevision
    String prevRevision
    String isMonopol
    boolean isRelease
    String spprDeliveryNumber
    String systemName
    String ebuildUrl

    String isCheckReleaseNumberNeeded
    String isUpdateReleaseNumberNeeded
    String isUpdateRevisionNumberNeeded

    String dbReleaseTemplate
    String schemaBeforeTemplate
    String schemaAfterTemplate
    String scmFileTemplate

    LinkedHashMap sectionWildcards
    LinkedHashMap schemaWildcards
    HashMap settings
    HashMap<String, String> commitSettings
    List<String> excludeFiles

    DbReleaseExtension(Project project) {
        this.project = project
    }

    void init(Project project) {
        this.project = project

        settings = project.settings

        commitSettings = project.findProperty('commitSettings') as HashMap<String, String>

        sectionWildcards = project.sectionWildcards

        schemaWildcards = project.schemaWildcards

        excludeFiles = project.excludeFiles

        currUrl = getProjectProperty("currUrl")

        currRevision = getProjectProperty("currRevision")

        prevUrl = getProjectProperty("prevUrl")

        prevRevision = getProjectProperty("prevRevision")

        isMonopol = getProjectProperty('monopol') ?: '1'

        dbReleaseTemplate = getProjectProperty('dbReleaseTemplate')

        schemaBeforeTemplate = getProjectProperty('schemaBeforeTemplate')

        schemaAfterTemplate = getProjectProperty('schemaAfterTemplate')

        scmFileTemplate = getProjectProperty('scmFileTemplate')

        spprDeliveryNumber = getSpprDeliveryNumber()

        isCheckReleaseNumberNeeded = getProjectProperty("isCheckReleaseNumberNeeded") ?: '1'

        isUpdateReleaseNumberNeeded = getProjectProperty("isUpdateReleaseNumberNeeded") ?: '1'

        isUpdateRevisionNumberNeeded = getProjectProperty("isUpdateRevisionNumberNeeded") ?: '1'

        systemName = getProjectProperty("systemName")

        ebuildUrl = getProjectProperty("ebuildUrl")

        if (project.hasProperty("domainUser")) {
            user = project.property("domainUser")
        }

        if (project.hasProperty("domainPassword")) {
            password = project.property("domainPassword")
        }

        if (!password) {
            password = ''
        }
    }

    /**
     * Ищет задачу СППР связанную с проектом. Если релиз, ищет в свойствах проекта иначе в имени проекта
     * @return код задачи или {@code null}
     */
    String getSpprDeliveryNumber() {
        return getProjectProperty('spprDeliveryNumber') ? getProjectProperty('spprDeliveryNumber') : project.name.split('-')[1]
    }

    /**
     * Ищет по имени свойство проекта. Сначала в аргументах командной строки и свойствах проекта, затем в секции
     * "project.ext.settings" в файле "project_settings"
     * @param name имя свойства
     * @return значение свойства или {@code null} если ничего не найдено
     */
    String getProjectProperty(String name) {
        return project.findProperty(name) ?: settings.get(name)
    }
}
