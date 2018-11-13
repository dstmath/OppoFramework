package com.android.server;

import android.util.Log;
import com.android.server.oppo.IElsaManager;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoDynamicLogManager {
    public static final String INVOKE_DUMP_NAME = "dump";
    public static String INVOKE_FUNTION_NAME = null;
    public static String OPPO_DYNAMIC_LOG = null;
    private static final String TAG = "OppoDynamicLogManager";
    private static OppoDynamicLogManager mDynamicLogManager;
    List<String> mModuleList;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.OppoDynamicLogManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.OppoDynamicLogManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.OppoDynamicLogManager.<clinit>():void");
    }

    public OppoDynamicLogManager() {
        this.mModuleList = new ArrayList();
    }

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
            String str = INVOKE_FUNTION_NAME;
            Class[] clsArr = new Class[1];
            clsArr[0] = Boolean.TYPE;
            Method m = cls.getDeclaredMethod(str, clsArr);
            Object newInstance = cls.newInstance();
            Object[] objArr = new Object[1];
            objArr[0] = Boolean.valueOf(on);
            m.invoke(newInstance, objArr);
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
            String str = INVOKE_DUMP_NAME;
            Class[] clsArr = new Class[1];
            clsArr[0] = String.class;
            Method m = cls.getDeclaredMethod(str, clsArr);
            Object newInstance = cls.newInstance();
            Object[] objArr = new Object[1];
            objArr[0] = arg;
            m.invoke(newInstance, objArr);
        } catch (Exception e) {
            Log.i(TAG, "invokeModuleOperation failed, e=" + e);
        }
    }

    public void handleOppoDynamicLog(PrintWriter pw, String[] args, int opti) {
        Log.i(TAG, "handleOppoDynamicLog !!! ");
        pw.println("handleOppoDynamicLog, opti:" + opti + ", args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("handleOppoDynamicLog, args[" + index + "]:" + args[index]);
        }
        if (args.length < 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutOppoDynamicLogHelp(pw);
            return;
        }
        if (args.length == 3) {
            handleOppoDynamicLogSwtich(pw, args, opti);
        } else if (args.length == 4) {
            handleOppoDynamicDump(pw, args, opti);
        }
    }

    private void handleOppoDynamicLogSwtich(PrintWriter pw, String[] args, int opti) {
        String tag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("handleOppoDynamicLog, tag:" + tag + ", on:" + on);
        boolean moduleInList = false;
        boolean isAll = false;
        for (String moduleName : this.mModuleList) {
            if (!moduleName.equals(tag)) {
                if ("all".equals(tag)) {
                    isAll = true;
                    break;
                }
            }
            invokeModuleOperation(moduleName, on);
            moduleInList = true;
            break;
        }
        if (isAll) {
            for (String moduleName2 : this.mModuleList) {
                invokeModuleOperation(moduleName2, on);
            }
            return;
        }
        if (!moduleInList) {
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
            for (String moduleName : this.mModuleList) {
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

    protected void logoutOppoDynamicLogHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("Please according to the following example");
        pw.println("cmd: dumpsys activity coloros-log modulename 0/1");
        pw.println("switch: open log use 1 / close log use 0");
        pw.println(IElsaManager.EMPTY_PACKAGE);
        pw.println("cmd: dumpsys activity coloros-log modulename dump args");
        pw.println("args: dump parameter name");
        pw.println("--------------------------------------------------------");
        pw.println("modulename list:");
        for (String str : this.mModuleList) {
            pw.println("modulename: " + str);
        }
        pw.println("--------------------------------------------------------");
        pw.println("********************** Help end.  **********************");
    }
}
