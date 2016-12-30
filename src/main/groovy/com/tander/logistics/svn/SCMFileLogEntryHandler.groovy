package com.tander.logistics.svn

import com.tander.logistics.core.SCMFile
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNLogEntry

/**
 * Created by durov_an on 22.12.2016.
 */
class SCMFileLogEntryHandler implements ISVNLogEntryHandler {
    SCMFile scmFile

    void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
        scmFile.lastAuthor = logEntry.getAuthor()
        scmFile.lastDate = logEntry.getDate()
        scmFile.lastMessage = logEntry.getMessage()
        scmFile.lastRevision = logEntry.getRevision()
    }
}
