package com.android.server.wifi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.format.Formatter;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.wifi.OppoTetheringStatistician;

public class OppoTetheringNotification {
    public static final int BLUETOOTH_ONLY_TETHERED = 1;
    private static boolean DBG = false;
    public static final String EXTRA_TETHER_STATE_STATUS = "ExtraTetherStateStatus";
    public static final int NO_INTERFACE_TETHERED = 0;
    private static final String PRIMARY_CHANNEL = "TetheringNotification";
    private static final String TAG = "OppoTetheringNotification";
    public static final int USB_ONLY_TETHERED = 2;
    public static final int WIFI_ONLY_TETHERED = 4;
    private TetherNotificationReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mFreq = 0;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public int mHotspotClientNums;
    /* access modifiers changed from: private */
    public Runnable mNotifyTask = new Runnable() {
        /* class com.android.server.wifi.OppoTetheringNotification.AnonymousClass2 */

        public void run() {
            long sizeBytes = OppoTetheringNotification.this.mTetheringStatistician.getTotalTraffic();
            OppoTetheringNotification oppoTetheringNotification = OppoTetheringNotification.this;
            String unused = oppoTetheringNotification.mTrifficFormat = Formatter.formatFileSize(oppoTetheringNotification.mContext, sizeBytes, 8);
            if (OppoTetheringNotification.this.mHotspotClientNums != 0) {
                OppoTetheringNotification.this.showSoftApOrWifiSharingNotification();
                if (OppoTetheringNotification.this.mFreq != 0 && !OppoTetheringNotification.this.mHandler.hasCallbacks(OppoTetheringNotification.this.mNotifyTask)) {
                    OppoTetheringNotification.this.mHandler.postDelayed(this, (long) (OppoTetheringNotification.this.mFreq * 1000));
                    return;
                }
                return;
            }
            Log.e(OppoTetheringNotification.TAG, "No client connected, don't refresh UI for data triffic");
        }
    };
    /* access modifiers changed from: private */
    public OppoTetheringStatistician mTetheringStatistician;
    /* access modifiers changed from: private */
    public String mTrifficFormat = "0 KB";
    /* access modifiers changed from: private */
    public int mWhatInterfaceTethering;

    public OppoTetheringNotification(Context context) {
        this.mContext = context;
        this.mWhatInterfaceTethering = 0;
        this.mHotspotClientNums = 0;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED");
        filter.addAction("android.net.conn.TETHER_STATE_CHANGED");
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mBroadcastReceiver = new TetherNotificationReceiver();
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mHandler = new Handler();
        this.mTetheringStatistician = new OppoTetheringStatistician(context);
    }

    private class TetherNotificationReceiver extends BroadcastReceiver {
        private TetherNotificationReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x0052  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x0084  */
        public void onReceive(Context content, Intent intent) {
            char c;
            OppoTetheringNotification.this.Logd(" onReceive: intent: " + intent);
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode != -1754841973) {
                if (hashCode != -237067668) {
                    if (hashCode == -19011148 && action.equals("android.intent.action.LOCALE_CHANGED")) {
                        c = 2;
                        if (c != 0) {
                            int unused = OppoTetheringNotification.this.mWhatInterfaceTethering = intent.getIntExtra(OppoTetheringNotification.EXTRA_TETHER_STATE_STATUS, 0);
                            OppoTetheringNotification.this.handleTetherStateChanged();
                            return;
                        } else if (c == 1) {
                            int unused2 = OppoTetheringNotification.this.mHotspotClientNums = intent.getIntExtra("HotspotClientNum", 0);
                            OppoTetheringNotification.this.handleHotspotClientChanged();
                            return;
                        } else if (c != 2) {
                            Log.e(OppoTetheringNotification.TAG, " unknown action received: " + action);
                            return;
                        } else {
                            OppoTetheringNotification.this.handleLocaleChanged();
                            return;
                        }
                    }
                } else if (action.equals("android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED")) {
                    c = 1;
                    if (c != 0) {
                    }
                }
            } else if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
                c = 0;
                if (c != 0) {
                }
            }
            c = 65535;
            if (c != 0) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleTetherStateChanged() {
        int i = this.mWhatInterfaceTethering;
        if (i == 0) {
            cancelTetheredNotification();
        } else if (i == 1) {
            oppoShowTetheredNotification(201852112);
        } else if (i == 2) {
            oppoShowTetheredNotification(201852004);
        } else if (i != 4) {
            oppoShowTetheredNotification(201852002);
        } else {
            showSoftApOrWifiSharingNotification();
            cancelTetheredStoppedNotification();
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager.createNotificationChannel(new NotificationChannel(PRIMARY_CHANNEL, Resources.getSystem().getText(201653657), 2));
        }
    }

    /* access modifiers changed from: private */
    public void handleHotspotClientChanged() {
        if (this.mWhatInterfaceTethering == 4) {
            this.mFreq = SystemProperties.getInt("softap.notify.freq", 3);
            showSoftApOrWifiSharingNotification();
        }
        if (this.mHotspotClientNums == 0) {
            stopTrafficStatisics();
        }
    }

    /* access modifiers changed from: private */
    public void showSoftApOrWifiSharingNotification() {
        if (WifiInjector.getInstance().getOppoWifiSharingManager().isWifiSharingTethering()) {
            oppoShowTetheredNotification(201852208);
            return;
        }
        oppoShowTetheredNotification(201852003);
        if (this.mHotspotClientNums != 0) {
            startTrafficStatisics();
        } else {
            Log.e(TAG, "No client connected, don't start traffic Statisics");
        }
    }

    private void startTrafficStatisics() {
        this.mTetheringStatistician.start(new OppoTetheringStatistician.OnTetheringStaticsCallback() {
            /* class com.android.server.wifi.OppoTetheringNotification.AnonymousClass1 */

            @Override // com.android.server.wifi.OppoTetheringStatistician.OnTetheringStaticsCallback
            public void onTetheringStoped() {
                Log.e(OppoTetheringNotification.TAG, "onTetheringStoped");
                OppoTetheringNotification.this.cancelTetheredNotification();
                OppoTetheringNotification.this.oppoShowTetherStoppedNotification();
            }
        });
        if (!this.mHandler.hasCallbacks(this.mNotifyTask)) {
            this.mHandler.postDelayed(this.mNotifyTask, WifiMetrics.TIMEOUT_RSSI_DELTA_MILLIS);
        }
    }

    private void stopTrafficStatisics() {
        this.mHandler.removeCallbacks(this.mNotifyTask);
        this.mTetheringStatistician.stop();
    }

    /* access modifiers changed from: private */
    public void handleLocaleChanged() {
        if (this.mWhatInterfaceTethering != 0) {
            handleTetherStateChanged();
        }
    }

    private void oppoShowTetheredNotification(int notificationType) {
        int largeIcon;
        CharSequence title;
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager != null) {
            createNotificationChannel(notificationManager);
            switch (notificationType) {
                case 201852003:
                    largeIcon = 201852114;
                    break;
                case 201852004:
                    largeIcon = 201852116;
                    break;
                case 201852112:
                    largeIcon = 201852115;
                    break;
                case 201852208:
                    largeIcon = 201852208;
                    break;
                default:
                    largeIcon = 201852117;
                    break;
            }
            Intent intent = new Intent();
            Resources r = Resources.getSystem();
            if (notificationType == 201852003) {
                intent.setAction("android.settings.OPPO_WIFI_AP_SETTINGS");
                int i = this.mHotspotClientNums;
                if (i == 0) {
                    title = r.getText(201590070);
                } else {
                    title = r.getQuantityString(202506240, i, Integer.valueOf(i));
                }
            } else if (notificationType == 201852208) {
                intent.setAction("oppo.settings.WLAN_SHARE_SETTINGS");
                int i2 = this.mHotspotClientNums;
                if (i2 == 0) {
                    title = r.getText(201590162);
                } else {
                    title = r.getQuantityString(202506241, i2, Integer.valueOf(i2));
                }
            } else {
                intent.setAction("android.settings.OPPO_TETHER_SETTINGS");
                title = r.getText(201590072);
            }
            intent.setFlags(1073741824);
            PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
            CharSequence message = r.getText(17041125);
            if (!(this.mFreq == 0 || this.mHotspotClientNums == 0 || WifiInjector.getInstance().getOppoWifiSharingManager().isWifiSharingTethering())) {
                message = ((Object) r.getText(201653627)) + this.mTrifficFormat;
            }
            Notification mTeNotification = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS).setContentTitle(title).setContentText(message).setTicker(title).setContentIntent(pi).setSmallIcon(201852197).setLargeIcon(BitmapFactory.decodeResource(this.mContext.getResources(), largeIcon)).setChannelId(PRIMARY_CHANNEL).build();
            mTeNotification.flags = 2;
            mTeNotification.defaults &= -2;
            Logd("notify teteredNotification ticker: " + ((Object) title));
            notificationManager.notify(0, mTeNotification);
        }
    }

    /* access modifiers changed from: private */
    public void cancelTetheredNotification() {
        NotificationManager mNM = (NotificationManager) this.mContext.getSystemService("notification");
        if (mNM != null) {
            mNM.cancel(0);
        } else {
            Log.e(TAG, " failed to get NotificationManager!");
        }
        this.mHotspotClientNums = 0;
        this.mWhatInterfaceTethering = 0;
        stopTrafficStatisics();
    }

    private void cancelTetheredStoppedNotification() {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(1);
    }

    /* access modifiers changed from: private */
    public void Logd(String whatToLog) {
        if (DBG) {
            Log.d(TAG, whatToLog);
        }
    }

    public void enableLogging(boolean enable) {
        DBG = enable;
    }

    /* access modifiers changed from: private */
    public void oppoShowTetherStoppedNotification() {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        createNotificationChannel(notificationManager);
        Intent intent = new Intent();
        Resources r = Resources.getSystem();
        intent.setAction("android.settings.OPPO_WIFI_AP_SETTINGS");
        CharSequence title = r.getText(201653629).toString();
        intent.setFlags(1073741824);
        PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
        CharSequence message = r.getText(201653628);
        Bitmap bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 201852114);
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.bigText(message);
        Notification mTeNotification = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS).setContentTitle(title).setStyle(bigTextStyle).setTicker(title).setContentIntent(pi).setSmallIcon(201852197).setLargeIcon(bitmap).setChannelId(PRIMARY_CHANNEL).build();
        mTeNotification.defaults &= -2;
        Logd("notify teteredNotification ticker: " + ((Object) title));
        notificationManager.notify(1, mTeNotification);
    }
}
