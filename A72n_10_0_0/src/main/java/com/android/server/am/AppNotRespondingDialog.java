package com.android.server.am;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.BidiFormatter;
import android.util.Slog;
import android.view.View;
import android.view.WindowManager;
import com.android.internal.logging.MetricsLogger;

public final class AppNotRespondingDialog extends BaseErrorDialog implements View.OnClickListener {
    public static final int ALREADY_SHOWING = -2;
    public static final int CANT_SHOW = -1;
    static final int FORCE_CLOSE = 1;
    private static final String TAG = "AppNotRespondingDialog";
    static final int WAIT = 2;
    static final int WAIT_AND_REPORT = 3;
    private final Handler mHandler = new Handler() {
        /* class com.android.server.am.AppNotRespondingDialog.AnonymousClass1 */

        public void handleMessage(Message msg) {
            Intent appErrorIntent = null;
            MetricsLogger.action(AppNotRespondingDialog.this.getContext(), 317, msg.what);
            int i = msg.what;
            if (i == 1) {
                AppNotRespondingDialog.this.mService.killAppAtUsersRequest(AppNotRespondingDialog.this.mProc, AppNotRespondingDialog.this);
            } else if (i == 2 || i == 3) {
                synchronized (AppNotRespondingDialog.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ProcessRecord app = AppNotRespondingDialog.this.mProc;
                        if (msg.what == 3) {
                            appErrorIntent = AppNotRespondingDialog.this.mService.mAppErrors.createAppErrorIntentLocked(app, System.currentTimeMillis(), null);
                        }
                        app.setNotResponding(false);
                        app.notRespondingReport = null;
                        if (app.anrDialog == AppNotRespondingDialog.this) {
                            app.anrDialog = null;
                        }
                        AppNotRespondingDialog.this.mService.mServices.scheduleServiceTimeoutLocked(app);
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
            if (appErrorIntent != null) {
                try {
                    AppNotRespondingDialog.this.getContext().startActivity(appErrorIntent);
                } catch (ActivityNotFoundException e) {
                    Slog.w(AppNotRespondingDialog.TAG, "bug report receiver dissappeared", e);
                }
            }
            AppNotRespondingDialog.this.dismiss();
        }
    };
    private final ProcessRecord mProc;
    private final ActivityManagerService mService;

    /* JADX WARNING: Removed duplicated region for block: B:16:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00d6  */
    public AppNotRespondingDialog(ActivityManagerService service, Context context, Data data) {
        super(context);
        CharSequence name1;
        int resid;
        String str;
        this.mService = service;
        this.mProc = data.proc;
        Resources res = context.getResources();
        setCancelable(false);
        if (data.aInfo != null) {
            name1 = data.aInfo.loadLabel(context.getPackageManager());
        } else {
            name1 = null;
        }
        CharSequence name2 = null;
        if (this.mProc.pkgList.size() == 1) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(this.mProc.info);
            name2 = applicationLabel;
            if (applicationLabel != null) {
                if (name1 != null) {
                    resid = 17039497;
                } else {
                    name1 = name2;
                    name2 = this.mProc.processName;
                    resid = 17039499;
                }
                BidiFormatter bidi = BidiFormatter.getInstance();
                if (name2 == null) {
                    str = res.getString(resid, bidi.unicodeWrap(name1.toString()), bidi.unicodeWrap(name2.toString()));
                } else {
                    str = res.getString(resid, bidi.unicodeWrap(name1.toString()));
                }
                setTitle(str);
                setButton(-3, res.getText(17040041), this.mHandler.obtainMessage(1));
                setButton(-2, res.getText(17041218), this.mHandler.obtainMessage(2));
                if (this.mProc.errorReportReceiver != null) {
                    setButton(-3, res.getText(17040923), this.mHandler.obtainMessage(3));
                }
                if (data.aboveSystem) {
                    getWindow().setType(2010);
                }
                WindowManager.LayoutParams attrs = getWindow().getAttributes();
                attrs.setTitle("Application Not Responding: " + this.mProc.info.processName);
                attrs.privateFlags = 272;
                getWindow().setAttributes(attrs);
            }
        }
        if (name1 != null) {
            name2 = this.mProc.processName;
            resid = 17039498;
        } else {
            name1 = this.mProc.processName;
            resid = 17039500;
        }
        BidiFormatter bidi2 = BidiFormatter.getInstance();
        if (name2 == null) {
        }
        setTitle(str);
        setButton(-3, res.getText(17040041), this.mHandler.obtainMessage(1));
        setButton(-2, res.getText(17041218), this.mHandler.obtainMessage(2));
        if (this.mProc.errorReportReceiver != null) {
        }
        if (data.aboveSystem) {
        }
        WindowManager.LayoutParams attrs2 = getWindow().getAttributes();
        attrs2.setTitle("Application Not Responding: " + this.mProc.info.processName);
        attrs2.privateFlags = 272;
        getWindow().setAttributes(attrs2);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == 16908704) {
            this.mHandler.obtainMessage(1).sendToTarget();
        } else if (id == 16908706) {
            this.mHandler.obtainMessage(3).sendToTarget();
        } else if (id == 16908708) {
            this.mHandler.obtainMessage(2).sendToTarget();
        }
    }

    public static class Data {
        final ApplicationInfo aInfo;
        final boolean aboveSystem;
        final ProcessRecord proc;

        public Data(ProcessRecord proc2, ApplicationInfo aInfo2, boolean aboveSystem2) {
            this.proc = proc2;
            this.aInfo = aInfo2;
            this.aboveSystem = aboveSystem2;
        }
    }
}
