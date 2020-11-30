package com.android.server.connectivity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.PswFrameworkFactory;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.IWifiRomUpdateHelper;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.theia.NoFocusWindow;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class OppoPrivateDnsHelper {
    private static final String DIALOG_COUNT = "PRIVATEDNS_DIALOG_COUNT";
    private static final int EVENT_PRIVATE_DNS_SETTINGS_CHANGED = 1;
    private static final int EVENT_PRIVATE_DNS_VALIDATING_STATUS = 2;
    private static final String LAST_DIALOG_TIMESTAMP = "PRIVATEDNS_LAST_DIALOG_TIMESTAMP";
    private static final String NOTIFICATION_CHANNEL_NAME = "privatedns_channel";
    private static final String OPPO_PRIVATE_DNS_STATUS_PROPERTY = "oppo.privatedns_status";
    private static final String PRIMARY_CHANNEL = "primary";
    private static final String PRIVATE_DNS_MODE = "private_dns_mode";
    private static final String TAG = "OppoPrivateDnsHelper";
    private static final String privateDnsUri = "oppo.comm.wirelesssettings.PRIVATE_DNS";
    private static OppoPrivateDnsHelper sInstance;
    private Context mContext = null;
    private final boolean mDbg = true;
    private final InternalHandler mHandler;
    protected final HandlerThread mHandlerThread;
    private AlertDialog mPrivateDnsDialog = null;
    private final SettingsObserver mSettingsObserver;
    private boolean needShowDialog = true;
    private boolean notificationShowed = false;

    private OppoPrivateDnsHelper(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
        registerPrivateDnsSettingsCallbacks();
    }

    private void setPrivateDnsProperty(boolean status) {
        SystemProperties.set(OPPO_PRIVATE_DNS_STATUS_PROPERTY, status ? NoFocusWindow.HUNG_CONFIG_ENABLE : "0");
    }

    private boolean isShowDialogRus() {
        boolean enable = PswFrameworkFactory.getInstance().getFeature(IWifiRomUpdateHelper.DEFAULT, new Object[]{this.mContext}).getBooleanValue("OPPO_PRIVATE_DNS_SHOW_DIALOG", false);
        logD("isShowDialogRus enable = " + enable);
        return enable;
    }

    private void privateDnsStatistics(boolean statusValidated) {
        HashMap<String, String> map = new HashMap<>();
        int status = -1;
        String mode = Settings.Global.getString(this.mContext.getContentResolver(), PRIVATE_DNS_MODE);
        if (mode != null) {
            map.put("mode", mode);
            if (mode.equals("hostname")) {
                String hostname = Settings.Global.getString(this.mContext.getContentResolver(), "private_dns_specifier");
                try {
                    status = Settings.Global.getInt(this.mContext.getContentResolver(), OppoDnsManagerHelper.PRIVATE_DNS_VALIDATING_STATUS);
                } catch (Settings.SettingNotFoundException e) {
                    logD("private_dns_validating_statusis not found");
                }
                map.put("hostname", hostname);
                if (statusValidated) {
                    map.put("status", status == 2 ? "successful" : "failed");
                }
            }
            OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "private_dns", map, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePrivateDnsSettingsChanged() {
        String mode = Settings.Global.getString(this.mContext.getContentResolver(), PRIVATE_DNS_MODE);
        logD("handlePrivateDnsSettingsChanged " + mode);
        this.needShowDialog = true;
        privateDnsStatistics(false);
        if (mode == null) {
            return;
        }
        if ((mode.equals("off") || mode.equals("opportunistic")) && this.notificationShowed) {
            cancelPrivateDnsNotification();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePrivateDnsValidatingStatus() {
        int status = -1;
        try {
            status = Settings.Global.getInt(this.mContext.getContentResolver(), OppoDnsManagerHelper.PRIVATE_DNS_VALIDATING_STATUS);
        } catch (Settings.SettingNotFoundException e) {
            logD("private_dns_validating_statusis not found");
        }
        logD("handlePrivateDnsValidatingStatus " + status);
        if (2 == status) {
            privateDnsStatistics(true);
            cancelPrivateDnsNotification();
            dismissDialog();
        }
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                OppoPrivateDnsHelper.this.handlePrivateDnsSettingsChanged();
            } else if (i == 2) {
                OppoPrivateDnsHelper.this.handlePrivateDnsValidatingStatus();
            }
        }
    }

    public static OppoPrivateDnsHelper getInstance(Context context) {
        synchronized (OppoPrivateDnsHelper.class) {
            if (sInstance == null) {
                sInstance = new OppoPrivateDnsHelper(context);
            }
        }
        return sInstance;
    }

    private synchronized void dismissDialog() {
        logD("enter dismissDialog");
        if (this.mPrivateDnsDialog != null) {
            this.mPrivateDnsDialog.dismiss();
        }
    }

    public void onDefaultNetworkChanged() {
        this.needShowDialog = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPrivateDnsModeOff() {
        Settings.Global.putString(this.mContext.getContentResolver(), PRIVATE_DNS_MODE, "off");
    }

    private boolean inStrictMode() {
        String mode = Settings.Global.getString(this.mContext.getContentResolver(), PRIVATE_DNS_MODE);
        if (mode == null || !mode.equals("hostname")) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap = new HashMap<>();

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mContext = context;
            this.mHandler = handler;
        }

        /* access modifiers changed from: package-private */
        public void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        public void onChange(boolean selfChange) {
            Log.d(OppoPrivateDnsHelper.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
                return;
            }
            Log.d(OppoPrivateDnsHelper.TAG, "No matching event to send for URI=" + uri);
        }
    }

    private void registerPrivateDnsSettingsCallbacks() {
        for (Uri uri : DnsManager.getPrivateDnsSettingsUris()) {
            this.mSettingsObserver.observe(uri, 1);
        }
        this.mSettingsObserver.observe(Settings.Global.getUriFor(OppoDnsManagerHelper.PRIVATE_DNS_VALIDATING_STATUS), 2);
    }

    public synchronized void showDialog() {
        if (isShowDialogRus()) {
            if (this.mPrivateDnsDialog != null || !this.needShowDialog || !inStrictMode()) {
                logD("need not show dialog mPrivateDnsDialog " + this.mPrivateDnsDialog + "needShowDialog = " + this.needShowDialog);
                return;
            }
            this.needShowDialog = false;
            privateDnsStatistics(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 201523207);
            builder.setPositiveButton(201653653, new DialogInterface.OnClickListener() {
                /* class com.android.server.connectivity.OppoPrivateDnsHelper.AnonymousClass1 */

                public void onClick(DialogInterface d, int w) {
                    if (OppoPrivateDnsHelper.this.mPrivateDnsDialog != null) {
                        Log.d(OppoPrivateDnsHelper.TAG, "showDialog disable PrivateDns");
                        OppoPrivateDnsHelper.this.setPrivateDnsModeOff();
                        OppoPrivateDnsHelper.this.mPrivateDnsDialog.dismiss();
                    }
                }
            });
            builder.setNegativeButton(201653650, new DialogInterface.OnClickListener() {
                /* class com.android.server.connectivity.OppoPrivateDnsHelper.AnonymousClass2 */

                public void onClick(DialogInterface d, int w) {
                    if (OppoPrivateDnsHelper.this.mPrivateDnsDialog != null) {
                        OppoPrivateDnsHelper.this.mPrivateDnsDialog.dismiss();
                        Log.d(OppoPrivateDnsHelper.TAG, "showDialog cancel");
                    }
                }
            });
            builder.setTitle("" + ((Object) this.mContext.getText(201653623)));
            builder.setMessage("" + ((Object) this.mContext.getText(201653648)));
            this.mPrivateDnsDialog = builder.create();
            this.mPrivateDnsDialog.setCanceledOnTouchOutside(false);
            this.mPrivateDnsDialog.setCancelable(false);
            this.mPrivateDnsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                /* class com.android.server.connectivity.OppoPrivateDnsHelper.AnonymousClass3 */

                public void onDismiss(DialogInterface dialog) {
                    OppoPrivateDnsHelper.this.mPrivateDnsDialog = null;
                }
            });
            WindowManager.LayoutParams p = this.mPrivateDnsDialog.getWindow().getAttributes();
            p.ignoreHomeMenuKey = 1;
            this.mPrivateDnsDialog.getWindow().setAttributes(p);
            this.mPrivateDnsDialog.getWindow().setType(2003);
            this.mPrivateDnsDialog.getWindow().addFlags(2);
            this.mPrivateDnsDialog.show();
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager.createNotificationChannel(new NotificationChannel(PRIMARY_CHANNEL, NOTIFICATION_CHANNEL_NAME, 2));
        }
    }

    public void oppoShowPrivateDnsNotification() {
        logD("oppoShowPrivateDnsNotification notificationShowed = " + this.notificationShowed);
        if (isShowDialogRus()) {
            if (this.notificationShowed || !inStrictMode()) {
                logD("oppoShowPrivateDnsNotification need not show Notification notificationShowed : " + this.notificationShowed);
                return;
            }
            NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (notificationManager != null) {
                setPrivateDnsProperty(true);
                createNotificationChannel(notificationManager);
                Intent intent = new Intent();
                Resources r = Resources.getSystem();
                intent.setAction(privateDnsUri);
                CharSequence title = r.getText(201653651);
                intent.setFlags(1140850688);
                Notification mPrivateDnsNotification = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS).setContentTitle(title).setContentText(r.getText(201653652)).setTicker(title).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT)).setSmallIcon(201852197).setLargeIcon(BitmapFactory.decodeResource(this.mContext.getResources(), 201852197)).setChannelId(PRIMARY_CHANNEL).build();
                mPrivateDnsNotification.flags = 2;
                mPrivateDnsNotification.defaults &= -2;
                logD("notify Notification ticker: " + ((Object) title));
                notificationManager.notify(0, mPrivateDnsNotification);
                this.notificationShowed = true;
            }
        }
    }

    public void cancelPrivateDnsNotification() {
        logD("cancelPrivateDnsNotification");
        setPrivateDnsProperty(false);
        NotificationManager mNM = (NotificationManager) this.mContext.getSystemService("notification");
        if (mNM != null) {
            mNM.cancel(0);
        } else {
            Log.e(TAG, " failed to get NotificationManager!");
        }
        this.notificationShowed = false;
    }

    private void logD(String log) {
        Log.d(TAG, "" + log);
    }
}
