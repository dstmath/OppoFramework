package com.mediatek.android.mms.pdu;

import android.util.Log;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu.AcknowledgeInd;
import com.google.android.mms.pdu.DeliveryInd;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.NotifyRespInd;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduContentTypes;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.ReadOrigInd;
import com.google.android.mms.pdu.ReadRecInd;
import com.google.android.mms.pdu.RetrieveConf;
import com.google.android.mms.pdu.SendReq;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

public class MtkPduParser extends PduParser {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String LOG_TAG = "MtkPduParser";
    protected static final int UNSIGNED_INT_LIMIT = 2;

    public MtkPduParser(byte[] pduDataStream, boolean parseContentDisposition) {
        super(pduDataStream, parseContentDisposition);
    }

    public GenericPdu parse() {
        if (this.mPduDataStream == null) {
            Log.i(LOG_TAG, "Input parse stream is null");
            return null;
        }
        this.mHeaders = parseHeaders(this.mPduDataStream);
        if (this.mHeaders == null) {
            Log.i(LOG_TAG, "Parse PduHeader Failed");
            return null;
        }
        int messageType = this.mHeaders.getOctet(140);
        if (!checkMandatoryHeader(this.mHeaders)) {
            Log.d(LOG_TAG, "check mandatory headers failed!");
            return null;
        }
        this.mPduDataStream.mark(1);
        int count = parseUnsignedInt(this.mPduDataStream);
        this.mPduDataStream.reset();
        if (132 == messageType && count >= 2) {
            byte[] contentType = this.mHeaders.getTextString(132);
            if (contentType == null) {
                Log.i(LOG_TAG, "Parse MESSAGE_TYPE_RETRIEVE_CONF Failed: content Type is null _0");
                return null;
            }
            String contentTypeStr = new String(contentType).toLowerCase();
            if (!contentTypeStr.equals("application/vnd.wap.multipart.mixed") && !contentTypeStr.equals("application/vnd.wap.multipart.related") && !contentTypeStr.equals("application/vnd.wap.multipart.alternative") && contentTypeStr.equals("text/plain")) {
                Log.i(LOG_TAG, "Content Type is text/plain");
                PduPart theOnlyPart = new PduPart();
                theOnlyPart.setContentType(contentType);
                theOnlyPart.setContentLocation(Long.toOctalString(System.currentTimeMillis()).getBytes());
                theOnlyPart.setContentId("<part1>".getBytes());
                this.mPduDataStream.mark(1);
                int partDataLen = 0;
                while (this.mPduDataStream.read() != -1) {
                    partDataLen++;
                }
                byte[] partData = new byte[partDataLen];
                Log.i(LOG_TAG, "got part length: " + partDataLen);
                this.mPduDataStream.reset();
                this.mPduDataStream.read(partData, 0, partDataLen);
                String showData = new String(partData);
                Log.i(LOG_TAG, "show data: " + showData);
                theOnlyPart.setData(partData);
                Log.i(LOG_TAG, "setData finish");
                PduBody onlyPartBody = new PduBody();
                onlyPartBody.addPart(theOnlyPart);
                RetrieveConf retrieveConf = null;
                try {
                    retrieveConf = new RetrieveConf(this.mHeaders, onlyPartBody);
                } catch (Exception e) {
                    Log.i(LOG_TAG, "new RetrieveConf has exception");
                }
                if (retrieveConf == null) {
                    Log.i(LOG_TAG, "retrieveConf is null");
                }
                return retrieveConf;
            }
        }
        if (128 == messageType || 132 == messageType) {
            this.mBody = parseParts(this.mPduDataStream);
            if (this.mBody == null) {
                Log.i(LOG_TAG, "Parse parts Failed");
                return null;
            }
        }
        switch (messageType) {
            case 128:
                return new SendReq(this.mHeaders, this.mBody);
            case 129:
                return new MtkSendConf(this.mHeaders);
            case 130:
                return new NotificationInd(this.mHeaders);
            case 131:
                return new NotifyRespInd(this.mHeaders);
            case 132:
                RetrieveConf retrieveConf2 = new RetrieveConf(this.mHeaders, this.mBody);
                byte[] contentType2 = retrieveConf2.getContentType();
                if (contentType2 == null) {
                    Log.i(LOG_TAG, "Parse MESSAGE_TYPE_RETRIEVE_CONF Failed: content Type is null");
                    return null;
                }
                String ctTypeStr = new String(contentType2).toLowerCase();
                if (ctTypeStr.equals("application/vnd.wap.multipart.mixed") || ctTypeStr.equals("application/vnd.wap.multipart.related") || ctTypeStr.equals("application/vnd.wap.multipart.alternative") || ctTypeStr.equals("text/plain")) {
                    return retrieveConf2;
                }
                if (ctTypeStr.equals("application/vnd.wap.multipart.alternative")) {
                    PduPart firstPart = this.mBody.getPart(0);
                    this.mBody.removeAll();
                    this.mBody.addPart(0, firstPart);
                    return retrieveConf2;
                }
                Log.i(LOG_TAG, "Parse MESSAGE_TYPE_RETRIEVE_CONF Failed: content Type is null _2");
                return null;
            case 133:
                return new AcknowledgeInd(this.mHeaders);
            case 134:
                return new DeliveryInd(this.mHeaders);
            case 135:
                return new ReadRecInd(this.mHeaders);
            case 136:
                return new ReadOrigInd(this.mHeaders);
            default:
                Log.d(LOG_TAG, "Parser doesn't support this message type in this version!");
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public void parseContentTypeParams(ByteArrayInputStream pduDataStream, HashMap<Integer, Object> map, Integer length) {
        byte[] type;
        int startPos = pduDataStream.available();
        int lastLen = length.intValue();
        while (lastLen > 0) {
            int param = pduDataStream.read();
            lastLen--;
            if (param != 129) {
                if (param != 131) {
                    if (param != 143) {
                        if (param != 133) {
                            if (param != 134) {
                                if (param != 137) {
                                    if (param != 138) {
                                        if (param != 140) {
                                            if (param != 141) {
                                                switch (param) {
                                                    case 151:
                                                        break;
                                                    case 152:
                                                        break;
                                                    case 153:
                                                        break;
                                                    default:
                                                        switch (param) {
                                                            case 155:
                                                                break;
                                                            case 156:
                                                                break;
                                                            case 157:
                                                                break;
                                                            default:
                                                                if (-1 != skipWapValue(pduDataStream, lastLen)) {
                                                                    lastLen = 0;
                                                                    break;
                                                                } else {
                                                                    Log.e(LOG_TAG, "Corrupt Content-Type");
                                                                    break;
                                                                }
                                                        }
                                                }
                                            }
                                            byte[] domain = parseWapString(pduDataStream, 0);
                                            if (!(domain == null || map == null)) {
                                                map.put(156, domain);
                                            }
                                            lastLen = length.intValue() - (startPos - pduDataStream.available());
                                        }
                                        byte[] comment = parseWapString(pduDataStream, 0);
                                        if (!(comment == null || map == null)) {
                                            map.put(155, comment);
                                        }
                                        lastLen = length.intValue() - (startPos - pduDataStream.available());
                                    }
                                    byte[] start = parseWapString(pduDataStream, 0);
                                    if (!(start == null || map == null)) {
                                        map.put(153, start);
                                    }
                                    lastLen = length.intValue() - (startPos - pduDataStream.available());
                                }
                            }
                            byte[] fileName = parseWapString(pduDataStream, 0);
                            if (!(fileName == null || map == null)) {
                                map.put(152, fileName);
                            }
                            lastLen = length.intValue() - (startPos - pduDataStream.available());
                        }
                        byte[] name = parseWapString(pduDataStream, 0);
                        if (!(name == null || map == null)) {
                            map.put(151, name);
                        }
                        lastLen = length.intValue() - (startPos - pduDataStream.available());
                    }
                    byte[] path = parseWapString(pduDataStream, 0);
                    if (!(path == null || map == null)) {
                        map.put(157, path);
                    }
                    lastLen = length.intValue() - (startPos - pduDataStream.available());
                }
                pduDataStream.mark(1);
                int first = extractByteValue(pduDataStream);
                pduDataStream.reset();
                if (first > 127) {
                    int index = parseShortInteger(pduDataStream);
                    if (!(index >= PduContentTypes.contentTypes.length || (type = PduContentTypes.contentTypes[index].getBytes()) == null || map == null)) {
                        map.put(131, type);
                    }
                } else {
                    byte[] type2 = parseWapString(pduDataStream, 0);
                    if (!(type2 == null || map == null)) {
                        map.put(131, type2);
                    }
                }
                lastLen = length.intValue() - (startPos - pduDataStream.available());
            } else {
                pduDataStream.mark(1);
                int firstValue = extractByteValue(pduDataStream);
                pduDataStream.reset();
                if ((firstValue <= 32 || firstValue >= 127) && firstValue != 0) {
                    int charset = (int) parseIntegerValue(pduDataStream);
                    if (map != null) {
                        Log.i(LOG_TAG, "Parse Well-known-charset: charset");
                        map.put(129, Integer.valueOf(charset));
                    }
                } else {
                    byte[] charsetStr = parseWapString(pduDataStream, 0);
                    try {
                        int charsetInt = MtkCharacterSets.getMibEnumValue(new String(charsetStr));
                        Log.i(LOG_TAG, "Parse CharacterSets: charsetStr");
                        if (map != null) {
                            map.put(129, Integer.valueOf(charsetInt));
                        }
                    } catch (UnsupportedEncodingException e) {
                        Log.e(LOG_TAG, Arrays.toString(charsetStr), e);
                        if (map != null) {
                            map.put(129, 0);
                        }
                    }
                }
                lastLen = length.intValue() - (startPos - pduDataStream.available());
            }
        }
        if (lastLen != 0) {
            Log.e(LOG_TAG, "Corrupt Content-Type");
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0321  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x03ac  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0111  */
    public PduHeaders parseHeaders(ByteArrayInputStream pduDataStream) {
        String str;
        EncodedStringValue from;
        byte[] address;
        String str2;
        if (pduDataStream == null) {
            return null;
        }
        MtkPduHeaders headers = new MtkPduHeaders();
        boolean keepParsing = true;
        while (keepParsing && pduDataStream.available() > 0) {
            pduDataStream.mark(1);
            int headerField = extractByteValue(pduDataStream);
            if (headerField < 32 || headerField > 127) {
                if (headerField != 175) {
                    if (headerField != 201) {
                        switch (headerField) {
                            case 129:
                            case 130:
                            case 151:
                                EncodedStringValue value = parseEncodedStringValue(pduDataStream);
                                if (value == null) {
                                    break;
                                } else {
                                    byte[] address2 = value.getTextString();
                                    if (address2 != null) {
                                        String str3 = new String(address2);
                                        int endIndex = str3.indexOf("/");
                                        if (endIndex > 0) {
                                            str = str3.substring(0, endIndex);
                                        } else {
                                            str = str3;
                                        }
                                        try {
                                            value.setTextString(str.getBytes());
                                        } catch (NullPointerException e) {
                                            log("null pointer error!");
                                            return null;
                                        }
                                    }
                                    try {
                                        headers.appendEncodedStringValue(value, headerField);
                                        break;
                                    } catch (NullPointerException e2) {
                                        log("null pointer error!");
                                        break;
                                    } catch (RuntimeException e3) {
                                        log(headerField + "is not Encoded-String-Value header field!");
                                        return null;
                                    }
                                }
                            case 131:
                            case 139:
                            case 152:
                            case 158:
                                byte[] value2 = parseWapString(pduDataStream, 0);
                                if (value2 == null) {
                                    break;
                                } else {
                                    try {
                                        headers.setTextString(value2, headerField);
                                        break;
                                    } catch (NullPointerException e4) {
                                        log("null pointer error!");
                                        break;
                                    } catch (RuntimeException e5) {
                                        log(headerField + "is not Text-String header field!");
                                        return null;
                                    }
                                }
                            case 132:
                                HashMap<Integer, Object> map = new HashMap<>();
                                byte[] contentType = parseContentType(pduDataStream, map);
                                if (contentType != null) {
                                    try {
                                        headers.setTextString(contentType, 132);
                                    } catch (NullPointerException e6) {
                                        log("null pointer error!");
                                    } catch (RuntimeException e7) {
                                        log(headerField + "is not Text-String header field!");
                                        return null;
                                    }
                                }
                                mStartParam = (byte[]) map.get(153);
                                mTypeParam = (byte[]) map.get(131);
                                keepParsing = false;
                                break;
                            case 133:
                            case 142:
                            case 159:
                                break;
                            case 134:
                            case 143:
                            case 144:
                            case 145:
                            case MtkPduPart.P_DATE:
                            case 148:
                            case 149:
                            case 153:
                            case 155:
                            case 156:
                            case 162:
                            case 163:
                            case 165:
                            case MtkPduPart.P_TRANSFER_ENCODING:
                                int value3 = extractByteValue(pduDataStream);
                                try {
                                    headers.setOctet(value3, headerField);
                                    break;
                                } catch (InvalidHeaderValueException e8) {
                                    log("Set invalid Octet value: " + value3 + " into the header filed: " + headerField);
                                    return null;
                                } catch (RuntimeException e9) {
                                    log(headerField + "is not Octet header field!");
                                    return null;
                                }
                            case 135:
                            case 136:
                            case 157:
                                parseValueLength(pduDataStream);
                                int token = extractByteValue(pduDataStream);
                                try {
                                    long timeValue = parseLongInteger(pduDataStream);
                                    if (129 == token) {
                                        timeValue += System.currentTimeMillis() / 1000;
                                    }
                                    try {
                                        headers.setLongInteger(timeValue, headerField);
                                        break;
                                    } catch (RuntimeException e10) {
                                        log(headerField + "is not Long-Integer header field!");
                                        return null;
                                    }
                                } catch (RuntimeException e11) {
                                    log(headerField + "is not Long-Integer header field!");
                                    return null;
                                }
                            case MtkPduHeaders.STATE_SKIP_RETRYING:
                                parseValueLength(pduDataStream);
                                if (128 == extractByteValue(pduDataStream)) {
                                    from = parseEncodedStringValue(pduDataStream);
                                    if (!(from == null || (address = from.getTextString()) == null)) {
                                        String str4 = new String(address);
                                        int endIndex2 = str4.indexOf("/");
                                        if (endIndex2 > 0) {
                                            str2 = str4.substring(0, endIndex2);
                                        } else {
                                            str2 = str4;
                                        }
                                        try {
                                            from.setTextString(str2.getBytes());
                                        } catch (NullPointerException e12) {
                                            log("null pointer error!");
                                            return null;
                                        }
                                    }
                                } else {
                                    try {
                                        from = new EncodedStringValue("insert-address-token".getBytes());
                                    } catch (NullPointerException e13) {
                                        log(headerField + "is not Encoded-String-Value header field!");
                                        return null;
                                    }
                                }
                                try {
                                    headers.setEncodedStringValue(from, MtkPduHeaders.STATE_SKIP_RETRYING);
                                    break;
                                } catch (NullPointerException e14) {
                                    log("null pointer error!");
                                    break;
                                } catch (RuntimeException e15) {
                                    log(headerField + "is not Encoded-String-Value header field!");
                                    return null;
                                }
                            case 138:
                                pduDataStream.mark(1);
                                int messageClass = extractByteValue(pduDataStream);
                                if (messageClass >= 128) {
                                    if (128 != messageClass) {
                                        if (129 != messageClass) {
                                            if (130 != messageClass) {
                                                if (131 != messageClass) {
                                                    break;
                                                } else {
                                                    headers.setTextString("auto".getBytes(), 138);
                                                    break;
                                                }
                                            } else {
                                                headers.setTextString("informational".getBytes(), 138);
                                                break;
                                            }
                                        } else {
                                            headers.setTextString("advertisement".getBytes(), 138);
                                            break;
                                        }
                                    } else {
                                        try {
                                            headers.setTextString("personal".getBytes(), 138);
                                            break;
                                        } catch (NullPointerException e16) {
                                            log("null pointer error!");
                                            break;
                                        } catch (RuntimeException e17) {
                                            log(headerField + "is not Text-String header field!");
                                            return null;
                                        }
                                    }
                                } else {
                                    pduDataStream.reset();
                                    byte[] messageClassString = parseWapString(pduDataStream, 0);
                                    if (messageClassString == null) {
                                        break;
                                    } else {
                                        try {
                                            headers.setTextString(messageClassString, 138);
                                            break;
                                        } catch (NullPointerException e18) {
                                            log("null pointer error!");
                                            break;
                                        } catch (RuntimeException e19) {
                                            log(headerField + "is not Text-String header field!");
                                            return null;
                                        }
                                    }
                                }
                            case 140:
                                int messageType = extractByteValue(pduDataStream);
                                Log.d(LOG_TAG, "parseHeaders: messageType: " + messageType);
                                switch (messageType) {
                                    case MtkPduHeaders.STATE_SKIP_RETRYING:
                                    case 138:
                                    case 139:
                                    case 140:
                                    case 141:
                                    case 142:
                                    case 143:
                                    case 144:
                                    case 145:
                                    case MtkPduPart.P_DATE:
                                    case 147:
                                    case 148:
                                    case 149:
                                    case 150:
                                    case 151:
                                        Log.i(LOG_TAG, "PduParser: parseHeaders: We don't support these kind of messages now.");
                                        return null;
                                    default:
                                        try {
                                            headers.setOctet(messageType, headerField);
                                            continue;
                                        } catch (InvalidHeaderValueException e20) {
                                            log("Set invalid Octet value: " + messageType + " into the header filed: " + headerField);
                                            return null;
                                        } catch (RuntimeException e21) {
                                            log(headerField + "is not Octet header field!");
                                            return null;
                                        }
                                }
                            case 141:
                                int version = parseShortInteger(pduDataStream);
                                try {
                                    headers.setOctet(version, 141);
                                    break;
                                } catch (InvalidHeaderValueException e22) {
                                    log("Set invalid Octet value: " + version + " into the header filed: " + headerField);
                                    return null;
                                } catch (RuntimeException e23) {
                                    log(headerField + "is not Octet header field!");
                                    return null;
                                }
                            case 147:
                            case 150:
                            case 154:
                            case 166:
                                EncodedStringValue value4 = parseEncodedStringValue(pduDataStream);
                                if (value4 == null) {
                                    break;
                                } else {
                                    try {
                                        headers.setEncodedStringValue(value4, headerField);
                                        break;
                                    } catch (NullPointerException e24) {
                                        log("null pointer error!");
                                        break;
                                    } catch (RuntimeException e25) {
                                        log(headerField + "is not Encoded-String-Value header field!");
                                        return null;
                                    }
                                }
                            case 160:
                                parseValueLength(pduDataStream);
                                try {
                                    parseIntegerValue(pduDataStream);
                                    EncodedStringValue previouslySentBy = parseEncodedStringValue(pduDataStream);
                                    if (previouslySentBy == null) {
                                        break;
                                    } else {
                                        try {
                                            headers.setEncodedStringValue(previouslySentBy, 160);
                                            break;
                                        } catch (NullPointerException e26) {
                                            log("null pointer error!");
                                            break;
                                        } catch (RuntimeException e27) {
                                            log(headerField + "is not Encoded-String-Value header field!");
                                            return null;
                                        }
                                    }
                                } catch (RuntimeException e28) {
                                    log(headerField + " is not Integer-Value");
                                    return null;
                                }
                            case 161:
                                parseValueLength(pduDataStream);
                                try {
                                    parseIntegerValue(pduDataStream);
                                    try {
                                        headers.setLongInteger(parseLongInteger(pduDataStream), 161);
                                        break;
                                    } catch (RuntimeException e29) {
                                        log(headerField + "is not Long-Integer header field!");
                                        return null;
                                    }
                                } catch (RuntimeException e30) {
                                    log(headerField + " is not Integer-Value");
                                    return null;
                                }
                            case 164:
                                parseValueLength(pduDataStream);
                                extractByteValue(pduDataStream);
                                parseEncodedStringValue(pduDataStream);
                                break;
                            default:
                                switch (headerField) {
                                    case 169:
                                    case 171:
                                        break;
                                    case 170:
                                    case 172:
                                        parseValueLength(pduDataStream);
                                        extractByteValue(pduDataStream);
                                        try {
                                            parseIntegerValue(pduDataStream);
                                            break;
                                        } catch (RuntimeException e31) {
                                            log(headerField + " is not Integer-Value");
                                            return null;
                                        }
                                    case 173:
                                        break;
                                    default:
                                        switch (headerField) {
                                            case 177:
                                            case 180:
                                            case 186:
                                            case 187:
                                            case 188:
                                            case 191:
                                                break;
                                            case 178:
                                                parseContentType(pduDataStream, null);
                                                break;
                                            case 179:
                                                break;
                                            case 181:
                                            case 182:
                                                break;
                                            case 183:
                                            case 184:
                                            case 185:
                                            case 189:
                                            case 190:
                                                break;
                                            default:
                                                log("Unknown header");
                                                break;
                                        }
                                }
                        }
                    }
                    Log.d(LOG_TAG, "parseHeaders " + headerField);
                    try {
                        long value5 = parseLongInteger(pduDataStream);
                        Log.d(LOG_TAG, "value = " + value5);
                        if (headerField == 133) {
                            headers.setLongInteger(value5, 201);
                            value5 = System.currentTimeMillis() / 1000;
                        }
                        headers.setLongInteger(value5, headerField);
                    } catch (RuntimeException e32) {
                        log(headerField + "is not Long-Integer header field!");
                        return null;
                    }
                }
                try {
                    headers.setLongInteger(parseIntegerValue(pduDataStream), headerField);
                } catch (RuntimeException e33) {
                    log(headerField + "is not Long-Integer header field!");
                    return null;
                }
            } else {
                pduDataStream.reset();
                parseWapString(pduDataStream, 0);
            }
        }
        return headers;
    }

    /* access modifiers changed from: protected */
    public boolean parsePartHeaders(ByteArrayInputStream pduDataStream, PduPart part, int length) {
        int startPos = pduDataStream.available();
        int lastLen = length;
        while (lastLen > 0) {
            int header = pduDataStream.read();
            lastLen--;
            Log.v(LOG_TAG, "Part headers: " + header);
            if (header > 127) {
                if (header == 142) {
                    byte[] contentLocation = parseWapString(pduDataStream, 0);
                    if (contentLocation != null) {
                        part.setContentLocation(contentLocation);
                    }
                    lastLen = length - (startPos - pduDataStream.available());
                } else if (header != 167) {
                    if (header != 174) {
                        if (header == 192) {
                            byte[] contentId = parseWapString(pduDataStream, 1);
                            if (contentId != null) {
                                part.setContentId(contentId);
                            }
                            lastLen = length - (startPos - pduDataStream.available());
                        } else if (header != 197) {
                            if (-1 == skipWapValue(pduDataStream, lastLen)) {
                                Log.e(LOG_TAG, "Corrupt Part headers");
                                return false;
                            }
                            lastLen = 0;
                        }
                    }
                    if (this.mParseContentDisposition) {
                        int len = parseValueLength(pduDataStream);
                        pduDataStream.mark(1);
                        int thisStartPos = pduDataStream.available();
                        int value = pduDataStream.read();
                        if (value == 128) {
                            part.setContentDisposition(PduPart.DISPOSITION_FROM_DATA);
                        } else if (value == 129) {
                            part.setContentDisposition(PduPart.DISPOSITION_ATTACHMENT);
                        } else if (value == 130) {
                            part.setContentDisposition(PduPart.DISPOSITION_INLINE);
                        } else {
                            pduDataStream.reset();
                            part.setContentDisposition(parseWapString(pduDataStream, 0));
                        }
                        if (thisStartPos - pduDataStream.available() < len) {
                            if (pduDataStream.read() == 152) {
                                part.setFilename(parseWapString(pduDataStream, 0));
                            }
                            int thisEndPos = pduDataStream.available();
                            if (thisStartPos - thisEndPos < len) {
                                int last = len - (thisStartPos - thisEndPos);
                                pduDataStream.read(new byte[last], 0, last);
                            }
                        }
                        lastLen = length - (startPos - pduDataStream.available());
                    }
                } else {
                    byte[] transferEncoding = parseWapString(pduDataStream, 0);
                    if (transferEncoding != null) {
                        part.setContentTransferEncoding(transferEncoding);
                    }
                    lastLen = length - (startPos - pduDataStream.available());
                }
            } else if (header >= 32 && header <= 127) {
                byte[] tempHeader = parseWapString(pduDataStream, 0);
                byte[] tempValue = parseWapString(pduDataStream, 0);
                if (true == "Content-Transfer-Encoding".equalsIgnoreCase(new String(tempHeader))) {
                    part.setContentTransferEncoding(tempValue);
                }
                lastLen = length - (startPos - pduDataStream.available());
            } else if (-1 == skipWapValue(pduDataStream, lastLen)) {
                Log.e(LOG_TAG, "Corrupt Part headers");
                return false;
            } else {
                lastLen = 0;
            }
        }
        if (lastLen == 0) {
            return true;
        }
        Log.e(LOG_TAG, "Corrupt Part headers");
        return false;
    }
}
