package com.android.internal.app;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.LocaleList;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.android.internal.content.NativeLibraryHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class LocaleStore {
    private static boolean sFullyInitialized = false;
    private static final HashMap<String, LocaleInfo> sLocaleCache = new HashMap<>();

    public static class LocaleInfo implements Serializable {
        private static final int SUGGESTION_TYPE_CFG = 2;
        private static final int SUGGESTION_TYPE_NONE = 0;
        private static final int SUGGESTION_TYPE_SIM = 1;
        private String mFullCountryNameNative;
        private String mFullNameNative;
        private final String mId;
        private boolean mIsChecked;
        private boolean mIsPseudo;
        private boolean mIsTranslated;
        private String mLangScriptKey;
        private final Locale mLocale;
        private final Locale mParent;
        private int mSuggestionFlags;

        static /* synthetic */ int access$076(LocaleInfo x0, int x1) {
            int i = x0.mSuggestionFlags | x1;
            x0.mSuggestionFlags = i;
            return i;
        }

        private LocaleInfo(Locale locale) {
            this.mLocale = locale;
            this.mId = locale.toLanguageTag();
            this.mParent = getParent(locale);
            this.mIsChecked = false;
            this.mSuggestionFlags = 0;
            this.mIsTranslated = false;
            this.mIsPseudo = false;
        }

        private LocaleInfo(String localeId) {
            this(Locale.forLanguageTag(localeId));
        }

        private static Locale getParent(Locale locale) {
            if (locale.getCountry().isEmpty()) {
                return null;
            }
            return new Locale.Builder().setLocale(locale).setRegion("").setExtension('u', "").build();
        }

        public String toString() {
            return this.mId;
        }

        @UnsupportedAppUsage
        public Locale getLocale() {
            return this.mLocale;
        }

        @UnsupportedAppUsage
        public Locale getParent() {
            return this.mParent;
        }

        @UnsupportedAppUsage
        public String getId() {
            return this.mId;
        }

        public boolean isTranslated() {
            return this.mIsTranslated;
        }

        public void setTranslated(boolean isTranslated) {
            this.mIsTranslated = isTranslated;
        }

        /* access modifiers changed from: package-private */
        public boolean isSuggested() {
            if (this.mIsTranslated && this.mSuggestionFlags != 0) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isSuggestionOfType(int suggestionMask) {
            if (this.mIsTranslated && (this.mSuggestionFlags & suggestionMask) == suggestionMask) {
                return true;
            }
            return false;
        }

        @UnsupportedAppUsage
        public String getFullNameNative() {
            if (this.mFullNameNative == null) {
                Locale locale = this.mLocale;
                this.mFullNameNative = LocaleHelper.getDisplayName(locale, locale, true);
            }
            return this.mFullNameNative;
        }

        /* access modifiers changed from: package-private */
        public String getFullCountryNameNative() {
            if (this.mFullCountryNameNative == null) {
                Locale locale = this.mLocale;
                this.mFullCountryNameNative = LocaleHelper.getDisplayCountry(locale, locale);
            }
            return this.mFullCountryNameNative;
        }

        /* access modifiers changed from: package-private */
        public String getFullCountryNameInUiLanguage() {
            return LocaleHelper.getDisplayCountry(this.mLocale);
        }

        @UnsupportedAppUsage
        public String getFullNameInUiLanguage() {
            return LocaleHelper.getDisplayName(this.mLocale, true);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getLangScriptKey() {
            String str;
            if (this.mLangScriptKey == null) {
                Locale parentWithScript = getParent(LocaleHelper.addLikelySubtags(new Locale.Builder().setLocale(this.mLocale).setExtension('u', "").build()));
                if (parentWithScript == null) {
                    str = this.mLocale.toLanguageTag();
                } else {
                    str = parentWithScript.toLanguageTag();
                }
                this.mLangScriptKey = str;
            }
            return this.mLangScriptKey;
        }

        /* access modifiers changed from: package-private */
        public String getLabel(boolean countryMode) {
            if (countryMode) {
                return getFullCountryNameNative();
            }
            return getFullNameNative();
        }

        /* access modifiers changed from: package-private */
        public String getContentDescription(boolean countryMode) {
            if (countryMode) {
                return getFullCountryNameInUiLanguage();
            }
            return getFullNameInUiLanguage();
        }

        public boolean getChecked() {
            return this.mIsChecked;
        }

        public void setChecked(boolean checked) {
            this.mIsChecked = checked;
        }
    }

    private static Set<String> getSimCountries(Context context) {
        Set<String> result = new HashSet<>();
        TelephonyManager tm = TelephonyManager.from(context);
        if (tm != null) {
            String iso = tm.getSimCountryIso().toUpperCase(Locale.US);
            if (!iso.isEmpty()) {
                result.add(iso);
            }
            String iso2 = tm.getNetworkCountryIso().toUpperCase(Locale.US);
            if (!iso2.isEmpty()) {
                result.add(iso2);
            }
        }
        return result;
    }

    public static void updateSimCountries(Context context) {
        Set<String> simCountries = getSimCountries(context);
        for (LocaleInfo li : sLocaleCache.values()) {
            if (simCountries.contains(li.getLocale().getCountry())) {
                LocaleInfo.access$076(li, 1);
            }
        }
    }

    private static void addSuggestedLocalesForRegion(Locale locale) {
        if (locale != null) {
            String country = locale.getCountry();
            if (!country.isEmpty()) {
                for (LocaleInfo li : sLocaleCache.values()) {
                    if (country.equals(li.getLocale().getCountry())) {
                        LocaleInfo.access$076(li, 1);
                    }
                }
            }
        }
    }

    @UnsupportedAppUsage
    public static void fillCache(Context context) {
        if (!sFullyInitialized) {
            Set<String> simCountries = getSimCountries(context);
            boolean isInDeveloperMode = Settings.Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) != 0;
            String[] supportedLocales = LocalePicker.getSupportedLocales(context);
            for (String localeId : supportedLocales) {
                if (!localeId.isEmpty()) {
                    LocaleInfo li = new LocaleInfo(localeId);
                    if (LocaleList.isPseudoLocale(li.getLocale())) {
                        if (isInDeveloperMode) {
                            li.setTranslated(true);
                            li.mIsPseudo = true;
                            LocaleInfo.access$076(li, 1);
                        }
                    }
                    if (simCountries.contains(li.getLocale().getCountry())) {
                        LocaleInfo.access$076(li, 1);
                    }
                    sLocaleCache.put(li.getId(), li);
                    Locale parent = li.getParent();
                    if (parent != null) {
                        String parentId = parent.toLanguageTag();
                        if (!sLocaleCache.containsKey(parentId)) {
                            sLocaleCache.put(parentId, new LocaleInfo(parent));
                        }
                    }
                } else {
                    throw new IllformedLocaleException("Bad locale entry in locale_config.xml");
                }
            }
            HashSet<String> localizedLocales = new HashSet<>();
            for (String localeId2 : LocalePicker.getSystemAssetLocales()) {
                LocaleInfo li2 = new LocaleInfo(localeId2);
                String country = li2.getLocale().getCountry();
                if (!country.isEmpty()) {
                    LocaleInfo cachedLocale = null;
                    if (sLocaleCache.containsKey(li2.getId())) {
                        cachedLocale = sLocaleCache.get(li2.getId());
                    } else {
                        String langScriptCtry = li2.getLangScriptKey() + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + country;
                        if (sLocaleCache.containsKey(langScriptCtry)) {
                            cachedLocale = sLocaleCache.get(langScriptCtry);
                        }
                    }
                    if (cachedLocale != null) {
                        LocaleInfo.access$076(cachedLocale, 2);
                    }
                }
                localizedLocales.add(li2.getLangScriptKey());
            }
            for (LocaleInfo li3 : sLocaleCache.values()) {
                li3.setTranslated(localizedLocales.contains(li3.getLangScriptKey()));
            }
            addSuggestedLocalesForRegion(Locale.getDefault());
            sFullyInitialized = true;
        }
    }

    private static int getLevel(Set<String> ignorables, LocaleInfo li, boolean translatedOnly) {
        if (ignorables.contains(li.getId())) {
            return 0;
        }
        if (li.mIsPseudo) {
            return 2;
        }
        if ((!translatedOnly || li.isTranslated()) && li.getParent() != null) {
            return 2;
        }
        return 0;
    }

    @UnsupportedAppUsage
    public static Set<LocaleInfo> getLevelLocales(Context context, Set<String> ignorables, LocaleInfo parent, boolean translatedOnly) {
        fillCache(context);
        String parentId = parent == null ? null : parent.getId();
        HashSet<LocaleInfo> result = new HashSet<>();
        ArrayList<LocaleInfo> localeCacheValList = new ArrayList<>();
        localeCacheValList.addAll(sLocaleCache.values());
        Iterator<LocaleInfo> it = localeCacheValList.iterator();
        while (it.hasNext()) {
            LocaleInfo li = it.next();
            if (getLevel(ignorables, li, translatedOnly) == 2) {
                if (parent != null) {
                    if (parentId.equals(li.getParent().toLanguageTag())) {
                        result.add(li);
                    }
                } else if (li.isSuggestionOfType(1)) {
                    result.add(li);
                } else {
                    result.add(getLocaleInfo(li.getParent()));
                }
            }
        }
        return result;
    }

    @UnsupportedAppUsage
    public static LocaleInfo getLocaleInfo(Locale locale) {
        String id = locale.toLanguageTag();
        if (sLocaleCache.containsKey(id)) {
            return sLocaleCache.get(id);
        }
        LocaleInfo result = new LocaleInfo(locale);
        sLocaleCache.put(id, result);
        return result;
    }
}
