package android.text;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface ITextJustificationHooks extends IOppoCommonFeature {
    public static final ITextJustificationHooks DEFAULT = new ITextJustificationHooks() {
        /* class android.text.ITextJustificationHooks.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default ITextJustificationHooks getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.ITextJustificationHooks;
    }

    default void setTextViewParaSpacing(Object textview, float paraSpacing, Layout layout) {
    }

    default float getTextViewParaSpacing(Object textview) {
        return 0.0f;
    }

    default float getTextViewDefaultLineMulti(Object textview, float pxSize, float oriValue) {
        return oriValue;
    }

    default float calculateAddedWidth(float justifyWidth, float width, int spaces, int start, int end, boolean charsValid, char[] chars, CharSequence text, int mstart) {
        return 0.0f;
    }

    default float getLayoutParaSpacingAdded(StaticLayout layout, Object builder, boolean moreChars, CharSequence source, int endPos) {
        return 0.0f;
    }

    default void setLayoutParaSpacingAdded(Object object, float paraSpacing) {
    }

    default boolean lineShouldIncludeFontPad(boolean firstLine, StaticLayout layout) {
        return firstLine;
    }

    default boolean lineNeedMultiply(boolean needMultiply, boolean addLastLineLineSpacing, boolean lastLine, StaticLayout layout) {
        return needMultiply && (addLastLineLineSpacing || !lastLine);
    }
}
