package com.tander.logistics.utils

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.wc.ISVNEventHandler
import org.tmatesoft.svn.core.wc.SVNEvent
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNUpdateClient
import org.tmatesoft.svn.core.wc.SVNWCUtil
import org.gradle.api.internal.*

/**
 * Created by durov_an on 20.02.2016.
 *
 * Утилиты для сборки
 */
public class WhsUtils {
    public static void CreateTarBZ(String dirPath, String tarBzPath) throws FileNotFoundException, IOException {

        dirPath = FilenameUtils.normalize(dirPath, true)
        tarBzPath = FilenameUtils.normalize(tarBzPath, true)
        FileOutputStream fOut
        BufferedOutputStream bOut
        BZip2CompressorOutputStream bzOut
        TarArchiveOutputStream tOut
        try {
            fOut = new FileOutputStream(new File(tarBzPath))
            bOut = new BufferedOutputStream(fOut)
            bzOut = new BZip2CompressorOutputStream(bOut)
            tOut = new TarArchiveOutputStream(bzOut)
            addFileToTarBz(tOut, dirPath, "")
        } catch (e) {
            println e
        } finally {
            tOut.finish()
            tOut.close()
            bzOut.close()
            bOut.close()
            fOut.close()
        }
    }

    private static void addFileToTarBz(TarArchiveOutputStream tOut, String path, String base)
            throws IOException {
        File f = new File(path)
        String entryName = base + f.getName()
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName)
        tOut.putArchiveEntry(tarEntry)

        if (f.isFile()) {
//            TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName)
//            tOut.putArchiveEntry(tarEntry)
            FileInputStream fIn = new FileInputStream(f)
            IOUtils.copy(fIn, tOut)
            fIn.close()
            tOut.closeArchiveEntry()
        } else {
            tOut.closeArchiveEntry()
            File[] children = f.listFiles()
            if (children != null) {
                for (File child : children) {
                    addFileToTarBz(tOut, child.getAbsolutePath(), entryName + "/")
                }
            }
        }
//        tOut.closeArchiveEntry()
    }


    public static void CopyFile(String filePath, String sourceDirPath, String targetDirPath) {
        def sourceFile = new File(sourceDirPath + filePath)
        if (sourceFile.exists()) {
//            println FilenameUtils.getFullPath(FilenameUtils.normalize(targetDirPath + filePath))
            def targetDir = new File(FilenameUtils.getFullPath(FilenameUtils.normalize(targetDirPath + filePath)))
            targetDir.mkdirs()
            new File(targetDirPath + filePath) << sourceFile.bytes
        }

    }


}
