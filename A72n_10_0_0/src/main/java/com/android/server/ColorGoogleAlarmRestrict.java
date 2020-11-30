package com.android.server;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import com.android.server.OppoBaseAlarmManagerService;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;

public class ColorGoogleAlarmRestrict implements IColorGoogleAlarmRestrict, IColorGoogleRestrictCallback {
    private static final boolean DEBUG_PANIC = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG_PANIC);
    private static final String TAG = "ColorGoogleAlarmRestrict";
    AlarmManagerService mAlarmMgS = null;
    ColorGoogleRestrictionHelper mColorGoogleRestrictionHelper = null;
    Handler mHandler = null;

    public void initArgs(Context context, Handler handler, AlarmManagerService ams) {
        this.mColorGoogleRestrictionHelper = ColorGoogleRestrictionHelper.getInstance(context);
        this.mHandler = handler;
        this.mAlarmMgS = ams;
        this.mColorGoogleRestrictionHelper.addCallback(this);
    }

    public void updateGoogleAlarmTypeAndTag(AlarmManagerService.Alarm a) {
        OppoBaseAlarmManagerService.BaseAlarm baseAlarm = typeCasting(a);
        if (baseAlarm != null) {
            int type = baseAlarm.type;
            if (!this.mColorGoogleRestrictionHelper.getGoogleRestrictList().contains(a.packageName) || !this.mColorGoogleRestrictionHelper.isGoogleRestrct()) {
                if (baseAlarm.type != baseAlarm.expectedType) {
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "restore google pkg " + a.packageName + ",from type " + type + " to " + baseAlarm.expectedType);
                    }
                    baseAlarm.type = baseAlarm.expectedType;
                    baseAlarm.statsTag = AlarmManagerService.Alarm.makeTag(baseAlarm.operation, baseAlarm.listenerTag, baseAlarm.type);
                }
            } else if (a.alarmClock != null) {
            } else {
                if (type == 2) {
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "updateGoogleAlarmTypeAndTag, change type from 2 to 3");
                    }
                    baseAlarm.type = 3;
                    baseAlarm.statsTag = AlarmManagerService.Alarm.makeTag(baseAlarm.operation, baseAlarm.listenerTag, baseAlarm.type);
                } else if (type == 0) {
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "updateGoogleAlarmTypeAndTag, change type from 0 to 1");
                    }
                    baseAlarm.type = 1;
                    baseAlarm.statsTag = AlarmManagerService.Alarm.makeTag(baseAlarm.operation, baseAlarm.listenerTag, baseAlarm.type);
                }
            }
        }
    }

    public void restrictChange() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.ColorGoogleAlarmRestrict.AnonymousClass1 */

            public void run() {
                ColorGoogleAlarmRestrict.this.mAlarmMgS.rebatchAllAlarms();
            }
        });
    }

    public void restrictListChange() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.ColorGoogleAlarmRestrict.AnonymousClass2 */

            public void run() {
                ColorGoogleAlarmRestrict.this.mAlarmMgS.rebatchAllAlarms();
            }
        });
    }

    private static OppoBaseAlarmManagerService.BaseAlarm typeCasting(AlarmManagerService.Alarm alarm) {
        if (alarm != null) {
            return (OppoBaseAlarmManagerService.BaseAlarm) ColorTypeCastingHelper.typeCasting(OppoBaseAlarmManagerService.BaseAlarm.class, alarm);
        }
        return null;
    }
}
