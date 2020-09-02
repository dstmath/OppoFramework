package com.mediatek.cta;

import android.util.Log;
import dalvik.system.PathClassLoader;

public class CtaManagerFactory {
    private static final String TAG = "CtaManagerFactory";
    public static PathClassLoader sClassLoader;
    protected static CtaManager sCtaManager = null;
    private static CtaManagerFactory sInstance = null;

    public static CtaManagerFactory getInstance() {
        CtaManagerFactory ctaManagerFactory;
        CtaManagerFactory ctaManagerFactory2 = sInstance;
        if (ctaManagerFactory2 != null) {
            return ctaManagerFactory2;
        }
        try {
            sClassLoader = new PathClassLoader("/system/framework/mediatek-cta.jar", CtaManagerFactory.class.getClassLoader());
            sInstance = (CtaManagerFactory) Class.forName("com.mediatek.cta.CtaManagerFactoryImpl", false, sClassLoader).getConstructor(new Class[0]).newInstance(new Object[0]);
            if (sInstance == null) {
                ctaManagerFactory = new CtaManagerFactory();
                sInstance = ctaManagerFactory;
            }
        } catch (Exception e) {
            Log.w(TAG, "CtaManagerFactoryImpl not found");
            if (sInstance == null) {
                ctaManagerFactory = new CtaManagerFactory();
            }
        } catch (Throwable th) {
            if (sInstance == null) {
                ctaManagerFactory = new CtaManagerFactory();
            }
        }
        return sInstance;
    }

    public CtaManager makeCtaManager() {
        if (sCtaManager == null) {
            sCtaManager = new CtaManager();
        }
        return sCtaManager;
    }
}
