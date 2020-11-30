package com.aiunit.aon.utils.core;

import android.os.Build;
import android.os.MemoryFile;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.SharedMemory;
import android.system.ErrnoException;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class MemShare implements Parcelable {
    public static final Parcelable.Creator<MemShare> CREATOR = new Parcelable.Creator<MemShare>() {
        /* class com.aiunit.aon.utils.core.MemShare.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MemShare createFromParcel(Parcel parcel) {
            return new MemShare(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MemShare[] newArray(int i) {
            return new MemShare[i];
        }
    };
    private static final String DEFAULT_MEMORY_NAME = "MemShare";
    private static final int SHARE_DATA_SIZE = 819200;
    private static final String TAG = "MemShare";
    private static final int TYPE_MEMORY_FILE = 2;
    private static final int TYPE_SHARED_MEMORY = 1;
    private byte[] mData;
    private int mDataLen;
    private ByteBuffer mMapping;
    private MemoryFile mMemoryFile;
    private ParcelFileDescriptor mPfd;
    private SharedMemory mSharedMemory;
    private int mSharedType;

    public MemShare() {
        this.mDataLen = 0;
    }

    private MemShare(Parcel parcel) {
        this.mDataLen = 0;
        readFromParcel(parcel);
    }

    private int checkSharedType() {
        return Build.VERSION.SDK_INT > 26 ? 1 : 2;
    }

    private ParcelFileDescriptor getPfd() {
        try {
            return ParcelFileDescriptor.dup((FileDescriptor) MemoryFile.class.getDeclaredMethod("getFileDescriptor", new Class[0]).invoke(this.mMemoryFile, new Object[0]));
        } catch (NoSuchMethodException e) {
            Log.w("MemShare", "NoSuchMethodException " + e.getMessage());
            return null;
        } catch (InvocationTargetException e2) {
            Log.w("MemShare", "InvocationTargetException " + e2.getMessage());
            return null;
        } catch (IllegalAccessException e3) {
            Log.w("MemShare", "IllegalAccessException " + e3.getMessage());
            return null;
        } catch (IOException e4) {
            Log.w("MemShare", "IOException " + e4.getMessage());
            return null;
        }
    }

    private void readFromMemoryFile(Parcel r7) {
    }

    private void readFromParcel(Parcel parcel) {
        this.mSharedType = parcel.readInt();
        this.mDataLen = parcel.readInt();
        Log.d("MemShare", "readFromParcel type=" + this.mSharedType + ", dataLen=" + this.mDataLen);
        if (this.mDataLen == 0) {
            Log.w("MemShare", "readFromParcel data len is 0");
            return;
        }
        int i = this.mSharedType;
        if (i == 1) {
            readFromSharedMemory(parcel);
        } else if (i == 2) {
            readFromMemoryFile(parcel);
        } else {
            Log.w("MemShare", "memory share type error " + this.mSharedType);
        }
    }

    private void readFromSharedMemory(Parcel parcel) {
        this.mSharedMemory = (SharedMemory) parcel.readParcelable(SharedMemory.class.getClassLoader());
        SharedMemory sharedMemory = this.mSharedMemory;
        if (sharedMemory == null) {
            Log.w("MemShare", "readParcelable error");
            return;
        }
        try {
            this.mMapping = sharedMemory.mapReadOnly();
            if (this.mMapping != null) {
                int size = this.mSharedMemory.getSize();
                this.mData = new byte[size];
                for (int i = 0; i < size; i++) {
                    this.mData[i] = this.mMapping.get(i);
                }
            }
        } catch (ErrnoException e) {
            Log.w("MemShare", "ErrnoException " + e.getMessage());
        } finally {
            close();
        }
    }

    private void writeToMemoryFile(Parcel parcel) {
        try {
            this.mMemoryFile = new MemoryFile("MemShare", this.mDataLen);
            this.mMemoryFile.writeBytes(this.mData, 0, 0, this.mDataLen);
            this.mPfd = getPfd();
            if (this.mPfd == null) {
                Log.w("MemShare", "getPfd null");
                return;
            }
        } catch (IOException e) {
            Log.w("MemShare", "create memory file error");
        }
        parcel.writeFileDescriptor(this.mPfd.getFileDescriptor());
    }

    private void writeToSharedMemory(Parcel parcel, int i) {
        try {
            this.mSharedMemory = SharedMemory.create("MemShare", this.mDataLen);
            this.mMapping = this.mSharedMemory.mapReadWrite();
            this.mMapping.put(this.mData);
        } catch (ErrnoException e) {
            Log.e("MemShare", "ErrnoException " + e.getMessage());
        }
        parcel.writeParcelable(this.mSharedMemory, i);
    }

    public void close() {
        if (this.mSharedType == 1 && this.mSharedMemory != null) {
            SharedMemory sharedMemory = this.mSharedMemory;
            SharedMemory.unmap(this.mMapping);
            this.mSharedMemory.close();
            this.mMapping = null;
            this.mSharedMemory = null;
        } else if (this.mSharedType == 2) {
            MemoryFile memoryFile = this.mMemoryFile;
            if (memoryFile != null) {
                memoryFile.close();
                this.mMemoryFile = null;
            }
            ParcelFileDescriptor parcelFileDescriptor = this.mPfd;
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    Log.w("MemShare", "fd close error");
                }
                this.mPfd = null;
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        Log.d("MemShare", "finalize");
        close();
        super.finalize();
    }

    public byte[] getData() {
        byte[] bArr = this.mData;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public void release() {
        close();
        this.mData = null;
    }

    public void setData(byte[] bArr) {
        if (bArr == null) {
            this.mData = null;
            this.mDataLen = 0;
            return;
        }
        this.mData = (byte[]) bArr.clone();
        this.mDataLen = bArr.length;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        this.mSharedType = checkSharedType();
        Log.d("MemShare", "writeToParcel memory share type " + this.mSharedType);
        parcel.writeInt(this.mSharedType);
        parcel.writeInt(this.mDataLen);
        if (this.mDataLen == 0) {
            Log.w("MemShare", "writeToParcel data size is 0");
            return;
        }
        int i2 = this.mSharedType;
        if (i2 == 1) {
            writeToSharedMemory(parcel, i);
        } else if (i2 == 2) {
            writeToMemoryFile(parcel);
        }
    }
}
