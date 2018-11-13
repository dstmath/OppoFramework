package android.view;

import android.graphics.Rect;
import android.os.RemoteException;

public interface IColorLongshotWindowManager {
    void getFocusedWindowFrame(Rect rect) throws RemoteException;

    int getLongshotSurfaceLayer() throws RemoteException;

    int getLongshotSurfaceLayerByType(int i) throws RemoteException;

    boolean isKeyguardShowingAndNotOccluded() throws RemoteException;

    boolean isNavigationBarVisible() throws RemoteException;

    boolean isShortcutsPanelShow() throws RemoteException;

    void longshotInjectInput(InputEvent inputEvent, int i) throws RemoteException;

    void longshotInjectInputBegin() throws RemoteException;

    void longshotInjectInputEnd() throws RemoteException;

    void longshotNotifyConnected(boolean z) throws RemoteException;
}
