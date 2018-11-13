package com.android.server.net;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Slog;
import com.android.server.NetworkManagementSocketTagger;
import com.android.server.display.OppoBrightUtils;
import com.android.server.usage.UnixCalendar;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class NetworkLostStatUtil {
    private static final String ACTION_SYSTEM_TIME_SET = "android.intent.action.TIME_SET";
    private static final String BROADCAST_ACTION_LOST_STAT_ALARM = "oppo.intent.action.LOST_STAT_ALARM";
    private static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final int CLEAR_ALARM_TIME = 3;
    private static final String COLUMN_NAME_XML = "xml";
    private static final Uri CONTENT_URI = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String DATA_SHUTDOWN_ENABLE_TAG = "DataShutdownEnable";
    private static int DATA_SHUTDOWN_TIME = OppoBrightUtils.HIGH_BRIGHTNESS_LUX_STEP;
    private static final String DATA_SHUTDOWN_TIME_TAG = "DataShutdownTime";
    private static final String DCS_EVENTID = "LostStatEventId";
    private static final String DCS_LOGTAG = "2012001";
    private static final String LOST_STAT_KEY = "lost_stat_config";
    private static final int LS_CLOSE_ENABLE = 1;
    private static final int LS_ENABLE = 0;
    private static final int LS_MAX = 7;
    private static final int LS_REPORT_THRESHOLD = 3;
    private static final int LS_TIMEOUT = 2;
    private static final int LS_UPLOAD_LOG = 4;
    private static final int LS_UPLOAD_LOG_THRESHOLD = 5;
    private static final int LS_WIFI_ENABLE = 6;
    private static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String TAG = "NetworkLostStatUtil";
    private static final String TAG_CLOSE_SOCKET = "CloseEnable";
    private static final String TAG_ENABLE = "Enable";
    private static final String TAG_LOST_STAT_WIFI_ENABLE = "WifiLost";
    private static final String TAG_REPORT_THRESH = "ReportThresh";
    private static final String TAG_TIMEOUT_THRESH = "TimeOutThresh";
    private static final String TAG_UPLOAD_LOG = "UploadEnable";
    private static final String TAG_UPLOAD_TIME_THRESH = "UploadTimeThresh";
    private static final String UEVENT_MSG = "LOST_STAT=LOST";
    private static int mDataShutdownEnable = 1;
    private static NetworkLostStatUtil mInstall = null;
    final Object lock = new Object();
    private Context mContext = null;
    private UEventObserver mLostStatObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            String mIp = event.get("SIP");
            String mPort = event.get("SPORT");
            String mProtocol = event.get("PROTOCOL");
            String mRx = event.get("RX");
            String mTx = event.get("TX");
            String mUid = event.get("UID");
            String mClassify = event.get("CLASSIFY");
            String mNetworkType = NetworkLostStatUtil.this.getCurrentNetworkType();
            Slog.d(NetworkLostStatUtil.TAG, "ip: " + mIp + " port: " + mPort + " protocol: " + mProtocol + " rx: " + mRx + " tx: " + mTx + " type: " + mNetworkType + " Classify " + mClassify);
            if (NetworkLostStatUtil.this.mUsbConnected) {
                Slog.d(NetworkLostStatUtil.TAG, "network lost reprot, but usb connect is enable, return");
                return;
            }
            try {
                Map<String, String> logMap = new HashMap();
                logMap.put("ip", mIp);
                logMap.put("port", mPort);
                logMap.put("protocol", mProtocol);
                logMap.put("rx", mRx);
                logMap.put("tx", mTx);
                logMap.put(SoundModelContract.KEY_TYPE, mNetworkType);
                if (mUid != null) {
                    int uid = Integer.parseInt(mUid);
                    if (uid != 0) {
                        String packageName = NetworkLostStatUtil.this.mPackageManager.getNameForUid(uid);
                        if (packageName != null) {
                            Slog.d(NetworkLostStatUtil.TAG, "packageName " + packageName);
                            logMap.put("packageName", packageName);
                        }
                    }
                }
                logMap.put("classify", mClassify);
                if (NetworkLostStatUtil.this.mPowerManager.isScreenOn()) {
                    logMap.put("screen", "on");
                } else {
                    logMap.put("screen", "off");
                }
                if (NetworkLostStatUtil.mDataShutdownEnable == 1) {
                    new Thread(new networkMarRunnable(NetworkLostStatUtil.this.mContext)).start();
                }
            } catch (Exception e) {
                Slog.e(NetworkLostStatUtil.TAG, "send DCS message error", e);
            }
        }
    };
    private BroadcastReceiver mLostStatReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS")) {
                ArrayList<String> changeTableNameList = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                if (changeTableNameList != null && (changeTableNameList.isEmpty() ^ 1) != 0 && changeTableNameList.contains(NetworkLostStatUtil.LOST_STAT_KEY)) {
                    new Thread(new ConfigRunnable(context)).start();
                }
            } else if (action.equals(NetworkLostStatUtil.ACTION_SYSTEM_TIME_SET) || action.equals(NetworkLostStatUtil.ACTION_SYSTEM_TIME_SET)) {
                Slog.d(NetworkLostStatUtil.TAG, "time change intent " + intent);
                new Thread(new Runnable() {
                    public void run() {
                        NetworkLostStatUtil.this.scheduleAlarm();
                    }
                }).start();
            } else if (action.equals(NetworkLostStatUtil.BROADCAST_ACTION_LOST_STAT_ALARM)) {
                Slog.d(NetworkLostStatUtil.TAG, "alarm is wakeup, clear statistics rbtree");
                new Thread(new Runnable() {
                    public void run() {
                        NetworkManagementSocketTagger.clearLostStatParams();
                    }
                }).start();
            } else if (action.equals("android.hardware.usb.action.USB_STATE")) {
                NetworkLostStatUtil.this.mUsbConnected = intent.getBooleanExtra("connected", false);
                Slog.d(NetworkLostStatUtil.TAG, "usb connect change to " + NetworkLostStatUtil.this.mUsbConnected);
            }
        }
    };
    private PackageManager mPackageManager;
    private PowerManager mPowerManager;
    private TelephonyManager mTelephonyManager;
    private boolean mUsbConnected = false;

    private class BackupRunnable implements Runnable {
        private Context sContext;

        public BackupRunnable(Context context) {
            this.sContext = context;
        }

        public void run() {
            Slog.d(NetworkLostStatUtil.TAG, "BackupRunnable update");
            String data = NetworkLostStatUtil.this.getDataFromProvider(this.sContext, NetworkLostStatUtil.LOST_STAT_KEY);
            if (data != null) {
                Slog.d(NetworkLostStatUtil.TAG, "BackupRunnable update data " + data);
                NetworkLostStatUtil.this.parserConfigXmlValue(data);
                NetworkLostStatUtil.this.backupXmlConfig(data);
                return;
            }
            String config = NetworkLostStatUtil.this.loadBackXmlConfig();
            if (config != null && (config.isEmpty() ^ 1) != 0) {
                Slog.d(NetworkLostStatUtil.TAG, "backup config is " + config);
                NetworkLostStatUtil.this.parserConfigXmlValue(config);
            }
        }
    }

    private class ConfigRunnable implements Runnable {
        private Context sContext;

        public ConfigRunnable(Context context) {
            this.sContext = context;
        }

        public void run() {
            Slog.d(NetworkLostStatUtil.TAG, "ConfigRunnable update");
            String data = NetworkLostStatUtil.this.getDataFromProvider(this.sContext, NetworkLostStatUtil.LOST_STAT_KEY);
            if (data != null) {
                Slog.d(NetworkLostStatUtil.TAG, "ConfigRunnable update data " + data);
                NetworkLostStatUtil.this.parserConfigXmlValue(data);
                NetworkLostStatUtil.this.backupXmlConfig(data);
            }
        }
    }

    private class networkMarRunnable implements Runnable {
        private Context sContext;

        public networkMarRunnable(Context context) {
            this.sContext = context;
        }

        public void run() {
            synchronized (NetworkLostStatUtil.this.lock) {
                if (NetworkLostStatUtil.this.mTelephonyManager == null) {
                    return;
                }
                Slog.d(NetworkLostStatUtil.TAG, "networkMarThread running");
                try {
                    boolean neworkEnable = NetworkLostStatUtil.this.mTelephonyManager.getDataEnabled();
                    if (neworkEnable) {
                        Slog.d(NetworkLostStatUtil.TAG, "disable data network");
                        NetworkLostStatUtil.this.mTelephonyManager.setDataEnabled(neworkEnable ^ 1);
                        Thread.sleep((long) NetworkLostStatUtil.DATA_SHUTDOWN_TIME);
                        neworkEnable = NetworkLostStatUtil.this.mTelephonyManager.getDataEnabled();
                        if (!neworkEnable) {
                            Slog.d(NetworkLostStatUtil.TAG, "enable data network");
                            NetworkLostStatUtil.this.mTelephonyManager.setDataEnabled(neworkEnable ^ 1);
                        }
                        Slog.d(NetworkLostStatUtil.TAG, "exist networkMarThread");
                        return;
                    }
                    Slog.d(NetworkLostStatUtil.TAG, "exist networkMarThread");
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private NetworkLostStatUtil(Context context) {
        this.mContext = context;
        registBroadcast();
        startObserving();
        this.mTelephonyManager = TelephonyManager.from(context);
        this.mPackageManager = context.getPackageManager();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        new Thread(new BackupRunnable(context)).start();
        scheduleAlarm();
    }

    public static NetworkLostStatUtil getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new NetworkLostStatUtil(context);
        }
        return mInstall;
    }

    private void scheduleAlarm() {
        AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(BROADCAST_ACTION_LOST_STAT_ALARM), 0);
        am.cancel(pendingIntent);
        long firstTime = SystemClock.elapsedRealtime();
        Slog.d(TAG, "set alarm");
        Time now = new Time();
        now.setToNow();
        Slog.d(TAG, "current hour " + now.hour);
        int elapsedHour = (24 - now.hour) + 3;
        Slog.d(TAG, "scheduled alarm after " + elapsedHour + " hours");
        am.setRepeating(3, firstTime + (((long) elapsedHour) * 3600000), UnixCalendar.DAY_IN_MILLIS, pendingIntent);
    }

    private void registBroadcast() {
        IntentFilter lostStatFilter = new IntentFilter();
        lostStatFilter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        lostStatFilter.addAction(ACTION_SYSTEM_TIME_SET);
        lostStatFilter.addAction(ACTION_SYSTEM_TIME_SET);
        lostStatFilter.addAction(BROADCAST_ACTION_LOST_STAT_ALARM);
        lostStatFilter.addAction("android.hardware.usb.action.USB_STATE");
        this.mContext.registerReceiver(this.mLostStatReceiver, lostStatFilter);
    }

    private void startObserving() {
        this.mLostStatObserver.startObserving(UEVENT_MSG);
    }

    private String getCurrentNetworkType() {
        int networkType = this.mTelephonyManager.getNetworkType();
        TelephonyManager telephonyManager = this.mTelephonyManager;
        int classType = TelephonyManager.getNetworkClass(networkType);
        String type = Shell.NIGHT_MODE_STR_UNKNOWN;
        switch (classType) {
            case 0:
                return Shell.NIGHT_MODE_STR_UNKNOWN;
            case 1:
                return "2G";
            case 2:
                return "3G";
            case 3:
                return "4G";
            default:
                return type;
        }
    }

    private String getDataFromProvider(Context context, String filterName) {
        Cursor cursor = null;
        String xmlValue = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI, new String[]{COLUMN_NAME_XML}, "filtername=\"" + filterName + "\"", null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                Slog.d(TAG, "lost_stat filter cursor is null");
            } else {
                int xmlColumnIndex = cursor.getColumnIndex(COLUMN_NAME_XML);
                cursor.moveToNext();
                xmlValue = cursor.getString(xmlColumnIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
            return xmlValue;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            Slog.e(TAG, "We can not get Filtrate lost stat data from provider,because of " + e);
            return null;
        }
    }

    private boolean parserConfigXmlValue(String xmlValue) {
        if (xmlValue == null || xmlValue.isEmpty()) {
            return false;
        }
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(xmlValue));
            parser.nextTag();
            int type;
            do {
                type = parser.next();
                if (type == 2) {
                    String tag = parser.getName();
                    String value;
                    if (TAG_ENABLE.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            updateLostStatParams(0, value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (TAG_CLOSE_SOCKET.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            updateLostStatParams(1, value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (TAG_TIMEOUT_THRESH.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            updateLostStatParams(2, value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (TAG_REPORT_THRESH.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            updateLostStatParams(3, value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (TAG_UPLOAD_LOG.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            updateLostStatParams(4, value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (TAG_UPLOAD_TIME_THRESH.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            updateLostStatParams(5, value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (TAG_LOST_STAT_WIFI_ENABLE.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            updateLostStatParams(6, value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (DATA_SHUTDOWN_TIME_TAG.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            DATA_SHUTDOWN_TIME = Integer.parseInt(value);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (DATA_SHUTDOWN_ENABLE_TAG.equals(tag)) {
                        value = parser.nextText();
                        if (value != null) {
                            mDataShutdownEnable = Integer.parseInt(value);
                            continue;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } while (type != 1);
            return true;
        } catch (NullPointerException e) {
            while (true) {
                Slog.e(TAG, "failed parsing " + e);
                return false;
            }
        } catch (NumberFormatException e2) {
            Slog.e(TAG, "failed parsing " + e2);
            return false;
        } catch (XmlPullParserException e3) {
            Slog.e(TAG, "failed parsing " + e3);
            return false;
        } catch (IOException e4) {
            Slog.e(TAG, "failed parsing " + e4);
            return false;
        } catch (IndexOutOfBoundsException e5) {
            Slog.e(TAG, "failed parsing " + e5);
            return false;
        } catch (Throwable th) {
            return false;
        }
    }

    private void updateLostStatParams(int index, String value) {
        int res = NetworkManagementSocketTagger.setLostStatParams(index, Long.parseLong(value));
        if (res != 0) {
            Slog.e(TAG, "updata params error " + res);
        }
    }

    private File getDefaultSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    private void backupXmlConfig(String xmlValue) {
        try {
            File baseDir = new File(getDefaultSystemDir(), "netloststat");
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            File file = new File(baseDir, "lost_stat_config.xml");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(xmlValue.getBytes());
            fos.close();
        } catch (Exception e) {
            Slog.e(TAG, "write xml config failed " + e);
        }
    }

    private String loadBackXmlConfig() {
        Exception e;
        String res = null;
        File baseDir = new File(getDefaultSystemDir(), "netloststat");
        if (!baseDir.exists()) {
            return null;
        }
        File file = new File(baseDir, "lost_stat_config.xml");
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream fin = new FileInputStream(file);
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer);
            String res2 = new String(buffer, "UTF_8");
            try {
                fin.close();
                res = res2;
            } catch (Exception e2) {
                e = e2;
                res = res2;
                Slog.e(TAG, "write backup xml failed " + e);
                return res;
            }
        } catch (Exception e3) {
            e = e3;
            Slog.e(TAG, "write backup xml failed " + e);
            return res;
        }
        return res;
    }
}
