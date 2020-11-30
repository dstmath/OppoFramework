package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.OppoNetworkingControlManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.util.Log;
import android.util.Range;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.AbstractLocationProvider;
import com.android.server.location.interfaces.IPswNlpProxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class OppoNlpProxy implements IPswNlpProxy {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final int CHINA_NLP_DELAY_TIME = 3000;
    private static boolean DEBUG = false;
    private static final String DEFAULT_DATA_SUBSCRIPTION_CHANGED_ACTION = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED";
    private static final String DEFAULT_EXIST_NLP_PACKAGE_NAME = "nothing";
    private static final String DEFAULT_NLP_NAME_PROPERTIES = "ro.vendor.local.nlp";
    private static final int DEVICE_IN_REGULATED_REGION_AT_EMBARGOED = 2;
    private static final int DEVICE_IN_REGULATED_REGION_AT_REGIONAL_NLP = 4;
    private static final int DEVICE_IN_REGULATED_REGION_KNOWN = 1;
    private static final int DEVICE_IN_REGULATED_REGION_UNKNOWN = 0;
    private static final String GMS_PACKAGE_NAME = "com.google.android.gms";
    private static final String KEY_AMAP_NLP_ID = "adiu";
    private static final String KEY_NLP_ROAMING_ENABLE = "config_nlpRoamingEnabled";
    private static final String KEY_OPPO_NLP_ID_PROPERTIES = "persist.sys.oppo.nlp.id";
    private static final String KEY_OS_NLP_ENABLE = "config_osNlpEnabled";
    private static final String KEY_REGION_NLP_ENABLE = "config_regionNlpEnabled";
    private static final String KEY_REGION_NLP_MCC = "config_regionNlpMcc";
    private static final String KEY_REGION_NLP_SID = "config_regionNlpSid";
    private static final String KEY_REGION_PROPERTIES = "persist.sys.oppo.region";
    private static final String KEY_TENCENT_NLP_ID = "QID";
    private static final int MSG_CELL_LOCATION_CHANGED = 102;
    private static final int MSG_CHINA_NLP_DELAY = 108;
    private static final int MSG_GOOGLE_ACCESS_STATE = 105;
    private static final int MSG_INIT = 101;
    private static final int MSG_LOCATION_CHANGED = 104;
    private static final int MSG_REBIND_NLP_OR_GEOCODER = 107;
    private static final String NETWORK_LOCATION_SERVICE_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    private static final String OPPO_GOOGLE_RESTRICT_CHANGE_ACTION = "oppo.intent.action.google_restrict_change";
    private static final String OPPO_LBS_CONFIG_UPDATE_ACTION = "com.android.location.oppo.lbsconfig.update.success";
    private static final String OPPO_SIM_STATE_CHANGED_ACTION = "android.intent.action.SIM_STATE_CHANGED";
    private static final String OPPO_SUPL_CONFIG_UPDATE_ACTION = "com.android.location.oppo.suplconfig.update.success";
    private static final int POLICY_ALL_REJECT = 4;
    private static final int POLICY_MOBILE_ONLY = 2;
    private static final int POLICY_NONE = 0;
    private static final int POLICY_WIFI_ONLY = 1;
    private static final int PROVIDER_MAX_FAIL_THRESHOLD = 3;
    private static final String TAG = "OppoNlpProxy";
    private static final int VISIT_GOOGLE_FAILED = 0;
    private static final int VISIT_GOOGLE_NONE = -1;
    private static final int VISIT_GOOGLE_SUCCESSFUL = 1;
    private static String mRomType = null;
    private GeocoderProxy mActiveGeocoderProxy;
    private LocationController mActiveNpHandle = null;
    private LocationProviderProxy mActiveNpProxy;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.OppoNlpProxy.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
            oppoNlpProxy.logd("receive broadcast intent, action: " + action);
            if (action.equals(OppoNlpProxy.OPPO_LBS_CONFIG_UPDATE_ACTION)) {
                OppoNlpProxy.this.initValue();
            } else if (action.equals(OppoNlpProxy.OPPO_SIM_STATE_CHANGED_ACTION)) {
                OppoNlpProxy.this.handleSimStateChanged(intent);
            } else if (action.equals(OppoNlpProxy.DEFAULT_DATA_SUBSCRIPTION_CHANGED_ACTION)) {
                if (OppoNlpProxy.this.mNlpRoamingEnabled && !OppoNlpProxy.isExpROM() && !OppoNlpProxy.this.mDeviceInRegion) {
                    OppoNlpProxy.this.isChinaSimRomingAborad();
                }
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                OppoNlpProxy.this.handleNetworkStateEvent(intent);
            } else if (OppoNlpProxy.ACTION_BOOT_COMPLETED.equals(action)) {
                OppoNlpProxy.this.handleBootCompleted();
            } else if (OppoNlpProxy.OPPO_GOOGLE_RESTRICT_CHANGE_ACTION.equals(action)) {
                OppoNlpProxy.this.handleGoogleRestrictChanged();
            }
        }
    };
    private boolean mChinaNlpDelayMessageHandledFlag = false;
    private ConnectivityManager mConnManager = null;
    private Context mContext = null;
    @GuardedBy({"mLock"})
    private boolean mDeviceInRegion = (true ^ isExpROM());
    private boolean mEnabled;
    private int mGmsUid = -1;
    private int mGmsUidPolicy = 0;
    private Handler mHandler = null;
    private boolean mIsGmsRestrict = false;
    private Location mLastChinaNLpLocation = null;
    private WorkSource mLastWorkSource = null;
    private Object mLock = new Object();
    private int mNetworkType = -2;
    private int mNlpAmapResId;
    private int mNlpBaiduResId;
    private boolean mNlpRoamingEnabled = true;
    private int mNlpTencentResId;
    private int mNnlpArrayResId;
    private LocationController mOSNpHandle;
    private OppoNetworkingControlManager mOppoNetworkingControlManager = null;
    private String mOppoNlpID = null;
    private GeocoderProxy mOsGeocoderProxy;
    private boolean mOsNlpEnabled = true;
    private LocationProviderProxy mOsNpProxy;
    private int mOsnlpResId;
    private int mOverlayResId;
    private Handler mParentHandler = null;
    private AbstractLocationProvider.LocationProviderManager mParentLocationProvider = null;
    private GeocoderProxy mRegionGeocoderProxy;
    private boolean mRegionNlpEnabled = true;
    private LocationProviderProxy mRegionNpProxy;
    private LocationController mRegionOSNpHandle;
    private HashMap<Integer, HashSet<Integer>> mRegionalNLPCountryList = new HashMap<>();
    private ArrayList<Range<Integer>> mRegionalNLPSIDRanges = new ArrayList<>();
    private int mRegionalOsnlpResId;
    private RilListener mRilListenerForSim1 = null;
    private RilListener mRilListenerForSim2 = null;
    private OppoLbsRomUpdateUtil mRomUpdateUtil = null;
    private int mSwitchToGms = -1;
    private int mVisitGoogleState = -1;
    private Handler.Callback m_handler_callback = new Handler.Callback() {
        /* class com.android.server.location.OppoNlpProxy.AnonymousClass3 */

        public boolean handleMessage(Message msg) {
            int msgID = msg.what;
            OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
            oppoNlpProxy.logv("handleMessage what - " + msgID);
            if (msgID == OppoNlpProxy.MSG_INIT) {
                OppoNlpProxy.this.initServiceBinding();
                OppoNlpProxy.this.installRilListener();
            } else if (msgID == OppoNlpProxy.MSG_GOOGLE_ACCESS_STATE) {
                if (((Boolean) msg.obj).booleanValue()) {
                    OppoNlpProxy.this.mSwitchToGms = 1;
                } else {
                    OppoNlpProxy.this.mSwitchToGms = 0;
                }
                OppoNlpProxy.this.initServiceBinding();
            } else if (msgID == OppoNlpProxy.MSG_REBIND_NLP_OR_GEOCODER) {
                OppoNlpProxy.this.rebindNlpOrGeocoder();
            } else if (msgID != OppoNlpProxy.MSG_CHINA_NLP_DELAY) {
                OppoNlpProxy.this.loge("Unhandled message");
            } else {
                OppoNlpProxy.this.handleMessageForDelayChinaNlpReport();
            }
            return true;
        }
    };

    public OppoNlpProxy(Context context, Handler.Callback callback) {
        this.mContext = context;
        this.mParentHandler = new Handler(callback);
        initValue();
    }

    public OppoNlpProxy(Context context, AbstractLocationProvider.LocationProviderManager locationProvider) {
        this.mContext = context;
        this.mParentLocationProvider = locationProvider;
        HandlerThread nlpThread = new HandlerThread(TAG);
        nlpThread.start();
        this.mHandler = new Handler(nlpThread.getLooper(), this.m_handler_callback);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OPPO_LBS_CONFIG_UPDATE_ACTION);
        intentFilter.addAction(OPPO_SIM_STATE_CHANGED_ACTION);
        intentFilter.addAction(DEFAULT_DATA_SUBSCRIPTION_CHANGED_ACTION);
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction(OPPO_GOOGLE_RESTRICT_CHANGE_ACTION);
        intentFilter.addAction(ACTION_BOOT_COMPLETED);
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter, "oppo.permission.OPPO_COMPONENT_SAFE", this.mHandler);
        initValue();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initValue() {
        if (this.mOppoNlpID == null) {
            this.mOppoNlpID = SystemProperties.get(KEY_OPPO_NLP_ID_PROPERTIES, "0");
        }
        this.mRomUpdateUtil = OppoLbsRomUpdateUtil.getInstall(this.mContext);
        this.mOsNlpEnabled = this.mRomUpdateUtil.getBoolean(KEY_OS_NLP_ENABLE);
        this.mRegionNlpEnabled = this.mRomUpdateUtil.getBoolean(KEY_REGION_NLP_ENABLE);
        this.mNlpRoamingEnabled = this.mOsNlpEnabled && this.mRegionNlpEnabled && this.mRomUpdateUtil.getBoolean(KEY_NLP_ROAMING_ENABLE);
        populateSIDRanges(this.mRegionalNLPSIDRanges, this.mRomUpdateUtil.getString(KEY_REGION_NLP_SID));
        populateCountryList(this.mRegionalNLPCountryList, this.mRomUpdateUtil.getString(KEY_REGION_NLP_MCC));
        this.mRegionalOsnlpResId = getOemResId("config_regionNetworkLocationProviderPackageName", "string", -1);
        this.mOsnlpResId = getOemResId("config_osNetworkLocationProviderPackageName", "string", -1);
        this.mOverlayResId = getOemResId("config_enableNetworkLocationProviderOverlay", "bool", -1);
        this.mNnlpArrayResId = getOemResId("config_networkLocationProviderPackageNames", "array", -1);
        this.mNlpAmapResId = getOemResId("config_nlp_packageName_amap", "string", -1);
        this.mNlpBaiduResId = getOemResId("config_nlp_packageName_baidu", "string", -1);
        this.mNlpTencentResId = getOemResId("config_nlp_packageName_tencent", "string", -1);
        initServiceBinding();
    }

    private int getOemResId(String resName, String type, int defValue) {
        try {
            int resId = this.mContext.getResources().getIdentifier(resName, type, "oppo");
            Log.d(TAG, "resource of name:" + resName + " id is:" + resId);
            return resId;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            return defValue;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initServiceBinding() {
        int localNlpPackageResId = findLocalExistNlpPackage(this.mRegionalOsnlpResId, this.mNnlpArrayResId);
        if (-1 != localNlpPackageResId) {
            this.mRegionalOsnlpResId = localNlpPackageResId;
        }
        if (!this.mDeviceInRegion || !this.mRegionNlpEnabled) {
            if (this.mOsNlpEnabled && this.mOSNpHandle == null && !this.mIsGmsRestrict) {
                this.mOSNpHandle = new LocationController();
                this.mOsNpProxy = LocationProviderProxy.createAndBind(this.mContext, this.mOSNpHandle, NETWORK_LOCATION_SERVICE_ACTION, this.mOverlayResId, this.mOsnlpResId, this.mNnlpArrayResId);
                this.mOSNpHandle.attachProvider(this.mOsNpProxy);
                this.mOSNpHandle.setNlpName("gms");
                this.mOsGeocoderProxy = GeocoderProxy.createAndBind(this.mContext, this.mOverlayResId, this.mOsnlpResId, this.mNnlpArrayResId);
            }
        } else if (this.mRegionOSNpHandle == null) {
            this.mRegionOSNpHandle = new LocationController();
            this.mRegionNpProxy = LocationProviderProxy.createAndBind(this.mContext, this.mRegionOSNpHandle, NETWORK_LOCATION_SERVICE_ACTION, this.mOverlayResId, this.mRegionalOsnlpResId, this.mNnlpArrayResId);
            this.mRegionOSNpHandle.attachProvider(this.mRegionNpProxy);
            this.mRegionOSNpHandle.setNlpName("nlp");
            this.mRegionGeocoderProxy = GeocoderProxy.createAndBind(this.mContext, this.mOverlayResId, this.mRegionalOsnlpResId, this.mNnlpArrayResId);
        }
        updateServiceBinding();
    }

    private void updateServiceBinding() {
        boolean hasAndAllowDefaultNp = false;
        boolean useRegional = this.mDeviceInRegion || (!isExpROM() && (this.mIsGmsRestrict || this.mSwitchToGms == 0));
        this.mSwitchToGms = -1;
        boolean hasDefaultNp = this.mOSNpHandle != null && checkPackageExists(this.mContext.getResources().getString(this.mOsnlpResId));
        boolean hasRegionNp = this.mRegionOSNpHandle != null && checkPackageExists(this.mContext.getResources().getString(this.mRegionalOsnlpResId));
        Log.d(TAG, "hasRegionNp = " + hasRegionNp + "; hasDefaultNp = " + hasDefaultNp);
        if (hasDefaultNp && !useRegional) {
            hasAndAllowDefaultNp = true;
        }
        if (hasAndAllowDefaultNp) {
            this.mActiveNpHandle = this.mOSNpHandle;
            this.mActiveGeocoderProxy = this.mOsGeocoderProxy;
            this.mActiveNpProxy = this.mOsNpProxy;
            SystemProperties.set("oppo.nlp.proxy", "gms");
            logd("OS NLP is selected to run");
        } else if (hasRegionNp) {
            this.mActiveNpHandle = this.mRegionOSNpHandle;
            this.mActiveGeocoderProxy = this.mRegionGeocoderProxy;
            this.mActiveNpProxy = this.mRegionNpProxy;
            SystemProperties.set("oppo.nlp.proxy", "nlp");
            logd("Regional NLP is selected to run");
        } else if (this.mActiveNpHandle == null) {
            Log.w(TAG, "Device has no NLP service to use");
        }
    }

    public void setRequestTry(ProviderRequest requests, WorkSource workSource) {
        if (requests != null) {
            if (requests.reportLocation) {
                OppoLocationStatistics.getInstance().recordNlpNavigatingStarted();
            }
            if (isNeedDoubleRequest(!isExpROM(), this.mActiveNpHandle)) {
                doubleRequestStart(this.mOSNpHandle, this.mRegionOSNpHandle, requests, workSource);
                return;
            }
            try {
                this.mActiveNpHandle.setRequest(requests, workSource);
            } catch (NullPointerException npe) {
                OppoLocationStatistics.getInstance().recordNlpError(1);
                Log.w(TAG, npe);
            }
        }
    }

    private boolean checkPackageExists(String packageName) {
        boolean exists = true;
        boolean exists2 = packageName != null && !packageName.isEmpty();
        if (!exists2) {
            return exists2;
        }
        try {
            String info = this.mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
            if (info == null || info.isEmpty()) {
                exists = false;
            }
            return exists;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private int getPackageUid(String packageName) {
        int uid = -1;
        try {
            uid = this.mContext.getPackageManager().getApplicationInfo(packageName, 1).uid;
            logd("getPackageUid is : " + uid);
            return uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return uid;
        }
    }

    public LocationController getActiveNpHandle() {
        return this.mActiveNpHandle;
    }

    public GeocoderProxy getActiveGeocoderProxy() {
        if (this.mActiveNpProxy == null) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_REBIND_NLP_OR_GEOCODER, true));
        }
        return this.mActiveGeocoderProxy;
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        GeocoderProxy geocoderProxy;
        OppoLocationStatistics.getInstance().recordGeocoderRequestStarted();
        String geocoderReturn = null;
        if (isNeedDoubleRequest(!isExpROM(), this.mActiveNpHandle)) {
            GeocoderProxy geocoderProxy2 = this.mOsGeocoderProxy;
            if (geocoderProxy2 != null) {
                geocoderReturn = geocoderProxy2.getFromLocation(latitude, longitude, maxResults, params, addrs);
                if ((geocoderReturn != null || addrs == null || (addrs != null && addrs.size() == 0)) && (geocoderProxy = this.mRegionGeocoderProxy) != null) {
                    geocoderReturn = geocoderProxy.getFromLocation(latitude, longitude, maxResults, params, addrs);
                    logd("gms geocoder not ok,so I use china geocoder");
                } else {
                    logd("gms geocoder get address is ok");
                }
            }
        } else {
            GeocoderProxy geocoderProxy3 = getActiveGeocoderProxy();
            if (geocoderProxy3 != null) {
                geocoderReturn = geocoderProxy3.getFromLocation(latitude, longitude, maxResults, params, addrs);
            } else {
                OppoLocationStatistics.getInstance().recordGeocoderError(3);
            }
        }
        if (geocoderReturn == null) {
            OppoLocationStatistics.getInstance().recordGeocoderRequestStopped();
        } else {
            OppoLocationStatistics.getInstance().recordGeocoderError(3);
        }
        return geocoderReturn;
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        GeocoderProxy geocoderProxy;
        OppoLocationStatistics.getInstance().recordGeocoderRequestStarted();
        String geocoderReturn = null;
        if (isNeedDoubleRequest(!isExpROM(), this.mActiveNpHandle)) {
            GeocoderProxy geocoderProxy2 = this.mOsGeocoderProxy;
            if (geocoderProxy2 != null) {
                geocoderReturn = geocoderProxy2.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
                if ((geocoderReturn != null || addrs == null || (addrs != null && addrs.size() == 0)) && (geocoderProxy = this.mRegionGeocoderProxy) != null) {
                    geocoderReturn = geocoderProxy.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
                    logd("gms geocoder not ok,so I use china geocoder");
                } else {
                    logd("gms geocoder get address is ok");
                }
            }
        } else {
            GeocoderProxy geocoderProxy3 = getActiveGeocoderProxy();
            if (geocoderProxy3 != null) {
                geocoderReturn = geocoderProxy3.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            } else {
                OppoLocationStatistics.getInstance().recordGeocoderError(3);
            }
        }
        if (geocoderReturn == null) {
            OppoLocationStatistics.getInstance().recordGeocoderRequestStopped();
        } else {
            OppoLocationStatistics.getInstance().recordGeocoderError(3);
        }
        return geocoderReturn;
    }

    public LocationProviderProxy getActiveNpProxy() {
        if (this.mActiveNpProxy == null) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_REBIND_NLP_OR_GEOCODER, true));
        }
        return this.mActiveNpProxy;
    }

    /* access modifiers changed from: private */
    public static boolean isExpROM() {
        String str = mRomType;
        if (str == null || str.isEmpty()) {
            mRomType = SystemProperties.get(KEY_REGION_PROPERTIES);
        }
        return !"CN".equalsIgnoreCase(mRomType);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void installRilListener() {
        if (!isExpROM()) {
            if (this.mRilListenerForSim1 == null) {
                this.mRilListenerForSim1 = new RilListener(0, this.mHandler.getLooper());
            }
            if (this.mRilListenerForSim2 == null) {
                this.mRilListenerForSim2 = new RilListener(1, this.mHandler.getLooper());
            }
        }
    }

    private void populateSIDRanges(ArrayList<Range<Integer>> sidRangeList, String configSidRangeList) {
        if (sidRangeList == null) {
            sidRangeList = new ArrayList<>();
        } else {
            sidRangeList.clear();
        }
        try {
            String[] sidRanges = configSidRangeList.trim().split("\\[");
            for (String sidRangeStr : sidRanges) {
                try {
                    String sidRangeStr_t = sidRangeStr.trim();
                    if (true != sidRangeStr_t.equals(StringUtils.EMPTY)) {
                        String[] sidRange = sidRangeStr_t.substring(0, sidRangeStr_t.length() - 1).split(",");
                        sidRangeList.add(new Range<>(Integer.valueOf(Integer.parseInt(sidRange[0].trim())), Integer.valueOf(Integer.parseInt(sidRange[1].trim()))));
                    }
                } catch (PatternSyntaxException e) {
                    loge("OsAgent: Error in spliting SID range" + sidRangeStr + ":" + e.toString());
                } catch (IndexOutOfBoundsException e2) {
                    loge("OsAgent: Error in reading SID range " + sidRangeStr + ":" + e2.toString());
                } catch (NumberFormatException e3) {
                    loge("OsAgent: Error in reading SID range" + sidRangeStr + ":" + e3.toString());
                } catch (NullPointerException e4) {
                    loge("OsAgent: Error in get SID range" + sidRangeStr + ":" + e4.toString());
                }
            }
        } catch (PatternSyntaxException e5) {
            loge("OsAgent: Error in reading configurations:" + e5.toString());
        } catch (NullPointerException e6) {
            loge("OsAgent: Input SID range is null:" + e6.toString());
        }
    }

    private void populateCountryList(HashMap<Integer, HashSet<Integer>> mapMccMnc, String configMccList) {
        HashMap<Integer, HashSet<Integer>> mapMccMnc2;
        NullPointerException e;
        PatternSyntaxException e2;
        IndexOutOfBoundsException e3;
        NumberFormatException e4;
        if (mapMccMnc == null) {
            mapMccMnc2 = new HashMap<>();
        } else {
            mapMccMnc.clear();
            mapMccMnc2 = mapMccMnc;
        }
        try {
            String[] mccList = configMccList.split("~");
            int i = 0;
            for (String mccStr : mccList) {
                try {
                    if (mccStr.trim().contains("[")) {
                        String[] mccMncsCombo = mccStr.split("\\[");
                        String mncSingleString = mccMncsCombo[1].trim();
                        String[] mncs = mncSingleString.substring(i, mncSingleString.length() - 1).split(",");
                        HashSet<Integer> mnc_list = new HashSet<>(mncs.length);
                        int length = mncs.length;
                        for (int i2 = i; i2 < length; i2++) {
                            try {
                                mnc_list.add(Integer.valueOf(Integer.parseInt(mncs[i2].trim())));
                            } catch (NullPointerException e5) {
                                e = e5;
                                i = 0;
                                loge("OsAgent: Error in reading MCC" + mccStr + ":" + e.toString());
                            } catch (PatternSyntaxException e6) {
                                e2 = e6;
                                i = 0;
                                loge("OsAgent: Error in spliting MCC String" + mccStr + ":" + e2.toString());
                            } catch (IndexOutOfBoundsException e7) {
                                e3 = e7;
                                i = 0;
                                loge("OsAgent: Error in reading MNC for MCC " + mccStr + ":" + e3.toString());
                            } catch (NumberFormatException e8) {
                                e4 = e8;
                                i = 0;
                                loge("OsAgent: Error in reading MCC" + mccStr + ":" + e4.toString());
                            }
                        }
                        i = 0;
                        mapMccMnc2.put(Integer.valueOf(Integer.parseInt(mccMncsCombo[0].trim())), mnc_list);
                    } else if (!mccStr.trim().equals(StringUtils.EMPTY)) {
                        mapMccMnc2.put(Integer.valueOf(Integer.parseInt(mccStr.trim())), null);
                    }
                } catch (NullPointerException e9) {
                    e = e9;
                    loge("OsAgent: Error in reading MCC" + mccStr + ":" + e.toString());
                } catch (PatternSyntaxException e10) {
                    e2 = e10;
                    loge("OsAgent: Error in spliting MCC String" + mccStr + ":" + e2.toString());
                } catch (IndexOutOfBoundsException e11) {
                    e3 = e11;
                    loge("OsAgent: Error in reading MNC for MCC " + mccStr + ":" + e3.toString());
                } catch (NumberFormatException e12) {
                    e4 = e12;
                    loge("OsAgent: Error in reading MCC" + mccStr + ":" + e4.toString());
                }
            }
        } catch (PatternSyntaxException e13) {
            loge("OsAgent: Error in reading configurations:" + e13.toString());
        } catch (NullPointerException e14) {
            loge("OsAgent: Input configMccList is null:" + e14.toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void handleCellLocationCallback(boolean deviceInRegion) {
        if (this.mDeviceInRegion != deviceInRegion) {
            this.mDeviceInRegion = deviceInRegion;
            initServiceBinding();
        }
        if (this.mNlpRoamingEnabled && !isExpROM() && !this.mDeviceInRegion) {
            tryGoogleAccess();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isChinaSimRomingAborad() {
        try {
            logd("isChinaSimRomingAboard DefaultDataSubscriptionId = " + SubscriptionManager.getDefaultDataSubscriptionId());
            if (!OppoNetworkUtil.isChineseSim(this.mContext, SubscriptionManager.getDefaultDataSubscriptionId())) {
                return false;
            }
            tryGoogleAccess();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void tryGoogleAccess() {
        LocationController locationController;
        boolean switchGms = true;
        boolean needUpdate = false;
        OppoNetworkingControlManager oppoNetworkingControlManager = this.mOppoNetworkingControlManager;
        if (oppoNetworkingControlManager != null) {
            this.mGmsUidPolicy = oppoNetworkingControlManager.getUidPolicy(this.mGmsUid);
            logd("handle network state changed: mNetworkType = " + this.mNetworkType + ", mGmsUidPolicy = " + this.mGmsUidPolicy);
            int i = this.mGmsUidPolicy;
            boolean z = false;
            if (4 == i || ((this.mNetworkType == 1 && 1 == i) || (this.mNetworkType == 0 && 2 == this.mGmsUidPolicy))) {
                this.mIsGmsRestrict = true;
                switchGms = false;
                LocationController locationController2 = this.mActiveNpHandle;
                if (locationController2 == null || "gms".equals(locationController2.getNlpName())) {
                    z = true;
                }
                needUpdate = z;
            } else {
                this.mIsGmsRestrict = false;
                int i2 = this.mVisitGoogleState;
                if (-1 == i2) {
                    new Thread() {
                        /* class com.android.server.location.OppoNlpProxy.AnonymousClass2 */

                        public void run() {
                            if (OppoNetworkUtil.checkGoogleNetwork()) {
                                OppoNlpProxy.this.logd("tryGoogleAccess Access Google passed");
                                OppoNlpProxy.this.mVisitGoogleState = 1;
                                if (OppoNlpProxy.this.mActiveNpHandle == null || "nlp".equals(OppoNlpProxy.this.mActiveNpHandle.getNlpName())) {
                                    OppoNlpProxy.this.mHandler.sendMessage(Message.obtain(OppoNlpProxy.this.mHandler, OppoNlpProxy.MSG_GOOGLE_ACCESS_STATE, true));
                                    return;
                                }
                                return;
                            }
                            OppoNlpProxy.this.logd("tryGoogleAccess Access Google failed");
                            OppoNlpProxy.this.mVisitGoogleState = 0;
                            if (OppoNlpProxy.this.mActiveNpHandle == null || "gms".equals(OppoNlpProxy.this.mActiveNpHandle.getNlpName())) {
                                OppoNlpProxy.this.mHandler.sendMessage(Message.obtain(OppoNlpProxy.this.mHandler, OppoNlpProxy.MSG_GOOGLE_ACCESS_STATE, false));
                            }
                        }
                    }.start();
                } else if (1 == i2) {
                    LocationController locationController3 = this.mActiveNpHandle;
                    if (locationController3 == null || "nlp".equals(locationController3.getNlpName())) {
                        switchGms = true;
                        needUpdate = true;
                    }
                } else if (i2 == 0 && ((locationController = this.mActiveNpHandle) == null || "gms".equals(locationController.getNlpName()))) {
                    switchGms = false;
                    needUpdate = true;
                }
            }
        }
        if (needUpdate) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, MSG_GOOGLE_ACCESS_STATE, Boolean.valueOf(switchGms)));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBootCompleted() {
        installRilListener();
        logd("handle boot completed!!---");
        this.mGmsUid = getPackageUid(GMS_PACKAGE_NAME);
        this.mConnManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mOppoNetworkingControlManager = OppoNetworkingControlManager.getOppoNetworkingControlManager();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimStateChanged(Intent intent) {
        intent.getIntExtra("subscription", -1);
        int slotId = intent.getIntExtra("phone", 0);
        logd("get bundle:" + intent.getExtras().toString());
        String state = intent.getStringExtra("ss");
        if (state == null) {
            Log.e(TAG, "Get a null sim state!!");
        } else if (state.equals("READY")) {
            RilListener rilListener = this.mRilListenerForSim1;
            if (rilListener == null || rilListener.getSlotId() != slotId) {
                RilListener rilListener2 = this.mRilListenerForSim2;
                if (rilListener2 != null && rilListener2.getSlotId() == slotId) {
                    this.mRilListenerForSim2.unRegistListener();
                    this.mRilListenerForSim2.registListener();
                    return;
                }
                return;
            }
            this.mRilListenerForSim1.unRegistListener();
            this.mRilListenerForSim1.registListener();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkStateEvent(Intent intent) {
        if (this.mNlpRoamingEnabled && !isExpROM() && !this.mDeviceInRegion) {
            int networkType = intent.getIntExtra("networkType", 0);
            logd("handleNetworkStateEvent : networkType " + networkType + ", mNetworkType : " + this.mNetworkType);
            if (this.mNetworkType != networkType) {
                this.mNetworkType = networkType;
                this.mVisitGoogleState = -1;
                tryGoogleAccess();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGoogleRestrictChanged() {
        tryGoogleAccess();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void rebindNlpOrGeocoder() {
        LocationController locationController;
        LocationController locationController2;
        if (this.mActiveNpProxy == null || this.mActiveGeocoderProxy == null) {
            String nowNlp = SystemProperties.get("oppo.nlp.proxy", DEFAULT_EXIST_NLP_PACKAGE_NAME);
            if (nowNlp.equals("gms") && (locationController2 = this.mOSNpHandle) != null) {
                this.mOsNpProxy = LocationProviderProxy.createAndBind(this.mContext, locationController2, NETWORK_LOCATION_SERVICE_ACTION, this.mOverlayResId, this.mOsnlpResId, this.mNnlpArrayResId);
                this.mOSNpHandle.attachProvider(this.mOsNpProxy);
                this.mOsGeocoderProxy = GeocoderProxy.createAndBind(this.mContext, this.mOverlayResId, this.mOsnlpResId, this.mNnlpArrayResId);
                updateServiceBinding();
            } else if (nowNlp.equals("nlp") && (locationController = this.mRegionOSNpHandle) != null) {
                this.mRegionNpProxy = LocationProviderProxy.createAndBind(this.mContext, locationController, NETWORK_LOCATION_SERVICE_ACTION, this.mOverlayResId, this.mRegionalOsnlpResId, this.mNnlpArrayResId);
                this.mRegionOSNpHandle.attachProvider(this.mRegionNpProxy);
                this.mRegionGeocoderProxy = GeocoderProxy.createAndBind(this.mContext, this.mOverlayResId, this.mRegionalOsnlpResId, this.mNnlpArrayResId);
                updateServiceBinding();
            }
        }
    }

    /* access modifiers changed from: private */
    public class LocationController implements AbstractLocationProvider.LocationProviderManager {
        private int mFailCount;
        private String mNlpName;
        private AbstractLocationProvider mProvider;

        private LocationController() {
            this.mFailCount = 0;
        }

        public void attachProvider(AbstractLocationProvider provider) {
            this.mProvider = provider;
        }

        public void setRequest(ProviderRequest request, WorkSource workSource) {
            try {
                OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
                oppoNlpProxy.logd("OppoNlpProxy setRequest! NLP is: " + this.mNlpName);
                this.mProvider.setRequest(request, workSource);
            } catch (NullPointerException npe) {
                OppoLocationStatistics.getInstance().recordNlpError(1);
                Log.w(OppoNlpProxy.TAG, npe);
            }
        }

        public void setNlpName(String name) {
            this.mNlpName = name;
        }

        public String getNlpName() {
            return this.mNlpName;
        }

        public int getFailCount() {
            return this.mFailCount;
        }

        public void addFailCount() {
            int i = this.mFailCount;
            if (i <= 3) {
                this.mFailCount = i + 1;
            }
        }

        public void clearFailCount() {
            this.mFailCount = 0;
        }

        public int getStatus(Bundle extras) {
            try {
                return this.mProvider.getStatus(extras);
            } catch (NullPointerException npe) {
                Log.w(OppoNlpProxy.TAG, npe);
                return 1;
            }
        }

        public long getStatusUpdateTime() {
            try {
                return this.mProvider.getStatusUpdateTime();
            } catch (NullPointerException npe) {
                Log.w(OppoNlpProxy.TAG, npe);
                return 0;
            }
        }

        public void onReportLocation(Location location) {
            if ("network".equals(location.getProvider()) && OppoNlpProxy.this.reportLocationPreHandle(this.mNlpName, location)) {
                OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
                oppoNlpProxy.logd("OppoNlpProxy onlocation changed!! location:" + location);
                OppoLocationStatistics.getInstance().recordNlpNavigatingStopped();
                if (OppoNlpProxy.this.mParentHandler != null) {
                    OppoNlpProxy.this.mParentHandler.obtainMessage(OppoNlpProxy.MSG_LOCATION_CHANGED, location).sendToTarget();
                } else if (OppoNlpProxy.this.mParentLocationProvider != null) {
                    OppoNlpProxy.this.mParentLocationProvider.onReportLocation(location);
                }
                if (location.getExtras() != null) {
                    String nlpId = location.getExtras().getString(OppoNlpProxy.KEY_TENCENT_NLP_ID);
                    if (nlpId == null || nlpId.isEmpty()) {
                        nlpId = location.getExtras().getString(OppoNlpProxy.KEY_AMAP_NLP_ID);
                    }
                    if (nlpId != null && !OppoNlpProxy.this.mOppoNlpID.equals(nlpId)) {
                        OppoNlpProxy.this.mOppoNlpID = nlpId;
                        SystemProperties.set(OppoNlpProxy.KEY_OPPO_NLP_ID_PROPERTIES, OppoNlpProxy.this.mOppoNlpID);
                        Log.e(OppoNlpProxy.TAG, "QID has changed!");
                    }
                }
            }
        }

        public void onReportLocation(List<Location> list) {
        }

        public void onSetEnabled(boolean enabled) {
            if (OppoNlpProxy.this.mParentLocationProvider != null) {
                OppoNlpProxy.this.mParentLocationProvider.onSetEnabled(enabled);
            }
            OppoNlpProxy.this.mEnabled = enabled;
        }

        public void onSetProperties(ProviderProperties properties) {
            if (OppoNlpProxy.this.mParentLocationProvider != null) {
                OppoNlpProxy.this.mParentLocationProvider.onSetProperties(properties);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class RilListener extends PhoneStateListener {
        private int mCurrentServiceState = 1;
        private int mDeviceInRegulatedArea = 0;
        private RilHandler mRilHandler = null;
        private int mSlotId = 0;
        private int mSubId = Integer.MAX_VALUE;
        private TelephonyManager mTelephonyMgr = null;

        public RilListener(int slotId, Looper looper) {
            this.mSlotId = slotId;
            this.mRilHandler = new RilHandler(looper);
            registListener();
            OppoNlpProxy.this.logd("new RilListener");
        }

        public void unRegistListener() {
            if (this.mTelephonyMgr != null) {
                OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
                oppoNlpProxy.logd("unRegistListener : Slot id " + this.mSlotId);
                this.mTelephonyMgr.listen(this, 0);
                this.mTelephonyMgr = null;
            }
        }

        public void registListener() {
            int[] subIds = SubscriptionManager.getSubId(this.mSlotId);
            if (subIds != null && subIds.length > 0) {
                this.mSubId = subIds[0];
            }
            OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
            oppoNlpProxy.logd("registListener : Slot id " + this.mSlotId + ", subId " + this.mSubId);
            this.mTelephonyMgr = ((TelephonyManager) OppoNlpProxy.this.mContext.getSystemService(TelephonyManager.class)).createForSubscriptionId(this.mSubId);
            this.mTelephonyMgr.listen(this, 17);
        }

        public int getSlotId() {
            return this.mSlotId;
        }

        public int getSubId() {
            return this.mSubId;
        }

        public void onCellLocationChanged(CellLocation location) {
            OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
            oppoNlpProxy.logd("deal cellLocationChanged " + location + ", mSlotId " + this.mSlotId);
            this.mRilHandler.sendEmptyMessage(OppoNlpProxy.MSG_CELL_LOCATION_CHANGED);
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
            oppoNlpProxy.logd("CurrentServiceState = " + this.mCurrentServiceState + " NewVoiceServiceState = " + serviceState.getVoiceRegState() + " NewDataServiceState = " + serviceState.getDataRegState() + ", mSlotId " + this.mSlotId);
            if (serviceState.getVoiceRegState() == 0 || serviceState.getDataRegState() == 0) {
                if (this.mCurrentServiceState == 0) {
                    OppoNlpProxy oppoNlpProxy2 = OppoNlpProxy.this;
                    oppoNlpProxy2.logd("No Service State to changed : IN_SERVICE, mSlotId " + this.mSlotId);
                    return;
                }
                this.mCurrentServiceState = 0;
                this.mRilHandler.sendEmptyMessage(OppoNlpProxy.MSG_CELL_LOCATION_CHANGED);
            } else if (1 == this.mCurrentServiceState) {
                OppoNlpProxy oppoNlpProxy3 = OppoNlpProxy.this;
                oppoNlpProxy3.logd("No Service State to changed : OUT_OF_SERVICE, mSlotId " + this.mSlotId);
            } else {
                this.mCurrentServiceState = 1;
                this.mRilHandler.sendEmptyMessage(OppoNlpProxy.MSG_CELL_LOCATION_CHANGED);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleCellLocationChanged() {
            int isDevInRegulatedArea = isDeviceInRegulatedArea();
            boolean deviceInRegion = OppoNlpProxy.this.mDeviceInRegion;
            if (isDevInRegulatedArea != 0) {
                deviceInRegion = (isDevInRegulatedArea & 4) != 0;
            }
            OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
            oppoNlpProxy.logd("Get result from: sim" + this.mSubId + ", isDevInRegulatedArea = " + isDevInRegulatedArea + ", deviceInRegion = " + deviceInRegion + ", mIsGmsRestrict = " + OppoNlpProxy.this.mIsGmsRestrict);
            OppoNlpProxy.this.handleCellLocationCallback(deviceInRegion);
            if (this.mDeviceInRegulatedArea != isDevInRegulatedArea) {
                this.mDeviceInRegulatedArea = isDevInRegulatedArea;
            }
        }

        private int isDeviceInRegulatedArea() {
            HashSet<Integer> mncs;
            int deviceInRegulatedArea = 0;
            if (this.mCurrentServiceState == 0) {
                int iMcc = 0;
                int iMnc = 0;
                int iSid = 0;
                int phoneType = this.mTelephonyMgr.getPhoneType();
                OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
                oppoNlpProxy.logv("Before computing is current n/w state regulated area:  " + this.mDeviceInRegulatedArea + ", mSlotId " + this.mSlotId);
                if (phoneType != 0) {
                    if (phoneType == 1) {
                        String strNetworkOperator = this.mTelephonyMgr.getNetworkOperator();
                        if (strNetworkOperator != null && !strNetworkOperator.isEmpty()) {
                            iMcc = OppoNlpProxy.this.getMcc(Integer.parseInt(strNetworkOperator), strNetworkOperator.length());
                            iMnc = OppoNlpProxy.this.getMnc(Integer.parseInt(strNetworkOperator), strNetworkOperator.length());
                        }
                        if (iMcc == 0) {
                            OppoNlpProxy.this.logv("isDeviceInRegulatedArea: MCC is zero");
                        } else {
                            deviceInRegulatedArea = 1;
                            if (OppoNlpProxy.this.mRegionalNLPCountryList.containsKey(Integer.valueOf(iMcc)) && ((mncs = (HashSet) OppoNlpProxy.this.mRegionalNLPCountryList.get(Integer.valueOf(iMcc))) == null || mncs.contains(Integer.valueOf(iMnc)))) {
                                deviceInRegulatedArea = 1 | 4;
                            }
                        }
                    } else if (phoneType == 2) {
                        CellLocation location = this.mTelephonyMgr.getCellLocation();
                        if (location instanceof CdmaCellLocation) {
                            iSid = ((CdmaCellLocation) location).getSystemId();
                        }
                        if (iSid != 0) {
                            deviceInRegulatedArea = 1;
                            Iterator it = OppoNlpProxy.this.mRegionalNLPSIDRanges.iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    if (((Range) it.next()).contains((Range<Integer>) Integer.valueOf(iSid))) {
                                        deviceInRegulatedArea = 1 | 4;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        } else {
                            OppoNlpProxy.this.logd("isDeviceInRegulatedArea: Sid is zero");
                        }
                    }
                }
                OppoNlpProxy oppoNlpProxy2 = OppoNlpProxy.this;
                oppoNlpProxy2.logv("After computing is n/w state in regulated area?, " + deviceInRegulatedArea + ", mSlotId " + this.mSlotId);
            }
            if (deviceInRegulatedArea != 0) {
                return deviceInRegulatedArea;
            }
            OppoNlpProxy.this.logv("isDeviceInRegulatedArea: unknown - keeping previous state");
            return this.mDeviceInRegulatedArea;
        }

        private class RilHandler extends Handler {
            public RilHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                int msgID = msg.what;
                OppoNlpProxy oppoNlpProxy = OppoNlpProxy.this;
                oppoNlpProxy.logv("handleMessage what - " + msgID);
                if (msgID != OppoNlpProxy.MSG_CELL_LOCATION_CHANGED) {
                    OppoNlpProxy.this.loge("Unhandled message");
                } else {
                    RilListener.this.handleCellLocationChanged();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getMnc(int mncmccCombo, int digits) {
        int mnc = 0;
        if (digits == 6) {
            mnc = mncmccCombo % 1000;
        } else if (digits == 5) {
            mnc = mncmccCombo % 100;
        }
        logd("getMnc() - " + mnc);
        return mnc;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getMcc(int mncmccCombo, int digits) {
        int mcc = 0;
        if (digits == 6) {
            mcc = mncmccCombo / 1000;
        } else if (digits == 5) {
            mcc = mncmccCombo / 100;
        }
        logd("getMcc() - " + mcc);
        return mcc;
    }

    private boolean isNumeric(String strToCheck) {
        try {
            Double.parseDouble(strToCheck);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getExistNlpPackageName(int nlpArrayResId) {
        String[] nlpPackageNameList;
        String existPackageName = DEFAULT_EXIST_NLP_PACKAGE_NAME;
        Context context = this.mContext;
        if (!(context == null || (nlpPackageNameList = context.getResources().getStringArray(nlpArrayResId)) == null)) {
            for (String packageName : nlpPackageNameList) {
                if (!packageName.equals(GMS_PACKAGE_NAME) && !packageName.equals("com.android.location.fused") && checkPackageExists(packageName)) {
                    existPackageName = packageName;
                }
            }
        }
        return existPackageName;
    }

    private String getProductDefaultNlpName() {
        return SystemProperties.get(DEFAULT_NLP_NAME_PROPERTIES, "CN");
    }

    private int findTheNlpPackageResId(String packageName) {
        if (packageName.contains("amap")) {
            return this.mNlpAmapResId;
        }
        if (packageName.contains("baidu")) {
            return this.mNlpBaiduResId;
        }
        if (packageName.contains("tencent")) {
            return this.mNlpTencentResId;
        }
        return -1;
    }

    private int findLocalExistNlpPackage(int defaultResId, int defaultResIdArray) {
        try {
            String defaultNlpPackage = getProductDefaultNlpName();
            if (defaultNlpPackage == null || defaultNlpPackage.equals("CN") || !checkPackageExists(defaultNlpPackage)) {
                String defaultNlpPackage2 = this.mContext.getResources().getString(defaultResId);
                if (defaultNlpPackage2 == null || !checkPackageExists(defaultNlpPackage2)) {
                    String defaultNlpPackage3 = getExistNlpPackageName(defaultResIdArray);
                    if (defaultNlpPackage3 == null || defaultNlpPackage3.equals(DEFAULT_EXIST_NLP_PACKAGE_NAME)) {
                        return -1;
                    }
                    int defaultNlpResId = findTheNlpPackageResId(defaultNlpPackage3);
                    Log.v(TAG, "use exist package " + defaultNlpPackage3 + " as the region nlp");
                    return defaultNlpResId;
                }
                Log.v(TAG, "use default conf" + defaultNlpPackage2 + " as the region nlp");
                return defaultResId;
            }
            int defaultNlpResId2 = findTheNlpPackageResId(defaultNlpPackage);
            Log.v(TAG, "use product conf" + defaultNlpPackage + " as the region nlp");
            return defaultNlpResId2;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            return -1;
        }
    }

    private boolean isNeedDoubleRequest(boolean isChinaRom, LocationController activeController) {
        if (activeController == null || activeController.getNlpName() == null || !"gms".equals(activeController.getNlpName()) || !isChinaRom) {
            return false;
        }
        return true;
    }

    private void doubleRequestStart(LocationController gmsNlpController, LocationController chinaNlpController, ProviderRequest requests, WorkSource workSource) {
        if (gmsNlpController != null && chinaNlpController != null && requests != null && workSource != null) {
            logd("start doubleRequestStart");
            try {
                gmsNlpController.setRequest(requests, workSource);
                chinaNlpController.setRequest(requests, workSource);
                if (!workSource.isEmpty()) {
                    gmsNlpController.addFailCount();
                    chinaNlpController.addFailCount();
                }
                if (gmsNlpController.getFailCount() >= 3) {
                    this.mChinaNlpDelayMessageHandledFlag = true;
                } else if ((this.mLastWorkSource == null || this.mLastWorkSource.isEmpty()) && !workSource.isEmpty()) {
                    sendMessageForDelayChinaNlpReport(3000);
                }
                this.mLastWorkSource = workSource;
            } catch (NullPointerException npe) {
                Log.w(TAG, npe);
            }
        }
    }

    private void sendMessageForDelayChinaNlpReport(long delyTime) {
        Message msgObj = Message.obtain(this.mHandler, MSG_CHINA_NLP_DELAY, true);
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mChinaNlpDelayMessageHandledFlag = false;
            handler.sendMessageDelayed(msgObj, delyTime);
        }
    }

    private void removeMessageForDelayChinaNlpReport() {
        Handler handler = this.mHandler;
        if (handler != null && handler.hasMessages(MSG_CHINA_NLP_DELAY)) {
            this.mChinaNlpDelayMessageHandledFlag = true;
            this.mHandler.removeMessages(MSG_CHINA_NLP_DELAY);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessageForDelayChinaNlpReport() {
        Location location = this.mLastChinaNLpLocation;
        if (location != null) {
            this.mChinaNlpDelayMessageHandledFlag = true;
            trigerChinaNlpReportLocation(this.mRegionOSNpHandle, location);
            this.mLastChinaNLpLocation = null;
        }
    }

    private void trigerChinaNlpReportLocation(LocationController chinaNlpController, Location location) {
        if (chinaNlpController != null && location != null) {
            chinaNlpController.onReportLocation(location);
            logd("china nlp report in 3s,and GMS NLP not report in 3s");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean reportLocationPreHandle(String providerName, Location location) {
        boolean needReportFlag;
        if (!isNeedDoubleRequest(!isExpROM(), this.mActiveNpHandle)) {
            return true;
        }
        if (providerName == null || location == null) {
            return false;
        }
        if (providerName.equals("gms")) {
            removeMessageForDelayChinaNlpReport();
            this.mLastChinaNLpLocation = null;
            ProviderRequest locationRequest = new ProviderRequest();
            WorkSource workSource = new WorkSource();
            LocationController locationController = this.mRegionOSNpHandle;
            if (locationController != null) {
                locationController.setRequest(locationRequest, workSource);
            }
            LocationController locationController2 = this.mOSNpHandle;
            if (locationController2 == null) {
                return true;
            }
            locationController2.clearFailCount();
            return true;
        } else if (!providerName.equals("nlp")) {
            return false;
        } else {
            if (this.mChinaNlpDelayMessageHandledFlag) {
                needReportFlag = true;
                logd("GMS NLP not report,report NLP location to app");
            } else {
                needReportFlag = false;
                this.mLastChinaNLpLocation = location;
            }
            LocationController locationController3 = this.mRegionOSNpHandle;
            if (locationController3 == null) {
                return needReportFlag;
            }
            locationController3.clearFailCount();
            return needReportFlag;
        }
    }

    public static void setDebug(boolean isDebug) {
        DEBUG = isDebug;
    }

    public String getOppoNlpId() {
        return this.mOppoNlpID;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logv(String msg) {
        if (DEBUG) {
            Log.v(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String msg) {
        Log.e(TAG, msg);
    }
}
