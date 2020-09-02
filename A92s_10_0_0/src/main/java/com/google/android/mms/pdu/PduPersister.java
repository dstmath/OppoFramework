package com.google.android.mms.pdu;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HbpcdLookup;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.util.DownloadDrmHelper;
import com.google.android.mms.util.DrmConvertSession;
import com.google.android.mms.util.PduCache;
import com.google.android.mms.util.PduCacheEntry;
import com.google.android.mms.util.SqliteWrapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PduPersister {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected static final int[] ADDRESS_FIELDS = {129, 130, 137, 151};
    protected static final HashMap<Integer, Integer> CHARSET_COLUMN_INDEX_MAP = new HashMap<>();
    protected static final HashMap<Integer, String> CHARSET_COLUMN_NAME_MAP = new HashMap<>();
    private static final boolean DEBUG = false;
    private static final long DUMMY_THREAD_ID = Long.MAX_VALUE;
    protected static final HashMap<Integer, Integer> ENCODED_STRING_COLUMN_INDEX_MAP = new HashMap<>();
    protected static final HashMap<Integer, String> ENCODED_STRING_COLUMN_NAME_MAP = new HashMap<>();
    protected static final boolean LOCAL_LOGV = false;
    protected static final HashMap<Integer, Integer> LONG_COLUMN_INDEX_MAP = new HashMap<>();
    protected static final HashMap<Integer, String> LONG_COLUMN_NAME_MAP = new HashMap<>();
    protected static final HashMap<Uri, Integer> MESSAGE_BOX_MAP = new HashMap<>();
    protected static final HashMap<Integer, Integer> OCTET_COLUMN_INDEX_MAP = new HashMap<>();
    protected static final HashMap<Integer, String> OCTET_COLUMN_NAME_MAP = new HashMap<>();
    protected static final int PART_COLUMN_CHARSET = 1;
    protected static final int PART_COLUMN_CONTENT_DISPOSITION = 2;
    protected static final int PART_COLUMN_CONTENT_ID = 3;
    protected static final int PART_COLUMN_CONTENT_LOCATION = 4;
    protected static final int PART_COLUMN_CONTENT_TYPE = 5;
    protected static final int PART_COLUMN_FILENAME = 6;
    protected static final int PART_COLUMN_ID = 0;
    protected static final int PART_COLUMN_NAME = 7;
    protected static final int PART_COLUMN_TEXT = 8;
    protected static final String[] PART_PROJECTION = {HbpcdLookup.ID, "chset", "cd", "cid", "cl", "ct", "fn", "name", "text"};
    protected static final PduCache PDU_CACHE_INSTANCE = PduCache.getInstance();
    protected static final int PDU_COLUMN_CONTENT_CLASS = 11;
    protected static final int PDU_COLUMN_CONTENT_LOCATION = 5;
    protected static final int PDU_COLUMN_CONTENT_TYPE = 6;
    protected static final int PDU_COLUMN_DATE = 21;
    protected static final int PDU_COLUMN_DELIVERY_REPORT = 12;
    protected static final int PDU_COLUMN_DELIVERY_TIME = 22;
    protected static final int PDU_COLUMN_EXPIRY = 23;
    protected static final int PDU_COLUMN_ID = 0;
    protected static final int PDU_COLUMN_MESSAGE_BOX = 1;
    protected static final int PDU_COLUMN_MESSAGE_CLASS = 7;
    protected static final int PDU_COLUMN_MESSAGE_ID = 8;
    protected static final int PDU_COLUMN_MESSAGE_SIZE = 24;
    protected static final int PDU_COLUMN_MESSAGE_TYPE = 13;
    protected static final int PDU_COLUMN_MMS_VERSION = 14;
    protected static final int PDU_COLUMN_PRIORITY = 15;
    protected static final int PDU_COLUMN_READ_REPORT = 16;
    protected static final int PDU_COLUMN_READ_STATUS = 17;
    protected static final int PDU_COLUMN_REPORT_ALLOWED = 18;
    protected static final int PDU_COLUMN_RESPONSE_TEXT = 9;
    protected static final int PDU_COLUMN_RETRIEVE_STATUS = 19;
    protected static final int PDU_COLUMN_RETRIEVE_TEXT = 3;
    protected static final int PDU_COLUMN_RETRIEVE_TEXT_CHARSET = 26;
    protected static final int PDU_COLUMN_STATUS = 20;
    protected static final int PDU_COLUMN_SUBJECT = 4;
    protected static final int PDU_COLUMN_SUBJECT_CHARSET = 25;
    protected static final int PDU_COLUMN_THREAD_ID = 2;
    protected static final int PDU_COLUMN_TRANSACTION_ID = 10;
    private static final String[] PDU_PROJECTION = {HbpcdLookup.ID, "msg_box", "thread_id", "retr_txt", "sub", "ct_l", "ct_t", "m_cls", "m_id", "resp_txt", "tr_id", "ct_cls", "d_rpt", "m_type", "v", "pri", "rr", "read_status", "rpt_a", "retr_st", "st", "date", "d_tm", "exp", "m_size", "sub_cs", "retr_txt_cs"};
    public static final int PROC_STATUS_COMPLETED = 3;
    public static final int PROC_STATUS_PERMANENTLY_FAILURE = 2;
    public static final int PROC_STATUS_TRANSIENT_FAILURE = 1;
    private static final String TAG = "PduPersister";
    public static final String TEMPORARY_DRM_OBJECT_URI = "content://mms/9223372036854775807/part";
    protected static final HashMap<Integer, Integer> TEXT_STRING_COLUMN_INDEX_MAP = new HashMap<>();
    protected static final HashMap<Integer, String> TEXT_STRING_COLUMN_NAME_MAP = new HashMap<>();
    protected static PduPersister sPersister;
    protected final ContentResolver mContentResolver;
    public final Context mContext;
    private final DrmManagerClient mDrmManagerClient;
    protected final TelephonyManager mTelephonyManager;

    static {
        MESSAGE_BOX_MAP.put(Telephony.Mms.Inbox.CONTENT_URI, 1);
        MESSAGE_BOX_MAP.put(Telephony.Mms.Sent.CONTENT_URI, 2);
        MESSAGE_BOX_MAP.put(Telephony.Mms.Draft.CONTENT_URI, 3);
        MESSAGE_BOX_MAP.put(Telephony.Mms.Outbox.CONTENT_URI, 4);
        CHARSET_COLUMN_INDEX_MAP.put(150, 25);
        CHARSET_COLUMN_INDEX_MAP.put(154, 26);
        CHARSET_COLUMN_NAME_MAP.put(150, "sub_cs");
        CHARSET_COLUMN_NAME_MAP.put(154, "retr_txt_cs");
        ENCODED_STRING_COLUMN_INDEX_MAP.put(154, 3);
        ENCODED_STRING_COLUMN_INDEX_MAP.put(150, 4);
        ENCODED_STRING_COLUMN_NAME_MAP.put(154, "retr_txt");
        ENCODED_STRING_COLUMN_NAME_MAP.put(150, "sub");
        TEXT_STRING_COLUMN_INDEX_MAP.put(131, 5);
        TEXT_STRING_COLUMN_INDEX_MAP.put(132, 6);
        TEXT_STRING_COLUMN_INDEX_MAP.put(138, 7);
        TEXT_STRING_COLUMN_INDEX_MAP.put(139, 8);
        TEXT_STRING_COLUMN_INDEX_MAP.put(147, 9);
        TEXT_STRING_COLUMN_INDEX_MAP.put(152, 10);
        TEXT_STRING_COLUMN_NAME_MAP.put(131, "ct_l");
        TEXT_STRING_COLUMN_NAME_MAP.put(132, "ct_t");
        TEXT_STRING_COLUMN_NAME_MAP.put(138, "m_cls");
        TEXT_STRING_COLUMN_NAME_MAP.put(139, "m_id");
        TEXT_STRING_COLUMN_NAME_MAP.put(147, "resp_txt");
        TEXT_STRING_COLUMN_NAME_MAP.put(152, "tr_id");
        OCTET_COLUMN_INDEX_MAP.put(Integer.valueOf((int) PduHeaders.CONTENT_CLASS), 11);
        OCTET_COLUMN_INDEX_MAP.put(134, 12);
        OCTET_COLUMN_INDEX_MAP.put(140, 13);
        OCTET_COLUMN_INDEX_MAP.put(141, 14);
        OCTET_COLUMN_INDEX_MAP.put(143, 15);
        OCTET_COLUMN_INDEX_MAP.put(144, 16);
        OCTET_COLUMN_INDEX_MAP.put(155, 17);
        OCTET_COLUMN_INDEX_MAP.put(145, 18);
        OCTET_COLUMN_INDEX_MAP.put(153, 19);
        OCTET_COLUMN_INDEX_MAP.put(149, 20);
        OCTET_COLUMN_NAME_MAP.put(Integer.valueOf((int) PduHeaders.CONTENT_CLASS), "ct_cls");
        OCTET_COLUMN_NAME_MAP.put(134, "d_rpt");
        OCTET_COLUMN_NAME_MAP.put(140, "m_type");
        OCTET_COLUMN_NAME_MAP.put(141, "v");
        OCTET_COLUMN_NAME_MAP.put(143, "pri");
        OCTET_COLUMN_NAME_MAP.put(144, "rr");
        OCTET_COLUMN_NAME_MAP.put(155, "read_status");
        OCTET_COLUMN_NAME_MAP.put(145, "rpt_a");
        OCTET_COLUMN_NAME_MAP.put(153, "retr_st");
        OCTET_COLUMN_NAME_MAP.put(149, "st");
        LONG_COLUMN_INDEX_MAP.put(133, 21);
        LONG_COLUMN_INDEX_MAP.put(135, 22);
        LONG_COLUMN_INDEX_MAP.put(136, 23);
        LONG_COLUMN_INDEX_MAP.put(142, 24);
        LONG_COLUMN_NAME_MAP.put(133, "date");
        LONG_COLUMN_NAME_MAP.put(135, "d_tm");
        LONG_COLUMN_NAME_MAP.put(136, "exp");
        LONG_COLUMN_NAME_MAP.put(142, "m_size");
    }

    protected PduPersister(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mDrmManagerClient = new DrmManagerClient(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public static PduPersister getPduPersister(Context context) {
        PduPersister pduPersister = sPersister;
        if (pduPersister == null) {
            sPersister = new PduPersister(context);
        } else if (!context.equals(pduPersister.mContext)) {
            sPersister.release();
            sPersister = new PduPersister(context);
        }
        return sPersister;
    }

    /* access modifiers changed from: protected */
    public void setEncodedStringValueToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null && s.length() > 0) {
            headers.setEncodedStringValue(new EncodedStringValue(c.getInt(CHARSET_COLUMN_INDEX_MAP.get(Integer.valueOf(mapColumn)).intValue()), getBytes(s)), mapColumn);
        }
    }

    /* access modifiers changed from: protected */
    public void setTextStringToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null) {
            headers.setTextString(getBytes(s), mapColumn);
        }
    }

    /* access modifiers changed from: protected */
    public void setOctetToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) throws InvalidHeaderValueException {
        if (!c.isNull(columnIndex)) {
            headers.setOctet(c.getInt(columnIndex), mapColumn);
        }
    }

    /* access modifiers changed from: protected */
    public void setLongToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        if (!c.isNull(columnIndex)) {
            headers.setLongInteger(c.getLong(columnIndex), mapColumn);
        }
    }

    /* access modifiers changed from: protected */
    public Integer getIntegerFromPartColumn(Cursor c, int columnIndex) {
        if (!c.isNull(columnIndex)) {
            return Integer.valueOf(c.getInt(columnIndex));
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public byte[] getByteArrayFromPartColumn(Cursor c, int columnIndex) {
        if (!c.isNull(columnIndex)) {
            return getBytes(c.getString(columnIndex));
        }
        return null;
    }

    /* JADX INFO: finally extract failed */
    /* JADX INFO: Multiple debug info for r6v9 android.net.Uri: [D('partURI' android.net.Uri), D('charset' java.lang.Integer)] */
    /* JADX INFO: Multiple debug info for r9v3 java.lang.String: [D('type' java.lang.String), D('partId' long)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x019b A[SYNTHETIC, Splitter:B:78:0x019b] */
    public PduPart[] loadParts(long msgId) throws MmsException {
        ByteArrayOutputStream baos;
        IOException iOException;
        PduPersister pduPersister = this;
        Cursor c = SqliteWrapper.query(pduPersister.mContext, pduPersister.mContentResolver, Uri.parse("content://mms/" + msgId + "/part"), PART_PROJECTION, null, null, null);
        if (c != null) {
            try {
                if (c.getCount() != 0) {
                    int partCount = c.getCount();
                    PduPart[] parts = new PduPart[partCount];
                    int partIdx = 0;
                    while (c.moveToNext()) {
                        PduPart part = new PduPart();
                        Integer charset = pduPersister.getIntegerFromPartColumn(c, 1);
                        if (charset != null) {
                            part.setCharset(charset.intValue());
                        }
                        byte[] contentDisposition = pduPersister.getByteArrayFromPartColumn(c, 2);
                        if (contentDisposition != null) {
                            part.setContentDisposition(contentDisposition);
                        }
                        byte[] contentId = pduPersister.getByteArrayFromPartColumn(c, 3);
                        if (contentId != null) {
                            part.setContentId(contentId);
                        }
                        byte[] contentLocation = pduPersister.getByteArrayFromPartColumn(c, 4);
                        if (contentLocation != null) {
                            part.setContentLocation(contentLocation);
                        }
                        byte[] contentType = pduPersister.getByteArrayFromPartColumn(c, 5);
                        if (contentType != null) {
                            part.setContentType(contentType);
                            byte[] fileName = pduPersister.getByteArrayFromPartColumn(c, 6);
                            if (fileName != null) {
                                part.setFilename(fileName);
                            }
                            byte[] name = pduPersister.getByteArrayFromPartColumn(c, 7);
                            if (name != null) {
                                part.setName(name);
                            }
                            Uri partURI = Uri.parse("content://mms/part/" + c.getLong(0));
                            part.setDataUri(partURI);
                            String type = toIsoString(contentType);
                            if (!ContentType.isImageType(type)) {
                                if (!ContentType.isAudioType(type)) {
                                    if (!ContentType.isVideoType(type)) {
                                        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                                        InputStream is = null;
                                        String type2 = type;
                                        if (ContentType.TEXT_PLAIN.equals(type2) || ContentType.APP_SMIL.equals(type2)) {
                                            baos = baos2;
                                        } else if (ContentType.TEXT_HTML.equals(type2)) {
                                            baos = baos2;
                                        } else {
                                            try {
                                                InputStream is2 = pduPersister.mContentResolver.openInputStream(partURI);
                                                try {
                                                    byte[] buffer = new byte[256];
                                                    int len = is2.read(buffer);
                                                    while (len >= 0) {
                                                        try {
                                                            baos2.write(buffer, 0, len);
                                                            len = is2.read(buffer);
                                                            type2 = type2;
                                                            baos2 = baos2;
                                                            partURI = partURI;
                                                        } catch (IOException e) {
                                                            e = e;
                                                            is = is2;
                                                            try {
                                                                Log.e(TAG, "Failed to load part data", e);
                                                                c.close();
                                                                throw new MmsException(e);
                                                            } catch (Throwable e2) {
                                                                iOException = e2;
                                                                if (is != null) {
                                                                }
                                                                throw iOException;
                                                            }
                                                        } catch (Throwable th) {
                                                            iOException = th;
                                                            is = is2;
                                                            if (is != null) {
                                                            }
                                                            throw iOException;
                                                        }
                                                    }
                                                    baos = baos2;
                                                    try {
                                                        is2.close();
                                                    } catch (IOException e3) {
                                                        Log.e(TAG, "Failed to close stream", e3);
                                                    }
                                                    part.setData(baos.toByteArray());
                                                } catch (IOException e4) {
                                                    e = e4;
                                                    is = is2;
                                                    Log.e(TAG, "Failed to load part data", e);
                                                    c.close();
                                                    throw new MmsException(e);
                                                } catch (Throwable th2) {
                                                    iOException = th2;
                                                    is = is2;
                                                    if (is != null) {
                                                    }
                                                    throw iOException;
                                                }
                                            } catch (IOException e5) {
                                                e = e5;
                                                Log.e(TAG, "Failed to load part data", e);
                                                c.close();
                                                throw new MmsException(e);
                                            } catch (Throwable th3) {
                                                iOException = th3;
                                                if (is != null) {
                                                    try {
                                                        is.close();
                                                    } catch (IOException e6) {
                                                        Log.e(TAG, "Failed to close stream", e6);
                                                    }
                                                }
                                                throw iOException;
                                            }
                                        }
                                        String text = c.getString(8);
                                        byte[] blob = new EncodedStringValue(text != null ? text : PhoneConfigurationManager.SSSS).getTextString();
                                        baos.write(blob, 0, blob.length);
                                        part.setData(baos.toByteArray());
                                    }
                                }
                            }
                            parts[partIdx] = part;
                            pduPersister = this;
                            partIdx++;
                            partCount = partCount;
                        } else {
                            throw new MmsException("Content-Type must be set.");
                        }
                    }
                    c.close();
                    return parts;
                }
            } catch (Throwable th4) {
                c.close();
                throw th4;
            }
        }
        if (c != null) {
            c.close();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void loadAddress(long msgId, PduHeaders headers) {
        Context context = this.mContext;
        ContentResolver contentResolver = this.mContentResolver;
        Cursor c = SqliteWrapper.query(context, contentResolver, Uri.parse("content://mms/" + msgId + "/addr"), new String[]{"address", "charset", "type"}, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                try {
                    String addr = c.getString(0);
                    if (!TextUtils.isEmpty(addr)) {
                        int addrType = c.getInt(2);
                        if (!(addrType == 129 || addrType == 130)) {
                            if (addrType == 137) {
                                headers.setEncodedStringValue(new EncodedStringValue(c.getInt(1), getBytes(addr)), addrType);
                            } else if (addrType != 151) {
                                Log.e(TAG, "Unknown address type: " + addrType);
                            }
                        }
                        headers.appendEncodedStringValue(new EncodedStringValue(c.getInt(1), getBytes(addr)), addrType);
                    }
                } finally {
                    c.close();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0033, code lost:
        r4 = com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0036, code lost:
        if (0 == 0) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.put(r19, new com.google.android.mms.util.PduCacheEntry(null, 0, -1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0044, code lost:
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.setUpdating(r19, false);
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r0 = com.google.android.mms.util.SqliteWrapper.query(r18.mContext, r18.mContentResolver, r19, com.google.android.mms.pdu.PduPersister.PDU_PROJECTION, null, null, null);
        r0 = new com.google.android.mms.pdu.PduHeaders();
        r4 = android.content.ContentUris.parseId(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0079, code lost:
        if (r0 == null) goto L_0x022d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x007f, code lost:
        if (r0.getCount() != 1) goto L_0x022d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0085, code lost:
        if (r0.moveToFirst() == false) goto L_0x022d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0087, code lost:
        r0 = r0.getInt(1);
        r6 = r0.getLong(2);
        r6 = com.google.android.mms.pdu.PduPersister.ENCODED_STRING_COLUMN_INDEX_MAP.entrySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00a0, code lost:
        if (r6.hasNext() == false) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a2, code lost:
        r7 = r6.next();
        setEncodedStringValueToHeaders(r0, r7.getValue().intValue(), r0, r7.getKey().intValue());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c1, code lost:
        r6 = com.google.android.mms.pdu.PduPersister.TEXT_STRING_COLUMN_INDEX_MAP.entrySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00d0, code lost:
        if (r6.hasNext() == false) goto L_0x00f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d2, code lost:
        r7 = r6.next();
        setTextStringToHeaders(r0, r7.getValue().intValue(), r0, r7.getKey().intValue());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00f1, code lost:
        r6 = com.google.android.mms.pdu.PduPersister.OCTET_COLUMN_INDEX_MAP.entrySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0100, code lost:
        if (r6.hasNext() == false) goto L_0x0121;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0102, code lost:
        r7 = r6.next();
        setOctetToHeaders(r0, r7.getValue().intValue(), r0, r7.getKey().intValue());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0121, code lost:
        r6 = com.google.android.mms.pdu.PduPersister.LONG_COLUMN_INDEX_MAP.entrySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0130, code lost:
        if (r6.hasNext() == false) goto L_0x0151;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0132, code lost:
        r7 = r6.next();
        setLongToHeaders(r0, r7.getValue().intValue(), r0, r7.getKey().intValue());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0151, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0159, code lost:
        if (r4 == -1) goto L_0x0223;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x015b, code lost:
        loadAddress(r4, r0);
        r6 = r0.getOctet(140);
        r7 = new com.google.android.mms.pdu.PduBody();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x016b, code lost:
        if (r6 == 132) goto L_0x0175;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x016f, code lost:
        if (r6 != 128) goto L_0x0172;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0175, code lost:
        r8 = loadParts(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0179, code lost:
        if (r8 == null) goto L_0x018e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x017b, code lost:
        r14 = r8.length;
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0182, code lost:
        if (r0 >= r14) goto L_0x0190;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0184, code lost:
        r7.addPart(r8[r0]);
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0190, code lost:
        switch(r6) {
            case 128: goto L_0x01e3;
            case 129: goto L_0x01c8;
            case 130: goto L_0x01c1;
            case 131: goto L_0x01ba;
            case 132: goto L_0x01b3;
            case 133: goto L_0x01ac;
            case 134: goto L_0x01a5;
            case 135: goto L_0x019e;
            case 136: goto L_0x0197;
            case 137: goto L_0x01c8;
            case 138: goto L_0x01c8;
            case 139: goto L_0x01c8;
            case 140: goto L_0x01c8;
            case 141: goto L_0x01c8;
            case 142: goto L_0x01c8;
            case 143: goto L_0x01c8;
            case 144: goto L_0x01c8;
            case 145: goto L_0x01c8;
            case 146: goto L_0x01c8;
            case 147: goto L_0x01c8;
            case 148: goto L_0x01c8;
            case 149: goto L_0x01c8;
            case 150: goto L_0x01c8;
            case 151: goto L_0x01c8;
            default: goto L_0x0193;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0197, code lost:
        r1 = new com.google.android.mms.pdu.ReadOrigInd(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x019e, code lost:
        r1 = new com.google.android.mms.pdu.ReadRecInd(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01a5, code lost:
        r1 = new com.google.android.mms.pdu.DeliveryInd(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01ac, code lost:
        r1 = new com.google.android.mms.pdu.AcknowledgeInd(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01b3, code lost:
        r1 = new com.google.android.mms.pdu.RetrieveConf(r0, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x01ba, code lost:
        r1 = new com.google.android.mms.pdu.NotifyRespInd(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x01c1, code lost:
        r1 = new com.google.android.mms.pdu.NotificationInd(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01e2, code lost:
        throw new com.google.android.mms.MmsException("Unsupported PDU type: " + java.lang.Integer.toHexString(r6));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01e3, code lost:
        r1 = new com.google.android.mms.pdu.SendReq(r0, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01e9, code lost:
        r2 = com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01eb, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:?, code lost:
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.put(r19, new com.google.android.mms.util.PduCacheEntry(r1, r0, r6));
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.setUpdating(r19, false);
        com.google.android.mms.pdu.PduPersister.PDU_CACHE_INSTANCE.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0204, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0206, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0222, code lost:
        throw new com.google.android.mms.MmsException("Unrecognized PDU type: " + java.lang.Integer.toHexString(r6));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x022c, code lost:
        throw new com.google.android.mms.MmsException("Error! ID of the message: -1.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0243, code lost:
        throw new com.google.android.mms.MmsException("Bad uri: " + r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0244, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0245, code lost:
        if (r0 != null) goto L_0x0247;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0247, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x024b, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x024c, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0259  */
    public GenericPdu load(Uri uri) throws MmsException {
        PduCacheEntry cacheEntry;
        try {
            synchronized (PDU_CACHE_INSTANCE) {
                try {
                    if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                        try {
                            PDU_CACHE_INSTANCE.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "load: ", e);
                        }
                        PduCacheEntry cacheEntry2 = (PduCacheEntry) PDU_CACHE_INSTANCE.get(uri);
                        if (cacheEntry2 != null) {
                            GenericPdu pdu = cacheEntry2.getPdu();
                        } else {
                            cacheEntry = cacheEntry2;
                        }
                    } else {
                        cacheEntry = null;
                    }
                    try {
                        PDU_CACHE_INSTANCE.setUpdating(uri, true);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        } catch (Throwable th3) {
            th = th3;
            synchronized (PDU_CACHE_INSTANCE) {
                if (0 != 0) {
                    PDU_CACHE_INSTANCE.put(uri, new PduCacheEntry(null, 0, -1));
                }
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll();
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void persistAddress(long msgId, int type, EncodedStringValue[] array) {
        ContentValues values = new ContentValues(3);
        for (EncodedStringValue addr : array) {
            values.clear();
            values.put("address", toIsoString(addr.getTextString()));
            values.put("charset", Integer.valueOf(addr.getCharacterSet()));
            values.put("type", Integer.valueOf(type));
            SqliteWrapper.insert(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + msgId + "/addr"), values);
        }
    }

    protected static String getPartContentType(PduPart part) {
        if (part.getContentType() == null) {
            return null;
        }
        return toIsoString(part.getContentType());
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Integer):void}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Byte):void}
      ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Float):void}
      ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.String):void}
      ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Long):void}
      ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Boolean):void}
      ClspMth{android.content.ContentValues.put(java.lang.String, byte[]):void}
      ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Double):void}
      ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Short):void}
      ClspMth{android.content.ContentValues.put(java.lang.String, java.lang.Integer):void} */
    public Uri persistPart(PduPart part, long msgId, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        Uri uri = Uri.parse("content://mms/" + msgId + "/part");
        ContentValues values = new ContentValues(8);
        int charset = part.getCharset();
        if (charset != 0) {
            values.put("chset", Integer.valueOf(charset));
        }
        String contentType = getPartContentType(part);
        if (contentType != null) {
            if (ContentType.IMAGE_JPG.equals(contentType)) {
                contentType = ContentType.IMAGE_JPEG;
            }
            values.put("ct", contentType);
            if (ContentType.APP_SMIL.equals(contentType)) {
                values.put("seq", (Integer) -1);
            }
            if (part.getFilename() != null) {
                values.put("fn", new String(part.getFilename()));
            }
            if (part.getName() != null) {
                values.put("name", new String(part.getName()));
            }
            if (part.getContentDisposition() != null) {
                values.put("cd", (String) toIsoString(part.getContentDisposition()));
            }
            if (part.getContentId() != null) {
                values.put("cid", (String) toIsoString(part.getContentId()));
            }
            if (part.getContentLocation() != null) {
                values.put("cl", (String) toIsoString(part.getContentLocation()));
            }
            Uri res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
            if (res != null) {
                persistData(part, res, contentType, preOpenedFiles);
                part.setDataUri(res);
                return res;
            }
            throw new MmsException("Failed to persist part, return null.");
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x02d6 A[SYNTHETIC, Splitter:B:161:0x02d6] */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x02f1 A[SYNTHETIC, Splitter:B:166:0x02f1] */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x039c A[SYNTHETIC, Splitter:B:195:0x039c] */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x03b7 A[SYNTHETIC, Splitter:B:200:0x03b7] */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x03d2  */
    /* JADX WARNING: Removed duplicated region for block: B:216:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0138 A[Catch:{ FileNotFoundException -> 0x0369, IOException -> 0x0363, all -> 0x035e }] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0139 A[Catch:{ FileNotFoundException -> 0x0369, IOException -> 0x0363, all -> 0x035e }] */
    public void persistData(PduPart part, Uri uri, String contentType, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        FileNotFoundException fileNotFoundException;
        OutputStream os;
        Object obj;
        Uri dataUri;
        InputStream is;
        OutputStream os2;
        OutputStream os3 = null;
        InputStream is2 = null;
        DrmConvertSession drmConvertSession = null;
        Uri dataUri2 = null;
        try {
            byte[] data = part.getData();
            if (ContentType.TEXT_PLAIN.equals(contentType)) {
                os = os3;
                obj = null;
            } else if (ContentType.APP_SMIL.equals(contentType)) {
                os = os3;
                obj = null;
            } else if (ContentType.TEXT_HTML.equals(contentType)) {
                os = os3;
                obj = null;
            } else {
                boolean isDrm = DownloadDrmHelper.isDrmConvertNeeded(contentType);
                if (isDrm) {
                    if (uri != null) {
                        try {
                            ParcelFileDescriptor pfd = this.mContentResolver.openFileDescriptor(uri, "r");
                            try {
                                if (pfd.getStatSize() > 0) {
                                    try {
                                        pfd.close();
                                        if (os3 != null) {
                                            try {
                                                os3.close();
                                            } catch (IOException e) {
                                                Log.e(TAG, "IOException while closing: " + os3, e);
                                            }
                                        }
                                        if (is2 != null) {
                                            try {
                                                is2.close();
                                            } catch (IOException e2) {
                                                Log.e(TAG, "IOException while closing: " + is2, e2);
                                            }
                                        }
                                        if (drmConvertSession != null) {
                                            drmConvertSession.close(null);
                                            File f = new File((String) null);
                                            ContentValues values = new ContentValues(0);
                                            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + f.getName()), values, null, null);
                                            return;
                                        }
                                        return;
                                    } catch (Exception e3) {
                                        e = e3;
                                        dataUri = null;
                                        os2 = os3;
                                        try {
                                            Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e);
                                            drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                                            if (drmConvertSession == null) {
                                            }
                                        } catch (FileNotFoundException e4) {
                                            e = e4;
                                            Log.e(TAG, "Failed to open Input/Output stream.", e);
                                            throw new MmsException(e);
                                        } catch (IOException e5) {
                                            e = e5;
                                            dataUri2 = dataUri;
                                            os3 = os2;
                                            Log.e(TAG, "Failed to read/write data.", e);
                                            throw new MmsException(e);
                                        } catch (Throwable th) {
                                            fileNotFoundException = th;
                                            os3 = os2;
                                            if (os3 != null) {
                                            }
                                            if (is2 != null) {
                                            }
                                            if (drmConvertSession != null) {
                                            }
                                            throw fileNotFoundException;
                                        }
                                    } catch (FileNotFoundException e6) {
                                        e = e6;
                                        Log.e(TAG, "Failed to open Input/Output stream.", e);
                                        throw new MmsException(e);
                                    } catch (IOException e7) {
                                        e = e7;
                                        Log.e(TAG, "Failed to read/write data.", e);
                                        throw new MmsException(e);
                                    } catch (Throwable th2) {
                                        fileNotFoundException = th2;
                                        if (os3 != null) {
                                            try {
                                                os3.close();
                                            } catch (IOException e8) {
                                                Log.e(TAG, "IOException while closing: " + os3, e8);
                                            }
                                        }
                                        if (is2 != null) {
                                            try {
                                                is2.close();
                                            } catch (IOException e9) {
                                                Log.e(TAG, "IOException while closing: " + is2, e9);
                                            }
                                        }
                                        if (drmConvertSession != null) {
                                            drmConvertSession.close(null);
                                            File f2 = new File((String) null);
                                            ContentValues values2 = new ContentValues(0);
                                            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + f2.getName()), values2, null, null);
                                        }
                                        throw fileNotFoundException;
                                    }
                                } else {
                                    dataUri = null;
                                    try {
                                        pfd.close();
                                    } catch (Exception e10) {
                                        e = e10;
                                        os2 = os3;
                                        Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e);
                                        drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                                        if (drmConvertSession == null) {
                                        }
                                    }
                                }
                            } catch (Throwable th3) {
                                if (pfd != null) {
                                    try {
                                        pfd.close();
                                    } catch (Exception e11) {
                                        e = e11;
                                        Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e);
                                        drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                                        if (drmConvertSession == null) {
                                        }
                                    } catch (Throwable th4) {
                                        os2 = os3;
                                        th.addSuppressed(th4);
                                    }
                                }
                                throw th3;
                            }
                        } catch (Exception e12) {
                            e = e12;
                            os2 = os3;
                            dataUri = null;
                            Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e);
                            drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                            if (drmConvertSession == null) {
                            }
                        }
                    } else {
                        dataUri = null;
                    }
                    drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                    if (drmConvertSession == null) {
                        throw new MmsException("Mimetype " + contentType + " can not be converted.");
                    }
                } else {
                    dataUri = null;
                }
                os3 = this.mContentResolver.openOutputStream(uri);
                if (data == null) {
                    try {
                        dataUri2 = part.getDataUri();
                        if (dataUri2 != null) {
                            try {
                                if (!dataUri2.equals(uri)) {
                                    if (preOpenedFiles != null && preOpenedFiles.containsKey(dataUri2)) {
                                        is2 = preOpenedFiles.get(dataUri2);
                                    }
                                    if (is2 == null) {
                                        is2 = this.mContentResolver.openInputStream(dataUri2);
                                    }
                                    try {
                                        byte[] buffer = new byte[8192];
                                        while (true) {
                                            int len = is2.read(buffer);
                                            if (len == -1) {
                                                break;
                                            }
                                            if (!isDrm) {
                                                os3.write(buffer, 0, len);
                                                is = is2;
                                            } else {
                                                byte[] convertedData = drmConvertSession.convert(buffer, len);
                                                if (convertedData != null) {
                                                    is = is2;
                                                    try {
                                                        os3.write(convertedData, 0, convertedData.length);
                                                    } catch (FileNotFoundException e13) {
                                                        e = e13;
                                                        Log.e(TAG, "Failed to open Input/Output stream.", e);
                                                        throw new MmsException(e);
                                                    } catch (IOException e14) {
                                                        e = e14;
                                                        is2 = is;
                                                        Log.e(TAG, "Failed to read/write data.", e);
                                                        throw new MmsException(e);
                                                    } catch (Throwable th5) {
                                                        fileNotFoundException = th5;
                                                        is2 = is;
                                                        if (os3 != null) {
                                                        }
                                                        if (is2 != null) {
                                                        }
                                                        if (drmConvertSession != null) {
                                                        }
                                                        throw fileNotFoundException;
                                                    }
                                                } else {
                                                    throw new MmsException("Error converting drm data.");
                                                }
                                            }
                                            is2 = is;
                                        }
                                    } catch (FileNotFoundException e15) {
                                        e = e15;
                                        Log.e(TAG, "Failed to open Input/Output stream.", e);
                                        throw new MmsException(e);
                                    } catch (IOException e16) {
                                        e = e16;
                                        Log.e(TAG, "Failed to read/write data.", e);
                                        throw new MmsException(e);
                                    } catch (Throwable th6) {
                                        fileNotFoundException = th6;
                                        if (os3 != null) {
                                        }
                                        if (is2 != null) {
                                        }
                                        if (drmConvertSession != null) {
                                        }
                                        throw fileNotFoundException;
                                    }
                                }
                            } catch (FileNotFoundException e17) {
                                e = e17;
                                Log.e(TAG, "Failed to open Input/Output stream.", e);
                                throw new MmsException(e);
                            } catch (IOException e18) {
                                e = e18;
                                Log.e(TAG, "Failed to read/write data.", e);
                                throw new MmsException(e);
                            }
                        }
                        Log.w(TAG, "Can't find data for this part.");
                        if (os3 != null) {
                            try {
                                os3.close();
                            } catch (IOException e19) {
                                Log.e(TAG, "IOException while closing: " + os3, e19);
                            }
                        }
                        if (is2 != null) {
                            try {
                                is2.close();
                            } catch (IOException e20) {
                                Log.e(TAG, "IOException while closing: " + is2, e20);
                            }
                        }
                        if (drmConvertSession != null) {
                            drmConvertSession.close(null);
                            File f3 = new File((String) null);
                            ContentValues values3 = new ContentValues(0);
                            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + f3.getName()), values3, null, null);
                            return;
                        }
                        return;
                    } catch (FileNotFoundException e21) {
                        e = e21;
                        Log.e(TAG, "Failed to open Input/Output stream.", e);
                        throw new MmsException(e);
                    } catch (IOException e22) {
                        e = e22;
                        dataUri2 = dataUri;
                        Log.e(TAG, "Failed to read/write data.", e);
                        throw new MmsException(e);
                    } catch (Throwable th7) {
                        fileNotFoundException = th7;
                        if (os3 != null) {
                        }
                        if (is2 != null) {
                        }
                        if (drmConvertSession != null) {
                        }
                        throw fileNotFoundException;
                    }
                } else if (!isDrm) {
                    os3.write(data);
                } else {
                    byte[] convertedData2 = drmConvertSession.convert(data, data.length);
                    if (convertedData2 != null) {
                        os3.write(convertedData2, 0, convertedData2.length);
                    } else {
                        throw new MmsException("Error converting drm data.");
                    }
                }
                if (os3 != null) {
                    try {
                        os3.close();
                    } catch (IOException e23) {
                        Log.e(TAG, "IOException while closing: " + os3, e23);
                    }
                }
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException e24) {
                        Log.e(TAG, "IOException while closing: " + is2, e24);
                    }
                }
                if (drmConvertSession == null) {
                    drmConvertSession.close(null);
                    File f4 = new File((String) null);
                    ContentValues values4 = new ContentValues(0);
                    SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + f4.getName()), values4, null, null);
                    return;
                }
                return;
            }
            ContentValues cv = new ContentValues();
            if (data == null) {
                data = new String(PhoneConfigurationManager.SSSS).getBytes("utf-8");
            }
            cv.put("text", new EncodedStringValue(data).getString());
            if (this.mContentResolver.update(uri, cv, null, null) == 1) {
                os3 = os;
                if (os3 != null) {
                }
                if (is2 != null) {
                }
                if (drmConvertSession == null) {
                }
            } else {
                throw new MmsException("unable to update " + uri.toString());
            }
        } catch (FileNotFoundException e25) {
            e = e25;
            Log.e(TAG, "Failed to open Input/Output stream.", e);
            throw new MmsException(e);
        } catch (IOException e26) {
            e = e26;
            Log.e(TAG, "Failed to read/write data.", e);
            throw new MmsException(e);
        } catch (Throwable e27) {
            fileNotFoundException = e27;
            if (os3 != null) {
            }
            if (is2 != null) {
            }
            if (drmConvertSession != null) {
            }
            throw fileNotFoundException;
        }
    }

    private void updateAddress(long msgId, int type, EncodedStringValue[] array) {
        Context context = this.mContext;
        ContentResolver contentResolver = this.mContentResolver;
        Uri parse = Uri.parse("content://mms/" + msgId + "/addr");
        StringBuilder sb = new StringBuilder();
        sb.append("type=");
        sb.append(type);
        SqliteWrapper.delete(context, contentResolver, parse, sb.toString(), null);
        persistAddress(msgId, type, array);
    }

    public void updateHeaders(Uri uri, SendReq sendReq) {
        int i;
        int i2;
        EncodedStringValue[] array;
        PduHeaders headers;
        int i3;
        synchronized (PDU_CACHE_INSTANCE) {
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "updateHeaders: ", e);
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri);
        ContentValues values = new ContentValues(10);
        byte[] contentType = sendReq.getContentType();
        if (contentType != null) {
            values.put("ct_t", toIsoString(contentType));
        }
        long date = sendReq.getDate();
        if (date != -1) {
            values.put("date", Long.valueOf(date));
        }
        int deliveryReport = sendReq.getDeliveryReport();
        if (deliveryReport != 0) {
            values.put("d_rpt", Integer.valueOf(deliveryReport));
        }
        long expiry = sendReq.getExpiry();
        if (expiry != -1) {
            values.put("exp", Long.valueOf(expiry));
        }
        byte[] msgClass = sendReq.getMessageClass();
        if (msgClass != null) {
            values.put("m_cls", toIsoString(msgClass));
        }
        int priority = sendReq.getPriority();
        if (priority != 0) {
            values.put("pri", Integer.valueOf(priority));
        }
        int readReport = sendReq.getReadReport();
        if (readReport != 0) {
            values.put("rr", Integer.valueOf(readReport));
        }
        byte[] transId = sendReq.getTransactionId();
        if (transId != null) {
            values.put("tr_id", toIsoString(transId));
        }
        EncodedStringValue subject = sendReq.getSubject();
        if (subject != null) {
            values.put("sub", toIsoString(subject.getTextString()));
            values.put("sub_cs", Integer.valueOf(subject.getCharacterSet()));
        } else {
            values.put("sub", PhoneConfigurationManager.SSSS);
        }
        long messageSize = sendReq.getMessageSize();
        if (messageSize > 0) {
            values.put("m_size", Long.valueOf(messageSize));
        }
        PduHeaders headers2 = sendReq.getPduHeaders();
        HashSet<String> recipients = new HashSet<>();
        int[] iArr = ADDRESS_FIELDS;
        int length = iArr.length;
        int i4 = 0;
        while (i4 < length) {
            int addrType = iArr[i4];
            if (addrType == 137) {
                EncodedStringValue v = headers2.getEncodedStringValue(addrType);
                if (v != null) {
                    i = length;
                    i2 = 0;
                    array = new EncodedStringValue[]{v};
                } else {
                    i = length;
                    i2 = 0;
                    array = null;
                }
            } else {
                i = length;
                i2 = 0;
                array = headers2.getEncodedStringValues(addrType);
            }
            if (array != null) {
                headers = headers2;
                updateAddress(ContentUris.parseId(uri), addrType, array);
                if (addrType == 151) {
                    int length2 = array.length;
                    int addrType2 = i2;
                    while (addrType2 < length2) {
                        EncodedStringValue v2 = array[addrType2];
                        if (v2 != null) {
                            i3 = length2;
                            recipients.add(v2.getString());
                        } else {
                            i3 = length2;
                        }
                        addrType2++;
                        length2 = i3;
                    }
                }
            } else {
                headers = headers2;
            }
            i4++;
            headers2 = headers;
            iArr = iArr;
            length = i;
        }
        if (!recipients.isEmpty()) {
            values.put("thread_id", Long.valueOf(Telephony.Threads.getOrCreateThreadId(this.mContext, recipients)));
        }
        SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
    }

    /* access modifiers changed from: protected */
    public void updatePart(Uri uri, PduPart part, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        ContentValues values = new ContentValues(7);
        int charset = part.getCharset();
        if (charset != 0) {
            values.put("chset", Integer.valueOf(charset));
        }
        if (part.getContentType() != null) {
            String contentType = toIsoString(part.getContentType());
            values.put("ct", contentType);
            if (part.getFilename() != null) {
                values.put("fn", new String(part.getFilename()));
            }
            if (part.getName() != null) {
                values.put("name", new String(part.getName()));
            }
            Object value = null;
            if (part.getContentDisposition() != null) {
                value = toIsoString(part.getContentDisposition());
                values.put("cd", (String) value);
            }
            if (part.getContentId() != null) {
                value = toIsoString(part.getContentId());
                values.put("cid", (String) value);
            }
            if (part.getContentLocation() != null) {
                values.put("cl", (String) toIsoString(part.getContentLocation()));
            }
            SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
            if (part.getData() != null || !uri.equals(part.getDataUri())) {
                persistData(part, uri, contentType, preOpenedFiles);
                return;
            }
            return;
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    public void updateParts(Uri uri, PduBody body, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        try {
            synchronized (PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "updateParts: ", e);
                    }
                    PduCacheEntry cacheEntry = (PduCacheEntry) PDU_CACHE_INSTANCE.get(uri);
                    if (cacheEntry != null) {
                        ((MultimediaMessagePdu) cacheEntry.getPdu()).setBody(body);
                    }
                }
                PDU_CACHE_INSTANCE.setUpdating(uri, true);
            }
            ArrayList<PduPart> toBeCreated = new ArrayList<>();
            HashMap<Uri, PduPart> toBeUpdated = new HashMap<>();
            int partsNum = body.getPartsNum();
            StringBuilder filter = new StringBuilder();
            filter.append('(');
            for (int i = 0; i < partsNum; i++) {
                PduPart part = body.getPart(i);
                Uri partUri = part.getDataUri();
                if (partUri == null || TextUtils.isEmpty(partUri.getAuthority()) || !partUri.getAuthority().startsWith("mms")) {
                    toBeCreated.add(part);
                } else {
                    toBeUpdated.put(partUri, part);
                    if (filter.length() > 1) {
                        filter.append(" AND ");
                    }
                    filter.append(HbpcdLookup.ID);
                    filter.append("!=");
                    DatabaseUtils.appendEscapedSQLString(filter, partUri.getLastPathSegment());
                }
            }
            filter.append(')');
            long msgId = ContentUris.parseId(uri);
            Context context = this.mContext;
            ContentResolver contentResolver = this.mContentResolver;
            SqliteWrapper.delete(context, contentResolver, Uri.parse(Telephony.Mms.CONTENT_URI + "/" + msgId + "/part"), filter.length() > 2 ? filter.toString() : null, null);
            Iterator<PduPart> it = toBeCreated.iterator();
            while (it.hasNext()) {
                persistPart(it.next(), msgId, preOpenedFiles);
            }
            for (Map.Entry<Uri, PduPart> e2 : toBeUpdated.entrySet()) {
                updatePart(e2.getKey(), e2.getValue(), preOpenedFiles);
            }
            synchronized (PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll();
            }
        } catch (Throwable th) {
            synchronized (PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll();
                throw th;
            }
        }
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        long msgId;
        long dummyId;
        PduBody body;
        int messageSize;
        long dummyId2;
        int i;
        Uri res;
        boolean textOnly;
        int i2;
        int[] iArr;
        if (uri != null) {
            try {
                msgId = ContentUris.parseId(uri);
            } catch (NumberFormatException e) {
                msgId = -1;
            }
            boolean existingUri = msgId != -1;
            if (existingUri || MESSAGE_BOX_MAP.get(uri) != null) {
                synchronized (PDU_CACHE_INSTANCE) {
                    if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                        try {
                            PDU_CACHE_INSTANCE.wait();
                        } catch (InterruptedException e2) {
                            Log.e(TAG, "persist1: ", e2);
                        }
                    }
                }
                PDU_CACHE_INSTANCE.purge(uri);
                PduHeaders header = pdu.getPduHeaders();
                ContentValues values = new ContentValues();
                for (Map.Entry<Integer, String> e3 : ENCODED_STRING_COLUMN_NAME_MAP.entrySet()) {
                    int field = e3.getKey().intValue();
                    EncodedStringValue encodedString = header.getEncodedStringValue(field);
                    if (encodedString != null) {
                        values.put(e3.getValue(), toIsoString(encodedString.getTextString()));
                        values.put(CHARSET_COLUMN_NAME_MAP.get(Integer.valueOf(field)), Integer.valueOf(encodedString.getCharacterSet()));
                    }
                }
                for (Map.Entry<Integer, String> e4 : TEXT_STRING_COLUMN_NAME_MAP.entrySet()) {
                    byte[] text = header.getTextString(e4.getKey().intValue());
                    if (text != null) {
                        values.put(e4.getValue(), toIsoString(text));
                    }
                }
                for (Map.Entry<Integer, String> e5 : OCTET_COLUMN_NAME_MAP.entrySet()) {
                    int b = header.getOctet(e5.getKey().intValue());
                    if (b != 0) {
                        values.put(e5.getValue(), Integer.valueOf(b));
                    }
                }
                for (Map.Entry<Integer, String> e6 : LONG_COLUMN_NAME_MAP.entrySet()) {
                    long l = header.getLongInteger(e6.getKey().intValue());
                    if (l != -1) {
                        values.put(e6.getValue(), Long.valueOf(l));
                    }
                }
                HashMap<Integer, EncodedStringValue[]> addressMap = new HashMap<>(ADDRESS_FIELDS.length);
                int[] iArr2 = ADDRESS_FIELDS;
                int length = iArr2.length;
                int i3 = 0;
                while (i3 < length) {
                    int addrType = iArr2[i3];
                    EncodedStringValue[] array = null;
                    if (addrType == 137) {
                        EncodedStringValue v = header.getEncodedStringValue(addrType);
                        if (v != null) {
                            iArr = iArr2;
                            i2 = length;
                            array = new EncodedStringValue[]{v};
                        } else {
                            iArr = iArr2;
                            i2 = length;
                        }
                    } else {
                        iArr = iArr2;
                        i2 = length;
                        array = header.getEncodedStringValues(addrType);
                    }
                    addressMap.put(Integer.valueOf(addrType), array);
                    i3++;
                    iArr2 = iArr;
                    length = i2;
                }
                HashSet<String> recipients = new HashSet<>();
                int msgType = pdu.getMessageType();
                if (msgType == 130 || msgType == 132 || msgType == 128) {
                    if (msgType == 128) {
                        loadRecipients(151, recipients, addressMap, false);
                    } else if (msgType == 130 || msgType == 132) {
                        loadRecipients(137, recipients, addressMap, false);
                        if (groupMmsEnabled) {
                            loadRecipients(151, recipients, addressMap, true);
                            loadRecipients(130, recipients, addressMap, true);
                        }
                    }
                    long threadId = 0;
                    if (createThreadId && !recipients.isEmpty()) {
                        threadId = Telephony.Threads.getOrCreateThreadId(this.mContext, recipients);
                    }
                    values.put("thread_id", Long.valueOf(threadId));
                }
                long dummyId3 = System.currentTimeMillis();
                boolean messageSize2 = true;
                if (pdu instanceof MultimediaMessagePdu) {
                    PduBody body2 = ((MultimediaMessagePdu) pdu).getBody();
                    if (body2 != null) {
                        int partsNum = body2.getPartsNum();
                        if (partsNum > 2) {
                            textOnly = false;
                        } else {
                            textOnly = true;
                        }
                        boolean textOnly2 = textOnly;
                        int i4 = 0;
                        int messageSize3 = 0;
                        while (i4 < partsNum) {
                            PduPart part = body2.getPart(i4);
                            messageSize3 += part.getDataLength();
                            persistPart(part, dummyId3, preOpenedFiles);
                            String contentType = getPartContentType(part);
                            if (contentType != null && !ContentType.APP_SMIL.equals(contentType) && !ContentType.TEXT_PLAIN.equals(contentType)) {
                                textOnly2 = false;
                            }
                            i4++;
                            partsNum = partsNum;
                            recipients = recipients;
                            dummyId3 = dummyId3;
                        }
                        dummyId = dummyId3;
                        body = body2;
                        messageSize = messageSize3;
                        messageSize2 = textOnly2;
                    } else {
                        dummyId = dummyId3;
                        body = body2;
                        messageSize = 0;
                    }
                } else {
                    dummyId = dummyId3;
                    body = null;
                    messageSize = 0;
                }
                values.put("text_only", Integer.valueOf(messageSize2 ? 1 : 0));
                if (values.getAsInteger("m_size") == null) {
                    values.put("m_size", Integer.valueOf(messageSize));
                }
                if (existingUri) {
                    dummyId2 = dummyId;
                    i = 0;
                    SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
                    res = uri;
                } else {
                    dummyId2 = dummyId;
                    i = 0;
                    res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
                    if (res != null) {
                        msgId = ContentUris.parseId(res);
                    } else {
                        throw new MmsException("persist() failed: return null.");
                    }
                }
                ContentValues values2 = new ContentValues(1);
                values2.put("mid", Long.valueOf(msgId));
                Context context = this.mContext;
                ContentResolver contentResolver = this.mContentResolver;
                StringBuilder sb = new StringBuilder();
                sb.append("content://mms/");
                Uri res2 = res;
                sb.append(dummyId2);
                sb.append("/part");
                SqliteWrapper.update(context, contentResolver, Uri.parse(sb.toString()), values2, null, null);
                if (!existingUri) {
                    res2 = Uri.parse(uri + "/" + msgId);
                }
                int[] iArr3 = ADDRESS_FIELDS;
                int length2 = iArr3.length;
                int i5 = i;
                while (i5 < length2) {
                    int addrType2 = iArr3[i5];
                    EncodedStringValue[] array2 = addressMap.get(Integer.valueOf(addrType2));
                    if (array2 != null) {
                        persistAddress(msgId, addrType2, array2);
                    }
                    i5++;
                    messageSize = messageSize;
                }
                return res2;
            }
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
        throw new MmsException("Uri may not be null.");
    }

    /* access modifiers changed from: protected */
    public void loadRecipients(int addressType, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        EncodedStringValue[] array = addressMap.get(Integer.valueOf(addressType));
        if (array != null) {
            if (!excludeMyNumber || array.length != 1) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(this.mContext);
                Set<String> myPhoneNumbers = new HashSet<>();
                if (excludeMyNumber) {
                    for (int subid : subscriptionManager.getActiveSubscriptionIdList()) {
                        String myNumber = this.mTelephonyManager.getLine1Number(subid);
                        if (myNumber != null) {
                            myPhoneNumbers.add(myNumber);
                        }
                    }
                }
                for (EncodedStringValue v : array) {
                    if (v != null) {
                        String number = v.getString();
                        if (excludeMyNumber) {
                            Iterator<String> it = myPhoneNumbers.iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    if (!PhoneNumberUtils.compare(number, it.next()) && !recipients.contains(number)) {
                                        recipients.add(number);
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        } else if (!recipients.contains(number)) {
                            recipients.add(number);
                        }
                    }
                }
            }
        }
    }

    public Uri move(Uri from, Uri to) throws MmsException {
        long msgId = ContentUris.parseId(from);
        if (msgId != -1) {
            Integer msgBox = MESSAGE_BOX_MAP.get(to);
            if (msgBox != null) {
                ContentValues values = new ContentValues(1);
                values.put("msg_box", msgBox);
                SqliteWrapper.update(this.mContext, this.mContentResolver, from, values, null, null);
                return ContentUris.withAppendedId(to, msgId);
            }
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
        throw new MmsException("Error! ID of the message: -1.");
    }

    public static String toIsoString(byte[] bytes) {
        try {
            return new String(bytes, CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return PhoneConfigurationManager.SSSS;
        }
    }

    public static byte[] getBytes(String data) {
        try {
            return data.getBytes(CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return new byte[0];
        }
    }

    public void release() {
        SqliteWrapper.delete(this.mContext, this.mContentResolver, Uri.parse(TEMPORARY_DRM_OBJECT_URI), null, null);
        this.mDrmManagerClient.release();
    }

    public Cursor getPendingMessages(long dueTime) {
        Uri.Builder uriBuilder = Telephony.MmsSms.PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        return SqliteWrapper.query(this.mContext, this.mContentResolver, uriBuilder.build(), null, "err_type < ? AND due_time <= ?", new String[]{String.valueOf(10), String.valueOf(dueTime)}, "due_time");
    }
}
