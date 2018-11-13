package android.view;

import android.os.RemoteException;
import com.color.view.analysis.ColorWindowNode;
import java.io.FileDescriptor;
import java.util.List;

public interface IColorLongshotWindow {
    ColorWindowNode longshotCollectWindow(boolean z, boolean z2) throws RemoteException;

    void longshotDump(FileDescriptor fileDescriptor, List<ColorWindowNode> list, List<ColorWindowNode> list2) throws RemoteException;

    void longshotInjectInput(InputEvent inputEvent, int i) throws RemoteException;

    void longshotInjectInputBegin() throws RemoteException;

    void longshotInjectInputEnd() throws RemoteException;

    void longshotNotifyConnected(boolean z) throws RemoteException;
}
