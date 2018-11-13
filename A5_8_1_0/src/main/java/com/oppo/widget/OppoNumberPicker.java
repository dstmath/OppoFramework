package com.oppo.widget;

import android.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Scroller;
import com.color.util.ColorAccessibilityUtil;
import com.color.util.ColorDialogUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OppoNumberPicker extends LinearLayout {
    public static final int ALIGN_LEFT = 1;
    public static final int ALIGN_MIDDLE = 0;
    public static final int ALIGN_RIGHT = 2;
    private static final int BUTTON_ALPHA_OPAQUE = 1;
    private static final int BUTTON_ALPHA_TRANSPARENT = 0;
    private static final int CHANGE_CURRENT_BY_ONE_SCROLL_DURATION = 300;
    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;
    private static final char[] DIGIT_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final String PROPERTY_BUTTON_ALPHA = "alpha";
    private static final String PROPERTY_SELECTOR_PAINT_ALPHA = "selectorPaintAlpha";
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;
    private static final int SELECTOR_WHEEL_BRIGHT_ALPHA = 255;
    private static final int SELECTOR_WHEEL_DIM_ALPHA = 255;
    private static final int SELECTOR_WHEEL_STATE_LARGE = 2;
    private static final int SELECTOR_WHEEL_STATE_NONE = 0;
    private static final int SELECTOR_WHEEL_STATE_SMALL = 1;
    private static final int SHOW_INPUT_CONTROLS_DELAY_MILLIS = ViewConfiguration.getDoubleTapTimeout();
    private static final int SIZE_UNSPECIFIED = -1;
    private static String TAG = "OppoNumberPicker";
    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;
    public static final Formatter TWO_DIGIT_FORMATTER = new Formatter() {
        final Object[] mArgs = new Object[1];
        final StringBuilder mBuilder = new StringBuilder();
        java.util.Formatter mFmt = new java.util.Formatter(this.mBuilder, Locale.getDefault());
        Locale mLocale = Locale.getDefault();

        public String format(int value) {
            Locale currentLocale = Locale.getDefault();
            if (!currentLocale.equals(this.mLocale)) {
                this.mLocale = currentLocale;
                this.mFmt = new java.util.Formatter(this.mBuilder, currentLocale);
            }
            this.mArgs[0] = Integer.valueOf(value);
            this.mBuilder.delete(0, this.mBuilder.length());
            this.mFmt.format("%02d", this.mArgs);
            return this.mFmt.toString();
        }
    };
    private int SELECTOR_MIDDLE_ITEM_INDEX;
    private AccessibilityNodeProviderImpl mAccessibilityNodeProvider;
    private final Scroller mAdjustScroller;
    private AdjustScrollerCommand mAdjustScrollerCommand;
    private boolean mAdjustScrollerOnUpEvent;
    private int mAlignPosition;
    int mBlueEnd;
    int mBlueStart;
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;
    private int mChangeIndexDistance;
    private boolean mCheckBeginEditOnUpEvent;
    private int mClickSoundId;
    private final boolean mComputeMaxWidth;
    private int mCurrentScrollOffset;
    private final ImageButton mDecrementButton;
    private final Animator mDimSelectorWheelAnimator;
    private String[] mDisplayedValues;
    private String mFilePath;
    private final Scroller mFlingScroller;
    private final boolean mFlingable;
    int mFocusTextSize;
    private Formatter mFormatter;
    int mGreenEnd;
    int mGreenStart;
    private final ImageButton mIncrementButton;
    private int mInitialScrollOffset;
    private final EditText mInputText;
    boolean mIsBold;
    private float mLastDownEventY;
    private int mLastHoveredChildVirtualViewId;
    private float mLastMotionEventY;
    private long mLastUpEventTimeMillis;
    private long mLongPressUpdateInterval;
    private String mLunarLeap11;
    private String mLunarLeap12;
    private final int mMaxHeight;
    private int mMaxValue;
    private int mMaxWidth;
    private int mMaximumFlingVelocity;
    private final int mMinHeight;
    private int mMinValue;
    private final int mMinWidth;
    private int mMinimumFlingVelocity;
    private int mNumberPickerPaddingLeft;
    private int mNumberPickerPaddingRight;
    private OnScrollListener mOnScrollListener;
    private OnValueChangeListener mOnValueChangeListener;
    private int mPreviousScrollerY;
    int mRedEnd;
    int mRedStart;
    private int mScrollState;
    private boolean mScrollWheelAndFadingEdgesInitialized;
    private int mSelectorElementHeight;
    private final SparseArray<String> mSelectorIndexToStringCache;
    private int[] mSelectorIndices;
    private int mSelectorTextGapHeight;
    private final Paint mSelectorWheelPaint;
    private int mSelectorWheelState;
    private SetSelectionCommand mSetSelectionCommand;
    private final AnimatorSet mShowInputControlsAnimator;
    private final long mShowInputControlsAnimimationDuration;
    private final int mSolidColor;
    private SoundPool mSoundPool;
    private final Rect mTempRect;
    int mTextEnd;
    private final int mTextSize;
    int mTextStart;
    private int mTouchSlop;
    private int mValue;
    private VelocityTracker mVelocityTracker;
    private int mVisualWidth;
    private boolean mWrapSelectorWheel;

    public interface OnValueChangeListener {
        void onValueChange(OppoNumberPicker oppoNumberPicker, int i, int i2);
    }

    public interface Formatter {
        String format(int i);
    }

    class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int UNDEFINED = Integer.MIN_VALUE;
        private static final int VIRTUAL_VIEW_ID_DECREMENT = 0;
        private static final int VIRTUAL_VIEW_ID_INCREMENT = 2;
        private static final int VIRTUAL_VIEW_ID_INPUT = 1;
        private int mAccessibilityFocusedView = UNDEFINED;
        private final int[] mTempArray = new int[2];
        private final Rect mTempRect = new Rect();

        AccessibilityNodeProviderImpl() {
        }

        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            switch (virtualViewId) {
                case -1:
                    return createAccessibilityNodeInfoForNumberPicker(OppoNumberPicker.this.mScrollX, OppoNumberPicker.this.mScrollY, OppoNumberPicker.this.mScrollX + (OppoNumberPicker.this.mRight - OppoNumberPicker.this.mLeft), OppoNumberPicker.this.mScrollY + (OppoNumberPicker.this.mBottom - OppoNumberPicker.this.mTop));
                case 0:
                    return createAccessibilityNodeInfoForVirtualButton(0, getVirtualDecrementButtonText(), OppoNumberPicker.this.mScrollX, OppoNumberPicker.this.mScrollY, OppoNumberPicker.this.mScrollX + (OppoNumberPicker.this.mRight - OppoNumberPicker.this.mLeft), OppoNumberPicker.this.SELECTOR_MIDDLE_ITEM_INDEX * OppoNumberPicker.this.mSelectorElementHeight);
                case 1:
                    return createAccessibiltyNodeInfoForInputText(OppoNumberPicker.this.mScrollX, OppoNumberPicker.this.SELECTOR_MIDDLE_ITEM_INDEX * OppoNumberPicker.this.mSelectorElementHeight, OppoNumberPicker.this.mScrollX + (OppoNumberPicker.this.mRight - OppoNumberPicker.this.mLeft), (OppoNumberPicker.this.SELECTOR_MIDDLE_ITEM_INDEX + 1) * OppoNumberPicker.this.mSelectorElementHeight);
                case 2:
                    return createAccessibilityNodeInfoForVirtualButton(2, getVirtualIncrementButtonText(), OppoNumberPicker.this.mScrollX, (OppoNumberPicker.this.SELECTOR_MIDDLE_ITEM_INDEX + 1) * OppoNumberPicker.this.mSelectorElementHeight, OppoNumberPicker.this.mScrollX + (OppoNumberPicker.this.mRight - OppoNumberPicker.this.mLeft), OppoNumberPicker.this.mScrollY + (OppoNumberPicker.this.mBottom - OppoNumberPicker.this.mTop));
                default:
                    return super.createAccessibilityNodeInfo(virtualViewId);
            }
        }

        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String searched, int virtualViewId) {
            if (TextUtils.isEmpty(searched)) {
                return Collections.emptyList();
            }
            String searchedLowerCase = searched.toLowerCase();
            List<AccessibilityNodeInfo> result = new ArrayList();
            switch (virtualViewId) {
                case -1:
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 0, result);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 1, result);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, 2, result);
                    return result;
                case 0:
                case 1:
                case 2:
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, virtualViewId, result);
                    return result;
                default:
                    return super.findAccessibilityNodeInfosByText(searched, virtualViewId);
            }
        }

        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            switch (virtualViewId) {
                case -1:
                    switch (action) {
                        case 64:
                            if (this.mAccessibilityFocusedView == virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = virtualViewId;
                            OppoNumberPicker.this.requestAccessibilityFocus();
                            return true;
                        case 128:
                            if (this.mAccessibilityFocusedView != virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = UNDEFINED;
                            OppoNumberPicker.this.clearAccessibilityFocus();
                            return true;
                        case 4096:
                            if (!OppoNumberPicker.this.isEnabled() || (!OppoNumberPicker.this.getWrapSelectorWheel() && OppoNumberPicker.this.getValue() >= OppoNumberPicker.this.getMaxValue())) {
                                return false;
                            }
                            OppoNumberPicker.this.changeCurrentByOne(true);
                            return true;
                        case 8192:
                            if (!OppoNumberPicker.this.isEnabled() || (!OppoNumberPicker.this.getWrapSelectorWheel() && OppoNumberPicker.this.getValue() <= OppoNumberPicker.this.getMinValue())) {
                                return false;
                            }
                            OppoNumberPicker.this.changeCurrentByOne(false);
                            return true;
                    }
                    break;
                case 0:
                    setDecrementIdAction(virtualViewId, action, 0);
                    return false;
                case 1:
                    switch (action) {
                        case 1:
                            if (!OppoNumberPicker.this.isEnabled() || (OppoNumberPicker.this.mInputText.isFocused() ^ 1) == 0) {
                                return false;
                            }
                            return OppoNumberPicker.this.mInputText.requestFocus();
                        case 2:
                            if (!OppoNumberPicker.this.isEnabled() || !OppoNumberPicker.this.mInputText.isFocused()) {
                                return false;
                            }
                            OppoNumberPicker.this.mInputText.clearFocus();
                            return true;
                        case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                            if (!OppoNumberPicker.this.isEnabled()) {
                                return false;
                            }
                            OppoNumberPicker.this.performClick();
                            return true;
                        case ColorDialogUtil.BIT_FOUSED_BUTTON_NEUTRAL /*32*/:
                            if (!OppoNumberPicker.this.isEnabled()) {
                                return false;
                            }
                            OppoNumberPicker.this.performLongClick();
                            return true;
                        case 64:
                            if (this.mAccessibilityFocusedView == virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = virtualViewId;
                            sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                            OppoNumberPicker.this.mInputText.invalidate();
                            return true;
                        case 128:
                            if (this.mAccessibilityFocusedView != virtualViewId) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = UNDEFINED;
                            sendAccessibilityEventForVirtualView(virtualViewId, 65536);
                            OppoNumberPicker.this.mInputText.invalidate();
                            return true;
                        default:
                            return OppoNumberPicker.this.mInputText.performAccessibilityAction(action, arguments);
                    }
                case 2:
                    setIncrementIdAction(virtualViewId, action, 2);
                    return false;
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        private boolean setIncrementIdAction(int virtualViewId, int action, int index) {
            switch (action) {
                case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                    if (!OppoNumberPicker.this.isEnabled()) {
                        return false;
                    }
                    OppoNumberPicker.this.changeCurrentByOne(true);
                    sendAccessibilityEventForVirtualView(virtualViewId, 1);
                    return true;
                case 64:
                    if (this.mAccessibilityFocusedView == virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = virtualViewId;
                    sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                    OppoNumberPicker.this.invalidate(0, (OppoNumberPicker.this.SELECTOR_MIDDLE_ITEM_INDEX + 1) * OppoNumberPicker.this.mSelectorElementHeight, OppoNumberPicker.this.getRight(), OppoNumberPicker.this.mSelectorIndices.length * OppoNumberPicker.this.mSelectorElementHeight);
                    return true;
                case 128:
                    if (this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = UNDEFINED;
                    sendAccessibilityEventForVirtualView(virtualViewId, 65536);
                    OppoNumberPicker.this.invalidate(0, (OppoNumberPicker.this.SELECTOR_MIDDLE_ITEM_INDEX + 1) * OppoNumberPicker.this.mSelectorElementHeight, OppoNumberPicker.this.getRight(), OppoNumberPicker.this.mSelectorIndices.length * OppoNumberPicker.this.mSelectorElementHeight);
                    return true;
                default:
                    return false;
            }
        }

        private boolean setDecrementIdAction(int virtualViewId, int action, int index) {
            switch (action) {
                case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                    if (!OppoNumberPicker.this.isEnabled()) {
                        return false;
                    }
                    OppoNumberPicker.this.changeCurrentByOne(virtualViewId == index);
                    sendAccessibilityEventForVirtualView(virtualViewId, 1);
                    return true;
                case 64:
                    if (this.mAccessibilityFocusedView == virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = virtualViewId;
                    sendAccessibilityEventForVirtualView(virtualViewId, 32768);
                    OppoNumberPicker.this.invalidate(0, 0, OppoNumberPicker.this.getRight(), OppoNumberPicker.this.SELECTOR_MIDDLE_ITEM_INDEX * OppoNumberPicker.this.mSelectorElementHeight);
                    return true;
                case 128:
                    if (this.mAccessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    this.mAccessibilityFocusedView = UNDEFINED;
                    sendAccessibilityEventForVirtualView(virtualViewId, 65536);
                    OppoNumberPicker.this.invalidate(0, 0, OppoNumberPicker.this.getRight(), OppoNumberPicker.this.SELECTOR_MIDDLE_ITEM_INDEX * OppoNumberPicker.this.mSelectorElementHeight);
                    return true;
                default:
                    return false;
            }
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            switch (virtualViewId) {
                case 0:
                    if (hasVirtualDecrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualDecrementButtonText());
                        return;
                    }
                    return;
                case 1:
                    sendAccessibilityEventForVirtualText(eventType);
                    return;
                case 2:
                    if (hasVirtualIncrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualIncrementButtonText());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private void sendAccessibilityEventForVirtualText(int eventType) {
            if (AccessibilityManager.getInstance(OppoNumberPicker.this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                OppoNumberPicker.this.mInputText.onInitializeAccessibilityEvent(event);
                OppoNumberPicker.this.mInputText.onPopulateAccessibilityEvent(event);
                event.setSource(OppoNumberPicker.this, 1);
                OppoNumberPicker.this.requestSendAccessibilityEvent(OppoNumberPicker.this, event);
            }
        }

        private void sendAccessibilityEventForVirtualButton(int virtualViewId, int eventType, String text) {
            if (AccessibilityManager.getInstance(OppoNumberPicker.this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setClassName(Button.class.getName());
                event.setPackageName(OppoNumberPicker.this.mContext.getPackageName());
                event.getText().add(text);
                event.setEnabled(OppoNumberPicker.this.isEnabled());
                event.setSource(OppoNumberPicker.this, virtualViewId);
                OppoNumberPicker.this.requestSendAccessibilityEvent(OppoNumberPicker.this, event);
            }
        }

        private void findAccessibilityNodeInfosByTextInChild(String searchedLowerCase, int virtualViewId, List<AccessibilityNodeInfo> outResult) {
            String text;
            switch (virtualViewId) {
                case 0:
                    text = getVirtualDecrementButtonText();
                    if (!TextUtils.isEmpty(text) && text.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(0));
                    }
                    return;
                case 1:
                    CharSequence text2 = OppoNumberPicker.this.mInputText.getText();
                    if (TextUtils.isEmpty(text2) || !text2.toString().toLowerCase().contains(searchedLowerCase)) {
                        CharSequence contentDesc = OppoNumberPicker.this.mInputText.getText();
                        if (!TextUtils.isEmpty(contentDesc) && contentDesc.toString().toLowerCase().contains(searchedLowerCase)) {
                            outResult.add(createAccessibilityNodeInfo(1));
                            return;
                        }
                    }
                    outResult.add(createAccessibilityNodeInfo(1));
                    return;
                    break;
                case 2:
                    text = getVirtualIncrementButtonText();
                    if (!TextUtils.isEmpty(text) && text.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(2));
                    }
                    return;
            }
        }

        private AccessibilityNodeInfo createAccessibiltyNodeInfoForInputText(int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = OppoNumberPicker.this.mInputText.createAccessibilityNodeInfo();
            info.setSource(OppoNumberPicker.this, 1);
            if (this.mAccessibilityFocusedView != 1) {
                info.addAction(64);
            }
            if (this.mAccessibilityFocusedView == 1) {
                info.addAction(128);
            }
            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);
            info.setVisibleToUser(OppoNumberPicker.this.isVisibleToUser(boundsInParent));
            info.setText(getVirtualInputText());
            info.setBoundsInParent(boundsInParent);
            Rect boundsInScreen = boundsInParent;
            int[] locationOnScreen = this.mTempArray;
            OppoNumberPicker.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInParent);
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForVirtualButton(int virtualViewId, String text, int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(Button.class.getName());
            info.setPackageName(OppoNumberPicker.this.mContext.getPackageName());
            info.setSource(OppoNumberPicker.this, virtualViewId);
            info.setParent(OppoNumberPicker.this);
            info.setText(text);
            info.setClickable(true);
            info.setLongClickable(true);
            info.setEnabled(OppoNumberPicker.this.isEnabled());
            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);
            info.setVisibleToUser(OppoNumberPicker.this.isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            Rect boundsInScreen = boundsInParent;
            int[] locationOnScreen = this.mTempArray;
            OppoNumberPicker.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInParent);
            if (this.mAccessibilityFocusedView != virtualViewId) {
                info.addAction(64);
            }
            if (this.mAccessibilityFocusedView == virtualViewId) {
                info.addAction(128);
            }
            if (OppoNumberPicker.this.isEnabled()) {
                info.addAction(16);
            }
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForNumberPicker(int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(OppoNumberPicker.class.getName());
            info.setPackageName(OppoNumberPicker.this.mContext.getPackageName());
            info.setSource(OppoNumberPicker.this);
            if (hasVirtualDecrementButton()) {
                info.addChild(OppoNumberPicker.this, 0);
            }
            info.addChild(OppoNumberPicker.this, 1);
            if (hasVirtualIncrementButton()) {
                info.addChild(OppoNumberPicker.this, 0);
            }
            info.setParent((View) OppoNumberPicker.this.getParentForAccessibility());
            info.setEnabled(OppoNumberPicker.this.isEnabled());
            info.setScrollable(true);
            float applicationScale = OppoNumberPicker.this.getContext().getResources().getCompatibilityInfo().applicationScale;
            Rect boundsInParent = this.mTempRect;
            boundsInParent.set(left, top, right, bottom);
            boundsInParent.scale(applicationScale);
            info.setBoundsInParent(boundsInParent);
            info.setVisibleToUser(OppoNumberPicker.this.isVisibleToUser());
            Rect boundsInScreen = boundsInParent;
            int[] locationOnScreen = this.mTempArray;
            OppoNumberPicker.this.getLocationOnScreen(locationOnScreen);
            boundsInParent.offset(locationOnScreen[0], locationOnScreen[1]);
            boundsInParent.scale(applicationScale);
            info.setBoundsInScreen(boundsInParent);
            if (this.mAccessibilityFocusedView != -1) {
                info.addAction(64);
            }
            if (this.mAccessibilityFocusedView == -1) {
                info.addAction(128);
            }
            if (OppoNumberPicker.this.isEnabled()) {
                if (OppoNumberPicker.this.getWrapSelectorWheel() || OppoNumberPicker.this.getValue() < OppoNumberPicker.this.getMaxValue()) {
                    info.addAction(4096);
                }
                if (OppoNumberPicker.this.getWrapSelectorWheel() || OppoNumberPicker.this.getValue() > OppoNumberPicker.this.getMinValue()) {
                    info.addAction(8192);
                }
            }
            return info;
        }

        private boolean hasVirtualDecrementButton() {
            return OppoNumberPicker.this.getWrapSelectorWheel() || OppoNumberPicker.this.getValue() > OppoNumberPicker.this.getMinValue();
        }

        private boolean hasVirtualIncrementButton() {
            return OppoNumberPicker.this.getWrapSelectorWheel() || OppoNumberPicker.this.getValue() < OppoNumberPicker.this.getMaxValue();
        }

        private String getVirtualDecrementButtonText() {
            int value = OppoNumberPicker.this.mValue - 1;
            if (OppoNumberPicker.this.mWrapSelectorWheel) {
                value = OppoNumberPicker.this.getWrappedSelectorIndex(value);
            }
            if (value < OppoNumberPicker.this.mMinValue) {
                return null;
            }
            return OppoNumberPicker.this.mDisplayedValues == null ? OppoNumberPicker.this.formatNumber(value) : OppoNumberPicker.this.mDisplayedValues[value - OppoNumberPicker.this.mMinValue];
        }

        private String getVirtualIncrementButtonText() {
            int value = OppoNumberPicker.this.mValue + 1;
            if (OppoNumberPicker.this.mWrapSelectorWheel) {
                value = OppoNumberPicker.this.getWrappedSelectorIndex(value);
            }
            if (value > OppoNumberPicker.this.mMaxValue) {
                return null;
            }
            return OppoNumberPicker.this.mDisplayedValues == null ? OppoNumberPicker.this.formatNumber(value) : OppoNumberPicker.this.mDisplayedValues[value - OppoNumberPicker.this.mMinValue];
        }

        private String getVirtualInputText() {
            int value = OppoNumberPicker.this.mValue;
            if (OppoNumberPicker.this.mWrapSelectorWheel) {
                value = OppoNumberPicker.this.getWrappedSelectorIndex(value);
            }
            if (value > OppoNumberPicker.this.mMaxValue) {
                return null;
            }
            return OppoNumberPicker.this.mDisplayedValues == null ? OppoNumberPicker.this.formatNumber(value) : OppoNumberPicker.this.mDisplayedValues[value - OppoNumberPicker.this.mMinValue];
        }
    }

    class AdjustScrollerCommand implements Runnable {
        AdjustScrollerCommand() {
        }

        public void run() {
            OppoNumberPicker.this.mPreviousScrollerY = 0;
            if (OppoNumberPicker.this.mInitialScrollOffset == OppoNumberPicker.this.mCurrentScrollOffset) {
                OppoNumberPicker.this.updateInputTextView();
                OppoNumberPicker.this.showInputControls(OppoNumberPicker.this.mShowInputControlsAnimimationDuration);
                return;
            }
            int deltaY = OppoNumberPicker.this.mInitialScrollOffset - OppoNumberPicker.this.mCurrentScrollOffset;
            if (Math.abs(deltaY) > OppoNumberPicker.this.mSelectorElementHeight / 2) {
                deltaY += deltaY > 0 ? -OppoNumberPicker.this.mSelectorElementHeight : OppoNumberPicker.this.mSelectorElementHeight;
            }
            OppoNumberPicker.this.mAdjustScroller.startScroll(0, 0, 0, deltaY, OppoNumberPicker.SELECTOR_ADJUSTMENT_DURATION_MILLIS);
            OppoNumberPicker.this.invalidate();
        }
    }

    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        ChangeCurrentByOneFromLongPressCommand() {
        }

        private void setIncrement(boolean increment) {
            this.mIncrement = increment;
        }

        public void run() {
            OppoNumberPicker.this.changeCurrentByOne(this.mIncrement);
            OppoNumberPicker.this.postDelayed(this, OppoNumberPicker.this.mLongPressUpdateInterval);
        }
    }

    public static class CustomEditText extends EditText {
        public CustomEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void onEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
            if (actionCode == 6) {
                clearFocus();
            }
        }
    }

    class InputTextFilter extends NumberKeyListener {
        InputTextFilter() {
        }

        public int getInputType() {
            return 1;
        }

        protected char[] getAcceptedChars() {
            return OppoNumberPicker.DIGIT_CHARACTERS;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            CharSequence filtered;
            String result;
            if (OppoNumberPicker.this.mDisplayedValues == null) {
                filtered = super.filter(source, start, end, dest, dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }
                result = String.valueOf(dest.subSequence(0, dstart)) + filtered + dest.subSequence(dend, dest.length());
                if ("".equals(result)) {
                    return result;
                }
                if (OppoNumberPicker.this.getSelectedPos(result) > OppoNumberPicker.this.mMaxValue) {
                    return "";
                }
                return filtered;
            }
            filtered = String.valueOf(source.subSequence(start, end));
            if (TextUtils.isEmpty(filtered)) {
                return "";
            }
            result = String.valueOf(dest.subSequence(0, dstart)) + filtered + dest.subSequence(dend, dest.length());
            String str = String.valueOf(result).toLowerCase();
            for (String val : OppoNumberPicker.this.mDisplayedValues) {
                if (val.toLowerCase().startsWith(str)) {
                    OppoNumberPicker.this.postSetSelectionCommand(result.length(), val.length());
                    return val.subSequence(dstart, val.length());
                }
            }
            return "";
        }
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScrollStateChange(OppoNumberPicker oppoNumberPicker, int i);
    }

    class SetSelectionCommand implements Runnable {
        private int mSelectionEnd;
        private int mSelectionStart;

        SetSelectionCommand() {
        }

        public void run() {
        }
    }

    public OppoNumberPicker(Context context) {
        this(context, null);
    }

    public OppoNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 201393153);
    }

    public OppoNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRedStart = 74;
        this.mGreenStart = 74;
        this.mBlueStart = 74;
        this.mRedEnd = 11;
        this.mGreenEnd = 152;
        this.mBlueEnd = 74;
        this.mTextStart = 0;
        this.mTextEnd = 0;
        this.mFocusTextSize = 0;
        this.SELECTOR_MIDDLE_ITEM_INDEX = 3;
        this.mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;
        this.mSelectorIndexToStringCache = new SparseArray();
        int[] iArr = new int[7];
        this.mSelectorIndices = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        this.mInitialScrollOffset = Integer.MIN_VALUE;
        this.mTempRect = new Rect();
        this.mScrollState = 0;
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker, defStyle, 0);
        this.mSolidColor = attributesArray.getColor(0, 0);
        this.mFlingable = true;
        attributesArray.recycle();
        this.mSoundPool = new SoundPool(1, 1, 3);
        this.mFilePath = Environment.getRootDirectory() + "/media/audio/ui/numberpicker_click.ogg";
        this.mClickSoundId = this.mSoundPool.load(this.mFilePath, 0);
        TypedArray oppoAttributesArray = context.obtainStyledAttributes(attrs, oppo.R.styleable.OppoNumberPicker, defStyle, 0);
        this.mNumberPickerPaddingLeft = oppoAttributesArray.getDimensionPixelSize(0, 0);
        this.mNumberPickerPaddingRight = oppoAttributesArray.getDimensionPixelSize(1, 0);
        this.mMinHeight = oppoAttributesArray.getDimensionPixelSize(10, -1);
        this.mMaxHeight = oppoAttributesArray.getDimensionPixelSize(11, -1);
        if (this.mMinHeight == -1 || this.mMaxHeight == -1 || this.mMinHeight <= this.mMaxHeight) {
            this.mMinWidth = oppoAttributesArray.getDimensionPixelSize(12, -1);
            this.mMaxWidth = oppoAttributesArray.getDimensionPixelSize(13, -1);
            this.mTextSize = oppoAttributesArray.getDimensionPixelSize(14, -1);
            this.mTextStart = oppoAttributesArray.getDimensionPixelSize(2, -1);
            this.mTextEnd = oppoAttributesArray.getDimensionPixelSize(3, -1);
            this.mFocusTextSize = oppoAttributesArray.getDimensionPixelSize(4, -1);
            this.mVisualWidth = oppoAttributesArray.getDimensionPixelSize(16, -1);
            this.mAlignPosition = oppoAttributesArray.getInteger(17, -1);
            this.mIsBold = oppoAttributesArray.getBoolean(15, true);
            int startColor = oppoAttributesArray.getColor(5, -1);
            int endColor = oppoAttributesArray.getColor(6, -1);
            setPickerRowNumber(oppoAttributesArray.getInt(7, -1));
            if (this.mMinWidth == -1 || this.mMaxWidth == -1 || this.mMinWidth <= this.mMaxWidth) {
                this.mComputeMaxWidth = this.mMaxWidth == Integer.MAX_VALUE;
                oppoAttributesArray.recycle();
                this.mShowInputControlsAnimimationDuration = (long) getResources().getInteger(17694722);
                setWillNotDraw(false);
                setSelectorWheelState(0);
                ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(201917440, this, true);
                OnClickListener onClickListener = new OnClickListener() {
                    public void onClick(View v) {
                        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
                        if (inputMethodManager != null && inputMethodManager.isActive(OppoNumberPicker.this.mInputText)) {
                            inputMethodManager.hideSoftInputFromWindow(OppoNumberPicker.this.getWindowToken(), 0);
                        }
                        OppoNumberPicker.this.mInputText.clearFocus();
                        if (v.getId() == 201458726) {
                            OppoNumberPicker.this.changeCurrentByOne(true);
                        } else {
                            OppoNumberPicker.this.changeCurrentByOne(false);
                        }
                    }
                };
                OnLongClickListener onLongClickListener = new OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        OppoNumberPicker.this.mInputText.clearFocus();
                        if (v.getId() == 201458726) {
                            OppoNumberPicker.this.postChangeCurrentByOneFromLongPress(true);
                        } else {
                            OppoNumberPicker.this.postChangeCurrentByOneFromLongPress(false);
                        }
                        return true;
                    }
                };
                this.mIncrementButton = (ImageButton) findViewById(201458726);
                this.mIncrementButton.setOnClickListener(onClickListener);
                this.mIncrementButton.setOnLongClickListener(onLongClickListener);
                this.mDecrementButton = (ImageButton) findViewById(201458727);
                this.mDecrementButton.setOnClickListener(onClickListener);
                this.mDecrementButton.setOnLongClickListener(onLongClickListener);
                this.mInputText = (EditText) findViewById(201458728);
                this.mInputText.setTextColor(-1);
                this.mInputText.setFilters(new InputFilter[]{new InputTextFilter()});
                this.mInputText.setRawInputType(2);
                this.mInputText.setImeOptions(6);
                ViewConfiguration configuration = ViewConfiguration.get(context);
                this.mTouchSlop = configuration.getScaledTouchSlop();
                this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
                this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity() / 8;
                this.mLunarLeap11 = getContext().getResources().getString(201589762);
                this.mLunarLeap12 = getContext().getResources().getString(201589763);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setTextAlign(Align.CENTER);
                paint.setTextSize((float) this.mTextSize);
                paint.setTypeface(this.mInputText.getTypeface());
                this.mChangeIndexDistance = (int) getContext().getResources().getDimension(201655479);
                this.mRedStart = Color.red(startColor);
                this.mRedEnd = Color.red(endColor);
                this.mGreenStart = Color.green(startColor);
                this.mGreenEnd = Color.green(endColor);
                this.mBlueStart = Color.blue(startColor);
                this.mBlueEnd = Color.blue(endColor);
                paint.setColor(endColor);
                paint.setFakeBoldText(this.mIsBold);
                this.mSelectorWheelPaint = paint;
                this.mDimSelectorWheelAnimator = ObjectAnimator.ofInt(this, PROPERTY_SELECTOR_PAINT_ALPHA, new int[]{255, 255});
                ObjectAnimator showIncrementButton = ObjectAnimator.ofFloat(this.mIncrementButton, PROPERTY_BUTTON_ALPHA, new float[]{0.0f, 1.0f});
                final ObjectAnimator showDecrementButton = ObjectAnimator.ofFloat(this.mDecrementButton, PROPERTY_BUTTON_ALPHA, new float[]{0.0f, 1.0f});
                this.mShowInputControlsAnimator = new AnimatorSet();
                this.mShowInputControlsAnimator.playTogether(new Animator[]{this.mDimSelectorWheelAnimator, showIncrementButton, showDecrementButton});
                final ObjectAnimator objectAnimator = showIncrementButton;
                this.mShowInputControlsAnimator.addListener(new AnimatorListenerAdapter() {
                    private boolean mCanceled = false;

                    public void onAnimationEnd(Animator animation) {
                        if (!this.mCanceled) {
                            OppoNumberPicker.this.setSelectorWheelState(1);
                        }
                        this.mCanceled = false;
                        objectAnimator.setCurrentPlayTime(objectAnimator.getDuration());
                        showDecrementButton.setCurrentPlayTime(showDecrementButton.getDuration());
                    }

                    public void onAnimationCancel(Animator animation) {
                        if (OppoNumberPicker.this.mShowInputControlsAnimator.isRunning()) {
                            this.mCanceled = true;
                        }
                    }
                });
                this.mFlingScroller = new Scroller(getContext(), null, true);
                this.mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));
                updateInputTextView();
                updateIncrementAndDecrementButtonsVisibilityState();
                if (this.mFlingable) {
                    if (isInEditMode()) {
                        setSelectorWheelState(1);
                    } else {
                        setSelectorWheelState(2);
                        hideInputControls();
                    }
                }
                if (getImportantForAccessibility() == 0) {
                    setImportantForAccessibility(1);
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("minWidth > maxWidth");
        }
        throw new IllegalArgumentException("minHeight > maxHeight");
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int msrdWdth = getMeasuredWidth();
        int msrdHght = getMeasuredHeight();
        int decrBtnMsrdWdth = this.mDecrementButton.getMeasuredWidth();
        int decrBtnLeft = (msrdWdth - decrBtnMsrdWdth) / 2;
        this.mDecrementButton.layout(decrBtnLeft, 0, decrBtnLeft + decrBtnMsrdWdth, this.mDecrementButton.getMeasuredHeight() + 0);
        int inptTxtMsrdWdth = this.mInputText.getMeasuredWidth();
        int inptTxtMsrdHght = this.mInputText.getMeasuredHeight();
        int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        this.mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtLeft + inptTxtMsrdWdth, inptTxtTop + inptTxtMsrdHght);
        int incrBtnLeft = (msrdWdth - this.mIncrementButton.getMeasuredWidth()) / 2;
        int incrBtnBottom = msrdHght;
        this.mIncrementButton.layout(incrBtnLeft, msrdHght - this.mIncrementButton.getMeasuredHeight(), incrBtnLeft + decrBtnMsrdWdth, msrdHght);
        if (!this.mScrollWheelAndFadingEdgesInitialized) {
            this.mScrollWheelAndFadingEdgesInitialized = true;
            initializeSelectorWheel();
            initializeFadingEdges();
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        this.mScrollWheelAndFadingEdgesInitialized = false;
        super.onConfigurationChanged(newConfig);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(makeMeasureSpec(widthMeasureSpec, this.mMaxWidth), makeMeasureSpec(heightMeasureSpec, this.mMaxHeight));
        setMeasuredDimension(resolveSizeAndStateRespectingMinSize(this.mMinWidth, getMeasuredWidth(), widthMeasureSpec) + ((this.mNumberPickerPaddingRight + this.mNumberPickerPaddingLeft) * 2), resolveSizeAndStateRespectingMinSize(this.mMinHeight, getMeasuredHeight(), heightMeasureSpec));
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || (this.mFlingable ^ 1) != 0) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
                float y = event.getY();
                this.mLastDownEventY = y;
                this.mLastMotionEventY = y;
                removeAllCallbacks();
                this.mShowInputControlsAnimator.cancel();
                this.mDimSelectorWheelAnimator.cancel();
                this.mCheckBeginEditOnUpEvent = false;
                this.mAdjustScrollerOnUpEvent = true;
                if (this.mSelectorWheelState == 2) {
                    boolean scrollersFinished;
                    this.mSelectorWheelPaint.setAlpha(255);
                    if (this.mFlingScroller.isFinished()) {
                        scrollersFinished = this.mAdjustScroller.isFinished();
                    } else {
                        scrollersFinished = false;
                    }
                    if (!scrollersFinished) {
                        this.mFlingScroller.forceFinished(true);
                        this.mAdjustScroller.forceFinished(true);
                        onScrollStateChange(0);
                    }
                    this.mCheckBeginEditOnUpEvent = scrollersFinished;
                    this.mAdjustScrollerOnUpEvent = true;
                    hideInputControls();
                    return true;
                } else if (isEventInVisibleViewHitRect(event, this.mIncrementButton) || isEventInVisibleViewHitRect(event, this.mDecrementButton)) {
                    return false;
                } else {
                    this.mAdjustScrollerOnUpEvent = false;
                    setSelectorWheelState(2);
                    hideInputControls();
                    return true;
                }
            case 2:
                if (((int) Math.abs(event.getY() - this.mLastDownEventY)) > this.mTouchSlop) {
                    this.mCheckBeginEditOnUpEvent = false;
                    onScrollStateChange(1);
                    setSelectorWheelState(2);
                    hideInputControls();
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        switch (ev.getActionMasked()) {
            case 1:
                if (this.mCheckBeginEditOnUpEvent) {
                    this.mCheckBeginEditOnUpEvent = false;
                    if (ev.getEventTime() - this.mLastUpEventTimeMillis < ((long) ViewConfiguration.getDoubleTapTimeout())) {
                        setSelectorWheelState(1);
                        showInputControls(this.mShowInputControlsAnimimationDuration);
                        this.mLastUpEventTimeMillis = ev.getEventTime();
                        return true;
                    }
                }
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > this.mMinimumFlingVelocity) {
                    fling(initialVelocity * 3);
                    onScrollStateChange(2);
                } else if (!this.mAdjustScrollerOnUpEvent) {
                    postAdjustScrollerCommand(SHOW_INPUT_CONTROLS_DELAY_MILLIS);
                } else if (this.mFlingScroller.isFinished() && this.mAdjustScroller.isFinished()) {
                    postAdjustScrollerCommand(0);
                }
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
                this.mLastUpEventTimeMillis = ev.getEventTime();
                break;
            case 2:
                float currentMoveY = ev.getY();
                if ((this.mCheckBeginEditOnUpEvent || this.mScrollState != 1) && ((int) Math.abs(currentMoveY - this.mLastDownEventY)) > this.mTouchSlop) {
                    this.mCheckBeginEditOnUpEvent = false;
                    onScrollStateChange(1);
                }
                scrollBy(0, (int) (currentMoveY - this.mLastMotionEventY));
                invalidate();
                this.mLastMotionEventY = currentMoveY;
                break;
            case 3:
                if (this.mAdjustScrollerOnUpEvent) {
                    if (this.mFlingScroller.isFinished() && this.mAdjustScroller.isFinished()) {
                        postAdjustScrollerCommand(0);
                        break;
                    }
                }
                postAdjustScrollerCommand(SHOW_INPUT_CONTROLS_DELAY_MILLIS);
                break;
        }
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 1:
            case 3:
                removeAllCallbacks();
                break;
            case 2:
                if (this.mSelectorWheelState == 2) {
                    removeAllCallbacks();
                    forceCompleteChangeCurrentByOneViaScroll();
                    break;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 23 || keyCode == 66) {
            removeAllCallbacks();
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 3 || action == 1) {
            removeAllCallbacks();
        }
        return super.dispatchTrackballEvent(event);
    }

    public void computeScroll() {
        if (this.mSelectorWheelState != 0) {
            Scroller scroller = this.mFlingScroller;
            if (scroller.isFinished()) {
                scroller = this.mAdjustScroller;
                if (scroller.isFinished()) {
                    return;
                }
            }
            scroller.computeScrollOffset();
            int currentScrollerY = scroller.getCurrY();
            if (this.mPreviousScrollerY == 0) {
                this.mPreviousScrollerY = scroller.getStartY();
            }
            scrollBy(0, currentScrollerY - this.mPreviousScrollerY);
            this.mPreviousScrollerY = currentScrollerY;
            if (scroller.isFinished()) {
                onScrollerFinished(scroller);
            } else {
                postInvalidate();
            }
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mIncrementButton.setEnabled(enabled);
        this.mDecrementButton.setEnabled(enabled);
        this.mInputText.setEnabled(enabled);
    }

    public void scrollBy(int x, int y) {
        if (this.mSelectorWheelState != 0) {
            int[] selectorIndices = this.mSelectorIndices;
            if (!this.mWrapSelectorWheel && y > 0 && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] <= this.mMinValue) {
                this.mCurrentScrollOffset = this.mInitialScrollOffset;
            } else if (this.mWrapSelectorWheel || y >= 0 || selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] < this.mMaxValue) {
                this.mCurrentScrollOffset += y;
                while (this.mCurrentScrollOffset - this.mInitialScrollOffset > this.mSelectorTextGapHeight + this.mChangeIndexDistance) {
                    this.mCurrentScrollOffset -= this.mSelectorElementHeight;
                    decrementSelectorIndices(selectorIndices);
                    changeCurrent(selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX]);
                    if (!this.mWrapSelectorWheel && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] <= this.mMinValue) {
                        this.mCurrentScrollOffset = this.mInitialScrollOffset;
                    }
                }
                while (this.mCurrentScrollOffset - this.mInitialScrollOffset < (-(this.mSelectorTextGapHeight + this.mChangeIndexDistance))) {
                    this.mCurrentScrollOffset += this.mSelectorElementHeight;
                    incrementSelectorIndices(selectorIndices);
                    changeCurrent(selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX]);
                    if (!this.mWrapSelectorWheel && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] >= this.mMaxValue) {
                        this.mCurrentScrollOffset = this.mInitialScrollOffset;
                    }
                }
            } else {
                this.mCurrentScrollOffset = this.mInitialScrollOffset;
            }
        }
    }

    public int getSolidColor() {
        return this.mSolidColor;
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        this.mOnValueChangeListener = onValueChangedListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    public void setFormatter(Formatter formatter) {
        if (formatter != this.mFormatter) {
            this.mFormatter = formatter;
            initializeSelectorWheelIndices();
            updateInputTextView();
        }
    }

    public void updateString() {
        initializeSelectorWheelIndices();
        updateInputTextView();
    }

    public void setValue(int value) {
        if (this.mValue == value) {
            invalidate();
            return;
        }
        if (value < this.mMinValue) {
            value = this.mWrapSelectorWheel ? this.mMaxValue : this.mMinValue;
        }
        if (value > this.mMaxValue) {
            value = this.mWrapSelectorWheel ? this.mMinValue : this.mMaxValue;
        }
        this.mValue = value;
        initializeSelectorWheelIndices();
        updateInputTextView();
        updateIncrementAndDecrementButtonsVisibilityState();
        invalidate();
    }

    private void tryComputeMaxWidth() {
        if (this.mComputeMaxWidth) {
            int maxTextWidth = 0;
            int i;
            if (this.mDisplayedValues == null) {
                float maxDigitWidth = 0.0f;
                for (i = 0; i <= 9; i++) {
                    float digitWidth = this.mSelectorWheelPaint.measureText(String.valueOf(i));
                    if (digitWidth > maxDigitWidth) {
                        maxDigitWidth = digitWidth;
                    }
                }
                int numberOfDigits = 0;
                for (int current = this.mMaxValue; current > 0; current /= 10) {
                    numberOfDigits++;
                }
                maxTextWidth = (int) (((float) numberOfDigits) * maxDigitWidth);
            } else {
                for (String measureText : this.mDisplayedValues) {
                    float textWidth = this.mSelectorWheelPaint.measureText(measureText);
                    if (textWidth > ((float) maxTextWidth)) {
                        maxTextWidth = (int) textWidth;
                    }
                }
            }
            maxTextWidth += this.mInputText.getPaddingLeft() + this.mInputText.getPaddingRight();
            if (this.mMaxWidth != maxTextWidth) {
                if (maxTextWidth > this.mMinWidth) {
                    this.mMaxWidth = maxTextWidth;
                } else {
                    this.mMaxWidth = this.mMinWidth;
                }
                invalidate();
            }
        }
    }

    public boolean getWrapSelectorWheel() {
        return this.mWrapSelectorWheel;
    }

    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        if (wrapSelectorWheel && this.mMaxValue - this.mMinValue < this.mSelectorIndices.length) {
            throw new IllegalStateException("Range less than selector items count.");
        } else if (wrapSelectorWheel != this.mWrapSelectorWheel) {
            this.mWrapSelectorWheel = wrapSelectorWheel;
            updateIncrementAndDecrementButtonsVisibilityState();
        }
    }

    public void setOnLongPressUpdateInterval(long intervalMillis) {
        this.mLongPressUpdateInterval = intervalMillis;
    }

    public int getValue() {
        return this.mValue;
    }

    public int getMinValue() {
        return this.mMinValue;
    }

    public void setMinValue(int minValue) {
        if (this.mMinValue != minValue) {
            if (minValue < 0) {
                throw new IllegalArgumentException("minValue must be >= 0");
            }
            this.mMinValue = minValue;
            if (this.mMinValue > this.mValue) {
                this.mValue = this.mMinValue;
            }
            setWrapSelectorWheel(this.mMaxValue - this.mMinValue > this.mSelectorIndices.length);
            initializeSelectorWheelIndices();
            updateInputTextView();
            tryComputeMaxWidth();
        }
    }

    public void setAlignPosition(int position) {
        this.mAlignPosition = position;
    }

    public int getMaxValue() {
        return this.mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        if (this.mMaxValue != maxValue) {
            if (maxValue < 0) {
                throw new IllegalArgumentException("maxValue must be >= 0");
            }
            this.mMaxValue = maxValue;
            if (this.mMaxValue < this.mValue) {
                this.mValue = this.mMaxValue;
            }
            setWrapSelectorWheel(this.mMaxValue - this.mMinValue > this.mSelectorIndices.length);
            initializeSelectorWheelIndices();
            updateInputTextView();
            tryComputeMaxWidth();
        }
    }

    public String[] getDisplayedValues() {
        return this.mDisplayedValues;
    }

    public void setDisplayedValues(String[] displayedValues) {
        if (this.mDisplayedValues != displayedValues) {
            this.mDisplayedValues = displayedValues;
            if (this.mDisplayedValues != null) {
                this.mInputText.setRawInputType(524289);
            } else {
                this.mInputText.setRawInputType(2);
            }
            updateInputTextView();
            initializeSelectorWheelIndices();
            tryComputeMaxWidth();
        }
    }

    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mFlingable && (isInEditMode() ^ 1) != 0) {
            showInputControls(this.mShowInputControlsAnimimationDuration * 2);
        }
        if (this.mSoundPool == null) {
            this.mSoundPool = new SoundPool(1, 1, 3);
            this.mClickSoundId = this.mSoundPool.load(this.mFilePath, 0);
        }
    }

    protected void onDetachedFromWindow() {
        removeAllCallbacks();
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
    }

    protected void dispatchDraw(Canvas canvas) {
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mShowInputControlsAnimator.isRunning() || this.mSelectorWheelState != 2) {
            long drawTime = getDrawingTime();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (getChildAt(i).isShown()) {
                    drawChild(canvas, getChildAt(i), drawTime);
                }
            }
        }
    }

    private int gradualChange(int start, int end, float distance) {
        if (((double) distance) <= ((double) this.mInitialScrollOffset) + (((double) this.mSelectorElementHeight) * (((double) this.SELECTOR_MIDDLE_ITEM_INDEX) - 0.5d)) || ((double) distance) >= ((double) this.mInitialScrollOffset) + (((double) this.mSelectorElementHeight) * (((double) this.SELECTOR_MIDDLE_ITEM_INDEX) + 0.5d))) {
            return start;
        }
        return end - ((int) ((((float) ((end - start) * 2)) * Math.abs((distance - ((float) this.mInitialScrollOffset)) - ((float) (this.SELECTOR_MIDDLE_ITEM_INDEX * this.mSelectorElementHeight)))) / ((float) this.mSelectorElementHeight)));
    }

    private int gradualChangeTextSize(int start, int end, int normalStart, int normalEnd, float distance) {
        int middleItemY = this.mInitialScrollOffset + (this.SELECTOR_MIDDLE_ITEM_INDEX * this.mSelectorElementHeight);
        int firstItemY = this.mInitialScrollOffset;
        int lastItemY = this.mInitialScrollOffset + ((this.mSelectorIndices.length - 1) * this.mSelectorElementHeight);
        if (((double) distance) > ((double) middleItemY) - (((double) this.mSelectorElementHeight) * 0.5d) && ((double) distance) < ((double) middleItemY) + (((double) this.mSelectorElementHeight) * 0.5d)) {
            return end - ((int) ((((float) ((end - start) * 2)) * Math.abs(distance - ((float) middleItemY))) / ((float) this.mSelectorElementHeight)));
        }
        if (distance <= ((float) (middleItemY - this.mSelectorElementHeight))) {
            return (int) (((float) normalStart) + ((((((float) (normalEnd - normalStart)) * 1.0f) * (distance - ((float) firstItemY))) / ((float) this.mSelectorElementHeight)) / 2.0f));
        }
        if (distance >= ((float) (this.mSelectorElementHeight + middleItemY))) {
            return (int) (((float) normalStart) + ((((((float) (normalEnd - normalStart)) * 1.0f) * (((float) lastItemY) - distance)) / ((float) this.mSelectorElementHeight)) / 2.0f));
        }
        return normalEnd;
    }

    public void setNumberPickerPaddingLeft(int i) {
        this.mNumberPickerPaddingLeft = i;
    }

    public void setNumberPickerPaddingRight(int i) {
        this.mNumberPickerPaddingRight = i;
    }

    public void setPickerRowNumber(int number) {
        this.mSelectorIndices = new int[number];
        for (int i = 0; i < number; i++) {
            this.mSelectorIndices[i] = Integer.MIN_VALUE;
        }
        this.SELECTOR_MIDDLE_ITEM_INDEX = this.mSelectorIndices.length / 2;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mSelectorWheelState != 0) {
            float x = (float) ((this.mRight - this.mLeft) / 2);
            if (this.mVisualWidth != -1 && this.mVisualWidth < this.mRight - this.mLeft) {
                switch (this.mAlignPosition) {
                    case 1:
                        x = (float) (this.mVisualWidth / 2);
                        break;
                    case 2:
                        x = (float) (((this.mRight - this.mLeft) - this.mVisualWidth) + (this.mVisualWidth / 2));
                        break;
                }
            }
            float y = (float) this.mCurrentScrollOffset;
            if (this.mNumberPickerPaddingLeft != 0) {
                x += (float) this.mNumberPickerPaddingLeft;
            }
            if (this.mNumberPickerPaddingRight != 0) {
                x -= (float) this.mNumberPickerPaddingRight;
            }
            int restoreCount = canvas.save();
            int[] selectorIndices = this.mSelectorIndices;
            for (int i = 0; i < selectorIndices.length; i++) {
                String scrollSelectorValue = (String) this.mSelectorIndexToStringCache.get(selectorIndices[i]);
                if (i != this.SELECTOR_MIDDLE_ITEM_INDEX || this.mInputText.getVisibility() != 0) {
                    int red = gradualChange(this.mRedStart, this.mRedEnd, y);
                    int green = gradualChange(this.mGreenStart, this.mGreenEnd, y);
                    int blue = gradualChange(this.mBlueStart, this.mBlueEnd, y);
                    int f = gradualChangeTextSize(this.mTextEnd, this.mFocusTextSize, this.mTextStart, this.mTextEnd, y);
                    this.mSelectorWheelPaint.setColor(Color.rgb(red, green, blue));
                    if (scrollSelectorValue.trim().equals(this.mLunarLeap11.trim()) || scrollSelectorValue.trim().equals(this.mLunarLeap12.trim())) {
                        this.mSelectorWheelPaint.setTextSize((float) ((f * 3) / 4));
                    } else {
                        this.mSelectorWheelPaint.setTextSize((float) f);
                    }
                    FontMetrics fmi = this.mSelectorWheelPaint.getFontMetrics();
                    canvas.drawText(scrollSelectorValue, x, (float) (((int) ((((y + y) + ((float) this.mSelectorElementHeight)) - fmi.top) - fmi.bottom)) / 2), this.mSelectorWheelPaint);
                }
                y += (float) this.mSelectorElementHeight;
            }
            canvas.restoreToCount(restoreCount);
        }
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case Integer.MIN_VALUE:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), 1073741824);
            case 0:
                return MeasureSpec.makeMeasureSpec(maxSize, 1073741824);
            case 1073741824:
                return measureSpec;
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        if (minSize != -1) {
            return resolveSizeAndState(Math.max(minSize, measuredSize), measureSpec, 0);
        }
        return measuredSize;
    }

    private void initializeSelectorWheelIndices() {
        this.mSelectorIndexToStringCache.clear();
        int[] selectorIdices = this.mSelectorIndices;
        int current = getValue();
        for (int i = 0; i < this.mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - this.SELECTOR_MIDDLE_ITEM_INDEX);
            if (this.mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            this.mSelectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(this.mSelectorIndices[i]);
        }
    }

    private void changeCurrent(int current) {
        if (this.mValue != current) {
            if (this.mWrapSelectorWheel) {
                current = getWrappedSelectorIndex(current);
            }
            int previous = this.mValue;
            setValue(current);
            notifyChange(previous, current);
            playSoundEffect();
        }
    }

    private void changeCurrentByOne(boolean increment) {
        if (this.mFlingable) {
            this.mDimSelectorWheelAnimator.cancel();
            this.mInputText.setVisibility(4);
            this.mSelectorWheelPaint.setAlpha(255);
            this.mPreviousScrollerY = 0;
            if (!ColorAccessibilityUtil.isTalkbackEnabled(getContext())) {
                forceCompleteChangeCurrentByOneViaScroll();
            } else if (!moveToFinalScrollerPosition(this.mFlingScroller)) {
                moveToFinalScrollerPosition(this.mAdjustScroller);
            }
            if (increment) {
                this.mFlingScroller.startScroll(0, 0, 0, -this.mSelectorElementHeight, 300);
            } else {
                this.mFlingScroller.startScroll(0, 0, 0, this.mSelectorElementHeight, 300);
            }
            invalidate();
        } else if (increment) {
            changeCurrent(this.mValue + 1);
        } else {
            changeCurrent(this.mValue - 1);
        }
    }

    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        int overshootAdjustment = this.mInitialScrollOffset - ((this.mCurrentScrollOffset + amountToScroll) % this.mSelectorElementHeight);
        if (overshootAdjustment == 0) {
            return false;
        }
        if (Math.abs(overshootAdjustment) > this.mSelectorElementHeight / 2) {
            if (overshootAdjustment > 0) {
                overshootAdjustment -= this.mSelectorElementHeight;
            } else {
                overshootAdjustment += this.mSelectorElementHeight;
            }
        }
        scrollBy(0, amountToScroll + overshootAdjustment);
        return true;
    }

    private void forceCompleteChangeCurrentByOneViaScroll() {
        Scroller scroller = this.mFlingScroller;
        if (!scroller.isFinished()) {
            int yBeforeAbort = scroller.getCurrY();
            scroller.abortAnimation();
            scrollBy(0, scroller.getCurrY() - yBeforeAbort);
        }
    }

    private void setSelectorPaintAlpha(int alpha) {
        this.mSelectorWheelPaint.setAlpha(alpha);
        invalidate();
    }

    private boolean isEventInVisibleViewHitRect(MotionEvent event, View view) {
        if (view.getVisibility() != 0) {
            return false;
        }
        view.getHitRect(this.mTempRect);
        return this.mTempRect.contains((int) event.getX(), (int) event.getY());
    }

    private void setSelectorWheelState(int selectorWheelState) {
        this.mSelectorWheelState = selectorWheelState;
        if (selectorWheelState == 2) {
            this.mSelectorWheelPaint.setAlpha(255);
        }
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = this.mSelectorIndices;
        this.mSelectorTextGapHeight = (int) ((((float) ((this.mBottom - this.mTop) - (selectorIndices.length * this.mTextSize))) / ((float) selectorIndices.length)) + 0.5f);
        this.mSelectorElementHeight = this.mTextSize + this.mSelectorTextGapHeight;
        int editTextTextPosition = this.mInputText.getBaseline() + this.mInputText.getTop();
        this.mInitialScrollOffset = 0;
        this.mCurrentScrollOffset = this.mInitialScrollOffset;
        updateInputTextView();
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(((this.mBottom - this.mTop) - this.mTextSize) / 2);
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller != this.mFlingScroller) {
            updateInputTextView();
            showInputControls(this.mShowInputControlsAnimimationDuration);
        } else if (this.mSelectorWheelState == 2) {
            postAdjustScrollerCommand(0);
            onScrollStateChange(0);
        } else {
            updateInputTextView();
            fadeSelectorWheel(this.mShowInputControlsAnimimationDuration);
        }
    }

    private void onScrollStateChange(int scrollState) {
        if (this.mScrollState != scrollState) {
            this.mScrollState = scrollState;
            if (this.mOnScrollListener != null) {
                this.mOnScrollListener.onScrollStateChange(this, scrollState);
            }
        }
    }

    private void fling(int velocityY) {
        this.mPreviousScrollerY = 0;
        if (velocityY > 0) {
            this.mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            this.mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }
        invalidate();
    }

    private void hideInputControls() {
        this.mShowInputControlsAnimator.cancel();
        this.mIncrementButton.setVisibility(4);
        this.mDecrementButton.setVisibility(4);
        this.mInputText.setVisibility(4);
    }

    private void showInputControls(long animationDuration) {
        updateIncrementAndDecrementButtonsVisibilityState();
        this.mShowInputControlsAnimator.setDuration(animationDuration);
        this.mShowInputControlsAnimator.start();
    }

    private void fadeSelectorWheel(long animationDuration) {
        this.mDimSelectorWheelAnimator.setDuration(animationDuration);
        this.mDimSelectorWheelAnimator.start();
    }

    private void updateIncrementAndDecrementButtonsVisibilityState() {
        if (this.mWrapSelectorWheel || this.mValue < this.mMaxValue) {
            this.mIncrementButton.setVisibility(0);
        } else {
            this.mIncrementButton.setVisibility(4);
        }
        if (this.mWrapSelectorWheel || this.mValue > this.mMinValue) {
            this.mDecrementButton.setVisibility(0);
        } else {
            this.mDecrementButton.setVisibility(4);
        }
    }

    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > this.mMaxValue) {
            return (this.mMinValue + ((selectorIndex - this.mMaxValue) % (this.mMaxValue - this.mMinValue))) - 1;
        }
        if (selectorIndex < this.mMinValue) {
            return (this.mMaxValue - ((this.mMinValue - selectorIndex) % (this.mMaxValue - this.mMinValue))) + 1;
        }
        return selectorIndex;
    }

    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex > this.mMaxValue) {
            nextScrollSelectorIndex = this.mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex < this.mMinValue) {
            nextScrollSelectorIndex = this.mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = this.mSelectorIndexToStringCache;
        String scrollSelectorValue = (String) cache.get(selectorIndex);
        if (scrollSelectorValue == null) {
            if (selectorIndex < this.mMinValue || selectorIndex > this.mMaxValue) {
                scrollSelectorValue = "";
            } else if (this.mDisplayedValues != null) {
                int displayedValueIndex = selectorIndex - this.mMinValue;
                if (this.mDisplayedValues.length > displayedValueIndex) {
                    scrollSelectorValue = this.mDisplayedValues[displayedValueIndex];
                }
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
            cache.put(selectorIndex, scrollSelectorValue);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "Suying.You@Plf.SDK, 2017-12-27 : [-private] Modify for accessibility ", property = OppoRomType.ROM)
    public String formatNumber(int value) {
        return this.mFormatter != null ? this.mFormatter.format(value) : formatNumberWithLocale(value);
    }

    public static String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(value)});
    }

    private void updateInputTextView() {
        if (this.mDisplayedValues == null) {
            this.mInputText.setText(formatNumber(this.mValue));
        } else {
            this.mInputText.setText(this.mDisplayedValues[this.mValue - this.mMinValue]);
        }
        this.mInputText.setSelection(this.mInputText.getText().length());
    }

    private void notifyChange(int previous, int current) {
        if (this.mOnValueChangeListener != null) {
            this.mOnValueChangeListener.onValueChange(this, previous, this.mValue);
        }
    }

    private void postChangeCurrentByOneFromLongPress(boolean increment) {
        this.mInputText.clearFocus();
        removeAllCallbacks();
        if (this.mChangeCurrentByOneFromLongPressCommand == null) {
            this.mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        }
        this.mChangeCurrentByOneFromLongPressCommand.setIncrement(increment);
        post(this.mChangeCurrentByOneFromLongPressCommand);
    }

    private void removeAllCallbacks() {
        if (this.mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(this.mChangeCurrentByOneFromLongPressCommand);
        }
        if (this.mAdjustScrollerCommand != null) {
            removeCallbacks(this.mAdjustScrollerCommand);
        }
        if (this.mSetSelectionCommand != null) {
            removeCallbacks(this.mSetSelectionCommand);
        }
    }

    private int getSelectedPos(String value) {
        if (this.mDisplayedValues == null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        } else {
            for (int i = 0; i < this.mDisplayedValues.length; i++) {
                value = value.toLowerCase();
                if (this.mDisplayedValues[i].toLowerCase().startsWith(value)) {
                    return this.mMinValue + i;
                }
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e2) {
            }
        }
        return this.mMinValue;
    }

    private void postSetSelectionCommand(int selectionStart, int selectionEnd) {
        if (this.mSetSelectionCommand == null) {
            this.mSetSelectionCommand = new SetSelectionCommand();
        } else {
            removeCallbacks(this.mSetSelectionCommand);
        }
        this.mSetSelectionCommand.mSelectionStart = selectionStart;
        this.mSetSelectionCommand.mSelectionEnd = selectionEnd;
        post(this.mSetSelectionCommand);
    }

    private void postAdjustScrollerCommand(int delayMillis) {
        if (this.mAdjustScrollerCommand == null) {
            this.mAdjustScrollerCommand = new AdjustScrollerCommand();
        } else {
            removeCallbacks(this.mAdjustScrollerCommand);
        }
        postDelayed(this.mAdjustScrollerCommand, (long) delayMillis);
    }

    public void playSoundEffect() {
        if (this.mSoundPool != null) {
            this.mSoundPool.play(this.mClickSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            int hoveredVirtualViewId;
            int eventY = (int) event.getY();
            if (eventY < this.SELECTOR_MIDDLE_ITEM_INDEX * this.mSelectorElementHeight) {
                hoveredVirtualViewId = 0;
            } else if (eventY > (this.SELECTOR_MIDDLE_ITEM_INDEX + 1) * this.mSelectorElementHeight) {
                hoveredVirtualViewId = 2;
            } else {
                hoveredVirtualViewId = 1;
            }
            AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
            switch (event.getActionMasked()) {
                case 7:
                    if (!(this.mLastHoveredChildVirtualViewId == hoveredVirtualViewId || this.mLastHoveredChildVirtualViewId == -1)) {
                        provider.sendAccessibilityEventForVirtualView(this.mLastHoveredChildVirtualViewId, 256);
                        provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 128);
                        this.mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                        provider.performAction(hoveredVirtualViewId, 64, null);
                        break;
                    }
                case 9:
                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 128);
                    this.mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                    provider.performAction(hoveredVirtualViewId, 64, null);
                    break;
                case 10:
                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId, 256);
                    this.mLastHoveredChildVirtualViewId = -1;
                    break;
            }
        }
        return false;
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setClassName(OppoNumberPicker.class.getName());
        event.setScrollable(true);
        event.setScrollY((this.mMinValue + this.mValue) * this.mSelectorElementHeight);
        event.setMaxScrollY((this.mMaxValue - this.mMinValue) * this.mSelectorElementHeight);
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (this.mAccessibilityNodeProvider == null) {
            this.mAccessibilityNodeProvider = new AccessibilityNodeProviderImpl();
        }
        return this.mAccessibilityNodeProvider;
    }
}
