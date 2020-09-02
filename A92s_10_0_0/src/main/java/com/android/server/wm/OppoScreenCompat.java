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
                if (this.mScreenWidth == 1440 && win.getParentWindow() != null && "com.tencent.mm".equals(win.getOwningPackage()) && win.getParentWindow().toString().contains("plugin.groupsolitaire.ui.GroupSolitatireEditUI")) {
                    win.mGlobalScale = DEFAULT_2K_SCALE;
                    win.mInvGlobalScale = 0.749999f;
                }
                return win.mGlobalScale;
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
        private static final String TAG_NOT_FORCE_STOP = "noForceStop";
        private static final String TAG_SCALEINFO = "scaleinfo";
        private static final String TAG_SCREENWIDTH = "screenwidth";
        private static final String TAG_VERSION = "version";
        /* access modifiers changed from: private */
        public AppScaleInfo mAppScaleInfo = new AppScaleInfo(OppoScreenCompat.DEFAULT_2K_SCALE, OppoScreenCompat.DEFAULT_2K_INVSCALE, OppoScreenCompat.DEFAULT_2K_DENSITY);
        /* access modifiers changed from: private */
        public int mEnableScreenWidth = OppoScreenCompat.this.mScreenWidth;

        /* access modifiers changed from: private */
        public void changeFilePermisson(String filename) {
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

            public void parseContentFromXML(String content) {
                char c;
                if (content != null) {
                    char c2 = 0;
                    OppoScreenCompat.this.mInitialComplete = false;
                    OppoResolotionHelper.this.changeFilePermisson(OppoScreenCompat.DATA_FILE_DIR);
                    FileReader xmlReader = null;
                    StringReader strReader = null;
                    OppoScreenCompat.this.mDisplayCompatMap.clear();
                    OppoScreenCompat.this.mDisplayForceFhdMap.clear();
                    OppoScreenCompat.this.mNotForceStopList.clear();
                    OppoScreenCompat.this.mDisplayCompatPkgList.clear();
                    OppoScreenCompat.this.mForceStopList.clear();
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        StringReader strReader2 = new StringReader(content);
                        parser.setInput(strReader2);
                        int eventType = parser.getEventType();
                        while (eventType != 1) {
                            if (eventType == 0) {
                                c = c2;
                            } else if (eventType != 2) {
                                c = c2;
                            } else {
                                String tag = parser.getName();
                                if (OppoScreenCompat.DEBUG) {
                                    Slog.d(OppoResolotionHelper.TAG, "initializing tag:" + tag);
                                }
                                if (OppoResolotionHelper.TAG_FHD.equals(tag)) {
                                    parser.next();
                                    String info = parser.getText();
                                    String[] result = info.split(",");
                                    String packagename = result[c2];
                                    if (OppoScreenCompat.DEBUG_PANIC) {
                                        Slog.d(OppoResolotionHelper.TAG, "initializing list info:" + info + " type:" + tag);
                                    }
                                    OppoScreenCompat.this.mDisplayCompatMap.put(packagename, OppoResolotionHelper.this.createAppScaleInfo(result));
                                    c = 0;
                                } else if (OppoResolotionHelper.TAG_FORCE_FHD.equals(tag)) {
                                    parser.next();
                                    String info2 = parser.getText();
                                    String[] result2 = info2.split(",");
                                    c = 0;
                                    String packagename2 = result2[0];
                                    if (OppoScreenCompat.DEBUG_PANIC) {
                                        Slog.d(OppoResolotionHelper.TAG, "initializing list info:" + info2 + " type:" + tag);
                                    }
                                    OppoScreenCompat.this.mDisplayForceFhdMap.put(packagename2, OppoResolotionHelper.this.createAppScaleInfo(result2));
                                } else {
                                    c = 0;
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
                                        if (OppoScreenCompat.DEBUG_PANIC) {
                                            Slog.d(OppoResolotionHelper.TAG, "verion :" + parser.getText() + " type:" + tag);
                                        }
                                    } else if (OppoResolotionHelper.TAG_SCALEINFO.equals(tag)) {
                                        parser.next();
                                        String info5 = parser.getText();
                                        AppScaleInfo unused = OppoResolotionHelper.this.mAppScaleInfo = OppoResolotionHelper.this.createAppScaleInfo(info5.split(","));
                                        if (OppoScreenCompat.DEBUG_PANIC) {
                                            Slog.d(OppoResolotionHelper.TAG, "scale info :" + info5 + " type:" + tag);
                                        }
                                    } else if (OppoResolotionHelper.TAG_SCREENWIDTH.equals(tag)) {
                                        parser.next();
                                        int unused2 = OppoResolotionHelper.this.mEnableScreenWidth = Integer.parseInt(parser.getText());
                                        if (OppoScreenCompat.DEBUG_PANIC) {
                                            Slog.d(OppoResolotionHelper.TAG, "screenwidth :" + parser.getText() + " type:" + tag);
                                        }
                                    }
                                }
                            }
                            eventType = parser.next();
                            if (OppoScreenCompat.DEBUG) {
                                Slog.d(OppoResolotionHelper.TAG, "initializing eventType:" + eventType);
                            }
                            c2 = c;
                        }
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e) {
                                Slog.i(OppoResolotionHelper.TAG, "Got execption close permReader.", e);
                            }
                        }
                        strReader2.close();
                        if (OppoScreenCompat.DEBUG_PANIC) {
                            Slog.d(OppoResolotionHelper.TAG, "load data end ");
                        }
                        if (OppoResolotionHelper.this.mEnableScreenWidth != OppoScreenCompat.this.mScreenWidth) {
                            OppoScreenCompat.this.mDisplayCompatMap.clear();
                            OppoScreenCompat.this.mDisplayForceFhdMap.clear();
                            Slog.e(OppoResolotionHelper.TAG, "load wrong Config file width=" + OppoScreenCompat.this.mScreenWidth + ",target " + OppoResolotionHelper.this.mEnableScreenWidth);
                        }
                        OppoScreenCompat.this.mInitialComplete = true;
                    } catch (XmlPullParserException e2) {
                        Slog.i(OppoResolotionHelper.TAG, "Got execption parsing permissions.", e2);
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e3) {
                                Slog.i(OppoResolotionHelper.TAG, "Got execption close permReader.", e3);
                                return;
                            }
                        }
                        if (strReader != null) {
                            strReader.close();
                        }
                    } catch (IOException e4) {
                        Slog.i(OppoResolotionHelper.TAG, "Got execption parsing permissions.", e4);
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e5) {
                                Slog.i(OppoResolotionHelper.TAG, "Got execption close permReader.", e5);
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
                                Slog.i(OppoResolotionHelper.TAG, "Got execption close permReader.", e6);
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
        }

        /* access modifiers changed from: private */
        public AppScaleInfo createAppScaleInfo(String[] args) {
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
    }
}
