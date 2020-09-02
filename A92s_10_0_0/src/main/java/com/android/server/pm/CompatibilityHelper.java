package com.android.server.pm;

import android.app.ActivityThread;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.os.Build;
import android.os.SystemProperties;
import android.util.LruCache;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.slice.SliceClientPermissions;
import com.oppo.RomUpdateHelper;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class CompatibilityHelper extends RomUpdateHelper {
    private static final Map<String, Integer> ABI_TO_INT_MAP = new HashMap();
    private static final Map<Integer, String> ABI_TO_STRING_MAP = new HashMap();
    public static final int ADD_FLAG_ACTIVITY_NEW_TASK = 719;
    public static final int ALARM_COUNTS_EXCEED_WHITE_LIST = 732;
    private static final Map<String, Integer> ANDROID_TO_INT_MAP = new HashMap();
    private static final Map<Integer, String> ANDROID_TO_STRING_MAP = new HashMap();
    private static final int BAIUDPROTECT_NATIVE_LIBRARY_SIZE_1 = 408028;
    private static final int BAIUDPROTECT_NATIVE_LIBRARY_SIZE_2 = 412124;
    private static final int BAIUDPROTECT_NATIVE_LIBRARY_SIZE_3 = 416220;
    private static final int BAIUDPROTECT_NATIVE_LIBRARY_SIZE_V7A = 367076;
    private static final int BAIUDPROTECT_NATIVE_LIBRARY_SIZE_V8A = 610128;
    public static final int BENCH_MARK_LIST = 734;
    public static final int BGOPT_THERMAL = 730;
    private static final String BROADCAST_ACTION_ROM_COMPATIBILITY_WHITELIST_PREPARED = "oppo.intent.action.COMPATIBILITY_WHITELIST_PREPARED";
    public static final int CAMERA_FLASH_CHECK = 707;
    public static final int CAMERA_RELEASE_DELAYTIME_CHECK = 717;
    private static final int CONST_FOUR = 4;
    private static final int CONST_THREE = 3;
    private static final int CONST_ZERO = 0;
    public static final int CR_MODEL = 727;
    private static final String DATA_FILE_DIR = "data/format_unclear/compatibility/oppo_cpt_list.xml";
    private static final String DCS_EVENTID_IN_WHITELIST = "compatibility_in_whitelist";
    static boolean DEBUG_CPT = false;
    public static final int DENY_BOOTCOMPLETE_RECEIVER = 736;
    public static final int DEX_OPT_SKIP_POLICY = 729;
    public static final int DO_FRAME_OPT = 725;
    public static final int FACE_PAY = 733;
    public static final String FILTER_NAME = "compatibility_whitelist_values";
    public static final int FINGERPRINT_CHECK_THROW = 708;
    public static final int FLOATING_WIN_START = 735;
    public static final int FLOAT_WIN_CHECK = 703;
    public static final int FORCE_ADD_APACHE_AND_LOAD_WITHOUT_SPEED_PROFILE = 714;
    public static final int FORCE_ALLOW_DEVICE_ID_ACCESS = 30;
    public static final int FORCE_ATTACH_APPLICATION_CHECK = 710;
    public static final int FORCE_CHOOSING_TARGETSDK_L = 25;
    public static final int FORCE_CHOOSING_TARGETSDK_M = 689;
    public static final int FORCE_DELAY_DEXOPT = 18;
    public static final int FORCE_DELAY_DEXOPT_M = 27;
    public static final int FORCE_DELAY_TO_USE_POST = 700;
    public static final int FORCE_DEX2OAT_ROLLBACK = 684;
    public static final int FORCE_DEXOPT_IN_SPEED = 688;
    public static final int FORCE_DISABLE_GR = 29;
    public static final int FORCE_DISABLE_HARDWAREACCELERATE_FOR_ACTIVITIES = 22;
    public static final int FORCE_DISABLE_HARDWAREACCELERATE_MTK = 16;
    public static final int FORCE_DISABLE_HARDWAREACCELERATE_QCOM = 15;
    public static final int FORCE_DISABLE_HYPNUS = 20;
    public static final int FORCE_DISABLE_OPENSSL = 683;
    public static final int FORCE_DISABLE_SHOW_FORCE_SOFTINPUT = 701;
    public static final int FORCE_DISABLE_START_BG_APP_SERVICE_CRASH = 696;
    public static final int FORCE_DISALLOW_REMOTEVIEW_REAPPLY = 723;
    public static final int FORCE_DISCARD_SURFACE = 724;
    public static final int FORCE_DRAW_SYSTEMBAR_BACKGROUND_DISABLED = 31;
    public static final int FORCE_EFFECT_LIB_BY_OPENSSL = 24;
    public static final int FORCE_ENABLE_DEBUGGER = 682;
    public static final int FORCE_ENABLE_HARDWAREACCELERATE = 14;
    public static final int FORCE_ENABLE_HARDWAREACCELERATE_FOR_ACTIVITIES = 21;
    public static final int FORCE_ENABLE_SAVE_SURFACE = 694;
    public static final int FORCE_FILTER_EXPLICIT_SERVICEINTENTCHECK = 3;
    public static final int FORCE_FILTER_INVALID_WIN_TYPE = 9;
    public static final int FORCE_FILTER_MESSAGE = 7;
    public static final int FORCE_FILTER_SERIALIZABLE_IMPLEMENT = 4;
    public static final int FORCE_FILTER_UNBIND_SERVICE = 5;
    public static final int FORCE_FILTER_WALLPAPER = 10;
    public static final int FORCE_IGNORE_DEXOPT = 8;
    public static final int FORCE_IGNORE_GSF = 6;
    public static final int FORCE_IN_SAFEMODE_DEX = 12;
    public static final int FORCE_IN_SAFEMODE_DEX_MTK = 13;
    public static final int FORCE_MINI_TRIMMEMORY = 678;
    public static final int FORCE_NEED_SPECIAL_LIBRARIES = 23;
    public static final int FORCE_NEED_SPECIAL_LIBRARIES_IN = 677;
    public static final int FORCE_NOTSTART_INPUTINNER_WHEN_ENABLE_FALSE = 706;
    public static final int FORCE_NOT_SHOW_WALLPAPER_WHEN_TASK_TRANSITION = 712;
    public static final int FORCE_NO_RELAUNCH_AFTER_ORIENTATE = 693;
    public static final int FORCE_REPLACE_DEXINTERPRET = 28;
    public static final int FORCE_RESET_WRONG_FRAME_SIZE = 713;
    public static final int FORCE_RUNNING_IN_32_BIT_V5 = 2;
    public static final int FORCE_RUNNING_IN_32_BIT_V7 = 0;
    public static final int FORCE_RUNNING_IN_64_BIT = 1;
    public static final int FORCE_SKIP_FILEURI_STRICT_MODE_CHECK = 692;
    public static final int FORCE_SKIP_OPENNDK_CHECK = 687;
    public static final int FORCE_SKIP_REQUEST_ORIENTATION_REVERSE_PORTRAIT = 697;
    public static final int FORCE_SKIP_TOAST_CHECK = 685;
    public static final int FORCE_SKIP_WEBVIEW_THREADCHECK = 691;
    public static final int FORCE_TOAST_USING_OLD_STYLE = 695;
    public static final int FORCE_VISIBLE_WHEN_BACK_TO_KEYGUARD = 699;
    public static final int GOOGLE_SOS_DELAYTIME_CHECK = 702;
    public static final int GR_BLACK_LIST = 679;
    public static final int GR_WHITE_LIST = 680;
    public static final int IME_SKIP_TMP_DETACH = 686;
    public static final int KEYGUARD_DELAYTIME_CHECK = 716;
    private static final int LENGTH_OF_WHITELIST = 36;
    private static final int LETTER_NUM = 26;
    private static final int LIBMG20PBASE_SIZE = 42156;
    public static final int MM_MONEY_LUCKY_CHECK = 698;
    public static final int MOTOR_UP_VIEW_CHECK = 728;
    private static final String PERMISSION_OPPO_COMPONENT_SAFE = "oppo.permission.OPPO_COMPONENT_SAFE";
    public static final int PLAY_APP_CHECK = 705;
    public static final int RECORD_APP_CHECK = 704;
    public static final int RESET_UNITY_STUCK_CPU_FREQ = 720;
    public static final int RLIMIT_STACK_CUSTOMIZED = 721;
    public static final int RUN_SCORE_BLACK_LIST = 690;
    public static final int SHOW_TOAST_APP_CHECK = 718;
    public static final int SKIP_PREPARE_IMAGE_OPTION = 715;
    public static final int SKIP_START_BG_APP_SERVICE_CRASH = 711;
    private static final String SYS_FILE_DIR = "system/etc/oppo_cpt_list.xml";
    public static final String TAG_CP = "CompatibilityHelper";
    private static final int TARGETSDK_L = 22;
    private static final int TARGETSDK_M = 23;
    public static final int UA_MODEL = 722;
    public static final int UI_FIRST_BLACK_LIST = 731;
    public static final int UNTRIMMABLE_COMPONENT_LIST = 709;
    public static final String VERSION_NAME = "version";
    /* access modifiers changed from: private */
    public static CompatibilityCallback mCompatibilityCallback;
    private static CompatibilitySchemeListInfo sSchemeList;
    private static CompatibilityDcsUploader sUploader;
    private final Context mContext;
    private final ConcurrentHashMap<Integer, LruCache<String, String>> mDcsCache = new ConcurrentHashMap<>();

    static {
        ABI_TO_INT_MAP.put("armeabi", 2);
        ABI_TO_INT_MAP.put("armeabi-v7a", 1);
        ABI_TO_INT_MAP.put("arm64-v8a", 0);
        ABI_TO_STRING_MAP.put(2, "armeabi");
        ABI_TO_STRING_MAP.put(1, "armeabi-v7a");
        ABI_TO_STRING_MAP.put(0, "arm64-v8a");
        ANDROID_TO_INT_MAP.put("Oreo", 2);
        ANDROID_TO_INT_MAP.put("Nougat", 1);
        ANDROID_TO_INT_MAP.put("beforeN", 0);
        ANDROID_TO_STRING_MAP.put(2, "Oreo");
        ANDROID_TO_STRING_MAP.put(1, "Nougat");
        ANDROID_TO_STRING_MAP.put(0, "beforeN");
    }

    private class CompatibilityUpdateInfo extends RomUpdateHelper.UpdateInfo {
        private SparseArray<ArrayList<String>> mCmpWhiteList = new SparseArray<>();
        private Context mContext = null;

        private CompatibilityUpdateInfo() {
            super(CompatibilityHelper.this);
        }

        public CompatibilityUpdateInfo(Context context) {
            super(CompatibilityHelper.this);
            this.mContext = context;
        }

        public void parseContentFromXML(String content) {
            if (content != null) {
                FileReader xmlReader = null;
                StringReader strReader = null;
                this.mCmpWhiteList.clear();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                char[] typeChar = parser.getName().toCharArray();
                                if (typeChar.length > 3) {
                                    parser.next();
                                    updateConfigVersion(String.valueOf(typeChar), parser.getText());
                                } else {
                                    int type = char2int(typeChar);
                                    parser.next();
                                    if (type >= 0) {
                                        ArrayList<String> tmp = this.mCmpWhiteList.get(type);
                                        if (tmp == null) {
                                            ArrayList<String> tmp2 = new ArrayList<>();
                                            tmp2.add(parser.getText());
                                            this.mCmpWhiteList.put(type, tmp2);
                                        } else {
                                            tmp.add(parser.getText());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e) {
                            CompatibilityHelper.this.log("Got execption close permReader.", e);
                        }
                    }
                    strReader2.close();
                    dealUpdate();
                } catch (XmlPullParserException e2) {
                    CompatibilityHelper.this.log("Got execption parsing permissions.", e2);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            CompatibilityHelper.this.log("Got execption close permReader.", e3);
                            return;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e4) {
                    CompatibilityHelper.this.log("Got execption parsing permissions.", e4);
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e5) {
                            CompatibilityHelper.this.log("Got execption close permReader.", e5);
                            return;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (Throwable th) {
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e6) {
                            CompatibilityHelper.this.log("Got execption close permReader.", e6);
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

        /* access modifiers changed from: private */
        public long getContentVersion(String content) {
            long version = -1;
            if (content == null) {
                return -1;
            }
            FileReader xmlReader = null;
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                StringReader strReader2 = new StringReader(content);
                parser.setInput(strReader2);
                int eventType = parser.getEventType();
                boolean found = false;
                while (true) {
                    if (eventType == 1) {
                        break;
                    }
                    if (eventType != 0) {
                        if (eventType != 2) {
                            if (eventType != 3) {
                            }
                        } else if ("version".equals(parser.getName())) {
                            int eventType2 = parser.next();
                            String text = parser.getText();
                            Slog.d("CompatibilityHelper", "eventType = " + eventType2 + ", text = " + text);
                            version = (long) Integer.parseInt(parser.getText());
                            found = true;
                        }
                    }
                    if (found) {
                        break;
                    }
                    eventType = parser.next();
                }
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e) {
                        CompatibilityHelper.this.log("Got execption close permReader.", e);
                    }
                }
                strReader2.close();
                return version;
            } catch (XmlPullParserException e2) {
                CompatibilityHelper.this.log("Got execption parsing permissions.", e2);
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        CompatibilityHelper.this.log("Got execption close permReader.", e3);
                        return -1;
                    }
                }
                if (strReader == null) {
                    return -1;
                }
                strReader.close();
                return -1;
            } catch (IOException e4) {
                CompatibilityHelper.this.log("Got execption parsing permissions.", e4);
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e5) {
                        CompatibilityHelper.this.log("Got execption close permReader.", e5);
                        return -1;
                    }
                }
                if (strReader == null) {
                    return -1;
                }
                strReader.close();
                return -1;
            } catch (Throwable th) {
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e6) {
                        CompatibilityHelper.this.log("Got execption close permReader.", e6);
                        throw th;
                    }
                }
                if (strReader != null) {
                    strReader.close();
                }
                throw th;
            }
        }

        private void updateConfigVersion(String type, String value) {
            Slog.d("CompatibilityHelper", hashCode() + " updateConfigVersion, type = " + type + ", value = " + value);
            if ("version".equals(type)) {
                this.mVersion = (long) Integer.parseInt(value);
            }
        }

        public boolean updateToLowerVersion(String content) {
            long newVersion = getContentVersion(content);
            Slog.d("CompatibilityHelper", "upateToLowerVersion, newVersion = " + newVersion + ", mVersion = " + this.mVersion);
            return newVersion < this.mVersion;
        }

        public boolean clone(RomUpdateHelper.UpdateInfo input) {
            SparseArray<ArrayList<String>> other = ((CompatibilityUpdateInfo) input).getAllList();
            if (other == null || other.size() == 0) {
                CompatibilityHelper.this.log("Source object is empty");
                return false;
            }
            this.mCmpWhiteList.clear();
            for (int i = 0; i < other.size(); i++) {
                int key = other.keyAt(i);
                this.mCmpWhiteList.put(key, (ArrayList) other.get(key).clone());
            }
            return true;
        }

        public boolean insert(int type, String verifyStr) {
            ArrayList<String> tmp = this.mCmpWhiteList.get(type);
            if (tmp == null) {
                return false;
            }
            tmp.add(verifyStr);
            return true;
        }

        public void clear() {
            this.mCmpWhiteList.clear();
        }

        /* access modifiers changed from: package-private */
        public int char2int(char[] in) {
            int out = 0;
            if (in.length < 1) {
                return -1;
            }
            for (int n = 0; n < in.length; n++) {
                out = (int) (((double) out) + (((double) (in[n] - 'a')) * Math.pow(26.0d, (double) ((in.length - n) - 1))));
            }
            return out;
        }

        /* access modifiers changed from: package-private */
        public void dealUpdate() {
            enableDisableHypnus();
            if (CompatibilityHelper.mCompatibilityCallback != null) {
                CompatibilityHelper.mCompatibilityCallback.dealUpdateCompatibility();
            }
            dispatchUpdateCompleted();
        }

        /* access modifiers changed from: package-private */
        public void enableDisableHypnus() {
            if (this.mCmpWhiteList.indexOfKey(20) < 0) {
                return;
            }
            if (this.mCmpWhiteList.get(20).contains("disable")) {
                SystemProperties.set("sys.enable.hypnus", "0");
            } else {
                SystemProperties.set("sys.enable.hypnus", "1");
            }
        }

        private void dispatchUpdateCompleted() {
            if (!"1".equals(SystemProperties.get("sys.boot_completed"))) {
                Slog.d("CompatibilityHelper", "dispatchUpdateCompleted, boot not completed!");
                return;
            }
            Context context = this.mContext;
            if (context == null) {
                Slog.e("CompatibilityHelper", "dispatchUpdateCompleted no context error!");
            } else {
                context.sendBroadcast(new Intent(CompatibilityHelper.BROADCAST_ACTION_ROM_COMPATIBILITY_WHITELIST_PREPARED), CompatibilityHelper.PERMISSION_OPPO_COMPONENT_SAFE);
            }
        }

        public boolean isGrEnable(String defaultValue) {
            if (this.mCmpWhiteList.indexOfKey(29) < 0 || !this.mCmpWhiteList.get(29).contains("disable")) {
                return true;
            }
            return false;
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("CompatibilityInfo [" + hashCode() + ", version = " + getVersion() + "]\n");
            for (int i = 0; i < this.mCmpWhiteList.size(); i++) {
                int key = this.mCmpWhiteList.keyAt(i);
                strBuilder.append("type = " + key);
                strBuilder.append(", value = " + this.mCmpWhiteList.get(key) + StringUtils.LF);
            }
            return strBuilder.toString();
        }

        public boolean isInWhiteList(int type, String verifyStr) {
            if (this.mCmpWhiteList.indexOfKey(type) < 0 || !this.mCmpWhiteList.get(type).contains(verifyStr)) {
                return false;
            }
            return true;
        }

        public int getTimeInWhiteList(int type, String verifyStr) {
            if (this.mCmpWhiteList.indexOfKey(type) >= 0) {
                String appStr = this.mCmpWhiteList.get(type).toString();
                if (appStr.contains(verifyStr)) {
                    String[] appArray = appStr.substring(1, appStr.length() - 1).split(", ");
                    String msecStr = "";
                    for (int i = 0; i < appArray.length; i++) {
                        if (verifyStr.equals(appArray[i]) && i < appArray.length - 1) {
                            msecStr = appArray[i + 1];
                        }
                    }
                    try {
                        int msec = Integer.parseInt(msecStr);
                        Slog.d("CompatibilityHelper", "getTimeInWhiteList msec = " + msec);
                        return msec;
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            }
            return 0;
        }

        public ArrayList<String> getOneList(int type) {
            return this.mCmpWhiteList.get(type);
        }

        public SparseArray<ArrayList<String>> getAllList() {
            return this.mCmpWhiteList;
        }
    }

    public CompatibilityHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mContext = context;
        sUploader = CompatibilityDcsUploader.getInstance(context);
        setUpdateInfo(new CompatibilityUpdateInfo(context), new CompatibilityUpdateInfo(context));
        sSchemeList = new CompatibilitySchemeListInfo();
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        CompatibilityHelper.super.init();
        String content = readFromFile(new File(SYS_FILE_DIR));
        CompatibilityUpdateInfo tempInfo = getUpdateInfo(true);
        if (tempInfo.getContentVersion(content) > tempInfo.getVersion()) {
            tempInfo.parseContentFromXML(content);
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public CompatibilityDcsUploader getUploader() {
        return sUploader;
    }

    public void sendUploadCptTest() {
        sUploader.sendToUploadCptTest();
    }

    public void sendToUploadCpt(PackageInfo pkgInfo, String point) {
        if (sUploader == null) {
            sUploader = CompatibilityDcsUploader.getInstance(this.mContext);
        }
        sUploader.sendToUploadCpt(pkgInfo, point);
    }

    public void sendToUploadCpt(String data, String point) {
        if (sUploader == null) {
            sUploader = CompatibilityDcsUploader.getInstance(this.mContext);
        }
        sUploader.sendToUploadCpt(data, point);
    }

    public static void setCallBack(CompatibilityCallback callBack) {
        mCompatibilityCallback = callBack;
    }

    public void dumpScheme(PrintWriter pw, String[] args) {
        sSchemeList.dump(pw, args);
    }

    public String dumpToString() {
        return getUpdateInfo(true).dumpToString();
    }

    public void dump(PrintWriter pw, String[] args, int opti) {
        String cmd = args[1];
        boolean isConfVersion = TemperatureProvider.SWITCH_ON.equals(SystemProperties.get("persist.version.confidential"));
        if (!"insert".equals(cmd) || !isConfVersion) {
            if (!"top".equals(cmd)) {
                pw.println("I know nothing\n");
            }
        } else if (args.length != 4) {
            pw.println("Invalid arguements!");
        } else {
            try {
                if (insertValueInList(Integer.parseInt(args[2]), args[3])) {
                    pw.println("Success!");
                }
            } catch (NumberFormatException e) {
                pw.println("Invalid arguements!");
            }
        }
    }

    private boolean isContainMode(int type) {
        if (type == 18 || type == 27 || type == 693 || type == 712 || type == 698) {
            return true;
        }
        return false;
    }

    private boolean pickNeededForCommonDcs(int type) {
        if (type == 691 || type == 710) {
            return true;
        }
        return false;
    }

    public boolean isInWhiteList(int type, String verifyStr) {
        boolean isContained = isInWhiteList(type, verifyStr, false);
        if (pickNeededForCommonDcs(type)) {
            LruCache<String, String> cache = this.mDcsCache.get(Integer.valueOf(type));
            if (cache == null) {
                cache = new LruCache<>(20);
                this.mDcsCache.put(Integer.valueOf(type), cache);
            }
            if (cache.get(verifyStr) != null) {
                return isContained;
            }
            HashMap<String, String> data = new HashMap<>();
            data.put("tag", Integer.toString(type));
            data.put(Settings.ATTR_PACKAGE, verifyStr);
            data.put("isContained", isContained ? TemperatureProvider.SWITCH_ON : TemperatureProvider.SWITCH_OFF);
            try {
                ActivityThread.class.getMethod("sendCommonDcsUploader", String.class, String.class, HashMap.class).invoke(null, DcsFingerprintStatisticsUtil.DCS_LOG_TAG, DCS_EVENTID_IN_WHITELIST, data);
                cache.put(verifyStr, verifyStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isContained;
    }

    public ArrayList<String> getCptListByType(int tag) {
        CompatibilityUpdateInfo tempInfo = getUpdateInfo(true);
        if (tempInfo != null) {
            return tempInfo.getOneList(tag);
        }
        Slog.d("CompatibilityHelper", "can not get compatibility updateInfo");
        return null;
    }

    private boolean isInWhiteList(int type, String verifyStr, boolean containMode) {
        CompatibilityUpdateInfo tempInfo;
        if (DEBUG_CPT) {
            Slog.d("CompatibilityHelper", "WhiteList type: " + type + " verifyStr: " + verifyStr);
        }
        if (verifyStr == null || (tempInfo = getUpdateInfo(true)) == null) {
            return false;
        }
        if (type == 5) {
            verifyStr = extraServiceName(verifyStr);
        }
        if (type == 29) {
            return tempInfo.isGrEnable(verifyStr);
        }
        if (!containMode && !isContainMode(type)) {
            return tempInfo.isInWhiteList(type, verifyStr);
        }
        ArrayList<String> tmp = tempInfo.getOneList(type);
        if (tmp != null) {
            return isContained(tmp, verifyStr);
        }
        return false;
    }

    public int getTimeInWhiteList(int type, String verifyStr) {
        CompatibilityUpdateInfo tempInfo;
        if (DEBUG_CPT) {
            Slog.d("CompatibilityHelper", "WhiteList type: " + type + " verifyStr: " + verifyStr);
        }
        if (verifyStr == null || (tempInfo = getUpdateInfo(true)) == null) {
            return 0;
        }
        return tempInfo.getTimeInWhiteList(type, verifyStr);
    }

    public void customizePackageIfNeeded(PackageParser.Package pkg) {
        try {
            pkg.cpuAbiOverride = abiOverride(pkg.cpuAbiOverride, pkg.packageName);
            customizeHardwareAccelerateIfNeeded(pkg);
            customizeHardwareAccelerateForActivityIfNeeded(pkg);
            customizeVMSafeModeIfNeeded(pkg);
            customizeTargetSdkIfNeeded(pkg);
            customizePrivateFlagsIfNeeded(pkg);
        } catch (RuntimeException e) {
        }
    }

    private void customizePrivateFlagsIfNeeded(PackageParser.Package pkg) {
        if (isInWhiteList(FORCE_SKIP_TOAST_CHECK, pkg.packageName)) {
            pkg.applicationInfo.oppoPrivateFlags |= 2;
        }
        if (isInWhiteList(FORCE_SKIP_OPENNDK_CHECK, pkg.packageName)) {
            pkg.applicationInfo.oppoPrivateFlags |= 4;
        }
    }

    private boolean isBaiduProtectedApk(long length, String abiString) {
        int index = 0;
        if (abiString != null) {
            index = ABI_TO_INT_MAP.get(abiString).intValue();
        }
        if (index != 0) {
            if (index != 1) {
                if (index != 2) {
                    return false;
                }
                if (length == 408028 || length == 412124 || length == 416220) {
                    return true;
                }
                return false;
            } else if (length == 367076) {
                return true;
            } else {
                return false;
            }
        } else if (length == 610128) {
            return true;
        } else {
            return false;
        }
    }

    public void customizeNativeLibrariesIfNeeded(PackageParser.Package pkg) {
        String opensslLibraryDir;
        ArrayList tmpList = new ArrayList();
        boolean bOpenssl = false;
        if (isInWhiteList(23, pkg.packageName)) {
            tmpList.add("openssl");
        }
        File dir = new File(pkg.applicationInfo.nativeLibraryDir);
        if (dir.isDirectory()) {
            for (File tmp : dir.listFiles()) {
                String libName = tmp.getName();
                if (libName != null) {
                    if (bOpenssl || !isInWhiteList(24, libName)) {
                        if ("libssl.so".equals(libName) || "libcrypto.so".equals(libName)) {
                            bOpenssl = true;
                        }
                    } else if (!tmpList.contains("openssl")) {
                        tmpList.add("openssl");
                    }
                    if ("libdexinterpret.so".equals(libName) && isInWhiteList(28, pkg.packageName)) {
                        tmpList.add("atlas");
                    }
                }
            }
            if (bOpenssl || isInWhiteList(FORCE_DISABLE_OPENSSL, pkg.packageName)) {
                tmpList.remove("openssl");
            }
        }
        if (tmpList.size() > 0) {
            pkg.applicationInfo.specialNativeLibraryDirs = (String[]) tmpList.toArray(new String[tmpList.size()]);
        }
        if (isInWhiteList(FORCE_NEED_SPECIAL_LIBRARIES_IN, pkg.packageName)) {
            if (VMRuntime.is64BitInstructionSet(InstructionSets.getPrimaryInstructionSet(pkg.applicationInfo))) {
                opensslLibraryDir = "/vendor/lib64/openssl";
            } else {
                opensslLibraryDir = "/vendor/lib/openssl";
            }
            pkg.applicationInfo.nativeLibraryDir = opensslLibraryDir + File.pathSeparator + pkg.applicationInfo.nativeLibraryDir;
        }
    }

    private void customizeHardwareAccelerateIfNeeded(PackageParser.Package pkg) {
        if (isInWhiteList(14, pkg.packageName)) {
            pkg.baseHardwareAccelerated = true;
        } else if (isInWhiteList(15, pkg.packageName)) {
            pkg.baseHardwareAccelerated = false;
        } else {
            return;
        }
        changeActivitiesHW(pkg.activities, pkg.baseHardwareAccelerated);
    }

    private void customizeHardwareAccelerateForActivityIfNeeded(PackageParser.Package pkg) {
        if (isInWhiteList(21, pkg.packageName, true)) {
            changeActivityHW(pkg.activities, pkg.packageName, true);
        } else if (isInWhiteList(22, pkg.packageName, true)) {
            changeActivityHW(pkg.activities, pkg.packageName, false);
        }
    }

    private void customizeVMSafeModeIfNeeded(PackageParser.Package pkg) {
        if (isInWhiteList(12, pkg.packageName)) {
            pkg.applicationInfo.flags |= 16384;
        }
    }

    private void customizeSpecialLibraryIfNeeded(PackageParser.Package pkg) {
        if (isInWhiteList(23, pkg.packageName)) {
            pkg.applicationInfo.specialNativeLibraryDirs = new String[]{"openssl"};
        }
    }

    private void customizeTargetSdkIfNeeded(PackageParser.Package pkg) {
        if (isInWhiteList(25, pkg.packageName)) {
            pkg.applicationInfo.targetSdkVersion = 22;
        }
        if (isInWhiteList(FORCE_CHOOSING_TARGETSDK_M, pkg.packageName)) {
            pkg.applicationInfo.targetSdkVersion = 23;
        }
    }

    private void changeActivitiesHW(ArrayList<PackageParser.Activity> activities, boolean enable) {
        for (int i = activities.size() - 1; i >= 0; i--) {
            if (enable) {
                activities.get(i).info.flags |= 512;
            } else {
                activities.get(i).info.flags &= -513;
            }
        }
    }

    private void changeActivityHW(ArrayList<PackageParser.Activity> activities, String pkgName, boolean enable) {
        int i;
        for (int i2 = activities.size() - 1; i2 >= 0; i2--) {
            String cmp = pkgName + SliceClientPermissions.SliceAuthority.DELIMITER + activities.get(i2).className;
            if (enable) {
                i = 21;
            } else {
                i = 22;
            }
            if (isInWhiteList(i, cmp)) {
                if (enable) {
                    activities.get(i2).info.flags |= 512;
                } else {
                    activities.get(i2).info.flags &= -513;
                }
            }
        }
    }

    public String abiOverride(String packageAbiOverride, String pkgName) {
        if (pkgName == null) {
            return packageAbiOverride;
        }
        if (isInWhiteList(0, pkgName) && Build.SUPPORTED_32_BIT_ABIS.length > 0) {
            return Build.SUPPORTED_32_BIT_ABIS[0];
        }
        if (isInWhiteList(1, pkgName) && Build.SUPPORTED_64_BIT_ABIS.length > 0) {
            return Build.SUPPORTED_64_BIT_ABIS[0];
        }
        if (!isInWhiteList(2, pkgName) || Build.SUPPORTED_32_BIT_ABIS.length <= 1) {
            return packageAbiOverride;
        }
        return Build.SUPPORTED_32_BIT_ABIS[1];
    }

    public static int convertAbi2Int(String abiString) {
        return ABI_TO_INT_MAP.get(abiString).intValue();
    }

    public static String convertAbi2String(int abiInt) {
        return ABI_TO_STRING_MAP.get(Integer.valueOf(abiInt));
    }

    public static String convertAndroid2String(int andInt) {
        return ANDROID_TO_STRING_MAP.get(Integer.valueOf(andInt));
    }

    public static int convertAndroid2int(String andString) {
        return ANDROID_TO_INT_MAP.get(andString).intValue();
    }

    private String extraServiceName(String fullName) {
        if (fullName == null) {
            return "";
        }
        String[] temp = fullName.split("\\$");
        if (temp[0] != null) {
            return temp[0].split("\\@")[0];
        }
        return "";
    }

    private boolean isContained(ArrayList<String> tmpList, String verifyStr) {
        for (int i = 0; i < tmpList.size(); i++) {
            if (verifyStr != null && (verifyStr.contains(tmpList.get(i)) || tmpList.get(i).contains(verifyStr))) {
                return true;
            }
        }
        return false;
    }
}
