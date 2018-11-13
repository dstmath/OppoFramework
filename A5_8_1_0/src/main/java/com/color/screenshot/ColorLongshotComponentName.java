package com.color.screenshot;

import android.content.Context;
import android.os.Parcel;
import com.color.util.ColorContextUtil;

public final class ColorLongshotComponentName {
    private String mAccessibilityName = null;
    private String mClassName = null;
    private String mContextName = null;
    private String mPackageName = null;

    private ColorLongshotComponentName(String packageName, String contextName, String className, String accessibilityName) {
        this.mPackageName = packageName;
        this.mContextName = contextName;
        this.mClassName = className;
        this.mAccessibilityName = accessibilityName;
    }

    public static ColorLongshotComponentName create(String packageName, String contextName, String className, String accessibilityName) {
        return new ColorLongshotComponentName(packageName, contextName, className, accessibilityName);
    }

    public static ColorLongshotComponentName create(ColorLongshotViewBase view, String accessibilityName) {
        Context context = view.getContext();
        return create(context.getPackageName(), ColorContextUtil.getActivityContextName(context), view.getClass().getName(), accessibilityName);
    }

    public String toString() {
        return "Component{" + this.mPackageName + "/" + this.mContextName + "/" + this.mClassName + "/" + this.mAccessibilityName + "}";
    }

    public void writeToParcel(Parcel out, int flags) {
        writeString(out, this.mPackageName);
        writeString(out, this.mContextName);
        writeString(out, this.mAccessibilityName);
        writeString(out, this.mClassName);
    }

    public void readFromParcel(Parcel in) {
        this.mPackageName = readString(in);
        this.mContextName = readString(in);
        this.mClassName = readString(in);
        this.mAccessibilityName = readString(in);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getContextName() {
        return this.mContextName;
    }

    public String getClassName() {
        return this.mClassName;
    }

    public String getAccessibilityName() {
        return this.mAccessibilityName;
    }

    private void writeString(Parcel out, String s) {
        if (s != null) {
            out.writeInt(1);
            out.writeString(s);
            return;
        }
        out.writeInt(0);
    }

    private String readString(Parcel in) {
        if (1 == in.readInt()) {
            return in.readString();
        }
        return null;
    }
}
