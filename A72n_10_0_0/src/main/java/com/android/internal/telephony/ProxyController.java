package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.ims.RcsMessageStoreController;
import com.android.internal.telephony.uicc.UiccController;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyController {
    protected static final int EVENT_APPLY_RC_RESPONSE = 3;
    protected static final int EVENT_FINISH_RC_RESPONSE = 4;
    protected static final int EVENT_NOTIFICATION_RC_CHANGED = 1;
    protected static final int EVENT_START_RC_RESPONSE = 2;
    protected static final int EVENT_TIMEOUT = 5;
    static final String LOG_TAG = "ProxyController";
    protected static final int SET_RC_STATUS_APPLYING = 3;
    protected static final int SET_RC_STATUS_FAIL = 5;
    protected static final int SET_RC_STATUS_IDLE = 0;
    protected static final int SET_RC_STATUS_STARTED = 2;
    protected static final int SET_RC_STATUS_STARTING = 1;
    protected static final int SET_RC_STATUS_SUCCESS = 4;
    protected static final int SET_RC_TIMEOUT_WAITING_MSEC = 45000;
    @UnsupportedAppUsage
    private static ProxyController sProxyController;
    protected CommandsInterface[] mCi;
    protected Context mContext;
    protected String[] mCurrentLogicalModemIds;
    protected Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.ProxyController.AnonymousClass1 */

        public void handleMessage(Message msg) {
            ProxyController proxyController = ProxyController.this;
            proxyController.logd("handleMessage msg.what=" + msg.what);
            int i = msg.what;
            if (i == 1) {
                ProxyController.this.onNotificationRadioCapabilityChanged(msg);
            } else if (i == 2) {
                ProxyController.this.onStartRadioCapabilityResponse(msg);
            } else if (i == 3) {
                ProxyController.this.onApplyRadioCapabilityResponse(msg);
            } else if (i == 4) {
                ProxyController.this.onFinishRadioCapabilityResponse(msg);
            } else if (i == 5) {
                ProxyController.this.onTimeoutRadioCapability(msg);
            }
        }
    };
    protected String[] mNewLogicalModemIds;
    protected int[] mNewRadioAccessFamily;
    @UnsupportedAppUsage
    protected int[] mOldRadioAccessFamily;
    protected PhoneSubInfoController mPhoneSubInfoController;
    protected PhoneSwitcher mPhoneSwitcher;
    protected Phone[] mPhones;
    protected int mRadioAccessFamilyStatusCounter;
    @UnsupportedAppUsage
    protected int mRadioCapabilitySessionId;
    @UnsupportedAppUsage
    protected int[] mSetRadioAccessFamilyStatus;
    private SmsController mSmsController;
    protected boolean mTransactionFailed = false;
    protected UiccController mUiccController;
    protected UiccPhoneBookController mUiccPhoneBookController;
    protected SmsController mUiccSmsController;
    @UnsupportedAppUsage
    protected AtomicInteger mUniqueIdGenerator = new AtomicInteger(new Random().nextInt());
    protected PowerManager.WakeLock mWakeLock;

    public static ProxyController getInstance(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher ps) {
        if (sProxyController == null) {
            sProxyController = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeProxyController(context, phone, uiccController, ci, ps);
        }
        return sProxyController;
    }

    @UnsupportedAppUsage
    public static ProxyController getInstance() {
        return sProxyController;
    }

    public ProxyController(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher phoneSwitcher) {
        logd("Constructor - Enter");
        this.mContext = context;
        this.mPhones = phone;
        this.mUiccController = uiccController;
        this.mCi = ci;
        this.mPhoneSwitcher = phoneSwitcher;
        RcsMessageStoreController.init(context);
        this.mUiccPhoneBookController = new UiccPhoneBookController(this.mPhones);
        this.mPhoneSubInfoController = new PhoneSubInfoController(this.mContext, this.mPhones);
        this.mSmsController = new SmsController(this.mContext);
        Phone[] phoneArr = this.mPhones;
        this.mSetRadioAccessFamilyStatus = new int[phoneArr.length];
        this.mNewRadioAccessFamily = new int[phoneArr.length];
        this.mOldRadioAccessFamily = new int[phoneArr.length];
        this.mCurrentLogicalModemIds = new String[phoneArr.length];
        this.mNewLogicalModemIds = new String[phoneArr.length];
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        clearTransaction();
        int i = 0;
        while (true) {
            Phone[] phoneArr2 = this.mPhones;
            if (i < phoneArr2.length) {
                phoneArr2[i].registerForRadioCapabilityChanged(this.mHandler, 1, null);
                i++;
            } else {
                logd("Constructor - Exit");
                return;
            }
        }
    }

    public void registerForAllDataDisconnected(int subId, Handler h, int what) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount()) {
            this.mPhones[phoneId].registerForAllDataDisconnected(h, what);
        }
    }

    public void unregisterForAllDataDisconnected(int subId, Handler h) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount()) {
            this.mPhones[phoneId].unregisterForAllDataDisconnected(h);
        }
    }

    public boolean areAllDataDisconnected(int subId) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return true;
        }
        return this.mPhones[phoneId].areAllDataDisconnected();
    }

    public int getRadioAccessFamily(int phoneId) {
        Phone[] phoneArr = this.mPhones;
        if (phoneId >= phoneArr.length) {
            return 0;
        }
        return phoneArr[phoneId].getRadioAccessFamily();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
        r0 = true;
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        r2 = r5.mPhones;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        if (r1 >= r2.length) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0048, code lost:
        if (r2[r1].getRadioAccessFamily() == r6[r1].getRadioAccessFamily()) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004a, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004b, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004e, code lost:
        if (r0 == false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        logd("setRadioCapability: Already in requested configuration, nothing to do.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0056, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0057, code lost:
        clearTransaction();
        r5.mWakeLock.acquire();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0063, code lost:
        return doSetRadioCapabilities(r6);
     */
    public boolean setRadioCapability(RadioAccessFamily[] rafs) {
        if (rafs.length == this.mPhones.length) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                for (int i = 0; i < this.mPhones.length; i++) {
                    if (this.mSetRadioAccessFamilyStatus[i] != 0) {
                        loge("setRadioCapability: Phone[" + i + "] is not idle. Rejecting request.");
                        return false;
                    }
                }
            }
        } else {
            throw new RuntimeException("Length of input rafs must equal to total phone count");
        }
    }

    public SmsController getSmsController() {
        return this.mSmsController;
    }

    /* access modifiers changed from: protected */
    public boolean doSetRadioCapabilities(RadioAccessFamily[] rafs) {
        this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, this.mRadioCapabilitySessionId, 0), 45000);
        synchronized (this.mSetRadioAccessFamilyStatus) {
            logd("setRadioCapability: new request session id=" + this.mRadioCapabilitySessionId);
            resetRadioAccessFamilyStatusCounter();
            for (int i = 0; i < rafs.length; i++) {
                int phoneId = rafs[i].getPhoneId();
                logd("setRadioCapability: phoneId=" + phoneId + " status=STARTING");
                this.mSetRadioAccessFamilyStatus[phoneId] = 1;
                this.mOldRadioAccessFamily[phoneId] = this.mPhones[phoneId].getRadioAccessFamily();
                int requestedRaf = rafs[i].getRadioAccessFamily();
                this.mNewRadioAccessFamily[phoneId] = requestedRaf;
                this.mCurrentLogicalModemIds[phoneId] = this.mPhones[phoneId].getModemUuId();
                this.mNewLogicalModemIds[phoneId] = getLogicalModemIdFromRaf(requestedRaf);
                logd("setRadioCapability: mOldRadioAccessFamily[" + phoneId + "]=" + this.mOldRadioAccessFamily[phoneId]);
                logd("setRadioCapability: mNewRadioAccessFamily[" + phoneId + "]=" + this.mNewRadioAccessFamily[phoneId]);
                sendRadioCapabilityRequest(phoneId, this.mRadioCapabilitySessionId, 1, this.mOldRadioAccessFamily[phoneId], this.mCurrentLogicalModemIds[phoneId], 0, 2);
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onStartRadioCapabilityResponse(Message msg) {
        synchronized (this.mSetRadioAccessFamilyStatus) {
            AsyncResult ar = (AsyncResult) msg.obj;
            boolean z = true;
            if (TelephonyManager.getDefault().getPhoneCount() != 1 || ar.exception == null) {
                RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
                if (rc != null) {
                    if (rc.getSession() == this.mRadioCapabilitySessionId) {
                        this.mRadioAccessFamilyStatusCounter--;
                        int id = rc.getPhoneId();
                        if (((AsyncResult) msg.obj).exception != null) {
                            logd("onStartRadioCapabilityResponse: Error response session=" + rc.getSession());
                            logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                            this.mSetRadioAccessFamilyStatus[id] = 5;
                            this.mTransactionFailed = true;
                        } else {
                            logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=STARTED");
                            this.mSetRadioAccessFamilyStatus[id] = 2;
                        }
                        if (this.mRadioAccessFamilyStatusCounter == 0) {
                            HashSet<String> modemsInUse = new HashSet<>(this.mNewLogicalModemIds.length);
                            for (String modemId : this.mNewLogicalModemIds) {
                                if (!modemsInUse.add(modemId)) {
                                    this.mTransactionFailed = true;
                                    Log.wtf(LOG_TAG, "ERROR: sending down the same id for different phones");
                                }
                            }
                            StringBuilder sb = new StringBuilder();
                            sb.append("onStartRadioCapabilityResponse: success=");
                            if (this.mTransactionFailed) {
                                z = false;
                            }
                            sb.append(z);
                            logd(sb.toString());
                            if (this.mTransactionFailed) {
                                issueFinish(this.mRadioCapabilitySessionId);
                            } else {
                                resetRadioAccessFamilyStatusCounter();
                                for (int i = 0; i < this.mPhones.length; i++) {
                                    sendRadioCapabilityRequest(i, this.mRadioCapabilitySessionId, 2, this.mNewRadioAccessFamily[i], this.mNewLogicalModemIds[i], 0, 3);
                                    logd("onStartRadioCapabilityResponse: phoneId=" + i + " status=APPLYING");
                                    this.mSetRadioAccessFamilyStatus[i] = 3;
                                }
                            }
                        }
                        return;
                    }
                }
                logd("onStartRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
                return;
            }
            logd("onStartRadioCapabilityResponse got exception=" + ar.exception);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED"));
            clearTransaction();
        }
    }

    /* access modifiers changed from: protected */
    public void onApplyRadioCapabilityResponse(Message msg) {
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
        if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
            logd("onApplyRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
            onApplyRadioCapabilityErrorHandler(msg);
            return;
        }
        logd("onApplyRadioCapabilityResponse: rc=" + rc);
        if (((AsyncResult) msg.obj).exception != null) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd("onApplyRadioCapabilityResponse: Error response session=" + rc.getSession());
                int id = rc.getPhoneId();
                onApplyExceptionHandler(msg);
                logd("onApplyRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                this.mSetRadioAccessFamilyStatus[id] = 5;
                this.mTransactionFailed = true;
            }
            return;
        }
        logd("onApplyRadioCapabilityResponse: Valid start expecting notification rc=" + rc);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00bc  */
    public void onNotificationRadioCapabilityChanged(Message msg) {
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
        if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
            logd("onNotificationRadioCapabilityChanged: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
            return;
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            logd("onNotificationRadioCapabilityChanged: rc=" + rc);
            if (rc.getSession() != this.mRadioCapabilitySessionId) {
                logd("onNotificationRadioCapabilityChanged: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
                return;
            }
            int id = rc.getPhoneId();
            if (((AsyncResult) msg.obj).exception == null) {
                if (rc.getStatus() != 2) {
                    logd("onNotificationRadioCapabilityChanged: phoneId=" + id + " status=SUCCESS");
                    this.mSetRadioAccessFamilyStatus[id] = 4;
                    this.mPhoneSwitcher.onRadioCapChanged(id);
                    this.mPhones[id].radioCapabilityUpdated(rc);
                    this.mRadioAccessFamilyStatusCounter--;
                    if (this.mRadioAccessFamilyStatusCounter == 0) {
                        logd("onNotificationRadioCapabilityChanged: APPLY URC success=" + this.mTransactionFailed);
                        issueFinish(this.mRadioCapabilitySessionId);
                    }
                }
            }
            logd("onNotificationRadioCapabilityChanged: phoneId=" + id + " status=FAIL");
            this.mSetRadioAccessFamilyStatus[id] = 5;
            this.mTransactionFailed = true;
            this.mRadioAccessFamilyStatusCounter--;
            if (this.mRadioAccessFamilyStatusCounter == 0) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishRadioCapabilityResponse(Message msg) {
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
        if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
            logd("onFinishRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
            return;
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            logd(" onFinishRadioCapabilityResponse mRadioAccessFamilyStatusCounter=" + this.mRadioAccessFamilyStatusCounter);
            this.mRadioAccessFamilyStatusCounter = this.mRadioAccessFamilyStatusCounter + -1;
            if (this.mRadioAccessFamilyStatusCounter == 0) {
                completeRadioCapabilityTransaction();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onTimeoutRadioCapability(Message msg) {
        if (msg.arg1 != this.mRadioCapabilitySessionId) {
            logd("RadioCapability timeout: Ignore msg.arg1=" + msg.arg1 + "!= mRadioCapabilitySessionId=" + this.mRadioCapabilitySessionId);
            return;
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i = 0; i < this.mPhones.length; i++) {
                logd("RadioCapability timeout: mSetRadioAccessFamilyStatus[" + i + "]=" + this.mSetRadioAccessFamilyStatus[i]);
            }
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            this.mRadioAccessFamilyStatusCounter = 0;
            this.mTransactionFailed = true;
            issueFinish(this.mRadioCapabilitySessionId);
        }
    }

    /* access modifiers changed from: protected */
    public void issueFinish(int sessionId) {
        int i;
        String str;
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i2 = 0; i2 < this.mPhones.length; i2++) {
                logd("issueFinish: phoneId=" + i2 + " sessionId=" + sessionId + " mTransactionFailed=" + this.mTransactionFailed);
                this.mRadioAccessFamilyStatusCounter = this.mRadioAccessFamilyStatusCounter + 1;
                if (this.mTransactionFailed) {
                    i = this.mOldRadioAccessFamily[i2];
                } else {
                    i = this.mNewRadioAccessFamily[i2];
                }
                if (this.mTransactionFailed) {
                    str = this.mCurrentLogicalModemIds[i2];
                } else {
                    str = this.mNewLogicalModemIds[i2];
                }
                sendRadioCapabilityRequest(i2, sessionId, 4, i, str, this.mTransactionFailed ? 2 : 1, 4);
                if (this.mTransactionFailed) {
                    logd("issueFinish: phoneId: " + i2 + " status: FAIL");
                    this.mSetRadioAccessFamilyStatus[i2] = 5;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void completeRadioCapabilityTransaction() {
        Intent intent;
        StringBuilder sb = new StringBuilder();
        sb.append("onFinishRadioCapabilityResponse: success=");
        sb.append(!this.mTransactionFailed);
        logd(sb.toString());
        if (!this.mTransactionFailed) {
            ArrayList<RadioAccessFamily> phoneRAFList = new ArrayList<>();
            int i = 0;
            while (true) {
                Phone[] phoneArr = this.mPhones;
                if (i >= phoneArr.length) {
                    break;
                }
                int raf = phoneArr[i].getRadioAccessFamily();
                logd("radioAccessFamily[" + i + "]=" + raf);
                phoneRAFList.add(new RadioAccessFamily(i, raf));
                i++;
            }
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            intent.putParcelableArrayListExtra("rafs", phoneRAFList);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            clearTransaction();
        } else {
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
            this.mTransactionFailed = false;
            RadioAccessFamily[] rafs = new RadioAccessFamily[this.mPhones.length];
            for (int phoneId = 0; phoneId < this.mPhones.length; phoneId++) {
                rafs[phoneId] = new RadioAccessFamily(phoneId, this.mOldRadioAccessFamily[phoneId]);
            }
            doSetRadioCapabilities(rafs);
        }
        this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
    }

    /* access modifiers changed from: protected */
    public void clearTransaction() {
        logd("clearTransaction");
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i = 0; i < this.mPhones.length; i++) {
                logd("clearTransaction: phoneId=" + i + " status=IDLE");
                this.mSetRadioAccessFamilyStatus[i] = 0;
                this.mOldRadioAccessFamily[i] = 0;
                this.mNewRadioAccessFamily[i] = 0;
                this.mTransactionFailed = false;
            }
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetRadioAccessFamilyStatusCounter() {
        this.mRadioAccessFamilyStatusCounter = this.mPhones.length;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void sendRadioCapabilityRequest(int phoneId, int sessionId, int rcPhase, int radioFamily, String logicalModemId, int status, int eventId) {
        this.mPhones[phoneId].setRadioCapability(new RadioCapability(phoneId, sessionId, rcPhase, radioFamily, logicalModemId, status), this.mHandler.obtainMessage(eventId));
    }

    public int getMaxRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int maxNumRafBit = 0;
        int maxRaf = 0;
        int len = 0;
        while (true) {
            Phone[] phoneArr = this.mPhones;
            if (len >= phoneArr.length) {
                return maxRaf;
            }
            numRafSupported[len] = Integer.bitCount(phoneArr[len].getRadioAccessFamily());
            if (maxNumRafBit < numRafSupported[len]) {
                maxNumRafBit = numRafSupported[len];
                maxRaf = this.mPhones[len].getRadioAccessFamily();
            }
            len++;
        }
    }

    public int getMinRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int minNumRafBit = 0;
        int minRaf = 0;
        int len = 0;
        while (true) {
            Phone[] phoneArr = this.mPhones;
            if (len >= phoneArr.length) {
                return minRaf;
            }
            numRafSupported[len] = Integer.bitCount(phoneArr[len].getRadioAccessFamily());
            if (minNumRafBit == 0 || minNumRafBit > numRafSupported[len]) {
                minNumRafBit = numRafSupported[len];
                minRaf = this.mPhones[len].getRadioAccessFamily();
            }
            len++;
        }
    }

    /* access modifiers changed from: protected */
    public String getLogicalModemIdFromRaf(int raf) {
        int phoneId = 0;
        while (true) {
            Phone[] phoneArr = this.mPhones;
            if (phoneId >= phoneArr.length) {
                return null;
            }
            if (phoneArr[phoneId].getRadioAccessFamily() == raf) {
                return this.mPhones[phoneId].getModemUuId();
            }
            phoneId++;
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    /* access modifiers changed from: protected */
    public void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }

    /* access modifiers changed from: protected */
    public void onApplyRadioCapabilityErrorHandler(Message msg) {
    }

    /* access modifiers changed from: protected */
    public void onApplyExceptionHandler(Message msg) {
    }
}
