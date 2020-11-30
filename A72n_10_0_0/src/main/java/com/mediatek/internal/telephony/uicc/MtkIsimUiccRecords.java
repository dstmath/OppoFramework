package com.mediatek.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.IsimUiccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.uicc.IsimServiceTable;
import java.util.ArrayList;

public class MtkIsimUiccRecords extends IsimUiccRecords implements MtkIsimRecords {
    private static final int EVENT_GET_GBABP_DONE = 200;
    private static final int EVENT_GET_GBANL_DONE = 201;
    private static final int EVENT_GET_PSISMSC_DONE = 202;
    protected static final String LOG_TAG = "MtkIsimUiccRecords";
    ArrayList<byte[]> mEfGbanlList;
    byte[] mEfPsismsc = null;
    private int mIsimChannel;
    private String mIsimGbabp;
    IsimServiceTable mIsimServiceTable;
    private int mSlotId;
    protected UiccController mUiccController;

    static /* synthetic */ int access$308(MtkIsimUiccRecords x0) {
        int i = x0.mRecordsToLoad;
        x0.mRecordsToLoad = i + 1;
        return i;
    }

    static /* synthetic */ int access$508(MtkIsimUiccRecords x0) {
        int i = x0.mRecordsToLoad;
        x0.mRecordsToLoad = i + 1;
        return i;
    }

    public MtkIsimUiccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        log("MtkIsimUiccRecords X ctor this=" + this);
        this.mSlotId = ((MtkUiccCardApplication) app).getPhoneId();
        this.mUiccController = UiccController.getInstance();
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            Rlog.e(LOG_TAG, "Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        loge("IsimUiccRecords: handleMessage " + msg + "[" + msg.what + "] ");
        try {
            switch (msg.what) {
                case EVENT_GET_GBABP_DONE /* 200 */:
                    isRecordLoadResponse = true;
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        loge("Error on GET_ISIM_GBABP with exp " + ar.exception);
                        break;
                    } else {
                        this.mIsimGbabp = IccUtils.bytesToHexString((byte[]) ar.result);
                        break;
                    }
                case 201:
                    isRecordLoadResponse = true;
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception != null) {
                        loge("Error on GET_ISIM_GBANL with exp " + ar2.exception);
                        break;
                    } else {
                        this.mEfGbanlList = (ArrayList) ar2.result;
                        log("GET_ISIM_GBANL record count: " + this.mEfGbanlList.size());
                        break;
                    }
                case 202:
                    isRecordLoadResponse = true;
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    byte[] data = (byte[]) ar3.result;
                    if (ar3.exception == null) {
                        log("EF_PSISMSC: " + IccUtils.bytesToHexString(data));
                        if (data != null) {
                            this.mEfPsismsc = data;
                            break;
                        }
                    } else {
                        break;
                    }
                    break;
                default:
                    MtkIsimUiccRecords.super.handleMessage(msg);
                    break;
            }
            if (!isRecordLoadResponse) {
                return;
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing SIM record", exc);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                onRecordLoaded();
            }
            throw th;
        }
        onRecordLoaded();
    }

    /* access modifiers changed from: protected */
    public void fetchIsimRecords() {
        this.mRecordsRequested = true;
        this.mFh.loadEFTransparent(28418, obtainMessage(100, new IsimUiccRecords.EfIsimImpiLoaded(this)));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixedAll(28420, obtainMessage(100, new IsimUiccRecords.EfIsimImpuLoaded(this)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28419, obtainMessage(100, new IsimUiccRecords.EfIsimDomainLoaded(this)));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent((int) MtkIccConstants.EF_IMSI, obtainMessage(100, new MtkEfIsimIstLoaded()));
        this.mRecordsToLoad++;
        log("fetchIsimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    /* access modifiers changed from: protected */
    public void fetchGbaParam() {
        if (this.mIsimServiceTable.isAvailable(IsimServiceTable.IsimService.GBA)) {
            this.mFh.loadEFTransparent((int) MtkIccConstants.EF_ISIM_GBABP, obtainMessage(EVENT_GET_GBABP_DONE));
            this.mRecordsToLoad++;
            this.mFh.loadEFLinearFixedAll((int) MtkIccConstants.EF_ISIM_GBANL, obtainMessage(201));
            this.mRecordsToLoad++;
        }
    }

    private class MtkEfIsimIstLoaded implements IccRecords.IccRecordLoaded {
        private MtkEfIsimIstLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IST";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            MtkIsimUiccRecords.this.mIsimIst = IccUtils.bytesToHexString(data);
            MtkIsimUiccRecords.this.mIsimServiceTable = new IsimServiceTable(data);
            MtkIsimUiccRecords mtkIsimUiccRecords = MtkIsimUiccRecords.this;
            mtkIsimUiccRecords.log("IST: " + MtkIsimUiccRecords.this.mIsimServiceTable);
            if (MtkIsimUiccRecords.this.mIsimServiceTable.isAvailable(IsimServiceTable.IsimService.PCSCF_ADDRESS) || MtkIsimUiccRecords.this.mIsimServiceTable.isAvailable(IsimServiceTable.IsimService.PCSCF_DISCOVERY)) {
                IccFileHandler iccFileHandler = MtkIsimUiccRecords.this.mFh;
                MtkIsimUiccRecords mtkIsimUiccRecords2 = MtkIsimUiccRecords.this;
                iccFileHandler.loadEFLinearFixedAll(28425, mtkIsimUiccRecords2.obtainMessage(100, new IsimUiccRecords.EfIsimPcscfLoaded(mtkIsimUiccRecords2)));
                MtkIsimUiccRecords.access$308(MtkIsimUiccRecords.this);
            }
            if (MtkIsimUiccRecords.this.mIsimServiceTable.isAvailable(IsimServiceTable.IsimService.SM_OVER_IP)) {
                MtkIsimUiccRecords.this.mFh.loadEFLinearFixed((int) MtkIccConstants.EF_PSISMSC, 1, MtkIsimUiccRecords.this.obtainMessage(202));
                MtkIsimUiccRecords.access$508(MtkIsimUiccRecords.this);
            }
            MtkIsimUiccRecords.this.fetchGbaParam();
        }
    }

    public void registerForRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mRecordsLoadedRegistrants.add(r);
            if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void unregisterForRecordsLoaded(Handler h) {
        this.mRecordsLoadedRegistrants.remove(h);
    }

    @Override // com.mediatek.internal.telephony.uicc.MtkIsimRecords
    public byte[] getEfPsismsc() {
        log("PSISMSC = " + IccUtils.bytesToHexString(this.mEfPsismsc));
        return this.mEfPsismsc;
    }

    @Override // com.mediatek.internal.telephony.uicc.MtkIsimRecords
    public String getIsimGbabp() {
        log("ISIM GBABP = " + this.mIsimGbabp);
        return this.mIsimGbabp;
    }

    @Override // com.mediatek.internal.telephony.uicc.MtkIsimRecords
    public void setIsimGbabp(String gbabp, Message onComplete) {
        this.mFh.updateEFTransparent((int) MtkIccConstants.EF_ISIM_GBABP, IccUtils.hexStringToBytes(gbabp), onComplete);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(LOG_TAG, "[ISIM] " + s + " (slot " + this.mSlotId + ")");
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[ISIM] " + s + " (slot " + this.mSlotId + ")");
    }
}
