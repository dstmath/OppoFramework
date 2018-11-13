package com.color.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import oppo.R;

public class ColorJumpPreference extends Preference {
    public static final int FORCE_CLICK = 1;
    public static final int FORCE_UNCLICK = 2;
    public static final int NORMAL = 0;
    private int mClickStyle;
    Context mContext;
    Drawable mJumpRes;
    CharSequence mStatusText_1;
    CharSequence mStatusText_2;
    CharSequence mStatusText_3;
    TextView mSummaryView;

    public ColorJumpPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mClickStyle = 0;
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorJumpPreference, defStyleAttr, 0);
        this.mJumpRes = a.getDrawable(0);
        this.mStatusText_1 = a.getText(1);
        this.mStatusText_2 = a.getText(2);
        this.mStatusText_3 = a.getText(3);
        this.mClickStyle = a.getInt(4, 0);
        a.recycle();
    }

    public ColorJumpPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorJumpPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 201393249);
    }

    public ColorJumpPreference(Context context) {
        this(context, null);
    }

    public void setClickStyle(int style) {
        if (this.mClickStyle != style) {
            this.mClickStyle = style;
            notifyChanged();
        }
    }

    public int getClickStyle() {
        return this.mClickStyle;
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
        View preference = view.findViewById(201458880);
        if (preference != null) {
            switch (this.mClickStyle) {
                case 0:
                    preference.setClickable(false);
                    break;
                case 1:
                    preference.setClickable(false);
                    break;
                case 2:
                    preference.setClickable(true);
                    break;
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
