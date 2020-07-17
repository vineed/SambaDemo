package com.vin.sambademo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.MalformedURLException


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

            // Connect to Share
            val diskShare = session.connectShare("forbes_logo") as DiskShare?

            diskShare?.let {
                for (f in it.list("")) {
                    println("File : " + f.fileName)
                    appendTextWithContext("File : " + f.fileName)
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