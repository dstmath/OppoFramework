package com.android.server;

import android.common.OppoFeatureCache;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.INetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.notification.ZenModeHelper;
import com.android.server.theia.NoFocusWindow;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public abstract class OppoBaseNetworkPolicyManagerService extends INetworkPolicyManager.Stub {
    public static final String ACTION_MULTI_PACKAGE_ADDED = "oppo.intent.action.MULTI_APP_PACKAGE_ADDED";
    public static final String ACTION_MULTI_PACKAGE_REMOVED = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED";
    public static final String EXTRA_ALERT_TYPE = "extra_alert_type";
    public static final String EXTRA_ALERT_TYPE_DAY = "extra_alert_type_day";
    public static final String EXTRA_ALERT_TYPE_MONTH = "extra_alert_type_month";
    public static final int INVALID_UID = -1;
    public static final String KEY_DAILY_ALERT = "oppo_comm_simsettings_daily_alert_";
    public static final String KEY_DAILY_ALERT_CHANGE = "oppo_comm_simsettings_daily_alert_change";
    public static final String KEY_DAILY_ALERT_SNOOZE = "oppo_comm_simsettings_daily_alert_snooze_";
    private static final boolean LOGD = Log.isLoggable(TAG, 3);
    private static final boolean LOGV = Log.isLoggable(TAG, 2);
    public static final int MONITOR_MOBILE_DATA_CHANGE_DELAYED = 300;
    public static final int MSG_MONITOR_MOBILE_DATA_CHANGE = 1000;
    public static final int MSG_SET_MOBILE_DATA_ENABLED = 1001;
    public static final int NOTIFICATION_LIMIT = 1;
    public static final long NOT_DAILY_ALERT = -1;
    public static final int NOT_NOTIFICATION_LIMIT = 0;
    static final String TAG = "NetworkPolicy";
    public static final int TYPE_ALERT = 1;
    public static final int TYPE_ENABLED = 0;
    public IColorNetworkPolicyManagerServiceInner mColorNPMSInner;
    private final Context mContext;
    protected boolean mGameSpaceMode = false;
    protected BroadcastReceiver mMultiPackageChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.OppoBaseNetworkPolicyManagerService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            int uid = intent.getIntExtra("android.intent.extra.UID", -1);
            String action = intent.getAction();
            if (OppoBaseNetworkPolicyManagerService.LOGV) {
                Slog.v(OppoBaseNetworkPolicyManagerService.TAG, "mMultiPackageChangeReceiver uid:" + uid + " ,action:" + action);
            }
            OppoBaseNetworkPolicyManagerService.this.setCloneUidPolicyNL(uid);
        }
    };
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    public final ArraySet<NetworkTemplate> mOverDailyAlertNotified = new ArraySet<>();
    protected BroadcastReceiver mSimStateChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.OppoBaseNetworkPolicyManagerService.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getStringExtra(IColorAppStartupManager.TYPE_START_SERVICE), "LOADED")) {
                OppoBaseNetworkPolicyManagerService.this.updateIccId(context);
            }
        }
    };
    public boolean mSnoozedDailyAlert = false;
    public HashMap<Integer, String> mSubidToIccId = new HashMap<>();

    public abstract void addUidPolicy(int i, int i2);

    public abstract int getUidPolicy(int i);

    public abstract void removeUidPolicy(int i, int i2);

    public abstract void setUidPolicy(int i, int i2);

    public abstract void snoozeLimit(NetworkTemplate networkTemplate);

    public OppoBaseNetworkPolicyManagerService(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void dumpGameSpaceNetPolicy(IndentingPrintWriter fout) {
        fout.println("dumpGSNetPolicy start");
        fout.println("Status for appID with rules: ");
        fout.increaseIndent();
        boolean gameSpace = OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).isGameSpaceMode();
        fout.print("Status for gs mode: ");
        fout.print(gameSpace);
        fout.println();
        List<Integer> appIdWhiteList = OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).getNetWhiteAppIdlist();
        if (appIdWhiteList != null) {
            int size = appIdWhiteList.size();
            for (int i = 0; i < size; i++) {
                int appid = appIdWhiteList.get(i).intValue();
                fout.print("appID=");
                fout.print(appid);
                fout.println();
            }
        }
        fout.print("appID=");
        fout.print(OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).getDefaultInputMethodAppId());
        fout.println();
        fout.decreaseIndent();
        fout.println("dumpGSNetPolicy end");
    }

    /* access modifiers changed from: protected */
    public int getCloneAppUidNL(int uid) {
        int cloneAppUid = UserHandle.getUid(ZenModeHelper.OPPO_MULTI_USER_ID, UserHandle.getAppId(uid));
        if (LOGD) {
            Slog.v(TAG, "getCloneAppUidNL:" + cloneAppUid);
        }
        return cloneAppUid;
    }

    /* access modifiers changed from: protected */
    public boolean isCloneUidNL(int uid) {
        if (uid != -1) {
            boolean isCloneUid = false;
            if (UserHandle.getUserId(uid) == 999) {
                isCloneUid = true;
            }
            if (LOGD) {
                Slog.v(TAG, "isCloneUid:" + isCloneUid);
            }
            return isCloneUid;
        } else if (!LOGD) {
            return false;
        } else {
            Slog.v(TAG, "isCloneUid uid is invalid");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void setCloneUidPolicyNL(int uid) {
        setUidPolicy(getCloneAppUidNL(uid), getUidPolicy(uid));
    }

    /* access modifiers changed from: protected */
    public void addCloneUidPolicyNL(int uid) {
        addUidPolicy(getCloneAppUidNL(uid), getUidPolicy(uid));
    }

    /* access modifiers changed from: protected */
    public void removeCloneUidPolicyNL(int uid) {
        removeUidPolicy(getCloneAppUidNL(uid), getUidPolicy(uid));
    }

    /* access modifiers changed from: protected */
    public void onMoBileDataChange(Context context, boolean selfChange, ArraySet<NetworkTemplate> overLimitNotified) {
        int subId;
        int subId2;
        if (((TelephonyManager) context.getSystemService(TelephonyManager.class)).isDataEnabled()) {
            if (!(overLimitNotified == null || overLimitNotified.size() <= 0 || (subId2 = getDefaultDataSubscriptionId(context)) == -1)) {
                for (int i = 0; i < overLimitNotified.size(); i++) {
                    NetworkTemplate template = overLimitNotified.valueAt(i);
                    if (subId2 == findSubIdNL(template)) {
                        snoozeLimit(template);
                    }
                }
            }
            ArraySet<NetworkTemplate> arraySet = this.mOverDailyAlertNotified;
            if (!(arraySet == null || arraySet.size() <= 0 || (subId = getDefaultDataSubscriptionId(context)) == -1)) {
                for (int i2 = 0; i2 < this.mOverDailyAlertNotified.size(); i2++) {
                    if (subId == findSubIdNL(this.mOverDailyAlertNotified.valueAt(i2))) {
                        Settings.Global.putLong(context.getContentResolver(), KEY_DAILY_ALERT_SNOOZE + this.mSubidToIccId.get(Integer.valueOf(subId)), System.currentTimeMillis());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setMobileDataEnabledInner(Context context, NetworkTemplate template, boolean enabled) {
        if (template.getMatchRule() == 1) {
            setMobileDataNL(enabled);
        }
    }

    /* access modifiers changed from: protected */
    public int getDefaultDataSubscriptionId(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "multi_sim_data_call", -1);
    }

    /* access modifiers changed from: protected */
    public void setMobileDataNL(boolean enabled) {
    }

    /* access modifiers changed from: protected */
    public int findSubIdNL(NetworkTemplate template) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void updateIccId(Context context) {
        SubscriptionManager sm = (SubscriptionManager) context.getSystemService(SubscriptionManager.class);
        int[] subIds = ArrayUtils.defeatNullable(sm.getActiveSubscriptionIdList());
        new SparseArray(subIds.length);
        this.mSubidToIccId.clear();
        for (int subId : subIds) {
            SubscriptionInfo subInfo = sm.getActiveSubscriptionInfo(subId);
            if (subInfo != null) {
                this.mSubidToIccId.put(Integer.valueOf(subId), subInfo.getIccId());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateNotificationsForDailyAlert(Context context, NetworkTemplate template, int subId) {
        long numDailyAlert;
        String lastSixOfIccid;
        String iccid = this.mSubidToIccId.get(Integer.valueOf(subId));
        long numDailyAlert2 = Settings.Global.getLong(context.getContentResolver(), KEY_DAILY_ALERT + iccid, -1);
        if (TextUtils.isEmpty(iccid)) {
            numDailyAlert = -1;
        } else {
            numDailyAlert = numDailyAlert2;
        }
        if (numDailyAlert != -1) {
            long dailyStart = getTodayStart();
            long dailyEnd = System.currentTimeMillis();
            long dailyTotalBytes = getTotalBytesNL(template, dailyStart, dailyEnd);
            if (dailyTotalBytes > numDailyAlert) {
                StringBuilder sb = new StringBuilder();
                sb.append(KEY_DAILY_ALERT_SNOOZE);
                sb.append(iccid);
                this.mSnoozedDailyAlert = Settings.Global.getLong(context.getContentResolver(), sb.toString(), dailyStart) > dailyStart;
                if (!this.mSnoozedDailyAlert) {
                    int iccidLength = iccid == null ? 0 : iccid.length();
                    if (iccidLength > 6) {
                        lastSixOfIccid = iccid.substring(iccidLength - 6);
                    } else {
                        lastSixOfIccid = "";
                    }
                    Log.d(TAG, "lastSixOfIccid: " + lastSixOfIccid + " numDailyAlert: " + numDailyAlert + " dailyStart: " + dailyStart + " dailyEnd: " + dailyEnd + " dailyTotalBytes: " + dailyTotalBytes);
                    notifyDailyAlertNL(context, template);
                    return;
                }
                return;
            }
            notifyUnderDailyAlertNL(template);
            return;
        }
        notifyUnderDailyAlertNL(template);
    }

    /* access modifiers changed from: protected */
    public void updateNetworkEnabledForDailyAlert(Context context, NetworkTemplate template, int subId) {
        long numDailyAlert;
        String iccid = this.mSubidToIccId.get(Integer.valueOf(subId));
        long numDailyAlert2 = Settings.Global.getLong(context.getContentResolver(), KEY_DAILY_ALERT + iccid, -1);
        if (TextUtils.isEmpty(iccid)) {
            numDailyAlert = -1;
        } else {
            numDailyAlert = numDailyAlert2;
        }
        boolean snoozedThisCycle = true;
        if (numDailyAlert != -1) {
            long dailyStart = getTodayStart();
            if (getTotalBytesNL(template, dailyStart, System.currentTimeMillis()) > numDailyAlert) {
                if (Settings.Global.getLong(context.getContentResolver(), KEY_DAILY_ALERT_SNOOZE + iccid, dailyStart) <= dailyStart) {
                    snoozedThisCycle = false;
                }
                if (!snoozedThisCycle) {
                    setMobileDataEnabledNL(template, false);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateNotificationAndNetworkEnableForDailyAlert(Context context, NetworkTemplate template, int subId, boolean updateNetworkEnable) {
        long numDailyAlert;
        String lastSixOfIccid;
        String iccid = this.mSubidToIccId.get(Integer.valueOf(subId));
        long numDailyAlert2 = Settings.Global.getLong(context.getContentResolver(), KEY_DAILY_ALERT + iccid, -1);
        if (TextUtils.isEmpty(iccid)) {
            numDailyAlert = -1;
        } else {
            numDailyAlert = numDailyAlert2;
        }
        if (numDailyAlert != -1) {
            long dailyStart = getTodayStart();
            long dailyEnd = System.currentTimeMillis();
            long dailyTotalBytes = getTotalBytesNL(template, dailyStart, dailyEnd);
            if (dailyTotalBytes > numDailyAlert) {
                StringBuilder sb = new StringBuilder();
                sb.append(KEY_DAILY_ALERT_SNOOZE);
                sb.append(iccid);
                this.mSnoozedDailyAlert = Settings.Global.getLong(context.getContentResolver(), sb.toString(), dailyStart) > dailyStart;
                if (!this.mSnoozedDailyAlert) {
                    int iccidLength = iccid == null ? 0 : iccid.length();
                    if (iccidLength > 6) {
                        lastSixOfIccid = iccid.substring(iccidLength - 6);
                    } else {
                        lastSixOfIccid = "";
                    }
                    Log.d(TAG, "lastSixOfIccid: " + lastSixOfIccid + " numDailyAlert: " + numDailyAlert + " dailyStart: " + dailyStart + " dailyEnd: " + dailyEnd + " dailyTotalBytes: " + dailyTotalBytes);
                    if (updateNetworkEnable) {
                        setMobileDataEnabledNL(template, false);
                    }
                    notifyDailyAlertNL(context, template);
                    return;
                }
                return;
            }
            notifyUnderDailyAlertNL(template);
            return;
        }
        notifyUnderDailyAlertNL(template);
    }

    /* access modifiers changed from: protected */
    public long getTodayStart() {
        Date date = new Date();
        return new Date(date.getYear(), date.getMonth(), date.getDate()).getTime();
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    public void notifyDailyAlertNL(Context context, NetworkTemplate template) {
        if (!this.mOverDailyAlertNotified.contains(template)) {
            try {
                context.startActivityAsUser(buildNetworkDailyAlertIntent(context.getResources(), template), UserHandle.CURRENT);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, e.toString());
            }
            this.mOverDailyAlertNotified.add(template);
        }
    }

    protected static Intent buildNetworkDailyAlertIntent(Resources res, NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString(res.getString(17039760)));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", (Parcelable) template);
        intent.putExtra(EXTRA_ALERT_TYPE, EXTRA_ALERT_TYPE_DAY);
        return intent;
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mNetworkPoliciesSecondLock"})
    public void notifyUnderDailyAlertNL(NetworkTemplate template) {
        this.mOverDailyAlertNotified.remove(template);
    }

    /* access modifiers changed from: protected */
    public long getTotalBytesNL(NetworkTemplate template, long start, long end) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void setMobileDataEnabledNL(NetworkTemplate template, boolean enabled) {
    }

    /* access modifiers changed from: protected */
    public boolean getLimitNotification(Context context, int subId) {
        HashMap<Integer, String> hashMap = this.mSubidToIccId;
        if (hashMap == null) {
            return false;
        }
        int numLimitNotificaion = Settings.Global.getInt(context.getContentResolver(), "data_limit_notification_bool" + hashMap.get(Integer.valueOf(subId)), 0);
        if (LOGD) {
            Slog.v(TAG, "getLimitNotification subId:" + subId + " ,numLimitNotificaion:" + numLimitNotificaion);
        }
        if (numLimitNotificaion == 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean getWarningNotification(Context context, int subId) {
        HashMap<Integer, String> hashMap = this.mSubidToIccId;
        if (hashMap == null) {
            return false;
        }
        int numWarningNotificaion = Settings.Global.getInt(context.getContentResolver(), "data_warning_notification_bool" + hashMap.get(Integer.valueOf(subId)), 0);
        if (LOGD) {
            Slog.v(TAG, "getWarningNotification subId:" + subId + " ,numWarningNotificaion:" + numWarningNotificaion);
        }
        if (numWarningNotificaion == 0) {
            return false;
        }
        return true;
    }

    public boolean getGameSpaceMode() {
        return this.mGameSpaceMode;
    }

    public void setGameSpaceMode(boolean gameMode) {
        this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", TAG);
        if (gameMode) {
            SystemProperties.set("debug.gamemode.value", NoFocusWindow.HUNG_CONFIG_ENABLE);
        } else {
            SystemProperties.set("debug.gamemode.value", "0");
        }
        setDeviceIdleMode(gameMode);
        this.mGameSpaceMode = gameMode;
    }

    public void setDeviceIdleMode(boolean enabled) {
    }
}
