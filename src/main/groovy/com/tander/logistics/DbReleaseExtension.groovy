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

    String isCheckReleaseNumberNeeded
    String isUpdateReleaseNumberNeeded
    String isUpdateRevisionNumberNeeded

    String dbReleaseTemplate
    String scmFileTemplate

    LinkedHashMap sectionWildcards
    HashMap settings

    DbReleaseExtension(Project project) {
        this.project = project
    }

    void init(Project project) {
        this.project = project

        settings = project.settings

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

    /**
     * Ищет задачу СППР связанную с проектом. Если релиз ищет в свойствах проекта иначе в имени проекта
     * @return код задачи или {@code null}
     */
    String getSpprDeliveryNumber() {
        return isRelease ? getProjectProperty('spprDeliveryNumber') : project.name.split('-')[1]
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
