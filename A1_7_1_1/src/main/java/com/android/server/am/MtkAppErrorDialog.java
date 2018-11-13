package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.text.BidiFormatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.server.am.AppErrorDialog.Data;
import com.android.server.input.InputManagerService;

public class MtkAppErrorDialog extends BaseErrorDialog implements OnClickListener {
    public static final int CANCEL = 7;
    static final long DISMISS_TIMEOUT = 300000;
    public static final int FORCE_QUIT = 1;
    public static final int FORCE_QUIT_AND_REPORT = 2;
    public static final int MUTE = 5;
    public static final int RESTART = 3;
    public static final int TIMEOUT = 6;
    private final boolean mForeground;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int result = msg.what;
            synchronized (MtkAppErrorDialog.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (MtkAppErrorDialog.this.mProc != null && MtkAppErrorDialog.this.mProc.crashDialog == MtkAppErrorDialog.this) {
                        MtkAppErrorDialog.this.mProc.crashDialog = null;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            MtkAppErrorDialog.this.mResult.set(result);
            removeMessages(6);
            MtkAppErrorDialog.this.dismiss();
        }
    };
    private CharSequence mName;
    private final ProcessRecord mProc;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                MtkAppErrorDialog.this.cancel();
            }
        }
    };
    private final boolean mRepeating;
    private final AppErrorResult mResult;
    private final ActivityManagerService mService;

    /* JADX WARNING: Removed duplicated region for block: B:13:0x00b8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MtkAppErrorDialog(Context context, ActivityManagerService service, Data data) {
        boolean z;
        int i;
        LayoutParams attrs;
        super(context);
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = data.proc;
        this.mResult = data.result;
        this.mRepeating = data.repeating;
        if (data.task != null) {
            z = true;
        } else {
            z = false;
        }
        this.mForeground = z;
        BidiFormatter bidi = BidiFormatter.getInstance();
        if (this.mProc.pkgList.size() == 1) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(this.mProc.info);
            this.mName = applicationLabel;
            if (applicationLabel != null) {
                if (this.mRepeating) {
                    i = 17040283;
                } else {
                    i = 17040281;
                }
                setTitle(res.getString(i, new Object[]{bidi.unicodeWrap(this.mName.toString()), bidi.unicodeWrap(this.mProc.info.processName)}));
                setCancelable(true);
                setCancelMessage(this.mHandler.obtainMessage(7));
                attrs = getWindow().getAttributes();
                attrs.setTitle("Application Error: " + this.mProc.info.processName);
                attrs.privateFlags |= InputManagerService.BTN_MOUSE;
                getWindow().setAttributes(attrs);
                if (this.mProc.persistent) {
                    getWindow().setType(2010);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 300000);
            }
        }
        this.mName = this.mProc.processName;
        if (this.mRepeating) {
            i = 17040284;
        } else {
            i = 17040282;
        }
        setTitle(res.getString(i, new Object[]{bidi.unicodeWrap(this.mName.toString())}));
        setCancelable(true);
        setCancelMessage(this.mHandler.obtainMessage(7));
        attrs = getWindow().getAttributes();
        attrs.setTitle("Application Error: " + this.mProc.info.processName);
        attrs.privateFlags |= InputManagerService.BTN_MOUSE;
        getWindow().setAttributes(attrs);
        if (this.mProc.persistent) {
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 300000);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout((FrameLayout) findViewById(16908331));
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
            this.mResult.set(1);
        }
        super.dismiss();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16909114:
                clickButtonForResult(1);
                return;
            case 16909116:
                clickButtonForResult(2);
                return;
            case 16909117:
                clickButtonForResult(3);
                return;
            case 16909118:
                clickButtonForResult(5);
                return;
            default:
                return;
        }
    }

    public void initLayout(FrameLayout frame) {
        int i;
        int i2 = 8;
        Context context = getContext();
        LayoutInflater.from(context).inflate(17367092, frame, true);
        boolean hasRestart = !this.mRepeating ? this.mForeground : false;
        boolean hasReceiver = this.mProc.errorReportReceiver != null;
        TextView restart = (TextView) findViewById(16909117);
        restart.setOnClickListener(this);
        if (hasRestart) {
            i = 0;
        } else {
            i = 8;
        }
        restart.setVisibility(i);
        TextView report = (TextView) findViewById(16909116);
        report.setOnClickListener(this);
        if (hasReceiver) {
            i = 0;
        } else {
            i = 8;
        }
        report.setVisibility(i);
        TextView close = (TextView) findViewById(16909114);
        if (hasRestart) {
            i = 8;
        } else {
            i = 0;
        }
        close.setVisibility(i);
        close.setOnClickListener(this);
        boolean showMute = (ActivityManagerService.IS_USER_BUILD || Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) == 0) ? false : true;
        TextView mute = (TextView) findViewById(16909118);
        mute.setOnClickListener(this);
        if (showMute) {
            i2 = 0;
        }
        mute.setVisibility(i2);
        findViewById(16909100).setVisibility(0);
    }

    public void clickButtonForResult(int result) {
        this.mHandler.obtainMessage(result).sendToTarget();
    }
}
