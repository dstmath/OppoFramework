package com.android.i18n.phonenumbers.geocoding;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader;
import java.util.List;
import java.util.Locale;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class PhoneNumberOfflineGeocoder {
    private static final String MAPPING_DATA_DIRECTORY = "/com/android/i18n/phonenumbers/geocoding/data/";
    private static PhoneNumberOfflineGeocoder instance;
    private final PhoneNumberUtil phoneUtil;
    private PrefixFileReader prefixFileReader;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder.<clinit>():void");
    }

    PhoneNumberOfflineGeocoder(String phonePrefixDataDirectory) {
        this.prefixFileReader = null;
        this.phoneUtil = PhoneNumberUtil.getInstance();
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
        if (canBeGeocoded(numberType)) {
            return getDescriptionForValidNumber(number, languageCode);
        }
        return getCountryNameForNumber(number, languageCode);
    }

    public String getDescriptionForNumber(PhoneNumber number, Locale languageCode, String userRegion) {
        PhoneNumberType numberType = this.phoneUtil.getNumberType(number);
        if (numberType == PhoneNumberType.UNKNOWN) {
            return "";
        }
        if (canBeGeocoded(numberType)) {
            return getDescriptionForValidNumber(number, languageCode, userRegion);
        }
        return getCountryNameForNumber(number, languageCode);
    }

    private boolean canBeGeocoded(PhoneNumberType numberType) {
        if (numberType == PhoneNumberType.FIXED_LINE || numberType == PhoneNumberType.MOBILE || numberType == PhoneNumberType.FIXED_LINE_OR_MOBILE) {
            return true;
        }
        return false;
    }
}
