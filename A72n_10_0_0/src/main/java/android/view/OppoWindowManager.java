package android.view;

import android.common.OppoFeatureCache;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.color.darkmode.IColorDarkModeListener;
import com.color.direct.ColorDirectFindCmd;
import com.color.screenshot.IColorScreenShotEuclidManager;

public class OppoWindowManager implements IOppoWindowManager {
    private static final String TAG = "OppoWindowManager";
    private final ColorDirectWindowManager mColorDirect = new ColorDirectWindowManager();
    private final IColorLongshotWindowManager mColorLongshot = ((IColorScreenShotEuclidManager) OppoFeatureCache.getOrCreate(IColorScreenShotEuclidManager.DEFAULT, new Object[0])).getIColorLongshotWindowManager();
    private final ColorWindowManager mColorWm = new ColorWindowManager();

    @Override // android.view.IColorWindowManager
    public boolean isLockWndShow() throws RemoteException {
        return this.mColorWm.isLockWndShow();
    }

    @Override // android.view.IColorWindowManager
    public void keyguardSetApkLockScreenShowing(boolean showing) throws RemoteException {
        Log.d(TAG, "keyguardSetApkLockScreenShowing showing = " + showing);
        this.mColorWm.keyguardSetApkLockScreenShowing(showing);
    }

    @Override // android.view.IColorWindowManager
    public IBinder getApkUnlockWindow() throws RemoteException {
        return this.mColorWm.getApkUnlockWindow();
    }

    @Override // android.view.IColorWindowManager
    public void keyguardShowSecureApkLock(boolean show) throws RemoteException {
        this.mColorWm.keyguardShowSecureApkLock(show);
    }

    @Override // android.view.IColorWindowManager
    public boolean isLockOnShow() throws RemoteException {
        return this.mColorWm.isLockOnShow();
    }

    @Override // android.view.IColorWindowManager
    public boolean isSIMUnlockRunning() throws RemoteException {
        return this.mColorWm.isSIMUnlockRunning();
    }

    @Override // android.view.IColorWindowManager
    public boolean isStatusBarVisible() throws RemoteException {
        return this.mColorWm.isStatusBarVisible();
    }

    @Override // android.view.IColorWindowManager
    public boolean isInputShow() throws RemoteException {
        return this.mColorWm.isInputShow();
    }

    @Override // android.view.IColorWindowManager
    public boolean isFullScreen() throws RemoteException {
        return this.mColorWm.isFullScreen();
    }

    @Override // android.view.IColorWindowManager
    public boolean isRotatingLw() throws RemoteException {
        return this.mColorWm.isRotatingLw();
    }

    @Override // android.view.IColorWindowManager
    public boolean checkIsFloatWindowForbidden(String packageName, int type) throws RemoteException {
        return this.mColorWm.checkIsFloatWindowForbidden(packageName, type);
    }

    @Override // android.view.IColorWindowManager
    public void setMagnification(Bundle bundle) throws RemoteException {
        this.mColorWm.setMagnification(bundle);
    }

    @Override // android.view.IColorWindowManager
    public void setMagnificationSpecEx(MagnificationSpec spec) throws RemoteException {
        this.mColorWm.setMagnificationSpecEx(spec);
    }

    @Override // android.view.IColorWindowManager
    public void requestDismissKeyguard() throws RemoteException {
        this.mColorWm.requestDismissKeyguard();
    }

    @Override // android.view.IColorWindowManager
    public void requestKeyguard(String command) throws RemoteException {
        this.mColorWm.requestKeyguard(command);
    }

    @Override // android.view.IColorWindowManager
    public boolean isWindowShownForUid(int uid) throws RemoteException {
        return this.mColorWm.isWindowShownForUid(uid);
    }

    @Override // android.view.IColorWindowManager
    public void removeWindowShownOnKeyguard() throws RemoteException {
        this.mColorWm.removeWindowShownOnKeyguard();
    }

    @Override // android.view.IColorWindowManager
    public String getCurrentFocus() throws RemoteException {
        return this.mColorWm.getCurrentFocus();
    }

    @Override // android.view.IColorWindowManager
    public Rect getFloatWindowRect(int displayId) throws RemoteException {
        return this.mColorWm.getFloatWindowRect(displayId);
    }

    @Override // android.view.IColorWindowManager
    public void setSplitTimeout(int timeout) throws RemoteException {
        this.mColorWm.setSplitTimeout(timeout);
    }

    @Override // android.view.IColorWindowManager
    public void setGestureFollowAnimation(boolean animation) throws RemoteException {
        this.mColorWm.setGestureFollowAnimation(animation);
    }

    @Override // android.view.IColorWindowManager
    public void startColorDragWindow(String packageName, int resId, int mode, Bundle options) throws RemoteException {
        this.mColorWm.startColorDragWindow(packageName, resId, mode, options);
    }

    @Override // android.view.IColorWindowManager
    public void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) throws RemoteException {
        this.mColorWm.registerOppoWindowStateObserver(observer);
    }

    @Override // android.view.IColorWindowManager
    public void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) throws RemoteException {
        this.mColorWm.unregisterOppoWindowStateObserver(observer);
    }

    @Override // android.view.IColorWindowManager
    public boolean isInFreeformMode() throws RemoteException {
        return this.mColorWm.isInFreeformMode();
    }

    @Override // android.view.IColorWindowManager
    public void getFreeformStackBounds(Rect outBounds) throws RemoteException {
        this.mColorWm.getFreeformStackBounds(outBounds);
    }

    @Override // android.view.IColorWindowManager
    public void createMonitorInputConsumer(IBinder token, String name, InputChannel inputChannel) throws RemoteException {
        this.mColorWm.createMonitorInputConsumer(token, name, inputChannel);
    }

    @Override // android.view.IColorWindowManager
    public boolean destroyMonitorInputConsumer(String name) throws RemoteException {
        return this.mColorWm.destroyMonitorInputConsumer(name);
    }

    @Override // android.view.IColorWindowManager
    public void pilferPointers(String name) throws RemoteException {
        this.mColorWm.pilferPointers(name);
    }

    @Override // android.view.IColorLongshotWindowManager
    public void getFocusedWindowFrame(Rect frame) throws RemoteException {
        this.mColorLongshot.getFocusedWindowFrame(frame);
    }

    @Override // android.view.IColorLongshotWindowManager
    public int getLongshotSurfaceLayer() throws RemoteException {
        return this.mColorLongshot.getLongshotSurfaceLayer();
    }

    @Override // android.view.IColorLongshotWindowManager
    public int getLongshotSurfaceLayerByType(int type) throws RemoteException {
        return this.mColorLongshot.getLongshotSurfaceLayerByType(type);
    }

    @Override // android.view.IColorLongshotWindowManager
    public void longshotNotifyConnected(boolean isConnected) throws RemoteException {
        this.mColorLongshot.longshotNotifyConnected(isConnected);
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isNavigationBarVisible() throws RemoteException {
        return this.mColorLongshot.isNavigationBarVisible();
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isShortcutsPanelShow() throws RemoteException {
        return this.mColorLongshot.isShortcutsPanelShow();
    }

    @Override // android.view.IColorLongshotWindowManager
    public void longshotInjectInput(InputEvent event, int mode) throws RemoteException {
        this.mColorLongshot.longshotInjectInput(event, mode);
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isKeyguardShowingAndNotOccluded() throws RemoteException {
        return this.mColorLongshot.isKeyguardShowingAndNotOccluded();
    }

    @Override // android.view.IColorLongshotWindowManager
    public void longshotInjectInputBegin() throws RemoteException {
        this.mColorLongshot.longshotInjectInputBegin();
    }

    @Override // android.view.IColorLongshotWindowManager
    public void longshotInjectInputEnd() throws RemoteException {
        this.mColorLongshot.longshotInjectInputEnd();
    }

    @Override // android.view.IColorLongshotWindowManager
    public IBinder getLongshotWindowByType(int type) throws RemoteException {
        return this.mColorLongshot.getLongshotWindowByType(type);
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isVolumeShow() throws RemoteException {
        return this.mColorLongshot.isVolumeShow();
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isFloatAssistExpand() throws RemoteException {
        return this.mColorLongshot.isFloatAssistExpand();
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isEdgePanelExpand() throws RemoteException {
        return this.mColorLongshot.isEdgePanelExpand();
    }

    @Override // android.view.IColorDirectWindowManager
    public void directFindCmd(ColorDirectFindCmd findCmd) throws RemoteException {
        this.mColorDirect.directFindCmd(findCmd);
    }

    @Override // android.view.IColorWindowManager
    public boolean isActivityNeedPalette(String pkg, String activityName) throws RemoteException {
        return this.mColorWm.isActivityNeedPalette(pkg, activityName);
    }

    @Override // android.view.IColorWindowManager
    public int getNavBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
        return this.mColorWm.getNavBarColorFromAdaptation(pkg, activityName);
    }

    @Override // android.view.IColorWindowManager
    public int getStatusBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
        return this.mColorWm.getStatusBarColorFromAdaptation(pkg, activityName);
    }

    @Override // android.view.IColorWindowManager
    public int getImeBgColorFromAdaptation(String pkg) throws RemoteException {
        return this.mColorWm.getImeBgColorFromAdaptation(pkg);
    }

    @Override // android.view.IColorWindowManager
    public int getTypedWindowLayer(int type) throws RemoteException {
        return this.mColorWm.getTypedWindowLayer(type);
    }

    @Override // android.view.IColorWindowManager
    public int getFocusedWindowIgnoreHomeMenuKey() throws RemoteException {
        return this.mColorWm.getFocusedWindowIgnoreHomeMenuKey();
    }

    @Override // android.view.IColorWindowManager
    public void registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) throws RemoteException {
        this.mColorWm.registerOnUiModeConfigurationChangeFinishListener(listener);
    }

    @Override // android.view.IColorWindowManager
    public void unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) throws RemoteException {
        this.mColorWm.unregisterOnUiModeConfigurationChangeFinishListener(listener);
    }

    @Override // android.view.IColorWindowManager
    public void setBootAnimationRotationLock(boolean lockRotation) throws RemoteException {
        this.mColorWm.setBootAnimationRotationLock(lockRotation);
    }
}
