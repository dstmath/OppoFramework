package com.mediatek.mtklogger.c2klogger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.mediatek.mtklogger.c2klogger.EtsLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;

public class C2KLogService {
    private static final String ACTION_VIA_ETS_DEV_CHANGED = "via.cdma.action.ets.dev.changed.c2klogger";
    private static final int MSG_SET_CHANNEL = 1;
    public static final String UPDATE_INFO_ACTION = "android.intent.action.UPDATE_INFO";
    private final BroadcastReceiver mBcRecverCbp = new BroadcastReceiver() {
        /* class com.mediatek.mtklogger.c2klogger.C2KLogService.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            Log.i(C2KLogUtils.TAG_APP, "I am bc receiver of cbp in log service of saber");
            if (intent.getAction().equals(C2KLogService.ACTION_VIA_ETS_DEV_CHANGED)) {
                boolean resultOk = intent.getBooleanExtra(C2KLogUtils.EXTRAL_VIA_ETS_DEV_RESULT, false);
                Log.i(C2KLogUtils.TAG_APP, "The result for set SDIO channel is " + resultOk + ", mIsSetChannelDone = " + C2KLogService.this.mIsSetChannelDone);
                if (!C2KLogService.this.mIsSetChannelDone) {
                    boolean unused = C2KLogService.this.mIsSetChannelDone = true;
                }
            }
        }
    };
    private EtsLog.EtsLogCallback mCbEtsLog = new EtsLog.EtsLogCallback() {
        /* class com.mediatek.mtklogger.c2klogger.C2KLogService.AnonymousClass1 */

        @Override // com.mediatek.mtklogger.c2klogger.EtsLog.EtsLogCallback
        public void onProcess(EtsLog.LogStatus status, String info) {
            if (status == EtsLog.LogStatus.Error) {
                Log.e(C2KLogUtils.TAG_APP, "error:" + info);
                C2KLogService.this.mService.stopSelf();
            } else if (status == EtsLog.LogStatus.Logging) {
                Log.i(C2KLogUtils.TAG_APP, "info:" + info);
            }
        }
    };
    public String mDevicePath = null;
    private EtsLog mEtsLog = null;
    public String mFilterFilePath = null;
    private HandlerThread mHandlerThread;
    private boolean mIsModemReady = false;
    private boolean mIsOnDestroy = false;
    /* access modifiers changed from: private */
    public boolean mIsSetChannelDone = true;
    private String mMdlogPath = null;
    private Handler mMessageHandler;
    private Thread mMonitModemThread;
    private final BroadcastReceiver mRebootReceiver = new BroadcastReceiver() {
        /* class com.mediatek.mtklogger.c2klogger.C2KLogService.AnonymousClass4 */
        /* access modifiers changed from: private */
        public boolean mIsStopDone = false;

        public void onReceive(Context context, Intent intent) {
            Log.i(C2KLogUtils.TAG_APP, "I am bc receiver of mRebootReceiver");
            if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN")) {
                Log.i(C2KLogUtils.TAG_APP, "-->System is normal SHUTDOWN now, save buffer to file.");
                this.mIsStopDone = false;
                new Thread(new Runnable() {
                    /* class com.mediatek.mtklogger.c2klogger.C2KLogService.AnonymousClass4.AnonymousClass1 */

                    public void run() {
                        C2KLogService.this.destroy();
                        boolean unused = AnonymousClass4.this.mIsStopDone = true;
                    }
                }).start();
                int i = 0;
                while (true) {
                    if (this.mIsStopDone) {
                        break;
                    }
                    int i2 = i + C2KLogService.MSG_SET_CHANNEL;
                    if (i >= 60) {
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i = i2;
                }
                Log.i(C2KLogUtils.TAG_APP, "<--System is normal SHUTDOWN now, save buffer to file!");
            }
        }
    };
    /* access modifiers changed from: private */
    public Service mService;
    private SharedPreferences mSharedPreferences;

    public C2KLogService(Service service) {
        this.mService = service;
    }

    public IBinder onBind(Intent arg0) {
        Log.i(C2KLogUtils.TAG_APP, "============>  onBind");
        return null;
    }

    public boolean onUnbind(Intent i) {
        Log.i(C2KLogUtils.TAG_APP, "============>  onUnbind");
        return this.mService.onUnbind(i);
    }

    public void onRebind(Intent i) {
        Log.i(C2KLogUtils.TAG_APP, "============>  onRebind");
    }

    public void onCreate() {
        Log.i(C2KLogUtils.TAG_APP, "============>  onCreate");
        this.mIsOnDestroy = false;
        this.mHandlerThread = new HandlerThread("C2KLoggerServiceThread");
        this.mHandlerThread.start();
        this.mMessageHandler = new MyHandler(this.mHandlerThread.getLooper());
        init();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_VIA_ETS_DEV_CHANGED);
        this.mService.registerReceiver(this.mBcRecverCbp, filter);
        IntentFilter filterReboot = new IntentFilter();
        filterReboot.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mService.registerReceiver(this.mRebootReceiver, filterReboot);
    }

    private void init() {
        Log.i(C2KLogUtils.TAG_APP, "============>  init()");
        new C2KLogConfig(this.mService).checkConfig();
        this.mSharedPreferences = this.mService.getSharedPreferences(C2KLogUtils.CONFIG_FILE_NAME, 0);
        this.mMdlogPath = this.mSharedPreferences.getString("ModemLogPath", "") + "/mtklog/" + C2KLogUtils.C2K_MODEM_LOG_PATH + "/";
        this.mDevicePath = this.mSharedPreferences.getString(C2KLogUtils.CONIFG_DEVICEPATH, C2KLogUtils.DEFAULT_CONIFG_DEVICEPATH);
        StringBuilder sb = new StringBuilder();
        sb.append("device path is ");
        sb.append(this.mDevicePath);
        Log.i(C2KLogUtils.TAG_APP, sb.toString());
        String filterFileName = this.mSharedPreferences.getString(C2KLogUtils.CONIFG_FILTERFILE, C2KLogUtils.DEFAULT_CONIFG_FILTERFILE);
        this.mFilterFilePath = this.mSharedPreferences.getString(C2KLogUtils.CONIFG_PATH, C2KLogUtils.DEFAULT_CONIFG_PATH) + "/" + filterFileName;
        StringBuilder sb2 = new StringBuilder();
        sb2.append("bcfg path is ");
        sb2.append(this.mFilterFilePath);
        Log.i(C2KLogUtils.TAG_APP, sb2.toString());
        this.mEtsLog = new EtsLog(this.mSharedPreferences.getInt(C2KLogUtils.CONIFG_PERLOGSIZE, 6) * 1024 * 1024, this.mCbEtsLog);
        C2KLogRecycle.getInstance().startRecycle(this.mMdlogPath, (long) this.mSharedPreferences.getInt(C2KLogUtils.CONIFG_TOTALLOGSIZE, C2KLogUtils.DEFAULT_CONIFG_TOTALLOGSIZE), this.mSharedPreferences);
    }

    public int onStartCommand() {
        BufferedReader br;
        Log.i(C2KLogUtils.TAG_APP, "============>  onStartCommand");
        try {
            if (!new File(this.mFilterFilePath).exists()) {
                Log.i(C2KLogUtils.TAG_APP, "open v5.bcfg");
                br = new BufferedReader(new InputStreamReader(this.mService.getAssets().open("v5.bcfg")), 1048576);
            } else {
                Log.i(C2KLogUtils.TAG_APP, "open " + this.mFilterFilePath);
                br = new BufferedReader(new FileReader(this.mFilterFilePath), 1048576);
            }
            String logPath = this.mMdlogPath + "MDLog3_" + translateTime(System.currentTimeMillis());
            File logFile = new File(logPath);
            if (!logFile.exists()) {
                logFile.mkdirs();
            }
            this.mSharedPreferences.edit().putString(C2KLogUtils.KEY_C2K_MODEM_LOGGING_PATH, logPath).apply();
            Log.i(C2KLogUtils.TAG_APP, "logPath " + logPath);
            C2KLogUtils.writerToFileTree(this.mMdlogPath + C2KLogUtils.LOG_TREE_FILE, logPath);
            this.mEtsLog.start(this.mDevicePath, br, logPath + "/");
            br.close();
            this.mEtsLog.stopLogRecord(false);
            setChannelForStart();
            return 3;
        } catch (IOException e) {
            Log.w(C2KLogUtils.TAG_APP, e.getMessage());
            return 3;
        }
    }

    /* access modifiers changed from: private */
    public void setChannelForStart() {
        String isReadyStr = SystemProperties.get("net.cdma.mdmstat", "");
        Log.i(C2KLogUtils.TAG_APP, "isReadyStr = " + isReadyStr);
        this.mIsModemReady = isReadyStr.equalsIgnoreCase("ready");
        Log.i(C2KLogUtils.TAG_APP, "Modem state:" + this.mIsModemReady);
        if (this.mIsModemReady) {
            setEtsChannel(MSG_SET_CHANNEL);
            this.mEtsLog.stopReadThread(false);
            this.mEtsLog.startReadThread();
            this.mEtsLog.writeEtsCfgs(true);
            this.mMonitModemThread = new Thread(new Runnable() {
                /* class com.mediatek.mtklogger.c2klogger.C2KLogService.AnonymousClass2 */

                public void run() {
                    C2KLogService.this.monitModemStatus();
                }
            });
            this.mMonitModemThread.start();
        } else if (isReadyStr.equalsIgnoreCase("exception") || isReadyStr.equalsIgnoreCase("not ready")) {
            Log.i(C2KLogUtils.TAG_APP, "Waiting modem boot up!");
            Handler handler = this.mMessageHandler;
            if (handler != null) {
                handler.sendMessageDelayed(handler.obtainMessage(MSG_SET_CHANNEL), 2000);
            }
        } else {
            onDestroy();
        }
    }

    /* access modifiers changed from: private */
    public void monitModemStatus() {
        Log.i(C2KLogUtils.TAG_APP, "----->monitModemStatus() is start!");
        while (this.mIsModemReady) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String isReadyStr = SystemProperties.get("net.cdma.mdmstat", "");
            if (isReadyStr.equalsIgnoreCase("not ready")) {
                Log.i(C2KLogUtils.TAG_APP, "isReadyStr = " + isReadyStr);
                Log.w(C2KLogUtils.TAG_APP, "Modem is not ready now!");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                if (this.mIsOnDestroy) {
                    break;
                }
                destroy();
                this.mIsOnDestroy = false;
                init();
                onStartCommand();
                this.mIsModemReady = false;
            }
        }
        Log.i(C2KLogUtils.TAG_APP, "<-----monitModemStatus() is stop!");
    }

    public void onDestroy() {
        Log.i(C2KLogUtils.TAG_APP, "============>  onDestroy");
        this.mIsOnDestroy = true;
        try {
            this.mService.unregisterReceiver(this.mBcRecverCbp);
            this.mService.unregisterReceiver(this.mRebootReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(C2KLogUtils.TAG_APP, "unregisterReceiver exception");
        }
        EtsLog etsLog = this.mEtsLog;
        if (etsLog != null) {
            etsLog.writeEtsCfgs(false);
            destroy();
            int i = 0;
            while (!this.mEtsLog.isWriteConfigDone()) {
                int i2 = i + MSG_SET_CHANNEL;
                if (i < 20) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    i = i2;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void destroy() {
        Log.i(C2KLogUtils.TAG_APP, "============>  destroy");
        this.mIsOnDestroy = true;
        this.mIsModemReady = false;
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.interrupt();
            this.mHandlerThread = null;
            this.mMessageHandler = null;
        }
        Thread thread = this.mMonitModemThread;
        if (thread != null) {
            thread.interrupt();
            this.mMonitModemThread = null;
        }
        SharedPreferences sharedPreferences = this.mSharedPreferences;
        if (sharedPreferences == null || sharedPreferences.getString(C2KLogUtils.KEY_C2K_MODEM_LOGGING_PATH, "").length() == 0) {
            Log.i(C2KLogUtils.TAG_APP, "The service has been stopped!");
        } else if (!this.mEtsLog.mStopLogrecord) {
            this.mEtsLog.stop();
            C2KLogRecycle.getInstance().stopRecycle();
            this.mSharedPreferences.edit().putString(C2KLogUtils.KEY_C2K_MODEM_LOGGING_PATH, "").apply();
            setEtsChannel(0);
            Log.i(C2KLogUtils.TAG_APP, "<============  Destroy Done!");
        }
    }

    private void setEtsChannel(int channel) {
        Log.i(C2KLogUtils.TAG_APP, "setEtsChannel channel = " + channel);
        Intent intent = new Intent(C2KLogUtils.ACTION_VIA_SET_ETS_DEV);
        intent.putExtra(C2KLogUtils.EXTRAL_VIA_ETS_DEV, channel);
        C2KLogUtils.sendBroadCast(this.mService, intent);
        if (channel != 0) {
            this.mIsSetChannelDone = false;
            int i = 0;
            while (true) {
                if (this.mIsSetChannelDone) {
                    break;
                } else if (i >= 60) {
                    Log.w(C2KLogUtils.TAG_APP, "set channel timeout" + channel);
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                        i += MSG_SET_CHANNEL;
                    } catch (InterruptedException e) {
                        Log.w(C2KLogUtils.TAG_APP, "sleep failed?");
                    }
                }
            }
            this.mIsSetChannelDone = true;
            Log.i(C2KLogUtils.TAG_APP, "set channel done, time is " + i);
        }
    }

    private String translateTime(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("00");
        calendar.setTime(new Date(time));
        int year = calendar.get(MSG_SET_CHANNEL);
        int month = calendar.get(2) + MSG_SET_CHANNEL;
        int day = calendar.get(5);
        int hour = calendar.get(11);
        int minu = calendar.get(12);
        int second = calendar.get(13);
        return "" + year + "_" + df.format((long) month) + df.format((long) day) + "_" + df.format((long) hour) + df.format((long) minu) + df.format((long) second);
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int what = msg.what;
            Log.d(C2KLogUtils.TAG_APP, "mMessageHandler, what = " + what);
            if (what == C2KLogService.MSG_SET_CHANNEL) {
                Log.i(C2KLogUtils.TAG_APP, "Re-setChannelForStart");
                C2KLogService.this.setChannelForStart();
            }
        }
    }
}
