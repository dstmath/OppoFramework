package android.view;

import android.graphics.Rect;
import android.os.RemoteException;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.IColorLongshotViewCallback;
import com.color.view.analysis.IColorViewAnalysisCallback;

public interface IColorLongshotWindowManager {
    void getFocusedWindowFrame(Rect rect) throws RemoteException;

    int getLongshotSurfaceLayer() throws RemoteException;

    int getLongshotSurfaceLayerByType(int i) throws RemoteException;

    void getLongshotViewInfo(ColorLongshotViewInfo colorLongshotViewInfo) throws RemoteException;

    void getLongshotViewInfoAsync(ColorLongshotViewInfo colorLongshotViewInfo, IColorLongshotViewCallback iColorLongshotViewCallback) throws RemoteException;

    boolean isNavigationBarVisible() throws RemoteException;

    boolean isShortcutsPanelShow() throws RemoteException;

    void longshotAnalysisView(IColorViewAnalysisCallback iColorViewAnalysisCallback, boolean z) throws RemoteException;

    void longshotNotifyConnected(boolean z) throws RemoteException;
}
