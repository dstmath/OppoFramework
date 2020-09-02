package com.color.inner.security;

import android.security.IOppoKeyStoreEx;
import android.security.KeyStore;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class KeyStoreWrapper {
    private static final String TAG = "KeyStoreWrapper";

    private KeyStoreWrapper() {
    }

    public static byte[] getGateKeeperAuthToken() {
        try {
            IOppoKeyStoreEx iKeyStoreWrapper = typeCasting(KeyStore.getInstance());
            if (iKeyStoreWrapper != null) {
                return iKeyStoreWrapper.getGateKeeperAuthToken();
            }
            return null;
        } catch (Exception e) {
            Log.w(TAG, "getGateKeeperAuthToken failed", e);
            return null;
        }
    }

    private static IOppoKeyStoreEx typeCasting(KeyStore keyStore) {
        return (IOppoKeyStoreEx) ColorTypeCastingHelper.typeCasting(IOppoKeyStoreEx.class, keyStore);
    }
}
