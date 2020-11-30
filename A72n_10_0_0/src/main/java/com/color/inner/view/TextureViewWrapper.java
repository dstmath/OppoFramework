package com.color.inner.view;

import android.util.Log;
import android.view.OppoBaseTextureView;
import android.view.TextureView;
import com.color.util.ColorTypeCastingHelper;

public class TextureViewWrapper {
    private static final String TAG = "TextureViewWrapper";

    public static void setCallBackSizeChangeWhenLayerUpdate(TextureView view, boolean doCallBack) {
        try {
            typeCasting(view).setCallBackSizeChangeWhenLayerUpdate(doCallBack);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private static OppoBaseTextureView typeCasting(TextureView textureView) {
        return (OppoBaseTextureView) ColorTypeCastingHelper.typeCasting(OppoBaseTextureView.class, textureView);
    }
}
