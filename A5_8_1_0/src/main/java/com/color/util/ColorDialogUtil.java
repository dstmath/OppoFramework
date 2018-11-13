package com.color.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import com.color.app.ColorAlertController;

public class ColorDialogUtil {
    private static final int BIT_BUTTON_NEGATIVE = 2;
    private static final int BIT_BUTTON_NEUTRAL = 4;
    private static final int BIT_BUTTON_POSITIVE = 1;
    public static final int BIT_FOUSED_BUTTON_NEGATIVE = 16;
    public static final int BIT_FOUSED_BUTTON_NEUTRAL = 32;
    public static final int BIT_FOUSED_BUTTON_POSITIVE = 8;
    public static final int BIT_FOUSED_DEFAULT = 0;
    private static final boolean DBG = true;
    private static final String TAG = "OppoDialogUtil";
    private static int mDeleteDialogOption = 0;
    public int mAlertDialogPadding;
    private Button mButtonNegative;
    private Button mButtonNeutral;
    private Button mButtonPositive;
    private Context mContext;
    private int mFousedFlag = 1;
    private ColorStateList textColor;
    private ColorStateList textFousedColor;

    public ColorDialogUtil(Context context) {
        this.mContext = context;
    }

    public static void setDeleteDialogOption(int delete) {
        mDeleteDialogOption = delete;
    }

    public void setButtonBackground(int whichButtons, int option) {
        switch (whichButtons) {
            case 1:
                LayoutParams lp = (LayoutParams) this.mButtonPositive.getLayoutParams();
                if (option == 0) {
                    lp.leftMargin = this.mAlertDialogPadding;
                    lp.rightMargin = this.mAlertDialogPadding;
                    this.mButtonPositive.setLayoutParams(lp);
                    return;
                }
                return;
            case 2:
                if (mDeleteDialogOption == 2 || mDeleteDialogOption == 3) {
                    this.mButtonNegative.setTextColor(this.textFousedColor);
                }
                LayoutParams ln = (LayoutParams) this.mButtonNegative.getLayoutParams();
                if (option == 0) {
                    ln.leftMargin = this.mAlertDialogPadding;
                    ln.rightMargin = this.mAlertDialogPadding;
                    this.mButtonNegative.setLayoutParams(ln);
                    return;
                }
                return;
            case 4:
                LayoutParams lne = (LayoutParams) this.mButtonNeutral.getLayoutParams();
                if (option == 0) {
                    lne.leftMargin = this.mAlertDialogPadding;
                    lne.rightMargin = this.mAlertDialogPadding;
                    this.mButtonNegative.setLayoutParams(lne);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void initialize(Window mWindow) {
        this.textColor = this.mContext.getColorStateList(201720844);
        this.mAlertDialogPadding = (int) this.mContext.getResources().getDimension(201655463);
        if (mDeleteDialogOption == 2 || mDeleteDialogOption == 2) {
            this.textFousedColor = this.mContext.getColorStateList(201719832);
        } else {
            this.textFousedColor = this.mContext.getColorStateList(201720844);
        }
    }

    public void setDialogButtonFlag(Window mWindow, int option) {
        int flag = 0;
        this.mButtonPositive = (Button) mWindow.findViewById(201458947);
        this.mButtonNegative = (Button) mWindow.findViewById(201458945);
        this.mButtonNeutral = (Button) mWindow.findViewById(201458946);
        if (hasFousedFlag(this.mButtonPositive)) {
            flag = 8;
        }
        if (hasFousedFlag(this.mButtonNegative)) {
            flag = 16;
        }
        if (hasFousedFlag(this.mButtonNeutral)) {
            flag = 32;
        }
        setDialogButtonFlag(mWindow, flag, option);
    }

    private boolean hasFousedFlag(Button button) {
        boolean z = false;
        if (button == null) {
            return false;
        }
        Object tag = button.getTag();
        if (!(tag instanceof Integer)) {
            return false;
        }
        if (this.mFousedFlag == ((Integer) tag).intValue()) {
            z = DBG;
        }
        return z;
    }

    public void setDialogButtonFlag(Window mWindow, int flag, int option) {
        try {
            this.mButtonPositive = (Button) mWindow.findViewById(201458947);
            this.mButtonNegative = (Button) mWindow.findViewById(201458945);
            this.mButtonNeutral = (Button) mWindow.findViewById(201458946);
            if (this.mButtonPositive != null || this.mButtonNegative != null || this.mButtonNeutral != null) {
                initialize(mWindow);
                int whichButtons = 0;
                if (this.mButtonPositive.getVisibility() == 0) {
                    whichButtons = 1;
                }
                if (this.mButtonNegative.getVisibility() == 0) {
                    whichButtons |= 2;
                }
                if (this.mButtonNeutral.getVisibility() == 0) {
                    whichButtons |= 4;
                }
                setButtonBackground(whichButtons, option);
                if (flag != 0) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDialogDrag(ColorAlertController ac, Window window, int option) {
        setDialogButtonFlag(window, option);
    }
}
