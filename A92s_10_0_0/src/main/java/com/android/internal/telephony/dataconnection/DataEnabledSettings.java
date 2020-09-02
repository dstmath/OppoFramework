package com.android.internal.telephony.dataconnection;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import android.util.Pair;
import com.android.internal.telephony.GlobalSettingsHelper;
import com.android.internal.telephony.MultiSimSettingController;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DataEnabledSettings {
    private static final String LOG_TAG = "DataEnabledSettings";
    public static final int REASON_DATA_ENABLED_BY_CARRIER = 4;
    public static final int REASON_INTERNAL_DATA_ENABLED = 1;
    public static final int REASON_OVERRIDE_CONDITION_CHANGED = 8;
    public static final int REASON_OVERRIDE_RULE_CHANGED = 7;
    public static final int REASON_POLICY_DATA_ENABLED = 3;
    public static final int REASON_PROVISIONED_CHANGED = 5;
    public static final int REASON_PROVISIONING_DATA_ENABLED_CHANGED = 6;
    public static final int REASON_REGISTERED = 0;
    public static final int REASON_USER_DATA_ENABLED = 2;
    private boolean mCarrierDataEnabled = true;
    /* access modifiers changed from: private */
    public DataEnabledOverride mDataEnabledOverride;
    private boolean mInternalDataEnabled = true;
    private boolean mIsDataEnabled = false;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.dataconnection.DataEnabledSettings.AnonymousClass1 */

        public void onSubscriptionsChanged() {
            synchronized (this) {
                if (DataEnabledSettings.this.mSubId != DataEnabledSettings.this.mPhone.getSubId()) {
                    DataEnabledSettings dataEnabledSettings = DataEnabledSettings.this;
                    dataEnabledSettings.log("onSubscriptionsChanged subId: " + DataEnabledSettings.this.mSubId + " to: " + DataEnabledSettings.this.mPhone.getSubId());
                    int unused = DataEnabledSettings.this.mSubId = DataEnabledSettings.this.mPhone.getSubId();
                    DataEnabledOverride unused2 = DataEnabledSettings.this.mDataEnabledOverride = DataEnabledSettings.this.getDataEnabledOverride();
                    DataEnabledSettings.this.updatePhoneStateListener();
                    DataEnabledSettings.this.updateDataEnabledAndNotify(2);
                    DataEnabledSettings.this.mPhone.notifyUserMobileDataStateChanged(DataEnabledSettings.this.isUserDataEnabled());
                }
            }
        }
    };
    private final RegistrantList mOverallDataEnabledChangedRegistrants = new RegistrantList();
    private final RegistrantList mOverallDataEnabledOverrideChangedRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public final Phone mPhone;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.internal.telephony.dataconnection.DataEnabledSettings.AnonymousClass2 */

        public void onCallStateChanged(int state, String phoneNumber) {
            DataEnabledSettings.this.updateDataEnabledAndNotify(8);
        }
    };
    private boolean mPolicyDataEnabled = true;
    private ContentResolver mResolver = null;
    private final LocalLog mSettingChangeLocalLog = new LocalLog(50);
    /* access modifiers changed from: private */
    public int mSubId = -1;
    private TelephonyManager mTelephonyManager;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DataEnabledChangedReason {
    }

    /* access modifiers changed from: private */
    public void updatePhoneStateListener() {
        this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        if (SubscriptionManager.isUsableSubscriptionId(this.mSubId)) {
            this.mTelephonyManager = this.mTelephonyManager.createForSubscriptionId(this.mSubId);
        }
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
    }

    public String toString() {
        return "[mInternalDataEnabled=" + this.mInternalDataEnabled + ", isUserDataEnabled=" + isUserDataEnabled() + ", isProvisioningDataEnabled=" + isProvisioningDataEnabled() + ", mPolicyDataEnabled=" + this.mPolicyDataEnabled + ", mCarrierDataEnabled=" + this.mCarrierDataEnabled + ", mIsDataEnabled=" + this.mIsDataEnabled + ", " + this.mDataEnabledOverride + "]";
    }

    public DataEnabledSettings(Phone phone) {
        this.mPhone = phone;
        this.mResolver = this.mPhone.getContext().getContentResolver();
        ((SubscriptionManager) this.mPhone.getContext().getSystemService("telephony_subscription_service")).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        this.mTelephonyManager = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        this.mDataEnabledOverride = getDataEnabledOverride();
        updateDataEnabled();
    }

    /* access modifiers changed from: private */
    public DataEnabledOverride getDataEnabledOverride() {
        return new DataEnabledOverride(SubscriptionController.getInstance().getDataEnabledOverrideRules(this.mPhone.getSubId()));
    }

    public synchronized void setInternalDataEnabled(boolean enabled) {
        localLog("InternalDataEnabled", enabled);
        if (this.mInternalDataEnabled != enabled) {
            this.mInternalDataEnabled = enabled;
            updateDataEnabledAndNotify(1);
        }
    }

    public synchronized boolean isInternalDataEnabled() {
        return this.mInternalDataEnabled;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0072, code lost:
        return;
     */
    public synchronized void setUserDataEnabled(boolean enabled) {
        if (isStandAloneOpportunistic(this.mPhone.getSubId(), this.mPhone.getContext()) && !enabled) {
            return;
        }
        if (this.mPhone == null || !OemConstant.isPoliceVersion(this.mPhone) || OemConstant.canSwitchByUser(this.mPhone) || enabled == OemConstant.isDataAllow(this.mPhone)) {
            updateDataEnabled();
            localLog("UserDataEnabled", enabled);
            if (GlobalSettingsHelper.setInt(this.mPhone.getContext(), "mobile_data", this.mPhone.getSubId(), enabled ? 1 : 0)) {
                this.mPhone.notifyUserMobileDataStateChanged(enabled);
                updateDataEnabledAndNotify(2);
                MultiSimSettingController.getInstance().notifyUserDataEnabled(this.mPhone.getSubId(), enabled);
            }
        } else {
            log("---data-enable-return---");
        }
    }

    public synchronized boolean isUserDataEnabled() {
        if (isStandAloneOpportunistic(this.mPhone.getSubId(), this.mPhone.getContext())) {
            return true;
        }
        return GlobalSettingsHelper.getBoolean(this.mPhone.getContext(), "mobile_data", this.mPhone.getSubId(), "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.mobiledata", "true")));
    }

    public synchronized boolean setAlwaysAllowMmsData(boolean alwaysAllow) {
        boolean changed;
        localLog("setAlwaysAllowMmsData", alwaysAllow);
        this.mDataEnabledOverride.setAlwaysAllowMms(alwaysAllow);
        changed = SubscriptionController.getInstance().setDataEnabledOverrideRules(this.mPhone.getSubId(), this.mDataEnabledOverride.getRules());
        if (changed) {
            updateDataEnabledAndNotify(7);
            notifyDataEnabledOverrideChanged();
        }
        return changed;
    }

    public synchronized boolean setAllowDataDuringVoiceCall(boolean allow) {
        boolean changed;
        localLog("setAllowDataDuringVoiceCall", allow);
        this.mDataEnabledOverride.setDataAllowedInVoiceCall(allow);
        changed = SubscriptionController.getInstance().setDataEnabledOverrideRules(this.mPhone.getSubId(), this.mDataEnabledOverride.getRules());
        if (changed) {
            updateDataEnabledAndNotify(7);
            notifyDataEnabledOverrideChanged();
        }
        return changed;
    }

    public synchronized boolean isDataAllowedInVoiceCall() {
        return this.mDataEnabledOverride.isDataAllowedInVoiceCall();
    }

    public synchronized void setPolicyDataEnabled(boolean enabled) {
        localLog("PolicyDataEnabled", enabled);
        if (this.mPolicyDataEnabled != enabled) {
            this.mPolicyDataEnabled = enabled;
            updateDataEnabledAndNotify(3);
        }
    }

    public synchronized boolean isPolicyDataEnabled() {
        return this.mPolicyDataEnabled;
    }

    public synchronized void setCarrierDataEnabled(boolean enabled) {
        localLog("CarrierDataEnabled", enabled);
        if (this.mCarrierDataEnabled != enabled) {
            this.mCarrierDataEnabled = enabled;
            updateDataEnabledAndNotify(4);
        }
    }

    public synchronized boolean isCarrierDataEnabled() {
        return this.mCarrierDataEnabled;
    }

    public synchronized void updateProvisionedChanged() {
        updateDataEnabledAndNotify(5);
    }

    public synchronized void updateProvisioningDataEnabled() {
        updateDataEnabledAndNotify(6);
    }

    public synchronized boolean isDataEnabled() {
        updateDataEnabled();
        return this.mIsDataEnabled;
    }

    /* access modifiers changed from: private */
    public synchronized void updateDataEnabledAndNotify(int reason) {
        boolean prevDataEnabled = this.mIsDataEnabled;
        updateDataEnabled();
        if (prevDataEnabled != this.mIsDataEnabled) {
            notifyDataEnabledChanged(!prevDataEnabled, reason);
        }
    }

    private synchronized void updateDataEnabled() {
        this.mIsDataEnabled = this.mInternalDataEnabled && (isUserDataEnabled() || this.mDataEnabledOverride.shouldOverrideDataEnabledSettings(this.mPhone, 255)) && this.mPolicyDataEnabled && this.mCarrierDataEnabled;
    }

    public boolean isProvisioning() {
        return Settings.Global.getInt(this.mResolver, "device_provisioned", 0) == 0;
    }

    public boolean isProvisioningDataEnabled() {
        String prov_property = SystemProperties.get("ro.com.android.prov_mobiledata", "false");
        int prov_mobile_data = Settings.Global.getInt(this.mResolver, "device_provisioning_mobile_data", "true".equalsIgnoreCase(prov_property) ? 1 : 0);
        boolean retVal = prov_mobile_data != 0;
        log("getDataEnabled during provisioning retVal=" + retVal + " - (" + prov_property + ", " + prov_mobile_data + ")");
        return retVal;
    }

    public synchronized void setDataRoamingEnabled(boolean enabled) {
        localLog("setDataRoamingEnabled", enabled);
        if (GlobalSettingsHelper.setBoolean(this.mPhone.getContext(), "data_roaming", this.mPhone.getSubId(), enabled)) {
            MultiSimSettingController.getInstance().notifyRoamingDataEnabled(this.mPhone.getSubId(), enabled);
        }
    }

    public synchronized boolean getDataRoamingEnabled() {
        return GlobalSettingsHelper.getBoolean(this.mPhone.getContext(), "data_roaming", this.mPhone.getSubId(), getDefaultDataRoamingEnabled());
    }

    public synchronized boolean getDefaultDataRoamingEnabled() {
        return "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false")) | ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId()).getBoolean("carrier_default_data_roaming_enabled_bool");
    }

    private void notifyDataEnabledChanged(boolean enabled, int reason) {
        this.mOverallDataEnabledChangedRegistrants.notifyResult(new Pair(Boolean.valueOf(enabled), Integer.valueOf(reason)));
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mOverallDataEnabledChangedRegistrants.addUnique(h, what, obj);
        notifyDataEnabledChanged(isDataEnabled(), 0);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mOverallDataEnabledChangedRegistrants.remove(h);
    }

    private void notifyDataEnabledOverrideChanged() {
        this.mOverallDataEnabledOverrideChangedRegistrants.notifyRegistrants();
    }

    public void registerForDataEnabledOverrideChanged(Handler h, int what) {
        this.mOverallDataEnabledOverrideChangedRegistrants.addUnique(h, what, (Object) null);
        notifyDataEnabledOverrideChanged();
    }

    public void unregisterForDataEnabledOverrideChanged(Handler h) {
        this.mOverallDataEnabledOverrideChangedRegistrants.remove(h);
    }

    private static boolean isStandAloneOpportunistic(int subId, Context context) {
        SubscriptionInfo info = SubscriptionController.getInstance().getActiveSubscriptionInfo(subId, context.getOpPackageName());
        return info != null && info.isOpportunistic() && info.getGroupUuid() == null;
    }

    public synchronized boolean isDataEnabled(int apnType) {
        return this.mInternalDataEnabled && this.mPolicyDataEnabled && this.mCarrierDataEnabled && (isUserDataEnabled() || this.mDataEnabledOverride.shouldOverrideDataEnabledSettings(this.mPhone, apnType));
    }

    /* access modifiers changed from: private */
    public void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void localLog(String name, boolean value) {
        LocalLog localLog = this.mSettingChangeLocalLog;
        localLog.log(name + " change to " + value);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(" DataEnabledSettings=");
        this.mSettingChangeLocalLog.dump(fd, pw, args);
    }
}
