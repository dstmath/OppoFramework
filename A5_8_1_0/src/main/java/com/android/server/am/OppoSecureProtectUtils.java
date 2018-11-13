package com.android.server.am;

import android.app.ActivityOptions;
import android.app.IActivityController;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;

class OppoSecureProtectUtils {
    private static final int MONKEY_CONTROLLER = 1;
    private static final int NO_CONTROLLER = 0;
    private static final int SECURE_CONTROLLER = 2;
    private static final String TAG = "OppoSecureProtectUtils";
    private static int sControllerType = 0;
    private int mRequestCodeTemp;
    private ActivityRecord mResultToTemp;
    private String mResultWhoTemp;

    OppoSecureProtectUtils() {
    }

    static final void setSecureControllerLocked(ActivityManagerService ams, IActivityController controller) {
        setControllerLocked(ams, controller, controller == null ? 0 : 2);
    }

    static final void setMonkeyControllerLocked(ActivityManagerService ams, IActivityController controller) {
        setControllerLocked(ams, controller, controller == null ? 0 : 1);
    }

    static final void setControllerLocked(ActivityManagerService ams, IActivityController controller, int type) {
        Log.d(TAG, "Controller type = " + type);
        ams.mController = controller;
        sControllerType = type;
    }

    static final boolean isMonkeyController(ActivityManagerService ams) {
        boolean z = true;
        if (ams.mController == null) {
            return false;
        }
        if (sControllerType != 1) {
            z = false;
        }
        return z;
    }

    void setTempValue(ActivityRecord resultToTemp, String resultWhoTemp, int requestCodeTemp) {
        this.mResultToTemp = resultToTemp;
        this.mResultWhoTemp = resultWhoTemp;
        this.mRequestCodeTemp = requestCodeTemp;
    }

    ActivityRecord handleStartActivityLocked(ActivityManagerService service, ProcessRecord caller, int launchedFromPid, int launchedFromUid, String launchedFromPackage, Intent intent, String resolvedType, ActivityInfo aInfo, Configuration configuration, ActivityRecord resultTo, String resultWho, int reqCode, boolean componentSpecified, boolean rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        if (this.mResultToTemp == null || ((intent.getFlags() & Integer.MIN_VALUE) == 0 && (intent.getFlags() & 1024) == 0 && (intent.getFlags() & 512) == 0)) {
            return new ActivityRecord(service, caller, launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, aInfo, configuration, resultTo, resultWho, reqCode, componentSpecified, rootVoiceInteraction, supervisor, options, sourceRecord);
        }
        ActivityRecord r = new ActivityRecord(service, caller, launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, aInfo, configuration, this.mResultToTemp, this.mResultWhoTemp, this.mRequestCodeTemp, componentSpecified, rootVoiceInteraction, supervisor, options, sourceRecord);
        setTempValue(null, null, 0);
        return r;
    }

    void handleFinishActivityLocked(ActivityStack stack) {
        if (this.mResultToTemp != null) {
            stack.sendActivityResultLocked(-1, this.mResultToTemp, this.mResultWhoTemp, this.mRequestCodeTemp, 0, null);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isSecureController(ActivityStackSupervisor stack) {
        boolean z = false;
        if (stack.mWindowManager == null || !stack.mWindowManager.inKeyguardRestrictedInputMode() || stack.mService.mController == null) {
            return false;
        }
        if (sControllerType == 2) {
            z = true;
        }
        return z;
    }

    static final void nofityMonkeyFinish(ActivityManagerService ams) {
        Log.i(TAG, "nofityMonkeyFinish!");
        ams.mContext.sendBroadcast(new Intent("android.intent.action.OPPO_SECURE_MONKEY_FINISH"));
    }

    public ActivityRecord getValue() {
        return this.mResultToTemp;
    }

    public boolean isNeedReplaceActivityRequest(ActivityRecord newResultTo, String newResultWho, int newRequestCode) {
        if (this.mResultToTemp == newResultTo && this.mResultWhoTemp != null && (this.mResultWhoTemp.equals(newResultWho) ^ 1) == 0 && this.mRequestCodeTemp == newRequestCode) {
            return false;
        }
        return true;
    }

    public Intent secureIntent(int uid, ActivityRecord sourceRecord, Intent intent) {
        if (uid != 1000 || intent == null || sourceRecord == null || sourceRecord.intent == null || sourceRecord.intent.getComponent() == null || sourceRecord.intent.getComponent().getClassName() == null || !sourceRecord.intent.getComponent().getClassName().contains("com.coloros.safecenter.privacy.view.password")) {
            return null;
        }
        Intent newIntent = new Intent(intent);
        newIntent.addFlags(Integer.MIN_VALUE);
        return newIntent;
    }
}
