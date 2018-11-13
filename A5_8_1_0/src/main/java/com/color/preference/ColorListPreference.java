package com.color.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import oppo.R;

public class ColorListPreference extends ListPreference {
    Context mContext;
    Drawable mJumpRes;
    CharSequence mStatusText_1;
    CharSequence mStatusText_2;
    CharSequence mStatusText_3;
    TextView mSummaryView;

    public ColorListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorJumpPreference, defStyleAttr, 0);
        this.mJumpRes = a.getDrawable(0);
        this.mStatusText_1 = a.getText(1);
        this.mStatusText_2 = a.getText(2);
        this.mStatusText_3 = a.getText(3);
        a.recycle();
    }

    public ColorListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842897);
    }

    public ColorListPreference(Context context) {
        this(context, null);
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
