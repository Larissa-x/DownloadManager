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
