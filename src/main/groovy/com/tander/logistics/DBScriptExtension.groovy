package com.tander.logistics


/**
 * Created by durov_an on 10.02.2016.
 *
 * Настройки плагина
 */
class DBScriptExtension {
    String version
    String currentInstallSVNURL
    String currentUninstallSVNURL
    String previousInstallSVNURL
    String previousReleaseNumber
    String releaseNumber
    String releaseTask
    String projectType
    String svnUsername
    String svnPassword
    String spprTask
    int currentInstallRevision
    int previousInstallRevision
    boolean showSvnExportLog = false
    boolean showSvnDiffLog = false
    boolean showFileCopyLog = false
    boolean showCompressLog = false
    boolean showLastCommiter = true

    def acceptedFilesWildcards = [
            'sql' : [
                    '*.sql'
            ]
    ]

    def installSectionWildacards = [
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
    def uninstallSectionWildacards = [
            'before_section': [
                    '*/before/*.sql',
                    '*.seq',
                    '*.tab',
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


    def installTemplatePath = 'install.tmpl'

}
