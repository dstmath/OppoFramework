package android.app;

import android.R;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.ResourceId;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.internal.app.AlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.oppo.util.OppoDialogUtil;

public class AlertDialog extends Dialog implements DialogInterface {
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK, 2016-08-18 : Add for it sets whether the button is bold", property = OppoRomType.ROM)
    public static final int DEFAULT_BUTTON = 0;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for Delete AlertDialog", property = OppoRomType.ROM)
    public static final int DELETE_ALERT_DIALOG_ONE = 1;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-05-04 : Add for Delete AlertDialog", property = OppoRomType.ROM)
    public static final int DELETE_ALERT_DIALOG_THREE = 3;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for Delete AlertDialog", property = OppoRomType.ROM)
    public static final int DELETE_ALERT_DIALOG_TWO = 2;
    public static final int LAYOUT_HINT_NONE = 0;
    public static final int LAYOUT_HINT_SIDE = 1;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-05-04 : Add for Delete AlertDialog", property = OppoRomType.ROM)
    public static final int LIST_TEXTCOLOR_ONE = 0;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-05-04 : Add for Delete AlertDialog", property = OppoRomType.ROM)
    public static final int LIST_TEXTCOLOR_TWO = 1;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-05-04 : Add for Delete AlertDialog", property = OppoRomType.ROM)
    public static final int[] LIST_TEXT_COLOR = new int[]{0, 1};
    @Deprecated
    public static final int THEME_DEVICE_DEFAULT_DARK = 4;
    @Deprecated
    public static final int THEME_DEVICE_DEFAULT_LIGHT = 5;
    @Deprecated
    public static final int THEME_HOLO_DARK = 2;
    @Deprecated
    public static final int THEME_HOLO_LIGHT = 3;
    @Deprecated
    public static final int THEME_TRADITIONAL = 1;
    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "JianHui.Yu@ROM.SDK, 2017-11-11 : [-private +protected +@hide] Modify for Longshot 5.0", property = OppoRomType.ROM)
    protected AlertController mAlert;

    public static class Builder {
        private final AlertParams P;
        @OppoHook(level = OppoHookType.NEW_FIELD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for a new style AlertDialog", property = OppoRomType.ROM)
        private int mDeleteDialogOption;

        public Builder(Context context) {
            this(context, AlertDialog.resolveDialogTheme(context, 0));
        }

        public Builder(Context context, int themeResId) {
            this.mDeleteDialogOption = 0;
            this.P = new AlertParams(new ContextThemeWrapper(context, AlertDialog.resolveDialogTheme(context, themeResId)));
        }

        public Context getContext() {
            return this.P.mContext;
        }

        public Builder setTitle(int titleId) {
            this.P.mTitle = this.P.mContext.getText(titleId);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.P.mTitle = title;
            return this;
        }

        public Builder setCustomTitle(View customTitleView) {
            this.P.mCustomTitleView = customTitleView;
            return this;
        }

        public Builder setMessage(int messageId) {
            this.P.mMessage = this.P.mContext.getText(messageId);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            this.P.mMessage = message;
            return this;
        }

        public Builder setIcon(int iconId) {
            this.P.mIconId = iconId;
            return this;
        }

        public Builder setIcon(Drawable icon) {
            this.P.mIcon = icon;
            return this;
        }

        public Builder setIconAttribute(int attrId) {
            TypedValue out = new TypedValue();
            this.P.mContext.getTheme().resolveAttribute(attrId, out, true);
            this.P.mIconId = out.resourceId;
            return this;
        }

        public Builder setPositiveButton(int textId, OnClickListener listener) {
            this.P.mPositiveButtonText = this.P.mContext.getText(textId);
            this.P.mPositiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            this.P.mPositiveButtonText = text;
            this.P.mPositiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId, OnClickListener listener) {
            this.P.mNegativeButtonText = this.P.mContext.getText(textId);
            this.P.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            this.P.mNegativeButtonText = text;
            this.P.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(int textId, OnClickListener listener) {
            this.P.mNeutralButtonText = this.P.mContext.getText(textId);
            this.P.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(CharSequence text, OnClickListener listener) {
            this.P.mNeutralButtonText = text;
            this.P.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.P.mCancelable = cancelable;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            this.P.mOnCancelListener = onCancelListener;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            this.P.mOnDismissListener = onDismissListener;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            this.P.mOnKeyListener = onKeyListener;
            return this;
        }

        public Builder setItems(int itemsId, OnClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setItems(CharSequence[] items, OnClickListener listener) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setAdapter(ListAdapter adapter, OnClickListener listener) {
            this.P.mAdapter = adapter;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setCursor(Cursor cursor, OnClickListener listener, String labelColumn) {
            this.P.mCursor = cursor;
            this.P.mLabelColumn = labelColumn;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setMultiChoiceItems(int itemsId, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnCheckboxClickListener = listener;
            this.P.mCheckedItems = checkedItems;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            this.P.mItems = items;
            this.P.mOnCheckboxClickListener = listener;
            this.P.mCheckedItems = checkedItems;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, OnMultiChoiceClickListener listener) {
            this.P.mCursor = cursor;
            this.P.mOnCheckboxClickListener = listener;
            this.P.mIsCheckedColumn = isCheckedColumn;
            this.P.mLabelColumn = labelColumn;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(int itemsId, int checkedItem, OnClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, OnClickListener listener) {
            this.P.mCursor = cursor;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mLabelColumn = labelColumn;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, OnClickListener listener) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, OnClickListener listener) {
            this.P.mAdapter = adapter;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setOnItemSelectedListener(OnItemSelectedListener listener) {
            this.P.mOnItemSelectedListener = listener;
            return this;
        }

        public Builder setView(int layoutResId) {
            this.P.mView = null;
            this.P.mViewLayoutResId = layoutResId;
            this.P.mViewSpacingSpecified = false;
            return this;
        }

        public Builder setView(View view) {
            this.P.mView = view;
            this.P.mViewLayoutResId = 0;
            this.P.mViewSpacingSpecified = false;
            return this;
        }

        @Deprecated
        public Builder setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
            this.P.mView = view;
            this.P.mViewLayoutResId = 0;
            this.P.mViewSpacingSpecified = true;
            this.P.mViewSpacingLeft = viewSpacingLeft;
            this.P.mViewSpacingTop = viewSpacingTop;
            this.P.mViewSpacingRight = viewSpacingRight;
            this.P.mViewSpacingBottom = viewSpacingBottom;
            return this;
        }

        @Deprecated
        public Builder setInverseBackgroundForced(boolean useInverseBackground) {
            this.P.mForceInverseBackground = useInverseBackground;
            return this;
        }

        public Builder setRecycleOnMeasureEnabled(boolean enabled) {
            this.P.mRecycleOnMeasure = enabled;
            return this;
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Modify for a new style AlertDialog", property = OppoRomType.ROM)
        public AlertDialog create() {
            AlertDialog dialog = new AlertDialog(this.P.mContext, 0, false, this.mDeleteDialogOption);
            this.P.apply(dialog.mAlert);
            dialog.setCancelable(this.P.mCancelable);
            dialog.setOnCancelListener(this.P.mOnCancelListener);
            dialog.setOnDismissListener(this.P.mOnDismissListener);
            if (this.P.mOnKeyListener != null) {
                dialog.setOnKeyListener(this.P.mOnKeyListener);
            }
            return dialog;
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Modify for a new style AlertDialog", property = OppoRomType.ROM)
        public AlertDialog show() {
            AlertDialog dialog = create();
            dialog.mAlert.setDeleteDialogOption(this.mDeleteDialogOption);
            dialog.show();
            return dialog;
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for a new style AlertDialog", property = OppoRomType.ROM)
        public Builder setDeleteDialogOption(int deleteDialogOption) {
            this.mDeleteDialogOption = deleteDialogOption;
            OppoDialogUtil.setDeleteDialogOption(deleteDialogOption);
            return this;
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Changwei.Li@Plf.SDK, 2016-05-25 : Add for delete dialog type three", property = OppoRomType.ROM)
        public int getDeleteDialogOption() {
            return this.mDeleteDialogOption;
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2016-06-01 : Add for 3.1 dialog", property = OppoRomType.ROM)
        public Builder setItems(CharSequence[] items, OnClickListener listener, int[] textColor) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            this.P.textColor = textColor;
            return this;
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2016-06-01 : Add for 3.1 dialog", property = OppoRomType.ROM)
        public Builder setItems(int itemsId, OnClickListener listener, int[] textColor) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            this.P.textColor = textColor;
            return this;
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2016-08-20 : Add for 3.1 dialog ,the Button of the dialog has the summary", property = OppoRomType.ROM)
        public Builder setItems(CharSequence[] items, CharSequence[] summaryItems, OnClickListener listener, int[] textColor) {
            this.P.mItems = items;
            this.P.mSummaryItems = summaryItems;
            this.P.mOnClickListener = listener;
            this.P.textColor = textColor;
            return this;
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2016-08-20 : Add for 3.1 dialog ,the Button of the dialog has the summary", property = OppoRomType.ROM)
        public Builder setItems(int itemsId, int summaryItemsId, OnClickListener listener, int[] textColor) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mSummaryItems = this.P.mContext.getResources().getTextArray(summaryItemsId);
            this.P.mOnClickListener = listener;
            this.P.textColor = textColor;
            return this;
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2016-08-20 : Add for 3.1 dialog ,the Button of the dialog has the summary", property = OppoRomType.ROM)
        public Builder isNeedScroll(boolean isScroll) {
            this.P.mIsScroll = isScroll;
            return this;
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2017-01-23 : Add for 3.1 dialog ,whether the message of the dialog has the scroll", property = OppoRomType.ROM)
        public Builder isMessageNeedScroll(boolean isScroll) {
            this.P.mMessageIsScroll = isScroll;
            return this;
        }
    }

    protected AlertDialog(Context context) {
        this(context, 0);
    }

    protected AlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        this(context, 0);
        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
    }

    protected AlertDialog(Context context, int themeResId) {
        this(context, themeResId, true);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Modify for a new style AlertDialog", property = OppoRomType.ROM)
    AlertDialog(Context context, int themeResId, boolean createContextThemeWrapper) {
        this(context, themeResId, createContextThemeWrapper, 0);
    }

    static int resolveDialogTheme(Context context, int themeResId) {
        if (themeResId == 1) {
            return 16974797;
        }
        if (themeResId == 2) {
            return 16974804;
        }
        if (themeResId == 3) {
            return 16974811;
        }
        if (themeResId == 4) {
            return R.style.Theme_DeviceDefault_Dialog_Alert;
        }
        if (themeResId == 5) {
            return R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        }
        if (ResourceId.isValid(themeResId)) {
            return themeResId;
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.alertDialogTheme, outValue, true);
        return outValue.resourceId;
    }

    public Button getButton(int whichButton) {
        return this.mAlert.getButton(whichButton);
    }

    public ListView getListView() {
        return this.mAlert.getListView();
    }

    public void setTitle(CharSequence title) {
        super.setTitle(title);
        this.mAlert.setTitle(title);
    }

    public void setCustomTitle(View customTitleView) {
        this.mAlert.setCustomTitle(customTitleView);
    }

    public void setMessage(CharSequence message) {
        this.mAlert.setMessage(message);
    }

    public void setView(View view) {
        this.mAlert.setView(view);
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mAlert.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
    }

    void setButtonPanelLayoutHint(int layoutHint) {
        this.mAlert.setButtonPanelLayoutHint(layoutHint);
    }

    public void setButton(int whichButton, CharSequence text, Message msg) {
        this.mAlert.setButton(whichButton, text, null, msg);
    }

    public void setButton(int whichButton, CharSequence text, OnClickListener listener) {
        this.mAlert.setButton(whichButton, text, listener, null);
    }

    @Deprecated
    public void setButton(CharSequence text, Message msg) {
        setButton(-1, text, msg);
    }

    @Deprecated
    public void setButton2(CharSequence text, Message msg) {
        setButton(-2, text, msg);
    }

    @Deprecated
    public void setButton3(CharSequence text, Message msg) {
        setButton(-3, text, msg);
    }

    @Deprecated
    public void setButton(CharSequence text, OnClickListener listener) {
        setButton(-1, text, listener);
    }

    @Deprecated
    public void setButton2(CharSequence text, OnClickListener listener) {
        setButton(-2, text, listener);
    }

    @Deprecated
    public void setButton3(CharSequence text, OnClickListener listener) {
        setButton(-3, text, listener);
    }

    public void setIcon(int resId) {
        this.mAlert.setIcon(resId);
    }

    public void setIcon(Drawable icon) {
        this.mAlert.setIcon(icon);
    }

    public void setIconAttribute(int attrId) {
        TypedValue out = new TypedValue();
        this.mContext.getTheme().resolveAttribute(attrId, out, true);
        this.mAlert.setIcon(out.resourceId);
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        this.mAlert.setInverseBackgroundForced(forceInverseBackground);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAlert.installContent();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for a new style AlertDialog", property = OppoRomType.ROM)
    void createDialog(int deleteDialogOption) {
        if (deleteDialogOption > 0) {
            this.mAlert = AlertController.create(getContext(), this, getWindow(), deleteDialogOption);
            setCanceledOnTouchOutside(true);
            return;
        }
        this.mAlert = AlertController.create(getContext(), this, getWindow());
        if (isOppoStyle()) {
            setCanceledOnTouchOutside(false);
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Shuai.Zhang@Plf.SDK, 2016-04-28 : Add for a new style AlertDialog", property = OppoRomType.ROM)
    protected AlertDialog(Context context, int themeResId, boolean createContextThemeWrapper, int deleteDialogOption) {
        super(context, createContextThemeWrapper ? resolveDialogTheme(context, themeResId) : 0, createContextThemeWrapper);
        this.mWindow.alwaysReadCloseOnTouchAttr();
        createDialog(deleteDialogOption);
        if (isOppoStyle()) {
            setStatusBarDisable();
        }
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK, 2016-08-18 : Add for it sets whether the button is bold", property = OppoRomType.ROM)
    public void setButtonIsBold(int positive, int negative, int neutral) {
        this.mAlert.setButtonIsBold(positive, negative, neutral);
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK : Add for when the dialog is the middle's dialog,the statusBar is not pull down", property = OppoRomType.ROM)
    private void setStatusBarDisable() {
        int option = getDeleteDialogOption();
        if (option != 2 && option != 3 && option == 0 && this.mContext != null && this.mContext.getThemeResId() != 201524238) {
            Window dialogWindow = getWindow();
            LayoutParams p = dialogWindow.getAttributes();
            p.isDisableStatusBar = 1;
            dialogWindow.setAttributes(p);
        }
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK : Add for when the delete dialog change the default dialog,the single button of the default dialog does not set the horizontal padding,because the mDeleteDialogOption variable in the OppoDialogUtil is static ", property = OppoRomType.ROM)
    public int getDeleteDialogOption() {
        return this.mAlert.getDeleteDialogOption();
    }
}
