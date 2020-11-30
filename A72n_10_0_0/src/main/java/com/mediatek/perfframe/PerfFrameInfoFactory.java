package com.mediatek.perfframe;

import dalvik.system.PathClassLoader;

public class PerfFrameInfoFactory {
    private static Object lock = new Object();
    private static PerfFrameInfoFactory sInstance;

    public static PerfFrameInfoFactory getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    try {
                        sInstance = (PerfFrameInfoFactory) Class.forName("com.mediatek.perfframe.PerfFrameInfoFactoryImpl", false, new PathClassLoader("/system/framework/mediatek-framework.jar", PerfFrameInfoFactory.class.getClassLoader())).getConstructor(new Class[0]).newInstance(new Object[0]);
                    } catch (Exception e) {
                        sInstance = new PerfFrameInfoFactory();
                    }
                }
            }
        }
        return sInstance;
    }

    public PerfFrameInfoManager makePerfFrameInfoManager() {
        return new PerfFrameInfoManager();
    }
}
