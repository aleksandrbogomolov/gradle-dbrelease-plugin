package com.tander.logistics.core

import com.tander.logistics.DBReleaseExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging


/**
 * Created by durov_an on 22.12.2016.
 */
class DBRelease {
//    ScriptType scriptType

    Project project
    DBReleaseExtension ext

    String projectDir
    String currURL
    String currURLRevision
    String prevURL
    String prevURLRevision

    Logger logger

    LinkedHashMap wildacards

    LinkedHashMap scmFilesInstall
    LinkedHashMap scmFilesUninstall
    LinkedHashMap scriptSections

    DBRelease(Project project) {
//        this.scriptType = scriptType
        this.project = project
        this.ext = project.dbrelease
        logger = Logging.getLogger(this.class)
        projectDir = project.projectDir
    }


    void assemblyScript(File scriptTemplate) {
        // заполним скрипты по секциям для вставки в ${scriptType}.sql и скопируем файлы

        ext.wildacards.each {
            scriptSections[it.key] = ''
        }

        scmFilesInstall.each { String fileName, SCMFile scmFile ->
//            код для поиска номера задачи в СППР, сейчас не используется т.к. svn list не выводит текст последнего коммита
//            def m = sCMFile.lastMessage =~ /(#SP\d+)/
//            def task = m.group(0)
            scriptSections[scmFile.scriptSection] += "\n-- Revision: $scmFile.lastRevision " +
                    "Date: $scmFile.lastDateFormatted " +
                    "Author: $scmFile.lastAuthor \n"
            scriptSections[scmFile.scriptSection] += "prompt [!] File: @install/$scmFile.name \n"
            scriptSections[scmFile.scriptSection] += "prompt [!] Revision: $scmFile.lastRevision " +
                    "Date: $scmFile.lastDateFormatted " +
                    "Author: $scmFile.lastAuthor \n"
            scriptSections[scmFile.scriptSection] += "@install/$scmFile.name \n"
        }

        // создадим итоговый скрипт с помощью template движка
        def binding = ["information_created"   : "",
                       "information_statistics": "",
                       "log_version"           : "123.lst",
                       "desc_name"             : "",
                       "desc_version"          : "",
                       "current_version"       : "123",
                       "new_version"           : "234"]
        scriptSections.each {
            binding[it.key] = it.value
        }

        DBTemplate installTemplate = new DBTemplate(scriptTemplate)
        installTemplate.makeScript(project.buildDir.path + "script/install.sql", binding)

    }

    // компаратор для сортировки списка файлов. Сперва сортируем по маске файла из настроек, потом по пути к файлу
    Comparator<Map.Entry<String, SCMFile>> scmFileComparatorWildcard = new Comparator<Map.Entry<String, SCMFile>>() {
        @Override
        int compare(Map.Entry<String, SCMFile> o1, Map.Entry<String, SCMFile> o2) {
            if (o1.value.wildcardID > o2.value.wildcardID) {
                return 1
            }
            if (o1.value.wildcardID < o2.value.wildcardID) {
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
}
