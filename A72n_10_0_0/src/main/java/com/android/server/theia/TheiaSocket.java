package com.android.server.theia;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class TheiaSocket {
    private static final String TAG = "TheiaSocketClient";
    private static volatile TheiaSocket theiaSocket;
    private final String SOCKET_NAME = "theia_socket";
    private LocalSocketAddress address;
    private LocalSocket client;
    private ConnectSocketThread connectSocketThread = new ConnectSocketThread();
    private int connectTime = 1;
    private BufferedReader in;
    private boolean isConnected = false;
    HandlerThread mSendThread;
    TheiaSender mSender;
    private PrintWriter out;

    static /* synthetic */ int access$208(TheiaSocket x0) {
        int i = x0.connectTime;
        x0.connectTime = i + 1;
        return i;
    }

    public TheiaSender getSender() {
        return this.mSender;
    }

    public static TheiaSocket getInstance() {
        if (theiaSocket == null) {
            synchronized (TheiaSocket.class) {
                if (theiaSocket == null) {
                    theiaSocket = new TheiaSocket();
                }
            }
        }
        return theiaSocket;
    }

    public TheiaSocket() {
        Log.d(TAG, "TheiaSocket ready to Start");
        TheiaSocketStart();
    }

    private void TheiaSocketStart() {
        this.client = new LocalSocket();
        this.address = new LocalSocketAddress("theia_socket", LocalSocketAddress.Namespace.RESERVED);
        this.mSendThread = new HandlerThread("TheiaSender");
        this.mSendThread.start();
        this.mSender = new TheiaSender(this.mSendThread.getLooper());
        this.connectSocketThread.start();
    }

    public void sendMessage(String content) {
        Message message = Message.obtain();
        message.what = 1;
        message.obj = content;
        this.mSender.sendMessage(message);
    }

    /* access modifiers changed from: private */
    public class ConnectSocketThread extends Thread {
        private ConnectSocketThread() {
        }

        public void run() {
            while (!TheiaSocket.this.isConnected && TheiaSocket.this.connectTime <= 10) {
                try {
                    sleep(1000);
                    Log.d(TheiaSocket.TAG, "Try to connect socket;ConnectTime:" + TheiaSocket.this.connectTime);
                    TheiaSocket.this.client.connect(TheiaSocket.this.address);
                    TheiaSocket.this.out = new PrintWriter(TheiaSocket.this.client.getOutputStream());
                    TheiaSocket.this.in = new BufferedReader(new InputStreamReader(TheiaSocket.this.client.getInputStream()));
                    TheiaSocket.this.isConnected = true;
                    TheiaSocket.this.client.setSoTimeout(1200);
                    Log.d(TheiaSocket.TAG, "TheiaSocket Connect Success");
                } catch (Exception e) {
                    TheiaSocket.access$208(TheiaSocket.this);
                    Log.d(TheiaSocket.TAG, "Connect fail; Reason: " + e.toString());
                }
            }
        }
    }

    public void destroy() {
        try {
            if (this.in != null) {
                this.in.close();
            }
            if (this.out != null) {
                this.out.close();
            }
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public class TheiaSender extends Handler implements Runnable {
        public TheiaSender(Looper looper) {
            super(looper);
            Log.d(TheiaSocket.TAG, "entering TheiaSender Constructor");
        }

        public void run() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String content = msg.obj.toString();
                Log.d(TheiaSocket.TAG, "TheiaSend receive content : " + content);
                if (!TheiaSocket.this.connectSocketThread.isAlive() && !TheiaSocket.this.isConnected) {
                    Log.d(TheiaSocket.TAG, "Socket unConnected, restart connectSocketThread");
                    TheiaSocket theiaSocket = TheiaSocket.this;
                    theiaSocket.connectSocketThread = new ConnectSocketThread();
                    TheiaSocket.this.connectSocketThread.start();
                }
                if (TheiaSocket.this.out != null) {
                    TheiaSocket.this.out.println(content);
                    TheiaSocket.this.out.flush();
                    Log.d(TheiaSocket.TAG, "send message success");
                }
            }
        }
    }
}
