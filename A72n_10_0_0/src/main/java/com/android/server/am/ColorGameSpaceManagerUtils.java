package com.android.server.am;

import android.app.AppGlobals;
import android.common.OppoFeatureCache;
import android.content.pm.IPackageManager;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class ColorGameSpaceManagerUtils {
    private static final List<String> DEFAULT_NET_WHITE_PKG_LIST = Arrays.asList("com.alibaba.android.rimet", ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.tencent.mobileqq", "com.immomo.momo", "com.coloros.gamespacesdk", "com.kascend.chushou", "com.zing.zalo", "com.facebook.orca", "com.facebook.katana", "com.instagram.android", "jp.naver.line.android", "com.whatsapp", "com.bbm", "com.skype.raider", "com.viber.voip", "com.path", "com.facebook.lite", "com.truecaller", "com.bsb.hike", "com.snapchat.android", "com.twitter.android", "com.imo.android.imoim", "com.google.android.gm", "com.coloros.accegamesdk");
    private static final List<String> DEFAULT_SECURE_CPN_LIST = Arrays.asList("com.coloros.safecenter.privacy.view.password.AppUnlockPatternActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPatternForMoveTaskActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPatternWhiteBgActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPasswordActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPasswordForMoveTaskActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPasswordWhiteBgActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockComplexActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockComplexForMoveTaskActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockComplexWhiteBgActivity");
    private static final List<String> DEFAULT_SPECIAL_CPN_LIST = Arrays.asList("com.android.packageinstaller.permission.ui.GrantPermissionsActivity", "com.android.vpndialogs.ManageDialog");
    private static final List<String> DEFAULT_VIDEO_CPN_LIST = Arrays.asList("com.tencent.mm.plugin.voip.ui.VideoActivity", "com.facebook.rtc.activities.WebrtcIncallFragmentHostActivity");
    public static final int FLAG_DISABLE_NETWORK = 2;
    public static final int FLAG_PERFORMANCE_OPTIMIZATION = 1;
    private static final String GAME_SPACE_ELSA_SWITCH = "elsaswitch";
    private static final String GAME_SPACE_NET_SWITCH = "netswitch";
    private static final String GAME_SPACE_PACKAGE = "package";
    private static final String GAME_SPACE_SWITCH = "gamespaceswitch";
    private static final String NET_WHITE_PKG = "netwhitepkg";
    private static final String OPPO_GAME_SPACE_CONFIG_FILE_PATH = "/data/oppo/coloros/gamespace/sys_gamespace_config.xml";
    private static final String OPPO_GAME_SPACE_DIR_PATH = "/data/oppo/coloros/gamespace/";
    private static final String OPPO_GAME_SPACE_FILE_PATH = "/data/oppo/coloros/gamespace/oppo_gmsp.txt";
    private static final String OPPO_GAME_SPACE_MULUSER_GMSP_PATH = "gamespace/oppo_gmsp.txt";
    private static final String OPPO_GAME_SPACE_MULUSER_GMSP_UTIL_PATH = "gamespace/oppo_gmsp_util.txt";
    private static final String OPPO_GAME_SPACE_MULUSER_ROOT_PATH = "gamespace/";
    private static final String OPPO_GAME_SPACE_UTIL_FILE_PATH = "/data/oppo/coloros/gamespace/oppo_gmsp_util.txt";
    private static final String SECURE_CPN_LIST = "securecpn";
    private static final String SPECIAL_CPN_LIST = "specialcpn";
    private static final String TAG = "ColorGameSpaceManager";
    private static final String VIDEO_CPN_LIST = "videocpn";
    private static final String VIDEO_SWITCH = "videoswitch";
    private static ColorGameSpaceManagerUtils sGsmUtils = null;
    private ActivityManagerService mAMS;
    private ColorSettingsChangeListener mColorSettingsChangeListener = new ColorSettingsChangeListener(new Handler()) {
        /* class com.android.server.am.ColorGameSpaceManagerUtils.AnonymousClass1 */

        public void onSettingsChange(boolean selfChange, String customPath, int userId) {
            ColorGameSpaceManagerUtils.this.readGameSpacePkgFile();
            if (ColorGameSpaceManagerUtils.this.mDebugSwitch) {
                Slog.i(ColorGameSpaceManagerUtils.TAG, "onSettingsChange changed! get change id is :" + userId);
            }
            String uid = String.valueOf(userId);
            SystemProperties.set("persist.sys.gamespace.uid", uid);
            Slog.i(ColorGameSpaceManagerUtils.TAG, "current actived uid: " + uid);
        }
    };
    private boolean mDebugDetail = IColorGameSpaceManager.sDebugfDetail;
    private boolean mDebugSwitch = this.mDebugDetail;
    private int mDefaultInputMethodAppId;
    private List<String> mDisplayDeviceList = new ArrayList();
    private final Object mDisplayDeviceListLock = new Object();
    private List<Integer> mDozeRuleAppIdList = new ArrayList();
    private final Object mDozeRuleAppIdLock = new Object();
    private boolean mDynamicDebug = false;
    private boolean mElsaSwitch = false;
    private final Object mElsaSwitchLock = new Object();
    private FileObserverPolicy mGameSpaceFileObserver = null;
    private final Object mGameSpacePkgLock = new Object();
    private Map<String, Boolean> mGameSpacePkgMap = new HashMap();
    private FileObserverPolicy mGsConfigFileObserver = null;
    private boolean mGsSwitch = true;
    private final Object mGsSwitchLock = new Object();
    private FileObserverPolicy mGsUtilFileObserver = null;
    private final Object mGsUtilLock = new Object();
    private IPackageManager mIPm = null;
    private boolean mIsConfigEmpty = false;
    private boolean mNetSwitch = true;
    private final Object mNetSwitchLock = new Object();
    private final Object mNetWhitListLock = new Object();
    private List<Integer> mNetWhiteAppIdlist = new ArrayList();
    private final Object mNetWhiteAppIdlistLock = new Object();
    private List<String> mNetWhitePkglist = new ArrayList();
    private final Object mRadioRecordListLock = new Object();
    private List<Integer> mRadioRecordPidList = new ArrayList();
    private List<String> mSecureCpnList = new ArrayList();
    private final Object mSecureCpnListLock = new Object();
    private ColorSettingsChangeListener mSettingsConfigChangeListener = new ColorSettingsChangeListener(new Handler()) {
        /* class com.android.server.am.ColorGameSpaceManagerUtils.AnonymousClass2 */

        public void onSettingsChange(boolean selfChange, String customPath, int userId) {
            ColorGameSpaceManagerUtils.this.readGsUtilFile();
            if (ColorGameSpaceManagerUtils.this.mDebugSwitch) {
                Slog.i(ColorGameSpaceManagerUtils.TAG, "config changed! get change id is :" + userId);
            }
        }
    };
    private List<String> mSpecialCpnList = new ArrayList();
    private final Object mSpecilaCpnListLock = new Object();
    private int mSwitchFLAG = 3;
    private List<String> mVideoCpnList = new ArrayList();
    private final Object mVideoCpnListLock = new Object();
    private boolean mVideoSwitch = true;
    private final Object mVideoSwitchLock = new Object();

    private ColorGameSpaceManagerUtils() {
        Slog.i(TAG, "call ColorGameSpaceManagerUtils()");
    }

    public static ColorGameSpaceManagerUtils getInstance() {
        if (sGsmUtils == null) {
            sGsmUtils = new ColorGameSpaceManagerUtils();
        }
        return sGsmUtils;
    }

    public void init(ActivityManagerService ams) {
        this.mAMS = ams;
        initDir();
        initFileObserver();
        readGameSpacePkgFile();
        readGsUtilFile();
        readGsConfigFile();
        updateNetWhiteAppIdList();
        confirmDefaultGsList();
    }

    private void initDir() {
        Slog.i(TAG, "initDir start");
        File gameSpaceDir = new File(OPPO_GAME_SPACE_DIR_PATH);
        File gameSpaceFile = new File(OPPO_GAME_SPACE_FILE_PATH);
        File gsUtilFile = new File(OPPO_GAME_SPACE_UTIL_FILE_PATH);
        File gsConfigFile = new File(OPPO_GAME_SPACE_CONFIG_FILE_PATH);
        try {
            if (!gameSpaceDir.exists()) {
                gameSpaceDir.mkdir();
            }
            if (!gameSpaceFile.exists()) {
                gameSpaceFile.createNewFile();
            }
            if (!gsConfigFile.exists()) {
                gsConfigFile.createNewFile();
            }
            if (!gsUtilFile.exists()) {
                gsUtilFile.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "initDir failed!!!");
            e.printStackTrace();
        }
        changeModFile(OPPO_GAME_SPACE_FILE_PATH);
        changeModFile(OPPO_GAME_SPACE_UTIL_FILE_PATH);
        changeModFile(OPPO_GAME_SPACE_CONFIG_FILE_PATH);
    }

    private void initFileObserver() {
        this.mGameSpaceFileObserver = new FileObserverPolicy(OPPO_GAME_SPACE_FILE_PATH);
        this.mGameSpaceFileObserver.startWatching();
        this.mGsUtilFileObserver = new FileObserverPolicy(OPPO_GAME_SPACE_UTIL_FILE_PATH);
        this.mGsUtilFileObserver.startWatching();
        this.mGsConfigFileObserver = new FileObserverPolicy(OPPO_GAME_SPACE_CONFIG_FILE_PATH);
        this.mGsConfigFileObserver.startWatching();
        ColorSettings.registerChangeListener(this.mAMS.mContext, OPPO_GAME_SPACE_MULUSER_GMSP_PATH, 0, this.mColorSettingsChangeListener);
        ColorSettings.registerChangeListener(this.mAMS.mContext, OPPO_GAME_SPACE_MULUSER_GMSP_UTIL_PATH, 0, this.mSettingsConfigChangeListener);
    }

    public void unregisterListener() {
        ColorSettings.registerChangeListenerForAll(this.mAMS.mContext, OPPO_GAME_SPACE_MULUSER_GMSP_PATH, 0, this.mColorSettingsChangeListener);
        ColorSettings.registerChangeListener(this.mAMS.mContext, OPPO_GAME_SPACE_MULUSER_GMSP_UTIL_PATH, 0, this.mSettingsConfigChangeListener);
    }

    private void changeModFile(String fileName) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Slog.w(TAG, " " + e);
        }
    }

    public void readGameSpacePkgFile() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "readGameSpacePkgFile start");
        }
        try {
            readGameSpacePkgFromFileLocked(ColorSettings.readConfigAsUser(this.mAMS.mContext, OPPO_GAME_SPACE_MULUSER_GMSP_PATH, this.mAMS.mUserController.getCurrentUserId(), 0));
        } catch (IOException e) {
            e.printStackTrace();
            readGameSpacePkgFromFileLocked(new File(OPPO_GAME_SPACE_FILE_PATH));
        }
    }

    private void readGameSpacePkgFromFileLocked(InputStream stream) {
        IOException e;
        int type;
        if (stream != null) {
            if (this.mDebugSwitch) {
                Slog.i(TAG, "readGameSpacePkgFromFileLocked start");
            }
            synchronized (this.mGameSpacePkgLock) {
                if (!this.mGameSpacePkgMap.isEmpty()) {
                    this.mGameSpacePkgMap.clear();
                }
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, "utf-8");
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String tagName = parser.getName();
                            if (this.mDebugSwitch) {
                                Slog.i(TAG, " readGameSpacePkgFromFileLocked tagName=" + tagName);
                            }
                            if ("package".equals(tagName)) {
                                this.mGameSpacePkgMap.put(parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME), Boolean.valueOf(Boolean.parseBoolean(parser.getAttributeValue(null, "isInWhiteList"))));
                            }
                        }
                    } while (type != 1);
                    try {
                        stream.close();
                    } catch (IOException e2) {
                        e = e2;
                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        e = e4;
                    }
                } catch (Throwable th) {
                    try {
                        stream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                    throw th;
                }
            }
        }
        return;
        e.printStackTrace();
    }

    private void readGameSpacePkgFromFileLocked(File file) {
        IOException e;
        int type;
        if (this.mDebugSwitch) {
            Slog.i(TAG, "readGameSpacePkgFromFileLocked start");
        }
        synchronized (this.mGameSpacePkgLock) {
            if (!this.mGameSpacePkgMap.isEmpty()) {
                this.mGameSpacePkgMap.clear();
            }
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, "utf-8");
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.mDebugSwitch) {
                            Slog.i(TAG, " readGameSpacePkgFromFileLocked tagName=" + tagName);
                        }
                        if ("package".equals(tagName)) {
                            this.mGameSpacePkgMap.put(parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME), Boolean.valueOf(Boolean.parseBoolean(parser.getAttributeValue(null, "isInWhiteList"))));
                        }
                    }
                } while (type != 1);
                try {
                    stream2.close();
                } catch (IOException e2) {
                    e = e2;
                }
            } catch (Exception e3) {
                e3.printStackTrace();
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        e = e4;
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                throw th;
            }
        }
        e.printStackTrace();
    }

    public void readGsUtilFile() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "readGsUtilFile start");
        }
        try {
            readGsUtilFileLocked(ColorSettings.readConfigAsUser(this.mAMS.mContext, OPPO_GAME_SPACE_MULUSER_GMSP_UTIL_PATH, this.mAMS.mUserController.getCurrentUserId(), 0));
        } catch (IOException e) {
            e.printStackTrace();
            readGsUtilFileLocked(new File(OPPO_GAME_SPACE_UTIL_FILE_PATH));
        }
    }

    private void readGsUtilFileLocked(InputStream file) {
        if (file != null) {
            if (this.mDebugSwitch) {
                Slog.i(TAG, "readGsUtilFileLocked start");
            }
            synchronized (this.mGsUtilLock) {
                try {
                    InputStreamReader fReader = new InputStreamReader(file);
                    BufferedReader bReader = new BufferedReader(fReader);
                    String line = bReader.readLine();
                    if (line != null && !TextUtils.isEmpty(line)) {
                        if (this.mDebugSwitch) {
                            Slog.i(TAG, "readGsUtilFileLocked line = " + line);
                        }
                        this.mSwitchFLAG = Integer.parseInt(line.trim());
                    }
                    bReader.close();
                    fReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void readGsUtilFileLocked(File file) {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "readGsUtilFileLocked start");
        }
        synchronized (this.mGsUtilLock) {
            try {
                FileReader fReader = new FileReader(file);
                BufferedReader bReader = new BufferedReader(fReader);
                String line = bReader.readLine();
                if (line != null && !TextUtils.isEmpty(line)) {
                    if (this.mDebugSwitch) {
                        Slog.i(TAG, "readGsUtilFileLocked line = " + line);
                    }
                    this.mSwitchFLAG = Integer.parseInt(line.trim());
                }
                bReader.close();
                fReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void readGsConfigFile() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "readGsConfigFile");
        }
        File gsConfigFile = new File(OPPO_GAME_SPACE_CONFIG_FILE_PATH);
        if (!gsConfigFile.exists()) {
            Slog.i(TAG, "gsConfigFile isn't exist!");
        } else if (gsConfigFile.length() == 0) {
            setDefaultGsConfig();
        } else {
            readConfigFromFileLocked(gsConfigFile);
        }
    }

    private void readConfigFromFileLocked(File file) {
        StringBuilder sb;
        int type;
        if (this.mDebugSwitch) {
            Slog.i(TAG, "readConfigFromFileLocked start");
        }
        List<String> netWhitePkglist = new ArrayList<>();
        List<String> specialCpnList = new ArrayList<>();
        List<String> secureCpnList = new ArrayList<>();
        List<String> videoCpnList = new ArrayList<>();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2) {
                    String tagName = parser.getName();
                    if (this.mDebugSwitch) {
                        Slog.i(TAG, " readConfigFromFileLocked tagName=" + tagName);
                    }
                    if (GAME_SPACE_SWITCH.equals(tagName)) {
                        String gsSwitch = parser.nextText();
                        if (!gsSwitch.equals("")) {
                            synchronized (this.mGsSwitchLock) {
                                this.mGsSwitch = Boolean.parseBoolean(gsSwitch);
                            }
                            if (this.mDebugSwitch) {
                                Slog.i(TAG, " readConfigFromFileLocked gsSwitch = " + gsSwitch);
                            }
                        }
                    } else if (GAME_SPACE_NET_SWITCH.equals(tagName)) {
                        String gsNetSwitch = parser.nextText();
                        if (!gsNetSwitch.equals("")) {
                            synchronized (this.mNetSwitchLock) {
                                this.mNetSwitch = Boolean.parseBoolean(gsNetSwitch);
                            }
                            if (this.mDebugSwitch) {
                                Slog.i(TAG, " readConfigFromFileLocked gsNetSwitch = " + gsNetSwitch);
                            }
                        }
                    } else if (GAME_SPACE_ELSA_SWITCH.equals(tagName)) {
                        String gsElsaSwitch = parser.nextText();
                        if (!gsElsaSwitch.equals("")) {
                            synchronized (this.mElsaSwitchLock) {
                                this.mElsaSwitch = Boolean.parseBoolean(gsElsaSwitch);
                            }
                            if (this.mDebugSwitch) {
                                Slog.i(TAG, " readConfigFromFileLocked gsElsaSwitch = " + gsElsaSwitch);
                            }
                        }
                    } else if (VIDEO_SWITCH.equals(tagName)) {
                        String gsVideoSwitch = parser.nextText();
                        if (!gsVideoSwitch.equals("")) {
                            synchronized (this.mVideoSwitchLock) {
                                this.mVideoSwitch = Boolean.parseBoolean(gsVideoSwitch);
                            }
                            if (this.mDebugSwitch) {
                                Slog.i(TAG, " readConfigFromFileLocked gsVideoSwitch = " + gsVideoSwitch);
                            }
                        }
                    } else if (NET_WHITE_PKG.equals(tagName)) {
                        String pkg = parser.nextText();
                        if (!pkg.equals("")) {
                            netWhitePkglist.add(pkg);
                            if (this.mDebugDetail) {
                                Slog.i(TAG, " readConfigFromFileLocked pkg = " + pkg);
                            }
                        }
                    } else if (SPECIAL_CPN_LIST.equals(tagName)) {
                        String cpn = parser.nextText();
                        if (!cpn.equals("")) {
                            specialCpnList.add(cpn);
                            if (this.mDebugDetail) {
                                Slog.i(TAG, " readConfigFromFileLocked speical cpn = " + cpn);
                            }
                        }
                    } else if (SECURE_CPN_LIST.equals(tagName)) {
                        String cpn2 = parser.nextText();
                        if (!cpn2.equals("")) {
                            secureCpnList.add(cpn2);
                            if (this.mDebugDetail) {
                                Slog.i(TAG, " readConfigFromFileLocked secure cpn = " + cpn2);
                            }
                        }
                    } else if (VIDEO_CPN_LIST.equals(tagName)) {
                        String cpn3 = parser.nextText();
                        if (!cpn3.equals("")) {
                            videoCpnList.add(cpn3);
                            if (this.mDebugDetail) {
                                Slog.i(TAG, " readConfigFromFileLocked video cpn = " + cpn3);
                            }
                        }
                    }
                }
            } while (type != 1);
            synchronized (this.mNetWhitListLock) {
                if (!netWhitePkglist.isEmpty()) {
                    this.mNetWhitePkglist.clear();
                    this.mNetWhitePkglist.addAll(netWhitePkglist);
                }
            }
            synchronized (this.mSpecilaCpnListLock) {
                if (!specialCpnList.isEmpty()) {
                    this.mSpecialCpnList.clear();
                    this.mSpecialCpnList.addAll(specialCpnList);
                }
            }
            synchronized (this.mSecureCpnListLock) {
                if (!secureCpnList.isEmpty()) {
                    this.mSecureCpnList.clear();
                    this.mSecureCpnList.addAll(secureCpnList);
                }
            }
            synchronized (this.mVideoCpnListLock) {
                if (!videoCpnList.isEmpty()) {
                    this.mVideoCpnList.clear();
                    this.mVideoCpnList.addAll(videoCpnList);
                }
            }
            try {
                stream2.close();
                return;
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e);
            Slog.e(TAG, sb.toString());
        } catch (Exception e2) {
            Slog.e(TAG, "failed parsing ", e2);
            setDefaultGsConfig();
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "Failed to close state FileInputStream " + e4);
                }
            }
            throw th;
        }
    }

    public boolean isBpmEnable() {
        boolean result = isGameSpaceSwtichEnable() && isPerformanceEnable();
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isBpmEnable result = " + result);
        }
        return result;
    }

    public boolean isNetProtectEnable(String gamePkg) {
        boolean result = isNetSwitchEnable() && isDisableNetworkEnable(gamePkg);
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isNetProtectEnable result = " + result);
        }
        return result;
    }

    public boolean isPerformanceEnable() {
        boolean result = false;
        synchronized (this.mGsUtilLock) {
            if (1 == (this.mSwitchFLAG & 1)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isPerformanceEnable result = " + result);
        }
        return result;
    }

    private boolean isDisableNetworkEnable(String gamePkg) {
        boolean result = false;
        synchronized (this.mGsUtilLock) {
            if (TextUtils.isEmpty(gamePkg)) {
                result = true;
            } else {
                Boolean b = this.mGameSpacePkgMap.get(gamePkg);
                if (b != null) {
                    result = b.booleanValue();
                }
            }
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isDisableNetworkEnable result = " + result);
        }
        return result;
    }

    public boolean inGameSpacePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mGameSpacePkgLock) {
            if (this.mGameSpacePkgMap.containsKey(pkg)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "inGameSpacePkgList result = " + result);
        }
        return result;
    }

    public boolean inNetWhitePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mNetWhitListLock) {
            if (this.mNetWhitePkglist.contains(pkg)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "inNetWhitePkgList result = " + result + "  pkg = " + pkg);
        }
        return result;
    }

    public void updateNetWhiteAppIdList() {
        if (this.mDebugSwitch) {
            Slog.i(TAG, "updateNetWhiteAppIdList");
        }
        if (this.mIPm == null) {
            this.mIPm = AppGlobals.getPackageManager();
        }
        List<Integer> netWhiteAppIdlist = new ArrayList<>();
        List<String> pkgList = new ArrayList<>();
        synchronized (this.mNetWhitListLock) {
            pkgList.addAll(this.mNetWhitePkglist);
        }
        int uid = 0;
        int appId = 0;
        for (String pkg : pkgList) {
            try {
                uid = this.mIPm.getPackageUid(pkg, 8192, 0);
                appId = UserHandle.getAppId(uid);
                if (this.mDebugSwitch) {
                    Slog.i(TAG, "updateNetWhiteAppIdList  appId = " + appId);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "updateNetWhiteAppIdList RemoteException one");
            }
            if (appId != -1) {
                netWhiteAppIdlist.add(Integer.valueOf(appId));
            }
        }
        List<String> deviceList = new ArrayList<>();
        synchronized (this.mDisplayDeviceListLock) {
            deviceList.addAll(this.mDisplayDeviceList);
        }
        if (!deviceList.isEmpty()) {
            for (String pkg2 : deviceList) {
                try {
                    uid = this.mIPm.getPackageUid(pkg2, 8192, 0);
                    appId = UserHandle.getAppId(uid);
                    if (this.mDebugSwitch) {
                        Slog.i(TAG, "updateNetWhiteAppIdList  appId = " + appId);
                    }
                } catch (RemoteException e2) {
                    Slog.w(TAG, "updateNetWhiteAppIdList RemoteException two");
                }
                if (appId != -1) {
                    netWhiteAppIdlist.add(Integer.valueOf(appId));
                }
            }
        }
        synchronized (this.mNetWhiteAppIdlistLock) {
            this.mNetWhiteAppIdlist.clear();
            this.mNetWhiteAppIdlist.addAll(netWhiteAppIdlist);
        }
    }

    public boolean inNetWhiteAppIdList(int appId) {
        boolean result = false;
        synchronized (this.mNetWhiteAppIdlistLock) {
            if (this.mNetWhiteAppIdlist.contains(Integer.valueOf(appId))) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "inNetWhiteAppIdList result = " + result);
        }
        return result;
    }

    public List<Integer> getNetWhiteAppIdlist() {
        List<Integer> list;
        synchronized (this.mNetWhiteAppIdlistLock) {
            list = this.mNetWhiteAppIdlist;
        }
        return list;
    }

    public boolean isVideoInterceptEnable() {
        boolean z;
        synchronized (this.mVideoSwitchLock) {
            z = this.mVideoSwitch;
        }
        return z;
    }

    public boolean isGameSpaceSwtichEnable() {
        boolean z;
        synchronized (this.mGsSwitchLock) {
            z = this.mGsSwitch;
        }
        return z;
    }

    public boolean isNetSwitchEnable() {
        boolean z;
        synchronized (this.mNetSwitchLock) {
            z = this.mNetSwitch;
        }
        return z;
    }

    public boolean isElsaSwitchEnable() {
        boolean z;
        synchronized (this.mElsaSwitchLock) {
            z = this.mElsaSwitch;
        }
        return z;
    }

    public void addPkgToDisplayDeviceList(String pkgName) {
        if (pkgName != null) {
            synchronized (this.mDisplayDeviceListLock) {
                if (!this.mDisplayDeviceList.contains(pkgName)) {
                    this.mDisplayDeviceList.add(pkgName);
                }
            }
            OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).sendDeviceUpdateMessage();
        }
    }

    public void removePkgFromDisplayDeviceList(String pkgName) {
        if (pkgName != null) {
            synchronized (this.mDisplayDeviceListLock) {
                this.mDisplayDeviceList.remove(pkgName);
            }
            OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).sendDeviceUpdateMessage();
        }
    }

    public boolean isSpecialCpn(String cpn) {
        boolean result = false;
        synchronized (this.mSpecilaCpnListLock) {
            if (this.mSpecialCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isSpecialCpn result = " + result);
        }
        return result;
    }

    public boolean isSecureCpn(String cpn) {
        boolean result = false;
        synchronized (this.mSecureCpnListLock) {
            if (this.mSecureCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isSecureCpn result = " + result);
        }
        return result;
    }

    public boolean isVideoCpn(String cpn) {
        boolean result = false;
        synchronized (this.mVideoCpnListLock) {
            if (this.mVideoCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isVideoCpn result = " + result);
        }
        return result;
    }

    private void setDefaultGsConfig() {
        this.mGsSwitch = true;
        this.mNetSwitch = true;
        this.mElsaSwitch = false;
        this.mVideoSwitch = true;
        synchronized (this.mNetWhitListLock) {
            this.mNetWhitePkglist.clear();
            this.mNetWhitePkglist.addAll(DEFAULT_NET_WHITE_PKG_LIST);
        }
        synchronized (this.mSpecilaCpnListLock) {
            this.mSpecialCpnList.clear();
            this.mSpecialCpnList.addAll(DEFAULT_SPECIAL_CPN_LIST);
        }
        synchronized (this.mSecureCpnListLock) {
            this.mSecureCpnList.clear();
            this.mSecureCpnList.addAll(DEFAULT_SECURE_CPN_LIST);
        }
        synchronized (this.mVideoCpnListLock) {
            this.mVideoCpnList.clear();
            this.mVideoCpnList.addAll(DEFAULT_VIDEO_CPN_LIST);
        }
    }

    private void confirmDefaultGsList() {
        synchronized (this.mNetWhitListLock) {
            if (this.mNetWhitePkglist.isEmpty()) {
                this.mNetWhitePkglist.addAll(DEFAULT_NET_WHITE_PKG_LIST);
            }
        }
        synchronized (this.mSpecilaCpnListLock) {
            if (this.mSpecialCpnList.isEmpty()) {
                this.mSpecialCpnList.addAll(DEFAULT_SPECIAL_CPN_LIST);
            }
        }
        synchronized (this.mSecureCpnListLock) {
            if (this.mSecureCpnList.isEmpty()) {
                this.mSecureCpnList.addAll(DEFAULT_SECURE_CPN_LIST);
            }
        }
        synchronized (this.mVideoCpnListLock) {
            if (this.mVideoCpnList.isEmpty()) {
                this.mVideoCpnList.addAll(DEFAULT_VIDEO_CPN_LIST);
            }
        }
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.mFocusPath.equals(ColorGameSpaceManagerUtils.OPPO_GAME_SPACE_FILE_PATH)) {
                Slog.i(ColorGameSpaceManagerUtils.TAG, "mFocusPath OPPO_GAME_SPACE_FILE_PATH!");
                ColorGameSpaceManagerUtils.this.readGameSpacePkgFile();
            } else if (this.mFocusPath.equals(ColorGameSpaceManagerUtils.OPPO_GAME_SPACE_UTIL_FILE_PATH)) {
                Slog.i(ColorGameSpaceManagerUtils.TAG, "mFocusPath OPPO_GAME_SPACE_UTIL_FILE_PATH!");
                ColorGameSpaceManagerUtils.this.readGsUtilFile();
            } else if (this.mFocusPath.equals(ColorGameSpaceManagerUtils.OPPO_GAME_SPACE_CONFIG_FILE_PATH)) {
                Slog.i(ColorGameSpaceManagerUtils.TAG, "mFocusPath OPPO_GAME_SPACE_CONFIG_FILE_PATH!");
                ColorGameSpaceManagerUtils.this.readGsConfigFile();
                ColorGameSpaceManagerUtils.this.updateNetWhiteAppIdList();
            }
        }
    }

    public void setDynamicDebugSwitch() {
        this.mDynamicDebug = OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).getDynamicDebug();
        this.mDebugSwitch = this.mDebugDetail | this.mDynamicDebug;
    }

    /* access modifiers changed from: protected */
    public void handleDefatultInputMethodAppId(String defaultIm) {
        if (this.mIPm == null) {
            this.mIPm = AppGlobals.getPackageManager();
        }
        try {
            this.mDefaultInputMethodAppId = UserHandle.getAppId(this.mIPm.getPackageUid(defaultIm, 8192, 0));
        } catch (RemoteException e) {
            Slog.w(TAG, "handleDefatultInputMethodAppId RemoteException " + defaultIm);
        }
    }

    /* access modifiers changed from: protected */
    public int getDefatultInputMethodAppId() {
        return this.mDefaultInputMethodAppId;
    }

    /* access modifiers changed from: protected */
    public void handleDozeRuleWhite(boolean isGameMode, String gamePkg) {
        if (isGameMode) {
            handleCurrentGameDozeWhite(gamePkg);
        }
    }

    /* access modifiers changed from: protected */
    public void handleDozeRuleWhite(boolean isGameMode) {
        handleRadioRecordingList(isGameMode);
        if (!isGameMode) {
            this.mDozeRuleAppIdList.clear();
        }
    }

    /* access modifiers changed from: protected */
    public List<Integer> getDozeRuleWhiteAppIdlist() {
        List<Integer> list;
        synchronized (this.mDozeRuleAppIdLock) {
            if (this.mDynamicDebug) {
                Slog.d(TAG, "dozeRuleAppIdList " + this.mDozeRuleAppIdList);
            }
            list = this.mDozeRuleAppIdList;
        }
        return list;
    }

    /* access modifiers changed from: protected */
    public boolean inDozeRuleAppIdList(int appId) {
        boolean contains;
        synchronized (this.mDozeRuleAppIdLock) {
            contains = this.mDozeRuleAppIdList.contains(Integer.valueOf(appId));
        }
        return contains;
    }

    private void handleCurrentGameDozeWhite(String gamePkg) {
        if (gamePkg != null && !gamePkg.isEmpty()) {
            if (this.mIPm == null) {
                this.mIPm = AppGlobals.getPackageManager();
            }
            int appId = -1;
            try {
                appId = UserHandle.getAppId(this.mIPm.getPackageUid(gamePkg, 8192, 0));
            } catch (RemoteException e) {
                Slog.w(TAG, "handleCurrentGameDozeWhite RemoteException");
            }
            if (appId != -1) {
                synchronized (this.mDozeRuleAppIdLock) {
                    this.mDozeRuleAppIdList.add(Integer.valueOf(appId));
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addRadioRecordingList(int pid) {
        synchronized (this.mRadioRecordListLock) {
            if (!this.mRadioRecordPidList.contains(Integer.valueOf(pid))) {
                this.mRadioRecordPidList.add(Integer.valueOf(pid));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeRadioRecordingList(int pid) {
        synchronized (this.mRadioRecordListLock) {
            if (this.mRadioRecordPidList.contains(Integer.valueOf(pid))) {
                this.mRadioRecordPidList.remove(new Integer(pid));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleRadioRecordingList(boolean isGameSpaceMode) {
        int appId;
        List<Integer> radioRecordAppIdList = new ArrayList<>();
        synchronized (this.mRadioRecordListLock) {
            if (isGameSpaceMode) {
                Iterator<Integer> it = this.mRadioRecordPidList.iterator();
                while (it.hasNext()) {
                    int uid = FileUtils.getUid("/proc/" + it.next());
                    if (!(uid == -1 || (appId = UserHandle.getAppId(uid)) == -1)) {
                        radioRecordAppIdList.add(Integer.valueOf(appId));
                    }
                }
            } else {
                List<Integer> tmpPidList = new ArrayList<>();
                for (Integer pid : this.mRadioRecordPidList) {
                    if (FileUtils.getUid("/proc/" + pid) == -1) {
                        tmpPidList.add(pid);
                    }
                }
                this.mRadioRecordPidList.removeAll(tmpPidList);
            }
        }
        if (!radioRecordAppIdList.isEmpty()) {
            synchronized (this.mDozeRuleAppIdLock) {
                this.mDozeRuleAppIdList.addAll(radioRecordAppIdList);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setGameSpaceModeProp(boolean gameMode) {
        if (gameMode) {
            SystemProperties.set("debug.gamemode.value", "1");
        } else {
            SystemProperties.set("debug.gamemode.value", "0");
        }
    }
}
