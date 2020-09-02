package com.mediatek.internal.telephony;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class MtkSubscriptionInfo extends SubscriptionInfo {
    public static final Parcelable.Creator<MtkSubscriptionInfo> CREATOR = new Parcelable.Creator<MtkSubscriptionInfo>() {
        /* class com.mediatek.internal.telephony.MtkSubscriptionInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkSubscriptionInfo createFromParcel(Parcel source) {
            int id = source.readInt();
            String iccId = source.readString();
            int simSlotIndex = source.readInt();
            CharSequence displayName = source.readCharSequence();
            CharSequence carrierName = source.readCharSequence();
            int nameSource = source.readInt();
            int iconTint = source.readInt();
            String number = source.readString();
            int dataRoaming = source.readInt();
            String mcc = source.readString();
            String mnc = source.readString();
            String countryIso = source.readString();
            source.readInt();
            source.readInt();
            Bitmap iconBitmap = (Bitmap) source.readParcelable(Bitmap.class.getClassLoader());
            boolean isEmbedded = source.readBoolean();
            UiccAccessRule[] accessRules = (UiccAccessRule[]) source.createTypedArray(UiccAccessRule.CREATOR);
            String cardString = source.readString();
            int cardId = source.readInt();
            boolean isOpportunistic = source.readBoolean();
            String groupUUID = source.readString();
            boolean isGroupDisabled = source.readBoolean();
            int carrierid = source.readInt();
            int profileClass = source.readInt();
            int subType = source.readInt();
            String[] ehplmns = source.readStringArray();
            String[] hplmns = source.readStringArray();
            MtkSubscriptionInfo info = new MtkSubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso, isEmbedded, accessRules, cardString, cardId, isOpportunistic, groupUUID, isGroupDisabled, carrierid, profileClass, subType, source.readString());
            info.setAssociatedPlmns(ehplmns, hplmns);
            return info;
        }

        @Override // android.os.Parcelable.Creator
        public MtkSubscriptionInfo[] newArray(int size) {
            return new MtkSubscriptionInfo[size];
        }
    };
    private static final boolean IS_DEBUG_BUILD = (Build.TYPE.equals("eng") || Build.TYPE.equals("userdebug"));
    private static final String LOG_TAG = "MtkSubscriptionInfo";
    private static final int TEXT_SIZE = 16;
    private Bitmap mIconBitmap;

    public MtkSubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, String mcc, String mnc, String countryIso, boolean isEmbedded, UiccAccessRule[] accessRules, String cardString) {
        super(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, roaming, icon, mcc, mnc, countryIso, isEmbedded, accessRules, cardString);
        this.mIconBitmap = icon;
    }

    public MtkSubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, String mcc, String mnc, String countryIso, boolean isEmbedded, UiccAccessRule[] accessRules, String cardString, boolean isOpportunistic, String groupUUID, int carrierId, int profileClass) {
        super(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, roaming, icon, mcc, mnc, countryIso, isEmbedded, accessRules, cardString, isOpportunistic, groupUUID, carrierId, profileClass);
        this.mIconBitmap = icon;
    }

    public MtkSubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, String mcc, String mnc, String countryIso, boolean isEmbedded, UiccAccessRule[] accessRules, String cardString, int cardId, boolean isOpportunistic, String groupUUID, boolean isGroupDisabled, int carrierId, int profileClass, int subType, String groupOwner) {
        super(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, roaming, icon, mcc, mnc, countryIso, isEmbedded, accessRules, cardString, cardId, isOpportunistic, groupUUID, isGroupDisabled, carrierId, profileClass, subType, groupOwner);
        this.mIconBitmap = icon;
    }

    public MtkSubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, String mcc, String mnc, String countryIso, boolean isEmbedded, UiccAccessRule[] accessRules, String cardString, int cardId, boolean isOpportunistic, String groupUUID, boolean isGroupDisabled, int carrierId, int profileClass, int subType, String groupOwner, int status, int nwMode) {
        super(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, roaming, icon, mcc, mnc, countryIso, isEmbedded, accessRules, cardString, cardId, isOpportunistic, groupUUID, isGroupDisabled, carrierId, profileClass, subType, groupOwner, status, nwMode);
        this.mIconBitmap = icon;
    }

    public Bitmap createIconBitmap(Context context) {
        return createIconBitmap(context, -1, true);
    }

    public Bitmap createIconBitmap(Context context, int color) {
        return createIconBitmap(context, color, true);
    }

    public Bitmap createIconBitmap(Context context, int color, boolean showSlotIndex) {
        Bitmap workingBitmap;
        synchronized (this) {
            int width = this.mIconBitmap.getWidth();
            int height = this.mIconBitmap.getHeight();
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            if (IS_DEBUG_BUILD) {
                Rlog.d(LOG_TAG, "mIconBitmap width:" + width + " height:" + height + " metrics:" + metrics.toString());
            }
            workingBitmap = Bitmap.createBitmap(metrics, width, height, this.mIconBitmap.getConfig());
            Canvas canvas = new Canvas(workingBitmap);
            Paint paint = new Paint();
            paint.setColorFilter(new PorterDuffColorFilter(color == -1 ? getIconTint() : color, PorterDuff.Mode.SRC_ATOP));
            canvas.drawBitmap(this.mIconBitmap, 0.0f, 0.0f, paint);
            paint.setColorFilter(null);
            if (showSlotIndex) {
                paint.setAntiAlias(true);
                paint.setTypeface(Typeface.create("sans-serif", 0));
                paint.setColor(-1);
                paint.setTextSize(metrics.density * 16.0f);
                String index = String.format("%d", Integer.valueOf(getSimSlotIndex() + 1));
                Rect textBound = new Rect();
                paint.getTextBounds(index, 0, 1, textBound);
                canvas.drawText(index, (((float) width) / 2.0f) - ((float) textBound.centerX()), (((float) height) / 2.0f) - ((float) textBound.centerY()), paint);
            }
        }
        return workingBitmap;
    }

    public void writeToParcel(Parcel dest, int flags) {
        synchronized (this) {
            super.writeToParcel(dest, flags);
        }
    }

    public int describeContents() {
        return 0;
    }

    public String getCountryIso() {
        String tmpCountryIso = super.getCountryIso();
        if (TextUtils.isEmpty(tmpCountryIso)) {
            return TelephonyManager.getDefault().getSimCountryIso(super.getSubscriptionId());
        }
        return tmpCountryIso;
    }

    public static boolean isPrintableFullIccId() {
        return true;
    }
}
