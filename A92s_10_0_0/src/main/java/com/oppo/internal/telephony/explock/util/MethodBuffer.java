package com.oppo.internal.telephony.explock.util;

import android.telephony.Rlog;
import android.text.TextUtils;
import java.util.Arrays;

public class MethodBuffer {
    private static final int BUFFER_SIZE = 10000;
    private static final String TAG = "MethodBuffer";
    private byte[] mBuffer = null;
    private MethodType mMethodType = null;
    private int mParamNum = 0;

    private MethodBuffer() {
    }

    MethodBuffer(MethodType methodType) {
        this.mMethodType = methodType;
    }

    public int getParamNum() {
        return this.mParamNum;
    }

    /* access modifiers changed from: package-private */
    public MethodBuffer appendStringParam(MethodParamType methodParamType, String param) {
        if (methodParamType != null && !TextUtils.isEmpty(param)) {
            appendBytes(getParamBuffer(methodParamType, Util.getUTF8Bytes(param)));
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public MethodBuffer appendIntParam(MethodParamType methodParamType, int param) {
        appendBytes(getParamBuffer(methodParamType, Util.intToByteArray(param)));
        return this;
    }

    public MethodBuffer appendBooleanParam(MethodParamType methodParamType, boolean param) {
        appendBytes(getParamBuffer(methodParamType, Util.booleanToByteArray(param)));
        return this;
    }

    /* access modifiers changed from: package-private */
    public MethodBuffer appendBytesParam(MethodParamType methodParamType, byte[] param) {
        appendBytes(getParamBuffer(methodParamType, param));
        return this;
    }

    /* access modifiers changed from: package-private */
    public byte[] buildBuffer() {
        MethodType methodType = this.mMethodType;
        if (methodType != null) {
            return concat(concat(Util.intToByteArray(methodType.getCode()), Util.intToByteArray(10000)), Util.intToByteArray(this.mParamNum), this.mBuffer);
        }
        return null;
    }

    private MethodBuffer appendBytes(byte[] bytes) {
        if (!Util.isArrayEmpty(bytes)) {
            byte[] bArr = this.mBuffer;
            if (bArr == null) {
                this.mBuffer = bytes;
            } else {
                this.mBuffer = concat(bArr, bytes);
            }
            this.mParamNum++;
        } else {
            Rlog.e(TAG, "appendBytes bytes is empty");
        }
        return this;
    }

    private static byte[] getParamBuffer(MethodParamType methodParamType, byte[] paramBuffer) {
        byte[] buffer = null;
        if (methodParamType != null) {
            try {
                if (!Util.isArrayEmpty(paramBuffer)) {
                    byte[] paramTypeBuffer = Util.intToByteArray(methodParamType.getCode());
                    byte[] paramLenBuffer = Util.intToByteArray(paramBuffer.length);
                    if (Util.isArrayEmpty(paramTypeBuffer) || Util.isArrayEmpty(paramBuffer) || Util.isArrayEmpty(paramLenBuffer)) {
                        Rlog.e(TAG, "getParamBuffer at least one array is empty");
                    } else {
                        buffer = concat(paramTypeBuffer, paramLenBuffer, paramBuffer);
                    }
                    return buffer;
                }
            } catch (Exception e) {
                Rlog.e(TAG, "getParamBuffer e = " + e);
            }
        }
        Rlog.e(TAG, "getParamBuffer methodParamType = " + methodParamType + ", paramBuffer = " + paramBuffer);
        return buffer;
    }

    private static byte[] concat(byte[] first, byte[] second) {
        if (first == null || first.length == 0) {
            return second;
        }
        if (second == null || second.length == 0) {
            return first;
        }
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static byte[] concat(byte[] first, byte[] second, byte[] third) {
        return concat(concat(first, second), third);
    }
}
