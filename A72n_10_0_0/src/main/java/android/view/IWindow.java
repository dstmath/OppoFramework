package android.view;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.view.DisplayCutout;
import com.android.internal.os.IResultReceiver;

public interface IWindow extends IInterface {
    void closeSystemDialogs(String str) throws RemoteException;

    void dispatchAppVisibility(boolean z) throws RemoteException;

    void dispatchDragEvent(DragEvent dragEvent) throws RemoteException;

    void dispatchFreeformChanged(boolean z) throws RemoteException;

    void dispatchGetNewSurface() throws RemoteException;

    void dispatchPointerCaptureChanged(boolean z) throws RemoteException;

    void dispatchSystemUiVisibilityChanged(int i, int i2, int i3, int i4) throws RemoteException;

    void dispatchWallpaperCommand(String str, int i, int i2, int i3, Bundle bundle, boolean z) throws RemoteException;

    void dispatchWallpaperOffsets(float f, float f2, float f3, float f4, boolean z) throws RemoteException;

    void dispatchWindowShown() throws RemoteException;

    void executeCommand(String str, String str2, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void insetsChanged(InsetsState insetsState) throws RemoteException;

    void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] insetsSourceControlArr) throws RemoteException;

    void moved(int i, int i2) throws RemoteException;

    void requestAppKeyboardShortcuts(IResultReceiver iResultReceiver, int i) throws RemoteException;

    void resized(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, Rect rect6, boolean z, MergedConfiguration mergedConfiguration, Rect rect7, boolean z2, boolean z3, int i, DisplayCutout.ParcelableWrapper parcelableWrapper) throws RemoteException;

    void updatePointerIcon(float f, float f2) throws RemoteException;

    void windowFocusChanged(boolean z, boolean z2) throws RemoteException;

    public static class Default implements IWindow {
        @Override // android.view.IWindow
        public void executeCommand(String command, String parameters, ParcelFileDescriptor descriptor) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration newMergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void insetsChanged(InsetsState insetsState) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] activeControls) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void moved(int newX, int newY) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchAppVisibility(boolean visible) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchGetNewSurface() throws RemoteException {
        }

        @Override // android.view.IWindow
        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void closeSystemDialogs(String reason) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchDragEvent(DragEvent event) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void updatePointerIcon(float x, float y) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchWindowShown() throws RemoteException {
        }

        @Override // android.view.IWindow
        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchPointerCaptureChanged(boolean hasCapture) throws RemoteException {
        }

        @Override // android.view.IWindow
        public void dispatchFreeformChanged(boolean hasFreeform) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWindow {
        private static final String DESCRIPTOR = "android.view.IWindow";
        static final int TRANSACTION_closeSystemDialogs = 9;
        static final int TRANSACTION_dispatchAppVisibility = 6;
        static final int TRANSACTION_dispatchDragEvent = 12;
        static final int TRANSACTION_dispatchFreeformChanged = 18;
        static final int TRANSACTION_dispatchGetNewSurface = 7;
        static final int TRANSACTION_dispatchPointerCaptureChanged = 17;
        static final int TRANSACTION_dispatchSystemUiVisibilityChanged = 14;
        static final int TRANSACTION_dispatchWallpaperCommand = 11;
        static final int TRANSACTION_dispatchWallpaperOffsets = 10;
        static final int TRANSACTION_dispatchWindowShown = 15;
        static final int TRANSACTION_executeCommand = 1;
        static final int TRANSACTION_insetsChanged = 3;
        static final int TRANSACTION_insetsControlChanged = 4;
        static final int TRANSACTION_moved = 5;
        static final int TRANSACTION_requestAppKeyboardShortcuts = 16;
        static final int TRANSACTION_resized = 2;
        static final int TRANSACTION_updatePointerIcon = 13;
        static final int TRANSACTION_windowFocusChanged = 8;

        public Stub() {
            attachInterface(this, "android.view.IWindow");
        }

        public static IWindow asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("android.view.IWindow");
            if (iin == null || !(iin instanceof IWindow)) {
                return new Proxy(obj);
            }
            return (IWindow) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "executeCommand";
                case 2:
                    return "resized";
                case 3:
                    return "insetsChanged";
                case 4:
                    return "insetsControlChanged";
                case 5:
                    return "moved";
                case 6:
                    return "dispatchAppVisibility";
                case 7:
                    return "dispatchGetNewSurface";
                case 8:
                    return "windowFocusChanged";
                case 9:
                    return "closeSystemDialogs";
                case 10:
                    return "dispatchWallpaperOffsets";
                case 11:
                    return "dispatchWallpaperCommand";
                case 12:
                    return "dispatchDragEvent";
                case 13:
                    return "updatePointerIcon";
                case 14:
                    return "dispatchSystemUiVisibilityChanged";
                case 15:
                    return "dispatchWindowShown";
                case 16:
                    return "requestAppKeyboardShortcuts";
                case 17:
                    return "dispatchPointerCaptureChanged";
                case 18:
                    return "dispatchFreeformChanged";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor _arg2;
            Rect _arg0;
            Rect _arg1;
            Rect _arg22;
            Rect _arg3;
            Rect _arg4;
            Rect _arg5;
            MergedConfiguration _arg7;
            Rect _arg8;
            DisplayCutout.ParcelableWrapper _arg12;
            InsetsState _arg02;
            InsetsState _arg03;
            Bundle _arg42;
            DragEvent _arg04;
            if (code != 1598968902) {
                boolean _arg05 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface("android.view.IWindow");
                        String _arg06 = data.readString();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        executeCommand(_arg06, _arg13, _arg2);
                        return true;
                    case 2:
                        data.enforceInterface("android.view.IWindow");
                        if (data.readInt() != 0) {
                            _arg0 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg5 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        boolean _arg6 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg7 = MergedConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg7 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg8 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg8 = null;
                        }
                        boolean _arg9 = data.readInt() != 0;
                        boolean _arg10 = data.readInt() != 0;
                        int _arg11 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = DisplayCutout.ParcelableWrapper.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        resized(_arg0, _arg1, _arg22, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, _arg10, _arg11, _arg12);
                        return true;
                    case 3:
                        data.enforceInterface("android.view.IWindow");
                        if (data.readInt() != 0) {
                            _arg02 = InsetsState.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        insetsChanged(_arg02);
                        return true;
                    case 4:
                        data.enforceInterface("android.view.IWindow");
                        if (data.readInt() != 0) {
                            _arg03 = InsetsState.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        insetsControlChanged(_arg03, (InsetsSourceControl[]) data.createTypedArray(InsetsSourceControl.CREATOR));
                        return true;
                    case 5:
                        data.enforceInterface("android.view.IWindow");
                        moved(data.readInt(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface("android.view.IWindow");
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        dispatchAppVisibility(_arg05);
                        return true;
                    case 7:
                        data.enforceInterface("android.view.IWindow");
                        dispatchGetNewSurface();
                        return true;
                    case 8:
                        data.enforceInterface("android.view.IWindow");
                        boolean _arg07 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        windowFocusChanged(_arg07, _arg05);
                        return true;
                    case 9:
                        data.enforceInterface("android.view.IWindow");
                        closeSystemDialogs(data.readString());
                        return true;
                    case 10:
                        data.enforceInterface("android.view.IWindow");
                        dispatchWallpaperOffsets(data.readFloat(), data.readFloat(), data.readFloat(), data.readFloat(), data.readInt() != 0);
                        return true;
                    case 11:
                        data.enforceInterface("android.view.IWindow");
                        String _arg08 = data.readString();
                        int _arg14 = data.readInt();
                        int _arg23 = data.readInt();
                        int _arg32 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg42 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        dispatchWallpaperCommand(_arg08, _arg14, _arg23, _arg32, _arg42, data.readInt() != 0);
                        return true;
                    case 12:
                        data.enforceInterface("android.view.IWindow");
                        if (data.readInt() != 0) {
                            _arg04 = DragEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        dispatchDragEvent(_arg04);
                        return true;
                    case 13:
                        data.enforceInterface("android.view.IWindow");
                        updatePointerIcon(data.readFloat(), data.readFloat());
                        return true;
                    case 14:
                        data.enforceInterface("android.view.IWindow");
                        dispatchSystemUiVisibilityChanged(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 15:
                        data.enforceInterface("android.view.IWindow");
                        dispatchWindowShown();
                        return true;
                    case 16:
                        data.enforceInterface("android.view.IWindow");
                        requestAppKeyboardShortcuts(IResultReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 17:
                        data.enforceInterface("android.view.IWindow");
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        dispatchPointerCaptureChanged(_arg05);
                        return true;
                    case 18:
                        data.enforceInterface("android.view.IWindow");
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        dispatchFreeformChanged(_arg05);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString("android.view.IWindow");
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWindow {
            public static IWindow sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "android.view.IWindow";
            }

            @Override // android.view.IWindow
            public void executeCommand(String command, String parameters, ParcelFileDescriptor descriptor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeString(command);
                    _data.writeString(parameters);
                    if (descriptor != null) {
                        _data.writeInt(1);
                        descriptor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().executeCommand(command, parameters, descriptor);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration newMergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) throws RemoteException {
                Parcel _data;
                Throwable th;
                Parcel _data2 = Parcel.obtain();
                try {
                    _data2.writeInterfaceToken("android.view.IWindow");
                    if (frame != null) {
                        try {
                            _data2.writeInt(1);
                            frame.writeToParcel(_data2, 0);
                        } catch (Throwable th2) {
                            th = th2;
                            _data = _data2;
                        }
                    } else {
                        _data2.writeInt(0);
                    }
                    if (overscanInsets != null) {
                        _data2.writeInt(1);
                        overscanInsets.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (contentInsets != null) {
                        _data2.writeInt(1);
                        contentInsets.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (visibleInsets != null) {
                        _data2.writeInt(1);
                        visibleInsets.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (stableInsets != null) {
                        _data2.writeInt(1);
                        stableInsets.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (outsets != null) {
                        _data2.writeInt(1);
                        outsets.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeInt(reportDraw ? 1 : 0);
                    if (newMergedConfiguration != null) {
                        _data2.writeInt(1);
                        newMergedConfiguration.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (backDropFrame != null) {
                        _data2.writeInt(1);
                        backDropFrame.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeInt(forceLayout ? 1 : 0);
                    _data2.writeInt(alwaysConsumeSystemBars ? 1 : 0);
                    _data2.writeInt(displayId);
                    if (displayCutout != null) {
                        _data2.writeInt(1);
                        displayCutout.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data2, null, 1) || Stub.getDefaultImpl() == null) {
                        _data2.recycle();
                        return;
                    }
                    _data = _data2;
                    try {
                        Stub.getDefaultImpl().resized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, newMergedConfiguration, backDropFrame, forceLayout, alwaysConsumeSystemBars, displayId, displayCutout);
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data = _data2;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.IWindow
            public void insetsChanged(InsetsState insetsState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    if (insetsState != null) {
                        _data.writeInt(1);
                        insetsState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().insetsChanged(insetsState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] activeControls) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    if (insetsState != null) {
                        _data.writeInt(1);
                        insetsState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(activeControls, 0);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().insetsControlChanged(insetsState, activeControls);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void moved(int newX, int newY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeInt(newX);
                    _data.writeInt(newY);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().moved(newX, newY);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void dispatchAppVisibility(boolean visible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeInt(visible ? 1 : 0);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchAppVisibility(visible);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void dispatchGetNewSurface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchGetNewSurface();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    int i = 0;
                    _data.writeInt(hasFocus ? 1 : 0);
                    if (inTouchMode) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().windowFocusChanged(hasFocus, inTouchMode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeString(reason);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().closeSystemDialogs(reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    _data.writeFloat(xStep);
                    _data.writeFloat(yStep);
                    _data.writeInt(sync ? 1 : 0);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchWallpaperOffsets(x, y, xStep, yStep, sync);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    try {
                        _data.writeString(action);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(x);
                        try {
                            _data.writeInt(y);
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(z);
                            int i = 0;
                            if (extras != null) {
                                _data.writeInt(1);
                                extras.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (sync) {
                                i = 1;
                            }
                            _data.writeInt(i);
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().dispatchWallpaperCommand(action, x, y, z, extras, sync);
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.view.IWindow
            public void dispatchDragEvent(DragEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchDragEvent(event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void updatePointerIcon(float x, float y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updatePointerIcon(x, y);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeInt(seq);
                    _data.writeInt(globalVisibility);
                    _data.writeInt(localValue);
                    _data.writeInt(localChanges);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchSystemUiVisibilityChanged(seq, globalVisibility, localValue, localChanges);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void dispatchWindowShown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchWindowShown();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeInt(deviceId);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestAppKeyboardShortcuts(receiver, deviceId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void dispatchPointerCaptureChanged(boolean hasCapture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeInt(hasCapture ? 1 : 0);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchPointerCaptureChanged(hasCapture);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IWindow
            public void dispatchFreeformChanged(boolean hasFreeform) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindow");
                    _data.writeInt(hasFreeform ? 1 : 0);
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchFreeformChanged(hasFreeform);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWindow impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWindow getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
