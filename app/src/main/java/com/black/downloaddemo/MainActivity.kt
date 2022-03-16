package com.black.downloaddemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.black.library.DownloadManager
import com.black.library.FileConfig
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_download).setOnClickListener {
            start()
        }

    }

    private fun start() {
        DownloadManager.Builder(this)
            .apply {
                isNetworkReConnect = true
                downloadUrl = ""
                savePath = "${filesDir}${FileConfig.download_path}"
            }
            .build()
            .setListener(object : DownloadManager.DownloadListener {
                override fun onComplete(file: File) {
                    Log.d("MainActivity", "onComplete: ${file.absolutePath}")
                }

                override fun onProgress(progress: Int) {
                    Log.d("MainActivity", "onProgress: $progress")
                }

                override fun onError() {
                    Log.d("MainActivity", "onError")
                }
            }).start()
    }
}