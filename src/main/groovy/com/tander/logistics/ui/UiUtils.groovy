package com.tander.logistics.ui

import groovy.swing.SwingBuilder

/**
 * Created by durov_an on 07.02.2017.
 *
 * "Please enter password for user $user:"
 */
class UiUtils {
    static String promptPassword(String windowTitle, String editLabel) {
        String result = ''
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
                            result = input.password.toString(); // Set pass variable to value of input field
                            dispose(); // Close dialog
                        })
                    }
                }
            }
        } else {
            result = System.console().readPassword("\n $editLabel").toString()
        }
        return result
    }
}
