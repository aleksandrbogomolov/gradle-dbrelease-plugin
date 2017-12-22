package com.tander.logistics.util

import com.tander.logistics.core.ScmFile
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project

import java.util.regex.Pattern

/**
 * Created by durov_an on 20.02.2016.
 *
 * Вспомогательные методы
 */
class FileUtils {

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

    static Comparator<ScmFile> schemaFileComparator = new Comparator<ScmFile>() {
        @Override
        int compare(ScmFile o1, ScmFile o2) {
            if (o1.wildcardId > o2.wildcardId) {
                return 1
            }
            if (o1.wildcardId < o2.wildcardId) {
                return -1
            }
            if (o1.name > o2.name) {
                return 1
            }
            if (o1.name < o2.name) {
                return -1
            }
            return 0
        }
    }
}
