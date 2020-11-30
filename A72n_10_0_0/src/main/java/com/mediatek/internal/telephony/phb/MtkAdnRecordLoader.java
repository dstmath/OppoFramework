package com.mediatek.internal.telephony.phb;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.uicc.AdnRecordLoader;
import com.android.internal.telephony.uicc.IccException;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.MtkPhoneNumberUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MtkAdnRecordLoader extends AdnRecordLoader {
    private static int ADN_FILE_SIZE = 250;
    static final int EVENT_PHB_LOAD_ALL_DONE = 104;
    static final int EVENT_PHB_LOAD_DONE = 103;
    static final int EVENT_PHB_QUERY_STAUTS = 105;
    static final int EVENT_UPDATE_PHB_RECORD_DONE = 101;
    static final int EVENT_VERIFY_PIN2 = 102;
    static final String LOG_TAG = "MtkRecordLoader";
    private ArrayList<MtkAdnRecord> mAdns;

    public MtkAdnRecordLoader(IccFileHandler fh) {
        super(fh);
    }

    public void loadFromEF(int ef, int extensionEF, int recordNumber, Message response) {
        this.mEf = ef;
        this.mExtensionEF = extensionEF;
        this.mRecordNumber = recordNumber;
        this.mUserResponse = response;
        int type = getPhbStorageType(ef);
        if (type != -1) {
            this.mFh.mCi.readPhbEntry(type, recordNumber, recordNumber, obtainMessage(EVENT_PHB_LOAD_DONE));
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

    public void updateEF(MtkAdnRecord adn, int ef, int extensionEF, int recordNumber, String pin2, Message response) {
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
            int i = msg.what;
            if (i == 1) {
                AsyncResult ar = (AsyncResult) msg.obj;
                byte[] data = (byte[]) ar.result;
                if (ar.exception == null) {
                    MtkAdnRecord adn = new MtkAdnRecord(this.mEf, this.mRecordNumber, data);
                    this.mResult = adn;
                    if (adn.hasExtendedRecord()) {
                        this.mPendingExtLoads = 1;
                        this.mFh.loadEFLinearFixed(this.mExtensionEF, adn.mExtRecord, obtainMessage(2, adn));
                    }
                } else {
                    throw new RuntimeException("load failed", ar.exception);
                }
            } else if (i == 2) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                byte[] data2 = (byte[]) ar2.result;
                MtkAdnRecord adn2 = (MtkAdnRecord) ar2.userObj;
                if (ar2.exception == null) {
                    Rlog.d(LOG_TAG, "ADN extension EF: 0x" + Integer.toHexString(this.mExtensionEF) + ":" + adn2.mExtRecord + "\n" + IccUtils.bytesToHexString(data2));
                    adn2.appendExtRecord(data2);
                } else {
                    Rlog.e(LOG_TAG, "Failed to read ext record. Clear the number now.");
                    adn2.setNumber("");
                }
                this.mPendingExtLoads--;
            } else if (i == 3) {
                AsyncResult ar3 = (AsyncResult) msg.obj;
                ArrayList<byte[]> datas = (ArrayList) ar3.result;
                if (ar3.exception != null) {
                    try {
                        int retryCount = Integer.parseInt(ar3.userObj.toString());
                        if (retryCount < 3) {
                            this.mFh.loadEFLinearFixedAll(this.mEf, obtainMessage(3, Integer.valueOf(retryCount + 1)));
                        } else {
                            throw new RuntimeException("load failed", ar3.exception);
                        }
                    } catch (NumberFormatException e) {
                        Rlog.d(LOG_TAG, e.toString());
                    } catch (Exception e2) {
                        Rlog.d(LOG_TAG, e2.toString());
                    }
                }
                this.mAdns = new ArrayList<>(datas.size());
                this.mResult = this.mAdns;
                this.mPendingExtLoads = 0;
                int s = datas.size();
                for (int i2 = 0; i2 < s; i2++) {
                    MtkAdnRecord adn3 = new MtkAdnRecord(this.mEf, i2 + 1, datas.get(i2));
                    this.mAdns.add(adn3);
                    if (adn3.hasExtendedRecord()) {
                        this.mPendingExtLoads++;
                        this.mFh.loadEFLinearFixed(this.mExtensionEF, adn3.mExtRecord, obtainMessage(2, adn3));
                    }
                }
            } else if (i == 4) {
                AsyncResult ar4 = (AsyncResult) msg.obj;
                MtkAdnRecord adn4 = (MtkAdnRecord) ar4.userObj;
                if (ar4.exception == null) {
                    int[] recordSize = (int[]) ar4.result;
                    int recordIndex = this.mRecordNumber;
                    if (!CsimPhbUtil.hasModemPhbEnhanceCapability(this.mFh)) {
                        recordIndex = ((recordIndex - 1) % ADN_FILE_SIZE) + 1;
                    }
                    Rlog.d(LOG_TAG, "[AdnRecordLoader] recordIndex :" + recordIndex);
                    if (recordSize.length != 3 || recordIndex > recordSize[2]) {
                        throw new RuntimeException("get wrong EF record size format", ar4.exception);
                    }
                    Rlog.d(LOG_TAG, "[AdnRecordLoader] EVENT_EF_LINEAR_RECORD_SIZE_DONE safe ");
                    Rlog.d(LOG_TAG, "in EVENT_EF_LINEAR_RECORD_SIZE_DONE,call adn.buildAdnString");
                    byte[] data3 = adn4.buildAdnString(recordSize[0]);
                    if (data3 == null) {
                        Rlog.d(LOG_TAG, "data is null");
                        int errorNum = adn4.getErrorNumber();
                        if (errorNum == -1) {
                            throw new RuntimeException("data is null and DIAL_STRING_TOO_LONG", CommandException.fromRilErrno(501));
                        } else if (errorNum == -2) {
                            throw new RuntimeException("data is null and TEXT_STRING_TOO_LONG", CommandException.fromRilErrno(502));
                        } else if (errorNum != -15) {
                            this.mPendingExtLoads = 0;
                            this.mResult = null;
                        } else {
                            throw new RuntimeException("wrong ADN format", ar4.exception);
                        }
                    } else {
                        this.mFh.updateEFLinearFixed(this.mEf, getEFPath(this.mEf), recordIndex, data3, this.mPin2, obtainMessage(5));
                        this.mPendingExtLoads = 1;
                    }
                } else {
                    throw new RuntimeException("get EF record size failed", ar4.exception);
                }
            } else if (i == 5) {
                AsyncResult ar5 = (AsyncResult) msg.obj;
                IccIoResult result = (IccIoResult) ar5.result;
                if (ar5.exception == null) {
                    IccException iccException = result.getException();
                    if (iccException == null) {
                        this.mPendingExtLoads = 0;
                        this.mResult = null;
                    } else {
                        throw new RuntimeException("update EF adn record failed for sw", iccException);
                    }
                } else {
                    throw new RuntimeException("update EF adn record failed", ar5.exception);
                }
            } else if (i != 990) {
                switch (i) {
                    case 101:
                        AsyncResult ar6 = (AsyncResult) msg.obj;
                        if (ar6.exception == null) {
                            this.mPendingExtLoads = 0;
                            this.mResult = null;
                            break;
                        } else {
                            throw new RuntimeException("update PHB EF record failed", ar6.exception);
                        }
                    case 102:
                        AsyncResult ar7 = (AsyncResult) msg.obj;
                        MtkAdnRecord adn5 = (MtkAdnRecord) ar7.userObj;
                        if (ar7.exception == null) {
                            writeEntryToModem(adn5, getPhbStorageType(this.mEf));
                            this.mPendingExtLoads = 1;
                            break;
                        } else {
                            throw new RuntimeException("PHB Verify PIN2 error", ar7.exception);
                        }
                    case EVENT_PHB_LOAD_DONE /* 103 */:
                        AsyncResult ar8 = (AsyncResult) msg.obj;
                        PhbEntry[] entries = (PhbEntry[]) ar8.result;
                        if (ar8.exception == null) {
                            this.mResult = getAdnRecordFromPhbEntry(entries[0]);
                            this.mPendingExtLoads = 0;
                            break;
                        } else {
                            throw new RuntimeException("PHB Read an entry Error", ar8.exception);
                        }
                    case 104:
                        AsyncResult ar9 = (AsyncResult) msg.obj;
                        int[] readInfo = (int[]) ar9.userObj;
                        PhbEntry[] entries2 = (PhbEntry[]) ar9.result;
                        if (ar9.exception == null) {
                            for (PhbEntry phbEntry : entries2) {
                                MtkAdnRecord adn6 = getAdnRecordFromPhbEntry(phbEntry);
                                if (adn6 != null) {
                                    this.mAdns.set(adn6.mRecordNumber - 1, adn6);
                                    readInfo[1] = readInfo[1] - 1;
                                } else {
                                    throw new RuntimeException("getAdnRecordFromPhbEntry return null", CommandException.fromRilErrno(2));
                                }
                            }
                            readInfo[0] = readInfo[0] + 10;
                            if (readInfo[1] >= 0) {
                                readInfo[3] = 0;
                                if (readInfo[1] == 0 || readInfo[0] >= readInfo[2]) {
                                    this.mResult = this.mAdns;
                                    this.mPendingExtLoads = 0;
                                    break;
                                } else {
                                    readEntryFromModem(getPhbStorageType(this.mEf), readInfo);
                                    break;
                                }
                            } else {
                                throw new RuntimeException("the read entries is not sync with query status: " + readInfo[1], CommandException.fromRilErrno(2));
                            }
                        } else if (readInfo[3] < 3) {
                            readInfo[3] = readInfo[3] + 1;
                            readEntryFromModem(getPhbStorageType(this.mEf), readInfo);
                            break;
                        } else {
                            throw new RuntimeException("PHB Read Entries Error", ar9.exception);
                        }
                        break;
                    case 105:
                        AsyncResult ar10 = (AsyncResult) msg.obj;
                        int[] info = (int[]) ar10.result;
                        if (ar10.exception != null) {
                            try {
                                int retryCount2 = Integer.parseInt(ar10.userObj.toString());
                                if (retryCount2 < 3) {
                                    this.mFh.mCi.queryPhbStorageInfo(getPhbStorageType(this.mEf), obtainMessage(105, Integer.valueOf(retryCount2 + 1)));
                                    break;
                                } else {
                                    throw new RuntimeException("PHB Query Info Error", ar10.exception);
                                }
                            } catch (NumberFormatException e3) {
                                Rlog.d(LOG_TAG, e3.toString());
                            } catch (Exception e4) {
                                Rlog.d(LOG_TAG, e4.toString());
                            }
                        }
                        int type = getPhbStorageType(this.mEf);
                        int[] readInfo2 = {1, info[0], info[1], 0};
                        this.mAdns = new ArrayList<>(readInfo2[2]);
                        for (int i3 = 0; i3 < readInfo2[2]; i3++) {
                            this.mAdns.add(i3, new MtkAdnRecord(this.mEf, i3 + 1, "", ""));
                        }
                        readEntryFromModem(type, readInfo2);
                        this.mPendingExtLoads = 1;
                        break;
                }
            } else {
                AsyncResult ar11 = (AsyncResult) msg.obj;
                int[] resultInfo = (int[]) ar11.result;
                this.mResult = null;
                if (ar11.exception == null) {
                    int[] ret = new int[2];
                    ret[1] = resultInfo[0] - 14;
                    ret[0] = 20;
                    this.mResult = ret;
                    this.mPendingExtLoads = 0;
                } else {
                    throw new RuntimeException("PHB Query Field Info Error", ar11.exception);
                }
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
                    AsyncResult.forMessage(this.mUserResponse).exception = new CommandException(CommandException.Error.GENERIC_FAILURE);
                } else {
                    AsyncResult.forMessage(this.mUserResponse).exception = exc.getCause();
                }
                this.mUserResponse.sendToTarget();
                this.mUserResponse = null;
            }
        }
    }

    private void updatePhb(MtkAdnRecord adn, int type) {
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
        if (ef == 28474) {
            return 0;
        }
        if (ef != 28475) {
            return -1;
        }
        return 1;
    }

    private void writeEntryToModem(MtkAdnRecord adn, int type) {
        int ton = 129;
        String number = adn.getNumber();
        String alphaId = adn.getAlphaTag();
        if (number.indexOf(43) != -1) {
            if (number.indexOf(43) != number.lastIndexOf(43)) {
                Rlog.w(LOG_TAG, "There are multiple '+' in number");
            }
            ton = 145;
            number = number.replace("+", "");
        }
        String number2 = number.replace('N', '?').replace(',', 'p').replace("W", "");
        String alphaId2 = encodeATUCS(alphaId);
        PhbEntry entry = new PhbEntry();
        if (!number2.equals("") || !alphaId2.equals("") || ton != 129) {
            entry.type = type;
            entry.index = this.mRecordNumber;
            entry.number = PhoneNumberUtils.stripSeparators(number2);
            entry.ton = ton;
            entry.alphaId = alphaId2;
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
        this.mFh.mCi.readPhbEntry(type, readInfo[0], eIndex, obtainMessage(104, readInfo));
    }

    private MtkAdnRecord getAdnRecordFromPhbEntry(PhbEntry entry) {
        String number;
        Rlog.d(LOG_TAG, "Parse Adn entry :" + entry);
        byte[] ba = IccUtils.hexStringToBytes(entry.alphaId);
        if (ba == null) {
            Rlog.e(LOG_TAG, "entry.alphaId is null");
            return null;
        }
        try {
            String alphaId = new String(ba, 0, entry.alphaId.length() / 2, "utf-16be");
            if (entry.ton == 145) {
                number = MtkPhoneNumberUtils.prependPlusToNumber(entry.number);
            } else {
                number = entry.number;
            }
            return new MtkAdnRecord(this.mEf, entry.index, alphaId, number.replace('?', 'N'));
        } catch (UnsupportedEncodingException ex) {
            Rlog.e(LOG_TAG, "implausible UnsupportedEncodingException", ex);
            return null;
        }
    }
}
