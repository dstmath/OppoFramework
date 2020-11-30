package com.android.server.am;

import android.common.OppoFeatureCache;
import android.os.Binder;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorSecurityPermissionManager implements IColorSecurityPermissionManager {
    public static final String TAG = "ColorPermissionManager";
    private static ColorSecurityPermissionManager sColorSecurityPermissionManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private ActivityManagerService mAms = null;
    private IColorActivityManagerServiceEx mColorAmsEx = null;
    boolean mDynamicDebug = false;

    public static ColorSecurityPermissionManager getInstance() {
        if (sColorSecurityPermissionManager == null) {
            sColorSecurityPermissionManager = new ColorSecurityPermissionManager();
        }
        return sColorSecurityPermissionManager;
    }

    private ColorSecurityPermissionManager() {
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mColorAmsEx = amsEx;
            this.mAms = amsEx.getActivityManagerService();
        }
        registerLogModule();
    }

    public boolean handleServiceTimeOut(Message msg) {
        OppoBaseProcessRecord basePr = typeCasting((ProcessRecord) msg.obj);
        if (basePr == null || !basePr.isWaitingPermissionChoice) {
            return false;
        }
        Message nmsg = this.mAms.mHandler.obtainMessage(12);
        nmsg.obj = msg.obj;
        this.mAms.mHandler.sendMessageDelayed(nmsg, 20000);
        return true;
    }

    public boolean checkPermission(String permission, int pid, int uid) {
        int uid2 = OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCorrectUid(uid);
        if (!UserHandle.isCore(Binder.getCallingUid())) {
            return OppoPermissionCallback.checkOppoPermission(permission, pid, uid2, this.mAms) == 0;
        }
        if (permission.startsWith(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
            permission = OppoPermissionConstants.PERMISSION_SEND_SMS;
        } else if (permission.startsWith(OppoPermissionConstants.PERMISSION_CALL_PHONE)) {
            permission = OppoPermissionConstants.PERMISSION_CALL_PHONE;
        }
        return this.mAms.checkPermission(permission, pid, uid2) == 0;
    }

    public boolean needCheckPermission() {
        return true;
    }

    public boolean isWaitingPermissionChoice(ProcessRecord proc) {
        OppoBaseProcessRecord basePr = typeCasting(proc);
        if (basePr != null) {
            return basePr.isWaitingPermissionChoice;
        }
        return false;
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
            m.invoke(cls.newInstance(), ColorSecurityPermissionManager.class.getName());
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

    private static OppoBaseProcessRecord typeCasting(ProcessRecord pr) {
        if (pr != null) {
            return (OppoBaseProcessRecord) ColorTypeCastingHelper.typeCasting(OppoBaseProcessRecord.class, pr);
        }
        return null;
    }
}
