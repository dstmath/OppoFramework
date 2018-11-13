package com.qualcomm.qcrilhook;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.uicc.IccUtils;
import com.qualcomm.qcrilmsgtunnel.IQcrilMsgTunnel;
import com.qualcomm.qcrilmsgtunnel.IQcrilMsgTunnel.Stub;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class QcRilHook implements IQcRilHook {
    public static final String ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW = "com.qualcomm.intent.action.ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW";
    private static final int AVOIDANCE_BUFF_LEN = 164;
    private static final int BYTE_SIZE = 1;
    private static final boolean DBG = true;
    private static final int DEFAULT_PHONE = 0;
    private static final int INT_SIZE = 4;
    private static final String LOG_TAG = "QC_RIL_OEM_HOOK";
    private static final int MAX_PDC_ID_LEN = 124;
    private static final int MAX_REQUEST_BUFFER_SIZE = 1024;
    private static final int MAX_SPC_LEN = 6;
    public static final String QCRIL_MSG_TUNNEL_PACKAGE_NAME = "com.qualcomm.qcrilmsgtunnel";
    public static final String QCRIL_MSG_TUNNEL_SERVICE_NAME = "com.qualcomm.qcrilmsgtunnel.QcrilMsgTunnelService";
    private static final int RESPONSE_BUFFER_SIZE = 2048;
    private static final boolean VDBG = false;
    private static RegistrantList mOnServiceConnectedRegistrants = new RegistrantList();
    private static RegistrantList mRegistrants;
    private final String ENCODING;
    private boolean mBound;
    private Context mContext;
    private final int mHeaderSize;
    private BroadcastReceiver mIntentReceiver;
    private final String mOemIdentifier;
    private ITelephony mPhoneService;
    private QcRilHookCallback mQcrilHookCb;
    private ServiceConnection mQcrilMsgTunnelConnection;
    private IQcrilMsgTunnel mService;

    public QcRilHook() {
        this.mOemIdentifier = QmiOemHookConstants.OEM_IDENTIFIER;
        this.mHeaderSize = QmiOemHookConstants.OEM_IDENTIFIER.length() + 8;
        this.mService = null;
        this.mBound = false;
        this.mQcrilHookCb = null;
        this.ENCODING = "ISO-8859-1";
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(QcRilHook.ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW)) {
                    QcRilHook.this.logd("Received Broadcast Intent ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW");
                    byte[] payload = intent.getByteArrayExtra("payload");
                    int instanceId = intent.getIntExtra(QmiOemHookConstants.INSTANCE_ID, QcRilHook.DEFAULT_PHONE);
                    if (payload != null) {
                        if (payload.length < QcRilHook.this.mHeaderSize) {
                            Log.e(QcRilHook.LOG_TAG, "UNSOL_RESPONSE_OEM_HOOK_RAW incomplete header");
                            Log.e(QcRilHook.LOG_TAG, "Expected " + QcRilHook.this.mHeaderSize + " bytes. Received " + payload.length + " bytes.");
                            return;
                        }
                        ByteBuffer response = QcRilHook.createBufferWithNativeByteOrder(payload);
                        byte[] oem_id_bytes = new byte[QmiOemHookConstants.OEM_IDENTIFIER.length()];
                        response.get(oem_id_bytes);
                        String oem_id_str = new String(oem_id_bytes);
                        QcRilHook.this.logd("Oem ID in QCRILHOOK UNSOL RESP is " + oem_id_str);
                        if (oem_id_str.equals(QmiOemHookConstants.OEM_IDENTIFIER)) {
                            int remainingSize = payload.length - QmiOemHookConstants.OEM_IDENTIFIER.length();
                            if (remainingSize > 0) {
                                byte[] remainingPayload = new byte[remainingSize];
                                response.get(remainingPayload);
                                Message msg = Message.obtain();
                                msg.obj = remainingPayload;
                                msg.arg1 = instanceId;
                                QcRilHook.notifyRegistrants(new AsyncResult(null, msg, null));
                            }
                        } else {
                            Log.w(QcRilHook.LOG_TAG, "Incorrect Oem ID in QCRILHOOK UNSOL RESP. Expected QOEMHOOK. Received " + oem_id_str);
                            return;
                        }
                    }
                }
                Log.w(QcRilHook.LOG_TAG, "Received Unknown Intent: action = " + action);
            }
        };
        this.mQcrilMsgTunnelConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                QcRilHook.this.mService = Stub.asInterface(service);
                if (QcRilHook.this.mService == null) {
                    Log.e(QcRilHook.LOG_TAG, "QcrilMsgTunnelService Connect Failed (onServiceConnected)");
                } else {
                    QcRilHook.this.logd("QcrilMsgTunnelService Connected Successfully (onServiceConnected)");
                }
                QcRilHook.this.mBound = QcRilHook.DBG;
                if (QcRilHook.this.mQcrilHookCb != null) {
                    QcRilHook.this.logd("Calling onQcRilHookReady callback");
                    QcRilHook.this.mQcrilHookCb.onQcRilHookReady();
                }
                QcRilHook.notifyOnServiceConnect();
            }

            public void onServiceDisconnected(ComponentName name) {
                QcRilHook.this.logd("The connection to the service got disconnected unexpectedly!");
                QcRilHook.this.mService = null;
                QcRilHook.this.mBound = false;
                if (QcRilHook.this.mQcrilHookCb != null) {
                    QcRilHook.this.logd("Calling onQcRilHookDisconnected callback");
                    QcRilHook.this.mQcrilHookCb.onQcRilHookDisconnected();
                }
            }
        };
        this.mPhoneService = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        mRegistrants = new RegistrantList();
        if (this.mPhoneService == null) {
            Log.e(LOG_TAG, "QcRilOemHook Service Failed");
        } else {
            Log.i(LOG_TAG, "QcRilOemHook Service Created Successfully");
        }
    }

    @Deprecated
    public QcRilHook(Context context) {
        this(context, null);
    }

    public QcRilHook(Context context, QcRilHookCallback cb) {
        this.mOemIdentifier = QmiOemHookConstants.OEM_IDENTIFIER;
        this.mHeaderSize = QmiOemHookConstants.OEM_IDENTIFIER.length() + 8;
        this.mService = null;
        this.mBound = false;
        this.mQcrilHookCb = null;
        this.ENCODING = "ISO-8859-1";
        this.mIntentReceiver = /* anonymous class already generated */;
        this.mQcrilMsgTunnelConnection = /* anonymous class already generated */;
        this.mQcrilHookCb = cb;
        mRegistrants = new RegistrantList();
        this.mContext = context;
        if (this.mContext == null) {
            throw new IllegalArgumentException("Context is null");
        }
        Intent intent = new Intent();
        intent.setClassName(QCRIL_MSG_TUNNEL_PACKAGE_NAME, QCRIL_MSG_TUNNEL_SERVICE_NAME);
        logd("Starting QcrilMsgTunnel Service");
        this.mContext.startService(intent);
        logd("Attempt to bind service returned with: " + this.mContext.bindService(intent, this.mQcrilMsgTunnelConnection, 1));
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW);
            this.mContext.registerReceiver(this.mIntentReceiver, filter);
            logd("Registering for intent ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Uncaught Exception while while registering ACTION_UNSOL_RESPONSE_OEM_HOOK_RAW intent. Reason: " + e);
        }
    }

    public boolean isDisposed() {
        return this.mContext == null ? DBG : false;
    }

    private void validateInternalState() {
        if (isDisposed()) {
            throw new IllegalStateException("QcRilHook is in disposed state");
        }
    }

    public void dispose() {
        if (this.mContext != null) {
            if (this.mBound) {
                logv("dispose(): Unbinding service");
                this.mContext.unbindService(this.mQcrilMsgTunnelConnection);
                this.mQcrilHookCb.onQcRilHookDisconnected();
                this.mBound = false;
            }
            logv("dispose(): Unregistering receiver");
            this.mContext.unregisterReceiver(this.mIntentReceiver);
            this.mContext = null;
            mRegistrants = null;
            this.mQcrilHookCb = null;
        }
    }

    public static ByteBuffer createBufferWithNativeByteOrder(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(ByteOrder.nativeOrder());
        return buf;
    }

    private void addQcRilHookHeader(ByteBuffer buf, int requestId, int requestSize) {
        buf.put(QmiOemHookConstants.OEM_IDENTIFIER.getBytes());
        buf.putInt(requestId);
        buf.putInt(requestSize);
    }

    private AsyncResult sendRilOemHookMsg(int requestId, byte[] request) {
        return sendRilOemHookMsg(requestId, request, DEFAULT_PHONE);
    }

    private AsyncResult sendRilOemHookMsg(int requestId, byte[] request, int phoneId) {
        byte[] response = new byte[2048];
        logv("sendRilOemHookMsg: Outgoing Data is " + IccUtils.bytesToHexString(request));
        try {
            int retVal = this.mService.sendOemRilRequestRaw(request, response, phoneId);
            logd("sendOemRilRequestRaw returns value = " + retVal);
            if (retVal >= 0) {
                Object validResponseBytes = null;
                if (retVal > 0) {
                    validResponseBytes = new byte[retVal];
                    System.arraycopy(response, DEFAULT_PHONE, validResponseBytes, DEFAULT_PHONE, retVal);
                }
                return new AsyncResult(Integer.valueOf(retVal), validResponseBytes, null);
            }
            byte[] validResponseBytes2 = new byte[response.length];
            System.arraycopy(response, DEFAULT_PHONE, validResponseBytes2, DEFAULT_PHONE, response.length);
            return new AsyncResult(request, validResponseBytes2, CommandException.fromRilErrno(retVal * -1));
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "sendOemRilRequestRaw RequestID = " + requestId + " exception, unable to send RIL request from this application", e);
            return new AsyncResult(Integer.valueOf(requestId), null, e);
        } catch (NullPointerException e2) {
            Log.e(LOG_TAG, "NullPointerException caught at sendOemRilRequestRaw.RequestID = " + requestId + ". Return Error");
            return new AsyncResult(Integer.valueOf(requestId), null, e2);
        }
    }

    private void sendRilOemHookMsgAsync(int requestId, byte[] request, IOemHookCallback oemHookCb, int phoneId) throws NullPointerException {
        logv("sendRilOemHookMsgAsync: Outgoing Data is " + IccUtils.bytesToHexString(request));
        try {
            this.mService.sendOemRilRequestRawAsync(request, oemHookCb, phoneId);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "sendOemRilRequestRawAsync RequestID = " + requestId + " exception, unable to send RIL request from this application", e);
        } catch (NullPointerException e2) {
            Log.e(LOG_TAG, "NullPointerException caught at sendOemRilRequestRawAsync.RequestID = " + requestId + ". Throw to the caller");
            throw e2;
        }
    }

    public boolean getLpluslSupportStatus() {
        boolean status = false;
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_REQ_HOOK_GET_L_PLUS_L_FEATURE_SUPPORT_STATUS_REQ);
        if (ar.exception == null && ar.result != null) {
            status = (ByteBuffer.wrap((byte[]) ar.result).get() & 1) == 1 ? DBG : false;
        }
        logd("getLpluslSupportStatus: " + status + " exception: " + ar.exception);
        return status;
    }

    public String qcRilGetConfig(int phoneId, int mbnType) {
        validateInternalState();
        byte[] payload = new byte[((this.mHeaderSize + 4) + 1)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_GET_CONFIG, 5);
        reqBuffer.put((byte) phoneId);
        reqBuffer.putInt(mbnType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_CONFIG, payload);
        if (ar.exception != null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_CONFIG failed w/ " + ar.exception);
            return null;
        } else if (ar.result == null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_CONFIG failed w/ null result");
            return null;
        } else {
            try {
                String result = new String((byte[]) ar.result, "ISO-8859-1");
                logv("QCRIL_EVT_HOOK_GET_CONFIG returned w/ " + result);
                return result;
            } catch (UnsupportedEncodingException e) {
                logd("unsupport ISO-8859-1");
                return null;
            }
        }
    }

    public String qcRilGetConfig() {
        return qcRilGetConfig(DEFAULT_PHONE);
    }

    public String qcRilGetConfig(int phoneId) {
        return qcRilGetConfig(phoneId, DEFAULT_PHONE);
    }

    public boolean qcRilSetConfig(String file, String config, int subMask) {
        return qcRilSetConfig(file, config, subMask, DEFAULT_PHONE);
    }

    public boolean qcRilSetConfig(String file, String config, int subMask, int mbnType) {
        validateInternalState();
        if (config.isEmpty() || config.length() > MAX_PDC_ID_LEN || (file.isEmpty() ^ 1) == 0) {
            Log.e(LOG_TAG, "set with incorrect config id: " + config);
            return false;
        }
        byte[] payload = new byte[((((this.mHeaderSize + 3) + 4) + file.length()) + config.length())];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_SET_CONFIG, (file.length() + 7) + config.length());
        buf.put((byte) subMask);
        buf.putInt(mbnType);
        buf.put(file.getBytes());
        buf.put((byte) 0);
        try {
            buf.put(config.getBytes("ISO-8859-1"));
            buf.put((byte) 0);
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_CONFIG, payload);
            if (ar.exception == null) {
                return DBG;
            }
            Log.e(LOG_TAG, "QCRIL_EVT_HOOK_SET_CONFIG failed w/ " + ar.exception);
            return false;
        } catch (UnsupportedEncodingException e) {
            logd("unsupport ISO-8859-1");
            return false;
        }
    }

    public boolean qcRilSetConfig(String file) {
        return qcRilSetConfig(file, file, 1);
    }

    public boolean qcRilSetConfig(String file, int subMask) {
        return qcRilSetConfig(file, file, subMask);
    }

    public byte[] qcRilGetQcVersionOfFile(String file) {
        validateInternalState();
        if (file.isEmpty()) {
            return null;
        }
        byte[] payload = new byte[(this.mHeaderSize + file.getBytes().length)];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE, file.getBytes().length);
        buf.put(file.getBytes());
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE, payload);
        if (ar.exception != null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE failed w/ " + ar.exception);
            return null;
        } else if (ar.result == null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE failed w/ null result");
            return null;
        } else {
            logv("QCRIL_EVT_HOOK_GET_QC_VERSION_OF_FILE returned w/ " + ((byte[]) ar.result));
            return (byte[]) ar.result;
        }
    }

    public byte[] qcRilGetOemVersionOfFile(String file) {
        validateInternalState();
        if (file.isEmpty()) {
            return null;
        }
        byte[] payload = new byte[(this.mHeaderSize + file.getBytes().length)];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE, file.getBytes().length);
        buf.put(file.getBytes());
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE, payload);
        if (ar.exception != null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE failed w/ " + ar.exception);
            return null;
        } else if (ar.result == null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE failed w/ null result");
            return null;
        } else {
            logv("QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_FILE returned w/ " + ((byte[]) ar.result));
            return (byte[]) ar.result;
        }
    }

    public byte[] qcRilGetQcVersionOfID(String configId) {
        validateInternalState();
        if (configId.isEmpty() || configId.length() > MAX_PDC_ID_LEN) {
            Log.w(LOG_TAG, "invalid config id");
            return null;
        }
        byte[] payload = new byte[(this.mHeaderSize + configId.length())];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID, configId.length());
        try {
            buf.put(configId.getBytes("ISO-8859-1"));
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID, payload);
            if (ar.exception != null) {
                Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID failed w/ " + ar.exception);
                return null;
            } else if (ar.result == null) {
                Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID failed w/ null result");
                return null;
            } else {
                logv("QCRIL_EVT_HOOK_GET_QC_VERSION_OF_ID returned w/ " + ((byte[]) ar.result));
                return (byte[]) ar.result;
            }
        } catch (UnsupportedEncodingException e) {
            logd("unsupport ISO-8859-1");
            return null;
        }
    }

    public byte[] qcRilGetOemVersionOfID(String config_id) {
        validateInternalState();
        if (config_id.isEmpty() || config_id.length() > MAX_PDC_ID_LEN) {
            Log.w(LOG_TAG, "invalid config_id");
            return null;
        }
        byte[] payload = new byte[(this.mHeaderSize + config_id.length())];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID, config_id.length());
        try {
            buf.put(config_id.getBytes("ISO-8859-1"));
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID, payload);
            if (ar.exception != null) {
                Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID failed w/ " + ar.exception);
                return null;
            } else if (ar.result == null) {
                Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID failed w/ null result");
                return null;
            } else {
                logv("QCRIL_EVT_HOOK_GET_OEM_VERSION_OF_ID returned w/ " + ((byte[]) ar.result));
                return (byte[]) ar.result;
            }
        } catch (UnsupportedEncodingException e) {
            logd("unsupport ISO-8859-1");
            return null;
        }
    }

    public boolean qcRilActivateConfig(int phoneId) {
        return qcRilActivateConfig(phoneId, DEFAULT_PHONE);
    }

    public boolean qcRilActivateConfig(int phoneId, int mbnType) {
        validateInternalState();
        byte[] payload = new byte[((this.mHeaderSize + 4) + 1)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_ACT_CONFIGS, 5);
        reqBuffer.put((byte) phoneId);
        reqBuffer.putInt(mbnType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_ACT_CONFIGS, payload);
        if (ar.exception == null) {
            return DBG;
        }
        Log.w(LOG_TAG, "QCRIL_EVT_HOOK_ACT_CONFIGS failed w/ " + ar.exception);
        return false;
    }

    public boolean qcRilValidateConfig(String configId, int phoneId) {
        validateInternalState();
        if (configId.isEmpty() || configId.length() > MAX_PDC_ID_LEN) {
            Log.w(LOG_TAG, "invalid config id");
            return false;
        }
        byte[] payload = new byte[((this.mHeaderSize + 2) + configId.length())];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_VALIDATE_CONFIG, configId.length() + 2);
        buf.put((byte) phoneId);
        try {
            buf.put(configId.getBytes("ISO-8859-1"));
            buf.put((byte) 0);
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_META_INFO, payload);
            if (ar.exception == null) {
                return DBG;
            }
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_VALIDATE_CONFIG failed w/ " + ar.exception);
            return false;
        } catch (UnsupportedEncodingException e) {
            logd("unsupport ISO-8859-1");
            return false;
        }
    }

    @Deprecated
    public String[] qcRilGetAvailableConfigs(String device) {
        Log.w(LOG_TAG, "qcRilGetAvailableConfigs is deprecated");
        return null;
    }

    public boolean qcRilGetAllConfigs() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS);
        if (ar.exception == null) {
            return DBG;
        }
        Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_AVAILABLE_CONFIGS failed w/ " + ar.exception);
        return false;
    }

    public boolean qcRilCleanupConfigs() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_DELETE_ALL_CONFIGS);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL_EVT_HOOK_DELETE_ALL_CONFIGS failed w/ " + ar.exception);
        return false;
    }

    public boolean qcRilDeactivateConfigs() {
        return qcRilDeactivateConfigs(DEFAULT_PHONE);
    }

    public boolean qcRilDeactivateConfigs(int mbnType) {
        validateInternalState();
        byte[] payload = new byte[(this.mHeaderSize + 4)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(payload);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_DEACT_CONFIGS, 4);
        reqBuffer.putInt(mbnType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_DEACT_CONFIGS, payload);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL_EVT_HOOK_DEACT_CONFIGS failed w/ " + ar.exception);
        return false;
    }

    public boolean qcRilSelectConfig(String config, int subMask) {
        return qcRilSelectConfig(config, subMask, DEFAULT_PHONE);
    }

    public boolean qcRilSelectConfig(String config, int subMask, int mbnType) {
        validateInternalState();
        if (config.isEmpty() || config.length() > MAX_PDC_ID_LEN) {
            Log.e(LOG_TAG, "select with incorrect config id: " + config);
            return false;
        }
        try {
            byte[] payload = new byte[(((this.mHeaderSize + 1) + 4) + config.getBytes("ISO-8859-1").length)];
            ByteBuffer buf = createBufferWithNativeByteOrder(payload);
            addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_SEL_CONFIG, config.getBytes("ISO-8859-1").length + 5);
            buf.put((byte) subMask);
            buf.putInt(mbnType);
            buf.put(config.getBytes("ISO-8859-1"));
            AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SEL_CONFIG, payload);
            if (ar.exception == null) {
                return DBG;
            }
            Log.e(LOG_TAG, "QCRIL_EVT_HOOK_SEL_CONFIG failed w/ " + ar.exception);
            return false;
        } catch (UnsupportedEncodingException e) {
            logd("unsupport ISO-8859-1");
            return false;
        }
    }

    public String qcRilGetMetaInfoForConfig(String config) {
        return qcRilGetMetaInfoForConfig(config, DEFAULT_PHONE);
    }

    public String qcRilGetMetaInfoForConfig(String config, int mbnType) {
        validateInternalState();
        String result = null;
        if (config.isEmpty() || config.length() > MAX_PDC_ID_LEN) {
            Log.e(LOG_TAG, "get meta info with incorrect config id: " + config);
        } else {
            try {
                byte[] payload = new byte[((this.mHeaderSize + 4) + config.getBytes("ISO-8859-1").length)];
                ByteBuffer buf = createBufferWithNativeByteOrder(payload);
                addQcRilHookHeader(buf, IQcRilHook.QCRIL_EVT_HOOK_GET_META_INFO, config.getBytes("ISO-8859-1").length + 4);
                buf.putInt(mbnType);
                buf.put(config.getBytes("ISO-8859-1"));
                AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_META_INFO, payload);
                if (ar.exception != null) {
                    Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_META_INFO failed w/ " + ar.exception);
                    return null;
                } else if (ar.result == null) {
                    Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_META_INFO failed w/ null result");
                    return null;
                } else {
                    try {
                        result = new String((byte[]) ar.result, "ISO-8859-1");
                        logv("QCRIL_EVT_HOOK_GET_META_INFO returned w/ " + result);
                    } catch (UnsupportedEncodingException e) {
                        logd("unsupport ISO-8859-1");
                        return null;
                    }
                }
            } catch (UnsupportedEncodingException e2) {
                logd("unsupport ISO-8859-1");
                return null;
            }
        }
        return result;
    }

    public boolean qcRilGoDormant(String interfaceName) {
        AsyncResult result = sendQcRilHookMsg((int) IQcRilHook.QCRILHOOK_GO_DORMANT, interfaceName);
        if (result.exception == null) {
            return DBG;
        }
        Log.w(LOG_TAG, "Go Dormant Command returned Exception: " + result.exception);
        return false;
    }

    public int qcRilGetPrioritySub() {
        AsyncResult result = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_PAGING_PRIORITY);
        if (result.exception != null) {
            Log.e(LOG_TAG, "QCRIL  get priority sub Command returned Exception: " + result.exception);
            return DEFAULT_PHONE;
        } else if (result.result != null) {
            int retval = ByteBuffer.wrap(result.result).get();
            Log.v(LOG_TAG, "qcRilGetPrioritySub: priority subscription is :: " + retval);
            return retval;
        } else {
            Log.e(LOG_TAG, "QCRIL get priority sub Command returned null response ");
            return DEFAULT_PHONE;
        }
    }

    public boolean qcRilSetPrioritySub(int priorityIndex) {
        byte payload = (byte) priorityIndex;
        Log.v(LOG_TAG, "qcRilSetPrioritySub: Outgoing priority subscription is:" + priorityIndex);
        AsyncResult result = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_PAGING_PRIORITY, payload);
        if (result.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL set priority sub Command returned Exception: " + result.exception);
        return false;
    }

    public boolean qcRilSetCdmaSubSrcWithSpc(int cdmaSubscription, String spc) {
        validateInternalState();
        logv("qcRilSetCdmaSubSrcWithSpc: Set Cdma Subscription to " + cdmaSubscription);
        if (spc.isEmpty() || spc.length() > 6) {
            Log.e(LOG_TAG, "QCRIL Set Cdma Subscription Source Command incorrect SPC: " + spc);
            return false;
        }
        byte[] payload = new byte[(spc.length() + 1)];
        ByteBuffer buf = createBufferWithNativeByteOrder(payload);
        buf.put((byte) cdmaSubscription);
        buf.put(spc.getBytes());
        AsyncResult ar = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_CDMA_SUB_SRC_WITH_SPC, payload);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "QCRIL Set Cdma Subscription Source Command returned Exception: " + ar.exception);
            return false;
        } else if (ar.result == null) {
            return false;
        } else {
            byte succeed = ByteBuffer.wrap(ar.result).get();
            logv("QCRIL Set Cdma Subscription Source Command " + (succeed == (byte) 1 ? "Succeed." : "Failed."));
            if (succeed == (byte) 1) {
                return DBG;
            }
            return false;
        }
    }

    public byte[] qcRilSendProtocolBufferMessage(byte[] protocolBuffer, int phoneId) {
        logv("qcRilSendProtoBufMessage: protocolBuffer" + protocolBuffer.toString());
        AsyncResult ar = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_PROTOBUF_MSG, protocolBuffer, phoneId);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "qcRilSendProtoBufMessage: Exception " + ar.exception);
            return null;
        } else if (ar.result != null) {
            return (byte[]) ar.result;
        } else {
            Log.e(LOG_TAG, "QCRIL_EVT_HOOK_PROTOBUF_MSG returned null");
            return null;
        }
    }

    public boolean qcRilSetTuneAway(boolean tuneAway) {
        logd("qcRilSetTuneAway: tuneAway Value to be set to " + tuneAway);
        byte payload = (byte) 0;
        if (tuneAway) {
            payload = (byte) 1;
        }
        logv("qcRilSetTuneAway: tuneAway payload " + payload);
        AsyncResult ar = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_TUNEAWAY, payload);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "qcRilSetTuneAway: Exception " + ar.exception);
        return false;
    }

    public boolean qcRilGetTuneAway() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_TUNEAWAY);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "qcRilGetTuneAway: Exception " + ar.exception);
            return false;
        } else if (ar.result != null) {
            byte tuneAwayValue = ByteBuffer.wrap(ar.result).get();
            logd("qcRilGetTuneAway: tuneAwayValue " + tuneAwayValue);
            if (tuneAwayValue == (byte) 1) {
                return DBG;
            }
            return false;
        } else {
            Log.e(LOG_TAG, "qcRilGetTuneAway: Null Response");
            return false;
        }
    }

    public boolean qcRilSetPrioritySubscription(int priorityIndex) {
        logv("qcRilSetPrioritySubscription: PrioritySubscription to be set to" + priorityIndex);
        byte payload = (byte) priorityIndex;
        logv("qcRilSetPrioritySubscription: PrioritySubscription payload " + payload);
        AsyncResult ar = sendQcRilHookMsg((int) IQcRilHook.QCRIL_EVT_HOOK_SET_PAGING_PRIORITY, payload);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "qcRilSetPrioritySubscription: Exception " + ar.exception);
        return false;
    }

    public int qcRilGetCsgId() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_CSG_ID);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "qcRilGetCsgId: Exception " + ar.exception);
            return -1;
        } else if (ar.result != null) {
            int csgId = ByteBuffer.wrap(ar.result).get();
            logd("qcRilGetCsgId: csg Id " + csgId);
            return csgId;
        } else {
            Log.e(LOG_TAG, "qcRilGetCsgId: Null Response");
            return -1;
        }
    }

    public int qcRilGetPrioritySubscription() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_PAGING_PRIORITY);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "qcRilGetPrioritySubscription: Exception " + ar.exception);
            return DEFAULT_PHONE;
        } else if (ar.result != null) {
            int subscriptionIndex = ByteBuffer.wrap(ar.result).get();
            logv("qcRilGetPrioritySubscription: subscriptionIndex " + subscriptionIndex);
            return subscriptionIndex;
        } else {
            Log.e(LOG_TAG, "qcRilGetPrioritySubscription: Null Response");
            return DEFAULT_PHONE;
        }
    }

    public boolean qcRilInformShutDown(int phoneId) {
        logd("QCRIL Inform shutdown for phoneId " + phoneId);
        sendQcRilHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_INFORM_SHUTDOWN, null, new OemHookCallback(null) {
            public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
                QcRilHook.this.logd("QCRIL Inform shutdown DONE!");
            }
        }, phoneId);
        return DBG;
    }

    public boolean qcRilCdmaAvoidCurNwk() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_CDMA_AVOID_CUR_NWK);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL Avoid the current cdma network Command returned Exception: " + ar.exception);
        return false;
    }

    public boolean qcRilSetFieldTestMode(int phoneId, byte ratType, int enable) {
        validateInternalState();
        byte[] request = new byte[(this.mHeaderSize + 8)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_ENABLE_ENGINEER_MODE, DEFAULT_PHONE);
        reqBuffer.putInt(ratType);
        reqBuffer.putInt(enable);
        logd("enable = " + enable + "ratType =" + ratType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_ENABLE_ENGINEER_MODE, request, phoneId);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL enable engineer mode cmd returned exception: " + ar.exception);
        return false;
    }

    public boolean qcRilCdmaClearAvoidanceList() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_CDMA_CLEAR_AVOIDANCE_LIST);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL Clear the cdma avoidance list Command returned Exception: " + ar.exception);
        return false;
    }

    public byte[] qcRilCdmaGetAvoidanceList() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_CDMA_GET_AVOIDANCE_LIST);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "QCRIL Get the cdma avoidance list Command returned Exception: " + ar.exception);
            return null;
        } else if (ar.result != null) {
            byte[] result = ar.result;
            if (result.length == AVOIDANCE_BUFF_LEN) {
                return result;
            }
            Log.e(LOG_TAG, "QCRIL Get unexpected cdma avoidance list buffer length: " + result.length);
            return null;
        } else {
            Log.e(LOG_TAG, "QCRIL Get cdma avoidance list command returned a null result.");
            return null;
        }
    }

    public boolean qcRilPerformIncrManualScan(int phoneId) {
        validateInternalState();
        byte[] request = new byte[this.mHeaderSize];
        addQcRilHookHeader(createBufferWithNativeByteOrder(request), IQcRilHook.QCRIL_EVT_HOOK_PERFORM_INCREMENTAL_NW_SCAN, phoneId);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_PERFORM_INCREMENTAL_NW_SCAN, request, phoneId);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL perform incr manual scan returned exception " + ar.exception);
        return false;
    }

    public boolean qcRilAbortNetworkScan(int phoneId) {
        validateInternalState();
        byte[] request = new byte[this.mHeaderSize];
        addQcRilHookHeader(createBufferWithNativeByteOrder(request), IQcRilHook.QCRIL_EVT_HOOK_ABORT_NW_SCAN, phoneId);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_ABORT_NW_SCAN, request, phoneId);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL cancel ongoing nw scan returned exception " + ar.exception);
        return false;
    }

    public boolean qcrilSetBuiltInPLMNList(byte[] payload, int phoneId) {
        validateInternalState();
        boolean retval = false;
        if (payload == null) {
            Log.e(LOG_TAG, "payload is null");
            return false;
        }
        byte[] request = new byte[(this.mHeaderSize + payload.length)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_BUILTIN_PLMN_LIST, payload.length);
        reqBuffer.put(payload);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_BUILTIN_PLMN_LIST, request, phoneId);
        if (ar.exception == null) {
            retval = DBG;
        } else {
            Log.e(LOG_TAG, "QCRIL set builtin PLMN list returned exception: " + ar.exception);
        }
        return retval;
    }

    public boolean qcRilSetPreferredNetworkAcqOrder(int acqOrder, int phoneId) {
        validateInternalState();
        byte[] request = new byte[(this.mHeaderSize + 4)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        logd("acq order: " + acqOrder);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_ACQ_ORDER, 4);
        reqBuffer.putInt(acqOrder);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_ACQ_ORDER, request, phoneId);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL set acq order cmd returned exception: " + ar.exception);
        return false;
    }

    public byte qcRilGetPreferredNetworkAcqOrder(int phoneId) {
        validateInternalState();
        byte[] request = new byte[this.mHeaderSize];
        addQcRilHookHeader(createBufferWithNativeByteOrder(request), IQcRilHook.QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_ACQ_ORDER, 4);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_ACQ_ORDER, request, phoneId);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "QCRIL set acq order cmd returned exception: " + ar.exception);
            return (byte) 0;
        } else if (ar.result != null) {
            byte acq_order = ByteBuffer.wrap(ar.result).get();
            logd("acq order is " + acq_order);
            return acq_order;
        } else {
            Log.e(LOG_TAG, "no acq order result return");
            return (byte) 0;
        }
    }

    public boolean qcRilSetLteTuneaway(boolean enable, int phoneId) {
        validateInternalState();
        byte tuneaway = enable ? (byte) 1 : (byte) 0;
        byte[] request = new byte[(this.mHeaderSize + 1)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        logd("qcRilSetLteTuneaway enable :" + enable);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_LTE_TUNE_AWAY, 1);
        reqBuffer.put(tuneaway);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_LTE_TUNE_AWAY, request, phoneId);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL set lte tune away returned exception: " + ar.exception);
        return false;
    }

    public void qcRilSendDataEnableStatus(int enable, int phoneId) {
        validateInternalState();
        OemHookCallback oemHookCb = new OemHookCallback(null) {
            public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
                QcRilHook.this.logd("QCRIL send data enable status DONE!");
            }
        };
        byte[] request = new byte[(this.mHeaderSize + 4)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_IS_DATA_ENABLED, 4);
        reqBuffer.putInt(enable);
        sendRilOemHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_SET_IS_DATA_ENABLED, request, oemHookCb, phoneId);
    }

    public void qcRilSendDataRoamingEnableStatus(int enable, int phoneId) {
        validateInternalState();
        OemHookCallback oemHookCb = new OemHookCallback(null) {
            public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
                QcRilHook.this.logd("QCRIL send data roaming enable status DONE!");
            }
        };
        byte[] request = new byte[(this.mHeaderSize + 4)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_IS_DATA_ROAMING_ENABLED, 4);
        reqBuffer.putInt(enable);
        sendRilOemHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_SET_IS_DATA_ROAMING_ENABLED, request, oemHookCb, phoneId);
    }

    public void qcRilSendApnInfo(String type, String apn, int isValid, int phoneId) {
        validateInternalState();
        OemHookCallback oemHookCb = new OemHookCallback(null) {
            public void onOemHookResponse(byte[] response, int phoneId) throws RemoteException {
                QcRilHook.this.logd("QCRIL send apn info DONE!");
            }
        };
        int payloadSize = ((type.getBytes().length + 12) + apn.getBytes().length) + 2;
        if (payloadSize > MAX_REQUEST_BUFFER_SIZE) {
            Log.e(LOG_TAG, "APN sent is larger than maximum buffer. Bail out");
            return;
        }
        byte[] request = new byte[(this.mHeaderSize + payloadSize)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_APN_INFO, payloadSize);
        reqBuffer.putInt(type.getBytes().length + 1);
        reqBuffer.put(type.getBytes());
        reqBuffer.put((byte) 0);
        reqBuffer.putInt(apn.getBytes().length + 1);
        reqBuffer.put(apn.getBytes());
        reqBuffer.put((byte) 0);
        reqBuffer.putInt(isValid);
        sendRilOemHookMsgAsync(IQcRilHook.QCRIL_EVT_HOOK_SET_APN_INFO, request, oemHookCb, phoneId);
    }

    public boolean qcRilSendDDSInfo(int dds, int reason, int phoneId) {
        validateInternalState();
        byte[] request = new byte[(this.mHeaderSize + 8)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        logd("dds phoneId: " + dds + " reason: " + reason);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_DATA_SUBSCRIPTION, 8);
        reqBuffer.putInt(dds);
        reqBuffer.putInt(reason);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_DATA_SUBSCRIPTION, request, phoneId);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL send dds sub info returned exception: " + ar.exception);
        return false;
    }

    public boolean setLocalCallHold(int phoneId, boolean enable) {
        validateInternalState();
        byte lchStatus = enable ? (byte) 1 : (byte) 0;
        byte[] request = new byte[(this.mHeaderSize + 1)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        logd("setLocalCallHold: " + phoneId + " " + enable);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_SET_LOCAL_CALL_HOLD, 1);
        reqBuffer.put(lchStatus);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_SET_LOCAL_CALL_HOLD, request, phoneId);
        if (ar.exception == null) {
            logd("setLocalCallHold success");
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL setLocalCallHold returned exception: " + ar.exception);
        return false;
    }

    public boolean qcRilSetPreferredNetworkBandPref(int bandPref, int phoneId) {
        validateInternalState();
        byte[] request = new byte[(this.mHeaderSize + 4)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        logd("band pref: " + bandPref);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_BAND_PREF, 4);
        reqBuffer.putInt(bandPref);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_PREFERRED_NETWORK_BAND_PREF, request, phoneId);
        if (ar.exception == null) {
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL set band pref cmd returned exception: " + ar.exception);
        return false;
    }

    public byte qcRilGetPreferredNetworkBandPref(int bandType, int phoneId) {
        validateInternalState();
        byte[] request = new byte[this.mHeaderSize];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_BAND_PREF, 4);
        reqBuffer.putInt(bandType);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_PREFERRED_NETWORK_BAND_PREF, request, phoneId);
        if (ar.exception != null) {
            Log.e(LOG_TAG, "QCRIL get band perf cmd returned exception: " + ar.exception);
            return (byte) 0;
        } else if (ar.result != null) {
            byte band_pref = ByteBuffer.wrap(ar.result).get();
            logd("band pref is " + band_pref);
            return band_pref;
        } else {
            Log.e(LOG_TAG, "no band pref result return");
            return (byte) 0;
        }
    }

    public byte[] qcRilGetSlotStatus() {
        AsyncResult ar = sendQcRilHookMsg(IQcRilHook.QCRIL_EVT_HOOK_GET_SLOTS_STATUS_REQ);
        if (ar.exception != null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_SLOTS_STATUS_REQ failed w/ " + ar.exception);
            return null;
        } else if (ar.result == null) {
            Log.w(LOG_TAG, "QCRIL_EVT_HOOK_GET_SLOTS_STATUS_REQ failed w/ null result");
            return null;
        } else {
            logv("QCRIL_EVT_HOOK_GET_SLOTS_STATUS_REQ returned w/ " + ((byte[]) ar.result));
            return (byte[]) ar.result;
        }
    }

    public AsyncResult sendQcRilHookMsg(int requestId) {
        validateInternalState();
        byte[] request = new byte[this.mHeaderSize];
        addQcRilHookHeader(createBufferWithNativeByteOrder(request), requestId, DEFAULT_PHONE);
        return sendRilOemHookMsg(requestId, request);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, byte payload) {
        return sendQcRilHookMsg(requestId, payload, (int) DEFAULT_PHONE);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, byte payload, int phoneId) {
        validateInternalState();
        byte[] request = new byte[(this.mHeaderSize + 1)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, 1);
        reqBuffer.put(payload);
        return sendRilOemHookMsg(requestId, request, phoneId);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, byte[] payload) {
        return sendQcRilHookMsg(requestId, payload, (int) DEFAULT_PHONE);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, byte[] payload, int phoneId) {
        validateInternalState();
        byte[] request = new byte[(this.mHeaderSize + payload.length)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, payload.length);
        reqBuffer.put(payload);
        return sendRilOemHookMsg(requestId, request, phoneId);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, int payload) {
        validateInternalState();
        byte[] request = new byte[(this.mHeaderSize + 4)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, 4);
        reqBuffer.putInt(payload);
        return sendRilOemHookMsg(requestId, request);
    }

    public AsyncResult sendQcRilHookMsg(int requestId, String payload) {
        validateInternalState();
        byte[] request = new byte[(this.mHeaderSize + payload.length())];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, payload.length());
        reqBuffer.put(payload.getBytes());
        return sendRilOemHookMsg(requestId, request);
    }

    public void sendQcRilHookMsgAsync(int requestId, byte[] payload, OemHookCallback oemHookCb) {
        sendQcRilHookMsgAsync(requestId, payload, oemHookCb, DEFAULT_PHONE);
    }

    public void sendQcRilHookMsgAsync(int requestId, byte[] payload, OemHookCallback oemHookCb, int phoneId) {
        validateInternalState();
        int payloadLength = DEFAULT_PHONE;
        if (payload != null) {
            payloadLength = payload.length;
        }
        byte[] request = new byte[(this.mHeaderSize + payloadLength)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        addQcRilHookHeader(reqBuffer, requestId, payloadLength);
        if (payload != null) {
            reqBuffer.put(payload);
        }
        sendRilOemHookMsgAsync(requestId, request, oemHookCb, phoneId);
    }

    public static void register(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (mRegistrants) {
            mRegistrants.add(r);
        }
    }

    public static void unregister(Handler h) {
        synchronized (mRegistrants) {
            mRegistrants.remove(h);
        }
    }

    public void registerForFieldTestData(Handler h, int what, Object obj) {
    }

    public void unregisterForFieldTestData(Handler h) {
    }

    public void registerForExtendedDbmIntl(Handler h, int what, Object obj) {
    }

    public void unregisterForExtendedDbmIntl(Handler h) {
    }

    protected void finalize() {
        logv("is destroyed");
    }

    public static void notifyRegistrants(AsyncResult ar) {
        if (mRegistrants != null) {
            mRegistrants.notifyRegistrants(ar);
        } else {
            Log.e(LOG_TAG, "QcRilOemHook notifyRegistrants Failed");
        }
    }

    private void logd(String str) {
        Log.d(LOG_TAG, str);
    }

    private void logv(String str) {
    }

    public static void registerOnServiceConnected(Handler h, int what, Object obj) {
        Log.e(LOG_TAG, "registerOnServiceConnected, H:" + h + "what:" + what);
        mOnServiceConnectedRegistrants.add(new Registrant(h, what, obj));
    }

    public static void unregisterOnServiceConnected(Handler h) {
        Log.e(LOG_TAG, "unregisterOnServiceConnected, H:" + h);
        mOnServiceConnectedRegistrants.remove(h);
    }

    public static synchronized void notifyOnServiceConnect() {
        synchronized (QcRilHook.class) {
            Log.e(LOG_TAG, "Notifying registrants: OnServiceConnect");
            mOnServiceConnectedRegistrants.notifyRegistrants();
        }
    }

    public void oppo_dispose() {
        Log.e(LOG_TAG, "[oppo] dispose of QcRilHook.");
        dispose();
    }

    public boolean setQcilLogOn(int phoneId, boolean enable) {
        byte logon = enable ? (byte) 1 : (byte) 0;
        byte[] request = new byte[(this.mHeaderSize + 1)];
        ByteBuffer reqBuffer = createBufferWithNativeByteOrder(request);
        Log.d(LOG_TAG, "setQcilLogOn: " + phoneId + " " + enable);
        addQcRilHookHeader(reqBuffer, IQcRilHook.QCRIL_EVT_HOOK_SET_QCRILLOG_ON, 1);
        reqBuffer.put(logon);
        AsyncResult ar = sendRilOemHookMsg(IQcRilHook.QCRIL_EVT_HOOK_SET_QCRILLOG_ON, request, phoneId);
        if (ar.exception == null) {
            Log.d(LOG_TAG, "setQcilLogOn success");
            return DBG;
        }
        Log.e(LOG_TAG, "QCRIL setQcilLogOn returned exception: " + ar.exception);
        return false;
    }
}
