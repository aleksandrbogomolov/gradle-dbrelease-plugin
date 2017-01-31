package com.tander.logistics.core

/**
 * Created by durov_an on 31.01.2017.
 */
class DbReleaseScript {

    ScriptType scriptType
    LinkedHashMap<String, ScmFile> scmFiles = []

    DbReleaseScript(ScriptType scriptType) {
        this.scriptType = scriptType
    }
}

