package com.tander.logistics.core

/**
 * Created by durov_an on 17.01.2017.
 */
interface IScmBranch {

    String getUrlFromFolder(String path)

    String getFirstRevision()

    String getLastRevision()

    String getRevisionName()

    void export(String path)
}
