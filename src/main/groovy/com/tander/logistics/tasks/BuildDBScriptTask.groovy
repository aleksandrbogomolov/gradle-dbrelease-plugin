package com.tander.logistics.tasks

import com.tander.logistics.core.DBTemplate
import com.tander.logistics.DBReleaseExtension
import com.tander.logistics.core.SCMFile
import com.tander.logistics.svn.DBReleaseSVN
import com.tander.logistics.svn.SVNUtils
import com.tander.logistics.core.FileUtils
import com.tander.logistics.core.ScriptType
import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.tmatesoft.svn.core.ISVNDirEntryHandler
import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNDirEntry
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.SVNNodeKind
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler
import org.tmatesoft.svn.core.wc.ISVNEventHandler
import org.tmatesoft.svn.core.wc.SVNDiffStatus
import org.tmatesoft.svn.core.wc.SVNEvent
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNStatusType
import org.tmatesoft.svn.core.wc.SVNWCUtil

/**
 * Created by durov_an on 17.02.2016.
 *
 * Таск для сборки БД релиза.
 */


class BuildDBScriptTask extends DefaultTask {

    File dbTemplate

    SVNRevision currentInstallRevision
    SVNRevision previousInstallRevision
    SVNRevision currentUninstallRevision
    SVNRevision previousUninstallRevision
    LinkedHashMap installFiles  // список файлов для установки
    LinkedHashMap uninstallFiles  // список файлов для отката
    LinkedHashMap scriptSections

    SVNUtils svnUtils
    DBReleaseExtension ext

    String RELEASE_PATH = '/release/'

    BuildDBScriptTask() {
        description = 'Generate install and uninstall SQL scripts'
//        initObjectValues(project.whsrelease)
//        svnUtils = new SVNUtils(user, password)
        this.ext = project.dbrelease
    }

    @TaskAction
    void run() {
        File releaseDir = new File(project.buildDir.getPath() + '/script')
        releaseDir.deleteDir();


        DBReleaseSVN dbRelease = new DBReleaseSVN(project)

        dbRelease.setChangedFilesByDiff()
        dbRelease.exportChangedFilesToDir()
        dbRelease.setLastCommitInfo()
        dbRelease.assemblyScript(dbTemplate)

//        initObjectValues()
//        buildRelease()
    }


    private checkoutSVNBranch(String svnURL, String exportDirPath, SVNRevision revision) {

        ISVNEventHandler dispatcher = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent svnEvent, double v) throws SVNException {
                logger.lifecycle("exporting file " + svnEvent.getFile().toString())
            }

            @Override
            void checkCancelled() throws SVNCancelException {
            }
        }
//        updateClient.doExport(SVNURL.parseURIEncoded(svnURL), checkoutSVNBranch, revision, revision, null, true, SVNDepth.INFINITY);

        if (SVNWCUtil.isVersionedDirectory(new File(exportDirPath))) {
            if (svnUtils.getWorkingDirectoryURL(exportDirPath) == svnURL) {
                logger.lifecycle("update folder $exportDirPath")
                svnUtils.doUpdate(exportDirPath, revision, dispatcher)
            } else {
                throw new Exception("Need to clean build dir")
            }
        } else {
            File exportDir = new File(exportDirPath)
            exportDir.deleteDir();
            logger.lifecycle("checkout URL $svnURL to folder $exportDirPath")
            svnUtils.doCheckout(svnURL, exportDirPath, revision, dispatcher)
        }
    }

    private initObjectValues() {
        // инициализация для работы с SVN
        svnUtils = new SVNUtils(ext.user, ext.password.toCharArray())
        currentInstallRevision = (ext.currentInstallRevision <= 0) ?
                SVNRevision.HEAD : SVNRevision.create(ext.currentInstallRevision)

        if (!ext.previousInstallSVNURL) {
            previousInstallRevision = (ext.previousInstallRevision <= 0) ?
                    getFirstRevision(ext.currentInstallSVNURL) : SVNRevision.create(ext.previousInstallRevision)
        } else {
            previousInstallRevision = (ext.previousInstallRevision <= 0) ?
                    SVNRevision.HEAD : SVNRevision.create(ext.previousInstallRevision)
        }
    }

    def buildRelease() {
//        getChangedFiles()
//        if (ext.showLastCommiter) {
//            setLastSVNCommitInfo()
//        }
//        exportFiles()

        assemblyOracleScript(ScriptType.stInstall, installFiles)
        if (ext.currentUninstallSVNURL) {
            assemblyOracleScript(ScriptType.stUninstall, uninstallFiles)
        }
    }

    private assemblyOracleScript(ScriptType scriptType, Map scmFiles) {
        // заполним скрипты по секциям для вставки в ${scriptType}.sql и скопируем файлы
        boolean isInstall = scriptType == ScriptType.stInstall

        ext.wildacards.each {
            scriptSections[it.key] = ''
        }

        scmFiles.each { String fileName, SCMFile scmFile ->
            if (isInstall) {
                // если формируем скрипт наката, то
                FileUtils.CopyFile(scmFile.name,
                        project.buildDir.path + '/export/current/install/',
                        project.buildDir.path + '/release/install/install/')
                FileUtils.CopyFile(scmFile.name,
                        project.buildDir.path + '/export/previous/install/',
                        project.buildDir.path + '/release/uninstall/uninstall/')
            } else {
                FileUtils.CopyFile(scmFile.name,
                        project.buildDir.path + '/export/current/uninstall/',
                        project.buildDir.path + '/release/uninstall/uninstall/')
            }
//            код для поиска номера задачи в СППР, сейчас не используется т.к. svn list не выводит текст последнего коммита
//            def m = sCMFile.lastMessage =~ /(#SP\d+)/
//            def task = m.group(0)
            if (ext.showLastCommiter && isInstall) {
                scriptSections[scmFile.scriptSection] += "\n-- Revision: $scmFile.lastRevision " +
                        "Date: $scmFile.lastDateFormatted " +
                        "Author: $scmFile.lastAuthor \n"
            }
            scriptSections[scmFile.scriptSection] += "prompt [!] File: @$scriptType.dirName/$scmFile.name \n"
            if (ext.showLastCommiter && isInstall) {
                scriptSections[scmFile.scriptSection] += "prompt [!] Revision: $scmFile.lastRevision " +
                        "Date: $scmFile.lastDateFormatted " +
                        "Author: $scmFile.lastAuthor \n"
            }
            scriptSections[scmFile.scriptSection] += "@$scriptType.dirName/$scmFile.name \n"
        }

        // создадим итоговый скрипт с помощью template движка
        def previousReleaseNumberShort = ext.previousReleaseNumber ?
                ext.previousReleaseNumber[0..[29, ext.previousReleaseNumber.length()].min()] :
                ""
        def releaseNumberShort = ext.releaseNumber ?
                ext.releaseNumber[0..[29, ext.releaseNumber.length()].min()] :
                ""
        def binding = ["information_created"   : "",
                       "information_statistics": "",
                       "log_version"           : "${scriptType}_log_" + ext.releaseNumber.replace('.', '_') + ".lst",
                       "desc_name"             : "",
                       "desc_version"          : "",
                       "current_version"       : isInstall ? previousReleaseNumberShort : releaseNumberShort,
                       "new_version"           : isInstall ? releaseNumberShort : previousReleaseNumberShort]
        scriptSections.each {
            binding[it.key] = it.value
        }

        DBTemplate installTemplate = new DBTemplate(dbTemplate)
        installTemplate.makeScript(project.buildDir.path + RELEASE_PATH + "${scriptType.dirName}/${scriptType.dirName}.sql", binding)

//        FileUtils.CreateTarBZ(project.buildDir.getAbsolutePath() + RELEASE_PATH + "${scriptType}/",
//                project.buildDir.getAbsolutePath() + RELEASE_PATH + "${releaseNumber}-${scriptType}.tbz")

//        Tar tar = project.tasks.create("${ext.releaseNumber}-${scriptType}-tartask", Tar)
//        tar.configure {
//            compression = Compression.BZIP2
//            extension = 'tbz'
//            baseName = "${releaseNumber}-${scriptType}-tartask"
//            destinationDir = new File(project.buildDir.path + '/distribution')
//            from(project.buildDir.getAbsolutePath() + RELEASE_PATH + "${scriptType}/")
//        }
//        tar.execute()

    }

    private void exportFiles() {
// выгрузим все три директории
        logger.lifecycle('svn export started')
        checkoutSVNBranch(ext.currentInstallSVNURL, project.buildDir.path + '/export/current/install', currentInstallRevision)
        if (ext.currentUninstallSVNURL) {
            checkoutSVNBranch(ext.currentUninstallSVNURL, project.buildDir.path + '/export/current/uninstall', SVNRevision.HEAD)
        }
        checkoutSVNBranch(ext.previousInstallSVNURL, project.buildDir.path + '/export/previous/install', previousInstallRevision)
        logger.lifecycle('svn export done')
    }

    private void getChangedFiles() {
        runSvnDiff(
                ext.previousInstallSVNURL,
                previousInstallRevision,
                ext.currentInstallSVNURL,
                currentInstallRevision,
                false)
        if (ext.currentUninstallSVNURL) {
            currentUninstallRevision = SVNRevision.HEAD
            runSvnDiff(
                    ext.currentUninstallSVNURL,
                    getFirstRevision(ext.currentUninstallSVNURL),
                    ext.currentUninstallSVNURL,
                    currentUninstallRevision,
                    true)
        }
        // и отсортируем полученные списки
        installFiles = installFiles.entrySet().sort(false, whsDBFileComparatorWildcard).collectEntries()
        uninstallFiles = uninstallFiles.entrySet().sort(false, whsDBFileComparatorWildcard).collectEntries()
    }

    private runSvnDiff(String prevURL, SVNRevision prevURLRevision, String curURL, SVNRevision curURLRevision, boolean isUninstallBranch) {
        // сделаем дифф между двумя ветками и ревизиями
        logger.lifecycle('svn diff started')

        logger.info("prevURL = $prevURL")
        logger.info("prevURLRevision = $prevURLRevision")
        logger.info("currURL = $curURL")
        logger.info("curURLRevision = $curURLRevision")
        // обработчик команды svn diff, заполняет массив файлов, которые нужно включить в сборку
        ISVNDiffStatusHandler diffStatusHandler = new ISVNDiffStatusHandler() {
            int wildcardMatchCount

            @Override
            void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
                if (svnDiffStatus.getKind() == SVNNodeKind.FILE) {
                    wildcardMatchCount = 0
                    String wildcardsMatched = ''
                    SCMFile scmFile = new SCMFile(svnDiffStatus.getPath())
                    ext.wildacards.each { sectionName, wildcards ->
                        wildcards.eachWithIndex { wildcard, i ->
                            if (FilenameUtils.wildcardMatch(scmFile.name, wildcard as String)) {
                                scmFile.wildcardID = i as int
                                wildcardMatchCount += 1
                                wildcardsMatched += wildcard + ', '
                                scmFile.scriptSection = sectionName
                            }
                        }
                    }
                    if (svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                SVNStatusType.STATUS_DELETED,
                                                                SVNStatusType.STATUS_ADDED]) {
                        if (wildcardMatchCount > 1) {
                            throw new Exception(scmFile.name + " Multiply wildcards matched: " + wildcardsMatched)
                        }
                        if (wildcardMatchCount == 0) {
                            throw new Exception(scmFile.name + " File not matched by any wildcard: " + svnDiffStatus.getModificationType().toString())
                        }
                    } else {
                        logger.warn(scmFile.name + " Uncorrect file status : " + svnDiffStatus.getModificationType().toString())
//                        throw new Exception(scmFile.name + " Неизвестный статус файла : " + svnDiffStatus.getModificationType().toString())
                    }

                    if ((svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                 SVNStatusType.STATUS_ADDED])) {
                        if (isUninstallBranch) {
                            uninstallFiles[scmFile.name] = scmFile
                        } else {
                            installFiles[scmFile.name] = scmFile
                        }
                    }
                    if (svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                SVNStatusType.STATUS_DELETED]) {
                        uninstallFiles[scmFile.name] = scmFile
                    }
                }
                logger.info(svnDiffStatus.getModificationType().toString() + ' ' + svnDiffStatus.getFile().toString())
            }
        }
        svnUtils.doDiffStatus(prevURL, prevURLRevision, curURL, curURLRevision, diffStatusHandler)
        logger.lifecycle('svn diff done')
    }

    private void setLastSVNCommitInfo() {
        // заполним информацию о последнем коммите каждого файла
        logger.lifecycle('svn list started')
        ISVNDirEntryHandler isvnDirEntryHandler = new ISVNDirEntryHandler() {
            @Override
            void handleDirEntry(SVNDirEntry svnDirEntry) throws SVNException {
                if (installFiles.containsKey(svnDirEntry.getRelativePath())) {
                    SCMFile scmFile = installFiles[svnDirEntry.getRelativePath()]
                    scmFile.lastAuthor = svnDirEntry.getAuthor()
                    scmFile.lastRevision = svnDirEntry.getRevision()
//                    (installFiles[svnDirEntry.getRelativePath()] as SCMFile).lastMessage = svnDirEntry.getCommitMessage()
                    scmFile.lastDate = svnDirEntry.getDate()
                    scmFile.lastDateFormatted = svnDirEntry.getDate().format('dd.MM.yyyy ss:mm:HH')
                }
            }
        }

        svnUtils.doList(ext.currentInstallSVNURL, isvnDirEntryHandler)
        logger.lifecycle('svn list done')
    }


    SVNRevision getFirstRevision(String svnURL) {
        // получение первой ревизии в ветке
        long firstRevisionNumber = 0
        SVNRevision firstRevision = SVNRevision.HEAD

        ISVNLogEntryHandler isvnLogEntryHandler = new ISVNLogEntryHandler() {
            @Override
            void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
                logger.info("определена начальная ревизия в ветке $svnURL:" + logEntry.getRevision() + ' ' + logEntry.getMessage())
                firstRevisionNumber = logEntry.getRevision()
            }
        }

        svnUtils.doLog(svnURL, SVNRevision.create(0), 1, isvnLogEntryHandler)
        if (firstRevision != 0) {
            firstRevision = SVNRevision.create(firstRevisionNumber)
        }
        return firstRevision
    }


}
