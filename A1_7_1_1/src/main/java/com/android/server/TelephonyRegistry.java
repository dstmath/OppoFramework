package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PreciseCallState;
import android.telephony.PreciseDataConnectionState;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import android.text.TextUtils;
import android.text.format.Time;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.DefaultPhoneNotifier;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.IPhoneStateListener;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.am.OppoPhoneStateReceiver;
import com.android.server.oppo.IElsaManager;
import com.android.server.policy.PhoneWindowManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class TelephonyRegistry extends Stub {
    private static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    static final int CHECK_PHONE_STATE_PERMISSION_MASK = 224;
    private static final boolean DBG = true;
    private static final boolean DBG_LOC = false;
    static final int ENFORCE_PHONE_STATE_PERMISSION_MASK = 16396;
    private static final int MSG_UPDATE_DEFAULT_SUB = 2;
    private static final int MSG_UPDATE_PHONE_SUB_ID_MAPPING = 3;
    private static final int MSG_USER_SWITCHED = 1;
    static final int PRECISE_PHONE_STATE_PERMISSION_MASK = 6144;
    private static final boolean SDBG = false;
    private static final String TAG = "TelephonyRegistry";
    private static final int TEL_DBG = 0;
    private static final boolean VDBG = false;
    private boolean hasNotifySubscriptionInfoChangedOccurred;
    private LogSSC[] logSSC;
    private final AppOpsManager mAppOps;
    private int mBackgroundCallState;
    private final IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver;
    private boolean[] mCallForwarding;
    private String[] mCallIncomingNumber;
    private int[] mCallState;
    private boolean mCarrierNetworkChangeState;
    private ArrayList<List<CellInfo>> mCellInfo;
    private Bundle[] mCellLocation;
    private ArrayList<String>[] mConnectedApns;
    private final Context mContext;
    private int[] mDataActivity;
    private String[] mDataConnectionApn;
    private LinkProperties[] mDataConnectionLinkProperties;
    private NetworkCapabilities[] mDataConnectionNetworkCapabilities;
    private int[] mDataConnectionNetworkType;
    private boolean[] mDataConnectionPossible;
    private String[] mDataConnectionReason;
    private int[] mDataConnectionState;
    private int mDefaultPhoneId;
    private int mDefaultSubId;
    private int mForegroundCallState;
    private final Handler mHandler;
    private boolean[] mMessageWaiting;
    private int mNumPhones;
    private int mOtaspMode;
    private PreciseCallState mPreciseCallState;
    private PreciseDataConnectionState[] mPreciseDataConnectionState;
    private final ArrayList<Record> mRecords;
    private final ArrayList<IBinder> mRemoveList;
    private int mRingingCallState;
    private ServiceState[] mServiceState;
    private SignalStrength[] mSignalStrength;
    private int[] mSubIdMapping;
    private VoLteServiceState mVoLteServiceState;
    private int next;

    private static class LogSSC {
        private int mPhoneId;
        private String mS;
        private ServiceState mState;
        private int mSubId;
        private Time mTime;

        /* synthetic */ LogSSC(LogSSC logSSC) {
            this();
        }

        private LogSSC() {
        }

        public void set(Time t, String s, int subId, int phoneId, ServiceState state) {
            this.mTime = t;
            this.mS = s;
            this.mSubId = subId;
            this.mPhoneId = phoneId;
            this.mState = state;
        }

        public String toString() {
            return this.mS + " Time " + this.mTime.toString() + " mSubId " + this.mSubId + " mPhoneId " + this.mPhoneId + "  mState " + this.mState;
        }
    }

    private static class Record implements DeathRecipient {
        IBinder binder;
        IPhoneStateListener callback;
        int callerUserId;
        String callingPackage;
        boolean canReadPhoneState;
        int events;
        private WeakReference<TelephonyRegistry> mTeleReg;
        IOnSubscriptionsChangedListener onSubscriptionsChangedListenerCallback;
        int phoneId = -1;
        int subId = -1;

        public void binderDied() {
            TelephonyRegistry mTel = (TelephonyRegistry) this.mTeleReg.get();
            if (mTel != null) {
                TelephonyRegistry.log("listen: remove record " + mTel.mRecords.size());
                synchronized (mTel.mRecords) {
                    mTel.mRemoveList.add(this.binder);
                    mTel.handleRemoveListLocked();
                }
            }
        }

        public Record(TelephonyRegistry teleReg) {
            this.mTeleReg = new WeakReference(teleReg);
        }

        boolean matchPhoneStateListenerEvent(int events) {
            return (this.callback == null || (this.events & events) == 0) ? false : true;
        }

        boolean matchOnSubscriptionsChangedListener() {
            return this.onSubscriptionsChangedListenerCallback != null;
        }

        public String toString() {
            return "{callingPackage=" + this.callingPackage + " binder=" + this.binder + " callback=" + this.callback + " onSubscriptionsChangedListenererCallback=" + this.onSubscriptionsChangedListenerCallback + " callerUserId=" + this.callerUserId + " subId=" + this.subId + " phoneId=" + this.phoneId + " events=" + Integer.toHexString(this.events) + " canReadPhoneState=" + this.canReadPhoneState + "}";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.TelephonyRegistry.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.TelephonyRegistry.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.TelephonyRegistry.<clinit>():void");
    }

    TelephonyRegistry(Context context) {
        int i;
        this.mRemoveList = new ArrayList();
        this.mRecords = new ArrayList();
        this.hasNotifySubscriptionInfoChangedOccurred = false;
        this.mOtaspMode = 1;
        this.mCellInfo = null;
        this.mVoLteServiceState = new VoLteServiceState();
        this.mDefaultSubId = -1;
        this.mDefaultPhoneId = -1;
        this.mRingingCallState = 0;
        this.mForegroundCallState = 0;
        this.mBackgroundCallState = 0;
        this.mPreciseCallState = new PreciseCallState();
        this.mCarrierNetworkChangeState = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (TelephonyRegistry.VDBG) {
                            TelephonyRegistry.log("MSG_USER_SWITCHED userId=" + msg.arg1);
                        }
                        int numPhones = TelephonyManager.getDefault().getPhoneCount();
                        for (int sub = 0; sub < numPhones; sub++) {
                            TelephonyRegistry.this.notifyCellLocationForSubscriber(sub, TelephonyRegistry.this.mCellLocation[sub]);
                        }
                        return;
                    case 2:
                        int newDefaultPhoneId = msg.arg1;
                        int newDefaultSubId = ((Integer) msg.obj).intValue();
                        if (TelephonyRegistry.VDBG) {
                            TelephonyRegistry.log("MSG_UPDATE_DEFAULT_SUB:current mDefaultSubId=" + TelephonyRegistry.this.mDefaultSubId + " current mDefaultPhoneId=" + TelephonyRegistry.this.mDefaultPhoneId + " newDefaultSubId= " + newDefaultSubId + " newDefaultPhoneId=" + newDefaultPhoneId);
                        }
                        synchronized (TelephonyRegistry.this.mRecords) {
                            for (Record r : TelephonyRegistry.this.mRecords) {
                                if (r.subId == Integer.MAX_VALUE) {
                                    TelephonyRegistry.this.checkPossibleMissNotify(r, newDefaultPhoneId);
                                }
                            }
                            TelephonyRegistry.this.handleRemoveListLocked();
                        }
                        TelephonyRegistry.this.mDefaultSubId = newDefaultSubId;
                        TelephonyRegistry.this.mDefaultPhoneId = newDefaultPhoneId;
                        return;
                    case 3:
                        TelephonyRegistry.this.onUpdatePhoneSubIdMapping();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TelephonyRegistry.VDBG) {
                    TelephonyRegistry.log("mBroadcastReceiver: action=" + action);
                }
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    int userHandle = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    TelephonyRegistry.log("onReceive: userHandle=" + userHandle);
                    TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(1, userHandle, 0));
                } else if (action.equals("android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED")) {
                    Integer newDefaultSubIdObj = new Integer(intent.getIntExtra("subscription", SubscriptionManager.getDefaultSubscriptionId()));
                    int newDefaultPhoneId = intent.getIntExtra("slot", SubscriptionManager.getPhoneId(TelephonyRegistry.this.mDefaultSubId));
                    TelephonyRegistry.log("onReceive:current mDefaultSubId=" + TelephonyRegistry.this.mDefaultSubId + " current mDefaultPhoneId=" + TelephonyRegistry.this.mDefaultPhoneId + " newDefaultSubId= " + newDefaultSubIdObj + " newDefaultPhoneId=" + newDefaultPhoneId);
                    if (!TelephonyRegistry.this.validatePhoneId(newDefaultPhoneId)) {
                        return;
                    }
                    if (!newDefaultSubIdObj.equals(Integer.valueOf(TelephonyRegistry.this.mDefaultSubId)) || newDefaultPhoneId != TelephonyRegistry.this.mDefaultPhoneId) {
                        TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(2, newDefaultPhoneId, 0, newDefaultSubIdObj));
                    }
                } else if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED")) {
                    int detectedType = intent.getIntExtra("simDetectStatus", 4);
                    TelephonyRegistry.log("TelephonyRegistryonReceive: ACTION_SUBINFO_RECORD_UPDATED, detectedType:" + detectedType);
                    if (detectedType != 4) {
                        TelephonyRegistry.this.mHandler.sendMessage(TelephonyRegistry.this.mHandler.obtainMessage(3, 0, 0));
                    }
                } else if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
                    TelephonyRegistry.log("TelephonyRegistryonReceive: ACTION_SHUTDOWN_IPO, clean cfi.");
                    for (int i = 0; i < TelephonyRegistry.this.mCallForwarding.length; i++) {
                        TelephonyRegistry.this.mCallForwarding[i] = false;
                    }
                }
            }
        };
        this.logSSC = new LogSSC[1];
        this.next = 0;
        CellLocation location = CellLocation.getEmpty();
        this.mContext = context;
        this.mBatteryStats = BatteryStatsService.getService();
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        log("TelephonyRegistor: ctor numPhones=" + numPhones);
        this.mNumPhones = numPhones;
        this.mConnectedApns = new ArrayList[numPhones];
        this.mCallState = new int[numPhones];
        this.mDataActivity = new int[numPhones];
        this.mDataConnectionState = new int[numPhones];
        this.mDataConnectionNetworkType = new int[numPhones];
        this.mCallIncomingNumber = new String[numPhones];
        this.mServiceState = new ServiceState[numPhones];
        this.mSignalStrength = new SignalStrength[numPhones];
        this.mMessageWaiting = new boolean[numPhones];
        this.mDataConnectionPossible = new boolean[numPhones];
        this.mDataConnectionReason = new String[numPhones];
        this.mDataConnectionApn = new String[numPhones];
        this.mCallForwarding = new boolean[numPhones];
        this.mCellLocation = new Bundle[numPhones];
        this.mDataConnectionLinkProperties = new LinkProperties[numPhones];
        this.mDataConnectionNetworkCapabilities = new NetworkCapabilities[numPhones];
        this.mCellInfo = new ArrayList();
        this.mSubIdMapping = new int[numPhones];
        this.mPreciseDataConnectionState = new PreciseDataConnectionState[numPhones];
        for (i = 0; i < numPhones; i++) {
            this.mConnectedApns[i] = new ArrayList();
            this.mCallState[i] = 0;
            this.mDataActivity[i] = 0;
            this.mDataConnectionState[i] = -1;
            this.mCallIncomingNumber[i] = IElsaManager.EMPTY_PACKAGE;
            this.mServiceState[i] = new ServiceState();
            this.mSignalStrength[i] = new SignalStrength();
            this.mMessageWaiting[i] = false;
            this.mCallForwarding[i] = false;
            this.mDataConnectionPossible[i] = false;
            this.mDataConnectionReason[i] = IElsaManager.EMPTY_PACKAGE;
            this.mDataConnectionApn[i] = IElsaManager.EMPTY_PACKAGE;
            this.mCellLocation[i] = new Bundle();
            this.mCellInfo.add(i, null);
            this.mSubIdMapping[i] = SubscriptionManager.getSubIdUsingPhoneId(i);
            this.mPreciseDataConnectionState[i] = new PreciseDataConnectionState();
        }
        if (location != null) {
            for (i = 0; i < numPhones; i++) {
                location.fillInNotifierBundle(this.mCellLocation[i]);
            }
        }
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
    }

    public void systemRunning() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        log("systemRunning register for intents");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void addOnSubscriptionsChangedListener(String callingPackage, IOnSubscriptionsChangedListener callback) {
        int callerUserId = UserHandle.getCallingUserId();
        if (VDBG) {
            log("listen oscl: E pkg=" + callingPackage + " myUserId=" + UserHandle.myUserId() + " callerUserId=" + callerUserId + " callback=" + callback + " callback.asBinder=" + callback.asBinder());
        }
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", "addOnSubscriptionsChangedListener");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE, "addOnSubscriptionsChangedListener");
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                return;
            }
        }
        synchronized (this.mRecords) {
            Record r;
            IBinder b = callback.asBinder();
            int N = this.mRecords.size();
            for (int i = 0; i < N; i++) {
                r = (Record) this.mRecords.get(i);
                if (b == r.binder) {
                    break;
                }
            }
            try {
                r = new Record(this);
                r.binder = b;
                r.binder.linkToDeath(r, 0);
                this.mRecords.add(r);
                log("listen oscl: add new record");
                r.onSubscriptionsChangedListenerCallback = callback;
                r.callingPackage = callingPackage;
                r.callerUserId = callerUserId;
                r.events = 0;
                r.canReadPhoneState = true;
                log("listen oscl:  Register r=" + r);
                if (this.hasNotifySubscriptionInfoChangedOccurred) {
                    try {
                        if (VDBG) {
                            log("listen oscl: send to r=" + r);
                        }
                        r.onSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                        if (VDBG) {
                            log("listen oscl: sent to r=" + r);
                        }
                    } catch (RemoteException e2) {
                        if (VDBG) {
                            log("listen oscl: remote exception sending to r=" + r + " e=" + e2);
                        }
                        remove(r.binder);
                    }
                } else {
                    log("listen oscl: hasNotifySubscriptionInfoChangedOccurred==false no callback");
                }
            } catch (RemoteException e22) {
                e22.printStackTrace();
            }
        }
    }

    public void removeOnSubscriptionsChangedListener(String pkgForDebug, IOnSubscriptionsChangedListener callback) {
        log("listen oscl: Unregister");
        remove(callback.asBinder());
    }

    public void notifySubscriptionInfoChanged() {
        synchronized (this.mRecords) {
            if (!this.hasNotifySubscriptionInfoChangedOccurred) {
                log("notifySubscriptionInfoChanged: first invocation mRecords.size=" + this.mRecords.size());
            }
            this.hasNotifySubscriptionInfoChangedOccurred = true;
            this.mRemoveList.clear();
            for (Record r : this.mRecords) {
                if (r.matchOnSubscriptionsChangedListener()) {
                    try {
                        r.onSubscriptionsChangedListenerCallback.onSubscriptionsChanged();
                        if (VDBG) {
                            log("notifySubscriptionInfoChanged: done osc to r=" + r);
                        }
                    } catch (RemoteException e) {
                        if (VDBG) {
                            log("notifySubscriptionInfoChanged: RemoteException r=" + r);
                        }
                        this.mRemoveList.add(r.binder);
                    }
                }
            }
            handleRemoveListLocked();
        }
    }

    public void listen(String pkgForDebug, IPhoneStateListener callback, int events, boolean notifyNow) {
        listenForSubscriber(Integer.MAX_VALUE, pkgForDebug, callback, events, notifyNow);
    }

    public void listenForSubscriber(int subId, String pkgForDebug, IPhoneStateListener callback, int events, boolean notifyNow) {
        listen(pkgForDebug, callback, events, notifyNow, subId);
    }

    private void listen(String callingPackage, IPhoneStateListener callback, int events, boolean notifyNow, int subId) {
        int callerUserId = UserHandle.getCallingUserId();
        if (VDBG) {
            log("listen: E pkg=" + callingPackage + " events=0x" + Integer.toHexString(events) + " notifyNow=" + notifyNow + " subId=" + subId + " myUserId=" + UserHandle.myUserId() + " callerUserId=" + callerUserId);
        }
        if (events != 0) {
            checkListenerPermission(events);
            if ((events & ENFORCE_PHONE_STATE_PERMISSION_MASK) != 0) {
                try {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
                } catch (SecurityException e) {
                    if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                        return;
                    }
                }
            }
            synchronized (this.mRecords) {
                Record r;
                HashMap<Integer, Integer> map = new HashMap();
                IBinder b = callback.asBinder();
                int N = this.mRecords.size();
                for (int i = 0; i < N; i++) {
                    r = (Record) this.mRecords.get(i);
                    if (b == r.binder) {
                        break;
                    }
                }
                try {
                    r = new Record(this);
                    r.binder = b;
                    r.binder.linkToDeath(r, 0);
                    this.mRecords.add(r);
                    log("listen: add new record");
                    r.callback = callback;
                    r.callingPackage = callingPackage;
                    r.callerUserId = callerUserId;
                    r.canReadPhoneState = (events & 16620) != 0 ? canReadPhoneState(callingPackage) : false;
                    if (SubscriptionManager.isValidSubscriptionId(subId)) {
                        r.subId = subId;
                    } else {
                        r.subId = Integer.MAX_VALUE;
                    }
                    r.phoneId = oemGetPhoneId(map, r.subId);
                    int phoneId = r.phoneId;
                    r.events = events;
                    log("listen:  Register r=" + r + " r.subId=" + r.subId + " phoneId=" + phoneId);
                    if (VDBG) {
                        toStringLogSSC("listen");
                    }
                    if (notifyNow && validatePhoneId(phoneId)) {
                        if ((events & 1) != 0) {
                            try {
                                if (VDBG) {
                                    log("listen: call onSSC state=" + this.mServiceState[phoneId]);
                                }
                                r.callback.onServiceStateChanged(new ServiceState(this.mServiceState[phoneId]));
                            } catch (RemoteException e2) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 2) != 0) {
                            try {
                                int gsmSignalStrength = this.mSignalStrength[phoneId].getGsmSignalStrength();
                                IPhoneStateListener iPhoneStateListener = r.callback;
                                if (gsmSignalStrength == 99) {
                                    gsmSignalStrength = -1;
                                }
                                iPhoneStateListener.onSignalStrengthChanged(gsmSignalStrength);
                            } catch (RemoteException e3) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 4) != 0) {
                            try {
                                r.callback.onMessageWaitingIndicatorChanged(this.mMessageWaiting[phoneId]);
                            } catch (RemoteException e4) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 8) != 0) {
                            try {
                                log("listen: mCallForwarding = " + this.mCallForwarding[phoneId] + ", phoneid: " + phoneId);
                                r.callback.onCallForwardingIndicatorChanged(this.mCallForwarding[phoneId]);
                            } catch (RemoteException e5) {
                                remove(r.binder);
                            }
                        }
                        if (validateEventsAndUserLocked(r, 16)) {
                            try {
                                r.callback.onCellLocationChanged(new Bundle(this.mCellLocation[phoneId]));
                            } catch (RemoteException e6) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 32) != 0) {
                            try {
                                r.callback.onCallStateChanged(this.mCallState[phoneId], getCallIncomingNumber(r, phoneId));
                            } catch (RemoteException e7) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 64) != 0) {
                            try {
                                r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
                            } catch (RemoteException e8) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 128) != 0) {
                            try {
                                r.callback.onDataActivity(this.mDataActivity[phoneId]);
                            } catch (RemoteException e9) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 256) != 0) {
                            try {
                                r.callback.onSignalStrengthsChanged(this.mSignalStrength[phoneId]);
                            } catch (RemoteException e10) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 512) != 0) {
                            try {
                                r.callback.onOtaspChanged(this.mOtaspMode);
                            } catch (RemoteException e11) {
                                remove(r.binder);
                            }
                        }
                        if (validateEventsAndUserLocked(r, 1024)) {
                            try {
                                r.callback.onCellInfoChanged((List) this.mCellInfo.get(phoneId));
                            } catch (RemoteException e12) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 2048) != 0) {
                            try {
                                r.callback.onPreciseCallStateChanged(this.mPreciseCallState);
                            } catch (RemoteException e13) {
                                remove(r.binder);
                            }
                        }
                        if ((events & 4096) != 0) {
                            try {
                                r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState[phoneId]);
                            } catch (RemoteException e14) {
                                remove(r.binder);
                            }
                        }
                        if ((DumpState.DUMP_INSTALLS & events) != 0) {
                            try {
                                r.callback.onCarrierNetworkChange(this.mCarrierNetworkChangeState);
                            } catch (RemoteException e15) {
                                remove(r.binder);
                            }
                        }
                    }
                    map.clear();
                } catch (RemoteException e16) {
                    e16.printStackTrace();
                    return;
                }
            }
        }
        log("listen: Unregister");
        remove(callback.asBinder());
    }

    private boolean canReadPhoneState(String callingPackage) {
        boolean canReadPhoneState = true;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0) {
            return true;
        }
        if (this.mContext.checkCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE) != 0) {
            canReadPhoneState = false;
        }
        if (!canReadPhoneState || this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) == 0) {
            return canReadPhoneState;
        }
        return false;
    }

    private String getCallIncomingNumber(Record record, int phoneId) {
        return record.canReadPhoneState ? this.mCallIncomingNumber[phoneId] : IElsaManager.EMPTY_PACKAGE;
    }

    private void remove(IBinder binder) {
        synchronized (this.mRecords) {
            int recordCount = this.mRecords.size();
            for (int i = 0; i < recordCount; i++) {
                if (((Record) this.mRecords.get(i)).binder == binder) {
                    Record r = (Record) this.mRecords.get(i);
                    r.binder.unlinkToDeath(r, 0);
                    log("remove: binder=" + binder + "r.callingPackage" + r.callingPackage + "r.callback" + r.callback);
                    this.mRecords.remove(i);
                    return;
                }
            }
        }
    }

    public void notifyCallState(int state, String incomingNumber) {
        if (checkNotifyPermission("notifyCallState()")) {
            if (VDBG) {
                log("notifyCallState: state=" + state + " incomingNumber=" + incomingNumber);
            }
            synchronized (this.mRecords) {
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(32) && r.subId == Integer.MAX_VALUE) {
                        try {
                            r.callback.onCallStateChanged(state, r.canReadPhoneState ? incomingNumber : IElsaManager.EMPTY_PACKAGE);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastCallStateChanged(state, incomingNumber, -1, -1);
        }
    }

    public void notifyCallStateForPhoneId(int phoneId, int subId, int state, String incomingNumber) {
        if (checkNotifyPermission("notifyCallState()")) {
            if (VDBG) {
                log("notifyCallStateForPhoneId: subId=" + subId + " state=" + state + " incomingNumber=" + incomingNumber);
            }
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCallState[phoneId] = state;
                    this.mCallIncomingNumber[phoneId] = incomingNumber;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(32) && r.subId == subId && r.subId != Integer.MAX_VALUE) {
                            try {
                                r.callback.onCallStateChanged(state, getCallIncomingNumber(r, phoneId));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastCallStateChanged(state, incomingNumber, phoneId, subId);
        }
    }

    public void notifyCallStateForPhoneInfo(int phoneType, int phoneId, int subId, int state, String incomingNumber) {
        if (checkNotifyPermission("notifyCallState()")) {
            if (VDBG) {
                log("notifyCallStateForPhoneInfo: phoneType=" + phoneType + " phoneId =" + phoneId + " subId=" + subId + " state=" + state + " incomingNumber=" + Rlog.pii(SDBG, incomingNumber));
            }
            synchronized (this.mRecords) {
                if (validatePhoneId(phoneId)) {
                    this.mCallState[phoneId] = state;
                    this.mCallIncomingNumber[phoneId] = incomingNumber;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(32) && r.subId == subId && r.subId != Integer.MAX_VALUE) {
                            try {
                                r.callback.onCallStateChanged(state, getCallIncomingNumber(r, phoneId));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastCallStateChangedForPhoneInfo(phoneType, state, incomingNumber, phoneId, subId);
        }
    }

    public void notifyServiceStateForPhoneId(int phoneId, int subId, ServiceState state) {
        if (checkNotifyPermission("notifyServiceState()")) {
            synchronized (this.mRecords) {
                if (VDBG) {
                    log("notifyServiceStateForSubscriber: subId=" + subId + " phoneId=" + phoneId + " state=" + state);
                }
                HashMap<Integer, Integer> map = new HashMap();
                if (validatePhoneId(phoneId)) {
                    this.mServiceState[phoneId] = state;
                    logServiceStateChanged("notifyServiceStateForSubscriber", subId, phoneId, state);
                    if (VDBG) {
                        toStringLogSSC("notifyServiceStateForSubscriber");
                    }
                    HashMap<Integer, Integer> SubPhoneMap = new HashMap();
                    for (Record r : this.mRecords) {
                        Integer rPhoneId = (Integer) SubPhoneMap.get(Integer.valueOf(r.subId));
                        if (rPhoneId == null) {
                            rPhoneId = Integer.valueOf(SubscriptionManager.getPhoneId(r.subId));
                            log("Get rPhoneId: " + rPhoneId + " for rSubId: " + r.subId);
                            SubPhoneMap.put(Integer.valueOf(r.subId), rPhoneId);
                        }
                        if (r.matchPhoneStateListenerEvent(1) && idMatchForNetwork(r.subId, subId, rPhoneId.intValue(), phoneId)) {
                            try {
                                log("notifyServiceStateForSubscriber: callback.onSSC r=" + r + " subId=" + subId + " phoneId=" + phoneId + " state=" + state);
                                r.callback.onServiceStateChanged(new ServiceState(state));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                } else {
                    log("notifyServiceStateForSubscriber: INVALID phoneId=" + phoneId);
                }
                handleRemoveListLocked();
                map.clear();
            }
            broadcastServiceStateChanged(state, phoneId, subId);
        }
    }

    public void notifySignalStrengthForPhoneId(int phoneId, int subId, SignalStrength signalStrength) {
        if (checkNotifyPermission("notifySignalStrength()")) {
            if (VDBG) {
                log("notifySignalStrengthForPhoneId: subId=" + subId + " phoneId=" + phoneId + " signalStrength=" + signalStrength);
                toStringLogSSC("notifySignalStrengthForPhoneId");
            }
            synchronized (this.mRecords) {
                HashMap<Integer, Integer> map = new HashMap();
                phoneId = oemGetPhoneId(map, subId);
                if (validatePhoneId(phoneId)) {
                    if (VDBG) {
                        log("notifySignalStrengthForPhoneId: valid phoneId=" + phoneId);
                    }
                    this.mSignalStrength[phoneId] = signalStrength;
                    HashMap<Integer, Integer> SubPhoneMap = new HashMap();
                    for (Record r : this.mRecords) {
                        Integer rPhoneId = (Integer) SubPhoneMap.get(Integer.valueOf(r.subId));
                        if (rPhoneId == null) {
                            rPhoneId = Integer.valueOf(SubscriptionManager.getPhoneId(r.subId));
                            log("Get rPhoneId: " + rPhoneId.intValue() + " for rSubId: " + r.subId);
                            SubPhoneMap.put(Integer.valueOf(r.subId), rPhoneId);
                        }
                        if (r.matchPhoneStateListenerEvent(256) && idMatchForNetwork(r.subId, subId, rPhoneId.intValue(), phoneId)) {
                            try {
                                log("notifySignalStrengthForPhoneId: callback.onSsS r=" + r + " subId=" + subId + " phoneId=" + phoneId + " ss=" + signalStrength);
                                r.callback.onSignalStrengthsChanged(new SignalStrength(signalStrength));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                        if (r.matchPhoneStateListenerEvent(2) && idMatchForNetwork(r.subId, subId, rPhoneId.intValue(), phoneId)) {
                            try {
                                int gsmSignalStrength = signalStrength.getGsmSignalStrength();
                                int ss = gsmSignalStrength == 99 ? -1 : gsmSignalStrength;
                                log("notifySignalStrengthForPhoneId: callback.onSS r=" + r + " subId=" + subId + " phoneId=" + phoneId + " gsmSS=" + gsmSignalStrength + " ss=" + ss);
                                r.callback.onSignalStrengthChanged(ss);
                            } catch (RemoteException e2) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                } else {
                    log("notifySignalStrengthForPhoneId: invalid phoneId=" + phoneId);
                }
                handleRemoveListLocked();
                map.clear();
            }
            broadcastSignalStrengthChanged(signalStrength, phoneId, subId);
        }
    }

    public void notifyCarrierNetworkChange(boolean active) {
        enforceNotifyPermissionOrCarrierPrivilege("notifyCarrierNetworkChange()");
        if (VDBG) {
            log("notifyCarrierNetworkChange: active=" + active);
        }
        synchronized (this.mRecords) {
            this.mCarrierNetworkChangeState = active;
            for (Record r : this.mRecords) {
                if (r.matchPhoneStateListenerEvent(DumpState.DUMP_INSTALLS)) {
                    try {
                        r.callback.onCarrierNetworkChange(active);
                    } catch (RemoteException e) {
                        this.mRemoveList.add(r.binder);
                    }
                }
            }
            handleRemoveListLocked();
        }
    }

    public void notifyCellInfo(List<CellInfo> cellInfo) {
        notifyCellInfoForSubscriber(Integer.MAX_VALUE, cellInfo);
    }

    public void notifyCellInfoForSubscriber(int subId, List<CellInfo> cellInfo) {
        if (checkNotifyPermission("notifyCellInfo()")) {
            if (VDBG) {
                log("notifyCellInfoForSubscriber: subId=" + subId + " cellInfo=" + cellInfo);
            }
            synchronized (this.mRecords) {
                HashMap<Integer, Integer> map = new HashMap();
                int phoneId = oemGetPhoneId(map, subId);
                if (validatePhoneId(phoneId)) {
                    this.mCellInfo.set(phoneId, cellInfo);
                    HashMap<Integer, Integer> SubPhoneMap = new HashMap();
                    for (Record r : this.mRecords) {
                        Integer rPhoneId = (Integer) SubPhoneMap.get(Integer.valueOf(r.subId));
                        if (rPhoneId == null) {
                            rPhoneId = Integer.valueOf(SubscriptionManager.getPhoneId(r.subId));
                            log("Get rPhoneId: " + rPhoneId.intValue() + " for rSubId: " + r.subId);
                            SubPhoneMap.put(Integer.valueOf(r.subId), rPhoneId);
                        }
                        if (validateEventsAndUserLocked(r, 1024) && idMatchForNetwork(r.subId, subId, rPhoneId.intValue(), phoneId)) {
                            try {
                                r.callback.onCellInfoChanged(cellInfo);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
                map.clear();
            }
        }
    }

    public void notifyMessageWaitingChangedForPhoneId(int phoneId, int subId, boolean mwi) {
        if (checkNotifyPermission("notifyMessageWaitingChanged()")) {
            if (VDBG) {
                log("notifyMessageWaitingChangedForSubscriberPhoneID: subId=" + phoneId + " mwi=" + mwi);
            }
            synchronized (this.mRecords) {
                HashMap<Integer, Integer> map = new HashMap();
                if (validatePhoneId(phoneId)) {
                    this.mMessageWaiting[phoneId] = mwi;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(4) && oemIdMatch(map, r.subId, subId)) {
                            try {
                                r.callback.onMessageWaitingIndicatorChanged(mwi);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
                map.clear();
            }
        }
    }

    public void notifyCallForwardingChanged(boolean cfi) {
        notifyCallForwardingChangedForSubscriber(Integer.MAX_VALUE, cfi);
    }

    public void notifyCallForwardingChangedForSubscriber(int subId, boolean cfi) {
        if (checkNotifyPermission("notifyCallForwardingChanged()")) {
            if (VDBG) {
                log("notifyCallForwardingChangedForSubscriber: subId=" + subId + " cfi=" + cfi);
            }
            synchronized (this.mRecords) {
                HashMap<Integer, Integer> map = new HashMap();
                int phoneId = oemGetPhoneId(map, subId);
                if (validatePhoneId(phoneId)) {
                    this.mCallForwarding[phoneId] = cfi;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(8) && oemIdMatch(map, r.subId, subId)) {
                            try {
                                r.callback.onCallForwardingIndicatorChanged(cfi);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
                map.clear();
            }
        }
    }

    public void notifyDataActivity(int state) {
        notifyDataActivityForSubscriber(Integer.MAX_VALUE, state);
    }

    public void notifyDataActivityForSubscriber(int subId, int state) {
        if (checkNotifyPermission("notifyDataActivity()")) {
            synchronized (this.mRecords) {
                int phoneId = SubscriptionManager.getPhoneId(subId);
                if (validatePhoneId(phoneId)) {
                    this.mDataActivity[phoneId] = state;
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(128) && idMatch(r.subId, subId)) {
                            try {
                                r.callback.onDataActivity(state);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifyDataConnection(int state, boolean isDataConnectivityPossible, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int networkType, boolean roaming) {
        notifyDataConnectionForSubscriber(Integer.MAX_VALUE, state, isDataConnectivityPossible, reason, apn, apnType, linkProperties, networkCapabilities, networkType, roaming);
    }

    public void notifyDataConnectionForSubscriber(int subId, int state, boolean isDataConnectivityPossible, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int networkType, boolean roaming) {
        if (checkNotifyPermission("notifyDataConnection()")) {
            if (VDBG) {
                log("notifyDataConnectionForSubscriber E: subId=" + subId + " state=" + state + " isDataConnectivityPossible=" + isDataConnectivityPossible + " reason='" + reason + "' apn='" + apn + "' apnType=" + apnType + " networkType=" + networkType + " mRecords.size()=" + this.mRecords.size());
            }
            synchronized (this.mRecords) {
                HashMap<Integer, Integer> map = new HashMap();
                int phoneId = getPhoneSubIdMapping(subId);
                if (validatePhoneId(phoneId)) {
                    boolean modified = false;
                    if (state == 2) {
                        if (!this.mConnectedApns[phoneId].contains(apnType)) {
                            this.mConnectedApns[phoneId].add(apnType);
                            if (!("ims".equals(apnType) || this.mDataConnectionState[phoneId] == state)) {
                                this.mDataConnectionState[phoneId] = state;
                                modified = true;
                            }
                        }
                    } else if (this.mConnectedApns[phoneId].remove(apnType)) {
                        if (this.mConnectedApns[phoneId].isEmpty()) {
                            this.mDataConnectionState[phoneId] = state;
                            modified = true;
                        } else if (this.mConnectedApns[phoneId].size() == 1 && this.mConnectedApns[phoneId].contains("ims")) {
                            this.mDataConnectionState[phoneId] = state;
                            modified = true;
                        }
                    }
                    this.mDataConnectionPossible[phoneId] = isDataConnectivityPossible;
                    this.mDataConnectionReason[phoneId] = reason;
                    this.mDataConnectionLinkProperties[phoneId] = linkProperties;
                    this.mDataConnectionNetworkCapabilities[phoneId] = networkCapabilities;
                    if (this.mDataConnectionNetworkType[phoneId] != networkType) {
                        this.mDataConnectionNetworkType[phoneId] = networkType;
                        modified = true;
                    }
                    logv("notifyDataConnectionForSubscriber: handle onDataConnectionStateChanged");
                    if (modified) {
                        log("onDataConnectionStateChanged(" + this.mDataConnectionState[phoneId] + ", " + this.mDataConnectionNetworkType[phoneId] + ")");
                        for (Record r : this.mRecords) {
                            if (r.matchPhoneStateListenerEvent(64)) {
                                if (oemIdMatch(map, r.subId, subId)) {
                                    try {
                                        log("Notify data connection state changed on sub: " + subId);
                                        if (VDBG) {
                                            log("onDataConnectionStateChanged E: PKG = " + r.callingPackage);
                                        }
                                        r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
                                        if (VDBG) {
                                            log("onDataConnectionStateChanged X");
                                        }
                                    } catch (RemoteException e) {
                                        this.mRemoveList.add(r.binder);
                                    }
                                } else {
                                    continue;
                                }
                            }
                        }
                        handleRemoveListLocked();
                    }
                    logv("notifyDataConnectionForSubscriber: handle PreciseDataConnectionState");
                    this.mPreciseDataConnectionState[phoneId] = new PreciseDataConnectionState(state, networkType, apnType, apn, reason, linkProperties, IElsaManager.EMPTY_PACKAGE);
                    for (Record r2 : this.mRecords) {
                        if (r2.matchPhoneStateListenerEvent(4096)) {
                            if (!oemIdMatch(map, r2.subId, subId)) {
                                if (!isEmergencyWithoutSim(r2.subId, subId, apnType)) {
                                    continue;
                                }
                            }
                            try {
                                if (VDBG) {
                                    log("onPreciseDataConnectionStateChanged E: PKG = " + r2.callingPackage);
                                }
                                r2.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState[phoneId]);
                                if (VDBG) {
                                    log("onDataConnectionStateChanged X");
                                }
                            } catch (RemoteException e2) {
                                this.mRemoveList.add(r2.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
                map.clear();
            }
            logv("notifyDataConnectionForSubscriber: broadcast intent");
            broadcastDataConnectionStateChanged(state, isDataConnectivityPossible, reason, apn, apnType, linkProperties, networkCapabilities, roaming, subId);
            broadcastPreciseDataConnectionStateChanged(state, networkType, apnType, apn, reason, linkProperties, IElsaManager.EMPTY_PACKAGE);
            logv("notifyDataConnectionForSubscriber X");
        }
    }

    public void notifyDataConnectionFailed(String reason, String apnType) {
        notifyDataConnectionFailedForSubscriber(Integer.MAX_VALUE, reason, apnType);
    }

    public void notifyDataConnectionFailedForSubscriber(int subId, String reason, String apnType) {
        if (checkNotifyPermission("notifyDataConnectionFailed()")) {
            int phoneId;
            if (VDBG) {
                log("notifyDataConnectionFailedForSubscriber: subId=" + subId + " reason=" + reason + " apnType=" + apnType);
            }
            synchronized (this.mRecords) {
                HashMap<Integer, Integer> map = new HashMap();
                phoneId = getPhoneSubIdMapping(subId);
                this.mPreciseDataConnectionState[phoneId] = new PreciseDataConnectionState(-1, 0, apnType, IElsaManager.EMPTY_PACKAGE, reason, null, IElsaManager.EMPTY_PACKAGE);
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(4096)) {
                        if (oemIdMatch(map, r.subId, subId)) {
                            try {
                                r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState[phoneId]);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        } else {
                            continue;
                        }
                    }
                }
                handleRemoveListLocked();
                map.clear();
            }
            broadcastDataConnectionFailed(reason, apnType, subId, phoneId);
            broadcastPreciseDataConnectionStateChanged(-1, 0, apnType, IElsaManager.EMPTY_PACKAGE, reason, null, IElsaManager.EMPTY_PACKAGE);
        }
    }

    public void notifyCellLocation(Bundle cellLocation) {
        notifyCellLocationForSubscriber(Integer.MAX_VALUE, cellLocation);
    }

    public void notifyCellLocationForSubscriber(int subId, Bundle cellLocation) {
        log("notifyCellLocationForSubscriber: subId=" + subId + " cellLocation=" + cellLocation);
        if (checkNotifyPermission("notifyCellLocation()")) {
            if (VDBG) {
                log("notifyCellLocationForSubscriber: subId=" + subId + " cellLocation=" + cellLocation);
            }
            synchronized (this.mRecords) {
                HashMap<Integer, Integer> map = new HashMap();
                int phoneId = oemGetPhoneId(map, subId);
                if (validatePhoneId(phoneId)) {
                    this.mCellLocation[phoneId] = cellLocation;
                    HashMap<Integer, Integer> SubPhoneMap = new HashMap();
                    for (Record r : this.mRecords) {
                        Integer rPhoneId = (Integer) SubPhoneMap.get(Integer.valueOf(r.subId));
                        if (rPhoneId == null) {
                            rPhoneId = Integer.valueOf(SubscriptionManager.getPhoneId(r.subId));
                            log("Get rPhoneId: " + rPhoneId.intValue() + " for rSubId: " + r.subId);
                            SubPhoneMap.put(Integer.valueOf(r.subId), rPhoneId);
                        }
                        if (validateEventsAndUserLocked(r, 16) && idMatchForNetwork(r.subId, subId, rPhoneId.intValue(), phoneId)) {
                            try {
                                r.callback.onCellLocationChanged(new Bundle(cellLocation));
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
                map.clear();
            }
        }
    }

    public void notifyOtaspChanged(int otaspMode) {
        if (checkNotifyPermission("notifyOtaspChanged()")) {
            synchronized (this.mRecords) {
                this.mOtaspMode = otaspMode;
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(512)) {
                        try {
                            r.callback.onOtaspChanged(otaspMode);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifyPreciseCallState(int ringingCallState, int foregroundCallState, int backgroundCallState) {
        if (checkNotifyPermission("notifyPreciseCallState()")) {
            synchronized (this.mRecords) {
                this.mRingingCallState = ringingCallState;
                this.mForegroundCallState = foregroundCallState;
                this.mBackgroundCallState = backgroundCallState;
                this.mPreciseCallState = new PreciseCallState(ringingCallState, foregroundCallState, backgroundCallState, -1, -1);
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(2048)) {
                        try {
                            r.callback.onPreciseCallStateChanged(this.mPreciseCallState);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastPreciseCallStateChanged(ringingCallState, foregroundCallState, backgroundCallState, -1, -1);
        }
    }

    public void notifyDisconnectCause(int disconnectCause, int preciseDisconnectCause) {
        if (checkNotifyPermission("notifyDisconnectCause()")) {
            synchronized (this.mRecords) {
                this.mPreciseCallState = new PreciseCallState(this.mRingingCallState, this.mForegroundCallState, this.mBackgroundCallState, disconnectCause, preciseDisconnectCause);
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(2048)) {
                        try {
                            r.callback.onPreciseCallStateChanged(this.mPreciseCallState);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastPreciseCallStateChanged(this.mRingingCallState, this.mForegroundCallState, this.mBackgroundCallState, disconnectCause, preciseDisconnectCause);
        }
    }

    public void notifyPreciseDataConnectionFailed(String reason, String apnType, String apn, String failCause) {
        if (checkNotifyPermission("notifyPreciseDataConnectionFailed()")) {
            synchronized (this.mRecords) {
                int phoneId = getPhoneSubIdMapping(Integer.MAX_VALUE);
                if (validatePhoneId(phoneId)) {
                    this.mPreciseDataConnectionState[phoneId] = new PreciseDataConnectionState(-1, 0, apnType, apn, reason, null, failCause);
                    for (Record r : this.mRecords) {
                        if (r.matchPhoneStateListenerEvent(4096)) {
                            try {
                                r.callback.onPreciseDataConnectionStateChanged(this.mPreciseDataConnectionState[phoneId]);
                            } catch (RemoteException e) {
                                this.mRemoveList.add(r.binder);
                            }
                        }
                    }
                }
                handleRemoveListLocked();
            }
            broadcastPreciseDataConnectionStateChanged(-1, 0, apnType, apn, reason, null, failCause);
        }
    }

    public void notifyVoLteServiceStateChanged(VoLteServiceState lteState) {
        if (checkNotifyPermission("notifyVoLteServiceStateChanged()")) {
            synchronized (this.mRecords) {
                this.mVoLteServiceState = lteState;
                for (Record r : this.mRecords) {
                    if (r.matchPhoneStateListenerEvent(16384)) {
                        try {
                            r.callback.onVoLteServiceStateChanged(new VoLteServiceState(this.mVoLteServiceState));
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifyOemHookRawEventForSubscriber(int subId, byte[] rawData) {
        if (checkNotifyPermission("notifyOemHookRawEventForSubscriber")) {
            synchronized (this.mRecords) {
                for (Record r : this.mRecords) {
                    if (VDBG) {
                        log("notifyOemHookRawEventForSubscriber:  r=" + r + " subId=" + subId);
                    }
                    if (r.matchPhoneStateListenerEvent(32768) && (r.subId == subId || r.subId == Integer.MAX_VALUE)) {
                        try {
                            r.callback.onOemHookRawEvent(rawData);
                        } catch (RemoteException e) {
                            this.mRemoveList.add(r.binder);
                        }
                    }
                }
                handleRemoveListLocked();
            }
        }
    }

    public void notifyLteAccessStratumChanged(String state) {
        if (checkNotifyPermission("notifyLteAccessStratumChanged()")) {
            if (VDBG) {
                log("notifyLteAccessStratumChanged: broadcast intent");
            }
            broadcastLteAccessStratumChanged(state);
        }
    }

    public void notifyPsNetworkTypeChanged(int nwType) {
        if (checkNotifyPermission("notifyPsNetworkTypeChanged()")) {
            if (VDBG) {
                log("notifyPsNetworkTypeChanged: broadcast intent");
            }
            broadcastPsNetworkTypeChanged(nwType);
        }
    }

    public void notifySharedDefaultApnStateChanged(boolean isSharedDefaultApn) {
        if (checkNotifyPermission("notifySharedDefaultApnStateChanged()")) {
            if (VDBG) {
                log("notifySharedDefaultApnStateChanged: broadcast intent");
            }
            broadcastSharedDefaultApnStateChanged(isSharedDefaultApn);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump telephony.registry from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mRecords) {
            int recordCount = this.mRecords.size();
            pw.println("last known state:");
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                pw.println("  Phone Id=" + i);
                pw.println("  mCallState=" + this.mCallState[i]);
                pw.println("  mCallIncomingNumber=" + this.mCallIncomingNumber[i]);
                pw.println("  mServiceState=" + this.mServiceState[i]);
                pw.println("  mSignalStrength=" + this.mSignalStrength[i]);
                pw.println("  mMessageWaiting=" + this.mMessageWaiting[i]);
                pw.println("  mCallForwarding=" + this.mCallForwarding[i]);
                pw.println("  mDataActivity=" + this.mDataActivity[i]);
                pw.println("  mDataConnectionState=" + this.mDataConnectionState[i]);
                pw.println("  mDataConnectionPossible=" + this.mDataConnectionPossible[i]);
                pw.println("  mDataConnectionReason=" + this.mDataConnectionReason[i]);
                pw.println("  mDataConnectionApn=" + this.mDataConnectionApn[i]);
                pw.println("  mDataConnectionLinkProperties=" + this.mDataConnectionLinkProperties[i]);
                pw.println("  mDataConnectionNetworkCapabilities=" + this.mDataConnectionNetworkCapabilities[i]);
                pw.println("  mCellLocation=" + this.mCellLocation[i]);
                pw.println("  mCellInfo=" + this.mCellInfo.get(i));
            }
            pw.println("registrations: count=" + recordCount);
            for (Record r : this.mRecords) {
                pw.println("  " + r);
            }
        }
    }

    private void broadcastServiceStateChanged(ServiceState state, int phoneId, int subId) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.notePhoneState(state.getState());
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        Intent intent = new Intent("android.intent.action.SERVICE_STATE");
        Bundle data = new Bundle();
        state.fillInNotifierBundle(data);
        intent.putExtras(data);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, SubscriptionManager.getPhoneId(subId));
        intent.putExtra("color_int_subId", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastSignalStrengthChanged(SignalStrength signalStrength, int phoneId, int subId) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.notePhoneSignalStrength(signalStrength);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        Intent intent = new Intent("android.intent.action.SIG_STR");
        intent.addFlags(536870912);
        Bundle data = new Bundle();
        signalStrength.fillInNotifierBundle(data);
        intent.putExtras(data);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, SubscriptionManager.getPhoneId(subId));
        intent.putExtra("color_int_subId", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastCallStateChanged(int state, String incomingNumber, int phoneId, int subId) {
        long ident = Binder.clearCallingIdentity();
        if (state == 0) {
            try {
                this.mBatteryStats.notePhoneOff();
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            this.mBatteryStats.notePhoneOn();
        }
        Binder.restoreCallingIdentity(ident);
        Intent intent = new Intent(OppoPhoneStateReceiver.ACTION_PHONE_STATE_CHANGED);
        intent.putExtra("state", DefaultPhoneNotifier.convertCallState(state).toString());
        if (!TextUtils.isEmpty(incomingNumber)) {
            intent.putExtra("incoming_number", incomingNumber);
        }
        if (subId != -1) {
            intent.setAction("android.intent.action.SUBSCRIPTION_PHONE_STATE");
            intent.putExtra("color_int_subId", subId);
            intent.putExtra("subscription", subId);
        }
        if (phoneId != -1) {
            intent.putExtra("slot", phoneId);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, OppoPermissionConstants.PERMISSION_READ_PHONE_STATE, 51);
    }

    private void broadcastCallStateChangedForPhoneInfo(int phoneType, int state, String incomingNumber, int phoneId, int subId) {
        long ident = Binder.clearCallingIdentity();
        if (state == 0) {
            try {
                this.mBatteryStats.notePhoneOff();
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            this.mBatteryStats.notePhoneOn();
        }
        Binder.restoreCallingIdentity(ident);
        Intent intent = new Intent(OppoPhoneStateReceiver.ACTION_PHONE_STATE_CHANGED);
        intent.putExtra("state", DefaultPhoneNotifier.convertCallState(state).toString());
        if (!TextUtils.isEmpty(incomingNumber)) {
            intent.putExtra("incoming_number", incomingNumber);
        }
        if (subId != -1) {
            intent.setAction("android.intent.action.SUBSCRIPTION_PHONE_STATE");
            intent.putExtra("color_int_subId", subId);
            intent.putExtra("subscription", subId);
        }
        if (phoneId != -1) {
            intent.putExtra("slot", phoneId);
        }
        intent.putExtra("phoneType", phoneType);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, OppoPermissionConstants.PERMISSION_READ_PHONE_STATE, 51);
    }

    private void broadcastLteAccessStratumChanged(String state) {
        Intent intent = new Intent("mediatek.intent.action.LTE_ACCESS_STRATUM_STATE_CHANGED");
        intent.putExtra("lteAccessStratumState", state);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, OppoPermissionConstants.PERMISSION_READ_PHONE_STATE);
    }

    private void broadcastPsNetworkTypeChanged(int nwType) {
        Intent intent = new Intent("mediatek.intent.action.PS_NETWORK_TYPE_CHANGED");
        intent.putExtra("psNetworkType", nwType);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, OppoPermissionConstants.PERMISSION_READ_PHONE_STATE);
    }

    private void broadcastSharedDefaultApnStateChanged(boolean isSharedDefaultApn) {
        Intent intent = new Intent("mediatek.intent.action.SHARED_DEFAULT_APN_STATE_CHANGED");
        intent.putExtra("sharedDefaultApn", isSharedDefaultApn);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, OppoPermissionConstants.PERMISSION_READ_PHONE_STATE);
    }

    private void broadcastDataConnectionStateChanged(int state, boolean isDataConnectivityPossible, String reason, String apn, String apnType, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, boolean roaming, int subId) {
        Intent intent = new Intent("android.intent.action.ANY_DATA_STATE");
        intent.putExtra("state", DefaultPhoneNotifier.convertDataState(state).toString());
        if (!isDataConnectivityPossible) {
            intent.putExtra("networkUnvailable", true);
        }
        if (reason != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason);
        }
        if (linkProperties != null) {
            intent.putExtra("linkProperties", linkProperties);
            String iface = linkProperties.getInterfaceName();
            if (iface != null) {
                intent.putExtra("iface", iface);
            }
        }
        if (networkCapabilities != null) {
            intent.putExtra("networkCapabilities", networkCapabilities);
        }
        if (roaming) {
            intent.putExtra("networkRoaming", true);
        }
        intent.putExtra("apn", apn);
        intent.putExtra("apnType", apnType);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastDataConnectionFailed(String reason, String apnType, int subId, int phoneId) {
        Intent intent = new Intent("android.intent.action.DATA_CONNECTION_FAILED");
        intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason);
        intent.putExtra("apnType", apnType);
        intent.putExtra("subscription", subId);
        intent.putExtra("phone", phoneId);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void broadcastPreciseCallStateChanged(int ringingCallState, int foregroundCallState, int backgroundCallState, int disconnectCause, int preciseDisconnectCause) {
        Intent intent = new Intent("android.intent.action.PRECISE_CALL_STATE");
        intent.putExtra("ringing_state", ringingCallState);
        intent.putExtra("foreground_state", foregroundCallState);
        intent.putExtra("background_state", backgroundCallState);
        intent.putExtra("disconnect_cause", disconnectCause);
        intent.putExtra("precise_disconnect_cause", preciseDisconnectCause);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRECISE_PHONE_STATE");
    }

    private void broadcastPreciseDataConnectionStateChanged(int state, int networkType, String apnType, String apn, String reason, LinkProperties linkProperties, String failCause) {
        Intent intent = new Intent("android.intent.action.PRECISE_DATA_CONNECTION_STATE_CHANGED");
        intent.putExtra("state", state);
        intent.putExtra("networkType", networkType);
        if (reason != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason);
        }
        if (apnType != null) {
            intent.putExtra("apnType", apnType);
        }
        if (apn != null) {
            intent.putExtra("apn", apn);
        }
        if (linkProperties != null) {
            intent.putExtra("linkProperties", linkProperties);
        }
        if (failCause != null) {
            intent.putExtra("failCause", failCause);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRECISE_PHONE_STATE");
    }

    private void enforceNotifyPermissionOrCarrierPrivilege(String method) {
        if (!checkNotifyPermission()) {
            enforceCarrierPrivilege();
        }
    }

    private boolean checkNotifyPermission(String method) {
        if (checkNotifyPermission()) {
            return true;
        }
        log("Modify Phone State Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    private boolean checkNotifyPermission() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0;
    }

    private void enforceCarrierPrivilege() {
        TelephonyManager tm = TelephonyManager.getDefault();
        String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid());
        int i = 0;
        int length = pkgs.length;
        while (i < length) {
            if (tm.checkCarrierPrivilegesForPackage(pkgs[i]) != 1) {
                i++;
            } else {
                return;
            }
        }
        String msg = "Carrier Privilege Permission Denial: from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
        log(msg);
        throw new SecurityException(msg);
    }

    private void checkListenerPermission(int events) {
        if ((events & 16) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION", null);
        }
        if ((events & 1024) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION", null);
        }
        if ((events & ENFORCE_PHONE_STATE_PERMISSION_MASK) != 0) {
            try {
                this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
            } catch (SecurityException e) {
                this.mContext.enforceCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE, null);
            }
        }
        if ((events & PRECISE_PHONE_STATE_PERMISSION_MASK) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRECISE_PHONE_STATE", null);
        }
        if ((32768 & events) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", null);
        }
    }

    private void handleRemoveListLocked() {
        int size = this.mRemoveList.size();
        if (VDBG) {
            log("handleRemoveListLocked: mRemoveList.size()=" + size);
        }
        if (size > 0) {
            for (IBinder b : this.mRemoveList) {
                remove(b);
            }
            this.mRemoveList.clear();
        }
    }

    private boolean validateEventsAndUserLocked(Record r, int events) {
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            boolean valid = r.callerUserId == ActivityManager.getCurrentUser() ? r.matchPhoneStateListenerEvent(events) : false;
            Binder.restoreCallingIdentity(callingIdentity);
            return valid;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    private boolean validatePhoneId(int phoneId) {
        boolean valid = phoneId >= 0 && phoneId < this.mNumPhones;
        if (VDBG) {
            log("validatePhoneId: " + valid);
        }
        return valid;
    }

    private void onUpdatePhoneSubIdMapping() {
        log("onUpdatePhoneSubIdMapping E");
        synchronized (this.mSubIdMapping) {
            for (int i = 0; i < this.mNumPhones; i++) {
                this.mSubIdMapping[i] = SubscriptionManager.getSubIdUsingPhoneId(i);
                log("update phone id:" + i + " is mapping to sub id:" + this.mSubIdMapping[i]);
            }
        }
        log("onUpdatePhoneSubIdMapping X");
    }

    /* JADX WARNING: Missing block: B:17:0x0069, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPhoneSubIdMapping(int subId) {
        synchronized (this.mSubIdMapping) {
            for (int i = 0; i < this.mNumPhones; i++) {
                if (this.mSubIdMapping[i] == subId) {
                    log("subId:" + subId + " finds phone id:" + i);
                    return i;
                }
            }
            int tmpPhoneId = SubscriptionManager.getPhoneId(subId);
            if (tmpPhoneId < this.mNumPhones) {
                this.mSubIdMapping[tmpPhoneId] = subId;
                log("Can't find in map, update phone id:" + tmpPhoneId + " is mapping to sub id:" + this.mSubIdMapping[tmpPhoneId]);
            }
        }
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void logv(String s) {
        if (TEL_DBG > 0) {
            log(s);
        }
    }

    private void logServiceStateChanged(String s, int subId, int phoneId, ServiceState state) {
        if (this.logSSC != null && this.logSSC.length != 0) {
            if (this.logSSC[this.next] == null) {
                this.logSSC[this.next] = new LogSSC();
            }
            Time t = new Time();
            t.setToNow();
            this.logSSC[this.next].set(t, s, subId, phoneId, state);
            int i = this.next + 1;
            this.next = i;
            if (i >= this.logSSC.length) {
                this.next = 0;
            }
        }
    }

    private void toStringLogSSC(String prompt) {
        if (this.logSSC == null || this.logSSC.length == 0 || (this.next == 0 && this.logSSC[this.next] == null)) {
            log(prompt + ": logSSC is empty");
            return;
        }
        log(prompt + ": logSSC.length=" + this.logSSC.length + " next=" + this.next);
        int i = this.next;
        if (this.logSSC[i] == null) {
            i = 0;
        }
        do {
            log(this.logSSC[i].toString());
            i++;
            if (i >= this.logSSC.length) {
                i = 0;
            }
        } while (i != this.next);
        log(prompt + ": ----------------");
    }

    boolean idMatch(int rSubId, int subId, int phoneId) {
        boolean z = true;
        if (subId < 0) {
            if (this.mDefaultPhoneId != phoneId) {
                z = false;
            }
            return z;
        } else if (rSubId == Integer.MAX_VALUE) {
            if (subId != this.mDefaultSubId) {
                z = false;
            }
            return z;
        } else {
            if (rSubId != subId) {
                z = false;
            }
            return z;
        }
    }

    boolean idMatch(int rSubId, int subId) {
        boolean rlt = false;
        if (subId < 0) {
            if (SubscriptionManager.getPhoneId(rSubId) == SubscriptionManager.getPhoneId(subId)) {
                rlt = true;
            }
            return rlt;
        }
        if (rSubId == Integer.MAX_VALUE) {
            if (subId == this.mDefaultSubId) {
                rlt = true;
            }
        } else if (rSubId == subId) {
            rlt = true;
        }
        return rlt;
    }

    boolean idMatchForNetwork(int rSubId, int subId, int rPhoneId, int phoneId) {
        boolean z = true;
        if (subId < 0) {
            if (rPhoneId != phoneId) {
                z = false;
            }
            return z;
        } else if (rSubId == Integer.MAX_VALUE) {
            if (subId != this.mDefaultSubId) {
                z = false;
            }
            return z;
        } else {
            if (rSubId != subId) {
                z = false;
            }
            return z;
        }
    }

    boolean isEmergencyWithoutSim(int rSubId, int subId, String apnType) {
        if (!"emergency".equalsIgnoreCase(apnType) || rSubId != Integer.MAX_VALUE || SubscriptionManager.isValidSubscriptionId(subId)) {
            return false;
        }
        log("isEimsWithoutSim() rSubId: " + rSubId + " subId: " + subId + " apnType: " + apnType);
        return true;
    }

    private void checkPossibleMissNotify(Record r, int phoneId) {
        int events = r.events;
        if ((events & 1) != 0) {
            try {
                if (VDBG) {
                    log("checkPossibleMissNotify: onServiceStateChanged state=" + this.mServiceState[phoneId]);
                }
                r.callback.onServiceStateChanged(new ServiceState(this.mServiceState[phoneId]));
            } catch (RemoteException e) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 256) != 0) {
            try {
                SignalStrength signalStrength = this.mSignalStrength[phoneId];
                log("checkPossibleMissNotify: onSignalStrengthsChanged SS=" + signalStrength);
                r.callback.onSignalStrengthsChanged(new SignalStrength(signalStrength));
            } catch (RemoteException e2) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 2) != 0) {
            try {
                int gsmSignalStrength = this.mSignalStrength[phoneId].getGsmSignalStrength();
                log("checkPossibleMissNotify: onSignalStrengthChanged SS=" + gsmSignalStrength);
                IPhoneStateListener iPhoneStateListener = r.callback;
                if (gsmSignalStrength == 99) {
                    gsmSignalStrength = -1;
                }
                iPhoneStateListener.onSignalStrengthChanged(gsmSignalStrength);
            } catch (RemoteException e3) {
                this.mRemoveList.add(r.binder);
            }
        }
        if (validateEventsAndUserLocked(r, 1024)) {
            try {
                r.callback.onCellInfoChanged((List) this.mCellInfo.get(phoneId));
            } catch (RemoteException e4) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 4) != 0) {
            try {
                if (VDBG) {
                    log("checkPossibleMissNotify: onMessageWaitingIndicatorChanged phoneId=" + phoneId + " mwi=" + this.mMessageWaiting[phoneId]);
                }
                r.callback.onMessageWaitingIndicatorChanged(this.mMessageWaiting[phoneId]);
            } catch (RemoteException e5) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 8) != 0) {
            try {
                if (VDBG) {
                    log("checkPossibleMissNotify: onCallForwardingIndicatorChanged phoneId=" + phoneId + " cfi=" + this.mCallForwarding[phoneId]);
                }
                r.callback.onCallForwardingIndicatorChanged(this.mCallForwarding[phoneId]);
            } catch (RemoteException e6) {
                this.mRemoveList.add(r.binder);
            }
        }
        if (validateEventsAndUserLocked(r, 16)) {
            try {
                r.callback.onCellLocationChanged(new Bundle(this.mCellLocation[phoneId]));
            } catch (RemoteException e7) {
                this.mRemoveList.add(r.binder);
            }
        }
        if ((events & 64) != 0) {
            try {
                log("checkPossibleMissNotify: onDataConnectionStateChanged(mDataConnectionState=" + this.mDataConnectionState[phoneId] + ", mDataConnectionNetworkType=" + this.mDataConnectionNetworkType[phoneId] + ")");
                r.callback.onDataConnectionStateChanged(this.mDataConnectionState[phoneId], this.mDataConnectionNetworkType[phoneId]);
            } catch (RemoteException e8) {
                this.mRemoveList.add(r.binder);
            }
        }
    }

    private int oemGetPhoneId(HashMap<Integer, Integer> map, int subId) {
        if (!map.containsKey(Integer.valueOf(subId))) {
            map.put(Integer.valueOf(subId), Integer.valueOf(SubscriptionManager.getPhoneId(subId)));
        }
        return ((Integer) map.get(Integer.valueOf(subId))).intValue();
    }

    private boolean oemIdMatch(HashMap<Integer, Integer> map, int rSubId, int subId) {
        boolean rlt = false;
        if (subId < 0) {
            if (oemGetPhoneId(map, rSubId) == oemGetPhoneId(map, subId)) {
                rlt = true;
            }
            log("oem idMatch: rlt=" + rlt);
            return rlt;
        }
        if (rSubId == Integer.MAX_VALUE) {
            if (subId == this.mDefaultSubId) {
                rlt = true;
            }
        } else if (rSubId == subId) {
            rlt = true;
        }
        log("oem idMatch: rlt=" + rlt);
        return rlt;
    }
}
