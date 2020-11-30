package com.android.server.oppo;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import android.view.WindowManager;
import android.widget.Button;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.pm.DumpState;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;

public class TorchManagerService {
    private static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String COLUMN_NAME_VERSION = "version";
    private static final String COLUMN_NAME_XML = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final boolean DEBUG = false;
    private static final String EVENT_ID_CLOSE = "close_torch";
    private static final String EVENT_ID_OPEN = "open_torch";
    private static final String FLASH_LIGHT_HIDE_ICON = "flashlight_hide_icon";
    private static final String KEY_CAMERA_ID = "torch_protect_cameraId";
    private static final String KEY_DURATION = "torch_protect_duration";
    private static final String KEY_MAX_BATTERY_TEMPERATURE = "torch_protect_tempMax_battery";
    private static final String KEY_TIME_STAMP = "torch_protect_timestamp";
    private static final String LOG_TAG = "2012002";
    private static final int MSG_NOTIFY_CLOSE = 103;
    private static final int MSG_NOTIFY_IN = 101;
    private static final int MSG_NOTIFY_OUT = 102;
    private static final String NOTIFICATION_CHANNEL_ID = "TORCH_HIGH_TEMPERATURE_PROTECT";
    private static final String NOTIFICATION_CHANNEL_IN_ID = "TORCH_HIGH_TEMPERATURE_PROTECT_IN";
    private static final String NOTIFICATION_CHANNEL_OUT_ID = "TORCH_HIGH_TEMPERATURE_PROTECT_OUT";
    private static final String NOTIFY_ACTION = "flashlight_close";
    private static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final int SECONDTOMILLISECOND = 1000;
    private static final String TAG = "TorchManagerService";
    private static final int TORCH_OFF_DELAY_DEFAULT = 30;
    private static final int TYPE_NOTIFY_IN = 10;
    private static final int TYPE_NOTIFY_OUT = 11;
    private static final String mainTorchID = "0";
    private static TorchManagerService sInstance = null;
    private boolean bTorchMode;
    private int countTime = 30;
    private boolean doSetTorchModeFalseByUs = false;
    private int flashChargeThreshold = 0;
    private String flashSwitch = "";
    private int flashThreshold = 0;
    private boolean isNotifyClose = false;
    private boolean isNotifyIn = false;
    private boolean isNotifyOut = false;
    private CameraManager mCameraManager;
    private NotificationChannel mChannelIn = null;
    private NotificationChannel mChannelOut = null;
    private Context mContext;
    private HashMap<String, String> mEventMap = new HashMap<>();
    private String mFilterName = "sys_high_temp_protect";
    private Handler mHandler;
    private KeyguardManager mKeyguardManager;
    private NotificationManager mNotificationManager;
    private BroadcastReceiver mNotifyReceiver = new BroadcastReceiver() {
        /* class com.android.server.oppo.TorchManagerService.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null && TorchManagerService.NOTIFY_ACTION.equals(intent.getAction())) {
                TorchManagerService.this.handleNotifyClose(true);
                try {
                    TorchManagerService.this.mContext.unregisterReceiver(TorchManagerService.this.mNotifyReceiver);
                } catch (Exception e) {
                    Slog.d(TorchManagerService.TAG, "Already Unregistered !");
                }
                Slog.d(TorchManagerService.TAG, "notification clicked, close flash now!");
            }
        }
    };
    private PowerManager mPowerManager = null;
    private PowerManager.WakeLock mScreenLock = null;
    private StatusBarManager mStatusBarManager;
    private int mTemperature = 0;
    private TemperatureProvider mTemperatureProvider = null;
    private HandlerThread mThread;
    private int mThreshold = 0;
    private AlertDialog mTorchAlertDialog;
    private BroadcastReceiver mTorchReceiver = new BroadcastReceiver() {
        /* class com.android.server.oppo.TorchManagerService.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            ArrayList<String> list;
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    TorchManagerService.this.handleTempChanged(intent.getIntExtra("temperature", -1), intent.getIntExtra("status", 1));
                } else if ("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS".equals(action) && (list = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST")) != null && list.contains(TorchManagerService.this.mFilterName)) {
                    TorchManagerService.this.queryProvider();
                }
            }
        }
    };
    private int maxTemp = 0;
    private CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        /* class com.android.server.oppo.TorchManagerService.AnonymousClass1 */

        public void onTorchModeUnavailable(String cameraId) {
            super.onTorchModeUnavailable(cameraId);
        }

        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            TorchManagerService.this.bTorchMode = enabled;
            if (enabled) {
                return;
            }
            if (!TorchManagerService.this.doSetTorchModeFalseByUs) {
                TorchManagerService.this.handleNotifyClose(false);
            } else {
                TorchManagerService.this.doSetTorchModeFalseByUs = false;
            }
        }
    };
    private final Runnable torchDelayRunnable = new Runnable() {
        /* class com.android.server.oppo.TorchManagerService.AnonymousClass5 */

        public void run() {
            if (TorchManagerService.this.mTorchAlertDialog != null) {
                Button btn = TorchManagerService.this.mTorchAlertDialog.getButton(-3);
                if (TorchManagerService.this.torchOffDelay == 0) {
                    try {
                        TorchManagerService.this.mCameraManager.setTorchMode(TorchManagerService.mainTorchID, false);
                    } catch (Exception e) {
                        Slog.e(TorchManagerService.TAG, "control torch error!", e);
                        TorchManagerService.this.bTorchMode = false;
                    }
                    String contentTitle = TorchManagerService.this.mContext.getString(201653637);
                    String understood = TorchManagerService.this.mContext.getString(201653638);
                    TorchManagerService.this.mTorchAlertDialog.setTitle(contentTitle);
                    btn.setText(understood);
                    TorchManagerService.this.torchOffDelay = 30;
                    return;
                }
                btn.setText(TorchManagerService.this.mContext.getString(201653636, Integer.valueOf(TorchManagerService.this.torchOffDelay)));
                TorchManagerService.access$1010(TorchManagerService.this);
                TorchManagerService.this.mHandler.postDelayed(TorchManagerService.this.torchDelayRunnable, 1000);
            }
        }
    };
    private int torchOffDelay = 30;
    private long torchOnStamp = 0;

    static /* synthetic */ int access$1010(TorchManagerService x0) {
        int i = x0.torchOffDelay;
        x0.torchOffDelay = i - 1;
        return i;
    }

    public static TorchManagerService getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TorchManagerService(context);
        }
        return sInstance;
    }

    public static TorchManagerService getInstance() {
        return sInstance;
    }

    private TorchManagerService(Context context) {
        this.mContext = context;
        this.mThread = new HandlerThread(TAG);
        this.mThread.start();
        this.mHandler = new FlashHandler(this.mThread.getLooper());
        this.mTemperatureProvider = new TemperatureProvider();
        this.mTemperatureProvider.initTemperatureParams();
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        this.mChannelIn = new NotificationChannel(NOTIFICATION_CHANNEL_IN_ID, "flashlight_in", 4);
        this.mChannelIn.setLockscreenVisibility(1);
        this.mNotificationManager.createNotificationChannel(this.mChannelIn);
        this.mChannelOut = new NotificationChannel(NOTIFICATION_CHANNEL_OUT_ID, "flashlight_out", 3);
        this.mChannelOut.setLockscreenVisibility(1);
        this.mNotificationManager.createNotificationChannel(this.mChannelOut);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mScreenLock = this.mPowerManager.newWakeLock(268435466, TAG);
        this.mScreenLock.setReferenceCounted(false);
    }

    public void systemReady() {
        this.mCameraManager.registerTorchCallback(this.torchCallback, this.mHandler);
        IntentFilter filter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        this.mContext.registerReceiver(this.mTorchReceiver, filter);
    }

    private void handleTorchChanged(String cameraId, boolean enabled) {
        AlertDialog alertDialog;
        if (this.bTorchMode && !enabled && this.torchOffDelay != 30 && (alertDialog = this.mTorchAlertDialog) != null && alertDialog.isShowing()) {
            neutralClick();
        }
        this.bTorchMode = enabled;
        this.mEventMap.clear();
        if (enabled) {
            this.torchOnStamp = System.currentTimeMillis();
            this.mEventMap.put(KEY_TIME_STAMP, String.valueOf(System.currentTimeMillis()));
            OppoStatistics.onCommon(this.mContext, LOG_TAG, EVENT_ID_OPEN, this.mEventMap, false);
            this.mNotificationManager.cancel(NOTIFICATION_CHANNEL_ID, 0);
        } else if (this.torchOnStamp != 0) {
            this.mEventMap.put(KEY_CAMERA_ID, cameraId);
            this.mEventMap.put(KEY_DURATION, String.valueOf(System.currentTimeMillis() - this.torchOnStamp));
            this.mEventMap.put(KEY_MAX_BATTERY_TEMPERATURE, String.valueOf(this.maxTemp));
            OppoStatistics.onCommon(this.mContext, LOG_TAG, EVENT_ID_CLOSE, this.mEventMap, false);
            this.maxTemp = 0;
            this.torchOnStamp = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTempChanged(int temperature, int status) {
        boolean switchOn = TextUtils.isEmpty(this.flashSwitch) ? this.mTemperatureProvider.isTemperatureSwitchOn() : TemperatureProvider.SWITCH_ON.equals(this.flashSwitch);
        boolean charge = status == 2;
        int normalThreshold = this.flashThreshold;
        if (normalThreshold == 0) {
            normalThreshold = this.mTemperatureProvider.getTemperatureLimit();
        }
        int chargeThreshold = this.flashChargeThreshold;
        if (chargeThreshold == 0) {
            chargeThreshold = this.mTemperatureProvider.getTemperatureChargeLimit();
        }
        this.countTime = this.mTemperatureProvider.getTemperatureCountDownTime();
        int threshold = charge ? chargeThreshold : normalThreshold;
        AlertDialog alertDialog = this.mTorchAlertDialog;
        boolean showing = alertDialog != null && alertDialog.isShowing();
        this.mThreshold = threshold;
        this.mTemperature = temperature;
        if (temperature < threshold) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), FLASH_LIGHT_HIDE_ICON, 0, -2);
            cancelNotifyOut();
        }
        if (temperature >= threshold && !this.bTorchMode) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), FLASH_LIGHT_HIDE_ICON, 1, -2);
        }
        if (this.bTorchMode) {
            if (this.maxTemp < temperature) {
                this.maxTemp = temperature;
            }
            if (switchOn && !showing) {
                if (temperature >= threshold && !this.mHandler.hasMessages(101) && !this.isNotifyIn) {
                    this.mHandler.sendEmptyMessage(101);
                }
                if (temperature < threshold && !this.mHandler.hasMessages(103)) {
                    this.mHandler.sendEmptyMessage(103);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyIn() {
        if (this.mTemperature < this.mThreshold) {
            Slog.d(TAG, "temperature: " + this.mTemperature + " is low than " + this.mThreshold + ", show notification cancel");
            return;
        }
        this.isNotifyIn = true;
        String contentTitle = this.mContext.getString(201653664, Integer.valueOf(this.countTime));
        String contentText = this.mContext.getString(201653667);
        notifyIn(contentTitle + StringUtils.LF + contentText);
        this.mHandler.sendEmptyMessageDelayed(102, (long) (this.countTime * 1000));
        this.mScreenLock.acquire((long) (this.countTime * 1000));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NOTIFY_ACTION);
        this.mContext.registerReceiver(this.mNotifyReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyOut() {
        this.isNotifyOut = true;
        setTorchMode(false);
        cancelNotifyIn();
        String contentTitle = this.mContext.getString(201653665);
        notifyOut(contentTitle + StringUtils.LF + "");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyClose(boolean needCloseFlash) {
        if (needCloseFlash) {
            setTorchMode(false);
        }
        cancelNotifyIn();
        if (this.mHandler.hasMessages(102)) {
            this.mHandler.removeMessages(102);
        }
    }

    private void setTorchMode(boolean mode) {
        try {
            this.mCameraManager.setTorchMode(mainTorchID, mode);
            if (!mode) {
                Settings.System.putIntForUser(this.mContext.getContentResolver(), FLASH_LIGHT_HIDE_ICON, 1, -2);
                this.doSetTorchModeFalseByUs = true;
            }
        } catch (Exception e) {
            Slog.e(TAG, "handleTempChanged, control torch error!", e);
            this.bTorchMode = false;
        }
    }

    private void notifyIn(String text) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(NOTIFY_ACTION);
        PendingIntent notifyIntent = PendingIntent.getBroadcast(this.mContext, 0, broadcastIntent, 0);
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.bigText(text);
        this.mNotificationManager.notifyAsUser(TAG, 10, new Notification.Builder(this.mContext, this.mChannelIn.getId()).setContentText(text).setStyle(bigTextStyle).setContentIntent(notifyIntent).setWhen(System.currentTimeMillis()).setShowWhen(true).setSmallIcon(201852197).setVisibility(1).setAutoCancel(false).setOngoing(true).build(), UserHandle.CURRENT);
    }

    private void notifyOut(String text) {
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.bigText(text);
        this.mNotificationManager.notifyAsUser(TAG, 11, new Notification.Builder(this.mContext, this.mChannelOut.getId()).setContentText(text).setStyle(bigTextStyle).setWhen(System.currentTimeMillis()).setShowWhen(true).setSmallIcon(201852197).setVisibility(1).setAutoCancel(false).setOngoing(false).build(), UserHandle.CURRENT);
    }

    private void cancelNotifyIn() {
        if (this.isNotifyIn) {
            this.mNotificationManager.cancelAsUser(TAG, 10, UserHandle.CURRENT);
            this.isNotifyIn = false;
            this.mScreenLock.release();
        }
    }

    private void cancelNotifyOut() {
        if (this.isNotifyOut) {
            this.mNotificationManager.cancelAsUser(TAG, 11, UserHandle.CURRENT);
            this.isNotifyOut = false;
        }
    }

    private void showAlertDialog() {
        AlertDialog alertDialog = this.mTorchAlertDialog;
        if (alertDialog == null || !alertDialog.isShowing()) {
            String alertTitle = this.mContext.getString(201653635);
            String neutralClose = this.mContext.getString(201653636);
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 201523207);
            builder.setCancelable(false);
            builder.setTitle(alertTitle);
            int endIndex = neutralClose.indexOf("(");
            builder.setNeutralButton(neutralClose.substring(0, endIndex <= 0 ? neutralClose.length() : endIndex), new DialogInterface.OnClickListener() {
                /* class com.android.server.oppo.TorchManagerService.AnonymousClass4 */

                public void onClick(DialogInterface dialogInterface, int i) {
                    TorchManagerService.this.neutralClick();
                }
            });
            this.mTorchAlertDialog = builder.create();
            WindowManager.LayoutParams lp = this.mTorchAlertDialog.getWindow().getAttributes();
            lp.setTitle("Torch Protect");
            lp.ignoreHomeMenuKey = 1;
            this.mTorchAlertDialog.getWindow().setAttributes(lp);
            this.mTorchAlertDialog.getWindow().setType(2003);
            this.mTorchAlertDialog.getWindow().addFlags(DumpState.DUMP_FROZEN);
            this.mTorchAlertDialog.show();
            this.mHandler.post(this.torchDelayRunnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void neutralClick() {
        try {
            this.mCameraManager.setTorchMode(mainTorchID, false);
        } catch (Exception e) {
            Slog.e(TAG, "neutralClick, control torch error!", e);
            this.bTorchMode = false;
        }
        AlertDialog alertDialog = this.mTorchAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.mTorchAlertDialog = null;
        }
        this.torchOffDelay = 30;
        this.mHandler.removeCallbacks(this.torchDelayRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queryProvider() {
        Cursor cursor = null;
        String[] projection = {"version", COLUMN_NAME_XML};
        try {
            if (this.mContext == null) {
                Slog.e(TAG, "can't query provider because context is null.");
                if (0 != 0) {
                    cursor.close();
                    return;
                }
                return;
            }
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = CONTENT_URI_WHITE_LIST;
            cursor = contentResolver.query(uri, projection, "filtername=\"" + this.mFilterName + "\"", null, null);
            int versioncolumnIndex = cursor.getColumnIndex("version");
            int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_XML);
            cursor.moveToNext();
            cursor.getInt(versioncolumnIndex);
            parseXml(cursor.getString(xmlcolumnIndex));
            cursor.close();
        } catch (Exception e) {
            Slog.e(TAG, "queryProvider, We can not get white list data from provider, because of " + e);
            if (0 == 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x005d A[Catch:{ Exception -> 0x0095 }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0088 A[Catch:{ Exception -> 0x0095 }] */
    private void parseXml(String str) {
        if (TextUtils.isEmpty(str)) {
            Slog.e(TAG, "parseXml, can't parse empty xml.");
            return;
        }
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(str));
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType == 2) {
                    String name = parser.getName();
                    char c = 65535;
                    int hashCode = name.hashCode();
                    if (hashCode != -1329636050) {
                        if (hashCode != -1305620563) {
                            if (hashCode == -1296695679 && name.equals(TemperatureProvider.HIGH_TEMPERATURE_DISABLE_FLASH_LIMIT)) {
                                c = 1;
                                if (c == 0) {
                                    int i = TemperatureProvider.HIGH_TEMPERATURE_THRESHOLD;
                                    if (c == 1) {
                                        String textThreshold = parser.nextText();
                                        if (!TextUtils.isEmpty(textThreshold)) {
                                            i = Integer.parseInt(textThreshold);
                                        }
                                        this.flashThreshold = i;
                                    } else if (c == 2) {
                                        String textCharge = parser.nextText();
                                        if (!TextUtils.isEmpty(textCharge)) {
                                            i = Integer.parseInt(textCharge);
                                        }
                                        this.flashChargeThreshold = i;
                                    }
                                } else {
                                    this.flashSwitch = parser.nextText();
                                }
                            }
                        } else if (name.equals(TemperatureProvider.HIGH_TEMPERATURE_DISABLE_FLASH_CHARGE_LIMIT)) {
                            c = 2;
                            if (c == 0) {
                            }
                        }
                    } else if (name.equals(TemperatureProvider.HIGH_TEMPERATURE_DISABLE_FLASH_SWITCH)) {
                        c = 0;
                        if (c == 0) {
                        }
                    }
                    if (c == 0) {
                    }
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "parseXml, occur error when parse xml.", e);
        }
    }

    class FlashHandler extends Handler {
        public FlashHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 101:
                    TorchManagerService.this.handleNotifyIn();
                    return;
                case 102:
                    TorchManagerService.this.handleNotifyOut();
                    return;
                case 103:
                    TorchManagerService.this.handleNotifyClose(false);
                    return;
                default:
                    return;
            }
        }
    }
}
