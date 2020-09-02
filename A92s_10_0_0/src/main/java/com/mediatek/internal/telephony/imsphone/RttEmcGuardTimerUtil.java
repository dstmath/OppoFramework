package com.mediatek.internal.telephony.imsphone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.telephony.Rlog;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;

public class RttEmcGuardTimerUtil {
    private static final String INTENT_RTT_EMC_GUARD_TIMER_180 = "com.mediatek.internal.telephony.imsphone.rtt_emc_guard_timer_180";
    private static final String TAG = "RttEmcGuardTimerUtil";
    private AlarmManager mAlarmManager;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mDuringRttGuardDuration = false;
    private boolean mIsRttEmcGuardTimerSupported = false;
    /* access modifiers changed from: private */
    public PendingIntent mRttEmcIntent = null;
    private BroadcastReceiver mRttReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.imsphone.RttEmcGuardTimerUtil.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RttEmcGuardTimerUtil.INTENT_RTT_EMC_GUARD_TIMER_180)) {
                Rlog.d(RttEmcGuardTimerUtil.TAG, "onReceive : mRttReceiver rtt guard timer 180");
                RttEmcGuardTimerUtil.this.stopRttEmcGuardTimer();
                PendingIntent unused = RttEmcGuardTimerUtil.this.mRttEmcIntent = null;
                boolean unused2 = RttEmcGuardTimerUtil.this.mDuringRttGuardDuration = false;
            }
        }
    };

    public RttEmcGuardTimerUtil(Context context) {
        this.mContext = context;
    }

    public void initRttEmcGuardTimer() {
        Rlog.d(TAG, "initRttEmcGuardTimer");
        Context context = this.mContext;
        if (context == null) {
            Rlog.d(TAG, "initRttEmcGuardTimer mContext == null");
            return;
        }
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        registerRttReceiver();
    }

    public void disposeRttEmcGuardTimer() {
        Rlog.d(TAG, "disposeRttEmcGuardTimer");
        unregisterRttReceiver();
    }

    private void registerRttReceiver() {
        Rlog.d(TAG, "registerRttReceiver");
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(INTENT_RTT_EMC_GUARD_TIMER_180);
        this.mContext.registerReceiver(this.mRttReceiver, intentfilter);
    }

    private void unregisterRttReceiver() {
        Rlog.d(TAG, "unregisterRttReceiver");
        this.mContext.unregisterReceiver(this.mRttReceiver);
    }

    public void stopRttEmcGuardTimer() {
        Rlog.d(TAG, "stopRttEmcGuardTimer");
        if (this.mRttEmcIntent != null) {
            Rlog.d(TAG, "stopRttEmcGuardTimer, cancel timer");
            this.mAlarmManager.cancel(this.mRttEmcIntent);
            this.mRttEmcIntent = null;
            this.mDuringRttGuardDuration = false;
        }
    }

    public void checkIncomingCallInRttEmcGuardTime(ImsPhoneConnection conn) {
        Rlog.d(TAG, "checkIncomingCallInRttEmcGuardTime: " + conn);
        if (conn == null) {
            Rlog.e(TAG, "conn == null, checkIncomingCallInRttEmcGuardTime return");
        } else if (this.mDuringRttGuardDuration) {
            ((MtkImsPhoneConnection) conn).setIncomingCallDuringRttEmcGuard(true);
        } else {
            ((MtkImsPhoneConnection) conn).setIncomingCallDuringRttEmcGuard(false);
        }
    }

    public void startRttEmcGuardTimer() {
        if (!this.mIsRttEmcGuardTimerSupported) {
            Rlog.d(TAG, "startRttEmcGuardTimer: Current carrier doesn't support RTT EMC guard timer, just return");
        } else if (this.mContext == null) {
            Rlog.e(TAG, "startRttEmcGuardTimer mContext == null");
        } else {
            stopRttEmcGuardTimer();
            this.mRttEmcIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_RTT_EMC_GUARD_TIMER_180), 134217728);
            Rlog.d(TAG, "startRttEmcGuardTimer: delay=" + 180000);
            this.mDuringRttGuardDuration = true;
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) 180000), this.mRttEmcIntent);
        }
    }

    public void setRttEmcGuardTimerSupported(boolean isSupported) {
        this.mIsRttEmcGuardTimerSupported = isSupported;
    }
}
