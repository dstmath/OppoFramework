package com.oppo.enterprise.mdmcoreservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;

public class InitService extends Service {
    public IBinder onBind(Intent intent) {
        Log.d("InitService", "onBind");
        return null;
    }

    public void onCreate() {
        Log.d("InitService", "onCreate");
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int i, int i1) {
        Log.d("InitService", "onStartCommand");
        ServiceManager.addService("oppomdmservice", new OppoMdmService(getApplicationContext()));
        return super.onStartCommand(intent, i, i1);
    }

    public void onDestroy() {
        Log.d("InitService", "onDestroy");
        super.onDestroy();
    }
}
