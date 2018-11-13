package org.simalliance.openmobileapi.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class SmartcardError implements Parcelable {
    private static final Class[] ALLOWED_EXCEPTIONS = new Class[]{IOException.class, SecurityException.class, NoSuchElementException.class, IllegalStateException.class, IllegalArgumentException.class, UnsupportedOperationException.class, NullPointerException.class};
    public static final Creator<SmartcardError> CREATOR = new Creator<SmartcardError>() {
        public SmartcardError createFromParcel(Parcel in) {
            return new SmartcardError(in, null);
        }

        public SmartcardError[] newArray(int size) {
            return new SmartcardError[size];
        }
    };
    private String mClazz;
    private String mMessage;

    /* synthetic */ SmartcardError(Parcel in, SmartcardError -this1) {
        this(in);
    }

    public SmartcardError() {
        this.mClazz = "";
        this.mMessage = "";
    }

    private SmartcardError(Parcel in) {
        readFromParcel(in);
    }

    public void set(Exception e) throws IllegalArgumentException {
        if (e == null) {
            throw new IllegalArgumentException("Cannot set a null exception");
        }
        Class clazz = e.getClass();
        if (Arrays.asList(ALLOWED_EXCEPTIONS).contains(clazz)) {
            this.mClazz = clazz.getCanonicalName();
            this.mMessage = e.getMessage() != null ? e.getMessage() : "";
            return;
        }
        throw new IllegalArgumentException("Unexpected exception class: " + clazz.getCanonicalName());
    }

    public boolean isSet() {
        return this.mClazz != null ? this.mClazz.isEmpty() ^ 1 : false;
    }

    public void setError(Class clazz, String message) {
        this.mClazz = clazz == null ? "" : clazz.getName();
        if (message == null) {
            message = "";
        }
        this.mMessage = message;
    }

    public void throwException() throws IOException, SecurityException, NoSuchElementException, IllegalStateException, IllegalArgumentException, UnsupportedOperationException, NullPointerException {
        if (this.mClazz.equals(IOException.class.getCanonicalName())) {
            throw new IOException(this.mMessage);
        } else if (this.mClazz.equals(SecurityException.class.getCanonicalName())) {
            throw new SecurityException(this.mMessage);
        } else if (this.mClazz.equals(AccessControlException.class.getCanonicalName())) {
            throw new SecurityException(this.mMessage);
        } else if (this.mClazz.equals(NoSuchElementException.class.getCanonicalName())) {
            throw new NoSuchElementException(this.mMessage);
        } else if (this.mClazz.equals(IllegalStateException.class.getCanonicalName())) {
            throw new IllegalStateException(this.mMessage);
        } else if (this.mClazz.equals(IllegalArgumentException.class.getCanonicalName())) {
            throw new IllegalArgumentException(this.mMessage);
        } else if (this.mClazz.equals(UnsupportedOperationException.class.getCanonicalName())) {
            throw new UnsupportedOperationException(this.mMessage);
        } else if (this.mClazz.equals(NullPointerException.class.getCanonicalName())) {
            throw new NullPointerException(this.mMessage);
        } else {
            Log.wtf(getClass().getSimpleName(), "SmartcardError.throwException() finished without throwing exception. mClazz: " + this.mClazz);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mClazz);
        out.writeString(this.mMessage);
    }

    public void readFromParcel(Parcel in) {
        this.mClazz = in.readString();
        this.mMessage = in.readString();
    }
}
