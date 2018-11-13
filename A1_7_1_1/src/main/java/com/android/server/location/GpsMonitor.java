package com.android.server.location;

import android.util.Log;
import java.util.Arrays;

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
public class GpsMonitor {
    private static boolean DEBUG = false;
    private static final int MIN_DIS_NAV_DIST_NUM = 360;
    private static final int MIN_INDOOR_DIST_NUM = 600;
    private static final int MIN_INDOOR_RESET_NUM = 5;
    private static final int MIN_NAV_DIST_NUM = 30;
    private static final int MIN_NAV_RESET_NUM = 5;
    private static final int MIN_OUTDOOR_DIST_NUM = 10;
    private static final int MIN_OUTDOOR_SNR_NUM = 4;
    private static final int MIN_SNR_VALUE = 8;
    private static final float MIN_WALK_SPEED = 4.0f;
    public static final int NAVIGATION_MODE_FALSE = 2;
    public static final int NAVIGATION_MODE_INVALID = -1;
    public static final int NAVIGATION_MODE_TRUE = 1;
    public static final int NAVIGATION_MODE_UNSURE = 0;
    private static final String TAG = "GpsMonitor";
    private static int mDisNavTimer;
    private static int mNavTimer;
    private int mIndoorTimer;
    private boolean mIsNavNow;
    private float[] mLastSnrs;
    private int mOutdoorTimer;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.location.GpsMonitor.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.location.GpsMonitor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsMonitor.<clinit>():void");
    }

    public GpsMonitor() {
        this.mLastSnrs = null;
        this.mIndoorTimer = 0;
        this.mOutdoorTimer = 0;
        this.mIsNavNow = false;
    }

    public void resetStatus() {
        this.mIndoorTimer = 0;
        this.mOutdoorTimer = 0;
        mNavTimer = 0;
        mDisNavTimer = 0;
        this.mIsNavNow = false;
    }

    public boolean needStopGps(float speed, float[] snrs) {
        if (this.mIsNavNow) {
            return false;
        }
        boolean isNavigating = isNavigateMode(speed);
        boolean isIndoor = isIndoorMode(snrs);
        if (isNavigating || !isIndoor) {
            return false;
        }
        return true;
    }

    public void enableLog(boolean verbose) {
        DEBUG = verbose;
    }

    public static int getNavigateMode() {
        if (DEBUG) {
            Log.d(TAG, "getNavigateMode mDisNavTimer = " + mDisNavTimer + ", mNavTimer = " + mNavTimer);
        }
        if (MIN_DIS_NAV_DIST_NUM <= mDisNavTimer) {
            return 2;
        }
        if (30 <= mNavTimer) {
            return 1;
        }
        if (mDisNavTimer > 0 || mNavTimer > 0) {
            return 0;
        }
        return -1;
    }

    private boolean isNavigateMode(float speed) {
        boolean isNavigate = this.mIsNavNow;
        if (isNavigateStatus(speed)) {
            mNavTimer++;
            if (5 <= mNavTimer) {
                mDisNavTimer = 0;
                if (30 <= mNavTimer) {
                    isNavigate = true;
                    this.mIsNavNow = true;
                }
            }
        } else {
            mDisNavTimer++;
            if (5 <= mDisNavTimer) {
                mNavTimer = 0;
            }
            if (MIN_DIS_NAV_DIST_NUM <= mDisNavTimer) {
                isNavigate = false;
                this.mIsNavNow = false;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "--isNavigateMode NavTime = " + mNavTimer + ", disNavTime = " + mDisNavTimer + ", mode = " + isNavigate);
        }
        return isNavigate;
    }

    private boolean isIndoorMode(float[] snrs) {
        boolean isIndoor = false;
        if (isIndoorStatus(snrs)) {
            this.mIndoorTimer++;
            if (5 <= this.mIndoorTimer) {
                this.mOutdoorTimer = 0;
                if (600 <= this.mIndoorTimer) {
                    isIndoor = true;
                }
            }
        } else {
            this.mOutdoorTimer++;
            if (5 <= this.mOutdoorTimer) {
                this.mIndoorTimer = 0;
                if (10 <= this.mOutdoorTimer) {
                    isIndoor = false;
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "--isIndoorMode mOutdoorTimer = " + this.mOutdoorTimer + ", mIndoorTimer = " + this.mIndoorTimer + ", mode = " + isIndoor);
        }
        return isIndoor;
    }

    private boolean isNavigateStatus(float speed) {
        boolean isNavigate = false;
        if (MIN_WALK_SPEED < speed) {
            isNavigate = true;
        }
        if (DEBUG) {
            Log.d(TAG, "--isNavigateStatus speed = " + speed + ", status = " + isNavigate);
        }
        return isNavigate;
    }

    private boolean isIndoorStatus(float[] snrs) {
        boolean isIndoorMode = true;
        if (this.mLastSnrs == null) {
            this.mLastSnrs = (float[]) snrs.clone();
        } else if (!Arrays.equals(this.mLastSnrs, snrs)) {
            int tmpNum = 0;
            this.mLastSnrs = (float[]) snrs.clone();
            for (float snr : this.mLastSnrs) {
                if (8.0f < snr) {
                    tmpNum++;
                    if (4 <= tmpNum) {
                        isIndoorMode = false;
                        break;
                    }
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "--isIndoorStatus snr = " + this.mLastSnrs.toString() + ", status = " + isIndoorMode);
        }
        return isIndoorMode;
    }
}
