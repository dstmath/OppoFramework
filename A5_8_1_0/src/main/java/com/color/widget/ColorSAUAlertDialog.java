package com.color.widget;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ColorSAUAlertDialog {
    public static final int TYPE_ALREADY_DOWNLOAD = 2;
    public static final int TYPE_BUTTON_DOWNLOAD_EXIT = 9;
    public static final int TYPE_BUTTON_DOWNLOAD_LATER = 8;
    public static final int TYPE_BUTTON_INSTALL_EXIT = 7;
    public static final int TYPE_BUTTON_INSTALL_LATER = 6;
    public static final int TYPE_MOBILE_PROMPT = 1;
    public static final int TYPE_NO_PROMPT = 0;
    private AlertDialog mAlertDialog;
    private Context mContext;
    private TextView mNetworkPrompt;
    private OnButtonClickListener mOnButtonClickListener;
    private TextView mSizeStr;
    private TextView mUpdateDescription;
    private TextView mVersionName;

    public interface OnButtonClickListener {
        void onClick(int i);
    }

    public void setOnButtonClickListener(OnButtonClickListener e) {
        this.mOnButtonClickListener = e;
    }

    public ColorSAUAlertDialog(Context context) {
        this.mContext = context;
        View layout = LayoutInflater.from(this.mContext).inflate(201917539, null);
        this.mVersionName = (TextView) layout.findViewById(201458923);
        this.mSizeStr = (TextView) layout.findViewById(201458922);
        this.mNetworkPrompt = (TextView) layout.findViewById(201458921);
        this.mUpdateDescription = (TextView) layout.findViewById(201458920);
        this.mAlertDialog = new Builder(this.mContext).setTitle(201590104).setView(layout).create();
    }

    public void setCancelable(boolean flag) {
        this.mAlertDialog.setCancelable(flag);
    }

    public void setVersionName(String versionName) {
        this.mVersionName.setText(((String) this.mVersionName.getText()) + versionName);
    }

    public void setSizeStr(String sizeStr) {
        this.mSizeStr.setText(((String) this.mSizeStr.getText()) + sizeStr);
    }

    public void setNetworkPrompt(int type) {
        switch (type) {
            case 0:
                this.mNetworkPrompt.setVisibility(8);
                return;
            case 1:
                this.mNetworkPrompt.setText(201590098);
                return;
            case 2:
                this.mNetworkPrompt.setText(201590099);
                return;
            default:
                return;
        }
    }

    public void setUpdateDescription(String updateDescription) {
        this.mUpdateDescription.setText(updateDescription);
    }

    public void setButtonType(int type) {
        if (this.mAlertDialog != null) {
            switch (type) {
                case 6:
                    setButtons(this.mContext.getString(201590105), this.mContext.getString(201590103));
                    return;
                case 7:
                    setButtons(this.mContext.getString(201590100), this.mContext.getString(201590103));
                    return;
                case 8:
                    setButtons(this.mContext.getString(201590101), this.mContext.getString(201590102));
                    return;
                case 9:
                    setButtons(this.mContext.getString(201590100), this.mContext.getString(201590102));
                    return;
                default:
                    return;
            }
        }
    }

    private void setButtons(String negative, String positive) {
        this.mAlertDialog.setButton(-2, negative, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ColorSAUAlertDialog.this.mOnButtonClickListener.onClick(which);
            }
        });
        this.mAlertDialog.setButton(-1, positive, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ColorSAUAlertDialog.this.mOnButtonClickListener.onClick(which);
            }
        });
    }

    public AlertDialog getDialog() {
        return this.mAlertDialog;
    }

    public void show() {
        if (this.mAlertDialog != null) {
            this.mAlertDialog.show();
        }
    }

    public void dismiss() {
        if (this.mAlertDialog != null) {
            this.mAlertDialog.dismiss();
        }
    }
}
