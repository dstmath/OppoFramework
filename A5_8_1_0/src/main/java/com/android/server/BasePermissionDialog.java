package com.android.server;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class BasePermissionDialog extends AlertDialog {
    private final Handler mInfoHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                BasePermissionDialog.this.mState = false;
                BasePermissionDialog.this.setEnabled(true);
            }
        }
    };
    private boolean mState = true;

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mState) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void onStart() {
        super.onStart();
        setEnabled(false);
        this.mInfoHandler.sendMessage(this.mInfoHandler.obtainMessage(0));
    }

    public BasePermissionDialog(Context dialogCon) {
        super(dialogCon, 16974798);
        getWindow().setFlags(DumpState.DUMP_INTENT_FILTER_VERIFIERS, DumpState.DUMP_INTENT_FILTER_VERIFIERS);
        LayoutParams perm = getWindow().getAttributes();
        getWindow().setType(2003);
        perm.setTitle("Permission");
        setIconAttribute(16843605);
        getWindow().setAttributes(perm);
    }

    private void setEnabled(boolean setState) {
        Button btn = (Button) findViewById(16908313);
        if (btn != null) {
            btn.setEnabled(setState);
        }
        btn = (Button) findViewById(16908314);
        if (btn != null) {
            btn.setEnabled(setState);
        }
        btn = (Button) findViewById(16908315);
        if (btn != null) {
            btn.setEnabled(setState);
        }
    }
}
