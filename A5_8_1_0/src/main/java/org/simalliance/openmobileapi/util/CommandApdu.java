package org.simalliance.openmobileapi.util;

public class CommandApdu {
    private byte mCla;
    private byte[] mData;
    private byte mIns;
    private Integer mLe;
    private byte mP1;
    private byte mP2;

    private CommandApdu() {
    }

    public CommandApdu(byte cla, byte ins, byte p1, byte p2) throws IllegalArgumentException {
        setCla(cla);
        setIns(ins);
        setP1(p1);
        setP2(p2);
    }

    public CommandApdu(byte cla, byte ins, byte p1, byte p2, int le) throws IllegalArgumentException {
        setCla(cla);
        setIns(ins);
        setP1(p1);
        setP2(p2);
        setLe(le);
    }

    public CommandApdu(byte cla, byte ins, byte p1, byte p2, byte[] data) throws IllegalArgumentException {
        setCla(cla);
        setIns(ins);
        setP1(p1);
        setP2(p2);
        setData(data);
    }

    public CommandApdu(byte cla, byte ins, byte p1, byte p2, byte[] data, int le) throws IllegalArgumentException {
        setCla(cla);
        setIns(ins);
        setP1(p1);
        setP2(p2);
        setData(data);
        setLe(le);
    }

    public CommandApdu(byte[] cmdApduAsByteArray) throws IllegalArgumentException {
        if (cmdApduAsByteArray.length < 4) {
            throw new IllegalArgumentException("Invalid length for command (" + cmdApduAsByteArray.length + ").");
        }
        setCla(cmdApduAsByteArray[0]);
        setIns(cmdApduAsByteArray[1]);
        setP1(cmdApduAsByteArray[2]);
        setP2(cmdApduAsByteArray[3]);
        if (cmdApduAsByteArray.length != 4) {
            int lc;
            if (cmdApduAsByteArray.length == 5) {
                setLe(cmdApduAsByteArray[4] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
            } else if (cmdApduAsByteArray[4] != (byte) 0) {
                lc = cmdApduAsByteArray[4] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED;
                if (lc == 0) {
                    throw new IllegalArgumentException("Lc can't be 0");
                }
                if (cmdApduAsByteArray.length != lc + 5) {
                    if (cmdApduAsByteArray.length == lc + 6) {
                        setLe(cmdApduAsByteArray[cmdApduAsByteArray.length - 1] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
                    } else {
                        throw new IllegalArgumentException("Unexpected value of Lc (" + lc + ")");
                    }
                }
                this.mData = new byte[lc];
                System.arraycopy(cmdApduAsByteArray, 5, this.mData, 0, lc);
            } else if (cmdApduAsByteArray.length == 7) {
                setLe(((cmdApduAsByteArray[5] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 8) + (cmdApduAsByteArray[6] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED));
            } else if (cmdApduAsByteArray.length <= 7) {
                throw new IllegalArgumentException("Unexpected value of Lc or Le" + cmdApduAsByteArray.length);
            } else {
                lc = ((cmdApduAsByteArray[5] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 8) + (cmdApduAsByteArray[6] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
                if (lc == 0) {
                    throw new IllegalArgumentException("Lc can't be 0");
                }
                if (cmdApduAsByteArray.length != lc + 7) {
                    if (cmdApduAsByteArray.length == lc + 9) {
                        setLe(((cmdApduAsByteArray[cmdApduAsByteArray.length - 2] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED) << 8) + (cmdApduAsByteArray[cmdApduAsByteArray.length - 1] & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED));
                    } else {
                        throw new IllegalArgumentException("Unexpected value of Lc (" + lc + ")--- 9 -" + cmdApduAsByteArray.length);
                    }
                }
                this.mData = new byte[lc];
                System.arraycopy(cmdApduAsByteArray, 7, this.mData, 0, lc);
            }
        }
    }

    public byte[] toByteArray() {
        byte[] array;
        if (isExtendedLength()) {
            if (this.mData == null && this.mLe != null) {
                return new byte[]{this.mCla, this.mIns, this.mP1, this.mP2, (byte) 0, (byte) ((this.mLe.intValue() >> 8) & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED), (byte) (this.mLe.intValue() & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED)};
            } else if (this.mData == null || this.mLe != null) {
                array = new byte[(this.mData.length + 9)];
                array[0] = this.mCla;
                array[1] = this.mIns;
                array[2] = this.mP1;
                array[3] = this.mP2;
                array[4] = (byte) 0;
                array[5] = (byte) ((this.mData.length >> 8) & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
                array[6] = (byte) (this.mData.length & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
                System.arraycopy(this.mData, 0, array, 7, this.mData.length);
                array[array.length - 2] = (byte) ((this.mLe.intValue() >> 8) & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
                array[array.length - 1] = (byte) (this.mLe.intValue() & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
                return array;
            } else {
                array = new byte[(this.mData.length + 7)];
                array[0] = this.mCla;
                array[1] = this.mIns;
                array[2] = this.mP1;
                array[3] = this.mP2;
                array[4] = (byte) 0;
                array[5] = (byte) ((this.mData.length >> 8) & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
                array[6] = (byte) (this.mData.length & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
                System.arraycopy(this.mData, 0, array, 7, this.mData.length);
                return array;
            }
        } else if (this.mData == null && this.mLe == null) {
            return new byte[]{this.mCla, this.mIns, this.mP1, this.mP2};
        } else if (this.mData == null && this.mLe != null) {
            return new byte[]{this.mCla, this.mIns, this.mP1, this.mP2, (byte) (this.mLe.intValue() & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED)};
        } else if (this.mData == null || this.mLe != null) {
            array = new byte[(this.mData.length + 6)];
            array[0] = this.mCla;
            array[1] = this.mIns;
            array[2] = this.mP1;
            array[3] = this.mP2;
            array[4] = (byte) (this.mData.length & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
            System.arraycopy(this.mData, 0, array, 5, this.mData.length);
            array[array.length - 1] = (byte) (this.mLe.intValue() & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
            return array;
        } else {
            array = new byte[(this.mData.length + 5)];
            array[0] = this.mCla;
            array[1] = this.mIns;
            array[2] = this.mP1;
            array[3] = this.mP2;
            array[4] = (byte) (this.mData.length & ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED);
            System.arraycopy(this.mData, 0, array, 5, this.mData.length);
            return array;
        }
    }

    public CommandApdu cloneWithLe(int le) {
        if (this.mData == null) {
            return new CommandApdu(this.mCla, this.mIns, this.mP1, this.mP2, (byte) le);
        }
        return new CommandApdu(this.mCla, this.mIns, this.mP1, this.mP2, this.mData, (byte) le);
    }

    private void setCla(byte cla) throws IllegalArgumentException {
        if (cla == (byte) -1) {
            throw new IllegalArgumentException("Invalid value of CLA (" + Integer.toHexString(cla) + ")");
        }
        this.mCla = cla;
    }

    private void setIns(byte ins) throws IllegalArgumentException {
        if ((ins & 240) == 96 || (ins & 240) == 144) {
            throw new IllegalArgumentException("Invalid value of INS (" + Integer.toHexString(ins) + "). " + "0x6X and 0x9X are not valid values");
        }
        this.mIns = ins;
    }

    private void setP1(byte p1) {
        this.mP1 = p1;
    }

    private void setP2(byte p2) {
        this.mP2 = p2;
    }

    private void setData(byte[] data) throws IllegalArgumentException {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null.");
        } else if (data.length > ISO7816.MAX_COMMAND_DATA_LENGTH) {
            throw new IllegalArgumentException("Data too long.");
        } else if (data.length == 0) {
            throw new IllegalArgumentException("Data must not be empty.");
        } else {
            this.mData = new byte[data.length];
            System.arraycopy(data, 0, this.mData, 0, data.length);
        }
    }

    private void setLe(int le) throws IllegalArgumentException {
        if (le < 0 || le > ISO7816.MAX_RESPONSE_DATA_LENGTH) {
            throw new IllegalArgumentException("Invalid value for le parameter (" + le + ").");
        }
        this.mLe = Integer.valueOf(le);
    }

    public boolean isExtendedLength() {
        if ((this.mLe == null || this.mLe.intValue() <= ISO7816.MAX_RESPONSE_DATA_LENGTH_NO_EXTENDED) && (this.mData == null || this.mData.length <= ISO7816.MAX_COMMAND_DATA_LENGTH_NO_EXTENDED)) {
            return false;
        }
        return true;
    }
}
