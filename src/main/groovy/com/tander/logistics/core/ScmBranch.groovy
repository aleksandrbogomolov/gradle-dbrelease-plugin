package com.tander.logistics.core

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Created by durov_an on 18.01.2017.
 */
abstract class ScmBranch {
    protected Logger logger

    String url
    String version
//    String revisionName
    ScmBranch() {
        logger = Logging.getLogger(this.class)
    }

    def getPathSegmentsFromUrl() {
        def segments = (new URL(url)).getPath().toString().split("/")
        return segments
    }

    def getLastPathSegmentFromUrl() {
        def segments = getPathSegmentsFromUrl()
        return segments[segments.length-1]
    }

}
