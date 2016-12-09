package com.tander.logistics

/**
 * Created by durov_an on 10.02.2016.
 *
 * Структура для описания скриптов, включаемых в БД релиз
 */
class WhsDBFile {
    String name
    String path
    String svnUrl
    String lastRevision
    String lastMessage
    String lastAuthor
    Date lastDate
    String lastDateFormatted
    int wildcardID
    String installSection

    WhsDBFile(String name) {
        this.name = name

    }

}
