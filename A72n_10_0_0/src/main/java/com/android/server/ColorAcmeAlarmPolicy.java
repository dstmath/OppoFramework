package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import com.android.server.display.ai.utils.ColorAILog;

public class ColorAcmeAlarmPolicy {
    private static final int NETWORK_STATUS_CONNECTED = 1;
    private static final int NETWORK_STATUS_DISCONNECTED = 0;
    private static final int NETWORK_STATUS_UNKNOWN = -1;
    private static final String TAG = "ColorAcmeAlarmPolicy";
    private final boolean ADBG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private AlarmScene mAcmeAlarmScene = null;
    private ConnectivityService mConnectivityService;
    private int mNetworkStatus = -1;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.ColorAcmeAlarmPolicy.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                ColorAcmeAlarmPolicy.this.updateConnectivityState(intent);
            }
        }
    };
    private IntentFilter mRestrictFilter = null;
    ArrayMap<String, Boolean> mSystemPackages = new ArrayMap<>();
    private boolean mWorking = false;

    public void init(Context ctx, Handler handler) {
        this.mAcmeAlarmScene = new AlarmScene();
        this.mRestrictFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    }

    public void triggerAlignTickEvent(Context ctx, long nowElapsed, long screenOffElapsed) {
        long j = nowElapsed - screenOffElapsed;
        if (nowElapsed - screenOffElapsed >= ColorAlarmManagerHelper.getInstance().getAcmeScreenOffTime() * 60 * 1000) {
            this.mAcmeAlarmScene.updateFlags(1, this.mNetworkStatus == 0);
        }
    }

    public void triggerScreenOff(Context ctx) {
        if (!this.mWorking) {
            updateConnectivityState(null);
            ctx.registerReceiver(this.mReceiver, this.mRestrictFilter);
            this.mWorking = true;
        }
    }

    public void triggerScreenOn(Context ctx) {
        boolean z = false;
        if (this.mWorking) {
            ctx.unregisterReceiver(this.mReceiver);
            this.mWorking = false;
        }
        this.mNetworkStatus = -1;
        AlarmScene alarmScene = this.mAcmeAlarmScene;
        if (this.mNetworkStatus == 0) {
            z = true;
        }
        alarmScene.updateFlags(1, z);
    }

    public boolean isRestrictedByAcme(Context context, AlarmManagerService.Alarm alarm, String pkgName, boolean normalAlignWhitelist, boolean enforceAlignWhitelist) {
        return this.mAcmeAlarmScene.matchAlarm(context, alarm, pkgName, normalAlignWhitelist, enforceAlignWhitelist);
    }

    public boolean isInAcmeState() {
        return this.mAcmeAlarmScene.isNetworkRestrict();
    }

    private class AlarmScene {
        public static final int SCENE_NETWORK_OFF = 1;
        private int flags;

        private AlarmScene() {
            this.flags = 0;
        }

        /* access modifiers changed from: package-private */
        public boolean matchAlarm(Context context, AlarmManagerService.Alarm alarm, String packageName, boolean normalAlignWhitelist, boolean enforceAlignWhitelist) {
            if (alarm.alarmClock != null) {
                return false;
            }
            if (!((alarm.flags & 1) != 0)) {
                return true;
            }
            int uid = alarm.uid;
            String tag = getAlarmTag(alarm);
            if (uid < 10000) {
                boolean black = isBlackList(packageName, tag);
                if (ColorAcmeAlarmPolicy.this.ADBG && black) {
                    Slog.d(ColorAcmeAlarmPolicy.TAG, "core system but in black list " + alarm);
                }
                return black;
            } else if (enforceAlignWhitelist) {
                return false;
            } else {
                if (!normalAlignWhitelist && !ColorAlarmManagerHelper.getInstance().inPackageNameWhiteList(packageName) && !ColorAlarmManagerHelper.getInstance().inUidWhiteList(uid)) {
                    if (ColorAcmeAlarmPolicy.this.ADBG) {
                        Slog.d(ColorAcmeAlarmPolicy.TAG, "not normal whitelist , match = true");
                    }
                    return true;
                } else if (ColorAlarmManagerHelper.getInstance().isAcmeBlackWord(packageName) || isBlackList(packageName, tag)) {
                    if (ColorAcmeAlarmPolicy.this.ADBG) {
                        Slog.d(ColorAcmeAlarmPolicy.TAG, "black or network app , match = true");
                    }
                    return true;
                } else {
                    Boolean system = ColorAcmeAlarmPolicy.this.mSystemPackages.get(packageName);
                    if (system == null) {
                        try {
                            system = Boolean.valueOf(context.getPackageManager().getApplicationInfo(packageName, 1048576) != null);
                        } catch (Exception e) {
                            system = false;
                        }
                        ColorAcmeAlarmPolicy.this.mSystemPackages.put(packageName, system);
                    }
                    if (ColorAcmeAlarmPolicy.this.ADBG && !system.booleanValue()) {
                        Slog.d(ColorAcmeAlarmPolicy.TAG, "normal whitelist , match = true");
                    }
                    return !system.booleanValue();
                }
            }
        }

        private String getAlarmTag(AlarmManagerService.Alarm alarm) {
            String tag;
            if (alarm.operation == null) {
                tag = alarm.listenerTag;
            } else {
                tag = alarm.operation.getTag("");
            }
            if (tag == null) {
                return "null";
            }
            return tag;
        }

        public boolean isBlackList(String pkg, String tag) {
            Integer config = ColorAlarmManagerHelper.getInstance().getAcmeBlackConfig(pkg, tag);
            int acmeConfig = config != null ? config.intValue() : 0;
            if (acmeConfig == 0 || (acmeConfig & 1) == 0) {
                return false;
            }
            return true;
        }

        public boolean updateFlags(int flag, boolean add) {
            boolean exist = (this.flags & flag) != 0;
            if (add) {
                this.flags |= flag;
            } else {
                this.flags &= ~flag;
            }
            if (ColorAcmeAlarmPolicy.this.ADBG) {
                StringBuilder sb = new StringBuilder();
                sb.append(add ? "add" : "remove");
                sb.append(" flag ");
                sb.append(flag);
                sb.append(", now flags = ");
                sb.append(this.flags);
                Slog.d(ColorAcmeAlarmPolicy.TAG, sb.toString());
            }
            return add != exist;
        }

        public boolean isNetworkRestrict() {
            return (this.flags & 1) != 0;
        }
    }

    private ConnectivityService getConnectivityService() {
        if (this.mConnectivityService == null) {
            this.mConnectivityService = ServiceManager.getService("connectivity");
        }
        return this.mConnectivityService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConnectivityState(Intent connIntent) {
        ConnectivityService cm;
        boolean conn;
        synchronized (this) {
            cm = getConnectivityService();
        }
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            synchronized (this) {
                int status = 1;
                if (ni == null) {
                    conn = false;
                } else if (connIntent == null) {
                    conn = ni.isConnected();
                } else {
                    if (ni.getType() == connIntent.getIntExtra("networkType", -1)) {
                        conn = !connIntent.getBooleanExtra("noConnectivity", false);
                    } else {
                        return;
                    }
                }
                if (!conn) {
                    status = 0;
                }
                if (status != this.mNetworkStatus) {
                    Slog.d(TAG, "network change from " + this.mNetworkStatus + " to " + status);
                    this.mNetworkStatus = status;
                }
            }
        }
    }
}
