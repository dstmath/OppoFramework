package com.android.server.connectivity.networkrecovery.dnsresolve;

import android.net.wifi.WifiRomUpdateHelper;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import vendor.oppo.hardware.nfc.V1_0.OppoNfcChipVersion;

public class UdpTransport extends DnsTransport {
    private static final boolean DBG = true;
    private static final int DNS_RESPONSE_HEADER_LENGHT = 12;
    private static final int DNS_RESPONSE_MAX_LENGHT = 1500;
    private static final int MAX_PACKET_SIZE = 512;
    private static final String TAG = "OPPODnsSelfrecoveryEngine.UdpTransport";
    private int mQuerryTimeout = 2000;
    private int mRetryCount = 2;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private final int port = 53;
    private final InetAddress server;

    public UdpTransport(InetAddress server2, int querryTimeout, int retryCount) {
        this.server = server2;
        this.mQuerryTimeout = querryTimeout;
        this.mRetryCount = retryCount;
    }

    @Override // com.android.server.connectivity.networkrecovery.dnsresolve.DnsTransport
    public Message sendQuery(Message requestMessage) throws IOException {
        byte[] messageBytes = serializeMessage(requestMessage, 512);
        short queryId = requestMessage.getHeader().getId();
        DatagramPacket requestPacket = new DatagramPacket(messageBytes, messageBytes.length, this.server, 53);
        DatagramSocket s = new DatagramSocket();
        s.setSoTimeout(this.mQuerryTimeout);
        boolean revResponse = false;
        byte[] buf = new byte[1500];
        DatagramPacket response = new DatagramPacket(buf, buf.length);
        int send_count = 0;
        while (!revResponse && send_count < this.mRetryCount) {
            try {
                s.send(requestPacket);
                s.receive(response);
                revResponse = true;
            } catch (IOException e) {
                send_count++;
            }
        }
        s.close();
        if (revResponse && response.getLength() > 12) {
            byte[] responseBytes = new byte[response.getLength()];
            System.arraycopy(response.getData(), response.getOffset(), responseBytes, 0, response.getLength());
            ByteBuffer byteBuffer = ByteBuffer.wrap(responseBytes);
            short responseId = byteBuffer.getShort(0);
            if (responseId == queryId) {
                return new Message().fromBytes(byteBuffer);
            }
            logd("responseId = " + ((int) responseId) + " queryId=" + ((int) queryId));
            String dumpString = "";
            int i = 0;
            while (i < responseBytes.length) {
                String temp = Integer.toHexString(responseBytes[i] & OppoNfcChipVersion.NONE);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                dumpString = dumpString + StringUtils.SPACE + temp;
                i++;
                messageBytes = messageBytes;
            }
            logd("bytes:" + dumpString);
        }
        Message responseMessage = new Message();
        responseMessage.setFlag(false);
        return responseMessage;
    }

    private void logd(String message) {
        Log.d(TAG, message);
    }
}
