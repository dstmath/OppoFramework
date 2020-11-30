package com.android.server.wm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.OppoApplicationInfoEx;
import android.os.OppoBaseEnvironment;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.oppo.OppoRomUpdateHelper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoScreenCompat implements IOppoScreenCompat {
    static final String DATA_FILE_DIR = "data/system/sys_resolution_switch_config.xml";
    static boolean DEBUG = false;
    private static final boolean DEBUG_DISABLE_FORCE_FHD = SystemProperties.getBoolean("debug.disable.forcefhd", false);
    static final boolean DEBUG_PANIC = OppoScreenModeService.DEBUG_PANIC;
    static final int DEFAULT_2K_DENSITY = 420;
    static final float DEFAULT_2K_INVSCALE = 0.75f;
    static final float DEFAULT_2K_SCALE = 1.3333334f;
    private static final boolean OPPO_ALL_THIRDPART_COMPAT = SystemProperties.getBoolean("persist.sys.third.compat", false);
    static final String SYS_FILE_DIR = (OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath() + "/etc/sys_resolution_switch_config.xml");
    private static final String TAG = "ScreenCompat";
    private static OppoScreenCompat sOppoScreenCompat;
    HashMap<String, AppScaleInfo> mDisplayCompatMap = new HashMap<>();
    ArrayList<String> mDisplayCompatPkgList = new ArrayList<>();
    HashMap<String, AppScaleInfo> mDisplayForceFhdMap = new HashMap<>();
    boolean mForceFhd = false;
    boolean mForceStopAppOnSwitch = false;
    ArrayList<String> mForceStopList = new ArrayList<>();
    boolean mInitialComplete = false;
    ArrayList<String> mNotForceStopList = new ArrayList<>();
    private OppoResolotionHelper mResolotionHelper = null;
    private boolean mResolutionSupport = false;
    int mScreenWidth = 0;
    private WindowManagerService mWms = null;

    public static OppoScreenCompat getInstance() {
        OppoScreenCompat oppoScreenCompat;
        synchronized (OppoScreenCompat.class) {
            if (sOppoScreenCompat == null) {
                sOppoScreenCompat = new OppoScreenCompat();
            }
            oppoScreenCompat = sOppoScreenCompat;
        }
        return oppoScreenCompat;
    }

    private OppoScreenCompat() {
    }

    public void init(WindowManagerService wms, Context context, boolean resolutionSupport, int screenWidht) {
        this.mWms = wms;
        this.mResolutionSupport = resolutionSupport;
        this.mScreenWidth = screenWidht;
        this.mResolotionHelper = new OppoResolotionHelper(context);
        this.mResolotionHelper.initUpdateBroadcastReceiver();
    }

    public void setForceFhd(boolean on) {
        this.mForceFhd = on;
    }

    public void openLog(boolean on) {
        DEBUG = on;
    }

    /* access modifiers changed from: package-private */
    public boolean notForceStop(String pkg) {
        if (this.mNotForceStopList.contains(pkg)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean needForceStop(String pkg) {
        if (this.mForceStopList.contains(pkg) || this.mDisplayCompatMap.containsKey(pkg)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean forceStopAll() {
        return this.mForceStopAppOnSwitch;
    }

    public void overrideCompatInfoIfNeed(ApplicationInfo ai) {
        OppoApplicationInfoEx oppoAppInfoEx = OppoApplicationInfoEx.getOppoAppInfoExFromAppInfoRef(ai);
        AppScaleInfo appScaleInfo = getCompatAppScaleInfo(ai.packageName, ai.uid);
        if (appScaleInfo != null) {
            int overrideDensityFromConfig = (int) (((float) this.mWms.mRoot.getConfiguration().densityDpi) * appScaleInfo.mInvScale);
            if (oppoAppInfoEx != null) {
                oppoAppInfoEx.setOverrideDensity(overrideDensityFromConfig);
                oppoAppInfoEx.setAppScale(appScaleInfo.mScale);
                oppoAppInfoEx.setAppInvScale(appScaleInfo.mInvScale);
                oppoAppInfoEx.setCompatDensity(appScaleInfo.mDensity);
                ai.privateFlags |= Integer.MIN_VALUE;
                if (DEBUG) {
                    Slog.d(TAG, "overrideCompatInfoIfNeed add compat: " + ai);
                    return;
                }
                return;
            }
            Slog.e(TAG, "fatal exception to overrideCompatInfoIfNeed", new RuntimeException().fillInStackTrace());
        } else if ((Integer.MIN_VALUE & ai.privateFlags) == 0) {
        } else {
            if (oppoAppInfoEx != null) {
                oppoAppInfoEx.setOverrideDensity(0);
                oppoAppInfoEx.setAppScale(1.0f);
                oppoAppInfoEx.setAppInvScale(1.0f);
                ai.privateFlags &= Integer.MAX_VALUE;
                if (DEBUG) {
                    Slog.i(TAG, "overrideCompatInfoIfNeed reset compat: " + ai);
                    return;
                }
                return;
            }
            Slog.e(TAG, "fatal exception to overrideCompatInfoIfNeed", new RuntimeException().fillInStackTrace());
        }
    }

    public boolean isDisplayCompat(String packageName, int uid) {
        getCompatAppScaleInfo(packageName, uid);
        return false;
    }

    private AppScaleInfo getCompatAppScaleInfo(String packageName, int uid) {
        if (packageName == null || !this.mInitialComplete || !this.mResolutionSupport || DEBUG_DISABLE_FORCE_FHD) {
            if (DEBUG) {
                Slog.i(TAG, "isDisplayCompat pkg: " + packageName + ",mInitialComplete =" + this.mInitialComplete);
            }
            return null;
        }
        AppScaleInfo appScaleInfo = this.mForceFhd ? this.mDisplayForceFhdMap.get(packageName) : null;
        if (this.mForceFhd && appScaleInfo != null) {
            if (DEBUG) {
                Slog.i(TAG, "isDisplayCompat pkg: " + packageName + " on fhd list ");
            }
            return appScaleInfo;
        } else if (!OppoScreenModeService.sIsResolutionAuto) {
            if (DEBUG) {
                Slog.i(TAG, "isDisplayCompat pkg: " + packageName + ",sIsResolutionAuto = false");
            }
            return null;
        } else {
            AppScaleInfo appScaleInfo2 = this.mDisplayCompatMap.get(packageName);
            if (appScaleInfo2 != null) {
                if (DEBUG) {
                    Slog.i(TAG, "isDisplayCompat pkg: " + packageName + " on fhd list ");
                }
                return appScaleInfo2;
            }
            if (DEBUG) {
                Slog.i(TAG, "isDisplayCompat pkg: " + packageName + " return false");
            }
            return null;
        }
    }

    public float overrideScaleIfNeed(WindowState win) {
        if ((OppoScreenModeService.sIsResolutionAuto || this.mForceFhd) && !DEBUG_DISABLE_FORCE_FHD) {
            if (win.mAttrs.type == 3) {
                return 1.0f;
            }
            AppScaleInfo appScaleInfo = getCompatAppScaleInfo(win.getOwningPackage(), win.getOwningUid());
            if (appScaleInfo != null) {
                win.mGlobalScale = appScaleInfo.mScale;
                win.mInvGlobalScale = appScaleInfo.mInvScale;
                if (DEBUG) {
                    Slog.i(TAG, "overrideScaleIfNeed scale: " + win.mGlobalScale + ",win=" + win);
                }
            }
        }
        return win.mGlobalScale;
    }

    private boolean checkContains(ArrayList arrayList, String packageName) {
        if (arrayList.contains(packageName)) {
            return true;
        }
        String[] splitString = packageName.split("\\.");
        String partialname = StringUtils.EMPTY;
        if (splitString != null && splitString.length > 2) {
            partialname = splitString[0] + "." + splitString[1] + "." + splitString[2];
        }
        if (partialname == StringUtils.EMPTY || !arrayList.contains(partialname)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public class AppScaleInfo {
        int mDensity = 0;
        float mInvScale = 1.0f;
        float mScale = 1.0f;

        AppScaleInfo(float scale, float invScale, int density) {
            this.mScale = scale;
            this.mInvScale = invScale;
            this.mDensity = density;
        }

        AppScaleInfo(AppScaleInfo info) {
            this.mScale = info.mScale;
            this.mInvScale = info.mInvScale;
            this.mDensity = info.mDensity;
        }

        public String toString() {
            return "mScale=" + this.mScale + ",mInvScale=" + this.mInvScale + ", mDensity=" + this.mDensity;
        }
    }

    private class OppoResolotionHelper extends OppoRomUpdateHelper {
        public static final String FILTER_NAME = "sys_resolution_switch_config";
        static final String TAG = "OppoResolotionHelper";
        private static final String TAG_FHD = "CompatFhd";
        private static final String TAG_FORCE_FHD = "ForceFhd";
        private static final String TAG_FORCE_STOP = "ForceStop";
        private static final String TAG_FORCE_STOP_ALL = "ForceStopAll";
        private static final String TAG_NOT_FORCE_STOP = "noForceStop";
        private static final String TAG_SCALEINFO = "scaleinfo";
        private static final String TAG_SCREENWIDTH = "screenwidth";
        private static final String TAG_VERSION = "version";
        private AppScaleInfo mAppScaleInfo = new AppScaleInfo(OppoScreenCompat.DEFAULT_2K_SCALE, OppoScreenCompat.DEFAULT_2K_INVSCALE, OppoScreenCompat.DEFAULT_2K_DENSITY);
        private int mEnableScreenWidth = OppoScreenCompat.this.mScreenWidth;
        private long mLastVersion = 0;

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void changeFilePermisson(String filename) {
            File file = new File(filename);
            if (file.exists()) {
                boolean result = file.setReadable(true, false);
                Slog.i(TAG, "setReadable result :" + result);
                return;
            }
            Slog.i(TAG, "filename :" + filename + " is not exist");
        }

        private class OppoResolotionInfo extends OppoRomUpdateHelper.UpdateInfo {
            public OppoResolotionInfo() {
                super(OppoResolotionHelper.this);
            }

            /* JADX WARNING: Removed duplicated region for block: B:102:0x0307 A[SYNTHETIC, Splitter:B:102:0x0307] */
            /* JADX WARNING: Removed duplicated region for block: B:107:0x0310 A[Catch:{ IOException -> 0x030b }] */
            /* JADX WARNING: Removed duplicated region for block: B:113:0x0323 A[SYNTHETIC, Splitter:B:113:0x0323] */
            /* JADX WARNING: Removed duplicated region for block: B:118:0x032c A[Catch:{ IOException -> 0x0327 }] */
            /* JADX WARNING: Removed duplicated region for block: B:123:0x033a A[SYNTHETIC, Splitter:B:123:0x033a] */
            /* JADX WARNING: Removed duplicated region for block: B:128:0x0343 A[Catch:{ IOException -> 0x033e }] */
            /* JADX WARNING: Removed duplicated region for block: B:133:? A[RETURN, SYNTHETIC] */
            /* JADX WARNING: Removed duplicated region for block: B:136:? A[RETURN, SYNTHETIC] */
            public void parseContentFromXML(String content) {
                Throwable th;
                XmlPullParserException e;
                IOException e2;
                String name;
                if (content != null) {
                    OppoResolotionHelper.this.changeFilePermisson(OppoScreenCompat.DATA_FILE_DIR);
                    FileReader xmlReader = null;
                    StringReader strReader = null;
                    String name2 = null;
                    OppoResolotionHelper.this.clearCache();
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        strReader = new StringReader(content);
                        parser.setInput(strReader);
                        int eventType = parser.getEventType();
                        while (eventType != 1) {
                            if (eventType == 0) {
                                name = name2;
                            } else if (eventType != 2) {
                                name = name2;
                            } else {
                                String tag = parser.getName();
                                if (OppoScreenCompat.DEBUG) {
                                    try {
                                        Slog.d(OppoResolotionHelper.TAG, "initializing tag:" + tag);
                                    } catch (XmlPullParserException e3) {
                                        e = e3;
                                    } catch (IOException e4) {
                                        e2 = e4;
                                        try {
                                            Slog.i(OppoResolotionHelper.TAG, "Got execption parsing permissions.", e2);
                                            if (0 != 0) {
                                            }
                                            if (strReader != null) {
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            if (0 != 0) {
                                                try {
                                                    xmlReader.close();
                                                } catch (IOException e5) {
                                                    Slog.i(OppoResolotionHelper.TAG, "Got execption close permReader.", e5);
                                                    throw th;
                                                }
                                            }
                                            if (strReader != null) {
                                                strReader.close();
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        if (0 != 0) {
                                        }
                                        if (strReader != null) {
                                        }
                                        throw th;
                                    }
                                }
                                if (OppoResolotionHelper.TAG_FHD.equals(tag)) {
                                    parser.next();
                                    String info = parser.getText();
                                    String[] result = info.split(",");
                                    String packagename = result[0];
                                    if (OppoScreenCompat.DEBUG_PANIC) {
                                        Slog.d(OppoResolotionHelper.TAG, "initializing list info:" + info + " type:" + tag);
                                    }
                                    OppoScreenCompat.this.mDisplayCompatMap.put(packagename, OppoResolotionHelper.this.createAppScaleInfo(result));
                                    name = name2;
                                } else if (OppoResolotionHelper.TAG_FORCE_FHD.equals(tag)) {
                                    parser.next();
                                    String info2 = parser.getText();
                                    String[] result2 = info2.split(",");
                                    String packagename2 = result2[0];
                                    if (OppoScreenCompat.DEBUG_PANIC) {
                                        name = name2;
                                        try {
                                            Slog.d(OppoResolotionHelper.TAG, "initializing list info:" + info2 + " type:" + tag);
                                        } catch (XmlPullParserException e6) {
                                            e = e6;
                                            Slog.i(OppoResolotionHelper.TAG, "Got execption parsing permissions.", e);
                                            if (0 != 0) {
                                            }
                                            if (strReader == null) {
                                            }
                                        } catch (IOException e7) {
                                            e2 = e7;
                                            Slog.i(OppoResolotionHelper.TAG, "Got execption parsing permissions.", e2);
                                            if (0 != 0) {
                                            }
                                            if (strReader != null) {
                                            }
                                        }
                                    } else {
                                        name = name2;
                                    }
                                    OppoScreenCompat.this.mDisplayForceFhdMap.put(packagename2, OppoResolotionHelper.this.createAppScaleInfo(result2));
                                } else {
                                    name = name2;
                                    if (OppoResolotionHelper.TAG_NOT_FORCE_STOP.equals(tag)) {
                                        parser.next();
                                        String info3 = parser.getText();
                                        if (OppoScreenCompat.DEBUG_PANIC) {
                                            Slog.d(OppoResolotionHelper.TAG, "initializing list info:" + info3 + " type:" + tag);
                                        }
                                        OppoScreenCompat.this.mNotForceStopList.add(info3);
                                    } else if (OppoResolotionHelper.TAG_FORCE_STOP.equals(tag)) {
                                        parser.next();
                                        String info4 = parser.getText();
                                        if (OppoScreenCompat.DEBUG_PANIC) {
                                            Slog.d(OppoResolotionHelper.TAG, "initializing list info:" + info4 + " type:" + tag);
                                        }
                                        OppoScreenCompat.this.mForceStopList.add(info4);
                                    } else if (OppoResolotionHelper.TAG_VERSION.equals(tag)) {
                                        parser.next();
                                        String info5 = parser.getText();
                                        Slog.d(OppoResolotionHelper.TAG, "verion :" + info5 + " type:" + tag);
                                        OppoResolotionHelper.this.mLastVersion = Long.parseLong(info5);
                                    } else if (OppoResolotionHelper.TAG_SCALEINFO.equals(tag)) {
                                        parser.next();
                                        String info6 = parser.getText();
                                        OppoResolotionHelper.this.mAppScaleInfo = OppoResolotionHelper.this.createAppScaleInfo(info6.split(","));
                                        if (OppoScreenCompat.DEBUG_PANIC) {
                                            Slog.d(OppoResolotionHelper.TAG, "scale info :" + info6 + " type:" + tag);
                                        }
                                    } else if (OppoResolotionHelper.TAG_SCREENWIDTH.equals(tag)) {
                                        parser.next();
                                        OppoResolotionHelper.this.mEnableScreenWidth = Integer.parseInt(parser.getText());
                                        if (OppoScreenCompat.DEBUG_PANIC) {
                                            Slog.d(OppoResolotionHelper.TAG, "screenwidth :" + parser.getText() + " type:" + tag);
                                        }
                                    } else if (OppoResolotionHelper.TAG_FORCE_STOP_ALL.equals(tag)) {
                                        parser.next();
                                        OppoScreenCompat.this.mForceStopAppOnSwitch = 1 == Integer.parseInt(parser.getText());
                                        if (OppoScreenCompat.DEBUG_PANIC) {
                                            Slog.d(OppoResolotionHelper.TAG, StringUtils.SPACE + OppoScreenCompat.this.mForceStopAppOnSwitch + " type:" + tag);
                                        }
                                    }
                                }
                            }
                            eventType = parser.next();
                            name2 = name;
                        }
                        if (0 != 0) {
                            try {
                                xmlReader.close();
                            } catch (IOException e8) {
                                Slog.i(OppoResolotionHelper.TAG, "Got execption close permReader.", e8);
                            }
                        }
                        strReader.close();
                        if (OppoScreenCompat.DEBUG_PANIC) {
                            Slog.d(OppoResolotionHelper.TAG, "load data end ");
                        }
                        if (OppoResolotionHelper.this.mEnableScreenWidth != OppoScreenCompat.this.mScreenWidth) {
                            OppoScreenCompat.this.mDisplayCompatMap.clear();
                            OppoScreenCompat.this.mDisplayForceFhdMap.clear();
                            Slog.e(OppoResolotionHelper.TAG, "load wrong Config file width=" + OppoScreenCompat.this.mScreenWidth + ",target " + OppoResolotionHelper.this.mEnableScreenWidth);
                        }
                        OppoScreenCompat.this.mInitialComplete = true;
                    } catch (XmlPullParserException e9) {
                        e = e9;
                        Slog.i(OppoResolotionHelper.TAG, "Got execption parsing permissions.", e);
                        if (0 != 0) {
                            try {
                                xmlReader.close();
                            } catch (IOException e10) {
                                Slog.i(OppoResolotionHelper.TAG, "Got execption close permReader.", e10);
                                return;
                            }
                        }
                        if (strReader == null) {
                            strReader.close();
                        }
                    } catch (IOException e11) {
                        e2 = e11;
                        Slog.i(OppoResolotionHelper.TAG, "Got execption parsing permissions.", e2);
                        if (0 != 0) {
                            try {
                                xmlReader.close();
                            } catch (IOException e12) {
                                Slog.i(OppoResolotionHelper.TAG, "Got execption close permReader.", e12);
                                return;
                            }
                        }
                        if (strReader != null) {
                            strReader.close();
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        if (0 != 0) {
                        }
                        if (strReader != null) {
                        }
                        throw th;
                    }
                }
            }

            public boolean updateToLowerVersion(String newContent) {
                long dataversion = OppoResolotionHelper.this.getConfigVersion(newContent, false);
                Slog.d(OppoResolotionHelper.TAG, "dataversion =" + dataversion + ",version =" + OppoResolotionHelper.this.mLastVersion);
                if (dataversion > OppoResolotionHelper.this.mLastVersion) {
                    return false;
                }
                Slog.d(OppoResolotionHelper.TAG, " data version is low! ");
                return true;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private AppScaleInfo createAppScaleInfo(String[] args) {
            int length = args.length;
            if (args.length < 3) {
                return new AppScaleInfo(this.mAppScaleInfo);
            }
            int density = Integer.parseInt(args[length - 1]);
            float invScale = Float.parseFloat(args[length - 2]);
            return new AppScaleInfo(Float.parseFloat(args[length - 3]), invScale, density);
        }

        public OppoResolotionHelper(Context context) {
            super(context, FILTER_NAME, OppoScreenCompat.SYS_FILE_DIR, OppoScreenCompat.DATA_FILE_DIR);
            setUpdateInfo(new OppoResolotionInfo(), new OppoResolotionInfo());
            try {
                init();
                changeFilePermisson(OppoScreenCompat.DATA_FILE_DIR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void init() {
            File datafile = new File(OppoScreenCompat.DATA_FILE_DIR);
            File sysfile = new File(OppoScreenCompat.SYS_FILE_DIR);
            if (!datafile.exists()) {
                Slog.d(TAG, "datafile not exist try to load from system");
                parseContentFromXML(readFromFile(sysfile));
                return;
            }
            long dataversion = getConfigVersion(OppoScreenCompat.DATA_FILE_DIR, true);
            long sysversion = getConfigVersion(OppoScreenCompat.SYS_FILE_DIR, true);
            Slog.d(TAG, "dataversion:" + dataversion + " sysversion:" + sysversion);
            if (dataversion >= sysversion) {
                parseContentFromXML(readFromFile(datafile));
            } else {
                parseContentFromXML(readFromFile(sysfile));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long getConfigVersion(String content, boolean isPath) {
            Reader xmlReader;
            if (content == null) {
                return 0;
            }
            Slog.d(TAG, "getConfigVersion content length:" + content.length() + "," + isPath);
            long version = 0;
            if (isPath) {
                try {
                    xmlReader = new FileReader(content);
                } catch (Exception e) {
                    return 0;
                }
            } else {
                xmlReader = new StringReader(content);
            }
            XmlPullParser parser = Xml.newPullParser();
            try {
                parser.setInput(xmlReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0) {
                        if (eventType == 2) {
                            String tagName = parser.getName();
                            Slog.d(TAG, "getConfigVersion called  tagname:" + tagName);
                            if (TAG_VERSION.equals(tagName)) {
                                parser.next();
                                String text = parser.getText();
                                if (text.length() > 8) {
                                    text = text.substring(0, 8);
                                }
                                try {
                                    version = Long.parseLong(text);
                                } catch (NumberFormatException e2) {
                                    Slog.e(TAG, "version convert fail");
                                }
                                try {
                                    xmlReader.close();
                                } catch (IOException e3) {
                                    Slog.e(TAG, StringUtils.EMPTY + e3);
                                }
                                return version;
                            }
                        }
                    }
                }
                try {
                    xmlReader.close();
                } catch (IOException e4) {
                    Slog.e(TAG, StringUtils.EMPTY + e4);
                }
                return 0;
            } catch (XmlPullParserException e5) {
                Slog.e(TAG, StringUtils.EMPTY + e5);
                try {
                    xmlReader.close();
                } catch (IOException e6) {
                    Slog.e(TAG, StringUtils.EMPTY + e6);
                }
                return 0;
            } catch (Exception e7) {
                Slog.e(TAG, StringUtils.EMPTY + e7);
                try {
                    xmlReader.close();
                } catch (IOException e8) {
                    Slog.e(TAG, StringUtils.EMPTY + e8);
                }
                return 0;
            } catch (Throwable th) {
                try {
                    xmlReader.close();
                } catch (IOException e9) {
                    Slog.e(TAG, StringUtils.EMPTY + e9);
                }
                throw th;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void clearCache() {
            OppoScreenCompat oppoScreenCompat = OppoScreenCompat.this;
            oppoScreenCompat.mInitialComplete = false;
            oppoScreenCompat.mDisplayCompatMap.clear();
            OppoScreenCompat.this.mDisplayForceFhdMap.clear();
            OppoScreenCompat.this.mNotForceStopList.clear();
            OppoScreenCompat.this.mDisplayCompatPkgList.clear();
            OppoScreenCompat.this.mForceStopList.clear();
        }
    }
}
