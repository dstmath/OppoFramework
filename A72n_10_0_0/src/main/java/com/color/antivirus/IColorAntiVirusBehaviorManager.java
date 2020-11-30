package com.color.antivirus;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public interface IColorAntiVirusBehaviorManager extends IOppoCommonFeature {
    public static final IColorAntiVirusBehaviorManager DEFAULT = new IColorAntiVirusBehaviorManager() {
        /* class com.color.antivirus.IColorAntiVirusBehaviorManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAntiVirusBehaviorManager";

    @Override // android.common.IOppoCommonFeature
    default IColorAntiVirusBehaviorManager getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAntiVirusBehaviorManager;
    }

    default void checkAndSendReceiverInvocation(Intent intent, boolean dy) {
    }

    default void senderInit() {
    }

    default IBinder getOrCreateFakeBinder(IBinder origBinder, String svcName) {
        return null;
    }

    default boolean isInstance(IBinder binder) {
        return false;
    }

    default boolean checkNeedReplace(String name) {
        return false;
    }

    default void setAction(int actionId, int uid) {
    }

    default void broadcastIntent(Intent intent, int uid) {
    }

    default void setForegroundApp(String packageName) {
    }

    default void setAction_startActivityMayWait(int uid, String action, ComponentName comp, String pkg, Uri dat) {
    }

    default void setAction_AMS_getContentProvider(int uid, String name) {
    }

    default void setAction_SensorManager_registerListenerImp(int uid, int sensortype) {
    }

    default void setAction_SetContentProviderAction(Uri uri, int action, int uid) {
    }

    default void setComponentEnabledSetting(int newState, int uid) {
    }

    default void setAction_addWindow(int actionId, int uid, int type) {
    }
}
