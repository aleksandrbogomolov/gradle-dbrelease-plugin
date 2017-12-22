package com.tander.logistics.svn

import com.tander.logistics.DbReleaseExtension
import com.tander.logistics.ui.UiUtils
import org.gradle.api.InvalidUserDataException
import org.tmatesoft.svn.core.SVNErrorMessage
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider
import org.tmatesoft.svn.core.auth.SVNAuthentication
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication

/**
 * Created by durov_an on 08.02.2017.
 */
class SvnAuthProvider implements ISVNAuthenticationProvider {

    DbReleaseExtension ext

    SvnAuthProvider(DbReleaseExtension ext) {
        this.ext = ext
    }

    @Override
    SVNAuthentication requestClientAuthentication(String kind,
                                                  SVNURL svnurl,
                                                  String realm,
                                                  SVNErrorMessage svnErrorMessage,
                                                  SVNAuthentication previousAuth,
                                                  boolean authMayBeStored) {
        Boolean isCanceled
        String scmPass

        ext.user = previousAuth.getUserName()
        def password = ext.project.findProperty("domainPassword")
        if (password) {
            ext.password = password
        } else {
            (scmPass, isCanceled) = UiUtils.promptPassword(
                    "Please enter password to access $realm",
                    "Please enter password to access $realm \n for user ${ext.user}:")
            ext.password = scmPass
        }
        if (isCanceled && svnErrorMessage) {
            throw new InvalidUserDataException(" ${svnErrorMessage.getErrorCode().toString()} \n" +
                    "${svnErrorMessage.toString()} ")
        }

        SVNAuthentication svnAuthenticationNew = SVNPasswordAuthentication.newInstance(ext.user, ext.password.toCharArray(), true, ext.currUrl, false)
        return svnAuthenticationNew
    }

    @Override
    int acceptServerAuthentication(SVNURL svnurl, String s, Object o, boolean b) {
        return ACCEPTED
    }
}
