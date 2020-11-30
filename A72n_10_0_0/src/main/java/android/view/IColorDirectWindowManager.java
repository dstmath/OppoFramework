package android.view;

import android.os.RemoteException;
import com.color.direct.ColorDirectFindCmd;

public interface IColorDirectWindowManager extends IColorBaseWindowManager {
    public static final int DIRECT_FIND_CMD = 10402;
    public static final int ICOLORDIRECTWINDOWMANAGER_INDEX = 10401;

    void directFindCmd(ColorDirectFindCmd colorDirectFindCmd) throws RemoteException;
}
