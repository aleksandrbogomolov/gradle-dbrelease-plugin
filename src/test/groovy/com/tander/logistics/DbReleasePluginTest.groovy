package com.tander.logistics

import org.gradle.api.Project
import org.gradle.internal.impldep.org.junit.Test
import org.gradle.testfixtures.ProjectBuilder

/**
 * Created by durov_an on 02.02.2017.
 */
class DbReleasePluginTest extends GroovyTestCase {
    Project project
    DbReleasePlugin plugin
    void setUp() {
        super.setUp()
        project = ProjectBuilder.builder().build()
        plugin = new DbReleasePlugin()
        plugin.apply(project)
    }

    void tearDown() {

    }

    void testApply() {
        DbReleaseExtension ext = project.extensions.findByType(DbReleaseExtension)
        ext.isTest = true
        "execution of buildDbRelease task is success"()
    }
    @Test
    def "execution of buildDbRelease task is success"() {
        when:
        project

        then:
        project.getTasksByName('buildDbRelease', true).size() == 1

    }
}
