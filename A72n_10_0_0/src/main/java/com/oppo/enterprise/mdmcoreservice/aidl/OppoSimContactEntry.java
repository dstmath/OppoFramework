package com.oppo.enterprise.mdmcoreservice.aidl;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class OppoSimContactEntry implements Parcelable {
    public static final Parcelable.Creator<OppoSimContactEntry> CREATOR = new Parcelable.Creator<OppoSimContactEntry>() {
        /* class com.oppo.enterprise.mdmcoreservice.aidl.OppoSimContactEntry.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OppoSimContactEntry createFromParcel(Parcel parcel) {
            OppoSimContactEntry oppoCallLogEntry = new OppoSimContactEntry();
            oppoCallLogEntry.setContentValues((ContentValues) parcel.readParcelable(getClass().getClassLoader()));
            return oppoCallLogEntry;
        }

        @Override // android.os.Parcelable.Creator
        public OppoSimContactEntry[] newArray(int i) {
            return new OppoSimContactEntry[i];
        }
    };
    public static final String SIM_CONTACTS_ADDITION_NUMBER = "additionalNumber";
    public static final String SIM_CONTACTS_EMAIL = "emails";
    public static final String SIM_CONTACTS_ID = "_id";
    public static final String SIM_CONTACTS_NAME = "name";
    public static final String SIM_CONTACTS_NUMBER = "number";
    private ContentValues contentValues = new ContentValues();

    public ContentValuesBuilder addContentValues() {
        if (this.contentValues == null) {
            this.contentValues = new ContentValues();
        }
        return new ContentValuesBuilder(this.contentValues);
    }

    public void setContentValues(ContentValues contentValues2) {
        this.contentValues = contentValues2;
    }

    public ContentValues getContentValues() {
        if (this.contentValues == null) {
            this.contentValues = new ContentValues();
        }
        return this.contentValues;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.contentValues, i);
    }
}
