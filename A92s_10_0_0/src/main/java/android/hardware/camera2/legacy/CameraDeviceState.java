package android.hardware.camera2.legacy;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Handler;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants;

public class CameraDeviceState {
    private static final boolean DEBUG = ParameterUtils.DEBUG;
    public static final int NO_CAPTURE_ERROR = -1;
    private static final int STATE_CAPTURING = 4;
    private static final int STATE_CONFIGURING = 2;
    private static final int STATE_ERROR = 0;
    private static final int STATE_IDLE = 3;
    private static final int STATE_UNCONFIGURED = 1;
    private static final String TAG = "CameraDeviceState";
    private static final String[] sStateNames = {"ERROR", "UNCONFIGURED", "CONFIGURING", "IDLE", "CAPTURING"};
    /* access modifiers changed from: private */
    public int mCurrentError = -1;
    private Handler mCurrentHandler = null;
    /* access modifiers changed from: private */
    public CameraDeviceStateListener mCurrentListener = null;
    /* access modifiers changed from: private */
    public RequestHolder mCurrentRequest = null;
    private int mCurrentState = 1;

    public interface CameraDeviceStateListener {
        void onBusy();

        void onCaptureResult(CameraMetadataNative cameraMetadataNative, RequestHolder requestHolder);

        void onCaptureStarted(RequestHolder requestHolder, long j);

        void onConfiguring();

        void onError(int i, Object obj, RequestHolder requestHolder);

        void onIdle();

        void onRepeatingRequestError(long j, int i);

        void onRequestQueueEmpty();
    }

    public synchronized void setError(int error) {
        this.mCurrentError = error;
        doStateTransition(0);
    }

    public synchronized boolean setConfiguring() {
        doStateTransition(2);
        return this.mCurrentError == -1;
    }

    public synchronized boolean setIdle() {
        doStateTransition(3);
        return this.mCurrentError == -1;
    }

    public synchronized boolean setCaptureStart(RequestHolder request, long timestamp, int captureError) {
        this.mCurrentRequest = request;
        doStateTransition(4, timestamp, captureError);
        return this.mCurrentError == -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0054, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x002d, code lost:
        return r2;
     */
    public synchronized boolean setCaptureResult(final RequestHolder request, final CameraMetadataNative result, final int captureError, final Object captureErrorArg) {
        boolean z = true;
        if (this.mCurrentState != 4) {
            Log.e(TAG, "Cannot receive result while in state: " + this.mCurrentState);
            this.mCurrentError = 1;
            doStateTransition(0);
            if (this.mCurrentError != -1) {
                z = false;
            }
        } else {
            if (!(this.mCurrentHandler == null || this.mCurrentListener == null)) {
                if (captureError != -1) {
                    this.mCurrentHandler.post(new Runnable() {
                        /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass1 */

                        public void run() {
                            CameraDeviceState.this.mCurrentListener.onError(captureError, captureErrorArg, request);
                        }
                    });
                } else {
                    this.mCurrentHandler.post(new Runnable() {
                        /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass2 */

                        public void run() {
                            CameraDeviceState.this.mCurrentListener.onCaptureResult(result, request);
                        }
                    });
                }
            }
            if (this.mCurrentError != -1) {
                z = false;
            }
        }
    }

    public synchronized boolean setCaptureResult(RequestHolder request, CameraMetadataNative result) {
        return setCaptureResult(request, result, -1, null);
    }

    public synchronized void setRepeatingRequestError(final long lastFrameNumber, final int repeatingRequestId) {
        this.mCurrentHandler.post(new Runnable() {
            /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass3 */

            public void run() {
                CameraDeviceState.this.mCurrentListener.onRepeatingRequestError(lastFrameNumber, repeatingRequestId);
            }
        });
    }

    public synchronized void setRequestQueueEmpty() {
        this.mCurrentHandler.post(new Runnable() {
            /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass4 */

            public void run() {
                CameraDeviceState.this.mCurrentListener.onRequestQueueEmpty();
            }
        });
    }

    public synchronized void setCameraDeviceCallbacks(Handler handler, CameraDeviceStateListener listener) {
        this.mCurrentHandler = handler;
        this.mCurrentListener = listener;
    }

    private void doStateTransition(int newState) {
        doStateTransition(newState, 0, -1);
    }

    /* access modifiers changed from: protected */
    public synchronized int getCurrentState() {
        return this.mCurrentState;
    }

    private void doStateTransition(int newState, final long timestamp, final int error) {
        Handler handler;
        Handler handler2;
        Handler handler3;
        Handler handler4;
        if (newState != this.mCurrentState) {
            String stateName = IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            if (newState >= 0) {
                String[] strArr = sStateNames;
                if (newState < strArr.length) {
                    stateName = strArr[newState];
                }
            }
            Log.i(TAG, "Legacy camera service transitioning to state " + stateName);
        }
        if (!(newState == 0 || newState == 3 || this.mCurrentState == newState || (handler4 = this.mCurrentHandler) == null || this.mCurrentListener == null)) {
            handler4.post(new Runnable() {
                /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass5 */

                public void run() {
                    CameraDeviceState.this.mCurrentListener.onBusy();
                }
            });
        }
        if (newState == 0) {
            if (!(this.mCurrentState == 0 || (handler = this.mCurrentHandler) == null || this.mCurrentListener == null)) {
                handler.post(new Runnable() {
                    /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass6 */

                    public void run() {
                        CameraDeviceState.this.mCurrentListener.onError(CameraDeviceState.this.mCurrentError, null, CameraDeviceState.this.mCurrentRequest);
                    }
                });
            }
            this.mCurrentState = 0;
        } else if (newState == 2) {
            int i = this.mCurrentState;
            if (i == 1 || i == 3) {
                if (!(this.mCurrentState == 2 || (handler2 = this.mCurrentHandler) == null || this.mCurrentListener == null)) {
                    handler2.post(new Runnable() {
                        /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass7 */

                        public void run() {
                            CameraDeviceState.this.mCurrentListener.onConfiguring();
                        }
                    });
                }
                this.mCurrentState = 2;
                return;
            }
            Log.e(TAG, "Cannot call configure while in state: " + this.mCurrentState);
            this.mCurrentError = 1;
            doStateTransition(0);
        } else if (newState == 3) {
            int i2 = this.mCurrentState;
            if (i2 == 3) {
                Handler handler5 = this.mCurrentHandler;
                if (handler5 != null && this.mCurrentListener != null) {
                    handler5.post(new Runnable() {
                        /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass8 */

                        public void run() {
                            CameraDeviceState.this.mCurrentListener.onIdle();
                        }
                    });
                }
            } else if (i2 == 2 || i2 == 4) {
                if (!(this.mCurrentState == 3 || (handler3 = this.mCurrentHandler) == null || this.mCurrentListener == null)) {
                    handler3.post(new Runnable() {
                        /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass9 */

                        public void run() {
                            CameraDeviceState.this.mCurrentListener.onIdle();
                        }
                    });
                }
                this.mCurrentState = 3;
            } else {
                Log.e(TAG, "Cannot call idle while in state: " + this.mCurrentState);
                this.mCurrentError = 1;
                doStateTransition(0);
            }
        } else if (newState == 4) {
            int i3 = this.mCurrentState;
            if (i3 == 3 || i3 == 4) {
                Handler handler6 = this.mCurrentHandler;
                if (!(handler6 == null || this.mCurrentListener == null)) {
                    if (error != -1) {
                        handler6.post(new Runnable() {
                            /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass10 */

                            public void run() {
                                CameraDeviceState.this.mCurrentListener.onError(error, null, CameraDeviceState.this.mCurrentRequest);
                            }
                        });
                    } else {
                        handler6.post(new Runnable() {
                            /* class android.hardware.camera2.legacy.CameraDeviceState.AnonymousClass11 */

                            public void run() {
                                CameraDeviceState.this.mCurrentListener.onCaptureStarted(CameraDeviceState.this.mCurrentRequest, timestamp);
                            }
                        });
                    }
                }
                this.mCurrentState = 4;
                return;
            }
            Log.e(TAG, "Cannot call capture while in state: " + this.mCurrentState);
            this.mCurrentError = 1;
            doStateTransition(0);
        } else {
            throw new IllegalStateException("Transition to unknown state: " + newState);
        }
    }
}
