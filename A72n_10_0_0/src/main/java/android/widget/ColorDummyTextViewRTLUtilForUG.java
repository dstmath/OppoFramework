package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.Layout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;

public class ColorDummyTextViewRTLUtilForUG implements IColorTextViewRTLUtilForUG {
    private static volatile ColorDummyTextViewRTLUtilForUG sInstance = null;

    public static ColorDummyTextViewRTLUtilForUG getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyTextViewRTLUtilForUG.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyTextViewRTLUtilForUG();
                }
            }
        }
        return sInstance;
    }

    ColorDummyTextViewRTLUtilForUG() {
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    public Layout.Alignment getLayoutAlignmentForTextView(Layout.Alignment alignment, Context context, TextView textView) {
        return alignment;
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    public TextDirectionHeuristic getTextDirectionHeuristicForTextView(boolean defaultIsRtl) {
        if (defaultIsRtl) {
            return TextDirectionHeuristics.FIRSTSTRONG_RTL;
        }
        return TextDirectionHeuristics.FIRSTSTRONG_LTR;
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    public void initRtlParameter(Resources res) {
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    public boolean getOppoSupportRtl() {
        return false;
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    public boolean getDirectionAnyRtl() {
        return false;
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    public boolean getTextViewStart() {
        return false;
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    public boolean hasRtlSupportForView(Context context) {
        return context != null && context.getApplicationInfo().hasRtlSupport();
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    public void updateRtlParameterForUG(String[] availableLocales, Configuration newConfig) {
    }

    @Override // android.widget.IColorTextViewRTLUtilForUG
    @Deprecated
    public void updateRtlParameterForUG(Resources res, Configuration newConfig) {
    }
}
