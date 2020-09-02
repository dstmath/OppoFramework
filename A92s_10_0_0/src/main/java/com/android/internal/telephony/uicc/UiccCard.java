package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class UiccCard {
    protected static final boolean DBG = true;
    public static final String EXTRA_ICC_CARD_ADDED = "com.android.internal.telephony.uicc.ICC_CARD_ADDED";
    protected static final String LOG_TAG = "UiccCard";
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"gsm.ril.fulluicctype", "gsm.ril.fulluicctype.2", "gsm.ril.fulluicctype.3", "gsm.ril.fulluicctype.4"};
    /* access modifiers changed from: protected */
    public String mCardId;
    @UnsupportedAppUsage
    private IccCardStatus.CardState mCardState;
    @UnsupportedAppUsage
    private CommandsInterface mCi;
    @UnsupportedAppUsage
    private Context mContext;
    private String mIccid;
    @UnsupportedAppUsage
    protected final Object mLock;
    @UnsupportedAppUsage
    private final int mPhoneId;
    private UiccProfile mUiccProfile;

    public UiccCard(Context c, CommandsInterface ci, IccCardStatus ics, int phoneId, Object lock) {
        log("Creating");
        this.mCardState = ics.mCardState;
        this.mPhoneId = phoneId;
        this.mLock = lock;
        update(c, ci, ics);
    }

    public void dispose() {
        synchronized (this.mLock) {
            log("Disposing card");
            if (this.mUiccProfile != null) {
                this.mUiccProfile.dispose();
            }
            this.mUiccProfile = null;
        }
    }

    public void update(Context c, CommandsInterface ci, IccCardStatus ics) {
        synchronized (this.mLock) {
            this.mCardState = ics.mCardState;
            this.mContext = c;
            this.mCi = ci;
            this.mIccid = ics.iccid;
            updateCardId();
            if (this.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT) {
                throw new RuntimeException("Card state is absent when updating!");
            } else if (this.mUiccProfile == null) {
                this.mUiccProfile = TelephonyComponentFactory.getInstance().inject(UiccProfile.class.getName()).makeUiccProfile(this.mContext, this.mCi, ics, this.mPhoneId, this, this.mLock);
            } else {
                this.mUiccProfile.update(this.mContext, this.mCi, ics);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        log("UiccCard finalized");
    }

    /* access modifiers changed from: protected */
    public void updateCardId() {
        this.mCardId = this.mIccid;
    }

    @Deprecated
    public void registerForCarrierPrivilegeRulesLoaded(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            if (this.mUiccProfile != null) {
                this.mUiccProfile.registerForCarrierPrivilegeRulesLoaded(h, what, obj);
            } else {
                loge("registerForCarrierPrivilegeRulesLoaded Failed!");
            }
        }
    }

    @Deprecated
    public void unregisterForCarrierPrivilegeRulesLoaded(Handler h) {
        synchronized (this.mLock) {
            if (this.mUiccProfile != null) {
                this.mUiccProfile.unregisterForCarrierPrivilegeRulesLoaded(h);
            } else {
                loge("unregisterForCarrierPrivilegeRulesLoaded Failed!");
            }
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public boolean isApplicationOnIcc(IccCardApplicationStatus.AppType type) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return false;
            }
            boolean isApplicationOnIcc = this.mUiccProfile.isApplicationOnIcc(type);
            return isApplicationOnIcc;
        }
    }

    @UnsupportedAppUsage
    public IccCardStatus.CardState getCardState() {
        IccCardStatus.CardState cardState;
        synchronized (this.mLock) {
            cardState = this.mCardState;
        }
        return cardState;
    }

    @Deprecated
    public IccCardStatus.PinState getUniversalPinState() {
        synchronized (this.mLock) {
            if (this.mUiccProfile != null) {
                IccCardStatus.PinState universalPinState = this.mUiccProfile.getUniversalPinState();
                return universalPinState;
            }
            IccCardStatus.PinState pinState = IccCardStatus.PinState.PINSTATE_UNKNOWN;
            return pinState;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public UiccCardApplication getApplication(int family) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return null;
            }
            UiccCardApplication application = this.mUiccProfile.getApplication(family);
            return application;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public UiccCardApplication getApplicationIndex(int index) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return null;
            }
            UiccCardApplication applicationIndex = this.mUiccProfile.getApplicationIndex(index);
            return applicationIndex;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public UiccCardApplication getApplicationByType(int type) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return null;
            }
            UiccCardApplication applicationByType = this.mUiccProfile.getApplicationByType(type);
            return applicationByType;
        }
    }

    @Deprecated
    public boolean resetAppWithAid(String aid, boolean reset) {
        synchronized (this.mLock) {
            if (this.mUiccProfile == null) {
                return false;
            }
            boolean resetAppWithAid = this.mUiccProfile.resetAppWithAid(aid, reset);
            return resetAppWithAid;
        }
    }

    @Deprecated
    public void iccOpenLogicalChannel(String AID, int p2, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccOpenLogicalChannel(AID, p2, response);
        } else {
            loge("iccOpenLogicalChannel Failed!");
        }
    }

    @Deprecated
    public void iccCloseLogicalChannel(int channel, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccCloseLogicalChannel(channel, response);
        } else {
            loge("iccCloseLogicalChannel Failed!");
        }
    }

    @Deprecated
    public void iccTransmitApduLogicalChannel(int channel, int cla, int command, int p1, int p2, int p3, String data, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccTransmitApduLogicalChannel(channel, cla, command, p1, p2, p3, data, response);
        } else {
            loge("iccTransmitApduLogicalChannel Failed!");
        }
    }

    @Deprecated
    public void iccTransmitApduBasicChannel(int cla, int command, int p1, int p2, int p3, String data, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccTransmitApduBasicChannel(cla, command, p1, p2, p3, data, response);
        } else {
            loge("iccTransmitApduBasicChannel Failed!");
        }
    }

    @Deprecated
    public void iccExchangeSimIO(int fileID, int command, int p1, int p2, int p3, String pathID, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.iccExchangeSimIO(fileID, command, p1, p2, p3, pathID, response);
        } else {
            loge("iccExchangeSimIO Failed!");
        }
    }

    @Deprecated
    public void sendEnvelopeWithStatus(String contents, Message response) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.sendEnvelopeWithStatus(contents, response);
        } else {
            loge("sendEnvelopeWithStatus Failed!");
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public int getNumApplications() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getNumApplications();
        }
        return 0;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public UiccProfile getUiccProfile() {
        return this.mUiccProfile;
    }

    @Deprecated
    public boolean areCarrierPriviligeRulesLoaded() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.areCarrierPriviligeRulesLoaded();
        }
        return false;
    }

    @Deprecated
    public boolean hasCarrierPrivilegeRules() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.hasCarrierPrivilegeRules();
        }
        return false;
    }

    @Deprecated
    public int getCarrierPrivilegeStatus(Signature signature, String packageName) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPrivilegeStatus(signature, packageName);
        }
        return -1;
    }

    @Deprecated
    public int getCarrierPrivilegeStatus(PackageManager packageManager, String packageName) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPrivilegeStatus(packageManager, packageName);
        }
        return -1;
    }

    @Deprecated
    public int getCarrierPrivilegeStatus(PackageInfo packageInfo) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPrivilegeStatus(packageInfo);
        }
        return -1;
    }

    @Deprecated
    public int getCarrierPrivilegeStatusForCurrentTransaction(PackageManager packageManager) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPrivilegeStatusForCurrentTransaction(packageManager);
        }
        return -1;
    }

    @UnsupportedAppUsage
    @Deprecated
    public List<String> getCarrierPackageNamesForIntent(PackageManager packageManager, Intent intent) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getCarrierPackageNamesForIntent(packageManager, intent);
        }
        return null;
    }

    @Deprecated
    public boolean setOperatorBrandOverride(String brand) {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.setOperatorBrandOverride(brand);
        }
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public String getOperatorBrandOverride() {
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getOperatorBrandOverride();
        }
        return null;
    }

    public String[] getFullIccCardType() {
        return SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[this.mPhoneId]).split(",");
    }

    @UnsupportedAppUsage
    public String getIccId() {
        String str = this.mIccid;
        if (str != null) {
            return str;
        }
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getIccId();
        }
        return null;
    }

    public String getCardId() {
        if (!TextUtils.isEmpty(this.mCardId)) {
            return this.mCardId;
        }
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            return uiccProfile.getIccId();
        }
        return null;
    }

    @UnsupportedAppUsage
    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    @UnsupportedAppUsage
    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UiccCard:");
        pw.println(" mCi=" + this.mCi);
        pw.println(" mCardState=" + this.mCardState);
        pw.println(" mCardId=" + this.mCardId);
        pw.println(" mPhoneId=" + this.mPhoneId);
        pw.println();
        UiccProfile uiccProfile = this.mUiccProfile;
        if (uiccProfile != null) {
            uiccProfile.dump(fd, pw, args);
        }
    }
}
