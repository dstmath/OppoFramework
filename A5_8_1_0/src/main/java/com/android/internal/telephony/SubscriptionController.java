package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import com.android.ims.ImsManager;
import com.android.internal.telephony.ISub.Stub;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.regionlock.RegionLockPlmnListService.PlmnCodeEntry;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.util.NotificationChannelController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.codeaurora.internal.IExtTelephony;

public class SubscriptionController extends Stub {
    private static final String ACTION_SUBINFO_STATE_CHANGE = "oppo.intent.action.SUBINFO_STATE_CHANGE";
    protected static final boolean DBG = true;
    protected static final boolean DBG_CACHE = false;
    private static final int EVENT_CHECK_VSIM = 2;
    private static final int EVENT_DELAY_SET_WFC_MODE = 10;
    private static final int EVENT_WRITE_MSISDN_DONE = 1;
    private static final String INTENT_KEY_SIM_STATE = "simstate";
    private static final String INTENT_KEY_SLOT_ID = "slotid";
    private static final String INTENT_KEY_SUB_ID = "subid";
    private static final String INTENT_VALUE_SIM_CARD_TYPE = "CARDTYPE";
    static final String LOG_TAG = "SubscriptionController";
    static final int MAX_LOCAL_LOG_LINES = 500;
    public static final String OPPO_MULTI_SIM_NETWORK_PRIMARY_SLOT = "oppo_multi_sim_network_primary_slot";
    private static final Comparator<SubscriptionInfo> SUBSCRIPTION_INFO_COMPARATOR = -$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg.$INST$0;
    protected static final boolean VDBG = Log.isLoggable(LOG_TAG, 2);
    protected static int mDefaultFallbackSubId = -1;
    protected static int mDefaultPhoneId = Integer.MAX_VALUE;
    protected static SubscriptionController sInstance = null;
    protected static Phone[] sPhones;
    private static Map<Integer, Integer> sSlotIndexToSubId = new ConcurrentHashMap();
    private int[] colorArr;
    private boolean inSwitchingDssState1 = false;
    private boolean inSwitchingDssState2 = false;
    private AppOpsManager mAppOps;
    protected CallManager mCM;
    private AtomicReference<List<SubscriptionInfo>> mCacheActiveSubInfoList = new AtomicReference();
    protected Context mContext;
    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AsyncResult ar = msg.obj;
                    synchronized (SubscriptionController.this.mLock) {
                        SubscriptionController.this.mSuccess = ar.exception == null;
                        SubscriptionController.this.logd("EVENT_WRITE_MSISDN_DONE, mSuccess = " + SubscriptionController.this.mSuccess);
                        SubscriptionController.this.mLock.notifyAll();
                    }
                    return;
                case 2:
                    SubscriptionController.this.checkSoftSimCard();
                    return;
                default:
                    return;
            }
        }
    };
    private ScLocalLog mLocalLog = new ScLocalLog(MAX_LOCAL_LOG_LINES);
    protected final Object mLock = new Object();
    protected boolean mSuccess;
    protected TelephonyManager mTelephonyManager;

    static class ScLocalLog {
        private LinkedList<String> mLog = new LinkedList();
        private int mMaxLines;
        private Time mNow;

        public ScLocalLog(int maxLines) {
            this.mMaxLines = maxLines;
            this.mNow = new Time();
        }

        public synchronized void log(String msg) {
            if (this.mMaxLines > 0) {
                int pid = Process.myPid();
                int tid = Process.myTid();
                this.mNow.setToNow();
                this.mLog.add(this.mNow.format("%m-%d %H:%M:%S") + " pid=" + pid + " tid=" + tid + " " + msg);
                while (this.mLog.size() > this.mMaxLines) {
                    this.mLog.remove();
                }
            }
        }

        public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            Iterator<String> itr = this.mLog.listIterator(0);
            int i = 0;
            while (true) {
                int i2 = i;
                if (itr.hasNext()) {
                    i = i2 + 1;
                    pw.println(Integer.toString(i2) + ": " + ((String) itr.next()));
                    if (i % 10 == 0) {
                        pw.flush();
                    }
                }
            }
        }
    }

    /* renamed from: lambda$-com_android_internal_telephony_SubscriptionController_7755 */
    static /* synthetic */ int m0x2e1b807b(SubscriptionInfo arg0, SubscriptionInfo arg1) {
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
                sInstance = new SubscriptionController(phone);
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
                sInstance = new SubscriptionController(c);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    public static SubscriptionController getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    protected SubscriptionController(Context c) {
        init(c);
    }

    protected void init(Context c) {
        this.mContext = c;
        this.mCM = CallManager.getInstance();
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        logdl("[SubscriptionController] init by Context");
    }

    private boolean isSubInfoReady() {
        return sSlotIndexToSubId.size() > 0;
    }

    private SubscriptionController(Phone phone) {
        this.mContext = phone.getContext();
        this.mCM = CallManager.getInstance();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        logdl("[SubscriptionController] init by Phone");
    }

    private boolean canReadPhoneState(String callingPackage, String message) {
        boolean z = true;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = false;
            }
            return z;
        }
    }

    protected void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private void broadcastSimInfoContentChanged() {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
        intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
    }

    public void notifySubscriptionInfoChanged() {
        ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        try {
            logd("notifySubscriptionInfoChanged:");
            tr.notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        broadcastSimInfoContentChanged();
    }

    private SubscriptionInfo getSubInfoRecord(Cursor cursor) {
        UiccAccessRule[] accessRules;
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String iccId = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
        int simSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
        String carrierName = cursor.getString(cursor.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor.getInt(cursor.getColumnIndexOrThrow("name_source"));
        int iconTint = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
        String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
        int dataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow("data_roaming"));
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302735);
        int mcc = cursor.getInt(cursor.getColumnIndexOrThrow(PlmnCodeEntry.MCC_ATTR));
        int mnc = cursor.getInt(cursor.getColumnIndexOrThrow(PlmnCodeEntry.MNC_ATTR));
        String countryIso = getSubscriptionCountryIso(id);
        boolean isEmbedded = false;
        try {
            isEmbedded = cursor.getInt(cursor.getColumnIndexOrThrow("is_embedded")) == 1;
        } catch (Exception e) {
            logd("get IS_EMBEDDED exception");
        }
        if (isEmbedded) {
            try {
                accessRules = UiccAccessRule.decodeRules(cursor.getBlob(cursor.getColumnIndexOrThrow("access_rules")));
            } catch (Exception e2) {
                accessRules = null;
                logd("get ACCESS_RULES exception");
            }
        } else {
            accessRules = null;
        }
        if (VDBG) {
            logd("[getSubInfoRecord] id:" + id + " iccid:" + SubscriptionInfo.givePrintableIccid(iccId) + " simSlotIndex:" + simSlotIndex + " displayName:" + displayName + " nameSource:" + nameSource + " iconTint:" + iconTint + " dataRoaming:" + dataRoaming + " mcc:" + mcc + " mnc:" + mnc + " countIso:" + countryIso + " isEmbedded:" + isEmbedded + " accessRules:" + Arrays.toString(accessRules));
        }
        String line1Number = this.mTelephonyManager.getLine1Number(id);
        if (!(TextUtils.isEmpty(line1Number) || (line1Number.equals(number) ^ 1) == 0)) {
            number = line1Number;
        }
        return new SubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso, isEmbedded, accessRules);
    }

    private String getSubscriptionCountryIso(int subId) {
        int phoneId = getPhoneId(subId);
        if (phoneId < 0) {
            return SpnOverride.MVNO_TYPE_NONE;
        }
        return this.mTelephonyManager.getSimCountryIsoForPhone(phoneId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<SubscriptionInfo> getSubInfo(String selection, Object queryKey) {
        Throwable th;
        if (VDBG) {
            logd("selection:" + selection + " " + queryKey);
        }
        String[] strArr = null;
        if (queryKey != null) {
            strArr = new String[]{queryKey.toString()};
        }
        List<SubscriptionInfo> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, strArr, null);
        if (cursor != null) {
            ArrayList<SubscriptionInfo> subList2;
            loop0:
            while (true) {
                while (true) {
                    try {
                        ArrayList<SubscriptionInfo> subList3;
                        subList2 = subList3;
                        if (!cursor.moveToNext()) {
                            break loop0;
                        }
                        SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                        if (subInfo == null) {
                            subList3 = subList2;
                            break;
                        }
                        if (subList2 == null) {
                            subList3 = new ArrayList();
                        } else {
                            subList3 = subList2;
                        }
                        try {
                            int slotID = subInfo.getSimSlotIndex();
                            logd("getSubInfo slotID is:" + slotID);
                            if (!OemConstant.isUiccSlotForbid(slotID)) {
                                subList3.add(subInfo);
                                break;
                            }
                            logd("getSubInfo the " + slotID + " is forbid");
                        } catch (Throwable th2) {
                            th = th2;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            Object subList4 = subList2;
        } else {
            logd("Query fail");
        }
        if (cursor != null) {
            cursor.close();
        }
        return subList4;
    }

    private int getUnusedColor(String callingPackage) {
        List<SubscriptionInfo> availableSubInfos = getActiveSubscriptionInfoList(callingPackage);
        this.colorArr = this.mContext.getResources().getIntArray(17236072);
        int colorIdx = 0;
        if (availableSubInfos != null) {
            int i = 0;
            while (i < this.colorArr.length) {
                int j = 0;
                while (j < availableSubInfos.size() && this.colorArr[i] != ((SubscriptionInfo) availableSubInfos.get(j)).getIconTint()) {
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

    public SubscriptionInfo getActiveSubscriptionInfo(int subId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfo")) {
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
            logd("[getActiveSubInfoForSubscriber]- subId=" + subId + " subList=" + subList + " subInfo=null");
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccId(String iccId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForIccId") || iccId == null) {
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
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForSimSlotIndex")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getSimSlotIndex() == slotIndex) {
                        logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex + " subId=" + si);
                        return si;
                    }
                }
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex + " subId=null");
            } else {
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ subList=null");
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getAllSubInfoList(String callingPackage) {
        logd("[getAllSubInfoList]+");
        if (!canReadPhoneState(callingPackage, "getAllSubInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getSubInfo(null, null);
            if (subList != null) {
                logd("[getAllSubInfoList]- " + subList.size() + " infos return");
            } else {
                logd("[getAllSubInfoList]- no info return");
            }
            Binder.restoreCallingIdentity(identity);
            return subList;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (isSubInfoReady()) {
                List<SubscriptionInfo> tmpCachedSubList = (List) this.mCacheActiveSubInfoList.get();
                if (tmpCachedSubList != null) {
                    List arrayList = new ArrayList(tmpCachedSubList);
                    Binder.restoreCallingIdentity(identity);
                    return arrayList;
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            }
            logdl("[getActiveSubInfoList] Sub Controller not ready");
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected void refreshCachedActiveSubscriptionInfoList() {
        long identity = Binder.clearCallingIdentity();
        try {
            if (isSubInfoReady()) {
                List<SubscriptionInfo> subList = getSubInfo("sim_id>=0", null);
                if (subList != null) {
                    subList.sort(SUBSCRIPTION_INFO_COMPARATOR);
                }
                this.mCacheActiveSubInfoList.set(subList);
                Binder.restoreCallingIdentity(identity);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getActiveSubInfoCount(String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (records == null) {
                if (VDBG) {
                    logd("[getActiveSubInfoCount] records null");
                }
                Binder.restoreCallingIdentity(identity);
                return 0;
            }
            if (VDBG) {
                logd("[getActiveSubInfoCount]- count: " + records.size());
            }
            int size = records.size();
            Binder.restoreCallingIdentity(identity);
            return size;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getAllSubInfoCount(String callingPackage) {
        logd("[getAllSubInfoCount]+");
        if (!canReadPhoneState(callingPackage, "getAllSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        Cursor cursor;
        try {
            cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                int count = cursor.getCount();
                logd("[getAllSubInfoCount]- " + count + " SUB(s) in DB");
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            logd("[getAllSubInfoCount]- no SUB in DB");
            Binder.restoreCallingIdentity(identity);
            return 0;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getActiveSubInfoCountMax() {
        return this.mTelephonyManager.getSimCount();
    }

    public List<SubscriptionInfo> getAvailableSubscriptionInfoList(String callingPackage) {
        if (canReadPhoneState(callingPackage, "getAvailableSubscriptionInfoList")) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (((EuiccManager) this.mContext.getSystemService("euicc_service")).isEnabled()) {
                    List<SubscriptionInfo> subList = getSubInfo("sim_id>=0 OR is_embedded=1", null);
                    if (subList != null) {
                        subList.sort(SUBSCRIPTION_INFO_COMPARATOR);
                        if (VDBG) {
                            logdl("[getAvailableSubInfoList]- " + subList.size() + " infos return");
                        }
                    } else {
                        logdl("[getAvailableSubInfoList]- no info return");
                    }
                    Binder.restoreCallingIdentity(identity);
                    return subList;
                }
                logdl("[getAvailableSubInfoList] Embedded subscriptions are disabled");
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new SecurityException("Need READ_PHONE_STATE to call  getAvailableSubscriptionInfoList");
        }
    }

    public List<SubscriptionInfo> getAccessibleSubscriptionInfoList(String callingPackage) {
        if (((EuiccManager) this.mContext.getSystemService("euicc_service")).isEnabled()) {
            this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
            long identity = Binder.clearCallingIdentity();
            try {
                List<SubscriptionInfo> subList = getSubInfo("is_embedded=1", null);
                if (subList == null) {
                    logdl("[getAccessibleSubInfoList] No info returned");
                    return null;
                }
                List<SubscriptionInfo> filteredList = (List) subList.stream().filter(new com.android.internal.telephony.-$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg.AnonymousClass1(this, callingPackage)).sorted(SUBSCRIPTION_INFO_COMPARATOR).collect(Collectors.toList());
                if (VDBG) {
                    logdl("[getAccessibleSubInfoList] " + filteredList.size() + " infos returned");
                }
                return filteredList;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            logdl("[getAccessibleSubInfoList] Embedded subscriptions are disabled");
            return null;
        }
    }

    /* renamed from: lambda$-com_android_internal_telephony_SubscriptionController_35654 */
    /* synthetic */ boolean m1x951b4c18(String callingPackage, SubscriptionInfo subscriptionInfo) {
        return subscriptionInfo.canManageSubscription(this.mContext, callingPackage);
    }

    public List<SubscriptionInfo> getSubscriptionInfoListForEmbeddedSubscriptionUpdate(String[] embeddedIccids, boolean isEuiccRemovable) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(").append("is_embedded").append("=1");
        if (isEuiccRemovable) {
            whereClause.append(" AND ").append("is_removable").append("=1");
        }
        whereClause.append(") OR ").append("icc_id").append(" IN (");
        for (int i = 0; i < embeddedIccids.length; i++) {
            if (i > 0) {
                whereClause.append(",");
            }
            whereClause.append("\"").append(embeddedIccids[i]).append("\"");
        }
        whereClause.append(")");
        List<SubscriptionInfo> list = getSubInfo(whereClause.toString(), null);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void requestEmbeddedSubscriptionInfoListRefresh() {
        this.mContext.enforceCallingOrSelfPermission("com.android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS", "requestEmbeddedSubscriptionInfoListRefresh");
        long token = Binder.clearCallingIdentity();
        try {
            PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void requestEmbeddedSubscriptionInfoListRefresh(Runnable callback) {
        PhoneFactory.requestEmbeddedSubscriptionInfoListRefresh(callback);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int, dom blocks: [B:2:0x0048, B:10:0x0086, B:19:0x00cb]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0208 A:{Catch:{ all -> 0x0303, all -> 0x02e1, all -> 0x02e8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0237 A:{Catch:{ all -> 0x0303, all -> 0x02e1, all -> 0x02e8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x00b1 A:{SYNTHETIC, Splitter: B:15:0x00b1} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00cb A:{SYNTHETIC, Splitter: B:19:0x00cb} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x024d A:{SYNTHETIC, Splitter: B:52:0x024d} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0319  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x025e A:{Catch:{ all -> 0x0303, all -> 0x02e1, all -> 0x02e8 }} */
    public int addSubInfoRecord(java.lang.String r32, int r33) {
        /*
        r31 = this;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "[addSubInfoRecord]+ iccId:";
        r5 = r5.append(r6);
        r6 = android.telephony.SubscriptionInfo.givePrintableIccid(r32);
        r5 = r5.append(r6);
        r6 = " slotIndex:";
        r5 = r5.append(r6);
        r0 = r33;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r0 = r31;
        r0.logdl(r5);
        r5 = "addSubInfoRecord";
        r0 = r31;
        r0.enforceModifyPhoneState(r5);
        r0 = r31;
        r5 = r0.mHandler;
        r0 = r31;
        r6 = r0.mHandler;
        r7 = 2;
        r6 = r6.obtainMessage(r7);
        r5.sendMessage(r6);
        r16 = android.os.Binder.clearCallingIdentity();
        if (r32 != 0) goto L_0x0055;
    L_0x0048:
        r5 = "[addSubInfoRecord]- null iccId";	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        r5 = -1;
        android.os.Binder.restoreCallingIdentity(r16);
        return r5;
    L_0x0055:
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r5 = r0.mContext;	 Catch:{ all -> 0x02e8 }
        r4 = r5.getContentResolver();	 Catch:{ all -> 0x02e8 }
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02e8 }
        r6 = 3;	 Catch:{ all -> 0x02e8 }
        r6 = new java.lang.String[r6];	 Catch:{ all -> 0x02e8 }
        r7 = "_id";	 Catch:{ all -> 0x02e8 }
        r8 = 0;	 Catch:{ all -> 0x02e8 }
        r6[r8] = r7;	 Catch:{ all -> 0x02e8 }
        r7 = "sim_id";	 Catch:{ all -> 0x02e8 }
        r8 = 1;	 Catch:{ all -> 0x02e8 }
        r6[r8] = r7;	 Catch:{ all -> 0x02e8 }
        r7 = "name_source";	 Catch:{ all -> 0x02e8 }
        r8 = 2;	 Catch:{ all -> 0x02e8 }
        r6[r8] = r7;	 Catch:{ all -> 0x02e8 }
        r7 = "icc_id=?";	 Catch:{ all -> 0x02e8 }
        r8 = 1;	 Catch:{ all -> 0x02e8 }
        r8 = new java.lang.String[r8];	 Catch:{ all -> 0x02e8 }
        r9 = 0;	 Catch:{ all -> 0x02e8 }
        r8[r9] = r32;	 Catch:{ all -> 0x02e8 }
        r9 = 0;	 Catch:{ all -> 0x02e8 }
        r13 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x02e8 }
        r22 = 0;
        if (r13 == 0) goto L_0x008e;
    L_0x0086:
        r5 = r13.moveToFirst();	 Catch:{ all -> 0x02e1 }
        r5 = r5 ^ 1;	 Catch:{ all -> 0x02e1 }
        if (r5 == 0) goto L_0x027e;	 Catch:{ all -> 0x02e1 }
    L_0x008e:
        r22 = 1;	 Catch:{ all -> 0x02e1 }
        r28 = r31.insertEmptySubInfoRecord(r32, r33);	 Catch:{ all -> 0x02e1 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e1 }
        r5.<init>();	 Catch:{ all -> 0x02e1 }
        r6 = "[addSubInfoRecord] New record created: ";	 Catch:{ all -> 0x02e1 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e1 }
        r0 = r28;	 Catch:{ all -> 0x02e1 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e1 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e1 }
        r0 = r31;	 Catch:{ all -> 0x02e1 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e1 }
    L_0x00af:
        if (r13 == 0) goto L_0x00b4;
    L_0x00b1:
        r13.close();	 Catch:{ all -> 0x02e8 }
    L_0x00b4:
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02e8 }
        r7 = "sim_id=?";	 Catch:{ all -> 0x02e8 }
        r6 = 1;	 Catch:{ all -> 0x02e8 }
        r8 = new java.lang.String[r6];	 Catch:{ all -> 0x02e8 }
        r6 = java.lang.String.valueOf(r33);	 Catch:{ all -> 0x02e8 }
        r9 = 0;	 Catch:{ all -> 0x02e8 }
        r8[r9] = r6;	 Catch:{ all -> 0x02e8 }
        r6 = 0;	 Catch:{ all -> 0x02e8 }
        r9 = 0;	 Catch:{ all -> 0x02e8 }
        r13 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x02e8 }
        if (r13 == 0) goto L_0x024b;
    L_0x00cb:
        r5 = r13.moveToFirst();	 Catch:{ all -> 0x0303 }
        if (r5 == 0) goto L_0x024b;	 Catch:{ all -> 0x0303 }
    L_0x00d1:
        r5 = "_id";	 Catch:{ all -> 0x0303 }
        r5 = r13.getColumnIndexOrThrow(r5);	 Catch:{ all -> 0x0303 }
        r25 = r13.getInt(r5);	 Catch:{ all -> 0x0303 }
        r5 = sSlotIndexToSubId;	 Catch:{ all -> 0x0303 }
        r6 = java.lang.Integer.valueOf(r33);	 Catch:{ all -> 0x0303 }
        r12 = r5.get(r6);	 Catch:{ all -> 0x0303 }
        r12 = (java.lang.Integer) r12;	 Catch:{ all -> 0x0303 }
        if (r12 == 0) goto L_0x00f2;	 Catch:{ all -> 0x0303 }
    L_0x00ea:
        r5 = r12.intValue();	 Catch:{ all -> 0x0303 }
        r0 = r25;	 Catch:{ all -> 0x0303 }
        if (r5 == r0) goto L_0x02ed;	 Catch:{ all -> 0x0303 }
    L_0x00f2:
        r5 = sSlotIndexToSubId;	 Catch:{ all -> 0x0303 }
        r6 = java.lang.Integer.valueOf(r33);	 Catch:{ all -> 0x0303 }
        r7 = java.lang.Integer.valueOf(r25);	 Catch:{ all -> 0x0303 }
        r5.put(r6, r7);	 Catch:{ all -> 0x0303 }
        r26 = r31.getActiveSubInfoCountMax();	 Catch:{ all -> 0x0303 }
        r14 = r31.getDefaultSubId();	 Catch:{ all -> 0x0303 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0303 }
        r5.<init>();	 Catch:{ all -> 0x0303 }
        r6 = "[addSubInfoRecord] sSlotIndexToSubId.size=";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r6 = sSlotIndexToSubId;	 Catch:{ all -> 0x0303 }
        r6 = r6.size();	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r6 = " slotIndex=";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r0 = r33;	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0303 }
        r6 = " subId=";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r0 = r25;	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0303 }
        r6 = " defaultSubId=";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r14);	 Catch:{ all -> 0x0303 }
        r6 = " simCount=";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r0 = r26;	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0303 }
        r5 = r5.toString();	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r0.logdl(r5);	 Catch:{ all -> 0x0303 }
        r5 = android.telephony.SubscriptionManager.isValidSubscriptionId(r14);	 Catch:{ all -> 0x0303 }
        if (r5 == 0) goto L_0x0163;	 Catch:{ all -> 0x0303 }
    L_0x015e:
        r5 = 1;	 Catch:{ all -> 0x0303 }
        r0 = r26;	 Catch:{ all -> 0x0303 }
        if (r0 != r5) goto L_0x030a;	 Catch:{ all -> 0x0303 }
    L_0x0163:
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r1 = r25;	 Catch:{ all -> 0x0303 }
        r0.setDefaultFallbackSubId(r1);	 Catch:{ all -> 0x0303 }
    L_0x016a:
        r5 = 1;	 Catch:{ all -> 0x0303 }
        r0 = r26;	 Catch:{ all -> 0x0303 }
        if (r0 != r5) goto L_0x019f;	 Catch:{ all -> 0x0303 }
    L_0x016f:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0303 }
        r5.<init>();	 Catch:{ all -> 0x0303 }
        r6 = "[addSubInfoRecord] one sim set defaults to subId=";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r0 = r25;	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0303 }
        r5 = r5.toString();	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r0.logdl(r5);	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r1 = r25;	 Catch:{ all -> 0x0303 }
        r0.setDefaultDataSubId(r1);	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r1 = r25;	 Catch:{ all -> 0x0303 }
        r0.setDefaultSmsSubId(r1);	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r1 = r25;	 Catch:{ all -> 0x0303 }
        r0.setDefaultVoiceSubId(r1);	 Catch:{ all -> 0x0303 }
    L_0x019f:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0303 }
        r5.<init>();	 Catch:{ all -> 0x0303 }
        r6 = "[addSubInfoRecord] hashmap(";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r0 = r33;	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0303 }
        r6 = ",";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r0 = r25;	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0303 }
        r6 = ")";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r5 = r5.toString();	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r0.logdl(r5);	 Catch:{ all -> 0x0303 }
        r5 = sPhones;	 Catch:{ all -> 0x0303 }
        r5 = r5[r33];	 Catch:{ all -> 0x0303 }
        if (r5 == 0) goto L_0x01f7;	 Catch:{ all -> 0x0303 }
    L_0x01d4:
        r5 = sPhones;	 Catch:{ all -> 0x0303 }
        r5 = r5[r33];	 Catch:{ all -> 0x0303 }
        r5 = r5.getServiceStateTracker();	 Catch:{ all -> 0x0303 }
        if (r5 == 0) goto L_0x01f7;	 Catch:{ all -> 0x0303 }
    L_0x01de:
        r5 = "[addSubInfoRecord] hashmap build, notifyServiceState";	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r0.logdl(r5);	 Catch:{ all -> 0x0303 }
        r5 = sPhones;	 Catch:{ all -> 0x0303 }
        r5 = r5[r33];	 Catch:{ all -> 0x0303 }
        r6 = sPhones;	 Catch:{ all -> 0x0303 }
        r6 = r6[r33];	 Catch:{ all -> 0x0303 }
        r6 = r6.getServiceStateTracker();	 Catch:{ all -> 0x0303 }
        r6 = r6.mSS;	 Catch:{ all -> 0x0303 }
        r5.notifyServiceStateChangedP(r6);	 Catch:{ all -> 0x0303 }
    L_0x01f7:
        r10 = 0;	 Catch:{ all -> 0x0303 }
        r11 = 0;	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r5 = r0.mContext;	 Catch:{ all -> 0x0303 }
        r6 = "carrier_config";	 Catch:{ all -> 0x0303 }
        r18 = r5.getSystemService(r6);	 Catch:{ all -> 0x0303 }
        r18 = (android.telephony.CarrierConfigManager) r18;	 Catch:{ all -> 0x0303 }
        if (r18 == 0) goto L_0x021c;	 Catch:{ all -> 0x0303 }
    L_0x0208:
        r0 = r18;	 Catch:{ all -> 0x0303 }
        r1 = r25;	 Catch:{ all -> 0x0303 }
        r10 = r0.getConfigForSubId(r1);	 Catch:{ all -> 0x0303 }
        if (r10 == 0) goto L_0x021c;	 Catch:{ all -> 0x0303 }
    L_0x0212:
        r5 = "oppo_probe";	 Catch:{ all -> 0x0303 }
        r5 = r10.getBoolean(r5);	 Catch:{ all -> 0x0303 }
        if (r5 == 0) goto L_0x0316;	 Catch:{ all -> 0x0303 }
    L_0x021b:
        r11 = 1;	 Catch:{ all -> 0x0303 }
    L_0x021c:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0303 }
        r5.<init>();	 Catch:{ all -> 0x0303 }
        r6 = "configLoaded is ";	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0303 }
        r5 = r5.append(r11);	 Catch:{ all -> 0x0303 }
        r5 = r5.toString();	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r0.logdl(r5);	 Catch:{ all -> 0x0303 }
        if (r11 == 0) goto L_0x0245;	 Catch:{ all -> 0x0303 }
    L_0x0237:
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r5 = r0.mContext;	 Catch:{ all -> 0x0303 }
        r0 = r33;	 Catch:{ all -> 0x0303 }
        r5 = com.android.ims.ImsManager.getInstance(r5, r0);	 Catch:{ all -> 0x0303 }
        r6 = 1;	 Catch:{ all -> 0x0303 }
        r5.updateImsServiceConfigForSlot(r6);	 Catch:{ all -> 0x0303 }
    L_0x0245:
        r5 = r13.moveToNext();	 Catch:{ all -> 0x0303 }
        if (r5 != 0) goto L_0x00d1;
    L_0x024b:
        if (r13 == 0) goto L_0x0250;
    L_0x024d:
        r13.close();	 Catch:{ all -> 0x02e8 }
    L_0x0250:
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r1 = r33;	 Catch:{ all -> 0x02e8 }
        r25 = r0.getSubIdUsingPhoneId(r1);	 Catch:{ all -> 0x02e8 }
        r5 = android.telephony.SubscriptionManager.isValidSubscriptionId(r25);	 Catch:{ all -> 0x02e8 }
        if (r5 != 0) goto L_0x0319;	 Catch:{ all -> 0x02e8 }
    L_0x025e:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord]- getSubId failed invalid subId = ";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r25;	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        r5 = -1;
        android.os.Binder.restoreCallingIdentity(r16);
        return r5;
    L_0x027e:
        r5 = 0;
        r25 = r13.getInt(r5);	 Catch:{ all -> 0x02e1 }
        r5 = 1;	 Catch:{ all -> 0x02e1 }
        r21 = r13.getInt(r5);	 Catch:{ all -> 0x02e1 }
        r5 = 2;	 Catch:{ all -> 0x02e1 }
        r19 = r13.getInt(r5);	 Catch:{ all -> 0x02e1 }
        r29 = new android.content.ContentValues;	 Catch:{ all -> 0x02e1 }
        r29.<init>();	 Catch:{ all -> 0x02e1 }
        r0 = r33;	 Catch:{ all -> 0x02e1 }
        r1 = r21;	 Catch:{ all -> 0x02e1 }
        if (r0 == r1) goto L_0x02a4;	 Catch:{ all -> 0x02e1 }
    L_0x0298:
        r5 = "sim_id";	 Catch:{ all -> 0x02e1 }
        r6 = java.lang.Integer.valueOf(r33);	 Catch:{ all -> 0x02e1 }
        r0 = r29;	 Catch:{ all -> 0x02e1 }
        r0.put(r5, r6);	 Catch:{ all -> 0x02e1 }
    L_0x02a4:
        r5 = 2;	 Catch:{ all -> 0x02e1 }
        r0 = r19;	 Catch:{ all -> 0x02e1 }
        if (r0 == r5) goto L_0x02ab;	 Catch:{ all -> 0x02e1 }
    L_0x02a9:
        r22 = 1;	 Catch:{ all -> 0x02e1 }
    L_0x02ab:
        r5 = r29.size();	 Catch:{ all -> 0x02e1 }
        if (r5 <= 0) goto L_0x02d7;	 Catch:{ all -> 0x02e1 }
    L_0x02b1:
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02e1 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e1 }
        r6.<init>();	 Catch:{ all -> 0x02e1 }
        r7 = "_id=";	 Catch:{ all -> 0x02e1 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x02e1 }
        r0 = r25;	 Catch:{ all -> 0x02e1 }
        r8 = (long) r0;	 Catch:{ all -> 0x02e1 }
        r7 = java.lang.Long.toString(r8);	 Catch:{ all -> 0x02e1 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x02e1 }
        r6 = r6.toString();	 Catch:{ all -> 0x02e1 }
        r7 = 0;	 Catch:{ all -> 0x02e1 }
        r0 = r29;	 Catch:{ all -> 0x02e1 }
        r4.update(r5, r0, r6, r7);	 Catch:{ all -> 0x02e1 }
        r31.refreshCachedActiveSubscriptionInfoList();	 Catch:{ all -> 0x02e1 }
    L_0x02d7:
        r5 = "[addSubInfoRecord] Record already exists";	 Catch:{ all -> 0x02e1 }
        r0 = r31;	 Catch:{ all -> 0x02e1 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e1 }
        goto L_0x00af;
    L_0x02e1:
        r5 = move-exception;
        if (r13 == 0) goto L_0x02e7;
    L_0x02e4:
        r13.close();	 Catch:{ all -> 0x02e8 }
    L_0x02e7:
        throw r5;	 Catch:{ all -> 0x02e8 }
    L_0x02e8:
        r5 = move-exception;
        android.os.Binder.restoreCallingIdentity(r16);
        throw r5;
    L_0x02ed:
        r5 = r12.intValue();	 Catch:{ all -> 0x0303 }
        r5 = android.telephony.SubscriptionManager.isValidSubscriptionId(r5);	 Catch:{ all -> 0x0303 }
        r5 = r5 ^ 1;	 Catch:{ all -> 0x0303 }
        if (r5 != 0) goto L_0x00f2;	 Catch:{ all -> 0x0303 }
    L_0x02f9:
        r5 = "[addSubInfoRecord] currentSubId != null && currentSubId is valid, IGNORE";	 Catch:{ all -> 0x0303 }
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r0.logdl(r5);	 Catch:{ all -> 0x0303 }
        goto L_0x019f;
    L_0x0303:
        r5 = move-exception;
        if (r13 == 0) goto L_0x0309;
    L_0x0306:
        r13.close();	 Catch:{ all -> 0x02e8 }
    L_0x0309:
        throw r5;	 Catch:{ all -> 0x02e8 }
    L_0x030a:
        r0 = r31;	 Catch:{ all -> 0x0303 }
        r5 = r0.isActiveSubId(r14);	 Catch:{ all -> 0x0303 }
        r5 = r5 ^ 1;
        if (r5 == 0) goto L_0x016a;
    L_0x0314:
        goto L_0x0163;
    L_0x0316:
        r11 = 0;
        goto L_0x021c;
    L_0x0319:
        if (r22 == 0) goto L_0x04ce;
    L_0x031b:
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r5 = r0.mTelephonyManager;	 Catch:{ all -> 0x02e8 }
        r0 = r25;	 Catch:{ all -> 0x02e8 }
        r23 = r5.getSimOperatorName(r0);	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r5 = r0.mTelephonyManager;	 Catch:{ all -> 0x02e8 }
        r0 = r25;	 Catch:{ all -> 0x02e8 }
        r15 = r5.getSubscriberId(r0);	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r1 = r25;	 Catch:{ all -> 0x02e8 }
        r27 = r0.getSubState(r1);	 Catch:{ all -> 0x02e8 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord] subState = ";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r27;	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord] simCarrierName = ";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r23;	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r1 = r33;	 Catch:{ all -> 0x02e8 }
        r24 = r0.getSimStateForSlotIndex(r1);	 Catch:{ all -> 0x02e8 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord] simState = ";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r24;	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        r5 = 6;	 Catch:{ all -> 0x02e8 }
        r0 = r24;	 Catch:{ all -> 0x02e8 }
        if (r0 == r5) goto L_0x0397;	 Catch:{ all -> 0x02e8 }
    L_0x0395:
        if (r27 != 0) goto L_0x04b4;	 Catch:{ all -> 0x02e8 }
    L_0x0397:
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r5 = r0.mContext;	 Catch:{ all -> 0x02e8 }
        r6 = "";	 Catch:{ all -> 0x02e8 }
        r7 = "";	 Catch:{ all -> 0x02e8 }
        r0 = r32;	 Catch:{ all -> 0x02e8 }
        r1 = r33;	 Catch:{ all -> 0x02e8 }
        r20 = getCarrierName(r5, r6, r7, r0, r1);	 Catch:{ all -> 0x02e8 }
    L_0x03a9:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord] nameToSet = ";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r20;	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord] carrier name = ";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r23;	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        r5 = "ro.oppo.version";	 Catch:{ all -> 0x02e8 }
        r6 = "CN";	 Catch:{ all -> 0x02e8 }
        r30 = android.os.SystemProperties.get(r5, r6);	 Catch:{ all -> 0x02e8 }
        r5 = "US";	 Catch:{ all -> 0x02e8 }
        r0 = r30;	 Catch:{ all -> 0x02e8 }
        r5 = r0.equalsIgnoreCase(r5);	 Catch:{ all -> 0x02e8 }
        if (r5 == 0) goto L_0x0417;	 Catch:{ all -> 0x02e8 }
    L_0x03f4:
        r5 = getSoftSimCardSlotIdInner();	 Catch:{ all -> 0x02e8 }
        r0 = r33;	 Catch:{ all -> 0x02e8 }
        if (r5 != r0) goto L_0x04c4;	 Catch:{ all -> 0x02e8 }
    L_0x03fc:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord]It's red tea, nameToSet = ";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r20;	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
    L_0x0417:
        r29 = new android.content.ContentValues;	 Catch:{ all -> 0x02e8 }
        r29.<init>();	 Catch:{ all -> 0x02e8 }
        r5 = "display_name";	 Catch:{ all -> 0x02e8 }
        r0 = r29;	 Catch:{ all -> 0x02e8 }
        r1 = r20;	 Catch:{ all -> 0x02e8 }
        r0.put(r5, r1);	 Catch:{ all -> 0x02e8 }
        r5 = "color";	 Catch:{ all -> 0x02e8 }
        r6 = 0;	 Catch:{ all -> 0x02e8 }
        r0 = r32;	 Catch:{ all -> 0x02e8 }
        r6 = com.android.internal.telephony.OemConstant.getCardType(r6, r0);	 Catch:{ all -> 0x02e8 }
        r6 = java.lang.Integer.valueOf(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r29;	 Catch:{ all -> 0x02e8 }
        r0.put(r5, r6);	 Catch:{ all -> 0x02e8 }
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x02e8 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r6.<init>();	 Catch:{ all -> 0x02e8 }
        r7 = "_id=";	 Catch:{ all -> 0x02e8 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x02e8 }
        r0 = r25;	 Catch:{ all -> 0x02e8 }
        r8 = (long) r0;	 Catch:{ all -> 0x02e8 }
        r7 = java.lang.Long.toString(r8);	 Catch:{ all -> 0x02e8 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x02e8 }
        r6 = r6.toString();	 Catch:{ all -> 0x02e8 }
        r7 = 0;	 Catch:{ all -> 0x02e8 }
        r0 = r29;	 Catch:{ all -> 0x02e8 }
        r4.update(r5, r0, r6, r7);	 Catch:{ all -> 0x02e8 }
        r31.refreshCachedActiveSubscriptionInfoList();	 Catch:{ all -> 0x02e8 }
        r5 = "-1";	 Catch:{ all -> 0x02e8 }
        r6 = java.lang.Integer.toString(r25);	 Catch:{ all -> 0x02e8 }
        r7 = "CARDTYPE";	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.broadcastSubInfoUpdateIntent(r5, r6, r7);	 Catch:{ all -> 0x02e8 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord] sim name = ";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r0 = r20;	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
    L_0x0489:
        r5 = sPhones;	 Catch:{ all -> 0x02e8 }
        r5 = r5[r33];	 Catch:{ all -> 0x02e8 }
        r5.updateDataConnectionTracker();	 Catch:{ all -> 0x02e8 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02e8 }
        r5.<init>();	 Catch:{ all -> 0x02e8 }
        r6 = "[addSubInfoRecord]- info size=";	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r6 = sSlotIndexToSubId;	 Catch:{ all -> 0x02e8 }
        r6 = r6.size();	 Catch:{ all -> 0x02e8 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x02e8 }
        r5 = r5.toString();	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        android.os.Binder.restoreCallingIdentity(r16);
        r5 = 0;
        return r5;
    L_0x04b4:
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r5 = r0.mContext;	 Catch:{ all -> 0x02e8 }
        r0 = r23;	 Catch:{ all -> 0x02e8 }
        r1 = r32;	 Catch:{ all -> 0x02e8 }
        r2 = r33;	 Catch:{ all -> 0x02e8 }
        r20 = getCarrierName(r5, r0, r15, r1, r2);	 Catch:{ all -> 0x02e8 }
        goto L_0x03a9;	 Catch:{ all -> 0x02e8 }
    L_0x04c4:
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r1 = r33;	 Catch:{ all -> 0x02e8 }
        r20 = r0.getExportSimDefaultName(r1);	 Catch:{ all -> 0x02e8 }
        goto L_0x0417;	 Catch:{ all -> 0x02e8 }
    L_0x04ce:
        r5 = r31.isSubInfoReady();	 Catch:{ all -> 0x02e8 }
        if (r5 == 0) goto L_0x0489;	 Catch:{ all -> 0x02e8 }
    L_0x04d4:
        r5 = "[addSubInfoRecord] name is obtained by user input, refresh the mCacheActiveSubInfoList";	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.logdl(r5);	 Catch:{ all -> 0x02e8 }
        r31.refreshCachedActiveSubscriptionInfoList();	 Catch:{ all -> 0x02e8 }
        r5 = "-1";	 Catch:{ all -> 0x02e8 }
        r6 = java.lang.Integer.toString(r25);	 Catch:{ all -> 0x02e8 }
        r7 = "CARDTYPE";	 Catch:{ all -> 0x02e8 }
        r0 = r31;	 Catch:{ all -> 0x02e8 }
        r0.broadcastSubInfoUpdateIntent(r5, r6, r7);	 Catch:{ all -> 0x02e8 }
        goto L_0x0489;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int");
    }

    public Uri insertEmptySubInfoRecord(String iccId, int slotIndex) {
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues();
        value.put("icc_id", iccId);
        value.put("color", Integer.valueOf(getUnusedColor(this.mContext.getOpPackageName())));
        value.put("sim_id", Integer.valueOf(slotIndex));
        value.put("carrier_name", SpnOverride.MVNO_TYPE_NONE);
        Uri uri = resolver.insert(SubscriptionManager.CONTENT_URI, value);
        refreshCachedActiveSubscriptionInfoList();
        return uri;
    }

    public boolean setPlmnSpn(int slotIndex, boolean showPlmn, String plmn, boolean showSpn, String spn) {
        synchronized (this.mLock) {
            int subId = getSubIdUsingPhoneId(slotIndex);
            if (this.mContext.getPackageManager().resolveContentProvider(SubscriptionManager.CONTENT_URI.getAuthority(), 0) == null || (SubscriptionManager.isValidSubscriptionId(subId) ^ 1) != 0) {
                logd("[setPlmnSpn] No valid subscription to store info");
                notifySubscriptionInfoChanged();
                return false;
            }
            String carrierText = SpnOverride.MVNO_TYPE_NONE;
            if (showPlmn) {
                carrierText = plmn;
                if (showSpn && !Objects.equals(spn, plmn)) {
                    carrierText = plmn + this.mContext.getString(17040128).toString() + spn;
                }
            } else if (showSpn) {
                carrierText = spn;
            }
            setCarrierText(carrierText, subId);
            return true;
        }
    }

    private int setCarrierText(String text, int subId) {
        logd("[setCarrierText]+ text:" + text + " subId:" + subId);
        enforceModifyPhoneState("setCarrierText");
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, new String[]{"carrier_name"}, "_id=" + Integer.toString(subId), null, null);
            if (cursor == null) {
                logd("leon setCarrierText Query fail");
            } else if (cursor.moveToNext()) {
                String carrierName = cursor.getString(0);
                if (text != null && text.equals(carrierName)) {
                    logd("leon setCarrierText block for the same");
                    if (cursor != null) {
                        cursor.close();
                    }
                    Binder.restoreCallingIdentity(identity);
                    return 0;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            logd("leon setCarrierText Query error:" + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
        ContentValues value = new ContentValues(1);
        value.put("carrier_name", text);
        int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
        refreshCachedActiveSubscriptionInfoList();
        notifySubscriptionInfoChanged();
        Binder.restoreCallingIdentity(identity);
        return result;
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
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDisplayName(String displayName, int subId) {
        return setDisplayNameUsingSrc(displayName, subId, -1);
    }

    public int setDisplayNameUsingSrc(String displayName, int subId, long nameSource) {
        logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId + " nameSource:" + nameSource);
        enforceModifyPhoneState("setDisplayNameUsingSrc");
        long identity = Binder.clearCallingIdentity();
        try {
            String nameToSet;
            validateSubId(subId);
            if (displayName == null) {
                nameToSet = this.mContext.getString(17039374);
            } else {
                nameToSet = displayName;
            }
            ContentValues value = new ContentValues(1);
            value.put("display_name", nameToSet);
            if (nameSource >= 0) {
                logd("Set nameSource=" + nameSource);
                value.put("name_source", Long.valueOf(nameSource));
            }
            logd("[setDisplayName]- mDisplayName:" + nameToSet + " set");
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            return result;
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
                    int result;
                    ContentValues value = new ContentValues(1);
                    value.put("number", number);
                    Phone phone = sPhones[phoneId];
                    String alphaTag = TelephonyManager.getDefault().getLine1AlphaTagForSubscriber(subId);
                    synchronized (this.mLock) {
                        this.mSuccess = false;
                        if (phone.setLine1Number(alphaTag, number, this.mHandler.obtainMessage(1))) {
                            try {
                                this.mLock.wait(3000);
                            } catch (InterruptedException e) {
                                loge("interrupted while trying to write MSISDN");
                            }
                        }
                    }
                    if (this.mSuccess) {
                        result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
                    } else {
                        result = 0;
                    }
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
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            refreshCachedActiveSubscriptionInfoList();
            notifySubscriptionInfoChanged();
            Binder.restoreCallingIdentity(identity);
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setMccMnc(String mccMnc, int subId) {
        int mcc = 0;
        int mnc = 0;
        try {
            mcc = Integer.parseInt(mccMnc.substring(0, 3));
            mnc = Integer.parseInt(mccMnc.substring(3));
        } catch (NumberFormatException e) {
            loge("[setMccMnc] - couldn't parse mcc/mnc: " + mccMnc);
        }
        logd("[setMccMnc]+ mcc/mnc:" + mcc + "/" + mnc + " subId:" + subId);
        ContentValues value = new ContentValues(2);
        value.put(PlmnCodeEntry.MCC_ATTR, Integer.valueOf(mcc));
        value.put(PlmnCodeEntry.MNC_ATTR, Integer.valueOf(mnc));
        int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
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
        } else if (sSlotIndexToSubId.size() == 0) {
            logd("[getSlotIndex]- size == 0, return SIM_NOT_INSERTED instead");
            return -1;
        } else {
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                int sim = ((Integer) entry.getKey()).intValue();
                if (subId == ((Integer) entry.getValue()).intValue()) {
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
        if (!SubscriptionManager.isValidSlotIndex(slotIndex)) {
            logd("[getSubId]- invalid slotIndex=" + slotIndex);
            return null;
        } else if (sSlotIndexToSubId.size() == 0) {
            if (VDBG) {
                logd("[getSubId]- sSlotIndexToSubId.size == 0, return DummySubIds slotIndex=" + slotIndex);
            }
            return getDummySubIds(slotIndex);
        } else {
            ArrayList<Integer> subIds = new ArrayList();
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                int slot = ((Integer) entry.getKey()).intValue();
                int sub = ((Integer) entry.getValue()).intValue();
                if (slotIndex == slot) {
                    subIds.add(Integer.valueOf(sub));
                }
            }
            int numSubIds = subIds.size();
            if (numSubIds > 0) {
                int[] subIdArr = new int[numSubIds];
                for (int i = 0; i < numSubIds; i++) {
                    subIdArr[i] = ((Integer) subIds.get(i)).intValue();
                }
                if (VDBG) {
                    logd("[getSubId]- subIdArr=" + subIdArr);
                }
                return subIdArr;
            }
            logd("[getSubId]- numSubIds == 0, return DummySubIds slotIndex=" + slotIndex);
            return getDummySubIds(slotIndex);
        }
    }

    public int getPhoneId(int subId) {
        if (VDBG) {
            printStackTrace("[getPhoneId] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
            logdl("[getPhoneId] asked for default subId=" + subId);
        }
        int phoneId;
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            if (VDBG) {
                logdl("[getPhoneId]- invalid subId return=-1");
            }
            return -1;
        } else if (sSlotIndexToSubId.size() == 0) {
            phoneId = mDefaultPhoneId;
            logdl("[getPhoneId]- no sims, returning default phoneId=" + phoneId);
            return phoneId;
        } else {
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                int sim = ((Integer) entry.getKey()).intValue();
                if (subId == ((Integer) entry.getValue()).intValue()) {
                    if (VDBG) {
                        logdl("[getPhoneId]- found subId=" + subId + " phoneId=" + sim);
                    }
                    return sim;
                }
            }
            phoneId = mDefaultPhoneId;
            logdl("[getPhoneId]- subId=" + subId + " not found return default phoneId=" + phoneId);
            return phoneId;
        }
    }

    protected int[] getDummySubIds(int slotIndex) {
        int numSubs = getActiveSubInfoCountMax();
        if (numSubs <= 0) {
            return null;
        }
        int[] dummyValues = new int[numSubs];
        for (int i = 0; i < numSubs; i++) {
            dummyValues[i] = -2 - slotIndex;
        }
        if (VDBG) {
            logd("getDummySubIds: slotIndex=" + slotIndex + " return " + numSubs + " DummySubIds with each subId=" + dummyValues[0]);
        }
        return dummyValues;
    }

    public int clearSubInfo() {
        enforceModifyPhoneState("clearSubInfo");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        long identity = Binder.clearCallingIdentity();
        try {
            int size = sSlotIndexToSubId.size();
            if (size == 0) {
                logdl("[clearSubInfo]- no simInfo size=" + size);
                return 0;
            }
            sSlotIndexToSubId.clear();
            logdl("[clearSubInfo]- clear size=" + size);
            Binder.restoreCallingIdentity(identity);
            return size;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected void logvl(String msg) {
        logv(msg);
        this.mLocalLog.log(msg);
    }

    protected void logv(String msg) {
        Rlog.v(LOG_TAG, msg);
    }

    protected void logdl(String msg) {
        logd(msg);
        this.mLocalLog.log(msg);
    }

    protected static void slogd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    protected void logel(String msg) {
        loge(msg);
        this.mLocalLog.log(msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

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
        if (this.mContext.getResources().getBoolean(17957059)) {
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

    public void setDefaultSmsSubId(int subId) {
        enforceModifyPhoneState("setDefaultSmsSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSmsSubId called with DEFAULT_SUB_ID");
        }
        if (isHasSoftSimCard()) {
            int softSimSlotId = getSoftSimCardSlotId();
            if (softSimSlotId > -1) {
                int[] subIds = getSubId(softSimSlotId);
                if (subIds == null || subIds.length <= 0) {
                    logdl("[setDefaultSmsSubId]- getSubId failed subIds == null || length == 0 subIds=" + subIds);
                } else if (subIds[0] == subId) {
                    logdl("[setDefaultSmsSubId]- this subid refrence to the Soft sim, so return ");
                    return;
                }
            }
            logdl("[setDefaultSmsSubId]- SoftSimCard enable,but slotId is wrong!!,softSimSlotId:" + softSimSlotId);
        } else {
            logdl("[setDefaultSmsSubId]- SoftSimCard disable!!");
        }
        logdl("[setDefaultSmsSubId] subId=" + subId);
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms", subId);
        broadcastDefaultSmsSubIdChanged(subId);
    }

    private void broadcastDefaultSmsSubIdChanged(int subId) {
        logdl("[broadcastDefaultSmsSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.telephony.action.DEFAULT_SMS_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("color_int_subId", subId);
        intent.putExtra("subscription", subId);
        intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultSmsSubId() {
        int subId;
        try {
            subId = Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms", -1);
            if (isActiveSubId(subId)) {
                return subId;
            }
            int subIdRet = getDefaultDataSubId();
            if (VDBG) {
                Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "[getDefaultSmsSubId] subId=" + subIdRet);
            }
            return subIdRet;
        } catch (Exception e) {
            e.printStackTrace();
            subId = Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms", -1);
            if (VDBG) {
                logd("[getDefaultSmsSubId] subId=" + subId);
            }
            return subId;
        }
    }

    public void setDefaultVoiceSubId(int subId) {
        enforceModifyPhoneState("setDefaultVoiceSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultVoiceSubId called with DEFAULT_SUB_ID");
        }
        if (isHasSoftSimCard()) {
            int softSimSlotId = getSoftSimCardSlotId();
            if (softSimSlotId > -1) {
                int[] subIds = getSubId(softSimSlotId);
                if (subIds != null) {
                    logdl("[setDefaultVoiceSubId]- Soft sim slot id: " + softSimSlotId + "Soft sim subId: " + subIds[0]);
                    if (subIds.length <= 0) {
                        logdl("[setDefaultVoiceSubId]- getSubId failed subIds length == 0 subIds=" + subIds);
                    } else if (subIds[0] == subId) {
                        logdl("[setDefaultVoiceSubId]- this subid refrence to the Soft sim, so return ");
                        return;
                    }
                }
                logdl("[setDefaultVoiceSubId] subIds = null");
            } else {
                logdl("[setDefaultVoiceSubId]- SoftSimCard enable,but slotId is wrong!!,softSimSlotId:" + softSimSlotId);
            }
        } else {
            logdl("[setDefaultVoiceSubId]- soft sim disable!!");
        }
        logdl("[setDefaultVoiceSubId] subId=" + subId);
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_call", subId);
        broadcastDefaultVoiceSubIdChanged(subId);
    }

    private void broadcastDefaultVoiceSubIdChanged(int subId) {
        logdl("[broadcastDefaultVoiceSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("color_int_subId", subId);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultVoiceSubId() {
        int subId = Global.getInt(this.mContext.getContentResolver(), "multi_sim_voice_call", -1);
        if (VDBG) {
            logd("[getDefaultVoiceSubId] subId=" + subId);
        }
        return subId;
    }

    public int getDefaultDataSubId() {
        int subId = Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", -1);
        if (VDBG) {
            logd("[getDefaultDataSubId] subId= " + subId);
        }
        return subId;
    }

    public void setDefaultDataSubId(int subId) {
        enforceModifyPhoneState("setDefaultDataSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultDataSubId called with DEFAULT_SUB_ID");
        }
        ProxyController proxyController = ProxyController.getInstance();
        int len = sPhones.length;
        logdl("[setDefaultDataSubId] num phones=" + len + ", subId=" + subId);
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            RadioAccessFamily[] rafs = new RadioAccessFamily[len];
            boolean atLeastOneMatch = false;
            for (int phoneId = 0; phoneId < len; phoneId++) {
                int raf;
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
        int oldDdsSubId = getDefaultDataSubId();
        logdl("oldDdsSubId = " + oldDdsSubId + " , to set current dds subId = " + subId);
        if (oldDdsSubId != subId) {
            setSwitchingDssState(0, true);
            setSwitchingDssState(1, true);
        }
        this.mHandler.sendEmptyMessageDelayed(10, 3000);
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
        broadcastDefaultDataSubIdChanged(subId);
    }

    protected void updateAllDataConnectionTrackers() {
        int len = sPhones.length;
        logdl("[updateAllDataConnectionTrackers] sPhones.length=" + len);
        for (int phoneId = 0; phoneId < len; phoneId++) {
            logdl("[updateAllDataConnectionTrackers] phoneId=" + phoneId);
            sPhones[phoneId].updateDataConnectionTracker();
        }
    }

    protected void broadcastDefaultDataSubIdChanged(int subId) {
        logdl("[broadcastDefaultDataSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intent.addFlags(553648128);
        intent.putExtra("color_int_subId", subId);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    protected void setDefaultFallbackSubId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSubId called with DEFAULT_SUB_ID");
        }
        logdl("[setDefaultFallbackSubId] subId=" + subId);
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            int phoneId = getPhoneId(subId);
            if (phoneId < 0 || (phoneId >= this.mTelephonyManager.getPhoneCount() && this.mTelephonyManager.getSimCount() != 1)) {
                logdl("[setDefaultFallbackSubId] not set invalid phoneId=" + phoneId + " subId=" + subId);
                return;
            }
            logdl("[setDefaultFallbackSubId] set mDefaultFallbackSubId=" + subId);
            mDefaultFallbackSubId = subId;
            MccTable.updateMccMncConfiguration(this.mContext, this.mTelephonyManager.getSimOperatorNumericForPhone(phoneId), false);
            Intent intent = new Intent("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED");
            intent.addFlags(553648128);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, subId);
            logdl("[setDefaultFallbackSubId] broadcast default subId changed phoneId=" + phoneId + " subId=" + subId);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public void clearDefaultsForInactiveSubIds() {
        enforceModifyPhoneState("clearDefaultsForInactiveSubIds");
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            logdl("[clearDefaultsForInactiveSubIds] records: " + records);
            if (shouldDefaultBeCleared(records, getDefaultDataSubId())) {
                logd("[clearDefaultsForInactiveSubIds] clearing default data sub id");
                setDefaultDataSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultSmsSubId())) {
                logdl("[clearDefaultsForInactiveSubIds] clearing default sms sub id");
                setDefaultSmsSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultVoiceSubId())) {
                logdl("[clearDefaultsForInactiveSubIds] clearing default voice sub id");
                setDefaultVoiceSubId(-1);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected boolean shouldDefaultBeCleared(List<SubscriptionInfo> records, int subId) {
        logdl("[shouldDefaultBeCleared: subId] " + subId);
        if (records == null) {
            logdl("[shouldDefaultBeCleared] return true no records subId=" + subId);
            return true;
        } else if (SubscriptionManager.isValidSubscriptionId(subId)) {
            for (SubscriptionInfo record : records) {
                int id = record.getSubscriptionId();
                logdl("[shouldDefaultBeCleared] Record.id: " + id);
                if (id == subId) {
                    logdl("[shouldDefaultBeCleared] return false subId is active, subId=" + subId);
                    return false;
                }
            }
            logdl("[shouldDefaultBeCleared] return true not active subId=" + subId);
            return true;
        } else {
            logdl("[shouldDefaultBeCleared] return false only one subId, subId=" + subId);
            return false;
        }
    }

    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return -1;
        }
        return subIds[0];
    }

    public List<SubscriptionInfo> getSubInfoUsingSlotIndexWithCheck(int slotIndex, boolean needCheck, String callingPackage) {
        Throwable th;
        logd("[getSubInfoUsingSlotIndexWithCheck]+ slotIndex:" + slotIndex);
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIndexWithCheck")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        if (slotIndex == Integer.MAX_VALUE) {
            try {
                slotIndex = getSlotIndex(getDefaultSubId());
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(identity);
            }
        }
        if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
            if (needCheck) {
                if ((isSubInfoReady() ^ 1) != 0) {
                    logd("[getSubInfoUsingSlotIndexWithCheck]- not ready");
                    Binder.restoreCallingIdentity(identity);
                    return null;
                }
            }
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotIndex)}, null);
            List<SubscriptionInfo> subList = null;
            if (cursor != null) {
                while (true) {
                    ArrayList<SubscriptionInfo> subList2;
                    ArrayList<SubscriptionInfo> subList3 = subList2;
                    try {
                        if (!cursor.moveToNext()) {
                            subList = subList3;
                            break;
                        }
                        SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                        if (subInfo != null) {
                            if (subList3 == null) {
                                subList2 = new ArrayList();
                            } else {
                                subList2 = subList3;
                            }
                            try {
                                subList2.add(subInfo);
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        } else {
                            subList2 = subList3;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        subList2 = subList3;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            logd("[getSubInfoUsingSlotIndex]- null info return");
            Binder.restoreCallingIdentity(identity);
            return subList;
        }
        logd("[getSubInfoUsingSlotIndexWithCheck]- invalid slotIndex");
        Binder.restoreCallingIdentity(identity);
        return null;
    }

    private void validateSubId(int subId) {
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

    public int[] getActiveSubIdList() {
        Set<Entry<Integer, Integer>> simInfoSet = new HashSet(sSlotIndexToSubId.entrySet());
        int[] subIdArr = new int[simInfoSet.size()];
        int i = 0;
        try {
            for (Entry<Integer, Integer> entry : simInfoSet) {
                subIdArr[i] = ((Integer) entry.getValue()).intValue();
                i++;
            }
        } catch (Exception e) {
        }
        if (VDBG) {
            logdl("[getActiveSubIdList] simInfoSet=" + simInfoSet + " subIdArr.length=" + subIdArr.length);
        }
        return subIdArr;
    }

    public boolean isActiveSubId(int subId) {
        boolean retVal;
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            retVal = sSlotIndexToSubId.containsValue(Integer.valueOf(subId));
        } else {
            retVal = false;
        }
        if (VDBG) {
            logdl("[isActiveSubId]- " + retVal);
        }
        return retVal;
    }

    public int getSimStateForSlotIndex(int slotIndex) {
        State simState;
        String err;
        if (slotIndex < 0) {
            simState = State.UNKNOWN;
            err = "invalid slotIndex";
        } else {
            Phone phone = PhoneFactory.getPhone(slotIndex);
            if (phone == null) {
                simState = State.UNKNOWN;
                err = "phone == null";
            } else {
                IccCard icc = phone.getIccCard();
                if (icc == null) {
                    simState = State.UNKNOWN;
                    err = "icc == null";
                } else {
                    simState = icc.getState();
                    err = SpnOverride.MVNO_TYPE_NONE;
                }
            }
        }
        if (VDBG) {
            logd("getSimStateForSlotIndex: " + err + " simState=" + simState + " ordinal=" + simState.ordinal() + " slotIndex=" + slotIndex);
        }
        return simState.ordinal();
    }

    public void setSubscriptionProperty(int subId, String propKey, String propValue) {
        enforceModifyPhoneState("setSubscriptionProperty");
        long token = Binder.clearCallingIdentity();
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues();
        if (propKey.equals("enable_cmas_extreme_threat_alerts") || propKey.equals("enable_cmas_severe_threat_alerts") || propKey.equals("enable_cmas_amber_alerts") || propKey.equals("enable_emergency_alerts") || propKey.equals("alert_sound_duration") || propKey.equals("alert_reminder_interval") || propKey.equals("enable_alert_vibrate") || propKey.equals("enable_alert_speech") || propKey.equals("enable_etws_test_alerts") || propKey.equals("enable_channel_50_alerts") || propKey.equals("enable_cmas_test_alerts") || propKey.equals("show_cmas_opt_out_dialog")) {
            value.put(propKey, Integer.valueOf(Integer.parseInt(propValue)));
        } else {
            logd("Invalid column name");
        }
        resolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(subId), null);
        refreshCachedActiveSubscriptionInfoList();
        Binder.restoreCallingIdentity(token);
    }

    public String getSubscriptionProperty(int subId, String propKey, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIndexWithCheck")) {
            return null;
        }
        String resultValue = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, new String[]{propKey}, InboundSmsHandler.SELECT_BY_ID, new String[]{subId + SpnOverride.MVNO_TYPE_NONE}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (!propKey.equals("enable_cmas_extreme_threat_alerts")) {
                        if (!(propKey.equals("enable_cmas_severe_threat_alerts") || propKey.equals("enable_cmas_amber_alerts") || propKey.equals("enable_emergency_alerts") || propKey.equals("alert_sound_duration") || propKey.equals("alert_reminder_interval") || propKey.equals("enable_alert_vibrate") || propKey.equals("enable_alert_speech") || propKey.equals("enable_etws_test_alerts") || propKey.equals("enable_channel_50_alerts") || propKey.equals("enable_cmas_test_alerts") || propKey.equals("show_cmas_opt_out_dialog"))) {
                            logd("Invalid column name");
                        }
                    }
                    resultValue = cursor.getInt(0) + SpnOverride.MVNO_TYPE_NONE;
                } else {
                    logd("Valid row not present in db");
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            logd("Query failed");
        }
        if (cursor != null) {
            cursor.close();
        }
        logd("getSubscriptionProperty Query value = " + resultValue);
        return resultValue;
    }

    protected static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        slogd("StackTrace - " + msg);
        boolean first = true;
        for (StackTraceElement ste : re.getStackTrace()) {
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
            pw.println(" defaultSubId=" + getDefaultSubId());
            pw.println(" defaultDataSubId=" + getDefaultDataSubId());
            pw.println(" defaultVoiceSubId=" + getDefaultVoiceSubId());
            pw.println(" defaultSmsSubId=" + getDefaultSmsSubId());
            pw.println(" defaultDataPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultDataPhoneId());
            pw.println(" defaultVoicePhoneId=" + SubscriptionManager.getDefaultVoicePhoneId());
            pw.println(" defaultSmsPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultSmsPhoneId());
            pw.flush();
            for (Entry<Integer, Integer> entry : sSlotIndexToSubId.entrySet()) {
                pw.println(" sSlotIndexToSubId[" + entry.getKey() + "]: subId=" + entry.getValue());
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            List<SubscriptionInfo> sirl = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" ActiveSubInfoList:");
                for (SubscriptionInfo entry2 : sirl) {
                    pw.println("  " + entry2.toString());
                }
            } else {
                pw.println(" ActiveSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            sirl = getAllSubInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" AllSubInfoList:");
                for (SubscriptionInfo entry22 : sirl) {
                    pw.println("  " + entry22.toString());
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

    public List<SubInfoRecord> getActiveSubInfoList(String callingPackage) {
        List<SubInfoRecord> list = null;
        logdl("[getActiveSubInfoList]+");
        if (!canReadPhoneState(callingPackage, "getActiveSubInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (isSubInfoReady()) {
                list = null;
                List<SubInfoRecord> subList = colorgetSubInfo("sim_id>=0", null);
                if (subList != null) {
                    Collections.sort(subList, new Comparator<SubInfoRecord>() {
                        public int compare(SubInfoRecord arg0, SubInfoRecord arg1) {
                            int flag = arg0.getSimSlotIndex() - arg1.getSimSlotIndex();
                            if (flag == 0) {
                                return arg0.getSubscriptionId() - arg1.getSubscriptionId();
                            }
                            return flag;
                        }
                    });
                    logdl("[getActiveSubInfoList]- " + subList.size() + " infos return");
                } else {
                    logdl("[getActiveSubInfoList]- no info return");
                }
                Binder.restoreCallingIdentity(identity);
                return subList;
            }
            logdl("[getActiveSubInfoList] Sub Controller not ready");
            return list;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x009f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<SubInfoRecord> colorgetSubInfo(String selection, Object queryKey) {
        Throwable th;
        logd("selection:" + selection + " " + queryKey);
        String[] strArr = null;
        if (queryKey != null) {
            strArr = new String[]{queryKey.toString()};
        }
        List<SubInfoRecord> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, strArr, "sim_id ASC");
        if (cursor != null) {
            ArrayList<SubInfoRecord> subList2;
            loop0:
            while (true) {
                while (true) {
                    try {
                        ArrayList<SubInfoRecord> subList3;
                        subList2 = subList3;
                        if (!cursor.moveToNext()) {
                            break loop0;
                        }
                        SubInfoRecord subInfo = colorgetSubInfoRecord(cursor);
                        if (subInfo == null) {
                            subList3 = subList2;
                            break;
                        }
                        if (subList2 == null) {
                            subList3 = new ArrayList();
                        } else {
                            subList3 = subList2;
                        }
                        try {
                            int slotID = subInfo.getSimSlotIndex();
                            logd("colorgetSubInfo slotID is:" + slotID);
                            if (!OemConstant.isUiccSlotForbid(slotID)) {
                                subList3.add(subInfo);
                                break;
                            }
                            logd("colorgetSubInfo the " + slotID + " is forbid");
                        } catch (Throwable th2) {
                            th = th2;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                }
            }
            Object subList4 = subList2;
        } else {
            logd("Query fail");
        }
        if (cursor != null) {
            cursor.close();
        }
        return subList4;
    }

    private SubInfoRecord colorgetSubInfoRecord(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String iccId = cursor.getString(cursor.getColumnIndexOrThrow("icc_id"));
        int simSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow("sim_id"));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"));
        String carrierName = cursor.getString(cursor.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor.getInt(cursor.getColumnIndexOrThrow("name_source"));
        int iconTint = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
        String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
        int dataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow("data_roaming"));
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302735);
        int mcc = cursor.getInt(cursor.getColumnIndexOrThrow(PlmnCodeEntry.MCC_ATTR));
        int mnc = cursor.getInt(cursor.getColumnIndexOrThrow(PlmnCodeEntry.MNC_ATTR));
        String countryIso = getSubscriptionCountryIso(id);
        logd("[getSubInfoRecord] id:" + id + " iccid:" + iccId + " simSlotIndex:" + simSlotIndex + " displayName:" + displayName + " nameSource:" + nameSource + " iconTint:" + iconTint + " dataRoaming:" + dataRoaming + " mcc:" + mcc + " mnc:" + mnc + " countIso:" + countryIso);
        String line1Number = this.mTelephonyManager.getLine1NumberForSubscriber(id);
        if (!(TextUtils.isEmpty(line1Number) || (line1Number.equals(number) ^ 1) == 0)) {
            number = line1Number;
        }
        return new SubInfoRecord(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso);
    }

    public List<SubInfoRecord> getSubInfoUsingSlotId(int slotId, String callingPackage) {
        return colorgetSubInfoUsingSlotIdWithCheck(slotId, true, callingPackage);
    }

    public List<SubInfoRecord> colorgetSubInfoUsingSlotIdWithCheck(int slotId, boolean needCheck, String callingPackage) {
        Throwable th;
        if (!canReadPhoneState(callingPackage, "colorgetSubInfoUsingSlotIdWithCheck")) {
            return null;
        }
        if (slotId == Integer.MAX_VALUE) {
            slotId = getSlotIndex(getDefaultSubId());
        }
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            logd("[getSubInfoUsingSlotIdWithCheck]- invalid slotId");
            return null;
        } else if (!needCheck || (isSubInfoReady() ^ 1) == 0) {
            long identity = Binder.clearCallingIdentity();
            try {
                Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", new String[]{String.valueOf(slotId)}, null);
                List<SubInfoRecord> subList = null;
                if (cursor != null) {
                    while (true) {
                        ArrayList<SubInfoRecord> subList2;
                        ArrayList<SubInfoRecord> subList3 = subList2;
                        try {
                            if (!cursor.moveToNext()) {
                                subList = subList3;
                                break;
                            }
                            SubInfoRecord subInfo = colorgetSubInfoRecord(cursor);
                            if (subInfo != null) {
                                if (subList3 == null) {
                                    subList2 = new ArrayList();
                                } else {
                                    subList2 = subList3;
                                }
                                try {
                                    subList2.add(subInfo);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } else {
                                subList2 = subList3;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            subList2 = subList3;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return subList;
            } catch (Throwable th4) {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            logd("[getSubInfoUsingSlotIdWithCheck]- not ready");
            return null;
        }
    }

    public SubInfoRecord getSubInfoForSubscriber(int subId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getSubInfoForSubscriber")) {
            return null;
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
        }
        if (SubscriptionManager.isValidSubscriptionId(subId) && (isSubInfoReady() ^ 1) == 0) {
            long identity = Binder.clearCallingIdentity();
            Cursor cursor;
            try {
                cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, InboundSmsHandler.SELECT_BY_ID, new String[]{Long.toString((long) subId)}, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        SubInfoRecord colorgetSubInfoRecord = colorgetSubInfoRecord(cursor);
                        if (cursor != null) {
                            cursor.close();
                        }
                        Binder.restoreCallingIdentity(identity);
                        return colorgetSubInfoRecord;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                logd("[getSubInfoForSubscriber]- null info return");
                Binder.restoreCallingIdentity(identity);
                return null;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            logd("[getSubInfoForSubscriberx]- invalid subId or not ready, subId = " + subId);
            return null;
        }
    }

    public static String getCarrierName(Context context, String name, String imsi, String iccid, int slotid) {
        String operatorNumic = SpnOverride.MVNO_TYPE_NONE;
        String plmn = SpnOverride.MVNO_TYPE_NONE;
        if (!TextUtils.isEmpty(name)) {
            plmn = name;
        } else if (!TextUtils.isEmpty(imsi) && imsi.length() >= 5) {
            operatorNumic = imsi.substring(0, 5);
        } else if (TextUtils.isEmpty(iccid)) {
            operatorNumic = SpnOverride.MVNO_TYPE_NONE;
        } else if (iccid.startsWith("898600") || iccid.startsWith("986800")) {
            operatorNumic = "46000";
        } else if (iccid.startsWith("898601") || iccid.startsWith("986810")) {
            operatorNumic = "46001";
        } else if (iccid.startsWith("898602")) {
            operatorNumic = "46002";
        } else if (iccid.startsWith("898603") || iccid.startsWith("986830") || iccid.startsWith("898606") || iccid.startsWith("898611")) {
            operatorNumic = "46003";
        } else if (iccid.startsWith("898520")) {
            operatorNumic = "45407";
        }
        if (!TextUtils.isEmpty(operatorNumic)) {
            plmn = getOemOperator(context, operatorNumic);
        }
        if (!TextUtils.isEmpty(iccid) && iccid.startsWith("898601234")) {
            plmn = "Test";
        }
        if (TextUtils.isEmpty(plmn)) {
            plmn = "SIM";
        }
        String mSimConfig = SystemProperties.get("persist.radio.multisim.config", SpnOverride.MVNO_TYPE_NONE);
        if (getSoftSimCardSlotIdInner() == slotid) {
            return OemConstant.getOemRes(context, "redtea_virtul_card", "SIM");
        }
        if (mSimConfig.equals("dsds") || mSimConfig.equals("dsda")) {
            return plmn + Integer.toString(slotid + 1);
        }
        return plmn;
    }

    public static String getOemOperator(Context context, String plmn) {
        if (TextUtils.isEmpty(plmn)) {
            return SpnOverride.MVNO_TYPE_NONE;
        }
        try {
            return context.getString(context.getResources().getIdentifier("mmcmnc" + plmn, "string", "oppo"));
        } catch (Exception e) {
            Rlog.d(LOG_TAG, "leon getCarrierName no res," + plmn);
            return SpnOverride.MVNO_TYPE_NONE;
        }
    }

    public boolean isCTCCard(int slotId) {
        return false;
    }

    public void setDefaultApplication(String packageName) {
        SmsApplication.setDefaultApplication(packageName, this.mContext);
    }

    public boolean isCurrentSubActive(int slotId) {
        int provisionStatus = 1;
        IExtTelephony mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
        if (this.mTelephonyManager.isMultiSimEnabled()) {
            try {
                provisionStatus = mExtTelephony.getCurrentUiccCardProvisioningStatus(slotId);
            } catch (RemoteException e) {
                provisionStatus = -1;
            } catch (NullPointerException e2) {
                provisionStatus = -1;
            }
        }
        logdl("ProvisionStatus: " + provisionStatus);
        if (provisionStatus == 1) {
            return true;
        }
        return false;
    }

    private int getActiveSlotCount() {
        List<SubscriptionInfo> subInfoList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        if (subInfoList == null) {
            logd("[getActiveSlotCount] records null");
            return 0;
        }
        int mActiveCount = 0;
        for (SubscriptionInfo subInfo : subInfoList) {
            logdl("subInfo.getSimSlotIndex:" + subInfo.getSimSlotIndex());
            if (isCurrentSubActive(subInfo.getSimSlotIndex())) {
                mActiveCount++;
            }
        }
        logdl("[getActiveSlotCount]- count: " + mActiveCount);
        return mActiveCount;
    }

    public boolean isHasSoftSimCard() {
        return getSoftSimCardSlotId() >= 0;
    }

    public int getSoftSimCardSlotId() {
        return getSoftSimCardSlotIdInner();
    }

    private void checkSoftSimCard() {
        if (SystemProperties.getInt("gsm.vsim.slotid", -1) != -1 && TextUtils.isEmpty(getSoftSimIccid(this.mContext))) {
            logd("checkSoftSimCard clear");
            SystemProperties.set("gsm.vsim.slotid", "-1");
        }
    }

    private static int getSoftSimCardSlotIdInner() {
        return SystemProperties.getInt("gsm.vsim.slotid", -1);
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0059 A:{SYNTHETIC, Splitter: B:18:0x0059} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getSoftSimIccid(Context context) {
        String[] columns = new String[]{"slot", "iccid"};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://com.redteamobile.roaming.provider"), columns, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                    }
                }
                return null;
            }
            do {
                String slot = cursor.getString(cursor.getColumnIndex("slot"));
                String iccid = cursor.getString(cursor.getColumnIndex("iccid"));
                if (!TextUtils.isEmpty(iccid)) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e2) {
                        }
                    }
                    return iccid;
                }
            } while (cursor.moveToNext());
            if (cursor != null) {
            }
            return null;
        } catch (Exception e3) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e4) {
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e5) {
                }
            }
        }
    }

    public String getExportSimDefaultName(int slotId) {
        String simName = "SIM1";
        if (ColorOSTelephonyManager.getDefault(this.mContext).isOppoSingleSimCard()) {
            return "SIM";
        }
        if (slotId == 0) {
            return "SIM1";
        }
        if (slotId == 1) {
            return "SIM2";
        }
        return "SIM1";
    }

    public int getSubState(int subId) {
        int subStatus = 0;
        int slotId = getSlotIndex(subId);
        if (slotId < 0) {
            return 0;
        }
        try {
            subStatus = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone")).getCurrentUiccCardProvisioningStatus(slotId);
        } catch (RemoteException e) {
        }
        return subStatus;
    }

    public String getOperatorNumericForData(int phoneId) {
        if (sPhones[phoneId] == null || sPhones[phoneId].mDcTracker == null) {
            return SpnOverride.MVNO_TYPE_NONE;
        }
        return sPhones[phoneId].mDcTracker.getOperatorNumeric();
    }

    public boolean getSwitchingDssState(int phoneId) {
        if (phoneId == 0) {
            return this.inSwitchingDssState1;
        }
        return this.inSwitchingDssState2;
    }

    public void setSwitchingDssState(int phoneId, boolean state) {
        if (phoneId == 0) {
            this.inSwitchingDssState1 = state;
        } else {
            this.inSwitchingDssState2 = state;
        }
    }

    private void setWfcMode() {
        if (ImsManager.isWfcEnabledByUser(this.mContext)) {
            ImsManager.setWfcMode(this.mContext, ImsManager.getWfcMode(this.mContext));
        }
    }

    private void broadcastSubInfoUpdateIntent(String slotid, String subid, String simstate) {
        Intent intent = new Intent(ACTION_SUBINFO_STATE_CHANGE);
        intent.putExtra(INTENT_KEY_SLOT_ID, slotid);
        intent.putExtra(INTENT_KEY_SUB_ID, subid);
        intent.putExtra(INTENT_KEY_SIM_STATE, simstate);
        logd("Broadcasting intent ACTION_SUBINFO_STATE_CHANGE slotid:" + slotid + " simstate:" + simstate + " subid:" + subid);
        this.mContext.sendBroadcast(intent);
    }
}
