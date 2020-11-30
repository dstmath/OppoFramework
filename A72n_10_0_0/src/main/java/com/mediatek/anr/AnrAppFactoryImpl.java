package com.mediatek.anr;

public class AnrAppFactoryImpl extends AnrAppFactory {
    public AnrAppManager makeAnrAppManager() {
        return new AnrAppManagerImpl();
    }
}
