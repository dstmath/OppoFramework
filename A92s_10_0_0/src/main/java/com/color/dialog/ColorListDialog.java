package com.color.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ColorListDialog implements DialogInterface {
    private ListAdapter mAdapter;
    private AlertDialog.Builder mBuilder;
    private Context mContext;
    private int mCustomRes;
    private View mCustomView;
    /* access modifiers changed from: private */
    public AlertDialog mDialog;
    private boolean mHasCustom;
    private CharSequence[] mItems;
    private CharSequence mMessage;
    /* access modifiers changed from: private */
    public TextView mMessageView;
    /* access modifiers changed from: private */
    public DialogInterface.OnClickListener mOnClickListener;
    private int[] mTextAppearances;

    public ColorListDialog(Context context) {
        this.mContext = context;
        this.mBuilder = new AlertDialog.Builder(context);
    }

    public ColorListDialog(Context context, int theme) {
        this.mContext = context;
        this.mBuilder = new AlertDialog.Builder(context, theme);
    }

    public ColorListDialog setItems(CharSequence[] items, int[] textAppearances, DialogInterface.OnClickListener onClickListener) {
        this.mItems = items;
        this.mTextAppearances = textAppearances;
        this.mOnClickListener = onClickListener;
        return this;
    }

    public ColorListDialog setTitle(CharSequence title) {
        this.mBuilder.setTitle(title);
        return this;
    }

    public ColorListDialog setMessage(CharSequence message) {
        this.mMessage = message;
        return this;
    }

    public ColorListDialog setCustomView(int layout) {
        this.mCustomRes = layout;
        this.mHasCustom = true;
        return this;
    }

    public ColorListDialog setCustomView(View customView) {
        this.mCustomView = customView;
        this.mHasCustom = true;
        return this;
    }

    public void show() {
        if (this.mDialog == null) {
            this.mDialog = create();
        }
        this.mDialog.show();
    }

    public AlertDialog create() {
        View layout = LayoutInflater.from(this.mContext).inflate(201917620, (ViewGroup) null);
        setupMessage(layout);
        setupCustomPanel(layout);
        if (!(this.mItems == null && this.mAdapter == null)) {
            setupListPanel(layout);
        }
        this.mBuilder.setView(layout);
        return this.mBuilder.create();
    }

    @Override // android.content.DialogInterface
    public void cancel() {
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.cancel();
        }
    }

    public AlertDialog getDialog() {
        if (this.mDialog == null) {
            this.mDialog = create();
        }
        return this.mDialog;
    }

    @Override // android.content.DialogInterface
    public void dismiss() {
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    public boolean isShowing() {
        AlertDialog alertDialog = this.mDialog;
        return alertDialog != null && alertDialog.isShowing();
    }

    private void setupMessage(View parentPanel) {
        this.mMessageView = (TextView) parentPanel.findViewById(201459061);
        this.mMessageView.setText(this.mMessage);
        if (TextUtils.isEmpty(this.mMessage)) {
            this.mMessageView.setVisibility(8);
            return;
        }
        parentPanel.findViewById(201459062).setVisibility(0);
        this.mMessageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class com.color.dialog.ColorListDialog.AnonymousClass1 */

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                if (ColorListDialog.this.mMessageView.getLineCount() > 1) {
                    ColorListDialog.this.mMessageView.setTextAlignment(2);
                } else {
                    ColorListDialog.this.mMessageView.setTextAlignment(4);
                }
                ColorListDialog.this.mMessageView.setText(ColorListDialog.this.mMessageView.getText());
                ColorListDialog.this.mMessageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setupCustomPanel(View parentPanel) {
        if (this.mHasCustom) {
            FrameLayout customPanel = (FrameLayout) parentPanel.findViewById(201459063);
            View view = this.mCustomView;
            if (view != null) {
                customPanel.addView(view);
            } else {
                customPanel.addView(LayoutInflater.from(this.mContext).inflate(this.mCustomRes, (ViewGroup) null));
            }
        }
    }

    private void setupListPanel(View parentPanel) {
        ListView listView = (ListView) parentPanel.findViewById(201459064);
        listView.setAdapter(getAdapter());
        if (this.mOnClickListener != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                /* class com.color.dialog.ColorListDialog.AnonymousClass2 */

                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    ColorListDialog.this.mOnClickListener.onClick(ColorListDialog.this.mDialog, position);
                }
            });
        }
    }

    private ListAdapter getAdapter() {
        ListAdapter listAdapter = this.mAdapter;
        return listAdapter == null ? new Adapter(this.mContext, this.mItems, this.mTextAppearances) : listAdapter;
    }

    public ColorListDialog setAdapter(ListAdapter adapter) {
        this.mAdapter = adapter;
        return this;
    }

    private static class Adapter extends BaseAdapter {
        private static final int LAYOUT = 201917621;
        private Context mContext;
        private CharSequence[] mItems;
        private int[] mTextAppearances;

        Adapter(Context context, CharSequence[] items, int[] textAppearances) {
            this.mContext = context;
            this.mItems = items;
            this.mTextAppearances = textAppearances;
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

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return (long) position;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View convertView2 = getViewInternal(position, convertView, parent);
            resetPadding(position, convertView2);
            return convertView2;
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View
         arg types: [int, android.view.ViewGroup, int]
         candidates:
          android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View
          android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View */
        private View getViewInternal(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(this.mContext).inflate(201917621, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mTextView = (TextView) convertView.findViewById(16908308);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.mTextView.setText(getItem(position));
            int[] iArr = this.mTextAppearances;
            if (iArr != null) {
                int textAppearance = iArr[position];
                if (textAppearance > 0) {
                    viewHolder.mTextView.setTextAppearance(this.mContext, textAppearance);
                } else {
                    viewHolder.mTextView.setTextAppearance(this.mContext, 201524259);
                }
            }
            return convertView;
        }

        private void resetPadding(int position, View convertView) {
            int paddingOffset = this.mContext.getResources().getDimensionPixelSize(201655805);
            int paddingTop = this.mContext.getResources().getDimensionPixelSize(201655815);
            int paddingLeft = this.mContext.getResources().getDimensionPixelSize(201655813);
            int paddingBottom = this.mContext.getResources().getDimensionPixelSize(201655816);
            int paddingRight = this.mContext.getResources().getDimensionPixelSize(201655814);
            int minHeight = this.mContext.getResources().getDimensionPixelSize(201655810);
            if (getCount() <= 1) {
                return;
            }
            if (position == getCount() - 1) {
                convertView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + paddingOffset);
                convertView.setMinimumHeight(minHeight + paddingOffset);
            } else if (position == 0) {
                convertView.setPadding(paddingLeft, paddingTop + paddingOffset, paddingRight, paddingBottom);
                convertView.setMinimumHeight(minHeight + paddingOffset);
            } else {
                convertView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                convertView.setMinimumHeight(minHeight);
            }
        }
    }

    private static class ViewHolder {
        TextView mTextView;

        private ViewHolder() {
        }
    }
}
