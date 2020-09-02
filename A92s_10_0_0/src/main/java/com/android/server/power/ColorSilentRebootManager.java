package com.android.server.power;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ColorFreeformManagerService;
import com.color.util.ColorTypeCastingHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorSilentRebootManager implements IColorSilentRebootManager {
    private static String BLACKLIGHT_PATH = "/sys/kernel/oppo_display/sau_closebl_node";
    private static final int CANCEL_BACKLIGHT_DELAY_TIME = 2000;
    private static final String TAG = "ColorSilentRebootManager";
    private static ColorSilentRebootManager sColorSilentRebootManager;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private final Runnable mCancelBacklightLimit = new Runnable() {
        /* class com.android.server.power.ColorSilentRebootManager.AnonymousClass1 */

        public void run() {
            Slog.d(ColorSilentRebootManager.TAG, "run processBlackLight");
            ColorSilentRebootManager colorSilentRebootManager = ColorSilentRebootManager.this;
            colorSilentRebootManager.processBlackLight(colorSilentRebootManager.mContext);
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    boolean mDynamicDebug = false;
    private PowerManagerService mPowerService;

    private ColorSilentRebootManager() {
    }

    public static ColorSilentRebootManager getInstance() {
        if (sColorSilentRebootManager == null) {
            sColorSilentRebootManager = new ColorSilentRebootManager();
        }
        return sColorSilentRebootManager;
    }

    public void init(IColorPowerManagerServiceEx pmsEx) {
        this.mPowerService = pmsEx.getPowerManagerService();
        this.mContext = pmsEx.getContext();
    }

    private int readSilenceFlagValue() {
        File file = new File(BLACKLIGHT_PATH);
        if (file.exists()) {
            InputStream instream = null;
            try {
                instream = new FileInputStream(file);
                int value = Integer.parseInt(new BufferedReader(new InputStreamReader(instream)).readLine());
                Slog.d(TAG, "silence flags file value is :" + value);
                try {
                    instream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return value;
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e3) {
                Slog.d(TAG, "read silence flags file exception");
                if (instream != null) {
                    try {
                        instream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (instream != null) {
                    try {
                        instream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                throw th;
            }
        } else {
            Slog.d(TAG, "silence flags file no exist");
            return 0;
        }
        return 0;
    }

    private void markSlsauEnd() {
        if (SystemProperties.get("persist.sys.saupwk_en", "").equals("1")) {
            SystemProperties.set("persist.sys.rbsreason", "");
            String str_old = SystemProperties.get("sys.slsau_finished", "");
            SystemProperties.set("sys.slsau_finished", "true");
            String str_new = SystemProperties.get("sys.slsau_finished", "");
            Slog.d(TAG, "[SAUPWK]: setting property sys.slsau_finished:" + str_old + " to " + str_new);
        }
    }

    private void writeSilenceFlagValue() {
        File file = new File(BLACKLIGHT_PATH);
        if (file.exists()) {
            OutputStream outstream = null;
            try {
                outstream = new FileOutputStream(file);
                Slog.d(TAG, "write 0 to silence flags file ");
                outstream.write("0".getBytes());
                outstream.flush();
                try {
                    outstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                if (outstream != null) {
                    outstream.close();
                }
            } catch (IOException e3) {
                e3.printStackTrace();
                if (outstream != null) {
                    outstream.close();
                }
            } catch (Throwable th) {
                if (outstream != null) {
                    try {
                        outstream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                throw th;
            }
        } else {
            Slog.d(TAG, "write flags file no exist");
        }
    }

    /* access modifiers changed from: private */
    public void processBlackLight(Context context) {
        if (1 == readSilenceFlagValue()) {
            Slog.d(TAG, "now, there is no backlight, so open it!");
            markSlsauEnd();
            writeSilenceFlagValue();
            getInner().goToSleepInternal(SystemClock.uptimeMillis(), 4, 0, (int) ColorFreeformManagerService.FREEFORM_CALLER_UID);
        }
    }

    public void postProcessBlackLightTask(Handler hanlder) {
        hanlder.postDelayed(this.mCancelBacklightLimit, 2000);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog#### mDynamicDebug = " + this.mDynamicDebug);
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + this.mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorSilentRebootManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    private IColorPowerManagerServiceInner getInner() {
        OppoBasePowerManagerService basePms;
        PowerManagerService powerManagerService = this.mPowerService;
        if (powerManagerService == null || (basePms = (OppoBasePowerManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePowerManagerService.class, powerManagerService)) == null || basePms.mColorPowerMSInner == null) {
            return IColorPowerManagerServiceInner.DEFAULT;
        }
        return basePms.mColorPowerMSInner;
    }
}
