package com.android.internal.telephony.dataconnection;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.LinkProperties.CompareResult;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.DctConstants.Activity;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.DataConnection.UpdateLinkPropertyResult;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.mediatek.internal.telephony.gsm.GsmVTProviderUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DcController extends StateMachine {
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_DORMANT = 1;
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_INACTIVE = 0;
    static final int DATA_CONNECTION_ACTIVE_PH_LINK_UP = 2;
    static final int DATA_CONNECTION_ACTIVE_UNKNOWN = Integer.MAX_VALUE;
    private static final boolean DBG = true;
    private static final boolean MTK_SRLTE_SUPPORT = false;
    private static final boolean MTK_SVLTE_SUPPORT = false;
    private static final String PROP_MTK_CDMA_LTE_MODE = "ro.boot.opt_c2k_lte_mode";
    private static final boolean VDBG = false;
    private HashMap<Integer, DataConnection> mDcListActiveByCid;
    ArrayList<DataConnection> mDcListAll;
    private DcTesterDeactivateAll mDcTesterDeactivateAll;
    private DccDefaultState mDccDefaultState;
    private DcTracker mDct;
    private volatile boolean mExecutingCarrierChange;
    private Phone mPhone;
    private PhoneStateListener mPhoneStateListener;
    TelephonyManager mTelephonyManager;

    private class DccDefaultState extends State {
        final /* synthetic */ DcController this$0;

        /* renamed from: com.android.internal.telephony.dataconnection.DcController$DccDefaultState$1 */
        class AnonymousClass1 extends Thread {
            final /* synthetic */ DccDefaultState this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcController.DccDefaultState.1.<init>(com.android.internal.telephony.dataconnection.DcController$DccDefaultState):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(com.android.internal.telephony.dataconnection.DcController.DccDefaultState r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcController.DccDefaultState.1.<init>(com.android.internal.telephony.dataconnection.DcController$DccDefaultState):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcController.DccDefaultState.1.<init>(com.android.internal.telephony.dataconnection.DcController$DccDefaultState):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcController.DccDefaultState.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcController.DccDefaultState.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcController.DccDefaultState.1.run():void");
            }
        }

        /* synthetic */ DccDefaultState(DcController this$0, DccDefaultState dccDefaultState) {
            this(this$0);
        }

        private DccDefaultState(DcController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.mPhone.mCi.registerForRilConnected(this.this$0.getHandler(), 262149, null);
            this.this$0.mPhone.mCi.registerForDataNetworkStateChanged(this.this$0.getHandler(), 262151, null);
            if (Build.IS_DEBUGGABLE) {
                this.this$0.mDcTesterDeactivateAll = new DcTesterDeactivateAll(this.this$0.mPhone, this.this$0, this.this$0.getHandler());
            }
        }

        public void exit() {
            if (this.this$0.mPhone != null) {
                this.this$0.mPhone.mCi.unregisterForRilConnected(this.this$0.getHandler());
                this.this$0.mPhone.mCi.unregisterForDataNetworkStateChanged(this.this$0.getHandler());
            }
            if (this.this$0.mDcTesterDeactivateAll != null) {
                this.this$0.mDcTesterDeactivateAll.dispose();
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:34:0x0136  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x00e0  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0102  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case 262149:
                    ar = msg.obj;
                    if (ar.exception != null) {
                        this.this$0.log("DccDefaultState: Unexpected exception on EVENT_RIL_CONNECTED");
                        break;
                    }
                    this.this$0.log("DccDefaultState: msg.what=EVENT_RIL_CONNECTED mRilVersion=" + ar.result);
                    this.this$0.mPhone.mCi.getDataCallList(this.this$0.obtainMessage(262151));
                    break;
                case 262151:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        onDataStateChanged((ArrayList) ar.result);
                    } else {
                        this.this$0.log("DccDefaultState: EVENT_DATA_STATE_CHANGED: exception; likely radio not available, ignore");
                    }
                    if (OemConstant.getWlanAssistantEnable(this.this$0.mPhone.getContext())) {
                        SubscriptionManager s = SubscriptionManager.from(this.this$0.mPhone.getContext());
                        boolean isDefaultDataPhone = this.this$0.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
                        if (isDefaultDataPhone) {
                            boolean myMeasureDataState;
                            DcController dcController;
                            StringBuilder append;
                            StringBuilder append2;
                            boolean haveVsimIgnoreUserDataSetting;
                            boolean isRomming = this.this$0.mPhone.getServiceState().getRoaming();
                            this.this$0.mDct;
                            if (DcTracker.mMeasureDataState) {
                                this.this$0.mDct;
                                if (!(DcTracker.mDelayMeasure || isRomming)) {
                                    myMeasureDataState = !this.this$0.mDct.getDataEnabled() ? this.this$0.mDct.haveVsimIgnoreUserDataSetting() : true;
                                    dcController = this.this$0;
                                    append = new StringBuilder().append("WLAN+ EVENT_DATA_STATE_CHANGED: mMeasureDataState=");
                                    this.this$0.mDct;
                                    append2 = append.append(DcTracker.mMeasureDataState).append(" Roaming=").append(isRomming).append(" DataEnabled=");
                                    if (this.this$0.mDct.getDataEnabled()) {
                                        haveVsimIgnoreUserDataSetting = this.this$0.mDct.haveVsimIgnoreUserDataSetting();
                                    } else {
                                        haveVsimIgnoreUserDataSetting = true;
                                    }
                                    dcController.log(append2.append(haveVsimIgnoreUserDataSetting).append(" isDefaultDataPhone").append(isDefaultDataPhone).toString());
                                    if (myMeasureDataState) {
                                        new AnonymousClass1(this).start();
                                        break;
                                    }
                                }
                            }
                            myMeasureDataState = false;
                            dcController = this.this$0;
                            append = new StringBuilder().append("WLAN+ EVENT_DATA_STATE_CHANGED: mMeasureDataState=");
                            this.this$0.mDct;
                            append2 = append.append(DcTracker.mMeasureDataState).append(" Roaming=").append(isRomming).append(" DataEnabled=");
                            if (this.this$0.mDct.getDataEnabled()) {
                            }
                            dcController.log(append2.append(haveVsimIgnoreUserDataSetting).append(" isDefaultDataPhone").append(isDefaultDataPhone).toString());
                            if (myMeasureDataState) {
                            }
                        }
                    }
                    break;
            }
            return true;
        }

        private void onDataStateChanged(ArrayList<DataCallResponse> dcsList) {
            DataConnection dc;
            this.this$0.lr("onDataStateChanged: dcsList=" + dcsList + " mDcListActiveByCid=" + this.this$0.mDcListActiveByCid);
            if (DcController.VDBG) {
                this.this$0.log("onDataStateChanged: mDcListAll=" + this.this$0.mDcListAll);
            }
            HashMap<Integer, DataCallResponse> dataCallResponseListByCid = new HashMap();
            for (DataCallResponse dcs : dcsList) {
                dataCallResponseListByCid.put(Integer.valueOf(dcs.cid), dcs);
            }
            ArrayList<DataConnection> dcsToRetry = new ArrayList();
            for (DataConnection dc2 : this.this$0.mDcListActiveByCid.values()) {
                if (dataCallResponseListByCid.get(Integer.valueOf(dc2.mCid)) == null) {
                    this.this$0.log("onDataStateChanged: add to retry dc=" + dc2);
                    dcsToRetry.add(dc2);
                }
            }
            this.this$0.log("onDataStateChanged: dcsToRetry=" + dcsToRetry);
            ArrayList<ApnContext> apnsToCleanup = new ArrayList();
            boolean isAnyDataCallDormant = false;
            boolean isAnyDataCallActive = false;
            for (DataCallResponse newState : dcsList) {
                dc2 = (DataConnection) this.this$0.mDcListActiveByCid.get(Integer.valueOf(newState.cid));
                if (dc2 == null) {
                    this.this$0.loge("onDataStateChanged: no associated DC yet, ignore");
                    this.this$0.loge("Deactivate unlinked PDP context.");
                    this.this$0.mDct.deactivatePdpByCid(newState.cid);
                } else {
                    if (dc2.mApnContexts.size() == 0) {
                        this.this$0.loge("onDataStateChanged: no connected apns, ignore");
                    } else {
                        this.this$0.log("onDataStateChanged: Found ConnId=" + newState.cid + " newState=" + newState.toString());
                        if (newState.active != 0) {
                            UpdateLinkPropertyResult result = dc2.updateLinkProperty(newState);
                            if (result.oldLp.equals(result.newLp)) {
                                this.this$0.log("onDataStateChanged: no change");
                            } else if (!result.oldLp.isIdenticalInterfaceName(result.newLp)) {
                                apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                this.this$0.log("onDataStateChanged: interface change, cleanup apns=" + dc2.mApnContexts);
                            } else if (result.oldLp.isIdenticalDnses(result.newLp) && result.oldLp.isIdenticalRoutes(result.newLp) && result.oldLp.isIdenticalHttpProxy(result.newLp) && this.this$0.isIpMatched(result.oldLp, result.newLp)) {
                                this.this$0.log("onDataStateChanged: no changes");
                            } else {
                                CompareResult<LinkAddress> car = result.oldLp.compareAddresses(result.newLp);
                                this.this$0.log("onDataStateChanged: oldLp=" + result.oldLp + " newLp=" + result.newLp + " car=" + car);
                                boolean needToClean = false;
                                for (LinkAddress added : car.added) {
                                    for (LinkAddress removed : car.removed) {
                                        if (NetworkUtils.addressTypeMatches(removed.getAddress(), added.getAddress())) {
                                            needToClean = true;
                                            break;
                                        }
                                    }
                                }
                                if ((DcController.MTK_SVLTE_SUPPORT || DcController.MTK_SRLTE_SUPPORT) && this.this$0.mPhone.getPhoneType() == 2) {
                                    this.this$0.log("onDataStateChanged: IRAT set needToClean false");
                                    needToClean = false;
                                }
                                if (needToClean) {
                                    this.this$0.log("onDataStateChanged: addr change, cleanup apns=" + dc2.mApnContexts + " oldLp=" + result.oldLp + " newLp=" + result.newLp);
                                    apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                                } else {
                                    this.this$0.log("onDataStateChanged: simple change");
                                    for (ApnContext apnContext : dc2.mApnContexts.keySet()) {
                                        this.this$0.mPhone.notifyDataConnection("linkPropertiesChanged", apnContext.getApnType());
                                    }
                                }
                            }
                        } else if (this.this$0.mDct.isCleanupRequired.get()) {
                            apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                            this.this$0.mDct.isCleanupRequired.set(false);
                        } else {
                            DcFailCause failCause = DcFailCause.fromInt(newState.status);
                            if (failCause.isRestartRadioFail()) {
                                this.this$0.log("onDataStateChanged: X restart radio, failCause=" + failCause);
                                this.this$0.mDct.sendRestartRadio();
                            } else if (this.this$0.mDct.isPermanentFail(failCause)) {
                                this.this$0.log("onDataStateChanged: inactive, add to cleanup list. failCause=" + failCause);
                                apnsToCleanup.addAll(dc2.mApnContexts.keySet());
                            } else {
                                this.this$0.log("onDataStateChanged: inactive, add to retry list. failCause=" + failCause);
                                dcsToRetry.add(dc2);
                            }
                        }
                    }
                    if (newState.active == 2) {
                        isAnyDataCallActive = true;
                    }
                    if (newState.active == 1) {
                        isAnyDataCallDormant = true;
                    }
                }
            }
            if (!isAnyDataCallDormant || isAnyDataCallActive) {
                this.this$0.log("onDataStateChanged: Data Activity updated to NONE. isAnyDataCallActive = " + isAnyDataCallActive + " isAnyDataCallDormant = " + isAnyDataCallDormant);
                if (isAnyDataCallActive) {
                    this.this$0.mDct.sendStartNetStatPoll(Activity.NONE);
                }
            } else {
                this.this$0.log("onDataStateChanged: Data Activity updated to DORMANT. stopNetStatePoll");
                this.this$0.mDct.sendStopNetStatPoll(Activity.DORMANT);
            }
            this.this$0.lr("onDataStateChanged: dcsToRetry=" + dcsToRetry + " apnsToCleanup=" + apnsToCleanup);
            for (ApnContext apnContext2 : apnsToCleanup) {
                this.this$0.mDct.sendCleanUpConnection(true, apnContext2);
            }
            for (DataConnection dc22 : dcsToRetry) {
                this.this$0.log("onDataStateChanged: send EVENT_LOST_CONNECTION dc.mTag=" + dc22.mTag);
                dc22.sendMessage(262153, dc22.mTag);
            }
            if (DcController.VDBG) {
                this.this$0.log("onDataStateChanged: X");
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DcController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DcController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcController.<clinit>():void");
    }

    private DcController(String name, Phone phone, DcTracker dct, Handler handler) {
        super(name, handler);
        this.mDcListAll = new ArrayList();
        this.mDcListActiveByCid = new HashMap();
        this.mDccDefaultState = new DccDefaultState(this, null);
        setLogRecSize(300);
        log("E ctor");
        this.mPhone = phone;
        this.mDct = dct;
        addState(this.mDccDefaultState);
        setInitialState(this.mDccDefaultState);
        log("X ctor");
        this.mPhoneStateListener = new PhoneStateListener(handler.getLooper()) {
            public void onCarrierNetworkChange(boolean active) {
                DcController.this.mExecutingCarrierChange = active;
            }
        };
        this.mTelephonyManager = (TelephonyManager) phone.getContext().getSystemService("phone");
        if (this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, GsmVTProviderUtil.UI_MODE_DESTROY);
        }
    }

    public static DcController makeDcc(Phone phone, DcTracker dct, Handler handler) {
        DcController dcc = new DcController("Dcc", phone, dct, handler);
        dcc.start();
        return dcc;
    }

    void dispose() {
        log("dispose: call quiteNow()");
        if (this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        }
        quitNow();
    }

    void addDc(DataConnection dc) {
        this.mDcListAll.add(dc);
    }

    void removeDc(DataConnection dc) {
        this.mDcListActiveByCid.remove(Integer.valueOf(dc.mCid));
        this.mDcListAll.remove(dc);
    }

    public void addActiveDcByCid(DataConnection dc) {
        if (dc.mCid < 0) {
            log("addActiveDcByCid dc.mCid < 0 dc=" + dc);
        }
        this.mDcListActiveByCid.put(Integer.valueOf(dc.mCid), dc);
    }

    public DataConnection getActiveDcByCid(int cid) {
        return (DataConnection) this.mDcListActiveByCid.get(Integer.valueOf(cid));
    }

    void removeActiveDcByCid(DataConnection dc) {
        try {
            if (((DataConnection) this.mDcListActiveByCid.remove(Integer.valueOf(dc.mCid))) == null) {
                log("removeActiveDcByCid removedDc=null dc.mCid=" + dc.mCid);
            }
        } catch (ConcurrentModificationException e) {
            log("concurrentModificationException happened!!");
        }
    }

    boolean isExecutingCarrierChange() {
        return this.mExecutingCarrierChange;
    }

    private void lr(String s) {
        logAndAddLogRec(s);
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected String getWhatToString(int what) {
        String info = DataConnection.cmdToString(what);
        if (info == null) {
            return DcAsyncChannel.cmdToString(what);
        }
        return info;
    }

    public String toString() {
        return "mDcListAll=" + this.mDcListAll + " mDcListActiveByCid=" + this.mDcListActiveByCid;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDcListAll=" + this.mDcListAll);
        pw.println(" mDcListActiveByCid=" + this.mDcListActiveByCid);
    }

    private boolean isIpMatched(LinkProperties oldLp, LinkProperties newLp) {
        if (oldLp.isIdenticalAddresses(newLp)) {
            return true;
        }
        log("isIpMatched: address count is different but matched");
        return newLp.getAddresses().containsAll(oldLp.getAddresses());
    }
}
