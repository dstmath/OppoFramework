package com.color.inner.nfc;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.util.Log;

public class NfcAdapterWrapper {
    private static final String TAG = "NfcAdapterWrapper";

    private NfcAdapterWrapper() {
    }

    public static NfcAdapter getNfcAdapter(Context context) {
        return NfcAdapter.getNfcAdapter(context);
    }

    public static boolean disable(NfcAdapter nfcAdapter) {
        return nfcAdapter.disable();
    }

    public static boolean disable(NfcAdapter nfcAdapter, boolean persist) {
        return nfcAdapter.disable(persist);
    }

    public static boolean enable(NfcAdapter nfcAdapter) {
        return nfcAdapter.enable();
    }

    public static boolean setABFListenTechMask(NfcAdapter nfcAdapter, int techMask) {
        try {
            return ((Boolean) NfcAdapter.class.getDeclaredMethod("setABFListenTechMask", Integer.TYPE).invoke(nfcAdapter, Integer.valueOf(techMask))).booleanValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }
}
