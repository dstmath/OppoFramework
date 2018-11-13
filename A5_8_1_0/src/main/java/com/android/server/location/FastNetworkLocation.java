package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class FastNetworkLocation {
    private static final int MSG_CELL_LOCATION_CHANGED = 102;
    private static final int MSG_INSTALL_RIL_LISTENER = 101;
    private static final int MSG_SERVICE_STATE_CHANGED = 103;
    private static final String TAG = "FastNetworkLocation";
    private CdmaCellLocation mCdmaCellLocation = null;
    private List<CellInfo> mCellInfoValue = null;
    private Context mContext;
    private int mCurrentServiceState = 1;
    private boolean mDebug = false;
    private OppoGnssWhiteListProxy mGnssWhiteListProxy;
    private GsmCellLocation mGsmCellLocation = null;
    private Handler mHandler;
    private Callback mHandlerCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    FastNetworkLocation.this.installRilListener();
                    break;
                case 102:
                    FastNetworkLocation.this.handleCellLocationChanged();
                    break;
                case 103:
                    FastNetworkLocation.this.handleServiceStateChanged(msg.arg1);
                    break;
            }
            return true;
        }
    };
    private long mLastLocationTimer = 0;
    private Location mLastNetworkLocation = null;
    private Object mLock = new Object();
    private int mMcc = 0;
    private int mMnc = 0;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.wifi.SCAN_RESULTS")) {
                synchronized (FastNetworkLocation.this.mLock) {
                    FastNetworkLocation.this.mValid = false;
                }
            }
        }
    };
    private RilListener mRilListener;
    private TelephonyManager mTelephonyMgr;
    private boolean mValid = true;

    private final class RilListener extends PhoneStateListener {
        /* synthetic */ RilListener(FastNetworkLocation this$0, RilListener -this1) {
            this();
        }

        private RilListener() {
        }

        public void onCellLocationChanged(CellLocation location) {
            FastNetworkLocation.this.mHandler.sendEmptyMessage(102);
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            if (FastNetworkLocation.this.mDebug) {
                Log.d(FastNetworkLocation.TAG, "CurrentServiceState = " + FastNetworkLocation.this.mCurrentServiceState + " NewVoiceServiceState = " + serviceState.getVoiceRegState() + " NewDataServiceState = " + serviceState.getDataRegState());
            }
            Message msgServiceStateChanged;
            if (serviceState.getVoiceRegState() == 0 || serviceState.getDataRegState() == 0) {
                msgServiceStateChanged = Message.obtain(FastNetworkLocation.this.mHandler, 103);
                msgServiceStateChanged.arg1 = 0;
                FastNetworkLocation.this.mHandler.sendMessage(msgServiceStateChanged);
                return;
            }
            msgServiceStateChanged = Message.obtain(FastNetworkLocation.this.mHandler, 103);
            msgServiceStateChanged.arg1 = 1;
            FastNetworkLocation.this.mHandler.sendMessage(msgServiceStateChanged);
        }
    }

    public FastNetworkLocation(Context context, Looper looper) {
        this.mContext = context;
        this.mGnssWhiteListProxy = OppoGnssWhiteListProxy.getInstall(this.mContext);
        this.mHandler = new Handler(looper, this.mHandlerCallback);
    }

    public void setLastLocation(Location location) {
        if (this.mLastLocationTimer < location.getTime()) {
            this.mLastNetworkLocation = new Location(location);
            this.mLastLocationTimer = this.mLastNetworkLocation.getTime();
            synchronized (this.mLock) {
                this.mValid = true;
            }
            if (this.mDebug) {
                Log.d(TAG, "Save the location time : " + this.mLastNetworkLocation.getTime());
                return;
            }
            return;
        }
        Log.e(TAG, "location time is older!!");
    }

    /* JADX WARNING: Missing block: B:9:0x000d, code:
            if (r9.mGnssWhiteListProxy == null) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:11:0x0015, code:
            if (r9.mGnssWhiteListProxy.isEnableFastNetworkLocation() == false) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:13:0x0019, code:
            if (r9.mLastNetworkLocation == null) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:14:0x001b, code:
            r0 = java.lang.System.currentTimeMillis();
     */
    /* JADX WARNING: Missing block: B:15:0x0021, code:
            if (r9.mDebug == false) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:16:0x0023, code:
            android.util.Log.d(TAG, "lastTime " + r9.mLastLocationTimer + ", currTime " + r0);
     */
    /* JADX WARNING: Missing block: B:18:0x0056, code:
            if ((r0 - r9.mLastLocationTimer) >= r9.mGnssWhiteListProxy.getNetworkStandTime()) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:20:0x005a, code:
            if (r9.mDebug == false) goto L_0x0065;
     */
    /* JADX WARNING: Missing block: B:21:0x005c, code:
            android.util.Log.d(TAG, "Start FLP feature!!");
     */
    /* JADX WARNING: Missing block: B:22:0x0065, code:
            r2 = new android.location.Location(r9.mLastNetworkLocation);
            r2.setTime(r0);
     */
    /* JADX WARNING: Missing block: B:23:0x006f, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:27:0x0073, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Location getValidLocation() {
        synchronized (this.mLock) {
            if (!this.mValid) {
                return null;
            }
        }
    }

    private void installRilListener() {
        this.mTelephonyMgr = (TelephonyManager) this.mContext.getSystemService("phone");
        if (this.mTelephonyMgr == null) {
            Log.e(TAG, "Unable to get TELEPHONY_SERVICE");
            return;
        }
        this.mRilListener = new RilListener(this, null);
        this.mTelephonyMgr.listen(this.mRilListener, 17);
    }

    private void initScanResultReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    private void handleServiceStateChanged(int service_state) {
        if (this.mCurrentServiceState != service_state) {
            this.mCurrentServiceState = service_state;
            handleCellLocationChanged();
        }
    }

    private boolean isTheSameLteCell(List<CellInfo> destCell, List<CellInfo> srcCell) {
        if (destCell.size() != srcCell.size()) {
            return false;
        }
        for (CellInfo cell : destCell) {
            if (!srcCell.contains(cell)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTheSameGsmCell(GsmCellLocation destCell, GsmCellLocation srcCell) {
        if (destCell.getCid() == srcCell.getCid() && destCell.getLac() == srcCell.getLac()) {
            return true;
        }
        return false;
    }

    private boolean isTheSameCdmaCell(CdmaCellLocation destCell, CdmaCellLocation srcCell) {
        if (destCell.getSystemId() == srcCell.getSystemId() && destCell.getNetworkId() == srcCell.getNetworkId() && destCell.getBaseStationId() == srcCell.getBaseStationId() && destCell.getBaseStationLatitude() == srcCell.getBaseStationLatitude() && destCell.getBaseStationLongitude() == srcCell.getBaseStationLongitude()) {
            return true;
        }
        return false;
    }

    private int getMnc(int mncmccCombo, int digits) {
        int mnc = 0;
        if (digits == 6) {
            mnc = mncmccCombo % 1000;
        } else if (digits == 5) {
            mnc = mncmccCombo % 100;
        }
        Log.d(TAG, "getMnc() - " + mnc);
        return mnc;
    }

    private int getMcc(int mncmccCombo, int digits) {
        int mcc = 0;
        if (digits == 6) {
            mcc = mncmccCombo / 1000;
        } else if (digits == 5) {
            mcc = mncmccCombo / 100;
        }
        Log.d(TAG, "getMcc() - " + mcc);
        return mcc;
    }

    private void handleCellLocationChanged() {
        try {
            CellLocation location = this.mTelephonyMgr.getCellLocation();
            if (this.mCurrentServiceState == 1 || this.mCurrentServiceState == 3 || this.mCurrentServiceState == 2) {
                if (this.mDebug) {
                    Log.d(TAG, "Change service state to Off, valid to false");
                }
                synchronized (this.mLock) {
                    this.mValid = false;
                }
                return;
            }
            String strNetworkOperator = this.mTelephonyMgr.getNetworkOperator();
            int nwtype = this.mTelephonyMgr.getNetworkType();
            if (this.mDebug) {
                Log.d(TAG, "network type: " + nwtype + ", " + strNetworkOperator);
            }
            if (nwtype == 13) {
                List<CellInfo> cellInfoValue = this.mTelephonyMgr.getAllCellInfo();
                if (cellInfoValue == null) {
                    return;
                }
                if (this.mCellInfoValue == null) {
                    this.mCellInfoValue = new ArrayList();
                    System.arraycopy(cellInfoValue, 0, this.mCellInfoValue, 0, cellInfoValue.size());
                } else if (!isTheSameLteCell(cellInfoValue, this.mCellInfoValue)) {
                    if (this.mDebug) {
                        Log.d(TAG, "Lte cells had changed, valid to false");
                    }
                    synchronized (this.mLock) {
                        this.mValid = false;
                    }
                    System.arraycopy(cellInfoValue, 0, this.mCellInfoValue, 0, cellInfoValue.size());
                }
            } else if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmCell = (GsmCellLocation) location;
                int mcc = getMcc(Integer.parseInt(strNetworkOperator), strNetworkOperator.length());
                int mnc = getMnc(Integer.parseInt(strNetworkOperator), strNetworkOperator.length());
                if (!isTheSameGsmCell(gsmCell, this.mGsmCellLocation) || mcc != this.mMcc || mnc != this.mMnc) {
                    if (this.mDebug) {
                        Log.d(TAG, "Gsm cells had changed, valid to false");
                    }
                    this.mMcc = mcc;
                    this.mMnc = mnc;
                    this.mGsmCellLocation = gsmCell;
                    synchronized (this.mLock) {
                        this.mValid = false;
                    }
                }
            } else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaCell = (CdmaCellLocation) location;
                if (!isTheSameCdmaCell(cdmaCell, this.mCdmaCellLocation)) {
                    if (this.mDebug) {
                        Log.d(TAG, "Cdma cells had changed, valid to false!");
                    }
                    this.mCdmaCellLocation = cdmaCell;
                    synchronized (this.mLock) {
                        this.mValid = false;
                    }
                }
            } else if (this.mDebug) {
                Log.d(TAG, "Unknown cell location type!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot generate RIL Cell Update Information");
            e.printStackTrace();
        }
    }

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }
}
