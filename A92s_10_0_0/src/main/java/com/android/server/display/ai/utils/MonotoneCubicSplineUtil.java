package com.android.server.display.ai.utils;

import android.content.Context;
import com.android.server.display.ai.bean.BrightnessPoint;
import com.android.server.display.ai.utils.BrightnessConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import oppo.util.OppoStatistics;

public class MonotoneCubicSplineUtil {
    public static final String SOURCE_DRAG = "Drag";
    public static final String SOURCE_INIT = "Init";
    public static final String SOURCE_TRAIN = "Train";
    private static final String TAG = "MonotoneCubicSplineUtil";

    public static ArrayList<BrightnessPoint> spline(List<BrightnessPoint> points, int splinePointSize) {
        ArrayList<BrightnessPoint> splinePoints = new ArrayList<>();
        float[][] mCArray = createInterpolant(points);
        if (mCArray != null) {
            BrightnessPoint lastPoint = points.get(points.size() - 1);
            for (int i = 0; i < splinePointSize; i++) {
                float x = (((float) i) * lastPoint.x) / ((float) splinePointSize);
                splinePoints.add(new BrightnessPoint(x, f(x, mCArray, points)));
            }
            splinePoints.add(new BrightnessPoint(lastPoint.x, lastPoint.y));
        } else {
            ColorAILog.d(TAG, "createInterpolant error");
        }
        return splinePoints;
    }

    /* JADX INFO: Multiple debug info for r6v2 int: [D('i' int), D('dxsLength' int)] */
    private static float[][] createInterpolant(List<BrightnessPoint> points) {
        int pointsLength = points.size();
        if (pointsLength == 1) {
            return null;
        }
        float[] dys = new float[(pointsLength - 1)];
        float[] dxs = new float[(pointsLength - 1)];
        float[] ms = new float[(pointsLength - 1)];
        for (int i = 0; i < pointsLength - 1; i++) {
            dxs[i] = points.get(i + 1).x - points.get(i).x;
            dys[i] = points.get(i + 1).y - points.get(i).y;
            ms[i] = dys[i] / dxs[i];
        }
        int dxsLength = dxs.length;
        float[] c1s = new float[pointsLength];
        for (int i2 = 0; i2 < pointsLength; i2++) {
            if (i2 == 0) {
                c1s[0] = ms[0];
            } else if (i2 == pointsLength - 1) {
                c1s[pointsLength - 1] = ms[ms.length - 1];
            } else {
                float m = ms[i2 - 1];
                float mNext = ms[i2];
                if (m * mNext <= 0.0f) {
                    c1s[i2] = 0.0f;
                } else {
                    float dx = dxs[i2 - 1];
                    float dxNext = dxs[i2];
                    float common = dx + dxNext;
                    c1s[i2] = (3.0f * common) / (((common + dxNext) / m) + ((common + dx) / mNext));
                }
            }
        }
        ColorAILog.d(TAG, Arrays.toString(c1s));
        float[] c2s = new float[dxsLength];
        float[] c3s = new float[dxsLength];
        int length = c1s.length - 1;
        for (int i3 = 0; i3 < length; i3++) {
            float c1 = c1s[i3];
            float invDx = 1.0f / dxs[i3];
            float common2 = ((c1 + c1s[i3 + 1]) - ms[i3]) - ms[i3];
            c2s[i3] = ((ms[i3] - c1) - common2) * invDx;
            c3s[i3] = common2 * invDx * invDx;
            ColorAILog.d(TAG, "c:" + c1s[i3] + ", " + c2s[i3] + "," + c3s[i3]);
        }
        return new float[][]{c1s, c2s, c3s};
    }

    private static float f(float x, float[][] cArray, List<BrightnessPoint> points) {
        float[] c1s = cArray[0];
        float[] c2s = cArray[1];
        float[] c3s = cArray[2];
        int low = 0;
        int high = c3s.length - 1;
        while (low <= high) {
            int mid = (int) Math.floor(((double) (low + high)) * 0.5d);
            float xHere = points.get(mid).x;
            if (xHere < x) {
                low = mid + 1;
            } else if (xHere <= x) {
                return points.get(mid).y;
            } else {
                high = mid - 1;
            }
        }
        int i = Math.max(0, high);
        float diff = x - points.get(i).x;
        float diffSq = diff * diff;
        return points.get(i).y + (c1s[i] * diff) + (c2s[i] * diffSq) + (c3s[i] * diff * diffSq);
    }

    private static boolean verifyMonotone(List<BrightnessPoint> points) {
        int length = points.size() - 1;
        for (int i = 0; i < length; i++) {
            BrightnessPoint point = points.get(i);
            BrightnessPoint nextPoint = points.get(i + 1);
            if (point.x >= nextPoint.x || point.y > nextPoint.y) {
                return false;
            }
        }
        return true;
    }

    private static void fixToMonotone(List<BrightnessPoint> points) {
        ArrayList<BrightnessPoint> errorPoints = new ArrayList<>();
        int length = points.size() - 1;
        for (int i = 0; i < length; i++) {
            BrightnessPoint point = points.get(i);
            BrightnessPoint nextPoint = points.get(i + 1);
            if (point.x >= nextPoint.x || point.y > nextPoint.y) {
                errorPoints.add(nextPoint);
            }
        }
        Iterator<BrightnessPoint> it = errorPoints.iterator();
        while (it.hasNext()) {
            BrightnessPoint point2 = it.next();
            points.remove(point2);
            ColorAILog.d(TAG, "fixToMonotone remove point:" + point2);
        }
    }

    public static void checkMonotone(Context context, List<BrightnessPoint> points, BrightnessPoint dragPoint, String source) {
        if (points != null) {
            if (!verifyMonotone(points)) {
                String errorMsg = getPointDumpString(points, dragPoint, source);
                ColorAILog.e(TAG, "checkMonotone, verifyMonotone error:" + errorMsg);
                HashMap<String, String> logMap = new HashMap<>();
                logMap.put(BrightnessConstants.Statistics.KEY_VERIFY_DRAG_MONOTONE, errorMsg);
                fixToMonotone(points);
                String resolveMsg = getPointDumpString(points, dragPoint, source);
                ColorAILog.e(TAG, "checkMonotone, verifyMonotone resolveMsg:" + resolveMsg);
                logMap.put(BrightnessConstants.Statistics.KEY_VERIFY_DRAG_MONOTONE_RESOLVE, resolveMsg);
                OppoStatistics.onCommon(context, "ai_brightness", BrightnessConstants.Statistics.EVENT_ID_VERIFY, logMap, false);
                return;
            }
            ColorAILog.i(TAG, "checkMonotone, verifyMonotone ok");
        }
    }

    private static String getPointDumpString(List<BrightnessPoint> points, BrightnessPoint dragPoint, String source) {
        if (points == null) {
            return "points is null";
        }
        StringBuilder sb = new StringBuilder("points:");
        for (BrightnessPoint point : points) {
            sb.append("(");
            sb.append(point.x);
            sb.append(",");
            sb.append(point.y);
            sb.append("),");
        }
        if (dragPoint != null) {
            sb.append("dragPoint:");
            sb.append(dragPoint.x);
            sb.append(", ");
            sb.append(dragPoint.y);
        }
        sb.append(" source: ");
        sb.append(source);
        return sb.toString();
    }
}
