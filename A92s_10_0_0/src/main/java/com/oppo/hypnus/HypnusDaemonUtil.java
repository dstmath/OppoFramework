package com.oppo.hypnus;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class HypnusDaemonUtil {
    private static final int CLR_HYPNUS_PERFDATA = 23;
    private static final String DESCRIPTOR = "com.oppo.hypnus.IHypnusService";
    private static final int GET_HYPNUS_BENCHMODE_STATUS = 16;
    private static final int GET_HYPNUS_GHPM_STATUS = 15;
    private static final int GET_HYPNUS_PM_STATUS = 14;
    private static final int HYPNUS_ON = 0;
    private static final int SET_HYPNUS_ACTION = 3;
    private static final int SET_HYPNUS_BOOTCOMPLETED = 7;
    private static final int SET_HYPNUS_DISPLAT_STATE = 4;
    private static final int SET_HYPNUS_IS_OK = 10;
    private static final int SET_HYPNUS_NOTIFICATION = 6;
    private static final int SET_HYPNUS_PERFDATA = 21;
    private static final int SET_HYPNUS_SCENE = 5;
    private static final int SET_HYPNUS_SCENE_PERFDATA = 9;
    private static final String TAG = "HypnusDaemonUtil";
    private static int sHypnusProp = SystemProperties.getInt("persist.sys.hypnus.daemon.enable", 0);
    private static HypnusDaemonUtil sInstance;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.oppo.hypnus.HypnusDaemonUtil.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.d(HypnusDaemonUtil.TAG, "HypnusDaemonUtil binderDied");
            IBinder unused = HypnusDaemonUtil.this.mRemote = null;
        }
    };
    /* access modifiers changed from: private */
    public IBinder mRemote;

    public static synchronized HypnusDaemonUtil getInstance() {
        HypnusDaemonUtil hypnusDaemonUtil;
        synchronized (HypnusDaemonUtil.class) {
            if (sInstance == null) {
                sInstance = new HypnusDaemonUtil();
            }
            hypnusDaemonUtil = sInstance;
        }
        return hypnusDaemonUtil;
    }

    private HypnusDaemonUtil() {
        connectHypnusDataService();
    }

    private IBinder connectHypnusDataService() {
        this.mRemote = ServiceManager.checkService("hypnusd");
        IBinder iBinder = this.mRemote;
        if (iBinder != null) {
            try {
                iBinder.linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
                this.mRemote = null;
            }
        }
        return this.mRemote;
    }

    public void hypnusSetAction(int action, int timeout) {
        if (hypnusEnable()) {
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(action);
                _data.writeInt(timeout);
                this.mRemote.transact(3, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "hypnusd hypnusSetAction has Exception : " + e);
            } catch (Throwable th) {
                _data.recycle();
                throw th;
            }
            _data.recycle();
        }
    }

    public void hypnusSetScene(int pid, String processName) {
        if (hypnusEnable()) {
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(pid);
                _data.writeString(processName);
                this.mRemote.transact(5, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "hypnusd hypnusSetScene has Exception : " + e);
            } catch (Throwable th) {
                _data.recycle();
                throw th;
            }
            _data.recycle();
        }
    }

    public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) {
        if (hypnusEnable()) {
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(msg_src);
                _data.writeInt(msg_type);
                _data.writeLong(msg_time);
                _data.writeInt(pid);
                _data.writeInt(v0);
                _data.writeInt(v1);
                this.mRemote.transact(6, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "hypnusd hypnusSetNotification has Exception : " + e);
            } catch (Throwable th) {
                _data.recycle();
                throw th;
            }
            _data.recycle();
        }
    }

    public void hypnusBootCompleted(String processName) {
        if (hypnusEnable()) {
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(processName);
                this.mRemote.transact(7, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "hypnusd hypnusBootCompleted has Exception : " + e);
            } catch (Throwable th) {
                _data.recycle();
                throw th;
            }
            _data.recycle();
        }
    }

    public int HypnusSetPerfData(int small_max, int small_min, int small_cores, int big_max, int big_min, int big_cores, int gpu_max, int gpu_min, int gpu_cores, int flags) {
        if (!hypnusEnable()) {
            return 0;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(0);
            _data.writeInt(0);
            try {
                _data.writeInt(small_max);
                try {
                    _data.writeInt(small_min);
                    _data.writeInt(small_cores);
                    _data.writeInt(small_cores);
                    _data.writeInt(0);
                } catch (Exception e) {
                    e = e;
                    try {
                        Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
                        _reply.recycle();
                        _data.recycle();
                        return _result;
                    } catch (Throwable th) {
                        th = th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
                try {
                    _data.writeInt(big_max);
                    try {
                        _data.writeInt(big_min);
                        _data.writeInt(big_cores);
                        _data.writeInt(big_cores);
                        _data.writeInt(0);
                        for (int i = 0; i < 2; i++) {
                            _data.writeInt(0);
                            _data.writeInt(0);
                            _data.writeInt(0);
                            _data.writeInt(0);
                            _data.writeInt(0);
                        }
                    } catch (Exception e2) {
                        e = e2;
                        Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
                        _reply.recycle();
                        _data.recycle();
                        return _result;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Exception e3) {
                    e = e3;
                    Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
                _reply.recycle();
                _data.recycle();
                return _result;
            } catch (Throwable th5) {
                th = th5;
                _reply.recycle();
                _data.recycle();
                throw th;
            }
            try {
                _data.writeInt(gpu_max);
                try {
                    _data.writeInt(gpu_min);
                    _data.writeInt(gpu_cores);
                    _data.writeInt(gpu_cores);
                    _data.writeInt(0);
                } catch (Exception e5) {
                    e = e5;
                    Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
                try {
                    _data.writeInt(flags);
                    try {
                        this.mRemote.transact(21, _data, _reply, 0);
                        _result = _reply.readInt();
                    } catch (Exception e6) {
                        e = e6;
                        Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
                        _reply.recycle();
                        _data.recycle();
                        return _result;
                    }
                } catch (Exception e7) {
                    e = e7;
                    Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            } catch (Exception e8) {
                e = e8;
                Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
                _reply.recycle();
                _data.recycle();
                return _result;
            } catch (Throwable th8) {
                th = th8;
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        } catch (Exception e9) {
            e = e9;
            Log.d(TAG, "hypnusd HypnusSetPerfData has Exception : " + e);
            _reply.recycle();
            _data.recycle();
            return _result;
        } catch (Throwable th9) {
            th = th9;
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public int HypnusClrPerfData() {
        if (!hypnusEnable()) {
            return 0;
        }
        Parcel _data = Parcel.obtain();
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(23, _data, null, 0);
        } catch (Exception e) {
            Log.d(TAG, "hypnusd HypnusClrPerfData has Exception : " + e);
        } catch (Throwable th) {
            _data.recycle();
            throw th;
        }
        _data.recycle();
        return 0;
    }

    public int HypnusSetScenePerfData(int scene) {
        if (!hypnusEnable()) {
            return 0;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result = 0;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeInt(scene);
            this.mRemote.transact(9, _data, _reply, 0);
            _result = _reply.readInt();
        } catch (Exception e) {
            Log.d(TAG, "hypnusd HypnusSetScenePerfData has Exception : " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public void HypnusSetDisplayState(int state) {
        if (hypnusEnable()) {
            Parcel _data = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(state);
                this.mRemote.transact(4, _data, null, 1);
            } catch (Exception e) {
                Log.d(TAG, "hypnusd HypnusSetDisplayState has Exception : " + e);
            } catch (Throwable th) {
                _data.recycle();
                throw th;
            }
            _data.recycle();
        }
    }

    public boolean isHypnusOK() {
        boolean z = false;
        if (!hypnusEnable()) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean _result = false;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(10, _data, _reply, 0);
            if (_reply.readInt() != 0) {
                z = true;
            }
            _result = z;
        } catch (Exception e) {
            Log.d(TAG, "hypnusd isHypnusOK has Exception : " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public String hypnusGetHighPerfModeState() {
        if (!hypnusEnable()) {
            return null;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result = null;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(15, _data, _reply, 0);
            _result = "supported:" + _reply.readInt();
        } catch (Exception e) {
            Log.d(TAG, "hypnusd hypnusGetHighPerfModeState has Exception : " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "hypnusGetHighPerfModeState:" + _result);
        return _result;
    }

    public String hypnusGetPMState() {
        if (!hypnusEnable()) {
            return null;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result = null;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(14, _data, _reply, 0);
            _result = "supported:1,enable:" + _reply.readInt() + ",mode:" + _reply.readInt();
        } catch (Exception e) {
            Log.d(TAG, "hypnusd hypnusGetPMState has Exception : " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "hypnusGetPMState:" + _result);
        return _result;
    }

    public String hypnusGetBenchModeState() {
        if (!hypnusEnable()) {
            return null;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result = null;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            this.mRemote.transact(16, _data, _reply, 0);
            _result = "supported:" + _reply.readInt();
        } catch (Exception e) {
            Log.d(TAG, "hypnusd hypnusGetBenchModeState has Exception : " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "hypnusGetBenchModeState:" + _result);
        return _result;
    }

    private boolean hypnusEnable() {
        if (this.mRemote != null || connectHypnusDataService() != null) {
            return true;
        }
        Log.e(TAG, "hypnusd cannot connect to HypnusService");
        return false;
    }
}
