package android.view;

import android.content.Context;
import android.util.Log;
import com.color.util.ColorContextUtil;

public class ColorViewConfigHelper extends ColorDummyViewConfigHelper {
    private int mColorOverDist;
    private boolean mIsColorStyle;

    public ColorViewConfigHelper(Context context) {
        this.mColorOverDist = context.getResources().getDisplayMetrics().heightPixels;
        this.mIsColorStyle = ColorContextUtil.isColorStyle(context);
    }

    public int getScaledOverscrollDistance(int dist) {
        if (this.mIsColorStyle) {
            Log.d("TestOverScroll", "getScaledOverscrollDistance: a mColorOverDist: " + this.mColorOverDist);
            return this.mColorOverDist;
        }
        Log.d("TestOverScroll", "getScaledOverscrollDistance: b");
        return dist;
    }

    public int getScaledOverflingDistance(int dist) {
        if (this.mIsColorStyle) {
            return this.mColorOverDist;
        }
        return dist;
    }

    public int calcRealOverScrollDist(int dist, int scrollY) {
        if (!this.mIsColorStyle) {
            return dist;
        }
        Log.d("TestOverScroll", "calcRealOverScrollDist: a-scrollY: " + scrollY);
        return (int) ((((float) dist) * (1.0f - ((((float) Math.abs(scrollY)) * 1.0f) / ((float) this.mColorOverDist)))) / 3.0f);
    }

    public int calcRealOverScrollDist(int dist, int scrollY, int range) {
        if (!this.mIsColorStyle || (scrollY >= 0 && scrollY <= range)) {
            return dist;
        }
        int overScrollY = scrollY;
        if (scrollY > range) {
            overScrollY = scrollY - range;
        }
        Log.d("TestOverScroll", "calcRealOverScrollDist: b-scrollY: " + scrollY);
        return (int) ((((float) dist) * (1.0f - ((((float) Math.abs(overScrollY)) * 1.0f) / ((float) this.mColorOverDist)))) / 3.0f);
    }
}
