package com.android.internal.app;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

public interface IColorAlertControllerEuclidManager extends IOppoCommonFeature {
    public static final IColorAlertControllerEuclidManager DEFAULT = new IColorAlertControllerEuclidManager() {
        /* class com.android.internal.app.IColorAlertControllerEuclidManager.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorAlertControllerEuclidManager getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAlertControllerEuclidManager;
    }

    default AlertController getAlertController(Context context, DialogInterface di, Window window) {
        return new AlertController(context, di, window);
    }

    default void setListStyle(ListView listView, boolean isSingleChoice, boolean isMultiChoice) {
        if (isSingleChoice) {
            listView.setChoiceMode(1);
        } else if (isMultiChoice) {
            listView.setChoiceMode(2);
        }
    }

    default View getConvertView(View target, int position, int count) {
        return null;
    }
}
