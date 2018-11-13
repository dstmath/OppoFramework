package com.oppo.hypnus;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.android.internal.app.IHypnusService;
import com.android.internal.app.IHypnusService.Stub;

public class HypnusManager {
    private static boolean DEBUG = false;
    private static final String TAG = "HypnusManager";
    private static final int mMaxVelocity = 24000;
    private static final int mMinDiffX = 150;
    private static final int mMinVelocity = 150;
    private static VelocityTracker mVelocityTracker = null;
    private static HypnusManager sManager;
    private static IHypnusService sService = null;
    private int startX = 0;

    public HypnusManager() {
        if (sService == null) {
            sService = Stub.asInterface(ServiceManager.getService("hypnus"));
        }
    }

    public void hypnusSetNotification(int msg_src, int msg_type) {
        hypnusSetNotification(msg_src, msg_type, 0, 0, 0, 0);
    }

    public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) {
        if (sService != null) {
            try {
                sService.hypnusSetNotification(msg_src, msg_type, msg_time, pid, v0, v1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void hypnusSetScene(int pid, String processName) {
        if (sService != null) {
            try {
                sService.hypnusSetScene(pid, processName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void hypnusSetAction(int action, int timeout) {
        if (sService != null) {
            try {
                sService.hypnusSetAction(action, timeout);
                if (DEBUG) {
                    Log.d(TAG, "hypnusSetAction");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void hypnusSetBurst(int tid, int type, int timeout) {
        if (sService != null) {
            try {
                sService.hypnusSetBurst(tid, type, timeout);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (DEBUG) {
            Log.d(TAG, "hypnus service is not ready yet!");
        }
    }

    public boolean isHypnusOK() {
        if (sService != null) {
            try {
                return sService.isHypnusOK();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "hypnus service is not ready yet!");
            }
            return false;
        }
    }

    public void hypnusManagerSendFling(MotionEvent ev, int duration) {
        try {
            int actionMasked = ev.getActionMasked();
            int pointerIndex = ev.getActionIndex();
            int pointerId = ev.getPointerId(pointerIndex);
            switch (actionMasked) {
                case 0:
                    this.startX = (int) ev.getX(pointerIndex);
                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        mVelocityTracker.clear();
                    }
                    if (mVelocityTracker != null) {
                        mVelocityTracker.addMovement(ev);
                        break;
                    }
                    break;
                case 1:
                    if (mVelocityTracker != null) {
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000, 24000.0f);
                        int initialVelocity = Math.abs((int) mVelocityTracker.getYVelocity(pointerId));
                        if (initialVelocity > Hypnus.TIME_PRE_LAUNCH) {
                            int diffX = Math.abs(((int) ev.getX(pointerIndex)) - this.startX);
                            if (DEBUG) {
                                Log.d("Hypnus", "diffX is:" + diffX);
                            }
                            duration = (int) (((float) duration) * ((((float) initialVelocity) * 1.0f) / 150.0f));
                            if (diffX <= Hypnus.TIME_PRE_LAUNCH) {
                                hypnusSetSignatureAction(7, duration, Hypnus.getLocalSignature());
                                break;
                            } else {
                                hypnusSetSignatureAction(8, duration + 100, Hypnus.getLocalSignature());
                                break;
                            }
                        }
                    }
                    break;
                case 2:
                    if (mVelocityTracker != null) {
                        mVelocityTracker.addMovement(ev);
                        break;
                    }
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "java.lang.IllegalArgumentException");
        }
    }

    public void hypnusSetSignatureAction(int action, int timeout, String signatureinfo) {
        if (sService != null) {
            try {
                sService.hypnusSetSignatureAction(action, timeout, signatureinfo);
                if (DEBUG) {
                    Log.d(TAG, "hypnusSetSignatureAction");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized HypnusManager getHypnusManager() {
        HypnusManager hypnusManager;
        synchronized (HypnusManager.class) {
            if (sManager == null) {
                sManager = new HypnusManager();
            }
            if (sManager == null) {
                Log.e(TAG, "HypnusManager is null");
            }
            hypnusManager = sManager;
        }
        return hypnusManager;
    }
}
