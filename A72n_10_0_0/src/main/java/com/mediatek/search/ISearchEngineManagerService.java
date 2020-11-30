package com.mediatek.search;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.common.search.SearchEngine;
import java.util.List;

public interface ISearchEngineManagerService extends IInterface {
    List<SearchEngine> getAvailables() throws RemoteException;

    SearchEngine getBestMatch(String str, String str2) throws RemoteException;

    SearchEngine getDefault() throws RemoteException;

    SearchEngine getSearchEngine(int i, String str) throws RemoteException;

    boolean setDefault(SearchEngine searchEngine) throws RemoteException;

    public static class Default implements ISearchEngineManagerService {
        @Override // com.mediatek.search.ISearchEngineManagerService
        public List<SearchEngine> getAvailables() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.search.ISearchEngineManagerService
        public SearchEngine getDefault() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.search.ISearchEngineManagerService
        public SearchEngine getBestMatch(String name, String favicon) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.search.ISearchEngineManagerService
        public SearchEngine getSearchEngine(int field, String value) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.search.ISearchEngineManagerService
        public boolean setDefault(SearchEngine engine) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISearchEngineManagerService {
        private static final String DESCRIPTOR = "com.mediatek.search.ISearchEngineManagerService";
        static final int TRANSACTION_getAvailables = 1;
        static final int TRANSACTION_getBestMatch = 3;
        static final int TRANSACTION_getDefault = 2;
        static final int TRANSACTION_getSearchEngine = 4;
        static final int TRANSACTION_setDefault = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISearchEngineManagerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISearchEngineManagerService)) {
                return new Proxy(obj);
            }
            return (ISearchEngineManagerService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SearchEngine _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                List<SearchEngine> _result = getAvailables();
                reply.writeNoException();
                reply.writeTypedList(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                SearchEngine _result2 = getDefault();
                reply.writeNoException();
                if (_result2 != null) {
                    reply.writeInt(1);
                    _result2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                SearchEngine _result3 = getBestMatch(data.readString(), data.readString());
                reply.writeNoException();
                if (_result3 != null) {
                    reply.writeInt(1);
                    _result3.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                SearchEngine _result4 = getSearchEngine(data.readInt(), data.readString());
                reply.writeNoException();
                if (_result4 != null) {
                    reply.writeInt(1);
                    _result4.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (SearchEngine) SearchEngine.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                boolean z = setDefault(_arg0);
                reply.writeNoException();
                reply.writeInt(z ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISearchEngineManagerService {
            public static ISearchEngineManagerService sDefaultImpl;
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

            @Override // com.mediatek.search.ISearchEngineManagerService
            public List<SearchEngine> getAvailables() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAvailables();
                    }
                    _reply.readException();
                    List<SearchEngine> _result = _reply.createTypedArrayList(SearchEngine.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.search.ISearchEngineManagerService
            public SearchEngine getDefault() throws RemoteException {
                SearchEngine _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefault();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (SearchEngine) SearchEngine.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.search.ISearchEngineManagerService
            public SearchEngine getBestMatch(String name, String favicon) throws RemoteException {
                SearchEngine _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeString(favicon);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBestMatch(name, favicon);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (SearchEngine) SearchEngine.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.search.ISearchEngineManagerService
            public SearchEngine getSearchEngine(int field, String value) throws RemoteException {
                SearchEngine _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(field);
                    _data.writeString(value);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSearchEngine(field, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (SearchEngine) SearchEngine.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.search.ISearchEngineManagerService
            public boolean setDefault(SearchEngine engine) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (engine != null) {
                        _data.writeInt(1);
                        engine.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDefault(engine);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISearchEngineManagerService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISearchEngineManagerService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
