package com.android.internal.logging;

import com.android.internal.telephony.PhoneConstants;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidConfig {
    public AndroidConfig() {
        try {
            Logger rootLogger = Logger.getLogger(PhoneConstants.MVNO_TYPE_NONE);
            rootLogger.addHandler(new AndroidHandler());
            rootLogger.setLevel(Level.INFO);
            Logger.getLogger("org.apache").setLevel(Level.WARNING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
