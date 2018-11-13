package qcom.fmradio;

import android.os.SystemProperties;
import android.util.Log;
import java.util.List;

public class FmConfig {
    private static final String TAG = "FmConfig";
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_EMPHASIS = 134217740;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDS_STD = 134217741;
    private static final int V4L2_CID_PRIVATE_TAVARUA_REGION = 134217735;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SPACING = 134217742;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SRCH_ALGORITHM = 134217771;
    private static final int each_Spur_entry_size = 16;
    public static final int no_Of_Spurs_For_Entry = 3;
    private int mBandLowerLimit;
    private int mBandUpperLimit;
    private int mChSpacing;
    private int mEmphasis;
    private int mRadioBand;
    private int mRdsStd;

    public static boolean fmSpurConfig(int fd) {
        SpurTable t = new SpurFileParser().GetSpurTable("/etc/fm/SpurTableFile.txt");
        List<Spur> list = t.GetSpurList();
        byte no_of_spur_freq = t.GetspurNoOfFreq();
        short[] buff = new short[((no_of_spur_freq * each_Spur_entry_size) + 2)];
        buff[0] = (short) t.GetMode();
        buff[1] = (short) no_of_spur_freq;
        for (byte i = (byte) 0; i < no_of_spur_freq; i++) {
            List<Spur> spur = t.GetSpurList();
            int freq = ((Spur) spur.get(i)).getSpurFreq();
            buff[(i * each_Spur_entry_size) + 2] = (short) (freq & 255);
            buff[(i * each_Spur_entry_size) + 3] = (short) ((freq >> 8) & 255);
            buff[(i * each_Spur_entry_size) + 4] = (short) ((freq >> each_Spur_entry_size) & 255);
            buff[(i * each_Spur_entry_size) + 5] = (short) ((Spur) spur.get(i)).getNoOfSpursToTrack();
            List<SpurDetails> spurDetails = ((Spur) spur.get(i)).getSpurDetailsList();
            for (int j = 0; j < 3; j++) {
                int rotation_value = ((SpurDetails) spurDetails.get(j)).getRotationValue();
                buff[((j * 4) + 6) + (i * each_Spur_entry_size)] = (short) (rotation_value & 255);
                buff[((j * 4) + 7) + (i * each_Spur_entry_size)] = (short) ((rotation_value >> 8) & 255);
                buff[((j * 4) + 8) + (i * each_Spur_entry_size)] = (short) ((rotation_value >> each_Spur_entry_size) & 15);
                int i2 = (i * each_Spur_entry_size) + ((j * 4) + 8);
                buff[i2] = (short) (((short) (((SpurDetails) spurDetails.get(j)).getLsbOfIntegrationLength() << 4)) | buff[i2]);
                i2 = (i * each_Spur_entry_size) + ((j * 4) + 8);
                buff[i2] = (short) (((short) (((SpurDetails) spurDetails.get(j)).getFilterCoefficeint() << 5)) | buff[i2]);
                i2 = (i * each_Spur_entry_size) + ((j * 4) + 8);
                buff[i2] = (short) (((short) (((SpurDetails) spurDetails.get(j)).getIsEnableSpur() << 7)) | buff[i2]);
                buff[(i * each_Spur_entry_size) + ((j * 4) + 9)] = (short) ((SpurDetails) spurDetails.get(j)).getSpurLevel();
            }
        }
        if (FmReceiverJNI.setSpurDataNative(fd, buff, (no_of_spur_freq * each_Spur_entry_size) + 2) < 0) {
            return false;
        }
        return true;
    }

    public int getRadioBand() {
        return this.mRadioBand;
    }

    public void setRadioBand(int band) {
        this.mRadioBand = band;
    }

    public int getEmphasis() {
        return this.mEmphasis;
    }

    public void setEmphasis(int emp) {
        this.mEmphasis = emp;
    }

    public int getChSpacing() {
        return this.mChSpacing;
    }

    public void setChSpacing(int spacing) {
        this.mChSpacing = spacing;
    }

    public int getRdsStd() {
        return this.mRdsStd;
    }

    public void setRdsStd(int rdsStandard) {
        this.mRdsStd = rdsStandard;
    }

    public int getLowerLimit() {
        return this.mBandLowerLimit;
    }

    public void setLowerLimit(int lowLimit) {
        this.mBandLowerLimit = lowLimit;
    }

    public int getUpperLimit() {
        return this.mBandUpperLimit;
    }

    public void setUpperLimit(int upLimit) {
        this.mBandUpperLimit = upLimit;
    }

    protected static boolean fmConfigure(int fd, FmConfig configSettings) {
        Log.v(TAG, "In fmConfigure");
        int re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_EMPHASIS, configSettings.getEmphasis());
        re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_RDS_STD, configSettings.getRdsStd());
        re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SPACING, configSettings.getChSpacing());
        if (SystemProperties.getBoolean("persist.fm.new.srch.algorithm", false)) {
            Log.v(TAG, "fmConfigure() : FM Srch Alg : NEW ");
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_ALGORITHM, 1);
        } else {
            Log.v(TAG, "fmConfigure() : FM Srch Alg : OLD ");
            re = FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SRCH_ALGORITHM, 0);
        }
        return re >= 0 && FmReceiverJNI.setBandNative(fd, configSettings.getLowerLimit(), configSettings.getUpperLimit()) >= 0 && FmReceiverJNI.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_REGION, configSettings.mRadioBand) >= 0;
    }
}
