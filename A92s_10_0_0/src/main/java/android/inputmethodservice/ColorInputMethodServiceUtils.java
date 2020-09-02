package android.inputmethodservice;

import android.app.ColorStatusBarManager;
import android.app.Dialog;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ColorSmartCutQuantizer;
import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.IColorViewRootUtil;
import android.view.OppoBaseView;
import android.view.View;
import android.view.WindowManagerGlobal;
import com.color.util.ColorNavigationBarUtil;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class ColorInputMethodServiceUtils implements IColorInputMethodServiceUtils {
    private static final String DCS_TAG = "inputMethod";
    static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.imelog", false);
    static final boolean DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String EVENT_INPUT_METHOD_SHOW = "input_method_show";
    private static final String HIDE_NAVIGATIONBAR_ENABLE = "hide_navigationbar_enable";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_INNER = "isInner";
    private static final String KEY_LABEL = "label";
    private static final String KEY_NAME = "packageName";
    private static final String KEY_OTA_VERSION = "otaVersion";
    private static final String KEY_START_TIME = "startTime";
    private static final String KEY_VERSION_CODE = "versionCode";
    private static final String KEY_VERSION_NAME = "versionName";
    static final String TAG = "ColorInputMethodServiceUtils";
    private static int mNavigationBarState = 0;
    private int DELAY_TIME = 100;
    private int SHOW_DELAY = 500;
    private ColorStatusBarManager mColorStatusBarManager = null;
    private Handler mHandler;
    private ArrayList<String> mInnerList = new ArrayList<>(Arrays.asList("com.sohu.inputmethod.sogouoem", "com.baidu.input_oppo", "com.google.android.inputmethod.latin", "com.simeji.android.oppo"));
    private InputMethodService mInputMethodService;
    private String mOtaVersion;

    public void init(Context context) {
        this.mInputMethodService = (InputMethodService) context;
        this.mHandler = new Handler(context.getMainLooper());
        this.mColorStatusBarManager = new ColorStatusBarManager();
        boolean z = false;
        mNavigationBarState = Settings.Secure.getInt(context.getContentResolver(), "hide_navigationbar_enable", 0);
        ColorNavigationBarUtil instance = ColorNavigationBarUtil.getInstance();
        if (mNavigationBarState == 2) {
            z = true;
        }
        instance.setImePackageInGestureMode(z);
        this.mOtaVersion = SystemProperties.get("ro.build.version.ota");
    }

    public void beforeInputShow() {
        if (DEBUG) {
            Log.v(TAG, " mNavigationBarState=" + mNavigationBarState);
        }
        if (mNavigationBarState == 1) {
            ColorStatusBarManager colorStatusBarManager = this.mColorStatusBarManager;
        }
    }

    public void afterInputShow() {
        if (DEBUG) {
            Log.v(TAG, " mNavigationBarState=" + mNavigationBarState);
        }
        if (mNavigationBarState == 1) {
            ColorStatusBarManager colorStatusBarManager = this.mColorStatusBarManager;
        }
    }

    public boolean getDockSide() {
        int dock = -1;
        try {
            dock = WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get dock side: " + e);
        }
        return dock != -1;
    }

    public void onChange(Uri uri) {
        if (Settings.Secure.getUriFor("hide_navigationbar_enable").equals(uri)) {
            boolean inGestureMode = false;
            mNavigationBarState = Settings.Secure.getInt(this.mInputMethodService.getContentResolver(), "hide_navigationbar_enable", 0);
            int i = mNavigationBarState;
            if (i == 2 || i == 3) {
                inGestureMode = true;
            }
            ColorNavigationBarUtil.getInstance().setImePackageInGestureMode(inGestureMode);
        }
    }

    public void updateColorNavigationGuardColor(final Dialog window) {
        if (window.getWindow() != null) {
            window.getWindow().getDecorView().post(new Runnable() {
                /* class android.inputmethodservice.ColorInputMethodServiceUtils.AnonymousClass1 */

                public void run() {
                    int unused = ColorInputMethodServiceUtils.this.getCacheRgb(window);
                }
            });
        }
    }

    public void uploadData(long startTime) {
        HashMap<String, String> map = new HashMap<>();
        String pkgName = this.mInputMethodService.getPackageName();
        map.put(KEY_NAME, this.mInputMethodService.getPackageName());
        map.put(KEY_DURATION, ((int) ((((double) (System.currentTimeMillis() - startTime)) / 1000.0d) + 0.5d)) + "");
        map.put(KEY_START_TIME, startTime + "");
        try {
            PackageManager pm = this.mInputMethodService.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(pkgName, 0);
            map.put(KEY_VERSION_NAME, packageInfo.versionName);
            map.put(KEY_VERSION_CODE, packageInfo.versionCode + "");
            map.put(KEY_LABEL, pm.getApplicationInfo(pkgName, 0).loadLabel(pm).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put(KEY_OTA_VERSION, this.mOtaVersion);
        boolean isInner = this.mInnerList.contains(pkgName);
        map.put(KEY_INNER, isInner + "");
        OppoStatistics.onCommon(this.mInputMethodService, DCS_TAG, EVENT_INPUT_METHOD_SHOW, map, false);
    }

    /* access modifiers changed from: private */
    public int getCacheRgb(Dialog window) {
        Bitmap clip;
        int dominantColor;
        OppoBaseView oppoBaseDecorView;
        View mInputFrame = window.findViewById(16909216);
        if (mInputFrame != null) {
            Rect contentRect = new Rect();
            mInputFrame.getBoundsOnScreen(contentRect, true);
            int viewTop = contentRect.top;
            if (contentRect.bottom <= 10) {
                return -1;
            }
            contentRect.top = contentRect.bottom - 10;
            int width = contentRect.width() / 2;
            if (width > 800) {
                contentRect.left = (contentRect.left + width) - 400;
                contentRect.right = (contentRect.right - width) + 400;
            }
            OppoBaseView oppoBaseView = (OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, mInputFrame);
            Bitmap cache = oppoBaseView != null ? oppoBaseView.getColorCustomDrawingCache(contentRect, viewTop) : null;
            if (cache != null && !cache.isRecycled()) {
                int cacheWidth = cache.getWidth();
                int cacheHeight = cache.getHeight();
                if (cacheHeight >= 10) {
                    if (cacheWidth > 0) {
                        if (cacheWidth >= 800) {
                            clip = Bitmap.createBitmap(cache, (cacheWidth / 2) - 400, cacheHeight - 10, (cacheWidth / 2) + 400, 10);
                        } else {
                            clip = Bitmap.createBitmap(cache, 0, cacheHeight - 10, cacheWidth, 10);
                        }
                        if (clip != null) {
                            int alpha = getTransparentBitmap(clip);
                            int bitmapWidth = clip.getWidth();
                            int bitmapHeight = clip.getHeight();
                            int[] pixels = new int[(bitmapWidth * bitmapHeight)];
                            clip.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
                            int dominantColor2 = new ColorSmartCutQuantizer(pixels).getDominantColor();
                            if (DEBUG_PANIC) {
                                Log.d(TAG, "getCacheRgb: " + Integer.toHexString(dominantColor2));
                            }
                            if (dominantColor2 == 0) {
                                dominantColor = (16777215 & dominantColor2) | (alpha << 24);
                            } else {
                                dominantColor = (16777215 & dominantColor2) | -16777216;
                            }
                            if (!(window.getWindow() == null || (oppoBaseDecorView = (OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, window.getWindow().getDecorView())) == null)) {
                                oppoBaseDecorView.updateColorNavigationGuardColor(dominantColor);
                            }
                            clip.recycle();
                            cache.recycle();
                        }
                    }
                }
                cache.recycle();
                return -1;
            }
        }
        return -1;
    }

    private int getTransparentBitmap(Bitmap sourceImg) {
        int length = sourceImg.getWidth() * sourceImg.getHeight();
        int[] argb = new int[length];
        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg.getWidth(), sourceImg.getHeight());
        int count = 0;
        for (int i : argb) {
            count += i >>> 24;
        }
        return count / length;
    }

    public void onComputeRaise(InputMethodService.Insets mTmpInsets, Dialog window) {
        View navigaitionBar = null;
        if (window.getWindow() != null) {
            navigaitionBar = window.getWindow().getDecorView().findViewById(16908336);
        }
        if (navigaitionBar != null) {
            int[] loc = new int[2];
            navigaitionBar.getLocationInWindow(loc);
            mTmpInsets.touchableRegion.union(new Rect(loc[0], loc[1], loc[0] + navigaitionBar.getWidth(), loc[1] + navigaitionBar.getHeight()));
        }
    }

    public void updateColorNavigationGuardColorDelay(final Dialog window) {
        if (window.getWindow() != null) {
            window.getWindow().getDecorView().postDelayed(new Runnable() {
                /* class android.inputmethodservice.ColorInputMethodServiceUtils.AnonymousClass2 */

                public void run() {
                    int unused = ColorInputMethodServiceUtils.this.getCacheRgb(window);
                }
            }, (long) this.DELAY_TIME);
        }
        OppoFeatureCache.getOrCreate(IColorViewRootUtil.DEFAULT, new Object[0]).checkGestureConfig(this.mInputMethodService);
    }
}
