package android.operator;

import android.operator.IOppoOperatorManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.color.antivirus.tencent.TRPEngManager;
import java.util.Collections;
import java.util.Map;

public class OppoOperatorManager implements IOppoOperator {
    public static final boolean SERVICE_ENABLED = SystemProperties.getBoolean("ro.oppo.opt_enabled", false);
    public static final String SERVICE_NAME = "operator";
    private static final String TAG = "OppoOperatorManager";
    private static final Map mEmptyMap = Collections.emptyMap();
    private static volatile OppoOperatorManager sManager = null;
    private IOppoOperatorManager mService;

    private OppoOperatorManager(IOppoOperatorManager service) {
        this.mService = service;
    }

    public static OppoOperatorManager getInstance() {
        if (sManager == null) {
            synchronized (OppoOperatorManager.class) {
                if (sManager == null) {
                    IOppoOperatorManager mService2 = IOppoOperatorManager.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
                    if (SERVICE_ENABLED && mService2 != null) {
                        sManager = new OppoOperatorManager(mService2);
                    } else if (!SERVICE_ENABLED) {
                        sManager = new OppoOperatorDummyManager(null);
                    } else {
                        Slog.e(TAG, "Whoops, service not initiated yet , print caller stack " + Debug.getCallers(9));
                        return null;
                    }
                }
            }
        }
        return sManager;
    }

    @Override // android.operator.IOppoOperator
    public void testAidl() {
        try {
            if (this.mService != null) {
                this.mService.testAidl();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "testAidl ", ex);
        }
    }

    @Override // android.operator.IOppoOperator
    public Map getConfigMap(String config) {
        try {
            if (this.mService == null) {
                return null;
            }
            long startTime = System.currentTimeMillis();
            Bundle bundle = new Bundle();
            bundle.putString(TRPEngManager.CONFIG, config);
            Map configMap = this.mService.getConfigMap(bundle);
            Log.i(TAG, "getConfigMap " + config + " took " + (System.currentTimeMillis() - startTime) + "ms");
            return configMap;
        } catch (RemoteException ex) {
            Log.e(TAG, "getConfigMap ", ex);
        } catch (Throwable th) {
        }
        return null;
    }

    @Override // android.operator.IOppoOperator
    public void grantCustomizedRuntimePermissions() {
        try {
            if (this.mService != null) {
                long startTime = System.currentTimeMillis();
                this.mService.grantCustomizedRuntimePermissions();
                Log.i(TAG, "grantCustomizedPermissions  took " + (System.currentTimeMillis() - startTime) + "ms");
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "grantCustomizedPermissions ", ex);
        }
    }

    @Override // android.operator.IOppoOperator
    public boolean isDynamicFeatureEnabled() {
        try {
            if (this.mService != null) {
                return this.mService.isDynamicFeatureEnabled();
            }
            return false;
        } catch (RemoteException ex) {
            Log.e(TAG, "isDynamicFeatureEnabled ", ex);
        } catch (Throwable th) {
        }
        return false;
    }

    @Override // android.operator.IOppoOperator
    public void notifySmartCustomizationStart() {
        try {
            if (this.mService != null) {
                this.mService.notifySmartCustomizationStart();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "notifySmartCustomizationStart ", ex);
        }
    }

    @Override // android.operator.IOppoOperator
    public boolean hasFeatureDynamiclyEnabeld(String name) {
        try {
            if (this.mService != null) {
                return this.mService.hasFeatureDynamiclyEnabeld(name);
            }
            return false;
        } catch (RemoteException ex) {
            Log.e(TAG, "hasFeatureDynamiclyEnabeld " + name, ex);
        } catch (Throwable th) {
        }
        return false;
    }

    @Override // android.operator.IOppoOperator
    public boolean isInSimTriggeredSystemBlackList(String pkgName) {
        try {
            if (this.mService != null) {
                return this.mService.isInSimTriggeredSystemBlackList(pkgName);
            }
            return false;
        } catch (RemoteException ex) {
            Log.e(TAG, "isInSimTriggeredSystemBlackList " + pkgName, ex);
        } catch (Throwable th) {
        }
        return false;
    }

    private static class OppoOperatorDummyManager extends OppoOperatorManager implements IOppoOperator {
        private OppoOperatorDummyManager(IOppoOperatorManager service) {
            super(service);
        }

        @Override // android.operator.OppoOperatorManager, android.operator.IOppoOperator
        public void testAidl() {
            Log.i(OppoOperatorManager.TAG, "dummy testAidl");
        }

        @Override // android.operator.OppoOperatorManager, android.operator.IOppoOperator
        public Map getConfigMap(String config) {
            return OppoOperatorManager.mEmptyMap;
        }

        @Override // android.operator.OppoOperatorManager, android.operator.IOppoOperator
        public void grantCustomizedRuntimePermissions() {
            Log.i(OppoOperatorManager.TAG, "dummy grantCustomizedPermissions");
        }

        @Override // android.operator.OppoOperatorManager, android.operator.IOppoOperator
        public boolean isDynamicFeatureEnabled() {
            return false;
        }

        @Override // android.operator.OppoOperatorManager, android.operator.IOppoOperator
        public void notifySmartCustomizationStart() {
            Log.i(OppoOperatorManager.TAG, "dummy notifySmartCustomizationStart");
        }

        @Override // android.operator.OppoOperatorManager, android.operator.IOppoOperator
        public boolean hasFeatureDynamiclyEnabeld(String name) {
            return false;
        }

        @Override // android.operator.OppoOperatorManager, android.operator.IOppoOperator
        public boolean isInSimTriggeredSystemBlackList(String pkgName) {
            return false;
        }
    }
}
