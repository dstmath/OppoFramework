package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.Handler;
import com.android.server.AlarmManagerService;

public interface IColorGoogleAlarmRestrict extends IOppoCommonFeature {
    public static final IColorGoogleAlarmRestrict DEFAULT = new IColorGoogleAlarmRestrict() {
        /* class com.android.server.IColorGoogleAlarmRestrict.AnonymousClass1 */
    };
    public static final String NAME = "IColorGoogleAlarmRestrict";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorGoogleAlarmRestrict;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void initArgs(Context context, Handler handler, AlarmManagerService ams) {
    }

    default void updateGoogleAlarmTypeAndTag(AlarmManagerService.Alarm a) {
    }
}
