package cm.android.mdm.manager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings.Global;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import cm.android.mdm.interfaces.INetworkManager.Carriers;
import cm.android.mdm.util.HarmonyNetUtil.HistoryColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class NetworkManager extends NetworkBaseManager {
    private static final String APN_ID = "apn_id";
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);
    private static final String PREFERRED_APN_URI = "content://telephony/carriers/preferapn";
    private static final String SOURCE_TYPE = "sourcetype";
    private static final String SUBSCRIPTION_KEY = "subscription";
    private static final String TAG = "NetworkManager";
    private final int APN_INDEX = 1;
    private final int AUTH_TYPE_INDEX = 13;
    private final int BEARER_INDEX = 19;
    private final int CARRIER_ENABLED_INDEX = 18;
    private final int CURRENT_INDEX = 17;
    private int INVALID_SUBID = -1;
    private final int MCC_INDEX = 10;
    private final int MMSC_INDEX = 9;
    private final int MMS_PORT_INDEX = 5;
    private final int MMS_PROXY_INDEX = 4;
    private final int MNC_INDEX = 11;
    private final int MVNO_MATCH_DAT_INDEX = 21;
    private final int MVNO_TYPE_INDEX = 20;
    private final int NAME_INDEX = 0;
    private final int NUMERIC_INDEX = 12;
    private final String OPPO_MULTI_SIM_NETWORK_PRIMARY_SLOT = "oppo_multi_sim_network_primary_slot";
    private final int PASSWORD_INDEX = 8;
    private final int PORT_INDEX = 3;
    private final int PROTOCOL_INDEX = 15;
    private final int PROXY_INDEX = 2;
    private final int ROAMING_PROTOCOL_INDEX = 16;
    private final int SERVER_INDEX = 6;
    private final int SOURCE_TYPE_HIDDEN = 3;
    private final int SUB_ID_INDEX = 22;
    private final int TYPE_INDEX = 14;
    private final int USER_INDEX = 7;
    private String mApn = null;
    private String mApnType = null;
    private int mAuthType = -1;
    private int mBearer = -1;
    private int mCarrierEnabled = -1;
    private Context mContext;
    private int mCurrent = -1;
    private String mMcc = null;
    private String mMmsPort = null;
    private String mMmsProxy = null;
    private String mMmsc = null;
    private String mMnc = null;
    private String mMvnoMatchData = null;
    private String mMvnoType = null;
    private String mName = null;
    private String mNumeric = null;
    private String mPassword = null;
    private int mPhoneCount = 2;
    private String mPort = null;
    private final String[] mProjection = new String[]{Carriers.NAME, Carriers.APN, Carriers.PROXY, Carriers.PORT, Carriers.MMSPROXY, Carriers.MMSPORT, Carriers.SERVER, Carriers.USER, Carriers.PASSWORD, Carriers.MMSC, Carriers.MCC, Carriers.MNC, Carriers.NUMERIC, Carriers.AUTH_TYPE, "type", Carriers.PROTOCOL, Carriers.ROAMING_PROTOCOL, Carriers.CURRENT, Carriers.CARRIER_ENABLED, Carriers.BEARER, Carriers.MVNO_TYPE, Carriers.MVNO_MATCH_DATA, Carriers.SUBSCRIPTION_ID};
    private String mProtocol = null;
    private String mProxy = null;
    private final ContentResolver mResolver;
    private String mRoamingProtocol = null;
    private String mServer = null;
    private int mSimId;
    private int mSubId = this.INVALID_SUBID;
    private int mSubscriptionId = -1;
    private final Uri[] mUri = new Uri[]{Uri.parse("content://telephony/carriers"), Uri.parse("content://telephony/carriers_sim2")};
    private String mUser = null;
    private TelephonyManager tm = null;

    public NetworkManager(Context context) {
        this.mContext = context;
        if (this.mContext != null) {
            this.mResolver = this.mContext.getContentResolver();
            this.tm = (TelephonyManager) this.mContext.getSystemService("phone");
        } else {
            this.mResolver = null;
        }
        this.mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
        initSimState();
    }

    public void addApn(Map<String, String> apnInfo) {
        Log.d(TAG, "addApn() apnInfo is " + apnInfo);
        if (this.mResolver == null || apnInfo == null) {
            Log.e(TAG, "addApn() mResolver is null");
        } else if (validate(apnInfo)) {
            getApnValues(apnInfo);
            ContentValues values = putApnValues(new ContentValues());
            Log.d(TAG, "addApn() mSubscriptionId is " + this.mSubscriptionId);
            int slotId = -1;
            if (this.mSubscriptionId != -1) {
                slotId = SubscriptionManager.getSlotIndex(this.mSubscriptionId);
            }
            if (slotId == -1) {
                slotId = getMainSlotId();
            }
            Log.d(TAG, "addApn() slotId is " + slotId);
            try {
                this.mResolver.update(this.mResolver.insert(this.mUri[slotId], new ContentValues()), values, null, null);
            } catch (Exception e) {
                Log.e(TAG, "setPreferApn: catch the exception- " + e);
            }
        }
    }

    public void removeApn(String apnId) {
        Log.d(TAG, "removeApn() apnId is " + apnId);
        if (this.mResolver == null || apnId == null) {
            Log.e(TAG, "updateApn: mResolver is null");
            return;
        }
        int id = Integer.parseInt(apnId);
        for (int i = 0; i < this.mPhoneCount; i++) {
            try {
                this.mResolver.delete(ContentUris.withAppendedId(this.mUri[i], (long) id), null, null);
            } catch (Exception e) {
                Log.e(TAG, "setPreferApn: catch the exception- " + e);
            }
        }
    }

    public void updateApn(Map<String, String> apnInfo, String apnId) {
        Log.d(TAG, "updateApn() apnInfo is " + apnInfo);
        Log.d(TAG, "updateApn() apnId is " + apnId);
        if (this.mResolver == null || apnInfo == null || apnId == null) {
            Log.e(TAG, "updateApn() mResolver is null");
        } else if (validate(apnInfo)) {
            getApnValues(apnInfo);
            ContentValues values = putApnValues(new ContentValues());
            String condition = "_id=\"" + apnId + "\"";
            for (int i = 0; i < this.mPhoneCount; i++) {
                try {
                    this.mResolver.update(this.mUri[i], values, condition, null);
                } catch (Exception e) {
                    Log.e(TAG, "setPreferApn: catch the exception- " + e);
                }
            }
        }
    }

    public void setPreferApn(String apnId) {
        Log.d(TAG, "setPreferApn() apnId is " + apnId);
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(APN_ID, apnId);
        try {
            resolver.update(getUri(PREFERAPN_URI), values, null, null);
        } catch (Exception e) {
            Log.e(TAG, "setPreferApn: catch the exception- " + e);
        }
    }

    public List<String> queryApn(Map<String, String> apnInfo) {
        Log.d(TAG, "queryApn() apnInfo is " + apnInfo);
        List<String> ids = new ArrayList();
        if (this.mResolver == null || apnInfo == null) {
            Log.e(TAG, "mResolver is null.");
            return ids;
        }
        String condition = getCondition(apnInfo);
        Log.d(TAG, "getApnInfo() condition is " + condition);
        for (int i = 0; i < this.mPhoneCount; i++) {
            Cursor cursor = this.mResolver.query(this.mUri[i], new String[]{HistoryColumn._ID}, condition, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                Log.d(TAG, "cursor query is null.");
                if (i != 0) {
                    return ids;
                }
            } else {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int id = cursor.getInt(0);
                    Log.d(TAG, "id is " + id);
                    ids.add(String.valueOf(id));
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }
        return ids;
    }

    public Map<String, String> getApnInfo(String apnId) {
        Log.d(TAG, "getApnInfo() apnId is " + apnId);
        if (this.mResolver == null || apnId == null) {
            Log.e(TAG, "mResolver is null.");
            return null;
        }
        Cursor cursor = this.mResolver.query(this.mUri[getMainSlotId()], this.mProjection, "_id=\"" + apnId + "\"", null, null);
        if (cursor == null || cursor.getCount() == 0) {
            Log.d(TAG, "cursor query is null.");
            return null;
        }
        cursor.moveToFirst();
        getApnValuesFromCursor(cursor);
        cursor.close();
        return createApnInfo();
    }

    private Uri getUri(Uri uri) {
        return Uri.withAppendedPath(uri, "/subId/" + this.mSubId);
    }

    private void initSimState() {
        this.mSimId = getMainSlotId();
        int[] subId = SubscriptionManager.getSubId(this.mSimId);
        if (subId != null) {
            this.mSubId = subId[0];
        }
        Log.d(TAG, "GEMINI_SIM_ID_KEY = " + this.mSimId + ", subId = " + this.mSubId);
    }

    private static int getDefaultDataSubscription(Context context) {
        int subscription = 0;
        try {
            ColorOSTelephonyManager.getDefault(context);
            return ColorOSTelephonyManager.colorgetDefaultDataPhoneId(context);
        } catch (Exception e) {
            return subscription;
        }
    }

    private boolean validate(Map<String, String> apnInfo) {
        if (TextUtils.isEmpty((CharSequence) apnInfo.get(Carriers.NAME))) {
            Log.e(TAG, "name is null");
            return false;
        } else if (TextUtils.isEmpty((CharSequence) apnInfo.get(Carriers.APN))) {
            Log.e(TAG, "apn is null");
            return false;
        } else if (!TextUtils.isEmpty((CharSequence) apnInfo.get(Carriers.NUMERIC))) {
            return true;
        } else {
            Log.e(TAG, "numeric is null");
            return false;
        }
    }

    private int getMainSlotId() {
        return Global.getInt(this.mResolver, "oppo_multi_sim_network_primary_slot", 0);
    }

    private String getCondition(Map<String, String> apnInfo) {
        getApnValues(apnInfo);
        String where = "name=\"" + this.mName + "\"";
        if (!TextUtils.isEmpty(this.mApn)) {
            where = where + " AND apn=\"" + this.mApn + "\"";
        }
        if (!TextUtils.isEmpty(this.mProxy)) {
            where = where + " AND proxy=\"" + this.mProxy + "\"";
        }
        if (!TextUtils.isEmpty(this.mPort)) {
            where = where + " AND port=\"" + this.mPort + "\"";
        }
        if (!TextUtils.isEmpty(this.mMmsProxy)) {
            where = where + " AND mmsproxy=\"" + this.mMmsProxy + "\"";
        }
        if (!TextUtils.isEmpty(this.mMmsPort)) {
            where = where + " AND mmsport=\"" + this.mMmsPort + "\"";
        }
        if (!TextUtils.isEmpty(this.mServer)) {
            where = where + " AND server=\"" + this.mServer + "\"";
        }
        if (!TextUtils.isEmpty(this.mUser)) {
            where = where + " AND user=\"" + this.mUser + "\"";
        }
        if (!TextUtils.isEmpty(this.mPassword)) {
            where = where + " AND password=\"" + this.mPassword + "\"";
        }
        if (!TextUtils.isEmpty(this.mMmsc)) {
            where = where + " AND mmsc=\"" + this.mMmsc + "\"";
        }
        if (!TextUtils.isEmpty(this.mMcc)) {
            where = where + " AND mcc=\"" + this.mMcc + "\"";
        }
        if (!TextUtils.isEmpty(this.mMnc)) {
            where = where + " AND mnc=\"" + this.mMnc + "\"";
        }
        if (!TextUtils.isEmpty(this.mNumeric)) {
            where = where + " AND numeric=\"" + this.mNumeric + "\"";
        }
        if (this.mAuthType != -1) {
            where = where + " AND authtype=\"" + this.mAuthType + "\"";
        }
        if (!TextUtils.isEmpty(this.mApnType)) {
            where = where + " AND type=\"" + this.mApnType + "\"";
        }
        if (!TextUtils.isEmpty(this.mProtocol)) {
            where = where + " AND protocol=\"" + this.mProtocol + "\"";
        }
        if (!TextUtils.isEmpty(this.mRoamingProtocol)) {
            where = where + " AND roaming_protocol=\"" + this.mRoamingProtocol + "\"";
        }
        if (this.mCurrent != -1) {
            where = where + " AND current=\"" + this.mCurrent + "\"";
        }
        if (this.mCarrierEnabled != -1) {
            where = where + " AND carrier_enabled=\"" + this.mCarrierEnabled + "\"";
        }
        if (this.mBearer != -1) {
            where = where + " AND bearer=\"" + this.mBearer + "\"";
        }
        if (!TextUtils.isEmpty(this.mMvnoType)) {
            where = where + " AND mvno_type=\"" + this.mMvnoType + "\"";
        }
        if (!TextUtils.isEmpty(this.mMvnoMatchData)) {
            where = where + " AND mvno_match_data=\"" + this.mMvnoMatchData + "\"";
        }
        if (this.mSubscriptionId != -1) {
            return where + " AND sub_id=\"" + this.mSubscriptionId + "\"";
        }
        return where;
    }

    private void getApnValues(Map<String, String> apnInfo) {
        this.mName = (String) apnInfo.get(Carriers.NAME);
        this.mApn = (String) apnInfo.get(Carriers.APN);
        this.mProxy = (String) apnInfo.get(Carriers.PROXY);
        this.mPort = (String) apnInfo.get(Carriers.PORT);
        this.mMmsProxy = (String) apnInfo.get(Carriers.MMSPROXY);
        this.mMmsPort = (String) apnInfo.get(Carriers.MMSPORT);
        this.mServer = (String) apnInfo.get(Carriers.SERVER);
        this.mUser = (String) apnInfo.get(Carriers.USER);
        this.mPassword = (String) apnInfo.get(Carriers.PASSWORD);
        this.mMmsc = (String) apnInfo.get(Carriers.MMSC);
        this.mMcc = (String) apnInfo.get(Carriers.MCC);
        this.mMnc = (String) apnInfo.get(Carriers.MNC);
        this.mNumeric = (String) apnInfo.get(Carriers.NUMERIC);
        String authType = (String) apnInfo.get(Carriers.AUTH_TYPE);
        if (authType != null) {
            this.mAuthType = Integer.parseInt(authType);
        }
        this.mApnType = (String) apnInfo.get("type");
        this.mProtocol = (String) apnInfo.get(Carriers.PROTOCOL);
        this.mRoamingProtocol = (String) apnInfo.get(Carriers.ROAMING_PROTOCOL);
        String current = (String) apnInfo.get(Carriers.CURRENT);
        if (current != null) {
            this.mCurrent = Integer.parseInt(current);
        }
        String carrierEnabled = (String) apnInfo.get(Carriers.CARRIER_ENABLED);
        if (carrierEnabled != null) {
            this.mCarrierEnabled = Integer.parseInt(carrierEnabled);
        }
        String bearer = (String) apnInfo.get(Carriers.BEARER);
        if (bearer != null) {
            this.mBearer = Integer.parseInt(bearer);
        }
        this.mMvnoType = (String) apnInfo.get(Carriers.MVNO_TYPE);
        this.mMvnoMatchData = (String) apnInfo.get(Carriers.MVNO_MATCH_DATA);
        String subscriptionId = (String) apnInfo.get(Carriers.SUBSCRIPTION_ID);
        if (subscriptionId != null) {
            this.mSubscriptionId = Integer.parseInt(subscriptionId);
        }
    }

    private void getApnValuesFromCursor(Cursor cursor) {
        this.mName = cursor.getString(0);
        this.mApn = cursor.getString(1);
        this.mProxy = cursor.getString(2);
        this.mPort = cursor.getString(3);
        this.mMmsProxy = cursor.getString(4);
        this.mMmsPort = cursor.getString(5);
        this.mServer = cursor.getString(6);
        this.mUser = cursor.getString(7);
        this.mPassword = cursor.getString(8);
        this.mMmsc = cursor.getString(9);
        this.mMcc = cursor.getString(10);
        this.mMnc = cursor.getString(11);
        this.mNumeric = cursor.getString(12);
        this.mAuthType = cursor.getInt(13);
        this.mApnType = cursor.getString(14);
        this.mProtocol = cursor.getString(15);
        this.mRoamingProtocol = cursor.getString(16);
        this.mCurrent = cursor.getInt(17);
        this.mCarrierEnabled = cursor.getInt(18);
        this.mBearer = cursor.getInt(19);
        this.mMvnoType = cursor.getString(20);
        this.mMvnoMatchData = cursor.getString(21);
        this.mSubscriptionId = cursor.getInt(22);
    }

    private void resetApnValues() {
        this.mName = null;
        this.mApn = null;
        this.mProxy = null;
        this.mPort = null;
        this.mMmsProxy = null;
        this.mMmsPort = null;
        this.mServer = null;
        this.mUser = null;
        this.mPassword = null;
        this.mMmsc = null;
        this.mMcc = null;
        this.mMnc = null;
        this.mNumeric = null;
        this.mAuthType = -1;
        this.mApnType = null;
        this.mProtocol = null;
        this.mRoamingProtocol = null;
        this.mCurrent = -1;
        this.mCarrierEnabled = -1;
        this.mBearer = -1;
        this.mMvnoType = null;
        this.mMvnoMatchData = null;
        this.mSubscriptionId = -1;
    }

    private ContentValues putApnValues(ContentValues values) {
        values.put(Carriers.NAME, this.mName);
        values.put(Carriers.APN, this.mApn);
        if (this.mProxy != null) {
            values.put(Carriers.PROXY, this.mProxy);
        }
        if (this.mPort != null) {
            values.put(Carriers.PORT, this.mPort);
        }
        if (this.mMmsProxy != null) {
            values.put(Carriers.MMSPROXY, this.mMmsProxy);
        }
        if (this.mMmsPort != null) {
            values.put(Carriers.PORT, this.mMmsPort);
        }
        if (this.mServer != null) {
            values.put(Carriers.SERVER, this.mServer);
        }
        if (this.mUser != null) {
            values.put(Carriers.USER, this.mUser);
        }
        if (this.mPassword != null) {
            values.put(Carriers.PASSWORD, this.mPassword);
        }
        if (this.mMmsc != null) {
            values.put(Carriers.MMSC, this.mMmsc);
        }
        values.put(Carriers.MCC, this.mMcc);
        values.put(Carriers.MNC, this.mMnc);
        if (this.mNumeric != null) {
            values.put(Carriers.NUMERIC, this.mNumeric);
        }
        if (this.mAuthType != -1) {
            values.put(Carriers.AUTH_TYPE, Integer.valueOf(this.mAuthType));
        }
        if (this.mApnType != null) {
            values.put("type", this.mApnType);
        }
        if (this.mProtocol != null) {
            values.put(Carriers.PROTOCOL, this.mProtocol);
        }
        if (this.mRoamingProtocol != null) {
            values.put(Carriers.ROAMING_PROTOCOL, this.mRoamingProtocol);
        }
        if (this.mCurrent != -1) {
            values.put(Carriers.CURRENT, Integer.valueOf(this.mCurrent));
        }
        if (this.mCarrierEnabled != -1) {
            values.put(Carriers.CARRIER_ENABLED, Integer.valueOf(this.mCarrierEnabled));
        }
        if (this.mBearer != -1) {
            values.put(Carriers.BEARER, Integer.valueOf(this.mBearer));
        }
        if (this.mMvnoType != null) {
            values.put(Carriers.MVNO_TYPE, this.mMvnoType);
        }
        if (this.mMvnoMatchData != null) {
            values.put(Carriers.MVNO_MATCH_DATA, this.mMvnoMatchData);
        }
        if (this.mSubscriptionId != -1) {
            values.put(Carriers.SUBSCRIPTION_ID, Integer.valueOf(this.mSubscriptionId));
        }
        return values;
    }

    private Map<String, String> createApnInfo() {
        WeakHashMap<String, String> apnInfo = new WeakHashMap();
        apnInfo.put(Carriers.NAME, this.mName);
        apnInfo.put(Carriers.APN, this.mApn);
        apnInfo.put(Carriers.PROXY, this.mProxy);
        apnInfo.put(Carriers.PORT, this.mPort);
        apnInfo.put(Carriers.MMSPROXY, this.mMmsProxy);
        apnInfo.put(Carriers.MMSPORT, this.mMmsPort);
        apnInfo.put(Carriers.SERVER, this.mServer);
        apnInfo.put(Carriers.USER, this.mUser);
        apnInfo.put(Carriers.PASSWORD, this.mPassword);
        apnInfo.put(Carriers.MMSC, this.mMmsc);
        apnInfo.put(Carriers.MCC, this.mMcc);
        apnInfo.put(Carriers.MNC, this.mMnc);
        apnInfo.put(Carriers.NUMERIC, this.mNumeric);
        apnInfo.put(Carriers.AUTH_TYPE, String.valueOf(this.mAuthType));
        apnInfo.put("type", this.mApnType);
        apnInfo.put(Carriers.PROTOCOL, this.mProtocol);
        apnInfo.put(Carriers.ROAMING_PROTOCOL, this.mRoamingProtocol);
        apnInfo.put(Carriers.CURRENT, String.valueOf(this.mCurrent));
        apnInfo.put(Carriers.CARRIER_ENABLED, String.valueOf(this.mCarrierEnabled));
        apnInfo.put(Carriers.BEARER, String.valueOf(this.mBearer));
        apnInfo.put(Carriers.MVNO_TYPE, this.mMvnoType);
        apnInfo.put(Carriers.MVNO_MATCH_DATA, this.mMvnoMatchData);
        apnInfo.put(Carriers.SUBSCRIPTION_ID, String.valueOf(this.mSubscriptionId));
        return apnInfo;
    }
}
