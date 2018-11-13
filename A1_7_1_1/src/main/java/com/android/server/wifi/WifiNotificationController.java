package com.android.server.wifi;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.RingtoneManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.IWifiManager;
import android.net.wifi.IWifiManager.Stub;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.internal.telephony.ITelephony;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

final class WifiNotificationController {
    private static final int ICON_NETWORKS_AVAILABLE = 17303261;
    private static final int NUM_SCANS_BEFORE_ACTUALLY_SCANNING = 0;
    private static final String TAG = "WifiNotificationController";
    private final long NOTIFICATION_REPEAT_DELAY_MS;
    private final Context mContext;
    private DetailedState mDetailedState;
    private FrameworkFacade mFrameworkFacade;
    NetworkInfo mNetworkInfo = new NetworkInfo(1, 0, "WIFI", "");
    private Builder mNotificationBuilder;
    private boolean mNotificationEnabled;
    private NotificationEnabledSettingObserver mNotificationEnabledSettingObserver;
    private long mNotificationRepeatTime;
    private boolean mNotificationShown;
    private int mNumScansSinceNetworkStateChange;
    private boolean mShowReselectDialog = false;
    private boolean mWaitForScanResult = false;
    private volatile int mWifiState;
    private final WifiStateMachine mWifiStateMachine;

    private class NotificationEnabledSettingObserver extends ContentObserver {
        public NotificationEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            WifiNotificationController.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_networks_available_notification_on"), true, this);
            synchronized (WifiNotificationController.this) {
                WifiNotificationController.this.mNotificationEnabled = getValue();
            }
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (WifiNotificationController.this) {
                WifiNotificationController.this.mNotificationEnabled = getValue();
                WifiNotificationController.this.resetNotification();
            }
        }

        private boolean getValue() {
            return WifiNotificationController.this.mFrameworkFacade.getIntegerSetting(WifiNotificationController.this.mContext, "wifi_networks_available_notification_on", 1) == 1;
        }
    }

    WifiNotificationController(Context context, Looper looper, WifiStateMachine wsm, FrameworkFacade framework, Builder builder) {
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mFrameworkFacade = framework;
        this.mNotificationBuilder = builder;
        this.mWifiState = 4;
        this.mDetailedState = DetailedState.IDLE;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* renamed from: -android-net-NetworkInfo$DetailedStateSwitchesValues */
            private static final /* synthetic */ int[] f9-android-net-NetworkInfo$DetailedStateSwitchesValues = null;
            final /* synthetic */ int[] $SWITCH_TABLE$android$net$NetworkInfo$DetailedState;

            /* renamed from: -getandroid-net-NetworkInfo$DetailedStateSwitchesValues */
            private static /* synthetic */ int[] m9-getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
                if (f9-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
                    return f9-android-net-NetworkInfo$DetailedStateSwitchesValues;
                }
                int[] iArr = new int[DetailedState.values().length];
                try {
                    iArr[DetailedState.AUTHENTICATING.ordinal()] = 4;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[DetailedState.BLOCKED.ordinal()] = 5;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 1;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[DetailedState.CONNECTED.ordinal()] = 2;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[DetailedState.CONNECTING.ordinal()] = 6;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[DetailedState.DISCONNECTED.ordinal()] = 3;
                } catch (NoSuchFieldError e6) {
                }
                try {
                    iArr[DetailedState.DISCONNECTING.ordinal()] = 7;
                } catch (NoSuchFieldError e7) {
                }
                try {
                    iArr[DetailedState.FAILED.ordinal()] = 8;
                } catch (NoSuchFieldError e8) {
                }
                try {
                    iArr[DetailedState.IDLE.ordinal()] = 9;
                } catch (NoSuchFieldError e9) {
                }
                try {
                    iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 10;
                } catch (NoSuchFieldError e10) {
                }
                try {
                    iArr[DetailedState.SCANNING.ordinal()] = 11;
                } catch (NoSuchFieldError e11) {
                }
                try {
                    iArr[DetailedState.SUSPENDED.ordinal()] = 12;
                } catch (NoSuchFieldError e12) {
                }
                try {
                    iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 13;
                } catch (NoSuchFieldError e13) {
                }
                f9-android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
                return iArr;
            }

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    WifiNotificationController.this.mWifiState = intent.getIntExtra("wifi_state", 4);
                    WifiNotificationController.this.resetNotification();
                    WifiNotificationController.this.mWaitForScanResult = false;
                    WifiNotificationController.this.mShowReselectDialog = false;
                } else if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                    WifiNotificationController.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    DetailedState detailedState = WifiNotificationController.this.mNetworkInfo.getDetailedState();
                    if (detailedState != DetailedState.SCANNING && detailedState != WifiNotificationController.this.mDetailedState) {
                        WifiNotificationController.this.mDetailedState = detailedState;
                        switch (AnonymousClass1.m9-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[WifiNotificationController.this.mDetailedState.ordinal()]) {
                            case 1:
                            case 3:
                                break;
                            case 2:
                                WifiNotificationController.this.mWaitForScanResult = false;
                                break;
                            default:
                                return;
                        }
                        WifiNotificationController.this.resetNotification();
                    }
                } else if (intent.getAction().equals("android.net.wifi.SCAN_RESULTS")) {
                    WifiNotificationController.this.mShowReselectDialog = intent.getBooleanExtra("SHOW_RESELECT_DIALOG", false);
                    WifiNotificationController.this.checkAndSetNotification(WifiNotificationController.this.mNetworkInfo, WifiNotificationController.this.mWifiStateMachine.syncGetScanResultsList());
                }
            }
        }, filter);
        this.NOTIFICATION_REPEAT_DELAY_MS = ((long) this.mFrameworkFacade.getIntegerSetting(context, "wifi_networks_available_repeat_delay", 900)) * 1000;
        this.mNotificationEnabledSettingObserver = new NotificationEnabledSettingObserver(new Handler(looper));
        this.mNotificationEnabledSettingObserver.register();
    }

    /* JADX WARNING: Missing block: B:62:0x0113, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void checkAndSetNotification(NetworkInfo networkInfo, List<ScanResult> scanResults) {
        if (!this.mNotificationEnabled) {
            return;
        }
        if (this.mWifiState == 3) {
            State state = State.DISCONNECTED;
            if (networkInfo != null) {
                state = networkInfo.getState();
            }
            if (this.mWifiStateMachine.hasCustomizedAutoConnect()) {
                Slog.i(TAG, "checkAndSetNotification, mWaitForScanResult:" + this.mWaitForScanResult);
                if (this.mWaitForScanResult && scanResults == null) {
                    showSwitchDialog();
                }
            }
            Slog.i(TAG, "checkAndSetNotification, state:" + state);
            if ((state == State.DISCONNECTED || state == State.UNKNOWN) && scanResults != null) {
                int numOpenNetworks = 0;
                for (int i = scanResults.size() - 1; i >= 0; i--) {
                    ScanResult scanResult = (ScanResult) scanResults.get(i);
                    if (scanResult.capabilities != null && scanResult.capabilities.equals("[ESS]")) {
                        numOpenNetworks++;
                    }
                }
                IWifiManager wifiService = Stub.asInterface(ServiceManager.getService("wifi"));
                int networkId = -1;
                if (wifiService != null) {
                    try {
                        networkId = wifiService.syncGetConnectingNetworkId();
                    } catch (RemoteException e) {
                        Slog.d(TAG, "syncGetConnectingNetworkId failed!");
                    }
                }
                boolean isConnecting = this.mWifiStateMachine.isWifiConnecting(networkId);
                Slog.d(TAG, "Connecting networkId:" + networkId + ", isConnecting:" + isConnecting);
                if (this.mWifiStateMachine.hasCustomizedAutoConnect()) {
                    if (!isConnecting) {
                        if (this.mWaitForScanResult) {
                            showSwitchDialog();
                        }
                    } else {
                        return;
                    }
                }
                Slog.i(TAG, "Open network num:" + numOpenNetworks);
                if (numOpenNetworks > 0) {
                    int i2 = this.mNumScansSinceNetworkStateChange + 1;
                    this.mNumScansSinceNetworkStateChange = i2;
                    if (i2 >= 0) {
                        setNotificationVisible(true, numOpenNetworks, false, 0);
                    }
                }
            }
            setNotificationVisible(false, 0, false, 0);
            return;
        }
        return;
    }

    private synchronized void resetNotification() {
        this.mNotificationRepeatTime = 0;
        this.mNumScansSinceNetworkStateChange = 0;
        setNotificationVisible(false, 0, false, 0);
    }

    private void setNotificationVisible(boolean visible, int numNetworks, boolean force, int delay) {
        if (visible || this.mNotificationShown || force) {
            NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (!visible) {
                Slog.d(TAG, "cancel notification");
                notificationManager.cancelAsUser(null, ICON_NETWORKS_AVAILABLE, UserHandle.ALL);
            } else if (System.currentTimeMillis() >= this.mNotificationRepeatTime) {
                if (this.mNotificationBuilder == null) {
                    this.mNotificationBuilder = new Builder(this.mContext).setWhen(0).setSmallIcon(ICON_NETWORKS_AVAILABLE).setAutoCancel(true).setContentIntent(TaskStackBuilder.create(this.mContext).addNextIntentWithParentStack(new Intent("android.net.wifi.PICK_WIFI_NETWORK")).getPendingIntent(0, 0, null, UserHandle.CURRENT)).setColor(this.mContext.getResources().getColor(17170523));
                }
                CharSequence title = this.mContext.getResources().getQuantityText(18087959, numNetworks);
                CharSequence details = this.mContext.getResources().getQuantityText(18087960, numNetworks);
                this.mNotificationBuilder.setTicker(title);
                this.mNotificationBuilder.setContentTitle(title);
                this.mNotificationBuilder.setContentText(details);
                this.mNotificationRepeatTime = System.currentTimeMillis() + this.NOTIFICATION_REPEAT_DELAY_MS;
                if (this.mNotificationShown) {
                    this.mNotificationBuilder.setSound(null);
                } else {
                    this.mNotificationBuilder.setSound(RingtoneManager.getActualDefaultRingtoneUri(this.mContext, 2));
                }
                Slog.d(TAG, "Pop up notification, mNotificationBuilder.setSound");
                notificationManager.notifyAsUser(null, ICON_NETWORKS_AVAILABLE, this.mNotificationBuilder.build(), UserHandle.CURRENT);
            } else {
                return;
            }
            this.mNotificationShown = visible;
        }
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mNotificationEnabled " + this.mNotificationEnabled);
        pw.println("mNotificationRepeatTime " + this.mNotificationRepeatTime);
        pw.println("mNotificationShown " + this.mNotificationShown);
        pw.println("mNumScansSinceNetworkStateChange " + this.mNumScansSinceNetworkStateChange);
    }

    private boolean isDataAvailable() {
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
            if (phone == null || !phone.isRadioOn(this.mContext.getPackageName()) || tm == null) {
                return false;
            }
            boolean isSim1Insert = tm.hasIccCard(0);
            boolean isSim2Insert = false;
            if (TelephonyManager.getDefault().getPhoneCount() >= 2) {
                isSim2Insert = tm.hasIccCard(1);
            }
            return isSim1Insert || isSim2Insert;
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to get phone service, error:" + e);
            return false;
        }
    }

    private void showSwitchDialog() {
        this.mWaitForScanResult = false;
        boolean isDataAvailable = isDataAvailable();
        Slog.d(TAG, "showSwitchDialog, isDataAvailable:" + isDataAvailable + ", mShowReselectDialog:" + this.mShowReselectDialog);
        if (!this.mShowReselectDialog && isDataAvailable) {
            Intent intent = new Intent("android.intent.action_WIFI_FAILOVER_GPRS_DIALOG");
            intent.addFlags(67108864);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public void setWaitForScanResult(boolean value) {
        this.mWaitForScanResult = value;
    }
}
