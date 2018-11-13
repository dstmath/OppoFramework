package com.android.server.wm;

import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.OppoSafeDbReader;
import com.android.server.coloros.OppoListManager;
import com.android.server.policy.IconUtilities;

class AlertWindowNotification {
    private static final String CHANNEL_PREFIX = "com.android.server.wm.AlertWindowNotification - ";
    private static final int NOTIFICATION_ID = 0;
    public static final String PKG_PERFIX_COLOR = "com.coloros.";
    public static final String PKG_PERFIX_NEARME = "com.nearme.";
    public static final String PKG_PERFIX_OPPO = "com.oppo.";
    public static final String PKG_SUFFIX_NEARME_GAMECENTER = "nearme.gamecenter";
    public static final String PROPERTY_PERMISSION_ENABLE = "persist.sys.permission.enable";
    private static NotificationChannelGroup sChannelGroup;
    private static int sNextRequestCode = 0;
    private IconUtilities mIconUtilities;
    private final NotificationManager mNotificationManager = ((NotificationManager) this.mService.mContext.getSystemService("notification"));
    private String mNotificationTag = (CHANNEL_PREFIX + this.mPackageName);
    private final String mPackageName;
    private boolean mPosted;
    private final int mRequestCode;
    private final WindowManagerService mService;

    AlertWindowNotification(WindowManagerService service, String packageName) {
        this.mService = service;
        this.mPackageName = packageName;
        int i = sNextRequestCode;
        sNextRequestCode = i + 1;
        this.mRequestCode = i;
        this.mIconUtilities = new IconUtilities(this.mService.mContext);
    }

    void post() {
        this.mService.mH.post(new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 1, this));
    }

    void cancel() {
        this.mService.mH.post(new -$Lambda$aEpJ2RCAIjecjyIIYTv6ricEwh4((byte) 0, this));
    }

    private void onCancelNotification() {
        if (this.mPosted) {
            this.mPosted = false;
            this.mNotificationManager.cancel(this.mNotificationTag, 0);
        }
    }

    public boolean shouldHideAlertWindowNotification(String packageName) {
        if (!SystemProperties.getBoolean(PROPERTY_PERMISSION_ENABLE, false)) {
            return false;
        }
        if (packageName.startsWith("com.oppo.") || packageName.startsWith("com.coloros.") || packageName.startsWith("com.nearme.") || packageName.endsWith(PKG_SUFFIX_NEARME_GAMECENTER) || OppoListManager.getInstance().isInCustomWhiteList(packageName) || OppoListManager.getInstance().getGlobalWhiteList(this.mService.mContext, 2).contains(packageName) || OppoListManager.getInstance().getGlobalCmccCdsTestWhiteList(this.mService.mContext).contains(packageName) || OppoSafeDbReader.getInstance(this.mService.mContext).isUserClose(packageName)) {
            return true;
        }
        return false;
    }

    private void onPostNotification() {
        if (!this.mPosted && !shouldHideAlertWindowNotification(this.mPackageName)) {
            this.mPosted = true;
            Context context = this.mService.mContext;
            PackageManager pm = context.getPackageManager();
            ApplicationInfo aInfo = getApplicationInfo(pm, this.mPackageName);
            createNotificationChannel(context, aInfo != null ? pm.getApplicationLabel(aInfo).toString() : this.mPackageName);
            String message = context.getString(17039473, new Object[]{appName});
            Builder builder = new Builder(context, this.mNotificationTag).setOngoing(true).setContentTitle(context.getString(17039474, new Object[]{appName})).setContentText(message).setSmallIcon(17301708).setColor(context.getColor(17170763)).setStyle(new BigTextStyle().bigText(message)).setLocalOnly(true).setContentIntent(getContentIntent(context, this.mPackageName));
            if (aInfo != null) {
                Drawable drawable = pm.getApplicationIcon(aInfo);
                if (drawable != null) {
                    builder.setLargeIcon(this.mIconUtilities.createIconBitmap(drawable));
                }
            }
            this.mNotificationManager.notify(this.mNotificationTag, 0, builder.build());
        }
    }

    private PendingIntent getContentIntent(Context context, String packageName) {
        Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.fromParts("package", packageName, null));
        intent.setFlags(268468224);
        return PendingIntent.getActivity(context, this.mRequestCode, intent, 268435456);
    }

    private void createNotificationChannel(Context context, String appName) {
        if (sChannelGroup == null) {
            sChannelGroup = new NotificationChannelGroup(CHANNEL_PREFIX, this.mService.mContext.getString(17039471));
            this.mNotificationManager.createNotificationChannelGroup(sChannelGroup);
        }
        NotificationChannel channel = new NotificationChannel(this.mNotificationTag, context.getString(17039472, new Object[]{appName}), 1);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setBlockableSystem(true);
        channel.setGroup(sChannelGroup.getId());
        this.mNotificationManager.createNotificationChannel(channel);
    }

    private ApplicationInfo getApplicationInfo(PackageManager pm, String packageName) {
        try {
            return pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }
}
