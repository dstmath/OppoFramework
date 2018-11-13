package com.android.server.oppo;

import android.content.Context;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder.DeathRecipient;
import android.os.IOppoExInputCallBack;
import android.os.IOppoExService.Stub;
import android.os.IOppoGestureCallBack;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;
import com.android.server.LocationManagerService;
import com.android.server.wm.WindowManagerService;
import com.color.util.ColorAccidentallyTouchUtils;
import com.oppo.util.EdgePointInterceptUtils;
import java.util.ArrayList;
import java.util.HashMap;

public final class OppoExService extends Stub {
    private static final String OPPO_EDGE_GESTURE = "oppo.common.support.edge.gesture";
    private static final String OPPO_EX_CHANNEL_NAME = "OppoExInputReceiver";
    private static final String OPPO_EX_DEBUGGABLE = "persist.sys.ex.service";
    private static final String TAG = "OppoExService";
    private boolean OPPODEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    InputChannel mExInputChannel = null;
    OppoExInputEventReceiver mExInputEventReceiver = null;
    private IOppoGestureCallBack mGestureCallback = null;
    private HashMap<Integer, Integer> mGestureStatesMap = new HashMap();
    private HandlerThread mHandlerThread;
    private boolean mHasEdgeGesture = false;
    private final ArrayList<OppoExInputCallBack> mInputCallBacks = new ArrayList();
    private boolean mIsPaused = false;
    private final ArrayList<OppoExInputCallBack> mRawInputCallBacks = new ArrayList();
    private OppoRootCheckHelper mRootCheckHelper = null;
    WindowManagerService mWindowManager = null;

    private class OppoExInputCallBack implements DeathRecipient {
        private IOppoExInputCallBack mCallBack = null;

        OppoExInputCallBack(IOppoExInputCallBack callBack) {
            this.mCallBack = callBack;
        }

        public void binderDied() {
            synchronized (OppoExService.this.mInputCallBacks) {
                this.mCallBack = null;
                OppoExService.this.mInputCallBacks.remove(this);
                OppoExService.this.mRawInputCallBacks.remove(this);
            }
        }
    }

    private final class OppoExInputEventReceiver extends InputEventReceiver {
        public OppoExInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event, int displayId) {
            boolean handled = false;
            try {
                if (!OppoExService.this.mIsPaused) {
                    synchronized (OppoExService.this.mInputCallBacks) {
                        OppoExInputCallBack callBackInfo;
                        for (OppoExInputCallBack callBackInfo2 : OppoExService.this.mRawInputCallBacks) {
                            if (!(callBackInfo2 == null || callBackInfo2.mCallBack == null)) {
                                callBackInfo2.mCallBack.onInputEvent(event);
                            }
                        }
                        int size = OppoExService.this.mInputCallBacks.size();
                        InputEvent tmpEvent = event;
                        if (OppoExService.this.mHasEdgeGesture && event != null && (event instanceof MotionEvent)) {
                            tmpEvent = EdgePointInterceptUtils.getInstance().splitEvent((MotionEvent) event);
                        }
                        for (int i = 0; i < size; i++) {
                            callBackInfo2 = (OppoExInputCallBack) OppoExService.this.mInputCallBacks.get(i);
                            if (!(callBackInfo2 == null || callBackInfo2.mCallBack == null || tmpEvent == null)) {
                                callBackInfo2.mCallBack.onInputEvent(tmpEvent);
                            }
                        }
                    }
                    handled = true;
                }
                finishInputEvent(event, handled);
            } catch (Throwable th) {
                try {
                    if (OppoExService.this.OPPODEBUG) {
                        Slog.w(OppoExService.TAG, "Failure IOppoExInputCallBack onInputEvent");
                    }
                    finishInputEvent(event, false);
                } catch (Throwable th2) {
                    finishInputEvent(event, false);
                }
            }
        }
    }

    public OppoExService(Context context, WindowManagerService windowManager) {
        this.mWindowManager = windowManager;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mRootCheckHelper = new OppoRootCheckHelper(context);
        ColorAccidentallyTouchUtils.getInstance().initData(context);
        try {
            this.mHasEdgeGesture = context.getPackageManager().hasSystemFeature(OPPO_EDGE_GESTURE);
        } catch (Exception e) {
            Log.e(TAG, "get feature oppo.common.support.edge.gesture error!");
        }
    }

    public boolean registerInputEvent(IOppoExInputCallBack callBackAdd) {
        return register(callBackAdd, false);
    }

    public boolean registerRawInputEvent(IOppoExInputCallBack callBackAdd) {
        return register(callBackAdd, true);
    }

    /* JADX WARNING: Missing block: B:47:0x00c9, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean register(IOppoExInputCallBack callBackAdd, boolean isRawEvent) {
        if (callBackAdd == null) {
            Log.d(TAG, "registerInputEvent failed null");
            return false;
        }
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get(OPPO_EX_DEBUGGABLE))) {
            Log.d(TAG, " OppoExService  registerInputEvent");
        }
        synchronized (this.mInputCallBacks) {
            int size = this.mInputCallBacks.size();
            int i = 0;
            while (i < size) {
                try {
                    if (((OppoExInputCallBack) this.mInputCallBacks.get(i)).mCallBack.asBinder() == callBackAdd.asBinder()) {
                        Log.d(TAG, " already exist!");
                        return false;
                    }
                    i++;
                } catch (Exception exception) {
                    Log.d(TAG, "registerInputEvent failed!" + exception);
                    return false;
                }
            }
            for (OppoExInputCallBack callBackInfo : this.mRawInputCallBacks) {
                if (callBackInfo.mCallBack.asBinder() == callBackAdd.asBinder()) {
                    Log.d(TAG, " already exist!");
                    return false;
                }
            }
            OppoExInputCallBack callNewBackInfo = new OppoExInputCallBack(callBackAdd);
            if (isRawEvent) {
                this.mRawInputCallBacks.add(callNewBackInfo);
            } else {
                this.mInputCallBacks.add(callNewBackInfo);
            }
            try {
                callBackAdd.asBinder().linkToDeath(callNewBackInfo, 0);
                if (!(this.mInputCallBacks.isEmpty() && (this.mRawInputCallBacks.isEmpty() ^ 1) == 0)) {
                    enableInputReceiver();
                }
            } catch (RemoteException e) {
                return false;
            }
        }
    }

    public void pauseExInputEvent() {
        this.mIsPaused = true;
    }

    public void resumeExInputEvent() {
        this.mIsPaused = false;
    }

    public void unregisterInputEvent(IOppoExInputCallBack callBackRemove) {
        synchronized (this.mInputCallBacks) {
            int i;
            OppoExInputCallBack callBackInfo;
            int size = this.mInputCallBacks.size();
            for (i = 0; i < size; i++) {
                callBackInfo = (OppoExInputCallBack) this.mInputCallBacks.get(i);
                if (callBackInfo.mCallBack.asBinder() == callBackRemove.asBinder()) {
                    this.mInputCallBacks.remove(i);
                    callBackRemove.asBinder().unlinkToDeath(callBackInfo, 0);
                    break;
                }
            }
            int rawSize = this.mRawInputCallBacks.size();
            for (i = 0; i < rawSize; i++) {
                callBackInfo = (OppoExInputCallBack) this.mRawInputCallBacks.get(i);
                if (callBackInfo.mCallBack.asBinder() == callBackRemove.asBinder()) {
                    this.mRawInputCallBacks.remove(i);
                    callBackRemove.asBinder().unlinkToDeath(callBackInfo, 0);
                    break;
                }
            }
            if (this.mInputCallBacks.isEmpty() && this.mRawInputCallBacks.isEmpty()) {
                disableInputReceiver();
            }
        }
    }

    public void enableInputReceiver() {
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get(OPPO_EX_DEBUGGABLE))) {
            Log.d(TAG, "OppoExService  enableInputReceiver");
        }
        if (this.mExInputEventReceiver == null) {
            this.mExInputChannel = this.mWindowManager.getInputManagerService().monitorInput(OPPO_EX_CHANNEL_NAME);
            this.mExInputEventReceiver = new OppoExInputEventReceiver(this.mExInputChannel, this.mHandlerThread.getLooper());
        }
    }

    public void disableInputReceiver() {
        if (this.mExInputEventReceiver != null) {
            this.mExInputEventReceiver.dispose();
            this.mExInputEventReceiver = null;
        }
        if (this.mExInputChannel != null) {
            this.mExInputChannel.dispose();
            this.mExInputChannel = null;
        }
    }

    public boolean registerScreenoffGesture(IOppoGestureCallBack callBack) {
        int callingUid = Binder.getCallingUid();
        if (callingUid >= 100000) {
            if (this.OPPODEBUG) {
                Log.d(TAG, "registerScreenoffGesture. ignore.  callingUid= " + callingUid);
            }
            return true;
        }
        if (this.OPPODEBUG) {
            Log.d(TAG, "registerScreenoffGesture  callingUid= " + callingUid);
        }
        this.mGestureCallback = callBack;
        return true;
    }

    public void unregisterScreenoffGesture(IOppoGestureCallBack callBack) {
        int callingUid = Binder.getCallingUid();
        if (callingUid >= 100000) {
            if (this.OPPODEBUG) {
                Log.d(TAG, "unregisterScreenoffGesture. ignore.  callingUid= " + callingUid);
            }
            return;
        }
        if (this.OPPODEBUG) {
            Log.d(TAG, "unregisterScreenoffGesture  callingUid= " + callingUid);
        }
        this.mGestureCallback = null;
    }

    public void dealScreenoffGesture(int nGesture) {
        Log.d(TAG, "OppoExService  dealScreenoffGesture nGesture = " + nGesture + "  mGestureCallback = " + this.mGestureCallback);
        if (this.mGestureCallback != null) {
            try {
                this.mGestureCallback.onDealGesture(nGesture);
            } catch (Throwable th) {
                if (this.OPPODEBUG) {
                    Slog.w(TAG, "Failure IOppoGestureCallBack onDealGesture");
                }
            }
        }
    }

    public void setGestureState(int nGesture, boolean isOpen) {
        if (this.OPPODEBUG) {
            Log.d(TAG, "OppoExService  setGestureState nGesture = " + nGesture);
        }
        synchronized (this.mGestureStatesMap) {
            this.mGestureStatesMap.put(Integer.valueOf(nGesture), Integer.valueOf(isOpen ? 1 : 0));
        }
    }

    public boolean getGestureState(int nGesture) {
        if (this.OPPODEBUG) {
            Log.d(TAG, "OppoExService  setGestureState mGestureStatesMap = " + this.mGestureStatesMap);
        }
        return ((Integer) this.mGestureStatesMap.get(Integer.valueOf(nGesture))).intValue() == 1;
    }
}
