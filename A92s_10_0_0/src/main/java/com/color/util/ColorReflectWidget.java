package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import com.android.internal.logging.nano.MetricsProto;
import com.oppo.luckymoney.LMManager;

public class ColorReflectWidget implements Parcelable {
    public static final Parcelable.Creator<ColorReflectWidget> CREATOR = new Parcelable.Creator<ColorReflectWidget>() {
        /* class com.color.util.ColorReflectWidget.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorReflectWidget createFromParcel(Parcel source) {
            return new ColorReflectWidget(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorReflectWidget[] newArray(int size) {
            return new ColorReflectWidget[size];
        }
    };
    public static final ColorReflectWidget DEFAULT_WIDGET = new ColorReflectWidget(LMManager.MM_PACKAGENAME, 1280, "com.tencent.mm.ui.widget.MMNeatTextView", 1, "mText");
    public static final ColorReflectWidget DEFAULT_WIDGET_WECHAT_1420 = new ColorReflectWidget(LMManager.MM_PACKAGENAME, MetricsProto.MetricsEvent.FIELD_CHARGING_DURATION_MILLIS, "com.tencent.mm.ui.widget.MMNeat7extView", 1, "mText");
    private String className;
    private String field;
    private int fieldLevel;
    private String packageName;
    private int versionCode;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.versionCode);
        dest.writeString(this.className);
        dest.writeInt(this.fieldLevel);
        dest.writeString(this.field);
    }

    public ColorReflectWidget() {
    }

    public ColorReflectWidget(String packageName2, int versionCode2, String className2, int fieldLevel2, String field2) {
        this.packageName = packageName2;
        this.versionCode = versionCode2;
        this.className = className2;
        this.fieldLevel = fieldLevel2;
        this.field = field2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(int versionCode2) {
        this.versionCode = versionCode2;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className2) {
        this.className = className2;
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

    public void setFieldLevel(int fieldLevel2) {
        this.fieldLevel = fieldLevel2;
    }

    public int getFieldLevel() {
        return this.fieldLevel;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field2) {
        this.field = field2;
    }

    protected ColorReflectWidget(Parcel in) {
        this.packageName = in.readString();
        this.versionCode = in.readInt();
        this.className = in.readString();
        this.fieldLevel = in.readInt();
        this.field = in.readString();
    }
}
