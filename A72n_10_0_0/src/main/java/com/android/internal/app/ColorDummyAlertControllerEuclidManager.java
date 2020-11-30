package com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

public class ColorDummyAlertControllerEuclidManager implements IColorAlertControllerEuclidManager {
    private static volatile ColorDummyAlertControllerEuclidManager sInstance;

    private ColorDummyAlertControllerEuclidManager() {
    }

    public static ColorDummyAlertControllerEuclidManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyAlertControllerEuclidManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyAlertControllerEuclidManager();
                }
            }
        }
        return sInstance;
    }

    @Override // com.android.internal.app.IColorAlertControllerEuclidManager
    public AlertController getAlertController(Context context, DialogInterface di, Window window) {
        return new AlertController(context, di, window);
    }

    @Override // com.android.internal.app.IColorAlertControllerEuclidManager
    public void setListStyle(ListView listView, boolean isSingleChoice, boolean isMultiChoice) {
        if (isSingleChoice) {
            listView.setChoiceMode(1);
        } else if (isMultiChoice) {
            listView.setChoiceMode(2);
        }
    }

    @Override // com.android.internal.app.IColorAlertControllerEuclidManager
    public View getConvertView(View target, int position, int count) {
        return null;
    }
}
