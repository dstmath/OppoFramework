package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;

public class ColorMagnifierHooksImpl implements IColorMagnifierHooks {
    private static Bitmap mShadowBitmap;

    public int getMagnifierWidth(TypedArray a, Context context) {
        return (int) context.getResources().getDimension(201655801);
    }

    public int getMagnifierHeight(TypedArray a, Context context) {
        return (int) context.getResources().getDimension(201655802);
    }

    public float getMagnifierCornerRadius(TypedArray a, Context context) {
        return context.getResources().getDimension(201655800);
    }

    public void decodeShadowBitmap(Context context) {
        mShadowBitmap = BitmapFactory.decodeResource(context.getResources(), 201852312);
    }

    public void recycleShadowBitmap() {
        Bitmap bitmap = mShadowBitmap;
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    public void drawShadowBitmap(int contentWidth, int contentHeight, RecordingCanvas canvas, Paint paint) {
        canvas.drawBitmap(mShadowBitmap, new Rect(0, 0, mShadowBitmap.getWidth(), mShadowBitmap.getHeight()), new Rect(0, 0, contentWidth, contentHeight), paint);
    }
}
