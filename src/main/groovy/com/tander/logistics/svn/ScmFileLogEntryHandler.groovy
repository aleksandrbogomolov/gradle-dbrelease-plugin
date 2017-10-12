package com.tander.logistics.svn

import com.tander.logistics.core.ScmFile
import org.gradle.api.logging.Logger
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNLogEntry

/**
 * Created by durov_an on 22.12.2016.
 */
class ScmFileLogEntryHandler implements ISVNLogEntryHandler {

    ScmFile scmFile
    Logger logger

    @Override
    void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
        logger.lifecycle("get revision info " + scmFile.name)
        scmFile.author = logEntry.getAuthor()
        scmFile.date = logEntry.getDate()
        scmFile.message = logEntry.getMessage()
        scmFile.revision = logEntry.getRevision()
    }
}
