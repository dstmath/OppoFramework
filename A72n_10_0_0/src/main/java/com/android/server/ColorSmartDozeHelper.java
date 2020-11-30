package com.android.server;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.util.concurrent.atomic.AtomicBoolean;

public class ColorSmartDozeHelper implements IColorSmartDozeHelper {
    public static final String ACTION_OPPO_SMARTDOZE_ALARM_EXEMPTION_END = "com.coloros.smartdoze.ALARM_EXEMPTION_END";
    public static final String ACTION_OPPO_SMARTDOZE_GPS_EXEMPTION_CHANGED = "com.coloros.smartdoze.GPS_EXEMPTION_CHANGE";
    public static final long ALARM_EXEPTION_TIME = 1500000;
    public static final long DEBUG_ALARM_EXEPTION_TIME = 600000;
    public static final long DEBUG_GPS_EXEPTION_TIME = 300000;
    public static final String DOZE_REASON = "smartdoze";
    public static final String EARLY_REASON = "s:early";
    public static final long GPS_EXEPTION_TIME = 900000;
    public static final String STATE = "state";
    public static final String STATE_GPS_EXEMPTION_END = "gps_exeption_end";
    public static final String STATE_GPS_EXEMPTION_START = "gps_exeption_start";
    public static final String TAG = "ColorSmartDozeHelper";
    private static volatile ColorSmartDozeHelper sColorSmartDozeHelper;
    private static boolean sOppoDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private AtomicBoolean mIsInSmartDozeEarlyTime = new AtomicBoolean(false);
    private AtomicBoolean mIsInSmartDozeMode = new AtomicBoolean(false);
    private AtomicBoolean mIsInSmartDozeMotionMaintance = new AtomicBoolean(false);
    private boolean mIsSupportSmartDoze = false;

    private ColorSmartDozeHelper() {
    }

    public static ColorSmartDozeHelper getInstance() {
        if (sColorSmartDozeHelper == null) {
            synchronized (ColorSmartDozeHelper.class) {
                if (sColorSmartDozeHelper == null) {
                    sColorSmartDozeHelper = new ColorSmartDozeHelper();
                }
            }
        }
        return sColorSmartDozeHelper;
    }

    public void init() {
        this.mIsSupportSmartDoze = true;
    }

    public void enterSmartDozeIfNeeded(String reason) {
        if (this.mIsSupportSmartDoze) {
            if (sOppoDebug) {
                Slog.d(TAG, "enterSmartDozeIfNeeded: " + reason);
            }
            if (DOZE_REASON.equals(reason)) {
                this.mIsInSmartDozeMode.set(true);
                this.mIsInSmartDozeEarlyTime.set(true);
            } else if (!EARLY_REASON.equals(reason) || !this.mIsInSmartDozeMode.get()) {
                this.mIsInSmartDozeMode.set(false);
                this.mIsInSmartDozeEarlyTime.set(false);
            } else {
                this.mIsInSmartDozeMode.set(true);
                this.mIsInSmartDozeEarlyTime.set(false);
            }
        }
    }

    public void exitSmartDoze() {
        if (this.mIsSupportSmartDoze) {
            if (sOppoDebug) {
                Slog.d(TAG, "exitSmartDoze: ");
            }
            this.mIsInSmartDozeMode.set(false);
            this.mIsInSmartDozeEarlyTime.set(false);
            this.mIsInSmartDozeMotionMaintance.set(false);
        }
    }

    public boolean isInSmartDozeMode() {
        if (!this.mIsSupportSmartDoze) {
            return false;
        }
        if (sOppoDebug) {
            Slog.d(TAG, "isInSmartDozeMode: " + this.mIsInSmartDozeMode.get());
        }
        return this.mIsInSmartDozeMode.get();
    }

    public boolean isInSmartDozeEearlyTime() {
        boolean isInSmartDozeEearlyTime = false;
        if (!this.mIsSupportSmartDoze) {
            return false;
        }
        if (this.mIsInSmartDozeMode.get() && this.mIsInSmartDozeEarlyTime.get()) {
            isInSmartDozeEearlyTime = true;
        }
        if (sOppoDebug) {
            Slog.d(TAG, "isInSmartDozeEearlyTime: " + isInSmartDozeEearlyTime);
        }
        return isInSmartDozeEearlyTime;
    }

    public boolean isInSmartDozeMotionMaintance() {
        boolean isInSmartDozeMotionMaintance = false;
        if (!this.mIsSupportSmartDoze) {
            return false;
        }
        if (this.mIsInSmartDozeMode.get() && this.mIsInSmartDozeMotionMaintance.get()) {
            isInSmartDozeMotionMaintance = true;
        }
        if (sOppoDebug) {
            Slog.d(TAG, "isInSmartDozeMotionMaintance: " + isInSmartDozeMotionMaintance);
        }
        return isInSmartDozeMotionMaintance;
    }

    public void moveGpsExemption(boolean state) {
        if (this.mIsSupportSmartDoze) {
            if (sOppoDebug) {
                Slog.d(TAG, "moveGpsExemption: state = " + state);
            }
            if (!state) {
                this.mIsInSmartDozeMotionMaintance.set(false);
            } else if (this.mIsInSmartDozeMode.get()) {
                this.mIsInSmartDozeMotionMaintance.set(true);
            }
        }
    }

    public void exitAlarmExemption() {
        if (this.mIsSupportSmartDoze) {
            if (sOppoDebug) {
                Slog.d(TAG, "exitAlarmExemption: ");
            }
            this.mIsInSmartDozeEarlyTime.set(false);
        }
    }

    public boolean isSupportSmartDoze() {
        return this.mIsSupportSmartDoze;
    }
}
