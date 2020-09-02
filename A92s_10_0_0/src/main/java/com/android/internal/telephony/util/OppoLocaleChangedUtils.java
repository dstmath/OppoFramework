package com.android.internal.telephony.util;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.PhoneConfigurationManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class OppoLocaleChangedUtils {
    public static final boolean DBG = true;
    public static final String PROPERTY_LANGUAGE_PERSISTED = "persist.sys.language_persisted";
    public static final String PROPERTY_OVERRIDE_MCC = "persist.sys.override_mcc";
    public static final String TAG = "OppoLocaleChangedUtils";
    static ArrayList<CounteryEntry> sTable = new ArrayList<>(240);

    static class CounteryEntry implements Comparable<CounteryEntry> {
        final String mCountery;
        final String mLanguage;
        final String mLocaleCountery;

        CounteryEntry(String countery, String language, String localeCountery) {
            this.mCountery = countery;
            this.mLanguage = language;
            this.mLocaleCountery = localeCountery;
        }

        public int compareTo(CounteryEntry o) {
            return this.mCountery.compareToIgnoreCase(o.mCountery);
        }

        public String getLocaleCountery() {
            return this.mLocaleCountery;
        }

        public String getLocaleLanguage() {
            return this.mLanguage;
        }
    }

    public static Locale getLocaleForCountery(String countery) {
        int index = Collections.binarySearch(sTable, new CounteryEntry(countery, PhoneConfigurationManager.SSSS, PhoneConfigurationManager.SSSS));
        if (index < 0) {
            return null;
        }
        CounteryEntry n = sTable.get(index);
        return new Locale(n.getLocaleLanguage(), n.getLocaleCountery());
    }

    public static boolean isDebuggingMccOverride() {
        if (!Build.IS_DEBUGGABLE || SystemProperties.get(PROPERTY_OVERRIDE_MCC, PhoneConfigurationManager.SSSS).isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean canUpdateLocale(Context context) {
        return !isDeviceProvisioned(context);
    }

    public static boolean userHasPersistedLocale() {
        return !SystemProperties.get(PROPERTY_LANGUAGE_PERSISTED, PhoneConfigurationManager.SSSS).isEmpty();
    }

    public static void setPersistedLocale(Locale locale) {
        if (locale != null && !locale.getLanguage().isEmpty()) {
            SystemProperties.set(PROPERTY_LANGUAGE_PERSISTED, locale.getLanguage());
        }
    }

    private static boolean isDeviceProvisioned(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "device_provisioned") != 0;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    public static boolean isNotNeedUpdateMccMncConfiguration() {
        if (!OemConstant.EXP_VERSION || !userHasPersistedLocale()) {
            return false;
        }
        log("Should not update config");
        return true;
    }

    public static boolean isNotNeedGetLocaleForLanguageCountry(Context context) {
        if (isDebuggingMccOverride() || canUpdateLocale(context)) {
            return false;
        }
        log("getLocaleForLanguageCountry: not permitted to update locale");
        return true;
    }

    public static void logd(String msg) {
        log(msg);
    }

    public static void log(String msg) {
        Rlog.d(TAG, msg);
    }

    static {
        sTable.add(new CounteryEntry("ng", "en", "GB"));
        sTable.add(new CounteryEntry("ke", "en", "GB"));
        sTable.add(new CounteryEntry("lk", "en", "GB"));
        sTable.add(new CounteryEntry("sg", "en", "GB"));
        sTable.add(new CounteryEntry("in", "en", "GB"));
        sTable.add(new CounteryEntry("ma", "fr", "FR"));
        sTable.add(new CounteryEntry("dz", "fr", "FR"));
        sTable.add(new CounteryEntry("tn", "fr", "FR"));
        sTable.add(new CounteryEntry("bd", "en", "US"));
        sTable.add(new CounteryEntry("ae", "ar", "EG"));
        sTable.add(new CounteryEntry("bh", "ar", "EG"));
        sTable.add(new CounteryEntry("qa", "ar", "EG"));
        sTable.add(new CounteryEntry("om", "ar", "EG"));
        sTable.add(new CounteryEntry("sa", "ar", "EG"));
        sTable.add(new CounteryEntry("ZA", "en", "US"));
        Collections.sort(sTable);
    }
}
