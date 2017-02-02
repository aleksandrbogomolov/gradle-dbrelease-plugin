package com.tander.logistics

import org.gradle.api.Project

/**
 * Created by durov_an on 10.02.2016.
 *
 * Настройки плагина
 */
class DbReleaseExtension {
    Project project
    String scmType
    String user
    String password

    String taskNumber
    String releaseVersion
    String currUrl
    String prevUrl
    String currRevision
    String prevRevision

    String isCheckReleaseNumberNeeded
    String isUpdateReleaseNumberNeeded

    String buildTaskNumber


    def wildacards = [
            'TMPL_SCRIPT_BEFORE_INSTALL': [
                    '*/before/*.sql',
                    '*.seq',
                    '*.tab',
                    '*.alt',
                    '*_rec_t.tps',
                    '*_tab_t.tps',
                    '*.vw',
                    '*.mw',
                    '*.syn',
                    '*.trg',
                    '*.qtb',
                    '*.q',
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

    DbReleaseExtension(Project project) {
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


        if (project.hasProperty("taskNumber")) {
            taskNumber = project.property("taskNumber")
        } else {
            taskNumber = "build.gradle"
        }

        if (project.hasProperty("releaseVersion")) {
            releaseVersion = project.property("releaseVersion")
        }


        if (project.hasProperty("isCheckReleaseNumberNeeded")) {
            isCheckReleaseNumberNeeded = project.property("isCheckReleaseNumberNeeded")
        }
        if (project.hasProperty("isUpdateReleaseNumberNeeded")) {
            isUpdateReleaseNumberNeeded = project.property("isUpdateReleaseNumberNeeded")
        }

        buildTaskNumber = buildTaskNumber ?: "build.gradle"
    }


}
