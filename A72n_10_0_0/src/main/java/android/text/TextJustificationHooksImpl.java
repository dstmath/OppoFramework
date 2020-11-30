package android.text;

import android.content.Context;
import android.widget.TextView;
import com.color.util.ColorContextUtil;

public class TextJustificationHooksImpl implements ITextJustificationHooks {
    private static final String TAG = "TextJustificationHooksImpl";
    public float mBuilderParaSpacingAdded = 0.0f;
    public boolean mLayoutSpecifiedParaSpacing = false;
    public float mTextViewParaSpacing = 0.0f;

    public void setTextViewParaSpacing(Object view, float paraSpacing, Layout layout) {
        if (view != null && (view instanceof TextView)) {
            TextView textview = (TextView) view;
            textview.hashCode();
            if (paraSpacing != this.mTextViewParaSpacing) {
                this.mTextViewParaSpacing = paraSpacing;
                if (layout != null) {
                    textview.nullLayouts();
                    textview.requestLayout();
                    textview.invalidate();
                }
            }
        }
    }

    public float getTextViewParaSpacing(Object view) {
        return this.mTextViewParaSpacing;
    }

    public float getTextViewDefaultLineMulti(Object view, float pxSize, float oriValue) {
        if (view == null || !(view instanceof TextView)) {
            return oriValue;
        }
        Context context = ((TextView) view).getContext();
        if (!ColorContextUtil.isColorStyle(context)) {
            return oriValue;
        }
        int spSize = px2sp(context, pxSize);
        if (spSize == 10) {
            return 1.3f;
        }
        if (spSize == 12) {
            return 1.15f;
        }
        if (spSize == 14) {
            return 1.2f;
        }
        if (spSize == 16 || spSize == 18 || spSize == 20 || spSize == 22 || spSize == 24) {
            return 1.1f;
        }
        return oriValue;
    }

    private int px2sp(Context context, float pxValue) {
        return (int) ((pxValue / context.getResources().getDisplayMetrics().scaledDensity) + 0.5f);
    }

    public float calculateAddedWidth(float justifyWidth, float width, int spaces, int start, int end, boolean charsValid, char[] chars, CharSequence text, int mstart) {
        int hans = countStretchableHan(0, end, charsValid, chars, text, mstart);
        if (hans != 0) {
            return (justifyWidth - width) / ((float) (hans + spaces));
        }
        return 0.0f;
    }

    private int countStretchableHan(int start, int end, boolean charsValid, char[] chars, CharSequence text, int mstart) {
        int count = 0;
        for (int i = start; i < end; i++) {
            if (isStretchableHan(charsValid ? chars[i] : text.charAt(i + mstart))) {
                count++;
            }
        }
        return count;
    }

    private boolean isStretchableHan(int ch) {
        return ch >= 19968 && ch <= 40869;
    }

    public float getLayoutParaSpacingAdded(StaticLayout layout, Object builder, boolean moreChars, CharSequence source, int endPos) {
        layout.hashCode();
        this.mLayoutSpecifiedParaSpacing = false;
        float builderParaSpacingAdded = 0.0f;
        if (moreChars && source.charAt(endPos - 1) == '\n') {
            builderParaSpacingAdded = this.mBuilderParaSpacingAdded;
            if (builderParaSpacingAdded > 0.0f) {
                this.mLayoutSpecifiedParaSpacing = true;
            }
        }
        return builderParaSpacingAdded;
    }

    public void setLayoutParaSpacingAdded(Object object, float paraSpacing) {
        this.mBuilderParaSpacingAdded = paraSpacing;
    }

    public boolean lineShouldIncludeFontPad(boolean firstLine, StaticLayout layout) {
        return firstLine || this.mLayoutSpecifiedParaSpacing;
    }

    public boolean lineNeedMultiply(boolean needMultiply, boolean addLastLineLineSpacing, boolean lastLine, StaticLayout layout) {
        return needMultiply && (addLastLineLineSpacing || !lastLine) && !this.mLayoutSpecifiedParaSpacing;
    }
}
