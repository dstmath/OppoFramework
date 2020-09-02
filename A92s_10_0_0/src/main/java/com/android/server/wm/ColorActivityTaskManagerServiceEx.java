package com.android.server.wm;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.ColorServiceRegistry;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;

public class ColorActivityTaskManagerServiceEx extends ColorDummyActivityTaskManagerServiceEx {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String TAG = "ColorActivityTaskManagerServiceEx";
    private ColorActivityTaskManagerTransactionHelper mTransactionHelper;

    public ColorActivityTaskManagerServiceEx(Context context, ActivityTaskManagerService atms) {
        super(context, atms);
        init(context);
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        ColorServiceRegistry.getInstance().serviceReady(21);
        ColorActivityTaskManagerTransactionHelper colorActivityTaskManagerTransactionHelper = this.mTransactionHelper;
        if (colorActivityTaskManagerTransactionHelper != null) {
            colorActivityTaskManagerTransactionHelper.systemReady();
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (DEBUG) {
            Slog.i(TAG, "onTransaction code = " + code);
        }
        ColorActivityTaskManagerTransactionHelper colorActivityTaskManagerTransactionHelper = this.mTransactionHelper;
        if (colorActivityTaskManagerTransactionHelper != null) {
            return colorActivityTaskManagerTransactionHelper.onTransact(code, data, reply, flags);
        }
        return ColorActivityTaskManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    private void init(Context context) {
        this.mTransactionHelper = new ColorActivityTaskManagerTransactionHelper(context, this);
        ColorServiceRegistry.getInstance().serviceInit(1, this);
    }

    public IColorActivityStackInner getColorActivityStackInner(ActivityStack stack) {
        OppoBaseActivityStack baseStack = typeCasting(stack);
        if (baseStack != null) {
            return baseStack.mColorStackInner;
        }
        return null;
    }

    public IColorActivityRecordEx getColorActivityRecordEx(ActivityRecord ar) {
        return new ColorActivityRecordEx(ar);
    }

    public IColorActivityStackEx getColorActivityStackEx(ActivityStack stack) {
        return new ColorActivityStackEx(stack);
    }

    public IColorActivityStarterEx getColorActivityStarterEx(ActivityStarter starter) {
        return new ColorActivityStarterEx(starter);
    }

    public IColorActivityStackSupervisorEx getColorActivityStackSupervisorEx(ActivityStackSupervisor supervisor) {
        return new ColorActivityStackSupervisorEx(supervisor);
    }

    private static OppoBaseActivityStack typeCasting(ActivityStack stack) {
        if (stack != null) {
            return (OppoBaseActivityStack) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStack.class, stack);
        }
        return null;
    }
}
