package com.android.internal.telephony;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.oppo.CallLog.Calls;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.IIccPhoneBook.Stub;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.SpnOverride;
import java.util.List;

public class IccProvider extends ContentProvider {
    private static final String[] ADDRESS_BOOK_COLUMN_NAMES = new String[]{"_id", Calls.CACHED_NAME, "number", STR_EMAILS, "additionalNumber"};
    protected static final int ADN = 1;
    protected static final int ADN_ALL = 7;
    protected static final int ADN_SUB = 2;
    private static final int COLOROS_ADN_CAPACITY = 30;
    private static final int COLOROS_BASE = 24;
    private static final int COLOROS_EMAIL_LEN = 28;
    private static final int COLOROS_NAME_LEN = 26;
    private static final int COLOROS_PB_PBR_EXIST = 29;
    private static final int COLOROS_PB_READY = 27;
    private static final int COLOROS_TOTAL = 24;
    private static final int COLOROS_USED = 25;
    private static final boolean DBG = OemConstant.SWITCH_LOG;
    protected static final int FDN = 3;
    protected static final int FDN_SUB = 4;
    protected static final int SDN = 5;
    protected static final int SDN_SUB = 6;
    public static final String STR_ANRS = "anrs";
    public static final String STR_EMAILS = "emails";
    public static final String STR_INDEX = "index";
    public static final String STR_NEW_ANRS = "newAnrs";
    public static final String STR_NEW_EMAILS = "newEmails";
    public static final String STR_NEW_NUMBER = "newNumber";
    public static final String STR_NEW_TAG = "newTag";
    public static final String STR_NUMBER = "number";
    public static final String STR_OP = "operator";
    public static final String STR_PIN2 = "pin2";
    public static final String STR_TAG = "tag";
    private static final String TAG = "IccProvider";
    private static final UriMatcher URL_MATCHER = new UriMatcher(-1);
    private SubscriptionManager mSubscriptionManager;

    static {
        URL_MATCHER.addURI("icc", "adn", 1);
        URL_MATCHER.addURI("icc", "adn/subId/#", 2);
        URL_MATCHER.addURI("icc", "fdn", 3);
        URL_MATCHER.addURI("icc", "fdn/subId/#", 4);
        URL_MATCHER.addURI("icc", "sdn", 5);
        URL_MATCHER.addURI("icc", "sdn/subId/#", 6);
        URL_MATCHER.addURI("icc", "all_space/#", 24);
        URL_MATCHER.addURI("icc", "used_space/#", 25);
        URL_MATCHER.addURI("icc", "sim_name_length/#", 26);
        URL_MATCHER.addURI("icc", "phonebook_ready/#", 27);
        URL_MATCHER.addURI("icc", "emailLen/#", 28);
        URL_MATCHER.addURI("icc", "phonebook_pbrexist/#", 29);
        URL_MATCHER.addURI("icc", "adn_capacity/subId/#", 30);
    }

    public boolean onCreate() {
        this.mSubscriptionManager = SubscriptionManager.from(getContext());
        return true;
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        if (OemConstant.isSimProtectContact(getContext(), getCallingPackage())) {
            return null;
        }
        if (DBG) {
            log("query");
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
        Cursor[] result;
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList == null || subInfoList.size() == 0) {
            result = new Cursor[0];
        } else {
            int subIdCount = subInfoList.size();
            result = new Cursor[subIdCount];
            for (int i = 0; i < subIdCount; i++) {
                int subId = ((SubscriptionInfo) subInfoList.get(i)).getSubscriptionId();
                result[i] = loadFromEf(efType, subId);
                Rlog.i(TAG, "ADN Records loaded for Subscription ::" + subId);
            }
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
                return "vnd.android.cursor.dir/sim-contact";
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    public Uri insert(Uri url, ContentValues initialValues) {
        if (OemConstant.isSimProtectContact(getContext(), getCallingPackage())) {
            return null;
        }
        int efType;
        int subId;
        String pin2 = null;
        if (DBG) {
            log("insert");
        }
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
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = initialValues.getAsString(STR_TAG);
        String number = initialValues.getAsString("number");
        String emails = initialValues.getAsString(STR_EMAILS);
        String anrs = initialValues.getAsString("anr");
        ContentValues values = new ContentValues();
        values.put(STR_TAG, SpnOverride.MVNO_TYPE_NONE);
        values.put("number", SpnOverride.MVNO_TYPE_NONE);
        values.put(STR_EMAILS, SpnOverride.MVNO_TYPE_NONE);
        values.put(STR_ANRS, SpnOverride.MVNO_TYPE_NONE);
        values.put(STR_NEW_TAG, tag);
        values.put(STR_NEW_NUMBER, number);
        values.put(STR_NEW_EMAILS, emails);
        values.put(STR_NEW_ANRS, anrs);
        values.put(STR_OP, "insert");
        values.put(STR_INDEX, "-1");
        int index = oppoUpdateIccRecordInEf(efType, values, pin2, subId);
        if (index == -1) {
            return null;
        }
        StringBuilder buf = new StringBuilder("content://icc/");
        switch (match) {
            case 1:
                buf.append("adn/");
                break;
            case 2:
                buf.append("adn/subId/");
                break;
            case 3:
                buf.append("fdn/");
                break;
            case 4:
                buf.append("fdn/subId/");
                break;
        }
        buf.append(index);
        Uri resultUri = Uri.parse(buf.toString());
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
        if (OemConstant.isSimProtectContact(getContext(), getCallingPackage())) {
            return 0;
        }
        int efType;
        int subId;
        switch (URL_MATCHER.match(url)) {
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
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        if (DBG) {
            log("delete");
        }
        String tag = null;
        String number = null;
        String emails = null;
        String anrs = null;
        CharSequence pin2 = null;
        String id = null;
        int index = -1;
        AdnRecord record = null;
        String[] tokens = where.split("AND");
        int n = tokens.length;
        while (true) {
            n--;
            if (n >= 0) {
                String param = tokens[n];
                if (DBG) {
                    log("parsing '" + param + "'");
                }
                String[] pair = param.split("=", 2);
                if (pair.length != 2) {
                    Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
                } else {
                    String key = pair[0].trim();
                    String val = pair[1].trim();
                    if (STR_TAG.equals(key)) {
                        tag = normalizeValue(val);
                    } else if ("number".equals(key)) {
                        number = normalizeValue(val);
                    } else if (STR_EMAILS.equals(key)) {
                        emails = normalizeValue(val);
                    } else if (STR_ANRS.equals(key)) {
                        anrs = normalizeValue(val);
                    } else if (STR_PIN2.equals(key)) {
                        pin2 = normalizeValue(val);
                    } else if ("id".equals(key)) {
                        id = normalizeValue(val);
                        index = Integer.valueOf(id).intValue();
                        if (DBG) {
                            log("atlast id= " + id + " index= " + index);
                        }
                    }
                }
            } else {
                if (index > 0) {
                    try {
                        IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
                        if (iccIpb != null) {
                            record = iccIpb.oppoGetAndRecordByIndexUsingSubId(subId, efType, index);
                        }
                    } catch (RemoteException e) {
                    } catch (SecurityException ex) {
                        if (DBG) {
                            log(ex.toString());
                        }
                    }
                    if (record == null) {
                        if (DBG) {
                            log("delete not find record");
                        }
                        return 0;
                    }
                    tag = record.getAlphaTag();
                    number = record.getNumber();
                    if (record.getEmails() != null) {
                        emails = record.getEmails()[0];
                    }
                    if (record.getAnrNumbers() != null) {
                        anrs = record.getAnrNumbers()[0];
                    }
                    if (DBG) {
                        log("tag=  " + tag + "  number= " + number + "  emails= " + emails + "  anrs= " + anrs);
                    }
                } else if (efType == 28474 && number == null) {
                    if (DBG) {
                        log("ADN index error");
                    }
                    return 0;
                }
                ContentValues values = new ContentValues();
                values.put(STR_TAG, tag);
                values.put("number", number);
                values.put(STR_EMAILS, emails);
                values.put(STR_ANRS, anrs);
                values.put(STR_NEW_TAG, SpnOverride.MVNO_TYPE_NONE);
                values.put(STR_NEW_NUMBER, SpnOverride.MVNO_TYPE_NONE);
                values.put(STR_NEW_EMAILS, SpnOverride.MVNO_TYPE_NONE);
                values.put(STR_NEW_ANRS, SpnOverride.MVNO_TYPE_NONE);
                values.put(STR_OP, "delete");
                values.put(STR_INDEX, id);
                if (efType == 3 && TextUtils.isEmpty(pin2)) {
                    return 0;
                }
                if (DBG) {
                    log("delete mvalues= " + values);
                }
                if (efType == 28475) {
                    log(" efType == FDN , calling original interface ");
                    if (!updateIccRecordInEf(efType, values, pin2, subId)) {
                        return 0;
                    }
                }
                log(" efType != FDN , calling oppo interface ");
                if (oppoUpdateIccRecordInEf(efType, values, pin2, subId) < 0) {
                    return 0;
                }
                getContext().getContentResolver().notifyChange(url, null);
                return 1;
            }
        }
    }

    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        if (OemConstant.isSimProtectContact(getContext(), getCallingPackage())) {
            return 0;
        }
        int efType;
        int subId;
        String pin2 = null;
        if (DBG) {
            log("update");
        }
        switch (URL_MATCHER.match(url)) {
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
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag = values.getAsString(STR_TAG);
        String number = values.getAsString("number");
        String newTag = values.getAsString(STR_NEW_TAG);
        String newNumber = values.getAsString(STR_NEW_NUMBER);
        String anrs = null;
        String newAnrs = values.getAsString("newAnr");
        String emails = null;
        String newEmails = values.getAsString(STR_NEW_EMAILS);
        String id = values.getAsString("id");
        if (!TextUtils.isEmpty(id)) {
            int index = Integer.valueOf(id).intValue();
            AdnRecord record = null;
            if (DBG) {
                log("update index= " + index + "  id= " + id);
            }
            try {
                IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
                if (iccIpb != null) {
                    record = iccIpb.oppoGetAndRecordByIndexUsingSubId(subId, efType, index);
                }
            } catch (RemoteException e) {
            } catch (SecurityException ex) {
                if (DBG) {
                    log(ex.toString());
                }
            }
            if (record == null) {
                if (DBG) {
                    log("update not find record");
                }
                return 0;
            }
            tag = record.getAlphaTag();
            number = record.getNumber();
            if (record.getEmails() != null) {
                emails = record.getEmails()[0];
            }
            if (record.getAnrNumbers() != null) {
                anrs = record.getAnrNumbers()[0];
            }
            if (DBG) {
                log("update:  tag=  " + tag + "  number= " + number + "  emails= " + emails + "  anrs= " + anrs);
            }
            if (DBG) {
                log("update:  newTag=  " + newTag + "  newNumber= " + newNumber + "  newEmails= " + newEmails + "  newAnrs= " + newAnrs);
            }
        } else if (efType == 28474) {
            if (DBG) {
                log("ADN index error");
            }
            return 0;
        }
        ContentValues mValues = new ContentValues();
        mValues.put(STR_TAG, tag);
        mValues.put("number", number);
        mValues.put(STR_EMAILS, emails);
        mValues.put(STR_ANRS, anrs);
        mValues.put(STR_NEW_TAG, newTag);
        mValues.put(STR_NEW_NUMBER, newNumber);
        mValues.put(STR_NEW_EMAILS, newEmails);
        mValues.put(STR_NEW_ANRS, newAnrs);
        mValues.put(STR_OP, "update");
        mValues.put(STR_INDEX, id);
        if (efType == 28475) {
            log(" efType == FDN , calling original interface ");
            if (!updateIccRecordInEf(efType, mValues, pin2, subId)) {
                return 0;
            }
        }
        log(" efType != FDN , calling oppo interface ");
        if (oppoUpdateIccRecordInEf(efType, mValues, pin2, subId) < 0) {
            return 0;
        }
        getContext().getContentResolver().notifyChange(url, null);
        return 1;
    }

    private MatrixCursor loadFromEf(int efType, int subId) {
        if (DBG) {
            log("loadFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        }
        int simState = SubscriptionManager.getSimStateForSlotIndex(SubscriptionManager.getSlotIndex(subId));
        if (DBG) {
            log("simState:" + simState);
        }
        if (simState == 2 || simState == 1 || simState == 6) {
            Rlog.w(TAG, "Cannot load ADN records and return");
            return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
        }
        List adnRecords = null;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                adnRecords = iccIpb.getAdnRecordsInEfForSubscriber(subId, efType);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            if (DBG) {
                log(ex.toString());
            }
        }
        if (adnRecords != null) {
            int N = adnRecords.size();
            MatrixCursor cursor = new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES, N);
            log("adnRecords.size=" + N);
            for (int i = 0; i < N; i++) {
                loadRecord((AdnRecord) adnRecords.get(i), cursor, i);
            }
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
    }

    private boolean addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        if (DBG) {
            log("addIccRecordToEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", name=" + Rlog.pii(TAG, name) + ", number=" + Rlog.pii(TAG, number) + ", emails=" + Rlog.pii(TAG, emails) + ", subscription=" + subId);
        }
        boolean success = false;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, name, number, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            if (DBG) {
                log(ex.toString());
            }
        }
        if (DBG) {
            log("addIccRecordToEf: " + success);
        }
        return success;
    }

    private boolean updateIccRecordInEf(int efType, ContentValues values, String pin2, int subId) {
        boolean success = false;
        if (DBG) {
            log("updateIccRecordInEf: efType=" + efType + ", values: [ " + values + " ], subId:" + subId);
        }
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsWithContentValuesInEfBySearchUsingSubId(subId, efType, values, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            if (DBG) {
                log(ex.toString());
            }
        }
        if (DBG) {
            log("updateIccRecordInEf: " + success);
        }
        return success;
    }

    private boolean deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        if (DBG) {
            log("deleteIccRecordFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", name=" + Rlog.pii(TAG, name) + ", number=" + Rlog.pii(TAG, number) + ", emails=" + Rlog.pii(TAG, emails) + ", pin2=" + Rlog.pii(TAG, pin2) + ", subscription=" + subId);
        }
        boolean success = false;
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                success = iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efType, name, number, SpnOverride.MVNO_TYPE_NONE, SpnOverride.MVNO_TYPE_NONE, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            if (DBG) {
                log(ex.toString());
            }
        }
        if (DBG) {
            log("deleteIccRecordFromEf: " + success);
        }
        return success;
    }

    private void loadRecord(AdnRecord record, MatrixCursor cursor, int id) {
        if (!record.isEmpty()) {
            Object[] contact = new Object[5];
            String alphaTag = record.getAlphaTag();
            String number = record.getNumber();
            String[] anrs = record.getAdditionalNumbers();
            if (DBG) {
                log("loadRecord: " + alphaTag + ", " + Rlog.pii(TAG, number));
            }
            contact[1] = alphaTag;
            contact[2] = number;
            String[] emails = record.getEmails();
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
            if (anrs != null) {
                StringBuilder anrString = new StringBuilder();
                for (String anr : anrs) {
                    if (DBG) {
                        log("Adding anr:" + anr);
                    }
                    anrString.append(anr);
                }
                contact[4] = anrString.toString();
            }
            if (DBG) {
                log("loadRecord: RecordNumber = " + record.oppoGetRecordNumber());
            }
            contact[0] = Integer.valueOf(record.oppoGetRecordNumber());
            cursor.addRow(contact);
        }
    }

    private void log(String msg) {
        Rlog.d(TAG, "[IccProvider] " + msg);
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

    private int oppoUpdateIccRecordInEf(int efType, ContentValues values, String pin2, int subId) {
        int index = -1;
        if (DBG) {
            log("updateIccRecordInEf: efType=" + efType + ", values: [ " + values + " ], subId:" + subId);
        }
        try {
            IIccPhoneBook iccIpb = Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                index = iccIpb.oppoUpdateAdnRecordsWithContentValuesInEfBySearchUsingSubId(subId, efType, values, pin2);
            }
        } catch (RemoteException e) {
        } catch (SecurityException ex) {
            if (DBG) {
                log(ex.toString());
            }
        }
        if (DBG) {
            log("updateIccRecordInEf: " + index);
        }
        return index;
    }
}
