package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import libcore.icu.ICU;
import libcore.icu.TimeZoneNames;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class MccTable {
    private static final Map<Locale, Locale> FALLBACKS = null;
    static final String LOG_TAG = "MccTable";
    static ArrayList<MccEntry> sTable;

    static class MccEntry implements Comparable<MccEntry> {
        final String mIso;
        String mLanguage;
        final int mMcc;
        final int mSmallestDigitsMnc;

        MccEntry(int mnc, String iso, int smallestDigitsMCC) {
            this(mnc, iso, smallestDigitsMCC, null);
        }

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language) {
            if (iso == null) {
                throw new NullPointerException();
            }
            this.mMcc = mnc;
            this.mIso = iso;
            this.mSmallestDigitsMnc = smallestDigitsMCC;
            this.mLanguage = language;
        }

        public int compareTo(MccEntry o) {
            return this.mMcc - o.mMcc;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.MccTable.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.MccTable.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.MccTable.<clinit>():void");
    }

    private static MccEntry entryForMcc(int mcc) {
        int index = Collections.binarySearch(sTable, new MccEntry(mcc, UsimPBMemInfo.STRING_NOT_SET, 0));
        if (index < 0) {
            return null;
        }
        return (MccEntry) sTable.get(index);
    }

    public static String defaultTimeZoneForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return null;
        }
        String[] tz = TimeZoneNames.forLocale(new Locale(UsimPBMemInfo.STRING_NOT_SET, entry.mIso));
        if (tz.length == 0) {
            return null;
        }
        return tz[0];
    }

    public static String countryCodeForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        return entry.mIso;
    }

    public static String defaultLanguageForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            Slog.d(LOG_TAG, "defaultLanguageForMcc(" + mcc + "): no country for mcc");
            return null;
        }
        String country = entry.mIso;
        if ("in".equals(country)) {
            return "en";
        }
        String likelyLanguage = ICU.addLikelySubtags(new Locale("und", country)).getLanguage();
        Slog.d(LOG_TAG, "defaultLanguageForMcc(" + mcc + "): country " + country + " uses " + likelyLanguage);
        return likelyLanguage;
    }

    public static int smallestDigitsMccForMnc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return 2;
        }
        return entry.mSmallestDigitsMnc;
    }

    public static void updateMccMncConfiguration(Context context, String mccmnc, boolean fromServiceState) {
        Slog.d(LOG_TAG, "updateMccMncConfiguration mccmnc='" + mccmnc + "' fromServiceState=" + fromServiceState);
        if (Build.IS_DEBUGGABLE) {
            String overrideMcc = SystemProperties.get("persist.sys.override_mcc");
            if (!TextUtils.isEmpty(overrideMcc)) {
                mccmnc = overrideMcc;
                Slog.d(LOG_TAG, "updateMccMncConfiguration overriding mccmnc='" + overrideMcc + "'");
            }
        }
        if (!TextUtils.isEmpty(mccmnc)) {
            String defaultMccMnc = TelephonyManager.getDefault().getSimOperatorNumeric();
            Slog.d(LOG_TAG, "updateMccMncConfiguration defaultMccMnc=" + defaultMccMnc);
            try {
                int mcc = Integer.parseInt(mccmnc.substring(0, 3));
                int mnc = Integer.parseInt(mccmnc.substring(3));
                Slog.d(LOG_TAG, "updateMccMncConfiguration: mcc=" + mcc + ", mnc=" + mnc);
                Locale locale = null;
                boolean expVersion = context.getPackageManager().hasSystemFeature("oppo.version.exp");
                if (mcc != 0) {
                    setTimezoneFromMccIfNeeded(context, mcc);
                    if (expVersion) {
                        locale = getLocaleFromMcc(context, mcc, null);
                    }
                }
                if (fromServiceState) {
                    setWifiCountryCodeFromMcc(context, mcc);
                } else {
                    try {
                        if (!TelephonyManager.getDefault().isMultiSimEnabled() || mccmnc.equals(defaultMccMnc)) {
                            Configuration config = new Configuration();
                            boolean updateConfig = false;
                            if (mcc != 0) {
                                config.mcc = mcc;
                                if (mnc == 0) {
                                    mnc = 65535;
                                }
                                config.mnc = mnc;
                                updateConfig = true;
                            }
                            if (locale != null) {
                                config.setLocale(locale);
                                updateConfig = true;
                                config.userSetLocale = true;
                                setPersistedLocale(locale);
                            }
                            if (updateConfig) {
                                Slog.d(LOG_TAG, "updateMccMncConfiguration updateConfig config=" + config);
                                ActivityManagerNative.getDefault().updateConfiguration(config);
                            } else {
                                Slog.d(LOG_TAG, "updateMccMncConfiguration nothing to update");
                            }
                        } else {
                            Slog.d(LOG_TAG, "OEM:Not a Default subscription, ignoring mccmnc config update.");
                        }
                    } catch (RemoteException e) {
                        Slog.e(LOG_TAG, "Can't update configuration", e);
                    }
                }
            } catch (NumberFormatException e2) {
                Slog.e(LOG_TAG, "Error parsing IMSI: " + mccmnc);
            }
        } else if (fromServiceState) {
            setWifiCountryCodeFromMcc(context, 0);
        }
    }

    private static Locale chooseBestFallback(Locale target, List<Locale> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        Locale fallback = target;
        do {
            fallback = (Locale) FALLBACKS.get(fallback);
            if (fallback == null) {
                return (Locale) candidates.get(0);
            }
        } while (!candidates.contains(fallback));
        return fallback;
    }

    private static Locale getLocaleForLanguageCountry(Context context, String language, String country) {
        if (language == null) {
            Slog.d(LOG_TAG, "getLocaleForLanguageCountry: skipping no language");
            return null;
        }
        if (country == null) {
            country = UsimPBMemInfo.STRING_NOT_SET;
        }
        if (!isDebuggingMccOverride() ? canUpdateLocale(context) : true) {
            Locale target = new Locale(language, country);
            if (country.equals("ng") || country.equals("ke") || country.equals("lk") || country.equals("sg") || country.equals("in")) {
                target = new Locale("en", "GB");
            } else if (country.equals("ma") || country.equals("dz")) {
                target = new Locale("fr", "FR");
            } else if (country.equals("bd")) {
                target = new Locale("en", "US");
            }
            try {
                List<String> locales = new ArrayList(Arrays.asList(context.getAssets().getLocales()));
                locales.remove("ar-XB");
                locales.remove("en-XA");
                List<Locale> languageMatches = new ArrayList();
                for (String locale : locales) {
                    Locale l = Locale.forLanguageTag(locale.replace('_', '-'));
                    if (!(l == null || "und".equals(l.getLanguage()) || l.getLanguage().isEmpty() || l.getCountry().isEmpty() || !l.getLanguage().equals(target.getLanguage()))) {
                        if (l.getCountry().equals(target.getCountry())) {
                            Slog.d(LOG_TAG, "getLocaleForLanguageCountry: got perfect match: " + l.toLanguageTag());
                            return l;
                        }
                        languageMatches.add(l);
                    }
                }
                Locale bestMatch = chooseBestFallback(target, languageMatches);
                if (bestMatch != null) {
                    Slog.d(LOG_TAG, "getLocaleForLanguageCountry: got a language-only match: " + bestMatch.toLanguageTag());
                    if (bestMatch.getLanguage().equals("en") && bestMatch.getCountry().equals("ZA")) {
                        bestMatch = new Locale("en", "US");
                    }
                    return bestMatch;
                }
                Slog.d(LOG_TAG, "getLocaleForLanguageCountry: no locales for language " + language);
                return null;
            } catch (Exception e) {
                Slog.d(LOG_TAG, "getLocaleForLanguageCountry: exception", e);
            }
        } else {
            Slog.d(LOG_TAG, "getLocaleForLanguageCountry: not permitted to update locale");
            return null;
        }
    }

    private static void setTimezoneFromMccIfNeeded(Context context, int mcc) {
        String timezone = SystemProperties.get("persist.sys.timezone");
        if (timezone == null || timezone.length() == 0) {
            String zoneId = defaultTimeZoneForMcc(mcc);
            if (zoneId != null && zoneId.length() > 0) {
                ((AlarmManager) context.getSystemService("alarm")).setTimeZone(zoneId);
                Slog.d(LOG_TAG, "timezone set to " + zoneId);
            }
        }
    }

    public static Locale getLocaleFromMcc(Context context, int mcc, String simLanguage) {
        String language = simLanguage == null ? defaultLanguageForMcc(mcc) : simLanguage;
        String country = countryCodeForMcc(mcc);
        Slog.d(LOG_TAG, "getLocaleFromMcc(" + language + ", " + country + ", " + mcc);
        Locale locale = getLocaleForLanguageCountry(context, language, country);
        if (locale != null || simLanguage == null) {
            return locale;
        }
        language = defaultLanguageForMcc(mcc);
        Slog.d(LOG_TAG, "[retry ] getLocaleFromMcc(" + language + ", " + country + ", " + mcc);
        return getLocaleForLanguageCountry(context, language, country);
    }

    private static boolean isDebuggingMccOverride() {
        if (!Build.IS_DEBUGGABLE || SystemProperties.get("persist.sys.override_mcc", UsimPBMemInfo.STRING_NOT_SET).isEmpty()) {
            return false;
        }
        return true;
    }

    private static boolean canUpdateLocale(Context context) {
        return (userHasPersistedLocale() || isDeviceProvisioned(context)) ? false : true;
    }

    private static boolean userHasPersistedLocale() {
        return !SystemProperties.get("persist.sys.language2", UsimPBMemInfo.STRING_NOT_SET).isEmpty();
    }

    private static void setPersistedLocale(Locale locale) {
        if (locale != null && !locale.getLanguage().isEmpty()) {
            SystemProperties.set("persist.sys.language2", locale.getLanguage());
        }
    }

    private static boolean isDeviceProvisioned(Context context) {
        boolean z = false;
        try {
            if (Global.getInt(context.getContentResolver(), "device_provisioned") != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private static void setWifiCountryCodeFromMcc(Context context, int mcc) {
        String country = countryCodeForMcc(mcc);
        Slog.d(LOG_TAG, "WIFI_COUNTRY_CODE set to " + country);
        ((WifiManager) context.getSystemService("wifi")).setCountryCode(country, false);
    }
}
