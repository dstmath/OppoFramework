package com.android.server.notification;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityThread;
import android.app.IColorNotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.IRingtonePlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.notification.ManagedServices;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.OppoCustomizeNotificationHelper;
import com.color.util.ColorTypeCastingHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Objects;
import oppo.util.OppoCommonConstants;

public class ColorNotificationManagerServiceEx extends ColorDummyNotificationManagerServiceEx implements IColorNotificationManager {
    private String TAG = "ColorNotificationManagerServiceEx";

    public ColorNotificationManagerServiceEx(Context context, NotificationManagerService nms) {
        super(context, nms);
    }

    public Context getContext() {
        return this.mNotificationManagerService.getContext();
    }

    public void onStart() {
        Slog.i(this.TAG, "onStart");
        OppoNotificationManager.getInstance().init(getNotificationManagerService().getContext(), getNotificationManagerServiceInner().getWorkerHandler(), getNotificationManagerService());
    }

    public void systemReady() {
        Slog.i(this.TAG, "systemReady");
    }

    public void onBootPhase(int phase) {
        Slog.i(this.TAG, "onBootPhase");
        if (phase == 600) {
            OppoNotificationManager.getInstance().onPhaseThrirdPartyAppsCanStart();
        }
    }

    public void handleMessage(Message msg, int whichHandler) {
    }

    public boolean shouldSuppressToast(String pkg) {
        return true;
    }

    public boolean shouldInterceptToast(String pkg) {
        if (OppoNotificationManager.getInstance().shouldInterceptToast(pkg)) {
            return true;
        }
        return false;
    }

    public boolean shouldToast(String pkg) {
        return false;
    }

    public void addToast(String pkg) {
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (!OppoCommonConstants.checkCodeValid(code, 1, 1)) {
            return false;
        }
        switch (code) {
            case 10002:
                data.enforceInterface("android.app.INotificationManager");
                boolean result = shouldInterceptSound(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeString(String.valueOf(result));
                return true;
            case 10003:
                data.enforceInterface("android.app.INotificationManager");
                boolean result2 = shouldKeepAlive(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeString(String.valueOf(result2));
                return true;
            case 10004:
                data.enforceInterface("android.app.INotificationManager");
                int result3 = getNavigationMode(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(result3);
                return true;
            case 10005:
                data.enforceInterface("android.app.INotificationManager");
                boolean result4 = isDriveNavigationMode(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeString(String.valueOf(result4));
                return true;
            case 10006:
                data.enforceInterface("android.app.INotificationManager");
                boolean result5 = isNavigationMode(data.readInt());
                reply.writeNoException();
                reply.writeString(String.valueOf(result5));
                return true;
            case 10007:
                data.enforceInterface("android.app.INotificationManager");
                String[] result6 = getEnableNavigationApps(data.readInt());
                reply.writeNoException();
                reply.writeStringArray(result6);
                return true;
            case 10008:
                data.enforceInterface("android.app.INotificationManager");
                boolean result7 = isSuppressedByDriveMode(data.readInt());
                reply.writeNoException();
                reply.writeString(String.valueOf(result7));
                return true;
            case 10009:
                data.enforceInterface("android.app.INotificationManager");
                setSuppressedByDriveMode(Boolean.valueOf(data.readString()).booleanValue(), data.readInt());
                reply.writeNoException();
                return true;
            case 10010:
                data.enforceInterface("android.app.INotificationManager");
                String result8 = getOpenid(data.readString(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeString(result8);
                return true;
            case 10011:
                data.enforceInterface("android.app.INotificationManager");
                clearOpenid(data.readString(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            case 10012:
                data.enforceInterface("android.app.INotificationManager");
                setBadgeOption(data.readString(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case 10013:
                data.enforceInterface("android.app.INotificationManager");
                int result9 = getBadgeOption(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(result9);
                return true;
            case 10014:
                data.enforceInterface("android.app.INotificationManager");
                boolean result10 = isNumbadgeSupport(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeString(String.valueOf(result10));
                return true;
            case 10015:
                data.enforceInterface("android.app.INotificationManager");
                setNumbadgeSupport(data.readString(), data.readInt(), Boolean.valueOf(data.readString()).booleanValue());
                reply.writeNoException();
                return true;
            case 10016:
                data.enforceInterface("android.app.INotificationManager");
                int result11 = getStowOption(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeString(String.valueOf(result11));
                return true;
            case 10017:
                data.enforceInterface("android.app.INotificationManager");
                setStowOption(data.readString(), data.readInt(), Integer.valueOf(data.readString()).intValue());
                reply.writeNoException();
                return true;
            case 10018:
                data.enforceInterface("android.app.INotificationManager");
                boolean result12 = checkGetOpenid(data.readString(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeString(String.valueOf(result12));
                return true;
            default:
                return false;
        }
    }

    public IColorNotificationManagerServiceInner getNotificationManagerServiceInner() {
        OppoBaseNotificationManagerService baseNms = typeCasting(this.mNotificationManagerService);
        if (baseNms != null) {
            return baseNms.mColorNmsInner;
        }
        return null;
    }

    public boolean shouldInterceptSound(String pkg, int uid) throws RemoteException {
        return OppoNotificationManager.getInstance().shouldInterceptSound(getNotificationManagerServiceInner().getPreferencesHelper(), this.mNotificationManagerService.mZenModeHelper, pkg, uid);
    }

    public boolean shouldKeepAlive(String pkg, int userId) throws RemoteException {
        return OppoNotificationManager.getInstance().shouldKeepAlive(pkg, userId);
    }

    public int getNavigationMode(String pkg, int userId) throws RemoteException {
        return OppoNotificationManager.getInstance().getNavigationMode(pkg, userId);
    }

    public boolean isDriveNavigationMode(String pkg, int userId) throws RemoteException {
        return OppoNotificationManager.getInstance().isDriveNavigationMode(pkg, userId);
    }

    public boolean isNavigationMode(int userId) throws RemoteException {
        return OppoNotificationManager.getInstance().isNavigationMode(userId);
    }

    public String[] getEnableNavigationApps(int userId) throws RemoteException {
        return OppoNotificationManager.getInstance().getEnableNavigationApps(userId);
    }

    public boolean isSuppressedByDriveMode(int userId) throws RemoteException {
        return OppoNotificationManager.getInstance().isSuppressedByDriveMode(userId);
    }

    public void setSuppressedByDriveMode(boolean mode, int userId) {
        OppoNotificationManager.getInstance().setSuppressedByDriveMode(mode, userId);
    }

    public String getOpenid(String pkg, int uid, String type) {
        if (TextUtils.isEmpty(type) || getNotificationManagerServiceInner() == null) {
            return "";
        }
        char c = 65535;
        switch (type.hashCode()) {
            case 2015626:
                if (type.equals(OpenID.TYPE_APID)) {
                    c = 1;
                    break;
                }
                break;
            case 2020431:
                if (type.equals(OpenID.TYPE_AUID)) {
                    c = 4;
                    break;
                }
                break;
            case 2109804:
                if (type.equals(OpenID.TYPE_DUID)) {
                    c = 3;
                    break;
                }
                break;
            case 2199177:
                if (type.equals(OpenID.TYPE_GUID)) {
                    c = 0;
                    break;
                }
                break;
            case 2437505:
                if (type.equals(OpenID.TYPE_OUID)) {
                    c = 2;
                    break;
                }
                break;
        }
        if (c != 0) {
            if (c != 1) {
                if (c != 2 && c != 3 && c != 4) {
                    return "";
                }
                getNotificationManagerServiceInner().checkCallerIsSystemOrSameApp(pkg);
                return OppoNotificationManager.getInstance().getOpenid(pkg, uid, type);
            } else if (getNotificationManagerService().isCallerSystemOrPhone()) {
                return OppoNotificationManager.getInstance().getOpenid(pkg, uid, type);
            } else {
                getNotificationManagerServiceInner().checkCallerIsSameApp(pkg);
                if (OppoNotificationManager.getInstance().checkGetAPID(pkg, uid)) {
                    return OppoNotificationManager.getInstance().getOpenid(pkg, uid, type);
                }
                return "";
            }
        } else if (getNotificationManagerService().isCallerSystemOrPhone()) {
            return OppoNotificationManager.getInstance().getOpenid(pkg, uid, type);
        } else {
            getNotificationManagerServiceInner().checkCallerIsSameApp(pkg);
            if (OppoNotificationManager.getInstance().checkGetGUID(pkg, uid)) {
                return OppoNotificationManager.getInstance().getOpenid(pkg, uid, type);
            }
            return "";
        }
    }

    public void clearOpenid(String pkg, int uid, String type) {
        if (getNotificationManagerServiceInner() != null) {
            getNotificationManagerServiceInner().checkCallerIsSystem();
            OppoNotificationManager.getInstance().clearOpenid(pkg, uid, type);
        }
    }

    public boolean checkGetOpenid(String pkg, int uid, String type) {
        if (TextUtils.isEmpty(type) || getNotificationManagerServiceInner() == null) {
            return false;
        }
        getNotificationManagerServiceInner().checkCallerIsSystem();
        char c = 65535;
        int hashCode = type.hashCode();
        if (hashCode != 2015626) {
            if (hashCode == 2199177 && type.equals(OpenID.TYPE_GUID)) {
                c = 0;
            }
        } else if (type.equals(OpenID.TYPE_APID)) {
            c = 1;
        }
        if (c == 0) {
            return OppoNotificationManager.getInstance().checkGetGUID(pkg, uid);
        }
        if (c != 1) {
            return false;
        }
        return OppoNotificationManager.getInstance().checkGetAPID(pkg, uid);
    }

    public void setBadgeOption(String pkg, int uid, int option) {
        if (getNotificationManagerServiceInner() != null) {
            getNotificationManagerServiceInner().checkCallerIsSystem();
            OppoBasePreferencesHelper basePreferencesHelper = typeCasting(getNotificationManagerServiceInner().getPreferencesHelper());
            if (basePreferencesHelper != null) {
                basePreferencesHelper.setBadgeOption(pkg, uid, option);
            }
            getNotificationManagerService().handleSavePolicyFile();
        }
    }

    public int getBadgeOption(String pkg, int uid) {
        if (getNotificationManagerServiceInner() == null) {
            return 0;
        }
        getNotificationManagerServiceInner().checkCallerIsSystem();
        OppoBasePreferencesHelper basePreferencesHelper = typeCasting(getNotificationManagerServiceInner().getPreferencesHelper());
        if (basePreferencesHelper != null) {
            return basePreferencesHelper.getBadgeOption(pkg, uid);
        }
        return 0;
    }

    public boolean isNumbadgeSupport(String pkg, int uid) {
        if (getNotificationManagerServiceInner() == null) {
            return false;
        }
        getNotificationManagerServiceInner().checkCallerIsSystem();
        OppoBasePreferencesHelper basePreferencesHelper = typeCasting(getNotificationManagerServiceInner().getPreferencesHelper());
        if (basePreferencesHelper != null) {
            return basePreferencesHelper.getSupportNumBadge(pkg, uid);
        }
        return false;
    }

    public void setNumbadgeSupport(String pkg, int uid, boolean support) {
        if (getNotificationManagerServiceInner() != null) {
            getNotificationManagerServiceInner().checkCallerIsSystem();
            OppoBasePreferencesHelper basePreferencesHelper = typeCasting(getNotificationManagerServiceInner().getPreferencesHelper());
            if (basePreferencesHelper != null) {
                basePreferencesHelper.setSupportNumBadge(pkg, uid, support);
            }
            getNotificationManagerService().handleSavePolicyFile();
        }
    }

    public int getStowOption(String pkg, int uid) {
        if (getNotificationManagerServiceInner() == null) {
            return 0;
        }
        getNotificationManagerServiceInner().checkCallerIsSystem();
        OppoBasePreferencesHelper basePreferencesHelper = typeCasting(getNotificationManagerServiceInner().getPreferencesHelper());
        if (basePreferencesHelper != null) {
            return basePreferencesHelper.getStowOption(pkg, uid);
        }
        return 0;
    }

    public void setStowOption(String pkg, int uid, int option) {
        if (getNotificationManagerServiceInner() != null) {
            getNotificationManagerServiceInner().checkCallerIsSystem();
            OppoBasePreferencesHelper basePreferencesHelper = typeCasting(getNotificationManagerServiceInner().getPreferencesHelper());
            if (basePreferencesHelper != null) {
                basePreferencesHelper.setStowOption(pkg, uid, option);
            }
            getNotificationManagerService().handleSavePolicyFile();
        }
    }

    public void cancelAllNotificationsInt(String action, int callingUid, int callingPid, String pkg, String channelId, int mustHaveFlags, int mustNotHaveFlags, boolean doit, int userId, int reason, ManagedServices.ManagedServiceInfo listener) {
        int finalReason = reason;
        if (TextUtils.equals(action, "android.intent.action.PACKAGE_RESTARTED")) {
            finalReason = 10020;
        }
        this.mNotificationManagerService.cancelAllNotificationsInt(callingUid, callingPid, pkg, channelId, mustHaveFlags, mustNotHaveFlags, doit, userId, finalReason, listener);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x008d A[ADDED_TO_REGION] */
    public void enqueueNotificationInternal(String oriPkg, String oriOpPkg, int oriCallingUid, int callingPid, String tag, int id, Notification notification, int incomingUserId) {
        int callingUid;
        String opPkg;
        String opPkg2;
        NotificationChannel channel;
        int userId;
        Notification notification2;
        boolean z;
        String pkg;
        int notificationUid;
        boolean z2;
        String channelId;
        NotificationChannel channel2;
        if (DEBUG) {
            Slog.v(this.TAG, "Notification--enqueueNotificationInternal: oriPkg=" + oriPkg + ",oriOpPkg:" + oriOpPkg + ",id=" + id + " notification=" + notification);
        }
        if ("com.coloros.mcs".equals(oriPkg)) {
            String pkg2 = notification.extras.getString("appPackage");
            String opPkg3 = notification.extras.getString("appPackage");
            int callingUid2 = -1;
            try {
                ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(pkg2, 268435456, 0);
                if (appInfo != null) {
                    callingUid2 = appInfo.uid;
                }
                opPkg = opPkg3;
                opPkg2 = pkg2;
                callingUid = callingUid2;
            } catch (RemoteException e) {
                Slog.w(this.TAG, "Could not contact PackageManager", e);
                opPkg = opPkg3;
                opPkg2 = pkg2;
                callingUid = -1;
                if (opPkg2 != null) {
                }
                throw new IllegalArgumentException("null not allowed: pkg=" + opPkg2 + " id=" + id + " notification=" + notification);
            } catch (Exception e2) {
                Slog.w(this.TAG, "get pkg uid exception", e2);
                opPkg = opPkg3;
                opPkg2 = pkg2;
                callingUid = -1;
                if (opPkg2 != null) {
                }
                throw new IllegalArgumentException("null not allowed: pkg=" + opPkg2 + " id=" + id + " notification=" + notification);
            }
        } else {
            opPkg = oriOpPkg;
            callingUid = oriCallingUid;
            opPkg2 = oriPkg;
        }
        if (opPkg2 != null || notification == null) {
            throw new IllegalArgumentException("null not allowed: pkg=" + opPkg2 + " id=" + id + " notification=" + notification);
        } else if (OppoCustomizeNotificationHelper.getInstance().shouldInterceptNotifications(oriPkg)) {
            if (DEBUG) {
                Slog.v(this.TAG, "Notification--enqueueNotificationInternal is intercept by custom version. oriPkg=" + oriPkg);
            }
        } else if (OppoNotificationManager.getInstance().isHidePkg(oriPkg, incomingUserId)) {
            if (DEBUG) {
                Slog.v(this.TAG, "Notification--enqueueNotificationInternal-isHidePkg: oriPkg=" + oriPkg + ",incomingUserId:" + incomingUserId);
            }
        } else if (OppoNotificationManager.getInstance().isInvalidOpushNotification(oriPkg, callingUid, getNotificationManagerServiceInner().getPreferencesHelper(), notification)) {
            if (DEBUG) {
                Slog.v(this.TAG, "Notification--isInvalidOpushNotification-true: " + notification);
            }
        } else if (OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).shouldSkipNotification(incomingUserId, opPkg2)) {
            Slog.v(this.TAG, "enqueueNotificationInternal Not showing " + opPkg2 + " userId:" + incomingUserId);
        } else {
            OppoNotificationManager.getInstance().updateNoClearNotification(notification, opPkg2);
            int userId2 = ActivityManager.handleIncomingUser(callingPid, callingUid, incomingUserId, true, false, "enqueueNotification", opPkg2);
            UserHandle user = UserHandle.of(userId2);
            if (!OppoNotificationManager.getInstance().shouldInterceptNotification(opPkg2, notification, userId2)) {
                int notificationUid2 = getNotificationManagerService().resolveNotificationUid(opPkg, opPkg2, callingUid, userId2);
                getNotificationManagerServiceInner().checkRestrictedCategories(notification);
                try {
                    getNotificationManagerService().fixNotification(notification, opPkg2, userId2);
                    getNotificationManagerServiceInner().getNotificationUsageStats().registerEnqueuedByApp(opPkg2);
                    String channelId2 = notification.getChannelId();
                    if (getNotificationManagerServiceInner().isTelevision() && new Notification.TvExtender(notification).getChannelId() != null) {
                        channelId2 = new Notification.TvExtender(notification).getChannelId();
                    }
                    NotificationChannel channel3 = getNotificationManagerServiceInner().getPreferencesHelper().getNotificationChannel(opPkg2, notificationUid2, channelId2, false);
                    if (channel3 == null) {
                        Slog.e(this.TAG, "No Channel found for pkg=" + opPkg2 + ", channelId=" + channelId2 + ", id=" + id + ", tag=" + tag + ", opPkg=" + opPkg + ", callingUid=" + callingUid + ", userId=" + userId2 + ", incomingUserId=" + incomingUserId + ", notificationUid=" + notificationUid2 + ", notification=" + notification);
                        if (getNotificationManagerServiceInner().getPreferencesHelper().getImportance(opPkg2, notificationUid2) == 0) {
                            return;
                        }
                        if (OppoNotificationManager.getInstance().shouldShowNotificationToast()) {
                            getNotificationManagerServiceInner().doChannelWarningToast("Developer warning for package \"" + opPkg2 + "\"\nFailed to post notification on channel \"" + channelId2 + "\"\nSee log for more details");
                            return;
                        }
                        return;
                    }
                    StatusBarNotification n = new StatusBarNotification(opPkg2, opPkg, id, tag, notificationUid2, callingPid, notification, user, null, System.currentTimeMillis());
                    NotificationRecord r = new NotificationRecord(getContext(), n, channel3);
                    if (getNotificationManagerServiceInner().getPreferencesHelper().getImportance(r.sbn.getPackageName(), r.sbn.getUid()) != 0) {
                        EnvelopeDetectorController mEnvelopeDetectorController = OppoNotificationManager.getInstance().getEnvelopeDetectorController();
                        if (mEnvelopeDetectorController != null) {
                            Context context = getContext();
                            String key = n.getKey();
                            int lockscreenVisibility = channel3.getLockscreenVisibility();
                            userId = userId2;
                            pkg = opPkg2;
                            channel = channel3;
                            z = true;
                            notification2 = notification;
                            mEnvelopeDetectorController.detectEnvelope(context, notification, oriPkg, key, oriCallingUid, lockscreenVisibility);
                        } else {
                            channel = channel3;
                            notification2 = notification;
                            userId = userId2;
                            pkg = opPkg2;
                            z = true;
                        }
                    } else {
                        channel = channel3;
                        z = true;
                        notification2 = notification;
                        userId = userId2;
                        pkg = opPkg2;
                    }
                    r.setIsAppImportanceLocked(getNotificationManagerServiceInner().getPreferencesHelper().getIsAppImportanceLocked(pkg, callingUid));
                    if ((notification2.flags & 64) != 0) {
                        boolean fgServiceShown = channel.isFgServiceShown();
                        if ((channel.getUserLockedFields() & 4) == 0 || !fgServiceShown) {
                            if (r.getImportance() == z) {
                                channel2 = channel;
                            } else if (r.getImportance() == 0) {
                                channel2 = channel;
                            }
                            if (TextUtils.isEmpty(channelId2)) {
                                notificationUid = notificationUid2;
                                z2 = false;
                            } else if ("miscellaneous".equals(channelId2)) {
                                notificationUid = notificationUid2;
                                z2 = false;
                            } else {
                                channel2.setImportance(2);
                                r.setSystemImportance(2);
                                if (!fgServiceShown) {
                                    channel2.unlockFields(4);
                                    channel2.setFgServiceShown(z);
                                }
                                notificationUid = notificationUid2;
                                z2 = false;
                                getNotificationManagerServiceInner().getPreferencesHelper().updateNotificationChannel(pkg, notificationUid, channel2, false);
                                r.updateNotificationChannel(channel2);
                            }
                            r.setSystemImportance(2);
                        }
                        if (fgServiceShown || TextUtils.isEmpty(channelId2)) {
                            notificationUid = notificationUid2;
                            z2 = false;
                        } else if (!"miscellaneous".equals(channelId2)) {
                            channel.setFgServiceShown(z);
                            r.updateNotificationChannel(channel);
                            notificationUid = notificationUid2;
                            z2 = false;
                        } else {
                            notificationUid = notificationUid2;
                            z2 = false;
                        }
                    } else {
                        notificationUid = notificationUid2;
                        z2 = false;
                    }
                    OppoNotificationManager.getInstance().setKeepAliveAppIfNeed(pkg, id, z);
                    IColorNotificationManagerServiceInner notificationManagerServiceInner = getNotificationManagerServiceInner();
                    if (r.sbn.getOverrideGroupKey() == null) {
                        z = z2;
                    }
                    if (notificationManagerServiceInner.checkDisqualifyingFeatures(userId, notificationUid, id, tag, r, z)) {
                        if (notification2.allPendingIntents != null) {
                            int intentCount = notification2.allPendingIntents.size();
                            if (intentCount > 0) {
                                ActivityManagerInternal am = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                                long duration = ((DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class)).getNotificationWhitelistDuration();
                                int i = 0;
                                while (i < intentCount) {
                                    PendingIntent pendingIntent = (PendingIntent) notification2.allPendingIntents.valueAt(i);
                                    if (pendingIntent != null) {
                                        am.setPendingIntentWhitelistDuration(pendingIntent.getTarget(), getNotificationManagerServiceInner().getWhitelistToken(), duration);
                                        channelId = channelId2;
                                        am.setPendingIntentAllowBgActivityStarts(pendingIntent.getTarget(), getNotificationManagerServiceInner().getWhitelistToken(), 7);
                                    } else {
                                        channelId = channelId2;
                                    }
                                    i++;
                                    channelId2 = channelId;
                                }
                            }
                        }
                        Handler workerHandler = getNotificationManagerServiceInner().getWorkerHandler();
                        NotificationManagerService notificationManagerService = getNotificationManagerService();
                        Objects.requireNonNull(notificationManagerService);
                        workerHandler.post(new NotificationManagerService.EnqueueNotificationRunnable(notificationManagerService, userId, r));
                    }
                } catch (PackageManager.NameNotFoundException e3) {
                    Slog.e(this.TAG, "Cannot create a context for sending app", e3);
                }
            } else if (DEBUG) {
                Slog.d(this.TAG, "shouldInterceptNotification: pkg: " + opPkg2 + ",notification:" + notification);
            }
        }
    }

    public boolean shouldLimitChannels(PreferencesHelper preferencesHelper, String pkg, int uid, int channelsSize) {
        return OppoNotificationManager.getInstance().shouldLimitChannels(preferencesHelper, pkg, uid, channelsSize);
    }

    public boolean isMutilAppUserid(int userId) {
        return userId == 999;
    }

    public boolean shouldLimitNotification(NotificationRecord r, int callingUid, int userId, NotificationManagerService.NotificationListeners listeners, int max) {
        boolean isSystemNotification;
        int count;
        try {
            String pkg = r.sbn.getPackageName();
            if (!this.mNotificationManagerService.isUidSystemOrPhone(callingUid)) {
                if (!"android".equals(pkg)) {
                    isSystemNotification = false;
                    boolean isNotificationFromListener = listeners.isListenerPackage(pkg);
                    if ((callingUid == -1 || (!isSystemNotification && !isNotificationFromListener)) && (count = this.mNotificationManagerService.getNotificationCountLocked(pkg, userId, r.sbn.getId(), r.sbn.getTag())) >= max) {
                        getNotificationManagerServiceInner().getNotificationUsageStats().registerOverCountQuota(pkg);
                        Slog.e(this.TAG, "Package really has already posted or enqueued " + count + " notifications.  Not showing more.  package=" + pkg);
                        return true;
                    }
                    return false;
                }
            }
            isSystemNotification = true;
            boolean isNotificationFromListener2 = listeners.isListenerPackage(pkg);
            getNotificationManagerServiceInner().getNotificationUsageStats().registerOverCountQuota(pkg);
            Slog.e(this.TAG, "Package really has already posted or enqueued " + count + " notifications.  Not showing more.  package=" + pkg);
            return true;
        } catch (Exception e) {
        }
    }

    public boolean playAsync(IRingtonePlayer player, NotificationRecord record, Uri soundUri, boolean looping) {
        try {
            UserHandle user = record.sbn.getUser();
            if (OppoNotificationManager.getInstance().isMultiAppUserIdMatch(record, record.getUserId())) {
                user = UserHandle.OWNER;
            }
            EnvelopeDetectorController mEnvelopeDetectorController = OppoNotificationManager.getInstance().getEnvelopeDetectorController();
            if (mEnvelopeDetectorController != null && mEnvelopeDetectorController.useEnvelopeSound(record.getNotification())) {
                soundUri = record.getNotification().sound;
            }
            player.playAsync(soundUri, user, looping, record.getAudioAttributes());
            return true;
        } catch (Exception e) {
            Slog.e(this.TAG, "playAsync fatal error", e);
            return true;
        }
    }

    public boolean isNotificationForCurrentUser(NotificationRecord record, int userId) {
        return OppoNotificationManager.getInstance().isMultiAppUserIdMatch(record, userId);
    }

    public void removeToastQueue(String pkg) {
    }

    public void setNavigationStatus(String pkg, String channelId, int callingUid, int callingPid, int reason) {
        OppoNotificationManager.getInstance().setNavigationStatus(pkg, channelId, callingUid, callingPid, reason);
    }

    public boolean dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        return OppoNotificationManager.getInstance().dumpOppoNotificationInfo(fd, pw, args);
    }

    public boolean shouldKeepNotifcationWhenForceStop(String pkg, NotificationRecord r, int reason) {
        return OppoNotificationManager.getInstance().shouldKeepNotifcationWhenForceStop(pkg, r, reason);
    }

    public boolean isShutdown() {
        return OppoNotificationManager.getInstance().isShutdown();
    }

    public boolean canListenNotificationChannelChange(String pkg) {
        return OppoNotificationManager.getInstance().canListenNotificationChannelChange(pkg);
    }

    public boolean enabledAndUserMatches(StatusBarNotification sbn, ManagedServices.ManagedServiceInfo listener) {
        return listener.enabledAndUserMatches(OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUserId(sbn.getUserId(), sbn.getPackageName(), true));
    }

    public void setShutdown(String action, boolean shutdown) {
        if (TextUtils.equals(action, "android.intent.action.ACTION_SHUTDOWN")) {
            OppoNotificationManager.getInstance().setShutdown(true);
            getNotificationManagerServiceInner().updateNotificationPulse();
        }
    }

    public void setKeepAliveAppIfNeed(String pkgName, int id, boolean isKeepAlive) {
        OppoNotificationManager.getInstance().setKeepAliveAppIfNeed(pkgName, id, isKeepAlive);
    }

    public void detectCancelAction(Context context, int id, String pkg, int userId) {
        EnvelopeDetectorController mEnvelopeDetectorController = OppoNotificationManager.getInstance().getEnvelopeDetectorController();
        if (mEnvelopeDetectorController != null) {
            mEnvelopeDetectorController.detectCancelAction(getContext(), id, pkg, userId);
        }
    }

    public boolean shouldSuppressEffect(NotificationRecord record) {
        return OppoNotificationManager.getInstance().shouldSuppressEffect(record) || isStowed(record);
    }

    public boolean isStowOptionKey(String key) {
        return OppoNotificationManager.getInstance().isStowOptionKey(key);
    }

    private OppoBaseNotificationManagerService typeCasting(NotificationManagerService nms) {
        if (nms != null) {
            return (OppoBaseNotificationManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseNotificationManagerService.class, nms);
        }
        return null;
    }

    private OppoBasePreferencesHelper typeCasting(PreferencesHelper helper) {
        if (helper != null) {
            return (OppoBasePreferencesHelper) ColorTypeCastingHelper.typeCasting(OppoBasePreferencesHelper.class, helper);
        }
        return null;
    }

    public boolean vibrateLinearmotorIfNeed(long[] vibration, boolean hasValidSound, Uri soundUri) {
        return OppoNotificationManager.getInstance().vibrateLinearmotorIfNeed(vibration, hasValidSound, soundUri);
    }

    private boolean isStowed(NotificationRecord record) {
        if (record == null || record.sbn == null || getStowOption(record.sbn.getPackageName(), record.sbn.getUid()) != -1) {
            return false;
        }
        return true;
    }
}
