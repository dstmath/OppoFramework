package android.support.v4.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaDescriptionCompatApi21;
import android.support.v4.media.MediaDescriptionCompatApi23;
import android.text.TextUtils;

public final class MediaDescriptionCompat implements Parcelable {
    public static final Parcelable.Creator<MediaDescriptionCompat> CREATOR = new Parcelable.Creator<MediaDescriptionCompat>() {
        /* class android.support.v4.media.MediaDescriptionCompat.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MediaDescriptionCompat createFromParcel(Parcel in) {
            if (Build.VERSION.SDK_INT < 21) {
                return new MediaDescriptionCompat(in);
            }
            return MediaDescriptionCompat.fromMediaDescription(MediaDescriptionCompatApi21.fromParcel(in));
        }

        @Override // android.os.Parcelable.Creator
        public MediaDescriptionCompat[] newArray(int size) {
            return new MediaDescriptionCompat[size];
        }
    };
    private final CharSequence mDescription;
    private Object mDescriptionObj;
    private final Bundle mExtras;
    private final Bitmap mIcon;
    private final Uri mIconUri;
    private final String mMediaId;
    private final Uri mMediaUri;
    private final CharSequence mSubtitle;
    private final CharSequence mTitle;

    MediaDescriptionCompat(String mediaId, CharSequence title, CharSequence subtitle, CharSequence description, Bitmap icon, Uri iconUri, Bundle extras, Uri mediaUri) {
        this.mMediaId = mediaId;
        this.mTitle = title;
        this.mSubtitle = subtitle;
        this.mDescription = description;
        this.mIcon = icon;
        this.mIconUri = iconUri;
        this.mExtras = extras;
        this.mMediaUri = mediaUri;
    }

    MediaDescriptionCompat(Parcel in) {
        this.mMediaId = in.readString();
        this.mTitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mSubtitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mIcon = (Bitmap) in.readParcelable(null);
        this.mIconUri = (Uri) in.readParcelable(null);
        this.mExtras = in.readBundle();
        this.mMediaUri = (Uri) in.readParcelable(null);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (Build.VERSION.SDK_INT < 21) {
            dest.writeString(this.mMediaId);
            TextUtils.writeToParcel(this.mTitle, dest, flags);
            TextUtils.writeToParcel(this.mSubtitle, dest, flags);
            TextUtils.writeToParcel(this.mDescription, dest, flags);
            dest.writeParcelable(this.mIcon, flags);
            dest.writeParcelable(this.mIconUri, flags);
            dest.writeBundle(this.mExtras);
            dest.writeParcelable(this.mMediaUri, flags);
            return;
        }
        MediaDescriptionCompatApi21.writeToParcel(getMediaDescription(), dest, flags);
    }

    public String toString() {
        return ((Object) this.mTitle) + ", " + ((Object) this.mSubtitle) + ", " + ((Object) this.mDescription);
    }

    public Object getMediaDescription() {
        if (this.mDescriptionObj != null || Build.VERSION.SDK_INT < 21) {
            return this.mDescriptionObj;
        }
        Object bob = MediaDescriptionCompatApi21.Builder.newInstance();
        MediaDescriptionCompatApi21.Builder.setMediaId(bob, this.mMediaId);
        MediaDescriptionCompatApi21.Builder.setTitle(bob, this.mTitle);
        MediaDescriptionCompatApi21.Builder.setSubtitle(bob, this.mSubtitle);
        MediaDescriptionCompatApi21.Builder.setDescription(bob, this.mDescription);
        MediaDescriptionCompatApi21.Builder.setIconBitmap(bob, this.mIcon);
        MediaDescriptionCompatApi21.Builder.setIconUri(bob, this.mIconUri);
        Bundle extras = this.mExtras;
        if (Build.VERSION.SDK_INT < 23 && this.mMediaUri != null) {
            if (extras == null) {
                extras = new Bundle();
                extras.putBoolean("android.support.v4.media.description.NULL_BUNDLE_FLAG", true);
            }
            extras.putParcelable("android.support.v4.media.description.MEDIA_URI", this.mMediaUri);
        }
        MediaDescriptionCompatApi21.Builder.setExtras(bob, extras);
        if (Build.VERSION.SDK_INT >= 23) {
            MediaDescriptionCompatApi23.Builder.setMediaUri(bob, this.mMediaUri);
        }
        this.mDescriptionObj = MediaDescriptionCompatApi21.Builder.build(bob);
        return this.mDescriptionObj;
    }

    public static MediaDescriptionCompat fromMediaDescription(Object descriptionObj) {
        Uri mediaUri = null;
        if (descriptionObj == null || Build.VERSION.SDK_INT < 21) {
            return null;
        }
        Builder bob = new Builder();
        bob.setMediaId(MediaDescriptionCompatApi21.getMediaId(descriptionObj));
        bob.setTitle(MediaDescriptionCompatApi21.getTitle(descriptionObj));
        bob.setSubtitle(MediaDescriptionCompatApi21.getSubtitle(descriptionObj));
        bob.setDescription(MediaDescriptionCompatApi21.getDescription(descriptionObj));
        bob.setIconBitmap(MediaDescriptionCompatApi21.getIconBitmap(descriptionObj));
        bob.setIconUri(MediaDescriptionCompatApi21.getIconUri(descriptionObj));
        Bundle extras = MediaDescriptionCompatApi21.getExtras(descriptionObj);
        if (extras != null) {
            mediaUri = (Uri) extras.getParcelable("android.support.v4.media.description.MEDIA_URI");
        }
        if (mediaUri != null) {
            if (!extras.containsKey("android.support.v4.media.description.NULL_BUNDLE_FLAG") || extras.size() != 2) {
                extras.remove("android.support.v4.media.description.MEDIA_URI");
                extras.remove("android.support.v4.media.description.NULL_BUNDLE_FLAG");
            } else {
                extras = null;
            }
        }
        bob.setExtras(extras);
        if (mediaUri != null) {
            bob.setMediaUri(mediaUri);
        } else if (Build.VERSION.SDK_INT >= 23) {
            bob.setMediaUri(MediaDescriptionCompatApi23.getMediaUri(descriptionObj));
        }
        MediaDescriptionCompat descriptionCompat = bob.build();
        descriptionCompat.mDescriptionObj = descriptionObj;
        return descriptionCompat;
    }

    public static final class Builder {
        private CharSequence mDescription;
        private Bundle mExtras;
        private Bitmap mIcon;
        private Uri mIconUri;
        private String mMediaId;
        private Uri mMediaUri;
        private CharSequence mSubtitle;
        private CharSequence mTitle;

        public Builder setMediaId(String mediaId) {
            this.mMediaId = mediaId;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Builder setSubtitle(CharSequence subtitle) {
            this.mSubtitle = subtitle;
            return this;
        }

        public Builder setDescription(CharSequence description) {
            this.mDescription = description;
            return this;
        }

        public Builder setIconBitmap(Bitmap icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder setIconUri(Uri iconUri) {
            this.mIconUri = iconUri;
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public Builder setMediaUri(Uri mediaUri) {
            this.mMediaUri = mediaUri;
            return this;
        }

        public MediaDescriptionCompat build() {
            return new MediaDescriptionCompat(this.mMediaId, this.mTitle, this.mSubtitle, this.mDescription, this.mIcon, this.mIconUri, this.mExtras, this.mMediaUri);
        }
    }
}
