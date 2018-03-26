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
    String url
    String revision
    String message
    String author
    String taskNumber
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

    boolean checkWildcards(LinkedHashMap wildcards) {
        for (wildcard in wildcards) {
            List values = wildcard.value as List<String>
            for (int i = 0; i < values.size(); i++) {
                String w = values.get(i)
                if (FilenameUtils.wildcardMatch(name, w)) {
                    wildcardId = i
                    wildcardMatchCount += 1
                    wildcardsMatched += w + ', '
                    scriptSection = wildcard.key
                }
                if (wildcardMatchCount == 1) {
                    break
                }
            }
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

    @Override
    String toString() {
        final StringBuilder sb = new StringBuilder("ScmFile{")
        sb.append(", name='").append(name).append('\'')
        sb.append(", url='").append(url).append('\'')
        sb.append(", revision='").append(revision).append('\'')
        sb.append(", message='").append(message).append('\'')
        sb.append(", author='").append(author).append('\'')
        sb.append(", date=").append(date)
        sb.append('}')
        return sb.toString()
    }
}
