package com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.color.util.ColorContextUtil;

public abstract class ColorBaseAlertController {
    private int mDialogType = 0;
    boolean mMessageNeedScroll = false;

    /* access modifiers changed from: package-private */
    public abstract Button getAlertControllerButtonNegative();

    /* access modifiers changed from: package-private */
    public abstract CharSequence getAlertControllerButtonNegativeText();

    /* access modifiers changed from: package-private */
    public abstract Button getAlertControllerButtonNeutral();

    /* access modifiers changed from: package-private */
    public abstract CharSequence getAlertControllerButtonNeutralText();

    /* access modifiers changed from: package-private */
    public abstract Button getAlertControllerButtonPositive();

    /* access modifiers changed from: package-private */
    public abstract CharSequence getAlertControllerButtonPositiveText();

    /* access modifiers changed from: package-private */
    public abstract CharSequence getAlertControllerTitle();

    /* access modifiers changed from: protected */
    public abstract int selectContentViewWrapper();

    /* access modifiers changed from: package-private */
    public abstract void setupViewWrapper();

    public boolean isMessageNeedScroll() {
        return this.mMessageNeedScroll;
    }

    public void setMessageNeedScroll(boolean messageNeedScroll) {
        this.mMessageNeedScroll = messageNeedScroll;
    }

    public int getDialogType() {
        return this.mDialogType;
    }

    public void setDialogType(int dialogType) {
        this.mDialogType = dialogType;
    }

    public void setButtonIsBold(int positive, int negative, int neutral) {
    }

    public void setDeleteDialogOption(int option) {
    }

    public static abstract class BaseAlertParams {
        public int mDialogType;
        public boolean mMessageNeedScroll;
        public CharSequence[] mSummaries;

        public abstract Context getContext();

        public abstract void setItems(CharSequence[] charSequenceArr);

        public abstract void setOnClickListener(DialogInterface.OnClickListener onClickListener);

        public boolean needHookAdapter(boolean isSingleChoice, boolean isMultiChoice, CharSequence[] summaries) {
            return !isSingleChoice && !isMultiChoice && ColorContextUtil.isOppoStyle(getContext());
        }

        public ListAdapter getHookAdapter(Context context, CharSequence title, CharSequence message, CharSequence[] items, CharSequence[] summaries) {
            return new SummaryAdapter(context, !TextUtils.isEmpty(title), !TextUtils.isEmpty(message), items, summaries);
        }

        public void hookApply(ColorBaseAlertController alertController) {
            if (ColorContextUtil.isOppoStyle(getContext())) {
                alertController.mMessageNeedScroll = this.mMessageNeedScroll;
                alertController.setDialogType(this.mDialogType);
            }
        }
    }

    public static class SummaryAdapter extends BaseAdapter {
        private static final int LAYOUT = 201917546;
        private Context mContext;
        private boolean mHasMessage;
        private boolean mHasTitle;
        private CharSequence[] mItems;
        private CharSequence[] mSummaries;

        public SummaryAdapter(Context context, boolean hasTitle, boolean hasMessage, CharSequence[] items, CharSequence[] summaries) {
            this.mHasTitle = hasTitle;
            this.mHasMessage = hasMessage;
            this.mContext = context;
            this.mItems = items;
            this.mSummaries = summaries;
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View
         arg types: [int, android.view.ViewGroup, int]
         candidates:
          android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View
          android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View */
        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View convertView2 = LayoutInflater.from(this.mContext).inflate(201917546, parent, false);
            TextView summaryView = (TextView) convertView2.findViewById(201458935);
            CharSequence item = getItem(position);
            CharSequence summary = getSummary(position);
            ((TextView) convertView2.findViewById(16908308)).setText(item);
            if (TextUtils.isEmpty(summary)) {
                summaryView.setVisibility(8);
            } else {
                summaryView.setVisibility(0);
                summaryView.setText(summary);
            }
            resetPadding(position, convertView2);
            return convertView2;
        }

        private void resetPadding(int position, View convertView) {
            int paddingOffset = this.mContext.getResources().getDimensionPixelSize(201655805);
            int paddingTop = convertView.getPaddingTop();
            int paddingLeft = convertView.getPaddingLeft();
            int paddingBottom = convertView.getPaddingBottom();
            int paddingRight = convertView.getPaddingRight();
            if (getCount() <= 1) {
                return;
            }
            if (position == getCount() - 1) {
                convertView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + paddingOffset);
                convertView.setMinimumHeight(convertView.getMinimumHeight() + paddingOffset);
            } else if (!this.mHasTitle && !this.mHasMessage) {
                if (position == 0) {
                    convertView.setPadding(paddingLeft, paddingTop + paddingOffset, paddingRight, paddingBottom);
                    convertView.setMinimumHeight(convertView.getMinimumHeight() + paddingOffset);
                    return;
                }
                convertView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            }
        }

        @Override // android.widget.Adapter
        public int getCount() {
            CharSequence[] charSequenceArr = this.mItems;
            if (charSequenceArr == null) {
                return 0;
            }
            return charSequenceArr.length;
        }

        @Override // android.widget.Adapter
        public CharSequence getItem(int position) {
            CharSequence[] charSequenceArr = this.mItems;
            if (charSequenceArr == null) {
                return null;
            }
            return charSequenceArr[position];
        }

        public CharSequence getSummary(int position) {
            CharSequence[] charSequenceArr = this.mSummaries;
            if (charSequenceArr != null && position < charSequenceArr.length) {
                return charSequenceArr[position];
            }
            return null;
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return (long) position;
        }

        @Override // android.widget.Adapter, android.widget.BaseAdapter
        public boolean hasStableIds() {
            return true;
        }
    }
}
