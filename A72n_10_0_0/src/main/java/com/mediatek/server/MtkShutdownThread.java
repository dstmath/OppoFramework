package com.mediatek.server;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import com.android.server.power.PowerManagerService;
import com.android.server.power.ShutdownThread;
import java.lang.Thread;

public class MtkShutdownThread extends ShutdownThread {
    private static final int ANIMATION_MODE = 1;
    private static final int BACKLIGHT_STATE_POLL_SLEEP_MSEC = 50;
    private static boolean DEBUG = true;
    private static final int DEFAULT_MODE = 0;
    private static final int MAX_BLIGHT_OFF_DELAY_TIME = 5000;
    private static final int MAX_BLIGHT_OFF_POLL_TIME = 1000;
    private static final int MIN_SHUTDOWN_ANIMATION_PLAY_TIME = 5000;
    private static final String OPERATOR_SYSPROP = "persist.vendor.operator.optr";
    private static String TAG = "MtkShutdownThread";
    private static boolean bConfirmForAnimation = true;
    private static boolean bPlayaudio = true;
    private static long beginAnimationTime = 0;
    private static long endAnimationTime = 0;
    private static boolean mBlightOff = false;
    private static Runnable mDelayDim = new Runnable() {
        /* class com.mediatek.server.MtkShutdownThread.AnonymousClass1 */

        public void run() {
            if (MtkShutdownThread.sInstance.mScreenWakeLock != null && MtkShutdownThread.sInstance.mScreenWakeLock.isHeld()) {
                MtkShutdownThread.sInstance.mScreenWakeLock.release();
            }
            if (MtkShutdownThread.sInstance.mPowerManager == null) {
                MtkShutdownThread.sInstance.mPowerManager = (PowerManager) MtkShutdownThread.sInstance.mContext.getSystemService("power");
            }
            MtkShutdownThread.setBacklightOff();
        }
    };
    private static int mShutOffAnimation = -1;
    private static boolean mSpew = false;

    /* access modifiers changed from: protected */
    public boolean mIsShowShutdownSysui() {
        if (isCustBootAnim() == 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mIsShowShutdownDialog(Context context) {
        if (showShutdownAnimation(context)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mStartShutdownSeq(Context context) {
        if (getState() != Thread.State.NEW || isAlive()) {
            Log.i(TAG, "Thread state is not normal! froce to shutdown!");
            if (isCustBootAnim() == 1) {
                delayForPlayAnimation();
            }
            setBacklightOff();
            PowerManagerService.lowLevelShutdown(mReason);
            return false;
        }
        int screenTurnOffTime = 5000;
        if (isCustBootAnim() == 1) {
            screenTurnOffTime = getScreenTurnOffTime(context);
        }
        this.mHandler.postDelayed(mDelayDim, (long) screenTurnOffTime);
        return true;
    }

    /* access modifiers changed from: protected */
    public void mShutdownSeqFinish(Context context) {
        shutdownAnimationService();
        setBacklightOff();
    }

    /* access modifiers changed from: protected */
    public void mLowLevelShutdownSeq(Context context) {
        pollBacklightOff(context);
        if (mSpew && SystemProperties.getInt("vendor.shutdown_delay", 0) == 1) {
            Log.i(TAG, "Delay Shutdown 5s");
            SystemClock.sleep(5000);
        }
    }

    /* access modifiers changed from: private */
    public static void setBacklightOff() {
        if (!mBlightOff) {
            if (sInstance.mPowerManager == null) {
                Log.e(TAG, "check PowerManager: PowerManager service is null");
                return;
            }
            mBlightOff = true;
            Log.i(TAG, "setBacklightBrightness: Off");
            sInstance.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 8, 0);
        }
    }

    private void pollBacklightOff(Context context) {
        try {
            DisplayManager displayManager = (DisplayManager) context.getSystemService("display");
            DisplayInfo di = new DisplayInfo();
            long endTime = SystemClock.elapsedRealtime() + 1000;
            long TimeOut = endTime - SystemClock.elapsedRealtime();
            while (true) {
                if (TimeOut <= 0) {
                    break;
                }
                displayManager.getDisplay(0).getDisplayInfo(di);
                if (di.state == 1) {
                    break;
                }
                SystemClock.sleep(50);
                TimeOut = endTime - SystemClock.elapsedRealtime();
            }
            String str = TAG;
            Log.i(str, "Backlight polling take:" + (1000 - TimeOut) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutdownAnimationService() {
        if (isCustBootAnim() == 1) {
            Log.i(TAG, "set service.shutanim.running to 1");
            SystemProperties.set("service.shutanim.running", "1");
            if ((mReboot && mReason != null && mReason.equals("recovery")) || !mReboot) {
                delayForPlayAnimation();
            }
        }
    }

    private boolean showShutdownAnimation(Context context) {
        beginAnimationTime = 0;
        if (isCustBootAnim() != 1) {
            return false;
        }
        configShutdownAnimation(context);
        bootanimCust(context);
        return true;
    }

    private static void bootanimCust(Context context) {
        SystemProperties.set("service.shutanim.running", "0");
        Log.i(TAG, "set service.shutanim.running to 0");
        try {
            if (Settings.System.getInt(context.getContentResolver(), "accelerometer_rotation", 1) != 0) {
                IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
                if (wm != null) {
                    wm.freezeRotation(0);
                }
                Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation", 0);
                Settings.System.putInt(context.getContentResolver(), "accelerometer_rotation_restore", 1);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "check Rotation: context object is null when get Rotation");
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        beginAnimationTime = SystemClock.elapsedRealtime() + 5000;
        try {
            IWindowManager wm2 = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (wm2 != null) {
                wm2.setEventDispatching(false);
            }
        } catch (RemoteException e3) {
            e3.printStackTrace();
        }
        startBootAnimation();
    }

    private static void configShutdownAnimation(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService("power");
        if (bConfirmForAnimation || pm.isScreenOn()) {
            bPlayaudio = true;
        } else {
            bPlayaudio = false;
        }
    }

    private static void startBootAnimation() {
        Log.i(TAG, "Set 'service.bootanim.exit' = 0).");
        SystemProperties.set("service.bootanim.exit", "0");
        if (bPlayaudio) {
            SystemProperties.set("ctl.start", "banim_shutmp3");
            Log.i(TAG, "bootanim:shut mp3");
            return;
        }
        SystemProperties.set("ctl.start", "banim_shutnomp3");
        Log.i(TAG, "bootanim:shut nomp3");
    }

    private static void delayForPlayAnimation() {
        long j = beginAnimationTime;
        if (j > 0) {
            endAnimationTime = j - SystemClock.elapsedRealtime();
            if (endAnimationTime > 0) {
                try {
                    Thread.currentThread();
                    Thread.sleep(endAnimationTime);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Shutdown stop bootanimation Thread.currentThread().sleep exception!");
                }
            }
        }
    }

    public static int getScreenTurnOffTime() {
        if (SystemProperties.get(OPERATOR_SYSPROP, "0").equals("OP01")) {
            Log.i(TAG, "Inside MtkShutdownThread OP01");
            return 4000;
        } else if (SystemProperties.get(OPERATOR_SYSPROP, "0").equals("OP02")) {
            Log.i(TAG, "Inside MtkShutdownThread OP02");
            return 4000;
        } else if (!SystemProperties.get(OPERATOR_SYSPROP, "0").equals("OP09")) {
            return 4000;
        } else {
            Log.i(TAG, "Inside MtkShutdownThread OP09");
            return 3000;
        }
    }

    private static int getScreenTurnOffTime(Context context) {
        int screenTurnOffTime = 0;
        try {
            screenTurnOffTime = getScreenTurnOffTime();
            String str = TAG;
            Log.i(str, "screen turn off time screenTurnOffTime =" + screenTurnOffTime);
            return screenTurnOffTime;
        } catch (Exception e) {
            e.printStackTrace();
            return screenTurnOffTime;
        }
    }

    public static int isCustBootAnim() {
        return mShutOffAnimation;
    }
}
