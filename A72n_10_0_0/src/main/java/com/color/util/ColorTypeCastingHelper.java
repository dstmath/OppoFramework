package com.color.util;

public final class ColorTypeCastingHelper {
    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public static <T> T typeCasting(Class<T> type, Object object) {
        if (object == 0 || !type.isInstance(object)) {
            return null;
        }
        return object;
    }
}
