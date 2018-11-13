package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SdpDipRecord implements Parcelable {
    public static final Creator CREATOR = new Creator() {
        public SdpDipRecord createFromParcel(Parcel in) {
            return new SdpDipRecord(in);
        }

        public SdpDipRecord[] newArray(int size) {
            return new SdpDipRecord[size];
        }
    };
    private final String mClientExecutableUrl;
    private final String mDocumentationUrl;
    private final boolean mPrimaryRecord;
    private final int mProductId;
    private final String mServiceDescription;
    private final int mSpecificationId;
    private final int mVendorId;
    private final int mVendorIdSource;
    private final int mVersion;

    public SdpDipRecord(int specificationId, int vendorId, int vendorIdSource, int productId, int version, boolean primaryRecord, String clientExecutableUrl, String serviceDescription, String documentationUrl) {
        this.mSpecificationId = specificationId;
        this.mVendorId = vendorId;
        this.mVendorIdSource = vendorIdSource;
        this.mProductId = productId;
        this.mVersion = version;
        this.mPrimaryRecord = primaryRecord;
        this.mClientExecutableUrl = clientExecutableUrl;
        this.mServiceDescription = serviceDescription;
        this.mDocumentationUrl = documentationUrl;
    }

    public SdpDipRecord(Parcel in) {
        this.mSpecificationId = in.readInt();
        this.mVendorId = in.readInt();
        this.mVendorIdSource = in.readInt();
        this.mProductId = in.readInt();
        this.mVersion = in.readInt();
        this.mPrimaryRecord = in.readBoolean();
        this.mClientExecutableUrl = in.readString();
        this.mServiceDescription = in.readString();
        this.mDocumentationUrl = in.readString();
    }

    public int getSpecificationId() {
        return this.mSpecificationId;
    }

    public int getVendorId() {
        return this.mVendorId;
    }

    public int getVendorIdSource() {
        return this.mVendorIdSource;
    }

    public int getProductId() {
        return this.mProductId;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public boolean getPrimaryRecord() {
        return this.mPrimaryRecord;
    }

    public String getClientExecutableUrl() {
        return this.mClientExecutableUrl;
    }

    public String getServiceDescription() {
        return this.mServiceDescription;
    }

    public String getDocumentationUrl() {
        return this.mDocumentationUrl;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSpecificationId);
        dest.writeInt(this.mVendorId);
        dest.writeInt(this.mVendorIdSource);
        dest.writeInt(this.mProductId);
        dest.writeInt(this.mVersion);
        dest.writeBoolean(this.mPrimaryRecord);
        dest.writeString(this.mClientExecutableUrl);
        dest.writeString(this.mServiceDescription);
        dest.writeString(this.mDocumentationUrl);
    }

    public int describeContents() {
        return 0;
    }
}
