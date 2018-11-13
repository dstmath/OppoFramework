package com.suntek.rcs.ui.common.mms;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageUtils {
    private static int IMAGE_MAX_HEIGHT = 960;
    private static int IMAGE_MAX_WIDTH = 480;

    public static Bitmap getBitmap(String imagePath) {
        Options option = new Options();
        option.inSampleSize = getImageScale(imagePath);
        return BitmapFactory.decodeFile(imagePath, option);
    }

    public static int getImageScale(String imagePath) {
        Options option = new Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, option);
        int scale = 1;
        while (true) {
            if (option.outWidth / scale < IMAGE_MAX_WIDTH && option.outHeight / scale < IMAGE_MAX_HEIGHT) {
                return scale;
            }
            scale *= 2;
        }
    }

    public static Drawable getRoundCornerDrawable(Resources resources, int resId) {
        Bitmap bitmapRes = BitmapFactory.decodeResource(resources, resId);
        return new BitmapDrawable(createBitmapRoundCorner(bitmapRes, bitmapRes.getWidth() / 2));
    }

    private static Bitmap createBitmapRoundCorner(Bitmap bitmap, int roundCornerPixels) {
        if (bitmap == null) {
            return Bitmap.createBitmap(10, 10, Config.ARGB_8888);
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        float roundPx = (float) roundCornerPixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-12434878);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
