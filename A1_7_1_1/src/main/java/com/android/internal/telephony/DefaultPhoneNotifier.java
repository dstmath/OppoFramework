package com.android.internal.telephony;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.CellInfo;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneInternalInterface.DataActivityState;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DefaultPhoneNotifier implements PhoneNotifier {
    /* renamed from: -com-android-internal-telephony-Call$StateSwitchesValues */
    private static final /* synthetic */ int[] f4-com-android-internal-telephony-Call$StateSwitchesValues = null;
    /* renamed from: -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues */
    private static final /* synthetic */ int[] f5xce0d2696 = null;
    /* renamed from: -com-android-internal-telephony-PhoneConstants$StateSwitchesValues */
    private static final /* synthetic */ int[] f6xb32e9020 = null;
    /* renamed from: -com-android-internal-telephony-PhoneInternalInterface$DataActivityStateSwitchesValues */
    private static final /* synthetic */ int[] f7xb8401fb4 = null;
    private static final boolean DBG = false;
    private static final String LOG_TAG = "DefaultPhoneNotifier";
    private int mPendingSub;
    protected ITelephonyRegistry mRegistry;
    private Phone mSender;

    /* renamed from: com.android.internal.telephony.DefaultPhoneNotifier$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ DefaultPhoneNotifier this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.DefaultPhoneNotifier.1.<init>(com.android.internal.telephony.DefaultPhoneNotifier):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass1(com.android.internal.telephony.DefaultPhoneNotifier r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.DefaultPhoneNotifier.1.<init>(com.android.internal.telephony.DefaultPhoneNotifier):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.DefaultPhoneNotifier.1.<init>(com.android.internal.telephony.DefaultPhoneNotifier):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.DefaultPhoneNotifier.1.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.DefaultPhoneNotifier.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.DefaultPhoneNotifier.1.run():void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-Call$StateSwitchesValues */
    private static /* synthetic */ int[] m11-getcom-android-internal-telephony-Call$StateSwitchesValues() {
        if (f4-com-android-internal-telephony-Call$StateSwitchesValues != null) {
            return f4-com-android-internal-telephony-Call$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 18;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.WAITING.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        f4-com-android-internal-telephony-Call$StateSwitchesValues = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues */
    private static /* synthetic */ int[] m12x9dac0c3a() {
        if (f5xce0d2696 != null) {
            return f5xce0d2696;
        }
        int[] iArr = new int[DataState.values().length];
        try {
            iArr[DataState.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataState.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DataState.DISCONNECTED.ordinal()] = 18;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DataState.SUSPENDED.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        f5xce0d2696 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-PhoneConstants$StateSwitchesValues */
    private static /* synthetic */ int[] m13x3549e7c4() {
        if (f6xb32e9020 != null) {
            return f6xb32e9020;
        }
        int[] iArr = new int[PhoneConstants.State.values().length];
        try {
            iArr[PhoneConstants.State.IDLE.ordinal()] = 18;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PhoneConstants.State.OFFHOOK.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PhoneConstants.State.RINGING.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        f6xb32e9020 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-PhoneInternalInterface$DataActivityStateSwitchesValues */
    private static /* synthetic */ int[] m14x2c473d58() {
        if (f7xb8401fb4 != null) {
            return f7xb8401fb4;
        }
        int[] iArr = new int[DataActivityState.values().length];
        try {
            iArr[DataActivityState.DATAIN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataActivityState.DATAINANDOUT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DataActivityState.DATAOUT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DataActivityState.DORMANT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DataActivityState.NONE.ordinal()] = 18;
        } catch (NoSuchFieldError e5) {
        }
        f7xb8401fb4 = iArr;
        return iArr;
    }

    public DefaultPhoneNotifier() {
        this.mPendingSub = -1;
        this.mSender = null;
        this.mRegistry = Stub.asInterface(ServiceManager.getService("telephony.registry"));
    }

    public void notifyPhoneState(Phone sender) {
        if (sender.getState() == PhoneConstants.State.RINGING) {
            Rlog.d(LOG_TAG, "ringing state notify move to oppo/phone/Telecom/PhoneStateBroadcaster,so this return!");
            return;
        }
        Call ringingCall = sender.getRingingCall();
        int subId = sender.getSubId();
        int phoneId = sender.getPhoneId();
        int phoneType = sender.getPhoneType();
        String incomingNumber = UsimPBMemInfo.STRING_NOT_SET;
        if (!(ringingCall == null || ringingCall.getEarliestConnection() == null)) {
            incomingNumber = ringingCall.getEarliestConnection().getAddress();
        }
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCallStateForPhoneInfo(phoneType, phoneId, subId, convertCallState(sender.getState()), incomingNumber);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyServiceState(Phone sender) {
        ServiceState ss = sender.getServiceState();
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        Rlog.d(LOG_TAG, "nofityServiceState: mRegistry=" + this.mRegistry + " ss=" + ss + " sender=" + sender + " phondId=" + phoneId + " subId=" + subId);
        if (ss == null) {
            ss = new ServiceState();
            ss.setStateOutOfService();
        }
        if (SubscriptionManager.getDefaultDataSubId() == subId) {
            evalNotifyVolteOff(sender, ss);
        }
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyServiceStateForPhoneId(phoneId, subId, ss);
            }
        } catch (RemoteException e) {
        }
    }

    private void evalNotifyVolteOff(Phone sender, ServiceState ss) {
        if (ss != null && sender != null) {
            int subId = sender.getSubId();
            boolean isFeatureOn = sender.getBooleanCarrierConfig("config_oppo_volte_notify_stable_bool");
            int dataType = ss.getDataNetworkType();
            Rlog.d(LOG_TAG, "notifyVolte OFF , mPendingSub " + this.mPendingSub + ", isFeatureOn " + isFeatureOn + ", isVolte " + sender.isVolteEnabled());
            if (!(!isFeatureOn || dataType == 13 || dataType == 19 || !sender.isVolteEnabled() || this.mPendingSub == subId || this.mRegistry == null)) {
                try {
                    this.mRegistry.notifyVoLteServiceStateChanged(new VoLteServiceState(5));
                    this.mPendingSub = subId;
                    this.mSender = sender;
                    sender.postDelayed(new AnonymousClass1(this), 4000);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private void evalNotifyVolteOn() {
        if (this.mPendingSub != -1 && this.mSender != null) {
            boolean isIms = !this.mSender.isVolteEnabled() ? this.mSender.isWifiCallingEnabled() : true;
            Rlog.d(LOG_TAG, "notifyVolte ON, isIms " + isIms + ", mPendingSub" + this.mPendingSub);
            if (isIms) {
                try {
                    if (this.mRegistry != null) {
                        this.mRegistry.notifyVoLteServiceStateChanged(new VoLteServiceState(4));
                    }
                } catch (RemoteException e) {
                }
            }
            this.mPendingSub = -1;
        }
    }

    public void notifySignalStrength(Phone sender) {
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifySignalStrengthForPhoneId(phoneId, subId, sender.getSignalStrength());
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyMessageWaitingChanged(Phone sender) {
        int phoneId = sender.getPhoneId();
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyMessageWaitingChangedForPhoneId(phoneId, subId, sender.getMessageWaitingIndicator());
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyCallForwardingChanged(Phone sender) {
        int subId = sender.getSubId();
        Rlog.d(LOG_TAG, "notifyCallForwardingChanged: " + subId + ",  mRegistry: " + this.mRegistry);
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCallForwardingChangedForSubscriber(subId, sender.getCallForwardingIndicator());
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataActivity(Phone sender) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyDataActivityForSubscriber(subId, convertDataActivityState(sender.getDataActivityState()));
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataConnection(Phone sender, String reason, String apnType, DataState state) {
        doNotifyDataConnection(sender, reason, apnType, state);
    }

    private void doNotifyDataConnection(Phone sender, String reason, String apnType, DataState state) {
        int subId = sender.getSubId();
        long dds = (long) SubscriptionManager.getDefaultDataSubscriptionId();
        if ("emergency".equals(apnType) && DataState.CONNECTED == state) {
            log("doNotifyDataConnection apnType=" + apnType + ", state=" + state + ", oppo did not notify emergency PDN");
            return;
        }
        int networkType;
        TelephonyManager telephony = TelephonyManager.getDefault();
        LinkProperties linkProperties = null;
        NetworkCapabilities networkCapabilities = null;
        boolean roaming = false;
        if (state == DataState.CONNECTED) {
            linkProperties = sender.getLinkProperties(apnType);
            networkCapabilities = sender.getNetworkCapabilities(apnType);
        }
        ServiceState ss = sender.getServiceState();
        if (ss != null) {
            roaming = ss.getDataRoaming();
        }
        if (telephony != null) {
            networkType = telephony.getDataNetworkType(subId);
        } else {
            networkType = 0;
        }
        try {
            if (this.mRegistry != null && (sender.getActiveApnHost(apnType) != null || "default".equals(apnType) || "emergency".equals(apnType))) {
                this.mRegistry.notifyDataConnectionForSubscriber(subId, convertDataState(state), sender.isDataConnectivityPossible(apnType), reason, sender.getActiveApnHost(apnType), apnType, linkProperties, networkCapabilities, networkType, roaming);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyDataConnectionFailed(Phone sender, String reason, String apnType) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyDataConnectionFailedForSubscriber(subId, reason, apnType);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyCellLocation(Phone sender) {
        int subId = sender.getSubId();
        Bundle data = new Bundle();
        sender.getCellLocation().fillInNotifierBundle(data);
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCellLocationForSubscriber(subId, data);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyCellInfo(Phone sender, List<CellInfo> cellInfo) {
        int subId = sender.getSubId();
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyCellInfoForSubscriber(subId, cellInfo);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyOtaspChanged(Phone sender, int otaspMode) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyOtaspChanged(otaspMode);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyPreciseCallState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        Call foregroundCall = sender.getForegroundCall();
        Call backgroundCall = sender.getBackgroundCall();
        if (ringingCall != null && foregroundCall != null && backgroundCall != null) {
            try {
                this.mRegistry.notifyPreciseCallState(convertPreciseCallState(ringingCall.getState()), convertPreciseCallState(foregroundCall.getState()), convertPreciseCallState(backgroundCall.getState()));
            } catch (RemoteException e) {
            }
        }
    }

    public void notifyDisconnectCause(int cause, int preciseCause) {
        try {
            this.mRegistry.notifyDisconnectCause(cause, preciseCause);
        } catch (RemoteException e) {
        }
    }

    public void notifyPreciseDataConnectionFailed(Phone sender, String reason, String apnType, String apn, String failCause) {
        try {
            this.mRegistry.notifyPreciseDataConnectionFailed(reason, apnType, apn, failCause);
        } catch (RemoteException e) {
        }
    }

    public void notifyVoLteServiceStateChanged(Phone sender, VoLteServiceState lteState) {
        try {
            this.mRegistry.notifyVoLteServiceStateChanged(lteState);
        } catch (RemoteException e) {
        }
    }

    public void notifyOemHookRawEventForSubscriber(int subId, byte[] rawData) {
        try {
            this.mRegistry.notifyOemHookRawEventForSubscriber(subId, rawData);
        } catch (RemoteException e) {
        }
    }

    public void notifyLteAccessStratumChanged(Phone sender, String state) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyLteAccessStratumChanged(state);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifyPsNetworkTypeChanged(Phone sender, int nwType) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifyPsNetworkTypeChanged(nwType);
            }
        } catch (RemoteException e) {
        }
    }

    public void notifySharedDefaultApnStateChanged(Phone sender, boolean isSharedDefaultApn) {
        try {
            if (this.mRegistry != null) {
                this.mRegistry.notifySharedDefaultApnStateChanged(isSharedDefaultApn);
            }
        } catch (RemoteException e) {
        }
    }

    public static int convertCallState(PhoneConstants.State state) {
        switch (m13x3549e7c4()[state.ordinal()]) {
            case 1:
                return 2;
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    public static PhoneConstants.State convertCallState(int state) {
        switch (state) {
            case 1:
                return PhoneConstants.State.RINGING;
            case 2:
                return PhoneConstants.State.OFFHOOK;
            default:
                return PhoneConstants.State.IDLE;
        }
    }

    public static int convertDataState(DataState state) {
        switch (m12x9dac0c3a()[state.ordinal()]) {
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
                return 3;
            default:
                return 0;
        }
    }

    public static DataState convertDataState(int state) {
        switch (state) {
            case 1:
                return DataState.CONNECTING;
            case 2:
                return DataState.CONNECTED;
            case 3:
                return DataState.SUSPENDED;
            default:
                return DataState.DISCONNECTED;
        }
    }

    public static int convertDataActivityState(DataActivityState state) {
        switch (m14x2c473d58()[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 4;
            default:
                return 0;
        }
    }

    public static int convertPreciseCallState(State state) {
        switch (m11-getcom-android-internal-telephony-Call$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 7;
            case 5:
                return 8;
            case 6:
                return 2;
            case 7:
                return 5;
            case 8:
                return 6;
            default:
                return 0;
        }
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
