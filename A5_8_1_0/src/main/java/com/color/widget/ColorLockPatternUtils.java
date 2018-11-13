package com.color.widget;

import android.content.Context;
import com.color.widget.ColorLockPatternView.Cell;
import com.google.android.collect.Lists;
import java.util.List;

public class ColorLockPatternUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "OppoLockPatternUtils";
    private final Context mContext;

    public ColorLockPatternUtils(Context context) {
        this.mContext = context;
    }

    public static List<Cell> stringToPattern(String string) {
        if (string == null) {
            return null;
        }
        List<Cell> result = Lists.newArrayList();
        byte[] bytes = string.getBytes();
        for (byte b : bytes) {
            byte b2 = (byte) (b - 49);
            result.add(Cell.of(b2 / 3, b2 % 3));
        }
        return result;
    }
}
