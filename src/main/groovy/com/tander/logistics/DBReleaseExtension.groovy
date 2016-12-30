package com.tander.logistics

/**
 * Created by durov_an on 10.02.2016.
 *
 * Настройки плагина
 */
class DBReleaseExtension {
    String version
    String currURL
    String prevURL
    String scmType
    String user
    String password
    String currUrlRevision
    String prevURLRevision

    def wildacards = [
            'before_section': [
                    '*/before/*.sql',
                    '*.seq',
                    '*/app/*.tab',
                    '*.alt',
                    '*/log/*.tab',
                    '*.alt',
                    '*_rec_t.tps',
                    '*_tab_t.tps',
                    '*.vw',
                    '*.syn',
                    '*.trg',
                    '*.pck',
                    '*.prc',
                    '*.fnc',
                    '*.job'
            ],
            'after_section' : [
                    '*/after/*.sql'
            ]
    ]

    def extensionDropSQL = [
            'pck': 'drop package',
            'tps': 'drop type'
    ]


    String installTemplatePath = 'install.tmpl'

    DBReleaseExtension() {
//        previousInstallSVNURL = previousInstallSVNURL ?: currentInstallSVNURL
//        if (!releaseNumber) {
//            throw new Exception("Не указан параметр releaseNumber")
//        }

    }

}
