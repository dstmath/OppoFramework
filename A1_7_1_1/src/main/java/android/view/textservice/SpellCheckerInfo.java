package android.view.textservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParserException;

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
public final class SpellCheckerInfo implements Parcelable {
    public static final Creator<SpellCheckerInfo> CREATOR = null;
    private static final String TAG = null;
    private final String mId;
    private final int mLabel;
    private final ResolveInfo mService;
    private final String mSettingsActivityName;
    private final ArrayList<SpellCheckerSubtype> mSubtypes;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.textservice.SpellCheckerInfo.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.textservice.SpellCheckerInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SpellCheckerInfo.<clinit>():void");
    }

    public SpellCheckerInfo(Context context, ResolveInfo service) throws XmlPullParserException, IOException {
        this.mSubtypes = new ArrayList();
        this.mService = service;
        ServiceInfo si = service.serviceInfo;
        this.mId = new ComponentName(si.packageName, si.name).flattenToShortString();
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser = null;
        try {
            parser = si.loadXmlMetaData(pm, SpellCheckerSession.SERVICE_META_DATA);
            if (parser == null) {
                throw new XmlPullParserException("No android.view.textservice.scs meta-data");
            }
            int type;
            Resources res = pm.getResourcesForApplication(si.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            do {
                type = parser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if ("spell-checker".equals(parser.getName())) {
                TypedArray sa = res.obtainAttributes(attrs, R.styleable.SpellChecker);
                int label = sa.getResourceId(0, 0);
                String settingsActivityComponent = sa.getString(1);
                sa.recycle();
                int depth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                        if (type == 2) {
                            if ("subtype".equals(parser.getName())) {
                                TypedArray a = res.obtainAttributes(attrs, R.styleable.SpellChecker_Subtype);
                                this.mSubtypes.add(new SpellCheckerSubtype(a.getResourceId(0, 0), a.getString(1), a.getString(4), a.getString(2), a.getInt(3, 0)));
                            } else {
                                throw new XmlPullParserException("Meta-data in spell-checker does not start with subtype tag");
                            }
                        }
                    }
                }
                if (parser != null) {
                    parser.close();
                }
                this.mLabel = label;
                this.mSettingsActivityName = settingsActivityComponent;
                return;
            }
            throw new XmlPullParserException("Meta-data does not start with spell-checker tag");
        } catch (Exception e) {
            Slog.e(TAG, "Caught exception: " + e);
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
        }
    }

    public SpellCheckerInfo(Parcel source) {
        this.mSubtypes = new ArrayList();
        this.mLabel = source.readInt();
        this.mId = source.readString();
        this.mSettingsActivityName = source.readString();
        this.mService = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
        source.readTypedList(this.mSubtypes, SpellCheckerSubtype.CREATOR);
    }

    public String getId() {
        return this.mId;
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public String getPackageName() {
        return this.mService.serviceInfo.packageName;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mLabel);
        dest.writeString(this.mId);
        dest.writeString(this.mSettingsActivityName);
        this.mService.writeToParcel(dest, flags);
        dest.writeTypedList(this.mSubtypes);
    }

    public CharSequence loadLabel(PackageManager pm) {
        if (this.mLabel == 0 || pm == null) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        return pm.getText(getPackageName(), this.mLabel, this.mService.serviceInfo.applicationInfo);
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mService.loadIcon(pm);
    }

    public ServiceInfo getServiceInfo() {
        return this.mService.serviceInfo;
    }

    public String getSettingsActivity() {
        return this.mSettingsActivityName;
    }

    public int getSubtypeCount() {
        return this.mSubtypes.size();
    }

    public SpellCheckerSubtype getSubtypeAt(int index) {
        return (SpellCheckerSubtype) this.mSubtypes.get(index);
    }

    public int describeContents() {
        return 0;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "mId=" + this.mId);
        pw.println(prefix + "mSettingsActivityName=" + this.mSettingsActivityName);
        pw.println(prefix + "Service:");
        this.mService.dump(new PrintWriterPrinter(pw), prefix + "  ");
        int N = getSubtypeCount();
        for (int i = 0; i < N; i++) {
            SpellCheckerSubtype st = getSubtypeAt(i);
            pw.println(prefix + "  " + "Subtype #" + i + ":");
            pw.println(prefix + "    " + "locale=" + st.getLocale() + " languageTag=" + st.getLanguageTag());
            pw.println(prefix + "    " + "extraValue=" + st.getExtraValue());
        }
    }
}
