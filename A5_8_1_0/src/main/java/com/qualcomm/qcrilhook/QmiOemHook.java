package com.qualcomm.qcrilhook;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.util.Log;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiItemType;
import com.qualcomm.qcrilhook.BaseQmiTypes.QmiBase;
import com.qualcomm.qcrilhook.QmiOemHookConstants.ResponseType;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiNull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class QmiOemHook extends Handler {
    private static final int DEFAULT_PHONE = 0;
    private static String LOG_TAG = "QMI_OEMHOOK";
    private static final int QMI_OEM_HOOK_UNSOL = 0;
    private static final int RESERVED_SIZE = 8;
    private static final boolean enableVLog = true;
    private static QmiOemHook mInstance;
    private static boolean mIsServiceConnected = false;
    private static int mRefCount = 0;
    private static RegistrantList sReadyCbRegistrantList = new RegistrantList();
    public static HashMap<Short, Registrant> serviceRegistrantsMap = new HashMap();
    private Context mContext;
    private QcRilHook mQcRilOemHook;
    private QcRilHookCallback mQcrilHookCb = new QcRilHookCallback() {
        public void onQcRilHookReady() {
            QmiOemHook.mIsServiceConnected = QmiOemHook.enableVLog;
            AsyncResult ar = new AsyncResult(null, Boolean.valueOf(QmiOemHook.mIsServiceConnected), null);
            Log.i(QmiOemHook.LOG_TAG, "onQcRilHookReadyCb notifying registrants");
            QmiOemHook.sReadyCbRegistrantList.notifyRegistrants(ar);
        }

        public synchronized void onQcRilHookDisconnected() {
            QmiOemHook.mIsServiceConnected = false;
            AsyncResult ar = new AsyncResult(null, Boolean.valueOf(QmiOemHook.mIsServiceConnected), null);
            Log.i(QmiOemHook.LOG_TAG, "onQcRilHookReadyCb: service disconnected; notifying registrants.");
            QmiOemHook.sReadyCbRegistrantList.notifyRegistrants(ar);
        }
    };
    int mResponseResult = 0;
    public ByteBuffer respByteBuf;

    private QmiOemHook(Context context) {
        this.mQcRilOemHook = new QcRilHook(context, this.mQcrilHookCb);
        QcRilHook.register(this, 0, null);
    }

    private QmiOemHook(Context context, Looper looper) {
        super(looper);
        this.mQcRilOemHook = new QcRilHook(context, this.mQcrilHookCb);
        QcRilHook.register(this, 0, null);
    }

    public static synchronized QmiOemHook getInstance(Context context) {
        QmiOemHook qmiOemHook;
        synchronized (QmiOemHook.class) {
            if (mInstance == null) {
                mInstance = new QmiOemHook(context);
            }
            mRefCount++;
            qmiOemHook = mInstance;
        }
        return qmiOemHook;
    }

    public static synchronized QmiOemHook getInstance(Context context, Looper looper) {
        QmiOemHook qmiOemHook;
        synchronized (QmiOemHook.class) {
            if (mInstance == null) {
                mInstance = new QmiOemHook(context, looper);
            }
            mRefCount++;
            qmiOemHook = mInstance;
        }
        return qmiOemHook;
    }

    public synchronized void dispose() {
        mRefCount--;
        if (mRefCount == 0) {
            vLog("dispose(): Unregistering QcRilHook and calling QcRilHook dispose");
            QcRilHook.unregister(this);
            mIsServiceConnected = false;
            this.mQcRilOemHook.dispose();
            mInstance = null;
            sReadyCbRegistrantList.removeCleared();
        } else {
            vLog("dispose mRefCount = " + mRefCount);
        }
    }

    private void vLog(String logString) {
        Log.v(LOG_TAG, logString);
    }

    public static void registerService(short serviceId, Handler h, int what) {
        Log.v(LOG_TAG, "Registering Service Id = " + serviceId + " h = " + h + " what = " + what);
        synchronized (serviceRegistrantsMap) {
            serviceRegistrantsMap.put(Short.valueOf(serviceId), new Registrant(h, what, null));
        }
    }

    public static void registerOnReadyCb(Handler h, int what, Object obj) {
        Log.v(LOG_TAG, "Registering Service for OnQcRilHookReadyCb =  h = " + h + " what = " + what);
        synchronized (sReadyCbRegistrantList) {
            sReadyCbRegistrantList.add(new Registrant(h, what, obj));
        }
    }

    public static void unregisterService(int serviceId) {
        synchronized (serviceRegistrantsMap) {
            serviceRegistrantsMap.remove(Integer.valueOf(serviceId));
        }
    }

    public static void unregisterOnReadyCb(Handler h) {
        synchronized (sReadyCbRegistrantList) {
            sReadyCbRegistrantList.remove(h);
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                Log.v(LOG_TAG, "Thread=" + Thread.currentThread().getName() + " received " + msg);
                Log.v(LOG_TAG, "QMI_OEM_HOOK_UNSOL received");
                Message mesg = msg.obj.result;
                byte[] response = mesg.obj;
                int phoneId = mesg.arg1;
                Log.d(LOG_TAG, "QMI_OEM_HOOK_UNSOL received phoneId: " + phoneId);
                receive(response, null, ResponseType.IS_UNSOL, phoneId);
                return;
            default:
                return;
        }
    }

    public static HashMap<Integer, Object> receive(byte[] payload, Message msg, ResponseType responseType, int phoneId) {
        Log.v(LOG_TAG, "receive responseData = " + EmbmsOemHook.bytesToHexString(payload) + " message=" + msg + " responseType= " + responseType);
        ByteBuffer respByteBuf = ByteBuffer.wrap(payload);
        if (respByteBuf == null) {
            Log.v(LOG_TAG, "respByteBuf is null");
            return null;
        }
        respByteBuf.order(BaseQmiItemType.QMI_BYTE_ORDER);
        Log.v(LOG_TAG, "receive respByteBuf after ByteBuffer.wrap(payload) = " + EmbmsOemHook.bytesToHexString(respByteBuf.array()));
        Log.v(LOG_TAG, "receive respByteBuf = " + respByteBuf);
        int requestId = respByteBuf.getInt();
        int responseSize = respByteBuf.getInt();
        int successStatus = -1;
        if (isValidQmiMessage(responseType, requestId)) {
            if (responseSize > 0) {
                short serviceId = respByteBuf.getShort();
                short messageId = respByteBuf.getShort();
                int responseTlvSize = responseSize - 4;
                if (responseType != ResponseType.IS_UNSOL) {
                    successStatus = PrimitiveParser.toUnsigned(respByteBuf.getShort());
                    responseTlvSize -= 2;
                }
                Log.d(LOG_TAG, "receive requestId=" + requestId + " responseSize=" + responseSize + " responseTlvSize=" + responseTlvSize + " serviceId=" + serviceId + " messageId=" + messageId + " successStatus = " + successStatus + " phoneId: " + phoneId);
                HashMap<Integer, Object> hashMap = new HashMap();
                hashMap.put(Integer.valueOf(1), Integer.valueOf(requestId));
                hashMap.put(Integer.valueOf(2), Integer.valueOf(responseTlvSize));
                hashMap.put(Integer.valueOf(7), Short.valueOf(serviceId));
                hashMap.put(Integer.valueOf(8), Short.valueOf(messageId));
                hashMap.put(Integer.valueOf(3), Integer.valueOf(successStatus));
                hashMap.put(Integer.valueOf(4), msg);
                hashMap.put(Integer.valueOf(5), responseType);
                hashMap.put(Integer.valueOf(6), respByteBuf);
                hashMap.put(Integer.valueOf(9), Integer.valueOf(phoneId));
                if (responseType != ResponseType.IS_UNSOL && responseType != ResponseType.IS_ASYNC_RESPONSE) {
                    return hashMap;
                }
                AsyncResult ar = new AsyncResult(null, hashMap, null);
                Registrant r = (Registrant) serviceRegistrantsMap.get(Short.valueOf(serviceId));
                if (r != null) {
                    Log.v(LOG_TAG, "Notifying registrant for responseType = " + responseType);
                    r.notifyRegistrant(ar);
                    return null;
                }
                Log.e(LOG_TAG, "Did not find the registered serviceId = " + serviceId);
            }
            return null;
        }
        Log.e(LOG_TAG, "requestId NOT in QMI OemHook range, No further processing");
        return null;
    }

    private static boolean isValidQmiMessage(ResponseType responseType, int requestId) {
        boolean z = enableVLog;
        if (responseType == ResponseType.IS_UNSOL) {
            if (requestId != IQcRilHook.QCRILHOOK_UNSOL_OEMHOOK) {
                z = false;
            }
            return z;
        }
        if (requestId != IQcRilHook.QCRILHOOK_QMI_OEMHOOK_REQUEST_ID) {
            z = false;
        }
        return z;
    }

    private byte[] createPayload(short serviceId, short messageId, short[] types, BaseQmiItemType[] qmiItems) {
        int i;
        int tlvSize = 0;
        if (qmiItems == null || types == null || qmiItems[0] == null) {
            Log.v(LOG_TAG, "This message has no payload");
        } else {
            for (BaseQmiItemType size : qmiItems) {
                tlvSize += size.getSize() + 3;
            }
        }
        ByteBuffer buf = QmiBase.createByteBuffer(tlvSize + 12);
        buf.putInt(0);
        buf.putInt(0);
        buf.putShort(serviceId);
        buf.putShort(messageId);
        Log.v(LOG_TAG, "createPayload: serviceId= " + serviceId + " messageId= " + messageId);
        if (!(qmiItems == null || types == null || qmiItems[0] == null)) {
            for (i = 0; i < qmiItems.length; i++) {
                vLog(qmiItems[i].toString());
                buf.put(qmiItems[i].toTlv(types[i]));
                Log.v(LOG_TAG, "Intermediate buf in QmiOemHook sendQmiMessage Sync or Async = " + EmbmsOemHook.bytesToHexString(qmiItems[i].toTlv(types[i])));
            }
        }
        Log.v(LOG_TAG, "Byte buf in QmiOemHook createPayload = " + buf);
        return buf.array();
    }

    public byte[] sendQmiMessage(int serviceHook, short[] types, BaseQmiItemType[] qmiItems) throws IOException {
        int i;
        int msgSize = 0;
        for (BaseQmiItemType size : qmiItems) {
            msgSize += size.getSize() + 3;
        }
        ByteBuffer buf = QmiBase.createByteBuffer(msgSize + 4);
        buf.putInt(0);
        buf.putShort(PrimitiveParser.parseShort(msgSize));
        for (i = 0; i < qmiItems.length; i++) {
            vLog(qmiItems[i].toString());
            buf.put(qmiItems[i].toTlv(types[i]));
        }
        AsyncResult result = this.mQcRilOemHook.sendQcRilHookMsg(serviceHook, buf.array());
        if (result.exception == null) {
            return (byte[]) result.result;
        }
        Log.w(LOG_TAG, String.format("sendQmiMessage() Failed : %s", new Object[]{result.exception.toString()}));
        result.exception.printStackTrace();
        throw new IOException();
    }

    public HashMap<Integer, Object> sendQmiMessageSync(short serviceId, short messageId, short[] types, BaseQmiItemType[] qmiItems) throws IOException {
        return sendQmiMessageSync(serviceId, messageId, types, qmiItems, 0);
    }

    public HashMap<Integer, Object> sendQmiMessageSync(short serviceId, short messageId, short[] types, BaseQmiItemType[] qmiItems, int phoneId) throws IOException {
        AsyncResult result = this.mQcRilOemHook.sendQcRilHookMsg((int) IQcRilHook.QCRILHOOK_QMI_OEMHOOK_REQUEST_ID, createPayload(serviceId, messageId, types, qmiItems), phoneId);
        if (result.exception == null) {
            return receive(result.result, null, ResponseType.IS_SYNC_RESPONSE, phoneId);
        }
        Log.w(LOG_TAG, String.format("sendQmiMessage() Failed : %s", new Object[]{result.exception.toString()}));
        result.exception.printStackTrace();
        throw new IOException();
    }

    public void sendQmiMessageAsync(short serviceId, short messageId, short[] types, BaseQmiItemType[] qmiItems, Message msg) throws IOException {
        sendQmiMessageAsync(serviceId, messageId, types, qmiItems, msg, 0);
    }

    public void sendQmiMessageAsync(short serviceId, short messageId, short[] types, BaseQmiItemType[] qmiItems, Message msg, int phoneId) throws IOException {
        Log.w(LOG_TAG, "sendQmiMessageAsync phoneId: " + phoneId);
        this.mQcRilOemHook.sendQcRilHookMsgAsync(IQcRilHook.QCRILHOOK_QMI_OEMHOOK_REQUEST_ID, createPayload(serviceId, messageId, types, qmiItems), new OemHookCallback(msg), phoneId);
    }

    public byte[] sendQmiMessage(int serviceHook, short type, BaseQmiItemType qmiItem) throws IOException {
        return sendQmiMessage(serviceHook, new short[]{type}, new BaseQmiItemType[]{qmiItem});
    }

    public HashMap<Integer, Object> sendQmiMessageSync(short serviceId, short messageId, short type, BaseQmiItemType qmiItem) throws IOException {
        return sendQmiMessageSync(serviceId, messageId, type, qmiItem, 0);
    }

    public HashMap<Integer, Object> sendQmiMessageSync(short serviceId, short messageId, short type, BaseQmiItemType qmiItem, int phoneId) throws IOException {
        return sendQmiMessageSync(serviceId, messageId, new short[]{type}, new BaseQmiItemType[]{qmiItem}, phoneId);
    }

    public void sendQmiMessageAsync(short serviceId, short messageId, short type, BaseQmiItemType qmiItem, Message msg) throws IOException {
        sendQmiMessageAsync(serviceId, messageId, type, qmiItem, msg, 0);
    }

    public void sendQmiMessageAsync(short serviceId, short messageId, short type, BaseQmiItemType qmiItem, Message msg, int phoneId) throws IOException {
        Log.w(LOG_TAG, "sendQmiMessageAsync phoneId: " + phoneId);
        sendQmiMessageAsync(serviceId, messageId, new short[]{type}, new BaseQmiItemType[]{qmiItem}, msg, phoneId);
    }

    public byte[] sendQmiMessage(int serviceHook) throws IOException {
        return sendQmiMessage(serviceHook, (short) 0, new QmiNull());
    }

    public HashMap<Integer, Object> sendQmiMessageSync(short serviceId, short messageId) throws IOException {
        return sendQmiMessageSync(serviceId, messageId, null, null);
    }

    public void sendQmiMessageAsync(short serviceId, short messageId, Message msg) throws IOException {
        sendQmiMessageAsync(serviceId, messageId, msg, 0);
    }

    public void sendQmiMessageAsync(short serviceId, short messageId, Message msg, int phoneId) throws IOException {
        sendQmiMessageAsync(serviceId, messageId, null, null, msg, phoneId);
    }

    protected void finalize() {
        Log.v(LOG_TAG, "is destroyed");
    }
}
