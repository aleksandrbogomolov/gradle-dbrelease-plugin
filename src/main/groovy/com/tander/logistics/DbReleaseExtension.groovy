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

    String taskNumber
    String releaseVersion
    String currUrl
    String prevUrl
    String currRevision
    String prevRevision
    String monopol
    String packageName

    String isCheckReleaseNumberNeeded
    String isUpdateReleaseNumberNeeded
    String isUpdateRevisionNumberNeeded

    String buildTaskNumber

    LinkedHashMap sectionWildcards = [
            'TMPL_SCRIPT_BEFORE_INSTALL': [
                    '*/before/*.sql',
                    '*.ind',
                    '*.seq',
                    '*.tab',
                    '*.alt',
                    '*_rec_t.tps',
                    '*_tab_t.tps',
                    '*.q',
                    '*.tpsalt',
                    '*.vw',
                    '*.mw',
                    '*.syn',
                    '*.trg',
                    '*.qtb',
                    '*.pck',
                    '*.prc',
                    '*.fnc',
                    '*.dic',
                    '*.job'
            ],
            'TMPL_SCRIPT_AFTER_INSTALL' : [
                    '*/after/*.sql'
            ]
    ]

    def extensionDropSQL = [
            'pck': 'drop package',
            'tps': 'drop type'
    ]

    String dbReleaseTemplate = 'dbrelease.sql.tmpl'
    String scmFileTemplate = 'scmfile.sql.tmpl'

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

        monopol = project.findProperty("monopol") ?: "1"

        taskNumber = project.findProperty("taskNumber") ?: "номер задачи СППР не заполнен"

        isCheckReleaseNumberNeeded = project.findProperty("isCheckReleaseNumberNeeded") ?: "1"

        isUpdateReleaseNumberNeeded = project.findProperty("isUpdateReleaseNumberNeeded") ?: "1"

        isUpdateRevisionNumberNeeded = project.findProperty("isUpdateRevisionNumberNeeded") ?: "1"

        buildTaskNumber = buildTaskNumber ?: "номер задачи сборки не заполнен"

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

    DbReleaseExtension(Project project) {
        this.project = project
    }
}
