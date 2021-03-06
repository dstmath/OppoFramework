package com.google.android.mms.pdu;

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

public class PduComposer {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected static final int END_STRING_FLAG = 0;
    private static final int LENGTH_QUOTE = 31;
    private static final int LONG_INTEGER_LENGTH_MAX = 8;
    protected static final int PDU_COMPOSER_BLOCK_SIZE = 1024;
    protected static final int PDU_COMPOSE_CONTENT_ERROR = 1;
    protected static final int PDU_COMPOSE_FIELD_NOT_SET = 2;
    protected static final int PDU_COMPOSE_FIELD_NOT_SUPPORTED = 3;
    protected static final int PDU_COMPOSE_SUCCESS = 0;
    private static final int PDU_EMAIL_ADDRESS_TYPE = 2;
    private static final int PDU_IPV4_ADDRESS_TYPE = 3;
    private static final int PDU_IPV6_ADDRESS_TYPE = 4;
    private static final int PDU_PHONE_NUMBER_ADDRESS_TYPE = 1;
    private static final int PDU_UNKNOWN_ADDRESS_TYPE = 5;
    private static final int QUOTED_STRING_FLAG = 34;
    static final String REGEXP_EMAIL_ADDRESS_TYPE = "[a-zA-Z| ]*\\<{0,1}[a-zA-Z| ]+@{1}[a-zA-Z| ]+\\.{1}[a-zA-Z| ]+\\>{0,1}";
    static final String REGEXP_IPV4_ADDRESS_TYPE = "[0-9]{1,3}\\.{1}[0-9]{1,3}\\.{1}[0-9]{1,3}\\.{1}[0-9]{1,3}";
    static final String REGEXP_IPV6_ADDRESS_TYPE = "[a-fA-F]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}";
    static final String REGEXP_PHONE_NUMBER_ADDRESS_TYPE = "\\+?[0-9|\\.|\\-]+";
    private static final int SHORT_INTEGER_MAX = 127;
    static final String STRING_IPV4_ADDRESS_TYPE = "/TYPE=IPV4";
    static final String STRING_IPV6_ADDRESS_TYPE = "/TYPE=IPV6";
    static final String STRING_PHONE_NUMBER_ADDRESS_TYPE = "/TYPE=PLMN";
    private static final int TEXT_MAX = 127;
    protected static HashMap<String, Integer> mContentTypeMap;
    protected ByteArrayOutputStream mMessage = null;
    protected GenericPdu mPdu = null;
    protected PduHeaders mPduHeader = null;
    protected int mPosition = 0;
    protected final ContentResolver mResolver;
    protected BufferStack mStack = null;

    static {
        mContentTypeMap = null;
        mContentTypeMap = new HashMap<>();
        for (int i = 0; i < PduContentTypes.contentTypes.length; i++) {
            mContentTypeMap.put(PduContentTypes.contentTypes[i], Integer.valueOf(i));
        }
    }

    public PduComposer(Context context, GenericPdu pdu) {
        this.mPdu = pdu;
        this.mResolver = context.getContentResolver();
        this.mPduHeader = pdu.getPduHeaders();
        this.mStack = new BufferStack();
        this.mMessage = new ByteArrayOutputStream();
        this.mPosition = 0;
    }

    public byte[] make() {
        int type = this.mPdu.getMessageType();
        if (type != 128) {
            if (type != 135) {
                switch (type) {
                    case 131:
                        if (makeNotifyResp() != 0) {
                            return null;
                        }
                        break;
                    case 132:
                        break;
                    case 133:
                        if (makeAckInd() != 0) {
                            return null;
                        }
                        break;
                    default:
                        return null;
                }
            } else if (makeReadRecInd() != 0) {
                return null;
            }
            return this.mMessage.toByteArray();
        }
        if (makeSendRetrievePdu(type) != 0) {
            return null;
        }
        return this.mMessage.toByteArray();
    }

    /* access modifiers changed from: protected */
    public void arraycopy(byte[] buf, int pos, int length) {
        this.mMessage.write(buf, pos, length);
        this.mPosition += length;
    }

    /* access modifiers changed from: protected */
    public void append(int value) {
        this.mMessage.write(value);
        this.mPosition++;
    }

    /* access modifiers changed from: protected */
    public void appendShortInteger(int value) {
        append((value | 128) & 255);
    }

    /* access modifiers changed from: protected */
    public void appendOctet(int number) {
        append(number);
    }

    /* access modifiers changed from: protected */
    public void appendShortLength(int value) {
        append(value);
    }

    /* access modifiers changed from: protected */
    public void appendLongInteger(long longInt) {
        long temp = longInt;
        int size = 0;
        while (temp != 0 && size < 8) {
            temp >>>= 8;
            size++;
        }
        appendShortLength(size);
        int shift = (size - 1) * 8;
        for (int i = 0; i < size; i++) {
            append((int) ((longInt >>> shift) & 255));
            shift -= 8;
        }
    }

    /* access modifiers changed from: protected */
    public void appendTextString(byte[] text) {
        if ((text[0] & 255) > Byte.MAX_VALUE) {
            append(127);
        }
        arraycopy(text, 0, text.length);
        append(0);
    }

    /* access modifiers changed from: protected */
    public void appendTextString(String str) {
        appendTextString(str.getBytes());
    }

    /* access modifiers changed from: protected */
    public void appendEncodedString(EncodedStringValue enStr) {
        int charset = enStr.getCharacterSet();
        byte[] textString = enStr.getTextString();
        if (textString != null) {
            this.mStack.newbuf();
            PositionMarker start = this.mStack.mark();
            appendShortInteger(charset);
            appendTextString(textString);
            int len = start.getLength();
            this.mStack.pop();
            appendValueLength((long) len);
            this.mStack.copy();
        }
    }

    /* access modifiers changed from: protected */
    public void appendUintvarInteger(long value) {
        long max = 127;
        int i = 0;
        while (i < 5 && value >= max) {
            max = (max << 7) | 127;
            i++;
        }
        while (i > 0) {
            append((int) ((128 | ((value >>> (i * 7)) & 127)) & 255));
            i--;
        }
        append((int) (value & 127));
    }

    /* access modifiers changed from: protected */
    public void appendDateValue(long date) {
        appendLongInteger(date);
    }

    /* access modifiers changed from: protected */
    public void appendValueLength(long value) {
        if (value < 31) {
            appendShortLength((int) value);
            return;
        }
        append(31);
        appendUintvarInteger(value);
    }

    /* access modifiers changed from: protected */
    public void appendQuotedString(byte[] text) {
        append(34);
        arraycopy(text, 0, text.length);
        append(0);
    }

    /* access modifiers changed from: protected */
    public void appendQuotedString(String str) {
        appendQuotedString(str.getBytes());
    }

    private EncodedStringValue appendAddressType(EncodedStringValue address) {
        try {
            int addressType = checkAddressType(address.getString());
            EncodedStringValue temp = EncodedStringValue.copy(address);
            if (1 == addressType) {
                temp.appendTextString(STRING_PHONE_NUMBER_ADDRESS_TYPE.getBytes());
            } else if (3 == addressType) {
                temp.appendTextString(STRING_IPV4_ADDRESS_TYPE.getBytes());
            } else if (4 == addressType) {
                temp.appendTextString(STRING_IPV6_ADDRESS_TYPE.getBytes());
            }
            return temp;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public int appendHeader(int field) {
        switch (field) {
            case 129:
            case 130:
            case 151:
                EncodedStringValue[] addr = this.mPduHeader.getEncodedStringValues(field);
                if (addr == null) {
                    return 2;
                }
                for (EncodedStringValue encodedStringValue : addr) {
                    EncodedStringValue temp = appendAddressType(encodedStringValue);
                    if (temp == null) {
                        return 1;
                    }
                    appendOctet(field);
                    appendEncodedString(temp);
                }
                return 0;
            case 131:
            case 132:
            case 135:
            case 140:
            case 142:
            case 146:
            case 147:
            case 148:
            default:
                return 3;
            case 133:
                long date = this.mPduHeader.getLongInteger(field);
                if (-1 == date) {
                    return 2;
                }
                appendOctet(field);
                appendDateValue(date);
                return 0;
            case 134:
            case 143:
            case 144:
            case 145:
            case 149:
            case 153:
            case 155:
                int octet = this.mPduHeader.getOctet(field);
                if (octet == 0) {
                    return 2;
                }
                appendOctet(field);
                appendOctet(octet);
                return 0;
            case 136:
                long expiry = this.mPduHeader.getLongInteger(field);
                if (-1 == expiry) {
                    return 2;
                }
                appendOctet(field);
                this.mStack.newbuf();
                PositionMarker expiryStart = this.mStack.mark();
                append(129);
                appendLongInteger(expiry);
                int expiryLength = expiryStart.getLength();
                this.mStack.pop();
                appendValueLength((long) expiryLength);
                this.mStack.copy();
                return 0;
            case 137:
                appendOctet(field);
                EncodedStringValue from = this.mPduHeader.getEncodedStringValue(field);
                if (from == null || TextUtils.isEmpty(from.getString()) || new String(from.getTextString()).equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                    append(1);
                    append(129);
                    return 0;
                }
                this.mStack.newbuf();
                PositionMarker fstart = this.mStack.mark();
                append(128);
                EncodedStringValue temp2 = appendAddressType(from);
                if (temp2 == null) {
                    return 1;
                }
                appendEncodedString(temp2);
                int flen = fstart.getLength();
                this.mStack.pop();
                appendValueLength((long) flen);
                this.mStack.copy();
                return 0;
            case 138:
                byte[] messageClass = this.mPduHeader.getTextString(field);
                if (messageClass == null) {
                    return 2;
                }
                appendOctet(field);
                if (Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_ADVERTISEMENT_STR.getBytes())) {
                    appendOctet(129);
                    return 0;
                } else if (Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_AUTO_STR.getBytes())) {
                    appendOctet(131);
                    return 0;
                } else if (Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes())) {
                    appendOctet(128);
                    return 0;
                } else if (Arrays.equals(messageClass, PduHeaders.MESSAGE_CLASS_INFORMATIONAL_STR.getBytes())) {
                    appendOctet(130);
                    return 0;
                } else {
                    appendTextString(messageClass);
                    return 0;
                }
            case 139:
            case 152:
                byte[] textString = this.mPduHeader.getTextString(field);
                if (textString == null) {
                    return 2;
                }
                appendOctet(field);
                appendTextString(textString);
                return 0;
            case 141:
                appendOctet(field);
                int version = this.mPduHeader.getOctet(field);
                if (version == 0) {
                    appendShortInteger(18);
                    return 0;
                }
                appendShortInteger(version);
                return 0;
            case 150:
            case 154:
                EncodedStringValue enString = this.mPduHeader.getEncodedStringValue(field);
                if (enString == null) {
                    return 2;
                }
                appendOctet(field);
                appendEncodedString(enString);
                return 0;
        }
    }

    /* access modifiers changed from: protected */
    public int makeReadRecInd() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(135);
        if (appendHeader(141) != 0 || appendHeader(139) != 0 || appendHeader(151) != 0 || appendHeader(137) != 0) {
            return 1;
        }
        appendHeader(133);
        if (appendHeader(155) != 0) {
            return 1;
        }
        return 0;
    }

    private int makeNotifyResp() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(131);
        if (appendHeader(152) == 0 && appendHeader(141) == 0 && appendHeader(149) == 0) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public int makeAckInd() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(133);
        if (appendHeader(152) != 0 || appendHeader(141) != 0) {
            return 1;
        }
        appendHeader(145);
        return 0;
    }

    private int makeSendRetrievePdu(int type) {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(type);
        appendOctet(152);
        byte[] trid = this.mPduHeader.getTextString(152);
        if (trid != null) {
            appendTextString(trid);
            if (appendHeader(141) != 0) {
                return 1;
            }
            appendHeader(133);
            if (appendHeader(137) != 0) {
                return 1;
            }
            boolean recipient = false;
            if (appendHeader(151) != 1) {
                recipient = true;
            }
            if (appendHeader(130) != 1) {
                recipient = true;
            }
            if (appendHeader(129) != 1) {
                recipient = true;
            }
            if (!recipient) {
                return 1;
            }
            appendHeader(150);
            appendHeader(138);
            appendHeader(136);
            appendHeader(143);
            appendHeader(134);
            appendHeader(144);
            if (type == 132) {
                appendHeader(153);
                appendHeader(154);
            }
            appendOctet(132);
            return makeMessageBody(type);
        }
        throw new IllegalArgumentException("Transaction-ID is null.");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x02bd A[SYNTHETIC, Splitter:B:132:0x02bd] */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x02d0 A[SYNTHETIC, Splitter:B:140:0x02d0] */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x02e4 A[SYNTHETIC, Splitter:B:147:0x02e4] */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02f8 A[SYNTHETIC, Splitter:B:154:0x02f8] */
    /* JADX WARNING: Removed duplicated region for block: B:167:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:170:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:173:? A[RETURN, SYNTHETIC] */
    public int makeMessageBody(int type) {
        PduBody body;
        byte[] name;
        int dataLength;
        Throwable th;
        String str = ">";
        String str2 = "<";
        this.mStack.newbuf();
        PositionMarker ctStart = this.mStack.mark();
        String contentType = new String(this.mPduHeader.getTextString(132));
        Integer contentTypeIdentifier = mContentTypeMap.get(contentType);
        int i = 1;
        if (contentTypeIdentifier == null) {
            return 1;
        }
        appendShortInteger(contentTypeIdentifier.intValue());
        if (type == 132) {
            body = ((RetrieveConf) this.mPdu).getBody();
        } else {
            body = ((SendReq) this.mPdu).getBody();
        }
        if (body != null) {
            if (body.getPartsNum() != 0) {
                try {
                    PduPart part = body.getPart(0);
                    byte[] start = part.getContentId();
                    if (start != null) {
                        appendOctet(138);
                        if (60 == start[0] && 62 == start[start.length - 1]) {
                            appendTextString(start);
                        } else {
                            appendTextString(str2 + new String(start) + str);
                        }
                    }
                    appendOctet(137);
                    appendTextString(part.getContentType());
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                int ctLength = ctStart.getLength();
                this.mStack.pop();
                appendValueLength((long) ctLength);
                this.mStack.copy();
                int partNum = body.getPartsNum();
                appendUintvarInteger((long) partNum);
                int i2 = 0;
                while (i2 < partNum) {
                    PduPart part2 = body.getPart(i2);
                    this.mStack.newbuf();
                    PositionMarker attachment = this.mStack.mark();
                    this.mStack.newbuf();
                    PositionMarker contentTypeBegin = this.mStack.mark();
                    byte[] partContentType = part2.getContentType();
                    if (partContentType == null) {
                        return i;
                    }
                    Integer partContentTypeIdentifier = mContentTypeMap.get(new String(partContentType));
                    if (partContentTypeIdentifier == null) {
                        appendTextString(partContentType);
                    } else {
                        appendShortInteger(partContentTypeIdentifier.intValue());
                    }
                    byte[] name2 = part2.getName();
                    if (name2 == null) {
                        byte[] name3 = part2.getFilename();
                        if (name3 == null) {
                            byte[] name4 = part2.getContentLocation();
                            if (name4 == null) {
                                return i;
                            }
                            name = name4;
                        } else {
                            name = name3;
                        }
                    } else {
                        name = name2;
                    }
                    appendOctet(133);
                    appendTextString(name);
                    int charset = part2.getCharset();
                    if (charset != 0) {
                        appendOctet(129);
                        appendShortInteger(charset);
                    }
                    int contentTypeLength = contentTypeBegin.getLength();
                    this.mStack.pop();
                    appendValueLength((long) contentTypeLength);
                    this.mStack.copy();
                    byte[] contentId = part2.getContentId();
                    if (contentId != null) {
                        appendOctet(192);
                        if (60 == contentId[0]) {
                            if (62 == contentId[contentId.length - 1]) {
                                appendQuotedString(contentId);
                            }
                        }
                        appendQuotedString(str2 + new String(contentId) + str);
                    }
                    byte[] contentLocation = part2.getContentLocation();
                    if (contentLocation != null) {
                        appendOctet(142);
                        appendTextString(contentLocation);
                    }
                    int headerLength = attachment.getLength();
                    int dataLength2 = 0;
                    byte[] partData = part2.getData();
                    if (partData != null) {
                        arraycopy(partData, 0, partData.length);
                        dataLength = partData.length;
                    } else {
                        InputStream cr = null;
                        try {
                            byte[] buffer = new byte[PDU_COMPOSER_BLOCK_SIZE];
                            try {
                            } catch (FileNotFoundException e2) {
                                e = e2;
                                if (cr != null) {
                                }
                            } catch (IOException e3) {
                                e = e3;
                                if (cr != null) {
                                }
                            } catch (RuntimeException e4) {
                                e = e4;
                                if (cr != null) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (cr != null) {
                                }
                                throw th;
                            }
                            try {
                                cr = this.mResolver.openInputStream(part2.getDataUri());
                                while (true) {
                                    try {
                                        int len = cr.read(buffer);
                                        if (len == -1) {
                                            break;
                                        }
                                        try {
                                            try {
                                                this.mMessage.write(buffer, 0, len);
                                                this.mPosition += len;
                                                dataLength2 += len;
                                                contentLocation = contentLocation;
                                                contentId = contentId;
                                            } catch (FileNotFoundException e5) {
                                                e = e5;
                                                if (cr != null) {
                                                }
                                            } catch (IOException e6) {
                                                e = e6;
                                                if (cr != null) {
                                                }
                                            } catch (RuntimeException e7) {
                                                e = e7;
                                                if (cr != null) {
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                if (cr != null) {
                                                }
                                                throw th;
                                            }
                                        } catch (FileNotFoundException e8) {
                                            e = e8;
                                            if (cr != null) {
                                            }
                                        } catch (IOException e9) {
                                            e = e9;
                                            if (cr != null) {
                                            }
                                        } catch (RuntimeException e10) {
                                            e = e10;
                                            if (cr != null) {
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            if (cr != null) {
                                            }
                                            throw th;
                                        }
                                    } catch (FileNotFoundException e11) {
                                        e = e11;
                                        if (cr != null) {
                                        }
                                    } catch (IOException e12) {
                                        e = e12;
                                        if (cr != null) {
                                        }
                                    } catch (RuntimeException e13) {
                                        e = e13;
                                        if (cr != null) {
                                        }
                                    } catch (Throwable th5) {
                                        th = th5;
                                        if (cr != null) {
                                        }
                                        throw th;
                                    }
                                }
                                try {
                                    cr.close();
                                } catch (IOException e14) {
                                }
                                dataLength = dataLength2;
                            } catch (FileNotFoundException e15) {
                                e = e15;
                                cr = null;
                                if (cr != null) {
                                    return 1;
                                }
                                try {
                                    cr.close();
                                    return 1;
                                } catch (IOException e16) {
                                    return 1;
                                }
                            } catch (IOException e17) {
                                e = e17;
                                cr = null;
                                if (cr != null) {
                                    return 1;
                                }
                                try {
                                    cr.close();
                                    return 1;
                                } catch (IOException e18) {
                                    return 1;
                                }
                            } catch (RuntimeException e19) {
                                e = e19;
                                cr = null;
                                if (cr != null) {
                                    return 1;
                                }
                                try {
                                    cr.close();
                                    return 1;
                                } catch (IOException e20) {
                                    return 1;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                cr = null;
                                if (cr != null) {
                                    try {
                                        cr.close();
                                    } catch (IOException e21) {
                                    }
                                }
                                throw th;
                            }
                        } catch (FileNotFoundException e22) {
                            e = e22;
                            if (cr != null) {
                            }
                        } catch (IOException e23) {
                            e = e23;
                            if (cr != null) {
                            }
                        } catch (RuntimeException e24) {
                            e = e24;
                            if (cr != null) {
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            if (cr != null) {
                            }
                            throw th;
                        }
                    }
                    if (dataLength == attachment.getLength() - headerLength) {
                        this.mStack.pop();
                        appendUintvarInteger((long) headerLength);
                        appendUintvarInteger((long) dataLength);
                        this.mStack.copy();
                        i2++;
                        ctStart = ctStart;
                        contentType = contentType;
                        body = body;
                        str = str;
                        str2 = str2;
                        i = 1;
                    } else {
                        throw new RuntimeException("BUG: Length sanity check failed");
                    }
                }
                return 0;
            }
        }
        appendUintvarInteger(0);
        this.mStack.pop();
        this.mStack.copy();
        return 0;
    }

    private static class LengthRecordNode {
        ByteArrayOutputStream currentMessage;
        public int currentPosition;
        public LengthRecordNode next;

        private LengthRecordNode() {
            this.currentMessage = null;
            this.currentPosition = 0;
            this.next = null;
        }
    }

    protected class PositionMarker {
        protected int c_pos;
        protected int currentStackSize;

        protected PositionMarker() {
        }

        public int getLength() {
            if (this.currentStackSize == PduComposer.this.mStack.stackSize) {
                return PduComposer.this.mPosition - this.c_pos;
            }
            throw new RuntimeException("BUG: Invalid call to getLength()");
        }
    }

    protected class BufferStack {
        protected LengthRecordNode stack = null;
        int stackSize = 0;
        protected LengthRecordNode toCopy = null;

        protected BufferStack() {
        }

        public void newbuf() {
            if (this.toCopy == null) {
                LengthRecordNode temp = new LengthRecordNode();
                temp.currentMessage = PduComposer.this.mMessage;
                temp.currentPosition = PduComposer.this.mPosition;
                temp.next = this.stack;
                this.stack = temp;
                this.stackSize++;
                PduComposer.this.mMessage = new ByteArrayOutputStream();
                PduComposer.this.mPosition = 0;
                return;
            }
            throw new RuntimeException("BUG: Invalid newbuf() before copy()");
        }

        public void pop() {
            ByteArrayOutputStream currentMessage = PduComposer.this.mMessage;
            int currentPosition = PduComposer.this.mPosition;
            PduComposer.this.mMessage = this.stack.currentMessage;
            PduComposer.this.mPosition = this.stack.currentPosition;
            LengthRecordNode lengthRecordNode = this.stack;
            this.toCopy = lengthRecordNode;
            this.stack = lengthRecordNode.next;
            this.stackSize--;
            LengthRecordNode lengthRecordNode2 = this.toCopy;
            lengthRecordNode2.currentMessage = currentMessage;
            lengthRecordNode2.currentPosition = currentPosition;
        }

        public void copy() {
            PduComposer.this.arraycopy(this.toCopy.currentMessage.toByteArray(), 0, this.toCopy.currentPosition);
            this.toCopy = null;
        }

        public PositionMarker mark() {
            PositionMarker m = new PositionMarker();
            m.c_pos = PduComposer.this.mPosition;
            m.currentStackSize = this.stackSize;
            return m;
        }
    }

    protected static int checkAddressType(String address) {
        if (address == null) {
            return 5;
        }
        if (address.matches(REGEXP_IPV4_ADDRESS_TYPE)) {
            return 3;
        }
        if (address.matches(REGEXP_PHONE_NUMBER_ADDRESS_TYPE)) {
            return 1;
        }
        if (address.matches(REGEXP_EMAIL_ADDRESS_TYPE)) {
            return 2;
        }
        if (address.matches(REGEXP_IPV6_ADDRESS_TYPE)) {
            return 4;
        }
        return 5;
    }
}
