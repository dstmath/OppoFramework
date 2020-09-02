package com.android.internal.telephony;

import android.util.Log;
import com.android.internal.telephony.common.IOppoCommonFactory;
import com.android.internal.telephony.common.OppoFeatureList;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;

public class OppoTelephonyFactory implements IOppoCommonFactory {
    protected static final String TAG = "OppoTelephonyFactory";
    private static final Object mLock = new Object();
    private static OppoTelephonyFactory sInstance;

    public static OppoTelephonyFactory getInstance() {
        OppoTelephonyFactory oppoTelephonyFactory = sInstance;
        if (oppoTelephonyFactory != null) {
            return oppoTelephonyFactory;
        }
        synchronized (mLock) {
            if (sInstance == null) {
                try {
                    Class<?> cls = Class.forName("com.oppo.internal.telephony.OppoTelephonyFactoryImpl", false, new PathClassLoader("/system/framework/oppo-telephony-common.jar", ClassLoader.getSystemClassLoader()));
                    Log.d(TAG, "cls = " + cls);
                    Constructor custMethod = cls.getConstructor(new Class[0]);
                    Log.d(TAG, "constructor method = " + custMethod);
                    sInstance = (OppoTelephonyFactory) custMethod.newInstance(new Object[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Reflect exception getInstance: " + e.toString());
                    sInstance = new OppoTelephonyFactory();
                }
            }
        }
        return sInstance;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFactory
    public boolean isValid(int index) {
        boolean valid = index < OppoFeatureList.OppoIndex.End.ordinal() && index > OppoFeatureList.OppoIndex.Start.ordinal();
        Log.i(TAG, "isValid = " + valid + "index = " + index);
        return valid;
    }
}
