package com.android.server.face.sensetime.faceapi;

import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.android.server.face.sensetime.faceapi.model.FaceOrientation;
import com.android.server.face.utils.LogUtil;
import com.sensetime.faceapi.FaceLibrary;
import com.sensetime.faceapi.model.FaceInfo;

public class FaceHacker extends FaceHandleBase {
    private static final String TAG = "FaceService.FaceHacker";

    public FaceHacker(String modelPath) {
        this.mCvFaceHandle = FaceLibrary.cvFaceCreateHackness(modelPath);
        LogUtil.d(TAG, "isHandleInitialized = " + isHandleInitialized());
    }

    public float hacker(byte[] image, CvPixelFormat format, int width, int height, int stride, FaceOrientation orientation, FaceInfo info) {
        return FaceLibrary.cvFaceHackness(this.mCvFaceHandle, image, format.getValue(), width, height, stride, orientation.getValue(), info, this.mResultCode);
    }

    protected void releaseHandle() {
        FaceLibrary.cvFaceDestroyHackness(this.mCvFaceHandle);
    }
}
