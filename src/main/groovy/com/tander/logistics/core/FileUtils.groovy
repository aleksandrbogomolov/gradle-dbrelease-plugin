package com.tander.logistics.core

import org.apache.commons.io.FilenameUtils

/**
 * Created by durov_an on 20.02.2016.
 *
 * Утилиты для сборки
 */
class FileUtils {

    static void CopyFile(String filePath, String sourceDirPath, String targetDirPath) {
        def sourceFile = new File(sourceDirPath + filePath)
        if (sourceFile.exists()) {
//            println FilenameUtils.getFullPath(FilenameUtils.normalize(targetDirPath + filePath))
            def targetDir = new File(FilenameUtils.getFullPath(FilenameUtils.normalize(targetDirPath + filePath)))
            targetDir.mkdirs()
            new File(targetDirPath + filePath) << sourceFile.bytes
        }

    }


}
