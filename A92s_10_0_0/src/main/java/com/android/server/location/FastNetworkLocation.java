package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.location.interfaces.IPswFastNetworkLocation;
import java.util.ArrayList;
import java.util.List;

public class FastNetworkLocation implements IPswFastNetworkLocation {
    private static final String KEY_FNL_ENABLE = "config_fastNetworkLocationEnabled";
    private static final int MSG_CELL_LOCATION_CHANGED = 102;
    private static final int MSG_INIT = 101;
    private static final int MSG_SERVICE_STATE_CHANGED = 103;
    private static final String OPPO_SIM_STATE_CHANGED_ACTION = "android.intent.action.SIM_STATE_CHANGED";
    private static final String TAG = "FastNetworkLocation";
    /* access modifiers changed from: private */
    public static boolean mDebug = false;
    /* access modifiers changed from: private */
    public Context mContext = null;
    private Handler mHandler = null;
    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        /* class com.android.server.location.FastNetworkLocation.AnonymousClass1 */

        public boolean handleMessage(Message msg) {
            if (msg.what != FastNetworkLocation.MSG_INIT) {
                return true;
            }
            FastNetworkLocation.this.init();
            return true;
        }
    };
    private Location mLastNetworkLocation = null;
    private OppoLbsRomUpdateUtil mLbsRomUpdateUtil = null;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public boolean mLocationValid = false;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.FastNetworkLocation.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.SCAN_RESULTS")) {
                FastNetworkLocation.this.logd("Location is set to invalid by wifi scan!!");
                boolean unused = FastNetworkLocation.this.mLocationValid = false;
            } else if (action.equals(FastNetworkLocation.OPPO_SIM_STATE_CHANGED_ACTION)) {
                intent.getIntExtra("subscription", -1);
                int slotId = intent.getIntExtra("phone", 0);
                FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
                fastNetworkLocation.logd("get bundle:" + intent.getExtras().toString());
                String state = intent.getStringExtra("ss");
                if (state == null) {
                    Log.e(FastNetworkLocation.TAG, "Get a null sim state!!");
                } else if (!state.equals("READY")) {
                } else {
                    if (FastNetworkLocation.this.mRilListenerForSim1.getSlotId() == slotId) {
                        FastNetworkLocation.this.mRilListenerForSim1.unRegistListener();
                        FastNetworkLocation.this.mRilListenerForSim1.registListener();
                    } else if (FastNetworkLocation.this.mRilListenerForSim2.getSlotId() == slotId) {
                        FastNetworkLocation.this.mRilListenerForSim2.unRegistListener();
                        FastNetworkLocation.this.mRilListenerForSim2.registListener();
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public RilListener mRilListenerForSim1 = null;
    /* access modifiers changed from: private */
    public RilListener mRilListenerForSim2 = null;

    public FastNetworkLocation(Context context, Looper looper) {
        this.mContext = context;
        this.mLbsRomUpdateUtil = OppoLbsRomUpdateUtil.getInstall(context);
        if (looper == null) {
            HandlerThread localThread = new HandlerThread(TAG);
            localThread.start();
            this.mHandler = new Handler(localThread.getLooper(), this.mHandlerCallback);
        } else {
            this.mHandler = new Handler(looper, this.mHandlerCallback);
        }
        this.mHandler.sendMessage(Message.obtain(this.mHandler, (int) MSG_INIT));
    }

    @GuardedBy({"mLock"})
    public void setLastLocation(Location location) {
        Log.d(TAG, "on setLastLocation");
        if (!this.mLocationValid) {
            this.mLastNetworkLocation = new Location(location);
            this.mLocationValid = true;
            logd("Save the location time : " + this.mLastNetworkLocation.getTime());
            return;
        }
        logd("last location is valid, don't need update!!");
    }

    @GuardedBy({"mLock"})
    public Location getValidLocation() {
        Log.d(TAG, "on getValidLocation");
        if (!this.mLbsRomUpdateUtil.getBoolean(KEY_FNL_ENABLE)) {
            return null;
        }
        logd("The location is valid " + this.mLocationValid);
        if (this.mLocationValid) {
            return this.mLastNetworkLocation;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void init() {
        this.mRilListenerForSim1 = new RilListener(0, this.mHandler.getLooper());
        this.mRilListenerForSim2 = new RilListener(1, this.mHandler.getLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        intentFilter.addAction(OPPO_SIM_STATE_CHANGED_ACTION);
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public final void logd(String msg) {
        if (mDebug) {
            Log.d(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    public final class RilListener extends PhoneStateListener {
        private CdmaCellLocation mCdmaCellLocation = null;
        private int mCurrentServiceState = 1;
        private GsmCellLocation mGsmCellLocation = null;
        private ArrayList<CellIdentityLte> mLteCellIdentityList = null;
        private int mMcc = 0;
        private int mMnc = 0;
        private RilHandler mRilHandler = null;
        private int mSlotId = 0;
        private int mSubId = Integer.MAX_VALUE;
        private TelephonyManager mTelephonyMgr = null;

        public RilListener(int slotId, Looper looper) {
            this.mSlotId = slotId;
            this.mRilHandler = new RilHandler(looper);
            registListener();
        }

        public void registListener() {
            int[] subIds = SubscriptionManager.getSubId(this.mSlotId);
            if (subIds != null && subIds.length > 0) {
                this.mSubId = subIds[0];
            }
            FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
            fastNetworkLocation.logd("registListener : Slot id " + this.mSlotId + ", subId " + this.mSubId);
            this.mTelephonyMgr = ((TelephonyManager) FastNetworkLocation.this.mContext.getSystemService(TelephonyManager.class)).createForSubscriptionId(this.mSubId);
            this.mTelephonyMgr.listen(this, 17);
        }

        public void unRegistListener() {
            if (this.mTelephonyMgr != null) {
                FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
                fastNetworkLocation.logd("unRegistListener : Slot id " + this.mSlotId);
                this.mTelephonyMgr.listen(this, 0);
                this.mTelephonyMgr = null;
            }
        }

        public int getSlotId() {
            return this.mSlotId;
        }

        public int getSubId() {
            return this.mSubId;
        }

        public void onCellLocationChanged(CellLocation location) {
            FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
            fastNetworkLocation.logd("deal cellLocationChanged " + location + ", mSlotId " + this.mSlotId);
            this.mRilHandler.sendEmptyMessage(FastNetworkLocation.MSG_CELL_LOCATION_CHANGED);
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
            fastNetworkLocation.logd("CurrentServiceState = " + this.mCurrentServiceState + " NewVoiceServiceState = " + serviceState.getVoiceRegState() + " NewDataServiceState = " + serviceState.getDataRegState() + ", SlotId " + this.mSlotId);
            if (serviceState.getVoiceRegState() == 0 || serviceState.getDataRegState() == 0) {
                this.mCurrentServiceState = 0;
            } else {
                this.mCurrentServiceState = 1;
            }
            this.mRilHandler.sendEmptyMessage(FastNetworkLocation.MSG_CELL_LOCATION_CHANGED);
        }

        private boolean isTheSameLteCell(List<CellIdentityLte> destCell, List<CellIdentityLte> srcCell) {
            if (destCell == null || destCell.size() == 0 || srcCell == null || srcCell.size() == 0) {
                FastNetworkLocation.this.logd("isTheSameLteCell is null");
                return false;
            } else if (destCell.size() != srcCell.size()) {
                return false;
            } else {
                for (CellIdentityLte cell : destCell) {
                    if (!srcCell.contains(cell)) {
                        return false;
                    }
                }
                return true;
            }
        }

        private boolean isTheSameGsmCell(GsmCellLocation destCell, GsmCellLocation srcCell) {
            if (destCell != null && srcCell != null) {
                return destCell.equals(srcCell);
            }
            FastNetworkLocation.this.logd("isTheSameGsmCell is null!!");
            return false;
        }

        private boolean isTheSameCdmaCell(CdmaCellLocation destCell, CdmaCellLocation srcCell) {
            if (destCell == null || srcCell == null) {
                return false;
            }
            return destCell.equals(srcCell);
        }

        private int getMnc(int mncmccCombo, int digits) {
            int mnc = 0;
            if (digits == 6) {
                mnc = mncmccCombo % 1000;
            } else if (digits == 5) {
                mnc = mncmccCombo % 100;
            }
            FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
            fastNetworkLocation.logd("getMnc() - " + mnc);
            return mnc;
        }

        private int getMcc(int mncmccCombo, int digits) {
            int mcc = 0;
            if (digits == 6) {
                mcc = mncmccCombo / 1000;
            } else if (digits == 5) {
                mcc = mncmccCombo / 100;
            }
            FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
            fastNetworkLocation.logd("getMcc() - " + mcc);
            return mcc;
        }

        /* access modifiers changed from: private */
        public void handleCellLocationChanged() {
            CellIdentityLte cellIdentityLte;
            try {
                if (!(this.mCurrentServiceState == 1 || this.mCurrentServiceState == 3)) {
                    if (this.mCurrentServiceState != 2) {
                        String strNetworkOperator = this.mTelephonyMgr.getNetworkOperator();
                        int nwtype = this.mTelephonyMgr.getNetworkType();
                        FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
                        fastNetworkLocation.logd("network type: " + nwtype + ", " + strNetworkOperator);
                        if (nwtype == 13) {
                            ArrayList<CellIdentityLte> lteCellList = new ArrayList<>();
                            boolean mNeedInitList = false;
                            if (this.mLteCellIdentityList == null) {
                                this.mLteCellIdentityList = new ArrayList<>();
                                mNeedInitList = true;
                            }
                            List<CellInfo> cellInfoValue = this.mTelephonyMgr.getAllCellInfo();
                            if (cellInfoValue != null) {
                                for (CellInfo ci : cellInfoValue) {
                                    if ((ci instanceof CellInfoLte) && ci.isRegistered() && (cellIdentityLte = ((CellInfoLte) ci).getCellIdentity()) != null) {
                                        if (mNeedInitList) {
                                            this.mLteCellIdentityList.add(cellIdentityLte);
                                        } else {
                                            lteCellList.add(cellIdentityLte);
                                        }
                                    }
                                }
                                if (!mNeedInitList && !isTheSameLteCell(lteCellList, this.mLteCellIdentityList)) {
                                    FastNetworkLocation.this.logd("Lte cells had changed, valid to false");
                                    boolean unused = FastNetworkLocation.this.mLocationValid = false;
                                    this.mLteCellIdentityList = (ArrayList) lteCellList.clone();
                                    FastNetworkLocation fastNetworkLocation2 = FastNetworkLocation.this;
                                    fastNetworkLocation2.logd("mLte list is" + this.mLteCellIdentityList.toString());
                                }
                            }
                            return;
                        }
                        CellLocation location = this.mTelephonyMgr.getCellLocation();
                        if (location instanceof GsmCellLocation) {
                            GsmCellLocation gsmCell = (GsmCellLocation) location;
                            int mcc = getMcc(Integer.parseInt(strNetworkOperator), strNetworkOperator.length());
                            int mnc = getMnc(Integer.parseInt(strNetworkOperator), strNetworkOperator.length());
                            if (!(isTheSameGsmCell(gsmCell, this.mGsmCellLocation) && mcc == this.mMcc && mnc == this.mMnc)) {
                                FastNetworkLocation.this.logd("Gsm cells had changed, valid to false");
                                this.mMcc = mcc;
                                this.mMnc = mnc;
                                this.mGsmCellLocation = gsmCell;
                                synchronized (FastNetworkLocation.this.mLock) {
                                    boolean unused2 = FastNetworkLocation.this.mLocationValid = false;
                                }
                            }
                            return;
                        } else if (location instanceof CdmaCellLocation) {
                            CdmaCellLocation cdmaCell = (CdmaCellLocation) location;
                            if (!isTheSameCdmaCell(cdmaCell, this.mCdmaCellLocation)) {
                                FastNetworkLocation.this.logd("Cdma cells had changed, valid to false!");
                                this.mCdmaCellLocation = cdmaCell;
                                synchronized (FastNetworkLocation.this.mLock) {
                                    boolean unused3 = FastNetworkLocation.this.mLocationValid = false;
                                }
                                return;
                            }
                            return;
                        } else if (FastNetworkLocation.mDebug) {
                            Log.d(FastNetworkLocation.TAG, "Unknown cell location type!");
                            return;
                        } else {
                            return;
                        }
                    }
                }
                FastNetworkLocation.this.logd("Change service state to Off, valid to false");
                boolean unused4 = FastNetworkLocation.this.mLocationValid = false;
            } catch (Exception e) {
                Log.e(FastNetworkLocation.TAG, "Cannot generate RIL Cell Update Information");
                e.printStackTrace();
            }
        }

        private class RilHandler extends Handler {
            public RilHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                int msgID = msg.what;
                FastNetworkLocation fastNetworkLocation = FastNetworkLocation.this;
                fastNetworkLocation.logd("handleMessage what - " + msgID);
                if (msgID != FastNetworkLocation.MSG_CELL_LOCATION_CHANGED) {
                    FastNetworkLocation.this.logd("Unhandled message");
                } else {
                    RilListener.this.handleCellLocationChanged();
                }
            }
        }
    }

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }
}
