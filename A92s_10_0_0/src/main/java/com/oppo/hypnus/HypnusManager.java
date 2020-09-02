package com.oppo.hypnus;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.android.internal.app.IHypnusService;

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
            sService = IHypnusService.Stub.asInterface(ServiceManager.getService("hypnus"));
        }
    }

    public void hypnusSetNotification(int msg_src, int msg_type) {
        hypnusSetNotification(msg_src, msg_type, 0, 0, 0, 0);
    }

    public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                iHypnusService.hypnusSetNotification(msg_src, msg_type, msg_time, pid, v0, v1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void hypnusSetScene(int pid, String processName) {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                iHypnusService.hypnusSetScene(pid, processName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void HypnusSetDisplayState(int state) {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                iHypnusService.HypnusSetDisplayState(state);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void hypnusSetAction(int action, int timeout) {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                iHypnusService.hypnusSetAction(action, timeout);
                if (DEBUG) {
                    Log.d(TAG, "hypnusSetAction");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void hypnusSetBurst(int tid, int type, int timeout) {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                iHypnusService.hypnusSetBurst(tid, type, timeout);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (DEBUG) {
            Log.d(TAG, "hypnus service is not ready yet!");
        }
    }

    public boolean isHypnusOK() {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                return iHypnusService.isHypnusOK();
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        } else if (!DEBUG) {
            return false;
        } else {
            Log.d(TAG, "hypnus service is not ready yet!");
            return false;
        }
    }

    public String hypnusGetHighPerfModeState() {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                return iHypnusService.hypnusGetHighPerfModeState();
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        } else if (!DEBUG) {
            return null;
        } else {
            Log.d(TAG, "hypnus service is not ready yet!");
            return null;
        }
    }

    public String hypnusGetPMState() {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                return iHypnusService.hypnusGetPMState();
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        } else if (!DEBUG) {
            return null;
        } else {
            Log.d(TAG, "hypnus service is not ready yet!");
            return null;
        }
    }

    public String hypnusGetBenchModeState() {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                return iHypnusService.hypnusGetBenchModeState();
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        } else if (!DEBUG) {
            return null;
        } else {
            Log.d(TAG, "hypnus service is not ready yet!");
            return null;
        }
    }

    public int HypnusSetPerfData(int small_max, int small_min, int small_cores, int big_max, int big_min, int big_cores, int gpu_max, int gpu_min, int gpu_cores, int flags) {
        int ret = -1;
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                ret = iHypnusService.HypnusSetPerfData(small_max, small_min, small_cores, big_max, big_min, big_cores, gpu_max, gpu_min, gpu_cores, flags);
                if (DEBUG) {
                    Log.d(TAG, "hypnus service is not ready yet!");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public int HypnusClrPerfData() {
        int ret = -1;
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                ret = iHypnusService.HypnusClrPerfData();
                if (DEBUG) {
                    Log.d(TAG, "hypnus service is not ready yet!");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public int HypnusSetScenePerfData(int scene) {
        int ret = -1;
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                ret = iHypnusService.HypnusSetScenePerfData(scene);
                if (DEBUG) {
                    Log.d(TAG, "hypnus service is not ready yet!");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public void hypnusManagerSendFling(MotionEvent ev, int duration) {
        try {
            int actionMasked = ev.getActionMasked();
            int pointerIndex = ev.getActionIndex();
            int pointerId = ev.getPointerId(pointerIndex);
            if (actionMasked == 0) {
                this.startX = (int) ev.getX(pointerIndex);
                VelocityTracker velocityTracker = mVelocityTracker;
                if (velocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                VelocityTracker velocityTracker2 = mVelocityTracker;
                if (velocityTracker2 != null) {
                    velocityTracker2.addMovement(ev);
                }
            } else if (actionMasked == 1) {
                VelocityTracker velocityTracker3 = mVelocityTracker;
                if (velocityTracker3 != null) {
                    velocityTracker3.addMovement(ev);
                    mVelocityTracker.computeCurrentVelocity(1000, 24000.0f);
                    int initialVelocity = Math.abs((int) mVelocityTracker.getYVelocity(pointerId));
                    if (initialVelocity > 150) {
                        int diffX = Math.abs(((int) ev.getX(pointerIndex)) - this.startX);
                        if (DEBUG) {
                            Log.d("Hypnus", "diffX is:" + diffX);
                        }
                        int duration2 = (int) (((float) duration) * ((((float) initialVelocity) * 1.0f) / 150.0f));
                        if (diffX > 150) {
                            hypnusSetSignatureAction(8, duration2 + 100, Hypnus.getLocalSignature());
                        } else {
                            hypnusSetSignatureAction(7, duration2, Hypnus.getLocalSignature());
                        }
                    }
                }
            } else if (actionMasked != 2) {
                if (actionMasked != 3) {
                }
            } else {
                VelocityTracker velocityTracker4 = mVelocityTracker;
                if (velocityTracker4 != null) {
                    velocityTracker4.addMovement(ev);
                }
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "java.lang.IllegalArgumentException");
        }
    }

    public void hypnusSetSignatureAction(int action, int timeout, String signatureinfo) {
        IHypnusService iHypnusService = sService;
        if (iHypnusService != null) {
            try {
                iHypnusService.hypnusSetSignatureAction(action, timeout, signatureinfo);
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
