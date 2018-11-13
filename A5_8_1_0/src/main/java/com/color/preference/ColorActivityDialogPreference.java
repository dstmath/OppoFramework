package com.color.preference;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.color.actionbar.app.ColorActionBarUtil;
import oppo.R;

public class ColorActivityDialogPreference extends ListPreference {
    public static final int STYLE_GROUP = 0;
    public static final int STYLE_TILE = 1;
    private int mColorClickedDialogEntryIndex;
    Context mContext;
    private String mDefaultBackTitle;
    private Dialog mDialog;
    private int mGroupPadding;
    Drawable mJumpRes;
    private int mListViewListStyle;
    CharSequence mStatusText_1;
    CharSequence mStatusText_2;
    CharSequence mStatusText_3;

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    public ColorActivityDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mListViewListStyle = 0;
        this.mGroupPadding = 0;
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorJumpPreference, defStyleAttr, 0);
        this.mJumpRes = a.getDrawable(0);
        this.mStatusText_1 = a.getText(1);
        this.mStatusText_2 = a.getText(2);
        this.mStatusText_3 = a.getText(3);
        a.recycle();
        this.mGroupPadding = context.getResources().getDimensionPixelSize(201655564);
        this.mDefaultBackTitle = context.getResources().getString(201590122);
    }

    public ColorActivityDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorActivityDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842897);
    }

    public ColorActivityDialogPreference(Context context) {
        this(context, null);
    }

    private int getValueIndex() {
        return findIndexOfValue(getValue());
    }

    protected void onClick() {
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            showDialog(null);
        }
    }

    protected void showDialog(Bundle state) {
        this.mColorClickedDialogEntryIndex = getValueIndex();
        final Dialog dialog = new Dialog(getContext(), 201524239) {
            public boolean onMenuItemSelected(int featureId, MenuItem item) {
                if (item.getItemId() != 16908332) {
                    return super.onMenuItemSelected(featureId, item);
                }
                dismiss();
                return true;
            }
        };
        this.mDialog = dialog;
        dialog.getWindow().addFlags(Integer.MIN_VALUE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(1280);
        dialog.getWindow().getDecorView().setSystemUiVisibility(8192);
        final ListView list = (ListView) LayoutInflater.from(getContext()).inflate(201917549, null);
        list.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                if (!(v == null || insets == null)) {
                    v.setPadding(v.getPaddingStart(), insets.getSystemWindowInsetTop() + ColorActivityDialogPreference.this.getContext().getResources().getDimensionPixelOffset(201654459), v.getPaddingEnd(), v.getPaddingBottom());
                }
                return insets;
            }
        });
        ListAdapter adapter = new CheckedItemAdapter(getContext(), 201917550, 201458933, getEntries()) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position == ColorActivityDialogPreference.this.mColorClickedDialogEntryIndex) {
                    list.setItemChecked(list.getHeaderViewsCount() + position, true);
                }
                int groupSize = getCount();
                View diver = view.findViewById(201458963);
                if (groupSize == 1 || position == groupSize - 1) {
                    if (diver != null) {
                        diver.setVisibility(8);
                    }
                } else if (diver != null) {
                    diver.setVisibility(0);
                }
                return view;
            }
        };
        CharSequence charSequence = null;
        if (this.mContext instanceof Activity) {
            ActionBar actActionbar = ((Activity) this.mContext).getActionBar();
            if (actActionbar != null) {
                charSequence = (String) actActionbar.getTitle();
            }
        }
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                ColorActivityDialogPreference.this.mColorClickedDialogEntryIndex = position - list.getHeaderViewsCount();
                ColorActivityDialogPreference.this.onClick(null, -1);
                dialog.dismiss();
            }
        });
        list.setChoiceMode(1);
        dialog.setContentView(list);
        dialog.setOnDismissListener(this);
        dialog.show();
        ActionBar act = dialog.getActionBar();
        if (act != null) {
            act.setTitle(getDialogTitle());
            act.setDisplayHomeAsUpEnabled(true);
            if (charSequence == null || charSequence.equals("")) {
                charSequence = this.mDefaultBackTitle;
            }
            ColorActionBarUtil.setBackTitle(act, charSequence);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && this.mColorClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String value = getEntryValues()[this.mColorClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    public CharSequence getStatusText1() {
        return this.mStatusText_1;
    }

    public void setStatusText1(CharSequence text) {
        if ((text == null && this.mStatusText_1 != null) || (text != null && (text.equals(this.mStatusText_1) ^ 1) != 0)) {
            this.mStatusText_1 = text;
            notifyChanged();
        }
    }

    public CharSequence getStatusText2() {
        return this.mStatusText_2;
    }

    public void setStatusText2(CharSequence text) {
        if ((text == null && this.mStatusText_2 != null) || (text != null && (text.equals(this.mStatusText_2) ^ 1) != 0)) {
            this.mStatusText_2 = text;
            notifyChanged();
        }
    }

    public CharSequence getStatusText3() {
        return this.mStatusText_3;
    }

    public void setStatusText3(CharSequence text) {
        if ((text == null && this.mStatusText_3 != null) || (text != null && (text.equals(this.mStatusText_3) ^ 1) != 0)) {
            this.mStatusText_3 = text;
            notifyChanged();
        }
    }

    public void setJump(Drawable jump) {
        if (this.mJumpRes != jump) {
            this.mJumpRes = jump;
            notifyChanged();
        }
    }

    public void setJump(int iconResId) {
        setJump(this.mContext.getDrawable(iconResId));
    }

    public Drawable getJump() {
        return this.mJumpRes;
    }

    public void setListStyle(int style) {
        this.mListViewListStyle = style;
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        View jump = view.findViewById(201457920);
        if (jump != null) {
            if (this.mJumpRes != null) {
                jump.setBackground(this.mJumpRes);
                jump.setVisibility(0);
            } else {
                jump.setVisibility(8);
            }
        }
        TextView status1 = (TextView) view.findViewById(201457921);
        if (status1 != null) {
            CharSequence statusText_1 = this.mStatusText_1;
            if (TextUtils.isEmpty(statusText_1)) {
                status1.setVisibility(8);
            } else {
                status1.setText(statusText_1);
                status1.setVisibility(0);
            }
        }
        TextView status2 = (TextView) view.findViewById(201457922);
        if (status2 != null) {
            CharSequence statusText_2 = this.mStatusText_2;
            if (TextUtils.isEmpty(statusText_2)) {
                status2.setVisibility(8);
            } else {
                status2.setText(statusText_2);
                status2.setVisibility(0);
            }
        }
        TextView status3 = (TextView) view.findViewById(201457923);
        if (status3 != null) {
            CharSequence statusText_3 = this.mStatusText_3;
            if (TextUtils.isEmpty(statusText_3)) {
                status3.setVisibility(8);
                return;
            }
            status3.setText(statusText_3);
            status3.setVisibility(0);
        }
    }
}
