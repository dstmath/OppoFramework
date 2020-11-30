package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.util.Range;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.interfaces.IPswSuplController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

public class OppoSuplController implements IPswSuplController {
    private static final int AGPS_CARRIER_SUPL_HOST_SWITCH = 16;
    private static final int AGPS_SUPL_HOST_SPEC = 4;
    private static final int AGPS_SUPL_HOST_SWITCH = 8;
    private static final int AGPS_SUPL_MODE_MSA = 2;
    private static final int AGPS_SUPL_MODE_MSB = 1;
    private static boolean DEBUG = false;
    private static final int DEVICE_IN_REGULATED_REGION_AT_EMBARGOED = 2;
    private static final int DEVICE_IN_REGULATED_REGION_AT_REGIONAL_NLP = 4;
    private static final int DEVICE_IN_REGULATED_REGION_KNOWN = 1;
    private static final int DEVICE_IN_REGULATED_REGION_UNKNOWN = 0;
    private static final int GPS_CAPABILITY_MSA = 4;
    private static final int GPS_CAPABILITY_MSB = 2;
    private static final String KEY_AGPS_REGION_SUPL_MCC = "config_regionSuplMcc";
    private static final String KEY_AGPS_REGION_SUPL_SID = "config_regionSuplSid";
    private static final String KEY_AGPS_SUPL_BEGION_REX = "config_suplConfig_";
    private static final String KEY_AGPS_SUPL_CONTROLLER = "config_agpsSuplContoller";
    private static final String KEY_REGION_PROPERTIES = "persist.sys.oppo.region";
    private static final int MSG_CELL_LOCATION_CHANGED = 103;
    private static final int MSG_INSTALL_RIL_LISTENER = 102;
    private static final int MSG_SERVICE_STATE_CHANGED = 104;
    private static final int MSG_SUPL_CONF_TEST = 105;
    private static final int MSG_UPDATE_SUPL_CONFIG = 101;
    private static final int NOT_READ_SYSTEM_PROPERTIES = 2;
    private static final String OPPO_LBS_CONFIG_UPDATE_ACTION = "com.android.location.oppo.lbsconfig.update.success";
    private static final String OPPO_SUPL_CONFIG_TEST_ACTION = "com.android.location.oppo.suplconfig.test";
    private static final String OPPO_SUPL_CONFIG_UPDATE_ACTION = "com.android.location.oppo.suplconfig.update.success";
    private static final int READ_SYSTEM_PROPERTIES = 1;
    private static final String TAG = "OppoSuplController";
    private static final String TEST_IMSI = "TEST_IMSI";
    private static final String TEST_MCC = "TEST_MCC";
    private static final String TEST_PHONE_TYPE = "TEST_PHONE_TYPE";
    private static final String TEST_REGION_NAME = "TEST_SUPL_CONF";
    private static final String TEST_SID = "TEST_SID";
    private static OppoSuplController mController = null;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.OppoSuplController.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (OppoSuplController.DEBUG) {
                Log.d(OppoSuplController.TAG, "receive broadcast intent, action: " + action);
            }
            if (action != null) {
                if (action.equals(OppoSuplController.OPPO_LBS_CONFIG_UPDATE_ACTION)) {
                    OppoSuplController oppoSuplController = OppoSuplController.this;
                    oppoSuplController.mPreDeviceInRegion = !oppoSuplController.mDeviceInRegion;
                    Message msgUpdateSuplConfig = Message.obtain(OppoSuplController.this.mHandler, (int) OppoSuplController.MSG_UPDATE_SUPL_CONFIG);
                    msgUpdateSuplConfig.arg1 = 1;
                    OppoSuplController.this.mHandler.sendMessage(msgUpdateSuplConfig);
                } else if (action.equals(OppoSuplController.OPPO_SUPL_CONFIG_TEST_ACTION)) {
                    OppoSuplController.this.mForTestEnabled = intent.getBooleanExtra(OppoSuplController.TEST_REGION_NAME, false);
                    OppoSuplController.this.mTestPhoneType = intent.getIntExtra(OppoSuplController.TEST_PHONE_TYPE, 0);
                    OppoSuplController.this.mTestSid = intent.getIntExtra(OppoSuplController.TEST_SID, 0);
                    OppoSuplController.this.mTestMcc = intent.getIntExtra(OppoSuplController.TEST_MCC, 0);
                    OppoSuplController.this.mTestImsi = intent.getStringExtra(OppoSuplController.TEST_IMSI);
                    Log.d(OppoSuplController.TAG, "get mForTestEnabled : " + OppoSuplController.this.mForTestEnabled + ", mTestPhoneType " + OppoSuplController.this.mTestPhoneType + ", mTestSid " + OppoSuplController.this.mTestSid + ", mTestMcc " + OppoSuplController.this.mTestMcc);
                    OppoSuplController.this.mHandler.sendMessage(Message.obtain(OppoSuplController.this.mHandler, (int) OppoSuplController.MSG_SUPL_CONF_TEST));
                }
            }
        }
    };
    private ArrayList<SuplConfigCallback> mCallbacks = new ArrayList<>();
    private boolean mCarrierHostSwitchEnabled = false;
    private ArrayList<SuplServiceConfig> mCarrierSuplConfigs = new ArrayList<>();
    private Context mContext = null;
    private int mCurrentServiceState = 1;
    private boolean mDeviceInRegion = isCnROM();
    private int mDeviceInRegulatedArea = 0;
    private int mEmergencyPdn = 0;
    private int mEsExtensionSe = 0;
    private boolean mForTestEnabled = false;
    private int mGpsLock = 0;
    private Handler mHandler = null;
    private boolean mHostSwitchEnabled = false;
    private boolean mMsaEnabled = false;
    private boolean mMsbEnabled = false;
    private boolean mPreDeviceInRegion = (!this.mDeviceInRegion);
    private HashMap<Integer, HashSet<Integer>> mRegionalNLPCountryList = new HashMap<>();
    private ArrayList<Range<Integer>> mRegionalNLPSIDRanges = new ArrayList<>();
    private RilListener mRilListener = null;
    private OppoLbsRomUpdateUtil mRomUpdateUtil = null;
    private boolean mSpecHostEnabled = false;
    private int mSuplEs = 0;
    private int mSuplGlonassProtocol = 0;
    private String mSuplHostName = "supl.google.com";
    private int mSuplHostPort = 7275;
    private int mSuplLppProfile = 0;
    private int mSuplMode = 0;
    private int mSuplVersion = 131072;
    private String mTelephonyImsi = null;
    private TelephonyManager mTelephonyMgr = null;
    private String mTestImsi = null;
    private int mTestMcc = 0;
    private int mTestPhoneType = 0;
    private int mTestSid = 0;
    private Handler.Callback m_handler_callback = new Handler.Callback() {
        /* class com.android.server.location.OppoSuplController.AnonymousClass2 */

        public boolean handleMessage(Message msg) {
            int msgID = msg.what;
            OppoSuplController oppoSuplController = OppoSuplController.this;
            oppoSuplController.logv("handleMessage what - " + msgID);
            switch (msgID) {
                case OppoSuplController.MSG_UPDATE_SUPL_CONFIG /* 101 */:
                    OppoSuplController.this.updateSuplConfig(msg.arg1 == 1);
                    break;
                case OppoSuplController.MSG_INSTALL_RIL_LISTENER /* 102 */:
                    OppoSuplController.this.installRilListener();
                    break;
                case OppoSuplController.MSG_CELL_LOCATION_CHANGED /* 103 */:
                    OppoSuplController.this.handleCellLocationChanged();
                    break;
                case OppoSuplController.MSG_SERVICE_STATE_CHANGED /* 104 */:
                    OppoSuplController.this.handleServiceStateChanged(msg.arg1);
                    break;
                case OppoSuplController.MSG_SUPL_CONF_TEST /* 105 */:
                    OppoSuplController.this.handleCellLocationChanged();
                    break;
                default:
                    OppoSuplController.this.loge("Unhandled message");
                    break;
            }
            return true;
        }
    };

    public interface SuplConfigCallback {
        void onConfigChanged(String str, int i, int i2);
    }

    public static OppoSuplController getInstaller(Context context) {
        Log.d(TAG, "on get OppoSuplController!");
        if (mController == null) {
            mController = new OppoSuplController(context);
        }
        return mController;
    }

    private OppoSuplController(Context context) {
        this.mContext = context;
        initValue();
    }

    public void updateProperties(Properties properties) {
        carrierChoose();
        Log.d(TAG, "Get GnssConfiguration from Opposuplcontroller.");
        properties.setProperty("SUPL_HOST", getSuplHostName());
        properties.setProperty("SUPL_PORT", StringUtils.EMPTY + getSuplHostPort());
        properties.setProperty("SUPL_MODE", StringUtils.EMPTY + getSuplMode());
        properties.setProperty("SUPL_VER", StringUtils.EMPTY + getSuplVersion());
        properties.setProperty("LPP_PROFILE", StringUtils.EMPTY + getSuplLppProfile());
        properties.setProperty("A_GLONASS_POS_PROTOCOL_SELECT", StringUtils.EMPTY + getSuplGlonassProtocol());
        properties.setProperty("ES_EXTENSION_SEC", StringUtils.EMPTY + getEsExtensionSe());
        properties.setProperty("SUPL_ES", StringUtils.EMPTY + getSuplEs());
        properties.setProperty("USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL", StringUtils.EMPTY + getEmergencyPdn());
        properties.setProperty("GPS_LOCK", StringUtils.EMPTY + getGpsLock());
    }

    public String getSuplHostName() {
        return this.mSuplHostName;
    }

    public int getSuplHostPort() {
        return this.mSuplHostPort;
    }

    public int getSuplMode() {
        return this.mSuplMode;
    }

    public int getSuplVersion() {
        return this.mSuplVersion;
    }

    public int getSuplLppProfile() {
        return this.mSuplLppProfile;
    }

    public int getSuplGlonassProtocol() {
        return this.mSuplGlonassProtocol;
    }

    public int getEsExtensionSe() {
        return this.mEsExtensionSe;
    }

    public int getSuplEs() {
        return this.mSuplEs;
    }

    public int getEmergencyPdn() {
        return this.mEmergencyPdn;
    }

    public int getGpsLock() {
        return this.mGpsLock;
    }

    public void registSuplCallback(SuplConfigCallback callback) {
        if (this.mCallbacks == null) {
            this.mCallbacks = new ArrayList<>();
        }
        if (callback == null) {
            loge("input callback is null!!!");
        } else if (this.mCallbacks.contains(callback)) {
            logd("This callback has been include, don't need add again!");
        } else {
            this.mCallbacks.add(callback);
        }
    }

    public int getGnssCapabilities(int capabilities) {
        int capabilities2;
        if (this.mMsaEnabled) {
            capabilities2 = capabilities | 4;
        } else {
            capabilities2 = capabilities & -5;
        }
        if (this.mMsbEnabled) {
            return capabilities2 | 2;
        }
        return capabilities2 & -3;
    }

    private void handleSuplConfigChanged() {
        ArrayList<SuplConfigCallback> arrayList = this.mCallbacks;
        if (arrayList == null) {
            loge("No callbacks haven't been regist!");
            return;
        }
        Iterator<SuplConfigCallback> it = arrayList.iterator();
        while (it.hasNext()) {
            it.next().onConfigChanged(this.mSuplHostName, this.mSuplHostPort, this.mSuplMode);
        }
    }

    public static void setDebug(boolean isDebug) {
        DEBUG = isDebug;
    }

    private void initValue() {
        this.mRomUpdateUtil = OppoLbsRomUpdateUtil.getInstall(this.mContext);
        HandlerThread localThread = new HandlerThread("FastNetworkLocation");
        localThread.start();
        this.mHandler = new Handler(localThread.getLooper(), this.m_handler_callback);
        this.mHandler.sendMessage(Message.obtain(this.mHandler, (int) MSG_INSTALL_RIL_LISTENER));
        Message msgUpdateSuplConfig = Message.obtain(this.mHandler, (int) MSG_UPDATE_SUPL_CONFIG);
        msgUpdateSuplConfig.arg1 = 1;
        this.mHandler.sendMessage(msgUpdateSuplConfig);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OPPO_LBS_CONFIG_UPDATE_ACTION);
        intentFilter.addAction(OPPO_SUPL_CONFIG_TEST_ACTION);
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    private static boolean isCnROM() {
        return SystemProperties.get(KEY_REGION_PROPERTIES, "CN").equalsIgnoreCase("CN");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSuplConfig(boolean needReadUpdateFile) {
        if (needReadUpdateFile) {
            int agpsSuplController = this.mRomUpdateUtil.getInt(KEY_AGPS_SUPL_CONTROLLER);
            boolean z = false;
            this.mMsaEnabled = (agpsSuplController & 2) != 0;
            this.mMsbEnabled = (agpsSuplController & 1) != 0;
            this.mSuplMode = agpsSuplController & -29;
            this.mSpecHostEnabled = (agpsSuplController & 4) != 0;
            this.mHostSwitchEnabled = this.mSpecHostEnabled && (agpsSuplController & AGPS_SUPL_HOST_SWITCH) != 0;
            if (this.mSpecHostEnabled && (agpsSuplController & AGPS_CARRIER_SUPL_HOST_SWITCH) != 0) {
                z = true;
            }
            this.mCarrierHostSwitchEnabled = z;
            populateCountryList(this.mRegionalNLPCountryList, this.mRomUpdateUtil.getString(KEY_AGPS_REGION_SUPL_MCC));
            logd("OSAgent Regional NLP MCC: " + this.mRegionalNLPCountryList.toString());
            populateSIDRanges(this.mRegionalNLPSIDRanges, this.mRomUpdateUtil.getString(KEY_AGPS_REGION_SUPL_SID));
            logd("OSAgent Regional NLP SID: " + this.mRegionalNLPSIDRanges.toString());
            populateSuplList(this.mCarrierSuplConfigs);
        }
        boolean hasGetReserve = false;
        Iterator<SuplServiceConfig> it = this.mCarrierSuplConfigs.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            SuplServiceConfig supl = it.next();
            if (supl.isChoose(this.mDeviceInRegion, this.mHostSwitchEnabled, this.mCarrierHostSwitchEnabled, this.mTelephonyImsi, this.mSuplMode)) {
                this.mSuplHostName = supl.getSuplServer();
                this.mSuplHostPort = supl.getSuplPort();
                this.mSuplVersion = supl.getSuplVer();
                this.mSuplLppProfile = supl.getSuplLPP();
                this.mSuplGlonassProtocol = supl.getSuplGlonass();
                this.mEsExtensionSe = supl.getSuplExtensionSe();
                this.mSuplEs = supl.getSuplEsEnable();
                this.mEmergencyPdn = supl.getSuplEmergencyPdn();
                this.mGpsLock = supl.getGpsLock();
                break;
            } else if (this.mCarrierHostSwitchEnabled && !hasGetReserve && supl.isChoose(this.mDeviceInRegion, this.mHostSwitchEnabled, false, this.mTelephonyImsi, this.mSuplMode)) {
                this.mSuplHostName = supl.getSuplServer();
                this.mSuplHostPort = supl.getSuplPort();
                this.mSuplVersion = supl.getSuplVer();
                this.mSuplLppProfile = supl.getSuplLPP();
                this.mSuplGlonassProtocol = supl.getSuplGlonass();
                this.mEsExtensionSe = supl.getSuplExtensionSe();
                this.mSuplEs = supl.getSuplEsEnable();
                this.mEmergencyPdn = supl.getSuplEmergencyPdn();
                this.mGpsLock = supl.getGpsLock();
                hasGetReserve = true;
            }
        }
        handleSuplConfigChanged();
        if (DEBUG) {
            Log.d(TAG, "mSuplHostName : " + this.mSuplHostName + ", mSuplHostPort : " + this.mSuplHostPort + ", mSuplMode : " + this.mSuplMode + ", mMsaEnabled : " + this.mMsaEnabled + ", mMsbEnabled : " + this.mMsbEnabled + ", mSpecHostEnabled : " + this.mSpecHostEnabled + ", mHostSwitchEnabled : " + this.mHostSwitchEnabled + ", mCarrierHostSwitchEnabled : " + this.mCarrierHostSwitchEnabled + ", mSuplVersion: " + this.mSuplVersion + ", mSuplLppProfile: " + this.mSuplLppProfile + ", mSuplGlonassProtocol: " + this.mSuplGlonassProtocol + ", mEsExtensionSe: " + this.mEsExtensionSe + ", mSuplEs: " + this.mSuplEs + ", mEmergencyPdn: " + this.mEmergencyPdn + ", mGpsLock: " + this.mGpsLock);
        }
    }

    private void sendSuplConfigUpdateBroadcast() {
        logd("Will send udate broadcast!");
        Intent intent = new Intent();
        intent.setAction(OPPO_SUPL_CONFIG_UPDATE_ACTION);
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void installRilListener() {
        this.mTelephonyMgr = (TelephonyManager) this.mContext.getSystemService("phone");
        if (this.mTelephonyMgr == null) {
            loge("Unable to get TELEPHONY_SERVICE");
            return;
        }
        this.mRilListener = new RilListener();
        this.mTelephonyMgr.listen(this.mRilListener, 17);
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
                    loge("OsAgent: Error in reading SID range" + sidRangeStr + ":" + e4.toString());
                }
            }
        } catch (PatternSyntaxException e5) {
            loge("OsAgent: Error in reading configurations:" + e5.toString());
        } catch (NullPointerException e6) {
            loge("OsAgent: configSidRangeList is null:" + e6.toString());
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
                        mapMccMnc2.put(Integer.valueOf(Integer.parseInt(mccStr.trim())), new HashSet<>());
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
            loge("OsAgent: configMccList is null:" + e14.toString());
        }
    }

    private void populateSuplList(ArrayList<SuplServiceConfig> suplList) {
        if (suplList == null) {
            suplList = new ArrayList<>();
        } else {
            suplList.clear();
        }
        int suplListLength = 0;
        Iterator<ArrayList<String>> it = this.mRomUpdateUtil.getMatchStringArray(KEY_AGPS_SUPL_BEGION_REX).iterator();
        while (it.hasNext()) {
            SuplServiceConfig supl = new SuplServiceConfig(it.next());
            if (suplListLength <= 0 || !"Other".equals(suplList.get(suplListLength - 1).getConfigName())) {
                suplList.add(supl);
            } else {
                suplList.add(suplListLength - 1, supl);
            }
            suplListLength++;
        }
    }

    private int isDeviceInRegulatedArea() {
        HashSet<Integer> mncs;
        int deviceInRegulatedArea = 0;
        int currentServiceState = this.mCurrentServiceState;
        if (this.mForTestEnabled) {
            Log.d(TAG, "--change service state to in service!!");
            this.mCurrentServiceState = 0;
        }
        if (this.mCurrentServiceState == 0) {
            int iMcc = 0;
            int iMnc = 0;
            int iSid = 0;
            int phoneType = this.mTelephonyMgr.getPhoneType();
            logv("Before computing is current n/w state regulated area:  " + this.mDeviceInRegulatedArea);
            if (this.mForTestEnabled) {
                phoneType = this.mTestPhoneType;
                Log.d(TAG, "--change phoneType to : " + phoneType);
            }
            if (phoneType != 0) {
                if (phoneType == 1) {
                    String strNetworkOperator = this.mTelephonyMgr.getNetworkOperator();
                    if (strNetworkOperator != null && !strNetworkOperator.isEmpty()) {
                        iMcc = getMcc(Integer.parseInt(strNetworkOperator), strNetworkOperator.length());
                        iMnc = getMnc(Integer.parseInt(strNetworkOperator), strNetworkOperator.length());
                        this.mTelephonyImsi = String.format("%d%02d", Integer.valueOf(iMcc), Integer.valueOf(iMnc));
                    }
                    if (this.mForTestEnabled) {
                        iMcc = this.mTestMcc;
                        this.mTelephonyImsi = this.mTestImsi;
                        logd("--running GMS  iMcc " + iMcc);
                    }
                    if (iMcc == 0) {
                        logv("isDeviceInRegulatedArea: MCC is zero");
                    } else {
                        deviceInRegulatedArea = 1;
                        if (this.mRegionalNLPCountryList.containsKey(Integer.valueOf(iMcc)) && ((mncs = this.mRegionalNLPCountryList.get(Integer.valueOf(iMcc))) == null || mncs.contains(Integer.valueOf(iMnc)))) {
                            deviceInRegulatedArea = 1 | 4;
                        }
                    }
                } else if (phoneType == 2) {
                    CellLocation location = this.mTelephonyMgr.getCellLocation();
                    if (location instanceof CdmaCellLocation) {
                        iSid = ((CdmaCellLocation) location).getSystemId();
                    }
                    if (this.mForTestEnabled) {
                        iSid = this.mTestSid;
                    }
                    if (iSid != 0) {
                        deviceInRegulatedArea = 1;
                        Iterator<Range<Integer>> it = this.mRegionalNLPSIDRanges.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (it.next().contains((Range<Integer>) Integer.valueOf(iSid))) {
                                    deviceInRegulatedArea = 1 | 4;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        logd("isDeviceInRegulatedArea: Sid is zero");
                    }
                }
            }
            logv("After computing is n/w state in regulated area?, " + deviceInRegulatedArea);
        }
        if (deviceInRegulatedArea == 0) {
            logv("isDeviceInRegulatedArea: unknown - keeping previous state");
            deviceInRegulatedArea = this.mDeviceInRegulatedArea;
        }
        if (this.mForTestEnabled) {
            this.mCurrentServiceState = currentServiceState;
        }
        return deviceInRegulatedArea;
    }

    public void carrierChoose() {
        int iMcc;
        boolean deviceInRegulatedArea;
        int iMcc2;
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int ddSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        String mccMnc = SubscriptionManager.isValidSubscriptionId(ddSubId) ? phone.getSimOperator(ddSubId) : phone.getSimOperator();
        if (!TextUtils.isEmpty(mccMnc)) {
            if (DEBUG) {
                Log.d(TAG, "SIM MCC/MNC is available: " + mccMnc + " ddSubId: " + ddSubId + " mccMnc.length(): " + mccMnc.length());
            }
            try {
                iMcc = getMcc(Integer.parseInt(mccMnc), mccMnc.length());
            } catch (NumberFormatException e) {
                loge("carrierChoose NumberFormatException: iMcc:" + e.toString());
                iMcc = 0;
            }
            boolean z = this.mDeviceInRegion;
            if (this.mRegionalNLPCountryList.containsKey(Integer.valueOf(iMcc))) {
                deviceInRegulatedArea = true;
            } else {
                deviceInRegulatedArea = false;
            }
            Iterator<SuplServiceConfig> it = this.mCarrierSuplConfigs.iterator();
            boolean hasGetReserve = false;
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                SuplServiceConfig supl = it.next();
                if (supl.isChoose(deviceInRegulatedArea, this.mHostSwitchEnabled, this.mCarrierHostSwitchEnabled, mccMnc, this.mSuplMode)) {
                    this.mSuplHostName = supl.getSuplServer();
                    this.mSuplHostPort = supl.getSuplPort();
                    this.mSuplVersion = supl.getSuplVer();
                    this.mSuplLppProfile = supl.getSuplLPP();
                    this.mSuplGlonassProtocol = supl.getSuplGlonass();
                    this.mEsExtensionSe = supl.getSuplExtensionSe();
                    this.mSuplEs = supl.getSuplEsEnable();
                    this.mEmergencyPdn = supl.getSuplEmergencyPdn();
                    this.mGpsLock = supl.getGpsLock();
                    if (DEBUG) {
                        Log.d(TAG, "carrierChoose " + mccMnc + " has been choosed!! ");
                    }
                } else {
                    if (!this.mCarrierHostSwitchEnabled || hasGetReserve) {
                        iMcc2 = iMcc;
                    } else {
                        iMcc2 = iMcc;
                        if (supl.isChoose(this.mDeviceInRegion, this.mHostSwitchEnabled, false, mccMnc, this.mSuplMode)) {
                            this.mSuplHostName = supl.getSuplServer();
                            this.mSuplHostPort = supl.getSuplPort();
                            this.mSuplVersion = supl.getSuplVer();
                            this.mSuplLppProfile = supl.getSuplLPP();
                            this.mSuplGlonassProtocol = supl.getSuplGlonass();
                            this.mEsExtensionSe = supl.getSuplExtensionSe();
                            this.mSuplEs = supl.getSuplEsEnable();
                            this.mEmergencyPdn = supl.getSuplEmergencyPdn();
                            this.mGpsLock = supl.getGpsLock();
                            if (DEBUG) {
                                Log.d(TAG, "carrierChoose regin" + mccMnc + " has been choosed!! " + toString());
                            }
                            hasGetReserve = true;
                        }
                    }
                    iMcc = iMcc2;
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "carrierChoose final" + mccMnc + " has been choosed!! mSuplHostName:" + this.mSuplHostName + " mSuplHostPort:" + this.mSuplHostPort + " mSuplVersion:" + this.mSuplVersion + " mSuplLppProfile:" + this.mSuplLppProfile + " mSuplGlonassProtocol:" + this.mSuplGlonassProtocol + " mEsExtensionSe:" + this.mEsExtensionSe + " mSuplEs:" + this.mSuplEs + " mEmergencyPdn:" + this.mEmergencyPdn + " mGpsLock:" + this.mGpsLock);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceStateChanged(int service_state) {
        if (this.mCurrentServiceState != service_state) {
            this.mCurrentServiceState = service_state;
            handleCellLocationChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCellLocationChanged() {
        int isDevInRegulatedArea = isDeviceInRegulatedArea();
        if (this.mCarrierHostSwitchEnabled || this.mDeviceInRegulatedArea != isDevInRegulatedArea) {
            this.mDeviceInRegulatedArea = isDevInRegulatedArea;
            if ((this.mDeviceInRegulatedArea & 4) != 0) {
                this.mDeviceInRegion = true;
            } else {
                this.mDeviceInRegion = false;
            }
            Message msgUpdateSuplConfig = Message.obtain(this.mHandler, (int) MSG_UPDATE_SUPL_CONFIG);
            msgUpdateSuplConfig.arg1 = 2;
            this.mHandler.sendMessage(msgUpdateSuplConfig);
        }
    }

    /* access modifiers changed from: private */
    public final class RilListener extends PhoneStateListener {
        private RilListener() {
        }

        public void onCellLocationChanged(CellLocation location) {
            OppoSuplController oppoSuplController = OppoSuplController.this;
            oppoSuplController.logd("deal cellLocationChanged " + location);
            OppoSuplController.this.mHandler.sendEmptyMessage(OppoSuplController.MSG_CELL_LOCATION_CHANGED);
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            OppoSuplController oppoSuplController = OppoSuplController.this;
            oppoSuplController.logd("CurrentServiceState = " + OppoSuplController.this.mCurrentServiceState + " NewVoiceServiceState = " + serviceState.getVoiceRegState() + " NewDataServiceState = " + serviceState.getDataRegState());
            if (serviceState.getVoiceRegState() == 0 || serviceState.getDataRegState() == 0) {
                Message msgServiceStateChanged = Message.obtain(OppoSuplController.this.mHandler, (int) OppoSuplController.MSG_SERVICE_STATE_CHANGED);
                msgServiceStateChanged.arg1 = 0;
                OppoSuplController.this.mHandler.sendMessage(msgServiceStateChanged);
                return;
            }
            Message msgServiceStateChanged2 = Message.obtain(OppoSuplController.this.mHandler, (int) OppoSuplController.MSG_SERVICE_STATE_CHANGED);
            msgServiceStateChanged2.arg1 = 1;
            OppoSuplController.this.mHandler.sendMessage(msgServiceStateChanged2);
        }
    }

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
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    public class SuplServiceConfig {
        private String KEY_CONFIG_NAME = "CONFIG_NAME";
        private String KEY_GPS_LOCK = "GPS_LOCK";
        private String KEY_SUPL_A_GLONASS = "A_GLONASS_POS_PROTOCOL_SELECT";
        private String KEY_SUPL_CARRIER = "SUPL_CARRIER";
        private String KEY_SUPL_EMERGENCY = "USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL";
        private String KEY_SUPL_ES = "SUPL_ES";
        private String KEY_SUPL_ES_EXTENSION_SE = "ES_EXTENSION_SE";
        private String KEY_SUPL_HOST = "SUPL_HOST";
        private String KEY_SUPL_LPP = "LPP_PROFILE";
        private String KEY_SUPL_MODE = "SUPL_MODE";
        private String KEY_SUPL_PORT = "SUPL_PORT";
        private String KEY_SUPL_REGION = "SUPL_REGION";
        private String KEY_SUPL_SWITCH = "SUPL_SWITCH";
        private String KEY_SUPL_VER = "SUPL_VER";
        private String mConfigName = null;
        private int mGpsLock = 0;
        private int mNeedSuplMode = 0;
        private boolean mRegionSupl = false;
        private String mSuplCarrier = null;
        private int mSuplEmergencyPdn = 0;
        private int mSuplEsEnable = 0;
        private int mSuplExtensionSe = 0;
        private int mSuplGlonass = 0;
        private int mSuplLPP = 0;
        private int mSuplPort = 0;
        private String mSuplServer = null;
        private boolean mSuplSwitchEnable = false;
        private int mSuplVer = 131072;

        public SuplServiceConfig() {
        }

        public SuplServiceConfig(String descript) {
        }

        public SuplServiceConfig(ArrayList<String> configs) {
            Iterator<String> it = configs.iterator();
            while (it.hasNext()) {
                String[] tmp = it.next().split("=");
                if (tmp[0].equals(this.KEY_CONFIG_NAME)) {
                    this.mConfigName = tmp[1];
                } else if (tmp[0].equals(this.KEY_SUPL_REGION)) {
                    this.mRegionSupl = tmp[1].equals("1");
                } else if (tmp[0].equals(this.KEY_SUPL_SWITCH)) {
                    this.mSuplSwitchEnable = tmp[1].equals("1");
                } else if (tmp[0].equals(this.KEY_SUPL_CARRIER)) {
                    this.mSuplCarrier = tmp[1];
                } else if (tmp[0].equals(this.KEY_SUPL_HOST)) {
                    this.mSuplServer = tmp[1];
                } else if (tmp[0].equals(this.KEY_SUPL_PORT)) {
                    try {
                        this.mSuplPort = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e) {
                    }
                } else if (tmp[0].equals(this.KEY_SUPL_VER)) {
                    try {
                        this.mSuplVer = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e2) {
                    }
                } else if (tmp[0].equals(this.KEY_SUPL_MODE)) {
                    try {
                        this.mNeedSuplMode = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e3) {
                    }
                } else if (tmp[0].equals(this.KEY_SUPL_ES)) {
                    try {
                        this.mSuplEsEnable = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e4) {
                    }
                } else if (tmp[0].equals(this.KEY_SUPL_LPP)) {
                    try {
                        this.mSuplLPP = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e5) {
                    }
                } else if (tmp[0].equals(this.KEY_SUPL_A_GLONASS)) {
                    try {
                        this.mSuplGlonass = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e6) {
                    }
                } else if (tmp[0].equals(this.KEY_SUPL_ES_EXTENSION_SE)) {
                    try {
                        this.mSuplExtensionSe = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e7) {
                    }
                } else if (tmp[0].equals(this.KEY_SUPL_EMERGENCY)) {
                    try {
                        this.mSuplEmergencyPdn = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e8) {
                    }
                } else if (tmp[0].equals(this.KEY_GPS_LOCK)) {
                    try {
                        this.mGpsLock = Integer.parseInt(tmp[1]);
                    } catch (NumberFormatException e9) {
                    }
                }
            }
        }

        public String getSuplServer() {
            return this.mSuplServer;
        }

        public int getSuplPort() {
            return this.mSuplPort;
        }

        public String getConfigName() {
            return this.mConfigName;
        }

        public int getSuplLPP() {
            return this.mSuplLPP;
        }

        public int getSuplVer() {
            return this.mSuplVer;
        }

        public int getSuplGlonass() {
            return this.mSuplGlonass;
        }

        public int getSuplExtensionSe() {
            return this.mSuplExtensionSe;
        }

        public int getSuplEsEnable() {
            return this.mSuplEsEnable;
        }

        public int getGpsLock() {
            return this.mGpsLock;
        }

        public int getSuplEmergencyPdn() {
            return this.mSuplEmergencyPdn;
        }

        public boolean isChoose(boolean region, boolean switchEnable, boolean carrierSupport, String carrier, int suplMode) {
            boolean isChoosed = false;
            int i = this.mNeedSuplMode;
            if (i == (suplMode & i)) {
                if (carrierSupport) {
                    if (carrier != null && carrier.equals(this.mSuplCarrier)) {
                        isChoosed = true;
                        if (OppoSuplController.DEBUG) {
                            Log.d(OppoSuplController.TAG, "Carrier " + carrier + " has been choosed!! " + toString());
                        }
                    }
                } else if (switchEnable == this.mSuplSwitchEnable && region == this.mRegionSupl) {
                    isChoosed = true;
                    if (OppoSuplController.DEBUG) {
                        Log.d(OppoSuplController.TAG, "Region " + region + " has been choosed!! " + toString());
                    }
                } else if (!this.mRegionSupl && this.mSuplSwitchEnable == switchEnable && this.mSuplCarrier.equals("0")) {
                    isChoosed = true;
                    if (OppoSuplController.DEBUG) {
                        Log.d(OppoSuplController.TAG, "Else path has been choosed!! " + toString());
                    }
                }
            }
            return isChoosed;
        }

        public String toString() {
            return "ConfigName " + this.mConfigName + ", SuplServer : " + this.mSuplServer + ", port : " + this.mSuplPort + ", mRegionSupl : " + this.mRegionSupl + ",  mSuplSwitchEnable : " + this.mSuplSwitchEnable + ", mSuplEsEnable : " + this.mSuplEsEnable + ", mSuplCarrier : " + this.mSuplCarrier + ",  mSuplVersion : " + this.mSuplVer + ", mSuplLPP : " + this.mSuplLPP + ", mSuplGlonass" + this.mSuplGlonass + ",mSuplExtensionSe" + this.mSuplExtensionSe + ",mSuplEmergencyPdn" + this.mSuplEmergencyPdn;
        }
    }
}
