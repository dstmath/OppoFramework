package com.android.server.storage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.View;
import com.android.server.display.OppoBrightUtils;
import com.color.util.ColorContextUtil;
import com.color.util.ColorUnitConversionUtils;

public class ColorDataMonitorView extends View {
    private static final int BOUNDARY_BOTTOM = 48;
    private static final int BOUNDARY_WIDTH = 54;
    private static final int DATA_LOW_THRED_WIDTH = 267;
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
    private boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(this.mPictureWidth, widthMeasureSpec), resolveSize(this.mPictureHeight, heightMeasureSpec));
    }

    protected void onDraw(Canvas canvas) {
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
        this.mPicture.setBounds(x - (this.mPictureWidth / 2), y - (this.mPictureHeight / 2), (this.mPictureWidth / 2) + x, (this.mPictureHeight / 2) + y);
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
        paint1.setTextAlign(Align.LEFT);
        canvas.drawText(this.mContext.getResources().getString(201590198), OppoBrightUtils.MIN_LUX_LIMITI, (float) (0 - paint1.getFontMetricsInt().top), paint1);
        Paint paint2 = new Paint();
        paint2.setTextSize(this.mContext.getResources().getDimension(201654408));
        paint2.setColor(ColorContextUtil.getAttrColor(this.mContext, 201392714));
        paint2.setTextAlign(Align.LEFT);
        canvas.drawText(this.mContext.getResources().getString(201590199), OppoBrightUtils.MIN_LUX_LIMITI, (float) (availableHeight - paint2.getFontMetricsInt().bottom), paint2);
        Paint paint3 = new Paint();
        paint3.setTextSize(this.mContext.getResources().getDimension(201654408));
        paint3.setColor(ColorContextUtil.getAttrColor(this.mContext, 201392714));
        paint3.setTextAlign(Align.RIGHT);
        int baseLineY3 = availableHeight - paint3.getFontMetricsInt().bottom;
        canvas.drawText(byteCountToDisplaySize(this.mShowTotalData), (float) availableWidth, (float) baseLineY3, paint3);
        Paint paint5 = new Paint();
        paint5.setAntiAlias(true);
        paint5.setColor(LINE_COLOR);
        paint5.setStyle(Style.STROKE);
        paint5.setStrokeWidth(2.0f);
        float[] fArr = new float[4];
        paint5.setPathEffect(new DashPathEffect(new float[]{4.0f, 4.0f, 4.0f, 4.0f}, OppoBrightUtils.MIN_LUX_LIMITI));
        Path path = new Path();
        path.moveTo((float) (((long) (availableWidth - 54)) - ((267 * dataFreeSpace) / this.mDataLowThreshold)), (float) (availableHeight - 48));
        path.lineTo((float) (((long) (availableWidth - 54)) - ((267 * dataFreeSpace) / this.mDataLowThreshold)), (float) ((availableHeight - 360) - 48));
        canvas.drawPath(path, paint5);
    }

    private String byteCountToDisplaySize(long size) {
        String dispalySize = "";
        if (this.mContext == null) {
            return dispalySize;
        }
        try {
            return new ColorUnitConversionUtils(this.mContext).getUnitValue(size);
        } catch (Exception e) {
            Slog.e(TAG, "byteCountToDisplaySize e:" + e);
            return dispalySize;
        }
    }
}
