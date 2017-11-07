package com.tander.logistics.core

import org.apache.commons.io.FilenameUtils
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Created by durov_an on 10.02.2016.
 *
 * Структура для описания скриптов, включаемых в БД релиз
 */
class ScmFile {

    Logger logger

    ScriptType scriptType
    String name
    String url
    String revision
    String message
    String author
    String taskNumber
    String schema
    Date date
    int wildcardId
    String scriptSection
    int wildcardMatchCount = 0
    String wildcardsMatched = ""
    boolean isUninstall

    LinkedHashMap binding = []

    void setMessage(String message) {
        this.message = message
        def m = message =~ /(?s)(#SP\d+).*/
        if (m.matches()) {
            taskNumber = m.group(1)
        } else {
            taskNumber = ""
        }
    }

    ScmFile(String name) {
        this.name = name
        logger = Logging.getLogger(this.class)
    }

    boolean checkWildcards(Map<String, List<ScmFile>> schemas, Map wildcards) {
        for (s in schemas.keySet()) {
            if (FilenameUtils.wildcardMatch(name, s)) {
                schema = s
                for (wildcard in wildcards) {
                    List values = wildcard.value as List<String>
                    for (int i = 0; i < values.size(); i++) {
                        String w = values.get(i)
                        if (FilenameUtils.wildcardMatch(name, w)) {
                            wildcardId = i
                            wildcardMatchCount += 1
                            wildcardsMatched += w + ', '
                            scriptSection = wildcard.key
                            schemas[s].add(this)
                        }
                        if (wildcardMatchCount == 1) {
                            break
                        }
                    }
                }
                break
            }
        }
        if (wildcardMatchCount == 0) {
            logger.warn("$name - file not matched by any wildcard")
        }
        return wildcardMatchCount == 1
    }

    LinkedHashMap makeBinding() {
        binding.clear()
        binding["showRevisionInfo"] = scriptType == ScriptType.stInstall
        if (scriptType == ScriptType.stInstall) {
            binding["revision"] = revision
            binding["task"] = taskNumber
            binding["date"] = date.format("dd.MM.yyyy HH:mm:ss z", TimeZone.getTimeZone('UTC'))
            binding["author"] = author
        }
        binding["type"] = scriptType.dirName
        binding["name"] = name
        return binding
    }
}
