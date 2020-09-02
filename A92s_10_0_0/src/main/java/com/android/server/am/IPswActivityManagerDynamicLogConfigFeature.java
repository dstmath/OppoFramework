package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface IPswActivityManagerDynamicLogConfigFeature extends IOppoCommonFeature {
    public static final IPswActivityManagerDynamicLogConfigFeature DEFAULT = new IPswActivityManagerDynamicLogConfigFeature() {
        /* class com.android.server.am.IPswActivityManagerDynamicLogConfigFeature.AnonymousClass1 */
    };
    public static final String NAME = "IPswActivityManagerDynamicLogConfigFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswActivityManagerDynamicLogConfigFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean doDump(ActivityManagerService ams, String cmd, FileDescriptor fd, PrintWriter pw, String[] args, int opti) {
        Log.d(NAME, "default doDump");
        return false;
    }
}
