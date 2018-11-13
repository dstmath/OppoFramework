package com.google.android.mms.pdu;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Threads;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.util.DownloadDrmHelper;
import com.google.android.mms.util.DrmConvertSession;
import com.google.android.mms.util.PduCache;
import com.google.android.mms.util.PduCacheEntry;
import com.google.android.mms.util.SqliteWrapper;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

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
public class PduPersister {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f34-assertionsDisabled = false;
    private static final int[] ADDRESS_FIELDS = null;
    private static final HashMap<Integer, Integer> CHARSET_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> CHARSET_COLUMN_NAME_MAP = null;
    private static final boolean DEBUG = false;
    private static final long DUMMY_THREAD_ID = Long.MAX_VALUE;
    private static final HashMap<Integer, Integer> ENCODED_STRING_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> ENCODED_STRING_COLUMN_NAME_MAP = null;
    private static final boolean LOCAL_LOGV = false;
    private static final HashMap<Integer, Integer> LONG_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> LONG_COLUMN_NAME_MAP = null;
    private static final HashMap<Uri, Integer> MESSAGE_BOX_MAP = null;
    private static final HashMap<Integer, Integer> OCTET_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> OCTET_COLUMN_NAME_MAP = null;
    private static final int PART_COLUMN_CHARSET = 1;
    private static final int PART_COLUMN_CONTENT_DISPOSITION = 2;
    private static final int PART_COLUMN_CONTENT_ID = 3;
    private static final int PART_COLUMN_CONTENT_LOCATION = 4;
    private static final int PART_COLUMN_CONTENT_TYPE = 5;
    private static final int PART_COLUMN_FILENAME = 6;
    private static final int PART_COLUMN_ID = 0;
    private static final int PART_COLUMN_NAME = 7;
    private static final int PART_COLUMN_TEXT = 8;
    private static final String[] PART_PROJECTION = null;
    private static final PduCache PDU_CACHE_INSTANCE = null;
    private static final int PDU_COLUMN_CONTENT_CLASS = 11;
    private static final int PDU_COLUMN_CONTENT_LOCATION = 5;
    private static final int PDU_COLUMN_CONTENT_TYPE = 6;
    private static final int PDU_COLUMN_DATE = 21;
    private static final int PDU_COLUMN_DATE_SENT = 28;
    private static final int PDU_COLUMN_DELIVERY_REPORT = 12;
    private static final int PDU_COLUMN_DELIVERY_TIME = 22;
    private static final int PDU_COLUMN_EXPIRY = 23;
    private static final int PDU_COLUMN_ID = 0;
    private static final int PDU_COLUMN_MESSAGE_BOX = 1;
    private static final int PDU_COLUMN_MESSAGE_CLASS = 7;
    private static final int PDU_COLUMN_MESSAGE_ID = 8;
    private static final int PDU_COLUMN_MESSAGE_SIZE = 24;
    private static final int PDU_COLUMN_MESSAGE_TYPE = 13;
    private static final int PDU_COLUMN_MMS_VERSION = 14;
    private static final int PDU_COLUMN_PRIORITY = 15;
    private static final int PDU_COLUMN_READ = 27;
    private static final int PDU_COLUMN_READ_REPORT = 16;
    private static final int PDU_COLUMN_READ_STATUS = 17;
    private static final int PDU_COLUMN_REPORT_ALLOWED = 18;
    private static final int PDU_COLUMN_RESPONSE_TEXT = 9;
    private static final int PDU_COLUMN_RETRIEVE_STATUS = 19;
    private static final int PDU_COLUMN_RETRIEVE_TEXT = 3;
    private static final int PDU_COLUMN_RETRIEVE_TEXT_CHARSET = 26;
    private static final int PDU_COLUMN_STATUS = 20;
    private static final int PDU_COLUMN_SUBJECT = 4;
    private static final int PDU_COLUMN_SUBJECT_CHARSET = 25;
    private static final int PDU_COLUMN_THREAD_ID = 2;
    private static final int PDU_COLUMN_TRANSACTION_ID = 10;
    private static final String[] PDU_PROJECTION = null;
    public static final int PROC_STATUS_COMPLETED = 3;
    public static final int PROC_STATUS_PERMANENTLY_FAILURE = 2;
    public static final int PROC_STATUS_TRANSIENT_FAILURE = 1;
    private static final String TAG = "PduPersister";
    public static final String TEMPORARY_DRM_OBJECT_URI = "content://mms/9223372036854775807/part";
    private static final HashMap<Integer, Integer> TEXT_STRING_COLUMN_INDEX_MAP = null;
    private static final HashMap<Integer, String> TEXT_STRING_COLUMN_NAME_MAP = null;
    private static PduPersister sPersister;
    private boolean mBackupRestore;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final DrmManagerClient mDrmManagerClient;
    private final TelephonyManager mTelephonyManager;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.google.android.mms.pdu.PduPersister.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.google.android.mms.pdu.PduPersister.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.pdu.PduPersister.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.google.android.mms.pdu.PduPersister.persistForBackupRestore(com.google.android.mms.pdu.GenericPdu, android.net.Uri, java.util.HashMap):android.net.Uri, dex: 
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
    private android.net.Uri persistForBackupRestore(com.google.android.mms.pdu.GenericPdu r1, android.net.Uri r2, java.util.HashMap<java.lang.String, java.lang.String> r3) throws com.google.android.mms.MmsException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.google.android.mms.pdu.PduPersister.persistForBackupRestore(com.google.android.mms.pdu.GenericPdu, android.net.Uri, java.util.HashMap):android.net.Uri, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.pdu.PduPersister.persistForBackupRestore(com.google.android.mms.pdu.GenericPdu, android.net.Uri, java.util.HashMap):android.net.Uri");
    }

    private PduPersister(Context context) {
        this.mBackupRestore = false;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mDrmManagerClient = new DrmManagerClient(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public static PduPersister getPduPersister(Context context) {
        if (sPersister == null) {
            sPersister = new PduPersister(context);
        } else if (!context.equals(sPersister.mContext)) {
            sPersister.release();
            sPersister = new PduPersister(context);
        }
        return sPersister;
    }

    private void setEncodedStringValueToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null && s.length() > 0) {
            headers.setEncodedStringValue(new EncodedStringValue(c.getInt(((Integer) CHARSET_COLUMN_INDEX_MAP.get(Integer.valueOf(mapColumn))).intValue()), getBytes(s)), mapColumn);
        }
    }

    private void setTextStringToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        String s = c.getString(columnIndex);
        if (s != null) {
            headers.setTextString(getBytes(s), mapColumn);
        }
    }

    private void setOctetToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) throws InvalidHeaderValueException {
        if (!c.isNull(columnIndex)) {
            headers.setOctet(c.getInt(columnIndex), mapColumn);
        }
    }

    private void setLongToHeaders(Cursor c, int columnIndex, PduHeaders headers, int mapColumn) {
        if (!c.isNull(columnIndex)) {
            headers.setLongInteger(c.getLong(columnIndex), mapColumn);
        }
    }

    private Integer getIntegerFromPartColumn(Cursor c, int columnIndex) {
        if (c.isNull(columnIndex)) {
            return null;
        }
        return Integer.valueOf(c.getInt(columnIndex));
    }

    private byte[] getByteArrayFromPartColumn(Cursor c, int columnIndex) {
        if (c.isNull(columnIndex)) {
            return null;
        }
        return getBytes(c.getString(columnIndex));
    }

    private byte[] getByteArrayFromPartColumn2(Cursor c, int columnIndex) {
        if (c.isNull(columnIndex)) {
            return null;
        }
        return c.getString(columnIndex).getBytes();
    }

    private PduPart[] loadParts(long msgId) throws MmsException {
        Cursor c = SqliteWrapper.query(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + msgId + "/part"), PART_PROJECTION, null, null, null);
        if (c != null) {
            try {
                if (c.getCount() != 0) {
                    int partIdx = 0;
                    PduPart[] parts = new PduPart[c.getCount()];
                    while (true) {
                        int partIdx2 = partIdx;
                        if (c.moveToNext()) {
                            PduPart part = new PduPart();
                            Integer charset = getIntegerFromPartColumn(c, 1);
                            if (charset != null) {
                                part.setCharset(charset.intValue());
                            }
                            byte[] contentDisposition = getByteArrayFromPartColumn(c, 2);
                            if (contentDisposition != null) {
                                part.setContentDisposition(contentDisposition);
                            }
                            byte[] contentId = getByteArrayFromPartColumn(c, 3);
                            if (contentId != null) {
                                part.setContentId(contentId);
                            }
                            byte[] contentLocation = getByteArrayFromPartColumn(c, 4);
                            if (contentLocation != null) {
                                part.setContentLocation(contentLocation);
                            }
                            byte[] contentType = getByteArrayFromPartColumn(c, 5);
                            if (contentType != null) {
                                part.setContentType(contentType);
                                byte[] fileName = getByteArrayFromPartColumn(c, 6);
                                if (fileName != null) {
                                    part.setFilename(fileName);
                                }
                                byte[] name = getByteArrayFromPartColumn(c, 7);
                                if (name != null) {
                                    part.setName(name);
                                }
                                Uri partURI = Uri.parse("content://mms/part/" + c.getLong(0));
                                part.setDataUri(partURI);
                                String type = toIsoString(contentType);
                                if (!(ContentType.isImageType(type) || ContentType.isAudioType(type))) {
                                    if (ContentType.isVideoType(type)) {
                                        continue;
                                    } else {
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        InputStream inputStream = null;
                                        if (ContentType.TEXT_PLAIN.equals(type) || ContentType.APP_SMIL.equals(type) || ContentType.TEXT_HTML.equals(type)) {
                                            byte[] blob;
                                            String text = c.getString(8);
                                            if (charset != null) {
                                                int intValue = charset.intValue();
                                                if (text == null) {
                                                    text = UsimPBMemInfo.STRING_NOT_SET;
                                                }
                                                blob = new EncodedStringValue(intValue, text).getTextString();
                                            } else {
                                                if (text == null) {
                                                    text = UsimPBMemInfo.STRING_NOT_SET;
                                                }
                                                blob = new EncodedStringValue(text).getTextString();
                                            }
                                            baos.write(blob, 0, blob.length);
                                        } else {
                                            try {
                                                inputStream = this.mContentResolver.openInputStream(partURI);
                                                byte[] buffer = new byte[256];
                                                for (int len = inputStream.read(buffer); len >= 0; len = inputStream.read(buffer)) {
                                                    baos.write(buffer, 0, len);
                                                }
                                                if (inputStream != null) {
                                                    inputStream.close();
                                                }
                                            } catch (Throwable e) {
                                                Log.e(TAG, "Failed to load part data", e);
                                                c.close();
                                                throw new MmsException(e);
                                            } catch (Throwable th) {
                                                if (inputStream != null) {
                                                    try {
                                                        inputStream.close();
                                                    } catch (Throwable e2) {
                                                        Log.e(TAG, "Failed to close stream", e2);
                                                    }
                                                }
                                            }
                                        }
                                        part.setData(baos.toByteArray());
                                    }
                                }
                                partIdx = partIdx2 + 1;
                                parts[partIdx2] = part;
                            } else {
                                throw new MmsException("Content-Type must be set.");
                            }
                        }
                        if (c != null) {
                            c.close();
                        }
                        return parts;
                    }
                }
            } catch (Throwable e22) {
                Log.e(TAG, "Failed to close stream", e22);
            } catch (Throwable th2) {
                if (c != null) {
                    c.close();
                }
            }
        }
        if (c != null) {
            c.close();
        }
        return null;
    }

    private void loadAddress(long msgId, PduHeaders headers) {
        Context context = this.mContext;
        ContentResolver contentResolver = this.mContentResolver;
        Uri parse = Uri.parse("content://mms/" + msgId + "/addr");
        String[] strArr = new String[3];
        strArr[0] = "address";
        strArr[1] = "charset";
        strArr[2] = "type";
        Cursor c = SqliteWrapper.query(context, contentResolver, parse, strArr, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                try {
                    String addr = c.getString(0);
                    if (!TextUtils.isEmpty(addr)) {
                        int addrType = c.getInt(2);
                        switch (addrType) {
                            case 129:
                            case 130:
                            case 151:
                                headers.appendEncodedStringValue(new EncodedStringValue(c.getInt(1), getBytes(addr)), addrType);
                                break;
                            case 137:
                                headers.setEncodedStringValue(new EncodedStringValue(c.getInt(1), getBytes(addr)), addrType);
                                break;
                            default:
                                Log.e(TAG, "Unknown address type: " + addrType);
                                break;
                        }
                    }
                } finally {
                    c.close();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0052 A:{SYNTHETIC} */
    /* JADX WARNING: Missing block: B:43:0x006e, code:
            r13 = com.google.android.mms.util.SqliteWrapper.query(r32.mContext, r32.mContentResolver, r33, PDU_PROJECTION, null, null, null);
            r19 = new com.google.android.mms.pdu.PduHeaders();
            r22 = android.content.ContentUris.parseId(r33);
     */
    /* JADX WARNING: Missing block: B:44:0x008a, code:
            if (r13 == null) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:47:0x0091, code:
            if (r13.getCount() == 1) goto L_0x00b9;
     */
    /* JADX WARNING: Missing block: B:49:0x00ae, code:
            throw new com.google.android.mms.MmsException("Bad uri: " + r33);
     */
    /* JADX WARNING: Missing block: B:59:0x00bd, code:
            if (r13.moveToFirst() == false) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:60:0x00bf, code:
            r21 = r13.getInt(1);
            r30 = r13.getLong(2);
            r18 = ENCODED_STRING_COLUMN_INDEX_MAP.entrySet().iterator();
     */
    /* JADX WARNING: Missing block: B:62:0x00d7, code:
            if (r18.hasNext() == false) goto L_0x00fb;
     */
    /* JADX WARNING: Missing block: B:63:0x00d9, code:
            r17 = (java.util.Map.Entry) r18.next();
            setEncodedStringValueToHeaders(r13, ((java.lang.Integer) r17.getValue()).intValue(), r19, ((java.lang.Integer) r17.getKey()).intValue());
     */
    /* JADX WARNING: Missing block: B:64:0x00fb, code:
            r18 = TEXT_STRING_COLUMN_INDEX_MAP.entrySet().iterator();
     */
    /* JADX WARNING: Missing block: B:66:0x0109, code:
            if (r18.hasNext() == false) goto L_0x012d;
     */
    /* JADX WARNING: Missing block: B:67:0x010b, code:
            r17 = (java.util.Map.Entry) r18.next();
            setTextStringToHeaders(r13, ((java.lang.Integer) r17.getValue()).intValue(), r19, ((java.lang.Integer) r17.getKey()).intValue());
     */
    /* JADX WARNING: Missing block: B:68:0x012d, code:
            r18 = OCTET_COLUMN_INDEX_MAP.entrySet().iterator();
     */
    /* JADX WARNING: Missing block: B:70:0x013b, code:
            if (r18.hasNext() == false) goto L_0x015f;
     */
    /* JADX WARNING: Missing block: B:71:0x013d, code:
            r17 = (java.util.Map.Entry) r18.next();
            setOctetToHeaders(r13, ((java.lang.Integer) r17.getValue()).intValue(), r19, ((java.lang.Integer) r17.getKey()).intValue());
     */
    /* JADX WARNING: Missing block: B:72:0x015f, code:
            r18 = LONG_COLUMN_INDEX_MAP.entrySet().iterator();
     */
    /* JADX WARNING: Missing block: B:74:0x016d, code:
            if (r18.hasNext() == false) goto L_0x0191;
     */
    /* JADX WARNING: Missing block: B:75:0x016f, code:
            r17 = (java.util.Map.Entry) r18.next();
            setLongToHeaders(r13, ((java.lang.Integer) r17.getValue()).intValue(), r19, ((java.lang.Integer) r17.getKey()).intValue());
     */
    /* JADX WARNING: Missing block: B:77:0x0195, code:
            if (r32.mBackupRestore == false) goto L_0x01d4;
     */
    /* JADX WARNING: Missing block: B:78:0x0197, code:
            android.util.Log.i(TAG, "load for backuprestore");
     */
    /* JADX WARNING: Missing block: B:79:0x01a6, code:
            if (r13.isNull(27) != false) goto L_0x01d4;
     */
    /* JADX WARNING: Missing block: B:80:0x01a8, code:
            r11 = r13.getInt(27);
            android.util.Log.i(TAG, "read value=" + r11);
     */
    /* JADX WARNING: Missing block: B:81:0x01c9, code:
            if (r11 != 1) goto L_0x01d4;
     */
    /* JADX WARNING: Missing block: B:82:0x01cb, code:
            r19.setOctet(128, 155);
     */
    /* JADX WARNING: Missing block: B:83:0x01d4, code:
            if (r13 == null) goto L_0x01d9;
     */
    /* JADX WARNING: Missing block: B:85:?, code:
            r13.close();
     */
    /* JADX WARNING: Missing block: B:87:0x01dd, code:
            if (r22 != -1) goto L_0x01e8;
     */
    /* JADX WARNING: Missing block: B:89:0x01e7, code:
            throw new com.google.android.mms.MmsException("Error! ID of the message: -1.");
     */
    /* JADX WARNING: Missing block: B:90:0x01e8, code:
            loadAddress(r22, r19);
            r24 = r19.getOctet(140);
            r12 = new com.google.android.mms.pdu.PduBody();
     */
    /* JADX WARNING: Missing block: B:91:0x0202, code:
            if (r24 == 132) goto L_0x020a;
     */
    /* JADX WARNING: Missing block: B:93:0x0208, code:
            if (r24 != 128) goto L_0x0229;
     */
    /* JADX WARNING: Missing block: B:94:0x020a, code:
            r25 = loadParts(r22);
     */
    /* JADX WARNING: Missing block: B:95:0x0212, code:
            if (r25 == null) goto L_0x0229;
     */
    /* JADX WARNING: Missing block: B:96:0x0214, code:
            r26 = r25.length;
            r20 = 0;
     */
    /* JADX WARNING: Missing block: B:98:0x021f, code:
            if (r20 >= r26) goto L_0x0229;
     */
    /* JADX WARNING: Missing block: B:99:0x0221, code:
            r12.addPart(r25[r20]);
            r20 = r20 + 1;
     */
    /* JADX WARNING: Missing block: B:100:0x0229, code:
            switch(r24) {
                case 128: goto L_0x0291;
                case 129: goto L_0x02b9;
                case 130: goto L_0x024a;
                case 131: goto L_0x02a5;
                case 132: goto L_0x0287;
                case 133: goto L_0x029b;
                case 134: goto L_0x0273;
                case 135: goto L_0x02af;
                case 136: goto L_0x027d;
                case 137: goto L_0x02b9;
                case 138: goto L_0x02b9;
                case 139: goto L_0x02b9;
                case 140: goto L_0x02b9;
                case 141: goto L_0x02b9;
                case 142: goto L_0x02b9;
                case 143: goto L_0x02b9;
                case 144: goto L_0x02b9;
                case 145: goto L_0x02b9;
                case 146: goto L_0x02b9;
                case 147: goto L_0x02b9;
                case 148: goto L_0x02b9;
                case 149: goto L_0x02b9;
                case 150: goto L_0x02b9;
                case 151: goto L_0x02b9;
                default: goto L_0x022c;
            };
     */
    /* JADX WARNING: Missing block: B:102:0x0249, code:
            throw new com.google.android.mms.MmsException("Unrecognized PDU type: " + java.lang.Integer.toHexString(r24));
     */
    /* JADX WARNING: Missing block: B:103:0x024a, code:
            r0 = new com.google.android.mms.pdu.NotificationInd(r19);
     */
    /* JADX WARNING: Missing block: B:104:0x0253, code:
            r5 = PDU_CACHE_INSTANCE;
     */
    /* JADX WARNING: Missing block: B:105:0x0255, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:106:0x0256, code:
            if (r27 == null) goto L_0x0304;
     */
    /* JADX WARNING: Missing block: B:109:0x025a, code:
            if (-assertionsDisabled != false) goto L_0x02d9;
     */
    /* JADX WARNING: Missing block: B:111:0x0264, code:
            if (PDU_CACHE_INSTANCE.get(r33) != null) goto L_0x02d7;
     */
    /* JADX WARNING: Missing block: B:112:0x0266, code:
            r4 = 1;
     */
    /* JADX WARNING: Missing block: B:113:0x0267, code:
            if (r4 != null) goto L_0x02d9;
     */
    /* JADX WARNING: Missing block: B:115:0x026e, code:
            throw new java.lang.AssertionError();
     */
    /* JADX WARNING: Missing block: B:116:0x026f, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:117:0x0270, code:
            r14 = r15;
     */
    /* JADX WARNING: Missing block: B:118:0x0271, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:119:0x0272, code:
            throw r4;
     */
    /* JADX WARNING: Missing block: B:121:?, code:
            r0 = new com.google.android.mms.pdu.DeliveryInd(r19);
     */
    /* JADX WARNING: Missing block: B:122:0x027d, code:
            r0 = new com.google.android.mms.pdu.ReadOrigInd(r19);
     */
    /* JADX WARNING: Missing block: B:123:0x0287, code:
            r0 = new com.google.android.mms.pdu.RetrieveConf(r19, r12);
     */
    /* JADX WARNING: Missing block: B:124:0x0291, code:
            r0 = new com.google.android.mms.pdu.SendReq(r19, r12);
     */
    /* JADX WARNING: Missing block: B:125:0x029b, code:
            r0 = new com.google.android.mms.pdu.AcknowledgeInd(r19);
     */
    /* JADX WARNING: Missing block: B:126:0x02a5, code:
            r0 = new com.google.android.mms.pdu.NotifyRespInd(r19);
     */
    /* JADX WARNING: Missing block: B:127:0x02af, code:
            r0 = new com.google.android.mms.pdu.ReadRecInd(r19);
     */
    /* JADX WARNING: Missing block: B:129:0x02d6, code:
            throw new com.google.android.mms.MmsException("Unsupported PDU type: " + java.lang.Integer.toHexString(r24));
     */
    /* JADX WARNING: Missing block: B:130:0x02d7, code:
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:134:?, code:
            PDU_CACHE_INSTANCE.put(r33, new com.google.android.mms.util.PduCacheEntry(r27, r21, r30));
     */
    /* JADX WARNING: Missing block: B:135:0x02eb, code:
            PDU_CACHE_INSTANCE.setUpdating(r33, false);
            PDU_CACHE_INSTANCE.notifyAll();
     */
    /* JADX WARNING: Missing block: B:136:0x02f8, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:137:0x02f9, code:
            return r27;
     */
    /* JADX WARNING: Missing block: B:141:0x02fd, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:144:0x0304, code:
            r14 = r15;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public GenericPdu load(Uri uri) throws MmsException {
        Throwable th;
        PduCacheEntry cacheEntry = null;
        try {
            synchronized (PDU_CACHE_INSTANCE) {
                try {
                    if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                        PDU_CACHE_INSTANCE.wait();
                        cacheEntry = (PduCacheEntry) PDU_CACHE_INSTANCE.get(uri);
                        if (cacheEntry != null) {
                            GenericPdu pdu = cacheEntry.getPdu();
                            synchronized (PDU_CACHE_INSTANCE) {
                                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                                PDU_CACHE_INSTANCE.notifyAll();
                            }
                            return pdu;
                        }
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "load: ", e);
                } catch (Throwable th2) {
                    th = th2;
                }
                PduCacheEntry cacheEntry2 = cacheEntry;
                try {
                    PDU_CACHE_INSTANCE.setUpdating(uri, true);
                } catch (Throwable th3) {
                    th = th3;
                    cacheEntry = cacheEntry2;
                    throw th;
                }
                try {
                } catch (Throwable th4) {
                    th = th4;
                    cacheEntry = cacheEntry2;
                    synchronized (PDU_CACHE_INSTANCE) {
                        PDU_CACHE_INSTANCE.setUpdating(uri, false);
                        PDU_CACHE_INSTANCE.notifyAll();
                    }
                    throw th;
                }
            }
        } catch (Throwable th5) {
            th = th5;
            synchronized (PDU_CACHE_INSTANCE) {
            }
            throw th;
        }
    }

    private void persistAddress(long msgId, int type, EncodedStringValue[] array) {
        ArrayList<String> strValues = new ArrayList();
        ContentValues values = new ContentValues();
        Uri uri = Uri.parse("content://mms/" + msgId + "/addr");
        if (array != null) {
            for (EncodedStringValue addr : array) {
                strValues.add(toIsoString(addr.getTextString()));
                strValues.add(String.valueOf(addr.getCharacterSet()));
                strValues.add(String.valueOf(type));
            }
            values.putStringArrayList("addresses", strValues);
            SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
        }
    }

    private static String getPartContentType(PduPart part) {
        return part.getContentType() == null ? null : toIsoString(part.getContentType());
    }

    public Uri persistPart(PduPart part, long msgId, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        Uri uri = Uri.parse("content://mms/" + msgId + "/part");
        ContentValues values = new ContentValues(8);
        String contentType = getPartContentType(part);
        if (contentType != null) {
            if (ContentType.IMAGE_JPG.equals(contentType)) {
                contentType = ContentType.IMAGE_JPEG;
            }
            values.put("ct", contentType);
            if (ContentType.APP_SMIL.equals(contentType)) {
                values.put("seq", Integer.valueOf(-1));
            }
            int charset = part.getCharset();
            if (!(charset == 0 || ContentType.APP_SMIL.equals(contentType))) {
                values.put("chset", Integer.valueOf(charset));
            }
            if (part.getFilename() != null) {
                values.put("fn", new String(part.getFilename()));
            }
            if (part.getName() != null) {
                values.put("name", new String(part.getName()));
            }
            if (part.getContentDisposition() != null) {
                values.put("cd", toIsoString(part.getContentDisposition()));
            }
            if (part.getContentId() != null) {
                values.put("cid", toIsoString(part.getContentId()));
            }
            if (part.getContentLocation() != null) {
                values.put("cl", toIsoString(part.getContentLocation()));
            }
            Uri res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
            if (res == null) {
                throw new MmsException("Failed to persist part, return null.");
            }
            persistData(part, res, contentType, preOpenedFiles);
            part.setDataUri(res);
            return res;
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    private void persistData(PduPart part, Uri uri, String contentType, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        OutputStream os = null;
        InputStream is = null;
        DrmConvertSession drmConvertSession = null;
        String str = null;
        File file;
        try {
            byte[] data = part.getData();
            if (ContentType.TEXT_PLAIN.equals(contentType) || ContentType.APP_SMIL.equals(contentType) || ContentType.TEXT_HTML.equals(contentType)) {
                ContentValues cv = new ContentValues();
                if (data == null) {
                    Log.v("MMSLog", "convert data from null to empty.");
                    data = new String(UsimPBMemInfo.STRING_NOT_SET).getBytes("utf-8");
                }
                int charset = part.getCharset();
                if (charset == 0 || ContentType.APP_SMIL.equals(contentType)) {
                    cv.put("text", new EncodedStringValue(data).getString());
                } else {
                    cv.put("text", new EncodedStringValue(charset, data).getString());
                }
                if (this.mContentResolver.update(uri, cv, null, null) != 1) {
                    throw new MmsException("unable to update " + uri.toString());
                }
            }
            boolean isDrm = DownloadDrmHelper.isDrmConvertNeeded(contentType);
            if (isDrm) {
                if (uri != null) {
                    try {
                        str = convertUriToPath(this.mContext, uri);
                        if (new File(str).length() > 0) {
                            return;
                        }
                    } catch (Throwable e) {
                        Log.e(TAG, "Can't get file info for: " + part.getDataUri(), e);
                    }
                }
                drmConvertSession = DrmConvertSession.open(this.mContext, contentType);
                if (drmConvertSession == null) {
                    throw new MmsException("Mimetype " + contentType + " can not be converted.");
                }
            }
            os = this.mContentResolver.openOutputStream(uri);
            Uri dataUri;
            byte[] convertedData;
            if (data == null) {
                dataUri = part.getDataUri();
                if (dataUri != null && dataUri != uri) {
                    if (preOpenedFiles != null) {
                        if (preOpenedFiles.containsKey(dataUri)) {
                            is = (InputStream) preOpenedFiles.get(dataUri);
                        }
                    }
                    if (is == null) {
                        is = this.mContentResolver.openInputStream(dataUri);
                    }
                    if (is != null) {
                        byte[] buffer = new byte[SmsCbConstants.SERIAL_NUMBER_ETWS_EMERGENCY_USER_ALERT];
                        while (true) {
                            int len = is.read(buffer);
                            if (len == -1) {
                                break;
                            } else if (isDrm) {
                                convertedData = drmConvertSession.convert(buffer, len);
                                if (convertedData != null) {
                                    os.write(convertedData, 0, convertedData.length);
                                } else {
                                    throw new MmsException("Error converting drm data.");
                                }
                            } else {
                                os.write(buffer, 0, len);
                            }
                        }
                    } else {
                        Log.d(TAG, "the valude of ContentResolver.openInputStream() is null. InputStream uri:" + dataUri);
                        if (os != null) {
                            try {
                                os.close();
                            } catch (Throwable e2) {
                                Log.e(TAG, "IOException while closing: " + os, e2);
                            }
                        }
                        if (is != null) {
                            try {
                                is.close();
                            } catch (Throwable e22) {
                                Log.e(TAG, "IOException while closing: " + is, e22);
                            }
                        }
                        if (drmConvertSession != null) {
                            drmConvertSession.close(str);
                            file = new File(str);
                            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + file.getName()), new ContentValues(0), null, null);
                        }
                        return;
                    }
                }
                Log.w(TAG, "Can't find data for this part.");
                if (os != null) {
                    try {
                        os.close();
                    } catch (Throwable e222) {
                        Log.e(TAG, "IOException while closing: " + os, e222);
                    }
                }
                if (drmConvertSession != null) {
                    drmConvertSession.close(str);
                    file = new File(str);
                    SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + file.getName()), new ContentValues(0), null, null);
                }
                return;
            } else if (isDrm) {
                dataUri = uri;
                convertedData = drmConvertSession.convert(data, data.length);
                if (convertedData != null) {
                    os.write(convertedData, 0, convertedData.length);
                } else {
                    throw new MmsException("Error converting drm data.");
                }
            } else {
                os.write(data);
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Throwable e2222) {
                    Log.e(TAG, "IOException while closing: " + os, e2222);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e22222) {
                    Log.e(TAG, "IOException while closing: " + is, e22222);
                }
            }
            if (drmConvertSession != null) {
                drmConvertSession.close(str);
                file = new File(str);
                SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + file.getName()), new ContentValues(0), null, null);
            }
        } catch (Throwable e3) {
            Log.e(TAG, "Failed to open Input/Output stream.", e3);
            throw new MmsException(e3);
        } catch (Throwable e222222) {
            Log.e(TAG, "Failed to read/write data.", e222222);
            throw new MmsException(e222222);
        } catch (Throwable th) {
            Throwable th2 = th;
            if (os != null) {
                try {
                    os.close();
                } catch (Throwable e2222222) {
                    Log.e(TAG, "IOException while closing: " + os, e2222222);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e22222222) {
                    Log.e(TAG, "IOException while closing: " + is, e22222222);
                }
            }
            if (drmConvertSession != null) {
                drmConvertSession.close(str);
                file = new File(str);
                SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/resetFilePerm/" + file.getName()), new ContentValues(0), null, null);
            }
        }
    }

    public static String convertUriToPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(UsimPBMemInfo.STRING_NOT_SET) || scheme.equals("file")) {
            return uri.getPath();
        }
        if (scheme.equals("http")) {
            return uri.toString();
        }
        if (scheme.equals("content")) {
            String[] projection = new String[1];
            projection[0] = "_data";
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (!(cursor == null || cursor.getCount() == 0)) {
                    if (cursor.moveToFirst()) {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                        if (cursor == null) {
                            return path;
                        }
                        cursor.close();
                        return path;
                    }
                }
                throw new IllegalArgumentException("Given Uri could not be found in media store");
            } catch (SQLiteException e) {
                throw new IllegalArgumentException("Given Uri is not formatted in a way so that it can be found in media store.");
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            throw new IllegalArgumentException("Given Uri scheme is not supported");
        }
    }

    private void updateAddress(long msgId, int type, EncodedStringValue[] array) {
        SqliteWrapper.delete(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + msgId + "/addr"), "type=" + type, null);
        persistAddress(msgId, type, array);
    }

    public void updateHeaders(Uri uri, SendReq sendReq) {
        synchronized (PDU_CACHE_INSTANCE) {
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                try {
                    PDU_CACHE_INSTANCE.wait();
                } catch (Throwable e) {
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
            values.put("sub", UsimPBMemInfo.STRING_NOT_SET);
        }
        long messageSize = sendReq.getMessageSize();
        if (messageSize > 0) {
            values.put("m_size", Long.valueOf(messageSize));
        }
        PduHeaders headers = sendReq.getPduHeaders();
        HashSet<String> recipients = new HashSet();
        for (int addrType : ADDRESS_FIELDS) {
            EncodedStringValue v;
            EncodedStringValue[] array = null;
            if (addrType == 137) {
                v = headers.getEncodedStringValue(addrType);
                if (v != null) {
                    array = new EncodedStringValue[1];
                    array[0] = v;
                }
            } else {
                array = headers.getEncodedStringValues(addrType);
            }
            if (array != null) {
                updateAddress(ContentUris.parseId(uri), addrType, array);
                if (addrType == 151) {
                    for (EncodedStringValue v2 : array) {
                        if (v2 != null) {
                            recipients.add(v2.getString());
                        }
                    }
                }
            }
            if (addrType == 130 && array == null) {
                updateAddress(ContentUris.parseId(uri), addrType, array);
            }
        }
        if (!recipients.isEmpty()) {
            values.put("thread_id", Long.valueOf(Threads.getOrCreateThreadId(this.mContext, (Set) recipients)));
        }
        SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
        return;
    }

    private void updatePart(Uri uri, PduPart part, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
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
            if (part.getContentDisposition() != null) {
                values.put("cd", toIsoString(part.getContentDisposition()));
            }
            if (part.getContentId() != null) {
                values.put("cid", toIsoString(part.getContentId()));
            }
            if (part.getContentLocation() != null) {
                values.put("cl", toIsoString(part.getContentLocation()));
            }
            SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
            if (part.getData() != null || uri != part.getDataUri()) {
                persistData(part, uri, contentType, preOpenedFiles);
                return;
            }
            return;
        }
        throw new MmsException("MIME type of the part must be set.");
    }

    public void updateParts(Uri uri, PduBody body, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        PduPart pduPart;
        try {
            PduPart part;
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
            ArrayList<PduPart> toBeCreated = new ArrayList();
            HashMap<Uri, PduPart> toBeUpdated = new HashMap();
            int partsNum = body.getPartsNum();
            StringBuilder filter = new StringBuilder().append('(');
            int skippedCount = 0;
            int updateCount = 0;
            for (int i = 0; i < partsNum; i++) {
                part = body.getPart(i);
                Uri partUri = part.getDataUri();
                if (partUri == null || !partUri.getAuthority().startsWith("mms")) {
                    toBeCreated.add(part);
                    updateCount++;
                } else {
                    if (part.needUpdate()) {
                        toBeUpdated.put(partUri, part);
                        updateCount++;
                    } else {
                        skippedCount++;
                    }
                    if (filter.length() > 1) {
                        filter.append(" AND ");
                    }
                    filter.append("_id");
                    filter.append("!=");
                    DatabaseUtils.appendEscapedSQLString(filter, partUri.getLastPathSegment());
                }
            }
            filter.append(')');
            long msgId = ContentUris.parseId(uri);
            pduPart = this.mContext;
            SqliteWrapper.delete(pduPart, this.mContentResolver, Uri.parse(Mms.CONTENT_URI + "/" + msgId + "/part"), filter.length() > 2 ? filter.toString() : null, null);
            for (PduPart part2 : toBeCreated) {
                persistPart(part2, msgId, preOpenedFiles);
            }
            for (Entry<Uri, PduPart> e2 : toBeUpdated.entrySet()) {
                pduPart = (PduPart) e2.getValue();
                updatePart((Uri) e2.getKey(), pduPart, preOpenedFiles);
            }
            PDU_CACHE_INSTANCE.setUpdating(uri, false);
            PDU_CACHE_INSTANCE.notifyAll();
            return;
        } finally {
            pduPart = PDU_CACHE_INSTANCE;
            synchronized (pduPart) {
                PDU_CACHE_INSTANCE.setUpdating(uri, false);
                PDU_CACHE_INSTANCE.notifyAll();
            }
        }
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled) throws MmsException {
        return persist(pdu, uri, createThreadId, groupMmsEnabled, null);
    }

    public Uri persist(GenericPdu pdu, Uri uri) throws MmsException {
        return persist(pdu, uri, true, false);
    }

    public Uri persist(GenericPdu pdu, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) throws MmsException {
        if (uri == null) {
            throw new MmsException("Uri may not be null.");
        }
        long msgId = -1;
        try {
            msgId = ContentUris.parseId(uri);
        } catch (NumberFormatException e) {
        }
        boolean existingUri = msgId != -1;
        if (existingUri || MESSAGE_BOX_MAP.get(uri) != null) {
            int b;
            EncodedStringValue[] array;
            Uri res;
            synchronized (PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    try {
                        PDU_CACHE_INSTANCE.wait();
                    } catch (Throwable e2) {
                        Log.e(TAG, "persist1: ", e2);
                    }
                }
            }
            PDU_CACHE_INSTANCE.purge(uri);
            Log.d(TAG, "persist uri " + uri);
            PduHeaders header = pdu.getPduHeaders();
            ContentValues values = new ContentValues();
            for (Entry<Integer, String> e3 : ENCODED_STRING_COLUMN_NAME_MAP.entrySet()) {
                int field = ((Integer) e3.getKey()).intValue();
                EncodedStringValue encodedString = header.getEncodedStringValue(field);
                if (encodedString != null) {
                    String charsetColumn = (String) CHARSET_COLUMN_NAME_MAP.get(Integer.valueOf(field));
                    values.put((String) e3.getValue(), toIsoString(encodedString.getTextString()));
                    values.put(charsetColumn, Integer.valueOf(encodedString.getCharacterSet()));
                }
            }
            for (Entry<Integer, String> e32 : TEXT_STRING_COLUMN_NAME_MAP.entrySet()) {
                byte[] text = header.getTextString(((Integer) e32.getKey()).intValue());
                if (text != null) {
                    values.put((String) e32.getValue(), toIsoString(text));
                }
            }
            for (Entry<Integer, String> e322 : OCTET_COLUMN_NAME_MAP.entrySet()) {
                b = header.getOctet(((Integer) e322.getKey()).intValue());
                if (b != 0) {
                    values.put((String) e322.getValue(), Integer.valueOf(b));
                }
            }
            if (this.mBackupRestore) {
                Log.i(TAG, "add READ");
                b = header.getOctet(155);
                Log.i(TAG, "READ=" + b);
                if (b == 0) {
                    values.put("read", Integer.valueOf(b));
                } else if (b == 128) {
                    values.put("read", Integer.valueOf(1));
                } else {
                    values.put("read", Integer.valueOf(0));
                }
            }
            for (Entry<Integer, String> e3222 : LONG_COLUMN_NAME_MAP.entrySet()) {
                long l = header.getLongInteger(((Integer) e3222.getKey()).intValue());
                if (l != -1) {
                    values.put((String) e3222.getValue(), Long.valueOf(l));
                }
            }
            HashMap<Integer, EncodedStringValue[]> addressMap = new HashMap(ADDRESS_FIELDS.length);
            for (int addrType : ADDRESS_FIELDS) {
                array = null;
                if (addrType == 137) {
                    EncodedStringValue v = header.getEncodedStringValue(addrType);
                    if (v != null) {
                        array = new EncodedStringValue[1];
                        array[0] = v;
                    }
                } else {
                    array = header.getEncodedStringValues(addrType);
                }
                addressMap.put(Integer.valueOf(addrType), array);
            }
            HashSet<String> recipients = new HashSet();
            int msgType = pdu.getMessageType();
            if (msgType == 130 || msgType == 132 || msgType == 128) {
                switch (msgType) {
                    case 128:
                        loadRecipients(151, recipients, addressMap, false);
                        break;
                    case 130:
                    case 132:
                        loadRecipients(137, recipients, addressMap, false);
                        if (groupMmsEnabled) {
                            loadRecipients(151, recipients, addressMap, true);
                            loadRecipients(130, recipients, addressMap, true);
                            break;
                        }
                        break;
                }
                long threadId = 0;
                if (createThreadId && !recipients.isEmpty()) {
                    threadId = Threads.getOrCreateThreadId(this.mContext, (Set) recipients);
                }
                values.put("thread_id", Long.valueOf(threadId));
            }
            Log.d(TAG, "persist part begin ");
            long dummyId = System.currentTimeMillis();
            boolean textOnly = true;
            int messageSize = 0;
            if (pdu instanceof MultimediaMessagePdu) {
                PduBody body = ((MultimediaMessagePdu) pdu).getBody();
                if (body != null) {
                    int partsNum = body.getPartsNum();
                    if (partsNum > 2) {
                        textOnly = false;
                    }
                    for (int i = 0; i < partsNum; i++) {
                        PduPart part = body.getPart(i);
                        messageSize += part.getDataLength();
                        persistPart(part, dummyId, preOpenedFiles);
                        String contentType = getPartContentType(part);
                        if (!(contentType == null || ContentType.APP_SMIL.equals(contentType) || ContentType.TEXT_PLAIN.equals(contentType))) {
                            textOnly = false;
                        }
                    }
                }
            }
            values.put("text_only", Integer.valueOf(textOnly ? 1 : 0));
            if (values.getAsInteger("m_size") == null) {
                values.put("m_size", Integer.valueOf(messageSize));
            }
            Log.d(TAG, "persist pdu begin ");
            values.put("need_notify", Boolean.valueOf(false));
            if (this.mBackupRestore) {
                values.put("seen", Integer.valueOf(1));
            }
            if (existingUri) {
                res = uri;
                SqliteWrapper.update(this.mContext, this.mContentResolver, uri, values, null, null);
            } else {
                res = SqliteWrapper.insert(this.mContext, this.mContentResolver, uri, values);
                if (res == null) {
                    throw new MmsException("persist() failed: return null.");
                }
                msgId = ContentUris.parseId(res);
            }
            Log.d(TAG, "persist address begin ");
            for (int addrType2 : ADDRESS_FIELDS) {
                array = (EncodedStringValue[]) addressMap.get(Integer.valueOf(addrType2));
                if (array != null) {
                    persistAddress(msgId, addrType2, array);
                }
            }
            Log.d(TAG, "persist update part begin ");
            values = new ContentValues(1);
            values.put("mid", Long.valueOf(msgId));
            values.put("need_notify", Boolean.valueOf(true));
            SqliteWrapper.update(this.mContext, this.mContentResolver, Uri.parse("content://mms/" + dummyId + "/part"), values, null, null);
            PDU_CACHE_INSTANCE.purge(uri);
            Log.d(TAG, "persist purge end ");
            return !existingUri ? Uri.parse(uri + "/" + msgId) : res;
        } else {
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
    }

    private void loadRecipients(int addressType, HashSet<String> recipients, HashMap<Integer, EncodedStringValue[]> addressMap, boolean excludeMyNumber) {
        EncodedStringValue[] array = (EncodedStringValue[]) addressMap.get(Integer.valueOf(addressType));
        if (array != null) {
            if (!excludeMyNumber || array.length != 1) {
                int[] SubIdList = SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList();
                for (EncodedStringValue v : array) {
                    if (v != null) {
                        String number = v.getString();
                        Log.d(TAG, "number = " + number);
                        Log.d(TAG, "length = " + SubIdList.length);
                        if (SubIdList.length == 0) {
                            Log.d(TAG, "recipients add number = " + number);
                            recipients.add(number);
                        } else {
                            for (int subid : SubIdList) {
                                String myNumber;
                                Log.d(TAG, "subid = " + subid);
                                if (excludeMyNumber) {
                                    myNumber = this.mTelephonyManager.getLine1Number(subid);
                                } else {
                                    myNumber = null;
                                }
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

    public Uri move(Uri from, Uri to) throws MmsException {
        long msgId = ContentUris.parseId(from);
        if (msgId == -1) {
            throw new MmsException("Error! ID of the message: -1.");
        }
        Integer msgBox = (Integer) MESSAGE_BOX_MAP.get(to);
        if (msgBox == null) {
            throw new MmsException("Bad destination, must be one of content://mms/inbox, content://mms/sent, content://mms/drafts, content://mms/outbox, content://mms/temp.");
        }
        ContentValues values = new ContentValues(1);
        values.put("msg_box", msgBox);
        SqliteWrapper.update(this.mContext, this.mContentResolver, from, values, null, null);
        return ContentUris.withAppendedId(to, msgId);
    }

    public static String toIsoString(byte[] bytes) {
        try {
            return new String(bytes, CharacterSets.MIMENAME_ISO_8859_1);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "ISO_8859_1 must be supported!", e);
            return UsimPBMemInfo.STRING_NOT_SET;
        } catch (NullPointerException e2) {
            return UsimPBMemInfo.STRING_NOT_SET;
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
    }

    public Cursor getPendingMessages(long dueTime) {
        Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        String[] selectionArgs = new String[2];
        selectionArgs[0] = String.valueOf(10);
        selectionArgs[1] = String.valueOf(dueTime);
        return SqliteWrapper.query(this.mContext, this.mContentResolver, uriBuilder.build(), null, "err_type < ? AND due_time <= ? AND msg_id in ( SELECT msg_id FROM pdu  WHERE msg_box <>2  AND m_type <>132  )", selectionArgs, "due_time");
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
}
