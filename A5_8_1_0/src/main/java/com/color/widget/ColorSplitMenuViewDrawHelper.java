package com.color.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.TextPaint;
import com.color.util.ColorContextUtil;
import com.color.view.ColorMenuItemImpl;
import com.color.widget.ColorBottomMenuView.DrawItem;
import com.color.widget.ColorBottomMenuView.DrawItems;

public class ColorSplitMenuViewDrawHelper {
    private int mBgColor;
    private Paint mBgPaint = new Paint();
    private int mCircleMaxWidth = 0;
    private int mCircleMinRadius = 0;
    private int mCircleMinWidth = 0;
    private int mCircleNormalRadius = 0;
    private Context mContext;
    private DrawItems mCurrItems;
    private int mIconWidth = 0;
    private int mTextColor;
    private TextPaint mTextPaint = null;
    private int mTextSize;

    public ColorSplitMenuViewDrawHelper(Context context, DrawItems currItems) {
        this.mContext = context;
        this.mCurrItems = currItems;
        initRedPointResource();
    }

    public void initRedPointResource() {
        this.mBgColor = this.mContext.getResources().getColor(201720884);
        this.mTextColor = ColorContextUtil.getAttrColor(this.mContext, 201392708);
        this.mTextSize = (int) this.mContext.getResources().getDimension(201655503);
        this.mTextPaint = new TextPaint(1);
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setColor(this.mTextColor);
        this.mTextPaint.setTextSize((float) this.mTextSize);
        this.mBgPaint.setAntiAlias(true);
        this.mBgPaint.setColor(this.mBgColor);
        this.mBgPaint.setStyle(Style.FILL);
        this.mCircleNormalRadius = (int) this.mContext.getResources().getDimension(201655500);
        this.mCircleMinRadius = (int) this.mContext.getResources().getDimension(201655499);
        this.mCircleMaxWidth = (int) this.mContext.getResources().getDimension(201655501);
        this.mCircleMinWidth = (int) this.mContext.getResources().getDimension(201655502);
        this.mIconWidth = (int) this.mContext.getResources().getDimension(201655436);
    }

    public void draw(Canvas canvas) {
        drawRedPoint(canvas);
    }

    public void drawRedPoint(Canvas canvas) {
        int count = this.mCurrItems.size();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                int mode;
                DrawItem drawItem = (DrawItem) this.mCurrItems.get(i);
                int num = 0;
                if (drawItem.getMenuItem() instanceof ColorMenuItemImpl) {
                    mode = ((ColorMenuItemImpl) drawItem.getMenuItem()).getPointMode();
                    num = ((ColorMenuItemImpl) drawItem.getMenuItem()).getPointNumber();
                } else {
                    mode = 0;
                }
                int right = (drawItem.getLeft() + (((drawItem.getRight() - drawItem.getLeft()) - this.mIconWidth) / 2)) + this.mIconWidth;
                int top = (int) drawItem.getIconY();
                switch (mode) {
                    case 1:
                        drawPointOnly(canvas, top, right);
                        break;
                    case 2:
                        drawPointWithNumber(canvas, top, right, num);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public boolean isLayoutRtl() {
        return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
    }

    public void drawPointOnly(Canvas canvas, int top, int right) {
        if (isLayoutRtl()) {
            right -= this.mIconWidth;
        }
        canvas.drawCircle((float) right, (float) (this.mCircleMinRadius + top), (float) this.mCircleMinRadius, this.mBgPaint);
    }

    public void drawPointWithNumber(Canvas canvas, int top, int right, int number) {
        if (number > 0) {
            String numSring = String.valueOf(number);
            RectF r = new RectF();
            r.left = (float) (right - this.mCircleMinRadius);
            r.top = (float) top;
            r.bottom = r.top + ((float) (this.mCircleNormalRadius * 2));
            int pointWidth = 0;
            if (number < 10) {
                pointWidth = this.mCircleNormalRadius * 2;
            }
            if (number >= 10 && number < 100) {
                pointWidth = this.mCircleMinWidth;
            }
            if (number >= 100) {
                numSring = "99+";
                pointWidth = this.mCircleMaxWidth;
            }
            if (isLayoutRtl()) {
                r.left = (float) ((right - this.mIconWidth) - (pointWidth - this.mCircleMinRadius));
            }
            r.right = r.left + ((float) pointWidth);
            canvas.drawRoundRect(r, (float) this.mCircleNormalRadius, (float) this.mCircleNormalRadius, this.mBgPaint);
            FontMetricsInt fmi = this.mTextPaint.getFontMetricsInt();
            canvas.drawText(numSring, (float) ((int) (r.left + (((r.right - r.left) - ((float) ((int) this.mTextPaint.measureText(numSring)))) / 2.0f))), (float) (((int) (((r.top + r.bottom) - ((float) fmi.ascent)) - ((float) fmi.descent))) / 2), this.mTextPaint);
        }
    }
}
