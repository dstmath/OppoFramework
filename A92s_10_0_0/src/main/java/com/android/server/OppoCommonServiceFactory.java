package com.android.server;

import android.common.IOppoCommonFactory;
import android.content.Context;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.pm.Installer;
import com.android.server.pm.OppoUserDataPreparer;
import com.android.server.pm.OppoUserManagerService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserManagerService;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.OppoTransactionFactory;
import com.android.server.wm.WindowManagerService;
import java.lang.reflect.InvocationTargetException;

public abstract class OppoCommonServiceFactory implements IOppoCommonFactory {
    private static final String AMS_CLASSNAME = "com.android.server.am.OppoActivityManagerService";
    private static final String ATMS_CLASSNAME = "com.android.server.wm.OppoActivityTaskManagerService";
    private static final String MY_TAG = "turing_decoupling";
    private static final String PMS_CLASSNAME = "com.android.server.pm.OppoPackageManagerService";
    private static final String WMS_CLASSNAME = "com.android.server.wm.OppoWindowManagerService";
    private final String TAG = getClass().getSimpleName();

    public static final ActivityManagerService getActivityManagerService(Context context, ActivityTaskManagerService atm) {
        return createActivityManagerService(context, atm);
    }

    public static final ActivityTaskManagerService getActivityTaskManagerService(Context context) {
        return createActivityTaskManagerService(context);
    }

    public static final WindowManagerService getWindowManagerService(Context context, InputManagerService im, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy, ActivityTaskManagerService atm, OppoTransactionFactory transactionFactory) {
        return createWindowManagerService(context, im, showBootMsgs, onlyCore, policy, atm, transactionFactory);
    }

    public static final PackageManagerService getPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        return createPackageManagerService(context, installer, factoryTest, onlyCore);
    }

    public static final UserManagerService getUserManagerService(Context context, PackageManagerService pm, OppoUserDataPreparer userDataPreparer, Object packagesLock) {
        return new OppoUserManagerService(context, pm, userDataPreparer, packagesLock);
    }

    static Object newInstance(String className) throws Exception {
        return Class.forName(className).getConstructor(new Class[0]).newInstance(new Object[0]);
    }

    /* access modifiers changed from: protected */
    public void warn(String methodName) {
        Slog.w(this.TAG, methodName);
    }

    private static WindowManagerService createWindowManagerService(Context context, InputManagerService im, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy, ActivityTaskManagerService atm, OppoTransactionFactory transactionFactory) {
        Slog.i(MY_TAG, "createWindowManagerService reflect");
        try {
            return (WindowManagerService) Class.forName(WMS_CLASSNAME).getDeclaredConstructor(Context.class, InputManagerService.class, Boolean.TYPE, Boolean.TYPE, WindowManagerPolicy.class, ActivityTaskManagerService.class, OppoTransactionFactory.class).newInstance(context, im, Boolean.valueOf(showBootMsgs), Boolean.valueOf(onlyCore), policy, atm, transactionFactory);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InstantiationException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return null;
        }
    }

    private static ActivityManagerService createActivityManagerService(Context context, ActivityTaskManagerService atms) {
        Slog.i(MY_TAG, "createActivityManagerService reflect");
        try {
            return (ActivityManagerService) Class.forName(AMS_CLASSNAME).getDeclaredConstructor(Context.class, ActivityTaskManagerService.class).newInstance(context, atms);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InstantiationException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return null;
        }
    }

    private static ActivityTaskManagerService createActivityTaskManagerService(Context context) {
        Slog.i(MY_TAG, "createActivityTaskManagerService reflect");
        try {
            return (ActivityTaskManagerService) Class.forName(ATMS_CLASSNAME).getDeclaredConstructor(Context.class).newInstance(context);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InstantiationException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return null;
        }
    }

    private static PackageManagerService createPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        Slog.i(MY_TAG, "createPackageManagerService reflect");
        try {
            return (PackageManagerService) Class.forName(PMS_CLASSNAME).getDeclaredConstructor(Context.class, Installer.class, Boolean.TYPE, Boolean.TYPE).newInstance(context, installer, Boolean.valueOf(factoryTest), Boolean.valueOf(onlyCore));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InstantiationException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            return null;
        }
    }
}
