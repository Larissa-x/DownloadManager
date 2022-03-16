# 断点续传下载框架
[![](https://jitpack.io/v/Larissa-x/DownloadManager.svg)](https://jitpack.io/#Larissa-x/DownloadManager)

#### 本库采用了workManager + kotlin + okhttp实现了断点续传下载、断网重连下载
### 使用方式
#### 步骤1.将其添加到存储库末尾的根 build.gradle 中：
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
#### 步骤2.添加依赖项
```
dependencies {
	implementation 'com.github.Larissa-x:DownloadManager:1.0.0'
}
```
#### 步骤3.在你的application中进行初始化
```
DownloadManager.init(this);
```
#### 步骤4.在清单文件中添加权限，如果已经有该权限，请勿重复添加
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
#### 步骤5.在kotlin中的使用方式
```
DownloadManager.Builder(context)
            .apply {
                //是否开启断网重连
                isNetworkReConnect = true
                downloadUrl = "你的url"
                //本库默认的下载路径、如果需要别的路径，需要自己手动创建文件夹
                savePath = "${filesDir}${FileConfig.download_path}"
            }
            .build()
            //下载进度回调监听
            .setListener(object : DownloadManager.DownloadListener {
                override fun onComplete(file: File) {
                    Log.d("下载成功", "onComplete: ${file.absolutePath}")
                }

                override fun onProgress(progress: Int) {
                    Log.d("下载中 progress进度回调 0 - 100", "onProgress: $progress")
                }

                override fun onError() {
                    Log.d("下载失败", "onError")
                }
            }).start()//开始下载任务
```
#### 在java中的使用方式
```
 DownloadManager.Builder builder = new DownloadManager.Builder(this);
        //是否开启断网重连
        builder.setNetworkReConnect(true);
        //本库默认的下载路径、如果需要别的路径，需要自己手动创建文件夹
        builder.setSavePath(getFilesDir() + FileConfig.download_path);
        //下载url
        builder.setDownloadUrl("");
        builder.build()

                .setListener(new DownloadManager.DownloadListener() {//下载监听回调
                    @Override
                    public void onComplete(@NonNull File file) {
                        //下载成功回调file、如果只使用一次，使用后可以调用file.delete()删除文件
                    }

                    @Override
                    public void onProgress(int progress) {
                        //已经转换好的下载进度
                    }

                    @Override
                    public void onError() {
                        //发生了异常
                    }
                })
                .start();
```
