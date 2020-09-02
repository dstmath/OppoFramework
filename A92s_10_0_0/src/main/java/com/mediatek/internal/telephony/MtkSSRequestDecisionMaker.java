package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsSsInfo;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsUtInterface;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.mediatek.ims.MtkImsCallForwardInfo;
import com.mediatek.ims.MtkImsUt;
import com.mediatek.internal.telephony.imsphone.MtkImsPhone;
import java.util.ArrayList;

public class MtkSSRequestDecisionMaker {
    private static final int CLEAR_DELAY_TIMEOUT = 10000;
    private static final int EVENT_SS_CLEAR_TEMP_VOLTE_USER_FLAG = 3;
    private static final int EVENT_SS_RESPONSE = 2;
    private static final int EVENT_SS_SEND = 1;
    static final String LOG_TAG = "MtkSSDecisonMaker";
    private static final int SS_REQUEST_GET_CALL_BARRING = 3;
    private static final int SS_REQUEST_GET_CALL_FORWARD = 1;
    private static final int SS_REQUEST_GET_CALL_FORWARD_TIME_SLOT = 15;
    private static final int SS_REQUEST_GET_CALL_WAITING = 5;
    private static final int SS_REQUEST_GET_CLIP = 9;
    private static final int SS_REQUEST_GET_CLIR = 7;
    private static final int SS_REQUEST_GET_COLP = 13;
    private static final int SS_REQUEST_GET_COLR = 11;
    private static final int SS_REQUEST_SET_CALL_BARRING = 4;
    private static final int SS_REQUEST_SET_CALL_FORWARD = 2;
    private static final int SS_REQUEST_SET_CALL_FORWARD_TIME_SLOT = 16;
    private static final int SS_REQUEST_SET_CALL_WAITING = 6;
    private static final int SS_REQUEST_SET_CLIP = 10;
    private static final int SS_REQUEST_SET_CLIR = 8;
    private static final int SS_REQUEST_SET_COLP = 14;
    private static final int SS_REQUEST_SET_COLR = 12;
    private CommandsInterface mCi = this.mPhone.mCi;
    private ImsManager mImsManager;
    /* access modifiers changed from: private */
    public boolean mIsTempVolteUser;
    private Phone mPhone;
    private int mPhoneId;
    private HandlerThread mSSHandlerThread;
    private SSRequestHandler mSSRequestHandler;

    public MtkSSRequestDecisionMaker(Context context, Phone phone) {
        this.mPhone = phone;
        this.mPhoneId = phone.getPhoneId();
        this.mImsManager = ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId());
    }

    public void starThread() {
        this.mSSHandlerThread = new HandlerThread("SSRequestHandler");
        this.mSSHandlerThread.start();
        this.mSSRequestHandler = new SSRequestHandler(this.mSSHandlerThread.getLooper());
    }

    public void dispose() {
        Rlog.d(LOG_TAG, "dispose.");
        this.mSSHandlerThread.getLooper().quit();
    }

    private int getPhoneId() {
        this.mPhoneId = this.mPhone.getPhoneId();
        return this.mPhoneId;
    }

    private ImsUtInterface getUtInterface() throws ImsException {
        if (this.mImsManager != null) {
            int phoneId = this.mPhone.getPhoneId() + 1;
            return this.mImsManager.getSupplementaryServiceConfiguration();
        }
        throw new ImsException("no ims manager", 0);
    }

    /* access modifiers changed from: package-private */
    public void sendGenericErrorResponse(Message onComplete) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    private void sendRadioNotAvailable(Message onComplete) {
        Rlog.d(LOG_TAG, "sendRadioNotAvailable");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, (Object) null, new CommandException(CommandException.Error.RADIO_NOT_AVAILABLE));
            onComplete.sendToTarget();
        }
    }

    private int getActionFromCFAction(int action) {
        if (action == 0) {
            return 0;
        }
        if (action == 1) {
            return 1;
        }
        if (action == 3) {
            return 3;
        }
        if (action != 4) {
            return -1;
        }
        return 4;
    }

    private int getConditionFromCFReason(int reason) {
        if (reason == 0) {
            return 0;
        }
        if (reason == 1) {
            return 1;
        }
        if (reason == 2) {
            return 2;
        }
        if (reason == 3) {
            return 3;
        }
        if (reason != 4) {
            return reason != 5 ? -1 : 5;
        }
        return 4;
    }

    private int getCBTypeFromFacility(String facility) {
        if ("AO".equals(facility)) {
            return 2;
        }
        if ("OI".equals(facility)) {
            return 3;
        }
        if ("OX".equals(facility)) {
            return 4;
        }
        if ("AI".equals(facility)) {
            return 1;
        }
        if ("IR".equals(facility)) {
            return 5;
        }
        if ("AB".equals(facility)) {
            return 7;
        }
        if ("AG".equals(facility)) {
            return 8;
        }
        if ("AC".equals(facility)) {
            return 9;
        }
        return 0;
    }

    private int[] handleCbQueryResult(ImsSsInfo[] infos) {
        return new int[]{infos[0].mStatus};
    }

    private int[] handleCwQueryResult(ImsSsInfo[] infos) {
        int[] cwInfos = new int[2];
        cwInfos[0] = 0;
        if (infos[0].mStatus == 1) {
            cwInfos[0] = 1;
            cwInfos[1] = 1;
        }
        return cwInfos;
    }

    private MtkCallForwardInfo getMtkCallForwardInfo(MtkImsCallForwardInfo info) {
        MtkCallForwardInfo cfInfo = new MtkCallForwardInfo();
        cfInfo.status = info.mStatus;
        cfInfo.reason = getCFReasonFromCondition(info.mCondition);
        cfInfo.serviceClass = info.mServiceClass;
        cfInfo.toa = info.mToA;
        cfInfo.number = info.mNumber;
        cfInfo.timeSeconds = info.mTimeSeconds;
        cfInfo.timeSlot = info.mTimeSlot;
        return cfInfo;
    }

    private MtkCallForwardInfo[] imsCFInfoExToCFInfoEx(MtkImsCallForwardInfo[] infos) {
        MtkCallForwardInfo[] cfInfos;
        if (infos == null || infos.length == 0) {
            Rlog.d(LOG_TAG, "No CFInfoEx exist .");
            cfInfos = new MtkCallForwardInfo[0];
        } else {
            cfInfos = new MtkCallForwardInfo[infos.length];
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                cfInfos[i] = getMtkCallForwardInfo(infos[i]);
            }
        }
        Rlog.d(LOG_TAG, "imsCFInfoExToCFInfoEx finish.");
        return cfInfos;
    }

    private CallForwardInfo getCallForwardInfo(ImsCallForwardInfo info) {
        CallForwardInfo cfInfo = new CallForwardInfo();
        cfInfo.status = info.mStatus;
        cfInfo.reason = getCFReasonFromCondition(info.mCondition);
        cfInfo.serviceClass = info.mServiceClass;
        cfInfo.toa = info.mToA;
        cfInfo.number = info.mNumber;
        cfInfo.timeSeconds = info.mTimeSeconds;
        return cfInfo;
    }

    private CallForwardInfo[] imsCFInfoToCFInfo(ImsCallForwardInfo[] infos) {
        CallForwardInfo[] cfInfos;
        if (infos == null || infos.length == 0) {
            Rlog.d(LOG_TAG, "No CFInfo exist .");
            cfInfos = new CallForwardInfo[0];
        } else {
            cfInfos = new CallForwardInfo[infos.length];
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                cfInfos[i] = getCallForwardInfo(infos[i]);
            }
        }
        Rlog.d(LOG_TAG, "imsCFInfoToCFInfo finish.");
        return cfInfos;
    }

    private int getCFReasonFromCondition(int condition) {
        if (condition == 0) {
            return 0;
        }
        if (condition == 1) {
            return 1;
        }
        if (condition == 2) {
            return 2;
        }
        if (condition == 3) {
            return 3;
        }
        if (condition != 4) {
            return condition != 5 ? 3 : 5;
        }
        return 4;
    }

    class SSRequestHandler extends Handler implements Runnable {
        public SSRequestHandler(Looper looper) {
            super(looper);
        }

        public void run() {
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                MtkSSRequestDecisionMaker.this.processSendRequest(msg.obj);
            } else if (i == 2) {
                MtkSSRequestDecisionMaker.this.processResponse(msg.obj);
            } else if (i != 3) {
                Rlog.d(MtkSSRequestDecisionMaker.LOG_TAG, "MtkSSRequestDecisionMaker:msg.what=" + msg.what);
            } else {
                boolean unused = MtkSSRequestDecisionMaker.this.mIsTempVolteUser = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void processSendRequest(Object obj) {
        Message resp;
        Message resp2;
        String number;
        Message resp3;
        String getNumber;
        Message resp4;
        Message resp5;
        Message resp6;
        Message resp7;
        ArrayList<Object> ssParmList = (ArrayList) obj;
        Integer request = (Integer) ssParmList.get(0);
        Message utResp = this.mSSRequestHandler.obtainMessage(2, ssParmList);
        Rlog.d(LOG_TAG, "processSendRequest, request = " + request);
        boolean z = true;
        boolean z2 = true;
        switch (request.intValue()) {
            case 1:
                int cfReason = ((Integer) ssParmList.get(1)).intValue();
                int serviceClass = ((Integer) ssParmList.get(2)).intValue();
                String str = (String) ssParmList.get(3);
                resp = (Message) ssParmList.get(4);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp);
                    return;
                } else if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp);
                    return;
                } else {
                    try {
                        ((MtkImsUt) getUtInterface()).queryCFForServiceClass(getConditionFromCFReason(cfReason), (String) null, serviceClass, utResp);
                    } catch (ImsException e) {
                        sendGenericErrorResponse(resp);
                    }
                    return;
                }
            case 2:
                int action = ((Integer) ssParmList.get(1)).intValue();
                int cfReason2 = ((Integer) ssParmList.get(2)).intValue();
                int serviceClass2 = ((Integer) ssParmList.get(3)).intValue();
                String number2 = (String) ssParmList.get(4);
                int timeSeconds = ((Integer) ssParmList.get(5)).intValue();
                Message resp8 = (Message) ssParmList.get(6);
                if (!this.mPhone.isRadioAvailable()) {
                    resp2 = resp8;
                } else if (!this.mPhone.isRadioOn()) {
                    resp2 = resp8;
                } else {
                    if ((number2 == null || number2.isEmpty()) && this.mPhone.getPhoneType() == 1) {
                        MtkGsmCdmaPhone mtkGsmCdmaPhone = this.mPhone;
                        if ((mtkGsmCdmaPhone instanceof MtkGsmCdmaPhone) && mtkGsmCdmaPhone.isSupportSaveCFNumber() && ((action == 1 || action == 3) && (getNumber = this.mPhone.getCFPreviousDialNumber(cfReason2)) != null && !getNumber.isEmpty())) {
                            number = getNumber;
                            ImsUtInterface ut = getUtInterface();
                            int actionFromCFAction = getActionFromCFAction(action);
                            resp3 = resp8;
                            ut.updateCallForward(actionFromCFAction, getConditionFromCFReason(cfReason2), number, serviceClass2, timeSeconds, utResp);
                            return;
                        }
                    }
                    number = number2;
                    try {
                        ImsUtInterface ut2 = getUtInterface();
                        int actionFromCFAction2 = getActionFromCFAction(action);
                        resp3 = resp8;
                        try {
                            ut2.updateCallForward(actionFromCFAction2, getConditionFromCFReason(cfReason2), number, serviceClass2, timeSeconds, utResp);
                            return;
                        } catch (ImsException e2) {
                        }
                    } catch (ImsException e3) {
                        resp3 = resp8;
                        sendGenericErrorResponse(resp3);
                        return;
                    }
                }
                sendRadioNotAvailable(resp2);
                return;
            case 3:
                String facility = (String) ssParmList.get(1);
                String str2 = (String) ssParmList.get(2);
                int serviceClass3 = ((Integer) ssParmList.get(3)).intValue();
                resp = (Message) ssParmList.get(4);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp);
                    return;
                } else if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp);
                    return;
                } else {
                    if (this.mPhone.isOpNotSupportOCB(facility)) {
                        if (!this.mIsTempVolteUser) {
                            facility = "AI";
                        } else if (resp != null) {
                            AsyncResult.forMessage(resp, (Object) null, new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED));
                            resp.sendToTarget();
                            return;
                        } else {
                            return;
                        }
                    }
                    try {
                        getUtInterface().queryCallBarring(getCBTypeFromFacility(facility), utResp, serviceClass3);
                    } catch (ImsException e4) {
                        sendGenericErrorResponse(resp);
                    }
                    return;
                }
            case 4:
                String facility2 = (String) ssParmList.get(1);
                boolean booleanValue = ((Boolean) ssParmList.get(2)).booleanValue();
                String str3 = (String) ssParmList.get(3);
                int serviceClass4 = ((Integer) ssParmList.get(4)).intValue();
                Message resp9 = (Message) ssParmList.get(5);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp9);
                } else if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp9);
                } else {
                    try {
                        getUtInterface().updateCallBarring(getCBTypeFromFacility(facility2), booleanValue ? 1 : 0, utResp, (String[]) null, serviceClass4);
                    } catch (ImsException e5) {
                        sendGenericErrorResponse(resp9);
                    }
                }
                return;
            case 5:
                ((Integer) ssParmList.get(1)).intValue();
                resp4 = (Message) ssParmList.get(2);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp4);
                    return;
                } else if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp4);
                    return;
                } else {
                    try {
                        getUtInterface().queryCallWaiting(utResp);
                    } catch (ImsException e6) {
                        sendGenericErrorResponse(resp4);
                    }
                    return;
                }
            case 6:
                boolean enable = ((Boolean) ssParmList.get(1)).booleanValue();
                int serviceClass5 = ((Integer) ssParmList.get(2)).intValue();
                resp5 = (Message) ssParmList.get(3);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp5);
                    return;
                } else if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp5);
                    return;
                } else {
                    try {
                        ImsUtInterface ut3 = getUtInterface();
                        if (this.mPhone.isOpNwCW()) {
                            ut3.updateCallWaiting(enable, serviceClass5, utResp);
                        } else {
                            ut3.queryCallWaiting(utResp);
                        }
                    } catch (ImsException e7) {
                        sendGenericErrorResponse(resp5);
                    }
                    return;
                }
            case 7:
                Message resp10 = (Message) ssParmList.get(1);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp10);
                    return;
                }
                try {
                    getUtInterface().queryCLIR(utResp);
                    return;
                } catch (ImsException e8) {
                    sendGenericErrorResponse(resp10);
                    return;
                }
            case 8:
                int mode = ((Integer) ssParmList.get(1)).intValue();
                resp4 = (Message) ssParmList.get(2);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp4);
                    return;
                } else if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp4);
                    return;
                } else {
                    try {
                        ImsUtInterface ut4 = getUtInterface();
                        if (!this.mPhone.isOpNotSupportCallIdentity()) {
                            ut4.updateCLIR(mode, utResp);
                        } else {
                            Rlog.d(LOG_TAG, "Silent queryCLIR");
                            ut4.queryCLIR(utResp);
                        }
                    } catch (ImsException e9) {
                        sendGenericErrorResponse(resp4);
                    }
                    return;
                }
            case 9:
                Message resp11 = (Message) ssParmList.get(1);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp11);
                    return;
                } else if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp11);
                    return;
                } else {
                    try {
                        getUtInterface().queryCLIP(utResp);
                        return;
                    } catch (ImsException e10) {
                        sendGenericErrorResponse(resp11);
                        return;
                    }
                }
            case 10:
                int mode2 = ((Integer) ssParmList.get(1)).intValue();
                resp4 = (Message) ssParmList.get(2);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp4);
                    return;
                } else if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp4);
                    return;
                } else {
                    try {
                        ImsUtInterface ut5 = getUtInterface();
                        if (!this.mPhone.isOpNotSupportCallIdentity()) {
                            if (mode2 == 0) {
                                z = false;
                            }
                            ut5.updateCLIP(z, utResp);
                        } else {
                            Rlog.d(LOG_TAG, "Silent queryCLIP");
                            ut5.queryCLIP(utResp);
                        }
                    } catch (ImsException e11) {
                        sendGenericErrorResponse(resp4);
                    }
                    return;
                }
            case 11:
                Message resp12 = (Message) ssParmList.get(1);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp12);
                    return;
                }
                try {
                    getUtInterface().queryCOLR(utResp);
                    return;
                } catch (ImsException e12) {
                    sendGenericErrorResponse(resp12);
                    return;
                }
            case 12:
                int mode3 = ((Integer) ssParmList.get(1)).intValue();
                resp4 = (Message) ssParmList.get(2);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp4);
                    return;
                }
                try {
                    ImsUtInterface ut6 = getUtInterface();
                    if (!this.mPhone.isOpNotSupportCallIdentity()) {
                        ut6.updateCOLR(mode3, utResp);
                    } else {
                        Rlog.d(LOG_TAG, "Silent queryCOLR");
                        ut6.queryCOLR(utResp);
                    }
                } catch (ImsException e13) {
                    sendGenericErrorResponse(resp4);
                }
                return;
            case 13:
                Message resp13 = (Message) ssParmList.get(1);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp13);
                    return;
                }
                try {
                    getUtInterface().queryCOLP(utResp);
                    return;
                } catch (ImsException e14) {
                    sendGenericErrorResponse(resp13);
                    return;
                }
            case 14:
                int mode4 = ((Integer) ssParmList.get(1)).intValue();
                resp4 = (Message) ssParmList.get(2);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp4);
                    return;
                }
                try {
                    ImsUtInterface ut7 = getUtInterface();
                    if (!this.mPhone.isOpNotSupportCallIdentity()) {
                        if (mode4 == 0) {
                            z2 = false;
                        }
                        ut7.updateCOLP(z2, utResp);
                    } else {
                        Rlog.d(LOG_TAG, "Silent queryCOLP");
                        ut7.queryCOLP(utResp);
                    }
                } catch (ImsException e15) {
                    sendGenericErrorResponse(resp4);
                }
                return;
            case 15:
                int cfReason3 = ((Integer) ssParmList.get(1)).intValue();
                ((Integer) ssParmList.get(2)).intValue();
                resp5 = (Message) ssParmList.get(3);
                if (!this.mPhone.isRadioAvailable() || !this.mPhone.isRadioOn()) {
                    sendRadioNotAvailable(resp5);
                    return;
                }
                try {
                    ((MtkImsUt) getUtInterface()).queryCallForwardInTimeSlot(getConditionFromCFReason(cfReason3), utResp);
                } catch (ImsException e16) {
                    sendGenericErrorResponse(resp5);
                }
                return;
            case 16:
                int action2 = ((Integer) ssParmList.get(1)).intValue();
                int cfReason4 = ((Integer) ssParmList.get(2)).intValue();
                ((Integer) ssParmList.get(3)).intValue();
                String number3 = (String) ssParmList.get(4);
                int timeSeconds2 = ((Integer) ssParmList.get(5)).intValue();
                long[] timeSlot = (long[]) ssParmList.get(6);
                Message resp14 = (Message) ssParmList.get(7);
                if (!this.mPhone.isRadioAvailable()) {
                    resp6 = resp14;
                } else if (!this.mPhone.isRadioOn()) {
                    resp6 = resp14;
                } else {
                    try {
                        MtkImsUt mtkImsUt = (MtkImsUt) getUtInterface();
                        int actionFromCFAction3 = getActionFromCFAction(action2);
                        resp7 = resp14;
                        try {
                            mtkImsUt.updateCallForwardInTimeSlot(actionFromCFAction3, getConditionFromCFReason(cfReason4), number3, timeSeconds2, timeSlot, utResp);
                            return;
                        } catch (ImsException e17) {
                        }
                    } catch (ImsException e18) {
                        resp7 = resp14;
                        sendGenericErrorResponse(resp7);
                        return;
                    }
                }
                sendRadioNotAvailable(resp6);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x080d  */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x0828  */
    /* JADX WARNING: Removed duplicated region for block: B:326:0x0844  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x09ce  */
    public void processResponse(Object obj) {
        Throwable arException;
        Throwable arException2;
        Throwable arException3;
        int serviceClass;
        Message resp;
        Message resp2 = null;
        AsyncResult ar = (AsyncResult) obj;
        Object arResult = ar.result;
        Throwable arException4 = ar.exception;
        ArrayList<Object> ssParmList = (ArrayList) ar.userObj;
        Integer request = (Integer) ssParmList.get(0);
        Rlog.d(LOG_TAG, "processResponse, request = " + request);
        switch (request.intValue()) {
            case 1:
                int cfReason = ((Integer) ssParmList.get(1)).intValue();
                int serviceClass2 = ((Integer) ssParmList.get(2)).intValue();
                String number = (String) ssParmList.get(3);
                resp2 = (Message) ssParmList.get(4);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException = ar.exception;
                    if (imsException.getCode() == 61446) {
                        this.mPhone.setCsFallbackStatus(2);
                        if (this.mPhone.isNotSupportUtToCS()) {
                            Rlog.d(LOG_TAG, "isNotSupportUtToCS.");
                            arException = new CommandException(CommandException.Error.OPERATION_NOT_ALLOWED);
                            arResult = null;
                        } else {
                            Rlog.d(LOG_TAG, "mCi.queryCallForwardStatus.");
                            this.mCi.queryCallForwardStatus(cfReason, serviceClass2, number, resp2);
                            return;
                        }
                    } else if (imsException.getCode() == 61447) {
                        if (this.mPhone.isNotSupportUtToCS()) {
                            Rlog.d(LOG_TAG, "isNotSupportUtToCS.");
                            arException = new CommandException(CommandException.Error.OPERATION_NOT_ALLOWED);
                            arResult = null;
                        } else {
                            Rlog.d(LOG_TAG, "mCi.queryCallForwardStatus.");
                            this.mCi.queryCallForwardStatus(cfReason, serviceClass2, number, resp2);
                            return;
                        }
                    }
                    if (arResult != null) {
                        Rlog.d(LOG_TAG, "SS_REQUEST_GET_CALL_FORWARD cfinfo check.");
                        if (arResult instanceof ImsCallForwardInfo[]) {
                            arResult = imsCFInfoToCFInfo((ImsCallForwardInfo[]) arResult);
                            arException4 = arException;
                            break;
                        }
                    }
                    arException4 = arException;
                    break;
                }
                arException = arException4;
                if (arResult != null) {
                }
                arException4 = arException;
            case 2:
                arException2 = arException4;
                int action = ((Integer) ssParmList.get(1)).intValue();
                int cfReason2 = ((Integer) ssParmList.get(2)).intValue();
                int serviceClass3 = ((Integer) ssParmList.get(3)).intValue();
                String number2 = (String) ssParmList.get(4);
                int timeSeconds = ((Integer) ssParmList.get(5)).intValue();
                resp2 = (Message) ssParmList.get(6);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException2 = ar.exception;
                    if (imsException2.getCode() == 61446) {
                        this.mCi.setCallForward(action, cfReason2, serviceClass3, number2, timeSeconds, resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException2.getCode() == 61447) {
                        this.mCi.setCallForward(action, cfReason2, serviceClass3, number2, timeSeconds, resp2);
                        return;
                    }
                }
                if (ar.exception == null) {
                    if (this.mPhone.getPhoneType() == 1) {
                        MtkGsmCdmaPhone mtkGsmCdmaPhone = this.mPhone;
                        if ((mtkGsmCdmaPhone instanceof MtkGsmCdmaPhone) && mtkGsmCdmaPhone.isSupportSaveCFNumber()) {
                            if (action == 1 || action == 3) {
                                if (!this.mPhone.applyCFSharePreference(cfReason2, number2)) {
                                    Rlog.d(LOG_TAG, "applySharePreference false.");
                                }
                            } else if (action == 4) {
                                this.mPhone.clearCFSharePreference(cfReason2);
                            }
                        }
                    }
                    if (this.mPhone.queryCFUAgainAfterSet() && cfReason2 == 0) {
                        if (arResult == null) {
                            Rlog.d(LOG_TAG, "arResult is null.");
                        } else if (arResult instanceof ImsCallForwardInfo[]) {
                            arResult = imsCFInfoToCFInfo((ImsCallForwardInfo[]) arResult);
                        } else if (arResult instanceof CallForwardInfo[]) {
                            CallForwardInfo[] callForwardInfoArr = (CallForwardInfo[]) arResult;
                        }
                        arException4 = arException2;
                        break;
                    }
                }
                arException4 = arException2;
                break;
            case 3:
                String facility = (String) ssParmList.get(1);
                String password = (String) ssParmList.get(2);
                int serviceClass4 = ((Integer) ssParmList.get(3)).intValue();
                resp2 = (Message) ssParmList.get(4);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException3 = ar.exception;
                    if (imsException3.getCode() == 61446 || imsException3.getCode() == 61447) {
                        Throwable checkError = ((MtkGsmCdmaPhone) this.mPhone).checkUiccApplicationForCB();
                        if (checkError != null) {
                            arException3 = checkError;
                            if (arResult != null) {
                                Rlog.d(LOG_TAG, "SS_REQUEST_GET_CALL_BARRING ssinfo check.");
                                if (arResult instanceof ImsSsInfo[]) {
                                    arResult = handleCbQueryResult((ImsSsInfo[]) ar.result);
                                }
                            }
                            if (!this.mPhone.isOpNotSupportOCB(facility)) {
                                arException4 = arException3;
                                break;
                            } else {
                                Throwable arException5 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                                arResult = null;
                                this.mIsTempVolteUser = true;
                                this.mSSRequestHandler.sendMessageDelayed(this.mSSRequestHandler.obtainMessage(3), 10000);
                                arException4 = arException5;
                                break;
                            }
                        } else {
                            this.mCi.queryFacilityLockForApp(facility, password, serviceClass4, this.mPhone.getUiccCardApplication().getAid(), resp2);
                            if (imsException3.getCode() == 61446) {
                                this.mPhone.setCsFallbackStatus(2);
                                return;
                            }
                            return;
                        }
                    } else if (imsException3.getCode() == 61448 && this.mPhone.isOpTransferXcap404()) {
                        Rlog.d(LOG_TAG, "processResponse CODE_UT_XCAP_404_NOT_FOUND");
                        arException3 = new CommandException(CommandException.Error.NO_SUCH_ELEMENT);
                        if (arResult != null) {
                        }
                        if (!this.mPhone.isOpNotSupportOCB(facility)) {
                        }
                    }
                }
                arException3 = arException4;
                if (arResult != null) {
                }
                if (!this.mPhone.isOpNotSupportOCB(facility)) {
                }
                break;
            case 4:
                arException2 = arException4;
                String facility2 = (String) ssParmList.get(1);
                boolean lockState = ((Boolean) ssParmList.get(2)).booleanValue();
                String password2 = (String) ssParmList.get(3);
                int serviceClass5 = ((Integer) ssParmList.get(4)).intValue();
                resp2 = (Message) ssParmList.get(5);
                if (this.mPhone.isOpNotSupportOCB(facility2)) {
                    arResult = null;
                    arException2 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                }
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException4 = ar.exception;
                    if (imsException4.getCode() != 61446) {
                        if (imsException4.getCode() != 61447) {
                            if (imsException4.getCode() == 61448 && this.mPhone.isOpTransferXcap404()) {
                                Rlog.d(LOG_TAG, "processResponse CODE_UT_XCAP_404_NOT_FOUND");
                                arException2 = new CommandException(CommandException.Error.NO_SUCH_ELEMENT);
                            }
                            arException4 = arException2;
                            break;
                        } else {
                            this.mCi.setFacilityLock(facility2, lockState, password2, serviceClass5, resp2);
                            return;
                        }
                    } else {
                        this.mCi.setFacilityLock(facility2, lockState, password2, serviceClass5, resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    }
                }
                arException4 = arException2;
                break;
            case 5:
                boolean queryVolteUser = false;
                MtkGsmCdmaPhone mtkGsmCdmaPhone2 = this.mPhone;
                if ((mtkGsmCdmaPhone2 instanceof MtkGsmCdmaPhone) && mtkGsmCdmaPhone2.getTbcwMode() == 0) {
                    queryVolteUser = true;
                }
                if (!queryVolteUser || this.mPhone.isOpNwCW()) {
                    Rlog.d(LOG_TAG, "processResponse: SS_REQUEST_GET_CALL_WAITING");
                    int serviceClass6 = ((Integer) ssParmList.get(1)).intValue();
                    resp2 = (Message) ssParmList.get(2);
                    if (ar.exception != null && (ar.exception instanceof ImsException)) {
                        ImsException imsException5 = ar.exception;
                        if (imsException5.getCode() == 61446) {
                            this.mCi.queryCallWaiting(serviceClass6, resp2);
                            this.mPhone.setCsFallbackStatus(2);
                            return;
                        } else if (imsException5.getCode() == 61447) {
                            this.mCi.queryCallWaiting(serviceClass6, resp2);
                            return;
                        }
                    }
                    if (arResult != null) {
                        Rlog.d(LOG_TAG, "SS_REQUEST_GET_CALL_WAITING ssinfo check.");
                        if (arResult instanceof ImsSsInfo[]) {
                            arResult = handleCwQueryResult((ImsSsInfo[]) ar.result);
                        }
                    }
                    arException4 = arException4;
                    break;
                } else {
                    MtkGsmCdmaPhone mtkGsmCdmaPhone3 = this.mPhone;
                    Integer reqCode = (Integer) ssParmList.get(0);
                    boolean enable = false;
                    if (reqCode.intValue() == 5) {
                        serviceClass = ((Integer) ssParmList.get(1)).intValue();
                        resp = (Message) ssParmList.get(2);
                    } else {
                        enable = ((Boolean) ssParmList.get(1)).booleanValue();
                        serviceClass = ((Integer) ssParmList.get(2)).intValue();
                        resp = (Message) ssParmList.get(3);
                    }
                    ImsException imsException6 = null;
                    if (ar.exception != null && (ar.exception instanceof ImsException)) {
                        imsException6 = ar.exception;
                    }
                    if (ar.exception == null) {
                        mtkGsmCdmaPhone3.setTbcwMode(1);
                        mtkGsmCdmaPhone3.setTbcwToEnabledOnIfDisabled();
                        if (reqCode.intValue() == 5) {
                            mtkGsmCdmaPhone3.getTerminalBasedCallWaiting(resp);
                            return;
                        } else {
                            mtkGsmCdmaPhone3.setTerminalBasedCallWaiting(enable, resp);
                            return;
                        }
                    } else {
                        if (imsException6 != null) {
                            if (imsException6.getCode() == 61446) {
                                mtkGsmCdmaPhone3.setTbcwMode(2);
                                mtkGsmCdmaPhone3.setSSPropertyThroughHidl(mtkGsmCdmaPhone3.getPhoneId(), "persist.vendor.radio.terminal-based.cw", "disabled_tbcw");
                                this.mPhone.setCsFallbackStatus(2);
                                if (this.mPhone.isNotSupportUtToCS()) {
                                    Rlog.d(LOG_TAG, "isNotSupportUtToCS.");
                                    Throwable arException6 = new CommandException(CommandException.Error.OPERATION_NOT_ALLOWED);
                                    if (resp != null) {
                                        AsyncResult.forMessage(resp, (Object) null, arException6);
                                        resp.sendToTarget();
                                        return;
                                    }
                                    return;
                                } else if (reqCode.intValue() == 5) {
                                    this.mCi.queryCallWaiting(serviceClass, resp);
                                    return;
                                } else {
                                    this.mCi.setCallWaiting(enable, serviceClass, resp);
                                    return;
                                }
                            }
                        }
                        if (imsException6 == null || imsException6.getCode() != 61447) {
                            mtkGsmCdmaPhone3.setTbcwToEnabledOnIfDisabled();
                            if (reqCode.intValue() == 5) {
                                mtkGsmCdmaPhone3.getTerminalBasedCallWaiting(resp);
                                return;
                            } else {
                                mtkGsmCdmaPhone3.setTerminalBasedCallWaiting(enable, resp);
                                return;
                            }
                        } else if (this.mPhone.isNotSupportUtToCS()) {
                            Rlog.d(LOG_TAG, "isNotSupportUtToCS.");
                            Throwable arException7 = new CommandException(CommandException.Error.OPERATION_NOT_ALLOWED);
                            if (resp != null) {
                                AsyncResult.forMessage(resp, (Object) null, arException7);
                                resp.sendToTarget();
                                return;
                            }
                            return;
                        } else if (reqCode.intValue() == 5) {
                            this.mCi.queryCallWaiting(serviceClass, resp);
                            return;
                        } else {
                            this.mCi.setCallWaiting(enable, serviceClass, resp);
                            return;
                        }
                    }
                }
                break;
            case 6:
                boolean enable2 = ((Boolean) ssParmList.get(1)).booleanValue();
                int serviceClass7 = ((Integer) ssParmList.get(2)).intValue();
                resp2 = (Message) ssParmList.get(3);
                if (ar.exception != null) {
                    if (ar.exception != null && (ar.exception instanceof ImsException)) {
                        ImsException imsException7 = ar.exception;
                        if (imsException7.getCode() == 61446) {
                            this.mCi.setCallWaiting(enable2, serviceClass7, resp2);
                            this.mPhone.setCsFallbackStatus(2);
                            return;
                        } else if (imsException7.getCode() == 61447) {
                            this.mCi.setCallWaiting(enable2, serviceClass7, resp2);
                            return;
                        }
                    }
                    break;
                } else {
                    MtkGsmCdmaPhone mtkGsmCdmaPhone4 = this.mPhone;
                    mtkGsmCdmaPhone4.setTbcwMode(1);
                    mtkGsmCdmaPhone4.setTbcwToEnabledOnIfDisabled();
                    mtkGsmCdmaPhone4.setTerminalBasedCallWaiting(enable2, resp2);
                    return;
                }
            case 7:
                resp2 = (Message) ssParmList.get(1);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException8 = ar.exception;
                    if (imsException8.getCode() == 61446) {
                        this.mCi.getCLIR(resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException8.getCode() == 61447) {
                        this.mCi.getCLIR(resp2);
                        return;
                    }
                }
                if (!this.mPhone.isOpNotSupportCallIdentity()) {
                    int[] clirInfo = null;
                    if (ar.exception == null) {
                        clirInfo = ((Bundle) arResult).getIntArray(MtkImsPhone.UT_BUNDLE_KEY_CLIR);
                        int[] clirSetting = this.mPhone.getSavedClirSetting();
                        if (clirSetting[0] == 0) {
                            Rlog.d(LOG_TAG, "Set clirInfo[0] to default");
                            clirInfo[0] = clirSetting[0];
                        }
                        Rlog.d(LOG_TAG, "SS_REQUEST_GET_CLIR: CLIR param n=" + clirInfo[0] + " m=" + clirInfo[1]);
                    }
                    arResult = clirInfo;
                    break;
                } else {
                    arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                    arResult = null;
                    break;
                }
            case 8:
                int mode = ((Integer) ssParmList.get(1)).intValue();
                resp2 = (Message) ssParmList.get(2);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException9 = ar.exception;
                    if (imsException9.getCode() == 61446) {
                        this.mCi.setCLIR(mode, resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException9.getCode() == 61447) {
                        this.mCi.setCLIR(mode, resp2);
                        return;
                    }
                }
                if (!this.mPhone.isOpNotSupportCallIdentity()) {
                    break;
                } else {
                    arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                    arResult = null;
                    break;
                }
            case 9:
                resp2 = (Message) ssParmList.get(1);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException10 = ar.exception;
                    if (imsException10.getCode() == 61446) {
                        this.mCi.queryCLIP(resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException10.getCode() == 61447) {
                        this.mCi.queryCLIP(resp2);
                        return;
                    }
                }
                if (!this.mPhone.isOpNotSupportCallIdentity()) {
                    int[] clipInfo = {0};
                    if (ar.exception == null) {
                        Bundle bundle = (Bundle) arResult;
                        if (bundle != null) {
                            ImsSsInfo ssInfo = bundle.getParcelable("imsSsInfo");
                            if (ssInfo != null) {
                                Rlog.d(LOG_TAG, "ImsSsInfo mStatus = " + ssInfo.mStatus);
                                clipInfo[0] = ssInfo.mStatus;
                            } else {
                                Rlog.e(LOG_TAG, "SS_REQUEST_GET_CLIP: ssInfo null!");
                            }
                        } else {
                            Rlog.e(LOG_TAG, "SS_REQUEST_GET_CLIP: bundle null!");
                        }
                    }
                    arResult = clipInfo;
                    break;
                } else {
                    arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                    arResult = null;
                    break;
                }
            case 10:
                int mode2 = ((Integer) ssParmList.get(1)).intValue();
                resp2 = (Message) ssParmList.get(2);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException11 = ar.exception;
                    if (imsException11.getCode() == 61446) {
                        this.mPhone.mMtkCi.setCLIP(mode2, resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException11.getCode() == 61447) {
                        this.mPhone.mMtkCi.setCLIP(mode2, resp2);
                        return;
                    }
                }
                if (!this.mPhone.isOpNotSupportCallIdentity()) {
                    break;
                } else {
                    arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                    arResult = null;
                    break;
                }
            case 11:
                resp2 = (Message) ssParmList.get(1);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException12 = ar.exception;
                    if (imsException12.getCode() == 61446) {
                        this.mPhone.mMtkCi.getCOLR(resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException12.getCode() == 61447) {
                        this.mPhone.mMtkCi.getCOLR(resp2);
                        return;
                    }
                }
                if (!this.mPhone.isOpNotSupportCallIdentity()) {
                    int[] colrInfo = {0};
                    if (ar.exception == null) {
                        Bundle bundle2 = (Bundle) arResult;
                        if (bundle2 != null) {
                            ImsSsInfo ssInfo2 = bundle2.getParcelable("imsSsInfo");
                            if (ssInfo2 != null) {
                                Rlog.d(LOG_TAG, "ImsSsInfo mStatus = " + ssInfo2.mStatus);
                                colrInfo[0] = ssInfo2.mStatus;
                            } else {
                                Rlog.e(LOG_TAG, "SS_REQUEST_GET_COLR: ssInfo null!");
                            }
                        } else {
                            Rlog.e(LOG_TAG, "SS_REQUEST_GET_COLR: bundle null!");
                        }
                    }
                    arResult = colrInfo;
                    break;
                } else {
                    arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                    arResult = null;
                    break;
                }
            case 12:
                int mode3 = ((Integer) ssParmList.get(1)).intValue();
                resp2 = (Message) ssParmList.get(2);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException13 = ar.exception;
                    if (imsException13.getCode() == 61446) {
                        this.mPhone.mMtkCi.setCOLR(mode3, resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException13.getCode() == 61447) {
                        this.mPhone.mMtkCi.setCOLR(mode3, resp2);
                        return;
                    }
                }
                if (!this.mPhone.isOpNotSupportCallIdentity()) {
                    break;
                } else {
                    arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                    arResult = null;
                    break;
                }
            case 13:
                resp2 = (Message) ssParmList.get(1);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException14 = ar.exception;
                    if (imsException14.getCode() == 61446) {
                        this.mPhone.mMtkCi.getCOLP(resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException14.getCode() == 61447) {
                        this.mPhone.mMtkCi.getCOLP(resp2);
                        return;
                    }
                }
                if (!this.mPhone.isOpNotSupportCallIdentity()) {
                    int[] colpInfo = {0};
                    if (ar.exception == null) {
                        Bundle bundle3 = (Bundle) arResult;
                        if (bundle3 != null) {
                            ImsSsInfo ssInfo3 = bundle3.getParcelable("imsSsInfo");
                            if (ssInfo3 != null) {
                                Rlog.d(LOG_TAG, "ImsSsInfo mStatus = " + ssInfo3.mStatus);
                                colpInfo[0] = ssInfo3.mStatus;
                            } else {
                                Rlog.e(LOG_TAG, "SS_REQUEST_GET_COLP: ssInfo null!");
                            }
                        } else {
                            Rlog.e(LOG_TAG, "SS_REQUEST_GET_COLP: bundle null!");
                        }
                    }
                    arResult = colpInfo;
                    break;
                } else {
                    arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                    arResult = null;
                    break;
                }
            case 14:
                int mode4 = ((Integer) ssParmList.get(1)).intValue();
                resp2 = (Message) ssParmList.get(2);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException15 = ar.exception;
                    if (imsException15.getCode() == 61446) {
                        this.mPhone.mMtkCi.setCOLP(mode4, resp2);
                        this.mPhone.setCsFallbackStatus(2);
                        return;
                    } else if (imsException15.getCode() == 61447) {
                        this.mPhone.mMtkCi.setCOLP(mode4, resp2);
                        return;
                    }
                }
                if (!this.mPhone.isOpNotSupportCallIdentity()) {
                    break;
                } else {
                    arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                    arResult = null;
                    break;
                }
            case 15:
                resp2 = (Message) ssParmList.get(3);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException16 = ar.exception;
                    if (imsException16.getCode() == 61446) {
                        arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                        arResult = null;
                        this.mPhone.setCsFallbackStatus(2);
                    } else if (imsException16.getCode() == 61447) {
                        if (resp2 != null) {
                            AsyncResult.forMessage(resp2, (Object) null, new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED));
                            resp2.sendToTarget();
                            return;
                        }
                        return;
                    }
                }
                if (arResult != null) {
                    Rlog.d(LOG_TAG, "SS_REQUEST_GET_CALL_FORWARD_TIME_SLOT cfinfoEx check.");
                    if (arResult instanceof MtkImsCallForwardInfo[]) {
                        arResult = imsCFInfoExToCFInfoEx((MtkImsCallForwardInfo[]) arResult);
                        break;
                    }
                }
                break;
            case 16:
                resp2 = (Message) ssParmList.get(7);
                if (ar.exception != null && (ar.exception instanceof ImsException)) {
                    ImsException imsException17 = ar.exception;
                    if (imsException17.getCode() == 61446) {
                        arException4 = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
                        arResult = null;
                        this.mPhone.setCsFallbackStatus(2);
                    } else if (imsException17.getCode() == 61447) {
                        if (resp2 != null) {
                            AsyncResult.forMessage(resp2, (Object) null, new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED));
                            resp2.sendToTarget();
                            return;
                        }
                        return;
                    }
                    break;
                }
                break;
        }
        if (arException4 != null && (arException4 instanceof ImsException)) {
            Rlog.d(LOG_TAG, "processResponse, imsException.getCode = " + ((ImsException) arException4).getCode());
            arException4 = getCommandException((ImsException) arException4);
        }
        if (resp2 != null) {
            AsyncResult.forMessage(resp2, arResult, arException4);
            resp2.sendToTarget();
        }
    }

    private CommandException getCommandException(ImsException imsException) {
        if (imsException.getCode() != 61449) {
            Rlog.d(LOG_TAG, "getCommandException GENERIC_FAILURE");
            return new CommandException(CommandException.Error.GENERIC_FAILURE);
        } else if (this.mPhone.isEnableXcapHttpResponse409()) {
            Rlog.d(LOG_TAG, "getCommandException UT_XCAP_409_CONFLICT");
            return new CommandException(CommandException.Error.OEM_ERROR_25);
        } else {
            Rlog.d(LOG_TAG, "getCommandException GENERIC_FAILURE");
            return new CommandException(CommandException.Error.GENERIC_FAILURE);
        }
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(1));
        ssParmList.add(new Integer(cfReason));
        ssParmList.add(new Integer(serviceClass));
        ssParmList.add(number);
        ssParmList.add(response);
        send(ssParmList);
    }

    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(2));
        ssParmList.add(new Integer(action));
        ssParmList.add(new Integer(cfReason));
        ssParmList.add(new Integer(serviceClass));
        ssParmList.add(number);
        ssParmList.add(new Integer(timeSeconds));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void queryCallForwardInTimeSlotStatus(int cfReason, int serviceClass, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(15));
        ssParmList.add(new Integer(cfReason));
        ssParmList.add(new Integer(serviceClass));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void setCallForwardInTimeSlot(int action, int cfReason, int serviceClass, String number, int timeSeconds, long[] timeSlot, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(16));
        ssParmList.add(new Integer(action));
        ssParmList.add(new Integer(cfReason));
        ssParmList.add(new Integer(serviceClass));
        ssParmList.add(number);
        ssParmList.add(new Integer(timeSeconds));
        ssParmList.add(timeSlot);
        ssParmList.add(response);
        send(ssParmList);
    }

    public void queryFacilityLock(String facility, String password, int serviceClass, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(3));
        ssParmList.add(facility);
        ssParmList.add(password);
        ssParmList.add(new Integer(serviceClass));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(4));
        ssParmList.add(facility);
        ssParmList.add(new Boolean(lockState));
        ssParmList.add(password);
        ssParmList.add(new Integer(serviceClass));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void queryCallWaiting(int serviceClass, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(5));
        ssParmList.add(new Integer(serviceClass));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(6));
        ssParmList.add(new Boolean(enable));
        ssParmList.add(new Integer(serviceClass));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void getCLIR(Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(7));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void setCLIR(int clirMode, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(8));
        ssParmList.add(new Integer(clirMode));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void getCLIP(Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(9));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void setCLIP(int clipMode, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(10));
        ssParmList.add(new Integer(clipMode));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void getCOLR(Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(11));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void setCOLR(int colrMode, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(12));
        ssParmList.add(new Integer(colrMode));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void getCOLP(Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(13));
        ssParmList.add(response);
        send(ssParmList);
    }

    public void setCOLP(int colpMode, Message response) {
        ArrayList<Object> ssParmList = new ArrayList<>();
        ssParmList.add(new Integer(14));
        ssParmList.add(new Integer(colpMode));
        ssParmList.add(response);
        send(ssParmList);
    }

    /* access modifiers changed from: package-private */
    public void send(ArrayList<Object> ssParmList) {
        this.mSSRequestHandler.obtainMessage(1, ssParmList).sendToTarget();
    }
}
