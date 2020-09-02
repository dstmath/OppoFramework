package com.color.multiapp;

import android.util.Log;

public class ColorMultiAppFactory extends BaseColorCommonAppFactory {
    private static final String CLASSNAME = "com.color.multiapp.ColorMultiAppFactoryImpl";
    private static final String TAG = "ColorMultiAppFactory";
    private static ColorMultiAppFactory sInstance;

    public static ColorMultiAppFactory getInstance() {
        if (sInstance == null) {
            synchronized (ColorMultiAppFactory.class) {
                try {
                    if (sInstance == null) {
                        sInstance = (ColorMultiAppFactory) newInstance(CLASSNAME);
                    }
                } catch (Exception e) {
                    Log.e(TAG, " Reflect exception getInstance: " + e.toString());
                    if (sInstance == null) {
                        sInstance = new ColorMultiAppFactory();
                    }
                }
            }
        }
        return sInstance;
    }

    @Override // com.color.multiapp.BaseColorCommonAppFactory
    public IColorMultiApp getColorMultiApp() {
        Log.d(TAG, "getColorMultiApp use ColorMultiAppDummy " + Log.getStackTraceString(new Exception()));
        return new ColorMultiAppDummy();
    }
}
