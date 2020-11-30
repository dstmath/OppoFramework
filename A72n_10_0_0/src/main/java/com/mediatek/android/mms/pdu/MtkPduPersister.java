package com.mediatek.android.mms.pdu;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.util.SqliteWrapper;
import com.mediatek.internal.telephony.cat.BipUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import mediatek.telephony.MtkTelephony;

public class MtkPduPersister extends PduPersister {
    private static final int PDU_COLUMN_DATE_SENT = 28;
    private static final int PDU_COLUMN_READ = 27;
    private static final String[] PDU_PROJECTION = {"_id", "msg_box", "thread_id", "retr_txt", "sub", "ct_l", "ct_t", "m_cls", "m_id", "resp_txt", "tr_id", "ct_cls", "d_rpt", "m_type", "v", "pri", "rr", "read_status", "rpt_a", "retr_st", "st", "date", "d_tm", "exp", "m_size", "sub_cs", "retr_txt_cs", "read", "date_sent"};
    private static final String TAG = "MtkPduPersister";
    protected static MtkPduPersister sPersister;
    private boolean mBackupRestore = false;

    protected MtkPduPersister(Context context) {
        super(context);
        if (LONG_COLUMN_INDEX_MAP != null) {
            LONG_COLUMN_INDEX_MAP.put(201, Integer.valueOf((int) PDU_COLUMN_DATE_SENT));
        }
        if (LONG_COLUMN_NAME_MAP != null) {
            LONG_COLUMN_NAME_MAP.put(201, "date_sent");
        }
    }

    public static synchronized MtkPduPersister getPduPersister(Context context) {
        MtkPduPersister mtkPduPersister;
        synchronized (MtkPduPersister.class) {
            Log.d(TAG, "getPduPersister context = " + context);
            if (sPersister == null) {
                sPersister = new MtkPduPersister(context);
            } else if (!context.equals(sPersister.mContext)) {
                sPersister.release();
                sPersister = new MtkPduPersister(context);
            }
            Log.d(TAG, "releaseOwnPduPersister return " + sPersister);
            mtkPduPersister = sPersister;
        }
        return mtkPduPersister;
    }

    public static synchronized void releaseOwnPduPersister(Context context) {
        synchronized (MtkPduPersister.class) {
            Log.d(TAG, "releaseOwnPduPersister context = " + context + ", sPersister = " + sPersister);
            if (!(sPersister == null || sPersister.mContext == null || !context.equals(sPersister.mContext))) {
                Log.d(TAG, "releaseOwnPduPersister it is my own");
                sPersister.release();
                sPersister = null;
            }
            Log.d(TAG, "releaseOwnPduPersister leave syncObj");
        }
    }

    private byte[] getByteArrayFromPartColumn2(Cursor c, int columnIndex) {
        if (!c.isNull(columnIndex)) {
            return c.getString(columnIndex).getBytes();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void persistAddress(long msgId, int type, EncodedStringValue[] array) {
        ArrayList<String> strValues = new ArrayList<>();
        ContentValues values = new ContentValues();
        Uri uri = Uri.parse("content://mms/" + msgId + "/addr");
        if (array != null) {
            for (EncodedStringValue addrOrg : array) {
                strValues.add(toIsoString(addrOrg.getTextString()));
                strValues.add(String.valueOf(addrOrg.getCharacterSet()));
                strValues.add(String.valueOf(type));
            }
            values.putStringArrayList("addresses", strValues);
            SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX INFO: Multiple debug info for r10v2 long: [D('partId' long), D('contentDisposition' byte[])] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01ad A[SYNTHETIC, Splitter:B:84:0x01ad] */
    public PduPart[] loadParts(long msgId) throws MmsException {
        ByteArrayOutputStream baos;
        byte[] blob;
        IOException iOException;
        IOException e;
        byte[] buffer;
        MtkPduPersister mtkPduPersister = this;
        Cursor c = SqliteWrapper.query(mtkPduPersister.mContext, mtkPduPersister.mContentResolver, Uri.parse("content://mms/" + msgId + "/part"), PART_PROJECTION, (String) null, (String[]) null, (String) null);
        if (c != null) {
            try {
                if (c.getCount() != 0) {
                    int partCount = c.getCount();
                    MtkPduPart[] parts = new MtkPduPart[partCount];
                    int partIdx = 0;
                    while (c.moveToNext()) {
                        MtkPduPart part = new MtkPduPart();
                        Integer charset = mtkPduPersister.getIntegerFromPartColumn(c, 1);
                        if (charset != null) {
                            part.setCharset(charset.intValue());
                        }
                        byte[] contentDisposition = mtkPduPersister.getByteArrayFromPartColumn(c, 2);
                        if (contentDisposition != null) {
                            part.setContentDisposition(contentDisposition);
                        }
                        byte[] contentId = mtkPduPersister.getByteArrayFromPartColumn(c, 3);
                        if (contentId != null) {
                            part.setContentId(contentId);
                        }
                        byte[] contentLocation = mtkPduPersister.getByteArrayFromPartColumn(c, 4);
                        if (contentLocation != null) {
                            part.setContentLocation(contentLocation);
                        }
                        byte[] contentType = mtkPduPersister.getByteArrayFromPartColumn(c, 5);
                        if (contentType != null) {
                            part.setContentType(contentType);
                            byte[] fileName = mtkPduPersister.getByteArrayFromPartColumn2(c, 6);
                            if (fileName != null) {
                                part.setFilename(fileName);
                            }
                            byte[] name = mtkPduPersister.getByteArrayFromPartColumn2(c, 7);
                            if (name != null) {
                                part.setName(name);
                            }
                            long partId = c.getLong(0);
                            StringBuilder sb = new StringBuilder();
                            sb.append("content://mms/part/");
                            long partId2 = partId;
                            sb.append(partId2);
                            Uri partURI = Uri.parse(sb.toString());
                            part.setDataUri(partURI);
                            String type = toIsoString(contentType);
                            if (!ContentType.isImageType(type)) {
                                if (!ContentType.isAudioType(type)) {
                                    if (!ContentType.isVideoType(type)) {
                                        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                                        InputStream is = null;
                                        if ("text/plain".equals(type) || "application/smil".equals(type)) {
                                            baos = baos2;
                                        } else if ("text/html".equals(type)) {
                                            baos = baos2;
                                        } else {
                                            try {
                                                InputStream is2 = mtkPduPersister.mContentResolver.openInputStream(partURI);
                                                try {
                                                    buffer = new byte[256];
                                                } catch (IOException e2) {
                                                    e = e2;
                                                    is = is2;
                                                    try {
                                                        Log.e(TAG, "Failed to load part data", e);
                                                        c.close();
                                                        throw new MmsException(e);
                                                    } catch (Throwable e3) {
                                                        iOException = e3;
                                                        if (is != null) {
                                                        }
                                                        throw iOException;
                                                    }
                                                } catch (Throwable th) {
                                                    is = is2;
                                                    iOException = th;
                                                    if (is != null) {
                                                    }
                                                    throw iOException;
                                                }
                                                try {
                                                    int len = is2.read(buffer);
                                                    while (len >= 0) {
                                                        try {
                                                            baos2.write(buffer, 0, len);
                                                            len = is2.read(buffer);
                                                            baos2 = baos2;
                                                            partId2 = partId2;
                                                        } catch (IOException e4) {
                                                            e = e4;
                                                            is = is2;
                                                            Log.e(TAG, "Failed to load part data", e);
                                                            c.close();
                                                            throw new MmsException(e);
                                                        } catch (Throwable th2) {
                                                            is = is2;
                                                            iOException = th2;
                                                            if (is != null) {
                                                            }
                                                            throw iOException;
                                                        }
                                                    }
                                                    baos = baos2;
                                                    try {
                                                        is2.close();
                                                    } catch (IOException e5) {
                                                        Log.e(TAG, "Failed to close stream", e5);
                                                    }
                                                    part.setData(baos.toByteArray());
                                                } catch (IOException e6) {
                                                    e = e6;
                                                    is = is2;
                                                    Log.e(TAG, "Failed to load part data", e);
                                                    c.close();
                                                    throw new MmsException(e);
                                                } catch (Throwable th3) {
                                                    is = is2;
                                                    iOException = th3;
                                                    if (is != null) {
                                                    }
                                                    throw iOException;
                                                }
                                            } catch (IOException e7) {
                                                e = e7;
                                                Log.e(TAG, "Failed to load part data", e);
                                                c.close();
                                                throw new MmsException(e);
                                            } catch (Throwable th4) {
                                                iOException = th4;
                                                if (is != null) {
                                                    try {
                                                        is.close();
                                                    } catch (IOException e8) {
                                                        Log.e(TAG, "Failed to close stream", e8);
                                                    }
                                                }
                                                throw iOException;
                                            }
                                        }
                                        String text = c.getString(8);
                                        String str = "";
                                        if (charset != null) {
                                            int intValue = charset.intValue();
                                            if (text != null) {
                                                str = text;
                                            }
                                            blob = new MtkEncodedStringValue(intValue, str).getTextString();
                                        } else {
                                            if (text != null) {
                                                str = text;
                                            }
                                            blob = new MtkEncodedStringValue(str).getTextString();
                                        }
                                        baos.write(blob, 0, blob.length);
                                        part.setData(baos.toByteArray());
                                    }
                                }
                            }
                            parts[partIdx] = part;
                            mtkPduPersister = this;
                            partIdx++;
                            partCount = partCount;
                        } else {
                            throw new MmsException("Content-Type must be set.");
                        }
                    }
                    c.close();
                    return parts;
                }
            } catch (Throwable th5) {
                c.close();
                throw th5;
            }
        }
        if (c != null) {
            c.close();
        }
        return null;
    }

    public Uri persistPart(PduPart part, long msgId, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        Uri uri = Uri.parse("content://mms/" + msgId + "/part");
        ContentValues values = new ContentValues(8);
        String contentType = getPartContentType(part);
        if (contentType != null) {
            if ("image/jpg".equals(contentType)) {
                contentType = "image/jpeg";
            }
            values.put("ct", contentType);
            if ("application/smil".equals(contentType)) {
                values.put("seq", (Integer) -1);
            }
            int charset = part.getCharset();
            if (charset != 0 && !"application/smil".equals(contentType)) {
                values.put("chset", Integer.valueOf(charset));
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
                values.put(BipUtils.KEY_QOS_CID, (String) toIsoString(part.getContentId()));
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

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled) throws MmsException {
        return persist(pdu, uri, createThreadId, groupMmsEnabled, null);
    }

    public Uri persist(GenericPdu pdu, Uri uri) throws MmsException {
        return persist(pdu, uri, true, false);
    }

    /* access modifiers changed from: protected */
    public void loadRecipients(int addressType, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        EncodedStringValue[] array = addressMap.get(Integer.valueOf(addressType));
        if (array != null) {
            if (!excludeMyNumber || array.length != 1) {
                int[] SubIdList = SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList();
                for (EncodedStringValue vOrg : array) {
                    if (vOrg != null) {
                        String number = vOrg.getString();
                        Log.d(TAG, "length = " + SubIdList.length);
                        if (SubIdList.length == 0) {
                            recipients.add(number);
                        } else {
                            for (int subid : SubIdList) {
                                Log.d(TAG, "subid = " + subid);
                                String myNumber = excludeMyNumber ? this.mTelephonyManager.getLine1Number(subid) : null;
                                if ((myNumber == null || !PhoneNumberUtils.compare(number, myNumber)) && !recipients.contains(number)) {
                                    recipients.add(number);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Cursor getPendingMessages(long dueTime) {
        Uri.Builder uriBuilder = Telephony.MmsSms.PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        return SqliteWrapper.query(this.mContext, this.mContentResolver, uriBuilder.build(), (String[]) null, "err_type < ? AND due_time <= ? AND msg_id in ( SELECT msg_id FROM pdu  WHERE msg_box <>2  AND m_type <>132  )", new String[]{String.valueOf(10), String.valueOf(dueTime)}, "due_time");
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        return persist(pdu, uri, createThreadId, groupMmsEnabled, preOpenedFiles, null);
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles, String contentLocation) throws MmsException {
        long msgId;
        int i;
        int messageSize;
        PduBody body;
        long dummyId;
        Uri res;
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
                Log.d(TAG, "persist uri " + uri);
                PduHeaders header = pdu.getPduHeaders();
                ContentValues values = new ContentValues();
                for (Map.Entry<Integer, String> e3 : ENCODED_STRING_COLUMN_NAME_MAP.entrySet()) {
                    int field = e3.getKey().intValue();
                    EncodedStringValue encodedString = header.getEncodedStringValue(field);
                    if (encodedString != null) {
                        values.put(e3.getValue(), toIsoString(encodedString.getTextString()));
                        values.put((String) CHARSET_COLUMN_NAME_MAP.get(Integer.valueOf(field)), Integer.valueOf(encodedString.getCharacterSet()));
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
                        i = 130;
                        loadRecipients(151, recipients, addressMap, false);
                    } else if (msgType == 130 || msgType == 132) {
                        loadRecipients(MtkPduHeaders.STATE_SKIP_RETRYING, recipients, addressMap, false);
                        if (groupMmsEnabled) {
                            loadRecipients(151, recipients, addressMap, true);
                            loadRecipients(130, recipients, addressMap, true);
                            i = 130;
                        } else {
                            i = 130;
                        }
                    } else {
                        i = 130;
                    }
                    long threadId = 0;
                    if (createThreadId && !recipients.isEmpty()) {
                        threadId = Telephony.Threads.getOrCreateThreadId(this.mContext, recipients);
                    }
                    values.put("thread_id", Long.valueOf(threadId));
                } else {
                    i = 130;
                }
                Log.d(TAG, "persist part begin ");
                long dummyId2 = System.currentTimeMillis();
                boolean textOnly = true;
                if (pdu instanceof MultimediaMessagePdu) {
                    PduBody body2 = ((MultimediaMessagePdu) pdu).getBody();
                    if (body2 != null) {
                        int partsNum = body2.getPartsNum();
                        if (partsNum > 2) {
                            textOnly = false;
                        }
                        messageSize = 0;
                        int i4 = 0;
                        while (i4 < partsNum) {
                            PduPart part = body2.getPart(i4);
                            messageSize += part.getDataLength();
                            persistPart(part, dummyId2, preOpenedFiles);
                            String contentType = getPartContentType(part);
                            if (contentType != null && !"application/smil".equals(contentType) && !"text/plain".equals(contentType)) {
                                textOnly = false;
                            }
                            i4++;
                            partsNum = partsNum;
                        }
                        body = body2;
                    } else {
                        body = body2;
                        messageSize = 0;
                    }
                } else {
                    body = null;
                    messageSize = 0;
                }
                values.put("text_only", Integer.valueOf(textOnly ? 1 : 0));
                if (values.getAsInteger("m_size") == null) {
                    values.put("m_size", Integer.valueOf(messageSize));
                }
                Log.d(TAG, "persist pdu begin ");
                values.put("need_notify", (Boolean) false);
                if (existingUri) {
                    dummyId = dummyId2;
                    SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, (String) null, (String[]) null);
                    res = uri;
                } else {
                    dummyId = dummyId2;
                    res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
                    if (res != null) {
                        msgId = ContentUris.parseId(res);
                    } else {
                        throw new MmsException("persist() failed: return null.");
                    }
                }
                Log.d(TAG, "persist address begin ");
                int[] iArr3 = ADDRESS_FIELDS;
                for (int addrType2 : iArr3) {
                    EncodedStringValue[] array2 = addressMap.get(Integer.valueOf(addrType2));
                    if (array2 != null) {
                        persistAddress(msgId, addrType2, array2);
                    }
                }
                Log.d(TAG, "persist update part begin ");
                ContentValues values2 = new ContentValues(1);
                values2.put("mid", Long.valueOf(msgId));
                if (contentLocation == null) {
                    values2.put("need_notify", (Boolean) true);
                }
                SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + dummyId + "/part"), values2, (String) null, (String[]) null);
                if (contentLocation != null) {
                    SqliteWrapper.delete(this.mContext, this.mContentResolver, Telephony.Mms.CONTENT_URI, "m_type=? AND ct_l =?", new String[]{Integer.toString(i), contentLocation});
                }
                PDU_CACHE_INSTANCE.purge(uri);
                Log.d(TAG, "persist purge end ");
                if (existingUri) {
                    return res;
                }
                return Uri.parse(uri + "/" + msgId);
            }
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
        throw new MmsException("Uri may not be null.");
    }

    public GenericPdu load(Uri uri, boolean backupRestore) throws MmsException {
        Log.i("MMSLog", "load for backuprestore");
        this.mBackupRestore = backupRestore;
        return load(uri);
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean backupRestore) throws MmsException {
        Log.i("MMSLog", "persist for backuprestore");
        this.mBackupRestore = backupRestore;
        return persist(pdu, uri, true, false);
    }

    public Uri persistEx(GenericPdu pdu, Uri uri, HashMap<String, String> attach) throws MmsException {
        Log.i("MMSLog", "Call persist_ex 1");
        return persistForBackupRestore(pdu, uri, attach);
    }

    public Uri persistEx(GenericPdu pdu, Uri uri, boolean backupRestore, HashMap<String, String> attach) throws MmsException {
        Log.i("MMSLog", "Call persist_ex 2");
        this.mBackupRestore = backupRestore;
        return persistForBackupRestore(pdu, uri, attach);
    }

    /* JADX INFO: Multiple debug info for r11v2 long: [D('values' android.content.ContentValues), D('persistPdu_t' long)] */
    /* JADX INFO: Multiple debug info for r10v2 long: [D('persistAddress_t' long), D('addressMap' java.util.HashMap<java.lang.Integer, com.google.android.mms.pdu.EncodedStringValue[]>)] */
    /* JADX INFO: Multiple debug info for r11v3 android.content.ContentValues: [D('msgId' long), D('values' android.content.ContentValues)] */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0272  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x027b  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x028a  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x02d2  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x02df  */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x031a  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0332  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x034e  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x0397  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x03da  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0422  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x043b  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0196  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01d1  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x0243  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0255  */
    private Uri persistForBackupRestore(GenericPdu pdu, Uri uri, HashMap<String, String> attach) throws MmsException {
        long msgId;
        boolean existingUri;
        Throwable th;
        ContentValues values;
        int length;
        int i;
        HashSet<String> recipients;
        int msgType;
        int sub_id;
        int locked;
        int locked2;
        int sub_id2;
        ContentValues values2;
        HashMap<Integer, EncodedStringValue[]> addressMap;
        long msgId2;
        Uri res;
        int length2;
        int i2;
        EncodedStringValue[] array;
        EncodedStringValue[] array2;
        int[] iArr;
        int i3;
        EncodedStringValue[] array3;
        if (uri != null) {
            try {
                msgId = ContentUris.parseId(uri);
            } catch (NumberFormatException e) {
                msgId = -1;
            }
            existingUri = msgId != -1;
            if (existingUri || MESSAGE_BOX_MAP.get(uri) != null) {
                synchronized (PDU_CACHE_INSTANCE) {
                    try {
                        if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                            try {
                                PDU_CACHE_INSTANCE.wait();
                            } catch (InterruptedException e2) {
                                Log.e(TAG, "persist1: ", e2);
                            } catch (Throwable th2) {
                                th = th2;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                                throw th;
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } else {
                throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
            }
        } else {
            throw new MmsException("Uri may not be null.");
        }
        PDU_CACHE_INSTANCE.purge(uri);
        Log.d(TAG, "persist uri " + uri);
        PduHeaders header = pdu.getPduHeaders();
        values = new ContentValues();
        for (Map.Entry<Integer, String> e3 : ENCODED_STRING_COLUMN_NAME_MAP.entrySet()) {
            int field = e3.getKey().intValue();
            EncodedStringValue encodedString = header.getEncodedStringValue(field);
            if (encodedString != null) {
                values.put(e3.getValue(), toIsoString(encodedString.getTextString()));
                values.put((String) CHARSET_COLUMN_NAME_MAP.get(Integer.valueOf(field)), Integer.valueOf(encodedString.getCharacterSet()));
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
        if (this.mBackupRestore) {
            int read = 0;
            if (attach != null) {
                try {
                    read = Integer.parseInt(attach.get("read"));
                } catch (NumberFormatException e6) {
                    Log.d(TAG, e6.toString());
                } catch (Exception e7) {
                    Log.d(TAG, e7.toString());
                }
            }
            values.put("read", Integer.valueOf(read));
            long size = 0;
            if (!(attach == null || attach.get("m_size") == null)) {
                size = Long.parseLong(attach.get("m_size"));
            }
            values.put("m_size", Long.valueOf(size));
        }
        for (Map.Entry<Integer, String> e8 : LONG_COLUMN_NAME_MAP.entrySet()) {
            long l = header.getLongInteger(e8.getKey().intValue());
            if (l != -1) {
                values.put(e8.getValue(), Long.valueOf(l));
            }
        }
        HashMap<Integer, EncodedStringValue[]> addressMap2 = new HashMap<>(ADDRESS_FIELDS.length);
        int[] iArr2 = ADDRESS_FIELDS;
        length = iArr2.length;
        i = 0;
        while (i < length) {
            int addrType = iArr2[i];
            if (addrType == 137) {
                EncodedStringValue v = header.getEncodedStringValue(addrType);
                if (v != null) {
                    iArr = iArr2;
                    i3 = length;
                    array3 = new EncodedStringValue[]{v};
                } else {
                    iArr = iArr2;
                    i3 = length;
                    array3 = null;
                }
            } else {
                iArr = iArr2;
                i3 = length;
                array3 = header.getEncodedStringValues(addrType);
            }
            addressMap2.put(Integer.valueOf(addrType), array3);
            i++;
            length = i3;
            iArr2 = iArr;
        }
        recipients = new HashSet<>();
        msgType = pdu.getMessageType();
        if (msgType != 130 || msgType == 132 || msgType == 128) {
            if (msgType != 128) {
                array = addressMap2.get(151);
            } else if (msgType == 130 || msgType == 132) {
                array = addressMap2.get(Integer.valueOf((int) MtkPduHeaders.STATE_SKIP_RETRYING));
            } else {
                array = null;
            }
            if (array == null) {
                int length3 = array.length;
                int i4 = 0;
                while (i4 < length3) {
                    EncodedStringValue v2 = array[i4];
                    if (v2 != null) {
                        array2 = array;
                        recipients.add(v2.getString());
                    } else {
                        array2 = array;
                    }
                    i4++;
                    array = array2;
                }
            }
            long time_1 = System.currentTimeMillis();
            String backupRestore = null;
            if (attach != null) {
                backupRestore = attach.get("index");
            }
            if (!recipients.isEmpty()) {
                values.put("thread_id", Long.valueOf(MtkTelephony.MtkThreads.getOrCreateThreadId(this.mContext, recipients, backupRestore)));
            }
            Log.d("MMSLog", "BR_TEST: getThreadId=" + (System.currentTimeMillis() - time_1));
        }
        long time_12 = System.currentTimeMillis();
        Log.d(TAG, "persist pdu begin ");
        values.put("need_notify", (Boolean) true);
        if (this.mBackupRestore) {
            values.put("seen", (Integer) 1);
        }
        sub_id = -1;
        locked = 0;
        if (attach == null) {
            try {
                sub_id = Integer.parseInt(attach.get("sub_id"));
                locked = Integer.parseInt(attach.get("locked"));
            } catch (NumberFormatException e9) {
                Log.d(TAG, e9.toString());
            } catch (Exception e10) {
                Log.d(TAG, e10.toString());
                sub_id2 = sub_id;
                locked2 = 0;
                values.put("sub_id", Integer.valueOf(sub_id2));
                values.put("locked", Integer.valueOf(locked2));
                if (!existingUri) {
                    addressMap = addressMap2;
                    values2 = values;
                    SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, (String) null, (String[]) null);
                    res = uri;
                    msgId2 = msgId;
                } else {
                    addressMap = addressMap2;
                    values2 = values;
                    res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values2);
                    if (res != null) {
                        msgId2 = ContentUris.parseId(res);
                    } else {
                        throw new MmsException("persist() failed: return null.");
                    }
                }
                Log.d("MMSLog", "BR_TEST: parse time persistPdu=" + (System.currentTimeMillis() - time_12));
                Log.d(TAG, "persist address begin ");
                long time_13 = System.currentTimeMillis();
                int[] iArr3 = ADDRESS_FIELDS;
                length2 = iArr3.length;
                i2 = 0;
                while (i2 < length2) {
                    int addrType2 = iArr3[i2];
                    EncodedStringValue[] array4 = addressMap.get(Integer.valueOf(addrType2));
                    if (array4 != null) {
                        persistAddress(msgId2, addrType2, array4);
                    }
                    i2++;
                    iArr3 = iArr3;
                }
                Log.d("MMSLog", "BR_TEST: parse time persistAddress=" + (System.currentTimeMillis() - time_13));
                Log.d(TAG, "persist part begin ");
                if (pdu instanceof MultimediaMessagePdu) {
                    PduBody body = ((MultimediaMessagePdu) pdu).getBody();
                    if (body != null) {
                        int partsNum = body.getPartsNum();
                        long time_14 = System.currentTimeMillis();
                        int i5 = 0;
                        while (i5 < partsNum) {
                            persistPart(body.getPart(i5), msgId2, null);
                            i5++;
                            body = body;
                        }
                        Log.d("MMSLog", "BR_TEST: parse time PersistPart=" + (System.currentTimeMillis() - time_14));
                    }
                }
                if (!existingUri) {
                    return res;
                }
                return Uri.parse(uri + "/" + msgId2);
            }
            sub_id2 = sub_id;
            locked2 = locked;
        } else {
            sub_id2 = -1;
            locked2 = 0;
        }
        values.put("sub_id", Integer.valueOf(sub_id2));
        values.put("locked", Integer.valueOf(locked2));
        if (!existingUri) {
        }
        Log.d("MMSLog", "BR_TEST: parse time persistPdu=" + (System.currentTimeMillis() - time_12));
        Log.d(TAG, "persist address begin ");
        long time_132 = System.currentTimeMillis();
        int[] iArr32 = ADDRESS_FIELDS;
        length2 = iArr32.length;
        i2 = 0;
        while (i2 < length2) {
        }
        Log.d("MMSLog", "BR_TEST: parse time persistAddress=" + (System.currentTimeMillis() - time_132));
        Log.d(TAG, "persist part begin ");
        if (pdu instanceof MultimediaMessagePdu) {
        }
        if (!existingUri) {
        }
        sub_id2 = sub_id;
        locked2 = locked;
        values.put("sub_id", Integer.valueOf(sub_id2));
        values.put("locked", Integer.valueOf(locked2));
        if (!existingUri) {
        }
        Log.d("MMSLog", "BR_TEST: parse time persistPdu=" + (System.currentTimeMillis() - time_12));
        Log.d(TAG, "persist address begin ");
        long time_1322 = System.currentTimeMillis();
        int[] iArr322 = ADDRESS_FIELDS;
        length2 = iArr322.length;
        i2 = 0;
        while (i2 < length2) {
        }
        Log.d("MMSLog", "BR_TEST: parse time persistAddress=" + (System.currentTimeMillis() - time_1322));
        Log.d(TAG, "persist part begin ");
        if (pdu instanceof MultimediaMessagePdu) {
        }
        if (!existingUri) {
        }
        values.put("read", Integer.valueOf(read));
        long size2 = 0;
        size2 = Long.parseLong(attach.get("m_size"));
        values.put("m_size", Long.valueOf(size2));
        while (r0.hasNext()) {
        }
        HashMap<Integer, EncodedStringValue[]> addressMap22 = new HashMap<>(ADDRESS_FIELDS.length);
        int[] iArr22 = ADDRESS_FIELDS;
        length = iArr22.length;
        i = 0;
        while (i < length) {
        }
        recipients = new HashSet<>();
        msgType = pdu.getMessageType();
        if (msgType != 130) {
        }
        if (msgType != 128) {
        }
        if (array == null) {
        }
        long time_15 = System.currentTimeMillis();
        String backupRestore2 = null;
        if (attach != null) {
        }
        if (!recipients.isEmpty()) {
        }
        Log.d("MMSLog", "BR_TEST: getThreadId=" + (System.currentTimeMillis() - time_15));
        long time_122 = System.currentTimeMillis();
        Log.d(TAG, "persist pdu begin ");
        values.put("need_notify", (Boolean) true);
        if (this.mBackupRestore) {
        }
        sub_id = -1;
        locked = 0;
        if (attach == null) {
        }
        values.put("sub_id", Integer.valueOf(sub_id2));
        values.put("locked", Integer.valueOf(locked2));
        if (!existingUri) {
        }
        Log.d("MMSLog", "BR_TEST: parse time persistPdu=" + (System.currentTimeMillis() - time_122));
        Log.d(TAG, "persist address begin ");
        long time_13222 = System.currentTimeMillis();
        int[] iArr3222 = ADDRESS_FIELDS;
        length2 = iArr3222.length;
        i2 = 0;
        while (i2 < length2) {
        }
        Log.d("MMSLog", "BR_TEST: parse time persistAddress=" + (System.currentTimeMillis() - time_13222));
        Log.d(TAG, "persist part begin ");
        if (pdu instanceof MultimediaMessagePdu) {
        }
        if (!existingUri) {
        }
    }
}
