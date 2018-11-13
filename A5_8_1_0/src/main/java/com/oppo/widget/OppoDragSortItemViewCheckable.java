package com.oppo.widget;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;

public class OppoDragSortItemViewCheckable extends OppoDragSortItemView implements Checkable {
    public OppoDragSortItemViewCheckable(Context context) {
        super(context);
    }

    public boolean isChecked() {
        View child = getChildAt(0);
        if (child instanceof Checkable) {
            return ((Checkable) child).isChecked();
        }
        return false;
    }

    public void setChecked(boolean checked) {
        View child = getChildAt(0);
        if (child instanceof Checkable) {
            ((Checkable) child).setChecked(checked);
        }
    }

    public void toggle() {
        View child = getChildAt(0);
        if (child instanceof Checkable) {
            ((Checkable) child).toggle();
        }
    }
}
