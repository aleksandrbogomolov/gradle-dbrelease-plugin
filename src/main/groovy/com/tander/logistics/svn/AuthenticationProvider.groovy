package com.tander.logistics.svn

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
class AuthenticationProvider implements ISVNAuthenticationProvider {

    @Override
    SVNAuthentication requestClientAuthentication(String s, SVNURL svnurl, String s1, SVNErrorMessage svnErrorMessage, SVNAuthentication svnAuthentication, boolean b) {
        Boolean isCanceled
        String scmUser
        String scmPass

        scmUser = svnAuthentication.getUserName()
        (scmPass, isCanceled) = UiUtils.promptPassword(
                "Please enter password to access $s1",
                "Please enter password to access $s1 \n for user $scmUser:")

        if (isCanceled && svnErrorMessage) {
            throw new InvalidUserDataException(" ${svnErrorMessage.getErrorCode().toString()} \n" +
                    "${svnErrorMessage.toString()} ")
        }

        SVNAuthentication svnAuthenticationNew = new SVNPasswordAuthentication(scmUser, scmPass, true)
        return svnAuthenticationNew
    }

    @Override
    int acceptServerAuthentication(SVNURL svnurl, String s, Object o, boolean b) {
        return ACCEPTED
    }
}