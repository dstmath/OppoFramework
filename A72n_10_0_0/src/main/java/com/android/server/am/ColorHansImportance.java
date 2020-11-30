package com.android.server.am;

import android.os.UserHandle;
import com.android.server.am.ColorHansPackageSelector;
import java.util.ArrayList;

public class ColorHansImportance {
    private static final int HANS_IMPORTANT_CASE_AUDIO_FOCUS = 4;
    private static final int HANS_IMPORTANT_CASE_BT = 16;
    private static final int HANS_IMPORTANT_CASE_DEFAULT = 0;
    private static final int HANS_IMPORTANT_CASE_DEFAULT_DIALER = 128;
    private static final int HANS_IMPORTANT_CASE_DEFAULT_INPUT = 256;
    private static final int HANS_IMPORTANT_CASE_DEFAULT_LAUNCHER = 32;
    private static final int HANS_IMPORTANT_CASE_DEFAULT_SMS = 64;
    private static final int HANS_IMPORTANT_CASE_FLOAT_WINDOW = 2;
    private static final int HANS_IMPORTANT_CASE_GPS = 8;
    private static final int HANS_IMPORTANT_CASE_LIVE_WALLPAPER = 2048;
    private static final int HANS_IMPORTANT_CASE_MAX = 262144;
    private static final int HANS_IMPORTANT_CASE_PRELOAD_APP = 131072;
    private static final int HANS_IMPORTANT_CASE_PREV_APP = 8192;
    private static final int HANS_IMPORTANT_CASE_SCREEN_RECORDER = 1024;
    private static final int HANS_IMPORTANT_CASE_SPECIAL_APP = 32768;
    private static final int HANS_IMPORTANT_CASE_SPECIAL_WINDOW = 65536;
    private static final int HANS_IMPORTANT_CASE_TOP_APP = 16384;
    private static final int HANS_IMPORTANT_CASE_TRAFFIC = 1;
    private static final int HANS_IMPORTANT_CASE_VPN = 512;
    private static final int HANS_IMPORTANT_CASE_WIDGET = 4096;
    public static final int HANS_IMPORTANT_FOR_ABNORMAL = 221183;
    public static final int HANS_IMPORTANT_FOR_FAST_FREEZE = 229374;
    public static final int HANS_IMPORTANT_FOR_FROZEN_STATE = 213023;
    public static final int HANS_IMPORTANT_FOR_SPECIAL_APPS = 6400;
    private static final String HANS_IMPORTANT_REASON_AUDIO_FOCUS = "audiofocus";
    private static final String HANS_IMPORTANT_REASON_BT = "bt";
    private static final String HANS_IMPORTANT_REASON_CASE_TRAFFIC = "traffic";
    private static final String HANS_IMPORTANT_REASON_FLOAT_WINDOW = "floatwindow";
    private static final String HANS_IMPORTANT_REASON_GPS = "gps";
    private static final String HANS_IMPORTANT_REASON_PRELOAD_APP = "preload-app";
    private static final String HANS_IMPORTANT_REASON_PREV_APP = "prev";
    private static final String HANS_IMPORTANT_REASON_SPECIAL_APP = "special-app";
    private static final String HANS_IMPORTANT_REASON_SPECIAL_WINDOW = "special-window";
    private static final String HANS_IMPORTANT_REASON_TOP_APP = "top";
    public static final int HANS_IMPORTANT_SCENE_FOR_LCD_OFF_FREEZE = 133119;
    public static final int HANS_IMPORTANT_SCENE_FOR_LCD_ON_FREEZE = 221183;
    public static final int HANS_IMPORTANT_SCENE_FOR_NIGHT_FREEZE = 131296;
    public static final int HANS_IMPORTANT_SCENE_FOR_PRELOAD_FREEZER = 0;
    public static final int HANS_IMPORTANT_SCENE_FOR_STATISTICS = 480;
    private static volatile ColorHansImportance sInstance = null;
    private ColorCommonListManager mCommonListManager;
    private ArrayList<String> mSpecialAppList;

    private ColorHansImportance() {
        this.mCommonListManager = null;
        this.mSpecialAppList = new ArrayList<>();
        this.mCommonListManager = ColorCommonListManager.getInstance();
    }

    protected static synchronized ColorHansImportance getInstance() {
        ColorHansImportance colorHansImportance;
        synchronized (ColorHansImportance.class) {
            if (sInstance == null) {
                sInstance = new ColorHansImportance();
            }
            colorHansImportance = sInstance;
        }
        return colorHansImportance;
    }

    /* access modifiers changed from: protected */
    public boolean isHansImportantCase(ColorHansPackageSelector.HansPackage pkgState, int importantValue, DynamicImportantAppList dImpAppList) {
        int uid = pkgState.getUid();
        String pkg = pkgState.getPkgName();
        boolean isImportant = false;
        for (int step = 1; step <= HANS_IMPORTANT_CASE_MAX; step <<= 1) {
            int i = importantValue & step;
            if (i != 1) {
                if (i != 2) {
                    switch (i) {
                        case 4:
                            if (dImpAppList.getAudioList() != null && dImpAppList.getAudioList().contains(Integer.valueOf(uid))) {
                                pkgState.setImportantReason(HANS_IMPORTANT_REASON_AUDIO_FOCUS);
                                isImportant = true;
                                break;
                            }
                        case 8:
                            if (dImpAppList.getNavigationList() != null && dImpAppList.getNavigationList().contains(pkg)) {
                                pkgState.setImportantReason(HANS_IMPORTANT_REASON_GPS);
                                isImportant = true;
                                break;
                            }
                        case 16:
                            if (this.mCommonListManager.getBluetoothList().contains(Integer.valueOf(uid))) {
                                pkgState.setImportantReason(HANS_IMPORTANT_REASON_BT);
                                isImportant = true;
                                break;
                            }
                            break;
                        case 32:
                            if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_LAUNCHER, uid).isEmpty()) {
                                pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_LAUNCHER);
                                isImportant = true;
                                break;
                            }
                            break;
                        case 64:
                            if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_SMS, uid).isEmpty()) {
                                pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_SMS);
                                isImportant = true;
                                break;
                            }
                            break;
                        case 128:
                            if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_DIALER, uid).isEmpty()) {
                                pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_DIALER);
                                isImportant = true;
                                break;
                            }
                            break;
                        case HANS_IMPORTANT_CASE_DEFAULT_INPUT /* 256 */:
                            if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_INPUT, uid).isEmpty()) {
                                pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_INPUT);
                                isImportant = true;
                                break;
                            }
                            break;
                        case HANS_IMPORTANT_CASE_VPN /* 512 */:
                            if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_VPN, uid).isEmpty()) {
                                pkgState.setImportantReason(ColorCommonListManager.CONFIG_VPN);
                                isImportant = true;
                                break;
                            }
                            break;
                        case 1024:
                            if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_SCREEN_RECORDER, uid).isEmpty()) {
                                pkgState.setImportantReason(ColorCommonListManager.CONFIG_SCREEN_RECORDER);
                                isImportant = true;
                                break;
                            }
                            break;
                        case HANS_IMPORTANT_CASE_LIVE_WALLPAPER /* 2048 */:
                            if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_LIVE_WALLPAPER, uid).isEmpty()) {
                                pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_LIVE_WALLPAPER);
                                isImportant = true;
                                break;
                            }
                            break;
                        case HANS_IMPORTANT_CASE_WIDGET /* 4096 */:
                            if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_WIDGET, uid).isEmpty()) {
                                pkgState.setImportantReason(ColorCommonListManager.CONFIG_WIDGET);
                                isImportant = true;
                                break;
                            }
                            break;
                        case HANS_IMPORTANT_CASE_PREV_APP /* 8192 */:
                            if (ColorHansManager.getInstance().getLastResumeUid() == uid && ColorHansManager.getInstance().getLastResumePkgName().equals(pkg)) {
                                pkgState.setImportantReason(HANS_IMPORTANT_REASON_PREV_APP);
                                isImportant = true;
                                break;
                            }
                        case HANS_IMPORTANT_CASE_TOP_APP /* 16384 */:
                            if (ColorHansManager.getInstance().getCurResumeUid() == uid && ColorHansManager.getInstance().getCurResumePkgName().equals(pkg)) {
                                pkgState.setImportantReason(HANS_IMPORTANT_REASON_TOP_APP);
                                isImportant = true;
                                break;
                            }
                        case HANS_IMPORTANT_CASE_SPECIAL_APP /* 32768 */:
                            if (this.mSpecialAppList.contains(pkg)) {
                                pkgState.setImportantReason(HANS_IMPORTANT_REASON_SPECIAL_APP);
                                isImportant = true;
                                break;
                            }
                            break;
                        case HANS_IMPORTANT_CASE_SPECIAL_WINDOW /* 65536 */:
                            if (ColorHansManager.getInstance().inSpecialWindowMode(pkg)) {
                                pkgState.setImportantReason(HANS_IMPORTANT_REASON_SPECIAL_WINDOW);
                                isImportant = true;
                                break;
                            }
                            break;
                        case HANS_IMPORTANT_CASE_PRELOAD_APP /* 131072 */:
                            if (ColorHansManager.getInstance().isPreloadPkg(pkg, UserHandle.getUserId(uid))) {
                                pkgState.setImportantReason(HANS_IMPORTANT_REASON_PRELOAD_APP);
                                isImportant = true;
                                break;
                            }
                            break;
                    }
                } else if (this.mCommonListManager.isFloatWindow(uid)) {
                    pkgState.setImportantReason(HANS_IMPORTANT_REASON_FLOAT_WINDOW);
                    isImportant = true;
                }
            } else if (this.mCommonListManager.isProtectedByTraffic(pkgState.getLastUsedTime(), uid, pkgState.getFgService(), ColorHansManager.getInstance().getCommonConfig().isScreenOn(), ColorHansManager.getInstance().getCommonConfig().getScreenOffTime() + ColorHansPackageSelector.getInstance().getLcdOffSceneInterval(), ColorHansManager.getInstance().getCurResumeUid())) {
                pkgState.setImportantReason(HANS_IMPORTANT_REASON_CASE_TRAFFIC);
                isImportant = true;
            }
            if (isImportant) {
                return true;
            }
        }
        return false;
    }

    public void addSpecialAppList(String pkgName) {
        synchronized (this.mSpecialAppList) {
            if (!this.mSpecialAppList.contains(pkgName)) {
                this.mSpecialAppList.add(pkgName);
            }
        }
    }
}
