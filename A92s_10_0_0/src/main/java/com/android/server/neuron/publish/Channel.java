package com.android.server.neuron.publish;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.neuron.publish.Response;
import com.oppo.neuron.NeuronSystemManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public final class Channel {
    private static final int CONNECT_INTERNAL = 2000;
    private static final int MAX_COMMAND_BYTES = 3072;
    private static final int MAX_CONNECT_COUNT = 32;
    private static final int MAX_QUEUE_CAPACITY = 256;
    private static final int RE_INIT = 1;
    private static final String SOCK_NAME = "/dev/socket/neosocket";
    static final String TAG = "NeuronSystem";
    /* access modifiers changed from: private */
    public boolean mInited = false;
    /* access modifiers changed from: private */
    public InputStream mInputStream = null;
    /* access modifiers changed from: private */
    public ChannelEventListener mListener = null;
    private OutputStream mOutputStream = null;
    /* access modifiers changed from: private */
    public ArrayList<Request> mPendingRequestQueue = new ArrayList<>();
    private int mReConnectCount = 0;
    private RequestSender mSender = new RequestSender();
    private LocalSocket mSocket = new LocalSocket();
    private LocalSocketAddress mSocketAddress = new LocalSocketAddress(SOCK_NAME, LocalSocketAddress.Namespace.FILESYSTEM);
    /* access modifiers changed from: private */
    public boolean mStop = true;
    /* access modifiers changed from: private */
    public SparseArray<Request> mWaitForReponse = new SparseArray<>();
    private Handler myHandler = new Handler() {
        /* class com.android.server.neuron.publish.Channel.AnonymousClass1 */

        public void handleMessage(Message message) {
            if (message.what == 1) {
                boolean unused = Channel.this.mInited = false;
                if (!Channel.this.init()) {
                    Channel.this.triggerInitDelay();
                }
            }
        }
    };

    public interface ChannelEventListener {
        void onConnection(RequestSender requestSender);

        void onError(int i);

        void onIndication(Response.NativeIndication nativeIndication);

        void onResponse(Request request, Response.NativeResponse nativeResponse);
    }

    public void triggerInit() {
        if (SystemProperties.getBoolean("persist.vendor.neuron.channel", false) && !this.myHandler.hasMessages(1)) {
            this.myHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0046, code lost:
        return;
     */
    public synchronized void triggerInitDelay() {
        if (this.mReConnectCount <= 32) {
            this.mReConnectCount++;
            if (!this.myHandler.hasMessages(1)) {
                long interval = (long) (this.mReConnectCount * 2000);
                this.myHandler.sendEmptyMessageDelayed(1, interval);
                if (NeuronSystemManager.LOG_ON) {
                    Slog.d("NeuronSystem", "Channel will init again in " + (interval / 1000) + "s");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
        return true;
     */
    public synchronized boolean init() {
        if (this.mInited) {
            return true;
        }
        if (!initSocket()) {
            return false;
        }
        initRecvThread();
        Slog.i("NeuronSystem", "Channel init ok");
        this.mInited = true;
        if (this.mListener != null) {
            this.mListener.onConnection(this.mSender);
        }
    }

    public RequestSender getRequestSender() {
        return this.mSender;
    }

    public void setEventListener(ChannelEventListener listener) {
        this.mListener = listener;
    }

    private boolean initSocket() {
        try {
            if (this.mSocket.isConnected()) {
                try {
                    this.mSocket.close();
                } catch (IOException e) {
                }
                this.mSocket = null;
            }
            this.mSocket = new LocalSocket();
            this.mSocket.connect(this.mSocketAddress);
            Slog.d("NeuronSystem", "local socket connect ok");
            try {
                this.mInputStream = this.mSocket.getInputStream();
                this.mOutputStream = this.mSocket.getOutputStream();
                drainPendingRequestQueue();
                this.mReConnectCount = 0;
                return true;
            } catch (IOException e2) {
                this.mInputStream = null;
                this.mOutputStream = null;
                Slog.e("NeuronSystem", "socket get inputstream or outputstream err:" + e2);
                try {
                    this.mSocket.close();
                } catch (IOException e3) {
                }
                return false;
            }
        } catch (IOException e4) {
            try {
                this.mSocket.close();
            } catch (IOException e5) {
            }
            return false;
        }
    }

    private void initRecvThread() {
        this.mStop = false;
        new ReceiverThread().start();
    }

    /* access modifiers changed from: private */
    public boolean doSendRequest(Request req) {
        try {
            if (NeuronSystemManager.LOG_ON) {
                Slog.d("NeuronSystem", "doSendRequest timestamp:" + (System.nanoTime() / 1000));
            }
            if (this.mOutputStream == null) {
                return false;
            }
            this.mOutputStream.write(req.getBytes());
            this.mOutputStream.flush();
            return true;
        } catch (IOException e) {
            Slog.e("NeuronSystem", "write to local socket err: " + e);
            this.mOutputStream = null;
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0025, code lost:
        if (r1.hasNext() == false) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        r2 = r1.next();
        r4 = r7.mWaitForReponse;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r7.mWaitForReponse.put(r2.getSequenceNumber(), r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0039, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003e, code lost:
        if (doSendRequest(r2) != false) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0040, code lost:
        android.util.Slog.d("NeuronSystem", "drainPendingRequestQueue err, will drop this request");
        r4 = r7.mWaitForReponse;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0049, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r7.mWaitForReponse.remove(r2.getSequenceNumber());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0053, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005c, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001d, code lost:
        r1 = r2.iterator();
     */
    private boolean drainPendingRequestQueue() {
        synchronized (this.mPendingRequestQueue) {
            if (this.mPendingRequestQueue.size() == 0) {
                return true;
            }
            ArrayList<Request> tempRequestQueue = new ArrayList<>(this.mPendingRequestQueue);
            this.mPendingRequestQueue.clear();
        }
    }

    private class ReceiverThread extends Thread {
        private static final int INT_SIZE = 4;
        private int end;
        private byte[] mRecvBuf;

        private ReceiverThread() {
            this.mRecvBuf = new byte[Channel.MAX_COMMAND_BYTES];
            this.end = 0;
        }

        public void run() {
            while (!Channel.this.mStop) {
                try {
                    int len = Channel.this.mInputStream.read(this.mRecvBuf, this.end, 3072 - this.end);
                    if (len < 0) {
                        Slog.e("NeuronSystem", "ReceiverThread socket err");
                        this.end = 0;
                        boolean unused = Channel.this.mStop = true;
                        synchronized (Channel.this.mWaitForReponse) {
                            Channel.this.mWaitForReponse.clear();
                        }
                        Channel.this.triggerInitDelay();
                        return;
                    }
                    this.end += len;
                    int start = 0;
                    while (true) {
                        if (this.end - start > 4) {
                            Parcel parcel = Parcel.obtain();
                            parcel.unmarshall(this.mRecvBuf, start, this.end);
                            parcel.setDataPosition(0);
                            int responseLen = parcel.readInt();
                            if (responseLen + 4 < Channel.MAX_COMMAND_BYTES) {
                                int targetPos = start + responseLen + 4;
                                if (this.end >= targetPos) {
                                    Response resp = Response.makeReponse(parcel);
                                    start = targetPos;
                                    parcel.recycle();
                                    if (resp != null) {
                                        processResponse(resp);
                                        if (this.end - start <= 0) {
                                            break;
                                        }
                                    } else {
                                        Slog.d("NeuronSystem", "recv illegal bit data");
                                        this.end = start;
                                        break;
                                    }
                                } else {
                                    int copyLen = this.end - start;
                                    System.arraycopy(this.mRecvBuf, start, this.mRecvBuf, 0, copyLen);
                                    parcel.recycle();
                                    this.end = copyLen;
                                    if (NeuronSystemManager.LOG_ON) {
                                        Slog.d("NeuronSystem", "recv data less than packet len");
                                    }
                                }
                            } else {
                                Slog.e("NeuronSystem", "recv illegal bit data, data len larger than buffer size");
                                resetConnect();
                                parcel.recycle();
                                return;
                            }
                        } else {
                            int copyLen2 = this.end - start;
                            System.arraycopy(this.mRecvBuf, start, this.mRecvBuf, 0, copyLen2);
                            this.end = copyLen2;
                            if (NeuronSystemManager.LOG_ON) {
                                Slog.d("NeuronSystem", "recv data less than 4");
                            }
                        }
                    }
                    if (this.end == start) {
                        this.end = 0;
                    }
                } catch (IOException e) {
                    Slog.e("NeuronSystem", "socket read err: " + e);
                    boolean unused2 = Channel.this.mStop = true;
                    resetConnect();
                    return;
                }
            }
        }

        private void processResponse(Response resp) {
            if (Channel.this.mListener == null) {
                return;
            }
            if (resp.isIndication()) {
                Channel.this.mListener.onIndication(resp.getIndication());
                return;
            }
            Response.NativeResponse respData = resp.getResponseData();
            synchronized (Channel.this.mWaitForReponse) {
                int index = Channel.this.mWaitForReponse.indexOfKey(respData.serial);
                if (index >= 0) {
                    Channel.this.mListener.onResponse((Request) Channel.this.mWaitForReponse.get(respData.serial), respData);
                    Channel.this.mWaitForReponse.removeAt(index);
                    ((Request) Channel.this.mWaitForReponse.get(respData.serial)).release();
                } else {
                    Slog.e("NeuronSystem", "receive a unknown sequence number: " + respData.serial);
                }
            }
            if (respData.error != 1) {
                Channel.this.mListener.onError(respData.error);
            }
        }

        private void resetConnect() {
            synchronized (Channel.this.mWaitForReponse) {
                Channel.this.mWaitForReponse.clear();
            }
            Channel.this.triggerInitDelay();
        }
    }

    public class RequestSender {
        public RequestSender() {
        }

        public void sendRequest(Request req) {
            if (Channel.this.mInited) {
                try {
                    synchronized (Channel.this.mWaitForReponse) {
                        Channel.this.mWaitForReponse.put(req.getSequenceNumber(), req);
                    }
                    if (!Channel.this.doSendRequest(req)) {
                        synchronized (Channel.this.mWaitForReponse) {
                            Channel.this.mWaitForReponse.remove(req.getSequenceNumber());
                        }
                        synchronized (Channel.this.mPendingRequestQueue) {
                            if (Channel.this.mPendingRequestQueue.size() > 256) {
                                Channel.this.mPendingRequestQueue.clear();
                            }
                            Channel.this.mPendingRequestQueue.add(req);
                        }
                    }
                } catch (Exception e) {
                    Slog.e("NeuronSystem", "RequestSender add request to queue err: " + e);
                }
            }
        }
    }
}
