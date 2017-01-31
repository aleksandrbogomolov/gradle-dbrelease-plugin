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
    String scmUrl

    LinkedHashMap wildacards
    LinkedHashMap<String, ScmFile> scmFilesInstall = []
    LinkedHashMap<String, ScmFile> scmFilesUninstall = []
    LinkedHashMap scriptSections = []

    SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
    File scmFileTemplateFile
    Template scmFileTemplate

    DbRelease(Project project) {
        this.project = project
        this.ext = project.dbrelease
        this.wildacards = ext.wildacards

        logger = Logging.getLogger(this.class)
        projectDir = project.projectDir

        scmFileTemplateFile = new File(ext.scmFileTemplate)
        if (!scmFileTemplateFile.exists()) {
            throw new Exception("Template not exists: " + scmFileTemplateFile.canonicalPath)
        }

        scmFileTemplate = templateEngine.createTemplate(scmFileTemplateFile)
    }

    void assemblyScript() {
        // заполним скрипты по секциям для вставки в ${scriptType}.sql и скопируем файлы
        ext.wildacards.each {
            scriptSections[it.key] = ''
        }

        scmFilesInstall.each { String fileName, ScmFile scmFile ->
            scmFile.scriptType = ScriptType.stInstall
            scriptSections[scmFile.scriptSection] +=
                    scmFileTemplate.make(scmFile.makeBinding()).toString()
        }

        // создадим итоговый скрипт с помощью template движка
        def binding = makeBinding(ScriptType.stInstall)

        scriptSections.each {
            binding[it.key] = it.value
        }

        DbScriptTemplate installTemplate = new DbScriptTemplate(ext.dbReleaseTemplate)
        installTemplate.makeScript(project.buildDir.path + "/install.sql", binding)
    }

    // компаратор для сортировки списка файлов. Сперва сортируем по маске файла из настроек, потом по пути к файлу
    Comparator<Map.Entry<String, ScmFile>> scmFileComparatorWildcard = new Comparator<Map.Entry<String, ScmFile>>() {
        @Override
        int compare(Map.Entry<String, ScmFile> o1, Map.Entry<String, ScmFile> o2) {
            if (o1.value.wildcardId > o2.value.wildcardId) {
                return 1
            }
            if (o1.value.wildcardId < o2.value.wildcardId) {
                return -1
            }
            if (o1.value.name > o2.value.name) {
                return 1
            }
            if (o1.value.name < o2.value.name) {
                return -1
            }
            return 0
        }
    }

    String getVersion() {
//        if (ext.releaseVersion) {
//            return ext.releaseVersion
//        } else {
//            return
//        }

    }

    LinkedHashMap makeBinding(ScriptType type) {

    }
}
