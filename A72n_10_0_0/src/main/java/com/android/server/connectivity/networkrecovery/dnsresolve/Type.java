package com.android.server.connectivity.networkrecovery.dnsresolve;

import com.android.server.usb.descriptors.UsbDescriptor;

public enum Type {
    A(1),
    NS(2),
    MD(3),
    MF(4),
    CNAME(5),
    SOA(6),
    MB(7),
    MG(8),
    MR(9),
    NULL(10),
    WKS(11),
    PTR(12),
    HINFO(13),
    MINFO(14),
    MX(15),
    TXT(16),
    RP(17),
    AFSDB(18),
    X25(19),
    ISDN(20),
    RT(21),
    NSAP(22),
    NSAP_PTR(23),
    LOC(29),
    AXFR(252),
    MAILB(253),
    MAILA(UsbDescriptor.CLASSID_APPSPECIFIC),
    ALL(255),
    UNKNOWN(-1);
    
    private final int code;

    private Type(int code2) {
        this.code = code2;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isQuestionType() {
        return this.code > 16;
    }

    public static Type byCode(int code2) {
        Type[] values = values();
        for (Type t : values) {
            if (t.code == code2) {
                return t;
            }
        }
        System.err.println("No type with code " + code2 + " exists.");
        return UNKNOWN;
    }
}
