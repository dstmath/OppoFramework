package com.color.inner.graphics;

import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapFactoryWrapper {
    private static final String TAG = "BitmapFactoryWrapper";

    public static class OptionsWrapper {
        public static boolean getInPostProc(BitmapFactory.Options options) {
            try {
                return ((Boolean) options.getClass().getField("inPostProc").get(options)).booleanValue();
            } catch (Exception e) {
                Log.e(BitmapFactoryWrapper.TAG, e.toString());
                return false;
            }
        }

        public static void setInPostProc(BitmapFactory.Options options, boolean inPostProc) {
            try {
                options.getClass().getField("inPostProc").set(options, Boolean.valueOf(inPostProc));
            } catch (Exception e) {
                Log.e(BitmapFactoryWrapper.TAG, e.toString());
            }
        }
    }
}
