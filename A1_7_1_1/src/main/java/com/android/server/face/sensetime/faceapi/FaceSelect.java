package com.android.server.face.sensetime.faceapi;

import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.android.server.face.sensetime.faceapi.model.FaceConfig.FaceImageResize;
import com.android.server.face.utils.LogUtil;
import com.sensetime.faceapi.FaceLibrary;
import com.sensetime.faceapi.model.FaceInfo;

public class FaceSelect extends FaceHandleBase {
    private static final String TAG = "FaceService.FaceSelect";

    public FaceSelect() {
        this(null, null);
    }

    public FaceSelect(FaceImageResize resize) {
        this(null, resize);
    }

    public FaceSelect(String modelPath, FaceImageResize resize) {
        if (resize == null) {
            resize = FaceImageResize.DEFAULT_CONFIG;
        }
        init(modelPath, resize.getValue());
    }

    private void init(String modelPath, int config) {
        this.mCvFaceHandle = FaceLibrary.cvFaceCreateSelect(modelPath, config);
    }

    public void reset() {
        if (isHandleInitialized()) {
            FaceLibrary.cvFaceResetSelect(this.mCvFaceHandle);
        } else {
            LogUtil.e(TAG, "reset Handle not Initialized");
        }
    }

    public float selectFrame(byte[] image, CvPixelFormat format, int width, int height, int stride, FaceInfo info) {
        return FaceLibrary.cvFaceSelectFrame(this.mCvFaceHandle, image, format.getValue(), width, height, stride, info, this.mResultCode);
    }

    protected void releaseHandle() {
        FaceLibrary.cvFaceDestroySelect(this.mCvFaceHandle);
    }
}
