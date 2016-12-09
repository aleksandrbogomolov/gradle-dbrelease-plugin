package com.tander.logistics

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

/**
 * Created by durov_an on 25.02.2016.
 *
 * Формирование итоговых скриптов на основании шаблонов
 */
class WhsDBTemplate {
    def Template template

    def WhsDBTemplate(String templateFilePath) {
        def File templateFile = new File(templateFilePath)
        def SimpleTemplateEngine engine = new SimpleTemplateEngine()
        if (templateFile.exists()) {
            template = engine.createTemplate(templateFile)
        } else {
            template = engine.createTemplate(this.getClass().getResource('/sql-script-templates/install_sql.tmpl').text)
        }
    }

    def makeScript(String scriptFilePath, Map binding) {
        def installSqlFile = new File(scriptFilePath)
        installSqlFile.write(template.make(binding).toString())
    }
}
