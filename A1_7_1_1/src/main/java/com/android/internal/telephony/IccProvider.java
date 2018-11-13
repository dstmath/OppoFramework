package com.android.internal.telephony;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.IIccPhoneBook.Stub;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccConstants;
import com.mediatek.internal.telephony.ppl.PplMessageManager.PendingMessage;
import com.mediatek.internal.telephony.uicc.CsimPhbStorageInfo;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.UnsupportedEncodingException;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class IccProvider extends ContentProvider {
    private static final String[] ADDRESS_BOOK_COLUMN_NAMES = null;
    private static final int ADDRESS_SUPPORT_AAS = 8;
    private static final int ADDRESS_SUPPORT_SNE = 9;
    protected static final int ADN = 1;
    protected static final int ADN_ALL = 9;
    protected static final int ADN_SUB = 2;
    private static final int COLOROS_ADN_CAPACITY = 30;
    private static final int COLOROS_BASE = 24;
    private static final int COLOROS_EMAIL_LEN = 28;
    private static final int COLOROS_NAME_LEN = 26;
    private static final int COLOROS_PB_PBR_EXIST = 29;
    private static final int COLOROS_PB_READY = 27;
    private static final int COLOROS_TOTAL = 24;
    private static final int COLOROS_USED = 25;
    private static final boolean DBG = false;
    public static final int ERROR_ICC_PROVIDER_ADN_LIST_NOT_EXIST = -11;
    public static final int ERROR_ICC_PROVIDER_ANR_SAVE_FAILURE = -14;
    public static final int ERROR_ICC_PROVIDER_ANR_TOO_LONG = -6;
    public static final int ERROR_ICC_PROVIDER_EMAIL_FULL = -12;
    public static final int ERROR_ICC_PROVIDER_EMAIL_TOO_LONG = -13;
    public static final int ERROR_ICC_PROVIDER_GENERIC_FAILURE = -10;
    public static final int ERROR_ICC_PROVIDER_NOT_READY = -4;
    public static final int ERROR_ICC_PROVIDER_NUMBER_TOO_LONG = -1;
    public static final int ERROR_ICC_PROVIDER_PASSWORD_ERROR = -5;
    public static final int ERROR_ICC_PROVIDER_SNE_FULL = -16;
    public static final int ERROR_ICC_PROVIDER_SNE_TOO_LONG = -17;
    public static final int ERROR_ICC_PROVIDER_STORAGE_FULL = -3;
    public static final int ERROR_ICC_PROVIDER_SUCCESS = 1;
    public static final int ERROR_ICC_PROVIDER_TEXT_TOO_LONG = -2;
    public static final int ERROR_ICC_PROVIDER_UNKNOWN = 0;
    public static final int ERROR_ICC_PROVIDER_WRONG_ADN_FORMAT = -15;
    protected static final int FDN = 3;
    protected static final int FDN_SUB = 4;
    protected static final int SDN = 5;
    protected static final int SDN_SUB = 6;
    protected static final String STR_ANR = "anr";
    protected static final String STR_EMAILS = "emails";
    protected static final String STR_INDEX = "index";
    protected static final String STR_NUMBER = "number";
    protected static final String STR_PIN2 = "pin2";
    protected static final String STR_TAG = "tag";
    private static final String TAG = "IccProvider";
    protected static final int UPB = 7;
    private static final String[] UPB_GRP_COLUMN_NAMES = null;
    protected static final int UPB_SUB = 8;
    private static final UriMatcher URL_MATCHER = null;
    private SubscriptionManager mSubscriptionManager;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IccProvider.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IccProvider.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IccProvider.<clinit>():void");
    }

    public boolean onCreate() {
        this.mSubscriptionManager = SubscriptionManager.from(getContext());
        return true;
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        logi("query " + url);
        if (OemConstant.PRINT_TEST) {
            log("leon.icc.query uid:" + Binder.getCallingUid() + " pid:" + Binder.getCallingPid());
        }
        switch (URL_MATCHER.match(url)) {
            case 1:
                return loadFromEf(28474, SubscriptionManager.getDefaultSubscriptionId());
            case 2:
                return loadFromEf(28474, getRequestSubId(url));
            case 3:
                return loadFromEf(IccConstants.EF_FDN, SubscriptionManager.getDefaultSubscriptionId());
            case 4:
                return loadFromEf(IccConstants.EF_FDN, getRequestSubId(url));
            case 5:
                return loadFromEf(IccConstants.EF_SDN, SubscriptionManager.getDefaultSubscriptionId());
            case 6:
                return loadFromEf(IccConstants.EF_SDN, getRequestSubId(url));
            case 7:
                return loadFromEf(IccConstants.EF_PBR, SubscriptionManager.getDefaultSubscriptionId());
            case 8:
                return loadFromEf(IccConstants.EF_PBR, getRequestSubId(url));
            case 9:
                return loadAllSimContacts(28474);
            case 24:
                return ColorOSFunIccProvider.colorOSMixSimAllSpace(getContext(), url);
            case 25:
                return ColorOSFunIccProvider.colorOSMixSimUsedSpace(getContext(), url);
            case 26:
                return ColorOSFunIccProvider.colorOSMixSimNameLen(getContext(), url);
            case 27:
                return ColorOSFunIccProvider.colorOSMSimCheckPhoneBookReady(url);
            case 28:
                return ColorOSFunIccProvider.colorOSMixEmailLen(getContext(), url);
            case 29:
                return ColorOSFunIccProvider.colorOSMSimCheckPhoneBookPbrExist(url);
            case 30:
                return ColorOSFunIccProvider.colorOSMSimAdnCapacity(getRequestSubId(url));
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    private Cursor loadAllSimContacts(int efType) {
        int[] subIdList = this.mSubscriptionManager.getActiveSubscriptionIdList();
        Cursor[] result = new Cursor[subIdList.length];
        int i = 0;
        int length = subIdList.length;
        int i2 = 0;
        while (i < length) {
            int subId = subIdList[i];
            int i3 = i2 + 1;
            result[i2] = loadFromEf(efType, subId);
            Rlog.i(TAG, "loadAllSimContacts: subId=" + subId);
            i++;
            i2 = i3;
        }
        return new MergeCursor(result);
    }

    public String getType(Uri url) {
        switch (URL_MATCHER.match(url)) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return "vnd.android.cursor.dir/sim-contact";
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public Uri insert(Uri url, ContentValues initialValues) {
        int efType;
        int subId;
        int result;
        String pin2 = null;
        logi("insert " + url);
        int match = URL_MATCHER.match(url);
        switch (match) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = initialValues.getAsString(STR_PIN2);
                break;
            case 4:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                pin2 = initialValues.getAsString(STR_PIN2);
                break;
            case 7:
                efType = IccConstants.EF_PBR;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 8:
                efType = IccConstants.EF_PBR;
                subId = getRequestSubId(url);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = initialValues.getAsString(STR_TAG);
        String number = initialValues.getAsString("number");
        if (7 == match || 8 == match) {
            String strGas = initialValues.getAsString("gas");
            String strAnr = initialValues.getAsString(STR_ANR);
            String strEmail = initialValues.getAsString(STR_EMAILS);
            if (ADDRESS_BOOK_COLUMN_NAMES.length >= 8) {
                Integer aasIndex = initialValues.getAsInteger("aas");
                if (number == null) {
                    number = UsimPBMemInfo.STRING_NOT_SET;
                }
                if (tag == null) {
                    tag = UsimPBMemInfo.STRING_NOT_SET;
                }
                AdnRecord adnRecord = new AdnRecord(efType, 0, tag, number);
                adnRecord.setAnr(strAnr);
                if (initialValues.containsKey("anr2")) {
                    String strAnr2 = initialValues.getAsString("anr2");
                    if (DBG) {
                        log("insert anr2: " + strAnr2);
                    }
                    adnRecord.setAnr(strAnr2, 1);
                }
                if (initialValues.containsKey("anr3")) {
                    String strAnr3 = initialValues.getAsString("anr3");
                    if (DBG) {
                        log("insert anr3: " + strAnr3);
                    }
                    adnRecord.setAnr(strAnr3, 2);
                }
                adnRecord.setGrpIds(strGas);
                String[] emails = null;
                if (!(strEmail == null || strEmail.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                    emails = new String[1];
                    emails[0] = strEmail;
                }
                adnRecord.setEmails(emails);
                if (aasIndex != null) {
                    adnRecord.setAasIndex(aasIndex.intValue());
                }
                if (ADDRESS_BOOK_COLUMN_NAMES.length >= 9) {
                    adnRecord.setSne(initialValues.getAsString("sne"));
                }
                logi("updateUsimPBRecordsBySearchWithError ");
                result = updateUsimPBRecordsBySearchWithError(efType, new AdnRecord(UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET), adnRecord, subId);
            } else {
                logi("addUsimRecordToEf ");
                result = addUsimRecordToEf(efType, tag, number, strAnr, strEmail, strGas, subId);
                if (result < 0) {
                    if (DBG) {
                        log("oppoAddUsimRecordToEf fail.");
                    }
                    return null;
                }
            }
            if (result > 0) {
                updatePhbStorageInfo(1, subId);
            }
        } else {
            logi("addIccRecordToEf ");
            result = addIccRecordToEf(efType, tag, number, null, pin2, subId);
            if (result == -1) {
                if (DBG) {
                    log("insert fail.");
                }
                return null;
            }
        }
        StringBuilder stringBuilder = new StringBuilder("content://icc/");
        if (result <= 0) {
            log("insert fail. result:" + result);
            return null;
        }
        switch (match) {
            case 1:
                stringBuilder.append("adn/");
                break;
            case 2:
                stringBuilder.append("adn/subId/");
                break;
            case 3:
                stringBuilder.append("fdn/");
                break;
            case 4:
                stringBuilder.append("fdn/subId/");
                break;
            case 7:
                stringBuilder.append("pbr/");
                break;
            case 8:
                stringBuilder.append("pbr/subId/");
                break;
        }
        stringBuilder.append(result);
        Uri resultUri = Uri.parse(stringBuilder.toString());
        logi(resultUri.toString());
        getContext().getContentResolver().notifyChange(url, null);
        return resultUri;
    }

    private String normalizeValue(String inVal) {
        int len = inVal.length();
        if (len == 0) {
            if (DBG) {
                log("len of input String is 0");
            }
            return inVal;
        }
        String retVal = inVal;
        if (inVal.charAt(0) == '\'' && inVal.charAt(len - 1) == '\'') {
            retVal = inVal.substring(1, len - 1);
        }
        return retVal;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        int efType;
        int subId;
        logi("delete " + url);
        int match = URL_MATCHER.match(url);
        switch (match) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 4:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                break;
            case 7:
                efType = IccConstants.EF_PBR;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 8:
                efType = IccConstants.EF_PBR;
                subId = getRequestSubId(url);
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        if (DBG) {
            log("delete");
        }
        String tag = UsimPBMemInfo.STRING_NOT_SET;
        String number = UsimPBMemInfo.STRING_NOT_SET;
        String[] emails = null;
        String pin2 = null;
        int nIndex = -1;
        String[] tokens = where.split("AND");
        int n = tokens.length;
        String id = null;
        while (true) {
            n--;
            int result;
            if (n >= 0) {
                String param = tokens[n];
                if (DBG) {
                    log("parsing '" + param + "'");
                }
                int index = param.indexOf(61);
                if (index == -1) {
                    Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
                } else {
                    String key = param.substring(0, index).trim();
                    String val = param.substring(index + 1).trim();
                    if (DBG) {
                        log("parsing key is " + key + " index of = is " + index + " val is " + val);
                    }
                    if (STR_INDEX.equals(key)) {
                        nIndex = Integer.parseInt(val);
                    } else if (STR_TAG.equals(key)) {
                        tag = normalizeValue(val);
                    } else if ("number".equals(key)) {
                        number = normalizeValue(val);
                    } else if (STR_EMAILS.equals(key)) {
                        emails = null;
                    } else if (STR_PIN2.equals(key)) {
                        pin2 = normalizeValue(val);
                    } else if (PendingMessage.KEY_ID.equals(key)) {
                        id = normalizeValue(val);
                        if (DBG) {
                            log("delete: id=" + id);
                        }
                    }
                }
            } else if (nIndex > 0) {
                logi("delete index is " + nIndex);
                if (7 == match || 8 == match) {
                    logi("deleteUsimRecordFromEfByIndex ");
                    result = deleteUsimRecordFromEfByIndex(efType, nIndex, subId);
                    if (result > 0) {
                        updatePhbStorageInfo(-1, subId);
                    }
                } else {
                    logi("deleteIccRecordFromEfByIndex ");
                    result = deleteIccRecordFromEfByIndex(efType, nIndex, pin2, subId);
                }
                logi("delete result = " + result);
                return result;
            } else if (TextUtils.isEmpty(id)) {
                if (efType == IccConstants.EF_FDN && TextUtils.isEmpty(pin2)) {
                    return -5;
                }
                if (tag.length() == 0 && number.length() == 0) {
                    return 0;
                }
                if (7 == match || 8 == match) {
                    if (ADDRESS_BOOK_COLUMN_NAMES.length >= 8) {
                        logi("updateUsimPBRecordsBySearchWithError ");
                        result = updateUsimPBRecordsBySearchWithError(efType, new AdnRecord(tag, number, UsimPBMemInfo.STRING_NOT_SET), new AdnRecord(UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET), subId);
                    } else {
                        logi("deleteUsimRecordFromEf ");
                        result = deleteUsimRecordFromEf(efType, tag, number, emails, subId);
                    }
                    if (result > 0) {
                        updatePhbStorageInfo(-1, subId);
                    }
                } else {
                    logi("deleteIccRecordFromEf ");
                    result = deleteIccRecordFromEf(efType, tag, number, emails, pin2, subId);
                }
                logi("delete result = " + result);
                getContext().getContentResolver().notifyChange(url, null);
                return result;
            } else if (match == 7 || 8 == match) {
                return deleteUsimRecordFromEfByIndex(efType, Integer.valueOf(id).intValue(), subId);
            } else {
                return deleteIccRecordFromEfByIndex(efType, Integer.valueOf(id).intValue(), pin2, subId);
            }
        }
    }

    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int efType;
        int subId;
        String pin2 = null;
        logi("update " + url);
        int match = URL_MATCHER.match(url);
        switch (match) {
            case 1:
                efType = 28474;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 2:
                efType = 28474;
                subId = getRequestSubId(url);
                break;
            case 3:
                efType = IccConstants.EF_FDN;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                pin2 = values.getAsString(STR_PIN2);
                break;
            case 4:
                efType = IccConstants.EF_FDN;
                subId = getRequestSubId(url);
                pin2 = values.getAsString(STR_PIN2);
                break;
            case 7:
                efType = IccConstants.EF_PBR;
                subId = SubscriptionManager.getDefaultSubscriptionId();
                break;
            case 8:
                efType = IccConstants.EF_PBR;
                subId = getRequestSubId(url);
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + match);
        }
        String tag = values.getAsString(STR_TAG);
        String number = values.getAsString("number");
        String[] emails = null;
        String newTag = values.getAsString("newTag");
        String newNumber = values.getAsString("newNumber");
        Integer idInt = values.getAsInteger(STR_INDEX);
        int index = 0;
        if (idInt != null) {
            index = idInt.intValue();
        }
        logi("update: index=" + index);
        Long id = values.getAsLong(PendingMessage.KEY_ID);
        if (DBG) {
            log("[update] tag: " + tag + " number: " + number + " newTag: " + newTag + " newNumber: " + newNumber + " id: " + id);
        }
        if (newTag == null) {
            newTag = UsimPBMemInfo.STRING_NOT_SET;
        }
        if (newNumber == null) {
            newNumber = UsimPBMemInfo.STRING_NOT_SET;
        }
        String strAnr;
        String strEmail;
        if (id == null) {
            int result;
            if (7 == match || 8 == match) {
                strAnr = values.getAsString("newAnr");
                strEmail = values.getAsString("newEmails");
                Integer aasIndex = values.getAsInteger("aas");
                String sne = values.getAsString("sne");
                if (newNumber == null) {
                    newNumber = UsimPBMemInfo.STRING_NOT_SET;
                }
                if (newTag == null) {
                    newTag = UsimPBMemInfo.STRING_NOT_SET;
                }
                AdnRecord adnRecord = new AdnRecord(efType, 0, newTag, newNumber);
                adnRecord.setAnr(strAnr);
                if (values.containsKey("newAnr2")) {
                    String strAnr2 = values.getAsString("newAnr2");
                    if (DBG) {
                        log("update newAnr2: " + strAnr2);
                    }
                    adnRecord.setAnr(strAnr2, 1);
                }
                if (values.containsKey("newAnr3")) {
                    String strAnr3 = values.getAsString("newAnr3");
                    if (DBG) {
                        log("update newAnr3: " + strAnr3);
                    }
                    adnRecord.setAnr(strAnr3, 2);
                }
                if (!(strEmail == null || strEmail.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                    emails = new String[1];
                    emails[0] = strEmail;
                }
                adnRecord.setEmails(emails);
                if (aasIndex != null) {
                    adnRecord.setAasIndex(aasIndex.intValue());
                }
                if (sne != null) {
                    adnRecord.setSne(sne);
                }
                if (index > 0) {
                    if (ADDRESS_BOOK_COLUMN_NAMES.length >= 8) {
                        logi("updateUsimPBRecordsByIndexWithError");
                        result = updateUsimPBRecordsByIndexWithError(efType, adnRecord, index, subId);
                    } else {
                        logi("updateUsimRecordInEfByIndex");
                        result = updateUsimRecordInEfByIndex(efType, index, newTag, newNumber, strAnr, strEmail, subId);
                    }
                } else if (ADDRESS_BOOK_COLUMN_NAMES.length >= 8) {
                    logi("updateUsimPBRecordsBySearchWithError");
                    result = updateUsimPBRecordsBySearchWithError(efType, new AdnRecord(tag, number, UsimPBMemInfo.STRING_NOT_SET), adnRecord, subId);
                } else {
                    logi("updateUsimRecordInEf");
                    result = updateUsimRecordInEf(efType, tag, number, newTag, newNumber, strAnr, strEmail, subId);
                }
            } else if (index > 0) {
                logi("updateIccRecordInEfByIndex");
                result = updateIccRecordInEfByIndex(efType, index, newTag, newNumber, pin2, subId);
            } else {
                logi("updateIccRecordInEf");
                result = updateIccRecordInEf(efType, tag, number, newTag, newNumber, pin2, subId);
            }
            logi("update result = " + result);
            getContext().getContentResolver().notifyChange(url, null);
            return result;
        } else if (7 == match || 8 == match) {
            strAnr = values.getAsString("newAnr");
            strEmail = values.getAsString("newEmails");
            if (strAnr == null) {
                strAnr = UsimPBMemInfo.STRING_NOT_SET;
            }
            if (strEmail == null) {
                strEmail = UsimPBMemInfo.STRING_NOT_SET;
            }
            return updateUsimRecordInEfByIndex(efType, id.intValue(), newTag, newNumber, strAnr, strEmail, subId);
        } else {
            return updateIccRecordInEfByIndex(efType, id.intValue(), newTag, newNumber, pin2, subId);
        }
    }

    private MatrixCursor loadFromEf(int efType, int subId) {
        if (DBG) {
            log("loadFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        }
        if (SubscriptionManager.getSimStateForSlotIdx(SubscriptionManager.getSlotId(subId)) == 2) {
            Rlog.w(TAG, "Cannot load ADN records SIM_STATE_PIN_REQUIRED");
            return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
        }
        List adnRecords = null;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEfForSubscriber(subId, efType);
            }
        } catch (RemoteException ex) {
            if (DBG) {
                log(ex.toString());
            }
        } catch (SecurityException ex2) {
            if (DBG) {
                log(ex2.toString());
            }
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES, N);
            if (DBG) {
                log("adnRecords.size=" + N);
            }
            for (int i = 0; i < N; i++) {
                loadRecord((AdnRecord) adnRecords.get(i), cursor, i);
            }
            logi("query success, size = " + N);
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
    }

    private int addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        if (DBG) {
            log("addIccRecordToEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", name=" + Rlog.pii(TAG, name) + ", number=" + Rlog.pii(TAG, number) + ", emails=" + Rlog.pii(TAG, emails) + ", subscription=" + subId);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfBySearchWithError(subId, efType, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, name, number, pin2);
            }
        } catch (RemoteException ex) {
            if (DBG) {
                log(ex.toString());
            }
        } catch (SecurityException ex2) {
            if (DBG) {
                log(ex2.toString());
            }
        }
        if (DBG) {
            log("addIccRecordToEf: " + result);
        }
        return result;
    }

    private int addUsimRecordToEf(int efType, String name, String number, String strAnr, String strEmail, String strGas, int subId) {
        if (DBG) {
            log("addUSIMRecordToEf: efType=" + efType + ", name=" + name + ", number=" + number + ", anr =" + strAnr + ", emails=" + strEmail + ", subId=" + subId);
        }
        int result = 0;
        String[] emails = null;
        if (strEmail != null) {
            if (!strEmail.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                emails = new String[1];
                emails[0] = strEmail;
            }
        }
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateUsimPBRecordsInEfBySearchWithError(subId, efType, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, null, null, name, number, strAnr, null, emails);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("addUsimRecordToEf: " + result);
        return result;
    }

    private int updateIccRecordInEf(int efType, String oldName, String oldNumber, String newName, String newNumber, String pin2, int subId) {
        if (DBG) {
            log("updateIccRecordInEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", oldname=" + Rlog.pii(TAG, oldName) + ", oldnumber=" + Rlog.pii(TAG, oldNumber) + ", newname=" + Rlog.pii(TAG, newName) + ", newnumber=" + Rlog.pii(TAG, newName) + ", subscription=" + subId);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfBySearchWithError(subId, efType, oldName, oldNumber, newName, newNumber, pin2);
            }
        } catch (RemoteException ex) {
            if (DBG) {
                log(ex.toString());
            }
        } catch (SecurityException ex2) {
            if (DBG) {
                log(ex2.toString());
            }
        }
        if (DBG) {
            log("updateIccRecordInEf: " + result);
        }
        return result;
    }

    private int updateIccRecordInEfByIndex(int efType, int nIndex, String newName, String newNumber, String pin2, int subId) {
        if (DBG) {
            log("updateIccRecordInEfByIndex: efType=" + efType + ", index=" + nIndex + ", newname=" + newName + ", newnumber=" + newNumber);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfByIndexWithError(subId, efType, newName, newNumber, nIndex, pin2);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("updateIccRecordInEfByIndex: " + result);
        return result;
    }

    private int updateUsimRecordInEf(int efType, String oldName, String oldNumber, String newName, String newNumber, String strAnr, String strEmail, int subId) {
        if (DBG) {
            log("updateUsimRecordInEf: efType=" + efType + ", oldname=" + oldName + ", oldnumber=" + oldNumber + ", newname=" + newName + ", newnumber=" + newNumber + ", anr =" + strAnr + ", emails=" + strEmail);
        }
        int result = 0;
        String[] emails = null;
        if (strEmail != null) {
            emails = new String[1];
            emails[0] = strEmail;
        }
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateUsimPBRecordsInEfBySearchWithError(subId, efType, oldName, oldNumber, UsimPBMemInfo.STRING_NOT_SET, null, null, newName, newNumber, strAnr, null, emails);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("updateUsimRecordInEf: " + result);
        return result;
    }

    private int updateUsimRecordInEfByIndex(int efType, int nIndex, String newName, String newNumber, String strAnr, String strEmail, int subId) {
        if (DBG) {
            log("updateUsimRecordInEfByIndex: efType=" + efType + ", Index=" + nIndex + ", newname=" + newName + ", newnumber=" + newNumber + ", anr =" + strAnr + ", emails=" + strEmail);
        }
        int result = 0;
        String[] emails = null;
        if (strEmail != null) {
            emails = new String[1];
            emails[0] = strEmail;
        }
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateUsimPBRecordsInEfByIndexWithError(subId, efType, newName, newNumber, strAnr, null, emails, nIndex);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("updateUsimRecordInEfByIndex: " + result);
        return result;
    }

    private int deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        if (DBG) {
            log("deleteIccRecordFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", name=" + Rlog.pii(TAG, name) + ", number=" + Rlog.pii(TAG, number) + ", emails=" + Rlog.pii(TAG, emails) + ", pin2=" + Rlog.pii(TAG, pin2) + ", subscription=" + subId);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfBySearchWithError(subId, efType, name, number, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, pin2);
            }
        } catch (RemoteException ex) {
            if (DBG) {
                log(ex.toString());
            }
        } catch (SecurityException ex2) {
            if (DBG) {
                log(ex2.toString());
            }
        }
        if (DBG) {
            log("deleteIccRecordFromEf: " + result);
        }
        return result;
    }

    private int deleteIccRecordFromEfByIndex(int efType, int nIndex, String pin2, int subId) {
        if (DBG) {
            log("deleteIccRecordFromEfByIndex: efType=" + efType + ", index=" + nIndex + ", pin2=" + pin2);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfByIndexWithError(subId, efType, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, nIndex, pin2);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("deleteIccRecordFromEfByIndex: " + result);
        return result;
    }

    private int deleteUsimRecordFromEf(int efType, String name, String number, String[] emails, int subId) {
        if (DBG) {
            log("deleteUsimRecordFromEf: efType=" + efType + ", name=" + name + ", number=" + number + ", emails=" + emails);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateUsimPBRecordsInEfBySearchWithError(subId, efType, name, number, UsimPBMemInfo.STRING_NOT_SET, null, null, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, null, null);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("deleteUsimRecordFromEf: " + result);
        return result;
    }

    private int deleteUsimRecordFromEfByIndex(int efType, int nIndex, int subId) {
        if (DBG) {
            log("deleteUsimRecordFromEfByIndex: efType=" + efType + ", index=" + nIndex);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateUsimPBRecordsInEfByIndexWithError(subId, efType, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, null, null, nIndex);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("deleteUsimRecordFromEfByIndex: " + result);
        return result;
    }

    private void loadRecord(AdnRecord record, MatrixCursor cursor, int id) {
        int len = ADDRESS_BOOK_COLUMN_NAMES.length;
        if (!record.isEmpty()) {
            Object[] contact = new Object[len];
            String alphaTag = record.getAlphaTag();
            String number = record.getNumber();
            String[] emails = record.getEmails();
            String grpIds = record.getGrpIds();
            String index = Integer.toString(record.getRecId());
            if (len >= 8) {
                contact[7] = Integer.valueOf(record.getAasIndex());
            }
            if (len >= 9) {
                contact[8] = record.getSne();
            }
            if (DBG) {
                log("loadRecord: record:" + record);
            }
            contact[0] = index;
            contact[1] = alphaTag;
            contact[2] = number;
            if (SystemProperties.get("ro.mtk_kor_customization").equals("1") && alphaTag.length() >= 2 && alphaTag.charAt(0) == 65278) {
                String strKSC = UsimPBMemInfo.STRING_NOT_SET;
                try {
                    strKSC = new String(alphaTag.substring(1).getBytes("utf-16be"), "KSC5601");
                } catch (UnsupportedEncodingException ex) {
                    if (DBG) {
                        log("Implausible UnsupportedEncodingException : " + ex);
                    }
                }
                if (strKSC != null) {
                    int ucslen = strKSC.length();
                    while (ucslen > 0 && strKSC.charAt(ucslen - 1) == 63735) {
                        ucslen--;
                    }
                    contact[1] = strKSC.substring(0, ucslen);
                    if (DBG) {
                        log("Decode ADN using KSC5601 : " + contact[1]);
                    }
                }
            }
            if (emails != null) {
                StringBuilder emailString = new StringBuilder();
                for (String email : emails) {
                    log("Adding email:" + Rlog.pii(TAG, email));
                    if (email != null) {
                        if (DBG) {
                            log("Adding email:" + email);
                        }
                        emailString.append(email);
                        contact[3] = emailString.toString();
                    }
                }
            }
            contact[4] = record.getAdditionalNumber();
            if (DBG) {
                log("loadRecord Adding anrs:" + contact[4]);
            }
            contact[5] = grpIds;
            contact[6] = Integer.valueOf(id);
            cursor.addRow(contact);
        }
    }

    private void log(String msg) {
        Rlog.d(TAG, "[IccProvider] " + msg);
    }

    private void logi(String msg) {
        Rlog.i(TAG, "[IccProvider] " + msg);
    }

    private IIccPhoneBook getIccPhbService() {
        return Stub.asInterface(ServiceManager.getService("simphonebook"));
    }

    private int getRequestSubId(Uri url) {
        if (DBG) {
            log("getRequestSubId url: " + url);
        }
        try {
            return Integer.parseInt(url.getLastPathSegment());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    private int updateUsimPBRecordsBySearchWithError(int efType, AdnRecord oldAdn, AdnRecord newAdn, int subId) {
        if (DBG) {
            log("updateUsimPBRecordsBySearchWithError subId:" + subId + ",oldAdn:" + oldAdn + ",newAdn:" + newAdn);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateUsimPBRecordsBySearchWithError(subId, efType, oldAdn, newAdn);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("updateUsimPBRecordsBySearchWithError: " + result);
        return result;
    }

    private int updateUsimPBRecordsByIndexWithError(int efType, AdnRecord newAdn, int index, int subId) {
        if (DBG) {
            log("updateUsimPBRecordsByIndexWithError subId:" + subId + ",index:" + index + ",newAdn:" + newAdn);
        }
        int result = 0;
        try {
            IIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateUsimPBRecordsByIndexWithError(subId, efType, newAdn, index);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("updateUsimPBRecordsByIndexWithError: " + result);
        return result;
    }

    private void updatePhbStorageInfo(int update, int subId) {
        boolean res = false;
        try {
            Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
            if (phone != null) {
                if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(phone.getIccFileHandler())) {
                    log("[updatePhbStorageInfo] is not a csim card");
                    res = false;
                } else {
                    res = CsimPhbStorageInfo.updatePhbStorageInfo(update);
                }
            }
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("[updatePhbStorageInfo] res = " + res);
    }
}
