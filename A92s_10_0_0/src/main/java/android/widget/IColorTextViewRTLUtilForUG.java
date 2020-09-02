package android.widget;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.Layout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;

public interface IColorTextViewRTLUtilForUG extends IOppoCommonFeature {
    public static final IColorTextViewRTLUtilForUG DEFAULT = new IColorTextViewRTLUtilForUG() {
        /* class android.widget.IColorTextViewRTLUtilForUG.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorTextViewRTLUtilForUG getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorTextViewRTLUtilForUG;
    }

    default Layout.Alignment getLayoutAlignmentForTextView(Layout.Alignment alignment, Context context, TextView textView) {
        return alignment;
    }

    default TextDirectionHeuristic getTextDirectionHeuristicForTextView(boolean defaultIsRtl) {
        if (defaultIsRtl) {
            return TextDirectionHeuristics.FIRSTSTRONG_RTL;
        }
        return TextDirectionHeuristics.FIRSTSTRONG_LTR;
    }

    default void initRtlParameter(Resources res) {
    }

    default boolean getOppoSupportRtl() {
        return false;
    }

    default boolean getDirectionAnyRtl() {
        return false;
    }

    default boolean getTextViewStart() {
        return false;
    }

    default boolean hasRtlSupportForView(Context context) {
        return context != null && context.getApplicationInfo().hasRtlSupport();
    }

    default void updateRtlParameterForUG(String[] availableLocales, Configuration newConfig) {
    }

    @Deprecated
    default void updateRtlParameterForUG(Resources res, Configuration newConfig) {
    }
}
