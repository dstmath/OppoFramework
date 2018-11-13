package android_maps_conflict_avoidance.com.google.android.gtalkservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

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
public final class Presence implements Parcelable {
    public static final Creator<Presence> CREATOR = null;
    public static final Presence OFFLINE = null;
    private boolean mAllowInvisibility;
    private boolean mAvailable;
    private int mCapabilities;
    private List<String> mDefaultStatusList;
    private List<String> mDndStatusList;
    private boolean mInvisible;
    private Show mShow;
    private String mStatus;
    private int mStatusListContentsMax;
    private int mStatusListMax;
    private int mStatusMax;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum Show {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.android.gtalkservice.Presence.Show.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.android.gtalkservice.Presence.Show.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.android.gtalkservice.Presence.Show.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.android.gtalkservice.Presence.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.android.gtalkservice.Presence.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.android.gtalkservice.Presence.<clinit>():void");
    }

    public Presence() {
        this(false, Show.NONE, null, 8);
    }

    public Presence(boolean available, Show show, String status, int caps) {
        this.mAvailable = available;
        this.mShow = show;
        this.mStatus = status;
        this.mInvisible = false;
        this.mDefaultStatusList = new ArrayList();
        this.mDndStatusList = new ArrayList();
        this.mCapabilities = caps;
    }

    public Presence(Parcel source) {
        boolean z;
        boolean z2 = true;
        setStatusMax(source.readInt());
        setStatusListMax(source.readInt());
        setStatusListContentsMax(source.readInt());
        setAllowInvisibility(source.readInt() != 0);
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        setAvailable(z);
        setShow((Show) Enum.valueOf(Show.class, source.readString()));
        this.mStatus = source.readString();
        if (source.readInt() == 0) {
            z2 = false;
        }
        setInvisible(z2);
        this.mDefaultStatusList = new ArrayList();
        source.readStringList(this.mDefaultStatusList);
        this.mDndStatusList = new ArrayList();
        source.readStringList(this.mDndStatusList);
        setCapabilities(source.readInt());
    }

    public int getStatusMax() {
        return this.mStatusMax;
    }

    public void setStatusMax(int max) {
        this.mStatusMax = max;
    }

    public int getStatusListMax() {
        return this.mStatusListMax;
    }

    public void setStatusListMax(int max) {
        this.mStatusListMax = max;
    }

    public int getStatusListContentsMax() {
        return this.mStatusListContentsMax;
    }

    public void setStatusListContentsMax(int max) {
        this.mStatusListContentsMax = max;
    }

    public boolean allowInvisibility() {
        return this.mAllowInvisibility;
    }

    public void setAllowInvisibility(boolean allowInvisibility) {
        this.mAllowInvisibility = allowInvisibility;
    }

    public boolean isAvailable() {
        return this.mAvailable;
    }

    public void setAvailable(boolean available) {
        this.mAvailable = available;
    }

    public boolean isInvisible() {
        return this.mInvisible;
    }

    public boolean setInvisible(boolean invisible) {
        this.mInvisible = invisible;
        if (!invisible || allowInvisibility()) {
            return true;
        }
        return false;
    }

    public void setShow(Show show) {
        this.mShow = show;
    }

    public int getCapabilities() {
        return this.mCapabilities;
    }

    public void setCapabilities(int capabilities) {
        this.mCapabilities = capabilities;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(getStatusMax());
        dest.writeInt(getStatusListMax());
        dest.writeInt(getStatusListContentsMax());
        dest.writeInt(allowInvisibility() ? 1 : 0);
        if (this.mAvailable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.mShow.toString());
        dest.writeString(this.mStatus);
        if (!this.mInvisible) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeStringList(this.mDefaultStatusList);
        dest.writeStringList(this.mDndStatusList);
        dest.writeInt(getCapabilities());
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        if (!isAvailable()) {
            return "UNAVAILABLE";
        }
        if (isInvisible()) {
            return "INVISIBLE";
        }
        StringBuilder sb = new StringBuilder(40);
        if (this.mShow == Show.NONE) {
            sb.append("AVAILABLE(x)");
        } else {
            sb.append(this.mShow.toString());
        }
        if ((this.mCapabilities & 8) != 0) {
            sb.append(" pmuc-v1");
        }
        if ((this.mCapabilities & 1) != 0) {
            sb.append(" voice-v1");
        }
        if ((this.mCapabilities & 2) != 0) {
            sb.append(" video-v1");
        }
        if ((this.mCapabilities & 4) != 0) {
            sb.append(" camera-v1");
        }
        return sb.toString();
    }
}
