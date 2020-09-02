package com.mediatek.ims.legacy.ss;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.mediatek.ims.OperatorUtils;
import com.mediatek.ims.SuppSrvConfig;
import com.mediatek.internal.telephony.MtkCallForwardInfo;
import com.mediatek.internal.telephony.MtkSuppServHelper;
import com.mediatek.simservs.client.CommunicationDiversion;
import com.mediatek.simservs.client.CommunicationWaiting;
import com.mediatek.simservs.client.IncomingCommunicationBarring;
import com.mediatek.simservs.client.OriginatingIdentityPresentation;
import com.mediatek.simservs.client.OriginatingIdentityPresentationRestriction;
import com.mediatek.simservs.client.OutgoingCommunicationBarring;
import com.mediatek.simservs.client.SimServs;
import com.mediatek.simservs.client.SimservType;
import com.mediatek.simservs.client.TerminatingIdentityPresentation;
import com.mediatek.simservs.client.TerminatingIdentityPresentationRestriction;
import com.mediatek.simservs.client.policy.Actions;
import com.mediatek.simservs.client.policy.Conditions;
import com.mediatek.simservs.client.policy.Rule;
import com.mediatek.simservs.client.policy.RuleSet;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.simservs.xcap.XcapException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public final class MMTelSSTransport {
    private static final int CB_ALL = 5;
    /* access modifiers changed from: private */
    public static String[] CB_FACILITY = {"AO", "OI", "OX", "AI", "IR"};
    private static final int CB_ICB = 2;
    private static final int CB_MO = 3;
    private static final int CB_MT = 4;
    private static final int CB_NONE = 0;
    private static final int CB_OCB = 1;
    /* access modifiers changed from: private */
    public static int[] CF_REASON = {1, 2, 3, 6, 0};
    private static final int CLIR_NOT_PROVISION = 2;
    private static final int CLIR_PROVISION = 1;
    private static final int CLIR_TEMP_ALLOW = 4;
    private static final int CLIR_TEMP_DISALLOW = 3;
    static final boolean DBG = true;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT = 5000;
    static final int EVENT_SEND = 1;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;
    private static final int IDENTITY_CLIP = 0;
    private static final int IDENTITY_CLIR = 1;
    private static final int IDENTITY_COLP = 2;
    private static final int IDENTITY_COLR = 3;
    private static final MMTelSSTransport INSTANCE = new MMTelSSTransport();
    private static final int INVALID_PHONE_ID = -1;
    private static final String LOG_TAG = "MMTelSS";
    private static final int MATCHED_MEDIA_AUDIO = 1;
    private static final int MATCHED_MEDIA_NO_MATCHED = 0;
    private static final int MATCHED_MEDIA_VIDEO = 2;
    static final int MMTELSS_MAX_COMMAND_BYTES = 8192;
    static final int MMTELSS_REQ_GET_CB = 7;
    static final int MMTELSS_REQ_GET_CF = 9;
    static final int MMTELSS_REQ_GET_CF_TIME_SLOT = 16;
    static final int MMTELSS_REQ_GET_CLIP = 3;
    static final int MMTELSS_REQ_GET_CLIR = 2;
    static final int MMTELSS_REQ_GET_COLP = 4;
    static final int MMTELSS_REQ_GET_COLR = 5;
    static final int MMTELSS_REQ_GET_CW = 11;
    static final int MMTELSS_REQ_SET_CB = 6;
    static final int MMTELSS_REQ_SET_CF = 8;
    static final int MMTELSS_REQ_SET_CF_TIME_SLOT = 15;
    static final int MMTELSS_REQ_SET_CLIP = 12;
    static final int MMTELSS_REQ_SET_CLIR = 1;
    static final int MMTELSS_REQ_SET_COLP = 13;
    static final int MMTELSS_REQ_SET_COLR = 14;
    static final int MMTELSS_REQ_SET_CW = 10;
    private static final String MODE_SS_CS = "Prefer CS";
    private static final String MODE_SS_XCAP = "Prefer XCAP";
    private static final int MODIFIED_SERVICE_AUDIO = 1;
    private static final int MODIFIED_SERVICE_VIDEO = 2;
    private static final String PROP_SS_CFNUM = "persist.vendor.radio.xcap.cfn";
    private static final String PROP_SS_DISABLE_METHOD = "persist.vendor.radio.ss.xrdm";
    private static final String PROP_SS_MODE = "persist.vendor.radio.ss.mode";
    /* access modifiers changed from: private */
    public static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    /* access modifiers changed from: private */
    public static SuppSrvConfig mSSConfig = null;
    /* access modifiers changed from: private */
    public static SimServs mSimservs = SimServs.getInstance();
    private final int CACHE_IDX_CF = 0;
    private final int CACHE_IDX_CW = 3;
    private final int CACHE_IDX_ICB = 2;
    private final int CACHE_IDX_OCB = 1;
    private final int CACHE_IDX_OIP = 4;
    private final int CACHE_IDX_OIR = 5;
    private final int CACHE_IDX_TIP = 6;
    private final int CACHE_IDX_TIR = 7;
    private final int CACHE_IDX_TOTAL = 8;
    /* access modifiers changed from: private */
    public int[] mCachePhoneId = new int[8];
    /* access modifiers changed from: private */
    public SimservType[] mCacheSimserv = new SimservType[8];
    Context mContext = null;
    IntentFilter mFilter = null;
    private ServiceConnection mGbaConnection = new ServiceConnection() {
        /* class com.mediatek.ims.legacy.ss.MMTelSSTransport.AnonymousClass2 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "GbaService onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "GbaService onServiceFailed");
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.mediatek.ims.legacy.ss.MMTelSSTransport.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "onReceive:" + intent.getAction());
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED") || intent.getAction().equals("android.intent.action.AIRPLANE_MODE")) {
                MMTelSSTransport.this.onReceiveSimStateChangedIntent(intent);
            }
        }
    };
    /* access modifiers changed from: private */
    public long[] mLastQueried = new long[8];
    /* access modifiers changed from: private */
    public Network mNetwork = null;
    int mRequestMessagesPending = 0;
    int mRequestMessagesWaiting;
    ArrayList<MMTelSSRequest> mRequestsList = new ArrayList<>();
    MMTelSSTransmitter mSender;
    HandlerThread mSenderThread = new HandlerThread("MMTelSSTransmitter");
    PowerManager.WakeLock mWakeLock;
    int mWakeLockTimeout;
    /* access modifiers changed from: private */
    public XcapMobileDataNetworkManager mXcapMobileDataNetworkManager = null;
    private PowerManager pm = null;

    public MMTelSSTransport() {
        this.mSenderThread.start();
        this.mSender = new MMTelSSTransmitter(this.mSenderThread.getLooper());
        for (int i = 0; i < 8; i++) {
            this.mCacheSimserv[i] = null;
            this.mCachePhoneId[i] = INVALID_PHONE_ID;
            this.mLastQueried[i] = 0;
        }
    }

    public static MMTelSSTransport getInstance() {
        return INSTANCE;
    }

    public void registerUtService(Context context) {
        this.mContext = context;
        if (this.mWakeLock == null) {
            this.pm = (PowerManager) this.mContext.getSystemService("power");
            this.mWakeLock = this.pm.newWakeLock(1, LOG_TAG);
            this.mWakeLock.setReferenceCounted(false);
            this.mWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", (int) DEFAULT_WAKE_LOCK_TIMEOUT);
        }
        if (this.mXcapMobileDataNetworkManager == null) {
            this.mXcapMobileDataNetworkManager = new XcapMobileDataNetworkManager(this.mContext, this.mSenderThread.getLooper());
        }
        Rlog.d(LOG_TAG, "registerReceiver");
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mFilter.addAction("android.intent.action.AIRPLANE_MODE");
        this.mContext.registerReceiver(this.mIntentReceiver, this.mFilter);
    }

    private void requestXcapNetwork(int phoneId) {
        Rlog.d(LOG_TAG, "requestXcapNetwork(): phoneId = " + phoneId + ", mXcapMobileDataNetworkManager = " + this.mXcapMobileDataNetworkManager);
        this.mNetwork = null;
        XcapMobileDataNetworkManager xcapMobileDataNetworkManager = this.mXcapMobileDataNetworkManager;
        if (xcapMobileDataNetworkManager != null) {
            this.mNetwork = xcapMobileDataNetworkManager.acquireNetwork(phoneId);
            ConnectivityManager connMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (connMgr != null) {
                Rlog.d(LOG_TAG, "Bind process to xcap network");
                connMgr.bindProcessToNetwork(this.mNetwork);
            }
        }
    }

    private void startGbaService(Context context) {
        Rlog.d(LOG_TAG, "start gba service");
        ComponentName gbaService = new ComponentName("com.mediatek.gba", "com.mediatek.gba.GbaService");
        Intent gbaIntent = new Intent();
        gbaIntent.setComponent(gbaService);
        this.mContext.bindService(gbaIntent, this.mGbaConnection, 1);
        Rlog.d(LOG_TAG, "Is gba service running = " + isGbaServiceRunning(context));
    }

    private boolean isGbaServiceRunning(Context context) {
        List<ActivityManager.RunningServiceInfo> runningServices = ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE);
        for (int i = 0; i < runningServices.size(); i++) {
            if (runningServices.get(i).service.getClassName().equals("com.mediatek.gba.GbaService")) {
                Rlog.d(LOG_TAG, "Gba service is running");
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean updateNetworkInitSimServ(int phoneId) {
        Rlog.d(LOG_TAG, "updateNetworkInitSimServ:" + phoneId);
        if (mSSConfig.isNotSupportXcap()) {
            Rlog.d(LOG_TAG, "Use CS instead of XCAP.");
            return false;
        }
        mSimservs = MMTelSSUtils.initSimserv(this.mContext, phoneId);
        String xcapRoot = MMTelSSUtils.getXcapRootUri(phoneId, this.mContext);
        if (xcapRoot == null || xcapRoot.isEmpty()) {
            Rlog.d(LOG_TAG, "XcapRoot is empty");
            return false;
        }
        if (!isGbaServiceRunning(this.mContext)) {
            startGbaService(this.mContext);
        }
        requestXcapNetwork(phoneId);
        initAuthentication(xcapRoot, phoneId, this.mNetwork);
        int defaultDataPhoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        Rlog.d(LOG_TAG, "defaultDataPhoneId = " + defaultDataPhoneId + ", phoneId = " + phoneId);
        if (this.mNetwork != null || defaultDataPhoneId == phoneId) {
            return true;
        }
        Rlog.e(LOG_TAG, "mNetwork is null anddefault data doesn't match the current SIM card, request XCAP failed.");
        return false;
    }

    public void initAuthentication(String xcapRoot, final int phoneId, Network network) {
        String digestId = mSSConfig.getDigestId();
        String digestPwd = mSSConfig.getDigestPwd();
        Rlog.d(LOG_TAG, "initAuthentication: xcapRoot=" + xcapRoot + ", phoneId=" + phoneId + ", network=" + network + ", digestId=" + digestId + ", digestPwd=" + digestPwd);
        if (!TextUtils.isEmpty(digestId) || !TextUtils.isEmpty(digestPwd)) {
            Authenticator.setDefault(new Authenticator() {
                /* class com.mediatek.ims.legacy.ss.MMTelSSTransport.AnonymousClass3 */

                /* access modifiers changed from: protected */
                public PasswordAuthentication getPasswordAuthentication() {
                    String digestId = MMTelSSTransport.mSSConfig.getDigestId();
                    String digestPwd = MMTelSSTransport.mSSConfig.getDigestPwd();
                    if (TextUtils.isEmpty(digestId)) {
                        digestId = MMTelSSUtils.getXui(phoneId, MMTelSSTransport.this.mContext);
                    }
                    Rlog.d(MMTelSSTransport.LOG_TAG, "getPasswordAuthentication: digestId=" + digestId + ", digestPwd=" + digestPwd);
                    return new PasswordAuthentication(digestId, digestPwd.toCharArray());
                }
            });
            System.setProperty("http.digest.support", XcapElement.TRUE);
        }
    }

    /* access modifiers changed from: private */
    public void onReceiveSimStateChangedIntent(Intent intent) {
        String simStatus = intent.getStringExtra("ss");
        int phoneId = intent.getIntExtra("phone", INVALID_PHONE_ID);
        if ("ABSENT".equals(simStatus)) {
            Rlog.d(LOG_TAG, "onReceiveSimStateChangedIntent: simStatus=" + simStatus + "phoneId=" + phoneId);
            for (int i = 0; i < 8; i++) {
                this.mCacheSimserv[i] = null;
                this.mCachePhoneId[i] = INVALID_PHONE_ID;
                this.mLastQueried[i] = 0;
            }
            mSimservs.resetParameters();
        }
    }

    class MMTelSSTransmitter extends Handler implements Runnable {
        byte[] dataLength = new byte[4];

        public MMTelSSTransmitter(Looper looper) {
            super(looper);
        }

        public void run() {
        }

        public String getMediaType(int serviceClass) {
            if ((serviceClass & 1) != 0) {
                return "audio";
            }
            if ((serviceClass & 512) != 0) {
                return "video";
            }
            return "";
        }

        public Exception reportXcapException(XcapException xcapException) {
            if (xcapException.isConnectionError()) {
                Rlog.d(MMTelSSTransport.LOG_TAG, "reportXcapException: isConnectionError()");
                return new UnknownHostException();
            } else if (!MMTelSSTransport.mSSConfig.isHttpErrToUnknownHostErr() || xcapException.getHttpErrorCode() == 0) {
                return xcapException;
            } else {
                Rlog.d(MMTelSSTransport.LOG_TAG, "reportXcapException: HttpErrCode=" + xcapException.getHttpErrorCode());
                return new UnknownHostException();
            }
        }

        public String identityToString(int identity) {
            if (identity == 0) {
                return "CLIP";
            }
            if (identity == 1) {
                return "CLIR";
            }
            if (identity == 2) {
                return "COLP";
            }
            if (identity != 3) {
                return "ERR";
            }
            return "COLR";
        }

        public int identityToCacheId(int identity) {
            if (identity == 0) {
                return 4;
            }
            if (identity == 1) {
                return 5;
            }
            if (identity == 2) {
                return 6;
            }
            if (identity != 3) {
                return 8;
            }
            return 7;
        }

        public void handleGetIdentity(MMTelSSRequest rr, int identity) {
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            rr.mp.readInt();
            int phoneId = rr.mp.readInt();
            Rlog.d(MMTelSSTransport.LOG_TAG, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", phoneId=" + phoneId);
            if (!MMTelSSUtils.isPreferXcap(phoneId, MMTelSSTransport.this.mContext) || !MMTelSSTransport.this.updateNetworkInitSimServ(phoneId)) {
                triggerCSFB(rr.mResult);
                return;
            }
            CommandException commandException = null;
            int[] response = new int[2];
            Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", phoneId=" + phoneId);
            int i = 1;
            if (identity == 0) {
                try {
                    response[0] = 0;
                    if (!((OriginatingIdentityPresentation) getCache(identityToCacheId(identity), phoneId)).isActive()) {
                        i = 0;
                    }
                    response[0] = i;
                    Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", enable=" + response[0]);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    commandException = e;
                } catch (XcapException xcapException) {
                    xcapException.printStackTrace();
                    commandException = reportXcapException(xcapException);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    commandException = CommandException.fromRilErrno(2);
                }
            } else if (identity == 1) {
                response[0] = 0;
                response[1] = 2;
                boolean restricted = ((OriginatingIdentityPresentationRestriction) getCache(identityToCacheId(identity), phoneId)).isDefaultPresentationRestricted();
                if (restricted) {
                    response[0] = 1;
                    response[1] = 3;
                } else {
                    response[0] = 2;
                    response[1] = 4;
                }
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", restricted=" + restricted);
            } else if (identity == 2) {
                response[0] = 0;
                response[1] = 0;
                int i2 = ((TerminatingIdentityPresentation) getCache(identityToCacheId(identity), phoneId)).isActive() ? 1 : 0;
                response[1] = i2;
                response[0] = i2;
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", enable=" + response[0]);
            } else if (identity == 3) {
                response[0] = 0;
                if (!((TerminatingIdentityPresentationRestriction) getCache(identityToCacheId(identity), phoneId)).isActive()) {
                    i = 0;
                }
                response[0] = i;
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", enable=" + response[0]);
            }
            if (commandException != null) {
                clearCache(identityToCacheId(identity));
            }
            if (rr.mResult != null) {
                AsyncResult.forMessage(rr.mResult, response, commandException);
                rr.mResult.sendToTarget();
            }
            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                MMTelSSTransport.this.mXcapMobileDataNetworkManager.releaseNetwork();
            }
        }

        public void handleSetIdentity(MMTelSSRequest rr, int identity) {
            boolean enable = false;
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            rr.mp.readInt();
            int mode = rr.mp.readInt();
            int phoneId = rr.mp.readInt();
            Rlog.d(MMTelSSTransport.LOG_TAG, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", mode=" + mode + ", phoneId=" + phoneId);
            if (!MMTelSSUtils.isPreferXcap(phoneId, MMTelSSTransport.this.mContext) || !MMTelSSTransport.this.updateNetworkInitSimServ(phoneId)) {
                triggerCSFB(rr.mResult);
                return;
            }
            Exception exceptionReport = null;
            Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", mode=" + mode + ", phoneId=" + phoneId);
            if (identity == 0) {
                try {
                    OriginatingIdentityPresentation oip = (OriginatingIdentityPresentation) getCache(identityToCacheId(identity), phoneId);
                    if (mode == 1) {
                        enable = true;
                    }
                    Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", enable=" + enable);
                    oip.setActive(enable);
                } catch (XcapException xcapException) {
                    xcapException.printStackTrace();
                    exceptionReport = reportXcapException(xcapException);
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionReport = CommandException.fromRilErrno(2);
                }
            } else if (identity == 1) {
                OriginatingIdentityPresentationRestriction oir = (OriginatingIdentityPresentationRestriction) getCache(identityToCacheId(identity), phoneId);
                boolean enable2 = mode == 1;
                boolean putWhole = MMTelSSTransport.mSSConfig.isPutWholeCLIR();
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", enable=" + enable2 + ", putWhole=" + putWhole);
                if (putWhole || !oir.isContainDefaultBehaviour()) {
                    oir.setDefaultPresentationRestricted(enable2, enable2, OriginatingIdentityPresentationRestriction.NODE_ROOT_FULL_CHILD, true);
                } else {
                    oir.setDefaultPresentationRestricted(enable2);
                }
            } else if (identity == 2) {
                TerminatingIdentityPresentation tip = (TerminatingIdentityPresentation) getCache(identityToCacheId(identity), phoneId);
                boolean enable3 = mode == 1;
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", enable=" + enable3);
                tip.setActive(enable3);
            } else if (identity == 3) {
                TerminatingIdentityPresentationRestriction tir = (TerminatingIdentityPresentationRestriction) getCache(identityToCacheId(identity), phoneId);
                boolean enable4 = mode == 1;
                boolean putWhole2 = MMTelSSTransport.mSSConfig.isPutWholeCLIR();
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetIdentity() " + MMTelSSTransport.requestToString(reqNo) + ", identity=" + identityToString(identity) + ", enable=" + enable4 + ", putWhole=" + putWhole2);
                if (putWhole2 || !tir.isContainDefaultBehaviour()) {
                    tir.setDefaultPresentationRestricted(enable4, enable4, TerminatingIdentityPresentationRestriction.NODE_ROOT_FULL_CHILD, true);
                } else {
                    tir.setDefaultPresentationRestricted(enable4);
                }
            }
            clearCache(identityToCacheId(identity));
            if (rr.mResult != null) {
                AsyncResult.forMessage(rr.mResult, (Object) null, exceptionReport);
                rr.mResult.sendToTarget();
            }
            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                MMTelSSTransport.this.mXcapMobileDataNetworkManager.releaseNetwork();
            }
        }

        public void handleGetCW(MMTelSSRequest rr) {
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            rr.mp.readInt();
            int serviceClass = rr.mp.readInt();
            int phoneId = rr.mp.readInt();
            Rlog.d(MMTelSSTransport.LOG_TAG, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo) + ", serviceClass=" + serviceClass + ", phoneId=" + phoneId);
            if (!MMTelSSUtils.isPreferXcap(phoneId, MMTelSSTransport.this.mContext) || !MMTelSSTransport.this.updateNetworkInitSimServ(phoneId)) {
                triggerCSFB(rr.mResult);
                return;
            }
            Exception exceptionReport = null;
            int[] response = new int[2];
            int serviceClass2 = convertServiceClass(serviceClass);
            Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetCW() " + MMTelSSTransport.requestToString(reqNo) + ", serviceClass=" + serviceClassToString(serviceClass2) + ", phoneId=" + phoneId);
            try {
                response[0] = ((CommunicationWaiting) getCache(3, phoneId)).isActive() ? 1 : 0;
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetCW(): isActive = " + response[0]);
            } catch (UnknownHostException unknownHostException) {
                exceptionReport = unknownHostException;
            } catch (XcapException xcapException) {
                xcapException.printStackTrace();
                exceptionReport = reportXcapException(xcapException);
            } catch (Exception e) {
                e.printStackTrace();
                exceptionReport = CommandException.fromRilErrno(2);
            }
            if (exceptionReport != null) {
                clearCache(3);
            }
            if (response[0] == 1) {
                if (serviceClass2 == 0) {
                    response[1] = response[1] | 1;
                    response[1] = 512 | response[1];
                } else {
                    response[1] = response[1] | serviceClass2;
                    if (serviceClass2 == 512) {
                        response[1] = response[1] | 1;
                    }
                }
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetCW(): service=" + serviceClassToString(response[1]));
            }
            if (exceptionReport != null && OperatorUtils.isMatched(OperatorUtils.OPID.OP156, phoneId)) {
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetCW() OP156 not support, just CSFB");
                exceptionReport = new UnknownHostException();
            }
            if (rr.mResult != null) {
                AsyncResult.forMessage(rr.mResult, response, exceptionReport);
                rr.mResult.sendToTarget();
            }
            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                MMTelSSTransport.this.mXcapMobileDataNetworkManager.releaseNetwork();
            }
        }

        public String getCBFacility(int cbType, Conditions cond) {
            String r;
            String str;
            if (cbType == 1) {
                if (cond == null) {
                    r = "AO";
                } else if (cond.comprehendInternational()) {
                    r = "OI";
                } else if (cond.comprehendInternationalExHc()) {
                    r = "OX";
                } else {
                    r = "AO";
                }
            } else if (cbType != 2) {
                r = "ERR";
            } else if (cond == null) {
                r = "AI";
            } else if (cond.comprehendRoaming()) {
                r = "IR";
            } else {
                r = "AI";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("getCBFacility() ");
            sb.append(r);
            sb.append(": cbType=");
            sb.append(cbTypeToString(cbType));
            if (cond == null) {
                str = ", cond=null";
            } else {
                str = ", OCB: international=" + cond.comprehendInternational() + ",internationalExHc=" + cond.comprehendInternationalExHc() + " | ICB roaming=" + cond.comprehendRoaming();
            }
            sb.append(str);
            Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
            return r;
        }

        public int getCB(SimservType cb, String facility, int serviceClass, int phoneId) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "getCB() facility=" + facility + ", service=" + serviceClassToString(serviceClass) + ", phoneId=" + phoneId + ", cb=" + cb);
            int result = 0;
            List<Rule> ruleList = null;
            int cbType = getCBType(facility);
            if (cbType == 1) {
                ruleList = ((OutgoingCommunicationBarring) cb).getRuleSet().getRules();
            } else if (cbType == 2) {
                ruleList = ((IncomingCommunicationBarring) cb).getRuleSet().getRules();
            } else {
                Rlog.d(MMTelSSTransport.LOG_TAG, "getCB() not support facility=" + facility + ", cbType=" + cbTypeToString(cbType));
            }
            if (ruleList != null) {
                for (Rule rule : ruleList) {
                    Conditions cond = rule.getConditions();
                    Actions act = rule.getActions();
                    if (getCBFacility(cbType, cond).equals(facility) && isRuleMatchServiceClass(rule, serviceClass) != 0) {
                        boolean enable = false;
                        if (act != null && !act.isAllow() && (cond == null || !cond.comprehendRuleDeactivated())) {
                            result |= serviceClass;
                            enable = true;
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("getCB() found rule: ");
                        sb.append(rule.mId);
                        sb.append(", facility=");
                        sb.append(facility);
                        sb.append(", service=");
                        sb.append(serviceClassToString(serviceClass));
                        sb.append(", status=");
                        sb.append(enable ? "Enable" : "Disable");
                        sb.append(", result=");
                        sb.append(result);
                        Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
                    }
                }
            }
            return result;
        }

        public int getCBType(String facility) {
            if (facility.equals("AO") || facility.equals("OI") || facility.equals("OX")) {
                return 1;
            }
            if (facility.equals("AI") || facility.equals("IR")) {
                return 2;
            }
            if (facility.equals("AG")) {
                return 3;
            }
            if (facility.equals("AC")) {
                return 4;
            }
            if (facility.equals("AB")) {
                return 5;
            }
            return 0;
        }

        public String cbTypeToString(int cbType) {
            if (cbType == 1) {
                return "OCB";
            }
            if (cbType == 2) {
                return "ICB";
            }
            if (cbType == 3) {
                return "CB_MO";
            }
            if (cbType == 4) {
                return "CB_MT";
            }
            if (cbType != 5) {
                return "ERR";
            }
            return "CB_ALL";
        }

        /* JADX WARN: Failed to insert an additional move for type inference into block B:13:0x00c8 */
        /* JADX WARN: Failed to insert an additional move for type inference into block B:16:0x00cc */
        /* JADX DEBUG: Additional 6 move instruction added to help type inference */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v0, resolved type: java.lang.StringBuilder} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.StringBuilder} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v3, resolved type: int} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v4, resolved type: char} */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r9v2, types: [java.lang.String] */
        /* JADX WARN: Type inference failed for: r9v6 */
        /* JADX WARN: Type inference failed for: r9v8 */
        /* JADX WARN: Type inference failed for: r9v10 */
        /* JADX WARN: Type inference failed for: r9v14, types: [int] */
        /* JADX WARN: Type inference failed for: r9v15 */
        /* JADX WARN: Type inference failed for: r9v17 */
        /* JADX WARN: Type inference failed for: r9v19 */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x011d  */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0124  */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0136  */
        /* JADX WARNING: Removed duplicated region for block: B:42:? A[RETURN, SYNTHETIC] */
        public void handleGetCB(MMTelSSRequest rr) {
            int i;
            int cacheIdx;
            int cacheIdx2;
            int cacheIdx3;
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            rr.mp.readInt();
            String facility = rr.mp.readString();
            int serviceClass = rr.mp.readInt();
            int phoneId = rr.mp.readInt();
            StringBuilder sb = new StringBuilder();
            sb.append("Read from parcel: ");
            sb.append(MMTelSSTransport.requestToString(reqNo));
            int cacheIdx4 = ", facility=";
            sb.append((String) cacheIdx4);
            sb.append(facility);
            sb.append(", serviceClass=");
            sb.append(serviceClass);
            sb.append(", phoneId=");
            sb.append(phoneId);
            Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
            if (!MMTelSSUtils.isPreferXcap(phoneId, MMTelSSTransport.this.mContext) || !MMTelSSTransport.this.updateNetworkInitSimServ(phoneId)) {
                triggerCSFB(rr.mResult);
                return;
            }
            Exception exceptionReport = null;
            exceptionReport = null;
            int serviceClass2 = convertServiceClass(serviceClass);
            int[] response = {0};
            int cbType = getCBType(facility);
            if (cbType == 1) {
                i = 1;
            } else {
                i = cbType == 2 ? 2 : 8;
            }
            Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetCB() " + MMTelSSTransport.requestToString(reqNo) + ((String) cacheIdx4) + facility + ", cbType=" + cbTypeToString(cbType) + ", serviceClass=" + serviceClassToString(serviceClass2) + ", phoneId=" + phoneId);
            if (serviceClass2 == 0) {
                try {
                    cacheIdx4 = i;
                    try {
                        response[0] = response[0] | getCB(getCache(cacheIdx4, phoneId), facility, 1, phoneId);
                        response[0] = response[0] | getCB(getCache(cacheIdx4, phoneId), facility, 512, phoneId);
                    } catch (UnknownHostException e) {
                        unknownHostException = e;
                        cacheIdx = cacheIdx4;
                        exceptionReport = unknownHostException;
                        cacheIdx4 = cacheIdx;
                        if (exceptionReport != null) {
                        }
                        if (rr.mResult != null) {
                        }
                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                        }
                    } catch (XcapException e2) {
                        xcapException = e2;
                        cacheIdx2 = cacheIdx4;
                        xcapException.printStackTrace();
                        exceptionReport = reportXcapException(xcapException);
                        cacheIdx4 = cacheIdx2;
                        if (exceptionReport != null) {
                        }
                        if (rr.mResult != null) {
                        }
                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                        }
                    } catch (Exception e3) {
                        e = e3;
                        cacheIdx3 = cacheIdx4;
                        e.printStackTrace();
                        exceptionReport = CommandException.fromRilErrno(2);
                        cacheIdx4 = cacheIdx3;
                        if (exceptionReport != null) {
                        }
                        if (rr.mResult != null) {
                        }
                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                        }
                    }
                } catch (UnknownHostException e4) {
                    unknownHostException = e4;
                    cacheIdx = i;
                    exceptionReport = unknownHostException;
                    cacheIdx4 = cacheIdx;
                    if (exceptionReport != null) {
                    }
                    if (rr.mResult != null) {
                    }
                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                    }
                } catch (XcapException e5) {
                    xcapException = e5;
                    cacheIdx2 = i;
                    xcapException.printStackTrace();
                    exceptionReport = reportXcapException(xcapException);
                    cacheIdx4 = cacheIdx2;
                    if (exceptionReport != null) {
                    }
                    if (rr.mResult != null) {
                    }
                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                    }
                } catch (Exception e6) {
                    e = e6;
                    cacheIdx3 = i;
                    e.printStackTrace();
                    exceptionReport = CommandException.fromRilErrno(2);
                    cacheIdx4 = cacheIdx3;
                    if (exceptionReport != null) {
                    }
                    if (rr.mResult != null) {
                    }
                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                    }
                }
            } else {
                cacheIdx4 = i;
                response[0] = response[0] | getCB(getCache(cacheIdx4, phoneId), facility, serviceClass2, phoneId);
            }
            if (exceptionReport != null) {
                clearCache(cacheIdx4);
            }
            if (rr.mResult != null) {
                AsyncResult.forMessage(rr.mResult, response, exceptionReport);
                rr.mResult.sendToTarget();
            }
            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                MMTelSSTransport.this.mXcapMobileDataNetworkManager.releaseNetwork();
            }
        }

        public List<MtkCallForwardInfo> getCFInfo(CommunicationDiversion cd, int action, int reason, int serviceClass) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "getCFInfo() reason=" + reasonCFToString(reason) + ", action=" + actionCFToString(action) + ", service=" + serviceClassToString(serviceClass));
            boolean found = false;
            List<MtkCallForwardInfo> result = new ArrayList<>();
            List<Rule> ruleList = cd.getRuleSet().getRules();
            long[] timeSlot = null;
            if (ruleList != null) {
                for (Rule rule : ruleList) {
                    if (getCFType(rule.getConditions()) == reason && isRuleMatchServiceClass(rule, serviceClass) != 0) {
                        int enable = 0;
                        if (!rule.getConditions().comprehendRuleDeactivated()) {
                            enable = 1;
                        }
                        String number = null;
                        if (rule.getActions().getFowardTo() != null) {
                            number = convertUriToNumber(rule.getActions().getFowardTo().getTarget());
                        }
                        if (enable == 1) {
                            timeSlot = convertToLocalTime(rule.getConditions().comprehendTime());
                        }
                        MtkCallForwardInfo cfInfo = new MtkCallForwardInfo();
                        cfInfo.status = enable;
                        cfInfo.reason = reason;
                        cfInfo.serviceClass = serviceClass;
                        cfInfo.toa = 0;
                        cfInfo.number = number;
                        cfInfo.timeSeconds = cd.getNoReplyTimer();
                        cfInfo.timeSlot = timeSlot;
                        result.add(cfInfo);
                        StringBuilder sb = new StringBuilder();
                        sb.append("getCFInfo() found rule: ");
                        sb.append(rule.mId);
                        sb.append(", reason=");
                        sb.append(reasonCFToString(reason));
                        sb.append(", service=");
                        sb.append(serviceClassToString(serviceClass));
                        sb.append(", status=");
                        sb.append(enable == 1 ? "Enable" : "Disable");
                        sb.append(", number=");
                        sb.append(MtkSuppServHelper.encryptString(number));
                        sb.append(", time=");
                        sb.append(cfInfo.timeSeconds);
                        sb.append(", timeSlot=");
                        sb.append(timeSlot);
                        Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
                        found = true;
                    }
                }
            }
            if (!found) {
                MtkCallForwardInfo cfInfo2 = new MtkCallForwardInfo();
                cfInfo2.status = 0;
                cfInfo2.reason = reason;
                cfInfo2.serviceClass = serviceClass;
                cfInfo2.toa = 0;
                cfInfo2.number = null;
                cfInfo2.timeSeconds = cd.getNoReplyTimer();
                cfInfo2.timeSlot = null;
                result.add(cfInfo2);
                Rlog.d(MMTelSSTransport.LOG_TAG, "getCFInfo() not found rule, reason=" + reasonCFToString(reason) + ", service=" + serviceClassToString(serviceClass));
            }
            return result;
        }

        public List<MtkCallForwardInfo> getCFInfoList(CommunicationDiversion cd, int action, int reason, int serviceClass) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "getCFInfoList() reason=" + reasonCFToString(reason) + ", action=" + actionCFToString(action) + ", service=" + serviceClassToString(serviceClass));
            List<MtkCallForwardInfo> result = new ArrayList<>();
            if (serviceClass == 0) {
                result.addAll(getCFInfo(cd, action, reason, 1));
                result.addAll(getCFInfo(cd, action, reason, 512));
            } else {
                result.addAll(getCFInfo(cd, action, reason, serviceClass));
            }
            return result;
        }

        public Object convertCFInfo(boolean isGetTimeSlot, MtkCallForwardInfo[] infos) {
            if (infos == null) {
                infos = new MtkCallForwardInfo[0];
            }
            if (isGetTimeSlot) {
                return infos;
            }
            CallForwardInfo[] result = new CallForwardInfo[infos.length];
            for (int i = 0; i < infos.length; i++) {
                result[i] = new CallForwardInfo();
                result[i].status = infos[i].status;
                result[i].reason = infos[i].reason;
                result[i].serviceClass = infos[i].serviceClass;
                result[i].toa = 0;
                result[i].number = infos[i].number;
                result[i].timeSeconds = infos[i].timeSeconds;
            }
            return result;
        }

        public String cacheIdxToString(int idx) {
            switch (idx) {
                case 0:
                    return "CF";
                case 1:
                    return "OCB";
                case 2:
                    return "ICB";
                case XcapException.NO_HTTP_RESPONSE_EXCEPTION /*{ENCODED_INT: 3}*/:
                    return "CW";
                case XcapException.HTTP_RECOVERABL_EEXCEPTION /*{ENCODED_INT: 4}*/:
                    return "OIP";
                case XcapException.MALFORMED_CHALLENGE_EXCEPTION /*{ENCODED_INT: 5}*/:
                    return "OIR";
                case 6:
                    return "TIP";
                case 7:
                    return "TIR";
                default:
                    return "ERR: " + String.valueOf(idx);
            }
        }

        public void updateNetwork(int idx) {
            switch (idx) {
                case 0:
                    ((CommunicationDiversion) MMTelSSTransport.this.mCacheSimserv[idx]).setNetwork(MMTelSSTransport.this.mNetwork);
                    return;
                case 1:
                    ((OutgoingCommunicationBarring) MMTelSSTransport.this.mCacheSimserv[idx]).setNetwork(MMTelSSTransport.this.mNetwork);
                    return;
                case 2:
                    ((IncomingCommunicationBarring) MMTelSSTransport.this.mCacheSimserv[idx]).setNetwork(MMTelSSTransport.this.mNetwork);
                    return;
                case XcapException.NO_HTTP_RESPONSE_EXCEPTION /*{ENCODED_INT: 3}*/:
                    ((CommunicationWaiting) MMTelSSTransport.this.mCacheSimserv[idx]).setNetwork(MMTelSSTransport.this.mNetwork);
                    return;
                case XcapException.HTTP_RECOVERABL_EEXCEPTION /*{ENCODED_INT: 4}*/:
                    ((OriginatingIdentityPresentation) MMTelSSTransport.this.mCacheSimserv[idx]).setNetwork(MMTelSSTransport.this.mNetwork);
                    return;
                case XcapException.MALFORMED_CHALLENGE_EXCEPTION /*{ENCODED_INT: 5}*/:
                    ((OriginatingIdentityPresentationRestriction) MMTelSSTransport.this.mCacheSimserv[idx]).setNetwork(MMTelSSTransport.this.mNetwork);
                    return;
                case 6:
                    ((TerminatingIdentityPresentation) MMTelSSTransport.this.mCacheSimserv[idx]).setNetwork(MMTelSSTransport.this.mNetwork);
                    return;
                case 7:
                    ((TerminatingIdentityPresentationRestriction) MMTelSSTransport.this.mCacheSimserv[idx]).setNetwork(MMTelSSTransport.this.mNetwork);
                    return;
                default:
                    return;
            }
        }

        public SimservType getCache(int idx, int phoneId) throws Exception {
            boolean usingCache = true;
            long curTime = System.currentTimeMillis();
            Rlog.d(MMTelSSTransport.LOG_TAG, "getCache(): " + cacheIdxToString(idx) + " phoneId=" + phoneId + ", cachePhoneId=" + MMTelSSTransport.this.mCachePhoneId[idx] + ", curTime=" + curTime + ", lastQuery=" + MMTelSSTransport.this.mLastQueried[idx] + ", mCacheSimserv=" + MMTelSSTransport.this.mCacheSimserv[idx]);
            Long cacheValidTime = Long.valueOf(MMTelSSTransport.mSSConfig.getCacheValidTime());
            if (phoneId != MMTelSSTransport.this.mCachePhoneId[idx] || MMTelSSTransport.this.mCacheSimserv[idx] == null || curTime - MMTelSSTransport.this.mLastQueried[idx] > cacheValidTime.longValue()) {
                usingCache = false;
                switch (idx) {
                    case 0:
                        MMTelSSTransport.this.mCacheSimserv[idx] = MMTelSSTransport.mSimservs.getCommunicationDiversion(true, MMTelSSTransport.this.mNetwork);
                        break;
                    case 1:
                        MMTelSSTransport.this.mCacheSimserv[idx] = MMTelSSTransport.mSimservs.getOutgoingCommunicationBarring(true, MMTelSSTransport.this.mNetwork);
                        break;
                    case 2:
                        MMTelSSTransport.this.mCacheSimserv[idx] = MMTelSSTransport.mSimservs.getIncomingCommunicationBarring(true, MMTelSSTransport.this.mNetwork);
                        break;
                    case XcapException.NO_HTTP_RESPONSE_EXCEPTION /*{ENCODED_INT: 3}*/:
                        MMTelSSTransport.this.mCacheSimserv[idx] = MMTelSSTransport.mSimservs.getCommunicationWaiting(true, MMTelSSTransport.this.mNetwork);
                        break;
                    case XcapException.HTTP_RECOVERABL_EEXCEPTION /*{ENCODED_INT: 4}*/:
                        MMTelSSTransport.this.mCacheSimserv[idx] = MMTelSSTransport.mSimservs.getOriginatingIdentityPresentation(true, MMTelSSTransport.this.mNetwork);
                        break;
                    case XcapException.MALFORMED_CHALLENGE_EXCEPTION /*{ENCODED_INT: 5}*/:
                        MMTelSSTransport.this.mCacheSimserv[idx] = MMTelSSTransport.mSimservs.getOriginatingIdentityPresentationRestriction(true, MMTelSSTransport.this.mNetwork);
                        break;
                    case 6:
                        MMTelSSTransport.this.mCacheSimserv[idx] = MMTelSSTransport.mSimservs.getTerminatingIdentityPresentation(true, MMTelSSTransport.this.mNetwork);
                        break;
                    case 7:
                        MMTelSSTransport.this.mCacheSimserv[idx] = MMTelSSTransport.mSimservs.getTerminatingIdentityPresentationRestriction(true, MMTelSSTransport.this.mNetwork);
                        break;
                }
                MMTelSSTransport.this.mCachePhoneId[idx] = phoneId;
                MMTelSSTransport.this.mLastQueried[idx] = curTime;
                Rlog.d(MMTelSSTransport.LOG_TAG, "getCache(): new Cache phoneId=" + phoneId + ", curTime=" + curTime + ", mCacheSimserv=" + MMTelSSTransport.this.mCacheSimserv[idx]);
            }
            if (usingCache) {
                updateNetwork(idx);
            }
            return MMTelSSTransport.this.mCacheSimserv[idx];
        }

        public void clearCache(int idx) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "Clear [" + cacheIdxToString(idx) + "] cache");
            MMTelSSTransport.this.mCacheSimserv[idx] = null;
            MMTelSSTransport.this.mCachePhoneId[idx] = MMTelSSTransport.INVALID_PHONE_ID;
            MMTelSSTransport.this.mLastQueried[idx] = 0;
        }

        public void removeDuplicateCF(ArrayList<MtkCallForwardInfo> cfInfoList) {
            for (int i = 0; i < 5; i++) {
                int reason = MMTelSSTransport.CF_REASON[i];
                MtkCallForwardInfo firstCfInfo = null;
                Iterator<MtkCallForwardInfo> iterator = cfInfoList.iterator();
                while (iterator.hasNext()) {
                    MtkCallForwardInfo cfInfo = iterator.next();
                    if (firstCfInfo == null && cfInfo.reason == reason) {
                        firstCfInfo = cfInfo;
                        Rlog.d(MMTelSSTransport.LOG_TAG, "firstCfInfo() reason=" + cfInfo.reason + ", service=" + serviceClassToString(cfInfo.serviceClass) + ", number=" + MtkSuppServHelper.encryptString(cfInfo.number));
                    } else if (firstCfInfo != null && firstCfInfo.reason == cfInfo.reason && firstCfInfo.serviceClass == cfInfo.serviceClass) {
                        iterator.remove();
                        Rlog.d(MMTelSSTransport.LOG_TAG, "removeDuplicateCF() reason=" + cfInfo.reason + ", service=" + serviceClassToString(cfInfo.serviceClass) + ", number=" + MtkSuppServHelper.encryptString(cfInfo.number));
                    } else if (!(firstCfInfo == null || firstCfInfo.serviceClass == cfInfo.serviceClass)) {
                        firstCfInfo = cfInfo;
                        Rlog.d(MMTelSSTransport.LOG_TAG, "reassign cf info, reason=" + cfInfo.reason + ", service=" + serviceClassToString(cfInfo.serviceClass) + ", number=" + MtkSuppServHelper.encryptString(cfInfo.number));
                    }
                }
            }
        }

        public void handleGetCF(MMTelSSRequest rr) {
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            int serialNo = rr.mp.readInt();
            int action = rr.mp.readInt();
            int reason = rr.mp.readInt();
            int serviceClass = rr.mp.readInt();
            String number = rr.mp.readString();
            int phoneId = rr.mp.readInt();
            Message msg = rr.mResult;
            Rlog.d(MMTelSSTransport.LOG_TAG, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo) + ", action=" + action + ", reason=" + reason + ", serviceClass=" + serviceClass + ", number=" + MtkSuppServHelper.encryptString(number) + ", phoneId=" + phoneId + ", msg=" + msg);
            handleGetCF(reqNo, serialNo, action, reason, serviceClass, number, phoneId, false, msg);
        }

        /* JADX WARNING: Removed duplicated region for block: B:38:0x0103  */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x0108  */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x011a  */
        /* JADX WARNING: Removed duplicated region for block: B:50:? A[RETURN, SYNTHETIC] */
        public void handleGetCF(int reqNo, int serialNo, int action, int reason, int serviceClass, String number, int phoneId, boolean isGetTimeSlot, Message msg) {
            if (MMTelSSUtils.isPreferXcap(phoneId, MMTelSSTransport.this.mContext)) {
                if (MMTelSSTransport.this.updateNetworkInitSimServ(phoneId)) {
                    Exception exceptionReport = null;
                    MtkCallForwardInfo[] infos = null;
                    int serviceClass2 = convertServiceClass(serviceClass);
                    Rlog.d(MMTelSSTransport.LOG_TAG, "handleGetCF() " + MMTelSSTransport.requestToString(reqNo) + ", action=" + actionCFToString(action) + ", reason=" + reasonCFToString(reason) + ", serviceClass=" + serviceClassToString(serviceClass2) + ", number=" + MtkSuppServHelper.encryptString(number) + ", phoneId=" + phoneId + ", isGetTimeSlot=" + isGetTimeSlot);
                    try {
                        CommunicationDiversion cd = (CommunicationDiversion) getCache(0, phoneId);
                        if (isEmptyCF(cd)) {
                            clearCache(0);
                            infos = new MtkCallForwardInfo[0];
                        } else {
                            ArrayList<MtkCallForwardInfo> cfInfoList = new ArrayList<>();
                            if (reason == 5) {
                                int i = 0;
                                while (i < 4) {
                                    try {
                                        cfInfoList.addAll(getCFInfoList(cd, action, MMTelSSTransport.CF_REASON[i], serviceClass2));
                                        i++;
                                    } catch (UnknownHostException e) {
                                        unknownHostException = e;
                                        exceptionReport = unknownHostException;
                                        if (exceptionReport != null) {
                                        }
                                        if (msg != null) {
                                        }
                                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                        }
                                    } catch (XcapException e2) {
                                        xcapException = e2;
                                        xcapException.printStackTrace();
                                        exceptionReport = reportXcapException(xcapException);
                                        if (exceptionReport != null) {
                                        }
                                        if (msg != null) {
                                        }
                                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                        }
                                    } catch (Exception e3) {
                                        e = e3;
                                        e.printStackTrace();
                                        exceptionReport = CommandException.fromRilErrno(2);
                                        if (exceptionReport != null) {
                                        }
                                        if (msg != null) {
                                        }
                                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                        }
                                    }
                                }
                            } else if (reason == 4) {
                                for (int i2 = 0; i2 < 5; i2++) {
                                    cfInfoList.addAll(getCFInfoList(cd, action, MMTelSSTransport.CF_REASON[i2], serviceClass2));
                                }
                            } else {
                                cfInfoList.addAll(getCFInfoList(cd, action, reason, serviceClass2));
                            }
                            removeDuplicateCF(cfInfoList);
                            infos = (MtkCallForwardInfo[]) cfInfoList.toArray(new MtkCallForwardInfo[cfInfoList.size()]);
                        }
                    } catch (UnknownHostException e4) {
                        unknownHostException = e4;
                        exceptionReport = unknownHostException;
                        if (exceptionReport != null) {
                        }
                        if (msg != null) {
                        }
                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                        }
                    } catch (XcapException e5) {
                        xcapException = e5;
                        xcapException.printStackTrace();
                        exceptionReport = reportXcapException(xcapException);
                        if (exceptionReport != null) {
                        }
                        if (msg != null) {
                        }
                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                        }
                    } catch (Exception e6) {
                        e = e6;
                        e.printStackTrace();
                        exceptionReport = CommandException.fromRilErrno(2);
                        if (exceptionReport != null) {
                        }
                        if (msg != null) {
                        }
                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                        }
                    }
                    if (exceptionReport != null) {
                        clearCache(0);
                    }
                    if (msg != null) {
                        AsyncResult.forMessage(msg, convertCFInfo(isGetTimeSlot, infos), exceptionReport);
                        msg.sendToTarget();
                    }
                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                        MMTelSSTransport.this.mXcapMobileDataNetworkManager.releaseNetwork();
                        return;
                    }
                    return;
                }
            }
            triggerCSFB(msg);
        }

        public void handleSetCW(MMTelSSRequest rr) {
            boolean z = false;
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            rr.mp.readInt();
            int enable = rr.mp.readInt();
            int serviceClass = rr.mp.readInt();
            int phoneId = rr.mp.readInt();
            Rlog.d(MMTelSSTransport.LOG_TAG, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo) + ", enable=" + enable + ", serviceClass=" + serviceClass + ", phoneId=" + phoneId);
            if (!MMTelSSUtils.isPreferXcap(phoneId, MMTelSSTransport.this.mContext) || !MMTelSSTransport.this.updateNetworkInitSimServ(phoneId)) {
                triggerCSFB(rr.mResult);
                return;
            }
            Exception exceptionReport = null;
            Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetCW() " + MMTelSSTransport.requestToString(reqNo) + ", enable=" + enable + ", serviceClass=" + serviceClassToString(serviceClass) + ", phoneId=" + phoneId);
            try {
                CommunicationWaiting cw = (CommunicationWaiting) getCache(3, phoneId);
                if (enable == 1) {
                    z = true;
                }
                cw.setActive(z);
            } catch (XcapException xcapException) {
                xcapException.printStackTrace();
                exceptionReport = reportXcapException(xcapException);
            } catch (Exception e) {
                e.printStackTrace();
                exceptionReport = CommandException.fromRilErrno(2);
            }
            clearCache(3);
            if (exceptionReport != null && OperatorUtils.isMatched(OperatorUtils.OPID.OP156, phoneId)) {
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetCW() OP156 not support, just CSFB");
                exceptionReport = new UnknownHostException();
            }
            if (rr.mResult != null) {
                AsyncResult.forMessage(rr.mResult, (Object) null, exceptionReport);
                rr.mResult.sendToTarget();
            }
            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                MMTelSSTransport.this.mXcapMobileDataNetworkManager.releaseNetwork();
            }
        }

        public String reasonCFToString(int reason) {
            if (reason == 0) {
                return "CFU";
            }
            if (reason == 1) {
                return "CFB";
            }
            if (reason == 2) {
                return "CFNRy";
            }
            if (reason == 3) {
                return "CFNRc";
            }
            if (reason == 6) {
                return "CFNL";
            }
            if (reason == 4) {
                return "CF All";
            }
            if (reason == 5) {
                return "CF All Conditional";
            }
            return "ERR: " + String.valueOf(reason);
        }

        public String actionCFToString(int action) {
            if (action == 0) {
                return "Disable";
            }
            if (action == 1) {
                return "Enable";
            }
            if (action == 2) {
                return "UNUSED";
            }
            if (action == 3) {
                return "Registration";
            }
            if (action == 4) {
                return "Erasure";
            }
            return "ERR: " + String.valueOf(action);
        }

        public String serviceClassToString(int service) {
            if (service == 0) {
                return "None";
            }
            if (service == 256) {
                return "Line2";
            }
            if (service == 512) {
                return "Video";
            }
            if (service == 1) {
                return "Voice";
            }
            if (service == 2) {
                return "Data";
            }
            if (service == 4) {
                return "Fax";
            }
            if (service == 8) {
                return "Sms";
            }
            if (service == MMTelSSTransport.MMTELSS_REQ_GET_CF_TIME_SLOT) {
                return "Data sync";
            }
            if (service == 32) {
                return "Data async";
            }
            if (service == 64) {
                return "Packet";
            }
            if (service == 128) {
                return "Pad";
            }
            if (service == 512) {
                return "Max";
            }
            if (service == 513) {
                return "Voice&Video";
            }
            return "ERR: " + String.valueOf(service);
        }

        public String serviceClassToMediaString(int service) {
            if (service == 1) {
                return "audio";
            }
            if (service == 512) {
                return "video";
            }
            return "ERR: " + String.valueOf(service);
        }

        public String matchedMediaToString(int matchedMedia) {
            String r = "";
            if ((matchedMedia & 1) != 0) {
                r = r + "audio ";
            }
            if ((matchedMedia & 2) != 0) {
                r = r + "video ";
            }
            if (r.equals("")) {
                return "no matched";
            }
            return r;
        }

        public String mediaTypeToString(int type) {
            if (type == 0) {
                return "Standard";
            }
            if (type == 1) {
                return "Only Audio";
            }
            if (type == 2) {
                return "Seperate";
            }
            if (type == 3) {
                return "Video with Audio";
            }
            return "ERR: " + String.valueOf(type);
        }

        public int getCFType(Conditions cond) {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("getCFType() ");
            if (cond == null) {
                str = "cond=null";
            } else {
                str = "Busy=" + cond.comprehendBusy() + ",NoAnswer=" + cond.comprehendNoAnswer() + ",NotReachable=" + cond.comprehendNotReachable() + ",NotRegistered=" + cond.comprehendNotRegistered();
            }
            sb.append(str);
            Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
            if (cond == null) {
                return 0;
            }
            if (cond.comprehendBusy()) {
                return 1;
            }
            if (cond.comprehendNoAnswer()) {
                return 2;
            }
            if (cond.comprehendNotReachable()) {
                return 3;
            }
            if (cond.comprehendNotRegistered()) {
                return 6;
            }
            return 0;
        }

        public int isMediaMatchServiceClass(List<String> media, int serviceClass) {
            int r = 0;
            int mediaType = MMTelSSTransport.mSSConfig.getMediaTagType();
            String printMedia = "";
            Iterator<String> it = media.iterator();
            while (it.hasNext()) {
                printMedia = printMedia + it.next() + " ";
            }
            boolean containAudio = media.contains(serviceClassToMediaString(1));
            boolean containVideo = media.contains(serviceClassToMediaString(512));
            if ((serviceClass & 1) != 0) {
                if (containAudio) {
                    if (!containVideo) {
                        r = 1;
                    } else if (mediaType == 1 || mediaType == 2 || mediaType == 3) {
                        r = 0;
                    } else {
                        r = 1;
                    }
                } else if (containVideo) {
                    r = 0;
                } else {
                    r = 1;
                }
            } else if ((serviceClass & 512) != 0) {
                if (containVideo) {
                    if (containAudio) {
                        if (mediaType == 0 || mediaType == 3) {
                            r = 2;
                        } else {
                            r = 0;
                        }
                    } else if (mediaType == 0 || mediaType == 2) {
                        r = 2;
                    } else {
                        r = 0;
                    }
                } else if (containAudio) {
                    r = 0;
                } else if (mediaType == 1) {
                    r = 0;
                } else {
                    r = 2;
                }
            } else if (serviceClass == 0) {
                Rlog.d(MMTelSSTransport.LOG_TAG, "isMediaMatchServiceClass: break down SERVICE_CLASS_NONE");
                r = 0 | isMediaMatchServiceClass(media, 1) | isMediaMatchServiceClass(media, 512);
            }
            Rlog.d(MMTelSSTransport.LOG_TAG, "isMediaMatchServiceClass()=" + matchedMediaToString(r) + ", mediaType=" + mediaTypeToString(mediaType) + ", service=" + serviceClassToString(serviceClass) + ", media=" + printMedia);
            return r;
        }

        public int isRuleMatchServiceClass(Rule rule, int serviceClass) {
            boolean isSupportMediaTag = MMTelSSTransport.mSSConfig.isSupportMediaTag();
            int r = 0;
            if (isSupportMediaTag) {
                r = isMediaMatchServiceClass(rule.getConditions().getMedias(), serviceClass);
            } else if (serviceClass == 0) {
                r = 3;
            } else if ((serviceClass & 1) != 0) {
                r = 1;
            } else if ((serviceClass & 512) != 0) {
                r = 2;
            }
            Rlog.d(MMTelSSTransport.LOG_TAG, "isRuleMatchServiceClass()=" + matchedMediaToString(r) + ", isSupportMediaTag=" + isSupportMediaTag + ", service=" + serviceClassToString(serviceClass));
            return r;
        }

        public void setForwardTo(Actions action, String number) {
            if (number != null) {
                action.setFowardTo(number, true);
            }
            action.getFowardTo().setRevealIdentityToCaller(true);
            action.getFowardTo().setRevealIdentityToTarget(true);
        }

        public void setMedia(Conditions cond, int serviceClass) {
            boolean isSupportMedia = MMTelSSTransport.mSSConfig.isSupportMediaTag();
            Rlog.d(MMTelSSTransport.LOG_TAG, "setMedia() isSupportMedia=" + isSupportMedia + ", service=" + serviceClassToString(serviceClass));
            if (isSupportMedia) {
                if ((serviceClass & 1) != 0) {
                    cond.addMedia(serviceClassToMediaString(1));
                }
                if ((serviceClass & 512) != 0) {
                    cond.addMedia(serviceClassToMediaString(512));
                }
            }
        }

        public List<Rule> modifyMatchedCFRule(Rule rule, int reason, int action, int serviceClass, String number, int time, String timeSlot) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "modifyMatchedCFRule() reason=" + reasonCFToString(reason) + ", action=" + actionCFToString(action) + ", service=" + serviceClassToString(serviceClass) + ", number=" + MtkSuppServHelper.encryptString(number) + ", time=" + time + ", timeSlot=" + timeSlot);
            List<Rule> result = new ArrayList<>();
            Conditions cond = rule.getConditions();
            Actions act = rule.getActions();
            if (action == 1 || action == 3) {
                cond.removeRuleDeactivated();
                setForwardTo(act, number);
                if (MMTelSSTransport.mSSConfig.isNoReplyTimeInsideCFAction() && time > 0 && reason == 2 && act.getNoReplyTimer() != MMTelSSTransport.INVALID_PHONE_ID) {
                    Rlog.d(MMTelSSTransport.LOG_TAG, "Set inside no-reply timer = " + time);
                    act.setNoReplyTimer(time);
                }
                if (!MMTelSSTransport.mSSConfig.isSupportTimeSlot() || timeSlot == null) {
                    cond.addTime(null);
                } else {
                    cond.addTime(timeSlot);
                }
            } else if (action == 0 || action == 4) {
                cond.addRuleDeactivated();
                if (action == 4 && act.getFowardTo() != null) {
                    setForwardTo(act, "");
                }
            }
            result.add(rule);
            return result;
        }

        private int modifyCFRuleForSeperateMedia(List<Rule> result, RuleSet ruleSet, int reason, int action, int serviceClass, String number, int time, String timeSlot) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "modifyCFRuleForSeperateMedia() reason=" + reasonCFToString(reason) + ", action=" + actionCFToString(action) + ", service=" + serviceClassToString(serviceClass) + ", number=" + MtkSuppServHelper.encryptString(number) + ", time=" + time + ", timeSlot=" + timeSlot);
            List<Rule> ruleList = ruleSet.getRules();
            List<Integer> modifyRuleIdx = new ArrayList<>();
            boolean findNotMatch = false;
            int r = 0;
            for (int i = 0; i < ruleList.size(); i++) {
                Rule rule = ruleList.get(i);
                if (getCFType(rule.getConditions()) == reason) {
                    int isMatchMedia = isRuleMatchServiceClass(rule, serviceClass);
                    if (isMatchMedia != 0) {
                        modifyRuleIdx.add(Integer.valueOf(i));
                    } else {
                        findNotMatch = true;
                    }
                    if ((isMatchMedia & 1) != 0) {
                        r |= 1;
                    }
                    if ((isMatchMedia & 2) != 0) {
                        r |= 2;
                    }
                }
            }
            Rlog.d(MMTelSSTransport.LOG_TAG, "modifyRuleIdx size: " + modifyRuleIdx.size() + ", findNotMatch: " + findNotMatch);
            if (modifyRuleIdx.size() != 0) {
                int i2 = 0;
                for (List<Integer> modifyRuleIdx2 = modifyRuleIdx; i2 < modifyRuleIdx2.size(); modifyRuleIdx2 = modifyRuleIdx2) {
                    result.addAll(modifyMatchedCFRule(ruleList.get(modifyRuleIdx2.get(i2).intValue()), reason, action, serviceClass, number, time, timeSlot));
                    i2++;
                }
            } else if (findNotMatch) {
                if ((serviceClass & 512) != 0) {
                    result.addAll(createCFRuleForService(ruleSet, reason, action, 512, number, time, timeSlot, "_VIDEO"));
                } else if ((serviceClass & 1) != 0) {
                    result.addAll(createCFRuleForService(ruleSet, reason, action, 1, number, time, timeSlot, "_AUDIO"));
                }
            }
            return r;
        }

        public int modifyCFRule(List<Rule> result, RuleSet ruleSet, Rule rule, int reason, int action, int serviceClass, String number, int time, String timeSlot) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "modifyCFRule() reason=" + reasonCFToString(reason) + ", action=" + actionCFToString(action) + ", service=" + serviceClassToString(serviceClass) + ", number=" + MtkSuppServHelper.encryptString(number) + ", time=" + time + ", timeSlot=" + timeSlot);
            int r = 0;
            int isMatchMedia = isRuleMatchServiceClass(rule, serviceClass);
            if (isMatchMedia != 0) {
                result.addAll(modifyMatchedCFRule(rule, reason, action, serviceClass, number, time, timeSlot));
            } else if (MMTelSSTransport.mSSConfig.getMediaTagType() == 2 && (serviceClass & 512) != 0) {
                result.addAll(createCFRuleForService(ruleSet, reason, action, 512, number, time, timeSlot, "_VIDEO"));
            }
            if ((isMatchMedia & 1) != 0) {
                r = 0 | 1;
            }
            if ((isMatchMedia & 2) != 0) {
                return r | 2;
            }
            return r;
        }

        public String getRuleId(int reason) {
            Map idMap = MMTelSSTransport.mSSConfig.getRuleId();
            if (reason == 0) {
                return (String) idMap.get("CFU");
            }
            if (reason == 1) {
                return (String) idMap.get("CFB");
            }
            if (reason == 2) {
                return (String) idMap.get("CFNRy");
            }
            if (reason == 3) {
                return (String) idMap.get("CFNRc");
            }
            if (reason != 6) {
                return "None";
            }
            return (String) idMap.get("CFNL");
        }

        public List<Rule> createCFRuleForService(RuleSet ruleSet, int reason, int action, int serviceClass, String number, int time, String timeSlot, String ruleIdPostfix) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "createCFRuleForService() reason=" + reasonCFToString(reason) + ", action=" + actionCFToString(action) + ", service=" + serviceClassToString(serviceClass) + ", number=" + MtkSuppServHelper.encryptString(number) + ", time=" + time + ", timeSlot=" + timeSlot);
            List<Rule> result = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append(getRuleId(reason));
            sb.append(ruleIdPostfix);
            Rule rule = ruleSet.createNewRule(sb.toString());
            Conditions cond = rule.createConditions();
            Actions act = rule.createActions();
            setForwardTo(act, number);
            if (reason != 0) {
                if (reason == 1) {
                    cond.addBusy();
                } else if (reason == 2) {
                    cond.addNoAnswer();
                    if (MMTelSSTransport.mSSConfig.isNoReplyTimeInsideCFAction() && time > 0) {
                        act.setNoReplyTimer(time);
                    }
                } else if (reason == 3) {
                    cond.addNotReachable();
                } else if (reason == 6) {
                    cond.addNotRegistered();
                }
            }
            setMedia(cond, serviceClass);
            if (MMTelSSTransport.mSSConfig.isSupportTimeSlot() && timeSlot != null) {
                cond.addTime(timeSlot);
            }
            result.add(rule);
            return result;
        }

        public String modifiedServiceToString(int modifiedService) {
            String r = "";
            if ((modifiedService & 1) != 0) {
                r = r + "audio ";
            }
            if ((modifiedService & 2) != 0) {
                r = r + "video ";
            }
            if (r.equals("")) {
                return "no modified";
            }
            return r;
        }

        public int[] serviceNeedToCreate(int modifiedService) {
            int[] r = new int[1];
            String print_r = "";
            int mediaType = MMTelSSTransport.mSSConfig.getMediaTagType();
            if (mediaType == 0) {
                if (modifiedService == 0) {
                    r[0] = 513;
                } else if (modifiedService == 1) {
                    r[0] = 512;
                } else if (modifiedService == 2) {
                    r[0] = 1;
                } else {
                    r = new int[0];
                }
            } else if (mediaType == 1) {
                if (modifiedService == 0) {
                    r[0] = 1;
                } else {
                    r = new int[0];
                }
            } else if (mediaType == 2) {
                if (modifiedService == 0) {
                    r = new int[]{1, 512};
                } else if (modifiedService == 1) {
                    r[0] = 512;
                } else if (modifiedService == 2) {
                    r[0] = 1;
                } else {
                    r = new int[0];
                }
            } else if (mediaType == 3) {
                if (modifiedService == 0) {
                    r = new int[]{1, 513};
                } else if (modifiedService == 1) {
                    r[0] = 513;
                } else if (modifiedService == 2) {
                    r[0] = 1;
                } else {
                    r = new int[0];
                }
            }
            for (int i = 0; i < r.length; i++) {
                print_r = print_r + serviceClassToString(r[i]) + " ";
            }
            Rlog.d(MMTelSSTransport.LOG_TAG, "serviceNeedToCreate(): " + print_r + ", mediaType=" + mediaTypeToString(mediaType) + ", modifiedService=" + modifiedServiceToString(modifiedService));
            return r;
        }

        public List<Rule> createCFRule(int modifiedRuleService, RuleSet ruleSet, int reason, int action, int serviceClass, String number, int time, String timeSlot) {
            String ruleIdPostfix;
            Rlog.d(MMTelSSTransport.LOG_TAG, "createCFRule() modifiedRuleService=" + modifiedServiceToString(modifiedRuleService) + ", reason=" + reasonCFToString(reason) + ", action=" + actionCFToString(action) + ", service=" + serviceClassToString(serviceClass) + ", number=" + MtkSuppServHelper.encryptString(number) + ", time=" + time + ", timeSlot=" + timeSlot);
            List<Rule> result = new ArrayList<>();
            if ((serviceClass & 1) != 0) {
                result.addAll(createCFRuleForService(ruleSet, reason, action, 1, number, time, timeSlot, ""));
            } else {
                int i = 2;
                if ((serviceClass & 512) != 0) {
                    if (MMTelSSTransport.mSSConfig.getMediaTagType() == 2) {
                        ruleIdPostfix = "_VIDEO";
                    } else {
                        ruleIdPostfix = "";
                    }
                    result.addAll(createCFRuleForService(ruleSet, reason, action, 512, number, time, timeSlot, ruleIdPostfix));
                } else if (serviceClass == 0) {
                    int[] serviceNeedCreate = serviceNeedToCreate(modifiedRuleService);
                    int i2 = 0;
                    while (i2 < serviceNeedCreate.length) {
                        String ruleIdPostfix2 = "";
                        if (serviceNeedCreate[i2] == 512 && MMTelSSTransport.mSSConfig.getMediaTagType() == i) {
                            ruleIdPostfix2 = "_VIDEO";
                        }
                        result.addAll(createCFRuleForService(ruleSet, reason, action, serviceNeedCreate[i2], number, time, timeSlot, ruleIdPostfix2));
                        i2++;
                        serviceNeedCreate = serviceNeedCreate;
                        i = i;
                    }
                }
            }
            return result;
        }

        public List<Rule> getRuleForSetCF(CommunicationDiversion cd, int reason, int action, int serviceClass, String number, int time, String timeSlot) {
            List<Rule> result;
            int modifiedRuleService;
            int i;
            List<Rule> ruleList;
            String str;
            MMTelSSTransmitter mMTelSSTransmitter = this;
            int i2 = reason;
            String str2 = MMTelSSTransport.LOG_TAG;
            Rlog.d(str2, "getRuleForSetCF() reason=" + mMTelSSTransmitter.reasonCFToString(i2) + ", action=" + mMTelSSTransmitter.actionCFToString(action) + ", service=" + mMTelSSTransmitter.serviceClassToString(serviceClass) + ", number=" + MtkSuppServHelper.encryptString(number) + ", time=" + time + ", timeSlot=" + timeSlot);
            List<Rule> result2 = new ArrayList<>();
            RuleSet ruleSet = cd.getRuleSet();
            boolean foundRule = false;
            int modifiedRuleService2 = 0;
            List<Rule> ruleList2 = ruleSet.getRules();
            if (ruleList2 == null) {
                result = result2;
                modifiedRuleService = 0;
            } else if (MMTelSSTransport.mSSConfig.getMediaTagType() == 2) {
                List<Rule> modifiedRuleList = new ArrayList<>();
                result = result2;
                int modifiedRuleService3 = 0 | modifyCFRuleForSeperateMedia(modifiedRuleList, ruleSet, reason, action, serviceClass, number, time, timeSlot);
                result.addAll(modifiedRuleList);
                if (modifiedRuleList.size() > 0) {
                    foundRule = true;
                }
                modifiedRuleService = modifiedRuleService3;
            } else {
                List<Rule> ruleList3 = ruleList2;
                result = result2;
                int i3 = 0;
                while (i3 < ruleList3.size()) {
                    Rule rule = ruleList3.get(i3);
                    if (mMTelSSTransmitter.getCFType(rule.getConditions()) == i2) {
                        foundRule = true;
                        List<Rule> modifiedRuleList2 = new ArrayList<>();
                        ruleList = ruleList3;
                        i = i3;
                        str = str2;
                        int modifiedRuleService4 = modifiedRuleService2 | modifyCFRule(modifiedRuleList2, ruleSet, rule, reason, action, serviceClass, number, time, timeSlot);
                        if (rule.getActions().getFowardTo().mIsValidTargetNumber || MMTelSSTransport.mSSConfig.isSupportPutNonUriNumber()) {
                            result.addAll(modifiedRuleList2);
                        } else {
                            Rlog.d(str, "getRuleForSetCF() skip rule = " + rule.toXmlString());
                        }
                        modifiedRuleService2 = modifiedRuleService4;
                    } else {
                        ruleList = ruleList3;
                        i = i3;
                        str = str2;
                    }
                    i3 = i + 1;
                    mMTelSSTransmitter = this;
                    str2 = str;
                    ruleList3 = ruleList;
                    i2 = reason;
                }
                modifiedRuleService = modifiedRuleService2;
            }
            if ((action == 1 || action == 3) && (!foundRule || modifiedRuleService == 0 || (serviceClass == 0 && (modifiedRuleService == 1 || modifiedRuleService == 2)))) {
                result.addAll(createCFRule(modifiedRuleService, ruleSet, reason, action, serviceClass, number, time, timeSlot));
            }
            return result;
        }

        public boolean isEmptyCF(CommunicationDiversion cd) {
            boolean r = false;
            RuleSet ruleSet = cd.getRuleSet();
            List<Rule> ruleList = ruleSet == null ? null : ruleSet.getRules();
            if (ruleSet == null || ruleList == null) {
                r = true;
            }
            Rlog.d(MMTelSSTransport.LOG_TAG, "isEmptyCF()=" + r);
            return r;
        }

        public String convertUriToNumber(String uri) {
            String r = uri;
            if (uri != null && (uri.startsWith("sip:") || uri.startsWith("sips:"))) {
                int offset = uri.indexOf(";");
                if (offset == MMTelSSTransport.INVALID_PHONE_ID) {
                    offset = uri.length();
                }
                r = uri.substring(uri.indexOf(":") + 1, offset);
                if (r.contains("@")) {
                    r = r.substring(r.indexOf(":") + 1, r.indexOf("@"));
                }
            } else if (uri != null && uri.startsWith("tel:")) {
                int offset2 = uri.indexOf(";");
                if (offset2 == MMTelSSTransport.INVALID_PHONE_ID) {
                    offset2 = uri.length();
                }
                r = uri.substring(uri.indexOf(":") + 1, offset2);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("convertUriToNumber: ");
            sb.append(!MMTelSSTransport.SENLOG ? r : "[hidden]");
            Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
            return r;
        }

        public boolean isAllRulesDeativated(List<Rule> ruleList) {
            for (Rule rule : ruleList) {
                if (!rule.getConditions().comprehendRuleDeactivated()) {
                    return false;
                }
            }
            return true;
        }

        public String convertCFNumber(String number, int phoneId) {
            if (number != null) {
                boolean isContainPlus = number.contains("+");
                boolean isContainContext = number.contains("phone-context");
                boolean isURIFormat = number.startsWith("tel:") || number.startsWith("sip:") || number.startsWith("sips:");
                boolean isPhoneContextNeed = false;
                String domain = MMTelSSUtils.getXui(phoneId, MMTelSSTransport.this.mContext);
                if (!isContainPlus && !isContainContext) {
                    int offset = domain.indexOf("@");
                    if (offset != MMTelSSTransport.INVALID_PHONE_ID) {
                        domain = domain.substring(offset + 1);
                        isPhoneContextNeed = true;
                    }
                    if (!TextUtils.isEmpty(MMTelSSTransport.mSSConfig.getPhoneContext())) {
                        domain = MMTelSSTransport.mSSConfig.getPhoneContext();
                        isPhoneContextNeed = true;
                    }
                    Rlog.d(MMTelSSTransport.LOG_TAG, "domain:" + domain);
                }
                if (isPhoneContextNeed) {
                    if (isURIFormat) {
                        number = number + ";phone-context=" + domain;
                    } else if (MMTelSSTransport.mSSConfig.isFwdNumUseSipUri()) {
                        number = "sip:" + number + ";phone-context=" + domain + "@" + domain + ";user=phone";
                    } else {
                        number = "tel:" + number + ";phone-context=" + domain;
                    }
                } else if (!isURIFormat) {
                    number = "tel:" + number;
                }
            }
            String XcapCFNum = SystemProperties.get(MMTelSSTransport.PROP_SS_CFNUM, "");
            if (!XcapCFNum.startsWith("sip:") && !XcapCFNum.startsWith("sips:") && !XcapCFNum.startsWith("tel:")) {
                return number;
            }
            Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetCF():get call forwarding num from EM setting:" + XcapCFNum);
            String ss_mode = SystemProperties.get(MMTelSSTransport.PROP_SS_MODE, MMTelSSTransport.MODE_SS_XCAP);
            Rlog.d(MMTelSSTransport.LOG_TAG, "handleSetCF():ss_mode=" + ss_mode);
            return MMTelSSTransport.MODE_SS_XCAP.equals(ss_mode) ? XcapCFNum : number;
        }

        public int convertServiceClass(int serviceClass) {
            if (serviceClass == 528) {
                return 512;
            }
            return serviceClass;
        }

        public void triggerCSFB(Message msg) {
            Rlog.d(MMTelSSTransport.LOG_TAG, "triggerCSFB msg=" + msg);
            if (msg != null) {
                AsyncResult.forMessage(msg, (Object) null, new UnknownHostException());
                msg.sendToTarget();
            }
        }

        public void handleSetCF(MMTelSSRequest rr) {
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            rr.mp.readInt();
            int action = rr.mp.readInt();
            int reason = rr.mp.readInt();
            int serviceClass = rr.mp.readInt();
            String number = rr.mp.readString();
            int time = rr.mp.readInt();
            int phoneId = rr.mp.readInt();
            Message msg = rr.mResult;
            Rlog.d(MMTelSSTransport.LOG_TAG, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo) + ", action=" + action + ", reason=" + reason + ", serviceClass=" + serviceClass + ", number=" + MtkSuppServHelper.encryptString(number) + ", time=" + time + ", phoneId=" + phoneId + ", msg=" + msg);
            handleSetCF(reqNo, action, reason, serviceClass, number, time, null, phoneId, msg);
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX INFO: Multiple debug info for r14v14 'newReason'  int: [D('exceptionReport' java.lang.Exception), D('newReason' int)] */
        /* JADX WARN: Type inference failed for: r1v47, types: [java.lang.StringBuilder] */
        /* JADX WARN: Type inference failed for: r17v14 */
        /* JADX WARNING: Removed duplicated region for block: B:137:0x0305  */
        /* JADX WARNING: Removed duplicated region for block: B:140:0x0314  */
        /* JADX WARNING: Removed duplicated region for block: B:156:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x00f7  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0114 A[Catch:{ XcapException -> 0x02da, Exception -> 0x02d5 }] */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x01a7 A[Catch:{ XcapException -> 0x02da, Exception -> 0x02d5 }] */
        /* JADX WARNING: Removed duplicated region for block: B:64:0x01c2 A[Catch:{ XcapException -> 0x02da, Exception -> 0x02d5 }] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public void handleSetCF(int reqNo, int action, int reason, int serviceClass, String number, int time, String timeSlot, int phoneId, Message msg) {
            MMTelSSTransmitter mMTelSSTransmitter;
            Message message;
            Exception exceptionReport;
            int i;
            MMTelSSTransmitter mMTelSSTransmitter2;
            MMTelSSTransmitter mMTelSSTransmitter3;
            Exception exceptionReport2;
            int i2;
            List<Rule> resultList;
            String str;
            int i3;
            int i4;
            int i5;
            int i6;
            int i7;
            char c;
            boolean needAdd;
            int newReason;
            boolean needAdd2;
            Exception exceptionReport3;
            boolean z;
            int newReason2;
            int i8;
            int serviceClass2;
            boolean needAdd3;
            List<Rule> resultList2;
            String str2;
            if (!MMTelSSUtils.isPreferXcap(phoneId, MMTelSSTransport.this.mContext)) {
                mMTelSSTransmitter = this;
                message = msg;
            } else if (!MMTelSSTransport.this.updateNetworkInitSimServ(phoneId)) {
                mMTelSSTransmitter = this;
                message = msg;
            } else {
                Exception exceptionReport4 = null;
                String number2 = convertCFNumber(number, phoneId);
                int serviceClass3 = convertServiceClass(serviceClass);
                String str3 = MMTelSSTransport.LOG_TAG;
                Rlog.d(str3, "handleSetCF() " + MMTelSSTransport.requestToString(reqNo) + ", action=" + actionCFToString(action) + ", reason=" + reasonCFToString(reason) + ", serviceClass=" + serviceClassToString(serviceClass3) + ", number=" + MtkSuppServHelper.encryptString(number2) + ", time=" + time + ", timeSlot=" + timeSlot + ", phoneId=" + phoneId);
                char c2 = 2;
                int i9 = 0;
                try {
                    CommunicationDiversion cd = (CommunicationDiversion) getCache(0, phoneId);
                    boolean isEmptyRules = isEmptyCF(cd);
                    List<Rule> resultList3 = new ArrayList<>();
                    if (reason == 5) {
                        int i10 = 0;
                        while (i10 < 4) {
                            try {
                                int newReason3 = MMTelSSTransport.CF_REASON[i10];
                                if (MMTelSSTransport.mSSConfig.isNotSupportCFNotRegistered()) {
                                    exceptionReport3 = exceptionReport4;
                                    newReason2 = newReason3;
                                    if (newReason2 == 6) {
                                        z = i9;
                                        ? sb = new StringBuilder();
                                        sb.append("reason == 4, needAdd = ");
                                        sb.append(z);
                                        Rlog.d(str3, sb.toString());
                                        if (!z) {
                                            resultList2 = resultList3;
                                            needAdd3 = z;
                                            str2 = str3;
                                            serviceClass2 = serviceClass3;
                                            try {
                                                resultList2.addAll(getRuleForSetCF(cd, newReason2, action, serviceClass3, number2, time, timeSlot));
                                            } catch (XcapException e) {
                                                xcapException = e;
                                                i6 = 0;
                                                mMTelSSTransmitter3 = this;
                                                xcapException.printStackTrace();
                                                exceptionReport = mMTelSSTransmitter2.reportXcapException(xcapException);
                                                mMTelSSTransmitter2.clearCache(i);
                                                if (msg != null) {
                                                }
                                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                                }
                                            } catch (Exception e2) {
                                                e = e2;
                                                i7 = 0;
                                                mMTelSSTransmitter3 = this;
                                                e.printStackTrace();
                                                exceptionReport = CommandException.fromRilErrno(2);
                                                mMTelSSTransmitter2.clearCache(i);
                                                if (msg != null) {
                                                }
                                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                                }
                                            }
                                        } else {
                                            resultList2 = resultList3;
                                            needAdd3 = z;
                                            str2 = str3;
                                            serviceClass2 = serviceClass3;
                                        }
                                        i10++;
                                        str3 = str2;
                                        resultList3 = resultList2;
                                        serviceClass3 = serviceClass2;
                                        i9 = 0;
                                        c2 = 2;
                                        exceptionReport4 = exceptionReport3;
                                    }
                                } else {
                                    exceptionReport3 = exceptionReport4;
                                    newReason2 = newReason3;
                                }
                                z = true;
                                try {
                                    ? sb2 = new StringBuilder();
                                } catch (XcapException e3) {
                                    xcapException = e3;
                                    i8 = i9;
                                    mMTelSSTransmitter3 = this;
                                    xcapException.printStackTrace();
                                    exceptionReport = mMTelSSTransmitter2.reportXcapException(xcapException);
                                    mMTelSSTransmitter2.clearCache(i);
                                    if (msg != null) {
                                    }
                                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                    }
                                } catch (Exception e4) {
                                    e = e4;
                                    i = i9;
                                    mMTelSSTransmitter3 = this;
                                    e.printStackTrace();
                                    exceptionReport = CommandException.fromRilErrno(2);
                                    mMTelSSTransmitter2.clearCache(i);
                                    if (msg != null) {
                                    }
                                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                    }
                                }
                            } catch (XcapException e5) {
                                xcapException = e5;
                                i = i9;
                                mMTelSSTransmitter3 = this;
                                xcapException.printStackTrace();
                                exceptionReport = mMTelSSTransmitter2.reportXcapException(xcapException);
                                mMTelSSTransmitter2.clearCache(i);
                                if (msg != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                }
                            } catch (Exception e6) {
                                e = e6;
                                i = i9;
                                mMTelSSTransmitter3 = this;
                                e.printStackTrace();
                                exceptionReport = CommandException.fromRilErrno(2);
                                mMTelSSTransmitter2.clearCache(i);
                                if (msg != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                }
                            }
                            try {
                                sb2.append("reason == 4, needAdd = ");
                                sb2.append(z);
                                Rlog.d(str3, sb2.toString());
                                if (!z) {
                                }
                                i10++;
                                str3 = str2;
                                resultList3 = resultList2;
                                serviceClass3 = serviceClass2;
                                i9 = 0;
                                c2 = 2;
                                exceptionReport4 = exceptionReport3;
                            } catch (XcapException e7) {
                                xcapException = e7;
                                i8 = 0;
                                mMTelSSTransmitter3 = this;
                                xcapException.printStackTrace();
                                exceptionReport = mMTelSSTransmitter2.reportXcapException(xcapException);
                                mMTelSSTransmitter2.clearCache(i);
                                if (msg != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                }
                            } catch (Exception e8) {
                                e = e8;
                                i = 0;
                                mMTelSSTransmitter3 = this;
                                e.printStackTrace();
                                exceptionReport = CommandException.fromRilErrno(2);
                                mMTelSSTransmitter2.clearCache(i);
                                if (msg != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                }
                            }
                        }
                        resultList = resultList3;
                        str = str3;
                        exceptionReport2 = exceptionReport4;
                        i2 = 5;
                    } else {
                        resultList = resultList3;
                        str = str3;
                        exceptionReport2 = null;
                        if (reason == 4) {
                            int i11 = 0;
                            while (true) {
                                i2 = 5;
                                if (i11 >= 5) {
                                    break;
                                }
                                int newReason4 = MMTelSSTransport.CF_REASON[i11];
                                if (MMTelSSTransport.mSSConfig.isNotSupportCFNotRegistered()) {
                                    c = 6;
                                    c = 6;
                                    if (newReason4 == 6) {
                                        needAdd = false;
                                        Rlog.d(str, "reason == 5, needAdd = " + needAdd);
                                        if (!needAdd) {
                                            needAdd2 = needAdd;
                                            newReason = newReason4;
                                            resultList.addAll(getRuleForSetCF(cd, newReason4, action, serviceClass3, number2, time, timeSlot));
                                        } else {
                                            needAdd2 = needAdd;
                                            newReason = newReason4;
                                        }
                                        i11++;
                                    }
                                } else {
                                    c = 6;
                                }
                                needAdd = true;
                                Rlog.d(str, "reason == 5, needAdd = " + needAdd);
                                if (!needAdd) {
                                }
                                i11++;
                            }
                        } else {
                            i2 = 5;
                            i2 = 5;
                            i2 = 5;
                            resultList.addAll(getRuleForSetCF(cd, reason, action, serviceClass3, number2, time, timeSlot));
                            if (reason == 3 && MMTelSSTransport.mSSConfig.isSetCFNRcWithCFNL()) {
                                resultList.addAll(getRuleForSetCF(cd, 6, action, serviceClass3, number2, time, timeSlot));
                            }
                        }
                    }
                    if (MMTelSSTransport.mSSConfig.isSupportPutCfRoot()) {
                        if (!MMTelSSTransport.mSSConfig.isSaveWholeNode() && reason != i2) {
                            if (reason != 4) {
                                i3 = 0;
                            }
                        }
                        cd.getRuleSet().getRules();
                        if (time <= 0) {
                            i5 = 0;
                        } else if (reason == i2 || reason == 4 || reason == 2) {
                            i5 = 0;
                            try {
                                cd.setNoReplyTimer(time, false);
                            } catch (XcapException e9) {
                                xcapException = e9;
                                i6 = 0;
                                mMTelSSTransmitter3 = this;
                                xcapException.printStackTrace();
                                exceptionReport = mMTelSSTransmitter2.reportXcapException(xcapException);
                                mMTelSSTransmitter2.clearCache(i);
                                if (msg != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                }
                            } catch (Exception e10) {
                                e = e10;
                                i7 = 0;
                                mMTelSSTransmitter3 = this;
                                e.printStackTrace();
                                exceptionReport = CommandException.fromRilErrno(2);
                                mMTelSSTransmitter2.clearCache(i);
                                if (msg != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                }
                            }
                        } else {
                            i5 = 0;
                        }
                        cd.save(true);
                        i4 = i5;
                        mMTelSSTransmitter2 = this;
                        exceptionReport = exceptionReport2;
                        mMTelSSTransmitter2.clearCache(i);
                        if (msg != null) {
                            AsyncResult.forMessage(msg, (Object) null, exceptionReport);
                            msg.sendToTarget();
                        }
                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                            MMTelSSTransport.this.mXcapMobileDataNetworkManager.releaseNetwork();
                            return;
                        }
                        return;
                    }
                    i3 = 0;
                    if (isEmptyRules) {
                        cd.saveRuleSet();
                        i4 = i3;
                        mMTelSSTransmitter2 = this;
                    } else {
                        for (Rule rule : resultList) {
                            Rlog.d(str, "handleSetCF(): rule=" + MtkSuppServHelper.encryptString(rule.toXmlString()));
                            cd.saveRule(rule);
                            i = i3;
                            mMTelSSTransmitter3 = this;
                            try {
                                if (mMTelSSTransmitter3.getCFType(rule.getConditions()) == 2 && time > 0) {
                                    if (cd.getNoReplyTimer() > MMTelSSTransport.INVALID_PHONE_ID) {
                                        if (!MMTelSSTransport.mSSConfig.isNoReplyTimeInsideCFAction()) {
                                            Rlog.d(str, "Set outside no-reply timer = " + time);
                                            cd.setNoReplyTimer(time, true);
                                        }
                                    }
                                }
                                i3 = i;
                            } catch (XcapException e11) {
                                xcapException = e11;
                                xcapException.printStackTrace();
                                exceptionReport = mMTelSSTransmitter2.reportXcapException(xcapException);
                                mMTelSSTransmitter2.clearCache(i);
                                if (msg != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                }
                            } catch (Exception e12) {
                                e = e12;
                                e.printStackTrace();
                                exceptionReport = CommandException.fromRilErrno(2);
                                mMTelSSTransmitter2.clearCache(i);
                                if (msg != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                                }
                            }
                        }
                        i4 = i3;
                        mMTelSSTransmitter2 = this;
                    }
                    exceptionReport = exceptionReport2;
                } catch (XcapException e13) {
                    xcapException = e13;
                    i = 0;
                    mMTelSSTransmitter3 = this;
                    xcapException.printStackTrace();
                    exceptionReport = mMTelSSTransmitter2.reportXcapException(xcapException);
                    mMTelSSTransmitter2.clearCache(i);
                    if (msg != null) {
                    }
                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                    }
                } catch (Exception e14) {
                    e = e14;
                    i = 0;
                    mMTelSSTransmitter3 = this;
                    e.printStackTrace();
                    exceptionReport = CommandException.fromRilErrno(2);
                    mMTelSSTransmitter2.clearCache(i);
                    if (msg != null) {
                    }
                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                    }
                }
                mMTelSSTransmitter2.clearCache(i);
                if (msg != null) {
                }
                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager == null) {
                }
            }
            mMTelSSTransmitter.triggerCSFB(message);
        }

        public List<Rule> createCBRuleForService(RuleSet ruleSet, String facility, int serviceClass, int lockState) {
            StringBuilder sb = new StringBuilder();
            sb.append("createCBRuleForService() facility=");
            sb.append(facility);
            sb.append(", lockState=");
            sb.append(lockState == 1 ? "Enable" : "Disable");
            sb.append(", service=");
            sb.append(serviceClassToString(serviceClass));
            Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
            List<Rule> result = new ArrayList<>();
            Rule rule = ruleSet.createNewRule(facility);
            Conditions cond = rule.createConditions();
            Actions act = rule.createActions();
            if (facility.equals("OI")) {
                cond.addInternational();
            } else if (facility.equals("OX")) {
                cond.addInternationalExHc();
            } else if (facility.equals("IR")) {
                cond.addRoaming();
            }
            setMedia(cond, serviceClass);
            act.setAllow(false);
            result.add(rule);
            return result;
        }

        public List<Rule> createCBRule(int modifiedRuleService, RuleSet ruleSet, String facility, int serviceClass, int lockState) {
            int[] serviceNeedCreate;
            StringBuilder sb = new StringBuilder();
            sb.append("createCBRule()  modifiedRuleService=");
            sb.append(modifiedServiceToString(modifiedRuleService));
            sb.append(", facility=");
            sb.append(facility);
            sb.append(", lockState=");
            sb.append(lockState == 1 ? "Enable" : "Disable");
            sb.append(", service=");
            sb.append(serviceClassToString(serviceClass));
            Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
            List<Rule> result = new ArrayList<>();
            if ((serviceClass & 1) != 0) {
                result.addAll(createCBRuleForService(ruleSet, facility, 1, lockState));
            } else if ((serviceClass & 512) != 0) {
                result.addAll(createCBRuleForService(ruleSet, facility, 512, lockState));
            } else if (serviceClass == 0) {
                for (int i : serviceNeedToCreate(modifiedRuleService)) {
                    result.addAll(createCBRuleForService(ruleSet, facility, i, lockState));
                }
            }
            return result;
        }

        public List<Rule> modifyMatchedCBRule(Rule rule, String facility, int serviceClass, int lockState) {
            StringBuilder sb = new StringBuilder();
            sb.append("modifyMatchedCBRule() facility=");
            sb.append(facility);
            sb.append(", lockState=");
            sb.append(lockState == 1 ? "Enable" : "Disable");
            sb.append(", service=");
            sb.append(serviceClassToString(serviceClass));
            Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
            List<Rule> result = new ArrayList<>();
            Conditions cond = rule.getConditions();
            Actions act = rule.getActions();
            if (lockState == 1) {
                cond.removeRuleDeactivated();
                act.setAllow(false);
            } else {
                cond.addRuleDeactivated();
            }
            result.add(rule);
            return result;
        }

        public int modifyCBRule(List<Rule> result, Rule rule, String facility, int serviceClass, int lockState) {
            StringBuilder sb = new StringBuilder();
            sb.append("modifyCBRule() facility=");
            sb.append(facility);
            sb.append(", lockState=");
            sb.append(lockState == 1 ? "Enable" : "Disable");
            sb.append(", service=");
            sb.append(serviceClassToString(serviceClass));
            Rlog.d(MMTelSSTransport.LOG_TAG, sb.toString());
            int r = 0;
            int isMatchMedia = isRuleMatchServiceClass(rule, serviceClass);
            if (isMatchMedia != 0) {
                result.addAll(modifyMatchedCBRule(rule, facility, serviceClass, lockState));
            }
            if ((isMatchMedia & 1) != 0) {
                r = 0 | 1;
            }
            if ((isMatchMedia & 2) != 0) {
                return r | 2;
            }
            return r;
        }

        public List<Rule> getRuleForSetCB(SimservType cb, String facility, int serviceClass, int lockState, int phoneId) {
            RuleSet ruleSet;
            int foundRule;
            int modifiedRuleService;
            Rlog.d(MMTelSSTransport.LOG_TAG, "getRuleForSetCB() facility=" + facility + ", service=" + serviceClassToString(serviceClass) + ", lockState=" + lockState + ", phoneId=" + phoneId);
            List<Rule> result = new ArrayList<>();
            if (getCBType(facility) == 1) {
                ruleSet = ((OutgoingCommunicationBarring) cb).getRuleSet();
            } else if (getCBType(facility) == 2) {
                ruleSet = ((IncomingCommunicationBarring) cb).getRuleSet();
            } else {
                ruleSet = null;
            }
            int modifiedRuleService2 = 0;
            List<Rule> ruleList = ruleSet == null ? null : ruleSet.getRules();
            if (ruleList != null) {
                int modifiedRuleService3 = 0;
                for (Rule rule : ruleList) {
                    Conditions cond = rule.getConditions();
                    rule.getActions();
                    if (getCBFacility(getCBType(facility), cond).equals(facility)) {
                        List<Rule> modifiedRuleList = new ArrayList<>();
                        result.addAll(modifiedRuleList);
                        modifiedRuleService3 |= modifyCBRule(modifiedRuleList, rule, facility, serviceClass, lockState);
                        modifiedRuleService2 = 1;
                    }
                }
                foundRule = modifiedRuleService2;
                modifiedRuleService = modifiedRuleService3;
            } else {
                foundRule = 0;
                modifiedRuleService = 0;
            }
            if (lockState == 1) {
                if (foundRule == 0 || modifiedRuleService == 0 || (serviceClass == 0 && (modifiedRuleService == 1 || modifiedRuleService == 2))) {
                    result.addAll(createCBRule(modifiedRuleService, ruleSet, facility, serviceClass, lockState));
                }
            }
            return result;
        }

        public boolean isEmptyCB(int cbType, SimservType cb) {
            boolean r = false;
            RuleSet ruleSet = null;
            if (cbType == 1) {
                ruleSet = ((OutgoingCommunicationBarring) cb).getRuleSet();
            } else if (cbType == 2) {
                ruleSet = ((IncomingCommunicationBarring) cb).getRuleSet();
            }
            List<Rule> ruleList = ruleSet == null ? null : ruleSet.getRules();
            if (ruleSet == null || ruleList == null) {
                r = true;
            }
            Rlog.d(MMTelSSTransport.LOG_TAG, "isEmptyCB()=" + r);
            return r;
        }

        /* JADX WARN: Failed to insert an additional move for type inference into block B:33:0x0141 */
        /* JADX WARN: Failed to insert an additional move for type inference into block B:46:0x0188 */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.util.List} */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX INFO: Multiple debug info for r3v24 ?: [D('facility' java.lang.String), D('resultOcbList' java.util.List<com.mediatek.simservs.client.policy.Rule>)] */
        /* JADX WARN: Type inference failed for: r3v21, types: [java.util.List] */
        /* JADX WARN: Type inference failed for: r3v22 */
        /* JADX WARN: Type inference failed for: r3v23 */
        /* JADX WARN: Type inference failed for: r3v24, types: [java.lang.String] */
        /* JADX WARN: Type inference failed for: r3v25 */
        /* JADX WARNING: Code restructure failed: missing block: B:101:0x02a6, code lost:
            if (r12 == 2) goto L_0x02aa;
         */
        /* JADX WARNING: Removed duplicated region for block: B:134:0x0376 A[ADDED_TO_REGION] */
        /* JADX WARNING: Removed duplicated region for block: B:143:0x0393  */
        /* JADX WARNING: Removed duplicated region for block: B:146:0x03a6  */
        /* JADX WARNING: Removed duplicated region for block: B:158:? A[RETURN, SYNTHETIC] */
        public void handleSetCB(MMTelSSRequest rr) {
            int cbType;
            Exception e;
            boolean isEmptyOcbRules;
            SimservType ocb;
            String str;
            List<Rule> resultIcbList;
            List<Rule> list;
            int phoneId;
            int serviceClass;
            int reqNo;
            List<Rule> resultIcbList2;
            String str2;
            int i;
            rr.mp.setDataPosition(0);
            int reqNo2 = rr.mp.readInt();
            rr.mp.readInt();
            String facility = rr.mp.readString();
            int lockState = rr.mp.readInt();
            int serviceClass2 = rr.mp.readInt();
            int phoneId2 = rr.mp.readInt();
            String str3 = MMTelSSTransport.LOG_TAG;
            Rlog.d(str3, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo2) + ", facility=" + facility + ", serviceClass=" + serviceClass2 + ", lockState=" + lockState + ", phoneId=" + phoneId2);
            if (MMTelSSUtils.isPreferXcap(phoneId2, MMTelSSTransport.this.mContext)) {
                if (MMTelSSTransport.this.updateNetworkInitSimServ(phoneId2)) {
                    Exception exceptionReport = null;
                    exceptionReport = null;
                    exceptionReport = null;
                    exceptionReport = null;
                    exceptionReport = null;
                    exceptionReport = null;
                    int serviceClass3 = convertServiceClass(serviceClass2);
                    int i2 = getCBType(facility);
                    Rlog.d(str3, "handleSetCB() " + MMTelSSTransport.requestToString(reqNo2) + ", facility=" + facility + ", cbType=" + cbTypeToString(i2) + ", serviceClass=" + serviceClassToString(serviceClass3) + ", lockState=" + lockState + ", phoneId=" + phoneId2);
                    int cbType2 = 4;
                    int i3 = 3;
                    int i4 = 5;
                    int i5 = 2;
                    boolean z = true;
                    if (lockState == 0 || !(i2 == 5 || i2 == 3 || i2 == 4)) {
                        SimservType icb = null;
                        boolean isEmptyIcbRules = false;
                        if (i2 == 5 || i2 == 3 || i2 == 1) {
                            try {
                                SimservType ocb2 = getCache(1, phoneId2);
                                isEmptyOcbRules = isEmptyCB(1, ocb2);
                                ocb = ocb2;
                            } catch (XcapException e2) {
                                xcapException = e2;
                                cbType2 = i2;
                                cbType = 2;
                                reqNo2 = 3;
                                xcapException.printStackTrace();
                                exceptionReport = reportXcapException(xcapException);
                                e = exceptionReport;
                                if (cbType2 != 1) {
                                }
                                clearCache(1);
                                if (rr.mResult != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                }
                            } catch (Exception e3) {
                                e = e3;
                                cbType2 = i2;
                                cbType = 2;
                                reqNo2 = 3;
                                e.printStackTrace();
                                e = CommandException.fromRilErrno(cbType);
                                if (cbType2 != 1) {
                                }
                                clearCache(1);
                                if (rr.mResult != null) {
                                }
                                if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                }
                            }
                        } else {
                            isEmptyOcbRules = false;
                            ocb = null;
                        }
                        if (i2 == 5 || i2 == 4 || i2 == 2) {
                            SimservType icb2 = getCache(2, phoneId2);
                            isEmptyIcbRules = isEmptyCB(2, icb2);
                            icb = icb2;
                        }
                        List arrayList = new ArrayList();
                        List<Rule> resultIcbList3 = new ArrayList<>();
                        if (i2 == 5) {
                            String facility2 = facility;
                            int i6 = 0;
                            while (i6 < i4) {
                                try {
                                    arrayList = MMTelSSTransport.CB_FACILITY[i6];
                                    if (i6 < i3) {
                                        cbType2 = i2;
                                        reqNo = reqNo2;
                                        reqNo2 = i3;
                                        serviceClass = serviceClass3;
                                        try {
                                            arrayList.addAll(getRuleForSetCB(ocb, arrayList, serviceClass3, lockState, phoneId2));
                                            i = i6;
                                            str2 = str3;
                                            phoneId = phoneId2;
                                            resultIcbList2 = resultIcbList3;
                                        } catch (XcapException e4) {
                                            xcapException = e4;
                                            cbType = 2;
                                            xcapException.printStackTrace();
                                            exceptionReport = reportXcapException(xcapException);
                                            e = exceptionReport;
                                            if (cbType2 != 1) {
                                            }
                                            clearCache(1);
                                            if (rr.mResult != null) {
                                            }
                                            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                            }
                                        } catch (Exception e5) {
                                            e = e5;
                                            cbType = 2;
                                            e.printStackTrace();
                                            e = CommandException.fromRilErrno(cbType);
                                            if (cbType2 != 1) {
                                            }
                                            clearCache(1);
                                            if (rr.mResult != null) {
                                            }
                                            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                            }
                                        }
                                    } else {
                                        cbType2 = i2;
                                        serviceClass = serviceClass3;
                                        reqNo = reqNo2;
                                        reqNo2 = i3;
                                        i = i6;
                                        str2 = str3;
                                        phoneId = phoneId2;
                                        try {
                                            resultIcbList2 = resultIcbList3;
                                            resultIcbList2.addAll(getRuleForSetCB(icb, arrayList, serviceClass, lockState, phoneId2));
                                        } catch (XcapException e6) {
                                            xcapException = e6;
                                            cbType = 2;
                                            xcapException.printStackTrace();
                                            exceptionReport = reportXcapException(xcapException);
                                            e = exceptionReport;
                                            if (cbType2 != 1) {
                                            }
                                            clearCache(1);
                                            if (rr.mResult != null) {
                                            }
                                            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                            }
                                        } catch (Exception e7) {
                                            e = e7;
                                            cbType = 2;
                                            e.printStackTrace();
                                            e = CommandException.fromRilErrno(cbType);
                                            if (cbType2 != 1) {
                                            }
                                            clearCache(1);
                                            if (rr.mResult != null) {
                                            }
                                            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                            }
                                        }
                                    }
                                    i6 = i + 1;
                                    facility2 = arrayList;
                                    resultIcbList3 = resultIcbList2;
                                    i2 = cbType2;
                                    i3 = reqNo2;
                                    reqNo2 = reqNo;
                                    serviceClass3 = serviceClass;
                                    phoneId2 = phoneId;
                                    i5 = 2;
                                    cbType2 = 4;
                                    arrayList = arrayList;
                                    str3 = str2;
                                    i4 = 5;
                                    z = true;
                                } catch (XcapException e8) {
                                    xcapException = e8;
                                    cbType2 = i2;
                                    reqNo2 = i3;
                                    cbType = i5;
                                    xcapException.printStackTrace();
                                    exceptionReport = reportXcapException(xcapException);
                                    e = exceptionReport;
                                    if (cbType2 != 1) {
                                    }
                                    clearCache(1);
                                    if (rr.mResult != null) {
                                    }
                                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                    }
                                } catch (Exception e9) {
                                    e = e9;
                                    cbType2 = i2;
                                    reqNo2 = i3;
                                    cbType = i5;
                                    e.printStackTrace();
                                    e = CommandException.fromRilErrno(cbType);
                                    if (cbType2 != 1) {
                                    }
                                    clearCache(1);
                                    if (rr.mResult != null) {
                                    }
                                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                    }
                                }
                            }
                            cbType2 = i2;
                            list = arrayList;
                            resultIcbList = resultIcbList3;
                            reqNo2 = i3;
                            str = str3;
                        } else {
                            cbType2 = i2;
                            list = arrayList;
                            String str4 = str3;
                            resultIcbList = resultIcbList3;
                            reqNo2 = 3;
                            if (cbType2 == 3) {
                                String facility3 = facility;
                                int i7 = 0;
                                while (i7 < 3) {
                                    facility3 = MMTelSSTransport.CB_FACILITY[i7];
                                    list.addAll(getRuleForSetCB(ocb, facility3, serviceClass3, lockState, phoneId2));
                                    i7++;
                                    str4 = str4;
                                }
                                str = str4;
                            } else {
                                str = str4;
                                if (cbType2 == 4) {
                                    String facility4 = facility;
                                    for (int i8 = 3; i8 < 5; i8++) {
                                        facility4 = MMTelSSTransport.CB_FACILITY[i8];
                                        resultIcbList.addAll(getRuleForSetCB(icb, facility4, serviceClass3, lockState, phoneId2));
                                    }
                                } else if (cbType2 == 1) {
                                    try {
                                        list.addAll(getRuleForSetCB(ocb, facility, serviceClass3, lockState, phoneId2));
                                    } catch (XcapException e10) {
                                        xcapException = e10;
                                        cbType = 2;
                                        xcapException.printStackTrace();
                                        exceptionReport = reportXcapException(xcapException);
                                        e = exceptionReport;
                                        if (cbType2 != 1) {
                                        }
                                        clearCache(1);
                                        if (rr.mResult != null) {
                                        }
                                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                        }
                                    } catch (Exception e11) {
                                        e = e11;
                                        cbType = 2;
                                        e.printStackTrace();
                                        e = CommandException.fromRilErrno(cbType);
                                        if (cbType2 != 1) {
                                        }
                                        clearCache(1);
                                        if (rr.mResult != null) {
                                        }
                                        if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                        }
                                    }
                                } else if (cbType2 == 2) {
                                    resultIcbList.addAll(getRuleForSetCB(icb, facility, serviceClass3, lockState, phoneId2));
                                }
                            }
                        }
                        if (MMTelSSTransport.mSSConfig.isSaveWholeNode()) {
                            if ((cbType2 == 5 || cbType2 == reqNo2 || cbType2 == 1) && ocb != null) {
                                if (isAllRulesDeativated(((OutgoingCommunicationBarring) ocb).getRuleSet().getRules())) {
                                    ((OutgoingCommunicationBarring) ocb).save(false);
                                } else {
                                    ((OutgoingCommunicationBarring) ocb).save(true);
                                }
                            }
                            if (cbType2 == 5 || cbType2 == 4) {
                                cbType = 2;
                            } else {
                                cbType = 2;
                                cbType = 2;
                            }
                            if (icb != null) {
                                try {
                                    if (isAllRulesDeativated(((IncomingCommunicationBarring) icb).getRuleSet().getRules())) {
                                        ((IncomingCommunicationBarring) icb).save(false);
                                    } else {
                                        ((IncomingCommunicationBarring) icb).save(true);
                                    }
                                } catch (XcapException e12) {
                                    xcapException = e12;
                                    xcapException.printStackTrace();
                                    exceptionReport = reportXcapException(xcapException);
                                    e = exceptionReport;
                                    if (cbType2 != 1) {
                                    }
                                    clearCache(1);
                                    if (rr.mResult != null) {
                                    }
                                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                    }
                                } catch (Exception e13) {
                                    e = e13;
                                    e.printStackTrace();
                                    e = CommandException.fromRilErrno(cbType);
                                    if (cbType2 != 1) {
                                    }
                                    clearCache(1);
                                    if (rr.mResult != null) {
                                    }
                                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                                    }
                                }
                            }
                        } else {
                            cbType = 2;
                            cbType = 2;
                            if (isEmptyOcbRules) {
                                ((OutgoingCommunicationBarring) ocb).saveRuleSet();
                            } else {
                                for (Rule rule : list) {
                                    Rlog.d(str, "handleSetCB(): rule=" + rule.toXmlString());
                                    ((OutgoingCommunicationBarring) ocb).saveRule(rule.mId);
                                }
                            }
                            if (isEmptyIcbRules) {
                                ((IncomingCommunicationBarring) icb).saveRuleSet();
                            } else {
                                for (Rule rule2 : resultIcbList) {
                                    Rlog.d(str, "handleSetCB(): rule=" + rule2.toXmlString());
                                    ((IncomingCommunicationBarring) icb).saveRule(rule2.mId);
                                }
                            }
                        }
                    } else {
                        try {
                            throw new Exception();
                        } catch (XcapException e14) {
                            xcapException = e14;
                            cbType2 = i2;
                            cbType = 2;
                            reqNo2 = 3;
                            xcapException.printStackTrace();
                            exceptionReport = reportXcapException(xcapException);
                            e = exceptionReport;
                            if (cbType2 != 1) {
                            }
                            clearCache(1);
                            if (rr.mResult != null) {
                            }
                            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                            }
                        } catch (Exception e15) {
                            e = e15;
                            cbType2 = i2;
                            cbType = 2;
                            reqNo2 = 3;
                            e.printStackTrace();
                            e = CommandException.fromRilErrno(cbType);
                            if (cbType2 != 1) {
                            }
                            clearCache(1);
                            if (rr.mResult != null) {
                            }
                            if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                            }
                        }
                    }
                    e = exceptionReport;
                    if (cbType2 != 1 || cbType2 == reqNo2) {
                        clearCache(1);
                    } else if (cbType2 == cbType || cbType2 == 4) {
                        clearCache(cbType);
                    } else {
                        clearCache(1);
                        clearCache(cbType);
                    }
                    if (rr.mResult != null) {
                        AsyncResult.forMessage(rr.mResult, (Object) null, e);
                        rr.mResult.sendToTarget();
                    }
                    if (MMTelSSTransport.this.mXcapMobileDataNetworkManager != null) {
                        MMTelSSTransport.this.mXcapMobileDataNetworkManager.releaseNetwork();
                        return;
                    }
                    return;
                }
            }
            triggerCSFB(rr.mResult);
        }

        public void handleGetCFInTimeSlot(MMTelSSRequest rr) {
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            int serialNo = rr.mp.readInt();
            int action = rr.mp.readInt();
            int reason = rr.mp.readInt();
            int serviceClass = rr.mp.readInt();
            int phoneId = rr.mp.readInt();
            Message msg = rr.mResult;
            Rlog.d(MMTelSSTransport.LOG_TAG, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo) + ", action=" + action + ", reason=" + reason + ", serviceClass=" + serviceClass + ", phoneId=" + phoneId + ", msg=" + msg);
            handleGetCF(reqNo, serialNo, action, reason, serviceClass, null, phoneId, true, msg);
        }

        public void handleSetCFInTimeSlot(MMTelSSRequest rr) {
            long[] timeSlot;
            rr.mp.setDataPosition(0);
            int reqNo = rr.mp.readInt();
            rr.mp.readInt();
            int action = rr.mp.readInt();
            int reason = rr.mp.readInt();
            int serviceClass = rr.mp.readInt();
            String number = rr.mp.readString();
            int time = rr.mp.readInt();
            long[] timeSlot2 = new long[2];
            try {
                rr.mp.readLongArray(timeSlot2);
                timeSlot = timeSlot2;
            } catch (Exception e) {
                timeSlot = null;
            }
            String timeSlotString = convertToSeverTime(timeSlot);
            int phoneId = rr.mp.readInt();
            Message msg = rr.mResult;
            Rlog.d(MMTelSSTransport.LOG_TAG, "Read from parcel: " + MMTelSSTransport.requestToString(reqNo) + ", action=" + action + ", reason=" + reason + ", serviceClass=" + serviceClass + ", number=" + MtkSuppServHelper.encryptString(number) + ", time=" + time + ", timeSlot=" + timeSlotString + ", phoneId=" + phoneId + ", msg=" + msg);
            handleSetCF(reqNo, action, reason, serviceClass, number, time, timeSlotString, phoneId, msg);
        }

        public long[] convertToLocalTime(String timeSlotString) {
            long[] timeSlot = null;
            if (timeSlotString != null) {
                String[] timeArray = timeSlotString.split(",", 2);
                if (timeArray.length == 2) {
                    timeSlot = new long[2];
                    int i = 0;
                    while (i < 2) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                        try {
                            timeSlot[i] = dateFormat.parse(timeArray[i]).getTime();
                            i++;
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }
            }
            return timeSlot;
        }

        public String convertToSeverTime(long[] timeSlot) {
            String timeSlotString = null;
            if (timeSlot == null || timeSlot.length != 2) {
                return null;
            }
            for (int i = 0; i < timeSlot.length; i++) {
                Date date = new Date(timeSlot[i]);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                if (i == 0) {
                    timeSlotString = dateFormat.format(date);
                } else {
                    timeSlotString = timeSlotString + "," + dateFormat.format(date);
                }
            }
            return timeSlotString;
        }

        public void handleMessage(Message msg) {
            MMTelSSRequest rr = (MMTelSSRequest) msg.obj;
            int i = msg.what;
            if (i == 1) {
                boolean alreadySubtracted = false;
                Rlog.d(MMTelSSTransport.LOG_TAG, "handleMessage(): EVENT_SEND:mRequestMessagesPending = " + MMTelSSTransport.this.mRequestMessagesPending + ", mRequestsList.size() = " + MMTelSSTransport.this.mRequestsList.size());
                try {
                    synchronized (MMTelSSTransport.this.mRequestsList) {
                        MMTelSSTransport.this.mRequestsList.add(rr);
                    }
                    MMTelSSTransport.this.mRequestMessagesPending--;
                    alreadySubtracted = true;
                    MMTelSSRequest unused = MMTelSSTransport.this.findAndRemoveRequestFromList(rr.mSerial);
                    Rlog.d(MMTelSSTransport.LOG_TAG, "Receive MMTelSS Request:" + MMTelSSTransport.requestToString(rr.mRequest));
                    switch (rr.mRequest) {
                        case 1:
                            handleSetIdentity(rr, 1);
                            break;
                        case 2:
                            handleGetIdentity(rr, 1);
                            break;
                        case XcapException.NO_HTTP_RESPONSE_EXCEPTION /*{ENCODED_INT: 3}*/:
                            handleGetIdentity(rr, 0);
                            break;
                        case XcapException.HTTP_RECOVERABL_EEXCEPTION /*{ENCODED_INT: 4}*/:
                            handleGetIdentity(rr, 2);
                            break;
                        case XcapException.MALFORMED_CHALLENGE_EXCEPTION /*{ENCODED_INT: 5}*/:
                            handleGetIdentity(rr, 3);
                            break;
                        case 6:
                            handleSetCB(rr);
                            break;
                        case 7:
                            handleGetCB(rr);
                            break;
                        case 8:
                            handleSetCF(rr);
                            break;
                        case 9:
                            handleGetCF(rr);
                            break;
                        case 10:
                            handleSetCW(rr);
                            break;
                        case 11:
                            handleGetCW(rr);
                            break;
                        case 12:
                            handleSetIdentity(rr, 0);
                            break;
                        case 13:
                            handleSetIdentity(rr, 2);
                            break;
                        case 14:
                            handleSetIdentity(rr, 3);
                            break;
                        case MMTelSSTransport.MMTELSS_REQ_SET_CF_TIME_SLOT /*{ENCODED_INT: 15}*/:
                            handleSetCFInTimeSlot(rr);
                            break;
                        case MMTelSSTransport.MMTELSS_REQ_GET_CF_TIME_SLOT /*{ENCODED_INT: 16}*/:
                            handleGetCFInTimeSlot(rr);
                            break;
                        default:
                            Rlog.d(MMTelSSTransport.LOG_TAG, "Invalid MMTelSS Request:" + rr.mRequest);
                            throw new RuntimeException("Unrecognized MMTelSS Request: " + rr.mRequest);
                    }
                } catch (RuntimeException exc) {
                    Rlog.e(MMTelSSTransport.LOG_TAG, "Uncaught exception ", exc);
                    MMTelSSRequest req = MMTelSSTransport.this.findAndRemoveRequestFromList(rr.mSerial);
                    Rlog.d(MMTelSSTransport.LOG_TAG, "handleMessage(): RuntimeException:mRequestMessagesPending = " + MMTelSSTransport.this.mRequestMessagesPending + ", mRequestsList.size() = " + MMTelSSTransport.this.mRequestsList.size());
                    if (req != null || 0 == 0) {
                        rr.onError(2, null);
                        rr.release();
                    }
                } catch (Throwable th) {
                    MMTelSSTransport.this.releaseWakeLockIfDone();
                    throw th;
                }
                MMTelSSTransport.this.releaseWakeLockIfDone();
                if (!alreadySubtracted) {
                    Rlog.d(MMTelSSTransport.LOG_TAG, "handleMessage(): !alreadySubtracted:mRequestMessagesPending = " + MMTelSSTransport.this.mRequestMessagesPending + ", mRequestsList.size() = " + MMTelSSTransport.this.mRequestsList.size());
                    MMTelSSTransport mMTelSSTransport = MMTelSSTransport.this;
                    mMTelSSTransport.mRequestMessagesPending = mMTelSSTransport.mRequestMessagesPending - 1;
                }
                if (rr.mp != null) {
                    rr.mp.recycle();
                    rr.mp = null;
                }
                if (MMTelSSTransport.this.mRequestMessagesPending != 0 || MMTelSSTransport.this.mRequestsList.size() != 0) {
                    Rlog.d(MMTelSSTransport.LOG_TAG, "handleMessage(): ERROR wakeLock:mRequestMessagesPending = " + MMTelSSTransport.this.mRequestMessagesPending + ", mRequestsList.size() = " + MMTelSSTransport.this.mRequestsList.size());
                }
            } else if (i == 2) {
                synchronized (MMTelSSTransport.this.mWakeLock) {
                    if (MMTelSSTransport.this.mWakeLock.isHeld()) {
                        synchronized (MMTelSSTransport.this.mRequestsList) {
                            int count = MMTelSSTransport.this.mRequestsList.size();
                            Rlog.d(MMTelSSTransport.LOG_TAG, "WAKE_LOCK_TIMEOUT  mReqPending=" + MMTelSSTransport.this.mRequestMessagesPending + " mRequestList=" + count);
                            for (int i2 = 0; i2 < count; i2++) {
                                MMTelSSRequest rr2 = MMTelSSTransport.this.mRequestsList.get(i2);
                                Rlog.d(MMTelSSTransport.LOG_TAG, i2 + ": [" + rr2.mSerial + "] " + MMTelSSTransport.requestToString(rr2.mRequest));
                            }
                        }
                        MMTelSSTransport.this.mWakeLock.release();
                    }
                }
            }
        }
    }

    public void setCLIR(int clirMode, Message result, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(1, result);
        rr.mp.writeInt(clirMode);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void getCLIR(Message result, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(2, result);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void setCLIP(int clipEnable, Message result, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(12, result);
        rr.mp.writeInt(clipEnable);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void queryCLIP(Message result, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(3, result);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void setCOLP(int colpEnable, Message result, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(13, result);
        rr.mp.writeInt(colpEnable);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void getCOLP(Message result, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(4, result);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void setCOLR(int colrMode, Message result, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(14, result);
        rr.mp.writeInt(colrMode);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void getCOLR(Message result, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(5, result);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message response, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(10, response);
        Parcel parcel = rr.mp;
        int i = 1;
        if (!enable) {
            i = 0;
        }
        parcel.writeInt(i);
        rr.mp.writeInt(serviceClass);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void queryCallWaiting(int serviceClass, Message response, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(11, response);
        rr.mp.writeInt(serviceClass);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message response, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(6, response);
        rr.mp.writeString(facility);
        Parcel parcel = rr.mp;
        int i = 1;
        if (!lockState) {
            i = 0;
        }
        parcel.writeInt(i);
        rr.mp.writeInt(serviceClass);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void queryFacilityLock(String facility, String password, int serviceClass, Message response, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(7, response);
        rr.mp.writeString(facility);
        rr.mp.writeInt(serviceClass);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response, int phoneId) {
        StringBuilder sb = new StringBuilder();
        sb.append("number: ");
        sb.append(!SENLOG ? number : "[hidden]");
        Rlog.d(LOG_TAG, sb.toString());
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(8, response);
        rr.mp.writeInt(action);
        rr.mp.writeInt(cfReason);
        rr.mp.writeInt(serviceClass);
        rr.mp.writeString(number);
        rr.mp.writeInt(timeSeconds);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message response, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(9, response);
        rr.mp.writeInt(2);
        rr.mp.writeInt(cfReason);
        rr.mp.writeInt(serviceClass);
        if (number != null) {
            rr.mp.writeString(number);
        } else {
            rr.mp.writeString("");
        }
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void setCallForwardInTimeSlot(int action, int cfReason, int serviceClass, String number, int timeSeconds, long[] timeSlot, Message response, int phoneId) {
        if (number != null && !number.startsWith("sip:") && !number.startsWith("sips:") && !number.startsWith("tel:")) {
            number = "tel:" + number;
        }
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(MMTELSS_REQ_SET_CF_TIME_SLOT, response);
        rr.mp.writeInt(action);
        rr.mp.writeInt(cfReason);
        rr.mp.writeInt(serviceClass);
        rr.mp.writeString(number);
        rr.mp.writeInt(timeSeconds);
        rr.mp.writeLongArray(timeSlot);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    public void queryCallForwardInTimeSlotStatus(int cfReason, int serviceClass, Message response, int phoneId) {
        mSSConfig = SuppSrvConfig.getInstance(this.mContext);
        MMTelSSRequest rr = MMTelSSRequest.obtain(MMTELSS_REQ_GET_CF_TIME_SLOT, response);
        rr.mp.writeInt(2);
        rr.mp.writeInt(cfReason);
        rr.mp.writeInt(serviceClass);
        rr.mp.writeInt(phoneId);
        send(rr);
    }

    private void acquireWakeLock() {
        Rlog.d(LOG_TAG, "=>wakeLock() mRequestMessagesPending = " + this.mRequestMessagesPending + ", mRequestsList.size() = " + this.mRequestsList.size());
        synchronized (this.mWakeLock) {
            this.mWakeLock.acquire();
            this.mRequestMessagesPending++;
            this.mSender.removeMessages(2);
            this.mSender.sendMessageDelayed(this.mSender.obtainMessage(2), (long) this.mWakeLockTimeout);
        }
    }

    /* access modifiers changed from: private */
    public void releaseWakeLockIfDone() {
        Rlog.d(LOG_TAG, "wakeLock()=> mRequestMessagesPending = " + this.mRequestMessagesPending + ", mRequestsList.size() = " + this.mRequestsList.size());
        synchronized (this.mWakeLock) {
            if (this.mWakeLock.isHeld() && this.mRequestMessagesPending == 0 && this.mRequestsList.size() == 0) {
                this.mSender.removeMessages(2);
                this.mWakeLock.release();
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        return r3;
     */
    public MMTelSSRequest findAndRemoveRequestFromList(int serial) {
        synchronized (this.mRequestsList) {
            int i = 0;
            int s = this.mRequestsList.size();
            while (i < s) {
                MMTelSSRequest rr = this.mRequestsList.get(i);
                if (rr.mSerial == serial) {
                    this.mRequestsList.remove(i);
                    if (this.mRequestMessagesWaiting > 0) {
                        this.mRequestMessagesWaiting += INVALID_PHONE_ID;
                    }
                } else {
                    i++;
                }
            }
            return null;
        }
    }

    static String requestToString(int request) {
        switch (request) {
            case 1:
                return "SET_CLIR";
            case 2:
                return "GET_CLIR";
            case XcapException.NO_HTTP_RESPONSE_EXCEPTION /*{ENCODED_INT: 3}*/:
                return "GET_CLIP";
            case XcapException.HTTP_RECOVERABL_EEXCEPTION /*{ENCODED_INT: 4}*/:
                return "GET_COLP";
            case XcapException.MALFORMED_CHALLENGE_EXCEPTION /*{ENCODED_INT: 5}*/:
                return "GET_COLR";
            case 6:
                return "SET_CB";
            case 7:
                return "GET_CB";
            case 8:
                return "SET_CF";
            case 9:
                return "GET_CF";
            case 10:
                return "SET_CW";
            case 11:
                return "GET_CW";
            case 12:
                return "SET_CLIP";
            case 13:
                return "SET_COLP";
            case 14:
                return "SET_COLR";
            case MMTELSS_REQ_SET_CF_TIME_SLOT /*{ENCODED_INT: 15}*/:
                return "SET_CF_TIME_SLOT";
            case MMTELSS_REQ_GET_CF_TIME_SLOT /*{ENCODED_INT: 16}*/:
                return "GET_CF_TIME_SLOT";
            default:
                return "UNKNOWN MMTELSS REQ";
        }
    }

    private void send(MMTelSSRequest rr) {
        Message msg = this.mSender.obtainMessage(1, rr);
        acquireWakeLock();
        msg.sendToTarget();
    }
}
