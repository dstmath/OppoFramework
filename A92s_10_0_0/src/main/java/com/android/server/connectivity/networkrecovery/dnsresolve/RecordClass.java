package com.android.server.connectivity.networkrecovery.dnsresolve;

public enum RecordClass {
    IN(1),
    CS(2),
    CH(3),
    HS(4),
    ANY(255);
    
    private final int code;

    private RecordClass(int code2) {
        this.code = code2;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isQuestionClass() {
        return this.code == 255;
    }

    public static RecordClass byCode(int code2) {
        RecordClass[] values = values();
        for (RecordClass c : values) {
            if (c.code == code2) {
                return c;
            }
        }
        System.err.println("No RecordClass with code " + code2 + " exists.");
        return IN;
    }
}
