package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.parser.JSONToken;

public class OppoCustomizeNotificationHelper {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = OppoCustomizeNotificationHelper.class.getSimpleName();
    private static volatile OppoCustomizeNotificationHelper sNotificationHelper;
    private Context mContext;
    private boolean mCustomizeVersion;
    private INotificationManager mNotificationManager;

    public interface Constants {

        public interface BadgeOption {
        }

        public interface LockScreen {
        }

        public interface SwitchType {
        }
    }

    public static OppoCustomizeNotificationHelper getInstance() {
        if (sNotificationHelper == null) {
            synchronized (OppoCustomizeNotificationHelper.class) {
                if (sNotificationHelper == null) {
                    sNotificationHelper = new OppoCustomizeNotificationHelper();
                }
            }
        }
        return sNotificationHelper;
    }

    private OppoCustomizeNotificationHelper() {
    }

    public void init(Context context) {
        this.mContext = context;
        this.mCustomizeVersion = context.getPackageManager().hasSystemFeature("oppo.business.custom");
        this.mNotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
    }

    public boolean setInterceptAllNotifications(boolean intercepted) {
        Util.logDebug("setInterceptAllNotifications() called with: intercepted = [" + intercepted + "]");
        if (!this.mCustomizeVersion) {
            Util.logDebug("Not customize version.");
            return false;
        }
        SystemProperties.set("persist.sys.customize.notification_disable", Boolean.toString(intercepted));
        SystemProperties.set("persist.sys.customize.non_system_notifications_disable", Boolean.toString(intercepted));
        return true;
    }

    public boolean setInterceptNonSystemNotifications(boolean intercepted) {
        Util.logDebug("setInterceptNonSystemNotifications() called with: intercepted = [" + intercepted + "]");
        if (!this.mCustomizeVersion) {
            Util.logDebug("Not customize version.");
            return false;
        }
        SystemProperties.set("persist.sys.customize.non_system_notifications_disable", Boolean.toString(intercepted));
        return true;
    }

    public boolean shouldInterceptAllNotifications() {
        if (!this.mCustomizeVersion || !SystemProperties.getBoolean("persist.sys.customize.notification_disable", false)) {
            return false;
        }
        return true;
    }

    public boolean shouldInterceptNonSystemNotifications() {
        if (shouldInterceptAllNotifications() || (this.mCustomizeVersion && SystemProperties.getBoolean("persist.sys.customize.non_system_notifications_disable", false))) {
            return true;
        }
        return false;
    }

    public boolean setPackageNotificationEnable(String pkgName, boolean isMultiApp, boolean enabled) {
        Util.logDebug("setNotificationsEnable() called with: pkgName = [" + pkgName + "], enabled = [" + enabled + "]");
        if (!this.mCustomizeVersion) {
            Util.logDebug("Not customize version.");
            return false;
        }
        try {
            this.mNotificationManager.setNotificationsEnabledForPackage(pkgName, Util.getAppUid(this.mContext, pkgName, isMultiApp), enabled);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isPackageNotificationEnable(String pkgName, boolean isMultiApp) {
        Util.logDebug("isPackageNotificationEnable() called with: pkgName = [" + pkgName + "]");
        if (!this.mCustomizeVersion) {
            Util.logDebug("Not customize version.");
            return false;
        }
        try {
            return this.mNotificationManager.areNotificationsEnabledForPackage(pkgName, Util.getAppUid(this.mContext, pkgName, isMultiApp));
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean updateNotificationChannel(String pkgName, boolean isMultiApp, String channelId, String switchType, boolean enabled) {
        Util.logDebug("updateNotificationChannel() called with: pkgName = [" + pkgName + "], channelId = [" + channelId + "], switchType = [" + switchType + "], enabled = [" + enabled + "]");
        if (!this.mCustomizeVersion) {
            Util.logDebug("Not customize version.");
            return false;
        }
        int uid = Util.getAppUid(this.mContext, pkgName, isMultiApp);
        NotificationChannel channel = getChannel(pkgName, uid, channelId);
        if (channel != null) {
            return updateChannel(pkgName, channel, uid, switchType, enabled);
        }
        Util.logDebug("updateNotificationChannel() error: get channel is null.");
        return false;
    }

    public boolean queryNotificationChannel(String pkgName, boolean isMultiApp, String channelId, String switchType) {
        Util.logDebug("queryNotificationChannel() called with: pkgName = [" + pkgName + "], channelId = [" + channelId + "], switchType = [" + switchType + "]");
        if (!this.mCustomizeVersion) {
            Util.logDebug("Not customize version.");
            return false;
        }
        NotificationChannel channel = getChannel(pkgName, Util.getAppUid(this.mContext, pkgName, isMultiApp), channelId);
        if (channel != null) {
            return queryChannel(channel, switchType);
        }
        Util.logDebug("queryNotificationChannel() error: get channel is null.");
        return false;
    }

    private NotificationChannel getChannel(String pkgName, int uid, String channelId) {
        if (TextUtils.isEmpty(pkgName)) {
            Util.logDebug("getChannel() error: pkgName is null or empty.");
            return null;
        }
        if (TextUtils.isEmpty(channelId)) {
            channelId = "miscellaneous";
        }
        try {
            return this.mNotificationManager.getNotificationChannelForPackage(pkgName, uid, channelId, true);
        } catch (RemoteException e) {
            return null;
        }
    }

    private boolean updateChannel(String pkgName, NotificationChannel channel, int uid, String switchType, boolean enabled) {
        Util.logDebug("updateChannel() called with: channel = [" + channel + "], pkgName = [" + pkgName + "], switchType = [" + switchType + "], enabled = [" + enabled + "]");
        channel.unlockFields(channel.getUserLockedFields());
        NotificationChannel modifiedChannel = modifyChannel(channel, switchType, enabled);
        if (modifiedChannel == null) {
            Util.logDebug("updateChannel() error: switchType " + switchType + "is not support, called with: pkgName = [" + pkgName + "], channel = [" + channel + "]");
            return false;
        }
        try {
            this.mNotificationManager.updateNotificationChannelForPackage(pkgName, uid, modifiedChannel);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean queryChannel(NotificationChannel channel, String switchType) {
        char c;
        switch (switchType.hashCode()) {
            case -1818961150:
                if (switchType.equals("channel_status_bar")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1168710168:
                if (switchType.equals("channel_banner")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1150608568:
                if (switchType.equals("channel_bubble")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1082005965:
                if (switchType.equals("channel_vibrate")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -43298620:
                if (switchType.equals("channel_lock_screen")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 738950403:
                if (switchType.equals("channel")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1706825215:
                if (switchType.equals("channel_bypass_dnd")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1763405159:
                if (switchType.equals("channel_badge")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1779538643:
                if (switchType.equals("channel_sound")) {
                    c = 6;
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
                return isChannelEnable(channel);
            case 1:
                return isLockScreenEnable(channel);
            case 2:
                return isChannelBadgeEnable(channel);
            case 3:
                return isChannelBannerEnable(channel);
            case 4:
                return isShowOnStatusEnable(channel);
            case 5:
                return isChannelBubbleEnable(channel);
            case JSONToken.TRUE /* 6 */:
                return isSoundEnable(channel);
            case JSONToken.FALSE /* 7 */:
                return isVibrateEnable(channel);
            case JSONToken.NULL /* 8 */:
                return isBypassDndEnable(channel);
            default:
                Util.logDebug(switchType + " is not support.");
                return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private NotificationChannel modifyChannel(NotificationChannel channel, String switchType, boolean enabled) {
        char c;
        switch (switchType.hashCode()) {
            case -1818961150:
                if (switchType.equals("channel_status_bar")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1168710168:
                if (switchType.equals("channel_banner")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1150608568:
                if (switchType.equals("channel_bubble")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1082005965:
                if (switchType.equals("channel_vibrate")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -43298620:
                if (switchType.equals("channel_lock_screen")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 738950403:
                if (switchType.equals("channel")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1706825215:
                if (switchType.equals("channel_bypass_dnd")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1763405159:
                if (switchType.equals("channel_badge")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1779538643:
                if (switchType.equals("channel_sound")) {
                    c = 6;
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
                setChannelEnable(channel, enabled);
                break;
            case 1:
                setLockScreenEnable(channel, enabled);
                break;
            case 2:
                setBadgeEnable(channel, enabled);
                break;
            case 3:
                setBannerEnable(channel, enabled);
                break;
            case 4:
                setShowOnStatusEnable(channel, enabled);
                break;
            case 5:
                setChannelBubbleEnable(channel, enabled);
                break;
            case JSONToken.TRUE /* 6 */:
                setSoundEnable(channel, enabled);
                break;
            case JSONToken.FALSE /* 7 */:
                setVibrateEnable(channel, enabled);
                break;
            case JSONToken.NULL /* 8 */:
                setBypassDndEnable(channel, enabled);
                break;
            default:
                Util.logDebug(switchType + " is not support.");
                return null;
        }
        return channel;
    }

    private void setImportance(NotificationChannel channel, int importance) {
        Util.logDebug("setImportance() called with: channel = [" + channel + "], importance = [" + importance + "]");
        channel.setImportance(importance);
    }

    private void adjustImportance(NotificationChannel channel) {
        int importance = 0;
        int oldImportance = channel.getImportance();
        if (oldImportance != 0) {
            importance = calculateImportance(channel);
        }
        if (importance != oldImportance) {
            setImportance(channel, importance);
        }
    }

    private int calculateImportance(NotificationChannel channel) {
        if (isBannerEnableWithoutImportance(channel)) {
            return 4;
        }
        boolean sound = isSoundEnableWithoutImportance(channel);
        boolean vibrate = channel.shouldVibrate();
        if (sound || vibrate) {
            return 3;
        }
        int visibility = channel.getLockscreenVisibility();
        if (visibility == 0 || visibility == 1 || visibility == -1000) {
            return 2;
        }
        return channel.getImportance() == 0 ? 0 : 1;
    }

    private void setChannelEnable(NotificationChannel channel, boolean enabled) {
        setImportance(channel, enabled ? 1 : 0);
        adjustImportance(channel);
    }

    private void setLockScreenEnable(NotificationChannel channel, boolean enabled) {
        int oldVisibility = channel.getLockscreenVisibility();
        int newVisibility = enabled ? 1 : -1;
        if (newVisibility != oldVisibility) {
            channel.setLockscreenVisibility(newVisibility);
        }
    }

    private void setBadgeEnable(NotificationChannel channel, boolean enabled) {
        if (enabled) {
            setBadgeOption(channel, isSupportNumBadge(channel) ? 1 : 2);
        }
        channel.setShowBadge(enabled);
    }

    private void setBadgeOption(NotificationChannel channel, int option) {
        Util.reflectCall(channel, "setBadgeOption", null, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(option)});
    }

    private void setBannerEnable(NotificationChannel channel, boolean enabled) {
        Util.reflectCall(channel, "setShowBanner", null, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(enabled)});
        adjustImportance(channel);
    }

    private void setShowOnStatusEnable(NotificationChannel channel, boolean enabled) {
        Util.reflectCall(channel, "setShowIcon", null, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(enabled)});
    }

    private void setChannelBubbleEnable(NotificationChannel channel, boolean enabled) {
        Util.reflectCall(channel, "setAllowBubbles", null, new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(enabled)});
    }

    private void setSoundEnable(NotificationChannel channel, boolean enabled) {
        channel.setSound(enabled ? Settings.System.DEFAULT_NOTIFICATION_URI : null, channel.getAudioAttributes());
        adjustImportance(channel);
    }

    private void setVibrateEnable(NotificationChannel channel, boolean enabled) {
        channel.enableVibration(enabled);
        adjustImportance(channel);
    }

    private void setBypassDndEnable(NotificationChannel channel, boolean enabled) {
        channel.setBypassDnd(enabled);
    }

    private boolean isChannelEnable(NotificationChannel channel) {
        return channel.getImportance() != 0;
    }

    private boolean isLockScreenEnable(NotificationChannel channel) {
        int lockScreenVisibility = channel.getLockscreenVisibility();
        if (lockScreenVisibility == -1000) {
            return true;
        }
        switch (lockScreenVisibility) {
            case 0:
            case 1:
                return true;
            default:
                return false;
        }
    }

    private boolean isChannelBadgeEnable(NotificationChannel channel) {
        return channel.canShowBadge();
    }

    private boolean isSupportNumBadge(NotificationChannel channel) {
        return ((Boolean) Util.reflectCall(channel, "isSupportNumBadge", false, null, null)).booleanValue();
    }

    private boolean isChannelBannerEnable(NotificationChannel channel) {
        return (channel.getImportance() >= 4) && isBannerEnableWithoutImportance(channel);
    }

    private boolean isBannerEnableWithoutImportance(NotificationChannel channel) {
        return ((Boolean) Util.reflectCall(channel, "canShowBanner", false, null, null)).booleanValue();
    }

    private boolean isShowOnStatusEnable(NotificationChannel channel) {
        return ((Boolean) Util.reflectCall(channel, "canShowIcon", false, null, null)).booleanValue();
    }

    private boolean isChannelBubbleEnable(NotificationChannel channel) {
        return ((Boolean) Util.reflectCall(channel, "canBubble", false, null, null)).booleanValue();
    }

    private boolean isSoundEnable(NotificationChannel channel) {
        return (channel.getImportance() >= 3) && isSoundEnableWithoutImportance(channel);
    }

    private boolean isSoundEnableWithoutImportance(NotificationChannel channel) {
        return channel.getSound() != null;
    }

    private boolean isVibrateEnable(NotificationChannel channel) {
        return (channel.getImportance() >= 3) && isVibrateEnableWithoutImportance(channel);
    }

    private boolean isVibrateEnableWithoutImportance(NotificationChannel channel) {
        return channel.shouldVibrate();
    }

    private boolean isBypassDndEnable(NotificationChannel channel) {
        return channel.canBypassDnd();
    }

    /* access modifiers changed from: private */
    public static final class Util {
        /* access modifiers changed from: private */
        public static int getAppUid(Context context, String pkg, boolean isMultiApp) {
            try {
                return context.getPackageManager().getApplicationInfoAsUser(pkg, 0, isMultiApp ? 999 : UserHandle.myUserId()).uid;
            } catch (Exception e) {
                logDebug("getAppUid() called with: pkg = [" + pkg + "], isMultiApp = [" + isMultiApp + "]", e);
                return -10000;
            }
        }

        /* access modifiers changed from: private */
        public static <R> R reflectCall(Object object, String method, R forNullOrExceptionReturn, Class<?>[] clazz, Object[] args) {
            try {
                R r = (R) object.getClass().getMethod(method, clazz).invoke(object, args);
                if (r != null) {
                    return r;
                }
                logDebug("method: " + method + " in " + object + " returned null.");
                return forNullOrExceptionReturn;
            } catch (Exception e) {
                logDebug("method: " + method + " in " + object + " error.", e);
                return forNullOrExceptionReturn;
            }
        }

        /* access modifiers changed from: private */
        public static void logDebug(String msg) {
            if (OppoCustomizeNotificationHelper.DEBUG) {
                Log.d(OppoCustomizeNotificationHelper.TAG, msg);
            }
        }

        private static void logDebug(String msg, Throwable tr) {
            if (OppoCustomizeNotificationHelper.DEBUG) {
                Log.d(OppoCustomizeNotificationHelper.TAG, msg, tr);
            }
        }
    }
}
