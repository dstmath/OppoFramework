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
    public static final String GESTURE_UP_RANGE_PINNING_SUPPORT = "coloros.gesture.range.pinning.support";
    private static final float GLOABL_SCALE_COMPAT_APP = 1.333333f;
    private static final String HATEEROMORPHISM = "oppo.systemui.disable.edgepanel";
    public static final String KEY_NAVIGATIONBAR_MODE = "hide_navigationbar_enable";
    private static final String KEY_SWIPE_UP_GESTURE_ROTATION_FOLLOW_SCREEN = "follow_rotation_gesture_bar_enable";
    private static final boolean LOCAL_LOGV = true;
    private static final int MODE_NAVIGATIONBAR = 0;
    private static final int MODE_NAVIGATIONBAR_GESTURE = 2;
    private static final int MODE_NAVIGATIONBAR_GESTURE_SIDE = 3;
    private static final int MODE_NAVIGATIONBAR_WITH_HIDE = 1;
    private static final int NAV_BAR_POSITION_FOLLOW_SCREEN_DISABLE = 0;
    private static final int NAV_BAR_POSITION_FOLLOW_SCREEN_ENABLE = 1;
    private static final String TAG = "ColorViewRootUtil";
    private static boolean mHeteromorphism = false;
    private static int mHeteromorphismHeight = -1;
    private static ColorViewRootUtil sInstance = null;
    private float mCompactScale = 1.0f;
    private DisplayInfo mDisplayInfo = new DisplayInfo();
    private boolean mFollowScreenRotationSupport;
    private boolean mFullScreen = true;
    private int mHideNavigationbarArea;
    private boolean mIgnoring;
    private boolean mIsDisplayCompatApp;
    private int mNavBarMode = 0;
    private int mNavBarPositionFollowScreenRotation;
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

    private void updateScreenSize() {
        this.mScreenHeight = OppoScreenDragUtil.getHeight();
        this.mScreenWidth = OppoScreenDragUtil.getWidth();
        int height = this.mDisplayInfo.logicalHeight;
        int width = this.mDisplayInfo.logicalWidth;
        Slog.d(TAG, "updateScreenSize, mScreenHeight " + this.mScreenHeight + ",mScreenWidth:" + this.mScreenWidth + ",height:" + height + ",width:" + width);
        if (width > height) {
            int i = this.mScreenHeight;
            if (i <= width) {
                i = width;
            }
            this.mScreenHeight = i;
            int i2 = this.mScreenWidth;
            if (i2 <= height) {
                i2 = height;
            }
            this.mScreenWidth = i2;
            return;
        }
        int i3 = this.mScreenHeight;
        if (i3 <= height) {
            i3 = height;
        }
        this.mScreenHeight = i3;
        int i4 = this.mScreenWidth;
        if (i4 <= width) {
            i4 = width;
        }
        this.mScreenWidth = i4;
    }

    public void initSwipState(Display display, Context context) {
        initSwipState(display, context, false);
    }

    public void initSwipState(Display display, Context context, boolean isDisplayCompatApp) {
        Slog.d(TAG, "initSwipState, isDisplayCompatApp " + isDisplayCompatApp);
        this.mIsDisplayCompatApp = isDisplayCompatApp;
        if (display == null || display.getDisplayId() == -1 || display.getDisplayId() == 0) {
            display.getDisplayInfo(this.mDisplayInfo);
            updateScreenSize();
            this.mStatusBarHeight = context.getResources().getDimensionPixelSize(201655827);
            mHeteromorphism = !context.getPackageManager().hasSystemFeature(HATEEROMORPHISM);
            if (mHeteromorphism) {
                mHeteromorphismHeight = context.getResources().getInteger(202178562);
            }
            DisplayMetrics appMetrics = new DisplayMetrics();
            this.mDisplayInfo.getAppMetrics(appMetrics);
            this.mCompactScale = CompatibilityInfo.computeCompatibleScaling(appMetrics, (DisplayMetrics) null);
            this.mNavBarMode = Settings.Secure.getInt(context.getContentResolver(), KEY_NAVIGATIONBAR_MODE, 0);
            this.mNavBarPositionFollowScreenRotation = Settings.Secure.getInt(context.getContentResolver(), KEY_SWIPE_UP_GESTURE_ROTATION_FOLLOW_SCREEN, 1);
            this.mFollowScreenRotationSupport = context.getPackageManager().hasSystemFeature(GESTURE_UP_RANGE_PINNING_SUPPORT);
            this.mSideGestureAreaWidth = context.getResources().getInteger(202178573);
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

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0127  */
    private boolean shouldIgnore(int downX, int downY, MotionEvent event, Display display) {
        int downY2;
        int downX2;
        String str;
        int downX3;
        String str2;
        String str3;
        boolean ignore;
        int downX4;
        int downX5;
        boolean ignore2;
        int i;
        boolean ignore3;
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
        int i2 = this.mNavBarMode;
        if (i2 == 0) {
            str = TAG;
            downX3 = downX2;
            str2 = " downX ";
        } else if (i2 == 1) {
            str = TAG;
            downX3 = downX2;
            str2 = " downX ";
        } else {
            if (this.mFollowScreenRotationSupport && i2 == 2) {
                if (rotation != 1 && rotation != 3) {
                    downX4 = downX2;
                    if (rotation == 0) {
                        if (rotation != 1) {
                            if (rotation == 2) {
                                downX5 = downX4;
                            } else if (rotation != 3) {
                                ignore2 = false;
                                downX5 = downX4;
                                Slog.d(TAG, "nav gesture mode swipeFromBottom ignore " + ignore2 + " downX " + downX5 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
                                return ignore2;
                            }
                        }
                        if (downY2 < this.mScreenWidth - this.mStatusBarHeight) {
                            int i3 = this.mSideGestureAreaWidth;
                            downX5 = downX4;
                            if ((downX5 >= i3 && downX5 <= this.mScreenHeight - i3) || this.mNavBarMode != 3) {
                                ignore2 = false;
                                Slog.d(TAG, "nav gesture mode swipeFromBottom ignore " + ignore2 + " downX " + downX5 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
                                return ignore2;
                            }
                        } else {
                            downX5 = downX4;
                        }
                        ignore2 = true;
                        Slog.d(TAG, "nav gesture mode swipeFromBottom ignore " + ignore2 + " downX " + downX5 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
                        return ignore2;
                    }
                    downX5 = downX4;
                    if (downY2 < this.mScreenHeight - this.mStatusBarHeight || ((downX5 < (i = this.mSideGestureAreaWidth) || downX5 > this.mScreenWidth - i) && this.mNavBarMode == 3)) {
                        ignore2 = true;
                    } else {
                        ignore2 = false;
                    }
                    Slog.d(TAG, "nav gesture mode swipeFromBottom ignore " + ignore2 + " downX " + downX5 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
                    return ignore2;
                } else if (this.mNavBarPositionFollowScreenRotation == 0) {
                    if (rotation != 1) {
                        if (rotation != 3) {
                            ignore3 = false;
                        } else if (downX2 > this.mStatusBarHeight) {
                            ignore3 = false;
                        } else {
                            ignore3 = true;
                        }
                    } else if (downX2 < this.mScreenHeight - this.mStatusBarHeight) {
                        ignore3 = false;
                    } else {
                        ignore3 = true;
                    }
                    Slog.d(TAG, "nav gesture up and not follow_roation_gesture_bar_enable mode swipeFromBottom ignore " + ignore3 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
                    return ignore3;
                }
            }
            downX4 = downX2;
            if (rotation == 0) {
            }
            if (downY2 < this.mScreenHeight - this.mStatusBarHeight) {
            }
            ignore2 = true;
            Slog.d(TAG, "nav gesture mode swipeFromBottom ignore " + ignore2 + " downX " + downX5 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " event " + event + " rotation " + rotation);
            return ignore2;
        }
        if (rotation != 0) {
            if (rotation == 1) {
                str3 = str;
                if (downX3 < this.mScreenHeight - this.mStatusBarHeight) {
                    ignore = false;
                } else {
                    ignore = true;
                }
            } else if (rotation != 2) {
                if (rotation != 3) {
                    str3 = str;
                    ignore = false;
                } else if (downX3 > this.mStatusBarHeight) {
                    ignore = false;
                    str3 = str;
                } else {
                    ignore = true;
                    str3 = str;
                }
            }
            Slog.d(str3, "nav bar mode ignore " + ignore + str2 + downX3 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " rotation " + rotation + " event " + event);
            return ignore;
        }
        str3 = str;
        if (downY2 < this.mScreenHeight - this.mStatusBarHeight) {
            ignore = false;
        } else {
            ignore = true;
        }
        Slog.d(str3, "nav bar mode ignore " + ignore + str2 + downX3 + " downY " + downY2 + " mScreenHeight " + this.mScreenHeight + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mCompactScale + " nav mode " + this.mNavBarMode + " rotation " + rotation + " event " + event);
        return ignore;
    }
}
