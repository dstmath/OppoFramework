package com.google.i18n.phonenumbers;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.prefixmapper.PrefixFileReader;
import java.util.Locale;

public class PhoneNumberToCarrierMapper {
    private static final String MAPPING_DATA_DIRECTORY = "/com/google/i18n/phonenumbers/carrier/data/";
    private static PhoneNumberToCarrierMapper instance = null;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private PrefixFileReader prefixFileReader = null;

    PhoneNumberToCarrierMapper(String phonePrefixDataDirectory) {
        this.prefixFileReader = new PrefixFileReader(phonePrefixDataDirectory);
    }

    public static synchronized PhoneNumberToCarrierMapper getInstance() {
        PhoneNumberToCarrierMapper phoneNumberToCarrierMapper;
        synchronized (PhoneNumberToCarrierMapper.class) {
            if (instance == null) {
                instance = new PhoneNumberToCarrierMapper(MAPPING_DATA_DIRECTORY);
            }
            phoneNumberToCarrierMapper = instance;
        }
        return phoneNumberToCarrierMapper;
    }

    public String getNameForValidNumber(Phonenumber.PhoneNumber number, Locale languageCode) {
        return this.prefixFileReader.getDescriptionForNumber(number, languageCode.getLanguage(), "", languageCode.getCountry());
    }

    public String getNameForNumber(Phonenumber.PhoneNumber number, Locale languageCode) {
        if (isMobile(this.phoneUtil.getNumberType(number))) {
            return getNameForValidNumber(number, languageCode);
        }
        return "";
    }

    public String getSafeDisplayName(Phonenumber.PhoneNumber number, Locale languageCode) {
        PhoneNumberUtil phoneNumberUtil = this.phoneUtil;
        if (phoneNumberUtil.isMobileNumberPortableRegion(phoneNumberUtil.getRegionCodeForNumber(number))) {
            return "";
        }
        return getNameForNumber(number, languageCode);
    }

    private boolean isMobile(PhoneNumberUtil.PhoneNumberType numberType) {
        return numberType == PhoneNumberUtil.PhoneNumberType.MOBILE || numberType == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE || numberType == PhoneNumberUtil.PhoneNumberType.PAGER;
    }
}
