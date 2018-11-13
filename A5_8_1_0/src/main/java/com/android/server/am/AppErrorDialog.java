package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.BidiFormatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;

final class AppErrorDialog extends BaseErrorDialog implements OnClickListener {
    static int ALREADY_SHOWING = -3;
    static int BACKGROUND_USER = -2;
    static final int CANCEL = 7;
    static int CANT_SHOW = -1;
    static final long DISMISS_TIMEOUT = 300000;
    static final int FORCE_QUIT = 1;
    static final int FORCE_QUIT_AND_REPORT = 2;
    static final int MUTE = 5;
    static final int RESTART = 3;
    static final int TIMEOUT = 6;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AppErrorDialog.this.setResult(msg.what);
            AppErrorDialog.this.dismiss();
        }
    };
    private final boolean mIsRestartable;
    private CharSequence mName;
    private final ProcessRecord mProc;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                AppErrorDialog.this.cancel();
            }
        }
    };
    private final boolean mRepeating;
    private final AppErrorResult mResult;
    private final ActivityManagerService mService;

    static class Data {
        boolean isRestartableForService;
        ProcessRecord proc;
        boolean repeating;
        AppErrorResult result;
        TaskRecord task;

        Data() {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x00e1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AppErrorDialog(Context context, ActivityManagerService service, Data data) {
        boolean z;
        int i;
        LayoutParams attrs;
        super(context);
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = data.proc;
        this.mResult = data.result;
        this.mRepeating = data.repeating;
        if (data.task == null) {
            z = data.isRestartableForService;
        } else {
            z = true;
        }
        this.mIsRestartable = z;
        BidiFormatter bidi = BidiFormatter.getInstance();
        if (this.mProc.pkgList.size() == 1) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(this.mProc.info);
            this.mName = applicationLabel;
            if (applicationLabel != null) {
                if (this.mRepeating) {
                    i = 17039461;
                } else {
                    i = 17039460;
                }
                setTitle(res.getString(i, new Object[]{bidi.unicodeWrap(this.mName.toString()), bidi.unicodeWrap(this.mProc.info.processName)}));
                setCancelable(true);
                setCancelMessage(this.mHandler.obtainMessage(7));
                setButton(-1, res.getText(17039465), this.mHandler.obtainMessage(1));
                if (this.mProc.errorReportReceiver != null) {
                    setButton(-2, res.getText(17040766), this.mHandler.obtainMessage(2));
                }
                attrs = getWindow().getAttributes();
                attrs.setTitle("Application Error: " + this.mProc.info.processName);
                attrs.privateFlags |= 272;
                getWindow().setAttributes(attrs);
                if (this.mProc.persistent) {
                    getWindow().setType(2010);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 300000);
            }
        }
        this.mName = this.mProc.processName;
        if (this.mRepeating) {
            i = 17039467;
        } else {
            i = 17039466;
        }
        setTitle(res.getString(i, new Object[]{bidi.unicodeWrap(this.mName.toString())}));
        setCancelable(true);
        setCancelMessage(this.mHandler.obtainMessage(7));
        setButton(-1, res.getText(17039465), this.mHandler.obtainMessage(1));
        if (this.mProc.errorReportReceiver != null) {
        }
        attrs = getWindow().getAttributes();
        attrs.setTitle("Application Error: " + this.mProc.info.processName);
        attrs.privateFlags |= 272;
        getWindow().setAttributes(attrs);
        if (this.mProc.persistent) {
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 300000);
    }

    public void onStart() {
        super.onStart();
        getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    protected void onStop() {
        super.onStop();
        getContext().unregisterReceiver(this.mReceiver);
    }

    public void dismiss() {
        if (!this.mResult.mHasResult) {
            setResult(1);
        }
        super.dismiss();
    }

    private void setResult(int result) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mProc != null && this.mProc.crashDialog == this) {
                    this.mProc.crashDialog = null;
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mResult.set(result);
        this.mHandler.removeMessages(6);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16908690:
                this.mHandler.obtainMessage(1).sendToTarget();
                return;
            case 16908691:
                this.mHandler.obtainMessage(5).sendToTarget();
                return;
            case 16908692:
                this.mHandler.obtainMessage(2).sendToTarget();
                return;
            case 16908693:
                this.mHandler.obtainMessage(3).sendToTarget();
                return;
            default:
                return;
        }
    }
}
