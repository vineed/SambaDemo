package com.vin.sambademo

import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.protocol.commons.EnumWithValue
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.FileOutputStream
import java.lang.Runnable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet


typealias JFile = java.io.File

class MainActivity : AppCompatActivity() {

    val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@MainActivity, "Connection failed!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bFiles.setOnClickListener {
            //CoroutineScope(Dispatchers.Default).launch(coroutineExceptionHandler) {
            /*var modifiedOn = System.currentTimeMillis()
            modifiedOn -= modifiedOn % 1000 // skimming nanoseconds part

            Utility.loadSambaWithSMBJ("192.168.2.190", filesDir, modifiedOn)

            Utility.deleteNotTouchedFiles(filesDir, modifiedOn)*/

            runPeriodicSync(0)
            ///}
        }
    }

    val interval = TimeUnit.MINUTES.toMillis(1)
    val handler = Handler()
    val runnable = Runnable {
        CoroutineScope(Dispatchers.Default).launch(coroutineExceptionHandler) {
            var modifiedOn = System.currentTimeMillis()
            modifiedOn -= modifiedOn % 1000 // skimming nanoseconds part

            Utility.loadSambaWithSMBJ("192.168.10.5", filesDir, modifiedOn)

            Utility.deleteNotTouchedFiles(filesDir, modifiedOn)

        }

        //runPeriodicSync(interval)
    }

    private fun runPeriodicSync(interval: Long) {
        if (!isDestroyed) {
            Toast.makeText(this, "Sync in progress..", Toast.LENGTH_SHORT).show()
            handler.postDelayed(runnable, interval)
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
            Utility.addFileRecursively(localPath, "", it, System.currentTimeMillis())
        }
    }
}


