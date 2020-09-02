package com.android.server;

import android.util.Log;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OppoDynamicLogManager implements IColorDynamicLogManager {
    public static final String INVOKE_DUMP_NAME = "dump";
    public static String INVOKE_FUNTION_NAME = "openLog";
    public static String OPPO_DYNAMIC_LOG = "coloros-log";
    private static final String TAG = "OppoDynamicLogManager";
    private static OppoDynamicLogManager mDynamicLogManager = null;
    List<String> mModuleList = new ArrayList();

    public static OppoDynamicLogManager getInstance() {
        if (mDynamicLogManager == null) {
            mDynamicLogManager = new OppoDynamicLogManager();
        }
        return mDynamicLogManager;
    }

    public static void invokeRegisterLogModule(String module) {
        Log.i(TAG, "invokeRegisterLogModule  " + module);
        Log.i(TAG, "invokeRegisterLogModule  instance == " + getInstance());
        getInstance().registerLogModule(module);
    }

    public static void invokeUnRegisterLogModule(String module) {
        Log.i(TAG, "invokeUnRegisterLogModule  " + module);
        Log.i(TAG, "invokeUnRegisterLogModule  instance == " + getInstance());
        getInstance().unRegisterLogModule(module);
    }

    public boolean registerLogModule(String module) {
        Log.i(TAG, "registerLogModule  " + module);
        if (this.mModuleList.contains(module)) {
            return false;
        }
        this.mModuleList.add(module);
        return true;
    }

    public boolean unRegisterLogModule(String module) {
        Log.i(TAG, "unRegisterLogModule  " + module);
        if (!this.mModuleList.contains(module)) {
            return false;
        }
        this.mModuleList.remove(module);
        return true;
    }

    public void invokeModuleOperation(String moduleName, boolean on) {
        Log.i(TAG, "invokeModuleOperation !!! ");
        try {
            Log.i(TAG, "invoke " + moduleName);
            Class<?> cls = Class.forName(moduleName);
            cls.getDeclaredMethod(INVOKE_FUNTION_NAME, Boolean.TYPE).invoke(cls.newInstance(), Boolean.valueOf(on));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    public void invokeModuleOperation(String moduleName, String arg) {
        Log.i(TAG, "invokeModuleOperation !!! ");
        try {
            Log.i(TAG, "invoke " + moduleName);
            Class<?> cls = Class.forName(moduleName);
            cls.getDeclaredMethod(INVOKE_DUMP_NAME, String.class).invoke(cls.newInstance(), arg);
        } catch (Exception e) {
            Log.i(TAG, "invokeModuleOperation failed, e=" + e);
        }
    }

    public void handleDynamicLog(PrintWriter pw, String[] args, int opti) {
        Log.i(TAG, "handleOppoDynamicLog !!! ");
        pw.println("handleOppoDynamicLog, opti:" + opti + ", args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("handleOppoDynamicLog, args[" + index + "]:" + args[index]);
        }
        if (args.length < 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutOppoDynamicLogHelp(pw);
        } else if (args.length == 3) {
            handleOppoDynamicLogSwtich(pw, args, opti);
        } else if (args.length == 4) {
            handleOppoDynamicDump(pw, args, opti);
        }
    }

    private void handleOppoDynamicLogSwtich(PrintWriter pw, String[] args, int opti) {
        String tag = args[1];
        boolean on = "1".equals(args[2]);
        pw.println("handleOppoDynamicLog, tag:" + tag + ", on:" + on);
        boolean moduleInList = false;
        boolean isAll = false;
        Iterator<String> it = this.mModuleList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String moduleName = it.next();
            if (!moduleName.equals(tag)) {
                if ("all".equals(tag)) {
                    isAll = true;
                    break;
                }
            } else {
                invokeModuleOperation(moduleName, on);
                moduleInList = true;
                break;
            }
        }
        if (isAll) {
            for (String moduleName2 : this.mModuleList) {
                invokeModuleOperation(moduleName2, on);
            }
        } else if (!moduleInList) {
            pw.println("Failed! Invalid argument! Type cmd for help: dumpsys activity log");
        }
    }

    private void handleOppoDynamicDump(PrintWriter pw, String[] args, int opti) {
        String tag = args[1];
        String dump = args[2];
        String arg = args[3];
        if (dump != null && INVOKE_DUMP_NAME.equals(dump) && arg != null && arg.length() != 0) {
            pw.println("handleOppoDynamicDump: adb shell dumpsys activity coloros-log " + tag + " " + dump + " " + arg);
            boolean moduleInList = false;
            Iterator<String> it = this.mModuleList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String moduleName = it.next();
                if (moduleName.equals(tag)) {
                    invokeModuleOperation(moduleName, arg);
                    moduleInList = true;
                    break;
                }
            }
            if (!moduleInList) {
                pw.println("Failed! Invalid argument! Type cmd for help: dumpsys activity log");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logoutOppoDynamicLogHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("Please according to the following example");
        pw.println("cmd: dumpsys activity coloros-log modulename 0/1");
        pw.println("switch: open log use 1 / close log use 0");
        pw.println("");
        pw.println("cmd: dumpsys activity coloros-log modulename dump args");
        pw.println("args: dump parameter name");
        pw.println("--------------------------------------------------------");
        pw.println("modulename list:");
        Iterator<String> it = this.mModuleList.iterator();
        while (it.hasNext()) {
            pw.println("modulename: " + it.next());
        }
        pw.println("--------------------------------------------------------");
        pw.println("********************** Help end.  **********************");
    }
}
