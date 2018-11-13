package com.android.internal.telephony.cdma;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.provider.Settings.Global;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CdmaSubscriptionSourceManager extends Handler {
    private static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 1;
    private static final int EVENT_GET_CDMA_SUBSCRIPTION_SOURCE = 2;
    private static final int EVENT_RADIO_ON = 3;
    private static final int EVENT_SUBSCRIPTION_STATUS_CHANGED = 4;
    static final String LOG_TAG = "CdmaSSM";
    public static final int PREFERRED_CDMA_SUBSCRIPTION = 0;
    private static final int SUBSCRIPTION_ACTIVATED = 1;
    public static final int SUBSCRIPTION_FROM_NV = 1;
    public static final int SUBSCRIPTION_FROM_RUIM = 0;
    public static final int SUBSCRIPTION_SOURCE_UNKNOWN = -1;
    private static final HashMap<CommandsInterface, CdmaSubscriptionSourceManager> sCiInstances = null;
    private static final HashMap<CommandsInterface, Integer> sCiReferenceCounts = null;
    private static final HashMap<Handler, CommandsInterface> sHandlerCis = null;
    private static CdmaSubscriptionSourceManager sInstance;
    private static final Object sReferenceCountMonitor = null;
    private int mActStatus;
    private AtomicInteger mCdmaSubscriptionSource;
    private RegistrantList mCdmaSubscriptionSourceChangedRegistrants;
    private CommandsInterface mCi;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager.<clinit>():void");
    }

    private CdmaSubscriptionSourceManager(Context context, CommandsInterface ci) {
        this.mCdmaSubscriptionSourceChangedRegistrants = new RegistrantList();
        this.mCdmaSubscriptionSource = new AtomicInteger(1);
        this.mActStatus = 0;
        this.mCi = ci;
        this.mCi.registerForCdmaSubscriptionChanged(this, 1, null);
        this.mCi.registerForOn(this, 3, null);
        int subscriptionSource = getDefault(context);
        log("cdmaSSM constructor: " + subscriptionSource);
        this.mCdmaSubscriptionSource.set(subscriptionSource);
        this.mCi.registerForSubscriptionStatusChanged(this, 4, null);
    }

    public static CdmaSubscriptionSourceManager getInstance(Context context, CommandsInterface ci, Handler h, int what, Object obj) {
        synchronized (sReferenceCountMonitor) {
            int referenceCount;
            sInstance = (CdmaSubscriptionSourceManager) sCiInstances.get(ci);
            if (sInstance == null) {
                sInstance = new CdmaSubscriptionSourceManager(context, ci);
                sCiInstances.put(ci, sInstance);
            }
            sHandlerCis.put(h, ci);
            if (sCiReferenceCounts.get(ci) == null) {
                referenceCount = 0;
            } else {
                referenceCount = ((Integer) sCiReferenceCounts.get(ci)).intValue();
            }
            sCiReferenceCounts.put(ci, Integer.valueOf(referenceCount + 1));
        }
        sInstance.registerForCdmaSubscriptionSourceChanged(h, what, obj);
        return sInstance;
    }

    /* JADX WARNING: Missing block: B:16:0x007c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dispose(Handler h) {
        this.mCdmaSubscriptionSourceChangedRegistrants.remove(h);
        synchronized (sReferenceCountMonitor) {
            CommandsInterface mCi = (CommandsInterface) sHandlerCis.get(h);
            if (mCi == null) {
                log("The handler doesn't create CdmaSSM, return !");
                return;
            }
            int referenceCount = ((Integer) sCiReferenceCounts.get(mCi)).intValue() - 1;
            sCiReferenceCounts.put(mCi, Integer.valueOf(referenceCount));
            sHandlerCis.remove(h);
            log("dispose ci = " + (mCi != null ? Integer.valueOf(mCi.hashCode()) : null) + "  referenceCount = " + referenceCount);
            if (referenceCount <= 0) {
                mCi.unregisterForCdmaSubscriptionChanged(this);
                mCi.unregisterForOn(this);
                mCi.unregisterForSubscriptionStatusChanged(this);
                this.mActStatus = 0;
                sCiInstances.remove(mCi);
                sCiReferenceCounts.remove(mCi);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
            case 2:
                log("CDMA_SUBSCRIPTION_SOURCE event = " + msg.what);
                handleGetCdmaSubscriptionSource(msg.obj);
                return;
            case 3:
                this.mCi.getCdmaSubscriptionSource(obtainMessage(2));
                return;
            case 4:
                log("EVENT_SUBSCRIPTION_STATUS_CHANGED");
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int actStatus = ((int[]) ar.result)[0];
                    log("actStatus = " + actStatus);
                    this.mActStatus = actStatus;
                    if (actStatus == 1) {
                        Rlog.v(LOG_TAG, "get Cdma Subscription Source");
                        this.mCi.getCdmaSubscriptionSource(obtainMessage(2));
                        return;
                    }
                    return;
                }
                logw("EVENT_SUBSCRIPTION_STATUS_CHANGED, Exception:" + ar.exception);
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    public int getCdmaSubscriptionSource() {
        log("getcdmasubscriptionSource: " + this.mCdmaSubscriptionSource.get());
        return this.mCdmaSubscriptionSource.get();
    }

    public static int getDefault(Context context) {
        int subscriptionSource = Global.getInt(context.getContentResolver(), "subscription_mode", 0);
        Rlog.d(LOG_TAG, "subscriptionSource from settings: " + subscriptionSource);
        return subscriptionSource;
    }

    private void registerForCdmaSubscriptionSourceChanged(Handler h, int what, Object obj) {
        this.mCdmaSubscriptionSourceChangedRegistrants.add(new Registrant(h, what, obj));
    }

    private void handleGetCdmaSubscriptionSource(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            logw("Unable to get CDMA Subscription Source, Exception: " + ar.exception + ", result: " + ar.result);
            return;
        }
        int newSubscriptionSource = ((int[]) ar.result)[0];
        if (newSubscriptionSource != this.mCdmaSubscriptionSource.get()) {
            log("Subscription Source Changed : " + this.mCdmaSubscriptionSource + " >> " + newSubscriptionSource);
            this.mCdmaSubscriptionSource.set(newSubscriptionSource);
            this.mCdmaSubscriptionSourceChangedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void logw(String s) {
        Rlog.w(LOG_TAG, s);
    }

    public int getActStatus() {
        log("getActStatus " + this.mActStatus);
        return this.mActStatus;
    }
}
