package android.security.keystore.recovery;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;

@SystemApi
public final class WrappedApplicationKey implements Parcelable {
    public static final Parcelable.Creator<WrappedApplicationKey> CREATOR = new Parcelable.Creator<WrappedApplicationKey>() {
        /* class android.security.keystore.recovery.WrappedApplicationKey.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WrappedApplicationKey createFromParcel(Parcel in) {
            return new WrappedApplicationKey(in);
        }

        @Override // android.os.Parcelable.Creator
        public WrappedApplicationKey[] newArray(int length) {
            return new WrappedApplicationKey[length];
        }
    };
    /* access modifiers changed from: private */
    public String mAlias;
    /* access modifiers changed from: private */
    public byte[] mEncryptedKeyMaterial;
    /* access modifiers changed from: private */
    public byte[] mMetadata;

    public static class Builder {
        private WrappedApplicationKey mInstance = new WrappedApplicationKey();

        public Builder setAlias(String alias) {
            String unused = this.mInstance.mAlias = alias;
            return this;
        }

        public Builder setEncryptedKeyMaterial(byte[] encryptedKeyMaterial) {
            byte[] unused = this.mInstance.mEncryptedKeyMaterial = encryptedKeyMaterial;
            return this;
        }

        public Builder setMetadata(byte[] metadata) {
            byte[] unused = this.mInstance.mMetadata = metadata;
            return this;
        }

        public WrappedApplicationKey build() {
            Preconditions.checkNotNull(this.mInstance.mAlias);
            Preconditions.checkNotNull(this.mInstance.mEncryptedKeyMaterial);
            return this.mInstance;
        }
    }

    private WrappedApplicationKey() {
    }

    @Deprecated
    public WrappedApplicationKey(String alias, byte[] encryptedKeyMaterial) {
        this.mAlias = (String) Preconditions.checkNotNull(alias);
        this.mEncryptedKeyMaterial = (byte[]) Preconditions.checkNotNull(encryptedKeyMaterial);
    }

    public String getAlias() {
        return this.mAlias;
    }

    public byte[] getEncryptedKeyMaterial() {
        return this.mEncryptedKeyMaterial;
    }

    public byte[] getMetadata() {
        return this.mMetadata;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mAlias);
        out.writeByteArray(this.mEncryptedKeyMaterial);
        out.writeByteArray(this.mMetadata);
    }

    protected WrappedApplicationKey(Parcel in) {
        this.mAlias = in.readString();
        this.mEncryptedKeyMaterial = in.createByteArray();
        if (in.dataAvail() > 0) {
            this.mMetadata = in.createByteArray();
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
