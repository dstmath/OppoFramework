package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CarrierAppUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IOppoUiccManager;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.MultiSimSettingController;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoSimlockManager;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccSlot;
import com.android.internal.telephony.util.OppoVdfLocaleUpdateUtils;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.phb.MtkIccPhoneBookInterfaceManager;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.internal.telephony.uicc.MtkSpnOverride;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MtkSubscriptionInfoUpdater extends SubscriptionInfoUpdater {
    private static final String COMMON_SLOT_PROPERTY = "ro.vendor.mtk_sim_hot_swap_common_slot";
    private static final boolean DBG = true;
    private static final boolean ENGDEBUG = TextUtils.equals(Build.TYPE, "eng");
    private static final int EVENT_RADIO_AVAILABLE = 101;
    private static final int EVENT_RADIO_UNAVAILABLE = 102;
    private static final int EVENT_SIM_MOUNT_CHANGED = 106;
    private static final int EVENT_SIM_NO_CHANGED = 103;
    private static final int EVENT_SIM_PLUG_OUT = 105;
    private static final int EVENT_TRAY_PLUG_IN = 104;
    private static final String ICCID_STRING_FOR_NO_SIM = "N/A";
    private static final String LOG_TAG = "MtkSubscriptionInfoUpdater";
    private static final boolean MTK_FLIGHTMODE_POWEROFF_MD_SUPPORT = "1".equals(SystemProperties.get("ro.vendor.mtk_flight_mode_power_off_md"));
    private static final String[] PROPERTY_ICCID_SIM = {"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"};
    private static final String PROPERTY_SML_MODE = "ro.vendor.sim_me_lock_mode";
    private static MtkSubscriptionInfoUpdater sInstance = null;
    private static boolean sIsMultiSimSettingControllerInitialized = false;
    private static final int sReadICCID_retry_time = 1000;
    private CommandsInterface[] mCis = null;
    private boolean mCommonSlotResetDone = false;
    private boolean mIsSmlLockMode = SystemProperties.get(PROPERTY_SML_MODE, "").equals("3");
    private int[] mIsUpdateAvailable = new int[PROJECT_SIM_NUM];
    private final Object mLock = new Object();
    private final Object mLockUpdateNew = new Object();
    private final Object mLockUpdateOld = new Object();
    private final BroadcastReceiver mMtkReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkSubscriptionInfoUpdater.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MtkSubscriptionInfoUpdater.this.logd("onReceive, Action: " + action);
            if (action.equals("android.intent.action.LOCALE_CHANGED")) {
                for (int subId : MtkSubscriptionInfoUpdater.this.mSubscriptionManager.getActiveSubscriptionIdList()) {
                    MtkSubscriptionInfoUpdater.this.updateSubName(subId);
                }
            } else if (action.equals("com.mediatek.phone.ACTION_COMMON_SLOT_NO_CHANGED")) {
                int slotIndex = intent.getIntExtra("phone", -1);
                MtkSubscriptionInfoUpdater.this.logd("[Common Slot] NO_CHANTED, slotId: " + slotIndex);
                MtkSubscriptionInfoUpdater mtkSubscriptionInfoUpdater = MtkSubscriptionInfoUpdater.this;
                mtkSubscriptionInfoUpdater.sendMessage(mtkSubscriptionInfoUpdater.obtainMessage(MtkSubscriptionInfoUpdater.EVENT_SIM_NO_CHANGED, slotIndex, -1));
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED") && MtkSubscriptionInfoUpdater.this.oldDensityDpi != MtkSubscriptionInfoUpdater.mContext.getResources().getConfiguration().densityDpi) {
                MtkSubscriptionInfoUpdater.this.oldDensityDpi = MtkSubscriptionInfoUpdater.mContext.getResources().getConfiguration().densityDpi;
                MtkSubscriptionController.getMtkInstance().refreshCachedActiveSubscriptionInfoList();
            }
        }
    };
    OppoSimlockManager mOppoSimlockManager = null;
    private int mReadIccIdCount = 0;
    private Runnable mReadIccIdPropertyRunnable = new Runnable() {
        /* class com.mediatek.internal.telephony.MtkSubscriptionInfoUpdater.AnonymousClass2 */

        public void run() {
            MtkSubscriptionInfoUpdater.access$604(MtkSubscriptionInfoUpdater.this);
            if (MtkSubscriptionInfoUpdater.this.mReadIccIdCount > 10) {
                return;
            }
            if (!MtkSubscriptionInfoUpdater.this.checkAllIccIdReady()) {
                MtkSubscriptionInfoUpdater mtkSubscriptionInfoUpdater = MtkSubscriptionInfoUpdater.this;
                mtkSubscriptionInfoUpdater.postDelayed(mtkSubscriptionInfoUpdater.mReadIccIdPropertyRunnable, 1000);
                return;
            }
            MtkSubscriptionInfoUpdater.this.updateSubscriptionInfoIfNeed();
        }
    };
    private boolean mSimMountChangeState = false;
    private int[] newSmlInfo = {4, 0, -1, -1};
    private int oldDensityDpi;
    private int[] oldSmlInfo = {4, 0, -1, -1};

    static /* synthetic */ int access$604(MtkSubscriptionInfoUpdater x0) {
        int i = x0.mReadIccIdCount + 1;
        x0.mReadIccIdCount = i;
        return i;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.mediatek.internal.telephony.MtkSubscriptionInfoUpdater */
    /* JADX WARN: Multi-variable type inference failed */
    public MtkSubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        super(looper, context, phone, ci);
        IOppoUiccManager uiccManager;
        logd("MtkSubscriptionInfoUpdater created");
        this.mCis = ci;
        this.oldDensityDpi = mContext.getResources().getConfiguration().densityDpi;
        IntentFilter intentFilter = new IntentFilter("com.mediatek.phone.ACTION_COMMON_SLOT_NO_CHANGED");
        if (DataSubConstants.OPERATOR_OP09.equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR))) {
            intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        }
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        mContext.registerReceiver(this.mMtkReceiver, intentFilter);
        for (int i = 0; i < this.mCis.length; i++) {
            Integer index = new Integer(i);
            this.mCis[i].registerForNotAvailable(this, 102, index);
            this.mCis[i].registerForAvailable(this, 101, index);
            if (SystemProperties.get(COMMON_SLOT_PROPERTY).equals("1")) {
                this.mCis[i].registerForSimTrayPlugIn(this, 104, index);
                this.mCis[i].registerForSimPlugOut(this, 105, index);
            }
        }
        if (OemConstant.EXP_VERSION && (uiccManager = OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0])) != null) {
            this.mOppoSimlockManager = uiccManager.createOppoSimlockManager(phone, ci, context);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAllIccIdQueryDone() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (mIccId[i] == null || mIccId[i].equals("")) {
                logd("Wait for SIM " + i + " Iccid");
                return false;
            }
        }
        logd("All IccIds query complete");
        return true;
    }

    public void handleMessage(Message msg) {
        Integer index = getCiIndex(msg);
        int i = msg.what;
        if (i == 3) {
            DcTracker mDcTracker = mPhone[msg.arg1].getDcTracker(1);
            if (mDcTracker != null) {
                mDcTracker.setDataRoamingEnabledForOperator(msg.arg1);
            }
            MtkSubscriptionInfoUpdater.super.handleMessage(msg);
        } else if (i != 10) {
            switch (i) {
                case 101:
                    logd("handleMessage : <EVENT_RADIO_AVAILABLE> SIM" + (index.intValue() + 1));
                    this.mIsUpdateAvailable[index.intValue()] = 1;
                    if (checkIsAvailable()) {
                        this.mReadIccIdCount = 0;
                        if (!checkAllIccIdReady()) {
                            postDelayed(this.mReadIccIdPropertyRunnable, 1000);
                            return;
                        } else {
                            updateSubscriptionInfoIfNeed();
                            return;
                        }
                    } else {
                        return;
                    }
                case 102:
                    logd("handleMessage : <EVENT_RADIO_UNAVAILABLE> SIM" + (index.intValue() + 1));
                    this.mIsUpdateAvailable[index.intValue()] = 0;
                    if (SystemProperties.get(COMMON_SLOT_PROPERTY).equals("1")) {
                        logd("[Common slot] reset mCommonSlotResetDone in EVENT_RADIO_UNAVAILABLE");
                        this.mCommonSlotResetDone = false;
                        return;
                    }
                    return;
                case EVENT_SIM_NO_CHANGED /* 103 */:
                    if (checkAllIccIdReady()) {
                        updateSubscriptionInfoIfNeed();
                        return;
                    }
                    int slotId = msg.arg1;
                    mIccId[slotId] = "N/A";
                    logd("case SIM_NO_CHANGED: set N/A for slot" + slotId);
                    this.mReadIccIdCount = 0;
                    postDelayed(this.mReadIccIdPropertyRunnable, 1000);
                    return;
                case 104:
                    logd("[Common Slot] handle EVENT_TRAY_PLUG_IN " + this.mCommonSlotResetDone);
                    if (!this.mCommonSlotResetDone) {
                        this.mCommonSlotResetDone = true;
                        if (!ExternalSimManager.isAnyVsimEnabled()) {
                            this.mReadIccIdCount = 0;
                            if (!checkAllIccIdReady()) {
                                postDelayed(this.mReadIccIdPropertyRunnable, 1000);
                                return;
                            } else {
                                updateSubscriptionInfoIfNeed();
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case 105:
                    logd("[Common Slot] handle EVENT_SIM_PLUG_OUT " + this.mCommonSlotResetDone);
                    this.mCommonSlotResetDone = false;
                    return;
                case EVENT_SIM_MOUNT_CHANGED /* 106 */:
                    int[] iArr = this.newSmlInfo;
                    updateNewSmlInfo(iArr[0], iArr[1]);
                    resetSimMountChangeState();
                    return;
                default:
                    MtkSubscriptionInfoUpdater.super.handleMessage(msg);
                    return;
            }
        } else {
            updateSubscriptionInfoIfNeed();
            IOppoUiccManager oppoUiccManager = OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0]);
            if (oppoUiccManager != null) {
                oppoUiccManager.updateSimReadyExt(msg.arg1);
            }
            MtkSubscriptionInfoUpdater.super.handleMessage(msg);
        }
    }

    /* access modifiers changed from: protected */
    public void handleSimLocked(int slotId, String reason) {
        OppoSimlockManager oppoSimlockManager;
        if (mIccId[slotId] != null && mIccId[slotId].equals("N/A")) {
            logd("SIM" + (slotId + 1) + " hot plug in");
            mIccId[slotId] = null;
        }
        String iccId = mIccId[slotId];
        if (iccId == null) {
            IccCard iccCard = mPhone[slotId].getIccCard();
            if (iccCard == null) {
                logd("handleSimLocked: IccCard null");
                return;
            } else if (iccCard.getIccRecords() == null) {
                logd("handleSimLocked: IccRecords null");
                return;
            }
        } else {
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + MtkSubscriptionInfo.givePrintableIccid(iccId));
            String tempIccid = SystemProperties.get(PROPERTY_ICCID_SIM[slotId], "");
            if (MTK_FLIGHTMODE_POWEROFF_MD_SUPPORT && !checkAllIccIdReady() && !tempIccid.equals(mIccId[slotId])) {
                logd("All iccids are not ready and iccid changed");
                mIccId[slotId] = null;
                this.mSubscriptionManager.clearSubscriptionInfo();
            }
        }
        updateSubscriptionInfoIfNeed();
        broadcastSimStateChanged(slotId, "LOCKED", reason);
        broadcastSimCardStateChanged(slotId, 11);
        broadcastSimApplicationStateChanged(slotId, getSimStateFromLockedReason(reason));
        updateSubscriptionCarrierId(slotId, "LOCKED");
        updateCarrierServices(slotId, "LOCKED");
        if (OemConstant.EXP_VERSION && "NETWORK".equals(reason) && (oppoSimlockManager = this.mOppoSimlockManager) != null) {
            oppoSimlockManager.handleOppoSimlocked(slotId);
        }
    }

    /* access modifiers changed from: protected */
    public void handleSimLoaded(int slotId) {
        String vdfSimMccmnc;
        IccRecords records;
        int subId;
        String nameToSet;
        String iccid;
        logd("handleSimLoaded: slotId: " + slotId);
        IccCard iccCard = mPhone[slotId].getIccCard();
        if (iccCard == null) {
            logd("handleSimLoaded: IccCard null");
            return;
        }
        IccRecords records2 = iccCard.getIccRecords();
        if (records2 == null) {
            logd("handleSimLoaded: IccRecords null");
        } else if (IccUtils.stripTrailingFs(records2.getFullIccId()) == null) {
            logd("handleSimLoaded: IccID null");
        } else {
            updateSubscriptionInfoIfNeed();
            List<SubscriptionInfo> subscriptionInfos = MtkSubscriptionController.getMtkInstance().getSubInfoUsingSlotIndexPrivileged(slotId);
            if (subscriptionInfos != null) {
                if (!subscriptionInfos.isEmpty()) {
                    for (SubscriptionInfo sub : subscriptionInfos) {
                        int subId2 = sub.getSubscriptionId();
                        TelephonyManager tm = TelephonyManager.from(mContext);
                        String operator = tm.getSimOperatorNumeric(subId2);
                        if (!TextUtils.isEmpty(operator)) {
                            if (subId2 == MtkSubscriptionController.getMtkInstance().getDefaultSubId()) {
                                MccTable.updateMccMncConfiguration(mContext, operator);
                            }
                            MtkSubscriptionController.getMtkInstance().setMccMnc(operator, subId2);
                        } else {
                            logd("EVENT_RECORDS_LOADED Operator name is null");
                        }
                        String iso = tm.getSimCountryIsoForPhone(slotId);
                        if (!TextUtils.isEmpty(iso)) {
                            MtkSubscriptionController.getMtkInstance().setCountryIso(iso, subId2);
                        } else {
                            logd("EVENT_RECORDS_LOADED sim country iso is null");
                        }
                        String msisdn = tm.getLine1Number(subId2);
                        if (msisdn != null) {
                            MtkSubscriptionController.getMtkInstance().setDisplayNumber(msisdn, subId2, false);
                        }
                        String imsi = tm.createForSubscriptionId(subId2).getSubscriberId();
                        if (imsi != null) {
                            MtkSubscriptionController.getMtkInstance().setImsi(imsi, subId2);
                        }
                        String[] ehplmns = records2.getEhplmns();
                        String[] hplmns = records2.getPlmnsFromHplmnActRecord();
                        if (!(ehplmns == null && hplmns == null)) {
                            MtkSubscriptionController.getMtkInstance().setAssociatedPlmns(ehplmns, hplmns, subId2);
                        }
                        SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId2);
                        String simCarrierName = tm.getSimOperatorName(subId2);
                        if (subInfo == null || subInfo.getNameSource() == 2) {
                            records = records2;
                            subId = subId2;
                        } else {
                            if (!OemConstant.EXP_VERSION) {
                                if (mIccId == null || slotId >= mIccId.length || slotId < 0) {
                                    iccid = "";
                                } else {
                                    iccid = mIccId[slotId];
                                }
                                int cardType = getCardType(imsi, iccid);
                                logd("[handleSimLoaded] cardType = " + cardType);
                                if (cardType == 1) {
                                    records = records2;
                                    subId = subId2;
                                    nameToSet = MtkSubscriptionController.getMtkInstance().getCarrierName(mContext, "", "", iccid, slotId);
                                } else {
                                    records = records2;
                                    subId = subId2;
                                    nameToSet = MtkSubscriptionController.getMtkInstance().getCarrierName(mContext, simCarrierName, imsi, iccid, slotId);
                                }
                            } else if (SystemProperties.getInt("gsm.vsim.slotid", -1) == slotId) {
                                nameToSet = OemConstant.getOemRes(mContext, "redtea_virtul_card", "");
                                logd("It's red tea,sim name = " + nameToSet);
                                records = records2;
                                subId = subId2;
                            } else {
                                nameToSet = SubscriptionController.getInstance().getExportSimDefaultName(slotId);
                                records = records2;
                                subId = subId2;
                            }
                            logd("sim name = " + nameToSet + ", nameSource = " + subInfo.getNameSource());
                            MtkSubscriptionController.getMtkInstance().setDisplayNameUsingSrc(nameToSet, subId, subInfo.getNameSource());
                        }
                        String[] imsi_lteCdma = mPhone[slotId].getLteCdmaImsi(slotId);
                        String iccid2 = mPhone[slotId].getIccSerialNumber();
                        if ("" != imsi_lteCdma[0]) {
                            setCardTypeInColor(subId, imsi_lteCdma[0], iccid2);
                            MtkSubscriptionController.getMtkInstance().setIconTint(getCardType(imsi_lteCdma[0], iccid2), subId);
                        } else {
                            setCardTypeInColor(subId, imsi_lteCdma[1], iccid2);
                            MtkSubscriptionController.getMtkInstance().setIconTint(getCardType(imsi_lteCdma[1], iccid2), subId);
                        }
                        broadcastCardTypeUpdateIntent(Integer.toString(slotId), Integer.toString(subId), "CARDTYPE");
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                        if (sp.getInt("curr_subid" + slotId, -1) != subId) {
                            ContentResolver contentResolver = mPhone[slotId].getContext().getContentResolver();
                            int networkType = Settings.Global.getInt(contentResolver, "preferred_network_mode" + subId, -1);
                            if (networkType == -1) {
                                int networkType2 = RILConstants.PREFERRED_NETWORK_MODE;
                                try {
                                    networkType = TelephonyManager.getIntAtIndex(mContext.getContentResolver(), "preferred_network_mode", slotId);
                                } catch (Settings.SettingNotFoundException e) {
                                    Rlog.e(LOG_TAG, "Settings Exception Reading Value At Index for Settings.Global.PREFERRED_NETWORK_MODE");
                                    networkType = networkType2;
                                }
                                ContentResolver contentResolver2 = mPhone[slotId].getContext().getContentResolver();
                                Settings.Global.putInt(contentResolver2, "preferred_network_mode" + subId, networkType);
                            }
                            mPhone[slotId].setPreferredNetworkType(networkType, (Message) null);
                            mPhone[slotId].getNetworkSelectionMode(obtainMessage(2, new Integer(slotId)));
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt("curr_subid" + slotId, subId);
                            editor.apply();
                        }
                        IOppoUiccManager oppoUiccManager = OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0]);
                        if (oppoUiccManager != null) {
                            oppoUiccManager.updateSimLoadedExt(slotId, mIccId);
                        }
                        iccCard = iccCard;
                        subscriptionInfos = subscriptionInfos;
                        records2 = records;
                    }
                    vdfSimMccmnc = new OppoVdfLocaleUpdateUtils().getVdfMccmncIfExist(mContext, slotId);
                    if (vdfSimMccmnc != null && !vdfSimMccmnc.isEmpty()) {
                        MccTable.updateVdfPersistedLocale(mContext, vdfSimMccmnc);
                    }
                    CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
                    broadcastSimStateChanged(slotId, "LOADED", null);
                    broadcastSimCardStateChanged(slotId, 11);
                    broadcastSimApplicationStateChanged(slotId, 10);
                    updateSubscriptionCarrierId(slotId, "LOADED");
                    updateCarrierServices(slotId, "LOADED");
                }
            }
            loge("empty subinfo for slotId: " + slotId + "could not update ContentResolver");
            vdfSimMccmnc = new OppoVdfLocaleUpdateUtils().getVdfMccmncIfExist(mContext, slotId);
            MccTable.updateVdfPersistedLocale(mContext, vdfSimMccmnc);
            CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
            broadcastSimStateChanged(slotId, "LOADED", null);
            broadcastSimCardStateChanged(slotId, 11);
            broadcastSimApplicationStateChanged(slotId, 10);
            updateSubscriptionCarrierId(slotId, "LOADED");
            updateCarrierServices(slotId, "LOADED");
        }
    }

    /* access modifiers changed from: protected */
    public void handleSimAbsent(int slotId, int absentAndInactive) {
        OppoSimlockManager oppoSimlockManager;
        if (mIccId[slotId] != null && !mIccId[slotId].equals("N/A")) {
            logd("SIM" + (slotId + 1) + " hot plug out, absentAndInactive=" + absentAndInactive);
        }
        TelephonyManager.setTelephonyProperty(slotId, "persist.radio.nitz_oper_code", "");
        TelephonyManager.setTelephonyProperty(slotId, "persist.radio.nitz_oper_lname", "");
        TelephonyManager.setTelephonyProperty(slotId, "persist.radio.nitz_oper_sname", "");
        try {
            IccPhoneBookInterfaceManager iccPbkIntMgr = mPhone[slotId].getIccPhoneBookInterfaceManager();
            if (iccPbkIntMgr != null) {
                ((MtkIccPhoneBookInterfaceManager) iccPbkIntMgr).resetSimNameLength();
            }
        } catch (Exception e) {
        }
        updateSubscriptionInfoIfNeed();
        if (absentAndInactive == 0) {
            broadcastSimStateChanged(slotId, "ABSENT", null);
            broadcastSimCardStateChanged(slotId, 1);
            broadcastSimApplicationStateChanged(slotId, 0);
            updateSubscriptionCarrierId(slotId, "ABSENT");
            updateCarrierServices(slotId, "ABSENT");
        }
        if (OemConstant.EXP_VERSION && (oppoSimlockManager = this.mOppoSimlockManager) != null) {
            oppoSimlockManager.handleOppoAbsentOrError(slotId);
        }
    }

    /* access modifiers changed from: protected */
    public void handleSimError(int slotId) {
        OppoSimlockManager oppoSimlockManager;
        if (mIccId[slotId] != null && !mIccId[slotId].equals("N/A")) {
            logd("SIM" + (slotId + 1) + " Error ");
        }
        mIccId[slotId] = "N/A";
        updateSubscriptionInfoByIccId(slotId, true);
        broadcastSimStateChanged(slotId, "CARD_IO_ERROR", "CARD_IO_ERROR");
        broadcastSimCardStateChanged(slotId, 8);
        broadcastSimApplicationStateChanged(slotId, 6);
        updateSubscriptionCarrierId(slotId, "CARD_IO_ERROR");
        updateCarrierServices(slotId, "CARD_IO_ERROR");
        if (OemConstant.EXP_VERSION && (oppoSimlockManager = this.mOppoSimlockManager) != null) {
            oppoSimlockManager.handleOppoAbsentOrError(slotId);
        }
    }

    /* access modifiers changed from: protected */
    public void handleSimNotReady(int slotId) {
        logd("handleSimNotReady: slotId: " + slotId);
        IccCard iccCard = mPhone[slotId].getIccCard();
        UiccSlot slot = UiccController.getInstance().getUiccSlotForPhone(slotId);
        if (slot == null || !slot.isEuicc() || !iccCard.isEmptyProfile()) {
            updateSubscriptionInfoIfNeed();
        } else {
            mIccId[slotId] = "N/A";
            updateSubscriptionInfoByIccId(slotId, false);
        }
        broadcastSimStateChanged(slotId, "NOT_READY", null);
        broadcastSimCardStateChanged(slotId, 11);
        broadcastSimApplicationStateChanged(slotId, 6);
    }

    /* access modifiers changed from: protected */
    public synchronized void updateSubscriptionInfoByIccId(int slotIndex, boolean updateEmbeddedSubs) {
        String oldIccId;
        int detectedType;
        logd("updateSubscriptionInfoByIccId:+ Start");
        if (!SubscriptionManager.isValidSlotIndex(slotIndex)) {
            loge("[updateSubscriptionInfoByIccId]- invalid slotIndex=" + slotIndex);
            return;
        }
        IOppoUiccManager uiccManager = OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0]);
        if (uiccManager != null) {
            uiccManager.enableHypnusAction();
        }
        this.mCommonSlotResetDone = false;
        MtkSubscriptionController.getMtkInstance();
        String initIccid = MtkSubscriptionController.initIccid[slotIndex];
        if (!TextUtils.isEmpty(initIccid)) {
            MtkSubscriptionController.getMtkInstance();
            MtkSubscriptionController.initIccid[slotIndex] = "";
            oldIccId = initIccid;
        } else {
            List<SubscriptionInfo> oldSubInfo = MtkSubscriptionController.getMtkInstance().getSubInfoUsingSlotIndexPrivileged(slotIndex);
            if (oldSubInfo != null) {
                oldIccId = oldSubInfo.get(0).getIccId();
            } else {
                oldIccId = "N/A";
            }
        }
        logd("updateSubscriptionInfoByIccId: removing subscription info record: slotIndex:" + slotIndex + " initIccid:" + MtkSubscriptionInfo.givePrintableIccid(initIccid) + " oldIccId:" + MtkSubscriptionInfo.givePrintableIccid(oldIccId));
        MtkSubscriptionController.getMtkInstance().clearSubInfoRecord(slotIndex);
        if (!"N/A".equals(mIccId[slotIndex])) {
            logd("updateSubscriptionInfoByIccId: adding subscription info record: iccid: " + MtkSubscriptionInfo.givePrintableIccid(mIccId[slotIndex]) + "  slot: " + slotIndex);
            this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[slotIndex], slotIndex);
        }
        List<SubscriptionInfo> subInfos = MtkSubscriptionController.getMtkInstance().getSubInfoUsingSlotIndexPrivileged(slotIndex);
        if (subInfos != null) {
            boolean changed = false;
            for (int i = 0; i < subInfos.size(); i++) {
                SubscriptionInfo temp = subInfos.get(i);
                ContentValues value = new ContentValues(1);
                String msisdn = TelephonyManager.from(mContext).getLine1Number(temp.getSubscriptionId());
                if (!TextUtils.equals(msisdn, temp.getNumber())) {
                    value.put(PplMessageManager.PendingMessage.KEY_NUMBER, msisdn);
                    mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(temp.getSubscriptionId()), value, null, null);
                    changed = true;
                }
            }
            if (changed) {
                MtkSubscriptionController.getMtkInstance().refreshCachedActiveSubscriptionInfoList();
            }
        }
        List<SubscriptionInfo> subInfoList = MtkSubscriptionController.getMtkInstance().getActiveSubscriptionInfoList(mContext.getOpPackageName());
        if (isAllIccIdQueryDone()) {
            MtkDefaultSmsSimSettings.setSmsTalkDefaultSim(subInfoList, mContext);
            if (SystemProperties.getInt("ro.vendor.mtk_external_sim_support", 0) == 1 && SystemProperties.getInt("ro.vendor.mtk_non_dsda_rsim_support", 0) == 1) {
                int rsimPhoneId = SystemProperties.getInt("vendor.gsm.prefered.rsim.slot", -1);
                int[] rsimSubId = MtkSubscriptionController.getMtkInstance().getSubId(rsimPhoneId);
                if (rsimPhoneId >= 0 && rsimPhoneId < PROJECT_SIM_NUM && rsimSubId != null && rsimSubId.length != 0) {
                    MtkSubscriptionController.getMtkInstance().setDefaultDataSubId(rsimSubId[0]);
                }
            }
            MtkSubscriptionController.getMtkInstance().setDefaultDataSubIdWithoutCapabilitySwitch(calculateDataSubId());
            setSubInfoInitialized();
        }
        String decIccId = IccUtils.getDecimalSubstring(mIccId[slotIndex]);
        if (mIccId[slotIndex] != null && mIccId[slotIndex].equals("N/A") && !oldIccId.equals("N/A")) {
            detectedType = 2;
        } else if (isNewSim(mIccId[slotIndex], decIccId, oldIccId)) {
            detectedType = 1;
        } else {
            detectedType = 4;
        }
        int subCount = subInfoList == null ? 0 : subInfoList.size();
        if (this.mIsSmlLockMode) {
            updateNewSmlInfo(detectedType, subCount);
            resetSimMountChangeState();
        }
        IOppoUiccManager oppoUiccManager = OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0]);
        if (oppoUiccManager != null) {
            oppoUiccManager.updateSubscriptionInfoByIccIdExt(mContext, mIccId);
        }
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        MtkSubscriptionController.getMtkInstance().putSubinfoRecordUpdatedExtra(intent, slotIndex, detectedType, subCount, null);
        UiccController uiccController = UiccController.getInstance();
        UiccSlot[] uiccSlots = uiccController.getUiccSlots();
        if (uiccSlots != null && updateEmbeddedSubs) {
            List<Integer> cardIds = new ArrayList<>();
            for (UiccSlot uiccSlot : uiccSlots) {
                if (!(uiccSlot == null || uiccSlot.getUiccCard() == null)) {
                    try {
                        cardIds.add(Integer.valueOf(uiccController.convertToPublicCardId(uiccSlot.getUiccCard().getCardId())));
                    } catch (NullPointerException e) {
                        logd("updateSubscriptionInfoByIccId uiccSlots.getUiccCard() is null.");
                    }
                }
            }
            updateEmbeddedSubscriptions(cardIds, new SubscriptionInfoUpdater.UpdateEmbeddedSubsCallback() {
                /* class com.mediatek.internal.telephony.$$Lambda$MtkSubscriptionInfoUpdater$IcslM9hUPlWmI6G65crWTNur4q4 */

                public final void run(boolean z) {
                    MtkSubscriptionInfoUpdater.this.lambda$updateSubscriptionInfoByIccId$0$MtkSubscriptionInfoUpdater(z);
                }
            });
        }
        MtkSubscriptionController.getMtkInstance().notifySubscriptionInfoChanged(intent);
        logd("updateSubscriptionInfoByIccId: SubscriptionInfo update complete: slotIndex" + slotIndex + " detectedType = " + detectedType + " subCount = " + subCount);
        if (isAllIccIdQueryDone() && !sIsMultiSimSettingControllerInitialized) {
            sIsMultiSimSettingControllerInitialized = true;
            MultiSimSettingController.getInstance().notifyAllSubscriptionLoaded();
        }
    }

    public /* synthetic */ void lambda$updateSubscriptionInfoByIccId$0$MtkSubscriptionInfoUpdater(boolean hasChanges) {
        if (hasChanges) {
            MtkSubscriptionController.getMtkInstance().notifySubscriptionInfoChanged();
        }
        logd("updateSubscriptionInfoByIccId: SubscriptionInfo update complete");
    }

    protected static void setSubInfoInitialized() {
        if (!sIsSubInfoInitialized) {
            sIsSubInfoInitialized = true;
            SubscriptionController.getInstance().notifySubInfoReady();
        }
    }

    private boolean isNewSim(String iccId, String decIccId, String oldIccId) {
        boolean newSim = true;
        if (iccId != null && oldIccId != null && (oldIccId.indexOf(iccId) == 0 || oldIccId.indexOf(iccId.toLowerCase()) == 0)) {
            newSim = false;
        } else if (decIccId != null && decIccId.equals(oldIccId)) {
            newSim = false;
        }
        logd("isNewSim newSim = " + newSim);
        return newSim;
    }

    public void dispose() {
        logd("[dispose]");
        mContext.unregisterReceiver(this.mMtkReceiver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkAllIccIdReady() {
        logd("checkAllIccIdReady +, retry_count = " + this.mReadIccIdCount);
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            String iccId = SystemProperties.get(PROPERTY_ICCID_SIM[i], "");
            if (iccId.length() == 3) {
                logd("No SIM insert :" + i);
            }
            if (iccId.equals("")) {
                return false;
            }
            logd("iccId[" + i + "] = " + MtkSubscriptionInfo.givePrintableIccid(iccId));
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSubscriptionInfoIfNeed() {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            boolean needUpdate = false;
            logd("[updateSubscriptionInfoIfNeed] before update mIccId[" + i + "]: " + MtkSubscriptionInfo.givePrintableIccid(mIccId[i]));
            if (mIccId[i] == null || !mIccId[i].equals(SystemProperties.get(PROPERTY_ICCID_SIM[i], ""))) {
                mIccId[i] = SystemProperties.get(PROPERTY_ICCID_SIM[i], "");
                needUpdate = true;
                StringBuilder sb = new StringBuilder();
                sb.append("[updateSubscriptionInfoIfNeed] mIccId[");
                sb.append(i);
                sb.append("]: ");
                sb.append(MtkSubscriptionInfo.givePrintableIccid(mIccId[i]));
                sb.append(" needUpdate: ");
                sb.append(true);
                sb.append("  !TextUtils.isEmpty(mIccId[");
                sb.append(i);
                sb.append("]): ");
                sb.append(!TextUtils.isEmpty(mIccId[i]));
                logd(sb.toString());
            }
            if (needUpdate && !TextUtils.isEmpty(mIccId[i])) {
                updateSubscriptionInfoByIccId(i, true);
            }
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = new Integer(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return (Integer) ar.userObj;
    }

    private boolean checkIsAvailable() {
        boolean result = true;
        int i = 0;
        while (true) {
            if (i >= PROJECT_SIM_NUM) {
                break;
            } else if (this.mIsUpdateAvailable[i] <= 0) {
                logd("mIsUpdateAvailable[" + i + "] = " + this.mIsUpdateAvailable[i]);
                result = false;
                break;
            } else {
                i++;
            }
        }
        logd("checkIsAvailable result = " + result);
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSubName(int subId) {
        String nameToSet;
        SubscriptionInfo subInfo = MtkSubscriptionManager.getSubInfo((String) null, subId);
        if (subInfo != null && subInfo.getNameSource() != 2) {
            MtkSpnOverride spnOverride = MtkSpnOverride.getInstance();
            String carrierName = TelephonyManager.getDefault().getSimOperator(subId);
            int slotId = SubscriptionManager.getSlotIndex(subId);
            logd("updateSubName, carrierName = " + carrierName + ", subId = " + subId);
            if (SubscriptionManager.isValidSlotIndex(slotId)) {
                if (spnOverride.containsCarrierEx(carrierName)) {
                    nameToSet = spnOverride.lookupOperatorName(subId, carrierName, true, mContext);
                    logd("SPN found, name = " + nameToSet);
                } else {
                    nameToSet = "CARD " + Integer.toString(slotId + 1);
                    logd("SPN not found, set name to " + nameToSet);
                }
                MtkSubscriptionController.getMtkInstance().setDisplayNameUsingSrc(nameToSet, subId, 0);
            }
        }
    }

    public void updateCardIdInfo() {
        int numPhysicalSlots = mContext.getResources().getInteger(17694873);
        if (numPhysicalSlots < PROJECT_SIM_NUM) {
            numPhysicalSlots = PROJECT_SIM_NUM;
        }
        UiccController uiccController = UiccController.getInstance();
        UiccSlot[] uiccSlots = uiccController.getUiccSlots();
        if (uiccSlots != null) {
            for (int i = 0; i < numPhysicalSlots; i++) {
                if (uiccSlots[i] != null && uiccSlots[i].getUiccCard() != null && uiccSlots[i].getCardState() != IccCardStatus.CardState.CARDSTATE_ABSENT) {
                    List<SubscriptionInfo> subInfos = MtkSubscriptionController.getMtkInstance().getSubInfoUsingSlotIndexPrivileged(i);
                    if (subInfos != null && !subInfos.isEmpty()) {
                        try {
                            List<Integer> cardIds = new ArrayList<>();
                            int cardId = uiccController.convertToPublicCardId(uiccSlots[i].getUiccCard().getCardId());
                            cardIds.add(Integer.valueOf(cardId));
                            for (int j = 0; j < subInfos.size(); j++) {
                                SubscriptionInfo temp = subInfos.get(j);
                                if (!(temp == null || temp.getCardId() == cardId)) {
                                    ContentValues value = new ContentValues(1);
                                    value.put("card_id", Integer.valueOf(cardId));
                                    mContext.getContentResolver().update(SubscriptionManager.getUriForSubscriptionId(temp.getSubscriptionId()), value, null, null);
                                    MtkSubscriptionController.getMtkInstance().refreshCachedActiveSubscriptionInfoList();
                                    updateEmbeddedSubscriptions(cardIds, null);
                                }
                            }
                        } catch (NullPointerException e) {
                            logd("updateCardIdInfo uiccSlots[" + i + "].getUiccCard() is null.");
                        }
                    } else if (ENGDEBUG) {
                        logd("updateCardIdInfo slot " + i + "subInfos is null.");
                    }
                } else if (ENGDEBUG) {
                    logd("updateCardIdInfo uiccSlots[" + i + "] or uiccSlots[" + i + "].getUiccCard() is null.");
                }
            }
        }
    }

    public void triggerUpdateInternalSimMountState(int slotId) {
        logd("triggerUpdateInternalSimMountState slotId " + slotId);
        sendMessage(obtainMessage(EVENT_SIM_MOUNT_CHANGED, Integer.valueOf(slotId)));
    }

    private void resetSimMountChangeState() {
        boolean needReport = false;
        int i = 0;
        while (true) {
            if (i >= 4) {
                break;
            } else if (this.newSmlInfo[i] != this.oldSmlInfo[i]) {
                needReport = true;
                break;
            } else {
                i++;
            }
        }
        if (needReport) {
            int[] iArr = this.newSmlInfo;
            int newDetectedType = iArr[0];
            int newSimCount = iArr[1];
            int newValid1 = iArr[2];
            int newValid2 = iArr[3];
            Intent intent = new Intent("com.mediatek.phone.ACTION_SIM_SLOT_SIM_MOUNT_CHANGE");
            intent.putExtra("DETECTED_TYPE", newDetectedType);
            intent.putExtra("SML_SIM_COUNT", newSimCount);
            intent.putExtra("SML_SIM1_VALID", newValid1);
            intent.putExtra("SML_SIM2_VALID", newValid2);
            logd("Broadcasting ACTION_SIM_SLOT_SIM_MOUNT_CHANGE,  detected type: " + newDetectedType + ", newSubCount: " + newSimCount + ", SIM 1 valid" + newValid1 + ", SIM 2 valid" + newValid2);
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            updateOldSmlInfo(newDetectedType, newSimCount, newValid1, newValid2);
            return;
        }
        logd("resetSimMountChangeState no  need report ");
    }

    public void updateNewSmlInfo(int detectedType, int simCount) {
        synchronized (this.mLockUpdateNew) {
            this.newSmlInfo[0] = detectedType;
            this.newSmlInfo[1] = simCount;
            this.newSmlInfo[2] = MtkTelephonyManagerEx.getDefault().checkValidCard(0);
            this.newSmlInfo[3] = MtkTelephonyManagerEx.getDefault().checkValidCard(1);
            logd("[updateNewSmlInfo]- [" + this.newSmlInfo[0] + ", " + this.newSmlInfo[1] + ", " + this.newSmlInfo[2] + ", " + this.newSmlInfo[3] + "]");
        }
    }

    public void updateOldSmlInfo(int detectedType, int simCount, int valid1, int valid2) {
        synchronized (this.mLockUpdateOld) {
            this.oldSmlInfo[0] = detectedType;
            this.oldSmlInfo[1] = simCount;
            this.oldSmlInfo[2] = valid1;
            this.oldSmlInfo[3] = valid2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String message) {
        Rlog.d(LOG_TAG, message);
    }

    private void loge(String message) {
        Rlog.e(LOG_TAG, message);
    }

    public int calculateDataSubId() {
        SubscriptionManager subscriptionManager = this.mSubscriptionManager;
        int oldDatasubId = SubscriptionManager.getDefaultDataSubscriptionId();
        int calDataSubId = -1;
        int primarySlot = Settings.Global.getInt(mContext.getContentResolver(), "oppo_multi_sim_network_primary_slot", 0);
        logd("calculateDataSubId oldDatasubId:" + oldDatasubId + ", primarySlot:" + primarySlot);
        SubscriptionController subController = SubscriptionController.getInstance();
        if (subController == null) {
            return oldDatasubId;
        }
        List<SubscriptionInfo> subList = subController.getActiveSubscriptionInfoList(mContext.getOpPackageName());
        if (subList != null) {
            int activeSimRef = 0;
            Iterator<SubscriptionInfo> it = subList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                SubscriptionInfo si = it.next();
                if (1 == subController.getSubState(si.getSubscriptionId())) {
                    activeSimRef++;
                    if (si.getSimSlotIndex() == primarySlot) {
                        calDataSubId = si.getSubscriptionId();
                        logd("calculateDataSubId get subId:" + calDataSubId + " from primarySlot:" + primarySlot);
                        break;
                    } else if (-1 == calDataSubId) {
                        calDataSubId = si.getSubscriptionId();
                    }
                }
            }
            if (activeSimRef == 0 && 1 == subList.size()) {
                Iterator<SubscriptionInfo> it2 = subList.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    SubscriptionInfo si2 = it2.next();
                    if (si2.getSimSlotIndex() == primarySlot) {
                        calDataSubId = si2.getSubscriptionId();
                        logd("calculateDataSubId activeSimRef == 0, calDataSubId:" + calDataSubId);
                        break;
                    }
                }
            }
            if (calDataSubId == -1) {
                Iterator<SubscriptionInfo> it3 = subList.iterator();
                while (true) {
                    if (!it3.hasNext()) {
                        break;
                    }
                    SubscriptionInfo si3 = it3.next();
                    if (si3.getSimSlotIndex() == primarySlot) {
                        calDataSubId = si3.getSubscriptionId();
                        break;
                    }
                    calDataSubId = si3.getSubscriptionId();
                }
            }
        }
        int calDataSubId2 = calDataSubId == -1 ? oldDatasubId : calDataSubId;
        logd("calculateDataSubId return calDataSubId:" + calDataSubId2);
        return calDataSubId2;
    }
}
