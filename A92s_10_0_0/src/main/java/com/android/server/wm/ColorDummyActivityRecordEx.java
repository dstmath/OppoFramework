package com.android.server.wm;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.view.IApplicationToken;

public class ColorDummyActivityRecordEx implements IColorActivityRecordEx {
    final ActivityRecord mActivityRecord;

    public ColorDummyActivityRecordEx(ActivityRecord ar) {
        this.mActivityRecord = ar;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public boolean forceRelaunchByNavBarHide() {
        return false;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public void setForceRelaunchByNavBarHide(boolean shouldRelaunch) {
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public boolean isUpdateFromNavbarHide(Configuration lastConfig, Configuration currentConfig, int height, String packageName) {
        return false;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public String getLaunchedFromPackage() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.launchedFromPackage;
        }
        return null;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public String getPackageName() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.packageName;
        }
        return null;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public int getLaunchedFromPid() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.launchedFromPid;
        }
        return -1;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public int getLaunchedFromUid() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.launchedFromUid;
        }
        return -1;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public ApplicationInfo getAppliationInfo() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.appInfo;
        }
        return null;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public Intent getIntent() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.intent;
        }
        return null;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public IApplicationToken.Stub getAppToken() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.appToken;
        }
        return null;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public String getshortComponentName() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.shortComponentName;
        }
        return null;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public boolean isActivityTypeHome() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.isActivityTypeHome();
        }
        return false;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public int getResultToUserId() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.resultTo.mUserId;
        }
        return 0;
    }

    @Override // com.android.server.wm.IColorActivityRecordEx
    public String getResultToPackageName() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.resultTo.packageName;
        }
        return null;
    }
}
