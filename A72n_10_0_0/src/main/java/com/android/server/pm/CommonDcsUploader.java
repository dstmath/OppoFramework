package com.android.server.pm;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.HwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.theia.NoFocusWindow;
import com.android.server.usage.AppStandbyController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import vendor.oppo.hardware.commondcs.V1_0.ICommonDcsHalService;
import vendor.oppo.hardware.commondcs.V1_0.StringPair;

public class CommonDcsUploader {
    private static boolean DEBUG_CPT_UP = false;
    private static final String EVENT_ID_ADD = "OPPO_SYSTEM_INFORMATION";
    private static final String EVENT_ID_GET_SYSTEM_UPTIME = "Android_get_system_uptime";
    private static final String LOG_TAG = "common_system_info";
    private static final int MSG_ADD_EVENT = 1;
    private static final int MSG_DEBUG_LOG_SWITCH = 2;
    public static final int PERIOD_DAY_MILLIS = 86400000;
    private static final int SPECIAL_TYPE_ACCESSING_HIDDEN_API = 1;
    private static final int SPECIAL_TYPE_TEST = 0;
    private static final int SPECIAL_TYPE_UNKNOWN = -1;
    private static final String TAG = "CommonDcsUploader";
    private static final List<String> mSpecialTypes = Arrays.asList("SPECIAL_TYPE_TEST", "accssing_hidden_api");
    private static Method mUploadFunc = null;
    private static CommonDcsUploader sInstance = null;
    private AlarmManager mAlarmManager;
    private boolean mBootCompleted = false;
    private Context mContext;
    private Handler mHandler;
    private long mNextAlarmTime = 0;
    private PackageManager mPackageManager;
    private HandlerThread mThread;
    private final AlarmManager.OnAlarmListener mUploadUptimeAlarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.pm.CommonDcsUploader.AnonymousClass1 */

        public void onAlarm() {
            synchronized (CommonDcsUploader.this) {
                CommonDcsUploader.this.handleGetUptimeAlarm();
            }
        }
    };

    public native int sendToDcsTestNative(int i);

    private CommonDcsUploader(Context context) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        startHidlService();
        this.mThread = new HandlerThread("CommonSendDcs");
        this.mThread.start();
        initHandler(this.mThread.getLooper());
    }

    public static CommonDcsUploader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CommonDcsUploader(context);
        }
        return sInstance;
    }

    public void initAlarmForDcs() {
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            Slog.d(TAG, " can not get AlarmManager");
            return;
        }
        this.mNextAlarmTime = getNextTime();
        scheduleAlarmForDcs();
    }

    private void scheduleAlarmForDcs() {
        this.mAlarmManager.setExact(0, this.mNextAlarmTime, "CommonDcsUploader.uptime", this.mUploadUptimeAlarmListener, this.mHandler);
    }

    private void collectWhenBooted() {
        readOppoDriverInfo();
    }

    private void readOppoDriverInfo() {
        File file = new File("/proc/oppo_driver_info");
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                FileInputStream fis2 = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis2));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = br.readLine();
                    if (line != null) {
                        sb.append(line);
                        sb.append(StringUtils.LF);
                    } else {
                        HashMap<String, String> msg = new HashMap<>();
                        msg.put("data", sb.toString());
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, new MessageContainer(DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "kernel_device_event", msg)));
                        try {
                            fis2.close();
                            return;
                        } catch (Exception e) {
                            return;
                        }
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                if (0 != 0) {
                    fis.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fis.close();
                    } catch (Exception e3) {
                    }
                }
                throw th;
            }
        }
    }

    public void sendToDcsNativeTest() {
        sendToDcsTestNative(2009);
    }

    public void sendToUploadTest() {
        Map<String, String> logMap = new ConcurrentHashMap<>();
        logMap.put("point", "test_packaege");
        logMap.put("Package", "test_version");
        logMap.put("version", "test_point");
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1, logMap));
    }

    public void sendToCommonDcsUpload(String logTag, String eventId, Map<String, String> data) {
        fixDataForSpecialType(logTag, eventId, data);
        sendToCommonDcsUploadUnchecked(logTag, eventId, data);
    }

    public void sendToDebugLogSwitch(boolean switchOn) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(2, Boolean.valueOf(switchOn)));
    }

    /* access modifiers changed from: package-private */
    public PackageManager getPackageManager() {
        if (this.mPackageManager == null) {
            this.mPackageManager = this.mContext.getPackageManager();
        }
        return this.mPackageManager;
    }

    private long getNextTime() {
        Calendar currentDate = Calendar.getInstance();
        long currentMillis = currentDate.getTimeInMillis();
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(currentDate.get(1), currentDate.get(2), currentDate.get(5), 22, 55, 0);
        long expectedMillis = targetDate.getTimeInMillis();
        if (expectedMillis < 1000 + currentMillis) {
            return expectedMillis + 86400000;
        }
        return expectedMillis;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetUptimeAlarm() {
        Slog.d(TAG, "getUptime");
        this.mNextAlarmTime = getNextTime();
        scheduleAlarmForDcs();
        long time = SystemClock.uptimeMillis();
        HashMap<String, String> msg = new HashMap<>();
        msg.put("uptime", String.format("%dh", Integer.valueOf((int) (time / AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT))));
        MessageContainer msgContainer = new MessageContainer(DcsFingerprintStatisticsUtil.DCS_LOG_TAG, EVENT_ID_GET_SYSTEM_UPTIME, msg);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1, msgContainer));
    }

    private int accquireIntType(String strType) {
        return mSpecialTypes.indexOf(strType);
    }

    private void fixDataForSpecialType(String logTag, String eventId, Map<String, String> msg) {
        int accquireIntType;
        if (DcsFingerprintStatisticsUtil.DCS_LOG_TAG.equals(logTag) && (accquireIntType = accquireIntType(eventId)) != 0 && accquireIntType == 1) {
            int uid = Binder.getCallingUid();
            String pkgName = getPackageManager().getNameForUid(uid);
            Slog.d(TAG, "hidden api uid: " + uid + " pkg:" + pkgName);
            if (!msg.containsKey("Package")) {
                msg.put("Package", pkgName);
            }
        }
    }

    private void sendToCommonDcsUploadUnchecked(String logTag, String eventId, Map<String, String> msg) {
        MessageContainer msgContainer = new MessageContainer(logTag, eventId, msg);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1, msgContainer));
    }

    private void sendToCommonDcsUploadInternal(String type, Map<String, String> map) {
        int accquireIntType = accquireIntType(type);
        if (accquireIntType != -1 && accquireIntType == 0) {
            Slog.d(TAG, "special type:" + type);
        }
    }

    private void sendDcsCustomizeImpl(String tag, String eventId, Map<String, String> msg) {
        if (DEBUG_CPT_UP) {
            Slog.d(TAG, "uploadDcsImpl, logTag: " + tag + "eventId: " + eventId + " UploadMsg: " + msg.toString());
        }
        if (!this.mBootCompleted) {
            if (!NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("sys.boot_completed"))) {
                Slog.d(TAG, "system_server not ready for dcs, drop event, tag: " + tag + " eventId: " + eventId);
                return;
            }
            this.mBootCompleted = true;
            collectWhenBooted();
        }
        try {
            if (mUploadFunc == null) {
                mUploadFunc = Class.forName("oppo.util.OppoStatistics").getMethod("onCommon", Context.class, String.class, String.class, Map.class, Boolean.TYPE);
            }
            mUploadFunc.invoke(null, this.mContext, tag, eventId, msg, true);
        } catch (Exception e) {
            mUploadFunc = null;
            e.printStackTrace();
        }
    }

    class CommonDcsHalService extends ICommonDcsHalService.Stub {
        CommonDcsHalService() {
        }

        @Override // vendor.oppo.hardware.commondcs.V1_0.ICommonDcsHalService
        public int notifyMsgToCommonDcs(ArrayList<StringPair> data, String logTag, String eventId) {
            Slog.d(CommonDcsUploader.TAG, "notifyMsgToCommonDcs, logTag: " + logTag + eventId);
            HashMap<String, String> dataMap = new HashMap<>();
            Iterator<StringPair> it = data.iterator();
            while (it.hasNext()) {
                StringPair pair = it.next();
                dataMap.put(pair.key, pair.value);
            }
            CommonDcsUploader.this.mHandler.sendMessage(CommonDcsUploader.this.mHandler.obtainMessage(1, new MessageContainer(logTag, eventId, dataMap)));
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public class MessageContainer {
        public final String id;
        public final Map<String, String> msg;
        public final String tag;

        MessageContainer(String logTag, String eventId, Map<String, String> log) {
            this.tag = logTag;
            this.id = eventId;
            this.msg = log;
        }
    }

    private void startHidlService() {
        new Thread(new Runnable() {
            /* class com.android.server.pm.CommonDcsUploader.AnonymousClass2 */

            public void run() {
                try {
                    Slog.e(CommonDcsUploader.TAG, "startHidlService");
                    HwBinder.configureRpcThreadpool(1, true);
                    new CommonDcsHalService().registerAsService("commondcsservice");
                    HwBinder.joinRpcThreadpool();
                } catch (RemoteException e) {
                    Slog.e(CommonDcsUploader.TAG, "startHidlService  RemoteException " + e);
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAddEventMsg(Message msg) {
        if (!(msg.obj instanceof MessageContainer)) {
            Slog.e(TAG, "cpt upload data error!");
            return;
        }
        MessageContainer tmp = (MessageContainer) msg.obj;
        sendDcsCustomizeImpl(tmp.tag, tmp.id, tmp.msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLogSwitchMsg(Message msg) {
        DEBUG_CPT_UP = ((Boolean) msg.obj).booleanValue();
    }

    private void initHandler(Looper looper) {
        if (looper == null) {
            Slog.e(TAG, "can not get my looper!");
        } else {
            this.mHandler = new Handler(looper) {
                /* class com.android.server.pm.CommonDcsUploader.AnonymousClass3 */

                public void handleMessage(Message msg) {
                    if (CommonDcsUploader.DEBUG_CPT_UP) {
                        Slog.d(CommonDcsUploader.TAG, " handleMsg, what: " + msg.what);
                    }
                    int i = msg.what;
                    if (i == 1) {
                        CommonDcsUploader.this.handleAddEventMsg(msg);
                    } else if (i != 2) {
                        Slog.w(CommonDcsUploader.TAG, "undefined cpt upload event!");
                    } else {
                        CommonDcsUploader.this.handleLogSwitchMsg(msg);
                    }
                }
            };
        }
    }
}
