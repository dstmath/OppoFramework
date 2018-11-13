package com.android.server.face.sensetime.faceapi;

import com.android.server.face.sensetime.faceapi.model.ResultCode;
import com.android.server.face.utils.LogUtil;

public abstract class FaceHandleBase {
    protected byte[] mBuffer;
    protected long mCvFaceHandle;
    protected int[] mResultCode = new int[1];

    protected abstract void releaseHandle();

    protected boolean isHandleInitialized() {
        return this.mCvFaceHandle != 0;
    }

    protected void checkResultCode(int resultCode) {
        if (resultCode != ResultCode.OK.getValue()) {
            LogUtil.e("FaceHandleBase", "Calling native method failed, ResultCode = " + resultCode + " Reason = " + ResultCode.getDescription(resultCode), new Throwable("checkResultCode"));
        }
    }

    protected void checkResultCode() {
        checkResultCode(this.mResultCode[0]);
    }

    protected byte[] createBufferIfNeed(int width, int height) {
        int size = (width * height) * 3;
        if (this.mBuffer != null && this.mBuffer.length == size) {
            return this.mBuffer;
        }
        this.mBuffer = new byte[size];
        return this.mBuffer;
    }

    protected void finalize() throws Throwable {
        release();
    }

    public void release() {
        if (this.mCvFaceHandle != 0) {
            this.mBuffer = null;
            releaseHandle();
            this.mCvFaceHandle = 0;
        }
    }
}
