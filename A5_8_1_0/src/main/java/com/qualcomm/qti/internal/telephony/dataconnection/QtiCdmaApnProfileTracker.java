package com.qualcomm.qti.internal.telephony.dataconnection;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.qualcomm.qti.internal.telephony.QtiRilInterface;
import java.util.ArrayList;
import java.util.HashMap;

public final class QtiCdmaApnProfileTracker extends Handler {
    private static final int EVENT_GET_DATA_CALL_PROFILE_DONE = 1;
    private static final int EVENT_LOAD_PROFILES = 2;
    private static final int EVENT_READ_MODEM_PROFILES = 0;
    private static final String[] mDefaultApnTypes = new String[]{"default", "mms", "supl", "hipri", "fota", "ims", "cbs"};
    private static final String[] mSupportedApnTypes = new String[]{"default", "mms", "supl", "dun", "hipri", "fota", "ims", "cbs"};
    protected final String LOG_TAG = "QtiCdmaApnProfileTracker";
    protected QtiApnSetting mActiveApn;
    private ArrayList<QtiApnSetting> mApnProfilesList = new ArrayList();
    private CdmaSubscriptionSourceManager mCdmaSsm;
    private RegistrantList mModemApnProfileRegistrants = new RegistrantList();
    private int mOmhReadProfileContext = 0;
    private int mOmhReadProfileCount = 0;
    HashMap<String, Integer> mOmhServicePriorityMap;
    private Phone mPhone;
    ArrayList<QtiApnSetting> mTempOmhApnProfilesList = new ArrayList();

    QtiCdmaApnProfileTracker(Phone phone) {
        this.mPhone = phone;
        this.mCdmaSsm = CdmaSubscriptionSourceManager.getInstance(phone.getContext(), phone.mCi, this, 2, null);
        this.mOmhServicePriorityMap = new HashMap();
    }

    void loadProfiles() {
        log("loadProfiles...");
        this.mApnProfilesList.clear();
        readApnProfilesFromModem();
    }

    private String[] parseTypes(String types) {
        if (types != null && !types.equals("")) {
            return types.split(",");
        }
        return new String[]{"*"};
    }

    protected void finalize() {
        Log.d("QtiCdmaApnProfileTracker", "QtiCdmaApnProfileTracker finalized");
    }

    public void registerForModemProfileReady(Handler h, int what, Object obj) {
        this.mModemApnProfileRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForModemProfileReady(Handler h) {
        this.mModemApnProfileRegistrants.remove(h);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                onReadApnProfilesFromModem();
                return;
            case 1:
                onGetDataCallProfileDone((AsyncResult) msg.obj, msg.arg1);
                return;
            case 2:
                loadProfiles();
                return;
            default:
                return;
        }
    }

    private void readApnProfilesFromModem() {
        sendMessage(obtainMessage(0));
    }

    private void onReadApnProfilesFromModem() {
        log("OMH: onReadApnProfilesFromModem()");
        this.mOmhReadProfileContext++;
        this.mOmhReadProfileCount = 0;
        this.mTempOmhApnProfilesList.clear();
        this.mOmhServicePriorityMap.clear();
        ApnProfileTypeModem[] values = ApnProfileTypeModem.values();
        int length = values.length;
        int i = 0;
        while (i < length) {
            ApnProfileTypeModem p = values[i];
            log("OMH: Reading profiles for:" + p.getid());
            this.mOmhReadProfileCount++;
            QtiRilInterface qtiRilInterface = QtiRilInterface.getInstance(this.mPhone.getContext());
            if (qtiRilInterface.isServiceReady()) {
                qtiRilInterface.getOmhCallProfile(p.getid(), obtainMessage(1, this.mOmhReadProfileContext, 0, p), this.mPhone.getPhoneId());
                i++;
            } else {
                log("Oem hook service is not ready yet " + this.mPhone.getPhoneId());
                return;
            }
        }
    }

    private void onGetDataCallProfileDone(AsyncResult ar, int context) {
        if (context == this.mOmhReadProfileContext) {
            if (ar.exception != null) {
                log("OMH: Exception in onGetDataCallProfileDone:" + ar.exception);
                this.mOmhReadProfileCount--;
                return;
            }
            ArrayList<QtiApnSetting> dataProfileListModem = ar.result;
            ApnProfileTypeModem modemProfile = ar.userObj;
            this.mOmhReadProfileCount--;
            if (dataProfileListModem != null && dataProfileListModem.size() > 0) {
                String serviceType = modemProfile.getDataServiceType();
                log("OMH: # profiles returned from modem:" + dataProfileListModem.size() + " for " + serviceType);
                this.mOmhServicePriorityMap.put(serviceType, Integer.valueOf(omhListGetArbitratedPriority(dataProfileListModem, serviceType)));
                for (QtiApnSetting apn : dataProfileListModem) {
                    ((QtiApnProfileOmh) apn).setApnProfileTypeModem(modemProfile);
                    QtiApnProfileOmh omhDuplicateDp = getDuplicateProfile(apn);
                    if (omhDuplicateDp == null) {
                        this.mTempOmhApnProfilesList.add(apn);
                        ((QtiApnProfileOmh) apn).addServiceType(ApnProfileTypeModem.getApnProfileTypeModem(serviceType));
                    } else {
                        log("OMH: Duplicate Profile " + omhDuplicateDp);
                        omhDuplicateDp.addServiceType(ApnProfileTypeModem.getApnProfileTypeModem(serviceType));
                    }
                }
            }
            if (this.mOmhReadProfileCount == 0) {
                log("OMH: Modem omh profile read complete.");
                addServiceTypeToUnSpecified();
                this.mApnProfilesList.addAll(this.mTempOmhApnProfilesList);
                if (this.mApnProfilesList.size() > 0) {
                    log("OMH: Found some OMH profiles.");
                    this.mModemApnProfileRegistrants.notifyRegistrants();
                }
            }
        }
    }

    private QtiApnProfileOmh getDuplicateProfile(QtiApnSetting apn) {
        for (QtiApnSetting dataProfile : this.mTempOmhApnProfilesList) {
            if (((QtiApnProfileOmh) apn).getProfileId() == ((QtiApnProfileOmh) dataProfile).getProfileId()) {
                return (QtiApnProfileOmh) dataProfile;
            }
        }
        return null;
    }

    public QtiApnSetting getApnProfile(String serviceType) {
        log("getApnProfile: serviceType=" + serviceType);
        QtiApnSetting profile = null;
        for (QtiApnSetting apn : this.mApnProfilesList) {
            if (apn.canHandleType(serviceType)) {
                profile = apn;
                break;
            }
        }
        log("getApnProfile: return profile=" + profile);
        return profile;
    }

    public ArrayList<QtiApnSetting> getOmhApnProfilesList() {
        log("getOmhApnProfilesList:" + this.mApnProfilesList);
        return this.mApnProfilesList;
    }

    private void addServiceTypeToUnSpecified() {
        for (String apntype : mSupportedApnTypes) {
            if (!this.mOmhServicePriorityMap.containsKey(apntype)) {
                for (QtiApnSetting apn : this.mTempOmhApnProfilesList) {
                    if (((QtiApnProfileOmh) apn).getApnProfileTypeModem() == ApnProfileTypeModem.PROFILE_TYPE_UNSPECIFIED) {
                        ((QtiApnProfileOmh) apn).addServiceType(ApnProfileTypeModem.getApnProfileTypeModem(apntype));
                        log("OMH: Service Type added to UNSPECIFIED is : " + ApnProfileTypeModem.getApnProfileTypeModem(apntype));
                        break;
                    }
                }
            }
        }
    }

    private int omhListGetArbitratedPriority(ArrayList<QtiApnSetting> dataProfileListModem, String serviceType) {
        QtiApnSetting profile = null;
        for (QtiApnSetting apn : dataProfileListModem) {
            if (!((QtiApnProfileOmh) apn).isValidPriority()) {
                log("[OMH] Invalid priority... skipping");
            } else if (profile == null) {
                profile = apn;
            } else if (serviceType == "supl") {
                if (((QtiApnProfileOmh) apn).isPriorityLower(((QtiApnProfileOmh) profile).getPriority())) {
                    profile = apn;
                }
            } else if (((QtiApnProfileOmh) apn).isPriorityHigher(((QtiApnProfileOmh) profile).getPriority())) {
                profile = apn;
            }
        }
        return ((QtiApnProfileOmh) profile).getPriority();
    }

    public void clearActiveApnProfile() {
        this.mActiveApn = null;
    }

    public boolean isApnTypeActive(String type) {
        return this.mActiveApn != null ? this.mActiveApn.canHandleType(type) : false;
    }

    protected boolean isApnTypeAvailable(String type) {
        for (String s : mSupportedApnTypes) {
            if (TextUtils.equals(type, s)) {
                return true;
            }
        }
        return false;
    }

    protected void log(String s) {
        Log.d("QtiCdmaApnProfileTracker", s);
    }

    protected void loge(String s) {
        Log.e("QtiCdmaApnProfileTracker", s);
    }
}
