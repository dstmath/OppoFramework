package com.mediatek.telephony;

import android.app.ActivityThread;
import android.content.Context;
import android.net.NetworkStats;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.mediatek.gwsd.GwsdListener;
import com.mediatek.gwsd.IGwsdService;
import com.mediatek.internal.telephony.FemtoCellInfo;
import com.mediatek.internal.telephony.IMtkPhoneSubInfoEx;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.MtkIccCardConstants;
import com.mediatek.internal.telephony.MtkTelephonyProperties;
import com.mediatek.internal.telephony.PseudoCellInfo;
import java.util.Arrays;
import java.util.List;

public class MtkTelephonyManagerEx {
    public static final int APP_FAM_3GPP = 1;
    public static final int APP_FAM_3GPP2 = 2;
    public static final int APP_FAM_NONE = 0;
    public static final int CARD_TYPE_CSIM = 4;
    public static final int CARD_TYPE_NONE = 0;
    public static final int CARD_TYPE_RUIM = 8;
    public static final int CARD_TYPE_SIM = 1;
    public static final int CARD_TYPE_USIM = 2;
    private static final String PRLVERSION = "vendor.cdma.prl.version";
    private static final String[] PROPERTY_RIL_CDMA_CARD_TYPE = {"vendor.ril.cdma.card.type.1", "vendor.ril.cdma.card.type.2", "vendor.ril.cdma.card.type.3", "vendor.ril.cdma.card.type.4"};
    private static final String[] PROPERTY_RIL_CT3G = {"vendor.gsm.ril.ct3g", "vendor.gsm.ril.ct3g.2", "vendor.gsm.ril.ct3g.3", "vendor.gsm.ril.ct3g.4"};
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    public static final String PROPERTY_SIM_CARD_ONOFF = "ro.vendor.mtk_sim_card_onoff";
    public static final String[] PROPERTY_SIM_ONOFF_STATE = {"vendor.ril.sim.onoff.state1", "vendor.ril.sim.onoff.state2", "vendor.ril.sim.onoff.state3", "vendor.ril.sim.onoff.state4"};
    public static final String PROPERTY_SIM_ONOFF_SUPPORT = "vendor.ril.sim.onoff.support";
    private static final String[] PROPERTY_SIM_SLOT_LOCK_CARD_VALID = {"vendor.gsm.sim.slot.lock.card.valid", "vendor.gsm.sim.slot.lock.card.valid.2", "vendor.gsm.sim.slot.lock.card.valid.3", "vendor.gsm.sim.slot.lock.card.valid.4"};
    private static final String PROPERTY_SIM_SLOT_LOCK_POLICY = "vendor.gsm.sim.slot.lock.policy";
    private static final String[] PROPERTY_SIM_SLOT_LOCK_SERVICE_CAPABILITY = {"vendor.gsm.sim.slot.lock.service.capability", "vendor.gsm.sim.slot.lock.service.capability.2", "vendor.gsm.sim.slot.lock.service.capability.3", "vendor.gsm.sim.slot.lock.service.capability.4"};
    private static final String PROPERTY_SIM_SLOT_LOCK_STATE = "vendor.gsm.sim.slot.lock.state";
    private static final String PROPERTY_SML_MODE = "ro.vendor.sim_me_lock_mode";
    public static final int SET_SIM_POWER_ERROR_ALREADY_SIM_OFF = 14;
    public static final int SET_SIM_POWER_ERROR_ALREADY_SIM_ON = 15;
    public static final int SET_SIM_POWER_ERROR_EXECUTING_SIM_OFF = 12;
    public static final int SET_SIM_POWER_ERROR_EXECUTING_SIM_ON = 13;
    public static final int SET_SIM_POWER_ERROR_NOT_ALLOWED = 54;
    public static final int SET_SIM_POWER_ERROR_NOT_SUPPORT = -1;
    public static final int SET_SIM_POWER_ERROR_SIM_ABSENT = 11;
    public static final int SET_SIM_POWER_SUCCESS = 0;
    public static final int SIM_POWER_STATE_EXECUTING_SIM_OFF = 10;
    public static final int SIM_POWER_STATE_EXECUTING_SIM_ON = 11;
    public static final int SIM_POWER_STATE_SIM_OFF = 10;
    public static final int SIM_POWER_STATE_SIM_ON = 11;
    private static final String TAG = "MtkTelephonyManagerEx";
    private static MtkTelephonyManagerEx sInstance = new MtkTelephonyManagerEx();
    private Context mContext;
    private boolean mIsSmlLockMode;
    private ITelephonyRegistry mRegistry;

    public MtkTelephonyManagerEx(Context context) {
        this.mContext = null;
        this.mIsSmlLockMode = SystemProperties.get(PROPERTY_SML_MODE, "").equals("3");
        this.mContext = context;
        this.mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
    }

    private MtkTelephonyManagerEx() {
        this.mContext = null;
        this.mIsSmlLockMode = SystemProperties.get(PROPERTY_SML_MODE, "").equals("3");
        this.mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
    }

    public static MtkTelephonyManagerEx getDefault() {
        return sInstance;
    }

    public int getPhoneType(int simId) {
        int[] subIds = SubscriptionManager.getSubId(simId);
        if (subIds == null) {
            return TelephonyManager.getDefault().getCurrentPhoneType(-1);
        }
        Rlog.e(TAG, "Deprecated! getPhoneType with simId " + simId + ", subId " + subIds[0]);
        return TelephonyManager.getDefault().getCurrentPhoneType(subIds[0]);
    }

    private int getSubIdBySlot(int slot) {
        int[] subId = SubscriptionManager.getSubId(slot);
        StringBuilder sb = new StringBuilder();
        sb.append("getSubIdBySlot, simId ");
        sb.append(slot);
        sb.append("subId ");
        sb.append(subId != null ? Integer.valueOf(subId[0]) : "invalid!");
        Rlog.d(TAG, sb.toString());
        return subId != null ? subId[0] : SubscriptionManager.getDefaultSubscriptionId();
    }

    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
    }

    private IMtkTelephonyEx getIMtkTelephonyEx() {
        return IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
    }

    private IPhoneSubInfo getSubscriberInfo() {
        return IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
    }

    public boolean isInDsdaMode() {
        if (!SystemProperties.get("ro.vendor.mtk_switch_antenna", MtkTelephonyProperties.CFU_QUERY_TYPE_DEF_VALUE).equals("1") && SystemProperties.getInt("ro.vendor.mtk_c2k_lte_mode", 0) == 1) {
            TelephonyManager tm = TelephonyManager.getDefault();
            int simCount = tm.getSimCount();
            for (int i = 0; i < simCount; i++) {
                int[] allSubId = SubscriptionManager.getSubId(i);
                if (allSubId == null) {
                    Rlog.d(TAG, "isInDsdaMode, allSubId is null for slot" + i);
                } else {
                    int phoneType = tm.getCurrentPhoneType(allSubId[0]);
                    Rlog.d(TAG, "isInDsdaMode, allSubId[0]:" + allSubId[0] + ", phoneType:" + phoneType);
                    if (phoneType == 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isInHomeNetwork(int subId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx == null) {
                return false;
            }
            return telephonyEx.isInHomeNetwork(subId);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    private IMtkPhoneSubInfoEx getMtkSubscriberInfoEx() {
        return IMtkPhoneSubInfoEx.Stub.asInterface(ServiceManager.getService("iphonesubinfoEx"));
    }

    public boolean getUsimService(int service) {
        return getUsimService(SubscriptionManager.getDefaultSubscriptionId(), service);
    }

    public boolean getUsimService(int subId, int service) {
        try {
            return getMtkSubscriberInfoEx().getUsimServiceForSubscriber(subId, service, getOpPackageName());
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public int getIccAppFamily(int slotId) {
        try {
            return getIMtkTelephonyEx().getIccAppFamily(slotId);
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    public String getIccCardType(int subId) {
        String type = null;
        try {
            type = getIMtkTelephonyEx().getIccCardType(subId);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex2) {
            ex2.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getIccCardType sub ");
        sb.append(subId);
        sb.append(" ,icc type ");
        sb.append(type != null ? type : "null");
        Rlog.d(TAG, sb.toString());
        return type;
    }

    private String getOpPackageName() {
        Context context = this.mContext;
        if (context != null) {
            return context.getOpPackageName();
        }
        return ActivityThread.currentOpPackageName();
    }

    public byte[] loadEFTransparent(int slotId, int family, int fileID, String filePath) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.loadEFTransparent(slotId, family, fileID, filePath);
            }
            return null;
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public List<String> loadEFLinearFixedAll(int slotId, int family, int fileID, String filePath) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.loadEFLinearFixedAll(slotId, family, fileID, filePath);
            }
            return null;
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getUimSubscriberId(int subId) {
        try {
            return getIMtkTelephonyEx().getUimSubscriberId(getOpPackageName(), subId);
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return "";
        } catch (NullPointerException ex2) {
            ex2.printStackTrace();
            return "";
        }
    }

    public String[] getSupportCardType(int slotId) {
        String[] values = null;
        if (slotId >= 0) {
            String[] strArr = PROPERTY_RIL_FULL_UICC_TYPE;
            if (slotId < strArr.length) {
                String prop = SystemProperties.get(strArr[slotId], "");
                if (!prop.equals("") && prop.length() > 0) {
                    values = prop.split(",");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("getSupportCardType slotId ");
                sb.append(slotId);
                sb.append(", prop value= ");
                sb.append(prop);
                sb.append(", size= ");
                sb.append(values != null ? values.length : 0);
                Rlog.d(TAG, sb.toString());
                return values;
            }
        }
        Rlog.e(TAG, "getSupportCardType: invalid slotId " + slotId);
        return null;
    }

    public boolean isCt3gDualMode(int slotId) {
        if (slotId >= 0) {
            String[] strArr = PROPERTY_RIL_CT3G;
            if (slotId < strArr.length) {
                String result = SystemProperties.get(strArr[slotId], "");
                Rlog.d(TAG, "isCt3gDualMode:  " + result);
                return "1".equals(result);
            }
        }
        Rlog.e(TAG, "isCt3gDualMode: invalid slotId " + slotId);
        return false;
    }

    public MtkIccCardConstants.CardType getCdmaCardType(int slotId) {
        if (slotId < 0 || slotId >= PROPERTY_RIL_CT3G.length) {
            Rlog.e(TAG, "getCdmaCardType: invalid slotId " + slotId);
            return null;
        }
        MtkIccCardConstants.CardType mCdmaCardType = MtkIccCardConstants.CardType.UNKNOW_CARD;
        String result = SystemProperties.get(PROPERTY_RIL_CDMA_CARD_TYPE[slotId], "");
        if (!result.equals("")) {
            mCdmaCardType = MtkIccCardConstants.CardType.getCardTypeFromInt(Integer.parseInt(result));
        }
        Rlog.d(TAG, "getCdmaCardType slotId: " + slotId + " result: " + result + "  mCdmaCardType: " + mCdmaCardType);
        return mCdmaCardType;
    }

    public String getSimSerialNumber(int simId) {
        if (simId < 0 || simId >= TelephonyManager.getDefault().getSimCount()) {
            Rlog.e(TAG, "getSimSerialNumber with invalid simId " + simId);
            return null;
        }
        String iccId = null;
        try {
            iccId = getIMtkTelephonyEx().getSimSerialNumber(getOpPackageName(), simId);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex2) {
            ex2.printStackTrace();
        }
        if (iccId == null) {
            return iccId;
        }
        if (iccId.equals("N/A") || iccId.equals("")) {
            return null;
        }
        return iccId;
    }

    public String[] getSimOperatorNumericForPhoneEx(int phoneId) {
        String str;
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getSimCount()) {
            Rlog.e(TAG, "getSimOperatorNumericForPhoneEx with invalid phoneId:" + phoneId);
            return null;
        }
        String[] values = null;
        try {
            values = getIMtkTelephonyEx().getSimOperatorNumericForPhoneEx(phoneId);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex2) {
            ex2.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getSimOperatorNumericForPhoneEx phoneId ");
        sb.append(phoneId);
        sb.append(" values = ");
        if (values != null) {
            str = values[0] + ", " + values[1];
        } else {
            str = "null";
        }
        sb.append(str);
        Rlog.d(TAG, sb.toString());
        return values;
    }

    public CellLocation getCellLocation(int simId) {
        CellLocation cl;
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony == null) {
                Rlog.d(TAG, "getCellLocation returning null because telephony is null");
                return null;
            }
            Bundle bundle = telephony.getCellLocationUsingSlotId(simId);
            if (bundle == null) {
                Rlog.d(TAG, "getCellLocation returning null because bundle is null");
                return null;
            } else if (bundle.isEmpty()) {
                Rlog.d(TAG, "getCellLocation returning null because bundle is empty");
                return null;
            } else {
                int phoneType = getPhoneType(simId);
                if (phoneType == 1) {
                    cl = new GsmCellLocation(bundle);
                } else if (phoneType != 2) {
                    cl = null;
                } else {
                    cl = new CdmaCellLocation(bundle);
                }
                Rlog.d(TAG, "getCellLocation is" + cl);
                if (cl == null) {
                    Rlog.d(TAG, "getCellLocation returning null because cl is null");
                    return null;
                } else if (!cl.isEmpty()) {
                    return cl;
                } else {
                    Rlog.d(TAG, "getCellLocation returning null because CellLocation is empty");
                    return null;
                }
            }
        } catch (RemoteException ex) {
            Rlog.d(TAG, "getCellLocation returning null due to RemoteException " + ex);
            return null;
        } catch (NullPointerException ex2) {
            Rlog.d(TAG, "getCellLocation returning null due to NullPointerException " + ex2);
            return null;
        }
    }

    public boolean isGsm(int radioTechnology) {
        return radioTechnology == 1 || radioTechnology == 2 || radioTechnology == 3 || radioTechnology == 8 || radioTechnology == 9 || radioTechnology == 10 || radioTechnology == 13 || radioTechnology == 15 || radioTechnology == 16 || radioTechnology == 17 || radioTechnology == 19;
    }

    public String getIsimImpi(int subId) {
        try {
            return getMtkSubscriberInfoEx().getIsimImpiForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getIsimDomain(int subId) {
        try {
            return getMtkSubscriberInfoEx().getIsimDomainForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String[] getIsimImpu(int subId) {
        try {
            return getMtkSubscriberInfoEx().getIsimImpuForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getIsimIst(int subId) {
        try {
            return getMtkSubscriberInfoEx().getIsimIstForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String[] getIsimPcscf(int subId) {
        try {
            return getMtkSubscriberInfoEx().getIsimPcscfForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public boolean isImsRegistered(int subId) {
        try {
            return getIMtkTelephonyEx().isImsRegistered(subId);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isVolteEnabled(int subId) {
        try {
            return getIMtkTelephonyEx().isVolteEnabled(subId);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isWifiCallingEnabled(int subId) {
        try {
            return getIMtkTelephonyEx().isWifiCallingEnabled(subId);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isWifiCalllingActive(int subId) {
        try {
            return getIMtkTelephonyEx().isWifiCallingEnabled(subId);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public String getIsimGbabp() {
        return getIsimGbabp(SubscriptionManager.getDefaultSubscriptionId());
    }

    public String getIsimGbabp(int subId) {
        try {
            return getMtkSubscriberInfoEx().getIsimGbabpForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public void setIsimGbabp(String gbabp, Message onComplete) {
        setIsimGbabp(SubscriptionManager.getDefaultSubscriptionId(), gbabp, onComplete);
    }

    public void setIsimGbabp(int subId, String gbabp, Message onComplete) {
        try {
            getMtkSubscriberInfoEx().setIsimGbabpForSubscriber(subId, gbabp, onComplete);
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    public String getUsimGbabp() {
        return getUsimGbabp(SubscriptionManager.getDefaultSubscriptionId());
    }

    public String getUsimGbabp(int subId) {
        try {
            return getMtkSubscriberInfoEx().getUsimGbabpForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public void setUsimGbabp(String gbabp, Message onComplete) {
        setUsimGbabp(SubscriptionManager.getDefaultSubscriptionId(), gbabp, onComplete);
    }

    public void setUsimGbabp(int subId, String gbabp, Message onComplete) {
        try {
            getMtkSubscriberInfoEx().setUsimGbabpForSubscriber(subId, gbabp, onComplete);
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    public String getPrlVersion(int subId) {
        int slotId = SubscriptionManager.getSlotIndex(subId);
        String prlVersion = SystemProperties.get(PRLVERSION + slotId, "");
        Rlog.d(TAG, "getPrlversion PRLVERSION subId = " + subId + " key = " + PRLVERSION + slotId + " value = " + prlVersion);
        return prlVersion;
    }

    public int[] setRxTestConfig(int config) {
        try {
            return getIMtkTelephonyEx().setRxTestConfig(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()), config);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public int[] getRxTestResult() {
        try {
            return getIMtkTelephonyEx().getRxTestResult(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()));
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public boolean exitEmergencyCallbackMode(int subId) {
        try {
            return getIMtkTelephonyEx().exitEmergencyCallbackMode(subId);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public void setApcMode(int slotId, int mode, boolean reportOn, int reportInterval) {
        if (slotId < 0 || slotId >= TelephonyManager.getDefault().getSimCount()) {
            Rlog.e(TAG, "setApcMode error with invalid slotId " + slotId);
        } else if (mode < 0 || mode > 2) {
            Rlog.e(TAG, "setApcMode error with invalid mode " + mode);
        } else {
            try {
                IMtkTelephonyEx telephony = getIMtkTelephonyEx();
                if (telephony == null) {
                    Rlog.e(TAG, "setApcMode error because telephony is null");
                } else {
                    telephony.setApcModeUsingSlotId(slotId, mode, reportOn, reportInterval);
                }
            } catch (RemoteException ex) {
                Rlog.e(TAG, "setApcMode error due to RemoteException " + ex);
            } catch (NullPointerException ex2) {
                Rlog.e(TAG, "setApcMode error due to NullPointerException " + ex2);
            }
        }
    }

    public PseudoCellInfo getApcInfo(int slotId) {
        if (slotId < 0 || slotId >= TelephonyManager.getDefault().getSimCount()) {
            Rlog.e(TAG, "getApcInfo with invalid slotId " + slotId);
            return null;
        }
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.getApcInfoUsingSlotId(slotId);
            }
            Rlog.e(TAG, "getApcInfo return null because telephony is null");
            return null;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getApcInfo returning null due to RemoteException " + ex);
            return null;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getApcInfo returning null due to NullPointerException " + ex2);
            return null;
        }
    }

    public int getCdmaSubscriptionActStatus(int subId) {
        try {
            return getIMtkTelephonyEx().getCdmaSubscriptionActStatus(subId);
        } catch (RemoteException e) {
            Rlog.d(TAG, "fail to getCdmaSubscriptionActStatus due to RemoteException");
            return 0;
        } catch (NullPointerException e2) {
            Rlog.d(TAG, "fail to getCdmaSubscriptionActStatus due to NullPointerException");
            return 0;
        }
    }

    public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.invokeOemRilRequestRaw(oemReq, oemResp);
            }
            return -1;
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    public int invokeOemRilRequestRawBySlot(int slotId, byte[] oemReq, byte[] oemResp) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.invokeOemRilRequestRawBySlot(slotId, oemReq, oemResp);
            }
            return -1;
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    public boolean isDigitsSupported() {
        return SystemProperties.getInt("persist.vendor.mtk_digits_support", 0) == 1;
    }

    public boolean isInCsCall(int phoneId) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.isInCsCall(phoneId);
            }
            Rlog.e(TAG, "[isInCsCall] telephony = null");
            return false;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "[isInCsCall] RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "[isInCsCall] NullPointerException " + ex2);
            return false;
        }
    }

    public int getSimCardState(int slotId) {
        int simCardState = SubscriptionManager.getSimStateForSlotIndex(slotId);
        if (simCardState == 0 || simCardState == 1 || simCardState == 8 || simCardState == 9) {
            return simCardState;
        }
        return 11;
    }

    public int getSimApplicationState(int slotId) {
        int simApplicationState = SubscriptionManager.getSimStateForSlotIndex(slotId);
        if (simApplicationState == 0 || simApplicationState == 1) {
            return 0;
        }
        if (simApplicationState == 5) {
            return 6;
        }
        if (simApplicationState == 8 || simApplicationState == 9) {
            return 0;
        }
        return simApplicationState;
    }

    public List<CellInfo> getAllCellInfo(int slotId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx == null) {
                return null;
            }
            return telephonyEx.getAllCellInfo(slotId, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getLocatedPlmn(int phoneId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.getLocatedPlmn(phoneId);
            }
            return null;
        } catch (RemoteException e) {
            Rlog.e(TAG, "fail to getLocatedPlmn due to RemoteException");
            return null;
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "fail to getLocatedPlmn due to NullPointerException");
            return null;
        }
    }

    public boolean setDisable2G(int phoneId, boolean mode) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.setDisable2G(phoneId, mode);
            }
            Rlog.e(TAG, "setDisable2G error because telephony is null");
            return false;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setDisable2G error due to RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setDisable2G error due to NullPointerException " + ex2);
            return false;
        }
    }

    public int getDisable2G(int phoneId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.getDisable2G(phoneId);
            }
            Rlog.e(TAG, "getDisable2G error because telephony is null");
            return -1;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getDisable2G error due to RemoteException " + ex);
            return -1;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getDisable2G error due to NullPointerException " + ex2);
            return -1;
        }
    }

    public List<FemtoCellInfo> getFemtoCellList(int phoneId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.getFemtoCellList(phoneId);
            }
            Rlog.e(TAG, "getFemtoCellList error because telephony is null");
            return null;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getFemtoCellList error due to RemoteException " + ex);
            return null;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getFemtoCellList error due to NullPointerException " + ex2);
            return null;
        }
    }

    public boolean abortFemtoCellList(int phoneId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.abortFemtoCellList(phoneId);
            }
            Rlog.e(TAG, "abortFemtoCellList error because telephony is null");
            return false;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "abortFemtoCellList error due to RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "abortFemtoCellList error due to NullPointerException " + ex2);
            return false;
        }
    }

    public boolean selectFemtoCell(int phoneId, FemtoCellInfo femtocell) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.selectFemtoCell(phoneId, femtocell);
            }
            Rlog.e(TAG, "selectFemtoCell error because telephony is null");
            return false;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "selectFemtoCell error due to RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "selectFemtoCell error due to NullPointerException " + ex2);
            return false;
        }
    }

    public int queryFemtoCellSystemSelectionMode(int phoneId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.queryFemtoCellSystemSelectionMode(phoneId);
            }
            Rlog.e(TAG, "queryFemtoCellSystemSelectionMode error because telephony is null");
            return -1;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "queryFemtoCellSystemSelectionMode error due to RemoteException " + ex);
            return -1;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "queryFemtoCellSystemSelectionMode due to NullPointerException " + ex2);
            return -1;
        }
    }

    public boolean setFemtoCellSystemSelectionMode(int phoneId, int mode) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.setFemtoCellSystemSelectionMode(phoneId, mode);
            }
            Rlog.e(TAG, "setFemtoCellSystemSelectionMode error because telephony is null");
            return false;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setFemtoCellSystemSelectionMode error due to RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setFemtoCellSystemSelectionMode due to NullPointerException " + ex2);
            return false;
        }
    }

    public boolean cancelAvailableNetworks(int phoneId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.cancelAvailableNetworks(phoneId);
            }
            Rlog.e(TAG, " cancelAvailableNetworks error because telephony is null");
            return false;
        } catch (RemoteException ex) {
            Rlog.e(TAG, " cancelAvailableNetworks error due to RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, " cancelAvailableNetworks error due to NullPointerException " + ex2);
            return false;
        }
    }

    public boolean isDssNoResetSupport() {
        if (SystemProperties.get("vendor.ril.simswitch.no_reset_support").equals("1")) {
            Rlog.d(TAG, "return true for isDssNoResetSupport");
            return true;
        }
        Rlog.d(TAG, "return false for isDssNoResetSupport");
        return false;
    }

    public int getProtocolStackId(int slot) {
        int majorSlot = 0;
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                majorSlot = telephonyEx.getMainCapabilityPhoneId();
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "fail to getMainCapabilityPhoneId due to RemoteException");
        } catch (NullPointerException e2) {
            Rlog.e(TAG, "fail to getMainCapabilityPhoneId due to NullPointerException");
        }
        if (slot == majorSlot) {
            return 1;
        }
        if (isDssNoResetSupport()) {
            if (slot < majorSlot) {
                return slot + 2;
            }
        } else if (slot == 0) {
            return majorSlot + 1;
        }
        return slot + 1;
    }

    public int getSimLockPolicy() {
        if (!this.mIsSmlLockMode) {
            return 0;
        }
        int policy = SystemProperties.getInt(PROPERTY_SIM_SLOT_LOCK_POLICY, -1);
        Rlog.d(TAG, "getSimLockPolicy: " + policy);
        return policy;
    }

    public int getShouldServiceCapability(int slotId) {
        if (!this.mIsSmlLockMode) {
            return 0;
        }
        if (slotId >= 0) {
            String[] strArr = PROPERTY_SIM_SLOT_LOCK_SERVICE_CAPABILITY;
            if (slotId < strArr.length) {
                int capability = SystemProperties.getInt(strArr[slotId], -1);
                Rlog.d(TAG, "getShouldServiceCapability: " + capability + ",slotId: " + slotId);
                return capability;
            }
        }
        Rlog.e(TAG, "getShouldServiceCapability: invalid slotId: " + slotId);
        return 4;
    }

    public int checkValidCard(int slotId) {
        if (!this.mIsSmlLockMode) {
            return 0;
        }
        if (slotId >= 0) {
            String[] strArr = PROPERTY_SIM_SLOT_LOCK_CARD_VALID;
            if (slotId < strArr.length) {
                int validCard = SystemProperties.getInt(strArr[slotId], -1);
                Rlog.d(TAG, "checkValidCard: " + validCard + ",slotId: " + slotId);
                return validCard;
            }
        }
        Rlog.e(TAG, "checkValidCard: invalid slotId " + slotId);
        return 2;
    }

    public int getSimLockState() {
        if (!this.mIsSmlLockMode) {
            return 1;
        }
        int lockState = SystemProperties.getInt(PROPERTY_SIM_SLOT_LOCK_STATE, -1);
        Rlog.d(TAG, "getSimLockState: " + lockState);
        return lockState;
    }

    public String getLine1PhoneNumber(int subId) {
        String number = null;
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                number = telephony.getLine1NumberForDisplay(subId, this.mContext.getOpPackageName());
            }
        } catch (RemoteException | NullPointerException e) {
        }
        if (number != null) {
            return number;
        }
        try {
            IMtkPhoneSubInfoEx info = getMtkSubscriberInfoEx();
            if (info == null) {
                return null;
            }
            return info.getLine1PhoneNumberForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e2) {
            return null;
        } catch (NullPointerException e3) {
            return null;
        }
    }

    public boolean isSimOnOffEnabled() {
        boolean result = SystemProperties.get(PROPERTY_SIM_CARD_ONOFF).equals("2") && SystemProperties.get(PROPERTY_SIM_ONOFF_SUPPORT).equals("1");
        Rlog.d(TAG, "isSimOnOffEnabled result = " + result);
        return result;
    }

    public int getSimOnOffState(int slotId) {
        int result = -1;
        if (slotId < 0 || slotId >= TelephonyManager.getDefault().getSimCount()) {
            Rlog.e(TAG, "getSimOnOffState error with invalid slotId " + slotId);
            return -1;
        }
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                result = telephonyEx.getSimOnOffState(slotId);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "Error calling ITelephony#getSimOnOffState", ex);
        } catch (SecurityException ex2) {
            Rlog.e(TAG, "Permission error calling ITelephony#getSimOnOffState", ex2);
        }
        Rlog.d(TAG, "getSimOnOffState slotId = " + slotId + " result = " + result);
        return result;
    }

    public int setSimPower(int slotIndex, int state) {
        if (slotIndex < 0 || slotIndex >= TelephonyManager.getDefault().getSimCount()) {
            Rlog.e(TAG, "setSimPower error with invalid slotIndex " + slotIndex);
            return -1;
        }
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.setSimPower(slotIndex, state);
            }
            return -1;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "Error calling ITelephony#setSimOnOffState", ex);
            return -1;
        } catch (SecurityException ex2) {
            Rlog.e(TAG, "Permission error calling ITelephony#setSimOnOffState", ex2);
            return -1;
        }
    }

    public int getSimOnOffExecutingState(int slotId) {
        if (slotId < 0 || slotId >= TelephonyManager.getDefault().getSimCount()) {
            Rlog.e(TAG, "getSimOnOffExecutingState error with invalid slotId " + slotId);
            return -1;
        }
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.getSimOnOffExecutingState(slotId);
            }
            return -1;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "Error calling ITelephony#getSimOnOffExecutingState", ex);
            return -1;
        } catch (SecurityException ex2) {
            Rlog.e(TAG, "Permission error calling ITelephony#getSimOnOffExecutingState", ex2);
            return -1;
        }
    }

    public void addGwsdListener(GwsdListener listener) {
        addGwsdListener(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()), listener);
    }

    public void addGwsdListener(int phoneId, GwsdListener listener) {
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                iGwsdService.addListener(phoneId, listener.callback);
            }
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void removeGwsdListener() {
        removeGwsdListener(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()));
    }

    public void removeGwsdListener(int phoneId) {
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                iGwsdService.removeListener(phoneId);
            }
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void setGwsdEnabled(boolean action) {
        setGwsdEnabled(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()), action);
    }

    public void setGwsdEnabled(int phoneId, boolean action) {
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                iGwsdService.setUserModeEnabled(phoneId, action);
            }
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void setGwsdAutoRejectEnabled(boolean action) {
        setGwsdAutoRejectEnabled(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()), action);
    }

    public void setGwsdAutoRejectEnabled(int phoneId, boolean action) {
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                iGwsdService.setAutoRejectModeEnabled(phoneId, action);
            }
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void syncGwsdInfo(boolean userEnable, boolean autoReject) {
        syncGwsdInfo(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()), userEnable, autoReject);
    }

    public void syncGwsdInfo(int phoneId, boolean userEnable, boolean autoReject) {
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                iGwsdService.syncGwsdInfo(phoneId, userEnable, autoReject);
            }
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void setCallValidTimer(int timer) {
        setCallValidTimer(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()), timer);
    }

    public void setCallValidTimer(int phoneId, int timer) {
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                iGwsdService.setCallValidTimer(phoneId, timer);
            }
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void setIgnoreSameNumberInterval(int internal) {
        setIgnoreSameNumberInterval(SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()), internal);
    }

    public void setIgnoreSameNumberInterval(int phoneId, int internal) {
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                iGwsdService.setIgnoreSameNumberInterval(phoneId, internal);
            }
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
        }
    }

    public boolean tearDownPdnByType(int phoneId, String type) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony == null) {
                Rlog.e(TAG, "tearDownPdnByType: telephony = null");
                return false;
            }
            telephony.tearDownPdnByType(phoneId, type);
            return true;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "tearDownPdnByType: RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "tearDownPdnByType: NullPointerException " + ex2);
            return false;
        }
    }

    public boolean setupPdnByType(int phoneId, String type) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony == null) {
                Rlog.e(TAG, "setupPdnByType: telephony = null");
                return false;
            }
            telephony.setupPdnByType(phoneId, type);
            return true;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setupPdnByType: RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setupPdnByType: NullPointerException " + ex2);
            return false;
        }
    }

    public ServiceState getServiceStateByPhoneId(int phoneId) {
        try {
            return getIMtkTelephonyEx().getServiceStateByPhoneId(phoneId, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public boolean setRoamingEnable(int phoneId, int[] config) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.setRoamingEnable(phoneId, config);
            }
            Rlog.e(TAG, "setRoamingEnable error because telephony is null");
            return false;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setRoamingEnable error due to RemoteException " + ex);
            return false;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setRoamingEnable error due to NullPointerException " + ex2);
            return false;
        }
    }

    public int[] getRoamingEnable(int phoneId) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.getRoamingEnable(phoneId);
            }
            Rlog.e(TAG, "getRoamingEnable error because telephony is null");
            return null;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getRoamingEnable error due to RemoteException " + ex);
            return null;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getRoamingEnable error due to NullPointerException " + ex2);
            return null;
        }
    }

    public String[] getSuggestedPlmnList(int phoneId, int rat, int num, int timer) {
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getSimCount()) {
            Rlog.e(TAG, "getSuggestedPlmnList with invalid phoneId:" + phoneId);
            return null;
        } else if (rat != 0 && rat != 1 && rat != 3) {
            Rlog.e(TAG, "getSuggestedPlmnList with invalid rat:" + rat);
            return null;
        } else if (num <= 0) {
            Rlog.e(TAG, "getSuggestedPlmnList with invalid num:" + num);
            return null;
        } else if (timer <= 0) {
            Rlog.e(TAG, "getSuggestedPlmnList with invalid timer:" + timer);
            return null;
        } else {
            String[] values = null;
            try {
                values = getIMtkTelephonyEx().getSuggestedPlmnList(phoneId, rat, num, timer, getOpPackageName());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (NullPointerException ex2) {
                ex2.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("getSuggestedPlmnList phoneId ");
            sb.append(phoneId);
            sb.append(" values = ");
            sb.append(values != null ? Arrays.toString(values) : "null");
            Rlog.d(TAG, sb.toString());
            return values;
        }
    }

    public NetworkStats getMobileDataUsage(int phoneId) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx != null) {
                return telephonyEx.getMobileDataUsage(phoneId);
            }
            Rlog.e(TAG, "getMobileDataUsage : telephony is null");
            return null;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getMobileDataUsage : RemoteException " + ex);
            return null;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getMobileDataUsage : NullPointerException " + ex2);
            return null;
        }
    }

    public void setMobileDataUsageSum(int phoneId, long txBytes, long txPkts, long rxBytes, long rxPkts) {
        try {
            IMtkTelephonyEx telephonyEx = getIMtkTelephonyEx();
            if (telephonyEx == null) {
                Rlog.e(TAG, "setMobileDataUsageSum : telephony is null");
            } else {
                telephonyEx.setMobileDataUsageSum(phoneId, txBytes, txPkts, rxBytes, rxPkts);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setMobileDataUsageSum : RemoteException " + ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setMobileDataUsageSum : NullPointerException " + ex2);
        }
    }

    public boolean isEmergencyNumber(int phoneId, String number) {
        try {
            IMtkTelephonyEx telephony = getIMtkTelephonyEx();
            if (telephony != null) {
                return telephony.isEmergencyNumber(phoneId, number);
            }
            Log.e(TAG, "isEmergencyNumber IMtkTelephonyEx is null");
            return false;
        } catch (RemoteException ex) {
            Log.e(TAG, "isEmergencyNumber RemoteException", ex);
            return false;
        }
    }

    public void setGwsdDualSimEnabled(boolean action) {
        Rlog.d(TAG, "setGwsdDualSimEnabled: " + action);
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                iGwsdService.setGwsdDualSimEnabled(action);
            }
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
        }
    }

    public boolean isDataAvailableForGwsdDualSim(boolean gwsdDualSimStatus) {
        Rlog.d(TAG, "isDataAvailableForGwsdDualSim");
        try {
            IGwsdService iGwsdService = IGwsdService.Stub.asInterface(ServiceManager.getService("gwsd"));
            if (iGwsdService != null) {
                return iGwsdService.isDataAvailableForGwsdDualSim(gwsdDualSimStatus);
            }
            return false;
        } catch (Exception e) {
            Rlog.e(TAG, Log.getStackTraceString(e));
            return false;
        }
    }
}
