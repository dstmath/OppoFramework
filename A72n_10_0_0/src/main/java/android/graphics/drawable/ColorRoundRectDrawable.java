package android.graphics.drawable;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

public class ColorRoundRectDrawable extends Drawable {
    Bitmap mBitmap;
    int mBottom;
    Drawable mDrawable;
    int mLeft;
    Paint mPaint;
    float mRadius;
    RectF mRectF;
    int mRight;
    int mTop;

    public ColorRoundRectDrawable(Drawable drawable, float radius) {
        this(drawable, radius, 0, 0, 1080, 2340);
    }

    public ColorRoundRectDrawable(Drawable drawable, float radius, int left, int top, int right, int bottom) {
        this.mRadius = 0.0f;
        this.mDrawable = drawable;
        this.mRadius = radius;
        this.mLeft = left;
        this.mTop = top;
        this.mRight = right;
        this.mBottom = bottom;
        if (this.mDrawable != null) {
            this.mBitmap = drawableToBitmap(drawable);
            this.mPaint = new Paint();
            this.mPaint.setAntiAlias(true);
            this.mPaint.setDither(true);
            this.mPaint.setShader(new BitmapShader(this.mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        RectF rectF = this.mRectF;
        if (rectF == null) {
            this.mRectF = new RectF((float) left, (float) top, (float) right, (float) bottom);
        } else {
            rectF.set((float) left, (float) top, (float) right, (float) bottom);
        }
    }

    public void setColorRoundBounds(int left, int top, int right, int bottom, float radius) {
        RectF rectF = this.mRectF;
        if (rectF == null) {
            this.mRectF = new RectF((float) left, (float) top, (float) right, (float) bottom);
        } else {
            rectF.set((float) left, (float) top, (float) right, (float) bottom);
        }
        this.mRadius = radius;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mBitmap.getHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mBitmap.getWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        RectF rectF = this.mRectF;
        float f = this.mRadius;
        canvas.drawRoundRect(rectF, f, f, this.mPaint);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    /* access modifiers changed from: package-private */
    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap.Config config;
        int w = this.mRight - this.mLeft;
        int h = this.mBottom - this.mTop;
        if (drawable.getOpacity() != -1) {
            config = Bitmap.Config.ARGB_8888;
        } else {
            config = Bitmap.Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(this.mLeft, this.mTop, this.mRight, this.mBottom);
        drawable.draw(canvas);
        return bitmap;
    }
}
