package com.vin.sambademo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import kotlinx.android.synthetic.main.activity_main.*
import java.net.MalformedURLException


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bFiles.setOnClickListener {
            loadSamba()
        }
    }

    private fun loadSamba() {
        //-----------------------[code]---------------------------------//
        val domains: Array<SmbFile>
        try {
            domains = SmbFile("smb://192.168.2.190").listFiles()
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

    private fun appendText(text: String) {
        tvMsg.append(text + "\n")
    }
}