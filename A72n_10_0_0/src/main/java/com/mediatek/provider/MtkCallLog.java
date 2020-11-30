package com.mediatek.provider;

import android.provider.CallLog;

public class MtkCallLog {

    public static class Calls extends CallLog.Calls {
        public static final String CACHED_INDICATE_PHONE_SIM = "indicate_phone_or_sim_contact";
        public static final String CACHED_IS_SDN_CONTACT = "is_sdn_contact";
    }
}
