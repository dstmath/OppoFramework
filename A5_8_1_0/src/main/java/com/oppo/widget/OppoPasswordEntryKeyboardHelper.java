package com.oppo.widget;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewRootImpl;
import com.color.util.ColorContextUtil;

public class OppoPasswordEntryKeyboardHelper implements OnKeyboardActionListener {
    public static final int KEYBOARD_MODE_ALPHA = 0;
    public static final int KEYBOARD_MODE_NUMERIC = 1;
    public static final int KEYBOARD_MODE_SECURITY_NUMERIC = 2;
    private static final int KEYBOARD_STATE_CAPSLOCK = 2;
    private static final int KEYBOARD_STATE_NORMAL = 0;
    private static final int KEYBOARD_STATE_SHIFTED = 1;
    private static final int NUMERIC = 0;
    private static final int QWERTY = 2;
    private static final int QWERTY_SHIFTED = 3;
    private static final int SECURITYKEYBOARD = 1;
    private static final int SECURITYNUMERIC = 1;
    private static final int SYMBOLS = 4;
    private static final String TAG = "OppoPasswordEntryKeyboardHelper";
    private static final int UNLOCKKEYBOARD = 2;
    private final Context mContext;
    private boolean mEnableHaptics;
    private int mKeyboardMode;
    private int mKeyboardState;
    private final KeyboardView mKeyboardView;
    int[] mLayouts;
    int[] mLockLayouts;
    private OppoPasswordEntryKeyboard mNumericKeyboard;
    private OppoPasswordEntryKeyboard mQwertyKeyboard;
    private OppoPasswordEntryKeyboard mQwertyKeyboardShifted;
    private OppoPasswordEntryKeyboard mSecurityNumKeyboard;
    private OppoPasswordEntryKeyboard mSymbolsKeyboard;
    private OppoPasswordEntryKeyboard mSymbolsKeyboardShifted;
    private final View mTargetView;
    private int mType;
    private boolean mUsingScreenWidth;

    public OppoPasswordEntryKeyboardHelper(Context context, KeyboardView keyboardView, View targetView) {
        this(context, keyboardView, targetView, true, null);
    }

    public OppoPasswordEntryKeyboardHelper(Context context, KeyboardView keyboardView, View targetView, boolean useFullScreenWidth) {
        this(context, keyboardView, targetView, useFullScreenWidth, null);
    }

    public OppoPasswordEntryKeyboardHelper(Context context, KeyboardView keyboardView, View targetView, boolean useFullScreenWidth, int[] layouts) {
        this.mKeyboardMode = 0;
        this.mKeyboardState = 0;
        this.mEnableHaptics = false;
        this.mType = 0;
        this.mLockLayouts = new int[]{202048513, 202048522, 202048512, 202048512, 202048516};
        this.mLayouts = new int[]{202048513, 202048523, 202048514, 202048514, 202048515};
        this.mContext = context;
        this.mTargetView = targetView;
        this.mKeyboardView = keyboardView;
        this.mKeyboardView.setOnKeyboardActionListener(this);
        this.mUsingScreenWidth = useFullScreenWidth;
        if (this.mTargetView != null) {
            this.mTargetView.setImportantForAccessibility(1);
        }
        if (layouts != null) {
            if (layouts.length != this.mLayouts.length) {
                throw new RuntimeException("Wrong number of layouts");
            }
            for (int i = 0; i < this.mLayouts.length; i++) {
                this.mLayouts[i] = layouts[i];
            }
        }
        createKeyboards();
    }

    public OppoPasswordEntryKeyboardHelper(Context context, KeyboardView keyboardView, View targetView, int type, boolean useFullScreenWidth) {
        this.mKeyboardMode = 0;
        this.mKeyboardState = 0;
        this.mEnableHaptics = false;
        this.mType = 0;
        this.mLockLayouts = new int[]{202048513, 202048522, 202048512, 202048512, 202048516};
        this.mLayouts = new int[]{202048513, 202048523, 202048514, 202048514, 202048515};
        this.mContext = context;
        this.mTargetView = targetView;
        this.mKeyboardView = keyboardView;
        this.mKeyboardView.setOnKeyboardActionListener(this);
        this.mUsingScreenWidth = useFullScreenWidth;
        this.mType = type;
        if (this.mTargetView != null) {
            this.mTargetView.setImportantForAccessibility(1);
        }
        createSecurtiyKeyboards();
    }

    private void createSecurtiyKeyboards() {
        if (isUnLockKeyboard()) {
            this.mSecurityNumKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLockLayouts[1], 201458761, this.mType, false);
            this.mSecurityNumKeyboard.enableShiftLock();
            this.mQwertyKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLockLayouts[2], 201458761, this.mType, false);
            this.mQwertyKeyboard.enableShiftLock();
            this.mQwertyKeyboardShifted = new OppoPasswordEntryKeyboard(this.mContext, this.mLockLayouts[3], 201458761, this.mType, false);
            this.mQwertyKeyboardShifted.enableShiftLock();
            this.mQwertyKeyboardShifted.setShifted(true);
            this.mSymbolsKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLockLayouts[4], this.mType, false);
            this.mSymbolsKeyboard.enableShiftLock();
            return;
        }
        this.mSecurityNumKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[1], 201458761, this.mType, false);
        this.mSecurityNumKeyboard.enableShiftLock();
        this.mQwertyKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[2], 201458761, this.mType, false);
        this.mQwertyKeyboard.enableShiftLock();
        this.mQwertyKeyboardShifted = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[3], 201458761, this.mType, false);
        this.mQwertyKeyboardShifted.enableShiftLock();
        this.mQwertyKeyboardShifted.setShifted(true);
        this.mSymbolsKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[4], this.mType, false);
        this.mSymbolsKeyboard.enableShiftLock();
    }

    public void createKeyboards() {
        LayoutParams lp = this.mKeyboardView.getLayoutParams();
        if (this.mUsingScreenWidth || lp.width == -1) {
            createKeyboardsWithDefaultWidth();
        } else {
            createKeyboardsWithSpecificSize(lp.width, lp.height);
        }
    }

    public void setEnableHaptics(boolean enabled) {
        this.mEnableHaptics = enabled;
    }

    public boolean isAlpha() {
        return this.mKeyboardMode == 0;
    }

    private void createKeyboardsWithSpecificSize(int width, int height) {
        this.mNumericKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[0], width, height);
        this.mQwertyKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[2], 201458761, width, height);
        this.mQwertyKeyboard.enableShiftLock();
        this.mQwertyKeyboardShifted = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[3], 201458761, width, height);
        this.mQwertyKeyboardShifted.enableShiftLock();
        this.mQwertyKeyboardShifted.setShifted(true);
        this.mSymbolsKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[4], width, height);
        this.mSymbolsKeyboard.enableShiftLock();
    }

    private void createKeyboardsWithDefaultWidth() {
        this.mNumericKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[0]);
        this.mQwertyKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[2], 201458761);
        this.mQwertyKeyboard.enableShiftLock();
        this.mQwertyKeyboardShifted = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[3], 201458761);
        this.mQwertyKeyboardShifted.enableShiftLock();
        this.mQwertyKeyboardShifted.setShifted(true);
        this.mSymbolsKeyboard = new OppoPasswordEntryKeyboard(this.mContext, this.mLayouts[4]);
        this.mSymbolsKeyboard.enableShiftLock();
    }

    public void setKeyboardMode(int mode) {
        switch (mode) {
            case 0:
                this.mKeyboardView.setSecurityNumericKeyboard(false);
                this.mKeyboardView.setKeyboard(this.mQwertyKeyboard);
                this.mKeyboardState = 0;
                if (System.getInt(this.mContext.getContentResolver(), "show_password", 1) != 0) {
                }
                this.mKeyboardView.setPreviewEnabled(false);
                if (isUnLockKeyboard()) {
                    this.mKeyboardView.dismissPopupWindow();
                    break;
                }
                break;
            case 1:
                this.mKeyboardView.setSecurityNumericKeyboard(false);
                this.mKeyboardView.setKeyboard(this.mNumericKeyboard);
                this.mKeyboardState = 0;
                this.mKeyboardView.setPreviewEnabled(false);
                break;
            case 2:
                this.mKeyboardView.setSecurityNumericKeyboard(true);
                this.mKeyboardView.setKeyboard(this.mSecurityNumKeyboard);
                this.mKeyboardView.setPreviewEnabled(false);
                break;
        }
        this.mKeyboardMode = mode;
    }

    private void sendKeyEventsToTarget(int character) {
        ViewRootImpl viewRootImpl = this.mTargetView.getViewRootImpl();
        if (this.mTargetView != null) {
            this.mTargetView.setImportantForAccessibility(2);
        }
        KeyEvent[] events = KeyCharacterMap.load(-1).getEvents(new char[]{(char) character});
        if (events != null) {
            for (KeyEvent event : events) {
                viewRootImpl.dispatchKeyFromIme(KeyEvent.changeFlags(event, (event.getFlags() | 2) | 4));
            }
        }
    }

    public void sendDownUpKeyEvents(int keyEventCode) {
        long eventTime = SystemClock.uptimeMillis();
        ViewRootImpl viewRootImpl = this.mTargetView.getViewRootImpl();
        viewRootImpl.dispatchKeyFromIme(new KeyEvent(eventTime, eventTime, 0, keyEventCode, 0, 0, -1, 0, 6));
        viewRootImpl.dispatchKeyFromIme(new KeyEvent(eventTime, eventTime, 1, keyEventCode, 0, 0, -1, 0, 6));
    }

    public void onKey(int primaryCode, int[] keyCodes) {
        if (primaryCode == -5) {
            handleBackspace();
        } else if (primaryCode == -1) {
            handleShift();
        } else if (primaryCode == -7) {
            handleClear();
        } else if (primaryCode == -3) {
            handleClose();
        } else if (primaryCode == 18 && this.mKeyboardMode == 1) {
            haldleWellNumber();
        } else if (primaryCode == -2 && this.mKeyboardView != null) {
            handleModeChange(primaryCode);
        } else if (primaryCode != -6 || this.mKeyboardView == null) {
            handleCharacter(primaryCode, keyCodes);
            if (this.mKeyboardState == 1) {
                this.mKeyboardState = 2;
                if (!isSecurityKeyboard()) {
                    handleShift();
                }
            }
        } else {
            handleModeChange(primaryCode);
        }
    }

    public void setVibratePattern(int id) {
        int[] tmpArray = null;
        try {
            tmpArray = this.mContext.getResources().getIntArray(id);
        } catch (NotFoundException e) {
            if (id != 0) {
                Log.e(TAG, "Vibrate pattern missing", e);
            }
        }
        if (tmpArray != null) {
        }
    }

    private void handleModeChange(int primaryCode) {
        Keyboard current = this.mKeyboardView.getKeyboard();
        Keyboard next = null;
        if (current == this.mSecurityNumKeyboard && primaryCode == -2) {
            next = this.mSymbolsKeyboard;
            this.mKeyboardView.setSecurityNumericKeyboard(false);
        } else if (current == this.mSecurityNumKeyboard && primaryCode == -6) {
            next = this.mQwertyKeyboard;
            this.mKeyboardView.setSecurityNumericKeyboard(false);
        } else if ((current == this.mQwertyKeyboard || current == this.mQwertyKeyboardShifted) && primaryCode == -2) {
            next = this.mSymbolsKeyboard;
            this.mKeyboardView.setSecurityNumericKeyboard(false);
        } else if ((current == this.mQwertyKeyboard || current == this.mQwertyKeyboardShifted) && primaryCode == -6) {
            next = this.mSecurityNumKeyboard;
            this.mKeyboardView.setSecurityNumericKeyboard(true);
        } else if ((current == this.mSymbolsKeyboard || current == this.mSymbolsKeyboardShifted) && primaryCode == -2) {
            next = this.mQwertyKeyboard;
            this.mKeyboardView.setSecurityNumericKeyboard(false);
        } else if ((current == this.mSymbolsKeyboard || current == this.mSymbolsKeyboardShifted) && primaryCode == -6) {
            next = this.mSecurityNumKeyboard;
            this.mKeyboardView.setSecurityNumericKeyboard(true);
        }
        this.mKeyboardView.setKeyboard(next);
        this.mKeyboardState = 0;
    }

    public void handleBackspace() {
        if (isUnLockKeyboard()) {
            sendDownUpKeyEvents(67);
        }
    }

    public void handleClear() {
        if (isUnLockKeyboard()) {
            sendDownUpKeyEvents(28);
        }
    }

    private void handleShift() {
        boolean z = true;
        if (this.mKeyboardView != null) {
            Keyboard current = this.mKeyboardView.getKeyboard();
            Keyboard next = null;
            boolean isAlphaMode = current == this.mQwertyKeyboard || current == this.mQwertyKeyboardShifted;
            if (this.mKeyboardState == 0) {
                int i;
                if (isAlphaMode) {
                    i = 1;
                } else {
                    i = 2;
                }
                this.mKeyboardState = i;
                next = isAlphaMode ? this.mQwertyKeyboardShifted : this.mSymbolsKeyboardShifted;
            } else if (this.mKeyboardState == 1) {
                this.mKeyboardState = 2;
                next = isAlphaMode ? this.mQwertyKeyboardShifted : this.mSymbolsKeyboardShifted;
                if (isSecurityKeyboard() && next != null && next == this.mQwertyKeyboardShifted && next == current) {
                    next = this.mQwertyKeyboard;
                    this.mKeyboardView.setKeyboard(next);
                    this.mKeyboardState = 0;
                }
            } else if (this.mKeyboardState == 2) {
                this.mKeyboardState = 0;
                next = isAlphaMode ? this.mQwertyKeyboard : this.mSymbolsKeyboard;
            }
            if (next != null) {
                boolean z2;
                if (next != current) {
                    this.mKeyboardView.setKeyboard(next);
                }
                if (this.mKeyboardState == 2) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                next.setShiftLocked(z2);
                KeyboardView keyboardView = this.mKeyboardView;
                if (this.mKeyboardState == 0) {
                    z = false;
                }
                keyboardView.setShifted(z);
            }
        }
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (!(!this.mKeyboardView.isShifted() || primaryCode == 32 || primaryCode == 10)) {
            primaryCode = Character.toUpperCase(primaryCode);
        }
        if (isUnLockKeyboard()) {
            sendKeyEventsToTarget(primaryCode);
        }
    }

    private void handleClose() {
    }

    private void haldleWellNumber() {
        sendKeyEventsToTarget(35);
    }

    public void onPress(int primaryCode) {
        performHapticFeedback();
    }

    private void performHapticFeedback() {
        if (this.mEnableHaptics) {
            this.mKeyboardView.performHapticFeedback(1, 3);
        }
    }

    public void onRelease(int primaryCode) {
    }

    public void onText(CharSequence text) {
    }

    public void swipeDown() {
    }

    public void swipeLeft() {
    }

    public void swipeRight() {
    }

    public void swipeUp() {
    }

    private boolean isSecurityKeyboard() {
        if (this.mType != 1) {
            return this.mType == 2 ? ColorContextUtil.isOppoStyle(this.mContext) : false;
        } else {
            return true;
        }
    }

    private boolean isUnLockKeyboard() {
        return this.mType != 1 ? ColorContextUtil.isOppoStyle(this.mContext) : false;
    }
}
