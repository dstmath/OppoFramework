package com.mediatek.internal.telephony.ppl;

import android.util.Log;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PplControlData {
    private static final int HEADER_SIZE = 60;
    public static final int SALT_LIST_LENGTH = 40;
    public static final int SALT_SIZE = 20;
    public static final int SECRET_LIST_LENGTH = 40;
    public static final int SECRET_SIZE = 32;
    public static final int SIM_FINGERPRINT_LENGTH = 40;
    public static final byte STATUS_ENABLED = 2;
    public static final byte STATUS_LOCKED = 4;
    public static final byte STATUS_PROVISIONED = 1;
    public static final byte STATUS_SIM_LOCKED = 8;
    public static final byte STATUS_WIPE_REQUESTED = 16;
    private static final String TAG = "PPL/ControlData";
    public static final int TRUSTED_NUMBER_LENGTH = 40;
    public static final byte VERSION = 1;
    private static Comparator<byte[]> mSIMComparator = new Comparator<byte[]>() {
        /* class com.mediatek.internal.telephony.ppl.PplControlData.AnonymousClass1 */

        public int compare(byte[] lhs, byte[] rhs) {
            return PplControlData.compareSIMFingerprints(lhs, rhs);
        }
    };
    public List<PplMessageManager.PendingMessage> PendingMessageList = null;
    public List<byte[]> SIMFingerprintList = null;
    public List<String> TrustedNumberList = null;
    public byte[] salt = new byte[20];
    public byte[] secret = new byte[32];
    public byte status = 0;
    public byte version = 1;

    public byte[] encode() {
        byte[] result = new byte[getDataSize()];
        result[0] = this.version;
        result[1] = this.status;
        List<byte[]> list = this.SIMFingerprintList;
        result[2] = list == null ? 0 : (byte) list.size();
        List<String> list2 = this.TrustedNumberList;
        result[3] = list2 == null ? 0 : (byte) list2.size();
        List<PplMessageManager.PendingMessage> list3 = this.PendingMessageList;
        result[4] = list3 == null ? 0 : (byte) list3.size();
        result[5] = 0;
        result[6] = 0;
        result[7] = 0;
        byte[] bArr = this.secret;
        System.arraycopy(bArr, 0, result, 8, bArr.length);
        int offset = 8 + this.secret.length;
        byte[] bArr2 = this.salt;
        System.arraycopy(bArr2, 0, result, offset, bArr2.length);
        int offset2 = offset + this.salt.length;
        if (this.SIMFingerprintList != null) {
            for (int i = 0; i < this.SIMFingerprintList.size(); i++) {
                System.arraycopy(this.SIMFingerprintList.get(i), 0, result, offset2, 40);
                offset2 += 40;
            }
        }
        if (this.TrustedNumberList != null) {
            int i2 = 0;
            while (i2 < this.TrustedNumberList.size()) {
                byte[] buffer = this.TrustedNumberList.get(i2).getBytes();
                if (buffer.length <= 40) {
                    System.arraycopy(Arrays.copyOf(buffer, 40), 0, result, offset2, 40);
                    offset2 += 40;
                    i2++;
                } else {
                    throw new Error("Trusted number is too long");
                }
            }
        }
        if (this.PendingMessageList != null) {
            for (int i3 = 0; i3 < this.PendingMessageList.size(); i3++) {
                this.PendingMessageList.get(i3).encode(result, offset2);
                offset2 += 49;
            }
        }
        return result;
    }

    public void decode(byte[] data) {
        this.version = data[0];
        this.status = data[1];
        byte numberOfSIMFingerprint = data[2];
        byte numberOfTrustedNumber = data[3];
        byte numberOfPendingMessage = data[4];
        byte[] bArr = this.secret;
        System.arraycopy(data, 8, bArr, 0, bArr.length);
        int offset = 8 + this.secret.length;
        byte[] bArr2 = this.salt;
        System.arraycopy(data, offset, bArr2, 0, bArr2.length);
        int offset2 = offset + this.salt.length;
        if (numberOfSIMFingerprint != 0) {
            this.SIMFingerprintList = new LinkedList();
            for (int i = 0; i < numberOfSIMFingerprint; i++) {
                byte[] fingerprint = new byte[40];
                System.arraycopy(data, offset2, fingerprint, 0, 40);
                this.SIMFingerprintList.add(fingerprint);
                offset2 += 40;
            }
        } else {
            this.SIMFingerprintList = null;
        }
        if (numberOfTrustedNumber != 0) {
            this.TrustedNumberList = new LinkedList();
            for (int i2 = 0; i2 < numberOfTrustedNumber; i2++) {
                int j = offset2;
                while (j < offset2 + 40 && data[j] != 0) {
                    j++;
                }
                this.TrustedNumberList.add(new String(data, offset2, j - offset2));
                offset2 += 40;
            }
        } else {
            this.TrustedNumberList = null;
        }
        if (numberOfPendingMessage != 0) {
            this.PendingMessageList = new LinkedList();
            for (int i3 = 0; i3 < numberOfPendingMessage; i3++) {
                this.PendingMessageList.add(new PplMessageManager.PendingMessage(data, offset2));
                offset2 += 49;
            }
            return;
        }
        this.PendingMessageList = null;
    }

    private int getDataSize() {
        int result = 60;
        List<byte[]> list = this.SIMFingerprintList;
        if (list != null) {
            result = 60 + (list.size() * 40);
        }
        List<String> list2 = this.TrustedNumberList;
        if (list2 != null) {
            result += list2.size() * 40;
        }
        List<PplMessageManager.PendingMessage> list3 = this.PendingMessageList;
        if (list3 != null) {
            return result + (list3.size() * 49);
        }
        return result;
    }

    public static PplControlData buildControlData(byte[] data) {
        PplControlData result = new PplControlData();
        if (data == null || data.length == 0) {
            Log.w(TAG, "buildControlData: data is empty, return empty instance");
        } else {
            result.decode(data);
        }
        return result;
    }

    public PplControlData clone() {
        PplControlData result = new PplControlData();
        result.version = this.version;
        result.status = this.status;
        result.secret = (byte[]) this.secret.clone();
        result.salt = (byte[]) this.salt.clone();
        if (this.SIMFingerprintList != null) {
            result.SIMFingerprintList = new LinkedList();
            for (int i = 0; i < this.SIMFingerprintList.size(); i++) {
                result.SIMFingerprintList.add((byte[]) this.SIMFingerprintList.get(i).clone());
            }
        } else {
            result.SIMFingerprintList = null;
        }
        if (this.TrustedNumberList != null) {
            result.TrustedNumberList = new LinkedList();
            for (String s : this.TrustedNumberList) {
                result.TrustedNumberList.add(s);
            }
        } else {
            result.TrustedNumberList = null;
        }
        if (this.PendingMessageList != null) {
            result.PendingMessageList = new LinkedList();
            for (PplMessageManager.PendingMessage pm : this.PendingMessageList) {
                result.PendingMessageList.add(pm.clone());
            }
        } else {
            this.PendingMessageList = null;
        }
        return result;
    }

    public void clear() {
        this.version = 1;
        this.status = 0;
        this.secret = new byte[32];
        this.salt = new byte[20];
        this.SIMFingerprintList = null;
        this.TrustedNumberList = null;
        this.PendingMessageList = null;
    }

    public boolean isEnabled() {
        return (this.status & 2) == 2;
    }

    public void setEnable(boolean flag) {
        if (flag) {
            this.status = (byte) (this.status | 2);
        } else {
            this.status = (byte) (this.status & -3);
        }
    }

    public boolean hasWipeFlag() {
        return (this.status & STATUS_WIPE_REQUESTED) == 16;
    }

    public void setWipeFlag(boolean flag) {
        if (flag) {
            this.status = (byte) (this.status | STATUS_WIPE_REQUESTED);
        } else {
            this.status = (byte) (this.status & -17);
        }
    }

    public boolean isProvisioned() {
        return (this.status & 1) == 1;
    }

    public void setProvision(boolean flag) {
        if (flag) {
            this.status = (byte) (this.status | 1);
        } else {
            this.status = (byte) (this.status & -2);
        }
    }

    public boolean isLocked() {
        return (this.status & 4) == 4;
    }

    public void setLock(boolean flag) {
        if (flag) {
            this.status = (byte) (this.status | 4);
        } else {
            this.status = (byte) (this.status & -5);
        }
    }

    public boolean isSIMLocked() {
        return (this.status & 8) == 8;
    }

    public void setSIMLock(boolean flag) {
        if (flag) {
            this.status = (byte) (this.status | 8);
        } else {
            this.status = (byte) (this.status & -9);
        }
    }

    public static byte[][] sortSIMFingerprints(byte[][] input) {
        byte[][] result = (byte[][]) input.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte[]) result[i].clone();
        }
        Arrays.sort(result, mSIMComparator);
        return result;
    }

    public static int compareSIMFingerprints(byte[] lhs, byte[] rhs) {
        if (lhs.length == rhs.length) {
            for (int i = 0; i < lhs.length; i++) {
                int difference = lhs[i] - rhs[i];
                if (difference != 0) {
                    return difference;
                }
            }
            return 0;
        }
        throw new Error("The two fingerprints must have the same length");
    }

    public String toString() {
        return "PplControlData " + hashCode() + " {" + Integer.toHexString(this.version) + ", " + Integer.toHexString(this.status) + ", " + this.SIMFingerprintList + ", " + this.TrustedNumberList + ", " + this.PendingMessageList + "}";
    }
}
