package com.mediatek.internal.telephony;

import com.android.internal.telephony.SmsHeader;
import java.io.ByteArrayInputStream;

public class MtkSmsHeader extends SmsHeader {
    public static final int CONCATENATED_8_BIT_REFERENCE_LENGTH = 5;
    public static final int NATIONAL_LANGUAGE_LOCKING_SHIFT_LENGTH = 3;
    public static final int NATIONAL_LANGUAGE_SINGLE_SHIFT_LENGTH = 3;
    private static final String TAG = "SmsHeader";
    public NationalLanguageShift nationalLang;

    public static class NationalLanguageShift {
        public int lockingShiftId = 0;
        public int singleShiftId = 0;
    }

    public static SmsHeader fromByteArray(byte[] data) {
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        MtkSmsHeader smsHeader = new MtkSmsHeader();
        while (inStream.available() > 0) {
            int id = inStream.read();
            int length = inStream.read();
            if (id == 0) {
                SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
                concatRef.refNumber = inStream.read();
                concatRef.msgCount = inStream.read();
                concatRef.seqNumber = inStream.read();
                concatRef.isEightBits = true;
                if (!(concatRef.msgCount == 0 || concatRef.seqNumber == 0 || concatRef.seqNumber > concatRef.msgCount)) {
                    smsHeader.concatRef = concatRef;
                }
            } else if (id == 1) {
                SmsHeader.SpecialSmsMsg specialSmsMsg = new SmsHeader.SpecialSmsMsg();
                specialSmsMsg.msgIndType = inStream.read();
                specialSmsMsg.msgCount = inStream.read();
                smsHeader.specialSmsMsgList.add(specialSmsMsg);
            } else if (id == 4) {
                SmsHeader.PortAddrs portAddrs = new SmsHeader.PortAddrs();
                portAddrs.destPort = inStream.read();
                portAddrs.origPort = inStream.read();
                portAddrs.areEightBits = true;
                smsHeader.portAddrs = portAddrs;
            } else if (id == 5) {
                SmsHeader.PortAddrs portAddrs2 = new SmsHeader.PortAddrs();
                portAddrs2.destPort = (inStream.read() << 8) | inStream.read();
                portAddrs2.origPort = (inStream.read() << 8) | inStream.read();
                portAddrs2.areEightBits = false;
                smsHeader.portAddrs = portAddrs2;
            } else if (id == 8) {
                SmsHeader.ConcatRef concatRef2 = new SmsHeader.ConcatRef();
                concatRef2.refNumber = (inStream.read() << 8) | inStream.read();
                concatRef2.msgCount = inStream.read();
                concatRef2.seqNumber = inStream.read();
                concatRef2.isEightBits = false;
                if (!(concatRef2.msgCount == 0 || concatRef2.seqNumber == 0 || concatRef2.seqNumber > concatRef2.msgCount)) {
                    smsHeader.concatRef = concatRef2;
                }
            } else if (id == 36) {
                smsHeader.languageShiftTable = inStream.read();
            } else if (id != 37) {
                SmsHeader.MiscElt miscElt = new SmsHeader.MiscElt();
                miscElt.id = id;
                miscElt.data = new byte[length];
                inStream.read(miscElt.data, 0, length);
                smsHeader.miscEltList.add(miscElt);
            } else {
                smsHeader.languageTable = inStream.read();
            }
        }
        return smsHeader;
    }

    public static byte[] toByteArray(SmsHeader smsHeader) {
        if (smsHeader instanceof MtkSmsHeader) {
            MtkSmsHeader smsh = (MtkSmsHeader) smsHeader;
            if (smsh.portAddrs == null && smsh.concatRef == null && smsh.specialSmsMsgList.isEmpty() && smsh.nationalLang == null && smsh.miscEltList.isEmpty() && smsh.languageShiftTable == 0 && smsh.languageTable == 0) {
                return null;
            }
        }
        return SmsHeader.toByteArray(smsHeader);
    }

    public static byte[] getSubmitPduHeader(int destPort) {
        return getSubmitPduHeader(destPort, 0, 0, 0);
    }

    public static byte[] getSubmitPduHeader(int destPort, int originalPort) {
        return getSubmitPduHeader(destPort, originalPort, 0, 0, 0);
    }

    public static byte[] getSubmitPduHeader(int refNumber, int seqNumber, int msgCount) {
        return getSubmitPduHeader(-1, refNumber, seqNumber, msgCount);
    }

    public static byte[] getSubmitPduHeader(int destPort, int refNumber, int seqNumber, int msgCount) {
        return getSubmitPduHeaderWithLang(destPort, refNumber, seqNumber, msgCount, -1, -1);
    }

    public static byte[] getSubmitPduHeader(int destPort, int originalPort, int refNumber, int seqNumber, int msgCount) {
        return getSubmitPduHeaderWithLang(destPort, originalPort, refNumber, seqNumber, msgCount, -1, -1);
    }

    public static byte[] getSubmitPduHeaderWithLang(int destPort, int singleShiftId, int lockingShiftId) {
        return getSubmitPduHeaderWithLang(destPort, 0, 0, 0, singleShiftId, lockingShiftId);
    }

    public static byte[] getSubmitPduHeaderWithLang(int refNumber, int seqNumber, int msgCount, int singleShiftId, int lockingShiftId) {
        return getSubmitPduHeaderWithLang(-1, refNumber, seqNumber, msgCount, singleShiftId, lockingShiftId);
    }

    public static byte[] getSubmitPduHeaderWithLang(int destPort, int refNumber, int seqNumber, int msgCount, int singleShiftId, int lockingShiftId) {
        MtkSmsHeader smsHeader = new MtkSmsHeader();
        if (destPort >= 0) {
            SmsHeader.PortAddrs portAddrs = new SmsHeader.PortAddrs();
            portAddrs.destPort = destPort;
            portAddrs.origPort = 0;
            portAddrs.areEightBits = false;
            smsHeader.portAddrs = portAddrs;
        }
        if (msgCount > 0) {
            SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = seqNumber;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            smsHeader.concatRef = concatRef;
        }
        if (singleShiftId > 0 || lockingShiftId > 0) {
            smsHeader.nationalLang = new NationalLanguageShift();
            NationalLanguageShift nationalLanguageShift = smsHeader.nationalLang;
            nationalLanguageShift.singleShiftId = singleShiftId;
            nationalLanguageShift.lockingShiftId = lockingShiftId;
        }
        return SmsHeader.toByteArray(smsHeader);
    }

    public static byte[] getSubmitPduHeaderWithLang(int destPort, int originalPort, int refNumber, int seqNumber, int msgCount, int singleShiftId, int lockingShiftId) {
        MtkSmsHeader smsHeader = new MtkSmsHeader();
        if (destPort >= 0) {
            SmsHeader.PortAddrs portAddrs = new SmsHeader.PortAddrs();
            portAddrs.destPort = destPort;
            portAddrs.origPort = originalPort;
            portAddrs.areEightBits = false;
            smsHeader.portAddrs = portAddrs;
        }
        if (msgCount > 0) {
            SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = seqNumber;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            smsHeader.concatRef = concatRef;
        }
        if (singleShiftId > 0 || lockingShiftId > 0) {
            smsHeader.nationalLang = new NationalLanguageShift();
            NationalLanguageShift nationalLanguageShift = smsHeader.nationalLang;
            nationalLanguageShift.singleShiftId = singleShiftId;
            nationalLanguageShift.lockingShiftId = lockingShiftId;
        }
        return SmsHeader.toByteArray(smsHeader);
    }
}
