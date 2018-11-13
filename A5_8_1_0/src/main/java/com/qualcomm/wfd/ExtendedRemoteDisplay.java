package com.qualcomm.wfd;

import android.content.Context;
import android.media.RemoteDisplay.Listener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import com.qualcomm.wfd.ServiceUtil.ServiceFailedToBindException;
import com.qualcomm.wfd.WfdEnums.WFDDeviceType;
import com.qualcomm.wfd.service.IWfdActionListener;
import dalvik.system.CloseGuard;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ExtendedRemoteDisplay {
    public static final int DISPLAY_ERROR_CONNECTION_DROPPED = 2;
    public static final int DISPLAY_ERROR_UNKOWN = 1;
    public static final int DISPLAY_FLAG_SECURE = 1;
    private static final int OP_TIMEOUT = 10000;
    private static final String TAG = "ExtendedRemoteDisplay";
    private static final int TEARDOWN_TIMEOUT = 9000;
    private static final int UNINITIALIZED_TIMEOUT = 10000;
    public static Condition sConditionDeinit = sLock.newCondition();
    public static Condition sConditionUninitialized = sLock.newCondition();
    public static Object sERDLock = new Object();
    public static Lock sLock = new ReentrantLock();
    public static WFDState sState = WFDState.UNINITIALIZED;
    private IWfdActionListener mActionListener;
    private boolean mConnected;
    private Context mContext;
    private Handler mDisplayHandler;
    private ExtendedRemoteDisplayHandler mERDHandler;
    private CloseGuard mGuard = CloseGuard.get();
    private String mIface;
    private boolean mInvalid;
    private Listener mListener;
    private WfdDevice mLocalWfdDevice;
    private WfdDevice mPeerWfdDevice;
    private HandlerThread mWorkerThread;
    private Surface surface;

    class ExtendedRemoteDisplayHandler extends Handler {
        /* renamed from: -com-qualcomm-wfd-ERDConstantsSwitchesValues */
        private static final /* synthetic */ int[] f1-com-qualcomm-wfd-ERDConstantsSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$qualcomm$wfd$ERDConstants;

        /* renamed from: -getcom-qualcomm-wfd-ERDConstantsSwitchesValues */
        private static /* synthetic */ int[] m1-getcom-qualcomm-wfd-ERDConstantsSwitchesValues() {
            if (f1-com-qualcomm-wfd-ERDConstantsSwitchesValues != null) {
                return f1-com-qualcomm-wfd-ERDConstantsSwitchesValues;
            }
            int[] iArr = new int[ERDConstants.values().length];
            try {
                iArr[ERDConstants.END_CMD.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ERDConstants.ESTABLISHED_CALLBACK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ERDConstants.INIT_CALLBACK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ERDConstants.INVALID_ERD_CONSTANT.ordinal()] = 14;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[ERDConstants.INVALID_STATE_CALLBACK.ordinal()] = 4;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[ERDConstants.MM_STREAM_STARTED_CALLBACK.ordinal()] = 5;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[ERDConstants.PAUSE_CALLBACK.ordinal()] = 6;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[ERDConstants.PLAY_CALLBACK.ordinal()] = 7;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[ERDConstants.SERVICE_BOUND_CALLBACK.ordinal()] = 8;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[ERDConstants.STANDBY_CALLBACK.ordinal()] = 9;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[ERDConstants.START_CMD.ordinal()] = 10;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[ERDConstants.TEARDOWN_CALLBACK.ordinal()] = 11;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[ERDConstants.TEARDOWN_START_CALLBACK.ordinal()] = 12;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[ERDConstants.UIBC_ACTION_COMPLETED_CALLBACK.ordinal()] = 13;
            } catch (NoSuchFieldError e14) {
            }
            f1-com-qualcomm-wfd-ERDConstantsSwitchesValues = iArr;
            return iArr;
        }

        public ExtendedRemoteDisplayHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ERDConstants cmd = ERDConstants.getConstant(msg.what);
            Log.d(ExtendedRemoteDisplay.TAG, "ERD handler received: " + cmd);
            switch (m1-getcom-qualcomm-wfd-ERDConstantsSwitchesValues()[cmd.ordinal()]) {
                case 1:
                    Log.d(ExtendedRemoteDisplay.TAG, "Ending.........");
                    if (ExtendedRemoteDisplay.sState != WFDState.DEINIT) {
                        if (ExtendedRemoteDisplay.sState != WFDState.BOUND) {
                            if (ExtendedRemoteDisplay.sState != WFDState.INITIALIZED) {
                                try {
                                    if (ServiceUtil.getmServiceAlreadyBound()) {
                                        Log.d(ExtendedRemoteDisplay.TAG, "Teardown WFD Session");
                                        ServiceUtil.getInstance().teardown();
                                        break;
                                    }
                                } catch (RemoteException e) {
                                    Log.e(ExtendedRemoteDisplay.TAG, "RemoteException in teardown");
                                    break;
                                }
                            }
                            try {
                                ServiceUtil.getInstance().deinit();
                            } catch (RemoteException e2) {
                                Log.e(ExtendedRemoteDisplay.TAG, "RemoteException in deInit");
                            }
                            return;
                        }
                        ExtendedRemoteDisplay.this.mERDHandler.sendMessage(ExtendedRemoteDisplay.this.mERDHandler.obtainMessage(ERDConstants.INVALID_STATE_CALLBACK.value()));
                        return;
                    }
                    return;
                    break;
                case 2:
                    ExtendedRemoteDisplay.this.setState(WFDState.ESTABLISHED);
                    Log.d(ExtendedRemoteDisplay.TAG, "EventHandler: startSession- completed");
                    break;
                case 3:
                    try {
                        int ret = ServiceUtil.getInstance().startWfdSession(ExtendedRemoteDisplay.this.mPeerWfdDevice);
                        Log.d(ExtendedRemoteDisplay.TAG, "EventHandler: startWfdSession returned " + ret);
                        if (ret == 0) {
                            ExtendedRemoteDisplay.this.setState(WFDState.ESTABLISHING);
                        }
                        ExtendedRemoteDisplay.this.unblockWaiters();
                        break;
                    } catch (RemoteException e3) {
                        e3.printStackTrace();
                        ExtendedRemoteDisplay.this.unblockWaiters();
                        break;
                    } catch (Throwable th) {
                        ExtendedRemoteDisplay.this.unblockWaiters();
                        throw th;
                    }
                case 4:
                    ExtendedRemoteDisplay.this.notifyDisplayDisconnected();
                    ExtendedRemoteDisplay.this.mInvalid = true;
                    if (ServiceUtil.getmServiceAlreadyBound()) {
                        try {
                            ServiceUtil.getInstance().unregisterListener(ExtendedRemoteDisplay.this.mActionListener);
                        } catch (RemoteException e32) {
                            Log.e(ExtendedRemoteDisplay.TAG, "RemoteException in un-registering listener" + e32);
                        }
                    }
                    Log.d(ExtendedRemoteDisplay.TAG, "Unbind the WFD service");
                    ServiceUtil.unbindService(ExtendedRemoteDisplay.this.mContext);
                    ExtendedRemoteDisplay.sLock.lock();
                    ExtendedRemoteDisplay.this.setState(WFDState.DEINIT);
                    Log.d(ExtendedRemoteDisplay.TAG, "Signal sConditionDeinit");
                    ExtendedRemoteDisplay.sConditionDeinit.signal();
                    ExtendedRemoteDisplay.sLock.unlock();
                    Log.d(ExtendedRemoteDisplay.TAG, "ERD instance invalidated");
                    break;
                case 5:
                    ExtendedRemoteDisplay.this.setState(WFDState.PLAYING);
                    Bundle b = msg.getData();
                    int width = b.getInt("width");
                    int height = b.getInt("height");
                    int secure = b.getInt("hdcp");
                    ExtendedRemoteDisplay.this.surface = (Surface) b.getParcelable("surface");
                    if (ExtendedRemoteDisplay.this.surface != null) {
                        Log.d(ExtendedRemoteDisplay.TAG, "MM Stream Started Width, Height and Secure:  " + width + " " + height + " " + secure);
                        if (secure == 0) {
                            ExtendedRemoteDisplay.this.notifyDisplayConnected(ExtendedRemoteDisplay.this.surface, width, height, 0);
                            break;
                        } else {
                            ExtendedRemoteDisplay.this.notifyDisplayConnected(ExtendedRemoteDisplay.this.surface, width, height, 1);
                            break;
                        }
                    }
                    break;
                case 6:
                    Log.d(ExtendedRemoteDisplay.TAG, "WFDService in PAUSE state");
                    break;
                case 7:
                    ExtendedRemoteDisplay.this.setState(WFDState.PLAY);
                    Log.d(ExtendedRemoteDisplay.TAG, "WFDService in PLAY state");
                    break;
                case 8:
                    ExtendedRemoteDisplay.this.mActionListener = new WfdActionListenerImpl(ExtendedRemoteDisplay.this.mERDHandler);
                    ExtendedRemoteDisplay.this.setState(WFDState.BOUND);
                    try {
                        int setDeviceTypeRet = ServiceUtil.getInstance().setDeviceType(WFDDeviceType.SOURCE.getCode());
                        if (setDeviceTypeRet == 0) {
                            if (ServiceUtil.getInstance().initSys(ExtendedRemoteDisplay.this.mActionListener, ExtendedRemoteDisplay.this.mLocalWfdDevice) == 0) {
                                ExtendedRemoteDisplay.this.setState(WFDState.INITIALIZED);
                                ExtendedRemoteDisplay.this.mInvalid = false;
                                break;
                            }
                            Log.e(ExtendedRemoteDisplay.TAG, "Init failed");
                            ExtendedRemoteDisplay.this.unblockWaiters();
                            return;
                        }
                        Log.d(ExtendedRemoteDisplay.TAG, "setDeviceType failed : " + setDeviceTypeRet);
                        ExtendedRemoteDisplay.this.unblockWaiters();
                        return;
                    } catch (RemoteException e322) {
                        Log.e(ExtendedRemoteDisplay.TAG, "WfdService init() failed", e322);
                        return;
                    }
                case 9:
                    Log.d(ExtendedRemoteDisplay.TAG, "WFDService in STANDBY state");
                    break;
                case 10:
                    Log.d(ExtendedRemoteDisplay.TAG, "Starting......");
                    ExtendedRemoteDisplay.this.mLocalWfdDevice = ExtendedRemoteDisplay.this.createWFDDevice(ExtendedRemoteDisplay.this.mIface, WFDDeviceType.SOURCE);
                    ExtendedRemoteDisplay.this.mPeerWfdDevice = ExtendedRemoteDisplay.this.createWFDDevice(ExtendedRemoteDisplay.this.mIface, WFDDeviceType.PRIMARY_SINK);
                    if (ExtendedRemoteDisplay.this.mLocalWfdDevice != null && ExtendedRemoteDisplay.this.mPeerWfdDevice != null) {
                        try {
                            ExtendedRemoteDisplay.this.setState(WFDState.BINDING);
                            ServiceUtil.bindService(ExtendedRemoteDisplay.this.mContext, ExtendedRemoteDisplay.this.mERDHandler);
                            break;
                        } catch (ServiceFailedToBindException e4) {
                            Log.e(ExtendedRemoteDisplay.TAG, "ServiceFailedToBindException received");
                            ExtendedRemoteDisplay.this.unblockWaiters();
                            break;
                        }
                    }
                    Log.i(ExtendedRemoteDisplay.TAG, "Invalid WFD devices for iface: " + ExtendedRemoteDisplay.this.mIface);
                    ExtendedRemoteDisplay.this.unblockWaiters();
                    break;
                case 11:
                    try {
                        ExtendedRemoteDisplay.this.setState(WFDState.TEARDOWN);
                        if (ServiceUtil.getmServiceAlreadyBound()) {
                            ServiceUtil.getInstance().deinit();
                        }
                        if (ExtendedRemoteDisplay.this.surface != null) {
                            if (!ExtendedRemoteDisplay.this.surface.isValid()) {
                                Log.e(ExtendedRemoteDisplay.TAG, "surface not valid");
                                break;
                            }
                            ExtendedRemoteDisplay.this.surface.release();
                            Log.e(ExtendedRemoteDisplay.TAG, "Released surface successfully");
                            break;
                        }
                        Log.e(ExtendedRemoteDisplay.TAG, "Why on earth is surface null??");
                        break;
                    } catch (RemoteException e3222) {
                        e3222.printStackTrace();
                        break;
                    }
                case 12:
                    Log.d(ExtendedRemoteDisplay.TAG, "Received TEARDOWN_START_CALLBACK");
                    ExtendedRemoteDisplay.this.notifyDisplayDisconnected();
                    break;
                case 13:
                    Log.d(ExtendedRemoteDisplay.TAG, "EventHandler: uibcActionCompleted- completed");
                    break;
                default:
                    Log.wtf(ExtendedRemoteDisplay.TAG, "Unknown cmd received: " + msg.what);
                    break;
            }
        }
    }

    public ExtendedRemoteDisplay(Listener listener, Handler handler, Context context) {
        this.mListener = listener;
        this.mDisplayHandler = handler;
        this.mContext = context;
        this.mConnected = true;
        this.mWorkerThread = new HandlerThread(TAG);
        this.mWorkerThread.setName("ERD_Worker_Thread");
        this.mWorkerThread.start();
        this.mERDHandler = new ExtendedRemoteDisplayHandler(this.mWorkerThread.getLooper());
        this.mInvalid = true;
    }

    protected void finalize() throws Throwable {
        Log.i(TAG, "finalize called on " + this);
        if (!this.mInvalid) {
            Log.e(TAG, "!!!Finalized without being invalidated!!!");
            processDispose(true);
        }
        this.mListener = null;
        super.finalize();
    }

    public static ExtendedRemoteDisplay listen(String iface, Listener listener, Handler handler, Context context) {
        if (iface == null) {
            throw new IllegalArgumentException("iface must not be null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        } else if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        } else {
            ExtendedRemoteDisplay display = new ExtendedRemoteDisplay(listener, handler, context);
            sLock.lock();
            try {
                long timeOutNanos = TimeUnit.MILLISECONDS.toNanos(10000);
                while (sState != WFDState.UNINITIALIZED) {
                    Log.d(TAG, "Waiting to move to UNINITIALIZED timetowait=" + timeOutNanos + " ns");
                    if (timeOutNanos <= 0) {
                        Log.d(TAG, "Waiting to move to UNINITIALIZED - timedout");
                        break;
                    }
                    timeOutNanos = sConditionUninitialized.awaitNanos(timeOutNanos);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Exception when waiting to move to UNINITIALIZED" + e.getMessage());
            }
            sLock.unlock();
            if (sState != WFDState.UNINITIALIZED) {
                Log.e(TAG, "Something has gone kaput in listen!");
                display.dispose();
                return null;
            }
            Log.d(TAG, "Waiting for RTSP server to start ");
            display.startListening(iface);
            display.blockOnLock();
            if (sState != WFDState.ESTABLISHING) {
                Log.d(TAG, "Failed to start RTSP Server!");
                display.dispose();
                return null;
            }
            Log.i(TAG, "New ERD instance " + display);
            return display;
        }
    }

    public void startListening(String iface) {
        this.mIface = iface;
        this.mERDHandler.sendMessage(this.mERDHandler.obtainMessage(ERDConstants.START_CMD.value()));
        this.mGuard.open("dispose");
    }

    public void dispose() {
        processDispose(false);
    }

    private void processDispose(final boolean finalized) {
        new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().setName("Dispose_Thread");
                Log.d(ExtendedRemoteDisplay.TAG, "Processing dispose with " + finalized + " from " + Thread.currentThread().getName());
                ExtendedRemoteDisplay.this.disposeInternal(finalized);
            }
        }).start();
    }

    private void disposeInternal(boolean finalized) {
        if (this.mGuard != null) {
            if (finalized) {
                this.mGuard.warnIfOpen();
            } else {
                this.mGuard.close();
            }
            this.mGuard = null;
        }
        if (this.mERDHandler != null) {
            this.mERDHandler.sendMessage(this.mERDHandler.obtainMessage(ERDConstants.END_CMD.value()));
        }
        Log.d(TAG, "Waiting for teardown to complete");
        sLock.lock();
        try {
            long timeOutNanos = TimeUnit.MILLISECONDS.toNanos(9000);
            while (sState != WFDState.DEINIT) {
                Log.d(TAG, "Waiting to move to DEINIT timetowait=" + timeOutNanos + " ns");
                if (timeOutNanos <= 0) {
                    Log.d(TAG, "Waiting to move to DEINIT - timedout");
                    break;
                }
                timeOutNanos = sConditionDeinit.awaitNanos(timeOutNanos);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Exception when waiting to move to DEINIT" + e.getMessage());
        }
        sLock.unlock();
        if (sState != WFDState.DEINIT) {
            Log.e(TAG, "Something has gone kaput! in teardown");
            this.mERDHandler.sendMessage(this.mERDHandler.obtainMessage(ERDConstants.INVALID_STATE_CALLBACK.value()));
        }
        cleanupResources();
        Log.d(TAG, "Done with teardown");
        sLock.lock();
        setState(WFDState.UNINITIALIZED);
        Log.d(TAG, "Signal sConditionUninitialized");
        sConditionUninitialized.signal();
        sLock.unlock();
    }

    private void notifyDisplayConnected(Surface surface, int width, int height, int flags) {
        Log.d(TAG, "notifyDisplayConnected");
        final Surface surface2 = surface;
        final int i = width;
        final int i2 = height;
        final int i3 = flags;
        this.mDisplayHandler.post(new Runnable() {
            public void run() {
                if (ExtendedRemoteDisplay.this.mListener != null) {
                    Log.d(ExtendedRemoteDisplay.TAG, "notifyDisplayConnected");
                    ExtendedRemoteDisplay.this.mListener.onDisplayConnected(surface2, i, i2, i3, 0);
                    ExtendedRemoteDisplay.this.mConnected = true;
                }
            }
        });
    }

    private void notifyDisplayDisconnected() {
        this.mDisplayHandler.post(new Runnable() {
            public void run() {
                if (ExtendedRemoteDisplay.this.mConnected && ExtendedRemoteDisplay.this.mListener != null) {
                    Log.d(ExtendedRemoteDisplay.TAG, "notifyDisplayDisconnected");
                    ExtendedRemoteDisplay.this.mListener.onDisplayDisconnected();
                    ExtendedRemoteDisplay.this.mConnected = false;
                }
            }
        });
    }

    private void notifyDisplayError(final int error) {
        this.mDisplayHandler.post(new Runnable() {
            public void run() {
                if (ExtendedRemoteDisplay.this.mListener != null) {
                    Log.d(ExtendedRemoteDisplay.TAG, "notifyDisplayError");
                    ExtendedRemoteDisplay.this.mListener.onDisplayError(error);
                }
            }
        });
    }

    private WfdDevice createWFDDevice(String iface, WFDDeviceType type) {
        WfdDevice wfdDevice = new WfdDevice();
        wfdDevice.deviceType = type.getCode();
        wfdDevice.macAddress = null;
        wfdDevice.isAvailableForSession = false;
        wfdDevice.addressOfAP = null;
        wfdDevice.coupledSinkStatus = 0;
        wfdDevice.preferredConnectivity = 0;
        if (type == WFDDeviceType.SOURCE) {
            wfdDevice.deviceName = "Source_device";
            if (iface != null) {
                int index = iface.indexOf(58, 0);
                if (index > 0) {
                    wfdDevice.ipAddress = iface.substring(0, index);
                    wfdDevice.rtspPort = Integer.parseInt(iface.substring(index + 1, iface.length()));
                    if (wfdDevice.ipAddress == null || wfdDevice.rtspPort < 1 || wfdDevice.rtspPort > 65535) {
                        Log.e(TAG, "Invalid RTSP port received or no valid IP");
                        return null;
                    }
                }
            }
        } else if (type == WFDDeviceType.PRIMARY_SINK) {
            wfdDevice.deviceName = "Sink_device";
            wfdDevice.rtspPort = 0;
            wfdDevice.ipAddress = null;
        } else {
            wfdDevice.deviceName = "WFD_device";
            wfdDevice.rtspPort = 0;
            wfdDevice.ipAddress = null;
        }
        return wfdDevice;
    }

    private void blockOnLock() {
        blockOnLock(10000);
    }

    private void blockOnLock(int timeout) {
        synchronized (sERDLock) {
            try {
                sERDLock.wait((long) timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void unblockWaiters() {
        synchronized (sERDLock) {
            sERDLock.notifyAll();
        }
    }

    private void cleanupResources() {
        Log.d(TAG, "Initiating resource cleanup");
        if (this.mWorkerThread != null) {
            this.mWorkerThread.quitSafely();
            try {
                this.mWorkerThread.join();
                this.mWorkerThread = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Failed to join on mWorkerThread");
            }
        }
        this.mDisplayHandler = null;
        this.mERDHandler = null;
        this.mContext = null;
        this.surface = null;
        this.mIface = null;
        this.mLocalWfdDevice = null;
        this.mPeerWfdDevice = null;
        this.mActionListener = null;
        Log.d(TAG, "Done with resource cleanup");
    }

    private void setState(WFDState s) {
        Log.d(TAG, "Moving from " + sState + " --> " + s);
        sState = s;
    }
}
