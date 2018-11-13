package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ColorCheckedLinearLayout extends LinearLayout implements Checkable {
    public ColorCheckedLinearLayout(Context context) {
        this(context, null);
    }

    public ColorCheckedLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorCheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorCheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isChecked() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof Checkable) {
                return ((Checkable) child).isChecked();
            }
        }
        return false;
    }

    public void setChecked(boolean checked) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(checked);
            }
        }
    }

    public void toggle() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof Checkable) {
                ((Checkable) child).toggle();
            }
        }
    }
}
