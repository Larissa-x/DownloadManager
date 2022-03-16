package com.black.library

import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.webkit.DownloadListener
import androidx.work.*
import java.io.File

/**
 * @Author Black
 * @Mail larissa_x@163.com
 * @Date 2022-03-16
 * @Describe  断点续传下载封装工具类
 */
class DownloadManager private constructor(var builder: Builder) {
    private var _listener: DownloadListener? = null
    fun start() {
        if (TextUtils.isEmpty(builder.savePath) || TextUtils.isEmpty(builder.downloadUrl)) {
            Log.d(DownloadWorker.TAG, "savePath or downloadUrl is null,This is illegal")
            return
        }

        val requestBuilder = getRequestBuilder()
        if (builder.isNetworkReConnect) {
            //网络连接正常时执行
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            requestBuilder.setConstraints(constraints)
        }
        val request = requestBuilder.build()
        builder.context.let {
            val manager = WorkManager.getInstance(it)
            manager.getWorkInfoByIdLiveData(request.id).observeForever() { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val outputData: Data = workInfo.outputData
                        val filePath = outputData.getString(DownloadWorker.EXTRA_FILE_PATH)
                        _listener?.onComplete(File(filePath))
                    }
                    WorkInfo.State.RUNNING -> {
                        val progressToInt =
                            workInfo.progress.getInt(DownloadWorker.EXTRA_PROGRESS, 0)
                        _listener?.onProgress(progressToInt)
                    }
                    WorkInfo.State.FAILED -> {
                        _listener?.onError()
                        Log.d(DownloadWorker.TAG, "下载失败，请稍后重试")
                    }
                }
            }
            manager.enqueue(request)
        }

    }

    private fun getRequestBuilder(): OneTimeWorkRequest.Builder {
        //构建下载任务
        val data = Data.Builder()
        data.putString(DownloadWorker.EXTRA_REQUEST_URL, builder.downloadUrl)
        //传入要存储的根目录，文件名字会根据downloadUrl进行截取
        data.putString(DownloadWorker.EXTRA_FILE_PATH, builder.savePath)
        return OneTimeWorkRequest.Builder(DownloadWorker::class.java).setInputData(data.build())
    }

    fun setListener(listener: DownloadListener): DownloadManager {
        _listener = listener
        return this
    }

    interface DownloadListener {
        fun onComplete(file: File)
        fun onProgress(progress: Int)
        fun onError()
    }

    class Builder constructor(val context: Context) {
        /** 默认的下载存储目录  */
        var savePath: String? = null

        /** 下载文件的url */
        var downloadUrl: String? = ""

        /** 是否需要断网重连 默认不重连*/
        var isNetworkReConnect: Boolean = false

        /**
         * 返回DownloadManager对象
         */
        fun build(): DownloadManager {
            savePath = "${context.applicationContext?.filesDir}${FileConfig.download_path}"
            return DownloadManager(this)
        }

    }

    companion object {
        @JvmStatic
        fun init(app: Application) {
            val file = File("${app.filesDir}${FileConfig.download_path}")
            if (file.isDirectory) {
                file.mkdirs()
            }
        }
    }
}