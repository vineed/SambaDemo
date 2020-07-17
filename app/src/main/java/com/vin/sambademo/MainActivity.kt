package com.vin.sambademo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import jcifs.CIFSContext
import jcifs.context.SingletonContext
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
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

        client.connect("SERVERNAME").use { connection ->
            val ac = AuthenticationContext.anonymous()
            //AuthenticationContext("", "".toCharArray(), "")
            val session: Session = connection.authenticate(ac)

            // Connect to Share
            val diskShare = session.connectShare("forbes_logo") as DiskShare?

            diskShare?.let {
                for (f in it.list("FOLDER", "*.TXT")) {
                    println("File : " + f.fileName)
                    appendTextWithContext("File : " + f.fileName)
                }
            }
        }
    }

    private suspend fun loadSamba() {
        //-----------------------[code]---------------------------------//
        try {
            val base: CIFSContext = SingletonContext.getInstance()
            val auth = base.withAnonymousCredentials()
            /*val authed1 =
                base.withCredentials(NtlmPasswordAuthentication(base, "", "", ""))*/

            val domains: Array<SmbFile> =
                SmbFile("smb://192.168.2.190/forbes_logo", auth).listFiles()

            for (i in domains.indices) {
                println(domains[i])
                appendText(domains[i].toString())
                val servers = domains[i].listFiles()
                for (j in servers.indices) {
                    println("\t" + servers[j])
                    appendTextWithContext("\t" + servers[j])
                }
            }
        } catch (e: SmbException) {
            e.printStackTrace()
            appendText(e.message ?: "Exception")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            appendText(e.message ?: "Exception")
        }
        //------------------------[/code]----------------------------------------//
    }

    private suspend fun appendTextWithContext(text: String) = withContext(Dispatchers.Main) {
        appendText(text)
    }

    private fun appendText(text: String) {
        tvMsg.append(text + "\n")
    }
}