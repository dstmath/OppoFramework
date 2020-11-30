package com.android.server.am;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.internal.annotations.GuardedBy;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.pm.DumpState;

/* access modifiers changed from: package-private */
public class UserSwitchingDialog extends AlertDialog implements ViewTreeObserver.OnWindowShownListener {
    private static final int MSG_START_USER = 1;
    private static final String TAG = "ActivityManagerUserSwitchingDialog";
    private static final int WINDOW_SHOWN_TIMEOUT_MS = 3000;
    protected final Context mContext;
    private final Handler mHandler = new Handler() {
        /* class com.android.server.am.UserSwitchingDialog.AnonymousClass1 */

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                UserSwitchingDialog.this.startUser();
            }
        }
    };
    protected final UserInfo mNewUser;
    protected final UserInfo mOldUser;
    private final ActivityManagerService mService;
    @GuardedBy({"this"})
    private boolean mStartedUser;
    private final String mSwitchingFromSystemUserMessage;
    private final String mSwitchingToSystemUserMessage;
    private final int mUserId;

    public UserSwitchingDialog(ActivityManagerService service, Context context, UserInfo oldUser, UserInfo newUser, boolean aboveSystem, String switchingFromSystemUserMessage, String switchingToSystemUserMessage) {
        super(context);
        this.mContext = context;
        this.mService = service;
        this.mUserId = newUser.id;
        this.mOldUser = oldUser;
        this.mNewUser = newUser;
        this.mSwitchingFromSystemUserMessage = switchingFromSystemUserMessage;
        this.mSwitchingToSystemUserMessage = switchingToSystemUserMessage;
        inflateContent();
        if (aboveSystem) {
            getWindow().setType(2010);
        }
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.privateFlags = 272;
        if (this.mUserId == 888) {
            attrs.privateFlags |= DumpState.DUMP_FROZEN;
        }
        getWindow().setAttributes(attrs);
    }

    /* access modifiers changed from: package-private */
    public void inflateContent() {
        String viewMessage;
        setCancelable(false);
        Resources res = getContext().getResources();
        View view = LayoutInflater.from(getContext()).inflate(17367333, (ViewGroup) null);
        if (UserManager.isSplitSystemUser() && this.mNewUser.id == 0) {
            viewMessage = res.getString(17041183, this.mOldUser.name);
        } else if (!UserManager.isDeviceInDemoMode(this.mContext)) {
            if (((UserInfo) this.mOldUser).id == 0) {
                viewMessage = this.mSwitchingFromSystemUserMessage;
            } else if (this.mNewUser.id == 0) {
                viewMessage = this.mSwitchingToSystemUserMessage;
            } else {
                viewMessage = null;
            }
            if (viewMessage == null) {
                viewMessage = res.getString(201590236);
            }
        } else if (this.mOldUser.isDemo()) {
            viewMessage = res.getString(17039854);
        } else {
            viewMessage = res.getString(17039855);
        }
        ((TextView) view.findViewById(16908299)).setText(viewMessage);
        setView(view);
    }

    public void show() {
        super.show();
        View decorView = getWindow().getDecorView();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnWindowShownListener(this);
        }
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1), BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
    }

    public void onWindowShown() {
        startUser();
    }

    /* access modifiers changed from: package-private */
    public void startUser() {
        synchronized (this) {
            if (!this.mStartedUser) {
                this.mService.mUserController.startUserInForeground(this.mUserId);
                dismiss();
                this.mStartedUser = true;
                View decorView = getWindow().getDecorView();
                if (decorView != null) {
                    decorView.getViewTreeObserver().removeOnWindowShownListener(this);
                }
                this.mHandler.removeMessages(1);
            }
        }
    }
}
