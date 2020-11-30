package android.graphics;

import com.color.util.ColorTypeCastingHelper;

public class OppoBaseBitmapFactory {
    protected static void setAssetSourceAndHasCalculatedColor(Bitmap bm, boolean isAssetSource, boolean hasCalculatedColor) {
        OppoBaseBitmap baseBitmap;
        if (bm != null && (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bm)) != null) {
            baseBitmap.setIsAssetSource(isAssetSource);
            baseBitmap.setHasCalculatedColor(hasCalculatedColor);
        }
    }

    protected static void setIsAssetSource(Bitmap bm, boolean value) {
        OppoBaseBitmap baseBitmap;
        if (bm != null && (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bm)) != null) {
            baseBitmap.setIsAssetSource(value);
        }
    }

    protected static void setHasCalculatedColor(Bitmap bm, boolean value) {
        OppoBaseBitmap baseBitmap;
        if (bm != null && (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bm)) != null) {
            baseBitmap.setHasCalculatedColor(value);
        }
    }
}
