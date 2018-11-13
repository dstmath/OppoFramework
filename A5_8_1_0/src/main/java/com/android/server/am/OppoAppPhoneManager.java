package com.android.server.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.coloros.OppoListManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OppoAppPhoneManager {
    private static final String ACTION_APP_PHONE_REFUSE = "oppo.intent.action.APP_PHONE_REFUSE";
    private static final String SMART_DRIVE_PKG = "com.coloros.smartdrive";
    public static final String TAG = "OppoAppPhoneManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static OppoAppPhoneManager sOppoAppPhoneManager = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private ActivityManagerService mAms = null;
    private boolean mAppPhoneRefuseMode = false;
    boolean mDynamicDebug = false;

    public static OppoAppPhoneManager getInstance() {
        if (sOppoAppPhoneManager == null) {
            sOppoAppPhoneManager = new OppoAppPhoneManager();
        }
        return sOppoAppPhoneManager;
    }

    public void init(ActivityManagerService ams) {
        this.mAms = ams;
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
        boolean result = false;
        if (aInfo == null || aInfo.name == null) {
            return false;
        }
        if (isAppPhoneRefuseMode()) {
            if (OppoListManager.getInstance().isAppPhoneCpn(aInfo.name)) {
                Slog.v(TAG, "handleAppPhoneComing isAppPhoneCpn!");
                notifySmartDrive();
                result = true;
            }
            return result;
        }
        if (this.mDynamicDebug) {
            Slog.v(TAG, "handleAppPhoneComing return");
        }
        return false;
    }

    private void notifySmartDrive() {
        Intent intent = new Intent(ACTION_APP_PHONE_REFUSE);
        intent.setPackage(SMART_DRIVE_PKG);
        if (this.mDynamicDebug) {
            Slog.v(TAG, "notifySmartDrive intent = " + intent);
        }
        if (this.mAms != null) {
            this.mAms.mContext.sendBroadcast(intent);
        }
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
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", new Class[]{String.class});
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), new Object[]{OppoAppPhoneManager.class.getName()});
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
