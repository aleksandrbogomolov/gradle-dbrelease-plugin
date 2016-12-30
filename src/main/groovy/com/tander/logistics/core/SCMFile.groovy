package com.tander.logistics.core

import org.apache.commons.io.FilenameUtils

/**
 * Created by durov_an on 10.02.2016.
 *
 * Структура для описания скриптов, включаемых в БД релиз
 */
class SCMFile {
    String name
    String path
    String url
    String lastRevision
    String lastMessage
    String lastAuthor
    String lastTaskNumber
    Date lastDate
    String lastDateFormatted
    int wildcardID
    String scriptSection
    int wildcardMatchCount = 0
    String wildcardsMatched = ""

    void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage
        def m =lastMessage =~ /(#SP\d+)/
        lastTaskNumber = m.group(0)
    }

    SCMFile(String name) {
        this.name = name
    }

    void checkWildcards(LinkedHashMap wildacards) {
        wildacards.each { sectionName, wildcards ->
            wildcards.eachWithIndex { wildcard, i ->
                if (FilenameUtils.wildcardMatch(name, wildcard as String)) {
                    wildcardID = i
                    wildcardMatchCount += 1
                    wildcardsMatched += wildcard + ', '
                    scriptSection = sectionName
                }
            }
        }
        if (wildcardMatchCount > 1) {
            throw new Exception(name + "Multiply wildcards matched: " + wildcardsMatched)
        } else if (wildcardMatchCount == 0) {
            throw new Exception(name + "File not matched by any wildcard ")
        }
    }

}
