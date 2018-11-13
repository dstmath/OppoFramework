package com.android.server.am;

import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoGameSpaceManagerUtils {
    private static final List<String> DEFAULT_NET_WHITE_PKG_LIST = null;
    private static final List<String> DEFAULT_SECURE_CPN_LIST = null;
    private static final List<String> DEFAULT_SPECIAL_CPN_LIST = null;
    private static final List<String> DEFAULT_VIDEO_CPN_LIST = null;
    public static final int FLAG_DISABLE_NETWORK = 2;
    public static final int FLAG_PERFORMANCE_OPTIMIZATION = 1;
    private static final String GAME_SPACE_ELSA_SWITCH = "elsaswitch";
    private static final String GAME_SPACE_NET_SWITCH = "netswitch";
    private static final String GAME_SPACE_SWITCH = "gamespaceswitch";
    private static final String NET_WHITE_PKG = "netwhitepkg";
    private static final String OPPO_GAME_SPACE_CONFIG_FILE_PATH = "/data/system/sys_gamespace_config.xml";
    private static final String OPPO_GAME_SPACE_FILE_PATH = "/data/system/oppo_gmsp.txt";
    private static final String OPPO_GAME_SPACE_UTIL_FILE_PATH = "/data/system/oppo_gmsp_util.txt";
    private static final String SECURE_CPN_LIST = "securecpn";
    private static final String SPECIAL_CPN_LIST = "specialcpn";
    private static final String TAG = "OppoGameSpaceManager";
    private static final String VIDEO_CPN_LIST = "videocpn";
    private static final String VIDEO_SWITCH = "videoswitch";
    private static OppoGameSpaceManagerUtils sGsmUtils;
    private boolean mDebugDetail;
    private boolean mDebugSwitch;
    private int mDefaultInputMethodAppId;
    private List<String> mDisplayDeviceList;
    private final Object mDisplayDeviceListLock;
    private List<Integer> mDozeRuleAppIdList;
    private final Object mDozeRuleAppIdLock;
    private boolean mDynamicDebug;
    private boolean mElsaSwitch;
    private final Object mElsaSwitchLock;
    private FileObserverPolicy mGameSpaceFileObserver;
    private final Object mGameSpacePkgLock;
    private List<String> mGameSpacePkglist;
    private FileObserverPolicy mGsConfigFileObserver;
    private boolean mGsSwitch;
    private final Object mGsSwitchLock;
    private FileObserverPolicy mGsUtilFileObserver;
    private final Object mGsUtilLock;
    private IPackageManager mIPm;
    private boolean mIsConfigEmpty;
    private boolean mNetSwitch;
    private final Object mNetSwitchLock;
    private final Object mNetWhitListLock;
    private List<Integer> mNetWhiteAppIdlist;
    private final Object mNetWhiteAppIdlistLock;
    private List<String> mNetWhitePkglist;
    private final Object mRadioRecordListLock;
    private List<Integer> mRadioRecordPidList;
    private List<String> mSecureCpnList;
    private final Object mSecureCpnListLock;
    private List<String> mSpecialCpnList;
    private final Object mSpecilaCpnListLock;
    private int mSwitchFLAG;
    private List<String> mVideoCpnList;
    private final Object mVideoCpnListLock;
    private boolean mVideoSwitch;
    private final Object mVideoSwitchLock;

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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoGameSpaceManagerUtils.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoGameSpaceManagerUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoGameSpaceManagerUtils.<clinit>():void");
    }

    private OppoGameSpaceManagerUtils() {
        this.mGameSpacePkgLock = new Object();
        this.mNetWhitListLock = new Object();
        this.mNetWhiteAppIdlistLock = new Object();
        this.mGsUtilLock = new Object();
        this.mGsSwitchLock = new Object();
        this.mNetSwitchLock = new Object();
        this.mElsaSwitchLock = new Object();
        this.mDisplayDeviceListLock = new Object();
        this.mDozeRuleAppIdLock = new Object();
        this.mRadioRecordListLock = new Object();
        this.mSpecilaCpnListLock = new Object();
        this.mSecureCpnListLock = new Object();
        this.mVideoCpnListLock = new Object();
        this.mVideoSwitchLock = new Object();
        this.mGameSpacePkglist = new ArrayList();
        this.mNetWhitePkglist = new ArrayList();
        this.mNetWhiteAppIdlist = new ArrayList();
        this.mDisplayDeviceList = new ArrayList();
        this.mDozeRuleAppIdList = new ArrayList();
        this.mRadioRecordPidList = new ArrayList();
        this.mSpecialCpnList = new ArrayList();
        this.mSecureCpnList = new ArrayList();
        this.mVideoCpnList = new ArrayList();
        this.mGameSpaceFileObserver = null;
        this.mGsUtilFileObserver = null;
        this.mGsConfigFileObserver = null;
        this.mIPm = null;
        this.mGsSwitch = true;
        this.mNetSwitch = true;
        this.mElsaSwitch = false;
        this.mVideoSwitch = true;
        this.mIsConfigEmpty = false;
        this.mSwitchFLAG = 3;
        this.mDebugDetail = OppoGameSpaceManager.sDebugfDetail;
        this.mDebugSwitch = this.mDebugDetail;
        this.mDynamicDebug = false;
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
        File gameSpaceFile = new File(OPPO_GAME_SPACE_FILE_PATH);
        File gsUtilFile = new File(OPPO_GAME_SPACE_UTIL_FILE_PATH);
        File gsConfigFile = new File(OPPO_GAME_SPACE_CONFIG_FILE_PATH);
        try {
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

    private void readGameSpacePkgFromFileLocked(File file) {
        if (this.mDebugSwitch) {
            Slog.i("OppoGameSpaceManager", "readGameSpacePkgFromFileLocked start");
        }
        synchronized (this.mGameSpacePkgLock) {
            if (!this.mGameSpacePkglist.isEmpty()) {
                this.mGameSpacePkglist.clear();
            }
            try {
                FileReader fReader = new FileReader(file);
                BufferedReader bReader = new BufferedReader(fReader);
                while (true) {
                    String line = bReader.readLine();
                    if (line == null) {
                        break;
                    } else if (!TextUtils.isEmpty(line)) {
                        Slog.i("OppoGameSpaceManager", "readGameSpacePkgFromFileLocked line = " + line);
                        this.mGameSpacePkglist.add(line.trim());
                    }
                }
                bReader.close();
                fReader.close();
            } catch (Exception e) {
                e.printStackTrace();
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
                            if (!gsSwitch.equals(IElsaManager.EMPTY_PACKAGE)) {
                                synchronized (this.mGsSwitchLock) {
                                    this.mGsSwitch = Boolean.parseBoolean(gsSwitch);
                                }
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked gsSwitch = " + gsSwitch);
                                }
                            }
                        } else if (GAME_SPACE_NET_SWITCH.equals(tagName)) {
                            String gsNetSwitch = parser.nextText();
                            if (!gsNetSwitch.equals(IElsaManager.EMPTY_PACKAGE)) {
                                synchronized (this.mNetSwitchLock) {
                                    this.mNetSwitch = Boolean.parseBoolean(gsNetSwitch);
                                }
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked gsNetSwitch = " + gsNetSwitch);
                                }
                            }
                        } else if (GAME_SPACE_ELSA_SWITCH.equals(tagName)) {
                            String gsElsaSwitch = parser.nextText();
                            if (!gsElsaSwitch.equals(IElsaManager.EMPTY_PACKAGE)) {
                                synchronized (this.mElsaSwitchLock) {
                                    this.mElsaSwitch = Boolean.parseBoolean(gsElsaSwitch);
                                }
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked gsElsaSwitch = " + gsElsaSwitch);
                                }
                            }
                        } else if (VIDEO_SWITCH.equals(tagName)) {
                            String gsVideoSwitch = parser.nextText();
                            if (!gsVideoSwitch.equals(IElsaManager.EMPTY_PACKAGE)) {
                                synchronized (this.mVideoSwitchLock) {
                                    this.mVideoSwitch = Boolean.parseBoolean(gsVideoSwitch);
                                }
                                if (this.mDebugSwitch) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked gsVideoSwitch = " + gsVideoSwitch);
                                }
                            }
                        } else if (NET_WHITE_PKG.equals(tagName)) {
                            String pkg = parser.nextText();
                            if (!pkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                netWhitePkglist.add(pkg);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked pkg = " + pkg);
                                }
                            }
                        } else if (SPECIAL_CPN_LIST.equals(tagName)) {
                            cpn = parser.nextText();
                            if (!cpn.equals(IElsaManager.EMPTY_PACKAGE)) {
                                specialCpnList.add(cpn);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked speical cpn = " + cpn);
                                }
                            }
                        } else if (SECURE_CPN_LIST.equals(tagName)) {
                            cpn = parser.nextText();
                            if (!cpn.equals(IElsaManager.EMPTY_PACKAGE)) {
                                secureCpnList.add(cpn);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoGameSpaceManager", " readConfigFromFileLocked secure cpn = " + cpn);
                                }
                            }
                        } else if (VIDEO_CPN_LIST.equals(tagName)) {
                            cpn = parser.nextText();
                            if (!cpn.equals(IElsaManager.EMPTY_PACKAGE)) {
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

    public boolean isNetProtectEnable() {
        boolean result = isNetSwitchEnable() ? isDisableNetworkEnable() : false;
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

    private boolean isDisableNetworkEnable() {
        boolean result = false;
        synchronized (this.mGsUtilLock) {
            if (2 == (this.mSwitchFLAG & 2)) {
                result = true;
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
            if (this.mGameSpacePkglist.contains(pkg)) {
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
                appId = UserHandle.getAppId(this.mIPm.getPackageUid(pkg, DumpState.DUMP_PREFERRED_XML, 0));
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
                    appId = UserHandle.getAppId(this.mIPm.getPackageUid(pkg2, DumpState.DUMP_PREFERRED_XML, 0));
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
            this.mDefaultInputMethodAppId = UserHandle.getAppId(this.mIPm.getPackageUid(defaultIm, DumpState.DUMP_PREFERRED_XML, 0));
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
                appId = UserHandle.getAppId(this.mIPm.getPackageUid(gamePkg, DumpState.DUMP_PREFERRED_XML, 0));
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
}
