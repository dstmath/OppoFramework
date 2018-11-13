package com.android.server.am;

import android.os.FileObserver;
import android.os.FileUtils;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class OppoFreeFormManagerUtils {
    private static final String DEFAULT_FREEFORM_CONFIG_FILE_PATH = "/system/oppo/sys_freeform_config.xml";
    private static final List<String> DEFAULT_NEXT_NEED_FULLSCREEN_CPN_LIST = Arrays.asList(new String[]{"com.tencent.mm.plugin.appbrand.ui.AppBrandUI", "com.whatsapp.voipcalling.VoipActivityV2"});
    private static final String FREEFORM_PKG = "freeformPkg";
    private static final String FREEFORM_SWITCH = "freeformSwitch";
    private static final String FULLSCREEN_CPN = "fullscreenCpn";
    private static final String NEXT_NEED_FULLSCREEN_CPN_LIST = "nextneedfullscreenCpn";
    private static final String OPPO_FREEFORM_CONFIG_FILE_PATH = "/data/oppo/coloros/freeform/sys_freeform_config.xml";
    private static final String OPPO_FREEFORM_DIR_PATH = "/data/oppo/coloros/freeform/";
    private static final String ROOT_PKG = "rootPkg";
    private static final String SECURE_CPN_LIST = "securecpn";
    private static final String SPECIAL_CPN_LIST = "specialcpn";
    private static final String TAG = "OppoFreeFormManagerService";
    private static OppoFreeFormManagerUtils sFreeFormManagerUtils = null;
    private boolean mDebugDetail = OppoFreeFormManagerService.sDebugfDetail;
    private boolean mDebugSwitch = this.mDebugDetail;
    private boolean mDynamicDebug = false;
    private FileObserverPolicy mFreeformFileObserver = null;
    private List<String> mFreeformPkgList = new ArrayList();
    private final Object mFreeformPkgListLock = new Object();
    private boolean mFreeformSwitch = true;
    private final Object mFreeformSwitchLock = new Object();
    private List<String> mFullscreenCpnList = new ArrayList();
    private final Object mFullscreenCpnListLock = new Object();
    private List<String> mNextNeddFullscreenCpnList = new ArrayList(DEFAULT_NEXT_NEED_FULLSCREEN_CPN_LIST);
    private final Object mNextNeedFullscreenCpnListLock = new Object();
    private List<String> mRootPkgList = new ArrayList();
    private final Object mRootPkgListLock = new Object();
    private List<String> mSecureCpnList = new ArrayList();
    private final Object mSecureCpnListLock = new Object();
    private List<String> mSpecialCpnList = new ArrayList();
    private final Object mSpecilaCpnListLock = new Object();

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(OppoFreeFormManagerUtils.OPPO_FREEFORM_CONFIG_FILE_PATH)) {
                Slog.i("OppoFreeFormManagerService", "mFocusPath OPPO_FREEFORM_CONFIG_FILE_PATH!");
                OppoFreeFormManagerUtils.this.readFreeformConfigFile();
            }
        }
    }

    private OppoFreeFormManagerUtils() {
    }

    public static OppoFreeFormManagerUtils getInstance() {
        if (sFreeFormManagerUtils == null) {
            sFreeFormManagerUtils = new OppoFreeFormManagerUtils();
        }
        return sFreeFormManagerUtils;
    }

    public void init() {
        initDir();
        initFileObserver();
        readFreeformConfigFile();
    }

    private void initDir() {
        Slog.i("OppoFreeFormManagerService", "initDir start");
        File freeformDir = new File(OPPO_FREEFORM_DIR_PATH);
        try {
            if (!freeformDir.exists()) {
                freeformDir.mkdir();
            }
            copyFile(DEFAULT_FREEFORM_CONFIG_FILE_PATH, OPPO_FREEFORM_CONFIG_FILE_PATH);
            confirmFileExist(OPPO_FREEFORM_CONFIG_FILE_PATH);
            changeModFile(OPPO_FREEFORM_CONFIG_FILE_PATH);
        } catch (Exception e) {
            Slog.e("OppoFreeFormManagerService", "initDir failed!!!");
        }
    }

    private void initFileObserver() {
        this.mFreeformFileObserver = new FileObserverPolicy(OPPO_FREEFORM_CONFIG_FILE_PATH);
        this.mFreeformFileObserver.startWatching();
    }

    private void changeModFile(String fileName) {
        try {
            Runtime.getRuntime().exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Slog.w("OppoFreeFormManagerService", " " + e);
        }
    }

    private void copyFile(String fromFile, String toFile) throws IOException {
        File targetFile = new File(toFile);
        if (!targetFile.exists()) {
            FileUtils.copyFile(new File(fromFile), targetFile);
        }
    }

    private void confirmFileExist(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public void readFreeformConfigFile() {
        if (this.mDebugSwitch) {
            Slog.i("OppoFreeFormManagerService", "readFreeformConfigFile start");
        }
        readFreeformConfigFileLocked(new File(OPPO_FREEFORM_CONFIG_FILE_PATH));
    }

    /* JADX WARNING: Removed duplicated region for block: B:104:0x0233 A:{SYNTHETIC, Splitter: B:104:0x0233} */
    /* JADX WARNING: Removed duplicated region for block: B:170:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01c7 A:{SYNTHETIC, Splitter: B:89:0x01c7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readFreeformConfigFileLocked(File file) {
        Exception e;
        Throwable th;
        if (this.mDebugSwitch) {
            Slog.i("OppoFreeFormManagerService", "readConfigFileLocked start");
        }
        List<String> freeformPkglist = new ArrayList();
        List<String> rootPkgList = new ArrayList();
        List<String> fullscreenCpnList = new ArrayList();
        List<String> nextNeedFullscreenCpnList = new ArrayList();
        List<String> specialCpnList = new ArrayList();
        List<String> secureCpnList = new ArrayList();
        FileInputStream fileInputStream = null;
        try {
            InputStream fileInputStream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.mDynamicDebug) {
                            Slog.i("OppoFreeFormManagerService", " readConfigFileLocked tagName=" + tagName);
                        }
                        String cpn;
                        if (FREEFORM_SWITCH.equals(tagName)) {
                            String freeformSwitch = parser.nextText();
                            if (!freeformSwitch.equals("")) {
                                synchronized (this.mFreeformSwitchLock) {
                                    this.mFreeformSwitch = Boolean.parseBoolean(freeformSwitch);
                                }
                                if (this.mDynamicDebug) {
                                    Slog.i("OppoFreeFormManagerService", " readConfigFromFileLocked freeformSwitch = " + freeformSwitch);
                                }
                            }
                        } else if (FREEFORM_PKG.equals(tagName)) {
                            String freeformPkg = parser.nextText();
                            if (!freeformPkg.equals("")) {
                                freeformPkglist.add(freeformPkg);
                                if (this.mDynamicDebug) {
                                    Slog.i("OppoFreeFormManagerService", " readConfigFromFileLocked freeformPkg = " + freeformPkg);
                                }
                            }
                        } else if (FULLSCREEN_CPN.equals(tagName)) {
                            String fullscreenCpn = parser.nextText();
                            if (!fullscreenCpn.equals("")) {
                                fullscreenCpnList.add(fullscreenCpn);
                                if (this.mDynamicDebug) {
                                    Slog.i("OppoFreeFormManagerService", " readConfigFromFileLocked fullscreenCpn = " + fullscreenCpn);
                                }
                            }
                        } else if (NEXT_NEED_FULLSCREEN_CPN_LIST.equals(tagName)) {
                            String nextNeedFullscreenCpn = parser.nextText();
                            if (!nextNeedFullscreenCpn.equals("")) {
                                nextNeedFullscreenCpnList.add(nextNeedFullscreenCpn);
                                if (this.mDynamicDebug) {
                                    Slog.i("OppoFreeFormManagerService", " readConfigFromFileLocked nextNeedFullscreenCpn = " + nextNeedFullscreenCpn);
                                }
                            }
                        } else if (ROOT_PKG.equals(tagName)) {
                            String rootPkg = parser.nextText();
                            if (!rootPkg.equals("")) {
                                rootPkgList.add(rootPkg);
                                if (this.mDynamicDebug) {
                                    Slog.i("OppoFreeFormManagerService", " readConfigFromFileLocked rootPkg = " + rootPkg);
                                }
                            }
                        } else if (SPECIAL_CPN_LIST.equals(tagName)) {
                            cpn = parser.nextText();
                            if (!cpn.equals("")) {
                                specialCpnList.add(cpn);
                                if (this.mDynamicDebug) {
                                    Slog.i("OppoFreeFormManagerService", " readConfigFromFileLocked speical cpn = " + cpn);
                                }
                            }
                        } else if (SECURE_CPN_LIST.equals(tagName)) {
                            cpn = parser.nextText();
                            if (!cpn.equals("")) {
                                secureCpnList.add(cpn);
                                if (this.mDynamicDebug) {
                                    Slog.i("OppoFreeFormManagerService", " readConfigFromFileLocked secure cpn = " + cpn);
                                }
                            }
                        }
                    }
                } while (type != 1);
                synchronized (this.mFreeformPkgListLock) {
                    if (!freeformPkglist.isEmpty()) {
                        this.mFreeformPkgList.clear();
                        this.mFreeformPkgList.addAll(freeformPkglist);
                    }
                }
                synchronized (this.mRootPkgListLock) {
                    if (!rootPkgList.isEmpty()) {
                        this.mRootPkgList.clear();
                        this.mRootPkgList.addAll(rootPkgList);
                    }
                }
                synchronized (this.mFullscreenCpnListLock) {
                    if (!fullscreenCpnList.isEmpty()) {
                        this.mFullscreenCpnList.clear();
                        this.mFullscreenCpnList.addAll(fullscreenCpnList);
                    }
                }
                synchronized (this.mNextNeedFullscreenCpnListLock) {
                    if (!nextNeedFullscreenCpnList.isEmpty()) {
                        this.mNextNeddFullscreenCpnList.clear();
                        this.mNextNeddFullscreenCpnList.addAll(nextNeedFullscreenCpnList);
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
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e2) {
                        Slog.e("OppoFreeFormManagerService", "Failed to close state FileInputStream " + e2);
                    }
                }
                InputStream inputStream = fileInputStream2;
            } catch (Exception e3) {
                e = e3;
                fileInputStream = fileInputStream2;
                try {
                    Slog.e("OppoFreeFormManagerService", "failed parsing ", e);
                    setDefaultConfig();
                    if (fileInputStream == null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            Slog.e("OppoFreeFormManagerService", "Failed to close state FileInputStream " + e22);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e222) {
                            Slog.e("OppoFreeFormManagerService", "Failed to close state FileInputStream " + e222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Slog.e("OppoFreeFormManagerService", "failed parsing ", e);
            setDefaultConfig();
            if (fileInputStream == null) {
            }
        }
    }

    public boolean inFreeformPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mFreeformPkgListLock) {
            if (this.mFreeformPkgList.contains(pkg)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i("OppoFreeFormManagerService", "inFreeformPkgList result = " + result);
        }
        return result;
    }

    public boolean inRootPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mRootPkgListLock) {
            if (this.mRootPkgList.contains(pkg)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i("OppoFreeFormManagerService", "inRootPkgList result = " + result + "  pkg = " + pkg);
        }
        return result;
    }

    public boolean inFullscreenCpnList(String cpn) {
        boolean result = false;
        synchronized (this.mFullscreenCpnListLock) {
            if (this.mFullscreenCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i("OppoFreeFormManagerService", "inFullscreenCpnList result = " + result + "  cpn = " + cpn);
        }
        return result;
    }

    public boolean inNextNeedFullscreenCpnList(String cpn) {
        boolean result = false;
        synchronized (this.mNextNeedFullscreenCpnListLock) {
            if (this.mNextNeddFullscreenCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i("OppoFreeFormManagerService", "inNextNeedFullscreenCpnList result = " + result + "  cpn = " + cpn);
        }
        return result;
    }

    public boolean isSpecialCpn(String cpn) {
        boolean result = false;
        synchronized (this.mSpecilaCpnListLock) {
            if (this.mSpecialCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (this.mDynamicDebug) {
            Slog.i("OppoFreeFormManagerService", "isSpecialCpn result = " + result);
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
            Slog.i("OppoFreeFormManagerService", "isSecureCpn result = " + result);
        }
        return result;
    }

    public boolean isFreeformEnable() {
        boolean z;
        synchronized (this.mFreeformSwitchLock) {
            z = this.mFreeformSwitch;
        }
        return z;
    }

    private void setDefaultConfig() {
        this.mFreeformSwitch = false;
        synchronized (this.mFreeformPkgListLock) {
            this.mFreeformPkgList.clear();
        }
        synchronized (this.mRootPkgListLock) {
            this.mRootPkgList.clear();
        }
        synchronized (this.mFullscreenCpnListLock) {
            this.mFullscreenCpnList.clear();
        }
        synchronized (this.mNextNeedFullscreenCpnListLock) {
            this.mNextNeddFullscreenCpnList.clear();
            this.mNextNeddFullscreenCpnList.addAll(DEFAULT_NEXT_NEED_FULLSCREEN_CPN_LIST);
        }
        synchronized (this.mSpecilaCpnListLock) {
            this.mSpecialCpnList.clear();
        }
        synchronized (this.mSecureCpnListLock) {
            this.mSecureCpnList.clear();
        }
    }

    public void setDynamicDebugSwitch() {
        this.mDynamicDebug = OppoFreeFormManagerService.getInstance().mDynamicDebug;
        this.mDebugSwitch = this.mDebugDetail | this.mDynamicDebug;
    }
}
