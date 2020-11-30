package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccSlotStatus;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class UiccSlot extends Handler {
    private static final boolean DBG = true;
    private static final int EVENT_CARD_ADDED = 14;
    private static final int EVENT_CARD_REMOVED = 13;
    public static final String EXTRA_ICC_CARD_ADDED = "com.android.internal.telephony.uicc.ICC_CARD_ADDED";
    public static final int INVALID_PHONE_ID = -1;
    private static final String TAG = "UiccSlot";
    private boolean mActive;
    private AnswerToReset mAtr;
    private IccCardStatus.CardState mCardState;
    private CommandsInterface mCi;
    private Context mContext;
    private String mIccId;
    private boolean mIsEuicc;
    private boolean mIsRemovable;
    private int mLastRadioState = 2;
    private final Object mLock = new Object();
    private int mPhoneId = -1;
    private boolean mStateIsUnknown = true;
    private UiccCard mUiccCard;

    public UiccSlot(Context c, boolean isActive) {
        log("Creating");
        this.mContext = c;
        this.mActive = isActive;
        this.mCardState = null;
    }

    public void update(CommandsInterface ci, IccCardStatus ics, int phoneId, int slotIndex) {
        log("cardStatus update: " + ics.toString());
        synchronized (this.mLock) {
            IccCardStatus.CardState oldState = this.mCardState;
            this.mCardState = ics.mCardState;
            this.mIccId = ics.iccid;
            this.mPhoneId = phoneId;
            parseAtr(ics.atr);
            this.mCi = ci;
            this.mIsRemovable = isSlotRemovable(slotIndex);
            int radioState = this.mCi.getRadioState();
            log("update: radioState=" + radioState + " mLastRadioState=" + this.mLastRadioState);
            if (absentStateUpdateNeeded(oldState)) {
                updateCardStateAbsent();
            } else if ((oldState == null || oldState == IccCardStatus.CardState.CARDSTATE_ABSENT || this.mUiccCard == null) && this.mCardState != IccCardStatus.CardState.CARDSTATE_ABSENT) {
                if (radioState == 1 && this.mLastRadioState == 1) {
                    log("update: notify card added");
                    sendMessage(obtainMessage(14, null));
                }
                if (this.mUiccCard != null) {
                    loge("update: mUiccCard != null when card was present; disposing it now");
                    this.mUiccCard.dispose();
                }
                if (!this.mIsEuicc) {
                    this.mUiccCard = new UiccCard(this.mContext, this.mCi, ics, this.mPhoneId, this.mLock);
                } else {
                    if (TextUtils.isEmpty(ics.eid)) {
                        loge("update: eid is missing. ics.eid=" + ics.eid);
                    }
                    this.mUiccCard = new EuiccCard(this.mContext, this.mCi, ics, phoneId, this.mLock);
                }
            } else if (this.mUiccCard != null) {
                this.mUiccCard.update(this.mContext, this.mCi, ics);
            }
            this.mLastRadioState = radioState;
        }
    }

    public void update(CommandsInterface ci, IccSlotStatus iss, int slotIndex) {
        log("slotStatus update: " + iss.toString());
        synchronized (this.mLock) {
            IccCardStatus.CardState oldState = this.mCardState;
            this.mCi = ci;
            parseAtr(iss.atr);
            this.mCardState = iss.cardState;
            this.mIccId = iss.iccid;
            this.mIsRemovable = isSlotRemovable(slotIndex);
            if (iss.slotState != IccSlotStatus.SlotState.SLOTSTATE_INACTIVE) {
                this.mActive = true;
                this.mPhoneId = iss.logicalSlotIndex;
                if (absentStateUpdateNeeded(oldState)) {
                    updateCardStateAbsent();
                }
            } else if (this.mActive) {
                this.mActive = false;
                this.mLastRadioState = 2;
                UiccController.updateInternalIccState(this.mContext, IccCardConstants.State.ABSENT, null, this.mPhoneId, true);
                this.mPhoneId = -1;
                nullifyUiccCard(true);
            }
        }
    }

    private boolean absentStateUpdateNeeded(IccCardStatus.CardState oldState) {
        return !(oldState == IccCardStatus.CardState.CARDSTATE_ABSENT && this.mUiccCard == null) && this.mCardState == IccCardStatus.CardState.CARDSTATE_ABSENT;
    }

    private void updateCardStateAbsent() {
        CommandsInterface commandsInterface = this.mCi;
        int radioState = commandsInterface == null ? 2 : commandsInterface.getRadioState();
        if (radioState == 1 && this.mLastRadioState == 1) {
            log("update: notify card removed");
            sendMessage(obtainMessage(13, null));
        }
        UiccController.updateInternalIccState(this.mContext, IccCardConstants.State.ABSENT, null, this.mPhoneId);
        nullifyUiccCard(false);
        this.mLastRadioState = radioState;
    }

    private void nullifyUiccCard(boolean stateUnknown) {
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard != null) {
            uiccCard.dispose();
        }
        this.mStateIsUnknown = stateUnknown;
        this.mUiccCard = null;
    }

    public boolean isStateUnknown() {
        IccCardStatus.CardState cardState = this.mCardState;
        if (cardState == null || cardState == IccCardStatus.CardState.CARDSTATE_ABSENT) {
            return this.mStateIsUnknown;
        }
        return this.mUiccCard == null;
    }

    private boolean isSlotRemovable(int slotIndex) {
        int[] euiccSlots = this.mContext.getResources().getIntArray(17236111);
        if (euiccSlots == null) {
            return true;
        }
        for (int euiccSlot : euiccSlots) {
            if (euiccSlot == slotIndex) {
                return false;
            }
        }
        return true;
    }

    private void checkIsEuiccSupported() {
        AnswerToReset answerToReset = this.mAtr;
        if (answerToReset == null || !answerToReset.isEuiccSupported()) {
            this.mIsEuicc = false;
        } else {
            this.mIsEuicc = true;
        }
    }

    private void parseAtr(String atr) {
        this.mAtr = AnswerToReset.parseAtr(atr);
        checkIsEuiccSupported();
    }

    public boolean isEuicc() {
        return this.mIsEuicc;
    }

    public boolean isActive() {
        return this.mActive;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public boolean isRemovable() {
        return this.mIsRemovable;
    }

    public String getIccId() {
        String str = this.mIccId;
        if (str != null) {
            return str;
        }
        UiccCard uiccCard = this.mUiccCard;
        if (uiccCard != null) {
            return uiccCard.getIccId();
        }
        return null;
    }

    public boolean isExtendedApduSupported() {
        AnswerToReset answerToReset = this.mAtr;
        return answerToReset != null && answerToReset.isExtendedApduSupported();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        log("UiccSlot finalized");
    }

    private void onIccSwap(boolean isAdded) {
        if (this.mContext.getResources().getBoolean(17891467)) {
            log("onIccSwap: isHotSwapSupported is true, don't prompt for rebooting");
            return;
        }
        log("onIccSwap: isHotSwapSupported is false, prompt for rebooting");
        promptForRestart(isAdded);
    }

    private void promptForRestart(boolean isAdded) {
        String title;
        String message;
        synchronized (this.mLock) {
            String dialogComponent = this.mContext.getResources().getString(17039740);
            if (dialogComponent != null) {
                try {
                    this.mContext.startActivity(new Intent().setComponent(ComponentName.unflattenFromString(dialogComponent)).addFlags(268435456).putExtra("com.android.internal.telephony.uicc.ICC_CARD_ADDED", isAdded));
                    return;
                } catch (ActivityNotFoundException e) {
                    loge("Unable to find ICC hotswap prompt for restart activity: " + e);
                }
            }
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                /* class com.android.internal.telephony.uicc.UiccSlot.AnonymousClass1 */

                public void onClick(DialogInterface dialog, int which) {
                    synchronized (UiccSlot.this.mLock) {
                        if (which == -1) {
                            UiccSlot.this.log("Reboot due to SIM swap");
                            ((PowerManager) UiccSlot.this.mContext.getSystemService("power")).reboot("SIM is added.");
                        }
                    }
                }
            };
            Resources r = Resources.getSystem();
            if (isAdded) {
                title = r.getString(17041029);
            } else {
                title = r.getString(17041032);
            }
            if (isAdded) {
                message = r.getString(17041028);
            } else {
                message = r.getString(17041031);
            }
            AlertDialog dialog = new AlertDialog.Builder(this.mContext).setTitle(title).setMessage(message).setPositiveButton(r.getString(17041033), listener).create();
            dialog.getWindow().setType(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE);
            dialog.show();
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 13) {
            onIccSwap(false);
        } else if (i != 14) {
            loge("Unknown Event " + msg.what);
        } else {
            onIccSwap(true);
        }
    }

    public IccCardStatus.CardState getCardState() {
        synchronized (this.mLock) {
            if (this.mCardState == null) {
                return IccCardStatus.CardState.CARDSTATE_ABSENT;
            }
            return this.mCardState;
        }
    }

    public UiccCard getUiccCard() {
        UiccCard uiccCard;
        synchronized (this.mLock) {
            uiccCard = this.mUiccCard;
        }
        return uiccCard;
    }

    public void onRadioStateUnavailable() {
        nullifyUiccCard(true);
        if (this.mPhoneId != -1) {
            UiccController.updateInternalIccState(this.mContext, IccCardConstants.State.UNKNOWN, null, this.mPhoneId);
        }
        this.mCardState = null;
        this.mLastRadioState = 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        Rlog.d(TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UiccSlot:");
        pw.println(" mCi=" + this.mCi);
        pw.println(" mActive=" + this.mActive);
        pw.println(" mIsEuicc=" + this.mIsEuicc);
        pw.println(" mLastRadioState=" + this.mLastRadioState);
        pw.println(" mIccId=" + this.mIccId);
        pw.println(" mCardState=" + this.mCardState);
        if (this.mUiccCard != null) {
            pw.println(" mUiccCard=" + this.mUiccCard);
            this.mUiccCard.dump(fd, pw, args);
        } else {
            pw.println(" mUiccCard=null");
        }
        pw.println();
        pw.flush();
    }
}
