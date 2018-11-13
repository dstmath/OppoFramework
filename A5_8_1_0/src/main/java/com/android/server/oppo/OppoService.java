package com.android.server.oppo;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IOppoService.Stub;
import android.os.OppoManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.LocationManagerService;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class OppoService extends Stub {
    private static final String DATA_FILE_DIR = "data/system/criticallog_config.xml";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_FLASH_LIGHT = true;
    public static final int DELAY_TIME = 36000000;
    public static final String FILTER_NAME = "criticallog_config";
    private static final String SYS_FILE_DIR = "system/etc/criticallog_config.xml";
    private static final String TAG = "OppoService";
    private Context mContext;
    OppoFallingMonitor mFallingMonitor;
    private FlashLightControler mFlashLightControler = null;
    private Handler mHandler = new Handler();
    OppoLogService mLogService;
    private NetWakeManager mNetWakeManager;
    CriticalLogConfigUpdateHelper mXmlHelper;

    class CriticalLogConfigUpdateHelper extends RomUpdateHelper {
        private static final int CONST_THREE = 3;
        public static final String FALLING_MONITOR = "falling_monitor_switch";
        public static final String VERSION_NAME = "version";
        CriticalLogUpdateInfo mCurrentInfo = new CriticalLogUpdateInfo();
        CriticalLogUpdateInfo mNewVersionInfo = new CriticalLogUpdateInfo();

        private class CriticalLogUpdateInfo extends UpdateInfo {
            private boolean mIsFallingSwitch = false;
            private boolean mIsUpdateToLowerVersion = false;

            public CriticalLogUpdateInfo() {
                super(CriticalLogConfigUpdateHelper.this);
            }

            public boolean getFallingSwitch() {
                Slog.v(OppoService.TAG, "mIsFallingSwitch :" + this.mIsFallingSwitch);
                return this.mIsFallingSwitch;
            }

            private void updateConfigVersion(String type, String value) {
                Slog.d(OppoService.TAG, hashCode() + " updateConfigVersion, type = " + type + ", value = " + value);
                if ("version".equals(type)) {
                    this.mVersion = (long) Integer.parseInt(value);
                }
            }

            public boolean updateToLowerVersion(String content) {
                long newVersion = getContentVersion(content);
                getFallingSwitchNewVersion(content);
                Slog.d(OppoService.TAG, "upateToLowerVersion, newVersion = " + newVersion + ", mVersion = " + this.mVersion);
                this.mIsUpdateToLowerVersion = newVersion < this.mVersion;
                return this.mIsUpdateToLowerVersion;
            }

            public boolean isUpdateToLowerVersion() {
                return this.mIsUpdateToLowerVersion;
            }

            /* JADX WARNING: Removed duplicated region for block: B:41:0x008d A:{SYNTHETIC, Splitter: B:41:0x008d} */
            /* JADX WARNING: Removed duplicated region for block: B:19:0x0053 A:{SYNTHETIC, Splitter: B:19:0x0053} */
            /* JADX WARNING: Removed duplicated region for block: B:32:0x0072 A:{SYNTHETIC, Splitter: B:32:0x0072} */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void parseContentFromXML(String content) {
                IOException e;
                XmlPullParserException e2;
                Throwable th;
                if (content != null) {
                    StringReader strReader = null;
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        StringReader strReader2 = new StringReader(content);
                        try {
                            parser.setInput(strReader2);
                            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                                switch (eventType) {
                                    case 2:
                                        char[] typeChar = parser.getName().toCharArray();
                                        if (typeChar.length <= 3) {
                                            break;
                                        }
                                        eventType = parser.next();
                                        updateConfigVersion(String.valueOf(typeChar), parser.getText());
                                        parserFallingMonitorSwitch(String.valueOf(typeChar), parser.getText());
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (strReader2 != null) {
                                try {
                                    strReader2.close();
                                } catch (IOException e3) {
                                    CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e3);
                                }
                            }
                        } catch (XmlPullParserException e4) {
                            e2 = e4;
                            strReader = strReader2;
                            try {
                                CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e2);
                                if (strReader != null) {
                                    try {
                                        strReader.close();
                                    } catch (IOException e32) {
                                        CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e32);
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (strReader != null) {
                                }
                                throw th;
                            }
                        } catch (IOException e5) {
                            e32 = e5;
                            strReader = strReader2;
                            CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e32);
                            if (strReader != null) {
                                try {
                                    strReader.close();
                                } catch (IOException e322) {
                                    CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e322);
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            strReader = strReader2;
                            if (strReader != null) {
                                try {
                                    strReader.close();
                                } catch (IOException e3222) {
                                    CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e3222);
                                }
                            }
                            throw th;
                        }
                    } catch (XmlPullParserException e6) {
                        e2 = e6;
                        CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e2);
                        if (strReader != null) {
                        }
                    } catch (IOException e7) {
                        e3222 = e7;
                        CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e3222);
                        if (strReader != null) {
                        }
                    }
                }
            }

            void parserFallingMonitorSwitch(String type, String value) {
                if (type != null && type.equals(CriticalLogConfigUpdateHelper.FALLING_MONITOR)) {
                    if (value == null || !value.equals("true")) {
                        Slog.v(OppoService.TAG, "parserFallingMonitorSwitch false");
                        this.mIsFallingSwitch = false;
                        return;
                    }
                    Slog.v(OppoService.TAG, "parserFallingMonitorSwitch true");
                    this.mIsFallingSwitch = true;
                }
            }

            /* JADX WARNING: Removed duplicated region for block: B:19:0x0049 A:{SYNTHETIC, Splitter: B:19:0x0049} */
            /* JADX WARNING: Removed duplicated region for block: B:32:0x0068 A:{SYNTHETIC, Splitter: B:32:0x0068} */
            /* JADX WARNING: Removed duplicated region for block: B:41:0x0083 A:{SYNTHETIC, Splitter: B:41:0x0083} */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private boolean getFallingSwitchNewVersion(String content) {
                IOException e;
                XmlPullParserException e2;
                Throwable th;
                if (content == null) {
                    return false;
                }
                StringReader strReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    try {
                        parser.setInput(strReader2);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    char[] typeChar = parser.getName().toCharArray();
                                    if (typeChar.length <= 3) {
                                        break;
                                    }
                                    eventType = parser.next();
                                    parserFallingMonitorSwitch(String.valueOf(typeChar), parser.getText());
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (strReader2 != null) {
                            try {
                                strReader2.close();
                            } catch (IOException e3) {
                                CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e3);
                            }
                        }
                        return true;
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        strReader = strReader2;
                        try {
                            CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e2);
                            if (strReader != null) {
                            }
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            if (strReader != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e5) {
                        e3 = e5;
                        strReader = strReader2;
                        CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e3);
                        if (strReader != null) {
                        }
                        return false;
                    } catch (Throwable th3) {
                        th = th3;
                        strReader = strReader2;
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e32) {
                                CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e32);
                            }
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e322) {
                            CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e322);
                        }
                    }
                    return false;
                } catch (IOException e7) {
                    e322 = e7;
                    CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e322);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e3222) {
                            CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e3222);
                        }
                    }
                    return false;
                }
            }

            /* JADX WARNING: Removed duplicated region for block: B:15:0x0026 A:{SYNTHETIC, Splitter: B:15:0x0026} */
            /* JADX WARNING: Removed duplicated region for block: B:41:0x00b4 A:{SYNTHETIC, Splitter: B:41:0x00b4} */
            /* JADX WARNING: Removed duplicated region for block: B:31:0x008f A:{SYNTHETIC, Splitter: B:31:0x008f} */
            /* JADX WARNING: Removed duplicated region for block: B:48:0x00cd A:{SYNTHETIC, Splitter: B:48:0x00cd} */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private long getContentVersion(String content) {
                IOException e;
                XmlPullParserException e2;
                Throwable th;
                long version = -1;
                if (content == null) {
                    return -1;
                }
                StringReader strReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    try {
                        parser.setInput(strReader2);
                        boolean found = false;
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if ("version".equals(parser.getName())) {
                                        eventType = parser.next();
                                        Slog.d(OppoService.TAG, "eventType = " + eventType + ", text = " + parser.getText());
                                        version = (long) Integer.parseInt(parser.getText());
                                        found = true;
                                        break;
                                    }
                                    break;
                            }
                            if (found) {
                                if (strReader2 != null) {
                                    try {
                                        strReader2.close();
                                    } catch (IOException e3) {
                                        CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e3);
                                    }
                                }
                                return version;
                            }
                        }
                        if (strReader2 != null) {
                        }
                        return version;
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        strReader = strReader2;
                        CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e2);
                        if (strReader != null) {
                        }
                        return -1;
                    } catch (IOException e5) {
                        e3 = e5;
                        strReader = strReader2;
                        try {
                            CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e3);
                            if (strReader != null) {
                            }
                            return -1;
                        } catch (Throwable th2) {
                            th = th2;
                            if (strReader != null) {
                                try {
                                    strReader.close();
                                } catch (IOException e32) {
                                    CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e32);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        strReader = strReader2;
                        if (strReader != null) {
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e322) {
                            CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e322);
                        }
                    }
                    return -1;
                } catch (IOException e7) {
                    e322 = e7;
                    CriticalLogConfigUpdateHelper.this.log("Got execption parsing permissions.", e322);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e3222) {
                            CriticalLogConfigUpdateHelper.this.log("Got execption close permReader.", e3222);
                        }
                    }
                    return -1;
                }
            }
        }

        public CriticalLogConfigUpdateHelper(Context context, String filterName, String systemFile, String dataFile) {
            super(context, filterName, systemFile, dataFile);
            setUpdateInfo(this.mCurrentInfo, this.mNewVersionInfo);
        }

        public void getUpdateFromProvider() {
            super.getUpdateFromProvider();
            if (this.mCurrentInfo.isUpdateToLowerVersion()) {
                Log.v(OppoService.TAG, "update criticallog UpdateToLowerVersion do nothing");
                return;
            }
            OppoManager.updateConfig();
            if (this.mCurrentInfo.getFallingSwitch()) {
                Slog.v(OppoService.TAG, "new version falling monitor true");
                OppoService.this.startFallingMonitor();
            } else {
                Slog.v(OppoService.TAG, "new version falling monitor false");
                OppoService.this.stopFallingMonitor();
            }
            Log.v(OppoService.TAG, "update criticallog config");
        }

        public void initFallingMonitor() {
            if (this.mCurrentInfo.getFallingSwitch()) {
                Slog.v(OppoService.TAG, "initFallingMonitor start monitor ");
                OppoService.this.startFallingMonitor();
                return;
            }
            Slog.v(OppoService.TAG, "initFallingMonitor stop monitor ");
            OppoService.this.stopFallingMonitor();
        }
    }

    private class FlashLightControler {
        private static final String FLASH_LIGHT_DRIVER_NODE = "/proc/qcom_flash";
        private static final String FLASH_LIGHT_MODE_CLOSE = "0";
        private static final String FLASH_LIGHT_MODE_OPEN = "1";

        public boolean openFlashLightImpl() {
            return writeValueToFlashLightNode("1");
        }

        public boolean closeFlashLightImpl() {
            return writeValueToFlashLightNode(FLASH_LIGHT_MODE_CLOSE);
        }

        public String getFlashLightStateImpl() {
            return getCurrentFlashLightState();
        }

        private boolean writeValueToFlashLightNode(String value) {
            IOException e;
            Slog.d(OppoService.TAG, "writeValueToFlashLightNode, new value:" + value);
            if (value == null || value.length() <= 0) {
                Slog.w(OppoService.TAG, "writeValueToFlashLightNode:value unavailable!");
                return false;
            }
            try {
                FileWriter nodeFileWriter = new FileWriter(new File(FLASH_LIGHT_DRIVER_NODE));
                try {
                    nodeFileWriter.write(value);
                    nodeFileWriter.close();
                    Slog.d(OppoService.TAG, "write flashLight node succeed!");
                    return true;
                } catch (IOException e2) {
                    e = e2;
                    e.printStackTrace();
                    Slog.e(OppoService.TAG, "write flashLight node failed!");
                    return false;
                }
            } catch (IOException e3) {
                e = e3;
                e.printStackTrace();
                Slog.e(OppoService.TAG, "write flashLight node failed!");
                return false;
            }
        }

        private String getCurrentFlashLightState() {
            char[] valueArray = new char[10];
            String result = "";
            try {
                FileReader nodeFileReader = new FileReader(new File(FLASH_LIGHT_DRIVER_NODE));
                nodeFileReader.read(valueArray);
                result = new String(valueArray).trim();
                nodeFileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                Slog.e(OppoService.TAG, "read flashLight node failed!");
            }
            Slog.d(OppoService.TAG, "getCurrentFlashLightState:" + result);
            return result;
        }
    }

    private native void native_finalizeRawPartition();

    private native boolean native_initRawPartition();

    private native String native_readCriticalData(int i, int i2);

    private native String native_readRawPartition(int i, int i2);

    private native int native_writeCriticalData(int i, String str);

    private native int native_writeRawPartition(String str);

    public OppoService(Context context) {
        this.mContext = context;
        this.mFallingMonitor = new OppoFallingMonitor(this.mContext);
        this.mNetWakeManager = new NetWakeManager(context);
        this.mNetWakeManager.CoverObservse_init();
        RegisterXmlUpdate(this.mContext);
        SyncCacheToEmmcTimmer();
        this.mLogService = new OppoLogService(this.mContext);
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            startSensorLog(true);
        }
        checkRebootStatus();
    }

    void checkRebootStatus() {
        String bootReason = readBootReason("/sys/power/app_boot");
        String ftmMode = readBootReason("/sys/systeminfo/ftmmode");
        String silence = SystemProperties.get("persist.sys.oppo.silence", "");
        String fatal = SystemProperties.get("persist.sys.oppo.fatal", "");
        Slog.v(TAG, "bootReason = " + bootReason + "ftmMode = " + ftmMode + " silence = " + silence + "fatal = " + fatal);
        if (bootReason.equals("kernel") || fatal.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || silence.trim().equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || ftmMode.equals("8")) {
            Slog.v(TAG, "do nothing");
        } else if (SystemProperties.getLong("ro.runtime.firstboot", 0) == 0) {
            SystemProperties.set("sys.oppo.reboot", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            Slog.v(TAG, "set sys.oppo.reboot 1");
        }
        SystemProperties.set("persist.sys.oppo.silence", "0");
    }

    private static String readBootReason(String path) {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(path);
            int length = fin.available();
            if (length != 0) {
                byte[] buffer = new byte[length];
                fin.read(buffer);
                res = new StringBuffer().append(new String(buffer)).toString().trim();
            }
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void recordCriticalEvent(int msg, int pid, String log) {
        switch (msg) {
            case 1004:
                OppoManager.writeLogToPartition(OppoManager.TYPE_ANDROID_INPUTMETHOD_FAIL, log, "ANDROID", "inputmethod_fail", this.mContext.getString(17040989));
                return;
            default:
                return;
        }
    }

    public String readRawPartition(int offset, int size) {
        return native_readRawPartition(offset, size);
    }

    public int writeRawPartition(String content) {
        return native_writeRawPartition(content);
    }

    public String readCriticalData(int id, int size) {
        return native_readCriticalData(id, size);
    }

    public int writeCriticalData(int id, String content) {
        return native_writeCriticalData(id, content);
    }

    void startFallingMonitor() {
        if (this.mFallingMonitor != null) {
            this.mFallingMonitor.startMonitor();
        }
    }

    void stopFallingMonitor() {
        if (this.mFallingMonitor != null) {
            this.mFallingMonitor.stopMonitor();
        }
    }

    void RegisterXmlUpdate(Context c) {
        this.mXmlHelper = new CriticalLogConfigUpdateHelper(c, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mXmlHelper.init();
        this.mXmlHelper.initUpdateBroadcastReceiver();
        this.mXmlHelper.initFallingMonitor();
    }

    void SyncCacheToEmmcTimmer() {
        Log.v(TAG, "syncCacheToEmmc , start timmer sync ");
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                Log.v(OppoService.TAG, "syncCacheToEmmc , timmer sync ");
                OppoManager.syncCacheToEmmc();
                OppoService.this.mHandler.postDelayed(this, 36000000);
            }
        }, 36000000);
    }

    public void systemReady() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                Log.v(OppoService.TAG, "systemReady initLogCoreService");
                if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    OppoService.this.mLogService.initLogCoreService();
                }
            }
        }, 20000);
    }

    public String getOppoLogInfoString(int index) {
        if (Binder.getCallingUid() != 1000) {
            return null;
        }
        return this.mLogService.getOppoLogInfoString(index);
    }

    public void assertKernelPanic() {
        int uid = Binder.getCallingUid();
        Log.d(TAG, "pid " + Binder.getCallingPid() + " call assertKernelPanic");
        if (uid != 1000) {
            Log.e(TAG, "not allowed to do that");
        } else {
            this.mLogService.assertKernelPanic();
        }
    }

    public void deleteSystemLogFile() {
        if (Binder.getCallingUid() == 1000) {
            this.mLogService.deleteSystemLogFile();
        }
    }

    public boolean iScoreLogServiceRunning() {
        if (this.mLogService == null) {
            return false;
        }
        boolean result = this.mLogService.isLogCoreServiceRunning();
        Log.v(TAG, "LogCoreService Running : " + result);
        return result;
    }

    public void StartLogCoreService() {
        Log.v(TAG, "StartLogCoreService : " + this.mLogService);
        if (this.mLogService == null) {
            this.mLogService = new OppoLogService(this.mContext);
        }
        this.mLogService.initLogCoreService();
    }

    public void unbindCoreLogService() {
        this.mLogService.unbindService();
    }

    public void startSensorLog(boolean isOutPutFile) {
        this.mLogService.startSensorLog(isOutPutFile);
    }

    public void stopSensorLog() {
        this.mLogService.stopSensorLog();
    }

    public boolean openFlashLight() {
        return getFlashLightControler().openFlashLightImpl();
    }

    public boolean closeFlashLight() {
        return getFlashLightControler().closeFlashLightImpl();
    }

    public String getFlashLightState() {
        return getFlashLightControler().getFlashLightStateImpl();
    }

    private FlashLightControler getFlashLightControler() {
        if (this.mFlashLightControler == null) {
            this.mFlashLightControler = new FlashLightControler();
        }
        return this.mFlashLightControler;
    }
}
