package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import java.io.ByteArrayOutputStream;

public abstract class ResponseData {
    @UnsupportedAppUsage
    public abstract void format(ByteArrayOutputStream byteArrayOutputStream);

    public static void writeLength(ByteArrayOutputStream buf, int length) {
        if (length > 127) {
            buf.write(129);
        }
        buf.write(length);
    }
}
