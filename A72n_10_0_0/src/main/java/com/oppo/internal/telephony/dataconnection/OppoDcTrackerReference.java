package com.oppo.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.data.ApnSetting;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractServiceStateTracker;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.telephony.dataconnection.AbstractDcTracker;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSettingUtils;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.dataconnection.DataEnabledSettings;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.IOppoDcTracker;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.OppoRIL;
import com.oppo.internal.telephony.OppoServiceStateTracker;
import com.oppo.internal.telephony.OppoTelephonyController;
import com.oppo.internal.telephony.OppoUiccManagerImpl;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import com.oppo.internal.telephony.utils.ConnectivityManagerHelper;
import com.oppo.internal.telephony.utils.OppoManagerHelper;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OppoDcTrackerReference implements IOppoDcTracker {
    private static final String ACTION_COMMAND_FORCE_DISABLE_ENDC = "android.intent.force_disable_endc";
    public static final int CMD_DELAY_SETUP_DATA = 103;
    public static final int CMD_RELEASE_WLAN_ASS = 104;
    private static final String COLOR_DATA_ROAMING_TYPE = "color_data_roaming_type";
    private static final int COLOR_DATA_ROAMING_TYPE_DOMESTIC = 2;
    private static final int COLOR_DATA_ROAMING_TYPE_INTERNATIONAL = 1;
    private static final int COLOR_DATA_ROAMING_TYPE_NOT_FOUND = 0;
    private static final String DATA_EVENT_ID = "050401";
    private static final int DATA_EVENT_IMSI_READY = 100;
    private static final int EVENT_OEM_SCREEN_CHANGED = 1;
    private static final boolean VDBG_STALL = false;
    private static final String WIFI_SCORE_CHANGE = "android.net.wifi.WIFI_SCORE_CHANGE";
    public static boolean mDelayMeasure = false;
    public static int mLastDataRadioTech = 0;
    public static ConnectivityManager.NetworkCallback mMeasureDCCallback;
    public static boolean mMeasureDataState = false;
    protected static boolean mOppoCtaSupport = false;
    public static boolean mVsimIgnoreUserDataSetting = false;
    public static Map<Integer, Integer> sSlotIndexToIsNewSim = new ConcurrentHashMap();
    protected static final String[] sTelstraOperaters = {"50501", "50511", "50571", "50572"};
    private final int LINGER_TIMER = OppoServiceStateTracker.DELAYTIME_3S;
    private String LOG_TAG = "OppoDcTracker";
    private final int RETRY_TIMES = 3;
    private final int WAITTING_TIMEOUT = 60;
    private AlarmManager mAlarmManager;
    private ConnectivityManager mCm;
    private int mDataType = 0;
    private DcTracker mDcTracker;
    IntentFilter mFilter;
    private Handler mHandler = new Handler() {
        /* class com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference.AnonymousClass2 */

        public void handleMessage(Message msg) {
            OppoDcTrackerReference oppoDcTrackerReference = OppoDcTrackerReference.this;
            oppoDcTrackerReference.logd("handleMessage msg.what=" + msg.what);
            AbstractDcTracker tmpDcTracker = (AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, OppoDcTrackerReference.this.mDcTracker);
            int i = msg.what;
            String imsi = "";
            if (i == 1) {
                IccRecords r = OppoDcTrackerReference.this;
                r.mIsOppoScreenOn = OppoTelephonyController.getInstance(r.mPhone.getContext()).isScreenOn();
                OppoDcTrackerReference oppoDcTrackerReference2 = OppoDcTrackerReference.this;
                oppoDcTrackerReference2.logd("EVENT_OEM_SCREEN_CHANGED mIsOppoScreenOn: " + OppoDcTrackerReference.this.mIsOppoScreenOn);
                String sPlatformName = SystemProperties.get("ro.hardware", imsi);
                OppoDcTrackerReference oppoDcTrackerReference3 = OppoDcTrackerReference.this;
                oppoDcTrackerReference3.logd("sPlatformName=" + sPlatformName + ",mIsOppoScreenOn=" + OppoDcTrackerReference.this.mIsOppoScreenOn + ",mOppoModemKeyLogState=" + OppoDcTrackerReference.this.mOppoModemKeyLogState + ",mOppoFirstcommingModemKeyLog=" + OppoDcTrackerReference.this.mOppoFirstcommingModemKeyLog);
                if (!OppoDcTrackerReference.this.mIsOppoScreenOn && (OppoDcTrackerReference.this.mOppoModemKeyLogState || OppoDcTrackerReference.this.mOppoFirstcommingModemKeyLog)) {
                    if (sPlatformName.equals("mt6779") || sPlatformName.equals("mt6885") || sPlatformName.equals("mt6873") || sPlatformName.equals("mt6853")) {
                        OppoDcTrackerReference.this.mPhone.invokeOemRilRequestStrings(new String[]{"AT+EDMFAPP=2,2", imsi}, (Message) null);
                    }
                    if (sPlatformName.equals(imsi)) {
                        OppoDcTrackerReference.this.mPhone.invokeOemRilRequestStrings(new String[]{"AT+EDMFAPP=2,2", imsi}, (Message) null);
                    }
                    OppoDcTrackerReference oppoDcTrackerReference4 = OppoDcTrackerReference.this;
                    oppoDcTrackerReference4.mOppoModemKeyLogState = false;
                    oppoDcTrackerReference4.mOppoFirstcommingModemKeyLog = false;
                } else if (OppoDcTrackerReference.this.mIsOppoScreenOn && (!OppoDcTrackerReference.this.mOppoModemKeyLogState || OppoDcTrackerReference.this.mOppoFirstcommingModemKeyLog)) {
                    if (sPlatformName.equals("mt6779") || sPlatformName.equals("mt6885") || sPlatformName.equals("mt6873") || sPlatformName.equals("mt6853")) {
                        OppoDcTrackerReference.this.mPhone.invokeOemRilRequestStrings(new String[]{"AT+EDMFAPP=2,1", imsi}, (Message) null);
                    }
                    OppoDcTrackerReference oppoDcTrackerReference5 = OppoDcTrackerReference.this;
                    oppoDcTrackerReference5.mOppoModemKeyLogState = true;
                    oppoDcTrackerReference5.mOppoFirstcommingModemKeyLog = false;
                }
                tmpDcTracker.refreshNetStat(OppoDcTrackerReference.this.mIsOppoScreenOn);
            } else if (i == OppoDcTrackerReference.DATA_EVENT_IMSI_READY) {
                IccRecords r2 = (IccRecords) tmpDcTracker.getIccRecords().get();
                if (r2 != null) {
                    imsi = r2.getIMSI();
                }
                OppoDcTrackerReference.this.oemInitDataRoamingEnabledType(imsi);
            } else if (i == 103) {
                OppoDcTrackerReference.mDelayMeasure = false;
                OppoDcTrackerReference.this.logd("WLAN+ handlemessage CMD_DELAY_SETUP_DATA mDelayMeasure:false");
                tmpDcTracker.setupDataOnAllConnectableApns("2GVoiceCallEnded");
            } else if (i != 104) {
                switch (i) {
                    case 270343:
                        OppoDcTrackerReference.this.oppoWlanAssistantDelayMeasure();
                        OppoDcTrackerReference.this.notifyDataConnectionOnVoiceCallStateChange("2GVoiceCallStarted");
                        return;
                    case 270344:
                        OppoDcTrackerReference.this.oppoWlanAssistantDelaySetupData();
                        OppoDcTrackerReference.this.notifyDataConnectionOnVoiceCallStateChange("2GVoiceCallEnded");
                        return;
                    case 270345:
                    default:
                        return;
                }
            } else {
                OppoDcTrackerReference.this.releaseWlanAssistantRequest();
            }
        }
    };
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final AbstractDcTracker tmpDcTracker = (AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, OppoDcTrackerReference.this.mDcTracker);
            boolean enabled = true;
            if (action.equals("android.net.wifi.STATE_CHANGE")) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                OppoDcTrackerReference oppoDcTrackerReference = OppoDcTrackerReference.this;
                if (networkInfo == null || !networkInfo.isConnected()) {
                    enabled = false;
                }
                oppoDcTrackerReference.mIsWifiConnected = enabled;
                if (networkInfo != null) {
                    NetworkInfo.DetailedState state = networkInfo.getDetailedState();
                    OppoDcTrackerReference.this.logd("NETWORK_STATE_CHANGED_ACTION: mIsWifiConnected=" + OppoDcTrackerReference.this.mIsWifiConnected + " state:" + state + " mWifiOldState:" + OppoDcTrackerReference.this.mWifiOldState);
                    if (OppoPhoneUtil.getWlanAssistantEnable(OppoDcTrackerReference.this.mPhone.getContext())) {
                        if (!OppoDcTrackerReference.this.mIsWifiConnected && state == NetworkInfo.DetailedState.DISCONNECTED) {
                            OppoDcTrackerReference.mMeasureDataState = false;
                            ConnectivityManagerHelper.shouldKeepCelluarNetwork(OppoDcTrackerReference.this.mCm, OppoDcTrackerReference.mMeasureDataState);
                            if (OppoDcTrackerReference.this.isUserDataEnabled()) {
                                OppoDcTrackerReference.this.logd("WLAN+ NETWORK_STATE_CHANGED_ACTION sendMessageDelayed");
                                OppoDcTrackerReference.this.mHandler.sendMessageDelayed(OppoDcTrackerReference.this.mHandler.obtainMessage(104), 500);
                            } else {
                                OppoDcTrackerReference.this.releaseWlanAssistantRequest();
                            }
                        } else if (OppoDcTrackerReference.this.mWifiOldState != NetworkInfo.DetailedState.CONNECTED && state == NetworkInfo.DetailedState.CONNECTED) {
                            OppoDcTrackerReference.this.mWifiConnectTimeStamp = SystemClock.elapsedRealtime();
                            OppoDcTrackerReference.this.releaseWlanAssistantRequest();
                        }
                    }
                    if (state == NetworkInfo.DetailedState.DISCONNECTED && OppoDcTrackerReference.this.mWifiOldState == NetworkInfo.DetailedState.CONNECTED && (OppoDcTrackerReference.this.isUserDataEnabled() || OppoDcTrackerReference.this.haveVsimIgnoreUserDataSetting())) {
                        tmpDcTracker.setupDataOnAllConnectableApns("android.net.wifi.STATE_CHANGE");
                    }
                    OppoDcTrackerReference.this.mWifiOldState = state;
                }
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                OppoDcTrackerReference.this.logd("Wifi state changed");
                if (intent.getIntExtra("wifi_state", 4) != 3) {
                    enabled = false;
                }
                if (!enabled) {
                    OppoDcTrackerReference.this.mIsWifiConnected = false;
                }
                OppoDcTrackerReference.this.logd("WIFI_STATE_CHANGED_ACTION: enabled=" + enabled + " mIsWifiConnected=" + OppoDcTrackerReference.this.mIsWifiConnected);
                if (OppoPhoneUtil.getWlanAssistantEnable(OppoDcTrackerReference.this.mPhone.getContext()) && !enabled) {
                    OppoDcTrackerReference.mMeasureDataState = false;
                    ConnectivityManagerHelper.shouldKeepCelluarNetwork(OppoDcTrackerReference.this.mCm, OppoDcTrackerReference.mMeasureDataState);
                    if (OppoDcTrackerReference.this.isUserDataEnabled()) {
                        OppoDcTrackerReference.this.logd("WLAN+ WIFI_STATE_CHANGED_ACTION sendMessageDelayed");
                        OppoDcTrackerReference.this.mHandler.sendMessageDelayed(OppoDcTrackerReference.this.mHandler.obtainMessage(104), 500);
                        return;
                    }
                    OppoDcTrackerReference.this.releaseWlanAssistantRequest();
                }
            } else if (action.equals(OppoDcTrackerReference.WIFI_SCORE_CHANGE)) {
                SubscriptionManager.from(OppoDcTrackerReference.this.mPhone.getContext());
                if (OppoDcTrackerReference.this.mPhone.getSubId() != SubscriptionManager.getDefaultDataSubscriptionId()) {
                    enabled = false;
                }
                boolean enableData = intent.getBooleanExtra("enableData", false);
                if (!enabled) {
                    return;
                }
                if (OppoDcTrackerReference.mMeasureDataState == enableData) {
                    OppoDcTrackerReference.this.logd("WLAN+ WIFI_SCORE_CHANGE mMeasureDataState already set to " + OppoDcTrackerReference.mMeasureDataState);
                    return;
                }
                OppoDcTrackerReference.mMeasureDataState = enableData;
                ConnectivityManagerHelper.shouldKeepCelluarNetwork(OppoDcTrackerReference.this.mCm, OppoDcTrackerReference.mMeasureDataState);
                new Thread() {
                    /* class com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference.AnonymousClass1.AnonymousClass1 */

                    public void run() {
                        int i;
                        boolean isBusy = true;
                        int wait = 0;
                        long now = SystemClock.elapsedRealtime();
                        while (true) {
                            if ((isBusy || now - OppoDcTrackerReference.this.mWifiConnectTimeStamp < 3000) && wait < 60) {
                                ApnContext defaultApnContext = tmpDcTracker.getApnContextByType(17);
                                if (defaultApnContext == null) {
                                    break;
                                }
                                int i2 = AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[defaultApnContext.getState().ordinal()];
                                boolean isBusy2 = i2 == 1 || i2 == 2 || i2 == 3;
                                long sleepTime = 0;
                                if (now - OppoDcTrackerReference.this.mWifiConnectTimeStamp < 3000) {
                                    sleepTime = 3000 - (now - OppoDcTrackerReference.this.mWifiConnectTimeStamp);
                                } else if (isBusy2) {
                                    sleepTime = 1000;
                                }
                                try {
                                    OppoDcTrackerReference.this.logd("WLAN+ WIFI_SCORE_CHANGE waiting " + sleepTime + "ms for last (dis)connect finish!");
                                    Thread.sleep(sleepTime);
                                } catch (Exception e) {
                                    OppoDcTrackerReference.this.logd("WLAN+ " + e.toString());
                                }
                                wait++;
                                now = SystemClock.elapsedRealtime();
                                isBusy = isBusy2;
                            }
                        }
                        if (OppoDcTrackerReference.mMeasureDataState) {
                            int i3 = 0;
                            for (i = 3; i3 < i; i = 3) {
                                boolean myMeasureDataState = (OppoDcTrackerReference.this.isUserDataEnabled() || OppoDcTrackerReference.this.haveVsimIgnoreUserDataSetting()) && !OppoDcTrackerReference.this.mPhone.getServiceState().getRoaming();
                                OppoDcTrackerReference oppoDcTrackerReference = OppoDcTrackerReference.this;
                                StringBuilder sb = new StringBuilder();
                                sb.append("WLAN+ WIFI_SCORE_CHANGE ");
                                sb.append(i3);
                                sb.append(": mMeasureDataState=");
                                sb.append(OppoDcTrackerReference.mMeasureDataState);
                                sb.append(" Roaming=");
                                sb.append(OppoDcTrackerReference.this.mPhone.getServiceState().getRoaming());
                                sb.append(" DataEnabled=");
                                sb.append(OppoDcTrackerReference.this.isUserDataEnabled() || OppoDcTrackerReference.this.haveVsimIgnoreUserDataSetting());
                                oppoDcTrackerReference.logd(sb.toString());
                                if (!myMeasureDataState) {
                                    OppoDcTrackerReference.this.logd("WLAN+ WIFI_SCORE_CHANGE myMeasureDataState is false. ignore!");
                                    return;
                                }
                                NetworkRequest request = ConnectivityManagerHelper.getCelluarNetworkRequest(OppoDcTrackerReference.this.mCm);
                                if (request != null) {
                                    OppoDcTrackerReference.this.releaseWlanAssistantRequest();
                                    OppoDcTrackerReference.mMeasureDCCallback = new ConnectivityManager.NetworkCallback();
                                    OppoDcTrackerReference.this.mCm.requestNetwork(request, OppoDcTrackerReference.mMeasureDCCallback);
                                    if (OppoDcTrackerReference.this.measureDataState()) {
                                        return;
                                    }
                                }
                                try {
                                    Thread.sleep((long) (OppoDcTrackerReference.this.waitToRetry[i3] * 1000));
                                    Boolean isConnected = false;
                                    ApnContext apnContext = tmpDcTracker.getApnContextByType(17);
                                    if (apnContext != null && apnContext.getState() == DctConstants.State.CONNECTED) {
                                        isConnected = true;
                                    }
                                    if (!isConnected.booleanValue()) {
                                        if (OppoDcTrackerReference.mMeasureDataState) {
                                            i3++;
                                        }
                                    }
                                    OppoDcTrackerReference.this.logd("WLAN+ WIFI_SCORE_CHANG retry ignore: mMeasureDataState=" + OppoDcTrackerReference.mMeasureDataState + " conntected:" + isConnected);
                                    return;
                                } catch (Exception e2) {
                                    OppoDcTrackerReference.this.logd("WLAN+ " + e2.toString());
                                }
                            }
                            return;
                        }
                        OppoDcTrackerReference.this.releaseWlanAssistantRequest();
                    }
                }.start();
            } else if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                OppoDcTrackerReference.this.updateDataEnableForCustom();
            } else {
                OppoDcTrackerReference.this.logd("onReceive: Unknown action=" + action);
            }
        }
    };
    boolean mIsOppoScreenOn;
    private boolean mIsWifiConnected = false;
    protected boolean mNeedDataStall = true;
    protected boolean mOppoFirstcommingModemKeyLog = true;
    protected boolean mOppoModemKeyLogState = false;
    OppoNetworkHongbao mOppoNetworkHongbao = null;
    protected final Phone mPhone;
    private SettingsObserver mSettingsObserver;
    private long mWifiConnectTimeStamp = 0;
    private NetworkInfo.DetailedState mWifiOldState = NetworkInfo.DetailedState.IDLE;
    private final int[] waitToRetry = {3, 30, 60};

    public OppoDcTrackerReference(DcTracker dcTracker, Phone phone) {
        this.mDcTracker = dcTracker;
        this.mPhone = phone;
        this.LOG_TAG += "/" + this.mPhone.getPhoneId();
        this.mFilter = new IntentFilter();
        this.mOppoNetworkHongbao = new OppoNetworkHongbao(this.mPhone);
        if (OppoPhoneUtil.getWlanAssistantEnable(this.mPhone.getContext())) {
            this.mFilter.addAction(WIFI_SCORE_CHANGE);
            this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.mFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        }
        this.mFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, dcTracker)).setAutoAttachOnCreationConfig(this.mPhone.getContext().getResources().getBoolean(17891366));
        mOppoCtaSupport = this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.cta.support");
        this.mSettingsObserver = new SettingsObserver(this.mPhone.getContext(), this.mHandler);
        this.mCm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        registerForAllEvents();
        registerSettingsObserver();
        mVsimIgnoreUserDataSetting = OppoPhoneUtil.isVsimIgnoreUserDataSetting(this.mPhone.getContext());
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.LOG_TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }

    public void unregisterForOppoAllEvents() {
        unregisterForAllEvents();
    }

    /* renamed from: com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference$6  reason: invalid class name */
    static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$DctConstants$State = new int[DctConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.DISCONNECTING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.RETRYING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.IDLE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public void updateDataEnableForCustom() {
        Phone phone = this.mPhone;
        if (phone == null) {
            return;
        }
        if ((phone.getContext().getPackageManager().hasSystemFeature("oppo.customize.function.mdpoe") || this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.business.custom")) && this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            boolean isUserData = isUserDataEnabled();
            String oem_slot1 = SystemProperties.get("persist.sys.oem_slot1_db", "-1");
            String oem_slot2 = SystemProperties.get("persist.sys.oem_slot2_db", "-1");
            logd("updateDataEnableFotCustom isUserData = " + isUserData + " oem_slot1 = " + oem_slot1 + " oem_slot2 = " + oem_slot2 + " mPhone.getPhoneId() = " + this.mPhone.getPhoneId());
            if (isUserData) {
                if (this.mPhone.getPhoneId() != 0 || (!"0".equals(oem_slot1) && !"3".equals(oem_slot1))) {
                    if (this.mPhone.getPhoneId() != 1) {
                        return;
                    }
                    if (!"0".equals(oem_slot2) && !"3".equals(oem_slot2)) {
                        return;
                    }
                }
                setUserDataEnabled(false);
                return;
            }
            if (this.mPhone.getPhoneId() != 0 || (!"1".equals(oem_slot1) && !RegionLockConstant.TEST_OP_DEFAULT.equals(oem_slot1))) {
                if (this.mPhone.getPhoneId() != 1) {
                    return;
                }
                if (!"1".equals(oem_slot2) && !RegionLockConstant.TEST_OP_DEFAULT.equals(oem_slot2)) {
                    return;
                }
            }
            setUserDataEnabled(true);
        }
    }

    private void setUserDataEnabled(boolean allow) {
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.getDataEnabledSettings().setUserDataEnabled(allow);
        }
    }

    private void registerSettingsObserver() {
        this.mSettingsObserver.unobserve();
        if (TelephonyManager.getDefault().getSimCount() > 1) {
            Integer.toString(this.mPhone.getSubId());
        }
        SettingsObserver settingsObserver = this.mSettingsObserver;
        settingsObserver.observe(Settings.Global.getUriFor(COLOR_DATA_ROAMING_TYPE + Integer.toString(this.mPhone.getSubId())), 270384);
    }

    private void registerForAllEvents() {
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, this.mFilter, null, this.mPhone);
        this.mPhone.getCallTracker().registerForVoiceCallStarted(this.mHandler, 270343, (Object) null);
        this.mPhone.getCallTracker().registerForVoiceCallEnded(this.mHandler, 270344, (Object) null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker)).getTransportType(), this.mHandler, 270345, (Object) null);
        OppoTelephonyController.getInstance(this.mPhone.getContext()).registerForOemScreenChanged(this.mHandler, 1, null);
    }

    private void unregisterForAllEvents() {
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        this.mPhone.getCallTracker().unregisterForVoiceCallStarted(this.mHandler);
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker)).getTransportType(), this.mHandler);
        OppoTelephonyController.getInstance(this.mPhone.getContext()).unregisterOemScreenChanged(this.mHandler);
        if (this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().getCallTracker() != null) {
            logd("unregister ims call state");
            this.mPhone.getImsPhone().getCallTracker().unregisterForVoiceCallEnded(this.mHandler);
            this.mPhone.getImsPhone().getCallTracker().unregisterForVoiceCallStarted(this.mHandler);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isUserDataEnabled() {
        Phone phone = this.mPhone;
        if (phone != null) {
            return phone.getDataEnabledSettings().isUserDataEnabled();
        }
        return false;
    }

    public DataConnectionReasons.DataDisallowedReasonType isOppoRoamingAllowed() {
        AbstractDcTracker tmpDcTracker = (AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker);
        if (!this.mPhone.getServiceState().getDataRoaming() || !this.mDcTracker.getDataRoamingEnabled() || tmpDcTracker.getIccRecords().get() == null || !OppoPhoneUtil.isDomesticRoamingSpecialSim(((IccRecords) tmpDcTracker.getIccRecords().get()).getIMSI()) || getOemDataRoamingEnabledType() != 2 || this.mPhone.getServiceState().getVoiceRoamingType() != 3) {
            return null;
        }
        logd("data not allowed, dataRoamingType: 2,can not trysetup on international roaming");
        return DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED;
    }

    private void onOemDataRoamingTypeOrSettingsChanged() {
        if (!this.mPhone.getServiceState().getDataRoaming()) {
            logd("onOemDataRoamingTypeOrSettingsChanged: device is not roaming. ignored the request.");
            return;
        }
        AbstractDcTracker tmpDcTracker = (AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker);
        if (tmpDcTracker.getIccRecords().get() != null) {
            if (!OppoPhoneUtil.isDomesticRoamingSpecialSim(((IccRecords) tmpDcTracker.getIccRecords().get()).getIMSI())) {
                logd("onOemDataRoamingTypeOrSettingsChanged: is not specific SIM. ignore the request.");
                return;
            }
            int dataRoamingType = getOemDataRoamingEnabledType();
            if (dataRoamingType == 1) {
                logd("dataRoamingType: 1, setup data on international roaming");
                tmpDcTracker.setupDataOnAllConnectableApns("roamingOn");
                this.mPhone.notifyDataConnection();
            } else if (dataRoamingType != 2) {
                logd("dataRoaming not enable, tear down data on roaming.");
                this.mDcTracker.cleanUpAllConnections("roamingOn");
                tmpDcTracker.notifyOffApnsOfAvailability();
            } else if (this.mPhone.getServiceState().getDataRoamingType() == 2) {
                logd("dataRoamingType: 2, setup data on domestic roaming");
                tmpDcTracker.setupDataOnAllConnectableApns("roamingOn");
                this.mPhone.notifyDataConnection();
            } else {
                logd("dataRoamingType: 2, tear down data on international roaming.");
                this.mDcTracker.cleanUpAllConnections("roamingOn");
                tmpDcTracker.notifyOffApnsOfAvailability();
            }
        }
    }

    private int getOemDataRoamingEnabledType() {
        int phoneSubId = this.mPhone.getSubId();
        try {
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                return Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), COLOR_DATA_ROAMING_TYPE, 0);
            }
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            return Settings.Global.getInt(contentResolver, COLOR_DATA_ROAMING_TYPE + phoneSubId, 0);
        } catch (Exception e) {
            logd("getOemDataRoamingEnabledType: SettingNofFoundException e=" + e);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void oemInitDataRoamingEnabledType(String imsi) {
        int phoneSubId = this.mPhone.getSubId();
        if (OppoPhoneUtil.isDomesticRoamingSpecialSim(imsi) && getOemDataRoamingEnabledType() == 0) {
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                Settings.Global.putInt(this.mPhone.getContext().getContentResolver(), COLOR_DATA_ROAMING_TYPE, 2);
            } else {
                ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
                Settings.Global.putInt(contentResolver, COLOR_DATA_ROAMING_TYPE + phoneSubId, 2);
            }
            this.mDcTracker.setDataRoamingEnabledByUser(true);
            logd("initialize roamingEnableType: 2");
        }
    }

    public void OppoSetupDataOnAllConnectableApns(String reason) {
        AbstractDcTracker tmpDcTracker = (AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker);
        if (tmpDcTracker.getIccRecords().get() == null || !OppoPhoneUtil.isDomesticRoamingSpecialSim(((IccRecords) tmpDcTracker.getIccRecords().get()).getIMSI()) || getOemDataRoamingEnabledType() == 0) {
            tmpDcTracker.setupDataOnAllConnectableApns("roamingOn");
            this.mPhone.notifyDataConnection();
            return;
        }
        onOemDataRoamingTypeOrSettingsChanged();
    }

    public void oppoRegisterForImsiReady(IccRecords iccRecords) {
        if (iccRecords != null) {
            iccRecords.registerForImsiReady(this.mHandler, (int) DATA_EVENT_IMSI_READY, (Object) null);
        }
    }

    public void oppoUnregisterForImsiReady(IccRecords iccRecords) {
        if (iccRecords != null) {
            iccRecords.unregisterForImsiReady(this.mHandler);
        }
    }

    public void oppoHandleRoamingTypeChange(IccRecords iccRecords) {
        AbstractDcTracker tmpDcTracker = (AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker);
        if (iccRecords == null || !OemConstant.isDomesticRoamingSpecialSim(iccRecords.getIMSI()) || getOemDataRoamingEnabledType() == 0) {
            tmpDcTracker.onRoamingTypeChanged();
        } else {
            onOemDataRoamingTypeOrSettingsChanged();
        }
    }

    public boolean informNewSimCardLoaded(int slotIndex) {
        for (Map.Entry<Integer, Integer> entry : sSlotIndexToIsNewSim.entrySet()) {
            int slot = entry.getKey().intValue();
            int newSimCard = entry.getValue().intValue();
            logd("slot = " + slot + " newSimCard = " + newSimCard);
            if (slotIndex == slot) {
                if (1 == newSimCard) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public void updateMapValue(int slotIndex, int value) {
        Map<Integer, Integer> map = sSlotIndexToIsNewSim;
        if (map == null) {
            return;
        }
        if (map.containsKey(Integer.valueOf(slotIndex))) {
            sSlotIndexToIsNewSim.replace(Integer.valueOf(slotIndex), Integer.valueOf(value));
        } else {
            sSlotIndexToIsNewSim.put(Integer.valueOf(slotIndex), Integer.valueOf(value));
        }
    }

    public void setDataRoamingEnabledForOperator(int slotIndex) {
        if (informNewSimCardLoaded(slotIndex)) {
            logd("New sim card loaded, check whether it is target operator.");
            IccRecords r = (IccRecords) ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker)).getIccRecords().get();
            if (r == null) {
                logd("IccRecord is null.");
                return;
            }
            String imsi = r.getIMSI();
            if (!TextUtils.isEmpty(imsi) && imsi.length() > 5) {
                boolean isOtherMvnoOperator = false;
                String operatorPlmn = imsi.substring(0, 5);
                logd("operatorPlmn = " + operatorPlmn);
                if (isNLTmobile()) {
                    logd("BEN NL = " + ApnSettingUtils.mvnoMatches(r, 0, "BEN NL"));
                    if (ApnSettingUtils.mvnoMatches(r, 0, "BEN NL")) {
                        return;
                    }
                    if (operatorPlmn.equals("20416") || operatorPlmn.equals("20420")) {
                        this.mDcTracker.setDataRoamingEnabledByUser(true);
                        logd("Set data romaing enabled for NL T-mobile in default state.");
                        return;
                    }
                    return;
                }
                logd("mGid = " + r.getGid1());
                if (ApnSettingUtils.mvnoMatches(r, 2, "28") || ApnSettingUtils.mvnoMatches(r, 2, "B2") || ApnSettingUtils.mvnoMatches(r, 0, "LIFE")) {
                    isOtherMvnoOperator = true;
                }
                if (isOtherMvnoOperator) {
                    return;
                }
                if (operatorPlmn.equals("23430") || operatorPlmn.equals("23433")) {
                    this.mDcTracker.setDataRoamingEnabledByUser(true);
                    logd("Set data romaing enabled for EE/BT in default state.");
                }
            }
        }
    }

    private boolean isNLTmobile() {
        try {
            String regionmark = SystemProperties.get("ro.oppo.regionmark", "");
            String operator = SystemProperties.get("ro.oppo.operator", "");
            String country = SystemProperties.get("ro.oppo.euex.country", "");
            if (TextUtils.isEmpty(regionmark) || !"EUEX".equals(regionmark) || TextUtils.isEmpty(operator) || !"TMOBILE".equals(operator) || TextUtils.isEmpty(country) || !"NL".equals(country)) {
                return false;
            }
            logd("regionmark = " + regionmark + " operator = " + operator + " country = " + country);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean haveVsimIgnoreUserDataSetting() {
        return mVsimIgnoreUserDataSetting && OppoUiccManagerImpl.getInstance().getSoftSimCardSlotId() == this.mPhone.getPhoneId();
    }

    public void oppoWlanAssistantMeasureForDataEnabled(boolean enabled) {
        if (OppoPhoneUtil.getWlanAssistantEnable(this.mPhone.getContext())) {
            SubscriptionManager.from(this.mPhone.getContext());
            boolean myMeasureDataState = true;
            boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId();
            if (isDefaultDataPhone) {
                boolean isRomming = this.mPhone.getServiceState().getRoaming();
                logd("WLAN+ CMD_SET_USER_DATA_ENABLE: mMeasureDataState=" + mMeasureDataState + " Roaming=" + isRomming + " DataEnabled=" + enabled + " isDefaultDataPhone=" + isDefaultDataPhone);
                if (!mMeasureDataState || !this.mIsWifiConnected || isRomming || !enabled) {
                    myMeasureDataState = false;
                }
                if (myMeasureDataState) {
                    new Thread() {
                        /* class com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference.AnonymousClass3 */

                        public void run() {
                            NetworkRequest request;
                            if (!OppoDcTrackerReference.this.measureDataState() && (request = ConnectivityManagerHelper.getCelluarNetworkRequest(OppoDcTrackerReference.this.mCm)) != null) {
                                if (OppoDcTrackerReference.mMeasureDCCallback != null) {
                                    OppoDcTrackerReference oppoDcTrackerReference = OppoDcTrackerReference.this;
                                    oppoDcTrackerReference.logd("WLAN+ CMD_SET_USER_DATA_ENABLE release DC befor request: mMeasureDataState=" + OppoDcTrackerReference.mMeasureDataState);
                                    try {
                                        OppoDcTrackerReference.this.mCm.unregisterNetworkCallback(OppoDcTrackerReference.mMeasureDCCallback);
                                    } catch (IllegalArgumentException e) {
                                        OppoDcTrackerReference oppoDcTrackerReference2 = OppoDcTrackerReference.this;
                                        oppoDcTrackerReference2.logd("WLAN+ " + e.toString());
                                    } catch (Exception e2) {
                                        OppoDcTrackerReference oppoDcTrackerReference3 = OppoDcTrackerReference.this;
                                        oppoDcTrackerReference3.logd("WLAN+ Exception:" + e2.toString());
                                    }
                                }
                                OppoDcTrackerReference.mMeasureDCCallback = new ConnectivityManager.NetworkCallback();
                                OppoDcTrackerReference.this.mCm.requestNetwork(request, OppoDcTrackerReference.mMeasureDCCallback);
                                OppoDcTrackerReference.this.measureDataState();
                            }
                        }
                    }.start();
                }
            }
        }
    }

    public boolean oppoWlanAssistantBlockTrySetupData(ApnContext apnContext, String reason) {
        String apnType = apnContext.getApnType();
        if (!OppoPhoneUtil.getWlanAssistantEnable(this.mPhone.getContext()) || !mDelayMeasure || !mMeasureDataState || !this.mIsWifiConnected || !"default".equals(apnType)) {
            return false;
        }
        logd("setupDataOnConnectableApns: " + reason + "ignore! block for WLAN+, return...");
        return true;
    }

    public void oppoWlanAssistantDelayMeasure() {
        SubscriptionManager.from(this.mPhone.getContext());
        if ((this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            this.mHandler.removeMessages(103);
            mDelayMeasure = true;
            logd("WLAN+ onVoiceCallStarted mDelayMeasure:true");
        }
    }

    public void oppoWlanAssistantDelaySetupData() {
        SubscriptionManager.from(this.mPhone.getContext());
        AbstractDcTracker tmpDcTracker = (AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker);
        boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId();
        if (!OppoPhoneUtil.getWlanAssistantEnable(this.mPhone.getContext()) || !mMeasureDataState || ((!isUserDataEnabled() && !haveVsimIgnoreUserDataSetting()) || this.mPhone.getServiceState().getRoaming() || !this.mIsWifiConnected || !isDefaultDataPhone || this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) {
            tmpDcTracker.setupDataOnAllConnectableApns("2GVoiceCallEnded");
            mDelayMeasure = false;
            logd("WLAN+ onVoiceCallEnded mDelayMeasure:false");
            return;
        }
        this.mHandler.removeMessages(103);
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(103, null), 60000);
        logd("WLAN+ onVoiceCallEnded, send CMD_DELAY_SETUP_DATA");
    }

    public void oppoWlanAssistantMeasureForRatChange() {
        if (OppoPhoneUtil.getWlanAssistantEnable(this.mPhone.getContext())) {
            int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
            if ((dataRadioTech == 19 && mLastDataRadioTech == 14) || (dataRadioTech == 14 && mLastDataRadioTech == 19)) {
                mLastDataRadioTech = dataRadioTech;
                return;
            }
            mLastDataRadioTech = dataRadioTech;
            SubscriptionManager.from(this.mPhone.getContext());
            boolean myMeasureDataState = true;
            boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId();
            if (isDefaultDataPhone) {
                boolean isRomming = this.mPhone.getServiceState().getRoaming();
                StringBuilder sb = new StringBuilder();
                sb.append("WLAN+ EVENT_DATA_RAT_CHANGED: mMeasureDataState=");
                sb.append(mMeasureDataState);
                sb.append(" Roaming=");
                sb.append(isRomming);
                sb.append(" DataEnabled=");
                sb.append(isUserDataEnabled() || haveVsimIgnoreUserDataSetting());
                sb.append(" isDefaultDataPhone=");
                sb.append(isDefaultDataPhone);
                logd(sb.toString());
                if (!mMeasureDataState || mDelayMeasure || isRomming || (!isUserDataEnabled() && !haveVsimIgnoreUserDataSetting())) {
                    myMeasureDataState = false;
                }
                if (myMeasureDataState) {
                    new Thread() {
                        /* class com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference.AnonymousClass4 */

                        public void run() {
                            NetworkRequest request;
                            if (!OppoDcTrackerReference.this.measureDataState() && (request = ConnectivityManagerHelper.getCelluarNetworkRequest(OppoDcTrackerReference.this.mCm)) != null) {
                                if (OppoDcTrackerReference.mMeasureDCCallback != null) {
                                    OppoDcTrackerReference oppoDcTrackerReference = OppoDcTrackerReference.this;
                                    oppoDcTrackerReference.logd("WLAN+ EVENT_DATA_RAT_CHANGED release DC befor request: mMeasureDataState=" + OppoDcTrackerReference.mMeasureDataState);
                                    try {
                                        OppoDcTrackerReference.this.mCm.unregisterNetworkCallback(OppoDcTrackerReference.mMeasureDCCallback);
                                    } catch (IllegalArgumentException e) {
                                        OppoDcTrackerReference oppoDcTrackerReference2 = OppoDcTrackerReference.this;
                                        oppoDcTrackerReference2.logd("WLAN+ " + e.toString());
                                    } catch (Exception e2) {
                                        OppoDcTrackerReference oppoDcTrackerReference3 = OppoDcTrackerReference.this;
                                        oppoDcTrackerReference3.logd("WLAN+ Exception:" + e2.toString());
                                    }
                                }
                                OppoDcTrackerReference.mMeasureDCCallback = new ConnectivityManager.NetworkCallback();
                                OppoDcTrackerReference.this.mCm.requestNetwork(request, OppoDcTrackerReference.mMeasureDCCallback);
                                OppoDcTrackerReference.this.measureDataState();
                            }
                        }
                    }.start();
                }
            }
        }
    }

    public void oppoWlanAssistantMeasureForDataStateChanged() {
        if (OppoPhoneUtil.getWlanAssistantEnable(this.mPhone.getContext())) {
            boolean z = true;
            boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId();
            if (isDefaultDataPhone) {
                boolean isRomming = this.mPhone.getServiceState().getRoaming();
                boolean myMeasureDataState = mMeasureDataState && !mDelayMeasure && !isRomming && (isUserDataEnabled() || haveVsimIgnoreUserDataSetting());
                StringBuilder sb = new StringBuilder();
                sb.append("WLAN+ EVENT_DATA_STATE_CHANGED: mMeasureDataState=");
                sb.append(mMeasureDataState);
                sb.append(" Roaming=");
                sb.append(isRomming);
                sb.append(" DataEnabled=");
                if (!isUserDataEnabled() && !haveVsimIgnoreUserDataSetting()) {
                    z = false;
                }
                sb.append(z);
                sb.append(" isDefaultDataPhone");
                sb.append(isDefaultDataPhone);
                logd(sb.toString());
                if (myMeasureDataState) {
                    new Thread() {
                        /* class com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference.AnonymousClass5 */

                        public void run() {
                            NetworkRequest request;
                            if (!OppoDcTrackerReference.this.measureDataState() && (request = ConnectivityManagerHelper.getCelluarNetworkRequest(OppoDcTrackerReference.this.mCm)) != null) {
                                if (OppoDcTrackerReference.mMeasureDCCallback != null) {
                                    OppoDcTrackerReference oppoDcTrackerReference = OppoDcTrackerReference.this;
                                    oppoDcTrackerReference.logd("WLAN+ EVENT_DATA_STATE_CHANGED release DC befor request: mMeasureDataState=" + OppoDcTrackerReference.mMeasureDCCallback);
                                    try {
                                        OppoDcTrackerReference.this.mCm.unregisterNetworkCallback(OppoDcTrackerReference.mMeasureDCCallback);
                                    } catch (IllegalArgumentException e) {
                                        OppoDcTrackerReference oppoDcTrackerReference2 = OppoDcTrackerReference.this;
                                        oppoDcTrackerReference2.loge("WLAN+ " + e.toString());
                                    } catch (Exception e2) {
                                        OppoDcTrackerReference oppoDcTrackerReference3 = OppoDcTrackerReference.this;
                                        oppoDcTrackerReference3.loge("WLAN+ Exception:" + e2.toString());
                                    }
                                }
                                OppoDcTrackerReference.mMeasureDCCallback = new ConnectivityManager.NetworkCallback();
                                OppoDcTrackerReference.this.mCm.requestNetwork(request, OppoDcTrackerReference.mMeasureDCCallback);
                                OppoDcTrackerReference.this.measureDataState();
                            }
                        }
                    }.start();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseWlanAssistantRequest() {
        if (mMeasureDCCallback != null) {
            logd("WLAN+ WIFI_SCORE_CHANGE release DC: mMeasureDataState=" + mMeasureDataState);
            try {
                this.mCm.unregisterNetworkCallback(mMeasureDCCallback);
            } catch (IllegalArgumentException e) {
                logd("WLAN+ " + e.toString());
            } catch (Exception e2) {
                logd("WLAN+ Exception:" + e2.toString());
            }
            mMeasureDCCallback = null;
        }
    }

    public void checkIfRetryAfterDisconnected(ApnContext apnContext, boolean retry) {
        if ("default".equals(apnContext.getApnType()) && this.mIsWifiConnected && !mMeasureDataState) {
            logd("wifi have conneted, set default apn type retry false!!");
        }
    }

    public boolean oemAllowMmsWhenDataDisableNonRoaming(ApnContext apnContext, DataEnabledSettings settings) {
        if (settings == null || !settings.isInternalDataEnabled() || apnContext == null || apnContext.getApnType() == null || !apnContext.getApnType().equals("mms")) {
            return false;
        }
        logd("mms allow");
        return true;
    }

    public void registerOnImsCallStateChange() {
        Phone phone = this.mPhone;
        if (phone != null && phone.getImsPhone() != null && this.mPhone.getImsPhone().getCallTracker() != null) {
            logd("register on ims call state");
            this.mPhone.getImsPhone().getCallTracker().registerForVoiceCallEnded(this.mHandler, 270344, (Object) null);
            this.mPhone.getImsPhone().getCallTracker().registerForVoiceCallStarted(this.mHandler, 270343, (Object) null);
            this.mPhone.getImsPhone().getCallTracker().registerForVoiceCallEnded(this.mDcTracker, 270344, (Object) null);
            this.mPhone.getImsPhone().getCallTracker().registerForVoiceCallStarted(this.mDcTracker, 270343, (Object) null);
        }
    }

    public void notifyDataConnectionOnVoiceCallStateChange(String reasonForVoiceCall) {
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (!((this.mPhone.getSubId() == defaultDataSubId) || !SubscriptionManager.isValidSubscriptionId(defaultDataSubId) || TelephonyManager.getDefault().getMultiSimConfiguration() == TelephonyManager.MultiSimVariants.DSDA)) {
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                Phone phone = PhoneFactory.getPhone(i);
                if (phone != null && phone.getSubId() == defaultDataSubId) {
                    phone.notifyDataConnection();
                    logd("reasonForVoiceCall: " + reasonForVoiceCall);
                }
            }
        }
    }

    public void startMobileDataHongbaoPolicy(int time1, int time2, String value1, String value2) {
        this.mOppoNetworkHongbao.startMobileDataHongbaoPolicy(time1, time2, value1, value2);
    }

    public DctConstants.State getApnState(String apnType) {
        ApnContext apnContext;
        AbstractDcTracker tmpDcTracker = (AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker);
        if (tmpDcTracker == null || tmpDcTracker.getApnContexts() == null || (apnContext = (ApnContext) tmpDcTracker.getApnContexts().get(apnType)) == null) {
            return DctConstants.State.FAILED;
        }
        return apnContext.getState();
    }

    public String getOperatorNumeric() {
        return ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker)).getOperatorNumeric();
    }

    private ApnSetting getDunApnFromCache(ArrayList<ApnSetting> allApnSettings) {
        IccRecords r = (IccRecords) ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker)).getIccRecords().get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        int i = 0;
        while (true) {
            String[] strArr = sTelstraOperaters;
            if (i >= strArr.length) {
                break;
            } else if (!strArr[i].equals(operator)) {
                i++;
            } else if (allApnSettings != null && !allApnSettings.isEmpty()) {
                logd("getDunApnFromCache: mAllApnSettings=" + allApnSettings);
                Iterator<ApnSetting> it = allApnSettings.iterator();
                while (it.hasNext()) {
                    ApnSetting apn = it.next();
                    if (apn.canHandleType(8)) {
                        logd("getDunApnFromCache: operator:" + operator + " apn:" + apn);
                        return apn;
                    }
                }
            }
        }
        logd("getDunApnFromCache: get DUN apn from the default config!!! operator:" + operator);
        return null;
    }

    public ArrayList<ApnSetting> getDunApnList(ArrayList<ApnSetting> allApnSettings) {
        ApnSetting dunApn = getDunApnFromCache(allApnSettings);
        if (dunApn == null) {
            return null;
        }
        ArrayList<ApnSetting> dunList = new ArrayList<>();
        dunList.add(dunApn);
        logd("fetchDunApn: from cache dunApn:" + dunApn);
        return dunList;
    }

    public boolean isTelstraSimAndNetworkClassNotChange() {
        if (!isTelstraSim()) {
            return false;
        }
        int oldClass = TelephonyManager.getNetworkClass(this.mDataType);
        this.mDataType = this.mPhone.getServiceState().getDataNetworkType();
        if (TelephonyManager.getNetworkClass(this.mDataType) == oldClass) {
            return true;
        }
        return false;
    }

    private boolean isTelstraSim() {
        boolean isTelstraSim = false;
        IccRecords r = (IccRecords) ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker)).getIccRecords().get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        int i = 0;
        while (true) {
            String[] strArr = sTelstraOperaters;
            if (i >= strArr.length) {
                break;
            } else if (strArr[i].equals(operator)) {
                isTelstraSim = true;
                break;
            } else {
                i++;
            }
        }
        logd("isTelstraSim isTelstraSim:" + isTelstraSim);
        return isTelstraSim;
    }

    public boolean needManualSelectAPN(String apnType, ApnSetting preferredApn) {
        if ((!"default".equals(apnType) && apnType != null) || preferredApn != null || !needManualSelectAPN(getOperatorNumeric())) {
            return false;
        }
        logd("trySetupData: mPreferredApn == null, need Manual Select APN from UI, can not set up data!");
        return true;
    }

    public boolean needManualSelectAPN(ApnSetting preferredApn) {
        if (preferredApn != null || !needManualSelectAPN(getOperatorNumeric())) {
            return false;
        }
        logd("setInitialAttachApn: mPreferredApn == null, need Manual Select APN from UI!");
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0066, code lost:
        if (r2 != null) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0068, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0083, code lost:
        if (0 == 0) goto L_0x008c;
     */
    private boolean needManualSelectAPN(String operator) {
        String oppoManualSelect = null;
        if (operator != null) {
            Cursor cursor = null;
            try {
                String selection = "numeric = '" + operator + "'";
                logd("isOppoManualSelectAPN: selection=" + selection);
                cursor = this.mPhone.getContext().getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, selection, null, "_id");
                if (cursor != null && cursor.moveToFirst()) {
                    while (true) {
                        oppoManualSelect = cursor.getString(cursor.getColumnIndexOrThrow("oppo_manual_select"));
                        if (!"1".equals(oppoManualSelect)) {
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logd("needManualSelectAPNException:" + e);
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
        return "1".equals(oppoManualSelect);
    }

    public boolean checkIfValidIAApn(ApnSetting initialAttachApnSetting) {
        return true;
    }

    public void updateWaitingApns(ApnSetting preferredApn, ApnContext apnContext) {
        if (!(preferredApn == null || apnContext == null)) {
            ArrayList<ApnSetting> waitingApnsList = new ArrayList<>();
            waitingApnsList.add(preferredApn);
            apnContext.setWaitingApns(waitingApnsList);
        }
        logd("updateWaitingApns......");
    }

    public boolean checkIfNeedDataStall() {
        return this.mNeedDataStall;
    }

    public void setNeedDataStallFlag(ApnSetting apn) {
        if (apn != null) {
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, "_id = " + apn.getId() + " and sourcetype > 0", null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    this.mNeedDataStall = false;
                    logd("The apn sourcetype > 0, mNeedDataStall = false");
                } else {
                    this.mNeedDataStall = true;
                    logd("cursor.getCount() == 0");
                }
                cursor.close();
                return;
            }
            this.mNeedDataStall = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean measureDataState() {
        return ConnectivityManagerHelper.measureDataState(this.mCm, ((AbstractServiceStateTracker) OemTelephonyUtils.typeCasting(AbstractServiceStateTracker.class, this.mPhone.getServiceStateTracker())).getSignalLevel());
    }

    public void writeLogToPartionForLteLimit(int nwLimitState, String cellLocation) {
        boolean z = true;
        int errcode = nwLimitState == 1 ? 257 : 65278;
        loge("nwLimitState = " + nwLimitState + ", errcode = " + errcode);
        String log = cellLocation + ", type:" + OppoRIL.SYS_OEM_NW_DIAG_CAUSE_DATA_RATE_LIMIT_ON_LTE + ", rat:5, errcode:" + errcode;
        loge("Write log, return:" + OppoManagerHelper.writeLogToPartition(this.mPhone.getContext(), "zz_oppo_critical_log_116", log, OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_RATE_LIMIT_ON_LTE));
        Intent intent = new Intent("oppo.intent.action.NW_RATE_LIMIT_STATE");
        int subId = this.mPhone.getSubId();
        intent.putExtra("subId", subId);
        if (nwLimitState != 1) {
            z = false;
        }
        intent.putExtra("nwLimitState", z);
        this.mPhone.getContext().sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
        loge("lte rate limit state changed, nwLimitState:" + nwLimitState + ", subId:" + subId);
        HashMap<String, String> dataAbnormalMap = new HashMap<>();
        dataAbnormalMap.put(OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_RATE_LIMIT_ON_LTE, log);
        OppoManagerHelper.onStamp(DATA_EVENT_ID, dataAbnormalMap);
    }

    public String getCellLocation() {
        int phoneId = this.mPhone.getPhoneId();
        String mccMnc = "";
        String prop = SystemProperties.get("gsm.operator.numeric", "");
        if (prop != null && prop.length() > 0) {
            String[] values = prop.split(",");
            if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                mccMnc = values[phoneId];
            }
        }
        int mcc = 0;
        int mnc = 0;
        if (mccMnc != null) {
            try {
                if (mccMnc.length() >= 3) {
                    mcc = Integer.parseInt(mccMnc.substring(0, 3));
                    mnc = Integer.parseInt(mccMnc.substring(3));
                }
            } catch (NumberFormatException e) {
                Rlog.d(this.LOG_TAG, e.toString());
            } catch (Exception e2) {
                loge("couldn't parse mcc/mnc: " + mccMnc);
            }
        }
        if (mcc == 460) {
            if (mnc == 2 || mnc == 7 || mnc == 8) {
                mnc = 0;
            }
            if (mnc == 6 || mnc == 9) {
                mnc = 1;
            }
            if (mnc == 3) {
                mnc = 11;
            }
        }
        String loc = "MCC:" + mcc + ", MNC:" + mnc;
        Phone phone = this.mPhone;
        if (phone != null) {
            CellLocation cell = phone.getCellLocation();
            if (cell instanceof GsmCellLocation) {
                loc = loc + ", LAC:" + ((GsmCellLocation) cell).getLac() + ", CID:" + ((GsmCellLocation) cell).getCid();
            } else if (cell instanceof CdmaCellLocation) {
                loc = loc + ", SID:" + ((CdmaCellLocation) cell).getSystemId() + ", NID:" + ((CdmaCellLocation) cell).getNetworkId() + ", BID:" + ((CdmaCellLocation) cell).getBaseStationId();
            }
            SignalStrength signal = this.mPhone.getSignalStrength();
            if (signal != null) {
                loc = loc + ", signalstrength:" + signal.getDbm() + ", signallevel:" + signal.getLevel();
            }
        }
        logd("getCellLocation:" + loc);
        return loc;
    }

    public boolean recordDataStallInfo(boolean hasInboundData, long sentSinceLastRecv) {
        if (this.mPhone.getServiceState().getDataRegState() != 0 || !hasInboundData) {
            return true;
        }
        int nwType = TelephonyManager.getDefault().getNetworkType();
        OppoManagerHelper.writeLogToPartition(this.mPhone.getContext(), "zz_oppo_critical_log_113", getCellLocation() + ", nwType=" + nwType + ", sentSinceLastRecv=" + sentSinceLastRecv, OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_STALL_ERROR);
        return false;
    }

    public void recordNoApnAvailable() {
        if (this.mPhone.getServiceState().getDataRegState() == 0) {
            OppoManagerHelper.writeLogToPartition(this.mPhone.getContext(), "zz_oppo_critical_log_31", "record No Apn Available", OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL);
            HashMap<String, String> mAPNDataErrorMap = new HashMap<>();
            mAPNDataErrorMap.put(String.valueOf(OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL), "apn reason data call error");
            OppoManagerHelper.onStamp(DATA_EVENT_ID, mAPNDataErrorMap);
        }
    }

    public void recordNoOperatorError() {
        if (this.mPhone.getServiceState().getDataRegState() == 0) {
            OppoManagerHelper.writeLogToPartition(this.mPhone.getContext(), "zz_oppo_critical_log_31", "record No Operator Error", OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL);
            HashMap<String, String> mAPNDataErrorMap = new HashMap<>();
            mAPNDataErrorMap.put(String.valueOf(OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_APN_REASON_DATA_CALL_FAIL), "apn reason data call error");
            OppoManagerHelper.onStamp(DATA_EVENT_ID, mAPNDataErrorMap);
        }
    }

    public void printNoReceiveDataError(long sendNumber) {
        if (this.mPhone.getServiceState().getDataRegState() == 0) {
            OppoManagerHelper.writeLogToPartition(this.mPhone.getContext(), "zz_oppo_critical_log_37", "no received data duration alarm time", OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_NO_RECEIVE_DATA_ERROR);
            HashMap<String, String> mNoReceiveDataErrorMap = new HashMap<>();
            mNoReceiveDataErrorMap.put(String.valueOf(OppoRIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_NO_RECEIVE_DATA_ERROR), "apn reason data call error");
            OppoManagerHelper.onStamp(DATA_EVENT_ID, mNoReceiveDataErrorMap);
        }
    }

    public void oemCloseNr(Phone phone) {
        try {
            boolean isNRConnected = true;
            boolean isWifiConnected = ((ConnectivityManager) phone.getContext().getSystemService("connectivity")).getNetworkInfo(1).isConnected();
            boolean hasCurrentActiveCall = ((TelephonyManager) phone.getContext().getSystemService(TelephonyManager.class)).getCallState() != 0;
            if (phone.getServiceState().getNrState() != 3) {
                isNRConnected = false;
            }
            Rlog.d(this.LOG_TAG, "wifi=" + isWifiConnected + ",call=" + hasCurrentActiveCall + ",nr=" + isNRConnected);
            if (isNRConnected && !isWifiConnected) {
                if (!hasCurrentActiveCall) {
                    Intent intent = new Intent(ACTION_COMMAND_FORCE_DISABLE_ENDC);
                    intent.putExtra("PhoneId", phone.getPhoneId());
                    phone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
                    Rlog.d(this.LOG_TAG, "sendBroadcastAsUser");
                    return;
                }
            }
            Rlog.d(this.LOG_TAG, "oemCloseNr,return");
        } catch (Exception e) {
            Rlog.e(this.LOG_TAG, "Send broadcast failed: " + e);
        }
    }

    public boolean isTargetVersion() {
        String operator = SystemProperties.get("ro.oppo.operator");
        String regionMark = SystemProperties.get("ro.oppo.regionmark");
        String region = SystemProperties.get("persist.sys.oppo.region");
        String country = SystemProperties.get("ro.oppo.euex.country");
        logd("OppoDcTrackerReference operator = " + operator);
        if (!"O2".equals(operator) || !"EUEX".equals(regionMark) || !"GB".equals(region) || !"GB".equals(country)) {
            return false;
        }
        return true;
    }

    public ArrayList<ApnSetting> sortApnList(ArrayList<ApnSetting> list) {
        logd("OppoDcTrackerReference sortApnList");
        IccRecords r = (IccRecords) ((AbstractDcTracker) OemTelephonyUtils.typeCasting(AbstractDcTracker.class, this.mDcTracker)).getIccRecords().get();
        if (r != null) {
            logd("r != null");
            String iccId = r.getIccId();
            if (iccId != null && iccId.length() > 6) {
                iccId = iccId.substring(0, 6);
            }
            if ("23410".equals(r.getOperatorNumeric()) && "894411".equals(iccId) && getPayWay() == 0) {
                logd("buildWaitingApns reverse apnlist");
                Collections.reverse(list);
            }
        }
        return list;
    }

    public int getPayWay() {
        SIMRecords mSimRecords = null;
        Phone phone = this.mPhone;
        IccRecords r = phone != null ? phone.getIccRecords() : null;
        if (r != null) {
            mSimRecords = (SIMRecords) r;
        }
        if (mSimRecords == null) {
            return -1;
        }
        logd("mSimRecords!=null");
        String mImpi = ((AbstractSIMRecords) OemTelephonyUtils.typeCasting(AbstractSIMRecords.class, mSimRecords)).getImpi();
        logd("mImpi=" + mImpi);
        if (mImpi == null || "".equals(mImpi)) {
            return -1;
        }
        byte[] b = IccUtils.hexStringToBytes(mImpi);
        if ((b[0] & 1) == 1) {
            logd("postpaid simcard");
            return 1;
        } else if ((b[0] & 1) != 0) {
            return -1;
        } else {
            logd("prepaid simcard");
            return 0;
        }
    }
}
