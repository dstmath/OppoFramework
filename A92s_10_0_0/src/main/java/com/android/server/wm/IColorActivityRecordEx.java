package com.android.server.wm;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.view.IApplicationToken;

public interface IColorActivityRecordEx {
    boolean forceRelaunchByNavBarHide();

    IApplicationToken.Stub getAppToken();

    ApplicationInfo getAppliationInfo();

    Intent getIntent();

    String getLaunchedFromPackage();

    int getLaunchedFromPid();

    int getLaunchedFromUid();

    String getPackageName();

    String getResultToPackageName();

    int getResultToUserId();

    String getshortComponentName();

    boolean isActivityTypeHome();

    boolean isUpdateFromNavbarHide(Configuration configuration, Configuration configuration2, int i, String str);

    void setForceRelaunchByNavBarHide(boolean z);
}
