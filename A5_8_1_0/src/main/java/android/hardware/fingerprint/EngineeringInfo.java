package android.hardware.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;

public final class EngineeringInfo implements Parcelable {
    public static final Creator<EngineeringInfo> CREATOR = new Creator<EngineeringInfo>() {
        public EngineeringInfo createFromParcel(Parcel in) {
            return new EngineeringInfo(in, null);
        }

        public EngineeringInfo[] newArray(int size) {
            return new EngineeringInfo[size];
        }
    };
    private HashMap<Integer, String> mEngineeringInfoMap;
    private int[] mKey;
    private int mLength;
    private String[] mValue;

    public class EngineeringInfoAcquireAction {
        public static final int FINGERPRINT_GET_BAD_PIXELS = 2;
        public static final int FINGERPRINT_GET_IMAGET_QUALITY = 1;
        public static final int FINGERPRINT_GET_IMAGE_SNR = 0;
        public static final int FINGERPRINT_GET_UNLOCK_TIME = 1000;
        public static final int FINGERPRINT_SELF_TEST = 3;
    }

    public class EngineeringParameterGroup {
        public static final int BAD_PIXEL_NUM = 4;
        public static final int IMAGE_QUALITY = 1;
        public static final int IMAGE_SNR = 3;
        public static final int LOCAL_BAD_PIXEL_NUM = 5;
        public static final int LOCAL_BIG_PIXEL_NUM = 8;
        public static final int M_ALL_TILT_ANGLE = 6;
        public static final int M_BLOCK_TILT_ANGLE_MAX = 7;
        public static final int SNR_SUCCESSED = 2;
        public static final int SUCCESSED = 0;
        public static final int TYPE_SCREEN_OFF_TOUCH_DOWN_TIME = 1000;
        public static final int TYPE_SCREEN_OFF_UNLOCK_TIME = 1002;
        public static final int TYPE_SCREEN_ON_TOUCH_DOWN_TIME = 1001;
        public static final int TYPE_SCREEN_ON_UNLOCK_TIME = 1003;
    }

    public EngineeringInfo(int key, String value) {
        this.mKey = null;
        this.mValue = null;
        this.mEngineeringInfoMap = new HashMap();
        this.mLength = 1;
        this.mKey = new int[this.mLength];
        this.mValue = new String[this.mLength];
        this.mKey[0] = key;
        this.mValue[0] = value;
        this.mEngineeringInfoMap.put(Integer.valueOf(key), value);
    }

    public EngineeringInfo(HashMap<Integer, String> map) {
        this.mKey = null;
        this.mValue = null;
        this.mEngineeringInfoMap = new HashMap();
        this.mEngineeringInfoMap = map;
        this.mLength = this.mEngineeringInfoMap.size();
        Integer[] key = (Integer[]) this.mEngineeringInfoMap.keySet().toArray();
        String[] value = (String[]) this.mEngineeringInfoMap.values().toArray();
        for (int i = 0; i < this.mLength; i++) {
            this.mKey[i] = key[i].intValue();
            this.mValue[i] = value[i];
        }
    }

    public EngineeringInfo(int length, ArrayList<Integer> keys, ArrayList<String> values) {
        this.mKey = null;
        this.mValue = null;
        this.mEngineeringInfoMap = new HashMap();
        this.mLength = length;
        this.mKey = new int[this.mLength];
        this.mValue = new String[this.mLength];
        for (int i = 0; i < this.mLength; i++) {
            this.mKey[i] = ((Integer) keys.get(i)).intValue();
            this.mValue[i] = (String) values.get(i);
        }
    }

    private EngineeringInfo(Parcel in) {
        int i;
        this.mKey = null;
        this.mValue = null;
        this.mEngineeringInfoMap = new HashMap();
        this.mLength = in.readInt();
        Log.d("EngineeringInfo", "mLength = " + this.mLength);
        if (this.mLength > 0) {
            this.mKey = new int[this.mLength];
            for (i = 0; i < this.mLength; i++) {
                this.mKey[i] = in.readInt();
            }
        }
        this.mValue = in.readStringArray();
        for (i = 0; i < this.mLength; i++) {
            this.mEngineeringInfoMap.put(Integer.valueOf(this.mKey[i]), this.mValue[i]);
        }
    }

    public int describeContents() {
        return 0;
    }

    public int getLength() {
        return this.mLength;
    }

    public int[] getKey() {
        for (int i = 0; i < this.mLength; i++) {
            Log.d("EngineeringInfo", "mKey[" + i + "] = " + this.mKey[i]);
        }
        return this.mKey;
    }

    public String[] getValue() {
        for (int i = 0; i < this.mLength; i++) {
            Log.d("EngineeringInfo", "mValue[" + i + "] = " + this.mValue[i]);
        }
        return this.mValue;
    }

    public HashMap<Integer, String> getEngineeringInfoMap() {
        return this.mEngineeringInfoMap;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeIntArray(this.mKey);
        out.writeStringArray(this.mValue);
    }
}
