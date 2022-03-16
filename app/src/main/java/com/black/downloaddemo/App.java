package com.black.downloaddemo;

import android.app.Application;
import com.black.library.DownloadManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.init(this);
    }
}
