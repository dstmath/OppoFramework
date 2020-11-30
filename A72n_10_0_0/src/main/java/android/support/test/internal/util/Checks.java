package android.support.test.internal.util;

import android.os.Looper;

public final class Checks {
    public static <T> T checkNotNull(T reference) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException();
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference != null) {
            return reference;
        }
        throw new NullPointerException(String.valueOf(errorMessage));
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public static void checkState(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static void checkNotMainThread() {
        checkState(!Thread.currentThread().equals(Looper.getMainLooper().getThread()), "Method cannot be called on the main application thread (on: %s)", Thread.currentThread().getName());
    }

    private static String format(String template, Object... args) {
        int placeholderStart;
        String template2 = String.valueOf(template);
        StringBuilder builder = new StringBuilder(template2.length() + (16 * args.length));
        int templateStart = 0;
        int i = 0;
        while (i < args.length && (placeholderStart = template2.indexOf("%s", templateStart)) != -1) {
            builder.append(template2.substring(templateStart, placeholderStart));
            builder.append(args[i]);
            templateStart = placeholderStart + 2;
            i++;
        }
        builder.append(template2.substring(templateStart));
        if (i < args.length) {
            builder.append(" [");
            int i2 = i + 1;
            builder.append(args[i]);
            while (i2 < args.length) {
                builder.append(", ");
                i2++;
                builder.append(args[i2]);
            }
            builder.append(']');
        }
        return builder.toString();
    }
}
