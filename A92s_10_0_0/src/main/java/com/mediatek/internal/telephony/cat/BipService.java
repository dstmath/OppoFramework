package com.mediatek.internal.telephony.cat;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.cat.ResponseData;
import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkIccCardConstants;
import com.mediatek.internal.telephony.MtkRIL;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BipService {
    static final int ADDITIONAL_INFO_FOR_BIP_CHANNEL_CLOSED = 2;
    static final int ADDITIONAL_INFO_FOR_BIP_CHANNEL_ID_NOT_AVAILABLE = 3;
    static final int ADDITIONAL_INFO_FOR_BIP_NO_CHANNEL_AVAILABLE = 1;
    static final int ADDITIONAL_INFO_FOR_BIP_NO_SPECIFIC_CAUSE = 0;
    static final int ADDITIONAL_INFO_FOR_BIP_REQUESTED_BUFFER_SIZE_NOT_AVAILABLE = 4;
    static final int ADDITIONAL_INFO_FOR_BIP_REQUESTED_INTERFACE_TRANSPORT_LEVEL_NOT_AVAILABLE = 6;
    static final int ADDITIONAL_INFO_FOR_BIP_SECURITY_ERROR = 5;
    private static final String BIP_NAME = "__M-BIP__";
    private static final int CHANNEL_KEEP_TIMEOUT = 30000;
    private static final int CONN_DELAY_TIMEOUT = 5000;
    private static final int CONN_MGR_TIMEOUT = 50000;
    private static final boolean DBG = true;
    private static final int DELAYED_CLOSE_CHANNEL_TIMEOUT = 5000;
    private static final int DEV_ID_DISPLAY = 2;
    private static final int DEV_ID_KEYPAD = 1;
    private static final int DEV_ID_NETWORK = 131;
    private static final int DEV_ID_TERMINAL = 130;
    private static final int DEV_ID_UICC = 129;
    protected static final int MSG_ID_BIP_CHANNEL_DELAYED_CLOSE = 22;
    protected static final int MSG_ID_BIP_CHANNEL_KEEP_TIMEOUT = 21;
    protected static final int MSG_ID_BIP_CONN_DELAY_TIMEOUT = 11;
    protected static final int MSG_ID_BIP_CONN_MGR_TIMEOUT = 10;
    protected static final int MSG_ID_BIP_DISCONNECT_TIMEOUT = 12;
    protected static final int MSG_ID_BIP_PROACTIVE_COMMAND = 18;
    protected static final int MSG_ID_BIP_WAIT_DATA_READY_TIMEOUT = 23;
    protected static final int MSG_ID_CLOSE_CHANNEL_DONE = 16;
    protected static final int MSG_ID_EVENT_NOTIFY = 19;
    protected static final int MSG_ID_GET_CHANNEL_STATUS_DONE = 17;
    protected static final int MSG_ID_OPEN_CHANNEL_DONE = 13;
    protected static final int MSG_ID_RECEIVE_DATA_DONE = 15;
    protected static final int MSG_ID_RIL_MSG_DECODED = 20;
    protected static final int MSG_ID_SEND_DATA_DONE = 14;
    private static final String PROPERTY_IA_APN = "vendor.ril.radio.ia-apn";
    private static final String PROPERTY_PERSIST_IA_APN = "persist.vendor.radio.ia-apn";
    private static final int WAIT_DATA_IN_SERVICE_TIMEOUT = 5000;
    private static BipService[] mInstance = null;
    private static int mSimCount = 0;
    final int NETWORK_TYPE = 0;
    /* access modifiers changed from: private */
    public boolean isConnMgrIntentTimeout = false;
    String mApn = null;
    private String mApnType = "bip";
    private String mApnTypeDb = "";
    boolean mAutoReconnected = false;
    BearerDesc mBearerDesc = null;
    /* access modifiers changed from: private */
    public BipChannelManager mBipChannelManager = null;
    /* access modifiers changed from: private */
    public BipRilMessageDecoder mBipMsgDecoder = null;
    /* access modifiers changed from: private */
    public Handler mBipSrvHandler = new Handler() {
        /* class com.mediatek.internal.telephony.cat.BipService.AnonymousClass1 */

        /* JADX INFO: Multiple debug info for r0v56 java.lang.Object: [D('arrList' java.util.ArrayList), D('cmd' com.mediatek.internal.telephony.cat.BipCmdMessage)] */
        /* JADX INFO: Multiple debug info for r0v82 int: [D('channelId' int), D('cmdParams' com.android.internal.telephony.cat.CommandParams)] */
        public void handleMessage(Message msg) {
            int protocolType;
            AsyncResult ar;
            MtkCatLog.d(this, "handleMessage[" + msg.what + "]");
            switch (msg.what) {
                case 10:
                    MtkCatLog.d("[BIP]", "handleMessage MSG_ID_BIP_CONN_MGR_TIMEOUT");
                    boolean unused = BipService.this.isConnMgrIntentTimeout = true;
                    BipService.this.disconnect();
                    return;
                case 11:
                    MtkCatLog.d("[BIP]", "handleMessage MSG_ID_BIP_CONN_DELAY_TIMEOUT");
                    BipService.this.acquireNetwork();
                    return;
                case 12:
                    MtkCatLog.d("[BIP]", "handleMessage MSG_ID_BIP_DISCONNECT_TIMEOUT");
                    synchronized (BipService.this.mCloseLock) {
                        MtkCatLog.d("[BIP]", "mIsCloseInProgress: " + BipService.this.mIsCloseInProgress + " mPreviousKeepChannelId:" + BipService.this.mPreviousKeepChannelId);
                        if (true == BipService.this.mIsCloseInProgress) {
                            boolean unused2 = BipService.this.mIsCloseInProgress = false;
                            BipService.this.mBipSrvHandler.sendMessage(BipService.this.mBipSrvHandler.obtainMessage(16, 0, 0, BipService.this.mCurrentCmd));
                        } else if (BipService.this.mPreviousKeepChannelId != 0) {
                            int unused3 = BipService.this.mPreviousKeepChannelId = 0;
                            BipService.this.openChannel((BipCmdMessage) msg.obj, BipService.this.mBipSrvHandler.obtainMessage(13));
                        }
                    }
                    return;
                case 13:
                    int ret = msg.arg1;
                    BipCmdMessage cmd = (BipCmdMessage) msg.obj;
                    if (BipService.this.mCurrentCmd == null) {
                        MtkCatLog.d("[BIP]", "SS-handleMessage: skip open channel response because current cmd is null");
                        return;
                    } else if (BipService.this.mCurrentCmd != null && BipService.this.mCurrentCmd.mCmdDet.typeOfCommand != AppInterface.CommandType.OPEN_CHANNEL.value()) {
                        MtkCatLog.d("[BIP]", "SS-handleMessage: skip open channel response because current cmd type is not OPEN_CHANNEL");
                        return;
                    } else if (8 != (cmd.getCmdQualifier() & 8)) {
                        if (cmd.mTransportProtocol != null) {
                            protocolType = cmd.mTransportProtocol.protocolType;
                        } else {
                            protocolType = 0;
                        }
                        if (ret == 0) {
                            ResponseData resp = new OpenChannelResponseDataEx(cmd.mChannelStatusData, cmd.mBearerDesc, cmd.mBufferSize, protocolType);
                            MtkCatLog.d("[BIP]", "SS-handleMessage: open channel successfully");
                            BipService bipService = BipService.this;
                            bipService.sendTerminalResponse(bipService.mCurrentCmd.mCmdDet, ResultCode.OK, false, 0, resp);
                            return;
                        } else if (ret == 3) {
                            ResponseData resp2 = new OpenChannelResponseDataEx(cmd.mChannelStatusData, cmd.mBearerDesc, cmd.mBufferSize, protocolType);
                            MtkCatLog.d("[BIP]", "SS-handleMessage: Modified parameters");
                            BipService bipService2 = BipService.this;
                            bipService2.sendTerminalResponse(bipService2.mCurrentCmd.mCmdDet, ResultCode.PRFRMD_WITH_MODIFICATION, false, 0, resp2);
                            return;
                        } else if (ret == 6) {
                            ResponseData resp3 = new OpenChannelResponseDataEx((ChannelStatus) null, cmd.mBearerDesc, cmd.mBufferSize, protocolType);
                            MtkCatLog.d("[BIP]", "SS-handleMessage: ME is busy on call");
                            BipService bipService3 = BipService.this;
                            bipService3.sendTerminalResponse(bipService3.mCurrentCmd.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 2, resp3);
                            return;
                        } else {
                            BipService.this.releaseRequest();
                            BipService.this.resetLocked();
                            ResponseData resp4 = new OpenChannelResponseDataEx((ChannelStatus) null, cmd.mBearerDesc, cmd.mBufferSize, protocolType);
                            MtkCatLog.d("[BIP]", "SS-handleMessage: open channel failed");
                            if (!BipService.this.isSprintSupport() || !BipService.this.mIsConnectTimeout) {
                                BipService.this.sendTerminalResponse(cmd.mCmdDet, ResultCode.BIP_ERROR, true, 0, resp4);
                                return;
                            }
                            BipService bipService4 = BipService.this;
                            bipService4.handleCommand(bipService4.mCachedParams, true);
                            return;
                        }
                    } else if (ret == 0) {
                        cmd.mChannelStatusData.mChannelStatus = 128;
                        cmd.mChannelStatusData.isActivated = true;
                        cmd.mChannelStatusData.mChannelId = BipService.this.mBipChannelManager.getFreeChannelId();
                        ResponseData resp5 = new OpenChannelResponseDataEx(cmd.mChannelStatusData, cmd.mBearerDesc, cmd.mBufferSize, cmd.mDnsServerAddress);
                        BipService bipService5 = BipService.this;
                        bipService5.sendTerminalResponse(bipService5.mCurrentCmd.mCmdDet, ResultCode.OK, false, 0, resp5);
                        return;
                    } else {
                        ResponseData resp6 = new OpenChannelResponseDataEx((ChannelStatus) null, cmd.mBearerDesc, cmd.mBufferSize, cmd.mDnsServerAddress);
                        BipService bipService6 = BipService.this;
                        bipService6.sendTerminalResponse(bipService6.mCurrentCmd.mCmdDet, ResultCode.BIP_ERROR, true, 0, resp6);
                        return;
                    }
                case 14:
                    int ret2 = msg.arg1;
                    int size = msg.arg2;
                    BipCmdMessage cmd2 = (BipCmdMessage) msg.obj;
                    ResponseData resp7 = new SendDataResponseData(size);
                    if (ret2 == 0) {
                        BipService.this.sendTerminalResponse(cmd2.mCmdDet, ResultCode.OK, false, 0, resp7);
                    } else if (ret2 == 7) {
                        BipService.this.sendTerminalResponse(cmd2.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                    } else {
                        BipService.this.sendTerminalResponse(cmd2.mCmdDet, ResultCode.BIP_ERROR, true, 0, resp7);
                    }
                    return;
                case 15:
                    int ret3 = msg.arg1;
                    BipCmdMessage cmd3 = (BipCmdMessage) msg.obj;
                    ResponseData resp8 = new ReceiveDataResponseData(cmd3.mChannelData, cmd3.mRemainingDataLength);
                    if (ret3 == 0) {
                        BipService.this.sendTerminalResponse(cmd3.mCmdDet, ResultCode.OK, false, 0, resp8);
                    } else if (ret3 == 9) {
                        BipService.this.sendTerminalResponse(cmd3.mCmdDet, ResultCode.PRFRMD_WITH_MISSING_INFO, false, 0, resp8);
                    } else {
                        BipService.this.sendTerminalResponse(cmd3.mCmdDet, ResultCode.BIP_ERROR, true, 0, null);
                    }
                    return;
                case 16:
                    BipCmdMessage cmd4 = (BipCmdMessage) msg.obj;
                    if (msg.arg1 == 0) {
                        BipService.this.sendTerminalResponse(cmd4.mCmdDet, ResultCode.OK, false, 0, null);
                        return;
                    } else if (msg.arg1 == 7) {
                        BipService.this.sendTerminalResponse(cmd4.mCmdDet, ResultCode.BIP_ERROR, true, 3, null);
                        return;
                    } else if (msg.arg1 == 8) {
                        BipService.this.sendTerminalResponse(cmd4.mCmdDet, ResultCode.BIP_ERROR, true, 2, null);
                        return;
                    } else {
                        return;
                    }
                case 17:
                    int ret4 = msg.arg1;
                    BipCmdMessage cmd5 = (BipCmdMessage) msg.obj;
                    ArrayList arrList = (ArrayList) cmd5.mChannelStatusList;
                    MtkCatLog.d("[BIP]", "SS-handleCmdResponse: MSG_ID_GET_CHANNEL_STATUS_DONE:" + arrList.size());
                    BipService.this.sendTerminalResponse(cmd5.mCmdDet, ResultCode.OK, false, 0, new GetMultipleChannelStatusResponseData(arrList));
                    return;
                case 18:
                case 19:
                    MtkCatLog.d(this, "ril message arrived, slotid: " + BipService.this.mSlotId);
                    String data = null;
                    if (!(msg.obj == null || (ar = (AsyncResult) msg.obj) == null || ar.result == null)) {
                        try {
                            data = (String) ar.result;
                        } catch (ClassCastException e) {
                            return;
                        }
                    }
                    BipService.this.mBipMsgDecoder.sendStartDecodingMessageParams(new MtkRilMessage(msg.what, data));
                    return;
                case 20:
                    BipService.this.handleRilMsg((MtkRilMessage) msg.obj);
                    return;
                case 21:
                    MtkCatLog.d("[BIP]", "handleMessage MSG_ID_BIP_CHANNEL_KEEP_TIMEOUT");
                    MtkCatLog.d("[BIP]", "mPreviousKeepChannelId:" + BipService.this.mPreviousKeepChannelId);
                    if (BipService.this.mPreviousKeepChannelId != 0) {
                        BipCmdMessage cmd6 = (BipCmdMessage) msg.obj;
                        Channel channel = BipService.this.mBipChannelManager.getChannel(BipService.this.mPreviousKeepChannelId);
                        BipService.this.releaseRequest();
                        BipService.this.resetLocked();
                        if (channel != null) {
                            channel.closeChannel();
                        }
                        BipService.this.mBipChannelManager.removeChannel(BipService.this.mPreviousKeepChannelId);
                        BipService.this.deleteApnParams();
                        BipService.this.setPdnReuse("1");
                        Channel unused4 = BipService.this.mChannel = null;
                        int unused5 = BipService.this.mChannelStatus = 2;
                        int unused6 = BipService.this.mPreviousKeepChannelId = 0;
                        BipService bipService7 = BipService.this;
                        bipService7.mApn = null;
                        bipService7.mLogin = null;
                        bipService7.mPassword = null;
                        return;
                    }
                    return;
                case BipService.MSG_ID_BIP_CHANNEL_DELAYED_CLOSE /*{ENCODED_INT: 22}*/:
                    int channelId = msg.arg1;
                    MtkCatLog.d("[BIP]", "MSG_ID_BIP_CHANNEL_DELAYED_CLOSE: channel id: " + channelId);
                    if (channelId <= 0 || channelId > 7 || !BipService.this.mBipChannelManager.isChannelIdOccupied(channelId)) {
                        MtkCatLog.d("[BIP]", "channel already closed");
                        return;
                    }
                    Channel channel2 = BipService.this.mBipChannelManager.getChannel(channelId);
                    MtkCatLog.d("[BIP]", "channel protocolType:" + channel2.mProtocolType);
                    if (1 == channel2.mProtocolType || 2 == channel2.mProtocolType) {
                        channel2.closeChannel();
                        BipService.this.mBipChannelManager.removeChannel(channelId);
                        return;
                    }
                    MtkCatLog.d("[BIP]", "MSG_ID_BIP_CHANNEL_DELAYED_CLOSE: channel type: " + channel2.mProtocolType);
                    return;
                case BipService.MSG_ID_BIP_WAIT_DATA_READY_TIMEOUT /*{ENCODED_INT: 23}*/:
                    MtkCatLog.d("[BIP]", "MSG_ID_BIP_WAIT_DATA_READY_TIMEOUT");
                    BipService.this.handleCommand((CommandParams) msg.obj, true);
                    return;
                default:
                    return;
            }
        }
    };
    int mBufferSize = 0;
    /* access modifiers changed from: private */
    public CommandParams mCachedParams = null;
    /* access modifiers changed from: private */
    public Channel mChannel = null;
    /* access modifiers changed from: private */
    public int mChannelId = 0;
    /* access modifiers changed from: private */
    public int mChannelStatus = 0;
    private ChannelStatus mChannelStatusDataObject = null;
    /* access modifiers changed from: private */
    public final Object mCloseLock = new Object();
    private CommandsInterface mCmdIf = null;
    private BipCmdMessage mCmdMessage = null;
    private ConnectivityManager mConnMgr = null;
    private Context mContext = null;
    /* access modifiers changed from: private */
    public BipCmdMessage mCurrentCmd = null;
    protected volatile MtkCatCmdMessage mCurrentSetupEventCmd = null;
    private BipCmdMessage mCurrntCmd = null;
    boolean mDNSaddrequest = false;
    OtherAddress mDataDestinationAddress = null;
    private List<InetAddress> mDnsAddres = new ArrayList();
    private Handler mHandler = null;
    private boolean mIsApnInserting = false;
    /* access modifiers changed from: private */
    public boolean mIsCloseInProgress = false;
    protected boolean mIsConnectTimeout = false;
    private volatile boolean mIsListenChannelStatus = false;
    private volatile boolean mIsListenDataAvailable = false;
    /* access modifiers changed from: private */
    public boolean mIsNetworkAvailableReceived = false;
    protected boolean mIsOpenChannelOverWifi = false;
    /* access modifiers changed from: private */
    public boolean mIsOpenInProgress = false;
    private boolean mIsUpdateApnParams = false;
    int mLinkMode = 0;
    OtherAddress mLocalAddress = null;
    String mLogin = null;
    private String mLoginDb = "";
    private MtkRIL mMtkCmdIf;
    private int mNeedRetryNum = 4;
    /* access modifiers changed from: private */
    public Network mNetwork;
    /* access modifiers changed from: private */
    public ConnectivityManager.NetworkCallback mNetworkCallback;
    private NetworkRequest mNetworkRequest = null;
    private String mNumeric = "";
    String mPassword = null;
    private String mPasswordDb = "";
    /* access modifiers changed from: private */
    public int mPreviousKeepChannelId = 0;
    private int mPreviousProtocolType = 0;
    private final Object mReleaseNetworkLock = new Object();
    /* access modifiers changed from: private */
    public int mSlotId = -1;
    TransportProtocol mTransportProtocol = null;
    private Uri mUri = null;

    public BipService(Context context, Handler handler, int sim_id) {
        MtkCatLog.d("[BIP]", "Construct BipService");
        if (context == null) {
            MtkCatLog.e("[BIP]", "Fail to construct BipService");
            return;
        }
        this.mContext = context;
        this.mSlotId = sim_id;
        MtkCatLog.d("[BIP]", "Construct instance sim id: " + sim_id);
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mHandler = handler;
        this.mBipChannelManager = new BipChannelManager();
        newThreadToDelelteApn();
    }

    public BipService(Context context, Handler handler, int sim_id, CommandsInterface cmdIf, IccFileHandler fh) {
        MtkCatLog.d("[BIP]", "Construct BipService " + sim_id);
        if (context == null) {
            MtkCatLog.e("[BIP]", "Fail to construct BipService");
            return;
        }
        this.mContext = context;
        this.mSlotId = sim_id;
        this.mCmdIf = cmdIf;
        this.mMtkCmdIf = (MtkRIL) cmdIf;
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mHandler = handler;
        this.mBipChannelManager = new BipChannelManager();
        this.mBipMsgDecoder = BipRilMessageDecoder.getInstance(this.mBipSrvHandler, fh, this.mSlotId);
        BipRilMessageDecoder bipRilMessageDecoder = this.mBipMsgDecoder;
        if (bipRilMessageDecoder == null) {
            MtkCatLog.d(this, "Null BipRilMessageDecoder instance");
            return;
        }
        bipRilMessageDecoder.start();
        this.mMtkCmdIf.setOnBipProactiveCmd(this.mBipSrvHandler, 18, null);
        newThreadToDelelteApn();
    }

    private ConnectivityManager getConnectivityManager() {
        if (this.mConnMgr == null) {
            this.mConnMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mConnMgr;
    }

    public static BipService getInstance(Context context, Handler handler, int simId) {
        MtkCatLog.d("[BIP]", "getInstance sim : " + simId);
        if (mInstance == null) {
            mSimCount = TelephonyManager.getDefault().getSimCount();
            mInstance = new BipService[mSimCount];
            for (int i = 0; i < mSimCount; i++) {
                mInstance[i] = null;
            }
        }
        if (simId < 0 || simId > mSimCount) {
            MtkCatLog.d("[BIP]", "getInstance invalid sim : " + simId);
            return null;
        }
        BipService[] bipServiceArr = mInstance;
        if (bipServiceArr[simId] == null) {
            bipServiceArr[simId] = new BipService(context, handler, simId);
        }
        return mInstance[simId];
    }

    public static BipService getInstance(Context context, Handler handler, int simId, CommandsInterface cmdIf, IccFileHandler fh) {
        MtkCatLog.d("[BIP]", "getInstance sim : " + simId);
        if (mInstance == null) {
            mSimCount = TelephonyManager.getDefault().getSimCount();
            mInstance = new BipService[mSimCount];
            for (int i = 0; i < mSimCount; i++) {
                mInstance[i] = null;
            }
        }
        if (simId < 0 || simId > mSimCount) {
            MtkCatLog.d("[BIP]", "getInstance invalid sim : " + simId);
            return null;
        }
        BipService[] bipServiceArr = mInstance;
        if (bipServiceArr[simId] == null) {
            bipServiceArr[simId] = new BipService(context, handler, simId, cmdIf, fh);
        }
        return mInstance[simId];
    }

    public void dispose() {
        MtkCatLog.d("[BIP]", "Dispose slotId : " + this.mSlotId);
        BipService[] bipServiceArr = mInstance;
        if (bipServiceArr != null) {
            int i = this.mSlotId;
            if (bipServiceArr[i] != null) {
                bipServiceArr[i] = null;
            }
            int i2 = 0;
            while (i2 < mSimCount && mInstance[i2] == null) {
                i2++;
            }
            if (i2 == mSimCount) {
                mInstance = null;
            }
        }
        Handler handler = this.mBipSrvHandler;
        if (handler != null) {
            this.mMtkCmdIf.unSetOnBipProactiveCmd(handler);
        }
        BipRilMessageDecoder bipRilMessageDecoder = this.mBipMsgDecoder;
        if (bipRilMessageDecoder != null) {
            bipRilMessageDecoder.dispose();
        }
    }

    /* access modifiers changed from: private */
    public void handleRilMsg(MtkRilMessage rilMsg) {
        if (rilMsg != null && rilMsg.mId == 18) {
            try {
                CommandParams cmdParams = (CommandParams) rilMsg.mData;
                if (cmdParams == null) {
                    return;
                }
                if (rilMsg.mResCode == ResultCode.OK) {
                    handleCommand(cmdParams, true);
                } else {
                    sendTerminalResponse(cmdParams.mCmdDet, rilMsg.mResCode, false, 0, null);
                }
            } catch (ClassCastException e) {
                MtkCatLog.d(this, "Fail to parse proactive command");
                BipCmdMessage bipCmdMessage = this.mCurrntCmd;
                if (bipCmdMessage != null) {
                    sendTerminalResponse(bipCmdMessage.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                }
            }
        }
    }

    private void checkPSEvent(MtkCatCmdMessage cmdMsg) {
        this.mIsListenDataAvailable = false;
        this.mIsListenChannelStatus = false;
        int[] iArr = cmdMsg.getSetEventList().eventList;
        for (int eventVal : iArr) {
            MtkCatLog.v(this, "Event: " + eventVal);
            if (eventVal == 9) {
                this.mIsListenDataAvailable = true;
            } else if (eventVal == 10) {
                this.mIsListenChannelStatus = true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setSetupEventList(MtkCatCmdMessage cmdMsg) {
        this.mCurrentSetupEventCmd = cmdMsg;
        checkPSEvent(cmdMsg);
    }

    /* access modifiers changed from: package-private */
    public boolean hasPsEvent(int eventId) {
        if (eventId == 9) {
            return this.mIsListenDataAvailable;
        }
        if (eventId != 10) {
            return false;
        }
        return this.mIsListenChannelStatus;
    }

    /* access modifiers changed from: private */
    public void handleCommand(CommandParams cmdParams, boolean isProactiveCmd) {
        int protocolType;
        MtkCatLog.d(this, cmdParams.getCommandType().name());
        BipCmdMessage cmdMsg = new BipCmdMessage(cmdParams);
        int i = AnonymousClass5.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[cmdParams.getCommandType().ordinal()];
        if (i == 1) {
            MtkCatLog.d(this, "SS-handleProactiveCommand: process OPEN_CHANNEL,slot id = " + this.mSlotId);
            PhoneConstants.State state = PhoneConstants.State.IDLE;
            CallManager callmgr = CallManager.getInstance();
            int phoneId = this.mSlotId;
            Phone myPhone = PhoneFactory.getPhone(phoneId);
            if (cmdMsg.mTransportProtocol != null) {
                protocolType = cmdMsg.mTransportProtocol.protocolType;
            } else {
                protocolType = 0;
            }
            if (myPhone == null) {
                MtkCatLog.d(this, "myPhone is still null");
                sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 2, new OpenChannelResponseDataEx((ChannelStatus) null, cmdMsg.mBearerDesc, cmdMsg.mBufferSize, protocolType));
                return;
            }
            String bipDisabled = SystemProperties.get("persist.vendor.ril.bip.disabled", "0");
            if (bipDisabled == null || !bipDisabled.equals("1")) {
                int networkType = myPhone.getServiceState().getVoiceNetworkType();
                MtkCatLog.d(this, "networkType = " + networkType);
                if ((networkType <= 2 || networkType == 16) && callmgr != null) {
                    PhoneConstants.State call_state = callmgr.getState();
                    MtkCatLog.d(this, "call_state" + call_state);
                    if (call_state != PhoneConstants.State.IDLE) {
                        MtkCatLog.d(this, "SS-handleProactiveCommand: ME is busy on call");
                        cmdMsg.mChannelStatusData = new ChannelStatus(getFreeChannelId(), 0, 0);
                        cmdMsg.mChannelStatusData.mChannelStatus = 0;
                        this.mCurrentCmd = cmdMsg;
                        this.mBipSrvHandler.obtainMessage(13, 6, 0, cmdMsg).sendToTarget();
                        return;
                    }
                } else {
                    MtkCatLog.d(this, "SS-handleProactiveCommand: type:" + myPhone.getServiceState().getVoiceNetworkType() + ",or null callmgr");
                }
                if (!isSprintSupport() || !isWifiConnected() || this.mIsConnectTimeout) {
                    this.mIsConnectTimeout = false;
                    if (isCurrentConnectionInService(phoneId)) {
                        this.mNeedRetryNum = 4;
                        openChannel(cmdMsg, this.mBipSrvHandler.obtainMessage(13));
                    } else if (!isSprintSupport() || this.mNeedRetryNum == 0) {
                        this.mNeedRetryNum = 4;
                        sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 4, new OpenChannelResponseDataEx((ChannelStatus) null, cmdMsg.mBearerDesc, cmdMsg.mBufferSize, protocolType));
                        return;
                    } else {
                        MtkCatLog.d(this, "handleCommand: wait for data in service");
                        Message tMsg = this.mBipSrvHandler.obtainMessage(MSG_ID_BIP_WAIT_DATA_READY_TIMEOUT);
                        tMsg.obj = cmdParams;
                        this.mBipSrvHandler.sendMessageDelayed(tMsg, 5000);
                        this.mNeedRetryNum--;
                        return;
                    }
                } else {
                    this.mCachedParams = cmdParams;
                    openChannelOverWifi(cmdMsg, this.mBipSrvHandler.obtainMessage(13));
                }
            } else {
                MtkCatLog.d(this, "BIP disabled");
                sendTerminalResponse(cmdMsg.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, new OpenChannelResponseDataEx((ChannelStatus) null, cmdMsg.mBearerDesc, cmdMsg.mBufferSize, protocolType));
                return;
            }
        } else if (i == 2) {
            MtkCatLog.d(this, "SS-handleProactiveCommand: process CLOSE_CHANNEL");
            closeChannel(cmdMsg, this.mBipSrvHandler.obtainMessage(16));
        } else if (i == 3) {
            MtkCatLog.d(this, "SS-handleProactiveCommand: process RECEIVE_DATA");
            receiveData(cmdMsg, this.mBipSrvHandler.obtainMessage(15));
        } else if (i == 4) {
            MtkCatLog.d(this, "SS-handleProactiveCommand: process SEND_DATA");
            sendData(cmdMsg, this.mBipSrvHandler.obtainMessage(14));
        } else if (i != 5) {
            MtkCatLog.d(this, "Unsupported command");
            return;
        } else {
            MtkCatLog.d(this, "SS-handleProactiveCommand: process GET_CHANNEL_STATUS");
            this.mCmdMessage = cmdMsg;
            getChannelStatus(cmdMsg, this.mBipSrvHandler.obtainMessage(17));
        }
        this.mCurrntCmd = cmdMsg;
    }

    /* renamed from: com.mediatek.internal.telephony.cat.BipService$5  reason: invalid class name */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType = new int[AppInterface.CommandType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.OPEN_CHANNEL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.CLOSE_CHANNEL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.RECEIVE_DATA.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_DATA.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.GET_CHANNEL_STATUS.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendTerminalResponse(CommandDetails cmdDet, ResultCode resultCode, boolean includeAdditionalInfo, int additionalInfo, ResponseData resp) {
        if (cmdDet == null) {
            MtkCatLog.e(this, "SS-sendTR: cmdDet is null");
            return;
        }
        MtkCatLog.d(this, "SS-sendTR: command type is " + cmdDet.typeOfCommand);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int tag = ComprehensionTlvTag.COMMAND_DETAILS.value();
        if (cmdDet.compRequired) {
            tag |= 128;
        }
        buf.write(tag);
        buf.write(3);
        buf.write(cmdDet.commandNumber);
        buf.write(cmdDet.typeOfCommand);
        buf.write(cmdDet.commandQualifier);
        buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        int length = 2;
        buf.write(2);
        buf.write(DEV_ID_TERMINAL);
        buf.write(DEV_ID_UICC);
        int tag2 = ComprehensionTlvTag.RESULT.value();
        if (cmdDet.compRequired) {
            tag2 |= 128;
        }
        buf.write(tag2);
        if (!includeAdditionalInfo) {
            length = 1;
        }
        buf.write(length);
        buf.write(resultCode.value());
        if (includeAdditionalInfo) {
            buf.write(additionalInfo);
        }
        if (resp != null) {
            MtkCatLog.d(this, "SS-sendTR: write response data into TR");
            resp.format(buf);
        } else {
            MtkCatLog.d(this, "SS-sendTR: null resp.");
        }
        String hexString = IccUtils.bytesToHexString(buf.toByteArray());
        MtkCatLog.d(this, "TERMINAL RESPONSE: " + hexString);
        this.mCmdIf.sendTerminalResponse(hexString, (Message) null);
    }

    /* access modifiers changed from: private */
    public void connect() {
        MtkCatLog.d("[BIP]", "establishConnect");
        this.mCurrentCmd.mChannelStatusData.isActivated = true;
        MtkCatLog.d("[BIP]", "requestNetwork: establish data channel");
        int ret = establishLink();
        if (ret != 10) {
            if (ret == 0 || ret == 3) {
                MtkCatLog.d("[BIP]", "1 channel is activated");
                updateCurrentChannelStatus(128);
            } else {
                MtkCatLog.d("[BIP]", "2 channel is un-activated");
                updateCurrentChannelStatus(0);
            }
            this.mIsOpenInProgress = false;
            this.mIsNetworkAvailableReceived = false;
            this.mBipSrvHandler.sendMessage(this.mBipSrvHandler.obtainMessage(13, ret, 0, this.mCurrentCmd));
        }
    }

    private void sendDelayedCloseChannel(int channelId) {
        Message bipTimerMsg = this.mBipSrvHandler.obtainMessage(MSG_ID_BIP_CHANNEL_DELAYED_CLOSE);
        bipTimerMsg.arg1 = channelId;
        this.mBipSrvHandler.sendMessageDelayed(bipTimerMsg, 5000);
    }

    /* access modifiers changed from: private */
    public void disconnect() {
        MtkCatLog.d("[BIP]", "disconnect: opening ? " + this.mIsOpenInProgress);
        if (!this.mIsOpenChannelOverWifi) {
            deleteOrRestoreApnParams();
            setPdnReuse("1");
        } else {
            this.mIsOpenChannelOverWifi = false;
        }
        if (true != this.mIsOpenInProgress || this.mChannelStatus == 4) {
            ArrayList<Byte> alByte = new ArrayList<>();
            MtkCatLog.d("[BIP]", "this is a drop link");
            this.mChannelStatus = 2;
            MtkCatResponseMessage resMsg = new MtkCatResponseMessage(MtkCatCmdMessage.getCmdMsg(), 10);
            for (int i = 1; i <= 7; i++) {
                if (true == this.mBipChannelManager.isChannelIdOccupied(i)) {
                    try {
                        Channel channel = this.mBipChannelManager.getChannel(i);
                        MtkCatLog.d("[BIP]", "channel protocolType:" + channel.mProtocolType);
                        if (1 == channel.mProtocolType || 2 == channel.mProtocolType) {
                            releaseRequest();
                            resetLocked();
                            if (isVzWSupport()) {
                                this.mBipChannelManager.updateChannelStatus(channel.mChannelId, 0);
                                this.mBipChannelManager.updateChannelStatusInfo(channel.mChannelId, 5);
                                sendDelayedCloseChannel(i);
                            } else {
                                channel.closeChannel();
                                this.mBipChannelManager.removeChannel(i);
                            }
                            alByte.add((byte) -72);
                            alByte.add((byte) 2);
                            alByte.add(Byte.valueOf((byte) (channel.mChannelId | 0)));
                            alByte.add((byte) 5);
                        }
                    } catch (NullPointerException ne) {
                        MtkCatLog.e("[BIP]", "NPE, channel null.");
                        ne.printStackTrace();
                    }
                }
            }
            if (alByte.size() > 0) {
                byte[] additionalInfo = new byte[alByte.size()];
                for (int i2 = 0; i2 < additionalInfo.length; i2++) {
                    additionalInfo[i2] = alByte.get(i2).byteValue();
                }
                resMsg.setSourceId(DEV_ID_TERMINAL);
                resMsg.setDestinationId(DEV_ID_UICC);
                resMsg.setAdditionalInfo(additionalInfo);
                resMsg.setOneShot(false);
                resMsg.setEventDownload(10, additionalInfo);
                MtkCatLog.d("[BIP]", "onEventDownload: for channel status");
                ((MtkCatService) this.mHandler).onEventDownload(resMsg);
                return;
            }
            MtkCatLog.d("[BIP]", "onEventDownload: No client channels are opened.");
            return;
        }
        Channel channel2 = this.mBipChannelManager.getChannel(this.mChannelId);
        if (channel2 != null) {
            channel2.closeChannel();
            this.mBipChannelManager.removeChannel(this.mChannelId);
        } else {
            TransportProtocol transportProtocol = this.mTransportProtocol;
            if (transportProtocol != null) {
                this.mBipChannelManager.releaseChannelId(this.mChannelId, transportProtocol.protocolType);
            }
        }
        releaseRequest();
        resetLocked();
        this.mChannelStatus = 2;
        MtkCatLog.d("[BIP]", "disconnect(): mCurrentCmd = " + this.mCurrentCmd);
        if (this.mCurrentCmd.mChannelStatusData != null) {
            this.mCurrentCmd.mChannelStatusData.mChannelStatus = 0;
            this.mCurrentCmd.mChannelStatusData.isActivated = false;
        }
        this.mIsOpenInProgress = false;
        this.mBipSrvHandler.sendMessage(this.mBipSrvHandler.obtainMessage(13, 2, 0, this.mCurrentCmd));
    }

    public void acquireNetwork() {
        String str;
        this.mIsOpenInProgress = true;
        if (this.mNetwork == null || ((str = this.mApn) != null && str.equals("web99.test-nfc1.com"))) {
            MtkCatLog.d("[BIP]", "requestNetwork: slotId " + this.mSlotId);
            if (!this.mIsOpenChannelOverWifi) {
                newRequest();
            } else {
                newRequestOverWifi();
            }
        } else {
            MtkCatLog.d("[BIP]", "acquireNetwork: already available");
            if (this.mBipChannelManager.getChannel(this.mChannelId) == null) {
                connect();
            }
        }
    }

    public void openChannel(BipCmdMessage cmdMsg, Message response) {
        int i;
        boolean z;
        MtkCatLog.d("[BIP]", "BM-openChannel: enter");
        if (!checkDataCapability(cmdMsg)) {
            cmdMsg.mChannelStatusData = new ChannelStatus(0, 0, 0);
            response.arg1 = 5;
            response.obj = cmdMsg;
            this.mCurrentCmd = cmdMsg;
            this.mBipSrvHandler.sendMessage(response);
            return;
        }
        this.isConnMgrIntentTimeout = false;
        this.mBipSrvHandler.removeMessages(21);
        this.mBipSrvHandler.removeMessages(MSG_ID_BIP_CHANNEL_DELAYED_CLOSE);
        MtkCatLog.d("[BIP]", "BM-openChannel: getCmdQualifier:" + cmdMsg.getCmdQualifier());
        this.mDNSaddrequest = 8 == (cmdMsg.getCmdQualifier() & 8);
        MtkCatLog.d("[BIP]", "BM-openChannel: mDNSaddrequest:" + this.mDNSaddrequest);
        MtkCatLog.d("[BIP]", "BM-openChannel: cmdMsg.mApn:" + cmdMsg.mApn);
        MtkCatLog.v("[BIP]", "BM-openChannel: cmdMsg.mLogin:" + cmdMsg.mLogin);
        MtkCatLog.v("[BIP]", "BM-openChannel: cmdMsg.mPwd:" + cmdMsg.mPwd);
        if (!this.mDNSaddrequest && cmdMsg.mTransportProtocol != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: mPreviousKeepChannelId:" + this.mPreviousKeepChannelId + " mChannelStatus:" + this.mChannelStatus + " mApn:" + this.mApn);
            if (this.mPreviousKeepChannelId == 0 || 4 != this.mChannelStatus) {
                this.mChannelId = this.mBipChannelManager.acquireChannelId(cmdMsg.mTransportProtocol.protocolType);
                if (this.mChannelId == 0) {
                    MtkCatLog.d("[BIP]", "BM-openChannel: acquire channel id = 0");
                    response.arg1 = 5;
                    response.obj = cmdMsg;
                    this.mCurrentCmd = cmdMsg;
                    this.mBipSrvHandler.sendMessage(response);
                    MtkCatLog.d("[BIP]", "BM-openChannel: channel id = 0. mCurrentCmd = " + this.mCurrentCmd);
                    return;
                }
                this.mApn = cmdMsg.mApn;
                this.mLogin = cmdMsg.mLogin;
                this.mPassword = cmdMsg.mPwd;
            } else if (!(this.mApn == null && cmdMsg.mApn == null) && (this.mApn == null || cmdMsg.mApn == null || true != this.mApn.equals(cmdMsg.mApn))) {
                this.mCurrentCmd = cmdMsg;
                releaseRequest();
                resetLocked();
                Channel pchannel = this.mBipChannelManager.getChannel(this.mPreviousKeepChannelId);
                if (pchannel != null) {
                    pchannel.closeChannel();
                }
                this.mBipChannelManager.removeChannel(this.mPreviousKeepChannelId);
                deleteApnParams();
                setPdnReuse("1");
                this.mChannel = null;
                this.mChannelStatus = 2;
                this.mApn = null;
                this.mLogin = null;
                this.mPassword = null;
                if (this.mPreviousKeepChannelId != 0) {
                    sendBipDisconnectTimeOutMsg(cmdMsg);
                    return;
                }
                return;
            } else if (cmdMsg.mTransportProtocol.protocolType == this.mPreviousProtocolType) {
                this.mChannelId = this.mPreviousKeepChannelId;
                cmdMsg.mChannelStatusData = new ChannelStatus(this.mChannelId, 128, 0);
                this.mCurrentCmd = cmdMsg;
                response.arg1 = 0;
                response.obj = cmdMsg;
                this.mBipSrvHandler.sendMessage(response);
                this.mPreviousKeepChannelId = 0;
                return;
            } else {
                MtkCatLog.d("[BIP]", "BM-openChannel: channel protocol type changed!");
                Channel pchannel2 = this.mBipChannelManager.getChannel(this.mPreviousKeepChannelId);
                if (pchannel2 != null) {
                    pchannel2.closeChannel();
                }
                this.mBipChannelManager.removeChannel(this.mPreviousKeepChannelId);
                this.mChannel = null;
                this.mChannelStatus = 2;
                this.mChannelId = this.mBipChannelManager.acquireChannelId(cmdMsg.mTransportProtocol.protocolType);
                if (this.mChannelId == 0) {
                    MtkCatLog.d("[BIP]", "BM-openChannel: acquire channel id = 0");
                    response.arg1 = 5;
                    response.obj = cmdMsg;
                    this.mCurrentCmd = cmdMsg;
                    this.mBipSrvHandler.sendMessage(response);
                    return;
                }
                this.mApn = cmdMsg.mApn;
                this.mLogin = cmdMsg.mLogin;
                this.mPassword = cmdMsg.mPwd;
            }
        }
        cmdMsg.mChannelStatusData = new ChannelStatus(this.mChannelId, 0, 0);
        this.mCurrentCmd = cmdMsg;
        MtkCatLog.d("[BIP]", "BM-openChannel: mCurrentCmd = " + this.mCurrentCmd);
        this.mBearerDesc = cmdMsg.mBearerDesc;
        if (cmdMsg.mBearerDesc != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: bearer type " + cmdMsg.mBearerDesc.bearerType);
        } else {
            MtkCatLog.d("[BIP]", "BM-openChannel: bearer type is null");
        }
        this.mBufferSize = cmdMsg.mBufferSize;
        MtkCatLog.d("[BIP]", "BM-openChannel: buffer size " + cmdMsg.mBufferSize);
        this.mLocalAddress = cmdMsg.mLocalAddress;
        if (cmdMsg.mLocalAddress != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: local address " + cmdMsg.mLocalAddress.address.toString());
        } else {
            MtkCatLog.d("[BIP]", "BM-openChannel: local address is null");
        }
        if (cmdMsg.mTransportProtocol != null) {
            this.mTransportProtocol = cmdMsg.mTransportProtocol;
            MtkCatLog.d("[BIP]", "BM-openChannel: transport protocol type/port " + cmdMsg.mTransportProtocol.protocolType + "/" + cmdMsg.mTransportProtocol.portNumber);
        }
        this.mDataDestinationAddress = cmdMsg.mDataDestinationAddress;
        if (cmdMsg.mDataDestinationAddress != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: dest address " + cmdMsg.mDataDestinationAddress.address.toString());
        } else {
            MtkCatLog.d("[BIP]", "BM-openChannel: dest address is null");
        }
        if ((cmdMsg.getCmdQualifier() & 1) == 1) {
            i = 0;
        } else {
            i = 1;
        }
        this.mLinkMode = i;
        MtkCatLog.d("[BIP]", "BM-openChannel: mLinkMode " + this.mLinkMode);
        this.mAutoReconnected = (cmdMsg.getCmdQualifier() & 2) == 2;
        int[] subId = SubscriptionManager.getSubId(this.mSlotId);
        Phone myPhone = PhoneFactory.getPhone(this.mSlotId);
        BearerDesc bearerDesc = this.mBearerDesc;
        if (bearerDesc == null) {
            z = false;
            TransportProtocol transportProtocol = this.mTransportProtocol;
            if (!(transportProtocol == null || 3 == transportProtocol.protocolType || 4 == this.mTransportProtocol.protocolType || 5 == this.mTransportProtocol.protocolType)) {
                MtkCatLog.e("[BIP]", "BM-openChannel: unsupported transport protocol type !!!");
                response.arg1 = 5;
                response.obj = this.mCurrentCmd;
                this.mBipSrvHandler.sendMessage(response);
                return;
            }
        } else if (bearerDesc.bearerType == 3) {
            setPdnReuse(MtkGsmCdmaPhone.ACT_TYPE_UTRAN);
            String str = this.mApn;
            if (str == null || str.length() <= 0) {
                String numeric = null;
                if (subId != null && SubscriptionManager.isValidSubscriptionId(subId[0])) {
                    numeric = TelephonyManager.getDefault().getSimOperator(subId[0]);
                }
                MtkCatLog.d("[BIP]", "numeric: " + numeric);
                if (numeric == null || !numeric.equals("00101")) {
                    setPdnNameReuse("");
                } else {
                    String iaApn = SystemProperties.get(PROPERTY_IA_APN);
                    if (iaApn == null || iaApn.length() <= 0) {
                        MtkCatLog.d("[BIP]", "no persist ia APN, get temp ia");
                        iaApn = SystemProperties.get(PROPERTY_PERSIST_IA_APN);
                    }
                    setPdnNameReuse(iaApn);
                    MtkCatLog.d("[BIP]", "set ia APN to reuse");
                }
                z = false;
            } else {
                setPdnReuse("1");
                z = false;
            }
        } else {
            setPdnReuse("0");
            String str2 = this.mApn;
            if (str2 == null || str2.length() <= 0) {
                z = false;
            } else {
                if (myPhone != null) {
                    int dataNetworkType = myPhone.getServiceState().getDataNetworkType();
                    MtkCatLog.d("[BIP]", "dataNetworkType: " + dataNetworkType);
                    if (13 == dataNetworkType) {
                        String iaApn2 = SystemProperties.get(PROPERTY_IA_APN);
                        String numeric2 = null;
                        if (SubscriptionManager.isValidSubscriptionId(subId[0])) {
                            z = false;
                            numeric2 = TelephonyManager.getDefault().getSimOperator(subId[0]);
                        } else {
                            z = false;
                        }
                        MtkCatLog.d("[BIP]", "numeric: " + numeric2);
                        if (numeric2 != null && !numeric2.equals("00101") && iaApn2 != null && iaApn2.length() > 0 && iaApn2.equals(this.mApn)) {
                            setPdnReuse(MtkGsmCdmaPhone.ACT_TYPE_UTRAN);
                        }
                    } else {
                        z = false;
                    }
                } else {
                    z = false;
                    MtkCatLog.e("[BIP]", "myPhone is null");
                }
                MtkCatLog.d("[BIP]", "BM-openChannel: override apn: " + this.mApn);
                setOverrideApn(this.mApn);
            }
        }
        this.mApnType = "bip";
        String str3 = this.mApn;
        if (str3 == null || str3.length() <= 0) {
            String numeric3 = null;
            if (subId != null && SubscriptionManager.isValidSubscriptionId(subId[0])) {
                numeric3 = TelephonyManager.getDefault().getSimOperator(subId[0]);
            }
            MtkCatLog.d("[BIP]", "numeric: " + numeric3);
            if (numeric3 == null || !numeric3.equals("00101")) {
                this.mApnType = "default";
            } else {
                String optr = SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR);
                MtkCatLog.d("[BIP]", "Optr load: " + optr);
                if (optr != null && DataSubConstants.OPERATOR_OP01.equals(optr)) {
                    this.mApnType = "bip";
                } else if (true == isBipApnTypeSupport()) {
                    this.mApnType = "bip";
                } else {
                    this.mApnType = "default";
                }
            }
            boolean needSupport = false;
            if (myPhone != null) {
                PersistableBundle b = ((CarrierConfigManager) myPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(myPhone.getSubId());
                if (b != null) {
                    needSupport = b.getBoolean("mtk_use_administrative_apn_bool");
                }
                if (needSupport) {
                    MtkCatLog.v("[BIP]", "support KDDI feature");
                    int dataNetworkType2 = myPhone.getServiceState().getDataNetworkType();
                    MtkCatLog.d("[BIP]", "dataNetworkType: " + dataNetworkType2);
                    if (13 == dataNetworkType2) {
                        this.mApnType = "fota";
                    }
                }
            } else {
                MtkCatLog.e("[BIP]", "myPhone is null");
            }
        } else if (this.mApn.equals("VZWADMIN") || this.mApn.equals("vzwadmin")) {
            this.mApnType = "fota";
        } else if (this.mApn.equals("VZWINTERNET") || this.mApn.equals("vzwinternet")) {
            this.mApnType = "internet";
        } else if (this.mApn.equals("titi") || this.mApn.equals("web99.test-nfc1.com")) {
            this.mApnType = "fota";
        } else if (this.mApn.equals("otasn") || this.mApn.equals("OTASN")) {
            this.mApnType = "fota";
        } else {
            this.mApnType = "bip";
            setApnParams(this.mApn, this.mLogin, this.mPassword);
        }
        MtkCatLog.d("[BIP]", "APN Type: " + this.mApnType);
        MtkCatLog.d("[BIP]", "MAXCHANNELID: 7");
        TransportProtocol transportProtocol2 = this.mTransportProtocol;
        if (transportProtocol2 != null && 3 == transportProtocol2.protocolType) {
            int ret = establishLink();
            if (ret == 0 || ret == 3) {
                MtkCatLog.d("[BIP]", "BM-openChannel: channel is activated");
                cmdMsg.mChannelStatusData.mChannelStatus = this.mBipChannelManager.getChannel(this.mChannelId).mChannelStatusData.mChannelStatus;
            } else {
                MtkCatLog.d("[BIP]", "BM-openChannel: channel is un-activated");
                cmdMsg.mChannelStatusData.mChannelStatus = 0;
            }
            response.arg1 = ret;
            response.obj = this.mCurrentCmd;
            this.mBipSrvHandler.sendMessage(response);
        } else if (true == this.mIsApnInserting) {
            MtkCatLog.d("[BIP]", "BM-openChannel: startUsingNetworkFeature delay trigger.");
            Message timerMsg = this.mBipSrvHandler.obtainMessage(11);
            timerMsg.obj = cmdMsg;
            this.mBipSrvHandler.sendMessageDelayed(timerMsg, 5000);
            this.mIsApnInserting = false;
        } else {
            acquireNetwork();
        }
        MtkCatLog.d("[BIP]", "BM-openChannel: exit");
    }

    public void openChannelOverWifi(BipCmdMessage cmdMsg, Message response) {
        int i;
        MtkCatLog.d("[BIP]", "BM-openChannelOverWifi: enter");
        boolean z = false;
        this.isConnMgrIntentTimeout = false;
        if (cmdMsg.mTransportProtocol == null) {
            MtkCatLog.e("[BIP]", "BM-openChannel: transport protocol is null");
            return;
        }
        this.mChannelId = this.mBipChannelManager.acquireChannelId(cmdMsg.mTransportProtocol.protocolType);
        int i2 = this.mChannelId;
        if (i2 == 0) {
            MtkCatLog.d("[BIP]", "BM-openChannel: acquire channel id = 0");
            response.arg1 = 5;
            response.obj = cmdMsg;
            this.mCurrentCmd = cmdMsg;
            this.mBipSrvHandler.sendMessage(response);
            return;
        }
        cmdMsg.mChannelStatusData = new ChannelStatus(i2, 0, 0);
        this.mCurrentCmd = cmdMsg;
        this.mBearerDesc = cmdMsg.mBearerDesc;
        if (cmdMsg.mBearerDesc != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: bearer type " + cmdMsg.mBearerDesc.bearerType);
        } else {
            MtkCatLog.d("[BIP]", "BM-openChannel: bearer type is null");
        }
        this.mBufferSize = cmdMsg.mBufferSize;
        MtkCatLog.d("[BIP]", "BM-openChannel: buffer size " + cmdMsg.mBufferSize);
        this.mLocalAddress = cmdMsg.mLocalAddress;
        if (cmdMsg.mLocalAddress != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: local address " + cmdMsg.mLocalAddress.address.toString());
        } else {
            MtkCatLog.d("[BIP]", "BM-openChannel: local address is null");
        }
        this.mTransportProtocol = cmdMsg.mTransportProtocol;
        if (cmdMsg.mTransportProtocol != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: transport protocol type/port " + cmdMsg.mTransportProtocol.protocolType + "/" + cmdMsg.mTransportProtocol.portNumber);
        } else {
            MtkCatLog.d("[BIP]", "BM-openChannel: transport protocol is null");
        }
        this.mDataDestinationAddress = cmdMsg.mDataDestinationAddress;
        if (cmdMsg.mDataDestinationAddress != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: dest address " + cmdMsg.mDataDestinationAddress.address.toString());
        } else {
            MtkCatLog.d("[BIP]", "BM-openChannel: dest address is null");
        }
        this.mApn = cmdMsg.mApn;
        if (cmdMsg.mApn != null) {
            MtkCatLog.d("[BIP]", "BM-openChannel: apn " + cmdMsg.mApn);
        } else {
            MtkCatLog.d("[BIP]", "BM-openChannel: apn is null.");
        }
        this.mLogin = cmdMsg.mLogin;
        MtkCatLog.d("[BIP]", "BM-openChannel: login " + cmdMsg.mLogin);
        this.mPassword = cmdMsg.mPwd;
        MtkCatLog.d("[BIP]", "BM-openChannel: password " + cmdMsg.mPwd);
        if ((cmdMsg.getCmdQualifier() & 1) == 1) {
            i = 0;
        } else {
            i = 1;
        }
        this.mLinkMode = i;
        MtkCatLog.d("[BIP]", "BM-openChannel: mLinkMode " + cmdMsg.getCmdQualifier());
        if ((cmdMsg.getCmdQualifier() & 2) != 0) {
            z = true;
        }
        this.mAutoReconnected = z;
        if (isSprintSupport() && isWifiConnected()) {
            this.mIsOpenChannelOverWifi = true;
        }
        MtkCatLog.d("[BIP]", "BM-openChannel: call startUsingNetworkFeature:" + this.mSlotId);
        MtkCatLog.d("[BIP]", "MAXCHANNELID :7");
        acquireNetwork();
        MtkCatLog.d("[BIP]", "BM-openChannelOverWifi: exit");
    }

    public void closeChannel(BipCmdMessage cmdMsg, Message response) {
        MtkCatLog.d("[BIP]", "BM-closeChannel: enter");
        int cId = cmdMsg.mCloseCid;
        response.arg1 = 0;
        this.mCurrentCmd = cmdMsg;
        if (cId < 0 || 7 < cId) {
            MtkCatLog.d("[BIP]", "BM-closeChannel: channel id:" + cId + " is invalid !!!");
            response.arg1 = 7;
        } else {
            this.mPreviousKeepChannelId = 0;
            MtkCatLog.d("[BIP]", "BM-closeChannel: getBipChannelStatus:" + this.mBipChannelManager.getBipChannelStatus(cId));
            try {
                if (this.mBipChannelManager.getBipChannelStatus(cId) == 0) {
                    MtkCatLog.d("[BIP]", "BM-closeChannel: mDNSaddrequest:" + this.mDNSaddrequest);
                    if (true == this.mDNSaddrequest) {
                        response.arg1 = 0;
                    } else {
                        response.arg1 = 7;
                    }
                } else if (2 == this.mBipChannelManager.getBipChannelStatus(cId)) {
                    response.arg1 = 8;
                } else {
                    Channel lChannel = this.mBipChannelManager.getChannel(cId);
                    if (lChannel == null) {
                        MtkCatLog.d("[BIP]", "BM-closeChannel: channel has already been closed");
                        response.arg1 = 7;
                    } else {
                        TcpServerChannel tcpSerCh = null;
                        MtkCatLog.d("[BIP]", "BM-closeChannel: mProtocolType:" + lChannel.mProtocolType + " getCmdQualifier:" + cmdMsg.getCmdQualifier());
                        if (3 == lChannel.mProtocolType) {
                            if (lChannel instanceof TcpServerChannel) {
                                tcpSerCh = (TcpServerChannel) lChannel;
                                tcpSerCh.setCloseBackToTcpListen(cmdMsg.mCloseBackToTcpListen);
                            }
                            response.arg1 = lChannel.closeChannel();
                        } else if (1 == (cmdMsg.getCmdQualifier() & 1)) {
                            this.mPreviousKeepChannelId = cId;
                            this.mPreviousProtocolType = lChannel.mProtocolType;
                            MtkCatLog.d("[BIP]", "BM-closeChannel: mPreviousKeepChannelId:" + this.mPreviousKeepChannelId + " mPreviousProtocolType:" + this.mPreviousProtocolType);
                            response.arg1 = 0;
                        } else {
                            MtkCatLog.d("[BIP]", "BM-closeChannel: stop data connection");
                            this.mIsCloseInProgress = true;
                            releaseRequest();
                            resetLocked();
                            if (!this.mIsOpenChannelOverWifi) {
                                deleteOrRestoreApnParams();
                                setPdnReuse("1");
                            } else {
                                this.mIsOpenChannelOverWifi = false;
                            }
                            response.arg1 = lChannel.closeChannel();
                        }
                        if (3 == lChannel.mProtocolType) {
                            if (tcpSerCh != null && !tcpSerCh.isCloseBackToTcpListen()) {
                                this.mBipChannelManager.removeChannel(cId);
                            }
                            this.mChannel = null;
                            this.mChannelStatus = 2;
                        } else if (1 == (cmdMsg.getCmdQualifier() & 1)) {
                            sendBipChannelKeepTimeOutMsg(cmdMsg);
                        } else {
                            this.mBipChannelManager.removeChannel(cId);
                            this.mChannel = null;
                            this.mChannelStatus = 2;
                            this.mApn = null;
                            this.mLogin = null;
                            this.mPassword = null;
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                MtkCatLog.e("[BIP]", "BM-closeChannel: IndexOutOfBoundsException cid=" + cId);
                response.arg1 = 7;
            }
        }
        if (!this.mIsCloseInProgress) {
            response.obj = cmdMsg;
            this.mBipSrvHandler.sendMessage(response);
        } else {
            sendBipDisconnectTimeOutMsg(cmdMsg);
        }
        MtkCatLog.d("[BIP]", "BM-closeChannel: exit");
    }

    public void receiveData(BipCmdMessage cmdMsg, Message response) {
        int requestCount = cmdMsg.mChannelDataLength;
        ReceiveDataResult result = new ReceiveDataResult();
        int cId = cmdMsg.mReceiveDataCid;
        Channel lChannel = this.mBipChannelManager.getChannel(cId);
        MtkCatLog.d("[BIP]", "BM-receiveData: receiveData enter");
        if (lChannel == null) {
            MtkCatLog.e("[BIP]", "lChannel is null cid=" + cId);
            response.arg1 = 5;
            response.obj = cmdMsg;
            this.mBipSrvHandler.sendMessage(response);
        } else if (lChannel.mChannelStatus == 4 || lChannel.mChannelStatus == 3) {
            if (requestCount > 237) {
                MtkCatLog.d("[BIP]", "BM-receiveData: Modify channel data length to MAX_APDU_SIZE");
                requestCount = BipUtils.MAX_APDU_SIZE;
            }
            new Thread(new RecvDataRunnable(requestCount, result, cmdMsg, response)).start();
        } else {
            MtkCatLog.d("[BIP]", "BM-receiveData: Channel status is invalid " + this.mChannelStatus);
            response.arg1 = 5;
            response.obj = cmdMsg;
            this.mBipSrvHandler.sendMessage(response);
        }
    }

    public void sendData(BipCmdMessage cmdMsg, Message response) {
        MtkCatLog.d("[BIP]", "sendData: Enter");
        new Thread(new SendDataThread(cmdMsg, response)).start();
        MtkCatLog.d("[BIP]", "sendData: Leave");
    }

    private void newRequest() {
        ConnectivityManager connectivityManager = getConnectivityManager();
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.mediatek.internal.telephony.cat.BipService.AnonymousClass2 */

            public void onAvailable(Network network) {
                super.onAvailable(network);
                if (!BipService.this.isConnMgrIntentTimeout) {
                    BipService.this.mBipSrvHandler.removeMessages(10);
                }
                MtkCatLog.d("[BIP]", "NetworkCallbackListener.onAvailable, mChannelId: " + BipService.this.mChannelId + " , mIsOpenInProgress: " + BipService.this.mIsOpenInProgress + " , mIsNetworkAvailableReceived: " + BipService.this.mIsNetworkAvailableReceived);
                if (true == BipService.this.mDNSaddrequest && true == BipService.this.mIsOpenInProgress) {
                    BipService.this.queryDnsServerAddress(network);
                } else if (true != BipService.this.mIsOpenInProgress || BipService.this.mIsNetworkAvailableReceived) {
                    MtkCatLog.d("[BIP]", "Bip channel has been established.");
                } else {
                    boolean unused = BipService.this.mIsNetworkAvailableReceived = true;
                    Network unused2 = BipService.this.mNetwork = network;
                    BipService.this.connect();
                }
            }

            public void onLost(Network network) {
                super.onLost(network);
                if (!BipService.this.isConnMgrIntentTimeout) {
                    BipService.this.mBipSrvHandler.removeMessages(10);
                }
                BipService.this.mBipSrvHandler.removeMessages(21);
                int unused = BipService.this.mPreviousKeepChannelId = 0;
                MtkCatLog.d("[BIP]", "onLost: network:" + network + " mNetworkCallback:" + BipService.this.mNetworkCallback + " this:" + this);
                BipService.this.releaseRequest();
                BipService.this.resetLocked();
                BipService.this.disconnect();
            }

            public void onUnavailable() {
                super.onUnavailable();
                if (!BipService.this.isConnMgrIntentTimeout) {
                    BipService.this.mBipSrvHandler.removeMessages(10);
                }
                BipService.this.mBipSrvHandler.removeMessages(21);
                int unused = BipService.this.mPreviousKeepChannelId = 0;
                MtkCatLog.d("[BIP]", "onUnavailable: mNetworkCallback:" + BipService.this.mNetworkCallback + " this:" + this);
                BipService.this.releaseRequest();
                BipService.this.resetLocked();
                BipService.this.disconnect();
            }
        };
        int[] subId = SubscriptionManager.getSubId(this.mSlotId);
        int networkCapability = 27;
        String str = this.mApnType;
        if (str == null || !str.equals("default")) {
            String str2 = this.mApnType;
            if (str2 == null || !str2.equals("internet")) {
                String str3 = this.mApnType;
                if (str3 == null || !str3.equals("fota")) {
                    String str4 = this.mApnType;
                    if (str4 != null && str4.equals("supl")) {
                        networkCapability = 1;
                    }
                } else {
                    networkCapability = 3;
                }
            } else {
                networkCapability = 12;
            }
        } else {
            networkCapability = 12;
        }
        if (subId == null || !SubscriptionManager.from(this.mContext).isActiveSubId(subId[0])) {
            this.mNetworkRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(networkCapability).build();
        } else {
            this.mNetworkRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(networkCapability).setNetworkSpecifier(String.valueOf(subId[0])).build();
        }
        MtkCatLog.d("[BIP]", "Start request network timer.");
        sendBipConnTimeOutMsg(this.mCurrentCmd);
        MtkCatLog.d("[BIP]", "requestNetwork: mNetworkRequest:" + this.mNetworkRequest + " mNetworkCallback:" + this.mNetworkCallback);
        connectivityManager.requestNetwork(this.mNetworkRequest, this.mNetworkCallback, CONN_MGR_TIMEOUT);
    }

    private void newRequestOverWifi() {
        MtkCatLog.d("[BIP]", "Open channel over wifi");
        ConnectivityManager connectivityManager = getConnectivityManager();
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.mediatek.internal.telephony.cat.BipService.AnonymousClass3 */

            public void onAvailable(Network network) {
                super.onAvailable(network);
                if (!BipService.this.isConnMgrIntentTimeout) {
                    BipService.this.mBipSrvHandler.removeMessages(10);
                }
                Channel channel = BipService.this.mBipChannelManager.getChannel(BipService.this.mChannelId);
                MtkCatLog.d("[BIP]", "NetworkCallbackListener.onAvailable, mChannelId: " + BipService.this.mChannelId + " , mIsOpenInProgress: " + BipService.this.mIsOpenInProgress + " , mIsNetworkAvailableReceived: " + BipService.this.mIsNetworkAvailableReceived);
                if (channel == null) {
                    MtkCatLog.d("[BIP]", "Channel is null.");
                }
                if (true != BipService.this.mIsOpenInProgress || BipService.this.mIsNetworkAvailableReceived) {
                    MtkCatLog.d("[BIP]", "Bip channel has been established.");
                    return;
                }
                boolean unused = BipService.this.mIsNetworkAvailableReceived = true;
                Network unused2 = BipService.this.mNetwork = network;
                BipService.this.connect();
            }

            public void onLost(Network network) {
                super.onLost(network);
                if (!BipService.this.isConnMgrIntentTimeout) {
                    BipService.this.mBipSrvHandler.removeMessages(10);
                }
                MtkCatLog.d("[BIP]", "NetworkCallbackListener.onLost: network=" + network);
                BipService.this.releaseRequest();
                BipService.this.resetLocked();
                BipService.this.disconnect();
            }

            public void onUnavailable() {
                super.onUnavailable();
                if (!BipService.this.isConnMgrIntentTimeout) {
                    BipService.this.mBipSrvHandler.removeMessages(10);
                }
                MtkCatLog.d("[BIP]", "NetworkCallbackListener.onUnavailable");
                BipService.this.releaseRequest();
                BipService.this.resetLocked();
                BipService.this.disconnect();
            }
        };
        this.mNetworkRequest = new NetworkRequest.Builder().addTransportType(1).addCapability(12).build();
        connectivityManager.requestNetwork(this.mNetworkRequest, this.mNetworkCallback, CONN_MGR_TIMEOUT);
        MtkCatLog.d("[BIP]", "Start request network timer.");
        sendBipConnTimeOutMsg(this.mCurrentCmd);
    }

    /* access modifiers changed from: private */
    public void resetLocked() {
        this.mNetwork = null;
    }

    /* access modifiers changed from: private */
    public void releaseRequest() {
        if (this.mNetworkCallback != null) {
            synchronized (this.mReleaseNetworkLock) {
                if (this.mNetworkCallback != null) {
                    MtkCatLog.d("[BIP]", "releaseRequest");
                    getConnectivityManager().unregisterNetworkCallback(this.mNetworkCallback);
                    this.mNetworkCallback = null;
                } else {
                    MtkCatLog.d("[BIP]", "releaseRequest: networkCallback is null.");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void queryDnsServerAddress(Network network) {
        LinkProperties curLinkProps = getConnectivityManager().getLinkProperties(network);
        if (curLinkProps == null) {
            MtkCatLog.e("[BIP]", "curLinkProps is null !!!");
            sendOpenChannelDoneMsg(5);
            return;
        }
        Collection<InetAddress> dnsAddres = curLinkProps.getDnsServers();
        if (dnsAddres == null || dnsAddres.size() == 0) {
            MtkCatLog.e("[BIP]", "LinkProps has null dnsAddres !!!");
            sendOpenChannelDoneMsg(5);
        } else if (this.mCurrentCmd != null && AppInterface.CommandType.OPEN_CHANNEL.value() == this.mCurrentCmd.mCmdDet.typeOfCommand) {
            this.mCurrentCmd.mDnsServerAddress = new DnsServerAddress();
            this.mCurrentCmd.mDnsServerAddress.dnsAddresses.clear();
            for (InetAddress addr : dnsAddres) {
                if (addr != null) {
                    MtkCatLog.d("[BIP]", "DNS Server Address:" + addr);
                    this.mCurrentCmd.mDnsServerAddress.dnsAddresses.add(addr);
                }
            }
            this.mIsOpenInProgress = false;
            sendOpenChannelDoneMsg(0);
        }
    }

    private void sendOpenChannelDoneMsg(int result) {
        this.mBipSrvHandler.sendMessage(this.mBipSrvHandler.obtainMessage(13, result, 0, this.mCurrentCmd));
    }

    protected class SendDataThread implements Runnable {
        BipCmdMessage cmdMsg;
        Message response;

        SendDataThread(BipCmdMessage Msg, Message resp) {
            MtkCatLog.d("[BIP]", "SendDataThread Init");
            this.cmdMsg = Msg;
            this.response = resp;
        }

        public void run() {
            int ret;
            MtkCatLog.d("[BIP]", "SendDataThread Run Enter");
            byte[] buffer = this.cmdMsg.mChannelData;
            int mode = this.cmdMsg.mSendMode;
            Channel lChannel = BipService.this.mBipChannelManager.getChannel(this.cmdMsg.mSendDataCid);
            if (lChannel == null) {
                MtkCatLog.d("[BIP]", "SendDataThread Run mChannelId != cmdMsg.mSendDataCid");
                ret = 7;
            } else if (lChannel.mChannelStatus == 4) {
                MtkCatLog.d("[BIP]", "SendDataThread Run mChannel.sendData");
                ret = lChannel.sendData(buffer, mode);
                this.response.arg2 = lChannel.getTxAvailBufferSize();
            } else {
                MtkCatLog.d("[BIP]", "SendDataThread Run CHANNEL_ID_NOT_VALID");
                ret = 7;
            }
            Message message = this.response;
            message.arg1 = ret;
            message.obj = this.cmdMsg;
            MtkCatLog.d("[BIP]", "SendDataThread Run mBipSrvHandler.sendMessage(response);");
            BipService.this.mBipSrvHandler.sendMessage(this.response);
        }
    }

    public void getChannelStatus(BipCmdMessage cmdMsg, Message response) {
        int cId = 1;
        List<ChannelStatus> csList = new ArrayList<>();
        while (true) {
            try {
                BipChannelManager bipChannelManager = this.mBipChannelManager;
                if (cId > 7) {
                    break;
                }
                if (true == this.mBipChannelManager.isChannelIdOccupied(cId)) {
                    MtkCatLog.d("[BIP]", "getChannelStatus: cId:" + cId);
                    csList.add(this.mBipChannelManager.getChannel(cId).mChannelStatusData);
                }
                cId++;
            } catch (NullPointerException ne) {
                MtkCatLog.e("[BIP]", "getChannelStatus: NE");
                ne.printStackTrace();
            }
        }
        cmdMsg.mChannelStatusList = csList;
        response.arg1 = 0;
        response.obj = cmdMsg;
        this.mBipSrvHandler.sendMessage(response);
    }

    private void sendBipConnTimeOutMsg(BipCmdMessage cmdMsg) {
        Message bipTimerMsg = this.mBipSrvHandler.obtainMessage(10);
        bipTimerMsg.obj = cmdMsg;
        this.mBipSrvHandler.sendMessageDelayed(bipTimerMsg, 50000);
    }

    private void sendBipDisconnectTimeOutMsg(BipCmdMessage cmdMsg) {
        Message bipTimerMsg = this.mBipSrvHandler.obtainMessage(12);
        bipTimerMsg.obj = cmdMsg;
        this.mBipSrvHandler.sendMessageDelayed(bipTimerMsg, 5000);
    }

    private void sendBipChannelKeepTimeOutMsg(BipCmdMessage cmdMsg) {
        Message bipTimerMsg = this.mBipSrvHandler.obtainMessage(21);
        bipTimerMsg.obj = cmdMsg;
        this.mBipSrvHandler.sendMessageDelayed(bipTimerMsg, 30000);
    }

    private void updateCurrentChannelStatus(int status) {
        try {
            this.mBipChannelManager.updateChannelStatus(this.mChannelId, status);
            this.mCurrentCmd.mChannelStatusData.mChannelStatus = status;
        } catch (NullPointerException ne) {
            MtkCatLog.e("[BIP]", "updateCurrentChannelStatus id:" + this.mChannelId + " is null");
            ne.printStackTrace();
        }
    }

    private boolean checkNetworkInfo(NetworkInfo nwInfo, NetworkInfo.State exState) {
        if (nwInfo == null) {
            return false;
        }
        int type = nwInfo.getType();
        NetworkInfo.State state = nwInfo.getState();
        StringBuilder sb = new StringBuilder();
        sb.append("network type is ");
        sb.append(type == 0 ? "MOBILE" : "WIFI");
        MtkCatLog.d("[BIP]", sb.toString());
        MtkCatLog.d("[BIP]", "network state is " + state);
        if (type == 0 && state == exState) {
            return true;
        }
        return false;
    }

    private int establishLink() {
        int ret;
        TransportProtocol transportProtocol = this.mTransportProtocol;
        if (transportProtocol == null) {
            MtkCatLog.e("[BIP]", "BM-establishLink: mTransportProtocol is null !!!");
            return 5;
        }
        if (transportProtocol.protocolType == 3) {
            MtkCatLog.d("[BIP]", "BM-establishLink: establish a TCPServer link");
            try {
                Channel lChannel = new TcpServerChannel(this.mChannelId, this.mLinkMode, this.mTransportProtocol.protocolType, this.mTransportProtocol.portNumber, this.mBufferSize, (MtkCatService) this.mHandler, this);
                ret = lChannel.openChannel(this.mCurrentCmd, this.mNetwork);
                if (ret == 0 || ret == 3) {
                    this.mChannelStatus = 4;
                    this.mBipChannelManager.addChannel(this.mChannelId, lChannel);
                } else {
                    this.mBipChannelManager.releaseChannelId(this.mChannelId, 3);
                    this.mChannelStatus = 7;
                }
            } catch (NullPointerException ne) {
                MtkCatLog.e("[BIP]", "BM-establishLink: NE,new TCP server channel fail.");
                ne.printStackTrace();
                return 5;
            }
        } else if (this.mTransportProtocol.protocolType == 2) {
            MtkCatLog.d("[BIP]", "BM-establishLink: establish a TCP link");
            try {
                Channel lChannel2 = new TcpChannel(this.mChannelId, this.mLinkMode, this.mTransportProtocol.protocolType, this.mDataDestinationAddress.address, this.mTransportProtocol.portNumber, this.mBufferSize, (MtkCatService) this.mHandler, this);
                ret = lChannel2.openChannel(this.mCurrentCmd, this.mNetwork);
                if (ret != 10) {
                    if (ret == 0 || ret == 3) {
                        this.mChannelStatus = 4;
                        this.mBipChannelManager.addChannel(this.mChannelId, lChannel2);
                    } else {
                        this.mBipChannelManager.releaseChannelId(this.mChannelId, 2);
                        this.mChannelStatus = 7;
                    }
                }
            } catch (NullPointerException ne2) {
                MtkCatLog.e("[BIP]", "BM-establishLink: NE,new TCP client channel fail.");
                ne2.printStackTrace();
                if (this.mDataDestinationAddress == null) {
                    return 9;
                }
                return 5;
            }
        } else if (this.mTransportProtocol.protocolType == 1) {
            MtkCatLog.d("[BIP]", "BM-establishLink: establish a UDP link");
            try {
                Channel lChannel3 = new UdpChannel(this.mChannelId, this.mLinkMode, this.mTransportProtocol.protocolType, this.mDataDestinationAddress.address, this.mTransportProtocol.portNumber, this.mBufferSize, (MtkCatService) this.mHandler, this);
                ret = lChannel3.openChannel(this.mCurrentCmd, this.mNetwork);
                if (ret == 0 || ret == 3) {
                    this.mChannelStatus = 4;
                    this.mBipChannelManager.addChannel(this.mChannelId, lChannel3);
                } else {
                    this.mBipChannelManager.releaseChannelId(this.mChannelId, 1);
                    this.mChannelStatus = 7;
                }
            } catch (NullPointerException ne3) {
                MtkCatLog.e("[BIP]", "BM-establishLink: NE,new UDP client channel fail.");
                ne3.printStackTrace();
                return 5;
            }
        } else {
            MtkCatLog.d("[BIP]", "BM-establishLink: unsupported channel type");
            ret = 4;
            this.mChannelStatus = 7;
        }
        MtkCatLog.d("[BIP]", "BM-establishLink: ret:" + ret);
        return ret;
    }

    private Uri getUri(Uri uri, int slodId) {
        int[] subId = SubscriptionManager.getSubId(slodId);
        if (subId == null) {
            MtkCatLog.d("[BIP]", "BM-getUri: null subId.");
            return null;
        } else if (SubscriptionManager.isValidSubscriptionId(subId[0])) {
            return Uri.withAppendedPath(uri, "/subId/" + subId[0]);
        } else {
            MtkCatLog.d("[BIP]", "BM-getUri: invalid subId.");
            return null;
        }
    }

    private void newThreadToDelelteApn() {
        new Thread() {
            /* class com.mediatek.internal.telephony.cat.BipService.AnonymousClass4 */

            public void run() {
                BipService.this.deleteApnParams();
            }
        }.start();
    }

    /* access modifiers changed from: private */
    public void deleteApnParams() {
        MtkCatLog.d("[BIP]", "BM-deleteApnParams: enter. ");
        int rows = this.mContext.getContentResolver().delete(Telephony.Carriers.CONTENT_URI, "name = '__M-BIP__'", null);
        MtkCatLog.d("[BIP]", "BM-deleteApnParams:[" + rows + "] end");
    }

    private void setApnParams(String apn, String user, String pwd) {
        String str;
        MtkCatLog.d("[BIP]", "BM-setApnParams: enter");
        if (apn == null) {
            MtkCatLog.d("[BIP]", "BM-setApnParams: No apn parameters");
            return;
        }
        String numeric = null;
        String apnType = this.mApnType;
        int[] subId = SubscriptionManager.getSubId(this.mSlotId);
        MtkIccCardConstants.CardType cardType = MtkTelephonyManagerEx.getDefault().getCdmaCardType(this.mSlotId);
        if (cardType != null && cardType.is4GCard()) {
            String[] simNumerics = MtkTelephonyManagerEx.getDefault().getSimOperatorNumericForPhoneEx(this.mSlotId);
            if (!(simNumerics == null || simNumerics[0] == null)) {
                numeric = simNumerics[0];
            }
        } else if (subId == null || !SubscriptionManager.isValidSubscriptionId(subId[0])) {
            MtkCatLog.e("[BIP]", "BM-setApnParams: Invalid subId !!!");
        } else {
            numeric = TelephonyManager.getDefault().getSimOperator(subId[0]);
        }
        if (numeric != null && numeric.length() >= 4) {
            String mcc = numeric.substring(0, 3);
            String mnc = numeric.substring(3);
            this.mNumeric = mcc + mnc;
            String selection = "apn = '" + apn + "' COLLATE NOCASE and " + "numeric" + " = '" + mcc + mnc + "'";
            MtkCatLog.d("[BIP]", "BM-setApnParams: selection = " + selection);
            Cursor cursor = this.mContext.getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, selection, null, null);
            if (cursor != null) {
                ContentValues values = new ContentValues();
                values.put("name", BIP_NAME);
                values.put("apn", apn);
                if (user != null) {
                    values.put(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER, user);
                }
                if (pwd != null) {
                    values.put("password", pwd);
                }
                values.put(PplMessageManager.PendingMessage.KEY_TYPE, apnType);
                values.put("mcc", mcc);
                values.put("mnc", mnc);
                values.put("numeric", mcc + mnc);
                values.put("sub_id", Integer.valueOf(subId[0]));
                if (apn.equals("web99.test-nfc1.com")) {
                    values.put("protocol", "IP");
                } else {
                    OtherAddress otherAddress = this.mDataDestinationAddress;
                    if (otherAddress != null) {
                        if (otherAddress.addressType == 87) {
                            values.put("protocol", "IPV6");
                        } else {
                            values.put("protocol", "IP");
                        }
                    }
                }
                int count = cursor.getCount();
                if (count == 0) {
                    MtkCatLog.d("[BIP]", "BM-setApnParams: insert one record");
                    if (this.mContext.getContentResolver().insert(Telephony.Carriers.CONTENT_URI, values) != null) {
                        MtkCatLog.d("[BIP]", "BM-setApnParams: insert a new record into db");
                        this.mIsApnInserting = true;
                    } else {
                        MtkCatLog.d("[BIP]", "BM-setApnParams: Fail to insert new record into db");
                    }
                } else if (count >= 1) {
                    MtkCatLog.d("[BIP]", "BM-setApnParams: count  = " + count);
                    if (cursor.moveToFirst()) {
                        while (true) {
                            if (count <= 0) {
                                break;
                            }
                            this.mApnTypeDb = cursor.getString(cursor.getColumnIndexOrThrow(PplMessageManager.PendingMessage.KEY_TYPE));
                            if (this.mApnTypeDb.contains("default")) {
                                MtkCatLog.d("[BIP]", "BM-setApnParams: find default apn type");
                                break;
                            }
                            count--;
                            if (!cursor.isLast()) {
                                cursor.moveToNext();
                            } else {
                                cursor.moveToFirst();
                            }
                        }
                        this.mUri = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, (long) Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id"))));
                        this.mApnTypeDb = cursor.getString(cursor.getColumnIndexOrThrow(PplMessageManager.PendingMessage.KEY_TYPE));
                        this.mLoginDb = cursor.getString(cursor.getColumnIndexOrThrow(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER));
                        this.mPasswordDb = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                        TelephonyManager telephony = TelephonyManager.getDefault();
                        boolean dataEnabled = true;
                        if (telephony != null) {
                            dataEnabled = telephony.getDataEnabled(subId[0]);
                            MtkCatLog.d("[BIP]", "BM-setApnParams: dataEnabled = " + dataEnabled);
                        }
                        ContentValues updateValues = new ContentValues();
                        MtkCatLog.d("[BIP]", "BM-setApnParams: apn old value : " + this.mApnTypeDb);
                        String str2 = this.mApnTypeDb;
                        if (str2 == null || !str2.contains("default") || !dataEnabled) {
                            String str3 = this.mApnTypeDb;
                            if (str3 == null || !str3.contains("supl") || !dataEnabled) {
                                String str4 = this.mApnTypeDb;
                                if (str4 == null || !str4.contains("fota")) {
                                    this.mApnType = "bip";
                                } else {
                                    this.mApnType = "fota";
                                }
                            } else {
                                this.mApnType = "supl";
                            }
                        } else {
                            this.mApnType = "default";
                        }
                        if (this.mApn.equals("orange") || this.mApn.equals("Orange")) {
                            String str5 = this.mApnTypeDb;
                            if (str5 == null || !str5.contains("supl")) {
                                this.mApnType = "bip";
                            } else {
                                this.mApnType = "supl";
                            }
                        }
                        MtkCatLog.d("[BIP]", "BM-setApnParams: mApnType :" + this.mApnType);
                        String str6 = this.mApnTypeDb;
                        if (str6 != null && !str6.contains(this.mApnType)) {
                            String apnTypeDbNew = this.mApnTypeDb + "," + this.mApnType;
                            MtkCatLog.d("[BIP]", "BM-setApnParams: will update apn to :" + apnTypeDbNew);
                            updateValues.put(PplMessageManager.PendingMessage.KEY_TYPE, apnTypeDbNew);
                        }
                        MtkCatLog.v("[BIP]", "BM-restoreApnParams: mLogin: " + this.mLogin + "mLoginDb:" + this.mLoginDb + "mPassword" + this.mPassword + "mPasswordDb" + this.mPasswordDb);
                        String str7 = this.mLogin;
                        if ((str7 != null && !str7.equals(this.mLoginDb)) || ((str = this.mPassword) != null && !str.equals(this.mPasswordDb))) {
                            MtkCatLog.d("[BIP]", "BM-setApnParams: will update login and password");
                            updateValues.put(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER, this.mLogin);
                            updateValues.put("password", this.mPassword);
                        }
                        if (this.mUri == null || updateValues.size() <= 0) {
                            MtkCatLog.d("[BIP]", "No need update APN db");
                        } else {
                            MtkCatLog.d("[BIP]", "BM-setApnParams: will update apn db");
                            this.mContext.getContentResolver().update(this.mUri, updateValues, null, null);
                            this.mIsApnInserting = true;
                            this.mIsUpdateApnParams = true;
                        }
                    }
                } else {
                    MtkCatLog.d("[BIP]", "BM-setApnParams: do not update one record");
                }
                cursor.close();
            }
        }
        MtkCatLog.d("[BIP]", "BM-setApnParams: exit");
    }

    private void restoreApnParams() {
        String str;
        Cursor cursor = null;
        if (this.mUri != null) {
            cursor = this.mContext.getContentResolver().query(this.mUri, null, null, null, null);
        } else {
            MtkCatLog.w("[BIP]", "restoreApnParams mUri is null!!!!");
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String apnTypeDb = cursor.getString(cursor.getColumnIndexOrThrow(PplMessageManager.PendingMessage.KEY_TYPE));
                MtkCatLog.d("[BIP]", "BM-restoreApnParams: apnTypeDb before = " + apnTypeDb);
                ContentValues updateValues = new ContentValues();
                if (apnTypeDb != null && !apnTypeDb.equals(this.mApnTypeDb) && apnTypeDb.contains(this.mApnType)) {
                    String apnTypeDb2 = apnTypeDb.replaceAll("," + this.mApnType, "");
                    MtkCatLog.d("[BIP]", "BM-restoreApnParams: apnTypeDb after = " + apnTypeDb2);
                    updateValues.put(PplMessageManager.PendingMessage.KEY_TYPE, apnTypeDb2);
                }
                MtkCatLog.v("[BIP]", "BM-restoreApnParams: mLogin: " + this.mLogin + "mLoginDb:" + this.mLoginDb + "mPassword" + this.mPassword + "mPasswordDb" + this.mPasswordDb);
                String str2 = this.mLogin;
                if ((str2 != null && !str2.equals(this.mLoginDb)) || ((str = this.mPassword) != null && !str.equals(this.mPasswordDb))) {
                    updateValues.put(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER, this.mLoginDb);
                    updateValues.put("password", this.mPasswordDb);
                }
                if (this.mUri != null && updateValues.size() > 0) {
                    this.mContext.getContentResolver().update(this.mUri, updateValues, null, null);
                    this.mUri = null;
                    this.mIsUpdateApnParams = false;
                }
            }
            cursor.close();
        }
    }

    private void deleteOrRestoreApnParams() {
        if (this.mIsUpdateApnParams) {
            restoreApnParams();
        } else {
            deleteApnParams();
        }
    }

    private int getCurrentSubId() {
        int[] subId = SubscriptionManager.getSubId(this.mSlotId);
        if (subId != null) {
            return subId[0];
        }
        MtkCatLog.d("[BIP]", "getCurrentSubId: invalid subId");
        return -1;
    }

    private boolean isCurrentConnectionInService(int phoneId) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            MtkCatLog.d("[BIP]", "isCurrentConnectionInService(): invalid phone id");
            return false;
        }
        Phone myPhone = PhoneFactory.getPhone(phoneId);
        if (myPhone == null) {
            MtkCatLog.d("[BIP]", "isCurrentConnectionInService(): phone null");
            return false;
        }
        ServiceStateTracker sst = myPhone.getServiceStateTracker();
        if (sst == null) {
            MtkCatLog.d("[BIP]", "isCurrentConnectionInService(): sst null");
            return false;
        } else if (sst.getCurrentDataConnectionState() == 0) {
            MtkCatLog.d("[BIP]", "isCurrentConnectionInService(): in service");
            return true;
        } else {
            MtkCatLog.d("[BIP]", "isCurrentConnectionInService(): not in service");
            return false;
        }
    }

    private boolean checkDataCapability(BipCmdMessage cmdMsg) {
        TelephonyManager mTelMan = (TelephonyManager) this.mContext.getSystemService("phone");
        int simInsertedCount = 0;
        for (int i = 0; i < mSimCount; i++) {
            if (mTelMan.hasIccCard(i)) {
                simInsertedCount++;
            }
        }
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        int[] subId = SubscriptionManager.getSubId(this.mSlotId);
        if (subId != null) {
            int currentSubId = subId[0];
            MtkCatLog.d("[BIP]", "checkDataCapability: simInsertedCount:" + simInsertedCount + " currentSubId:" + currentSubId + " defaultDataSubId:" + defaultDataSubId);
            if (simInsertedCount < 2 || cmdMsg.mBearerDesc == null || (!(2 == cmdMsg.mBearerDesc.bearerType || 3 == cmdMsg.mBearerDesc.bearerType || 9 == cmdMsg.mBearerDesc.bearerType || 11 == cmdMsg.mBearerDesc.bearerType) || currentSubId == defaultDataSubId)) {
                MtkCatLog.d("[BIP]", "checkDataCapability: return true");
                return true;
            }
            MtkCatLog.d("[BIP]", "checkDataCapability: return false");
            return false;
        }
        MtkCatLog.d("[BIP]", "checkDataCapability: invalid subId");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiConnected() {
        NetworkInfo activeInfo = getConnectivityManager().getActiveNetworkInfo();
        if (activeInfo == null) {
            MtkCatLog.d("[BIP]", "activeInfo is null !!!");
            return false;
        }
        MtkCatLog.d("[BIP]", "activeInfo getType:" + activeInfo.getType() + " isConnected:" + activeInfo.isConnected());
        if (!activeInfo.isConnected() || 1 != activeInfo.getType()) {
            MtkCatLog.d("[BIP]", "Wifi disconnected!");
            return false;
        }
        MtkCatLog.d("[BIP]", "Wifi connected!");
        return true;
    }

    public int getChannelId() {
        MtkCatLog.d("[BIP]", "BM-getChannelId: channel id is " + this.mChannelId);
        return this.mChannelId;
    }

    public int getFreeChannelId() {
        return this.mBipChannelManager.getFreeChannelId();
    }

    public void openChannelCompleted(int ret, Channel lChannel) {
        MtkCatLog.d("[BIP]", "BM-openChannelCompleted: ret: " + ret);
        if (ret == 3) {
            this.mCurrentCmd.mBufferSize = this.mBufferSize;
        }
        if (ret == 0 || ret == 3) {
            this.mChannelStatus = 4;
            this.mBipChannelManager.addChannel(this.mChannelId, lChannel);
        } else {
            this.mBipChannelManager.releaseChannelId(this.mChannelId, 2);
            this.mChannelStatus = 7;
        }
        this.mCurrentCmd.mChannelStatusData = lChannel.mChannelStatusData;
        if (true == this.mIsOpenInProgress && !this.isConnMgrIntentTimeout) {
            this.mIsOpenInProgress = false;
            this.mIsNetworkAvailableReceived = false;
            Message response = this.mBipSrvHandler.obtainMessage(13, ret, 0, this.mCurrentCmd);
            response.arg1 = ret;
            response.obj = this.mCurrentCmd;
            this.mBipSrvHandler.sendMessage(response);
        }
    }

    public BipChannelManager getBipChannelManager() {
        return this.mBipChannelManager;
    }

    /* access modifiers changed from: private */
    public boolean isSprintSupport() {
        if (!"OP20".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""))) {
            return false;
        }
        MtkCatLog.d("[BIP]", "isSprintSupport: true");
        return true;
    }

    private boolean isVzWSupport() {
        if ("OP12".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""))) {
            return true;
        }
        return false;
    }

    private boolean isBipApnTypeSupport() {
        String selection = "apn = '" + "TestGp.rs" + "' COLLATE NOCASE and " + "numeric" + " = '" + "001" + "01" + "'";
        MtkCatLog.d("[BIP]", "isBipApnTypeSupport: selection = " + selection);
        Cursor cursor = this.mContext.getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, selection, null, null);
        if (cursor != null) {
            if (cursor.getCount() == 0) {
                MtkCatLog.d("[BIP]", "There is no bip type apn for test sim");
                cursor.close();
                return true;
            }
            MtkCatLog.d("[BIP]", "TestGp.rs count = " + cursor.getCount());
            if (cursor.moveToFirst()) {
                String testApnType = cursor.getString(cursor.getColumnIndexOrThrow(PplMessageManager.PendingMessage.KEY_TYPE));
                MtkCatLog.d("[BIP]", "test apn type in db : " + testApnType);
                if (testApnType != null && testApnType.contains("default")) {
                    cursor.close();
                    return false;
                }
            }
            cursor.close();
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void setPdnReuse(String pdnReuse) {
        MtkCatLog.d("[BIP]", "setPdnReuse to  " + pdnReuse);
        this.mMtkCmdIf.setVendorSetting(5, pdnReuse, null);
    }

    private void setOverrideApn(String overrideApn) {
        this.mMtkCmdIf.setVendorSetting(6, overrideApn, null);
    }

    private void setPdnNameReuse(String apnName) {
        this.mMtkCmdIf.setVendorSetting(7, apnName, null);
    }

    public void setConnMgrTimeoutFlag(boolean flag) {
        this.isConnMgrIntentTimeout = flag;
    }

    public void setOpenInProgressFlag(boolean flag) {
        this.mIsOpenInProgress = flag;
    }

    private class RecvDataRunnable implements Runnable {
        BipCmdMessage cmdMsg;
        int requestDataSize;
        Message response;
        ReceiveDataResult result;

        public RecvDataRunnable(int size, ReceiveDataResult result2, BipCmdMessage cmdMsg2, Message response2) {
            this.requestDataSize = size;
            this.result = result2;
            this.cmdMsg = cmdMsg2;
            this.response = response2;
        }

        public void run() {
            int errCode;
            MtkCatLog.d("[BIP]", "BM-receiveData: start to receive data");
            Channel lChannel = BipService.this.mBipChannelManager.getChannel(this.cmdMsg.mReceiveDataCid);
            if (lChannel == null) {
                errCode = 5;
            } else {
                synchronized (lChannel.mLock) {
                    lChannel.isReceiveDataTRSent = false;
                }
                errCode = lChannel.receiveData(this.requestDataSize, this.result);
            }
            this.cmdMsg.mChannelData = this.result.buffer;
            this.cmdMsg.mRemainingDataLength = this.result.remainingCount;
            Message message = this.response;
            message.arg1 = errCode;
            message.obj = this.cmdMsg;
            BipService.this.mBipSrvHandler.sendMessage(this.response);
            if (lChannel != null) {
                synchronized (lChannel.mLock) {
                    lChannel.isReceiveDataTRSent = true;
                    if (lChannel.mRxBufferCount == 0) {
                        MtkCatLog.d("[BIP]", "BM-receiveData: notify waiting channel!");
                        lChannel.mLock.notify();
                    }
                }
            } else {
                MtkCatLog.e("[BIP]", "BM-receiveData: null channel.");
            }
            MtkCatLog.d("[BIP]", "BM-receiveData: end to receive data. Result code = " + errCode);
        }
    }
}
