package com.mediatek.common.multiwindow;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.common.multiwindow.IMWAmsCallback;
import com.mediatek.common.multiwindow.IMWSystemUiCallback;
import com.mediatek.common.multiwindow.IMWWmsCallback;
import java.util.List;

public interface IMultiWindowManager extends IInterface {
    void activityCreated(IBinder iBinder) throws RemoteException;

    void addConfigNotChangePkg(String str) throws RemoteException;

    void addDisableFloatPkg(String str) throws RemoteException;

    void addMiniMaxRestartPkg(String str) throws RemoteException;

    int appErrorHandling(String str, boolean z, boolean z2) throws RemoteException;

    void closeWindow(IBinder iBinder) throws RemoteException;

    void enableFocusedFrame(boolean z) throws RemoteException;

    List<String> getDisableFloatComponentList() throws RemoteException;

    List<String> getDisableFloatPkgList() throws RemoteException;

    boolean isFloatingStack(int i) throws RemoteException;

    boolean isInMiniMax(int i) throws RemoteException;

    boolean isStickStack(int i) throws RemoteException;

    boolean isSticky(IBinder iBinder) throws RemoteException;

    boolean matchConfigChangeList(String str) throws RemoteException;

    boolean matchConfigNotChangeList(String str) throws RemoteException;

    boolean matchDisableFloatActivityList(String str) throws RemoteException;

    boolean matchDisableFloatPkgList(String str) throws RemoteException;

    boolean matchDisableFloatWinList(String str) throws RemoteException;

    boolean matchMinimaxRestartList(String str) throws RemoteException;

    void miniMaxTask(int i) throws RemoteException;

    void moveActivityTaskToFront(IBinder iBinder) throws RemoteException;

    void moveFloatingWindow(int i, int i2) throws RemoteException;

    void resizeFloatingWindow(int i, int i2, int i3) throws RemoteException;

    void restoreWindow(IBinder iBinder, boolean z) throws RemoteException;

    void setAMSCallback(IMWAmsCallback iMWAmsCallback) throws RemoteException;

    void setFloatingStack(int i) throws RemoteException;

    void setSystemUiCallback(IMWSystemUiCallback iMWSystemUiCallback) throws RemoteException;

    void setWMSCallback(IMWWmsCallback iMWWmsCallback) throws RemoteException;

    void showRestoreButton(boolean z) throws RemoteException;

    void stickWindow(IBinder iBinder, boolean z) throws RemoteException;

    void taskAdded(int i) throws RemoteException;

    void taskRemoved(int i) throws RemoteException;

    public static class Default implements IMultiWindowManager {
        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void moveActivityTaskToFront(IBinder token) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void closeWindow(IBinder token) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void restoreWindow(IBinder token, boolean toMax) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void setAMSCallback(IMWAmsCallback cb) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void setSystemUiCallback(IMWSystemUiCallback cb) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void stickWindow(IBinder token, boolean isSticky) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean isFloatingStack(int stackId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void setFloatingStack(int stackId) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void setWMSCallback(IMWWmsCallback cb) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean isStickStack(int stackId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean isInMiniMax(int taskId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void moveFloatingWindow(int disX, int disY) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void resizeFloatingWindow(int direction, int deltaX, int deltaY) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void enableFocusedFrame(boolean enable) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void miniMaxTask(int taskId) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void showRestoreButton(boolean flag) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean matchConfigNotChangeList(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean matchDisableFloatPkgList(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean matchDisableFloatActivityList(String ActivityName) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean matchDisableFloatWinList(String winName) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public List<String> getDisableFloatPkgList() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public List<String> getDisableFloatComponentList() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean matchMinimaxRestartList(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean matchConfigChangeList(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void addDisableFloatPkg(String packageName) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void addConfigNotChangePkg(String packageName) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void addMiniMaxRestartPkg(String packageName) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public int appErrorHandling(String packageName, boolean inMaxOrRestore, boolean defaultChangeConfig) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public boolean isSticky(IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void activityCreated(IBinder token) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void taskAdded(int taskId) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMultiWindowManager
        public void taskRemoved(int taskId) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMultiWindowManager {
        private static final String DESCRIPTOR = "com.mediatek.common.multiwindow.IMultiWindowManager";
        static final int TRANSACTION_activityCreated = 30;
        static final int TRANSACTION_addConfigNotChangePkg = 26;
        static final int TRANSACTION_addDisableFloatPkg = 25;
        static final int TRANSACTION_addMiniMaxRestartPkg = 27;
        static final int TRANSACTION_appErrorHandling = 28;
        static final int TRANSACTION_closeWindow = 2;
        static final int TRANSACTION_enableFocusedFrame = 14;
        static final int TRANSACTION_getDisableFloatComponentList = 22;
        static final int TRANSACTION_getDisableFloatPkgList = 21;
        static final int TRANSACTION_isFloatingStack = 7;
        static final int TRANSACTION_isInMiniMax = 11;
        static final int TRANSACTION_isStickStack = 10;
        static final int TRANSACTION_isSticky = 29;
        static final int TRANSACTION_matchConfigChangeList = 24;
        static final int TRANSACTION_matchConfigNotChangeList = 17;
        static final int TRANSACTION_matchDisableFloatActivityList = 19;
        static final int TRANSACTION_matchDisableFloatPkgList = 18;
        static final int TRANSACTION_matchDisableFloatWinList = 20;
        static final int TRANSACTION_matchMinimaxRestartList = 23;
        static final int TRANSACTION_miniMaxTask = 15;
        static final int TRANSACTION_moveActivityTaskToFront = 1;
        static final int TRANSACTION_moveFloatingWindow = 12;
        static final int TRANSACTION_resizeFloatingWindow = 13;
        static final int TRANSACTION_restoreWindow = 3;
        static final int TRANSACTION_setAMSCallback = 4;
        static final int TRANSACTION_setFloatingStack = 8;
        static final int TRANSACTION_setSystemUiCallback = 5;
        static final int TRANSACTION_setWMSCallback = 9;
        static final int TRANSACTION_showRestoreButton = 16;
        static final int TRANSACTION_stickWindow = 6;
        static final int TRANSACTION_taskAdded = 31;
        static final int TRANSACTION_taskRemoved = 32;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMultiWindowManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMultiWindowManager)) {
                return new Proxy(obj);
            }
            return (IMultiWindowManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg1 = false;
                boolean _arg2 = false;
                boolean _arg0 = false;
                boolean _arg02 = false;
                boolean _arg12 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        moveActivityTaskToFront(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        closeWindow(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg03 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        restoreWindow(_arg03, _arg1);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setAMSCallback(IMWAmsCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setSystemUiCallback(IMWSystemUiCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        stickWindow(_arg04, _arg12);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFloatingStack = isFloatingStack(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isFloatingStack ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setFloatingStack(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        setWMSCallback(IMWWmsCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isStickStack = isStickStack(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isStickStack ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInMiniMax = isInMiniMax(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isInMiniMax ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        moveFloatingWindow(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        resizeFloatingWindow(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        enableFocusedFrame(_arg02);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        miniMaxTask(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        showRestoreButton(_arg0);
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean matchConfigNotChangeList = matchConfigNotChangeList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(matchConfigNotChangeList ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean matchDisableFloatPkgList = matchDisableFloatPkgList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(matchDisableFloatPkgList ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean matchDisableFloatActivityList = matchDisableFloatActivityList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(matchDisableFloatActivityList ? 1 : 0);
                        return true;
                    case TRANSACTION_matchDisableFloatWinList /*{ENCODED_INT: 20}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean matchDisableFloatWinList = matchDisableFloatWinList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(matchDisableFloatWinList ? 1 : 0);
                        return true;
                    case TRANSACTION_getDisableFloatPkgList /*{ENCODED_INT: 21}*/:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getDisableFloatPkgList();
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case TRANSACTION_getDisableFloatComponentList /*{ENCODED_INT: 22}*/:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getDisableFloatComponentList();
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case TRANSACTION_matchMinimaxRestartList /*{ENCODED_INT: 23}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean matchMinimaxRestartList = matchMinimaxRestartList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(matchMinimaxRestartList ? 1 : 0);
                        return true;
                    case TRANSACTION_matchConfigChangeList /*{ENCODED_INT: 24}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean matchConfigChangeList = matchConfigChangeList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(matchConfigChangeList ? 1 : 0);
                        return true;
                    case TRANSACTION_addDisableFloatPkg /*{ENCODED_INT: 25}*/:
                        data.enforceInterface(DESCRIPTOR);
                        addDisableFloatPkg(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_addConfigNotChangePkg /*{ENCODED_INT: 26}*/:
                        data.enforceInterface(DESCRIPTOR);
                        addConfigNotChangePkg(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_addMiniMaxRestartPkg /*{ENCODED_INT: 27}*/:
                        data.enforceInterface(DESCRIPTOR);
                        addMiniMaxRestartPkg(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_appErrorHandling /*{ENCODED_INT: 28}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        boolean _arg13 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        int _result3 = appErrorHandling(_arg05, _arg13, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case TRANSACTION_isSticky /*{ENCODED_INT: 29}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSticky = isSticky(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isSticky ? 1 : 0);
                        return true;
                    case TRANSACTION_activityCreated /*{ENCODED_INT: 30}*/:
                        data.enforceInterface(DESCRIPTOR);
                        activityCreated(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_taskAdded /*{ENCODED_INT: 31}*/:
                        data.enforceInterface(DESCRIPTOR);
                        taskAdded(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_taskRemoved /*{ENCODED_INT: 32}*/:
                        data.enforceInterface(DESCRIPTOR);
                        taskRemoved(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMultiWindowManager {
            public static IMultiWindowManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void moveActivityTaskToFront(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveActivityTaskToFront(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void closeWindow(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeWindow(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void restoreWindow(IBinder token, boolean toMax) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(toMax ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreWindow(token, toMax);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void setAMSCallback(IMWAmsCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAMSCallback(cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void setSystemUiCallback(IMWSystemUiCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSystemUiCallback(cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void stickWindow(IBinder token, boolean isSticky) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(isSticky ? 1 : 0);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stickWindow(token, isSticky);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean isFloatingStack(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFloatingStack(stackId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void setFloatingStack(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFloatingStack(stackId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void setWMSCallback(IMWWmsCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWMSCallback(cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean isStickStack(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(stackId);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isStickStack(stackId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean isInMiniMax(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInMiniMax(taskId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void moveFloatingWindow(int disX, int disY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disX);
                    _data.writeInt(disY);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveFloatingWindow(disX, disY);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void resizeFloatingWindow(int direction, int deltaX, int deltaY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(direction);
                    _data.writeInt(deltaX);
                    _data.writeInt(deltaY);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resizeFloatingWindow(direction, deltaX, deltaY);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void enableFocusedFrame(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableFocusedFrame(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void miniMaxTask(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().miniMaxTask(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void showRestoreButton(boolean flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag ? 1 : 0);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showRestoreButton(flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean matchConfigNotChangeList(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().matchConfigNotChangeList(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean matchDisableFloatPkgList(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().matchDisableFloatPkgList(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean matchDisableFloatActivityList(String ActivityName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ActivityName);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().matchDisableFloatActivityList(ActivityName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean matchDisableFloatWinList(String winName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(winName);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_matchDisableFloatWinList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().matchDisableFloatWinList(winName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public List<String> getDisableFloatPkgList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getDisableFloatPkgList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisableFloatPkgList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public List<String> getDisableFloatComponentList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getDisableFloatComponentList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisableFloatComponentList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean matchMinimaxRestartList(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_matchMinimaxRestartList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().matchMinimaxRestartList(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean matchConfigChangeList(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_matchConfigChangeList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().matchConfigChangeList(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void addDisableFloatPkg(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(Stub.TRANSACTION_addDisableFloatPkg, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDisableFloatPkg(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void addConfigNotChangePkg(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(Stub.TRANSACTION_addConfigNotChangePkg, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addConfigNotChangePkg(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void addMiniMaxRestartPkg(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(Stub.TRANSACTION_addMiniMaxRestartPkg, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addMiniMaxRestartPkg(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public int appErrorHandling(String packageName, boolean inMaxOrRestore, boolean defaultChangeConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    int i = 1;
                    _data.writeInt(inMaxOrRestore ? 1 : 0);
                    if (!defaultChangeConfig) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(Stub.TRANSACTION_appErrorHandling, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().appErrorHandling(packageName, inMaxOrRestore, defaultChangeConfig);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public boolean isSticky(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isSticky, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSticky(token);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void activityCreated(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(Stub.TRANSACTION_activityCreated, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().activityCreated(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void taskAdded(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(Stub.TRANSACTION_taskAdded, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().taskAdded(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMultiWindowManager
            public void taskRemoved(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(Stub.TRANSACTION_taskRemoved, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().taskRemoved(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMultiWindowManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMultiWindowManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
