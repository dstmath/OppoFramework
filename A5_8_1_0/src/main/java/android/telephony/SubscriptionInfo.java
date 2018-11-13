package android.telephony;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.DisplayMetrics;
import java.util.Arrays;

public class SubscriptionInfo implements Parcelable {
    public static final Creator<SubscriptionInfo> CREATOR = new Creator<SubscriptionInfo>() {
        public SubscriptionInfo createFromParcel(Parcel source) {
            return new SubscriptionInfo(source.readInt(), source.readString(), source.readInt(), source.readCharSequence(), source.readCharSequence(), source.readInt(), source.readInt(), source.readString(), source.readInt(), (Bitmap) Bitmap.CREATOR.createFromParcel(source), source.readInt(), source.readInt(), source.readString(), source.readBoolean(), (UiccAccessRule[]) source.createTypedArray(UiccAccessRule.CREATOR));
        }

        public SubscriptionInfo[] newArray(int size) {
            return new SubscriptionInfo[size];
        }
    };
    private static final int TEXT_SIZE = 16;
    private UiccAccessRule[] mAccessRules;
    private CharSequence mCarrierName;
    private String mCountryIso;
    private int mDataRoaming;
    private CharSequence mDisplayName;
    private String mIccId;
    private Bitmap mIconBitmap;
    private int mIconTint;
    private int mId;
    private boolean mIsEmbedded;
    private int mMcc;
    private int mMnc;
    private int mNameSource;
    private String mNumber;
    private int mSimSlotIndex;

    public SubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, int mcc, int mnc, String countryIso) {
        this(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, roaming, icon, mcc, mnc, countryIso, false, null);
    }

    public SubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, int mcc, int mnc, String countryIso, boolean isEmbedded, UiccAccessRule[] accessRules) {
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
        this.mIsEmbedded = isEmbedded;
        this.mAccessRules = accessRules;
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

    public boolean isEmbedded() {
        return this.mIsEmbedded;
    }

    public boolean canManageSubscription(Context context) {
        return canManageSubscription(context, context.getPackageName());
    }

    public boolean canManageSubscription(Context context, String packageName) {
        if (!isEmbedded()) {
            throw new UnsupportedOperationException("Not an embedded subscription");
        } else if (this.mAccessRules == null) {
            return false;
        } else {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 64);
                for (UiccAccessRule rule : this.mAccessRules) {
                    if (rule.getCarrierPrivilegeStatus(packageInfo) == 1) {
                        return true;
                    }
                }
                return false;
            } catch (NameNotFoundException e) {
                throw new IllegalArgumentException("Unknown package: " + packageName, e);
            }
        }
    }

    public UiccAccessRule[] getAccessRules() {
        if (isEmbedded()) {
            return this.mAccessRules;
        }
        throw new UnsupportedOperationException("Not an embedded subscription");
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
        dest.writeBoolean(this.mIsEmbedded);
        dest.writeTypedArray(this.mAccessRules, flags);
    }

    public int describeContents() {
        return 0;
    }

    public static String givePrintableIccid(String iccId) {
        if (iccId == null) {
            return null;
        }
        if (iccId.length() <= 9 || (Build.IS_DEBUGGABLE ^ 1) == 0) {
            return iccId;
        }
        return iccId.substring(0, 9) + Rlog.pii(false, iccId.substring(9));
    }

    public String toString() {
        return "{id=" + this.mId + ", iccId=" + givePrintableIccid(this.mIccId) + " simSlotIndex=" + this.mSimSlotIndex + " displayName=" + this.mDisplayName + " carrierName=" + this.mCarrierName + " nameSource=" + this.mNameSource + " iconTint=" + this.mIconTint + " dataRoaming=" + this.mDataRoaming + " iconBitmap=" + this.mIconBitmap + " mcc " + this.mMcc + " mnc " + this.mMnc + " isEmbedded " + this.mIsEmbedded + " accessRules " + Arrays.toString(this.mAccessRules) + "}";
    }
}
