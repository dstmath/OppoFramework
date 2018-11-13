package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateFormat;

public class ColorReflectWidget implements Parcelable {
    public static final Creator<ColorReflectWidget> CREATOR = new Creator<ColorReflectWidget>() {
        public ColorReflectWidget createFromParcel(Parcel source) {
            return new ColorReflectWidget(source);
        }

        public ColorReflectWidget[] newArray(int size) {
            return new ColorReflectWidget[size];
        }
    };
    public static final ColorReflectWidget DEFAULT_WIDGET = new ColorReflectWidget("com.tencent.mm", 1280, "com.tencent.mm.ui.widget.MMNeatTextView", 1, "mText");
    private String className;
    private String field;
    private int fieldLevel;
    private String packageName;
    private int versionCode;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.versionCode);
        dest.writeString(this.className);
        dest.writeInt(this.fieldLevel);
        dest.writeString(this.field);
    }

    public ColorReflectWidget(String packageName, int versionCode, String className, int fieldLevel, String field) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.className = className;
        this.fieldLevel = fieldLevel;
        this.field = field;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ColorReflectWidget that = (ColorReflectWidget) o;
        if (this.versionCode == that.versionCode && this.fieldLevel == that.fieldLevel && this.packageName.equals(that.packageName) && this.className.equals(that.className)) {
            return this.field.equals(that.field);
        }
        return false;
    }

    public String toString() {
        return "ColorReflectWidget{packageName='" + this.packageName + DateFormat.QUOTE + ", versionCode=" + this.versionCode + ", className='" + this.className + DateFormat.QUOTE + ", fieldLevel=" + this.fieldLevel + ", field='" + this.field + DateFormat.QUOTE + '}';
    }

    public int hashCode() {
        return (((((((this.packageName.hashCode() * 31) + this.versionCode) * 31) + this.className.hashCode()) * 31) + this.fieldLevel) * 31) + this.field.hashCode();
    }

    public void setFieldLevel(int fieldLevel) {
        this.fieldLevel = fieldLevel;
    }

    public int getFieldLevel() {
        return this.fieldLevel;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    protected ColorReflectWidget(Parcel in) {
        this.packageName = in.readString();
        this.versionCode = in.readInt();
        this.className = in.readString();
        this.fieldLevel = in.readInt();
        this.field = in.readString();
    }
}
