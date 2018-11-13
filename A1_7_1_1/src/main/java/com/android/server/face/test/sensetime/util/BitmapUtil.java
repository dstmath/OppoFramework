package com.android.server.face.test.sensetime.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import com.android.server.face.sensetime.faceapi.utils.ColorConvertUtil;

public class BitmapUtil {
    public static Bitmap cropBitmap(byte[] nv21, Rect rect, int previewWidth, int previewHeight) {
        Rect scaleRect = getScaleRect(rect, 2.0f, 2.0f, previewWidth, previewHeight);
        int cropWidth = scaleRect.width();
        int cropHeight = scaleRect.height();
        int startX = scaleRect.left % 2 == 0 ? scaleRect.left : scaleRect.left + 1;
        int startY = scaleRect.top % 2 == 0 ? scaleRect.top : scaleRect.top + 1;
        if (cropWidth % 2 != 0) {
            cropWidth--;
        }
        if (cropHeight % 2 != 0) {
            cropHeight--;
        }
        return ColorConvertUtil.cropNv21ToBitmap(nv21, previewWidth, previewHeight, startX, startY, cropWidth, cropHeight);
    }

    public static Rect getScaleRect(Rect rect, float scaleX, float scaleY, int maxW, int maxH) {
        Rect resultRect = new Rect();
        int left = (int) (((float) rect.left) - ((((float) rect.width()) * (scaleX - 1.0f)) / 2.0f));
        int right = (int) (((float) rect.right) + ((((float) rect.width()) * (scaleX - 1.0f)) / 2.0f));
        int bottom = (int) (((float) rect.bottom) + ((((float) rect.height()) * (scaleY - 1.0f)) / 2.0f));
        int top = (int) (((float) rect.top) - ((((float) rect.height()) * (scaleY - 1.0f)) / 2.0f));
        if (left <= 0) {
            left = 0;
        }
        resultRect.left = left;
        if (right <= maxW) {
            maxW = right;
        }
        resultRect.right = maxW;
        if (bottom <= maxH) {
            maxH = bottom;
        }
        resultRect.bottom = maxH;
        if (top <= 0) {
            top = 0;
        }
        resultRect.top = top;
        return resultRect;
    }

    public static Bitmap rotateBitmap(Bitmap sourceBitmap, int degree, boolean frontCamera) {
        if (degree == 0 && !frontCamera) {
            return sourceBitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate((float) degree);
        if (frontCamera) {
            matrix.postScale(-1.0f, 1.0f);
        }
        return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, false);
    }
}
