package com.tander.logistics.tasks

import com.tander.logistics.DbReleaseExtension
import com.tander.logistics.core.DbScriptBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by bogomolov_av on 23.10.2017
 */
class DbReleaseEbuildTask extends DefaultTask {

    DbReleaseExtension ext

    DbReleaseEbuildTask() {
        group = "build"
        description = 'Generate ebuild file'
    }

    @TaskAction
    void run() {
        this.ext = project.extensions.findByName('dbrelease') as DbReleaseExtension
        String setEbuildTemplate = ext.getProjectProperty('setEbuildTemplate')
        String oraEbuildTemplate = ext.getProjectProperty('oraEbuildTemplate')
        if (setEbuildTemplate) {
            File setEbuildDir = new File(project.buildDir, "ebuilds/set")
            setEbuildDir.mkdirs()
            //Создание install set ebuild
            DbScriptBuilder installSetTemplate = new DbScriptBuilder(new File(project.projectDir, ext.getProjectProperty('setEbuildTemplate')))
            installSetTemplate.makeScript("${project.buildDir}/ebuilds/set/" + "${ext.getProjectProperty('ebuildName')}-${project.version}.ebuild", makeTemplateBinding(), "cp1251")
            //Создание uninstall set ebuild
            DbScriptBuilder uninstallSetTemplate = new DbScriptBuilder(new File(project.projectDir, ext.getProjectProperty('setEbuildTemplate')))
            uninstallSetTemplate.makeScript("${project.buildDir}/ebuilds/set/" + "${ext.getProjectProperty('ebuildName')}-uninstall-${project.version}.ebuild", makeTemplateBinding(), "cp1251")
        }
        if (oraEbuildTemplate) {
            File oraEbuildDir = new File(project.buildDir, "ebuilds/ora")
            oraEbuildDir.mkdirs()
            //Создание install tander-tsdserver ebuild
            DbScriptBuilder installOraTemplate = new DbScriptBuilder(new File(project.projectDir, ext.getProjectProperty('oraEbuildTemplate')))
            installOraTemplate.makeScript("${project.buildDir}/ebuilds/ora/" + "${ext.getProjectProperty('ebuildName')}-${project.version}.ebuild", new HashMap(), "cp1251")
            //Создание uninstall tander-tsdserver ebuild
            DbScriptBuilder uninstallOraTemplate = new DbScriptBuilder(new File(project.projectDir, ext.getProjectProperty('oraEbuildTemplate')))
            uninstallOraTemplate.makeScript("${project.buildDir}/ebuilds/ora/" + "${ext.getProjectProperty('ebuildName')}-uninstall-${project.version}.ebuild", new HashMap(), "cp1251")
        }
    }

    private LinkedHashMap makeTemplateBinding() {
        LinkedHashMap binding = []
        binding.clear()
        binding["ebuildName"] = "${ext.getProjectProperty('ebuildName')}"
        binding["version"] = "${ext.getProjectProperty('version')}"
        return binding
    }
}
