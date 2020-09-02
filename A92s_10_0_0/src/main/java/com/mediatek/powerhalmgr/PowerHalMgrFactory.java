package com.mediatek.powerhalmgr;

import dalvik.system.PathClassLoader;

public class PowerHalMgrFactory {
    private static Object lock = new Object();
    private static PowerHalMgrFactory sInstance;

    public static PowerHalMgrFactory getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    try {
                        sInstance = (PowerHalMgrFactory) Class.forName("com.mediatek.powerhalmgr.PowerHalMgrFactoryImpl", false, new PathClassLoader("/system/framework/mediatek-framework.jar", PowerHalMgrFactory.class.getClassLoader())).getConstructor(new Class[0]).newInstance(new Object[0]);
                    } catch (Exception e) {
                        sInstance = new PowerHalMgrFactory();
                    }
                }
            }
        }
        return sInstance;
    }

    public PowerHalMgr makePowerHalMgr() {
        return new PowerHalMgr();
    }
}
