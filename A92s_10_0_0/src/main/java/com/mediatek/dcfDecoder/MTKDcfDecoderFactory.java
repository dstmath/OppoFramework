package com.mediatek.dcfDecoder;

import android.util.Log;

public class MTKDcfDecoderFactory {
    private static final String TAG = "MTKDcfDecoderFactory";
    private static MTKDcfDecoderFactory sInstance = null;

    public static MTKDcfDecoderFactory getInstance() {
        if (sInstance == null) {
            try {
                sInstance = (MTKDcfDecoderFactory) Class.forName("com.mediatek.dcfDecoder.MTKDcfDecoderFactoryImpl").getConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (Exception e) {
                sInstance = new MTKDcfDecoderFactory();
                Log.d(TAG, "Unable to load MTK Dcf Decoder and Exception " + e);
            }
        }
        return sInstance;
    }

    public MTKDcfDecoderManager makeMTKDcfDecoderManager() {
        return new MTKDcfDecoderManager();
    }
}
