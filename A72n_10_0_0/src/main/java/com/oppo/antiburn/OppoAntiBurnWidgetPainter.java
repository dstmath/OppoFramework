package com.oppo.antiburn;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.graphics.ColorUtils;
import java.util.List;

public class OppoAntiBurnWidgetPainter {
    public static final String ANTIBURN_DARK_LEVEL_1 = "DARK_LEVEL_1";
    public static final String ANTIBURN_DARK_LEVEL_2 = "DARK_LEVEL_2";
    public static final String ANTIBURN_DARK_LEVEL_3 = "DARK_LEVEL_3";
    public static final String ANTIBURN_DARK_LEVEL_4 = "DARK_LEVEL_4";
    public static final String ANTIBURN_DARK_LEVEL_5 = "DARK_LEVEL_5";
    public static final String ANTIBURN_DARK_LEVEL_6 = "DARK_LEVEL_6";
    public static final String ANTIBURN_DARK_LEVEL_7 = "DARK_LEVEL_7";
    public static final String ANTIBURN_DARK_LEVEL_8 = "DARK_LEVEL_8";
    public static final String ANTIBURN_DARK_LEVEL_9 = "DARK_LEVEL_9";
    public static final String ANTIBURN_NULL = "ANTIBURN_NULL";
    static final ColorMatrix DRAKEN_MATRIX_1 = new ColorMatrix(new float[]{0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    static final ColorMatrix DRAKEN_MATRIX_2 = new ColorMatrix(new float[]{0.2f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    static final ColorMatrix DRAKEN_MATRIX_3 = new ColorMatrix(new float[]{0.3f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    static final ColorMatrix DRAKEN_MATRIX_4 = new ColorMatrix(new float[]{0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    static final ColorMatrix DRAKEN_MATRIX_5 = new ColorMatrix(new float[]{0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    static final ColorMatrix DRAKEN_MATRIX_6 = new ColorMatrix(new float[]{0.6f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.6f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.6f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    static final ColorMatrix DRAKEN_MATRIX_7 = new ColorMatrix(new float[]{0.7f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    static final ColorMatrix DRAKEN_MATRIX_8 = new ColorMatrix(new float[]{0.8f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    static final ColorMatrix DRAKEN_MATRIX_9 = new ColorMatrix(new float[]{0.9f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    public static final String TAG = "OppoAntiBurnWidgetPainter";
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_1 = new ColorMatrixColorFilter(DRAKEN_MATRIX_1);
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_2 = new ColorMatrixColorFilter(DRAKEN_MATRIX_2);
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_3 = new ColorMatrixColorFilter(DRAKEN_MATRIX_3);
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_4 = new ColorMatrixColorFilter(DRAKEN_MATRIX_4);
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_5 = new ColorMatrixColorFilter(DRAKEN_MATRIX_5);
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_6 = new ColorMatrixColorFilter(DRAKEN_MATRIX_6);
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_7 = new ColorMatrixColorFilter(DRAKEN_MATRIX_7);
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_8 = new ColorMatrixColorFilter(DRAKEN_MATRIX_8);
    static final ColorMatrixColorFilter TRANSFORM_TO_DRAKEN_MATRIX_9 = new ColorMatrixColorFilter(DRAKEN_MATRIX_9);
    private OppoAntiBurnConfigHolder mConfigHolder;

    private OppoAntiBurnWidgetPainter() {
        this.mConfigHolder = OppoAntiBurnConfigHolder.getInstance();
    }

    /* access modifiers changed from: private */
    public static class Holder {
        private static final OppoAntiBurnWidgetPainter INSTANCE = new OppoAntiBurnWidgetPainter();

        private Holder() {
        }
    }

    public static OppoAntiBurnWidgetPainter getInstance() {
        return Holder.INSTANCE;
    }

    public List fetchOPFDConfigActionsForCurView(View view) {
        if (view != null) {
            try {
                if (this.mConfigHolder.hasSpecialViewsConfig()) {
                    return this.mConfigHolder.getOPFDActionCmds(view);
                }
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return null;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean executeOPFDConfigAction(View view, Canvas canvas, List actions) {
        try {
            for (Object action : actions) {
                executeAction(view, action);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void executeAction(View v, Object oneAction) {
        if (v != null && (oneAction instanceof String)) {
            String str = (String) oneAction;
            char c = 65535;
            int hashCode = str.hashCode();
            if (hashCode != -1627264811) {
                switch (hashCode) {
                    case 1146625613:
                        if (str.equals(ANTIBURN_DARK_LEVEL_1)) {
                            c = '\t';
                            break;
                        }
                        break;
                    case 1146625614:
                        if (str.equals(ANTIBURN_DARK_LEVEL_2)) {
                            c = '\b';
                            break;
                        }
                        break;
                    case 1146625615:
                        if (str.equals(ANTIBURN_DARK_LEVEL_3)) {
                            c = 7;
                            break;
                        }
                        break;
                    case 1146625616:
                        if (str.equals(ANTIBURN_DARK_LEVEL_4)) {
                            c = 6;
                            break;
                        }
                        break;
                    case 1146625617:
                        if (str.equals(ANTIBURN_DARK_LEVEL_5)) {
                            c = 5;
                            break;
                        }
                        break;
                    case 1146625618:
                        if (str.equals(ANTIBURN_DARK_LEVEL_6)) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1146625619:
                        if (str.equals(ANTIBURN_DARK_LEVEL_7)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1146625620:
                        if (str.equals(ANTIBURN_DARK_LEVEL_8)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1146625621:
                        if (str.equals(ANTIBURN_DARK_LEVEL_9)) {
                            c = 1;
                            break;
                        }
                        break;
                }
            } else if (str.equals(ANTIBURN_NULL)) {
                c = 0;
            }
            switch (c) {
                case 0:
                default:
                    return;
                case 1:
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_9, DRAKEN_MATRIX_9);
                    return;
                case 2:
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_8, DRAKEN_MATRIX_8);
                    return;
                case 3:
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_7, DRAKEN_MATRIX_7);
                    return;
                case 4:
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_6, DRAKEN_MATRIX_6);
                    return;
                case 5:
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_5, DRAKEN_MATRIX_5);
                    return;
                case 6:
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_4, DRAKEN_MATRIX_4);
                    return;
                case 7:
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_3, DRAKEN_MATRIX_3);
                    return;
                case '\b':
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_2, DRAKEN_MATRIX_2);
                    return;
                case '\t':
                    setColorFilter(v, TRANSFORM_TO_DRAKEN_MATRIX_1, DRAKEN_MATRIX_1);
                    return;
            }
        }
    }

    private void setColorFilter(View view, ColorMatrixColorFilter filter, ColorMatrix matrix) {
        if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(filter);
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            int curColor = textView.getCurrentTextColor();
            if (isVeryHighLightColor(curColor)) {
                textView.setTextColor(transformColor(curColor, matrix));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int transformColor(int color, ColorMatrix colorMatrix) {
        if (colorMatrix == null) {
            return color;
        }
        float[] colorArray = colorMatrix.getArray();
        return Color.argb(Color.alpha(color), (int) ((colorArray[0] * ((float) Color.red(color))) + colorArray[4]), (int) ((colorArray[6] * ((float) Color.green(color))) + colorArray[9]), (int) ((colorArray[12] * ((float) Color.blue(color))) + colorArray[14]));
    }

    private boolean isVeryHighLightColor(int color) {
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        return lab[0] > 80.0d;
    }
}
