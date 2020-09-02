package android.view;

import android.os.Parcel;
import android.view.ViewGroup;
import com.color.view.ColorLayoutParams;

public class ColorBaseLayoutParams extends ViewGroup.LayoutParams {
    public static final int COLOR_FLAG_WINDOW_DEFAULT = 0;
    public static final int COLOR_FLAG_WINDOW_USER_DEFINED_TOAST = 1;
    public static final int DEFAULT_STATUS_BAR = 0;
    public static final int DISABLE_STATUS_BAR = 1;
    public static final int ENABLE_STATUS_BAR = 2;
    public static final int FLAG_DISABLE_STATUS_BAR = 1048576;
    public static final int FLAG_ENABLE_STATUS_BAR = 2097152;
    public static final int FLAG_IGNORE_HOME_KEY = 33554432;
    public static final int FLAG_IGNORE_HOME_MENU = 16777216;
    public static final int FLAG_IMENU_KEY = 67108864;
    public static final int IGNORE_HOME_KEY = 2;
    public static final int IGNORE_HOME_MENU_KEY = 1;
    public static final int IGNORE_MENU_KEY = 3;
    public static final int PRIVATE_FLAG_BOTTOM_ALERT_DIALOG = 16777216;
    public static final int PRIVATE_FLAG_NAVIGATION_BAR_LIGHT = Integer.MIN_VALUE;
    public static final int TYPE_DRAG_SCREEN_BACKGROUND = 2301;
    public static final int TYPE_DRAG_SCREEN_FOREGROUND = 2302;
    public static final int UNSET_ANY_KEY = 0;
    public int colorFlags = 0;
    public int ignoreHomeMenuKey = 0;
    public int isDisableStatusBar = 0;
    public final ColorLayoutParams mColorLayoutParams = new ColorLayoutParams();
    public int navigationBarColor = 0;
    public int navigationBarVisibility = 0;

    public ColorBaseLayoutParams() {
    }

    public ColorBaseLayoutParams(int w, int h) {
        super(w, h);
    }

    public void writeToParcel(Parcel out, int parcelableFlags) {
        out.writeInt(this.ignoreHomeMenuKey);
        out.writeInt(this.isDisableStatusBar);
        this.mColorLayoutParams.writeToParcel(out, parcelableFlags);
        out.writeInt(this.colorFlags);
    }

    public void readFromParcel(Parcel in) {
        this.ignoreHomeMenuKey = in.readInt();
        this.isDisableStatusBar = in.readInt();
        this.mColorLayoutParams.readFromParcel(in);
        this.colorFlags = in.readInt();
    }

    public final int copyFrom(ColorBaseLayoutParams o) {
        int changes = 0;
        int i = this.ignoreHomeMenuKey;
        int i2 = o.ignoreHomeMenuKey;
        if (i != i2) {
            this.ignoreHomeMenuKey = i2;
        }
        int i3 = this.isDisableStatusBar;
        int i4 = o.isDisableStatusBar;
        if (i3 != i4) {
            this.isDisableStatusBar = i4;
        }
        if (!this.mColorLayoutParams.equals(o.mColorLayoutParams)) {
            this.mColorLayoutParams.set(o.mColorLayoutParams);
            changes = 0 | 16384;
        }
        int i5 = this.colorFlags;
        int i6 = o.colorFlags;
        if (i5 != i6) {
            this.colorFlags = i6;
        }
        return changes;
    }

    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder(128);
        if (this.ignoreHomeMenuKey != 0) {
            sb.append(" ignoreHomeMenuKey=");
            sb.append(this.ignoreHomeMenuKey);
        }
        if (this.isDisableStatusBar != 0) {
            sb.append(" isDisableStatusBar=");
            sb.append(this.isDisableStatusBar);
        }
        sb.append(this.mColorLayoutParams);
        if (this.colorFlags != 0) {
            sb.append(" colorFlags=");
            sb.append(this.colorFlags);
        }
        return sb.toString();
    }
}
