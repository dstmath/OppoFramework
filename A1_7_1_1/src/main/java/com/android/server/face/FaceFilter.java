package com.android.server.face;

import android.graphics.PointF;
import android.graphics.Rect;
import android.os.SystemClock;
import com.android.server.am.OppoProcessManager;
import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.android.server.face.utils.LogUtil;
import com.android.server.face.utils.TimeUtils;
import com.sensetime.faceapi.FaceLibrary;
import com.sensetime.faceapi.model.FaceInfo;

public class FaceFilter {
    private static final String TAG = "FaceService.FaceFilter";

    public static FaceInfo getMaxFace(FaceInfo[] faces) {
        if (faces == null || faces.length <= 0) {
            return null;
        }
        FaceInfo faceInfo = null;
        int i = 0;
        while (i < faces.length) {
            if (faceInfo == null || faces[i].faceRect.width() > faceInfo.faceRect.width()) {
                faceInfo = faces[i];
            }
            i++;
        }
        return faceInfo;
    }

    public static boolean hasNoneFaceInWidgetRect(Rect faceRect, Rect widgetRect) {
        LogUtil.d(TAG, "faceRect = " + faceRect.toString() + ", widgetRect = " + widgetRect.toString());
        if (widgetRect.left > faceRect.right || widgetRect.top > faceRect.bottom || widgetRect.right < faceRect.left || widgetRect.bottom < faceRect.top) {
            return true;
        }
        return false;
    }

    public static boolean isFaceBeyondWidgetRectBorder(Rect faceRect, Rect widgetRect) {
        LogUtil.d(TAG, "faceRect = " + faceRect.toString() + ", widgetRect = " + widgetRect.toString());
        return !isFaceInWidget(widgetRect, faceRect);
    }

    public static boolean isHighQuality(float score) {
        LogUtil.d(TAG, "filterFaces quality: " + score);
        return score > 2.0f;
    }

    public static Rect rotateFaceRect(Rect rect, int width, int height, boolean isFrontCamera, int degrees) {
        int tmp;
        switch (degrees) {
            case 0:
                if (isFrontCamera) {
                    rect.left = width - rect.left;
                    rect.right = width - rect.right;
                    break;
                }
                break;
            case 90:
                tmp = rect.left;
                rect.left = height - rect.bottom;
                rect.bottom = rect.right;
                rect.right = height - rect.top;
                rect.top = tmp;
                if (isFrontCamera) {
                    tmp = rect.top;
                    rect.top = width - rect.bottom;
                    rect.bottom = width - tmp;
                    break;
                }
                break;
            case OppoProcessManager.MSG_UPLOAD /*180*/:
                rect.top = height - rect.top;
                rect.bottom = height - rect.bottom;
                if (!isFrontCamera) {
                    rect.left = width - rect.left;
                    rect.right = width - rect.right;
                    break;
                }
                break;
            case 270:
                tmp = rect.left;
                rect.left = height - rect.bottom;
                rect.bottom = rect.right;
                rect.right = height - rect.top;
                rect.top = tmp;
                tmp = rect.left;
                rect.left = height - rect.right;
                rect.right = height - tmp;
                if (!isFrontCamera) {
                    tmp = rect.top;
                    rect.top = width - rect.bottom;
                    rect.bottom = width - tmp;
                    break;
                }
                break;
        }
        return rect;
    }

    public static PointF rotatePoints(PointF point, int width, int height, boolean isFrontCamera, int degrees) {
        float tmp;
        switch (degrees) {
            case 0:
                if (isFrontCamera) {
                    point.x = ((float) width) - point.x;
                    break;
                }
                break;
            case 90:
                tmp = point.x;
                point.x = ((float) height) - point.y;
                point.y = tmp;
                if (isFrontCamera) {
                    point.y = ((float) width) - point.y;
                    break;
                }
                break;
            case OppoProcessManager.MSG_UPLOAD /*180*/:
                point.y = ((float) height) - point.y;
                if (!isFrontCamera) {
                    point.x = ((float) width) - point.x;
                    break;
                }
                break;
            case 270:
                tmp = point.y;
                point.y = point.x;
                point.x = tmp;
                if (!isFrontCamera) {
                    point.y = ((float) width) - point.y;
                    break;
                }
                break;
        }
        return point;
    }

    public static float getBrightness(byte[] nv21, int width, int height, Rect faceRect) {
        long startTime = SystemClock.uptimeMillis();
        float bright = FaceLibrary.averageBrightness(nv21, CvPixelFormat.NV21, width, height, faceRect.left, faceRect.top, faceRect.width(), faceRect.height());
        TimeUtils.calculateTime(TAG, "getBrightness", SystemClock.uptimeMillis() - startTime);
        return bright;
    }

    public static boolean isFitAngle(FaceInfo faceInfo) {
        if (faceInfo.pitch < -30.0f || faceInfo.pitch > 25.0f || Math.abs(faceInfo.yaw) > 45.0f) {
            return true;
        }
        return false;
    }

    public static boolean isFaceInWidget(Rect frameRect, Rect faceRect) {
        int left = frameRect.left + 0;
        int top = frameRect.top + 0;
        int right = frameRect.right + 0;
        int bottom = frameRect.bottom + 0;
        if (left >= right || top >= bottom || left > faceRect.left || top > faceRect.top || right < faceRect.right || bottom < faceRect.bottom) {
            return false;
        }
        return true;
    }

    public static int faceScaleFit(Rect faceRect, Rect widgetRect, float yaw) {
        LogUtil.d(TAG, "faceScaleFit faceRect = " + faceRect.toString() + ", widgetRect = " + widgetRect.toString());
        float widthScale = (((float) (faceRect.right - faceRect.left)) * 1.0f) / ((float) (widgetRect.right - widgetRect.left));
        float heightScale = (((float) (faceRect.bottom - faceRect.top)) * 1.0f) / ((float) (widgetRect.bottom - widgetRect.top));
        float correction = 1.0f + (Math.abs(yaw / 45.0f) / 9.8f);
        LogUtil.d(TAG, "aftercorrect = " + (widthScale * correction) + ", widthScale = " + widthScale + ", correction = " + correction + ", heightScale = " + heightScale + ", yaw = " + yaw);
        if (widthScale * correction < 0.33f && heightScale * correction < 0.33f) {
            return 1;
        }
        if (widthScale * correction <= 0.84f || heightScale * correction <= 0.84f) {
            return 0;
        }
        return -1;
    }

    public static boolean containFace(Rect frameRect, Rect faceRect) {
        int tolerantWidth = faceRect.width() / 3;
        int left = frameRect.left - tolerantWidth;
        int top = frameRect.top - tolerantWidth;
        int right = frameRect.right + tolerantWidth;
        int bottom = frameRect.bottom + tolerantWidth;
        if (left >= right || top >= bottom || left > faceRect.left || top > faceRect.top || right < faceRect.right || bottom < faceRect.bottom) {
            return false;
        }
        return true;
    }

    public static boolean isUp(FaceInfo face) {
        if (face.pitch <= -20.0f || face.pitch >= -5.0f) {
            return false;
        }
        return true;
    }

    public static boolean isDown(FaceInfo face) {
        if (face.pitch <= 5.0f || face.pitch >= 20.0f) {
            return false;
        }
        return true;
    }

    public static boolean isLeft(FaceInfo face) {
        if (face.yaw <= -45.0f || face.yaw >= -10.0f) {
            return false;
        }
        return true;
    }

    public static boolean isRight(FaceInfo face) {
        if (face.yaw <= 10.0f || face.yaw >= 45.0f) {
            return false;
        }
        return true;
    }

    public static boolean isCenter(FaceInfo face) {
        if (Math.abs(face.pitch) >= 13.0f || Math.abs(face.yaw) >= 8.0f) {
            return false;
        }
        return true;
    }

    public static boolean isFitRollAngle(FaceInfo face, float minAngle, float maxAngle) {
        return face.roll > minAngle && face.roll < maxAngle;
    }
}
