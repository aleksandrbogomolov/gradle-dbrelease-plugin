package com.tander.logistics.ui

import groovy.swing.SwingBuilder

/**
 * Created by durov_an on 07.02.2017.
 *
 * "Please enter password for user $user:"
 */
class UiUtils {
    static promptPassword(String windowTitle, String editLabel) {
        String password = ''
        Boolean isCanceled = true
        if (System.console() == null) {
            new SwingBuilder().edt {
                lookAndFeel 'nimbus'
                dialog(modal: true, // Otherwise the build will continue running before you closed the dialog
                        title: windowTitle, // Dialog title
                        alwaysOnTop: true, // pretty much what the name says
                        resizable: false, // Don't allow the user to resize the dialog
                        locationRelativeTo: null, // Place dialog in center of the screen
                        pack: true, // We need to pack the dialog (so it will take the size of it's children
                        show: true // Let's show it
                ) {
                    borderLayout()
                    vbox { // Put everything below each other
                        label(text: editLabel)
                        input = passwordField()
                        button(defaultButton: true, text: 'OK', actionPerformed: {
                            isCanceled = false
                            password = input.password.toString(); // Set pass variable to value of input field
                            dispose(); // Close dialog
                        })
                    }
                }
            }
        } else {
            isCanceled = false
            password = System.console().readPassword("\n $editLabel").toString()
        }
        return [password, isCanceled]
    }

    static promptAuth(String windowTitle, String labelCaption, String prevLogin) {
        String login = prevLogin
        String password = ''
        Boolean isCanceled = true
        if (System.console() == null) {
            new SwingBuilder().edt {
                lookAndFeel 'nimbus'
                dialog(modal: true, // Otherwise the build will continue running before you closed the dialog
                        title: windowTitle, // Dialog title
                        alwaysOnTop: true, // pretty much what the name says
                        resizable: false, // Don't allow the user to resize the dialog
                        locationRelativeTo: null, // Place dialog in center of the screen
                        pack: true, // We need to pack the dialog (so it will take the size of it's children
                        show: true // Let's show it
                ) {
                    borderLayout()
                    label(text: labelCaption)
                    vbox { // Put everything below each other
                        label(text: "Login: ")
                        inputLogin = textField()
                        inputLogin.text = login
                        label(text: "Password: ")
                        inputPassword = passwordField()
                        button(defaultButton: true, text: 'OK', actionPerformed: {
                            isCanceled = false
                            login = inputLogin.text.toString();
                            password = inputPassword.password.toString(); // Set pass variable to value of input field
                            dispose(); // Close dialog
                        })
//                        button(defaultButton: false, text: 'Cancel', actionPerformed: {
//                            dispose(); // Close dialog
//                        })
                    }
                }
            }
        } else {
            isCanceled = false
            login = System.console().readLine("\n Please enter domain login").toString()
            password = System.console().readLine("\n Please enter domain password").toString()
        }
        return [login, password, isCanceled]
    }
}
