package com.android.server.pm;

import android.util.Slog;
import com.android.server.oppo.TemperatureProvider;
import java.util.ArrayList;
import java.util.Objects;

/* access modifiers changed from: package-private */
/* compiled from: PackageManagerXmlParse */
public class ThirdPartPackage {
    static final String TAG = "PackageManagerXmlParse";
    private String xmlCountry;
    private String xmlEuexCountry;
    private String xmlFlag;
    private String xmlLightOSConfig;
    private String xmlOperator;
    private String xmlPacakgeName;
    private String xmlPackagePath;
    private String xmlProject;
    private String xmlReverseMatch;

    ThirdPartPackage(String xmlPacakgeName2, String xmlReverseMatch2, String xmlLightOSConfig2, String xmlPackagePath2, String xmlProject2, String xmlCountry2, String xmlEuexCountry2, String xmlOperator2, String xmlFlag2) {
        this.xmlPacakgeName = xmlPacakgeName2;
        this.xmlReverseMatch = xmlReverseMatch2;
        this.xmlLightOSConfig = xmlLightOSConfig2;
        this.xmlPackagePath = xmlPackagePath2;
        this.xmlProject = xmlProject2;
        this.xmlCountry = xmlCountry2;
        this.xmlEuexCountry = xmlEuexCountry2;
        this.xmlOperator = xmlOperator2;
        this.xmlFlag = xmlFlag2;
    }

    public boolean isContentHit(String xmlList, String currentValue) {
        String[] getListArray = xmlList.split(",");
        if (getListArray == null) {
            return true;
        }
        ArrayList<String> itemList = new ArrayList<>();
        for (String str : getListArray) {
            String singleXmlValue = str.trim();
            if (singleXmlValue.startsWith("-")) {
                itemList.add(singleXmlValue.substring(singleXmlValue.indexOf("-") + 1));
                if (singleXmlValue.substring(singleXmlValue.indexOf("-") + 1).equalsIgnoreCase(currentValue)) {
                    return false;
                }
            } else {
                itemList.add(singleXmlValue);
            }
            if (currentValue.equalsIgnoreCase(singleXmlValue) || "common".equalsIgnoreCase(singleXmlValue)) {
                return true;
            }
        }
        if (!xmlList.contains("-") || itemList.contains(currentValue)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00da A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0125 A[RETURN] */
    public boolean ifSuit(PhoneInfo phoneObject) {
        if (getxmlFlag().equalsIgnoreCase(PackageManagerXmlParse.DATAFLAG) && phoneObject.getPhoneOSInfo().equalsIgnoreCase(TemperatureProvider.SWITCH_ON) && getXmlLightOSConfig().equalsIgnoreCase(TemperatureProvider.SWITCH_OFF)) {
            Slog.d(TAG, "data: remove " + getXmlPacakgeName() + " for ligthOS");
            return false;
        } else if (getxmlFlag().equalsIgnoreCase(PackageManagerXmlParse.SYSTEMFLAG) && phoneObject.getPhoneOSInfo().equalsIgnoreCase(TemperatureProvider.SWITCH_ON) && getXmlLightOSConfig().equalsIgnoreCase(TemperatureProvider.SWITCH_OFF)) {
            Slog.d(TAG, "system: disable " + getXmlPacakgeName() + " for ligthOS");
            return true;
        } else if (getXmlReverseMatch().equalsIgnoreCase(TemperatureProvider.SWITCH_ON)) {
            boolean projectHit = isContentHit(getXmlProject(), phoneObject.getPhoneProject());
            boolean countryHit = isContentHit(getXmlCountry(), phoneObject.getPhtoneCountry());
            boolean operatorHit = isContentHit(getXmlOperator(), phoneObject.getPhoneOperator());
            boolean euexCountryHit = isContentHit(getXmlEuexcountry(), phoneObject.getPhtoneEuexCountry());
            if (getXmlEuexcountry().equals("")) {
                if (!projectHit || !countryHit || !operatorHit) {
                    return true;
                }
                return false;
            } else if (projectHit && countryHit && operatorHit && euexCountryHit) {
                return false;
            }
            return true;
        } else {
            boolean projectHit2 = isContentHit(getXmlProject(), phoneObject.getPhoneProject());
            boolean countryHit2 = isContentHit(getXmlCountry(), phoneObject.getPhtoneCountry());
            boolean operatorHit2 = isContentHit(getXmlOperator(), phoneObject.getPhoneOperator());
            boolean euexCountryHit2 = isContentHit(getXmlEuexcountry(), phoneObject.getPhtoneEuexCountry());
            if (getXmlEuexcountry().equals("")) {
                if (!projectHit2 || !countryHit2 || !operatorHit2) {
                    return false;
                }
                return true;
            } else if (projectHit2 && countryHit2 && operatorHit2 && euexCountryHit2) {
                return true;
            }
            return false;
        }
    }

    public int hashCode() {
        if (getXmlPacakgeName() == null || getXmlPacakgeName().equalsIgnoreCase("")) {
            return this.xmlPackagePath.hashCode() & Integer.MAX_VALUE;
        }
        return getXmlPacakgeName().hashCode() & Integer.MAX_VALUE;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ThirdPartPackage thirdPartPackage = (ThirdPartPackage) obj;
        if (!Objects.equals(getXmlPacakgeName(), thirdPartPackage.getXmlPacakgeName()) || !Objects.equals(getXmlPackagePath(), thirdPartPackage.getXmlPackagePath())) {
            return false;
        }
        return true;
    }

    public String getXmlPacakgeNameOrPathName() {
        if (getXmlPacakgeName().equals("") || getXmlPacakgeName() == null) {
            return getXmlPackagePath();
        }
        return getXmlPacakgeName();
    }

    public String getXmlPacakgeName() {
        return this.xmlPacakgeName;
    }

    public String getXmlReverseMatch() {
        return this.xmlReverseMatch;
    }

    public String getXmlPackagePath() {
        return this.xmlPackagePath;
    }

    public String getXmlProject() {
        return this.xmlProject;
    }

    public String getXmlLightOSConfig() {
        return this.xmlLightOSConfig;
    }

    public String getXmlCountry() {
        return this.xmlCountry;
    }

    public String getXmlEuexcountry() {
        return this.xmlEuexCountry;
    }

    public String getXmlOperator() {
        return this.xmlOperator;
    }

    public String getxmlFlag() {
        return this.xmlFlag;
    }
}
