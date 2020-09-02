package android.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.view.View;
import com.android.internal.app.ColorBaseAlertController;

public abstract class ColorBaseAlertDialog extends Dialog {
    @Deprecated
    public static final int DELETE_ALERT_DIALOG_ONE = 1;
    @Deprecated
    public static final int DELETE_ALERT_DIALOG_THREE = 3;
    @Deprecated
    public static final int DELETE_ALERT_DIALOG_TWO = 2;
    public static final int TYPE_BOTTOM = 1;
    public static final int TYPE_CENTER = 0;

    public abstract void setView(View view);

    public ColorBaseAlertDialog(Context context) {
        super(context);
    }

    public ColorBaseAlertDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public ColorBaseAlertDialog(Context context, int themeResId, boolean createContextThemeWrapper) {
        super(context, themeResId, createContextThemeWrapper);
    }

    public ColorBaseAlertDialog(Context context, boolean cancelable, Message cancelCallback) {
        super(context, cancelable, cancelCallback);
    }

    public ColorBaseAlertDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setButtonIsBold(int positive, int negative, int neutral) {
    }

    public void setDeleteDialogOption(int option) {
    }

    public static abstract class BaseBuilder {
        public abstract AlertDialog create();

        /* access modifiers changed from: package-private */
        public abstract ColorBaseAlertController.BaseAlertParams getParams();

        public abstract AlertDialog.Builder setItems(int i, DialogInterface.OnClickListener onClickListener);

        public abstract AlertDialog.Builder setNegativeButton(int i, DialogInterface.OnClickListener onClickListener);

        public abstract AlertDialog.Builder setNegativeButton(CharSequence charSequence, DialogInterface.OnClickListener onClickListener);

        public abstract AlertDialog.Builder setNeutralButton(int i, DialogInterface.OnClickListener onClickListener);

        public abstract AlertDialog.Builder setNeutralButton(CharSequence charSequence, DialogInterface.OnClickListener onClickListener);

        public abstract AlertDialog.Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener);

        public abstract AlertDialog.Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener);

        public abstract AlertDialog.Builder setOnKeyListener(DialogInterface.OnKeyListener onKeyListener);

        public abstract AlertDialog.Builder setPositiveButton(int i, DialogInterface.OnClickListener onClickListener);

        public abstract AlertDialog.Builder setPositiveButton(CharSequence charSequence, DialogInterface.OnClickListener onClickListener);

        public abstract AlertDialog.Builder setView(int i);

        public BaseBuilder setDialogType(int dialogType) {
            getParams().mDialogType = dialogType;
            return this;
        }

        public BaseBuilder setMessageNeedScroll(boolean isScroll) {
            getParams().mMessageNeedScroll = isScroll;
            return this;
        }

        public BaseBuilder setDeleteDialogOption(int option) {
            return this;
        }

        public BaseBuilder setItems(CharSequence[] items, CharSequence[] summaryItems, DialogInterface.OnClickListener listener) {
            ColorBaseAlertController.BaseAlertParams p = getParams();
            p.setItems(items);
            p.mSummaries = summaryItems;
            p.setOnClickListener(listener);
            return this;
        }

        public BaseBuilder setItems(int itemsId, int summaryItemsId, DialogInterface.OnClickListener listener) {
            ColorBaseAlertController.BaseAlertParams p = getParams();
            p.setItems(p.getContext().getResources().getTextArray(itemsId));
            p.mSummaries = p.getContext().getResources().getTextArray(summaryItemsId);
            p.setOnClickListener(listener);
            return this;
        }
    }
}
