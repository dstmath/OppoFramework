package com.mediatek.internal.telephony.gsm.cbutil;

public class Shape {
    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_POLYGON = 2;
    public int mType;

    public Shape() {
    }

    public Shape(int type) {
        this.mType = type;
    }
}
