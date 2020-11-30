package android.graphics;

import android.graphics.Canvas;
import android.graphics.OppoBaseBaseCanvas;
import android.graphics.PorterDuff;
import android.graphics.text.MeasuredText;
import android.text.GraphicsOperations;
import android.text.PrecomputedText;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;

public class BaseRecordingCanvas extends Canvas {
    private static native void nDrawArc(long j, float f, float f2, float f3, float f4, float f5, float f6, boolean z, long j2);

    private static native void nDrawBitmap(long j, long j2, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, long j3, int i, int i2);

    private static native void nDrawBitmap(long j, long j2, float f, float f2, long j3, int i, int i2, int i3);

    private static native void nDrawBitmap(long j, int[] iArr, int i, int i2, float f, float f2, int i3, int i4, boolean z, long j2);

    private static native void nDrawBitmapMatrix(long j, long j2, long j3, long j4);

    private static native void nDrawBitmapMesh(long j, long j2, int i, int i2, float[] fArr, int i3, int[] iArr, int i4, long j3);

    private static native void nDrawCircle(long j, float f, float f2, float f3, long j2);

    private static native void nDrawColor(long j, int i, int i2);

    private static native void nDrawColor(long j, long j2, long j3, int i);

    private static native void nDrawDoubleRoundRect(long j, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, long j2);

    private static native void nDrawDoubleRoundRect(long j, float f, float f2, float f3, float f4, float[] fArr, float f5, float f6, float f7, float f8, float[] fArr2, long j2);

    private static native void nDrawLine(long j, float f, float f2, float f3, float f4, long j2);

    private static native void nDrawLines(long j, float[] fArr, int i, int i2, long j2);

    private static native void nDrawNinePatch(long j, long j2, long j3, float f, float f2, float f3, float f4, long j4, int i, int i2);

    private static native void nDrawOval(long j, float f, float f2, float f3, float f4, long j2);

    private static native void nDrawPaint(long j, long j2);

    private static native void nDrawPath(long j, long j2, long j3);

    private static native void nDrawPoint(long j, float f, float f2, long j2);

    private static native void nDrawPoints(long j, float[] fArr, int i, int i2, long j2);

    private static native void nDrawRect(long j, float f, float f2, float f3, float f4, long j2);

    private static native void nDrawRegion(long j, long j2, long j3);

    private static native void nDrawRoundRect(long j, float f, float f2, float f3, float f4, float f5, float f6, long j2);

    private static native void nDrawText(long j, String str, int i, int i2, float f, float f2, int i3, long j2);

    private static native void nDrawText(long j, char[] cArr, int i, int i2, float f, float f2, int i3, long j2);

    private static native void nDrawTextOnPath(long j, String str, long j2, float f, float f2, int i, long j3);

    private static native void nDrawTextOnPath(long j, char[] cArr, int i, int i2, long j2, float f, float f2, int i3, long j3);

    private static native void nDrawTextRun(long j, String str, int i, int i2, int i3, int i4, float f, float f2, boolean z, long j2);

    private static native void nDrawTextRun(long j, char[] cArr, int i, int i2, int i3, int i4, float f, float f2, boolean z, long j2, long j3);

    private static native void nDrawVertices(long j, int i, int i2, float[] fArr, int i3, float[] fArr2, int i4, int[] iArr, int i5, short[] sArr, int i6, int i7, long j2);

    public BaseRecordingCanvas(long nativeCanvas) {
        super(nativeCanvas);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF(left, top, right, bottom));
        nDrawArc(this.mNativeCanvasWrapper, left, top, right, bottom, startAngle, sweepAngle, useCenter, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        drawArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, useCenter, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawARGB(int a, int r, int g, int b) {
        drawColor(Color.argb(a, r, g, b));
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        throwIfCannotDraw(bitmap);
        Paint paint2 = paint;
        OppoBaseBaseCanvas.Entity entity = changeBitmap(paint2, bitmap, getRectF((float) bitmap.getWidth(), (float) bitmap.getHeight()));
        if (entity.isDarkMode) {
            paint2 = entity.newPaint;
        }
        nDrawBitmap(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), left, top, paint2 != null ? paint2.getNativeInstance() : 0, this.mDensity, this.mScreenDensity, bitmap.mDensity);
        resetEntity(entity, paint2);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeBitmap(paint, bitmap, getRectF((float) bitmap.getWidth(), (float) bitmap.getHeight()));
        if (entity.isDarkMode) {
            paint = entity.newPaint;
        }
        nDrawBitmapMatrix(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), matrix.ni(), paint != null ? paint.getNativeInstance() : 0);
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
        int right;
        int bottom;
        int top;
        int left;
        if (dst != null) {
            throwIfCannotDraw(bitmap);
            Paint paint2 = paint;
            OppoBaseBaseCanvas.Entity entity = changeBitmap(paint2, bitmap, getRectF(dst));
            if (entity.isDarkMode) {
                paint2 = entity.newPaint;
            }
            long nativePaint = paint2 == null ? 0 : paint2.getNativeInstance();
            if (src == null) {
                left = 0;
                top = 0;
                right = bitmap.getWidth();
                bottom = bitmap.getHeight();
            } else {
                left = src.left;
                int right2 = src.right;
                top = src.top;
                right = right2;
                bottom = src.bottom;
            }
            nDrawBitmap(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), (float) left, (float) top, (float) right, (float) bottom, (float) dst.left, (float) dst.top, (float) dst.right, (float) dst.bottom, nativePaint, this.mScreenDensity, bitmap.mDensity);
            resetEntity(entity, paint2);
            return;
        }
        throw new NullPointerException();
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        float bottom;
        float right;
        float right2;
        float left;
        if (dst != null) {
            throwIfCannotDraw(bitmap);
            Paint paint2 = paint;
            OppoBaseBaseCanvas.Entity entity = changeBitmap(paint2, bitmap, dst);
            if (entity.isDarkMode) {
                paint2 = entity.newPaint;
            }
            long nativePaint = paint2 == null ? 0 : paint2.getNativeInstance();
            if (src == null) {
                left = 0.0f;
                right2 = 0.0f;
                right = (float) bitmap.getWidth();
                bottom = (float) bitmap.getHeight();
            } else {
                left = (float) src.left;
                right = (float) src.right;
                right2 = (float) src.top;
                bottom = (float) src.bottom;
            }
            nDrawBitmap(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), left, right2, right, bottom, dst.left, dst.top, dst.right, dst.bottom, nativePaint, this.mScreenDensity, bitmap.mDensity);
            resetEntity(entity, paint2);
            return;
        }
        throw new NullPointerException();
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    @Deprecated
    public final void drawBitmap(int[] colors, int offset, int stride, float x, float y, int width, int height, boolean hasAlpha, Paint paint) {
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        } else if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        } else if (Math.abs(stride) >= width) {
            int lastScanline = offset + ((height - 1) * stride);
            int length = colors.length;
            if (offset < 0 || offset + width > length || lastScanline < 0 || lastScanline + width > length) {
                throw new ArrayIndexOutOfBoundsException();
            } else if (width != 0 && height != 0) {
                nDrawBitmap(this.mNativeCanvasWrapper, changeColors(colors), offset, stride, x, y, width, height, hasAlpha, paint != null ? paint.getNativeInstance() : 0);
            }
        } else {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    @Deprecated
    public final void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width, int height, boolean hasAlpha, Paint paint) {
        drawBitmap(colors, offset, stride, (float) x, (float) y, width, height, hasAlpha, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight, float[] verts, int vertOffset, int[] colors, int colorOffset, Paint paint) {
        Paint paint2;
        if ((meshWidth | meshHeight | vertOffset | colorOffset) < 0) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (meshWidth == 0) {
        } else {
            if (meshHeight != 0) {
                int count = (meshWidth + 1) * (meshHeight + 1);
                checkRange(verts.length, vertOffset, count * 2);
                if (colors != null) {
                    checkRange(colors.length, colorOffset, count);
                }
                OppoBaseBaseCanvas.Entity entity = changeBitmap(paint, bitmap, getRectF((float) bitmap.getWidth(), (float) bitmap.getHeight()));
                if (entity.isDarkMode) {
                    paint2 = entity.newPaint;
                } else {
                    paint2 = paint;
                }
                nDrawBitmapMesh(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), meshWidth, meshHeight, verts, vertOffset, colors, colorOffset, paint2 != null ? paint2.getNativeInstance() : 0);
                resetEntity(entity, paint2);
            }
        }
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawCircle(float cx, float cy, float radius, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF(radius * 2.0f, 2.0f * radius));
        nDrawCircle(this.mNativeCanvasWrapper, cx, cy, radius, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawColor(int color) {
        nDrawColor(this.mNativeCanvasWrapper, changeColor(color), BlendMode.SRC_OVER.getXfermode().porterDuffMode);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawColor(int color, PorterDuff.Mode mode) {
        nDrawColor(this.mNativeCanvasWrapper, changeColor(color), mode.nativeInt);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawColor(int color, BlendMode mode) {
        nDrawColor(this.mNativeCanvasWrapper, color, mode.getXfermode().porterDuffMode);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawColor(long color, BlendMode mode) {
        nDrawColor(this.mNativeCanvasWrapper, Color.colorSpace(color).getNativeInstance(), color, mode.getXfermode().porterDuffMode);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF(startX, startY, stopX, stopY));
        nDrawLine(this.mNativeCanvasWrapper, startX, startY, stopX, stopY, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawLines(float[] pts, int offset, int count, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF((float) getWidth(), (float) getHeight()));
        nDrawLines(this.mNativeCanvasWrapper, pts, offset, count, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawLines(float[] pts, Paint paint) {
        drawLines(pts, 0, pts.length, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawOval(float left, float top, float right, float bottom, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF(left, top, right, bottom));
        nDrawOval(this.mNativeCanvasWrapper, left, top, right, bottom, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawOval(RectF oval, Paint paint) {
        if (oval != null) {
            drawOval(oval.left, oval.top, oval.right, oval.bottom, paint);
            return;
        }
        throw new NullPointerException();
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawPaint(Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF((float) getWidth(), (float) getHeight()));
        nDrawPaint(this.mNativeCanvasWrapper, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawPatch(NinePatch patch, Rect dst, Paint paint) {
        Bitmap bitmap = patch.getBitmap();
        throwIfCannotDraw(bitmap);
        Paint paint2 = paint;
        OppoBaseBaseCanvas.Entity entity = changePatch(patch, paint2, getRectF(dst));
        if (entity.isDarkMode) {
            paint2 = entity.newPaint;
        }
        nDrawNinePatch(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), patch.mNativeChunk, (float) dst.left, (float) dst.top, (float) dst.right, (float) dst.bottom, paint2 == null ? 0 : paint2.getNativeInstance(), this.mDensity, patch.getDensity());
        resetEntity(entity, paint2);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawPatch(NinePatch patch, RectF dst, Paint paint) {
        Bitmap bitmap = patch.getBitmap();
        throwIfCannotDraw(bitmap);
        Paint paint2 = paint;
        OppoBaseBaseCanvas.Entity entity = changePatch(patch, paint2, dst);
        if (entity.isDarkMode) {
            paint2 = entity.newPaint;
        }
        nDrawNinePatch(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), patch.mNativeChunk, dst.left, dst.top, dst.right, dst.bottom, paint2 == null ? 0 : paint2.getNativeInstance(), this.mDensity, patch.getDensity());
        resetEntity(entity, paint2);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawPath(Path path, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF((float) getWidth(), (float) getHeight()), path);
        if (!path.isSimplePath || path.rects == null) {
            nDrawPath(this.mNativeCanvasWrapper, path.readOnlyNI(), paint.getNativeInstance());
        } else {
            nDrawRegion(this.mNativeCanvasWrapper, path.rects.mNativeRegion, paint.getNativeInstance());
        }
        resetEntity(entity, paint);
    }

    @Override // android.graphics.Canvas
    public final void drawPicture(Picture picture) {
        picture.endRecording();
        int restoreCount = save();
        picture.draw(this);
        restoreToCount(restoreCount);
    }

    @Override // android.graphics.Canvas
    public final void drawPicture(Picture picture, Rect dst) {
        save();
        translate((float) dst.left, (float) dst.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale(((float) dst.width()) / ((float) picture.getWidth()), ((float) dst.height()) / ((float) picture.getHeight()));
        }
        drawPicture(picture);
        restore();
    }

    @Override // android.graphics.Canvas
    public final void drawPicture(Picture picture, RectF dst) {
        save();
        translate(dst.left, dst.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale(dst.width() / ((float) picture.getWidth()), dst.height() / ((float) picture.getHeight()));
        }
        drawPicture(picture);
        restore();
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawPoint(float x, float y, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, null);
        nDrawPoint(this.mNativeCanvasWrapper, x, y, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawPoints(float[] pts, int offset, int count, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, null);
        nDrawPoints(this.mNativeCanvasWrapper, pts, offset, count, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawPoints(float[] pts, Paint paint) {
        drawPoints(pts, 0, pts.length, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    @Deprecated
    public final void drawPosText(char[] text, int index, int count, float[] pos, Paint paint) {
        if (index < 0 || index + count > text.length || count * 2 > pos.length) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < count; i++) {
            drawText(text, index + i, 1, pos[i * 2], pos[(i * 2) + 1], paint);
        }
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    @Deprecated
    public final void drawPosText(String text, float[] pos, Paint paint) {
        drawPosText(text.toCharArray(), 0, text.length(), pos, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawRect(float left, float top, float right, float bottom, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF(left, top, right, bottom));
        nDrawRect(this.mNativeCanvasWrapper, left, top, right, bottom, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawRect(Rect r, Paint paint) {
        drawRect((float) r.left, (float) r.top, (float) r.right, (float) r.bottom, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawRect(RectF rect, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, rect);
        nDrawRect(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawRGB(int r, int g, int b) {
        drawColor(Color.rgb(r, g, b));
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeArea(paint, getRectF(left, top, right, bottom));
        nDrawRoundRect(this.mNativeCanvasWrapper, left, top, right, bottom, rx, ry, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
        drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawDoubleRoundRect(RectF outer, float outerRx, float outerRy, RectF inner, float innerRx, float innerRy, Paint paint) {
        nDrawDoubleRoundRect(this.mNativeCanvasWrapper, outer.left, outer.top, outer.right, outer.bottom, outerRx, outerRy, inner.left, inner.top, inner.right, inner.bottom, innerRx, innerRy, paint.getNativeInstance());
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawDoubleRoundRect(RectF outer, float[] outerRadii, RectF inner, float[] innerRadii, Paint paint) {
        nDrawDoubleRoundRect(this.mNativeCanvasWrapper, outer.left, outer.top, outer.right, outer.bottom, outerRadii, inner.left, inner.top, inner.right, inner.bottom, innerRadii, paint.getNativeInstance());
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
        if ((index | count | (index + count) | ((text.length - index) - count)) >= 0) {
            OppoBaseBaseCanvas.Entity entity = changeText(paint);
            nDrawText(this.mNativeCanvasWrapper, text, index, count, x, y, paint.mBidiFlags, paint.getNativeInstance());
            resetEntity(entity, paint);
            return;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) {
        Paint paint2;
        OppoBaseBaseCanvas.Entity entity;
        if ((start | end | (end - start) | (text.length() - end)) >= 0) {
            OppoBaseBaseCanvas.Entity entity2 = changeText(paint);
            if ((text instanceof String) || (text instanceof SpannedString) || (text instanceof SpannableString)) {
                entity = entity2;
                paint2 = paint;
                nDrawText(this.mNativeCanvasWrapper, text.toString(), start, end, x, y, paint.mBidiFlags, paint.getNativeInstance());
            } else if (text instanceof GraphicsOperations) {
                ((GraphicsOperations) text).drawText(this, start, end, x, y, paint);
                entity = entity2;
                paint2 = paint;
            } else {
                char[] buf = TemporaryBuffer.obtain(end - start);
                TextUtils.getChars(text, start, end, buf, 0);
                nDrawText(this.mNativeCanvasWrapper, buf, 0, end - start, x, y, paint.mBidiFlags, paint.getNativeInstance());
                TemporaryBuffer.recycle(buf);
                entity = entity2;
                paint2 = paint;
            }
            resetEntity(entity, paint2);
            return;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawText(String text, float x, float y, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeText(paint);
        nDrawText(this.mNativeCanvasWrapper, text, 0, text.length(), x, y, paint.mBidiFlags, paint.getNativeInstance());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawText(String text, int start, int end, float x, float y, Paint paint) {
        if ((start | end | (end - start) | (text.length() - end)) >= 0) {
            OppoBaseBaseCanvas.Entity entity = changeText(paint);
            nDrawText(this.mNativeCanvasWrapper, text, start, end, x, y, paint.mBidiFlags, paint.getNativeInstance());
            resetEntity(entity, paint);
            return;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawTextOnPath(char[] text, int index, int count, Path path, float hOffset, float vOffset, Paint paint) {
        if (index >= 0) {
            if (index + count <= text.length) {
                OppoBaseBaseCanvas.Entity entity = changeText(paint);
                nDrawTextOnPath(this.mNativeCanvasWrapper, text, index, count, path.readOnlyNI(), hOffset, vOffset, paint.mBidiFlags, paint.getNativeInstance());
                resetEntity(entity, paint);
                return;
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawTextOnPath(String text, Path path, float hOffset, float vOffset, Paint paint) {
        if (text.length() > 0) {
            OppoBaseBaseCanvas.Entity entity = changeText(paint);
            nDrawTextOnPath(this.mNativeCanvasWrapper, text, path.readOnlyNI(), hOffset, vOffset, paint.mBidiFlags, paint.getNativeInstance());
            resetEntity(entity, paint);
        }
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawTextRun(char[] text, int index, int count, int contextIndex, int contextCount, float x, float y, boolean isRtl, Paint paint) {
        if (text == null) {
            throw new NullPointerException("text is null");
        } else if (paint == null) {
            throw new NullPointerException("paint is null");
        } else if ((index | count | contextIndex | contextCount | (index - contextIndex) | ((contextIndex + contextCount) - (index + count)) | (text.length - (contextIndex + contextCount))) >= 0) {
            OppoBaseBaseCanvas.Entity entity = changeText(paint);
            nDrawTextRun(this.mNativeCanvasWrapper, text, index, count, contextIndex, contextCount, x, y, isRtl, paint.getNativeInstance(), 0);
            resetEntity(entity, paint);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawTextRun(CharSequence text, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint paint) {
        OppoBaseBaseCanvas.Entity entity;
        if (text == null) {
            throw new NullPointerException("text is null");
        } else if (paint == null) {
            throw new NullPointerException("paint is null");
        } else if ((start | end | contextStart | contextEnd | (start - contextStart) | (end - start) | (contextEnd - end) | (text.length() - contextEnd)) >= 0) {
            OppoBaseBaseCanvas.Entity entity2 = changeText(paint);
            if ((text instanceof String) || (text instanceof SpannedString)) {
                entity = entity2;
            } else if (text instanceof SpannableString) {
                entity = entity2;
            } else {
                if (text instanceof GraphicsOperations) {
                    entity = entity2;
                    ((GraphicsOperations) text).drawTextRun(this, start, end, contextStart, contextEnd, x, y, isRtl, paint);
                } else {
                    entity = entity2;
                    if (text instanceof PrecomputedText) {
                        PrecomputedText pt = (PrecomputedText) text;
                        int paraIndex = pt.findParaIndex(start);
                        if (end <= pt.getParagraphEnd(paraIndex)) {
                            int paraStart = pt.getParagraphStart(paraIndex);
                            drawTextRun(pt.getMeasuredParagraph(paraIndex).getMeasuredText(), start - paraStart, end - paraStart, contextStart - paraStart, contextEnd - paraStart, x, y, isRtl, paint);
                            return;
                        }
                    }
                    int contextLen = contextEnd - contextStart;
                    char[] buf = TemporaryBuffer.obtain(contextLen);
                    TextUtils.getChars(text, contextStart, contextEnd, buf, 0);
                    nDrawTextRun(this.mNativeCanvasWrapper, buf, start - contextStart, end - start, 0, contextLen, x, y, isRtl, paint.getNativeInstance(), 0);
                    TemporaryBuffer.recycle(buf);
                }
                resetEntity(entity, paint);
            }
            nDrawTextRun(this.mNativeCanvasWrapper, text.toString(), start, end, contextStart, contextEnd, x, y, isRtl, paint.getNativeInstance());
            resetEntity(entity, paint);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public void drawTextRun(MeasuredText measuredText, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint paint) {
        OppoBaseBaseCanvas.Entity entity = changeText(paint);
        nDrawTextRun(this.mNativeCanvasWrapper, measuredText.getChars(), start, end - start, contextStart, contextEnd - contextStart, x, y, isRtl, paint.getNativeInstance(), measuredText.getNativePtr());
        resetEntity(entity, paint);
    }

    @Override // android.graphics.BaseCanvas, android.graphics.Canvas
    public final void drawVertices(Canvas.VertexMode mode, int vertexCount, float[] verts, int vertOffset, float[] texs, int texOffset, int[] colors, int colorOffset, short[] indices, int indexOffset, int indexCount, Paint paint) {
        checkRange(verts.length, vertOffset, vertexCount);
        if (texs != null) {
            checkRange(texs.length, texOffset, vertexCount);
        }
        if (colors != null) {
            checkRange(colors.length, colorOffset, vertexCount / 2);
        }
        if (indices != null) {
            checkRange(indices.length, indexOffset, indexCount);
        }
        nDrawVertices(this.mNativeCanvasWrapper, mode.nativeInt, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint.getNativeInstance());
    }
}
