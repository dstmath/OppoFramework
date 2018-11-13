package android.view;

import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import com.color.screenshot.ColorLongshotDump;
import com.color.util.ColorLog;
import com.color.view.analysis.ColorWindowNode;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class ColorLongshotViewHelper implements IColorLongshotWindow {
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String TAG = "LongshotDump";
    private final ColorLongshotViewDump mDump = new ColorLongshotViewDump();
    private final H mHandler;
    private final WeakReference<ViewRootImpl> mViewAncestor;

    private static final class DumpInfoData {
        private final List<ColorWindowNode> mFloatWindows;
        private final ParcelFileDescriptor mParcelFileDescriptor;
        private final List<ColorWindowNode> mSystemWindows;

        public DumpInfoData(ParcelFileDescriptor fd, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
            this.mParcelFileDescriptor = fd;
            this.mSystemWindows = systemWindows;
            this.mFloatWindows = floatWindows;
        }

        public ParcelFileDescriptor getParcelFileDescriptor() {
            return this.mParcelFileDescriptor;
        }

        public List<ColorWindowNode> getSystemWindows() {
            return this.mSystemWindows;
        }

        public List<ColorWindowNode> getFloatWindows() {
            return this.mFloatWindows;
        }
    }

    private class H extends Handler {
        public static final int MSG_DUMP_VIEW_HIERARCHY = 1;
        private final WeakReference<ViewRootImpl> mViewAncestor;

        public H(WeakReference<ViewRootImpl> viewAncestor) {
            this.mViewAncestor = viewAncestor;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
                    if (viewAncestor == null) {
                        ColorLog.e(ColorLongshotViewHelper.DBG, "LongshotDump", "longshotDump : viewAncestor is null");
                        return;
                    } else if (viewAncestor.mView != null) {
                        DumpInfoData data = msg.obj;
                        ColorLongshotViewHelper.this.mDump.dumpViewRoot(viewAncestor, data.getParcelFileDescriptor(), data.getSystemWindows(), data.getFloatWindows());
                        return;
                    } else {
                        ColorLog.e(ColorLongshotViewHelper.DBG, "LongshotDump", "longshotDump : viewAncestor.mView is null");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public ColorLongshotViewHelper(WeakReference<ViewRootImpl> viewAncestor) {
        this.mViewAncestor = viewAncestor;
        this.mHandler = new H(this.mViewAncestor);
    }

    public void longshotNotifyConnected(boolean isConnected) {
        ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
        if (viewAncestor != null && viewAncestor.mView != null) {
            viewAncestor.mViewRootHooks.getLongshotViewRoot().setConnected(isConnected);
            if (!isConnected) {
                this.mDump.reset();
            }
        }
    }

    public void longshotDump(FileDescriptor fd, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        try {
            sendMessage(1, new DumpInfoData(ParcelFileDescriptor.dup(fd), systemWindows, floatWindows), 0, 0, true);
        } catch (IOException e) {
            ColorLog.e(DBG, "LongshotDump", "dumpViewHierarchy failed : " + e);
        }
    }

    public ColorWindowNode longshotCollectWindow(boolean isStatusBar, boolean isNavigationBar) {
        ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
        if (viewAncestor == null || viewAncestor.mView == null) {
            return null;
        }
        return this.mDump.collectWindow(viewAncestor.mView, isStatusBar, isNavigationBar);
    }

    public void longshotInjectInput(InputEvent event, int mode) {
        ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
        if (viewAncestor != null && viewAncestor.mView != null) {
            viewAncestor.dispatchInputEvent(event, viewAncestor.mInputEventReceiver);
        }
    }

    public void longshotInjectInputBegin() {
        ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
        if (viewAncestor != null && viewAncestor.mView != null) {
            this.mDump.injectInputBegin();
        }
    }

    public void longshotInjectInputEnd() {
        ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
        if (viewAncestor != null && viewAncestor.mView != null) {
            this.mDump.injectInputEnd();
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        switch (code) {
            case 10002:
                data.enforceInterface(IColorWindow.DESCRIPTOR);
                longshotNotifyConnected(1 == data.readInt());
                return true;
            case 10003:
                data.enforceInterface(IColorWindow.DESCRIPTOR);
                ParcelFileDescriptor fd = data.readFileDescriptor();
                if (fd != null) {
                    List systemWindows;
                    List floatWindows;
                    if (1 == data.readInt()) {
                        systemWindows = data.createTypedArrayList(ColorWindowNode.CREATOR);
                    } else {
                        systemWindows = null;
                    }
                    if (1 == data.readInt()) {
                        floatWindows = data.createTypedArrayList(ColorWindowNode.CREATOR);
                    } else {
                        floatWindows = null;
                    }
                    longshotDump(fd.getFileDescriptor(), systemWindows, floatWindows);
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
                return true;
            case 10004:
                data.enforceInterface(IColorWindow.DESCRIPTOR);
                ColorWindowNode result = longshotCollectWindow(1 == data.readInt(), 1 == data.readInt());
                reply.writeNoException();
                if (result != null) {
                    reply.writeInt(1);
                    result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 10005:
                InputEvent event;
                data.enforceInterface(IColorWindow.DESCRIPTOR);
                if (data.readInt() != 0) {
                    event = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                } else {
                    event = null;
                }
                longshotInjectInput(event, data.readInt());
                return true;
            case 10006:
                data.enforceInterface(IColorWindow.DESCRIPTOR);
                longshotInjectInputBegin();
                return true;
            case 10007:
                data.enforceInterface(IColorWindow.DESCRIPTOR);
                longshotInjectInputEnd();
                return true;
            default:
                return false;
        }
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = this.mHandler.obtainMessage(what);
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        this.mHandler.sendMessage(msg);
    }
}
