package com.color.util;

public final class ColorTypeCastingHelper {
    public static <T> T typeCasting(Class<T> type, Object object) {
        if (object == null || !type.isInstance(object)) {
            return null;
        }
        return object;
    }
}
