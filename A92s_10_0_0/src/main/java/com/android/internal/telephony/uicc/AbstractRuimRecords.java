package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.OppoTelephonyFactory;

public abstract class AbstractRuimRecords extends AbstractBaseRecords {
    public static final int EVENT_GET_POL_DONE = 99;
    public static final int EVENT_GET_POL_ERROR = 77;
    public static final int EVENT_SET_POL_DONE = 88;
    public static final int EVENT_SET_POL_ERROR = 66;
    protected IOppoRuimRecords mReference = ((IOppoRuimRecords) OppoTelephonyFactory.getInstance().getFeature(IOppoRuimRecords.DEFAULT, this));

    public AbstractRuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
    }

    public UiccCardApplication getApp() {
        return this.mParentApp;
    }

    public Context getContext() {
        return this.mContext;
    }

    public IccFileHandler getFh() {
        return this.mFh;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onAllRecordsLoaded() {
        this.mReference.onAllRecordsLoaded();
    }

    public void onEfCsimImsimRecordLoaded(String mImsi) {
        this.mReference.onEfCsimImsimRecordLoaded(mImsi);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 66 || i == 77 || i == 88 || i == 99) {
            this.mReference.handleMessage(msg);
        } else {
            super.handleMessage(msg);
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.AbstractBaseRecords
    public void dispose() {
        super.dispose();
        this.mReference.dispose();
    }
}
