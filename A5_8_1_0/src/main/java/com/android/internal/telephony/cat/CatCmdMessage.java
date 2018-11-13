package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.cat.AppInterface.CommandType;

public class CatCmdMessage implements Parcelable {
    /* renamed from: -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static final /* synthetic */ int[] f25x72eb89a2 = null;
    public static final Creator<CatCmdMessage> CREATOR = new Creator<CatCmdMessage>() {
        public CatCmdMessage createFromParcel(Parcel in) {
            return new CatCmdMessage(in);
        }

        public CatCmdMessage[] newArray(int size) {
            return new CatCmdMessage[size];
        }
    };
    static final int REFRESH_NAA_INIT = 3;
    static final int REFRESH_NAA_INIT_AND_FILE_CHANGE = 2;
    static final int REFRESH_NAA_INIT_AND_FULL_FILE_CHANGE = 0;
    static final int REFRESH_UICC_RESET = 4;
    private BrowserSettings mBrowserSettings = null;
    private CallSettings mCallSettings = null;
    CommandDetails mCmdDet;
    private Input mInput;
    private boolean mLoadIconFailed = false;
    private Menu mMenu;
    private SetupEventListSettings mSetupEventListSettings = null;
    private TextMessage mTextMsg;
    private ToneSettings mToneSettings = null;

    public class BrowserSettings {
        public LaunchBrowserMode mode;
        public String url;
    }

    public final class BrowserTerminationCauses {
        public static final int ERROR_TERMINATION = 1;
        public static final int USER_TERMINATION = 0;
    }

    public class CallSettings {
        public TextMessage callMsg;
        public TextMessage confirmMsg;
    }

    public final class SetupEventListConstants {
        public static final int BROWSER_TERMINATION_EVENT = 8;
        public static final int BROWSING_STATUS_EVENT = 15;
        public static final int HCI_CONNECTIVITY_EVENT = 19;
        public static final int IDLE_SCREEN_AVAILABLE_EVENT = 5;
        public static final int LANGUAGE_SELECTION_EVENT = 7;
        public static final int USER_ACTIVITY_EVENT = 4;
    }

    public class SetupEventListSettings {
        public int[] eventList;
    }

    /* renamed from: -getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static /* synthetic */ int[] m27xe796fd46() {
        if (f25x72eb89a2 != null) {
            return f25x72eb89a2;
        }
        int[] iArr = new int[CommandType.values().length];
        try {
            iArr[CommandType.ACTIVATE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandType.CLOSE_CHANNEL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandType.DISPLAY_TEXT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandType.GET_CHANNEL_STATUS.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandType.GET_INKEY.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommandType.GET_INPUT.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommandType.LAUNCH_BROWSER.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CommandType.OPEN_CHANNEL.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CommandType.PLAY_TONE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CommandType.PROVIDE_LOCAL_INFORMATION.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CommandType.RECEIVE_DATA.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CommandType.REFRESH.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[CommandType.SELECT_ITEM.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[CommandType.SEND_DATA.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[CommandType.SEND_DTMF.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[CommandType.SEND_SMS.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[CommandType.SEND_SS.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[CommandType.SEND_USSD.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[CommandType.SET_UP_CALL.ordinal()] = 19;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[CommandType.SET_UP_EVENT_LIST.ordinal()] = 20;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[CommandType.SET_UP_IDLE_MODE_TEXT.ordinal()] = 21;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[CommandType.SET_UP_MENU.ordinal()] = 22;
        } catch (NoSuchFieldError e22) {
        }
        f25x72eb89a2 = iArr;
        return iArr;
    }

    CatCmdMessage(CommandParams cmdParams) {
        this.mCmdDet = cmdParams.mCmdDet;
        this.mLoadIconFailed = cmdParams.mLoadIconFailed;
        switch (m27xe796fd46()[getCmdType().ordinal()]) {
            case 2:
            case 8:
            case 11:
            case 14:
                this.mTextMsg = ((BIPClientParams) cmdParams).mTextMsg;
                return;
            case 3:
            case 12:
            case 15:
            case 16:
            case 17:
            case 18:
            case 21:
                this.mTextMsg = ((DisplayTextParams) cmdParams).mTextMsg;
                return;
            case 4:
                this.mTextMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                return;
            case 5:
            case 6:
                this.mInput = ((GetInputParams) cmdParams).mInput;
                return;
            case 7:
                this.mTextMsg = ((LaunchBrowserParams) cmdParams).mConfirmMsg;
                this.mBrowserSettings = new BrowserSettings();
                this.mBrowserSettings.url = ((LaunchBrowserParams) cmdParams).mUrl;
                this.mBrowserSettings.mode = ((LaunchBrowserParams) cmdParams).mMode;
                return;
            case 9:
                PlayToneParams params = (PlayToneParams) cmdParams;
                this.mToneSettings = params.mSettings;
                this.mTextMsg = params.mTextMsg;
                return;
            case 13:
            case 22:
                this.mMenu = ((SelectItemParams) cmdParams).mMenu;
                return;
            case 19:
                this.mCallSettings = new CallSettings();
                this.mCallSettings.confirmMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                this.mCallSettings.callMsg = ((CallSetupParams) cmdParams).mCallMsg;
                return;
            case 20:
                this.mSetupEventListSettings = new SetupEventListSettings();
                this.mSetupEventListSettings.eventList = ((SetEventListParams) cmdParams).mEventInfo;
                return;
            default:
                return;
        }
    }

    public CatCmdMessage(Parcel in) {
        this.mCmdDet = (CommandDetails) in.readParcelable(null);
        this.mTextMsg = (TextMessage) in.readParcelable(null);
        this.mMenu = (Menu) in.readParcelable(null);
        this.mInput = (Input) in.readParcelable(null);
        this.mLoadIconFailed = in.readByte() == (byte) 1;
        switch (m27xe796fd46()[getCmdType().ordinal()]) {
            case 7:
                this.mBrowserSettings = new BrowserSettings();
                this.mBrowserSettings.url = in.readString();
                this.mBrowserSettings.mode = LaunchBrowserMode.values()[in.readInt()];
                return;
            case 9:
                this.mToneSettings = (ToneSettings) in.readParcelable(null);
                return;
            case 19:
                this.mCallSettings = new CallSettings();
                this.mCallSettings.confirmMsg = (TextMessage) in.readParcelable(null);
                this.mCallSettings.callMsg = (TextMessage) in.readParcelable(null);
                return;
            case 20:
                this.mSetupEventListSettings = new SetupEventListSettings();
                int length = in.readInt();
                this.mSetupEventListSettings.eventList = new int[length];
                for (int i = 0; i < length; i++) {
                    this.mSetupEventListSettings.eventList[i] = in.readInt();
                }
                return;
            default:
                return;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCmdDet, 0);
        dest.writeParcelable(this.mTextMsg, 0);
        dest.writeParcelable(this.mMenu, 0);
        dest.writeParcelable(this.mInput, 0);
        dest.writeByte((byte) (this.mLoadIconFailed ? 1 : 0));
        switch (m27xe796fd46()[getCmdType().ordinal()]) {
            case 7:
                dest.writeString(this.mBrowserSettings.url);
                dest.writeInt(this.mBrowserSettings.mode.ordinal());
                return;
            case 9:
                dest.writeParcelable(this.mToneSettings, 0);
                return;
            case 19:
                dest.writeParcelable(this.mCallSettings.confirmMsg, 0);
                dest.writeParcelable(this.mCallSettings.callMsg, 0);
                return;
            case 20:
                dest.writeIntArray(this.mSetupEventListSettings.eventList);
                return;
            default:
                return;
        }
    }

    public int describeContents() {
        return 0;
    }

    public CommandType getCmdType() {
        return CommandType.fromInt(this.mCmdDet.typeOfCommand);
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public Input geInput() {
        return this.mInput;
    }

    public TextMessage geTextMessage() {
        return this.mTextMsg;
    }

    public BrowserSettings getBrowserSettings() {
        return this.mBrowserSettings;
    }

    public ToneSettings getToneSettings() {
        return this.mToneSettings;
    }

    public CallSettings getCallSettings() {
        return this.mCallSettings;
    }

    public SetupEventListSettings getSetEventList() {
        return this.mSetupEventListSettings;
    }

    public boolean hasIconLoadFailed() {
        return this.mLoadIconFailed;
    }

    public boolean isRefreshResetOrInit() {
        if (this.mCmdDet.commandQualifier == 0 || this.mCmdDet.commandQualifier == 2 || this.mCmdDet.commandQualifier == 3 || this.mCmdDet.commandQualifier == 4) {
            return true;
        }
        return false;
    }
}
