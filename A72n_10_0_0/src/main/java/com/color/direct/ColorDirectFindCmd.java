package com.color.direct;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.color.direct.IColorDirectFindCallback;
import com.color.util.ColorLog;

public class ColorDirectFindCmd implements Parcelable {
    public static final Parcelable.Creator<ColorDirectFindCmd> CREATOR = new Parcelable.Creator<ColorDirectFindCmd>() {
        /* class com.color.direct.ColorDirectFindCmd.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorDirectFindCmd createFromParcel(Parcel in) {
            return new ColorDirectFindCmd(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorDirectFindCmd[] newArray(int size) {
            return new ColorDirectFindCmd[size];
        }
    };
    public static final String EXTRA_CMD = "direct_find_cmd";
    public static final String EXTRA_ID_NAMES = "id_names";
    private static final String TAG = "ColorDirectFindCmd";
    private final Bundle mBundle = new Bundle();
    private IColorDirectFindCallback mCallback = null;

    public ColorDirectFindCmd() {
    }

    public ColorDirectFindCmd(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        return "Cmd=" + this.mBundle;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        this.mBundle.writeToParcel(out, flags);
        if (this.mCallback != null) {
            out.writeInt(1);
            out.writeStrongBinder(this.mCallback.asBinder());
            return;
        }
        out.writeInt(0);
    }

    public void readFromParcel(Parcel in) {
        this.mBundle.readFromParcel(in);
        if (in.readInt() == 1) {
            this.mCallback = IColorDirectFindCallback.Stub.asInterface(in.readStrongBinder());
        }
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public void putCommand(String cmd) {
        this.mBundle.putString(EXTRA_CMD, cmd);
    }

    public ColorDirectFindCmds getCommand() {
        try {
            ColorDirectFindCmds cmd = ColorDirectFindCmds.valueOf(this.mBundle.getString(EXTRA_CMD));
            if (cmd != null) {
                return cmd;
            }
        } catch (Exception e) {
            ColorLog.e(TAG, "getCommand ERROR : " + Log.getStackTraceString(e));
            if (0 != 0) {
                return null;
            }
        } catch (Throwable th) {
            if (0 == 0) {
                ColorDirectFindCmds cmd2 = ColorDirectFindCmds.UNKNOWN;
            }
            throw th;
        }
        return ColorDirectFindCmds.UNKNOWN;
    }

    public void setCallback(IColorDirectFindCallback callback) {
        this.mCallback = callback;
    }

    public IColorDirectFindCallback getCallback() {
        return this.mCallback;
    }
}
