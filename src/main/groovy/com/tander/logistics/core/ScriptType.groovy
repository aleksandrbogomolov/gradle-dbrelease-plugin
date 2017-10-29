package com.tander.logistics.core

/**
 * Created by durov_an on 21.12.2016.
 *
 * Типы скриптов
 */
enum ScriptType {

    stInstall("install"),
    stUninstall("uninstall")

    private ScriptType (String dirName) {
        this.dirName = dirName
    }

    final String dirName
}
