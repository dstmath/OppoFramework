package com.android.server.net;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.database.ContentObserver;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManagerInternal;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseIntArray;
import com.android.server.ColorAppActionTracker;
import com.android.server.ColorAppDownloadTracker;
import com.android.server.ColorAppLockedTracker;
import com.android.server.IColorNetworkPolicyManagerServiceInner;
import com.android.server.LocalServices;
import com.android.server.OppoBaseNetworkPolicyManagerService;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.IColorFullmodeManager;
import com.android.server.power.OppoPowerManagerInternal;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.Iterator;

public class ColorDozeNetworkOptimization implements IColorDozeNetworkOptimization {
    private static boolean DEBUG_OPPO = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static final String TAG = "ColorDeviceIdleForegroundOptimization";
    Context mContext = null;
    ColorAppActionTracker mDownloadTracker = null;
    ArrayList<Integer> mDownloadUids = new ArrayList<>();
    Handler mHandler = null;
    private boolean mIsExpVersion = false;
    PowerManagerInternal mLocalPowerManager = null;
    ColorAppActionTracker mLockedTracker = null;
    NetworkPolicyManagerService mNetWorkMgr = null;
    volatile boolean mOppoDeviceIdle = true;
    OppoPowerManagerInternal mOppoLocalPowerManager = null;
    ArrayList<ColorAppActionTracker> mTrackers = new ArrayList<>();

    public void initArgs(Context context, NetworkPolicyManagerService networkMgr, Handler handler) {
        this.mNetWorkMgr = networkMgr;
        this.mIsExpVersion = context.getPackageManager().hasSystemFeature("oppo.version.exp");
        this.mHandler = handler;
        this.mContext = context;
        initAppActionTrackers();
        handler.postDelayed(new Runnable() {
            /* class com.android.server.net.ColorDozeNetworkOptimization.AnonymousClass1 */

            public void run() {
                ColorDozeNetworkOptimization.this.startAppActionTrackers();
            }
        }, 60000);
        if (DEBUG_OPPO) {
            Slog.d(TAG, "coloros policy is on the road");
        }
    }

    public ArrayList<ColorAppActionTracker> getAppActionTracker() {
        return this.mTrackers;
    }

    public boolean effective() {
        return isOppoDeviceIdleSwitchOpen();
    }

    public boolean colorUpdateRulesForDeviceIdle(SparseIntArray uidRules, int uid, int uidState, boolean notAllowed) {
        boolean isProcStateAllowedWhileIdleOrPowerSaveMode;
        if (notAllowed) {
            if (DEBUG_OPPO) {
                Slog.d(TAG, "colorUpdateRulesForDeviceIdle, uid " + uid + " is now allowed, uid state = " + uidState);
            }
            uidRules.delete(uid);
            return true;
        }
        boolean isMusicPlayer = false;
        if (uid < 10000 || !isOppoDeviceIdleSwitchOpen()) {
            return false;
        }
        boolean locked = isLockedWhitelist(UserHandle.getAppId(uid));
        if (locked) {
            isProcStateAllowedWhileIdleOrPowerSaveMode = NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState);
        } else {
            isProcStateAllowedWhileIdleOrPowerSaveMode = isUidTopOnRestrictPowerUL(uidState);
        }
        boolean isUidDownload = this.mDownloadUids.contains(Integer.valueOf(UserHandle.getAppId(uid)));
        OppoPowerManagerInternal oppoPowerManagerInternal = this.mOppoLocalPowerManager;
        if (oppoPowerManagerInternal != null) {
            isMusicPlayer = oppoPowerManagerInternal.getMusicPlayerList().contains(Integer.valueOf(uid));
        }
        if (DEBUG_OPPO) {
            if (!isProcStateAllowedWhileIdleOrPowerSaveMode && NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState)) {
                Slog.d(TAG, "colorUpdateRulesForDeviceIdle , uid = " + uid + ", uidState = " + uidState + "procAllow = " + isProcStateAllowedWhileIdleOrPowerSaveMode + ", lockApp " + locked);
            } else if (!isProcStateAllowedWhileIdleOrPowerSaveMode && (isUidDownload || isMusicPlayer)) {
                Slog.d(TAG, "colorUpdateRulesForDeviceIdle , uid = " + uid + ", uidState = " + uidState + ", isUidDownload = " + isUidDownload + ", isMusicPlayer = " + isMusicPlayer);
            }
        }
        if (isProcStateAllowedWhileIdleOrPowerSaveMode || isUidDownload || isMusicPlayer) {
            uidRules.put(uid, 1);
        }
        return true;
    }

    public boolean colorUpdateWhitelist(boolean deviceIdle, int appId, boolean oldValue) {
        if (!deviceIdle || !isOppoDeviceIdleSwitchOpen()) {
            return oldValue;
        }
        OppoPowerManagerInternal oppoPowerManagerInternal = this.mOppoLocalPowerManager;
        boolean z = false;
        boolean isMusicPlayer = oppoPowerManagerInternal != null ? oppoPowerManagerInternal.getMusicPlayerList().contains(Integer.valueOf(appId)) : false;
        if (DEBUG_OPPO && (isMusicPlayer || this.mDownloadUids.contains(Integer.valueOf(appId)))) {
            Slog.d(TAG, "colorUpdateWhitelist change whilist, appId = " + appId + ", oldValue = " + oldValue + ", downloadState = " + this.mDownloadUids.contains(Integer.valueOf(appId)) + ", playState = " + isMusicPlayer);
        }
        if (this.mDownloadUids.contains(Integer.valueOf(appId)) || isMusicPlayer) {
            z = true;
        }
        return oldValue | z;
    }

    public boolean colorUpdateRulesForWhitelistedPowerSave(int uid, int chain, int uidState, boolean isWhitelisted, boolean notAllowed) {
        boolean foreground;
        if (chain != 1) {
            return false;
        }
        if (notAllowed) {
            if (DEBUG_OPPO) {
                Slog.d(TAG, "colorUpdateRulesForWhitelistedPowerSave uid " + uid + " is now allowed, set default rule, uidState = " + uidState + ", isWhitelisted = " + isWhitelisted);
            }
            getInner().setUidFirewallRule(chain, uid, 0);
            return true;
        } else if (uid < 10000) {
            return false;
        } else {
            if (isOppoDeviceIdleSwitchOpen()) {
                boolean locked = isLockedWhitelist(UserHandle.getAppId(uid));
                if (locked) {
                    foreground = NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState);
                } else {
                    foreground = isUidTopOnRestrictPowerUL(uidState);
                }
                if (DEBUG_OPPO && !foreground && NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState)) {
                    Slog.d(TAG, "colorUpdateRulesForWhitelistedPowerSave uid " + uid + " got foreground state " + foreground + ", uidState = " + uidState + ", lockState = " + locked);
                }
            } else {
                foreground = NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState);
            }
            if (isWhitelisted || foreground) {
                getInner().setUidFirewallRule(chain, uid, 1);
            } else {
                getInner().setUidFirewallRule(chain, uid, 0);
            }
            return true;
        }
    }

    public boolean colorGetStateForPowerRestrictions(boolean deviceIdle, int uid, int uidState, boolean notAllowed) {
        boolean isForeground;
        if (uid < 10000) {
            return NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState);
        }
        if (!deviceIdle || (!isOppoDeviceIdleSwitchOpen() && !notAllowed)) {
            return NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState);
        }
        boolean locked = isLockedWhitelist(UserHandle.getAppId(uid));
        if (locked) {
            isForeground = NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState);
        } else {
            isForeground = isUidTopOnRestrictPowerUL(uidState);
        }
        if (!DEBUG_OPPO || isForeground || !NetworkPolicyManager.isProcStateAllowedWhileIdleOrPowerSaveMode(uidState)) {
            return isForeground;
        }
        Slog.d(TAG, "colorGetStateForPowerRestrictions uid " + uid + " got foreground state " + isForeground + ", uidState = " + uidState + ", lockState = " + locked);
        return isForeground;
    }

    /* access modifiers changed from: package-private */
    public void initAppActionTrackers() {
        this.mDownloadTracker = ColorAppDownloadTracker.getInstance(this.mContext);
        if (!this.mIsExpVersion) {
            this.mDownloadTracker.setCallback(new ColorAppActionTracker.AppActionCallback() {
                /* class com.android.server.net.ColorDozeNetworkOptimization.AnonymousClass2 */

                public void updateAppActionChange() {
                    ColorDozeNetworkOptimization.this.mHandler.post(new Runnable() {
                        /* class com.android.server.net.ColorDozeNetworkOptimization.AnonymousClass2.AnonymousClass1 */

                        public void run() {
                            if (ColorDozeNetworkOptimization.this.isOppoDeviceIdleSwitchOpen()) {
                                ColorDozeNetworkOptimization.this.updateTrackDownloadWhiteList();
                            }
                        }
                    });
                }
            });
        }
        this.mTrackers.add(this.mDownloadTracker);
        this.mLockedTracker = new ColorAppLockedTracker(this.mContext);
        this.mTrackers.add(this.mLockedTracker);
        boolean z = true;
        if (1 != Settings.Global.getInt(this.mContext.getContentResolver(), "oppo_device_idle", 1)) {
            z = false;
        }
        this.mOppoDeviceIdle = z;
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("oppo_device_idle"), false, new DeviceIdleSettingsObserver(this.mHandler), -1);
    }

    /* access modifiers changed from: private */
    public class DeviceIdleSettingsObserver extends ContentObserver {
        public DeviceIdleSettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            boolean value = true;
            if (1 != Settings.Global.getInt(ColorDozeNetworkOptimization.this.mContext.getContentResolver(), "oppo_device_idle", 1)) {
                value = false;
            }
            ColorDozeNetworkOptimization colorDozeNetworkOptimization = ColorDozeNetworkOptimization.this;
            colorDozeNetworkOptimization.mOppoDeviceIdle = value;
            if (value) {
                colorDozeNetworkOptimization.startAppActionTrackers();
            } else {
                colorDozeNetworkOptimization.stopAppActionTrackers();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startAppActionTrackers() {
        if (DEBUG_OPPO) {
            Slog.d(TAG, "start coloros policy");
        }
        if (this.mLocalPowerManager == null) {
            this.mLocalPowerManager = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        }
        if (this.mOppoLocalPowerManager == null) {
            this.mOppoLocalPowerManager = (OppoPowerManagerInternal) LocalServices.getService(OppoPowerManagerInternal.class);
        }
        this.mDownloadTracker.start();
        this.mLockedTracker.start();
    }

    /* access modifiers changed from: package-private */
    public void stopAppActionTrackers() {
        if (DEBUG_OPPO) {
            Slog.d(TAG, "stop coloros policy");
        }
        this.mDownloadTracker.stop();
        this.mLockedTracker.stop();
    }

    /* access modifiers changed from: package-private */
    public void updateTrackDownloadWhiteList() {
        ArrayMap<Integer, Integer> downloadUidStats = (ArrayMap) this.mDownloadTracker.getTrackWhiteList(true);
        ArrayList<Integer> newDownloadUids = new ArrayList<>();
        for (int i = 0; i < downloadUidStats.size(); i++) {
            if (downloadUidStats.valueAt(i).intValue() == 1) {
                newDownloadUids.add(downloadUidStats.keyAt(i));
            }
        }
        if (DEBUG_OPPO) {
            Iterator<Integer> it = newDownloadUids.iterator();
            while (it.hasNext()) {
                Slog.d(TAG, "updateTrackDownloadWhiteList, uid = " + it.next());
            }
        }
        synchronized (this.mNetWorkMgr.mUidRulesFirstLock) {
            this.mDownloadUids.clear();
            this.mDownloadUids.addAll(newDownloadUids);
            if (this.mNetWorkMgr.mDeviceIdleMode) {
                Trace.traceBegin(2097152, "updateRulesForRestrictPowerUL");
                try {
                    this.mNetWorkMgr.updateRulesForDeviceIdleUL();
                    getInner().updateRulesForAllAppsUL();
                } finally {
                    Trace.traceEnd(2097152);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isOppoDeviceIdleSwitchOpen() {
        return !this.mIsExpVersion && this.mOppoDeviceIdle && !OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
    }

    private boolean isUidTopOnRestrictPowerUL(int uidState) {
        return uidState <= 2;
    }

    private boolean isLockedWhitelist(int appid) {
        return this.mLockedTracker.isWhiteList(appid);
    }

    private static OppoBaseNetworkPolicyManagerService typeCasting(NetworkPolicyManagerService npms) {
        if (npms != null) {
            return (OppoBaseNetworkPolicyManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseNetworkPolicyManagerService.class, npms);
        }
        return null;
    }

    private IColorNetworkPolicyManagerServiceInner getInner() {
        OppoBaseNetworkPolicyManagerService base = typeCasting(this.mNetWorkMgr);
        if (base == null || base.mColorNPMSInner == null) {
            return IColorNetworkPolicyManagerServiceInner.DEFAULT;
        }
        return base.mColorNPMSInner;
    }
}
