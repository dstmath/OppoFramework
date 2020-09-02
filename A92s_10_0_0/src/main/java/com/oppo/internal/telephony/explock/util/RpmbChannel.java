package com.oppo.internal.telephony.explock.util;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class RpmbChannel {
    private static final String CRYPTOENG_CLASS = "vendor.oppo.hardware.cryptoeng.V1_0.ICryptoeng";
    private static final String TAG = "RpmbChannel";
    private static volatile RpmbChannel sInstance = null;

    public static RpmbChannel getInstance() {
        if (sInstance == null) {
            synchronized (RpmbChannel.class) {
                if (sInstance == null) {
                    sInstance = new RpmbChannel();
                }
            }
        }
        return sInstance;
    }

    private Object getCryptoSerice() {
        try {
            Class<?> c = Class.forName(CRYPTOENG_CLASS);
            return c.getMethod("getService", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] processCmdV2(byte[] param) {
        int size;
        byte[] receiveBuffer = null;
        ArrayList<Byte> result = null;
        try {
            ArrayList<Byte> data = new ArrayList<>();
            for (byte b : param) {
                data.add(Byte.valueOf(b));
            }
            Method get = Class.forName(CRYPTOENG_CLASS).getMethod("cryptoeng_invoke_command", ArrayList.class);
            Object cryptoSerice = getCryptoSerice();
            if (cryptoSerice != null) {
                result = (ArrayList) get.invoke(cryptoSerice, data);
            }
            if (result != null && (size = result.size()) > 0) {
                receiveBuffer = new byte[size];
                for (int i = 0; i < size; i++) {
                    receiveBuffer[i] = result.get(i).byteValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receiveBuffer;
    }
}
