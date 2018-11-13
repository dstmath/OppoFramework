package android.telephony;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.DisplayMetrics;

public class SubInfoRecord implements Parcelable {
    public static final Creator<SubInfoRecord> CREATOR = new Creator<SubInfoRecord>() {
        public SubInfoRecord createFromParcel(Parcel source) {
            return new SubInfoRecord(source.readInt(), source.readString(), source.readInt(), source.readCharSequence(), source.readCharSequence(), source.readInt(), source.readInt(), source.readString(), source.readInt(), (Bitmap) Bitmap.CREATOR.createFromParcel(source), source.readInt(), source.readInt(), source.readString());
        }

        public SubInfoRecord[] newArray(int size) {
            return new SubInfoRecord[size];
        }
    };
    private static final int TEXT_SIZE = 16;
    public String displayName;
    public int displayNumberFormat;
    public String iccId;
    private CharSequence mCarrierName;
    private String mCountryIso;
    private int mDataRoaming;
    private CharSequence mDisplayName;
    private String mIccId;
    private Bitmap mIconBitmap;
    private int mIconTint;
    private int mId;
    private int mMcc;
    private int mMnc;
    private int mNameSource;
    private String mNumber;
    public int mOppoSubId;
    private int mSimSlotIndex;
    public int slotId;
    public int subId;

    public SubInfoRecord(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, int mcc, int mnc, String countryIso) {
        this.mId = id;
        this.mIccId = iccId;
        this.mSimSlotIndex = simSlotIndex;
        this.mDisplayName = displayName;
        this.mCarrierName = carrierName;
        this.mNameSource = nameSource;
        this.mIconTint = iconTint;
        this.mNumber = number;
        this.mDataRoaming = roaming;
        this.mIconBitmap = icon;
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mCountryIso = countryIso;
        this.mOppoSubId = id;
        this.subId = id;
        this.iccId = iccId;
        this.slotId = simSlotIndex;
        if (displayName == null) {
            this.displayName = "";
        } else {
            this.displayName = displayName.toString();
        }
    }

    public int getSubscriptionId() {
        return this.mId;
    }

    public String getIccId() {
        return this.mIccId;
    }

    public int getSimSlotIndex() {
        return this.mSimSlotIndex;
    }

    public CharSequence getDisplayName() {
        return this.mDisplayName;
    }

    public void setDisplayName(CharSequence name) {
        this.mDisplayName = name;
    }

    public CharSequence getCarrierName() {
        return this.mCarrierName;
    }

    public void setCarrierName(CharSequence name) {
        this.mCarrierName = name;
    }

    public int getNameSource() {
        return this.mNameSource;
    }

    public Bitmap createIconBitmap(Context context) {
        int width = this.mIconBitmap.getWidth();
        int height = this.mIconBitmap.getHeight();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Bitmap workingBitmap = Bitmap.createBitmap(metrics, width, height, this.mIconBitmap.getConfig());
        Canvas canvas = new Canvas(workingBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(this.mIconTint, Mode.SRC_ATOP));
        canvas.drawBitmap(this.mIconBitmap, 0.0f, 0.0f, paint);
        paint.setColorFilter(null);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.create("sans-serif", 0));
        paint.setColor(-1);
        paint.setTextSize(metrics.density * 16.0f);
        String index = String.format("%d", new Object[]{Integer.valueOf(this.mSimSlotIndex + 1)});
        Rect textBound = new Rect();
        paint.getTextBounds(index, 0, 1, textBound);
        canvas.drawText(index, (((float) width) / 2.0f) - ((float) textBound.centerX()), (((float) height) / 2.0f) - ((float) textBound.centerY()), paint);
        return workingBitmap;
    }

    public int getIconTint() {
        return this.mIconTint;
    }

    public void setIconTint(int iconTint) {
        this.mIconTint = iconTint;
    }

    public String getNumber() {
        return this.mNumber;
    }

    public int getDataRoaming() {
        return this.mDataRoaming;
    }

    public int getMcc() {
        return this.mMcc;
    }

    public int getMnc() {
        return this.mMnc;
    }

    public String getCountryIso() {
        return this.mCountryIso;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mIccId);
        dest.writeInt(this.mSimSlotIndex);
        dest.writeCharSequence(this.mDisplayName);
        dest.writeCharSequence(this.mCarrierName);
        dest.writeInt(this.mNameSource);
        dest.writeInt(this.mIconTint);
        dest.writeString(this.mNumber);
        dest.writeInt(this.mDataRoaming);
        dest.writeInt(this.mMcc);
        dest.writeInt(this.mMnc);
        dest.writeString(this.mCountryIso);
        this.mIconBitmap.writeToParcel(dest, flags);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "{id=" + this.mId + ", iccId=" + this.mIccId + " simSlotIndex=" + this.mSimSlotIndex + " displayName=" + this.mDisplayName + " carrierName=" + this.mCarrierName + " nameSource=" + this.mNameSource + " iconTint=" + this.mIconTint + " dataRoaming=" + this.mDataRoaming + " iconBitmap=" + this.mIconBitmap + " mcc " + this.mMcc + " mnc " + this.mMnc + "}";
    }
}
