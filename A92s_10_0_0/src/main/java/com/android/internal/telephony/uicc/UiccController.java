package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.app.BroadcastOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.UiccCardInfo;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.RadioConfig;
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.uicc.IccSlotStatus;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UiccController extends AbstractUiccController {
    public static final int APP_FAM_3GPP = 1;
    public static final int APP_FAM_3GPP2 = 2;
    public static final int APP_FAM_IMS = 3;
    private static final String CARD_STRINGS = "card_strings";
    protected static final boolean DBG = true;
    private static final String DEFAULT_CARD = "default_card";
    private static final int EID_LENGTH = 32;
    private static final int EVENT_EID_READY = 9;
    protected static final int EVENT_GET_ICC_STATUS_DONE = 3;
    protected static final int EVENT_GET_SLOT_STATUS_DONE = 4;
    protected static final int EVENT_ICC_STATUS_CHANGED = 1;
    protected static final int EVENT_RADIO_AVAILABLE = 6;
    protected static final int EVENT_RADIO_ON = 5;
    private static final int EVENT_RADIO_UNAVAILABLE = 7;
    private static final int EVENT_SIM_REFRESH = 8;
    private static final int EVENT_SLOT_STATUS_CHANGED = 2;
    public static final int INVALID_SLOT_ID = -1;
    private static final String LOG_TAG = "UiccController";
    private static final int TEMPORARILY_UNSUPPORTED_CARD_ID = -3;
    protected static final boolean VDBG = false;
    @UnsupportedAppUsage
    private static UiccController mInstance;
    @UnsupportedAppUsage
    protected static final Object mLock = new Object();
    private static ArrayList<IccSlotStatus> sLastSlotStatus;
    static LocalLog sLocalLog = new LocalLog(100);
    private ArrayList<String> mCardStrings;
    @UnsupportedAppUsage
    protected CommandsInterface[] mCis;
    @UnsupportedAppUsage
    @VisibleForTesting
    public Context mContext;
    private int mDefaultEuiccCardId;
    protected RegistrantList mIccChangedRegistrants = new RegistrantList();
    private boolean mIsSlotStatusSupported = true;
    private UiccStateChangedLauncher mLauncher;
    private int[] mPhoneIdToSlotId;
    protected RadioConfig mRadioConfig;
    @VisibleForTesting
    public UiccSlot[] mUiccSlots;

    public static UiccController make(Context c, CommandsInterface[] ci) {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeUiccController(c, ci);
                uiccController = mInstance;
            } else {
                throw new RuntimeException("UiccController.make() should only be called once");
            }
        }
        return uiccController;
    }

    public UiccController(Context c, CommandsInterface[] ci) {
        log("Creating UiccController");
        this.mContext = c;
        this.mCis = ci;
        String logStr = "config_num_physical_slots = " + c.getResources().getInteger(17694873);
        log(logStr);
        sLocalLog.log(logStr);
        int numPhysicalSlots = c.getResources().getInteger(17694873);
        CommandsInterface[] commandsInterfaceArr = this.mCis;
        this.mUiccSlots = new UiccSlot[(numPhysicalSlots < commandsInterfaceArr.length ? commandsInterfaceArr.length : numPhysicalSlots)];
        this.mPhoneIdToSlotId = new int[ci.length];
        Arrays.fill(this.mPhoneIdToSlotId, -1);
        this.mRadioConfig = RadioConfig.getInstance(this.mContext);
        this.mRadioConfig.registerForSimSlotStatusChanged(this, 2, null);
        int i = 0;
        while (true) {
            CommandsInterface[] commandsInterfaceArr2 = this.mCis;
            if (i < commandsInterfaceArr2.length) {
                commandsInterfaceArr2[i].registerForIccStatusChanged(this, 1, Integer.valueOf(i));
                if (!StorageManager.inCryptKeeperBounce()) {
                    this.mCis[i].registerForAvailable(this, 6, Integer.valueOf(i));
                } else {
                    this.mCis[i].registerForOn(this, 5, Integer.valueOf(i));
                }
                this.mCis[i].registerForNotAvailable(this, 7, Integer.valueOf(i));
                this.mCis[i].registerForIccRefresh(this, 8, Integer.valueOf(i));
                i++;
            } else {
                this.mLauncher = new UiccStateChangedLauncher(c, this);
                this.mCardStrings = loadCardStrings();
                this.mDefaultEuiccCardId = -2;
                this.mReference = (IOppoUiccController) OppoTelephonyFactory.getInstance().getFeature(IOppoUiccController.DEFAULT, this);
                return;
            }
        }
    }

    public int getPhoneIdFromSlotId(int slotId) {
        int i = 0;
        while (true) {
            int[] iArr = this.mPhoneIdToSlotId;
            if (i >= iArr.length) {
                return -1;
            }
            if (iArr[i] == slotId) {
                return i;
            }
            i++;
        }
    }

    public int getSlotIdFromPhoneId(int phoneId) {
        try {
            return this.mPhoneIdToSlotId[phoneId];
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    @UnsupportedAppUsage
    public static UiccController getInstance() {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance != null) {
                uiccController = mInstance;
            } else {
                throw new RuntimeException("UiccController.getInstance can't be called before make()");
            }
        }
        return uiccController;
    }

    @UnsupportedAppUsage
    public UiccCard getUiccCard(int phoneId) {
        UiccCard uiccCardForPhone;
        synchronized (mLock) {
            uiccCardForPhone = getUiccCardForPhone(phoneId);
        }
        return uiccCardForPhone;
    }

    public UiccCard getUiccCardForSlot(int slotId) {
        synchronized (mLock) {
            UiccSlot uiccSlot = getUiccSlot(slotId);
            if (uiccSlot == null) {
                return null;
            }
            UiccCard uiccCard = uiccSlot.getUiccCard();
            return uiccCard;
        }
    }

    public UiccCard getUiccCardForPhone(int phoneId) {
        UiccSlot uiccSlot;
        synchronized (mLock) {
            if (!isValidPhoneIndex(phoneId) || (uiccSlot = getUiccSlotForPhone(phoneId)) == null) {
                return null;
            }
            UiccCard uiccCard = uiccSlot.getUiccCard();
            return uiccCard;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        return r2;
     */
    public UiccProfile getUiccProfileForPhone(int phoneId) {
        synchronized (mLock) {
            UiccProfile uiccProfile = null;
            if (!isValidPhoneIndex(phoneId)) {
                return null;
            }
            UiccCard uiccCard = getUiccCardForPhone(phoneId);
            if (uiccCard != null) {
                uiccProfile = uiccCard.getUiccProfile();
            }
        }
    }

    public UiccSlot[] getUiccSlots() {
        UiccSlot[] uiccSlotArr;
        synchronized (mLock) {
            uiccSlotArr = this.mUiccSlots;
        }
        return uiccSlotArr;
    }

    public void switchSlots(int[] physicalSlots, Message response) {
        this.mRadioConfig.setSimSlotsMapping(physicalSlots, response);
    }

    public UiccSlot getUiccSlot(int slotId) {
        synchronized (mLock) {
            if (!isValidSlotIndex(slotId)) {
                return null;
            }
            UiccSlot uiccSlot = this.mUiccSlots[slotId];
            return uiccSlot;
        }
    }

    public UiccSlot getUiccSlotForPhone(int phoneId) {
        synchronized (mLock) {
            if (isValidPhoneIndex(phoneId)) {
                int slotId = getSlotIdFromPhoneId(phoneId);
                if (isValidSlotIndex(slotId)) {
                    UiccSlot uiccSlot = this.mUiccSlots[slotId];
                    return uiccSlot;
                }
            }
            return null;
        }
    }

    public int getUiccSlotForCardId(String cardId) {
        UiccCard uiccCard;
        synchronized (mLock) {
            for (int idx = 0; idx < this.mUiccSlots.length; idx++) {
                if (this.mUiccSlots[idx] != null && (uiccCard = this.mUiccSlots[idx].getUiccCard()) != null && cardId.equals(uiccCard.getCardId())) {
                    return idx;
                }
            }
            for (int idx2 = 0; idx2 < this.mUiccSlots.length; idx2++) {
                if (this.mUiccSlots[idx2] != null && cardId.equals(this.mUiccSlots[idx2].getIccId())) {
                    return idx2;
                }
            }
            return -1;
        }
    }

    @UnsupportedAppUsage
    public IccRecords getIccRecords(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app == null) {
                return null;
            }
            IccRecords iccRecords = app.getIccRecords();
            return iccRecords;
        }
    }

    @UnsupportedAppUsage
    public IccFileHandler getIccFileHandler(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app == null) {
                return null;
            }
            IccFileHandler iccFileHandler = app.getIccFileHandler();
            return iccFileHandler;
        }
    }

    @UnsupportedAppUsage
    public void registerForIccChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mIccChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForIccChanged(Handler h) {
        synchronized (mLock) {
            this.mIccChangedRegistrants.remove(h);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00e0, code lost:
        return;
     */
    public void handleMessage(Message msg) {
        synchronized (mLock) {
            Integer phoneId = getCiIndex(msg);
            if (phoneId.intValue() >= 0) {
                if (phoneId.intValue() < this.mCis.length) {
                    LocalLog localLog = sLocalLog;
                    localLog.log("handleMessage: Received " + msg.what + " for phoneId " + phoneId);
                    AsyncResult ar = (AsyncResult) msg.obj;
                    switch (msg.what) {
                        case 1:
                            log("Received EVENT_ICC_STATUS_CHANGED, calling getIccCardStatus");
                            this.mCis[phoneId.intValue()].getIccCardStatus(obtainMessage(3, phoneId));
                            break;
                        case 2:
                        case 4:
                            log("Received EVENT_SLOT_STATUS_CHANGED or EVENT_GET_SLOT_STATUS_DONE");
                            onGetSlotStatusDone(ar);
                            break;
                        case 3:
                            log("Received EVENT_GET_ICC_STATUS_DONE");
                            onGetIccCardStatusDone(ar, phoneId);
                            break;
                        case 5:
                        case 6:
                            log("Received EVENT_RADIO_AVAILABLE/EVENT_RADIO_ON, calling getIccCardStatus");
                            this.mCis[phoneId.intValue()].getIccCardStatus(obtainMessage(3, phoneId));
                            if (phoneId.intValue() == 0) {
                                log("Received EVENT_RADIO_AVAILABLE/EVENT_RADIO_ON for phoneId 0, calling getIccSlotsStatus");
                                this.mRadioConfig.getSimSlotsStatus(obtainMessage(4, phoneId));
                                break;
                            }
                            break;
                        case 7:
                            log("EVENT_RADIO_UNAVAILABLE, dispose card");
                            UiccSlot uiccSlot = getUiccSlotForPhone(phoneId.intValue());
                            if (uiccSlot != null) {
                                uiccSlot.onRadioStateUnavailable();
                            }
                            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, phoneId, (Throwable) null));
                            break;
                        case 8:
                            log("Received EVENT_SIM_REFRESH");
                            onSimRefresh(ar, phoneId);
                            break;
                        case 9:
                            log("Received EVENT_EID_READY");
                            onEidReady(ar, phoneId);
                            break;
                        default:
                            Rlog.e(LOG_TAG, " Unknown Event " + msg.what);
                            break;
                    }
                }
            }
            Rlog.e(LOG_TAG, "Invalid phoneId : " + phoneId + " received with event " + msg.what);
        }
    }

    /* access modifiers changed from: protected */
    public Integer getCiIndex(Message msg) {
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

    @UnsupportedAppUsage
    public UiccCardApplication getUiccCardApplication(int phoneId, int family) {
        synchronized (mLock) {
            UiccCard uiccCard = getUiccCardForPhone(phoneId);
            if (uiccCard == null) {
                return null;
            }
            UiccCardApplication application = uiccCard.getApplication(family);
            return application;
        }
    }

    /* renamed from: com.android.internal.telephony.uicc.UiccController$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$IccCardConstants$State = new int[IccCardConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PIN_REQUIRED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PUK_REQUIRED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.NETWORK_LOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.READY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.NOT_READY.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.PERM_DISABLED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.CARD_IO_ERROR.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.CARD_RESTRICTED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.LOADED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State[IccCardConstants.State.IMSI.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
        }
    }

    static String getIccStateIntentString(IccCardConstants.State state) {
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$IccCardConstants$State[state.ordinal()]) {
            case 1:
                return "ABSENT";
            case 2:
                return "LOCKED";
            case 3:
                return "LOCKED";
            case 4:
                return "LOCKED";
            case 5:
                return "READY";
            case 6:
                return "NOT_READY";
            case 7:
                return "LOCKED";
            case 8:
                return "CARD_IO_ERROR";
            case 9:
                return "CARD_RESTRICTED";
            case 10:
                return "LOADED";
            case 11:
                return "IMSI";
            default:
                return "UNKNOWN";
        }
    }

    public static void updateInternalIccState(Context context, IccCardConstants.State state, String reason, int phoneId) {
        updateInternalIccState(context, state, reason, phoneId, false);
    }

    static void updateInternalIccState(Context context, IccCardConstants.State state, String reason, int phoneId, boolean absentAndInactive) {
        ((TelephonyManager) context.getSystemService("phone")).setSimStateForPhone(phoneId, state.toString());
        SubscriptionInfoUpdater subInfoUpdator = PhoneFactory.getSubscriptionInfoUpdater();
        if (subInfoUpdator != null) {
            subInfoUpdator.updateInternalIccState(getIccStateIntentString(state), reason, phoneId, absentAndInactive);
        } else {
            Rlog.e(LOG_TAG, "subInfoUpdate is null.");
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        String cardString;
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error", ar.exception);
        } else if (!isValidPhoneIndex(index.intValue())) {
            Rlog.e(LOG_TAG, "onGetIccCardStatusDone: invalid index : " + index);
        } else {
            IccCardStatus status = (IccCardStatus) ar.result;
            LocalLog localLog = sLocalLog;
            localLog.log("onGetIccCardStatusDone: phoneId " + index + " IccCardStatus: " + status);
            int slotId = status.physicalSlotIndex;
            if (slotId == -1) {
                slotId = index.intValue();
            }
            if (eidIsNotSupported(status)) {
                log("eid is not supported");
                this.mDefaultEuiccCardId = -1;
            }
            this.mPhoneIdToSlotId[index.intValue()] = slotId;
            if (this.mUiccSlots[slotId] == null) {
                this.mUiccSlots[slotId] = new UiccSlot(this.mContext, true);
            }
            this.mUiccSlots[slotId].update(this.mCis[index.intValue()], status, index.intValue(), slotId);
            UiccCard card = this.mUiccSlots[slotId].getUiccCard();
            if (card == null) {
                log("mUiccSlots[" + slotId + "] has no card. Notifying IccChangedRegistrants");
                this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, index, (Throwable) null));
                return;
            }
            boolean isEuicc = this.mUiccSlots[slotId].isEuicc();
            if (isEuicc) {
                cardString = ((EuiccCard) card).getEid();
            } else {
                cardString = card.getIccId();
            }
            if (isEuicc && cardString == null && this.mDefaultEuiccCardId != -1) {
                ((EuiccCard) card).registerForEidReady(this, 9, index);
            }
            if (cardString != null) {
                addCardId(cardString);
            }
            log("Notifying IccChangedRegistrants");
            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, index, (Throwable) null));
        }
    }

    private boolean eidIsNotSupported(IccCardStatus status) {
        return status.physicalSlotIndex == -1;
    }

    private void addCardId(String cardString) {
        if (!TextUtils.isEmpty(cardString)) {
            if (cardString.length() < 32) {
                cardString = IccUtils.stripTrailingFs(cardString);
            }
            if (!this.mCardStrings.contains(cardString)) {
                this.mCardStrings.add(cardString);
                saveCardStrings();
            }
        }
    }

    public int convertToPublicCardId(String cardString) {
        if (this.mDefaultEuiccCardId == -1) {
            return -1;
        }
        if (TextUtils.isEmpty(cardString)) {
            return -2;
        }
        if (cardString.length() < 32) {
            cardString = IccUtils.stripTrailingFs(cardString);
        }
        int id = this.mCardStrings.indexOf(cardString);
        if (id == -1) {
            return -2;
        }
        return id;
    }

    public ArrayList<UiccCardInfo> getAllUiccCardInfos() {
        String iccid;
        String eid;
        int cardId;
        ArrayList<UiccCardInfo> infos = new ArrayList<>();
        int slotIndex = 0;
        while (true) {
            UiccSlot[] uiccSlotArr = this.mUiccSlots;
            if (slotIndex >= uiccSlotArr.length) {
                return infos;
            }
            UiccSlot slot = uiccSlotArr[slotIndex];
            if (slot != null) {
                boolean isEuicc = slot.isEuicc();
                UiccCard card = slot.getUiccCard();
                boolean isRemovable = slot.isRemovable();
                if (card != null) {
                    String iccid2 = card.getIccId();
                    if (isEuicc) {
                        String eid2 = ((EuiccCard) card).getEid();
                        eid = eid2;
                        iccid = iccid2;
                        cardId = convertToPublicCardId(eid2);
                    } else {
                        eid = null;
                        iccid = iccid2;
                        cardId = convertToPublicCardId(iccid2);
                    }
                } else {
                    String iccid3 = slot.getIccId();
                    if (isEuicc || TextUtils.isEmpty(iccid3)) {
                        eid = null;
                        iccid = iccid3;
                        cardId = -2;
                    } else {
                        eid = null;
                        iccid = iccid3;
                        cardId = convertToPublicCardId(iccid3);
                    }
                }
                infos.add(new UiccCardInfo(isEuicc, cardId, eid, IccUtils.stripTrailingFs(iccid), slotIndex, isRemovable));
            }
            slotIndex++;
        }
    }

    public int getCardIdForDefaultEuicc() {
        int i = this.mDefaultEuiccCardId;
        if (i == -3) {
            return -1;
        }
        return i;
    }

    private ArrayList<String> loadCardStrings() {
        String cardStrings = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(CARD_STRINGS, PhoneConfigurationManager.SSSS);
        if (TextUtils.isEmpty(cardStrings)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(cardStrings.split(",")));
    }

    private void saveCardStrings() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putString(CARD_STRINGS, TextUtils.join(",", this.mCardStrings));
        editor.commit();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0048, code lost:
        return;
     */
    private synchronized void onGetSlotStatusDone(AsyncResult ar) {
        if (this.mIsSlotStatusSupported) {
            Throwable e = ar.exception;
            int i = 0;
            if (e == null) {
                ArrayList<IccSlotStatus> status = (ArrayList) ar.result;
                if (!slotStatusChanged(status)) {
                    log("onGetSlotStatusDone: No change in slot status");
                    return;
                }
                sLastSlotStatus = status;
                int numActiveSlots = 0;
                boolean isDefaultEuiccCardIdSet = false;
                boolean anyEuiccIsActive = false;
                boolean hasEuicc = false;
                int i2 = 0;
                while (true) {
                    boolean isActive = true;
                    if (i2 >= status.size()) {
                        break;
                    }
                    IccSlotStatus iss = status.get(i2);
                    if (iss.slotState != IccSlotStatus.SlotState.SLOTSTATE_ACTIVE) {
                        isActive = false;
                    }
                    if (isActive) {
                        numActiveSlots++;
                        if (!isValidPhoneIndex(iss.logicalSlotIndex)) {
                            Rlog.e(LOG_TAG, "Skipping slot " + i2 + " as phone " + iss.logicalSlotIndex + " is not available to communicate with this slot");
                        } else {
                            this.mPhoneIdToSlotId[iss.logicalSlotIndex] = i2;
                        }
                    }
                    if (this.mUiccSlots[i2] == null) {
                        this.mUiccSlots[i2] = new UiccSlot(this.mContext, isActive);
                    }
                    CommandsInterface commandsInterface = null;
                    if (!isValidPhoneIndex(iss.logicalSlotIndex)) {
                        this.mUiccSlots[i2].update(null, iss, i2);
                    } else {
                        UiccSlot uiccSlot = this.mUiccSlots[i2];
                        if (isActive) {
                            commandsInterface = this.mCis[iss.logicalSlotIndex];
                        }
                        uiccSlot.update(commandsInterface, iss, i2);
                    }
                    if (this.mUiccSlots[i2].isEuicc()) {
                        hasEuicc = true;
                        if (isActive) {
                            anyEuiccIsActive = true;
                        }
                        String eid = iss.eid;
                        if (!TextUtils.isEmpty(eid)) {
                            addCardId(eid);
                            if (!isDefaultEuiccCardIdSet) {
                                isDefaultEuiccCardIdSet = true;
                                this.mDefaultEuiccCardId = convertToPublicCardId(eid);
                                log("Using eid=" + eid + " in slot=" + i2 + " to set mDefaultEuiccCardId=" + this.mDefaultEuiccCardId);
                            }
                        }
                    }
                    i2++;
                }
                if (hasEuicc && !anyEuiccIsActive && !isDefaultEuiccCardIdSet) {
                    log("onGetSlotStatusDone: setting TEMPORARILY_UNSUPPORTED_CARD_ID");
                    this.mDefaultEuiccCardId = -3;
                }
                if (numActiveSlots != this.mPhoneIdToSlotId.length) {
                    Rlog.e(LOG_TAG, "Number of active slots " + numActiveSlots + " does not match the number of Phones" + this.mPhoneIdToSlotId.length);
                }
                Set<Integer> slotIds = new HashSet<>();
                int[] iArr = this.mPhoneIdToSlotId;
                int length = iArr.length;
                while (i < length) {
                    int slotId = iArr[i];
                    if (!slotIds.contains(Integer.valueOf(slotId))) {
                        slotIds.add(Integer.valueOf(slotId));
                        i++;
                    } else {
                        throw new RuntimeException("slotId " + slotId + " mapped to multiple phoneIds");
                    }
                }
                BroadcastOptions options = BroadcastOptions.makeBasic();
                options.setBackgroundActivityStartsAllowed(true);
                Intent intent = new Intent("android.telephony.action.SIM_SLOT_STATUS_CHANGED");
                intent.addFlags(67108864);
                intent.addFlags(16777216);
                this.mContext.sendBroadcast(intent, "android.permission.READ_PRIVILEGED_PHONE_STATE", options.toBundle());
            } else if (!(e instanceof CommandException) || ((CommandException) e).getCommandError() != CommandException.Error.REQUEST_NOT_SUPPORTED) {
                String logStr = "Unexpected error getting slot status: " + ar.exception;
                Rlog.e(LOG_TAG, logStr);
                sLocalLog.log(logStr);
            } else {
                log("onGetSlotStatusDone: request not supported; marking mIsSlotStatusSupported to false");
                sLocalLog.log("onGetSlotStatusDone: request not supported; marking mIsSlotStatusSupported to false");
                this.mIsSlotStatusSupported = false;
            }
        }
    }

    private boolean slotStatusChanged(ArrayList<IccSlotStatus> slotStatusList) {
        ArrayList<IccSlotStatus> arrayList = sLastSlotStatus;
        if (arrayList == null || arrayList.size() != slotStatusList.size()) {
            return true;
        }
        Iterator<IccSlotStatus> it = slotStatusList.iterator();
        while (it.hasNext()) {
            if (!sLastSlotStatus.contains(it.next())) {
                return true;
            }
        }
        return false;
    }

    private void logPhoneIdToSlotIdMapping() {
        log("mPhoneIdToSlotId mapping:");
        for (int i = 0; i < this.mPhoneIdToSlotId.length; i++) {
            log("    phoneId " + i + " slotId " + this.mPhoneIdToSlotId[i]);
        }
    }

    private void onSimRefresh(AsyncResult ar, Integer index) {
        boolean changed;
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "onSimRefresh: Sim REFRESH with exception: " + ar.exception);
        } else if (!isValidPhoneIndex(index.intValue())) {
            Rlog.e(LOG_TAG, "onSimRefresh: invalid index : " + index);
        } else {
            IccRefreshResponse resp = (IccRefreshResponse) ar.result;
            log("onSimRefresh: " + resp);
            LocalLog localLog = sLocalLog;
            localLog.log("onSimRefresh: " + resp);
            if (resp == null) {
                Rlog.e(LOG_TAG, "onSimRefresh: received without input");
                return;
            }
            UiccCard uiccCard = getUiccCardForPhone(index.intValue());
            if (uiccCard == null) {
                Rlog.e(LOG_TAG, "onSimRefresh: refresh on null card : " + index);
                return;
            }
            turnOffHotspot(index.intValue());
            if (resp.refreshResult != 2 || resp.aid == null || resp.aid.equals(PhoneConfigurationManager.SSSS)) {
                Rlog.d(LOG_TAG, "Ignoring reset: " + resp);
                return;
            }
            int i = resp.refreshResult;
            if (i == 1) {
                changed = uiccCard.resetAppWithAid(resp.aid, false);
            } else if (i == 2) {
                changed = uiccCard.resetAppWithAid(resp.aid, true);
            } else {
                return;
            }
            if (changed && resp.refreshResult == 2) {
                ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).updateConfigForPhoneId(index.intValue(), "UNKNOWN");
                if (this.mContext.getResources().getBoolean(17891500)) {
                    this.mCis[index.intValue()].setRadioPower(false, null);
                }
            }
            this.mCis[index.intValue()].getIccCardStatus(obtainMessage(3, index));
        }
    }

    private void onEidReady(AsyncResult ar, Integer index) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "onEidReady: exception: " + ar.exception);
        } else if (!isValidPhoneIndex(index.intValue())) {
            Rlog.e(LOG_TAG, "onEidReady: invalid index: " + index);
        } else {
            int slotId = this.mPhoneIdToSlotId[index.intValue()];
            UiccCard card = this.mUiccSlots[slotId].getUiccCard();
            if (card == null) {
                Rlog.e(LOG_TAG, "onEidReady: UiccCard in slot " + slotId + " is null");
                return;
            }
            String eid = ((EuiccCard) card).getEid();
            addCardId(eid);
            int i = this.mDefaultEuiccCardId;
            if (i == -2 || i == -3) {
                this.mDefaultEuiccCardId = convertToPublicCardId(eid);
                log("onEidReady: eid=" + eid + " slot=" + slotId + " mDefaultEuiccCardId=" + this.mDefaultEuiccCardId);
            }
            ((EuiccCard) card).unregisterForEidReady(this);
        }
    }

    public static boolean isCdmaSupported(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.telephony.cdma");
    }

    private boolean isValidPhoneIndex(int index) {
        return index >= 0 && index < TelephonyManager.getDefault().getPhoneCount();
    }

    /* access modifiers changed from: protected */
    public boolean isValidSlotIndex(int index) {
        return index >= 0 && index < this.mUiccSlots.length;
    }

    @UnsupportedAppUsage
    private void log(String string) {
        Rlog.d(LOG_TAG, string);
    }

    public void addCardLog(String data) {
        sLocalLog.log(data);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UiccController: " + this);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mInstance=" + mInstance);
        pw.println(" mIccChangedRegistrants: size=" + this.mIccChangedRegistrants.size());
        for (int i = 0; i < this.mIccChangedRegistrants.size(); i++) {
            pw.println("  mIccChangedRegistrants[" + i + "]=" + ((Registrant) this.mIccChangedRegistrants.get(i)).getHandler());
        }
        pw.println();
        pw.flush();
        pw.println(" mIsCdmaSupported=" + isCdmaSupported(this.mContext));
        pw.println(" mUiccSlots: size=" + this.mUiccSlots.length);
        pw.println(" mCardStrings=" + this.mCardStrings);
        pw.println(" mDefaultEuiccCardId=" + this.mDefaultEuiccCardId);
        int i2 = 0;
        while (true) {
            UiccSlot[] uiccSlotArr = this.mUiccSlots;
            if (i2 < uiccSlotArr.length) {
                if (uiccSlotArr[i2] == null) {
                    pw.println("  mUiccSlots[" + i2 + "]=null");
                } else {
                    pw.println("  mUiccSlots[" + i2 + "]=" + this.mUiccSlots[i2]);
                    this.mUiccSlots[i2].dump(fd, pw, args);
                }
                i2++;
            } else {
                pw.println(" sLocalLog= ");
                sLocalLog.dump(fd, pw, args);
                return;
            }
        }
    }
}
