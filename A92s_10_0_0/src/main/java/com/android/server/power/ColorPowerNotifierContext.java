package com.android.server.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.color.os.IColorScreenStatusListener;
import com.color.util.ColorBaseServiceManager;
import com.color.util.ColorLog;
import java.io.PrintWriter;

public class ColorPowerNotifierContext extends ContextWrapper {
    public static final boolean DBG = ColorBaseServiceManager.DBG;
    public static final String TAG = "ColorPowerNotifierContext";
    private final ColorScreenStatusObservers mScreenStatusObservers;

    public ColorPowerNotifierContext(Context base) {
        super(base);
        this.mScreenStatusObservers = new ColorScreenStatusObservers(base);
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        super.sendOrderedBroadcastAsUser(intent, user, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
        if (intent != null) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                notifyScreenOff();
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                notifyScreenOn();
            }
        }
    }

    public static ColorPowerNotifierContext from(Context context) {
        if (context instanceof ColorPowerNotifierContext) {
            return (ColorPowerNotifierContext) context;
        }
        return null;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("callbacks: ");
        pw.println(this.mScreenStatusObservers.getRegisteredCallbackCount());
    }

    public void registerScreenStatusListener(IColorScreenStatusListener listener) {
        if (listener != null) {
            this.mScreenStatusObservers.register(listener);
        }
    }

    public void unregisterScreenStatusListener(IColorScreenStatusListener listener) {
        if (listener != null) {
            this.mScreenStatusObservers.unregister(listener);
        }
    }

    private void notifyScreenOff() {
        try {
            int size = this.mScreenStatusObservers.beginBroadcast();
            for (int i = 0; i < size; i++) {
                ColorScreenStatusObserver observer = (ColorScreenStatusObserver) this.mScreenStatusObservers.getBroadcastItem(i);
                String receiver = observer.getCaller();
                try {
                    boolean z = DBG;
                    ColorLog.d(z, "ColorPowerNotifierContext", receiver + " : notifyScreenOff");
                    observer.onScreenOff();
                } catch (RemoteException e) {
                    boolean z2 = DBG;
                    ColorLog.e(z2, "ColorPowerNotifierContext", receiver + " : notifyScreenOff : " + e.toString());
                } catch (Exception e2) {
                    boolean z3 = DBG;
                    ColorLog.e(z3, "ColorPowerNotifierContext", receiver + " : \n" + Log.getStackTraceString(e2));
                }
            }
        } catch (Exception e3) {
            ColorLog.e(DBG, "ColorPowerNotifierContext", "ERROR notifyScreenOff : ", e3);
        } catch (Throwable th) {
            this.mScreenStatusObservers.finishBroadcast();
            throw th;
        }
        this.mScreenStatusObservers.finishBroadcast();
    }

    private void notifyScreenOn() {
        try {
            int size = this.mScreenStatusObservers.beginBroadcast();
            for (int i = 0; i < size; i++) {
                ColorScreenStatusObserver observer = (ColorScreenStatusObserver) this.mScreenStatusObservers.getBroadcastItem(i);
                String receiver = observer.getCaller();
                try {
                    boolean z = DBG;
                    ColorLog.d(z, "ColorPowerNotifierContext", receiver + " : notifyScreenOn");
                    observer.onScreenOn();
                } catch (RemoteException e) {
                    boolean z2 = DBG;
                    ColorLog.e(z2, "ColorPowerNotifierContext", receiver + " : notifyScreenOn : " + e.toString());
                } catch (Exception e2) {
                    boolean z3 = DBG;
                    ColorLog.e(z3, "ColorPowerNotifierContext", receiver + " : \n" + Log.getStackTraceString(e2));
                }
            }
        } catch (Exception e3) {
            ColorLog.e(DBG, "ColorPowerNotifierContext", "ERROR notifyScreenOn : ", e3);
        } catch (Throwable th) {
            this.mScreenStatusObservers.finishBroadcast();
            throw th;
        }
        this.mScreenStatusObservers.finishBroadcast();
    }
}
