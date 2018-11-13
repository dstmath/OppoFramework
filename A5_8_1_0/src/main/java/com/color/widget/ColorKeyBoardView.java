package com.color.widget;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.oppo.widget.OppoPasswordEntryKeyboardHelper;
import com.oppo.widget.OppoPasswordEntryKeyboardView;
import oppo.R;

public class ColorKeyBoardView extends LinearLayout implements OnClickListener {
    public static final int KEYBOARD_MODE_ALPHA = 0;
    public static final int KEYBOARD_MODE_NUMERIC = 1;
    public static final int KEYBOARD_MODE_SECURITY_NUMERIC = 2;
    private static final String TAG = "ColorKeyBoardView";
    private static final int TARNSLATION = 80;
    public static final int UNLOCK_TYPE = 1;
    private String mAccessClose;
    private String mAccessDetail;
    private ImageView mCloseButton;
    private Context mContext;
    private int mDefaultType;
    private ImageView mDetailButton;
    private OppoPasswordEntryKeyboardHelper mHelper;
    private int mInputType;
    private OppoPasswordEntryKeyboardView mKeyboardView;
    private OnClickButtonListener mOnClickButtonListener;
    private OnClickSwitchListener mOnClickSwitchListener;
    private boolean mSecureType;
    private TextView mTextView;
    private RelativeLayout mTopView;

    public interface OnClickButtonListener {
        void onClickButton();
    }

    public interface OnClickSwitchListener {
        void onClickSwitch();
    }

    public ColorKeyBoardView(Context context) {
        this(context, null);
    }

    public ColorKeyBoardView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393275);
    }

    public ColorKeyBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTopView = null;
        this.mDefaultType = 0;
        this.mOnClickButtonListener = null;
        this.mOnClickSwitchListener = null;
        this.mKeyboardView = null;
        this.mSecureType = true;
        this.mTextView = null;
        this.mHelper = null;
        this.mInputType = -1;
        this.mContext = context;
        this.mSecureType = context.obtainStyledAttributes(attrs, R.styleable.ColorKeyboardView, defStyle, 0).getBoolean(0, true);
        LayoutInflater.from(context).inflate(201917585, this, true);
        this.mCloseButton = (ImageView) findViewById(201458968);
        this.mDetailButton = (ImageView) findViewById(201458990);
        this.mKeyboardView = (OppoPasswordEntryKeyboardView) findViewById(201458970);
        this.mTopView = (RelativeLayout) findViewById(201458984);
        this.mTextView = (TextView) findViewById(201458969);
        if (this.mSecureType) {
            this.mKeyboardView.setKeyboardType(1);
        } else {
            if (this.mTopView != null) {
                this.mTopView.setVisibility(8);
            }
            this.mKeyboardView.setKeyboardType(2);
        }
        this.mCloseButton.setOnClickListener(this);
        this.mDetailButton.setOnClickListener(this);
        this.mKeyboardView.setProximityCorrectionEnabled(true);
        if (this.mSecureType && this.mContext.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism")) {
            handleLandHookface(context);
        }
        this.mAccessClose = context.getResources().getString(201590177);
        this.mAccessDetail = context.getResources().getString(201590178);
        if (this.mCloseButton != null) {
            this.mCloseButton.setAccessibilityDelegate(new AccessibilityDelegate() {
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    if (ColorKeyBoardView.this.mAccessClose != null) {
                        info.setContentDescription(ColorKeyBoardView.this.mAccessClose);
                    }
                    info.setClassName(Button.class.getName());
                }
            });
        }
        if (this.mDetailButton != null) {
            this.mDetailButton.setAccessibilityDelegate(new AccessibilityDelegate() {
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    if (ColorKeyBoardView.this.mAccessDetail != null) {
                        info.setContentDescription(ColorKeyBoardView.this.mAccessDetail);
                    }
                    info.setClassName(Button.class.getName());
                }
            });
        }
    }

    private void handleLandHookface(Context context) {
        if (context.getResources().getConfiguration().orientation == 2) {
            LayoutParams detailLp = (LayoutParams) this.mDetailButton.getLayoutParams();
            LayoutParams closeLp = (LayoutParams) this.mCloseButton.getLayoutParams();
            if (detailLp != null && closeLp != null) {
                detailLp.setMarginsRelative(TARNSLATION, 0, 0, 0);
                closeLp.setMarginsRelative(0, 0, TARNSLATION, 0);
            }
        }
    }

    public void onClick(View v) {
        if (v.getId() == 201458968 && this.mOnClickButtonListener != null) {
            this.mOnClickButtonListener.onClickButton();
        }
        if (v.getId() == 201458990 && this.mOnClickSwitchListener != null) {
            this.mOnClickSwitchListener.onClickSwitch();
        }
    }

    private void showDetailDialog() {
        View convertView = LayoutInflater.from(getContext()).inflate(201917589, null);
        Builder dialogBuilder = new Builder(getContext(), 201524243);
        dialogBuilder.setTitle(201590160);
        dialogBuilder.setView(convertView);
        dialogBuilder.setPositiveButton(201590159, null);
        AlertDialog dialog = dialogBuilder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams p = window.getAttributes();
        p.ignoreHomeMenuKey = 1;
        p.type = 2003;
        p.dimAmount = 0.5f;
        p.setTitle("SecurityInputMethodDialog");
        window.setAttributes(p);
        window.setFlags(131074, 131072);
        dialog.setCanceledOnTouchOutside(false);
        try {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "excetion e= " + e);
        }
    }

    private Typeface getTextFont() {
        try {
            return Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
        } catch (RuntimeException e) {
            return Typeface.createFromFile("/system/fonts/Roboto-Light.ttf");
        } catch (Exception e2) {
            return Typeface.DEFAULT;
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mSecureType) {
            this.mHelper = new OppoPasswordEntryKeyboardHelper(this.mContext, this.mKeyboardView, null, 1, true);
            this.mKeyboardView.setKeyboardType(1);
            if (this.mInputType == 0) {
                this.mHelper.setKeyboardMode(0);
            } else if (this.mInputType == 2) {
                this.mHelper.setKeyboardMode(2);
            }
        }
    }

    public OppoPasswordEntryKeyboardView getColorKeyBoardView() {
        if (this.mKeyboardView != null) {
            return this.mKeyboardView;
        }
        return null;
    }

    public void setOnClickButtonListener(OnClickButtonListener listener) {
        this.mOnClickButtonListener = listener;
    }

    public void setOnClickSwitchListener(OnClickSwitchListener listener) {
        this.mOnClickSwitchListener = listener;
    }

    public void setKeyBoardType(int type) {
        this.mDefaultType = type;
    }

    public void setKeyboardHelper(OppoPasswordEntryKeyboardHelper helper) {
        this.mHelper = helper;
    }

    public OppoPasswordEntryKeyboardHelper getKeyboardHelper() {
        return this.mHelper;
    }

    public void setInputType(int inputType) {
        this.mInputType = inputType;
    }

    public int getInputType() {
        return this.mInputType;
    }
}
