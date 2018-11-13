package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import com.mediatek.internal.telephony.uicc.IsimServiceTable;
import com.mediatek.internal.telephony.uicc.IsimServiceTable.IsimService;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class IsimUiccRecords extends IccRecords implements IsimRecords {
    private static final boolean DBG = true;
    private static final boolean DUMP_RECORDS = false;
    private static final int EVENT_AKA_AUTHENTICATE_DONE = 91;
    private static final int EVENT_APP_READY = 1;
    private static final int EVENT_GET_GBABP_DONE = 200;
    private static final int EVENT_GET_GBANL_DONE = 201;
    private static final int EVENT_GET_PSISMSC_DONE = 202;
    private static final int EVENT_ISIM_REFRESH = 31;
    public static final String INTENT_ISIM_REFRESH = "com.android.intent.isim_refresh";
    protected static final String LOG_TAG = "IsimUiccRecords";
    private static final int TAG_ISIM_VALUE = 128;
    private static final boolean VDBG = false;
    private String auth_rsp;
    ArrayList<byte[]> mEfGbanlList;
    byte[] mEfPsismsc;
    private int mIsimChannel;
    private String mIsimDomain;
    private String mIsimGbabp;
    private String[] mIsimGbanl;
    private String mIsimImpi;
    private String[] mIsimImpu;
    private String mIsimIst;
    private String[] mIsimPcscf;
    IsimServiceTable mIsimServiceTable;
    private final Object mLock;
    private int mSlotId;
    protected UiccController mUiccController;

    private class EfIsimDomainLoaded implements IccRecordLoaded {
        /* synthetic */ EfIsimDomainLoaded(IsimUiccRecords this$0, EfIsimDomainLoaded efIsimDomainLoaded) {
            this();
        }

        private EfIsimDomainLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_DOMAIN";
        }

        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords.this.mIsimDomain = IsimUiccRecords.isimTlvToString(ar.result);
        }
    }

    private class EfIsimImpiLoaded implements IccRecordLoaded {
        /* synthetic */ EfIsimImpiLoaded(IsimUiccRecords this$0, EfIsimImpiLoaded efIsimImpiLoaded) {
            this();
        }

        private EfIsimImpiLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IMPI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords.this.mIsimImpi = IsimUiccRecords.isimTlvToString(ar.result);
        }
    }

    private class EfIsimImpuLoaded implements IccRecordLoaded {
        /* synthetic */ EfIsimImpuLoaded(IsimUiccRecords this$0, EfIsimImpuLoaded efIsimImpuLoaded) {
            this();
        }

        private EfIsimImpuLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IMPU";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> impuList = ar.result;
            IsimUiccRecords.this.log("EF_IMPU record count: " + impuList.size());
            IsimUiccRecords.this.mIsimImpu = new String[impuList.size()];
            int i = 0;
            for (byte[] identity : impuList) {
                int i2 = i + 1;
                IsimUiccRecords.this.mIsimImpu[i] = IsimUiccRecords.isimTlvToString(identity);
                i = i2;
            }
        }
    }

    private class EfIsimIstLoaded implements IccRecordLoaded {
        /* synthetic */ EfIsimIstLoaded(IsimUiccRecords this$0, EfIsimIstLoaded efIsimIstLoaded) {
            this();
        }

        private EfIsimIstLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IST";
        }

        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords isimUiccRecords;
            byte[] data = ar.result;
            IsimUiccRecords.this.mIsimIst = IccUtils.bytesToHexString(data);
            IsimUiccRecords.this.mIsimServiceTable = new IsimServiceTable(data);
            IsimUiccRecords.this.log("IST: " + IsimUiccRecords.this.mIsimServiceTable);
            if (IsimUiccRecords.this.mIsimServiceTable.isAvailable(IsimService.PCSCF_ADDRESS) || IsimUiccRecords.this.mIsimServiceTable.isAvailable(IsimService.PCSCF_DISCOVERY)) {
                IsimUiccRecords.this.mFh.loadEFLinearFixedAll(IccConstants.EF_PCSCF, IsimUiccRecords.this.obtainMessage(100, new EfIsimPcscfLoaded(IsimUiccRecords.this, null)));
                isimUiccRecords = IsimUiccRecords.this;
                isimUiccRecords.mRecordsToLoad++;
            }
            if (IsimUiccRecords.this.mIsimServiceTable.isAvailable(IsimService.SM_OVER_IP)) {
                IsimUiccRecords.this.mFh.loadEFLinearFixed(28645, 1, IsimUiccRecords.this.obtainMessage(IsimUiccRecords.EVENT_GET_PSISMSC_DONE));
                isimUiccRecords = IsimUiccRecords.this;
                isimUiccRecords.mRecordsToLoad++;
            }
            IsimUiccRecords.this.fetchGbaParam();
        }
    }

    private class EfIsimPcscfLoaded implements IccRecordLoaded {
        /* synthetic */ EfIsimPcscfLoaded(IsimUiccRecords this$0, EfIsimPcscfLoaded efIsimPcscfLoaded) {
            this();
        }

        private EfIsimPcscfLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_PCSCF";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> pcscflist = ar.result;
            IsimUiccRecords.this.log("EF_PCSCF record count: " + pcscflist.size());
            IsimUiccRecords.this.mIsimPcscf = new String[pcscflist.size()];
            int i = 0;
            for (byte[] identity : pcscflist) {
                int i2 = i + 1;
                IsimUiccRecords.this.mIsimPcscf[i] = IsimUiccRecords.isimTlvToString(identity);
                i = i2;
            }
        }
    }

    public String toString() {
        return "IsimUiccRecords: " + super.toString() + UsimPBMemInfo.STRING_NOT_SET;
    }

    public IsimUiccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mEfPsismsc = null;
        this.mLock = new Object();
        this.mRecordsRequested = false;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mCi.registerForIccRefresh(this, 31, null);
        this.mParentApp.registerForReady(this, 1, null);
        log("IsimUiccRecords X ctor this=" + this);
        this.mSlotId = app.getSlotId();
        this.mUiccController = UiccController.getInstance();
    }

    public void dispose() {
        log("Disposing " + this);
        this.mCi.unregisterForIccRefresh(this);
        this.mParentApp.unregisterForReady(this);
        resetRecords();
        super.dispose();
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            Rlog.e(LOG_TAG, "Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        loge("IsimUiccRecords: handleMessage " + msg + "[" + msg.what + "] ");
        try {
            AsyncResult ar;
            switch (msg.what) {
                case 1:
                    onReady();
                    break;
                case 31:
                    ar = msg.obj;
                    loge("ISim REFRESH(EVENT_ISIM_REFRESH) with exception: " + ar.exception);
                    if (ar.exception == null) {
                        Intent intent = new Intent(INTENT_ISIM_REFRESH);
                        loge("send ISim REFRESH: com.android.intent.isim_refresh");
                        this.mContext.sendBroadcast(intent);
                        handleIsimRefresh((IccRefreshResponse) ar.result);
                        break;
                    }
                    break;
                case 91:
                    ar = (AsyncResult) msg.obj;
                    log("EVENT_AKA_AUTHENTICATE_DONE");
                    if (ar.exception != null) {
                        log("Exception ISIM AKA: " + ar.exception);
                    } else {
                        try {
                            this.auth_rsp = (String) ar.result;
                            log("ISIM AKA: auth_rsp = " + this.auth_rsp);
                        } catch (Exception e) {
                            log("Failed to parse ISIM AKA contents: " + e);
                        }
                    }
                    synchronized (this.mLock) {
                        this.mLock.notifyAll();
                    }
                case 200:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        loge("Error on GET_ISIM_GBABP with exp " + ar.exception);
                        break;
                    } else {
                        this.mIsimGbabp = IccUtils.bytesToHexString(ar.result);
                        break;
                    }
                case 201:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        loge("Error on GET_ISIM_GBANL with exp " + ar.exception);
                        break;
                    }
                    this.mEfGbanlList = (ArrayList) ar.result;
                    log("GET_ISIM_GBANL record count: " + this.mEfGbanlList.size());
                    break;
                case EVENT_GET_PSISMSC_DONE /*202*/:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    byte[] data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_PSISMSC: " + IccUtils.bytesToHexString(data));
                        if (data != null) {
                            this.mEfPsismsc = data;
                            break;
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing SIM record", exc);
            if (null != null) {
                onRecordLoaded();
            }
        } catch (Throwable th) {
            if (null != null) {
                onRecordLoaded();
            }
        }
    }

    protected void fetchIsimRecords() {
        this.mRecordsRequested = true;
        this.mFh.loadEFTransparent(IccConstants.EF_IMPI, obtainMessage(100, new EfIsimImpiLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_IMPU, obtainMessage(100, new EfIsimImpuLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_DOMAIN, obtainMessage(100, new EfIsimDomainLoaded(this, null)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28423, obtainMessage(100, new EfIsimIstLoaded(this, null)));
        this.mRecordsToLoad++;
        log("fetchIsimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    private void fetchGbaParam() {
        if (this.mIsimServiceTable.isAvailable(IsimService.GBA)) {
            this.mFh.loadEFTransparent(IccConstants.EF_ISIM_GBABP, obtainMessage(200));
            this.mRecordsToLoad++;
            this.mFh.loadEFLinearFixedAll(IccConstants.EF_ISIM_GBANL, obtainMessage(201));
            this.mRecordsToLoad++;
        }
    }

    protected void resetRecords() {
        this.mIsimImpi = null;
        this.mIsimDomain = null;
        this.mIsimImpu = null;
        this.mIsimIst = null;
        this.mIsimPcscf = null;
        this.auth_rsp = null;
        this.mRecordsRequested = false;
    }

    private static String isimTlvToString(byte[] record) {
        SimTlv tlv = new SimTlv(record, 0, record.length);
        while (tlv.getTag() != 128) {
            if (!tlv.nextObject()) {
                return null;
            }
        }
        return new String(tlv.getData(), Charset.forName("UTF-8"));
    }

    protected void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            onAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    protected void onAllRecordsLoaded() {
        log("record load complete");
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
    }

    private void handleFileUpdate(int efid) {
        switch (efid) {
            case IccConstants.EF_IMPI /*28418*/:
                this.mFh.loadEFTransparent(IccConstants.EF_IMPI, obtainMessage(100, new EfIsimImpiLoaded(this, null)));
                this.mRecordsToLoad++;
                return;
            case IccConstants.EF_DOMAIN /*28419*/:
                this.mFh.loadEFTransparent(IccConstants.EF_DOMAIN, obtainMessage(100, new EfIsimDomainLoaded(this, null)));
                this.mRecordsToLoad++;
                return;
            case IccConstants.EF_IMPU /*28420*/:
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_IMPU, obtainMessage(100, new EfIsimImpuLoaded(this, null)));
                this.mRecordsToLoad++;
                return;
            case 28423:
                this.mFh.loadEFTransparent(28423, obtainMessage(100, new EfIsimIstLoaded(this, null)));
                this.mRecordsToLoad++;
                return;
            case IccConstants.EF_PCSCF /*28425*/:
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_PCSCF, obtainMessage(100, new EfIsimPcscfLoaded(this, null)));
                this.mRecordsToLoad++;
                break;
        }
        fetchIsimRecords();
    }

    private void handleIsimRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null) {
            log("handleIsimRefresh received without input");
        } else if (refreshResponse.aid == null || refreshResponse.aid.equals(this.mParentApp.getAid())) {
            switch (refreshResponse.refreshResult) {
                case 0:
                    log("handleIsimRefresh with REFRESH_RESULT_FILE_UPDATE");
                    handleFileUpdate(refreshResponse.efId);
                    break;
                case 1:
                    log("handleIsimRefresh with REFRESH_RESULT_INIT");
                    fetchIsimRecords();
                    break;
                case 2:
                    log("handleIsimRefresh with REFRESH_RESULT_RESET");
                    break;
                default:
                    log("handleIsimRefresh with unknown operation");
                    break;
            }
        } else {
            log("handleIsimRefresh received different app");
        }
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

    public String getIsimImpi() {
        return this.mIsimImpi;
    }

    public String getIsimDomain() {
        return this.mIsimDomain;
    }

    public String[] getIsimImpu() {
        return this.mIsimImpu != null ? (String[]) this.mIsimImpu.clone() : null;
    }

    public String getIsimIst() {
        return this.mIsimIst;
    }

    public String[] getIsimPcscf() {
        return this.mIsimPcscf != null ? (String[]) this.mIsimPcscf.clone() : null;
    }

    public String getIsimChallengeResponse(String nonce) {
        log("getIsimChallengeResponse-nonce:" + nonce);
        try {
            synchronized (this.mLock) {
                this.mCi.requestIsimAuthentication(nonce, obtainMessage(91));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to request Isim Auth");
                }
            }
            log("getIsimChallengeResponse-auth_rsp" + this.auth_rsp);
            return this.auth_rsp;
        } catch (Exception e2) {
            log("Fail while trying to request Isim Auth");
            return null;
        }
    }

    public byte[] getEfPsismsc() {
        log("PSISMSC = " + this.mEfPsismsc);
        return this.mEfPsismsc;
    }

    public String getIsimGbabp() {
        log("ISIM GBABP = " + this.mIsimGbabp);
        return this.mIsimGbabp;
    }

    public void setIsimGbabp(String gbabp, Message onComplete) {
        this.mFh.updateEFTransparent(IccConstants.EF_ISIM_GBABP, IccUtils.hexStringToBytes(gbabp), onComplete);
    }

    public int getDisplayRule(String plmn) {
        return 0;
    }

    public void onReady() {
        fetchIsimRecords();
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchIsimRecords();
        }
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, "[ISIM] " + s + " (slot " + this.mSlotId + ")");
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "[ISIM] " + s + " (slot " + this.mSlotId + ")");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("IsimRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.flush();
    }

    public int getVoiceMessageCount() {
        return 0;
    }

    protected int getChildPhoneId() {
        log("[getChildPhoneId] return -1 for iSimUiccRecords");
        return -1;
    }

    protected void updatePHBStatus(int status, boolean isSimLocked) {
    }
}
