package com.mediatek.net.connectivity;

import android.net.util.SocketUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

public class MtkNetPacketMonitor {
    private static final int MSG_CREATE_QUEUE = 11;
    private static final int MSG_DESTROY_QUEUE = 12;
    private static final int MSG_NOTIFY_PACKET = 21;
    private static final int MSG_SEND_VERDICT = 13;
    private static final int MSG_START_CONNECTION = 1;
    private static final String NAME_CONTROL_THREAD = "MtkNetPacketMonitor-control";
    private static final String NAME_NOTIFY_THREAD = "MtkNetPacketMonitor-notify";
    private static final String NAME_RECEIVE_THREAD = "MtkNetPacketMonitor-receive";
    private static final String TAG = "MtkNetPacketMonitor";
    private Handler mControlHandler;
    private HandlerThread mControlHandlerThread;
    private Handler mNotifyHandler;
    private HandlerThread mNotifyHandlerThread;
    private PacketCallback mPacketCallback;
    private Handler mReceiveHandler;
    private HandlerThread mReceiveHandlerThread;
    private SocketWrapper mSocketWrapper = new SocketWrapper();

    public MtkNetPacketMonitor() {
        initHandler();
        this.mReceiveHandler.sendEmptyMessage(1);
    }

    private void initHandler() {
        this.mControlHandlerThread = new HandlerThread(NAME_CONTROL_THREAD);
        this.mControlHandlerThread.start();
        this.mControlHandler = new Handler(this.mControlHandlerThread.getLooper()) {
            /* class com.mediatek.net.connectivity.MtkNetPacketMonitor.AnonymousClass1 */

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MtkNetPacketMonitor.MSG_CREATE_QUEUE /* 11 */:
                        MtkNetPacketMonitor.this.doCreateQueue(msg.arg1);
                        return;
                    case MtkNetPacketMonitor.MSG_DESTROY_QUEUE /* 12 */:
                        MtkNetPacketMonitor.this.doDestroyQueue(msg.arg1);
                        return;
                    case MtkNetPacketMonitor.MSG_SEND_VERDICT /* 13 */:
                        MtkNetPacketMonitor.this.doSendVerdict(msg.arg1, msg.arg2);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mReceiveHandlerThread = new HandlerThread(NAME_RECEIVE_THREAD);
        this.mReceiveHandlerThread.start();
        this.mReceiveHandler = new Handler(this.mReceiveHandlerThread.getLooper()) {
            /* class com.mediatek.net.connectivity.MtkNetPacketMonitor.AnonymousClass2 */

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    MtkNetPacketMonitor.this.doStartConnection();
                }
            }
        };
        this.mNotifyHandlerThread = new HandlerThread(NAME_NOTIFY_THREAD);
        this.mNotifyHandlerThread.start();
        this.mNotifyHandler = new Handler(this.mNotifyHandlerThread.getLooper()) {
            /* class com.mediatek.net.connectivity.MtkNetPacketMonitor.AnonymousClass3 */

            public void handleMessage(Message msg) {
                if (msg.what != MtkNetPacketMonitor.MSG_NOTIFY_PACKET) {
                    Log.i(MtkNetPacketMonitor.TAG, "Do nothing");
                } else {
                    MtkNetPacketMonitor.this.doNotifyPacket(msg.arg1);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doStartConnection() {
        this.mSocketWrapper.connectToKernel();
        this.mSocketWrapper.blockToReceive();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doCreateQueue(int queueNumber) {
        if (queueNumber <= 0) {
            Log.i(TAG, "doCreateQueue: invalid " + queueNumber);
            return;
        }
        Log.i(TAG, "send create queue message " + queueNumber);
        this.mSocketWrapper.sendMessage(MtkPacketMessage.getBindQueueMessage(queueNumber));
        Log.i(TAG, "send mode message " + queueNumber);
        this.mSocketWrapper.sendMessage(MtkPacketMessage.getSetModeMessage(queueNumber));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doDestroyQueue(int queueNumber) {
        if (queueNumber <= 0) {
            Log.i(TAG, "doDestroyQueue: invalid " + queueNumber);
            return;
        }
        Log.i(TAG, "doDestroyQueue " + queueNumber);
        this.mSocketWrapper.sendMessage(MtkPacketMessage.getUnbindQueueMessage(queueNumber));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doSendVerdict(int queueNumber, int packetId) {
        Log.i(TAG, "doSendVerdict queue " + queueNumber + ", packetId " + packetId);
        this.mSocketWrapper.sendMessage(MtkPacketMessage.getVerdictMessage(queueNumber, packetId, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doNotifyPacket(int uid) {
        PacketCallback packetCallback = this.mPacketCallback;
        if (packetCallback != null) {
            packetCallback.onPacketEvent(uid);
        } else {
            Log.e(TAG, "doNotifyPacket mPacketCallback is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processReceiveBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 1) {
            Log.e(TAG, "processReceiveBytes invalid bytes");
            return;
        }
        MtkPacketMessage packetMessage = new MtkPacketMessage();
        packetMessage.parseFromBytes(bytes);
        int queueNumber = packetMessage.getQueueNumber();
        int packetId = packetMessage.getPacketId();
        if (queueNumber <= 0 || packetId <= 0) {
            Log.i(TAG, "processReceiveBytes not notify");
            return;
        }
        Log.i(TAG, "processReceiveBytes " + queueNumber + ", packet " + packetId);
        Message notifyMessage = this.mNotifyHandler.obtainMessage(MSG_NOTIFY_PACKET);
        notifyMessage.arg1 = queueNumber;
        this.mNotifyHandler.sendMessage(notifyMessage);
        Message verdictMessage = this.mControlHandler.obtainMessage(MSG_SEND_VERDICT);
        verdictMessage.arg1 = queueNumber;
        verdictMessage.arg2 = packetId;
        this.mControlHandler.sendMessage(verdictMessage);
    }

    public void startMonitorProcessWithUid(int uid) {
        Log.i(TAG, "startMonitorProcessWithUid " + uid);
        Message message = this.mControlHandler.obtainMessage(MSG_CREATE_QUEUE);
        message.arg1 = uid;
        this.mControlHandler.sendMessage(message);
    }

    public void stopMonitorProcessWithUid(int uid) {
        Log.i(TAG, "stopMonitorProcessWithUid " + uid);
        Message message = this.mControlHandler.obtainMessage(MSG_DESTROY_QUEUE);
        message.arg1 = uid;
        this.mControlHandler.sendMessage(message);
    }

    public void setPacketCallback(PacketCallback callback) {
        Log.i(TAG, "setPacketCallback");
        this.mPacketCallback = callback;
    }

    /* access modifiers changed from: package-private */
    public static class PacketCallback {
        public void onPacketEvent(int uid) {
        }
    }

    /* access modifiers changed from: package-private */
    public class SocketWrapper {
        private static final long IO_TIMEOUT = 300;
        private static final int SOCKET_RECV_BUFSIZE = 4096;
        private FileDescriptor mSocket;

        public SocketWrapper() {
        }

        public void connectToKernel() {
            try {
                this.mSocket = Os.socket(OsConstants.AF_NETLINK, OsConstants.SOCK_DGRAM, OsConstants.NETLINK_NETFILTER);
                Os.setsockoptInt(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_RCVBUF, SOCKET_RECV_BUFSIZE);
                Os.connect(this.mSocket, SocketUtils.makeNetlinkSocketAddress(0, 0));
            } catch (ErrnoException | SocketException e) {
                e.printStackTrace();
            }
        }

        public void blockToReceive() {
            byte[] socketBuffer = new byte[SOCKET_RECV_BUFSIZE];
            InetSocketAddress inetSocketAddress = new InetSocketAddress();
            while (isSocketValid()) {
                try {
                    int rval = Os.recvfrom(this.mSocket, socketBuffer, 0, socketBuffer.length, 0, inetSocketAddress);
                    if (rval > 0) {
                        MtkNetPacketMonitor.this.processReceiveBytes(Arrays.copyOf(socketBuffer, rval));
                    } else {
                        Log.e(MtkNetPacketMonitor.TAG, "blockToReceive error " + rval);
                    }
                } catch (ErrnoException | SocketException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(byte[] bytes) {
            if (bytes == null || bytes.length < 1) {
                Log.e(MtkNetPacketMonitor.TAG, "sendMessage invalid bytes");
            } else {
                sendMessage(bytes, 0, bytes.length);
            }
        }

        private void sendMessage(byte[] bytes, int offset, int count) {
            if (!isSocketValid()) {
                Log.e(MtkNetPacketMonitor.TAG, "socket is not valid");
                return;
            }
            try {
                Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(IO_TIMEOUT));
                int ret = Os.write(this.mSocket, bytes, offset, count);
                if (ret <= 0) {
                    Log.e(MtkNetPacketMonitor.TAG, "sendMessage error " + ret);
                }
            } catch (ErrnoException | InterruptedIOException e) {
                e.printStackTrace();
            }
        }

        private boolean isSocketValid() {
            FileDescriptor fileDescriptor = this.mSocket;
            return fileDescriptor != null && fileDescriptor.valid();
        }
    }
}
