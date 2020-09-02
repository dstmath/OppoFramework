package com.mediatek.internal.telephony.cat;

import android.content.Context;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.CallSetupParams;
import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParamsFactory;
import com.android.internal.telephony.cat.ComprehensionTlv;
import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.cat.DisplayTextParams;
import com.android.internal.telephony.cat.GetInputParams;
import com.android.internal.telephony.cat.IconId;
import com.android.internal.telephony.cat.Input;
import com.android.internal.telephony.cat.Item;
import com.android.internal.telephony.cat.ItemsIconId;
import com.android.internal.telephony.cat.LaunchBrowserMode;
import com.android.internal.telephony.cat.LaunchBrowserParams;
import com.android.internal.telephony.cat.PlayToneParams;
import com.android.internal.telephony.cat.PresentationType;
import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.cat.ResultException;
import com.android.internal.telephony.cat.RilMessageDecoder;
import com.android.internal.telephony.cat.SelectItemParams;
import com.android.internal.telephony.cat.SetEventListParams;
import com.android.internal.telephony.cat.TextMessage;
import com.android.internal.telephony.cat.Tone;
import com.android.internal.telephony.cat.ValueParser;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.util.Iterator;
import java.util.List;

public class MtkCommandParamsFactory extends CommandParamsFactory {
    public static final int BATTERY_STATE = 10;
    private Context mContext;
    int tlvIndex = -1;

    public MtkCommandParamsFactory(RilMessageDecoder caller, IccFileHandler fh) {
        super(caller, fh);
        this.mContext = ((MtkCatService) caller.mCaller).getContext();
    }

    /* access modifiers changed from: protected */
    public void sendCmdParams(ResultCode resCode) {
        if (this.mCaller != null) {
            this.mCaller.sendMsgParamsDecoded(resCode, this.mCmdParams);
        } else {
            MtkCatLog.e(this, "mCaller is null!!!");
        }
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

    /* access modifiers changed from: protected */
    public boolean processDisplayText(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "process DisplayText");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveTextString(ctlv);
        }
        if (textMsg.text != null) {
            if (searchForTag(ComprehensionTlvTag.IMMEDIATE_RESPONSE, ctlvs) != null) {
                textMsg.responseNeeded = false;
            }
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv2 != null) {
                try {
                    iconId = ValueParser.retrieveIconId(ctlv2);
                } catch (ResultException e) {
                    MtkCatLog.e(this, "retrieveIconId ResultException: " + e.result());
                }
                try {
                    textMsg.iconSelfExplanatory = iconId.selfExplanatory;
                } catch (NullPointerException e2) {
                    MtkCatLog.e(this, "iconId is null.");
                }
            }
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
            if (ctlv3 != null) {
                try {
                    textMsg.duration = ValueParser.retrieveDuration(ctlv3);
                } catch (ResultException e3) {
                    MtkCatLog.e(this, "retrieveDuration ResultException: " + e3.result());
                }
            }
            textMsg.isHighPriority = (cmdDet.commandQualifier & 1) != 0;
            textMsg.userClear = (cmdDet.commandQualifier & 128) != 0;
            this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
            if (iconId == null) {
                return false;
            }
            this.mloadIcon = true;
            this.mIconLoadState = 1;
            this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
            return true;
        }
        throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
    }

    /* access modifiers changed from: protected */
    public boolean processGetInkey(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "process GetInkey");
        Input input = new Input();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            input.text = ValueParser.retrieveTextString(ctlv);
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv2 != null) {
                try {
                    iconId = ValueParser.retrieveIconId(ctlv2);
                } catch (ResultException e) {
                    MtkCatLog.e(this, "retrieveIconId ResultException: " + e.result());
                }
                try {
                    input.iconSelfExplanatory = iconId.selfExplanatory;
                } catch (NullPointerException e2) {
                    MtkCatLog.e(this, "iconId is null.");
                }
            }
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
            if (ctlv3 != null) {
                try {
                    input.duration = ValueParser.retrieveDuration(ctlv3);
                } catch (ResultException e3) {
                    MtkCatLog.e(this, "retrieveDuration ResultException: " + e3.result());
                }
            }
            input.minLen = 1;
            input.maxLen = 1;
            input.digitOnly = (cmdDet.commandQualifier & 1) == 0;
            input.ucs2 = (cmdDet.commandQualifier & 2) != 0;
            input.yesNo = (cmdDet.commandQualifier & 4) != 0;
            input.helpAvailable = (cmdDet.commandQualifier & 128) != 0;
            input.echo = true;
            this.mCmdParams = new GetInputParams(cmdDet, input);
            if (iconId == null) {
                return false;
            }
            this.mloadIcon = true;
            this.mIconLoadState = 1;
            this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
            return true;
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    /* access modifiers changed from: protected */
    public boolean processGetInput(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "process GetInput");
        Input input = new Input();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            input.text = ValueParser.retrieveTextString(ctlv);
            ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.RESPONSE_LENGTH, ctlvs);
            if (ctlv2 != null) {
                try {
                    byte[] rawValue = ctlv2.getRawValue();
                    int valueIndex = ctlv2.getValueIndex();
                    input.minLen = rawValue[valueIndex] & PplMessageManager.Type.INVALID;
                    if (input.minLen > 239) {
                        input.minLen = 239;
                    }
                    input.maxLen = rawValue[valueIndex + 1] & PplMessageManager.Type.INVALID;
                    if (input.maxLen > 239) {
                        input.maxLen = 239;
                    }
                    ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.DEFAULT_TEXT, ctlvs);
                    if (ctlv3 != null) {
                        try {
                            input.defaultText = ValueParser.retrieveTextString(ctlv3);
                        } catch (ResultException e) {
                            MtkCatLog.e(this, "retrieveTextString ResultException: " + e.result());
                        }
                    }
                    ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
                    if (ctlv4 != null) {
                        try {
                            iconId = ValueParser.retrieveIconId(ctlv4);
                        } catch (ResultException e2) {
                            MtkCatLog.e(this, "retrieveIconId ResultException: " + e2.result());
                        }
                        try {
                            input.iconSelfExplanatory = iconId.selfExplanatory;
                        } catch (NullPointerException e3) {
                            MtkCatLog.e(this, "iconId is null.");
                        }
                    }
                    ComprehensionTlv ctlv5 = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
                    if (ctlv5 != null) {
                        try {
                            input.duration = ValueParser.retrieveDuration(ctlv5);
                        } catch (ResultException e4) {
                            MtkCatLog.e(this, "retrieveDuration ResultException: " + e4.result());
                        }
                    }
                    input.digitOnly = (cmdDet.commandQualifier & 1) == 0;
                    input.ucs2 = (cmdDet.commandQualifier & 2) != 0;
                    input.echo = (cmdDet.commandQualifier & 4) == 0;
                    input.packed = (cmdDet.commandQualifier & 8) != 0;
                    input.helpAvailable = (cmdDet.commandQualifier & 128) != 0;
                    if (input.ucs2 && input.maxLen > 118) {
                        MtkCatLog.d(this, "UCS2: received maxLen = " + input.maxLen + ", truncating to " + 118);
                        input.maxLen = 118;
                    } else if (!input.packed && input.maxLen > 239) {
                        MtkCatLog.d(this, "GSM 7Bit Default: received maxLen = " + input.maxLen + ", truncating to " + 239);
                        input.maxLen = 239;
                    }
                    this.mCmdParams = new GetInputParams(cmdDet, input);
                    if (iconId == null) {
                        return false;
                    }
                    this.mloadIcon = true;
                    this.mIconLoadState = 1;
                    this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
                    return true;
                } catch (IndexOutOfBoundsException e5) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            } else {
                throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
            }
        } else {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
    }

    /* access modifiers changed from: protected */
    public boolean processSelectItem(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "process SelectItem");
        MtkMenu menu = new MtkMenu();
        IconId titleIconId = null;
        ItemsIconId itemsIconId = null;
        Iterator<ComprehensionTlv> iter = ctlvs.iterator();
        AppInterface.CommandType cmdType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            try {
                menu.title = MtkValueParser.retrieveAlphaId(ctlv);
            } catch (ResultException e) {
                MtkCatLog.e(this, "retrieveAlphaId ResultException: " + e.result());
            }
            MtkCatLog.d(this, "add AlphaId: " + menu.title);
        } else if (cmdType == AppInterface.CommandType.SET_UP_MENU) {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        while (true) {
            ComprehensionTlv ctlv2 = searchForNextTag(ComprehensionTlvTag.ITEM, iter);
            if (ctlv2 == null) {
                break;
            }
            Item item = MtkValueParser.retrieveItem(ctlv2);
            StringBuilder sb = new StringBuilder();
            sb.append("add menu item: ");
            sb.append(item == null ? "" : item.toString());
            MtkCatLog.d(this, sb.toString());
            menu.items.add(item);
        }
        if (menu.items.size() != 0) {
            ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.ITEM_ID, ctlvs);
            if (ctlv3 != null) {
                try {
                    menu.defaultItem = ValueParser.retrieveItemId(ctlv3) - 1;
                } catch (ResultException e2) {
                    MtkCatLog.e(this, "retrieveItemId ResultException: " + e2.result());
                }
                MtkCatLog.d(this, "default item: " + menu.defaultItem);
            }
            ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv4 != null) {
                this.mIconLoadState = 1;
                try {
                    titleIconId = ValueParser.retrieveIconId(ctlv4);
                } catch (ResultException e3) {
                    MtkCatLog.e(this, "retrieveIconId ResultException: " + e3.result());
                }
                try {
                    menu.titleIconSelfExplanatory = titleIconId.selfExplanatory;
                } catch (NullPointerException e4) {
                    MtkCatLog.e(this, "titleIconId is null.");
                }
            }
            ComprehensionTlv ctlv5 = searchForTag(ComprehensionTlvTag.ITEM_ICON_ID_LIST, ctlvs);
            if (ctlv5 != null) {
                this.mIconLoadState = 2;
                try {
                    itemsIconId = ValueParser.retrieveItemsIconId(ctlv5);
                } catch (ResultException e5) {
                    MtkCatLog.e(this, "retrieveItemsIconId ResultException: " + e5.result());
                }
                try {
                    menu.itemsIconSelfExplanatory = itemsIconId.selfExplanatory;
                } catch (NullPointerException e6) {
                    MtkCatLog.e(this, "itemsIconId is null.");
                }
            }
            if ((cmdDet.commandQualifier & 1) != 0) {
                if ((cmdDet.commandQualifier & 2) == 0) {
                    menu.presentationType = PresentationType.DATA_VALUES;
                } else {
                    menu.presentationType = PresentationType.NAVIGATION_OPTIONS;
                }
            }
            menu.softKeyPreferred = (cmdDet.commandQualifier & 4) != 0;
            menu.helpAvailable = (cmdDet.commandQualifier & 128) != 0;
            this.mCmdParams = new SelectItemParams(cmdDet, menu, titleIconId != null);
            int i = this.mIconLoadState;
            if (i == 0) {
                return false;
            }
            if (i != 1) {
                if (i == 2) {
                    if (itemsIconId == null) {
                        return false;
                    }
                    int[] recordNumbers = itemsIconId.recordNumbers;
                    if (titleIconId != null) {
                        recordNumbers = new int[(itemsIconId.recordNumbers.length + 1)];
                        recordNumbers[0] = titleIconId.recordNumber;
                        System.arraycopy(itemsIconId.recordNumbers, 0, recordNumbers, 1, itemsIconId.recordNumbers.length);
                    }
                    this.mloadIcon = true;
                    this.mIconLoader.loadIcons(recordNumbers, obtainMessage(1));
                }
            } else if (titleIconId == null || titleIconId.recordNumber <= 0) {
                return false;
            } else {
                this.mloadIcon = true;
                this.mIconLoader.loadIcon(titleIconId.recordNumber, obtainMessage(1));
            }
            return true;
        }
        MtkCatLog.d(this, "no menu item");
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    /* access modifiers changed from: protected */
    public boolean processEventNotify(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "process EventNotify");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            textMsg.text = MtkValueParser.retrieveAlphaId(ctlv);
        } else {
            textMsg.text = null;
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv2 != null) {
            iconId = ValueParser.retrieveIconId(ctlv2);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        textMsg.responseNeeded = false;
        this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
        if (iconId == null) {
            return false;
        }
        this.mloadIcon = true;
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v0, resolved type: int[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v2, resolved type: int} */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public boolean processSetUpEventList(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) {
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
            int index = 0;
            while (index < valueLen) {
                eventList[index] = rawValue[valueIndex];
                MtkCatLog.v(this, "CPF-processSetUpEventList: eventList[" + index + "] = " + eventList[index]);
                index++;
                valueIndex++;
            }
            this.mCmdParams = new SetEventListParams(cmdDet, eventList);
            return false;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e(this, " IndexOutofBoundException in processSetUpEventList");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processLaunchBrowser(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        LaunchBrowserMode mode;
        MtkCatLog.d(this, "process LaunchBrowser");
        TextMessage confirmMsg = new TextMessage();
        IconId iconId = null;
        String url = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.URL, ctlvs);
        if (ctlv != null) {
            try {
                byte[] rawValue = ctlv.getRawValue();
                int valueIndex = ctlv.getValueIndex();
                int valueLen = ctlv.getLength();
                if (valueLen > 0) {
                    url = GsmAlphabet.gsm8BitUnpackedToString(rawValue, valueIndex, valueLen);
                } else {
                    url = null;
                }
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv2 != null) {
            confirmMsg.text = MtkValueParser.retrieveAlphaId(ctlv2);
        }
        ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv3 != null) {
            iconId = ValueParser.retrieveIconId(ctlv3);
            confirmMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        int i = cmdDet.commandQualifier;
        if (i == 2) {
            mode = LaunchBrowserMode.USE_EXISTING_BROWSER;
        } else if (i != 3) {
            mode = LaunchBrowserMode.LAUNCH_IF_NOT_ALREADY_LAUNCHED;
        } else {
            mode = LaunchBrowserMode.LAUNCH_NEW_BROWSER;
        }
        this.mCmdParams = new LaunchBrowserParams(cmdDet, confirmMsg, url, mode);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x009c  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a6  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00c9 A[RETURN] */
    public boolean processPlayTone(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        ResultException e;
        ComprehensionTlv ctlv;
        IconId iconId;
        MtkCatLog.d(this, "process PlayTone");
        Tone tone = null;
        TextMessage textMsg = new TextMessage();
        ComprehensionTlv ctlv2 = searchForTag(ComprehensionTlvTag.TONE, ctlvs);
        if (ctlv2 != null && ctlv2.getLength() > 0) {
            try {
                tone = Tone.fromInt(ctlv2.getRawValue()[ctlv2.getValueIndex()]);
            } catch (IndexOutOfBoundsException e2) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        ComprehensionTlv ctlv3 = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv3 != null) {
            try {
                textMsg.text = MtkValueParser.retrieveAlphaId(ctlv3);
            } catch (ResultException e3) {
                MtkCatLog.e(this, "retrieveAlphaId ResultException: " + e3.result());
            }
        }
        ComprehensionTlv ctlv4 = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
        if (ctlv4 != null) {
            try {
                e = ValueParser.retrieveDuration(ctlv4);
            } catch (ResultException e4) {
                MtkCatLog.e(this, "retrieveDuration ResultException: " + e4.result());
            }
            ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv == null) {
                IconId iconId2 = ValueParser.retrieveIconId(ctlv);
                textMsg.iconSelfExplanatory = iconId2.selfExplanatory;
                iconId = iconId2;
            } else {
                iconId = null;
            }
            boolean vibrate = (cmdDet.commandQualifier & 1) == 0;
            textMsg.responseNeeded = false;
            this.mCmdParams = new PlayToneParams(cmdDet, textMsg, tone, e, vibrate);
            if (iconId != null) {
                return false;
            }
            this.mIconLoadState = 1;
            this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
            return true;
        }
        e = null;
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv == null) {
        }
        if ((cmdDet.commandQualifier & 1) == 0) {
        }
        textMsg.responseNeeded = false;
        this.mCmdParams = new PlayToneParams(cmdDet, textMsg, tone, e, vibrate);
        if (iconId != null) {
        }
    }

    /* access modifiers changed from: protected */
    public boolean processSetupCall(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        MtkCatLog.d(this, "process SetupCall");
        ctlvs.iterator();
        TextMessage confirmMsg = new TextMessage();
        TextMessage callMsg = new TextMessage();
        IconId confirmIconId = null;
        IconId callIconId = null;
        int addrIndex = getAddrIndex(ctlvs);
        int i = -1;
        if (-1 == addrIndex) {
            MtkCatLog.d(this, "fail to get ADDRESS data object");
            return false;
        }
        int alpha1Index = getConfirmationAlphaIdIndex(ctlvs, addrIndex);
        int alpha2Index = getCallingAlphaIdIndex(ctlvs, addrIndex);
        ComprehensionTlv ctlv = getConfirmationAlphaId(ctlvs, addrIndex);
        if (ctlv != null) {
            confirmMsg.text = MtkValueParser.retrieveAlphaId(ctlv);
        }
        ComprehensionTlv ctlv2 = getConfirmationIconId(ctlvs, alpha1Index, alpha2Index);
        if (ctlv2 != null) {
            confirmIconId = ValueParser.retrieveIconId(ctlv2);
            confirmMsg.iconSelfExplanatory = confirmIconId.selfExplanatory;
        }
        ComprehensionTlv ctlv3 = getCallingAlphaId(ctlvs, addrIndex);
        if (ctlv3 != null) {
            callMsg.text = MtkValueParser.retrieveAlphaId(ctlv3);
        }
        ComprehensionTlv ctlv4 = getCallingIconId(ctlvs, alpha2Index);
        if (ctlv4 != null) {
            callIconId = ValueParser.retrieveIconId(ctlv4);
            callMsg.iconSelfExplanatory = callIconId.selfExplanatory;
        }
        this.mCmdParams = new CallSetupParams(cmdDet, confirmMsg, callMsg);
        if (confirmIconId == null && callIconId == null) {
            return false;
        }
        this.mIconLoadState = 2;
        int[] recordNumbers = new int[2];
        recordNumbers[0] = confirmIconId != null ? confirmIconId.recordNumber : -1;
        if (callIconId != null) {
            i = callIconId.recordNumber;
        }
        recordNumbers[1] = i;
        this.mIconLoader.loadIcons(recordNumbers, obtainMessage(1));
        return true;
    }

    private int getAddrIndex(List<ComprehensionTlv> list) {
        int addrIndex = 0;
        for (ComprehensionTlv temp : list) {
            if (temp.getTag() == ComprehensionTlvTag.ADDRESS.value() || temp.getTag() == ComprehensionTlvTag.URL.value()) {
                return addrIndex;
            }
            addrIndex++;
        }
        return -1;
    }

    private int getConfirmationAlphaIdIndex(List<ComprehensionTlv> list, int addrIndex) {
        int alphaIndex = 0;
        for (ComprehensionTlv temp : list) {
            if (temp.getTag() == ComprehensionTlvTag.ALPHA_ID.value() && alphaIndex < addrIndex) {
                return alphaIndex;
            }
            alphaIndex++;
        }
        return -1;
    }

    private int getCallingAlphaIdIndex(List<ComprehensionTlv> list, int addrIndex) {
        int alphaIndex = 0;
        for (ComprehensionTlv temp : list) {
            if (temp.getTag() == ComprehensionTlvTag.ALPHA_ID.value() && alphaIndex > addrIndex) {
                return alphaIndex;
            }
            alphaIndex++;
        }
        return -1;
    }

    private ComprehensionTlv getConfirmationAlphaId(List<ComprehensionTlv> list, int addrIndex) {
        int alphaIndex = 0;
        for (ComprehensionTlv temp : list) {
            if (temp.getTag() == ComprehensionTlvTag.ALPHA_ID.value() && alphaIndex < addrIndex) {
                return temp;
            }
            alphaIndex++;
        }
        return null;
    }

    private ComprehensionTlv getCallingAlphaId(List<ComprehensionTlv> list, int addrIndex) {
        int alphaIndex = 0;
        for (ComprehensionTlv temp : list) {
            if (temp.getTag() == ComprehensionTlvTag.ALPHA_ID.value() && alphaIndex > addrIndex) {
                return temp;
            }
            alphaIndex++;
        }
        return null;
    }

    private ComprehensionTlv getConfirmationIconId(List<ComprehensionTlv> list, int alpha1Index, int alpha2Index) {
        if (-1 == alpha1Index) {
            return null;
        }
        int iconIndex = 0;
        for (ComprehensionTlv temp : list) {
            if (temp.getTag() == ComprehensionTlvTag.ICON_ID.value() && (-1 == alpha2Index || iconIndex < alpha2Index)) {
                return temp;
            }
            iconIndex++;
        }
        return null;
    }

    private ComprehensionTlv getCallingIconId(List<ComprehensionTlv> list, int alpha2Index) {
        if (-1 == alpha2Index) {
            return null;
        }
        int iconIndex = 0;
        for (ComprehensionTlv temp : list) {
            if (temp.getTag() == ComprehensionTlvTag.ICON_ID.value() && iconIndex > alpha2Index) {
                return temp;
            }
            iconIndex++;
        }
        return null;
    }
}
