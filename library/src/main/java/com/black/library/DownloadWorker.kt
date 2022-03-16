package com.black.library

import android.content.Context
import android.os.FileUtils
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

/**
 * 下载任务构建
 */
class DownloadWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val inputData = inputData
        val url: String? = inputData.getString(EXTRA_REQUEST_URL)
        val path = inputData.getString(EXTRA_FILE_PATH)
        if (TextUtils.isEmpty(url)) return Result.failure()
        if (TextUtils.isEmpty(path)) return Result.failure()
        //获取文件总长度
        val totalSize = getFileLength(url!!)
        if (totalSize == 0L || totalSize < 0) {
            return Result.retry()
        }
        val fileName = getFileName(url)
        val file = File(path, fileName)

        //如果文件不存在，说明是第一次下载，创建一个新的文件,并开始下载
        if (!file.exists()) {
            file.createNewFile()
//            FileUtils.createFileByDeleteOldFile(file)
            return startDownload(url, file, totalSize)
        } else {
            //如果文件已存在,获取文件的长度，判断上一次的加载进度是否已完成，如果未完成就继续下载，如果已完成就直接返回成功
            var startIndex = file.length() //获取文件长度
            //文件异常，删除已经存储的apk，重新创建一个文件，并且位置为0
            if (startIndex < 0 || startIndex > totalSize) {
                file.delete()
                file.createNewFile()
                startIndex = 0
            }
            //断点下载，开始位置是上次已下载长度
            return if (startIndex != totalSize) {
                continuousDownload(url, startIndex, totalSize, file)
            } else {
                //如果下载完成，直接回调成功
                val data = Data.Builder().putString(EXTRA_FILE_PATH, file.absolutePath).build()
                return Result.success(data)
            }
        }
    }

    /**
     * 获取文件总长度
     */
    private fun getFileLength(url: String): Long {
        val request: Request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        return if (response.body?.contentLength() != null) {
            response.body?.contentLength()!!
        } else {
            -1
        }
    }

    /**
     * 普通下载任务开始
     */
    private fun startDownload(url: String, file: File, fileLength: Long): Result {
        val request: Request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.d(TAG, "startDownload: 下载失败,正在准备重试")
            return Result.retry()
        }
        val fos = FileOutputStream(file)
        val inputStream = response.body!!.byteStream()
        val buf = ByteArray(1024)
        var len = 0
        while (inputStream.read(buf).also { len = it } != -1) {
            fos.write(buf, 0, len)
            noticeProgress(file, fileLength)
        }
        fos.flush()
        val data = Data.Builder().putString(EXTRA_FILE_PATH, file.absolutePath).build()
        return Result.success(data)
    }

    /**
     * 续传下载任务开始
     */
    private fun continuousDownload(
        url: String,
        startIndex: Long,
        fileLength: Long,
        file: File,
    ): Result {
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Range", "bytes=$startIndex-$fileLength")
            .build()
        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.d(TAG,"下载失败,正在准备重试")
            return Result.retry()
        }
        if (response.code == 206) {
            var randomAccessFile: RandomAccessFile? = null
            try {
                randomAccessFile = RandomAccessFile(file, "rwd")
                randomAccessFile.seek(startIndex)
                val inputStream = response.body!!.byteStream()
                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    randomAccessFile.write(buffer, 0, len)
                    noticeProgress(file, fileLength)
                }
                val data = Data.Builder().putString(EXTRA_FILE_PATH, file.absolutePath).build()
                return Result.success(data)
            } catch (e: Exception) {
                Log.d(TAG,"下载失败,正在准备重试")
                return Result.retry()
            } finally {
                randomAccessFile?.close()
            }
        } else {
            return Result.retry()
        }
    }

    /**
     * 获取保存的文件名字
     */
    private fun getFileName(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }


    /**
     * 通知进度刷新
     */
    private fun noticeProgress(file: File, fileLength: Long) {
        if (file.exists()) {
            val saveFileLength: Long = file.length()
            if (fileLength != 0L) {
                val builder = Data.Builder()
                    .putInt(EXTRA_PROGRESS, (saveFileLength * 100 / fileLength).toInt())
                setProgressAsync(builder.build())
            } else {
                val builder = Data.Builder()
                    .putInt(EXTRA_PROGRESS, (saveFileLength * 100 / fileLength).toInt())
                setProgressAsync(builder.build())
            }
        } else {
            val builder = Data.Builder().putInt(EXTRA_PROGRESS, 0)
            setProgressAsync(builder.build())
        }
    }

    companion object {
        const val EXTRA_REQUEST_URL = "extra_requestUrl"
        const val EXTRA_FILE_PATH = "extra_filePath"
        const val EXTRA_PROGRESS = "extra_progress"
        const val TAG = "DownloadWorkerTAG"
    }
}