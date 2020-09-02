package com.android.server.wm;

import android.app.OppoActivityManagerInternal;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import android.util.Xml;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

class ColorInterceptWindow {
    private static final String ENGINE_EXP_VERSION_FEATURE = "persist.sys.oppo.region";
    private static final String INTERCEPT_FEATURE = "open";
    private static final String OPPO_INTERCEPT_CONFIG = "/data/oppo/coloros/config/sys_wms_intercept_window.xml";
    private static final String OPPO_INTERCEPT_PATH = "/data/oppo/coloros/config";
    private static final String REGION = "CN";
    private static final String SKIP_NAME = "skip";
    private static final String TAG = "ColorInterceptWindow";
    private static final Object mLock = new Object();
    private static final Object mSkipLock = new Object();
    private static ColorInterceptWindow sInstance = null;
    OppoActivityManagerInternal mAmInternal;
    private String mAudioPids;
    private FileObserverPolicy mConfigFileObserver;
    private boolean mExpVersion;
    private boolean mInterceptFeature;
    private boolean mOpenChage;
    private List<String> mSkipApp;
    private List<String> mSkipMusicApp;
    protected List<String> mSkipNameList;

    public static ColorInterceptWindow getInstance() {
        ColorInterceptWindow colorInterceptWindow;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new ColorInterceptWindow();
            }
            colorInterceptWindow = sInstance;
        }
        return colorInterceptWindow;
    }

    private ColorInterceptWindow() {
        this.mConfigFileObserver = null;
        this.mInterceptFeature = true;
        this.mExpVersion = false;
        this.mOpenChage = false;
        this.mAudioPids = null;
        this.mSkipApp = Arrays.asList("com.yuesuoping", "com.tencent.mobileqq", ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.tencent.mobileqq/com.tencent.av.ui.VideoInviteLock", "com.tencent.mobileqq/com.tencent.av.ui.AVActivity", "com.coloros.calculator", "com.android.calculator2", "com.toprand.r11s", "com.toprand.voyager", "com.ademo.one.oppo", "com.ademo.two.oppo", "com.rdemo.one.oppo", "com.rdemo.two.oppo", "com.hunk.screen.ui", "com.tencent.mobileqqi");
        this.mSkipMusicApp = Arrays.asList("com.kugou.android", "cn.kuwo.player", "fm.xiami.main", "com.netease.cloudmusic", "cmccwm.mobilemusic");
        this.mExpVersion = !SystemProperties.get(ENGINE_EXP_VERSION_FEATURE, REGION).equalsIgnoreCase(REGION);
        if (this.mExpVersion) {
            this.mInterceptFeature = false;
        } else {
            this.mInterceptFeature = true;
        }
        initDir();
        readConfigFile();
        initFileObserver();
    }

    public boolean interceptWindow(Context context, WindowState win) {
        if (!this.mInterceptFeature || context == null || win == null || win.mAttrs == null) {
            return false;
        }
        if (win.mAttrs.packageName == null || (!win.mAttrs.packageName.contains("android.server.cts") && !win.mAttrs.packageName.contains("android.server.am") && !win.mAttrs.packageName.contains("android.server.wm.app") && !win.mAttrs.packageName.contains("com.android.compatibility.common.deviceinfo"))) {
            int i = win.mAttrs.type;
            boolean startingWindow = win.mAttrs.type == 3;
            if (win.mSession != null && win.mSession.mUid < 10000 && !startingWindow) {
                return false;
            }
            List<String> list = this.mSkipNameList;
            if (list != null && list.contains(win.mAttrs.packageName)) {
                return false;
            }
            boolean showWhenLocked = (win.mAttrs.flags & 524288) != 0;
            boolean dismissKeyguard = (win.mAttrs.flags & 4194304) != 0;
            if (!showWhenLocked && !dismissKeyguard) {
                return false;
            }
            if (this.mAmInternal == null) {
                this.mAmInternal = (OppoActivityManagerInternal) LocalServices.getService(OppoActivityManagerInternal.class);
            }
            if (!showWhenLocked && !dismissKeyguard) {
                return false;
            }
            if (win.mSession != null && isSystemApp(context, win.getOwningPackage(), UserHandle.getUserId(win.mSession.mUid))) {
                if (OppoWindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                    Slog.v(TAG, "isSystemApp win: " + win);
                }
                return false;
            } else if (!startingWindow && isAudioProcess(context, win)) {
                if (OppoWindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                    Slog.v(TAG, "isAudioProcess win: " + win);
                }
                return false;
            } else if (startingWindow || win.mSession == null || !OppoSysStateManager.getInstance().isSensorWorking(win.mSession.mUid)) {
                if (showWhenLocked) {
                    if (OppoWindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                        Slog.v(TAG, "interceptWindow showWhenLocked ... " + win);
                    }
                    win.mAttrs.flags &= -524289;
                }
                if (dismissKeyguard) {
                    if (OppoWindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                        Slog.v(TAG, "interceptWindow dismissKeyguard ... " + win);
                    }
                    win.mAttrs.flags &= -4194305;
                }
                return true;
            } else {
                if (OppoWindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                    Slog.v(TAG, "isSensorWorking win: " + win);
                }
                return false;
            }
        } else {
            if (OppoWindowManagerDebugConfig.DEBUG_OPPO_INTERCEPT) {
                Slog.v(TAG, "is App running return! app win: " + win);
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void getAudioPids(Context context, Session session, WindowManager.LayoutParams attrs) {
        if (this.mInterceptFeature && context != null && session != null && attrs != null && session.mUid >= 10000) {
            boolean dismissKeyguard = true;
            boolean showWhenLocked = (attrs.flags & 524288) != 0;
            if ((attrs.flags & 4194304) == 0) {
                dismissKeyguard = false;
            }
            if (showWhenLocked || dismissKeyguard) {
                synchronized (mSkipLock) {
                    this.mAudioPids = ((AudioManager) context.getSystemService("audio")).getParameters("get_pid");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0079, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x007b, code lost:
        return false;
     */
    private boolean isAudioProcess(Context context, WindowState win) {
        if (win == null || win.mSession == null) {
            return false;
        }
        synchronized (mSkipLock) {
            if (this.mAudioPids != null) {
                if (this.mAudioPids.length() != 0) {
                    String[] strPoint = this.mAudioPids.split(":");
                    if (strPoint != null) {
                        for (String strIndex : strPoint) {
                            if (strIndex != null) {
                                if (!strIndex.isEmpty()) {
                                    if (strIndex.equals(Integer.toString(win.mSession.mPid))) {
                                        return true;
                                    }
                                    try {
                                        int strPid = Integer.parseInt(strIndex);
                                        if (this.mAmInternal != null && this.mAmInternal.getProcPid(strPid) == win.mSession.mUid) {
                                            return true;
                                        }
                                    } catch (Exception e) {
                                        Slog.e(TAG, "isAudioProcess " + strIndex, e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isSystemApp(Context context, String pkg, int userId) {
        if (pkg != null) {
            try {
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfoAsUser(pkg, 0, userId);
                if (pkgInfo != null) {
                    if (pkgInfo.applicationInfo != null) {
                        if ((pkgInfo.applicationInfo.flags & 1) != 0) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void initDir() {
        File interceptPath = new File(OPPO_INTERCEPT_PATH);
        File interceptConfig = new File(OPPO_INTERCEPT_CONFIG);
        try {
            if (!interceptPath.exists()) {
                interceptPath.mkdirs();
            }
            if (!interceptConfig.exists()) {
                interceptConfig.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init interceptConfig Dir failed!!!");
        }
    }

    private void initFileObserver() {
        this.mConfigFileObserver = new FileObserverPolicy(OPPO_INTERCEPT_CONFIG);
        this.mConfigFileObserver.startWatching();
    }

    /* access modifiers changed from: private */
    public void readConfigFile() {
        File xmlFile = new File(OPPO_INTERCEPT_CONFIG);
        if (!xmlFile.exists()) {
            this.mSkipNameList = new ArrayList(this.mSkipApp);
            return;
        }
        FileReader xmlReader = null;
        StringReader strReader = null;
        this.mOpenChage = false;
        try {
            this.mSkipNameList = new ArrayList();
            XmlPullParser parser = Xml.newPullParser();
            try {
                FileReader xmlReader2 = new FileReader(xmlFile);
                parser.setInput(xmlReader2);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0 && eventType == 2) {
                        if (parser.getName().equals(SKIP_NAME)) {
                            parser.next();
                            updateListName(parser.getAttributeValue(null, "att"), this.mSkipNameList);
                        } else if (parser.getName().equals(INTERCEPT_FEATURE)) {
                            parser.next();
                            updateFeature(parser.getAttributeValue(null, "att"));
                        }
                    }
                }
                try {
                    if (!this.mOpenChage) {
                        if (this.mExpVersion) {
                            this.mInterceptFeature = false;
                        } else {
                            this.mInterceptFeature = true;
                        }
                    }
                    if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                        this.mSkipNameList = new ArrayList(this.mSkipApp);
                    }
                    xmlReader2.close();
                    if (strReader != null) {
                        strReader.close();
                    }
                    addFormSkipApp();
                } catch (IOException e) {
                    Slog.w(TAG, "Got execption close permReader.", e);
                }
            } catch (FileNotFoundException e2) {
                Slog.w(TAG, "Couldn't find or open sys_wms_intercept_window file " + xmlFile);
                try {
                    if (!this.mOpenChage) {
                        if (this.mExpVersion) {
                            this.mInterceptFeature = false;
                        } else {
                            this.mInterceptFeature = true;
                        }
                    }
                    if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                        this.mSkipNameList = new ArrayList(this.mSkipApp);
                    }
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                    addFormSkipApp();
                } catch (IOException e3) {
                    Slog.w(TAG, "Got execption close permReader.", e3);
                }
            }
        } catch (Exception e4) {
            Slog.w(TAG, "Got execption parsing permissions.", e4);
            if (!this.mOpenChage) {
                if (this.mExpVersion) {
                    this.mInterceptFeature = false;
                } else {
                    this.mInterceptFeature = true;
                }
            }
            if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                this.mSkipNameList = new ArrayList(this.mSkipApp);
            }
            if (xmlReader != null) {
                xmlReader.close();
            }
            if (strReader != null) {
                strReader.close();
            }
            addFormSkipApp();
        } catch (Throwable th) {
            try {
                if (!this.mOpenChage) {
                    if (this.mExpVersion) {
                        this.mInterceptFeature = false;
                    } else {
                        this.mInterceptFeature = true;
                    }
                }
                if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                    this.mSkipNameList = new ArrayList(this.mSkipApp);
                }
                if (xmlReader != null) {
                    xmlReader.close();
                }
                if (strReader != null) {
                    strReader.close();
                }
                addFormSkipApp();
            } catch (IOException e5) {
                Slog.w(TAG, "Got execption close permReader.", e5);
            }
            throw th;
        }
    }

    private void updateListName(String tagName, List<String> list) {
        if (tagName != null && tagName != "" && list != null) {
            list.add(tagName);
        }
    }

    private void updateFeature(String feature) {
        if (feature != null) {
            try {
                this.mInterceptFeature = Boolean.parseBoolean(feature);
                this.mOpenChage = true;
            } catch (NumberFormatException e) {
                if (this.mExpVersion) {
                    this.mInterceptFeature = false;
                } else {
                    this.mInterceptFeature = true;
                }
                Slog.e(TAG, "updateFeature NumberFormatException: ", e);
            }
        }
    }

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(ColorInterceptWindow.OPPO_INTERCEPT_CONFIG)) {
                Slog.i(ColorInterceptWindow.TAG, "onEvent: focusPath = OPPO_INTERCEPT_CONFIG");
                ColorInterceptWindow.this.readConfigFile();
            }
        }
    }

    private void addFormSkipApp() {
        if (this.mSkipNameList != null) {
            for (int index = 0; index < this.mSkipMusicApp.size(); index++) {
                if (!this.mSkipNameList.contains(this.mSkipMusicApp.get(index))) {
                    this.mSkipNameList.add(this.mSkipMusicApp.get(index));
                }
            }
            for (int index2 = 0; index2 < this.mSkipApp.size(); index2++) {
                if (!this.mSkipNameList.contains(this.mSkipApp.get(index2))) {
                    this.mSkipNameList.add(this.mSkipApp.get(index2));
                }
            }
        }
    }
}
