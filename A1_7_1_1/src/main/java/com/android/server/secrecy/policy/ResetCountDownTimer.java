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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ResetCountDownTimer {
    private static int DEFAULT_COUNTDOWN_TIME_FOR_ID;
    private static int DEFAULT_COUNTDOWN_TIME_FOR_MAC;
    private static String TAG;
    private final String ACTION_ALARM_INTENT;
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.policy.ResetCountDownTimer.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.policy.ResetCountDownTimer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.secrecy.policy.ResetCountDownTimer.<clinit>():void");
    }

    public ResetCountDownTimer(Context context, PolicyManager policyManager) {
        this.ACTION_ALARM_INTENT = "android.secrecy.policyfactor.invalid.expire." + getClass().getSimpleName();
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
