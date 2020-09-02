package com.mediatek.internal.telephony;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SubscriptionController;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MtkEmbmsAdaptor {
    private static final int MSG_ID_EVENT_IND = 2;
    private static final int MSG_ID_EVENT_REQUEST = 0;
    private static final int MSG_ID_EVENT_RESPONSE = 1;
    private static final String TAG = "MtkEmbmsAdaptor";
    private static MtkEmbmsAdaptor sInstance = null;
    /* access modifiers changed from: private */
    public MtkEmbmsAdaptEventHandler mEventHandler = null;
    /* access modifiers changed from: private */
    public SubscriptionController mSubscriptionController;

    private MtkEmbmsAdaptor(Context c, CommandsInterface[] ci) {
        Rlog.i(TAG, "construtor 2 parameter is called - start");
        this.mEventHandler = new MtkEmbmsAdaptEventHandler();
        this.mEventHandler.setRil(c, ci);
        this.mSubscriptionController = SubscriptionController.getInstance();
        new Thread() {
            /* class com.mediatek.internal.telephony.MtkEmbmsAdaptor.AnonymousClass1 */

            public void run() {
                new ServerTask().listenConnection(MtkEmbmsAdaptor.this.mEventHandler);
            }
        }.start();
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < numPhones; i++) {
            ((MtkRIL) ci[i]).setAtInfoNotification(this.mEventHandler, 2, Integer.valueOf(i));
        }
        Rlog.i(TAG, "construtor is called - end");
    }

    public static MtkEmbmsAdaptor getDefault(Context context, CommandsInterface[] ci) {
        Rlog.d(TAG, "getDefault()");
        if (sInstance == null) {
            sInstance = new MtkEmbmsAdaptor(context, ci);
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    public String messageToString(Message msg) {
        int i = msg.what;
        if (i == 0) {
            return "MSG_ID_EVENT_REQUEST";
        }
        if (i == 1) {
            return "MSG_ID_EVENT_RESPONSE";
        }
        if (i != 2) {
            return "UNKNOWN";
        }
        return "MSG_ID_EVENT_IND";
    }

    public class ServerTask {
        public static final String HOST_NAME = "/dev/socket/embmsd";

        public ServerTask() {
        }

        public void listenConnection(MtkEmbmsAdaptEventHandler eventHandler) {
            Rlog.i(MtkEmbmsAdaptor.TAG, "listenConnection() - start");
            LocalServerSocket serverSocket = null;
            ExecutorService threadExecutor = Executors.newCachedThreadPool();
            try {
                while (true) {
                    LocalSocket socket = new LocalServerSocket(HOST_NAME).accept();
                    Rlog.d(MtkEmbmsAdaptor.TAG, "There is a client is accepted: " + socket.toString());
                    threadExecutor.execute(new ConnectionHandler(socket, eventHandler));
                }
            } catch (IOException e) {
                Rlog.e(MtkEmbmsAdaptor.TAG, "listenConnection catch IOException");
                e.printStackTrace();
                Rlog.d(MtkEmbmsAdaptor.TAG, "listenConnection finally!!");
                if (threadExecutor != null) {
                    threadExecutor.shutdown();
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Exception e2) {
                Rlog.e(MtkEmbmsAdaptor.TAG, "listenConnection catch Exception");
                e2.printStackTrace();
                Rlog.d(MtkEmbmsAdaptor.TAG, "listenConnection finally!!");
                if (threadExecutor != null) {
                    threadExecutor.shutdown();
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                Rlog.d(MtkEmbmsAdaptor.TAG, "listenConnection finally!!");
                if (threadExecutor != null) {
                    threadExecutor.shutdown();
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                throw th;
            }
            Rlog.i(MtkEmbmsAdaptor.TAG, "listenConnection() - end");
        }
    }

    public class ConnectionHandler implements Runnable {
        private MtkEmbmsAdaptEventHandler mEventHandler;
        private LocalSocket mSocket;

        public ConnectionHandler(LocalSocket clientSocket, MtkEmbmsAdaptEventHandler eventHandler) {
            this.mSocket = clientSocket;
            this.mEventHandler = eventHandler;
        }

        public void run() {
            Rlog.i(MtkEmbmsAdaptor.TAG, "New connection: " + this.mSocket.toString());
            try {
                MtkEmbmsAdaptIoThread ioThread = new MtkEmbmsAdaptIoThread(ServerTask.HOST_NAME, this.mSocket.getInputStream(), this.mSocket.getOutputStream(), this.mEventHandler);
                this.mEventHandler.setDataStream(ioThread);
                ioThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class MtkEmbmsAdaptIoThread extends Thread {
        private static final int MAX_DATA_LENGTH = 4096;
        private MtkEmbmsAdaptEventHandler mEventHandler = null;
        private InputStream mInput = null;
        private boolean mIsContinue = true;
        private String mName = "";
        private OutputStream mOutput = null;
        private final Object mOutputLock = new Object();
        private byte[] readBuffer = null;

        public MtkEmbmsAdaptIoThread(String name, InputStream inputStream, OutputStream outputStream, MtkEmbmsAdaptEventHandler eventHandler) {
            this.mName = name;
            this.mInput = inputStream;
            this.mOutput = outputStream;
            this.mEventHandler = eventHandler;
            Rlog.i(MtkEmbmsAdaptor.TAG, "MtkEmbmsAdaptIoThread constructor is called.");
            this.readBuffer = new byte[4096];
        }

        public void terminate() {
            Rlog.i(MtkEmbmsAdaptor.TAG, "MtkEmbmsAdaptIoThread terminate.");
            this.mIsContinue = false;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0030, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0031, code lost:
            android.telephony.Rlog.e(com.mediatek.internal.telephony.MtkEmbmsAdaptor.TAG, "MtkEmbmsAdaptIoThread Exception.");
            r1.printStackTrace();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x003a, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x003b, code lost:
            android.telephony.Rlog.e(com.mediatek.internal.telephony.MtkEmbmsAdaptor.TAG, "MtkEmbmsAdaptIoThread IOException.");
            r1.printStackTrace();
            android.telephony.Rlog.e(com.mediatek.internal.telephony.MtkEmbmsAdaptor.TAG, "Socket disconnected.");
            terminate();
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x003a A[ExcHandler: IOException (r1v2 'e' java.io.IOException A[CUSTOM_DECLARE]), Splitter:B:3:0x000b] */
        public void run() {
            Rlog.i(MtkEmbmsAdaptor.TAG, "MtkEmbmsAdaptIoThread running.");
            while (this.mIsContinue) {
                try {
                    int count = this.mInput.read(this.readBuffer, 0, 4096);
                    if (count < 0) {
                        Rlog.e(MtkEmbmsAdaptor.TAG, "readEvent(), fail to read and throw exception");
                        return;
                    } else if (count > 0) {
                        handleInput(new String(this.readBuffer, 0, count));
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                } catch (IOException e) {
                }
            }
        }

        /* access modifiers changed from: protected */
        public void handleInput(String input) {
            Rlog.d(MtkEmbmsAdaptor.TAG, "process input: RCV <-(" + input + "),length:" + input.length());
            MtkEmbmsAdaptEventHandler mtkEmbmsAdaptEventHandler = this.mEventHandler;
            mtkEmbmsAdaptEventHandler.sendMessage(mtkEmbmsAdaptEventHandler.obtainMessage(0, input.trim()));
        }

        public void sendCommand(String rawCmd) {
            Rlog.d(MtkEmbmsAdaptor.TAG, "SND -> (" + rawCmd + ")");
            synchronized (this.mOutputLock) {
                if (this.mOutput == null) {
                    Rlog.e(MtkEmbmsAdaptor.TAG, "missing SIM output stream");
                } else {
                    try {
                        this.mOutput.write(rawCmd.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public class MtkEmbmsAdaptEventHandler extends Handler {
        private MtkEmbmsAdaptIoThread mAdaptorIoThread = null;
        private CommandsInterface[] mCis;
        private Context mContext;

        public MtkEmbmsAdaptEventHandler() {
        }

        public void handleMessage(Message msg) {
            Rlog.d(MtkEmbmsAdaptor.TAG, "handleMessage: " + MtkEmbmsAdaptor.this.messageToString(msg) + " = " + msg);
            int i = msg.what;
            if (i == 0) {
                String data = (String) msg.obj;
                Rlog.i(MtkEmbmsAdaptor.TAG, "MSG_ID_EVENT_REQUEST data: " + data);
                int subId = MtkEmbmsAdaptor.this.mSubscriptionController.getDefaultDataSubId();
                int slotId = -1;
                if (subId == -1) {
                    Rlog.e(MtkEmbmsAdaptor.TAG, "getDefaultDataSubId fail: " + subId);
                } else {
                    slotId = MtkEmbmsAdaptor.this.mSubscriptionController.getSlotIndex(subId);
                }
                if (!SubscriptionManager.isValidSlotIndex(slotId)) {
                    Rlog.e(MtkEmbmsAdaptor.TAG, "inValidSlotIndex:" + slotId);
                    sendFailureCmd();
                    return;
                }
                this.mCis[slotId].sendEmbmsAtCommand(data, obtainMessage(1, Integer.valueOf(slotId)));
            } else if (i == 1) {
                AsyncResult ar = (AsyncResult) msg.obj;
                ((Integer) ar.userObj).intValue();
                String data2 = (String) ar.result;
                Rlog.i(MtkEmbmsAdaptor.TAG, "MSG_ID_EVENT_RESPONSE data: " + data2);
                if ((ar.exception instanceof CommandException) && ar.exception.getCommandError() == CommandException.Error.RADIO_NOT_AVAILABLE) {
                    Rlog.e(MtkEmbmsAdaptor.TAG, "MSG_ID_EVENT_RESPONSE exception: " + ar.exception.getCommandError());
                    sendFailureCmd();
                } else if (data2 != null) {
                    sendCommand(data2);
                } else {
                    sendFailureCmd();
                }
            } else if (i == 2) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                ((Integer) ar2.userObj).intValue();
                String data3 = (String) ar2.result;
                Rlog.i(MtkEmbmsAdaptor.TAG, "MSG_ID_EVENT_IND data: " + data3);
                sendCommand(data3);
            }
        }

        /* access modifiers changed from: private */
        public void setDataStream(MtkEmbmsAdaptIoThread adpatorIo) {
            this.mAdaptorIoThread = adpatorIo;
            Rlog.d(MtkEmbmsAdaptor.TAG, "MtkEmbmsAdaptEventHandler setDataStream done.");
        }

        /* access modifiers changed from: private */
        public void setRil(Context context, CommandsInterface[] ci) {
            this.mContext = context;
            this.mCis = ci;
            Rlog.d(MtkEmbmsAdaptor.TAG, "MtkEmbmsAdaptEventHandler setRil done.");
        }

        public void sendCommand(String rawCmd) {
            MtkEmbmsAdaptIoThread mtkEmbmsAdaptIoThread = this.mAdaptorIoThread;
            if (mtkEmbmsAdaptIoThread != null) {
                mtkEmbmsAdaptIoThread.sendCommand(rawCmd);
            } else {
                Rlog.e(MtkEmbmsAdaptor.TAG, "sendCommand fail!! mAdaptorIoThread is null!");
            }
        }

        public void sendFailureCmd() {
            sendCommand(String.format("ERROR\n", new Object[0]));
        }
    }
}
