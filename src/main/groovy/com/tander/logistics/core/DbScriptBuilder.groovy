package com.tander.logistics.core

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

/**
 * Created by durov_an on 25.02.2016.
 *
 * Формирование итоговых скриптов на основании шаблонов
 */
class DbScriptBuilder {

    Template template

    DbScriptBuilder(String templateFilePath) {
        File templateFile = new File(templateFilePath)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        template = engine.createTemplate(templateFile)
        if (!templateFile.exists()) {
            template = engine.createTemplate(templateFile)
        } else {
            throw new Exception('Cant find template: ' + templateFile.canonicalPath)
        }
    }

    DbScriptBuilder(File templateFile) {
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        if (templateFile.exists()) {
            template = engine.createTemplate(templateFile)
        } else {
            throw new Exception('Не найден шаблон ' + templateFile.canonicalPath)
        }
    }

    def makeScript(String scriptFilePath, Map binding) {
        def installSqlFile = new File(scriptFilePath)
        installSqlFile.write(template.make(binding).toString(), 'Cp1251')
    }
}
