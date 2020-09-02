package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.common.OppoFeatureCache;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.UiccSlotInfo;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.ArrayUtils;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SubscriptionController extends AbstractSubscriptionController {
    protected static final boolean DBG = true;
    protected static final boolean DBG_CACHE = false;
    private static final int DEPRECATED_SETTING = -1;
    private static final ParcelUuid INVALID_GROUP_UUID = ParcelUuid.fromString("00000000-0000-0000-0000-000000000000");
    private static final String LOG_TAG = "SubscriptionController";
    private static final Comparator<SubscriptionInfo> SUBSCRIPTION_INFO_COMPARATOR = $$Lambda$SubscriptionController$Nt_ojdeqo4C2mbuwymYLvwgOLGo.INSTANCE;
    protected static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    protected static int mDefaultFallbackSubId = -1;
    @UnsupportedAppUsage
    protected static int mDefaultPhoneId = KeepaliveStatus.INVALID_HANDLE;
    private static SubscriptionController sInstance = null;
    public static Phone[] sPhones;
    protected static Map<Integer, ArrayList<Integer>> sSlotIndexToSubIds = new ConcurrentHashMap();
    @UnsupportedAppUsage
    private int[] colorArr;
    private AppOpsManager mAppOps;
    protected final List<SubscriptionInfo> mCacheActiveSubInfoList = new ArrayList();
    private List<SubscriptionInfo> mCacheOpportunisticSubInfoList = new ArrayList();
    @UnsupportedAppUsage
    public Context mContext;
    private long mLastISubServiceRegTime;
    public final LocalLog mLocalLog = new LocalLog(200);
    @UnsupportedAppUsage
    public final Object mLock = new Object();
    private Object mSubInfoListLock = new Object();
    public TelephonyManager mTelephonyManager;
    protected UiccController mUiccController;

    static /* synthetic */ int lambda$static$0(SubscriptionInfo arg0, SubscriptionInfo arg1) {
        int flag = arg0.getSimSlotIndex() - arg1.getSimSlotIndex();
        if (flag == 0) {
            return arg0.getSubscriptionId() - arg1.getSubscriptionId();
        }
        return flag;
    }

    public static SubscriptionController init(Phone phone) {
        SubscriptionController subscriptionController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeSubscriptionController(phone);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    public static SubscriptionController init(Context c, CommandsInterface[] ci) {
        SubscriptionController subscriptionController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeSubscriptionController(c, ci);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    @UnsupportedAppUsage
    public static SubscriptionController getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    protected SubscriptionController(Context c) {
        init(c);
        migrateImsSettings();
        this.mReference = (IOppoSubscriptionController) OppoTelephonyFactory.getInstance().getFeature(IOppoSubscriptionController.DEFAULT, this, c);
    }

    /* access modifiers changed from: protected */
    public void init(Context c) {
        this.mContext = c;
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        try {
            this.mUiccController = UiccController.getInstance();
            this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
            if (ServiceManager.getService("isub") == null) {
                ServiceManager.addService("isub", this);
                this.mLastISubServiceRegTime = System.currentTimeMillis();
            }
            clearSlotIndexForSubInfoRecords();
            logdl("[SubscriptionController] init by Context");
        } catch (RuntimeException e) {
            throw new RuntimeException("UiccController has to be initialised before SubscriptionController init");
        }
    }

    public void notifySubInfoReady() {
        sendDefaultChangedBroadcast(SubscriptionManager.getDefaultSubscriptionId());
    }

    @UnsupportedAppUsage
    private boolean isSubInfoReady() {
        return SubscriptionInfoUpdater.isSubInfoInitialized();
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
    /* access modifiers changed from: protected */
    public void clearSlotIndexForSubInfoRecords() {
        if (this.mContext == null) {
            logel("[clearSlotIndexForSubInfoRecords] TelephonyManager or mContext is null");
            return;
        }
        ContentValues value = new ContentValues(1);
        value.put("sim_id", (Integer) -1);
        this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, null, null);
    }

    protected SubscriptionController(Phone phone) {
        this.mContext = phone.getContext();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        migrateImsSettings();
        clearSlotIndexForSubInfoRecords();
        logdl("[SubscriptionController] init by Phone");
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private void enforceReadPrivilegedPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
    }

    private void broadcastSimInfoContentChanged() {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
        Intent intent2 = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intent2.addFlags(536870912);
        this.mContext.sendBroadcast(intent2);
    }

    @UnsupportedAppUsage
    public void notifySubscriptionInfoChanged() {
        List<SubscriptionInfo> subInfos;
        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            logd("notifySubscriptionInfoChanged:");
            tr.notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        broadcastSimInfoContentChanged();
        MultiSimSettingController.getInstance().notifySubscriptionInfoChanged();
        TelephonyMetrics metrics = TelephonyMetrics.getInstance();
        synchronized (this.mSubInfoListLock) {
            subInfos = new ArrayList<>(this.mCacheActiveSubInfoList);
        }
        metrics.updateActiveSubscriptionInfoList(subInfos);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public SubscriptionInfo getSubInfoRecord(Cursor cursor) {
        UiccAccessRule[] accessRules;
        String number;
        String cardId;
        int publicCardId;
        int subType;
        String number2;
        String number3;
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID));
        String iccId = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
        int simSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
        String carrierName = cursor.getString(cursor.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor.getInt(cursor.getColumnIndexOrThrow("name_source"));
        int iconTint = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
        String number4 = cursor.getString(cursor.getColumnIndexOrThrow("number"));
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
        int publicCardId2 = this.mUiccController.convertToPublicCardId(cardId2);
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
            String iccIdToPrint = SubscriptionInfo.givePrintableIccid(iccId);
            String cardIdToPrint = SubscriptionInfo.givePrintableIccid(cardId2);
            StringBuilder sb = new StringBuilder();
            cardId = cardId2;
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
            publicCardId = publicCardId2;
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
            publicCardId = publicCardId2;
            subType = subType2;
        }
        String line1Number = this.mTelephonyManager.getLine1Number(id);
        if (!TextUtils.isEmpty(line1Number)) {
            number3 = number;
            if (!line1Number.equals(number3)) {
                number2 = line1Number;
                SubscriptionInfo info = new SubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number2, dataRoaming, iconBitmap, mcc, mnc, countryIso, isEmbedded, accessRules, cardId, publicCardId, isOpportunistic, groupUUID, false, carrierId, profileClass, subType, groupOwner);
                info.setAssociatedPlmns(ehplmns, hplmns);
                return info;
            }
        } else {
            number3 = number;
        }
        number2 = number3;
        SubscriptionInfo info2 = new SubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number2, dataRoaming, iconBitmap, mcc, mnc, countryIso, isEmbedded, accessRules, cardId, publicCardId, isOpportunistic, groupUUID, false, carrierId, profileClass, subType, groupOwner);
        info2.setAssociatedPlmns(ehplmns, hplmns);
        return info2;
    }

    /* access modifiers changed from: protected */
    public String getOptionalStringFromCursor(Cursor cursor, String column, String defaultVal) {
        int columnIndex = cursor.getColumnIndex(column);
        return columnIndex == -1 ? defaultVal : cursor.getString(columnIndex);
    }

    @UnsupportedAppUsage
    public List<SubscriptionInfo> getSubInfo(String selection, Object queryKey) {
        if (VDBG) {
            logd("selection:" + selection + ", querykey: " + queryKey);
        }
        String[] selectionArgs = null;
        if (queryKey != null) {
            selectionArgs = new String[]{queryKey.toString()};
        }
        ArrayList<SubscriptionInfo> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, selectionArgs, "sim_id ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                    if (subInfo != null) {
                        if (subList == null) {
                            subList = new ArrayList<>();
                        }
                        subList.add(subInfo);
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        } else {
            logd("Query fail");
        }
        if (cursor != null) {
            cursor.close();
        }
        return subList;
    }

    private int getUnusedColor(String callingPackage) {
        List<SubscriptionInfo> availableSubInfos = getActiveSubscriptionInfoList(callingPackage);
        this.colorArr = this.mContext.getResources().getIntArray(17236116);
        int colorIdx = 0;
        if (availableSubInfos != null) {
            int i = 0;
            while (i < this.colorArr.length) {
                int j = 0;
                while (j < availableSubInfos.size() && this.colorArr[i] != availableSubInfos.get(j).getIconTint()) {
                    j++;
                }
                if (j == availableSubInfos.size()) {
                    return this.colorArr[i];
                }
                i++;
            }
            colorIdx = availableSubInfos.size() % this.colorArr.length;
        }
        return this.colorArr[colorIdx];
    }

    @UnsupportedAppUsage
    public SubscriptionInfo getActiveSubscriptionInfo(int subId, String callingPackage) {
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, "getActiveSubscriptionInfo")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getSubscriptionId() == subId) {
                        logd("[getActiveSubscriptionInfo]+ subId=" + subId + " subInfo=" + si);
                        return si;
                    }
                }
            }
            logd("[getActiveSubscriptionInfo]- subId=" + subId + " subList=" + subList + " subInfo=null");
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getSubscriptionInfo(int subId) {
        List<SubscriptionInfo> subInfoList = getSubInfo("_id=" + subId, null);
        if (subInfoList == null || subInfoList.isEmpty()) {
            return null;
        }
        return subInfoList.get(0);
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccId(String iccId, String callingPackage) {
        SubscriptionInfo si = getActiveSubscriptionInfoForIccIdInternal(iccId);
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, si != null ? si.getSubscriptionId() : -1, callingPackage, "getActiveSubscriptionInfoForIccId")) {
            return null;
        }
        return si;
    }

    private SubscriptionInfo getActiveSubscriptionInfoForIccIdInternal(String iccId) {
        if (iccId == null) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (iccId.equals(si.getIccId())) {
                        logd("[getActiveSubInfoUsingIccId]+ iccId=" + iccId + " subInfo=" + si);
                        return si;
                    }
                }
            }
            logd("[getActiveSubInfoUsingIccId]+ iccId=" + iccId + " subList=" + subList + " subInfo=null");
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIndex, String callingPackage) {
        SubscriptionInfo si;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(50, Binder.getCallingUid());
        Phone phone = PhoneFactory.getPhone(slotIndex);
        if (phone == null) {
            loge("[getActiveSubscriptionInfoForSimSlotIndex] no phone, slotIndex=" + slotIndex);
            return null;
        } else if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, phone.getSubId(), callingPackage, "getActiveSubscriptionInfoForSimSlotIndex")) {
            return null;
        } else {
            long identity = Binder.clearCallingIdentity();
            try {
                List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
                if (subList != null) {
                    Iterator<SubscriptionInfo> it = subList.iterator();
                    do {
                        if (it.hasNext()) {
                            si = it.next();
                        } else {
                            logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex + " subId=null");
                        }
                    } while (si.getSimSlotIndex() != slotIndex);
                    logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex + " subId=" + si);
                    return si;
                }
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ subList=null");
                Binder.restoreCallingIdentity(identity);
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public List<SubscriptionInfo> getAllSubInfoList(String callingPackage) {
        if (VDBG) {
            logd("[getAllSubInfoList]+");
        }
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, -1, callingPackage, "getAllSubInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getSubInfo(null, null);
            if (subList != null) {
                if (VDBG) {
                    logd("[getAllSubInfoList]- " + subList.size() + " infos return");
                }
            } else if (VDBG) {
                logd("[getAllSubInfoList]- no info return");
            }
            return subList;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @UnsupportedAppUsage
    public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(49, Binder.getCallingUid());
        return getSubscriptionInfoListFromCacheHelper(callingPackage, this.mCacheActiveSubInfoList);
    }

    @VisibleForTesting
    public void refreshCachedActiveSubscriptionInfoList() {
        boolean opptSubListChanged;
        synchronized (this.mSubInfoListLock) {
            List<SubscriptionInfo> activeSubscriptionInfoList = getSubInfo("sim_id>=0 OR subscription_type=1", null);
            if (activeSubscriptionInfoList != null) {
                if (this.mCacheActiveSubInfoList.size() != activeSubscriptionInfoList.size() || !this.mCacheActiveSubInfoList.containsAll(activeSubscriptionInfoList)) {
                    logdl("Active subscription info list changed. " + activeSubscriptionInfoList);
                }
                this.mCacheActiveSubInfoList.clear();
                activeSubscriptionInfoList.sort(SUBSCRIPTION_INFO_COMPARATOR);
                this.mCacheActiveSubInfoList.addAll(activeSubscriptionInfoList);
            } else {
                logd("activeSubscriptionInfoList is null.");
                this.mCacheActiveSubInfoList.clear();
            }
            opptSubListChanged = refreshCachedOpportunisticSubscriptionInfoList();
        }
        if (opptSubListChanged) {
            notifyOpportunisticSubscriptionInfoChanged();
        }
    }

    @UnsupportedAppUsage
    public int getActiveSubInfoCount(String callingPackage) {
        List<SubscriptionInfo> records = getActiveSubscriptionInfoList(callingPackage);
        if (records != null) {
            int mActCount = 0;
            for (SubscriptionInfo subInfo : records) {
                if (getSubState(subInfo.getSubscriptionId()) == 1) {
                    mActCount++;
                }
            }
            logd("[getActiveSubInfoCount]- count: " + mActCount);
            return mActCount;
        } else if (!VDBG) {
            return 0;
        } else {
            logd("[getActiveSubInfoCount] records null");
            return 0;
        }
    }

    /* JADX INFO: finally extract failed */
    public int getAllSubInfoCount(String callingPackage) {
        logd("[getAllSubInfoCount]+");
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, -1, callingPackage, "getAllSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                try {
                    int count = cursor.getCount();
                    logd("[getAllSubInfoCount]- " + count + " SUB(s) in DB");
                    cursor.close();
                    return count;
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                logd("[getAllSubInfoCount]- no SUB in DB");
                Binder.restoreCallingIdentity(identity);
                return 0;
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getActiveSubInfoCountMax() {
        return this.mTelephonyManager.getSimCount();
    }

    public List<SubscriptionInfo> getAvailableSubscriptionInfoList(String callingPackage) {
        if (TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, -1, callingPackage, "getAvailableSubscriptionInfoList")) {
            long identity = Binder.clearCallingIdentity();
            String selection = "sim_id>=0 OR subscription_type=1";
            try {
                if (((EuiccManager) this.mContext.getSystemService("euicc")).isEnabled()) {
                    selection = selection + " OR is_embedded=1";
                }
                List<SubscriptionInfo> subList = getSubInfo(selection, null);
                if (subList != null) {
                    subList.sort(SUBSCRIPTION_INFO_COMPARATOR);
                    if (VDBG) {
                        logdl("[getAvailableSubInfoList]- " + subList.size() + " infos return");
                    }
                } else {
                    logdl("[getAvailableSubInfoList]- no info return");
                }
                return subList;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new SecurityException("Need READ_PHONE_STATE to call  getAvailableSubscriptionInfoList");
        }
    }

    public List<SubscriptionInfo> getAccessibleSubscriptionInfoList(String callingPackage) {
        if (!((EuiccManager) this.mContext.getSystemService("euicc")).isEnabled()) {
            logdl("[getAccessibleSubInfoList] Embedded subscriptions are disabled");
            return null;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getSubInfo("is_embedded=1", null);
            if (subList == null) {
                logdl("[getAccessibleSubInfoList] No info returned");
                return null;
            }
            List<SubscriptionInfo> filteredList = (List) subList.stream().filter(new Predicate(callingPackage) {
                /* class com.android.internal.telephony.$$Lambda$SubscriptionController$z1ZWZtk5wqutKrKUs4Unkis2MRg */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return SubscriptionController.this.lambda$getAccessibleSubscriptionInfoList$1$SubscriptionController(this.f$1, (SubscriptionInfo) obj);
                }
            }).sorted(SUBSCRIPTION_INFO_COMPARATOR).collect(Collectors.toList());
            if (VDBG) {
                logdl("[getAccessibleSubInfoList] " + filteredList.size() + " infos returned");
            }
            return filteredList;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public /* synthetic */ boolean lambda$getAccessibleSubscriptionInfoList$1$SubscriptionController(String callingPackage, SubscriptionInfo subscriptionInfo) {
        return subscriptionInfo.canManageSubscription(this.mContext, callingPackage);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public List<SubscriptionInfo> getSubscriptionInfoListForEmbeddedSubscriptionUpdate(String[] embeddedIccids, boolean isEuiccRemovable) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        whereClause.append("is_embedded");
        whereClause.append("=1");
        if (isEuiccRemovable) {
            whereClause.append(" AND ");
            whereClause.append("is_removable");
            whereClause.append("=1");
        }
        whereClause.append(") OR ");
        whereClause.append("icc_id");
        whereClause.append(" IN (");
        for (int i = 0; i < embeddedIccids.length; i++) {
            if (i > 0) {
                whereClause.append(",");
            }
            whereClause.append("\"");
            whereClause.append(embeddedIccids[i]);
            whereClause.append("\"");
        }
        whereClause.append(")");
        List<SubscriptionInfo> list = getSubInfo(whereClause.toString(), null);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void requestEmbeddedSubscriptionInfoListRefresh(int cardId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS", "requestEmbeddedSubscriptionInfoListRefresh");
        long token = Binder.clearCallingIdentity();
        try {
            PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(cardId, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void requestEmbeddedSubscriptionInfoListRefresh(int cardId, Runnable callback) {
        PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(cardId, callback);
    }

    public void requestEmbeddedSubscriptionInfoListRefresh(Runnable callback) {
        PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(this.mTelephonyManager.getCardIdForDefaultEuicc(), callback);
    }

    public int addSubInfoRecord(String iccId, int slotIndex) {
        return addSubInfo(iccId, null, slotIndex, 0);
    }

    /* JADX INFO: Multiple debug info for r14v18 'value'  android.content.ContentValues: [D('recordsDoNotExist' boolean), D('value' android.content.ContentValues)] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x02dc  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x02e8  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x02ed  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x039a  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00f1  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x011e A[Catch:{ all -> 0x013c }] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01c7 A[SYNTHETIC, Splitter:B:79:0x01c7] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x01dd A[Catch:{ all -> 0x039f }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01f0 A[Catch:{ all -> 0x039f }] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0205 A[SYNTHETIC, Splitter:B:88:0x0205] */
    public int addSubInfo(String uniqueId, String displayName, int slotIndex, int subscriptionType) {
        long identity;
        String[] args;
        String selection;
        Cursor cursor;
        boolean recordsDoNotExist;
        boolean setDisplayName;
        String[] args2;
        String selection2;
        Cursor cursor2;
        String nameToSet;
        boolean setDisplayName2;
        int subId;
        int oldSimInfoId;
        String oldIccId;
        ContentValues value;
        String cardId;
        int slotIndex2 = slotIndex;
        String iccIdStr = uniqueId;
        if (!isSubscriptionForRemoteSim(subscriptionType)) {
            iccIdStr = SubscriptionInfo.givePrintableIccid(uniqueId);
        }
        logdl("[addSubInfoRecord]+ iccid: " + iccIdStr + ", slotIndex: " + slotIndex2 + ", subscriptionType: " + subscriptionType);
        enforceModifyPhoneState("addSubInfo");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        long identity2 = Binder.clearCallingIdentity();
        if (uniqueId == null) {
            try {
                logdl("[addSubInfo]- null iccId");
                Binder.restoreCallingIdentity(identity2);
                return -1;
            } catch (Throwable th) {
                th = th;
                identity = identity2;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            try {
                ContentResolver resolver = this.mContext.getContentResolver();
                if (isSubscriptionForRemoteSim(subscriptionType)) {
                    selection = "icc_id=?" + " AND subscription_type=?";
                    args = new String[]{uniqueId, Integer.toString(subscriptionType)};
                } else {
                    selection = "icc_id=?" + " OR icc_id=?";
                    args = new String[]{uniqueId, IccUtils.getDecimalSubstring(uniqueId)};
                }
                Cursor cursor3 = resolver.query(SubscriptionManager.CONTENT_URI, new String[]{HbpcdLookup.ID, "sim_id", "name_source", "icc_id", "card_id"}, selection, args, null);
                if (cursor3 != null) {
                    try {
                        if (cursor3.moveToFirst()) {
                            recordsDoNotExist = false;
                            identity = identity2;
                            if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                if (recordsDoNotExist) {
                                    slotIndex2 = -1;
                                    try {
                                        logd("[addSubInfoRecord] New record created: " + insertEmptySubInfoRecord(uniqueId, displayName, -1, subscriptionType));
                                        setDisplayName = false;
                                        if (cursor3 != null) {
                                            try {
                                                cursor3.close();
                                            } catch (Throwable th2) {
                                                th = th2;
                                                Binder.restoreCallingIdentity(identity);
                                                throw th;
                                            }
                                        }
                                        String[] args3 = {String.valueOf(slotIndex2)};
                                        if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                            args2 = new String[]{uniqueId, Integer.toString(subscriptionType)};
                                            selection2 = "icc_id=? AND subscription_type=?";
                                        } else {
                                            args2 = args3;
                                            selection2 = "sim_id=?";
                                        }
                                        cursor2 = resolver.query(SubscriptionManager.CONTENT_URI, null, selection2, args2, null);
                                        if (cursor2 != null) {
                                            try {
                                                if (cursor2.moveToFirst()) {
                                                    do {
                                                        int subId2 = cursor2.getInt(cursor2.getColumnIndexOrThrow(HbpcdLookup.ID));
                                                        if (addToSubIdList(slotIndex2, subId2, subscriptionType)) {
                                                            int subIdCountMax = getActiveSubInfoCountMax();
                                                            int defaultSubId = getDefaultSubId();
                                                            logdl("[addSubInfoRecord] sSlotIndexToSubIds.size=" + sSlotIndexToSubIds.size() + " slotIndex=" + slotIndex2 + " subId=" + subId2 + " defaultSubId=" + defaultSubId + " simCount=" + subIdCountMax);
                                                            if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                                                if (!SubscriptionManager.isValidSubscriptionId(defaultSubId) || subIdCountMax == 1) {
                                                                    logdl("setting default fallback subid to " + subId2);
                                                                    setDefaultFallbackSubId(subId2, subscriptionType);
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
                                                    } while (cursor2.moveToNext());
                                                }
                                            } catch (Throwable th3) {
                                                cursor2.close();
                                                throw th3;
                                            }
                                        }
                                        if (cursor2 != null) {
                                            cursor2.close();
                                        }
                                        refreshCachedActiveSubscriptionInfoList();
                                        if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                            notifySubscriptionInfoChanged();
                                        } else {
                                            int subId3 = getSubIdUsingPhoneId(slotIndex2);
                                            if (!SubscriptionManager.isValidSubscriptionId(subId3)) {
                                                logdl("[addSubInfoRecord]- getSubId failed invalid subId = " + subId3);
                                                Binder.restoreCallingIdentity(identity);
                                                return -1;
                                            }
                                            if (setDisplayName) {
                                                String simCarrierName = this.mTelephonyManager.getSimOperatorName(subId3);
                                                if (!TextUtils.isEmpty(simCarrierName)) {
                                                    nameToSet = simCarrierName;
                                                } else {
                                                    nameToSet = "CARD " + Integer.toString(slotIndex2 + 1);
                                                }
                                                ContentValues value2 = new ContentValues();
                                                value2.put("display_name", nameToSet);
                                                resolver.update(SubscriptionManager.getUriForSubscriptionId(subId3), value2, null, null);
                                                refreshCachedActiveSubscriptionInfoList();
                                                logdl("[addSubInfoRecord] sim name = " + nameToSet);
                                            }
                                            sPhones[slotIndex2].updateDataConnectionTracker();
                                            logdl("[addSubInfoRecord]- info size=" + sSlotIndexToSubIds.size());
                                        }
                                        Binder.restoreCallingIdentity(identity);
                                        return 0;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        cursor = cursor3;
                                        if (cursor != null) {
                                        }
                                        throw th;
                                    }
                                } else {
                                    try {
                                        logdl("[addSubInfoRecord] Record already exists");
                                        setDisplayName2 = false;
                                    } catch (Throwable th5) {
                                        th = th5;
                                        cursor = cursor3;
                                        if (cursor != null) {
                                        }
                                        throw th;
                                    }
                                }
                            } else if (recordsDoNotExist) {
                                logdl("[addSubInfoRecord] New record created: " + insertEmptySubInfoRecord(uniqueId, slotIndex2));
                                setDisplayName = true;
                                if (cursor3 != null) {
                                }
                                String[] args32 = {String.valueOf(slotIndex2)};
                                if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                }
                                cursor2 = resolver.query(SubscriptionManager.CONTENT_URI, null, selection2, args2, null);
                                if (cursor2 != null) {
                                }
                                if (cursor2 != null) {
                                }
                                refreshCachedActiveSubscriptionInfoList();
                                if (!isSubscriptionForRemoteSim(subscriptionType)) {
                                }
                                Binder.restoreCallingIdentity(identity);
                                return 0;
                            } else {
                                try {
                                    subId = cursor3.getInt(0);
                                    oldSimInfoId = cursor3.getInt(1);
                                    cursor3.getInt(2);
                                    oldIccId = cursor3.getString(3);
                                    setDisplayName2 = false;
                                } catch (Throwable th6) {
                                    th = th6;
                                    cursor = cursor3;
                                    if (cursor != null) {
                                    }
                                    throw th;
                                }
                                try {
                                    String oldCardId = cursor3.getString(4);
                                    ContentValues value3 = new ContentValues();
                                    if (slotIndex2 != oldSimInfoId) {
                                        try {
                                            value = value3;
                                            value.put("sim_id", Integer.valueOf(slotIndex));
                                        } catch (Throwable th7) {
                                            th = th7;
                                            cursor = cursor3;
                                            if (cursor != null) {
                                            }
                                            throw th;
                                        }
                                    } else {
                                        value = value3;
                                    }
                                    if (oldIccId != null && oldIccId.length() < uniqueId.length() && oldIccId.equals(IccUtils.getDecimalSubstring(uniqueId))) {
                                        value.put("icc_id", uniqueId);
                                    }
                                    UiccCard card = this.mUiccController.getUiccCardForPhone(slotIndex2);
                                    if (!(card == null || (cardId = card.getCardId()) == null || cardId == oldCardId)) {
                                        value.put("card_id", cardId);
                                    }
                                    if (value.size() > 0) {
                                        resolver.update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
                                    }
                                    logdl("[addSubInfoRecord] Record already exists");
                                } catch (Throwable th8) {
                                    th = th8;
                                    cursor = cursor3;
                                    if (cursor != null) {
                                    }
                                    throw th;
                                }
                            }
                            setDisplayName = setDisplayName2;
                            if (cursor3 != null) {
                            }
                            String[] args322 = {String.valueOf(slotIndex2)};
                            if (!isSubscriptionForRemoteSim(subscriptionType)) {
                            }
                            cursor2 = resolver.query(SubscriptionManager.CONTENT_URI, null, selection2, args2, null);
                            if (cursor2 != null) {
                            }
                            if (cursor2 != null) {
                            }
                            refreshCachedActiveSubscriptionInfoList();
                            if (!isSubscriptionForRemoteSim(subscriptionType)) {
                            }
                            Binder.restoreCallingIdentity(identity);
                            return 0;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        cursor = cursor3;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                }
                recordsDoNotExist = true;
                try {
                    identity = identity2;
                    if (!isSubscriptionForRemoteSim(subscriptionType)) {
                    }
                    setDisplayName = setDisplayName2;
                    if (cursor3 != null) {
                    }
                    String[] args3222 = {String.valueOf(slotIndex2)};
                    if (!isSubscriptionForRemoteSim(subscriptionType)) {
                    }
                    cursor2 = resolver.query(SubscriptionManager.CONTENT_URI, null, selection2, args2, null);
                    if (cursor2 != null) {
                    }
                    if (cursor2 != null) {
                    }
                    refreshCachedActiveSubscriptionInfoList();
                    if (!isSubscriptionForRemoteSim(subscriptionType)) {
                    }
                    Binder.restoreCallingIdentity(identity);
                    return 0;
                } catch (Throwable th10) {
                    th = th10;
                    cursor = cursor3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            } catch (Throwable th11) {
                th = th11;
                identity = identity2;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateDefaultSubIdsIfNeeded(int newDefault, int subscriptionType) {
        logdl("[updateDefaultSubIdsIfNeeded] newDefault=" + newDefault + ", subscriptionType=" + subscriptionType);
        if (!isActiveSubscriptionId(getDefaultSubId())) {
            logdl("[updateDefaultSubIdsIfNeeded] set mDefaultFallbackSubId=" + newDefault);
            setDefaultFallbackSubId(newDefault, subscriptionType);
        }
        if (!isActiveSubscriptionId(getDefaultSmsSubId())) {
            setDefaultSmsSubId(newDefault);
        }
        if (!isActiveSubscriptionId(getDefaultDataSubId())) {
            setDefaultDataSubId(newDefault);
        }
        if (!isActiveSubscriptionId(getDefaultVoiceSubId())) {
            setDefaultVoiceSubId(newDefault);
        }
    }

    private boolean isActiveSubscriptionId(int subId) {
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return false;
        }
        ArrayList<Integer> subIdList = getActiveSubIdArrayList();
        if (subIdList.isEmpty()) {
            return false;
        }
        return subIdList.contains(new Integer(subId));
    }

    public int removeSubInfo(String uniqueId, int subscriptionType) {
        int slotIndex;
        int slotIndex2;
        enforceModifyPhoneState("removeSubInfo");
        logd("[removeSubInfo] uniqueId: " + uniqueId + ", subscriptionType: " + subscriptionType);
        Iterator<SubscriptionInfo> it = this.mCacheActiveSubInfoList.iterator();
        while (true) {
            if (!it.hasNext()) {
                slotIndex = -1;
                slotIndex2 = -1;
                break;
            }
            SubscriptionInfo info = it.next();
            if (info.getSubscriptionType() == subscriptionType && info.getIccId().equalsIgnoreCase(uniqueId)) {
                int subId = info.getSubscriptionId();
                slotIndex = info.getSimSlotIndex();
                slotIndex2 = subId;
                break;
            }
        }
        if (slotIndex2 == -1) {
            logd("Invalid subscription details: subscriptionType = " + subscriptionType + ", uniqueId = " + uniqueId);
            return -1;
        }
        logd("removing the subid : " + slotIndex2);
        long identity = Binder.clearCallingIdentity();
        try {
            int result = this.mContext.getContentResolver().delete(SubscriptionManager.CONTENT_URI, "_id=? AND subscription_type=?", new String[]{Integer.toString(slotIndex2), Integer.toString(subscriptionType)});
            if (result != 1) {
                logd("found NO subscription to remove with subscriptionType = " + subscriptionType + ", uniqueId = " + uniqueId);
                return -1;
            }
            refreshCachedActiveSubscriptionInfoList();
            ArrayList<Integer> subIdsList = sSlotIndexToSubIds.get(Integer.valueOf(slotIndex));
            if (subIdsList == null) {
                loge("sSlotIndexToSubIds has no entry for slotIndex = " + slotIndex);
            } else if (subIdsList.contains(Integer.valueOf(slotIndex2))) {
                subIdsList.remove(new Integer(slotIndex2));
                if (subIdsList.isEmpty()) {
                    sSlotIndexToSubIds.remove(Integer.valueOf(slotIndex));
                }
            } else {
                loge("sSlotIndexToSubIds has no subid: " + slotIndex2 + ", in index: " + slotIndex);
            }
            SubscriptionInfo info2 = null;
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (!records.isEmpty()) {
                info2 = records.get(0);
            }
            updateDefaultSubIdsIfNeeded(info2.getSubscriptionId(), info2.getSubscriptionType());
            notifySubscriptionInfoChanged();
            Binder.restoreCallingIdentity(identity);
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
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
    public void clearSubInfoRecord(int slotIndex) {
        logdl("[clearSubInfoRecord]+ iccId: slotIndex:" + slotIndex);
        List<SubscriptionInfo> oldSubInfo = getSubInfoUsingSlotIndexPrivileged(slotIndex);
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues(1);
        value.put("sim_id", (Integer) -1);
        if (oldSubInfo != null) {
            for (int i = 0; i < oldSubInfo.size(); i++) {
                resolver.update(SubscriptionManager.getUriForSubscriptionId(oldSubInfo.get(i).getSubscriptionId()), value, null, null);
            }
        }
        refreshCachedActiveSubscriptionInfoList();
        sSlotIndexToSubIds.remove(Integer.valueOf(slotIndex));
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public Uri insertEmptySubInfoRecord(String iccId, int slotIndex) {
        return insertEmptySubInfoRecord(iccId, null, slotIndex, 0);
    }

    /* access modifiers changed from: protected */
    public Uri insertEmptySubInfoRecord(String uniqueId, String displayName, int slotIndex, int subscriptionType) {
        String cardId;
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues();
        value.put("icc_id", uniqueId);
        value.put("color", Integer.valueOf(getUnusedColor(this.mContext.getOpPackageName())));
        value.put("sim_id", Integer.valueOf(slotIndex));
        value.put("carrier_name", PhoneConfigurationManager.SSSS);
        value.put("card_id", uniqueId);
        value.put("subscription_type", Integer.valueOf(subscriptionType));
        if (isSubscriptionForRemoteSim(subscriptionType)) {
            value.put("display_name", displayName);
        } else {
            UiccCard card = this.mUiccController.getUiccCardForPhone(slotIndex);
            if (!(card == null || (cardId = card.getCardId()) == null)) {
                value.put("card_id", cardId);
            }
        }
        Uri uri = resolver.insert(SubscriptionManager.CONTENT_URI, value);
        refreshCachedActiveSubscriptionInfoList();
        return uri;
    }

    @UnsupportedAppUsage
    public boolean setPlmnSpn(int slotIndex, boolean showPlmn, String plmn, boolean showSpn, String spn) {
        synchronized (this.mLock) {
            int subId = getSubIdUsingPhoneId(slotIndex);
            if (this.mContext.getPackageManager().resolveContentProvider(SubscriptionManager.CONTENT_URI.getAuthority(), 0) != null) {
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    String carrierText = PhoneConfigurationManager.SSSS;
                    if (showPlmn) {
                        carrierText = plmn;
                        if (showSpn && !Objects.equals(spn, plmn)) {
                            carrierText = carrierText + this.mContext.getString(17040217).toString() + spn;
                        }
                    } else if (showSpn) {
                        carrierText = spn;
                    }
                    setCarrierText(carrierText, subId);
                    return true;
                }
            }
            logd("[setPlmnSpn] No valid subscription to store info");
            notifySubscriptionInfoChanged();
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0073, code lost:
        if (r1 != null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0075, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0094, code lost:
        if (r1 == null) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0097, code lost:
        r3 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r5 = new android.content.ContentValues(1);
        r5.put("carrier_name", r11);
        r0 = r10.mContext.getContentResolver().update(android.telephony.SubscriptionManager.getUriForSubscriptionId(r12), r5, null, null);
        refreshCachedActiveSubscriptionInfoList();
        notifySubscriptionInfoChanged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00bd, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00be, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00bf, code lost:
        android.os.Binder.restoreCallingIdentity(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00c2, code lost:
        throw r0;
     */
    private int setCarrierText(String text, int subId) {
        logd("[setCarrierText]+ text:" + text + " subId:" + subId);
        enforceModifyPhoneState("setCarrierText");
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, new String[]{"carrier_name"}, "_id=" + Integer.toString(subId), null, null);
            if (cursor == null) {
                logd("leon setCarrierText Query fail");
            } else if (cursor.moveToNext()) {
                String carrierName = cursor.getString(0);
                if (text != null && text.equals(carrierName)) {
                    logd("leon setCarrierText block for the same");
                    cursor.close();
                    return 1;
                }
            }
        } catch (Exception e) {
            logd("leon setCarrierText Query error:" + e.getMessage());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public int setIconTint(int tint, int subId) {
        logd("[setIconTint]+ tint:" + tint + " subId:" + subId);
        enforceModifyPhoneState("setIconTint");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            ContentValues value = new ContentValues(1);
            value.put("color", Integer.valueOf(tint));
            logd("[setIconTint]- tint:" + tint + " set");
            int result = this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public static int getNameSourcePriority(int nameSource) {
        if (nameSource == 1) {
            return 1;
        }
        if (nameSource == 2) {
            return 3;
        }
        if (nameSource != 3) {
            return 0;
        }
        return 2;
    }

    public int setDisplayNameUsingSrc(String displayName, int subId, int nameSource) {
        String nameToSet;
        logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId + " nameSource:" + nameSource);
        enforceModifyPhoneState("setDisplayNameUsingSrc");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            List<SubscriptionInfo> allSubInfo = getSubInfo(null, null);
            if (allSubInfo != null) {
                if (!allSubInfo.isEmpty()) {
                    for (SubscriptionInfo subInfo : allSubInfo) {
                        if (subInfo.getSubscriptionId() == subId && (getNameSourcePriority(subInfo.getNameSource()) > getNameSourcePriority(nameSource) || (displayName != null && displayName.equals(subInfo.getDisplayName())))) {
                            Binder.restoreCallingIdentity(identity);
                            return 0;
                        }
                    }
                    if (displayName == null) {
                        nameToSet = this.mContext.getString(17039374);
                    } else {
                        nameToSet = displayName;
                    }
                    ContentValues value = new ContentValues(1);
                    value.put("display_name", nameToSet);
                    if (nameSource >= 0) {
                        logd("Set nameSource=" + nameSource);
                        value.put("name_source", Integer.valueOf(nameSource));
                    }
                    logd("[setDisplayName]- mDisplayName:" + nameToSet + " set");
                    SubscriptionInfo sub = getSubscriptionInfo(subId);
                    if (sub != null && sub.isEmbedded()) {
                        int cardId = sub.getCardId();
                        logd("Updating embedded sub nickname on cardId: " + cardId);
                        ((EuiccManager) this.mContext.getSystemService("euicc")).createForCardId(cardId).updateSubscriptionNickname(subId, displayName, PendingIntent.getService(this.mContext, 0, new Intent(), 0));
                    }
                    int result = this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
                    refreshCachedActiveSubscriptionInfoList();
                    notifySubscriptionInfoChanged();
                    Binder.restoreCallingIdentity(identity);
                    return result;
                }
            }
            return 0;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDisplayNumber(String number, int subId) {
        logd("[setDisplayNumber]+ subId:" + subId);
        enforceModifyPhoneState("setDisplayNumber");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            int phoneId = getPhoneId(subId);
            if (number != null && phoneId >= 0) {
                if (phoneId < this.mTelephonyManager.getPhoneCount()) {
                    ContentValues value = new ContentValues(1);
                    value.put("number", number);
                    int result = this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
                    refreshCachedActiveSubscriptionInfoList();
                    logd("[setDisplayNumber]- update result :" + result);
                    notifySubscriptionInfoChanged();
                    Binder.restoreCallingIdentity(identity);
                    return result;
                }
            }
            logd("[setDispalyNumber]- fail");
            return -1;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void setAssociatedPlmns(String[] ehplmns, String[] hplmns, int subId) {
        logd("[setAssociatedPlmns]+ subId:" + subId);
        validateSubId(subId);
        int phoneId = getPhoneId(subId);
        if (phoneId < 0 || phoneId >= this.mTelephonyManager.getPhoneCount()) {
            logd("[setAssociatedPlmns]- fail");
            return;
        }
        String formattedHplmns = PhoneConfigurationManager.SSSS;
        String formattedEhplmns = ehplmns == null ? formattedHplmns : String.join(",", ehplmns);
        if (hplmns != null) {
            formattedHplmns = String.join(",", hplmns);
        }
        ContentValues value = new ContentValues(2);
        value.put("ehplmns", formattedEhplmns);
        value.put("hplmns", formattedHplmns);
        int count = this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
        refreshCachedActiveSubscriptionInfoList();
        logd("[setAssociatedPlmns]- update result :" + count);
        notifySubscriptionInfoChanged();
    }

    public int setDataRoaming(int roaming, int subId) {
        logd("[setDataRoaming]+ roaming:" + roaming + " subId:" + subId);
        enforceModifyPhoneState("setDataRoaming");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            if (roaming < 0) {
                logd("[setDataRoaming]- fail");
                return -1;
            }
            ContentValues value = new ContentValues(1);
            value.put("data_roaming", Integer.valueOf(roaming));
            logd("[setDataRoaming]- roaming:" + roaming + " set");
            int result = databaseUpdateHelper(value, subId, true);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            Binder.restoreCallingIdentity(identity);
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void syncGroupedSetting(int refSubId) {
        String dataEnabledOverrideRules = getSubscriptionProperty(refSubId, "data_enabled_override_rules");
        ContentValues value = new ContentValues(1);
        value.put("data_enabled_override_rules", dataEnabledOverrideRules);
        databaseUpdateHelper(value, refSubId, true);
    }

    private int databaseUpdateHelper(ContentValues value, int subId, boolean updateEntireGroup) {
        List<SubscriptionInfo> infoList = getSubscriptionsInGroup(getGroupUuid(subId), this.mContext.getOpPackageName());
        if (!updateEntireGroup || infoList == null || infoList.size() == 0) {
            return this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
        }
        int[] subIdList = new int[infoList.size()];
        for (int i = 0; i < infoList.size(); i++) {
            subIdList[i] = infoList.get(i).getSubscriptionId();
        }
        return this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, getSelectionForSubIdList(subIdList), null);
    }

    public int setCarrierId(int carrierId, int subId) {
        logd("[setCarrierId]+ carrierId:" + carrierId + " subId:" + subId);
        enforceModifyPhoneState("setCarrierId");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            ContentValues value = new ContentValues(1);
            value.put("carrier_id", Integer.valueOf(carrierId));
            int result = this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setMccMnc(String mccMnc, int subId) {
        String mccString = mccMnc.substring(0, 3);
        String mncString = mccMnc.substring(3);
        int mcc = 0;
        int mnc = 0;
        try {
            mcc = Integer.parseInt(mccString);
            mnc = Integer.parseInt(mncString);
        } catch (NumberFormatException e) {
            loge("[setMccMnc] - couldn't parse mcc/mnc: " + mccMnc);
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
        }
        logd("[setMccMnc]+ mcc/mnc:" + mcc + "/" + mnc + " subId:" + subId);
        ContentValues value = new ContentValues(4);
        value.put("mcc", Integer.valueOf(mcc));
        value.put("mnc", Integer.valueOf(mnc));
        value.put("mcc_string", mccString);
        value.put("mnc_string", mncString);
        int result = this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
        refreshCachedActiveSubscriptionInfoList();
        notifySubscriptionInfoChanged();
        return result;
    }

    public int setImsi(String imsi, int subId) {
        logd("[setImsi]+ imsi:" + imsi + " subId:" + subId);
        ContentValues value = new ContentValues(1);
        value.put("imsi", imsi);
        int result = this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
        refreshCachedActiveSubscriptionInfoList();
        notifySubscriptionInfoChanged();
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
        if (r0 != null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003d, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0040, code lost:
        throw r2;
     */
    public String getImsiPrivileged(int subId) {
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, InboundSmsHandler.SELECT_BY_ID, new String[]{String.valueOf(subId)}, null);
        String imsi = null;
        if (cursor == null) {
            logd("getImsiPrivileged: failed to retrieve imsi.");
        } else if (cursor.moveToNext()) {
            imsi = getOptionalStringFromCursor(cursor, "imsi", null);
        }
        if (cursor != null) {
            $closeResource(null, cursor);
        }
        return imsi;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public int setCountryIso(String iso, int subId) {
        logd("[setCountryIso]+ iso:" + iso + " subId:" + subId);
        ContentValues value = new ContentValues();
        value.put("iso_country_code", iso);
        int result = this.mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
        refreshCachedActiveSubscriptionInfoList();
        notifySubscriptionInfoChanged();
        return result;
    }

    public int getSlotIndex(int subId) {
        if (VDBG) {
            printStackTrace("[getSlotIndex] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("[getSlotIndex]- subId invalid");
            return -1;
        } else if (sSlotIndexToSubIds.size() == 0) {
            logd("[getSlotIndex]- size == 0, return SIM_NOT_INSERTED instead");
            return -1;
        } else {
            for (Map.Entry<Integer, ArrayList<Integer>> entry : sSlotIndexToSubIds.entrySet()) {
                int sim = entry.getKey().intValue();
                ArrayList<Integer> subs = entry.getValue();
                if (subs != null && subs.contains(Integer.valueOf(subId))) {
                    if (VDBG) {
                        logv("[getSlotIndex]- return = " + sim);
                    }
                    return sim;
                }
            }
            logd("[getSlotIndex]- return fail");
            return -1;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public int[] getSubId(int slotIndex) {
        if (VDBG) {
            printStackTrace("[getSubId]+ slotIndex=" + slotIndex);
        }
        if (slotIndex == Integer.MAX_VALUE) {
            slotIndex = getSlotIndex(getDefaultSubId());
            if (VDBG) {
                logd("[getSubId] map default slotIndex=" + slotIndex);
            }
        }
        if (!SubscriptionManager.isValidSlotIndex(slotIndex) && slotIndex != -1) {
            logd("[getSubId]- invalid slotIndex=" + slotIndex);
            return null;
        } else if (sSlotIndexToSubIds.size() == 0) {
            if (VDBG) {
                logd("[getSubId]- sSlotIndexToSubIds.size == 0, return null slotIndex=" + slotIndex);
            }
            return null;
        } else {
            ArrayList<Integer> subIds = sSlotIndexToSubIds.get(Integer.valueOf(slotIndex));
            if (subIds == null || subIds.size() <= 0) {
                logd("[getSubId]- numSubIds == 0, return null slotIndex=" + slotIndex);
                return null;
            }
            int[] subIdArr = new int[subIds.size()];
            for (int i = 0; i < subIds.size(); i++) {
                subIdArr[i] = subIds.get(i).intValue();
            }
            if (VDBG) {
                logd("[getSubId]- subIdArr=" + subIdArr);
            }
            return subIdArr;
        }
    }

    @UnsupportedAppUsage
    public int getPhoneId(int subId) {
        if (VDBG) {
            printStackTrace("[getPhoneId] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
            logd("[getPhoneId] asked for default subId=" + subId);
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            if (!VDBG) {
                return -1;
            }
            logdl("[getPhoneId]- invalid subId return=-1");
            return -1;
        } else if (sSlotIndexToSubIds.size() == 0) {
            int phoneId = mDefaultPhoneId;
            if (VDBG) {
                logdl("[getPhoneId]- no sims, returning default phoneId=" + phoneId);
            }
            return phoneId;
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
            int phoneId2 = mDefaultPhoneId;
            if (VDBG) {
                logd("[getPhoneId]- subId=" + subId + " not found return default phoneId=" + phoneId2);
            }
            return phoneId2;
        }
    }

    public int clearSubInfo() {
        enforceModifyPhoneState("clearSubInfo");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        long identity = Binder.clearCallingIdentity();
        try {
            int size = sSlotIndexToSubIds.size();
            if (size == 0) {
                logdl("[clearSubInfo]- no simInfo size=" + size);
                return 0;
            }
            sSlotIndexToSubIds.clear();
            logdl("[clearSubInfo]- clear size=" + size);
            Binder.restoreCallingIdentity(identity);
            return size;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void logvl(String msg) {
        logv(msg);
        this.mLocalLog.log(msg);
    }

    private void logv(String msg) {
        Rlog.v(LOG_TAG, msg);
    }

    @UnsupportedAppUsage
    private void logdl(String msg) {
        logd(msg);
        this.mLocalLog.log(msg);
    }

    private static void slogd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    @UnsupportedAppUsage
    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logel(String msg) {
        loge(msg);
        this.mLocalLog.log(msg);
    }

    @UnsupportedAppUsage
    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    @UnsupportedAppUsage
    public int getDefaultSubId() {
        int subId;
        try {
            int subIdRet = getDefaultDataSubId();
            if (isActiveSubId(subIdRet)) {
                return subIdRet;
            }
        } catch (Exception e) {
            logdl("getDefaultSubId--error");
        }
        if (this.mContext.getResources().getBoolean(17891571)) {
            subId = getDefaultVoiceSubId();
            if (VDBG) {
                logdl("[getDefaultSubId] isVoiceCapable subId=" + subId);
            }
        } else {
            subId = getDefaultDataSubId();
            if (VDBG) {
                logdl("[getDefaultSubId] NOT VoiceCapable subId=" + subId);
            }
        }
        if (!isActiveSubId(subId)) {
            subId = mDefaultFallbackSubId;
            if (VDBG) {
                logdl("[getDefaultSubId] NOT active use fall back subId=" + subId);
            }
        }
        if (VDBG) {
            logv("[getDefaultSubId]- value = " + subId);
        }
        return subId;
    }

    @UnsupportedAppUsage
    public void setDefaultSmsSubId(int subId) {
        enforceModifyPhoneState("setDefaultSmsSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSmsSubId called with DEFAULT_SUB_ID");
        } else if (isSoftSimCardSubId(subId)) {
            logdl("[setDefaultSmsSubId]- this subid refrence to the Soft sim, so return ");
        } else {
            logdl("[setDefaultSmsSubId]- SoftSimCard disable!!");
            logdl("[setDefaultSmsSubId] subId=" + subId);
            Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms", subId);
            broadcastDefaultSmsSubIdChanged(subId);
        }
    }

    private void broadcastDefaultSmsSubIdChanged(int subId) {
        logdl("[broadcastDefaultSmsSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.telephony.action.DEFAULT_SMS_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("subscription", subId);
        intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    @UnsupportedAppUsage
    public int getDefaultSmsSubId() {
        int subId = Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms", -1);
        if (VDBG) {
            logd("[getDefaultSmsSubId] subId=" + subId);
        }
        return subId;
    }

    @UnsupportedAppUsage
    public void setDefaultVoiceSubId(int subId) {
        enforceModifyPhoneState("setDefaultVoiceSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultVoiceSubId called with DEFAULT_SUB_ID");
        } else if (isSoftSimCardSubId(subId)) {
            logdl("[setDefaultVoiceSubId]- do not set the voice to virtual sim!!");
        } else {
            logdl("[setDefaultVoiceSubId]- soft sim disable!!");
            logdl("[setDefaultVoiceSubId] subId=" + subId);
            int previousDefaultSub = getDefaultSubId();
            Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_call", subId);
            broadcastDefaultVoiceSubIdChanged(subId);
            PhoneAccountHandle newHandle = subId == -1 ? null : this.mTelephonyManager.getPhoneAccountHandleForSubscriptionId(subId);
            TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService(TelecomManager.class);
            if (!Objects.equals(telecomManager.getUserSelectedOutgoingPhoneAccount(), newHandle)) {
                telecomManager.setUserSelectedOutgoingPhoneAccount(newHandle);
                logd("[setDefaultVoiceSubId] change to phoneAccountHandle=" + newHandle);
            } else {
                logd("[setDefaultVoiceSubId] default phone account not changed");
            }
            if (previousDefaultSub != getDefaultSubId()) {
                sendDefaultChangedBroadcast(getDefaultSubId());
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void broadcastDefaultVoiceSubIdChanged(int subId) {
        logdl("[broadcastDefaultVoiceSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("subscription", subId);
        intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    @UnsupportedAppUsage
    public int getDefaultVoiceSubId() {
        int subId = Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_voice_call", -1);
        if (VDBG) {
            slogd("[getDefaultVoiceSubId] subId=" + subId);
        }
        return subId;
    }

    public int getActiveDataSubscriptionId() {
        long token = Binder.clearCallingIdentity();
        try {
            PhoneSwitcher phoneSwitcher = PhoneSwitcher.getInstance();
            if (phoneSwitcher != null) {
                int activeDataSubId = phoneSwitcher.getActiveDataSubId();
                if (SubscriptionManager.isUsableSubscriptionId(activeDataSubId)) {
                    return activeDataSubId;
                }
            }
            int activeDataSubId2 = getDefaultDataSubId();
            Binder.restoreCallingIdentity(token);
            return activeDataSubId2;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @UnsupportedAppUsage
    public int getDefaultDataSubId() {
        int subId = Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", -1);
        if (VDBG) {
            logd("[getDefaultDataSubId] subId= " + subId);
        }
        return subId;
    }

    @UnsupportedAppUsage
    public void setDefaultDataSubId(int subId) {
        int raf;
        enforceModifyPhoneState("setDefaultDataSubId");
        long identity = Binder.clearCallingIdentity();
        if (subId != Integer.MAX_VALUE) {
            try {
                ProxyController proxyController = ProxyController.getInstance();
                int len = sPhones.length;
                logdl("[setDefaultDataSubId] num phones=" + len + ", subId=" + subId);
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
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
                        logdl("[setDefaultDataSubId] phoneId=" + phoneId + " subId=" + id + " RAF=" + raf);
                        rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
                    }
                    if (atLeastOneMatch) {
                        proxyController.setRadioCapability(rafs);
                    } else {
                        logdl("[setDefaultDataSubId] no valid subId's found - not updating.");
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
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new RuntimeException("setDefaultDataSubId called with DEFAULT_SUB_ID");
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void updateAllDataConnectionTrackers() {
        int len = sPhones.length;
        logd("[updateAllDataConnectionTrackers] sPhones.length=" + len);
        for (int phoneId = 0; phoneId < len; phoneId++) {
            logd("[updateAllDataConnectionTrackers] phoneId=" + phoneId);
            sPhones[phoneId].updateDataConnectionTracker();
        }
    }

    @UnsupportedAppUsage
    public void broadcastDefaultDataSubIdChanged(int subId) {
        logdl("[broadcastDefaultDataSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("subscription", subId);
        intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    @UnsupportedAppUsage
    public void setDefaultFallbackSubId(int subId, int subscriptionType) {
        if (subId != Integer.MAX_VALUE) {
            logdl("[setDefaultFallbackSubId] subId=" + subId + ", subscriptionType=" + subscriptionType);
            int previousDefaultSub = getDefaultSubId();
            if (isSubscriptionForRemoteSim(subscriptionType)) {
                mDefaultFallbackSubId = subId;
                return;
            }
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                int phoneId = getPhoneId(subId);
                if (phoneId < 0 || (phoneId >= this.mTelephonyManager.getPhoneCount() && this.mTelephonyManager.getSimCount() != 1)) {
                    logdl("[setDefaultFallbackSubId] not set invalid phoneId=" + phoneId + " subId=" + subId);
                } else {
                    logdl("[setDefaultFallbackSubId] set mDefaultFallbackSubId=" + subId);
                    mDefaultFallbackSubId = subId;
                    MccTable.updateMccMncConfiguration(this.mContext, this.mTelephonyManager.getSimOperatorNumericForPhone(phoneId));
                }
            }
            if (previousDefaultSub != getDefaultSubId()) {
                sendDefaultChangedBroadcast(getDefaultSubId());
                return;
            }
            return;
        }
        throw new RuntimeException("setDefaultSubId called with DEFAULT_SUB_ID");
    }

    public void sendDefaultChangedBroadcast(int subId) {
        int phoneId = SubscriptionManager.getPhoneId(subId);
        Intent intent = new Intent("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, subId);
        logdl("[sendDefaultChangedBroadcast] broadcast default subId changed phoneId=" + phoneId + " subId=" + subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public boolean isOpportunistic(int subId) {
        SubscriptionInfo info = getActiveSubscriptionInfo(subId, this.mContext.getOpPackageName());
        return info != null && info.isOpportunistic();
    }

    @UnsupportedAppUsage
    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return -1;
        }
        return subIds[0];
    }

    @VisibleForTesting
    public List<SubscriptionInfo> getSubInfoUsingSlotIndexPrivileged(int slotIndex) {
        logd("[getSubInfoUsingSlotIndexPrivileged]+ slotIndex:" + slotIndex);
        if (slotIndex == Integer.MAX_VALUE) {
            slotIndex = getSlotIndex(getDefaultSubId());
        }
        if (!SubscriptionManager.isValidSlotIndex(slotIndex)) {
            logd("[getSubInfoUsingSlotIndexPrivileged]- invalid slotIndex");
            return null;
        }
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotIndex)}, null);
        ArrayList<SubscriptionInfo> subList = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                    if (subInfo != null) {
                        if (subList == null) {
                            subList = new ArrayList<>();
                        }
                        subList.add(subInfo);
                    }
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        logd("[getSubInfoUsingSlotIndex]- null info return");
        return subList;
    }

    @UnsupportedAppUsage
    public void validateSubId(int subId) {
        logd("validateSubId subId: " + subId);
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            throw new RuntimeException("Invalid sub id passed as parameter");
        } else if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("Default sub id passed as parameter");
        }
    }

    public void updatePhonesAvailability(Phone[] phones) {
        sPhones = phones;
    }

    private synchronized ArrayList<Integer> getActiveSubIdArrayList() {
        ArrayList<Integer> allSubs;
        List<Map.Entry<Integer, ArrayList<Integer>>> simInfoList = new ArrayList<>(sSlotIndexToSubIds.entrySet());
        Collections.sort(simInfoList, $$Lambda$SubscriptionController$u5xEurXR6ElZ50305_6guo20Fc.INSTANCE);
        allSubs = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<Integer>> slot : simInfoList) {
            allSubs.addAll(slot.getValue());
        }
        return allSubs;
    }

    private boolean isSubscriptionVisible(int subId) {
        for (SubscriptionInfo info : this.mCacheOpportunisticSubInfoList) {
            if (info.getSubscriptionId() == subId) {
                if (info.getGroupUuid() == null) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    public int[] getActiveSubIdList(boolean visibleOnly) {
        List<Integer> allSubs = getActiveSubIdArrayList();
        if (visibleOnly) {
            allSubs = (List) allSubs.stream().filter(new Predicate() {
                /* class com.android.internal.telephony.$$Lambda$SubscriptionController$VCQsMNqRHpN3RyoXYzh2YUwA2yc */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return SubscriptionController.this.lambda$getActiveSubIdList$3$SubscriptionController((Integer) obj);
                }
            }).collect(Collectors.toList());
        }
        int[] subIdArr = new int[allSubs.size()];
        int i = 0;
        try {
            for (Integer num : allSubs) {
                subIdArr[i] = num.intValue();
                i++;
            }
        } catch (Exception e) {
        }
        if (VDBG) {
            logdl("[getActiveSubIdList] allSubs=" + allSubs + " subIdArr.length=" + subIdArr.length);
        }
        return subIdArr;
    }

    public /* synthetic */ boolean lambda$getActiveSubIdList$3$SubscriptionController(Integer subId) {
        return isSubscriptionVisible(subId.intValue());
    }

    public boolean isActiveSubId(int subId, String callingPackage) {
        if (TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, "isActiveSubId")) {
            long identity = Binder.clearCallingIdentity();
            try {
                return isActiveSubId(subId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new SecurityException("Requires READ_PHONE_STATE permission.");
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public boolean isActiveSubId(int subId) {
        boolean retVal = SubscriptionManager.isValidSubscriptionId(subId) && getActiveSubIdArrayList().contains(Integer.valueOf(subId));
        if (VDBG) {
            logdl("[isActiveSubId]- " + retVal);
        }
        return retVal;
    }

    public int getSimStateForSlotIndex(int slotIndex) {
        IccCardConstants.State simState;
        IccCardConstants.State simState2;
        if (slotIndex < 0) {
            simState2 = "invalid slotIndex";
            simState = IccCardConstants.State.UNKNOWN;
        } else {
            Phone phone = null;
            try {
                phone = PhoneFactory.getPhone(slotIndex);
            } catch (IllegalStateException e) {
            }
            if (phone == null) {
                simState = IccCardConstants.State.UNKNOWN;
                simState2 = "phone == null";
            } else {
                IccCard icc = phone.getIccCard();
                if (icc == null) {
                    simState = IccCardConstants.State.UNKNOWN;
                    simState2 = "icc == null";
                } else {
                    simState = icc.getState();
                    simState2 = PhoneConfigurationManager.SSSS;
                }
            }
        }
        if (VDBG) {
            logd("getSimStateForSlotIndex: " + ((String) simState2) + " simState=" + simState + " ordinal=" + simState.ordinal() + " slotIndex=" + slotIndex);
        }
        return simState.ordinal();
    }

    public int setSubscriptionProperty(int subId, String propKey, String propValue) {
        enforceModifyPhoneState("setSubscriptionProperty");
        long token = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            int result = setSubscriptionPropertyIntoContentResolver(subId, propKey, propValue, this.mContext.getContentResolver());
            refreshCachedActiveSubscriptionInfoList();
            return result;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int setSubscriptionPropertyIntoContentResolver(int subId, String propKey, String propValue, ContentResolver resolver) {
        ContentValues value = new ContentValues();
        char c = 65535;
        try {
            switch (propKey.hashCode()) {
                case -2000412720:
                    if (propKey.equals("enable_alert_vibrate")) {
                        c = 6;
                        break;
                    }
                    break;
                case -1950380197:
                    if (propKey.equals("volte_vt_enabled")) {
                        c = 12;
                        break;
                    }
                    break;
                case -1819373132:
                    if (propKey.equals("is_opportunistic")) {
                        c = 13;
                        break;
                    }
                    break;
                case -1555340190:
                    if (propKey.equals("enable_cmas_extreme_threat_alerts")) {
                        c = 0;
                        break;
                    }
                    break;
                case -1433878403:
                    if (propKey.equals("enable_cmas_test_alerts")) {
                        c = 10;
                        break;
                    }
                    break;
                case -1390801311:
                    if (propKey.equals("enable_alert_speech")) {
                        c = 7;
                        break;
                    }
                    break;
                case -1218173306:
                    if (propKey.equals("wfc_ims_enabled")) {
                        c = 15;
                        break;
                    }
                    break;
                case -461686719:
                    if (propKey.equals("enable_emergency_alerts")) {
                        c = 3;
                        break;
                    }
                    break;
                case -420099376:
                    if (propKey.equals("vt_ims_enabled")) {
                        c = 14;
                        break;
                    }
                    break;
                case -349439993:
                    if (propKey.equals("alert_sound_duration")) {
                        c = 4;
                        break;
                    }
                    break;
                case 180938212:
                    if (propKey.equals("wfc_ims_roaming_mode")) {
                        c = 17;
                        break;
                    }
                    break;
                case 203677434:
                    if (propKey.equals("enable_cmas_amber_alerts")) {
                        c = 2;
                        break;
                    }
                    break;
                case 240841894:
                    if (propKey.equals("show_cmas_opt_out_dialog")) {
                        c = 11;
                        break;
                    }
                    break;
                case 407275608:
                    if (propKey.equals("enable_cmas_severe_threat_alerts")) {
                        c = 1;
                        break;
                    }
                    break;
                case 462555599:
                    if (propKey.equals("alert_reminder_interval")) {
                        c = 5;
                        break;
                    }
                    break;
                case 1270593452:
                    if (propKey.equals("enable_etws_test_alerts")) {
                        c = 8;
                        break;
                    }
                    break;
                case 1288054979:
                    if (propKey.equals("enable_channel_50_alerts")) {
                        c = 9;
                        break;
                    }
                    break;
                case 1334635646:
                    if (propKey.equals("wfc_ims_mode")) {
                        c = 16;
                        break;
                    }
                    break;
                case 1604840288:
                    if (propKey.equals("wfc_ims_roaming_enabled")) {
                        c = 18;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                    value.put(propKey, Integer.valueOf(Integer.parseInt(propValue)));
                    break;
                default:
                    slogd("Invalid column name");
                    break;
            }
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
        }
        return resolver.update(SubscriptionManager.getUriForSubscriptionId(subId), value, null, null);
    }

    public String getSubscriptionProperty(int subId, String propKey, String callingPackage) {
        if (!TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subId, callingPackage, "getSubscriptionProperty")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return getSubscriptionProperty(subId, propKey);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0181, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0182, code lost:
        if (r2 != null) goto L_0x0184;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0184, code lost:
        $closeResource(r1, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0187, code lost:
        throw r3;
     */
    public String getSubscriptionProperty(int subId, String propKey) {
        String resultValue = null;
        char c = 1;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, new String[]{propKey}, InboundSmsHandler.SELECT_BY_ID, new String[]{subId + PhoneConfigurationManager.SSSS}, null);
        if (cursor == null) {
            logd("Query failed");
        } else if (cursor.moveToFirst()) {
            switch (propKey.hashCode()) {
                case -2000412720:
                    if (propKey.equals("enable_alert_vibrate")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -1973837322:
                    if (propKey.equals("white_listed_apn_data")) {
                        c = 20;
                        break;
                    }
                    c = 65535;
                    break;
                case -1950380197:
                    if (propKey.equals("volte_vt_enabled")) {
                        c = 12;
                        break;
                    }
                    c = 65535;
                    break;
                case -1819373132:
                    if (propKey.equals("is_opportunistic")) {
                        c = 18;
                        break;
                    }
                    c = 65535;
                    break;
                case -1555340190:
                    if (propKey.equals("enable_cmas_extreme_threat_alerts")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1433878403:
                    if (propKey.equals("enable_cmas_test_alerts")) {
                        c = 10;
                        break;
                    }
                    c = 65535;
                    break;
                case -1390801311:
                    if (propKey.equals("enable_alert_speech")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1218173306:
                    if (propKey.equals("wfc_ims_enabled")) {
                        c = 14;
                        break;
                    }
                    c = 65535;
                    break;
                case -461686719:
                    if (propKey.equals("enable_emergency_alerts")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -420099376:
                    if (propKey.equals("vt_ims_enabled")) {
                        c = 13;
                        break;
                    }
                    c = 65535;
                    break;
                case -349439993:
                    if (propKey.equals("alert_sound_duration")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 180938212:
                    if (propKey.equals("wfc_ims_roaming_mode")) {
                        c = 16;
                        break;
                    }
                    c = 65535;
                    break;
                case 203677434:
                    if (propKey.equals("enable_cmas_amber_alerts")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 240841894:
                    if (propKey.equals("show_cmas_opt_out_dialog")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case 407275608:
                    if (propKey.equals("enable_cmas_severe_threat_alerts")) {
                        break;
                    }
                    c = 65535;
                    break;
                case 462555599:
                    if (propKey.equals("alert_reminder_interval")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 512460727:
                    if (propKey.equals("data_enabled_override_rules")) {
                        c = 21;
                        break;
                    }
                    c = 65535;
                    break;
                case 1270593452:
                    if (propKey.equals("enable_etws_test_alerts")) {
                        c = 8;
                        break;
                    }
                    c = 65535;
                    break;
                case 1282534779:
                    if (propKey.equals("group_uuid")) {
                        c = 19;
                        break;
                    }
                    c = 65535;
                    break;
                case 1288054979:
                    if (propKey.equals("enable_channel_50_alerts")) {
                        c = 9;
                        break;
                    }
                    c = 65535;
                    break;
                case 1334635646:
                    if (propKey.equals("wfc_ims_mode")) {
                        c = 15;
                        break;
                    }
                    c = 65535;
                    break;
                case 1604840288:
                    if (propKey.equals("wfc_ims_roaming_enabled")) {
                        c = 17;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                    resultValue = cursor.getInt(0) + PhoneConfigurationManager.SSSS;
                    break;
                case 21:
                    resultValue = cursor.getString(0);
                    break;
                default:
                    logd("Invalid column name");
                    break;
            }
        } else {
            logd("Valid row not present in db");
        }
        if (cursor != null) {
            $closeResource(null, cursor);
        }
        logd("getSubscriptionProperty Query value = " + resultValue);
        return resultValue;
    }

    protected static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        slogd("StackTrace - " + msg);
        StackTraceElement[] st = re.getStackTrace();
        boolean first = true;
        for (StackTraceElement ste : st) {
            if (first) {
                first = false;
            } else {
                slogd(ste.toString());
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", "Requires DUMP");
        long token = Binder.clearCallingIdentity();
        try {
            pw.println("SubscriptionController:");
            pw.println(" mLastISubServiceRegTime=" + this.mLastISubServiceRegTime);
            pw.println(" defaultSubId=" + getDefaultSubId());
            pw.println(" defaultDataSubId=" + getDefaultDataSubId());
            pw.println(" defaultVoiceSubId=" + getDefaultVoiceSubId());
            pw.println(" defaultSmsSubId=" + getDefaultSmsSubId());
            pw.println(" defaultDataPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultDataPhoneId());
            pw.println(" defaultVoicePhoneId=" + SubscriptionManager.getDefaultVoicePhoneId());
            pw.println(" defaultSmsPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultSmsPhoneId());
            pw.flush();
            for (Map.Entry<Integer, ArrayList<Integer>> entry : sSlotIndexToSubIds.entrySet()) {
                pw.println(" sSlotIndexToSubId[" + entry.getKey() + "]: subIds=" + entry);
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            List<SubscriptionInfo> sirl = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" ActiveSubInfoList:");
                Iterator<SubscriptionInfo> it = sirl.iterator();
                while (it.hasNext()) {
                    pw.println("  " + it.next().toString());
                }
            } else {
                pw.println(" ActiveSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            List<SubscriptionInfo> sirl2 = getAllSubInfoList(this.mContext.getOpPackageName());
            if (sirl2 != null) {
                pw.println(" AllSubInfoList:");
                Iterator<SubscriptionInfo> it2 = sirl2.iterator();
                while (it2.hasNext()) {
                    pw.println("  " + it2.next().toString());
                }
            } else {
                pw.println(" AllSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            this.mLocalLog.dump(fd, pw, args);
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            pw.flush();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void migrateImsSettings() {
        migrateImsSettingHelper("volte_vt_enabled", "volte_vt_enabled");
        migrateImsSettingHelper("vt_ims_enabled", "vt_ims_enabled");
        migrateImsSettingHelper("wfc_ims_enabled", "wfc_ims_enabled");
        migrateImsSettingHelper("wfc_ims_mode", "wfc_ims_mode");
        migrateImsSettingHelper("wfc_ims_roaming_mode", "wfc_ims_roaming_mode");
        migrateImsSettingHelper("wfc_ims_roaming_enabled", "wfc_ims_roaming_enabled");
    }

    private void migrateImsSettingHelper(String settingGlobal, String subscriptionProperty) {
        ContentResolver resolver = this.mContext.getContentResolver();
        int defaultSubId = getDefaultVoiceSubId();
        if (defaultSubId != -1) {
            try {
                int prevSetting = Settings.Global.getInt(resolver, settingGlobal);
                if (prevSetting != -1) {
                    setSubscriptionPropertyIntoContentResolver(defaultSubId, subscriptionProperty, Integer.toString(prevSetting), resolver);
                    Settings.Global.putInt(resolver, settingGlobal, -1);
                }
            } catch (Settings.SettingNotFoundException e) {
            }
        }
    }

    public int setOpportunistic(boolean opportunistic, int subId, String callingPackage) {
        try {
            TelephonyPermissions.enforceCallingOrSelfModifyPermissionOrCarrierPrivilege(this.mContext, subId, callingPackage);
        } catch (SecurityException e) {
            enforceCarrierPrivilegeOnInactiveSub(subId, callingPackage, "Caller requires permission on sub " + subId);
        }
        long token = Binder.clearCallingIdentity();
        try {
            int ret = setSubscriptionProperty(subId, "is_opportunistic", String.valueOf(opportunistic ? 1 : 0));
            if (ret != 0) {
                notifySubscriptionInfoChanged();
            }
            return ret;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void enforceCarrierPrivilegeOnInactiveSub(int subId, String callingPackage, String message) {
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        SubscriptionManager subManager = (SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service");
        List<SubscriptionInfo> subInfo = getSubInfo("_id=" + subId, null);
        try {
            if (isActiveSubId(subId) || subInfo == null || subInfo.size() != 1 || !subManager.canManageSubscription(subInfo.get(0), callingPackage)) {
                throw new SecurityException(message);
            }
        } catch (IllegalArgumentException e) {
            throw new SecurityException(message);
        }
    }

    public void setPreferredDataSubscriptionId(int subId, boolean needValidation, ISetOpportunisticDataCallback callback) {
        enforceModifyPhoneState("setPreferredDataSubscriptionId");
        long token = Binder.clearCallingIdentity();
        try {
            PhoneSwitcher.getInstance().trySetOpportunisticDataSubscription(subId, needValidation, callback);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public int getPreferredDataSubscriptionId() {
        enforceReadPrivilegedPhoneState("getPreferredDataSubscriptionId");
        long token = Binder.clearCallingIdentity();
        try {
            return PhoneSwitcher.getInstance().getOpportunisticDataSubscriptionId();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public List<SubscriptionInfo> getOpportunisticSubscriptions(String callingPackage) {
        return getSubscriptionInfoListFromCacheHelper(callingPackage, this.mCacheOpportunisticSubInfoList);
    }

    public ParcelUuid createSubscriptionGroup(int[] subIdList, String callingPackage) {
        if (subIdList == null || subIdList.length == 0) {
            throw new IllegalArgumentException("Invalid subIdList " + subIdList);
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0 || checkCarrierPrivilegeOnSubList(subIdList, callingPackage)) {
            long identity = Binder.clearCallingIdentity();
            try {
                ParcelUuid groupUUID = new ParcelUuid(UUID.randomUUID());
                ContentValues value = new ContentValues();
                value.put("group_uuid", groupUUID.toString());
                value.put("group_owner", callingPackage);
                int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, getSelectionForSubIdList(subIdList), null);
                logdl("createSubscriptionGroup update DB result: " + result);
                refreshCachedActiveSubscriptionInfoList();
                notifySubscriptionInfoChanged();
                MultiSimSettingController.getInstance().notifySubscriptionGroupChanged(groupUUID);
                return groupUUID;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new SecurityException("CreateSubscriptionGroup needs MODIFY_PHONE_STATE or carrier privilege permission on all specified subscriptions");
        }
    }

    private String getOwnerPackageOfSubGroup(ParcelUuid groupUuid) {
        if (groupUuid == null) {
            return null;
        }
        List<SubscriptionInfo> infoList = getSubInfo("group_uuid='" + groupUuid.toString() + "'", null);
        if (ArrayUtils.isEmpty(infoList)) {
            return null;
        }
        return infoList.get(0).getGroupOwner();
    }

    /* JADX INFO: finally extract failed */
    public boolean canPackageManageGroup(ParcelUuid groupUuid, String callingPackage) {
        if (groupUuid == null) {
            throw new IllegalArgumentException("Invalid groupUuid");
        } else if (!TextUtils.isEmpty(callingPackage)) {
            long identity = Binder.clearCallingIdentity();
            try {
                List<SubscriptionInfo> infoList = getSubInfo("group_uuid='" + groupUuid.toString() + "'", null);
                Binder.restoreCallingIdentity(identity);
                if (!ArrayUtils.isEmpty(infoList) && !callingPackage.equals(infoList.get(0).getGroupOwner())) {
                    return checkCarrierPrivilegeOnSubList(infoList.stream().mapToInt($$Lambda$SubscriptionController$veExsDKa8gFN8Rhwod7PQ8HDxP0.INSTANCE).toArray(), callingPackage);
                }
                return true;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Empty callingPackage");
        }
    }

    private int updateGroupOwner(ParcelUuid groupUuid, String groupOwner) {
        ContentValues value = new ContentValues(1);
        value.put("group_owner", groupOwner);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri uri = SubscriptionManager.CONTENT_URI;
        return contentResolver.update(uri, value, "group_uuid=\"" + groupUuid + "\"", null);
    }

    public void addSubscriptionsIntoGroup(int[] subIdList, ParcelUuid groupUuid, String callingPackage) {
        if (subIdList == null || subIdList.length == 0) {
            throw new IllegalArgumentException("Invalid subId list");
        } else if (groupUuid == null || groupUuid.equals(INVALID_GROUP_UUID)) {
            throw new IllegalArgumentException("Invalid groupUuid");
        } else if (!getSubscriptionsInGroup(groupUuid, callingPackage).isEmpty()) {
            this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
            if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0 || (checkCarrierPrivilegeOnSubList(subIdList, callingPackage) && canPackageManageGroup(groupUuid, callingPackage))) {
                long identity = Binder.clearCallingIdentity();
                try {
                    logdl("addSubscriptionsIntoGroup sub list " + Arrays.toString(subIdList) + " into group " + groupUuid);
                    ContentValues value = new ContentValues();
                    value.put("group_uuid", groupUuid.toString());
                    int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, getSelectionForSubIdList(subIdList), null);
                    logdl("addSubscriptionsIntoGroup update DB result: " + result);
                    if (result > 0) {
                        updateGroupOwner(groupUuid, callingPackage);
                        refreshCachedActiveSubscriptionInfoList();
                        notifySubscriptionInfoChanged();
                        MultiSimSettingController.getInstance().notifySubscriptionGroupChanged(groupUuid);
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new SecurityException("Requires MODIFY_PHONE_STATE or carrier privilege permissions on subscriptions and the group.");
            }
        } else {
            throw new IllegalArgumentException("Cannot add subscriptions to a non-existent group!");
        }
    }

    public void removeSubscriptionsFromGroup(int[] subIdList, ParcelUuid groupUuid, String callingPackage) {
        if (subIdList != null && subIdList.length != 0) {
            this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
            if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0 || (checkCarrierPrivilegeOnSubList(subIdList, callingPackage) && canPackageManageGroup(groupUuid, callingPackage))) {
                long identity = Binder.clearCallingIdentity();
                try {
                    for (SubscriptionInfo info : getSubInfo(getSelectionForSubIdList(subIdList), null)) {
                        if (!groupUuid.equals(info.getGroupUuid())) {
                            throw new IllegalArgumentException("Subscription " + info.getSubscriptionId() + " doesn't belong to group " + groupUuid);
                        }
                    }
                    ContentValues value = new ContentValues();
                    value.put("group_uuid", (String) null);
                    value.put("group_owner", (String) null);
                    int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, getSelectionForSubIdList(subIdList), null);
                    logdl("removeSubscriptionsFromGroup update DB result: " + result);
                    if (result > 0) {
                        updateGroupOwner(groupUuid, callingPackage);
                        refreshCachedActiveSubscriptionInfoList();
                        notifySubscriptionInfoChanged();
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new SecurityException("removeSubscriptionsFromGroup needs MODIFY_PHONE_STATE or carrier privilege permission on all specified subscriptions");
            }
        }
    }

    private boolean checkCarrierPrivilegeOnSubList(int[] subIdList, String callingPackage) {
        Set<Integer> checkSubList = new HashSet<>();
        for (int subId : subIdList) {
            if (!isActiveSubId(subId)) {
                checkSubList.add(Integer.valueOf(subId));
            } else if (!this.mTelephonyManager.hasCarrierPrivileges(subId)) {
                return false;
            }
        }
        if (checkSubList.isEmpty()) {
            return true;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            SubscriptionManager subscriptionManager = (SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service");
            List<SubscriptionInfo> subInfoList = getSubInfo(getSelectionForSubIdList(subIdList), null);
            if (subInfoList == null || subInfoList.size() != subIdList.length) {
                throw new IllegalArgumentException("Invalid subInfoList.");
            }
            for (SubscriptionInfo subInfo : subInfoList) {
                if (checkSubList.contains(Integer.valueOf(subInfo.getSubscriptionId()))) {
                    if (!subInfo.isEmbedded() || !subscriptionManager.canManageSubscription(subInfo, callingPackage)) {
                        return false;
                    }
                    checkSubList.remove(Integer.valueOf(subInfo.getSubscriptionId()));
                }
            }
            boolean isEmpty = checkSubList.isEmpty();
            Binder.restoreCallingIdentity(identity);
            return isEmpty;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private String getSelectionForSubIdList(int[] subId) {
        StringBuilder selection = new StringBuilder();
        selection.append(HbpcdLookup.ID);
        selection.append(" IN (");
        for (int i = 0; i < subId.length - 1; i++) {
            selection.append(subId[i] + ", ");
        }
        selection.append(subId[subId.length - 1]);
        selection.append(")");
        return selection.toString();
    }

    /* JADX INFO: finally extract failed */
    public List<SubscriptionInfo> getSubscriptionsInGroup(ParcelUuid groupUuid, String callingPackage) {
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subInfoList = getAllSubInfoList(this.mContext.getOpPackageName());
            if (groupUuid == null || subInfoList == null || subInfoList.isEmpty()) {
                ArrayList arrayList = new ArrayList();
                Binder.restoreCallingIdentity(identity);
                return arrayList;
            }
            Binder.restoreCallingIdentity(identity);
            return (List) subInfoList.stream().filter(new Predicate(groupUuid, callingPackage) {
                /* class com.android.internal.telephony.$$Lambda$SubscriptionController$CaRmFtDrpSD7YdPKEdfMgAOlVZY */
                private final /* synthetic */ ParcelUuid f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return SubscriptionController.this.lambda$getSubscriptionsInGroup$5$SubscriptionController(this.f$1, this.f$2, (SubscriptionInfo) obj);
                }
            }).collect(Collectors.toList());
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public /* synthetic */ boolean lambda$getSubscriptionsInGroup$5$SubscriptionController(ParcelUuid groupUuid, String callingPackage, SubscriptionInfo info) {
        if (!groupUuid.equals(info.getGroupUuid())) {
            return false;
        }
        if (TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, info.getSubscriptionId(), callingPackage, "getSubscriptionsInGroup") || (info.isEmbedded() && info.canManageSubscription(this.mContext, callingPackage))) {
            return true;
        }
        return false;
    }

    public ParcelUuid getGroupUuid(int subId) {
        List<SubscriptionInfo> subInfo = getSubInfo("_id=" + subId, null);
        if (subInfo == null || subInfo.size() == 0) {
            return null;
        }
        return subInfo.get(0).getGroupUuid();
    }

    public boolean setSubscriptionEnabled(boolean enable, int subId) {
        enforceModifyPhoneState("setSubscriptionEnabled");
        long identity = Binder.clearCallingIdentity();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("setSubscriptionEnabled");
            sb.append(enable ? " enable " : " disable ");
            sb.append(" subId ");
            sb.append(subId);
            logd(sb.toString());
            if (SubscriptionManager.isUsableSubscriptionId(subId)) {
                SubscriptionInfo info = getInstance().getAllSubInfoList(this.mContext.getOpPackageName()).stream().filter(new Predicate(subId) {
                    /* class com.android.internal.telephony.$$Lambda$SubscriptionController$KLGYC8GQvJwXrWqyIaejMh0cYio */
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return SubscriptionController.lambda$setSubscriptionEnabled$6(this.f$0, (SubscriptionInfo) obj);
                    }
                }).findFirst().get();
                if (info == null) {
                    logd("setSubscriptionEnabled subId " + subId + " doesn't exist.");
                    return false;
                } else if (info.isEmbedded()) {
                    boolean enableEmbeddedSubscription = enableEmbeddedSubscription(info, enable);
                    Binder.restoreCallingIdentity(identity);
                    return enableEmbeddedSubscription;
                } else {
                    boolean enablePhysicalSubscription = enablePhysicalSubscription(info, enable);
                    Binder.restoreCallingIdentity(identity);
                    return enablePhysicalSubscription;
                }
            } else {
                throw new IllegalArgumentException("setSubscriptionEnabled not usable subId " + subId);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    static /* synthetic */ boolean lambda$setSubscriptionEnabled$6(int subId, SubscriptionInfo subInfo) {
        return subInfo.getSubscriptionId() == subId;
    }

    private boolean enableEmbeddedSubscription(SubscriptionInfo info, boolean enable) {
        enableSubscriptionOverEuiccManager(info.getSubscriptionId(), enable, -1);
        return false;
    }

    private static boolean isInactiveInsertedPSim(UiccSlotInfo slotInfo, String cardId) {
        return !slotInfo.getIsEuicc() && !slotInfo.getIsActive() && slotInfo.getCardStateInfo() == 2 && TextUtils.equals(slotInfo.getCardId(), cardId);
    }

    private boolean enablePhysicalSubscription(SubscriptionInfo info, boolean enable) {
        if (!enable || info.getSimSlotIndex() != -1) {
            return this.mTelephonyManager.enableModemForSlot(info.getSimSlotIndex(), enable);
        }
        UiccSlotInfo[] slotsInfo = this.mTelephonyManager.getUiccSlotsInfo();
        if (slotsInfo == null) {
            return false;
        }
        boolean foundMatch = false;
        int i = 0;
        while (true) {
            if (i >= slotsInfo.length) {
                break;
            } else if (isInactiveInsertedPSim(slotsInfo[i], info.getCardString())) {
                enableSubscriptionOverEuiccManager(info.getSubscriptionId(), enable, i);
                foundMatch = true;
                break;
            } else {
                i++;
            }
        }
        if (!foundMatch) {
            logdl("enablePhysicalSubscription subId " + info.getSubscriptionId() + " is not inserted.");
        }
        return false;
    }

    private void enableSubscriptionOverEuiccManager(int subId, boolean enable, int physicalSlotIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append("enableSubscriptionOverEuiccManager");
        sb.append(enable ? " enable " : " disable ");
        sb.append("subId ");
        sb.append(subId);
        sb.append(" on slotIndex ");
        sb.append(physicalSlotIndex);
        logdl(sb.toString());
        Intent intent = new Intent("android.telephony.euicc.action.TOGGLE_SUBSCRIPTION_PRIVILEGED");
        intent.addFlags(268435456);
        intent.putExtra("android.telephony.euicc.extra.SUBSCRIPTION_ID", subId);
        intent.putExtra("android.telephony.euicc.extra.ENABLE_SUBSCRIPTION", enable);
        if (physicalSlotIndex != -1) {
            intent.putExtra("android.telephony.euicc.extra.PHYSICAL_SLOT_ID", physicalSlotIndex);
        }
        this.mContext.startActivity(intent);
    }

    private void updateEnabledSubscriptionGlobalSetting(int subId, int physicalSlotIndex) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putInt(contentResolver, "enabled_subscription_for_slot" + physicalSlotIndex, subId);
    }

    private void updateModemStackEnabledGlobalSetting(boolean enabled, int physicalSlotIndex) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putInt(contentResolver, "modem_stack_enabled_for_slot" + physicalSlotIndex, enabled ? 1 : 0);
    }

    private int getPhysicalSlotIndex(boolean isEmbedded, int subId) {
        UiccSlotInfo[] slotInfos = this.mTelephonyManager.getUiccSlotsInfo();
        int logicalSlotIndex = getSlotIndex(subId);
        boolean isLogicalSlotIndexValid = SubscriptionManager.isValidSlotIndex(logicalSlotIndex);
        for (int i = 0; i < slotInfos.length; i++) {
            if ((isLogicalSlotIndexValid && slotInfos[i].getLogicalSlotIdx() == logicalSlotIndex) || (!isLogicalSlotIndexValid && slotInfos[i].getIsEuicc() && isEmbedded)) {
                return i;
            }
        }
        return -1;
    }

    private int getPhysicalSlotIndexFromLogicalSlotIndex(int logicalSlotIndex) {
        UiccSlotInfo[] slotInfos = this.mTelephonyManager.getUiccSlotsInfo();
        for (int i = 0; i < slotInfos.length; i++) {
            if (slotInfos[i].getLogicalSlotIdx() == logicalSlotIndex) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSubscriptionEnabled(int subId) {
        enforceReadPrivilegedPhoneState("isSubscriptionEnabled");
        long identity = Binder.clearCallingIdentity();
        try {
            if (SubscriptionManager.isUsableSubscriptionId(subId)) {
                List<SubscriptionInfo> infoList = getSubInfo("_id=" + subId, null);
                boolean z = false;
                if (infoList != null) {
                    if (!infoList.isEmpty()) {
                        if (infoList.get(0).isEmbedded()) {
                            boolean isActiveSubId = isActiveSubId(subId);
                            Binder.restoreCallingIdentity(identity);
                            return isActiveSubId;
                        }
                        if (isActiveSubId(subId) && PhoneConfigurationManager.getInstance().getPhoneStatus(PhoneFactory.getPhone(getPhoneId(subId)))) {
                            z = true;
                        }
                        Binder.restoreCallingIdentity(identity);
                        return z;
                    }
                }
                return false;
            }
            throw new IllegalArgumentException("isSubscriptionEnabled not usable subId " + subId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getEnabledSubscriptionId(int logicalSlotIndex) {
        int subId;
        enforceReadPrivilegedPhoneState("getEnabledSubscriptionId");
        long identity = Binder.clearCallingIdentity();
        try {
            if (SubscriptionManager.isValidPhoneId(logicalSlotIndex)) {
                int physicalSlotIndex = getPhysicalSlotIndexFromLogicalSlotIndex(logicalSlotIndex);
                if (physicalSlotIndex == -1) {
                    return -1;
                }
                ContentResolver contentResolver = this.mContext.getContentResolver();
                if (Settings.Global.getInt(contentResolver, "modem_stack_enabled_for_slot" + physicalSlotIndex, 1) != 1) {
                    Binder.restoreCallingIdentity(identity);
                    return -1;
                }
                try {
                    ContentResolver contentResolver2 = this.mContext.getContentResolver();
                    subId = Settings.Global.getInt(contentResolver2, "enabled_subscription_for_slot" + physicalSlotIndex);
                } catch (Settings.SettingNotFoundException e) {
                    subId = getSubIdUsingPhoneId(logicalSlotIndex);
                }
                Binder.restoreCallingIdentity(identity);
                return subId;
            }
            throw new IllegalArgumentException("getEnabledSubscriptionId with invalid logicalSlotIndex " + logicalSlotIndex);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private List<SubscriptionInfo> getSubscriptionInfoListFromCacheHelper(String callingPackage, List<SubscriptionInfo> cacheSubList) {
        boolean canReadAllPhoneState;
        try {
            canReadAllPhoneState = TelephonyPermissions.checkReadPhoneState(this.mContext, -1, Binder.getCallingPid(), Binder.getCallingUid(), callingPackage, "getSubscriptionInfoList");
        } catch (SecurityException e) {
            canReadAllPhoneState = false;
        }
        synchronized (this.mSubInfoListLock) {
            if (canReadAllPhoneState) {
                ArrayList arrayList = new ArrayList(cacheSubList);
                return arrayList;
            }
            List<SubscriptionInfo> list = (List) cacheSubList.stream().filter(new Predicate(callingPackage) {
                /* class com.android.internal.telephony.$$Lambda$SubscriptionController$0y_j8vef67bMEiPQdeWyjuFpPQ8 */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return SubscriptionController.this.lambda$getSubscriptionInfoListFromCacheHelper$7$SubscriptionController(this.f$1, (SubscriptionInfo) obj);
                }
            }).collect(Collectors.toList());
            return list;
        }
    }

    public /* synthetic */ boolean lambda$getSubscriptionInfoListFromCacheHelper$7$SubscriptionController(String callingPackage, SubscriptionInfo subscriptionInfo) {
        try {
            return TelephonyPermissions.checkCallingOrSelfReadPhoneState(this.mContext, subscriptionInfo.getSubscriptionId(), callingPackage, "getSubscriptionInfoList");
        } catch (SecurityException e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized boolean addToSubIdList(int slotIndex, int subId, int subscriptionType) {
        ArrayList<Integer> subIdsList = sSlotIndexToSubIds.get(Integer.valueOf(slotIndex));
        if (subIdsList == null) {
            subIdsList = new ArrayList<>();
        }
        if (subIdsList.contains(Integer.valueOf(subId))) {
            logdl("slotIndex, subId combo already exists in the map. Not adding it again.");
            return false;
        }
        if (isSubscriptionForRemoteSim(subscriptionType)) {
            subIdsList.add(Integer.valueOf(subId));
        } else {
            subIdsList.clear();
            subIdsList.add(Integer.valueOf(subId));
        }
        sSlotIndexToSubIds.put(Integer.valueOf(slotIndex), subIdsList);
        logd("addToSubIdList,sSlotIndexToSubIds:" + sSlotIndexToSubIds);
        logdl("slotIndex, subId combo is added to the map.");
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isSubscriptionForRemoteSim(int subscriptionType) {
        return subscriptionType == 1;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public Map<Integer, ArrayList<Integer>> getSlotIndexToSubIdsMap() {
        return sSlotIndexToSubIds;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void resetStaticMembers() {
        mDefaultFallbackSubId = -1;
        mDefaultPhoneId = KeepaliveStatus.INVALID_HANDLE;
    }

    private void notifyOpportunisticSubscriptionInfoChanged() {
        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            logd("notifyOpptSubscriptionInfoChanged:");
            tr.notifyOpportunisticSubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
    }

    private boolean refreshCachedOpportunisticSubscriptionInfoList() {
        boolean z;
        synchronized (this.mSubInfoListLock) {
            List<SubscriptionInfo> oldOpptCachedList = this.mCacheOpportunisticSubInfoList;
            List<SubscriptionInfo> subList = getSubInfo("is_opportunistic=1 AND (sim_id>=0 OR is_embedded=1)", null);
            if (subList != null) {
                subList.sort(SUBSCRIPTION_INFO_COMPARATOR);
            } else {
                subList = new ArrayList();
            }
            this.mCacheOpportunisticSubInfoList = subList;
            Iterator<SubscriptionInfo> it = this.mCacheOpportunisticSubInfoList.iterator();
            while (true) {
                z = true;
                if (!it.hasNext()) {
                    break;
                }
                SubscriptionInfo info = it.next();
                if (shouldDisableSubGroup(info.getGroupUuid())) {
                    info.setGroupDisabled(true);
                    if (isActiveSubId(info.getSubscriptionId()) && isSubInfoReady()) {
                        logd("[refreshCachedOpportunisticSubscriptionInfoList] Deactivating grouped opportunistic subscription " + info.getSubscriptionId());
                        deactivateSubscription(info);
                    }
                }
            }
            if (oldOpptCachedList.equals(this.mCacheOpportunisticSubInfoList)) {
                z = false;
            }
        }
        return z;
    }

    private boolean shouldDisableSubGroup(ParcelUuid groupUuid) {
        if (groupUuid == null) {
            return false;
        }
        for (SubscriptionInfo activeInfo : this.mCacheActiveSubInfoList) {
            if (!activeInfo.isOpportunistic() && groupUuid.equals(activeInfo.getGroupUuid())) {
                return false;
            }
        }
        return true;
    }

    private void deactivateSubscription(SubscriptionInfo info) {
        if (info.isEmbedded()) {
            logd("[deactivateSubscription] eSIM profile " + info.getSubscriptionId());
            ((EuiccManager) this.mContext.getSystemService("euicc")).switchToSubscription(-1, PendingIntent.getService(this.mContext, 0, new Intent(), 0));
        }
    }

    public boolean setAlwaysAllowMmsData(int subId, boolean alwaysAllow) {
        logd("[setAlwaysAllowMmsData]+ alwaysAllow:" + alwaysAllow + " subId:" + subId);
        enforceModifyPhoneState("setAlwaysAllowMmsData");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            Phone phone = PhoneFactory.getPhone(getPhoneId(subId));
            if (phone == null) {
                return false;
            }
            boolean alwaysAllowMmsData = phone.getDataEnabledSettings().setAlwaysAllowMmsData(alwaysAllow);
            Binder.restoreCallingIdentity(identity);
            return alwaysAllowMmsData;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean setDataEnabledOverrideRules(int subId, String rules) {
        logd("[setDataEnabledOverrideRules]+ rules:" + rules + " subId:" + subId);
        validateSubId(subId);
        boolean result = true;
        ContentValues value = new ContentValues(1);
        value.put("data_enabled_override_rules", rules);
        if (databaseUpdateHelper(value, subId, true) <= 0) {
            result = false;
        }
        if (result) {
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
        }
        return result;
    }

    public String getDataEnabledOverrideRules(int subId) {
        return TextUtils.emptyIfNull(getSubscriptionProperty(subId, "data_enabled_override_rules"));
    }
}
