package com.android.server.wifi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.wifi.V1_3.IWifiChip;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;
import java.util.Random;

/* access modifiers changed from: package-private */
public class OppoDualStaNotification {
    private static final String ACTION_NOTIFY_CANCEL_APP = "com.coloros.wirelesssettings.wifi.sla.DualStaFragment";
    private static final String ACTION_NOTIFY_LAUNCH__APP = "com.coloros.wirelesssettings.wifi.sla.DualStaFragment";
    public static final int CMD_NOTIFY_CANCEL_APP = 1;
    public static final int CMD_NOTIFY_LAUNCH_APP = 0;
    private static boolean DBG = true;
    private static final String DUAL_STA_FIRST_TAKE_EFFECT = "DUAL_STA_FIRST_TAKE_EFFECT";
    public static final String KEY_DUAL_STA_SWITCH = "dual_sta_switch_on";
    private static final String TAG = "OppoDualStaNotification";
    private CustomIntentReceiver mCancelNotificationBroadcastReceiver;
    private CustomIntentReceiver mClieckNotificationBroadcastReceiver;
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mIsFirstTakeEffectNotifShowed = false;
    private NotificationManager mNotificationManager;
    private TelephonyManager mTelephonyManager;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;

    public OppoDualStaNotification(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
    }

    private class CustomIntentReceiver extends BroadcastReceiver {
        private final String mAction;
        private final int mToken;
        private final int mWhat;

        CustomIntentReceiver(String action, int token, int what) {
            this.mToken = token;
            this.mWhat = what;
            this.mAction = action + "_" + token;
            OppoDualStaNotification.this.mContext.registerReceiver(this, new IntentFilter(this.mAction));
        }

        public PendingIntent getPendingIntent() {
            Intent intent = new Intent(this.mAction);
            intent.setPackage(OppoDualStaNotification.this.mContext.getPackageName());
            return PendingIntent.getBroadcast(OppoDualStaNotification.this.mContext, 0, intent, 0);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(this.mAction)) {
                OppoDualStaNotification.this.handleCustomIntentRecvMessage(this.mWhat);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleCustomIntentRecvMessage(int msg) {
        if (msg == 0) {
            log("CMD_NOTIFY_LAUNCH_APP");
            displayWifiSettingDualStaFragment();
        } else if (msg == 1) {
            log("CMD_NOTIFY_CANCEL_APP");
        }
    }

    private void displayWifiSettingDualStaFragment() {
        Intent intent = new Intent("oppo.settings.WIFI_DUAL_SETTINGS");
        intent.addFlags(IWifiChip.ChipCapabilityMask.DUAL_BAND_DUAL_CHANNEL);
        intent.setPackage("com.coloros.wirelesssettings");
        clearAllNotifying();
        try {
            if (this.mContext != null) {
                this.mContext.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "displayWifiSettingDualStaFragment catch RemoteException");
        }
    }

    private PendingIntent createClickNotificationIntent() {
        if (this.mClieckNotificationBroadcastReceiver == null) {
            this.mClieckNotificationBroadcastReceiver = new CustomIntentReceiver("com.coloros.wirelesssettings.wifi.sla.DualStaFragment", new Random().nextInt(), 0);
        }
        return this.mClieckNotificationBroadcastReceiver.getPendingIntent();
    }

    private PendingIntent createCancelNotificationIntent() {
        if (this.mCancelNotificationBroadcastReceiver == null) {
            this.mCancelNotificationBroadcastReceiver = new CustomIntentReceiver("com.coloros.wirelesssettings.wifi.sla.DualStaFragment", new Random().nextInt(), 1);
        }
        return this.mCancelNotificationBroadcastReceiver.getPendingIntent();
    }

    public void showFirstTakeEffectNotification() {
        String channelId = SystemNotificationChannels.NETWORK_ALERTS;
        CharSequence title = this.mContext.getText(201653632);
        CharSequence details = this.mContext.getText(201653633);
        try {
            this.mNotificationManager.notifyAsUser("DulaStaFirstTakeEffectNotification:", 740, new Notification.Builder(this.mContext, channelId).setWhen(System.currentTimeMillis()).setShowWhen(false).setSmallIcon(17303519).setAutoCancel(true).setTicker(title).setColor(this.mContext.getColor(17170460)).setContentTitle(title).setContentText(details).setContentIntent(createClickNotificationIntent()).setLocalOnly(true).setOnlyAlertOnce(true).build(), UserHandle.ALL);
        } catch (NullPointerException npe) {
            Log.d(TAG, "showDualStaFirstTakeEffectNotification: visible notificationManager error" + npe);
        }
        this.mIsFirstTakeEffectNotifShowed = true;
    }

    private void clearFirstTakeEffectNotification() {
        try {
            this.mNotificationManager.cancelAsUser("DulaStaFirstTakeEffectNotification:", 740, UserHandle.ALL);
        } catch (NullPointerException e) {
            Log.d(TAG, "failed to clearFirstTakeEffectNotification!");
        }
    }

    public void clearAllNotifying() {
        if (this.mIsFirstTakeEffectNotifShowed) {
            clearFirstTakeEffectNotification();
            this.mIsFirstTakeEffectNotifShowed = false;
        }
        CustomIntentReceiver customIntentReceiver = this.mClieckNotificationBroadcastReceiver;
        if (customIntentReceiver != null) {
            this.mContext.unregisterReceiver(customIntentReceiver);
            this.mClieckNotificationBroadcastReceiver = null;
        }
        CustomIntentReceiver customIntentReceiver2 = this.mCancelNotificationBroadcastReceiver;
        if (customIntentReceiver2 != null) {
            this.mContext.unregisterReceiver(customIntentReceiver2);
            this.mCancelNotificationBroadcastReceiver = null;
        }
        log("Clear all Dual Sta Notifying");
    }

    public boolean isDualStaEnabled() {
        return (Settings.Global.getInt(this.mContext.getContentResolver(), "dual_sta_switch_on", 1) == 1) && this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_DUAL_STA_ENABLED", true);
    }

    public boolean isDualStaFirstTakeEffect() {
        boolean isFirstTakeEffect = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), DUAL_STA_FIRST_TAKE_EFFECT, 1) != 1) {
            isFirstTakeEffect = false;
        }
        return isFirstTakeEffect;
    }

    public void setDualStaFirstTakeEffectFlag() {
        Settings.Global.putInt(this.mContext.getContentResolver(), DUAL_STA_FIRST_TAKE_EFFECT, 0);
    }

    private void log(String str) {
        if (DBG) {
            Log.d(TAG, "*****" + str);
        }
    }
}
