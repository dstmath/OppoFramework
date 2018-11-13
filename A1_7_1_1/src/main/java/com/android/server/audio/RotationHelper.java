package com.android.server.audio;

import android.content.Context;
import android.media.AudioSystem;
import android.os.Handler;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import com.android.server.display.DisplayTransformManager;
import com.android.server.policy.WindowOrientationListener;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class RotationHelper {
    private static final String TAG = "AudioService.RotationHelper";
    private static Context sContext;
    private static int sDeviceRotation;
    private static AudioOrientationListener sOrientationListener;
    private static final Object sRotationLock = null;
    private static AudioWindowOrientationListener sWindowOrientationListener;

    static final class AudioOrientationListener extends OrientationEventListener {
        AudioOrientationListener(Context context) {
            super(context);
        }

        public void onOrientationChanged(int orientation) {
            RotationHelper.updateOrientation();
        }
    }

    static final class AudioWindowOrientationListener extends WindowOrientationListener {
        private static RotationCheckThread sRotationCheckThread;

        AudioWindowOrientationListener(Context context, Handler handler) {
            super(context, handler);
        }

        public void onProposedRotationChanged(int rotation) {
            RotationHelper.updateOrientation();
            if (sRotationCheckThread != null) {
                sRotationCheckThread.endCheck();
            }
            sRotationCheckThread = new RotationCheckThread();
            sRotationCheckThread.beginCheck();
        }
    }

    static final class RotationCheckThread extends Thread {
        private final int[] WAIT_TIMES_MS = new int[]{10, 20, 50, 100, 100, DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, 500};
        private final Object mCounterLock = new Object();
        private int mWaitCounter;

        RotationCheckThread() {
            super("RotationCheck");
        }

        void beginCheck() {
            synchronized (this.mCounterLock) {
                this.mWaitCounter = 0;
            }
            try {
                start();
            } catch (IllegalStateException e) {
            }
        }

        void endCheck() {
            synchronized (this.mCounterLock) {
                this.mWaitCounter = this.WAIT_TIMES_MS.length;
            }
        }

        public void run() {
            while (this.mWaitCounter < this.WAIT_TIMES_MS.length) {
                int waitTimeMs;
                synchronized (this.mCounterLock) {
                    waitTimeMs = this.mWaitCounter < this.WAIT_TIMES_MS.length ? this.WAIT_TIMES_MS[this.mWaitCounter] : 0;
                    this.mWaitCounter++;
                }
                if (waitTimeMs > 0) {
                    try {
                        sleep((long) waitTimeMs);
                        RotationHelper.updateOrientation();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.audio.RotationHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.audio.RotationHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.RotationHelper.<clinit>():void");
    }

    RotationHelper() {
    }

    static void init(Context context, Handler handler) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid null context");
        }
        sContext = context;
        sWindowOrientationListener = new AudioWindowOrientationListener(context, handler);
        sWindowOrientationListener.enable();
        if (!sWindowOrientationListener.canDetectOrientation()) {
            Log.i(TAG, "Not using WindowOrientationListener, reverting to OrientationListener");
            sWindowOrientationListener.disable();
            sWindowOrientationListener = null;
            sOrientationListener = new AudioOrientationListener(context);
            sOrientationListener.enable();
        }
    }

    static void enable() {
        if (sWindowOrientationListener != null) {
            sWindowOrientationListener.enable();
        } else {
            sOrientationListener.enable();
        }
        updateOrientation();
    }

    static void disable() {
        if (sWindowOrientationListener != null) {
            sWindowOrientationListener.disable();
        } else {
            sOrientationListener.disable();
        }
    }

    static void updateOrientation() {
        int newRotation = ((WindowManager) sContext.getSystemService("window")).getDefaultDisplay().getRotation();
        synchronized (sRotationLock) {
            if (newRotation != sDeviceRotation) {
                sDeviceRotation = newRotation;
                publishRotation(sDeviceRotation);
            }
        }
    }

    private static void publishRotation(int rotation) {
        Log.v(TAG, "publishing device rotation =" + rotation + " (x90deg)");
        switch (rotation) {
            case 0:
                AudioSystem.setParameters("rotation=0");
                return;
            case 1:
                AudioSystem.setParameters("rotation=90");
                return;
            case 2:
                AudioSystem.setParameters("rotation=180");
                return;
            case 3:
                AudioSystem.setParameters("rotation=270");
                return;
            default:
                Log.e(TAG, "Unknown device rotation");
                return;
        }
    }
}
