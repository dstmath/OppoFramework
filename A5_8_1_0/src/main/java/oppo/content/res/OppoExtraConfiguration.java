package oppo.content.res;

import android.os.Parcel;

public class OppoExtraConfiguration implements Comparable {
    public static final int CONFIG_FLIPFONT = 536870912;
    public static final int THEME_NEW_SKIN_CHANGED = 150994944;
    public static final int THEME_OLD_SKIN_CHANGED = 134217728;
    public int mFlipFont;
    public int mThemeChanged;
    public long mThemeChangedFlags;

    public int compareTo(Object obj) {
        return compareTo((OppoExtraConfiguration) obj);
    }

    public int compareTo(OppoExtraConfiguration extraconfiguration) {
        int i = this.mThemeChanged - extraconfiguration.mThemeChanged;
        if (i != 0) {
            return i;
        }
        return this.mFlipFont - extraconfiguration.mFlipFont;
    }

    public void setTo(OppoExtraConfiguration extraconfiguration) {
        this.mThemeChanged = extraconfiguration.mThemeChanged;
        this.mThemeChangedFlags = extraconfiguration.mThemeChangedFlags;
        this.mFlipFont = extraconfiguration.mFlipFont;
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("mThemeChanged = ").append(this.mThemeChanged).append("mThemeChangedFlags = ").append(this.mThemeChangedFlags).append("mFlipFont = ").append(this.mFlipFont);
        return stringbuilder.toString();
    }

    public void setToDefaults() {
        this.mThemeChanged = 0;
        this.mThemeChangedFlags = 0;
        this.mFlipFont = 0;
    }

    public int updateFrom(OppoExtraConfiguration extraconfiguration) {
        int i = 0;
        if (extraconfiguration.mThemeChanged > 0 && this.mThemeChanged != extraconfiguration.mThemeChanged) {
            i = 134217728;
            this.mThemeChanged = extraconfiguration.mThemeChanged;
            this.mThemeChangedFlags = extraconfiguration.mThemeChangedFlags;
        }
        if (extraconfiguration.mFlipFont <= 0 || this.mFlipFont == extraconfiguration.mFlipFont) {
            return i;
        }
        i |= CONFIG_FLIPFONT;
        this.mFlipFont = extraconfiguration.mFlipFont;
        return i;
    }

    public int diff(OppoExtraConfiguration extraconfiguration) {
        int i = 0;
        if (extraconfiguration.mThemeChanged > 0 && this.mThemeChanged != extraconfiguration.mThemeChanged) {
            i = 134217728;
        }
        if (extraconfiguration.mFlipFont <= 0 || this.mFlipFont == extraconfiguration.mFlipFont) {
            return i;
        }
        return i | CONFIG_FLIPFONT;
    }

    public static boolean needNewResources(int i) {
        if ((134217728 & i) != 0) {
            return true;
        }
        if ((CONFIG_FLIPFONT & i) != 0) {
            return true;
        }
        return false;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mThemeChanged);
        parcel.writeLong(this.mThemeChangedFlags);
        parcel.writeInt(this.mFlipFont);
    }

    public void readFromParcel(Parcel parcel) {
        this.mThemeChanged = parcel.readInt();
        this.mThemeChangedFlags = parcel.readLong();
        this.mFlipFont = parcel.readInt();
    }

    public int hashCode() {
        return ((this.mThemeChanged + ((int) this.mThemeChangedFlags)) * 31) + this.mFlipFont;
    }
}
