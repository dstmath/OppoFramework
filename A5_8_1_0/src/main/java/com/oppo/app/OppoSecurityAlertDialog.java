package com.oppo.app;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class OppoSecurityAlertDialog {
    private Context mContext;
    private Dialog mDialog;
    private boolean mIsCheck;
    private TextView mMessage;
    private OnKeyListener mOnKeyListener;
    private OnSelectedListener mOnSelectedListener;
    private CheckBox mRemember;
    private View mView;

    public interface OnSelectedListener {
        void onSelected(DialogInterface dialogInterface, boolean z, int i);
    }

    public OppoSecurityAlertDialog(Context context, int titleId, int contentId, boolean always, boolean isCheck) {
        this(context, titleId, contentId, always, isCheck, 201589896, 201589895);
    }

    public OppoSecurityAlertDialog(Context context, int titleId, int contentId, boolean always, boolean isCheck, int positiveStringId, int negativeStringId) {
        this.mView = null;
        this.mMessage = null;
        this.mRemember = null;
        this.mIsCheck = false;
        this.mOnKeyListener = new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == 4 && event.getAction() == 0 && OppoSecurityAlertDialog.this.mDialog != null && OppoSecurityAlertDialog.this.mDialog.isShowing()) {
                    OppoSecurityAlertDialog.this.mOnSelectedListener.onSelected(OppoSecurityAlertDialog.this.mDialog, OppoSecurityAlertDialog.this.mIsCheck, -2);
                    OppoSecurityAlertDialog.this.mDialog.dismiss();
                }
                return false;
            }
        };
        this.mContext = context;
        this.mView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(201917514, null);
        this.mMessage = (TextView) this.mView.findViewById(201458845);
        this.mMessage.setText(this.mContext.getText(contentId));
        this.mRemember = (CheckBox) this.mView.findViewById(201458846);
        if (always) {
            this.mIsCheck = isCheck;
            this.mRemember.setChecked(this.mIsCheck);
            this.mRemember.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    OppoSecurityAlertDialog.this.mIsCheck = isChecked;
                    if (OppoSecurityAlertDialog.this.mOnSelectedListener != null) {
                        OppoSecurityAlertDialog.this.mOnSelectedListener.onSelected(OppoSecurityAlertDialog.this.mDialog, OppoSecurityAlertDialog.this.mIsCheck, 0);
                    }
                }
            });
        } else {
            this.mRemember.setVisibility(8);
        }
        this.mDialog = new Builder(this.mContext).setTitle(this.mContext.getText(titleId)).setView(this.mView).setPositiveButton(positiveStringId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (OppoSecurityAlertDialog.this.mOnSelectedListener != null) {
                    OppoSecurityAlertDialog.this.mOnSelectedListener.onSelected(dialog, OppoSecurityAlertDialog.this.mIsCheck, whichButton);
                }
            }
        }).setNegativeButton(negativeStringId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (OppoSecurityAlertDialog.this.mOnSelectedListener != null) {
                    OppoSecurityAlertDialog.this.mOnSelectedListener.onSelected(dialog, OppoSecurityAlertDialog.this.mIsCheck, whichButton);
                }
            }
        }).create();
        this.mDialog.setCancelable(false);
        this.mDialog.setOnKeyListener(this.mOnKeyListener);
    }

    public OppoSecurityAlertDialog(Context context, int titleId, String contentStr, boolean always, boolean isCheck, int positiveStringId, int negativeStringId) {
        this.mView = null;
        this.mMessage = null;
        this.mRemember = null;
        this.mIsCheck = false;
        this.mOnKeyListener = /* anonymous class already generated */;
        this.mContext = context;
        this.mView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(201917514, null);
        this.mMessage = (TextView) this.mView.findViewById(201458845);
        this.mMessage.setText(contentStr);
        this.mRemember = (CheckBox) this.mView.findViewById(201458846);
        if (always) {
            this.mIsCheck = isCheck;
            this.mRemember.setChecked(this.mIsCheck);
            this.mRemember.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    OppoSecurityAlertDialog.this.mIsCheck = isChecked;
                    if (OppoSecurityAlertDialog.this.mOnSelectedListener != null) {
                        OppoSecurityAlertDialog.this.mOnSelectedListener.onSelected(OppoSecurityAlertDialog.this.mDialog, OppoSecurityAlertDialog.this.mIsCheck, 0);
                    }
                }
            });
        } else {
            this.mRemember.setVisibility(8);
        }
        this.mDialog = new Builder(this.mContext).setTitle(this.mContext.getText(titleId)).setView(this.mView).setPositiveButton(positiveStringId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (OppoSecurityAlertDialog.this.mOnSelectedListener != null) {
                    OppoSecurityAlertDialog.this.mOnSelectedListener.onSelected(dialog, OppoSecurityAlertDialog.this.mIsCheck, whichButton);
                }
            }
        }).setNegativeButton(negativeStringId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (OppoSecurityAlertDialog.this.mOnSelectedListener != null) {
                    OppoSecurityAlertDialog.this.mOnSelectedListener.onSelected(dialog, OppoSecurityAlertDialog.this.mIsCheck, whichButton);
                }
            }
        }).create();
        this.mDialog.setCancelable(false);
        this.mDialog.setOnKeyListener(this.mOnKeyListener);
    }

    public Dialog getSecurityAlertDialog() {
        return this.mDialog;
    }

    public void setOnSelectedListener(OnSelectedListener listener) {
        this.mOnSelectedListener = listener;
    }

    public void show() {
        if (!SystemProperties.get("persist.sys.personnal.security").equals("false")) {
            this.mDialog.show();
        }
    }

    public void setWindowType(int type) {
        this.mDialog.getWindow().setType(type);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mDialog.setOnDismissListener(listener);
    }

    public void hide() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }
}
