package com.android.server.face.sensetime.faceapi.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.android.server.face.utils.LogUtil;
import com.sensetime.faceapi.FaceLibrary;
import java.nio.ByteBuffer;

public class ColorConvertUtil {
    public static final String TAG = "FaceService.ColorConvertUtil";

    public static void getBGRFromBitmap(Bitmap bitmap, byte[] bgr) {
        if (bitmap == null || bitmap.isRecycled() || bgr == null) {
            LogUtil.e(TAG, "bitmap or bgr is null !!!");
            return;
        }
        if (bitmap.getConfig() != Config.ARGB_8888) {
            bitmap = bitmap.copy(Config.ARGB_8888, false);
        }
        FaceLibrary.getBGRFromBitmap(bitmap, bgr);
    }

    public static Bitmap cropNv21ToBitmap(byte[] nv21, int imageWidth, int imageHeight, int startX, int startY, int cropWidth, int cropHeight) {
        if (nv21 == null || startX < 0 || startY < 0 || cropWidth < 0 || cropHeight < 0 || imageWidth < startX + cropWidth || imageHeight < startY + cropHeight) {
            LogUtil.e(TAG, "cropNv21ToBitmap failed: illegal para !");
            return null;
        }
        byte[] argb = new byte[((cropWidth * cropHeight) * 4)];
        cropNv21DataToARGB(nv21, imageWidth, imageHeight, startX, startY, cropWidth, cropHeight, argb);
        return byteArrayToBitmap(argb, cropWidth, cropHeight);
    }

    public static void cropNv21DataToARGB(byte[] nv21, int width, int height, int x, int y, int w, int h, byte[] argb) {
        if (nv21 == null || argb == null) {
            LogUtil.e(TAG, "cropNv21DataToARGB failed: nv21 or argb is null ");
        } else {
            FaceLibrary.cropNv21Data(nv21, width, height, x, y, w, h, argb);
        }
    }

    public static Bitmap byteArrayToBitmap(byte[] argb, int width, int height) {
        ByteBuffer buffer = ByteBuffer.wrap(argb);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }
}
