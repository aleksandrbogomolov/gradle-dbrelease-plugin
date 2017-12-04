package com.tander.logistics.core

import com.tander.logistics.DbReleaseExtension
import com.tander.logistics.util.FileUtils
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopActionException

/**
 * Created by durov_an on 31.01.2017.
 */
class DbTemplate {

    Logger logger

    ScriptType type
    Project project
    DbReleaseExtension ext
    DbRelease release

    LinkedHashMap<String, LinkedHashMap<String, String>> schemas = []
    LinkedHashMap<String, ScmFile> scmFiles = []

    ScmBranch currBranch
    ScmBranch prevBranch

    SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
    File schemaBeforeTemplateFile
    File schemaAfterTemplateFile
    File scmFileTemplateFile
    Template schemaBeforeTemplate
    Template schemaAfterTemplate
    Template scmFileTemplate

    DbTemplate(ScriptType scriptType, DbRelease release, Project project) {
        logger = Logging.getLogger(this.class)
        this.type = scriptType
        this.project = project
        this.ext = release.ext
        this.release = release

        schemaBeforeTemplateFile = new File(project.projectDir.toString(), ext.schemaBeforeTemplate)
        if (!schemaBeforeTemplateFile.exists()) {
            throw new StopActionException("Template not exists: " + schemaBeforeTemplateFile.canonicalPath)
        }
        schemaAfterTemplateFile = new File(project.projectDir.toString(), ext.schemaAfterTemplate)
        if (!schemaAfterTemplateFile.exists()) {
            throw new StopActionException("Template not exists: " + schemaAfterTemplateFile.canonicalPath)
        }
        scmFileTemplateFile = new File(project.projectDir.toString(), ext.scmFileTemplate)
        if (!scmFileTemplateFile.exists()) {
            throw new Exception("Template not exists: " + scmFileTemplateFile.canonicalPath)
        }

        schemaBeforeTemplate = templateEngine.createTemplate(schemaBeforeTemplateFile)
        schemaAfterTemplate = templateEngine.createTemplate(schemaAfterTemplateFile)
        scmFileTemplate = templateEngine.createTemplate(scmFileTemplateFile)
    }

    String getStat() {
        String stat = "prompt ...[INFO] Statistics\n"
        def cnt = scmFiles.countBy { it.value.wildcardsMatched }
        cnt.each { k, v ->
            stat += "prompt ...[STAT][${k.padLeft(22)}] - $v\n"
        }
        stat += "prompt ...[INFO] Statistics\n"
    }

    LinkedHashMap makeTemplateHeadBinding() {
        LinkedHashMap binding = []

        binding.clear()
        binding["TMPL_LOG_VERSION"] = "${type.dirName}_log_${project.version}.lst"
        binding["TMPL_DESC_VERSION"] = "$type.dirName assembly ${currBranch.version}."
        binding["TMPL_CONFIG_PREVIOUS_VERSION"] = prevBranch.version
        binding["TMPL_CONFIG_NEW_VERSION"] = currBranch.version
        binding["TMPL_CONFIG_NEW_REVISION"] = currBranch.revisionName
        binding["TMPL_CONFIG_TASK"] = ext.spprDeliveryNumber
        binding["TMPL_CONFIG_DATECREATED"] = "${new Date().format("dd.MM.yyyy HH:mm:ss z", TimeZone.getTimeZone('UTC'))}"
        binding["TMPL_CONFIG_USERCREATED"] = ext.user
        binding["TMPL_CONFIG_MONOPOL"] = ext.isMonopol
        binding["TMPL_CONFIG_CHECKVERS"] = ext.isCheckReleaseNumberNeeded
        binding["TMPL_CONFIG_CHECKREVISION"] = ""
        binding["TMPL_CONFIG_UPDATEVERS"] = ext.isUpdateReleaseNumberNeeded
        binding["TMPL_CONFIG_UPDATEREVISION"] = ext.isUpdateRevisionNumberNeeded
        binding["TMPL_CONFIG_LISTNODEBUGPACK"] = "0"
        binding["TMPL_CONFIG_TOTALBLOCKS"] = scmFiles.size()
        binding["TMPL_INFORMATION_STATISTICS"] = getStat()
        binding["TMPL_CONFIG_SYSTEMNAME"] = ext.systemName
        binding["TMPL_INFORMATION_CREATED"] = """
prompt BranchCurrent: ${currBranch.url} -revision: ${currBranch.getRevisionName()}
prompt BranchPrevios: ${prevBranch.url} -revision: ${prevBranch.getRevisionName()}

"""
        return binding
    }

    void assemblyScript() {

        release.schemas.values().each {
            it.sort(FileUtils.schemaFileComparator)
        }

        release.schemas.each {
            if (!it.value.isEmpty()) {
                schemas[getSchemaName(it.key as String)] = makeSchemaFileBinding(it.value)
            }
        }

        def binding = makeTemplateHeadBinding()

        schemas.each { k, v ->
            if (binding.get(v.keySet()[0])) {
                binding[v.keySet()[0]] += schemaBeforeTemplate.make(makeSchemaBinding(k)).toString()
            } else {
                binding[v.keySet()[0]] = schemaBeforeTemplate.make(makeSchemaBinding(k)).toString()
            }
            v.each {
                if (binding.get(it.key)) {
                    binding[it.key] += it.value
                } else {
                    binding[it.key] = it.value
                }
                binding[it.key] += schemaAfterTemplate.make(makeSchemaBinding(k)).toString()
            }
        }

        DbScriptBuilder installTemplate = new DbScriptBuilder(new File(project.projectDir, ext.dbReleaseTemplate))
        installTemplate.makeScript(release.releaseDir.path + "/${type.dirName}.sql", binding)
    }

    LinkedHashMap makeSchemaBinding(String schema) {
        LinkedHashMap binding = []
        binding["SCHEMA_NAME"] = schema.toUpperCase()
        return binding
    }

    /**
     * Очищает имя схемы от использованных для шаблона знаков
     * @param schemaWildcard имя схемы из project.ext.schemas словаря
     * @return очищенное от посторонних символов имя схемы в верхнем регистре
     */
    String getSchemaName(String schemaWildcard) {
        String result = schemaWildcard.replaceAll(~/\W/, '')
        return result.toUpperCase()
    }

    LinkedHashMap makeSchemaFileBinding(List scmFiles) {
        LinkedHashMap<String, String> scriptSections = []
        ext.sectionWildcards.each {
            scriptSections[it.key as String] = ''
        }
        scmFiles.each { ScmFile scmFile ->
            scmFile.scriptType = type
            if (this.scmFiles.keySet().contains(scmFile.name)) {
                scriptSections[scmFile.scriptSection] += scmFileTemplate.make(scmFile.makeBinding()).toString()
            }
        }
        return scriptSections
    }
}
