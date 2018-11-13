package com.android.server.secrecy.policy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.server.secrecy.policy.util.LogUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ResetCountDownTimer {
    private static int DEFAULT_COUNTDOWN_TIME_FOR_ID = 43200;
    private static int DEFAULT_COUNTDOWN_TIME_FOR_MAC = 1440;
    private static String TAG = "SecrecyService.ResetCountDownTimer";
    private final String ACTION_ALARM_INTENT = ("android.secrecy.policyfactor.invalid.expire." + getClass().getSimpleName());
    private Intent mAlarmIntent;
    private AlarmManager mAlarmManager;
    private AlarmReceiver mAlarmReceiver;
    private final Context mContext;
    private int mCountDownTimeForId;
    private int mCountDownTimeForMac;
    private PendingIntent mPendingIntent;
    private final PolicyManager mPolicyManager;

    public class AlarmReceiver extends BroadcastReceiver {
        private final ResetCountDownTimer mResetCountDownTimer;

        public AlarmReceiver(ResetCountDownTimer resetCountDownTimer) {
            this.mResetCountDownTimer = resetCountDownTimer;
        }

        public void onReceive(Context context, Intent intent) {
            LogUtil.d(ResetCountDownTimer.TAG, this + " PolicyFactor onReceive, action = " + intent.getAction());
            this.mResetCountDownTimer.getPolicyManager().onCountDownTimerExpired();
        }
    }

    public ResetCountDownTimer(Context context, PolicyManager policyManager) {
        this.mContext = context;
        this.mPolicyManager = policyManager;
        initAlarm();
    }

    private void initAlarm() {
        this.mAlarmReceiver = new AlarmReceiver(this);
        this.mContext.registerReceiver(this.mAlarmReceiver, new IntentFilter(this.ACTION_ALARM_INTENT));
        this.mAlarmIntent = new Intent(this.ACTION_ALARM_INTENT);
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, this.mAlarmIntent, 0);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        updateCountDownTime(DEFAULT_COUNTDOWN_TIME_FOR_MAC, DEFAULT_COUNTDOWN_TIME_FOR_ID);
    }

    public void updateCountDownTime(int countDownTimeForMac, int countDownTimeforId) {
        this.mCountDownTimeForMac = countDownTimeForMac;
        this.mCountDownTimeForId = countDownTimeforId;
        LogUtil.d(TAG, "updateCountDownTime, mCountDownTimeForMac = " + this.mCountDownTimeForMac);
        LogUtil.d(TAG, "updateCountDownTime, mCountDownTimeForId = " + this.mCountDownTimeForId, new Throwable("Kevin_DEBUG"));
    }

    public void startCountDown(String unlockType) {
        long countDownTime = (long) getCountDownTimer(unlockType);
        this.mAlarmManager.cancel(this.mPendingIntent);
        this.mAlarmManager.set(0, System.currentTimeMillis() + (1000 * countDownTime), this.mPendingIntent);
        LogUtil.d(TAG, "startCountDown, countDownTime = " + countDownTime);
    }

    public int getCountDownTimer(String unlockType) {
        if (DecryptTool.UNLOCK_TYPE_MAC.equals(unlockType)) {
            return this.mCountDownTimeForMac;
        }
        if (DecryptTool.UNLOCK_TYPE_ID.equals(unlockType)) {
            return this.mCountDownTimeForId;
        }
        return -1;
    }

    public PolicyManager getPolicyManager() {
        return this.mPolicyManager;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("ResetCountDownTimer dump");
        prefix = prefix + "    ";
        pw.print(prefix);
        pw.println("mCountDownTimeForMac  = " + this.mCountDownTimeForMac);
        pw.print(prefix);
        pw.println("mCountDownTimeForId   = " + this.mCountDownTimeForId);
    }
}
