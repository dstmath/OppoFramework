package android.view;

import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;

public interface IColorLongshotWindowManager extends IColorBaseWindowManager {
    public static final int GET_FOCUSED_WINDOW_FRAME = 10202;
    public static final int GET_LONGSHOT_SURFACE_LAYER = 10204;
    public static final int GET_LONGSHOT_SURFACE_LAYER_BY_TYPE = 10205;
    public static final int GET_LONGSHOT_WINDOW_BY_TYPE = 10212;
    public static final int ICOLORLONGSHOTWINDOWMANAGER_INDEX = 10201;
    public static final int IS_EDGE_PANEL_EXPAND = 10215;
    public static final int IS_FLOAT_ASSIST_EXPAND = 10214;
    public static final int IS_KEYGUARD_SHOWING_AND_NOT_OCCLUDED = 10208;
    public static final int IS_NAVIGATIONBAR_VISIBLE = 10207;
    public static final int IS_SHORTCUTS_PANEL_SHOW = 10209;
    public static final int IS_VOLUME_SHOW = 10213;
    public static final int LONGSHOT_INJECT_INPUT = 10203;
    public static final int LONGSHOT_INJECT_INPUT_BEGIN = 10210;
    public static final int LONGSHOT_INJECT_INPUT_END = 10211;
    public static final int LONGSHOT_NOTIFY_CONNECTED = 10206;

    void getFocusedWindowFrame(Rect rect) throws RemoteException;

    int getLongshotSurfaceLayer() throws RemoteException;

    int getLongshotSurfaceLayerByType(int i) throws RemoteException;

    IBinder getLongshotWindowByType(int i) throws RemoteException;

    boolean isEdgePanelExpand() throws RemoteException;

    boolean isFloatAssistExpand() throws RemoteException;

    boolean isKeyguardShowingAndNotOccluded() throws RemoteException;

    boolean isNavigationBarVisible() throws RemoteException;

    boolean isShortcutsPanelShow() throws RemoteException;

    boolean isVolumeShow() throws RemoteException;

    void longshotInjectInput(InputEvent inputEvent, int i) throws RemoteException;

    void longshotInjectInputBegin() throws RemoteException;

    void longshotInjectInputEnd() throws RemoteException;

    void longshotNotifyConnected(boolean z) throws RemoteException;
}
