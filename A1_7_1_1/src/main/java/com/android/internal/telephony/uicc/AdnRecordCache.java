package com.android.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.mediatek.internal.telephony.gsm.GsmVTProvider;
import com.mediatek.internal.telephony.uicc.AlphaTag;
import com.mediatek.internal.telephony.uicc.CsimPhbStorageInfo;
import com.mediatek.internal.telephony.uicc.UsimGroup;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class AdnRecordCache extends Handler implements IccConstants {
    private static final int ADN_FILE_SIZE = 250;
    protected static final boolean DBG = false;
    static final int EVENT_LOAD_ALL_ADN_LIKE_DONE = 1;
    static final int EVENT_UPDATE_ADN_DONE = 2;
    static final String LOG_TAG = "AdnRecordCache";
    public static final int MAX_PHB_NAME_LENGTH = 60;
    public static final int MAX_PHB_NUMBER_ANR_COUNT = 3;
    public static final int MAX_PHB_NUMBER_ANR_LENGTH = 20;
    public static final int MAX_PHB_NUMBER_LENGTH = 40;
    private boolean isUim;
    SparseArray<ArrayList<AdnRecord>> mAdnLikeFiles;
    SparseArray<ArrayList<Message>> mAdnLikeWaiters;
    private CommandsInterface mCi;
    private UiccCardApplication mCurrentApp;
    private IccFileHandler mFh;
    private final Object mLock;
    private boolean mLocked;
    private int mSlotId;
    private boolean mSuccess;
    SparseArray<Message> mUserWriteResponse;
    private UsimPhoneBookManager mUsimPhoneBookManager;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.AdnRecordCache.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.AdnRecordCache.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.AdnRecordCache.<clinit>():void");
    }

    AdnRecordCache(IccFileHandler fh) {
        this.mSlotId = -1;
        this.mAdnLikeFiles = new SparseArray();
        this.mAdnLikeWaiters = new SparseArray();
        this.mUserWriteResponse = new SparseArray();
        this.mLock = new Object();
        this.mSuccess = false;
        this.mLocked = false;
        this.mFh = fh;
        this.mCi = null;
        this.mCurrentApp = null;
        this.mUsimPhoneBookManager = new UsimPhoneBookManager(this.mFh, this);
    }

    AdnRecordCache(IccFileHandler fh, CommandsInterface ci, UiccCardApplication app) {
        this.mSlotId = -1;
        this.mAdnLikeFiles = new SparseArray();
        this.mAdnLikeWaiters = new SparseArray();
        this.mUserWriteResponse = new SparseArray();
        this.mLock = new Object();
        this.mSuccess = false;
        this.mLocked = false;
        this.mFh = fh;
        this.mCi = ci;
        this.mCurrentApp = app;
        this.mUsimPhoneBookManager = new UsimPhoneBookManager(this.mFh, this, ci, app);
        if (app != null) {
            this.mSlotId = app.getSlotId();
        }
    }

    public void reset() {
        logi("reset");
        this.mAdnLikeFiles.clear();
        this.mUsimPhoneBookManager.reset();
        clearWaiters();
        clearUserWriters();
        if (!CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
            CsimPhbStorageInfo.clearAdnRecordSize();
        }
    }

    public int getSlotId() {
        return this.mSlotId;
    }

    private void clearWaiters() {
        int size = this.mAdnLikeWaiters.size();
        for (int i = 0; i < size; i++) {
            notifyWaiters((ArrayList) this.mAdnLikeWaiters.valueAt(i), new AsyncResult(null, null, new RuntimeException("AdnCache reset")));
        }
        this.mAdnLikeWaiters.clear();
    }

    private void clearUserWriters() {
        logi("clearUserWriters,mLocked " + this.mLocked);
        if (this.mLocked) {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
            this.mLocked = false;
        }
        int size = this.mUserWriteResponse.size();
        for (int i = 0; i < size; i++) {
            sendErrorResponse((Message) this.mUserWriteResponse.valueAt(i), "AdnCace reset " + this.mUserWriteResponse.valueAt(i));
        }
        this.mUserWriteResponse.clear();
    }

    public ArrayList<AdnRecord> getRecordsIfLoaded(int efid) {
        return (ArrayList) this.mAdnLikeFiles.get(efid);
    }

    public int extensionEfForEf(int efid) {
        switch (efid) {
            case IccConstants.EF_PBR /*20272*/:
                return 0;
            case 28474:
                return IccConstants.EF_EXT1;
            case IccConstants.EF_FDN /*28475*/:
                return IccConstants.EF_EXT2;
            case IccConstants.EF_MSISDN /*28480*/:
                return IccConstants.EF_EXT1;
            case IccConstants.EF_SDN /*28489*/:
                return IccConstants.EF_EXT3;
            case IccConstants.EF_MBDN /*28615*/:
                return IccConstants.EF_EXT6;
            default:
                return -1;
        }
    }

    private void sendErrorResponse(Message response, String errString) {
        sendErrorResponse(response, errString, 2);
    }

    private void sendErrorResponse(Message response, String errString, int ril_errno) {
        CommandException e = CommandException.fromRilErrno(ril_errno);
        if (response != null) {
            logw(errString);
            AsyncResult.forMessage(response).exception = e;
            response.sendToTarget();
        }
    }

    /* JADX WARNING: Missing block: B:138:0x0306, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void updateAdnByIndex(int efid, AdnRecord adn, int recordIndex, String pin2, Message response) {
        logd("updateAdnByIndex efid:" + efid + ", pin2:" + pin2 + ", recordIndex:" + recordIndex + ", adn [" + adn + "]");
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
        } else if (adn.mAlphaTag.length() > 60) {
            sendErrorResponse(response, "the input length of mAlphaTag is too long: " + adn.mAlphaTag, 1002);
        } else {
            int num_length = adn.mNumber.length();
            if (adn.mNumber.indexOf(43) != -1) {
                num_length--;
            }
            if (num_length > 40) {
                sendErrorResponse(response, "the input length of phoneNumber is too long: " + adn.mNumber, 1001);
                return;
            }
            int i;
            String anr;
            for (i = 0; i < 3; i++) {
                anr = adn.getAdditionalNumber(i);
                if (anr != null) {
                    num_length = anr.length();
                    if (anr.indexOf(43) != -1) {
                        num_length--;
                    }
                    if (num_length > 20) {
                        sendErrorResponse(response, "the input length of additional number is too long: " + anr, GsmVTProvider.SESSION_EVENT_START_COUNTER);
                        return;
                    }
                }
            }
            if (this.mUsimPhoneBookManager.checkEmailLength(adn.mEmails)) {
                AdnRecord foundAdn = null;
                if (efid == 20272) {
                    ArrayList<AdnRecord> oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
                    if (oldAdnList == null) {
                        sendErrorResponse(response, "Adn list not exist for EF:" + efid, 1011);
                        return;
                    }
                    foundAdn = (AdnRecord) oldAdnList.get(recordIndex - 1);
                    efid = foundAdn.mEfid;
                    extensionEF = foundAdn.mExtRecord;
                    adn.mEfid = efid;
                }
                if (this.mUsimPhoneBookManager.checkEmailCapacityFree(recordIndex, adn.mEmails, foundAdn)) {
                    i = 0;
                    while (i < 3) {
                        anr = adn.getAdditionalNumber(i);
                        if (this.mUsimPhoneBookManager.isAnrCapacityFree(anr, recordIndex, i, foundAdn)) {
                            i++;
                        } else {
                            sendErrorResponse(response, "drop the additional number for the update fail: " + anr, 1012);
                            return;
                        }
                    }
                    if (!this.mUsimPhoneBookManager.checkSneCapacityFree(recordIndex, adn.sne, foundAdn)) {
                        sendErrorResponse(response, "drop the sne for the limitation of the SIM card", 1007);
                        return;
                    } else if (((Message) this.mUserWriteResponse.get(efid)) != null) {
                        sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid).toUpperCase());
                        return;
                    } else {
                        this.mUserWriteResponse.put(efid, response);
                        if ((efid == 28474 || efid == 20272 || efid == 20282 || efid == 20283 || efid == 20284 || efid == 20285) && adn.mAlphaTag.length() == 0 && adn.mNumber.length() == 0) {
                            this.mUsimPhoneBookManager.removeContactGroup(recordIndex);
                        }
                        if (this.mUserWriteResponse.size() != 0) {
                            synchronized (this.mLock) {
                                this.mSuccess = false;
                                this.mLocked = true;
                                new AdnRecordLoader(this.mFh).updateEF(adn, efid, extensionEF, recordIndex, pin2, obtainMessage(2, efid, recordIndex, adn));
                                try {
                                    this.mLock.wait();
                                } catch (InterruptedException e) {
                                    return;
                                }
                            }
                            if (!this.mSuccess) {
                                return;
                            }
                            if (efid == 28474 || efid == 20272 || efid == 20282 || efid == 20283 || efid == 20284 || efid == 20285) {
                                try {
                                    int mResult = this.mUsimPhoneBookManager.updateSneByAdnIndex(adn.sne, recordIndex);
                                    if (-30 == mResult) {
                                        sendErrorResponse(response, "drop the SNE for the limitation of the SIM card", 1007);
                                    } else if (-40 == mResult) {
                                        sendErrorResponse(response, "the sne string is too long", 1008);
                                    } else {
                                        for (i = 0; i < 3; i++) {
                                            this.mUsimPhoneBookManager.updateAnrByAdnIndex(adn.getAdditionalNumber(i), recordIndex, i);
                                        }
                                        int success = this.mUsimPhoneBookManager.updateEmailsByAdnIndex(adn.mEmails, recordIndex);
                                        if (-30 == success) {
                                            sendErrorResponse(response, "drop the email for the limitation of the SIM card", 1005);
                                        } else if (-40 == success) {
                                            sendErrorResponse(response, "the email string is too long", 1006);
                                        } else if (-50 == success) {
                                            sendErrorResponse(response, "Unkown error occurs when update email", 2);
                                        } else {
                                            AsyncResult.forMessage(response, null, null);
                                            response.sendToTarget();
                                        }
                                    }
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                    return;
                                }
                            } else if (efid == 28475) {
                                AsyncResult.forMessage(response, null, null);
                                response.sendToTarget();
                            }
                        } else {
                            return;
                        }
                    }
                }
                sendErrorResponse(response, "drop the email for the limitation of the SIM card", 1005);
                return;
            }
            sendErrorResponse(response, "the email string is too long", 1006);
        }
    }

    /* JADX WARNING: Missing block: B:73:0x01ec, code:
            return r7;
     */
    /* JADX WARNING: Missing block: B:164:0x03d2, code:
            return r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int updateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        logd("updateAdnBySearch efid:" + efid + ", pin2:" + pin2 + ", oldAdn [" + oldAdn + "], new Adn[" + newAdn + "]");
        int index = -1;
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
            return -1;
        } else if (newAdn.mAlphaTag.length() > 60) {
            sendErrorResponse(response, "the input length of mAlphaTag is too long: " + newAdn.mAlphaTag, 1002);
            return -1;
        } else {
            int num_length = newAdn.mNumber.length();
            if (newAdn.mNumber.indexOf(43) != -1) {
                num_length--;
            }
            if (num_length > 40) {
                sendErrorResponse(response, "the input length of phoneNumber is too long: " + newAdn.mNumber, 1001);
                return -1;
            }
            int i;
            String anr;
            for (i = 0; i < 3; i++) {
                anr = newAdn.getAdditionalNumber(i);
                if (anr != null) {
                    num_length = anr.length();
                    if (anr.indexOf(43) != -1) {
                        num_length--;
                    }
                    if (num_length > 20) {
                        sendErrorResponse(response, "the input length of additional number is too long: " + anr, GsmVTProvider.SESSION_EVENT_START_COUNTER);
                        return -1;
                    }
                }
            }
            if (this.mUsimPhoneBookManager.checkEmailLength(newAdn.mEmails)) {
                ArrayList<AdnRecord> oldAdnList;
                if (efid == 20272) {
                    oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
                } else {
                    oldAdnList = getRecordsIfLoaded(efid);
                }
                if (oldAdnList == null) {
                    sendErrorResponse(response, "Adn list not exist for EF:" + efid, 1011);
                    return -1;
                }
                int count = 1;
                Iterator<AdnRecord> it = oldAdnList.iterator();
                while (it.hasNext()) {
                    if (oldAdn.isEqual((AdnRecord) it.next())) {
                        index = count;
                        break;
                    }
                    count++;
                }
                logi("updateAdnBySearch index " + index);
                if (index != -1) {
                    AdnRecord foundAdn = null;
                    if (efid == 20272) {
                        foundAdn = (AdnRecord) oldAdnList.get(index - 1);
                        efid = foundAdn.mEfid;
                        extensionEF = foundAdn.mExtRecord;
                        index = foundAdn.mRecordNumber;
                        newAdn.mEfid = efid;
                        newAdn.mExtRecord = extensionEF;
                        newAdn.mRecordNumber = index;
                    }
                    if (((Message) this.mUserWriteResponse.get(efid)) != null) {
                        sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid).toUpperCase());
                        return index;
                    } else if (efid == 0) {
                        sendErrorResponse(response, "Abnormal efid: " + efid);
                        return index;
                    } else if (this.mUsimPhoneBookManager.checkEmailCapacityFree(index, newAdn.mEmails, foundAdn)) {
                        i = 0;
                        while (i < 3) {
                            anr = newAdn.getAdditionalNumber(i);
                            if (this.mUsimPhoneBookManager.isAnrCapacityFree(anr, index, i, foundAdn)) {
                                i++;
                            } else {
                                sendErrorResponse(response, "drop the additional number for the write fail: " + anr, 1012);
                                return index;
                            }
                        }
                        if (this.mUsimPhoneBookManager.checkSneCapacityFree(index, newAdn.sne, foundAdn)) {
                            this.mUserWriteResponse.put(efid, response);
                            synchronized (this.mLock) {
                                this.mSuccess = false;
                                this.mLocked = true;
                                new AdnRecordLoader(this.mFh).updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
                                try {
                                    this.mLock.wait();
                                } catch (InterruptedException e) {
                                    return index;
                                }
                            }
                            if (this.mSuccess) {
                                int success = 0;
                                if (efid == 28474 || efid == 20272 || efid == 20282 || efid == 20283 || efid == 20284 || efid == 20285) {
                                    int mResult = this.mUsimPhoneBookManager.updateSneByAdnIndex(newAdn.sne, index);
                                    if (-30 == mResult) {
                                        sendErrorResponse(response, "drop the SNE for the limitation of the SIM card", 1007);
                                    } else if (-40 == mResult) {
                                        sendErrorResponse(response, "the sne string is too long", 1008);
                                    }
                                    for (i = 0; i < 3; i++) {
                                        this.mUsimPhoneBookManager.updateAnrByAdnIndex(newAdn.getAdditionalNumber(i), index, i);
                                    }
                                    success = this.mUsimPhoneBookManager.updateEmailsByAdnIndex(newAdn.mEmails, index);
                                }
                                if (-30 == success) {
                                    sendErrorResponse(response, "drop the email for the limitation of the SIM card", 1005);
                                } else if (-40 == success) {
                                    sendErrorResponse(response, "the email string is too long", 1006);
                                } else if (-50 == success) {
                                    sendErrorResponse(response, "Unkown error occurs when update email", 2);
                                } else {
                                    logd("updateAdnBySearch response:" + response);
                                    AsyncResult.forMessage(response, null, null);
                                    response.sendToTarget();
                                }
                            } else {
                                loge("updateAdnBySearch mSuccess:" + this.mSuccess);
                                return index;
                            }
                        }
                        sendErrorResponse(response, "drop the sne for the limitation of the SIM card", 1007);
                        return index;
                    } else {
                        sendErrorResponse(response, "drop the email for the limitation of the SIM card", 1005);
                        return index;
                    }
                } else if (oldAdn.mAlphaTag.length() == 0 && oldAdn.mNumber.length() == 0) {
                    sendErrorResponse(response, "Adn record don't exist for " + oldAdn, 1003);
                } else {
                    sendErrorResponse(response, "Adn record don't exist for " + oldAdn);
                }
            } else {
                sendErrorResponse(response, "the email string is too long", 1006);
                return -1;
            }
        }
    }

    public void requestLoadAllAdnLike(int efid, int extensionEf, Message response) {
        ArrayList<AdnRecord> result;
        boolean z;
        logd("requestLoadAllAdnLike efid = " + efid + ", extensionEf = " + extensionEf);
        if (efid == IccConstants.EF_PBR) {
            result = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
            if (result != null) {
                this.isUim = true;
            } else {
                efid = 28474;
            }
        } else {
            result = getRecordsIfLoaded(efid);
        }
        StringBuilder append = new StringBuilder().append("requestLoadAllAdnLike result = null ?");
        if (result == null) {
            z = true;
        } else {
            z = false;
        }
        logi(append.append(z).toString());
        if (result != null) {
            if (response != null) {
                AsyncResult.forMessage(response).result = result;
                response.sendToTarget();
            }
            return;
        }
        ArrayList<Message> waiters = (ArrayList) this.mAdnLikeWaiters.get(efid);
        if (waiters != null) {
            waiters.add(response);
            return;
        }
        waiters = new ArrayList();
        waiters.add(response);
        this.mAdnLikeWaiters.put(efid, waiters);
        if (extensionEf < 0) {
            if (response != null) {
                AsyncResult.forMessage(response).exception = new RuntimeException("EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
                response.sendToTarget();
            }
            return;
        }
        new AdnRecordLoader(this.mFh).loadAllFromEF(efid, extensionEf, obtainMessage(1, efid, 0));
    }

    private void notifyWaiters(ArrayList<Message> waiters, AsyncResult ar) {
        if (waiters != null) {
            int s = waiters.size();
            for (int i = 0; i < s; i++) {
                Message waiter = (Message) waiters.get(i);
                if (waiter != null) {
                    logi("NotifyWaiters: " + waiter);
                    AsyncResult.forMessage(waiter, ar.result, ar.exception);
                    waiter.sendToTarget();
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        int efid;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                efid = msg.arg1;
                ArrayList<Message> waiters = (ArrayList) this.mAdnLikeWaiters.get(efid);
                this.mAdnLikeWaiters.delete(efid);
                if (ar.exception == null) {
                    this.mAdnLikeFiles.put(efid, (ArrayList) ar.result);
                } else {
                    Rlog.w(LOG_TAG, "EVENT_LOAD_ALL_ADN_LIKE_DONE exception", ar.exception);
                }
                notifyWaiters(waiters, ar);
                return;
            case 2:
                logd("EVENT_UPDATE_ADN_DONE");
                synchronized (this.mLock) {
                    if (this.mLocked) {
                        boolean z;
                        ar = (AsyncResult) msg.obj;
                        efid = msg.arg1;
                        int index = msg.arg2;
                        AdnRecord adn = ar.userObj;
                        if (ar.exception == null && adn != null) {
                            adn.setRecordIndex(index);
                            if (adn.mEfid <= 0) {
                                adn.mEfid = efid;
                            }
                            logd("mAdnLikeFiles changed index:" + index + ",adn:" + adn + "  efid:" + efid);
                            if (!(this.mAdnLikeFiles == null || this.mAdnLikeFiles.get(efid) == null)) {
                                if (efid == 20283 && !CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
                                    index -= 250;
                                }
                                ((ArrayList) this.mAdnLikeFiles.get(efid)).set(index - 1, adn);
                                logd(" index:" + index + "   efid:" + efid);
                            }
                            if (!(this.mUsimPhoneBookManager == null || efid == IccConstants.EF_FDN)) {
                                if (efid == 20283) {
                                    index += ADN_FILE_SIZE;
                                    logd(" index2:" + index);
                                }
                                this.mUsimPhoneBookManager.updateUsimPhonebookRecordsList(index - 1, adn);
                            }
                        }
                        Message response = (Message) this.mUserWriteResponse.get(efid);
                        this.mUserWriteResponse.delete(efid);
                        logi("AdnRecordCacheEx: " + ar.exception);
                        if (!(ar.exception == null || response == null)) {
                            AsyncResult.forMessage(response, null, ar.exception);
                            response.sendToTarget();
                        }
                        if (ar.exception == null) {
                            z = true;
                        } else {
                            z = false;
                        }
                        this.mSuccess = z;
                        this.mLock.notifyAll();
                        this.mLocked = false;
                    }
                }
                return;
            default:
                return;
        }
    }

    protected void logd(String msg) {
        if (DBG) {
            Rlog.d(LOG_TAG, "[AdnRecordCache] " + msg + "(slot " + this.mSlotId + ")");
        }
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[AdnRecordCache] " + msg + "(slot " + this.mSlotId + ")");
    }

    protected void logi(String msg) {
        Rlog.i(LOG_TAG, "[AdnRecordCache] " + msg + "(slot " + this.mSlotId + ")");
    }

    protected void logw(String msg) {
        Rlog.w(LOG_TAG, "[AdnRecordCache] " + msg + "(slot " + this.mSlotId + ")");
    }

    public List<UsimGroup> getUsimGroups() {
        return this.mUsimPhoneBookManager.getUsimGroups();
    }

    public String getUsimGroupById(int nGasId) {
        return this.mUsimPhoneBookManager.getUsimGroupById(nGasId);
    }

    public boolean removeUsimGroupById(int nGasId) {
        return this.mUsimPhoneBookManager.removeUsimGroupById(nGasId);
    }

    public int insertUsimGroup(String grpName) {
        return this.mUsimPhoneBookManager.insertUsimGroup(grpName);
    }

    public int updateUsimGroup(int nGasId, String grpName) {
        return this.mUsimPhoneBookManager.updateUsimGroup(nGasId, grpName);
    }

    public boolean addContactToGroup(int adnIndex, int grpIndex) {
        return this.mUsimPhoneBookManager.addContactToGroup(adnIndex, grpIndex);
    }

    public boolean removeContactFromGroup(int adnIndex, int grpIndex) {
        return this.mUsimPhoneBookManager.removeContactFromGroup(adnIndex, grpIndex);
    }

    public boolean updateContactToGroups(int adnIndex, int[] grpIdList) {
        return this.mUsimPhoneBookManager.updateContactToGroups(adnIndex, grpIdList);
    }

    public boolean moveContactFromGroupsToGroups(int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) {
        return this.mUsimPhoneBookManager.moveContactFromGroupsToGroups(adnIndex, fromGrpIdList, toGrpIdList);
    }

    public int hasExistGroup(String grpName) {
        return this.mUsimPhoneBookManager.hasExistGroup(grpName);
    }

    public int getUsimGrpMaxNameLen() {
        return this.mUsimPhoneBookManager.getUsimGrpMaxNameLen();
    }

    public int getUsimGrpMaxCount() {
        return this.mUsimPhoneBookManager.getUsimGrpMaxCount();
    }

    private void dumpAdnLikeFile() {
        int size = this.mAdnLikeFiles.size();
        logd("dumpAdnLikeFile size " + size);
        for (int i = 0; i < size; i++) {
            int key = this.mAdnLikeFiles.keyAt(i);
            ArrayList<AdnRecord> records = (ArrayList) this.mAdnLikeFiles.get(key);
            logd("dumpAdnLikeFile index " + i + " key " + key + "records size " + records.size());
            for (int j = 0; j < records.size(); j++) {
                logd("mAdnLikeFiles[" + j + "]=" + ((AdnRecord) records.get(j)));
            }
        }
    }

    public ArrayList<AlphaTag> getUsimAasList() {
        return this.mUsimPhoneBookManager.getUsimAasList();
    }

    public String getUsimAasById(int index) {
        return this.mUsimPhoneBookManager.getUsimAasById(index, 0);
    }

    public boolean removeUsimAasById(int index, int pbrIndex) {
        return this.mUsimPhoneBookManager.removeUsimAasById(index, pbrIndex);
    }

    public int insertUsimAas(String aasName) {
        return this.mUsimPhoneBookManager.insertUsimAas(aasName);
    }

    public boolean updateUsimAas(int index, int pbrIndex, String aasName) {
        return this.mUsimPhoneBookManager.updateUsimAas(index, pbrIndex, aasName);
    }

    public boolean updateAdnAas(int adnIndex, int aasIndex) {
        return this.mUsimPhoneBookManager.updateAdnAas(adnIndex, aasIndex);
    }

    public int getAnrCount() {
        return this.mUsimPhoneBookManager.getAnrCount();
    }

    public int getEmailCount() {
        return this.mUsimPhoneBookManager.getEmailCount();
    }

    public int getUsimAasMaxCount() {
        return this.mUsimPhoneBookManager.getUsimAasMaxCount();
    }

    public int getUsimAasMaxNameLen() {
        return this.mUsimPhoneBookManager.getUsimAasMaxNameLen();
    }

    public boolean hasSne() {
        return this.mUsimPhoneBookManager.hasSne();
    }

    public int getSneRecordLen() {
        return this.mUsimPhoneBookManager.getSneRecordLen();
    }

    public boolean isAdnAccessible() {
        return this.mUsimPhoneBookManager.isAdnAccessible();
    }

    public boolean isUsimPhbEfAndNeedReset(int fileId) {
        return this.mUsimPhoneBookManager.isUsimPhbEfAndNeedReset(fileId);
    }

    public UsimPBMemInfo[] getPhonebookMemStorageExt() {
        return this.mUsimPhoneBookManager.getPhonebookMemStorageExt();
    }

    public int getUpbDone() {
        return this.mUsimPhoneBookManager.getUpbDone();
    }

    public int[] getAdnRecordsCapacity() {
        return this.mUsimPhoneBookManager.getAdnRecordsCapacity();
    }

    public int oppoGetTotalAdn() {
        ArrayList<AdnRecord> adns = (ArrayList) this.mAdnLikeFiles.get(28474);
        if (adns != null) {
            logd("oppoGetTotalAdn: adns.size():" + adns.size());
            return adns.size();
        }
        logd("oppoGetTotalAdn: adns == null:");
        return -1;
    }

    public int oppoGetUesedAdn() {
        ArrayList<AdnRecord> adns = (ArrayList) this.mAdnLikeFiles.get(28474);
        if (adns == null) {
            logd("oppoGetUesedAdn: adns == null");
            return -1;
        }
        int usedAdn = 0;
        int totalAdn = adns.size();
        for (int i = 0; i < totalAdn; i++) {
            if (!((AdnRecord) adns.get(i)).isEmpty()) {
                usedAdn++;
            }
        }
        logd("oppoGetUesedAdn: usedAdn:" + usedAdn);
        return usedAdn;
    }

    public boolean hasCmdInProgress(int efid) {
        if (((Message) this.mUserWriteResponse.get(efid)) != null) {
            logd("hasCmdInProgress is True: efid:" + efid);
            return true;
        }
        logd("hasCmdInProgress is False efid:" + efid);
        return false;
    }

    public int getUsimEmailLength() {
        return this.mUsimPhoneBookManager.getUsimEmailLength();
    }

    public int oppoUpdateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        int index = -1;
        if (this.isUim && efid == 28474) {
            index = this.mUsimPhoneBookManager.oppoUpdateAdn(efid, oldAdn, newAdn, -1, pin2, response);
            if (index != -1) {
                return index;
            }
        }
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:" + efid);
            return -1;
        }
        ArrayList<AdnRecord> oldAdnList = getRecordsIfLoaded(efid);
        if (oldAdnList == null) {
            sendErrorResponse(response, "Adn list not exist for EF:" + efid);
            return -1;
        }
        int count = 1;
        Iterator<AdnRecord> it = oldAdnList.iterator();
        while (it.hasNext()) {
            if (oldAdn.isEqual((AdnRecord) it.next())) {
                index = count;
                break;
            }
            count++;
        }
        if (index == -1) {
            sendErrorResponse(response, "Adn record don't exist for " + oldAdn);
            return -1;
        } else if (((Message) this.mUserWriteResponse.get(efid)) != null) {
            sendErrorResponse(response, "Have pending update for EF:" + efid);
            return -1;
        } else {
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(this.mFh).updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
            return index;
        }
    }

    public void oppoUpdateAdnByIndex(int efid, int extensionEF, AdnRecord adn, int recordIndex, String pin2, Message response) {
        if (this.isUim && efid == 28474) {
            if (-1 == this.mUsimPhoneBookManager.oppoUpdateAdn(efid, null, adn, recordIndex, pin2, response)) {
                sendErrorResponse(response, "oppoUpdateAdnByIndex : update adn failed " + efid);
            }
        } else if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:" + efid);
        } else if (recordIndex == -1) {
            sendErrorResponse(response, "Adn record don't exist for " + adn);
        } else if (((Message) this.mUserWriteResponse.get(efid)) != null) {
            sendErrorResponse(response, "Have pending update for EF:" + efid);
        } else {
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(this.mFh).updateEF(adn, efid, extensionEF, recordIndex, pin2, obtainMessage(2, efid, recordIndex, adn));
        }
    }

    public boolean oppoCheckPbrIsExsit() {
        return this.mUsimPhoneBookManager.isPbrExsit();
    }
}
