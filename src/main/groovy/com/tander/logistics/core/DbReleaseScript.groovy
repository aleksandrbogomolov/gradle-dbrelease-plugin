package com.tander.logistics.core

import com.tander.logistics.DbReleaseExtension
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging


/**
 * Created by durov_an on 31.01.2017.
 */
class DbReleaseScript {

    protected Logger logger

    ScriptType type
    Project project
    DbReleaseExtension ext
    DbRelease release

    LinkedHashMap<String, ScmFile> scmFiles = []
    LinkedHashMap<String, String> scriptSections = []

    ScmBranch currBranch
    ScmBranch prevBranch

    SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
    File scmFileTemplateFile
    Template scmFileTemplate

    DbReleaseScript(ScriptType scriptType, DbRelease release, Project project) {
        logger = Logging.getLogger(this.class)
        this.type = scriptType
        this.project = project
        this.ext = release.ext
        this.release = release

        scmFileTemplateFile = new File(project.projectDir.toString(), ext.scmFileTemplate)
        if (!scmFileTemplateFile.exists()) {
            throw new Exception("Template not exists: " + scmFileTemplateFile.canonicalPath)
        }

        scmFileTemplate = templateEngine.createTemplate(scmFileTemplateFile)
    }

    void sortScmFiles() {
        scmFiles = scmFiles.entrySet().sort(false, scmFileComparatorWildcard).collectEntries() as LinkedHashMap<String, ScmFile>
    }

    String getStat() {
        String stat = "prompt [INFO] Statistics\n"
        def cnt = scmFiles.countBy { it.value.wildcardsMatched }
        cnt.each {
            stat += "prompt ...[STAT][${it.toString()}] - ${cnt[it.toString()]}\n"
        }
        stat += "prompt [INFO] Statistics\n"
    }

    LinkedHashMap makeBinding() {
        LinkedHashMap binding = []

        binding.clear()
        binding["TMPL_LOG_VERSION"] = "${type.dirName}_log_${currBranch.version}.lst"
        binding["TMPL_DESC_VERSION"] = "${type.dirName} assembly ${currBranch.version}. Installing Software DC Oracle"
        binding["TMPL_CONFIG_CURRENT_VERSION"] = "${prevBranch.version}"
        binding["TMPL_CONFIG_NEW_VERSION"] = "${currBranch.version}"
        binding["TMPL_CONFIG_TASK"] = "${ext.buildTaskNumber}"
        binding["TMPL_CONFIG_ASSEMBLY"] = "${ext.taskNumber}"
        binding["TMPL_CONFIG_DATECREATED"] = "${new Date().format("dd.MM.yyyy HH:mm:ss z", TimeZone.getTimeZone('UTC'))}"
        binding["TMPL_CONFIG_USERCREATED"] = "${ext.user}"
        binding["TMPL_CONFIG_REVISION"] = "${currBranch.getRevisionName()}"
        binding["TMPL_CONFIG_CHECKVERS"] = "${ext.isCheckReleaseNumberNeeded}"
        binding["TMPL_CONFIG_UPDATEVERS"] = "${ext.isUpdateReleaseNumberNeeded}"
        binding["TMPL_CONFIG_RECOMPILING"] = "${scriptSections["TMPL_SCRIPT_AFTER_INSTALL"].toString().length() ? "1" : "0"}"
        binding["TMPL_CONFIG_LISTNODEBUGPACK"] = ""
        binding["TMPL_CONFIG_TOTALBLOCKS"] = "${scmFiles.size()}"
        binding["TMPL_INFORMATION_SATISTICS"] = getStat()
        binding["TMPL_INFORMATION_CREATED"] = """
prompt BranchCurrent: ${currBranch.url} -revision: ${currBranch.getRevisionName()}
prompt BranchPrevios: ${prevBranch.url} -revision: ${prevBranch.getRevisionName()}

"""
        return binding
    }

    void assemblyScript() {
        logger.lifecycle("--------------- generate template start ---------------")
        // заполним скрипты по секциям для вставки в ${type}.sql и скопируем файлы
        ext.sectionWildacards.each {
            scriptSections[it.key as String] = ''
        }

        scmFiles.each { String fileName, ScmFile scmFile ->
            scmFile.scriptType = type
            scriptSections[scmFile.scriptSection] +=
                    scmFileTemplate.make(scmFile.makeBinding()).toString()
        }

        // создадим итоговый скрипт с помощью template движка
        def binding = makeBinding()

        scriptSections.each {
            binding[it.key] = it.value
        }

        DbScriptTemplate installTemplate = new DbScriptTemplate(new File(project.projectDir, ext.dbReleaseTemplate))
        installTemplate.makeScript(release.releaseDir.path + "/${type.dirName}.sql", binding)
        logger.lifecycle("--------------- generate template finish ---------------")
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
}
