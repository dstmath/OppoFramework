package com.android.server.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class ColorDexMetadataConfigHelper {
    private static final String ACTION_ROM_UPDATE_CONFIG = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String ATTR_ALL_BACKGROUND_COMPILE_SWITCH = "all_in_background_compile_switch";
    private static final String ATTR_ALL_PRE_COMPILE_SWITCH = "all_in_pre_compile_switch";
    private static final String ATTR_APP_USAGE_IN_RECENT_MONTHS = "app_usage_in_recent_months";
    private static final String ATTR_COMPILE_TEMPERATURE_THRESHOLD = "compile_temperature_threshold";
    private static final String ATTR_DCS_SWITCH = "dcs_switch";
    private static final String ATTR_IDLE_TIME_TO_COMPILE = "idle_time_to_compile";
    private static final String ATTR_MAIN_SWITCH = "main_switch";
    private static final String ATTR_OPPO_PROFILE_COMPILE_SET_SIZE = "oppo_profile_compile_set_size";
    private static final String ATTR_OPPO_PROFILE_COMPILE_SWITCH = "oppo_profile_compile_switch";
    private static final String ATTR_TIME_SLOT_TO_COMPILE = "time_slot_to_compile";
    private static final String ATTR_TOP_APP_NUMBER = "top_app_number";
    private static final String ATTR_USE_CLOUD_PROFILE_SWITCH = "use_cloud_profile_switch";
    private static final String ATTR_USE_RESTORE_PROFILE_SWITCH = "use_restore_profile_switch";
    private static final String ATTR_USE_UPGRADE_PROFILE_SWITCH = "use_upgrade_profile_switch";
    private static final String ATTR_VALID_FILE_SIZE_THRESHOLD = "valid_file_size_threshold";
    private static final String COLUMN_NAME_VERSION = "version";
    private static final String COLUMN_NAME_XML = "xml";
    private static final int DEFAULT_APP_USAGE_MONTH = -1;
    private static final int DEFAULT_COMPILE_PACKAGE_SIZE = 30;
    private static final int DEFAULT_IDLE_TIME_COMPILE = 1;
    private static final int DEFAULT_SLOT_TIME_COMPILE = 1;
    private static final int DEFAULT_TEMPERATURE_THRESHOLD = 400;
    private static final int DEFAULT_TOP_APP_NUMBER = 20;
    private static final int DEFAULT_VALID_FILE_THRESHOLD = 25;
    private static final String DEXMETADATA_CONFIG_FILE_NAME = "sys_dexmetadata_config";
    private static final Uri NEARME_ROM_UPDATE_URI = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final String OPPO_DEXMETADATA_CONFIG_FILE_PATH = "/data/system/sys_dexmetadata_config.xml";
    private static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String TAG = "ColorDexMetadataConfigHelper";
    private static final String TAG_ATTRIBUTE = "attr";
    private static final String TAG_BLACK_LIST_PACKAGE = "BlackListPackage";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_VERSION = "version";
    private static final String TAG_WHITE_LIST_PACKAGE = "WhiteListPackage";
    private static volatile ColorDexMetadataConfigHelper sDmConfigHelper = null;
    private boolean mAllInBackgroundCompileSwitch;
    private boolean mAllInPrecompileSwitch;
    private int mAppUsageInRecentMonths;
    private List<String> mBlackListPackage = new ArrayList();
    private int mCompilePackageSetSize;
    private final Object mConfigLock = new Object();
    private Context mContext;
    private boolean mDcsSwitch;
    private boolean mDebugDetail = ColorDexMetadataManager.sDebugDetail;
    private boolean mDebugSwitch = (this.mDebugDetail | this.mDynamicDebug);
    private boolean mDynamicDebug = false;
    private int mIdleTimeToCompile;
    private boolean mMainSwitch;
    private boolean mOppoProfileCompileSwitch;
    private BroadcastReceiver mRomUpdateReceiver = new BroadcastReceiver() {
        /* class com.android.server.pm.ColorDexMetadataConfigHelper.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ColorDexMetadataConfigHelper.this.mDebugSwitch) {
                Slog.d(ColorDexMetadataConfigHelper.TAG, "RomUpdateReceiver: action = " + action);
            }
            if (ColorDexMetadataConfigHelper.ACTION_ROM_UPDATE_CONFIG.equals(action)) {
                ArrayList<String> list = intent.getStringArrayListExtra(ColorDexMetadataConfigHelper.ROM_UPDATE_CONFIG_LIST);
                if (list == null || list.isEmpty()) {
                    Slog.w(ColorDexMetadataConfigHelper.TAG, "get the rom update list is null");
                } else if (list.contains(ColorDexMetadataConfigHelper.DEXMETADATA_CONFIG_FILE_NAME)) {
                    if (ColorDexMetadataConfigHelper.this.mDebugSwitch) {
                        Slog.d(ColorDexMetadataConfigHelper.TAG, "RomUpdateReceiver contains sys_dexmetadata_config");
                    }
                    ColorDexMetadataConfigHelper.this.loadRomUpdateConfigXML();
                }
            }
        }
    };
    private int mTemperatureThreshold;
    private int mTimeSlotToCompile;
    private int mTopAppNumber;
    private boolean mUseCloudProfileSwitch;
    private boolean mUseRestoreProfileSwitch;
    private boolean mUseUpgradeProfileSwitch;
    private int mValidFileSizeThreshold;
    private int mVersion = 0;
    private List<String> mWhiteListPackage = new ArrayList();

    public static ColorDexMetadataConfigHelper getInstance() {
        if (sDmConfigHelper == null) {
            synchronized (ColorDexMetadataConfigHelper.class) {
                if (sDmConfigHelper == null) {
                    sDmConfigHelper = new ColorDexMetadataConfigHelper();
                }
            }
        }
        return sDmConfigHelper;
    }

    private ColorDexMetadataConfigHelper() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "ConfigHelper: constructor!");
        }
        if (!initConfigValuesFromFile()) {
            initDefaultConfigValues();
        }
    }

    public void onSystemReady(Context context) {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "ConfigHelper: onSystemReady!");
        }
        this.mContext = context;
        registerRomUpdateBroadcast();
    }

    private boolean initConfigValuesFromFile() {
        File file = new File(OPPO_DEXMETADATA_CONFIG_FILE_PATH);
        if (!file.exists()) {
            return false;
        }
        if (this.mDebugSwitch) {
            Slog.i(TAG, "init config values from xml file!");
        }
        return parseContentFromXML(ColorDexMetadataUtils.readFromFile(file));
    }

    private void initDefaultConfigValues() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "init default config values!");
        }
        synchronized (this.mConfigLock) {
            this.mMainSwitch = true;
            this.mUseCloudProfileSwitch = true;
            this.mUseUpgradeProfileSwitch = true;
            this.mUseRestoreProfileSwitch = true;
            this.mDcsSwitch = true;
            this.mTopAppNumber = 20;
            this.mAppUsageInRecentMonths = -1;
            this.mValidFileSizeThreshold = DEFAULT_VALID_FILE_THRESHOLD;
            this.mAllInPrecompileSwitch = false;
            this.mAllInBackgroundCompileSwitch = false;
            this.mOppoProfileCompileSwitch = true;
            this.mIdleTimeToCompile = 1;
            this.mTimeSlotToCompile = 1;
            this.mCompilePackageSetSize = DEFAULT_COMPILE_PACKAGE_SIZE;
            this.mTemperatureThreshold = DEFAULT_TEMPERATURE_THRESHOLD;
        }
    }

    private void registerRomUpdateBroadcast() {
        if (this.mContext != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_ROM_UPDATE_CONFIG);
            this.mContext.registerReceiver(this.mRomUpdateReceiver, intentFilter, "oppo.permission.OPPO_COMPONENT_SAFE", null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005e, code lost:
        if (r1 != null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006a, code lost:
        if (0 == 0) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006c, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0070, code lost:
        if (r11 == 0) goto L_0x00ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0072, code lost:
        if (r10 != null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0075, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r2 = parseContentFromXML(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007b, code lost:
        if (r2 == false) goto L_0x0087;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007f, code lost:
        if (r12.mDebugSwitch == false) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0081, code lost:
        android.util.Slog.d(com.android.server.pm.ColorDexMetadataConfigHelper.TAG, "rom update config success!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0089, code lost:
        if (r12.mDebugSwitch == false) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008b, code lost:
        android.util.Slog.d(com.android.server.pm.ColorDexMetadataConfigHelper.TAG, "rom update config failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0092, code lost:
        android.util.Slog.e(com.android.server.pm.ColorDexMetadataConfigHelper.TAG, "parsing the xml content error!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ab, code lost:
        android.util.Slog.w(com.android.server.pm.ColorDexMetadataConfigHelper.TAG, "get the xml content is wrong!!!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b0, code lost:
        return;
     */
    private void loadRomUpdateConfigXML() {
        boolean ret;
        Cursor cursor = null;
        String[] projection = {"version", COLUMN_NAME_XML};
        String xml = null;
        int version = 0;
        try {
            if (this.mContext != null) {
                cursor = this.mContext.getContentResolver().query(NEARME_ROM_UPDATE_URI, projection, "filtername=\"sys_dexmetadata_config\"", null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    int versioncolumnIndex = cursor.getColumnIndex("version");
                    int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_XML);
                    cursor.moveToNext();
                    version = cursor.getInt(versioncolumnIndex);
                    xml = cursor.getString(xmlcolumnIndex);
                    if (this.mDebugSwitch) {
                        Slog.d(TAG, "rom update config updated, version = " + version);
                    }
                }
            } else if (0 != 0) {
                cursor.close();
                return;
            } else {
                return;
            }
        } catch (Exception e) {
            Slog.e(TAG, "get the update config from database fail!");
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        if (ret && ColorDexMetadataUtils.saveToFile(xml, OPPO_DEXMETADATA_CONFIG_FILE_PATH) && this.mDebugSwitch) {
            Slog.d(TAG, "save config xml success!");
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:328:0x02e7, code lost:
        continue;
     */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x032e  */
    /* JADX WARNING: Removed duplicated region for block: B:330:? A[RETURN, SYNTHETIC] */
    private boolean parseContentFromXML(String content) {
        Exception e;
        Exception e2;
        char c;
        boolean z = false;
        if (TextUtils.isEmpty(content)) {
            Slog.w(TAG, "parse content is null!");
            return false;
        }
        StringReader stringReader = null;
        List<String> whiteListPackage = new ArrayList<>();
        List<String> blackListPackage = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            try {
                StringReader stringReader2 = new StringReader(content);
                parser.setInput(stringReader2);
                parser.nextTag();
                int eventType = parser.getEventType();
                String currentTag = null;
                while (eventType != 1) {
                    if (2 == eventType) {
                        String tagName = parser.getName();
                        if (tagName.equals("version")) {
                            int version = Integer.parseInt(parser.nextText());
                            if (this.mVersion > version) {
                                Slog.w(TAG, "config xml version is old, no need to update!");
                                stringReader2.close();
                                return z;
                            }
                            this.mVersion = version;
                        } else if (tagName.equals(TAG_ATTRIBUTE)) {
                            if (parser.getAttributeCount() > 0) {
                                int i = z ? 1 : 0;
                                int i2 = z ? 1 : 0;
                                int i3 = z ? 1 : 0;
                                String attributeValue = parser.getAttributeValue(i);
                                switch (attributeValue.hashCode()) {
                                    case -2103023137:
                                        if (attributeValue.equals(ATTR_OPPO_PROFILE_COMPILE_SET_SIZE)) {
                                            c = '\r';
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -1551963629:
                                        if (attributeValue.equals(ATTR_USE_RESTORE_PROFILE_SWITCH)) {
                                            c = 3;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -1510554543:
                                        if (attributeValue.equals(ATTR_TOP_APP_NUMBER)) {
                                            c = 5;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -1271920115:
                                        if (attributeValue.equals(ATTR_VALID_FILE_SIZE_THRESHOLD)) {
                                            c = 7;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -1058209098:
                                        if (attributeValue.equals(ATTR_IDLE_TIME_TO_COMPILE)) {
                                            c = 11;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -967844487:
                                        if (attributeValue.equals(ATTR_APP_USAGE_IN_RECENT_MONTHS)) {
                                            c = 6;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -781298379:
                                        if (attributeValue.equals(ATTR_ALL_BACKGROUND_COMPILE_SWITCH)) {
                                            c = '\t';
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -730694534:
                                        if (attributeValue.equals(ATTR_MAIN_SWITCH)) {
                                            char c2 = z ? 1 : 0;
                                            Object[] objArr = z ? 1 : 0;
                                            Object[] objArr2 = z ? 1 : 0;
                                            c = c2;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -642123265:
                                        if (attributeValue.equals(ATTR_DCS_SWITCH)) {
                                            c = 4;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case -32683636:
                                        if (attributeValue.equals(ATTR_USE_CLOUD_PROFILE_SWITCH)) {
                                            c = 1;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case 435401694:
                                        if (attributeValue.equals(ATTR_TIME_SLOT_TO_COMPILE)) {
                                            c = '\f';
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case 558368340:
                                        if (attributeValue.equals(ATTR_COMPILE_TEMPERATURE_THRESHOLD)) {
                                            c = 14;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case 561206469:
                                        if (attributeValue.equals(ATTR_USE_UPGRADE_PROFILE_SWITCH)) {
                                            c = 2;
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case 1006320920:
                                        if (attributeValue.equals(ATTR_ALL_PRE_COMPILE_SWITCH)) {
                                            c = '\b';
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    case 1998482325:
                                        if (attributeValue.equals(ATTR_OPPO_PROFILE_COMPILE_SWITCH)) {
                                            c = '\n';
                                            break;
                                        }
                                        c = 65535;
                                        break;
                                    default:
                                        c = 65535;
                                        break;
                                }
                                switch (c) {
                                    case 0:
                                        String mainSwitch = parser.nextText();
                                        if (!TextUtils.isEmpty(mainSwitch)) {
                                            synchronized (this.mConfigLock) {
                                                this.mMainSwitch = Boolean.parseBoolean(mainSwitch);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case 1:
                                        String useCloudProfileSwitch = parser.nextText();
                                        if (!TextUtils.isEmpty(useCloudProfileSwitch)) {
                                            synchronized (this.mConfigLock) {
                                                this.mUseCloudProfileSwitch = Boolean.parseBoolean(useCloudProfileSwitch);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case 2:
                                        String useUpgradeProfileSwitch = parser.nextText();
                                        if (!TextUtils.isEmpty(useUpgradeProfileSwitch)) {
                                            synchronized (this.mConfigLock) {
                                                this.mUseUpgradeProfileSwitch = Boolean.parseBoolean(useUpgradeProfileSwitch);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case 3:
                                        String useRestoreProfileSwitch = parser.nextText();
                                        if (!TextUtils.isEmpty(useRestoreProfileSwitch)) {
                                            synchronized (this.mConfigLock) {
                                                this.mUseRestoreProfileSwitch = Boolean.parseBoolean(useRestoreProfileSwitch);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case 4:
                                        String dcsSwitch = parser.nextText();
                                        if (!TextUtils.isEmpty(dcsSwitch)) {
                                            synchronized (this.mConfigLock) {
                                                this.mDcsSwitch = Boolean.parseBoolean(dcsSwitch);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case 5:
                                        String topAppNumber = parser.nextText();
                                        if (!TextUtils.isEmpty(topAppNumber)) {
                                            synchronized (this.mConfigLock) {
                                                this.mTopAppNumber = Integer.parseInt(topAppNumber);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case 6:
                                        String appUsageInRecentMonths = parser.nextText();
                                        if (!TextUtils.isEmpty(appUsageInRecentMonths)) {
                                            synchronized (this.mConfigLock) {
                                                this.mAppUsageInRecentMonths = Integer.parseInt(appUsageInRecentMonths);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /* 7 */:
                                        String validFileSizeThreshold = parser.nextText();
                                        if (!TextUtils.isEmpty(validFileSizeThreshold)) {
                                            synchronized (this.mConfigLock) {
                                                this.mValidFileSizeThreshold = Integer.parseInt(validFileSizeThreshold);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case '\b':
                                        String allPrecompileSwitch = parser.nextText();
                                        if (!TextUtils.isEmpty(allPrecompileSwitch)) {
                                            synchronized (this.mConfigLock) {
                                                this.mAllInPrecompileSwitch = Boolean.parseBoolean(allPrecompileSwitch);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case ColorStartingWindowRUSHelper.FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT /* 9 */:
                                        String allBackgroundCompileSwitch = parser.nextText();
                                        if (!TextUtils.isEmpty(allBackgroundCompileSwitch)) {
                                            synchronized (this.mConfigLock) {
                                                this.mAllInBackgroundCompileSwitch = Boolean.parseBoolean(allBackgroundCompileSwitch);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case ColorStartingWindowRUSHelper.STARTING_WINDOW_EXIT_LONG_DURATION_PACKAGE /* 10 */:
                                        String oppoProfileCompileSwitch = parser.nextText();
                                        if (!TextUtils.isEmpty(oppoProfileCompileSwitch)) {
                                            synchronized (this.mConfigLock) {
                                                this.mOppoProfileCompileSwitch = Boolean.parseBoolean(oppoProfileCompileSwitch);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case ColorStartingWindowRUSHelper.SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION /* 11 */:
                                        String idleTimeToCompile = parser.nextText();
                                        if (!TextUtils.isEmpty(idleTimeToCompile)) {
                                            synchronized (this.mConfigLock) {
                                                this.mIdleTimeToCompile = Integer.parseInt(idleTimeToCompile);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case ColorStartingWindowRUSHelper.USE_TRANSLUCENT_DRAWABLE_FOR_SPLASH_WINDOW /* 12 */:
                                        String timeSlotToCompile = parser.nextText();
                                        if (!TextUtils.isEmpty(timeSlotToCompile)) {
                                            synchronized (this.mConfigLock) {
                                                this.mTimeSlotToCompile = Integer.parseInt(timeSlotToCompile);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case '\r':
                                        String compilePackageSetSize = parser.nextText();
                                        if (!TextUtils.isEmpty(compilePackageSetSize)) {
                                            synchronized (this.mConfigLock) {
                                                this.mCompilePackageSetSize = Integer.parseInt(compilePackageSetSize);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                    case 14:
                                        String temperatureThreshold = parser.nextText();
                                        if (!TextUtils.isEmpty(temperatureThreshold)) {
                                            synchronized (this.mConfigLock) {
                                                this.mTemperatureThreshold = Integer.parseInt(temperatureThreshold);
                                            }
                                            break;
                                        } else {
                                            continue;
                                        }
                                }
                            } else {
                                continue;
                            }
                        } else if (tagName.equals(TAG_WHITE_LIST_PACKAGE) || tagName.equals(TAG_BLACK_LIST_PACKAGE)) {
                            currentTag = tagName;
                        } else if (tagName.equals("package") && !TextUtils.isEmpty(currentTag)) {
                            String pkg = parser.nextText();
                            if (!TextUtils.isEmpty(pkg)) {
                                if (currentTag.equals(TAG_WHITE_LIST_PACKAGE)) {
                                    whiteListPackage.add(pkg);
                                } else if (currentTag.equals(TAG_BLACK_LIST_PACKAGE)) {
                                    blackListPackage.add(pkg);
                                }
                            }
                        }
                    }
                    eventType = parser.next();
                    z = false;
                }
                synchronized (this.mConfigLock) {
                    if (!whiteListPackage.isEmpty()) {
                        this.mWhiteListPackage.clear();
                        this.mWhiteListPackage.addAll(whiteListPackage);
                    }
                    if (!blackListPackage.isEmpty()) {
                        this.mBlackListPackage.clear();
                        this.mBlackListPackage.addAll(blackListPackage);
                    }
                }
                stringReader2.close();
                return true;
            } catch (Exception e3) {
                e2 = e3;
                try {
                    Slog.e(TAG, "parsing failed: ", e2);
                    if (0 != 0) {
                        return false;
                    }
                    stringReader.close();
                    return false;
                } catch (Throwable th) {
                    e = th;
                }
            }
        } catch (Exception e4) {
            e2 = e4;
            Slog.e(TAG, "parsing failed: ", e2);
            if (0 != 0) {
            }
        } catch (Throwable th2) {
            e = th2;
            if (0 != 0) {
                stringReader.close();
            }
            throw e;
        }
    }

    public boolean isMainSwitchEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mMainSwitch;
        }
        return z;
    }

    public boolean isUseCloudProfileSwitchEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mUseCloudProfileSwitch;
        }
        return z;
    }

    public boolean isUseUpgradeProfileSwitchEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mUseUpgradeProfileSwitch;
        }
        return z;
    }

    public boolean isUseRestoreProfileSwitchEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mUseRestoreProfileSwitch;
        }
        return z;
    }

    public boolean isDcsSwitchEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mDcsSwitch;
        }
        return z;
    }

    public int getTopAppNumber() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mTopAppNumber;
        }
        return i;
    }

    public int getAppUsageInRecentMonths() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mAppUsageInRecentMonths;
        }
        return i;
    }

    public int getValidFileSizeThreshold() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mValidFileSizeThreshold;
        }
        return i;
    }

    public boolean isPrecompileForAllPackageSwitchEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mAllInPrecompileSwitch;
        }
        return z;
    }

    public boolean isBackgroundCompileForAllPackageSwitchEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mAllInBackgroundCompileSwitch;
        }
        return z;
    }

    public boolean isOppoBackgroundCompileSwitchEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mOppoProfileCompileSwitch;
        }
        return z;
    }

    public int getIdleTimeValue() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mIdleTimeToCompile;
        }
        return i;
    }

    public int getTimeSlotValue() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mTimeSlotToCompile;
        }
        return i;
    }

    public int getCompilePackageSetSize() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mCompilePackageSetSize;
        }
        return i;
    }

    public int getCompileTemperatureThreshold() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mTemperatureThreshold;
        }
        return i;
    }

    public boolean isInWhiteListForPackage(String packageName) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (!TextUtils.isEmpty(packageName) && !this.mWhiteListPackage.isEmpty() && this.mWhiteListPackage.contains(packageName)) {
                result = true;
            }
        }
        return result;
    }

    public boolean isInBlackListForPackage(String packageName) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (!TextUtils.isEmpty(packageName) && !this.mBlackListPackage.isEmpty() && this.mBlackListPackage.contains(packageName)) {
                result = true;
            }
        }
        return result;
    }
}
