package com.android.i18n.phonenumbers.geocoding;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader;
import java.util.List;
import java.util.Locale;

public class PhoneNumberOfflineGeocoder {
    private static final String MAPPING_DATA_DIRECTORY = "/com/android/i18n/phonenumbers/geocoding/data/";
    private static PhoneNumberOfflineGeocoder instance = null;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private PrefixFileReader prefixFileReader = null;

    PhoneNumberOfflineGeocoder(String phonePrefixDataDirectory) {
        this.prefixFileReader = new PrefixFileReader(phonePrefixDataDirectory);
    }

    public static synchronized PhoneNumberOfflineGeocoder getInstance() {
        PhoneNumberOfflineGeocoder phoneNumberOfflineGeocoder;
        synchronized (PhoneNumberOfflineGeocoder.class) {
            if (instance == null) {
                instance = new PhoneNumberOfflineGeocoder(MAPPING_DATA_DIRECTORY);
            }
            phoneNumberOfflineGeocoder = instance;
        }
        return phoneNumberOfflineGeocoder;
    }

    private String getCountryNameForNumber(PhoneNumber number, Locale language) {
        List<String> regionCodes = this.phoneUtil.getRegionCodesForCountryCode(number.getCountryCode());
        if (regionCodes.size() == 1) {
            return getRegionDisplayName((String) regionCodes.get(0), language);
        }
        String regionWhereNumberIsValid = "ZZ";
        for (String regionCode : regionCodes) {
            if (this.phoneUtil.isValidNumberForRegion(number, regionCode)) {
                if (!regionWhereNumberIsValid.equals("ZZ")) {
                    return "";
                }
                regionWhereNumberIsValid = regionCode;
            }
        }
        return getRegionDisplayName(regionWhereNumberIsValid, language);
    }

    private String getRegionDisplayName(String regionCode, Locale language) {
        return (regionCode == null || regionCode.equals("ZZ") || regionCode.equals(PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY)) ? "" : new Locale("", regionCode).getDisplayCountry(language);
    }

    public String getDescriptionForValidNumber(PhoneNumber number, Locale languageCode) {
        String areaDescription;
        String langStr = languageCode.getLanguage();
        String scriptStr = "";
        String regionStr = languageCode.getCountry();
        String mobileToken = PhoneNumberUtil.getCountryMobileToken(number.getCountryCode());
        String nationalNumber = this.phoneUtil.getNationalSignificantNumber(number);
        if (mobileToken.equals("") || !nationalNumber.startsWith(mobileToken)) {
            areaDescription = this.prefixFileReader.getDescriptionForNumber(number, langStr, scriptStr, regionStr);
        } else {
            PhoneNumber copiedNumber;
            try {
                copiedNumber = this.phoneUtil.parse(nationalNumber.substring(mobileToken.length()), this.phoneUtil.getRegionCodeForCountryCode(number.getCountryCode()));
            } catch (NumberParseException e) {
                copiedNumber = number;
            }
            areaDescription = this.prefixFileReader.getDescriptionForNumber(copiedNumber, langStr, scriptStr, regionStr);
        }
        if (areaDescription.length() > 0) {
            return areaDescription;
        }
        return getCountryNameForNumber(number, languageCode);
    }

    public String getDescriptionForValidNumber(PhoneNumber number, Locale languageCode, String userRegion) {
        String regionCode = this.phoneUtil.getRegionCodeForNumber(number);
        if (userRegion.equals(regionCode)) {
            return getDescriptionForValidNumber(number, languageCode);
        }
        return getRegionDisplayName(regionCode, languageCode);
    }

    public String getDescriptionForNumber(PhoneNumber number, Locale languageCode) {
        PhoneNumberType numberType = this.phoneUtil.getNumberType(number);
        if (numberType == PhoneNumberType.UNKNOWN) {
            return "";
        }
        if (this.phoneUtil.isNumberGeographical(numberType, number.getCountryCode())) {
            return getDescriptionForValidNumber(number, languageCode);
        }
        return getCountryNameForNumber(number, languageCode);
    }

    public String getDescriptionForNumber(PhoneNumber number, Locale languageCode, String userRegion) {
        PhoneNumberType numberType = this.phoneUtil.getNumberType(number);
        if (numberType == PhoneNumberType.UNKNOWN) {
            return "";
        }
        if (this.phoneUtil.isNumberGeographical(numberType, number.getCountryCode())) {
            return getDescriptionForValidNumber(number, languageCode, userRegion);
        }
        return getCountryNameForNumber(number, languageCode);
    }
}
