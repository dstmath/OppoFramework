package com.android.server;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Slog;
import android.util.Xml;
import com.android.server.AlarmManagerService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.pm.RomUpdateHelper;
import com.android.server.usage.AppStandbyController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AlarmUpdateHelper extends RomUpdateHelper {
    private static final String CUSTOMIZE_FEATURE = "oppo.business.custom";
    private static final String CUSTOMIZE_LIST_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final String DATA_FILE_DIR = "data/system/sys_alarm_delay_list.xml";
    /* access modifiers changed from: private */
    public static boolean DEBUG_PANIC = false;
    public static final int DISABLED = 0;
    public static final String FILTER_NAME = "sys_alarm_delay_list";
    public static final int NORMAL_MODE = 1;
    public static final int STRICT_MODE = 2;
    private static final String SYS_FILE_DIR = "system/etc/sys_alarm_delay_list.xml";
    private static final String TAG = "AlarmUpdateHelper";
    /* access modifiers changed from: private */
    public final Callbacks mCallbacks;
    boolean mCellBroadcastAlertFeature = false;
    private ArrayList<String> mCustomizeList = new ArrayList<>();
    private boolean mInteractive = true;
    private boolean mIsCustomVersion = false;
    private NetstateReceiver mNetstateReceiver;
    private long mNonInteractiveStartTime = -1;

    /* access modifiers changed from: package-private */
    public interface Callbacks {
        void netStateChanged(boolean z);
    }

    private class AlarmUpdateInfo extends RomUpdateHelper.UpdateInfo {
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
        private ArrayList<String> mConnectedPkgBlackList = new ArrayList<>();
        private ArrayList<String> mDisconnectedPkgWhiteList = new ArrayList<>();
        private ArrayList<String> mFastNoWakeupAlarmList = new ArrayList<>();
        private ArrayList<String> mFastNoWakeupByPkgAlarmList = new ArrayList<>();
        private ArrayList<String> mForeignApkList = new ArrayList<>();
        private ArrayList<String> mIntervalApkList = new ArrayList<>();
        private ArrayList<String> mKeyList = new ArrayList<>();
        private ArrayList<String> mPkgWhiteList = new ArrayList<>();

        public AlarmUpdateInfo() {
            super();
        }

        @Override // com.android.server.pm.RomUpdateHelper.UpdateInfo
        public void parseContentFromXML(String content) {
            if (content != null) {
                FileReader xmlReader = null;
                StringReader strReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                String tmp = parser.getName();
                                if (PKG_WHITE_ARRAY.equals(tmp)) {
                                    parser.next();
                                    this.mPkgWhiteList.add(parser.getText());
                                } else if (KEY_ARRAY.equals(tmp)) {
                                    parser.next();
                                    this.mKeyList.add(parser.getText());
                                } else if (NET_WHITE_ARRAY.equals(tmp)) {
                                    parser.next();
                                    this.mDisconnectedPkgWhiteList.add(parser.getText());
                                } else if (NET_BLACK_ARRAY.equals(tmp)) {
                                    parser.next();
                                    this.mConnectedPkgBlackList.add(parser.getText());
                                } else if (FAST_ACTION_ARRAY.equals(tmp)) {
                                    parser.next();
                                    this.mFastNoWakeupAlarmList.add(parser.getText());
                                } else if (FAST_PKG_ARRAY.equals(tmp)) {
                                    parser.next();
                                    this.mFastNoWakeupByPkgAlarmList.add(parser.getText());
                                } else if (FOREIGN_APP_ARRAY.equals(tmp)) {
                                    parser.next();
                                    this.mForeignApkList.add(parser.getText());
                                } else if (INTERVAL_RESTRICTED_ARRAY.equals(tmp)) {
                                    parser.next();
                                    this.mIntervalApkList.add(parser.getText());
                                } else if (ALARM_DELAY_MODE.equals(tmp)) {
                                    parser.next();
                                    try {
                                        this.mAlarmDelayMode = Integer.parseInt(parser.getText());
                                    } catch (RuntimeException e) {
                                        this.mAlarmDelayMode = 0;
                                    }
                                }
                            }
                        }
                    }
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e2) {
                            AlarmUpdateHelper.this.log("Got execption close permReader.", e2);
                            return;
                        }
                    }
                    strReader2.close();
                } catch (XmlPullParserException e3) {
                    AlarmUpdateHelper.this.log("Got execption parsing permissions.", e3);
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e4) {
                    AlarmUpdateHelper.this.log("Got execption parsing permissions.", e4);
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (Throwable th) {
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e5) {
                            AlarmUpdateHelper.this.log("Got execption close permReader.", e5);
                            throw th;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                    throw th;
                }
            }
        }

        public boolean isImportantAlarm(PendingIntent intent) {
            if (intent == null || intent.getIntent() == null) {
                return false;
            }
            if (!this.mFastNoWakeupAlarmList.contains(intent.getIntent().getAction()) && !this.mFastNoWakeupByPkgAlarmList.contains(intent.getTargetPackage())) {
                return false;
            }
            return true;
        }

        public boolean convertInterval(String packageName) {
            ArrayList<String> arrayList;
            if (packageName == null || (arrayList = this.mIntervalApkList) == null || !arrayList.contains(packageName)) {
                return false;
            }
            return true;
        }

        public boolean convertType(String packageName, boolean netInteractive) {
            if (packageName == null) {
                return false;
            }
            String pkgLow = packageName.toLowerCase();
            Iterator<String> it = this.mKeyList.iterator();
            while (it.hasNext()) {
                if (pkgLow.contains(it.next())) {
                    return false;
                }
            }
            if (netInteractive) {
                ArrayList<String> arrayList = this.mConnectedPkgBlackList;
                if (arrayList == null || !arrayList.contains(packageName)) {
                    return false;
                }
                return true;
            }
            ArrayList<String> arrayList2 = this.mDisconnectedPkgWhiteList;
            if (arrayList2 == null || !arrayList2.contains(packageName)) {
                return true;
            }
            return false;
        }

        public boolean isForeignApp(String packageName) {
            ArrayList<String> arrayList;
            if (packageName == null || (arrayList = this.mForeignApkList) == null || !arrayList.contains(packageName)) {
                return false;
            }
            return true;
        }

        public int getMode() {
            return this.mAlarmDelayMode;
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("AlarmDelayMode:\n");
            strBuilder.append("  Alarm Delay Mode: " + this.mAlarmDelayMode + StringUtils.LF);
            strBuilder.append("KeyArray:\n");
            for (int m = 0; m < this.mKeyList.size(); m++) {
                strBuilder.append("  " + this.mKeyList.get(m) + StringUtils.LF);
            }
            strBuilder.append("NetWhiteArray:\n");
            for (int m2 = 0; m2 < this.mDisconnectedPkgWhiteList.size(); m2++) {
                strBuilder.append("  " + this.mDisconnectedPkgWhiteList.get(m2) + StringUtils.LF);
            }
            strBuilder.append("NetBlackArray:\n");
            for (int m3 = 0; m3 < this.mConnectedPkgBlackList.size(); m3++) {
                strBuilder.append("  " + this.mConnectedPkgBlackList.get(m3) + StringUtils.LF);
            }
            strBuilder.append("FastActionArray:\n");
            for (int m4 = 0; m4 < this.mFastNoWakeupAlarmList.size(); m4++) {
                strBuilder.append("  " + this.mFastNoWakeupAlarmList.get(m4) + StringUtils.LF);
            }
            strBuilder.append("FastPkgArray:\n");
            for (int m5 = 0; m5 < this.mFastNoWakeupByPkgAlarmList.size(); m5++) {
                strBuilder.append("  " + this.mFastNoWakeupByPkgAlarmList.get(m5) + StringUtils.LF);
            }
            strBuilder.append("ForeignAppArray:\n");
            for (int m6 = 0; m6 < this.mForeignApkList.size(); m6++) {
                strBuilder.append("  " + this.mForeignApkList.get(m6) + StringUtils.LF);
            }
            strBuilder.append("IntervalAppList:\n");
            for (int m7 = 0; m7 < this.mIntervalApkList.size(); m7++) {
                strBuilder.append("  " + this.mIntervalApkList.get(m7) + StringUtils.LF);
            }
            strBuilder.append("Features:\n");
            strBuilder.append("CellBroadcastAlertFeature=" + AlarmUpdateHelper.this.mCellBroadcastAlertFeature + StringUtils.LF);
            return strBuilder.toString();
        }
    }

    public AlarmUpdateHelper(Callbacks callbacks, Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
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
        this.mCellBroadcastAlertFeature = context.getPackageManager().hasSystemFeature("oppo.cellbroadcast.emergency.alert.grey");
    }

    private ArrayList<String> loadCustomizeWhiteList(String path) {
        String value;
        ArrayList<String> emptyList = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            Slog.w(TAG, "customize file don't exist!!!");
            return emptyList;
        }
        Slog.d(TAG, "try to load customize list!!!");
        ArrayList<String> ret = new ArrayList<>();
        FileInputStream listFileInputStream = null;
        boolean success = false;
        try {
            listFileInputStream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            String str = null;
            parser.setInput(listFileInputStream, null);
            while (true) {
                int type = parser.next();
                if (type == 2 && "alarm".equals(parser.getName()) && (value = parser.getAttributeValue(str, "att")) != null) {
                    ret.add(value);
                }
                if (type == 1) {
                    break;
                }
                str = null;
            }
            success = true;
            try {
                listFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e2) {
            Slog.w(TAG, "failed parsing ", e2);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (NumberFormatException e3) {
            Slog.w(TAG, "failed parsing ", e3);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "failed parsing ", e4);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (IOException e5) {
            Slog.w(TAG, "failed parsing ", e5);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (IndexOutOfBoundsException e6) {
            Slog.w(TAG, "failed parsing ", e6);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (Throwable th) {
            if (listFileInputStream != null) {
                try {
                    listFileInputStream.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            throw th;
        }
        if (success) {
            return ret;
        }
        Slog.w(TAG, path + " file failed parsing!");
        return emptyList;
    }

    private boolean checkWhiteList(String packageName) {
        ArrayList<String> arrayList = this.mCustomizeList;
        if (arrayList == null || arrayList.size() <= 0 || packageName == null) {
            return false;
        }
        try {
            Iterator<String> it = this.mCustomizeList.iterator();
            while (it.hasNext()) {
                if (it.next().equalsIgnoreCase(packageName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "check white list has exception! ", e);
            return false;
        }
    }

    private boolean checkWhiteListWithFeatures(String packageName) {
        if (packageName == null || !this.mCellBroadcastAlertFeature || !packageName.contains("com.android.cellbroadcastreceiver")) {
            return false;
        }
        return true;
    }

    public boolean isImportantAlarm(AlarmManagerService.Alarm alarm) {
        if (alarm.windowLength == 0) {
            return true;
        }
        return ((AlarmUpdateInfo) getUpdateInfo(true)).isImportantAlarm(alarm.operation);
    }

    public long convertInterval(long interval, String packageName) {
        if (interval >= AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT && ((AlarmUpdateInfo) getUpdateInfo(true)).convertInterval(packageName)) {
            return AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
        }
        return interval;
    }

    public int convertType(int type, PendingIntent operation, String packageName) {
        AlarmUpdateInfo temp = (AlarmUpdateInfo) getUpdateInfo(true);
        if (temp == null || temp.getMode() == 0 || type == 1 || type == 3 || this.mInteractive || Binder.getCallingUid() < 10000 || SystemClock.elapsedRealtime() - this.mNonInteractiveStartTime < 1440000) {
            return type;
        }
        if ((this.mIsCustomVersion && checkWhiteList(packageName)) || checkWhiteListWithFeatures(packageName)) {
            return type;
        }
        NetstateReceiver netstateReceiver = this.mNetstateReceiver;
        if (netstateReceiver == null || !temp.convertType(packageName, netstateReceiver.getState())) {
            NetstateReceiver netstateReceiver2 = this.mNetstateReceiver;
            if (netstateReceiver2 == null || !netstateReceiver2.needBlockForeignApp() || !temp.isForeignApp(packageName)) {
                return type;
            }
            if (type == 0) {
                return 1;
            }
            if (type == 2) {
                return 3;
            }
            return type;
        } else if (type == 0) {
            return 1;
        } else {
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

    class NetstateReceiver extends BroadcastReceiver {
        static final int NETWORK_CONNECTED = 2;
        static final int UNKNOWN = 4;
        static final int WIFI_CONNECTED = 1;
        boolean inChina = true;
        ConnectivityManager mConnectivityManager = null;
        int mState = 0;
        TelephonyManager mTelephonyManager = null;
        NetworkInfo.State mobileState = null;
        NetworkInfo.State wifiState = null;

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
                    if (this.wifiState != null && NetworkInfo.State.CONNECTED == this.wifiState) {
                        this.mState |= 1;
                    }
                    if (this.mobileState != null && NetworkInfo.State.CONNECTED == this.mobileState) {
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
            AlarmUpdateHelper.this.mCallbacks.netStateChanged((this.mState & 3) != 0);
        }

        public boolean needBlockForeignApp() {
            return this.inChina;
        }

        public boolean getState() {
            return (this.mState & 3) != 0;
        }
    }
}
