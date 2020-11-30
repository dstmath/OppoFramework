package androidx.media;

import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

public final class SessionToken2 {
    private final String mId;
    private final String mPackageName;
    private final String mServiceName;
    private final MediaSessionCompat.Token mSessionCompatToken;
    private final int mType;
    private final int mUid;

    public int hashCode() {
        return this.mType + (31 * (this.mUid + ((this.mPackageName.hashCode() + ((this.mId.hashCode() + ((this.mServiceName != null ? this.mServiceName.hashCode() : 0) * 31)) * 31)) * 31)));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SessionToken2)) {
            return false;
        }
        SessionToken2 other = (SessionToken2) obj;
        if (this.mUid != other.mUid || !TextUtils.equals(this.mPackageName, other.mPackageName) || !TextUtils.equals(this.mServiceName, other.mServiceName) || !TextUtils.equals(this.mId, other.mId) || this.mType != other.mType) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "SessionToken {pkg=" + this.mPackageName + " id=" + this.mId + " type=" + this.mType + " service=" + this.mServiceName + " sessionCompatToken=" + this.mSessionCompatToken + "}";
    }

    /* access modifiers changed from: package-private */
    public MediaSessionCompat.Token getSessionCompatToken() {
        return this.mSessionCompatToken;
    }
}
