package com.android.server.storage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.View;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorContextUtil;
import com.color.util.ColorUnitConversionUtils;

public class ColorDataMonitorView extends View {
    private static final int BOUNDARY_BOTTOM = 52;
    private static final int BOUNDARY_WIDTH = 54;
    private static final int DATA_LOW_THRED_WIDTH = 267;
    private static final float DEFAULT_DENSITY = 3.0f;
    private static final int DIAMETER_CIRCLE = 18;
    private static final float FLOAT_2 = 2.0f;
    private static final float FLOAT_5 = 5.0f;
    private static final float FLOAT_9 = 9.0f;
    private static final int HEIGHT_LINE = 378;
    private static final int INT_4 = 4;
    private static final int LINE_COLOR = -3080192;
    private static final int RADIUS_CIRCLE = 9;
    private static final String TAG = "DeviceStorageMonitor";
    private static final long THRESHOLD_DATA_LOW = 838860800;
    private Context mContext;
    private long mDataLowThreshold;
    private boolean mOppoDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private PaintFlagsDrawFilter mPaintFlagsDrawFilter;
    private Drawable mPicture;
    private int mPictureHeight;
    private int mPictureWidth;
    private long mShowTotalData;

    public ColorDataMonitorView(Context context) {
        super(context);
    }

    public ColorDataMonitorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0, 3);
        this.mPicture = context.getDrawable(201852214);
        this.mPictureWidth = this.mPicture.getIntrinsicWidth();
        this.mPictureHeight = this.mPicture.getIntrinsicHeight();
        ColorStorageUtils.getDataThreshold();
        this.mShowTotalData = ColorStorageUtils.getShowTotalData();
        this.mDataLowThreshold = ColorStorageUtils.getDataLowThreshold();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(this.mPictureWidth, widthMeasureSpec), resolveSize(this.mPictureHeight, heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        canvas.setDrawFilter(this.mPaintFlagsDrawFilter);
        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();
        if (this.mOppoDebug) {
            Slog.d(TAG, "availableWidth= " + availableWidth + " availableHeight = " + availableHeight);
        }
        int x = availableWidth / 2;
        int y = availableHeight / 2;
        float scale = 1.0f;
        if (availableWidth < this.mPictureWidth || availableHeight < this.mPictureHeight) {
            scale = Math.min(((float) availableWidth) / ((float) (this.mPictureWidth + 4)), ((float) availableHeight) / ((float) this.mPictureHeight));
            canvas.save();
            canvas.scale(scale, scale, (float) x, (float) y);
            if (this.mOppoDebug) {
                Slog.d(TAG, "should scale, scale = " + scale + ", mPictureWidth= " + this.mPictureWidth + " mPictureHeight = " + this.mPictureHeight);
            }
        }
        Drawable drawable = this.mPicture;
        int i = this.mPictureWidth;
        int i2 = this.mPictureHeight;
        drawable.setBounds(x - (i / 2), y - (i2 / 2), (i / 2) + x, (i2 / 2) + y);
        this.mPicture.draw(canvas);
        this.mPictureWidth = (int) (((float) this.mPictureWidth) * scale);
        this.mPictureHeight = (int) (((float) this.mPictureHeight) * scale);
        long dataFreeSpace = ColorStorageUtils.getLastDataFreeSpace();
        if (this.mOppoDebug) {
            Slog.d(TAG, "mPictureWidth= " + this.mPictureWidth + " mPictureHeight = " + this.mPictureHeight + " dataFreeSpace = " + dataFreeSpace);
        }
        Paint paint1 = new Paint();
        paint1.setTextSize(this.mContext.getResources().getDimension(201654408));
        paint1.setColor(ColorContextUtil.getAttrColor(this.mContext, 201392714));
        paint1.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(this.mContext.getResources().getString(201590198), 0.0f, (float) (0 - paint1.getFontMetricsInt().top), paint1);
        Paint paint2 = new Paint();
        paint2.setTextSize(this.mContext.getResources().getDimension(201654408));
        paint2.setColor(ColorContextUtil.getAttrColor(this.mContext, 201392714));
        paint2.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(this.mContext.getResources().getString(201590199), 0.0f, (float) (availableHeight - paint2.getFontMetricsInt().bottom), paint2);
        Paint paint3 = new Paint();
        paint3.setTextSize(this.mContext.getResources().getDimension(201654408));
        paint3.setColor(ColorContextUtil.getAttrColor(this.mContext, 201392714));
        paint3.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(byteCountToDisplaySize(this.mShowTotalData), (float) availableWidth, (float) (availableHeight - paint3.getFontMetricsInt().bottom), paint3);
        Paint paint5 = new Paint();
        paint5.setAntiAlias(true);
        paint5.setColor(LINE_COLOR);
        paint5.setStyle(Paint.Style.STROKE);
        paint5.setStrokeWidth(FLOAT_2);
        paint5.setPathEffect(new DashPathEffect(new float[]{4.0f, 4.0f, 4.0f, 4.0f}, 0.0f));
        Path path = new Path();
        path.moveTo((float) (((long) (availableWidth - 54)) - ((dataFreeSpace * 267) / this.mDataLowThreshold)), ((float) availableHeight) - ((getResources().getDisplayMetrics().density * 52.0f) / DEFAULT_DENSITY));
        path.lineTo((float) (((long) (availableWidth - 54)) - ((267 * dataFreeSpace) / this.mDataLowThreshold)), ((float) availableHeight) - ((getResources().getDisplayMetrics().density * 412.0f) / DEFAULT_DENSITY));
        canvas.drawPath(path, paint5);
    }

    private String byteCountToDisplaySize(long size) {
        Context context = this.mContext;
        if (context == null) {
            return "";
        }
        try {
            return new ColorUnitConversionUtils(context).getUnitValue(size);
        } catch (Exception e) {
            Slog.e(TAG, "byteCountToDisplaySize e:" + e);
            return "";
        }
    }
}
