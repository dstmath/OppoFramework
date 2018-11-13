package android.view;

import android.os.RemoteException;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.IColorLongshotViewCallback;
import com.color.view.analysis.ColorViewAnalysis;
import com.color.view.analysis.ColorViewNodeInfo;
import com.color.view.analysis.ColorWindowNode;
import com.color.view.inject.IColorInjectScrollCallback;
import java.io.FileDescriptor;
import java.util.List;

public interface IColorLongshotWindow {
    void getLongshotViewInfo(ColorLongshotViewInfo colorLongshotViewInfo) throws RemoteException;

    void getLongshotViewInfoAsync(ColorLongshotViewInfo colorLongshotViewInfo, IColorLongshotViewCallback iColorLongshotViewCallback) throws RemoteException;

    void longshotAnalysisView(ColorViewAnalysis colorViewAnalysis, List<ColorViewNodeInfo> list, List<ColorViewNodeInfo> list2, boolean z, boolean z2) throws RemoteException;

    void longshotCollectRoot(List<ColorViewNodeInfo> list) throws RemoteException;

    ColorWindowNode longshotCollectWindow(boolean z, boolean z2) throws RemoteException;

    void longshotDump(FileDescriptor fileDescriptor, List<ColorWindowNode> list, List<ColorWindowNode> list2) throws RemoteException;

    void longshotEndScroll() throws RemoteException;

    void longshotInjectScroll(IColorInjectScrollCallback iColorInjectScrollCallback) throws RemoteException;

    void longshotNotifyConnected(boolean z) throws RemoteException;

    void longshotSetScrollMode(boolean z) throws RemoteException;
}
