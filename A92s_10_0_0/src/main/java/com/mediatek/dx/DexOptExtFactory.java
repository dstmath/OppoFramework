package com.mediatek.dx;

import dalvik.system.PathClassLoader;

public class DexOptExtFactory {
    private static Object lock = new Object();
    private static DexOptExtFactory sInstance;

    public static DexOptExtFactory getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    try {
                        sInstance = (DexOptExtFactory) Class.forName("com.mediatek.server.dx.DexOptExtFactoryImpl", false, new PathClassLoader("/system/framework/mediatek-services.jar", DexOptExtFactory.class.getClassLoader())).getConstructor(new Class[0]).newInstance(new Object[0]);
                    } catch (Exception e) {
                        sInstance = new DexOptExtFactory();
                    }
                }
            }
        }
        return sInstance;
    }

    public DexOptExt makeDexOpExt() {
        return new DexOptExt();
    }
}
