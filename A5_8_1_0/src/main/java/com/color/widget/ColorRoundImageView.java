package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import oppo.R;

public class ColorRoundImageView extends ImageView {
    private static final int BORDER_CIRCLE_WIDTH = 5;
    private static final int CIRCLE = 0;
    private static final int DEFAULT_BORDER_RADIUS = 1;
    private static final int ROUND = 1;
    private static final int SHADOW = 2;
    private int bSize;
    private Bitmap mBitmap;
    private int mBitmapHeight;
    private Paint mBitmapPaint;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBorderRadius;
    private final RectF mBorderRect;
    private Context mContext;
    private Drawable mDefaultDrawable;
    private Drawable mDrawable;
    private boolean mHasBorder;
    private boolean mHasDefaultPic;
    private Matrix mMatrix;
    private Paint mOutCircle;
    private float mRadius;
    private RectF mRoundRect;
    private Bitmap mShadowBitmap;
    private BitmapShader mShadowBitmapShader;
    private int mShadowBorderWidth;
    private Drawable mShadowDrawable;
    private int mShadowDrawableHeight;
    private int mShadowDrawableWidth;
    private final RectF mShadowInsideRect;
    private int mSourceDrawableHeight;
    private int mSourceDrawableWidth;
    private RectF mSourceRect;
    private int mType;
    private int mWidth;
    private float scale;

    public ColorRoundImageView(Context context) {
        super(context);
        this.mType = 0;
        this.mBorderRect = new RectF();
        this.mShadowInsideRect = new RectF();
        this.mMatrix = new Matrix();
        this.mContext = context;
        this.mBitmapPaint = new Paint();
        this.mBitmapPaint.setAntiAlias(true);
        this.mOutCircle = new Paint();
        this.mOutCircle.setAntiAlias(true);
        this.mOutCircle.setColor(getResources().getColor(201720875));
        this.mOutCircle.setStrokeWidth(1.0f);
        this.mOutCircle.setStyle(Style.STROKE);
        this.mWidth = getResources().getDimensionPixelSize(201655438);
        setupShader(getDrawable());
    }

    public ColorRoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mType = 0;
        this.mBorderRect = new RectF();
        this.mShadowInsideRect = new RectF();
        this.mMatrix = new Matrix();
        this.mBitmapPaint = new Paint();
        this.mBitmapPaint.setAntiAlias(true);
        this.mBitmapPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
        this.mContext = context;
        this.mOutCircle = new Paint();
        this.mOutCircle.setAntiAlias(true);
        this.mOutCircle.setStrokeWidth(1.0f);
        this.mOutCircle.setStyle(Style.STROKE);
        this.mShadowDrawable = context.getDrawable(201852179);
        this.mShadowDrawableWidth = this.mShadowDrawable.getIntrinsicWidth();
        this.mShadowDrawableHeight = this.mShadowDrawable.getIntrinsicHeight();
        this.mSourceDrawableWidth = (int) context.getResources().getDimension(201655538);
        this.mSourceDrawableHeight = this.mSourceDrawableWidth;
        this.mOutCircle.setColor(getResources().getColor(201720875));
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorRoundImageView);
        this.mBorderRadius = a.getDimensionPixelSize(1, (int) TypedValue.applyDimension(1, 1.0f, getResources().getDisplayMetrics()));
        this.mType = a.getInt(0, 0);
        this.mHasBorder = a.getBoolean(2, false);
        this.mHasDefaultPic = a.getBoolean(3, true);
        initShadow();
        setupShader(getDrawable());
        a.recycle();
    }

    public void initShadow() {
        this.mBorderRect.set(0.0f, 0.0f, (float) this.mShadowDrawableWidth, (float) this.mShadowDrawableHeight);
        this.mShadowBorderWidth = this.mShadowDrawableWidth - this.mSourceDrawableWidth;
        this.mShadowInsideRect.set(this.mBorderRect);
        this.mShadowInsideRect.inset((float) (this.mShadowBorderWidth / 2), (float) (this.mShadowBorderWidth / 2));
    }

    public void setHasBorder(boolean b) {
        this.mHasBorder = b;
    }

    public void setHasDefaultPic(boolean b) {
        this.mHasDefaultPic = b;
    }

    public ColorRoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mType = 0;
        this.mBorderRect = new RectF();
        this.mShadowInsideRect = new RectF();
        initShadow();
    }

    protected void onDraw(Canvas canvas) {
        this.scale = 1.0f;
        if (this.mBitmap != null) {
            switch (this.mType) {
                case 0:
                    this.bSize = Math.min(this.mBitmap.getWidth(), this.mBitmap.getHeight());
                    this.scale = (((float) this.mWidth) * 1.0f) / ((float) this.bSize);
                    break;
                case 1:
                    this.scale = Math.max((((float) getWidth()) * 1.0f) / ((float) this.mBitmap.getWidth()), (((float) getHeight()) * 1.0f) / ((float) this.mBitmap.getHeight()));
                    break;
                case 2:
                    this.scale = Math.max((((float) getWidth()) * 1.0f) / ((float) this.mShadowDrawableWidth), (((float) getHeight()) * 1.0f) / ((float) this.mShadowDrawableHeight));
                    this.mMatrix.reset();
                    this.mMatrix.setScale(this.scale, this.scale);
                    this.mShadowBitmapShader.setLocalMatrix(this.mMatrix);
                    this.mBitmapPaint.setShader(this.mShadowBitmapShader);
                    canvas.drawRect(this.mRoundRect, this.mBitmapPaint);
                    return;
            }
            this.mMatrix.setScale(this.scale, this.scale);
            if (this.mBitmapShader != null) {
                this.mBitmapShader.setLocalMatrix(this.mMatrix);
                this.mBitmapPaint.setShader(this.mBitmapShader);
            }
        }
        if (this.mType == 0) {
            if (this.mHasBorder) {
                canvas.drawCircle(this.mRadius, this.mRadius, this.mRadius, this.mBitmapPaint);
                canvas.drawCircle(this.mRadius, this.mRadius, this.mRadius - 0.5f, this.mOutCircle);
            } else {
                canvas.drawCircle(this.mRadius, this.mRadius, this.mRadius, this.mBitmapPaint);
            }
        } else if (this.mType == 1) {
            canvas.drawRoundRect(this.mRoundRect, (float) this.mBorderRadius, (float) this.mBorderRadius, this.mBitmapPaint);
        }
    }

    public Bitmap creatBitmapWithShadow() {
        updateShaderMatrix();
        this.mShadowBitmapShader = new BitmapShader(this.mBitmap, TileMode.CLAMP, TileMode.CLAMP);
        this.mShadowBitmapShader.setLocalMatrix(this.mMatrix);
        this.mBitmapPaint.setShader(this.mShadowBitmapShader);
        Bitmap bitmap = Bitmap.createBitmap(this.mShadowDrawableWidth, this.mShadowDrawableHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.mBorderRadius = this.mSourceDrawableWidth / 2;
        canvas.drawRoundRect(this.mShadowInsideRect, (float) this.mBorderRadius, (float) this.mBorderRadius, this.mBitmapPaint);
        this.mShadowDrawable.setBounds(0, 0, this.mShadowDrawableWidth, this.mShadowDrawableHeight);
        this.mShadowDrawable.draw(canvas);
        return bitmap;
    }

    private void updateShaderMatrix() {
        this.mMatrix.reset();
        float bitmapScaleX = (float) ((((double) this.mSourceDrawableWidth) * 1.0d) / ((double) this.mBitmapWidth));
        float bitmapScaleY = (float) ((((double) this.mSourceDrawableHeight) * 1.0d) / ((double) this.mBitmapHeight));
        if (bitmapScaleX <= 1.0f) {
            bitmapScaleX = 1.0f;
        }
        if (bitmapScaleY <= 1.0f) {
            bitmapScaleY = 1.0f;
        }
        float scale = Math.max(bitmapScaleX, bitmapScaleY);
        float dx = (((float) this.mSourceDrawableWidth) - (((float) this.mBitmapWidth) * scale)) * 0.5f;
        float dy = (((float) this.mSourceDrawableHeight) - (((float) this.mBitmapHeight) * scale)) * 0.5f;
        this.mMatrix.setScale(scale, scale);
        this.mMatrix.postTranslate((float) (((int) (dx + 0.5f)) + (this.mShadowBorderWidth / 2)), (float) (((int) (dy + 0.5f)) + (this.mShadowBorderWidth / 2)));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mType == 1 || this.mType == 2) {
            this.mRoundRect = new RectF(0.0f, 0.0f, (float) getWidth(), (float) getHeight());
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mType == 0) {
            int minWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
            if (minWidth == 0) {
                minWidth = this.mWidth;
            }
            this.mWidth = minWidth;
            this.mRadius = ((float) this.mWidth) / 2.0f;
            setMeasuredDimension(this.mWidth, this.mWidth);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        setupShader(drawable);
    }

    public void setImageResource(int resId) {
        super.setImageResource(resId);
        setupShader(this.mContext.getResources().getDrawable(resId));
    }

    private void setupShader(Drawable d) {
        this.mDrawable = getDrawable();
        if (this.mDrawable == null || d == null) {
            if (this.mDefaultDrawable == null && this.mHasDefaultPic) {
                Drawable drawable = getResources().getDrawable(201852138);
                this.mDefaultDrawable = drawable;
                this.mDrawable = drawable;
            } else {
                return;
            }
        } else if (this.mDrawable != d) {
            this.mDrawable = d;
        }
        this.mBitmapWidth = this.mDrawable.getIntrinsicWidth();
        this.mBitmapHeight = this.mDrawable.getIntrinsicHeight();
        this.mBitmap = drawableToBitmap(this.mDrawable);
        if (this.mType == 2) {
            this.mShadowBitmap = creatBitmapWithShadow();
            this.mShadowBitmapShader = new BitmapShader(this.mShadowBitmap, TileMode.CLAMP, TileMode.CLAMP);
        }
        if (this.mBitmap != null) {
            this.mBitmapShader = new BitmapShader(this.mBitmap, TileMode.CLAMP, TileMode.CLAMP);
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int h = drawable.getIntrinsicHeight();
        int w = drawable.getIntrinsicWidth();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public void setType(int type) {
        if (this.mType != type) {
            this.mType = type;
            invalidate();
        }
    }

    public void setRoundRadius(int radius) {
        this.mBorderRadius = radius;
    }
}
