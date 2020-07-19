package com.vin.sambademo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.protocol.commons.EnumWithValue
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet


typealias JFile = java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bFiles.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                loadSambaWithSMBJ()
            }
        }
    }

    private fun loadSambaWithSMBJ() {
        val client = SMBClient()

        client.connect("192.168.2.190").use { connection ->
            val ac = AuthenticationContext.anonymous()
            //AuthenticationContext("", "".toCharArray(), "")
            val session: Session = connection.authenticate(ac)
            val SHARE_NAME = "vin"

            // Connect to Share
            val diskShare = session.connectShare(SHARE_NAME) as DiskShare?

            val smbShareSet: MutableSet<SMB2ShareAccess> = HashSet()
            smbShareSet.addAll(SMB2ShareAccess.ALL)//.iterator().next()) // this is to get READ only

            val localPath = JFile(filesDir, "temp")
            localPath.mkdir()

            diskShare?.let {
                addFileRecursively(localPath, "", it)
            }

        }
    }

    private suspend fun appendTextWithContext(text: String) = withContext(Dispatchers.Main) {
        appendText(text)
    }

    private fun appendText(text: String) {
        tvMsg.append(text + "\n")
    }
}

fun main(arr: Array<String>) {
    val config = SmbConfig.builder().withTimeout(120, TimeUnit.SECONDS)
        .withTimeout(
            120,
            TimeUnit.SECONDS
        ) // Timeout sets read, write and Transact timeouts (default is 60 seconds)
        .withSoTimeout(180, TimeUnit.SECONDS) // Socket timeout (default is 0 seconds)
        .build()

    val client = SMBClient(config)

    client.connect("192.168.2.190").use { connection ->
        val ac = AuthenticationContext.anonymous()
        //AuthenticationContext("", "".toCharArray(), "")
        val session: Session = connection.authenticate(ac)
        val SHARE_NAME = "vin"

        // Connect to Share
        val diskShare = session.connectShare(SHARE_NAME) as DiskShare?

        val smbShareSet: MutableSet<SMB2ShareAccess> = HashSet()
        smbShareSet.addAll(SMB2ShareAccess.ALL)//.iterator().next()) // this is to get READ only

        val localPath = JFile("C:\\Users\\Administrator\\Desktop", "temp")
        if (localPath.exists()) localPath.deleteRecursively()
        localPath.mkdir()

        diskShare?.let {
            addFileRecursively(localPath, "", it)
        }
    }
}


fun addFileRecursively(localRoot: JFile, dir: String, diskShare: DiskShare) {
    for (fileIdBothDirectoryInformation in diskShare.list(dir)) {
        val fileName = fileIdBothDirectoryInformation.fileName
        println("File : $fileName")

        if (fileName == "." || fileName == "..") continue

        if (EnumWithValue.EnumUtils.isSet(
                fileIdBothDirectoryInformation.fileAttributes,
                FileAttributes.FILE_ATTRIBUTE_DIRECTORY
            )
        ) {
            addFileRecursively(
                JFile(localRoot, fileName),
                if (dir.isBlank()) fileName else "$dir\\$fileName",
                diskShare
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

        val bufWriter = FileOutputStream(
            JFile(
                localRoot.apply { mkdirs() },
                fileName
            )
        ).buffered()

        bufReader.use { reader ->
            bufWriter.use { writer ->
                reader.copyTo(writer)
            }
        }
    }
}