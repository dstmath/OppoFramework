package com.android.server.am;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.ColorServiceRegistry;
import com.android.server.am.ColorInjector;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.IColorDataFreeManager;
import com.color.util.ColorTypeCastingHelper;

public final class ColorActivityManagerServiceEx extends ColorDummyActivityManagerServiceEx {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String TAG = "ColorActivityManagerServiceEx";
    private ColorActivityManagerMessageHelper mMessageHelper;
    private ColorActivityManagerTransactionHelper mTransactionHelper;

    public ColorActivityManagerServiceEx(Context context, ActivityManagerService ams) {
        super(context, ams);
        init(context, ams);
    }

    public IColorBroadcastQueueEx getColorBroadcastQueueEx(BroadcastQueue queue) {
        return new ColorBroadcastQueueEx(queue);
    }

    public IColorActivityManagerServiceInner getColorActivityManagerServiceInner() {
        OppoBaseActivityManagerService baseAms = typeCasting(this.mAms);
        if (baseAms != null) {
            return baseAms.mColorAmsInner;
        }
        return null;
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        ColorServiceRegistry.getInstance().serviceReady(22);
        ColorActivityManagerTransactionHelper colorActivityManagerTransactionHelper = this.mTransactionHelper;
        if (colorActivityManagerTransactionHelper != null) {
            colorActivityManagerTransactionHelper.systemReady();
        }
        OppoFeatureCache.get(IColorDataFreeManager.DEFAULT).generatePlaceHolderFiles();
        ColorInjector.ActivityManagerService.init(this.mAms);
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        ColorActivityManagerTransactionHelper colorActivityManagerTransactionHelper = this.mTransactionHelper;
        if (colorActivityManagerTransactionHelper != null) {
            return colorActivityManagerTransactionHelper.onTransact(code, data, reply, flags);
        }
        return ColorActivityManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    public void handleMessage(Message msg, int whichHandler) {
        if (DEBUG) {
            Slog.i(TAG, "handleMessage msg = " + msg + " handler = " + whichHandler);
        }
        ColorActivityManagerMessageHelper colorActivityManagerMessageHelper = this.mMessageHelper;
        if (colorActivityManagerMessageHelper != null) {
            colorActivityManagerMessageHelper.handleMessage(msg, whichHandler);
        }
    }

    private void init(Context context, ActivityManagerService ams) {
        this.mMessageHelper = new ColorActivityManagerMessageHelper(context, this);
        this.mTransactionHelper = new ColorActivityManagerTransactionHelper(context, this);
        ColorServiceRegistry.getInstance().serviceInit(2, this);
    }

    private static OppoBaseActivityManagerService typeCasting(ActivityManagerService ams) {
        if (ams != null) {
            return (OppoBaseActivityManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityManagerService.class, ams);
        }
        return null;
    }
}
