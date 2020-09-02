package com.mediatek.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.BerTlv;
import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.ComprehensionTlv;
import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.cat.IconId;
import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.cat.ResultException;
import com.android.internal.telephony.cat.SetEventListParams;
import com.android.internal.telephony.cat.TextMessage;
import com.android.internal.telephony.cat.ValueParser;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.util.Iterator;
import java.util.List;

class BipCommandParamsFactory extends Handler {
    static final int LOAD_MULTI_ICONS = 2;
    static final int LOAD_NO_ICON = 0;
    static final int LOAD_SINGLE_ICON = 1;
    static final int MSG_ID_LOAD_ICON_DONE = 1;
    private static BipCommandParamsFactory sInstance = null;
    private BipRilMessageDecoder mCaller = null;
    private CommandParams mCmdParams = null;
    private int mIconLoadState = 0;
    private BipIconLoader mIconLoader;
    int tlvIndex = -1;

    static synchronized BipCommandParamsFactory getInstance(BipRilMessageDecoder caller, IccFileHandler fh) {
        synchronized (BipCommandParamsFactory.class) {
            if (sInstance != null) {
                BipCommandParamsFactory bipCommandParamsFactory = sInstance;
                return bipCommandParamsFactory;
            } else if (fh == null) {
                return null;
            } else {
                BipCommandParamsFactory bipCommandParamsFactory2 = new BipCommandParamsFactory(caller, fh);
                return bipCommandParamsFactory2;
            }
        }
    }

    private BipCommandParamsFactory(BipRilMessageDecoder caller, IccFileHandler fh) {
        this.mCaller = caller;
        this.mIconLoader = BipIconLoader.getInstance(this, fh, this.mCaller.getSlotId());
    }

    private CommandDetails processCommandDetails(List<ComprehensionTlv> ctlvs) throws ResultException {
        ComprehensionTlv ctlvCmdDet;
        if (ctlvs == null || (ctlvCmdDet = searchForTag(ComprehensionTlvTag.COMMAND_DETAILS, ctlvs)) == null) {
            return null;
        }
        try {
            return ValueParser.retrieveCommandDetails(ctlvCmdDet);
        } catch (ResultException e) {
            MtkCatLog.d(this, "Failed to procees command details");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    /* access modifiers changed from: package-private */
    public void make(BerTlv berTlv) {
        boolean cmdPending;
        if (berTlv != null) {
            this.mCmdParams = null;
            this.mIconLoadState = 0;
            if (berTlv.getTag() != 208) {
                MtkCatLog.e(this, "CPF-make: Ununderstood proactive command tag");
                sendCmdParams(ResultCode.CMD_TYPE_NOT_UNDERSTOOD);
                return;
            }
            List<ComprehensionTlv> ctlvs = berTlv.getComprehensionTlvs();
            try {
                CommandDetails cmdDet = processCommandDetails(ctlvs);
                if (cmdDet == null) {
                    MtkCatLog.e(this, "CPF-make: No CommandDetails object");
                    sendCmdParams(ResultCode.CMD_TYPE_NOT_UNDERSTOOD);
                    return;
                }
                AppInterface.CommandType cmdType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
                if (cmdType == null) {
                    MtkCatLog.d(this, "CPF-make: Command type can't be found");
                    this.mCmdParams = new CommandParams(cmdDet);
                    sendCmdParams(ResultCode.BEYOND_TERMINAL_CAPABILITY);
                } else if (!berTlv.isLengthValid()) {
                    this.mCmdParams = new CommandParams(cmdDet);
                    sendCmdParams(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                } else {
                    try {
                        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[cmdType.ordinal()]) {
                            case 1:
                                cmdPending = processSetUpEventList(cmdDet, ctlvs);
                                break;
                            case 2:
                                cmdPending = processOpenChannel(cmdDet, ctlvs);
                                MtkCatLog.d(this, "process OpenChannel");
                                break;
                            case 3:
                                cmdPending = processCloseChannel(cmdDet, ctlvs);
                                MtkCatLog.d(this, "process CloseChannel");
                                break;
                            case 4:
                                cmdPending = processSendData(cmdDet, ctlvs);
                                MtkCatLog.d(this, "process SendData");
                                break;
                            case 5:
                                cmdPending = processReceiveData(cmdDet, ctlvs);
                                MtkCatLog.d(this, "process ReceiveData");
                                break;
                            case 6:
                                cmdPending = processGetChannelStatus(cmdDet, ctlvs);
                                MtkCatLog.d(this, "process GetChannelStatus");
                                break;
                            default:
                                this.mCmdParams = new CommandParams(cmdDet);
                                MtkCatLog.d(this, "CPF-make: default case");
                                sendCmdParams(ResultCode.BEYOND_TERMINAL_CAPABILITY);
                                return;
                        }
                        if (!cmdPending) {
                            sendCmdParams(ResultCode.OK);
                        }
                    } catch (ResultException e) {
                        MtkCatLog.d(this, "make: caught ResultException e=" + e);
                        this.mCmdParams = new CommandParams(cmdDet);
                        sendCmdParams(e.result());
                    }
                }
            } catch (ResultException e2) {
                MtkCatLog.e(this, "CPF-make: Except to procees command details : " + e2.result());
                sendCmdParams(e2.result());
            }
        }
    }

    /* renamed from: com.mediatek.internal.telephony.cat.BipCommandParamsFactory$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType = new int[AppInterface.CommandType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SET_UP_EVENT_LIST.ordinal()] = 1;
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
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_DATA.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.RECEIVE_DATA.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.GET_CHANNEL_STATUS.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    public void handleMessage(Message msg) {
        if (msg.what == 1) {
            sendCmdParams(setIcons(msg.obj));
        }
    }

    private ResultCode setIcons(Object data) {
        if (data == null) {
            return ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
        }
        int i = this.mIconLoadState;
        if (i != 1 && i == 2) {
            Bitmap[] icons = (Bitmap[]) data;
            for (Bitmap bitmap : icons) {
            }
        }
        return ResultCode.OK;
    }

    private void sendCmdParams(ResultCode resCode) {
        this.mCaller.sendMsgParamsDecoded(resCode, this.mCmdParams);
    }

    private ComprehensionTlv searchForTag(ComprehensionTlvTag tag, List<ComprehensionTlv> ctlvs) {
        return searchForNextTag(tag, ctlvs.iterator());
    }

    private ComprehensionTlv searchForNextTag(ComprehensionTlvTag tag, Iterator<ComprehensionTlv> iter) {
        int tagValue = tag.value();
        while (iter.hasNext()) {
            ComprehensionTlv ctlv = iter.next();
            if (ctlv.getTag() == tagValue) {
                return ctlv;
            }
        }
        return null;
    }

    private void resetTlvIndex() {
        this.tlvIndex = -1;
    }

    private ComprehensionTlv searchForNextTagAndIndex(ComprehensionTlvTag tag, Iterator<ComprehensionTlv> iter) {
        if (tag == null || iter == null) {
            MtkCatLog.d(this, "CPF-searchForNextTagAndIndex: Invalid params");
            return null;
        }
        int tagValue = tag.value();
        while (iter.hasNext()) {
            this.tlvIndex++;
            ComprehensionTlv ctlv = iter.next();
            if (ctlv.getTag() == tagValue) {
                return ctlv;
            }
        }
        return null;
    }

    private ComprehensionTlv searchForTagAndIndex(ComprehensionTlvTag tag, List<ComprehensionTlv> ctlvs) {
        resetTlvIndex();
        return searchForNextTagAndIndex(tag, ctlvs.iterator());
    }

    private boolean processSetUpEventList(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) {
        MtkCatLog.d(this, "process SetUpEventList");
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.EVENT_LIST, ctlvs);
        if (ctlv == null) {
            return false;
        }
        try {
            byte[] rawValue = ctlv.getRawValue();
            int valueIndex = ctlv.getValueIndex();
            int valueLen = ctlv.getLength();
            int[] eventList = new int[valueLen];
            int i = 0;
            while (valueLen > 0) {
                int eventValue = rawValue[valueIndex] & PplMessageManager.Type.INVALID;
                valueIndex++;
                valueLen--;
                if (eventValue == 4 || eventValue == 5 || eventValue == 7 || eventValue == 8 || eventValue == 15) {
                    eventList[i] = eventValue;
                    i++;
                }
            }
            this.mCmdParams = new SetEventListParams(cmdDet, eventList);
            return false;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e(this, " IndexOutofBoundException in processSetUpEventList");
            return false;
        }
    }

    private boolean processOpenChannel(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        boolean z;
        TextMessage confirmText;
        IconId confirmIcon;
        String accessPointName;
        BearerDesc bearerDesc;
        String userLogin;
        String userPwd;
        int indexTransportProtocol;
        OtherAddress dataDestinationAddress;
        OtherAddress localAddress;
        int bufferSize;
        TransportProtocol transportProtocol;
        TextMessage confirmText2;
        BearerDesc bearerDesc2;
        int bufferSize2;
        MtkCatLog.d(this, "enter: process OpenChannel");
        if ((cmdDet.commandQualifier & 1) == 1) {
            z = false;
        } else {
            z = true;
        }
        boolean z2 = (cmdDet.commandQualifier & 2) != 0;
        OtherAddress localAddress2 = null;
        TransportProtocol transportProtocol2 = null;
        OtherAddress dataDestinationAddress2 = null;
        TextMessage confirmText3 = new TextMessage();
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            confirmText = confirmText3;
            confirmText.text = MtkValueParser.retrieveAlphaId(ctlv);
        } else {
            confirmText = confirmText3;
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv2 != null) {
            this.mIconLoadState = 1;
            confirmIcon = ValueParser.retrieveIconId(ctlv2);
            confirmText.iconSelfExplanatory = confirmIcon.selfExplanatory;
        } else {
            confirmIcon = null;
        }
        ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.BEARER_DESCRIPTION, ctlvs);
        if (ctlv3 != null) {
            BearerDesc bearerDesc3 = BipValueParser.retrieveBearerDesc(ctlv3);
            MtkCatLog.d("[BIP]", "bearerDesc bearer type: " + bearerDesc3.bearerType);
            if (bearerDesc3 instanceof GPRSBearerDesc) {
                MtkCatLog.d("[BIP]", "\nprecedence: " + ((GPRSBearerDesc) bearerDesc3).precedence + "\ndelay: " + ((GPRSBearerDesc) bearerDesc3).delay + "\nreliability: " + ((GPRSBearerDesc) bearerDesc3).reliability + "\npeak: " + ((GPRSBearerDesc) bearerDesc3).peak + "\nmean: " + ((GPRSBearerDesc) bearerDesc3).mean + "\npdp type: " + ((GPRSBearerDesc) bearerDesc3).pdpType);
                accessPointName = null;
            } else if (bearerDesc3 instanceof UTranBearerDesc) {
                StringBuilder sb = new StringBuilder();
                sb.append("\ntrafficClass: ");
                sb.append(((UTranBearerDesc) bearerDesc3).trafficClass);
                sb.append("\nmaxBitRateUL_High: ");
                sb.append(((UTranBearerDesc) bearerDesc3).maxBitRateUL_High);
                sb.append("\nmaxBitRateUL_Low: ");
                accessPointName = null;
                sb.append(((UTranBearerDesc) bearerDesc3).maxBitRateUL_Low);
                sb.append("\nmaxBitRateDL_High: ");
                sb.append(((UTranBearerDesc) bearerDesc3).maxBitRateDL_High);
                sb.append("\nmaxBitRateUL_Low: ");
                sb.append(((UTranBearerDesc) bearerDesc3).maxBitRateDL_Low);
                sb.append("\nguarBitRateUL_High: ");
                sb.append(((UTranBearerDesc) bearerDesc3).guarBitRateUL_High);
                sb.append("\nguarBitRateUL_Low: ");
                sb.append(((UTranBearerDesc) bearerDesc3).guarBitRateUL_Low);
                sb.append("\nguarBitRateDL_High: ");
                sb.append(((UTranBearerDesc) bearerDesc3).guarBitRateDL_High);
                sb.append("\nguarBitRateDL_Low: ");
                sb.append(((UTranBearerDesc) bearerDesc3).guarBitRateDL_Low);
                sb.append("\ndeliveryOrder: ");
                sb.append(((UTranBearerDesc) bearerDesc3).deliveryOrder);
                sb.append("\nmaxSduSize: ");
                sb.append(((UTranBearerDesc) bearerDesc3).maxSduSize);
                sb.append("\nsduErrorRatio: ");
                sb.append(((UTranBearerDesc) bearerDesc3).sduErrorRatio);
                sb.append("\nresidualBitErrorRadio: ");
                sb.append(((UTranBearerDesc) bearerDesc3).residualBitErrorRadio);
                sb.append("\ndeliveryOfErroneousSdus: ");
                sb.append(((UTranBearerDesc) bearerDesc3).deliveryOfErroneousSdus);
                sb.append("\ntransferDelay: ");
                sb.append(((UTranBearerDesc) bearerDesc3).transferDelay);
                sb.append("\ntrafficHandlingPriority: ");
                sb.append(((UTranBearerDesc) bearerDesc3).trafficHandlingPriority);
                sb.append("\npdp type: ");
                sb.append(((UTranBearerDesc) bearerDesc3).pdpType);
                MtkCatLog.d("[BIP]", sb.toString());
            } else {
                accessPointName = null;
                if (bearerDesc3 instanceof EUTranBearerDesc) {
                    MtkCatLog.d("[BIP]", "\nQCI: " + ((EUTranBearerDesc) bearerDesc3).QCI + "\nmaxBitRateU: " + ((EUTranBearerDesc) bearerDesc3).maxBitRateU + "\nmaxBitRateD: " + ((EUTranBearerDesc) bearerDesc3).maxBitRateD + "\nguarBitRateU: " + ((EUTranBearerDesc) bearerDesc3).guarBitRateU + "\nguarBitRateD: " + ((EUTranBearerDesc) bearerDesc3).guarBitRateD + "\nmaxBitRateUEx: " + ((EUTranBearerDesc) bearerDesc3).maxBitRateUEx + "\nmaxBitRateDEx: " + ((EUTranBearerDesc) bearerDesc3).maxBitRateDEx + "\nguarBitRateUEx: " + ((EUTranBearerDesc) bearerDesc3).guarBitRateUEx + "\nguarBitRateDEx: " + ((EUTranBearerDesc) bearerDesc3).guarBitRateDEx + "\npdn Type: " + ((EUTranBearerDesc) bearerDesc3).pdnType);
                } else if (!(bearerDesc3 instanceof DefaultBearerDesc)) {
                    MtkCatLog.d("[BIP]", "Not support bearerDesc");
                }
            }
            bearerDesc = bearerDesc3;
        } else {
            accessPointName = null;
            MtkCatLog.d("[BIP]", "May Need BearerDescription object");
            bearerDesc = null;
        }
        ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.BUFFER_SIZE, ctlvs);
        if (ctlv4 != null) {
            int bufferSize3 = BipValueParser.retrieveBufferSize(ctlv4);
            MtkCatLog.d("[BIP]", "buffer size: " + bufferSize3);
            ComprehensionTlv ctlv5 = searchForTag(ComprehensionTlvTag.NETWORK_ACCESS_NAME, ctlvs);
            if (ctlv5 != null) {
                String accessPointName2 = BipValueParser.retrieveNetworkAccessName(ctlv5);
                MtkCatLog.d("[BIP]", "access point name: " + accessPointName2);
                accessPointName = accessPointName2;
            }
            Iterator<ComprehensionTlv> iter = ctlvs.iterator();
            ComprehensionTlv ctlv6 = searchForNextTag(ComprehensionTlvTag.TEXT_STRING, iter);
            if (ctlv6 != null) {
                String userLogin2 = ValueParser.retrieveTextString(ctlv6);
                MtkCatLog.d("[BIP]", "user login: " + userLogin2);
                userLogin = userLogin2;
            } else {
                userLogin = null;
            }
            ComprehensionTlv ctlv7 = searchForNextTag(ComprehensionTlvTag.TEXT_STRING, iter);
            if (ctlv7 != null) {
                String userPwd2 = ValueParser.retrieveTextString(ctlv7);
                MtkCatLog.d("[BIP]", "user password: " + userPwd2);
                userPwd = userPwd2;
            } else {
                userPwd = null;
            }
            ComprehensionTlv ctlv8 = searchForTagAndIndex(ComprehensionTlvTag.SIM_ME_INTERFACE_TRANSPORT_LEVEL, ctlvs);
            if (ctlv8 != null) {
                int indexTransportProtocol2 = this.tlvIndex;
                MtkCatLog.d("[BIP]", "CPF-processOpenChannel: indexTransportProtocol = " + indexTransportProtocol2);
                TransportProtocol transportProtocol3 = BipValueParser.retrieveTransportProtocol(ctlv8);
                MtkCatLog.d("[BIP]", "CPF-processOpenChannel: transport protocol(type/port): " + transportProtocol3.protocolType + "/" + transportProtocol3.portNumber);
                if ((1 == transportProtocol3.protocolType || 2 == transportProtocol3.protocolType) && bearerDesc == null) {
                    MtkCatLog.d("[BIP]", "Need BearerDescription object");
                    throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
                }
                transportProtocol2 = transportProtocol3;
                indexTransportProtocol = indexTransportProtocol2;
            } else if (bearerDesc != null) {
                indexTransportProtocol = -1;
            } else {
                MtkCatLog.d("[BIP]", "BearerDescription & transportProtocol object are null");
                throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
            }
            if (transportProtocol2 != null) {
                MtkCatLog.d("[BIP]", "CPF-processOpenChannel: transport protocol is existed");
                Iterator<ComprehensionTlv> iter2 = ctlvs.iterator();
                resetTlvIndex();
                ComprehensionTlv ctlv9 = searchForNextTagAndIndex(ComprehensionTlvTag.OTHER_ADDRESS, iter2);
                if (ctlv9 != null) {
                    int i = this.tlvIndex;
                    if (i < indexTransportProtocol) {
                        StringBuilder sb2 = new StringBuilder();
                        bufferSize = bufferSize3;
                        sb2.append("CPF-processOpenChannel: get local address, index is ");
                        sb2.append(this.tlvIndex);
                        MtkCatLog.d("[BIP]", sb2.toString());
                        localAddress2 = BipValueParser.retrieveOtherAddress(ctlv9);
                        ctlv9 = searchForNextTagAndIndex(ComprehensionTlvTag.OTHER_ADDRESS, iter2);
                        if (ctlv9 == null || this.tlvIndex <= indexTransportProtocol) {
                            MtkCatLog.d("[BIP]", "CPF-processOpenChannel: missing dest address " + this.tlvIndex + "/" + indexTransportProtocol);
                            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
                        }
                        MtkCatLog.d("[BIP]", "CPF-processOpenChannel: get dest address, index is " + this.tlvIndex);
                        dataDestinationAddress2 = BipValueParser.retrieveOtherAddress(ctlv9);
                    } else {
                        bufferSize = bufferSize3;
                        if (i > indexTransportProtocol) {
                            MtkCatLog.d("[BIP]", "CPF-processOpenChannel: get dest address, but no local address");
                            dataDestinationAddress2 = BipValueParser.retrieveOtherAddress(ctlv9);
                        } else {
                            MtkCatLog.d("[BIP]", "CPF-processOpenChannel: Incorrect index");
                        }
                    }
                } else {
                    bufferSize = bufferSize3;
                    MtkCatLog.d("[BIP]", "CPF-processOpenChannel: No other address object");
                }
                if (dataDestinationAddress2 == null && (2 == transportProtocol2.protocolType || 1 == transportProtocol2.protocolType)) {
                    MtkCatLog.d("[BIP]", "BM-openChannel: dataDestinationAddress is null.");
                    throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
                }
                localAddress = localAddress2;
                dataDestinationAddress = dataDestinationAddress2;
            } else {
                bufferSize = bufferSize3;
                localAddress = null;
                dataDestinationAddress = null;
            }
            if (bearerDesc == null) {
                confirmText2 = confirmText;
                transportProtocol = transportProtocol2;
                bufferSize2 = bufferSize;
                bearerDesc2 = bearerDesc;
            } else if (bearerDesc.bearerType == 2 || bearerDesc.bearerType == 3 || bearerDesc.bearerType == 9 || bearerDesc.bearerType == 11) {
                bufferSize2 = bufferSize;
                bearerDesc2 = bearerDesc;
                confirmText2 = confirmText;
                transportProtocol = transportProtocol2;
                this.mCmdParams = new OpenChannelParams(cmdDet, bearerDesc, bufferSize2, localAddress, transportProtocol2, dataDestinationAddress, accessPointName, userLogin, userPwd, confirmText2);
            } else {
                MtkCatLog.d("[BIP]", "Unsupport bearerType: " + bearerDesc.bearerType);
                confirmText2 = confirmText;
                transportProtocol = transportProtocol2;
                bufferSize2 = bufferSize;
                bearerDesc2 = bearerDesc;
            }
            this.mCmdParams = new OpenChannelParams(cmdDet, bearerDesc2, bufferSize2, localAddress, transportProtocol, dataDestinationAddress, accessPointName, userLogin, userPwd, confirmText2);
            if (confirmIcon == null) {
                return false;
            }
            this.mIconLoadState = 1;
            this.mIconLoader.loadIcon(confirmIcon.recordNumber, obtainMessage(1));
            return true;
        }
        MtkCatLog.d("[BIP]", "Need BufferSize object");
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    private boolean processCloseChannel(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "enter: process CloseChannel");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        int channelId = 0;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            textMsg.text = MtkValueParser.retrieveAlphaId(ctlv);
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv2 != null) {
            iconId = ValueParser.retrieveIconId(ctlv2);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.DEVICE_IDENTITIES, ctlvs);
        if (ctlv3 != null) {
            channelId = ctlv3.getRawValue()[ctlv3.getValueIndex() + 1] & 15;
            MtkCatLog.d("[BIP]", "To close channel " + channelId);
        }
        this.mCmdParams = new CloseChannelParams(cmdDet, channelId, textMsg, 1 == (cmdDet.commandQualifier & 1));
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    private boolean processReceiveData(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "enter: process ReceiveData");
        int channelDataLength = 0;
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        int channelId = 0;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.CHANNEL_DATA_LENGTH, ctlvs);
        if (ctlv != null) {
            channelDataLength = BipValueParser.retrieveChannelDataLength(ctlv);
            MtkCatLog.d("[BIP]", "Channel data length: " + channelDataLength);
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv2 != null) {
            textMsg.text = MtkValueParser.retrieveAlphaId(ctlv2);
        }
        ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv3 != null) {
            iconId = ValueParser.retrieveIconId(ctlv3);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.DEVICE_IDENTITIES, ctlvs);
        if (ctlv4 != null) {
            channelId = ctlv4.getRawValue()[ctlv4.getValueIndex() + 1] & 15;
            MtkCatLog.d("[BIP]", "To Receive data: " + channelId);
        }
        this.mCmdParams = new ReceiveDataParams(cmdDet, channelDataLength, channelId, textMsg);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    private boolean processSendData(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        IconId iconId;
        int channelId;
        MtkCatLog.d(this, "enter: process SendData");
        byte[] channelData = null;
        TextMessage textMsg = new TextMessage();
        int sendMode = (cmdDet.commandQualifier & 1) == 1 ? 1 : 0;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.CHANNEL_DATA, ctlvs);
        if (ctlv != null) {
            channelData = BipValueParser.retrieveChannelData(ctlv);
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv2 != null) {
            textMsg.text = MtkValueParser.retrieveAlphaId(ctlv2);
        }
        ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv3 != null) {
            IconId iconId2 = ValueParser.retrieveIconId(ctlv3);
            textMsg.iconSelfExplanatory = iconId2.selfExplanatory;
            iconId = iconId2;
        } else {
            iconId = null;
        }
        ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.DEVICE_IDENTITIES, ctlvs);
        if (ctlv4 != null) {
            int channelId2 = ctlv4.getRawValue()[ctlv4.getValueIndex() + 1] & 15;
            MtkCatLog.d("[BIP]", "To send data: " + channelId2);
            channelId = channelId2;
        } else {
            channelId = 0;
        }
        this.mCmdParams = new SendDataParams(cmdDet, channelData, channelId, textMsg, sendMode);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    private boolean processGetChannelStatus(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "enter: process GetChannelStatus");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            textMsg.text = MtkValueParser.retrieveAlphaId(ctlv);
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv2 != null) {
            iconId = ValueParser.retrieveIconId(ctlv2);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        this.mCmdParams = new GetChannelStatusParams(cmdDet, textMsg);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    public void dispose() {
        this.mIconLoader.dispose();
        this.mIconLoader = null;
        this.mCmdParams = null;
        this.mCaller = null;
        synchronized (BipCommandParamsFactory.class) {
            sInstance = null;
        }
    }
}
