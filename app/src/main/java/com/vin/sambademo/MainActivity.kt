package com.vin.sambademo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jcifs.CIFSContext
import jcifs.context.SingletonContext
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.MalformedURLException


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bFiles.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                loadSamba()
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
                    appendText("\t" + servers[j])
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

    private suspend fun appendText(text: String) = withContext(Dispatchers.Main) {
        tvMsg.append(text + "\n")
    }
}