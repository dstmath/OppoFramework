package com.mediatek.opcommon.telephony;

import android.content.Context;
import android.hardware.radio.V1_0.IRadio;
import android.hardware.radio.deprecated.V1_0.IOemHook;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILRequest;
import com.mediatek.internal.telephony.IMtkRilOp;
import java.util.concurrent.atomic.AtomicLong;
import vendor.mediatek.hardware.radio_op.V2_0.IRadioIndicationOp;
import vendor.mediatek.hardware.radio_op.V2_0.IRadioOp;
import vendor.mediatek.hardware.radio_op.V2_0.IRadioResponseOp;
import vendor.mediatek.hardware.radio_op.V2_0.RsuRequestInfo;

public class MtkRilOp extends RIL implements IMtkRilOp {
    static final String[] HIDL_SERVICE_NAME = {"slot1", "slot2", "slot3"};
    static final boolean MTK_RILJOP_LOGD = true;
    static final String TAG = "MtkRilOp";
    public Registrant mEnterSCBMRegistrant;
    public RegistrantList mExitSCBMRegistrants = new RegistrantList();
    protected RegistrantList mMelockRegistrants = new RegistrantList();
    protected RegistrantList mModulationRegistrants = new RegistrantList();
    IRadioIndicationOp mRadioIndicationOp = null;
    protected final RadioOpProxyDeathRecipient mRadioOpProxyDeathRecipient;
    protected final AtomicLong mRadioProxyCookie = new AtomicLong(0);
    volatile IRadioOp mRadioProxyOp = null;
    IRadioResponseOp mRadioResponseOp = null;
    protected final RilHandlerOp mRilHandlerOp;
    public RegistrantList mRsuEventRegistrants = new RegistrantList();

    protected class RilHandlerOp extends Handler {
        protected RilHandlerOp() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 6) {
                MtkRilOp mtkRilOp = MtkRilOp.this;
                mtkRilOp.log("handleMessage: EVENT_RADIO_PROXY_DEAD cookie = " + msg.obj + " mRadioProxyCookie = " + MtkRilOp.this.mRadioProxyCookie.get());
                if (((Long) msg.obj).longValue() == MtkRilOp.this.mRadioProxyCookie.get()) {
                    MtkRilOp.this.resetProxyAndRequestList();
                    MtkRilOp.this.getRadioOpProxy(null);
                }
            }
        }
    }

    final class RadioOpProxyDeathRecipient implements IHwBinder.DeathRecipient {
        RadioOpProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            MtkRilOp.this.log("serviceDied");
            MtkRilOp.this.mRilHandlerOp.sendMessageDelayed(MtkRilOp.this.mRilHandlerOp.obtainMessage(6, Long.valueOf(cookie)), 1000);
        }
    }

    public MtkRilOp(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        log("MtkRilOp constructor ");
        this.mRadioResponseOp = new MtkRadioResponseOp(this);
        this.mRadioIndicationOp = new MtkRadioIndicationOp(this);
        this.mRilHandlerOp = new RilHandlerOp();
        this.mRadioOpProxyDeathRecipient = new RadioOpProxyDeathRecipient();
        getRadioOpProxy(null);
    }

    public IRadio getRadioProxy(Message result) {
        log("MtkRilOp getRadioProxy");
        return null;
    }

    public IOemHook getOemHookProxy(Message result) {
        log("MtkRilOp getOemHookProxy");
        return null;
    }

    /* access modifiers changed from: protected */
    public IRadioOp getRadioOpProxy(Message result) {
        if (this.mRadioProxyOp != null) {
            return this.mRadioProxyOp;
        }
        try {
            this.mRadioProxyOp = IRadioOp.getService(HIDL_SERVICE_NAME[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()]);
            if (this.mRadioProxyOp != null) {
                this.mRadioProxyOp.linkToDeath(this.mRadioOpProxyDeathRecipient, this.mRadioProxyCookie.incrementAndGet());
                this.mRadioProxyOp.setResponseFunctions(this.mRadioResponseOp, this.mRadioIndicationOp);
            } else {
                log("getRadioOpProxy: mRadioProxy == null");
            }
        } catch (RemoteException | RuntimeException e) {
            this.mRadioProxyOp = null;
            log("RadioProxy getService/setResponseFunctions: " + e);
        }
        if (this.mRadioProxyOp == null) {
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            RilHandlerOp rilHandlerOp = this.mRilHandlerOp;
            rilHandlerOp.sendMessageDelayed(rilHandlerOp.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.get())), 1000);
        }
        return this.mRadioProxyOp;
    }

    /* access modifiers changed from: protected */
    public void handleRadioOpProxyExceptionForRR(RILRequest rr, String caller, Exception e) {
        log(caller + ": " + e);
        resetProxyAndRequestList();
        RilHandlerOp rilHandlerOp = this.mRilHandlerOp;
        rilHandlerOp.sendMessageDelayed(rilHandlerOp.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 1000);
    }

    /* access modifiers changed from: protected */
    public void resetProxyAndRequestList() {
        MtkRilOp.super.resetProxyAndRequestList();
        this.mRadioProxyOp = null;
    }

    public void log(String text) {
        Rlog.d(TAG, text);
    }

    public void registerForMelockChanged(Handler h, int what, Object obj) {
        this.mMelockRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMelockChanged(Handler h) {
        this.mMelockRegistrants.remove(h);
    }

    public void setIncomingVirtualLine(String fromMsisdn, String toMsisdn, Message response) {
        IRadioOp radioProxy = getRadioOpProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_SET_INCOMING_VIRTUAL_LINE, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " fromMsisdn = " + fromMsisdn + " toMsisdn = " + toMsisdn);
            try {
                radioProxy.setIncomingVirtualLine(rr.mSerial, fromMsisdn, toMsisdn);
            } catch (RemoteException | RuntimeException e) {
                handleRadioOpProxyExceptionForRR(rr, "setIncomingVirtualLineResponse", e);
            }
        }
    }

    public void setRxTestConfig(int AntType, Message result) {
        log("setRxTestConfig");
        IRadioOp radioProxy = getRadioOpProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2101, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.setRxTestConfig(rr.mSerial, AntType);
            } catch (RemoteException | RuntimeException e) {
                handleRadioOpProxyExceptionForRR(rr, "setRxTestConfig", e);
            }
        }
    }

    public void getRxTestResult(Message result) {
        log("getRxTestResult");
        IRadioOp radioProxy = getRadioOpProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(2102, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getRxTestResult(rr.mSerial, 0);
            } catch (RemoteException | RuntimeException e) {
                handleRadioOpProxyExceptionForRR(rr, "getRxTestResult", e);
            }
        }
    }

    public void registerForModulation(Handler h, int what, Object obj) {
        this.mModulationRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForModulation(Handler h) {
        this.mModulationRegistrants.remove(h);
    }

    public void setDisable2G(boolean mode, Message result) {
        log("setDisable2G " + mode);
        IRadioOp radioProxy = getRadioOpProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_SET_DISABLE_2G, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> RIL_REQUEST_SET_DISABLE_2G");
            try {
                radioProxy.setDisable2G(rr.mSerial, mode);
            } catch (RemoteException | RuntimeException e) {
                handleRadioOpProxyExceptionForRR(rr, "setDisable2G", e);
            }
        }
    }

    public void getDisable2G(Message result) {
        log("getDisable2G");
        IRadioOp radioProxy = getRadioOpProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_GET_DISABLE_2G, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> RIL_REQUEST_GET_DISABLE_2G");
            try {
                radioProxy.getDisable2G(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioOpProxyExceptionForRR(rr, "getDisable2G", e);
            }
        }
    }

    public void exitSCBM(Message result) {
        log("exitSCBM");
        IRadioOp radioProxy = getRadioOpProxy(result);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_EXIT_SCBM, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> RIL_REQUEST_EXIT_SCBM");
            try {
                IRadioOp radioProxy20 = IRadioOp.castFrom(radioProxy);
                if (radioProxy20 != null) {
                    radioProxy20.exitSCBM(rr.mSerial);
                }
            } catch (RemoteException | RuntimeException e) {
                handleRadioOpProxyExceptionForRR(rr, "exitSCBM", e);
            }
        }
    }

    public void sendRsuRequest(RsuRequestInfo rri, Message result) {
        IRadioOp radioProxy = getRadioOpProxy(result);
        if (radioProxy != null && rri != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_SEND_RSU_REQUEST, result, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + "> RIL_REQUEST_SEND_RSU_REQUEST opId = " + rri.opId + " requestId = " + rri.requestId);
            try {
                radioProxy.sendRsuRequest(rr.mSerial, rri);
            } catch (RemoteException | RuntimeException e) {
                handleRadioOpProxyExceptionForRR(rr, "sendRsuRequest", e);
            }
        }
    }

    public void registerForExitSCBM(Handler h, int what, Object obj) {
        this.mExitSCBMRegistrants.add(new Registrant(h, what, obj));
    }

    public void registerForRsuEvent(Handler h, int what, Object obj) {
        this.mRsuEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForExitSCBM(Handler h) {
        this.mExitSCBMRegistrants.remove(h);
    }

    public void unregisterForRsuEvent(Handler h) {
        this.mRsuEventRegistrants.remove(h);
    }

    public void setSCBM(Handler h, int what, Object obj) {
        this.mEnterSCBMRegistrant = new Registrant(h, what, obj);
    }
}
