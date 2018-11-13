package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccConstants.IccService;
import com.android.internal.telephony.uicc.IccConstants.IccServiceStatus;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

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
public abstract class IccRecords extends Handler implements IccConstants {
    public static final int C2K_PHB_NOT_READY = 2;
    public static final int C2K_PHB_READY = 3;
    public static final int CALL_FORWARDING_STATUS_DISABLED = 0;
    public static final int CALL_FORWARDING_STATUS_ENABLED = 1;
    public static final int CALL_FORWARDING_STATUS_UNKNOWN = -1;
    protected static final boolean DBG = true;
    public static final int EF_RAT_FOR_OTHER_CASE = 512;
    public static final int EF_RAT_NOT_EXIST_IN_USIM = 256;
    public static final int EF_RAT_UNDEFINED = -256;
    protected static final boolean ENGDEBUG = false;
    private static final int EVENT_AKA_AUTHENTICATE_DONE = 90;
    protected static final int EVENT_APP_READY = 1;
    public static final int EVENT_CFI = 1;
    public static final int EVENT_GET_ICC_RECORD_DONE = 100;
    public static final int EVENT_MSISDN = 100;
    public static final int EVENT_MWI = 0;
    public static final int EVENT_OPL = 3;
    protected static final int EVENT_PHB_READY = 410;
    protected static final int EVENT_SET_MSISDN_DONE = 30;
    public static final int EVENT_SPN = 2;
    public static final int GSM_PHB_NOT_READY = 0;
    public static final int GSM_PHB_READY = 1;
    protected static final String[] ICCRECORD_PROPERTY_ICCID = null;
    public static final int SPN_RULE_SHOW_PLMN = 2;
    public static final int SPN_RULE_SHOW_SPN = 1;
    protected static final int UNINITIALIZED = -1;
    protected static final int UNKNOWN = 0;
    protected static final boolean VDBG = false;
    private IccIoResult auth_rsp;
    protected AdnRecordCache mAdnCache;
    protected CommandsInterface mCi;
    protected Context mContext;
    protected AtomicBoolean mDestroyed;
    protected IccFileHandler mFh;
    protected String mFullIccId;
    protected String mGid1;
    protected String mGid2;
    protected String mIccId;
    protected String mImsi;
    protected RegistrantList mImsiReadyRegistrants;
    protected boolean mIsTestCard;
    protected boolean mIsVoiceMailFixed;
    private final Object mLock;
    protected int mMailboxIndex;
    protected int mMncLength;
    protected String mMsisdn;
    protected String mMsisdnTag;
    protected RegistrantList mNetworkSelectionModeAutomaticRegistrants;
    protected String mNewMsisdn;
    protected String mNewMsisdnTag;
    protected RegistrantList mNewSmsRegistrants;
    protected String mNewVoiceMailNum;
    protected String mNewVoiceMailTag;
    protected String mOldMccMnc;
    protected UiccCardApplication mParentApp;
    protected String mPrefLang;
    protected RegistrantList mRecordsEventsRegistrants;
    protected RegistrantList mRecordsLoadedRegistrants;
    protected boolean mRecordsRequested;
    protected int mRecordsToLoad;
    protected String mSpn;
    protected int mSubId;
    protected TelephonyManager mTelephonyManager;
    protected String mVoiceMailNum;
    protected String mVoiceMailTag;

    public interface IccRecordLoaded {
        String getEfName();

        void onRecordLoaded(AsyncResult asyncResult);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.IccRecords.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.IccRecords.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.IccRecords.<clinit>():void");
    }

    protected abstract int getChildPhoneId();

    public abstract int getDisplayRule(String str);

    public abstract int getVoiceMessageCount();

    protected abstract void log(String str);

    protected abstract void loge(String str);

    protected abstract void onAllRecordsLoaded();

    public abstract void onReady();

    protected abstract void onRecordLoaded();

    public abstract void onRefresh(boolean z, int[] iArr);

    public abstract void setVoiceMailNumber(String str, String str2, Message message);

    public abstract void setVoiceMessageWaiting(int i, int i2);

    protected abstract void updatePHBStatus(int i, boolean z);

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("mDestroyed=").append(this.mDestroyed).append(" mContext=").append(this.mContext).append(" mCi=").append(this.mCi).append(" mFh=").append(this.mFh).append(" mParentApp=").append(this.mParentApp).append(" recordsLoadedRegistrants=").append(this.mRecordsLoadedRegistrants).append(" mImsiReadyRegistrants=").append(this.mImsiReadyRegistrants).append(" mRecordsEventsRegistrants=").append(this.mRecordsEventsRegistrants).append(" mNewSmsRegistrants=").append(this.mNewSmsRegistrants).append(" mNetworkSelectionModeAutomaticRegistrants=").append(this.mNetworkSelectionModeAutomaticRegistrants).append(" recordsToLoad=").append(this.mRecordsToLoad).append(" adnCache=").append(this.mAdnCache).append(" recordsRequested=").append(this.mRecordsRequested).append(" iccid=").append(SubscriptionInfo.givePrintableIccid(this.mFullIccId)).append(" msisdnTag=").append(this.mMsisdnTag).append(" voiceMailNum=").append(Rlog.pii(false, this.mVoiceMailNum)).append(" voiceMailTag=").append(this.mVoiceMailTag).append(" voiceMailNum=").append(Rlog.pii(false, this.mNewVoiceMailNum)).append(" newVoiceMailTag=").append(this.mNewVoiceMailTag).append(" isVoiceMailFixed=").append(this.mIsVoiceMailFixed).append(" mImsi=");
        if (this.mImsi != null) {
            str = this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6));
        } else {
            str = "null";
        }
        return append.append(str).append(" mncLength=").append(this.mMncLength).append(" mailboxIndex=").append(this.mMailboxIndex).append(" spn=").append(this.mSpn).toString();
    }

    public IccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        this.mDestroyed = new AtomicBoolean(false);
        this.mRecordsLoadedRegistrants = new RegistrantList();
        this.mImsiReadyRegistrants = new RegistrantList();
        this.mRecordsEventsRegistrants = new RegistrantList();
        this.mNewSmsRegistrants = new RegistrantList();
        this.mNetworkSelectionModeAutomaticRegistrants = new RegistrantList();
        this.mRecordsRequested = false;
        this.mMsisdn = null;
        this.mMsisdnTag = null;
        this.mNewMsisdn = null;
        this.mNewMsisdnTag = null;
        this.mVoiceMailNum = null;
        this.mVoiceMailTag = null;
        this.mNewVoiceMailNum = null;
        this.mNewVoiceMailTag = null;
        this.mIsVoiceMailFixed = false;
        this.mMncLength = -1;
        this.mMailboxIndex = 0;
        this.mLock = new Object();
        this.mSubId = -1;
        this.mOldMccMnc = UsimPBMemInfo.STRING_NOT_SET;
        this.mIsTestCard = false;
        this.mContext = c;
        this.mCi = ci;
        this.mFh = app.getIccFileHandler();
        this.mParentApp = app;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public void dispose() {
        this.mDestroyed.set(true);
        this.mParentApp = null;
        this.mFh = null;
        this.mCi = null;
        this.mContext = null;
    }

    public AdnRecordCache getAdnCache() {
        return this.mAdnCache;
    }

    public String getIccId() {
        return this.mIccId;
    }

    public String getFullIccId() {
        if (this.mFullIccId != null) {
            return this.mFullIccId;
        }
        String fullIccid = null;
        int phoneId = -1;
        if (this.mParentApp != null) {
            phoneId = this.mParentApp.getPhoneId();
        }
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            fullIccid = SystemProperties.get(ICCRECORD_PROPERTY_ICCID[phoneId]);
            if (TextUtils.isEmpty(fullIccid) || "N/A".equals(fullIccid)) {
                fullIccid = null;
            }
        }
        return fullIccid;
    }

    public void registerForRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mRecordsLoadedRegistrants.add(r);
            if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForRecordsLoaded(Handler h) {
        this.mRecordsLoadedRegistrants.remove(h);
    }

    public void registerForImsiReady(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mImsiReadyRegistrants.add(r);
            if (this.mImsi != null) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForImsiReady(Handler h) {
        this.mImsiReadyRegistrants.remove(h);
    }

    public void registerForRecordsEvents(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mRecordsEventsRegistrants.add(r);
        r.notifyResult(Integer.valueOf(0));
        r.notifyResult(Integer.valueOf(1));
    }

    public void unregisterForRecordsEvents(Handler h) {
        this.mRecordsEventsRegistrants.remove(h);
    }

    public void registerForNewSms(Handler h, int what, Object obj) {
        this.mNewSmsRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNewSms(Handler h) {
        this.mNewSmsRegistrants.remove(h);
    }

    public void registerForNetworkSelectionModeAutomatic(Handler h, int what, Object obj) {
        this.mNetworkSelectionModeAutomaticRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkSelectionModeAutomatic(Handler h) {
        this.mNetworkSelectionModeAutomaticRegistrants.remove(h);
    }

    public String getIMSI() {
        return null;
    }

    public boolean isIndiaAirtelPlmn() {
        return false;
    }

    public boolean isTataDocomoPlmn() {
        return false;
    }

    public void setImsi(String imsi) {
        this.mImsi = imsi;
        this.mImsiReadyRegistrants.notifyRegistrants();
    }

    public String getNAI() {
        return null;
    }

    public String getMsisdnNumber() {
        return this.mMsisdn;
    }

    public String getGid1() {
        return null;
    }

    public String getGid2() {
        return null;
    }

    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mMsisdn = number;
        this.mMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mMsisdnTag + " " + this.mMsisdn);
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mMsisdnTag, this.mMsisdn), IccConstants.EF_MSISDN, IccConstants.EF_EXT1, 1, null, obtainMessage(30, onComplete));
    }

    public String getMsisdnAlphaTag() {
        return this.mMsisdnTag;
    }

    public String getVoiceMailNumber() {
        return this.mVoiceMailNum;
    }

    public String getServiceProviderName() {
        String providerName = this.mSpn;
        UiccCardApplication parentApp = this.mParentApp;
        if (parentApp != null) {
            UiccCard card = parentApp.getUiccCard();
            if (card != null) {
                String brandOverride = card.getOperatorBrandOverride();
                if (brandOverride != null) {
                    log("getServiceProviderName: override, providerName=" + providerName);
                    return brandOverride;
                }
                log("getServiceProviderName: no brandOverride, providerName=" + providerName);
                return providerName;
            }
            log("getServiceProviderName: card is null, providerName=" + providerName);
            return providerName;
        }
        log("getServiceProviderName: mParentApp is null, providerName=" + providerName);
        return providerName;
    }

    protected void setServiceProviderName(String spn) {
        this.mSpn = spn;
        if ("502153".equals(getOperatorNumeric())) {
            this.mSpn = "unifi";
        }
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    protected void onIccRefreshInit() {
        this.mAdnCache.reset();
        UiccCardApplication parentApp = this.mParentApp;
        if (parentApp != null && parentApp.getState() == AppState.APPSTATE_READY) {
            sendMessage(obtainMessage(1));
            parentApp.queryFdn();
        }
    }

    public boolean getRecordsLoaded() {
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            return true;
        }
        return false;
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case EVENT_AKA_AUTHENTICATE_DONE /*90*/:
                ar = (AsyncResult) msg.obj;
                this.auth_rsp = null;
                log("EVENT_AKA_AUTHENTICATE_DONE");
                if (ar.exception != null) {
                    loge("Exception ICC SIM AKA: " + ar.exception);
                } else {
                    try {
                        this.auth_rsp = (IccIoResult) ar.result;
                        log("ICC SIM AKA: auth_rsp = " + this.auth_rsp);
                    } catch (Exception e) {
                        loge("Failed to parse ICC SIM AKA contents: " + e);
                    }
                }
                synchronized (this.mLock) {
                    try {
                        this.mLock.notifyAll();
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                return;
            case 100:
                try {
                    ar = msg.obj;
                    IccRecordLoaded recordLoaded = ar.userObj;
                    log(recordLoaded.getEfName() + " LOADED");
                    if (ar.exception != null) {
                        loge("Record Load Exception: " + ar.exception);
                    } else {
                        recordLoaded.onRecordLoaded(ar);
                    }
                    onRecordLoaded();
                    return;
                } catch (RuntimeException exc) {
                    loge("Exception parsing SIM record: " + exc);
                    onRecordLoaded();
                    return;
                } catch (Throwable th2) {
                    onRecordLoaded();
                    throw th2;
                }
            case EVENT_PHB_READY /*410*/:
                ar = (AsyncResult) msg.obj;
                if (ar != null && ar.exception == null && ar.result != null) {
                    int[] isPhbReady = ar.result;
                    String strAllSimState = UsimPBMemInfo.STRING_NOT_SET;
                    String strCurSimState = UsimPBMemInfo.STRING_NOT_SET;
                    int phoneId = getChildPhoneId();
                    strAllSimState = SystemProperties.get("gsm.sim.state");
                    if (strAllSimState != null && strAllSimState.length() > 0) {
                        String[] values = strAllSimState.split(",");
                        if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                            strCurSimState = values[phoneId];
                        }
                    }
                    boolean isSimLocked = !strCurSimState.equals("NETWORK_LOCKED") ? strCurSimState.equals("PIN_REQUIRED") : true;
                    log("isPhbReady=" + isPhbReady[0] + ",strCurSimState = " + strCurSimState + ", isSimLocked = " + isSimLocked);
                    updatePHBStatus(isPhbReady[0], isSimLocked);
                    updateIccFdnStatus();
                    return;
                }
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    public String getSimLanguage() {
        return this.mPrefLang;
    }

    protected void setSimLanguage(byte[] efLi, byte[] efPl) {
        String[] locales = this.mContext.getAssets().getLocales();
        try {
            this.mPrefLang = findBestLanguage(efLi, locales);
        } catch (UnsupportedEncodingException e) {
            log("Unable to parse EF-LI: " + Arrays.toString(efLi));
        }
        if (this.mPrefLang == null) {
            try {
                this.mPrefLang = findBestLanguage(efPl, locales);
            } catch (UnsupportedEncodingException e2) {
                log("Unable to parse EF-PL: " + Arrays.toString(efLi));
            }
        }
    }

    protected static String findBestLanguage(byte[] languages, String[] locales) throws UnsupportedEncodingException {
        if (languages == null || locales == null) {
            return null;
        }
        for (int i = 0; i + 1 < languages.length; i += 2) {
            String lang = new String(languages, i, 2, "ISO-8859-1");
            int j = 0;
            while (j < locales.length) {
                if (locales[j] != null && locales[j].length() >= 2 && locales[j].substring(0, 2).equalsIgnoreCase(lang)) {
                    return lang;
                }
                j++;
            }
        }
        return null;
    }

    public boolean isCspPlmnEnabled() {
        return false;
    }

    public String getOperatorNumeric() {
        return null;
    }

    public int getVoiceCallForwardingFlag() {
        return -1;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
    }

    public boolean isProvisioned() {
        return true;
    }

    public IsimRecords getIsimRecords() {
        return null;
    }

    public UsimServiceTable getUsimServiceTable() {
        return null;
    }

    protected void setSystemProperty(String key, String val) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(this.mParentApp.getPhoneId(), key, val);
        log("[key, value]=" + key + ", " + val);
    }

    public String getIccSimChallengeResponse(int authContext, String data) {
        log("getIccSimChallengeResponse:");
        try {
            synchronized (this.mLock) {
                CommandsInterface ci = this.mCi;
                UiccCardApplication parentApp = this.mParentApp;
                if (ci == null || parentApp == null) {
                    loge("getIccSimChallengeResponse: Fail, ci or parentApp is null");
                    return null;
                }
                ci.requestIccSimAuthentication(authContext, data, parentApp.getAid(), obtainMessage(EVENT_AKA_AUTHENTICATE_DONE));
                try {
                    this.mLock.wait();
                    log("getIccSimChallengeResponse: return auth_rsp");
                    return Base64.encodeToString(this.auth_rsp.payload, 2);
                } catch (InterruptedException e) {
                    loge("getIccSimChallengeResponse: Fail, interrupted while trying to request Icc Sim Auth");
                    return null;
                }
            }
        } catch (Exception e2) {
            loge("getIccSimChallengeResponse: Fail while trying to request Icc Sim Auth");
            return null;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("IccRecords: " + this);
        pw.println(" mDestroyed=" + this.mDestroyed);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mFh=" + this.mFh);
        pw.println(" mParentApp=" + this.mParentApp);
        pw.println(" recordsLoadedRegistrants: size=" + this.mRecordsLoadedRegistrants.size());
        for (i = 0; i < this.mRecordsLoadedRegistrants.size(); i++) {
            pw.println("  recordsLoadedRegistrants[" + i + "]=" + ((Registrant) this.mRecordsLoadedRegistrants.get(i)).getHandler());
        }
        pw.println(" mImsiReadyRegistrants: size=" + this.mImsiReadyRegistrants.size());
        for (i = 0; i < this.mImsiReadyRegistrants.size(); i++) {
            pw.println("  mImsiReadyRegistrants[" + i + "]=" + ((Registrant) this.mImsiReadyRegistrants.get(i)).getHandler());
        }
        pw.println(" mRecordsEventsRegistrants: size=" + this.mRecordsEventsRegistrants.size());
        for (i = 0; i < this.mRecordsEventsRegistrants.size(); i++) {
            pw.println("  mRecordsEventsRegistrants[" + i + "]=" + ((Registrant) this.mRecordsEventsRegistrants.get(i)).getHandler());
        }
        pw.println(" mNewSmsRegistrants: size=" + this.mNewSmsRegistrants.size());
        for (i = 0; i < this.mNewSmsRegistrants.size(); i++) {
            pw.println("  mNewSmsRegistrants[" + i + "]=" + ((Registrant) this.mNewSmsRegistrants.get(i)).getHandler());
        }
        pw.println(" mNetworkSelectionModeAutomaticRegistrants: size=" + this.mNetworkSelectionModeAutomaticRegistrants.size());
        for (i = 0; i < this.mNetworkSelectionModeAutomaticRegistrants.size(); i++) {
            pw.println("  mNetworkSelectionModeAutomaticRegistrants[" + i + "]=" + ((Registrant) this.mNetworkSelectionModeAutomaticRegistrants.get(i)).getHandler());
        }
        pw.println(" mRecordsRequested=" + this.mRecordsRequested);
        pw.println(" mRecordsToLoad=" + this.mRecordsToLoad);
        pw.println(" mRdnCache=" + this.mAdnCache);
        pw.println(" iccid=" + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
        pw.println(" mMsisdn=" + Rlog.pii(false, this.mMsisdn));
        pw.println(" mMsisdnTag=" + this.mMsisdnTag);
        pw.println(" mVoiceMailNum=" + Rlog.pii(false, this.mVoiceMailNum));
        pw.println(" mVoiceMailTag=" + this.mVoiceMailTag);
        pw.println(" mNewVoiceMailNum=" + Rlog.pii(false, this.mNewVoiceMailNum));
        pw.println(" mNewVoiceMailTag=" + this.mNewVoiceMailTag);
        pw.println(" mIsVoiceMailFixed=" + this.mIsVoiceMailFixed);
        pw.println(" mImsi=" + (this.mImsi != null ? this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6)) : "null"));
        pw.println(" mMncLength=" + this.mMncLength);
        pw.println(" mMailboxIndex=" + this.mMailboxIndex);
        pw.println(" mSpn=" + this.mSpn);
        pw.flush();
    }

    public String getMenuTitleFromEf() {
        return null;
    }

    public int getEfRatBalancing() {
        return EF_RAT_UNDEFINED;
    }

    public String getSpNameInEfSpn() {
        return null;
    }

    public String isOperatorMvnoForImsi() {
        return null;
    }

    public String getFirstFullNameInEfPnn() {
        return null;
    }

    public String isOperatorMvnoForEfPnn() {
        return null;
    }

    public String getMvnoMatchType() {
        return null;
    }

    public String getSIMCPHSOns() {
        return null;
    }

    public String getEfGbabp() {
        return null;
    }

    public void setEfGbabp(String gbabp, Message onComplete) {
    }

    public byte[] getEfPsismsc() {
        return null;
    }

    public byte[] getEfSmsp() {
        return null;
    }

    public int getMncLength() {
        return 0;
    }

    public IccServiceStatus getSIMServiceStatus(IccService enService) {
        return IccServiceStatus.NOT_EXIST_IN_USIM;
    }

    public boolean isRadioAvailable() {
        return false;
    }

    public boolean isPhbReady() {
        return false;
    }

    protected void updateIccFdnStatus() {
    }

    public boolean is_test_card() {
        return this.mIsTestCard;
    }

    public boolean isInCnList(String spn) {
        int i = 0;
        if (TextUtils.isEmpty(spn)) {
            return false;
        }
        boolean isCnList = false;
        try {
            String[] plmn_list = this.mContext.getResources().getStringArray(this.mContext.getResources().getIdentifier("oppo_cn_operator_list", "array", "android"));
            int length = plmn_list.length;
            while (i < length) {
                if (spn.equalsIgnoreCase(plmn_list[i])) {
                    isCnList = true;
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            log("len is in cnlist error" + e.getMessage());
        }
        return isCnList;
    }

    public boolean isInCmccList(String spn) {
        int i = 0;
        if (TextUtils.isEmpty(spn)) {
            return false;
        }
        boolean isCnList = false;
        try {
            String[] plmn_list = this.mContext.getResources().getStringArray(this.mContext.getResources().getIdentifier("oppo_cmcc_operator_list", "array", "android"));
            int length = plmn_list.length;
            while (i < length) {
                if (spn.equalsIgnoreCase(plmn_list[i])) {
                    isCnList = true;
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            log("len is in cnlist error" + e.getMessage());
        }
        return isCnList;
    }

    public String oppoGeOperatorByPlmn(Context context, String operatorNumic) {
        if (operatorNumic != null) {
            try {
                return context.getString(context.getResources().getIdentifier("mmcmnc" + operatorNumic, "string", "oppo"));
            } catch (Exception e) {
            }
        }
        return operatorNumic;
    }

    protected void setOemSpnFromConfig(String carrier) {
        if (isInCnList(this.mSpn) || TextUtils.isEmpty(this.mSpn) || (this.mSpn != null && this.mSpn.startsWith(RadioCapabilitySwitchUtil.CN_MCC))) {
            String operator = SubscriptionController.getOemOperator(this.mContext, carrier);
            if (!TextUtils.isEmpty(operator)) {
                setServiceProviderName(operator);
            }
        }
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
    }
}
