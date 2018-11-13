package com.android.server.face.sensetime.faceapi;

import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.android.server.face.sensetime.faceapi.model.FaceConfig.FaceImageResize;
import com.android.server.face.sensetime.faceapi.model.FaceConfig.FaceKeyPointCount;
import com.android.server.face.sensetime.faceapi.model.FaceOrientation;
import com.android.server.face.utils.LogUtil;
import com.sensetime.faceapi.FaceLibrary;
import com.sensetime.faceapi.model.FaceInfo;

public class FaceDetect extends FaceHandleBase {
    private static final String TAG = "FaceService.FaceDetect";

    public FaceDetect(FaceImageResize resize) {
        this.mCvFaceHandle = FaceLibrary.cvFaceCreateDetector(null, resize.getValue() | FaceKeyPointCount.POINT_COUNT_21.getValue());
        LogUtil.d(TAG, "isHandleInitialized = " + isHandleInitialized());
    }

    public float getFaceThreshold() {
        return FaceLibrary.getDetectThreshold(this.mCvFaceHandle, this.mResultCode);
    }

    public void setFaceThreshold(float threshold) {
        FaceLibrary.setDetectThreshold(this.mCvFaceHandle, threshold, this.mResultCode);
        checkResultCode(this.mResultCode[0]);
    }

    public FaceInfo[] detect(byte[] colorImage, CvPixelFormat cvPixelFormat, int imageWidth, int imageHeight, int imageStride, FaceOrientation orientation) {
        FaceInfo[] faces = FaceLibrary.cvFaceDetectBytes(this.mCvFaceHandle, colorImage, cvPixelFormat.getValue(), imageWidth, imageHeight, imageStride, orientation.getValue(), this.mResultCode);
        checkResultCode();
        return faces;
    }

    protected void releaseHandle() {
        FaceLibrary.cvFaceDestroyDetector(this.mCvFaceHandle);
    }
}
