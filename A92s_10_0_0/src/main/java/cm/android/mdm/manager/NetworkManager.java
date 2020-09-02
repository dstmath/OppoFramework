package cm.android.mdm.manager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import cm.android.mdm.interfaces.INetworkManager;
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
    private final String[] mProjection = {INetworkManager.Carriers.NAME, INetworkManager.Carriers.APN, INetworkManager.Carriers.PROXY, INetworkManager.Carriers.PORT, INetworkManager.Carriers.MMSPROXY, INetworkManager.Carriers.MMSPORT, INetworkManager.Carriers.SERVER, INetworkManager.Carriers.USER, INetworkManager.Carriers.PASSWORD, INetworkManager.Carriers.MMSC, INetworkManager.Carriers.MCC, INetworkManager.Carriers.MNC, INetworkManager.Carriers.NUMERIC, INetworkManager.Carriers.AUTH_TYPE, "type", INetworkManager.Carriers.PROTOCOL, INetworkManager.Carriers.ROAMING_PROTOCOL, INetworkManager.Carriers.CURRENT, INetworkManager.Carriers.CARRIER_ENABLED, INetworkManager.Carriers.BEARER, INetworkManager.Carriers.MVNO_TYPE, INetworkManager.Carriers.MVNO_MATCH_DATA, INetworkManager.Carriers.SUBSCRIPTION_ID};
    private String mProtocol = null;
    private String mProxy = null;
    private final ContentResolver mResolver;
    private String mRoamingProtocol = null;
    private String mServer = null;
    private int mSimId;
    private int mSubId = this.INVALID_SUBID;
    private int mSubscriptionId = -1;
    private final Uri[] mUri = {Uri.parse("content://telephony/carriers"), Uri.parse("content://telephony/carriers_sim2")};
    private String mUser = null;
    private TelephonyManager tm = null;

    public NetworkManager(Context context) {
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mResolver = context2.getContentResolver();
            this.tm = (TelephonyManager) this.mContext.getSystemService("phone");
        } else {
            this.mResolver = null;
        }
        this.mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
        initSimState();
    }

    @Override // cm.android.mdm.manager.NetworkBaseManager, cm.android.mdm.interfaces.INetworkManager
    public void addApn(Map<String, String> apnInfo) {
        Log.d(TAG, "addApn() apnInfo is " + apnInfo);
        if (this.mResolver == null || apnInfo == null) {
            Log.e(TAG, "addApn() mResolver is null");
        } else if (validate(apnInfo)) {
            getApnValues(apnInfo);
            ContentValues values = putApnValues(new ContentValues());
            Log.d(TAG, "addApn() mSubscriptionId is " + this.mSubscriptionId);
            int slotId = -1;
            int i = this.mSubscriptionId;
            if (i != -1) {
                slotId = SubscriptionManager.getSlotIndex(i);
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

    @Override // cm.android.mdm.manager.NetworkBaseManager, cm.android.mdm.interfaces.INetworkManager
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

    @Override // cm.android.mdm.manager.NetworkBaseManager, cm.android.mdm.interfaces.INetworkManager
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

    @Override // cm.android.mdm.manager.NetworkBaseManager, cm.android.mdm.interfaces.INetworkManager
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

    @Override // cm.android.mdm.manager.NetworkBaseManager, cm.android.mdm.interfaces.INetworkManager
    public List<String> queryApn(Map<String, String> apnInfo) {
        Log.d(TAG, "queryApn() apnInfo is " + apnInfo);
        List<String> ids = new ArrayList<>();
        if (this.mResolver == null || apnInfo == null) {
            Log.e(TAG, "mResolver is null.");
            return ids;
        }
        String condition = getCondition(apnInfo);
        Log.d(TAG, "getApnInfo() condition is " + condition);
        for (int i = 0; i < this.mPhoneCount; i++) {
            Cursor cursor = this.mResolver.query(this.mUri[i], new String[]{"_id"}, condition, null, null);
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

    @Override // cm.android.mdm.manager.NetworkBaseManager, cm.android.mdm.interfaces.INetworkManager
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
        try {
            ColorOSTelephonyManager.getDefault(context);
            return ColorOSTelephonyManager.colorgetDefaultDataPhoneId(context);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean validate(Map<String, String> apnInfo) {
        if (TextUtils.isEmpty(apnInfo.get(INetworkManager.Carriers.NAME))) {
            Log.e(TAG, "name is null");
            return false;
        } else if (TextUtils.isEmpty(apnInfo.get(INetworkManager.Carriers.APN))) {
            Log.e(TAG, "apn is null");
            return false;
        } else if (!TextUtils.isEmpty(apnInfo.get(INetworkManager.Carriers.NUMERIC))) {
            return true;
        } else {
            Log.e(TAG, "numeric is null");
            return false;
        }
    }

    private int getMainSlotId() {
        return Settings.Global.getInt(this.mResolver, "oppo_multi_sim_network_primary_slot", 0);
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
        if (this.mSubscriptionId == -1) {
            return where;
        }
        return where + " AND sub_id=\"" + this.mSubscriptionId + "\"";
    }

    private void getApnValues(Map<String, String> apnInfo) {
        this.mName = apnInfo.get(INetworkManager.Carriers.NAME);
        this.mApn = apnInfo.get(INetworkManager.Carriers.APN);
        this.mProxy = apnInfo.get(INetworkManager.Carriers.PROXY);
        this.mPort = apnInfo.get(INetworkManager.Carriers.PORT);
        this.mMmsProxy = apnInfo.get(INetworkManager.Carriers.MMSPROXY);
        this.mMmsPort = apnInfo.get(INetworkManager.Carriers.MMSPORT);
        this.mServer = apnInfo.get(INetworkManager.Carriers.SERVER);
        this.mUser = apnInfo.get(INetworkManager.Carriers.USER);
        this.mPassword = apnInfo.get(INetworkManager.Carriers.PASSWORD);
        this.mMmsc = apnInfo.get(INetworkManager.Carriers.MMSC);
        this.mMcc = apnInfo.get(INetworkManager.Carriers.MCC);
        this.mMnc = apnInfo.get(INetworkManager.Carriers.MNC);
        this.mNumeric = apnInfo.get(INetworkManager.Carriers.NUMERIC);
        String authType = apnInfo.get(INetworkManager.Carriers.AUTH_TYPE);
        if (authType != null) {
            this.mAuthType = Integer.parseInt(authType);
        }
        this.mApnType = apnInfo.get("type");
        this.mProtocol = apnInfo.get(INetworkManager.Carriers.PROTOCOL);
        this.mRoamingProtocol = apnInfo.get(INetworkManager.Carriers.ROAMING_PROTOCOL);
        String current = apnInfo.get(INetworkManager.Carriers.CURRENT);
        if (current != null) {
            this.mCurrent = Integer.parseInt(current);
        }
        String carrierEnabled = apnInfo.get(INetworkManager.Carriers.CARRIER_ENABLED);
        if (carrierEnabled != null) {
            this.mCarrierEnabled = Integer.parseInt(carrierEnabled);
        }
        String bearer = apnInfo.get(INetworkManager.Carriers.BEARER);
        if (bearer != null) {
            this.mBearer = Integer.parseInt(bearer);
        }
        this.mMvnoType = apnInfo.get(INetworkManager.Carriers.MVNO_TYPE);
        this.mMvnoMatchData = apnInfo.get(INetworkManager.Carriers.MVNO_MATCH_DATA);
        String subscriptionId = apnInfo.get(INetworkManager.Carriers.SUBSCRIPTION_ID);
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
        values.put(INetworkManager.Carriers.NAME, this.mName);
        values.put(INetworkManager.Carriers.APN, this.mApn);
        String str = this.mProxy;
        if (str != null) {
            values.put(INetworkManager.Carriers.PROXY, str);
        }
        String str2 = this.mPort;
        if (str2 != null) {
            values.put(INetworkManager.Carriers.PORT, str2);
        }
        String str3 = this.mMmsProxy;
        if (str3 != null) {
            values.put(INetworkManager.Carriers.MMSPROXY, str3);
        }
        String str4 = this.mMmsPort;
        if (str4 != null) {
            values.put(INetworkManager.Carriers.PORT, str4);
        }
        String str5 = this.mServer;
        if (str5 != null) {
            values.put(INetworkManager.Carriers.SERVER, str5);
        }
        String str6 = this.mUser;
        if (str6 != null) {
            values.put(INetworkManager.Carriers.USER, str6);
        }
        String str7 = this.mPassword;
        if (str7 != null) {
            values.put(INetworkManager.Carriers.PASSWORD, str7);
        }
        String str8 = this.mMmsc;
        if (str8 != null) {
            values.put(INetworkManager.Carriers.MMSC, str8);
        }
        values.put(INetworkManager.Carriers.MCC, this.mMcc);
        values.put(INetworkManager.Carriers.MNC, this.mMnc);
        String str9 = this.mNumeric;
        if (str9 != null) {
            values.put(INetworkManager.Carriers.NUMERIC, str9);
        }
        int i = this.mAuthType;
        if (i != -1) {
            values.put(INetworkManager.Carriers.AUTH_TYPE, Integer.valueOf(i));
        }
        String str10 = this.mApnType;
        if (str10 != null) {
            values.put("type", str10);
        }
        String str11 = this.mProtocol;
        if (str11 != null) {
            values.put(INetworkManager.Carriers.PROTOCOL, str11);
        }
        String str12 = this.mRoamingProtocol;
        if (str12 != null) {
            values.put(INetworkManager.Carriers.ROAMING_PROTOCOL, str12);
        }
        int i2 = this.mCurrent;
        if (i2 != -1) {
            values.put(INetworkManager.Carriers.CURRENT, Integer.valueOf(i2));
        }
        int i3 = this.mCarrierEnabled;
        if (i3 != -1) {
            values.put(INetworkManager.Carriers.CARRIER_ENABLED, Integer.valueOf(i3));
        }
        int i4 = this.mBearer;
        if (i4 != -1) {
            values.put(INetworkManager.Carriers.BEARER, Integer.valueOf(i4));
        }
        String str13 = this.mMvnoType;
        if (str13 != null) {
            values.put(INetworkManager.Carriers.MVNO_TYPE, str13);
        }
        String str14 = this.mMvnoMatchData;
        if (str14 != null) {
            values.put(INetworkManager.Carriers.MVNO_MATCH_DATA, str14);
        }
        int i5 = this.mSubscriptionId;
        if (i5 != -1) {
            values.put(INetworkManager.Carriers.SUBSCRIPTION_ID, Integer.valueOf(i5));
        }
        return values;
    }

    private Map<String, String> createApnInfo() {
        WeakHashMap<String, String> apnInfo = new WeakHashMap<>();
        apnInfo.put(INetworkManager.Carriers.NAME, this.mName);
        apnInfo.put(INetworkManager.Carriers.APN, this.mApn);
        apnInfo.put(INetworkManager.Carriers.PROXY, this.mProxy);
        apnInfo.put(INetworkManager.Carriers.PORT, this.mPort);
        apnInfo.put(INetworkManager.Carriers.MMSPROXY, this.mMmsProxy);
        apnInfo.put(INetworkManager.Carriers.MMSPORT, this.mMmsPort);
        apnInfo.put(INetworkManager.Carriers.SERVER, this.mServer);
        apnInfo.put(INetworkManager.Carriers.USER, this.mUser);
        apnInfo.put(INetworkManager.Carriers.PASSWORD, this.mPassword);
        apnInfo.put(INetworkManager.Carriers.MMSC, this.mMmsc);
        apnInfo.put(INetworkManager.Carriers.MCC, this.mMcc);
        apnInfo.put(INetworkManager.Carriers.MNC, this.mMnc);
        apnInfo.put(INetworkManager.Carriers.NUMERIC, this.mNumeric);
        apnInfo.put(INetworkManager.Carriers.AUTH_TYPE, String.valueOf(this.mAuthType));
        apnInfo.put("type", this.mApnType);
        apnInfo.put(INetworkManager.Carriers.PROTOCOL, this.mProtocol);
        apnInfo.put(INetworkManager.Carriers.ROAMING_PROTOCOL, this.mRoamingProtocol);
        apnInfo.put(INetworkManager.Carriers.CURRENT, String.valueOf(this.mCurrent));
        apnInfo.put(INetworkManager.Carriers.CARRIER_ENABLED, String.valueOf(this.mCarrierEnabled));
        apnInfo.put(INetworkManager.Carriers.BEARER, String.valueOf(this.mBearer));
        apnInfo.put(INetworkManager.Carriers.MVNO_TYPE, this.mMvnoType);
        apnInfo.put(INetworkManager.Carriers.MVNO_MATCH_DATA, this.mMvnoMatchData);
        apnInfo.put(INetworkManager.Carriers.SUBSCRIPTION_ID, String.valueOf(this.mSubscriptionId));
        return apnInfo;
    }
}
