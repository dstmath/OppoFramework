package com.android.server.wm;

import android.app.OppoActivityManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.widget.Toast;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.wm.OppoScreenModeHelper;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.screenmode.IOppoScreenModeCallback;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class OppoScreenModeService extends IOppoScreenModeHook implements IOppoScreenModeService, OppoScreenModeHelper.Callback {
    private static final int ANIMATING_DELAY = 1000;
    public static final int CODE_NOTIFY_AIFRAMERATE_STATE = 21006;
    private static final String CONFIG_NAME_SCREEN_MODE = "ScreenMode";
    static boolean DEBUG = false;
    static boolean DEBUG_DISABLE_FORCE_STOP = SystemProperties.getBoolean("persist.sys.disable.forcestop", false);
    static boolean DEBUG_FORCE_REFRESH_RATE = false;
    static boolean DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    static final String DEFAULT_RATE_FEATURE = "oppo.display.screen.defaultsmartmode";
    static int[] DPI_FHD = new int[0];
    static int[] DPI_QHD = new int[0];
    public static final String OPPO_SCREENMODE = "opposcreenmode";
    private static final int RECORD_MODE_DELAY = SystemProperties.getInt("persist.vendor.screenmode.delay", 500);
    private static final int RECORD_MODE_DELAY_SHORT = SystemProperties.getInt("persist.vendor.screenmode.shortdelay", 0);
    private static final int REFRESH_RATE_120 = 3;
    private static final int REFRESH_RATE_60 = 2;
    private static final int REFRESH_RATE_90 = 1;
    private static final int REFRESH_RATE_AUTO = 0;
    private static final int REFRESH_RATE_SAVEPOWER = 2;
    private static final int RESOLUTION_2K = 3;
    private static final int RESOLUTION_AUTO = 1;
    private static final int RESOLUTION_FHD = 2;
    private static final String SF_COMPOSER_TOKEN = "android.ui.ISurfaceComposer";
    private static final String SF_SERVICE_NAME = "SurfaceFlinger";
    static final String TAG = "ScreenMode";
    static final int WIDTH_2K = 1440;
    static final int WIDTH_FHD = 1080;
    public static IBinder mFlinger = null;
    public static int mSFValue = -1;
    public static boolean sIsResolutionAuto = false;
    private static BroadcastReceiver sPkgReceiver = null;
    private boolean DEBUG_START_EMPTY = true;
    private final int FHD_DENSITY = SystemProperties.getInt("vendor.display.fhd_density", 420);
    private final String FHD_DPI_ZOOM = SystemProperties.get("ro.density.screenzoom.fdh", StringUtils.EMPTY);
    /* access modifiers changed from: private */
    public int MAX_REFRESH_RATE_MODE = 2;
    private final int QHD_DENSITY = SystemProperties.getInt("vendor.display.qhd_density", 560);
    private final String QHD_DPI_ZOOM = SystemProperties.get("ro.density.screenzoom.qdh", StringUtils.EMPTY);
    private String WEIXIN_PKG_NAME = "com.tencent.mm";
    /* access modifiers changed from: private */
    public final int defaultDensity = SystemProperties.getInt("vendor.display.lcd_density", 480);
    /* access modifiers changed from: private */
    public final String deviceName = SystemProperties.get("vendor.product.device", StringUtils.EMPTY);
    boolean isInDCAndLowBrightnessMode = false;
    private boolean isInPSMode = false;
    /* access modifiers changed from: private */
    public boolean mActivityChanging = false;
    private OppoActivityManagerInternal mAmInternal = null;
    /* access modifiers changed from: private */
    public final Object mAnimatingLock = new Object();
    private IOppoScreenModeCallback mCallback = null;
    Context mContext;
    HashMap<String, Integer> mCtsRatePackage = new HashMap<>();
    OppoBaseAppWindowToken mCurrentAppToken;
    public int mCurrentModeId;
    private int mCurrentTPStatus = 0;
    /* access modifiers changed from: private */
    public int mDefaultFreshRate = this.mRefreshRateFullspeed;
    ModeRecord mDefaultMode;
    Display mDisplay;
    DisplayContent mDisplayContent;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.server.wm.OppoScreenModeService.AnonymousClass1 */

        public void onDisplayAdded(int displayId) {
            if (displayId != 0) {
                Display display = OppoScreenModeService.this.mDisplayManager.getDisplay(displayId);
                if (OppoScreenModeService.DEBUG || OppoScreenModeService.DEBUG_PANIC) {
                    Slog.d("ScreenMode", "add display=" + display + ",displayId=" + displayId);
                }
                synchronized (OppoScreenModeService.this.mLock) {
                    boolean lastStatus = OppoScreenModeService.this.mHDMIDisplayAdded;
                    if (display != null) {
                        if (2 != display.getType()) {
                            if (3 != display.getType()) {
                                if (5 == display.getType()) {
                                    String pkgName = display.getOwnerPackageName();
                                    Slog.d("ScreenMode", "add display pkgName=" + pkgName);
                                    if (pkgName != null && pkgName.length() > 1 && !OppoScreenModeService.this.mLowRateDisplaList.contains(Integer.valueOf(displayId)) && OppoScreenModeService.this.mRUSHelper != null && OppoScreenModeService.this.mRUSHelper.onRecordList(pkgName)) {
                                        boolean unused = OppoScreenModeService.this.mHDMIDisplayAdded = true;
                                        OppoScreenModeService.this.mLowRateDisplaList.add(Integer.valueOf(displayId));
                                        OppoScreenModeService.this.mHandler.removeMessages(16);
                                        OppoScreenModeService.this.mHandler.sendEmptyMessageDelayed(16, 1000);
                                    }
                                }
                            }
                        }
                        boolean unused2 = OppoScreenModeService.this.mHDMIDisplayAdded = true;
                        if (!OppoScreenModeService.this.mLowRateDisplaList.contains(Integer.valueOf(displayId))) {
                            OppoScreenModeService.this.mLowRateDisplaList.add(Integer.valueOf(displayId));
                        }
                    }
                    if (lastStatus != OppoScreenModeService.this.mHDMIDisplayAdded) {
                        OppoScreenModeService.this.mHandler.removeMessages(1);
                        OppoScreenModeService.this.mHandler.sendEmptyMessage(1);
                    }
                }
            }
        }

        public void onDisplayRemoved(int displayId) {
            if (displayId != 0) {
                synchronized (OppoScreenModeService.this.mLock) {
                    if (OppoScreenModeService.DEBUG || OppoScreenModeService.DEBUG_PANIC) {
                        Slog.d("ScreenMode", "remove displayId=" + displayId + ",size=" + OppoScreenModeService.this.mLowRateDisplaList.size());
                    }
                    boolean lastStatus = OppoScreenModeService.this.mHDMIDisplayAdded;
                    if (OppoScreenModeService.this.mHDMIDisplayAdded && OppoScreenModeService.this.mLowRateDisplaList.contains(Integer.valueOf(displayId))) {
                        OppoScreenModeService.this.mLowRateDisplaList.remove(new Integer(displayId));
                        if (OppoScreenModeService.this.mLowRateDisplaList.size() == 0) {
                            boolean unused = OppoScreenModeService.this.mHDMIDisplayAdded = false;
                        }
                    }
                    if (lastStatus != OppoScreenModeService.this.mHDMIDisplayAdded) {
                        OppoScreenModeService.this.mHandler.removeMessages(16);
                        OppoScreenModeService.this.mHandler.removeMessages(1);
                        OppoScreenModeService.this.mHandler.sendEmptyMessage(1);
                    }
                }
            }
        }

        public void onDisplayChanged(int displayId) {
        }
    };
    DisplayManager mDisplayManager;
    private boolean mDynamicFPSSupport = false;
    HashMap<String, Integer> mFixedRatePackage = new HashMap<>();
    HashMap<String, Integer> mForcedRateList = new HashMap<>();
    private String mGamePkg = "unknow";
    private int mGameRequestRefreshRate = 0;
    /* access modifiers changed from: private */
    public boolean mHDMIDisplayAdded = false;
    /* access modifiers changed from: private */
    public ScreenModeHandler mHandler = null;
    int mHighTemperatureRate = -1;
    /* access modifiers changed from: private */
    public boolean mInAnimating = false;
    private boolean mInSceenSplitMode = false;
    int mLastPrefModeId = -1;
    private int mLastVideoRefreshRate = -1;
    private int mLastWindowUid = -1;
    private boolean mListOptimized = false;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public ArrayList<Integer> mLowRateDisplaList = new ArrayList<>();
    int mMEMCRequestRate = 0;
    private HashMap<Integer, ModeRecord> mModeRecordMap = new HashMap<>();
    ModeRecord[] mModeRecords;
    ArrayList<String> mNoKillList = new ArrayList<>(Arrays.asList("com.oppo.autotest.restart", "com.google.android.marvin.talkback", "com.android.systemui", "com.oppo.multimedia.pixelworks", "com.google.android.gsf", "com.nearme.romupdate", "com.qualcomm.timeservice"));
    OppoScreenCompat mOppoScreenCompat = null;
    private int mPSModeRate = 0;
    private HashMap<Integer, ModeRecord> mParamsMap = new HashMap<>();
    /* access modifiers changed from: private */
    public OppoScreenModeHelper mRUSHelper = null;
    private final Object mRateLock = new Object();
    private int mRefreshRateFullspeed = 2;
    int mResolutionSettings = 3;
    private boolean mResolutionSupport = false;
    /* access modifiers changed from: private */
    public boolean mResolutionSwitchEnable = false;
    int mScreenRateSettings = 0;
    int mScreenWidth = 0;
    WindowManagerService mService;
    boolean mServiceReady = false;
    SettingsObserver mSettingsObserver;
    private final Object mTPStatusLoack = new Object();
    private boolean mWhiteListLoaded = false;
    private int mWhiteListSequenec = 0;
    private boolean mWindowOnTPList = false;

    public OppoScreenModeService() {
        if (DEBUG) {
            Slog.d("ScreenMode", "construct called");
        }
    }

    public void publish() {
        if (DEBUG) {
            Slog.d("ScreenMode", "publish");
        }
        ServiceManager.addService(OPPO_SCREENMODE, asBinder());
        this.mServiceReady = true;
    }

    public void addCallback(IOppoScreenModeCallback callback) {
        this.mCallback = callback;
    }

    public void remove(IOppoScreenModeCallback callback) {
        this.mCallback = null;
    }

    public void setClientRefreshRate(IBinder token, int rate) {
        if (this.mServiceReady && this.mDynamicFPSSupport) {
            AppWindowToken appToken = getAppToken(token);
            OppoBaseAppWindowToken baseAppToken = appToken != null ? typeCasting(appToken) : null;
            if (baseAppToken != null) {
                synchronized (this.mRateLock) {
                    if (needCheckWhiteList(baseAppToken.mTmpRefreshRateSpec)) {
                        baseAppToken.mTmpRefreshRateSpec = getPackageRate(this.mScreenRateSettings, baseAppToken);
                    }
                    if (requestFreshRateFromSTView(baseAppToken, rate) || RateModeSpec.getAWTSettingsRate(baseAppToken.mTmpRefreshRateSpec) != this.mScreenRateSettings) {
                        requestupdateRefreshRate(baseAppToken, rate);
                    }
                }
            } else if (DEBUG) {
                Slog.d("ScreenMode", " setRefreshRate nothing done token " + token + " rate " + rate);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getAppToken(IBinder token) {
        AppWindowToken appToken = this.mService.getDefaultDisplayContentLocked().getAppWindowToken(token);
        if (DEBUG) {
            Slog.e("ScreenMode", "getAppToken " + appToken);
        }
        return appToken;
    }

    private boolean requestFreshRateFromSTView(OppoBaseAppWindowToken token, int rate) {
        if (token != null && rate >= 0 && rate <= this.MAX_REFRESH_RATE_MODE && !RateModeSpec.isGameOnList(token.mTmpRefreshRateSpec) && rate != RateModeSpec.getSTViewRate(token.mTmpRefreshRateSpec)) {
            token.mTmpRefreshRateSpec = RateModeSpec.setSTViewRate(token.mTmpRefreshRateSpec, rate);
            return true;
        } else if (!DEBUG) {
            return false;
        } else {
            Slog.d("ScreenMode", " requestFreshRateFromSTView token " + token + " rate =" + rate);
            return false;
        }
    }

    public void setRefreshRate(IBinder token, int rate) {
        if (this.mServiceReady && this.mDynamicFPSSupport) {
            AppWindowToken appToken = getAppToken(token);
            synchronized (this.mAnimatingLock) {
                if (!this.mInAnimating && appToken != null) {
                    this.mActivityChanging = true;
                }
            }
            setRefreshRate(appToken, rate);
        }
    }

    public void setRefreshRate(AppWindowToken token, int rate) {
        if (this.mServiceReady) {
            if (DEBUG) {
                Slog.d("ScreenMode", "setRefreshRate token " + token + " rate " + rate);
            }
            if (token != null) {
                OppoBaseAppWindowToken baseToken = typeCasting(token);
                if (baseToken == null) {
                    Slog.e("ScreenMode", "setRefreshRete failed for token type cast failed!");
                } else {
                    setRefreshRateInternal(baseToken, rate);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        if (r6 > r4.MAX_REFRESH_RATE_MODE) goto L_0x0014;
     */
    public void setRefreshRateInternal(OppoBaseAppWindowToken baseToken, int rate) {
        if (baseToken == null) {
            Slog.e("ScreenMode", "setRefreshRateInternal: token is null");
            return;
        }
        synchronized (this.mRateLock) {
            boolean needUpdate = false;
            if (rate >= 0) {
            }
            rate = this.mScreenRateSettings;
            if (needCheckWhiteList(baseToken.mTmpRefreshRateSpec)) {
                baseToken.mTmpRefreshRateSpec = getPackageRate(rate, baseToken);
                needUpdate = true;
            }
            if (needUpdate || RateModeSpec.getAWTSettingsRate(baseToken.mTmpRefreshRateSpec) != this.mScreenRateSettings) {
                requestupdateRefreshRate(baseToken, rate);
            }
        }
    }

    private void requestupdateRefreshRate(OppoBaseAppWindowToken token, int rate) {
        token.mTmpRefreshRateSpec = RateModeSpec.setSettingsRefreshRate(token.mTmpRefreshRateSpec, this.mScreenRateSettings);
        token.mTmpRefreshRateSpec = RateModeSpec.setRefreshRate(token.mTmpRefreshRateSpec, rate);
        token.mRefreshRateSpec = RateModeSpec.setRefreshRate(token.mRefreshRateSpec, this.mModeRecordMap.get(Integer.valueOf(this.mCurrentModeId)).rateId);
        int delayMs = (rate == this.mRefreshRateFullspeed || token.isActivityTypeHome() || isCtsActivity(token)) ? RECORD_MODE_DELAY_SHORT : RECORD_MODE_DELAY;
        this.mHandler.removeMessages(2, token);
        ScreenModeHandler screenModeHandler = this.mHandler;
        screenModeHandler.sendMessageDelayed(screenModeHandler.obtainMessage(2, token), (long) delayMs);
        Message message = this.mHandler.obtainMessage(4);
        message.arg1 = rate;
        this.mHandler.sendMessage(message);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0079, code lost:
        return false;
     */
    public boolean requestRefreshRate(boolean open, int rate) {
        if (!this.mServiceReady || !this.mDynamicFPSSupport) {
            return false;
        }
        if (DEBUG || DEBUG_PANIC) {
            Slog.d("ScreenMode", "requestRefreshRate open=" + open + ",rate=" + rate);
        }
        synchronized (this.mLock) {
            if (open) {
                if (rate > 0) {
                    if (rate <= this.MAX_REFRESH_RATE_MODE && (this.mLastVideoRefreshRate == 0 || this.mLastVideoRefreshRate == rate)) {
                        if ((!this.mHDMIDisplayAdded && this.mHighTemperatureRate <= 0) || rate == 2) {
                            this.mMEMCRequestRate = rate;
                            notifyFlingerRefreshRate(this.MAX_REFRESH_RATE_MODE);
                        }
                    }
                }
                if (DEBUG || DEBUG_PANIC) {
                    Slog.d("ScreenMode", " can't open PW " + this.mHighTemperatureRate);
                }
            } else {
                this.mMEMCRequestRate = 0;
                notifyFlingerRefreshRate(this.mScreenRateSettings);
            }
            this.mLastVideoRefreshRate = -1;
            this.mService.requestTraversal();
            return true;
        }
    }

    public void notifyFlingerRefreshRate(int val) {
        if (mSFValue != val) {
            SystemProperties.set("sys.oppo.display.rate", StringUtils.EMPTY + val);
            if (mFlinger == null) {
                mFlinger = ServiceManager.getService(SF_SERVICE_NAME);
            }
            try {
                if (mFlinger != null) {
                    Parcel data = Parcel.obtain();
                    data.writeInterfaceToken(SF_COMPOSER_TOKEN);
                    data.writeInt(val);
                    Parcel reply = Parcel.obtain();
                    mFlinger.transact(CODE_NOTIFY_AIFRAMERATE_STATE, data, reply, 1);
                    data.recycle();
                    reply.recycle();
                    mSFValue = val;
                    if (DEBUG || DEBUG_PANIC) {
                        Slog.d("ScreenMode", "notifyFlingerRefreshRate val=" + val);
                    }
                    return;
                }
                Slog.d("ScreenMode", "notifyFlingerRefreshRate null");
            } catch (RemoteException e) {
                Slog.d("ScreenMode", "get SurfaceFlinger Service failed");
            }
        }
    }

    public boolean supportDisplayCompat(String str, int uid) {
        if (this.mServiceReady && this.mResolutionSupport && DEBUG) {
            Slog.d("ScreenMode", "supportDisplayCompat str=" + str + ",uid=" + uid);
        }
        return false;
    }

    public boolean setHighTemperatureStatus(int status, int rate) {
        int lastStatus;
        if (!this.mServiceReady || !this.mDynamicFPSSupport) {
            return false;
        }
        if (DEBUG || DEBUG_PANIC) {
            Slog.d("ScreenMode", "setHighTemperatureStatus status=" + status + ",rate=" + rate);
        }
        synchronized (this.mLock) {
            lastStatus = this.mHighTemperatureRate;
            if (status == 0 || rate <= 0 || rate > this.MAX_REFRESH_RATE_MODE) {
                this.mHighTemperatureRate = -1;
                notifyFlingerRefreshRate(this.mScreenRateSettings);
            } else {
                this.mHighTemperatureRate = rate;
                notifyFlingerRefreshRate(this.MAX_REFRESH_RATE_MODE);
            }
        }
        if (lastStatus == this.mHighTemperatureRate) {
            return true;
        }
        Slog.d("ScreenMode", " temperature change status =" + this.mHighTemperatureRate);
        this.mService.requestTraversal();
        return true;
    }

    public void enterDCAndLowBrightnessMode(boolean enter) {
        synchronized (this.mLock) {
            if (this.isInDCAndLowBrightnessMode != enter) {
                this.isInDCAndLowBrightnessMode = enter;
            }
        }
        Slog.d("ScreenMode", "enter DC and low brightness mode:" + enter);
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
    }

    public void enterPSMode(boolean enter) {
        enterPSModeInternal(enter, this.mRefreshRateFullspeed);
    }

    public void enterPSModeOnRate(boolean enter, int rate) {
        enterPSModeInternal(enter, rate);
    }

    private void enterPSModeInternal(boolean enter, int rate) {
        synchronized (this.mLock) {
            this.isInPSMode = enter;
            if (enter) {
                this.mPSModeRate = rate;
                notifyFlingerRefreshRate(this.MAX_REFRESH_RATE_MODE);
            } else {
                this.mPSModeRate = 0;
                notifyFlingerRefreshRate(this.mScreenRateSettings);
            }
        }
        Slog.d("ScreenMode", "enterPSMode enter =" + enter + ",rate=" + rate);
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
    }

    public boolean requestGameRefreshRate(String pkg, int rate) {
        if (!this.mServiceReady || !this.mDynamicFPSSupport) {
            return false;
        }
        synchronized (this.mLock) {
            this.mGamePkg = pkg == null ? "unknow" : pkg;
            if (rate <= 0 || rate > this.MAX_REFRESH_RATE_MODE) {
                this.mGameRequestRefreshRate = 0;
                notifyFlingerRefreshRate(this.mScreenRateSettings);
            } else {
                this.mGameRequestRefreshRate = rate;
                notifyFlingerRefreshRate(this.MAX_REFRESH_RATE_MODE);
            }
        }
        if (DEBUG || DEBUG_PANIC) {
            Slog.d("ScreenMode", " game rate pkg= " + pkg + ",rate=" + rate);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
        return true;
    }

    public int getPreferredModeId(WindowState w, int candidateMode) {
        if (!this.mServiceReady) {
            return 0;
        }
        if (DEBUG_FORCE_REFRESH_RATE) {
            int forceRate = this.mScreenRateSettings;
            if (forceRate == 0) {
                forceRate = 2;
            }
            if (DEBUG) {
                Slog.d("ScreenMode", "getPreferredModeId  force rate =" + this.mScreenRateSettings);
            }
            return getModeRecord(forceRate, this.mResolutionSettings).mode.getModeId();
        } else if (this.isInPSMode) {
            if (DEBUG) {
                Slog.d("ScreenMode", "getPreferredModeId w " + w + " isInPSMode =" + this.isInPSMode);
            }
            return getModeRecord(this.mPSModeRate, this.mResolutionSettings).mode.getModeId();
        } else if ((w.mAttrs.type == 2315 && "OnScreenFingerprintIcon".equals(w.mAttrs.getTitle())) || (w.mAttrs.type == 2000 && this.mService.isKeyguardShowingAndNotOccluded())) {
            if (DEBUG) {
                Slog.d("ScreenMode", "getPreferredModeId w " + w + " full speed when keyguard showing");
            }
            return getModeRecord(this.mRefreshRateFullspeed, this.mResolutionSettings).mode.getModeId();
        } else if (w.mAppToken == null || w.getAttrs().type == 3) {
            return 0;
        } else {
            int i = this.mHighTemperatureRate;
            if (i > 0 && i <= this.MAX_REFRESH_RATE_MODE) {
                if (DEBUG) {
                    Slog.d("ScreenMode", "getPreferredModeId w " + w + " temperature request =" + this.mHighTemperatureRate);
                }
                return getModeRecord(this.mHighTemperatureRate, this.mResolutionSettings).mode.getModeId();
            } else if (!this.mHDMIDisplayAdded && this.mGameRequestRefreshRate != 0 && this.mGamePkg.equals(w.mAttrs.packageName)) {
                if (DEBUG) {
                    Slog.d("ScreenMode", "getPreferredModeId w " + w + " game rate = " + this.mGameRequestRefreshRate);
                }
                return getModeRecord(this.mGameRequestRefreshRate, this.mResolutionSettings).mode.getModeId();
            } else if (this.mScreenRateSettings == 2 || this.mInSceenSplitMode || this.mHDMIDisplayAdded) {
                if (DEBUG) {
                    Slog.d("ScreenMode", "getPreferredModeId w " + w + " REFRESH_RATE_SAVEPOWER");
                }
                return getModeRecord(2, this.mResolutionSettings).mode.getModeId();
            } else {
                int videoRate = videoRefreshRate(w);
                int i2 = this.mMEMCRequestRate;
                if (i2 > 0 && i2 <= this.MAX_REFRESH_RATE_MODE) {
                    if (DEBUG) {
                        Slog.d("ScreenMode", "getPreferredModeId w " + w + " memc rate =" + this.mMEMCRequestRate);
                    }
                    return getModeRecord(this.mMEMCRequestRate, this.mResolutionSettings).mode.getModeId();
                } else if (!this.isInDCAndLowBrightnessMode || this.mInAnimating || this.mLastPrefModeId == -1 || this.mLastWindowUid != w.getOwningUid()) {
                    if (this.mLastWindowUid != w.getOwningUid()) {
                        if (DEBUG) {
                            Slog.d("ScreenMode", "getPreferredModeId w " + w + " lastUid = " + this.mLastWindowUid + ",uid=" + w.getOwningUid());
                        }
                        synchronized (this.mAnimatingLock) {
                            this.mLastWindowUid = w.getOwningUid();
                            if (!this.mInAnimating) {
                                this.mInAnimating = true;
                                this.mActivityChanging = false;
                                this.mHandler.removeMessages(8);
                                this.mHandler.sendEmptyMessageDelayed(8, 1000);
                            }
                        }
                    }
                    this.mLastPrefModeId = -1;
                    if (this.mInAnimating) {
                        if (DEBUG) {
                            Slog.d("ScreenMode", "getPreferredModeId w " + w + " mInAnimating = " + this.mInAnimating);
                        }
                        return getModeRecord(this.mRefreshRateFullspeed, this.mResolutionSettings).mode.getModeId();
                    }
                    OppoBaseAppWindowToken baseAppWinToken = typeCasting(w.mAppToken);
                    int refreshRateSpec = baseAppWinToken != null ? baseAppWinToken.mRefreshRateSpec : 0;
                    this.mWindowOnTPList = false;
                    if (RateModeSpec.isOnTPList(refreshRateSpec)) {
                        this.mWindowOnTPList = true;
                    }
                    if (-1 != videoRate) {
                        if (DEBUG) {
                            Slog.d("ScreenMode", "getPreferredModeId w " + w + " on video list, videoRate=" + videoRate);
                        }
                        this.mLastPrefModeId = getModeRecord(videoRate, this.mResolutionSettings).mode.getModeId();
                    } else if (RateModeSpec.isGameOnList(refreshRateSpec)) {
                        int gameRate = RateModeSpec.getOnListRate(refreshRateSpec);
                        if (DEBUG) {
                            Slog.d("ScreenMode", "getPreferredModeId w " + w + " game list rate =" + gameRate);
                        }
                        this.mLastPrefModeId = getModeRecord(gameRate, this.mResolutionSettings).mode.getModeId();
                    } else if (RateModeSpec.getSTViewRate(refreshRateSpec) != 0) {
                        int refreshRate = RateModeSpec.getSTViewRate(refreshRateSpec);
                        if (DEBUG) {
                            Slog.d("ScreenMode", "getPreferredModeId w " + w + "  stview rate =" + refreshRate);
                        }
                        this.mLastPrefModeId = getModeRecord(refreshRate, this.mResolutionSettings).mode.getModeId();
                    } else if (RateModeSpec.isOnList(refreshRateSpec)) {
                        int listRate = RateModeSpec.getOnListRate(refreshRateSpec);
                        if (DEBUG) {
                            Slog.d("ScreenMode", "getPreferredModeId w " + w + " on list rate =" + listRate);
                        }
                        this.mLastPrefModeId = getModeRecord(listRate, this.mResolutionSettings).mode.getModeId();
                    } else if (candidateMode == 0 || RateModeSpec.getRate(refreshRateSpec) != 0) {
                        int refreshRate2 = RateModeSpec.getRate(refreshRateSpec);
                        if (DEBUG) {
                            Slog.d("ScreenMode", "getPreferredModeId w " + w + " rate " + refreshRate2 + ",mAttrs.preferredRefreshRate=" + w.mAttrs.preferredRefreshRate + ", hRateSpec= 0x" + Integer.toHexString(refreshRateSpec));
                        }
                        this.mLastPrefModeId = getModeRecord(refreshRate2, this.mResolutionSettings).mode.getModeId();
                    } else {
                        if (DEBUG) {
                            Slog.d("ScreenMode", "getPreferredModeId w " + w + " use candidate mode " + candidateMode);
                        }
                        this.mLastPrefModeId = candidateMode;
                    }
                    return this.mLastPrefModeId;
                } else {
                    if (DEBUG) {
                        Slog.d("ScreenMode", "getPreferredModeId in DC and low brightness mode ");
                    }
                    return this.mLastPrefModeId;
                }
            }
        }
    }

    private int videoRefreshRate(WindowState w) {
        ModeRecord modeRecord;
        OppoBaseAppWindowToken baseAppWinToken = typeCasting(w.mAppToken);
        if (baseAppWinToken == null) {
            return -1;
        }
        int i = 0;
        if (!RateModeSpec.isVideoOnList(baseAppWinToken.mRefreshRateSpec)) {
            synchronized (this.mLock) {
                this.mLastVideoRefreshRate = 0;
            }
            return -1;
        }
        if (DEBUG) {
            Slog.d("ScreenMode", "getPreferredModeId w " + w + ",mAttrs.preferredRefreshRate=" + w.mAttrs.preferredRefreshRate + ",mAttrs.preferredDisplayModeId=" + w.mAttrs.preferredDisplayModeId);
        }
        int rateId = -1;
        if (0.0f != w.mAttrs.preferredRefreshRate) {
            if (((double) Math.abs(w.mAttrs.preferredRefreshRate - 60.0f)) < 0.1d) {
                rateId = 2;
            } else if (((double) Math.abs(w.mAttrs.preferredRefreshRate - 90.0f)) < 0.1d) {
                rateId = 1;
            } else if (((double) Math.abs(w.mAttrs.preferredRefreshRate - 120.0f)) < 0.1d) {
                rateId = 3;
            }
        }
        if (!(-1 != rateId || w.mAttrs.preferredDisplayModeId == 0 || (modeRecord = this.mModeRecordMap.get(Integer.valueOf(w.mAttrs.preferredDisplayModeId))) == null)) {
            rateId = modeRecord.rateId;
        }
        if (-1 != rateId) {
            i = rateId;
        }
        checkVideoRateChange(w, i);
        return rateId;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0010, code lost:
        if (r3.mCallback == null) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0012, code lost:
        r3.mCallback.requestRefreshRate(r4.mAttrs.packageName, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001d, code lost:
        if (com.android.server.wm.OppoScreenModeService.DEBUG_PANIC != false) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        if (com.android.server.wm.OppoScreenModeService.DEBUG == false) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0023, code lost:
        r1 = new java.lang.StringBuilder();
        r1.append(" feedback PW APP rate change, w ");
        r1.append(r4);
        r1.append(",appRate");
        r1.append(r5);
        r1.append(",");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0041, code lost:
        if (r3.mCallback == null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0045, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
        r1.append(r2);
        android.util.Slog.d("ScreenMode", r1.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0051, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0052, code lost:
        android.util.Slog.d("ScreenMode", " request close PW error", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
        if (r3.mMEMCRequestRate == 0) goto L_0x0059;
     */
    private void checkVideoRateChange(WindowState w, int appRate) {
        synchronized (this.mLock) {
            if (this.mLastVideoRefreshRate == appRate) {
                return;
            }
        }
        synchronized (this.mLock) {
            this.mLastVideoRefreshRate = appRate;
        }
    }

    public int updateGlobalModeId(int modeId) {
        if (!this.mServiceReady) {
            return modeId;
        }
        if (modeId != 0) {
            ModeRecord modeRecord = this.mModeRecordMap.get(Integer.valueOf(modeId));
            if (!(modeRecord == null || modeRecord.resolutionId == this.mResolutionSettings)) {
                modeId = getModeRecord(modeRecord.rateId, this.mResolutionSettings).mode.getModeId();
            }
            if (DEBUG_PANIC || DEBUG) {
                int i = this.mModeRecordMap.get(Integer.valueOf(this.mCurrentModeId)).rateId;
                int i2 = this.mModeRecordMap.get(Integer.valueOf(modeId)).rateId;
                if (this.mCurrentModeId != modeId) {
                    Slog.d("ScreenMode", " switch from " + this.mModeRecordMap.get(Integer.valueOf(this.mCurrentModeId)) + " to " + this.mModeRecordMap.get(Integer.valueOf(modeId)) + ",mInAnimating=" + this.mInAnimating);
                }
            }
            this.mCurrentModeId = modeId;
            updateTPStatus();
        }
        if (DEBUG) {
            Slog.d("ScreenMode", "updateGlobalModeId modeId " + modeId + " mCurrentModeId " + this.mCurrentModeId);
        }
        return this.mCurrentModeId;
    }

    /* access modifiers changed from: package-private */
    public void updateTPStatus() {
        boolean change = false;
        synchronized (this.mTPStatusLoack) {
            ModeRecord modeRecord = this.mModeRecordMap.get(Integer.valueOf(this.mCurrentModeId));
            if (this.mWindowOnTPList && modeRecord.rateId == 3) {
                if (this.mCurrentTPStatus != 1) {
                    change = true;
                }
                this.mCurrentTPStatus = 1;
            } else if (this.mCurrentTPStatus != 0) {
                change = true;
                this.mCurrentTPStatus = 0;
            }
            if (DEBUG) {
                Slog.e("ScreenMode", "updateTPStatus mCurrentTPStatus " + this.mCurrentTPStatus + ",change=" + change);
            }
            if (change) {
                this.mHandler.removeMessages(7);
                Message msg = this.mHandler.obtainMessage(7);
                msg.arg1 = this.mCurrentTPStatus;
                this.mHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void transmitData(int data) {
        StringBuilder sb;
        FileWriter writer = null;
        String content = Integer.toString(data);
        try {
            writer = new FileWriter("/proc/touchpanel/report_rate_white_list");
            writer.write(content);
            writer.close();
            FileWriter writer2 = null;
            if (writer2 != null) {
                try {
                    writer2.close();
                    return;
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
            } else {
                return;
            }
            sb.append("finally fail to transmit data");
            sb.append(e);
            Slog.e("ScreenMode", sb.toString());
        } catch (IOException e2) {
            Slog.e("ScreenMode", "fail to transmit data" + e2);
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e4) {
                    Slog.e("ScreenMode", "finally fail to transmit data" + e4);
                }
            }
            throw th;
        }
    }

    public void onSetDensityForUser(int density, int userId) {
        if (DEBUG || DEBUG_PANIC) {
            Slog.d("ScreenMode", "onSetDensityForUser density " + density + " userId " + userId);
        }
        if (this.mResolutionSwitchEnable) {
            this.mHandler.removeMessages(5);
            ScreenModeHandler screenModeHandler = this.mHandler;
            screenModeHandler.sendMessageDelayed(screenModeHandler.obtainMessage(5, density, userId), 500);
        }
    }

    public int adjustDensityForUser(int srcDensity, int userId) {
        if (!this.mResolutionSwitchEnable) {
            return srcDensity;
        }
        int oldResolution = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "oppo_screen_resolution_backup", this.mDefaultMode.resolutionId, userId);
        if (DEBUG) {
            Slog.d("ScreenMode", "adjustDensityForUser oldResolution " + oldResolution + " mResolutionSettings " + this.mResolutionSettings);
        }
        return adjustDensityForResolution(srcDensity, oldResolution, this.mResolutionSettings);
    }

    /* access modifiers changed from: package-private */
    public int adjustDensityForResolution(int srcDensity, int oldResolution, int newResolution) {
        if (oldResolution != newResolution) {
            ModeRecord oldMode = getModeRecord(this.mScreenRateSettings, oldResolution);
            ModeRecord newMode = getModeRecord(this.mScreenRateSettings, newResolution);
            if (!(oldMode == null || newMode == null)) {
                int[] iArr = new int[0];
                int[] iArr2 = new int[0];
                int srcWidth = oldMode.mode.getPhysicalWidth();
                int[] srcDpiList = oldMode.sDpiList;
                int dstWidth = newMode.mode.getPhysicalWidth();
                int[] dstDpiList = newMode.sDpiList;
                if (!(srcDpiList.length == 0 || dstDpiList.length == 0 || srcWidth == 0 || dstWidth == 0)) {
                    for (int idx = 0; idx < srcDpiList.length; idx++) {
                        if (srcDensity == srcDpiList[idx]) {
                            if (DEBUG || DEBUG_PANIC) {
                                Slog.d("ScreenMode", "adjustDensityForUser from " + srcDensity + " to " + dstDpiList[idx]);
                            }
                            return dstDpiList[idx];
                        }
                    }
                }
            }
        }
        return srcDensity;
    }

    private int getDensityForResolution(int density) {
        if (density != 0) {
            if (2 == this.mResolutionSettings) {
                String str = this.FHD_DPI_ZOOM;
                if (!str.contains(StringUtils.EMPTY + density)) {
                    return this.FHD_DENSITY;
                }
            } else {
                String str2 = this.QHD_DPI_ZOOM;
                if (!str2.contains(StringUtils.EMPTY + density)) {
                    return this.QHD_DENSITY;
                }
            }
        }
        return density;
    }

    public void updateScreenSplitMode(boolean mode) {
        if (DEBUG) {
            Slog.d("ScreenMode", "updateScreenSplitMode mode " + mode);
        }
        this.mInSceenSplitMode = mode;
    }

    public void startAnimation(boolean start) {
        if (!this.isInDCAndLowBrightnessMode) {
            if (DEBUG) {
                Slog.d("ScreenMode", "startAnimation start " + start);
            }
            synchronized (this.mAnimatingLock) {
                if (start) {
                    if (this.mActivityChanging) {
                        this.mActivityChanging = false;
                        this.mInAnimating = true;
                        this.mHandler.removeMessages(8);
                        this.mHandler.sendEmptyMessageDelayed(8, 1000);
                    }
                }
            }
        }
    }

    public boolean isDisplayCompat(String packageName, int uid) {
        OppoScreenCompat oppoScreenCompat = this.mOppoScreenCompat;
        if (oppoScreenCompat != null) {
            return oppoScreenCompat.isDisplayCompat(packageName, uid);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public ModeRecord getModeRecord(int refreshRate, int resolution) {
        if (resolution == 1) {
            resolution = this.mDefaultMode.resolutionId;
        }
        if (refreshRate == 0) {
            if (this.mScreenRateSettings != 0) {
                refreshRate = this.mScreenRateSettings;
            } else {
                refreshRate = this.mDefaultMode.rateId;
            }
        }
        ModeRecord result = this.mParamsMap.get(Integer.valueOf((refreshRate << 4) | resolution));
        if (result != null) {
            return result;
        }
        return this.mDefaultMode;
    }

    /* access modifiers changed from: private */
    public void updateRefreshRate(OppoBaseAppWindowToken token) {
        if (token != null) {
            synchronized (this.mRateLock) {
                this.mCurrentAppToken = token;
                token.mRefreshRateSpec = token.mTmpRefreshRateSpec;
            }
            if (DEBUG) {
                Slog.d("ScreenMode", "updateRefreshRate token " + token + " rate=" + token.mRefreshRateSpec);
            }
            this.mService.requestTraversal();
        }
    }

    /* access modifiers changed from: private */
    public void updateScenario(int refreshRate) {
        if (DEBUG) {
            Slog.d("ScreenMode", "updateScenario refreshRate" + refreshRate);
        }
    }

    /* access modifiers changed from: package-private */
    public int getPackageRate(int autoRate, OppoBaseAppWindowToken token) {
        if (this.mListOptimized) {
            return getPackageRateFromOptimizedList(autoRate, token);
        }
        return getPackageRateFromHelper(autoRate, token);
    }

    /* access modifiers changed from: package-private */
    public int getPackageRateFromOptimizedList(int autoRate, OppoBaseAppWindowToken token) {
        Integer rate = null;
        if (!token.mActivityName.isEmpty()) {
            rate = this.mForcedRateList.get(token.mActivityName);
        }
        if (rate == null) {
            rate = this.mForcedRateList.get(token.mPackageName);
        }
        if (rate == null && !token.mActivityName.isEmpty()) {
            rate = this.mFixedRatePackage.get(token.mActivityName);
        }
        if (rate == null) {
            rate = this.mFixedRatePackage.get(token.mPackageName);
        }
        if (rate != null) {
            return rate.intValue();
        }
        return autoRate;
    }

    /* access modifiers changed from: package-private */
    public int getPackageRateFromHelper(int autoRate, OppoBaseAppWindowToken token) {
        Integer rate = null;
        token.mTmpRefreshRateSpec = RateModeSpec.setWhiteListSeq(token.mTmpRefreshRateSpec, this.mWhiteListSequenec);
        if (DEBUG) {
            Slog.d("ScreenMode", "getPackageRate activity = " + token.mActivityName + ",package=" + token.mPackageName);
        }
        if (this.mRUSHelper.onTPWhiteList(token.mPackageName)) {
            token.mTmpRefreshRateSpec = RateModeSpec.setTPListStatus(token.mTmpRefreshRateSpec);
            if (DEBUG) {
                Slog.d("ScreenMode", "tp white list name = " + token.mPackageName);
            }
        }
        if (!token.mActivityName.isEmpty()) {
            rate = this.mRUSHelper.getVideoMode(token.mActivityName);
        }
        if (rate == null) {
            rate = this.mRUSHelper.getVideoMode(token.mPackageName);
        }
        if (rate != null) {
            if (DEBUG) {
                Slog.d("ScreenMode", "video list rate =" + rate + ",name=" + token.mPackageName);
            }
            token.mTmpRefreshRateSpec = RateModeSpec.setOnVideoListRate(token.mTmpRefreshRateSpec, rate.intValue());
            return token.mTmpRefreshRateSpec;
        }
        if (!token.mActivityName.isEmpty()) {
            rate = this.mRUSHelper.getGameMode(token.mActivityName);
        }
        if (rate == null) {
            rate = this.mRUSHelper.getGameMode(token.mPackageName);
        }
        if (rate != null) {
            if (DEBUG) {
                Slog.d("ScreenMode", "game list rate =" + rate + ",name=" + token.mPackageName);
            }
            token.mTmpRefreshRateSpec = RateModeSpec.setOnListGameRate(token.mTmpRefreshRateSpec, rate.intValue());
            return token.mTmpRefreshRateSpec;
        }
        if (!token.mActivityName.isEmpty()) {
            rate = this.mRUSHelper.getActivityMode(token.mActivityName);
        }
        if (rate == null) {
            rate = this.mRUSHelper.getPackageMode(token.mPackageName);
        }
        if (rate != null) {
            if (DEBUG) {
                Slog.d("ScreenMode", "on list rate =" + rate + ",token=" + token);
            }
            token.mTmpRefreshRateSpec = RateModeSpec.setOnListRate(token.mTmpRefreshRateSpec, rate.intValue());
            return token.mTmpRefreshRateSpec;
        }
        token.mTmpRefreshRateSpec = RateModeSpec.setRefreshRate(token.mTmpRefreshRateSpec, autoRate);
        return token.mTmpRefreshRateSpec;
    }

    /* access modifiers changed from: package-private */
    public boolean needCheckWhiteList(int rateModeSpec) {
        if (!this.mWhiteListLoaded) {
            return false;
        }
        if (!RateModeSpec.sameWhiteListSeq(rateModeSpec, this.mWhiteListSequenec) || !RateModeSpec.hasSearchMask(rateModeSpec)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isSTViewAutoRate(int rateModeSpec) {
        if (!RateModeSpec.isOnList(rateModeSpec) || RateModeSpec.getOnListRate(rateModeSpec) != 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isAWTFixedRate(int rateModeSpec) {
        if (!RateModeSpec.isOnList(rateModeSpec) || RateModeSpec.getOnListRate(rateModeSpec) == 0) {
            return false;
        }
        return true;
    }

    public static class PowerModeSpec {
        private static final int BIT_2_SHIFT = 8;
        private static final int BIT_3_SHIFT = 16;
        private static final int BIT_MASK = 3;
        private static final int SEQ_MASK = -65536;
        private static final int USER_MASK = 32768;
        private static final int USER_RATE_MASK = 768;

        public static int setSettingsRate(int modeSpec, int rate) {
            return (modeSpec & -4) | (rate & 3);
        }

        public static int getSettingsRate(int modeSpec) {
            return modeSpec & 3;
        }

        public static int setUserChangeRate(int modeSpec, int rate) {
            return (modeSpec & -769) | ((rate << BIT_2_SHIFT) & USER_RATE_MASK) | USER_MASK;
        }

        public static int getUserChangeRate(int modeSpec) {
            if ((modeSpec & USER_MASK) != USER_MASK) {
                return -1;
            }
            return (modeSpec >> BIT_2_SHIFT) & 3;
        }

        public static int setStateSeq(int modeSpec, int seq) {
            return (65535 & modeSpec) | ((seq << BIT_3_SHIFT) & SEQ_MASK);
        }

        public static boolean sameStateSeq(int modeSpec, int seq) {
            return ((modeSpec >> BIT_3_SHIFT) & SEQ_MASK) == (SEQ_MASK & seq);
        }
    }

    public static class RateModeSpec {
        private static final int BIT_1_H_MASK = 240;
        private static final int BIT_1_H_SHIFT = 4;
        private static final int BIT_2_H_MASK = 61440;
        private static final int BIT_2_H_SHIFT = 12;
        private static final int BIT_2_L01_MASK = 768;
        private static final int BIT_2_L_MASK = 3840;
        private static final int BIT_2_L_SHIFT = 8;
        private static final int BIT_3_L0_MASK = 65536;
        private static final int BIT_4_SHIFT = 24;
        private static final int BIT_4__MASK = -16777216;
        private static final int BIT_MASK = 255;
        private static final int GAME_MASK = 2048;
        private static final int MODE_CHECKED = 128;
        private static final int MODE_MASK = 3;
        private static final int SEARCH_MASK = 1024;
        private static final int VIDEO_MASK = 131072;

        public static int setRefreshRate(int rateModeSpec, int rate) {
            return (rateModeSpec & -4) | (rate & 3);
        }

        public static int setSettingsRefreshRate(int rateModeSpec, int rate) {
            return (rateModeSpec & -241) | ((rate << 4) & BIT_1_H_MASK);
        }

        public static int setOnListRate(int rateModeSpec, int rate) {
            return (rateModeSpec & -3841) | ((rate << BIT_2_L_SHIFT) & BIT_2_L01_MASK) | SEARCH_MASK;
        }

        public static int setOnListGameRate(int rateModeSpec, int rate) {
            return (rateModeSpec & -3841) | ((rate << BIT_2_L_SHIFT) & BIT_2_L01_MASK) | SEARCH_MASK | GAME_MASK;
        }

        public static int setNotOnListRate(int rateModeSpec) {
            return (rateModeSpec & -3841) | SEARCH_MASK;
        }

        public static int setWhiteListSeq(int rateModeSpec, int seq) {
            return (16777215 & rateModeSpec) | ((seq << BIT_4_SHIFT) & BIT_4__MASK) | SEARCH_MASK;
        }

        public static int setSTViewRate(int rateModeSpec, int rate) {
            return (-61441 & rateModeSpec) | ((rate << BIT_2_H_SHIFT) & BIT_2_H_MASK);
        }

        public static int setTPListStatus(int rateModeSpec) {
            return BIT_3_L0_MASK | rateModeSpec;
        }

        public static boolean hasSearchMask(int rateModeSpec) {
            return (rateModeSpec & SEARCH_MASK) == SEARCH_MASK;
        }

        public static boolean sameWhiteListSeq(int rateModeSpec, int seq) {
            return ((rateModeSpec >> BIT_4_SHIFT) & BIT_MASK) == (seq & BIT_MASK);
        }

        public static boolean isOnList(int rateModeSpec) {
            return (rateModeSpec & BIT_2_L01_MASK) != 0;
        }

        public static boolean isGameOnList(int rateModeSpec) {
            return (rateModeSpec & GAME_MASK) != 0;
        }

        public static int setOnVideoListRate(int rateModeSpec, int rate) {
            return (rateModeSpec & -3841) | ((rate << BIT_2_L_SHIFT) & BIT_2_L01_MASK) | SEARCH_MASK | VIDEO_MASK;
        }

        public static boolean isVideoOnList(int rateModeSpec) {
            return (VIDEO_MASK & rateModeSpec) != 0;
        }

        public static int getRate(int rateModeSpec) {
            return rateModeSpec & 3;
        }

        public static int getAWTSettingsRate(int rateModeSpec) {
            return (rateModeSpec >> 4) & 3;
        }

        public static int getOnListRate(int rateModeSpec) {
            return (rateModeSpec >> BIT_2_L_SHIFT) & 3;
        }

        public static int getSTViewRate(int rateModeSpec) {
            return (rateModeSpec >> BIT_2_H_SHIFT) & 3;
        }

        public static boolean isOnTPList(int rateModeSpec) {
            return (rateModeSpec & BIT_3_L0_MASK) == BIT_3_L0_MASK;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCtsActivity(OppoBaseAppWindowToken token) {
        if (this.mCtsRatePackage.get(token.mActivityName) == null) {
            return false;
        }
        return true;
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri mAutoResolutionUri = Settings.Secure.getUriFor("app_auto_resolution");
        private final Uri mScreenRateUri = Settings.Secure.getUriFor("coloros_screen_refresh_rate");
        private final Uri mScreenResolutionUri = Settings.Secure.getUriFor("coloros_screen_resolution_adjust");
        private final Uri mSetupCompleteUri = Settings.Secure.getUriFor("user_setup_complete");

        public SettingsObserver(Handler handler) {
            super(handler);
            ContentResolver resolver = OppoScreenModeService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mScreenRateUri, false, this, -1);
            resolver.registerContentObserver(this.mScreenResolutionUri, false, this, -1);
            resolver.registerContentObserver(this.mSetupCompleteUri, false, this);
            if (!OppoScreenModeService.this.mResolutionSwitchEnable) {
                resolver.registerContentObserver(this.mAutoResolutionUri, false, this, -1);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                if (OppoScreenModeService.DEBUG_PANIC || OppoScreenModeService.DEBUG) {
                    Slog.d("ScreenMode", "onChange  uri=" + uri);
                }
                OppoScreenModeService.this.mHandler.removeMessages(2);
                if (this.mScreenRateUri.equals(uri)) {
                    int refreshRateSettings = Settings.Secure.getIntForUser(OppoScreenModeService.this.mContext.getContentResolver(), "coloros_screen_refresh_rate", OppoScreenModeService.this.mDefaultFreshRate, 0);
                    if (refreshRateSettings != OppoScreenModeService.this.mScreenRateSettings) {
                        if (refreshRateSettings < 0 || refreshRateSettings > OppoScreenModeService.this.MAX_REFRESH_RATE_MODE) {
                            refreshRateSettings = 0;
                        }
                        OppoScreenModeService.this.mScreenRateSettings = refreshRateSettings;
                        SystemProperties.set("sys.oppo.display.rate", StringUtils.EMPTY + OppoScreenModeService.this.mScreenRateSettings);
                        if (OppoScreenModeService.this.mCurrentAppToken != null) {
                            OppoScreenModeService oppoScreenModeService = OppoScreenModeService.this;
                            oppoScreenModeService.setRefreshRateInternal(oppoScreenModeService.mCurrentAppToken, OppoScreenModeService.this.mScreenRateSettings);
                        }
                    }
                } else if (this.mSetupCompleteUri.equals(uri)) {
                    OppoScreenModeService.this.initResolution();
                } else if (this.mScreenResolutionUri.equals(uri)) {
                    OppoScreenModeService.this.setResolution();
                } else if (this.mAutoResolutionUri.equals(uri)) {
                    OppoScreenModeService.this.autoResolutionChange();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void restoreStateIfPossible() {
        int modeSpec = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screenmode_powersavemode", 0, 0);
        if (this.mScreenRateSettings == 2 && modeSpec != 0) {
            int rate = PowerModeSpec.getSettingsRate(modeSpec);
            int userRate = PowerModeSpec.getUserChangeRate(modeSpec);
            if (DEBUG || DEBUG_PANIC) {
                Slog.d("ScreenMode", " restore rate=" + rate + ",userRate=" + userRate);
            }
            if (rate != 2) {
                Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "coloros_screen_refresh_rate", rate, 0);
            }
        }
        if (modeSpec != 0) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "screenmode_powersavemode", 0, 0);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v10, types: [int] */
    /* JADX WARN: Type inference failed for: r6v14 */
    /* JADX WARN: Type inference failed for: r6v15 */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void init(WindowManagerService service, Context context, boolean fpsSupport, boolean resolutionSupport, boolean switchEnable) {
        boolean z;
        ? r6;
        this.mContext = context;
        this.mService = service;
        this.mDynamicFPSSupport = fpsSupport;
        this.mResolutionSupport = resolutionSupport;
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        this.mDisplay = this.mDisplayManager.getDisplay(0);
        this.mDisplayContent = this.mService.getDefaultDisplayContentLocked();
        this.mResolutionSwitchEnable = switchEnable;
        initDensityList();
        Display.Mode[] modes = this.mDisplay.getSupportedModes();
        this.mModeRecords = new ModeRecord[modes.length];
        int totalResolution = 0;
        for (int i = 0; i < modes.length; i++) {
            this.mModeRecords[i] = new ModeRecord(modes[i]);
            int rate = this.mModeRecords[i].rateId;
            int resolution = this.mModeRecords[i].resolutionId;
            totalResolution += resolution;
            this.mParamsMap.put(Integer.valueOf((rate << 4) | resolution), this.mModeRecords[i]);
            this.mModeRecordMap.put(Integer.valueOf(modes[i].getModeId()), this.mModeRecords[i]);
            if (rate == 3) {
                this.mRefreshRateFullspeed = 3;
            } else if (rate == 1 && this.mRefreshRateFullspeed < 3) {
                this.mRefreshRateFullspeed = 1;
            }
            if (this.mScreenWidth < this.mModeRecords[i].mode.getPhysicalWidth()) {
                this.mScreenWidth = this.mModeRecords[i].mode.getPhysicalWidth();
            }
            if (DEBUG) {
                Slog.i("ScreenMode", "modeId " + modes[i]);
            }
        }
        if (this.mRefreshRateFullspeed == 3) {
            this.MAX_REFRESH_RATE_MODE = 3;
        }
        this.mDefaultMode = new ModeRecord(this.mDisplay.getMode());
        if (DEBUG || DEBUG_PANIC) {
            Slog.i("ScreenMode", "default modeId " + this.mDefaultMode.mode);
        }
        if (totalResolution == this.mDefaultMode.resolutionId * modes.length) {
            this.mResolutionSwitchEnable = false;
            int autoResolution = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "app_auto_resolution", 0, 0);
            sIsResolutionAuto = 1 == autoResolution;
            this.mResolutionSettings = this.mDefaultMode.resolutionId;
            if (autoResolution == 0) {
                z = true;
                z = true;
                if (SystemProperties.getInt("ro.oppo.appautoresolution.default", 0) == 1) {
                    Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "app_auto_resolution", 1, 0);
                    sIsResolutionAuto = true;
                }
            } else {
                z = true;
            }
        } else {
            z = true;
        }
        if (context.getPackageManager().hasSystemFeature(DEFAULT_RATE_FEATURE)) {
            r6 = 0;
            this.mDefaultFreshRate = 0;
        } else {
            r6 = 0;
            this.mDefaultFreshRate = this.mRefreshRateFullspeed;
        }
        this.mScreenRateSettings = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "coloros_screen_refresh_rate", this.mDefaultFreshRate, r6);
        this.mHandler = new ScreenModeHandler();
        this.mCurrentModeId = getModeRecord(this.mScreenRateSettings, this.mDefaultMode.resolutionId).mode.getModeId();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        initResolution();
        initCtsRateList();
        initReceiver();
        this.mRUSHelper = new OppoScreenModeHelper(context, this, this.mRefreshRateFullspeed);
        this.mRUSHelper.initUpdateBroadcastReceiver();
        this.mOppoScreenCompat = OppoScreenCompat.getInstance();
        this.mOppoScreenCompat.init(service, context, resolutionSupport, this.mScreenWidth);
        OppoScreenCompat oppoScreenCompat = this.mOppoScreenCompat;
        if (this.mResolutionSettings == 2) {
            z = r6;
        }
        oppoScreenCompat.setForceFhd(z);
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
        SystemProperties.set("sys.oppo.display.rate", StringUtils.EMPTY + this.mScreenRateSettings);
        publish();
    }

    /* access modifiers changed from: package-private */
    public void initDensityList() {
        if (this.FHD_DPI_ZOOM.length() != 0) {
            String[] densityFhd = this.FHD_DPI_ZOOM.split(",");
            String[] densityQhd = this.QHD_DPI_ZOOM.split(",");
            if (densityFhd != null) {
                try {
                    DPI_FHD = new int[densityFhd.length];
                    for (int i = 0; i < densityFhd.length; i++) {
                        DPI_FHD[i] = Integer.parseInt(densityFhd[i]);
                        if (DEBUG) {
                            Slog.d("ScreenMode", "initDensityList  DPI_FHD[" + i + "] = " + DPI_FHD[i]);
                        }
                    }
                } catch (Exception ex) {
                    Slog.d("ScreenMode", " initDensityList error " + ex);
                    return;
                }
            }
            if (densityQhd != null) {
                DPI_QHD = new int[densityQhd.length];
                for (int i2 = 0; i2 < densityQhd.length; i2++) {
                    DPI_QHD[i2] = Integer.parseInt(densityQhd[i2]);
                    if (DEBUG) {
                        Slog.d("ScreenMode", "initDensityList  DPI_QHD[" + i2 + "] = " + DPI_QHD[i2]);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void initResolution() {
        boolean setupComplete = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1) {
            setupComplete = true;
        }
        if (!setupComplete || !this.mResolutionSupport || !this.mResolutionSwitchEnable) {
            Slog.e("ScreenMode", "initResolution  complete?" + setupComplete + ",support?" + this.mResolutionSupport);
            return;
        }
        setResolution();
        setDensity();
    }

    /* access modifiers changed from: package-private */
    public void initReceiver() {
        if (sPkgReceiver == null) {
            sPkgReceiver = new BroadcastReceiver() {
                /* class com.android.server.wm.OppoScreenModeService.AnonymousClass2 */
                int systemDisplayMode = -1;
                boolean systemResolutionAuto = false;

                public void onReceive(Context context, Intent intent) {
                    try {
                        String action = intent.getAction();
                        context.getContentResolver();
                        Uri data = intent.getData();
                        if (data != null) {
                            String pkgName = data.getSchemeSpecificPart();
                            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                                if ("com.android.compatibility.common.deviceinfo".equals(pkgName) && OppoScreenModeService.this.mScreenRateSettings != 2) {
                                    this.systemDisplayMode = OppoScreenModeService.this.mScreenRateSettings;
                                    OppoScreenModeService.this.mScreenRateSettings = 2;
                                    this.systemResolutionAuto = OppoScreenModeService.sIsResolutionAuto;
                                    OppoScreenModeService.sIsResolutionAuto = false;
                                }
                            } else if ("com.android.tradefed.utils.wifi".equals(pkgName) && this.systemDisplayMode != -1) {
                                OppoScreenModeService.this.mScreenRateSettings = this.systemDisplayMode;
                                OppoScreenModeService.sIsResolutionAuto = this.systemResolutionAuto;
                            }
                        }
                    } catch (Exception ex) {
                        Slog.w("ScreenMode", "sPkgReceiver error.", ex);
                    }
                }
            };
            IntentFilter pkgFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            pkgFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            pkgFilter.addDataScheme("package");
            this.mContext.registerReceiver(sPkgReceiver, pkgFilter);
        }
    }

    public void setCurrentUser(int userId) {
        Slog.d("ScreenMode", " setCurrentUser.userId=" + userId);
    }

    /* access modifiers changed from: private */
    public void onUserSwitched() {
        this.mScreenRateSettings = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "coloros_screen_refresh_rate", this.mDefaultFreshRate, 0);
        SystemProperties.set("sys.oppo.display.rate", StringUtils.EMPTY + this.mScreenRateSettings);
        setResolution();
        this.mService.requestTraversal();
    }

    private void setDensity() {
        if (!this.mResolutionSupport) {
            Slog.e("ScreenMode", "setDensity support?" + this.mResolutionSupport);
            return;
        }
        int srcDensity = this.mService.getBaseDisplayDensity(0);
        int dstDensity = getDensityForResolution(srcDensity);
        Slog.i("ScreenMode", "init dstDensity=" + dstDensity + ",srcDensity=" + srcDensity);
        if (dstDensity != srcDensity) {
            this.mService.setForcedDisplayDensityForUser(0, dstDensity, -2);
        }
    }

    /* access modifiers changed from: package-private */
    public void autoResolutionChange() {
        if (this.mResolutionSupport && !this.mResolutionSwitchEnable) {
            int autoResolution = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "app_auto_resolution", 0, 0);
            Slog.i("ScreenMode", " autoResolution " + autoResolution);
            boolean z = true;
            if (autoResolution != 1) {
                z = false;
            }
            sIsResolutionAuto = z;
            forceStopAppsResolutionChange(false);
            this.mService.requestTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public void setResolution() {
        ModeRecord[] modeRecordArr;
        if (this.mResolutionSupport && this.mResolutionSwitchEnable) {
            boolean z = false;
            int resolutionSettings = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "coloros_screen_resolution_adjust", 1, 0);
            if (DEBUG) {
                Slog.i("ScreenMode", "setResolution resolutionSettings " + resolutionSettings);
            }
            int i = this.mResolutionSettings;
            if (i != resolutionSettings && resolutionSettings >= 1 && resolutionSettings <= 3) {
                boolean autoQhd = false;
                if (i + resolutionSettings == 4) {
                    autoQhd = true;
                }
                int defaultDensity2 = this.FHD_DENSITY;
                if (resolutionSettings != 2) {
                    defaultDensity2 = this.mService.getInitialDisplayDensity(0);
                }
                SystemProperties.set("persist.sys.display.density", StringUtils.EMPTY + defaultDensity2);
                this.mResolutionSettings = resolutionSettings;
                this.mService.requestTraversal();
                Slog.i("ScreenMode", "setResolution mResolutionSettings " + this.mResolutionSettings + "," + defaultDensity2);
                boolean isAutoMode = resolutionSettings == 1;
                OppoScreenCompat oppoScreenCompat = this.mOppoScreenCompat;
                if (oppoScreenCompat != null) {
                    oppoScreenCompat.setForceFhd(this.mResolutionSettings != 2);
                }
                ModeRecord[] modeRecordArr2 = this.mModeRecords;
                if (modeRecordArr2 != null && modeRecordArr2.length > 1 && (autoQhd || !DEBUG_DISABLE_FORCE_STOP)) {
                    if (!autoQhd) {
                        z = true;
                    }
                    forceStopAppsResolutionChange(z);
                }
                if (DEBUG) {
                    Slog.i("ScreenMode", "current resolution settings:" + this.mResolutionSettings);
                }
                if (this.DEBUG_START_EMPTY) {
                    this.mHandler.removeMessages(9);
                    this.mHandler.sendEmptyMessageDelayed(9, 500);
                }
                if (!(isAutoMode == sIsResolutionAuto || (modeRecordArr = this.mModeRecords) == null || modeRecordArr.length <= 1)) {
                    sIsResolutionAuto = isAutoMode;
                }
                this.mService.requestTraversal();
            }
        }
    }

    private void forceStopAppsResolutionChange(boolean killAll) {
        try {
            Slog.d("ScreenMode", " force stop all recent app killAll =" + killAll);
            HashMap<Integer, StopAppInfo> appMap = new HashMap<>(100);
            int myPid = Process.myPid();
            getforceStopApps(appMap);
            for (StopAppInfo appInfo : appMap.values()) {
                if (!this.mNoKillList.contains(appInfo.mPkg) && appInfo.mPid != myPid && (this.mOppoScreenCompat == null || !this.mOppoScreenCompat.notForceStop(appInfo.mPkg))) {
                    try {
                        if (this.mOppoScreenCompat != null && this.mOppoScreenCompat.needForceStop(appInfo.mPkg)) {
                            if (DEBUG || DEBUG_PANIC) {
                                Slog.d("ScreenMode", " force stop " + appInfo);
                            }
                            this.mService.mActivityManager.forceStopPackage(appInfo.mPkg, -1);
                        } else if (killAll) {
                            for (int i = 0; i < appInfo.mPidList.size(); i++) {
                                int pid = appInfo.mPidList.get(i).intValue();
                                if (Process.getThreadGroupLeader(pid) == pid) {
                                    Process.killProcess(pid);
                                    if (DEBUG || DEBUG_PANIC) {
                                        Slog.d("ScreenMode", " kill pid=" + pid + "," + appInfo);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Slog.d("ScreenMode", " kill app fail appInfo= " + appInfo);
                    }
                } else if (DEBUG || DEBUG_PANIC) {
                    Slog.d("ScreenMode", " not need kill " + appInfo);
                }
            }
        } catch (Exception ex) {
            Slog.w("ScreenMode", "fail to stoppackage  ", ex);
        }
        try {
            ((WindowManagerService) this.mService).mAtmInternal.cleanupRecentTasksForUser(-1);
        } catch (Exception e2) {
            Slog.d("ScreenMode", "Failed to remove all tasks", e2);
        }
    }

    private void getforceStopApps(HashMap<Integer, StopAppInfo> appMap) {
        try {
            SparseArray<WindowProcessController> pidMap = this.mService.mAtmService.mProcessMap.getPidMap();
            for (int i = pidMap.size() - 1; i >= 0; i--) {
                int pid = pidMap.keyAt(i);
                WindowProcessController app = pidMap.get(pid);
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" process info pid=");
                    sb.append(pid);
                    sb.append(",pkg=");
                    sb.append(app.mInfo.packageName);
                    sb.append(",uid = ");
                    sb.append(app.mUid);
                    sb.append(",is app ");
                    sb.append(UserHandle.getAppId(app.mUid) >= 10000);
                    sb.append(",ProcState=");
                    sb.append(app.getCurrentProcState());
                    Slog.d("ScreenMode", sb.toString());
                }
                if (UserHandle.getAppId(app.mUid) >= 10000 && app.mInfo.packageName != null) {
                    StopAppInfo stopAppInfo = appMap.get(Integer.valueOf(app.mUid));
                    if (stopAppInfo == null) {
                        stopAppInfo = new StopAppInfo(app.mInfo.packageName, pid, app.mUid, false);
                        appMap.put(Integer.valueOf(app.mUid), stopAppInfo);
                    }
                    stopAppInfo.mPidList.add(Integer.valueOf(pid));
                }
            }
        } catch (Exception ex) {
            Slog.w("ScreenMode", " get process info error  ", ex);
        }
    }

    private StopAppInfo getSameProcess(ArrayList<StopAppInfo> appList, String pkg, int uid) {
        for (int i = 0; i < appList.size(); i++) {
            StopAppInfo appInfo = appList.get(i);
            if ((uid == appInfo.mUid || uid == -1) && appInfo.mPkg.equals(pkg)) {
                return appInfo;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void initCtsRateList() {
        this.mFixedRatePackage.put("android.view.cts/.DisplayRefreshRateCtsActivity", 2);
        this.mCtsRatePackage.put("android.view.cts/.DisplayRefreshRateCtsActivity", 2);
        initLocalRateList(this.mRefreshRateFullspeed);
        initLocalRateList(2);
    }

    /* access modifiers changed from: package-private */
    public void initLocalRateList(int rate) {
    }

    @Override // com.android.server.wm.OppoScreenModeHelper.Callback
    public void onDataChange() {
        this.mListOptimized = false;
        this.mWhiteListLoaded = true;
        this.mWhiteListSequenec++;
        if (DEBUG || DEBUG_PANIC) {
            Slog.d("ScreenMode", "onDataChange called " + this.mWhiteListLoaded + ",seq=" + this.mWhiteListSequenec);
        }
    }

    private class ModeRecord {
        Display.Mode mode;
        int rateId = -1;
        int resolutionId = -1;
        int[] sDpiList = new int[0];

        ModeRecord(Display.Mode mode2) {
            this.mode = mode2;
            if (mode2.getPhysicalWidth() == OppoScreenModeService.WIDTH_2K) {
                this.resolutionId = 3;
                this.sDpiList = OppoScreenModeService.DPI_QHD;
            } else if (mode2.getPhysicalWidth() == OppoScreenModeService.WIDTH_FHD) {
                this.resolutionId = 2;
                if ((OppoScreenModeService.this.deviceName == null || !OppoScreenModeService.this.deviceName.contains("guacamole")) && OppoScreenModeService.this.defaultDensity > 480) {
                    this.sDpiList = OppoScreenModeService.DPI_FHD;
                } else {
                    this.sDpiList = OppoScreenModeService.DPI_FHD;
                }
            }
            if (((double) Math.abs(mode2.getRefreshRate() - 60.0f)) < 0.1d) {
                this.rateId = 2;
            } else if (((double) Math.abs(mode2.getRefreshRate() - 90.0f)) < 0.1d) {
                this.rateId = 1;
            } else if (((double) Math.abs(mode2.getRefreshRate() - 120.0f)) < 0.1d) {
                this.rateId = 3;
            }
        }

        public String toString() {
            return " record rateId =" + this.rateId + ",resolutionId=" + this.resolutionId + ",mode =" + this.mode;
        }
    }

    /* access modifiers changed from: private */
    public void startEmptyMMActivity() {
        try {
            ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfo(this.WEIXIN_PKG_NAME, 0);
            if (appInfo != null) {
                ModeRecord modeR = new ModeRecord(this.mDisplay.getMode());
                ModeRecord curModeR = this.mModeRecordMap.get(Integer.valueOf(this.mCurrentModeId));
                if (DEBUG) {
                    Slog.d("ScreenMode", "startMMEmptyActivity appInfo=" + appInfo + ",mode=" + modeR + ",curMode=" + curModeR);
                }
                if (curModeR != null) {
                    if (curModeR.resolutionId != modeR.resolutionId) {
                        this.mHandler.removeMessages(9);
                        this.mHandler.sendEmptyMessageDelayed(9, 500);
                        return;
                    }
                    ArrayList<String> apps_l = new ArrayList<>();
                    apps_l.add(this.WEIXIN_PKG_NAME);
                    Bundle bParams = new Bundle();
                    bParams.putStringArrayList("start_empty_apps", apps_l);
                    requestStartActivityAsUserEmpty(bParams);
                }
            }
        } catch (Exception ex) {
            Slog.d("ScreenMode", " error  ", ex);
        }
    }

    /* access modifiers changed from: private */
    public void showToast(String message) {
        Toast.makeText(this.mContext, message, 0).show();
    }

    public class ScreenModeHandler extends Handler {
        private static final int MSG_REQUEST_UPDATE = 1;
        private static final int MSG_RESET_ANIMATION_STATE = 8;
        private static final int MSG_SHOW_TOAST = 16;
        private static final int MSG_START_MM_EMPTY = 9;
        private static final int MSG_UPDATE_DENSITY = 5;
        private static final int MSG_UPDATE_REFRESH_RATE = 2;
        private static final int MSG_UPDATE_RESOLUTION = 3;
        private static final int MSG_UPDATE_SCENARIO = 4;
        private static final int MSG_UPDATE_TP = 7;
        private static final int MSG_USER_SWITCHED = 6;

        public ScreenModeHandler() {
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != MSG_SHOW_TOAST) {
                switch (i) {
                    case 1:
                        OppoScreenModeService.this.mService.requestTraversal();
                        return;
                    case 2:
                        OppoScreenModeService.this.updateRefreshRate((OppoBaseAppWindowToken) msg.obj);
                        return;
                    case 3:
                    default:
                        return;
                    case 4:
                        OppoScreenModeService.this.updateScenario(msg.arg1);
                        return;
                    case MSG_UPDATE_DENSITY /*{ENCODED_INT: 5}*/:
                        Settings.Secure.putIntForUser(OppoScreenModeService.this.mContext.getContentResolver(), "oppo_screen_resolution_backup", OppoScreenModeService.this.mResolutionSettings, msg.arg2);
                        if (OppoScreenModeService.DEBUG || OppoScreenModeService.DEBUG_PANIC) {
                            Slog.d("ScreenMode", " density " + msg.arg1 + ",resolution=" + OppoScreenModeService.this.mResolutionSettings);
                            return;
                        }
                        return;
                    case MSG_USER_SWITCHED /*{ENCODED_INT: 6}*/:
                        OppoScreenModeService.this.onUserSwitched();
                        return;
                    case MSG_UPDATE_TP /*{ENCODED_INT: 7}*/:
                        OppoScreenModeService.this.transmitData(msg.arg1);
                        return;
                    case MSG_RESET_ANIMATION_STATE /*{ENCODED_INT: 8}*/:
                        synchronized (OppoScreenModeService.this.mAnimatingLock) {
                            boolean unused = OppoScreenModeService.this.mInAnimating = false;
                            boolean unused2 = OppoScreenModeService.this.mActivityChanging = false;
                        }
                        OppoScreenModeService.this.mService.requestTraversal();
                        if (OppoScreenModeService.DEBUG || OppoScreenModeService.DEBUG_PANIC) {
                            Slog.d("ScreenMode", " reset mInAnimating = false");
                            return;
                        }
                        return;
                    case MSG_START_MM_EMPTY /*{ENCODED_INT: 9}*/:
                        OppoScreenModeService.this.startEmptyMMActivity();
                        return;
                }
            } else {
                OppoScreenModeService.this.showToast(OppoScreenModeService.this.mContext.getResources().getString(201653646));
            }
        }
    }

    class StopAppInfo {
        boolean mHasUI = false;
        int mPid = -1;
        ArrayList<Integer> mPidList = new ArrayList<>();
        String mPkg = StringUtils.EMPTY;
        int mUid = -1;

        StopAppInfo(String pkg, int pid, int uid, boolean hasUI) {
            this.mPkg = pkg;
            this.mPid = pid;
            this.mUid = uid;
            this.mHasUI = hasUI;
        }

        public String toString() {
            return "pkg=" + this.mPkg + ",uid=" + this.mUid + ", pid=" + this.mPid;
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, "ScreenMode", pw)) {
            if (args.length > 0 && "log".equals(args[0])) {
                dynamicallyConfigLogTag(pw, args);
            } else if (args.length <= 0 || !"rate".equals(args[0])) {
                if (args.length == 2 && "startempty".equals(args[0])) {
                    this.DEBUG_START_EMPTY = "1".equals(args[1]);
                }
                pw.println("   mScreenRateSettings:" + this.mScreenRateSettings + "[auto -0;90HZ-1;60hz-2;120HZ-3]");
                StringBuilder sb = new StringBuilder();
                sb.append("   mHighTemperatureRate:");
                sb.append(this.mHighTemperatureRate);
                pw.println(sb.toString());
                pw.println("   mDefaultFreshRate:" + this.mDefaultFreshRate);
                pw.println("   mMEMCRequestRate:" + this.mMEMCRequestRate);
                pw.println("   sIsResolutionAuto:" + sIsResolutionAuto);
                pw.println("   mResolutionSettings:" + this.mResolutionSettings);
                Display.Mode[] modes = this.mDisplay.getSupportedModes();
                for (int i = 0; i < modes.length; i++) {
                    pw.println("SupportedModes: " + modes[i]);
                }
                pw.println("mDisplay.getMode()  " + this.mDisplay.getMode());
                pw.println("mCurrentModeId  " + this.mCurrentModeId);
                pw.println("mDynamicFPSSupport  " + this.mDynamicFPSSupport);
                pw.println("mResolutionSupport=" + this.mResolutionSupport);
                pw.println("mWhiteListLoaded=" + this.mWhiteListLoaded);
                pw.println("mWhiteListSequenec=" + this.mWhiteListSequenec);
                pw.println("mCurrentTPStatus=" + this.mCurrentTPStatus);
                pw.println("mRefreshRateFullspeed=" + this.mRefreshRateFullspeed);
                pw.println("MAX_REFRESH_RATE_MODE=" + this.MAX_REFRESH_RATE_MODE);
                pw.println("mHDMIDisplayAdded=" + this.mHDMIDisplayAdded);
                pw.println("isInDCAndLowBrightnessMode=" + this.isInDCAndLowBrightnessMode);
                pw.println("isInPSMode=" + this.isInPSMode);
                pw.println("mPSModeRate=" + this.mPSModeRate);
                pw.println("mActivityChanging=" + this.mActivityChanging);
                pw.println("mInAnimating=" + this.mInAnimating);
                pw.println("DEBUG_START_EMPTY=" + this.DEBUG_START_EMPTY);
                pw.println("game rate =" + this.mGameRequestRefreshRate);
                pw.println("video request rate =" + this.mLastVideoRefreshRate);
                pw.println("screen width =" + this.mScreenWidth);
                pw.println("resolution switch enable =" + this.mResolutionSwitchEnable);
            } else if (args.length == 3) {
                String tag = args[1];
                boolean on = "1".equals(args[2]);
                if ("force".equals(tag)) {
                    DEBUG_FORCE_REFRESH_RATE = on;
                }
            }
        }
    }

    public void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        pw.println("dynamicallyConfigLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]: " + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = "1".equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag: " + tag + ", on: " + on);
        if ("all".equals(tag)) {
            DEBUG = on;
            this.mOppoScreenCompat.openLog(on);
        }
    }

    public void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1. open all log in VibratorService");
        pw.println("cmd: dumpsys vibrator log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    private OppoBaseAppWindowToken typeCasting(AppWindowToken token) {
        if (token != null) {
            return (OppoBaseAppWindowToken) ColorTypeCastingHelper.typeCasting(OppoBaseAppWindowToken.class, token);
        }
        return null;
    }

    private OppoActivityManagerInternal getAmInternal() {
        if (this.mAmInternal == null) {
            this.mAmInternal = (OppoActivityManagerInternal) LocalServices.getService(OppoActivityManagerInternal.class);
        }
        return this.mAmInternal;
    }

    private int requestStartActivityAsUserEmpty(Bundle options) {
        if (getAmInternal() != null) {
            return getAmInternal().startActivityAsUserEmpty(options);
        }
        return -1;
    }
}
