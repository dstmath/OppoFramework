package com.android.server.wifi.hotspot2;

import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;

public abstract class AuthMatch {
    public static final int Exact = 7;
    public static final int Indeterminate = 0;
    public static final int Method = 2;
    public static final int MethodParam = 3;
    public static final int None = -1;
    public static final int Param = 1;
    public static final int Realm = 4;

    public static String toString(int match) {
        if (match < 0) {
            return "None";
        }
        if (match == 0) {
            return "Indeterminate";
        }
        StringBuilder sb = new StringBuilder();
        if ((match & 4) != 0) {
            sb.append(PasspointManagementObjectManager.TAG_Realm);
        }
        if ((match & 2) != 0) {
            sb.append("Method");
        }
        if ((match & 1) != 0) {
            sb.append("Param");
        }
        return sb.toString();
    }
}
