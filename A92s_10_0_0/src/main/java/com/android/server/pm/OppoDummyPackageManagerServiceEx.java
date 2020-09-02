package com.android.server.pm;

import android.content.Context;
import android.os.Message;
import com.android.server.OppoDummyCommonManagerServiceEx;
import com.color.util.ColorTypeCastingHelper;

public class OppoDummyPackageManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoPackageManagerServiceEx {
    protected final OppoBasePackageManagerService mBasePms;
    protected final PackageManagerService mPms;

    public OppoDummyPackageManagerServiceEx(Context context, PackageManagerService pms) {
        super(context);
        this.mPms = pms;
        this.mBasePms = typeCasting(pms);
    }

    @Override // com.android.server.pm.IOppoPackageManagerServiceEx
    public PackageManagerService getPackageManagerService() {
        return this.mPms;
    }

    @Override // com.android.server.pm.IOppoPackageManagerServiceEx
    public void handleMessage(Message msg, int whichHandler) {
    }

    private static OppoBasePackageManagerService typeCasting(PackageManagerService pms) {
        if (pms != null) {
            return (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, pms);
        }
        return null;
    }
}
