package com.android.internal.telephony;

import android.os.Build;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.mediatek.internal.telephony.ImsSwitchController;
import com.mediatek.internal.telephony.dataconnection.DcFailCauseManager;
import com.mediatek.internal.telephony.dataconnection.DcFailCauseManager.Operator;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class RetryManager {
    public static final boolean DBG = true;
    private static final String DEFAULT_DATA_RETRY_CONFIG = "max_retries=3, 5000, 5000, 5000";
    private static final long DEFAULT_INTER_APN_DELAY = 20000;
    private static final long DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING = 3000;
    public static final String LOG_TAG = "RetryManager";
    private static final int MAX_SAME_APN_RETRY = 3;
    public static final long NO_RETRY = -1;
    public static final long NO_SUGGESTED_RETRY_DELAY = -2;
    private static final long OPPO_DEFAULT_INTER_APN_DELAY = 5000;
    private static final String OTHERS_APN_TYPE = "others";
    public static final boolean VDBG = false;
    private String mApnType;
    private String mConfig;
    private int mCurrentApnIndex = -1;
    private DcFailCauseManager mDcFcMgr;
    private long mFailFastInterApnDelay;
    private long mInterApnDelay;
    private int mMaxRetryCount;
    private long mModemSuggestedDelay = -2;
    private Phone mPhone;
    private ArrayList<RetryRec> mRetryArray = new ArrayList();
    private int mRetryCount = 0;
    private boolean mRetryForever = false;
    private Random mRng = new Random();
    private int mSameApnRetryCount = 0;
    private TelephonyDevController mTelDevController = TelephonyDevController.getInstance();
    private ArrayList<ApnSetting> mWaitingApns = null;

    private static class RetryRec {
        int mDelayTime;
        int mRandomizationTime;

        RetryRec(int delayTime, int randomizationTime) {
            this.mDelayTime = delayTime;
            this.mRandomizationTime = randomizationTime;
        }
    }

    public RetryManager(Phone phone, String apnType) {
        this.mPhone = phone;
        this.mApnType = apnType;
        this.mDcFcMgr = DcFailCauseManager.getInstance(this.mPhone);
    }

    private boolean configure(String configStr) {
        if (configStr.startsWith("\"") && configStr.endsWith("\"")) {
            configStr = configStr.substring(1, configStr.length() - 1);
        }
        reset();
        log("configure: '" + configStr + "'");
        this.mConfig = configStr;
        if (TextUtils.isEmpty(configStr)) {
            log("configure: cleared");
        } else {
            int defaultRandomization = 0;
            String[] strArray = configStr.split(",");
            for (int i = 0; i < strArray.length; i++) {
                String[] splitStr = strArray[i].split("=", 2);
                splitStr[0] = splitStr[0].trim();
                Pair<Boolean, Integer> value;
                if (splitStr.length > 1) {
                    splitStr[1] = splitStr[1].trim();
                    if (TextUtils.equals(splitStr[0], "default_randomization")) {
                        value = parseNonNegativeInt(splitStr[0], splitStr[1]);
                        if (!((Boolean) value.first).booleanValue()) {
                            return false;
                        }
                        defaultRandomization = ((Integer) value.second).intValue();
                    } else if (!TextUtils.equals(splitStr[0], "max_retries")) {
                        Rlog.e(LOG_TAG, "Unrecognized configuration name value pair: " + strArray[i]);
                        return false;
                    } else if (TextUtils.equals("infinite", splitStr[1])) {
                        this.mRetryForever = true;
                    } else {
                        value = parseNonNegativeInt(splitStr[0], splitStr[1]);
                        if (!((Boolean) value.first).booleanValue()) {
                            return false;
                        }
                        this.mMaxRetryCount = ((Integer) value.second).intValue();
                    }
                } else {
                    splitStr = strArray[i].split(":", 2);
                    splitStr[0] = splitStr[0].trim();
                    RetryRec rr = new RetryRec(0, 0);
                    value = parseNonNegativeInt("delayTime", splitStr[0]);
                    if (!((Boolean) value.first).booleanValue()) {
                        return false;
                    }
                    rr.mDelayTime = ((Integer) value.second).intValue();
                    if (splitStr.length > 1) {
                        splitStr[1] = splitStr[1].trim();
                        value = parseNonNegativeInt("randomizationTime", splitStr[1]);
                        if (!((Boolean) value.first).booleanValue()) {
                            return false;
                        }
                        rr.mRandomizationTime = ((Integer) value.second).intValue();
                    } else {
                        rr.mRandomizationTime = defaultRandomization;
                    }
                    this.mRetryArray.add(rr);
                }
            }
            if (this.mRetryArray.size() > this.mMaxRetryCount) {
                this.mMaxRetryCount = this.mRetryArray.size();
            }
        }
        return true;
    }

    private void configureRetry() {
        String configString = null;
        String str = null;
        try {
            if (Build.IS_DEBUGGABLE) {
                String config = SystemProperties.get("test.data_retry_config");
                if (!TextUtils.isEmpty(config)) {
                    configure(config);
                    return;
                }
            }
            PersistableBundle b = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
            this.mInterApnDelay = OPPO_DEFAULT_INTER_APN_DELAY;
            this.mFailFastInterApnDelay = b.getLong("carrier_data_call_apn_delay_faster_long", DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING);
            String[] allConfigStrings = b.getStringArray("carrier_data_call_retry_config_strings");
            if (allConfigStrings != null) {
                for (String s : allConfigStrings) {
                    if (!TextUtils.isEmpty(s)) {
                        String[] splitStr = s.split(":", 2);
                        if (splitStr.length == 2) {
                            String apnType = splitStr[0].trim();
                            if (apnType.equals(this.mApnType)) {
                                configString = splitStr[1];
                                break;
                            } else if (apnType.equals(OTHERS_APN_TYPE)) {
                                str = splitStr[1];
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
            if (configString == null) {
                if (str != null) {
                    configString = str;
                } else {
                    log("Invalid APN retry configuration!. Use the default one now.");
                    configString = DEFAULT_DATA_RETRY_CONFIG;
                }
            }
        } catch (NullPointerException e) {
            log("Failed to read configuration! Use the hardcoded default value.");
            this.mInterApnDelay = DEFAULT_INTER_APN_DELAY;
            this.mFailFastInterApnDelay = DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING;
            configString = DEFAULT_DATA_RETRY_CONFIG;
        }
        if (TextUtils.equals("default", this.mApnType) && this.mDcFcMgr != null) {
            if (this.mDcFcMgr.isSpecificNetworkAndSimOperator(Operator.OP19)) {
                configString = DcFailCauseManager.DEFAULT_DATA_RETRY_CONFIG_OP19;
            }
            if (this.mDcFcMgr.isSpecificNetworkAndSimOperator(Operator.OP12)) {
                configString = DcFailCauseManager.DEFAULT_DATA_RETRY_CONFIG_OP12;
            }
        }
        if (!(this.mTelDevController == null || this.mTelDevController.getModem(0) == null || this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability() || (!TextUtils.equals(ImsSwitchController.IMS_SERVICE, this.mApnType) && !TextUtils.equals("emergency", this.mApnType)))) {
            log("configureRetry: IMS/EIMS, no retry by mobile. ");
            configString = "max_retries=0, -1, -1, -1";
        }
        configure(configString);
        if (this.mWaitingApns == null || this.mWaitingApns.size() == 0) {
            Rlog.e(LOG_TAG, "configureRetry: mWaitingApns is null or empty");
        } else {
            int index = this.mCurrentApnIndex;
            if (index < 0 || index == this.mWaitingApns.size()) {
                index = 0;
            }
            log("configureRetry: mCurrentApnIndex: " + this.mCurrentApnIndex + ", reset MD data count for apn: " + ((ApnSetting) this.mWaitingApns.get(index)).apn);
            this.mPhone.mCi.resetMdDataRetryCount(((ApnSetting) this.mWaitingApns.get(index)).apn, null);
        }
    }

    private int getRetryTimer() {
        int index;
        int retVal;
        if (this.mRetryCount < this.mRetryArray.size()) {
            index = this.mRetryCount;
        } else {
            index = this.mRetryArray.size() - 1;
        }
        if (index < 0 || index >= this.mRetryArray.size()) {
            retVal = 0;
        } else {
            retVal = ((RetryRec) this.mRetryArray.get(index)).mDelayTime + nextRandomizationTime(index);
        }
        log("getRetryTimer: " + retVal);
        return retVal;
    }

    public int getRetryCount() {
        log("getRetryCount: " + this.mRetryCount);
        return this.mRetryCount;
    }

    private Pair<Boolean, Integer> parseNonNegativeInt(String name, String stringValue) {
        try {
            int value = Integer.parseInt(stringValue);
            return new Pair(Boolean.valueOf(validateNonNegativeInt(name, value)), Integer.valueOf(value));
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, name + " bad value: " + stringValue, e);
            return new Pair(Boolean.valueOf(false), Integer.valueOf(0));
        }
    }

    private boolean validateNonNegativeInt(String name, int value) {
        if (value >= 0) {
            return true;
        }
        Rlog.e(LOG_TAG, name + " bad value: is < 0");
        return false;
    }

    private int nextRandomizationTime(int index) {
        int randomTime = ((RetryRec) this.mRetryArray.get(index)).mRandomizationTime;
        if (randomTime == 0) {
            return 0;
        }
        return this.mRng.nextInt(randomTime);
    }

    private int getMaxSameApnRetry() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(this.mPhone.getSubId());
        }
        if (b != null) {
            return b.getInt("carrier_data_call_max_same_apn_retry_int", 3);
        }
        return 3;
    }

    public ApnSetting getNextApnSetting() {
        if (this.mWaitingApns == null || this.mWaitingApns.size() == 0) {
            log("Waiting APN list is null or empty.");
            return null;
        } else if (this.mModemSuggestedDelay == -2 || this.mSameApnRetryCount >= getMaxSameApnRetry()) {
            this.mSameApnRetryCount = 0;
            int index = this.mCurrentApnIndex;
            do {
                index++;
                if (index == this.mWaitingApns.size()) {
                    index = 0;
                }
                if (!((ApnSetting) this.mWaitingApns.get(index)).permanentFailed) {
                    this.mCurrentApnIndex = index;
                    return (ApnSetting) this.mWaitingApns.get(this.mCurrentApnIndex);
                }
            } while (index != this.mCurrentApnIndex);
            return null;
        } else {
            this.mSameApnRetryCount++;
            return (ApnSetting) this.mWaitingApns.get(this.mCurrentApnIndex);
        }
    }

    public long getDelayForNextApn(boolean failFastEnabled) {
        if (this.mWaitingApns == null || this.mWaitingApns.size() == 0) {
            log("Waiting APN list is null or empty.");
            return -1;
        } else if (this.mModemSuggestedDelay == -1) {
            log("Modem suggested not retrying.");
            return -1;
        } else if (this.mModemSuggestedDelay == -2 || this.mSameApnRetryCount >= getMaxSameApnRetry()) {
            int index = this.mCurrentApnIndex;
            do {
                index++;
                if (index >= this.mWaitingApns.size()) {
                    index = 0;
                }
                if (!((ApnSetting) this.mWaitingApns.get(index)).permanentFailed) {
                    long delay;
                    if (index > this.mCurrentApnIndex) {
                        delay = this.mInterApnDelay;
                    } else if (this.mRetryForever || this.mRetryCount + 1 <= this.mMaxRetryCount) {
                        delay = (long) getRetryTimer();
                        this.mRetryCount++;
                    } else {
                        log("Reached maximum retry count " + this.mMaxRetryCount + ".");
                        return -1;
                    }
                    if (failFastEnabled && delay > this.mFailFastInterApnDelay) {
                        delay = this.mFailFastInterApnDelay;
                    }
                    return delay;
                }
            } while (index != this.mCurrentApnIndex);
            log("All APNs have permanently failed.");
            return -1;
        } else {
            log("Modem suggested retry in " + this.mModemSuggestedDelay + " ms.");
            return this.mModemSuggestedDelay;
        }
    }

    public void markApnPermanentFailed(ApnSetting apn) {
        if (apn != null) {
            apn.permanentFailed = true;
        }
    }

    private void reset() {
        this.mMaxRetryCount = 0;
        this.mRetryCount = 0;
        this.mCurrentApnIndex = -1;
        this.mSameApnRetryCount = 0;
        this.mModemSuggestedDelay = -2;
        this.mRetryArray.clear();
    }

    public void setWaitingApns(ArrayList<ApnSetting> waitingApns) {
        if (waitingApns == null) {
            log("No waiting APNs provided");
            return;
        }
        this.mWaitingApns = waitingApns;
        configureRetry();
        for (ApnSetting apn : this.mWaitingApns) {
            apn.permanentFailed = false;
        }
        log("Setting " + this.mWaitingApns.size() + " waiting APNs.");
    }

    public ArrayList<ApnSetting> getWaitingApns() {
        return this.mWaitingApns;
    }

    public void setModemSuggestedDelay(long delay) {
        this.mModemSuggestedDelay = delay;
    }

    public long getInterApnDelay(boolean failFastEnabled) {
        return failFastEnabled ? this.mFailFastInterApnDelay : this.mInterApnDelay;
    }

    public String toString() {
        return "mApnType=" + this.mApnType + " mRetryCount=" + this.mRetryCount + " mMaxRetryCount=" + this.mMaxRetryCount + " mCurrentApnIndex=" + this.mCurrentApnIndex + " mSameApnRtryCount=" + this.mSameApnRetryCount + " mModemSuggestedDelay=" + this.mModemSuggestedDelay + " mRetryForever=" + this.mRetryForever + " mConfig={" + this.mConfig + "}";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("  RetryManager");
        pw.println("***************************************");
        pw.println("    config = " + this.mConfig);
        pw.println("    mApnType = " + this.mApnType);
        pw.println("    mCurrentApnIndex = " + this.mCurrentApnIndex);
        pw.println("    mRetryCount = " + this.mRetryCount);
        pw.println("    mMaxRetryCount = " + this.mMaxRetryCount);
        pw.println("    mSameApnRetryCount = " + this.mSameApnRetryCount);
        pw.println("    mModemSuggestedDelay = " + this.mModemSuggestedDelay);
        if (this.mWaitingApns != null) {
            pw.println("    APN list: ");
            for (int i = 0; i < this.mWaitingApns.size(); i++) {
                pw.println("      [" + i + "]=" + this.mWaitingApns.get(i));
            }
        }
        pw.println("***************************************");
        pw.flush();
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mApnType + "] " + s);
    }
}
