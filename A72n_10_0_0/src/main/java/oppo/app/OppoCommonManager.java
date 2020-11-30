package oppo.app;

import android.os.IBinder;
import android.os.ServiceManager;

public abstract class OppoCommonManager {
    protected final IBinder mRemote;

    public OppoCommonManager(String name) {
        this(ServiceManager.getService(name));
    }

    public OppoCommonManager(IBinder remote) {
        this.mRemote = remote;
    }
}
