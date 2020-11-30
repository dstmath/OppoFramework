package androidx.test.orchestrator.callback;

import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import androidx.test.runner.internal.deps.aidl.BaseProxy;
import androidx.test.runner.internal.deps.aidl.BaseStub;
import androidx.test.runner.internal.deps.aidl.Codecs;

public interface OrchestratorCallback extends IInterface {
    void addTest(String str) throws RemoteException;

    void sendTestNotification(Bundle bundle) throws RemoteException;

    public static abstract class Stub extends BaseStub implements OrchestratorCallback {
        public static OrchestratorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("androidx.test.orchestrator.callback.OrchestratorCallback");
            if (iin instanceof OrchestratorCallback) {
                return (OrchestratorCallback) iin;
            }
            return new Proxy(obj);
        }

        public static class Proxy extends BaseProxy implements OrchestratorCallback {
            Proxy(IBinder remote) {
                super(remote, "androidx.test.orchestrator.callback.OrchestratorCallback");
            }

            @Override // androidx.test.orchestrator.callback.OrchestratorCallback
            public void addTest(String test) throws RemoteException {
                Parcel data = obtainAndWriteInterfaceToken();
                data.writeString(test);
                transactAndReadExceptionReturnVoid(1, data);
            }

            @Override // androidx.test.orchestrator.callback.OrchestratorCallback
            public void sendTestNotification(Bundle bundle) throws RemoteException {
                Parcel data = obtainAndWriteInterfaceToken();
                Codecs.writeParcelable(data, bundle);
                transactAndReadExceptionReturnVoid(2, data);
            }
        }
    }
}
