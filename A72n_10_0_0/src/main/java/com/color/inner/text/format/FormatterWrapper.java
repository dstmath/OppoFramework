package com.color.inner.text.format;

import android.content.res.Resources;
import android.text.format.Formatter;

public class FormatterWrapper {
    public static final int FLAG_CALCULATE_ROUNDED = 2;
    public static final int FLAG_IEC_UNITS = 8;
    public static final int FLAG_SHORTER = 1;
    public static final int FLAG_SI_UNITS = 4;

    public static class BytesResultWrapper {
        private Formatter.BytesResult mBytesResult;

        private BytesResultWrapper(Formatter.BytesResult bytesResult) {
            this.mBytesResult = bytesResult;
        }

        public String getValue() {
            return this.mBytesResult.value;
        }

        public String getUnits() {
            return this.mBytesResult.units;
        }

        public long getRoundedBytes() {
            return this.mBytesResult.roundedBytes;
        }
    }

    public static BytesResultWrapper formatBytes(Resources res, long sizeBytes, int flags) {
        return new BytesResultWrapper(Formatter.formatBytes(res, sizeBytes, flags));
    }
}
