package com.android.server;

import android.app.ActivityManagerInternal;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseBooleanArray;
import java.io.IOException;
import org.xmlpull.v1.XmlSerializer;

public interface IColorGoogleDozeRestrict extends IOppoCommonFeature {
    public static final IColorGoogleDozeRestrict DEFAULT = new IColorGoogleDozeRestrict() {
        /* class com.android.server.IColorGoogleDozeRestrict.AnonymousClass1 */
    };
    public static final String NAME = "IColorGoogleDozeRestrict";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorGoogleDozeRestrict;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void initArgs(Context context, Handler handler, DeviceIdleController controller, IColorGoogleDozeRestrictInner inner) {
    }

    default void updateWhitelistApps(ArrayMap<String, Integer> arrayMap, boolean isSystem, boolean isExceptIdle) {
    }

    default void reportWhitelistForAms(ActivityManagerInternal localAms, SparseBooleanArray allAppIds, SparseBooleanArray allExceptIdleAppIds) {
    }

    default void restoreConfigFile(ArrayMap<String, Integer> arrayMap, XmlSerializer out) throws IOException {
    }

    default boolean interceptWhitelistOperation(ApplicationInfo ai, String name, boolean isSystem, boolean isExceptIdle, boolean add) {
        return false;
    }

    default void interceptWhitelistReset(boolean isExceptIdle, ArraySet<String> arraySet) {
    }

    default void oppoUpdateWhitelist() {
    }
}
