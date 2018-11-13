package com.color.widget;

import android.widget.AdapterView.OnItemClickListener;

public interface ColorSpinnerCallback {

    public interface DropdownDismissCallback {
        void setDismissListener(DropdownDismissListener dropdownDismissListener);
    }

    public interface DropdownDismissListener {
        void startDropdownDismiss();
    }

    boolean isDropDownShowing();

    void setDropdownDismissCallback(DropdownDismissCallback dropdownDismissCallback);

    void setDropdownItemClickListener(OnItemClickListener onItemClickListener);

    void setDropdownUpdateAfterAnim(boolean z);
}
