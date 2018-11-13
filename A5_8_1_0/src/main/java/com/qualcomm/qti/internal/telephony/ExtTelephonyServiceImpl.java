package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Global;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.util.Log;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.qualcomm.qti.internal.telephony.primarycard.QtiPrimaryCardController;
import com.qualcomm.qti.internal.telephony.primarycard.QtiPrimaryCardUtils;
import java.util.List;
import org.codeaurora.internal.IDepersoResCallback;
import org.codeaurora.internal.IDsda;
import org.codeaurora.internal.IExtTelephony.Stub;

public class ExtTelephonyServiceImpl extends Stub {
    private static final String CONFIG_CURRENT_PRIMARY_SUB = "config_current_primary_sub";
    private static final boolean DBG = true;
    private static final String LOG_TAG = "ExtTelephonyServiceImpl";
    private static final String TELEPHONY_SERVICE_NAME = "extphone";
    private static Context mContext;
    private static ExtTelephonyServiceImpl sInstance = null;
    private IDsda mDsda = null;
    private QtiSmscHelper mQtiSmscHelper;

    public static ExtTelephonyServiceImpl init(Context context) {
        ExtTelephonyServiceImpl extTelephonyServiceImpl;
        synchronized (ExtTelephonyServiceImpl.class) {
            mContext = context;
            if (sInstance == null) {
                sInstance = new ExtTelephonyServiceImpl();
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            extTelephonyServiceImpl = sInstance;
        }
        return extTelephonyServiceImpl;
    }

    public static ExtTelephonyServiceImpl getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    private ExtTelephonyServiceImpl() {
        logd("init constructor ");
        if (ServiceManager.getService(TELEPHONY_SERVICE_NAME) == null) {
            ServiceManager.addService(TELEPHONY_SERVICE_NAME, this);
        }
        this.mQtiSmscHelper = new QtiSmscHelper();
    }

    public int getCurrentUiccCardProvisioningStatus(int slotId) {
        return QtiUiccCardProvisioner.getInstance().getCurrentUiccCardProvisioningStatus(slotId);
    }

    public int getUiccCardProvisioningUserPreference(int slotId) {
        return QtiUiccCardProvisioner.getInstance().getUiccCardProvisioningUserPreference(slotId);
    }

    public int activateUiccCard(int slotId) {
        return QtiUiccCardProvisioner.getInstance().activateUiccCard(slotId);
    }

    public int deactivateUiccCard(int slotId) {
        return QtiUiccCardProvisioner.getInstance().deactivateUiccCard(slotId);
    }

    public boolean isSMSPromptEnabled() {
        if (QtiSubscriptionController.getInstance() == null) {
            Log.wtf(LOG_TAG, "QtiSubscriptionController getInstance is null");
        }
        return QtiSubscriptionController.getInstance().isSMSPromptEnabled();
    }

    public void setSMSPromptEnabled(boolean enabled) {
        if (QtiSubscriptionController.getInstance() == null) {
            Log.wtf(LOG_TAG, "QtiSubscriptionController getInstance is null");
        }
        QtiSubscriptionController.getInstance().setSMSPromptEnabled(enabled);
    }

    public int getPhoneIdForECall() {
        return QtiEmergencyCallHelper.getPhoneIdForECall();
    }

    public int getPrimaryStackPhoneId() {
        return QtiEmergencyCallHelper.getPrimaryStackPhoneId();
    }

    public void setDsdaAdapter(IDsda a) {
        logd("setDsdaAdapter:" + a);
        this.mDsda = a;
    }

    public void switchToActiveSub(int sub) {
        logd("switchToActiveSub:" + sub + " mDsda:" + this.mDsda);
        try {
            this.mDsda.switchToActiveSub(sub);
        } catch (RemoteException ex) {
            logd("switchToActiveSub:" + ex);
        }
    }

    public int getActiveSubscription() {
        logd("getActiveSubscription mDsda:" + this.mDsda);
        try {
            return this.mDsda.getActiveSubscription();
        } catch (RemoteException ex) {
            logd("getActiveSubscription:" + ex);
            return -1;
        }
    }

    public boolean isFdnEnabled() {
        IccCard card = PhoneFactory.getDefaultPhone().getIccCard();
        if (card != null) {
            return card.getIccFdnEnabled();
        }
        return false;
    }

    public int getUiccApplicationCount(int slotId) {
        UiccCard card = UiccController.getInstance().getUiccCard(slotId);
        if (card != null) {
            return card.getNumApplications();
        }
        return 0;
    }

    public void supplyIccDepersonalization(String netpin, String type, IDepersoResCallback callback, int phoneId) {
        logd("supplyIccDepersonalization");
        QtiDepersoSupplier.getInstance().supplyIccDepersonalization(netpin, type, callback, phoneId);
    }

    public int getUiccApplicationType(int slotId, int appIndex) {
        UiccCard card = UiccController.getInstance().getUiccCard(slotId);
        if (card != null) {
            return card.getApplicationIndex(appIndex).getType().ordinal();
        }
        return 0;
    }

    public int getUiccApplicationState(int slotId, int appIndex) {
        UiccCard card = UiccController.getInstance().getUiccCard(slotId);
        if (card != null) {
            return card.getApplicationIndex(appIndex).getState().ordinal();
        }
        return 0;
    }

    public void setPrimaryCardOnSlot(int slotId) {
        QtiPrimaryCardController.getInstance().setPrimaryCardOnSlot(slotId);
    }

    public int getCurrentPrimaryCardSlotId() {
        return QtiPrimaryCardUtils.getCurrentPrimarySlotFromDB(mContext);
    }

    public boolean isEmergencyNumber(String number) {
        return QtiEmergencyCallHelper.isEmergencyNumber(number);
    }

    public boolean isLocalEmergencyNumber(String number) {
        return QtiEmergencyCallHelper.isLocalEmergencyNumber(mContext, number);
    }

    public boolean isPotentialEmergencyNumber(String number) {
        return QtiEmergencyCallHelper.isPotentialEmergencyNumber(number);
    }

    public boolean isPotentialLocalEmergencyNumber(String number) {
        return QtiEmergencyCallHelper.isPotentialLocalEmergencyNumber(mContext, number);
    }

    public boolean isDeviceInSingleStandby() {
        return QtiEmergencyCallHelper.isDeviceInSingleStandby();
    }

    public boolean setLocalCallHold(int subscriptionId, boolean enable) {
        int phoneId = SubscriptionManager.getPhoneId(subscriptionId);
        Phone phone = PhoneFactory.getPhone(phoneId);
        logd("setLocalCallHold:" + phoneId + " enable:" + enable);
        return ((QtiGsmCdmaPhone) phone).setLocalCallHold(enable);
    }

    public boolean isDsdaEnabled() {
        return TelephonyManager.getDefault().getMultiSimConfiguration() == MultiSimVariants.DSDA ? DBG : false;
    }

    public int getPrimaryCarrierSlotId() {
        int slotId = -1;
        List<SubscriptionInfo> subInfoList = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
        int matchingCount = 0;
        if (subInfoList == null || subInfoList.size() < 1) {
            loge("No active subscriptions found!!");
            return -1;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            String mccMnc = String.valueOf(subInfo.getMcc()) + String.valueOf(subInfo.getMnc());
            int provisionStatus = getCurrentUiccCardProvisioningStatus(subInfo.getSimSlotIndex());
            logd("provisionStatus : " + provisionStatus + " slotId " + subInfo.getSimSlotIndex());
            if (provisionStatus == 1 && isPrimaryCarrierMccMnc(mccMnc)) {
                logd("Found a matching combination, slotId  " + subInfo.getSimSlotIndex());
                slotId = subInfo.getSimSlotIndex();
                matchingCount++;
            }
        }
        if (matchingCount > 1) {
            logd("Found multiple matches, returning primary slotid");
            slotId = Global.getInt(mContext.getContentResolver(), CONFIG_CURRENT_PRIMARY_SUB, slotId);
        }
        return slotId;
    }

    private boolean isPrimaryCarrierMccMnc(String mccMnc) {
        for (String mccmnc : new String[]{"405840", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874", "22201", "2221"}) {
            if (mccMnc.equals(mccmnc)) {
                logd("Found a matching combination  " + mccMnc);
                return DBG;
            }
        }
        logd("Not found a matching combination  " + mccMnc);
        return false;
    }

    public boolean isPrimaryCarrierSlotId(int slotId) {
        SubscriptionInfo subInfo = SubscriptionManager.from(mContext).getActiveSubscriptionInfoForSimSlotIndex(slotId);
        if (subInfo == null) {
            loge("No active subscription found on slot " + slotId);
            return false;
        } else if (!isPrimaryCarrierMccMnc(String.valueOf(subInfo.getMcc()) + String.valueOf(subInfo.getMnc()))) {
            return false;
        } else {
            logd("Found a matching combination, slotId  " + subInfo.getSimSlotIndex());
            return DBG;
        }
    }

    public boolean setSmscAddress(int slotId, String smsc) {
        return this.mQtiSmscHelper.setSmscAddress(slotId, smsc);
    }

    public String getSmscAddress(int slotId) {
        return this.mQtiSmscHelper.getSmscAddress(slotId);
    }

    public boolean isVendorApkAvailable(String packageName) {
        try {
            mContext.getPackageManager().getPackageInfo(packageName, 0);
            return DBG;
        } catch (NameNotFoundException e) {
            logd("Vendor apk not available for " + packageName);
            return false;
        }
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }
}
