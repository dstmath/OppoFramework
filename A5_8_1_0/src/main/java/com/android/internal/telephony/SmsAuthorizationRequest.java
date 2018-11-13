package com.android.internal.telephony;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import com.android.internal.telephony.ISmsSecurityService.Stub;

public class SmsAuthorizationRequest implements Parcelable {
    public static Creator<SmsAuthorizationRequest> CREATOR = new Creator<SmsAuthorizationRequest>() {
        public SmsAuthorizationRequest[] newArray(int size) {
            return new SmsAuthorizationRequest[size];
        }

        public SmsAuthorizationRequest createFromParcel(Parcel source) {
            return new SmsAuthorizationRequest(source);
        }
    };
    public final String destinationAddress;
    public final String message;
    public final String packageName;
    private final ISmsSecurityService service;
    private final IBinder token;

    public SmsAuthorizationRequest(Parcel source) {
        this.service = Stub.asInterface(source.readStrongBinder());
        this.token = source.readStrongBinder();
        this.packageName = source.readString();
        this.destinationAddress = source.readString();
        this.message = source.readString();
    }

    public SmsAuthorizationRequest(ISmsSecurityService service, IBinder binderToken, String packageName, String destinationAddress, String message) {
        this.service = service;
        this.token = binderToken;
        this.packageName = packageName;
        this.destinationAddress = destinationAddress;
        this.message = message;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.service.asBinder());
        dest.writeStrongBinder(this.token);
        dest.writeString(this.packageName);
        dest.writeString(this.destinationAddress);
        dest.writeString(this.message);
    }

    public int describeContents() {
        return 0;
    }

    public void accept() throws RemoteException {
        this.service.sendResponse(this, true);
    }

    public void reject() throws RemoteException {
        this.service.sendResponse(this, false);
    }

    public IBinder getToken() {
        return this.token;
    }

    public String toString() {
        return String.format("[%s] (%s) # %s", new Object[]{this.packageName, this.destinationAddress, this.message});
    }
}
