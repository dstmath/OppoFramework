package com.android.server.am;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.LocationManagerService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class OppoGameSpaceManagerUtils {
    private static final List<String> DEFAULT_NET_WHITE_PKG_LIST = Arrays.asList(new String[]{"com.alibaba.android.rimet", "com.tencent.mm", "com.tencent.mobileqq", "com.immomo.momo", "com.coloros.gamespacesdk", "com.kascend.chushou", "com.zing.zalo", "com.facebook.orca", "com.facebook.katana", "com.instagram.android", "jp.naver.line.android", "com.whatsapp", "com.bbm", "com.skype.raider", "com.viber.voip", "com.path", "com.facebook.lite", "com.truecaller", "com.bsb.hike", "com.snapchat.android", "com.twitter.android", "com.imo.android.imoim", "com.google.android.gm", "com.coloros.accegamesdk"});
    private static final List<String> DEFAULT_SECURE_CPN_LIST = Arrays.asList(new String[]{"com.coloros.safecenter.privacy.view.password.AppUnlockPatternActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPatternForMoveTaskActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPatternWhiteBgActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPasswordActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPasswordForMoveTaskActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockPasswordWhiteBgActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockComplexActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockComplexForMoveTaskActivity", "com.coloros.safecenter.privacy.view.password.AppUnlockComplexWhiteBgActivity"});
    private static final List<String> DEFAULT_SPECIAL_CPN_LIST = Arrays.asList(new String[]{"com.android.packageinstaller.permission.ui.GrantPermissionsActivity", "com.android.vpndialogs.ManageDialog"});
    private static final List<String> DEFAULT_VIDEO_CPN_LIST = Arrays.asList(new String[]{"com.tencent.mm.plugin.voip.ui.VideoActivity", "com.facebook.rtc.activities.WebrtcIncallFragmentHostActivity"});
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
    private static final String OPPO_GAME_SPACE_UTIL_FILE_PATH = "/data/oppo/coloros/gamespace/oppo_gmsp_util.txt";
    private static final String SECURE_CPN_LIST = "securecpn";
    private static final String SPECIAL_CPN_LIST = "specialcpn";
    private static final String TAG = "OppoGameSpaceManager";
    private static final String VIDEO_CPN_LIST = "videocpn";
    private static final String VIDEO_SWITCH = "videoswitch";
    private static OppoGameSpaceManagerUtils sGsmUtils = null;
    private boolean mDebugDetail = OppoGameSpaceManager.sDebugfDetail;
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
    private List<String> mSpecialCpnList = new ArrayList();
    private final Object mSpecilaCpnListLock = new Object();
    private int mSwitchFLAG = 3;
    private List<String> mVideoCpnList = new ArrayList();
    private final Object mVideoCpnListLock = new Object();
    private boolean mVideoSwitch = true;
    private final Object mVideoSwitchLock = new Object();

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.mFocusPath.equals(OppoGameSpaceManagerUtils.OPPO_GAME_SPACE_FILE_PATH)) {
                Slog.i("OppoGameSpaceManager", "mFocusPath OPPO_GAME_SPACE_FILE_PATH!");
                OppoGameSpaceManagerUtils.this.readGameSpacePkgFile();
            } else if (this.mFocusPath.equals(OppoGameSpaceManagerUtils.OPPO_GAME_SPACE_UTIL_FILE_PATH)) {
                Slog.i("OppoGameSpaceManager", "mFocusPath OPPO_GAME_SPACE_UTIL_FILE_PATH!");
                OppoGameSpaceManagerUtils.this.readGsUtilFile();
            } else if (this.mFocusPath.equals(OppoGameSpaceManagerUtils.OPPO_GAME_SPACE_CONFIG_FILE_PATH)) {
                Slog.i("OppoGameSpaceManager", "mFocusPath OPPO_GAME_SPACE_CONFIG_FILE_PATH!");
                OppoGameSpaceManagerUtils.this.readGsConfigFile();
                OppoGameSpaceManagerUtils.this.updateNetWhiteAppIdList();
            }
        }
    }

    private OppoGameSpaceManagerUtils() {
    }

    public static OppoGameSpaceManagerUtils getInstance() {
        if (sGsmUtils == null) {
            sGsmUtils = new OppoGameSpaceManagerUtils();
        }
        return sGsmUtils;
    }

    public void init() {
        initDir();
        initFileObserver();
        readGameSpacePkgFile();
        readGsUtilFile();
        readGsConfigFile();
        updateNetWhiteAppIdList();
        confirmDefaultGsList();
    }

    private void initDir() {
        Slog.i("OppoGameSpaceManager", "initDir start");
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
            Slog.e("OppoGameSpaceManager", "initDir failed!!!");
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
    }

    private void changeModFile(String fileName) {
        try {
            Runtime.getRuntime().exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Slog.w("OppoGameSpaceManager", " " + e);
        }
    }

    public void readGameSpacePkgFile() {
        if (this.mDebugSwitch) {
            Slog.i("OppoGameSpaceManager", "readGameSpacePkgFile start");
        }
        readGameSpacePkgFromFileLocked(new File(OPPO_GAME_SPACE_FILE_PATH));
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x0096 A:{SYNTHETIC, Splitter: B:40:0x0096} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00a2 A:{SYNTHETIC, Splitter: B:47:0x00a2} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readGameSpacePkgFromFileLocked(File file) {
        Exception e;
        Throwable th;
        if (this.mDebugSwitch) {
            Slog.i("OppoGameSpaceManager", "readGameSpacePkgFromFileLocked start");
        }
        synchronized (this.mGameSpacePkgLock) {
            if (!this.mGameSpacePkgMap.isEmpty()) {
                this.mGameSpacePkgMap.clear();
            }
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, "utf-8");
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String tagName = parser.getName();
                            if (this.mDebugSwitch) {
                                Slog.i("OppoGameSpaceManager", " readGameSpacePkgFromFileLocked tagName=" + tagName);
                            }
                            if (GAME_SPACE_PACKAGE.equals(tagName)) {
                                this.mGameSpacePkgMap.put(parser.getAttributeValue(null, "name"), Boolean.valueOf(Boolean.parseBoolean(parser.getAttributeValue(null, "isInWhiteList"))));
                            }
                        }
                    } while (type != 1);
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    stream = stream2;
                } catch (Exception e3) {
                    e = e3;
                    stream = stream2;
                    try {
                        e.printStackTrace();
                        if (stream != null) {
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                return;
            }
        }
        return;
    }

    public void readGsUtilFile() {
        if (this.mDebugSwitch) {
            Slog.i("OppoGameSpaceManager", "readGsUtilFile start");
        }
        readGsUtilFileLocked(new File(OPPO_GAME_SPACE_UTIL_FILE_PATH));
    }

    private void readGsUtilFileLocked(File file) {
        if (this.mDebugSwitch) {
            Slog.i("OppoGameSpaceManager", "readGsUtilFileLocked start");
        }
        synchronized (this.mGsUtilLock) {
            try {
                FileReader fReader = new FileReader(file);
                BufferedReader bReader = new BufferedReader(fReader);
                String line = bReader.readLine();
                if (!(line == null || TextUtils.isEmpty(line))) {
                    if (this.mDebugSwitch) {
                        Slog.i("OppoGameSpaceManager", "readGsUtilFileLocked line = " + line);
                    }
                    this.mSwitchFLAG = Integer.parseInt(line.trim());
                }
                bReader.close();
                fReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public void readGsConfigFile() {
        if (this.mDebugSwitch) {
            Slog.i("OppoGameSpaceManager", "readGsConfigFile");
        }
        File gsConfigFile = new File(OPPO_GAME_SPACE_CONFIG_FILE_PATH);
        if (!gsConfigFile.exists()) {
            Slog.i("OppoGameSpaceManager", "gsConfigFile isn't exist!");
        } else if (gsConfigFile.length() == 0) {
            setDefaultGsConfig();
        } else {
            readConfigFromFileLocked(gsConfigFile);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:94:0x01ec A:{SYNTHETIC, Splitter: B:94:0x01ec} */
    /* JADX WARNING: Removed duplicated region for block: B:185:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0172 A:{SYNTHETIC, Splitter: B:73:0x0172} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFromFileLocked(File file) {
        Exception e;
        Throwable th;
        if (this.mDebugSwitch) {
            Slog.i("OppoGameSpaceManager", "readConfigFromFileLocked start");
        }
        List<String> netWhitePkglist = new ArrayList();
        List<String> specialCpnList = new ArrayList();
        List<String> secureCpnList = new ArrayList();
        List<String> videoCpnList = new ArrayList();
        FileInputStream fileInputStream = null;
        try {
            FileInputStream stream = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.mDebugSwitch) {
                            Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked tagName=" + tagName);
                        }
                        String cpn;
                        if (GAME_SPACE_SWITCH.equals(tagName)) {
                            String gsSwitch = parser.nextText();
                            if (!gsSwitch.equals("")) {
                                synchronized (this.mGsSwitchLock) {
                                    this.mGsSwitch = Boolean.parseBoolean(gsSwitch);
                                }
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked gsSwitch = " + gsSwitch);
                                }
                            }
                        } else if (GAME_SPACE_NET_SWITCH.equals(tagName)) {
                            String gsNetSwitch = parser.nextText();
                            if (!gsNetSwitch.equals("")) {
                                synchronized (this.mNetSwitchLock) {
                                    this.mNetSwitch = Boolean.parseBoolean(gsNetSwitch);
                                }
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked gsNetSwitch = " + gsNetSwitch);
                                }
                            }
                        } else if (GAME_SPACE_ELSA_SWITCH.equals(tagName)) {
                            String gsElsaSwitch = parser.nextText();
                            if (!gsElsaSwitch.equals("")) {
                                synchronized (this.mElsaSwitchLock) {
                                    this.mElsaSwitch = Boolean.parseBoolean(gsElsaSwitch);
                                }
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked gsElsaSwitch = " + gsElsaSwitch);
                                }
                            }
                        } else if (VIDEO_SWITCH.equals(tagName)) {
                            String gsVideoSwitch = parser.nextText();
                            if (!gsVideoSwitch.equals("")) {
                                synchronized (this.mVideoSwitchLock) {
                                    this.mVideoSwitch = Boolean.parseBoolean(gsVideoSwitch);
                                }
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked gsVideoSwitch = " + gsVideoSwitch);
                                }
                            }
                        } else if (NET_WHITE_PKG.equals(tagName)) {
                            String pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                netWhitePkglist.add(pkg);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked pkg = " + pkg);
                                }
                            }
                        } else if (SPECIAL_CPN_LIST.equals(tagName)) {
                            cpn = parser.nextText();
                            if (!cpn.equals("")) {
                                specialCpnList.add(cpn);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked speical cpn = " + cpn);
                                }
                            }
                        } else if (SECURE_CPN_LIST.equals(tagName)) {
                            cpn = parser.nextText();
                            if (!cpn.equals("")) {
                                secureCpnList.add(cpn);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked secure cpn = " + cpn);
                                }
                            }
                        } else if (VIDEO_CPN_LIST.equals(tagName)) {
                            cpn = parser.nextText();
                            if (!cpn.equals("")) {
                                videoCpnList.add(cpn);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked video cpn = " + cpn);
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
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e2) {
                        Slog.e("OppoGameSpaceManager", "Failed to close state FileInputStream " + e2);
                    }
                }
                fileInputStream = stream;
            } catch (Exception e3) {
                e = e3;
                fileInputStream = stream;
                try {
                    Slog.e("OppoGameSpaceManager", "failed parsing ", e);
                    setDefaultGsConfig();
                    if (fileInputStream == null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            Slog.e("OppoGameSpaceManager", "Failed to close state FileInputStream " + e22);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e222) {
                            Slog.e("OppoGameSpaceManager", "Failed to close state FileInputStream " + e222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = stream;
                if (fileInputStream != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Slog.e("OppoGameSpaceManager", "failed parsing ", e);
            setDefaultGsConfig();
            if (fileInputStream == null) {
            }
        }
    }

    public boolean isBpmEnable() {
        boolean result = isGameSpaceSwtichEnable() ? isPerformanceEnable() : false;
        if (this.mDynamicDebug) {
            Slog.i("OppoGameSpaceManager", "isBpmEnable result = " + result);
        }
        return result;
    }

    public boolean isNetProtectEnable(String gamePkg) {
        boolean result = isNetSwitchEnable() ? isDisableNetworkEnable(gamePkg) : false;
        if (this.mDynamicDebug) {
            Slog.i("OppoGameSpaceManager", "isNetProtectEnable result = " + result);
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
            Slog.i("OppoGameSpaceManager", "isPerformanceEnable result = " + result);
        }
        return result;
    }

    private boolean isDisableNetworkEnable(String gamePkg) {
        boolean result = false;
        synchronized (this.mGsUtilLock) {
            if (TextUtils.isEmpty(gamePkg)) {
                result = true;
            } else {
                Boolean b = (Boolean) this.mGameSpacePkgMap.get(gamePkg);
                if (b != null) {
                    result = b.booleanValue();
                }
            }
        }
        if (this.mDynamicDebug) {
            Slog.i("OppoGameSpaceManager", "isDisableNetworkEnable result = " + result);
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
            Slog.i("OppoGameSpaceManager", "inGameSpacePkgList result = " + result);
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
            Slog.i("OppoGameSpaceManager", "inNetWhitePkgList result = " + result + "  pkg = " + pkg);
        }
        return result;
    }

    public void updateNetWhiteAppIdList() {
        if (this.mDebugSwitch) {
            Slog.i("OppoGameSpaceManager", "updateNetWhiteAppIdList");
        }
        if (this.mIPm == null) {
            this.mIPm = AppGlobals.getPackageManager();
        }
        int appId = 0;
        List<Integer> netWhiteAppIdlist = new ArrayList();
        List<String> pkgList = new ArrayList();
        synchronized (this.mNetWhitListLock) {
            pkgList.addAll(this.mNetWhitePkglist);
        }
        for (String pkg : pkgList) {
            try {
                appId = UserHandle.getAppId(this.mIPm.getPackageUid(pkg, 8192, 0));
                if (this.mDebugSwitch) {
                    Slog.i("OppoGameSpaceManager", "updateNetWhiteAppIdList  appId = " + appId);
                }
            } catch (RemoteException e) {
                Slog.w("OppoGameSpaceManager", "updateNetWhiteAppIdList RemoteException one");
            }
            if (appId != -1) {
                netWhiteAppIdlist.add(Integer.valueOf(appId));
            }
        }
        List<String> deviceList = new ArrayList();
        synchronized (this.mDisplayDeviceListLock) {
            deviceList.addAll(this.mDisplayDeviceList);
        }
        if (!deviceList.isEmpty()) {
            for (String pkg2 : deviceList) {
                try {
                    appId = UserHandle.getAppId(this.mIPm.getPackageUid(pkg2, 8192, 0));
                    if (this.mDebugSwitch) {
                        Slog.i("OppoGameSpaceManager", "updateNetWhiteAppIdList  appId = " + appId);
                    }
                } catch (RemoteException e2) {
                    Slog.w("OppoGameSpaceManager", "updateNetWhiteAppIdList RemoteException two");
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
            Slog.i("OppoGameSpaceManager", "inNetWhiteAppIdList result = " + result);
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
            OppoGameSpaceManager.getInstance().sendDeviceUpdateMessage();
        }
    }

    public void removePkgFromDisplayDeviceList(String pkgName) {
        if (pkgName != null) {
            synchronized (this.mDisplayDeviceListLock) {
                this.mDisplayDeviceList.remove(pkgName);
            }
            OppoGameSpaceManager.getInstance().sendDeviceUpdateMessage();
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
            Slog.i("OppoGameSpaceManager", "isSpecialCpn result = " + result);
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
            Slog.i("OppoGameSpaceManager", "isSecureCpn result = " + result);
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
            Slog.i("OppoGameSpaceManager", "isVideoCpn result = " + result);
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

    public void setDynamicDebugSwitch() {
        this.mDynamicDebug = OppoGameSpaceManager.getInstance().mDynamicDebug;
        this.mDebugSwitch = this.mDebugDetail | this.mDynamicDebug;
    }

    protected void handleDefatultInputMethodAppId(String defaultIm) {
        if (this.mIPm == null) {
            this.mIPm = AppGlobals.getPackageManager();
        }
        try {
            this.mDefaultInputMethodAppId = UserHandle.getAppId(this.mIPm.getPackageUid(defaultIm, 8192, 0));
        } catch (RemoteException e) {
            Slog.w("OppoGameSpaceManager", "handleDefatultInputMethodAppId RemoteException " + defaultIm);
        }
    }

    protected int getDefatultInputMethodAppId() {
        return this.mDefaultInputMethodAppId;
    }

    protected void handleDozeRuleWhite(boolean isGameMode, String gamePkg) {
        if (isGameMode) {
            handleCurrentGameDozeWhite(gamePkg);
        }
    }

    protected void handleDozeRuleWhite(boolean isGameMode) {
        handleRadioRecordingList(isGameMode);
        if (!isGameMode) {
            this.mDozeRuleAppIdList.clear();
        }
    }

    protected List<Integer> getDozeRuleWhiteAppIdlist() {
        List<Integer> list;
        synchronized (this.mDozeRuleAppIdLock) {
            if (this.mDynamicDebug) {
                Slog.d("OppoGameSpaceManager", "dozeRuleAppIdList " + this.mDozeRuleAppIdList);
            }
            list = this.mDozeRuleAppIdList;
        }
        return list;
    }

    protected boolean inDozeRuleAppIdList(int appId) {
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
                Slog.w("OppoGameSpaceManager", "handleCurrentGameDozeWhite RemoteException");
            }
            if (appId != -1) {
                synchronized (this.mDozeRuleAppIdLock) {
                    this.mDozeRuleAppIdList.add(Integer.valueOf(appId));
                }
            }
        }
    }

    protected void addRadioRecordingList(int pid) {
        synchronized (this.mRadioRecordListLock) {
            if (!this.mRadioRecordPidList.contains(Integer.valueOf(pid))) {
                this.mRadioRecordPidList.add(Integer.valueOf(pid));
            }
        }
    }

    protected void removeRadioRecordingList(int pid) {
        synchronized (this.mRadioRecordListLock) {
            if (this.mRadioRecordPidList.contains(Integer.valueOf(pid))) {
                this.mRadioRecordPidList.remove(new Integer(pid));
            }
        }
    }

    protected void handleRadioRecordingList(boolean isGameSpaceMode) {
        List<Integer> radioRecordAppIdList = new ArrayList();
        synchronized (this.mRadioRecordListLock) {
            if (isGameSpaceMode) {
                for (Integer pid : this.mRadioRecordPidList) {
                    int uid = FileUtils.getUid("/proc/" + pid);
                    if (uid != -1) {
                        int appId = UserHandle.getAppId(uid);
                        if (appId != -1) {
                            radioRecordAppIdList.add(Integer.valueOf(appId));
                        }
                    }
                }
            } else {
                List<Integer> tmpPidList = new ArrayList();
                for (Integer pid2 : this.mRadioRecordPidList) {
                    if (FileUtils.getUid("/proc/" + pid2) == -1) {
                        tmpPidList.add(pid2);
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

    protected void setGameSpaceModeProp(boolean gameMode) {
        if (gameMode) {
            SystemProperties.set("debug.gamemode.value", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        } else {
            SystemProperties.set("debug.gamemode.value", "0");
        }
    }
}
