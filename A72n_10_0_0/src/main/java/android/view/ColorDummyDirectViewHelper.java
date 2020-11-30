package android.view;

import android.os.Parcel;
import android.os.RemoteException;
import com.color.direct.ColorDirectFindCmd;
import java.lang.ref.WeakReference;

public class ColorDummyDirectViewHelper implements IColorDirectViewHelper {
    public ColorDummyDirectViewHelper(WeakReference<ViewRootImpl> weakReference) {
    }

    @Override // android.view.IColorDirectViewHelper
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        return false;
    }

    @Override // android.view.IColorDirectWindow
    public void directFindCmd(ColorDirectFindCmd colorDirectFindCmd) throws RemoteException {
    }
}
