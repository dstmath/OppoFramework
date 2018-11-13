package com.android.server.face.sensetime.faceapi;

import android.graphics.Rect;
import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.android.server.face.sensetime.faceapi.model.FaceOrientation;
import com.android.server.face.utils.LogUtil;
import com.sensetime.faceapi.FaceLibrary;

public class FaceOcular extends FaceHandleBase {
    private static final String TAG = "FaceService.FaceOcular";
    private float[] mScore;
    private float threshold;

    public FaceOcular() {
        this.mScore = new float[2];
        this.threshold = 0.08f;
        this.mCvFaceHandle = FaceLibrary.cvFaceCreateOcular(null);
        LogUtil.d(TAG, "isHandleInitialized = " + isHandleInitialized());
    }

    public boolean checkOcular(byte[] colorImage, CvPixelFormat cvPixelFormat, int imageWidth, int imageHeight, int imageStride, Rect rect, FaceOrientation orientation) {
        FaceLibrary.cvFaceCheckOcular(this.mCvFaceHandle, colorImage, cvPixelFormat.getValue(), imageWidth, imageHeight, imageStride, orientation.getValue(), rect, this.mScore, this.mResultCode);
        checkResultCode();
        for (int i = 0; i < this.mScore.length; i++) {
            LogUtil.d(TAG, "mScore[" + i + "]: " + this.mScore[i]);
            if (this.mScore[i] > this.threshold) {
                return true;
            }
        }
        return false;
    }

    protected void releaseHandle() {
        FaceLibrary.cvFaceDestroyOcular(this.mCvFaceHandle);
    }
}
