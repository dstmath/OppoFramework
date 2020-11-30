package com.android.server.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.StructNlMsgHdr;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.UserHandle;
import android.util.Slog;
import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import libcore.io.IoUtils;

public final class OppoNwPowerStateManager {
    private static final boolean DBG = false;
    private static final int INT = 4;
    private static final String INTENT_NW_POWER_BOOT_MONITOR = "oppo.intent.action.INTENT_NW_POWER_BOOT_MONITOR";
    private static final String INTENT_NW_POWER_BOOT_MONITOR_SCREEN_ON = "oppo.intent.action.INTENT_NW_POWER_BOOT_MONITOR_SCREEN_ON";
    private static final String INTENT_NW_POWER_STOP_MONITOR = "oppo.intent.action.INTENT_NW_POWER_STOP_MONITOR";
    private static final String INTENT_NW_POWER_STOP_MONITOR_UNSL = "oppo.intent.action.INTENT_NW_POWER_STOP_MONITOR_UNSL";
    private static final String INTENT_NW_POWER_UNSL_MONITOR = "oppo.intent.action.INTENT_NW_POWER_UNSL_MONITOR";
    private static final long IO_TIMEOUT = 300;
    private static final int KERNEL_UNSL_MSG_COUNT = 50;
    private static final String LOG_TAG = "OppoNwPowerStateManager";
    private static final int LONG = 8;
    private static final int MSG_NW_POWER_BOOT_MONITOR = 3;
    private static final int MSG_NW_POWER_BOOT_MONITOR_SCREEN_ON = 6;
    private static final int MSG_NW_POWER_STOP_MONITOR = 4;
    private static final int MSG_NW_POWER_STOP_MONITOR_UNSL = 5;
    private static final int MSG_SEND_PID_AND_LISTEN = 1;
    private static final int MSG_STOP_LISTENING = 2;
    private static final int NETLINK_OPPO_NWPOWERSTATE = 36;
    private static final short NW_POWER_ANDROID_PID = 17;
    private static final short NW_POWER_BOOT_MONITOR = 18;
    private static final short NW_POWER_BOOT_MONITOR_SCREEN_ON = 22;
    private static final short NW_POWER_STOP_MONITOR = 19;
    private static final short NW_POWER_STOP_MONITOR_UNSL = 20;
    private static final short NW_POWER_UNSL_MONITOR = 21;
    private static FileDescriptor mNlfd;
    private Handler mAsyncHandler;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private Thread mNetlinkThread = null;

    public OppoNwPowerStateManager(Context context) {
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
        this.mAsyncHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.power.OppoNwPowerStateManager.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    OppoNwPowerStateManager.this.sendPidAndListen();
                } else if (i == 3) {
                    OppoNwPowerStateManager.this.sendOn();
                } else if (i == 4) {
                    OppoNwPowerStateManager.this.sendOff();
                } else if (i != OppoNwPowerStateManager.MSG_NW_POWER_STOP_MONITOR_UNSL) {
                    if (i == OppoNwPowerStateManager.MSG_NW_POWER_BOOT_MONITOR_SCREEN_ON) {
                        OppoNwPowerStateManager.this.sendScreenOnMonitor();
                    }
                    Slog.e(OppoNwPowerStateManager.LOG_TAG, "AsyncHandler Unknow message: " + msg.what);
                } else {
                    OppoNwPowerStateManager.this.sendOffAndUnsl();
                }
            }
        };
        this.mAsyncHandler.sendEmptyMessageDelayed(1, 30000);
        initBroadcastRecriver();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        stopListening();
        super.finalize();
    }

    private void initBroadcastRecriver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_NW_POWER_BOOT_MONITOR);
        filter.addAction(INTENT_NW_POWER_STOP_MONITOR);
        filter.addAction(INTENT_NW_POWER_STOP_MONITOR_UNSL);
        filter.addAction(INTENT_NW_POWER_BOOT_MONITOR_SCREEN_ON);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.power.OppoNwPowerStateManager.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Slog.d(OppoNwPowerStateManager.LOG_TAG, "Received broadcast:" + action);
                if (action.equals(OppoNwPowerStateManager.INTENT_NW_POWER_BOOT_MONITOR)) {
                    OppoNwPowerStateManager.this.mAsyncHandler.sendEmptyMessage(3);
                } else if (action.equals(OppoNwPowerStateManager.INTENT_NW_POWER_STOP_MONITOR)) {
                    OppoNwPowerStateManager.this.mAsyncHandler.sendEmptyMessage(4);
                } else if (action.equals(OppoNwPowerStateManager.INTENT_NW_POWER_STOP_MONITOR_UNSL)) {
                    OppoNwPowerStateManager.this.mAsyncHandler.sendEmptyMessage(OppoNwPowerStateManager.MSG_NW_POWER_STOP_MONITOR_UNSL);
                } else if (action.equals(OppoNwPowerStateManager.INTENT_NW_POWER_BOOT_MONITOR_SCREEN_ON)) {
                    OppoNwPowerStateManager.this.mAsyncHandler.sendEmptyMessage(OppoNwPowerStateManager.MSG_NW_POWER_BOOT_MONITOR_SCREEN_ON);
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPidAndListen() {
        if (mNlfd == null && this.mNetlinkThread == null) {
            this.mNetlinkThread = new Thread() {
                /* class com.android.server.power.OppoNwPowerStateManager.AnonymousClass3 */

                public void run() {
                    try {
                        FileDescriptor unused = OppoNwPowerStateManager.mNlfd = NetlinkSocket.forProto((int) OppoNwPowerStateManager.NETLINK_OPPO_NWPOWERSTATE);
                        NetlinkSocket.connectToKernel(OppoNwPowerStateManager.mNlfd);
                        boolean result = OppoNwPowerStateManager.this.sendToKernel(OppoNwPowerStateManager.NW_POWER_ANDROID_PID, 0);
                        while (result) {
                            ByteBuffer bytes = NetlinkSocket.recvMessage(OppoNwPowerStateManager.mNlfd, 8192, 0);
                            StructNlMsgHdr nlmsghdr = StructNlMsgHdr.parse(bytes);
                            if (nlmsghdr != null) {
                                if (nlmsghdr.nlmsg_type == 21) {
                                    Slog.d(OppoNwPowerStateManager.LOG_TAG, "Received message: NW_POWER_UNSL_MONITOR");
                                    if (nlmsghdr.nlmsg_len < 416) {
                                        Slog.e(OppoNwPowerStateManager.LOG_TAG, "NW_POWER_UNSL_MONITOR, invalid length.");
                                    } else {
                                        long[] data = new long[OppoNwPowerStateManager.KERNEL_UNSL_MSG_COUNT];
                                        for (int i = 0; i < OppoNwPowerStateManager.KERNEL_UNSL_MSG_COUNT; i++) {
                                            try {
                                                bytes.position((i * OppoNwPowerStateManager.LONG) + 16);
                                                data[i] = bytes.getLong();
                                            } catch (Exception e) {
                                                Slog.e(OppoNwPowerStateManager.LOG_TAG, "Error:" + e.toString());
                                            }
                                        }
                                        Intent intent = new Intent(OppoNwPowerStateManager.INTENT_NW_POWER_UNSL_MONITOR);
                                        intent.putExtra("unsl", data);
                                        OppoNwPowerStateManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                                    }
                                }
                                Slog.e(OppoNwPowerStateManager.LOG_TAG, "Received unknow message: " + ((int) nlmsghdr.nlmsg_type));
                            }
                        }
                    } catch (Exception e2) {
                        Slog.e(OppoNwPowerStateManager.LOG_TAG, "Error:" + e2.toString());
                    }
                }
            };
            this.mNetlinkThread.start();
            Slog.d(LOG_TAG, "Start listening...");
            return;
        }
        Slog.e(LOG_TAG, "Already listening...");
    }

    private void stopListening() {
        if (mNlfd != null) {
            Slog.d(LOG_TAG, "Stop listening...");
            IoUtils.closeQuietly(mNlfd);
            mNlfd = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendOn() {
        sendToKernel(NW_POWER_BOOT_MONITOR, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendOff() {
        sendToKernel(NW_POWER_STOP_MONITOR, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendOffAndUnsl() {
        sendToKernel(NW_POWER_STOP_MONITOR_UNSL, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendScreenOnMonitor() {
        sendToKernel(NW_POWER_BOOT_MONITOR_SCREEN_ON, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendToKernel(short type, int data) {
        if (mNlfd != null) {
            try {
                StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
                nlMsgHdr.nlmsg_len = 20;
                nlMsgHdr.nlmsg_type = type;
                nlMsgHdr.nlmsg_flags = 1;
                nlMsgHdr.nlmsg_pid = Process.myPid();
                byte[] msg = new byte[nlMsgHdr.nlmsg_len];
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
                byteBuffer.order(ByteOrder.nativeOrder());
                nlMsgHdr.pack(byteBuffer);
                byteBuffer.putInt(data);
                if (msg.length == NetlinkSocket.sendMessage(mNlfd, msg, 0, msg.length, (long) IO_TIMEOUT)) {
                    return true;
                }
                return DBG;
            } catch (Exception e) {
                Slog.e(LOG_TAG, "Error:" + e.toString());
            }
        } else {
            Slog.e(LOG_TAG, "SendToKernel failed, mNlSock=null.");
            return DBG;
        }
    }
}
