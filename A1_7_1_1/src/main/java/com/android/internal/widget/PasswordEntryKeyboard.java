package com.android.internal.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.Keyboard.Row;
import com.android.internal.R;
import com.oppo.widget.OppoPasswordEntryKeyboardView;

public class PasswordEntryKeyboard extends Keyboard {
    public static final int KEYCODE_SPACE = 32;
    private static final int SHIFT_LOCKED = 2;
    private static final int SHIFT_OFF = 0;
    private static final int SHIFT_ON = 1;
    static int sSpacebarVerticalCorrection;
    private Key mEnterKey;
    private Key mF1Key;
    private Drawable[] mOldShiftIcons;
    private Drawable mShiftIcon;
    private Key[] mShiftKeys;
    private Drawable mShiftLockIcon;
    private int mShiftState;
    private Key mSpaceKey;

    static class LatinKey extends Key {
        private boolean mEnabled = true;
        private boolean mShiftLockEnabled;

        public LatinKey(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            if (this.popupCharacters != null && this.popupCharacters.length() == 0) {
                this.popupResId = 0;
            }
        }

        void setEnabled(boolean enabled) {
            this.mEnabled = enabled;
        }

        void enableShiftLock() {
            this.mShiftLockEnabled = true;
        }

        public void onReleased(boolean inside) {
            if (this.mShiftLockEnabled) {
                this.pressed = !this.pressed;
            } else {
                super.onReleased(inside);
            }
        }

        public boolean isInside(int x, int y) {
            if (!this.mEnabled) {
                return false;
            }
            int code = this.codes[0];
            if (code == -1 || code == -5) {
                y -= this.height / 10;
                if (code == -1) {
                    x += this.width / 6;
                }
                if (code == -5) {
                    x -= this.width / 6;
                }
            } else if (code == 32) {
                y += PasswordEntryKeyboard.sSpacebarVerticalCorrection;
            }
            return super.isInside(x, y);
        }
    }

    public PasswordEntryKeyboard(Context context, int xmlLayoutResId) {
        this(context, xmlLayoutResId, 0);
    }

    public PasswordEntryKeyboard(Context context, int xmlLayoutResId, int width, int height) {
        this(context, xmlLayoutResId, 0, width, height);
    }

    public PasswordEntryKeyboard(Context context, int xmlLayoutResId, int mode) {
        super(context, xmlLayoutResId, mode);
        this.mOldShiftIcons = new Drawable[]{null, null};
        this.mShiftKeys = new Key[]{null, null};
        this.mShiftState = 0;
        init(context);
    }

    public PasswordEntryKeyboard(Context context, int xmlLayoutResId, int mode, int width, int height) {
        super(context, xmlLayoutResId, mode, width, height);
        this.mOldShiftIcons = new Drawable[]{null, null};
        this.mShiftKeys = new Key[]{null, null};
        this.mShiftState = 0;
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();
        this.mShiftIcon = context.getDrawable(R.drawable.sym_keyboard_shift);
        this.mShiftLockIcon = context.getDrawable(R.drawable.sym_keyboard_shift_locked);
        sSpacebarVerticalCorrection = res.getDimensionPixelOffset(R.dimen.password_keyboard_spacebar_vertical_correction);
    }

    public PasswordEntryKeyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
        this.mOldShiftIcons = new Drawable[]{null, null};
        this.mShiftKeys = new Key[]{null, null};
        this.mShiftState = 0;
    }

    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        LatinKey key = new LatinKey(res, parent, x, y, parser);
        int code = key.codes[0];
        if (code >= 0 && code != 10 && (code < 32 || code > 127)) {
            key.label = " ";
            key.setEnabled(false);
        }
        switch (key.codes[0]) {
            case OppoPasswordEntryKeyboardView.KEYCODE_F1 /*-103*/:
                this.mF1Key = key;
                break;
            case 10:
                this.mEnterKey = key;
                break;
            case 32:
                this.mSpaceKey = key;
                break;
        }
        return key;
    }

    void setEnterKeyResources(Resources res, int previewId, int iconId, int labelId) {
        if (this.mEnterKey != null) {
            this.mEnterKey.popupCharacters = null;
            this.mEnterKey.popupResId = 0;
            this.mEnterKey.text = null;
            this.mEnterKey.iconPreview = res.getDrawable(previewId);
            this.mEnterKey.icon = res.getDrawable(iconId);
            this.mEnterKey.label = res.getText(labelId);
            if (this.mEnterKey.iconPreview != null) {
                this.mEnterKey.iconPreview.setBounds(0, 0, this.mEnterKey.iconPreview.getIntrinsicWidth(), this.mEnterKey.iconPreview.getIntrinsicHeight());
            }
        }
    }

    void enableShiftLock() {
        int i = 0;
        for (int index : getShiftKeyIndices()) {
            if (index >= 0 && i < this.mShiftKeys.length) {
                this.mShiftKeys[i] = (Key) getKeys().get(index);
                if (this.mShiftKeys[i] instanceof LatinKey) {
                    ((LatinKey) this.mShiftKeys[i]).enableShiftLock();
                }
                this.mOldShiftIcons[i] = this.mShiftKeys[i].icon;
                i++;
            }
        }
    }

    void setShiftLocked(boolean shiftLocked) {
        int i;
        for (Key shiftKey : this.mShiftKeys) {
            if (shiftKey != null) {
                shiftKey.on = shiftLocked;
                shiftKey.icon = this.mShiftLockIcon;
            }
        }
        if (shiftLocked) {
            i = 2;
        } else {
            i = 1;
        }
        this.mShiftState = i;
    }

    public boolean setShifted(boolean shiftState) {
        boolean shiftChanged = false;
        if (!shiftState) {
            shiftChanged = this.mShiftState != 0;
            this.mShiftState = 0;
        } else if (this.mShiftState == 0) {
            shiftChanged = this.mShiftState == 0;
            this.mShiftState = 1;
        }
        for (int i = 0; i < this.mShiftKeys.length; i++) {
            if (this.mShiftKeys[i] != null) {
                if (!shiftState) {
                    this.mShiftKeys[i].on = false;
                    this.mShiftKeys[i].icon = this.mOldShiftIcons[i];
                } else if (this.mShiftState == 0) {
                    this.mShiftKeys[i].on = false;
                    this.mShiftKeys[i].icon = this.mShiftIcon;
                }
            }
        }
        return shiftChanged;
    }

    public boolean isShifted() {
        boolean z = false;
        if (this.mShiftKeys[0] == null) {
            return super.isShifted();
        }
        if (this.mShiftState != 0) {
            z = true;
        }
        return z;
    }
}
