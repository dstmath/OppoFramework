package com.mediatek.internal.telephony.cat;

import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cat.BerTlv;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.cat.ResultException;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

/* access modifiers changed from: package-private */
public class BipRilMessageDecoder extends StateMachine {
    private static final int CMD_PARAMS_READY = 2;
    private static final int CMD_START = 1;
    private static BipRilMessageDecoder[] mInstance = null;
    private static int mSimCount = 0;
    private BipCommandParamsFactory mBipCmdParamsFactory = null;
    private Handler mCaller = null;
    /* access modifiers changed from: private */
    public MtkRilMessage mCurrentRilMessage = null;
    private int mSlotId;
    /* access modifiers changed from: private */
    public StateCmdParamsReady mStateCmdParamsReady = new StateCmdParamsReady();
    /* access modifiers changed from: private */
    public StateStart mStateStart = new StateStart();

    public static synchronized BipRilMessageDecoder getInstance(Handler caller, IccFileHandler fh, int slotId) {
        synchronized (BipRilMessageDecoder.class) {
            if (mInstance == null) {
                mSimCount = TelephonyManager.getDefault().getSimCount();
                mInstance = new BipRilMessageDecoder[mSimCount];
                for (int i = 0; i < mSimCount; i++) {
                    mInstance[i] = null;
                }
            }
            if (slotId == -1 || slotId >= mSimCount) {
                MtkCatLog.d("BipRilMessageDecoder", "invaild slot id: " + slotId);
                return null;
            }
            if (mInstance[slotId] == null) {
                mInstance[slotId] = new BipRilMessageDecoder(caller, fh, slotId);
            }
            BipRilMessageDecoder bipRilMessageDecoder = mInstance[slotId];
            return bipRilMessageDecoder;
        }
    }

    public void sendStartDecodingMessageParams(MtkRilMessage rilMsg) {
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

    /* access modifiers changed from: private */
    public void sendCmdForExecution(MtkRilMessage rilMsg) {
        this.mCaller.obtainMessage(20, new MtkRilMessage(rilMsg)).sendToTarget();
    }

    public int getSlotId() {
        return this.mSlotId;
    }

    private BipRilMessageDecoder(Handler caller, IccFileHandler fh, int slotId) {
        super("BipRilMessageDecoder");
        addState(this.mStateStart);
        addState(this.mStateCmdParamsReady);
        setInitialState(this.mStateStart);
        this.mCaller = caller;
        this.mSlotId = slotId;
        MtkCatLog.d(this, "mCaller is " + this.mCaller.getClass().getName());
        this.mBipCmdParamsFactory = BipCommandParamsFactory.getInstance(this, fh);
    }

    private BipRilMessageDecoder() {
        super("BipRilMessageDecoder");
    }

    /* access modifiers changed from: private */
    public class StateStart extends State {
        private StateStart() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 1) {
                MtkCatLog.d(this, "StateStart unexpected expecting START=1 got " + msg.what);
            } else if (BipRilMessageDecoder.this.decodeMessageParams((MtkRilMessage) msg.obj)) {
                BipRilMessageDecoder bipRilMessageDecoder = BipRilMessageDecoder.this;
                bipRilMessageDecoder.transitionTo(bipRilMessageDecoder.mStateCmdParamsReady);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class StateCmdParamsReady extends State {
        private StateCmdParamsReady() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what == 2) {
                BipRilMessageDecoder.this.mCurrentRilMessage.mResCode = ResultCode.fromInt(msg.arg1);
                BipRilMessageDecoder.this.mCurrentRilMessage.mData = msg.obj;
                BipRilMessageDecoder bipRilMessageDecoder = BipRilMessageDecoder.this;
                bipRilMessageDecoder.sendCmdForExecution(bipRilMessageDecoder.mCurrentRilMessage);
                BipRilMessageDecoder bipRilMessageDecoder2 = BipRilMessageDecoder.this;
                bipRilMessageDecoder2.transitionTo(bipRilMessageDecoder2.mStateStart);
                return true;
            }
            MtkCatLog.d(this, "StateCmdParamsReady expecting CMD_PARAMS_READY=2 got " + msg.what);
            BipRilMessageDecoder.this.deferMessage(msg);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean decodeMessageParams(MtkRilMessage rilMsg) {
        this.mCurrentRilMessage = rilMsg;
        int i = rilMsg.mId;
        if (i != 18 && i != 19) {
            return false;
        }
        MtkCatLog.d(this, "decodeMessageParams raw: " + ((String) rilMsg.mData));
        try {
            try {
                this.mBipCmdParamsFactory.make(BerTlv.decode(IccUtils.hexStringToBytes((String) rilMsg.mData)));
                return true;
            } catch (ResultException e) {
                MtkCatLog.d(this, "decodeMessageParams: caught ResultException e=" + e);
                MtkRilMessage mtkRilMessage = this.mCurrentRilMessage;
                mtkRilMessage.mId = 1;
                mtkRilMessage.mResCode = e.result();
                sendCmdForExecution(this.mCurrentRilMessage);
                return false;
            }
        } catch (Exception e2) {
            MtkCatLog.d(this, "decodeMessageParams dropping zombie messages");
            return false;
        }
    }

    public void dispose() {
        this.mStateStart = null;
        this.mStateCmdParamsReady = null;
        this.mBipCmdParamsFactory.dispose();
        this.mBipCmdParamsFactory = null;
        this.mCurrentRilMessage = null;
        this.mCaller = null;
        BipRilMessageDecoder[] bipRilMessageDecoderArr = mInstance;
        if (bipRilMessageDecoderArr != null) {
            int i = this.mSlotId;
            if (bipRilMessageDecoderArr[i] != null) {
                bipRilMessageDecoderArr[i].quit();
                mInstance[this.mSlotId] = null;
            }
            int i2 = 0;
            while (i2 < mSimCount && mInstance[i2] == null) {
                i2++;
            }
            if (i2 == mSimCount) {
                mInstance = null;
            }
        }
    }
}
