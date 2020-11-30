package com.mediatek.mtklogger.c2klogger;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class EtsMsg {
    private static long sMsPerDay = 86400000;
    public static int sSizeLast = 0;
    private static long sSpanMs = 2209017600000L;
    protected byte[] mData;
    protected short mId;
    protected long mTime;

    public short getId() {
        return this.mId;
    }

    public byte[] getData() {
        return this.mData;
    }

    public byte[] getLogEntry() {
        byte[] buf = new byte[(this.mData.length + 14)];
        System.arraycopy(EtsUtil.doubleToByte(((double) (System.currentTimeMillis() + sSpanMs)) / ((double) sMsPerDay)), 0, buf, 0, 8);
        buf[8] = 0;
        buf[9] = 0;
        System.arraycopy(EtsUtil.short2bytes(getLength()), 0, buf, 10, 2);
        System.arraycopy(EtsUtil.short2bytes(this.mId), 0, buf, 12, 2);
        byte[] bArr = this.mData;
        System.arraycopy(bArr, 0, buf, 14, bArr.length);
        return buf;
    }

    public byte[] getBuf() {
        byte[] bArr = this.mData;
        if (bArr == null) {
            return null;
        }
        byte[] buf = new byte[(bArr.length + 8)];
        buf[0] = -2;
        buf[1] = -36;
        buf[2] = -70;
        buf[3] = -104;
        short length = (short) (bArr.length + 2);
        buf[4] = (byte) (length & 255);
        buf[5] = (byte) ((length >> 8) & 255);
        short s = this.mId;
        buf[6] = (byte) (s & 255);
        buf[7] = (byte) ((s >> 8) & 255);
        System.arraycopy(bArr, 0, buf, 8, bArr.length);
        return buf;
    }

    public short getLength() {
        return (short) (this.mData.length + 2);
    }

    public int getProgRspSequence() {
        if (this.mId != 1202) {
            return -1;
        }
        return EtsUtil.bytes2int(this.mData);
    }

    public byte getProgRspAck() {
        if (this.mId != 1202) {
            return -1;
        }
        return this.mData[4];
    }

    public EtsMsg(short id, byte[] data) {
        this("00:00:00.0", id, data);
    }

    public EtsMsg(String time, short id, byte[] data) {
        this.mTime = 0;
        this.mData = null;
        try {
            this.mTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2000-01-01 " + time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.mId = id;
        if (data != null) {
            this.mData = new byte[data.length];
            System.arraycopy(data, 0, this.mData, 0, data.length);
            return;
        }
        this.mData = new byte[0];
    }

    public static List<EtsMsg> parse(byte[] buf, int sizeBuf) {
        int index = 0;
        List<EtsMsg> msgs = new ArrayList<>();
        sSizeLast = sizeBuf;
        while (true) {
            if (sSizeLast < 8) {
                break;
            }
            Boolean findHeader = false;
            while (true) {
                if (index <= sizeBuf - 8) {
                    if (buf[index] == -2 && buf[index + 1] == -36 && buf[index + 2] == -70 && buf[index + 3] == -104) {
                        findHeader = true;
                        break;
                    }
                    index++;
                } else {
                    break;
                }
            }
            if (!findHeader.booleanValue()) {
                Log.e("via_ets", "can't find the header(index=" + index + ")");
                sSizeLast = 0;
                break;
            }
            sSizeLast = sizeBuf - index;
            int index2 = index + 4;
            int index3 = index2 + 1;
            index = index3 + 1;
            byte[] temp = {buf[index2], buf[index3]};
            short length = EtsUtil.bytes2short(temp);
            if (length < 2 || length > 2048) {
                Log.e("via_ets", "invalid length(length=" + ((int) length) + "),index=" + index);
            } else if (sizeBuf < index + length) {
                Log.w("via_ets", "not enough data for this message(length=" + ((int) length) + ", last buf size=" + (sizeBuf - index));
                break;
            } else {
                int index4 = index + 1;
                temp[0] = buf[index];
                int index5 = index4 + 1;
                temp[1] = buf[index4];
                short id = EtsUtil.bytes2short(temp);
                byte[] temp2 = new byte[(length - 2)];
                System.arraycopy(buf, index5, temp2, 0, length - 2);
                index = index5 + (length - 2);
                msgs.add(new EtsMsg(id, temp2));
                sSizeLast = sizeBuf - index;
            }
        }
        return msgs;
    }

    public static int removeErrorBuffer(byte[] buf, int sizeTotal) {
        Log.i("via_ets", "The size_buf = " + sizeTotal);
        int index = 0;
        if (sizeTotal <= 8) {
            return 0;
        }
        Boolean findHeader = false;
        while (true) {
            if (index <= sizeTotal - 8) {
                if (buf[index] == -2 && buf[index + 1] == -36 && buf[index + 2] == -70 && buf[index + 3] == -104) {
                    Log.i("via_ets", "buf[" + index + "] is " + ((int) buf[index]));
                    Log.i("via_ets", "buf[" + (index + 1) + "] is " + ((int) buf[index + 1]));
                    Log.i("via_ets", "buf[" + (index + 2) + "] is " + ((int) buf[index + 2]));
                    Log.i("via_ets", "buf[" + (index + 3) + "] is " + ((int) buf[index + 3]));
                    findHeader = true;
                    break;
                }
                index++;
            } else {
                break;
            }
        }
        Log.e("via_ets", "The index=" + index);
        if (!findHeader.booleanValue()) {
            Log.e("via_ets", "can't find the header(index=" + index + ")");
            return 0;
        }
        System.arraycopy(buf, index, buf, 0, sizeTotal - index);
        return sizeTotal - index;
    }

    public static EtsMsg parse(String line) {
        int index;
        int index2 = line.indexOf("Raw Tx");
        if (index2 < 0 || (index = line.indexOf(44, index2)) < 0) {
            return null;
        }
        byte[] msgBin = hexStr2bytes(line.substring(index + 2));
        short id = EtsUtil.bytes2short(msgBin);
        byte[] data = new byte[(msgBin.length - 2)];
        for (int i = 0; i < msgBin.length - 2; i++) {
            data[i] = msgBin[i + 2];
        }
        return new EtsMsg(line.substring(0, 10), id, data);
    }

    private static byte[] hexStr2bytes(String src) {
        String[] datas = src.split(" ");
        byte[] ret = new byte[datas.length];
        for (int i = 0; i < datas.length; i++) {
            ret[i] = Integer.decode(datas[i]).byteValue();
        }
        return ret;
    }
}
