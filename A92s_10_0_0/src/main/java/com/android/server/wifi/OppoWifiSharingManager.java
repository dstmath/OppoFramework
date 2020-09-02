package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import oppo.util.OppoStatistics;

public class OppoWifiSharingManager {
    /* access modifiers changed from: private */
    public static boolean DBG = false;
    private static final String EVENTID_SHARING_STA_CONNECTED = "wifi_sharing_sta_connected";
    private static final String EXTRA_WIFI_TO_DATA = "wifi_to_data";
    private static final String KEY_STA_CONNECTED_SUCCESS = "success";
    private static final String KEY_WIFI_STATISTIC = "wifi_fool_proof";
    private static final int MSG_DISABLE_WIFI = 3;
    private static final int MSG_START_WIFI = 2;
    private static final int MSG_START_WIFI_SHARING = 0;
    private static final int MSG_STOP_WIFI_SHARING = 1;
    private static final String SETTINGS_WIFI_SHARING = "settings_wifi_sharing";
    /* access modifiers changed from: private */
    public static String TAG = "OppoWifiSharingManager";
    private static final int WIFI_SHARING_DISABLED = 1;
    private static final int WIFI_SHARING_ENABLED = 2;
    private static final String WIFI_TO_DATA = "android.net.wifi.WIFI_TO_DATA";
    /* access modifiers changed from: private */
    public AsyncHandler mAsyncHandler;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mIsSharingEnableTriggered = false;
    private boolean mIsWifiApEnabled = false;
    private boolean mIsWifiClosedByUser = true;
    private boolean mIsWifiSharingFunctionOn = true;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.OppoWifiSharingManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (OppoWifiSharingManager.DBG) {
                String access$100 = OppoWifiSharingManager.TAG;
                Log.d(access$100, "OppoWifiSharingManager" + action);
            }
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == 0) {
                    Log.d(OppoWifiSharingManager.TAG, "OppoWifiSharingManager stop wifi sharing when connected to the internet");
                    if (!OppoWifiSharingManager.this.mAsyncHandler.hasMessages(1)) {
                        OppoWifiSharingManager.this.mAsyncHandler.sendEmptyMessage(1);
                    }
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo.DetailedState state = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState();
                String access$1002 = OppoWifiSharingManager.TAG;
                Log.d(access$1002, "wifi connect state = " + state);
                if (OppoWifiSharingManager.this.mIsSharingEnableTriggered || state != NetworkInfo.DetailedState.CONNECTED) {
                    if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                        OppoWifiSharingManager.this.mSharingEnabledDeferNum.set(0);
                        if (!OppoWifiSharingManager.this.mAsyncHandler.hasMessages(1)) {
                            OppoWifiSharingManager.this.mAsyncHandler.sendEmptyMessage(1);
                        }
                    }
                } else if (!OppoWifiSharingManager.this.mAsyncHandler.hasMessages(0)) {
                    OppoWifiSharingManager.this.mAsyncHandler.sendEmptyMessage(0);
                }
            } else if (OppoWifiSharingManager.WIFI_TO_DATA.equals(action)) {
                boolean isdata = intent.getBooleanExtra(OppoWifiSharingManager.EXTRA_WIFI_TO_DATA, false);
                String access$1003 = OppoWifiSharingManager.TAG;
                Log.d(access$1003, "wifi WIFI_TO_DATA state " + isdata);
                if (!isdata && OppoWifiSharingManager.this.mWifiInjector.getClientModeImpl().isConnected() && !OppoWifiSharingManager.this.mAsyncHandler.hasMessages(0)) {
                    OppoWifiSharingManager.this.mAsyncHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
                String access$1004 = OppoWifiSharingManager.TAG;
                Log.d(access$1004, "wifi ACTION_AIRPLANE_MODE_CHANGED  airplaneModeOn = " + isAirplaneModeOn);
                if (isAirplaneModeOn) {
                    OppoWifiSharingManager.this.setWifiClosedByUser(true);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final AtomicInteger mSharingEnabledDeferNum = new AtomicInteger();
    private int mWifiApState = 11;
    /* access modifiers changed from: private */
    public WifiInjector mWifiInjector;
    private final AtomicInteger mWifiTetheringType = new AtomicInteger(0);

    public OppoWifiSharingManager(Context context, WifiInjector wifiInjector) {
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        String str = TAG;
        Log.d(str, "wifi isWifiSharingFunctionOn = " + this.mIsWifiSharingFunctionOn);
        HandlerThread ht = new HandlerThread("WifiSharingThread");
        ht.start();
        this.mAsyncHandler = new AsyncHandler(ht.getLooper());
        registerForBroadcasts();
    }

    /* access modifiers changed from: private */
    public final class AsyncHandler extends Handler {
        AsyncHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                OppoWifiSharingManager.this.startWifiSharingTetheringIfNeed();
            } else if (i == 1) {
                OppoWifiSharingManager.this.stopWifiSharingTetheringIfNeed();
            } else if (i == 2) {
                OppoWifiSharingManager.this.setWifiEnabled(true);
            } else if (i == 3) {
                OppoWifiSharingManager.this.setWifiEnabled(false);
            }
        }
    }

    private void registerForBroadcasts() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        filter.addAction(WIFI_TO_DATA);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public void setWifiSharingConfiguration(WifiConfiguration config) {
        this.mWifiInjector.getWifiApConfigStore().setSharingConfiguration(config);
    }

    public WifiConfiguration syncGetWifiSharingConfiguration() {
        return this.mWifiInjector.getWifiApConfigStore().getSharingConfiguration();
    }

    private void setWifiSharingTetheringEnable(boolean enabled) {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (enabled) {
            Log.d(TAG, "ConnectedState start wifisharing ");
            cm.startTethering(4, false, new ConnectivityManager.OnStartTetheringCallback() {
                /* class com.android.server.wifi.OppoWifiSharingManager.AnonymousClass2 */

                public void onTetheringStarted() {
                    Log.d(OppoWifiSharingManager.TAG, "onTetheringStarted ");
                    boolean unused = OppoWifiSharingManager.this.mIsSharingEnableTriggered = true;
                }

                public void onTetheringFailed() {
                    Log.d(OppoWifiSharingManager.TAG, "onTetheringFailed ");
                    boolean unused = OppoWifiSharingManager.this.mIsSharingEnableTriggered = true;
                }
            });
            return;
        }
        Log.d(TAG, "ConnectedState stop wifisharing");
        cm.stopTethering(4);
    }

    public boolean isWifiSharingTethering() {
        return this.mWifiTetheringType.get() == 4;
    }

    public int transformApState2SharingState(int wifiApState) {
        return wifiApState + 100;
    }

    public void setWifiTetheringType(int wifiTetheringType) {
        if (wifiTetheringType == 4) {
            this.mSharingEnabledDeferNum.incrementAndGet();
        }
        this.mWifiTetheringType.set(wifiTetheringType);
    }

    public void updateWifiSharingPersistState(int state) {
        if (isWifiSharingSupported()) {
            this.mWifiApState = state;
            this.mWifiInjector.getWifiSettingsStore();
            if (state == 11) {
                setWifiApSavedState(false);
            } else if (state == 13) {
                setWifiApSavedState(true);
            } else if (state == 111) {
                setWifiSharingSavedState(1);
                this.mIsSharingEnableTriggered = false;
                if (this.mSharingEnabledDeferNum.get() == 0) {
                    this.mWifiInjector.getWifiController().setStaSoftApConcurrencyForSharing(false);
                }
            } else if (state == 113) {
                setWifiSharingSavedState(2);
                setSharingEnableCompleted();
            } else if (state == 114) {
                setSharingEnableCompleted();
            }
        }
    }

    public boolean isWifiSharingEnabledState() {
        return getWifiSharingSavedState() == 2;
    }

    public void setWifiSharingSavedState(int state) {
        Settings.Global.putInt(this.mContext.getContentResolver(), SETTINGS_WIFI_SHARING, state);
    }

    public int getWifiSharingSavedState() {
        try {
            return Settings.Global.getInt(this.mContext.getContentResolver(), SETTINGS_WIFI_SHARING);
        } catch (Settings.SettingNotFoundException e) {
            return 1;
        }
    }

    public boolean isWifiApEnabledState() {
        return this.mIsWifiApEnabled;
    }

    public void setWifiApSavedState(boolean enabled) {
        this.mIsWifiApEnabled = enabled;
    }

    public boolean isWifiSharingSupported() {
        return this.mIsWifiSharingFunctionOn;
    }

    /* access modifiers changed from: private */
    public void startWifiSharingTetheringIfNeed() {
        if (DBG) {
            Log.d(TAG, "startWifiSharingTetheringIfNeed ");
        }
        if (this.mWifiApState == 111) {
            int thisPid = Binder.getCallingPid();
            int lastWifiSharingClosedPid = getWifiSharingClosedPid();
            if (lastWifiSharingClosedPid == 0 || lastWifiSharingClosedPid == thisPid) {
                boolean isAirplaneModeOn = this.mWifiInjector.getWifiSettingsStore().isAirplaneModeOn();
                if (isWifiClosedByUser() || isAirplaneModeOn) {
                    String str = TAG;
                    Log.d(str, "if close the wifi by user " + isWifiClosedByUser());
                    return;
                }
                setWifiSharingTetheringEnable(true);
                return;
            }
            String str2 = TAG;
            Log.d(str2, "if close the sharing by user " + lastWifiSharingClosedPid);
        }
    }

    public void stopWifiSharingTetheringIfNeed() {
        if (DBG) {
            Log.d(TAG, "stopWifiSharingTetheringIfNeed");
        }
        int sharingState = this.mWifiApState;
        if (!this.mWifiInjector.getWifiController().getStaSoftApConcurrencyForSharing()) {
            return;
        }
        if (sharingState == 112 || sharingState == 113) {
            if (DBG) {
                Log.d(TAG, "stopWifiSharingTetheringIfNeed stopTethering");
            }
            setWifiSharingTetheringEnable(false);
        }
    }

    /* access modifiers changed from: private */
    public void setWifiEnabled(boolean enabled) {
        if (DBG) {
            String str = TAG;
            Log.d(str, "setWifiEnabled enabled " + enabled);
        }
        getWifiManager().setWifiEnabled(enabled);
    }

    private WifiManager getWifiManager() {
        return (WifiManager) this.mContext.getSystemService("wifi");
    }

    public int getWifiSharingClosedPid() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "settings_wifi_sharing_closed_pid", 0);
    }

    private void setSharingEnableCompleted() {
        this.mSharingEnabledDeferNum.decrementAndGet();
        setWifiClosedByUser(false);
        this.mIsSharingEnableTriggered = false;
    }

    public void setWifiClosedByUser(boolean isUser) {
        if (DBG) {
            String str = TAG;
            Log.d(str, "setWifiClosedByUser isUser " + isUser);
        }
        this.mIsWifiClosedByUser = isUser;
    }

    private boolean isWifiClosedByUser() {
        if (DBG) {
            String str = TAG;
            Log.d(str, "isWifiClosedByUser isUser " + this.mIsWifiClosedByUser);
        }
        return this.mIsWifiClosedByUser;
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }

    public void statsStaConnected() {
        setStatistics(KEY_STA_CONNECTED_SUCCESS, String.valueOf(1), EVENTID_SHARING_STA_CONNECTED);
    }

    private void setStatistics(String mapKey, String mapValue, String eventId) {
        HashMap<String, String> map = new HashMap<>();
        map.put(mapKey, mapValue);
        if (DBG) {
            String str = TAG;
            Log.d(str, "sharing, onCommon eventId = " + eventId);
        }
        OppoStatistics.onCommon(this.mContext, KEY_WIFI_STATISTIC, eventId, map, false);
    }
}
