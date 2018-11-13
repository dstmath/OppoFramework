package com.android.internal.telephony.cat;

public class CatResponseMessage {
    byte[] mAddedInfo = null;
    int mAdditionalInfo = 0;
    CommandDetails mCmdDet = null;
    int mEventValue = -1;
    boolean mIncludeAdditionalInfo = false;
    ResultCode mResCode = ResultCode.OK;
    boolean mUsersConfirm = false;
    String mUsersInput = null;
    int mUsersMenuSelection = 0;
    boolean mUsersYesNoSelection = false;

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

    CommandDetails getCmdDetails() {
        return this.mCmdDet;
    }
}
