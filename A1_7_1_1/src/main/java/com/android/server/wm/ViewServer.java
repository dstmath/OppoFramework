package com.android.server.wm;

import android.util.Slog;
import com.android.server.oppo.IElsaManager;
import com.android.server.wm.WindowManagerService.WindowChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
class ViewServer implements Runnable {
    private static final String COMMAND_PROTOCOL_VERSION = "PROTOCOL";
    private static final String COMMAND_SERVER_VERSION = "SERVER";
    private static final String COMMAND_WINDOW_MANAGER_AUTOLIST = "AUTOLIST";
    private static final String COMMAND_WINDOW_MANAGER_GET_FOCUS = "GET_FOCUS";
    private static final String COMMAND_WINDOW_MANAGER_LIST = "LIST";
    private static final String LOG_TAG = null;
    private static final String VALUE_PROTOCOL_VERSION = "4";
    private static final String VALUE_SERVER_VERSION = "4";
    public static final int VIEW_SERVER_DEFAULT_PORT = 4939;
    private static final int VIEW_SERVER_MAX_CONNECTIONS = 10;
    private final int mPort;
    private ServerSocket mServer;
    private ArrayList<Socket> mSockets;
    private Thread mThread;
    private ExecutorService mThreadPool;
    private final WindowManagerService mWindowManager;

    class ViewServerWorker implements Runnable, WindowChangeListener {
        private Socket mClient;
        private boolean mNeedFocusedWindowUpdate = false;
        private boolean mNeedWindowListUpdate = false;

        public ViewServerWorker(Socket client) {
            this.mClient = client;
        }

        /* JADX WARNING: Removed duplicated region for block: B:53:0x00fa A:{SYNTHETIC, Splitter: B:53:0x00fa} */
        /* JADX WARNING: Removed duplicated region for block: B:99:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x0101 A:{SYNTHETIC, Splitter: B:57:0x0101} */
        /* JADX WARNING: Removed duplicated region for block: B:74:0x012c A:{SYNTHETIC, Splitter: B:74:0x012c} */
        /* JADX WARNING: Removed duplicated region for block: B:78:0x0133 A:{SYNTHETIC, Splitter: B:78:0x0133} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            IOException e;
            Throwable th;
            BufferedReader in = null;
            try {
                BufferedReader in2 = new BufferedReader(new InputStreamReader(this.mClient.getInputStream()), 1024);
                try {
                    String command;
                    String parameters;
                    boolean result;
                    String request = in2.readLine();
                    int index = request.indexOf(32);
                    if (index == -1) {
                        command = request;
                        parameters = IElsaManager.EMPTY_PACKAGE;
                    } else {
                        command = request.substring(0, index);
                        parameters = request.substring(index + 1);
                    }
                    if (ViewServer.COMMAND_PROTOCOL_VERSION.equalsIgnoreCase(command)) {
                        result = ViewServer.writeValue(this.mClient, "4");
                    } else if (ViewServer.COMMAND_SERVER_VERSION.equalsIgnoreCase(command)) {
                        result = ViewServer.writeValue(this.mClient, "4");
                    } else if (ViewServer.COMMAND_WINDOW_MANAGER_LIST.equalsIgnoreCase(command)) {
                        result = ViewServer.this.mWindowManager.viewServerListWindows(this.mClient);
                    } else if (ViewServer.COMMAND_WINDOW_MANAGER_GET_FOCUS.equalsIgnoreCase(command)) {
                        result = ViewServer.this.mWindowManager.viewServerGetFocusedWindow(this.mClient);
                    } else if (ViewServer.COMMAND_WINDOW_MANAGER_AUTOLIST.equalsIgnoreCase(command)) {
                        result = windowManagerAutolistLoop();
                    } else {
                        result = ViewServer.this.mWindowManager.viewServerWindowCommand(this.mClient, command, parameters);
                    }
                    if (!result) {
                        Slog.w(ViewServer.LOG_TAG, "An error occurred with the command: " + command);
                    }
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (this.mClient != null) {
                        try {
                            this.mClient.close();
                            synchronized (ViewServer.this.mSockets) {
                                ViewServer.this.mSockets.remove(this.mClient);
                            }
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    in = in2;
                } catch (IOException e3) {
                    e22 = e3;
                    in = in2;
                    try {
                        Slog.w(ViewServer.LOG_TAG, "Connection error: ", e22);
                        if (in != null) {
                        }
                        if (this.mClient == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        if (this.mClient != null) {
                            try {
                                this.mClient.close();
                                synchronized (ViewServer.this.mSockets) {
                                    ViewServer.this.mSockets.remove(this.mClient);
                                }
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (in != null) {
                    }
                    if (this.mClient != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e2222 = e4;
                Slog.w(ViewServer.LOG_TAG, "Connection error: ", e2222);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                if (this.mClient == null) {
                    try {
                        this.mClient.close();
                        synchronized (ViewServer.this.mSockets) {
                            ViewServer.this.mSockets.remove(this.mClient);
                        }
                    } catch (IOException e222222) {
                        e222222.printStackTrace();
                    }
                }
            }
        }

        public void windowsChanged() {
            synchronized (this) {
                this.mNeedWindowListUpdate = true;
                notifyAll();
            }
        }

        public void focusChanged() {
            synchronized (this) {
                this.mNeedFocusedWindowUpdate = true;
                notifyAll();
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:30:0x0057 A:{SYNTHETIC, Splitter: B:30:0x0057} */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x0070 A:{SYNTHETIC, Splitter: B:45:0x0070} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean windowManagerAutolistLoop() {
            Throwable th;
            ViewServer.this.mWindowManager.addWindowChangeListener(this);
            BufferedWriter bufferedWriter = null;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.mClient.getOutputStream()));
                while (!Thread.interrupted()) {
                    try {
                        boolean needWindowListUpdate = false;
                        boolean needFocusedWindowUpdate = false;
                        synchronized (this) {
                            while (!this.mNeedWindowListUpdate && !this.mNeedFocusedWindowUpdate) {
                                wait();
                            }
                            if (this.mNeedWindowListUpdate) {
                                this.mNeedWindowListUpdate = false;
                                needWindowListUpdate = true;
                            }
                            if (this.mNeedFocusedWindowUpdate) {
                                this.mNeedFocusedWindowUpdate = false;
                                needFocusedWindowUpdate = true;
                            }
                        }
                        if (needWindowListUpdate) {
                            out.write("LIST UPDATE\n");
                            out.flush();
                        }
                        if (needFocusedWindowUpdate) {
                            out.write("ACTION_FOCUS UPDATE\n");
                            out.flush();
                        }
                    } catch (Exception e) {
                        bufferedWriter = out;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e2) {
                            }
                        }
                        ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                        return true;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedWriter = out;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e3) {
                            }
                        }
                        ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                        throw th;
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
            } catch (Exception e5) {
                if (bufferedWriter != null) {
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                return true;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedWriter != null) {
                }
                ViewServer.this.mWindowManager.removeWindowChangeListener(this);
                throw th;
            }
            return true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.ViewServer.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.ViewServer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.ViewServer.<clinit>():void");
    }

    ViewServer(WindowManagerService windowManager, int port) {
        this.mSockets = new ArrayList();
        this.mWindowManager = windowManager;
        this.mPort = port;
    }

    boolean start() throws IOException {
        synchronized (this) {
            if (this.mThread != null) {
                return false;
            }
            this.mServer = new ServerSocket(this.mPort, 10, InetAddress.getLocalHost());
            this.mThread = new Thread(this, "Remote View Server [port=" + this.mPort + "]");
            this.mThreadPool = Executors.newFixedThreadPool(10);
            this.mThread.start();
            return true;
        }
    }

    boolean stop() {
        synchronized (this) {
            if (this.mThread != null) {
                this.mThread.interrupt();
                if (this.mThreadPool != null) {
                    int i;
                    try {
                        this.mThreadPool.shutdownNow();
                        synchronized (this.mSockets) {
                            for (i = 0; i < this.mSockets.size(); i++) {
                                try {
                                    ((Socket) this.mSockets.get(i)).close();
                                } catch (IOException e) {
                                    Slog.w(LOG_TAG, "Could not close mSockets");
                                }
                            }
                            this.mSockets.clear();
                        }
                    } catch (SecurityException e2) {
                        Slog.w(LOG_TAG, "Could not stop all view server threads");
                        synchronized (this.mSockets) {
                            for (i = 0; i < this.mSockets.size(); i++) {
                                try {
                                    ((Socket) this.mSockets.get(i)).close();
                                } catch (IOException e3) {
                                    Slog.w(LOG_TAG, "Could not close mSockets");
                                }
                            }
                            this.mSockets.clear();
                        }
                    } catch (Throwable th) {
                        Throwable th2 = th;
                        synchronized (this.mSockets) {
                            for (i = 0; i < this.mSockets.size(); i++) {
                                try {
                                    ((Socket) this.mSockets.get(i)).close();
                                } catch (IOException e4) {
                                    Slog.w(LOG_TAG, "Could not close mSockets");
                                }
                            }
                            this.mSockets.clear();
                        }
                    }
                }
                this.mThreadPool = null;
                this.mThread = null;
                try {
                    this.mServer.close();
                    this.mServer = null;
                    return true;
                } catch (IOException e5) {
                    Slog.w(LOG_TAG, "Could not close the view server");
                }
            }
            return false;
        }
    }

    boolean isRunning() {
        return this.mThread != null ? this.mThread.isAlive() : false;
    }

    public void run() {
        while (Thread.currentThread() == this.mThread && this.mServer != null) {
            try {
                Socket client = this.mServer.accept();
                if (this.mThreadPool != null) {
                    synchronized (this.mSockets) {
                        this.mSockets.add(client);
                    }
                    this.mThreadPool.submit(new ViewServerWorker(client));
                } else {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e2) {
                Slog.w(LOG_TAG, "Connection error: ", e2);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002c A:{SYNTHETIC, Splitter: B:15:0x002c} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0036 A:{SYNTHETIC, Splitter: B:21:0x0036} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean writeValue(Socket client, String value) {
        Throwable th;
        BufferedWriter out = null;
        try {
            BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), DumpState.DUMP_PREFERRED_XML);
            try {
                out2.write(value);
                out2.write("\n");
                out2.flush();
                boolean result = true;
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e) {
                        result = false;
                    }
                }
                out = out2;
                return result;
            } catch (Exception e2) {
                out = out2;
                if (out != null) {
                }
            } catch (Throwable th2) {
                th = th2;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (Exception e3) {
            if (out != null) {
                return false;
            }
            try {
                out.close();
                return false;
            } catch (IOException e4) {
                return false;
            }
        } catch (Throwable th3) {
            th = th3;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
    }
}
