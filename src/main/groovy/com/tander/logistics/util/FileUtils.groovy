package com.tander.logistics.util

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project

import java.util.regex.Pattern

/**
 * Created by durov_an on 20.02.2016.
 *
 * Вспомогательные методы
 */
class FileUtils {

    static void CopyFile(String filePath, String sourceDirPath, String targetDirPath) {
        def sourceFile = new File(sourceDirPath + filePath)
        if (sourceFile.exists()) {
            def targetDir = new File(FilenameUtils.getFullPath(FilenameUtils.normalize(targetDirPath + filePath)))
            targetDir.mkdirs()
            new File(targetDirPath + filePath) << sourceFile.bytes
        }
    }

    /**
     * Проверим подходит ли поданая строка под маску
     * @param str строка для проверки
     * @param pattern маска для проверки
     * @return {@code true} если подходит, иначе {@code false}
     */
    static boolean checkByPattern(String str, Pattern pattern) {
        return pattern.matcher(str).matches()
    }

    static String getProjectName(Project project) {
        project.getRootDir().getName()
    }
}
