package com.android.server.power;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.PswServiceRegistry;

public class PswPowerManagerServiceEx extends OppoDummyPowerManagerServiceEx implements IPswPowerManagerServiceEx {
    private static final String TAG = "PswPowerManagerServiceEx";
    private OppoNwPowerStateManager mOppoNwPowerStateManager = null;

    public PswPowerManagerServiceEx(Context context, PowerManagerService pms) {
        super(context, pms);
        Slog.i(TAG, "create");
        init(context);
    }

    public void onStart() {
        PswPowerManagerServiceEx.super.onStart();
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        PswServiceRegistry.getInstance().serviceReady(23);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return PswPowerManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    private void init(Context context) {
        PswServiceRegistry.getInstance().serviceInit(3, this);
    }

    public void initOppoNwPowerStateManager() {
        Slog.d("IPswPowerManagerServiceEx", "impl initOppoNwPowerStateManager");
        if (this.mOppoNwPowerStateManager == null) {
            this.mOppoNwPowerStateManager = new OppoNwPowerStateManager(this.mContext);
        }
    }
}
