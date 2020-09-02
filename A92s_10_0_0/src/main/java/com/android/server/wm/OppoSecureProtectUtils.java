package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.IActivityController;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class OppoSecureProtectUtils {
    private static final int MONKEY_CONTROLLER = 1;
    private static final int NO_CONTROLLER = 0;
    private static final int SECURE_CONTROLLER = 2;
    private static final String TAG = "OppoSecureProtectUtils";
    private static int sControllerType = 0;
    private int mRequestCodeTemp;
    private ActivityRecord mResultToTemp;
    private String mResultWhoTemp;

    static final void setSecureControllerLocked(ActivityTaskManagerService ams, IActivityController controller) {
        setControllerLocked(ams, controller, controller == null ? 0 : 2);
    }

    static final void setMonkeyControllerLocked(ActivityTaskManagerService ams, IActivityController controller) {
        setControllerLocked(ams, controller, controller == null ? 0 : 1);
    }

    static final void setControllerLocked(ActivityTaskManagerService ams, IActivityController controller, int type) {
        Log.d(TAG, "Controller type = " + type);
        ams.mController = controller;
        sControllerType = type;
    }

    static final boolean isMonkeyController(ActivityTaskManagerService ams) {
        if (ams.mController != null && sControllerType == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setTempValue(ActivityRecord resultToTemp, String resultWhoTemp, int requestCodeTemp) {
        this.mResultToTemp = resultToTemp;
        this.mResultWhoTemp = resultWhoTemp;
        this.mRequestCodeTemp = requestCodeTemp;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord handleStartActivityLocked(ActivityTaskManagerService service, WindowProcessController caller, int launchedFromPid, int launchedFromUid, String launchedFromPackage, Intent intent, String resolvedType, ActivityInfo aInfo, Configuration configuration, ActivityRecord resultTo, String resultWho, int reqCode, boolean componentSpecified, boolean rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        ActivityRecord r = null;
        OppoBaseActivityTaskManagerService oBatms = (OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, service);
        if (this.mResultToTemp != null && ((intent.getFlags() & Integer.MIN_VALUE) != 0 || (intent.getFlags() & 1024) != 0 || (intent.getFlags() & 512) != 0)) {
            if (oBatms != null) {
                r = oBatms.createActivityRecord(service, caller, launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, aInfo, configuration, this.mResultToTemp, this.mResultWhoTemp, this.mRequestCodeTemp, componentSpecified, rootVoiceInteraction, supervisor, options, sourceRecord);
            }
            setTempValue(null, null, 0);
            return r;
        } else if (oBatms != null) {
            return oBatms.createActivityRecord(service, caller, launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, aInfo, configuration, resultTo, resultWho, reqCode, componentSpecified, rootVoiceInteraction, supervisor, options, sourceRecord);
        } else {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void handleFinishActivityLocked(ActivityStack stack) {
        ActivityRecord activityRecord = this.mResultToTemp;
        if (activityRecord != null) {
            stack.sendActivityResultLocked(-1, activityRecord, this.mResultWhoTemp, this.mRequestCodeTemp, 0, null);
        }
    }

    public boolean isSecureController(ActivityStackSupervisor stack) {
        return false;
    }

    static final void nofityMonkeyFinish(ActivityTaskManagerService atms) {
        if (atms != null && atms.mH != null) {
            atms.mH.post(new Runnable() {
                /* class com.android.server.wm.$$Lambda$OppoSecureProtectUtils$_yi1Fj5vkp_zvthE_wDR6MVk */

                public final void run() {
                    OppoSecureProtectUtils.lambda$nofityMonkeyFinish$0(ActivityTaskManagerService.this);
                }
            });
        }
    }

    static /* synthetic */ void lambda$nofityMonkeyFinish$0(ActivityTaskManagerService atms) {
        Log.i(TAG, "nofityMonkeyFinish!");
        atms.mContext.sendBroadcast(new Intent("android.intent.action.OPPO_SECURE_MONKEY_FINISH"));
    }

    public ActivityRecord getValue() {
        return this.mResultToTemp;
    }

    public boolean isNeedReplaceActivityRequest(ActivityRecord newResultTo, String newResultWho, int newRequestCode) {
        String str;
        return this.mResultToTemp != newResultTo || (str = this.mResultWhoTemp) == null || !str.equals(newResultWho) || this.mRequestCodeTemp != newRequestCode;
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
