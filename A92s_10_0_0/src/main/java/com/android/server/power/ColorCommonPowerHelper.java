package com.android.server.power;

import android.content.Context;
import android.os.ColorCommonPowerTransaction;
import android.os.IColorCommonPowerManager;
import android.os.Parcel;
import com.color.os.IColorScreenStatusListener;

public class ColorCommonPowerHelper implements IColorCommonPowerManager {
    private static final String TAG = "ColorCommonPowerHelper";
    private final ColorPowerNotifierContext mContext;
    private final PowerManagerService mService;

    public ColorCommonPowerHelper(Context context, PowerManagerService service) {
        this.mContext = ColorPowerNotifierContext.from(context);
        this.mService = service;
    }

    public void registerScreenStatusListener(IColorScreenStatusListener listener) {
        ColorPowerNotifierContext colorPowerNotifierContext = this.mContext;
        if (colorPowerNotifierContext != null) {
            colorPowerNotifierContext.registerScreenStatusListener(listener);
        }
    }

    public void unregisterScreenStatusListener(IColorScreenStatusListener listener) {
        ColorPowerNotifierContext colorPowerNotifierContext = this.mContext;
        if (colorPowerNotifierContext != null) {
            colorPowerNotifierContext.unregisterScreenStatusListener(listener);
        }
    }

    /* renamed from: com.android.server.power.ColorCommonPowerHelper$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$os$ColorCommonPowerTransaction = new int[ColorCommonPowerTransaction.values().length];

        static {
            try {
                $SwitchMap$android$os$ColorCommonPowerTransaction[ColorCommonPowerTransaction.REGISTER_SCREEN_STATUS_LISTENER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$os$ColorCommonPowerTransaction[ColorCommonPowerTransaction.UNREGISTER_SCREEN_STATUS_LISTENER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        IColorScreenStatusListener listener;
        IColorScreenStatusListener listener2;
        int i = AnonymousClass1.$SwitchMap$android$os$ColorCommonPowerTransaction[getTransaction(code).ordinal()];
        if (i == 1) {
            data.enforceInterface("android.os.IPowerManager");
            if (data.readInt() != 0) {
                listener = IColorScreenStatusListener.Stub.asInterface(data.readStrongBinder());
            } else {
                listener = null;
            }
            registerScreenStatusListener(listener);
            reply.writeNoException();
            return true;
        } else if (i != 2) {
            return false;
        } else {
            data.enforceInterface("android.os.IPowerManager");
            if (data.readInt() != 0) {
                listener2 = IColorScreenStatusListener.Stub.asInterface(data.readStrongBinder());
            } else {
                listener2 = null;
            }
            unregisterScreenStatusListener(listener2);
            reply.writeNoException();
            return true;
        }
    }

    private ColorCommonPowerTransaction getTransaction(int code) {
        int ordinal = code - 10001;
        ColorCommonPowerTransaction[] transactions = ColorCommonPowerTransaction.values();
        if (ordinal < 0 || ordinal >= transactions.length) {
            return ColorCommonPowerTransaction.END;
        }
        return transactions[ordinal];
    }
}
