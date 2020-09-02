package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.AsyncResult;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.util.LocalLog;
import android.util.SparseIntArray;
import android.view.Display;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeviceStateMonitor extends Handler {
    public static final String ACTION_ENDC_PWR_WORK_STATE_UPDATE = "android.intent.action.endc_pwr_workstate_update";
    @VisibleForTesting
    static final int CELL_INFO_INTERVAL_LONG_MS = 10000;
    @VisibleForTesting
    static final int CELL_INFO_INTERVAL_SHORT_MS = 2000;
    protected static final boolean DBG = OemConstant.SWITCH_LOG;
    @VisibleForTesting
    static final int EVENT_CHARGING_STATE_CHANGED = 4;
    private static final int EVENT_OEM_CALL_STATE_CHANGED = 99;
    static final int EVENT_POWER_SAVE_MODE_CHANGED = 3;
    static final int EVENT_RADIO_AVAILABLE = 6;
    static final int EVENT_RIL_CONNECTED = 0;
    @VisibleForTesting
    static final int EVENT_SCREEN_STATE_CHANGED = 2;
    static final int EVENT_TETHERING_STATE_CHANGED = 5;
    static final int EVENT_UPDATE_MODE_CHANGED = 1;
    @VisibleForTesting
    static final int EVENT_WIFI_CONNECTION_CHANGED = 7;
    private static final int HYSTERESIS_KBPS = 50;
    private static final int[] LINK_CAPACITY_DOWNLINK_THRESHOLDS = {100, AbstractSIMRecords.EVENT_GET_ALL_PNN_DONE, 1000, 5000, CELL_INFO_INTERVAL_LONG_MS, 20000, 50000, 100000, 200000, 500000, 1000000};
    private static final int[] LINK_CAPACITY_UPLINK_THRESHOLDS = {100, AbstractSIMRecords.EVENT_GET_ALL_PNN_DONE, 1000, 5000, CELL_INFO_INTERVAL_LONG_MS, 20000, 50000, 100000, 200000};
    protected static final String TAG = DeviceStateMonitor.class.getSimpleName();
    private static final int WIFI_AVAILABLE = 1;
    private static final int WIFI_UNAVAILABLE = 0;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.DeviceStateMonitor.AnonymousClass3 */

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public void onReceive(Context context, Intent intent) {
            char c;
            Message msg;
            int i = 1;
            DeviceStateMonitor.this.log("received: " + intent, true);
            String action = intent.getAction();
            switch (action.hashCode()) {
                case -1754841973:
                    if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -81217522:
                    if (action.equals(DeviceStateMonitor.ACTION_ENDC_PWR_WORK_STATE_UPDATE)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -54942926:
                    if (action.equals("android.os.action.DISCHARGING")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 948344062:
                    if (action.equals("android.os.action.CHARGING")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1779291251:
                    if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            String str = "on";
            if (c == 0) {
                Message msg2 = DeviceStateMonitor.this.obtainMessage(3);
                msg2.arg1 = DeviceStateMonitor.this.isPowerSaveModeOn() ? 1 : 0;
                DeviceStateMonitor deviceStateMonitor = DeviceStateMonitor.this;
                StringBuilder sb = new StringBuilder();
                sb.append("Power Save mode ");
                if (msg2.arg1 != 1) {
                    str = "off";
                }
                sb.append(str);
                deviceStateMonitor.log(sb.toString(), true);
                msg = msg2;
            } else if (c == 1) {
                msg = DeviceStateMonitor.this.obtainMessage(4);
                msg.arg1 = 1;
            } else if (c == 2) {
                msg = DeviceStateMonitor.this.obtainMessage(4);
                msg.arg1 = 0;
            } else if (c == 3) {
                ArrayList<String> activeTetherIfaces = intent.getStringArrayListExtra("tetherArray");
                boolean isTetheringOn = activeTetherIfaces != null && activeTetherIfaces.size() > 0;
                DeviceStateMonitor deviceStateMonitor2 = DeviceStateMonitor.this;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Tethering ");
                if (!isTetheringOn) {
                    str = "off";
                }
                sb2.append(str);
                deviceStateMonitor2.log(sb2.toString(), true);
                msg = DeviceStateMonitor.this.obtainMessage(5);
                if (!isTetheringOn) {
                    i = 0;
                }
                msg.arg1 = i;
            } else if (c != 4) {
                DeviceStateMonitor.this.log("Unexpected broadcast intent: " + intent, false);
                return;
            } else {
                int mPhoneId = intent.getIntExtra("phoneId", 0);
                if (mPhoneId == DeviceStateMonitor.this.mPhone.getPhoneId()) {
                    boolean unused = DeviceStateMonitor.this.mIsEndcPwrWorking = intent.getBooleanExtra("state", false);
                    DeviceStateMonitor.this.log("ACTION_ENDC_PWR_WORK_STATE_UPDATE PhoneId=" + mPhoneId + ",mIsWorking=" + DeviceStateMonitor.this.mIsEndcPwrWorking, false);
                    return;
                }
                return;
            }
            DeviceStateMonitor.this.sendMessage(msg);
        }
    };
    private int mCellInfoMinInterval = 2000;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.internal.telephony.DeviceStateMonitor.AnonymousClass2 */

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            boolean access$100 = DeviceStateMonitor.this.isScreenOn();
            Message msg = DeviceStateMonitor.this.obtainMessage(2);
            msg.arg1 = access$100 ? 1 : 0;
            DeviceStateMonitor.this.sendMessage(msg);
        }
    };
    protected boolean mIsCharging;
    /* access modifiers changed from: private */
    public boolean mIsEndcPwrWorking = false;
    private boolean mIsInCall = false;
    protected boolean mIsLowDataExpected;
    private boolean mIsPowerSaveOn;
    protected boolean mIsScreenOn;
    protected boolean mIsTetheringOn;
    private boolean mIsWifiConnected;
    private final LocalLog mLocalLog = new LocalLog(100);
    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        /* class com.android.internal.telephony.DeviceStateMonitor.AnonymousClass1 */
        Set<Network> mWifiNetworks = new HashSet();

        public void onAvailable(Network network) {
            synchronized (this.mWifiNetworks) {
                if (this.mWifiNetworks.size() == 0) {
                    DeviceStateMonitor.this.obtainMessage(7, 1, 0).sendToTarget();
                    DeviceStateMonitor.this.log("Wifi (default) connected", true);
                }
                this.mWifiNetworks.add(network);
            }
        }

        public void onLost(Network network) {
            synchronized (this.mWifiNetworks) {
                this.mWifiNetworks.remove(network);
                if (this.mWifiNetworks.size() == 0) {
                    DeviceStateMonitor.this.obtainMessage(7, 0, 0).sendToTarget();
                    DeviceStateMonitor.this.log("Wifi (default) disconnected", true);
                }
            }
        }
    };
    RegistrantList mOemScreenRegistrants = new RegistrantList();
    protected final Phone mPhone;
    private int mUnsolicitedResponseFilter = -1;
    private SparseIntArray mUpdateModes = new SparseIntArray();
    private final NetworkRequest mWifiNetworkRequest = new NetworkRequest.Builder().addTransportType(1).addCapability(12).removeCapability(13).build();

    public DeviceStateMonitor(Phone phone) {
        this.mPhone = phone;
        ((DisplayManager) phone.getContext().getSystemService("display")).registerDisplayListener(this.mDisplayListener, null);
        this.mIsPowerSaveOn = isPowerSaveModeOn();
        this.mIsCharging = isDeviceCharging();
        this.mIsScreenOn = isScreenOn();
        this.mIsTetheringOn = false;
        this.mIsLowDataExpected = false;
        log("DeviceStateMonitor mIsPowerSaveOn=" + this.mIsPowerSaveOn + ",mIsScreenOn=" + this.mIsScreenOn + ",mIsCharging=" + this.mIsCharging, false);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        filter.addAction("android.os.action.CHARGING");
        filter.addAction("android.os.action.DISCHARGING");
        filter.addAction("android.net.conn.TETHER_STATE_CHANGED");
        filter.addAction(ACTION_ENDC_PWR_WORK_STATE_UPDATE);
        this.mPhone.getContext().registerReceiver(this.mBroadcastReceiver, filter, null, this.mPhone);
        this.mPhone.mCi.registerForRilConnected(this, 0, null);
        this.mPhone.mCi.registerForAvailable(this, 6, null);
        ((ConnectivityManager) phone.getContext().getSystemService("connectivity")).registerNetworkCallback(this.mWifiNetworkRequest, this.mNetworkCallback);
    }

    /* access modifiers changed from: protected */
    public boolean isLowDataExpected() {
        return !this.mIsCharging && !this.mIsTetheringOn && !this.mIsScreenOn;
    }

    @VisibleForTesting
    public int computeCellInfoMinInterval() {
        if (this.mIsScreenOn && !this.mIsWifiConnected) {
            return 2000;
        }
        if (!this.mIsScreenOn || !this.mIsCharging) {
            return CELL_INFO_INTERVAL_LONG_MS;
        }
        return 2000;
    }

    private boolean shouldTurnOffSignalStrength() {
        if (!this.mIsInCall && !this.mIsCharging && !this.mIsScreenOn && this.mUpdateModes.get(1) != 2) {
            return true;
        }
        return false;
    }

    private boolean shouldTurnOffFullNetworkUpdate() {
        if (this.mIsCharging || this.mIsScreenOn || this.mIsTetheringOn || this.mUpdateModes.get(2) == 2) {
            return false;
        }
        return true;
    }

    private boolean shouldTurnOffDormancyUpdate() {
        if (this.mIsCharging || this.mIsScreenOn || this.mIsTetheringOn || this.mUpdateModes.get(4) == 2) {
            return false;
        }
        return true;
    }

    private boolean shouldTurnOffLinkCapacityEstimate() {
        if (this.mIsCharging || this.mIsScreenOn || this.mIsTetheringOn || this.mUpdateModes.get(8) == 2) {
            return false;
        }
        return true;
    }

    private boolean shouldTurnOffPhysicalChannelConfig() {
        if (this.mIsCharging || this.mIsScreenOn || this.mIsTetheringOn || this.mUpdateModes.get(16) == 2 || this.mIsEndcPwrWorking) {
            return false;
        }
        return true;
    }

    public void setIndicationUpdateMode(int filters, int mode) {
        sendMessage(obtainMessage(1, filters, mode));
    }

    private void onSetIndicationUpdateMode(int filters, int mode) {
        if ((filters & 1) != 0) {
            this.mUpdateModes.put(1, mode);
        }
        if ((filters & 2) != 0) {
            this.mUpdateModes.put(2, mode);
        }
        if ((filters & 4) != 0) {
            this.mUpdateModes.put(4, mode);
        }
        if ((filters & 8) != 0) {
            this.mUpdateModes.put(8, mode);
        }
        if ((filters & 16) != 0) {
            this.mUpdateModes.put(16, mode);
        }
    }

    public void handleMessage(Message msg) {
        boolean z = false;
        log("handleMessage msg=" + msg, false);
        int i = msg.what;
        if (i != 99) {
            switch (i) {
                case 0:
                case 6:
                    onReset();
                    return;
                case 1:
                    onSetIndicationUpdateMode(msg.arg1, msg.arg2);
                    return;
                case 2:
                case 3:
                case 4:
                case 5:
                    break;
                case 7:
                    int i2 = msg.what;
                    if (msg.arg1 != 0) {
                        z = true;
                    }
                    onUpdateDeviceState(i2, z);
                    return;
                default:
                    throw new IllegalStateException("Unexpected message arrives. msg = " + msg.what);
            }
        }
        int i3 = msg.what;
        if (msg.arg1 != 0) {
            z = true;
        }
        onUpdateDeviceState(i3, z);
    }

    private void onUpdateDeviceState(int eventType, boolean state) {
        if (eventType != 2) {
            if (eventType != 3) {
                if (eventType != 4) {
                    if (eventType != 5) {
                        if (eventType != 7) {
                            if (eventType == 99 && this.mIsInCall != state) {
                                this.mIsInCall = state;
                            } else {
                                return;
                            }
                        } else if (this.mIsWifiConnected != state) {
                            this.mIsWifiConnected = state;
                        } else {
                            return;
                        }
                    } else if (this.mIsTetheringOn != state) {
                        this.mIsTetheringOn = state;
                    } else {
                        return;
                    }
                } else if (this.mIsCharging != state) {
                    this.mIsCharging = state;
                    sendDeviceState(1, this.mIsCharging);
                } else {
                    return;
                }
            } else if (this.mIsPowerSaveOn != state) {
                this.mIsPowerSaveOn = state;
                sendDeviceState(0, this.mIsPowerSaveOn);
            } else {
                return;
            }
        } else if (this.mIsScreenOn != state) {
            this.mIsScreenOn = state;
            notifyOemScreenChange(this.mIsScreenOn);
        } else {
            return;
        }
        int newCellInfoMinInterval = computeCellInfoMinInterval();
        if (this.mCellInfoMinInterval != newCellInfoMinInterval) {
            this.mCellInfoMinInterval = newCellInfoMinInterval;
            setCellInfoMinInterval(this.mCellInfoMinInterval);
            log("CellInfo Min Interval Updated to " + newCellInfoMinInterval, true);
        }
        if (this.mIsLowDataExpected != isLowDataExpected()) {
            this.mIsLowDataExpected = true ^ this.mIsLowDataExpected;
            sendDeviceState(2, this.mIsLowDataExpected);
        }
        int newFilter = 0;
        if (!shouldTurnOffSignalStrength()) {
            newFilter = 0 | 1;
        }
        if (!shouldTurnOffFullNetworkUpdate()) {
            newFilter |= 2;
        }
        if (!shouldTurnOffDormancyUpdate()) {
            newFilter |= 4;
        }
        if (!shouldTurnOffLinkCapacityEstimate()) {
            newFilter |= 8;
        }
        if (!shouldTurnOffPhysicalChannelConfig()) {
            newFilter |= 16;
        }
        setUnsolResponseFilter(newFilter, false);
    }

    private void onReset() {
        log("onReset.", true);
        sendDeviceState(1, this.mIsCharging);
        sendDeviceState(2, this.mIsLowDataExpected);
        sendDeviceState(0, this.mIsPowerSaveOn);
        setUnsolResponseFilter(this.mUnsolicitedResponseFilter, true);
        setSignalStrengthReportingCriteria();
        setLinkCapacityReportingCriteria();
        setCellInfoMinInterval(this.mCellInfoMinInterval);
    }

    private String deviceTypeToString(int type) {
        if (type == 0) {
            return "POWER_SAVE_MODE";
        }
        if (type == 1) {
            return "CHARGING_STATE";
        }
        if (type != 2) {
            return "UNKNOWN";
        }
        return "LOW_DATA_EXPECTED";
    }

    private void sendDeviceState(int type, boolean state) {
        log("send type: " + deviceTypeToString(type) + ", state=" + state, true);
        this.mPhone.mCi.sendDeviceState(type, state, null);
    }

    private void setUnsolResponseFilter(int newFilter, boolean force) {
        if (force || newFilter != this.mUnsolicitedResponseFilter) {
            log("old filter: " + this.mUnsolicitedResponseFilter + ", new filter: " + newFilter, true);
            this.mPhone.mCi.setUnsolResponseFilter(newFilter, null);
            this.mUnsolicitedResponseFilter = newFilter;
        }
    }

    private void setSignalStrengthReportingCriteria() {
        this.mPhone.setSignalStrengthReportingCriteria(AccessNetworkThresholds.GERAN, 1);
        this.mPhone.setSignalStrengthReportingCriteria(AccessNetworkThresholds.UTRAN, 2);
        this.mPhone.setSignalStrengthReportingCriteria(AccessNetworkThresholds.EUTRAN, 3);
        this.mPhone.setSignalStrengthReportingCriteria(AccessNetworkThresholds.CDMA2000, 4);
    }

    private void setLinkCapacityReportingCriteria() {
        this.mPhone.setLinkCapacityReportingCriteria(LINK_CAPACITY_DOWNLINK_THRESHOLDS, LINK_CAPACITY_UPLINK_THRESHOLDS, 1);
        this.mPhone.setLinkCapacityReportingCriteria(LINK_CAPACITY_DOWNLINK_THRESHOLDS, LINK_CAPACITY_UPLINK_THRESHOLDS, 2);
        this.mPhone.setLinkCapacityReportingCriteria(LINK_CAPACITY_DOWNLINK_THRESHOLDS, LINK_CAPACITY_UPLINK_THRESHOLDS, 3);
        this.mPhone.setLinkCapacityReportingCriteria(LINK_CAPACITY_DOWNLINK_THRESHOLDS, LINK_CAPACITY_UPLINK_THRESHOLDS, 4);
    }

    private void setCellInfoMinInterval(int rate) {
        this.mPhone.setCellInfoMinInterval(rate);
    }

    /* access modifiers changed from: private */
    public boolean isPowerSaveModeOn() {
        return ((PowerManager) this.mPhone.getContext().getSystemService("power")).isPowerSaveMode();
    }

    private boolean isDeviceCharging() {
        return ((BatteryManager) this.mPhone.getContext().getSystemService("batterymanager")).isCharging();
    }

    /* access modifiers changed from: private */
    public boolean isScreenOn() {
        Display[] displays = ((DisplayManager) this.mPhone.getContext().getSystemService("display")).getDisplays();
        if (displays != null) {
            for (Display display : displays) {
                if (display.getState() == 2) {
                    log("Screen " + Display.typeToString(display.getType()) + " on", true);
                    return true;
                }
            }
            log("Screens all off", true);
            return false;
        }
        log("No displays found", true);
        return false;
    }

    /* access modifiers changed from: private */
    public void log(String msg, boolean logIntoLocalLog) {
        if (DBG) {
            Rlog.d(TAG, msg);
        }
        if (logIntoLocalLog) {
            this.mLocalLog.log(msg);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.increaseIndent();
        ipw.println("mIsTetheringOn=" + this.mIsTetheringOn);
        ipw.println("mIsScreenOn=" + this.mIsScreenOn);
        ipw.println("mIsCharging=" + this.mIsCharging);
        ipw.println("mIsPowerSaveOn=" + this.mIsPowerSaveOn);
        ipw.println("mIsLowDataExpected=" + this.mIsLowDataExpected);
        ipw.println("mUnsolicitedResponseFilter=" + this.mUnsolicitedResponseFilter);
        ipw.println("mIsWifiConnected=" + this.mIsWifiConnected);
        ipw.println("Local logs:");
        ipw.increaseIndent();
        this.mLocalLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.decreaseIndent();
        ipw.flush();
    }

    public void notifyCallStateChange(boolean isInCall) {
        Message msg = obtainMessage(99);
        msg.arg1 = isInCall ? 1 : 0;
        sendMessage(msg);
    }

    public void registerForOemScreenChanged(Handler h, int what, Object obj) {
        this.mOemScreenRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterOemScreenChanged(Handler h) {
        this.mOemScreenRegistrants.remove(h);
    }

    private void notifyOemScreenChange(boolean isScreenOn) {
        this.mOemScreenRegistrants.notifyRegistrants(new AsyncResult((Object) null, Boolean.valueOf(isScreenOn), (Throwable) null));
    }

    private static final class AccessNetworkThresholds {
        public static final int[] CDMA2000 = {-105, -90, -75, -65};
        public static final int[] EUTRAN = {-128, -118, -108, -98};
        public static final int[] GERAN = {-109, -103, -97, -89};
        public static final int[] UTRAN = {-114, -104, -94, -84};

        private AccessNetworkThresholds() {
        }
    }
}
