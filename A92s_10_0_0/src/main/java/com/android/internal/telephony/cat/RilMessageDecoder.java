package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class RilMessageDecoder extends StateMachine {
    protected static final int CMD_PARAMS_READY = 2;
    protected static final int CMD_START = 1;
    @UnsupportedAppUsage
    protected static RilMessageDecoder[] mInstance = null;
    protected static int mSimCount = 0;
    public Handler mCaller = null;
    @UnsupportedAppUsage
    protected CommandParamsFactory mCmdParamsFactory = null;
    protected RilMessage mCurrentRilMessage = null;
    protected StateCmdParamsReady mStateCmdParamsReady = new StateCmdParamsReady();
    @UnsupportedAppUsage
    protected StateStart mStateStart = new StateStart();

    @UnsupportedAppUsage
    public static synchronized RilMessageDecoder getInstance(Handler caller, IccFileHandler fh, int slotId) {
        synchronized (RilMessageDecoder.class) {
            if (mInstance == null) {
                mSimCount = TelephonyManager.getDefault().getSimCount();
                mInstance = new RilMessageDecoder[mSimCount];
                for (int i = 0; i < mSimCount; i++) {
                    mInstance[i] = null;
                }
            }
            if (slotId == -1 || slotId >= mSimCount) {
                CatLog.d("RilMessageDecoder", "invaild slot id: " + slotId);
                return null;
            }
            if (mInstance[slotId] == null) {
                mInstance[slotId] = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeRilMessageDecoder(caller, fh, slotId);
            }
            RilMessageDecoder rilMessageDecoder = mInstance[slotId];
            return rilMessageDecoder;
        }
    }

    public void sendStartDecodingMessageParams(RilMessage rilMsg) {
        Message msg = obtainMessage(1);
        msg.obj = rilMsg;
        sendMessage(msg);
    }

    public void sendMsgParamsDecoded(ResultCode resCode, CommandParams cmdParams) {
        Message msg = obtainMessage(2);
        msg.arg1 = resCode.value();
        msg.obj = cmdParams;
        sendMessage(msg);
    }

    /* access modifiers changed from: protected */
    public void sendCmdForExecution(RilMessage rilMsg) {
        Handler handler = this.mCaller;
        if (handler == null) {
            CatLog.d(this, "oem:caller is null");
        } else {
            handler.obtainMessage(10, new RilMessage(rilMsg)).sendToTarget();
        }
    }

    public RilMessageDecoder(Handler caller, IccFileHandler fh) {
        super("RilMessageDecoder");
        addState(this.mStateStart);
        addState(this.mStateCmdParamsReady);
        setInitialState(this.mStateStart);
        this.mCaller = caller;
        this.mCmdParamsFactory = CommandParamsFactory.getInstance(this, fh);
    }

    public RilMessageDecoder() {
        super("RilMessageDecoder");
    }

    private class StateStart extends State {
        private StateStart() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 1) {
                CatLog.d(this, "StateStart unexpected expecting START=1 got " + msg.what);
            } else if (RilMessageDecoder.this.decodeMessageParams((RilMessage) msg.obj)) {
                RilMessageDecoder rilMessageDecoder = RilMessageDecoder.this;
                rilMessageDecoder.transitionTo(rilMessageDecoder.mStateCmdParamsReady);
            }
            return true;
        }
    }

    private class StateCmdParamsReady extends State {
        private StateCmdParamsReady() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what == 2) {
                RilMessageDecoder.this.mCurrentRilMessage.mResCode = ResultCode.fromInt(msg.arg1);
                RilMessageDecoder.this.mCurrentRilMessage.mData = msg.obj;
                RilMessageDecoder rilMessageDecoder = RilMessageDecoder.this;
                rilMessageDecoder.sendCmdForExecution(rilMessageDecoder.mCurrentRilMessage);
                RilMessageDecoder rilMessageDecoder2 = RilMessageDecoder.this;
                rilMessageDecoder2.transitionTo(rilMessageDecoder2.mStateStart);
                return true;
            }
            CatLog.d(this, "StateCmdParamsReady expecting CMD_PARAMS_READY=2 got " + msg.what);
            RilMessageDecoder.this.deferMessage(msg);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean decodeMessageParams(RilMessage rilMsg) {
        boolean decodingStarted;
        this.mCurrentRilMessage = rilMsg;
        int i = rilMsg.mId;
        if (i != 1) {
            if (!(i == 2 || i == 3)) {
                if (i != 4) {
                    if (i != 5) {
                        return false;
                    }
                }
            }
            try {
                byte[] rawData = IccUtils.hexStringToBytes((String) rilMsg.mData);
                try {
                    if (this.mCmdParamsFactory != null) {
                        this.mCmdParamsFactory.make(BerTlv.decode(rawData));
                        decodingStarted = true;
                    } else {
                        CatLog.d(this, "mCmdParamsFactory is null");
                        decodingStarted = false;
                    }
                    return decodingStarted;
                } catch (ResultException e) {
                    CatLog.d(this, "decodeMessageParams: caught ResultException e=" + e);
                    this.mCurrentRilMessage.mResCode = e.result();
                    sendCmdForExecution(this.mCurrentRilMessage);
                    return false;
                }
            } catch (Exception e2) {
                CatLog.d(this, "decodeMessageParams dropping zombie messages");
                return false;
            }
        }
        this.mCurrentRilMessage.mResCode = ResultCode.OK;
        sendCmdForExecution(this.mCurrentRilMessage);
        return false;
    }

    public void dispose() {
        quitNow();
        this.mStateStart = null;
        this.mStateCmdParamsReady = null;
        this.mCmdParamsFactory.dispose();
        this.mCmdParamsFactory = null;
        this.mCurrentRilMessage = null;
        this.mCaller = null;
        mInstance = null;
    }
}
