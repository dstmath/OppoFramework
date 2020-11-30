package com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import com.color.util.ColorContextUtil;

public class ColorAlertControllerEuclidManger implements IColorAlertControllerEuclidManager {
    private static volatile ColorAlertControllerEuclidManger sInstance;

    private ColorAlertControllerEuclidManger() {
    }

    public static ColorAlertControllerEuclidManger getInstance() {
        if (sInstance == null) {
            synchronized (ColorAlertControllerEuclidManger.class) {
                if (sInstance == null) {
                    sInstance = new ColorAlertControllerEuclidManger();
                }
            }
        }
        return sInstance;
    }

    public AlertController getAlertController(Context context, DialogInterface di, Window window) {
        if (ColorContextUtil.isOppoStyle(context)) {
            return new ColorAlertController(context, di, window);
        }
        return new AlertController(context, di, window);
    }

    public void setListStyle(ListView listView, boolean isSingleChoice, boolean isMultiChoice) {
        boolean isOppoStyle = ColorContextUtil.isOppoStyle(listView.getContext());
        if (isSingleChoice) {
            if (isOppoStyle) {
                listView.setSelector(201852327);
                listView.setItemsCanFocus(false);
            }
            listView.setChoiceMode(1);
        } else if (isMultiChoice) {
            if (isOppoStyle) {
                listView.setSelector(201852327);
                listView.setItemsCanFocus(false);
            }
            listView.setChoiceMode(2);
        }
    }

    public View getConvertView(View target, int position, int count) {
        if (!ColorContextUtil.isOppoStyle(target.getContext())) {
            return null;
        }
        Context context = target.getContext();
        int paddingLeft = context.getResources().getDimensionPixelOffset(201655808);
        int paddingRight = context.getResources().getDimensionPixelOffset(201655809);
        int paddingBottom = context.getResources().getDimensionPixelOffset(201655823);
        int paddingBottomOffset = context.getResources().getDimensionPixelOffset(201655805);
        int paddingTop = context.getResources().getDimensionPixelOffset(201655823);
        int minHeight = context.getResources().getDimensionPixelOffset(201655810);
        if (position == count - 1) {
            target.setMinimumHeight(minHeight + paddingBottomOffset);
            target.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + paddingBottomOffset);
        } else {
            target.setMinimumHeight(minHeight);
            target.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }
        return target;
    }
}
