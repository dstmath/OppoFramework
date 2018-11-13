package com.oppo.theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import java.io.InputStream;

public class OppoMaskBitmapUtilities {
    private static final String TAG = "MaskBitmapUtilities";
    private static OppoMaskBitmapUtilities mMaskBitmapUtilities = null;
    private Options mOpt;

    private static final native void nativeInit();

    public native Bitmap cutAndScaleBitmap(Bitmap bitmap);

    public native void releaseResouce();

    public native Bitmap scaleAndMaskBitmap(Context context, int i);

    public native Bitmap scaleAndMaskBitmap(Bitmap bitmap);

    public native void setCutAndScalePram(int i, int i2);

    public native void setMaskBitmap(Context context, int i);

    public native void setMaskBitmap(Bitmap bitmap);

    static {
        System.loadLibrary("oppographic");
        nativeInit();
    }

    private OppoMaskBitmapUtilities() {
    }

    public static synchronized OppoMaskBitmapUtilities getInstance() {
        OppoMaskBitmapUtilities oppoMaskBitmapUtilities;
        synchronized (OppoMaskBitmapUtilities.class) {
            if (mMaskBitmapUtilities == null) {
                mMaskBitmapUtilities = new OppoMaskBitmapUtilities();
            }
            oppoMaskBitmapUtilities = mMaskBitmapUtilities;
        }
        return oppoMaskBitmapUtilities;
    }

    private Bitmap readBitmap(Context context, int id) {
        this.mOpt = new Options();
        this.mOpt.inPreferredConfig = Config.ARGB_8888;
        this.mOpt.inMutable = true;
        InputStream is = context.getResources().openRawResource(id);
        Bitmap b = BitmapFactory.decodeStream(is, null, this.mOpt);
        try {
            is.close();
        } catch (Exception e) {
        }
        return b;
    }
}
