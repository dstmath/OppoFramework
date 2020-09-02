package android.view;

import android.common.IOppoCommonFeature;
import android.os.RemoteException;
import com.color.direct.ColorDirectFindCmd;

public interface IColorDirectWindow extends IOppoCommonFeature {
    default void directFindCmd(ColorDirectFindCmd findCmd) throws RemoteException {
    }
}
