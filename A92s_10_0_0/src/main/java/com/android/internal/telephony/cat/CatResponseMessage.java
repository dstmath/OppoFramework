package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;

public class CatResponseMessage {
    public byte[] mAddedInfo = null;
    public int mAdditionalInfo = 0;
    public CommandDetails mCmdDet = null;
    public int mEventValue = -1;
    public boolean mIncludeAdditionalInfo = false;
    public ResultCode mResCode = ResultCode.OK;
    public boolean mUsersConfirm = false;
    public String mUsersInput = null;
    public int mUsersMenuSelection = 0;
    public boolean mUsersYesNoSelection = false;

    public CatResponseMessage(CatCmdMessage cmdMsg) {
        this.mCmdDet = cmdMsg.mCmdDet;
    }

    public void setResultCode(ResultCode resCode) {
        this.mResCode = resCode;
    }

    public void setMenuSelection(int selection) {
        this.mUsersMenuSelection = selection;
    }

    public void setInput(String input) {
        this.mUsersInput = input;
    }

    @UnsupportedAppUsage
    public void setEventDownload(int event, byte[] addedInfo) {
        this.mEventValue = event;
        this.mAddedInfo = addedInfo;
    }

    public void setYesNo(boolean yesNo) {
        this.mUsersYesNoSelection = yesNo;
    }

    public void setConfirmation(boolean confirm) {
        this.mUsersConfirm = confirm;
    }

    public void setAdditionalInfo(int info) {
        this.mIncludeAdditionalInfo = true;
        this.mAdditionalInfo = info;
    }

    public CommandDetails getCmdDetails() {
        return this.mCmdDet;
    }
}
