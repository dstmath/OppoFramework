package com.qualcomm.qti.internal.telephony.dataconnection;

import android.database.Cursor;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Telephony.Carriers;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public final class QtiDcTracker extends DcTracker {
    private final String CONFIG_FETCH_APN_FROM_OMH_CARD = "config_fetch_apn_from_omh_card";
    private final int EVENT_MODEM_DATA_PROFILE_READY = 1001;
    private String LOG_TAG = "QtiDCT";
    private final String OMH_FEATURE_ENABLE_OVERRIDE = "persist.radio.omh.enable";
    private final int QTI_DCT_EVENTS_BASE = 1000;
    private HashSet<String> mIccidSet = new HashSet();
    private QtiCdmaApnProfileTracker mOmhApt;

    public QtiDcTracker(Phone phone) {
        super(phone);
        if (phone.getPhoneType() == 1) {
            this.LOG_TAG = "QtiGsmDCT";
        } else if (phone.getPhoneType() == 2) {
            this.LOG_TAG = "QtiCdmaDCT";
        } else {
            this.LOG_TAG = "DCT";
            loge("unexpected phone type [" + phone.getPhoneType() + "]");
        }
        log(this.LOG_TAG + ".constructor");
        if (phone.getPhoneType() == 2) {
            boolean fetchApnFromOmhCard = getConfigItem("config_fetch_apn_from_omh_card");
            log(this.LOG_TAG + " fetchApnFromOmhCard: " + fetchApnFromOmhCard);
            boolean featureOverride = SystemProperties.getBoolean("persist.radio.omh.enable", false);
            if (featureOverride) {
                log(this.LOG_TAG + "OMH: feature-config override enabled");
                fetchApnFromOmhCard = featureOverride;
            }
            if (fetchApnFromOmhCard) {
                this.mOmhApt = new QtiCdmaApnProfileTracker(phone);
                this.mOmhApt.registerForModemProfileReady(this, 1001, null);
            }
        }
        fillIccIdSet();
    }

    public void dispose() {
        super.dispose();
        if (this.mOmhApt != null) {
            this.mOmhApt.unregisterForModemProfileReady(this);
        }
    }

    protected void cleanUpConnection(boolean tearDown, ApnContext apnContext) {
        super.cleanUpConnection(tearDown, apnContext);
        if (this.mOmhApt != null) {
            this.mOmhApt.clearActiveApnProfile();
        }
    }

    private boolean getConfigItem(String key) {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (carrierConfigManager == null) {
            loge("getConfigItem: No carrier config service found.");
            return false;
        }
        PersistableBundle carrierConfig = carrierConfigManager.getConfigForSubId(this.mPhone.getSubId());
        if (carrierConfig != null) {
            return carrierConfig.getBoolean(key);
        }
        loge("getConfigItem: Empty carrier config.");
        return false;
    }

    private void onModemApnProfileReady() {
        if (this.mState == State.FAILED) {
            cleanUpAllConnections(false, "psRestrictEnabled");
        }
        log("OMH: onModemApnProfileReady(): Setting up data call");
        State overallState = getOverallState();
        boolean isDisconnected = overallState != State.IDLE ? overallState == State.FAILED : true;
        log("onModemApnProfileReady: createAllApnList and cleanUpAllConnections");
        createAllApnList();
        setInitialAttachApn();
        cleanUpConnectionsOnUpdatedApns(isDisconnected ^ 1, "apnChanged");
        if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            setupDataOnConnectableApns("apnChanged");
        }
    }

    private boolean isRecordsLoaded() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.getRecordsLoaded();
        }
        return false;
    }

    protected void onRecordsLoadedOrSubIdChanged() {
        log("onRecordsLoaded: createAllApnList");
        this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17956893);
        if (this.mOmhApt != null) {
            log("OMH: onRecordsLoaded(): calling loadProfiles()");
            this.mOmhApt.loadProfiles();
            if (this.mPhone.mCi.getRadioState().isOn()) {
                log("OMH: onRecordsLoaded: notifying data availability");
                notifyOffApnsOfAvailability("simLoaded");
                return;
            }
            return;
        }
        createAllApnList();
        if (isRecordsLoaded()) {
            setInitialAttachApn();
        }
        if (this.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoaded: notifying data availability");
            notifyOffApnsOfAvailability("simLoaded");
        }
        setupDataOnConnectableApns("simLoaded");
    }

    protected void createAllApnList() {
        this.mMvnoMatched = false;
        this.mAllApnSettings = new ArrayList();
        String operator = getOperatorNumeric();
        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        if (!(this.mOmhApt == null || !ServiceState.isCdma(radioTech) || 13 == radioTech)) {
            ArrayList<QtiApnSetting> mOmhApnsList = new ArrayList();
            mOmhApnsList = this.mOmhApt.getOmhApnProfilesList();
            if (!mOmhApnsList.isEmpty()) {
                log("createAllApnList: Copy Omh profiles");
                this.mAllApnSettings.addAll(mOmhApnsList);
            }
        }
        if (!(!this.mAllApnSettings.isEmpty() || operator == null || (operator.isEmpty() ^ 1) == 0)) {
            String selection = "numeric = '" + operator + "'";
            log("createAllApnList: selection=" + selection);
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, "_id");
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    this.mAllApnSettings = createApnList(cursor);
                }
                cursor.close();
            }
        }
        addEmergencyApnSetting();
        dedupeApnSettings();
        if (this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: No APN found for carrier: " + operator);
            this.mPreferredApn = null;
        } else {
            this.mPreferredApn = getPreferredApn();
            if (!(this.mPreferredApn == null || (this.mPreferredApn.numeric.equals(operator) ^ 1) == 0)) {
                this.mPreferredApn = null;
                setPreferredApn(-1);
            }
            log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
        }
        log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
        setDataProfilesAsNeeded();
    }

    protected boolean getAttachedStatus() {
        int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        if (!SubscriptionManager.isUsableSubIdValue(dataSub) || dataSub == this.mPhone.getSubId()) {
            return super.getAttachedStatus();
        }
        return true;
    }

    protected boolean allowInitialAttachForOperator() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String iccId = r != null ? r.getIccId() : "";
        if (iccId != null) {
            Iterator<String> itr = this.mIccidSet.iterator();
            while (itr.hasNext()) {
                if (iccId.contains((CharSequence) itr.next())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void fillIccIdSet() {
        this.mIccidSet.add("8991840");
        this.mIccidSet.add("8991854");
        this.mIccidSet.add("8991855");
        this.mIccidSet.add("8991856");
        this.mIccidSet.add("8991857");
        this.mIccidSet.add("8991858");
        this.mIccidSet.add("8991859");
        this.mIccidSet.add("899186");
        this.mIccidSet.add("8991870");
        this.mIccidSet.add("8991871");
        this.mIccidSet.add("8991872");
        this.mIccidSet.add("8991873");
        this.mIccidSet.add("8991874");
    }

    public void handleMessage(Message msg) {
        log("QtiDcTracker handleMessage msg=" + msg);
        switch (msg.what) {
            case 1001:
                onModemApnProfileReady();
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    protected void log(String s) {
        Rlog.d(this.LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }
}
