package android.view;

import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import com.color.screenshot.ColorLongshotDump;
import com.color.util.ColorLog;
import com.color.util.ColorTypeCastingHelper;
import com.color.view.analysis.ColorWindowNode;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class ColorLongshotViewHelper implements IColorLongshotViewHelper {
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String TAG = "LongshotDump";
    private final ColorLongshotViewDump mDump = new ColorLongshotViewDump();
    private final H mHandler = new H(this.mViewAncestor, this.mDump);
    private final WeakReference<ViewRootImpl> mViewAncestor;

    public ColorLongshotViewHelper(WeakReference<ViewRootImpl> viewAncestor) {
        this.mViewAncestor = viewAncestor;
    }

    public void longshotNotifyConnected(boolean isConnected) {
        ViewRootImpl viewAncestor = this.mViewAncestor.get();
        if (viewAncestor != null && viewAncestor.mView != null) {
            ((IColorBaseViewRoot) ColorTypeCastingHelper.typeCasting(IColorBaseViewRoot.class, viewAncestor)).getColorViewRootImplHooks().getLongshotViewRoot().setConnected(isConnected);
            if (!isConnected) {
                this.mDump.reset();
            }
        }
    }

    public void longshotDump(FileDescriptor fd, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        try {
            sendMessage(1, new DumpInfoData(ParcelFileDescriptor.dup(fd), systemWindows, floatWindows, true), 0, 0, true);
        } catch (IOException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "longshotDump failed : " + e);
        }
    }

    public ColorWindowNode longshotCollectWindow(boolean isStatusBar, boolean isNavigationBar) {
        ViewRootImpl viewAncestor = this.mViewAncestor.get();
        if (viewAncestor == null || viewAncestor.mView == null) {
            return null;
        }
        return this.mDump.collectWindow(viewAncestor.mView, isStatusBar, isNavigationBar);
    }

    public void longshotInjectInput(InputEvent event, int mode) {
        ViewRootImpl viewAncestor = this.mViewAncestor.get();
        if (viewAncestor != null && viewAncestor.mView != null) {
            viewAncestor.dispatchInputEvent(event, viewAncestor.mInputEventReceiver);
        }
    }

    public void longshotInjectInputBegin() {
        ViewRootImpl viewAncestor = this.mViewAncestor.get();
        if (viewAncestor != null && viewAncestor.mView != null) {
            this.mDump.injectInputBegin();
        }
    }

    public void longshotInjectInputEnd() {
        ViewRootImpl viewAncestor = this.mViewAncestor.get();
        if (viewAncestor != null && viewAncestor.mView != null) {
            this.mDump.injectInputEnd();
        }
    }

    public void screenshotDump(FileDescriptor fd) {
        try {
            sendMessage(1, new DumpInfoData(ParcelFileDescriptor.dup(fd), null, null, false), 0, 0, true);
        } catch (IOException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "screenshotDump failed : " + e);
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        List<ColorWindowNode> systemWindows;
        List<ColorWindowNode> floatWindows;
        InputEvent event;
        boolean isConnected = false;
        switch (code) {
            case 10002:
                data.enforceInterface("android.view.IWindow");
                if (1 == data.readInt()) {
                    isConnected = true;
                }
                longshotNotifyConnected(isConnected);
                return true;
            case 10003:
                data.enforceInterface("android.view.IWindow");
                ParcelFileDescriptor fd = data.readFileDescriptor();
                if (fd != null) {
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
                data.enforceInterface("android.view.IWindow");
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
                data.enforceInterface("android.view.IWindow");
                if (data.readInt() != 0) {
                    event = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                } else {
                    event = null;
                }
                longshotInjectInput(event, data.readInt());
                return true;
            case 10006:
                data.enforceInterface("android.view.IWindow");
                longshotInjectInputBegin();
                return true;
            case 10007:
                data.enforceInterface("android.view.IWindow");
                longshotInjectInputEnd();
                return true;
            case 10008:
            default:
                return false;
            case 10009:
                data.enforceInterface("android.view.IWindow");
                ParcelFileDescriptor fd2 = data.readFileDescriptor();
                if (fd2 != null) {
                    screenshotDump(fd2.getFileDescriptor());
                    try {
                        fd2.close();
                    } catch (IOException e2) {
                    }
                }
                return true;
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

    /* access modifiers changed from: private */
    public static final class DumpInfoData {
        private final List<ColorWindowNode> mFloatWindows;
        private final boolean mIsLongshot;
        private final ParcelFileDescriptor mParcelFileDescriptor;
        private final List<ColorWindowNode> mSystemWindows;

        public DumpInfoData(ParcelFileDescriptor fd, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows, boolean isLongshot) {
            this.mParcelFileDescriptor = fd;
            this.mSystemWindows = systemWindows;
            this.mFloatWindows = floatWindows;
            this.mIsLongshot = isLongshot;
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

        public boolean isLongshot() {
            return this.mIsLongshot;
        }
    }

    /* access modifiers changed from: private */
    public static class H extends Handler {
        public static final int MSG_DUMP_VIEW_HIERARCHY = 1;
        private final WeakReference<ColorLongshotViewDump> mDump;
        private final WeakReference<ViewRootImpl> mViewAncestor;

        public H(WeakReference<ViewRootImpl> viewAncestor, ColorLongshotViewDump dump) {
            this.mViewAncestor = viewAncestor;
            this.mDump = new WeakReference<>(dump);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ViewRootImpl viewAncestor = this.mViewAncestor.get();
                ColorLongshotViewDump dump = this.mDump.get();
                if (viewAncestor == null || dump == null) {
                    ColorLog.e(ColorLongshotViewHelper.DBG, ColorLongshotViewHelper.TAG, "longshotDump : viewAncestor is null");
                } else if (viewAncestor.mView != null) {
                    DumpInfoData data = (DumpInfoData) msg.obj;
                    dump.dumpViewRoot(viewAncestor, data.getParcelFileDescriptor(), data.getSystemWindows(), data.getFloatWindows(), data.isLongshot());
                } else {
                    ColorLog.e(ColorLongshotViewHelper.DBG, ColorLongshotViewHelper.TAG, "longshotDump : viewAncestor.mView is null");
                }
            }
        }
    }
}
