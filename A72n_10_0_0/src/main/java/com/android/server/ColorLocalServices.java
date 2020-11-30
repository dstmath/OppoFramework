package com.android.server;

import android.util.ArrayMap;

public final class ColorLocalServices {
    private static final ArrayMap<Class<?>, Object> sLocalServiceObjects = new ArrayMap<>();

    private ColorLocalServices() {
    }

    public static <T> T getService(Class<T> type) {
        T t;
        synchronized (sLocalServiceObjects) {
            t = (T) sLocalServiceObjects.get(type);
        }
        return t;
    }

    public static <T> void addService(Class<T> type, T service) {
        synchronized (sLocalServiceObjects) {
            if (!sLocalServiceObjects.containsKey(type)) {
                sLocalServiceObjects.put(type, service);
            } else {
                throw new IllegalStateException("Overriding service registration");
            }
        }
    }
}
