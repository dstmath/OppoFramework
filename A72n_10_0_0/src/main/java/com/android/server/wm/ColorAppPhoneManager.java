package com.android.server.wm;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorAppPhoneManager implements IColorAppPhoneManager {
    private static final String ACTION_APP_PHONE_REFUSE = "oppo.intent.action.APP_PHONE_REFUSE";
    private static final String SMART_DRIVE_PKG = "com.coloros.smartdrive";
    public static final String TAG = "ColorAppPhoneManager";
    private static ColorAppPhoneManager sColorAppPhoneManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private boolean mAppPhoneRefuseMode = false;
    private ActivityTaskManagerService mAtms = null;
    private IColorActivityTaskManagerServiceEx mColorAtmsEx = null;
    boolean mDynamicDebug = false;

    public static ColorAppPhoneManager getInstance() {
        if (sColorAppPhoneManager == null) {
            sColorAppPhoneManager = new ColorAppPhoneManager();
        }
        return sColorAppPhoneManager;
    }

    private ColorAppPhoneManager() {
    }

    public void init(IColorActivityTaskManagerServiceEx atmsEx) {
        if (atmsEx != null) {
            this.mColorAtmsEx = atmsEx;
            this.mAtms = atmsEx.getActivityTaskManagerService();
        }
        registerLogModule();
    }

    public boolean isAppPhoneRefuseMode() {
        if (this.mDynamicDebug) {
            Slog.i(TAG, "isAppPhoneRefuseMode is " + this.mAppPhoneRefuseMode);
        }
        return this.mAppPhoneRefuseMode;
    }

    public void setAppPhoneRefuseMode(boolean mode) {
        if (this.DEBUG_SWITCH) {
            Slog.i(TAG, "setAppPhoneRefuseMode is " + mode);
        }
        this.mAppPhoneRefuseMode = mode;
    }

    public boolean handleAppPhoneComing(ActivityInfo aInfo) {
        if (this.mDynamicDebug) {
            Slog.v(TAG, "handleAppPhoneComing aInfo = " + aInfo);
        }
        if (aInfo == null || aInfo.name == null) {
            return false;
        }
        if (!isAppPhoneRefuseMode()) {
            if (this.mDynamicDebug) {
                Slog.v(TAG, "handleAppPhoneComing return");
            }
            return false;
        } else if (!OppoListManager.getInstance().isAppPhoneCpn(aInfo.name)) {
            return false;
        } else {
            Slog.v(TAG, "handleAppPhoneComing isAppPhoneCpn!");
            notifySmartDrive();
            return true;
        }
    }

    private void notifySmartDrive() {
        Intent intent = new Intent(ACTION_APP_PHONE_REFUSE);
        intent.setPackage(SMART_DRIVE_PKG);
        if (this.mDynamicDebug) {
            Slog.v(TAG, "notifySmartDrive intent = " + intent);
        }
        ActivityTaskManagerService activityTaskManagerService = this.mAtms;
        if (activityTaskManagerService != null && activityTaskManagerService.mH != null) {
            this.mAtms.mH.post(new Runnable(intent) {
                /* class com.android.server.wm.$$Lambda$ColorAppPhoneManager$gJhIKxyz4rPVVGzlG3xKRyfN1es */
                private final /* synthetic */ Intent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ColorAppPhoneManager.this.lambda$notifySmartDrive$0$ColorAppPhoneManager(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$notifySmartDrive$0$ColorAppPhoneManager(Intent intent) {
        this.mAtms.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorAppPhoneManager.class.getName());
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
}
