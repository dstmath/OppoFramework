package com.color.app;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import com.color.app.ColorAlertController.AlertParams;

public class ColorAlertDialogCustom extends ColorAlertDialog {
    public static final int DEFAULT_BUTTON = 0;

    public static class Builder extends com.color.app.ColorAlertDialog.Builder {
        public Builder(Context context) {
            this(context, ColorAlertDialog.resolveDialogTheme(context, 0));
        }

        public Builder(Context context, int theme) {
            super(context, theme);
        }

        public AlertParams initAlertParams(Context context, int theme) {
            return new ColorAlertControllerCustom.AlertParams(new ContextThemeWrapper(context, ColorAlertDialog.resolveDialogTheme(context, theme)));
        }

        public Builder setItemsAttrs(ListItemAttr[] itemsAttrs) {
            ((ColorAlertControllerCustom.AlertParams) this.P).mItemsAttrs = itemsAttrs;
            if (((ColorAlertControllerCustom.AlertParams) this.P).mItemsAttrs.length == this.P.mItems.length) {
                return this;
            }
            throw new IllegalArgumentException("the number of itemsAttrs must be agreed with the listView items");
        }

        public Builder setTitle(int titleId) {
            return (Builder) super.setTitle(titleId);
        }

        public Builder setTitle(CharSequence title) {
            return (Builder) super.setTitle(title);
        }

        public Builder setCustomTitle(View customTitleView) {
            return (Builder) super.setCustomTitle(customTitleView);
        }

        public Builder setMessage(int messageId) {
            return (Builder) super.setMessage(messageId);
        }

        public Builder setMessage(CharSequence message) {
            return (Builder) super.setMessage(message);
        }

        public Builder setIcon(int iconId) {
            return (Builder) super.setIcon(iconId);
        }

        public Builder setIcon(Drawable icon) {
            return (Builder) super.setIcon(icon);
        }

        public Builder setIconAttribute(int attrId) {
            return (Builder) super.setIconAttribute(attrId);
        }

        public Builder setPositiveButton(int textId, OnClickListener listener) {
            return (Builder) super.setPositiveButton(textId, listener);
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            return (Builder) super.setPositiveButton(text, listener);
        }

        public Builder setNegativeButton(int textId, OnClickListener listener) {
            return (Builder) super.setNegativeButton(textId, listener);
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            return (Builder) super.setNegativeButton(text, listener);
        }

        public Builder setNeutralButton(int textId, OnClickListener listener) {
            return (Builder) super.setNeutralButton(textId, listener);
        }

        public Builder setNeutralButton(CharSequence text, OnClickListener listener) {
            return (Builder) super.setNeutralButton(text, listener);
        }

        public Builder setCancelable(boolean cancelable) {
            return (Builder) super.setCancelable(cancelable);
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            return (Builder) super.setOnCancelListener(onCancelListener);
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            return (Builder) super.setOnDismissListener(onDismissListener);
        }

        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            return (Builder) super.setOnKeyListener(onKeyListener);
        }

        public Builder setAdapter(ListAdapter adapter, OnClickListener listener) {
            return (Builder) super.setAdapter(adapter, listener);
        }

        public Builder setCursor(Cursor cursor, OnClickListener listener, String labelColumn) {
            return (Builder) super.setCursor(cursor, listener, labelColumn);
        }

        public Builder setMultiChoiceItems(int itemsId, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            return (Builder) super.setMultiChoiceItems(itemsId, checkedItems, listener);
        }

        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            return (Builder) super.setMultiChoiceItems(items, checkedItems, listener);
        }

        public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, OnMultiChoiceClickListener listener) {
            return (Builder) super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener);
        }

        public Builder setSingleChoiceItems(int itemsId, int checkedItem, OnClickListener listener) {
            return (Builder) super.setSingleChoiceItems(itemsId, checkedItem, listener);
        }

        public Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, OnClickListener listener) {
            return (Builder) super.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener);
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, OnClickListener listener) {
            return (Builder) super.setSingleChoiceItems(items, checkedItem, listener);
        }

        public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, OnClickListener listener) {
            return (Builder) super.setSingleChoiceItems(adapter, checkedItem, listener);
        }

        public Builder setOnItemSelectedListener(OnItemSelectedListener listener) {
            return (Builder) super.setOnItemSelectedListener(listener);
        }

        public Builder setView(int layoutResId) {
            return (Builder) super.setView(layoutResId);
        }

        public Builder setView(View view) {
            return (Builder) super.setView(view);
        }

        public Builder setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
            return (Builder) super.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
        }

        public Builder setInverseBackgroundForced(boolean useInverseBackground) {
            return (Builder) super.setInverseBackgroundForced(useInverseBackground);
        }

        public Builder setItems(int itemsId, OnClickListener listener) {
            return (Builder) super.setItems(itemsId, listener);
        }

        public Builder setItems(CharSequence[] items, OnClickListener listener) {
            return (Builder) super.setItems(items, listener);
        }

        public ColorAlertDialogCustom initDialog() {
            return new ColorAlertDialogCustom(this.P.mContext, this.mTheme, false, this.mDeleteDialogOption);
        }

        public ColorAlertDialogCustom create() {
            return (ColorAlertDialogCustom) super.create();
        }
    }

    public static class ListItemAttr {
        private Boolean enable;
        private Boolean textBold;
        private Integer textColor;

        public ListItemAttr(Integer textColor, Boolean bold) {
            this.textColor = textColor;
            this.textBold = bold;
        }

        public ListItemAttr(Integer textColor, Boolean bold, Boolean enable) {
            this.textColor = textColor;
            this.textBold = bold;
            this.enable = enable;
        }

        public Integer getItemColor() {
            return this.textColor;
        }

        public Boolean getItemBold() {
            return this.textBold;
        }

        public Boolean getItemEnable() {
            return this.enable;
        }
    }

    protected ColorAlertDialogCustom(Context context) {
        this(context, ColorAlertDialog.resolveDialogTheme(context, 0), true);
    }

    protected ColorAlertDialogCustom(Context context, int theme) {
        this(context, theme, true);
    }

    public ColorAlertDialogCustom(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, ColorAlertDialog.resolveDialogTheme(context, 0));
        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
    }

    ColorAlertDialogCustom(Context context, int theme, boolean createThemeContextWrapper) {
        super(context, theme, createThemeContextWrapper);
    }

    protected ColorAlertDialogCustom(Context context, int theme, boolean createThemeContextWrapper, int mDeleteDialogOption) {
        super(context, theme, createThemeContextWrapper, mDeleteDialogOption);
    }

    void createDialog(int deleteDialogOption) {
        super.createDialog(deleteDialogOption);
        setCanceledOnTouchOutside(false);
    }

    public ColorAlertControllerCustom initController() {
        return new ColorAlertControllerCustom(getContext(), this, getWindow());
    }

    public void setListItemState(ListItemAttr[] itemsAttrs, boolean isDynaChange) {
        ((ColorAlertControllerCustom) this.mAlert).setListItemState(itemsAttrs, isDynaChange);
        if (isDynaChange && getListView() != null) {
            BaseAdapter adapter = (BaseAdapter) getListView().getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setButtonIsBold(int positive, int negative, int neutral) {
        ((ColorAlertControllerCustom) this.mAlert).setButtonIsBold(positive, negative, neutral);
    }
}
