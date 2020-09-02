package android.view;

import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Slog;
import java.lang.ref.WeakReference;

public class ColorViewRootUtil implements IColorViewRootUtil {
    private static final float EVENT_270 = 1.5707964f;
    private static final float EVENT_90 = -1.5707964f;
    private static final float EVENT_ORI = 0.0f;
    private static final float EVENT_OTHER = -3.1415927f;
    private static final float GESTURE_BOTTOM_AREA_PROP = 0.05f;
    private static final float GESTURE_SIDE_AREA_PROP = 0.067f;
    private static final float GLOABL_SCALE_COMPAT_APP = 1.333333f;
    private static final String HATEEROMORPHISM = "oppo.systemui.disable.edgepanel";
    public static final String KEY_NAVIGATIONBAR_MODE = "hide_navigationbar_enable";
    private static final boolean LOCAL_LOGV = true;
    private static final int MODE_NAVIGATIONBAR = 0;
    private static final int MODE_NAVIGATIONBAR_GESTURE = 2;
    private static final int MODE_NAVIGATIONBAR_GESTURE_SIDE = 3;
    private static final int MODE_NAVIGATIONBAR_WITH_HIDE = 1;
    private static final String TAG = "ColorViewRootUtil";
    private static boolean mHeteromorphism = false;
    private static int mHeteromorphismHeight = -1;
    private static ColorViewRootUtil sInstance = null;
    private float mCompactScale = 1.0f;
    private DisplayInfo mDisplayInfo = new DisplayInfo();
    private boolean mFullScreen = true;
    private int mHideNavigationbarArea;
    private boolean mIgnoring;
    private boolean mIsDisplayCompatApp;
    private int mNavBarMode = 0;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mSideGestureAreaWidth;
    private int mStatusBarHeight;

    public static ColorViewRootUtil getInstance() {
        ColorViewRootUtil colorViewRootUtil;
        synchronized (ColorViewRootUtil.class) {
            if (sInstance == null) {
                sInstance = new ColorViewRootUtil();
            }
            colorViewRootUtil = sInstance;
        }
        return colorViewRootUtil;
    }

    public void updateDisplayState(Display display, Context context, boolean isDisplayCompatApp) {
        Slog.d(TAG, "updateDisplayState, isDisplayCompatApp " + isDisplayCompatApp);
        this.mIsDisplayCompatApp = isDisplayCompatApp;
        if (display == null || display.getDisplayId() == -1 || display.getDisplayId() == 0) {
            display.getDisplayInfo(this.mDisplayInfo);
            int height = this.mDisplayInfo.logicalHeight;
            int width = this.mDisplayInfo.logicalWidth;
            this.mScreenHeight = height > width ? height : width;
            this.mScreenWidth = height > width ? width : height;
            Slog.d(TAG, "updateDisplayState mScreenHeight " + this.mScreenHeight + ", mScreenWidth " + this.mScreenWidth);
            DisplayMetrics appMetrics = new DisplayMetrics();
            this.mDisplayInfo.getAppMetrics(appMetrics);
            this.mCompactScale = CompatibilityInfo.computeCompatibleScaling(appMetrics, (DisplayMetrics) null);
            if (isDisplayCompatApp) {
                Slog.d(TAG, "updateDisplayState sIsDisplayCompatApp");
                this.mScreenHeight = (int) (((float) this.mScreenHeight) * GLOABL_SCALE_COMPAT_APP);
                this.mScreenWidth = (int) (((float) this.mScreenWidth) * GLOABL_SCALE_COMPAT_APP);
            }
            int i = this.mScreenWidth;
            this.mStatusBarHeight = (int) (((float) i) * GESTURE_BOTTOM_AREA_PROP);
            this.mSideGestureAreaWidth = (int) (((float) i) * GESTURE_SIDE_AREA_PROP);
            return;
        }
        Slog.d(TAG, "updateDisplayState don't initSwipState because display " + display.getDisplayId() + " is not default display");
    }

    public void initSwipState(Display display, Context context) {
        Slog.d(TAG, "initSwipState");
        initSwipState(display, context, false);
    }

    public void initSwipState(Display display, Context context, boolean isDisplayCompatApp) {
        Slog.d(TAG, "initSwipState, isDisplayCompatApp " + isDisplayCompatApp);
        this.mIsDisplayCompatApp = isDisplayCompatApp;
        if (display == null || display.getDisplayId() == -1 || display.getDisplayId() == 0) {
            display.getDisplayInfo(this.mDisplayInfo);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getRealMetrics(displayMetrics);
            int h = displayMetrics.heightPixels;
            int w = displayMetrics.widthPixels;
            this.mScreenHeight = h > w ? h : w;
            this.mScreenWidth = h > w ? w : h;
            Slog.d(TAG, "mScreenHeight " + this.mScreenHeight + ", mScreenWidth " + this.mScreenWidth);
            int height = this.mDisplayInfo.logicalHeight;
            int width = this.mDisplayInfo.logicalWidth;
            this.mHideNavigationbarArea = (context.getResources().getDimensionPixelSize(17105288) + (height > width ? height : width)) - 21;
            this.mStatusBarHeight = context.getResources().getDimensionPixelSize(201655827);
            mHeteromorphism = !context.getPackageManager().hasSystemFeature(HATEEROMORPHISM);
            if (mHeteromorphism) {
                mHeteromorphismHeight = context.getResources().getInteger(202178562);
            }
            DisplayMetrics appMetrics = new DisplayMetrics();
            this.mDisplayInfo.getAppMetrics(appMetrics);
            this.mCompactScale = CompatibilityInfo.computeCompatibleScaling(appMetrics, (DisplayMetrics) null);
            this.mNavBarMode = Settings.Secure.getInt(context.getContentResolver(), KEY_NAVIGATIONBAR_MODE, 0);
            this.mSideGestureAreaWidth = context.getResources().getInteger(202178573);
            if (isDisplayCompatApp) {
                Slog.d(TAG, "sIsDisplayCompatApp");
                this.mScreenHeight = (int) (((float) this.mScreenHeight) * GLOABL_SCALE_COMPAT_APP);
                this.mScreenWidth = (int) (((float) this.mScreenWidth) * GLOABL_SCALE_COMPAT_APP);
            }
            int i = this.mScreenWidth;
            this.mStatusBarHeight = (int) (((float) i) * GESTURE_BOTTOM_AREA_PROP);
            this.mSideGestureAreaWidth = (int) (((float) i) * GESTURE_SIDE_AREA_PROP);
            return;
        }
        Slog.d(TAG, "don't initSwipState because display " + display.getDisplayId() + " is not default display");
    }

    public boolean needScale(int noncompatDensity, int density, Display display) {
        CompatibilityInfo compatibilityInfo = null;
        if (!(display == null || display.getDisplayAdjustments() == null)) {
            compatibilityInfo = display.getDisplayAdjustments().getCompatibilityInfo();
        }
        if (compatibilityInfo == null || !compatibilityInfo.isScalingRequired() || compatibilityInfo.supportsScreen() || noncompatDensity == density) {
            return false;
        }
        return true;
    }

    public float getCompactScale() {
        return this.mCompactScale;
    }

    public int getScreenHeight() {
        return this.mScreenHeight;
    }

    public int getScreenWidth() {
        return this.mScreenWidth;
    }

    public boolean swipeFromBottom(MotionEvent event, int noncompatDensity, int density, Display display) {
        Slog.d(TAG, "swipeFromBottom!");
        if (display.getDisplayId() != 0) {
            Slog.e(TAG, "don't intercept event because display " + display.getDisplayId() + " is not default display");
            return false;
        } else if ((event.getSource() & 2) == 0) {
            Slog.d(TAG, "don't intercept event because event source is " + event.getSource());
            return false;
        } else {
            int action = event.getAction();
            if (action == 0) {
                int downY = (int) event.getRawY();
                int downX = (int) event.getRawX();
                if (this.mIsDisplayCompatApp) {
                    downY = OppoScreenDragUtil.adjustRawYForResolution(downY);
                    downX = OppoScreenDragUtil.adjustRawXForResolution(downX);
                }
                float f = this.mCompactScale;
                if (!(f == 1.0f || f == -1.0f || !needScale(noncompatDensity, density, display))) {
                    float f2 = this.mCompactScale;
                    downY = (int) ((((float) downY) * f2) + 0.5f);
                    downX = (int) ((f2 * ((float) downX)) + 0.5f);
                }
                this.mIgnoring = shouldIgnore(downX, downY, event, display);
            } else if (action == 1 && this.mIgnoring) {
                this.mIgnoring = false;
                return true;
            }
            return this.mIgnoring;
        }
    }

    public void checkGestureConfig(Context context) {
        this.mNavBarMode = Settings.Secure.getInt(context.getContentResolver(), KEY_NAVIGATIONBAR_MODE, 0);
    }

    public DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
    }

    public IColorLongshotViewHelper getColorLongshotViewHelper(WeakReference<ViewRootImpl> viewAncestor) {
        return new ColorLongshotViewHelper(viewAncestor);
    }

    private boolean shouldIgnore(int downX, int downY, MotionEvent event, Display display) {
        int downY2;
        int downX2;
        boolean ignore;
        String str;
        String str2;
        boolean ignore2;
        int i;
        int i2;
        if (event.getDownTime() != event.getEventTime()) {
            Slog.d(TAG, "do not ignore inject event MotionEvent:" + event);
            return false;
        }
        int rotation = 0;
        if (display != null) {
            rotation = display.getRotation();
        }
        if (this.mIsDisplayCompatApp) {
            Slog.d(TAG, "shouldIgnore, sIsDisplayCompatApp");
            downX2 = (int) (((float) downX) * GLOABL_SCALE_COMPAT_APP);
            downY2 = (int) (((float) downY) * GLOABL_SCALE_COMPAT_APP);
        } else {
            downX2 = downX;
            downY2 = downY;
        }
        int i3 = this.mNavBarMode;
        if (i3 == 0) {
            str = TAG;
        } else if (i3 == 1) {
            str = TAG;
        } else {
            if (rotation != 0) {
                if (rotation != 1) {
                    if (rotation != 2) {
                        if (rotation != 3) {
                            ignore = false;
                            Slog.d(TAG, "nav gesture mode swipeFromBottom ignore " + ignore + " downX " + downX2 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
                            return ignore;
                        }
                    }
                }
                if (downY2 >= this.mScreenWidth - this.mStatusBarHeight || ((downX2 < (i2 = this.mSideGestureAreaWidth) || downX2 > this.mScreenHeight - i2) && this.mNavBarMode == 3)) {
                    ignore = true;
                } else {
                    ignore = false;
                }
                Slog.d(TAG, "nav gesture mode swipeFromBottom ignore " + ignore + " downX " + downX2 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
                return ignore;
            }
            if (downY2 >= this.mScreenHeight - this.mStatusBarHeight || ((downX2 < (i = this.mSideGestureAreaWidth) || downX2 > this.mScreenWidth - i) && this.mNavBarMode == 3)) {
                ignore = true;
            } else {
                ignore = false;
            }
            Slog.d(TAG, "nav gesture mode swipeFromBottom ignore " + ignore + " downX " + downX2 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
            return ignore;
        }
        if (rotation != 0) {
            if (rotation == 1) {
                str2 = str;
                if (downX2 < this.mScreenHeight - this.mStatusBarHeight) {
                    ignore2 = false;
                } else {
                    ignore2 = true;
                }
            } else if (rotation != 2) {
                if (rotation != 3) {
                    str2 = str;
                    ignore2 = false;
                } else if (downX2 > this.mStatusBarHeight) {
                    ignore2 = false;
                    str2 = str;
                } else {
                    ignore2 = true;
                    str2 = str;
                }
            }
            Slog.d(str2, "nav bar mode ignore " + ignore + " downX " + downX2 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " rotation " + rotation + " event " + event);
            return ignore;
        }
        str2 = str;
        if (downY2 < this.mScreenHeight - this.mStatusBarHeight) {
            ignore2 = false;
        } else {
            ignore2 = true;
        }
        Slog.d(str2, "nav bar mode ignore " + ignore + " downX " + downX2 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " rotation " + rotation + " event " + event);
        return ignore;
    }
}
