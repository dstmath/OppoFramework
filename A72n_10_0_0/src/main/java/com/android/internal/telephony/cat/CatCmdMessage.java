package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.cat.AppInterface;

public class CatCmdMessage implements Parcelable {
    public static final Parcelable.Creator<CatCmdMessage> CREATOR = new Parcelable.Creator<CatCmdMessage>() {
        /* class com.android.internal.telephony.cat.CatCmdMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CatCmdMessage createFromParcel(Parcel in) {
            return new CatCmdMessage(in);
        }

        @Override // android.os.Parcelable.Creator
        public CatCmdMessage[] newArray(int size) {
            return new CatCmdMessage[size];
        }
    };
    public BrowserSettings mBrowserSettings;
    @UnsupportedAppUsage
    public CallSettings mCallSettings;
    @UnsupportedAppUsage
    public CommandDetails mCmdDet;
    @UnsupportedAppUsage
    public Input mInput;
    public boolean mLoadIconFailed;
    @UnsupportedAppUsage
    public Menu mMenu;
    public SetupEventListSettings mSetupEventListSettings;
    @UnsupportedAppUsage
    public TextMessage mTextMsg;
    public ToneSettings mToneSettings;

    public class BrowserSettings {
        public LaunchBrowserMode mode;
        public String url;

        public BrowserSettings() {
        }
    }

    public class CallSettings {
        @UnsupportedAppUsage
        public TextMessage callMsg;
        @UnsupportedAppUsage
        public TextMessage confirmMsg;

        public CallSettings() {
        }
    }

    public class SetupEventListSettings {
        @UnsupportedAppUsage
        public int[] eventList;

        public SetupEventListSettings() {
        }
    }

    public final class SetupEventListConstants {
        public static final int BROWSER_TERMINATION_EVENT = 8;
        public static final int BROWSING_STATUS_EVENT = 15;
        public static final int IDLE_SCREEN_AVAILABLE_EVENT = 5;
        public static final int LANGUAGE_SELECTION_EVENT = 7;
        public static final int USER_ACTIVITY_EVENT = 4;

        public SetupEventListConstants() {
        }
    }

    public final class BrowserTerminationCauses {
        public static final int ERROR_TERMINATION = 1;
        public static final int USER_TERMINATION = 0;

        public BrowserTerminationCauses() {
        }
    }

    public CatCmdMessage(CommandParams cmdParams) {
        this.mBrowserSettings = null;
        this.mToneSettings = null;
        this.mCallSettings = null;
        this.mSetupEventListSettings = null;
        this.mLoadIconFailed = false;
        this.mCmdDet = cmdParams.mCmdDet;
        this.mLoadIconFailed = cmdParams.mLoadIconFailed;
        switch (getCmdType()) {
            case SET_UP_MENU:
            case SELECT_ITEM:
                this.mMenu = ((SelectItemParams) cmdParams).mMenu;
                return;
            case DISPLAY_TEXT:
            case SET_UP_IDLE_MODE_TEXT:
            case SEND_DTMF:
            case SEND_SMS:
            case REFRESH:
            case RUN_AT:
            case SEND_SS:
            case SEND_USSD:
                this.mTextMsg = ((DisplayTextParams) cmdParams).mTextMsg;
                return;
            case GET_INPUT:
            case GET_INKEY:
                this.mInput = ((GetInputParams) cmdParams).mInput;
                return;
            case LAUNCH_BROWSER:
                this.mTextMsg = ((LaunchBrowserParams) cmdParams).mConfirmMsg;
                this.mBrowserSettings = new BrowserSettings();
                this.mBrowserSettings.url = ((LaunchBrowserParams) cmdParams).mUrl;
                this.mBrowserSettings.mode = ((LaunchBrowserParams) cmdParams).mMode;
                return;
            case PLAY_TONE:
                PlayToneParams params = (PlayToneParams) cmdParams;
                this.mToneSettings = params.mSettings;
                this.mTextMsg = params.mTextMsg;
                return;
            case GET_CHANNEL_STATUS:
                this.mTextMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                return;
            case SET_UP_CALL:
                this.mCallSettings = new CallSettings();
                this.mCallSettings.confirmMsg = ((CallSetupParams) cmdParams).mConfirmMsg;
                this.mCallSettings.callMsg = ((CallSetupParams) cmdParams).mCallMsg;
                return;
            case OPEN_CHANNEL:
            case CLOSE_CHANNEL:
            case RECEIVE_DATA:
            case SEND_DATA:
                this.mTextMsg = ((BIPClientParams) cmdParams).mTextMsg;
                return;
            case SET_UP_EVENT_LIST:
                this.mSetupEventListSettings = new SetupEventListSettings();
                this.mSetupEventListSettings.eventList = ((SetEventListParams) cmdParams).mEventInfo;
                return;
            default:
                return;
        }
    }

    public CatCmdMessage(Parcel in) {
        this.mBrowserSettings = null;
        this.mToneSettings = null;
        this.mCallSettings = null;
        this.mSetupEventListSettings = null;
        boolean z = false;
        this.mLoadIconFailed = false;
        this.mCmdDet = (CommandDetails) in.readParcelable(null);
        this.mTextMsg = (TextMessage) in.readParcelable(null);
        this.mMenu = (Menu) in.readParcelable(null);
        this.mInput = (Input) in.readParcelable(null);
        this.mLoadIconFailed = in.readByte() == 1 ? true : z;
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
        if (i == 13) {
            this.mBrowserSettings = new BrowserSettings();
            this.mBrowserSettings.url = in.readString();
            this.mBrowserSettings.mode = LaunchBrowserMode.values()[in.readInt()];
        } else if (i == 14) {
            this.mToneSettings = (ToneSettings) in.readParcelable(null);
        } else if (i == 16) {
            this.mCallSettings = new CallSettings();
            this.mCallSettings.confirmMsg = (TextMessage) in.readParcelable(null);
            this.mCallSettings.callMsg = (TextMessage) in.readParcelable(null);
        } else if (i == 21) {
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
        dest.writeParcelable(this.mMenu, 0);
        dest.writeParcelable(this.mInput, 0);
        dest.writeByte(this.mLoadIconFailed ? (byte) 1 : 0);
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[getCmdType().ordinal()];
        if (i == 13) {
            dest.writeString(this.mBrowserSettings.url);
            dest.writeInt(this.mBrowserSettings.mode.ordinal());
        } else if (i == 14) {
            dest.writeParcelable(this.mToneSettings, 0);
        } else if (i == 16) {
            dest.writeParcelable(this.mCallSettings.confirmMsg, 0);
            dest.writeParcelable(this.mCallSettings.callMsg, 0);
        } else if (i == 21) {
            dest.writeIntArray(this.mSetupEventListSettings.eventList);
        }
    }

    public int describeContents() {
        return 0;
    }

    @UnsupportedAppUsage
    public AppInterface.CommandType getCmdType() {
        return AppInterface.CommandType.fromInt(this.mCmdDet.typeOfCommand);
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public Input geInput() {
        return this.mInput;
    }

    @UnsupportedAppUsage
    public TextMessage geTextMessage() {
        return this.mTextMsg;
    }

    public BrowserSettings getBrowserSettings() {
        return this.mBrowserSettings;
    }

    public ToneSettings getToneSettings() {
        return this.mToneSettings;
    }

    @UnsupportedAppUsage
    public CallSettings getCallSettings() {
        return this.mCallSettings;
    }

    @UnsupportedAppUsage
    public SetupEventListSettings getSetEventList() {
        return this.mSetupEventListSettings;
    }

    @UnsupportedAppUsage
    public boolean hasIconLoadFailed() {
        return this.mLoadIconFailed;
    }
}
