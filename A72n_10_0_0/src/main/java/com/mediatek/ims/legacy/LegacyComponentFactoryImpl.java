package com.mediatek.ims.legacy;

import android.content.Context;
import android.telephony.ims.stub.ImsUtImplBase;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.feature.MtkImsUtImplBase;
import com.mediatek.ims.legacy.ss.ImsUtStub;
import com.mediatek.ims.legacy.ss.MtkImsUtStub;
import com.mediatek.ims.plugin.impl.LegacyComponentFactoryBase;

public class LegacyComponentFactoryImpl extends LegacyComponentFactoryBase {
    public ImsUtImplBase makeImsUt(Context context, int phoneId, ImsService imsService) {
        return ImsUtStub.getInstance(context, phoneId, imsService);
    }

    public MtkImsUtImplBase makeMtkImsUt(Context context, int phoneId, ImsService imsService) {
        return MtkImsUtStub.getInstance(context, phoneId, imsService);
    }
}
