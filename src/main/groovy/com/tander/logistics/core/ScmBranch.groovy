package com.tander.logistics.core

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Created by durov_an on 18.01.2017.
 */
abstract class ScmBranch implements IScmBranch {
    protected Logger logger

    String url
    String version
//    String revisionName
    ScmBranch() {
        logger = Logging.getLogger(this.class)
    }

    String[] getPathSegmentsFromUrl() {
        String[] segments = (new URL(url)).getPath().toString().split("/")
        return segments
    }

    String getLastPathSegmentFromUrl() {
        String[] segments = getPathSegmentsFromUrl()
        return segments[segments.length - 1]
    }


}
