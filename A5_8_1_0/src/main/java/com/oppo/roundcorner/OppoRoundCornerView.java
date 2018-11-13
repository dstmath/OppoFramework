package com.oppo.roundcorner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.View;

public class OppoRoundCornerView extends View {
    private static final int ROTATE_180 = 180;
    public static final String TAG = "OPPORoundCorner";
    private Context mContext;
    private Paint mPaint = new Paint();
    private int mWindowPos = 1;
    private Bitmap mbmp = null;
    private int mflag = 0;
    private int mrcVisible = 0;

    public OppoRoundCornerView(Context context, int windowpos) {
        super(context);
        this.mPaint.setAntiAlias(true);
        this.mContext = context;
        this.mWindowPos = windowpos;
        Resources res = this.mContext.getResources();
        if (this.mWindowPos == 1) {
            this.mbmp = BitmapFactory.decodeResource(res, 17305750);
        } else if (this.mWindowPos == 2) {
            this.mbmp = BitmapFactory.decodeResource(res, 17305751);
        } else if (this.mWindowPos == 3) {
            this.mbmp = BitmapFactory.decodeResource(res, 17305752);
        } else if (this.mWindowPos == 4) {
            this.mbmp = BitmapFactory.decodeResource(res, 17305753);
        } else {
            Log.e("OPPORoundCorner", "did not find the resource,window position=%d" + windowpos);
        }
    }

    public void setFlag(int flag) {
        this.mflag = flag;
    }

    public void setCornerType(int windowpos) {
        if (this.mWindowPos != windowpos) {
            this.mWindowPos = windowpos;
            Resources res = this.mContext.getResources();
            Bitmap tempbtimap = null;
            if (this.mbmp != null) {
                tempbtimap = this.mbmp;
            }
            if (this.mWindowPos == 1) {
                this.mbmp = BitmapFactory.decodeResource(res, 17305750);
            } else if (this.mWindowPos == 2) {
                this.mbmp = BitmapFactory.decodeResource(res, 17305751);
            } else if (this.mWindowPos == 3) {
                this.mbmp = BitmapFactory.decodeResource(res, 17305752);
            } else if (this.mWindowPos == 4) {
                this.mbmp = BitmapFactory.decodeResource(res, 17305753);
            } else {
                Log.e("OPPORoundCorner", "did not find the resource,window position=%d" + windowpos);
                return;
            }
            if (this.mflag != windowpos) {
                Matrix matrix = new Matrix();
                matrix.postRotate(180.0f);
                int width = this.mbmp.getWidth();
                int height = this.mbmp.getHeight();
                Bitmap temp = this.mbmp;
                this.mbmp = Bitmap.createBitmap(temp, 0, 0, width, height, matrix, true);
                if (temp != null && temp.isRecycled()) {
                    temp.recycle();
                }
            }
            if (!(this.mbmp == tempbtimap || tempbtimap == null || (tempbtimap.isRecycled() ^ 1) == 0)) {
                tempbtimap.recycle();
            }
        }
    }

    public int getBitmapHeight() {
        if (this.mbmp != null) {
            return this.mbmp.getHeight();
        }
        return 0;
    }

    public int getBitmapWidth() {
        if (this.mbmp != null) {
            return this.mbmp.getWidth();
        }
        return 0;
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        this.mrcVisible = visibility;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            Log.e("OPPORoundCorner", "onDraw canvas is null");
            return;
        }
        this.mPaint.setStyle(Style.FILL);
        this.mPaint.setColor(-16777216);
        this.mPaint.setAntiAlias(true);
        canvas.drawBitmap(this.mbmp, 0.0f, 0.0f, this.mPaint);
    }
}
