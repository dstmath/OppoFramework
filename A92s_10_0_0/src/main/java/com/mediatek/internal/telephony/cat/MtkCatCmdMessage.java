package com.mediatek.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.Menu;
import com.mediatek.android.mms.pdu.MtkCharacterSets;

public class MtkCatCmdMessage extends CatCmdMessage {
    public static final Parcelable.Creator<MtkCatCmdMessage> CREATOR = new Parcelable.Creator<MtkCatCmdMessage>() {
        /* class com.mediatek.internal.telephony.cat.MtkCatCmdMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkCatCmdMessage createFromParcel(Parcel in) {
            return new MtkCatCmdMessage(in);
        }

        @Override // android.os.Parcelable.Creator
        public MtkCatCmdMessage[] newArray(int size) {
            return new MtkCatCmdMessage[size];
        }
    };

    MtkCatCmdMessage(CommandParams cmdParams) {
        super(cmdParams);
    }

    public MtkCatCmdMessage(Parcel in) {
        super(in);
    }

    public int getCmdQualifier() {
        return this.mCmdDet.commandQualifier;
    }

    public CatCmdMessage convertToCatCmdMessage(CommandParams cmdParams, MtkCatCmdMessage mtkCmdMsg) {
        CatCmdMessage cmdMsg = new CatCmdMessage(cmdParams);
        if (mtkCmdMsg != null) {
            cmdMsg.mCmdDet = mtkCmdMsg.mCmdDet;
            cmdMsg.mTextMsg = mtkCmdMsg.mTextMsg;
            Menu menu = new Menu();
            if (mtkCmdMsg.mMenu != null) {
                menu.items = mtkCmdMsg.mMenu.items;
                menu.titleAttrs = mtkCmdMsg.mMenu.titleAttrs;
                menu.presentationType = mtkCmdMsg.mMenu.presentationType;
                menu.title = mtkCmdMsg.mMenu.title;
                menu.titleIcon = mtkCmdMsg.mMenu.titleIcon;
                menu.defaultItem = mtkCmdMsg.mMenu.defaultItem;
                menu.softKeyPreferred = mtkCmdMsg.mMenu.softKeyPreferred;
                menu.helpAvailable = mtkCmdMsg.mMenu.helpAvailable;
                menu.titleIconSelfExplanatory = mtkCmdMsg.mMenu.titleIconSelfExplanatory;
                menu.itemsIconSelfExplanatory = mtkCmdMsg.mMenu.itemsIconSelfExplanatory;
            }
            cmdMsg.mMenu = menu;
            cmdMsg.mInput = mtkCmdMsg.mInput;
            cmdMsg.mBrowserSettings = mtkCmdMsg.mBrowserSettings;
            cmdMsg.mToneSettings = mtkCmdMsg.mToneSettings;
            cmdMsg.mCallSettings = mtkCmdMsg.mCallSettings;
            cmdMsg.mSetupEventListSettings = mtkCmdMsg.mSetupEventListSettings;
            cmdMsg.mLoadIconFailed = mtkCmdMsg.mLoadIconFailed;
        }
        return cmdMsg;
    }

    public static CatCmdMessage getCmdMsg() {
        CommandDetails cmdDet = new CommandDetails();
        cmdDet.typeOfCommand = MtkCharacterSets.ISO_8859_16;
        return new CatCmdMessage(new CommandParams(cmdDet));
    }
}
