package android.graphics.drawable;

import android.common.ColorFrameworkFactory;
import android.graphics.Bitmap;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.OppoBaseBaseCanvas;
import android.graphics.OppoBaseBitmap;
import android.graphics.OppoBaseColorFilter;
import android.graphics.PorterDuffColorFilter;
import com.color.util.ColorTypeCastingHelper;

public abstract class OppoBaseDrawable {
    int mFilterColor;
    private boolean mHasSetColor = false;
    boolean shouldRestoreFilterColor = false;

    public void changeVectorColor(boolean isRestore) {
    }

    /* access modifiers changed from: package-private */
    public void handleVectorDraw(Canvas canvas, ColorFilter colorFilter) {
        if (!isDarkMode(canvas)) {
            return;
        }
        if (colorFilter != null) {
            changeFilter(colorFilter);
        } else if (!hasSetColor()) {
            changeVectorColor(false);
            setHasSetColor(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void restoreVectorDraw(Canvas canvas, ColorFilter colorFilter) {
        if (!isDarkMode(canvas)) {
            return;
        }
        if (colorFilter != null) {
            restoreFilter(colorFilter);
        } else if (hasSetColor()) {
            changeVectorColor(true);
            setHasSetColor(false);
        }
    }

    private void changeFilter(ColorFilter colorFilter) {
        if (colorFilter instanceof BlendModeColorFilter) {
            this.mFilterColor = ((BlendModeColorFilter) colorFilter).getColor();
            this.shouldRestoreFilterColor = true;
            ColorFrameworkFactory.getInstance().getColorDarkModeManager().changeColorFilterInDarkMode(colorFilter);
        } else if (colorFilter instanceof PorterDuffColorFilter) {
            this.mFilterColor = ((PorterDuffColorFilter) colorFilter).getColor();
            this.shouldRestoreFilterColor = true;
            ColorFrameworkFactory.getInstance().getColorDarkModeManager().changeColorFilterInDarkMode(colorFilter);
        }
    }

    private void restoreFilter(ColorFilter colorFilter) {
        if (colorFilter != null && this.shouldRestoreFilterColor) {
            this.shouldRestoreFilterColor = false;
            OppoBaseColorFilter baseColorFilter = (OppoBaseColorFilter) ColorTypeCastingHelper.typeCasting(OppoBaseColorFilter.class, colorFilter);
            if (baseColorFilter != null) {
                baseColorFilter.setColor(this.mFilterColor);
            }
        }
    }

    private boolean isDarkMode(Canvas canvas) {
        OppoBaseBaseCanvas baseCanvas;
        if (canvas == null || (baseCanvas = (OppoBaseBaseCanvas) ColorTypeCastingHelper.typeCasting(OppoBaseBaseCanvas.class, canvas)) == null) {
            return false;
        }
        return baseCanvas.isDarkMode();
    }

    /* access modifiers changed from: protected */
    public void setBitmapIsViewSrc(Bitmap bitmap, boolean value) {
        OppoBaseBitmap baseBitmap;
        if (bitmap != null && (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) != null && baseBitmap.isCanvasBaseBitmap()) {
            baseBitmap.setIsViewSrc(value);
        }
    }

    /* access modifiers changed from: protected */
    public void setHasSetColor(boolean hasSetColor) {
        this.mHasSetColor = hasSetColor;
    }

    /* access modifiers changed from: protected */
    public boolean hasSetColor() {
        return this.mHasSetColor;
    }
}
