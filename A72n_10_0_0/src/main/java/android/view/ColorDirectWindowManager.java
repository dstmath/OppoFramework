package android.view;

import android.os.Parcel;
import android.os.RemoteException;
import com.color.direct.ColorDirectFindCmd;

public class ColorDirectWindowManager extends ColorBaseWindowManager implements IColorDirectWindowManager {
    private static final String TAG = "ColorDirectWindowManager";

    @Override // android.view.IColorDirectWindowManager
    public void directFindCmd(ColorDirectFindCmd findCmd) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            if (findCmd != null) {
                data.writeInt(1);
                findCmd.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(IColorDirectWindowManager.DIRECT_FIND_CMD, data, null, 1);
        } finally {
            data.recycle();
        }
    }
}
