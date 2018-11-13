package com.color.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ColorVectorNumber extends ImageView {
    public static final int COLON = 10;
    private static final int[] ID_LIST = new int[]{201852232, 201852233, 201852234, 201852235, 201852236, 201852237, 201852238, 201852239, 201852240, 201852241, 201852242};
    private static final String TAG = "ColorVectorNumber";

    public ColorVectorNumber(Context context) {
        super(context);
    }

    public ColorVectorNumber(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ColorVectorNumber(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setVectorItem(int i) {
        if (i < 0 || i >= ID_LIST.length) {
            Log.e(TAG, "The Number (" + i + ") is not Support!");
        } else {
            setImageResource(ID_LIST[i]);
        }
    }
}
