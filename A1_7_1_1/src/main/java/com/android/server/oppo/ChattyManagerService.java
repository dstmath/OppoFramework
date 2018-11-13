package com.android.server.oppo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.internal.app.IChattyManager.Stub;
import com.android.server.LocationManagerService;
import com.android.server.ServiceThread;
import com.android.server.notification.NotificationManagerService;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ChattyManagerService {
    private static final int ALARM_INTERVAL = 10800000;
    private static final int BOOT_COMPLETED_DELAY = 5;
    private static final String CHATTY_UPDATE_ACTION = "oppo.intent.action.CHATTY_UPDATE";
    private static final int CMD_REPORT_LOG_STATE = 1;
    private static final int CMD_REPORT_LONG_LOG = 2;
    private static boolean DEBUG = false;
    private static final String FILE_LOG_PATH = "data/system/dropbox/chatty/update.log";
    private static final String FILE_PATH = "data/system/dropbox/chatty/log/";
    private static final String FILE_ROOT_PATH = "data/system/dropbox/";
    private static final String LOG_NUM_PROPERTY = "persist.sys.oppo.chatty_n";
    private static final int MAX_REPORT_TIMES = 10;
    private static final String TAG = "ChattyManagerService";
    private static final String THREAD_NAME = "ChattyManager";
    private static final int UPDATE_INTERVAL = 86400000;
    private String IMEI;
    private AlarmManager mAlarmManager;
    private IBinder mBinder;
    private long mBootTime;
    private Context mContext;
    private boolean mDynLog;
    private boolean mDynProp;
    private final ChattyHandler mHandler;
    private final ServiceThread mHandlerThread;
    private int mReportTimes;
    private boolean mScreenState;
    private final SocketThread mSocketThread;
    private StateReceiver mStateReceiver;
    private UpdateReceiver mUpdateReceiver;

    private class ChattyHandler extends Handler {
        public static final int MESSAGE_BOOT_COMPLETED = 6;
        public static final int MESSAGE_COMPRESSION = 3;
        public static final int MESSAGE_LOGGER_STATE = 7;
        public static final int MESSAGE_MARK_TIMESTAMP = 1;
        public static final int MESSAGE_RECEIVE_DATA = 0;
        public static final int MESSAGE_SCREEN_STATE = 4;
        public static final int MESSAGE_UPDATE = 2;

        public ChattyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0:
                        String strMsg = msg.obj;
                        if (strMsg != null) {
                            String[] args = strMsg.split(",");
                            if (args.length > 1) {
                                int cmd = Integer.parseInt(args[0]);
                                if (cmd == 1 && args.length == 4) {
                                    ChattyManagerService.this.dumpLogFile(args[1], args[2], args[3]);
                                    return;
                                } else {
                                    if (cmd == 2) {
                                    }
                                    return;
                                }
                            }
                            return;
                        }
                        return;
                    case 1:
                        ChattyManagerService.this.writeTimeStamp();
                        return;
                    case 2:
                        ChattyManagerService.this.saveToFileIfNeeded();
                        return;
                    case 3:
                        boolean recount = msg.arg1 > 0;
                        ChattyManagerService.this.doCompression();
                        if (recount) {
                            ChattyManagerService.this.mReportTimes = 0;
                            ChattyManagerService.this.saveRealNumber(0);
                            return;
                        }
                        return;
                    case 4:
                        ChattyManagerService.this.mScreenState = msg.arg1 == 1;
                        if (ChattyManagerService.DEBUG) {
                            Slog.d(ChattyManagerService.TAG, "update screen state, current=" + ChattyManagerService.this.mScreenState);
                            return;
                        }
                        return;
                    case 6:
                        ChattyManagerService.this.mBootTime = (System.currentTimeMillis() / 1000) + 5;
                        ChattyManagerService.this.mReportTimes = ChattyManagerService.this.getRealNumber();
                        if (ChattyManagerService.DEBUG) {
                            Slog.d(ChattyManagerService.TAG, "BOOT_COMPLETED, t=" + ChattyManagerService.this.mBootTime + ", IMEI=" + ChattyManagerService.this.IMEI);
                        }
                        if (ChattyManagerService.this.mAlarmManager != null) {
                            ChattyManagerService.this.mAlarmManager.setRepeating(1, (ChattyManagerService.this.mBootTime * 1000) + NotificationManagerService.TIME_UPLOAD_THRESHOLD, NotificationManagerService.TIME_UPLOAD_THRESHOLD, PendingIntent.getBroadcast(ChattyManagerService.this.mContext, 0, new Intent(ChattyManagerService.CHATTY_UPDATE_ACTION), 0));
                        }
                        ChattyManagerService.this.mHandler.sendEmptyMessage(7);
                        return;
                    case 7:
                        if (ChattyManagerService.this.isPanic()) {
                            new SocketSenderThread("setChattyRate 15000 100").start();
                            return;
                        } else {
                            new SocketSenderThread("setChattyRate 3000 20").start();
                            return;
                        }
                    default:
                        return;
                }
            } catch (NullPointerException e) {
                Slog.d(ChattyManagerService.TAG, "Exception in ChattyHandler.handleMessage: " + e);
            }
            Slog.d(ChattyManagerService.TAG, "Exception in ChattyHandler.handleMessage: " + e);
        }
    }

    private class SocketSenderThread extends Thread {
        private static final String TAG = "SocketSenderThread";
        private String mData;

        public SocketSenderThread(String data) {
            super(TAG);
            this.mData = data;
        }

        public void run() {
            IOException ex;
            LocalSocket localSocket = null;
            try {
                LocalSocket socket = new LocalSocket();
                try {
                    socket.connect(new LocalSocketAddress("logd", Namespace.RESERVED));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()), 256);
                    writer.write(this.mData);
                    writer.write(String.valueOf(0));
                    writer.flush();
                    DataInputStream reader = new DataInputStream(socket.getInputStream());
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            localSocket = socket;
                            return;
                        }
                        Slog.d(TAG, line);
                    }
                } catch (IOException e) {
                    ex = e;
                    localSocket = socket;
                    ex.printStackTrace();
                    try {
                        localSocket.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException e3) {
                ex = e3;
                ex.printStackTrace();
                localSocket.close();
            }
        }
    }

    public class SocketThread extends Thread {
        private static final int MSG_INFO_MAX_SIZE = 1024;
        private static final int MSG_INFO_MIN_SIZE = 3;
        private static final String SOCKET_NAME = "chatty_socket";
        private static final String TAG = "ChattySocketServer";
        private boolean keepRunning = true;
        private final ChattyManagerService mService;
        private LocalServerSocket serverSocket;

        public SocketThread(ChattyManagerService service, String name) {
            super(name);
            this.mService = service;
        }

        public void stopRun() {
            this.keepRunning = false;
        }

        /* JADX WARNING: Failed to extract finally block: empty outs */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            try {
                if (ChattyManagerService.DEBUG) {
                    Slog.d(TAG, "LocalServerSocket SOCKET_NAME = chatty_socket");
                }
                this.serverSocket = new LocalServerSocket(SOCKET_NAME);
                while (this.keepRunning && this.serverSocket != null) {
                    if (ChattyManagerService.DEBUG) {
                        Slog.d(TAG, "wait for new client coming!");
                    }
                    LocalSocket clientSocket = this.serverSocket.accept();
                    if (this.keepRunning) {
                        if (ChattyManagerService.DEBUG) {
                            Slog.d(TAG, "new client coming!");
                        }
                        readFromSocket(clientSocket);
                    }
                }
                try {
                    if (this.serverSocket != null) {
                        this.serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                try {
                    if (this.serverSocket != null) {
                        this.serverSocket.close();
                    }
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            } catch (Throwable th) {
                try {
                    if (this.serverSocket != null) {
                        this.serverSocket.close();
                    }
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
                throw th;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:25:0x007f A:{SYNTHETIC, Splitter: B:25:0x007f} */
        /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0084 A:{Catch:{ IOException -> 0x0088 }} */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x0090 A:{SYNTHETIC, Splitter: B:33:0x0090} */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x0095 A:{Catch:{ IOException -> 0x0099 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void readFromSocket(LocalSocket clientSocket) {
            IOException e;
            Throwable th;
            StringBuilder recvStrBuilder = new StringBuilder();
            InputStream inputStream = null;
            try {
                inputStream = clientSocket.getInputStream();
                byte[] bytes = new byte[1024];
                int readBytes = inputStream.read(bytes, 0, 1024);
                if (ChattyManagerService.DEBUG) {
                    Slog.d(TAG, "readBytes = " + readBytes);
                }
                if (readBytes >= 3) {
                    bytes[readBytes] = (byte) 0;
                    String info = new String(bytes, 0, readBytes);
                    String str;
                    try {
                        if (ChattyManagerService.DEBUG) {
                            Slog.d(TAG, "ClientSocket read info = " + info);
                        }
                        this.mService.receiveSocketFromClient(info);
                        str = info;
                    } catch (IOException e2) {
                        e = e2;
                        str = info;
                        try {
                            e.printStackTrace();
                            if (clientSocket != null) {
                            }
                            if (inputStream == null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (clientSocket != null) {
                                try {
                                    clientSocket.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                    throw th;
                                }
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (clientSocket != null) {
                        }
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                }
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                        return;
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e4) {
                e32 = e4;
                e32.printStackTrace();
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                        return;
                    }
                }
                if (inputStream == null) {
                    inputStream.close();
                }
            }
        }
    }

    class StateReceiver extends BroadcastReceiver {
        public static final String ACTION_MTKLOGGER_STATE_CHANGED = "com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED";

        public StateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction(ACTION_MTKLOGGER_STATE_CHANGED);
            ChattyManagerService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Message msg;
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                ChattyManagerService.this.mHandler.sendEmptyMessage(6);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                msg = ChattyManagerService.this.mHandler.obtainMessage();
                msg.what = 4;
                msg.arg1 = 1;
                msg.sendToTarget();
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                msg = ChattyManagerService.this.mHandler.obtainMessage();
                msg.what = 4;
                msg.arg1 = 0;
                msg.sendToTarget();
            } else if (ACTION_MTKLOGGER_STATE_CHANGED.equals(action)) {
                ChattyManagerService.this.mHandler.sendEmptyMessage(7);
            }
        }
    }

    class UpdateReceiver extends BroadcastReceiver {
        public UpdateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ChattyManagerService.CHATTY_UPDATE_ACTION);
            ChattyManagerService.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (ChattyManagerService.CHATTY_UPDATE_ACTION.equals(intent.getAction())) {
                ChattyManagerService.this.mHandler.sendEmptyMessage(2);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.ChattyManagerService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.ChattyManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.ChattyManagerService.<clinit>():void");
    }

    public ChattyManagerService(Context context) {
        this.mBootTime = 0;
        this.mScreenState = true;
        this.mDynLog = false;
        this.mDynProp = false;
        this.mBinder = new Stub() {
            public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
                if (ChattyManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    writer.println("Permission Denial: can't dump PowerManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                    return;
                }
                if (ChattyManagerService.DEBUG) {
                    Slog.d(ChattyManagerService.TAG, "dump, args=" + args);
                }
                if (args.length >= 1) {
                    String cmd = args[0];
                    if ("debug".equals(cmd)) {
                        if (args.length == 2) {
                            ChattyManagerService.DEBUG = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[1]);
                        } else {
                            writer.println("Invalid argument! Get detail help as bellow:");
                        }
                    } else if ("dyn".equals(cmd)) {
                        if (ChattyManagerService.DEBUG) {
                            Slog.d(ChattyManagerService.TAG, "Dynamic log may be triggered.");
                        }
                        ChattyManagerService.this.mDynLog = true;
                    } else if ("test".equals(cmd)) {
                        if (ChattyManagerService.DEBUG) {
                            Slog.d(ChattyManagerService.TAG, "Manual generate a package.");
                        }
                        ChattyManagerService.this.mHandler.sendEmptyMessage(3);
                    } else if ("socket".equals(cmd)) {
                        new SocketSenderThread("getStatistics 2").start();
                    }
                    return;
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    ChattyManagerService.this.dumpInternal(writer);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        };
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(THREAD_NAME, 10, true);
        this.mHandlerThread.start();
        this.mHandler = new ChattyHandler(this.mHandlerThread.getLooper());
        this.mSocketThread = new SocketThread(this, "ChattyManagerSocket");
        this.mSocketThread.start();
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mReportTimes = 0;
        this.mStateReceiver = new StateReceiver();
        this.mUpdateReceiver = new UpdateReceiver();
        initBinderService();
    }

    private void initBinderService() {
        try {
            if (DEBUG) {
                Slog.i(TAG, "Start Service");
            }
            ServiceManager.addService("chatty", this.mBinder);
        } catch (Throwable e) {
            if (DEBUG) {
                Slog.i(TAG, "Start Service failed", e);
            }
        }
    }

    private void savePSInfo(String fileName) {
        BufferedInputStream bis;
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        FileOutputStream output = null;
        BufferedInputStream bis2 = null;
        BufferedOutputStream bos = null;
        try {
            BufferedOutputStream bos2;
            inputStream = Runtime.getRuntime().exec("ps").getInputStream();
            FileOutputStream output2 = new FileOutputStream(fileName);
            try {
                bis = new BufferedInputStream(inputStream);
                try {
                    bos2 = new BufferedOutputStream(output2);
                } catch (IOException e2) {
                    e = e2;
                    bis2 = bis;
                    output = output2;
                    try {
                        Slog.w(TAG, "savePSInfo() error." + e);
                        try {
                            inputStream.close();
                            output.close();
                            bis2.close();
                            bos.close();
                        } catch (IOException e3) {
                            return;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        try {
                            inputStream.close();
                            output.close();
                            bis2.close();
                            bos.close();
                        } catch (IOException e4) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bis2 = bis;
                    output = output2;
                    inputStream.close();
                    output.close();
                    bis2.close();
                    bos.close();
                    throw th;
                }
            } catch (IOException e5) {
                e = e5;
                output = output2;
                Slog.w(TAG, "savePSInfo() error." + e);
                inputStream.close();
                output.close();
                bis2.close();
                bos.close();
            } catch (Throwable th4) {
                th = th4;
                output = output2;
                inputStream.close();
                output.close();
                bis2.close();
                bos.close();
                throw th;
            }
            try {
                byte[] byt = new byte[1024];
                while (bis.read(byt) != -1) {
                    bos2.write(byt, 0, byt.length);
                    bos2.flush();
                }
                try {
                    inputStream.close();
                    output2.close();
                    bis.close();
                    bos2.close();
                } catch (IOException e6) {
                }
                bos = bos2;
                bis2 = bis;
            } catch (IOException e7) {
                e = e7;
                bos = bos2;
                bis2 = bis;
                output = output2;
                Slog.w(TAG, "savePSInfo() error." + e);
                inputStream.close();
                output.close();
                bis2.close();
                bos.close();
            } catch (Throwable th5) {
                th = th5;
                bos = bos2;
                bis2 = bis;
                output = output2;
                inputStream.close();
                output.close();
                bis2.close();
                bos.close();
                throw th;
            }
        } catch (IOException e8) {
            e = e8;
            Slog.w(TAG, "savePSInfo() error." + e);
            inputStream.close();
            output.close();
            bis2.close();
            bos.close();
        }
    }

    private boolean isPanic() {
        return SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }

    private void dumpLogFileInner(String name, int lines, long time, boolean ps) {
        String logFileName = FILE_PATH + name + "-" + time + ".txt";
        try {
            File destDir = new File(FILE_PATH);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            Runtime.getRuntime().exec("logcat -t " + lines + " -f " + logFileName);
        } catch (IOException e) {
            Slog.w(TAG, "dumpLogFileInner() error." + e);
        }
        if (ps) {
            savePSInfo("data/system/dropbox/chatty/log/PS#" + this.mReportTimes + "-" + time + ".txt");
        }
    }

    private void dumpLogFile(String bufferRate, String logRate, String time) {
        if (this.mDynLog) {
            if (DEBUG) {
                Slog.d(TAG, "Dynamic log is opened, stop record chatty log any more.");
            }
        } else if (this.mDynProp || SystemProperties.getInt("persist.sys.oppo.chatty", 1) == 0) {
            this.mDynProp = true;
        } else if (this.mReportTimes < 10) {
            String str;
            if (this.mReportTimes == 0) {
                this.mReportTimes = getRealNumber();
            }
            if (this.mReportTimes == 0) {
                this.mHandler.sendEmptyMessage(1);
            }
            this.mReportTimes++;
            int bRate = Integer.parseInt(bufferRate);
            int lRate = Integer.parseInt(logRate);
            long rTime = (long) Integer.parseInt(time);
            boolean boot = true;
            if (rTime < this.mBootTime || this.mBootTime == 0) {
                boot = false;
            }
            StringBuilder append = new StringBuilder().append("LOG#").append(this.mReportTimes).append("@").append(bufferRate).append("@").append(logRate).append("@").append(boot ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0").append("@");
            if (this.mScreenState) {
                str = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON;
            } else {
                str = "0";
            }
            append = append.append(str).append("@");
            if (isPanic()) {
                str = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON;
            } else {
                str = "0";
            }
            String name = append.append(str).toString();
            if (DEBUG) {
                Slog.d(TAG, "#" + this.mReportTimes + ", bRate=" + bRate + ", lRate=" + lRate + ", time=" + rTime + ", boot=" + this.mBootTime + ", name=" + name);
            }
            dumpLogFileInner(name, lRate * 15, rTime, true);
            saveRealNumber(this.mReportTimes);
            if (this.mReportTimes == 10) {
                scheduleSaveToFile(false);
            }
        }
    }

    private int getRealNumberByFile() {
        File dir = new File(FILE_PATH);
        int n = 0;
        if (dir.exists() && dir.isDirectory()) {
            for (File tmp : dir.listFiles()) {
                String fileName = tmp.getName();
                if (fileName != null && (fileName.startsWith("LOG#") || fileName.startsWith("LLOG#"))) {
                    n++;
                }
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "getRealNumberByFile, n = " + n);
        }
        return n;
    }

    private int getRealNumber() {
        return SystemProperties.getInt(LOG_NUM_PROPERTY, 0);
    }

    private void saveRealNumber(int num) {
        SystemProperties.set(LOG_NUM_PROPERTY, String.valueOf(num));
    }

    long readTimeStamp(String fileName) {
        IOException e;
        NumberFormatException ee;
        FileReader reader = null;
        BufferedReader in = null;
        try {
            FileReader reader2 = new FileReader(fileName);
            try {
                BufferedReader in2 = new BufferedReader(reader2);
                try {
                    long res = Long.parseLong(in2.readLine());
                    if (DEBUG) {
                        Slog.d(TAG, "TimeStamp=" + res);
                    }
                    try {
                        in2.close();
                        reader2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    } catch (RuntimeException ee2) {
                        ee2.printStackTrace();
                    }
                    return res;
                } catch (IOException e3) {
                    e2 = e3;
                    in = in2;
                    reader = reader2;
                    e2.printStackTrace();
                    try {
                        in.close();
                        reader.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    } catch (RuntimeException ee22) {
                        ee22.printStackTrace();
                    }
                    return 0;
                } catch (NumberFormatException e4) {
                    ee = e4;
                    in = in2;
                    reader = reader2;
                    try {
                        ee.printStackTrace();
                        try {
                            in.close();
                            reader.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        } catch (RuntimeException ee222) {
                            ee222.printStackTrace();
                        }
                        return 0;
                    } catch (Throwable th) {
                        try {
                            in.close();
                            reader.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        } catch (RuntimeException ee2222) {
                            ee2222.printStackTrace();
                        }
                        return 0;
                    }
                } catch (Throwable th2) {
                    in = in2;
                    reader = reader2;
                    in.close();
                    reader.close();
                    return 0;
                }
            } catch (IOException e5) {
                e2222 = e5;
                reader = reader2;
                e2222.printStackTrace();
                in.close();
                reader.close();
                return 0;
            } catch (NumberFormatException e6) {
                ee = e6;
                reader = reader2;
                ee.printStackTrace();
                in.close();
                reader.close();
                return 0;
            } catch (Throwable th3) {
                reader = reader2;
                in.close();
                reader.close();
                return 0;
            }
        } catch (IOException e7) {
            e2222 = e7;
            e2222.printStackTrace();
            in.close();
            reader.close();
            return 0;
        } catch (NumberFormatException e8) {
            ee = e8;
            ee.printStackTrace();
            in.close();
            reader.close();
            return 0;
        }
    }

    void writeTimeStamp() {
        if (DEBUG) {
            Slog.d(TAG, "writeTimeStamp", new Throwable());
        }
        try {
            FileWriter writer = new FileWriter(FILE_LOG_PATH);
            writer.write(System.currentTimeMillis() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean isCompressionNeeded() {
        boolean res = false;
        if (this.mReportTimes == 0) {
            return false;
        }
        if (new File(FILE_LOG_PATH).exists()) {
            long time = readTimeStamp(FILE_LOG_PATH);
            if (time <= 0) {
                writeTimeStamp();
                return false;
            }
            long now = System.currentTimeMillis();
            if (now - time > 86400000) {
                if (DEBUG) {
                    Slog.d(TAG, "need compress, now=" + now + ", timeStamp=" + time);
                }
                writeTimeStamp();
                res = true;
            }
            return res;
        }
        writeTimeStamp();
        return false;
    }

    void saveToFileIfNeeded() {
        if (DEBUG) {
            Slog.d(TAG, "saveToFileIfNeeded");
        }
        if (this.IMEI == null) {
            this.IMEI = ((TelephonyManager) this.mContext.getSystemService("phone")).getImei();
        }
        if (isCompressionNeeded()) {
            scheduleSaveToFile(true);
        }
    }

    void scheduleSaveToFile(boolean recount) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleSaveToFile, recount = " + recount);
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = 3;
        msg.arg1 = recount ? 1 : 0;
        msg.sendToTarget();
    }

    void deleteDirs(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (File deleteDirs : childFiles) {
                deleteDirs(deleteDirs);
            }
            file.delete();
        }
    }

    void doCompression() {
        if (DEBUG) {
            Slog.d(TAG, "doCompression");
        }
        if (getRealNumberByFile() > 0) {
            String imei = this.IMEI;
            if (imei == null || imei.length() < 15) {
                imei = "860000000000000";
            }
            String fileName = imei + "@CHATTY@" + System.currentTimeMillis() + ".zip";
            try {
                doZip("data/system/dropbox/chatty", FILE_ROOT_PATH + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File(FILE_ROOT_PATH + fileName);
            if (file.exists()) {
                file.renameTo(new File("data/system/dropbox/chatty/" + fileName));
            }
            deleteDirs(new File(FILE_PATH));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x004d A:{SYNTHETIC, Splitter: B:27:0x004d} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0059 A:{SYNTHETIC, Splitter: B:33:0x0059} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void doZip(String src, String dest) throws IOException {
        IOException ex;
        Throwable th;
        ZipOutputStream out = null;
        try {
            File outFile = new File(dest);
            File fileOrDirectory = new File(src);
            if (fileOrDirectory.exists()) {
                ZipOutputStream out2 = new ZipOutputStream(new FileOutputStream(outFile));
                try {
                    if (fileOrDirectory.isFile()) {
                        zipFileOrDirectory(out2, fileOrDirectory, IElsaManager.EMPTY_PACKAGE);
                    } else {
                        File[] entries = fileOrDirectory.listFiles();
                        for (File zipFileOrDirectory : entries) {
                            zipFileOrDirectory(out2, zipFileOrDirectory, IElsaManager.EMPTY_PACKAGE);
                        }
                    }
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException ex2) {
                            ex2.printStackTrace();
                        }
                    }
                    out = out2;
                } catch (IOException e) {
                    ex2 = e;
                    out = out2;
                    try {
                        ex2.printStackTrace();
                        if (out != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ex22) {
                                ex22.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            }
        } catch (IOException e2) {
            ex22 = e2;
            ex22.printStackTrace();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex222) {
                    ex222.printStackTrace();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x0097 A:{SYNTHETIC, Splitter: B:42:0x0097} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0050 A:{SYNTHETIC, Splitter: B:21:0x0050} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath) throws IOException {
        IOException ex;
        Throwable th;
        FileInputStream in = null;
        try {
            if (fileOrDirectory.isDirectory()) {
                File[] entries = fileOrDirectory.listFiles();
                for (File zipFileOrDirectory : entries) {
                    zipFileOrDirectory(out, zipFileOrDirectory, curPath + fileOrDirectory.getName() + "/");
                }
            } else {
                String name = fileOrDirectory.getName();
                if (name == null || !name.endsWith(".zip")) {
                    byte[] buffer = new byte[4096];
                    FileInputStream in2 = new FileInputStream(fileOrDirectory);
                    try {
                        out.putNextEntry(new ZipEntry(curPath + fileOrDirectory.getName()));
                        while (true) {
                            int bytes_read = in2.read(buffer);
                            if (bytes_read == -1) {
                                break;
                            }
                            out.write(buffer, 0, bytes_read);
                        }
                        out.closeEntry();
                        in = in2;
                    } catch (IOException e) {
                        ex = e;
                        in = in2;
                        try {
                            ex.printStackTrace();
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException ex2) {
                                    ex2.printStackTrace();
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (in != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        in = in2;
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ex22) {
                                ex22.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
                return;
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex222) {
                    ex222.printStackTrace();
                }
            }
        } catch (IOException e2) {
            ex222 = e2;
            ex222.printStackTrace();
            if (in != null) {
            }
        }
    }

    public void receiveSocketFromClient(String strMsg) {
        if (DEBUG) {
            Slog.d(TAG, "msg = " + strMsg);
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = 0;
        msg.obj = strMsg;
        msg.sendToTarget();
    }

    private void dumpInternal(PrintWriter pw) {
        pw.println("Chatty Manager State:");
        pw.println(" Boot Time = " + this.mBootTime);
        pw.println(" Report Times = " + this.mReportTimes);
        pw.println(" Last Report Time = " + readTimeStamp(FILE_LOG_PATH));
        pw.println(" Dynamic Log Opened = " + this.mDynLog);
        pw.println(" MAX_REPORT_TIMES = 10");
        pw.println(" UPDATE_INTERVAL = 86400000");
        pw.println(" ALARM_INTERVAL = 10800000");
    }
}
