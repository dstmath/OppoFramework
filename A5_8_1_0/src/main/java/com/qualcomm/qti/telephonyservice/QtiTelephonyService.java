package com.qualcomm.qti.telephonyservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qcrilhook.QcRilHook;
import java.util.ArrayList;
import java.util.List;

public class QtiTelephonyService extends Service {
    private static final String TAG = "QtiTelephonyService";
    private List<QcRilAudio> mQcRilAudioHals = new ArrayList();
    private IQcRilHook mQcRilHook;
    private QtiTelephonyServiceImpl mService;

    public void onCreate() {
        Log.i(TAG, "onCreate:");
        this.mQcRilHook = new QcRilHook(getApplicationContext());
        this.mService = new QtiTelephonyServiceImpl(super.getApplicationContext(), this.mQcRilHook);
        int slotCount = TelephonyManager.from(this).getSimCount() + 1;
        for (int i = 1; i < slotCount; i++) {
            this.mQcRilAudioHals.add(new QcRilAudio(i, getApplicationContext()));
        }
    }

    private void setQcRilHook(IQcRilHook qcRilHook) {
        this.mQcRilHook = qcRilHook;
        this.mService.setQcRilHook(qcRilHook);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return this.mService;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        this.mService = null;
        for (QcRilAudio h : this.mQcRilAudioHals) {
            h.dispose();
        }
        super.onDestroy();
    }
}
