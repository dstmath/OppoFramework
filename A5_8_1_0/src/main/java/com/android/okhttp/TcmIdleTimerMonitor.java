package com.android.okhttp;

import com.android.okhttp.internal.Platform;
import com.quicinc.tcmiface.DpmTcmIface;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Method;

public class TcmIdleTimerMonitor implements DpmTcmIface {
    private static Object lockObj = new Object();
    private static Method mTcmRegisterMethod = null;
    private static Object tcmClient = null;
    private ConnectionPool connectionPool;
    Object result = null;

    public TcmIdleTimerMonitor(ConnectionPool connPool) {
        synchronized (lockObj) {
            this.connectionPool = connPool;
            try {
                if (mTcmRegisterMethod == null || tcmClient == null) {
                    Class tcmClass = new PathClassLoader("/system/framework/tcmclient.jar", ClassLoader.getSystemClassLoader()).loadClass("com.qti.tcmclient.DpmTcmClient");
                    tcmClient = tcmClass.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                    mTcmRegisterMethod = tcmClass.getDeclaredMethod("registerTcmMonitor", new Class[]{DpmTcmIface.class});
                }
                if (!(mTcmRegisterMethod == null || tcmClient == null)) {
                    this.result = mTcmRegisterMethod.invoke(tcmClient, new Object[]{this});
                }
            } catch (ClassNotFoundException e) {
            } catch (Exception e2) {
                Platform.get().logW("tcmclient load failed: " + e2);
            }
        }
        return;
    }

    public void OnCloseIdleConn() {
        this.connectionPool.closeIdleConnections();
    }
}
