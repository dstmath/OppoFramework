package com.color.antivirus;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public class ColorDummyAntiVirusBehaviorManager implements IColorAntiVirusBehaviorManager {
    private static volatile ColorDummyAntiVirusBehaviorManager sInstance;

    public static ColorDummyAntiVirusBehaviorManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyAntiVirusBehaviorManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyAntiVirusBehaviorManager();
                }
            }
        }
        return sInstance;
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void checkAndSendReceiverInvocation(Intent intent, boolean dy) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void senderInit() {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public IBinder getOrCreateFakeBinder(IBinder origBinder, String svcName) {
        return origBinder;
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public boolean isInstance(IBinder binder) {
        return false;
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public boolean checkNeedReplace(String name) {
        return false;
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void setAction(int actionId, int uid) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void broadcastIntent(Intent intent, int uid) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void setForegroundApp(String packageName) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void setAction_startActivityMayWait(int uid, String action, ComponentName comp, String pkg, Uri dat) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void setAction_AMS_getContentProvider(int uid, String name) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void setAction_SensorManager_registerListenerImp(int uid, int sensortype) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void setAction_SetContentProviderAction(Uri uri, int action, int uid) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void setComponentEnabledSetting(int newState, int uid) {
    }

    @Override // com.color.antivirus.IColorAntiVirusBehaviorManager
    public void setAction_addWindow(int actionId, int uid, int type) {
    }
}
