package org.simalliance.openmobileapi.util;

public class ResponseApdu {
    private byte[] mData;
    private byte[] mSw;

    public ResponseApdu(byte[] response) {
        if (response == null) {
            throw new IllegalArgumentException("Response must not be null.");
        } else if (response.length < 2 || response.length > 65538) {
            throw new IllegalArgumentException("Invalid response length (" + response.length + ").");
        } else {
            if (response.length > 2) {
                this.mData = new byte[(response.length - 2)];
                System.arraycopy(response, 0, this.mData, 0, this.mData.length);
            }
            this.mSw = new byte[2];
            System.arraycopy(response, response.length - 2, this.mSw, 0, 2);
        }
    }

    public byte[] getData() {
        return this.mData;
    }

    public byte[] getSw() {
        return this.mSw;
    }

    public int getSwValue() {
        return ((this.mSw[0] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 8) + (this.mSw[1] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
    }

    public boolean isSuccess() {
        return getSwValue() == ISO7816.SW_NO_FURTHER_QUALIFICATION;
    }

    public boolean isWarning() {
        return this.mSw[0] == ISO7816.SW1_62 || this.mSw[0] == ISO7816.SW1_63;
    }

    public int getSw1Value() {
        return this.mSw[0] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED;
    }

    public int getSw2Value() {
        return this.mSw[1] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED;
    }
}
