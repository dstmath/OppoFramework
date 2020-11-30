package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.netlink.NetlinkSocket;
import android.net.netlink.StructNlMsgHdr;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.server.pm.PackageManagerService;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.oppo.luckymoney.LMManager;
import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import libcore.io.IoUtils;

/* access modifiers changed from: package-private */
public class OppoNfHooksHelper {
    private static final long IO_TIMEOUT = 300;
    private static final int MSG_LM_BLOCK = 102;
    private static final int MSG_LM_DETECTED = 101;
    private static final int MSG_SEND_WECHAT_PARAMS = 103;
    private static final int MSG_SHOW_TOAST = 100;
    private static final int NETLINK_OPPO_NF_HOOKS = 32;
    private static final short NF_HOOKS_ANDROID_PID = 17;
    private static final short NF_HOOKS_LM_DETECTED = 19;
    private static final short NF_HOOKS_MAX = 20;
    private static final short NF_HOOKS_WECHAT_PARAM = 18;
    private static final int SIZE_OF_INT = 4;
    private static final String TAG = "OppoNfHooksHelper";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    private static FileDescriptor mNlfd;
    private Handler mAsyncHandler;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private Handler mHandler;
    private int mLuckyMoneyCount = 0;
    private PackageManagerService mPMS;
    private int mUserId = 0;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;

    static /* synthetic */ int access$208(OppoNfHooksHelper x0) {
        int i = x0.mLuckyMoneyCount;
        x0.mLuckyMoneyCount = i + 1;
        return i;
    }

    public OppoNfHooksHelper(Context context) {
        this.mContext = context;
        this.mPMS = ServiceManager.getService("package");
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mHandler = new Handler() {
            /* class com.android.server.wifi.OppoNfHooksHelper.AnonymousClass1 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 100) {
                    Log.d(OppoNfHooksHelper.TAG, "MSG_SHOW_TOAST:" + msg.obj);
                    OppoNfHooksHelper.this.showMyToast((String) msg.obj, msg.arg1);
                } else if (i != 102) {
                    Log.d(OppoNfHooksHelper.TAG, "Unknow message:" + msg.what);
                } else {
                    Log.d(OppoNfHooksHelper.TAG, "MSG_LM_BLOCK finished.");
                }
            }
        };
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mAsyncHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.OppoNfHooksHelper.AnonymousClass2 */

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i != 101) {
                    if (i != 103) {
                        Log.d(OppoNfHooksHelper.TAG, "Unknow message:" + msg.what);
                        return;
                    }
                    Log.d(OppoNfHooksHelper.TAG, "MSG_SEND_WECHAT_PARAMS");
                    boolean result = OppoNfHooksHelper.this.sendWechatParams();
                    Log.d(OppoNfHooksHelper.TAG, "After sendWechatParams:result=" + result);
                } else if (!OppoNfHooksHelper.this.mHandler.hasMessages(102)) {
                    Log.d(OppoNfHooksHelper.TAG, "MSG_LM_DETECTED enableBoost...");
                    LMManager.getLMManager().enableBoost(0, 2015);
                    OppoNfHooksHelper.this.mHandler.sendMessage(OppoNfHooksHelper.this.mHandler.obtainMessage(100, ActiveModeWardenForDeferRequest.TIMEOUT, 0, "Detected LM"));
                    OppoNfHooksHelper.this.mHandler.sendEmptyMessageDelayed(102, RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
                    OppoNfHooksHelper.access$208(OppoNfHooksHelper.this);
                } else {
                    Log.d(OppoNfHooksHelper.TAG, "MSG_LM_DETECTED too frequent, ignore!");
                }
            }
        };
        initBroadcastRecriver();
    }

    private void initBroadcastRecriver() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoNfHooksHelper.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(OppoNfHooksHelper.TAG, "Received broadcast:" + action);
                if (action.equals("android.intent.action.PACKAGE_ADDED") || action.equals("android.intent.action.PACKAGE_REPLACED") || action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    if (OppoNfHooksHelper.WECHAT_PACKAGE_NAME.equals(intent.getData().getSchemeSpecificPart())) {
                        Log.d(OppoNfHooksHelper.TAG, "WeChat package changed:" + action);
                        OppoNfHooksHelper.this.mAsyncHandler.sendEmptyMessage(103);
                    }
                } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                    OppoNfHooksHelper.this.mUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    Log.d(OppoNfHooksHelper.TAG, "userHandleId=" + OppoNfHooksHelper.this.mUserId);
                    OppoNfHooksHelper.this.mAsyncHandler.sendEmptyMessage(103);
                }
            }
        };
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, packageFilter, null, null);
    }

    /* access modifiers changed from: private */
    public class WechatParamInfo {
        public static final int ALLIGNED_LEN = 36;
        public static final int MAX_FIXED_VALUE_LEN = 20;
        public byte[] fixed_value = new byte[20];
        public int len;
        public int maxTotLen;
        public int minTotLen;
        public int offset;

        public WechatParamInfo() {
        }

        public boolean setParams(String[] params) {
            if (params == null || params.length != 5) {
                Log.e(OppoNfHooksHelper.TAG, "setParams invalid params:" + Arrays.toString(params));
                return false;
            }
            try {
                this.maxTotLen = Integer.parseInt(params[0]);
                this.minTotLen = Integer.parseInt(params[1]);
                this.offset = Integer.parseInt(params[2]);
                this.len = Integer.parseInt(params[3]);
                String fixedValue = params[4];
                if (!TextUtils.isEmpty(fixedValue)) {
                    if (fixedValue.length() <= 20) {
                        if (fixedValue.length() != this.len * 2) {
                            Log.d(OppoNfHooksHelper.TAG, "setParams invalid fixed value length, len=" + this.len + " fixedValue.length()=" + fixedValue.length());
                            return false;
                        }
                        for (int i = 0; i + 1 < fixedValue.length(); i += 2) {
                            this.fixed_value[i / 2] = (byte) Integer.parseInt(fixedValue.substring(i, i + 2), 16);
                        }
                        Log.d(OppoNfHooksHelper.TAG, "setParams succeeded:" + toString());
                        return true;
                    }
                }
                Log.d(OppoNfHooksHelper.TAG, "setParams invalid fixed value:" + fixedValue);
                return false;
            } catch (Exception e) {
                Log.e(OppoNfHooksHelper.TAG, "setParams failed...", e);
                return false;
            }
        }

        public void pack(ByteBuffer byteBuffer) {
            byteBuffer.putInt(this.maxTotLen);
            byteBuffer.putInt(this.minTotLen);
            byteBuffer.putInt(this.offset);
            byteBuffer.putInt(this.len);
            for (int i = 0; i < 20; i++) {
                byteBuffer.put(this.fixed_value[i]);
            }
        }

        public String toString() {
            return "maxTotLen:" + this.maxTotLen + ", minTotLen:" + this.minTotLen + ", offset:" + this.offset + ", len:" + this.len + ", fixed_value:" + OppoNfHooksHelper.bytesToHex(this.fixed_value);
        }
    }

    public void sendPidAndListen() {
        if (mNlfd != null) {
            Log.w(TAG, "Already listening!!");
        } else {
            new Thread() {
                /* class com.android.server.wifi.OppoNfHooksHelper.AnonymousClass4 */

                public void run() {
                    Log.d(OppoNfHooksHelper.TAG, "sendPidAndListen tid=" + Thread.currentThread().getId());
                    try {
                        FileDescriptor unused = OppoNfHooksHelper.mNlfd = NetlinkSocket.forProto(32);
                        NetlinkSocket.connectToKernel(OppoNfHooksHelper.mNlfd);
                        boolean result = OppoNfHooksHelper.this.sendToKernel((OppoNfHooksHelper) 17, (short) 0);
                        Log.d(OppoNfHooksHelper.TAG, "After sending pid:result=" + result);
                        boolean sendWechatParams = OppoNfHooksHelper.this.sendWechatParams();
                        Log.d(OppoNfHooksHelper.TAG, "After sendWechatParams:result=" + sendWechatParams);
                        while (true) {
                            StructNlMsgHdr nlmsghdr = StructNlMsgHdr.parse(NetlinkSocket.recvMessage(OppoNfHooksHelper.mNlfd, 8192, 0));
                            if (nlmsghdr != null) {
                                if (nlmsghdr.nlmsg_type != 19) {
                                    Log.w(OppoNfHooksHelper.TAG, "Received unknow message:type=" + ((int) nlmsghdr.nlmsg_type));
                                } else {
                                    Log.d(OppoNfHooksHelper.TAG, "Received message:NF_HOOKS_LM_DETECTED");
                                    OppoNfHooksHelper.this.mAsyncHandler.sendEmptyMessage(101);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(OppoNfHooksHelper.TAG, "Exception when sendPidAndListen:tid=" + Thread.currentThread().getId(), new Throwable());
                    }
                }
            }.start();
        }
    }

    public void stopListening() {
        if (mNlfd != null) {
            Log.d(TAG, "stopListening");
            IoUtils.closeQuietly(mNlfd);
            mNlfd = null;
        }
    }

    private boolean sendToKernel(short type, int[] data) {
        if (mNlfd != null) {
            try {
                StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
                nlMsgHdr.nlmsg_len = 16 + ((data == null ? 0 : data.length) * 4);
                nlMsgHdr.nlmsg_type = type;
                nlMsgHdr.nlmsg_flags = 1;
                nlMsgHdr.nlmsg_pid = Process.myPid();
                byte[] msg = new byte[nlMsgHdr.nlmsg_len];
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
                byteBuffer.order(ByteOrder.nativeOrder());
                nlMsgHdr.pack(byteBuffer);
                if (data != null) {
                    for (int i : data) {
                        byteBuffer.putInt(i);
                    }
                }
                if (msg.length == NetlinkSocket.sendMessage(mNlfd, msg, 0, msg.length, (long) IO_TIMEOUT)) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception when sendToKernel:" + e);
            }
        } else {
            if (type < 17 || type >= 20) {
                Log.e(TAG, "sendToKernel invalid message type:" + ((int) type));
            } else {
                Log.e(TAG, "sendToKernel type[" + ((int) type) + "] failed, mNlfd=null !!!");
            }
            return false;
        }
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
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception when sendToKernel:" + e);
            }
        } else {
            if (type < 17 || type >= 20) {
                Log.e(TAG, "sendToKernel invalid message type:" + ((int) type));
            } else {
                Log.e(TAG, "sendToKernel type[" + ((int) type) + "] failed, mNlfd=null !!!");
            }
            return false;
        }
    }

    private boolean sendToKernel(int uid, WechatParamInfo[] infos) {
        if (mNlfd != null) {
            try {
                StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
                nlMsgHdr.nlmsg_len = (infos.length * 36) + 24;
                nlMsgHdr.nlmsg_type = 18;
                nlMsgHdr.nlmsg_flags = 1;
                nlMsgHdr.nlmsg_pid = Process.myPid();
                byte[] msg = new byte[nlMsgHdr.nlmsg_len];
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg);
                byteBuffer.order(ByteOrder.nativeOrder());
                nlMsgHdr.pack(byteBuffer);
                byteBuffer.putInt(uid);
                byteBuffer.putInt(infos.length);
                for (WechatParamInfo paramInfo : infos) {
                    paramInfo.pack(byteBuffer);
                }
                if (msg.length == NetlinkSocket.sendMessage(mNlfd, msg, 0, msg.length, (long) IO_TIMEOUT)) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Exception when sendToKernel:" + e);
            }
        } else {
            Log.e(TAG, "sendToKernel type[18] failed, mNlfd=null !!!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendWechatParams() {
        String[] wechatParams = this.mWifiRomUpdateHelper.getWechatLmParams();
        if (wechatParams == null || wechatParams.length == 0) {
            Log.e(TAG, "sendWechatParams failed to get params...");
            return false;
        }
        int uid = this.mPMS.getPackageUid(WECHAT_PACKAGE_NAME, 65536, this.mUserId);
        if (uid <= 1000) {
            Log.d(TAG, "sendWechatParams invalid uid...");
            uid = 0;
        }
        Log.d(TAG, "sendWechatParams uid=" + uid);
        WechatParamInfo[] paramInfo = new WechatParamInfo[wechatParams.length];
        for (int i = 0; i < wechatParams.length; i++) {
            paramInfo[i] = new WechatParamInfo();
            if (!paramInfo[i].setParams(wechatParams[i].split("#"))) {
                return false;
            }
        }
        return sendToKernel(uid, paramInfo);
    }

    public int getLuckyMoneyCount() {
        int count = this.mLuckyMoneyCount;
        this.mLuckyMoneyCount = 0;
        Log.d(TAG, "getLuckyMoneyCount count=" + count);
        return count;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMyToast(String text, int cnt) {
        if (SystemProperties.getBoolean("sys.lm.debug.show_toast", false)) {
            final Toast toast = Toast.makeText(this.mContext, text, 1);
            new Timer().schedule(new TimerTask() {
                /* class com.android.server.wifi.OppoNfHooksHelper.AnonymousClass5 */

                public void run() {
                    toast.show();
                }
            }, 0);
            new Timer().schedule(new TimerTask() {
                /* class com.android.server.wifi.OppoNfHooksHelper.AnonymousClass6 */

                public void run() {
                    toast.cancel();
                }
            }, (long) cnt);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] buf = new char[(bytes.length * 2)];
        int index = 0;
        for (byte b : bytes) {
            int index2 = index + 1;
            buf[index] = HEX_CHAR[(b >>> 4) & 15];
            index = index2 + 1;
            buf[index2] = HEX_CHAR[b & 15];
        }
        return new String(buf);
    }
}
