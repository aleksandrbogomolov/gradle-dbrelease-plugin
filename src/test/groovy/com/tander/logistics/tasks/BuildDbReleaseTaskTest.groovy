//package com.tander.logistics.tasks
//
//import com.tander.logistics.DbReleaseExtension
//import com.tander.logistics.DbReleasePlugin
//import com.tander.logistics.categories.Perfomance
//import org.gradle.api.Project
//import org.gradle.internal.impldep.org.junit.Test
//import org.gradle.testfixtures.ProjectBuilder
//import groovyx.gprof.Profiler
//import org.junit.Test
//
///**
// * Created by durov_an on 02.02.2017.
// */
//class BuildDbReleaseTaskTest extends GroovyTestCase {
//    Project project
//    DbReleasePlugin plugin
//    DbReleaseBuildTask task
//    Profiler profiler
//
//
//    void setUp() {
//        super.setUp()
//        project = ProjectBuilder.builder()
//                .withProjectDir(new File("e:\\projects\\repos\\dc_ora\\Oracle\\branches\\durov_an-SP0055893-mercury-1_117_10\\"))
//                .build()
//        plugin = new DbReleasePlugin()
//        plugin.apply(project)
//        task = project.tasks.findByName("buildDbRelease") as DbReleaseBuildTask
//        DbReleaseExtension ext = project.extensions.findByType(DbReleaseExtension)
////        ext.user = System.getProperty('domainUser')
////        ext.password = System.getProperty('domainPassword')
//        ext.user = 'durov_an'
//        ext.password = ''
//        ext.currentRevision = "166358"
//
//        profiler = new Profiler()
//    }
//
//    void testRun() {
////        profiler.start()
////        task.run()
////        profiler.stop()
////
////        profiler.report.prettyPrint()
//    }
//
//    @Category(Perfomance)
//    @Test
//    void testProfileBuildDbRelease() {
//        setUp()
//        profiler.start()
//        task.run()
//        profiler.stop()
//        assertEquals("Gradle is gr8", "Gradle is gr8")
//
//        profiler.report.prettyPrint()
//    }
//
//    @Test public void sample() {
//        assertEquals("Gradle is gr8", "Gradle is gr8")
//    }
//}

