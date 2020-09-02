package com.android.server;

import java.util.HashMap;

public final class ColorCustomManagerRegistry {
    private static final HashMap<String, Object> sColorCustomManagerMap = new HashMap<>();

    private ColorCustomManagerRegistry() {
    }

    public static boolean registerColorCustomManager(String name, Object colormanager) {
        synchronized (sColorCustomManagerMap) {
            if (sColorCustomManagerMap.get(name) != null) {
                return false;
            }
            sColorCustomManagerMap.put(name, colormanager);
            return true;
        }
    }

    public static Object getColorCustomManager(String name) {
        Object obj;
        synchronized (sColorCustomManagerMap) {
            obj = sColorCustomManagerMap.get(name);
        }
        return obj;
    }

    public void dump() {
    }
}
