package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import java.io.IOException;
import oppo.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorMatrixDrawable extends DrawableWrapper {
    private static final float[] defaultcolorArray = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private static final float[] disablecolorArray = new float[]{0.1f, 0.5f, 0.1f, 0.0f, 0.0f, 0.1f, 0.5f, 0.1f, 0.0f, 0.0f, 0.1f, 0.5f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f};
    private static final float[] pressedcolorArray = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f};
    private ColorMatrixState mState;

    static final class ColorMatrixState extends DrawableWrapperState {
        private static final int DEFAULT_PAINT_FLAGS = 6;
        ColorMatrix mColorMatrix = new ColorMatrix(ColorMatrixDrawable.defaultcolorArray);
        final Paint mPaint = new Paint(6);

        ColorMatrixState(ColorMatrixState orig) {
            super(orig, null);
            if (orig != null) {
                this.mColorMatrix = orig.mColorMatrix;
            }
        }

        public Drawable newDrawable(Resources res) {
            return new ColorMatrixDrawable(this, res, null);
        }
    }

    /* synthetic */ ColorMatrixDrawable(ColorMatrixState state, Resources res, ColorMatrixDrawable -this2) {
        this(state, res);
    }

    ColorMatrixDrawable() {
        this(new ColorMatrixState(null), null);
    }

    public ColorMatrixDrawable(Drawable drawable, ColorMatrix martrix) {
        this(new ColorMatrixState(null), null);
        this.mState.mColorMatrix = martrix;
        setDrawable(drawable);
    }

    public ColorMatrixDrawable(Drawable drawable) {
        this(new ColorMatrixState(null), null);
        setDrawable(drawable);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.ColorMatrixDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        inflateChildDrawable(r, parser, attrs, theme);
        verifyRequiredAttributes(a);
        a.recycle();
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (getDrawable() == null && ((getDrawable() instanceof BitmapDrawable) ^ 1) != 0) {
            if (this.mState.mThemeAttrs == null || this.mState.mThemeAttrs[1] == 0) {
                throw new XmlPullParserException(a.getPositionDescription() + ": <color-matrix> tag requires a 'bitmapdrawable' attribute or " + "child tag defining a bitmapdrawable");
            }
        }
    }

    void updateStateFromTypedArray(TypedArray a) {
        ColorMatrixState state = this.mState;
        boolean hasCustomMatrix = false;
        String strMatrix = a.getString(0);
        if (strMatrix != null) {
            String[] strs = strMatrix.split(",");
            if (strs.length >= 20) {
                float[] tempColorArray = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
                int i = 0;
                while (i < 20 && i < strs.length) {
                    tempColorArray[i] = Float.parseFloat(strs[i].trim());
                    i++;
                }
                state.mColorMatrix = new ColorMatrix(tempColorArray);
                hasCustomMatrix = true;
            }
        }
        int mode = a.getInt(2, -1);
        if (!hasCustomMatrix) {
            if (mode == 0) {
                state.mColorMatrix = new ColorMatrix(defaultcolorArray);
            } else if (mode == 1) {
                state.mColorMatrix = new ColorMatrix(pressedcolorArray);
            } else if (mode == 2) {
                state.mColorMatrix = new ColorMatrix(disablecolorArray);
            }
        }
        Drawable dr = a.getDrawable(1);
        if (dr != null) {
            setDrawable(dr);
        }
    }

    public void applyTheme(Theme t) {
        ColorMatrixState state = this.mState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.ColorMatrixDrawable);
                try {
                    updateStateFromTypedArray(a);
                    verifyRequiredAttributes(a);
                    a.recycle();
                } catch (XmlPullParserException e) {
                    throw new RuntimeException(e);
                } catch (Throwable th) {
                    a.recycle();
                }
            }
            super.applyTheme(t);
        }
    }

    protected boolean onLevelChange(int level) {
        super.onLevelChange(level);
        invalidateSelf();
        return true;
    }

    public void draw(Canvas canvas) {
        Drawable dr = getDrawable();
        if (dr instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
            canvas.save();
            this.mState.mPaint.setColorFilter(new ColorMatrixColorFilter(this.mState.mColorMatrix));
            Rect tempRect = dr.copyBounds();
            if (tempRect != null) {
                canvas.drawBitmap(bitmap, null, tempRect, this.mState.mPaint);
            } else {
                canvas.drawBitmap(bitmap, 0.0f, 0.0f, this.mState.mPaint);
            }
            this.mState.mPaint.setColorFilter(null);
            canvas.restore();
        }
    }

    public int getIntrinsicWidth() {
        return getDrawable().getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        return getDrawable().getIntrinsicHeight();
    }

    private ColorMatrixDrawable(ColorMatrixState state, Resources res) {
        super(state, res);
        this.mState = state;
    }
}
