package android.graphics.drawable;

import android.app.uxicons.CustomAdaptiveIconConfig;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;

public class ColorAdaptiveIconDrawable extends AdaptiveIconDrawable {
    private static final float DEFAULT_VIEW_PORT_SCALE = 0.6666667f;
    private static final float EXTRA_INSET_PERCENTAGE = 0.25f;
    private Rect mBackgroundPositionBounds = new Rect();
    private Rect mBackgroundSizeBounds = new Rect();
    private Canvas mColorCanvas;
    private ColorLayerState mColorLayerState;
    private Bitmap mColorLayersBitmap;
    private Path mColorMask;
    private Path mColorMaskScaleOnly;
    private Paint mColorPaint = new Paint(7);
    private CustomAdaptiveIconConfig mConfig;
    private Matrix mCustomMatrix = new Matrix();
    private Rect mForegroundPositionBounds = new Rect();
    private Rect mForegroundSizeBounds = new Rect();

    ColorAdaptiveIconDrawable(AdaptiveIconDrawable.LayerState layerState, Resources res, CustomAdaptiveIconConfig config) {
        super(layerState, res);
        this.mConfig = config;
        init();
    }

    public ColorAdaptiveIconDrawable(Drawable backgroundDrawable, Drawable foregroundDrawable, CustomAdaptiveIconConfig config) {
        super(backgroundDrawable, foregroundDrawable);
        this.mConfig = config;
        init();
    }

    public float getForegroundScalePercent() {
        float fgScale = this.mConfig.getForegroundScalePercent() * this.mConfig.getScalePercent() * 1.0f;
        if (this.mConfig.getIsPlatformDrawable() || !this.mConfig.getIsAdaptiveIconDrawable()) {
            return fgScale;
        }
        return (1.0f * fgScale) / DEFAULT_VIEW_PORT_SCALE;
    }

    private void init() {
        CustomAdaptiveIconConfig customAdaptiveIconConfig = this.mConfig;
        if (customAdaptiveIconConfig != null && customAdaptiveIconConfig.getCustomMask() != null) {
            this.mColorCanvas = new Canvas();
            this.mColorMask = new Path(this.mConfig.getCustomMask());
            this.mColorMaskScaleOnly = new Path(this.mColorMask);
            this.mColorLayerState = new ColorLayerState(this.mLayerState, this.mConfig);
        }
    }

    private boolean drawIcon(Canvas canvas) {
        if (this.mColorLayersBitmap == null || this.mColorCanvas == null || this.mConfig.getCustomMask() == null) {
            return false;
        }
        canvas.save();
        Bitmap layersBitmap = this.mColorLayersBitmap;
        this.mColorCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        this.mColorCanvas.setBitmap(layersBitmap);
        if (getBackground() != null) {
            this.mColorCanvas.save();
            this.mColorCanvas.translate((float) this.mBackgroundPositionBounds.left, (float) this.mBackgroundPositionBounds.top);
            getBackground().draw(this.mColorCanvas);
            this.mColorCanvas.translate((float) (-this.mBackgroundPositionBounds.left), (float) (-this.mBackgroundPositionBounds.top));
            this.mColorCanvas.restore();
        }
        if (getForeground() != null) {
            this.mColorCanvas.save();
            this.mColorCanvas.translate((float) this.mForegroundPositionBounds.left, (float) this.mForegroundPositionBounds.top);
            getForeground().draw(this.mColorCanvas);
            this.mColorCanvas.translate((float) (-this.mForegroundPositionBounds.left), (float) (-this.mForegroundPositionBounds.top));
            this.mColorCanvas.restore();
        }
        this.mColorPaint.setShader(new BitmapShader(layersBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        if (this.mColorMaskScaleOnly != null) {
            Rect bounds = getBounds();
            canvas.translate((float) bounds.left, (float) bounds.top);
            canvas.drawPath(this.mColorMaskScaleOnly, this.mColorPaint);
            canvas.translate((float) (-bounds.left), (float) (-bounds.top));
        }
        canvas.restore();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean onDrawableBoundsChange(Rect bounds) {
        if (this.mConfig.getCustomMask() == null) {
            return false;
        }
        try {
            int sizeOffset = updateBounds(bounds);
            updateDrawableBounds();
            updateMaskBounds(bounds, sizeOffset);
            updateParams(bounds);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int updateBounds(Rect b) {
        int bgSize = (int) Math.ceil((double) (((float) b.width()) * this.mConfig.getScalePercent()));
        int offset = (int) (((float) (b.width() - bgSize)) / 2.0f);
        this.mBackgroundPositionBounds.set(offset, offset, bgSize + offset, bgSize + offset);
        this.mBackgroundSizeBounds.set(0, 0, bgSize, bgSize);
        int fgSize = (int) Math.ceil((double) (((float) b.width()) * this.mConfig.getScalePercent() * this.mConfig.getForegroundScalePercent()));
        int fgOffset = (int) (((float) (b.width() - fgSize)) / 2.0f);
        this.mForegroundSizeBounds.set(0, 0, fgSize, fgSize);
        this.mForegroundPositionBounds.set(fgOffset, fgOffset, fgSize + fgOffset, fgSize + fgOffset);
        if (!this.mConfig.getIsPlatformDrawable() && this.mConfig.getIsAdaptiveIconDrawable()) {
            updateThirdPartAdaptiveIconDrawableBound(this.mBackgroundSizeBounds);
            updateThirdPartAdaptiveIconDrawableBound(this.mForegroundSizeBounds);
        }
        return offset;
    }

    private void updateThirdPartAdaptiveIconDrawableBound(Rect bounds) {
        int cX = bounds.width() / 2;
        int cY = bounds.height() / 2;
        int insetWidth = (int) (((float) bounds.width()) / 1.3333334f);
        int insetHeight = (int) (((float) bounds.height()) / 1.3333334f);
        bounds.set(cX - insetWidth, cY - insetHeight, cX + insetWidth, cY + insetHeight);
    }

    private void updateDrawableBounds() {
        AdaptiveIconDrawable.ChildDrawable bg = this.mLayerState.mChildren[0];
        if (!(bg == null || bg.mDrawable == null)) {
            bg.mDrawable.setBounds(this.mBackgroundSizeBounds);
        }
        AdaptiveIconDrawable.ChildDrawable fg = this.mLayerState.mChildren[1];
        if (fg != null && fg.mDrawable != null) {
            fg.mDrawable.setBounds(this.mForegroundSizeBounds);
        }
    }

    private void updateMaskBounds(Rect b, int sizeOffset) {
        this.mCustomMatrix.reset();
        this.mCustomMatrix.setScale(((((float) b.width()) * this.mConfig.getScalePercent()) * 1.0f) / 150.0f, ((((float) b.height()) * this.mConfig.getScalePercent()) * 1.0f) / 150.0f);
        this.mColorMask.transform(this.mCustomMatrix, this.mColorMaskScaleOnly);
        this.mColorMaskScaleOnly.offset((float) sizeOffset, (float) sizeOffset);
    }

    private void updateParams(Rect bounds) {
        Bitmap bitmap = this.mColorLayersBitmap;
        if (!(bitmap != null && bitmap.getWidth() == bounds.width() && this.mColorLayersBitmap.getHeight() == this.mColorLayersBitmap.getHeight())) {
            this.mColorLayersBitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        }
        this.mColorPaint.setAntiAlias(true);
        this.mColorPaint.setShader(null);
    }

    static class ColorLayerState extends Drawable.ConstantState {
        private int mChangingConfigurations;
        private CustomAdaptiveIconConfig mConfig;
        private AdaptiveIconDrawable.LayerState mParentLayerState;

        ColorLayerState(AdaptiveIconDrawable.LayerState layerState, CustomAdaptiveIconConfig config) {
            this.mParentLayerState = layerState;
            this.mConfig = config;
            this.mChangingConfigurations = layerState.getChangingConfigurations();
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new ColorAdaptiveIconDrawable(this.mParentLayerState, (Resources) null, this.mConfig);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new ColorAdaptiveIconDrawable(this.mParentLayerState, res, this.mConfig);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }
    }

    @Override // android.graphics.drawable.AdaptiveIconDrawable, android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        CustomAdaptiveIconConfig customAdaptiveIconConfig = this.mConfig;
        if (customAdaptiveIconConfig == null || customAdaptiveIconConfig.getCustomMask() == null) {
            return super.getConstantState();
        }
        return this.mColorLayerState;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.ColorBaseAdaptiveIconDrawable
    public boolean hookDraw(Canvas canvas) {
        return drawIcon(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.ColorBaseAdaptiveIconDrawable
    public boolean hookOnBoundsChange(Rect bounds) {
        return onDrawableBoundsChange(bounds);
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.ColorBaseAdaptiveIconDrawable
    public Path hookGetIconMask() {
        return this.mColorMask;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.ColorBaseAdaptiveIconDrawable
    public boolean hookGetIntrinsicHeight() {
        return this.mConfig != null;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.ColorBaseAdaptiveIconDrawable
    public boolean hookGetIntrinsicWidth() {
        return this.mConfig != null;
    }

    @Override // android.graphics.drawable.ColorBaseAdaptiveIconDrawable
    public float getForegroundScalePercent(Drawable drawable) {
        if (drawable instanceof ColorAdaptiveIconDrawable) {
            return ((ColorAdaptiveIconDrawable) drawable).getForegroundScalePercent();
        }
        return 0.0f;
    }
}
