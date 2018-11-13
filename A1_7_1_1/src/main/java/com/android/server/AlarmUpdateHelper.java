package com.android.server;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Binder;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Slog;
import android.util.Xml;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AlarmUpdateHelper extends RomUpdateHelper {
    private static final String CUSTOMIZE_FEATURE = "oppo.business.custom";
    private static final String CUSTOMIZE_LIST_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final String DATA_FILE_DIR = "data/system/sys_alarm_delay_list.xml";
    private static boolean DEBUG_PANIC = false;
    public static final int DISABLED = 0;
    public static final String FILTER_NAME = "sys_alarm_delay_list";
    public static final int NORMAL_MODE = 1;
    public static final int STRICT_MODE = 2;
    private static final String SYS_FILE_DIR = "system/etc/sys_alarm_delay_list.xml";
    private static final String TAG = "AlarmUpdateHelper";
    private final Callbacks mCallbacks;
    private ArrayList<String> mCustomizeList;
    private boolean mInteractive;
    private boolean mIsCustomVersion;
    private NetstateReceiver mNetstateReceiver;
    private long mNonInteractiveStartTime;

    interface Callbacks {
        void netStateChanged(boolean z);
    }

    private class AlarmUpdateInfo extends UpdateInfo {
        static final String ALARM_DELAY_MODE = "AlarmDelayMode";
        static final String FAST_ACTION_ARRAY = "FastActionArray";
        static final String FAST_PKG_ARRAY = "FastPkgArray";
        static final String FOREIGN_APP_ARRAY = "ForeignAppArray";
        static final String INTERVAL_RESTRICTED_ARRAY = "IntervalAppArray";
        static final String KEY_ARRAY = "KeyArray";
        static final String NET_BLACK_ARRAY = "NetBlackArray";
        static final String NET_WHITE_ARRAY = "NetWhiteArray";
        static final String PKG_WHITE_ARRAY = "PkgWhiteArray";
        private int mAlarmDelayMode = 0;
        private ArrayList<String> mConnectedPkgBlackList = new ArrayList();
        private ArrayList<String> mDisconnectedPkgWhiteList = new ArrayList();
        private ArrayList<String> mFastNoWakeupAlarmList = new ArrayList();
        private ArrayList<String> mFastNoWakeupByPkgAlarmList = new ArrayList();
        private ArrayList<String> mForeignApkList = new ArrayList();
        private ArrayList<String> mIntervalApkList = new ArrayList();
        private ArrayList<String> mKeyList = new ArrayList();
        private ArrayList<String> mPkgWhiteList = new ArrayList();

        public AlarmUpdateInfo() {
            super(AlarmUpdateHelper.this);
        }

        public void parseContentFromXML(String content) {
            IOException e;
            XmlPullParserException e2;
            Throwable th;
            if (content != null) {
                StringReader stringReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader = new StringReader(content);
                    try {
                        parser.setInput(strReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    String tmp = parser.getName();
                                    if (PKG_WHITE_ARRAY.equals(tmp)) {
                                        eventType = parser.next();
                                        this.mPkgWhiteList.add(parser.getText());
                                        break;
                                    } else if (KEY_ARRAY.equals(tmp)) {
                                        eventType = parser.next();
                                        this.mKeyList.add(parser.getText());
                                        break;
                                    } else if (NET_WHITE_ARRAY.equals(tmp)) {
                                        eventType = parser.next();
                                        this.mDisconnectedPkgWhiteList.add(parser.getText());
                                        break;
                                    } else if (NET_BLACK_ARRAY.equals(tmp)) {
                                        eventType = parser.next();
                                        this.mConnectedPkgBlackList.add(parser.getText());
                                        break;
                                    } else if (FAST_ACTION_ARRAY.equals(tmp)) {
                                        eventType = parser.next();
                                        this.mFastNoWakeupAlarmList.add(parser.getText());
                                        break;
                                    } else if (FAST_PKG_ARRAY.equals(tmp)) {
                                        eventType = parser.next();
                                        this.mFastNoWakeupByPkgAlarmList.add(parser.getText());
                                        break;
                                    } else if (FOREIGN_APP_ARRAY.equals(tmp)) {
                                        eventType = parser.next();
                                        this.mForeignApkList.add(parser.getText());
                                        break;
                                    } else if (INTERVAL_RESTRICTED_ARRAY.equals(tmp)) {
                                        eventType = parser.next();
                                        this.mIntervalApkList.add(parser.getText());
                                        break;
                                    } else if (ALARM_DELAY_MODE.equals(tmp)) {
                                        eventType = parser.next();
                                        try {
                                            this.mAlarmDelayMode = Integer.parseInt(parser.getText());
                                            break;
                                        } catch (RuntimeException e3) {
                                            this.mAlarmDelayMode = 0;
                                            break;
                                        }
                                    } else {
                                        continue;
                                    }
                                default:
                                    break;
                            }
                        }
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e4) {
                                AlarmUpdateHelper.this.log("Got execption close permReader.", e4);
                            }
                        }
                    } catch (XmlPullParserException e5) {
                        e2 = e5;
                        stringReader = strReader;
                    } catch (IOException e6) {
                        e4 = e6;
                        stringReader = strReader;
                    } catch (Throwable th2) {
                        th = th2;
                        stringReader = strReader;
                    }
                } catch (XmlPullParserException e7) {
                    e2 = e7;
                    try {
                        AlarmUpdateHelper.this.log("Got execption parsing permissions.", e2);
                        if (stringReader != null) {
                            try {
                                stringReader.close();
                            } catch (IOException e42) {
                                AlarmUpdateHelper.this.log("Got execption close permReader.", e42);
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (stringReader != null) {
                            try {
                                stringReader.close();
                            } catch (IOException e422) {
                                AlarmUpdateHelper.this.log("Got execption close permReader.", e422);
                            }
                        }
                        throw th;
                    }
                } catch (IOException e8) {
                    e422 = e8;
                    AlarmUpdateHelper.this.log("Got execption parsing permissions.", e422);
                    if (stringReader != null) {
                        try {
                            stringReader.close();
                        } catch (IOException e4222) {
                            AlarmUpdateHelper.this.log("Got execption close permReader.", e4222);
                        }
                    }
                }
            }
        }

        public boolean isImportantAlarm(PendingIntent intent) {
            if (intent == null || intent.getIntent() == null) {
                return false;
            }
            return this.mFastNoWakeupAlarmList.contains(intent.getIntent().getAction()) || this.mFastNoWakeupByPkgAlarmList.contains(intent.getTargetPackage());
        }

        public boolean convertInterval(String packageName) {
            if (packageName == null || this.mIntervalApkList == null || !this.mIntervalApkList.contains(packageName)) {
                return false;
            }
            return true;
        }

        public boolean convertType(String packageName, boolean netInteractive) {
            if (packageName == null) {
                return false;
            }
            String pkgLow = packageName.toLowerCase();
            for (String key : this.mKeyList) {
                if (pkgLow.contains(key)) {
                    return false;
                }
            }
            return netInteractive ? this.mConnectedPkgBlackList != null && this.mConnectedPkgBlackList.contains(packageName) : this.mDisconnectedPkgWhiteList == null || !this.mDisconnectedPkgWhiteList.contains(packageName);
        }

        public boolean isForeignApp(String packageName) {
            if (packageName == null || this.mForeignApkList == null || !this.mForeignApkList.contains(packageName)) {
                return false;
            }
            return true;
        }

        public int getMode() {
            return this.mAlarmDelayMode;
        }

        public String dumpToString() {
            int m;
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("AlarmDelayMode:\n");
            strBuilder.append("  Alarm Delay Mode: ").append(this.mAlarmDelayMode).append("\n");
            strBuilder.append("KeyArray:\n");
            for (m = 0; m < this.mKeyList.size(); m++) {
                strBuilder.append("  ").append((String) this.mKeyList.get(m)).append("\n");
            }
            strBuilder.append("NetWhiteArray:\n");
            for (m = 0; m < this.mDisconnectedPkgWhiteList.size(); m++) {
                strBuilder.append("  ").append((String) this.mDisconnectedPkgWhiteList.get(m)).append("\n");
            }
            strBuilder.append("NetBlackArray:\n");
            for (m = 0; m < this.mConnectedPkgBlackList.size(); m++) {
                strBuilder.append("  ").append((String) this.mConnectedPkgBlackList.get(m)).append("\n");
            }
            strBuilder.append("FastActionArray:\n");
            for (m = 0; m < this.mFastNoWakeupAlarmList.size(); m++) {
                strBuilder.append("  ").append((String) this.mFastNoWakeupAlarmList.get(m)).append("\n");
            }
            strBuilder.append("FastPkgArray:\n");
            for (m = 0; m < this.mFastNoWakeupByPkgAlarmList.size(); m++) {
                strBuilder.append("  ").append((String) this.mFastNoWakeupByPkgAlarmList.get(m)).append("\n");
            }
            strBuilder.append("ForeignAppArray:\n");
            for (m = 0; m < this.mForeignApkList.size(); m++) {
                strBuilder.append("  ").append((String) this.mForeignApkList.get(m)).append("\n");
            }
            strBuilder.append("IntervalAppList:\n");
            for (m = 0; m < this.mIntervalApkList.size(); m++) {
                strBuilder.append("  ").append((String) this.mIntervalApkList.get(m)).append("\n");
            }
            return strBuilder.toString();
        }
    }

    class NetstateReceiver extends BroadcastReceiver {
        static final int NETWORK_CONNECTED = 2;
        static final int UNKNOWN = 4;
        static final int WIFI_CONNECTED = 1;
        boolean inChina = true;
        ConnectivityManager mConnectivityManager = null;
        int mState = 0;
        TelephonyManager mTelephonyManager = null;
        State mobileState = null;
        State wifiState = null;

        public NetstateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            AlarmUpdateHelper.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                updateNetState();
            }
        }

        private void updateNetState() {
            this.mState = 0;
            this.inChina = false;
            try {
                if (this.mConnectivityManager == null) {
                    this.mConnectivityManager = (ConnectivityManager) AlarmUpdateHelper.this.mContext.getSystemService("connectivity");
                }
                if (this.mConnectivityManager != null) {
                    this.wifiState = this.mConnectivityManager.getNetworkInfo(1).getState();
                    this.mobileState = this.mConnectivityManager.getNetworkInfo(0).getState();
                    if (this.wifiState != null && State.CONNECTED == this.wifiState) {
                        this.mState |= 1;
                    }
                    if (this.mobileState != null && State.CONNECTED == this.mobileState) {
                        this.mState |= 2;
                        if (this.mTelephonyManager == null) {
                            this.mTelephonyManager = (TelephonyManager) AlarmUpdateHelper.this.mContext.getSystemService("phone");
                        }
                        if (this.mTelephonyManager.getSubscriberId().startsWith("460")) {
                            this.inChina = true;
                        }
                    }
                } else {
                    this.mState = 4;
                }
            } catch (Exception e) {
                Slog.e(AlarmUpdateHelper.TAG, "Failure updating netstate." + e);
                this.mState = 4;
            }
            if (AlarmUpdateHelper.DEBUG_PANIC) {
                Slog.v(AlarmUpdateHelper.TAG, "updateNetState, mState = " + this.mState + ", inChina = " + this.inChina);
            }
            if (this.mState != 4) {
                netStateChanged();
            }
        }

        public void interactiveStateChanged() {
            updateNetState();
        }

        public void netStateChanged() {
            boolean z = false;
            Callbacks -get1 = AlarmUpdateHelper.this.mCallbacks;
            if ((this.mState & 3) != 0) {
                z = true;
            }
            -get1.netStateChanged(z);
        }

        public boolean needBlockForeignApp() {
            return this.inChina;
        }

        public boolean getState() {
            return (this.mState & 3) != 0;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.AlarmUpdateHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.AlarmUpdateHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.AlarmUpdateHelper.<clinit>():void");
    }

    public AlarmUpdateHelper(Callbacks callbacks, Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mNonInteractiveStartTime = -1;
        this.mInteractive = true;
        this.mCustomizeList = new ArrayList();
        this.mIsCustomVersion = false;
        this.mCallbacks = callbacks;
        setUpdateInfo(new AlarmUpdateInfo(), new AlarmUpdateInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mNetstateReceiver = new NetstateReceiver();
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mIsCustomVersion = context.getPackageManager().hasSystemFeature(CUSTOMIZE_FEATURE);
        this.mCustomizeList = loadCustomizeWhiteList(CUSTOMIZE_LIST_PATH);
    }

    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00fe A:{SYNTHETIC, Splitter: B:63:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00e5 A:{SYNTHETIC, Splitter: B:55:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00cc A:{SYNTHETIC, Splitter: B:47:0x00cc} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b3 A:{SYNTHETIC, Splitter: B:39:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x009a A:{SYNTHETIC, Splitter: B:31:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x010b A:{SYNTHETIC, Splitter: B:69:0x010b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> loadCustomizeWhiteList(String path) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Throwable th;
        ArrayList<String> emptyList = new ArrayList();
        File file = new File(path);
        if (file.exists()) {
            Slog.d(TAG, "try to load customize list!!!");
            ArrayList<String> ret = new ArrayList();
            FileInputStream listFileInputStream = null;
            boolean success = false;
            try {
                FileInputStream listFileInputStream2 = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(listFileInputStream2, null);
                    String strTagP = "alarm";
                    String strTagAtt = "att";
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2 && "alarm".equals(parser.getName())) {
                            String value = parser.getAttributeValue(null, "att");
                            if (value != null) {
                                ret.add(value);
                            }
                        }
                    } while (type != 1);
                    success = true;
                    if (listFileInputStream2 != null) {
                        try {
                            listFileInputStream2.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                    }
                    listFileInputStream = listFileInputStream2;
                } catch (NullPointerException e7) {
                    e2 = e7;
                    listFileInputStream = listFileInputStream2;
                    Slog.w(TAG, "failed parsing ", e2);
                    if (listFileInputStream != null) {
                    }
                    if (success) {
                    }
                } catch (NumberFormatException e8) {
                    e3 = e8;
                    listFileInputStream = listFileInputStream2;
                    Slog.w(TAG, "failed parsing ", e3);
                    if (listFileInputStream != null) {
                    }
                    if (success) {
                    }
                } catch (XmlPullParserException e9) {
                    e4 = e9;
                    listFileInputStream = listFileInputStream2;
                    Slog.w(TAG, "failed parsing ", e4);
                    if (listFileInputStream != null) {
                    }
                    if (success) {
                    }
                } catch (IOException e10) {
                    e6 = e10;
                    listFileInputStream = listFileInputStream2;
                    Slog.w(TAG, "failed parsing ", e6);
                    if (listFileInputStream != null) {
                    }
                    if (success) {
                    }
                } catch (IndexOutOfBoundsException e11) {
                    e5 = e11;
                    listFileInputStream = listFileInputStream2;
                    try {
                        Slog.w(TAG, "failed parsing ", e5);
                        if (listFileInputStream != null) {
                        }
                        if (success) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (listFileInputStream != null) {
                            try {
                                listFileInputStream.close();
                            } catch (IOException e62) {
                                e62.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    listFileInputStream = listFileInputStream2;
                    if (listFileInputStream != null) {
                    }
                    throw th;
                }
            } catch (NullPointerException e12) {
                e2 = e12;
                Slog.w(TAG, "failed parsing ", e2);
                if (listFileInputStream != null) {
                    try {
                        listFileInputStream.close();
                    } catch (IOException e622) {
                        e622.printStackTrace();
                    }
                }
                if (success) {
                }
            } catch (NumberFormatException e13) {
                e3 = e13;
                Slog.w(TAG, "failed parsing ", e3);
                if (listFileInputStream != null) {
                    try {
                        listFileInputStream.close();
                    } catch (IOException e6222) {
                        e6222.printStackTrace();
                    }
                }
                if (success) {
                }
            } catch (XmlPullParserException e14) {
                e4 = e14;
                Slog.w(TAG, "failed parsing ", e4);
                if (listFileInputStream != null) {
                    try {
                        listFileInputStream.close();
                    } catch (IOException e62222) {
                        e62222.printStackTrace();
                    }
                }
                if (success) {
                }
            } catch (IOException e15) {
                e62222 = e15;
                Slog.w(TAG, "failed parsing ", e62222);
                if (listFileInputStream != null) {
                    try {
                        listFileInputStream.close();
                    } catch (IOException e622222) {
                        e622222.printStackTrace();
                    }
                }
                if (success) {
                }
            } catch (IndexOutOfBoundsException e16) {
                e5 = e16;
                Slog.w(TAG, "failed parsing ", e5);
                if (listFileInputStream != null) {
                    try {
                        listFileInputStream.close();
                    } catch (IOException e6222222) {
                        e6222222.printStackTrace();
                    }
                }
                if (success) {
                }
            }
            if (success) {
                return ret;
            }
            Slog.w(TAG, path + " file failed parsing!");
            return emptyList;
        }
        Slog.w(TAG, "customize file don't exist!!!");
        return emptyList;
    }

    private boolean checkWhiteList(String packageName) {
        if (this.mCustomizeList == null || this.mCustomizeList.size() <= 0 || packageName == null) {
            return false;
        }
        try {
            for (String pkg : this.mCustomizeList) {
                if (pkg.equalsIgnoreCase(packageName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "check white list has exception! ", e);
            return false;
        }
    }

    public boolean isImportantAlarm(Alarm alarm) {
        if (alarm.windowLength == 0) {
            return true;
        }
        return ((AlarmUpdateInfo) getUpdateInfo(true)).isImportantAlarm(alarm.operation);
    }

    public long convertInterval(long interval, String packageName) {
        if (interval >= 3600000 && ((AlarmUpdateInfo) getUpdateInfo(true)).convertInterval(packageName)) {
            return 3600000;
        }
        return interval;
    }

    /* JADX WARNING: Missing block: B:8:0x0017, code:
            return r10;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int convertType(int type, PendingIntent operation, String packageName) {
        AlarmUpdateInfo temp = (AlarmUpdateInfo) getUpdateInfo(true);
        if (temp == null || temp.getMode() == 0 || type == 1 || type == 3 || this.mInteractive || Binder.getCallingUid() < 10000 || SystemClock.elapsedRealtime() - this.mNonInteractiveStartTime < 1440000) {
            return type;
        }
        if (this.mIsCustomVersion && checkWhiteList(packageName)) {
            return type;
        }
        if (temp.convertType(packageName, this.mNetstateReceiver.getState())) {
            if (type == 0) {
                return 1;
            }
            if (type == 2) {
                return 3;
            }
            return type;
        } else if (this.mNetstateReceiver == null || !this.mNetstateReceiver.needBlockForeignApp() || !temp.isForeignApp(packageName)) {
            return type;
        } else {
            if (type == 0) {
                return 1;
            }
            if (type == 2) {
                return 3;
            }
            return type;
        }
    }

    public boolean isForeignApp(String packageName) {
        return ((AlarmUpdateInfo) getUpdateInfo(true)).isForeignApp(packageName);
    }

    public int getMode() {
        return ((AlarmUpdateInfo) getUpdateInfo(true)).getMode();
    }

    public String dumpToString() {
        return ((AlarmUpdateInfo) getUpdateInfo(true)).dumpToString();
    }

    public void interactiveStateChangedLocked(boolean interactive, long nowELAPSED) {
        this.mInteractive = interactive;
        this.mNonInteractiveStartTime = nowELAPSED;
    }
}
