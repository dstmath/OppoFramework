package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

@Deprecated
public final class BluetoothHealthAppConfiguration implements Parcelable {
    @Deprecated
    public static final Parcelable.Creator<BluetoothHealthAppConfiguration> CREATOR = new Parcelable.Creator<BluetoothHealthAppConfiguration>() {
        /* class android.bluetooth.BluetoothHealthAppConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BluetoothHealthAppConfiguration createFromParcel(Parcel in) {
            return new BluetoothHealthAppConfiguration();
        }

        @Override // android.os.Parcelable.Creator
        public BluetoothHealthAppConfiguration[] newArray(int size) {
            return new BluetoothHealthAppConfiguration[size];
        }
    };

    BluetoothHealthAppConfiguration() {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Deprecated
    public int getDataType() {
        return 0;
    }

    @Deprecated
    public String getName() {
        return null;
    }

    @Deprecated
    public int getRole() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
    }
}
