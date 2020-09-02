package com.android.server.wm;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import com.android.internal.policy.PhoneWindow;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.startingwindow.ColorAppStartingSnapshotCache;
import com.android.server.wm.startingwindow.ColorAppStartingSnapshotLoader;
import com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister;
import com.android.server.wm.startingwindow.ColorStartingSurfaceRemoveRunnable;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import com.android.server.wm.startingwindow.ColorStartingWindowUtils;
import com.color.zoomwindow.ColorZoomWindowManager;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColorStartingWindowManager implements IColorStartingWindowManager {
    private static final Object LOCK = new Object();
    private boolean mAllowAppSnapshot = true;
    private int mAppTransit = -1;
    private boolean mIsStartingAppShotcut = false;
    private boolean mRequestStoreAppTokenSnapshot = false;
    /* access modifiers changed from: private */
    public ColorAppStartingSnapshotCache mSnapshotCache;
    private ColorAppStartingSnapshotLoader mSnapshotLoader;
    private ColorAppStartingSnapshotPersister mSnapshotPersister;
    private Runnable mStartingSurfaceRunnable;
    private int mStartingWindowFlags = 0;
    private boolean mSupportSnapshotPreview = true;
    private boolean mSupportSnapshotSplashForCallingApp = false;
    private ExecutorService mThreadPool;
    private boolean mUseSplashForWeChatProcessRunning = true;
    /* access modifiers changed from: private */
    public WindowManagerService mWMService;

    ColorStartingWindowManager() {
    }

    private static class Holder {
        /* access modifiers changed from: private */
        public static final ColorStartingWindowManager INSTANCE = new ColorStartingWindowManager();

        private Holder() {
        }
    }

    public static ColorStartingWindowManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(WindowManagerService wms) {
        this.mWMService = wms;
        checkFeature();
        if (this.mSupportSnapshotPreview) {
            this.mSnapshotPersister = new ColorAppStartingSnapshotPersister($$Lambda$DuM3fJ7DDorCTGUoF_TMwVpActg.INSTANCE);
            this.mSnapshotLoader = new ColorAppStartingSnapshotLoader(this.mSnapshotPersister);
            this.mSnapshotCache = new ColorAppStartingSnapshotCache(this.mWMService, this.mSnapshotLoader);
            this.mSnapshotPersister.start();
            ColorStartingWindowRUSHelper.getInstance().init();
        }
    }

    private void checkFeature() {
        WindowManagerService windowManagerService = this.mWMService;
        if (windowManagerService != null && windowManagerService.mContext != null) {
            ColorStartingWindowUtils.logD("checkFeature ==> mSupportSnapshotPreview =:" + this.mSupportSnapshotPreview);
        }
    }

    private void checkThreadPoolNotNull() {
        if (this.mThreadPool == null) {
            this.mThreadPool = Executors.newSingleThreadExecutor();
        }
    }

    public boolean handleDestroySurfaces(String packageName, int type) {
        if (!this.mSupportSnapshotPreview || !checkSplashWindowFlag() || type != 3 || !ColorStartingWindowContants.STARTING_DELAY_MAP.containsKey(packageName)) {
            return false;
        }
        ColorStartingWindowUtils.logD("handleDestroySurfaces packageName =:" + packageName + ",type =:" + type);
        return true;
    }

    public void clearCacheWhenOnConfigurationChange(int changes) {
        if (this.mSupportSnapshotPreview) {
            ColorStartingWindowUtils.logBackTrace("clearCacheWhenOnConfigurationChange");
            reset();
            if ((changes & 512) != 0) {
                clearCache();
                ColorStartingWindowUtils.logD("clearCache by Dark Mode");
            } else if ((changes & 4) != 0) {
                clearCache();
                ColorStartingWindowUtils.logD("clearCache by Locale");
            } else if ((changes & 8192) != 0) {
                clearCache();
                ColorStartingWindowUtils.logD("clearCache by LayoutDirection");
            }
        }
    }

    private void clearCache() {
        synchronized (LOCK) {
            this.mSnapshotCache.clearCache();
            this.mSnapshotPersister.clearCache(this.mWMService.mCurrentUserId);
        }
    }

    public boolean interceptRemoveStartingWindow(String packageName, Handler handler, WindowManagerPolicy.StartingSurface surface) {
        Long time;
        if (!this.mSupportSnapshotPreview || !checkSplashWindowFlag() || (time = ColorStartingWindowContants.STARTING_DELAY_MAP.get(packageName)) == null) {
            return false;
        }
        long delayTime = time.longValue();
        ColorStartingWindowUtils.logD("interceptRemoveStartingWindow->packageName:" + packageName + ",delayTime:" + delayTime);
        handler.removeCallbacks(this.mStartingSurfaceRunnable);
        this.mStartingSurfaceRunnable = new ColorStartingSurfaceRemoveRunnable(surface);
        handler.postDelayed(this.mStartingSurfaceRunnable, delayTime);
        return true;
    }

    public int getStartingWindowType(int defaultTypeNone, int defaultTypeSplash, int defaultTypeSnapshot) {
        int i = this.mStartingWindowFlags;
        if (i == 268435456 || i == 536870912) {
            return defaultTypeSplash;
        }
        if (i != 1073741824) {
            return -1;
        }
        return defaultTypeNone;
    }

    public void reviseWindowFlagsForStarting(ActivityRecord activityRecord, boolean newTask, boolean taskSwitch, boolean processRunning, boolean fromRecents, boolean activityCreated) {
        ColorStartingWindowUtils.logD("reviseWindowFlagsForStarting activityRecord =:" + activityRecord + "\nmSupportSnapshotPreview =:" + this.mSupportSnapshotPreview);
        if (this.mSupportSnapshotPreview) {
            if (!checkSplashWindowFlag()) {
                this.mStartingWindowFlags = 0;
            }
            this.mRequestStoreAppTokenSnapshot = false;
            this.mSupportSnapshotSplashForCallingApp = false;
            if (this.mIsStartingAppShotcut) {
                ColorStartingWindowUtils.logD("if starting app is shotcut,we don't use splash window");
                this.mIsStartingAppShotcut = false;
            } else if (!fromRecents && !activityCreated && activityRecord != null && activityRecord.mAppWindowToken != null) {
                long start = System.currentTimeMillis();
                if (activityRecord.mStackSupervisor != null && activityRecord.mStackSupervisor.getKeyguardController().isKeyguardLocked()) {
                    ColorStartingWindowUtils.logD("keyguard locked,we don't revise window flags");
                } else if (activityRecord.getActivityStack() == null || !activityRecord.getActivityStack().isHomeOrRecentsStack()) {
                    Task task = activityRecord.mAppWindowToken.getTask();
                    if (task != null) {
                        if (task.getConfiguration() == null || task.getConfiguration().orientation != 2) {
                            String callAppPackageName = activityRecord.launchedFromPackage;
                            this.mSupportSnapshotSplashForCallingApp = ColorStartingWindowUtils.supportSnapshotSplashForCallingApp(callAppPackageName);
                            ColorStartingWindowUtils.logD("the app is starting from launcher =:" + this.mSupportSnapshotSplashForCallingApp + ",callAppPackageName =:" + callAppPackageName);
                            if (this.mSupportSnapshotSplashForCallingApp) {
                                if (ColorStartingWindowUtils.isSplashBlackPackageStartFromLauncher(activityRecord.packageName)) {
                                    ColorStartingWindowUtils.logD("black package,we don't revise window flags ");
                                } else if (ColorStartingWindowUtils.isSplashBlackTokenStartFromLauncher(activityRecord.shortComponentName)) {
                                    ColorStartingWindowUtils.logD("black token,we don't revise window flags ");
                                } else if (activityRecord.inSplitScreenWindowingMode()) {
                                    ColorStartingWindowUtils.logD("if activityRecord is in split mode,we don't revise window flags");
                                } else if (!newTask && !taskSwitch) {
                                } else {
                                    if (activityRecord.pendingOptions == null || activityRecord.pendingOptions.getLaunchWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                                        if (ColorStartingWindowContants.WECHAT_PACKAGE_NAME.equals(activityRecord.packageName)) {
                                            if (!processRunning) {
                                                this.mUseSplashForWeChatProcessRunning = false;
                                            } else if (this.mUseSplashForWeChatProcessRunning) {
                                                ColorStartingWindowUtils.logD("for we chat first start after reboot,we add splash");
                                                this.mUseSplashForWeChatProcessRunning = false;
                                            } else {
                                                ColorStartingWindowUtils.logD("for we chat process running,we don't add splash");
                                                return;
                                            }
                                        } else if (processRunning && ColorStartingWindowUtils.blockSplashSnapshotWhenProcessRunning(activityRecord.packageName)) {
                                            ColorStartingWindowUtils.logD("for special app starting from launcher,if processRunning,we don't use splash window");
                                            return;
                                        }
                                        if (ColorStartingWindowUtils.forceClearSplashWindowForPackage(activityRecord.packageName)) {
                                            ColorStartingWindowUtils.logD("special app ,we force clear it's splash window");
                                            this.mStartingWindowFlags = 1073741824;
                                        } else {
                                            ColorStartingWindowUtils.logD("for app starting from launcher,we use splash window to speed up apptransition ready");
                                            this.mStartingWindowFlags = 268435456;
                                            this.mRequestStoreAppTokenSnapshot = true;
                                        }
                                        ColorStartingWindowUtils.logD("reviseWindowFlagsForStarting spend time = :" + (System.currentTimeMillis() - start));
                                        return;
                                    }
                                    this.mStartingWindowFlags = 536870912;
                                    ColorStartingWindowUtils.logD("when launch in zoom stack ,SUGGEST use splash window");
                                }
                            } else if (ColorStartingWindowUtils.isSplashBlackPackageForSystemApp(activityRecord.packageName)) {
                                ColorStartingWindowUtils.logD("black system app ,we don't revise window flags ");
                            } else if (ColorStartingWindowUtils.forceClearSplashWindowForToken(activityRecord.shortComponentName)) {
                                this.mStartingWindowFlags = 1073741824;
                                ColorStartingWindowUtils.logD("system app which should force clear startingwindow");
                            } else if (ColorStartingWindowUtils.isSplashBlackTokenForSystemApp(activityRecord.shortComponentName)) {
                                ColorStartingWindowUtils.logD("system app black token,we don't revise window flags");
                            } else if (!ColorStartingWindowUtils.isSystemApp(activityRecord.packageName)) {
                            } else {
                                if (activityRecord.mAppWindowToken.getDisplayContent().mAppTransition.getAppTransition() == 20) {
                                    ColorStartingWindowUtils.logD("for transit keyguard going away,we don't revise window flags");
                                    return;
                                }
                                ColorStartingWindowUtils.logD("for system apps we use splash window to speed up apptransition ready");
                                this.mStartingWindowFlags = 536870912;
                            }
                        } else {
                            ColorStartingWindowUtils.logD("this activity is landscape,we don't revise starting window flags");
                        }
                    }
                } else {
                    ColorStartingWindowUtils.logD("this activity is in home stack,we don't revise starting window flags");
                }
            }
        }
    }

    public boolean checkSplashWindowFlag() {
        boolean hasSplashWindowFlag = this.mStartingWindowFlags == 268435456;
        ColorStartingWindowUtils.logD("checkSplashWindowFlag ==> hasSplashWindowFlag =: " + hasSplashWindowFlag);
        return hasSplashWindowFlag;
    }

    public boolean checkSuggestShowSplashWindowFlag() {
        boolean hasSuggestShowWindowFlag = this.mStartingWindowFlags == 536870912;
        ColorStartingWindowUtils.logD("checkSuggestShowSplashWindowFlag ==> hasSuggestShowWindowFlag =: " + hasSuggestShowWindowFlag);
        return hasSuggestShowWindowFlag;
    }

    public boolean checkAppWindowAnimating(AppWindowToken token) {
        if (token == null) {
            return false;
        }
        boolean isAppAnimating = token.isAppAnimating();
        ColorStartingWindowUtils.logD("checkAppWindowAnimating ==> isAppAnimating =:" + isAppAnimating);
        return isAppAnimating;
    }

    public boolean allowUseSnapshot(AppWindowToken token, boolean newTask, boolean taskSwitch, boolean processRunning, boolean activityCreated) {
        ColorStartingWindowUtils.logD("allowUseSnapshot ==> token =:" + token + ",newTask =:" + newTask + ",taskSwitch =:" + taskSwitch + ",processRunning =:" + processRunning + ",activityCreated =:" + activityCreated);
        if (newTask || !processRunning || !taskSwitch || !activityCreated || token.mActivityRecord == null) {
            return true;
        }
        String packageName = token.mActivityRecord.packageName;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (ColorStartingWindowUtils.isTaskSnapshotBlackPackage(packageName)) {
            ColorStartingWindowUtils.logD("don't allow use snapshot if is in local black packages ==> packageName =:" + packageName);
            return false;
        } else if (!ColorStartingWindowUtils.isTaskSnapshotBlackToken(token.mActivityRecord.shortComponentName)) {
            return true;
        } else {
            ColorStartingWindowUtils.logD("don't allow use snapshot if is in local black tokens ==> token =:" + token);
            return false;
        }
    }

    public void setAllowAppSnapshot(boolean allowAppSnapshot) {
        if (this.mSupportSnapshotPreview) {
            this.mAllowAppSnapshot = allowAppSnapshot;
            if (!allowAppSnapshot) {
                reset();
            }
        }
    }

    public Animation createAnimationForLauncherExit() {
        if (!this.mSupportSnapshotPreview) {
            return null;
        }
        ColorStartingWindowUtils.logD("createAnimationForLauncherExit");
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 1.0f);
        alphaAnimation.setDuration(450);
        return alphaAnimation;
    }

    public boolean setStartingWindowExitAnimation(WindowState windowState) {
        if (!this.mSupportSnapshotPreview || windowState == null || windowState.mAppToken == null || windowState.mAttrs.type != 3) {
            return false;
        }
        windowState.startAnimation(createStartingWindowExitAnimation(windowState));
        windowState.mWinAnimator.mAnimationIsEntrance = false;
        return true;
    }

    private Animation createStartingWindowExitAnimation(WindowState windowState) {
        String packageName = windowState.mAppToken.mActivityRecord.packageName;
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        int duration = ColorStartingWindowContants.DEFAULT_STARTING_WINDOW_EXIT_ANIMATION_DURATION;
        if (checkSplashWindowFlag()) {
            if (ColorStartingWindowUtils.removeStartingWindowImmediately(packageName)) {
                alphaAnimation = new AlphaAnimation(1.0f, 1.0f);
                duration = 0;
                ColorStartingWindowUtils.logD("createStartingWindowExitAnimation use Alpha(1,1) windowState =:" + windowState);
            }
        } else if (checkSuggestShowSplashWindowFlag()) {
            duration = 0;
        }
        ColorStartingWindowUtils.logD("createStartingWindowExitAnimationForPackage duration =:" + duration);
        alphaAnimation.setDuration((long) duration);
        return alphaAnimation;
    }

    public void putSnapshot(final AppWindowToken token) {
        if (this.mSupportSnapshotPreview) {
            ColorStartingWindowUtils.logD("putSnapshot mRequestStoreAppTokenSnapshot =:" + this.mRequestStoreAppTokenSnapshot + ",mAllowAppSnapshot =: " + this.mAllowAppSnapshot + "\n token =: " + token);
            boolean allowAppSnapshot = this.mAllowAppSnapshot;
            this.mAllowAppSnapshot = true;
            if (!allowAppSnapshot) {
                ColorStartingWindowUtils.logD("putSnapshot token don't allow");
                reset();
            } else if (token == null || token.mActivityRecord == null) {
                ColorStartingWindowUtils.logD("putSnapshot token.mActivityRecord is null don't allow");
                reset();
            } else if (token.getWindowingMode() != 1) {
                ColorStartingWindowUtils.logD("putSnapshot token is not full screen don't allow");
                reset();
            } else {
                final String targetKey = token.mActivityRecord.packageName;
                if (ColorStartingWindowContants.SPLASH_SNAPSHOT_BLACK_SYSTEM_APP.contains(targetKey)) {
                    reset();
                    return;
                }
                if (token.token instanceof ActivityRecord.Token) {
                    targetKey = token.token.getName();
                }
                if (ColorStartingWindowContants.SPECIAL_APP_SNAPSHOTS_KEY_MAP.containsValue(targetKey)) {
                    ColorStartingWindowUtils.logD("putSnapshot special app token =:" + targetKey + " don't allow");
                    reset();
                    return;
                }
                String specialKey = ColorStartingWindowContants.SPECIAL_APP_SNAPSHOTS_KEY_MAP.get(targetKey);
                if (!TextUtils.isEmpty(specialKey)) {
                    if (checkSplashWindowFlag()) {
                        targetKey = specialKey;
                    } else {
                        reset();
                        return;
                    }
                } else if (!this.mRequestStoreAppTokenSnapshot) {
                    this.mStartingWindowFlags = 0;
                    return;
                }
                if (ColorStartingWindowContants.SPECIAL_APP_DRAWABLE_RES_KEY_MAP.containsKey(targetKey)) {
                    ColorStartingWindowUtils.logD("putSnapshot app has drawable res token =:" + targetKey + " don't allow");
                    reset();
                    return;
                }
                reset();
                checkThreadPoolNotNull();
                ColorStartingWindowUtils.logD("putSnapshot finalTargetKey =:" + targetKey);
                this.mThreadPool.execute(new Runnable() {
                    /* class com.android.server.wm.ColorStartingWindowManager.AnonymousClass1 */

                    public void run() {
                        ColorStartingWindowManager.this.cacheTokenSnapshot(token, targetKey);
                    }
                });
            }
        }
    }

    private void reset() {
        this.mStartingWindowFlags = 0;
        this.mRequestStoreAppTokenSnapshot = false;
    }

    /* access modifiers changed from: private */
    public void cacheTokenSnapshot(AppWindowToken token, String targetKey) {
        if (token != null) {
            String packageName = token.mActivityRecord.packageName;
            if (!ColorStartingWindowUtils.supportSnapshotSplash(packageName)) {
                ColorStartingWindowUtils.logD("cacheTokenSnapshot :: black app ,we don't capture screen,packageName ==> " + packageName);
                return;
            }
            try {
                Bitmap snapshot = createTokenSnapshot(token);
                if (snapshot != null) {
                    String fileNameOnDisk = token.mActivityRecord.packageName;
                    if (ColorStartingWindowContants.APP_TOKEN_DIALER.equals(targetKey)) {
                        fileNameOnDisk = ColorStartingWindowContants.DIALER_PREFIX + token.mActivityRecord.packageName;
                    }
                    synchronized (LOCK) {
                        this.mSnapshotCache.putSnapshot(this.mWMService.mCurrentUserId, targetKey, snapshot);
                        this.mSnapshotPersister.persistSnapshot(fileNameOnDisk, this.mWMService.mCurrentUserId, snapshot);
                    }
                }
            } catch (Exception e) {
                ColorStartingWindowUtils.logE("cacheTokenSnapshot :: error ==> " + e.getMessage());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0121, code lost:
        r0 = th;
     */
    private Bitmap createTokenSnapshot(AppWindowToken token) {
        WindowState mainWindow;
        GraphicBuffer buffer;
        long startTime = System.currentTimeMillis();
        ColorStartingWindowUtils.logD("createTokenSnapshot start -------------");
        Task task = token.getTask();
        if (task == null) {
            ColorStartingWindowUtils.logD("task is null");
            return null;
        }
        Rect cropRect = new Rect();
        task.getBounds(cropRect);
        cropRect.offsetTo(0, 0);
        synchronized (token.mWmService.mGlobalLock) {
            mainWindow = token.findMainWindow(false);
        }
        if (mainWindow == null) {
            ColorStartingWindowUtils.logD("mainWindow is null");
            return null;
        } else if (mainWindow.getSurfaceControl() == null) {
            ColorStartingWindowUtils.logD("mainWindow :: SurfaceControl is null");
            return null;
        } else {
            SurfaceControl.ScreenshotGraphicBuffer screenshotBuffer = SurfaceControl.captureLayers(mainWindow.getSurfaceControl().getHandle(), cropRect, 1.0f);
            ColorStartingWindowUtils.logD("createTokenSnapshot captureLayers spend time -------------" + (System.currentTimeMillis() - startTime));
            if (screenshotBuffer != null) {
                buffer = screenshotBuffer.getGraphicBuffer();
            } else {
                buffer = null;
            }
            if (buffer == null || buffer.getWidth() <= 1) {
                return null;
            }
            if (buffer.getHeight() <= 1) {
                return null;
            }
            Bitmap bitmap = Bitmap.wrapHardwareBuffer(buffer, screenshotBuffer.getColorSpace());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] bytes = outputStream.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap2 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (!(task.getConfiguration() != null && (task.getConfiguration().uiMode & 48) == 32)) {
                long checkStartTime = System.currentTimeMillis();
                boolean isBitmapValid = ColorStartingWindowUtils.checkBitmapValid(bitmap2);
                ColorStartingWindowUtils.logD("createTokenSnapshot checkBitmapValid cost " + (System.currentTimeMillis() - checkStartTime) + " ms,isBitmapValid =: " + isBitmapValid);
                if (!isBitmapValid) {
                    return null;
                }
            }
            ColorStartingWindowUtils.logD("createTokenSnapshot total spend time -------------" + (System.currentTimeMillis() - startTime));
            return bitmap2;
        }
        while (true) {
        }
    }

    public boolean checkSkipTransitionAnimation(DisplayContent displayContent) {
        if (this.mSupportSnapshotPreview && !displayContent.mClosingApps.isEmpty()) {
            Iterator it = displayContent.mClosingApps.iterator();
            while (it.hasNext()) {
                if (((AppWindowToken) it.next()).isActivityTypeHome()) {
                    ColorStartingWindowUtils.logD("checkSkipTransitionAnimation for launcher closing,we not skip animation");
                    return false;
                }
            }
        }
        return true;
    }

    public BitmapDrawable getAppSnapshotBitmapDrawable(final PhoneWindow window) {
        String key;
        final String fixedPackageName;
        if (!this.mSupportSnapshotPreview) {
            return null;
        }
        long startTime = System.currentTimeMillis();
        if (window == null) {
            return null;
        }
        WindowManager.LayoutParams params = window.getAttributes();
        if (params != null) {
            if (params.token != null) {
                String key2 = params.packageName;
                if (!ColorStartingWindowUtils.supportSnapshotSplash(key2)) {
                    ColorStartingWindowUtils.logD("getAppSnapshotBitmapDrawable don't support snapshot for package =: " + key2);
                    return null;
                }
                if (params.token instanceof ActivityRecord.Token) {
                    key = params.token.getName();
                } else {
                    key = key2;
                }
                ColorStartingWindowUtils.logD("getAppSnapshot key =:" + key);
                if (ColorStartingWindowContants.APP_TOKEN_DIALER.equals(key)) {
                    fixedPackageName = ColorStartingWindowContants.DIALER_PREFIX + params.packageName;
                } else {
                    fixedPackageName = params.packageName;
                }
                if ((!ColorStartingWindowContants.SPECIAL_APP_SNAPSHOTS_KEY_MAP.containsValue(key) && !this.mSupportSnapshotSplashForCallingApp) || !checkSplashWindowFlag()) {
                    return null;
                }
                final BitmapDrawable bitmapDrawable = this.mSnapshotCache.getPreloadedOrCachedSplash(this.mWMService.mCurrentUserId, fixedPackageName, key);
                if (bitmapDrawable == null) {
                    final long start = System.currentTimeMillis();
                    this.mSnapshotCache.registerSnapshotLoadListener(new ColorAppStartingSnapshotCache.OnSnapshotLoadFinishListener() {
                        /* class com.android.server.wm.ColorStartingWindowManager.AnonymousClass2 */

                        @Override // com.android.server.wm.startingwindow.ColorAppStartingSnapshotCache.OnSnapshotLoadFinishListener
                        public void onSnapshotLoaded(String packageName) {
                            ColorStartingWindowUtils.logD("load bitmap onSnapshotLoaded bitmapDrawable =: " + bitmapDrawable + ",packageName =: " + packageName + ",fixedPackageName =: " + fixedPackageName);
                            if (TextUtils.isEmpty(packageName) || !packageName.equals(fixedPackageName)) {
                                ColorStartingWindowManager.this.mSnapshotCache.clearCachedPreloadedSplash();
                                return;
                            }
                            final long now = System.currentTimeMillis();
                            ColorStartingWindowUtils.logD("load bitmap in disk spend time =: " + (now - start));
                            ColorStartingWindowManager.this.mWMService.mAnimationHandler.postAtFrontOfQueue(new Runnable() {
                                /* class com.android.server.wm.ColorStartingWindowManager.AnonymousClass2.AnonymousClass1 */

                                public void run() {
                                    ColorStartingWindowUtils.logD("set window background spend time =: " + (System.currentTimeMillis() - now));
                                    try {
                                        if (window != null) {
                                            window.setBackgroundDrawable(ColorStartingWindowManager.this.mSnapshotCache.getPreloadedSplash(fixedPackageName));
                                        }
                                    } catch (Exception e) {
                                        ColorStartingWindowUtils.logE("error when set window background e =: " + e.getMessage());
                                    }
                                    ColorStartingWindowManager.this.mSnapshotCache.clearCachedPreloadedSplash();
                                }
                            });
                        }
                    });
                } else {
                    this.mSnapshotCache.clearCachedPreloadedSplash();
                }
                ColorStartingWindowUtils.logD("get window background spend time =: " + (System.currentTimeMillis() - startTime));
                return bitmapDrawable;
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x011c, code lost:
        r0 = th;
     */
    public void preloadAppSplash(int realCallingPid, SafeActivityOptions options, ActivityInfo activityInfo, String reason) {
        WindowProcessController callApp;
        if (this.mSupportSnapshotPreview) {
            long start = System.currentTimeMillis();
            if (realCallingPid != 0 && activityInfo != null) {
                if (activityInfo.applicationInfo != null) {
                    synchronized (this.mWMService.mGlobalLock) {
                        callApp = this.mWMService.mAtmService.mProcessMap.getProcess(realCallingPid);
                    }
                    if (callApp == null) {
                        ColorStartingWindowUtils.logD("preloadAppSplash app is not support if calling app is null");
                        return;
                    }
                    String callAppPackageName = callApp.mInfo == null ? "" : callApp.mInfo.packageName;
                    if (!ColorStartingWindowUtils.supportSnapshotSplashForCallingApp(callAppPackageName)) {
                        ColorStartingWindowUtils.logD("preloadAppSplash app is not support for calling app ==>:" + callAppPackageName);
                        return;
                    }
                    String packageName = activityInfo.packageName;
                    if (!ColorStartingWindowUtils.supportSnapshotSplash(packageName)) {
                        ColorStartingWindowUtils.logD("preloadAppSplash app is not support cache startinwindow,don't preload splash ==> packageName = :" + packageName);
                        return;
                    }
                    String processName = activityInfo.processName;
                    WindowProcessController proc = (WindowProcessController) this.mWMService.mAtmService.mProcessNames.get(processName, activityInfo.applicationInfo.uid);
                    if (proc != null && proc.hasThread() && proc.hasActivities()) {
                        ColorStartingWindowUtils.logD("preloadAppSplash process is running ==> processName : " + processName);
                        return;
                    }
                    String appToken = activityInfo.getComponentName().flattenToShortString();
                    if (ColorStartingWindowContants.APP_TOKEN_DIALER.equals(appToken)) {
                        packageName = ColorStartingWindowContants.DIALER_PREFIX + packageName;
                    }
                    this.mSnapshotCache.preloadSplash(this.mWMService.mCurrentUserId, this.mWMService.mContext, packageName, appToken);
                    ColorStartingWindowUtils.logD("preloadAppSplash packageName =: " + packageName + ",appToken =: " + appToken + ",reason =: " + reason + "\n spend time =: " + (System.currentTimeMillis() - start));
                    return;
                }
                return;
            }
            return;
        }
        return;
        while (true) {
        }
    }

    public void onStartAppShotcut() {
        if (this.mSupportSnapshotPreview) {
            this.mIsStartingAppShotcut = true;
        }
    }

    public boolean skipAppTransitionAnimation() {
        return true;
    }

    public void setAppTransit(int transit) {
        this.mAppTransit = transit;
    }

    public int getAppTransit(int originTransit) {
        int transit = this.mAppTransit;
        this.mAppTransit = -1;
        return transit;
    }

    public boolean clearStartingWindowWhenSnapshotDiffOrientation(AppWindowToken token) {
        if (token == null || token.mActivityRecord == null) {
            return false;
        }
        return ColorStartingWindowUtils.clearStartingWindowWhenDiffOrientation(token.mActivityRecord.packageName);
    }

    public void preloadAppSplash(IBinder resultTo, ActivityInfo activityInfo) {
        if (this.mSupportSnapshotPreview) {
            long start = System.currentTimeMillis();
            if (resultTo != null && activityInfo != null && activityInfo.applicationInfo != null) {
                String packageName = activityInfo.packageName;
                if (!ColorStartingWindowUtils.supportSnapshotSplash(packageName)) {
                    ColorStartingWindowUtils.logD("preloadAppSplash app is not support cache startinwindow,don't preload splash ==> packageName = :" + packageName);
                    return;
                }
                ActivityRecord callingRecord = ActivityRecord.forTokenLocked(resultTo);
                if (callingRecord == null || callingRecord.isActivityTypeHome()) {
                    String processName = activityInfo.processName;
                    WindowProcessController proc = (WindowProcessController) this.mWMService.mAtmService.mProcessNames.get(processName, activityInfo.applicationInfo.uid);
                    if (proc != null && proc.hasThread() && proc.hasActivities()) {
                        ColorStartingWindowUtils.logD("preloadAppSplash process is running ==> processName : " + processName);
                        return;
                    }
                    String appToken = activityInfo.getComponentName().flattenToShortString();
                    if (ColorStartingWindowContants.APP_TOKEN_DIALER.equals(appToken)) {
                        packageName = ColorStartingWindowContants.DIALER_PREFIX + packageName;
                    }
                    this.mSnapshotCache.preloadSplash(this.mWMService.mCurrentUserId, this.mWMService.mContext, packageName, appToken);
                    ColorStartingWindowUtils.logD("preloadAppSplash packageName =: " + packageName + ",appToken =: " + appToken + "\n spend time =: " + (System.currentTimeMillis() - start));
                    return;
                }
                ColorStartingWindowUtils.logD("preloadAppSplash calling record is not launcher,don't preload splash");
            }
        }
    }
}
