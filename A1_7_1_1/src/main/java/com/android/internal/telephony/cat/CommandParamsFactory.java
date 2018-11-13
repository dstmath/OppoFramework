package com.android.internal.telephony.cat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.cat.AppInterface.CommandType;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.util.Iterator;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class CommandParamsFactory extends Handler {
    /* renamed from: -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static final /* synthetic */ int[] f37x72eb89a2 = null;
    static final int BATTERY_STATE = 10;
    static final int DTTZ_SETTING = 3;
    static final int LANGUAGE_SETTING = 4;
    static final int LOAD_MULTI_ICONS = 2;
    static final int LOAD_NO_ICON = 0;
    static final int LOAD_SINGLE_ICON = 1;
    private static final int MAX_GSM7_DEFAULT_CHARS = 239;
    private static final int MAX_UCS2_CHARS = 118;
    static final int MSG_ID_LOAD_ICON_DONE = 1;
    static final int REFRESH_NAA_INIT = 3;
    static final int REFRESH_NAA_INIT_AND_FILE_CHANGE = 2;
    static final int REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE = 0;
    static final int REFRESH_UICC_RESET = 4;
    private static CommandParamsFactory sInstance;
    private RilMessageDecoder mCaller;
    private CommandParams mCmdParams;
    private Context mContext;
    private int mIconLoadState;
    private IconLoader mIconLoader;
    private boolean mloadIcon;
    int tlvIndex;

    /* renamed from: -getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static /* synthetic */ int[] m158xe796fd46() {
        if (f37x72eb89a2 != null) {
            return f37x72eb89a2;
        }
        int[] iArr = new int[CommandType.values().length];
        try {
            iArr[CommandType.ACTIVATE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandType.CALLCTRL_RSP_MSG.ordinal()] = 23;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandType.CLOSE_CHANNEL.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandType.DECLARE_SERVICE.ordinal()] = 24;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandType.DISPLAY_MULTIMEDIA_MESSAGE.ordinal()] = 25;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommandType.DISPLAY_TEXT.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommandType.GET_CHANNEL_STATUS.ordinal()] = 4;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CommandType.GET_FRAME_STATUS.ordinal()] = 26;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CommandType.GET_INKEY.ordinal()] = 5;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CommandType.GET_INPUT.ordinal()] = 6;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CommandType.GET_READER_STATUS.ordinal()] = 27;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CommandType.GET_SERVICE_INFORMATION.ordinal()] = 28;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[CommandType.LANGUAGE_NOTIFICATION.ordinal()] = 29;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[CommandType.LAUNCH_BROWSER.ordinal()] = 7;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[CommandType.MORE_TIME.ordinal()] = 30;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[CommandType.OPEN_CHANNEL.ordinal()] = 8;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[CommandType.PERFORM_CARD_APDU.ordinal()] = 31;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[CommandType.PLAY_TONE.ordinal()] = 9;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[CommandType.POLLING_OFF.ordinal()] = 32;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[CommandType.POLL_INTERVAL.ordinal()] = 33;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[CommandType.POWER_OFF_CARD.ordinal()] = 34;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[CommandType.POWER_ON_CARD.ordinal()] = 35;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[CommandType.PROVIDE_LOCAL_INFORMATION.ordinal()] = 10;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[CommandType.RECEIVE_DATA.ordinal()] = 11;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[CommandType.REFRESH.ordinal()] = 12;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[CommandType.RETRIEVE_MULTIMEDIA_MESSAGE.ordinal()] = 36;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[CommandType.RUN_AT_COMMAND.ordinal()] = 37;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[CommandType.SELECT_ITEM.ordinal()] = 13;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[CommandType.SEND_DATA.ordinal()] = 14;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[CommandType.SEND_DTMF.ordinal()] = 15;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[CommandType.SEND_SMS.ordinal()] = 16;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[CommandType.SEND_SS.ordinal()] = 17;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[CommandType.SEND_USSD.ordinal()] = 18;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[CommandType.SERVICE_SEARCH.ordinal()] = 38;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[CommandType.SET_FRAME.ordinal()] = 39;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[CommandType.SET_UP_CALL.ordinal()] = 19;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[CommandType.SET_UP_EVENT_LIST.ordinal()] = 20;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[CommandType.SET_UP_IDLE_MODE_TEXT.ordinal()] = 21;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[CommandType.SET_UP_MENU.ordinal()] = 22;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[CommandType.SUBMIT_MULTIMEDIA_MESSAGE.ordinal()] = 40;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[CommandType.TIMER_MANAGEMENT.ordinal()] = 41;
        } catch (NoSuchFieldError e41) {
        }
        f37x72eb89a2 = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.CommandParamsFactory.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.CommandParamsFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CommandParamsFactory.<clinit>():void");
    }

    static synchronized CommandParamsFactory getInstance(RilMessageDecoder caller, IccFileHandler fh) {
        synchronized (CommandParamsFactory.class) {
            CommandParamsFactory commandParamsFactory;
            if (sInstance != null) {
                commandParamsFactory = sInstance;
                return commandParamsFactory;
            } else if (fh != null) {
                commandParamsFactory = new CommandParamsFactory(caller, fh);
                return commandParamsFactory;
            } else {
                return null;
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0018, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static synchronized CommandParamsFactory getInstance(RilMessageDecoder caller, IccFileHandler fh, Context context) {
        synchronized (CommandParamsFactory.class) {
            CommandParamsFactory commandParamsFactory;
            if (sInstance != null) {
                commandParamsFactory = sInstance;
                return commandParamsFactory;
            } else if (fh == null || context == null) {
            } else {
                commandParamsFactory = new CommandParamsFactory(caller, fh, context);
                return commandParamsFactory;
            }
        }
    }

    private CommandParamsFactory(RilMessageDecoder caller, IccFileHandler fh, Context context) {
        this.mCmdParams = null;
        this.mIconLoadState = 0;
        this.mCaller = null;
        this.mloadIcon = false;
        this.tlvIndex = -1;
        this.mCaller = caller;
        this.mIconLoader = IconLoader.getInstance(this, fh, this.mCaller.getSlotId());
        this.mContext = context;
    }

    private CommandParamsFactory(RilMessageDecoder caller, IccFileHandler fh) {
        this.mCmdParams = null;
        this.mIconLoadState = 0;
        this.mCaller = null;
        this.mloadIcon = false;
        this.tlvIndex = -1;
        this.mCaller = caller;
        this.mIconLoader = IconLoader.getInstance(this, fh, this.mCaller.getSlotId());
    }

    private CommandDetails processCommandDetails(List<ComprehensionTlv> ctlvs) throws ResultException {
        if (ctlvs == null) {
            return null;
        }
        ComprehensionTlv ctlvCmdDet = searchForTag(ComprehensionTlvTag.COMMAND_DETAILS, ctlvs);
        if (ctlvCmdDet == null) {
            return null;
        }
        try {
            return ValueParser.retrieveCommandDetails(ctlvCmdDet);
        } catch (ResultException e) {
            CatLog.d((Object) this, "Failed to procees command details");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    void make(BerTlv berTlv) {
        if (berTlv != null) {
            this.mCmdParams = null;
            this.mIconLoadState = 0;
            if (berTlv.getTag() != BerTlv.BER_PROACTIVE_COMMAND_TAG) {
                CatLog.e((Object) this, "CPF-make: Ununderstood proactive command tag");
                sendCmdParams(ResultCode.CMD_TYPE_NOT_UNDERSTOOD);
                return;
            }
            List<ComprehensionTlv> ctlvs = berTlv.getComprehensionTlvs();
            try {
                CommandDetails cmdDet = processCommandDetails(ctlvs);
                if (cmdDet == null) {
                    CatLog.e((Object) this, "CPF-make: No CommandDetails object");
                    sendCmdParams(ResultCode.CMD_TYPE_NOT_UNDERSTOOD);
                    return;
                }
                CommandType cmdType = CommandType.fromInt(cmdDet.typeOfCommand);
                if (cmdType == null) {
                    CatLog.d((Object) this, "CPF-make: Command type can't be found");
                    this.mCmdParams = new CommandParams(cmdDet);
                    sendCmdParams(ResultCode.BEYOND_TERMINAL_CAPABILITY);
                } else if (berTlv.isLengthValid()) {
                    try {
                        boolean cmdPending;
                        switch (m158xe796fd46()[cmdType.ordinal()]) {
                            case 1:
                                cmdPending = processActivate(cmdDet, ctlvs);
                                break;
                            case 2:
                            case 8:
                            case 11:
                            case 14:
                                cmdPending = processBIPClient(cmdDet, ctlvs);
                                break;
                            case 3:
                                cmdPending = processDisplayText(cmdDet, ctlvs);
                                break;
                            case 4:
                            case 19:
                                cmdPending = processSetupCall(cmdDet, ctlvs);
                                break;
                            case 5:
                                cmdPending = processGetInkey(cmdDet, ctlvs);
                                break;
                            case 6:
                                cmdPending = processGetInput(cmdDet, ctlvs);
                                break;
                            case 7:
                                cmdPending = processLaunchBrowser(cmdDet, ctlvs);
                                break;
                            case 9:
                                cmdPending = processPlayTone(cmdDet, ctlvs);
                                break;
                            case 10:
                                cmdPending = processProvideLocalInfo(cmdDet, ctlvs);
                                break;
                            case 12:
                                processRefresh(cmdDet, ctlvs);
                                cmdPending = false;
                                break;
                            case 13:
                                cmdPending = processSelectItem(cmdDet, ctlvs);
                                break;
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                                cmdPending = processEventNotify(cmdDet, ctlvs);
                                break;
                            case 20:
                                cmdPending = processSetUpEventList(cmdDet, ctlvs);
                                break;
                            case 21:
                                cmdPending = processSetUpIdleModeText(cmdDet, ctlvs);
                                break;
                            case 22:
                                cmdPending = processSelectItem(cmdDet, ctlvs);
                                break;
                            default:
                                this.mCmdParams = new CommandParams(cmdDet);
                                CatLog.d((Object) this, "CPF-make: default case");
                                sendCmdParams(ResultCode.BEYOND_TERMINAL_CAPABILITY);
                                return;
                        }
                        if (!cmdPending) {
                            sendCmdParams(ResultCode.OK);
                        }
                    } catch (ResultException e) {
                        CatLog.d((Object) this, "make: caught ResultException e=" + e);
                        this.mCmdParams = new CommandParams(cmdDet);
                        sendCmdParams(e.result());
                    }
                } else {
                    this.mCmdParams = new CommandParams(cmdDet);
                    sendCmdParams(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            } catch (ResultException e2) {
                CatLog.e((Object) this, "CPF-make: Except to procees command details : " + e2.result());
                sendCmdParams(e2.result());
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                sendCmdParams(setIcons(msg.obj));
                return;
            default:
                return;
        }
    }

    private ResultCode setIcons(Object data) {
        int i = 0;
        if (data == null) {
            CatLog.d((Object) this, "Optional Icon data is NULL");
            if (this.mCmdParams != null) {
                this.mCmdParams.mLoadIconFailed = true;
            }
            this.mloadIcon = false;
            return ResultCode.OK;
        }
        switch (this.mIconLoadState) {
            case 1:
                if (this.mCmdParams != null) {
                    this.mCmdParams.setIcon((Bitmap) data);
                    break;
                }
                break;
            case 2:
                Bitmap[] icons = (Bitmap[]) data;
                int length = icons.length;
                while (i < length) {
                    Bitmap icon = icons[i];
                    if (this.mCmdParams != null) {
                        this.mCmdParams.setIcon(icon);
                    }
                    if (icon == null && this.mloadIcon) {
                        CatLog.d((Object) this, "Optional Icon data is NULL while loading multi icons");
                        if (this.mCmdParams != null) {
                            this.mCmdParams.mLoadIconFailed = true;
                        }
                    }
                    i++;
                }
                break;
        }
        return ResultCode.OK;
    }

    private void sendCmdParams(ResultCode resCode) {
        if (this.mCaller != null) {
            this.mCaller.sendMsgParamsDecoded(resCode, this.mCmdParams);
        }
    }

    private ComprehensionTlv searchForTag(ComprehensionTlvTag tag, List<ComprehensionTlv> ctlvs) {
        return searchForNextTag(tag, ctlvs.iterator());
    }

    private ComprehensionTlv searchForNextTag(ComprehensionTlvTag tag, Iterator<ComprehensionTlv> iter) {
        int tagValue = tag.value();
        while (iter.hasNext()) {
            ComprehensionTlv ctlv = (ComprehensionTlv) iter.next();
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
            CatLog.d((Object) this, "CPF-searchForNextTagAndIndex: Invalid params");
            return null;
        }
        int tagValue = tag.value();
        while (iter.hasNext()) {
            this.tlvIndex++;
            ComprehensionTlv ctlv = (ComprehensionTlv) iter.next();
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

    private boolean processDisplayText(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process DisplayText");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveTextString(ctlv);
        }
        if (textMsg.text == null) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
        boolean z;
        if (searchForTag(ComprehensionTlvTag.IMMEDIATE_RESPONSE, ctlvs) != null) {
            textMsg.responseNeeded = false;
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            try {
                iconId = ValueParser.retrieveIconId(ctlv);
            } catch (ResultException e) {
                CatLog.e((Object) this, "retrieveIconId ResultException: " + e.result());
            }
            try {
                textMsg.iconSelfExplanatory = iconId.selfExplanatory;
            } catch (NullPointerException e2) {
                CatLog.e((Object) this, "iconId is null.");
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
        if (ctlv != null) {
            try {
                textMsg.duration = ValueParser.retrieveDuration(ctlv);
            } catch (ResultException e3) {
                CatLog.e((Object) this, "retrieveDuration ResultException: " + e3.result());
            }
        }
        if ((cmdDet.commandQualifier & 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        textMsg.isHighPriority = z;
        if ((cmdDet.commandQualifier & 128) != 0) {
            z = true;
        } else {
            z = false;
        }
        textMsg.userClear = z;
        this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
        if (iconId == null) {
            return false;
        }
        this.mloadIcon = true;
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    private boolean processSetUpIdleModeText(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SetUpIdleModeText");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            if (ctlv.getLength() == 1) {
                textMsg.text = null;
            } else {
                textMsg.text = ValueParser.retrieveTextString(ctlv);
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        if (textMsg.text != null || iconId == null || textMsg.iconSelfExplanatory) {
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

    private boolean processGetInkey(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process GetInkey");
        Input input = new Input();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            boolean z;
            input.text = ValueParser.retrieveTextString(ctlv);
            ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
            if (ctlv != null) {
                try {
                    iconId = ValueParser.retrieveIconId(ctlv);
                } catch (ResultException e) {
                    CatLog.e((Object) this, "retrieveIconId ResultException: " + e.result());
                }
                try {
                    input.iconSelfExplanatory = iconId.selfExplanatory;
                } catch (NullPointerException e2) {
                    CatLog.e((Object) this, "iconId is null.");
                }
            }
            ctlv = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
            if (ctlv != null) {
                try {
                    input.duration = ValueParser.retrieveDuration(ctlv);
                } catch (ResultException e3) {
                    CatLog.e((Object) this, "retrieveDuration ResultException: " + e3.result());
                }
            }
            input.minLen = 1;
            input.maxLen = 1;
            if ((cmdDet.commandQualifier & 1) == 0) {
                z = true;
            } else {
                z = false;
            }
            input.digitOnly = z;
            if ((cmdDet.commandQualifier & 2) != 0) {
                z = true;
            } else {
                z = false;
            }
            input.ucs2 = z;
            if ((cmdDet.commandQualifier & 4) != 0) {
                z = true;
            } else {
                z = false;
            }
            input.yesNo = z;
            if ((cmdDet.commandQualifier & 128) != 0) {
                z = true;
            } else {
                z = false;
            }
            input.helpAvailable = z;
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

    private boolean processGetInput(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process GetInput");
        Input input = new Input();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TEXT_STRING, ctlvs);
        if (ctlv != null) {
            input.text = ValueParser.retrieveTextString(ctlv);
            ctlv = searchForTag(ComprehensionTlvTag.RESPONSE_LENGTH, ctlvs);
            if (ctlv != null) {
                try {
                    byte[] rawValue = ctlv.getRawValue();
                    int valueIndex = ctlv.getValueIndex();
                    input.minLen = rawValue[valueIndex] & 255;
                    if (input.minLen > MAX_GSM7_DEFAULT_CHARS) {
                        input.minLen = MAX_GSM7_DEFAULT_CHARS;
                    }
                    input.maxLen = rawValue[valueIndex + 1] & 255;
                    if (input.maxLen > MAX_GSM7_DEFAULT_CHARS) {
                        input.maxLen = MAX_GSM7_DEFAULT_CHARS;
                    }
                    ctlv = searchForTag(ComprehensionTlvTag.DEFAULT_TEXT, ctlvs);
                    if (ctlv != null) {
                        try {
                            input.defaultText = ValueParser.retrieveTextString(ctlv);
                        } catch (ResultException e) {
                            CatLog.e((Object) this, "retrieveTextString ResultException: " + e.result());
                        }
                    }
                    ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
                    if (ctlv != null) {
                        try {
                            iconId = ValueParser.retrieveIconId(ctlv);
                        } catch (ResultException e2) {
                            CatLog.e((Object) this, "retrieveIconId ResultException: " + e2.result());
                        }
                        try {
                            input.iconSelfExplanatory = iconId.selfExplanatory;
                        } catch (NullPointerException e3) {
                            CatLog.e((Object) this, "iconId is null.");
                        }
                    }
                    input.digitOnly = (cmdDet.commandQualifier & 1) == 0;
                    input.ucs2 = (cmdDet.commandQualifier & 2) != 0;
                    input.echo = (cmdDet.commandQualifier & 4) == 0;
                    input.packed = (cmdDet.commandQualifier & 8) != 0;
                    input.helpAvailable = (cmdDet.commandQualifier & 128) != 0;
                    if (input.ucs2 && input.maxLen > 118) {
                        CatLog.d((Object) this, "UCS2: received maxLen = " + input.maxLen + ", truncating to " + 118);
                        input.maxLen = 118;
                    } else if (!input.packed && input.maxLen > MAX_GSM7_DEFAULT_CHARS) {
                        CatLog.d((Object) this, "GSM 7Bit Default: received maxLen = " + input.maxLen + ", truncating to " + MAX_GSM7_DEFAULT_CHARS);
                        input.maxLen = MAX_GSM7_DEFAULT_CHARS;
                    }
                    this.mCmdParams = new GetInputParams(cmdDet, input);
                    if (iconId == null) {
                        return false;
                    }
                    this.mloadIcon = true;
                    this.mIconLoadState = 1;
                    this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
                    return true;
                } catch (IndexOutOfBoundsException e4) {
                    throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            }
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
    }

    private boolean processRefresh(CommandDetails cmdDet, List<ComprehensionTlv> list) {
        CatLog.d((Object) this, "process Refresh");
        TextMessage textMsg = new TextMessage();
        switch (cmdDet.commandQualifier) {
            case 0:
            case 2:
            case 3:
            case 4:
                textMsg.text = null;
                this.mCmdParams = new DisplayTextParams(cmdDet, textMsg);
                break;
        }
        return false;
    }

    private boolean processSelectItem(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SelectItem");
        Menu menu = new Menu();
        IconId titleIconId = null;
        ItemsIconId itemsIconId = null;
        Iterator<ComprehensionTlv> iter = ctlvs.iterator();
        CommandType cmdType = CommandType.fromInt(cmdDet.typeOfCommand);
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            try {
                menu.title = ValueParser.retrieveAlphaId(ctlv);
            } catch (ResultException e) {
                CatLog.e((Object) this, "retrieveAlphaId ResultException: " + e.result());
            }
            CatLog.d((Object) this, "add AlphaId: " + menu.title);
        } else if (cmdType == CommandType.SET_UP_MENU) {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        while (true) {
            ctlv = searchForNextTag(ComprehensionTlvTag.ITEM, iter);
            if (ctlv == null) {
                break;
            }
            menu.items.add(ValueParser.retrieveItem(ctlv));
        }
        if (menu.items.size() == 0) {
            CatLog.d((Object) this, "no menu item");
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        }
        ctlv = searchForTag(ComprehensionTlvTag.NEXT_ACTION_INDICATOR, ctlvs);
        if (ctlv != null) {
            try {
                menu.nextActionIndicator = ValueParser.retrieveNextActionIndicator(ctlv);
            } catch (ResultException e2) {
                CatLog.e((Object) this, "retrieveNextActionIndicator ResultException: " + e2.result());
            }
            try {
                if (menu.nextActionIndicator.length != menu.items.size()) {
                    CatLog.d((Object) this, "nextActionIndicator.length != number of menu items");
                    menu.nextActionIndicator = null;
                }
            } catch (NullPointerException e3) {
                CatLog.e((Object) this, "nextActionIndicator is null.");
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.ITEM_ID, ctlvs);
        if (ctlv != null) {
            try {
                menu.defaultItem = ValueParser.retrieveItemId(ctlv) - 1;
            } catch (ResultException e22) {
                CatLog.e((Object) this, "retrieveItemId ResultException: " + e22.result());
            }
            CatLog.d((Object) this, "default item: " + menu.defaultItem);
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            this.mIconLoadState = 1;
            try {
                titleIconId = ValueParser.retrieveIconId(ctlv);
            } catch (ResultException e222) {
                CatLog.e((Object) this, "retrieveIconId ResultException: " + e222.result());
            }
            try {
                menu.titleIconSelfExplanatory = titleIconId.selfExplanatory;
            } catch (NullPointerException e4) {
                CatLog.e((Object) this, "titleIconId is null.");
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.ITEM_ICON_ID_LIST, ctlvs);
        if (ctlv != null) {
            this.mIconLoadState = 2;
            try {
                itemsIconId = ValueParser.retrieveItemsIconId(ctlv);
            } catch (ResultException e2222) {
                CatLog.e((Object) this, "retrieveItemsIconId ResultException: " + e2222.result());
            }
            try {
                menu.itemsIconSelfExplanatory = itemsIconId.selfExplanatory;
            } catch (NullPointerException e5) {
                CatLog.e((Object) this, "itemsIconId is null.");
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
        switch (this.mIconLoadState) {
            case 0:
                return false;
            case 1:
                if (titleIconId != null && titleIconId.recordNumber > 0) {
                    this.mloadIcon = true;
                    this.mIconLoader.loadIcon(titleIconId.recordNumber, obtainMessage(1));
                    break;
                }
                return false;
                break;
            case 2:
                if (itemsIconId != null) {
                    int[] recordNumbers = itemsIconId.recordNumbers;
                    if (titleIconId != null) {
                        recordNumbers = new int[(itemsIconId.recordNumbers.length + 1)];
                        recordNumbers[0] = titleIconId.recordNumber;
                        System.arraycopy(itemsIconId.recordNumbers, 0, recordNumbers, 1, itemsIconId.recordNumbers.length);
                    }
                    this.mloadIcon = true;
                    this.mIconLoader.loadIcons(recordNumbers, obtainMessage(1));
                    break;
                }
                return false;
        }
        return true;
    }

    private boolean processEventNotify(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process EventNotify");
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveAlphaId(ctlv);
        } else {
            textMsg.text = null;
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
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

    private boolean processSetUpEventList(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SetUpEventList");
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.EVENT_LIST, ctlvs);
        if (ctlv != null) {
            try {
                byte[] rawValue = ctlv.getRawValue();
                int valueIndex = ctlv.getValueIndex();
                int valueLen = ctlv.getLength();
                byte[] eventList = new byte[valueLen];
                int index = 0;
                while (index < valueLen) {
                    eventList[index] = rawValue[valueIndex];
                    CatLog.v((Object) this, "CPF-processSetUpEventList: eventList[" + index + "] = " + eventList[index]);
                    Intent intent;
                    if (rawValue[valueIndex] == (byte) 5) {
                        CatLog.v((Object) this, "CPF-processSetUpEventList: sent intent with idle = true");
                        intent = new Intent("android.intent.action.IDLE_SCREEN_NEEDED");
                        intent.putExtra("_enable", true);
                        this.mContext.sendBroadcast(intent);
                    } else if (rawValue[valueIndex] == (byte) 4) {
                        CatLog.v((Object) this, "CPF-processSetUpEventList: sent intent for user activity");
                        intent = new Intent("android.intent.action.stk.USER_ACTIVITY.enable");
                        intent.putExtra("state", true);
                        this.mContext.sendBroadcast(intent);
                    }
                    index++;
                    valueIndex++;
                }
                this.mCmdParams = new SetupEventListParams(cmdDet, eventList);
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        return false;
    }

    private boolean processLaunchBrowser(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        LaunchBrowserMode mode;
        CatLog.d((Object) this, "process LaunchBrowser");
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
        ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            confirmMsg.text = ValueParser.retrieveAlphaId(ctlv);
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            confirmMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        switch (cmdDet.commandQualifier) {
            case 2:
                mode = LaunchBrowserMode.USE_EXISTING_BROWSER;
                break;
            case 3:
                mode = LaunchBrowserMode.LAUNCH_NEW_BROWSER;
                break;
            default:
                mode = LaunchBrowserMode.LAUNCH_IF_NOT_ALREADY_LAUNCHED;
                break;
        }
        this.mCmdParams = new LaunchBrowserParams(cmdDet, confirmMsg, url, mode);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    private boolean processPlayTone(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process PlayTone");
        Tone tone = null;
        TextMessage textMsg = new TextMessage();
        Duration duration = null;
        IconId iconId = null;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.TONE, ctlvs);
        if (ctlv != null && ctlv.getLength() > 0) {
            try {
                tone = Tone.fromInt(ctlv.getRawValue()[ctlv.getValueIndex()]);
            } catch (IndexOutOfBoundsException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            try {
                textMsg.text = ValueParser.retrieveAlphaId(ctlv);
            } catch (ResultException e2) {
                CatLog.e((Object) this, "retrieveAlphaId ResultException: " + e2.result());
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.DURATION, ctlvs);
        if (ctlv != null) {
            try {
                duration = ValueParser.retrieveDuration(ctlv);
            } catch (ResultException e22) {
                CatLog.e((Object) this, "retrieveDuration ResultException: " + e22.result());
            }
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        boolean vibrate = (cmdDet.commandQualifier & 1) != 0;
        textMsg.responseNeeded = false;
        this.mCmdParams = new PlayToneParams(cmdDet, textMsg, tone, duration, vibrate);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    private boolean processSetupCall(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process SetupCall");
        Iterator<ComprehensionTlv> iter = ctlvs.iterator();
        TextMessage confirmMsg = new TextMessage();
        TextMessage callMsg = new TextMessage();
        IconId confirmIconId = null;
        IconId callIconId = null;
        int addrIndex = getAddrIndex(ctlvs);
        if (-1 == addrIndex) {
            CatLog.d((Object) this, "fail to get ADDRESS data object");
            return false;
        }
        int alpha1Index = getConfirmationAlphaIdIndex(ctlvs, addrIndex);
        int alpha2Index = getCallingAlphaIdIndex(ctlvs, addrIndex);
        ComprehensionTlv ctlv = getConfirmationAlphaId(ctlvs, addrIndex);
        if (ctlv != null) {
            confirmMsg.text = ValueParser.retrieveAlphaId(ctlv);
        }
        ctlv = getConfirmationIconId(ctlvs, alpha1Index, alpha2Index);
        if (ctlv != null) {
            confirmIconId = ValueParser.retrieveIconId(ctlv);
            confirmMsg.iconSelfExplanatory = confirmIconId.selfExplanatory;
        }
        ctlv = getCallingAlphaId(ctlvs, addrIndex);
        if (ctlv != null) {
            callMsg.text = ValueParser.retrieveAlphaId(ctlv);
        }
        ctlv = getCallingIconId(ctlvs, alpha2Index);
        if (ctlv != null) {
            callIconId = ValueParser.retrieveIconId(ctlv);
            callMsg.iconSelfExplanatory = callIconId.selfExplanatory;
        }
        this.mCmdParams = new CallSetupParams(cmdDet, confirmMsg, callMsg);
        if (confirmIconId == null && callIconId == null) {
            return false;
        }
        int i;
        this.mIconLoadState = 2;
        int[] recordNumbers = new int[2];
        recordNumbers[0] = confirmIconId != null ? confirmIconId.recordNumber : -1;
        if (callIconId != null) {
            i = callIconId.recordNumber;
        } else {
            i = -1;
        }
        recordNumbers[1] = i;
        this.mIconLoader.loadIcons(recordNumbers, obtainMessage(1));
        return true;
    }

    private boolean processProvideLocalInfo(CommandDetails cmdDet, List<ComprehensionTlv> list) throws ResultException {
        CatLog.d((Object) this, "process ProvideLocalInfo");
        switch (cmdDet.commandQualifier) {
            case 3:
                CatLog.d((Object) this, "PLI [DTTZ_SETTING]");
                this.mCmdParams = new CommandParams(cmdDet);
                break;
            case 4:
                CatLog.d((Object) this, "PLI [LANGUAGE_SETTING]");
                this.mCmdParams = new CommandParams(cmdDet);
                break;
            default:
                CatLog.d((Object) this, "PLI[" + cmdDet.commandQualifier + "] Command Not Supported");
                this.mCmdParams = new CommandParams(cmdDet);
                throw new ResultException(ResultCode.BEYOND_TERMINAL_CAPABILITY);
        }
        return false;
    }

    private boolean processActivate(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d((Object) this, "process Activate");
        int target = 0;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ACTIVATE_DESCRIPTOR, ctlvs);
        if (ctlv != null) {
            try {
                target = ValueParser.retrieveTarget(ctlv);
                CatLog.d((Object) this, "target: " + target);
            } catch (ResultException e) {
                throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
            }
        }
        this.mCmdParams = new ActivateParams(cmdDet, target);
        return false;
    }

    private int getAddrIndex(List<ComprehensionTlv> list) {
        int addrIndex = 0;
        for (ComprehensionTlv temp : list) {
            if (temp.getTag() == ComprehensionTlvTag.ADDRESS.value()) {
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

    private boolean processBIPClient(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CommandType commandType = CommandType.fromInt(cmdDet.typeOfCommand);
        if (commandType != null) {
            CatLog.d((Object) this, "process " + commandType.name());
        }
        TextMessage textMsg = new TextMessage();
        IconId iconId = null;
        boolean has_alpha_id = false;
        ComprehensionTlv ctlv = searchForTag(ComprehensionTlvTag.ALPHA_ID, ctlvs);
        if (ctlv != null) {
            textMsg.text = ValueParser.retrieveAlphaId(ctlv);
            CatLog.d((Object) this, "alpha TLV text=" + textMsg.text);
            has_alpha_id = true;
        }
        ctlv = searchForTag(ComprehensionTlvTag.ICON_ID, ctlvs);
        if (ctlv != null) {
            iconId = ValueParser.retrieveIconId(ctlv);
            textMsg.iconSelfExplanatory = iconId.selfExplanatory;
        }
        textMsg.responseNeeded = false;
        this.mCmdParams = new BIPClientParams(cmdDet, textMsg, has_alpha_id);
        if (iconId == null) {
            return false;
        }
        this.mIconLoadState = 1;
        this.mIconLoader.loadIcon(iconId.recordNumber, obtainMessage(1));
        return true;
    }

    public void dispose() {
        if (this.mIconLoader != null) {
            try {
                this.mIconLoader.dispose();
            } catch (Exception e) {
            }
        }
        this.mIconLoader = null;
        this.mCmdParams = null;
        this.mCaller = null;
        sInstance = null;
    }
}
