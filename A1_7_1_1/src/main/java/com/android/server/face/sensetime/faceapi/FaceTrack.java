package com.android.server.face.sensetime.faceapi;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.android.server.face.sensetime.faceapi.model.FaceConfig.FaceImageResize;
import com.android.server.face.sensetime.faceapi.model.FaceConfig.FaceKeyPointCount;
import com.android.server.face.sensetime.faceapi.model.FaceConfig.TrackThreadCount;
import com.android.server.face.sensetime.faceapi.model.FaceOrientation;
import com.android.server.face.sensetime.faceapi.utils.ColorConvertUtil;
import com.android.server.face.utils.LogUtil;
import com.sensetime.faceapi.FaceLibrary;
import com.sensetime.faceapi.model.FaceInfo;

public class FaceTrack extends FaceHandleBase {
    private static final String TAG = "FaceService.FaceTrack";

    public FaceTrack() {
        this(null, null, null, null, null);
    }

    public FaceTrack(FaceImageResize resize, FaceKeyPointCount pointCount, TrackThreadCount threadCount) {
        this(null, null, resize, pointCount, threadCount);
    }

    public FaceTrack(String detectModelPath, String alignModelPath, FaceImageResize resize, FaceKeyPointCount pointCount, TrackThreadCount threadCount) {
        if (resize == null) {
            resize = FaceImageResize.RESIZE_640W;
        }
        if (pointCount == null) {
            pointCount = FaceKeyPointCount.POINT_COUNT_21;
        }
        if (threadCount == null) {
            threadCount = TrackThreadCount.DEFAULT_CONFIG;
        }
        init(detectModelPath, alignModelPath, (resize.getValue() | pointCount.getValue()) | threadCount.getValue());
    }

    private void init(String detectModelPath, String alignModelPath, int config) {
        this.mCvFaceHandle = FaceLibrary.cvFaceCreateTracker(detectModelPath, alignModelPath, config);
    }

    public FaceInfo[] track(int[] colorImage, CvPixelFormat cvPixelFormat, int imageWidth, int imageHeight, int imageStride, FaceOrientation orientation) {
        FaceInfo[] faces = FaceLibrary.cvFaceTrackInts(this.mCvFaceHandle, colorImage, cvPixelFormat.getValue(), imageWidth, imageHeight, imageStride, orientation.getValue(), this.mResultCode);
        checkResultCode();
        return faces;
    }

    public FaceInfo[] track(byte[] colorImage, CvPixelFormat cvPixelFormat, int imageWidth, int imageHeight, int imageStride, FaceOrientation orientation) {
        FaceInfo[] faces = FaceLibrary.cvFaceTrackBytes(this.mCvFaceHandle, colorImage, cvPixelFormat.getValue(), imageWidth, imageHeight, imageStride, orientation.getValue(), this.mResultCode);
        checkResultCode();
        return faces;
    }

    public FaceInfo[] track(Bitmap bitmap) {
        return track(bitmap, FaceOrientation.UP);
    }

    public FaceInfo[] track(Bitmap bitmap, FaceOrientation orientation) {
        return track(bitmap, orientation, null);
    }

    public FaceInfo[] track(Bitmap bitmap, FaceOrientation orientation, byte[] buffer) {
        if (bitmap == null || bitmap.isRecycled()) {
            LogUtil.w(TAG, "track image is null or Recycled");
            return null;
        }
        if (bitmap.getConfig() != Config.ARGB_8888) {
            bitmap = bitmap.copy(Config.ARGB_8888, false);
        }
        if (buffer == null) {
            buffer = createBufferIfNeed(bitmap.getWidth(), bitmap.getHeight());
        } else if (buffer.length != (bitmap.getWidth() * bitmap.getHeight()) * 3) {
            LogUtil.e(TAG, "track detect buffer is illegal !");
        }
        ColorConvertUtil.getBGRFromBitmap(bitmap, buffer);
        return track(buffer, CvPixelFormat.BGR888, bitmap.getWidth(), bitmap.getHeight(), bitmap.getWidth() * 3, orientation);
    }

    public FaceInfo[] track(byte[] colorImage, CvPixelFormat cvPixelFormat, int imageWidth, int imageHeight) {
        return track(colorImage, cvPixelFormat, imageWidth, imageHeight, FaceOrientation.UP);
    }

    public FaceInfo[] track(byte[] colorImage, CvPixelFormat cvPixelFormat, int imageWidth, int imageHeight, FaceOrientation orientation) {
        return track(colorImage, cvPixelFormat, imageWidth, imageHeight, imageWidth, orientation);
    }

    public FaceInfo[] track(int[] colorImage, int imageWidth, int imageHeight, FaceOrientation orientation) {
        return track(colorImage, CvPixelFormat.BGR888, imageWidth, imageHeight, orientation);
    }

    public FaceInfo[] track(int[] colorImage, CvPixelFormat cvPixelFormat, int imageWidth, int imageHeight, FaceOrientation orientation) {
        return track(colorImage, cvPixelFormat, imageWidth, imageHeight, imageWidth * 4, orientation);
    }

    public void reset() {
        if (isHandleInitialized()) {
            FaceLibrary.cvFaceResetTracker(this.mCvFaceHandle);
        } else {
            LogUtil.e(TAG, "reset Handle not Initialized");
        }
    }

    public void showInsideModelVersion() {
        FaceLibrary.cvFaceShowInsideModel();
    }

    public void setFaceTrackInterval(int count) {
        FaceLibrary.cvFaceTrackSetDetectInterval(this.mCvFaceHandle, count);
    }

    public void setFaceLimit(int count) {
        if (isHandleInitialized()) {
            long j = this.mCvFaceHandle;
            if (count <= 0) {
                count = -1;
            }
            checkResultCode(FaceLibrary.cvFaceTrackSetDetectFaceCntLimit(j, count));
            return;
        }
        LogUtil.e(TAG, "setFaceLimit Handle not Initialized");
    }

    protected void releaseHandle() {
        FaceLibrary.cvFaceDestroyTracker(this.mCvFaceHandle);
    }
}
