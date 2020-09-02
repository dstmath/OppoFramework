package android.graphics;

import android.common.ColorFrameworkFactory;
import com.color.util.ColorTypeCastingHelper;

public abstract class OppoBaseBaseCanvas {

    public static class Entity {
        public boolean isDarkMode;
        public Paint newPaint;
        public RealPaintState realPaintState;
    }

    public static class RealPaintState {
        public int color;
        public ColorFilter colorFilter;
        public int porterDuffColor;
        public Shader shader;
    }

    public static RectF getRectF(float width, float height) {
        return new RectF(0.0f, 0.0f, width, height);
    }

    public static RectF getRectF(float left, float top, float right, float bottom) {
        return new RectF(left, top, right, bottom);
    }

    public static RectF getRectF(Rect rect) {
        return new RectF(rect);
    }

    /* access modifiers changed from: protected */
    public void setIsCanvasBaseBitmap(Bitmap bitmap, boolean value) {
        OppoBaseBitmap baseBitmap;
        if (bitmap != null && (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) != null) {
            baseBitmap.setIsCanvasBaseBitmap(value);
        }
    }

    public boolean isHardwareAccelerated() {
        return false;
    }

    public boolean isDarkMode() {
        return ColorFrameworkFactory.getInstance().getColorDarkModeManager().isInDarkMode(isHardwareAccelerated());
    }

    public int getWidth() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int[] changeColors(int[] colors) {
        return colors;
    }

    /* access modifiers changed from: protected */
    public Entity changeBitmap(Paint paint, Bitmap bitmap, RectF rectF) {
        boolean isDarkMode = isDarkMode();
        Entity entity = new Entity();
        RealPaintState realPaintState = null;
        if (isDarkMode) {
            realPaintState = ColorFrameworkFactory.getInstance().getColorDarkModeManager().getRealPaintState(paint);
            entity.newPaint = ColorFrameworkFactory.getInstance().getColorDarkModeManager().getPaintWhenDrawBitmap(paint, bitmap, rectF);
        }
        entity.realPaintState = realPaintState;
        entity.isDarkMode = isDarkMode;
        return entity;
    }

    /* access modifiers changed from: protected */
    public int changeColor(int color) {
        if (isDarkMode()) {
            return ColorFrameworkFactory.getInstance().getColorDarkModeManager().changeWhenDrawColor(color, isDarkMode());
        }
        return color;
    }

    /* access modifiers changed from: protected */
    public Entity changePatch(NinePatch patch, Paint paint, RectF rectF) {
        boolean isDarkMode = isDarkMode();
        Entity entity = new Entity();
        RealPaintState realPaintState = null;
        if (isDarkMode) {
            realPaintState = ColorFrameworkFactory.getInstance().getColorDarkModeManager().getRealPaintState(paint);
            entity.newPaint = ColorFrameworkFactory.getInstance().getColorDarkModeManager().getPaintWhenDrawPatch(patch, paint, rectF);
        }
        entity.realPaintState = realPaintState;
        entity.isDarkMode = isDarkMode;
        return entity;
    }

    /* access modifiers changed from: protected */
    public Entity changeArea(Paint paint, RectF rectF) {
        boolean isDarkMode = isDarkMode();
        RealPaintState realPaintState = null;
        if (isDarkMode) {
            realPaintState = ColorFrameworkFactory.getInstance().getColorDarkModeManager().getRealPaintState(paint);
            ColorFrameworkFactory.getInstance().getColorDarkModeManager().changePaintWhenDrawArea(paint, rectF);
        }
        Entity entity = new Entity();
        entity.realPaintState = realPaintState;
        entity.isDarkMode = isDarkMode;
        return entity;
    }

    /* access modifiers changed from: protected */
    public Entity changeArea(Paint paint, RectF rectF, Path path) {
        boolean isDarkMode = isDarkMode();
        RealPaintState realPaintState = null;
        if (isDarkMode) {
            realPaintState = ColorFrameworkFactory.getInstance().getColorDarkModeManager().getRealPaintState(paint);
            ColorFrameworkFactory.getInstance().getColorDarkModeManager().changePaintWhenDrawArea(paint, rectF, path);
        }
        Entity entity = new Entity();
        entity.realPaintState = realPaintState;
        entity.isDarkMode = isDarkMode;
        return entity;
    }

    /* access modifiers changed from: protected */
    public Entity changeText(Paint paint) {
        boolean isDarkMode = isDarkMode();
        RealPaintState realPaintState = null;
        if (isDarkMode) {
            realPaintState = ColorFrameworkFactory.getInstance().getColorDarkModeManager().getRealPaintState(paint);
            ColorFrameworkFactory.getInstance().getColorDarkModeManager().changePaintWhenDrawText(paint);
        }
        Entity entity = new Entity();
        entity.realPaintState = realPaintState;
        entity.isDarkMode = isDarkMode;
        return entity;
    }

    /* access modifiers changed from: protected */
    public void resetEntity(Entity entity, Paint paint) {
        if (entity.isDarkMode && entity.realPaintState != null && paint != null) {
            ColorFrameworkFactory.getInstance().getColorDarkModeManager().resetRealPaintIfNeed(paint, entity.realPaintState);
        }
    }
}
