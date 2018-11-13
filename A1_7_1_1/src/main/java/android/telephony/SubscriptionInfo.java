package android.telephony;

import android.content.Context;
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
import android.util.Log;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SubscriptionInfo implements Parcelable {
    public static final Creator<SubscriptionInfo> CREATOR = null;
    private static final int TEXT_SIZE = 16;
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
    public int mNwMode;
    private int mSimSlotIndex;
    public int mStatus;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.SubscriptionInfo.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.SubscriptionInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SubscriptionInfo.<clinit>():void");
    }

    public SubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, int mcc, int mnc, String countryIso, int status, int nwMode) {
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
        this.mStatus = status;
        this.mNwMode = nwMode;
    }

    public SubscriptionInfo(int id, String iccId, int simSlotIndex, CharSequence displayName, CharSequence carrierName, int nameSource, int iconTint, String number, int roaming, Bitmap icon, int mcc, int mnc, String countryIso) {
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
    }

    public int getSubscriptionId() {
        return this.mId;
    }

    public String getIccId() {
        return this.mIccId;
    }

    public void setIccId(String iccId) {
        this.mIccId = iccId;
    }

    public int getSimSlotIndex() {
        return this.mSimSlotIndex;
    }

    public void setSimSlotIndex(int slotId) {
        this.mSimSlotIndex = slotId;
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

    public void setNameSource(int nameSource) {
        this.mNameSource = nameSource;
    }

    public Bitmap createIconBitmap(Context context) {
        return createIconBitmap(context, -1, true);
    }

    public Bitmap createIconBitmap(Context context, int color) {
        return createIconBitmap(context, color, true);
    }

    public Bitmap createIconBitmap(Context context, int color, boolean showSlotIndex) {
        int width = this.mIconBitmap.getWidth();
        int height = this.mIconBitmap.getHeight();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        width = this.mIconBitmap.getScaledWidth(metrics.densityDpi);
        height = this.mIconBitmap.getScaledHeight(metrics.densityDpi);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(this.mIconBitmap, width, height, false);
        scaledBitmap.setDensity(metrics.densityDpi);
        Bitmap workingBitmap = Bitmap.createBitmap(metrics, width, height, scaledBitmap.getConfig());
        Canvas canvas = new Canvas(workingBitmap);
        Paint paint = new Paint();
        if (color == -1) {
            color = this.mIconTint;
        }
        paint.setColorFilter(new PorterDuffColorFilter(color, Mode.SRC_ATOP));
        canvas.drawBitmap(scaledBitmap, 0.0f, 0.0f, paint);
        paint.setColorFilter(null);
        if (showSlotIndex) {
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.create("sans-serif", 0));
            paint.setColor(-1);
            paint.setTextSize(metrics.density * 16.0f);
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(this.mSimSlotIndex + 1);
            String index = String.format("%d", objArr);
            Rect textBound = new Rect();
            paint.getTextBounds(index, 0, 1, textBound);
            canvas.drawText(index, (((float) width) / 2.0f) - ((float) textBound.centerX()), (((float) height) / 2.0f) - ((float) textBound.centerY()), paint);
        }
        if (scaledBitmap != this.mIconBitmap) {
            scaledBitmap.recycle();
            Log.d("SubscriptionInfo", "recycle scaledBitmap");
        }
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

    public void setNumber(String number) {
        this.mNumber = number;
    }

    public int getDataRoaming() {
        return this.mDataRoaming;
    }

    public void setDataRoaming(int roaming) {
        this.mDataRoaming = roaming;
    }

    public int getMcc() {
        return this.mMcc;
    }

    public void setMcc(int mcc) {
        this.mMcc = mcc;
    }

    public int getMnc() {
        return this.mMnc;
    }

    public void setMnc(int mnc) {
        this.mMnc = mnc;
    }

    public String getCountryIso() {
        return this.mCountryIso;
    }

    public int getStatus() {
        return this.mStatus;
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
        dest.writeInt(this.mStatus);
        dest.writeInt(this.mNwMode);
        this.mIconBitmap.writeToParcel(dest, flags);
    }

    public int describeContents() {
        return 0;
    }

    public static String givePrintableIccid(String iccId) {
        if (iccId == null) {
            return null;
        }
        if (iccId.length() <= 9 || Build.IS_DEBUGGABLE) {
            return iccId;
        }
        return iccId.substring(0, 9) + Rlog.pii(false, iccId.substring(9));
    }

    public String toString() {
        return "{id=" + this.mId + ", iccId=" + givePrintableIccid(this.mIccId) + " simSlotIndex=" + this.mSimSlotIndex + " displayName=" + this.mDisplayName + " carrierName=" + this.mCarrierName + " nameSource=" + this.mNameSource + " iconTint=" + this.mIconTint + " dataRoaming=" + this.mDataRoaming + " iconBitmap=" + this.mIconBitmap + " mcc " + this.mMcc + " mnc " + this.mMnc + "}";
    }
}
