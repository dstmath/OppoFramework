package com.mediatek.mmsdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import java.util.HashMap;

public class BaseParameters implements Parcelable {
    public static final String CAMERA_MM_SERVICE_BINDER_NAME = "media.mmsdk";
    public static final Parcelable.Creator<BaseParameters> CREATOR = new Parcelable.Creator<BaseParameters>() {
        /* class com.mediatek.mmsdk.BaseParameters.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BaseParameters createFromParcel(Parcel in) {
            Log.i(BaseParameters.TAG, "createFromParcel");
            return new BaseParameters(in);
        }

        @Override // android.os.Parcelable.Creator
        public BaseParameters[] newArray(int size) {
            Log.i(BaseParameters.TAG, "newArray");
            return new BaseParameters[size];
        }
    };
    public static final String KEY_CALLBACK_MSG_TYPE = "callback-msg-type";
    public static final String KEY_EFFECT_NAME_CFB = "capture_face_beauty";
    public static final String KEY_EFFECT_NAME_HDR = "hdr";
    public static final String KEY_FACE_BEAUTY_SHAPE = "fb-sharp";
    public static final String KEY_FACE_BEAUTY_SHAPE_MAX = "fb-sharp-max";
    public static final String KEY_FACE_BEAUTY_SHAPE_MIN = "fb-sharp-min";
    public static final String KEY_FACE_BEAUTY_SKIN_COLOR = "fb-skin-color";
    public static final String KEY_FACE_BEAUTY_SKIN_COLOR_MAX = "fb-skin-color-max";
    public static final String KEY_FACE_BEAUTY_SKIN_COLOR_MIN = "fb-skin-color-min";
    public static final String KEY_FACE_BEAUTY_SLIM_FACE = "fb-slim-face";
    public static final String KEY_FACE_BEAUTY_SLIM_FACE_MAX = "fb-slim-face-max";
    public static final String KEY_FACE_BEAUTY_SLIM_FACE_MIN = "fb-slim-face-min";
    public static final String KEY_FACE_BEAUTY_SMOOTH = "fb-smooth-level";
    public static final String KEY_FACE_BEAUTY_SMOOTH_MAX = "fb-smooth-level-max";
    public static final String KEY_FACE_BEAUTY_SMOOTH_MIN = "fb-smooth-level-min";
    public static final String KEY_IMAGE_FORMAT = "picture-format";
    public static final String KEY_OUT_PUT_CAPTURE_NUMBER = "picture-number";
    public static final String KEY_PICTURE_HEIGHT = "picture-height";
    public static final String KEY_PICTURE_ROTATION = "rotation";
    public static final String KEY_PICTURE_SIZE = "picture-size";
    public static final String KEY_PICTURE_WIDTH = "picture-width";
    private static final String TAG = "BaseParameters";
    private HashMap<String, String> mMap;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        Log.i(TAG, "writeToParcel");
        out.writeString(flatten());
    }

    public void readFromParcel(Parcel in) {
        Log.i(TAG, "readFromParcel");
        this.mMap = new HashMap<>(128);
        int dataSize = in.dataSize();
        int dataPosition = in.dataPosition();
        Log.i(TAG, "readFromParcel - in.dataSize " + dataSize);
        Log.i(TAG, "readFromParcel - in.dataPosition " + dataPosition);
        byte[] marshell = in.createByteArray();
        for (int i = 0; i < marshell.length; i++) {
            Log.i(TAG, i + " - " + ((int) marshell[i]) + ", " + ((char) marshell[i]));
        }
        in.setDataPosition(dataPosition);
        Log.i(TAG, "readFromParcel - in.dataPosition2 " + in.dataPosition());
        int totalSize = in.readInt();
        Log.i(TAG, "totalSize=" + totalSize);
        String string = in.readString();
        if (string != null) {
            Log.i(TAG, "readFromParcel - string=" + string);
            unflatten(string);
            return;
        }
        Log.e(TAG, "can't read string from parcel");
    }

    private BaseParameters(Parcel in) {
        Log.i(TAG, "BaseParameters(Parcel in)");
        readFromParcel(in);
    }

    public BaseParameters() {
        this.mMap = new HashMap<>(128);
    }

    public BaseParameters copy() {
        BaseParameters para = new BaseParameters();
        para.mMap = new HashMap<>(this.mMap);
        return para;
    }

    public void copyFrom(BaseParameters other) {
        if (other != null) {
            this.mMap.putAll(other.mMap);
            return;
        }
        throw new NullPointerException("other must not be null");
    }

    public boolean same(BaseParameters other) {
        if (this == other) {
            return true;
        }
        if (other == null || !this.mMap.equals(other.mMap)) {
            return false;
        }
        return true;
    }

    @Deprecated
    public void dump() {
        Log.e(TAG, "dump: size=" + this.mMap.size());
        for (String k : this.mMap.keySet()) {
            Log.e(TAG, "dump: " + k + "=" + this.mMap.get(k));
        }
    }

    public String flatten() {
        StringBuilder flattened = new StringBuilder(128);
        for (String k : this.mMap.keySet()) {
            flattened.append(k);
            flattened.append("=");
            flattened.append(this.mMap.get(k));
            flattened.append(";");
        }
        if (flattened.length() > 0) {
            flattened.deleteCharAt(flattened.length() - 1);
        }
        return flattened.toString();
    }

    public void unflatten(String flattened) {
        this.mMap.clear();
        TextUtils.StringSplitter<String> splitter = new TextUtils.SimpleStringSplitter(';');
        splitter.setString(flattened);
        for (String kv : splitter) {
            int pos = kv.indexOf(61);
            if (pos != -1) {
                this.mMap.put(kv.substring(0, pos), kv.substring(pos + 1));
            }
        }
    }

    public void remove(String key) {
        this.mMap.remove(key);
    }

    public void set(String key, String value) {
        if (key.indexOf(61) != -1 || key.indexOf(59) != -1 || key.indexOf(0) != -1) {
            Log.e(TAG, "Key \"" + key + "\" contains invalid character (= or ; or \\0)");
        } else if (value.indexOf(61) == -1 && value.indexOf(59) == -1 && value.indexOf(0) == -1) {
            put(key, value);
        } else {
            Log.e(TAG, "Value \"" + value + "\" contains invalid character (= or ; or \\0)");
        }
    }

    public void set(String key, int value) {
        put(key, Integer.toString(value));
    }

    private void put(String key, String value) {
        this.mMap.remove(key);
        this.mMap.put(key, value);
    }

    public String get(String key) {
        return this.mMap.get(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(this.mMap.get(key));
    }
}
