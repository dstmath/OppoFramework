package com.mediatek.internal.telephony.cat;

public class TransportProtocol {
    public int portNumber = 0;
    public int protocolType = 0;

    public TransportProtocol(int type, int port) {
        this.protocolType = type;
        this.portNumber = port;
    }
}
