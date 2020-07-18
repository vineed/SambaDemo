package com.vin.sambademo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.*
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

    private suspend fun loadSambaWithSMBJ() {
        val client = SMBClient()

        client.connect("192.168.2.190").use { connection ->
            val ac = AuthenticationContext.anonymous()
            //AuthenticationContext("", "".toCharArray(), "")
            val session: Session = connection.authenticate(ac)
            val SHARE_NAME = "vin"

            // Connect to Share
            val diskShare = session.connectShare(SHARE_NAME) as DiskShare?

            val smbShareSet: MutableSet<SMB2ShareAccess> = HashSet()
            smbShareSet.add(SMB2ShareAccess.ALL.iterator().next()) // this is to get READ only

            val localPath = JFile(filesDir, "temp")
            localPath.mkdir()

            diskShare?.let {
                for (fileIdBothDirectoryInformation in it.list("")) {
                    val fileName = fileIdBothDirectoryInformation.fileName
                    println("File : $fileName")
                    appendTextWithContext("File : $fileName")

                    if(fileName == "." || fileName == "..") continue

                    val remoteSmbjFile: File = it.openFile(
                        "$fileName",
                        EnumSet.of(AccessMask.GENERIC_ALL),
                        null,
                        smbShareSet,
                        null,
                        null
                    )

                    val bufReader = remoteSmbjFile.inputStream.buffered()

                    val bufWriter = FileOutputStream(JFile(localPath, fileName)).buffered()

                    bufReader.use { reader ->
                        bufWriter.use { writer ->
                            reader.copyTo(writer)
                        }
                    }
                }
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