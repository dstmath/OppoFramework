package com.color.util;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

public class ColorRoundRectUtil {
    private static ColorRoundRectUtil sInstance;
    private Path mPath = new Path();

    private ColorRoundRectUtil() {
    }

    public static synchronized ColorRoundRectUtil getInstance() {
        ColorRoundRectUtil colorRoundRectUtil;
        synchronized (ColorRoundRectUtil.class) {
            if (sInstance == null) {
                sInstance = new ColorRoundRectUtil();
            }
            colorRoundRectUtil = sInstance;
        }
        return colorRoundRectUtil;
    }

    public Path getPath(Rect rect, float radius) {
        return getPath((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, radius);
    }

    public Path getPath(RectF rect, float radius) {
        return getPath(rect.left, rect.top, rect.right, rect.bottom, radius);
    }

    public Path getPath(float left, float top, float right, float bottom, float radius) {
        return getPath(left, top, right, bottom, radius, true, true, true, true);
    }

    /* JADX INFO: Multiple debug info for r1v46 float: [D('percentage' float), D('vertexRatio' float)] */
    public Path getPath(float left, float top, float right, float bottom, float radius, boolean tl, boolean tr, boolean bl, boolean br) {
        float radius2;
        float vertexRatio;
        float controlRatio;
        Path path = this.mPath;
        if (path != null) {
            path.reset();
        }
        if (radius < 0.0f) {
            radius2 = 0.0f;
        } else {
            radius2 = radius;
        }
        float width = right - left;
        float height = bottom - top;
        if (((double) (radius2 / Math.min(width / 2.0f, height / 2.0f))) > 0.5d) {
            vertexRatio = 1.0f - (0.13877845f * Math.min(1.0f, ((radius2 / Math.min(width / 2.0f, height / 2.0f)) - 0.5f) / 0.4f));
        } else {
            vertexRatio = 1.0f;
        }
        if (radius2 / Math.min(width / 2.0f, height / 2.0f) > 0.6f) {
            controlRatio = (0.042454004f * Math.min(1.0f, ((radius2 / Math.min(width / 2.0f, height / 2.0f)) - 0.6f) / 0.3f)) + 1.0f;
        } else {
            controlRatio = 1.0f;
        }
        this.mPath.moveTo((width / 2.0f) + left, top);
        if (!tr) {
            this.mPath.lineTo(left + width, top);
        } else {
            this.mPath.lineTo(Math.max(width / 2.0f, width - (((radius2 / 100.0f) * 128.19f) * vertexRatio)) + left, top);
            this.mPath.cubicTo((left + width) - (((radius2 / 100.0f) * 83.62f) * controlRatio), top, (left + width) - ((radius2 / 100.0f) * 67.45f), top + ((radius2 / 100.0f) * 4.64f), (left + width) - ((radius2 / 100.0f) * 51.16f), top + ((radius2 / 100.0f) * 13.36f));
            this.mPath.cubicTo((left + width) - ((radius2 / 100.0f) * 34.86f), top + ((radius2 / 100.0f) * 22.07f), (left + width) - ((radius2 / 100.0f) * 22.07f), top + ((radius2 / 100.0f) * 34.86f), (left + width) - ((radius2 / 100.0f) * 13.36f), top + ((radius2 / 100.0f) * 51.16f));
            this.mPath.cubicTo((left + width) - ((radius2 / 100.0f) * 4.64f), top + ((radius2 / 100.0f) * 67.45f), left + width, top + ((radius2 / 100.0f) * 83.62f * controlRatio), left + width, top + Math.min(height / 2.0f, (radius2 / 100.0f) * 128.19f * vertexRatio));
        }
        if (!br) {
            this.mPath.lineTo(left + width, top + height);
        } else {
            this.mPath.lineTo(left + width, Math.max(height / 2.0f, height - (((radius2 / 100.0f) * 128.19f) * vertexRatio)) + top);
            this.mPath.cubicTo(left + width, (top + height) - (((radius2 / 100.0f) * 83.62f) * controlRatio), (left + width) - ((radius2 / 100.0f) * 4.64f), (top + height) - ((radius2 / 100.0f) * 67.45f), (left + width) - ((radius2 / 100.0f) * 13.36f), (top + height) - ((radius2 / 100.0f) * 51.16f));
            this.mPath.cubicTo((left + width) - ((radius2 / 100.0f) * 22.07f), (top + height) - ((radius2 / 100.0f) * 34.86f), (left + width) - ((radius2 / 100.0f) * 34.86f), (top + height) - ((radius2 / 100.0f) * 22.07f), (left + width) - ((radius2 / 100.0f) * 51.16f), (top + height) - ((radius2 / 100.0f) * 13.36f));
            this.mPath.cubicTo((left + width) - ((radius2 / 100.0f) * 67.45f), (top + height) - ((radius2 / 100.0f) * 4.64f), (left + width) - (((radius2 / 100.0f) * 83.62f) * controlRatio), top + height, left + Math.max(width / 2.0f, width - (((radius2 / 100.0f) * 128.19f) * vertexRatio)), top + height);
        }
        if (!bl) {
            this.mPath.lineTo(left, top + height);
        } else {
            this.mPath.lineTo(Math.min(width / 2.0f, (radius2 / 100.0f) * 128.19f * vertexRatio) + left, top + height);
            this.mPath.cubicTo(left + ((radius2 / 100.0f) * 83.62f * controlRatio), top + height, left + ((radius2 / 100.0f) * 67.45f), (top + height) - ((radius2 / 100.0f) * 4.64f), left + ((radius2 / 100.0f) * 51.16f), (top + height) - ((radius2 / 100.0f) * 13.36f));
            this.mPath.cubicTo(left + ((radius2 / 100.0f) * 34.86f), (top + height) - ((radius2 / 100.0f) * 22.07f), left + ((radius2 / 100.0f) * 22.07f), (top + height) - ((radius2 / 100.0f) * 34.86f), left + ((radius2 / 100.0f) * 13.36f), (top + height) - ((radius2 / 100.0f) * 51.16f));
            this.mPath.cubicTo(((radius2 / 100.0f) * 4.64f) + left, (top + height) - ((radius2 / 100.0f) * 67.45f), left, (top + height) - (((radius2 / 100.0f) * 83.62f) * controlRatio), left, top + Math.max(height / 2.0f, height - (((radius2 / 100.0f) * 128.19f) * vertexRatio)));
        }
        if (!tl) {
            this.mPath.lineTo(left, top);
        } else {
            this.mPath.lineTo(left, Math.min(height / 2.0f, (radius2 / 100.0f) * 128.19f * vertexRatio) + top);
            this.mPath.cubicTo(left, top + ((radius2 / 100.0f) * 83.62f * controlRatio), left + ((radius2 / 100.0f) * 4.64f), top + ((radius2 / 100.0f) * 67.45f), left + ((radius2 / 100.0f) * 13.36f), top + ((radius2 / 100.0f) * 51.16f));
            this.mPath.cubicTo(left + ((radius2 / 100.0f) * 22.07f), top + ((radius2 / 100.0f) * 34.86f), left + ((radius2 / 100.0f) * 34.86f), top + ((radius2 / 100.0f) * 22.07f), left + ((radius2 / 100.0f) * 51.16f), top + ((radius2 / 100.0f) * 13.36f));
            this.mPath.cubicTo(((radius2 / 100.0f) * 67.45f) + left, ((radius2 / 100.0f) * 4.64f) + top, ((radius2 / 100.0f) * 83.62f * controlRatio) + left, top, left + Math.min(width / 2.0f, (radius2 / 100.0f) * 128.19f * vertexRatio), top);
        }
        this.mPath.close();
        return this.mPath;
    }
}
