package com.android.server.am;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ColorEdgeTouchManagerService implements IColorEdgeTouchManager {
    private static final String DUMP_DEFAULT_PARAM = "adb shell dumpsys activity edgetouch default_param";
    private static final String DUMP_DIRECTION = "adb shell dumpsys activity edgetouch  direction";
    private static final String DUMP_PARAM = "adb shell dumpsys activity edgetouch  param";
    private static final String EDGE_TOUCH_DEFAULT_PARAM = "default_param";
    /* access modifiers changed from: private */
    public static int EDGE_TOUCH_DIERECTION_0 = 0;
    /* access modifiers changed from: private */
    public static int EDGE_TOUCH_DIERECTION_270 = 2;
    /* access modifiers changed from: private */
    public static int EDGE_TOUCH_DIERECTION_90 = 1;
    private static final String EDGE_TOUCH_DIRECTION = "direction";
    private static final String EDGE_TOUCH_PANEL_DIRECTION_PATH = "/proc/touchpanel/oppo_tp_direction";
    private static final String EDGE_TOUCH_PARAM = "param";
    public static final String EDGE_TOUCH_PREVENT_DUMP_CMD = "edgetouch";
    private static final String OPEARATION_FILE = "/proc/touchpanel/kernel_grip_handle";
    private static final String TAG = "ColorEdgeTouchManagerService";
    public static boolean mDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorEdgeTouchManagerService sInstance;
    private ActivityManagerService mAms = null;
    private List<String> mDefaultCmd = new ArrayList();
    boolean mDynamicDebug = false;
    private OppoEdgeTouchHandler mHandler;
    private Map<String, List<String>> mRules = new HashMap();
    private boolean mSupportEdgeTouchPrevent = false;

    public void init(ActivityManagerService ams) {
        log(TAG, "init");
        this.mAms = ams;
        registerLogModule();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new OppoEdgeTouchHandler(thread.getLooper());
        try {
            this.mSupportEdgeTouchPrevent = new File(OPEARATION_FILE).exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void log(String tag, String msg) {
        if (this.mDynamicDebug || mDebug) {
            Slog.i(TAG, msg);
        }
    }

    public static ColorEdgeTouchManagerService getInstance() {
        if (sInstance == null) {
            synchronized (ColorEdgeTouchManagerService.class) {
                if (sInstance == null) {
                    sInstance = new ColorEdgeTouchManagerService();
                }
            }
        }
        return sInstance;
    }

    private class OppoEdgeTouchHandler extends Handler {
        public OppoEdgeTouchHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
        }
    }

    public void setCallRules(String callPkg, Map<String, List<String>> rulesMap) {
        this.mRules = rulesMap;
    }

    public boolean isSupportEdgeTouchPrevent() {
        log(TAG, "isSupportEdgeTouchPrevent = " + this.mSupportEdgeTouchPrevent);
        return this.mSupportEdgeTouchPrevent;
    }

    public synchronized boolean writeEdgeTouchPreventParam(String callPkg, String scenePkg, List<String> paramCmdList) {
        if (!isSupportEdgeTouchPrevent()) {
            return false;
        }
        boolean result = writeCmdInner(paramCmdList);
        log(TAG, "writeEdgeTouchPreventParam   scenePkg" + scenePkg + " result = " + result);
        return result;
    }

    public synchronized boolean resetDefaultEdgeTouchPreventParam(String callPkg) {
        if (!isSupportEdgeTouchPrevent()) {
            return false;
        }
        return writeCmdInner(this.mDefaultCmd);
    }

    public void setDefaultEdgeTouchPreventParam(String callPkg, List<String> paramCmdList) {
        if (isSupportEdgeTouchPrevent()) {
            this.mDefaultCmd = paramCmdList;
            log(TAG, "setDefault  commands is " + paramCmdList);
        }
    }

    private boolean writeCmdInner(final List<String> commands) {
        if (this.mHandler == null) {
            return false;
        }
        if (commands == null || commands.isEmpty()) {
            log(TAG, "writeCmd  commands is null or empty");
            return false;
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.am.ColorEdgeTouchManagerService.AnonymousClass1 */

            public void run() {
                boolean unused = ColorEdgeTouchManagerService.this.writeCmdLock(commands);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    public boolean writeCmdLock(List<String> commands) {
        boolean result;
        if (commands == null || commands.isEmpty()) {
            log(TAG, "writeCmd  commands is null or empty");
            return false;
        }
        File file = new File(OPEARATION_FILE);
        if (!file.exists()) {
            log(TAG, "/proc/touchpanel/kernel_grip_handle is not exists!  writeCmd fail !");
            return false;
        }
        FileWriter fr = null;
        try {
            long start = System.currentTimeMillis();
            log(TAG, "writeCmd thread is  " + Thread.currentThread().getName());
            FileWriter fr2 = new FileWriter(file);
            for (String command : commands) {
                fr2.write(command);
                fr2.flush();
                log(TAG, "writeCmd >>>> " + command);
            }
            log(TAG, "write all " + commands.size() + " cmd time is " + (System.currentTimeMillis() - start) + " ms");
            result = true;
            try {
                fr2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            log(TAG, "writeCmd excption " + e2);
            result = false;
            e2.printStackTrace();
            if (fr != null) {
                fr.close();
            }
        } catch (Throwable th) {
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
        if (this.mDynamicDebug) {
            log(TAG, "kernel_grip_handle  begain --------------------------- ");
            log(TAG, "\n" + readFile(OPEARATION_FILE) + "\n");
            log(TAG, "kernel_grip_handle  end    --------------------------- ");
        }
        return result;
    }

    public void updateRotation(final int rotation) {
        OppoEdgeTouchHandler oppoEdgeTouchHandler = this.mHandler;
        if (oppoEdgeTouchHandler != null) {
            oppoEdgeTouchHandler.post(new Runnable() {
                /* class com.android.server.am.ColorEdgeTouchManagerService.AnonymousClass2 */

                public void run() {
                    int i = rotation;
                    if (i == 0) {
                        ColorEdgeTouchManagerService colorEdgeTouchManagerService = ColorEdgeTouchManagerService.this;
                        colorEdgeTouchManagerService.log(ColorEdgeTouchManagerService.TAG, "the screen  rotation is ROTATION_0 ,int rotation = " + rotation);
                        ColorEdgeTouchManagerService.this.writeTouchPanelDirection(ColorEdgeTouchManagerService.EDGE_TOUCH_DIERECTION_0);
                    } else if (i == 1) {
                        ColorEdgeTouchManagerService colorEdgeTouchManagerService2 = ColorEdgeTouchManagerService.this;
                        colorEdgeTouchManagerService2.log(ColorEdgeTouchManagerService.TAG, "the screen  rotation is ROTATION_90 ,int rotation = " + rotation);
                        ColorEdgeTouchManagerService.this.writeTouchPanelDirection(ColorEdgeTouchManagerService.EDGE_TOUCH_DIERECTION_90);
                    } else if (i == 2) {
                        ColorEdgeTouchManagerService colorEdgeTouchManagerService3 = ColorEdgeTouchManagerService.this;
                        colorEdgeTouchManagerService3.log(ColorEdgeTouchManagerService.TAG, "the screen  rotation is ROTATION_180 ,int rotation = " + rotation);
                        ColorEdgeTouchManagerService.this.writeTouchPanelDirection(ColorEdgeTouchManagerService.EDGE_TOUCH_DIERECTION_0);
                    } else if (i == 3) {
                        ColorEdgeTouchManagerService colorEdgeTouchManagerService4 = ColorEdgeTouchManagerService.this;
                        colorEdgeTouchManagerService4.log(ColorEdgeTouchManagerService.TAG, "the screen  rotation is ROTATION_270 ,int rotation = " + rotation);
                        ColorEdgeTouchManagerService.this.writeTouchPanelDirection(ColorEdgeTouchManagerService.EDGE_TOUCH_DIERECTION_270);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void writeTouchPanelDirection(int edgeRotation) {
        File file = new File(EDGE_TOUCH_PANEL_DIRECTION_PATH);
        String rotationString = String.valueOf(edgeRotation);
        log(TAG, "write edge rotation " + edgeRotation);
        if (!file.exists()) {
            log(TAG, "writeTouchPanelDirection >>> not exists: " + file);
            return;
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(rotationString.getBytes());
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (out != null) {
                out.close();
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
        if (this.mDynamicDebug) {
            log(TAG, "tp_direction: " + readFile(EDGE_TOUCH_PANEL_DIRECTION_PATH));
        }
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug old value = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug new value = " + getInstance().mDynamicDebug);
        if (on) {
            dumpInfo();
        }
    }

    public void setDynamicDebugSwitch(boolean dynamicDebug) {
        this.mDynamicDebug = dynamicDebug;
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorEdgeTouchManagerService.class.getName());
            Slog.i(TAG, "invoke end!");
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

    private String readFile(String path) {
        StringBuffer buffer = new StringBuffer();
        InputStream is = null;
        BufferedReader reader = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                log(TAG, path + " is not exists!");
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    is.close();
                }
                return "";
            }
            InputStream is2 = new FileInputStream(file);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(is2));
            for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                buffer.append(line);
                buffer.append("\n");
            }
            try {
                reader2.close();
                is2.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return buffer.toString();
        } catch (Exception e3) {
            log(TAG, "read fail\n" + e3);
            if (reader != null) {
                reader.close();
            }
            if (is != null) {
                is.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                    throw th;
                }
            }
            if (is != null) {
                is.close();
            }
            throw th;
        }
    }

    private void dumpInfo() {
        log(TAG, "dump begin ----------------------------------------------");
        String paramStr = readFile(OPEARATION_FILE);
        log(TAG, "param:\n" + paramStr);
        String directionStr = readFile(EDGE_TOUCH_PANEL_DIRECTION_PATH);
        log(TAG, "direction:\n" + directionStr);
        log(TAG, "default_param:\n");
        List<String> list = this.mDefaultCmd;
        if (list != null) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                log(TAG, it.next() + ":\n");
            }
        }
        log(TAG, "Rules:\n" + this.mRules);
        log(TAG, "dump end     ----------------------------------------------");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ColorEdgeTouchManagerService  dump  begin -------------------------------");
        if (args.length == 2) {
            String target = args[1];
            if (EDGE_TOUCH_PARAM.equals(target)) {
                String paramStr = readFile(OPEARATION_FILE, pw);
                pw.println("param:\n" + paramStr);
            } else if (EDGE_TOUCH_DIRECTION.equals(target)) {
                String directionStr = readFile(EDGE_TOUCH_PANEL_DIRECTION_PATH, pw);
                pw.println("direction:\n" + directionStr);
            } else if (EDGE_TOUCH_DEFAULT_PARAM.equals(target)) {
                pw.println("default_param:\n");
                List<String> list = this.mDefaultCmd;
                if (list != null) {
                    Iterator<String> it = list.iterator();
                    while (it.hasNext()) {
                        pw.println(it.next() + ":\n");
                    }
                }
            } else {
                pw.println("ColorEdgeTouchManagerService dump like this :\nadb shell dumpsys activity edgetouch  param\nadb shell dumpsys activity edgetouch default_param\nadb shell dumpsys activity edgetouch  direction");
            }
        } else {
            pw.println("ColorEdgeTouchManagerService dump like this :\nadb shell dumpsys activity edgetouch  param\nadb shell dumpsys activity edgetouch default_param\nadb shell dumpsys activity edgetouch  direction");
        }
        pw.println("ColorEdgeTouchManagerService  dump  end  -------------------------------");
    }

    private String readFile(String path, PrintWriter pw) {
        StringBuffer buffer = new StringBuffer();
        InputStream is = null;
        BufferedReader reader = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                pw.println(path + " is not exists!");
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    is.close();
                }
                return "";
            }
            InputStream is2 = new FileInputStream(file);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(is2));
            for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                buffer.append(line);
                buffer.append("\n");
            }
            try {
                reader2.close();
                is2.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return buffer.toString();
        } catch (Exception e3) {
            pw.println("read fail\n" + e3);
            if (reader != null) {
                reader.close();
            }
            if (is != null) {
                is.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                    throw th;
                }
            }
            if (is != null) {
                is.close();
            }
            throw th;
        }
    }
}
