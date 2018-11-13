package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ColorMultiChoiceGridView extends GridView {
    private boolean mChoiceModeByLongPressEnabled = false;

    public ColorMultiChoiceGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorMultiChoiceGridView(Context context) {
        super(context);
    }

    public ColorMultiChoiceGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    boolean performLongPress(View child, int longPressPosition, long longPressId, float x, float y) {
        if (this.mChoiceModeByLongPressEnabled) {
            return super.performLongPress(child, longPressPosition, longPressId, x, y);
        }
        int temp = this.mChoiceMode;
        this.mChoiceMode = 0;
        boolean result = super.performLongPress(child, longPressPosition, longPressId, x, y);
        this.mChoiceMode = temp;
        return result;
    }

    public void setChoiceModeByLongPressEnabled(boolean enabled) {
        this.mChoiceModeByLongPressEnabled = enabled;
    }

    public boolean isChoiceModeByLongPressEnabled() {
        return this.mChoiceModeByLongPressEnabled;
    }
}
