package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.telephony.SmsParameters;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.mediatek.common.telephony.gsm.PBEntry;
import com.mediatek.internal.telephony.FemtoCellInfo;
import com.mediatek.internal.telephony.SrvccCallContext;
import com.mediatek.internal.telephony.uicc.PhbEntry;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class BaseCommands implements CommandsInterface {
    protected RegistrantList mAbnormalEventRegistrant;
    protected RegistrantList mAcceptedRegistrant;
    protected RegistrantList mAttachApnChangedRegistrants;
    protected RegistrantList mAvailRegistrants;
    protected Registrant mBipProCmdRegistrant;
    protected int mBipPsType;
    protected Registrant mCDMACardEsnMeidRegistrant;
    protected RegistrantList mCallForwardingInfoRegistrants;
    protected RegistrantList mCallInfoRegistrants;
    protected RegistrantList mCallRedialStateRegistrants;
    protected Registrant mCallRelatedSuppSvcRegistrant;
    protected RegistrantList mCallStateRegistrants;
    protected RegistrantList mCallWaitingInfoRegistrants;
    protected Registrant mCatCallSetUpRegistrant;
    protected Registrant mCatCcAlphaRegistrant;
    protected Registrant mCatEventRegistrant;
    protected Registrant mCatProCmdRegistrant;
    protected Registrant mCatSessionEndRegistrant;
    protected RegistrantList mCdmaCardTypeRegistrants;
    protected Object mCdmaCardTypeValue;
    protected RegistrantList mCdmaPrlChangedRegistrants;
    protected Object mCdmaSms;
    protected Registrant mCdmaSmsRegistrant;
    protected int mCdmaSubscription;
    protected RegistrantList mCdmaSubscriptionChangedRegistrants;
    protected Object mCfuReturnValue;
    protected RegistrantList mCipherIndicationRegistrant;
    protected RegistrantList mCommonSlotNoChangedRegistrants;
    protected Context mContext;
    protected RegistrantList mCsNetworkStateRegistrants;
    protected RegistrantList mDataAllowedRegistrants;
    protected RegistrantList mDataNetworkStateRegistrants;
    protected RegistrantList mDedicateBearerActivatedRegistrant;
    protected RegistrantList mDedicateBearerDeactivatedRegistrant;
    protected RegistrantList mDedicateBearerModifiedRegistrant;
    protected RegistrantList mDisplayInfoRegistrants;
    protected RegistrantList mEconfResultRegistrants;
    protected RegistrantList mEconfSrvccRegistrants;
    protected Object mEcopsReturnValue;
    protected Registrant mEfCspPlmnModeBitRegistrant;
    protected RegistrantList mEmergencyBearerSupportInfoRegistrants;
    protected Registrant mEmergencyCallbackModeRegistrant;
    protected Object mEmsrReturnValue;
    protected RegistrantList mEpsNetworkFeatureInfoRegistrants;
    protected RegistrantList mEpsNetworkFeatureSupportRegistrants;
    protected Object mEspOrMeid;
    protected Registrant mEtwsNotificationRegistrant;
    protected RegistrantList mExitEmergencyCallbackModeRegistrants;
    protected RegistrantList mFemtoCellInfoRegistrants;
    protected RegistrantList mGetAvailableNetworkDoneRegistrant;
    protected RegistrantList mGmssRatChangedRegistrant;
    protected Registrant mGsmBroadcastSmsRegistrant;
    protected Registrant mGsmSmsRegistrant;
    protected RegistrantList mHardwareConfigChangeRegistrants;
    protected RegistrantList mIccRefreshRegistrants;
    protected Registrant mIccSmsFullRegistrant;
    protected RegistrantList mIccStatusChangedRegistrants;
    protected RegistrantList mImeiLockRegistrant;
    protected RegistrantList mImsDisableRegistrants;
    protected RegistrantList mImsEnableRegistrants;
    protected RegistrantList mImsNetworkStateChangedRegistrants;
    protected RegistrantList mImsRegistrationInfoRegistrants;
    protected RegistrantList mImsiRefreshDoneRegistrant;
    protected Registrant mIncomingCallIndicationRegistrant;
    protected RegistrantList mInvalidSimInfoRegistrant;
    protected boolean mIsCatchPhbStatus;
    protected boolean mIsSmsReady;
    protected boolean mIsSmsSimFull;
    protected Registrant mLceInfoRegistrant;
    protected RegistrantList mLineControlInfoRegistrants;
    protected RegistrantList mLteAccessStratumStateRegistrants;
    protected RegistrantList mMdDataRetryCountResetRegistrants;
    protected Registrant mMeSmsFullRegistrant;
    protected RegistrantList mMelockRegistrants;
    protected RegistrantList mModulationRegistrants;
    protected Registrant mNITZTimeRegistrant;
    protected RegistrantList mNeighboringInfoRegistrants;
    protected RegistrantList mNetworkEventRegistrants;
    protected RegistrantList mNetworkExistRegistrants;
    protected RegistrantList mNetworkInfoRegistrants;
    protected int[] mNewVoiceTech;
    protected RegistrantList mNotAvailRegistrants;
    protected RegistrantList mNumberInfoRegistrants;
    protected RegistrantList mOemScreenRegistrants;
    protected RegistrantList mOffOrNotAvailRegistrants;
    protected RegistrantList mOnRegistrants;
    protected RegistrantList mOtaProvisionRegistrants;
    protected RegistrantList mPcoDataRegistrants;
    protected RegistrantList mPcoStatusRegistrant;
    protected RegistrantList mPhbReadyRegistrants;
    protected RegistrantList mPhoneRadioCapabilityChangedRegistrants;
    protected int mPhoneType;
    protected RegistrantList mPlmnChangeNotificationRegistrant;
    protected int mPreferredNetworkType;
    protected RegistrantList mPsNetworkStateRegistrants;
    RadioCapability mRadioCapability;
    protected RegistrantList mRadioStateChangedRegistrants;
    protected RegistrantList mRedirNumInfoRegistrants;
    protected Registrant mRegistrationSuspendedRegistrant;
    protected RegistrantList mRemoveRestrictEutranRegistrants;
    protected RegistrantList mResendIncallMuteRegistrants;
    protected RegistrantList mResetAttachApnRegistrants;
    protected Registrant mRestrictedStateRegistrant;
    protected RegistrantList mRilCellInfoListRegistrants;
    protected RegistrantList mRilConnectedRegistrants;
    protected int mRilVersion;
    protected Registrant mRingRegistrant;
    protected RegistrantList mRingbackToneRegistrants;
    protected RegistrantList mSessionChangedRegistrants;
    protected RegistrantList mSignalInfoRegistrants;
    protected Registrant mSignalStrengthRegistrant;
    protected RegistrantList mSimMissing;
    protected RegistrantList mSimPlugInRegistrants;
    protected RegistrantList mSimPlugOutRegistrants;
    protected RegistrantList mSimRecovery;
    protected Object mSimSms;
    protected Object mSms;
    protected Registrant mSmsOnSimRegistrant;
    protected RegistrantList mSmsReadyRegistrants;
    protected Registrant mSmsStatusRegistrant;
    protected Registrant mSpeechCodecInfoRegistrant;
    protected RegistrantList mSrvccHandoverInfoIndicationRegistrants;
    protected RegistrantList mSrvccStateRegistrants;
    protected Registrant mSsRegistrant;
    protected RegistrantList mSsacBarringInfoRegistrants;
    protected Registrant mSsnRegistrant;
    protected RadioState mState;
    protected Object mStateMonitor;
    protected Object mStatusSms;
    protected Registrant mStkCallCtrlRegistrant;
    protected Registrant mStkEvdlCallRegistrant;
    protected Registrant mStkSetupMenuResetRegistrant;
    protected int mStkSwitchMode;
    protected RegistrantList mSubscriptionStatusRegistrants;
    protected RegistrantList mT53AudCntrlInfoRegistrants;
    protected RegistrantList mT53ClirInfoRegistrants;
    protected RegistrantList mTrayPlugInRegistrants;
    protected RegistrantList mTxPowerRegistrant;
    protected Registrant mUSSDRegistrant;
    protected Registrant mUnsolOemHookRawRegistrant;
    protected RegistrantList mVirtualSimOff;
    protected RegistrantList mVirtualSimOn;
    protected RegistrantList mVoiceNetworkStateRegistrants;
    protected RegistrantList mVoicePrivacyOffRegistrants;
    protected RegistrantList mVoicePrivacyOnRegistrants;
    protected RegistrantList mVoiceRadioTechChangedRegistrants;
    protected RegistrantList mVtRingRegistrants;
    protected RegistrantList mVtStatusInfoRegistrants;
    protected Object mWPMonitor;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.BaseCommands.<init>(android.content.Context):void, dex:  in method: com.android.internal.telephony.BaseCommands.<init>(android.content.Context):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.BaseCommands.<init>(android.content.Context):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public BaseCommands(android.content.Context r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.BaseCommands.<init>(android.content.Context):void, dex:  in method: com.android.internal.telephony.BaseCommands.<init>(android.content.Context):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.BaseCommands.<init>(android.content.Context):void");
    }

    public RadioState getRadioState() {
        return this.mState;
    }

    public void registerForRadioStateChanged(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mRadioStateChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForRadioStateChanged(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mRadioStateChangedRegistrants.remove(h);
        }
    }

    public void registerForImsNetworkStateChanged(Handler h, int what, Object obj) {
        this.mImsNetworkStateChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsNetworkStateChanged(Handler h) {
        this.mImsNetworkStateChangedRegistrants.remove(h);
    }

    public void registerForOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mOnRegistrants.add(r);
            if (this.mState.isOn()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForOn(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mOnRegistrants.remove(h);
        }
    }

    public void registerForAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mAvailRegistrants.add(r);
            if (this.mState.isAvailable()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mAvailRegistrants.remove(h);
        }
    }

    public void registerForNotAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mNotAvailRegistrants.add(r);
            if (!this.mState.isAvailable()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForNotAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mNotAvailRegistrants.remove(h);
        }
    }

    public void registerForOffOrNotAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mOffOrNotAvailRegistrants.add(r);
            if (this.mState == RadioState.RADIO_OFF || !this.mState.isAvailable()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForOffOrNotAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mOffOrNotAvailRegistrants.remove(h);
        }
    }

    public void registerForCallStateChanged(Handler h, int what, Object obj) {
        this.mCallStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallStateChanged(Handler h) {
        this.mCallStateRegistrants.remove(h);
    }

    public void registerForVoiceNetworkStateChanged(Handler h, int what, Object obj) {
        this.mVoiceNetworkStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceNetworkStateChanged(Handler h) {
        this.mVoiceNetworkStateRegistrants.remove(h);
    }

    public void registerForDataNetworkStateChanged(Handler h, int what, Object obj) {
        this.mDataNetworkStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDataNetworkStateChanged(Handler h) {
        this.mDataNetworkStateRegistrants.remove(h);
    }

    public void registerForVoiceRadioTechChanged(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        if (this.mNewVoiceTech[0] != -1) {
            r.notifyRegistrant(new AsyncResult(null, this.mNewVoiceTech, null));
        }
        this.mVoiceRadioTechChangedRegistrants.add(r);
    }

    public void unregisterForVoiceRadioTechChanged(Handler h) {
        this.mVoiceRadioTechChangedRegistrants.remove(h);
    }

    public void registerForIccStatusChanged(Handler h, int what, Object obj) {
        this.mIccStatusChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIccStatusChanged(Handler h) {
        this.mIccStatusChangedRegistrants.remove(h);
    }

    public void setOnNewGsmSms(Handler h, int what, Object obj) {
        this.mGsmSmsRegistrant = new Registrant(h, what, obj);
    }

    public void notifyCachedSms() {
        Rlog.d("sms", "notifyCachedNewGsmSms-init");
        if (this.mSms != null && this.mGsmSmsRegistrant != null) {
            Rlog.e("sms", "Send the cached short message: " + this.mSms);
            this.mGsmSmsRegistrant.notifyRegistrant(new AsyncResult(null, this.mSms, null));
            this.mSms = null;
        }
    }

    public void unSetOnNewGsmSms(Handler h) {
        if (this.mGsmSmsRegistrant != null && this.mGsmSmsRegistrant.getHandler() == h) {
            this.mGsmSmsRegistrant.clear();
            this.mGsmSmsRegistrant = null;
        }
    }

    public void setOnNewCdmaSms(Handler h, int what, Object obj) {
        this.mCdmaSmsRegistrant = new Registrant(h, what, obj);
    }

    public void notifyCachedCdmaSms() {
        Rlog.d("sms", "notifyCachedNewCdmaSms-init");
        if (this.mCdmaSms != null && this.mCdmaSmsRegistrant != null) {
            Rlog.e("sms", "Send the cached short cdma message: " + this.mCdmaSms);
            this.mCdmaSmsRegistrant.notifyRegistrant(new AsyncResult(null, this.mCdmaSms, null));
            this.mCdmaSms = null;
        }
    }

    public void unSetOnNewCdmaSms(Handler h) {
        if (this.mCdmaSmsRegistrant != null && this.mCdmaSmsRegistrant.getHandler() == h) {
            this.mCdmaSmsRegistrant.clear();
            this.mCdmaSmsRegistrant = null;
        }
    }

    public void setOnNewGsmBroadcastSms(Handler h, int what, Object obj) {
        this.mGsmBroadcastSmsRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNewGsmBroadcastSms(Handler h) {
        if (this.mGsmBroadcastSmsRegistrant != null && this.mGsmBroadcastSmsRegistrant.getHandler() == h) {
            this.mGsmBroadcastSmsRegistrant.clear();
            this.mGsmBroadcastSmsRegistrant = null;
        }
    }

    public void setOnSmsOnSim(Handler h, int what, Object obj) {
        this.mSmsOnSimRegistrant = new Registrant(h, what, obj);
    }

    public void notifyCachedSimSms() {
        if (this.mSimSms != null && this.mSmsOnSimRegistrant != null) {
            Rlog.e("sms", "Send the cached short sim message: " + this.mSimSms);
            this.mSmsOnSimRegistrant.notifyRegistrant(new AsyncResult(null, this.mSimSms, null));
            this.mSimSms = null;
        }
    }

    public void unSetOnSmsOnSim(Handler h) {
        if (this.mSmsOnSimRegistrant != null && this.mSmsOnSimRegistrant.getHandler() == h) {
            this.mSmsOnSimRegistrant.clear();
            this.mSmsOnSimRegistrant = null;
        }
    }

    public void setOnSmsStatus(Handler h, int what, Object obj) {
        this.mSmsStatusRegistrant = new Registrant(h, what, obj);
    }

    public void notifyCachedStatusSms() {
        if (this.mStatusSms != null && this.mSmsStatusRegistrant != null) {
            Rlog.e("sms", "Send the cached short sms status message: " + this.mStatusSms);
            this.mSmsStatusRegistrant.notifyRegistrant(new AsyncResult(null, this.mStatusSms, null));
            this.mStatusSms = null;
        }
    }

    public void unSetOnSmsStatus(Handler h) {
        if (this.mSmsStatusRegistrant != null && this.mSmsStatusRegistrant.getHandler() == h) {
            this.mSmsStatusRegistrant.clear();
            this.mSmsStatusRegistrant = null;
        }
    }

    public void setOnSignalStrengthUpdate(Handler h, int what, Object obj) {
        this.mSignalStrengthRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSignalStrengthUpdate(Handler h) {
        if (this.mSignalStrengthRegistrant != null && this.mSignalStrengthRegistrant.getHandler() == h) {
            this.mSignalStrengthRegistrant.clear();
            this.mSignalStrengthRegistrant = null;
        }
    }

    public void setOnNITZTime(Handler h, int what, Object obj) {
        this.mNITZTimeRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNITZTime(Handler h) {
        if (this.mNITZTimeRegistrant != null && this.mNITZTimeRegistrant.getHandler() == h) {
            this.mNITZTimeRegistrant.clear();
            this.mNITZTimeRegistrant = null;
        }
    }

    public void setOnUSSD(Handler h, int what, Object obj) {
        this.mUSSDRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnUSSD(Handler h) {
        if (this.mUSSDRegistrant != null && this.mUSSDRegistrant.getHandler() == h) {
            this.mUSSDRegistrant.clear();
            this.mUSSDRegistrant = null;
        }
    }

    public void setOnSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSuppServiceNotification(Handler h) {
        if (this.mSsnRegistrant != null && this.mSsnRegistrant.getHandler() == h) {
            this.mSsnRegistrant.clear();
            this.mSsnRegistrant = null;
        }
    }

    public void setOnCatSessionEnd(Handler h, int what, Object obj) {
        this.mCatSessionEndRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatSessionEnd(Handler h) {
        if (this.mCatSessionEndRegistrant != null && this.mCatSessionEndRegistrant.getHandler() == h) {
            this.mCatSessionEndRegistrant.clear();
            this.mCatSessionEndRegistrant = null;
        }
    }

    public void setOnCatProactiveCmd(Handler h, int what, Object obj) {
        this.mCatProCmdRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatProactiveCmd(Handler h) {
        if (this.mCatProCmdRegistrant != null && this.mCatProCmdRegistrant.getHandler() == h) {
            this.mCatProCmdRegistrant.clear();
            this.mCatProCmdRegistrant = null;
        }
    }

    public void setOnBipProactiveCmd(Handler h, int what, Object obj) {
        this.mBipProCmdRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnBipProactiveCmd(Handler h) {
        if (this.mBipProCmdRegistrant != null && this.mBipProCmdRegistrant.getHandler() == h) {
            this.mBipProCmdRegistrant.clear();
            this.mBipProCmdRegistrant = null;
        }
    }

    public void setOnCatEvent(Handler h, int what, Object obj) {
        this.mCatEventRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatEvent(Handler h) {
        if (this.mCatEventRegistrant != null && this.mCatEventRegistrant.getHandler() == h) {
            this.mCatEventRegistrant.clear();
            this.mCatEventRegistrant = null;
        }
    }

    public void setOnCatCallSetUp(Handler h, int what, Object obj) {
        this.mCatCallSetUpRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatCallSetUp(Handler h) {
        if (this.mCatCallSetUpRegistrant != null && this.mCatCallSetUpRegistrant.getHandler() == h) {
            this.mCatCallSetUpRegistrant.clear();
            this.mCatCallSetUpRegistrant = null;
        }
    }

    public void setOnIccSmsFull(Handler h, int what, Object obj) {
        this.mIccSmsFullRegistrant = new Registrant(h, what, obj);
        if (this.mIsSmsSimFull) {
            this.mIccSmsFullRegistrant.notifyRegistrant();
            this.mIsSmsSimFull = false;
        }
    }

    public void unSetOnIccSmsFull(Handler h) {
        if (this.mIccSmsFullRegistrant != null && this.mIccSmsFullRegistrant.getHandler() == h) {
            this.mIccSmsFullRegistrant.clear();
            this.mIccSmsFullRegistrant = null;
        }
    }

    public void registerForIccRefresh(Handler h, int what, Object obj) {
        this.mIccRefreshRegistrants.add(new Registrant(h, what, obj));
    }

    public void setOnIccRefresh(Handler h, int what, Object obj) {
        registerForIccRefresh(h, what, obj);
    }

    public void setEmergencyCallbackMode(Handler h, int what, Object obj) {
        this.mEmergencyCallbackModeRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForIccRefresh(Handler h) {
        this.mIccRefreshRegistrants.remove(h);
    }

    public void unsetOnIccRefresh(Handler h) {
        unregisterForIccRefresh(h);
    }

    public void setOnCallRing(Handler h, int what, Object obj) {
        this.mRingRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCallRing(Handler h) {
        if (this.mRingRegistrant != null && this.mRingRegistrant.getHandler() == h) {
            this.mRingRegistrant.clear();
            this.mRingRegistrant = null;
        }
    }

    public void setOnSs(Handler h, int what, Object obj) {
        this.mSsRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSs(Handler h) {
        this.mSsRegistrant.clear();
    }

    public void setOnCatCcAlphaNotify(Handler h, int what, Object obj) {
        this.mCatCcAlphaRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatCcAlphaNotify(Handler h) {
        this.mCatCcAlphaRegistrant.clear();
    }

    public void setStkEvdlCallByAP(int enabled, Message response) {
    }

    public void setOnStkEvdlCall(Handler h, int what, Object obj) {
        this.mStkEvdlCallRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnStkEvdlCall(Handler h) {
        this.mStkEvdlCallRegistrant.clear();
    }

    public void setOnStkSetupMenuReset(Handler h, int what, Object obj) {
        this.mStkSetupMenuResetRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnStkSetupMenuReset(Handler h) {
        this.mStkSetupMenuResetRegistrant.clear();
    }

    public void setOnStkCallCtrl(Handler h, int what, Object obj) {
        this.mStkCallCtrlRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnStkCallCtrl(Handler h) {
        this.mStkCallCtrlRegistrant.clear();
    }

    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj) {
        this.mVoicePrivacyOnRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForInCallVoicePrivacyOn(Handler h) {
        this.mVoicePrivacyOnRegistrants.remove(h);
    }

    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj) {
        this.mVoicePrivacyOffRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForInCallVoicePrivacyOff(Handler h) {
        this.mVoicePrivacyOffRegistrants.remove(h);
    }

    public void setOnRestrictedStateChanged(Handler h, int what, Object obj) {
        this.mRestrictedStateRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnRestrictedStateChanged(Handler h) {
        if (this.mRestrictedStateRegistrant != null && this.mRestrictedStateRegistrant.getHandler() == h) {
            this.mRestrictedStateRegistrant.clear();
            this.mRestrictedStateRegistrant = null;
        }
    }

    public void registerForDisplayInfo(Handler h, int what, Object obj) {
        this.mDisplayInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDisplayInfo(Handler h) {
        this.mDisplayInfoRegistrants.remove(h);
    }

    public void registerForCallWaitingInfo(Handler h, int what, Object obj) {
        this.mCallWaitingInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallWaitingInfo(Handler h) {
        this.mCallWaitingInfoRegistrants.remove(h);
    }

    public void registerForSignalInfo(Handler h, int what, Object obj) {
        this.mSignalInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void setOnUnsolOemHookRaw(Handler h, int what, Object obj) {
        this.mUnsolOemHookRawRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnUnsolOemHookRaw(Handler h) {
        if (this.mUnsolOemHookRawRegistrant != null && this.mUnsolOemHookRawRegistrant.getHandler() == h) {
            this.mUnsolOemHookRawRegistrant.clear();
            this.mUnsolOemHookRawRegistrant = null;
        }
    }

    public void unregisterForSignalInfo(Handler h) {
        this.mSignalInfoRegistrants.remove(h);
    }

    public void registerForCdmaOtaProvision(Handler h, int what, Object obj) {
        this.mOtaProvisionRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCdmaOtaProvision(Handler h) {
        this.mOtaProvisionRegistrants.remove(h);
    }

    public void registerForNumberInfo(Handler h, int what, Object obj) {
        this.mNumberInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNumberInfo(Handler h) {
        this.mNumberInfoRegistrants.remove(h);
    }

    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
        this.mRedirNumInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRedirectedNumberInfo(Handler h) {
        this.mRedirNumInfoRegistrants.remove(h);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mLineControlInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mLineControlInfoRegistrants.remove(h);
    }

    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
        this.mT53ClirInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForT53ClirInfo(Handler h) {
        this.mT53ClirInfoRegistrants.remove(h);
    }

    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
        this.mT53AudCntrlInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForT53AudioControlInfo(Handler h) {
        this.mT53AudCntrlInfoRegistrants.remove(h);
    }

    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mRingbackToneRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRingbackTone(Handler h) {
        this.mRingbackToneRegistrants.remove(h);
    }

    public void registerForResendIncallMute(Handler h, int what, Object obj) {
        this.mResendIncallMuteRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForResendIncallMute(Handler h) {
        this.mResendIncallMuteRegistrants.remove(h);
    }

    public void registerForCdmaSubscriptionChanged(Handler h, int what, Object obj) {
        this.mCdmaSubscriptionChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCdmaSubscriptionChanged(Handler h) {
        this.mCdmaSubscriptionChangedRegistrants.remove(h);
    }

    public void registerForCdmaPrlChanged(Handler h, int what, Object obj) {
        this.mCdmaPrlChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCdmaPrlChanged(Handler h) {
        this.mCdmaPrlChangedRegistrants.remove(h);
    }

    public void registerForExitEmergencyCallbackMode(Handler h, int what, Object obj) {
        this.mExitEmergencyCallbackModeRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForExitEmergencyCallbackMode(Handler h) {
        this.mExitEmergencyCallbackModeRegistrants.remove(h);
    }

    public void registerForHardwareConfigChanged(Handler h, int what, Object obj) {
        this.mHardwareConfigChangeRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForHardwareConfigChanged(Handler h) {
        this.mHardwareConfigChangeRegistrants.remove(h);
    }

    public void registerForRilConnected(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mRilConnectedRegistrants.add(r);
        if (this.mRilVersion != -1) {
            r.notifyRegistrant(new AsyncResult(null, new Integer(this.mRilVersion), null));
        }
    }

    public void unregisterForRilConnected(Handler h) {
        this.mRilConnectedRegistrants.remove(h);
    }

    public void registerForSubscriptionStatusChanged(Handler h, int what, Object obj) {
        this.mSubscriptionStatusRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSubscriptionStatusChanged(Handler h) {
        this.mSubscriptionStatusRegistrants.remove(h);
    }

    /* JADX WARNING: Missing block: B:26:0x0052, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void setRadioState(RadioState newState) {
        synchronized (this.mStateMonitor) {
            RadioState oldState = this.mState;
            this.mState = newState;
            if (oldState == this.mState) {
                return;
            }
            this.mRadioStateChangedRegistrants.notifyRegistrants();
            if (this.mState.isAvailable() && !oldState.isAvailable()) {
                this.mAvailRegistrants.notifyRegistrants();
                onRadioAvailable();
            }
            if (!this.mState.isAvailable() && oldState.isAvailable()) {
                this.mNotAvailRegistrants.notifyRegistrants();
            }
            if (this.mState.isOn() && !oldState.isOn()) {
                this.mOnRegistrants.notifyRegistrants();
            }
            if (!(this.mState.isOn() && this.mState.isAvailable()) && oldState.isOn() && oldState.isAvailable()) {
                this.mOffOrNotAvailRegistrants.notifyRegistrants();
            }
        }
    }

    public void setModemPower(boolean power, Message response) {
    }

    protected void onRadioAvailable() {
    }

    public int getLteOnCdmaMode() {
        return TelephonyManager.getLteOnCdmaModeStatic();
    }

    public void registerForCellInfoList(Handler h, int what, Object obj) {
        this.mRilCellInfoListRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCellInfoList(Handler h) {
        this.mRilCellInfoListRegistrants.remove(h);
    }

    public void registerForSrvccStateChanged(Handler h, int what, Object obj) {
        this.mSrvccStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSrvccStateChanged(Handler h) {
        this.mSrvccStateRegistrants.remove(h);
    }

    public void testingEmergencyCall() {
    }

    public int getRilVersion() {
        return this.mRilVersion;
    }

    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message response) {
    }

    public void setDataAllowed(boolean allowed, Message response) {
    }

    public void requestShutdown(Message result) {
    }

    public void getRadioCapability(Message result) {
    }

    public void setRadioCapability(RadioCapability rc, Message response) {
    }

    public void registerForRadioCapabilityChanged(Handler h, int what, Object obj) {
        this.mPhoneRadioCapabilityChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRadioCapabilityChanged(Handler h) {
        this.mPhoneRadioCapabilityChangedRegistrants.remove(h);
    }

    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
    }

    public void stopLceService(Message result) {
    }

    public void pullLceData(Message result) {
    }

    public void registerForLceInfo(Handler h, int what, Object obj) {
        this.mLceInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForLceInfo(Handler h) {
        if (this.mLceInfoRegistrant != null && this.mLceInfoRegistrant.getHandler() == h) {
            this.mLceInfoRegistrant.clear();
            this.mLceInfoRegistrant = null;
        }
    }

    public void registerForPcoData(Handler h, int what, Object obj) {
        this.mPcoDataRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForPcoData(Handler h) {
        this.mPcoDataRegistrants.remove(h);
    }

    public void setOnIncomingCallIndication(Handler h, int what, Object obj) {
        this.mIncomingCallIndicationRegistrant = new Registrant(h, what, obj);
    }

    public void unsetOnIncomingCallIndication(Handler h) {
        this.mIncomingCallIndicationRegistrant.clear();
    }

    public void registerForCipherIndication(Handler h, int what, Object obj) {
        this.mCipherIndicationRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForCipherIndication(Handler h) {
        this.mCipherIndicationRegistrant.remove(h);
    }

    public void setOnSpeechCodecInfo(Handler h, int what, Object obj) {
        this.mSpeechCodecInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSpeechCodecInfo(Handler h) {
        if (this.mSpeechCodecInfoRegistrant != null && this.mSpeechCodecInfoRegistrant.getHandler() == h) {
            this.mSpeechCodecInfoRegistrant.clear();
            this.mSpeechCodecInfoRegistrant = null;
        }
    }

    public void registerForVtStatusInfo(Handler h, int what, Object obj) {
        this.mVtStatusInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVtStatusInfo(Handler h) {
        this.mVtStatusInfoRegistrants.remove(h);
    }

    public void registerForVtRingInfo(Handler h, int what, Object obj) {
        this.mVtRingRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVtRingInfo(Handler h) {
        this.mVtRingRegistrants.remove(h);
    }

    public void registerForCallRedialState(Handler h, int what, Object obj) {
        this.mCallRedialStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallRedialState(Handler h) {
        this.mCallRedialStateRegistrants.remove(h);
    }

    public void setRemoveRestrictEutranMode(boolean enable, Message result) {
    }

    public void registerForRemoveRestrictEutran(Handler h, int what, Object obj) {
        this.mRemoveRestrictEutranRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRemoveRestrictEutran(Handler h) {
        this.mRemoveRestrictEutranRegistrants.remove(h);
    }

    public void registerForMdDataRetryCountReset(Handler h, int what, Object obj) {
        this.mMdDataRetryCountResetRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMdDataRetryCountReset(Handler h) {
        this.mMdDataRetryCountResetRegistrants.remove(h);
    }

    public void setInitialAttachApn(String apn, String protocol, int authType, String username, String password, Object obj, Message result) {
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

    public void setupDataCall(int radioTechnology, int profile, String apn, String user, String password, int authType, String protocol, Message result) {
    }

    public void setupDataCall(int radioTechnology, int profile, String apn, String user, String password, int authType, String protocol, int interfaceId, Message result) {
    }

    public void syncApnTable(String[] apnlist, Message result) {
    }

    public void syncDataSettingsToMd(int[] dataSetting, Message result) {
    }

    public void resetMdDataRetryCount(String apnName, Message result) {
    }

    public void setTrm(int mode, Message result) {
    }

    public void registerForCallForwardingInfo(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mCallForwardingInfoRegistrants.add(r);
        if (this.mCfuReturnValue != null) {
            r.notifyRegistrant(new AsyncResult(null, this.mCfuReturnValue, null));
        }
    }

    public void unregisterForCallForwardingInfo(Handler h) {
        this.mCallForwardingInfoRegistrants.remove(h);
    }

    public void setOnCallRelatedSuppSvc(Handler h, int what, Object obj) {
        this.mCallRelatedSuppSvcRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCallRelatedSuppSvc(Handler h) {
        this.mCallRelatedSuppSvcRegistrant.clear();
    }

    public void hangupAll(Message result) {
    }

    public void setCallIndication(int mode, int callId, int seqNumber, Message response) {
    }

    public void emergencyDial(String address, int clirMode, UUSInfo uusInfo, Message result) {
    }

    public void setEccServiceCategory(int serviceCategory) {
    }

    public void setSpeechCodecInfo(boolean enable, Message response) {
    }

    public void vtDial(String address, int clirMode, UUSInfo uusInfo, Message result) {
    }

    public void acceptVtCallWithVoiceOnly(int callId, Message result) {
    }

    public void replaceVtCall(int index, Message result) {
    }

    public void sendCNAPSS(String cnapssString, Message response) {
    }

    public void setCLIP(boolean enable, Message response) {
    }

    public void openIccApplication(int application, Message response) {
    }

    public void getIccApplicationStatus(int sessionId, Message result) {
    }

    public void registerForSessionChanged(Handler h, int what, Object obj) {
        this.mSessionChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSessionChanged(Handler h) {
        this.mSessionChangedRegistrants.remove(h);
    }

    public void queryNetworkLock(int categrory, Message response) {
    }

    public void setNetworkLock(int catagory, int lockop, String password, String data_imsi, String gid1, String gid2, Message response) {
    }

    public void setCarrierRestrictionState(int state, String password, Message response) {
    }

    public void getCarrierRestrictionState(Message response) {
    }

    public void doGeneralSimAuthentication(int sessionId, int mode, int tag, String param1, String param2, Message response) {
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

    public void registerForSimPlugOut(Handler h, int what, Object obj) {
        this.mSimPlugOutRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSimPlugOut(Handler h) {
        this.mSimPlugOutRegistrants.remove(h);
    }

    public void registerForSimPlugIn(Handler h, int what, Object obj) {
        this.mSimPlugInRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSimPlugIn(Handler h) {
        this.mSimPlugInRegistrants.remove(h);
    }

    public void registerForTrayPlugIn(Handler h, int what, Object obj) {
        this.mTrayPlugInRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForTrayPlugIn(Handler h) {
        this.mTrayPlugInRegistrants.remove(h);
    }

    public void registerForCommonSlotNoChanged(Handler h, int what, Object obj) {
        this.mCommonSlotNoChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCommonSlotNoChanged(Handler h) {
        this.mCommonSlotNoChangedRegistrants.remove(h);
    }

    public void registerSetDataAllowed(Handler h, int what, Object obj) {
        this.mDataAllowedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterSetDataAllowed(Handler h) {
        this.mDataAllowedRegistrants.remove(h);
    }

    public void sendBTSIMProfile(int nAction, int nType, String strData, Message response) {
    }

    public void registerForEfCspPlmnModeBitChanged(Handler h, int what, Object obj) {
        this.mEfCspPlmnModeBitRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForEfCspPlmnModeBitChanged(Handler h) {
        this.mEfCspPlmnModeBitRegistrant.clear();
    }

    public void queryPhbStorageInfo(int type, Message response) {
    }

    public void writePhbEntry(PhbEntry entry, Message result) {
    }

    public void ReadPhbEntry(int type, int bIndex, int eIndex, Message response) {
    }

    public void registerForPhbReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        Rlog.d("RILJ", "call registerForPhbReady Handler : " + h);
        this.mPhbReadyRegistrants.add(r);
    }

    public void unregisterForPhbReady(Handler h) {
        this.mPhbReadyRegistrants.remove(h);
    }

    public void queryUPBCapability(Message response) {
    }

    public void editUPBEntry(int entryType, int adnIndex, int entryIndex, String strVal, String tonForNum, String aasAnrIndex, Message response) {
    }

    public void editUPBEntry(int entryType, int adnIndex, int entryIndex, String strVal, String tonForNum, Message response) {
    }

    public void deleteUPBEntry(int entryType, int adnIndex, int entryIndex, Message response) {
    }

    public void readUPBGasList(int startIndex, int endIndex, Message response) {
    }

    public void readUPBGrpEntry(int adnIndex, Message response) {
    }

    public void writeUPBGrpEntry(int adnIndex, int[] grpIds, Message response) {
    }

    public void getPhoneBookStringsLength(Message result) {
    }

    public void getPhoneBookMemStorage(Message result) {
    }

    public void setPhoneBookMemStorage(String storage, String password, Message result) {
    }

    public void readPhoneBookEntryExt(int index1, int index2, Message result) {
    }

    public void writePhoneBookEntryExt(PBEntry entry, Message result) {
    }

    public void queryUPBAvailable(int eftype, int fileIndex, Message response) {
    }

    public void readUPBEmailEntry(int adnIndex, int fileIndex, Message response) {
    }

    public void readUPBSneEntry(int adnIndex, int fileIndex, Message response) {
    }

    public void readUPBAnrEntry(int adnIndex, int fileIndex, Message response) {
    }

    public void readUPBAasList(int startIndex, int endIndex, Message response) {
    }

    public void setLteAccessStratumReport(boolean enable, Message result) {
    }

    public void setLteUplinkDataTransfer(int state, int interfaceId, Message result) {
    }

    public void registerForLteAccessStratumState(Handler h, int what, Object obj) {
        this.mLteAccessStratumStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForLteAccessStratumState(Handler h) {
        this.mLteAccessStratumStateRegistrants.remove(h);
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

    public void getSmsParameters(Message response) {
    }

    public void setSmsParameters(SmsParameters params, Message response) {
    }

    public void setEtws(int mode, Message result) {
    }

    public void setOnEtwsNotification(Handler h, int what, Object obj) {
        this.mEtwsNotificationRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnEtwsNotification(Handler h) {
        this.mEtwsNotificationRegistrant.clear();
    }

    public void setCellBroadcastChannelConfigInfo(String config, int cb_set_type, Message response) {
    }

    public void setCellBroadcastLanguageConfigInfo(String config, Message response) {
    }

    public void queryCellBroadcastConfigInfo(Message response) {
    }

    public void removeCellBroadcastMsg(int channelId, int serialId, Message response) {
    }

    public void getSmsSimMemoryStatus(Message result) {
    }

    public void getSmsRuimMemoryStatus(Message result) {
    }

    public void setCDMACardInitalEsnMeid(Handler h, int what, Object obj) {
        this.mCDMACardEsnMeidRegistrant = new Registrant(h, what, obj);
        if (this.mEspOrMeid != null) {
            this.mCDMACardEsnMeidRegistrant.notifyRegistrant(new AsyncResult(null, this.mEspOrMeid, null));
        }
    }

    public void unSetCDMACardInitalEsnMeid(Handler h) {
        this.mCDMACardEsnMeidRegistrant.clear();
    }

    public void registerForNeighboringInfo(Handler h, int what, Object obj) {
        this.mNeighboringInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNeighboringInfo(Handler h) {
        this.mNeighboringInfoRegistrants.remove(h);
    }

    public void registerForNetworkInfo(Handler h, int what, Object obj) {
        this.mNetworkInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkInfo(Handler h) {
        this.mNetworkInfoRegistrants.remove(h);
    }

    public void setInvalidSimInfo(Handler h, int what, Object obj) {
        this.mInvalidSimInfoRegistrant.add(new Registrant(h, what, obj));
    }

    public void unSetInvalidSimInfo(Handler h) {
        this.mInvalidSimInfoRegistrant.remove(h);
    }

    public void registerForIMEILock(Handler h, int what, Object obj) {
        this.mImeiLockRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForIMEILock(Handler h) {
        this.mImeiLockRegistrant.remove(h);
    }

    public void setNetworkSelectionModeManualWithAct(String operatorNumeric, String act, Message result) {
    }

    public void setNetworkSelectionModeSemiAutomatic(String operatorNumeric, String act, Message response) {
    }

    public void cancelAvailableNetworks(Message response) {
    }

    public void registerForGetAvailableNetworksDone(Handler h, int what, Object obj) {
        this.mGetAvailableNetworkDoneRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForGetAvailableNetworksDone(Handler h) {
        this.mGetAvailableNetworkDoneRegistrant.remove(h);
    }

    public void getPOLCapabilty(Message response) {
    }

    public void getCurrentPOLList(Message response) {
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
    }

    public void getFemtoCellList(String operatorNumeric, int rat, Message response) {
    }

    public void abortFemtoCellList(Message response) {
    }

    public void selectFemtoCell(FemtoCellInfo femtocell, Message response) {
    }

    public void registerForFemtoCellInfo(Handler h, int what, Object obj) {
        this.mFemtoCellInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void queryFemtoCellSystemSelectionMode(Message response) {
    }

    public void setFemtoCellSystemSelectionMode(int mode, Message response) {
    }

    public void registerForPsNetworkStateChanged(Handler h, int what, Object obj) {
        this.mPsNetworkStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForPsNetworkStateChanged(Handler h) {
        this.mPsNetworkStateRegistrants.remove(h);
    }

    public void registerForCsNetworkStateChanged(Handler h, int what, Object obj) {
        this.mCsNetworkStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCsNetworkStateChanged(Handler h) {
        this.mCsNetworkStateRegistrants.remove(h);
    }

    public boolean isGettingAvailableNetworks() {
        return false;
    }

    public void unregisterForFemtoCellInfo(Handler h) {
        this.mFemtoCellInfoRegistrants.remove(h);
    }

    public void registerForImsEnable(Handler h, int what, Object obj) {
        this.mImsEnableRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsEnable(Handler h) {
        this.mImsEnableRegistrants.remove(h);
    }

    public void registerForImsDisable(Handler h, int what, Object obj) {
        this.mImsDisableRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsDisable(Handler h) {
        this.mImsDisableRegistrants.remove(h);
    }

    public void registerForImsRegistrationInfo(Handler h, int what, Object obj) {
        this.mImsRegistrationInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsRegistrationInfo(Handler h) {
        this.mImsRegistrationInfoRegistrants.remove(h);
    }

    public void setIMSEnabled(boolean enable, Message response) {
    }

    public void registerForImsDisableDone(Handler h, int what, Object obj) {
    }

    public void unregisterForImsDisableDone(Handler h) {
    }

    public void setOnPlmnChangeNotification(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mWPMonitor) {
            this.mPlmnChangeNotificationRegistrant.add(r);
            if (this.mEcopsReturnValue != null) {
                r.notifyRegistrant(new AsyncResult(null, this.mEcopsReturnValue, null));
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
                this.mRegistrationSuspendedRegistrant.notifyRegistrant(new AsyncResult(null, this.mEmsrReturnValue, null));
                this.mEmsrReturnValue = null;
            }
        }
    }

    public void unSetOnRegistrationSuspended(Handler h) {
        synchronized (this.mWPMonitor) {
            this.mRegistrationSuspendedRegistrant.clear();
        }
    }

    public void registerForMelockChanged(Handler h, int what, Object obj) {
        this.mMelockRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMelockChanged(Handler h) {
        this.mMelockRegistrants.remove(h);
    }

    public void setFDMode(int mode, int parameter1, int parameter2, Message response) {
    }

    public void registerForEpsNetworkFeatureSupport(Handler h, int what, Object obj) {
        this.mEpsNetworkFeatureSupportRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEpsNetworkFeatureSupport(Handler h) {
        this.mEpsNetworkFeatureSupportRegistrants.remove(h);
    }

    public void registerForEconfSrvcc(Handler h, int what, Object obj) {
        this.mEconfSrvccRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEconfSrvcc(Handler h) {
        this.mEconfSrvccRegistrants.remove(h);
    }

    public void registerForEconfResult(Handler h, int what, Object obj) {
        this.mEconfResultRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEconfResult(Handler h) {
        this.mEconfResultRegistrants.remove(h);
    }

    public void registerForCallInfo(Handler h, int what, Object obj) {
        this.mCallInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallInfo(Handler h) {
        this.mCallInfoRegistrants.remove(h);
    }

    public void addConferenceMember(int confCallId, String address, int callIdToAdd, Message response) {
    }

    public void removeConferenceMember(int confCallId, String address, int callIdToRemove, Message response) {
    }

    public void resumeCall(int callIdToResume, Message response) {
    }

    public void holdCall(int callIdToHold, Message response) {
    }

    public void registerForEpsNetworkFeatureInfo(Handler h, int what, Object obj) {
        this.mEpsNetworkFeatureInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEpsNetworkFeatureInfo(Handler h) {
        this.mEpsNetworkFeatureInfoRegistrants.remove(h);
    }

    public void registerForSsacBarringInfo(Handler h, int what, Object obj) {
        this.mSsacBarringInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSsacBarringInfo(Handler h) {
        this.mSsacBarringInfoRegistrants.remove(h);
    }

    public void registerForSrvccHandoverInfoIndication(Handler h, int what, Object obj) {
        this.mSrvccHandoverInfoIndicationRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSrvccHandoverInfoIndication(Handler h) {
        this.mSrvccHandoverInfoIndicationRegistrants.remove(h);
    }

    public void registerForEmergencyBearerSupportInfo(Handler h, int what, Object obj) {
        this.mEmergencyBearerSupportInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEmergencyBearerSupportInfo(Handler h) {
        this.mEmergencyBearerSupportInfoRegistrants.remove(h);
    }

    public void sendScreenState(boolean on) {
    }

    public void setDataCentric(boolean enable, Message response) {
    }

    public void setImsCallStatus(boolean existed, Message response) {
    }

    public void setSrvccCallContextTransfer(int numberOfCall, SrvccCallContext[] callList) {
    }

    public void updateImsRegistrationStatus(int regState, int regType, int reason) {
    }

    public void registerForAbnormalEvent(Handler h, int what, Object obj) {
        this.mAbnormalEventRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForAbnormalEvent(Handler h) {
        this.mAbnormalEventRegistrant.remove(h);
    }

    public int getDisplayState() {
        return 0;
    }

    public String lookupOperatorNameFromNetwork(long subId, String numeric, boolean desireLongName) {
        return null;
    }

    public void conferenceDial(String[] participants, int clirMode, boolean isVideoCall, Message result) {
    }

    public void registerForImsiRefreshDone(Handler h, int what, Object obj) {
        this.mImsiRefreshDoneRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsiRefreshDone(Handler h) {
        this.mImsiRefreshDoneRegistrant.remove(h);
    }

    public RadioCapability getBootupRadioCapability() {
        Rlog.d("RILJ", "getBootupRadioCapability: " + this.mRadioCapability);
        return this.mRadioCapability;
    }

    public void setRegistrationSuspendEnabled(int enabled, Message response) {
    }

    public void setResumeRegistration(int sessionId, Message response) {
    }

    public void enableMd3Sleep(int enable) {
    }

    public void registerForNetworkExsit(Handler h, int what, Object obj) {
        Rlog.d("RILJ", "registerForNetworkExsit h=" + h + " w=" + what);
        this.mNetworkExistRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkExsit(Handler h) {
        Rlog.d("RILJ", "registerForNetworkExsit");
        this.mNetworkExistRegistrants.remove(h);
    }

    public void registerForModulation(Handler h, int what, Object obj) {
        Rlog.d("RILJ", "registerForModulation h=" + h + " w=" + what);
        this.mModulationRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForModulation(Handler h) {
        Rlog.d("RILJ", "unregisterForModulation");
        this.mModulationRegistrants.remove(h);
    }

    public void registerForNetworkEvent(Handler h, int what, Object obj) {
        Rlog.d("RILJ", "registerForNetworkEvent h=" + h + " w=" + what);
        this.mNetworkEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkEvent(Handler h) {
        Rlog.d("RILJ", "registerForNetworkEvent");
        this.mNetworkEventRegistrants.remove(h);
    }

    public void registerForCallAccepted(Handler h, int what, Object obj) {
        this.mAcceptedRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallAccepted(Handler h) {
        this.mAcceptedRegistrant.remove(h);
    }

    public void setSimPower(int mode, Message result) {
    }

    public void triggerModeSwitchByEcc(int mode, Message response) {
    }

    public void setBandMode(int[] bandMode, Message response) {
    }

    public void getCOLP(Message response) {
    }

    public void setCOLP(boolean enable, Message response) {
    }

    public void getCOLR(Message response) {
    }

    public void iccGetATR(Message result) {
    }

    public void iccOpenChannelWithSw(String AID, Message result) {
    }

    public void storeModemType(int modemType, Message response) {
    }

    public void reloadModemType(int modemType, Message response) {
    }

    public void queryModemType(Message response) {
    }

    public void syncApnTableToRds(String[] apnlist, Message response) {
    }

    public void registerForPcoStatus(Handler h, int what, Object obj) {
        this.mPcoStatusRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForPcoStatus(Handler h) {
        this.mPcoStatusRegistrant.remove(h);
    }

    public void registerForGmssRatChanged(Handler h, int what, Object obj) {
        this.mGmssRatChangedRegistrant.add(new Registrant(h, what, obj));
    }

    public void enablePseudoBSMonitor(boolean reportOn, int reportRateInSeconds, Message response) {
    }

    public void disablePseudoBSMonitor(Message response) {
    }

    public void queryPseudoBSRecords(Message response) {
    }

    public void enablePseudoBSMonitor(int apcMode, boolean reportOn, int reportRateInSeconds, Message response) {
    }

    public void setRxTestConfig(int AntType, Message result) {
    }

    public void getRxTestResult(Message result) {
    }

    public void setCurrentStatus(int airplaneMode, int imsReg, Message response) {
    }

    public void setEccPreferredRat(int phoneType, Message response) {
    }

    public void setGsmBroadcastLangs(String lang, Message response) {
    }

    public void getGsmBroadcastLangs(Message response) {
    }

    public void getGsmBroadcastActivation(Message response) {
    }

    public void setGsmBroadcastConfigEx(SmsBroadcastConfigInfo[] config, Message response) {
    }

    public void getGsmBroadcastConfigEx(Message response) {
    }

    public void queryCallForwardInTimeSlotStatus(int cfReason, int serviceClass, Message response) {
    }

    public void setCallForwardInTimeSlot(int action, int cfReason, int serviceClass, String number, int timeSeconds, long[] timeSlot, Message response) {
    }

    public void registerForTxPower(Handler h, int what, Object obj) {
        this.mTxPowerRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForTxPower(Handler h) {
        this.mTxPowerRegistrant.remove(h);
    }

    public void registerForOemScreenChanged(Handler h, int what, Object obj) {
        Rlog.d("oem", "leon EVENT_OEM_SCREEN_CHANGED reg:" + h);
        this.mOemScreenRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterOemScreenChanged(Handler h) {
        Rlog.d("oem", "leon EVENT_OEM_SCREEN_CHANGED unreg:" + h);
        this.mOemScreenRegistrants.remove(h);
    }

    public int oppoGetPreferredNetworkType() {
        return this.mPreferredNetworkType;
    }
}
