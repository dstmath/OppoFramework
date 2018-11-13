package javax.obex;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ServerSession extends ObexSession implements Runnable {
    private static final String TAG = "Obex ServerSession";
    private static final boolean V = ObexHelper.VDBG;
    private boolean mClosed;
    private InputStream mInput;
    private ServerRequestHandler mListener;
    private int mMaxPacketLength;
    private OutputStream mOutput;
    private Thread mProcessThread;
    private ObexTransport mTransport;
    private boolean setMTU = false;
    private boolean updateMtu = false;
    private int updatedMtuSize = 0;

    public ServerSession(ObexTransport trans, ServerRequestHandler handler, Authenticator auth) throws IOException {
        this.mAuthenticator = auth;
        this.mTransport = trans;
        this.mInput = this.mTransport.openInputStream();
        this.mOutput = this.mTransport.openOutputStream();
        this.mListener = handler;
        this.mMaxPacketLength = 256;
        this.mClosed = false;
        this.mProcessThread = new Thread(this);
        this.mProcessThread.start();
    }

    public void setMaxPacketSize(int size) {
        if (V) {
            Log.v(TAG, "setMaxPacketSize" + size);
        }
        this.mMaxPacketLength = size;
    }

    public int getMaxPacketSize() {
        return this.mMaxPacketLength;
    }

    public void reduceMTU(boolean enable) {
        this.setMTU = enable;
    }

    public void updateMTU(int mtuSize) {
        this.updateMtu = true;
        this.updatedMtuSize = mtuSize;
        Log.i(TAG, "updateMTU: " + mtuSize);
    }

    public void run() {
        boolean done = false;
        while (!done) {
            try {
                if ((this.mClosed ^ 1) != 0) {
                    if (V) {
                        Log.v(TAG, "Waiting for incoming request...");
                    }
                    int requestType = this.mInput.read();
                    if (V) {
                        Log.v(TAG, "Read request: " + requestType);
                    }
                    switch (requestType) {
                        case -1:
                            Log.d(TAG, "Read request returned -1, exiting from loop");
                            done = true;
                            break;
                        case 2:
                        case ObexHelper.OBEX_OPCODE_PUT_FINAL /*130*/:
                            handlePutRequest(requestType);
                            break;
                        case 3:
                        case ObexHelper.OBEX_OPCODE_GET_FINAL /*131*/:
                            handleGetRequest(requestType);
                            break;
                        case 128:
                            handleConnectRequest();
                            break;
                        case ObexHelper.OBEX_OPCODE_DISCONNECT /*129*/:
                            handleDisconnectRequest();
                            break;
                        case ObexHelper.OBEX_OPCODE_SETPATH /*133*/:
                            handleSetPathRequest();
                            break;
                        case 255:
                            handleAbortRequest();
                            break;
                        default:
                            int length = (this.mInput.read() << 8) + this.mInput.read();
                            for (int i = 3; i < length; i++) {
                                this.mInput.read();
                            }
                            sendResponse(ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED, null);
                            break;
                    }
                }
                close();
            } catch (NullPointerException e) {
                Log.d(TAG, "Exception occured - ignoring", e);
            } catch (Exception e2) {
                Log.d(TAG, "Exception occured - ignoring", e2);
            }
        }
        close();
    }

    private void handleAbortRequest() throws IOException {
        int code;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int length = (this.mInput.read() << 8) + this.mInput.read();
        if (length > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
        } else {
            for (int i = 3; i < length; i++) {
                this.mInput.read();
            }
            code = this.mListener.onAbort(request, reply);
            Log.v(TAG, "onAbort request handler return value- " + code);
            code = validateResponseCode(code);
        }
        sendResponse(code, null);
    }

    private void handlePutRequest(int type) throws IOException {
        if (V) {
            Log.v(TAG, "handlePutRequest");
        }
        ServerOperation op = new ServerOperation(this, this.mInput, type, this.mMaxPacketLength, this.mListener);
        try {
            int response;
            if (!op.finalBitSet || (op.isValidBody() ^ 1) == 0) {
                response = validateResponseCode(this.mListener.onPut(op));
            } else {
                response = validateResponseCode(this.mListener.onDelete(op.requestHeader, op.replyHeader));
            }
            if (response != ResponseCodes.OBEX_HTTP_OK && (op.isAborted ^ 1) != 0) {
                if (V) {
                    Log.v(TAG, "handlePutRequest pre != HTTP_OK sendReply");
                }
                op.sendReply(response);
            } else if (!op.isAborted) {
                while (!op.finalBitSet) {
                    if (V) {
                        Log.v(TAG, "handlePutRequest pre looped sendReply");
                    }
                    op.sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                }
                op.sendReply(response);
            }
        } catch (Exception e) {
            if (V) {
                Log.d(TAG, "Exception occured - sending OBEX_HTTP_INTERNAL_ERROR reply", e);
            }
            if (!op.isAborted) {
                sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
            }
        }
    }

    private void handleGetRequest(int type) throws IOException {
        if (V) {
            Log.v(TAG, "handleGetRequest");
        }
        ServerOperation op = new ServerOperation(this, this.mInput, type, this.mMaxPacketLength, this.mListener);
        try {
            int response = validateResponseCode(this.mListener.onGet(op));
            if (!op.isAborted) {
                op.sendReply(response);
            }
        } catch (Exception e) {
            if (V) {
                Log.d(TAG, "Exception occured - sending OBEX_HTTP_INTERNAL_ERROR reply", e);
            }
            sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
        }
    }

    public void sendResponse(int code, byte[] header) throws IOException {
        if (V) {
            Log.v(TAG, "sendResponse code " + code + " header : " + header);
        }
        OutputStream op = this.mOutput;
        if (op != null) {
            byte[] data;
            if (header != null) {
                int totalLength = header.length + 3;
                if (V) {
                    Log.v(TAG, "header != null totalLength = " + totalLength);
                }
                data = new byte[totalLength];
                data[0] = (byte) code;
                data[1] = (byte) (totalLength >> 8);
                data[2] = (byte) totalLength;
                System.arraycopy(header, 0, data, 3, header.length);
            } else {
                data = new byte[]{(byte) code, (byte) 0, (byte) 3};
            }
            op.write(data);
            op.flush();
        }
    }

    private void handleSetPathRequest() throws IOException {
        int totalLength = 3;
        byte[] head = null;
        int code = -1;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int length = (this.mInput.read() << 8) + this.mInput.read();
        int flags = this.mInput.read();
        int constants = this.mInput.read();
        if (length > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 3;
        } else {
            if (length > 5) {
                byte[] headers = new byte[(length - 5)];
                int bytesReceived = this.mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += this.mInput.read(headers, bytesReceived, headers.length - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
                if (this.mListener.getConnectionId() == -1 || request.mConnectionID == null) {
                    this.mListener.setConnectionId(1);
                } else {
                    this.mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
                }
                if (request.mAuthResp != null) {
                    if (!handleAuthResp(request.mAuthResp)) {
                        code = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                        this.mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte) 1, request.mAuthResp));
                    }
                    request.mAuthResp = null;
                }
            }
            if (code != 193) {
                if (request.mAuthChall != null) {
                    handleAuthChall(request);
                    reply.mAuthResp = new byte[request.mAuthResp.length];
                    System.arraycopy(request.mAuthResp, 0, reply.mAuthResp, 0, reply.mAuthResp.length);
                    request.mAuthChall = null;
                    request.mAuthResp = null;
                }
                boolean backup = false;
                boolean create = true;
                if ((flags & 1) != 0) {
                    backup = true;
                }
                if ((flags & 2) != 0) {
                    create = false;
                }
                try {
                    code = validateResponseCode(this.mListener.onSetPath(request, reply, backup, create));
                    if (reply.nonce != null) {
                        this.mChallengeDigest = new byte[16];
                        System.arraycopy(reply.nonce, 0, this.mChallengeDigest, 0, 16);
                    } else {
                        this.mChallengeDigest = null;
                    }
                    long id = this.mListener.getConnectionId();
                    if (id == -1) {
                        reply.mConnectionID = null;
                    } else {
                        reply.mConnectionID = ObexHelper.convertToByteArray(id);
                    }
                    head = ObexHelper.createHeader(reply, false);
                    totalLength = head.length + 3;
                    if (totalLength > this.mMaxPacketLength) {
                        totalLength = 3;
                        head = null;
                        code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                    }
                } catch (Exception e) {
                    if (V) {
                        Log.d(TAG, "Exception occured - sending OBEX_HTTP_INTERNAL_ERROR reply", e);
                    }
                    sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
                    return;
                }
            }
        }
        byte[] replyData = new byte[totalLength];
        replyData[0] = (byte) code;
        replyData[1] = (byte) (totalLength >> 8);
        replyData[2] = (byte) totalLength;
        if (head != null) {
            System.arraycopy(head, 0, replyData, 3, head.length);
        }
        this.mOutput.write(replyData);
        this.mOutput.flush();
    }

    private void handleDisconnectRequest() throws IOException {
        byte[] replyData;
        int code = ResponseCodes.OBEX_HTTP_OK;
        int totalLength = 3;
        byte[] head = null;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int length = (this.mInput.read() << 8) + this.mInput.read();
        if (length > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 3;
        } else {
            if (length > 3) {
                byte[] headers = new byte[(length - 3)];
                int bytesReceived = this.mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += this.mInput.read(headers, bytesReceived, headers.length - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
            }
            if (this.mListener.getConnectionId() == -1 || request.mConnectionID == null) {
                this.mListener.setConnectionId(1);
            } else {
                this.mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
            }
            if (request.mAuthResp != null) {
                if (!handleAuthResp(request.mAuthResp)) {
                    code = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                    this.mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte) 1, request.mAuthResp));
                }
                request.mAuthResp = null;
            }
            if (code != ResponseCodes.OBEX_HTTP_UNAUTHORIZED) {
                if (request.mAuthChall != null) {
                    handleAuthChall(request);
                    request.mAuthChall = null;
                }
                try {
                    this.mListener.onDisconnect(request, reply);
                    long id = this.mListener.getConnectionId();
                    if (id == -1) {
                        reply.mConnectionID = null;
                    } else {
                        reply.mConnectionID = ObexHelper.convertToByteArray(id);
                    }
                    head = ObexHelper.createHeader(reply, false);
                    totalLength = head.length + 3;
                    if (totalLength > this.mMaxPacketLength) {
                        totalLength = 3;
                        head = null;
                        code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                    }
                } catch (Exception e) {
                    if (V) {
                        Log.d(TAG, "Exception occured - sending OBEX_HTTP_INTERNAL_ERROR reply", e);
                    }
                    sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
                    return;
                }
            }
        }
        if (head != null) {
            replyData = new byte[(head.length + 3)];
        } else {
            replyData = new byte[3];
        }
        replyData[0] = (byte) code;
        replyData[1] = (byte) (totalLength >> 8);
        replyData[2] = (byte) totalLength;
        if (head != null) {
            System.arraycopy(head, 0, replyData, 3, head.length);
        }
        this.mOutput.write(replyData);
        this.mOutput.flush();
    }

    private void handleConnectRequest() throws IOException {
        int totalLength = 7;
        byte[] head = null;
        int code = -1;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        if (V) {
            Log.v(TAG, "handleConnectRequest()");
        }
        int packetLength = (this.mInput.read() << 8) + this.mInput.read();
        if (V) {
            Log.v(TAG, "handleConnectRequest() - packetLength: " + packetLength);
        }
        int version = this.mInput.read();
        int flags = this.mInput.read();
        this.mMaxPacketLength = this.mInput.read();
        this.mMaxPacketLength = (this.mMaxPacketLength << 8) + this.mInput.read();
        if (V) {
            Log.v(TAG, "handleConnectRequest() - version: " + version + " MaxLength: " + this.mMaxPacketLength + " flags: " + flags);
        }
        if (this.setMTU) {
            this.mMaxPacketLength = ObexHelper.A2DP_SCO_OBEX_MAX_CLIENT_PACKET_SIZE;
            this.setMTU = false;
        } else if (this.updateMtu) {
            Log.d(TAG, "mMaxPacketLength: " + this.mMaxPacketLength + ", updatedMtuSize: " + this.updatedMtuSize);
            if (this.mMaxPacketLength > this.updatedMtuSize) {
                this.mMaxPacketLength = this.updatedMtuSize;
            }
            this.updateMtu = false;
        } else if (this.mMaxPacketLength > 65534) {
            this.mMaxPacketLength = ObexHelper.MAX_PACKET_SIZE_INT;
        }
        Log.d(TAG, "handleConnectRequest() - Updated MaxPacketLengh: " + this.mMaxPacketLength);
        if (this.mMaxPacketLength > ObexHelper.getMaxTxPacketSize(this.mTransport)) {
            Log.w(TAG, "Requested MaxObexPacketSize " + this.mMaxPacketLength + " is larger than the max size supported by the transport: " + ObexHelper.getMaxTxPacketSize(this.mTransport) + " Reducing to this size.");
            this.mMaxPacketLength = ObexHelper.getMaxTxPacketSize(this.mTransport);
        }
        if (packetLength > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 7;
        } else {
            if (packetLength > 7) {
                byte[] headers = new byte[(packetLength - 7)];
                int bytesReceived = this.mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += this.mInput.read(headers, bytesReceived, headers.length - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
            }
            if (this.mListener.getConnectionId() == -1 || request.mConnectionID == null) {
                this.mListener.setConnectionId(1);
            } else {
                this.mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
            }
            if (request.mAuthResp != null) {
                if (!handleAuthResp(request.mAuthResp)) {
                    code = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                    this.mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte) 1, request.mAuthResp));
                }
                request.mAuthResp = null;
            }
            if (code != 193) {
                if (request.mAuthChall != null) {
                    handleAuthChall(request);
                    reply.mAuthResp = new byte[request.mAuthResp.length];
                    System.arraycopy(request.mAuthResp, 0, reply.mAuthResp, 0, reply.mAuthResp.length);
                    request.mAuthChall = null;
                    request.mAuthResp = null;
                }
                try {
                    code = validateResponseCode(this.mListener.onConnect(request, reply));
                    if (reply.nonce != null) {
                        this.mChallengeDigest = new byte[16];
                        System.arraycopy(reply.nonce, 0, this.mChallengeDigest, 0, 16);
                    } else {
                        this.mChallengeDigest = null;
                    }
                    long id = this.mListener.getConnectionId();
                    if (id == -1) {
                        reply.mConnectionID = null;
                    } else {
                        reply.mConnectionID = ObexHelper.convertToByteArray(id);
                    }
                    head = ObexHelper.createHeader(reply, false);
                    totalLength = head.length + 7;
                    if (totalLength > this.mMaxPacketLength) {
                        totalLength = 7;
                        head = null;
                        code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                    }
                } catch (Exception e) {
                    if (V) {
                        Log.d(TAG, "Exception occured - sending OBEX_HTTP_INTERNAL_ERROR reply", e);
                    }
                    totalLength = 7;
                    head = null;
                    code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                }
            }
        }
        byte[] length = ObexHelper.convertToByteArray((long) totalLength);
        byte[] sendData = new byte[totalLength];
        int maxRxLength = ObexHelper.getMaxRxPacketSize(this.mTransport);
        if (maxRxLength > this.mMaxPacketLength) {
            if (V) {
                Log.v(TAG, "Set maxRxLength to min of maxRxServrLen:" + maxRxLength + " and MaxNegotiated from Client: " + this.mMaxPacketLength);
            }
            maxRxLength = this.mMaxPacketLength;
        }
        sendData[0] = (byte) code;
        sendData[1] = length[2];
        sendData[2] = length[3];
        sendData[3] = (byte) 16;
        sendData[4] = (byte) 0;
        sendData[5] = (byte) (maxRxLength >> 8);
        sendData[6] = (byte) (maxRxLength & 255);
        if (head != null) {
            System.arraycopy(head, 0, sendData, 7, head.length);
        }
        this.mOutput.write(sendData);
        this.mOutput.flush();
    }

    public synchronized void close() {
        if (this.mListener != null) {
            this.mListener.onClose();
        }
        try {
            this.mClosed = true;
            if (this.mInput != null) {
                this.mInput.close();
            }
            if (this.mOutput != null) {
                this.mOutput.close();
            }
            if (this.mTransport != null) {
                this.mTransport.close();
            }
        } catch (Exception e) {
            if (V) {
                Log.d(TAG, "Exception occured during close() - ignore", e);
            }
        }
        this.mTransport = null;
        this.mInput = null;
        this.mOutput = null;
        this.mListener = null;
    }

    private int validateResponseCode(int code) {
        if (code >= ResponseCodes.OBEX_HTTP_OK && code <= ResponseCodes.OBEX_HTTP_PARTIAL) {
            return code;
        }
        if (code >= ResponseCodes.OBEX_HTTP_MULT_CHOICE && code <= ResponseCodes.OBEX_HTTP_USE_PROXY) {
            return code;
        }
        if (code >= 192 && code <= ResponseCodes.OBEX_HTTP_UNSUPPORTED_TYPE) {
            return code;
        }
        if (code >= ResponseCodes.OBEX_HTTP_INTERNAL_ERROR && code <= ResponseCodes.OBEX_HTTP_VERSION) {
            return code;
        }
        if (code < ResponseCodes.OBEX_DATABASE_FULL || code > ResponseCodes.OBEX_DATABASE_LOCKED) {
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        return code;
    }

    public ObexTransport getTransport() {
        return this.mTransport;
    }
}
