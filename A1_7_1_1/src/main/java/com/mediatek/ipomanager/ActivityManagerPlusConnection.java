package com.mediatek.ipomanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Parcelable;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import com.android.internal.app.ShutdownManager;
import com.android.server.oppo.IElsaManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

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
public class ActivityManagerPlusConnection {
    private static final String TAG = "ActivityManagerPlusConnection";
    private static boolean sBooting;
    private static ActivityManagerPlusConnection sInstance;
    private final int BUFFER_SIZE;
    private Context mContext;
    private boolean mProcessing;
    private ServerSocketThread mServerSocketThread;

    private class InteractClientSocketThread extends Thread {
        static final String ACK = "ok";
        private LocalSocket interactClientSocket;
        private boolean mActionDone = false;
        private final Object mActionDoneSync = new Object();

        public InteractClientSocketThread(LocalSocket localSocket) {
            this.interactClientSocket = localSocket;
        }

        void actionDone() {
            synchronized (this.mActionDoneSync) {
                this.mActionDone = true;
                this.mActionDoneSync.notifyAll();
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:108:0x0276 A:{SYNTHETIC, Splitter: B:108:0x0276} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x027f A:{SYNTHETIC, Splitter: B:112:0x027f} */
        /* JADX WARNING: Removed duplicated region for block: B:108:0x0276 A:{SYNTHETIC, Splitter: B:108:0x0276} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x027f A:{SYNTHETIC, Splitter: B:112:0x027f} */
        /* JADX WARNING: Removed duplicated region for block: B:108:0x0276 A:{SYNTHETIC, Splitter: B:108:0x0276} */
        /* JADX WARNING: Removed duplicated region for block: B:112:0x027f A:{SYNTHETIC, Splitter: B:112:0x027f} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            IOException e;
            InputStream inputStream;
            Throwable th;
            OutputStream outputStream = null;
            String str = IElsaManager.EMPTY_PACKAGE;
            BroadcastReceiver anonymousClass1 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    InteractClientSocketThread.this.actionDone();
                }
            };
            InputStream inputStream2;
            OutputStream outputStream2;
            try {
                inputStream2 = this.interactClientSocket.getInputStream();
                try {
                    int i;
                    char[] cArr = new char[4096];
                    int read = new InputStreamReader(inputStream2).read(cArr);
                    if (read != -1) {
                        str = new String(cArr, 0, read);
                        Slog.i(ActivityManagerPlusConnection.TAG, "Receive String from client: " + str);
                        if (str.equals("ACTION_PREBOOT_IPO")) {
                            ActivityManagerPlusConnection.sBooting = true;
                            ShutdownManager.getInstance().preRestoreStates(ActivityManagerPlusConnection.this.mContext);
                            String str2 = "android.intent.action.ACTION_PREBOOT_IPO";
                            ActivityManagerPlusConnection.this.mContext.sendOrderedBroadcastAsUser(new Intent(str2), UserHandle.ALL, null, anonymousClass1, null, 0, null, null);
                            str = str2;
                        } else if (str.equals("ACTION_BOOT_IPO")) {
                            String str3 = "android.intent.action.ACTION_BOOT_IPO";
                            UserManager userManager = ActivityManagerPlus.getUserManager(ActivityManagerPlusConnection.this.mContext);
                            if (userManager != null) {
                                List users = userManager.getUsers();
                                int i2 = 0;
                                while (true) {
                                    int i3 = i2;
                                    if (i3 >= users.size()) {
                                        break;
                                    }
                                    Intent intent = new Intent(str3);
                                    intent.putExtra("android.intent.extra.user_handle", (Parcelable) users.get(i3));
                                    intent.addFlags(134217728);
                                    if (i3 != 0) {
                                        ActivityManagerPlusConnection.this.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(((UserInfo) users.get(i3)).id), null, null, null, 0, null, null);
                                    } else {
                                        ActivityManagerPlusConnection.this.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(((UserInfo) users.get(i3)).id), null, anonymousClass1, null, 0, null, null);
                                    }
                                    i2 = i3 + 1;
                                }
                                str = str3;
                            } else {
                                Slog.e(ActivityManagerPlusConnection.TAG, "ActivityManagerPlus not ready");
                                str = str3;
                            }
                        } else {
                            Slog.i(ActivityManagerPlusConnection.TAG, "unrecognized intent request: " + str);
                            if (inputStream2 != null) {
                                try {
                                    inputStream2.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            if (null != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            try {
                                this.interactClientSocket.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                            return;
                        }
                    }
                    synchronized (this.mActionDoneSync) {
                        i = 0;
                        while (!this.mActionDone) {
                            try {
                                this.mActionDoneSync.wait(200);
                                i++;
                            } catch (InterruptedException e3) {
                                Slog.i(ActivityManagerPlusConnection.TAG, "wait " + str + " but interrupted");
                                e3.printStackTrace();
                            }
                        }
                    }
                    Slog.i(ActivityManagerPlusConnection.TAG, str + " completed for " + ((((double) i) * 200.0d) / 1000.0d) + "s");
                    if (str.equals("android.intent.action.ACTION_BOOT_IPO")) {
                        ActivityManagerPlusConnection.sBooting = false;
                    }
                    outputStream2 = this.interactClientSocket.getOutputStream();
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream2);
                        outputStreamWriter.write(ACK, 0, ACK.length());
                        outputStreamWriter.flush();
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        if (outputStream2 != null) {
                            try {
                                outputStream2.close();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        try {
                            this.interactClientSocket.close();
                        } catch (IOException e222222) {
                            e222222.printStackTrace();
                        }
                    } catch (IOException e4) {
                        e222222 = e4;
                        inputStream = inputStream2;
                        try {
                            Slog.i(ActivityManagerPlusConnection.TAG, "transfer data error");
                            e222222.printStackTrace();
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e2222222) {
                                    e2222222.printStackTrace();
                                }
                            }
                            if (outputStream2 != null) {
                                try {
                                    outputStream2.close();
                                } catch (IOException e22222222) {
                                    e22222222.printStackTrace();
                                }
                            }
                            try {
                                this.interactClientSocket.close();
                            } catch (IOException e222222222) {
                                e222222222.printStackTrace();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            outputStream = outputStream2;
                            inputStream2 = inputStream;
                            if (inputStream2 != null) {
                            }
                            if (outputStream != null) {
                            }
                            try {
                                this.interactClientSocket.close();
                            } catch (IOException e5) {
                                e5.printStackTrace();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        outputStream = outputStream2;
                        if (inputStream2 != null) {
                        }
                        if (outputStream != null) {
                        }
                        this.interactClientSocket.close();
                        throw th;
                    }
                } catch (IOException e6) {
                    e222222222 = e6;
                    outputStream2 = outputStream;
                    inputStream = inputStream2;
                } catch (Throwable th4) {
                    th = th4;
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e52) {
                            e52.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e522) {
                            e522.printStackTrace();
                        }
                    }
                    this.interactClientSocket.close();
                    throw th;
                }
            } catch (IOException e7) {
                e222222222 = e7;
                inputStream = null;
                outputStream2 = outputStream;
            } catch (Throwable th5) {
                th = th5;
                inputStream2 = null;
                if (inputStream2 != null) {
                }
                if (outputStream != null) {
                }
                this.interactClientSocket.close();
                throw th;
            }
        }
    }

    private class ServerSocketThread extends Thread {
        private LocalServerSocket serverSocket;

        private ServerSocketThread() {
        }

        private void stopRun() {
            ActivityManagerPlusConnection.this.mProcessing = false;
        }

        public void run() {
            try {
                ActivityManagerPlusConnection.this.mProcessing = true;
                this.serverSocket = new LocalServerSocket("com.mediatek.ipomanager.ActivityManagerPlusConnection");
                while (ActivityManagerPlusConnection.this.mProcessing) {
                    Slog.i(ActivityManagerPlusConnection.TAG, "wait for new client coming!");
                    try {
                        LocalSocket accept = this.serverSocket.accept();
                        if (ActivityManagerPlusConnection.this.mProcessing) {
                            Slog.i(ActivityManagerPlusConnection.TAG, "new client coming!");
                            new InteractClientSocketThread(accept).start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ActivityManagerPlusConnection.this.mProcessing = false;
                    }
                }
                if (this.serverSocket != null) {
                    try {
                        this.serverSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                Slog.i(ActivityManagerPlusConnection.TAG, "ServerSocketThread exit");
            } catch (IOException e22) {
                e22.printStackTrace();
                ActivityManagerPlusConnection.this.mProcessing = false;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.ipomanager.ActivityManagerPlusConnection.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.ipomanager.ActivityManagerPlusConnection.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ipomanager.ActivityManagerPlusConnection.<clinit>():void");
    }

    private ActivityManagerPlusConnection(Context context) {
        this.BUFFER_SIZE = 4096;
        this.mProcessing = false;
        this.mContext = context;
        this.mServerSocketThread = new ServerSocketThread();
    }

    public static synchronized ActivityManagerPlusConnection getInstance(Context context) {
        ActivityManagerPlusConnection activityManagerPlusConnection;
        synchronized (ActivityManagerPlusConnection.class) {
            if (sInstance == null) {
                sInstance = new ActivityManagerPlusConnection(context);
            }
            activityManagerPlusConnection = sInstance;
        }
        return activityManagerPlusConnection;
    }

    public static boolean inBooting() {
        return sBooting;
    }

    public void startSocketServer() {
        Slog.i(TAG, "startSocketServer");
        if (this.mServerSocketThread != null) {
            if (!this.mServerSocketThread.isAlive()) {
                Slog.i(TAG, "SocketServer is not running, start it!");
                this.mProcessing = true;
                this.mServerSocketThread.start();
            } else if (!this.mProcessing) {
                this.mProcessing = true;
            }
        }
    }

    public void stopSocketServer() {
        Slog.i(TAG, "stopSocketServer");
        if (this.mServerSocketThread != null) {
            this.mServerSocketThread.stopRun();
        }
    }
}
