package com.mediatek.android.mms.pdu;

import android.util.Log;
import com.google.android.mms.pdu.EncodedStringValue;
import java.io.UnsupportedEncodingException;

public class MtkEncodedStringValue extends EncodedStringValue {
    private static final String TAG = "MtkEncodingStringValue";

    public MtkEncodedStringValue(int charset, String data) {
        super(data);
        try {
            this.mData = data.getBytes(MtkCharacterSets.getMimeName(charset));
            this.mCharacterSet = charset;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Default encoding must be supported.", e);
        }
    }

    public MtkEncodedStringValue(int charset, byte[] data) {
        super(charset, data);
    }

    public MtkEncodedStringValue(byte[] data) {
        super(data);
    }

    public MtkEncodedStringValue(String data) {
        super(data);
    }
}
