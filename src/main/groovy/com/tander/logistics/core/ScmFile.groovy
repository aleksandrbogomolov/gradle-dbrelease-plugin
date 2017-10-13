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

    protected Logger logger

    ScriptType scriptType
    String name
    String path
    String url
    String revision
    String message
    String author
    String taskNumber
    Date date
    String dateFormatted
    int wildcardId
    String scriptSection
    int wildcardMatchCount = 0
    String wildcardsMatched = ""
    boolean getFromUninstall

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

    void checkWildcards(LinkedHashMap wildacards) {
        wildacards.each { sectionName, wildcards ->
            wildcards.eachWithIndex { wildcard, i ->
                if (FilenameUtils.wildcardMatch(name, wildcard as String)) {
                    wildcardId = i as int
                    wildcardMatchCount += 1
                    wildcardsMatched += (wildcard as String) + ', '
                    scriptSection = sectionName
                }
            }
        }
        if (wildcardMatchCount > 1) {
//            logger.warn(name + " Multiply wildcards matched: " + wildcardsMatched)
            throw new Exception(name + " Multiply wildcards matched: " + wildcardsMatched)
        } else if (wildcardMatchCount == 0) {
            logger.warn(name + " File not matched by any wildcard ")
        }
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
