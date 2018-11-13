package com.android.server.policy;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IOppoExService;
import android.os.IOppoExService.Stub;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.oppo.IElsaManager;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;

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
public class OppoScreenOffGestureManager {
    public static final int GESTURE_CIRCLE = 6;
    public static final int GESTURE_DOUBLE_SWIP = 7;
    public static final int GESTURE_DOUBLE_TAP = 1;
    public static final int GESTURE_DOWN_TO_UP_SWIP = 11;
    public static final int GESTURE_DOWN_VEE = 3;
    public static final int GESTURE_LEFT_TO_RIGHT_SWIP = 8;
    public static final int GESTURE_LEFT_VEE = 4;
    public static final int GESTURE_M = 12;
    public static final int GESTURE_RIGHT_TO_LEFT_SWIP = 9;
    public static final int GESTURE_RIGHT_VEE = 5;
    public static final int GESTURE_UP_TO_DOWN_SWIP = 10;
    public static final int GESTURE_UP_VEE = 2;
    public static final int GESTURE_W = 13;
    public static final int MSG_SCREEN_TURNED_OFF = 10001;
    public static final int MSG_SCREEN_TURNING_ON = 10000;
    private static String TAG = null;
    private static final int WAIT_TIME_CPU_LOCK = 1000;
    PhoneStateListener listener;
    private WakeLock mAnimCpuLock;
    private AudioManager mAudioManager;
    private IOppoExService mExManager;
    private OppoScreenOffGestureUtil mGestureUtil;
    private boolean mIsInOffHook;
    private PowerManager mPowerManager;

    private class AnimDataInfo {
        public int mMode = 0;

        AnimDataInfo(int nMode) {
            this.mMode = nMode;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.OppoScreenOffGestureManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.OppoScreenOffGestureManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.OppoScreenOffGestureManager.<clinit>():void");
    }

    OppoScreenOffGestureManager(Context context, Handler handler, KeyguardServiceDelegate keyguardMediator, WakeLock broadcastWakeLock) {
        this.mIsInOffHook = false;
        this.mAudioManager = null;
        this.mExManager = null;
        this.mGestureUtil = null;
        this.listener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                Log.d(OppoScreenOffGestureManager.TAG, "onCallStateChanged state = " + state);
                switch (state) {
                    case 0:
                        OppoScreenOffGestureManager.this.mIsInOffHook = false;
                        return;
                    case 1:
                        OppoScreenOffGestureManager.this.mIsInOffHook = true;
                        return;
                    case 2:
                        OppoScreenOffGestureManager.this.mIsInOffHook = true;
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mAnimCpuLock = this.mPowerManager.newWakeLock(1, "OppoScreenOffGestureManager.mAnimCpuLock");
        this.mGestureUtil = new OppoScreenOffGestureUtil(context);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.listener, 32);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    int dealScreenOffGesture(KeyEvent event, int policyFlags, boolean isScreenOn) {
        int keyCode = event.getKeyCode();
        boolean down = event.getAction() == 0;
        policyFlags &= -1073741825;
        if (!isScreenOn || this.mIsInOffHook) {
            if (keyCode == 134 && down) {
                int nGesture = this.mGestureUtil.mGestureType;
                if (!isGestureExist(nGesture)) {
                    return policyFlags;
                }
                this.mAnimCpuLock.acquire(1000);
                Log.d(TAG, "dealScreenOffGesture is " + gestureTosString(nGesture));
                dealExScreenOffGesture(nGesture);
            }
            return policyFlags;
        }
        Log.d(TAG, "-----  return");
        return policyFlags;
    }

    void screenTurnedOff() {
        if (this.mExManager == null) {
            this.mExManager = Stub.asInterface(ServiceManager.getService("OPPOExService"));
        }
        if (this.mExManager == null) {
            Slog.e(TAG, "mExManager == null!!!");
            return;
        }
        try {
            this.mExManager.dealScreenoffGesture(MSG_SCREEN_TURNED_OFF);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failing screenTurnedOff", e);
        }
    }

    void screenTurningOn() {
        if (this.mExManager == null) {
            this.mExManager = Stub.asInterface(ServiceManager.getService("OPPOExService"));
        }
        if (this.mExManager == null) {
            Slog.e(TAG, "mExManager == null!!!");
            return;
        }
        try {
            this.mExManager.dealScreenoffGesture(10000);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failing screenTurningOn", e);
        }
    }

    private String gestureTosString(int nGesture) {
        String strGesture = IElsaManager.EMPTY_PACKAGE;
        switch (nGesture) {
            case 1:
                return "GESTURE_DOUBLE_TAP";
            case 2:
                return "GESTURE_UP_VEE";
            case 3:
                return "GESTURE_DOWN_VEE";
            case 4:
                return "GESTURE_LEFT_VEE";
            case 5:
                return "GESTURE_RIGHT_VEE";
            case 6:
                return "GESTURE_CIRCLE";
            case 7:
                return "GESTURE_DOUBLE_SWIP";
            case 8:
                return "GESTURE_LEFT_TO_RIGHT_SWIP";
            case 9:
                return "GESTURE_RIGHT_TO_LEFT_SWIP";
            case 10:
                return "GESTURE_UP_TO_DOWN_SWIP";
            case 11:
                return "GESTURE_DOWN_TO_UP_SWIP";
            case 12:
                return "GESTURE_M";
            case 13:
                return "GESTURE_W";
            default:
                return strGesture;
        }
    }

    boolean isScreenoffGestureKey(int keyCode) {
        return 134 == keyCode;
    }

    private void dealExScreenOffGesture(int nGesture) {
        if (this.mExManager == null) {
            this.mExManager = Stub.asInterface(ServiceManager.getService("OPPOExService"));
        }
        if (this.mExManager == null) {
            Slog.e(TAG, "mExManager == null!!!");
            return;
        }
        Log.d(TAG, "OppoScreenOffGestureManager  dealScreenoffGesture nGesture = " + nGesture + "  mExManager = " + this.mExManager);
        try {
            this.mExManager.dealScreenoffGesture(nGesture);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failing dealScreenoffGesture", e);
        }
    }

    private boolean isGestureExist(int nGesture) {
        boolean isGestureExist = false;
        if (this.mExManager == null) {
            this.mExManager = Stub.asInterface(ServiceManager.getService("OPPOExService"));
        }
        if (this.mExManager == null) {
            Slog.e(TAG, "mExManager == null!!!");
            return false;
        }
        try {
            isGestureExist = this.mExManager.getGestureState(nGesture);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failing getGestureState", e);
        }
        Log.d(TAG, "OppoScreenOffGestureManager isGestureExist = " + isGestureExist);
        return isGestureExist;
    }

    boolean isInOffHook() {
        return (!this.mIsInOffHook || this.mAudioManager.isSpeakerphoneOn() || this.mAudioManager.isWiredHeadsetOn()) ? false : true;
    }

    boolean isGestureDoubleTap() {
        return this.mGestureUtil.mGestureType == 1;
    }

    public void updateGestureInfo() {
        this.mGestureUtil.updateGestureInfo();
    }
}
