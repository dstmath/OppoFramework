package com.android.server.power;

import android.content.Context;
import android.util.Slog;
import com.android.server.biometrics.face.health.HealthState;
import com.color.util.ColorTypeCastingHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class OppoPowerFuncHelper {
    private static final long MIN_CNT_USER_ACTIVITY = 2;
    private static final long MIN_SCREEN_ON_DURATION = 20000;
    private String TAG;
    private OppoBasePowerManagerService mBasePower;
    protected AtomicInteger mCntUserActivity = new AtomicInteger(0);
    private Context mContext;
    protected AtomicBoolean mKeyguardLockEverUnlock = new AtomicBoolean(false);
    private Object mLock;
    private PowerManagerService mPowerMS;
    protected int mSleepReason;
    protected String mWakeupReason;

    public OppoPowerFuncHelper(Context context, Object lock, PowerManagerService pms, String tag) {
        this.mContext = context;
        this.mLock = lock;
        this.mPowerMS = pms;
        this.mBasePower = (OppoBasePowerManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePowerManagerService.class, pms);
        this.TAG = tag;
    }

    /* access modifiers changed from: protected */
    public boolean dumpShortScreenOn(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length < 1 || !"shortScreenOn".equals(args[0])) {
            return false;
        }
        pw.println("short:" + getShortScreenOnStatusInternal());
        pw.println("wakeupReason:" + this.mWakeupReason);
        pw.println("sleepReason:" + sleepReasonToString());
        return true;
    }

    /* access modifiers changed from: protected */
    public String getShortScreenOnStatusInternal() {
        long screenOnDuration = 0;
        OppoBasePowerManagerService oppoBasePowerManagerService = this.mBasePower;
        if (oppoBasePowerManagerService != null) {
            screenOnDuration = oppoBasePowerManagerService.mColorPowerMSInner.getLastSleepTime() - this.mBasePower.mColorPowerMSInner.getLastWakeTime();
        }
        if (this.mKeyguardLockEverUnlock.get()) {
            return "keyguardUnlock";
        }
        if (((long) this.mCntUserActivity.get()) >= 2) {
            return HealthState.USERACTIVITY;
        }
        if (screenOnDuration <= 0 || screenOnDuration > MIN_SCREEN_ON_DURATION) {
            return "longSreenOn";
        }
        return "shortSreenOn";
    }

    /* access modifiers changed from: protected */
    public String sleepReasonToString() {
        switch (this.mSleepReason) {
            case 0:
                return "app";
            case 1:
                return "device_admin";
            case 2:
                return "timeout";
            case 3:
                return "lid_switch";
            case 4:
                return "power_key";
            case 5:
                return "hdmi";
            case 6:
                return "sleep_key";
            case 7:
            case 8:
            default:
                return "unkown";
            case 9:
                return "proximity";
            case 10:
                Slog.i(this.TAG, "Going to sleep due to fingerprint");
                return "fingerprint";
        }
    }
}
