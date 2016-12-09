package com.tander.logistics.tasks

import com.tander.logistics.WhsDBFile

import com.tander.logistics.WhsDBTemplate
import com.tander.logistics.DBScriptExtension
import com.tander.logistics.utils.SVNUtils
import com.tander.logistics.utils.WhsUtils
import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
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
class WhsBuildDBReleaseTask extends DefaultTask {

    def String username
    def char[] password
    def String currentInstallSVNURL
    def String currentUninstallSVNURL
    def String previousInstallSVNURL
    def String releaseNumber
    def String previousReleaseNumber
    def SVNRevision currentInstallRevision
    def SVNRevision previousInstallRevision
    def SVNRevision currentUninstallRevision
    def SVNRevision previousUninstallRevisiologgern
    def boolean showSvnExportLog
    def boolean showSvnDiffLog
    def boolean showFileCopyLog
    def boolean showLastCommiter
    def String installTemplatePath
    def installSectionWildacards = []
    def installFiles = [:] // список файлов для установки
    def uninstallFiles = [:] // список файлов для отката
    def scriptSections = [:]
    def String spprTask

    def SVNUtils svnUtils

    def String RELEASE_PATH = '/release/'

    WhsBuildDBReleaseTask() {
        description = 'Generate install and uninstall SQL scripts'
//        initObjectValues(project.whsrelease)
//        svnUtils = new SVNUtils(username, password)
    }

    def private exportDir(String svnURL, String exportDirPath, SVNRevision revision) {

        ISVNEventHandler dispatcher = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent svnEvent, double v) throws SVNException {
                logger.info("exporting file " + svnEvent.getFile().toString())
            }

            @Override
            void checkCancelled() throws SVNCancelException {
            }
        }
//        updateClient.doExport(SVNURL.parseURIEncoded(svnURL), exportDir, revision, revision, null, true, SVNDepth.INFINITY);

        if (SVNWCUtil.isVersionedDirectory(new File(exportDirPath))) {
            if (svnUtils.getWorkingDirectoryURL(exportDirPath) == svnURL) {
                logger.lifecycle("update folder $exportDirPath")
                svnUtils.doUpdate(exportDirPath, revision, dispatcher)
            } else {
                throw new Exception("Необходимо очистить каталог build")
            }
        } else {
            File exportDir = new File(exportDirPath)
            exportDir.deleteDir();
            logger.lifecycle("checkout URL $svnURL to folder $exportDirPath")
            svnUtils.doCheckout(svnURL, exportDirPath, revision, dispatcher)
        }
    }

    @TaskAction
    def start() {
        initReleaseDirs()
        initObjectValues(project.whsrelease)
        buildRelease()
    }

    private void initReleaseDirs() {
//        File exportDirs = new File(project.buildDir.getPath() + '/export')
//        exportDirs.deleteDir();
        File releaseDir = new File(project.buildDir.getPath() + '/release')
        releaseDir.deleteDir();
    }


    def private initObjectValues(DBScriptExtension whsReleaseExtension) {
        // инициализация для работы с SVN

        username = whsReleaseExtension.svnUsername
        password = whsReleaseExtension.svnPassword as char[]

        svnUtils = new SVNUtils(username, password)

        currentInstallSVNURL = whsReleaseExtension.currentInstallSVNURL
        currentUninstallSVNURL = whsReleaseExtension.currentUninstallSVNURL
        previousInstallSVNURL = whsReleaseExtension.previousInstallSVNURL ?: currentInstallSVNURL
        releaseNumber = whsReleaseExtension.releaseNumber
        if (!releaseNumber) {
            throw new Exception("Не указан параметр releaseNumber")
        }
        previousReleaseNumber = whsReleaseExtension.previousReleaseNumber
        currentInstallRevision = (whsReleaseExtension.currentInstallRevision <= 0) ?
                SVNRevision.HEAD : SVNRevision.create(whsReleaseExtension.currentInstallRevision)

        if (!whsReleaseExtension.previousInstallSVNURL) {
            previousInstallRevision = (whsReleaseExtension.previousInstallRevision <= 0) ?
                    getFirstRevision(currentInstallSVNURL) : SVNRevision.create(whsReleaseExtension.previousInstallRevision)
        } else {
            previousInstallRevision = (whsReleaseExtension.previousInstallRevision <= 0) ?
                    SVNRevision.HEAD : SVNRevision.create(whsReleaseExtension.previousInstallRevision)
        }
        showSvnExportLog = whsReleaseExtension.showSvnExportLog
        showSvnDiffLog = whsReleaseExtension.showSvnDiffLog
        showFileCopyLog = whsReleaseExtension.showFileCopyLog
        showLastCommiter = whsReleaseExtension.showLastCommiter
        installTemplatePath = whsReleaseExtension.installTemplatePath
        installSectionWildacards = whsReleaseExtension.installSectionWildacards
        spprTask = whsReleaseExtension.spprTask

    }

    def buildRelease() {
        getChangedFiles()
        getLastCommitInfo()
        exportFiles()

        createSQLScript("install", installFiles)
        if (currentUninstallSVNURL) {
            createSQLScript("uninstall", uninstallFiles)
        }
    }

    def private createSQLScript(String scriptType, Map installFiles) {
// заполним скрипты по секциям для вставки в ${scriptType}.sql и скопируем файлы
        def boolean isInstall = scriptType == "install"

        installSectionWildacards.each {
            scriptSections[it.key] = ''
        }

        installFiles.each { String fileName, WhsDBFile whsDBFile ->
            if (isInstall) {
                WhsUtils.CopyFile(whsDBFile.name, project.buildDir.path + '/export/current/install/', project.buildDir.path + '/release/install/install/')
                WhsUtils.CopyFile(whsDBFile.name, project.buildDir.path + '/export/previous/install/', project.buildDir.path + '/release/uninstall/uninstall/')
            } else {
                WhsUtils.CopyFile(whsDBFile.name, project.buildDir.path + '/export/current/uninstall/', project.buildDir.path + '/release/uninstall/uninstall/')
            }
//            код для поиска номера задачи в СППР, сейчас не используется т.к. svn list не выводит текст последнего коммита
//            def m = whsDBFile.lastMessage =~ /(#SP\d+)/
//            def task = m.group(0)
            if (showLastCommiter && isInstall) {
                scriptSections[whsDBFile.installSection] += "\n-- Revision: $whsDBFile.lastRevision Date: $whsDBFile.lastDateFormatted Author: $whsDBFile.lastAuthor \n"
            }
            scriptSections[whsDBFile.installSection] += "prompt [!] File: @$scriptType/$whsDBFile.name \n"
            if (showLastCommiter && isInstall) {
                scriptSections[whsDBFile.installSection] += "prompt [!] Revision: $whsDBFile.lastRevision Date: $whsDBFile.lastDateFormatted Author: $whsDBFile.lastAuthor \n"
            }
            scriptSections[whsDBFile.installSection] += "@$scriptType/$whsDBFile.name \n"
        }

        // создадим итоговый скрипт с помощью template движка
        def previousReleaseNumberShort = previousReleaseNumber ?
                previousReleaseNumber[0..[29, previousReleaseNumber.length()].min()] :
                ""
        def releaseNumberShort = releaseNumber ?
                releaseNumber[0..[29, releaseNumber.length()].min()] :
                ""
        def binding = ["information_created"   : "",
                       "information_statistics": "",
                       "log_version"           : "${scriptType}_log_" + releaseNumber.replace('.', '_') + ".lst",
                       "desc_name"             : "",
                       "desc_version"          : "",
                       "current_version"       : isInstall ? previousReleaseNumberShort : releaseNumberShort,
                       "new_version"           : isInstall ? releaseNumberShort : previousReleaseNumberShort]
        scriptSections.each {
            binding[it.key] = it.value
        }

        WhsDBTemplate installTemplate = new WhsDBTemplate(installTemplatePath)
        installTemplate.makeScript(project.buildDir.path + RELEASE_PATH + "${scriptType}/${scriptType}.sql", binding)

        WhsUtils.CreateTarBZ(project.buildDir.getAbsolutePath() + RELEASE_PATH + "${scriptType}/",
                project.buildDir.getAbsolutePath() + RELEASE_PATH + "${releaseNumber}-${scriptType}.tbz")


        Tar tar = project.tasks.create("${releaseNumber}-${scriptType}-tartask", Tar)
        tar.configure {
            compression = Compression.BZIP2
            extension = 'tbz'
            baseName = "${releaseNumber}-${scriptType}-tartask"
            destinationDir = new File(project.buildDir.path + '/distribution')
            from(project.buildDir.getAbsolutePath() + RELEASE_PATH + "${scriptType}/")
        }
        tar.execute()

    }

    private void exportFiles() {
// выгрузим все три директории
        logger.lifecycle('svn export started')
        exportDir(currentInstallSVNURL, project.buildDir.path + '/export/current/install', currentInstallRevision)
        if (currentUninstallSVNURL) {
            exportDir(currentUninstallSVNURL, project.buildDir.path + '/export/current/uninstall', SVNRevision.HEAD)
        }
        exportDir(previousInstallSVNURL, project.buildDir.path + '/export/previous/install', previousInstallRevision)
        logger.lifecycle('svn export done')
    }

    private void getChangedFiles() {
        runSvnDiff(
                previousInstallSVNURL,
                previousInstallRevision,
                currentInstallSVNURL,
                currentInstallRevision,
                false)
        if (currentUninstallSVNURL) {
            currentUninstallRevision = SVNRevision.HEAD
            runSvnDiff(
                    currentUninstallSVNURL,
                    getFirstRevision(currentUninstallSVNURL),
                    currentUninstallSVNURL,
                    currentUninstallRevision,
                    true)
        }
        // и отсортируем полученные списки
        installFiles = installFiles.entrySet().sort(false, whsDBFileComparatorWildcard).collectEntries()
        uninstallFiles = uninstallFiles.entrySet().sort(false, whsDBFileComparatorWildcard).collectEntries()
    }

    def
    private runSvnDiff(String prevURL, SVNRevision prevURLRevision, String curURL, SVNRevision curURLRevision, boolean isUninstallBranch) {
        // сделаем дифф между двумя ветками и ревизиями
        logger.lifecycle('svn diff started')

        logger.info("prevURL = $prevURL")
        logger.info("prevURLRevision = $prevURLRevision")
        logger.info("curURL = $curURL")
        logger.info("curURLRevision = $curURLRevision")
        // обработчик команды svn diff, заполняет массив файлов, которые нужно включить в сборку
        ISVNDiffStatusHandler diffStatusHandler = new ISVNDiffStatusHandler() {
            @Override
            void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
                if (svnDiffStatus.getKind() == SVNNodeKind.FILE) {
                    def int wildcardMatchCount = 0
                    def String wildcardsMatched = ''
                    def WhsDBFile dbFile = new WhsDBFile()
                    dbFile.name = svnDiffStatus.getPath()
                    installSectionWildacards.each { sectionName, wildcards ->
                        wildcards.eachWithIndex { wildcard, i ->
                            if (FilenameUtils.wildcardMatch(dbFile.name, wildcard as String)) {
                                dbFile.wildcardID = i as int
                                wildcardMatchCount += 1
                                wildcardsMatched += wildcard + ', '
                                dbFile.installSection = sectionName
                            }
                        }
                    }
                    if (svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                SVNStatusType.STATUS_DELETED,
                                                                SVNStatusType.STATUS_ADDED]) {
                        if (wildcardMatchCount > 1) {
                            throw new Exception(dbFile.name + " Файл входит в несколько масок: " + wildcardsMatched)
                        }
                        if (wildcardMatchCount == 0) {
                            throw new Exception(dbFile.name + " Файл не входит ни в одну из масок " + svnDiffStatus.getModificationType().toString())
                        }
                    }
                    if ((svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                 SVNStatusType.STATUS_ADDED])) {
                        if (isUninstallBranch) {
                            uninstallFiles[dbFile.name] = dbFile
                        } else {
                            installFiles[dbFile.name] = dbFile
                        }
                    }
                    if (svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                SVNStatusType.STATUS_DELETED]) {
                        uninstallFiles[dbFile.name] = dbFile
                    }
                    if (!(svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                  SVNStatusType.STATUS_DELETED,
                                                                  SVNStatusType.STATUS_ADDED])) {
                        logger.warn(dbFile.name + " Некорректный статус файла : " + svnDiffStatus.getModificationType().toString())
//                        throw new Exception(dbFile.name + " Неизвестный статус файла : " + svnDiffStatus.getModificationType().toString())
                    }
                }
                logger.info(svnDiffStatus.getModificationType().toString() + ' ' + svnDiffStatus.getFile().toString())
            }
        }
        svnUtils.doDiffStatus(prevURL, prevURLRevision, curURL, curURLRevision, diffStatusHandler)
        logger.lifecycle('svn diff done')
    }

    def private getLastCommitInfo() {
        // заполним информацию о последнем коммите каждого файла, если требуется
        if (showLastCommiter) {
            logger.lifecycle('svn list started')
            ISVNDirEntryHandler isvnDirEntryHandler = new ISVNDirEntryHandler() {
                @Override
                void handleDirEntry(SVNDirEntry svnDirEntry) throws SVNException {
                    if (installFiles.containsKey(svnDirEntry.getRelativePath())) {
                        WhsDBFile whsDBFile = installFiles[svnDirEntry.getRelativePath()]
                        whsDBFile.lastAuthor = svnDirEntry.getAuthor()
                        whsDBFile.lastRevision = svnDirEntry.getRevision()
//                    (installFiles[svnDirEntry.getRelativePath()] as WhsDBFile).lastMessage = svnDirEntry.getCommitMessage()
                        whsDBFile.lastDate = svnDirEntry.getDate()
                        whsDBFile.lastDateFormatted = svnDirEntry.getDate().format('dd.MM.yyyy ss:mm:HH')
                    }
                }
            }

            svnUtils.doList(currentInstallSVNURL, isvnDirEntryHandler)
            logger.lifecycle('svn list done')
        }
    }

    def SVNRevision getFirstRevision(String svnURL) {
        // получение первой ревизии в ветке
        def long firstRevisionNumber = 0
        def SVNRevision firstRevision = SVNRevision.HEAD

        ISVNLogEntryHandler isvnLogEntryHandler = new ISVNLogEntryHandler() {
            @Override
            void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
                logger.info("определена начальная ревизия в ветке $svnURL:" + logEntry.getRevision() + ' ' + logEntry.getMessage())
                firstRevisionNumber = logEntry.getRevision()
            }
        }

        svnUtils.doLog(svnURL, 0, 1, isvnLogEntryHandler)
        if (firstRevision != 0) {
            firstRevision = SVNRevision.create(firstRevisionNumber)
        }
        return firstRevision
    }

    // компаратор для сортировки списка файлов. Сперва сортируем по маске файла из настроек, потом по пути к файлу
    Comparator<Map.Entry<String, WhsDBFile>> whsDBFileComparatorWildcard = new Comparator<Map.Entry<String, WhsDBFile>>() {
        @Override
        int compare(Map.Entry<String, WhsDBFile> o1, Map.Entry<String, WhsDBFile> o2) {
            if (o1.value.wildcardID > o2.value.wildcardID) {
                return 1
            }
            if (o1.value.wildcardID < o2.value.wildcardID) {
                return -1
            }
            if (o1.value.name > o2.value.name) {
                return 1
            }
            if (o1.value.name < o2.value.name) {
                return -1
            }
            return 0
        }
    }
}
