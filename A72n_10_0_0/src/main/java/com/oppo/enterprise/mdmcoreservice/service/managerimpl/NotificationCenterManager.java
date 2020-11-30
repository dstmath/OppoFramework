package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.alibaba.fastjson.parser.JSONToken;

public class NotificationCenterManager {
    private static final String TAG = NotificationCenterManager.class.getSimpleName();
    private static volatile NotificationCenterManager sInstance;
    private Context mContext;

    public static NotificationCenterManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (NotificationCenterManager.class) {
                if (sInstance == null) {
                    sInstance = new NotificationCenterManager(context);
                }
            }
        }
        return sInstance;
    }

    private NotificationCenterManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public boolean enableAllNotificationChannel(String pkg) throws RemoteException {
        String str = TAG;
        Log.d(str, "enableAllNotificationChannel() called with: pkg = [" + pkg + "]");
        if (Build.VERSION.SDK_INT != 28) {
            return false;
        }
        this.mContext.getContentResolver().call(Uri.parse("content://com.oppo.notification_third_party_custom"), "enableAllNotificationChannel", pkg, (Bundle) null);
        return true;
    }

    public boolean disableAllNotificationChannel(String pkg) throws RemoteException {
        String str = TAG;
        Log.d(str, "disableAllNotificationChannel() called with: pkg = [" + pkg + "]");
        if (Build.VERSION.SDK_INT != 28) {
            return false;
        }
        this.mContext.getContentResolver().call(Uri.parse("content://com.oppo.notification_third_party_custom"), "disableAllNotificationChannel", pkg, (Bundle) null);
        return true;
    }

    public boolean switchNotificationChannel(String pkg, String channelID, String manualType, boolean enabled) throws RemoteException {
        String str = TAG;
        Log.d(str, "switchNotificationChannel() called with: pkg = [" + pkg + "], channelID = [" + channelID + "], manualType = [" + manualType + "], enabled = [" + enabled + "]");
        if (Build.VERSION.SDK_INT != 28) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putString("channelID", channelID);
        bundle.putString("manualType", revertManualType2P(manualType));
        bundle.putBoolean("enabled", enabled);
        this.mContext.getContentResolver().call(Uri.parse("content://com.oppo.notification_third_party_custom"), "switchNotificationChannel", pkg, bundle);
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private String revertManualType2P(String manualTypeQ) {
        char c;
        switch (manualTypeQ.hashCode()) {
            case -1903660933:
                if (manualTypeQ.equals("show_icon")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1396342996:
                if (manualTypeQ.equals("banner")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1165461084:
                if (manualTypeQ.equals("priority")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -772596591:
                if (manualTypeQ.equals("badge_option")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -381820416:
                if (manualTypeQ.equals("lock_screen")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 102970646:
                if (manualTypeQ.equals("light")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 109627663:
                if (manualTypeQ.equals("sound")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 451310959:
                if (manualTypeQ.equals("vibrate")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 738950403:
                if (manualTypeQ.equals("channel")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return "ChannelNotification";
            case 1:
                return "ShowIconStatusBar";
            case 2:
                return "LockScreen";
            case 3:
                return "Banner";
            case 4:
                return "Sound";
            case 5:
                return "Vibrate";
            case JSONToken.TRUE /* 6 */:
                return "Priority";
            case JSONToken.FALSE /* 7 */:
                return "Light";
            case JSONToken.NULL /* 8 */:
                return "Badge";
            default:
                return "";
        }
    }
}
