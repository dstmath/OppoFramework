package com.mediatek.internal.telephony.phb;

public class PhbEntry {
    public String alphaId;
    public int index;
    public String number;
    public int ton;
    public int type;

    private String getMaskString(String str) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= 2) {
            return "xx";
        }
        return str.substring(0, str.length() >> 1) + "xxxxx";
    }

    public String toString() {
        return "type:" + this.type + " index:" + this.index + " number:" + getMaskString(this.number) + " ton:" + this.ton + " alphaId:" + getMaskString(this.alphaId);
    }
}
