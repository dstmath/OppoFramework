package com.mediatek.ims.legacy.ss;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.mediatek.ims.SuppSrvConfig;
import com.mediatek.ims.internal.ImsXuiManager;
import com.mediatek.internal.telephony.MtkSuppServHelper;
import com.mediatek.internal.telephony.dataconnection.MtkDcHelper;
import com.mediatek.simservs.client.SimServs;
import com.mediatek.telephony.MtkTelephonyManagerEx;

public class MMTelSSUtils {
    private static final String LOG_TAG = "MMTelSSUtils";
    private static final String MODE_SS_CS = "Prefer CS";
    private static final String MODE_SS_XCAP = "Prefer XCAP";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final String PROP_SS_MODE = "persist.vendor.radio.ss.mode";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final String SS_SERVICE_CLASS_PROP = "vendor.gsm.radio.ss.sc";
    private static final boolean TELDBG;
    private static SimServs mSimservs = null;

    static {
        boolean z = SENLOG;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    public static SimServs initSimserv(Context context, int phoneId) {
        mSimservs = SimServs.getInstance();
        if (mSimservs.getPhoneId() != phoneId) {
            Rlog.d(LOG_TAG, "[initSimserv] old PhoneId:" + mSimservs.getPhoneId() + " new PhoneId:" + phoneId);
            mSimservs.resetParameters();
            mSimservs.setPhoneId(phoneId);
        }
        SuppSrvConfig ssConfig = SuppSrvConfig.getInstance(context);
        mSimservs.setUseHttpProtocolScheme(ssConfig.isUseHttpProtocolScheme());
        if (ssConfig.getElementContentType() != null && !ssConfig.getElementContentType().isEmpty()) {
            mSimservs.setElementUpdateContentType(SENLOG, ssConfig.getElementContentType());
        }
        mSimservs.setHandleError409(ssConfig.isHandleError409());
        mSimservs.setFillCompleteForwardTo(ssConfig.isFillCompleteForwardTo());
        mSimservs.setXcapNSPrefixSS(ssConfig.isXcapNsPrefixSS());
        mSimservs.setSimservQueryWhole(ssConfig.isQueryWholeSimServ());
        mSimservs.setETagDisable(ssConfig.isDisableEtag());
        mSimservs.setAttrNeedQuotationMark(ssConfig.isAttrNeedQuotationMark());
        if (ssConfig.getAUID() != null && !ssConfig.getAUID().isEmpty()) {
            mSimservs.setAUID(ssConfig.getAUID());
        }
        if (ssConfig.getXcapRoot() != null && !ssConfig.getXcapRoot().isEmpty()) {
            mSimservs.setXcapRoot(ssConfig.getXcapRoot());
        }
        mSimservs.setXui(getXui(phoneId, context));
        String xcapRoot = getXcapRootUri(phoneId, context);
        if (xcapRoot != null && !xcapRoot.isEmpty()) {
            mSimservs.setXcapRoot(addXcapRootPort(xcapRoot, phoneId, context));
        }
        mSimservs.setIntendedId(getXIntendedId(phoneId, context));
        mSimservs.setContext(context);
        mSimservs.setPhoneId(phoneId);
        XcapMobileDataNetworkManager.setKeepAliveTimer(ssConfig.getDataKeepAliveTimer());
        if (ssConfig.getRequestDataTimer() > 0) {
            XcapMobileDataNetworkManager.setRequestDataTimer(ssConfig.getRequestDataTimer());
        }
        if (ssConfig.getDataCoolDownTimer() > 0) {
            XcapMobileDataNetworkManager.setDataCoolDownTimer(ssConfig.getDataCoolDownTimer());
        }
        return mSimservs;
    }

    public static String getXcapRootUri(int phoneId, Context context) {
        String mccMnc;
        String rootUri = SimServs.getInstance().getXcapRoot();
        Rlog.d(LOG_TAG, "getXcapRootUri():" + rootUri);
        int subId = getSubIdUsingPhoneId(phoneId);
        if (rootUri != null) {
            return rootUri;
        }
        String impi = MtkTelephonyManagerEx.getDefault().getIsimImpi(subId);
        if (isValidIMPI(impi)) {
            Rlog.d(LOG_TAG, "getXcapRootUri():get APP_FAM_IMS and impi=" + MtkSuppServHelper.encryptString(impi));
            mSimservs.setXcapRootByImpi(impi);
        } else {
            if (MtkDcHelper.isCdma4GDualModeCard(phoneId)) {
                mccMnc = MtkTelephonyManagerEx.getDefault().getSimOperatorNumericForPhoneEx(phoneId)[0];
                if (mccMnc == null || mccMnc.length() <= 0) {
                    mccMnc = TelephonyManager.getDefault().getSimOperator(subId);
                }
            } else {
                mccMnc = TelephonyManager.getDefault().getSimOperator(subId);
            }
            String mcc = "";
            String mnc = "";
            if (!TextUtils.isEmpty(mccMnc)) {
                mcc = mccMnc.substring(0, 3);
                mnc = mccMnc.substring(3);
            }
            if (mnc.length() == 2) {
                mccMnc = mcc + 0 + mnc;
                Rlog.d(LOG_TAG, "add 0 to mnc =" + mnc);
            }
            Rlog.d(LOG_TAG, "get mccMnc=" + mccMnc + " from the IccRecrods");
            if (!TextUtils.isEmpty(mccMnc)) {
                if (mccMnc.equals("460000") || mccMnc.equals("460002") || mccMnc.equals("460007") || mccMnc.equals("460008") || mccMnc.equals("460004")) {
                    mSimservs.setXcapRootByMccMnc("460", "000");
                } else {
                    mSimservs.setXcapRootByMccMnc(mccMnc.substring(0, 3), mccMnc.substring(3));
                }
            }
        }
        String rootUri2 = mSimservs.getXcapRoot();
        Rlog.d(LOG_TAG, "getXcapRoot():rootUri=" + rootUri2);
        return rootUri2;
    }

    public static String getXui(int phoneId, Context context) {
        String mccMnc;
        String sXui = ImsXuiManager.getInstance().getXui(phoneId);
        Rlog.d(LOG_TAG, "getXui():sXui from XuiManager = " + MtkSuppServHelper.encryptString(sXui));
        int subId = getSubIdUsingPhoneId(phoneId);
        if (sXui != null) {
            return getSipUriFromXui(sXui);
        }
        SimServs simSrv = SimServs.getInstance();
        String sXui2 = simSrv.getXui();
        Rlog.d(LOG_TAG, "getXui():sXui from simSrv=" + MtkSuppServHelper.encryptString(sXui2));
        if (sXui2 != null) {
            String sXui3 = getSipUriFromXui(sXui2);
            simSrv.setXui(sXui3);
            return sXui3;
        }
        String[] impu = MtkTelephonyManagerEx.getDefault().getIsimImpu(subId);
        if (impu == null || impu[0] == null || impu[0].isEmpty()) {
            String sImsi = ((TelephonyManager) context.getSystemService("phone")).getSubscriberId(getSubIdUsingPhoneId(phoneId));
            Rlog.d(LOG_TAG, "getXui():IMS uiccApp is null, try to select USIM uiccApp");
            if (MtkDcHelper.isCdma4GDualModeCard(phoneId)) {
                mccMnc = MtkTelephonyManagerEx.getDefault().getSimOperatorNumericForPhoneEx(phoneId)[0];
                if (mccMnc == null || mccMnc.length() <= 0) {
                    mccMnc = TelephonyManager.getDefault().getSimOperator(subId);
                }
            } else {
                mccMnc = TelephonyManager.getDefault().getSimOperator(subId);
            }
            Rlog.d(LOG_TAG, "getXui():Imsi=" + MtkSuppServHelper.encryptString(sImsi) + ", mccMnc=" + mccMnc);
            if (!TextUtils.isEmpty(mccMnc)) {
                mSimservs.setXuiByImsiMccMnc(sImsi, mccMnc.substring(0, 3), mccMnc.substring(3));
            }
        } else {
            String sImpu = impu[0];
            Rlog.d(LOG_TAG, "getXui():sImpu=" + MtkSuppServHelper.encryptString(sImpu));
            mSimservs.setXuiByImpu(sImpu);
        }
        String sXui4 = mSimservs.getXui();
        Rlog.d(LOG_TAG, "getXui():sXui=" + MtkSuppServHelper.encryptString(sXui4));
        return sXui4;
    }

    public static String getXIntendedId(int phoneId, Context context) {
        return getXui(phoneId, context);
    }

    public static boolean isPreferXcap(int phoneId, Context context) {
        String ssMode;
        boolean r = true;
        SuppSrvConfig ssConfig = SuppSrvConfig.getInstance(context);
        if (!SystemProperties.get("persist.vendor.ims_support").equals("1") || !SystemProperties.get("persist.vendor.volte_support").equals("1")) {
            Rlog.d(LOG_TAG, "isPreferXcap(): Not Enable VOLTE feature!");
            return SENLOG;
        }
        if (!SystemProperties.get("persist.vendor.ims_support").equals("1") || !SystemProperties.get("persist.vendor.volte_support").equals("1")) {
            ssMode = SystemProperties.get(PROP_SS_MODE, MODE_SS_CS);
        } else {
            ssMode = SystemProperties.get(PROP_SS_MODE, MODE_SS_XCAP);
            if (ssConfig.isNotSupportXcap()) {
                ssMode = MODE_SS_CS;
            }
        }
        if (MODE_SS_CS.equals(ssMode)) {
            r = SENLOG;
        }
        Rlog.d(LOG_TAG, "isPreferXcap() " + ssMode);
        return r;
    }

    public static String addXcapRootPort(String xcapRoot, int phoneId, Context context) {
        if (!"http".equals(xcapRoot.substring(0, xcapRoot.lastIndexOf(58))) && !"https".equals(xcapRoot.substring(0, xcapRoot.lastIndexOf(58)))) {
            return xcapRoot;
        }
        if (xcapRoot.charAt(xcapRoot.length() - 1) == '/') {
            xcapRoot = xcapRoot.substring(0, xcapRoot.length() - 1);
        }
        int port = SuppSrvConfig.getInstance(context).getPort();
        if (port != 0) {
            xcapRoot = xcapRoot + ":" + port;
        }
        return xcapRoot + "/";
    }

    public static String appendCountryCode(String dialNumber, int phoneId) {
        String currIso = TelephonyManager.getDefault().getNetworkCountryIsoForPhone(phoneId);
        Rlog.d(LOG_TAG, "currIso: " + currIso);
        int countryCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(currIso.toUpperCase());
        if (countryCode == 0) {
            Rlog.d(LOG_TAG, "Country code not found.");
            return dialNumber;
        }
        String countryCodeStr = Integer.toString(countryCode);
        if (dialNumber == null || dialNumber.isEmpty() || countryCodeStr == null) {
            return dialNumber;
        }
        if (dialNumber.substring(0, 1).equals("+")) {
            Rlog.d(LOG_TAG, "No need to append country code: " + MtkSuppServHelper.encryptString(dialNumber));
            return dialNumber;
        }
        String dialNumberWithCountryCode = "+" + countryCodeStr + dialNumber;
        Rlog.d(LOG_TAG, "dialNumberWithCountryCode: " + MtkSuppServHelper.encryptString(dialNumberWithCountryCode));
        return dialNumberWithCountryCode;
    }

    public static boolean isValidIMPI(String impi) {
        Rlog.d(LOG_TAG, "isValidIMPI, impi= " + MtkSuppServHelper.encryptString(impi));
        if (impi == null || impi.isEmpty() || !impi.contains("@")) {
            return SENLOG;
        }
        return true;
    }

    public static int getServiceClass() {
        return Integer.parseInt(SystemProperties.get(SS_SERVICE_CLASS_PROP, "-1"));
    }

    public static void resetServcieClass() {
        SystemProperties.set(SS_SERVICE_CLASS_PROP, "-1");
    }

    public static String getSipUriFromXui(String sXui) {
        String sipXui = null;
        String[] sXuiArray = sXui.split(",");
        boolean isContainSipUri = SENLOG;
        int i = 0;
        while (true) {
            if (i < sXuiArray.length) {
                if (sXuiArray[i] != null && !sXuiArray[i].isEmpty() && sXuiArray[i].contains("sip:")) {
                    sipXui = sXuiArray[i];
                    isContainSipUri = true;
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        if (!isContainSipUri) {
            sipXui = sXuiArray[0];
        }
        Rlog.d(LOG_TAG, "getSipUriFromXui: " + MtkSuppServHelper.encryptString(sipXui));
        return sipXui;
    }

    private static int getSubIdUsingPhoneId(int phoneId) {
        int[] values = SubscriptionManager.getSubId(phoneId);
        if (values == null || values.length <= 0) {
            return SubscriptionManager.getDefaultSubscriptionId();
        }
        return values[0];
    }
}
