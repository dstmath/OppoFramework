package com.mediatek.internal.telephony.cat;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OtherAddress {
    public InetAddress address = null;
    public int addressType = 0;
    public byte[] rawAddress = null;

    public OtherAddress(int type, byte[] rawData, int index) throws UnknownHostException {
        try {
            this.addressType = type;
            if (33 == this.addressType) {
                this.rawAddress = new byte[4];
                System.arraycopy(rawData, index, this.rawAddress, 0, this.rawAddress.length);
                this.address = InetAddress.getByAddress(this.rawAddress);
            } else if (87 == this.addressType) {
                this.rawAddress = new byte[16];
                System.arraycopy(rawData, index, this.rawAddress, 0, this.rawAddress.length);
                this.address = InetAddress.getByAddress(this.rawAddress);
            } else {
                MtkCatLog.e("[BIP]", "OtherAddress: unknown type: " + type);
            }
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.d("[BIP]", "OtherAddress: out of bounds");
            this.rawAddress = null;
            this.address = null;
        } catch (UnknownHostException e2) {
            MtkCatLog.e("[BIP]", "OtherAddress: UnknownHostException");
            this.rawAddress = null;
            this.address = null;
        }
    }
}
