package com.retry.vuga.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class DownloadService extends Service {


    private final IBinder dBinder = new DownloadBinder();
    MyDownloader myDownloader;


    public MyDownloader getMyDownloader() {
        return myDownloader;
    }

    public void setMyDownloader(MyDownloader myDownloader) {
        this.myDownloader = myDownloader;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return dBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();


        if (myDownloader == null) {
            myDownloader = new MyDownloader(this, this);
        }
    }

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

}
