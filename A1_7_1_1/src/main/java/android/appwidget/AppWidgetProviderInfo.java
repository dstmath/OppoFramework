package android.appwidget;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AppWidgetProviderInfo implements Parcelable {
    public static final Creator<AppWidgetProviderInfo> CREATOR = null;
    public static final int RESIZE_BOTH = 3;
    public static final int RESIZE_HORIZONTAL = 1;
    public static final int RESIZE_NONE = 0;
    public static final int RESIZE_VERTICAL = 2;
    public static final int WIDGET_CATEGORY_HOME_SCREEN = 1;
    public static final int WIDGET_CATEGORY_KEYGUARD = 2;
    public static final int WIDGET_CATEGORY_SEARCHBOX = 4;
    public int autoAdvanceViewId;
    public ComponentName configure;
    public int icon;
    public int initialKeyguardLayout;
    public int initialLayout;
    @Deprecated
    public String label;
    public int minHeight;
    public int minResizeHeight;
    public int minResizeWidth;
    public int minWidth;
    public int previewImage;
    public ComponentName provider;
    public ActivityInfo providerInfo;
    public int resizeMode;
    public int updatePeriodMillis;
    public int widgetCategory;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.appwidget.AppWidgetProviderInfo.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.appwidget.AppWidgetProviderInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.appwidget.AppWidgetProviderInfo.<clinit>():void");
    }

    public AppWidgetProviderInfo(Parcel in) {
        if (in.readInt() != 0) {
            this.provider = new ComponentName(in);
        }
        this.minWidth = in.readInt();
        this.minHeight = in.readInt();
        this.minResizeWidth = in.readInt();
        this.minResizeHeight = in.readInt();
        this.updatePeriodMillis = in.readInt();
        this.initialLayout = in.readInt();
        this.initialKeyguardLayout = in.readInt();
        if (in.readInt() != 0) {
            this.configure = new ComponentName(in);
        }
        this.label = in.readString();
        this.icon = in.readInt();
        this.previewImage = in.readInt();
        this.autoAdvanceViewId = in.readInt();
        this.resizeMode = in.readInt();
        this.widgetCategory = in.readInt();
        this.providerInfo = (ActivityInfo) in.readParcelable(null);
    }

    public final String loadLabel(PackageManager packageManager) {
        CharSequence label = this.providerInfo.loadLabel(packageManager);
        if (label != null) {
            return label.toString().trim();
        }
        return null;
    }

    public final Drawable loadIcon(Context context, int density) {
        return loadDrawable(context, density, this.providerInfo.getIconResource(), true);
    }

    public final Drawable loadPreviewImage(Context context, int density) {
        return loadDrawable(context, density, this.previewImage, false);
    }

    public final UserHandle getProfile() {
        return new UserHandle(UserHandle.getUserId(this.providerInfo.applicationInfo.uid));
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.provider != null) {
            out.writeInt(1);
            this.provider.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.minWidth);
        out.writeInt(this.minHeight);
        out.writeInt(this.minResizeWidth);
        out.writeInt(this.minResizeHeight);
        out.writeInt(this.updatePeriodMillis);
        out.writeInt(this.initialLayout);
        out.writeInt(this.initialKeyguardLayout);
        if (this.configure != null) {
            out.writeInt(1);
            this.configure.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        out.writeString(this.label);
        out.writeInt(this.icon);
        out.writeInt(this.previewImage);
        out.writeInt(this.autoAdvanceViewId);
        out.writeInt(this.resizeMode);
        out.writeInt(this.widgetCategory);
        out.writeParcelable(this.providerInfo, flags);
    }

    public AppWidgetProviderInfo clone() {
        String str = null;
        AppWidgetProviderInfo that = new AppWidgetProviderInfo();
        that.provider = this.provider == null ? null : this.provider.clone();
        that.minWidth = this.minWidth;
        that.minHeight = this.minHeight;
        that.minResizeWidth = this.minResizeHeight;
        that.minResizeHeight = this.minResizeHeight;
        that.updatePeriodMillis = this.updatePeriodMillis;
        that.initialLayout = this.initialLayout;
        that.initialKeyguardLayout = this.initialKeyguardLayout;
        that.configure = this.configure == null ? null : this.configure.clone();
        if (this.label != null) {
            str = this.label.substring(0);
        }
        that.label = str;
        that.icon = this.icon;
        that.previewImage = this.previewImage;
        that.autoAdvanceViewId = this.autoAdvanceViewId;
        that.resizeMode = this.resizeMode;
        that.widgetCategory = this.widgetCategory;
        that.providerInfo = this.providerInfo;
        return that;
    }

    public int describeContents() {
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x001f A:{ExcHandler: android.content.pm.PackageManager.NameNotFoundException (e android.content.pm.PackageManager$NameNotFoundException), Splitter: B:0:0x0000} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Drawable loadDrawable(Context context, int density, int resourceId, boolean loadDefaultIcon) {
        try {
            Resources resources = context.getPackageManager().getResourcesForApplication(this.providerInfo.applicationInfo);
            if (resourceId > 0) {
                if (density <= 0) {
                    density = context.getResources().getDisplayMetrics().densityDpi;
                }
                return resources.getDrawableForDensity(resourceId, density);
            }
        } catch (NameNotFoundException e) {
        }
        return loadDefaultIcon ? this.providerInfo.loadIcon(context.getPackageManager()) : null;
    }

    public String toString() {
        return "AppWidgetProviderInfo(" + getProfile() + '/' + this.provider + ')';
    }
}
