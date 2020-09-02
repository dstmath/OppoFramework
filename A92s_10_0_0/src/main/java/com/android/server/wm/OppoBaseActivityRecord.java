package com.android.server.wm;

import android.text.TextUtils;

public abstract class OppoBaseActivityRecord extends ConfigurationContainer {
    public static final String COLOROS_RECENTS_PACKAGE_NAME = "com.coloros.recents.RecentsActivity";
    public static final String COLOROS_SPLIT_PACKAGE_NAME = "com.coloros.systemui.stackdivider.SplitChooserActivity";
    String callingPkg;
    public IColorActivityRecordEx mColorArEx = null;
    boolean mFromFreeform = false;
    boolean mIsFreeformFullscreen = false;
    boolean mRootLockActivity = false;
    boolean mShouldRelaunch;
    boolean notifyHotStart = true;

    /* access modifiers changed from: package-private */
    public void notifyWindowFreezing(boolean freezing, int stackId) {
    }

    /* access modifiers changed from: protected */
    public boolean isColorOSRecents(String className) {
        return !TextUtils.isEmpty(className) && (className.contains(COLOROS_RECENTS_PACKAGE_NAME) || className.contains(COLOROS_SPLIT_PACKAGE_NAME));
    }
}
