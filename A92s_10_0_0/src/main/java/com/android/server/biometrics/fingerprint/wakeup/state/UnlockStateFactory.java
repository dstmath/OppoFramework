package com.android.server.biometrics.fingerprint.wakeup.state;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class UnlockStateFactory {
    private String TAG = "FingerprintService.UnlockStateFactory";
    public Context mContext;
    private HandlerThread mHandlerThread;
    public FingerprintService.IUnLocker mIUnLocker;
    private Looper mLooper;
    public HashMap<String, UnlockState> mStateFactoryInstance = new HashMap<>();

    public UnlockStateFactory(Context context, FingerprintService.IUnLocker unLocker) {
        this.mContext = context;
        this.mIUnLocker = unLocker;
        this.mHandlerThread = new HandlerThread("UnlockState thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e(this.TAG, "mLooper null");
        }
    }

    public UnlockState createUnlockState(String actionName) {
        String str = this.TAG;
        LogUtil.v(str, "create unlock action =" + actionName);
        UnlockState unlockStateInstance = (UnlockState) getInstance("com.android.server.fingerprint.wakeup.state." + actionName, new Class[]{Context.class, FingerprintService.IUnLocker.class, Looper.class}, new Object[]{this.mContext, this.mIUnLocker, this.mLooper});
        if (unlockStateInstance != null) {
            LogUtil.v(this.TAG, "create unlock state success");
            this.mStateFactoryInstance.put(actionName, unlockStateInstance);
        }
        return unlockStateInstance;
    }

    public boolean deleteUnlockState(String actionName) {
        String str = this.TAG;
        LogUtil.v(str, "delete unlock state action =" + actionName);
        if (this.mStateFactoryInstance.get(actionName) == null) {
            return false;
        }
        this.mStateFactoryInstance.put(actionName, null);
        this.mStateFactoryInstance.remove(actionName);
        return true;
    }

    public Object getInstance(String className, Class[] parameterClass, Object[] parameter) {
        try {
            return Class.forName(className).getConstructor(parameterClass).newInstance(parameter);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LogUtil.e(this.TAG, e.toString());
            return null;
        } catch (InstantiationException e2) {
            e2.printStackTrace();
            LogUtil.e(this.TAG, e2.toString());
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            LogUtil.e(this.TAG, e3.toString());
            return null;
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
            LogUtil.e(this.TAG, e4.toString());
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            LogUtil.e(this.TAG, e5.toString());
            return null;
        } catch (NoSuchMethodException e6) {
            e6.printStackTrace();
            LogUtil.e(this.TAG, e6.toString());
            return null;
        } catch (SecurityException e7) {
            e7.printStackTrace();
            LogUtil.e(this.TAG, e7.toString());
            return null;
        }
    }
}
