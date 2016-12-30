package com.tander.logistics.core

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

/**
 * Created by durov_an on 25.02.2016.
 *
 * Формирование итоговых скриптов на основании шаблонов
 */
class DBTemplate {
    Template template

    DBTemplate(String templateFilePath) {
        File templateFile = new File(templateFilePath)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        if (templateFile.exists()) {
            template = engine.createTemplate(templateFile)
        } else {
            template = engine.createTemplate(this.getClass().getResource('/sql-script-templates/install_sql.tmpl').text)
        }
    }

    DBTemplate(File templateFile) {
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        if (templateFile.exists()) {
            template = engine.createTemplate(templateFile)
        } else {
            throw new Exception('Не найден шаблон ' + templateFile.canonicalPath)
        }
    }


    def makeScript(String scriptFilePath, Map binding) {
        def installSqlFile = new File(scriptFilePath)
        installSqlFile.write(template.make(binding).toString())
    }
}
