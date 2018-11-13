package android.hardware.face;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.HashMap;

public final class CommandResult implements Parcelable {
    public static final Creator<CommandResult> CREATOR = new Creator<CommandResult>() {
        public CommandResult createFromParcel(Parcel in) {
            return new CommandResult(in, null);
        }

        public CommandResult[] newArray(int size) {
            return new CommandResult[size];
        }
    };
    private HashMap<Integer, String> mCommandResultMap;
    private int[] mKey;
    private int mLength;
    private String[] mValue;

    public class CommandResultKeySet {
        public static final int IMAGE_QUALITY = 1;
        public static final int IMAGE_SNR = 3;
        public static final int SNR_SUCCESSED = 2;
        public static final int SUCCESSED = 0;
    }

    public CommandResult(int[] key, String[] value) {
        this.mKey = null;
        this.mValue = null;
        this.mCommandResultMap = new HashMap();
        this.mLength = key.length;
        this.mKey = new int[this.mLength];
        this.mValue = new String[this.mLength];
        for (int i = 0; i < this.mLength; i++) {
            this.mCommandResultMap.put(Integer.valueOf(key[i]), value[i]);
        }
    }

    public CommandResult(HashMap<Integer, String> map) {
        this.mKey = null;
        this.mValue = null;
        this.mCommandResultMap = new HashMap();
        this.mCommandResultMap = map;
        this.mLength = this.mCommandResultMap.size();
        Integer[] key = (Integer[]) this.mCommandResultMap.keySet().toArray();
        String[] value = (String[]) this.mCommandResultMap.values().toArray();
        for (int i = 0; i < this.mLength; i++) {
            this.mKey[i] = key[i].intValue();
            this.mValue[i] = value[i];
        }
    }

    private CommandResult(Parcel in) {
        int i;
        this.mKey = null;
        this.mValue = null;
        this.mCommandResultMap = new HashMap();
        this.mLength = in.readInt();
        Log.d("CommandResult", "mLength = " + this.mLength);
        if (this.mLength > 0) {
            this.mKey = new int[this.mLength];
            for (i = 0; i < this.mLength; i++) {
                this.mKey[i] = in.readInt();
            }
        }
        this.mValue = in.readStringArray();
        for (i = 0; i < this.mLength; i++) {
            this.mCommandResultMap.put(Integer.valueOf(this.mKey[i]), this.mValue[i]);
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
            Log.d("CommandResult", "mKey[" + i + "] = " + this.mKey[i]);
        }
        return this.mKey;
    }

    public String[] getValue() {
        for (int i = 0; i < this.mLength; i++) {
            Log.d("CommandResult", "mValue[" + i + "] = " + this.mValue[i]);
        }
        return this.mValue;
    }

    public HashMap<Integer, String> getCommandResultMap() {
        return this.mCommandResultMap;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeIntArray(this.mKey);
        out.writeStringArray(this.mValue);
    }
}
