package com.mediatek.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.SetEventListParams;
import com.android.internal.telephony.cat.TextMessage;
import java.util.List;

public class BipCmdMessage implements Parcelable {
    public static final Parcelable.Creator<BipCmdMessage> CREATOR = new Parcelable.Creator<BipCmdMessage>() {
        /* class com.mediatek.internal.telephony.cat.BipCmdMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BipCmdMessage createFromParcel(Parcel in) {
            return new BipCmdMessage(in);
        }

        @Override // android.os.Parcelable.Creator
        public BipCmdMessage[] newArray(int size) {
            return new BipCmdMessage[size];
        }
    };
    public String mApn = null;
    public BearerDesc mBearerDesc = null;
    public int mBufferSize = 0;
    public byte[] mChannelData = null;
    public int mChannelDataLength = 0;
    public ChannelStatus mChannelStatusData = null;
    public List<ChannelStatus> mChannelStatusList = null;
    public boolean mCloseBackToTcpListen = false;
    public int mCloseCid = 0;
    CommandDetails mCmdDet;
    public OtherAddress mDataDestinationAddress = null;
    public String mDestAddress = null;
    public DnsServerAddress mDnsServerAddress = null;
    public int mInfoType = 0;
    public OtherAddress mLocalAddress = null;
    public String mLogin = null;
    public String mPwd = null;
    public int mReceiveDataCid = 0;
    public int mRemainingDataLength = 0;
    public int mSendDataCid = 0;
    public int mSendMode = 0;
    private SetupEventListSettings mSetupEventListSettings = null;
    private TextMessage mTextMsg;
    public TransportProtocol mTransportProtocol = null;

    public class SetupEventListSettings {
        public int[] eventList;

        public SetupEventListSettings() {
        }
    }

    BipCmdMessage(CommandParams cmdParams) {
        this.mCmdDet = cmdParams.mCmdDet;
        if (getCmdType() == null) {
            MtkCatLog.e("[BIP]", "cmd type is null!");
            return;
        }
        switch (AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()]) {
            case 1:
                this.mTextMsg = ((GetChannelStatusParams) cmdParams).textMsg;
                return;
            case 2:
                this.mBearerDesc = ((OpenChannelParams) cmdParams).bearerDesc;
                this.mBufferSize = ((OpenChannelParams) cmdParams).bufferSize;
                this.mLocalAddress = ((OpenChannelParams) cmdParams).localAddress;
                this.mTransportProtocol = ((OpenChannelParams) cmdParams).transportProtocol;
                this.mDataDestinationAddress = ((OpenChannelParams) cmdParams).dataDestinationAddress;
                this.mTextMsg = ((OpenChannelParams) cmdParams).textMsg;
                BearerDesc bearerDesc = this.mBearerDesc;
                if (bearerDesc == null) {
                    MtkCatLog.d("[BIP]", "Invalid BearerDesc object");
                    return;
                } else if (bearerDesc.bearerType == 2 || this.mBearerDesc.bearerType == 3 || this.mBearerDesc.bearerType == 9 || this.mBearerDesc.bearerType == 11) {
                    this.mApn = ((OpenChannelParams) cmdParams).gprsParams.accessPointName;
                    this.mLogin = ((OpenChannelParams) cmdParams).gprsParams.userLogin;
                    this.mPwd = ((OpenChannelParams) cmdParams).gprsParams.userPwd;
                    return;
                } else {
                    return;
                }
            case 3:
                this.mTextMsg = ((CloseChannelParams) cmdParams).textMsg;
                this.mCloseCid = ((CloseChannelParams) cmdParams).mCloseCid;
                this.mCloseBackToTcpListen = ((CloseChannelParams) cmdParams).mBackToTcpListen;
                return;
            case 4:
                this.mTextMsg = ((ReceiveDataParams) cmdParams).textMsg;
                this.mChannelDataLength = ((ReceiveDataParams) cmdParams).channelDataLength;
                this.mReceiveDataCid = ((ReceiveDataParams) cmdParams).mReceiveDataCid;
                return;
            case 5:
                this.mTextMsg = ((SendDataParams) cmdParams).textMsg;
                this.mChannelData = ((SendDataParams) cmdParams).channelData;
                this.mSendDataCid = ((SendDataParams) cmdParams).mSendDataCid;
                this.mSendMode = ((SendDataParams) cmdParams).mSendMode;
                return;
            case 6:
                this.mSetupEventListSettings = new SetupEventListSettings();
                this.mSetupEventListSettings.eventList = ((SetEventListParams) cmdParams).mEventInfo;
                return;
            default:
                return;
        }
    }

    /* renamed from: com.mediatek.internal.telephony.cat.BipCmdMessage$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType = new int[AppInterface.CommandType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.GET_CHANNEL_STATUS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.OPEN_CHANNEL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.CLOSE_CHANNEL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.RECEIVE_DATA.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_DATA.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SET_UP_EVENT_LIST.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    public BipCmdMessage(Parcel in) {
        this.mCmdDet = in.readParcelable(null);
        this.mTextMsg = in.readParcelable(null);
        if (getCmdType() == null) {
            MtkCatLog.e("[BIP]", "cmd type is null");
            return;
        }
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
        if (i == 2) {
            this.mBearerDesc = (BearerDesc) in.readParcelable(null);
        } else if (i == 6) {
            this.mSetupEventListSettings = new SetupEventListSettings();
            int length = in.readInt();
            this.mSetupEventListSettings.eventList = new int[length];
            for (int i2 = 0; i2 < length; i2++) {
                this.mSetupEventListSettings.eventList[i2] = in.readInt();
            }
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCmdDet, 0);
        dest.writeParcelable(this.mTextMsg, 0);
        if (getCmdType() == null) {
            MtkCatLog.e("[BIP]", "cmd type is null");
            return;
        }
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
        if (i == 2) {
            dest.writeParcelable(this.mBearerDesc, 0);
        } else if (i == 6) {
            dest.writeIntArray(this.mSetupEventListSettings.eventList);
        }
    }

    public int describeContents() {
        return 0;
    }

    public int getCmdQualifier() {
        return this.mCmdDet.commandQualifier;
    }

    public AppInterface.CommandType getCmdType() {
        return AppInterface.CommandType.fromInt(this.mCmdDet.typeOfCommand);
    }

    public BearerDesc getBearerDesc() {
        return this.mBearerDesc;
    }
}
