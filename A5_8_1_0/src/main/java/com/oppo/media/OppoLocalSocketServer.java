package com.oppo.media;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

public class OppoLocalSocketServer extends Thread {
    private static final int MSG_INFO_MAX_SIZE = 1024;
    private static final int MSG_INFO_MIN_SIZE = 3;
    private static final String SOCKET_NAME = "Multimedia_Socket_Address";
    private static final String TAG = "LocalSocketServer";
    private final Context mContext;
    private boolean mKeepRunning = true;
    private LocalServerSocket mServerSocket;

    class Task implements Runnable {
        private LocalSocket mClientSocket = null;

        public Task(LocalSocket connect) {
            this.mClientSocket = connect;
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x0083 A:{Catch:{ IOException -> 0x008e }} */
        /* JADX WARNING: Removed duplicated region for block: B:44:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x008a A:{Catch:{ IOException -> 0x008e }} */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0098 A:{Catch:{ IOException -> 0x00a3 }} */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x009f A:{Catch:{ IOException -> 0x00a3 }} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            IOException e;
            Throwable th;
            StringBuilder recvStrBuilder = new StringBuilder();
            InputStream inputStream = null;
            try {
                inputStream = this.mClientSocket.getInputStream();
                byte[] bytes = new byte[1024];
                int readBytes = inputStream.read(bytes, 0, 1024);
                Log.d(OppoLocalSocketServer.TAG, "readBytes = " + readBytes);
                if (readBytes >= 3) {
                    bytes[readBytes] = (byte) 0;
                    String info = new String(bytes, 0, readBytes);
                    String str;
                    try {
                        Log.d(OppoLocalSocketServer.TAG, "ClientSocket read info = " + info);
                        OppoMultimediaManager.getInstance(OppoLocalSocketServer.this.mContext).sendMessage(info);
                        str = info;
                    } catch (IOException e2) {
                        e = e2;
                        str = info;
                        try {
                            e.printStackTrace();
                            try {
                                if (this.mClientSocket != null) {
                                }
                                if (inputStream == null) {
                                }
                            } catch (IOException e3) {
                                e3.printStackTrace();
                                return;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                if (this.mClientSocket != null) {
                                    this.mClientSocket.close();
                                }
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (this.mClientSocket != null) {
                        }
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                }
                try {
                    if (this.mClientSocket != null) {
                        this.mClientSocket.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            } catch (IOException e4) {
                e322 = e4;
                e322.printStackTrace();
                if (this.mClientSocket != null) {
                    this.mClientSocket.close();
                }
                if (inputStream == null) {
                    inputStream.close();
                }
            }
        }
    }

    public OppoLocalSocketServer(Context context) {
        this.mContext = context;
    }

    private void stopRun() {
        this.mKeepRunning = false;
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        try {
            Log.d(TAG, "LocalServerSocket SOCKET_NAME = Multimedia_Socket_Address");
            this.mServerSocket = new LocalServerSocket(SOCKET_NAME);
            while (this.mKeepRunning && this.mServerSocket != null) {
                Log.d(TAG, "wait for new client coming !");
                LocalSocket clientSocket = this.mServerSocket.accept();
                if (this.mKeepRunning) {
                    Log.d(TAG, "new client coming !");
                    ThreadPool.getInstance().request(new Task(clientSocket));
                }
            }
            try {
                if (this.mServerSocket != null) {
                    this.mServerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            try {
                if (this.mServerSocket != null) {
                    this.mServerSocket.close();
                }
            } catch (IOException e22) {
                e22.printStackTrace();
            }
        } catch (Throwable th) {
            try {
                if (this.mServerSocket != null) {
                    this.mServerSocket.close();
                }
            } catch (IOException e222) {
                e222.printStackTrace();
            }
            throw th;
        }
    }
}
