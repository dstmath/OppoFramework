package com.oppo.internal.telephony.utils;

import android.content.Context;
import android.telephony.SignalStrength;
import android.util.Log;

public class OppoNecManagerHelper {
    private static final String TAG = "OppoNecManagerHelper";
    private static OppoNecManagerHelper mInstance = new OppoNecManagerHelper();
    private static Object mOppoNecManager = null;

    public static OppoNecManagerHelper getInstance(Context context) {
        if (mOppoNecManager == null) {
            try {
                Class<?> c = Class.forName("com.oppo.nec.OppoNecManager");
                mOppoNecManager = c.getMethod("getInstance", Context.class).invoke(c, context);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return mInstance;
    }

    public void notifyNwDiagnoseInitComplete() {
        Object obj = mOppoNecManager;
        if (obj != null) {
            try {
                obj.getClass().getDeclaredMethod("notifyNwDiagnoseInitComplete", new Class[0]).invoke(mOppoNecManager, new Object[0]);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void broadcastServiceStateChanged(boolean oos, int slotId) {
        Object obj = mOppoNecManager;
        if (obj != null) {
            try {
                obj.getClass().getDeclaredMethod("broadcastServiceStateChanged", Boolean.TYPE, Integer.TYPE).invoke(mOppoNecManager, Boolean.valueOf(oos), Integer.valueOf(slotId));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void notifyOrigSignalStrengthChanged(SignalStrength signalStrength, int slotId) {
        Object obj = mOppoNecManager;
        if (obj != null) {
            try {
                obj.getClass().getDeclaredMethod("notifyOrigSignalStrengthChanged", SignalStrength.class, Integer.TYPE).invoke(mOppoNecManager, signalStrength, Integer.valueOf(slotId));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
