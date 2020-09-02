package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.radio.V1_0.HardwareConfigModem;
import android.hardware.radio.V1_0.HardwareConfigSim;
import android.hardware.radio.V1_0.IRadio;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_4.CellInfoNr;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.CellInfo;
import android.telephony.MtkRadioAccessFamily;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.HardwareConfig;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILRequest;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.android.mms.pdu.MtkCharacterSets;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.ims.MtkDedicateDataCallResponse;
import com.mediatek.internal.telephony.ims.MtkPacketFilterInfo;
import com.mediatek.internal.telephony.ims.MtkQosStatus;
import com.mediatek.internal.telephony.ims.MtkTftParameter;
import com.mediatek.internal.telephony.ims.MtkTftStatus;
import com.mediatek.internal.telephony.phb.PBEntry;
import com.mediatek.internal.telephony.phb.PhbEntry;
import com.mediatek.internal.telephony.uicc.MtkSIMRecords;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimConstants;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import mediatek.telephony.MtkSmsParameters;
import vendor.mediatek.hardware.mtkradioex.V1_0.CallForwardInfoEx;
import vendor.mediatek.hardware.mtkradioex.V1_0.DedicateDataCall;
import vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx;
import vendor.mediatek.hardware.mtkradioex.V1_0.MtkApnTypes;
import vendor.mediatek.hardware.mtkradioex.V1_0.PhbEntryExt;
import vendor.mediatek.hardware.mtkradioex.V1_0.PhbEntryStructure;
import vendor.mediatek.hardware.mtkradioex.V1_0.PktFilter;
import vendor.mediatek.hardware.mtkradioex.V1_0.SimAuthStructure;
import vendor.mediatek.hardware.mtkradioex.V1_0.SmsParams;

public class MtkRIL extends RIL {
    public static final String CB_FACILITY_BA_ACR = "ACR";
    public static final int CF_REASON_NOT_REGISTERED = 6;
    protected static final int EVENT_MTKRADIOEX_PROXY_DEAD = 1006;
    static final String[] HIDL_SERVICE_NAME_MTK = {"mtkSlot1", "mtkSlot2", "mtkSlot3"};
    protected static final int IMTKRADIOEX_GET_SERVICE_DELAY_MILLIS = 1000;
    static final boolean MTK_RILJ_LOGD = true;
    static final boolean MTK_RILJ_LOGV = true;
    static final String RILJ_LOG_TAG = "MtkRILJ";
    public static final int SERVICE_CLASS_LINE2 = 256;
    public static final int SERVICE_CLASS_MTK_MAX = 512;
    public static final int SERVICE_CLASS_VIDEO = 512;
    private static final int WORLD_PHONE_RELOAD_TYPE = 1;
    private static final int WORLD_PHONE_STORE_TYPE = 2;
    public static final boolean showRat = true;
    private ArrayList<String> hide_plmns;
    protected RegistrantList mAttachApnChangedRegistrants;
    protected RegistrantList mBipProCmdRegistrant;
    protected Registrant mCDMACardEsnMeidRegistrant;
    protected RegistrantList mCallAdditionalInfoRegistrants;
    protected RegistrantList mCallForwardingInfoRegistrants;
    protected Registrant mCallRelatedSuppSvcRegistrant;
    protected RegistrantList mCardDetectedIndRegistrant;
    Object mCfuReturnValue;
    protected RegistrantList mCipherIndicationRegistrants;
    protected RegistrantList mCsNetworkStateRegistrants;
    protected RegistrantList mDataAllowedRegistrants;
    protected RegistrantList mDedicatedBearerActivedRegistrants;
    protected RegistrantList mDedicatedBearerDeactivatedRegistrants;
    protected RegistrantList mDedicatedBearerModifiedRegistrants;
    protected RegistrantList mDsbpStateRegistrant;
    protected RegistrantList mDsdaStateRegistrant;
    DtmfQueueHandler mDtmfReqQueue;
    protected RegistrantList mEconfSrvccRegistrants;
    protected Object mEcopsReturnValue;
    protected RegistrantList mEmbmsAtInfoNotificationRegistrant;
    protected RegistrantList mEmbmsSessionStatusNotificationRegistrant;
    protected Object mEmsrReturnValue;
    protected Object mEspOrMeid;
    protected Registrant mEtwsNotificationRegistrant;
    protected RegistrantList mFemtoCellInfoRegistrants;
    protected RegistrantList mGmssRatChangedRegistrant;
    protected RegistrantList mImeiLockRegistrant;
    protected RegistrantList mImsiRefreshDoneRegistrant;
    protected Registrant mIncomingCallIndicationRegistrant;
    public Integer mInstanceId;
    BroadcastReceiver mIntentReceiver;
    protected RegistrantList mInvalidSimInfoRegistrant;
    public boolean mIsCardDetected;
    public boolean mIsSmsReady;
    protected RegistrantList mLteAccessStratumStateRegistrants;
    protected RegistrantList mMccMncRegistrants;
    protected RegistrantList mMdDataRetryCountResetRegistrants;
    protected Registrant mMeSmsFullRegistrant;
    protected RegistrantList mMobileDataUsageRegistrants;
    protected RegistrantList mModulationRegistrants;
    protected Context mMtkContext;
    MtkRadioExIndication mMtkRadioExIndication;
    protected final AtomicLong mMtkRadioExProxyCookie;
    protected final MtkRadioExProxyDeathRecipient mMtkRadioExProxyDeathRecipient;
    MtkRadioExResponse mMtkRadioExResponse;
    MtkRadioIndication mMtkRadioIndication;
    MtkRadioResponse mMtkRadioResponse;
    private boolean mMtkRilJIntiDone;
    private IMtkRilOp mMtkRilOp;
    protected RegistrantList mNetworkEventRegistrants;
    protected RegistrantList mNetworkInfoRegistrant;
    protected RegistrantList mNetworkRejectRegistrants;
    protected RegistrantList mNwLimitRegistrants;
    protected RegistrantList mPcoDataAfterAttachedRegistrants;
    public RegistrantList mPhbReadyRegistrants;
    protected RegistrantList mPlmnChangeNotificationRegistrant;
    protected RegistrantList mPsNetworkStateRegistrants;
    protected RegistrantList mPseudoCellInfoRegistrants;
    protected RegistrantList mQualifiedNetworkTypesRegistrant;
    volatile IMtkRadioEx mRadioProxyMtk;
    protected Registrant mRegistrationSuspendedRegistrant;
    protected RegistrantList mRemoveRestrictEutranRegistrants;
    protected RegistrantList mResetAttachApnRegistrants;
    protected RegistrantList mRsuSimlockRegistrants;
    protected RegistrantList mSignalStrengthWithWcdmaEcioRegistrants;
    protected RegistrantList mSimCommonSlotNoChanged;
    protected RegistrantList mSimMissing;
    protected RegistrantList mSimPlugIn;
    protected RegistrantList mSimPlugOut;
    protected RegistrantList mSimPowerChanged;
    Object mSimPowerInfo;
    protected RegistrantList mSimRecovery;
    protected RegistrantList mSimTrayPlugIn;
    Object mSmlSlotLockInfo;
    protected RegistrantList mSmlSlotLockInfoChanged;
    protected RegistrantList mSmsReadyRegistrants;
    protected Registrant mSsnExRegistrant;
    protected RegistrantList mStkSetupMenuResetRegistrant;
    protected RegistrantList mTxPowerRegistrant;
    protected RegistrantList mTxPowerStatusRegistrant;
    protected Registrant mUnsolOemHookRegistrant;
    protected RegistrantList mVirtualSimOff;
    protected RegistrantList mVirtualSimOn;
    protected RegistrantList mVsimIndicationRegistrants;
    protected Object mWPMonitor;

    public class MtkRilHandler extends RIL.RilHandler {
        public MtkRilHandler() {
            super(MtkRIL.this);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1006) {
                MtkRIL.super.handleMessage(msg);
                return;
            }
            MtkRIL mtkRIL = MtkRIL.this;
            mtkRIL.riljLog("handleMessage: EVENT_MTKRADIOEX_PROXY_DEAD cookie = " + msg.obj + " mMtkRadioExProxyCookie = " + MtkRIL.this.mMtkRadioExProxyCookie.get());
            if (((Long) msg.obj).longValue() != MtkRIL.this.mMtkRadioExProxyCookie.get()) {
                return;
            }
            if (MtkRIL.this.mRadioProxy == null) {
                MtkRIL.this.riljLoge("handleMessage: wait for getting mRadioProxy");
                MtkRIL.this.mRilHandler.removeMessages(1006);
                MtkRIL.this.mRilHandler.sendMessageDelayed(MtkRIL.this.mRilHandler.obtainMessage(1006, Long.valueOf(MtkRIL.this.mMtkRadioExProxyCookie.get())), 1000);
                return;
            }
            MtkRIL.this.resetMtkProxyAndRequestList();
        }
    }

    final class MtkRadioExProxyDeathRecipient implements IHwBinder.DeathRecipient {
        MtkRadioExProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            MtkRIL.this.riljLog("IMtkRadioEx serviceDied");
            MtkRIL.this.mRilHandler.removeMessages(1006);
            MtkRIL.this.mRilHandler.sendMessageDelayed(MtkRIL.this.mRilHandler.obtainMessage(1006, Long.valueOf(MtkRIL.this.mMtkRadioExProxyCookie.get())), 1000);
        }
    }

    class DtmfQueueHandler {
        private final boolean DTMF_STATUS_START;
        private final boolean DTMF_STATUS_STOP;
        public final int MAXIMUM_DTMF_REQUEST;
        /* access modifiers changed from: private */
        public Vector mDtmfQueue;
        private boolean mDtmfStatus;
        private boolean mIsSendChldRequest;
        private DtmfQueueRR mPendingCHLDRequest;

        public class DtmfQueueRR {
            public Object[] params;
            public RILRequest rr;

            public DtmfQueueRR(RILRequest rr2, Object[] params2) {
                this.rr = rr2;
                this.params = params2;
            }
        }

        public DtmfQueueHandler() {
            this.MAXIMUM_DTMF_REQUEST = 32;
            this.DTMF_STATUS_START = true;
            this.DTMF_STATUS_STOP = false;
            this.mDtmfStatus = false;
            this.mDtmfQueue = new Vector(32);
            this.mPendingCHLDRequest = null;
            this.mIsSendChldRequest = false;
            this.mDtmfStatus = false;
        }

        public void start() {
            this.mDtmfStatus = true;
        }

        public void stop() {
            this.mDtmfStatus = false;
        }

        public boolean isStart() {
            return this.mDtmfStatus;
        }

        public void add(DtmfQueueRR o) {
            this.mDtmfQueue.addElement(o);
        }

        public void remove(DtmfQueueRR o) {
            this.mDtmfQueue.remove(o);
        }

        public void remove(int idx) {
            this.mDtmfQueue.removeElementAt(idx);
        }

        public DtmfQueueRR get() {
            return (DtmfQueueRR) this.mDtmfQueue.get(0);
        }

        public int size() {
            return this.mDtmfQueue.size();
        }

        public void setPendingRequest(DtmfQueueRR r) {
            this.mPendingCHLDRequest = r;
        }

        public DtmfQueueRR getPendingRequest() {
            return this.mPendingCHLDRequest;
        }

        public void setSendChldRequest() {
            this.mIsSendChldRequest = true;
        }

        public void resetSendChldRequest() {
            this.mIsSendChldRequest = false;
        }

        public boolean hasSendChldRequest() {
            MtkRIL mtkRIL = MtkRIL.this;
            mtkRIL.mtkRiljLog("mIsSendChldRequest = " + this.mIsSendChldRequest);
            return this.mIsSendChldRequest;
        }

        public DtmfQueueRR buildDtmfQueueRR(RILRequest rr, Object[] param) {
            if (rr == null) {
                return null;
            }
            MtkRIL mtkRIL = MtkRIL.this;
            mtkRIL.mtkRiljLog("DtmfQueueHandler.buildDtmfQueueRR build ([" + rr.mSerial + "] reqId=" + rr.mRequest + ")");
            return new DtmfQueueRR(rr, param);
        }
    }

    @VisibleForTesting
    public MtkRIL() {
        this.mRadioProxyMtk = null;
        this.mMtkRilJIntiDone = false;
        this.mCallAdditionalInfoRegistrants = new RegistrantList();
        this.mCipherIndicationRegistrants = new RegistrantList();
        this.mFemtoCellInfoRegistrants = new RegistrantList();
        this.mEmbmsSessionStatusNotificationRegistrant = new RegistrantList();
        this.mEmbmsAtInfoNotificationRegistrant = new RegistrantList();
        this.mPhbReadyRegistrants = new RegistrantList();
        this.mCallForwardingInfoRegistrants = new RegistrantList();
        this.mTxPowerRegistrant = new RegistrantList();
        this.mTxPowerStatusRegistrant = new RegistrantList();
        this.mCfuReturnValue = null;
        this.mMtkRilOp = null;
        this.mMtkRadioExProxyCookie = new AtomicLong(0);
        this.mDtmfReqQueue = new DtmfQueueHandler();
        this.mPlmnChangeNotificationRegistrant = new RegistrantList();
        this.mEmsrReturnValue = null;
        this.mEcopsReturnValue = null;
        this.mWPMonitor = new Object();
        this.mGmssRatChangedRegistrant = new RegistrantList();
        this.mResetAttachApnRegistrants = new RegistrantList();
        this.mAttachApnChangedRegistrants = new RegistrantList();
        this.mPcoDataAfterAttachedRegistrants = new RegistrantList();
        this.mRemoveRestrictEutranRegistrants = new RegistrantList();
        this.mMdDataRetryCountResetRegistrants = new RegistrantList();
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.mediatek.internal.telephony.MtkRIL.AnonymousClass1 */
            private static final int MODE_CDMA_ASSERT = 31;
            private static final int MODE_CDMA_RESET = 32;
            private static final int MODE_CDMA_RILD_NE = 103;
            private static final int MODE_GSM_RILD_NE = 101;
            private static final int MODE_PHONE_PROCESS_JE = 100;

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mtk.TEST_TRM")) {
                    int mode = intent.getIntExtra("mode", 2);
                    Rlog.d(MtkRIL.RILJ_LOG_TAG, "RIL received com.mtk.TEST_TRM, mode = " + mode + ", mInstanceIds = " + MtkRIL.this.mInstanceId);
                    if (mode != 100) {
                        MtkRIL.this.setTrm(mode, null);
                        return;
                    }
                    throw new RuntimeException("UserTriggerPhoneJE");
                }
                Rlog.w(MtkRIL.RILJ_LOG_TAG, "RIL received unexpected Intent: " + intent.getAction());
            }
        };
        this.hide_plmns = new ArrayList<>();
        this.mCsNetworkStateRegistrants = new RegistrantList();
        this.mSignalStrengthWithWcdmaEcioRegistrants = new RegistrantList();
        this.mVirtualSimOn = new RegistrantList();
        this.mVirtualSimOff = new RegistrantList();
        this.mImeiLockRegistrant = new RegistrantList();
        this.mImsiRefreshDoneRegistrant = new RegistrantList();
        this.mCardDetectedIndRegistrant = new RegistrantList();
        this.mRsuSimlockRegistrants = new RegistrantList();
        this.mInvalidSimInfoRegistrant = new RegistrantList();
        this.mNetworkEventRegistrants = new RegistrantList();
        this.mNetworkRejectRegistrants = new RegistrantList();
        this.mModulationRegistrants = new RegistrantList();
        this.mIsSmsReady = false;
        this.mSmsReadyRegistrants = new RegistrantList();
        this.mEspOrMeid = null;
        this.mIsCardDetected = false;
        this.mPsNetworkStateRegistrants = new RegistrantList();
        this.mNetworkInfoRegistrant = new RegistrantList();
        this.mDataAllowedRegistrants = new RegistrantList();
        this.mPseudoCellInfoRegistrants = new RegistrantList();
        this.mSimTrayPlugIn = new RegistrantList();
        this.mSimCommonSlotNoChanged = new RegistrantList();
        this.mBipProCmdRegistrant = new RegistrantList();
        this.mStkSetupMenuResetRegistrant = new RegistrantList();
        this.mLteAccessStratumStateRegistrants = new RegistrantList();
        this.mSimPlugIn = new RegistrantList();
        this.mSimPlugOut = new RegistrantList();
        this.mSimMissing = new RegistrantList();
        this.mSimRecovery = new RegistrantList();
        this.mSimPowerChanged = new RegistrantList();
        this.mSimPowerInfo = null;
        this.mSmlSlotLockInfoChanged = new RegistrantList();
        this.mSmlSlotLockInfo = null;
        this.mEconfSrvccRegistrants = new RegistrantList();
        this.mMccMncRegistrants = new RegistrantList();
        this.mVsimIndicationRegistrants = new RegistrantList();
        this.mDedicatedBearerActivedRegistrants = new RegistrantList();
        this.mDedicatedBearerModifiedRegistrants = new RegistrantList();
        this.mDedicatedBearerDeactivatedRegistrants = new RegistrantList();
        this.mDsbpStateRegistrant = new RegistrantList();
        this.mDsdaStateRegistrant = new RegistrantList();
        this.mQualifiedNetworkTypesRegistrant = new RegistrantList();
        this.mMobileDataUsageRegistrants = new RegistrantList();
        this.mNwLimitRegistrants = new RegistrantList();
        this.mMtkRadioExProxyDeathRecipient = null;
    }

    public MtkRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mRadioProxyMtk = null;
        this.mMtkRilJIntiDone = false;
        this.mCallAdditionalInfoRegistrants = new RegistrantList();
        this.mCipherIndicationRegistrants = new RegistrantList();
        this.mFemtoCellInfoRegistrants = new RegistrantList();
        this.mEmbmsSessionStatusNotificationRegistrant = new RegistrantList();
        this.mEmbmsAtInfoNotificationRegistrant = new RegistrantList();
        this.mPhbReadyRegistrants = new RegistrantList();
        this.mCallForwardingInfoRegistrants = new RegistrantList();
        this.mTxPowerRegistrant = new RegistrantList();
        this.mTxPowerStatusRegistrant = new RegistrantList();
        this.mCfuReturnValue = null;
        this.mMtkRilOp = null;
        this.mMtkRadioExProxyCookie = new AtomicLong(0);
        this.mDtmfReqQueue = new DtmfQueueHandler();
        this.mPlmnChangeNotificationRegistrant = new RegistrantList();
        this.mEmsrReturnValue = null;
        this.mEcopsReturnValue = null;
        this.mWPMonitor = new Object();
        this.mGmssRatChangedRegistrant = new RegistrantList();
        this.mResetAttachApnRegistrants = new RegistrantList();
        this.mAttachApnChangedRegistrants = new RegistrantList();
        this.mPcoDataAfterAttachedRegistrants = new RegistrantList();
        this.mRemoveRestrictEutranRegistrants = new RegistrantList();
        this.mMdDataRetryCountResetRegistrants = new RegistrantList();
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.mediatek.internal.telephony.MtkRIL.AnonymousClass1 */
            private static final int MODE_CDMA_ASSERT = 31;
            private static final int MODE_CDMA_RESET = 32;
            private static final int MODE_CDMA_RILD_NE = 103;
            private static final int MODE_GSM_RILD_NE = 101;
            private static final int MODE_PHONE_PROCESS_JE = 100;

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mtk.TEST_TRM")) {
                    int mode = intent.getIntExtra("mode", 2);
                    Rlog.d(MtkRIL.RILJ_LOG_TAG, "RIL received com.mtk.TEST_TRM, mode = " + mode + ", mInstanceIds = " + MtkRIL.this.mInstanceId);
                    if (mode != 100) {
                        MtkRIL.this.setTrm(mode, null);
                        return;
                    }
                    throw new RuntimeException("UserTriggerPhoneJE");
                }
                Rlog.w(MtkRIL.RILJ_LOG_TAG, "RIL received unexpected Intent: " + intent.getAction());
            }
        };
        this.hide_plmns = new ArrayList<>();
        this.mCsNetworkStateRegistrants = new RegistrantList();
        this.mSignalStrengthWithWcdmaEcioRegistrants = new RegistrantList();
        this.mVirtualSimOn = new RegistrantList();
        this.mVirtualSimOff = new RegistrantList();
        this.mImeiLockRegistrant = new RegistrantList();
        this.mImsiRefreshDoneRegistrant = new RegistrantList();
        this.mCardDetectedIndRegistrant = new RegistrantList();
        this.mRsuSimlockRegistrants = new RegistrantList();
        this.mInvalidSimInfoRegistrant = new RegistrantList();
        this.mNetworkEventRegistrants = new RegistrantList();
        this.mNetworkRejectRegistrants = new RegistrantList();
        this.mModulationRegistrants = new RegistrantList();
        this.mIsSmsReady = false;
        this.mSmsReadyRegistrants = new RegistrantList();
        this.mEspOrMeid = null;
        this.mIsCardDetected = false;
        this.mPsNetworkStateRegistrants = new RegistrantList();
        this.mNetworkInfoRegistrant = new RegistrantList();
        this.mDataAllowedRegistrants = new RegistrantList();
        this.mPseudoCellInfoRegistrants = new RegistrantList();
        this.mSimTrayPlugIn = new RegistrantList();
        this.mSimCommonSlotNoChanged = new RegistrantList();
        this.mBipProCmdRegistrant = new RegistrantList();
        this.mStkSetupMenuResetRegistrant = new RegistrantList();
        this.mLteAccessStratumStateRegistrants = new RegistrantList();
        this.mSimPlugIn = new RegistrantList();
        this.mSimPlugOut = new RegistrantList();
        this.mSimMissing = new RegistrantList();
        this.mSimRecovery = new RegistrantList();
        this.mSimPowerChanged = new RegistrantList();
        this.mSimPowerInfo = null;
        this.mSmlSlotLockInfoChanged = new RegistrantList();
        this.mSmlSlotLockInfo = null;
        this.mEconfSrvccRegistrants = new RegistrantList();
        this.mMccMncRegistrants = new RegistrantList();
        this.mVsimIndicationRegistrants = new RegistrantList();
        this.mDedicatedBearerActivedRegistrants = new RegistrantList();
        this.mDedicatedBearerModifiedRegistrants = new RegistrantList();
        this.mDedicatedBearerDeactivatedRegistrants = new RegistrantList();
        this.mDsbpStateRegistrant = new RegistrantList();
        this.mDsdaStateRegistrant = new RegistrantList();
        this.mQualifiedNetworkTypesRegistrant = new RegistrantList();
        this.mMobileDataUsageRegistrants = new RegistrantList();
        this.mNwLimitRegistrants = new RegistrantList();
        Rlog.d(RILJ_LOG_TAG, "constructor: sub = " + instanceId);
        this.mRilHandler = new MtkRilHandler();
        this.mMtkRadioExProxyDeathRecipient = new MtkRadioExProxyDeathRecipient();
        this.mMtkContext = context;
        this.mInstanceId = instanceId;
        this.mMtkRadioExResponse = new MtkRadioExResponse(this);
        this.mMtkRadioExIndication = new MtkRadioExIndication(this);
        if (instanceId.intValue() == 0) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.mtk.TEST_TRM");
            context.registerReceiver(this.mIntentReceiver, filter);
        }
        this.mMtkRilJIntiDone = true;
        getMtkRadioExProxy(null);
        getRilOp();
        this.hide_plmns.add("404999");
        this.hide_plmns.add("40548");
        this.hide_plmns.add("46020");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c6, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c7, code lost:
        r7.mRadioProxyMtk = null;
        android.telephony.Rlog.e(com.mediatek.internal.telephony.MtkRIL.RILJ_LOG_TAG, "MtkRadioExProxy getService/setResponseFunctions: " + r0);
     */
    public IMtkRadioEx getMtkRadioExProxy(Message result) {
        if (!this.mMtkRilJIntiDone) {
            return null;
        }
        if (!this.mIsMobileNetworkSupported) {
            mtkRiljLog("getMtkRadioExProxy:Not calling getService(): wifi-only");
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mRadioProxyMtk != null) {
            return this.mRadioProxyMtk;
        } else {
            this.mRadioProxyMtk = IMtkRadioEx.getService(HIDL_SERVICE_NAME_MTK[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()], false);
            if (this.mRadioProxyMtk != null) {
                this.mRadioProxyMtk.linkToDeath(this.mMtkRadioExProxyDeathRecipient, this.mMtkRadioExProxyCookie.incrementAndGet());
                this.mRadioProxyMtk.setResponseFunctionsMtk(this.mMtkRadioExResponse, this.mMtkRadioExIndication);
                if (this.mDtmfReqQueue != null) {
                    synchronized (this.mDtmfReqQueue) {
                        Rlog.d(RILJ_LOG_TAG, "queue size  " + this.mDtmfReqQueue.size());
                        for (int i = this.mDtmfReqQueue.size() - 1; i >= 0; i--) {
                            this.mDtmfReqQueue.remove(i);
                        }
                        if (this.mDtmfReqQueue.getPendingRequest() != null) {
                            Rlog.d(RILJ_LOG_TAG, "reset pending switch request");
                            RILRequest pendingRequest = this.mDtmfReqQueue.getPendingRequest().rr;
                            if (pendingRequest.mResult != null) {
                                AsyncResult.forMessage(pendingRequest.mResult, (Object) null, (Throwable) null);
                                pendingRequest.mResult.sendToTarget();
                            }
                            this.mDtmfReqQueue.resetSendChldRequest();
                            this.mDtmfReqQueue.setPendingRequest(null);
                        }
                    }
                }
            } else {
                Rlog.e(RILJ_LOG_TAG, "getMtkRadioExProxy: mRadioProxy == null");
            }
            if (this.mRadioProxyMtk == null) {
                if (result != null) {
                    AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                    result.sendToTarget();
                }
                this.mRilHandler.removeMessages(1006);
                this.mRilHandler.sendMessageDelayed(this.mRilHandler.obtainMessage(1006, Long.valueOf(this.mMtkRadioExProxyCookie.get())), 1000);
                riljLog("MtkRadioExProxy sendMessageDelayed");
            }
            return this.mRadioProxyMtk;
        }
    }

    /* access modifiers changed from: protected */
    public void handleMtkRadioProxyExceptionForRR(RILRequest rr, String caller, Exception e) {
        riljLoge(caller + ": " + e);
        clearRequestWithError(rr, 1);
        resetMtkProxyAndRequestList();
    }

    public IMtkRilOp getRilOp() {
        Rlog.d(RILJ_LOG_TAG, "getRilOp");
        IMtkRilOp iMtkRilOp = this.mMtkRilOp;
        if (iMtkRilOp != null) {
            return iMtkRilOp;
        }
        if ("0".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, "0"))) {
            Rlog.d(RILJ_LOG_TAG, "mMtkRilOp init fail, because OM load");
            return null;
        }
        try {
            Class<?> clazz = Class.forName("com.mediatek.opcommon.telephony.MtkRilOp");
            Rlog.d(RILJ_LOG_TAG, "class = " + clazz);
            Constructor clazzConstructfunc = clazz.getConstructor(Context.class, Integer.TYPE, Integer.TYPE, Integer.class);
            Rlog.d(RILJ_LOG_TAG, "constructor function = " + clazzConstructfunc);
            this.mMtkRilOp = (IMtkRilOp) clazzConstructfunc.newInstance(this.mContext, Integer.valueOf(this.mPreferredNetworkType), Integer.valueOf(this.mCdmaSubscription), this.mPhoneId);
            return this.mMtkRilOp;
        } catch (Exception e) {
            Rlog.d(RILJ_LOG_TAG, "mMtkRilOp init fail");
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void setResponseFunctions() {
        this.mMtkRadioResponse = new MtkRadioResponse(this);
        this.mMtkRadioIndication = new MtkRadioIndication(this);
        try {
            mtkRiljLog("override response functions");
            this.mRadioProxy.setResponseFunctions(this.mMtkRadioResponse, this.mMtkRadioIndication);
        } catch (RemoteException e) {
            mtkRiljLoge("override response function error, " + e);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isGetHidlServiceSync() {
        return false;
    }

    public MtkRadioResponse getMtkRadioResponse() {
        return this.mMtkRadioResponse;
    }

    public MtkRadioExResponse getMtkRadioExResponse() {
        return this.mMtkRadioExResponse;
    }

    public MtkRadioIndication getMtkRadioIndication() {
        return this.mMtkRadioIndication;
    }

    public MtkRadioExIndication getMtkRadioExIndication() {
        return this.mMtkRadioExIndication;
    }

    public void mtkRiljLog(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.d(RILJ_LOG_TAG, sb.toString());
    }

    public void mtkRiljLoge(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.e(RILJ_LOG_TAG, sb.toString());
    }

    private void clearRequestWithError(RILRequest rr, int error) {
        RadioResponseInfo responseInfo = new RadioResponseInfo();
        SparseArray<RILRequest> requestList = getRilRequestList();
        Rlog.e(RILJ_LOG_TAG, "clearRequestWithError target=" + rr.mRequest);
        synchronized (requestList) {
            requestList.remove(rr.mSerial);
        }
        responseInfo.type = 0;
        responseInfo.serial = rr.mSerial;
        responseInfo.error = error;
        processResponseDone(rr, responseInfo, null);
    }

    /* access modifiers changed from: protected */
    public void resetMtkProxyAndRequestList() {
        this.mRadioProxyMtk = null;
        this.mMtkRadioExProxyCookie.incrementAndGet();
        getMtkRadioExProxy(null);
    }

    public static String requestToStringEx(Integer request) {
        String msg;
        OemConstant.printStack("requestToStringEx");
        int intValue = request.intValue();
        if (intValue == 59) {
            msg = "OEM_HOOK_RAW";
        } else if (intValue == 60) {
            msg = "OEM_HOOK_STRINGS";
        } else if (intValue == 2062) {
            msg = "RIL_REQUEST_SYNC_DATA_SETTINGS_TO_MD";
        } else if (intValue != 2063) {
            switch (intValue) {
                case MtkGsmCdmaPhone.EVENT_IMS_UT_DONE:
                    msg = "RIL_REQUEST_RESUME_REGISTRATION";
                    break;
                case 2019:
                    msg = "HANGUP_ALL";
                    break;
                case 2028:
                    msg = "RIL_REQUEST_SET_TRM";
                    break;
                case 2030:
                    msg = "RIL_REQUEST_SET_ECC_LIST";
                    break;
                case 2035:
                    msg = "SET_ECC_MODE";
                    break;
                case 2036:
                    msg = "RIL_REQUEST_QUERY_PHB_STORAGE_INFO";
                    break;
                case 2037:
                    msg = "RIL_REQUEST_WRITE_PHB_ENTRY";
                    break;
                case 2038:
                    msg = "RIL_REQUEST_READ_PHB_ENTRY";
                    break;
                case 2039:
                    msg = "RIL_REQUEST_QUERY_UPB_CAPABILITY";
                    break;
                case 2040:
                    msg = "RIL_REQUEST_EDIT_UPB_ENTRY";
                    break;
                case 2041:
                    msg = "RIL_REQUEST_DELETE_UPB_ENTRY";
                    break;
                case 2042:
                    msg = "RIL_REQUEST_READ_UPB_GAS_LIST";
                    break;
                case 2043:
                    msg = "RIL_REQUEST_READ_UPB_GRP";
                    break;
                case 2044:
                    msg = "RIL_REQUEST_WRITE_UPB_GRP";
                    break;
                case 2045:
                    msg = "RIL_REQUEST_GET_PHB_STRING_LENGTH";
                    break;
                case 2046:
                    msg = "RIL_REQUEST_GET_PHB_MEM_STORAGE";
                    break;
                case 2047:
                    msg = "RIL_REQUEST_SET_PHB_MEM_STORAGE";
                    break;
                case MtkApnTypes.WAP /*{ENCODED_INT: 2048}*/:
                    msg = "RIL_REQUEST_READ_PHB_ENTRY_EXT";
                    break;
                case 2049:
                    msg = "RIL_REQUEST_WRITE_PHB_ENTRY_EXT";
                    break;
                case 2050:
                    msg = "RIL_REQUEST_QUERY_UPB_AVAILABLE";
                    break;
                case MtkCharacterSets.CP864:
                    msg = "RIL_REQUEST_READ_EMAIL_ENTRY";
                    break;
                case 2052:
                    msg = "RIL_REQUEST_READ_SNE_ENTRY";
                    break;
                case 2053:
                    msg = "RIL_REQUEST_READ_ANR_ENTRY";
                    break;
                case 2054:
                    msg = "RIL_REQUEST_READ_UPB_AAS_LIST";
                    break;
                case 2055:
                    msg = "REQUEST_GET_FEMTOCELL_LIST";
                    break;
                case 2056:
                    msg = "REQUEST_ABORT_FEMTOCELL_LIST";
                    break;
                case 2057:
                    msg = "REQUEST_SELECT_FEMTOCELL";
                    break;
                case 2058:
                    msg = "REQUEST_QUERY_FEMTOCELL_SYSTEM_SELECTION_MODE";
                    break;
                case 2059:
                    msg = "REQUEST_SET_FEMTOCELL_SYSTEM_SELECTION_MODE";
                    break;
                case 2060:
                    msg = "RIL_REQUEST_EMBMS_AT_CMD";
                    break;
                case 2130:
                    msg = "RIL_REQUEST_SET_SERVICE_STATE";
                    break;
                case 2133:
                    msg = "RIL_REQUEST_IMS_SEND_SMS_EX";
                    break;
                case 2157:
                    msg = "RIL_REQUEST_SET_PHONEBOOK_READY";
                    break;
                case 2158:
                    msg = "RIL_REQUEST_SET_TX_POWER_STATUS";
                    break;
                case 2159:
                    msg = "RIL_REQUEST_SETPROP_IMS_HANDOVER";
                    break;
                case 2168:
                    msg = "RIL_REQUEST_SET_SS_PROPERTY";
                    break;
                case 2171:
                    msg = "RIL_REQUEST_ENTER_DEVICE_NETWORK_DEPERSONALIZATION";
                    break;
                case 2173:
                    msg = "RIL_REQUEST_SET_VENDOR_SETTING";
                    break;
                case 2180:
                    msg = "RIL_REQUEST_SET_GWSD_MODE";
                    break;
                case 2181:
                    msg = "RIL_REQUEST_SET_GWSD_CALL_VALID";
                    break;
                case 2182:
                    msg = "RIL_REQUEST_SET_GWSD_IGNORE_CALL_INTERVAL";
                    break;
                case 2183:
                    msg = "HANGUP_WITH_REASON";
                    break;
                case 2184:
                    msg = "RIL_REQUEST_MODIFY_MODEM_TYPE";
                    break;
                case 2185:
                    msg = "RIL_REQUEST_ENABLE_DSDA_INDICATION";
                    break;
                case 2186:
                    msg = "RIL_REQUEST_GET_DSDA_STATUS";
                    break;
                case 2188:
                    msg = "RIL_REQUEST_IWLAN_REGISTER_CELLULAR_QUALITY_REPORT";
                    break;
                case 2189:
                    msg = "RIL_REQUEST_GET_SUGGESTED_PLMN_LIST";
                    break;
                case 2190:
                    msg = "RIL_REQUEST_CONFIG_A2_OFFSET";
                    break;
                case 2191:
                    msg = "RIL_REQUEST_CONFIG_B1_OFFSET";
                    break;
                case 2192:
                    msg = "RIL_REQUEST_ENABLE_SCG_FAILURE";
                    break;
                case 2193:
                    msg = "RIL_REQUEST_DISABLE_NR";
                    break;
                case 2194:
                    msg = "RIL_REQUEST_SET_TX_POWER";
                    break;
                case 2195:
                    msg = "RIL_REQUEST_SEARCH_STORED_FREQUENCY_INFO";
                    break;
                case 2196:
                    msg = "RIL_REQUEST_SEARCH_RAT";
                    break;
                case 2197:
                    msg = "RIL_REQUEST_SET_BACKGROUND_SEARCH_TIMER";
                    break;
                case 2199:
                    msg = "RIL_REQUEST_SET_GWSD_KEEP_ALIVE_PDCP";
                    break;
                case 2200:
                    msg = "RIL_REQUEST_SET_GWSD_KEEP_ALIVE_IPDATA";
                    break;
                case 2201:
                    msg = "RIL_REQUEST_SEND_SAR_INDICATOR";
                    break;
                default:
                    switch (intValue) {
                        case ExternalSimConstants.MSG_ID_CAPABILITY_SWITCH_DONE /*{ENCODED_INT: 2002}*/:
                            msg = "RIL_REQUEST_SET_SIM_POWER";
                            break;
                        case 2003:
                            msg = "RIL_REQUEST_MODEM_POWERON";
                            break;
                        case MtkGsmCdmaPhone.EVENT_GET_CLIR_COMPLETE:
                            msg = "RIL_REQUEST_MODEM_POWEROFF";
                            break;
                        case MtkGsmCdmaPhone.EVENT_SET_CALL_BARRING_COMPLETE:
                            msg = "SET_NETWORK_SELECTION_MANUAL_WITH_ACT";
                            break;
                        case MtkGsmCdmaPhone.EVENT_GET_CALL_BARRING_COMPLETE:
                            msg = "QUERY_AVAILABLE_NETWORKS_WITH_ACT";
                            break;
                        case 2007:
                            msg = "ABORT_QUERY_AVAILABLE_NETWORKS";
                            break;
                        default:
                            switch (intValue) {
                                case 2009:
                                    msg = "RIL_REQUEST_GSM_SET_BROADCAST_LANGUAGE";
                                    break;
                                case 2010:
                                    msg = "RIL_REQUEST_GSM_GET_BROADCAST_LANGUAGE";
                                    break;
                                case 2011:
                                    msg = "RIL_REQUEST_GET_SMS_SIM_MEM_STATUS";
                                    break;
                                case 2012:
                                    msg = "RIL_REQUEST_GET_SMS_PARAMS";
                                    break;
                                case 2013:
                                    msg = "RIL_REQUEST_SET_SMS_PARAMS";
                                    break;
                                case 2014:
                                    msg = "RIL_REQUEST_SET_ETWS";
                                    break;
                                case 2015:
                                    msg = "RIL_REQUEST_REMOVE_CB_MESSAGE";
                                    break;
                                case 2016:
                                    msg = "SET_CALL_INDICATION";
                                    break;
                                default:
                                    switch (intValue) {
                                        case 2021:
                                            msg = "RIL_REQUEST_SET_PSEUDO_CELL_MODE";
                                            break;
                                        case 2022:
                                            msg = "RIL_REQUEST_GET_PSEUDO_CELL_INFO";
                                            break;
                                        case 2023:
                                            msg = "RIL_REQUEST_SWITCH_MODE_FOR_ECC";
                                            break;
                                        case 2024:
                                            msg = "RIL_REQUEST_GET_SMS_RUIM_MEM_STATUS";
                                            break;
                                        case MtkCharacterSets.GB_2312:
                                            msg = "RIL_REQUEST_SET_FD_MODE";
                                            break;
                                        default:
                                            switch (intValue) {
                                                case 2065:
                                                    msg = "RIL_REQUEST_SET_LTE_ACCESS_STRATUM_REPORT";
                                                    break;
                                                case 2066:
                                                    msg = "RIL_REQUEST_SET_LTE_UPLINK_DATA_TRANSFER";
                                                    break;
                                                case 2067:
                                                    msg = "RIL_REQUEST_QUERY_SIM_NETWORK_LOCK";
                                                    break;
                                                case 2068:
                                                    msg = "RIL_REQUEST_SET_SIM_NETWORK_LOCK";
                                                    break;
                                                default:
                                                    switch (intValue) {
                                                        case 2100:
                                                            msg = "RIL_REQUEST_SET_REMOVE_RESTRICT_EUTRAN_MODE";
                                                            break;
                                                        case MtkCharacterSets.BIG5_HKSCS:
                                                            msg = "RIL_REQUEST_VSS_ANTENNA_CONF";
                                                            break;
                                                        case 2102:
                                                            msg = "RIL_REQUEST_VSS_ANTENNA_INFO";
                                                            break;
                                                        case 2103:
                                                            msg = "RIL_REQUEST_SET_CLIP";
                                                            break;
                                                        case 2104:
                                                            msg = "RIL_REQUEST_GET_COLP";
                                                            break;
                                                        case 2105:
                                                            msg = "RIL_REQUEST_GET_COLR";
                                                            break;
                                                        case 2106:
                                                            msg = "RIL_REQUEST_SEND_CNAP";
                                                            break;
                                                        case 2107:
                                                            msg = "RIL_REQUEST_GET_POL_CAPABILITY";
                                                            break;
                                                        case 2108:
                                                            msg = "RIL_REQUEST_GET_POL_LIST";
                                                            break;
                                                        case 2109:
                                                            msg = "RIL_REQUEST_SET_POL_ENTRY";
                                                            break;
                                                        case 2110:
                                                            msg = "ECC_PREFERRED_RAT";
                                                            break;
                                                        case 2111:
                                                            msg = "SET_ROAMING_ENABLE";
                                                            break;
                                                        case 2112:
                                                            msg = "GET_ROAMING_ENABLE";
                                                            break;
                                                        case 2113:
                                                            msg = "RIL_REQUEST_VSIM_NOTIFICATION";
                                                            break;
                                                        case 2114:
                                                            msg = "RIL_REQUEST_VSIM_OPERATION";
                                                            break;
                                                        case 2115:
                                                            msg = "RIL_REQUEST_GET_GSM_SMS_BROADCAST_ACTIVATION";
                                                            break;
                                                        case 2116:
                                                            msg = "RIL_REQUEST_SET_WIFI_ENABLED";
                                                            break;
                                                        case 2117:
                                                            msg = "RIL_REQUEST_SET_WIFI_ASSOCIATED";
                                                            break;
                                                        case 2118:
                                                            msg = "RIL_REQUEST_SET_WIFI_SIGNAL_LEVEL";
                                                            break;
                                                        case 2119:
                                                            msg = "RIL_REQUEST_SET_WIFI_IP_ADDRESS";
                                                            break;
                                                        case 2120:
                                                            msg = "RIL_REQUEST_SET_GEO_LOCATION";
                                                            break;
                                                        case 2121:
                                                            msg = "RIL_REQUEST_SET_EMERGENCY_ADDRESS_ID";
                                                            break;
                                                        default:
                                                            switch (intValue) {
                                                                case 2123:
                                                                    msg = "RIL_REQUEST_SET_COLP";
                                                                    break;
                                                                case 2124:
                                                                    msg = "RIL_REQUEST_SET_COLR";
                                                                    break;
                                                                case 2125:
                                                                    msg = "RIL_REQUEST_QUERY_CALL_FORWARD_IN_TIME_SLOT";
                                                                    break;
                                                                case 2126:
                                                                    msg = "RIL_REQUEST_SET_CALL_FORWARD_IN_TIME_SLOT";
                                                                    break;
                                                                default:
                                                                    switch (intValue) {
                                                                        case 2144:
                                                                            msg = "RIL_REQUEST_DATA_CONNECTION_ATTACH";
                                                                            break;
                                                                        case 2145:
                                                                            msg = "RIL_REQUEST_DATA_CONNECTION_DETACH";
                                                                            break;
                                                                        case 2146:
                                                                            msg = "RIL_REQUEST_RESET_ALL_CONNECTIONS";
                                                                            break;
                                                                        case 2147:
                                                                            msg = "RIL_REQUEST_SET_VOICE_PREFER_STATUS";
                                                                            break;
                                                                        case 2148:
                                                                            msg = "RIL_REQUEST_SET_ECC_NUM";
                                                                            break;
                                                                        case 2149:
                                                                            msg = "RIL_REQUEST_GET_ECC_NUM";
                                                                            break;
                                                                        case 2150:
                                                                            msg = "RIL_REQUEST_RESTART_RILD";
                                                                            break;
                                                                        case 2151:
                                                                            msg = "RIL_REQUEST_SET_LTE_RELEASE_VERSION";
                                                                            break;
                                                                        case 2152:
                                                                            msg = "RIL_REQUEST_GET_LTE_RELEASE_VERSION";
                                                                            break;
                                                                        case 2153:
                                                                            msg = "RIL_REQUEST_SIGNAL_STRENGTH_WITH_WCDMA_ECIO";
                                                                            break;
                                                                        default:
                                                                            msg = "<unknown request> " + request;
                                                                            break;
                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
            }
        } else {
            msg = "RIL_REQUEST_RESET_MD_DATA_RETRY_COUNT";
        }
        return "MTK: " + msg;
    }

    public static String responseToStringEx(Integer request) {
        String msg;
        int intValue = request.intValue();
        if (intValue == 3000) {
            msg = "RIL_UNSOL_RESPONSE_PLMN_CHANGED";
        } else if (intValue == 3001) {
            msg = "RIL_UNSOL_RESPONSE_REGISTRATION_SUSPENDED";
        } else if (intValue == 3059) {
            msg = "RIL_UNSOL_MD_DATA_RETRY_COUNT_RESET";
        } else if (intValue == 3060) {
            msg = "RIL_UNSOL_REMOVE_RESTRICT_EUTRAN";
        } else if (intValue == 3114) {
            msg = "RIL_UNSOL_DSBP_STATE_CHANGED";
        } else if (intValue != 3115) {
            switch (intValue) {
                case 1028:
                    msg = "UNSOL_OEM_HOOK_RAW";
                    break;
                case 3003:
                    msg = "RIL_UNSOL_GMSS_RAT_CHANGED";
                    break;
                case 3014:
                    msg = "RIL_UNSOL_DATA_ALLOWED";
                    break;
                case 3015:
                    msg = "UNSOL_INCOMING_CALL_INDICATION";
                    break;
                case 3016:
                    msg = "RIL_UNSOL_INVALID_SIM";
                    break;
                case 3017:
                    msg = "RIL_UNSOL_PSEUDO_CELL_INFO";
                    break;
                case 3018:
                    msg = "RIL_UNSOL_NETWORK_EVENT";
                    break;
                case 3019:
                    msg = "RIL_UNSOL_MODULATION_INFO";
                    break;
                case 3020:
                    msg = "RIL_UNSOL_RESET_ATTACH_APN";
                    break;
                case 3021:
                    msg = "RIL_UNSOL_DATA_ATTACH_APN_CHANGED";
                    break;
                case 3022:
                    msg = "RIL_UNSOL_WORLD_MODE_CHANGED";
                    break;
                case 3023:
                    msg = "RIL_UNSOL_CDMA_CARD_INITIAL_ESN_OR_MEID";
                    break;
                case 3024:
                    msg = "UNSOL_CIPHER_INDICATION";
                    break;
                case 3025:
                    msg = "UNSOL_CRSS_NOTIFICATION";
                    break;
                case 3026:
                    msg = "UNSOL_SUPP_SVC_NOTIFICATION_EX";
                    break;
                case 3062:
                    msg = "RIL_UNSOL_LTE_ACCESS_STRATUM_STATE_CHANGE";
                    break;
                case 3070:
                    msg = "UNSOL_CALL_FORWARDING";
                    break;
                case 3072:
                    msg = "RIL_UNSOL_ECONF_SRVCC_INDICATION";
                    break;
                case 3086:
                    msg = "RIL_UNSOL_NATT_KEEP_ALIVE_CHANGED";
                    break;
                case 3088:
                    msg = "RIL_UNSOL_WIFI_PDN_OOS";
                    break;
                case 3109:
                    msg = "RIL_UNSOL_NETWORK_REJECT_CAUSE";
                    break;
                case 3201:
                    msg = "RIL_UNSOL_SML_DEVICE_LOCK_INFO_NOTIFY";
                    break;
                case 3202:
                    msg = "RIL_UNSOL_ENCRYPTED_SERIAL_ID_UPDATED";
                    break;
                default:
                    switch (intValue) {
                        case 3028:
                            msg = "UNSOL_PHB_READY_NOTIFICATION";
                            break;
                        case 3029:
                            msg = "UNSOL_FEMTOCELL_INFO";
                            break;
                        case 3030:
                            msg = "UNSOL_NETWORK_INFO";
                            break;
                        default:
                            switch (intValue) {
                                case 3053:
                                    msg = "RIL_UNSOL_PCO_DATA_AFTER_ATTACHED";
                                    break;
                                case 3054:
                                    msg = "RIL_UNSOL_EMBMS_SESSION_STATUS";
                                    break;
                                case 3055:
                                    msg = "RIL_UNSOL_EMBMS_AT_INFO";
                                    break;
                                default:
                                    switch (intValue) {
                                        case 3074:
                                            msg = "RIL_UNSOL_VSIM_OPERATION_INDICATION";
                                            break;
                                        case 3075:
                                            msg = "RIL_UNSOL_MOBILE_WIFI_ROVEOUT";
                                            break;
                                        case 3076:
                                            msg = "RIL_UNSOL_MOBILE_WIFI_HANDOVER";
                                            break;
                                        case 3077:
                                            msg = "RIL_UNSOL_ACTIVE_WIFI_PDN_COUNT";
                                            break;
                                        case 3078:
                                            msg = "RIL_UNSOL_WIFI_RSSI_MONITORING_CONFIG";
                                            break;
                                        case 3079:
                                            msg = "RIL_UNSOL_WIFI_PDN_ERROR";
                                            break;
                                        case 3080:
                                            msg = "RIL_UNSOL_REQUEST_GEO_LOCATION";
                                            break;
                                        case 3081:
                                            msg = "RIL_UNSOL_WFC_PDN_STATE";
                                            break;
                                        case 3082:
                                            return "RIL_UNSOL_DEDICATE_BEARER_ACTIVATED";
                                        case 3083:
                                            return "RIL_UNSOL_DEDICATE_BEARER_MODIFIED";
                                        case 3084:
                                            return "RIL_UNSOL_DEDICATE_BEARER_DEACTIVATED";
                                        default:
                                            switch (intValue) {
                                                case 3095:
                                                    msg = "RIL_UNSOL_ECC_NUM";
                                                    break;
                                                case 3096:
                                                    msg = "RIL_UNSOL_MCCMNC_CHANGED";
                                                    break;
                                                case 3097:
                                                    msg = "UNSOL_SIGNAL_STRENGTH_WITH_WCDMA_ECIO";
                                                    break;
                                                default:
                                                    switch (intValue) {
                                                        case 3124:
                                                            msg = "RIL_UNSOL_SIM_POWER_CHANGED";
                                                            break;
                                                        case 3125:
                                                            msg = "RIL_UNSOL_CARD_DETECTED_IND";
                                                            break;
                                                        case 3126:
                                                            msg = "UNSOL_CALL_ADDITIONAL_INFO";
                                                            break;
                                                        default:
                                                            switch (intValue) {
                                                                case 3130:
                                                                    msg = "RIL_UNSOL_QUALIFIED_NETWORK_TYPES_CHANGED";
                                                                    break;
                                                                case 3131:
                                                                    msg = "RIL_UNSOL_ON_DSDA_CHANGED";
                                                                    break;
                                                                case 3132:
                                                                    msg = "RIL_UNSOL_IWLAN_CELLULAR_QUALITY_CHANGED_IND";
                                                                    break;
                                                                case 3133:
                                                                    msg = "RIL_UNSOL_MOBILE_DATA_USAGE";
                                                                    break;
                                                                case 3134:
                                                                    msg = "RIL_UNSOL_NW_LIMIT";
                                                                    break;
                                                                default:
                                                                    msg = "<unknown response>";
                                                                    break;
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
            }
        } else {
            msg = "RIL_UNSOL_SIM_SLOT_LOCK_POLICY_NOTIFY";
        }
        return "MTK: " + msg;
    }

    public void registerForCsNetworkStateChanged(Handler h, int what, Object obj) {
        this.mCsNetworkStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCsNetworkStateChanged(Handler h) {
        this.mCsNetworkStateRegistrants.remove(h);
    }

    public void registerForSignalStrengthWithWcdmaEcioChanged(Handler h, int what, Object obj) {
        this.mSignalStrengthWithWcdmaEcioRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForignalStrengthWithWcdmaEcioChanged(Handler h) {
        this.mSignalStrengthWithWcdmaEcioRegistrants.remove(h);
    }

    protected static String retToString(int req, Object ret) {
        return RIL.retToString(req, ret);
    }

    public void setTrm(int mode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2028, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setTrm(rr.mSerial, mode);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setTrm", e);
            }
        }
    }

    public void getATR(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2001, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getATR(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getATR", e);
            }
        }
    }

    public void getIccid(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2142, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getIccid(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getIccid", e);
            }
        }
    }

    public void setSimPower(int mode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(ExternalSimConstants.MSG_ID_CAPABILITY_SWITCH_DONE, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setSimPower(rr.mSerial, mode);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setSimPower", e);
            }
        }
    }

    public void registerForVirtualSimOn(Handler h, int what, Object obj) {
        this.mVirtualSimOn.add(new Registrant(h, what, obj));
    }

    public void unregisterForVirtualSimOn(Handler h) {
        this.mVirtualSimOn.remove(h);
    }

    public void registerForVirtualSimOff(Handler h, int what, Object obj) {
        this.mVirtualSimOff.add(new Registrant(h, what, obj));
    }

    public void unregisterForVirtualSimOff(Handler h) {
        this.mVirtualSimOff.remove(h);
    }

    public void registerForIMEILock(Handler h, int what, Object obj) {
        this.mImeiLockRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForIMEILock(Handler h) {
        this.mImeiLockRegistrant.remove(h);
    }

    public void registerForImsiRefreshDone(Handler h, int what, Object obj) {
        this.mImsiRefreshDoneRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsiRefreshDone(Handler h) {
        this.mImsiRefreshDoneRegistrant.remove(h);
    }

    public void registerForRsuSimLockChanged(Handler h, int what, Object obj) {
        this.mRsuSimlockRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRsuSimLockChanged(Handler h) {
        this.mRsuSimlockRegistrants.remove(h);
    }

    public void registerForCardDetectedInd(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mCardDetectedIndRegistrant.add(r);
        if (this.mIsCardDetected) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForCardDetectedInd(Handler h) {
        this.mCardDetectedIndRegistrant.remove(h);
    }

    private SimAuthStructure convertToHalSimAuthStructure(int sessionId, int mode, int tag, String param1, String param2) {
        String str;
        String str2;
        String str3;
        SimAuthStructure simAuth = new SimAuthStructure();
        simAuth.sessionId = sessionId;
        simAuth.mode = mode;
        String str4 = "0";
        if (param1 == null || param1.length() <= 0) {
            simAuth.param1 = convertNullToEmptyString(param1);
        } else {
            String length = Integer.toHexString(param1.length() / 2);
            StringBuilder sb = new StringBuilder();
            if (length.length() % 2 == 1) {
                str2 = str4;
            } else {
                str2 = "";
            }
            sb.append(str2);
            sb.append(length);
            String length2 = sb.toString();
            if (sessionId == 0) {
                str3 = param1;
            } else {
                str3 = length2 + param1;
            }
            simAuth.param1 = convertNullToEmptyString(str3);
        }
        if (param2 == null || param2.length() <= 0) {
            simAuth.param2 = convertNullToEmptyString(param2);
        } else {
            String length3 = Integer.toHexString(param2.length() / 2);
            StringBuilder sb2 = new StringBuilder();
            if (length3.length() % 2 != 1) {
                str4 = "";
            }
            sb2.append(str4);
            sb2.append(length3);
            String length4 = sb2.toString();
            if (sessionId == 0) {
                str = param2;
            } else {
                str = length4 + param2;
            }
            simAuth.param2 = convertNullToEmptyString(str);
        }
        if (mode == 1) {
            simAuth.tag = tag;
        }
        return simAuth;
    }

    public void doGeneralSimAuthentication(int sessionId, int mode, int tag, String param1, String param2, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2064, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.doGeneralSimAuthentication(rr.mSerial, convertToHalSimAuthStructure(sessionId, mode, tag, param1, param2));
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "doGeneralSimAuthentication", e);
            }
        }
    }

    public String lookupOperatorNameEons(int subId, String numeric, boolean desireLongName, int nLac) {
        String str;
        MtkSIMRecords simRecord = UiccController.getInstance().getIccRecords(this.mInstanceId.intValue(), 1);
        String sEons = null;
        StringBuilder lac_sb = new StringBuilder(Integer.toHexString(nLac));
        if (lac_sb.length() == 1 || lac_sb.length() == 2) {
            lac_sb.setCharAt(0, '*');
        } else {
            for (int i = 0; i < lac_sb.length() / 2; i++) {
                lac_sb.setCharAt(i, '*');
            }
        }
        Rlog.d(RILJ_LOG_TAG, "subId=" + subId + " numeric=" + numeric + " desireLongName=" + desireLongName + " nLac=" + lac_sb.toString());
        if (this.mPhoneType == 1) {
            if (nLac == 65534 || nLac == -1) {
                nLac = 0;
            }
            if (simRecord != null) {
                try {
                    str = simRecord.getEonsIfExist(numeric, nLac, desireLongName);
                } catch (RuntimeException ex) {
                    Rlog.e(RILJ_LOG_TAG, "Exception while getEonsIfExist. " + ex);
                }
            } else {
                str = null;
            }
            sEons = str;
            if (sEons == null || sEons.equals("")) {
                String mSimOperatorNumeric = simRecord != null ? simRecord.getOperatorNumeric() : null;
                if (mSimOperatorNumeric != null && mSimOperatorNumeric.equals(numeric)) {
                    String sCphsOns = simRecord != null ? simRecord.getSIMCPHSOns() : null;
                    if (!TextUtils.isEmpty(sCphsOns)) {
                        Rlog.d(RILJ_LOG_TAG, "plmn name update to CPHS Ons: " + sCphsOns);
                        return sCphsOns.trim();
                    }
                    String mSpn = simRecord != null ? simRecord.getServiceProviderName() : null;
                    if (!TextUtils.isEmpty(mSpn) && "63903".equals(numeric) && "Equitel".equals(mSpn)) {
                        Rlog.d(RILJ_LOG_TAG, "plmn name update to ServiceProviderName: " + mSpn);
                        return mSpn;
                    }
                }
            } else {
                Rlog.d(RILJ_LOG_TAG, "plmn name update to Eons: " + sEons);
                return sEons;
            }
        }
        return null;
    }

    public String lookupOperatorNameNitz(int subId, String numeric, boolean desireLongName) {
        String nitzOperatorName;
        int phoneId = SubscriptionManager.getPhoneId(subId);
        String nitzOperatorNumeric = TelephonyManager.getTelephonyProperty(phoneId, "persist.vendor.radio.nitz_oper_code", "");
        if (numeric == null || !numeric.equals(nitzOperatorNumeric)) {
            return null;
        }
        if (desireLongName) {
            nitzOperatorName = TelephonyManager.getTelephonyProperty(phoneId, "persist.vendor.radio.nitz_oper_lname", "");
        } else {
            nitzOperatorName = TelephonyManager.getTelephonyProperty(phoneId, "persist.vendor.radio.nitz_oper_sname", "");
        }
        if (nitzOperatorName != null && nitzOperatorName.startsWith("uCs2")) {
            Rlog.d(RILJ_LOG_TAG, "lookupOperatorName() handling UCS2 format name");
            try {
                nitzOperatorName = new String(IccUtils.hexStringToBytes(nitzOperatorName.substring(4)), "UTF-16");
            } catch (UnsupportedEncodingException e) {
                Rlog.d(RILJ_LOG_TAG, "lookupOperatorName() UnsupportedEncodingException");
            }
        }
        if (TextUtils.isEmpty(nitzOperatorName)) {
            return null;
        }
        Rlog.d(RILJ_LOG_TAG, "plmn name update to Nitz: " + nitzOperatorName);
        return nitzOperatorName;
    }

    public String lookupOperatorNameMVNO(int subId, String numeric, boolean desireLongName) {
        String operatorName = null;
        if (numeric != null) {
            operatorName = MtkServiceStateTracker.lookupOperatorName(this.mMtkContext, subId, numeric, desireLongName);
            if (!TextUtils.isEmpty(operatorName)) {
                Rlog.d(RILJ_LOG_TAG, "plmn name update to TS.25/MVNO: " + operatorName);
            }
        }
        return operatorName;
    }

    public String lookupOperatorName(int subId, String numeric, boolean desireLongName, int nLac) {
        String operatorName = lookupOperatorNameEons(subId, numeric, desireLongName, nLac);
        if (!TextUtils.isEmpty(operatorName)) {
            return operatorName;
        }
        String operatorName2 = lookupOperatorNameNitz(subId, numeric, desireLongName);
        if (!TextUtils.isEmpty(operatorName2)) {
            return operatorName2;
        }
        String operatorName3 = lookupOperatorNameMVNO(subId, numeric, desireLongName);
        if (!TextUtils.isEmpty(operatorName3)) {
            return operatorName3;
        }
        return null;
    }

    public void setNetworkSelectionModeManualWithAct(String operatorNumeric, String act, int mode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkGsmCdmaPhone.EVENT_SET_CALL_BARRING_COMPLETE, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " operatorNumeric = " + operatorNumeric);
            try {
                radioProxy.setNetworkSelectionModeManualWithAct(rr.mSerial, convertNullToEmptyString(operatorNumeric), convertNullToEmptyString(act), Integer.toString(mode));
            } catch (RemoteException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setNetworkSelectionModeManual", e);
            }
        }
    }

    public boolean hidePLMN(String mccmnc) {
        Iterator<String> it = this.hide_plmns.iterator();
        while (it.hasNext()) {
            if (it.next().equals(mccmnc)) {
                return true;
            }
        }
        return false;
    }

    public void getAvailableNetworksWithAct(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkGsmCdmaPhone.EVENT_GET_CALL_BARRING_COMPLETE, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getAvailableNetworksWithAct(rr.mSerial);
            } catch (RemoteException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getAvailableNetworks", e);
            }
        }
    }

    public void cancelAvailableNetworks(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2007, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.cancelAvailableNetworks(rr.mSerial);
            } catch (RemoteException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getAvailableNetworks", e);
            }
        }
    }

    public void getFemtoCellList(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2055, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getFemtocellList(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getFemtoCellList", e);
            }
        }
    }

    public void abortFemtoCellList(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2056, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.abortFemtocellList(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "abortFemtoCellList", e);
            }
        }
    }

    public void selectFemtoCell(FemtoCellInfo femtocell, Message result) {
        int act;
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2057, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            int act2 = femtocell.getCsgRat();
            if (act2 == 14) {
                act = 7;
            } else if (act2 == 3) {
                act = 2;
            } else {
                act = 0;
            }
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " csgId=" + femtocell.getCsgId() + " plmn=" + femtocell.getOperatorNumeric() + " rat=" + femtocell.getCsgRat() + " act=" + act);
            try {
                radioProxy.selectFemtocell(rr.mSerial, convertNullToEmptyString(femtocell.getOperatorNumeric()), convertNullToEmptyString(Integer.toString(act)), convertNullToEmptyString(Integer.toString(femtocell.getCsgId())));
            } catch (Exception e) {
                handleMtkRadioProxyExceptionForRR(rr, "selectFemtoCell", e);
            }
        }
    }

    public void queryFemtoCellSystemSelectionMode(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2058, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.queryFemtoCellSystemSelectionMode(rr.mSerial);
            } catch (Exception e) {
                handleMtkRadioProxyExceptionForRR(rr, "queryFemtoCellSystemSelectionMode", e);
            }
        }
    }

    public void setFemtoCellSystemSelectionMode(int mode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2059, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " mode=" + mode);
            try {
                radioProxy.setFemtoCellSystemSelectionMode(rr.mSerial, mode);
            } catch (Exception e) {
                handleMtkRadioProxyExceptionForRR(rr, "setFemtoCellSystemSelectionMode", e);
            }
        }
    }

    public void getSignalStrengthWithWcdmaEcio(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2153, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getSignalStrengthWithWcdmaEcio(rr.mSerial);
            } catch (Exception e) {
                handleMtkRadioProxyExceptionForRR(rr, "getSignalStrength", e);
            }
        }
    }

    public void setModemPower(boolean isOn, Message result) {
        RILRequest rr;
        mtkRiljLog("Set Modem power as: " + isOn);
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            if (isOn) {
                rr = obtainRequest(2003, result, this.mRILDefaultWorkSource);
            } else {
                rr = obtainRequest(MtkGsmCdmaPhone.EVENT_GET_CLIR_COMPLETE, result, this.mRILDefaultWorkSource);
            }
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + isOn);
            try {
                radioProxy.setModemPower(rr.mSerial, isOn);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setModemPower", e);
            }
        }
    }

    public void setInvalidSimInfo(Handler h, int what, Object obj) {
        this.mInvalidSimInfoRegistrant.add(new Registrant(h, what, obj));
    }

    public void unSetInvalidSimInfo(Handler h) {
        this.mInvalidSimInfoRegistrant.remove(h);
    }

    public void registerForNetworkEvent(Handler h, int what, Object obj) {
        this.mNetworkEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkEvent(Handler h) {
        this.mNetworkEventRegistrants.remove(h);
    }

    public void registerForNetworkReject(Handler h, int what, Object obj) {
        this.mNetworkRejectRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkReject(Handler h) {
        this.mNetworkRejectRegistrants.remove(h);
    }

    public void registerForModulation(Handler h, int what, Object obj) {
        IMtkRilOp rilOp = getRilOp();
        if (rilOp != null) {
            rilOp.registerForModulation(h, what, obj);
        }
    }

    public void unregisterForModulation(Handler h) {
        IMtkRilOp rilOp = getRilOp();
        if (rilOp != null) {
            rilOp.unregisterForModulation(h);
        }
    }

    public void registerForFemtoCellInfo(Handler h, int what, Object obj) {
        this.mFemtoCellInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForFemtoCellInfo(Handler h) {
        this.mFemtoCellInfoRegistrants.remove(h);
    }

    public void registerForSmsReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mSmsReadyRegistrants.add(r);
        if (this.mIsSmsReady) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForSmsReady(Handler h) {
        this.mSmsReadyRegistrants.remove(h);
    }

    public void setOnMeSmsFull(Handler h, int what, Object obj) {
        this.mMeSmsFullRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnMeSmsFull(Handler h) {
        this.mMeSmsFullRegistrant.clear();
    }

    public void setOnEtwsNotification(Handler h, int what, Object obj) {
        this.mEtwsNotificationRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnEtwsNotification(Handler h) {
        this.mEtwsNotificationRegistrant.clear();
    }

    public void getSmsParameters(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2012, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getSmsParameters(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getSmsParameters", e);
            }
        }
    }

    public void setSmsParameters(MtkSmsParameters params, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2013, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            SmsParams smsp = new SmsParams();
            smsp.dcs = params.dcs;
            smsp.format = params.format;
            smsp.pid = params.pid;
            smsp.vp = params.vp;
            try {
                radioProxy.setSmsParameters(rr.mSerial, smsp);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setSmsParameters", e);
            }
        }
    }

    public void setEtws(int mode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2014, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setEtws(rr.mSerial, mode);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setEtws", e);
            }
        }
    }

    public void removeCellBroadcastMsg(int channelId, int serialId, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2015, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + channelId + ", " + serialId);
            try {
                radioProxy.removeCbMsg(rr.mSerial, channelId, serialId);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "removeCellBroadcastMsg", e);
            }
        }
    }

    public void getSmsSimMemoryStatus(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2011, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getSmsMemStatus(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getSmsSimMemoryStatus", e);
            }
        }
    }

    public void setGsmBroadcastLangs(String lang, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2009, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", lang:" + lang);
            try {
                radioProxy.setGsmBroadcastLangs(rr.mSerial, lang);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setGsmBroadcastLangs", e);
            }
        }
    }

    public void getGsmBroadcastLangs(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2010, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getGsmBroadcastLangs(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getGsmBroadcastLangs", e);
            }
        }
    }

    public void getGsmBroadcastActivation(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2115, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getGsmBroadcastActivation(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getGsmBroadcastActivation", e);
            }
        }
    }

    public void setCDMACardInitalEsnMeid(Handler h, int what, Object obj) {
        this.mCDMACardEsnMeidRegistrant = new Registrant(h, what, obj);
        Object obj2 = this.mEspOrMeid;
        if (obj2 != null) {
            this.mCDMACardEsnMeidRegistrant.notifyRegistrant(new AsyncResult((Object) null, obj2, (Throwable) null));
        }
    }

    public void registerForPsNetworkStateChanged(Handler h, int what, Object obj) {
        this.mPsNetworkStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForPsNetworkStateChanged(Handler h) {
        this.mPsNetworkStateRegistrants.remove(h);
    }

    public void registerForNetworkInfo(Handler h, int what, Object obj) {
        this.mNetworkInfoRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkInfo(Handler h) {
        this.mNetworkInfoRegistrant.remove(h);
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, String newCfm, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(44, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "facility = " + facility);
            try {
                radioProxy.setBarringPasswordCheckedByNW(rr.mSerial, convertNullToEmptyString(facility), convertNullToEmptyString(oldPwd), convertNullToEmptyString(newPwd), convertNullToEmptyString(newCfm));
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "changeBarringPasswordCheckedByNW", e);
            }
        }
    }

    public void setCLIP(int clipEnable, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2103, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " clipEnable = " + clipEnable);
            try {
                radioProxy.setClip(rr.mSerial, clipEnable);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCLIP", e);
            }
        }
    }

    public void getCOLP(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2104, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getColp(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getCOLP", e);
            }
        }
    }

    public void getCOLR(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2105, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getColr(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getCOLR", e);
            }
        }
    }

    public void sendCNAP(String cnapssMessage, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2106, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "CNAP string = " + cnapssMessage);
            try {
                radioProxy.sendCnap(rr.mSerial, cnapssMessage);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "sendCNAP", e);
            }
        }
    }

    public void setCOLR(int colrEnable, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2124, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " colrEnable = " + colrEnable);
            try {
                radioProxy.setColr(rr.mSerial, colrEnable);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCOLR", e);
            }
        }
    }

    public void setCOLP(int colpEnable, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2123, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " colpEnable = " + colpEnable);
            try {
                radioProxy.setColp(rr.mSerial, colpEnable);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCOLP", e);
            }
        }
    }

    public void queryCallForwardInTimeSlotStatus(int cfReason, int serviceClass, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2125, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " cfreason = " + cfReason + " serviceClass = " + serviceClass);
            CallForwardInfoEx cfInfoEx = new CallForwardInfoEx();
            cfInfoEx.reason = cfReason;
            cfInfoEx.serviceClass = serviceClass;
            cfInfoEx.toa = PhoneNumberUtils.toaFromString("");
            cfInfoEx.number = convertNullToEmptyString("");
            cfInfoEx.timeSeconds = 0;
            cfInfoEx.timeSlotBegin = convertNullToEmptyString("");
            cfInfoEx.timeSlotEnd = convertNullToEmptyString("");
            try {
                radioProxy.queryCallForwardInTimeSlotStatus(rr.mSerial, cfInfoEx);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "queryCallForwardInTimeSlotStatus", e);
            }
        }
    }

    public void setCallForwardInTimeSlot(int action, int cfReason, int serviceClass, String number, int timeSeconds, long[] timeSlot, Message result) {
        String timeSlotBegin = "";
        String timeSlotEnd = "";
        if (timeSlot != null && timeSlot.length == 2) {
            for (int i = 0; i < timeSlot.length; i++) {
                Date date = new Date(timeSlot[i]);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                if (i == 0) {
                    timeSlotBegin = dateFormat.format(date);
                } else {
                    timeSlotEnd = dateFormat.format(date);
                }
            }
        }
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2126, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " action = " + action + " cfReason = " + cfReason + " serviceClass = " + serviceClass + " timeSeconds = " + timeSeconds + "timeSlot = [" + timeSlotBegin + ":" + timeSlotEnd + "]");
            CallForwardInfoEx cfInfoEx = new CallForwardInfoEx();
            cfInfoEx.status = action;
            cfInfoEx.reason = cfReason;
            cfInfoEx.serviceClass = serviceClass;
            cfInfoEx.toa = PhoneNumberUtils.toaFromString(number);
            cfInfoEx.number = convertNullToEmptyString(number);
            cfInfoEx.timeSeconds = timeSeconds;
            cfInfoEx.timeSlotBegin = convertNullToEmptyString(timeSlotBegin);
            cfInfoEx.timeSlotEnd = convertNullToEmptyString(timeSlotEnd);
            try {
                radioProxy.setCallForwardInTimeSlot(rr.mSerial, cfInfoEx);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCallForwardInTimeSlot", e);
            }
        }
    }

    public void runGbaAuthentication(String nafFqdn, String nafSecureProtocolId, boolean forceRun, int netId, int phoneId, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2127, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " nafFqdn = " + nafFqdn + " nafSecureProtocolId = " + nafSecureProtocolId + " forceRun = " + forceRun + " netId = " + netId);
            try {
                radioProxy.runGbaAuthentication(rr.mSerial, nafFqdn, nafSecureProtocolId, forceRun, netId);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "runGbaAuthentication", e);
            }
        }
    }

    public void registerForDataAllowed(Handler h, int what, Object obj) {
        this.mDataAllowedRegistrants.add(new Registrant(h, what, obj));
    }

    public void sendEmbmsAtCommand(String data, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2060, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " data: " + data);
            try {
                radioProxy.sendEmbmsAtCommand(rr.mSerial, data);
            } catch (RemoteException e) {
                handleMtkRadioProxyExceptionForRR(rr, "sendEmbmsAtCommand", e);
            }
        }
    }

    public void setEmbmsSessionStatusNotification(Handler h, int what, Object obj) {
        this.mEmbmsSessionStatusNotificationRegistrant.add(new Registrant(h, what, obj));
    }

    public void unSetEmbmsSessionStatusNotification(Handler h) {
        this.mEmbmsSessionStatusNotificationRegistrant.remove(h);
    }

    public void setAtInfoNotification(Handler h, int what, Object obj) {
        this.mEmbmsAtInfoNotificationRegistrant.add(new Registrant(h, what, obj));
    }

    public void unSetAtInfoNotification(Handler h) {
        this.mEmbmsAtInfoNotificationRegistrant.remove(h);
    }

    public void unregisterForDataAllowed(Handler h) {
        this.mDataAllowedRegistrants.remove(h);
    }

    public void registerForCallForwardingInfo(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        Rlog.d(RILJ_LOG_TAG, "call registerForCallForwardingInfo, Handler : " + h);
        this.mCallForwardingInfoRegistrants.add(r);
        Object obj2 = this.mCfuReturnValue;
        if (obj2 != null) {
            r.notifyRegistrant(new AsyncResult((Object) null, obj2, (Throwable) null));
        }
    }

    public void unregisterForCallForwardingInfo(Handler h) {
        this.mCallForwardingInfoRegistrants.remove(h);
    }

    public void setOnIncomingCallIndication(Handler h, int what, Object obj) {
        this.mIncomingCallIndicationRegistrant = new Registrant(h, what, obj);
    }

    public void unsetOnIncomingCallIndication(Handler h) {
        this.mIncomingCallIndicationRegistrant.clear();
    }

    public void registerForCallAdditionalInfo(Handler h, int what, Object obj) {
        this.mCallAdditionalInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallAdditionalInfo(Handler h) {
        this.mCallAdditionalInfoRegistrants.remove(h);
    }

    public void setOnSuppServiceNotificationEx(Handler h, int what, Object obj) {
        this.mSsnExRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSuppServiceNotificationEx(Handler h) {
        Registrant registrant = this.mSsnExRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mSsnExRegistrant.clear();
            this.mSsnExRegistrant = null;
        }
    }

    public void setOnCallRelatedSuppSvc(Handler h, int what, Object obj) {
        this.mCallRelatedSuppSvcRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCallRelatedSuppSvc(Handler h) {
        this.mCallRelatedSuppSvcRegistrant.clear();
    }

    public void registerForCipherIndication(Handler h, int what, Object obj) {
        this.mCipherIndicationRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCipherIndication(Handler h) {
        this.mCipherIndicationRegistrants.remove(h);
    }

    private void handleChldRelatedRequest(RILRequest rr, Object[] params) {
        int j;
        synchronized (this.mDtmfReqQueue) {
            int queueSize = this.mDtmfReqQueue.size();
            if (queueSize > 0) {
                if (this.mDtmfReqQueue.get().rr.mRequest == 49) {
                    Rlog.d(RILJ_LOG_TAG, "DTMF queue isn't 0, first request is START, send stop dtmf and pending switch");
                    if (queueSize > 1) {
                        j = 2;
                    } else {
                        j = 1;
                    }
                    Rlog.d(RILJ_LOG_TAG, "queue size  " + this.mDtmfReqQueue.size());
                    for (int i = queueSize + -1; i >= j; i--) {
                        this.mDtmfReqQueue.remove(i);
                    }
                    if (this.mDtmfReqQueue.size() == 1) {
                        Rlog.d(RILJ_LOG_TAG, "add dummy stop dtmf request");
                        RILRequest rr3 = obtainRequest(50, null, this.mRILDefaultWorkSource);
                        new Class[1][0] = Integer.TYPE;
                        DtmfQueueHandler.DtmfQueueRR dqrr3 = this.mDtmfReqQueue.buildDtmfQueueRR(rr3, new Object[]{Integer.valueOf(rr3.mSerial)});
                        this.mDtmfReqQueue.stop();
                        this.mDtmfReqQueue.add(dqrr3);
                    }
                } else {
                    Rlog.d(RILJ_LOG_TAG, "DTMF queue isn't 0, first is STOP, penging switch");
                    for (int i2 = queueSize - 1; i2 >= 1; i2--) {
                        this.mDtmfReqQueue.remove(i2);
                    }
                }
                if (this.mDtmfReqQueue.getPendingRequest() != null) {
                    RILRequest pendingRequest = this.mDtmfReqQueue.getPendingRequest().rr;
                    if (pendingRequest.mResult != null) {
                        AsyncResult.forMessage(pendingRequest.mResult, (Object) null, (Throwable) null);
                        pendingRequest.mResult.sendToTarget();
                    }
                }
                this.mDtmfReqQueue.setPendingRequest(this.mDtmfReqQueue.buildDtmfQueueRR(rr, params));
            } else {
                Rlog.d(RILJ_LOG_TAG, "DTMF queue is 0, send switch Immediately");
                this.mDtmfReqQueue.setSendChldRequest();
                sendDtmfQueueRR(this.mDtmfReqQueue.buildDtmfQueueRR(rr, params));
            }
        }
    }

    public void sendDtmfQueueRR(DtmfQueueHandler.DtmfQueueRR dqrr) {
        RILRequest rr = dqrr.rr;
        IRadio radioProxy = getRadioProxy(rr.mResult);
        if (radioProxy == null) {
            mtkRiljLoge("get RadioProxy null. ([" + rr.serialString() + "] request: " + requestToString(rr.mRequest) + ")");
            return;
        }
        mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " (by DtmfQueueRR)");
        int i = rr.mRequest;
        if (i == 15) {
            radioProxy.switchWaitingOrHoldingAndActive(rr.mSerial);
        } else if (i == 16) {
            radioProxy.conference(rr.mSerial);
        } else if (i == 49) {
            Object[] params = dqrr.params;
            if (params.length != 1) {
                mtkRiljLoge("request " + requestToString(rr.mRequest) + " params error. (" + params.toString() + ")");
                return;
            }
            char c = ((Character) params[0]).charValue();
            int i2 = rr.mSerial;
            radioProxy.startDtmf(i2, c + "");
        } else if (i == 50) {
            radioProxy.stopDtmf(rr.mSerial);
        } else if (i == 52) {
            Object[] params2 = dqrr.params;
            if (params2.length != 1) {
                mtkRiljLoge("request " + requestToString(rr.mRequest) + " params error. (" + Arrays.toString(params2) + ")");
                return;
            }
            radioProxy.separateConnection(rr.mSerial, ((Integer) params2[0]).intValue());
        } else if (i != 72) {
            try {
                mtkRiljLoge("get RadioProxy null. ([" + rr.serialString() + "] request: " + requestToString(rr.mRequest) + ")");
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "DtmfQueueRR(" + requestToString(rr.mRequest) + ")", e);
            }
        } else {
            radioProxy.explicitCallTransfer(rr.mSerial);
        }
    }

    public void handleDtmfQueueNext(int serial) {
        mtkRiljLog("handleDtmfQueueNext (serial = " + serial);
        synchronized (this.mDtmfReqQueue) {
            DtmfQueueHandler.DtmfQueueRR dqrr = null;
            int i = 0;
            while (true) {
                if (i < this.mDtmfReqQueue.mDtmfQueue.size()) {
                    DtmfQueueHandler.DtmfQueueRR adqrr = (DtmfQueueHandler.DtmfQueueRR) this.mDtmfReqQueue.mDtmfQueue.get(i);
                    if (adqrr != null && adqrr.rr.mSerial == serial) {
                        dqrr = adqrr;
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            if (dqrr == null) {
                mtkRiljLoge("cannot find serial " + serial + " from mDtmfQueue. (size = " + this.mDtmfReqQueue.size() + ")");
            } else {
                this.mDtmfReqQueue.remove(dqrr);
                mtkRiljLog("remove first item in dtmf queue done. (size = " + this.mDtmfReqQueue.size() + ")");
            }
            if (this.mDtmfReqQueue.size() > 0) {
                DtmfQueueHandler.DtmfQueueRR dqrr2 = this.mDtmfReqQueue.get();
                RILRequest rr2 = dqrr2.rr;
                mtkRiljLog(rr2.serialString() + "> " + requestToString(rr2.mRequest));
                sendDtmfQueueRR(dqrr2);
            } else if (this.mDtmfReqQueue.getPendingRequest() != null) {
                mtkRiljLog("send pending switch request");
                sendDtmfQueueRR(this.mDtmfReqQueue.getPendingRequest());
                this.mDtmfReqQueue.setSendChldRequest();
                this.mDtmfReqQueue.setPendingRequest(null);
            }
        }
    }

    public void switchWaitingOrHoldingAndActive(Message result) {
        if (getMtkRadioExProxy(result) != null) {
            RILRequest rr = obtainRequest(15, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            handleChldRelatedRequest(rr, null);
        }
    }

    public void conference(Message result) {
        if (getMtkRadioExProxy(result) != null) {
            RILRequest rr = obtainRequest(16, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            handleChldRelatedRequest(rr, null);
        }
    }

    public void separateConnection(int gsmIndex, Message result) {
        if (getMtkRadioExProxy(result) != null) {
            RILRequest rr = obtainRequest(52, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " gsmIndex = " + gsmIndex);
            handleChldRelatedRequest(rr, new Object[]{Integer.valueOf(gsmIndex)});
        }
    }

    public void explicitCallTransfer(Message result) {
        if (getMtkRadioExProxy(result) != null) {
            RILRequest rr = obtainRequest(72, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            handleChldRelatedRequest(rr, null);
        }
    }

    public void startDtmf(char c, Message result) {
        synchronized (this.mDtmfReqQueue) {
            if (!this.mDtmfReqQueue.hasSendChldRequest()) {
                int size = this.mDtmfReqQueue.size();
                Objects.requireNonNull(this.mDtmfReqQueue);
                if (size < 32) {
                    if (this.mDtmfReqQueue.isStart()) {
                        mtkRiljLog("DTMF status conflict, want to start DTMF when status is " + this.mDtmfReqQueue.isStart());
                    } else if (getMtkRadioExProxy(result) != null) {
                        RILRequest rr = obtainRequest(49, result, this.mRILDefaultWorkSource);
                        this.mDtmfReqQueue.start();
                        DtmfQueueHandler.DtmfQueueRR dqrr = this.mDtmfReqQueue.buildDtmfQueueRR(rr, new Object[]{Character.valueOf(c)});
                        this.mDtmfReqQueue.add(dqrr);
                        if (this.mDtmfReqQueue.size() == 1) {
                            mtkRiljLog("send start dtmf");
                            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                            sendDtmfQueueRR(dqrr);
                        }
                    }
                }
            }
        }
    }

    public void stopDtmf(Message result) {
        synchronized (this.mDtmfReqQueue) {
            if (!this.mDtmfReqQueue.hasSendChldRequest()) {
                int size = this.mDtmfReqQueue.size();
                Objects.requireNonNull(this.mDtmfReqQueue);
                if (size < 32) {
                    if (!this.mDtmfReqQueue.isStart()) {
                        mtkRiljLog("DTMF status conflict, want to start DTMF when status is " + this.mDtmfReqQueue.isStart());
                    } else if (getMtkRadioExProxy(result) != null) {
                        RILRequest rr = obtainRequest(50, result, this.mRILDefaultWorkSource);
                        this.mDtmfReqQueue.stop();
                        DtmfQueueHandler.DtmfQueueRR dqrr = this.mDtmfReqQueue.buildDtmfQueueRR(rr, null);
                        this.mDtmfReqQueue.add(dqrr);
                        if (this.mDtmfReqQueue.size() == 1) {
                            mtkRiljLog("send stop dtmf");
                            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                            sendDtmfQueueRR(dqrr);
                        }
                    }
                }
            }
        }
    }

    public void hangupAll(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2019, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.hangupAll(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "hangupAll", e);
            }
        }
    }

    public void setCallIndication(int mode, int callId, int seqNumber, int cause, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2016, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + mode + ", " + callId + ", " + seqNumber + ", " + cause);
            try {
                radioProxy.setCallIndication(rr.mSerial, mode, callId, seqNumber, cause);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCallIndication", e);
            }
        }
    }

    public void hangupConnectionWithCause(int gsmIndex, int cause, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2183, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " gsmIndex = " + gsmIndex);
            try {
                radioProxy.hangupWithReason(rr.mSerial, gsmIndex, cause);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "hangupConnectionWithCause", e);
            }
        }
    }

    public void setEccMode(String number, int enable, int airplaneMode, int imsReg, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2035, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " number=" + number + " enable=" + enable + " airplaneMode=" + airplaneMode + " imsReg=" + imsReg);
            try {
                radioProxy.setEccMode(rr.mSerial, number, enable, airplaneMode, imsReg);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setEccMode", e);
            }
        }
    }

    public void setEccPreferredRat(int phoneType, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2110, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " phoneType=" + phoneType);
            try {
                radioProxy.eccPreferredRat(rr.mSerial, phoneType);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setEccPreferredRat", e);
            }
        }
    }

    public void setVoicePreferStatus(int status) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(null);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2147, null, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " status: " + status);
            try {
                radioProxy.setVoicePreferStatus(rr.mSerial, status);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVoicePreferStatus", e);
            }
        }
    }

    public void setEccNum(String eccListWithCard, String eccListNoCard) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(null);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2148, null, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " eccListWithCard: " + eccListWithCard + ", eccListNoCard: " + eccListNoCard);
            try {
                radioProxy.setEccNum(rr.mSerial, eccListWithCard, eccListNoCard);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setEccNum", e);
            }
        }
    }

    public void getEccNum() {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(null);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2149, null, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getEccNum(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getEccNum", e);
            }
        }
    }

    public void registerForPseudoCellInfo(Handler h, int what, Object obj) {
        this.mPseudoCellInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForPseudoCellInfo(Handler h) {
        this.mPseudoCellInfoRegistrants.remove(h);
    }

    public void setApcMode(int apcMode, boolean reportOn, int reportInterval, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2021, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + apcMode + ", " + reportOn + ", " + reportInterval);
            int reportMode = 1;
            if (!reportOn) {
                reportMode = 0;
            }
            try {
                radioProxy.setApcMode(rr.mSerial, apcMode, reportMode, reportInterval);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setApcMode", e);
            }
        }
    }

    public void getApcInfo(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2022, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getApcInfo(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getApcInfo", e);
            }
        }
    }

    public void triggerModeSwitchByEcc(int mode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2023, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.triggerModeSwitchByEcc(rr.mSerial, mode);
                Message msg = this.mRilHandler.obtainMessage(5);
                msg.obj = null;
                msg.arg1 = rr.mSerial;
                this.mRilHandler.sendMessageDelayed(msg, 2000);
            } catch (RemoteException e) {
                handleMtkRadioProxyExceptionForRR(rr, "triggerModeSwitchByEcc", e);
            }
        }
    }

    public void getSmsRuimMemoryStatus(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2024, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getSmsRuimMemoryStatus(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getSmsRuimMemoryStatus", e);
            }
        }
    }

    public void setFdMode(int mode, int para1, int para2, Message response) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkCharacterSets.GB_2312, response, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setFdMode(rr.mSerial, mode, para1, para2);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setFdMode", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public ArrayList<HardwareConfig> convertHalHwConfigList(ArrayList<android.hardware.radio.V1_0.HardwareConfig> hwListRil, RIL ril) {
        HardwareConfig hw;
        int num = hwListRil.size();
        ArrayList<HardwareConfig> response = new ArrayList<>(num);
        mtkRiljLog("convertHalHwConfigList: num=" + num);
        Iterator<android.hardware.radio.V1_0.HardwareConfig> it = hwListRil.iterator();
        while (it.hasNext()) {
            android.hardware.radio.V1_0.HardwareConfig hwRil = it.next();
            int type = hwRil.type;
            if (type == 0) {
                hw = new MtkHardwareConfig(type);
                HardwareConfigModem hwModem = (HardwareConfigModem) hwRil.modem.get(0);
                hw.assignModem(hwRil.uuid, hwRil.state, hwModem.rilModel, hwModem.rat, hwModem.maxVoice, hwModem.maxData, hwModem.maxStandby);
            } else if (type == 1) {
                hw = new MtkHardwareConfig(type);
                hw.assignSim(hwRil.uuid, hwRil.state, ((HardwareConfigSim) hwRil.sim.get(0)).modemUuid);
            } else {
                throw new RuntimeException("RIL_REQUEST_GET_HARDWARE_CONFIG invalid hardward type:" + type);
            }
            response.add(hw);
        }
        return response;
    }

    public void setOnPlmnChangeNotification(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mWPMonitor) {
            this.mPlmnChangeNotificationRegistrant.add(r);
            if (this.mEcopsReturnValue != null) {
                r.notifyRegistrant(new AsyncResult((Object) null, this.mEcopsReturnValue, (Throwable) null));
                this.mEcopsReturnValue = null;
            }
        }
    }

    public void unSetOnPlmnChangeNotification(Handler h) {
        synchronized (this.mWPMonitor) {
            this.mPlmnChangeNotificationRegistrant.remove(h);
        }
    }

    public void setOnRegistrationSuspended(Handler h, int what, Object obj) {
        synchronized (this.mWPMonitor) {
            this.mRegistrationSuspendedRegistrant = new Registrant(h, what, obj);
            if (this.mEmsrReturnValue != null) {
                this.mRegistrationSuspendedRegistrant.notifyRegistrant(new AsyncResult((Object) null, this.mEmsrReturnValue, (Throwable) null));
                this.mEmsrReturnValue = null;
            }
        }
    }

    public void unSetOnRegistrationSuspended(Handler h) {
        synchronized (this.mWPMonitor) {
            this.mRegistrationSuspendedRegistrant.clear();
        }
    }

    public void registerForGmssRatChanged(Handler h, int what, Object obj) {
        this.mGmssRatChangedRegistrant.add(new Registrant(h, what, obj));
    }

    public void setResumeRegistration(int sessionId, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkGsmCdmaPhone.EVENT_IMS_UT_DONE, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " sessionId = " + sessionId);
            try {
                radioProxy.setResumeRegistration(rr.mSerial, sessionId);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setResumeRegistration", e);
            }
        }
    }

    public void storeModemType(int modemType, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2184, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " modemType = " + modemType + ", applyType:2");
            try {
                radioProxy.modifyModemType(rr.mSerial, 2, modemType);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "modifyModemType", e);
            }
        }
    }

    public void reloadModemType(int modemType, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2184, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " modemType = " + modemType + ", applyType:1");
            try {
                radioProxy.modifyModemType(rr.mSerial, 1, modemType);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "modifyModemType", e);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: int[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: int} */
    /* JADX WARN: Multi-variable type inference failed */
    public void handleStkCallSetupRequestFromSimWithResCode(boolean accept, int resCode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2029, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            int[] param = new int[1];
            if (resCode == 33 || resCode == 32) {
                param[0] = resCode;
            } else {
                param[0] = accept;
            }
            try {
                radioProxy.handleStkCallSetupRequestFromSimWithResCode(rr.mSerial, param[0]);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "handleStkCallSetupRequestFromSimWithResCode", e);
            }
        }
    }

    public void registerForSimTrayPlugIn(Handler h, int what, Object obj) {
        this.mSimTrayPlugIn.add(new Registrant(h, what, obj));
    }

    public void unregisterForSimTrayPlugIn(Handler h) {
        this.mSimTrayPlugIn.remove(h);
    }

    public void registerForCommonSlotNoChanged(Handler h, int what, Object obj) {
        this.mSimCommonSlotNoChanged.add(new Registrant(h, what, obj));
    }

    public void unregisterForCommonSlotNoChanged(Handler h) {
        this.mSimCommonSlotNoChanged.remove(h);
    }

    public void setVendorSetting(int setting, String value, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2173, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setVendorSetting(rr.mSerial, setting, value);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setVendorSetting", e);
            }
        }
    }

    public void restartRILD(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2150, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.restartRILD(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "restartRILD", e);
            }
        }
    }

    public void setOnBipProactiveCmd(Handler h, int what, Object obj) {
        this.mBipProCmdRegistrant.add(new Registrant(h, what, obj));
    }

    public void unSetOnBipProactiveCmd(Handler h) {
        this.mBipProCmdRegistrant.remove(h);
    }

    public void setOnStkSetupMenuReset(Handler h, int what, Object obj) {
        this.mStkSetupMenuResetRegistrant.add(new Registrant(h, what, obj));
    }

    public void unSetOnStkSetupMenuReset(Handler h) {
        this.mStkSetupMenuResetRegistrant.remove(h);
    }

    public void queryNetworkLock(int category, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2067, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.queryNetworkLock(rr.mSerial, category);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "queryNetworkLock", e);
            }
        }
    }

    public void setNetworkLock(int category, int lockop, String password, String data_imsi, String gid1, String gid2, Message result) {
        String data_imsi2;
        String gid12;
        String gid22;
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2068, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            String password2 = password == null ? "" : password;
            if (data_imsi == null) {
                data_imsi2 = "";
            } else {
                data_imsi2 = data_imsi;
            }
            if (gid1 == null) {
                gid12 = "";
            } else {
                gid12 = gid1;
            }
            if (gid2 == null) {
                gid22 = "";
            } else {
                gid22 = gid2;
            }
            try {
                radioProxy.setNetworkLock(rr.mSerial, category, lockop, password2, data_imsi2, gid12, gid22);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setNetworkLock", e);
            }
        }
    }

    public void supplyDepersonalization(String netpin, int type, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2143, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " netpin = " + netpin + " type = " + type);
            try {
                radioProxy.supplyDepersonalization(rr.mSerial, convertNullToEmptyString(netpin), type);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "supplyNetworkDepersonalization", e);
            }
        }
    }

    public void registerForResetAttachApn(Handler h, int what, Object obj) {
        this.mResetAttachApnRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForResetAttachApn(Handler h) {
        this.mResetAttachApnRegistrants.remove(h);
    }

    public void registerForAttachApnChanged(Handler h, int what, Object obj) {
        this.mAttachApnChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForAttachApnChanged(Handler h) {
        this.mAttachApnChangedRegistrants.remove(h);
    }

    public void registerForLteAccessStratumState(Handler h, int what, Object obj) {
        this.mLteAccessStratumStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForLteAccessStratumState(Handler h) {
        this.mLteAccessStratumStateRegistrants.remove(h);
    }

    public void setLteAccessStratumReport(boolean enable, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2065, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + (enable ? 1 : 0));
            try {
                radioProxy.setLteAccessStratumReport(rr.mSerial, enable);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setLteAccessStratumReport", e);
            }
        }
    }

    public void setLteUplinkDataTransfer(int state, int interfaceId, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2066, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " state = " + state + ", interfaceId = " + interfaceId);
            try {
                radioProxy.setLteUplinkDataTransfer(rr.mSerial, state, interfaceId);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setLteUplinkDataTransfer", e);
            }
        }
    }

    public void registerForSimPlugIn(Handler h, int what, Object obj) {
        this.mSimPlugIn.add(new Registrant(h, what, obj));
    }

    public void unregisterForSimPlugIn(Handler h) {
        this.mSimPlugIn.remove(h);
    }

    public void registerForSimPlugOut(Handler h, int what, Object obj) {
        this.mSimPlugOut.add(new Registrant(h, what, obj));
    }

    public void unregisterForSimPlugOut(Handler h) {
        this.mSimPlugOut.remove(h);
    }

    public void registerForSimMissing(Handler h, int what, Object obj) {
        this.mSimMissing.add(new Registrant(h, what, obj));
    }

    public void unregisterForSimMissing(Handler h) {
        this.mSimMissing.remove(h);
    }

    public void registerForSimRecovery(Handler h, int what, Object obj) {
        this.mSimRecovery.add(new Registrant(h, what, obj));
    }

    public void unregisterForSimRecovery(Handler h) {
        this.mSimRecovery.remove(h);
    }

    public void registerForSimPower(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mSimPowerChanged.add(r);
        Object obj2 = this.mSimPowerInfo;
        if (obj2 != null) {
            r.notifyRegistrant(new AsyncResult((Object) null, obj2, (Throwable) null));
        }
    }

    public void unregisterForSimPower(Handler h) {
        this.mSimPowerChanged.remove(h);
    }

    public void registerForSmlSlotLockInfoChanged(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mSmlSlotLockInfoChanged.add(r);
        Object obj2 = this.mSmlSlotLockInfo;
        if (obj2 != null) {
            r.notifyRegistrant(new AsyncResult((Object) null, obj2, (Throwable) null));
        }
    }

    public void unregisterForSmlSlotLockInfoChanged(Handler h) {
        this.mSmlSlotLockInfoChanged.remove(h);
    }

    public void supplyDeviceNetworkDepersonalization(String pwd, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2171, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.supplyDeviceNetworkDepersonalization(rr.mSerial, convertNullToEmptyString(pwd));
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "supplyDeviceNetworkDepersonalization", e);
            }
        }
    }

    public void registerForPhbReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        Rlog.d(RILJ_LOG_TAG, "call registerForPhbReady Handler : " + h);
        this.mPhbReadyRegistrants.add(r);
    }

    public void unregisterForPhbReady(Handler h) {
        this.mPhbReadyRegistrants.remove(h);
    }

    public void queryPhbStorageInfo(int type, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2036, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + type);
            try {
                radioProxy.queryPhbStorageInfo(rr.mSerial, type);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "queryPhbStorageInfo", e);
            }
        }
    }

    private PhbEntryStructure convertToHalPhbEntryStructure(PhbEntry pe) {
        PhbEntryStructure pes = new PhbEntryStructure();
        pes.type = pe.type;
        pes.index = pe.index;
        pes.number = convertNullToEmptyString(pe.number);
        pes.ton = pe.ton;
        pes.alphaId = convertNullToEmptyString(pe.alphaId);
        return pes;
    }

    public void writePhbEntry(PhbEntry entry, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2037, result, this.mRILDefaultWorkSource);
            PhbEntryStructure pes = convertToHalPhbEntryStructure(entry);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + entry);
            try {
                radioProxy.writePhbEntry(rr.mSerial, pes);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "writePhbEntry", e);
            }
        }
    }

    public void readPhbEntry(int type, int bIndex, int eIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2038, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + type + " begin: " + bIndex + " end: " + eIndex);
            try {
                radioProxy.readPhbEntry(rr.mSerial, type, bIndex, eIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "readPhbEntry", e);
            }
        }
    }

    public void queryUPBCapability(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2039, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.queryUPBCapability(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "queryUPBCapability", e);
            }
        }
    }

    public void editUPBEntry(int entryType, int adnIndex, int entryIndex, String strVal, String tonForNum, String aasAnrIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2040, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            ArrayList<String> arrList = new ArrayList<>();
            arrList.add(Integer.toString(entryType));
            arrList.add(Integer.toString(adnIndex));
            arrList.add(Integer.toString(entryIndex));
            arrList.add(strVal);
            if (entryType == 0) {
                arrList.add(tonForNum);
                arrList.add(aasAnrIndex);
            }
            try {
                radioProxy.editUPBEntry(rr.mSerial, arrList);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "editUPBEntry", e);
            }
        }
    }

    public void editUPBEntry(int entryType, int adnIndex, int entryIndex, String strVal, String tonForNum, Message result) {
        editUPBEntry(entryType, adnIndex, entryIndex, strVal, tonForNum, null, result);
    }

    public void deleteUPBEntry(int entryType, int adnIndex, int entryIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2041, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + entryType + " adnIndex: " + adnIndex + " entryIndex: " + entryIndex);
            try {
                radioProxy.deleteUPBEntry(rr.mSerial, entryType, adnIndex, entryIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "deleteUPBEntry", e);
            }
        }
    }

    public void readUPBGasList(int startIndex, int endIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2042, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ":  startIndex: " + startIndex + " endIndex: " + endIndex);
            try {
                radioProxy.readUPBGasList(rr.mSerial, startIndex, endIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "readUPBGasList", e);
            }
        }
    }

    public void readUPBGrpEntry(int adnIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2043, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ":  adnIndex: " + adnIndex);
            try {
                radioProxy.readUPBGrpEntry(rr.mSerial, adnIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "readUPBGrpEntry", e);
            }
        }
    }

    public void writeUPBGrpEntry(int adnIndex, int[] grpIds, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2044, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ":  adnIndex: " + adnIndex + " nLen: " + grpIds.length);
            ArrayList<Integer> intList = new ArrayList<>(grpIds.length);
            for (int i : grpIds) {
                intList.add(Integer.valueOf(i));
            }
            try {
                radioProxy.writeUPBGrpEntry(rr.mSerial, adnIndex, intList);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "writeUPBGrpEntry", e);
            }
        }
    }

    public void getPhoneBookStringsLength(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2045, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
            try {
                radioProxy.getPhoneBookStringsLength(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getPhoneBookStringsLength", e);
            }
        }
    }

    public void getPhoneBookMemStorage(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2046, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
            try {
                radioProxy.getPhoneBookMemStorage(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getPhoneBookMemStorage", e);
            }
        }
    }

    public void setPhoneBookMemStorage(String storage, String password, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2047, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
            try {
                radioProxy.setPhoneBookMemStorage(rr.mSerial, convertNullToEmptyString(storage), convertNullToEmptyString(password));
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "writeUPBGrpEntry", e);
            }
        }
    }

    public void readPhoneBookEntryExt(int index1, int index2, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkApnTypes.WAP, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
            try {
                radioProxy.readPhoneBookEntryExt(rr.mSerial, index1, index2);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "readPhoneBookEntryExt", e);
            }
        }
    }

    private static PhbEntryExt convertToHalPhbEntryExt(PBEntry pbe) {
        PhbEntryExt pee = new PhbEntryExt();
        pee.index = pbe.getIndex1();
        pee.number = pbe.getNumber();
        pee.type = pbe.getType();
        pee.text = pbe.getText();
        pee.hidden = pbe.getHidden();
        pee.group = pbe.getGroup();
        pee.adnumber = pbe.getAdnumber();
        pee.adtype = pbe.getAdtype();
        pee.secondtext = pbe.getSecondtext();
        pee.email = pbe.getEmail();
        return pee;
    }

    public void writePhoneBookEntryExt(PBEntry entry, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2049, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
            try {
                radioProxy.writePhoneBookEntryExt(rr.mSerial, convertToHalPhbEntryExt(entry));
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "writePhoneBookEntryExt", e);
            }
        }
    }

    public void queryUPBAvailable(int eftype, int fileIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2050, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " eftype: " + eftype + " fileIndex: " + fileIndex);
            try {
                radioProxy.queryUPBAvailable(rr.mSerial, eftype, fileIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "queryUPBAvailable", e);
            }
        }
    }

    public void readUPBEmailEntry(int adnIndex, int fileIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkCharacterSets.CP864, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " adnIndex: " + adnIndex + " fileIndex: " + fileIndex);
            try {
                radioProxy.readUPBEmailEntry(rr.mSerial, adnIndex, fileIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "readUPBEmailEntry", e);
            }
        }
    }

    public void readUPBSneEntry(int adnIndex, int fileIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2052, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " adnIndex: " + adnIndex + " fileIndex: " + fileIndex);
            try {
                radioProxy.readUPBSneEntry(rr.mSerial, adnIndex, fileIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "readUPBSneEntry", e);
            }
        }
    }

    public void readUPBAnrEntry(int adnIndex, int fileIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2053, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " adnIndex: " + adnIndex + " fileIndex: " + fileIndex);
            try {
                radioProxy.readUPBAnrEntry(rr.mSerial, adnIndex, fileIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "readUPBAnrEntry", e);
            }
        }
    }

    public void readUPBAasList(int startIndex, int endIndex, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2054, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " startIndex: " + startIndex + " endIndex: " + endIndex);
            try {
                radioProxy.readUPBAasList(rr.mSerial, startIndex, endIndex);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "readUPBAasList", e);
            }
        }
    }

    public void setPhonebookReady(int ready, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2157, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " ready = " + ready);
            try {
                radioProxy.setPhonebookReady(rr.mSerial, ready);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setPhonebookReady", e);
            }
        }
    }

    public void setRxTestConfig(int AntType, Message result) {
        IMtkRilOp rilOp = getRilOp();
        if (rilOp != null) {
            rilOp.setRxTestConfig(AntType, result);
        }
    }

    public void getRxTestResult(Message result) {
        IMtkRilOp rilOp = getRilOp();
        if (rilOp != null) {
            rilOp.getRxTestResult(result);
        }
    }

    public void getPOLCapability(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2107, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getPOLCapability(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getPOLCapability", e);
            }
        }
    }

    public void getCurrentPOLList(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2108, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getCurrentPOLList(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getCurrentPOLList", e);
            }
        }
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2109, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setPOLEntry(rr.mSerial, index, numeric, nAct);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setPOLEntry", e);
            }
        }
    }

    public void registerForPcoDataAfterAttached(Handler h, int what, Object obj) {
        this.mPcoDataAfterAttachedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForPcoDataAfterAttached(Handler h) {
        this.mPcoDataAfterAttachedRegistrants.remove(h);
    }

    public void syncDataSettingsToMd(int[] dataSetting, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2062, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", " + dataSetting[0] + ", " + dataSetting[1] + ", " + dataSetting[2]);
            ArrayList<Integer> settingList = new ArrayList<>(dataSetting.length);
            for (int i : dataSetting) {
                settingList.add(Integer.valueOf(i));
            }
            try {
                radioProxy.syncDataSettingsToMd(rr.mSerial, settingList);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "syncDataSettingsToMd", e);
            }
        }
    }

    public void resetMdDataRetryCount(String apnName, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2063, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + apnName);
            try {
                radioProxy.resetMdDataRetryCount(rr.mSerial, apnName);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "resetMdDataRetryCount", e);
            }
        }
    }

    public void registerForMdDataRetryCountReset(Handler h, int what, Object obj) {
        this.mMdDataRetryCountResetRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMdDataRetryCountReset(Handler h) {
        this.mMdDataRetryCountResetRegistrants.remove(h);
    }

    public void setRemoveRestrictEutranMode(boolean enable, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2100, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + (enable ? 1 : 0));
            try {
                radioProxy.setRemoveRestrictEutranMode(rr.mSerial, enable);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setRemoveRestrictEutranMode", e);
            }
        }
    }

    public void registerForRemoveRestrictEutran(Handler h, int what, Object obj) {
        this.mRemoveRestrictEutranRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRemoveRestrictEutran(Handler h) {
        this.mRemoveRestrictEutranRegistrants.remove(h);
    }

    public void registerForEconfSrvcc(Handler h, int what, Object obj) {
        this.mEconfSrvccRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEconfSrvcc(Handler h) {
        this.mEconfSrvccRegistrants.remove(h);
    }

    public void setRoamingEnable(int[] config, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2111, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            ArrayList<Integer> intList = new ArrayList<>(config.length);
            for (int i : config) {
                intList.add(Integer.valueOf(i));
            }
            try {
                radioProxy.setRoamingEnable(rr.mSerial, intList);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setRoamingEnable", e);
            }
        }
    }

    public void getRoamingEnable(int phoneId, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2112, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getRoamingEnable(rr.mSerial, phoneId);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getRoamingEnable", e);
            }
        }
    }

    public void setLteReleaseVersion(int mode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2151, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " mode = " + mode);
            try {
                radioProxy.setLteReleaseVersion(rr.mSerial, mode);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setLteReleaseVersion", e);
            }
        }
    }

    public void getLteReleaseVersion(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2152, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getLteReleaseVersion(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getLteReleaseVersion", e);
            }
        }
    }

    public void registerForMccMncChanged(Handler h, int what, Object obj) {
        this.mMccMncRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMccMncChanged(Handler h) {
        this.mMccMncRegistrants.remove(h);
    }

    public void registerForVsimIndication(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        mtkRiljLog("registerForVsimIndication called...");
        this.mVsimIndicationRegistrants.add(r);
    }

    public void unregisterForVsimIndication(Handler h) {
        mtkRiljLog("unregisterForVsimIndication called...");
        this.mVsimIndicationRegistrants.remove(h);
    }

    public boolean sendVsimNotification(int transactionId, int eventId, int simType, Message message) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(message);
        if (radioProxy == null) {
            return false;
        }
        RILRequest rr = obtainRequest(2113, message, this.mRILDefaultWorkSource);
        mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", eventId: " + eventId + ", simTpye: " + simType);
        try {
            radioProxy.sendVsimNotification(rr.mSerial, transactionId, eventId, simType);
            return true;
        } catch (RemoteException e) {
            handleMtkRadioProxyExceptionForRR(rr, "sendVsimNotification", e);
            return false;
        }
    }

    public boolean sendVsimOperation(int transactionId, int eventId, int message, int dataLength, byte[] data, Message response) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(response);
        if (radioProxy == null) {
            return false;
        }
        RILRequest rr = obtainRequest(2114, response, this.mRILDefaultWorkSource);
        mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", eventId: " + eventId + ", length: " + dataLength);
        ArrayList<Byte> arrList = new ArrayList<>();
        for (byte b : data) {
            arrList.add(Byte.valueOf(b));
        }
        try {
            try {
                radioProxy.sendVsimOperation(rr.mSerial, transactionId, eventId, message, dataLength, arrList);
                return true;
            } catch (RemoteException e) {
                e = e;
            }
        } catch (RemoteException e2) {
            e = e2;
            handleMtkRadioProxyExceptionForRR(rr, "sendVsimOperation", e);
            return false;
        }
    }

    public void registerForDedicatedBearerActivated(Handler h, int what, Object obj) {
        this.mDedicatedBearerActivedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDedicatedBearerActivated(Handler h) {
        this.mDedicatedBearerActivedRegistrants.remove(h);
    }

    public void registerForDedicatedBearerModified(Handler h, int what, Object obj) {
        this.mDedicatedBearerModifiedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDedicatedBearerModified(Handler h) {
        this.mDedicatedBearerModifiedRegistrants.remove(h);
    }

    public void registerForDedicatedBearerDeactivationed(Handler h, int what, Object obj) {
        this.mDedicatedBearerDeactivatedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDedicatedBearerDeactivationed(Handler h) {
        this.mDedicatedBearerDeactivatedRegistrants.remove(h);
    }

    public MtkDedicateDataCallResponse convertDedicatedDataCallResult(DedicateDataCall ddcResult) {
        MtkTftStatus mtkTftStatus;
        int i = ddcResult.ddcId;
        int interfaceId = ddcResult.interfaceId;
        int primaryCid = ddcResult.primaryCid;
        int cid = ddcResult.cid;
        int active = ddcResult.active;
        int signalingFlag = ddcResult.signalingFlag;
        int bearerId = ddcResult.bearerId;
        int failCause = ddcResult.failCause;
        mtkRiljLog("ddcResult.hasQos: " + ddcResult.hasQos);
        MtkQosStatus mtkQosStatus = ddcResult.hasQos != 0 ? new MtkQosStatus(ddcResult.qos.qci, ddcResult.qos.dlGbr, ddcResult.qos.ulGbr, ddcResult.qos.dlMbr, ddcResult.qos.ulMbr) : null;
        mtkRiljLog("ddcResult.hasTft: " + ddcResult.hasTft);
        if (ddcResult.hasTft != 0) {
            int operation = ddcResult.tft.operation;
            ArrayList<MtkPacketFilterInfo> mtkPacketFilterInfo = new ArrayList<>();
            Iterator<PktFilter> it = ddcResult.tft.pfList.iterator();
            while (it.hasNext()) {
                PktFilter info = it.next();
                mtkPacketFilterInfo.add(new MtkPacketFilterInfo(info.id, info.precedence, info.direction, info.networkPfIdentifier, info.bitmap, info.address, info.mask, info.protocolNextHeader, info.localPortLow, info.localPortHigh, info.remotePortLow, info.remotePortHigh, info.spi, info.tos, info.tosMask, info.flowLabel));
            }
            mtkTftStatus = new MtkTftStatus(operation, mtkPacketFilterInfo, new MtkTftParameter(ddcResult.tft.tftParameter.linkedPfList));
        } else {
            mtkTftStatus = null;
        }
        return new MtkDedicateDataCallResponse(interfaceId, primaryCid, cid, active, signalingFlag, bearerId, failCause, mtkQosStatus, mtkTftStatus, ddcResult.pcscf);
    }

    public void setServiceStateToModem(int voiceRegState, int dataRegState, int voiceRoamingType, int dataRoamingType, int rilVoiceRegState, int rilDataRegState, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2130, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " voiceRegState: " + voiceRegState + " dataRegState: " + dataRegState + " voiceRoamingType: " + voiceRoamingType + " dataRoamingType: " + dataRoamingType + " rilVoiceRegState: " + rilVoiceRegState + " rilDataRegState:" + rilDataRegState);
            try {
                radioProxy.setServiceStateToModem(rr.mSerial, voiceRegState, dataRegState, voiceRoamingType, dataRoamingType, rilVoiceRegState, rilDataRegState);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setServiceStateToModem", e);
            }
        }
    }

    public void dataConnectionAttach(int type, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2144, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.dataConnectionAttach(rr.mSerial, type);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "dataConnectionAttach", e);
            }
        }
    }

    public void dataConnectionDetach(int type, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2145, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.dataConnectionDetach(rr.mSerial, type);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "dataConnectionDetach", e);
            }
        }
    }

    public void resetAllConnections(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2146, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.resetAllConnections(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "resetAllConnections", e);
            }
        }
    }

    public void setOnUnsolOemHookRaw(Handler h, int what, Object obj) {
        this.mUnsolOemHookRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnUnsolOemHookRaw(Handler h) {
        Registrant registrant = this.mUnsolOemHookRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mUnsolOemHookRegistrant.clear();
            this.mUnsolOemHookRegistrant = null;
        }
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(59, response, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "[" + IccUtils.bytesToHexString(data) + "]");
            try {
                radioProxy.sendRequestRaw(rr.mSerial, primitiveArrayToArrayList(data));
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "invokeOemRilRequestStrings", e);
            }
        }
    }

    public void invokeOemRilRequestStrings(String[] strings, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(60, result, this.mRILDefaultWorkSource);
            String logStr = "";
            for (int i = 0; i < strings.length; i++) {
                logStr = logStr + strings[i] + " ";
            }
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " strings = " + logStr);
            try {
                radioProxy.sendRequestStrings(rr.mSerial, new ArrayList<>(Arrays.asList(strings)));
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "invokeOemRilRequestStrings", e);
            }
        }
    }

    public void setDisable2G(boolean mode, Message result) {
        IMtkRilOp rilOp = getRilOp();
        if (rilOp != null) {
            rilOp.setDisable2G(mode, result);
        }
    }

    public void getDisable2G(Message result) {
        IMtkRilOp rilOp = getRilOp();
        if (rilOp != null) {
            rilOp.getDisable2G(result);
        }
    }

    public void registerForTxPower(Handler h, int what, Object obj) {
        this.mTxPowerRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForTxPower(Handler h) {
        this.mTxPowerRegistrant.remove(h);
    }

    public void registerForTxPowerStatus(Handler h, int what, Object obj) {
        this.mTxPowerStatusRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForTxPowerStatus(Handler h) {
        this.mTxPowerStatusRegistrant.remove(h);
    }

    public void setTxPowerStatus(int enable, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2158, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setTxPowerStatus(rr.mSerial, enable);
                Message msg = this.mRilHandler.obtainMessage(5);
                msg.obj = null;
                msg.arg1 = rr.mSerial;
                this.mRilHandler.sendMessageDelayed(msg, 2000);
            } catch (RemoteException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setTxPowerStatus", e);
            }
        }
    }

    public void setSuppServProperty(String name, String value, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2168, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)) + " name=" + name + ", value=" + value + ", result=" + result);
            try {
                radioProxy.setSuppServProperty(rr.mSerial, name, value);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setSuppServProperty", e);
            }
        }
    }

    public void registerForDsbpStateChanged(Handler h, int what, Object obj) {
        this.mDsbpStateRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForDsbpStateChanged(Handler h) {
        this.mDsbpStateRegistrant.remove(h);
    }

    public void setGwsdMode(int mode, String kaMode, String kaCycle, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2180, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)));
            ArrayList<String> arrList = new ArrayList<>();
            arrList.add(Integer.toString(mode));
            arrList.add(kaMode);
            arrList.add(kaCycle);
            try {
                radioProxy.setGwsdMode(rr.mSerial, arrList);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setGwsdMode", e);
            }
        }
    }

    public void setCallValidTimer(int timer, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2181, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)) + " timer=" + timer);
            try {
                radioProxy.setCallValidTimer(rr.mSerial, timer);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setCallValidTimer", e);
            }
        }
    }

    public void setIgnoreSameNumberInterval(int interval, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2182, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)) + " interval=" + interval);
            try {
                radioProxy.setIgnoreSameNumberInterval(rr.mSerial, interval);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setIgnoreSameNumberInterval", e);
            }
        }
    }

    public void setKeepAliveByPDCPCtrlPDU(String data, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2199, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)) + " data=" + data);
            try {
                radioProxy.setKeepAliveByPDCPCtrlPDU(rr.mSerial, data);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setKeepAliveByPDCPCtrlPDU", e);
            }
        }
    }

    public void setKeepAliveByIpData(String data, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2200, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)) + " data=" + data);
            try {
                radioProxy.setKeepAliveByIpData(rr.mSerial, data);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "setKeepAliveByIpData", e);
            }
        }
    }

    public void enableDsdaIndication(boolean enable, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2185, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)) + " enable=" + enable);
            try {
                radioProxy.enableDsdaIndication(rr.mSerial, enable);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "enableDsdaIndication", e);
            }
        }
    }

    public void getDsdaStatus(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2186, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)));
            try {
                radioProxy.getDsdaStatus(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getDsdaStatus", e);
            }
        }
    }

    public void registerForDsdaStateChanged(Handler h, int what, Object obj) {
        this.mDsdaStateRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForDsdaStateChanged(Handler h) {
        this.mDsdaStateRegistrant.remove(h);
    }

    public void registerForQualifiedNetworkTypesChanged(Handler h, int what, Object obj) {
        this.mQualifiedNetworkTypesRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForQualifiedNetworkTypesChanged(Handler h) {
        this.mQualifiedNetworkTypesRegistrant.remove(h);
    }

    public void setPreferredNetworkType(int networkType, Message result) {
        IRadio radioProxy = getRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(73, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " networkType = " + networkType);
            this.mPreferredNetworkType = networkType;
            this.mMetrics.writeSetPreferredNetworkType(this.mPhoneId.intValue(), networkType);
            if (this.mRadioVersion.lessOrEqual(RADIO_HAL_VERSION_1_3)) {
                try {
                    radioProxy.setPreferredNetworkType(rr.mSerial, networkType);
                } catch (RemoteException | RuntimeException e) {
                    handleRadioProxyExceptionForRR(rr, "setPreferredNetworkType", e);
                }
            } else if (this.mRadioVersion.equals(RADIO_HAL_VERSION_1_4)) {
                try {
                    ((android.hardware.radio.V1_4.IRadio) radioProxy).setPreferredNetworkTypeBitmap(rr.mSerial, convertToHalRadioAccessFamily(MtkRadioAccessFamily.getRafFromNetworkType(networkType)));
                } catch (RemoteException | RuntimeException e2) {
                    handleRadioProxyExceptionForRR(rr, "setPreferredNetworkTypeBitmap", e2);
                }
            }
        }
    }

    public void iwlanSetRegisterCellularQualityReport(int qualityRegister, int type, int[] values, Message result) {
        int modemSignalType;
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2188, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " enable = " + qualityRegister + " type = " + type);
            String str1 = qualityRegister == 1 ? "1" : "0";
            if (type == 0) {
                modemSignalType = 0;
            } else if (type == 2) {
                modemSignalType = 2;
            } else if (type == 5) {
                modemSignalType = 1;
            } else if (type == 6) {
                modemSignalType = 3;
            } else if (type != 7) {
                riljLoge("iwlanSetRegisterCellularQualityReport(): type not support");
                modemSignalType = -1;
            } else {
                modemSignalType = 4;
            }
            if (modemSignalType == -1) {
                riljLog(rr.serialString() + "< MTK : RIL_REQUEST_IWLAN_REGISTER_CELLULAR_QUALITY_REPORT Fail - Type is not supported");
                if (result != null) {
                    AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
                    result.sendToTarget();
                    return;
                }
                return;
            }
            String str2 = String.valueOf(modemSignalType);
            int[] arraySorting = Arrays.copyOf(values, values.length);
            Arrays.sort(arraySorting);
            StringBuilder sb = new StringBuilder();
            for (int i = values.length - 1; i >= 0; i--) {
                sb.append(String.valueOf(arraySorting[i]));
                if (i > 0) {
                    sb.append(",");
                }
            }
            try {
                try {
                    radioProxy.registerCellQltyReport(rr.mSerial, str1, str2, sb.toString(), "500");
                } catch (RemoteException | RuntimeException e) {
                    e = e;
                }
            } catch (RemoteException | RuntimeException e2) {
                e = e2;
                handleMtkRadioProxyExceptionForRR(rr, "registerCellQltyReport", e);
            }
        }
    }

    public void getSuggestedPlmnList(int rat, int num, int timer, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2189, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", rat=" + rat + ", num=" + num + ", timer=" + timer);
            try {
                radioProxy.getSuggestedPlmnList(rr.mSerial, rat, num, timer);
            } catch (RemoteException | RuntimeException e) {
                handleMtkRadioProxyExceptionForRR(rr, "getSuggestedPlmnList", e);
            }
        }
    }

    public void registerForMobileDataUsage(Handler h, int what, Object obj) {
        this.mMobileDataUsageRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMobileDataUsage(Handler h) {
        this.mMobileDataUsageRegistrants.remove(h);
    }

    public void registerForNwLimitState(Handler h, int what, Object obj) {
        this.mNwLimitRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNwLimitState(Handler h) {
        this.mNwLimitRegistrants.remove(h);
    }

    public void sendSarIndicator(int sarCmdType, String sarParameter, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioExProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2201, result, this.mRILDefaultWorkSource);
            mtkRiljLog(rr.serialString() + "> " + requestToStringEx(Integer.valueOf(rr.mRequest)) + " sarCmdType=" + sarCmdType + " sarParameter=" + sarParameter);
            try {
                vendor.mediatek.hardware.mtkradioex.V1_5.IMtkRadioEx.castFrom((IHwInterface) radioProxy).sendSarIndicator(rr.mSerial, sarCmdType, sarParameter);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendSarIndicator", e);
            }
        }
    }

    public ArrayList<CellInfo> mtkConvertHalCellInfoList_1_4(ArrayList<android.hardware.radio.V1_4.CellInfo> records) {
        ArrayList<CellInfo> response = new ArrayList<>(records.size());
        long nanotime = SystemClock.elapsedRealtimeNanos();
        Iterator<android.hardware.radio.V1_4.CellInfo> it = records.iterator();
        while (it.hasNext()) {
            android.hardware.radio.V1_4.CellInfo record = it.next();
            if (record != null) {
                if (record.info.getDiscriminator() == 5) {
                    CellInfoNr nr_cellInfo = record.info.nr();
                    Parcel p = Parcel.obtain();
                    p.writeInt(0);
                    p.writeInt(record.isRegistered ? 1 : 0);
                    p.writeLong(nanotime);
                    p.writeInt(record.connectionStatus);
                    p.writeInt(0);
                    p.writeString(nr_cellInfo.cellidentity.mcc);
                    p.writeString(nr_cellInfo.cellidentity.mnc);
                    p.writeString(nr_cellInfo.cellidentity.operatorNames.alphaLong);
                    p.writeString(nr_cellInfo.cellidentity.operatorNames.alphaShort);
                    p.writeInt(nr_cellInfo.cellidentity.pci);
                    p.writeInt(nr_cellInfo.cellidentity.tac);
                    p.writeInt(nr_cellInfo.cellidentity.nrarfcn);
                    p.writeLong(nr_cellInfo.cellidentity.nci);
                    p.writeInt(nr_cellInfo.signalStrength.csiRsrp);
                    p.writeInt(nr_cellInfo.signalStrength.csiRsrq);
                    p.writeInt(nr_cellInfo.signalStrength.csiSinr);
                    p.writeInt(nr_cellInfo.signalStrength.ssRsrp);
                    p.writeInt(nr_cellInfo.signalStrength.ssRsrq);
                    p.writeInt(nr_cellInfo.signalStrength.ssSinr);
                    p.writeInt(0);
                    p.setDataPosition(0);
                    response.add((android.telephony.CellInfoNr) android.telephony.CellInfoNr.CREATOR.createFromParcel(p));
                } else {
                    response.add(CellInfo.create(record, nanotime));
                }
            }
        }
        return response;
    }

    public void notifyCellInfoListRegistrants(ArrayList<CellInfo> response) {
        this.mRilCellInfoListRegistrants.notifyRegistrants(new AsyncResult((Object) null, response, (Throwable) null));
    }
}
