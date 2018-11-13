package com.qualcomm.wfd.service;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.InputEvent;
import android.view.Surface;
import com.qualcomm.wfd.WfdDevice;
import com.qualcomm.wfd.WfdStatus;

public interface IWfdSession extends IInterface {

    public static abstract class Stub extends Binder implements IWfdSession {
        private static final String DESCRIPTOR = "com.qualcomm.wfd.service.IWfdSession";
        static final int TRANSACTION_audioPause = 42;
        static final int TRANSACTION_audioResume = 43;
        static final int TRANSACTION_deinit = 8;
        static final int TRANSACTION_enableDynamicBitRateAdaptation = 32;
        static final int TRANSACTION_execRuntimeCommand = 39;
        static final int TRANSACTION_getCommonCapabilities = 25;
        static final int TRANSACTION_getCommonResolution = 30;
        static final int TRANSACTION_getConfigItems = 27;
        static final int TRANSACTION_getNegotiatedResolution = 31;
        static final int TRANSACTION_getStatus = 3;
        static final int TRANSACTION_getSupportedTypes = 2;
        static final int TRANSACTION_getUIBCStatus = 29;
        static final int TRANSACTION_init = 4;
        static final int TRANSACTION_initSys = 5;
        static final int TRANSACTION_pause = 20;
        static final int TRANSACTION_play = 19;
        static final int TRANSACTION_queryTCPTransportSupport = 17;
        static final int TRANSACTION_registerHIDEventListener = 33;
        static final int TRANSACTION_registerListener = 6;
        static final int TRANSACTION_sendEvent = 36;
        static final int TRANSACTION_setAvPlaybackMode = 18;
        static final int TRANSACTION_setBitRate = 13;
        static final int TRANSACTION_setCodecResolution = 12;
        static final int TRANSACTION_setDecoderLatency = 16;
        static final int TRANSACTION_setDeviceType = 1;
        static final int TRANSACTION_setNegotiatedCapabilities = 26;
        static final int TRANSACTION_setResolution = 11;
        static final int TRANSACTION_setRtpTransport = 14;
        static final int TRANSACTION_setSurface = 35;
        static final int TRANSACTION_setSurfaceProp = 37;
        static final int TRANSACTION_setSurfacePropEx = 41;
        static final int TRANSACTION_setUIBC = 28;
        static final int TRANSACTION_standby = 22;
        static final int TRANSACTION_startUibcSession = 23;
        static final int TRANSACTION_startWfdSession = 9;
        static final int TRANSACTION_stopUibcSession = 24;
        static final int TRANSACTION_stopWfdSession = 10;
        static final int TRANSACTION_tcpPlaybackControl = 15;
        static final int TRANSACTION_teardown = 21;
        static final int TRANSACTION_toggleDSMode = 40;
        static final int TRANSACTION_uibcRotate = 38;
        static final int TRANSACTION_unregisterHIDEventListener = 34;
        static final int TRANSACTION_unregisterListener = 7;
        static final int TRANSACTION_videoPause = 44;
        static final int TRANSACTION_videoResume = 45;

        private static class Proxy implements IWfdSession {
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

            public int setDeviceType(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSupportedTypes(int[] types) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (types == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(types.length);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readIntArray(types);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WfdStatus getStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WfdStatus _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStatus, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (WfdStatus) WfdStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int init(IWfdActionListener listener, WfdDevice thisDevice) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (thisDevice != null) {
                        _data.writeInt(1);
                        thisDevice.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int initSys(IWfdActionListener listener, WfdDevice thisDevice) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (thisDevice != null) {
                        _data.writeInt(1);
                        thisDevice.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_initSys, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int registerListener(IWfdActionListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unregisterListener(IWfdActionListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deinit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startWfdSession(WfdDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startWfdSession, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopWfdSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopWfdSession, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setResolution(int formatType, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(formatType);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_setResolution, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setCodecResolution(int codec, int profile, int level, int formatType, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(codec);
                    _data.writeInt(profile);
                    _data.writeInt(level);
                    _data.writeInt(formatType);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_setCodecResolution, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setBitRate(int bitrate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bitrate);
                    this.mRemote.transact(Stub.TRANSACTION_setBitRate, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setRtpTransport(int transportType, int bufferLengthMs, int port) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transportType);
                    _data.writeInt(bufferLengthMs);
                    _data.writeInt(port);
                    this.mRemote.transact(Stub.TRANSACTION_setRtpTransport, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int tcpPlaybackControl(int cmdType, int cmdVal) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmdType);
                    _data.writeInt(cmdVal);
                    this.mRemote.transact(Stub.TRANSACTION_tcpPlaybackControl, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDecoderLatency(int latency) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(latency);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int queryTCPTransportSupport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_queryTCPTransportSupport, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setAvPlaybackMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_setAvPlaybackMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int play() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_play, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int pause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int teardown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_teardown, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int standby() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_standby, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startUibcSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_startUibcSession, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopUibcSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopUibcSession, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCommonCapabilities(Bundle capabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCommonCapabilities, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        capabilities.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setNegotiatedCapabilities(Bundle capabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (capabilities != null) {
                        _data.writeInt(1);
                        capabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setNegotiatedCapabilities, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getConfigItems(Bundle configItems) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getConfigItems, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        configItems.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setUIBC() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_setUIBC, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getUIBCStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getUIBCStatus, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCommonResolution(Bundle comRes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCommonResolution, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        comRes.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNegotiatedResolution(Bundle negRes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNegotiatedResolution, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        negRes.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enableDynamicBitRateAdaptation(boolean flag) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (flag) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int registerHIDEventListener(IHIDEventListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerHIDEventListener, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unregisterHIDEventListener(IHIDEventListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterHIDEventListener, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSurface(Surface mmSurface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mmSurface != null) {
                        _data.writeInt(1);
                        mmSurface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setSurface, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendEvent(InputEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendEvent, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSurfaceProp(int width, int height, int orientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(orientation);
                    this.mRemote.transact(Stub.TRANSACTION_setSurfaceProp, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uibcRotate(int angle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(angle);
                    this.mRemote.transact(Stub.TRANSACTION_uibcRotate, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int execRuntimeCommand(int command) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(command);
                    this.mRemote.transact(Stub.TRANSACTION_execRuntimeCommand, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int toggleDSMode(boolean flag) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (flag) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_toggleDSMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSurfacePropEx(Bundle surfaceProp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surfaceProp != null) {
                        _data.writeInt(1);
                        surfaceProp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setSurfacePropEx, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int audioPause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_audioPause, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int audioResume() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_audioResume, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int videoPause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_videoPause, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int videoResume() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_videoResume, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWfdSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWfdSession)) {
                return new Proxy(obj);
            }
            return (IWfdSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            IWfdActionListener _arg0;
            WfdDevice _arg1;
            Bundle _arg02;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setDeviceType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    int[] _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0_length = data.readInt();
                    if (_arg0_length < 0) {
                        _arg03 = null;
                    } else {
                        _arg03 = new int[_arg0_length];
                    }
                    _result = getSupportedTypes(_arg03);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeIntArray(_arg03);
                    return true;
                case TRANSACTION_getStatus /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    WfdStatus _result2 = getStatus();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.qualcomm.wfd.service.IWfdActionListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (WfdDevice) WfdDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    _result = init(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_initSys /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.qualcomm.wfd.service.IWfdActionListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = (WfdDevice) WfdDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    _result = initSys(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_registerListener /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerListener(com.qualcomm.wfd.service.IWfdActionListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_unregisterListener /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unregisterListener(com.qualcomm.wfd.service.IWfdActionListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deinit();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_startWfdSession /*9*/:
                    WfdDevice _arg04;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = (WfdDevice) WfdDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    _result = startWfdSession(_arg04);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_stopWfdSession /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopWfdSession();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setResolution /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setResolution(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setCodecResolution /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setCodecResolution(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setBitRate /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setBitRate(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setRtpTransport /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setRtpTransport(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_tcpPlaybackControl /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = tcpPlaybackControl(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setDecoderLatency(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_queryTCPTransportSupport /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = queryTCPTransportSupport();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setAvPlaybackMode /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setAvPlaybackMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_play /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = play();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_pause /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = pause();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_teardown /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = teardown();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_standby /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = standby();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_startUibcSession /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = startUibcSession();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_stopUibcSession /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopUibcSession();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getCommonCapabilities /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = new Bundle();
                    _result = getCommonCapabilities(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg02 != null) {
                        reply.writeInt(1);
                        _arg02.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setNegotiatedCapabilities /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    _result = setNegotiatedCapabilities(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getConfigItems /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = new Bundle();
                    _result = getConfigItems(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg02 != null) {
                        reply.writeInt(1);
                        _arg02.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setUIBC /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setUIBC();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getUIBCStatus /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = getUIBCStatus();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_getCommonResolution /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = new Bundle();
                    _result = getCommonResolution(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg02 != null) {
                        reply.writeInt(1);
                        _arg02.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNegotiatedResolution /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = new Bundle();
                    _result = getNegotiatedResolution(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg02 != null) {
                        reply.writeInt(1);
                        _arg02.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableDynamicBitRateAdaptation(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_registerHIDEventListener /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerHIDEventListener(com.qualcomm.wfd.service.IHIDEventListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_unregisterHIDEventListener /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unregisterHIDEventListener(com.qualcomm.wfd.service.IHIDEventListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setSurface /*35*/:
                    Surface _arg05;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg05 = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        _arg05 = null;
                    }
                    _result = setSurface(_arg05);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_sendEvent /*36*/:
                    InputEvent _arg06;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg06 = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg06 = null;
                    }
                    _result = sendEvent(_arg06);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setSurfaceProp /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setSurfaceProp(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uibcRotate /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uibcRotate(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_execRuntimeCommand /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = execRuntimeCommand(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_toggleDSMode /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = toggleDSMode(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setSurfacePropEx /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    _result = setSurfacePropEx(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_audioPause /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = audioPause();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_audioResume /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = audioResume();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_videoPause /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = videoPause();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_videoResume /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = videoResume();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int audioPause() throws RemoteException;

    int audioResume() throws RemoteException;

    int deinit() throws RemoteException;

    int enableDynamicBitRateAdaptation(boolean z) throws RemoteException;

    int execRuntimeCommand(int i) throws RemoteException;

    int getCommonCapabilities(Bundle bundle) throws RemoteException;

    int getCommonResolution(Bundle bundle) throws RemoteException;

    int getConfigItems(Bundle bundle) throws RemoteException;

    int getNegotiatedResolution(Bundle bundle) throws RemoteException;

    WfdStatus getStatus() throws RemoteException;

    int getSupportedTypes(int[] iArr) throws RemoteException;

    boolean getUIBCStatus() throws RemoteException;

    int init(IWfdActionListener iWfdActionListener, WfdDevice wfdDevice) throws RemoteException;

    int initSys(IWfdActionListener iWfdActionListener, WfdDevice wfdDevice) throws RemoteException;

    int pause() throws RemoteException;

    int play() throws RemoteException;

    int queryTCPTransportSupport() throws RemoteException;

    int registerHIDEventListener(IHIDEventListener iHIDEventListener) throws RemoteException;

    int registerListener(IWfdActionListener iWfdActionListener) throws RemoteException;

    int sendEvent(InputEvent inputEvent) throws RemoteException;

    int setAvPlaybackMode(int i) throws RemoteException;

    int setBitRate(int i) throws RemoteException;

    int setCodecResolution(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int setDecoderLatency(int i) throws RemoteException;

    int setDeviceType(int i) throws RemoteException;

    int setNegotiatedCapabilities(Bundle bundle) throws RemoteException;

    int setResolution(int i, int i2) throws RemoteException;

    int setRtpTransport(int i, int i2, int i3) throws RemoteException;

    int setSurface(Surface surface) throws RemoteException;

    int setSurfaceProp(int i, int i2, int i3) throws RemoteException;

    int setSurfacePropEx(Bundle bundle) throws RemoteException;

    int setUIBC() throws RemoteException;

    int standby() throws RemoteException;

    int startUibcSession() throws RemoteException;

    int startWfdSession(WfdDevice wfdDevice) throws RemoteException;

    int stopUibcSession() throws RemoteException;

    int stopWfdSession() throws RemoteException;

    int tcpPlaybackControl(int i, int i2) throws RemoteException;

    int teardown() throws RemoteException;

    int toggleDSMode(boolean z) throws RemoteException;

    int uibcRotate(int i) throws RemoteException;

    int unregisterHIDEventListener(IHIDEventListener iHIDEventListener) throws RemoteException;

    int unregisterListener(IWfdActionListener iWfdActionListener) throws RemoteException;

    int videoPause() throws RemoteException;

    int videoResume() throws RemoteException;
}
