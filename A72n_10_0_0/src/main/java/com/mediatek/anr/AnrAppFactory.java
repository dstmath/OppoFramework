package com.mediatek.anr;

import dalvik.system.PathClassLoader;

public class AnrAppFactory {
    private static Object lock = new Object();
    private static AnrAppFactory sInstance;

    public static AnrAppFactory getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    try {
                        sInstance = (AnrAppFactory) Class.forName("com.mediatek.anr.AnrAppFactoryImpl", false, new PathClassLoader("/system/framework/mediatek-framework.jar", AnrAppFactory.class.getClassLoader())).getConstructor(new Class[0]).newInstance(new Object[0]);
                    } catch (Exception e) {
                        sInstance = new AnrAppFactory();
                    }
                }
            }
        }
        return sInstance;
    }

    public AnrAppManager makeAnrAppManager() {
        return new AnrAppManager();
    }
}
