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
            //Создание install set ebuild
            makeEbuild("ebuilds/set", "", "setEbuildTemplate")
            //Создание uninstall set ebuild
            makeEbuild("ebuilds/set", "-uninstall", "setEbuildTemplate")
        }
        if (oraEbuildTemplate) {
            //Создание install tander-tsdserver ebuild
            makeEbuild("ebuilds/tander-tsdserver", "", "oraEbuildTemplate")
            //Создание uninstall tander-tsdserver ebuild
            makeEbuild("ebuilds/tander-tsdserver", "-uninstall", "oraEbuildTemplate")
        }
    }

    private void makeEbuild(String path, String pathSuffix, String templateName) {
        File dir = new File(project.buildDir, "$path/${ext.settings.get("ebuildName")}$pathSuffix")
        dir.mkdirs()
        DbScriptBuilder builder = new DbScriptBuilder(new File(project.projectDir, ext.getProjectProperty(templateName)))
        builder.makeScript(dir.path + "/${ext.getProjectProperty('ebuildName')}$pathSuffix-${project.version}.ebuild", makeTemplateBinding(), "cp1251")
    }

    private LinkedHashMap makeTemplateBinding() {
        LinkedHashMap binding = []
        binding.clear()
        binding["ebuildName"] = "${ext.getProjectProperty('ebuildName')}"
        binding["version"] = "${ext.getProjectProperty('version')}"
        return binding
    }
}
