package com.mediatek.android.mms.pdu;

import android.content.Context;
import android.util.Log;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduPart;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MtkPduComposer extends PduComposer {
    private static final String LOG_TAG = "MtkPduComposer";

    public MtkPduComposer(Context context, GenericPdu pdu) {
        super(context, pdu);
    }

    public byte[] make() {
        int type = this.mPdu.getMessageType();
        Log.d(LOG_TAG, "make, type = " + type);
        switch (type) {
            case 128:
            case 132:
                if (makeSendRetrievePdu(type) != 0) {
                    return null;
                }
                break;
            case 129:
            case 134:
            default:
                return null;
            case 130:
                if (makeNotifyIndEx() != 0) {
                    return null;
                }
                break;
            case 131:
                if (makeNotifyRespEx() != 0) {
                    return null;
                }
                break;
            case 133:
                if (makeAckInd() != 0) {
                    return null;
                }
                break;
            case 135:
                if (makeReadRecInd() != 0) {
                    return null;
                }
                break;
        }
        return this.mMessage.toByteArray();
    }

    private int makeSendReqPduEx() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(128);
        appendOctet(152);
        byte[] trid = this.mPduHeader.getTextString(152);
        if (trid != null) {
            appendTextString(trid);
            if (appendHeader(141) != 0) {
                return 1;
            }
            appendHeader(133);
            if (appendHeader(MtkPduHeaders.STATE_SKIP_RETRYING) != 0) {
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
            appendOctet(132);
            makeMessageBodyEx(2);
            return 0;
        }
        throw new IllegalArgumentException("Transaction-ID is null.");
    }

    private int makeNotifyIndEx() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(130);
        if (appendHeader(152) != 0 || appendHeader(141) != 0 || appendHeader(138) != 0) {
            return 1;
        }
        appendOctet(142);
        appendLongInteger(this.mPdu.getMessageSize());
        if (appendHeader(136) != 0) {
            return 1;
        }
        appendOctet(131);
        byte[] contentLocation = this.mPdu.getContentLocation();
        if (contentLocation != null) {
            Log.d(LOG_TAG, "makeNotifyIndEx contentLocation != null");
            appendTextString(contentLocation);
        } else {
            Log.d(LOG_TAG, "makeNotifyIndEx contentLocation  = null");
        }
        EncodedStringValue subject = this.mPdu.getSubject();
        if (subject != null) {
            Log.d(LOG_TAG, "makeNotifyIndEx subject != null");
            appendOctet(150);
            appendEncodedString(subject);
        } else {
            Log.d(LOG_TAG, "makeNotifyIndEx subject  = null");
        }
        appendHeader(133);
        if (appendHeader(MtkPduHeaders.STATE_SKIP_RETRYING) == 0 && appendHeader(149) == 0) {
            return 0;
        }
        return 1;
    }

    private int makeRetrievePduEx() {
        Log.d(LOG_TAG, "makeRetrievePduEx begin");
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(132);
        byte[] trid = this.mPduHeader.getTextString(152);
        if (trid == null) {
            Log.d(LOG_TAG, "Transaction ID is null");
        } else {
            appendOctet(152);
            appendTextString(trid);
        }
        if (appendHeader(141) != 0) {
            return 1;
        }
        appendHeader(133);
        if (appendHeader(MtkPduHeaders.STATE_SKIP_RETRYING) != 0) {
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
        appendOctet(132);
        makeMessageBodyEx(1);
        Log.d(LOG_TAG, "makeRetrievePduEx end");
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:138:0x02f6 A[SYNTHETIC, Splitter:B:138:0x02f6] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0309 A[SYNTHETIC, Splitter:B:146:0x0309] */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x031d A[SYNTHETIC, Splitter:B:153:0x031d] */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0331 A[SYNTHETIC, Splitter:B:160:0x0331] */
    /* JADX WARNING: Removed duplicated region for block: B:176:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:179:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:182:? A[RETURN, SYNTHETIC] */
    private int makeMessageBodyEx(int type) {
        PduBody body;
        PduComposer.PositionMarker ctStart;
        byte[] name;
        int dataLength;
        InputStream cr;
        FileNotFoundException e;
        IOException e2;
        RuntimeException e3;
        Throwable th;
        String str = ">";
        String str2 = "<";
        this.mStack.newbuf();
        PduComposer.PositionMarker ctStart2 = this.mStack.mark();
        String contentType = new String(this.mPduHeader.getTextString(132));
        Integer contentTypeIdentifier = (Integer) mContentTypeMap.get(contentType);
        int i = 1;
        if (contentTypeIdentifier == null) {
            return 1;
        }
        appendShortInteger(contentTypeIdentifier.intValue());
        if (type == 132) {
            body = this.mPdu.getBody();
        } else {
            body = this.mPdu.getBody();
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
                    appendOctet(MtkPduHeaders.STATE_SKIP_RETRYING);
                    appendTextString(part.getContentType());
                } catch (ArrayIndexOutOfBoundsException e4) {
                    e4.printStackTrace();
                }
                int ctLength = ctStart2.getLength();
                this.mStack.pop();
                appendValueLength((long) ctLength);
                this.mStack.copy();
                int partNum = body.getPartsNum();
                appendUintvarInteger((long) partNum);
                int i2 = 0;
                while (i2 < partNum) {
                    PduPart part2 = (MtkPduPart) body.getPart(i2);
                    this.mStack.newbuf();
                    PduComposer.PositionMarker attachment = this.mStack.mark();
                    this.mStack.newbuf();
                    PduComposer.PositionMarker contentTypeBegin = this.mStack.mark();
                    byte[] partContentType = part2.getContentType();
                    if (partContentType == null) {
                        return i;
                    }
                    Integer partContentTypeIdentifier = (Integer) mContentTypeMap.get(new String(partContentType));
                    if (partContentTypeIdentifier == null) {
                        appendTextString(partContentType);
                    } else {
                        appendShortInteger(partContentTypeIdentifier.intValue());
                    }
                    byte[] name2 = part2.getName();
                    if (name2 == null || name2.length == 0) {
                        byte[] name3 = part2.getFilename();
                        if (name3 == null || name3.length == 0) {
                            byte[] name4 = part2.getContentLocation();
                            if (name4 == null || name4.length == 0) {
                                byte[] name5 = part2.getContentId();
                                if (name5 == null || name5.length == 0) {
                                    return 1;
                                }
                                StringBuilder sb = new StringBuilder();
                                ctStart = ctStart2;
                                sb.append("makeMessageBodyEx name 1= ");
                                sb.append(name5.toString());
                                Log.d(LOG_TAG, sb.toString());
                                name = name5;
                            } else {
                                ctStart = ctStart2;
                                name = name4;
                            }
                        } else {
                            ctStart = ctStart2;
                            name = name3;
                        }
                    } else {
                        ctStart = ctStart2;
                        name = name2;
                    }
                    if (name.length != 0) {
                        Log.d(LOG_TAG, "makeMessageBodyEx name 2= " + name.toString());
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
                    if (!(contentId == null || contentId.length == 0)) {
                        appendOctet(192);
                        if (60 == contentId[0]) {
                            if (62 == contentId[contentId.length - 1]) {
                                appendQuotedString(contentId);
                            }
                        }
                        appendQuotedString(str2 + new String(contentId) + str);
                    }
                    byte[] contentLocation = part2.getContentLocation();
                    if (!(contentLocation == null || contentLocation.length == 0)) {
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
                        InputStream cr2 = null;
                        try {
                            byte[] buffer = new byte[1024];
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
                                                if (cr == null) {
                                                }
                                            } catch (IOException e6) {
                                                e2 = e6;
                                                if (cr == null) {
                                                }
                                            } catch (RuntimeException e7) {
                                                e3 = e7;
                                                if (cr == null) {
                                                }
                                            } catch (Throwable th2) {
                                                cr2 = cr;
                                                th = th2;
                                                if (cr2 != null) {
                                                }
                                                throw th;
                                            }
                                        } catch (FileNotFoundException e8) {
                                            e = e8;
                                            if (cr == null) {
                                            }
                                        } catch (IOException e9) {
                                            e2 = e9;
                                            if (cr == null) {
                                            }
                                        } catch (RuntimeException e10) {
                                            e3 = e10;
                                            if (cr == null) {
                                            }
                                        } catch (Throwable th3) {
                                            cr2 = cr;
                                            th = th3;
                                            if (cr2 != null) {
                                            }
                                            throw th;
                                        }
                                    } catch (FileNotFoundException e11) {
                                        e = e11;
                                        if (cr == null) {
                                        }
                                    } catch (IOException e12) {
                                        e2 = e12;
                                        if (cr == null) {
                                        }
                                    } catch (RuntimeException e13) {
                                        e3 = e13;
                                        if (cr == null) {
                                        }
                                    } catch (Throwable th4) {
                                        cr2 = cr;
                                        th = th4;
                                        if (cr2 != null) {
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
                                if (cr == null) {
                                    return 1;
                                }
                                try {
                                    cr.close();
                                    return 1;
                                } catch (IOException e16) {
                                    return 1;
                                }
                            } catch (IOException e17) {
                                e2 = e17;
                                cr = null;
                                if (cr == null) {
                                    return 1;
                                }
                                try {
                                    cr.close();
                                    return 1;
                                } catch (IOException e18) {
                                    return 1;
                                }
                            } catch (RuntimeException e19) {
                                e3 = e19;
                                cr = null;
                                if (cr == null) {
                                    return 1;
                                }
                                try {
                                    cr.close();
                                    return 1;
                                } catch (IOException e20) {
                                    return 1;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                if (cr2 != null) {
                                    try {
                                        cr2.close();
                                    } catch (IOException e21) {
                                    }
                                }
                                throw th;
                            }
                        } catch (FileNotFoundException e22) {
                            e = e22;
                            cr = null;
                            if (cr == null) {
                            }
                        } catch (IOException e23) {
                            e2 = e23;
                            cr = null;
                            if (cr == null) {
                            }
                        } catch (RuntimeException e24) {
                            e3 = e24;
                            cr = null;
                            if (cr == null) {
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            if (cr2 != null) {
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
                        ctStart2 = ctStart;
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
        Log.d(LOG_TAG, "makeMessageBodyEx body == null");
        appendUintvarInteger(0);
        this.mStack.pop();
        this.mStack.copy();
        return 0;
    }

    private int makeNotifyRespEx() {
        if (this.mMessage == null) {
            this.mMessage = new ByteArrayOutputStream();
            this.mPosition = 0;
        }
        appendOctet(140);
        appendOctet(131);
        if (appendHeader(152) != 0 || appendHeader(141) != 0 || appendHeader(149) != 0) {
            return 1;
        }
        appendHeader(145);
        return 0;
    }

    /* access modifiers changed from: protected */
    public int appendHeader(int field) {
        if (field != 136) {
            return MtkPduComposer.super.appendHeader(field);
        }
        Log.d(LOG_TAG, "EXPIRY");
        long expiry = this.mPduHeader.getLongInteger(field);
        if (-1 == expiry) {
            return 2;
        }
        appendOctet(field);
        this.mStack.newbuf();
        PduComposer.PositionMarker expiryStart = this.mStack.mark();
        append(129);
        appendLongInteger(expiry);
        int expiryLength = expiryStart.getLength();
        this.mStack.pop();
        appendValueLength((long) expiryLength);
        this.mStack.copy();
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
        if (trid == null) {
            Log.d(LOG_TAG, "Transaction ID is null");
        } else {
            appendTextString(trid);
        }
        if (appendHeader(141) != 0) {
            return 1;
        }
        appendHeader(133);
        if (appendHeader(MtkPduHeaders.STATE_SKIP_RETRYING) != 0) {
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
        return makeMessageBodyEx(type);
    }
}
