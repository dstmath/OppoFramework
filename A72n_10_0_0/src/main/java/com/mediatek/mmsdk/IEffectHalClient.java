package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.mediatek.mmsdk.IEffectListener;
import java.util.ArrayList;
import java.util.List;

public interface IEffectHalClient extends IInterface {
    int abort(BaseParameters baseParameters) throws RemoteException;

    int addInputParameter(int i, BaseParameters baseParameters, long j, boolean z) throws RemoteException;

    int addOutputParameter(int i, BaseParameters baseParameters, long j, boolean z) throws RemoteException;

    int configure() throws RemoteException;

    int dequeueAndQueueBuf(long j) throws RemoteException;

    int getCaptureRequirement(BaseParameters baseParameters, List<BaseParameters> list) throws RemoteException;

    int getInputSurfaces(List<Surface> list) throws RemoteException;

    boolean getInputsyncMode(int i) throws RemoteException;

    int getNameVersion(EffectHalVersion effectHalVersion) throws RemoteException;

    boolean getOutputsyncMode(int i) throws RemoteException;

    int init() throws RemoteException;

    int prepare() throws RemoteException;

    int release() throws RemoteException;

    int setBaseParameter(BaseParameters baseParameters) throws RemoteException;

    int setEffectListener(IEffectListener iEffectListener) throws RemoteException;

    int setInputsyncMode(int i, boolean z) throws RemoteException;

    int setOutputSurfaces(List<Surface> list, List<BaseParameters> list2) throws RemoteException;

    int setOutputsyncMode(int i, boolean z) throws RemoteException;

    int setParameter(String str, String str2) throws RemoteException;

    int setParameters(BaseParameters baseParameters) throws RemoteException;

    long start() throws RemoteException;

    int unconfigure() throws RemoteException;

    int uninit() throws RemoteException;

    public static class Default implements IEffectHalClient {
        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int init() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int uninit() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int configure() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int unconfigure() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public long start() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int abort(BaseParameters effectParameter) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int getNameVersion(EffectHalVersion version) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int setEffectListener(IEffectListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int setParameter(String key, String paramValue) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int setParameters(BaseParameters parameter) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int getCaptureRequirement(BaseParameters effectParameter, List<BaseParameters> list) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int prepare() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int release() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int getInputSurfaces(List<Surface> list) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int setOutputSurfaces(List<Surface> list, List<BaseParameters> list2) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int addInputParameter(int index, BaseParameters parameter, long timestamp, boolean repeat) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int addOutputParameter(int index, BaseParameters parameter, long timestamp, boolean repeat) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int setInputsyncMode(int index, boolean sync) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public boolean getInputsyncMode(int index) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int setOutputsyncMode(int index, boolean sync) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public boolean getOutputsyncMode(int index) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int setBaseParameter(BaseParameters parameters) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectHalClient
        public int dequeueAndQueueBuf(long timestamp) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEffectHalClient {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IEffectHalClient";
        static final int TRANSACTION_abort = 6;
        static final int TRANSACTION_addInputParameter = 16;
        static final int TRANSACTION_addOutputParameter = 17;
        static final int TRANSACTION_configure = 3;
        static final int TRANSACTION_dequeueAndQueueBuf = 23;
        static final int TRANSACTION_getCaptureRequirement = 11;
        static final int TRANSACTION_getInputSurfaces = 14;
        static final int TRANSACTION_getInputsyncMode = 19;
        static final int TRANSACTION_getNameVersion = 7;
        static final int TRANSACTION_getOutputsyncMode = 21;
        static final int TRANSACTION_init = 1;
        static final int TRANSACTION_prepare = 12;
        static final int TRANSACTION_release = 13;
        static final int TRANSACTION_setBaseParameter = 22;
        static final int TRANSACTION_setEffectListener = 8;
        static final int TRANSACTION_setInputsyncMode = 18;
        static final int TRANSACTION_setOutputSurfaces = 15;
        static final int TRANSACTION_setOutputsyncMode = 20;
        static final int TRANSACTION_setParameter = 9;
        static final int TRANSACTION_setParameters = 10;
        static final int TRANSACTION_start = 5;
        static final int TRANSACTION_unconfigure = 4;
        static final int TRANSACTION_uninit = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEffectHalClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEffectHalClient)) {
                return new Proxy(obj);
            }
            return (IEffectHalClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BaseParameters _arg0;
            BaseParameters _arg02;
            BaseParameters _arg03;
            BaseParameters _arg1;
            BaseParameters _arg12;
            BaseParameters _arg04;
            if (code != 1598968902) {
                boolean _arg13 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = init();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = uninit();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = configure();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = unconfigure();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        long _result5 = start();
                        reply.writeNoException();
                        reply.writeLong(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = BaseParameters.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result6 = abort(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        EffectHalVersion _arg05 = new EffectHalVersion();
                        int _result7 = getNameVersion(_arg05);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        reply.writeInt(1);
                        _arg05.writeToParcel(reply, 1);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = setEffectListener(IEffectListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = setParameter(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BaseParameters.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        int _result10 = setParameters(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BaseParameters.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        ArrayList arrayList = new ArrayList();
                        int _result11 = getCaptureRequirement(_arg03, arrayList);
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        reply.writeTypedList(arrayList);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = prepare();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = release();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        ArrayList arrayList2 = new ArrayList();
                        int _result14 = getInputSurfaces(arrayList2);
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        reply.writeTypedList(arrayList2);
                        return true;
                    case TRANSACTION_setOutputSurfaces /* 15 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = setOutputSurfaces(data.createTypedArrayList(Surface.CREATOR), data.createTypedArrayList(BaseParameters.CREATOR));
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = BaseParameters.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result16 = addInputParameter(_arg06, _arg1, data.readLong(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case TRANSACTION_addOutputParameter /* 17 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = BaseParameters.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result17 = addOutputParameter(_arg07, _arg12, data.readLong(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case TRANSACTION_setInputsyncMode /* 18 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        int _result18 = setInputsyncMode(_arg08, _arg13);
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case TRANSACTION_getInputsyncMode /* 19 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean inputsyncMode = getInputsyncMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(inputsyncMode ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        int _result19 = setOutputsyncMode(_arg09, _arg13);
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case TRANSACTION_getOutputsyncMode /* 21 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean outputsyncMode = getOutputsyncMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(outputsyncMode ? 1 : 0);
                        return true;
                    case TRANSACTION_setBaseParameter /* 22 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = BaseParameters.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        int _result20 = setBaseParameter(_arg04);
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case TRANSACTION_dequeueAndQueueBuf /* 23 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = dequeueAndQueueBuf(data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IEffectHalClient {
            public static IEffectHalClient sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int init() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().init();
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int uninit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().uninit();
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int configure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configure();
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int unconfigure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unconfigure();
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public long start() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().start();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int abort(BaseParameters effectParameter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (effectParameter != null) {
                        _data.writeInt(1);
                        effectParameter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().abort(effectParameter);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int getNameVersion(EffectHalVersion version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNameVersion(version);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        version.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int setEffectListener(IEffectListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setEffectListener(listener);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int setParameter(String key, String paramValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(paramValue);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setParameter(key, paramValue);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int setParameters(BaseParameters parameter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (parameter != null) {
                        _data.writeInt(1);
                        parameter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setParameters(parameter);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int getCaptureRequirement(BaseParameters effectParameter, List<BaseParameters> requirement) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (effectParameter != null) {
                        _data.writeInt(1);
                        effectParameter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCaptureRequirement(effectParameter, requirement);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(requirement, BaseParameters.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int prepare() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().prepare();
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().release();
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int getInputSurfaces(List<Surface> input) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInputSurfaces(input);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(input, Surface.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int setOutputSurfaces(List<Surface> output, List<BaseParameters> parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(output);
                    _data.writeTypedList(parameters);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setOutputSurfaces, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setOutputSurfaces(output, parameters);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int addInputParameter(int index, BaseParameters parameter, long timestamp, boolean repeat) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    int i = 1;
                    if (parameter != null) {
                        _data.writeInt(1);
                        parameter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(timestamp);
                    if (!repeat) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addInputParameter(index, parameter, timestamp, repeat);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int addOutputParameter(int index, BaseParameters parameter, long timestamp, boolean repeat) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    int i = 1;
                    if (parameter != null) {
                        _data.writeInt(1);
                        parameter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(timestamp);
                    if (!repeat) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(Stub.TRANSACTION_addOutputParameter, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addOutputParameter(index, parameter, timestamp, repeat);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int setInputsyncMode(int index, boolean sync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    _data.writeInt(sync ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setInputsyncMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setInputsyncMode(index, sync);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public boolean getInputsyncMode(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_getInputsyncMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInputsyncMode(index);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int setOutputsyncMode(int index, boolean sync) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    _data.writeInt(sync ? 1 : 0);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setOutputsyncMode(index, sync);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public boolean getOutputsyncMode(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_getOutputsyncMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOutputsyncMode(index);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int setBaseParameter(BaseParameters parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (parameters != null) {
                        _data.writeInt(1);
                        parameters.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_setBaseParameter, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setBaseParameter(parameters);
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

            @Override // com.mediatek.mmsdk.IEffectHalClient
            public int dequeueAndQueueBuf(long timestamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timestamp);
                    if (!this.mRemote.transact(Stub.TRANSACTION_dequeueAndQueueBuf, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dequeueAndQueueBuf(timestamp);
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
        }

        public static boolean setDefaultImpl(IEffectHalClient impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEffectHalClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
