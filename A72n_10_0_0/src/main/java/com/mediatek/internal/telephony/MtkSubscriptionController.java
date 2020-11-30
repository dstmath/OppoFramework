package com.mediatek.internal.telephony;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IOppoUiccManager;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.MultiSimSettingController;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyPermissions;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MtkSubscriptionController extends SubscriptionController {
    private static final boolean ENGDEBUG = TextUtils.equals(Build.TYPE, "eng");
    private static final String LOG_TAG = "MtkSubCtrl";
    static final int MAX_LOCAL_LOG_LINES = 500;
    private static final int PROJECT_SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    static String[] initIccid = new String[PROJECT_SIM_NUM];
    private static final boolean sIsOP01 = DataSubConstants.OPERATOR_OP01.equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""));
    private static final boolean sIsOP02 = DataSubConstants.OPERATOR_OP02.equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""));
    private static MtkSubscriptionController sMtkInstance = null;
    private static Intent sStickyIntent = null;
    private int lastPhoneId = EndcBearController.INVALID_INT;
    private boolean mIsReady = false;

    protected static MtkSubscriptionController mtkInit(Phone phone) {
        MtkSubscriptionController mtkSubscriptionController;
        synchronized (MtkSubscriptionController.class) {
            if (sMtkInstance == null) {
                sMtkInstance = new MtkSubscriptionController(phone);
                Rlog.d(LOG_TAG, "mtkInit, sMtkInstance = " + sMtkInstance);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sMtkInstance = " + sMtkInstance);
            }
            mtkSubscriptionController = sMtkInstance;
        }
        return mtkSubscriptionController;
    }

    protected static MtkSubscriptionController mtkInit(Context c, CommandsInterface[] ci) {
        MtkSubscriptionController mtkSubscriptionController;
        synchronized (MtkSubscriptionController.class) {
            if (sMtkInstance == null) {
                sMtkInstance = new MtkSubscriptionController(c);
                Rlog.d(LOG_TAG, "mtkInit, sMtkInstance = " + sMtkInstance);
                MtkSubscriptionControllerEx.MtkInitStub(c);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sMtkInstance = " + sMtkInstance);
            }
            mtkSubscriptionController = sMtkInstance;
        }
        return mtkSubscriptionController;
    }

    protected MtkSubscriptionController(Context c) {
        super(c);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0041 A[SYNTHETIC] */
    public void clearSlotIndexForSubInfoRecords() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(i)}, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        initIccid[i] = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
                        if (cursor == null) {
                            cursor.close();
                        }
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            initIccid[i] = DataSubConstants.NO_SIM_VALUE;
            if (cursor == null) {
            }
        }
        MtkSubscriptionController.super.clearSlotIndexForSubInfoRecords();
    }

    public static MtkSubscriptionController getMtkInstance() {
        MtkSubscriptionController mtkSubscriptionController;
        synchronized (MtkSubscriptionController.class) {
            mtkSubscriptionController = sMtkInstance;
        }
        return mtkSubscriptionController;
    }

    protected MtkSubscriptionController(Phone phone) {
        super(phone);
    }

    public void notifySubscriptionInfoChanged() {
        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            logd("notifySubscriptionInfoChanged:");
            tr.notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        broadcastSimInfoContentChanged(null);
        MultiSimSettingController.getInstance().notifySubscriptionInfoChanged();
        TelephonyMetrics metrics = TelephonyMetrics.getInstance();
        List<SubscriptionInfo> subInfoList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        if (subInfoList == null) {
            subInfoList = new ArrayList<>();
        }
        metrics.updateActiveSubscriptionInfoList(Collections.unmodifiableList(subInfoList));
    }

    /* access modifiers changed from: protected */
    public SubscriptionInfo getSubInfoRecord(Cursor cursor) {
        UiccAccessRule[] accessRules;
        String number;
        String cardId;
        int subType;
        String number2;
        String number3;
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String iccId = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
        int simSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
        String carrierName = cursor.getString(cursor.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor.getInt(cursor.getColumnIndexOrThrow("name_source"));
        int iconTint = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
        String number4 = cursor.getString(cursor.getColumnIndexOrThrow(PplMessageManager.PendingMessage.KEY_NUMBER));
        int dataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow("data_roaming"));
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302816);
        String mcc = cursor.getString(cursor.getColumnIndexOrThrow("mcc_string"));
        String mnc = cursor.getString(cursor.getColumnIndexOrThrow("mnc_string"));
        String ehplmnsRaw = cursor.getString(cursor.getColumnIndexOrThrow("ehplmns"));
        String hplmnsRaw = cursor.getString(cursor.getColumnIndexOrThrow("hplmns"));
        String[] ehplmns = ehplmnsRaw == null ? null : ehplmnsRaw.split(",");
        String[] hplmns = hplmnsRaw == null ? null : hplmnsRaw.split(",");
        String cardId2 = cursor.getString(cursor.getColumnIndexOrThrow("card_id"));
        String countryIso = cursor.getString(cursor.getColumnIndexOrThrow("iso_country_code"));
        int publicCardId = this.mUiccController.convertToPublicCardId(cardId2);
        boolean isOpportunistic = false;
        boolean isEmbedded = cursor.getInt(cursor.getColumnIndexOrThrow("is_embedded")) == 1;
        int carrierId = cursor.getInt(cursor.getColumnIndexOrThrow("carrier_id"));
        if (isEmbedded) {
            number = number4;
            accessRules = UiccAccessRule.decodeRules(cursor.getBlob(cursor.getColumnIndexOrThrow("access_rules")));
        } else {
            number = number4;
            accessRules = null;
        }
        if (cursor.getInt(cursor.getColumnIndexOrThrow("is_opportunistic")) == 1) {
            isOpportunistic = true;
        }
        String groupUUID = cursor.getString(cursor.getColumnIndexOrThrow("group_uuid"));
        int profileClass = cursor.getInt(cursor.getColumnIndexOrThrow("profile_class"));
        int subType2 = cursor.getInt(cursor.getColumnIndexOrThrow("subscription_type"));
        String groupOwner = getOptionalStringFromCursor(cursor, "group_owner", null);
        if (VDBG) {
            String iccIdToPrint = MtkSubscriptionInfo.givePrintableIccid(iccId);
            String cardIdToPrint = MtkSubscriptionInfo.givePrintableIccid(cardId2);
            cardId = cardId2;
            StringBuilder sb = new StringBuilder();
            sb.append("[getSubInfoRecord] id:");
            sb.append(id);
            sb.append(" iccid:");
            sb.append(iccIdToPrint);
            sb.append(" simSlotIndex:");
            sb.append(simSlotIndex);
            sb.append(" carrierid:");
            sb.append(carrierId);
            sb.append(" displayName:");
            sb.append(displayName);
            sb.append(" nameSource:");
            sb.append(nameSource);
            sb.append(" iconTint:");
            sb.append(iconTint);
            sb.append(" dataRoaming:");
            sb.append(dataRoaming);
            sb.append(" mcc:");
            sb.append(mcc);
            sb.append(" mnc:");
            sb.append(mnc);
            sb.append(" countIso:");
            sb.append(countryIso);
            sb.append(" isEmbedded:");
            sb.append(isEmbedded);
            sb.append(" accessRules:");
            sb.append(Arrays.toString(accessRules));
            sb.append(" cardId:");
            sb.append(cardIdToPrint);
            sb.append(" publicCardId:");
            sb.append(publicCardId);
            sb.append(" isOpportunistic:");
            sb.append(isOpportunistic);
            sb.append(" groupUUID:");
            sb.append(groupUUID);
            sb.append(" profileClass:");
            sb.append(profileClass);
            sb.append(" subscriptionType: ");
            subType = subType2;
            sb.append(subType);
            logd(sb.toString());
        } else {
            cardId = cardId2;
            subType = subType2;
        }
        String line1Number = this.mTelephonyManager.getLine1Number(id);
        if (!TextUtils.isEmpty(line1Number)) {
            number3 = number;
            if (!line1Number.equals(number3)) {
                number2 = line1Number;
                MtkSubscriptionInfo info = new MtkSubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number2, dataRoaming, iconBitmap, mcc, mnc, countryIso, isEmbedded, accessRules, cardId, publicCardId, isOpportunistic, groupUUID, false, carrierId, profileClass, subType, groupOwner, cursor.getInt(cursor.getColumnIndexOrThrow("sub_state")), -1);
                info.setAssociatedPlmns(ehplmns, hplmns);
                return info;
            }
        } else {
            number3 = number;
        }
        number2 = number3;
        MtkSubscriptionInfo info2 = new MtkSubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number2, dataRoaming, iconBitmap, mcc, mnc, countryIso, isEmbedded, accessRules, cardId, publicCardId, isOpportunistic, groupUUID, false, carrierId, profileClass, subType, groupOwner, cursor.getInt(cursor.getColumnIndexOrThrow("sub_state")), -1);
        info2.setAssociatedPlmns(ehplmns, hplmns);
        return info2;
    }

    /* JADX INFO: Multiple debug info for r14v18 'value'  android.content.ContentValues: [D('recordsDoNotExist' boolean), D('value' android.content.ContentValues)] */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0263 A[SYNTHETIC, Splitter:B:113:0x0263] */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0279 A[Catch:{ all -> 0x04d9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x028e A[Catch:{ all -> 0x04d9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x02a5 A[SYNTHETIC, Splitter:B:122:0x02a5] */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x039a  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x03a6  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x03ad  */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x04f0 A[SYNTHETIC, Splitter:B:184:0x04f0] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0110  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0149 A[Catch:{ all -> 0x016e }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x017d  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01b5  */
    public int addSubInfo(String uniqueId, String displayName, int slotIndex, int subscriptionType) {
        long identity;
        Throwable th;
        String[] args;
        String selection;
        boolean z;
        Cursor cursor;
        Throwable th2;
        boolean recordsDoNotExist;
        String str;
        boolean setDisplayName;
        int slotIndex2;
        String[] args2;
        String selection2;
        int i;
        Cursor cursor2;
        int i2;
        ContentResolver resolver;
        String nameToSet;
        boolean recordsDoNotExist2;
        int subId;
        int oldSimInfoId;
        int nameSource;
        String oldIccId;
        ContentValues value;
        DcTracker mDcTracker;
        String cardId;
        boolean recordsDoNotExist3;
        int slotIndex3 = slotIndex;
        String iccIdStr = uniqueId;
        if (!isSubscriptionForRemoteSim(subscriptionType)) {
            iccIdStr = MtkSubscriptionInfo.givePrintableIccid(uniqueId);
        }
        logdl("[addSubInfoRecord]+ iccid: " + iccIdStr + ", slotIndex: " + slotIndex3 + ", subscriptionType: " + subscriptionType);
        enforceModifyPhoneState("addSubInfo");
        long identity2 = Binder.clearCallingIdentity();
        if (uniqueId == null) {
            try {
                logdl("[addSubInfo]- null iccId");
                Binder.restoreCallingIdentity(identity2);
                return -1;
            } catch (Throwable th3) {
                th = th3;
                identity = identity2;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            try {
                String uniqueId2 = uniqueId.toUpperCase();
                try {
                    ContentResolver resolver2 = this.mContext.getContentResolver();
                    if (isSubscriptionForRemoteSim(subscriptionType)) {
                        try {
                            selection = "icc_id=? AND subscription_type=?";
                            args = new String[]{uniqueId2, Integer.toString(subscriptionType)};
                            z = false;
                        } catch (Throwable th4) {
                            th = th4;
                            identity = identity2;
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                    } else {
                        z = false;
                        selection = "icc_id=? OR icc_id=? OR icc_id=?";
                        args = new String[]{uniqueId2, IccUtils.getDecimalSubstring(uniqueId2), uniqueId2.toLowerCase()};
                    }
                    Cursor cursor3 = resolver2.query(SubscriptionManager.CONTENT_URI, new String[]{"_id", "sim_id", "name_source", "icc_id", "card_id"}, selection, args, null);
                    if (cursor3 != null) {
                        try {
                            if (cursor3.moveToFirst()) {
                                recordsDoNotExist = z;
                                boolean setDisplayName2 = false;
                                identity = identity2;
                                if (isSubscriptionForRemoteSim(subscriptionType)) {
                                    if (!recordsDoNotExist) {
                                        str = "_id";
                                        recordsDoNotExist3 = recordsDoNotExist;
                                        IOppoUiccManager uiccManager = OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0]);
                                        if (uiccManager != null) {
                                            recordsDoNotExist2 = uiccManager.isRecordsDoNotExist(uniqueId, slotIndex3, cursor3);
                                            if (!recordsDoNotExist2) {
                                                try {
                                                    logdl("[addSubInfoRecord] New record created: " + insertEmptySubInfoRecord(uniqueId2, slotIndex3));
                                                    Phone mPhone = PhoneFactory.getPhone(slotIndex);
                                                    if (mPhone != null) {
                                                        DcTracker mDcTracker2 = mPhone.getDcTracker(1);
                                                        if (mDcTracker2 != null) {
                                                            mDcTracker2.updateMapValue(slotIndex3, 1);
                                                        }
                                                    }
                                                    setDisplayName = true;
                                                    slotIndex2 = slotIndex3;
                                                } catch (Throwable th5) {
                                                    th2 = th5;
                                                    cursor = cursor3;
                                                    if (cursor != null) {
                                                    }
                                                    throw th2;
                                                }
                                            } else {
                                                try {
                                                    subId = cursor3.getInt(0);
                                                    oldSimInfoId = cursor3.getInt(1);
                                                    nameSource = cursor3.getInt(2);
                                                    oldIccId = cursor3.getString(3);
                                                } catch (Throwable th6) {
                                                    th2 = th6;
                                                    cursor = cursor3;
                                                    if (cursor != null) {
                                                    }
                                                    throw th2;
                                                }
                                                try {
                                                    String oldCardId = cursor3.getString(4);
                                                    ContentValues value2 = new ContentValues();
                                                    if (slotIndex3 != oldSimInfoId) {
                                                        try {
                                                            value = value2;
                                                            value.put("sim_id", Integer.valueOf(slotIndex));
                                                        } catch (Throwable th7) {
                                                            th2 = th7;
                                                            cursor = cursor3;
                                                            if (cursor != null) {
                                                            }
                                                            throw th2;
                                                        }
                                                    } else {
                                                        value = value2;
                                                    }
                                                    if (2 != nameSource) {
                                                        setDisplayName2 = true;
                                                    }
                                                    if (oldIccId != null && ((oldIccId.length() < uniqueId2.length() && oldIccId.equals(IccUtils.getDecimalSubstring(uniqueId2))) || (!uniqueId2.toLowerCase().equals(uniqueId2) && uniqueId2.toLowerCase().equals(oldIccId)))) {
                                                        value.put("icc_id", uniqueId2);
                                                    }
                                                    UiccCard card = this.mUiccController.getUiccCardForPhone(slotIndex3);
                                                    if (!(card == null || (cardId = card.getCardId()) == null || cardId == oldCardId)) {
                                                        value.put("card_id", cardId);
                                                    }
                                                    if (value.size() > 0) {
                                                        resolver2.update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
                                                    }
                                                    logdl("[addSubInfoRecord] Record already exists");
                                                    Phone mPhone2 = PhoneFactory.getPhone(slotIndex);
                                                    if (!(mPhone2 == null || (mDcTracker = mPhone2.getDcTracker(1)) == null)) {
                                                        mDcTracker.updateMapValue(slotIndex3, 0);
                                                    }
                                                    slotIndex2 = slotIndex3;
                                                    setDisplayName = setDisplayName2;
                                                } catch (Throwable th8) {
                                                    th2 = th8;
                                                    cursor = cursor3;
                                                    if (cursor != null) {
                                                    }
                                                    throw th2;
                                                }
                                            }
                                        } else {
                                            logd("[addSubInfoRecord] OppoUiccManager is null:");
                                        }
                                    } else {
                                        str = "_id";
                                        recordsDoNotExist3 = recordsDoNotExist;
                                    }
                                    recordsDoNotExist2 = recordsDoNotExist3;
                                    if (!recordsDoNotExist2) {
                                    }
                                } else if (recordsDoNotExist) {
                                    slotIndex3 = -1;
                                    try {
                                        logd("[addSubInfoRecord] New record created: " + insertEmptySubInfoRecord(uniqueId2, displayName, -1, subscriptionType));
                                        str = "_id";
                                        setDisplayName = false;
                                        slotIndex2 = -1;
                                    } catch (Throwable th9) {
                                        th2 = th9;
                                        cursor = cursor3;
                                        if (cursor != null) {
                                        }
                                        throw th2;
                                    }
                                } else {
                                    try {
                                        logdl("[addSubInfoRecord] Record already exists");
                                        str = "_id";
                                        setDisplayName = false;
                                        slotIndex2 = slotIndex3;
                                    } catch (Throwable th10) {
                                        th2 = th10;
                                        cursor = cursor3;
                                        if (cursor != null) {
                                        }
                                        throw th2;
                                    }
                                }
                                if (cursor3 != null) {
                                    try {
                                        cursor3.close();
                                    } catch (Throwable th11) {
                                        th = th11;
                                        Binder.restoreCallingIdentity(identity);
                                        throw th;
                                    }
                                }
                                String[] args3 = {String.valueOf(slotIndex2)};
                                if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                    i = 0;
                                    selection2 = "icc_id=? AND subscription_type=?";
                                    args2 = new String[]{uniqueId2, Integer.toString(subscriptionType)};
                                } else {
                                    i = 0;
                                    selection2 = "sim_id=?";
                                    args2 = args3;
                                }
                                cursor2 = resolver2.query(SubscriptionManager.CONTENT_URI, null, selection2, args2, null);
                                if (cursor2 != null) {
                                    try {
                                        if (cursor2.moveToFirst()) {
                                            while (true) {
                                                int subId2 = cursor2.getInt(cursor2.getColumnIndexOrThrow(str));
                                                if (addToSubIdList(slotIndex2, subId2, subscriptionType)) {
                                                    int subIdCountMax = getActiveSubInfoCountMax();
                                                    int defaultSubId = getDefaultSubId();
                                                    logdl("[addSubInfoRecord] sSlotIndexToSubIds.size=" + sSlotIndexToSubIds.size() + " slotIndex=" + slotIndex2 + " subId=" + subId2 + " defaultSubId=" + defaultSubId + " simCount=" + subIdCountMax);
                                                    if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                                        if (!SubscriptionManager.isValidSubscriptionId(defaultSubId) || subIdCountMax == 1 || !isActiveSubId(defaultSubId) || !isActiveSubId(mDefaultFallbackSubId) || (mDefaultFallbackSubId == subId2 && this.lastPhoneId != slotIndex2)) {
                                                            logdl("setting default fallback subid to " + subId2);
                                                            setDefaultFallbackSubId(subId2, subscriptionType);
                                                            this.lastPhoneId = slotIndex2;
                                                        }
                                                        if (subIdCountMax == 1) {
                                                            logdl("[addSubInfoRecord] one sim set defaults to subId=" + subId2);
                                                            setDefaultDataSubId(subId2);
                                                            setDefaultSmsSubId(subId2);
                                                            setDefaultVoiceSubId(subId2);
                                                        }
                                                    } else {
                                                        updateDefaultSubIdsIfNeeded(subId2, subscriptionType);
                                                    }
                                                } else {
                                                    logdl("[addSubInfoRecord] current SubId is already known, IGNORE");
                                                }
                                                logdl("[addSubInfoRecord] hashmap(" + slotIndex2 + "," + subId2 + ")");
                                                if (!cursor2.moveToNext()) {
                                                    break;
                                                }
                                                str = str;
                                            }
                                        }
                                    } catch (Throwable th12) {
                                        cursor2.close();
                                        throw th12;
                                    }
                                }
                                if (cursor2 != null) {
                                    cursor2.close();
                                }
                                refreshCachedActiveSubscriptionInfoList();
                                if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                    notifySubscriptionInfoChanged();
                                    i2 = i;
                                } else {
                                    int subId3 = getSubIdUsingPhoneId(slotIndex2);
                                    if (!SubscriptionManager.isValidSubscriptionId(subId3)) {
                                        logdl("[addSubInfoRecord]- getSubId failed invalid subId = " + subId3);
                                        Binder.restoreCallingIdentity(identity);
                                        return -1;
                                    }
                                    if (setDisplayName) {
                                        String simCarrierName = this.mTelephonyManager.getSimOperatorName(subId3);
                                        if (!OemConstant.EXP_VERSION) {
                                            String imsi = TelephonyManager.getDefault().getSubscriberId(subId3);
                                            logdl("[addSubInfoRecord] imsi = " + imsi + "of " + subId3);
                                            i2 = i;
                                            resolver = resolver2;
                                            nameToSet = getCarrierName(this.mContext, simCarrierName, imsi, uniqueId2, slotIndex2);
                                        } else if (SystemProperties.getInt("gsm.vsim.slotid", -1) == slotIndex2) {
                                            nameToSet = OemConstant.getOemRes(this.mContext, "redtea_virtul_card", "");
                                            logdl("[addSubInfoRecord]It's red tea, nameToSet = " + nameToSet);
                                            i2 = i;
                                            resolver = resolver2;
                                        } else {
                                            nameToSet = getExportSimDefaultName(slotIndex2);
                                            i2 = i;
                                            resolver = resolver2;
                                        }
                                        logdl("[addSubInfoRecord] nameToSet = " + nameToSet);
                                        logdl("[addSubInfoRecord] simCarrierName = " + simCarrierName);
                                        ContentValues value3 = new ContentValues();
                                        value3.put("color", Integer.valueOf(OemConstant.getCardType((String) null, uniqueId2)));
                                        value3.put("display_name", nameToSet);
                                        resolver.update(SubscriptionManager.getUriForSubscriptionId(subId3), value3, null, null);
                                        logdl("[addSubInfoRecord] sim name = " + nameToSet);
                                    } else {
                                        i2 = i;
                                    }
                                    refreshCachedActiveSubscriptionInfoList();
                                    broadcastSubInfoUpdateIntent("-1", Integer.toString(subId3), "CARDTYPE");
                                    sPhones[slotIndex2].updateDataConnectionTracker();
                                    logdl("[addSubInfoRecord]- info size=" + sSlotIndexToSubIds.size());
                                }
                                Binder.restoreCallingIdentity(identity);
                                return i2;
                            }
                        } catch (Throwable th13) {
                            th2 = th13;
                            identity = identity2;
                            cursor = cursor3;
                            if (cursor != null) {
                            }
                            throw th2;
                        }
                    }
                    recordsDoNotExist = true;
                    try {
                        boolean setDisplayName22 = false;
                        identity = identity2;
                        if (isSubscriptionForRemoteSim(subscriptionType)) {
                        }
                        if (cursor3 != null) {
                        }
                        String[] args32 = {String.valueOf(slotIndex2)};
                        if (!isSubscriptionForRemoteSim(subscriptionType)) {
                        }
                        cursor2 = resolver2.query(SubscriptionManager.CONTENT_URI, null, selection2, args2, null);
                        if (cursor2 != null) {
                        }
                        if (cursor2 != null) {
                        }
                        refreshCachedActiveSubscriptionInfoList();
                        if (!isSubscriptionForRemoteSim(subscriptionType)) {
                        }
                        Binder.restoreCallingIdentity(identity);
                        return i2;
                    } catch (Throwable th14) {
                        th2 = th14;
                        identity = identity2;
                        cursor = cursor3;
                        if (cursor != null) {
                            try {
                                cursor.close();
                            } catch (Throwable th15) {
                                th = th15;
                                Binder.restoreCallingIdentity(identity);
                                throw th;
                            }
                        }
                        throw th2;
                    }
                } catch (Throwable th16) {
                    th = th16;
                    identity = identity2;
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } catch (Throwable th17) {
                th = th17;
                identity = identity2;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    public int getSlotIndex(int subId) {
        if (VDBG) {
            printStackTrace("[getSlotIndex] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            logd("[getSlotIndex]+ subId == SubscriptionManager.DEFAULT_SUBSCRIPTION_ID");
            subId = getDefaultSubId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("[getSlotIndex]- subId invalid");
            return -1;
        } else if (sSlotIndexToSubIds.size() == 0) {
            logd("[getSlotIndex]- size == 0, return SIM_NOT_INSERTED instead, subId =" + subId);
            return -1;
        } else {
            for (Map.Entry<Integer, ArrayList<Integer>> entry : sSlotIndexToSubIds.entrySet()) {
                int sim = entry.getKey().intValue();
                ArrayList<Integer> subs = entry.getValue();
                if (subs != null && subs.contains(Integer.valueOf(subId))) {
                    if (VDBG) {
                        logv("[getSlotIndex]- return =" + sim + ", subId = " + subId);
                    }
                    return sim;
                }
            }
            logd("[getSlotIndex]- return INVALID_SIM_SLOT_INDEX, subId = " + subId);
            return -1;
        }
    }

    public int getPhoneId(int subId) {
        int phoneId;
        if (VDBG) {
            printStackTrace("[getPhoneId] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
            logdl("[getPhoneId] asked for default subId=" + subId);
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            if (subId > -2 - getActiveSubInfoCountMax()) {
                phoneId = -2 - subId;
            } else {
                phoneId = -1;
            }
            if (VDBG) {
                logdl("[getPhoneId]- invalid subId = " + subId + " return = " + phoneId);
            }
            return phoneId;
        } else if (sSlotIndexToSubIds.size() == 0) {
            int phoneId2 = mDefaultPhoneId;
            logd("[getPhoneId]- no sims, returning default phoneId=" + phoneId2 + ", subId" + subId);
            return phoneId2;
        } else {
            for (Map.Entry<Integer, ArrayList<Integer>> entry : sSlotIndexToSubIds.entrySet()) {
                int sim = entry.getKey().intValue();
                ArrayList<Integer> subs = entry.getValue();
                if (subs != null && subs.contains(Integer.valueOf(subId))) {
                    if (VDBG) {
                        logdl("[getPhoneId]- found subId=" + subId + " phoneId=" + sim);
                    }
                    return sim;
                }
            }
            int phoneId3 = mDefaultPhoneId;
            logdl("[getPhoneId]- subId=" + subId + " not found return default phoneId=" + phoneId3);
            return phoneId3;
        }
    }

    public int clearSubInfo() {
        enforceModifyPhoneState("clearSubInfo");
        long identity = Binder.clearCallingIdentity();
        try {
            int size = sSlotIndexToSubIds.size();
            if (size == 0) {
                logdl("[clearSubInfo]- no simInfo size=" + size);
                return 0;
            }
            setReadyState(false);
            sSlotIndexToSubIds.clear();
            logdl("[clearSubInfo]- clear size=" + size);
            Binder.restoreCallingIdentity(identity);
            return size;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setDefaultDataSubId(int subId) {
        setDefaultDataSubIdWithResult(subId);
    }

    public int setSubscriptionProperty(int subId, String propKey, String propValue) {
        int ret = MtkSubscriptionController.super.setSubscriptionProperty(subId, propKey, propValue);
        if (ENGDEBUG) {
            logdl("[setSubscriptionProperty] propKey=" + propKey + ", propValue = " + propValue + ", subId=" + subId + ", Binder.getCallingPid and Binder.getCallingUid are " + Binder.getCallingPid() + "," + Binder.getCallingUid());
        }
        long token = Binder.clearCallingIdentity();
        if (ret != 0) {
            try {
                Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
                int phoneId = getPhoneId(subId);
                List<SubscriptionInfo> subInfoList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
                putSubinfoRecordUpdatedExtra(intent, phoneId, 4, subInfoList == null ? 0 : subInfoList.size(), propKey);
                notifySubscriptionInfoChanged(intent);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
        Binder.restoreCallingIdentity(token);
        return ret;
    }

    private void broadcastSimInfoContentChanged(Intent intentExt) {
        this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE"));
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        if (intentExt == null) {
            List<SubscriptionInfo> subInfoList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            putSubinfoRecordUpdatedExtra(intent, -1, 4, subInfoList == null ? 0 : subInfoList.size(), null);
        }
        synchronized (MtkSubscriptionController.class) {
            sStickyIntent = intentExt == null ? intent : intentExt;
            int detectedType = sStickyIntent.getIntExtra("simDetectStatus", 0);
            int phoneId = sStickyIntent.getIntExtra("phone", -1);
            if (ENGDEBUG) {
                logd("broadcast intent ACTION_SUBINFO_RECORD_UPDATED with detectType:" + detectedType + ", phoneId:" + phoneId);
            }
            this.mContext.sendStickyBroadcast(sStickyIntent);
        }
    }

    public void clearSubInfoRecord(int slotIndex) {
        setReadyState(false);
        MtkSubscriptionController.super.clearSubInfoRecord(slotIndex);
    }

    public boolean setDefaultDataSubIdWithResult(int subId) {
        int raf;
        enforceModifyPhoneState("setDefaultDataSubIdWithResult");
        long identity = Binder.clearCallingIdentity();
        if (subId != Integer.MAX_VALUE) {
            try {
                ProxyController proxyController = ProxyController.getInstance();
                int len = sPhones.length;
                logdl("[setDefaultDataSubIdWithResult] num phones=" + len + ", subId=" + subId + ", Binder.getCallingPid and Binder.getCallingUid are " + Binder.getCallingPid() + "," + Binder.getCallingUid());
                if (proxyController != null && SubscriptionManager.isValidSubscriptionId(subId)) {
                    RadioAccessFamily[] rafs = new RadioAccessFamily[len];
                    boolean atLeastOneMatch = false;
                    for (int phoneId = 0; phoneId < len; phoneId++) {
                        int id = sPhones[phoneId].getSubId();
                        if (id == subId) {
                            raf = proxyController.getMaxRafSupported();
                            atLeastOneMatch = true;
                        } else {
                            raf = proxyController.getMinRafSupported();
                        }
                        logdl("[setDefaultDataSubIdWithResult] phoneId=" + phoneId + " subId=" + id + " RAF=" + raf);
                        rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
                        if (getDefaultDataSubId() != subId) {
                            sPhones[phoneId].getServiceStateTracker().setSwitchingDdsState(true);
                        }
                    }
                    if (atLeastOneMatch) {
                        proxyController.setRadioCapability(rafs);
                    } else {
                        logdl("[setDefaultDataSubIdWithResult] no valid subId's found - not updating.");
                    }
                }
                updateAllDataConnectionTrackers();
                int previousDefaultSub = getDefaultSubId();
                Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
                MultiSimSettingController.getInstance().notifyDefaultDataSubChanged();
                broadcastDefaultDataSubIdChanged(subId);
                if (previousDefaultSub != getDefaultSubId()) {
                    sendDefaultChangedBroadcast(getDefaultSubId());
                }
                return true;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new RuntimeException("setDefaultDataSubIdWithResult called with DEFAULT_SUB_ID");
        }
    }

    public MtkSubscriptionInfo getSubscriptionInfo(String callingPackage, int subId) {
        MtkSubscriptionInfo si;
        String pkgName = callingPackage;
        if (callingPackage == null) {
            pkgName = this.mContext.getOpPackageName();
        }
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, pkgName, "getSubscriptionInfo")) {
            return null;
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("[getSubscriptionInfo]- invalid subId, subId =" + subId);
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "_id=?", new String[]{Long.toString((long) subId)}, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst() && (si = getSubInfoRecord(cursor)) != null) {
                        logd("[getSubscriptionInfo]+ subId=" + subId + ", subInfo=" + si);
                        cursor.close();
                        return si;
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            } else {
                logd("[getSubscriptionInfo]- Query fail");
            }
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            logd("[getSubscriptionInfo]- subId=" + subId + ",subInfo=null");
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public MtkSubscriptionInfo getSubscriptionInfoForIccId(String callingPackage, String iccId) {
        MtkSubscriptionInfo si;
        String pkgName = callingPackage;
        if (callingPackage == null) {
            pkgName = this.mContext.getOpPackageName();
        }
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, -1, pkgName, "getSubscriptionInfoForIccId")) {
            return null;
        }
        if (iccId == null) {
            logd("[getSubscriptionInfoForIccId]- null iccid");
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "icc_id=?", new String[]{iccId}, null);
            if (cursor != null) {
                do {
                    try {
                        if (cursor.moveToNext()) {
                            si = getSubInfoRecord(cursor);
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } while (si == null);
                logd("[getSubscriptionInfoForIccId]+ iccId=" + MtkSubscriptionInfo.givePrintableIccid(iccId) + ", subInfo=" + si);
                cursor.close();
                return si;
            }
            logd("[getSubscriptionInfoForIccId]- Query fail");
            if (cursor != null) {
                cursor.close();
            }
            Binder.restoreCallingIdentity(identity);
            logd("[getSubscriptionInfoForIccId]- iccId=" + iccId + ",subInfo=null");
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setDefaultDataSubIdWithoutCapabilitySwitch(int subId) {
        if (subId != Integer.MAX_VALUE) {
            if (ENGDEBUG) {
                logd("[setDefaultDataSubIdWithoutCapabilitySwitch] subId=" + subId + ", Binder.getCallingPid and Binder.getCallingUid are " + Binder.getCallingPid() + "," + Binder.getCallingUid());
            }
            updateAllDataConnectionTrackers();
            int previousDefaultSub = getDefaultSubId();
            Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
            MultiSimSettingController.getInstance().notifyDefaultDataSubChanged();
            broadcastDefaultDataSubIdChanged(subId);
            if (previousDefaultSub != getDefaultSubId()) {
                sendDefaultChangedBroadcast(getDefaultSubId());
                return;
            }
            return;
        }
        throw new RuntimeException("setDefaultDataSubIdWithoutCapabilitySwitch called with DEFAULT_SUB_ID");
    }

    public void notifySubscriptionInfoChanged(Intent intent) {
        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            setReadyState(true);
            tr.notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        broadcastSimInfoContentChanged(intent);
        MultiSimSettingController.getInstance().notifySubscriptionInfoChanged();
        TelephonyMetrics metrics = TelephonyMetrics.getInstance();
        List<SubscriptionInfo> subInfoList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        if (subInfoList == null) {
            subInfoList = new ArrayList<>();
        }
        metrics.updateActiveSubscriptionInfoList(Collections.unmodifiableList(subInfoList));
    }

    public boolean isReady() {
        if (ENGDEBUG) {
            logd("[isReady]- " + this.mIsReady);
        }
        return this.mIsReady;
    }

    public void setReadyState(boolean isReady) {
        if (ENGDEBUG) {
            logd("[setReadyState]- " + isReady);
        }
        this.mIsReady = isReady;
    }

    public int getDefaultFallbackSubId() {
        return mDefaultFallbackSubId;
    }

    public void putSubinfoRecordUpdatedExtra(Intent intent, int phoneId, int detectedType, int subCount, String propKey) {
        logd("putSubinfoRecordUpdatedExtra: phoneId = " + phoneId + " detectedType = " + detectedType + " subCount = " + subCount + " propKey = " + propKey);
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds == null || subIds.length <= 0) {
            logd("putSubinfoRecordUpdatedExtra: no valid subs");
            intent.putExtra("phone", phoneId);
            intent.putExtra("slot", phoneId);
        } else {
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, subIds[0]);
        }
        intent.putExtra("simDetectStatus", detectedType);
        intent.putExtra("simCount", subCount);
        if (propKey != null) {
            intent.putExtra("simPropKey", propKey);
        } else {
            intent.putExtra("simPropKey", "");
        }
    }

    private void logv(String msg) {
        Rlog.v(LOG_TAG, msg);
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    private void logdl(String msg) {
        logd(msg);
        this.mLocalLog.log(msg);
    }

    public void setDefaultVoiceSubId(int subId) {
        MtkSubscriptionController.super.setDefaultVoiceSubId(subId);
        PhoneAccountHandle newHandle = subId == -1 ? null : this.mTelephonyManager.getPhoneAccountHandleForSubscriptionId(subId);
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService(TelecomManager.class);
        PhoneAccountHandle currentHandle = telecomManager.getUserSelectedOutgoingPhoneAccount();
        if (newHandle == null && currentHandle == null) {
            telecomManager.setUserSelectedOutgoingPhoneAccount(null);
            logd("[setDefaultVoiceSubId] setUserSelectedOutgoingPhoneAccount(null) when SIM plug out");
        }
    }

    public int setDisplayNumber(String number, int subId) {
        logd("[setDisplayNumber]+ subId:" + subId);
        return setDisplayNumber(number, subId, true);
    }

    public int getMtkSimOnoffState(int slotId) {
        return MtkTelephonyManagerEx.getDefault().getSimOnOffState(slotId);
    }
}
