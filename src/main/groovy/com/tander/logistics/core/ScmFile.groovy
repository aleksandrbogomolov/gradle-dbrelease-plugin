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
                    wildcardId = i
                    wildcardMatchCount += 1
                    wildcardsMatched += wildcard + ', '
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

    String getReleaseString() {
        String releaseString = """
------------------------------------------------FILE--------------------------------------------------------------
 --{TMPL.INSTALL.COUNTBLOCK}--
 exec :totalPrc := round((:totalBlocksInstall * 100) / :totalBlocks);
 exec dbms_output.put_line('---------------------------[ Completed - '||:totalPrc||'% ]-------------------------------');
 -- Revision: $revision Task: $taskNumber Date: $dateFormatted Author: $author
 exec dbms_output.put_line(:separatorList);
 exec dbms_output.put_line('......[FILE] @${scriptType.dirName}/$name');
 exec dbms_output.put_line('.........[REVISION] $revision');
 exec dbms_output.put_line('.........[TASK] $taskNumber');
 exec dbms_output.put_line('.........[DATE] $dateFormatted');
 exec dbms_output.put_line('.........[AUTHOR] $author');
 exec dbms_output.put_line('.........[START] ' || to_char(sysdate,'dd.mm.yyyy hh24:mi:ss'));
 @${scriptType.dirName}/$name
 exec dbms_output.put_line('.........[FINISH] '|| to_char(sysdate,'dd.mm.yyyy hh24:mi:ss'));
 exec dbms_output.put_line('......[FILE]');
 exec dbms_output.put_line(:separatorList);
 exec :totalBlocksInstall := :totalBlocksInstall + 1;
-----------------------------------------------/FILE/-------------------------------------------------------------
"""
        return releaseString
    }

}
