package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class OppoGnssWhiteListProxy {
    private static final String ACTION_ROM_UPDATE_CONFIG = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String ATTR_ALL_CHINA_WITHOUT_GMS = "all_china_without_gms";
    private static final String ATTR_FAST_NETWORK_LOCATION_ON = "fast_network_location";
    private static final String ATTR_GNSS_POWER_SAVER_ON = "gnss_power_saver";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_NAVIGATION_MAP_ALWAY_ON = "navigation_map_alway_on";
    private static final String ATTR_NAVIGATION_MAP_WHITE_LIST = "default_navigation_map_whitelist";
    private static final String ATTR_NETWORK_LOCATION_ALWAY_ON = "networklocation_alway_on";
    private static final String ATTR_NETWORK_LOCATION_WHITE_LIST = "networklocation_whitelist";
    private static final String ATTR_WITHOUT_GMS_WHITE_LIST = "without_gms_country_whitelist";
    private static final String COLUMN_NAME_VERSION = "version";
    private static final String COLUMN_NAME_XML = "xml";
    private static final String DATA_FILE_PATH = "/data/system/sys_location_config.xml";
    private static final String EXTRA_ROM_UPDATE_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String FILE_NAME = "sys_location_config";
    private static final Uri NEARME_ROM_UPDATE_URI = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String SWITCH_OFF = "off";
    private static final String SWITCH_ON = "on";
    private static final String TAG = "OppoGnssWhiteList";
    private static final String TAG_ITEM = "item";
    private static final String TAG_LOCATION_STAND_TIME = "network_location_stand_time";
    private static final String TAG_MOTION_INTERVAL = "motion_interval";
    private static final String TAG_MOTION_SAMPLE_NUM = "motion_sample_num";
    private static final String TAG_STRING = "string";
    private static final String TAG_STRING_ARRAY = "string-array";
    private static final String TAG_THRESHOLD_ANGLE = "motion_threshold_angle";
    private static final String TAG_THRESHOLD_ENERGY = "motion_threshold_energy";
    private static final String TAG_VERSION = "version";
    private static OppoGnssWhiteListProxy mInstall = null;
    private boolean mAllChinaWithoutGms;
    private Context mContext = null;
    private ArrayList<String> mCurrList = null;
    private boolean mFastNetworkLocationEnabled;
    private boolean mIsCn = true;
    private boolean mIsDebug = false;
    private boolean mIsNavigationMapAlwayOn;
    private boolean mIsNetworkLocationAlwayOn;
    private long mLocationStandTime = 30000;
    private OppoMotionConfig mMotionConfig = null;
    private ArrayList<String> mNavigationMapWhiteList;
    private ArrayList<String> mNetworkLocationWhiteList;
    private boolean mPowerSaverEnabled;
    private BroadcastReceiver mRomUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS".equals(intent.getAction())) {
                List<String> list = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                if (list == null || list.isEmpty()) {
                    if (OppoGnssWhiteListProxy.this.mIsDebug) {
                        Log.d(OppoGnssWhiteListProxy.TAG, "Get the extend rom update list is null");
                    }
                } else if (list != null && list.contains(OppoGnssWhiteListProxy.FILE_NAME)) {
                    OppoGnssWhiteListProxy.this.loadXML();
                }
            }
        }
    };
    private int mVersion;
    private ArrayList<String> mWithoutCountryWhiteList;

    public static OppoGnssWhiteListProxy getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoGnssWhiteListProxy(context);
        }
        return mInstall;
    }

    private OppoGnssWhiteListProxy(Context context) {
        this.mContext = context;
        this.mIsCn = SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
        if (!initValueFromFile()) {
            initDefaultValues();
        }
        registBroadcast();
    }

    private boolean initValueFromFile() {
        File file = new File(DATA_FILE_PATH);
        if (!file.exists()) {
            return false;
        }
        this.mNetworkLocationWhiteList = new ArrayList();
        this.mNavigationMapWhiteList = new ArrayList();
        this.mWithoutCountryWhiteList = new ArrayList();
        this.mMotionConfig = new OppoMotionConfig();
        return parseContentFromXML(readFromFile(file));
    }

    public void setIsDebug(boolean isDebug) {
        this.mIsDebug = isDebug;
    }

    private void initDefaultValues() {
        this.mVersion = 20180213;
        this.mIsNetworkLocationAlwayOn = true;
        this.mNetworkLocationWhiteList = new ArrayList();
        this.mNetworkLocationWhiteList.add("com.coloros.weather");
        this.mNetworkLocationWhiteList.add("com.coloros.weather.service");
        this.mNetworkLocationWhiteList.add("com.coloros.speechassist.engine");
        this.mNetworkLocationWhiteList.add("com.qualcomm.location");
        this.mNetworkLocationWhiteList.add("com.mediatek.nlpservice");
        this.mNetworkLocationWhiteList.add("com.ted.number");
        this.mNetworkLocationWhiteList.add("com.android.mms");
        this.mNetworkLocationWhiteList.add("com.coloros.assistantscreen");
        this.mNetworkLocationWhiteList.add("com.coloros.colordirectservice");
        this.mNetworkLocationWhiteList.add("com.coloros.directui");
        this.mNetworkLocationWhiteList.add("com.coloros.smartlock");
        this.mFastNetworkLocationEnabled = true;
        this.mLocationStandTime = 30000;
        this.mIsNavigationMapAlwayOn = true;
        this.mNavigationMapWhiteList = new ArrayList();
        this.mNavigationMapWhiteList.add("com.baidu.BaiduMap");
        this.mNavigationMapWhiteList.add("com.baidu.location.fused");
        this.mNavigationMapWhiteList.add("com.autonavi.minimap");
        this.mNavigationMapWhiteList.add("com.amap.android.ams");
        this.mNavigationMapWhiteList.add("com.google.android.apps.maps");
        this.mNavigationMapWhiteList.add("com.sogou.android.maps");
        this.mNavigationMapWhiteList.add("com.tencent.map");
        this.mNavigationMapWhiteList.add("com.tencent.android.location");
        this.mAllChinaWithoutGms = true;
        this.mWithoutCountryWhiteList = new ArrayList();
        this.mWithoutCountryWhiteList.add("460");
        this.mWithoutCountryWhiteList.add("454");
        this.mWithoutCountryWhiteList.add("455");
        this.mPowerSaverEnabled = true;
        this.mMotionConfig = new OppoMotionConfig(62, 40, 7.0f, 2.0f);
        if (this.mIsDebug) {
            Log.d(TAG, "Initialize default values!!");
        }
    }

    private void loadXML() {
        String xml = null;
        int version = 0;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(NEARME_ROM_UPDATE_URI, new String[]{"version", COLUMN_NAME_XML}, "filtername=\"sys_location_config\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_XML);
                cursor.moveToNext();
                version = cursor.getInt(versioncolumnIndex);
                xml = cursor.getString(xmlcolumnIndex);
                if (this.mIsDebug) {
                    Log.d(TAG, "White List updated, version = " + version);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Get xml from database fail!!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (version == 0 || xml == null) {
            if (this.mIsDebug) {
                Log.d(TAG, "Get the xml content is wrong!!!");
            }
            return;
        }
        try {
            parseContentFromXML(xml);
        } catch (Exception e2) {
            Log.e(TAG, "Parsing the xml content error!!");
        }
        saveToFile(xml, DATA_FILE_PATH);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x002d A:{SYNTHETIC, Splitter: B:16:0x002d} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0040 A:{SYNTHETIC, Splitter: B:24:0x0040} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean saveToFile(String content, String filePath) {
        Exception e;
        Throwable th;
        FileOutputStream outStream = null;
        try {
            FileOutputStream outStream2 = new FileOutputStream(new File(filePath));
            try {
                outStream2.write(content.getBytes());
                outStream2.close();
                if (outStream2 != null) {
                    try {
                        outStream2.close();
                    } catch (Exception e2) {
                        Log.e(TAG, "saveToFile outStream close error!!");
                    }
                }
                return true;
            } catch (Exception e3) {
                e = e3;
                outStream = outStream2;
                try {
                    e.printStackTrace();
                    if (outStream != null) {
                        try {
                            outStream.close();
                        } catch (Exception e4) {
                            Log.e(TAG, "saveToFile outStream close error!!");
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (outStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outStream = outStream2;
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (Exception e5) {
                        Log.e(TAG, "saveToFile outStream close error!!");
                    }
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            e.printStackTrace();
            if (outStream != null) {
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x005a A:{SYNTHETIC, Splitter: B:32:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x006b A:{SYNTHETIC, Splitter: B:40:0x006b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readFromFile(File path) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (path == null) {
            return null;
        }
        InputStream inputStrm = null;
        try {
            InputStream inputStrm2 = new FileInputStream(path);
            try {
                BufferedReader inputRdr = new BufferedReader(new InputStreamReader(inputStrm2));
                StringBuffer buffer = new StringBuffer();
                String str = "";
                while (true) {
                    str = inputRdr.readLine();
                    if (str == null) {
                        break;
                    }
                    buffer.append(str + "\n");
                }
                String stringBuffer = buffer.toString();
                if (inputStrm2 != null) {
                    try {
                        inputStrm2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return stringBuffer;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                inputStrm = inputStrm2;
            } catch (IOException e5) {
                e3 = e5;
                inputStrm = inputStrm2;
                e3.printStackTrace();
                if (inputStrm != null) {
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                inputStrm = inputStrm2;
                if (inputStrm != null) {
                    try {
                        inputStrm.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            try {
                e2.printStackTrace();
                if (inputStrm != null) {
                    try {
                        inputStrm.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (inputStrm != null) {
                }
                throw th;
            }
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            if (inputStrm != null) {
                try {
                    inputStrm.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            return null;
        }
    }

    private boolean parseContentFromXML(String content) {
        if (content == null) {
            Log.e(TAG, "parse content is null");
            return false;
        }
        int version = 0;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            if (!TextUtils.isEmpty(content)) {
                parser.setInput(new StringReader(content));
            }
            parser.nextTag();
            for (int evenType = parser.getEventType(); evenType != 1; evenType = parser.next()) {
                if (2 == evenType) {
                    String tagName = parser.getName();
                    String name;
                    if (tagName.equals("version")) {
                        try {
                            version = Integer.valueOf(parser.nextText()).intValue();
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                        if (this.mVersion > version) {
                            Log.e(TAG, "Version is old, Don't need update anything from the xml file!");
                            break;
                        }
                        this.mVersion = version;
                    } else if (tagName.equals(TAG_STRING)) {
                        name = parser.getAttributeValue(null, ATTR_NAME);
                        String value = parser.nextText();
                        if (ATTR_NETWORK_LOCATION_ALWAY_ON.equals(name)) {
                            setNetworkLocationAlwayOn(value);
                        } else if (ATTR_NAVIGATION_MAP_ALWAY_ON.equals(name)) {
                            setNavigationMapAlwayOn(value);
                        } else if (ATTR_ALL_CHINA_WITHOUT_GMS.equals(name)) {
                            setAllChinaWithoutGms(value);
                        } else if (ATTR_GNSS_POWER_SAVER_ON.equals(name)) {
                            setGnssPowerSaverOn(value);
                        } else if (ATTR_FAST_NETWORK_LOCATION_ON.equals(name)) {
                            setFastNetworkLocationOn(value);
                        }
                    } else if (tagName.equals(TAG_STRING_ARRAY)) {
                        name = parser.getAttributeValue(null, ATTR_NAME);
                        if (ATTR_NETWORK_LOCATION_WHITE_LIST.equals(name)) {
                            this.mCurrList = this.mNetworkLocationWhiteList;
                        } else if (ATTR_NAVIGATION_MAP_WHITE_LIST.equals(name)) {
                            this.mCurrList = this.mNavigationMapWhiteList;
                        } else if (ATTR_WITHOUT_GMS_WHITE_LIST.equals(name)) {
                            this.mCurrList = this.mWithoutCountryWhiteList;
                        }
                    } else if (tagName.equals(TAG_ITEM)) {
                        insertWhiteList(parser.nextText());
                    } else if (tagName.equals(TAG_MOTION_SAMPLE_NUM)) {
                        try {
                            this.mMotionConfig.setSampleNum(Integer.valueOf(parser.nextText()).intValue());
                        } catch (NumberFormatException e2) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                    } else if (tagName.equals(TAG_MOTION_INTERVAL)) {
                        try {
                            this.mMotionConfig.setInterval(Integer.valueOf(parser.nextText()).intValue());
                        } catch (NumberFormatException e3) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                    } else if (tagName.equals(TAG_THRESHOLD_ENERGY)) {
                        try {
                            this.mMotionConfig.setThresholdEnergy(Float.valueOf(parser.nextText()).floatValue());
                        } catch (NumberFormatException e4) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                    } else if (tagName.equals(TAG_THRESHOLD_ANGLE)) {
                        try {
                            this.mMotionConfig.setThresholdAngle(Float.valueOf(parser.nextText()).floatValue());
                        } catch (NumberFormatException e5) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                    } else if (tagName.equals(TAG_LOCATION_STAND_TIME)) {
                        try {
                            this.mLocationStandTime = Long.valueOf(parser.nextText()).longValue();
                        } catch (NumberFormatException e6) {
                            Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (this.mIsDebug) {
                Log.d(TAG, "Parse gnss content done!");
            }
            return true;
        } catch (XmlPullParserException e7) {
            Log.e(TAG, "Got XmlPullParser exception parsing!");
            if (this.mIsDebug) {
                Log.d(TAG, "Parse gnss content done!");
            }
            return false;
        } catch (IOException e8) {
            Log.e(TAG, "Got IO exception parsing!!");
            if (this.mIsDebug) {
                Log.d(TAG, "Parse gnss content done!");
            }
            return false;
        } catch (Throwable th) {
            if (this.mIsDebug) {
                Log.d(TAG, "Parse gnss content done!");
            }
            throw th;
        }
    }

    private void setNetworkLocationAlwayOn(String onOff) {
        if (onOff.equals(SWITCH_ON)) {
            this.mIsNetworkLocationAlwayOn = true;
        } else if (onOff.equals(SWITCH_OFF)) {
            this.mIsNetworkLocationAlwayOn = false;
        }
    }

    public boolean isNetworkLocationAlwayOn() {
        return this.mIsCn ? this.mIsNetworkLocationAlwayOn : false;
    }

    private void setNavigationMapAlwayOn(String onOff) {
        if (onOff.equals(SWITCH_ON)) {
            this.mIsNavigationMapAlwayOn = true;
        } else if (onOff.equals(SWITCH_OFF)) {
            this.mIsNavigationMapAlwayOn = false;
        }
    }

    public boolean isNavigationMapAlwayOn() {
        return this.mIsNavigationMapAlwayOn;
    }

    private void setAllChinaWithoutGms(String onOff) {
        if (onOff.equals(SWITCH_ON)) {
            this.mAllChinaWithoutGms = true;
        } else if (onOff.equals(SWITCH_OFF)) {
            this.mAllChinaWithoutGms = false;
        }
    }

    public boolean isPowerSaverOn() {
        return this.mPowerSaverEnabled;
    }

    private void setGnssPowerSaverOn(String onOff) {
        if (onOff.equals(SWITCH_ON)) {
            this.mPowerSaverEnabled = true;
        } else if (onOff.equals(SWITCH_OFF)) {
            this.mPowerSaverEnabled = false;
        }
    }

    private void setFastNetworkLocationOn(String onOff) {
        if (onOff.equals(SWITCH_ON)) {
            this.mFastNetworkLocationEnabled = true;
        } else if (onOff.equals(SWITCH_OFF)) {
            this.mFastNetworkLocationEnabled = false;
        }
    }

    public boolean isEnableFastNetworkLocation() {
        return this.mFastNetworkLocationEnabled;
    }

    public long getNetworkStandTime() {
        return this.mLocationStandTime;
    }

    private void insertWhiteList(String packageName) {
        if (!(this.mCurrList == null || this.mCurrList.contains(packageName))) {
            this.mCurrList.add(packageName);
        }
    }

    public boolean inNetworkLocationWhiteList(String packageName) {
        if (!this.mIsNetworkLocationAlwayOn || packageName == null) {
            return false;
        }
        return this.mNetworkLocationWhiteList.contains(packageName);
    }

    public boolean inNavigationMapWhiteList(String packageName) {
        if (!this.mIsNavigationMapAlwayOn || packageName == null) {
            return false;
        }
        return this.mNavigationMapWhiteList.contains(packageName);
    }

    public boolean inWithoutGmsContryList(String mcc) {
        if (mcc == null || mcc.length() < 3) {
            return false;
        }
        String strMcc = mcc.substring(0, 3);
        if (strMcc.equals("460")) {
            return true;
        }
        if (this.mAllChinaWithoutGms) {
            return this.mWithoutCountryWhiteList.contains(strMcc);
        }
        return false;
    }

    public OppoMotionConfig getMotionConfig() {
        return this.mMotionConfig;
    }

    private void registBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        this.mContext.registerReceiver(this.mRomUpdateReceiver, intentFilter);
    }
}
