package oppo.content.res;

import android.os.Parcel;

public class OppoExtraConfiguration implements Comparable {
    public static final int ACESSIBLE_COLOR_MODE_CHANGED = 67108864;
    public static final int CONFIG_FLIPFONT = 33554432;
    public static final int CONFIG_USER_CHANGE = 268435456;
    public static final int DARK_MODE_CONFIG_CHANGED = 1073741824;
    public static final int FONT_USER_CHANGE = 536870912;
    public static final int THEME_NEW_SKIN_CHANGED = 150994944;
    public static final int THEME_OLD_SKIN_CHANGED = 134217728;
    public static final int UX_ICON_CONFIG_CHANGED = Integer.MIN_VALUE;
    public int mAccessibleChanged;
    public int mFlipFont;
    public int mFontUserId = -1;
    public int mThemeChanged;
    public long mThemeChangedFlags;
    public int mUserId;
    public long mUxIconConfig;

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        return compareTo((OppoExtraConfiguration) obj);
    }

    public int compareTo(OppoExtraConfiguration extraconfiguration) {
        int i = this.mThemeChanged - extraconfiguration.mThemeChanged;
        if (i != 0) {
            return i;
        }
        int i2 = this.mFlipFont - extraconfiguration.mFlipFont;
        if (i2 != 0) {
            return i2;
        }
        int i3 = this.mUserId - extraconfiguration.mUserId;
        if (i3 != 0) {
            return i3;
        }
        int i4 = this.mFontUserId - extraconfiguration.mFontUserId;
        if (i4 != 0) {
            return i4;
        }
        int i5 = this.mAccessibleChanged - extraconfiguration.mAccessibleChanged;
        if (i5 != 0) {
            return i5;
        }
        return Long.compare(this.mUxIconConfig, extraconfiguration.mUxIconConfig);
    }

    public void setTo(OppoExtraConfiguration extraconfiguration) {
        this.mThemeChanged = extraconfiguration.mThemeChanged;
        this.mThemeChangedFlags = extraconfiguration.mThemeChangedFlags;
        this.mFlipFont = extraconfiguration.mFlipFont;
        this.mUserId = extraconfiguration.mUserId;
        this.mAccessibleChanged = extraconfiguration.mAccessibleChanged;
        this.mUxIconConfig = extraconfiguration.mUxIconConfig;
        this.mFontUserId = extraconfiguration.mFontUserId;
    }

    public String toString() {
        return "mThemeChanged= " + this.mThemeChanged + ", mThemeChangedFlags= " + this.mThemeChangedFlags + ", mFlipFont= " + this.mFlipFont + ", mAccessibleChanged= " + this.mAccessibleChanged + ", mUxIconConfig= " + this.mUxIconConfig + ", mUserId= " + this.mUserId;
    }

    public void setToDefaults() {
        this.mThemeChanged = 0;
        this.mThemeChangedFlags = 0;
        this.mFlipFont = 0;
        this.mUserId = 0;
        this.mAccessibleChanged = 0;
        this.mUxIconConfig = 0;
        this.mFontUserId = -1;
    }

    public int updateFrom(OppoExtraConfiguration extraconfiguration) {
        int i = 0;
        int i2 = extraconfiguration.mThemeChanged;
        if (i2 > 0 && this.mThemeChanged != i2) {
            i = 0 | 134217728;
            this.mThemeChanged = i2;
            this.mThemeChangedFlags = extraconfiguration.mThemeChangedFlags;
            this.mUserId = extraconfiguration.mUserId;
        }
        int i3 = extraconfiguration.mAccessibleChanged;
        if (!(i3 == 0 || this.mAccessibleChanged == i3)) {
            i |= 67108864;
            this.mAccessibleChanged = i3;
            this.mUserId = extraconfiguration.mUserId;
        }
        int i4 = extraconfiguration.mFlipFont;
        if (i4 > 0 && this.mFlipFont != i4) {
            i |= 33554432;
            this.mFlipFont = i4;
        }
        int i5 = extraconfiguration.mUserId;
        if (i5 > 0 && this.mUserId != i5) {
            i |= 268435456;
            this.mUserId = i5;
        }
        int i6 = extraconfiguration.mFontUserId;
        if (i6 >= 0 && this.mFontUserId != i6) {
            i |= 536870912;
            this.mFontUserId = i6;
        }
        long j = extraconfiguration.mUxIconConfig;
        if (j <= 0 || j == this.mUxIconConfig) {
            return i;
        }
        int i7 = i | Integer.MIN_VALUE;
        this.mUxIconConfig = j;
        this.mUserId = extraconfiguration.mUserId;
        return i7;
    }

    public int diff(OppoExtraConfiguration extraconfiguration) {
        int i = 0;
        int i2 = extraconfiguration.mThemeChanged;
        if (i2 > 0 && this.mThemeChanged != i2) {
            i = 0 | 134217728;
        }
        int i3 = extraconfiguration.mAccessibleChanged;
        if (!(i3 == 0 || this.mAccessibleChanged == i3)) {
            i |= 67108864;
        }
        int i4 = extraconfiguration.mFlipFont;
        if (i4 > 0 && this.mFlipFont != i4) {
            i |= 33554432;
        }
        int i5 = extraconfiguration.mUserId;
        if (i5 > 0 && this.mUserId != i5) {
            i |= 268435456;
            this.mUserId = i5;
        }
        int i6 = extraconfiguration.mFontUserId;
        if (i6 >= 0 && this.mFontUserId != i6) {
            i |= 536870912;
            this.mFontUserId = i6;
        }
        long j = extraconfiguration.mUxIconConfig;
        if (j <= 0 || this.mUxIconConfig == j) {
            return i;
        }
        return i | Integer.MIN_VALUE;
    }

    public static boolean needNewResources(int i) {
        if ((134217728 & i) == 0 && (33554432 & i) == 0 && (i & 512) == 0 && (Integer.MIN_VALUE & i) == 0) {
            return false;
        }
        return true;
    }

    public static boolean needAccessNewResources(int i) {
        return (67108864 & i) != 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mThemeChanged);
        parcel.writeLong(this.mThemeChangedFlags);
        parcel.writeInt(this.mFlipFont);
        parcel.writeInt(this.mUserId);
        parcel.writeInt(this.mAccessibleChanged);
        parcel.writeLong(this.mUxIconConfig);
        parcel.writeInt(this.mFontUserId);
    }

    public void readFromParcel(Parcel parcel) {
        this.mThemeChanged = parcel.readInt();
        this.mThemeChangedFlags = parcel.readLong();
        this.mFlipFont = parcel.readInt();
        this.mUserId = parcel.readInt();
        this.mAccessibleChanged = parcel.readInt();
        this.mUxIconConfig = parcel.readLong();
        this.mFontUserId = parcel.readInt();
    }

    public int hashCode() {
        return ((this.mThemeChanged + ((int) this.mThemeChangedFlags)) * 31) + this.mFlipFont + (this.mAccessibleChanged * 16) + (this.mUserId * 8) + ((int) this.mUxIconConfig);
    }
}
