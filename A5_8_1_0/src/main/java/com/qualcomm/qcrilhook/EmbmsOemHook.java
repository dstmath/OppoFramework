package com.qualcomm.qcrilhook;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.util.Log;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiItemType;
import com.qualcomm.qcrilhook.BaseQmiTypes.BaseQmiStructType;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiArray;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiByte;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiInteger;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiLong;
import com.qualcomm.qcrilhook.QmiPrimitiveTypes.QmiString;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class EmbmsOemHook extends Handler {
    private static final int DEFAULT_PHONE = 0;
    private static final short EMBMSHOOK_MSG_ID_ACTDEACT = (short) 17;
    private static final short EMBMSHOOK_MSG_ID_ACTIVATE = (short) 2;
    private static final short EMBMSHOOK_MSG_ID_CONTENT_DESCRIPTION = (short) 29;
    private static final short EMBMSHOOK_MSG_ID_DEACTIVATE = (short) 3;
    private static final short EMBMSHOOK_MSG_ID_DELIVER_LOG_PACKET = (short) 22;
    private static final short EMBMSHOOK_MSG_ID_DISABLE = (short) 1;
    private static final short EMBMSHOOK_MSG_ID_ENABLE = (short) 0;
    private static final short EMBMSHOOK_MSG_ID_GET_ACTIVE = (short) 5;
    private static final short EMBMSHOOK_MSG_ID_GET_ACTIVE_LOG_PACKET_IDS = (short) 21;
    private static final short EMBMSHOOK_MSG_ID_GET_AVAILABLE = (short) 4;
    private static final short EMBMSHOOK_MSG_ID_GET_COVERAGE = (short) 8;
    private static final short EMBMSHOOK_MSG_ID_GET_E911_STATE = (short) 27;
    private static final short EMBMSHOOK_MSG_ID_GET_EMBMS_STATUS = (short) 33;
    private static final short EMBMSHOOK_MSG_ID_GET_INTERESTED_TMGI_LIST_RESP = (short) 35;
    private static final short EMBMSHOOK_MSG_ID_GET_PLMN_LIST = (short) 31;
    private static final short EMBMSHOOK_MSG_ID_GET_SIB16_COVERAGE = (short) 24;
    private static final short EMBMSHOOK_MSG_ID_GET_SIG_STRENGTH = (short) 9;
    private static final short EMBMSHOOK_MSG_ID_GET_TIME = (short) 26;
    private static final short EMBMSHOOK_MSG_ID_SET_TIME = (short) 23;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_ACTIVE_TMGI_LIST = (short) 12;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_AVAILABLE_TMGI_LIST = (short) 15;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_CELL_ID = (short) 18;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL = (short) 30;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_COVERAGE_STATE = (short) 13;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_E911_STATE = (short) 28;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_EMBMS_STATUS = (short) 32;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_GET_INTERESTED_TMGI_LIST = (short) 34;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_OOS_STATE = (short) 16;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_RADIO_STATE = (short) 19;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_SAI_LIST = (short) 20;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_SIB16 = (short) 25;
    private static final short EMBMSHOOK_MSG_ID_UNSOL_STATE_CHANGE = (short) 11;
    private static final short EMBMS_SERVICE_ID = (short) 2;
    private static final int FAILURE = -1;
    private static String LOG_TAG = "EmbmsOemHook";
    private static final int OEM_HOOK_RESPONSE = 1;
    private static final short ONE_BYTE = (short) 1;
    private static final int QCRILHOOK_READY_CALLBACK = 2;
    private static final short SIZE_OF_EACH_PLMN_IN_BYTES = (short) 6;
    private static final int SIZE_OF_TMGI = 6;
    private static final int SUCCESS = 0;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_ACT_TMGI = (byte) 3;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_DEACT_TMGI = (byte) 4;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_EARFCN_LIST = (byte) 6;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_PRIORITY = (byte) 5;
    private static final byte TLV_TYPE_ACTDEACTIVATE_REQ_SAI_LIST = (byte) 16;
    private static final byte TLV_TYPE_ACTDEACTIVATE_RESP_ACTTMGI = (byte) 17;
    private static final byte TLV_TYPE_ACTDEACTIVATE_RESP_ACT_CODE = (byte) 2;
    private static final byte TLV_TYPE_ACTDEACTIVATE_RESP_DEACTTMGI = (byte) 18;
    private static final byte TLV_TYPE_ACTDEACTIVATE_RESP_DEACT_CODE = (byte) 3;
    private static final byte TLV_TYPE_ACTIVATE_REQ_EARFCN_LIST = (byte) 5;
    private static final byte TLV_TYPE_ACTIVATE_REQ_PRIORITY = (byte) 4;
    private static final byte TLV_TYPE_ACTIVATE_REQ_SAI_LIST = (byte) 16;
    private static final byte TLV_TYPE_ACTIVATE_REQ_TMGI = (byte) 3;
    private static final byte TLV_TYPE_ACTIVATE_RESP_TMGI = (byte) 17;
    private static final short TLV_TYPE_ACTIVELOGPACKETID_REQ_PACKET_ID_LIST = (short) 2;
    private static final short TLV_TYPE_ACTIVELOGPACKETID_RESP_PACKET_ID_LIST = (short) 2;
    private static final byte TLV_TYPE_COMMON_REQ_CALL_ID = (byte) 2;
    private static final byte TLV_TYPE_COMMON_REQ_TRACE_ID = (byte) 1;
    private static final byte TLV_TYPE_COMMON_RESP_CALL_ID = (byte) 16;
    private static final byte TLV_TYPE_COMMON_RESP_CODE = (byte) 2;
    private static final byte TLV_TYPE_COMMON_RESP_TRACE_ID = (byte) 1;
    private static final byte TLV_TYPE_CONTENT_DESCRIPTION_REQ_PARAMETER_ARRAY = (byte) 16;
    private static final byte TLV_TYPE_CONTENT_DESCRIPTION_REQ_TMGI = (byte) 3;
    private static final byte TLV_TYPE_DEACTIVATE_REQ_TMGI = (byte) 3;
    private static final byte TLV_TYPE_DEACTIVATE_RESP_TMGI = (byte) 17;
    private static final short TLV_TYPE_DELIVERLOGPACKET_REQ_LOG_PACKET = (short) 3;
    private static final short TLV_TYPE_DELIVERLOGPACKET_REQ_PACKET_ID = (short) 2;
    private static final byte TLV_TYPE_ENABLE_RESP_IFNAME = (byte) 17;
    private static final byte TLV_TYPE_ENABLE_RESP_IF_INDEX = (byte) 18;
    private static final byte TLV_TYPE_GET_ACTIVE_RESP_TMGI_ARRAY = (byte) 16;
    private static final byte TLV_TYPE_GET_AVAILABLE_RESP_TMGI_ARRAY = (byte) 16;
    private static final byte TLV_TYPE_GET_COVERAGE_STATE_RESP_STATE = (byte) 16;
    private static final short TLV_TYPE_GET_E911_RESP_STATE = (short) 16;
    private static final short TLV_TYPE_GET_EMBMS_STATUS_RESP = (short) 2;
    private static final byte TLV_TYPE_GET_INTERESTED_TMGI_LIST_RESP_TMGI = (byte) 3;
    private static final byte TLV_TYPE_GET_PLMN_LIST_RESP_PLMN_LIST = (byte) 2;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_ACTIVE_TMGI_LIST = (byte) 20;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_EXCESS_SNR = (byte) 18;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_MBSFN_AREA_ID = (byte) 16;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_NUMBER_OF_TMGI_PER_MBSFN = (byte) 19;
    private static final byte TLV_TYPE_GET_SIG_STRENGTH_RESP_SNR = (byte) 17;
    private static final byte TLV_TYPE_GET_TIME_RESP_DAY_LIGHT_SAVING = (byte) 16;
    private static final byte TLV_TYPE_GET_TIME_RESP_LEAP_SECONDS = (byte) 17;
    private static final byte TLV_TYPE_GET_TIME_RESP_LOCAL_TIME_OFFSET = (byte) 18;
    private static final byte TLV_TYPE_GET_TIME_RESP_TIME_MSECONDS = (byte) 3;
    private static final byte TLV_TYPE_SET_TIME_REQ_SNTP_SUCCESS = (byte) 1;
    private static final byte TLV_TYPE_SET_TIME_REQ_TIME_MSECONDS = (byte) 16;
    private static final byte TLV_TYPE_SET_TIME_REQ_TIME_STAMP = (byte) 17;
    private static final short TLV_TYPE_UNSOL_ACTIVE_IND_TMGI_ARRAY = (short) 2;
    private static final short TLV_TYPE_UNSOL_AVAILABLE_IND_TMGI_ARRAY_OR_RESPONSE_CODE = (short) 2;
    private static final short TLV_TYPE_UNSOL_CELL_ID_IND_CID = (short) 4;
    private static final short TLV_TYPE_UNSOL_CELL_ID_IND_MCC = (short) 2;
    private static final short TLV_TYPE_UNSOL_CELL_ID_IND_MNC = (short) 3;
    private static final short TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_CONTENT_CONTROL = (short) 16;
    private static final short TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_STATUS_CONTROL = (short) 17;
    private static final short TLV_TYPE_UNSOL_CONTENT_DESC_PER_OBJ_CONTROL_TMGI = (short) 2;
    private static final short TLV_TYPE_UNSOL_COVERAGE_IND_STATE_OR_RESPONSE_CODE = (short) 2;
    private static final short TLV_TYPE_UNSOL_E911_STATE_OR_RESPONSE_CODE = (short) 2;
    private static final short TLV_TYPE_UNSOL_EMBMS_STATUS = (short) 1;
    private static final short TLV_TYPE_UNSOL_OOS_IND_STATE = (short) 2;
    private static final short TLV_TYPE_UNSOL_OOS_IND_TMGI_ARRAY = (short) 3;
    private static final short TLV_TYPE_UNSOL_RADIO_STATE = (short) 2;
    private static final short TLV_TYPE_UNSOL_SAI_IND_AVAILABLE_SAI_LIST = (short) 4;
    private static final short TLV_TYPE_UNSOL_SAI_IND_CAMPED_SAI_LIST = (short) 2;
    private static final short TLV_TYPE_UNSOL_SAI_IND_SAI_PER_GROUP_LIST = (short) 3;
    private static final short TLV_TYPE_UNSOL_SIB16 = (short) 1;
    private static final short TLV_TYPE_UNSOL_STATE_IND_IF_INDEX = (short) 3;
    private static final short TLV_TYPE_UNSOL_STATE_IND_IP_ADDRESS = (short) 2;
    private static final short TLV_TYPE_UNSOL_STATE_IND_STATE = (short) 1;
    private static final short TWO_BYTES = (short) 2;
    private static final int UNSOL_BASE_QCRILHOOK = 4096;
    public static final int UNSOL_TYPE_ACTIVE_TMGI_LIST = 2;
    public static final int UNSOL_TYPE_AVAILABLE_TMGI_LIST = 4;
    public static final int UNSOL_TYPE_BROADCAST_COVERAGE = 3;
    public static final int UNSOL_TYPE_CELL_ID = 6;
    public static final int UNSOL_TYPE_CONTENT_DESC_PER_OBJ_CONTROL = 11;
    public static final int UNSOL_TYPE_E911_STATE = 10;
    public static final int UNSOL_TYPE_EMBMSOEMHOOK_READY_CALLBACK = 4097;
    public static final int UNSOL_TYPE_EMBMS_STATUS = 12;
    public static final int UNSOL_TYPE_GET_INTERESTED_TMGI_LIST = 13;
    public static final int UNSOL_TYPE_OOS_STATE = 5;
    public static final int UNSOL_TYPE_RADIO_STATE = 7;
    public static final int UNSOL_TYPE_SAI_LIST = 8;
    public static final int UNSOL_TYPE_SIB16_COVERAGE = 9;
    public static final int UNSOL_TYPE_STATE_CHANGE = 1;
    private static int mRefCount = 0;
    private static EmbmsOemHook sInstance;
    private QmiOemHook mQmiOemHook;
    private RegistrantList mRegistrants = new RegistrantList();

    public class ActDeactRequest extends BaseQmiStructType {
        public QmiArray<QmiByte> actTmgi;
        public QmiByte callId;
        public QmiArray<QmiByte> deActTmgi;
        public QmiArray<QmiInteger> earfcnList;
        public QmiInteger priority;
        public QmiArray<QmiInteger> saiList;
        public QmiInteger traceId;

        public ActDeactRequest(int trace, byte callId, byte[] actTmgi, byte[] deActTmgi, int priority, int[] saiList, int[] earfcnList) {
            this.traceId = new QmiInteger((long) trace);
            this.callId = new QmiByte(callId);
            this.priority = new QmiInteger((long) priority);
            this.actTmgi = EmbmsOemHook.this.byteArrayToQmiArray((short) 1, actTmgi);
            this.deActTmgi = EmbmsOemHook.this.byteArrayToQmiArray((short) 1, deActTmgi);
            this.saiList = EmbmsOemHook.this.intArrayToQmiArray((short) 1, saiList);
            this.earfcnList = EmbmsOemHook.this.intArrayToQmiArray((short) 1, earfcnList);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId, this.callId, this.actTmgi, this.deActTmgi, this.priority, this.saiList, this.earfcnList};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 2, (short) 3, (short) 4, EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_ACTIVE, (short) 16, EmbmsOemHook.SIZE_OF_EACH_PLMN_IN_BYTES};
        }
    }

    public class ActDeactResponse {
        public short actCode = EmbmsOemHook.EMBMSHOOK_MSG_ID_ENABLE;
        public byte[] actTmgi = null;
        public short deactCode = EmbmsOemHook.EMBMSHOOK_MSG_ID_ENABLE;
        public byte[] deactTmgi = null;
        public int status;
        public int traceId = 0;

        public ActDeactResponse(int status, ByteBuffer buf) {
            this.status = status;
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                byte tmgiLength;
                byte[] tmgi;
                byte i;
                switch (type) {
                    case 1:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case 2:
                        this.actCode = buf.getShort();
                        Log.i(EmbmsOemHook.LOG_TAG, "Act code = " + this.actCode);
                        break;
                    case 3:
                        this.deactCode = buf.getShort();
                        Log.i(EmbmsOemHook.LOG_TAG, "Deact code = " + this.deactCode);
                        break;
                    case 16:
                        Log.i(EmbmsOemHook.LOG_TAG, "callid = " + buf.get());
                        break;
                    case 17:
                        tmgiLength = buf.get();
                        tmgi = new byte[tmgiLength];
                        for (i = (byte) 0; i < tmgiLength; i++) {
                            tmgi[i] = buf.get();
                        }
                        this.actTmgi = tmgi;
                        Log.i(EmbmsOemHook.LOG_TAG, "Act tmgi = " + EmbmsOemHook.bytesToHexString(this.actTmgi));
                        break;
                    case 18:
                        tmgiLength = buf.get();
                        tmgi = new byte[tmgiLength];
                        for (i = (byte) 0; i < tmgiLength; i++) {
                            tmgi[i] = buf.get();
                        }
                        this.deactTmgi = tmgi;
                        Log.i(EmbmsOemHook.LOG_TAG, "Deact tmgi = " + EmbmsOemHook.bytesToHexString(this.deactTmgi));
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "TmgiResponse: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class ActiveLogPacketIDsRequest extends BaseQmiStructType {
        public QmiArray<QmiInteger> supportedLogPacketIdList;
        public QmiInteger traceId;

        public ActiveLogPacketIDsRequest(int trace, int[] supportedLogPacketIdList) {
            this.traceId = new QmiInteger((long) trace);
            this.supportedLogPacketIdList = EmbmsOemHook.this.intArrayToQmiArray((short) 2, supportedLogPacketIdList);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId, this.supportedLogPacketIdList};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 2};
        }
    }

    public class ActiveLogPacketIDsResponse {
        public int[] activePacketIdList = null;
        public int status;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ActiveLogPacketIDsResponse(int status, ByteBuffer buf) {
            this.status = status;
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                            short logPacketIdLength = buf.getShort();
                            int[] activeLogPacketIdListArray = new int[logPacketIdLength];
                            for (short i = EmbmsOemHook.EMBMSHOOK_MSG_ID_ENABLE; i < logPacketIdLength; i++) {
                                activeLogPacketIdListArray[i] = buf.getInt();
                            }
                            this.activePacketIdList = activeLogPacketIdListArray;
                            Log.i(EmbmsOemHook.LOG_TAG, "Active log packet Id's = " + Arrays.toString(this.activePacketIdList));
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "ActiveLogPacketIDsResponse: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Invalid format of byte buffer received in ActiveLogPacketIDsResponse");
                }
            }
        }
    }

    public class BasicRequest extends BaseQmiStructType {
        public QmiInteger traceId;

        public BasicRequest(int trace) {
            this.traceId = new QmiInteger((long) trace);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId};
        }

        public short[] getTypes() {
            return new short[]{(short) 1};
        }
    }

    public class CellIdIndication {
        public String id = null;
        public String mcc = null;
        public String mnc = null;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public CellIdIndication(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    byte[] temp;
                    int i;
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                            temp = new byte[length];
                            for (i = 0; i < length; i++) {
                                temp[i] = buf.get();
                            }
                            this.mcc = new QmiString(temp).toStringValue();
                            Log.i(EmbmsOemHook.LOG_TAG, "MCC = " + this.mcc);
                            break;
                        case 3:
                            temp = new byte[length];
                            for (i = 0; i < length; i++) {
                                temp[i] = buf.get();
                            }
                            this.mnc = new QmiString(temp).toStringValue();
                            Log.i(EmbmsOemHook.LOG_TAG, "MNC = " + this.mnc);
                            break;
                        case 4:
                            this.id = String.format("%7s", new Object[]{Integer.toHexString(buf.getInt())}).replace(' ', '0');
                            Log.i(EmbmsOemHook.LOG_TAG, "CellId = " + this.id);
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "CellIdIndication: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing for CellIdIndication");
                }
            }
        }
    }

    public class ContentDescPerObjectControlIndication {
        public int perObjectContentControl;
        public int perObjectStatusControl;
        public byte[] tmgi = null;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ContentDescPerObjectControlIndication(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                            byte tmgiLength = buf.get();
                            byte[] tmgi = new byte[tmgiLength];
                            for (byte i = (byte) 0; i < tmgiLength; i++) {
                                tmgi[i] = buf.get();
                            }
                            this.tmgi = tmgi;
                            Log.i(EmbmsOemHook.LOG_TAG, "tmgi = " + EmbmsOemHook.bytesToHexString(this.tmgi));
                            break;
                        case 16:
                            this.perObjectContentControl = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "perObjectContentControl = " + this.perObjectContentControl);
                            break;
                        case 17:
                            this.perObjectStatusControl = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "perObjectStatusControl = " + this.perObjectStatusControl);
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "ContentDescPerObjectControl: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing forContentDescPerObjectControl Notification");
                }
            }
        }
    }

    public class ContentDescriptionReq extends BaseQmiStructType {
        public QmiByte callId;
        public QmiArray<QmiInteger> parameterArray;
        public QmiArray<QmiByte> tmgi;
        public QmiInteger traceId;

        public ContentDescriptionReq(int trace, byte callId, byte[] tmgi, int[] parameterArray) {
            this.traceId = new QmiInteger((long) trace);
            this.callId = new QmiByte(callId);
            this.tmgi = EmbmsOemHook.this.byteArrayToQmiArray((short) 1, tmgi);
            this.parameterArray = EmbmsOemHook.this.intArrayToQmiArray((short) 1, parameterArray, (short) 2);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId, this.callId, this.tmgi, this.parameterArray};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 2, (short) 3, (short) 16};
        }
    }

    public class CoverageState {
        public int code = 0;
        public int state;
        public int status;
        public int traceId = 0;

        public CoverageState(ByteBuffer buf, short msgId) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            continue;
                        case 2:
                            if (msgId == EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_COVERAGE) {
                                this.code = buf.getInt();
                                Log.i(EmbmsOemHook.LOG_TAG, "response code = " + this.code);
                                continue;
                            }
                            break;
                        case 16:
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "CoverageState: Unexpected Type " + type);
                            continue;
                    }
                    this.state = buf.getInt();
                    Log.i(EmbmsOemHook.LOG_TAG, "Coverage State = " + this.state);
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Invalid format of byte buffer received in CoverageState");
                }
            }
        }
    }

    public class DeliverLogPacketRequest extends BaseQmiStructType {
        public QmiArray<QmiByte> logPacket;
        public QmiInteger logPacketId;
        public QmiInteger traceId;

        public DeliverLogPacketRequest(int trace, int logPacketId, byte[] logPacket) {
            this.traceId = new QmiInteger((long) trace);
            this.logPacketId = new QmiInteger((long) logPacketId);
            this.logPacket = EmbmsOemHook.this.byteArrayToQmiArray((short) 2, logPacket);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId, this.logPacketId, this.logPacket};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 2, (short) 3};
        }
    }

    public class DisableResponse {
        public byte callId = (byte) 0;
        public int code = 0;
        public int status;
        public int traceId;

        public DisableResponse(int error, ByteBuffer buf) {
            this.status = error;
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case 1:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case 2:
                        this.code = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "code = " + this.code);
                        break;
                    case 16:
                        this.callId = buf.get();
                        Log.i(EmbmsOemHook.LOG_TAG, "callid = " + this.callId);
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "DisableResponse: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class E911StateIndication {
        public int code;
        public int state;
        public int traceId = 0;

        public E911StateIndication(ByteBuffer buf, short msgId) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            continue;
                        case 2:
                            if (msgId == EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_E911_STATE) {
                                this.code = buf.getInt();
                                Log.i(EmbmsOemHook.LOG_TAG, "response code = " + this.code);
                                continue;
                            }
                            break;
                        case 16:
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "E911 State: Unexpected Type " + type);
                            continue;
                    }
                    this.state = buf.getInt();
                    Log.i(EmbmsOemHook.LOG_TAG, "E911 State = " + this.state);
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing for E911 Notification");
                }
            }
        }
    }

    public class EmbmsStatus {
        private static final int TYPE_EMBMS_STATUS = 1000;
        public boolean embmsStatus = false;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public EmbmsStatus(ByteBuffer buf, int msgId) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    if (type == 1 && msgId == 32) {
                        type = TYPE_EMBMS_STATUS;
                    }
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                        case TYPE_EMBMS_STATUS /*1000*/:
                            byte status = buf.get();
                            Log.i(EmbmsOemHook.LOG_TAG, "Unsol embmsStatus received = " + status);
                            if (status == (byte) 1) {
                                this.embmsStatus = true;
                            }
                            Log.i(EmbmsOemHook.LOG_TAG, "Unsol embmsStatus = " + this.embmsStatus);
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "embmsStatus: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing for embmsStatus");
                }
            }
        }
    }

    public class EnableResponse {
        public byte callId = (byte) 0;
        public int code = 0;
        public int ifIndex = 0;
        public String interfaceName = null;
        public int status;
        public int traceId;

        public EnableResponse(int error, ByteBuffer buf) {
            this.status = error;
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case 1:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case 2:
                        this.code = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "code = " + this.code);
                        break;
                    case 16:
                        this.callId = buf.get();
                        Log.i(EmbmsOemHook.LOG_TAG, "callid = " + this.callId);
                        break;
                    case 17:
                        byte[] name = new byte[length];
                        for (int i = 0; i < length; i++) {
                            name[i] = buf.get();
                        }
                        this.interfaceName = new QmiString(name).toStringValue();
                        Log.i(EmbmsOemHook.LOG_TAG, "ifName = " + this.interfaceName);
                        break;
                    case 18:
                        this.ifIndex = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "ifIndex = " + this.ifIndex);
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "EnableResponse: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class GenericRequest extends BaseQmiStructType {
        public QmiByte callId;
        public QmiInteger traceId;

        public GenericRequest(int trace, byte callId) {
            this.traceId = new QmiInteger((long) trace);
            this.callId = new QmiByte(callId);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId, this.callId};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 2};
        }
    }

    public class GetInterestedTmgiResponse extends BaseQmiStructType {
        public QmiByte callId;
        public QmiArray<QmiByte> tmgiList;
        public QmiInteger traceId;

        public GetInterestedTmgiResponse(int traceId, byte callId, byte[] tmgiList) {
            this.traceId = new QmiInteger((long) traceId);
            this.callId = new QmiByte(callId);
            this.tmgiList = EmbmsOemHook.this.tmgiListArrayToQmiArray((short) 1, tmgiList);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId, this.callId, this.tmgiList};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 2, (short) 3};
        }
    }

    public class GetPLMNListResponse {
        public byte[] plmnList = null;
        public int status;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public GetPLMNListResponse(int status, ByteBuffer buf) {
            this.status = status;
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                            byte numOfPlmn = buf.get();
                            this.plmnList = new byte[(numOfPlmn * 6)];
                            int index = 0;
                            for (byte i = (byte) 0; i < numOfPlmn; i++) {
                                byte mccLen = buf.get();
                                buf.get(this.plmnList, index, mccLen);
                                index += mccLen;
                                byte mncLen = buf.get();
                                buf.get(this.plmnList, index, mncLen);
                                index += mncLen;
                                if (mncLen == (byte) 2) {
                                    int index2 = index + 1;
                                    this.plmnList[index] = (byte) 32;
                                    index = index2;
                                }
                            }
                            Log.i(EmbmsOemHook.LOG_TAG, "plmnList = " + EmbmsOemHook.bytesToHexString(this.plmnList));
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "GetPLMNListResponse: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Invalid format of byte buffer received in GetPLMNListResponse");
                }
            }
        }
    }

    public class OosState {
        public byte[] list = null;
        public int state;
        public int traceId = 0;

        public OosState(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case 1:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case 2:
                        this.state = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "OOs State = " + this.state);
                        break;
                    case 3:
                        this.list = EmbmsOemHook.this.parseTmgi(buf);
                        Log.i(EmbmsOemHook.LOG_TAG, "tmgiArray = " + EmbmsOemHook.bytesToHexString(this.list));
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "OosState: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class RadioStateIndication {
        public int state = 0;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public RadioStateIndication(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                            this.state = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "radio = " + this.state);
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "RadioStateIndication: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing for RadioStateIndication");
                }
            }
        }
    }

    public class RequestIndication {
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public RequestIndication(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "RequestIndication: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing for RequestIndication");
                }
            }
        }
    }

    public class SaiIndication {
        public int[] availableSaiList = null;
        public int[] campedSaiList = null;
        public int[] numSaiPerGroupList = null;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public SaiIndication(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                try {
                    int type = buf.get();
                    short length = buf.getShort();
                    byte listLength;
                    int[] list;
                    byte i;
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                            listLength = buf.get();
                            list = new int[listLength];
                            for (i = (byte) 0; i < listLength; i++) {
                                list[i] = buf.getInt();
                            }
                            this.campedSaiList = list;
                            Log.i(EmbmsOemHook.LOG_TAG, "Camped list = " + Arrays.toString(this.campedSaiList));
                            break;
                        case 3:
                            listLength = buf.get();
                            list = new int[listLength];
                            for (i = (byte) 0; i < listLength; i++) {
                                list[i] = buf.getInt();
                            }
                            this.numSaiPerGroupList = list;
                            Log.i(EmbmsOemHook.LOG_TAG, "Number of SAI per group list = " + Arrays.toString(this.numSaiPerGroupList));
                            break;
                        case 4:
                            short availableLength = buf.getShort();
                            list = new int[availableLength];
                            for (short i2 = EmbmsOemHook.EMBMSHOOK_MSG_ID_ENABLE; i2 < availableLength; i2++) {
                                list[i2] = buf.getInt();
                            }
                            this.availableSaiList = list;
                            Log.i(EmbmsOemHook.LOG_TAG, "Available SAI list = " + Arrays.toString(this.availableSaiList));
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "SaiIndication: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing for SaiIndication");
                }
            }
        }
    }

    public class SetTimeRequest extends BaseQmiStructType {
        public QmiByte sntpSuccess;
        public QmiLong timeMseconds;
        public QmiLong timeStamp;

        public SetTimeRequest(byte sntpSuccess, long timeMseconds, long timeStamp) {
            this.sntpSuccess = new QmiByte(sntpSuccess);
            this.timeMseconds = new QmiLong(timeMseconds);
            this.timeStamp = new QmiLong(timeStamp);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.sntpSuccess, this.timeMseconds, this.timeStamp};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 16, (short) 17};
        }
    }

    public class Sib16Coverage {
        public boolean inCoverage = false;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Sib16Coverage(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            if (buf.get() == (byte) 1) {
                                this.inCoverage = true;
                            }
                            Log.i(EmbmsOemHook.LOG_TAG, "Unsol SIB16 coverage status = " + this.inCoverage);
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "Sib16Coverage: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Unexpected buffer format when parsing for Sib16Coverage");
                }
            }
        }
    }

    public class SigStrengthResponse {
        public int code = 0;
        public float[] esnr = null;
        public int[] mbsfnAreaId = null;
        public float[] snr = null;
        public int status;
        public int[] tmgiPerMbsfn = null;
        public byte[] tmgilist = null;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public SigStrengthResponse(int status, ByteBuffer buf) {
            this.status = status;
            while (buf.hasRemaining()) {
                try {
                    int type = buf.get();
                    short length = buf.getShort();
                    byte i;
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                            this.code = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "code = " + this.code);
                            break;
                        case 16:
                            byte mbsfnLength = buf.get();
                            int[] mbsfnArray = new int[mbsfnLength];
                            for (i = (byte) 0; i < mbsfnLength; i++) {
                                mbsfnArray[i] = buf.getInt();
                            }
                            this.mbsfnAreaId = mbsfnArray;
                            Log.i(EmbmsOemHook.LOG_TAG, "MBSFN_Area_ID = " + Arrays.toString(this.mbsfnAreaId));
                            break;
                        case 17:
                            byte snrLength = buf.get();
                            float[] snrArray = new float[snrLength];
                            for (i = (byte) 0; i < snrLength; i++) {
                                snrArray[i] = buf.getFloat();
                            }
                            this.snr = snrArray;
                            Log.i(EmbmsOemHook.LOG_TAG, "SNR = " + Arrays.toString(this.snr));
                            break;
                        case 18:
                            byte esnrLength = buf.get();
                            float[] esnrArray = new float[esnrLength];
                            for (i = (byte) 0; i < esnrLength; i++) {
                                esnrArray[i] = buf.getFloat();
                            }
                            this.esnr = esnrArray;
                            Log.i(EmbmsOemHook.LOG_TAG, "EXCESS SNR = " + Arrays.toString(this.esnr));
                            break;
                        case 19:
                            byte tmgiPerMbsfnLength = buf.get();
                            int[] tmgiPerMbsfnArray = new int[tmgiPerMbsfnLength];
                            for (i = (byte) 0; i < tmgiPerMbsfnLength; i++) {
                                tmgiPerMbsfnArray[i] = buf.getInt();
                            }
                            this.tmgiPerMbsfn = tmgiPerMbsfnArray;
                            Log.i(EmbmsOemHook.LOG_TAG, "NUMBER OF TMGI PER MBSFN = " + Arrays.toString(this.tmgiPerMbsfn));
                            break;
                        case 20:
                            this.tmgilist = EmbmsOemHook.this.parseActiveTmgi(buf);
                            Log.i(EmbmsOemHook.LOG_TAG, "tmgiArray = " + EmbmsOemHook.bytesToHexString(this.tmgilist));
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "SigStrengthResponse: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Invalid format of byte buffer received in SigStrengthResponse");
                }
            }
            if (this.snr == null) {
                this.snr = new float[0];
            }
            if (this.esnr == null) {
                this.esnr = new float[0];
            }
            if (this.tmgiPerMbsfn == null) {
                this.tmgiPerMbsfn = new int[0];
            }
            if (this.mbsfnAreaId == null) {
                this.mbsfnAreaId = new int[0];
            }
            if (this.tmgilist == null) {
                this.tmgilist = new byte[0];
            }
        }
    }

    public class StateChangeInfo {
        public int ifIndex;
        public String ipAddress;
        public int state;

        public StateChangeInfo(int state, String address, int index) {
            this.state = state;
            this.ipAddress = address;
            this.ifIndex = index;
        }

        public StateChangeInfo(ByteBuffer buf) {
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case 1:
                        this.state = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "State = " + this.state);
                        break;
                    case 2:
                        byte[] address = new byte[length];
                        for (int i = 0; i < length; i++) {
                            address[i] = buf.get();
                        }
                        this.ipAddress = new QmiString(address).toString();
                        Log.i(EmbmsOemHook.LOG_TAG, "ip Address = " + this.ipAddress);
                        break;
                    case 3:
                        this.ifIndex = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "index = " + this.ifIndex);
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "StateChangeInfo: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class TimeResponse {
        public boolean additionalInfo = false;
        public int code = 0;
        public boolean dayLightSaving = false;
        public byte leapSeconds = (byte) 0;
        public long localTimeOffset = 0;
        public int status;
        public long timeMseconds = 0;
        public int traceId = 0;

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public TimeResponse(int status, ByteBuffer buf) {
            this.status = status;
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            break;
                        case 2:
                            this.code = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "code = " + this.code);
                            break;
                        case 3:
                            this.timeMseconds = buf.getLong();
                            Log.i(EmbmsOemHook.LOG_TAG, "timeMseconds = " + this.timeMseconds);
                            break;
                        case 16:
                            this.additionalInfo = true;
                            if (buf.get() == (byte) 1) {
                                this.dayLightSaving = true;
                            }
                            Log.i(EmbmsOemHook.LOG_TAG, "dayLightSaving = " + this.dayLightSaving);
                            break;
                        case 17:
                            this.additionalInfo = true;
                            this.leapSeconds = buf.get();
                            Log.i(EmbmsOemHook.LOG_TAG, "leapSeconds = " + this.leapSeconds);
                            break;
                        case 18:
                            this.additionalInfo = true;
                            this.localTimeOffset = (long) buf.get();
                            Log.i(EmbmsOemHook.LOG_TAG, "localTimeOffset = " + this.localTimeOffset);
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "TimeResponse: Unexpected Type " + type);
                            break;
                    }
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Invalid format of byte buffer received in TimeResponse");
                }
            }
            Log.i(EmbmsOemHook.LOG_TAG, "additionalInfo = " + this.additionalInfo);
        }

        public TimeResponse(int traceId, int status, long timeMseconds, boolean additionalInfo, long localTimeOffset, boolean dayLightSaving, byte leapSeconds) {
            this.status = status;
            this.traceId = traceId;
            this.code = 0;
            this.timeMseconds = timeMseconds;
            this.localTimeOffset = localTimeOffset;
            this.additionalInfo = additionalInfo;
            this.dayLightSaving = dayLightSaving;
            this.leapSeconds = leapSeconds;
            Log.i(EmbmsOemHook.LOG_TAG, "TimeResponse: traceId = " + this.traceId + " code = " + this.code + " timeMseconds = " + this.timeMseconds + "additionalInfo = " + this.additionalInfo + " localTimeOffset = " + this.localTimeOffset + " dayLightSaving = " + this.dayLightSaving + " leapSeconds = " + this.leapSeconds);
        }
    }

    public class TmgiActivateRequest extends BaseQmiStructType {
        public QmiByte callId;
        public QmiArray<QmiInteger> earfcnList;
        public QmiInteger priority;
        public QmiArray<QmiInteger> saiList;
        public QmiArray<QmiByte> tmgi;
        public QmiInteger traceId;

        public TmgiActivateRequest(int trace, byte callId, byte[] tmgi, int priority, int[] saiList, int[] earfcnList) {
            this.traceId = new QmiInteger((long) trace);
            this.callId = new QmiByte(callId);
            this.priority = new QmiInteger((long) priority);
            this.tmgi = EmbmsOemHook.this.byteArrayToQmiArray((short) 1, tmgi);
            this.saiList = EmbmsOemHook.this.intArrayToQmiArray((short) 1, saiList);
            this.earfcnList = EmbmsOemHook.this.intArrayToQmiArray((short) 1, earfcnList);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId, this.callId, this.tmgi, this.priority, this.saiList, this.earfcnList};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 2, (short) 3, (short) 4, (short) 16, EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_ACTIVE};
        }
    }

    public class TmgiDeActivateRequest extends BaseQmiStructType {
        public QmiByte callId;
        public QmiArray<QmiByte> tmgi;
        public QmiInteger traceId;

        public TmgiDeActivateRequest(int trace, byte[] tmgi, byte callId) {
            this.traceId = new QmiInteger((long) trace);
            this.tmgi = EmbmsOemHook.this.byteArrayToQmiArray((short) 1, tmgi);
            this.callId = new QmiByte(callId);
        }

        public BaseQmiItemType[] getItems() {
            return new BaseQmiItemType[]{this.traceId, this.callId, this.tmgi};
        }

        public short[] getTypes() {
            return new short[]{(short) 1, (short) 2, (short) 3};
        }
    }

    public class TmgiListIndication {
        public int code = 0;
        public byte[] list = new byte[0];
        public byte[] sessions = null;
        public int traceId = 0;

        public TmgiListIndication(ByteBuffer buf, short msgId) {
            while (buf.hasRemaining()) {
                try {
                    int type = PrimitiveParser.toUnsigned(buf.get());
                    int length = PrimitiveParser.toUnsigned(buf.getShort());
                    switch (type) {
                        case 1:
                            this.traceId = buf.getInt();
                            Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                            continue;
                        case 2:
                            if (msgId == (short) 4 || msgId == EmbmsOemHook.EMBMSHOOK_MSG_ID_GET_ACTIVE) {
                                this.code = buf.getInt();
                                Log.i(EmbmsOemHook.LOG_TAG, "response code = " + this.code);
                                continue;
                            }
                        case 16:
                            break;
                        default:
                            Log.e(EmbmsOemHook.LOG_TAG, "TmgiListIndication: Unexpected Type " + type);
                            continue;
                    }
                    this.list = EmbmsOemHook.this.parseTmgi(buf);
                    Log.i(EmbmsOemHook.LOG_TAG, "tmgiArray = " + EmbmsOemHook.bytesToHexString(this.list));
                } catch (BufferUnderflowException e) {
                    Log.e(EmbmsOemHook.LOG_TAG, "Invalid format of byte buffer received in TmgiListIndication");
                }
            }
        }
    }

    public class TmgiResponse {
        public int code = 0;
        public int status;
        public byte[] tmgi = null;
        public int traceId = 0;

        public TmgiResponse(int status, ByteBuffer buf) {
            this.status = status;
            while (buf.hasRemaining()) {
                int type = PrimitiveParser.toUnsigned(buf.get());
                int length = PrimitiveParser.toUnsigned(buf.getShort());
                switch (type) {
                    case 1:
                        this.traceId = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "traceId = " + this.traceId);
                        break;
                    case 2:
                        this.code = buf.getInt();
                        Log.i(EmbmsOemHook.LOG_TAG, "code = " + this.code);
                        break;
                    case 16:
                        Log.i(EmbmsOemHook.LOG_TAG, "callid = " + buf.get());
                        break;
                    case 17:
                        byte tmgiLength = buf.get();
                        byte[] tmgi = new byte[tmgiLength];
                        for (byte i = (byte) 0; i < tmgiLength; i++) {
                            tmgi[i] = buf.get();
                        }
                        this.tmgi = tmgi;
                        Log.i(EmbmsOemHook.LOG_TAG, "tmgi = " + EmbmsOemHook.bytesToHexString(this.tmgi));
                        break;
                    default:
                        Log.e(EmbmsOemHook.LOG_TAG, "TmgiResponse: Unexpected Type " + type);
                        break;
                }
            }
        }
    }

    public class UnsolObject {
        public Object obj;
        public int phoneId;
        public int unsolId;

        public UnsolObject(int i, Object o, int phone) {
            this.unsolId = i;
            this.obj = o;
            this.phoneId = phone;
        }
    }

    private EmbmsOemHook(Context context) {
        Log.v(LOG_TAG, "EmbmsOemHook ()");
        this.mQmiOemHook = QmiOemHook.getInstance(context);
        QmiOemHook.registerService((short) 2, this, 1);
        QmiOemHook.registerOnReadyCb(this, 2, null);
    }

    public static synchronized EmbmsOemHook getInstance(Context context) {
        EmbmsOemHook embmsOemHook;
        synchronized (EmbmsOemHook.class) {
            if (sInstance == null) {
                sInstance = new EmbmsOemHook(context);
                Log.d(LOG_TAG, "Singleton Instance of Embms created.");
            }
            mRefCount++;
            embmsOemHook = sInstance;
        }
        return embmsOemHook;
    }

    public synchronized void dispose() {
        int i = mRefCount + FAILURE;
        mRefCount = i;
        if (i == 0) {
            Log.d(LOG_TAG, "dispose(): Unregistering receiver");
            QmiOemHook.unregisterService(2);
            QmiOemHook.unregisterOnReadyCb(this);
            this.mQmiOemHook.dispose();
            this.mQmiOemHook = null;
            sInstance = null;
            this.mRegistrants.removeCleared();
        } else {
            Log.v(LOG_TAG, "dispose mRefCount = " + mRefCount);
        }
    }

    public void handleMessage(Message msg) {
        Log.i(LOG_TAG, "received message : " + msg.what);
        AsyncResult ar = msg.obj;
        switch (msg.what) {
            case 1:
                HashMap<Integer, Object> map = ar.result;
                if (map != null) {
                    handleResponse(map);
                    break;
                } else {
                    Log.e(LOG_TAG, "Hashmap async userobj is NULL");
                    return;
                }
            case 2:
                notifyUnsol(UNSOL_TYPE_EMBMSOEMHOOK_READY_CALLBACK, ar.result, 0);
                break;
            default:
                Log.e(LOG_TAG, "Unexpected message received from QmiOemHook what = " + msg.what);
                break;
        }
    }

    private void handleResponse(HashMap<Integer, Object> map) {
        short msgId = ((Short) map.get(Integer.valueOf(8))).shortValue();
        int responseSize = ((Integer) map.get(Integer.valueOf(2))).intValue();
        int successStatus = ((Integer) map.get(Integer.valueOf(3))).intValue();
        Message msg = (Message) map.get(Integer.valueOf(4));
        int phoneId = ((Integer) map.get(Integer.valueOf(9))).intValue();
        if (msg != null) {
            msg.arg1 = phoneId;
        }
        ByteBuffer respByteBuf = (ByteBuffer) map.get(Integer.valueOf(6));
        Log.d(LOG_TAG, " responseSize=" + responseSize + " successStatus=" + successStatus + "phoneId: " + phoneId);
        switch (msgId) {
            case (short) 0:
                msg.obj = new EnableResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 1:
                msg.obj = new DisableResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 2:
            case (short) 3:
                msg.obj = new TmgiResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 4:
            case (short) 15:
                if (msgId != (short) 4 || successStatus == 0) {
                    notifyUnsol(4, new TmgiListIndication(respByteBuf, msgId), phoneId);
                    return;
                }
                Log.e(LOG_TAG, "Error received in EMBMSHOOK_MSG_ID_GET_AVAILABLE: " + successStatus);
                return;
            case (short) 5:
            case UNSOL_TYPE_EMBMS_STATUS /*12*/:
                if (msgId != (short) 5 || successStatus == 0) {
                    notifyUnsol(2, new TmgiListIndication(respByteBuf, msgId), phoneId);
                    return;
                }
                Log.e(LOG_TAG, "Error received in EMBMSHOOK_MSG_ID_GET_ACTIVE: " + successStatus);
                return;
            case (short) 8:
            case UNSOL_TYPE_GET_INTERESTED_TMGI_LIST /*13*/:
                if (msgId != (short) 8 || successStatus == 0) {
                    notifyUnsol(3, new CoverageState(respByteBuf, msgId), phoneId);
                    return;
                }
                Log.e(LOG_TAG, "Error received in EMBMSHOOK_MSG_ID_GET_COVERAGE: " + successStatus);
                return;
            case (short) 9:
                msg.obj = new SigStrengthResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case UNSOL_TYPE_CONTENT_DESC_PER_OBJ_CONTROL /*11*/:
                notifyUnsol(1, new StateChangeInfo(respByteBuf), phoneId);
                return;
            case (short) 16:
                notifyUnsol(5, new OosState(respByteBuf), phoneId);
                return;
            case (short) 17:
                msg.obj = new ActDeactResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 18:
                notifyUnsol(6, new CellIdIndication(respByteBuf), phoneId);
                return;
            case (short) 19:
                notifyUnsol(7, new RadioStateIndication(respByteBuf), phoneId);
                return;
            case (short) 20:
                notifyUnsol(8, new SaiIndication(respByteBuf), phoneId);
                return;
            case (short) 21:
                msg.obj = new ActiveLogPacketIDsResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 22:
                Log.v(LOG_TAG, " deliverLogPacket response successStatus=" + successStatus);
                return;
            case (short) 23:
                msg.arg1 = successStatus;
                msg.sendToTarget();
                return;
            case (short) 24:
            case (short) 25:
                if (msgId != (short) 24 || successStatus == 0) {
                    notifyUnsol(9, new Sib16Coverage(respByteBuf), phoneId);
                    return;
                }
                Log.e(LOG_TAG, "Error received in EMBMSHOOK_MSG_ID_GET_SIB16_COVERAGE: " + successStatus);
                return;
            case (short) 26:
                msg.obj = new TimeResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 27:
            case (short) 28:
                notifyUnsol(10, new E911StateIndication(respByteBuf, msgId), phoneId);
                return;
            case (short) 29:
                Log.v(LOG_TAG, " contentDescription response successStatus=" + successStatus);
                return;
            case (short) 30:
                notifyUnsol(11, new ContentDescPerObjectControlIndication(respByteBuf), phoneId);
                return;
            case (short) 31:
                msg.obj = new GetPLMNListResponse(successStatus, respByteBuf);
                msg.sendToTarget();
                return;
            case (short) 32:
            case (short) 33:
                notifyUnsol(12, new EmbmsStatus(respByteBuf, msgId), phoneId);
                return;
            case (short) 34:
                notifyUnsol(13, new RequestIndication(respByteBuf), phoneId);
                return;
            case (short) 35:
                Log.v(LOG_TAG, " getInterestedTmgiListResponse ack successStatus=" + successStatus);
                return;
            default:
                Log.e(LOG_TAG, "received unexpected msgId " + msgId);
                return;
        }
    }

    private void notifyUnsol(int type, Object payload, int phoneId) {
        AsyncResult ar = new AsyncResult(null, new UnsolObject(type, payload, phoneId), null);
        Log.i(LOG_TAG, "Notifying registrants type = " + type);
        this.mRegistrants.notifyRegistrants(ar);
    }

    public void registerForNotifications(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mRegistrants) {
            Log.i(LOG_TAG, "Adding a registrant");
            this.mRegistrants.add(r);
        }
    }

    public void unregisterForNotifications(Handler h) {
        synchronized (this.mRegistrants) {
            Log.i(LOG_TAG, "Removing a registrant");
            this.mRegistrants.remove(h);
        }
    }

    public int enable(int traceId, Message msg, int phoneId) {
        try {
            Log.i(LOG_TAG, "enable called on PhoneId: " + phoneId);
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_ENABLE, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during enable !!!!!!");
            return FAILURE;
        }
    }

    public int activateTmgi(int traceId, byte callId, byte[] tmgi, int priority, int[] saiList, int[] earfcnList, Message msg, int phoneId) {
        Log.i(LOG_TAG, "activateTmgi called on PhoneId: " + phoneId);
        TmgiActivateRequest req = new TmgiActivateRequest(traceId, callId, tmgi, priority, saiList, earfcnList);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) 2, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during activate !!!!!!");
            return FAILURE;
        }
    }

    public int deactivateTmgi(int traceId, byte callId, byte[] tmgi, Message msg, int phoneId) {
        Log.i(LOG_TAG, "deactivateTmgi called on PhoneId: " + phoneId);
        TmgiDeActivateRequest req = new TmgiDeActivateRequest(traceId, tmgi, callId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) 3, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during deactivate !!!!!!");
            return FAILURE;
        }
    }

    public int actDeactTmgi(int traceId, byte callId, byte[] actTmgi, byte[] deActTmgi, int priority, int[] saiList, int[] earfcnList, Message msg, int phoneId) {
        Log.i(LOG_TAG, "actDeactTmgi called on PhoneId: " + phoneId);
        ActDeactRequest req = new ActDeactRequest(traceId, callId, actTmgi, deActTmgi, priority, saiList, earfcnList);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) 17, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during activate-deactivate !!!!!!");
            return FAILURE;
        }
    }

    public int getAvailableTMGIList(int traceId, byte callId, int phoneId) {
        Log.i(LOG_TAG, "getAvailableTMGIList called on PhoneId: " + phoneId);
        GenericRequest req = new GenericRequest(traceId, callId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) 4, req.getTypes(), req.getItems(), null, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getAvailableTMGIList !!!!!!");
            return FAILURE;
        }
    }

    public int getActiveTMGIList(int traceId, byte callId, int phoneId) {
        Log.i(LOG_TAG, "getActiveTMGIList called on PhoneId: " + phoneId);
        GenericRequest req = new GenericRequest(traceId, callId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_GET_ACTIVE, req.getTypes(), req.getItems(), null, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getActiveTMGIList !!!!!!");
            return FAILURE;
        }
    }

    public int getCoverageState(int traceId, int phoneId) {
        Log.i(LOG_TAG, "getCoverageState called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_GET_COVERAGE, req.getTypes(), req.getItems(), null, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getActiveTMGIList !!!!!!");
            return FAILURE;
        }
    }

    public int getSignalStrength(int traceId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getSignalStrength called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_GET_SIG_STRENGTH, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during enable !!!!!!");
            return FAILURE;
        }
    }

    public int disable(int traceId, byte callId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "disable called on PhoneId: " + phoneId);
        GenericRequest req = new GenericRequest(traceId, callId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) 1, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during disable !!!!!!");
            return FAILURE;
        }
    }

    public int getActiveLogPacketIDs(int traceId, int[] supportedLogPacketIdList, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getActiveLogPacketIDs called on PhoneId: " + phoneId);
        ActiveLogPacketIDsRequest req = new ActiveLogPacketIDsRequest(traceId, supportedLogPacketIdList);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_GET_ACTIVE_LOG_PACKET_IDS, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during activate log packet ID's !!!!!!");
            return FAILURE;
        }
    }

    public int deliverLogPacket(int traceId, int logPacketId, byte[] logPacket, int phoneId) {
        Log.i(LOG_TAG, "deliverLogPacket called on PhoneId: " + phoneId);
        DeliverLogPacketRequest req = new DeliverLogPacketRequest(traceId, logPacketId, logPacket);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_DELIVER_LOG_PACKET, req.getTypes(), req.getItems(), null, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during deliver logPacket !!!!!!");
            return FAILURE;
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append("0123456789abcdef".charAt((bytes[i] >> 4) & 15));
            ret.append("0123456789abcdef".charAt(bytes[i] & 15));
        }
        return ret.toString();
    }

    public int getTime(int traceId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getTime called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_GET_TIME, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getTime !!!!!!");
            return FAILURE;
        }
    }

    public int getSib16CoverageStatus(Message msg, int phoneId) {
        Log.i(LOG_TAG, "getSib16CoverageStatus called on PhoneId: " + phoneId);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, EMBMSHOOK_MSG_ID_GET_SIB16_COVERAGE, msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getSIB16 !!!!!!");
            return FAILURE;
        }
    }

    public int getEmbmsStatus(int traceId, int phoneId) {
        Log.i(LOG_TAG, "getEmbmsStatus called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) 33, req.getTypes(), req.getItems(), null, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getEmbmsStatus !!!!!!");
            return FAILURE;
        }
    }

    public int setTime(boolean sntpSuccess, long timeMseconds, long timeStamp, Message msg, int phoneId) {
        Log.i(LOG_TAG, "setTime called on PhoneId: " + phoneId);
        byte success = (byte) 0;
        if (sntpSuccess) {
            success = (byte) 1;
        }
        Log.i(LOG_TAG, "setTime success = " + success + " timeMseconds = " + timeMseconds + " timeStamp = " + timeStamp);
        SetTimeRequest req = new SetTimeRequest(success, timeMseconds, timeStamp);
        try {
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_SET_TIME, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occured during setTime !!!!!!");
            return FAILURE;
        }
    }

    public int getE911State(int traceId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getE911State called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_GET_E911_STATE, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getE911State !!!!!!");
            return FAILURE;
        }
    }

    public int contentDescription(int traceId, byte callId, byte[] tmgi, int numberOfParameter, int[] parameterCode, int[] parameterValue, Message msg, int phoneId) {
        try {
            Object parameterCode2;
            Object parameterValue2;
            Log.i(LOG_TAG, "contentDescription called on PhoneId: " + phoneId);
            if (parameterCode2 == null || parameterValue2 == null) {
                Log.i(LOG_TAG, "contentDescription: either parameterCode or parameterValue is nullparameterCode = " + parameterCode2 + " parameterValue = " + parameterValue2);
                parameterCode2 = new int[0];
                parameterValue2 = new int[0];
            }
            if (numberOfParameter == parameterCode2.length && numberOfParameter == parameterValue2.length && parameterCode2.length == parameterValue2.length) {
                int parameterArraySize = numberOfParameter * 2;
                int pointer = 0;
                int[] parameterArray = new int[parameterArraySize];
                for (int i = 0; i < parameterArraySize; i += 2) {
                    parameterArray[i] = parameterCode2[pointer];
                    parameterArray[i + 1] = parameterValue2[pointer];
                    pointer++;
                }
                Log.i(LOG_TAG, "contentDescription: parameterArray: " + Arrays.toString(parameterArray));
                ContentDescriptionReq req = new ContentDescriptionReq(traceId, callId, tmgi, parameterArray);
                this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_CONTENT_DESCRIPTION, req.getTypes(), req.getItems(), msg, phoneId);
                return 0;
            }
            Log.e(LOG_TAG, "contentDescription: Invalid input, numberOfParameter = " + numberOfParameter + " parameterCode = " + parameterCode2 + " parameterValue = " + parameterValue2);
            return FAILURE;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during contentDescription !!!!!!");
            return FAILURE;
        }
    }

    public int getPLMNListRequest(int traceId, Message msg, int phoneId) {
        Log.i(LOG_TAG, "getPLMNListRequest called on PhoneId: " + phoneId);
        try {
            BasicRequest req = new BasicRequest(traceId);
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) EMBMSHOOK_MSG_ID_GET_PLMN_LIST, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getPLMNListRequest !!!!!!");
            return FAILURE;
        }
    }

    public int getInterestedTMGIListResponse(int traceId, byte callId, byte[] tmgiList, int phoneId, Message msg) {
        try {
            GetInterestedTmgiResponse req = new GetInterestedTmgiResponse(traceId, callId, tmgiList);
            this.mQmiOemHook.sendQmiMessageAsync((short) 2, (short) 35, req.getTypes(), req.getItems(), msg, phoneId);
            return 0;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException occurred during getInterestedTMGIListResponse !!!!!!");
            return FAILURE;
        }
    }

    private byte[] parseTmgi(ByteBuffer buf) {
        int index = 0;
        byte totalTmgis = buf.get();
        byte[] tmgi = new byte[(totalTmgis * 6)];
        byte i = (byte) 0;
        while (i < totalTmgis) {
            byte tmgiLength = buf.get();
            byte j = (byte) 0;
            int index2 = index;
            while (j < tmgiLength) {
                index = index2 + 1;
                tmgi[index2] = buf.get();
                j++;
                index2 = index;
            }
            i++;
            index = index2;
        }
        return tmgi;
    }

    private byte[] parseActiveTmgi(ByteBuffer buf) {
        int index = 0;
        short totalTmgis = buf.getShort();
        byte[] tmgi = new byte[(totalTmgis * 6)];
        short i = EMBMSHOOK_MSG_ID_ENABLE;
        while (i < totalTmgis) {
            byte tmgiLength = buf.get();
            byte j = (byte) 0;
            int index2 = index;
            while (j < tmgiLength) {
                index = index2 + 1;
                tmgi[index2] = buf.get();
                j++;
                index2 = index;
            }
            i++;
            index = index2;
        }
        return tmgi;
    }

    private QmiArray<QmiByte> byteArrayToQmiArray(short vSize, byte[] arr) {
        BaseQmiItemType[] qmiByteArray = new QmiByte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            qmiByteArray[i] = new QmiByte(arr[i]);
        }
        return new QmiArray(qmiByteArray, QmiByte.class, vSize);
    }

    private QmiArray<QmiByte> tmgiListArrayToQmiArray(short vSize, byte[] tmgiList) {
        int length = tmgiList == null ? 0 : tmgiList.length;
        int numOfTmgi = length / 6;
        QmiByte[] qmiByteArray = new QmiByte[(length + (numOfTmgi * 1))];
        int index = 0;
        for (int i = 0; i < numOfTmgi; i++) {
            int index2 = index + 1;
            qmiByteArray[index] = new QmiByte(6);
            int j = i * 6;
            while (true) {
                index = index2;
                if (j >= (i + 1) * 6) {
                    break;
                }
                index2 = index + 1;
                qmiByteArray[index] = new QmiByte(tmgiList[j]);
                j++;
            }
        }
        return new QmiArray(qmiByteArray, QmiByte.class, vSize, (short) 7);
    }

    private QmiArray<QmiInteger> intArrayToQmiArray(short vSize, int[] arr) {
        int length = arr == null ? 0 : arr.length;
        BaseQmiItemType[] qmiIntArray = new QmiInteger[length];
        for (int i = 0; i < length; i++) {
            qmiIntArray[i] = new QmiInteger((long) arr[i]);
        }
        return new QmiArray(qmiIntArray, QmiInteger.class, vSize);
    }

    private QmiArray<QmiInteger> intArrayToQmiArray(short vSize, int[] arr, short numOfElements) {
        int length = arr == null ? 0 : arr.length;
        QmiInteger[] qmiIntArray = new QmiInteger[length];
        for (int i = 0; i < length; i++) {
            qmiIntArray[i] = new QmiInteger((long) arr[i]);
        }
        return new QmiArray(qmiIntArray, QmiInteger.class, vSize, numOfElements);
    }
}
