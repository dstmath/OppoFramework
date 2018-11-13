package android.view;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.System;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;

public class GestureAreaDetection {
    private boolean mActionCancel = false;
    private final ColorDirectHelp mColorDirectHelp;
    private final Context mContext;
    private final FrameLayout mDecorView;
    private boolean mFindViewRestricted;
    private boolean mHighType = false;
    private float mInterceptArea = 0.08f;
    private boolean mInterceptEnabled = true;
    private boolean mIsLongClickSwipe = false;
    boolean mObserverRegistered = false;
    private boolean mTextSpreadReady = false;
    private long mTouchDownTime = 0;
    private int mTouchPointerID = 0;

    private class ColorDirectHelp {
        private static final String TEXT_MODE = "direct_text_mode";
        private static final String TEXT_TRIGGER_AREA = "touch_area_settings";
        private ContentObserver mInterceptEnabledObserver;
        private ContentObserver mTriggerAreaObserver;

        /* synthetic */ ColorDirectHelp(GestureAreaDetection this$0, ColorDirectHelp -this1) {
            this();
        }

        private ColorDirectHelp() {
            this.mTriggerAreaObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    ColorDirectHelp.this.updateDefaultArea();
                }
            };
            this.mInterceptEnabledObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    ColorDirectHelp.this.updateInterceptEnabled();
                }
            };
        }

        public void registerObserver() {
            updateDefaultArea();
            updateInterceptEnabled();
            ContentResolver resolver = GestureAreaDetection.this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor(TEXT_TRIGGER_AREA), true, this.mTriggerAreaObserver);
            resolver.registerContentObserver(System.getUriFor(TEXT_MODE), true, this.mInterceptEnabledObserver);
        }

        public void unregisterObserver() {
            ContentResolver resolver = GestureAreaDetection.this.mContext.getContentResolver();
            resolver.unregisterContentObserver(this.mTriggerAreaObserver);
            resolver.unregisterContentObserver(this.mInterceptEnabledObserver);
        }

        private void updateDefaultArea() {
            GestureAreaDetection.this.mInterceptArea = System.getFloat(GestureAreaDetection.this.mContext.getContentResolver(), TEXT_TRIGGER_AREA, 0.085f);
        }

        private void updateInterceptEnabled() {
            boolean z = true;
            GestureAreaDetection gestureAreaDetection = GestureAreaDetection.this;
            if (System.getInt(GestureAreaDetection.this.mContext.getContentResolver(), TEXT_MODE, 1) != 3) {
                z = false;
            }
            gestureAreaDetection.mInterceptEnabled = z;
        }
    }

    public GestureAreaDetection(Context context, FrameLayout decorView) {
        this.mContext = context;
        this.mDecorView = decorView;
        this.mColorDirectHelp = new ColorDirectHelp(this, null);
    }

    public boolean isLongPressSwipe() {
        return this.mIsLongClickSwipe;
    }

    public void onAttached(int windowType) {
        this.mHighType = windowType >= 1000;
        this.mFindViewRestricted = AccessibilityManager.getInstance(this.mContext).isColorDirectEnabled() ^ 1;
    }

    public void onDetached() {
        if (!this.mFindViewRestricted) {
            removeCallbacks();
            if (this.mObserverRegistered) {
                this.mColorDirectHelp.unregisterObserver();
                this.mObserverRegistered = false;
            }
        }
    }

    public void handleBackKey() {
        if (!this.mFindViewRestricted) {
            removeCallbacks();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev, boolean isHandling) {
        if (isHandling || (AccessibilityManager.getInstance(this.mContext).isColorDirectEnabled() ^ 1) != 0 || (this.mInterceptEnabled ^ 1) != 0 || this.mHighType) {
            return false;
        }
        if (this.mDecorView.getParent() == this.mDecorView.getViewRootImpl()) {
            switch (ev.getAction() & 255) {
                case 0:
                    if (!this.mObserverRegistered) {
                        this.mObserverRegistered = true;
                        this.mColorDirectHelp.registerObserver();
                    }
                    this.mActionCancel = false;
                    this.mTextSpreadReady = false;
                    this.mIsLongClickSwipe = false;
                    this.mTouchPointerID = ev.getPointerId(ev.getActionIndex());
                    checkTouchArea(ev.getSize());
                    break;
                case 1:
                    if (!this.mActionCancel) {
                        removeCallbacks();
                        break;
                    }
                    removeCallbacks();
                    return true;
                case 2:
                    int actionIndex = ev.getActionIndex();
                    if (ev.getPointerId(actionIndex) == this.mTouchPointerID) {
                        float x = ev.getX(actionIndex);
                        float y = ev.getY(actionIndex);
                        if (!this.mTextSpreadReady) {
                            if (ev.getEventTime() - ev.getDownTime() <= 200) {
                                checkTouchArea(ev.getSize());
                                break;
                            }
                            this.mTextSpreadReady = true;
                            break;
                        }
                    }
                    break;
                case 3:
                    removeCallbacks();
                    break;
            }
        }
        removeCallbacks();
        return false;
    }

    private void removeCallbacks() {
        this.mIsLongClickSwipe = false;
        this.mActionCancel = false;
    }

    void checkTouchArea(float touchSize) {
        if (touchSize > this.mInterceptArea) {
            this.mActionCancel = true;
            this.mTextSpreadReady = true;
            this.mIsLongClickSwipe = true;
        }
    }
}
