package mediatek.telephony;

import android.os.Parcel;
import android.os.PersistableBundle;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.mediatek.internal.telephony.MtkOpTelephonyCustomizationFactoryBase;
import java.util.Arrays;

public class MtkSignalStrength extends SignalStrength {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "MtkSignalStrength";
    public static final String PROPERTY_OPERATOR_OPTR = "persist.vendor.operator.optr";
    private static String mOpId = null;
    private static ISignalStrengthExt mSignalStrengthExt = null;
    private int mMtkLevel = -1;
    private int[] mMtkLteRsrpThreshold = null;
    private int[] mMtkLteRssnrThreshold = null;
    private boolean mMtkRsrpOnly = false;
    protected int mPhoneId;
    private MtkOpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;

    public MtkSignalStrength(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public MtkSignalStrength(int phoneId, SignalStrength s) {
        super(s);
        this.mPhoneId = phoneId;
        if (s instanceof MtkSignalStrength) {
            MtkSignalStrength mtkSignal = (MtkSignalStrength) s;
            this.mMtkLevel = mtkSignal.getMtkLevel();
            setMtkRsrpOnly(mtkSignal.isMtkRsrpOnly());
            setMtkLteRsrpThreshold(mtkSignal.getMtkLteRsrpThreshold());
            setMtkLteRssnrThreshold(mtkSignal.getMtkLteRssnrThreshold());
        }
    }

    public MtkSignalStrength(Parcel in) {
        super(in);
        this.mPhoneId = in.readInt();
        this.mMtkLevel = in.readInt();
        this.mMtkRsrpOnly = in.readBoolean();
        int size = in.readInt();
        if (size > 0) {
            this.mMtkLteRsrpThreshold = new int[size];
            in.readIntArray(this.mMtkLteRsrpThreshold);
        } else {
            this.mMtkLteRsrpThreshold = null;
        }
        int size2 = in.readInt();
        if (size2 > 0) {
            this.mMtkLteRssnrThreshold = new int[size2];
            in.readIntArray(this.mMtkLteRssnrThreshold);
            return;
        }
        this.mMtkLteRssnrThreshold = null;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.mPhoneId);
        out.writeInt(this.mMtkLevel);
        out.writeBoolean(this.mMtkRsrpOnly);
        int[] iArr = this.mMtkLteRsrpThreshold;
        int size = 0;
        int size2 = iArr == null ? 0 : iArr.length;
        out.writeInt(size2);
        if (size2 > 0) {
            out.writeIntArray(this.mMtkLteRsrpThreshold);
        }
        int[] iArr2 = this.mMtkLteRssnrThreshold;
        if (iArr2 != null) {
            size = iArr2.length;
        }
        out.writeInt(size);
        if (size > 0) {
            out.writeIntArray(this.mMtkLteRssnrThreshold);
        }
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }

    public String toString() {
        return super.toString() + ", phoneId=" + this.mPhoneId + ", mMtkLevel=" + this.mMtkLevel + ", mMtkRsrpOnly=" + this.mMtkRsrpOnly + ", mMtkLteRsrpThreshold=" + Arrays.toString(this.mMtkLteRsrpThreshold) + ", mMtkLteRssnrThreshold=" + Arrays.toString(this.mMtkLteRssnrThreshold);
    }

    private boolean useMtkLevel() {
        if (!this.mLte.isValid()) {
            return false;
        }
        if (this.mMtkLteRsrpThreshold == null && this.mMtkLteRssnrThreshold == null) {
            return false;
        }
        return DBG;
    }

    public int getLevel() {
        if (useMtkLevel()) {
            return this.mMtkLevel;
        }
        if (this.mLte.isValid() || this.mNr.getSsRsrp() == Integer.MAX_VALUE) {
            return super.getLevel();
        }
        return getNrLevel();
    }

    public int getNrLevel() {
        int level;
        int mSsRsrp = this.mNr.getSsRsrp();
        if (mSsRsrp == Integer.MAX_VALUE) {
            level = 0;
        } else if (mSsRsrp >= -95) {
            level = 4;
        } else if (mSsRsrp >= -105) {
            level = 3;
        } else if (mSsRsrp >= -115) {
            level = 2;
        } else {
            level = 1;
        }
        log("getLevel customize NR signal=" + level);
        return level;
    }

    public int getLteLevel() {
        if (useMtkLevel()) {
            return this.mMtkLevel;
        }
        return this.mLte.getLevel();
    }

    public boolean isMtkRsrpOnly() {
        return this.mMtkRsrpOnly;
    }

    public void setMtkRsrpOnly(boolean a) {
        this.mMtkRsrpOnly = a;
    }

    public int[] getMtkLteRsrpThreshold() {
        return this.mMtkLteRsrpThreshold;
    }

    public void setMtkLteRsrpThreshold(int[] threshold) {
        if (threshold == null) {
            this.mMtkLteRsrpThreshold = null;
            return;
        }
        this.mMtkLteRsrpThreshold = new int[threshold.length];
        for (int i = 0; i < threshold.length; i++) {
            this.mMtkLteRsrpThreshold[i] = threshold[i];
        }
    }

    public int[] getMtkLteRssnrThreshold() {
        return this.mMtkLteRssnrThreshold;
    }

    public void setMtkLteRssnrThreshold(int[] threshold) {
        if (threshold == null) {
            this.mMtkLteRssnrThreshold = null;
            return;
        }
        this.mMtkLteRssnrThreshold = new int[threshold.length];
        for (int i = 0; i < threshold.length; i++) {
            this.mMtkLteRssnrThreshold[i] = threshold[i];
        }
    }

    public int getMtkLevel() {
        return this.mMtkLevel;
    }

    public void updateMtkLevel(PersistableBundle cc, ServiceState ss) {
        updateMtkLteLevel(cc, ss);
    }

    private void updateMtkLteLevel(PersistableBundle cc, ServiceState ss) {
        int rsrpIconLevel;
        int snrIconLevel;
        int rssiIconLevel;
        if (this.mMtkLteRsrpThreshold == null) {
            return;
        }
        if (this.mMtkRsrpOnly || this.mMtkLteRssnrThreshold != null) {
            int[] rsrpThresholds = this.mMtkLteRsrpThreshold;
            int[] iArr = this.mMtkLteRssnrThreshold;
            boolean rsrpOnly = this.mMtkRsrpOnly;
            int rsrpBoost = 0;
            if (ss != null) {
                rsrpBoost = ss.getLteEarfcnRsrpBoost();
            }
            int rsrp = this.mLte.getRsrp() + rsrpBoost;
            if (rsrp < -140 || rsrp > -44) {
                rsrpIconLevel = -1;
            } else {
                rsrpIconLevel = rsrpThresholds.length;
                while (rsrpIconLevel > 0 && rsrp < rsrpThresholds[rsrpIconLevel - 1]) {
                    rsrpIconLevel--;
                }
            }
            if (rsrpOnly) {
                log("updateLevel() - rsrp = " + rsrpIconLevel);
                if (rsrpIconLevel != -1) {
                    this.mMtkLevel = rsrpIconLevel;
                    return;
                }
            }
            int rssnr = this.mLte.getRssnr();
            if (rssnr > 300) {
                snrIconLevel = -1;
            } else {
                snrIconLevel = this.mMtkLteRssnrThreshold.length;
                while (snrIconLevel > 0 && rssnr < this.mMtkLteRssnrThreshold[snrIconLevel - 1]) {
                    snrIconLevel--;
                }
            }
            log("updateLevel() - rsrp:" + rsrp + " snr:" + rssnr + " rsrpIconLevel:" + rsrpIconLevel + " snrIconLevel:" + snrIconLevel + " lteRsrpBoost:" + rsrpBoost);
            if (snrIconLevel != -1 && rsrpIconLevel != -1) {
                this.mMtkLevel = rsrpIconLevel < snrIconLevel ? rsrpIconLevel : snrIconLevel;
            } else if (snrIconLevel != -1) {
                this.mMtkLevel = snrIconLevel;
            } else if (rsrpIconLevel != -1) {
                this.mMtkLevel = rsrpIconLevel;
            } else {
                int rssi = this.mLte.getRssi();
                if (rssi > -51) {
                    rssiIconLevel = 0;
                } else if (rssi >= -89) {
                    rssiIconLevel = 4;
                } else if (rssi >= -97) {
                    rssiIconLevel = 3;
                } else if (rssi >= -103) {
                    rssiIconLevel = 2;
                } else if (rssi >= -113) {
                    rssiIconLevel = 1;
                } else {
                    rssiIconLevel = 0;
                }
                log("getLteLevel - rssi:" + rssi + " rssiIconLevel:" + rssiIconLevel);
                this.mMtkLevel = rssiIconLevel;
            }
        }
    }
}
