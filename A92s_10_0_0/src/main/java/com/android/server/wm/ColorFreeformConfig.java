package com.android.server.wm;

import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Slog;
import android.util.Xml;
import com.android.server.FgThread;
import com.color.app.IColorFreeformConfigChangedListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorFreeformConfig {
    private static final String BLACK_PKG = "blackPkg";
    private static final String DEFAULT_FREEFORM_CONFIG_FILE_PATH = "/system/oppo/sys_freeform_config.xml";
    private static final List<String> DEFAULT_NEXT_NEED_FULLSCREEN_CPN_LIST = Arrays.asList("com.tencent.mm.plugin.appbrand.ui.AppBrandUI", "com.whatsapp.voipcalling.VoipActivityV2");
    private static final String FREEFORM_PKG = "freeformPkg";
    private static final String FREEFORM_SWITCH = "freeformSwitch";
    private static final String FULLSCREEN_CPN = "fullscreenCpn";
    private static final String NEXT_NEED_FULLSCREEN_CPN_LIST = "nextneedfullscreenCpn";
    private static final String OPPO_FREEFORM_CONFIG_FILE_PATH = "/data/oppo/coloros/freeform/sys_freeform_config.xml";
    private static final String OPPO_FREEFORM_DIR_PATH = "/data/oppo/coloros/freeform/";
    private static final String ROOT_PKG = "rootPkg";
    private static final String SECURE_CPN_LIST = "securecpn";
    private static final String SPECIAL_CPN_LIST = "specialcpn";
    private static final String TAG = "ColorFreeformConfig";
    private static ColorFreeformConfig sConfig = null;
    private static boolean sDebugDetail = ColorFreeformManagerService.sDebugfDetail;
    /* access modifiers changed from: private */
    public static boolean sDebugSwitch = sDebugDetail;
    private static boolean sDynamicDebug = false;
    private List<String> mBlackPkgList = new ArrayList();
    private final Object mConfigLock = new Object();
    private FileObserverPolicy mFreeformFileObserver = null;
    private List<String> mFreeformPkgList = new ArrayList();
    private boolean mFreeformSwitch = true;
    private List<String> mFullscreenCpnList = new ArrayList();
    private List<String> mNextNeddFullscreenCpnList = new ArrayList(DEFAULT_NEXT_NEED_FULLSCREEN_CPN_LIST);
    private final OnConfigChangeListeners mOnConfigChangeListeners = new OnConfigChangeListeners(FgThread.get().getLooper());
    private List<String> mRootPkgList = new ArrayList();
    private List<String> mSecureCpnList = new ArrayList();
    private List<String> mSpecialCpnList = new ArrayList();

    private ColorFreeformConfig() {
    }

    public static ColorFreeformConfig getInstance() {
        if (sConfig == null) {
            synchronized (ColorFreeformConfig.class) {
                if (sConfig == null) {
                    sConfig = new ColorFreeformConfig();
                }
            }
        }
        return sConfig;
    }

    public List<String> getConfigList(int type) {
        if (sDebugSwitch) {
            Slog.i(TAG, "getConfigList type = " + type);
        }
        synchronized (this.mConfigLock) {
            if (type == 1) {
                try {
                    List<String> list = this.mFreeformPkgList;
                    return list;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (type == 2) {
                List<String> list2 = this.mRootPkgList;
                return list2;
            } else if (type == 4) {
                List<String> list3 = this.mSecureCpnList;
                return list3;
            } else if (type == 8) {
                List<String> list4 = this.mSpecialCpnList;
                return list4;
            } else if (type == 32) {
                List<String> list5 = this.mNextNeddFullscreenCpnList;
                return list5;
            } else if (type == 16) {
                List<String> list6 = this.mFullscreenCpnList;
                return list6;
            } else if (type != 64) {
                return null;
            } else {
                List<String> list7 = this.mBlackPkgList;
                return list7;
            }
        }
    }

    public boolean isEnabled() {
        boolean z;
        if (sDebugSwitch) {
            Slog.i(TAG, "isEnabled mFreeformSwitch = " + this.mFreeformSwitch);
        }
        synchronized (this.mConfigLock) {
            z = this.mFreeformSwitch;
        }
        return z;
    }

    public boolean addConfigChangedListener(IColorFreeformConfigChangedListener listener) {
        if (sDebugSwitch) {
            Slog.i(TAG, "addConfigChangedListener listener = " + listener);
        }
        synchronized (this.mConfigLock) {
            if (this.mOnConfigChangeListeners == null) {
                return false;
            }
            this.mOnConfigChangeListeners.addListenerLocked(listener);
            return true;
        }
    }

    public boolean removeConfigChangedListener(IColorFreeformConfigChangedListener listener) {
        if (sDebugSwitch) {
            Slog.i(TAG, "removeConfigChangedListener listener = " + listener);
        }
        synchronized (this.mConfigLock) {
            if (this.mOnConfigChangeListeners == null) {
                return false;
            }
            this.mOnConfigChangeListeners.removeListenerLocked(listener);
            return true;
        }
    }

    public void init() {
        if (sDebugSwitch) {
            Slog.d(TAG, "init");
        }
        initDir();
        initFileObserver();
        readFreeformConfigFile();
    }

    public boolean inFreeformPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (this.mFreeformPkgList.contains(pkg)) {
                result = true;
            }
        }
        if (sDynamicDebug) {
            Slog.i(TAG, "inFreeformPkgList result = " + result);
        }
        return result;
    }

    public boolean inRootPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (this.mRootPkgList.contains(pkg)) {
                result = true;
            }
        }
        if (sDynamicDebug) {
            Slog.i(TAG, "inRootPkgList result = " + result + "  pkg = " + pkg);
        }
        return result;
    }

    public boolean inBlackPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (this.mBlackPkgList.contains(pkg)) {
                result = true;
            }
        }
        if (sDynamicDebug) {
            Slog.i(TAG, "inBlackPkgList result = " + result + "  pkg = " + pkg);
        }
        return result;
    }

    public boolean inFullscreenCpnList(String cpn) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (this.mFullscreenCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (sDynamicDebug) {
            Slog.i(TAG, "inFullscreenCpnList result = " + result + "  cpn = " + cpn);
        }
        return result;
    }

    public boolean inNextNeedFullscreenCpnList(String cpn) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (this.mNextNeddFullscreenCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (sDynamicDebug) {
            Slog.i(TAG, "inNextNeedFullscreenCpnList result = " + result + "  cpn = " + cpn);
        }
        return result;
    }

    public boolean isSpecialCpn(String cpn) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (this.mSpecialCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (sDynamicDebug) {
            Slog.i(TAG, "isSpecialCpn result = " + result);
        }
        return result;
    }

    public boolean isSecureCpn(String cpn) {
        boolean result = false;
        synchronized (this.mConfigLock) {
            if (this.mSecureCpnList.contains(cpn)) {
                result = true;
            }
        }
        if (sDynamicDebug) {
            Slog.i(TAG, "isSecureCpn result = " + result);
        }
        return result;
    }

    public boolean isFreeformEnable() {
        boolean z;
        synchronized (this.mConfigLock) {
            z = this.mFreeformSwitch;
        }
        return z;
    }

    public void setDynamicDebugSwitch() {
        sDynamicDebug = ColorFreeformManagerService.getInstance().mDynamicDebug;
        sDebugSwitch = sDebugDetail | sDynamicDebug;
    }

    private void setEnable(boolean enable) {
        if (sDebugSwitch) {
            Slog.d(TAG, "setEnable enable = " + enable + "mFreeformSwitch = " + this.mFreeformSwitch);
        }
        synchronized (this.mConfigLock) {
            if (this.mFreeformSwitch != enable) {
                this.mFreeformSwitch = enable;
                if (this.mOnConfigChangeListeners != null) {
                    this.mOnConfigChangeListeners.onConfigSwitchChanged(enable);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007c, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007e, code lost:
        return 0;
     */
    private int setConfigList(int type, List<String> list) {
        if (sDebugSwitch) {
            Slog.d(TAG, "setSupportPkgList type " + type);
        }
        synchronized (this.mConfigLock) {
            if (list != null) {
                if (!list.isEmpty()) {
                    if (type == 1) {
                        this.mFreeformPkgList.clear();
                        this.mFreeformPkgList.addAll(list);
                    } else if (type == 2) {
                        this.mRootPkgList.clear();
                        this.mRootPkgList.addAll(list);
                    } else if (type == 16) {
                        this.mFullscreenCpnList.clear();
                        this.mFullscreenCpnList.addAll(list);
                    } else if (type == 32) {
                        this.mNextNeddFullscreenCpnList.clear();
                        this.mNextNeddFullscreenCpnList.addAll(list);
                    } else if (type == 4) {
                        this.mSecureCpnList.clear();
                        this.mSecureCpnList.addAll(list);
                    } else if (type == 8) {
                        this.mSpecialCpnList.clear();
                        this.mSpecialCpnList.addAll(list);
                    }
                }
            }
        }
    }

    private void setDefaultConfig() {
        synchronized (this.mConfigLock) {
            this.mFreeformSwitch = false;
            this.mFreeformPkgList.clear();
            this.mRootPkgList.clear();
            this.mBlackPkgList.clear();
            this.mFullscreenCpnList.clear();
            this.mNextNeddFullscreenCpnList.clear();
            this.mNextNeddFullscreenCpnList.addAll(DEFAULT_NEXT_NEED_FULLSCREEN_CPN_LIST);
            this.mSpecialCpnList.clear();
            this.mSecureCpnList.clear();
            if (this.mOnConfigChangeListeners != null) {
                this.mOnConfigChangeListeners.onConfigTypeChanged(127);
                this.mOnConfigChangeListeners.onConfigSwitchChanged(this.mFreeformSwitch);
            }
        }
    }

    private void initDir() {
        Slog.i(TAG, "initDir start");
        File freeformDir = new File(OPPO_FREEFORM_DIR_PATH);
        try {
            if (!freeformDir.exists()) {
                freeformDir.mkdir();
            }
            copyFile(DEFAULT_FREEFORM_CONFIG_FILE_PATH, OPPO_FREEFORM_CONFIG_FILE_PATH);
            confirmFileExist(OPPO_FREEFORM_CONFIG_FILE_PATH);
            changeModFile(OPPO_FREEFORM_CONFIG_FILE_PATH);
        } catch (Exception e) {
            Slog.e(TAG, "initDir failed!!!");
        }
    }

    private void initFileObserver() {
        this.mFreeformFileObserver = new FileObserverPolicy(OPPO_FREEFORM_CONFIG_FILE_PATH);
        this.mFreeformFileObserver.startWatching();
    }

    private void changeModFile(String fileName) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Slog.w(TAG, " " + e);
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

    /* access modifiers changed from: private */
    public void readFreeformConfigFile() {
        if (sDebugSwitch) {
            Slog.i(TAG, "readFreeformConfigFile start");
        }
        readFreeformConfigFileLocked(new File(OPPO_FREEFORM_CONFIG_FILE_PATH));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:108:?, code lost:
        r11.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x027b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x027c, code lost:
        r0 = r0;
        r2 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01f9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01fa, code lost:
        r2 = r0;
        r12 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x025c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x025d, code lost:
        r12 = r18;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0277 A[SYNTHETIC, Splitter:B:107:0x0277] */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0289 A[SYNTHETIC, Splitter:B:113:0x0289] */
    /* JADX WARNING: Removed duplicated region for block: B:121:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x025c A[ExcHandler: Exception (e java.lang.Exception), PHI: r18 
      PHI: (r18v0 java.lang.String) = (r18v1 java.lang.String), (r18v1 java.lang.String), (r18v7 java.lang.String) binds: [B:84:0x0205, B:85:?, B:19:0x0099] A[DONT_GENERATE, DONT_INLINE], Splitter:B:19:0x0099] */
    private void readFreeformConfigFileLocked(File file) {
        String str;
        IOException e;
        StringBuilder sb;
        String str2;
        String str3 = "Failed to close state FileInputStream ";
        if (sDebugSwitch) {
            Slog.i(TAG, "readConfigFileLocked start");
        }
        List<String> freeformPkglist = new ArrayList<>();
        List<String> rootPkgList = new ArrayList<>();
        List<String> fullscreenCpnList = new ArrayList<>();
        List<String> nextNeedFullscreenCpnList = new ArrayList<>();
        List<String> specialCpnList = new ArrayList<>();
        List<String> secureCpnList = new ArrayList<>();
        List<String> blackPkgList = new ArrayList<>();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            while (true) {
                int type = parser.next();
                if (type == 2) {
                    String tagName = parser.getName();
                    if (sDynamicDebug) {
                        Slog.i(TAG, " readConfigFileLocked tagName=" + tagName);
                    }
                    if (FREEFORM_SWITCH.equals(tagName)) {
                        String freeformSwitch = parser.nextText();
                        if (!freeformSwitch.equals("")) {
                            setEnable(Boolean.parseBoolean(freeformSwitch));
                            if (sDynamicDebug) {
                                StringBuilder sb2 = new StringBuilder();
                                str2 = str3;
                                try {
                                    sb2.append(" readConfigFromFileLocked freeformSwitch = ");
                                    sb2.append(freeformSwitch);
                                    Slog.i(TAG, sb2.toString());
                                } catch (Exception e2) {
                                } catch (Throwable th) {
                                    th = th;
                                    str = str2;
                                    Throwable th2 = th;
                                    if (stream != null) {
                                    }
                                    throw th2;
                                }
                            } else {
                                str2 = str3;
                            }
                        } else {
                            str2 = str3;
                        }
                    } else {
                        str2 = str3;
                        if (FREEFORM_PKG.equals(tagName)) {
                            String freeformPkg = parser.nextText();
                            if (!freeformPkg.equals("")) {
                                freeformPkglist.add(freeformPkg);
                                if (sDynamicDebug) {
                                    Slog.i(TAG, " readConfigFromFileLocked freeformPkg = " + freeformPkg);
                                }
                            }
                        } else if (FULLSCREEN_CPN.equals(tagName)) {
                            String fullscreenCpn = parser.nextText();
                            if (!fullscreenCpn.equals("")) {
                                fullscreenCpnList.add(fullscreenCpn);
                                if (sDynamicDebug) {
                                    Slog.i(TAG, " readConfigFromFileLocked fullscreenCpn = " + fullscreenCpn);
                                }
                            }
                        } else if (NEXT_NEED_FULLSCREEN_CPN_LIST.equals(tagName)) {
                            String nextNeedFullscreenCpn = parser.nextText();
                            if (!nextNeedFullscreenCpn.equals("")) {
                                nextNeedFullscreenCpnList.add(nextNeedFullscreenCpn);
                                if (sDynamicDebug) {
                                    Slog.i(TAG, " readConfigFromFileLocked nextNeedFullscreenCpn = " + nextNeedFullscreenCpn);
                                }
                            }
                        } else if (ROOT_PKG.equals(tagName)) {
                            String rootPkg = parser.nextText();
                            if (!rootPkg.equals("")) {
                                rootPkgList.add(rootPkg);
                                if (sDynamicDebug) {
                                    Slog.i(TAG, " readConfigFromFileLocked rootPkg = " + rootPkg);
                                }
                            }
                        } else if (BLACK_PKG.equals(tagName)) {
                            String blackPkg = parser.nextText();
                            if (!blackPkg.equals("")) {
                                blackPkgList.add(blackPkg);
                                if (sDynamicDebug) {
                                    Slog.i(TAG, " readConfigFromFileLocked blackPkg = " + blackPkg);
                                }
                            }
                        } else if (SPECIAL_CPN_LIST.equals(tagName)) {
                            String cpn = parser.nextText();
                            if (!cpn.equals("")) {
                                specialCpnList.add(cpn);
                                if (sDynamicDebug) {
                                    Slog.i(TAG, " readConfigFromFileLocked speical cpn = " + cpn);
                                }
                            }
                        } else if (SECURE_CPN_LIST.equals(tagName)) {
                            String cpn2 = parser.nextText();
                            if (!cpn2.equals("")) {
                                secureCpnList.add(cpn2);
                                if (sDynamicDebug) {
                                    Slog.i(TAG, " readConfigFromFileLocked secure cpn = " + cpn2);
                                }
                            }
                        }
                    }
                } else {
                    str2 = str3;
                }
                if (type == 1) {
                    break;
                }
                str3 = str2;
            }
            int typeChanged = setConfigList(1, freeformPkglist) | 0 | setConfigList(2, rootPkgList) | setConfigList(64, blackPkgList) | setConfigList(16, fullscreenCpnList) | setConfigList(32, nextNeedFullscreenCpnList) | setConfigList(8, specialCpnList) | setConfigList(4, secureCpnList);
            if (this.mOnConfigChangeListeners != null) {
                this.mOnConfigChangeListeners.onConfigTypeChanged(typeChanged);
            }
            try {
                stream.close();
                return;
            } catch (IOException e3) {
                e = e3;
                sb = new StringBuilder();
                str = str2;
            }
            sb.append(str);
            sb.append(e);
            Slog.e(TAG, sb.toString());
        } catch (Exception e4) {
            e = e4;
            str = str3;
            try {
                Slog.e(TAG, "failed parsing ", e);
                setDefaultConfig();
                if (stream == null) {
                }
            } catch (Throwable th3) {
                th = th3;
                Throwable th22 = th;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e5) {
                        Slog.e(TAG, str + e5);
                    }
                }
                throw th22;
            }
        } catch (Throwable th4) {
            th = th4;
            str = str3;
            Throwable th222 = th;
            if (stream != null) {
            }
            throw th222;
        }
    }

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (ColorFreeformConfig.sDebugSwitch) {
                Slog.d(ColorFreeformConfig.TAG, "onEvent event = " + event + "path = " + path);
            }
            if (event == 8 && this.mFocusPath.equals(ColorFreeformConfig.OPPO_FREEFORM_CONFIG_FILE_PATH)) {
                Slog.i(ColorFreeformConfig.TAG, "mFocusPath OPPO_FREEFORM_CONFIG_FILE_PATH!");
                ColorFreeformConfig.this.readFreeformConfigFile();
            }
        }
    }

    private static final class OnConfigChangeListeners extends Handler {
        private static final int MSG_CONFIG_SWITCH_CHANGED = 2;
        private static final int MSG_CONFIG_TYPE_CHANGED = 1;
        private final RemoteCallbackList<IColorFreeformConfigChangedListener> mConfigListeners = new RemoteCallbackList<>();

        public OnConfigChangeListeners(Looper looper) {
            super(looper);
        }

        /* JADX INFO: Multiple debug info for r0v1 int: [D('enabled' boolean), D('type' int)] */
        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean enabled = true;
            if (i == 1) {
                handleOnConfigTypeChanged(msg.arg1);
            } else if (i == 2) {
                if (msg.arg1 == 0) {
                    enabled = false;
                }
                handleOnConfigSwitchChanged(enabled);
            }
        }

        public void addListenerLocked(IColorFreeformConfigChangedListener listener) {
            this.mConfigListeners.register(listener);
        }

        public void removeListenerLocked(IColorFreeformConfigChangedListener listener) {
            this.mConfigListeners.unregister(listener);
        }

        public void onConfigTypeChanged(int type) {
            if (ColorFreeformConfig.sDebugSwitch) {
                Slog.d(ColorFreeformConfig.TAG, "onConfigTypeChanged type = " + type + " listenerCount = " + this.mConfigListeners.getRegisteredCallbackCount());
            }
            if (this.mConfigListeners.getRegisteredCallbackCount() > 0) {
                obtainMessage(1, type, 0).sendToTarget();
            }
        }

        public void onConfigSwitchChanged(boolean enable) {
            if (ColorFreeformConfig.sDebugSwitch) {
                Slog.d(ColorFreeformConfig.TAG, "onConfigTypeChanged enable = " + enable + " listenerCount = " + this.mConfigListeners.getRegisteredCallbackCount());
            }
            if (this.mConfigListeners.getRegisteredCallbackCount() > 0) {
                obtainMessage(2, enable ? 1 : 0, 0).sendToTarget();
            }
        }

        private void handleOnConfigTypeChanged(int type) {
            int count = this.mConfigListeners.beginBroadcast();
            int i = 0;
            while (i < count) {
                try {
                    try {
                        this.mConfigListeners.getBroadcastItem(i).onConfigTypeChanged(type);
                    } catch (RemoteException e) {
                        Slog.e(ColorFreeformConfig.TAG, "Permission listener is dead", e);
                    }
                    i++;
                } catch (Throwable th) {
                    this.mConfigListeners.finishBroadcast();
                    throw th;
                }
            }
            this.mConfigListeners.finishBroadcast();
        }

        private void handleOnConfigSwitchChanged(boolean enable) {
            int count = this.mConfigListeners.beginBroadcast();
            int i = 0;
            while (i < count) {
                try {
                    try {
                        this.mConfigListeners.getBroadcastItem(i).onConfigSwitchChanged(enable);
                    } catch (RemoteException e) {
                        Slog.e(ColorFreeformConfig.TAG, "Freeform config listener is dead", e);
                    }
                    i++;
                } catch (Throwable th) {
                    this.mConfigListeners.finishBroadcast();
                    throw th;
                }
            }
            this.mConfigListeners.finishBroadcast();
        }
    }
}
