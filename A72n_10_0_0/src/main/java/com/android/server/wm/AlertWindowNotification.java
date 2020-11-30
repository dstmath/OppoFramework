package com.android.server.wm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.OppoSafeDbReader;
import com.android.server.coloros.OppoListManager;
import com.android.server.pm.Settings;
import com.android.server.policy.IconUtilities;

/* access modifiers changed from: package-private */
public class AlertWindowNotification {
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

    /* access modifiers changed from: package-private */
    public void post() {
        this.mService.mH.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$AlertWindowNotification$iVtcJMb6VtqtAgEtGUDCkGay0tM */

            public final void run() {
                AlertWindowNotification.lambda$iVtcJMb6VtqtAgEtGUDCkGay0tM(AlertWindowNotification.this);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void cancel(boolean deleteChannel) {
        this.mService.mH.post(new Runnable(deleteChannel) {
            /* class com.android.server.wm.$$Lambda$AlertWindowNotification$ZuqSYMLXnkNVTba_yeIT9hJ1s */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AlertWindowNotification.this.lambda$cancel$0$AlertWindowNotification(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: onCancelNotification */
    public void lambda$cancel$0$AlertWindowNotification(boolean deleteChannel) {
        if (this.mPosted) {
            this.mPosted = false;
            this.mNotificationManager.cancel(this.mNotificationTag, 0);
            if (deleteChannel) {
                this.mNotificationManager.deleteNotificationChannel(this.mNotificationTag);
            }
        }
    }

    public boolean shouldHideAlertWindowNotification(String packageName) {
        if (!SystemProperties.getBoolean(PROPERTY_PERMISSION_ENABLE, false)) {
            return false;
        }
        if (packageName.startsWith(PKG_PERFIX_OPPO) || packageName.startsWith(PKG_PERFIX_COLOR) || packageName.startsWith(PKG_PERFIX_NEARME) || packageName.endsWith(PKG_SUFFIX_NEARME_GAMECENTER) || OppoListManager.getInstance().isInCustomWhiteList(packageName) || OppoListManager.getInstance().getGlobalWhiteList(this.mService.mContext, 2).contains(packageName) || OppoListManager.getInstance().getGlobalCmccCdsTestWhiteList(this.mService.mContext).contains(packageName) || OppoListManager.getInstance().isSystemApp(packageName) || OppoSafeDbReader.getInstance(this.mService.mContext).isUserClose(packageName)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void onPostNotification() {
        Drawable drawable;
        if (!this.mPosted && !shouldHideAlertWindowNotification(this.mPackageName)) {
            this.mPosted = true;
            Context context = this.mService.mContext;
            PackageManager pm = context.getPackageManager();
            ApplicationInfo aInfo = getApplicationInfo(pm, this.mPackageName);
            String appName = aInfo != null ? pm.getApplicationLabel(aInfo).toString() : this.mPackageName;
            createNotificationChannel(context, appName);
            String message = context.getString(17039482, appName);
            Bundle extras = new Bundle();
            extras.putStringArray("android.foregroundApps", new String[]{this.mPackageName});
            Notification.Builder builder = new Notification.Builder(context, this.mNotificationTag).setOngoing(true).setContentTitle(context.getString(17039483, appName)).setContentText(message).setSmallIcon(17301713).setColor(context.getColor(17170460)).setStyle(new Notification.BigTextStyle().bigText(message)).setLocalOnly(true).addExtras(extras).setContentIntent(getContentIntent(context, this.mPackageName));
            if (!(aInfo == null || (drawable = pm.getApplicationIcon(aInfo)) == null)) {
                builder.setLargeIcon(this.mIconUtilities.createIconBitmap(drawable));
            }
            this.mNotificationManager.notify(this.mNotificationTag, 0, builder.build());
        }
    }

    private PendingIntent getContentIntent(Context context, String packageName) {
        Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.fromParts(Settings.ATTR_PACKAGE, packageName, null));
        intent.setFlags(268468224);
        return PendingIntent.getActivity(context, this.mRequestCode, intent, 268435456);
    }

    private void createNotificationChannel(Context context, String appName) {
        if (sChannelGroup == null) {
            sChannelGroup = new NotificationChannelGroup(CHANNEL_PREFIX, this.mService.mContext.getString(17039480));
            this.mNotificationManager.createNotificationChannelGroup(sChannelGroup);
        }
        String nameChannel = context.getString(17039481, appName);
        if (this.mNotificationManager.getNotificationChannel(this.mNotificationTag) == null) {
            NotificationChannel channel = new NotificationChannel(this.mNotificationTag, nameChannel, 1);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setBlockableSystem(true);
            channel.setGroup(sChannelGroup.getId());
            channel.setBypassDnd(true);
            this.mNotificationManager.createNotificationChannel(channel);
        }
    }

    private ApplicationInfo getApplicationInfo(PackageManager pm, String packageName) {
        try {
            return pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
