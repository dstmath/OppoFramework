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
import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.internal.app.ColorAlertControllerUpdate;
import com.android.internal.app.ColorAlertControllerUpdate.AlertParams;

public class ColorSystemUpdateDialog extends Dialog implements DialogInterface {
    public static final int LAYOUT_HINT_NONE = 0;
    public static final int LAYOUT_HINT_SIDE = 1;
    public static final int THEME_DEVICE_DEFAULT_DARK = 4;
    public static final int THEME_DEVICE_DEFAULT_LIGHT = 5;
    public static final int THEME_HOLO_DARK = 2;
    public static final int THEME_HOLO_LIGHT = 3;
    public static final int THEME_TRADITIONAL = 1;
    private ColorAlertControllerUpdate mAlert;

    public static class Builder {
        private final AlertParams P;
        private int mTheme;

        public Builder(Context context) {
            this(context, ColorSystemUpdateDialog.resolveDialogTheme(context, 0));
        }

        public Builder(Context context, int theme) {
            this.P = new AlertParams(new ContextThemeWrapper(context, ColorSystemUpdateDialog.resolveDialogTheme(context, theme)));
            this.mTheme = theme;
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

        public Builder setItemsAttrs(ListItemAttr[] itemsAttrs) {
            this.P.mItemsAttrs = itemsAttrs;
            if (this.P.mItemsAttrs.length == this.P.mItems.length) {
                return this;
            }
            throw new IllegalArgumentException("the number of itemsAttrs must be agreed with the listView items");
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

        public Builder setInverseBackgroundForced(boolean useInverseBackground) {
            this.P.mForceInverseBackground = useInverseBackground;
            return this;
        }

        public Builder setRecycleOnMeasureEnabled(boolean enabled) {
            this.P.mRecycleOnMeasure = enabled;
            return this;
        }

        public ColorSystemUpdateDialog create() {
            ColorSystemUpdateDialog dialog = new ColorSystemUpdateDialog(this.P.mContext, this.mTheme, false);
            this.P.apply(dialog.mAlert);
            dialog.setCancelable(this.P.mCancelable);
            if (this.P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(this.P.mOnCancelListener);
            dialog.setOnDismissListener(this.P.mOnDismissListener);
            return dialog;
        }

        public ColorSystemUpdateDialog show() {
            ColorSystemUpdateDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    public static class ListItemAttr {
        private Boolean textBold;
        private Integer textColor;

        public ListItemAttr(Integer textColor, Boolean bold) {
            this.textColor = textColor;
            this.textBold = bold;
        }

        public Integer getItemColor() {
            return this.textColor;
        }

        public Boolean getItemBold() {
            return this.textBold;
        }
    }

    protected ColorSystemUpdateDialog(Context context) {
        this(context, resolveDialogTheme(context, 0), true);
    }

    protected ColorSystemUpdateDialog(Context context, int theme) {
        this(context, theme, true);
    }

    ColorSystemUpdateDialog(Context context, int theme, boolean createThemeContextWrapper) {
        super(context, resolveDialogTheme(context, theme), createThemeContextWrapper);
        this.mWindow.alwaysReadCloseOnTouchAttr();
        this.mAlert = new ColorAlertControllerUpdate(getContext(), this, getWindow());
        setStatusBarDisable();
    }

    public ColorSystemUpdateDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        setStatusBarDisable();
    }

    static int resolveDialogTheme(Context context, int resid) {
        if (resid == 1) {
            return 16974797;
        }
        if (resid == 2) {
            return 16974804;
        }
        if (resid == 3) {
            return 16974811;
        }
        if (resid == 4) {
            return R.style.Theme_DeviceDefault_Dialog_Alert;
        }
        if (resid == 5) {
            return R.style.Theme_DeviceDefault_Light_Dialog_Alert;
        }
        if (resid >= 16777216) {
            return resid;
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.alertDialogTheme, outValue, true);
        return outValue.resourceId;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAlert.installContent();
    }

    public ListView getListView() {
        return this.mAlert.getListView();
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Suying.You@Plf.SDK : Add for when the dialog is the middle's dialog,the statusBar is not pull down", property = OppoRomType.ROM)
    private void setStatusBarDisable() {
        Window dialogWindow = getWindow();
        LayoutParams p = dialogWindow.getAttributes();
        p.isDisableStatusBar = 1;
        dialogWindow.setAttributes(p);
    }
}
