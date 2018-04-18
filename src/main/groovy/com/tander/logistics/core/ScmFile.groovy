package com.tander.logistics.core

import org.apache.commons.io.FilenameUtils

/**
 * Created by durov_an on 10.02.2016.
 *
 * Структура для описания скриптов, включаемых в БД релиз
 */
class ScmFile {

    ScriptType scriptType
    String scriptSection
    String name
    String url
    String revision
    String message
    String author
    String taskNumber
    String schema
    Date date
    int wildcardId
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
    }

    boolean checkWildcards(Map<String, List<ScmFile>> schemaWildcards, Map sectionWildcards) {
        for (schema in schemaWildcards.keySet()) {
            if (FilenameUtils.wildcardMatch(name, schema)) {
                this.schema = schema
                for (wildcard in sectionWildcards) {
                    List values = wildcard.value as List<String>
                    for (int i = 0; i < values.size(); i++) {
                        String w = values.get(i)
                        if (FilenameUtils.wildcardMatch(name, w)) {
                            wildcardId = i
                            wildcardsMatched += w + ', '
                            scriptSection = wildcard.key
                            if (!schemaWildcards[schema].contains(this)) {
                                schemaWildcards[schema].add(this)
                            }
                            return true
                        }
                    }
                }
            }
        }
        return false
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
        sb.toString()
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ScmFile scmFile = (ScmFile) o

        if (name != scmFile.name) return false
        if (schema != scmFile.schema) return false
        if (scriptSection != scmFile.scriptSection) return false
        if (scriptType != scmFile.scriptType) return false

        return true
    }

    int hashCode() {
        int result
        result = (scriptType != null ? scriptType.hashCode() : 0)
        result = 31 * result + (scriptSection != null ? scriptSection.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (schema != null ? schema.hashCode() : 0)
        return result
    }
}
