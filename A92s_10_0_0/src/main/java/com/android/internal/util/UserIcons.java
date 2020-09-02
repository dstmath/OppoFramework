package com.android.internal.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import com.android.internal.R;

public class UserIcons {
    private static final int[] USER_ICON_COLORS = {R.color.user_icon_1, R.color.user_icon_2, R.color.user_icon_3, R.color.user_icon_4, R.color.user_icon_5, R.color.user_icon_6, R.color.user_icon_7, R.color.user_icon_8};

    public static Bitmap convertToBitmap(Drawable icon) {
        if (icon == null) {
            return null;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, width, height);
        icon.draw(canvas);
        return bitmap;
    }

    public static Drawable getDefaultUserIcon(Resources resources, int userId, boolean light) {
        int colorResId = light ? R.color.user_icon_default_white : R.color.user_icon_default_gray;
        if (userId != -10000) {
            int[] iArr = USER_ICON_COLORS;
            colorResId = iArr[userId % iArr.length];
        }
        Drawable icon = resources.getDrawable(201852336, null).mutate();
        icon.setColorFilter(resources.getColor(colorResId, null), PorterDuff.Mode.XOR);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        return icon;
    }
}
