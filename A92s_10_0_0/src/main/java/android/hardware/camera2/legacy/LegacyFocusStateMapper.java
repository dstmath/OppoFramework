package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.utils.ParamsUtils;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public class LegacyFocusStateMapper {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = ParameterUtils.DEBUG;
    /* access modifiers changed from: private */
    public static String TAG = "LegacyFocusStateMapper";
    private String mAfModePrevious = null;
    /* access modifiers changed from: private */
    public int mAfRun = 0;
    /* access modifiers changed from: private */
    public int mAfState = 0;
    private int mAfStatePrevious = 0;
    private final Camera mCamera;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();

    public LegacyFocusStateMapper(Camera camera) {
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0124, code lost:
        if (r2.equals("auto") != false) goto L_0x0132;
     */
    public void processRequestTriggers(CaptureRequest captureRequest, Camera.Parameters parameters) {
        final int currentAfRun;
        boolean z;
        int afStateAfterStart;
        final int currentAfRun2;
        int updatedAfRun;
        Preconditions.checkNotNull(captureRequest, "captureRequest must not be null");
        boolean z2 = false;
        int afTrigger = ((Integer) ParamsUtils.getOrDefault(captureRequest, CaptureRequest.CONTROL_AF_TRIGGER, 0)).intValue();
        final String afMode = parameters.getFocusMode();
        if (!Objects.equals(this.mAfModePrevious, afMode)) {
            if (DEBUG) {
                Log.v(TAG, "processRequestTriggers - AF mode switched from " + this.mAfModePrevious + " to " + afMode);
            }
            synchronized (this.mLock) {
                this.mAfRun++;
                this.mAfState = 0;
            }
            this.mCamera.cancelAutoFocus();
        }
        this.mAfModePrevious = afMode;
        synchronized (this.mLock) {
            currentAfRun = this.mAfRun;
        }
        Camera.AutoFocusMoveCallback afMoveCallback = new Camera.AutoFocusMoveCallback() {
            /* class android.hardware.camera2.legacy.LegacyFocusStateMapper.AnonymousClass1 */

            @Override // android.hardware.Camera.AutoFocusMoveCallback
            public void onAutoFocusMoving(boolean start, Camera camera) {
                int newAfState;
                boolean z;
                synchronized (LegacyFocusStateMapper.this.mLock) {
                    int latestAfRun = LegacyFocusStateMapper.this.mAfRun;
                    if (LegacyFocusStateMapper.DEBUG) {
                        Log.v(LegacyFocusStateMapper.TAG, "onAutoFocusMoving - start " + start + " latest AF run " + latestAfRun + ", last AF run " + currentAfRun);
                    }
                    if (currentAfRun != latestAfRun) {
                        Log.d(LegacyFocusStateMapper.TAG, "onAutoFocusMoving - ignoring move callbacks from old af run" + currentAfRun);
                        return;
                    }
                    if (start) {
                        newAfState = 1;
                    } else {
                        newAfState = 2;
                    }
                    String str = afMode;
                    int hashCode = str.hashCode();
                    if (hashCode != -194628547) {
                        if (hashCode == 910005312 && str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            z = false;
                            if (z && !z) {
                                Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                            }
                            int unused = LegacyFocusStateMapper.this.mAfState = newAfState;
                        }
                    } else if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        z = true;
                        Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                        int unused2 = LegacyFocusStateMapper.this.mAfState = newAfState;
                    }
                    z = true;
                    Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                    int unused3 = LegacyFocusStateMapper.this.mAfState = newAfState;
                }
            }
        };
        switch (afMode.hashCode()) {
            case -194628547:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 3005871:
                if (afMode.equals("auto")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 103652300:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 910005312:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z || z || z || z) {
            this.mCamera.setAutoFocusMoveCallback(afMoveCallback);
        }
        if (afTrigger == 0) {
            return;
        }
        if (afTrigger == 1) {
            switch (afMode.hashCode()) {
                case -194628547:
                    if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        z2 = true;
                        break;
                    }
                    z2 = true;
                    break;
                case 3005871:
                    break;
                case 103652300:
                    if (afMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                        z2 = true;
                        break;
                    }
                    z2 = true;
                    break;
                case 910005312:
                    if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        z2 = true;
                        break;
                    }
                    z2 = true;
                    break;
                default:
                    z2 = true;
                    break;
            }
            if (!z2 || z2) {
                afStateAfterStart = 3;
            } else if (z2 || z2) {
                afStateAfterStart = 1;
            } else {
                afStateAfterStart = 0;
            }
            synchronized (this.mLock) {
                currentAfRun2 = this.mAfRun + 1;
                this.mAfRun = currentAfRun2;
                this.mAfState = afStateAfterStart;
            }
            if (DEBUG) {
                Log.v(TAG, "processRequestTriggers - got AF_TRIGGER_START, new AF run is " + currentAfRun2);
            }
            if (afStateAfterStart != 0) {
                this.mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    /* class android.hardware.camera2.legacy.LegacyFocusStateMapper.AnonymousClass2 */

                    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
                    @Override // android.hardware.Camera.AutoFocusCallback
                    public void onAutoFocus(boolean success, Camera camera) {
                        int newAfState;
                        synchronized (LegacyFocusStateMapper.this.mLock) {
                            int latestAfRun = LegacyFocusStateMapper.this.mAfRun;
                            if (LegacyFocusStateMapper.DEBUG) {
                                Log.v(LegacyFocusStateMapper.TAG, "onAutoFocus - success " + success + " latest AF run " + latestAfRun + ", last AF run " + currentAfRun2);
                            }
                            boolean z = false;
                            if (latestAfRun != currentAfRun2) {
                                Log.d(LegacyFocusStateMapper.TAG, String.format("onAutoFocus - ignoring AF callback (old run %d, new run %d)", Integer.valueOf(currentAfRun2), Integer.valueOf(latestAfRun)));
                                return;
                            }
                            if (success) {
                                newAfState = 4;
                            } else {
                                newAfState = 5;
                            }
                            String str = afMode;
                            switch (str.hashCode()) {
                                case -194628547:
                                    if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                                        z = true;
                                        break;
                                    }
                                    z = true;
                                    break;
                                case 3005871:
                                    if (str.equals("auto")) {
                                        break;
                                    }
                                    z = true;
                                    break;
                                case 103652300:
                                    if (str.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                                        z = true;
                                        break;
                                    }
                                    z = true;
                                    break;
                                case 910005312:
                                    if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                        z = true;
                                        break;
                                    }
                                    z = true;
                                    break;
                                default:
                                    z = true;
                                    break;
                            }
                            if (!(!z || z || z || z)) {
                                Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                            }
                            int unused = LegacyFocusStateMapper.this.mAfState = newAfState;
                        }
                    }
                });
            }
        } else if (afTrigger != 2) {
            Log.w(TAG, "processRequestTriggers - ignoring unknown control.afTrigger = " + afTrigger);
        } else {
            synchronized (this.mLock) {
                synchronized (this.mLock) {
                    updatedAfRun = this.mAfRun + 1;
                    this.mAfRun = updatedAfRun;
                    this.mAfState = 0;
                }
                this.mCamera.cancelAutoFocus();
                if (DEBUG) {
                    Log.v(TAG, "processRequestTriggers - got AF_TRIGGER_CANCEL, new AF run is " + updatedAfRun);
                }
            }
        }
    }

    public void mapResultTriggers(CameraMetadataNative result) {
        int newAfState;
        int i;
        Preconditions.checkNotNull(result, "result must not be null");
        synchronized (this.mLock) {
            newAfState = this.mAfState;
        }
        if (DEBUG && newAfState != (i = this.mAfStatePrevious)) {
            Log.v(TAG, String.format("mapResultTriggers - afState changed from %s to %s", afStateToString(i), afStateToString(newAfState)));
        }
        result.set(CaptureResult.CONTROL_AF_STATE, Integer.valueOf(newAfState));
        this.mAfStatePrevious = newAfState;
    }

    private static String afStateToString(int afState) {
        switch (afState) {
            case 0:
                return "INACTIVE";
            case 1:
                return "PASSIVE_SCAN";
            case 2:
                return "PASSIVE_FOCUSED";
            case 3:
                return "ACTIVE_SCAN";
            case 4:
                return "FOCUSED_LOCKED";
            case 5:
                return "NOT_FOCUSED_LOCKED";
            case 6:
                return "PASSIVE_UNFOCUSED";
            default:
                return "UNKNOWN(" + afState + ")";
        }
    }
}
