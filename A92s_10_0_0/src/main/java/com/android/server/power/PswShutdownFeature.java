package com.android.server.power;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.OppoBaseEnvironment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.util.Log;
import android.view.IWindowManager;
import android.view.OppoWindowManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.IOppoBrightness;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PswShutdownFeature implements IPswShutdownFeature {
    private static final int MIN_FILE_COUNT = 10;
    private static final int MIN_SHUTDOWN_ANIMATION_PLAY_TIME_FOR_CMCC = 3000;
    private static final String OEM_BOOTANIMATION_FILE = "/oem/media/shutdownanimation.zip";
    private static final int SHUTDOWN_ANIM_SHOW_DELAY = 50;
    private static final int SHUTDOWN_TIMEOUT = 10000;
    private static final String SYSTEM_BOOTANIMATION_FILE = (OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath() + "/media/bootanimation/rbootanimation.zip");
    private static final String SYSTEM_ENCRYPTED_BOOTANIMATION_FILE = "/system/product/media/shutdownanimation-encrypted.zip";
    private static final String TAG = "PswShutdownFeature";
    private static AtomicBoolean sHasVibrate = new AtomicBoolean(false);
    private static boolean sOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private long mBeginAnimationTime;
    private Context mContext;
    private long mEndAnimationTime;
    private boolean mIsAnimationtimeForCmcc;
    private boolean mIsContextInited;
    private IOppoBrightness mOppoBrightness;
    private OppoWindowManager mOppoWindowManager;

    private PswShutdownFeature() {
        this.mBeginAnimationTime = 0;
        this.mEndAnimationTime = 0;
        this.mIsAnimationtimeForCmcc = false;
        this.mOppoBrightness = null;
        this.mOppoWindowManager = null;
        this.mIsContextInited = false;
        init();
    }

    private void setContext(Context context) {
        if (!this.mIsContextInited) {
            this.mContext = context;
            this.mIsContextInited = true;
        }
    }

    private static class InstanceHolder {
        static final PswShutdownFeature INSTANCE = new PswShutdownFeature();

        private InstanceHolder() {
        }
    }

    public static PswShutdownFeature getInstance(Context context) {
        Log.d(TAG, "getInstance.");
        PswShutdownFeature instance = InstanceHolder.INSTANCE;
        instance.setContext(context);
        return instance;
    }

    private void init() {
        Log.d(TAG, "PswShutdownFeature init.");
        this.mOppoWindowManager = new OppoWindowManager();
    }

    public void showShutdownBacktrace(boolean spew) {
        Log.d(TAG, "!!! Request to shutdown !!!");
        if (spew) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (StackTraceElement element : stack) {
                Log.d(TAG, "    |----" + element.toString());
            }
        }
    }

    public void resetBrightnessAdj(Context context) {
        if (this.mOppoBrightness != null) {
            Log.d(TAG, "resetBrightnessAdj, now run callback");
            this.mOppoBrightness.resetBrightnessAdj();
            return;
        }
        Log.w(TAG, "resetBrightnessAdj, no callback");
    }

    public void setBeginAnimationTime(long beginAnimTime, boolean isCmcc) {
        Log.d(TAG, "setBeginAnimationTime");
        this.mBeginAnimationTime = beginAnimTime;
        this.mIsAnimationtimeForCmcc = isCmcc;
    }

    public void shutdownOppoService(Context context) {
        Log.d(TAG, "shutdownOppoService");
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        Log.i(TAG, "mute audios...");
        audioManager.requestAudioFocus(null, 3, 1);
        for (int i = 0; i < AudioSystem.getNumStreamTypes(); i++) {
            if (i != 1) {
                audioManager.setStreamMute(i, true);
            }
            if (!audioManager.isSilentMode()) {
                audioManager.setStreamMute(1, false);
            }
        }
        try {
            IWindowManager.Stub.asInterface(ServiceManager.checkService("window")).setEventDispatching(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SystemClock.sleep(50);
        showShutdownAnimation();
    }

    public void checkShutdownTimeout(Context context, final boolean reboot, final String reason, final int shutdonwVibrateInMs, final AudioAttributes vibrateAttribute) {
        Log.d(TAG, "checkShutdownTimeout");
        postDelayed(new Runnable() {
            /* class com.android.server.power.PswShutdownFeature.AnonymousClass1 */

            public void run() {
                PswShutdownFeature.this.onCheckShutdownTimeout(reboot, reason, shutdonwVibrateInMs, vibrateAttribute);
            }
        }, 10000);
    }

    public void delayForPlayAnimation() {
        Log.d(TAG, "delayForPlayAnimation");
        if (this.mIsAnimationtimeForCmcc) {
            Log.i(TAG, "delay for paly cmcc animation");
            long j = this.mBeginAnimationTime;
            if (j > 0) {
                this.mEndAnimationTime = j - SystemClock.elapsedRealtime();
                if (this.mEndAnimationTime > 0) {
                    try {
                        Thread.currentThread();
                        Thread.sleep(this.mEndAnimationTime);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Shutdown stop bootanimation Thread.currentThread().sleep exception!");
                    }
                }
            }
        }
    }

    public void storeDellog() {
        Log.d(TAG, "storeDellog");
        try {
            File baseDir = new File("/data/oppo/log/dellog");
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            BufferedReader br = new BufferedReader(new FileReader("/proc/dellog"));
            FileOutputStream fos = new FileOutputStream(new File(baseDir, "dellog-" + getDateToString()));
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    fos.write((line + StringUtils.LF).getBytes());
                } else {
                    fos.close();
                    br.close();
                    deleteDellog();
                    return;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "storeDellog error: " + e);
        }
    }

    public boolean shouldDoLowLevelShutdown() {
        Log.d(TAG, "judge shouldDoLowLevelShutdown");
        return sHasVibrate.compareAndSet(false, true);
    }

    public void setOppoBrightnessCallback(IOppoBrightness callback) {
        Log.d(TAG, "setOppoBrightnessCallback");
        this.mOppoBrightness = callback;
    }

    private void showShutdownAnimation() {
        Log.d(TAG, "showShutdownAnimation");
        if (checkAnimationFileExist()) {
            lockDevice();
            playShutdownAnimation();
            return;
        }
        Log.i(TAG, "shutdown animation res is not exit....");
    }

    private void postDelayed(final Runnable r, final long delayMillis) {
        new Thread() {
            /* class com.android.server.power.PswShutdownFeature.AnonymousClass2 */

            public void run() {
                try {
                    sleep(delayMillis);
                } catch (Exception e) {
                }
                r.run();
            }
        }.start();
    }

    private boolean checkAnimationFileExist() {
        if (new File(OEM_BOOTANIMATION_FILE).exists() || new File(SYSTEM_BOOTANIMATION_FILE).exists() || new File(SYSTEM_ENCRYPTED_BOOTANIMATION_FILE).exists()) {
            return true;
        }
        return false;
    }

    private void playShutdownAnimation() {
        if (this.mIsAnimationtimeForCmcc) {
            this.mBeginAnimationTime = SystemClock.elapsedRealtime() + 3000;
        }
        SystemProperties.set("service.bootanim.exit", "0");
        Log.i(TAG, "show ShutdownAnimation...");
        SystemProperties.set("ctl.start", "rbootanim");
    }

    /* access modifiers changed from: private */
    public void onCheckShutdownTimeout(boolean reboot, String reason, int shutdonwVibrateInMs, AudioAttributes vibrateAttribute) {
        if (sHasVibrate.compareAndSet(false, true)) {
            Log.i(TAG, "wait shutdown timeout! force shutdown now");
            if (shutdonwVibrateInMs > 0 && this.mContext != null && !"sau".equals(reason) && !"silence".equals(reason)) {
                try {
                    new SystemVibrator(this.mContext).vibrate((long) shutdonwVibrateInMs, vibrateAttribute);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to vibrate during shutdown.", e);
                }
                Log.i(TAG, "wait shutdown timeout! vibrate for better user experience");
                try {
                    Thread.sleep((long) shutdonwVibrateInMs);
                } catch (InterruptedException e2) {
                }
            }
            if (reboot) {
                PowerManagerService.lowLevelReboot(reason);
            } else {
                PowerManagerService.lowLevelShutdown(reason);
            }
        }
    }

    private String getDateToString() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    }

    private static void deleteDellog() {
        File[] allFiles = new File("/data/oppo/log/dellog").listFiles();
        if (allFiles.length >= MIN_FILE_COUNT) {
            allFiles[0].delete();
        }
    }

    private void lockDevice() {
        try {
            IWindowManager.Stub.asInterface(ServiceManager.getService("window")).updateRotation(false, false);
            this.mOppoWindowManager.setBootAnimationRotationLock(true);
        } catch (RemoteException e) {
            Log.w(TAG, "boot animation can not lock device!");
        }
    }

    private void stopTopApplication(final Context context) {
        new Thread(new Runnable() {
            /* class com.android.server.power.PswShutdownFeature.AnonymousClass3 */

            public void run() {
                ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
                List<ActivityManager.RecentTaskInfo> apps = activityManager.getRecentTasks(1, 2);
                if (apps.size() > 0) {
                    ActivityManager.RecentTaskInfo topApp = apps.get(0);
                    String packageName = topApp.baseIntent.getComponent().getPackageName();
                    String className = topApp.baseIntent.getComponent().getClassName();
                    Intent homeActIntent = new Intent("android.intent.action.MAIN");
                    homeActIntent.addCategory("android.intent.category.HOME");
                    ActivityInfo homeInfo = homeActIntent.resolveActivityInfo(context.getPackageManager(), 0);
                    if (homeInfo == null || !homeInfo.packageName.equals(packageName) || !homeInfo.name.equals(className)) {
                        Log.i(PswShutdownFeature.TAG, "force stop package " + packageName);
                        activityManager.forceStopPackage(packageName);
                    }
                }
            }
        }).start();
    }
}
