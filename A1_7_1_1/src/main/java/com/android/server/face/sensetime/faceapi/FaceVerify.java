package com.android.server.face.sensetime.faceapi;

import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.android.server.face.utils.LogUtil;
import com.sensetime.faceapi.FaceLibrary;
import com.sensetime.faceapi.model.FaceInfo;

public class FaceVerify extends FaceHandleBase {
    private static final String TAG = "FaceService.FaceVerify";

    public FaceVerify(String modelPath) {
        this.mCvFaceHandle = FaceLibrary.cvFaceCreateVerify(modelPath);
        LogUtil.d(TAG, "isHandleInitialized = " + isHandleInitialized());
    }

    public byte[] getFeature(byte[] colorImage, CvPixelFormat cvPixelFormat, int imageWidth, int imageHeight, int imageStride, FaceInfo faceInfo) {
        byte[] feature = FaceLibrary.cvFaceGetFeatureBytes(this.mCvFaceHandle, colorImage, cvPixelFormat.getValue(), imageWidth, imageHeight, imageStride, faceInfo, this.mResultCode);
        checkResultCode(this.mResultCode[0]);
        return feature;
    }

    public int getVersion() {
        return FaceLibrary.getVerifyVersion(this.mCvFaceHandle);
    }

    protected void releaseHandle() {
        FaceLibrary.cvFaceDestroyVerify(this.mCvFaceHandle);
    }
}
