package android.text.format;

import android.content.Context;
import android.content.res.Resources;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.proto.ProtoOutputStream;
import com.color.util.ColorFormatterCompatibilityUtils;

public final class ColorFormatter {
    public static final int FLAG_CALCULATE_ROUNDED = 2;
    public static final int FLAG_SHORTER = 1;

    public static class BytesResult {
        public final long roundedBytes;
        public final String units;
        public final String value;

        public BytesResult(String value, String units, long roundedBytes) {
            this.value = value;
            this.units = units;
            this.roundedBytes = roundedBytes;
        }
    }

    private static String bidiWrap(Context context, String source) {
        if (TextUtils.getLayoutDirectionFromLocale(context.getResources().getConfiguration().locale) == 1) {
            return BidiFormatter.getInstance(true).unicodeWrap(source);
        }
        return source;
    }

    public static String formatFileSize(Context context, long sizeBytes) {
        if (context == null) {
            return "";
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, 0);
        return bidiWrap(context, context.getString(201589913, new Object[]{res.value, res.units}));
    }

    public static String formatFileSize(Context context, long sizeBytes, int flags) {
        if (context == null) {
            return "";
        }
        if (ColorFormatterCompatibilityUtils.getInstance().getOptimizationData().getEnablePolicy() == 1) {
            return "";
        }
        String packagename = context.getPackageName();
        if (TextUtils.isEmpty(packagename)) {
            return "";
        }
        if (!ColorFormatterCompatibilityUtils.getInstance().inBlackPkgList(packagename)) {
            return "";
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, flags);
        return bidiWrap(context, context.getString(201589913, new Object[]{res.value, res.units}));
    }

    public static String formatShortFileSize(Context context, long sizeBytes) {
        if (context == null) {
            return "";
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, 1);
        return bidiWrap(context, context.getString(201589913, new Object[]{res.value, res.units}));
    }

    public static BytesResult formatBytes(Resources res, long sizeBytes, int flags) {
        int roundFactor;
        String roundFormat;
        long roundedBytes;
        boolean isNegative = sizeBytes < 0;
        if (isNegative) {
            sizeBytes = -sizeBytes;
        }
        float result = (float) sizeBytes;
        int suffix = 201589907;
        long mult = 1;
        if (result > 900.0f) {
            suffix = 201589908;
            mult = 1024;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 201589909;
            mult = 1048576;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 201589910;
            mult = 1073741824;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 201589911;
            mult = ProtoOutputStream.FIELD_COUNT_SINGLE;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = 201589912;
            mult = 1125899906842624L;
            result /= 1024.0f;
        }
        if (mult == 1 || result >= 100.0f) {
            roundFactor = 1;
            roundFormat = "%.0f";
        } else if (result < 1.0f) {
            roundFactor = 100;
            roundFormat = "%.2f";
        } else if (result < 10.0f) {
            if ((flags & 1) != 0) {
                roundFactor = 10;
                roundFormat = "%.1f";
            } else {
                roundFactor = 100;
                roundFormat = "%.2f";
            }
        } else if ((flags & 1) != 0) {
            roundFactor = 1;
            roundFormat = "%.0f";
        } else {
            roundFactor = 100;
            roundFormat = "%.2f";
        }
        if (isNegative) {
            result = -result;
        }
        String roundedString = String.format(roundFormat, new Object[]{Float.valueOf(result)});
        if ((flags & 2) == 0) {
            roundedBytes = 0;
        } else {
            roundedBytes = (((long) Math.round(((float) roundFactor) * result)) * mult) / ((long) roundFactor);
        }
        return new BytesResult(roundedString, res.getString(suffix), roundedBytes);
    }
}
