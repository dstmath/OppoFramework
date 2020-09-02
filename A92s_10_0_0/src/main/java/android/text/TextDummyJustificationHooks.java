package android.text;

public class TextDummyJustificationHooks implements ITextJustificationHooks {
    @Override // android.text.ITextJustificationHooks
    public void setTextViewParaSpacing(Object textview, float paraSpacing, Layout layout) {
    }

    @Override // android.text.ITextJustificationHooks
    public float getTextViewParaSpacing(Object textview) {
        return 0.0f;
    }

    @Override // android.text.ITextJustificationHooks
    public float getTextViewDefaultLineMulti(Object textview, float pxSize, float oriValue) {
        return oriValue;
    }

    @Override // android.text.ITextJustificationHooks
    public float calculateAddedWidth(float justifyWidth, float width, int spaces, int start, int end, boolean charsValid, char[] chars, CharSequence text, int mstart) {
        return 0.0f;
    }

    @Override // android.text.ITextJustificationHooks
    public float getLayoutParaSpacingAdded(StaticLayout layout, Object builder, boolean moreChars, CharSequence source, int endPos) {
        return 0.0f;
    }

    @Override // android.text.ITextJustificationHooks
    public void setLayoutParaSpacingAdded(Object object, float paraSpacing) {
    }

    @Override // android.text.ITextJustificationHooks
    public boolean lineShouldIncludeFontPad(boolean firstLine, StaticLayout layout) {
        return firstLine;
    }

    @Override // android.text.ITextJustificationHooks
    public boolean lineNeedMultiply(boolean needMultiply, boolean addLastLineLineSpacing, boolean lastLine, StaticLayout layout) {
        return needMultiply && (addLastLineLineSpacing || !lastLine);
    }
}
