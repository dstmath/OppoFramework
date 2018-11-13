package android.support.v13.app;

import android.app.Fragment;

class FragmentCompatICS {
    FragmentCompatICS() {
    }

    public static void setMenuVisibility(Fragment f, boolean visible) {
        f.setMenuVisibility(visible);
    }
}
