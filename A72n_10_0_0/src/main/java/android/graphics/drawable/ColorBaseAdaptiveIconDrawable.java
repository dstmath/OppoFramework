package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.Rect;

public class ColorBaseAdaptiveIconDrawable extends Drawable {
    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean hookOnBoundsChange(Rect bounds) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean hookDraw(Canvas canvas) {
        return false;
    }

    /* access modifiers changed from: protected */
    public Path hookGetIconMask() {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean hookGetIntrinsicHeight() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean hookGetIntrinsicWidth() {
        return false;
    }

    public float getForegroundScalePercent(Drawable drawable) {
        return 0.0f;
    }
}
