package com.qualcomm.qcrilhook;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiItemType;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiStructType;
import com.qualcomm.qcrilhook.QmiOemHookConstants.ResponseType;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiArray;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiInteger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;

public class TunerOemHook {
    private static String LOG_TAG = "TunerOemHook";
    public static final short QCRILHOOK_TUNER_RFRPE_GET_PROVISIONED_TABLE_REVISION_REQ = (short) 34;
    public static final short QCRILHOOK_TUNER_RFRPE_GET_RFM_SCENARIO_REQ = (short) 33;
    public static final short QCRILHOOK_TUNER_RFRPE_SET_RFM_SCENARIO_REQ = (short) 32;
    private static final byte TLV_TYPE_COMMON_REQ_SCENARIO_ID = (byte) 1;
    private static final byte TLV_TYPE_GET_PROVISION_TABLE_OPTIONAL_TAG1 = (byte) 16;
    private static final byte TLV_TYPE_GET_PROVISION_TABLE_OPTIONAL_TAG2 = (byte) 17;
    private static final short TUNER_SERVICE_ID = (short) 4;
    private static TunerOemHook mInstance;
    private static int mRefCount = 0;
    Context mContext;
    private QmiOemHook mQmiOemHook;

    public static class ProvisionTable {
        public int[] prv_tbl_oem = null;
        public int prv_tbl_rev = -1;

        public ProvisionTable(ByteBuffer buf) {
            Log.d(TunerOemHook.LOG_TAG, "ProvsionTableInfo: " + buf.toString());
            while (buf.hasRemaining() && buf.remaining() >= 3) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case 16:
                        byte[] data = new byte[length];
                        for (int i = 0; i < length; i++) {
                            data[i] = buf.get();
                        }
                        ByteBuffer wrapped = ByteBuffer.wrap(data);
                        wrapped.order(ByteOrder.LITTLE_ENDIAN);
                        this.prv_tbl_rev = wrapped.getInt();
                        Log.i(TunerOemHook.LOG_TAG, "Provision Table Rev = " + this.prv_tbl_rev);
                        break;
                    case 17:
                        byte prv_tbl_oem_len = buf.get();
                        this.prv_tbl_oem = new int[prv_tbl_oem_len];
                        for (byte i2 = (byte) 0; i2 < prv_tbl_oem_len; i2++) {
                            this.prv_tbl_oem[i2] = buf.getShort();
                        }
                        Log.i(TunerOemHook.LOG_TAG, "Provsions Table OEM = " + Arrays.toString(this.prv_tbl_oem));
                        break;
                    default:
                        Log.i(TunerOemHook.LOG_TAG, "Invalid TLV type");
                        break;
                }
            }
        }
    }

    public class ScenarioRequest extends BaseQmiStructType {
        public QmiArray<QmiInteger> list;

        public ScenarioRequest(int[] list) {
            this.list = TunerOemHook.this.intArrayToQmiArray(list);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.list};
        }

        public short[] getTypes() {
            return new short[]{(short) 1};
        }
    }

    public static class TunerSolResponse {
        public Object data;
        public int result;
    }

    public static class TunerUnsolIndication {
        public Object obj;
        public int oemHookMesgId;
    }

    private TunerOemHook(Context context, Looper listenerLooper) {
        this.mContext = context;
        this.mQmiOemHook = QmiOemHook.getInstance(context, listenerLooper);
    }

    public static TunerOemHook getInstance(Context context, Handler listenerHandler) {
        if (mInstance == null) {
            mInstance = new TunerOemHook(context, listenerHandler.getLooper());
        } else {
            mInstance.mContext = context;
        }
        mRefCount++;
        return mInstance;
    }

    public synchronized void registerOnReadyCb(Handler h, int what, Object obj) {
        QmiOemHook.registerOnReadyCb(h, what, null);
    }

    public synchronized void unregisterOnReadyCb(Handler h) {
        QmiOemHook.unregisterOnReadyCb(h);
    }

    public synchronized void dispose() {
        mRefCount--;
        if (mRefCount == 0) {
            Log.v(LOG_TAG, "dispose(): Unregistering service");
            this.mQmiOemHook.dispose();
            this.mQmiOemHook = null;
            mInstance = null;
        } else {
            Log.v(LOG_TAG, "dispose mRefCount = " + mRefCount);
        }
    }

    public Integer tuner_send_proximity_updates(int[] proximityValues) {
        ScenarioRequest req = new ScenarioRequest(proximityValues);
        try {
            return (Integer) receive(this.mQmiOemHook.sendQmiMessageSync((short) TUNER_SERVICE_ID, (short) 32, req.getTypes(), req.getItems()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int tuner_get_provisioned_table_revision() {
        try {
            return ((Integer) receive(this.mQmiOemHook.sendQmiMessageSync(TUNER_SERVICE_ID, (short) 34))).intValue();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Object receive(HashMap<Integer, Object> map) {
        int requestId = ((Integer) map.get(Integer.valueOf(1))).intValue();
        int responseSize = ((Integer) map.get(Integer.valueOf(2))).intValue();
        int successStatus = ((Integer) map.get(Integer.valueOf(3))).intValue();
        short messageId = ((Short) map.get(Integer.valueOf(8))).shortValue();
        ResponseType respType = (ResponseType) map.get(Integer.valueOf(5));
        Message msg = (Message) map.get(Integer.valueOf(4));
        ByteBuffer respByteBuf = (ByteBuffer) map.get(Integer.valueOf(6));
        Log.v(LOG_TAG, "receive respByteBuf = " + respByteBuf);
        Log.v(LOG_TAG, " responseSize=" + responseSize + " successStatus=" + successStatus + " messageId= " + messageId);
        Object returnObject = Integer.valueOf(successStatus);
        switch (messageId) {
            case (short) 32:
                Log.v(LOG_TAG, "Response: QCRILHOOK_TUNER_RFRPE_SET_RFM_SCENARIO_REQ=" + successStatus);
                return returnObject;
            case (short) 33:
                Log.v(LOG_TAG, "Response: QCRILHOOK_TUNER_RFRPE_GET_RFM_SCENARIO_REQ=" + successStatus);
                return returnObject;
            case (short) 34:
                Log.v(LOG_TAG, "Response: QCRILHOOK_TUNER_RFRPE_GET_PROVISIONED_TABLE_REVISION_REQ=" + successStatus);
                return Integer.valueOf(new ProvisionTable(respByteBuf).prv_tbl_rev);
            default:
                Log.v(LOG_TAG, "Invalid request");
                return returnObject;
        }
    }

    private QmiArray<QmiInteger> intArrayToQmiArray(int[] arr) {
        BaseQmiItemType[] qmiIntArray = new QmiInteger[arr.length];
        for (int i = 0; i < arr.length; i++) {
            qmiIntArray[i] = new QmiInteger((long) arr[i]);
        }
        return new QmiArray(qmiIntArray, (short) arr.length, QmiInteger.class);
    }
}
