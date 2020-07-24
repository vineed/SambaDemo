package com.vin.sambademo

import android.util.Log
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.protocol.commons.EnumWithValue
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashSet

class Utility private constructor() {
    companion object {

        fun loadSambaWithSMBJ(url:String, filesDir: JFile, modifiedOn: Long) {
            val client = SMBClient()

            client.connect(url).use { connection ->
                val ac = //AuthenticationContext.anonymous()
                AuthenticationContext("Vineed", "macrocks".toCharArray(), "")
                val session: Session = connection.authenticate(ac)
                val SHARE_NAME = "vin"

                // Connect to Share
                val diskShare = session.connectShare(SHARE_NAME) as DiskShare?

                val smbShareSet: MutableSet<SMB2ShareAccess> = HashSet()
                smbShareSet.addAll(SMB2ShareAccess.ALL)//.iterator().next()) // this is to get READ only

                val localPath = JFile(filesDir, "temp")
                localPath.mkdir()

                diskShare?.let {
                    addFileRecursively(localPath, "", it, modifiedOn)
                }
            }
        }

        fun addFileRecursively(
            localRoot: JFile,
            dir: String,
            diskShare: DiskShare,
            modifiedOn: Long
        ) {
            for (fileIdBothDirectoryInformation in diskShare.list(dir)) {
                val fileName = fileIdBothDirectoryInformation.fileName
                println("File : $fileName")

                if (fileName.startsWith(".")) continue

                if (EnumWithValue.EnumUtils.isSet(
                        fileIdBothDirectoryInformation.fileAttributes,
                        FileAttributes.FILE_ATTRIBUTE_DIRECTORY
                    )
                ) {
                    val fileDir = JFile(localRoot, fileName)

                    if (fileDir.exists()) {
                        fileDir.setLastModified(modifiedOn)
                    } else {
                        fileDir.mkdir()
                    }

                    addFileRecursively(
                        fileDir,
                        if (dir.isBlank()) fileName else "$dir\\$fileName",
                        diskShare, modifiedOn
                    )
                    continue
                }

                val remoteSmbjFile: File = diskShare.openFile(
                    if (dir.isBlank()) fileName else "$dir\\$fileName",
                    EnumSet.of(AccessMask.GENERIC_ALL),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    null
                )

                val bufReader = remoteSmbjFile.inputStream.buffered()

                val file = JFile(
                    localRoot.apply { mkdirs() },
                    fileName
                )

                val fileLastModTime = file.lastModified()

                if (!file.exists() || fileLastModTime < fileIdBothDirectoryInformation.changeTime.toEpochMillis()) {
                    val bufWriter = FileOutputStream(file).buffered()

                    bufReader.use { reader ->
                        bufWriter.use { writer ->
                            reader.copyTo(writer)
                        }
                    }
                } else {
                    Log.d("Touched", "Touched $file")
                    file.setLastModified(modifiedOn)
                }
            }
        }

        fun deleteNotTouchedFiles(filesDir: JFile, modifiedOn: Long) {
            val localPath = JFile(filesDir, "temp")

            localPath.listFiles()?.forEach { file ->
                file.listFiles()?.filter { it.lastModified() < modifiedOn }
                    ?.forEach {
                        it.delete()
                    }
            }

            localPath.listFiles()?.forEach { file ->
                if (file.listFiles()?.isEmpty() == true) file.delete()
            }
        }
    }
}