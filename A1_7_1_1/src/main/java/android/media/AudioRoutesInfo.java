package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

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
public class AudioRoutesInfo implements Parcelable {
    public static final Creator<AudioRoutesInfo> CREATOR = null;
    public static final int MAIN_DOCK_SPEAKERS = 4;
    public static final int MAIN_HDMI = 8;
    public static final int MAIN_HEADPHONES = 2;
    public static final int MAIN_HEADSET = 1;
    public static final int MAIN_SPEAKER = 0;
    public static final int MAIN_USB = 16;
    public CharSequence bluetoothName;
    public int mainType;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.AudioRoutesInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.AudioRoutesInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioRoutesInfo.<clinit>():void");
    }

    public AudioRoutesInfo() {
        this.mainType = 0;
    }

    public AudioRoutesInfo(AudioRoutesInfo o) {
        this.mainType = 0;
        this.bluetoothName = o.bluetoothName;
        this.mainType = o.mainType;
    }

    AudioRoutesInfo(Parcel src) {
        this.mainType = 0;
        this.bluetoothName = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(src);
        this.mainType = src.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return getClass().getSimpleName() + "{ type=" + typeToString(this.mainType) + (TextUtils.isEmpty(this.bluetoothName) ? "" : ", bluetoothName=" + this.bluetoothName) + " }";
    }

    private static String typeToString(int type) {
        if (type == 0) {
            return "SPEAKER";
        }
        if ((type & 1) != 0) {
            return "HEADSET";
        }
        if ((type & 2) != 0) {
            return "HEADPHONES";
        }
        if ((type & 4) != 0) {
            return "DOCK_SPEAKERS";
        }
        if ((type & 8) != 0) {
            return "HDMI";
        }
        if ((type & 16) != 0) {
            return "USB";
        }
        return Integer.toHexString(type);
    }

    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.bluetoothName, dest, flags);
        dest.writeInt(this.mainType);
    }
}
