package com.black.downloaddemo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.black.library.DownloadManager;
import com.black.library.FileConfig;

import java.io.File;

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DownloadManager.Builder builder = new DownloadManager.Builder(this);
        builder.setNetworkReConnect(true);
        builder.setSavePath(getFilesDir() + FileConfig.download_path);
        builder.setDownloadUrl("");
        builder.build()
                .setListener(new DownloadManager.DownloadListener() {
                    @Override
                    public void onComplete(@NonNull File file) {

                    }

                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onError() {

                    }
                })
                .start();
    }
}
