package com.android.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmAlphabet;
import com.mediatek.internal.telephony.uicc.CsimPhbStorageInfo;
import com.mediatek.internal.telephony.uicc.PhbEntry;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
public class AdnRecordLoader extends Handler {
    protected static final boolean DBG = false;
    static final int EVENT_ADN_LOAD_ALL_DONE = 3;
    static final int EVENT_ADN_LOAD_DONE = 1;
    static final int EVENT_EF_LINEAR_RECORD_SIZE_DONE = 4;
    static final int EVENT_EXT_RECORD_LOAD_DONE = 2;
    static final int EVENT_PHB_LOAD_ALL_DONE = 104;
    static final int EVENT_PHB_LOAD_DONE = 103;
    static final int EVENT_PHB_QUERY_STAUTS = 105;
    static final int EVENT_UPDATE_PHB_RECORD_DONE = 101;
    static final int EVENT_UPDATE_RECORD_DONE = 5;
    static final int EVENT_VERIFY_PIN2 = 102;
    static final String LOG_TAG = "AdnRecordLoader";
    static final boolean VDBG = false;
    int current_read;
    ArrayList<AdnRecord> mAdns;
    int mEf;
    int mExtensionEF;
    private IccFileHandler mFh;
    int mNameLength;
    int mNumberLength;
    int mPendingExtLoads;
    String mPin2;
    int mRecordNumber;
    Object mResult;
    Message mUserResponse;
    int total;
    int used;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.AdnRecordLoader.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.AdnRecordLoader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.AdnRecordLoader.<clinit>():void");
    }

    AdnRecordLoader(IccFileHandler fh) {
        super(Looper.getMainLooper());
        this.mNameLength = 0;
        this.mNumberLength = 0;
        this.mFh = fh;
    }

    private String getEFPath(int efid) {
        if (efid == 28474) {
            return "3F007F10";
        }
        return null;
    }

    public void loadFromEF(int ef, int extensionEF, int recordNumber, Message response) {
        this.mEf = ef;
        this.mExtensionEF = extensionEF;
        this.mRecordNumber = recordNumber;
        this.mUserResponse = response;
        int type = getPhbStorageType(ef);
        if (type != -1) {
            this.mFh.mCi.ReadPhbEntry(type, recordNumber, recordNumber, obtainMessage(103));
        } else {
            this.mFh.loadEFLinearFixed(ef, getEFPath(ef), recordNumber, obtainMessage(1));
        }
    }

    public void loadAllFromEF(int ef, int extensionEF, Message response) {
        this.mEf = ef;
        this.mExtensionEF = extensionEF;
        this.mUserResponse = response;
        Rlog.i(LOG_TAG, "Usim :loadEFLinearFixedAll");
        int type = getPhbStorageType(ef);
        if (type != -1) {
            this.mFh.mCi.queryPhbStorageInfo(type, obtainMessage(105));
        } else {
            this.mFh.loadEFLinearFixedAll(ef, getEFPath(ef), obtainMessage(3));
        }
    }

    public void updateEF(AdnRecord adn, int ef, int extensionEF, int recordNumber, String pin2, Message response) {
        this.mEf = ef;
        this.mExtensionEF = extensionEF;
        this.mRecordNumber = recordNumber;
        this.mUserResponse = response;
        this.mPin2 = pin2;
        int type = getPhbStorageType(ef);
        if (type != -1) {
            updatePhb(adn, type);
        } else {
            this.mFh.getEFLinearRecordSize(ef, getEFPath(ef), obtainMessage(4, adn));
        }
    }

    public void handleMessage(Message msg) {
        try {
            AsyncResult ar;
            byte[] data;
            AdnRecord adn;
            int i;
            int retryCount;
            PhbEntry[] entries;
            int[] readInfo;
            switch (msg.what) {
                case 1:
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        adn = new AdnRecord(this.mEf, this.mRecordNumber, data);
                        this.mResult = adn;
                        if (adn.hasExtendedRecord()) {
                            this.mPendingExtLoads = 1;
                            this.mFh.loadEFLinearFixed(this.mExtensionEF, adn.mExtRecord, obtainMessage(2, adn));
                            break;
                        }
                    }
                    throw new RuntimeException("load failed", ar.exception);
                    break;
                case 2:
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    adn = (AdnRecord) ar.userObj;
                    if (ar.exception == null) {
                        if (DBG) {
                            Rlog.d(LOG_TAG, "ADN extension EF: 0x" + Integer.toHexString(this.mExtensionEF) + ":" + adn.mExtRecord + "\n" + IccUtils.bytesToHexString(data));
                        }
                        adn.appendExtRecord(data);
                    } else {
                        Rlog.e(LOG_TAG, "Failed to read ext record. Clear the number now.");
                        adn.setNumber(UsimPBMemInfo.STRING_NOT_SET);
                    }
                    this.mPendingExtLoads--;
                    break;
                case 3:
                    ar = (AsyncResult) msg.obj;
                    ArrayList<byte[]> datas = ar.result;
                    if (ar.exception == null) {
                        this.mAdns = new ArrayList(datas.size());
                        this.mResult = this.mAdns;
                        this.mPendingExtLoads = 0;
                        int s = datas.size();
                        for (i = 0; i < s; i++) {
                            adn = new AdnRecord(this.mEf, i + 1, (byte[]) datas.get(i));
                            this.mAdns.add(adn);
                            if (adn.hasExtendedRecord()) {
                                this.mPendingExtLoads++;
                                this.mFh.loadEFLinearFixed(this.mExtensionEF, adn.mExtRecord, obtainMessage(2, adn));
                            }
                        }
                        break;
                    }
                    retryCount = Integer.parseInt(ar.userObj.toString());
                    if (retryCount < 3) {
                        retryCount++;
                        this.mFh.loadEFLinearFixedAll(this.mEf, obtainMessage(3, Integer.valueOf(retryCount)));
                        break;
                    }
                    throw new RuntimeException("load failed", ar.exception);
                case 4:
                    ar = msg.obj;
                    adn = ar.userObj;
                    if (ar.exception == null) {
                        int[] recordSize = (int[]) ar.result;
                        int recordIndex = this.mRecordNumber;
                        if (!CsimPhbStorageInfo.hasModemPhbEnhanceCapability(this.mFh)) {
                            recordIndex = ((recordIndex - 1) % 250) + 1;
                        }
                        Rlog.d(LOG_TAG, "[AdnRecordLoader] recordIndex :" + recordIndex);
                        if (recordSize.length == 3 && recordIndex <= recordSize[2]) {
                            Rlog.d(LOG_TAG, "[AdnRecordLoader] EVENT_EF_LINEAR_RECORD_SIZE_DONE safe ");
                            Rlog.d(LOG_TAG, "in EVENT_EF_LINEAR_RECORD_SIZE_DONE,call adn.buildAdnString");
                            data = adn.buildAdnString(recordSize[0]);
                            if (data != null) {
                                this.mFh.updateEFLinearFixed(this.mEf, getEFPath(this.mEf), recordIndex, data, this.mPin2, obtainMessage(5));
                                this.mPendingExtLoads = 1;
                                break;
                            }
                            Rlog.d(LOG_TAG, "data is null");
                            int errorNum = adn.getErrorNumber();
                            if (errorNum != -1) {
                                if (errorNum != -2) {
                                    if (errorNum != -15) {
                                        this.mPendingExtLoads = 0;
                                        this.mResult = null;
                                        break;
                                    }
                                    throw new RuntimeException("wrong ADN format", ar.exception);
                                }
                                throw new RuntimeException("data is null and TEXT_STRING_TOO_LONG", CommandException.fromRilErrno(1002));
                            }
                            throw new RuntimeException("data is null and DIAL_STRING_TOO_LONG", CommandException.fromRilErrno(1001));
                        }
                        throw new RuntimeException("get wrong EF record size format", ar.exception);
                    }
                    throw new RuntimeException("get EF record size failed", ar.exception);
                    break;
                case 5:
                    ar = (AsyncResult) msg.obj;
                    IccIoResult result = ar.result;
                    if (ar.exception == null) {
                        Throwable iccException = result.getException();
                        if (iccException == null) {
                            this.mPendingExtLoads = 0;
                            this.mResult = null;
                            break;
                        }
                        throw new RuntimeException("update EF adn record failed for sw", iccException);
                    }
                    throw new RuntimeException("update EF adn record failed", ar.exception);
                case 101:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mPendingExtLoads = 0;
                        this.mResult = null;
                        break;
                    }
                    throw new RuntimeException("update PHB EF record failed", ar.exception);
                case 102:
                    ar = (AsyncResult) msg.obj;
                    adn = (AdnRecord) ar.userObj;
                    if (ar.exception == null) {
                        writeEntryToModem(adn, getPhbStorageType(this.mEf));
                        this.mPendingExtLoads = 1;
                        break;
                    }
                    throw new RuntimeException("PHB Verify PIN2 error", ar.exception);
                case 103:
                    ar = (AsyncResult) msg.obj;
                    entries = ar.result;
                    if (ar.exception == null) {
                        this.mResult = getAdnRecordFromPhbEntry(entries[0]);
                        this.mPendingExtLoads = 0;
                        break;
                    }
                    throw new RuntimeException("PHB Read an entry Error", ar.exception);
                case 104:
                    ar = (AsyncResult) msg.obj;
                    readInfo = (int[]) ar.userObj;
                    entries = (PhbEntry[]) ar.result;
                    if (ar.exception == null) {
                        i = 0;
                        while (i < entries.length) {
                            adn = getAdnRecordFromPhbEntry(entries[i]);
                            if (adn != null) {
                                adn.mNumberLength = this.mNumberLength;
                                adn.mNameLength = this.mNameLength;
                                this.mAdns.set(adn.mRecordNumber - 1, adn);
                                readInfo[1] = readInfo[1] - 1;
                                if (DBG) {
                                    Rlog.d(LOG_TAG, "Read entries: " + adn);
                                }
                                i++;
                            } else {
                                throw new RuntimeException("getAdnRecordFromPhbEntry return null", CommandException.fromRilErrno(2));
                            }
                        }
                        readInfo[0] = readInfo[0] + 10;
                        if (readInfo[1] >= 0) {
                            readInfo[3] = 0;
                            if (readInfo[1] != 0 && readInfo[0] < readInfo[2]) {
                                readEntryFromModem(getPhbStorageType(this.mEf), readInfo);
                                break;
                            }
                            this.mResult = this.mAdns;
                            this.mPendingExtLoads = 0;
                            break;
                        }
                        throw new RuntimeException("the read entries is not sync with query status: " + readInfo[1], CommandException.fromRilErrno(2));
                    } else if (readInfo[3] < 3) {
                        readInfo[3] = readInfo[3] + 1;
                        readEntryFromModem(getPhbStorageType(this.mEf), readInfo);
                        break;
                    } else {
                        throw new RuntimeException("PHB Read Entries Error", ar.exception);
                    }
                    break;
                case 105:
                    ar = (AsyncResult) msg.obj;
                    int[] info = (int[]) ar.result;
                    int type;
                    if (ar.exception == null) {
                        type = getPhbStorageType(this.mEf);
                        readInfo = new int[4];
                        readInfo[0] = 1;
                        readInfo[1] = info[0];
                        readInfo[2] = info[1];
                        readInfo[3] = 0;
                        Rlog.d(LOG_TAG, "Read phb length: " + info.length);
                        if (info.length == 4) {
                            this.mNumberLength = info[2];
                            this.mNameLength = info[3];
                            Rlog.d(LOG_TAG, "num length: " + info[2] + " name len " + info[3]);
                        }
                        this.mAdns = new ArrayList(readInfo[2]);
                        for (i = 0; i < readInfo[2]; i++) {
                            adn = new AdnRecord(this.mEf, i + 1, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET);
                            adn.mNumberLength = this.mNumberLength;
                            adn.mNameLength = this.mNameLength;
                            this.mAdns.add(i, adn);
                        }
                        readEntryFromModem(type, readInfo);
                        this.mPendingExtLoads = 1;
                        break;
                    }
                    retryCount = Integer.parseInt(ar.userObj.toString());
                    if (retryCount < 3) {
                        retryCount++;
                        type = getPhbStorageType(this.mEf);
                        this.mFh.mCi.queryPhbStorageInfo(type, obtainMessage(105, Integer.valueOf(retryCount)));
                        break;
                    }
                    throw new RuntimeException("PHB Query Info Error", ar.exception);
                case 990:
                    ar = (AsyncResult) msg.obj;
                    int[] resultInfo = (int[]) ar.result;
                    this.mResult = null;
                    if (ar.exception == null) {
                        Object ret = new int[2];
                        ret[1] = resultInfo[0] - 14;
                        ret[0] = 20;
                        this.mResult = ret;
                        this.mPendingExtLoads = 0;
                        break;
                    }
                    throw new RuntimeException("PHB Query Field Info Error", ar.exception);
            }
            if (!(this.mUserResponse == null || this.mPendingExtLoads != 0 || this.mUserResponse.getTarget() == null)) {
                AsyncResult.forMessage(this.mUserResponse).result = this.mResult;
                this.mUserResponse.sendToTarget();
                this.mUserResponse = null;
            }
        } catch (RuntimeException exc) {
            if (!(this.mUserResponse == null || this.mUserResponse.getTarget() == null)) {
                Rlog.w(LOG_TAG, "handleMessage RuntimeException: " + exc.getMessage());
                Rlog.w(LOG_TAG, "handleMessage RuntimeException: " + exc.getCause());
                if (exc.getCause() == null) {
                    Rlog.d(LOG_TAG, "handleMessage Null RuntimeException");
                    AsyncResult.forMessage(this.mUserResponse).exception = new CommandException(Error.GENERIC_FAILURE);
                } else {
                    AsyncResult.forMessage(this.mUserResponse).exception = exc.getCause();
                }
                this.mUserResponse.sendToTarget();
                this.mUserResponse = null;
            }
        }
    }

    private void updatePhb(AdnRecord adn, int type) {
        if (this.mPin2 != null) {
            this.mFh.mCi.supplyIccPin2(this.mPin2, obtainMessage(102, adn));
        } else {
            writeEntryToModem(adn, type);
        }
    }

    private boolean canUseGsm7Bit(String alphaId) {
        return GsmAlphabet.countGsmSeptets(alphaId, true) != null;
    }

    private String encodeATUCS(String input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            String hexInt = Integer.toHexString(input.charAt(i));
            for (int j = 0; j < 4 - hexInt.length(); j++) {
                output.append("0");
            }
            output.append(hexInt);
        }
        return output.toString();
    }

    private int getPhbStorageType(int ef) {
        switch (ef) {
            case 28474:
                return 0;
            case IccConstants.EF_FDN /*28475*/:
                return 1;
            default:
                return -1;
        }
    }

    private void writeEntryToModem(AdnRecord adn, int type) {
        Object obj = null;
        int ton = 129;
        String number = adn.getNumber();
        String alphaId = adn.getAlphaTag();
        if (number.indexOf(43) != -1) {
            if (number.indexOf(43) != number.lastIndexOf(43) && DBG) {
                Rlog.w(LOG_TAG, "There are multiple '+' in the number: " + number);
            }
            ton = 145;
            number = number.replace("+", UsimPBMemInfo.STRING_NOT_SET);
        }
        number = number.replace('N', '?').replace(',', 'p').replace("W", UsimPBMemInfo.STRING_NOT_SET);
        alphaId = encodeATUCS(alphaId);
        PhbEntry entry = new PhbEntry();
        if (number.equals(UsimPBMemInfo.STRING_NOT_SET) && alphaId.equals(UsimPBMemInfo.STRING_NOT_SET) && ton == 129) {
            obj = 1;
        }
        if (obj == null) {
            entry.type = type;
            entry.index = this.mRecordNumber;
            entry.number = PhoneNumberUtils.stripSeparators(number);
            entry.ton = ton;
            entry.alphaId = alphaId;
        } else {
            entry.type = type;
            entry.index = this.mRecordNumber;
            entry.number = null;
            entry.ton = ton;
            entry.alphaId = null;
        }
        this.mFh.mCi.writePhbEntry(entry, obtainMessage(101));
    }

    private void readEntryFromModem(int type, int[] readInfo) {
        if (readInfo.length != 4) {
            Rlog.e(LOG_TAG, "readEntryToModem, invalid paramters:" + readInfo.length);
            return;
        }
        int eIndex = (readInfo[0] + 10) - 1;
        if (eIndex > readInfo[2]) {
            eIndex = readInfo[2];
        }
        this.mFh.mCi.ReadPhbEntry(type, readInfo[0], eIndex, obtainMessage(104, readInfo));
    }

    private AdnRecord getAdnRecordFromPhbEntry(PhbEntry entry) {
        if (DBG) {
            Rlog.d(LOG_TAG, "Parse Adn entry :" + entry);
        }
        byte[] ba = IccUtils.hexStringToBytes(entry.alphaId);
        if (ba == null) {
            Rlog.e(LOG_TAG, "entry.alphaId is null");
            return null;
        }
        try {
            String number;
            String alphaId = new String(ba, 0, entry.alphaId.length() / 2, "utf-16be");
            if (entry.ton == 145) {
                number = PhoneNumberUtils.prependPlusToNumber(entry.number);
            } else {
                number = entry.number;
            }
            return new AdnRecord(this.mEf, entry.index, alphaId, number.replace('?', 'N'));
        } catch (UnsupportedEncodingException ex) {
            Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
            return null;
        }
    }
}
