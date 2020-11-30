package com.st.android.nfc_extensions;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class DefaultRouteEntry implements Parcelable {
    public static final Parcelable.Creator<DefaultRouteEntry> CREATOR = new Parcelable.Creator<DefaultRouteEntry>() {
        /* class com.st.android.nfc_extensions.DefaultRouteEntry.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DefaultRouteEntry createFromParcel(Parcel source) {
            return new DefaultRouteEntry(source.readString(), source.readString());
        }

        @Override // android.os.Parcelable.Creator
        public DefaultRouteEntry[] newArray(int size) {
            return new DefaultRouteEntry[size];
        }
    };
    private static final String TAG = "Nfc_DefaultRouteEntry";
    String routeLoc;
    String routeName;

    public DefaultRouteEntry(String name, String loc) {
        Log.d(TAG, "DefaultRouteEntry(constructor) - name: " + name + ", loc: " + loc);
        this.routeName = name;
        this.routeLoc = loc;
    }

    public String getRouteName() {
        return this.routeName;
    }

    public String getRouteLoc() {
        return this.routeLoc;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.routeName);
        dest.writeString(this.routeLoc);
    }
}
