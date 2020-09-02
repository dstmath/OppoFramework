package com.mediatek.internal.telephony.phb;

import android.content.ContentValues;
import android.content.Context;
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
import com.android.internal.telephony.IccInternalInterface;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.internal.telephony.ColorOSFunIccProvider;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.phb.IMtkIccPhoneBook;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class MtkIccProvider implements IccInternalInterface {
    private static final String[] ADDRESS_BOOK_COLUMN_NAMES = {STR_INDEX, "name", "number", STR_EMAILS, "additionalNumber", "groupIds", "_id", "aas", "sne"};
    private static final int ADDRESS_SUPPORT_AAS = 8;
    private static final int ADDRESS_SUPPORT_SNE = 9;
    protected static final int ADN = 1;
    protected static final int ADN_ALL = 9;
    protected static final int ADN_SUB = 2;
    protected static final int COLOROS_ADN_CAPACITY = 30;
    protected static final int COLOROS_BASE = 24;
    protected static final int COLOROS_EMAIL_LEN = 28;
    protected static final int COLOROS_NAME_LEN = 26;
    protected static final int COLOROS_PB_PBR_EXIST = 29;
    protected static final int COLOROS_PB_READY = 27;
    protected static final int COLOROS_TOTAL = 24;
    protected static final int COLOROS_USED = 25;
    private static final boolean DBG = (!SystemProperties.get("ro.build.type").equals(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER));
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
    private static final String TAG = "MtkIccProvider";
    protected static final int UPB = 7;
    protected static final int UPB_SUB = 8;
    private static UriMatcher URL_MATCHER;
    private Context mContext;

    public MtkIccProvider(UriMatcher URL_MATCHER2, Context context) {
        logi("MtkIccProvider URL_MATCHER " + URL_MATCHER2);
        URL_MATCHER2.addURI("icc", "all_space/#", 24);
        URL_MATCHER2.addURI("icc", "used_space/#", COLOROS_USED);
        URL_MATCHER2.addURI("icc", "sim_name_length/#", COLOROS_NAME_LEN);
        URL_MATCHER2.addURI("icc", "phonebook_ready/#", COLOROS_PB_READY);
        URL_MATCHER2.addURI("icc", "emailLen/#", COLOROS_EMAIL_LEN);
        URL_MATCHER2.addURI("icc", "phonebook_pbrexist/#", COLOROS_PB_PBR_EXIST);
        URL_MATCHER2.addURI("icc", "adn_capacity/subId/#", 30);
        URL_MATCHER2.addURI("icc", "pbr", 7);
        URL_MATCHER2.addURI("icc", "pbr/subId/#", 8);
        URL_MATCHER = URL_MATCHER2;
        this.mContext = context;
    }

    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        logi("query " + url);
        if (OemConstant.PRINT_TEST) {
            log("leon.icc.query uid:" + Binder.getCallingUid() + " pid:" + Binder.getCallingPid());
        }
        int match = URL_MATCHER.match(url);
        switch (match) {
            case 1:
                return loadFromEf(28474, SubscriptionManager.getDefaultSubscriptionId());
            case 2:
                return loadFromEf(28474, getRequestSubId(url));
            case 3:
                return loadFromEf(28475, SubscriptionManager.getDefaultSubscriptionId());
            case 4:
                return loadFromEf(28475, getRequestSubId(url));
            case 5:
                return loadFromEf(28489, SubscriptionManager.getDefaultSubscriptionId());
            case 6:
                return loadFromEf(28489, getRequestSubId(url));
            case 7:
                return loadFromEf(20272, SubscriptionManager.getDefaultSubscriptionId());
            case 8:
                return loadFromEf(20272, getRequestSubId(url));
            case 9:
                return loadAllSimContacts(28474);
            default:
                switch (match) {
                    case 24:
                        return ColorOSFunIccProvider.colorOSMixSimAllSpace(this.mContext, url);
                    case COLOROS_USED /*{ENCODED_INT: 25}*/:
                        return ColorOSFunIccProvider.colorOSMixSimUsedSpace(this.mContext, url);
                    case COLOROS_NAME_LEN /*{ENCODED_INT: 26}*/:
                        return ColorOSFunIccProvider.colorOSMixSimNameLen(this.mContext, url);
                    case COLOROS_PB_READY /*{ENCODED_INT: 27}*/:
                        return ColorOSFunIccProvider.colorOSMSimCheckPhoneBookReady(url);
                    case COLOROS_EMAIL_LEN /*{ENCODED_INT: 28}*/:
                        return ColorOSFunIccProvider.colorOSMixEmailLen(this.mContext, url);
                    case COLOROS_PB_PBR_EXIST /*{ENCODED_INT: 29}*/:
                        return ColorOSFunIccProvider.colorOSMSimCheckPhoneBookPbrExist(url);
                    case 30:
                        return ColorOSFunIccProvider.colorOSMSimAdnCapacity(getRequestSubId(url));
                    default:
                        throw new IllegalArgumentException("Unknown URL " + url);
                }
        }
    }

    private Cursor loadAllSimContacts(int efType) {
        int[] subIdList = SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList();
        Cursor[] result = new Cursor[subIdList.length];
        int i = 0;
        int length = subIdList.length;
        int i2 = 0;
        while (i2 < length) {
            int subId = subIdList[i2];
            int i3 = i + 1;
            result[i] = loadFromEf(efType, subId);
            Rlog.i(TAG, "loadAllSimContacts: subId=" + subId);
            i2++;
            i = i3;
        }
        return new MergeCursor(result);
    }

    private MatrixCursor loadFromEf(int efType, int subId) {
        if (DBG) {
            log("loadFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", subscription=" + subId);
        }
        if (SubscriptionManager.getSimStateForSlotIndex(SubscriptionManager.getSlotId(subId)) == 2) {
            Rlog.w(TAG, "Cannot load ADN records SIM_STATE_PIN_REQUIRED");
            return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
        }
        List<MtkAdnRecord> adnRecords = null;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
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
                loadRecord(adnRecords.get(i), cursor, i);
            }
            logi("query success, size = " + N);
            return cursor;
        }
        Rlog.w(TAG, "Cannot load ADN records");
        return new MatrixCursor(ADDRESS_BOOK_COLUMN_NAMES);
    }

    /* JADX WARNING: Removed duplicated region for block: B:79:0x022f  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0244  */
    public Uri insert(Uri url, ContentValues initialValues) {
        String pin2;
        int efType;
        int subId;
        int i;
        int result;
        int subId2;
        int efType2;
        int i2;
        int subId3;
        String number;
        String tag;
        String number2;
        String tag2;
        logi("insert " + url);
        int match = URL_MATCHER.match(url);
        if (match == 1) {
            pin2 = null;
            efType = 28474;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 2) {
            pin2 = null;
            efType = 28474;
            subId = getRequestSubId(url);
        } else if (match == 3) {
            subId = SubscriptionManager.getDefaultSubscriptionId();
            pin2 = initialValues.getAsString(STR_PIN2);
            efType = 28475;
        } else if (match == 4) {
            subId = getRequestSubId(url);
            pin2 = initialValues.getAsString(STR_PIN2);
            efType = 28475;
        } else if (match == 7) {
            pin2 = null;
            efType = 20272;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 8) {
            pin2 = null;
            efType = 20272;
            subId = getRequestSubId(url);
        } else {
            throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String tag3 = initialValues.getAsString(STR_TAG);
        String number3 = initialValues.getAsString("number");
        if (7 == match) {
            subId2 = subId;
            efType2 = efType;
            i2 = 8;
        } else if (8 == match) {
            subId2 = subId;
            efType2 = efType;
            i2 = 8;
        } else {
            if (number3 == null) {
                number2 = "";
            } else {
                number2 = number3;
            }
            if (tag3 == null) {
                tag2 = "";
            } else {
                tag2 = tag3;
            }
            logi("addIccRecordToEf:" + getMaskString(number2) + ",tag:" + getMaskString(tag2));
            result = addIccRecordToEf(efType, tag2, number2, null, pin2, subId);
            if (result == -1) {
                if (DBG) {
                    log("insert fail.");
                }
                return null;
            }
            i = 2;
            StringBuilder buf = new StringBuilder("content://icc/");
            if (result > 0) {
                log("insert fail. result:" + result);
                return null;
            }
            if (match == 1) {
                buf.append("adn/");
            } else if (match == i) {
                buf.append("adn/subId/");
            } else if (match == 3) {
                buf.append("fdn/");
            } else if (match == 4) {
                buf.append("fdn/subId/");
            } else if (match == 7) {
                buf.append("pbr/");
            } else if (match == 8) {
                buf.append("pbr/subId/");
            } else {
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
            }
            buf.append(result);
            Uri resultUri = Uri.parse(buf.toString());
            logi(resultUri.toString());
            return resultUri;
        }
        String strGas = initialValues.getAsString("gas");
        String strAnr = initialValues.getAsString(STR_ANR);
        String strEmail = initialValues.getAsString(STR_EMAILS);
        if (ADDRESS_BOOK_COLUMN_NAMES.length >= i2) {
            Integer aasIndex = initialValues.getAsInteger("aas");
            if (number3 == null) {
                number = "";
            } else {
                number = number3;
            }
            if (tag3 == null) {
                tag = "";
            } else {
                tag = tag3;
            }
            MtkAdnRecord record = new MtkAdnRecord(efType2, 0, tag, number);
            record.setAnr(strAnr);
            if (initialValues.containsKey("anr2")) {
                String strAnr2 = initialValues.getAsString("anr2");
                if (DBG) {
                    log("insert anr2: " + getMaskString(strAnr2));
                }
                record.setAnr(strAnr2, 1);
            }
            if (initialValues.containsKey("anr3")) {
                String strAnr3 = initialValues.getAsString("anr3");
                if (DBG) {
                    log("insert anr3: " + getMaskString(strAnr3));
                }
                record.setAnr(strAnr3, 2);
            }
            record.setGrpIds(strGas);
            String[] emails = null;
            if (strEmail != null && !strEmail.equals("")) {
                emails = new String[]{strEmail};
            }
            record.setEmails(emails);
            if (aasIndex != null) {
                record.setAasIndex(aasIndex.intValue());
            }
            if (ADDRESS_BOOK_COLUMN_NAMES.length >= 9) {
                record.setSne(initialValues.getAsString("sne"));
            }
            logi("updateUsimPBRecordsBySearchWithError ");
            subId3 = subId2;
            result = updateUsimPBRecordsBySearchWithError(efType2, new MtkAdnRecord("", "", ""), record, subId3);
            i = 2;
        } else {
            subId3 = subId2;
            logi("addUsimRecordToEf ");
            i = 2;
            result = addUsimRecordToEf(efType2, tag3, number3, strAnr, strEmail, strGas, subId3);
            if (result < 0) {
                if (DBG) {
                    log("oppoAddUsimRecordToEf fail.");
                }
                return null;
            }
        }
        if (result > 0) {
            updatePhbStorageInfo(1, subId3);
        }
        StringBuilder buf2 = new StringBuilder("content://icc/");
        if (result > 0) {
        }
    }

    private String normalizeValue(String inVal) {
        int len = inVal.length();
        if (len == 0) {
            if (DBG) {
                log("len of input String is 0");
            }
            return inVal;
        } else if (inVal.charAt(0) == '\'' && inVal.charAt(len - 1) == '\'') {
            return inVal.substring(1, len - 1);
        } else {
            return inVal;
        }
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        int subId;
        int efType;
        int result;
        String number;
        String tag;
        int result2;
        logi("delete " + url);
        int match = URL_MATCHER.match(url);
        if (match == 1) {
            efType = 28474;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 2) {
            efType = 28474;
            subId = getRequestSubId(url);
        } else if (match == 3) {
            efType = 28475;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 4) {
            efType = 28475;
            subId = getRequestSubId(url);
        } else if (match == 7) {
            efType = 20272;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 8) {
            efType = 20272;
            subId = getRequestSubId(url);
        } else {
            throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        String[] tokens = where.split(" AND ");
        int n = tokens.length;
        String[] emails = null;
        String id = null;
        String number2 = "";
        int nIndex = -1;
        String pin2 = null;
        String tag2 = "";
        while (true) {
            int n2 = n - 1;
            if (n2 < 0) {
                break;
            }
            String param = tokens[n2];
            if (DBG) {
                log("parsing '" + param + "'");
            }
            int index = param.indexOf(61);
            if (index == -1) {
                Rlog.e(TAG, "resolve: bad whereClause parameter: " + param);
                n = n2;
            } else {
                String key = param.substring(0, index).trim();
                String val = param.substring(index + 1).trim();
                if (DBG) {
                    log("parsing key is " + key + " index of = is " + index + " val is " + val);
                }
                if (STR_INDEX.equals(key)) {
                    try {
                        nIndex = Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        Rlog.d(TAG, e.toString());
                    } catch (Exception e2) {
                        Rlog.d(TAG, e2.toString());
                    }
                } else if (STR_TAG.equals(key)) {
                    tag2 = normalizeValue(val);
                } else if ("number".equals(key)) {
                    number2 = normalizeValue(val);
                } else if (STR_EMAILS.equals(key)) {
                    emails = null;
                } else if (STR_PIN2.equals(key)) {
                    pin2 = normalizeValue(val);
                } else if (PplMessageManager.PendingMessage.KEY_ID.equals(key)) {
                    String id2 = normalizeValue(val);
                    if (DBG) {
                        log("delete: id=" + id2);
                    }
                    id = id2;
                }
                n = n2;
            }
        }
        String str = "delete result = ";
        if (nIndex > 0) {
            logi("delete index is " + nIndex);
            if (7 == match || 8 == match) {
                logi("deleteUsimRecordFromEfByIndex ");
                int result3 = deleteUsimRecordFromEfByIndex(efType, nIndex, subId);
                if (result3 > 0) {
                    updatePhbStorageInfo(-1, subId);
                }
                result2 = result3;
            } else {
                logi("deleteIccRecordFromEfByIndex ");
                result2 = deleteIccRecordFromEfByIndex(efType, nIndex, pin2, subId);
            }
            logi(str + result2);
            return result2;
        } else if (!TextUtils.isEmpty(id)) {
            if (match != 7) {
                if (8 != match) {
                    return deleteIccRecordFromEfByIndex(efType, Integer.valueOf(id).intValue(), pin2, subId);
                }
            }
            return deleteUsimRecordFromEfByIndex(efType, Integer.valueOf(id).intValue(), subId);
        } else if (efType == 28475 && TextUtils.isEmpty(pin2)) {
            return -5;
        } else {
            if (tag2.length() == 0 && number2.length() == 0) {
                return 0;
            }
            if (7 == match) {
                number = number2;
                tag = tag2;
            } else if (8 == match) {
                number = number2;
                tag = tag2;
            } else {
                logi("deleteIccRecordFromEf ");
                result = deleteIccRecordFromEf(efType, tag2, number2, emails, pin2, subId);
                str = str;
                logi(str + result);
                return result;
            }
            if (ADDRESS_BOOK_COLUMN_NAMES.length >= 8) {
                logi("updateUsimPBRecordsBySearchWithError ");
                result = updateUsimPBRecordsBySearchWithError(efType, new MtkAdnRecord(tag, number, ""), new MtkAdnRecord("", "", ""), subId);
            } else {
                logi("deleteUsimRecordFromEf ");
                result = deleteUsimRecordFromEf(efType, tag, number, emails, subId);
            }
            if (result > 0) {
                updatePhbStorageInfo(-1, subId);
            }
            logi(str + result);
            return result;
        }
    }

    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int subId;
        String pin2;
        int efType;
        int result;
        String newTag;
        String newNumber;
        String newNumber2;
        String newTag2;
        String newTag3;
        String[] emails;
        String newTag4;
        String newNumber3;
        Long id;
        String strAnr;
        String strEmail;
        logi("update " + url);
        int match = URL_MATCHER.match(url);
        if (match == 1) {
            pin2 = null;
            efType = 28474;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 2) {
            pin2 = null;
            efType = 28474;
            subId = getRequestSubId(url);
        } else if (match == 3) {
            int subId2 = SubscriptionManager.getDefaultSubscriptionId();
            pin2 = values.getAsString(STR_PIN2);
            efType = 28475;
            subId = subId2;
        } else if (match == 4) {
            int subId3 = getRequestSubId(url);
            pin2 = values.getAsString(STR_PIN2);
            efType = 28475;
            subId = subId3;
        } else if (match == 7) {
            pin2 = null;
            efType = 20272;
            subId = SubscriptionManager.getDefaultSubscriptionId();
        } else if (match == 8) {
            pin2 = null;
            efType = 20272;
            subId = getRequestSubId(url);
        } else {
            throw new IllegalArgumentException("Unknown URL " + match);
        }
        String tag = values.getAsString(STR_TAG);
        String number = values.getAsString("number");
        String newTag5 = values.getAsString("newTag");
        String newNumber4 = values.getAsString("newNumber");
        Integer idInt = values.getAsInteger(STR_INDEX);
        int index = 0;
        if (idInt != null) {
            index = idInt.intValue();
        }
        logi("update: index=" + index);
        Long id2 = values.getAsLong(PplMessageManager.PendingMessage.KEY_ID);
        if (DBG) {
            log("[update] tag: " + tag + " number: " + number + " newTag: " + newTag5 + " newNumber: " + newNumber4 + " id: " + id2);
        }
        if (id2 != null) {
            if (newTag5 == null) {
                newTag4 = "";
            } else {
                newTag4 = newTag5;
            }
            if (newNumber4 == null) {
                newNumber3 = "";
            } else {
                newNumber3 = newNumber4;
            }
            if (7 == match) {
                id = id2;
            } else if (8 != match) {
                return updateIccRecordInEfByIndex(efType, id2.intValue(), newTag4, newNumber3, pin2, subId);
            } else {
                id = id2;
            }
            String strAnr2 = values.getAsString("newAnr");
            String strEmail2 = values.getAsString("newEmails");
            if (strAnr2 == null) {
                strAnr = "";
            } else {
                strAnr = strAnr2;
            }
            if (strEmail2 == null) {
                strEmail = "";
            } else {
                strEmail = strEmail2;
            }
            return updateUsimRecordInEfByIndex(efType, id.intValue(), newTag4, newNumber3, strAnr, strEmail, subId);
        }
        if (7 == match) {
            newNumber = newNumber4;
            newTag = newTag5;
        } else if (8 == match) {
            newNumber = newNumber4;
            newTag = newTag5;
        } else {
            if (index > 0) {
                logi("updateIccRecordInEfByIndex");
                result = updateIccRecordInEfByIndex(efType, index, newTag5, newNumber4, pin2, subId);
            } else {
                logi("updateIccRecordInEf");
                result = updateIccRecordInEf(efType, tag, number, newTag5, newNumber4, pin2, subId);
            }
            logi("update result = " + result);
            return result;
        }
        String strAnr3 = values.getAsString("newAnr");
        String strEmail3 = values.getAsString("newEmails");
        Integer aasIndex = values.getAsInteger("aas");
        String sne = values.getAsString("sne");
        if (newNumber == null) {
            newNumber2 = "";
        } else {
            newNumber2 = newNumber;
        }
        if (newTag == null) {
            newTag2 = "";
        } else {
            newTag2 = newTag;
        }
        MtkAdnRecord record = new MtkAdnRecord(efType, 0, newTag2, newNumber2);
        record.setAnr(strAnr3);
        if (values.containsKey("newAnr2")) {
            String strAnr22 = values.getAsString("newAnr2");
            if (DBG) {
                StringBuilder sb = new StringBuilder();
                newTag3 = newTag2;
                sb.append("update newAnr2: ");
                sb.append(strAnr22);
                log(sb.toString());
            } else {
                newTag3 = newTag2;
            }
            record.setAnr(strAnr22, 1);
        } else {
            newTag3 = newTag2;
        }
        if (values.containsKey("newAnr3")) {
            String strAnr32 = values.getAsString("newAnr3");
            if (DBG) {
                log("update newAnr3: " + strAnr32);
            }
            record.setAnr(strAnr32, 2);
        }
        if (strEmail3 == null || strEmail3.equals("")) {
            emails = null;
        } else {
            emails = new String[]{strEmail3};
        }
        record.setEmails(emails);
        if (aasIndex != null) {
            record.setAasIndex(aasIndex.intValue());
        }
        if (sne != null) {
            record.setSne(sne);
        }
        if (index > 0) {
            if (ADDRESS_BOOK_COLUMN_NAMES.length >= 8) {
                logi("updateUsimPBRecordsByIndexWithError");
                result = updateUsimPBRecordsByIndexWithError(efType, record, index, subId);
            } else {
                logi("updateUsimRecordInEfByIndex");
                result = updateUsimRecordInEfByIndex(efType, index, newTag3, newNumber2, strAnr3, strEmail3, subId);
            }
        } else if (ADDRESS_BOOK_COLUMN_NAMES.length >= 8) {
            logi("updateUsimPBRecordsBySearchWithError");
            result = updateUsimPBRecordsBySearchWithError(efType, new MtkAdnRecord(tag, number, ""), record, subId);
        } else {
            logi("updateUsimRecordInEf");
            result = updateUsimRecordInEf(efType, tag, number, newTag3, newNumber2, strAnr3, strEmail3, subId);
        }
        logi("update result = " + result);
        return result;
    }

    private int addIccRecordToEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("addIccRecordToEf: efType=0x");
            sb.append(Integer.toHexString(efType).toUpperCase());
            sb.append(", name=");
            sb.append(getMaskString(name));
            sb.append(", number=");
            sb.append(getMaskString(number));
            sb.append(", emails=");
            sb.append(emails == null ? "null" : getMaskString(emails[0]));
            sb.append(", subscription=");
            sb.append(subId);
            log(sb.toString());
        }
        int result = 0;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfBySearchWithError(subId, efType, "", "", name, number, pin2);
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
        String[] emails;
        int result;
        if (DBG) {
            log("addUSIMRecordToEf: efType=" + efType + ", name=" + getMaskString(name) + ", number=" + getMaskString(number) + ", anr =" + getMaskString(strAnr) + ", emails=" + getMaskString(strEmail) + ", subId=" + subId);
        }
        int result2 = 0;
        if (strEmail == null || strEmail.equals("")) {
            emails = null;
        } else {
            emails = new String[]{strEmail};
        }
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result2 = iccIpb.updateUsimPBRecordsInEfBySearchWithError(subId, efType, "", "", "", null, null, name, number, strAnr, null, emails);
            }
            result = result2;
        } catch (RemoteException ex) {
            log(ex.toString());
            result = 0;
            log("addUsimRecordToEf: " + result);
            return result;
        } catch (SecurityException ex2) {
            log(ex2.toString());
            result = 0;
            log("addUsimRecordToEf: " + result);
            return result;
        }
        log("addUsimRecordToEf: " + result);
        return result;
    }

    private int updateIccRecordInEf(int efType, String oldName, String oldNumber, String newName, String newNumber, String pin2, int subId) {
        if (DBG) {
            log("updateIccRecordInEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", oldname=" + getMaskString(oldName) + ", oldnumber=" + getMaskString(oldNumber) + ", newname=" + getMaskString(newName) + ", newnumber=" + getMaskString(newNumber) + ", subscription=" + subId);
        }
        int result = 0;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
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
            log("updateIccRecordInEfByIndex: efType=" + efType + ", index=" + nIndex + ", newname=" + getMaskString(newName) + ", newnumber=" + getMaskString(newNumber));
        }
        int result = 0;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
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
        String[] emails;
        int result;
        if (DBG) {
            log("updateUsimRecordInEf: efType=" + efType + ", oldname=" + getMaskString(oldName) + ", oldnumber=" + getMaskString(oldNumber) + ", newname=" + getMaskString(newName) + ", newnumber=" + getMaskString(newNumber) + ", anr =" + getMaskString(strAnr) + ", emails=" + getMaskString(strEmail));
        }
        int result2 = 0;
        if (strEmail != null) {
            emails = new String[]{strEmail};
        } else {
            emails = null;
        }
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result2 = iccIpb.updateUsimPBRecordsInEfBySearchWithError(subId, efType, oldName, oldNumber, "", null, null, newName, newNumber, strAnr, null, emails);
            }
            result = result2;
        } catch (RemoteException ex) {
            log(ex.toString());
            result = 0;
            log("updateUsimRecordInEf: " + result);
            return result;
        } catch (SecurityException ex2) {
            log(ex2.toString());
            result = 0;
            log("updateUsimRecordInEf: " + result);
            return result;
        }
        log("updateUsimRecordInEf: " + result);
        return result;
    }

    private int updateUsimRecordInEfByIndex(int efType, int nIndex, String newName, String newNumber, String strAnr, String strEmail, int subId) {
        String[] emails;
        int result;
        if (DBG) {
            log("updateUsimRecordInEfByIndex: efType=" + efType + ", Index=" + nIndex + ", newname=" + getMaskString(newName) + ", newnumber=" + getMaskString(newNumber) + ", anr =" + getMaskString(strAnr) + ", emails=" + getMaskString(strEmail));
        }
        int result2 = 0;
        if (strEmail != null) {
            emails = new String[]{strEmail};
        } else {
            emails = null;
        }
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result2 = iccIpb.updateUsimPBRecordsInEfByIndexWithError(subId, efType, newName, newNumber, strAnr, null, emails, nIndex);
            }
            result = result2;
        } catch (RemoteException ex) {
            log(ex.toString());
            result = 0;
            log("updateUsimRecordInEfByIndex: " + result);
            return result;
        } catch (SecurityException ex2) {
            log(ex2.toString());
            result = 0;
            log("updateUsimRecordInEfByIndex: " + result);
            return result;
        }
        log("updateUsimRecordInEfByIndex: " + result);
        return result;
    }

    private int deleteIccRecordFromEf(int efType, String name, String number, String[] emails, String pin2, int subId) {
        if (DBG) {
            log("deleteIccRecordFromEf: efType=0x" + Integer.toHexString(efType).toUpperCase() + ", name=" + getMaskString(name) + ", number=" + getMaskString(number) + ", pin2=" + getMaskString(pin2) + ", subscription=" + subId);
        }
        int result = 0;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfBySearchWithError(subId, efType, name, number, "", "", pin2);
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
            log("deleteIccRecordFromEfByIndex: efType=" + efType + ", index=" + nIndex + ", pin2=" + getMaskString(pin2));
        }
        int result = 0;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateAdnRecordsInEfByIndexWithError(subId, efType, "", "", nIndex, pin2);
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
        int result;
        if (DBG) {
            log("deleteUsimRecordFromEf: efType=" + efType + ", name=" + getMaskString(name) + ", number=" + getMaskString(number));
        }
        int result2 = 0;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result2 = iccIpb.updateUsimPBRecordsInEfBySearchWithError(subId, efType, name, number, "", null, null, "", "", "", null, null);
            }
            result = result2;
        } catch (RemoteException ex) {
            log(ex.toString());
            result = 0;
            log("deleteUsimRecordFromEf: " + result);
            return result;
        } catch (SecurityException ex2) {
            log(ex2.toString());
            result = 0;
            log("deleteUsimRecordFromEf: " + result);
            return result;
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
            IMtkIccPhoneBook iccIpb = getIccPhbService();
            if (iccIpb != null) {
                result = iccIpb.updateUsimPBRecordsInEfByIndexWithError(subId, efType, "", "", "", null, null, nIndex);
            }
        } catch (RemoteException ex) {
            log(ex.toString());
        } catch (SecurityException ex2) {
            log(ex2.toString());
        }
        log("deleteUsimRecordFromEfByIndex: " + result);
        return result;
    }

    private void loadRecord(MtkAdnRecord record, MatrixCursor cursor, int id) {
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
            if (SystemProperties.get("ro.vendor.mtk_kor_customization").equals("1") && alphaTag.length() >= 2 && alphaTag.charAt(0) == 65278) {
                String strKSC = "";
                try {
                    strKSC = new String(alphaTag.substring(1).getBytes("utf-16be"), "KSC5601");
                } catch (UnsupportedEncodingException ex) {
                    if (DBG) {
                        log("Implausible UnsupportedEncodingException : " + ex);
                    }
                }
                int ucslen = strKSC.length();
                while (ucslen > 0 && strKSC.charAt(ucslen - 1) == 63735) {
                    ucslen--;
                }
                contact[1] = strKSC.substring(0, ucslen);
                if (DBG) {
                    log("Decode ADN using KSC5601 : " + contact[1]);
                }
            }
            if (emails != null) {
                StringBuilder emailString = new StringBuilder();
                for (String email : emails) {
                    if (email != null) {
                        emailString.append(email);
                        contact[3] = emailString.toString();
                    }
                }
            }
            contact[4] = record.getAdditionalNumber();
            contact[5] = grpIds;
            contact[6] = Integer.valueOf(id);
            cursor.addRow(contact);
        }
    }

    private void log(String msg) {
        Rlog.d(TAG, msg);
    }

    private void logi(String msg) {
        Rlog.i(TAG, msg);
    }

    private IMtkIccPhoneBook getIccPhbService() {
        return IMtkIccPhoneBook.Stub.asInterface(ServiceManager.getService("mtksimphonebook"));
    }

    private int getRequestSubId(Uri url) {
        if (DBG) {
            log("getRequestSubId url: " + url);
        }
        try {
            return Integer.parseInt(url.getLastPathSegment());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown URL " + url);
        } catch (Exception e2) {
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    private int updateUsimPBRecordsBySearchWithError(int efType, MtkAdnRecord oldAdn, MtkAdnRecord newAdn, int subId) {
        if (DBG) {
            log("updateUsimPBRecordsBySearchWithError subId:" + subId + ",oldAdn:" + oldAdn + ",newAdn:" + newAdn);
        }
        int result = 0;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
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

    private int updateUsimPBRecordsByIndexWithError(int efType, MtkAdnRecord newAdn, int index, int subId) {
        if (DBG) {
            log("updateUsimPBRecordsByIndexWithError subId:" + subId + ",index:" + index + ",newAdn:" + newAdn);
        }
        int result = 0;
        try {
            IMtkIccPhoneBook iccIpb = getIccPhbService();
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
                if (!CsimPhbUtil.hasModemPhbEnhanceCapability(phone.getIccFileHandler())) {
                    res = CsimPhbUtil.updatePhbStorageInfo(update);
                } else {
                    log("[updatePhbStorageInfo] is not a csim card");
                    res = false;
                }
            }
        } catch (SecurityException ex) {
            log(ex.toString());
        }
        log("[updatePhbStorageInfo] res = " + res);
    }

    private String getMaskString(String str) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= 2) {
            return "xx";
        }
        return str.substring(0, str.length() >> 1) + "xxxxx";
    }
}
