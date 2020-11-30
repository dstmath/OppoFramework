package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.IOppoCustomizeService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class DeviceNetworkManagerImpl extends IDeviceNetworkManager.Stub {
    private static final Uri PREFERAPN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static final Uri RESTORE_URI = Uri.parse("content://telephony/carriers/restore");
    private final int APN_INDEX = 1;
    private final int APN_MODE_DISABLE = 0;
    private final int APN_MODE_EDIT = 2;
    private final int APN_MODE_ONLY_READ = 1;
    private final int AUTH_TYPE_INDEX = 13;
    private final int BEARER_INDEX = 19;
    private final int BLACK_LIST = 1;
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
    private final int NORMAL = 0;
    private final int NUMERIC_INDEX = 12;
    private final String OPPO_MULTI_SIM_NETWORK_PRIMARY_SLOT = "oppo_multi_sim_network_primary_slot";
    private final int PASSWORD_INDEX = 8;
    private final int PORT_INDEX = 3;
    private final String PROP_APN_MODE = "persist.sys.apn.mode";
    private final int PROTOCOL_INDEX = 15;
    private final int PROXY_INDEX = 2;
    private final int ROAMING_PROTOCOL_INDEX = 16;
    private final int SERVER_INDEX = 6;
    private final int SUB_ID_INDEX = 22;
    private final int TYPE_INDEX = 14;
    private final int USER_INDEX = 7;
    private final int WHITE_LIST = 2;
    private String mApn = null;
    private String mApnType = null;
    private int mAuthType = -1;
    private int mBearer = -1;
    private int mCarrierEnabled = -1;
    private Context mContext;
    private int mCurrent = -1;
    private IOppoCustomizeService mCustService;
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
    private final String[] mProjection = {"name", "apn", "proxy", "port", "mmsproxy", "mmsport", "server", "user", "password", "mmsc", "mcc", "mnc", "numeric", "authtype", "type", "protocol", "roaming_protocol", "current", "carrier_enabled", "bearer", "mvno_type", "mvno_match_data", "sub_id"};
    private String mProtocol = null;
    private String mProxy = null;
    private ContentResolver mResolver;
    private String mRoamingProtocol = null;
    private String mServer = null;
    private int mSimId;
    private int mSubId = this.INVALID_SUBID;
    private int mSubscriptionId = -1;
    private final Uri[] mUri = {Uri.parse("content://telephony/carriers"), Uri.parse("content://telephony/carriers_sim2")};
    private String mUser = null;
    private TelephonyManager tm = null;

    public DeviceNetworkManagerImpl(Context context) {
        this.mContext = context;
        if (this.mContext != null) {
            this.mResolver = this.mContext.getContentResolver();
            this.tm = (TelephonyManager) this.mContext.getSystemService("phone");
        } else {
            this.mResolver = null;
        }
        this.mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
        this.mCustService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
        initSimState();
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void addApn(ComponentName componentName, Map apnInfo) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "addApn() apnInfo is " + apnInfo);
        if (this.mResolver == null || apnInfo == null) {
            Log.e("DeviceNetworkManagerImpl", "addApn() mResolver is null");
        } else if (validate(apnInfo)) {
            getApnValues(apnInfo);
            ContentValues values = putApnValues(new ContentValues());
            Log.d("DeviceNetworkManagerImpl", "addApn() mSubscriptionId is " + this.mSubscriptionId);
            int slotId = -1;
            if (this.mSubscriptionId != -1) {
                slotId = SubscriptionManager.getSlotIndex(this.mSubscriptionId);
            }
            if (slotId == -1) {
                slotId = getMainSlotId();
            }
            Log.d("DeviceNetworkManagerImpl", "addApn() slotId is " + slotId);
            try {
                this.mResolver.update(this.mResolver.insert(this.mUri[slotId], new ContentValues()), values, null, null);
            } catch (Exception e) {
                Log.e("DeviceNetworkManagerImpl", "addApn: catch the exception- " + e);
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void removeApn(ComponentName componentName, String apnId) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "removeApn() apnId is " + apnId);
        if (this.mResolver == null || apnId == null) {
            Log.e("DeviceNetworkManagerImpl", "removeApn: mResolver is null");
            return;
        }
        try {
            this.mResolver.delete(ContentUris.withAppendedId(this.mUri[0], (long) Integer.parseInt(apnId)), null, null);
        } catch (Exception e) {
            Log.e("DeviceNetworkManagerImpl", "removeApn: catch the exception- " + e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void updateApn(ComponentName componentName, Map apnInfo, String apnId) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "updateApn() apnInfo is " + apnInfo);
        Log.d("DeviceNetworkManagerImpl", "updateApn() apnId is " + apnId);
        if (this.mResolver == null || apnInfo == null || apnId == null) {
            Log.e("DeviceNetworkManagerImpl", "updateApn() mResolver is null");
        } else if (validate(apnInfo)) {
            getApnValues(apnInfo);
            ContentValues values = putApnValues(new ContentValues());
            try {
                this.mResolver.update(this.mUri[0], values, "_id=\"" + apnId + "\"", null);
            } catch (Exception e) {
                Log.e("DeviceNetworkManagerImpl", "updateApn: catch the exception- " + e);
            }
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public List queryApn(ComponentName componentName, Map apnInfo) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "queryApn() apnInfo is " + apnInfo);
        List<String> ids = new ArrayList<>();
        if (this.mResolver == null || apnInfo == null) {
            Log.e("DeviceNetworkManagerImpl", "queryApn mResolver is null.");
            return ids;
        }
        String condition = getCondition(apnInfo);
        Log.d("DeviceNetworkManagerImpl", "getApnInfo() condition is " + condition);
        Cursor cursor = this.mResolver.query(this.mUri[0], new String[]{"_id"}, condition, null, null);
        if (cursor == null) {
            Log.d("DeviceNetworkManagerImpl", "cursor query is null.");
            return ids;
        } else if (cursor.getCount() == 0) {
            Log.d("DeviceNetworkManagerImpl", "cursor query is null.");
            cursor.close();
            return ids;
        } else {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int id = cursor.getInt(0);
                Log.d("DeviceNetworkManagerImpl", "id is " + id);
                ids.add(String.valueOf(id));
                cursor.moveToNext();
            }
            cursor.close();
            return ids;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public Map getApnInfo(ComponentName componentName, String apnId) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "getApnInfo() apnId is " + apnId);
        if (this.mResolver == null || apnId == null) {
            Log.e("DeviceNetworkManagerImpl", "getApnInfo mResolver is null.");
            return null;
        }
        Cursor cursor = this.mResolver.query(this.mUri[getMainSlotId()], this.mProjection, "_id=\"" + apnId + "\"", null, null);
        if (cursor == null) {
            Log.d("DeviceNetworkManagerImpl", "cursor query is null.");
            return null;
        } else if (cursor.getCount() == 0) {
            Log.d("DeviceNetworkManagerImpl", "cursor query is null.");
            cursor.close();
            return null;
        } else {
            cursor.moveToFirst();
            getApnValuesFromCursor(cursor);
            cursor.close();
            return createApnInfo();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void setPreferApn(ComponentName componentName, String apnId) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "setPreferApn() apnId is " + apnId);
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", apnId);
        try {
            resolver.update(getUri(PREFERAPN_URI), values, null, null);
        } catch (Exception e) {
            Log.e("DeviceNetworkManagerImpl", "setPreferApn: catch the exception- " + e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void setAccessPointNameDisabled(ComponentName componentName, int mode) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "setAccessPointNameDisabled() mode is " + mode);
        switch (mode) {
            case 0:
            case 1:
            case 2:
                SystemProperties.set("persist.sys.apn.mode", mode + "");
                return;
            default:
                Log.e("DeviceNetworkManagerImpl", "Error! Invalid mode : " + mode);
                return;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public int getUserApnMgrPolicies() {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "getUserApnMgrPolicies()");
        return SystemProperties.getInt("persist.sys.apn.mode", 2);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void activeCTAPN(ComponentName arg0, String apnId) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "activeCTAPN()");
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", apnId);
        try {
            resolver.update(getUri(PREFERAPN_URI), values, null, null);
        } catch (Exception e) {
            Log.e("DeviceNetworkManagerImpl", "setPreferApn: catch the exception- " + e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public String getCurrentApn() {
        PermissionManager.getInstance().checkPermission();
        String key = null;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(getUri(PREFERAPN_URI), new String[]{"_id"}, null, null, "name ASC");
        } catch (Exception e) {
            Log.e("DeviceNetworkManagerImpl", "getPreferApn: catch the exception- " + e);
        }
        if (cursor == null) {
            Log.e("DeviceNetworkManagerImpl", "getPreferApn: cursor is null");
            return null;
        }
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(0);
        }
        cursor.close();
        return key;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void restoreDefaultApnOnSystem() {
        PermissionManager.getInstance().checkPermission();
        try {
            this.mContext.getContentResolver().delete(getUri(RESTORE_URI), null, null);
        } catch (Exception e) {
            Log.e("DeviceNetworkManagerImpl", "RestoreApnProcessHandler: catch the exception- " + e);
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public String getOperatorNumeric() {
        PermissionManager.getInstance().checkPermission();
        if (this.tm != null) {
            return this.tm.getSimOperator(this.mSubId);
        }
        Log.e("DeviceNetworkManagerImpl", "TELEPHONY_SERVICE is null");
        return "";
    }

    private void initSimState() {
        this.mSimId = getMainSlotId();
        int[] subId = SubscriptionManager.getSubId(this.mSimId);
        if (subId != null) {
            this.mSubId = subId[0];
        }
        Log.d("DeviceNetworkManagerImpl", "GEMINI_SIM_ID_KEY = " + this.mSimId + ", subId = " + this.mSubId);
    }

    private boolean validate(Map<String, String> apnInfo) {
        if (TextUtils.isEmpty(apnInfo.get("name"))) {
            Log.e("DeviceNetworkManagerImpl", "name is null");
            return false;
        } else if (TextUtils.isEmpty(apnInfo.get("apn"))) {
            Log.e("DeviceNetworkManagerImpl", "apn is null");
            return false;
        } else if (!TextUtils.isEmpty(apnInfo.get("numeric"))) {
            return true;
        } else {
            Log.e("DeviceNetworkManagerImpl", "numeric is null");
            return false;
        }
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

    private void getApnValues(Map<String, String> apnInfo) {
        this.mName = apnInfo.get("name");
        this.mApn = apnInfo.get("apn");
        this.mProxy = apnInfo.get("proxy");
        this.mPort = apnInfo.get("port");
        this.mMmsProxy = apnInfo.get("mmsproxy");
        this.mMmsPort = apnInfo.get("mmsport");
        this.mServer = apnInfo.get("server");
        this.mUser = apnInfo.get("user");
        this.mPassword = apnInfo.get("password");
        this.mMmsc = apnInfo.get("mmsc");
        this.mMcc = apnInfo.get("mcc");
        this.mMnc = apnInfo.get("mnc");
        this.mNumeric = apnInfo.get("numeric");
        String authType = apnInfo.get("authtype");
        if (authType != null) {
            this.mAuthType = Integer.parseInt(authType);
        }
        this.mApnType = apnInfo.get("type");
        this.mProtocol = apnInfo.get("protocol");
        this.mRoamingProtocol = apnInfo.get("roaming_protocol");
        String current = apnInfo.get("current");
        if (current != null) {
            this.mCurrent = Integer.parseInt(current);
        }
        String carrierEnabled = apnInfo.get("carrier_enabled");
        if (carrierEnabled != null) {
            this.mCarrierEnabled = Integer.parseInt(carrierEnabled);
        }
        String bearer = apnInfo.get("bearer");
        if (bearer != null) {
            this.mBearer = Integer.parseInt(bearer);
        }
        this.mMvnoType = apnInfo.get("mvno_type");
        this.mMvnoMatchData = apnInfo.get("mvno_match_data");
        String subscriptionId = apnInfo.get("sub_id");
        if (subscriptionId != null) {
            this.mSubscriptionId = Integer.parseInt(subscriptionId);
        }
    }

    private ContentValues putApnValues(ContentValues values) {
        values.put("name", this.mName);
        values.put("apn", this.mApn);
        if (this.mProxy != null) {
            values.put("proxy", this.mProxy);
        }
        if (this.mPort != null) {
            values.put("port", this.mPort);
        }
        if (this.mMmsProxy != null) {
            values.put("mmsproxy", this.mMmsProxy);
        }
        if (this.mMmsPort != null) {
            values.put("port", this.mMmsPort);
        }
        if (this.mServer != null) {
            values.put("server", this.mServer);
        }
        if (this.mUser != null) {
            values.put("user", this.mUser);
        }
        if (this.mPassword != null) {
            values.put("password", this.mPassword);
        }
        if (this.mMmsc != null) {
            values.put("mmsc", this.mMmsc);
        }
        values.put("mcc", this.mMcc);
        values.put("mnc", this.mMnc);
        if (this.mNumeric != null) {
            values.put("numeric", this.mNumeric);
        }
        if (this.mAuthType != -1) {
            values.put("authtype", Integer.valueOf(this.mAuthType));
        }
        if (this.mApnType != null) {
            values.put("type", this.mApnType);
        }
        if (this.mProtocol != null) {
            values.put("protocol", this.mProtocol);
        }
        if (this.mRoamingProtocol != null) {
            values.put("roaming_protocol", this.mRoamingProtocol);
        }
        if (this.mCurrent != -1) {
            values.put("current", Integer.valueOf(this.mCurrent));
        }
        if (this.mCarrierEnabled != -1) {
            values.put("carrier_enabled", Integer.valueOf(this.mCarrierEnabled));
        }
        if (this.mBearer != -1) {
            values.put("bearer", Integer.valueOf(this.mBearer));
        }
        if (this.mMvnoType != null) {
            values.put("mvno_type", this.mMvnoType);
        }
        if (this.mMvnoMatchData != null) {
            values.put("mvno_match_data", this.mMvnoMatchData);
        }
        if (this.mSubscriptionId != -1) {
            values.put("sub_id", Integer.valueOf(this.mSubscriptionId));
        }
        return values;
    }

    private int getMainSlotId() {
        return Settings.Global.getInt(this.mResolver, "oppo_multi_sim_network_primary_slot", 0);
    }

    private Map<String, String> createApnInfo() {
        WeakHashMap<String, String> apnInfo = new WeakHashMap<>();
        apnInfo.put("name", this.mName);
        apnInfo.put("apn", this.mApn);
        apnInfo.put("proxy", this.mProxy);
        apnInfo.put("port", this.mPort);
        apnInfo.put("mmsproxy", this.mMmsProxy);
        apnInfo.put("mmsport", this.mMmsPort);
        apnInfo.put("server", this.mServer);
        apnInfo.put("user", this.mUser);
        apnInfo.put("password", this.mPassword);
        apnInfo.put("mmsc", this.mMmsc);
        apnInfo.put("mcc", this.mMcc);
        apnInfo.put("mnc", this.mMnc);
        apnInfo.put("numeric", this.mNumeric);
        apnInfo.put("authtype", String.valueOf(this.mAuthType));
        apnInfo.put("type", this.mApnType);
        apnInfo.put("protocol", this.mProtocol);
        apnInfo.put("roaming_protocol", this.mRoamingProtocol);
        apnInfo.put("current", String.valueOf(this.mCurrent));
        apnInfo.put("carrier_enabled", String.valueOf(this.mCarrierEnabled));
        apnInfo.put("bearer", String.valueOf(this.mBearer));
        apnInfo.put("mvno_type", this.mMvnoType);
        apnInfo.put("mvno_match_data", this.mMvnoMatchData);
        apnInfo.put("sub_id", String.valueOf(this.mSubscriptionId));
        return apnInfo;
    }

    private Uri getUri(Uri uri) {
        Log.e("DeviceNetworkManagerImpl", "subId : " + this.mSubId);
        return Uri.withAppendedPath(uri, "/subId/" + this.mSubId);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void setNetworkRestriction(ComponentName componentName, int pattern) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "chen-liu setNetworkRestriction: pattern is " + pattern);
        try {
            this.mCustService.setNetworkRestriction(pattern);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void addNetworkRestriction(ComponentName componentName, int pattern, List<String> list) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "chen-liu addNetworkRestriction: pattern is " + pattern + ", list is " + list);
        try {
            this.mCustService.addNetworkRestriction(pattern, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void removeNetworkRestriction(ComponentName componentName, int pattern, List<String> list) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "removeNetworkRestriction: pattern is " + pattern + " ,list is " + list);
        try {
            this.mCustService.removeNetworkRestriction(pattern, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public void removeNetworkRestrictionAll(ComponentName componentName, int pattern) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceNetworkManagerImpl", "removeNetworkRestrictionAll: pattern is " + pattern);
        try {
            this.mCustService.removeNetworkRestrictionAll(pattern);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public List<String> getNetworkRestriction(ComponentName admin, int pattern) {
        PermissionManager.getInstance().checkPermission();
        List<String> restrictionList = new ArrayList<>();
        try {
            return this.mCustService.getNetworkRestrictionList(pattern);
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManagerImpl", "getNetworkRestriction error");
            return restrictionList;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public boolean addAppMeteredDataBlackList(ComponentName admin, List<String> pkgs) {
        PermissionManager.getInstance().checkPermission();
        try {
            this.mCustService.addAppMeteredDataBlackList(pkgs);
            return true;
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManagerImpl", "addAppMeteredDataBlackList error");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public boolean removeAppMeteredDataBlackList(ComponentName admin, List<String> pkgs) {
        PermissionManager.getInstance().checkPermission();
        try {
            this.mCustService.removeAppMeteredDataBlackList(pkgs);
            return true;
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManagerImpl", "removeAppMeteredDataBlackList error");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public List<String> getAppMeteredDataBlackList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<String> resolveList = new ArrayList<>();
        try {
            return this.mCustService.getAppMeteredDataBlackList();
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManagerImpl", "getAppMeteredDataBlackList error");
            return resolveList;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public boolean addAppWlanDataBlackList(ComponentName admin, List<String> pkgs) {
        PermissionManager.getInstance().checkPermission();
        try {
            this.mCustService.addAppWlanDataBlackList(pkgs);
            return true;
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManagerImpl", "addAppWlanDataBlackList error");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public boolean removeAppWlanDataBlackList(ComponentName admin, List<String> pkgs) {
        PermissionManager.getInstance().checkPermission();
        try {
            this.mCustService.removeAppWlanDataBlackList(pkgs);
            return true;
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManagerImpl", "removeAppWlanDataBlackList error");
            return true;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceNetworkManager
    public List<String> getAppWlanDataBlackList(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        List<String> resolveList = new ArrayList<>();
        try {
            return this.mCustService.getAppWlanDataBlackList();
        } catch (RemoteException e) {
            Log.e("DeviceNetworkManagerImpl", "getAppWlanDataBlackList error");
            return resolveList;
        }
    }
}
