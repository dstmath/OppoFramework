package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.io.PrintWriter;

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
public final class ComponentName implements Parcelable, Cloneable, Comparable<ComponentName> {
    public static final Creator<ComponentName> CREATOR = null;
    private final String mClass;
    private final String mPackage;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.ComponentName.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.ComponentName.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ComponentName.<clinit>():void");
    }

    public static ComponentName createRelative(String pkg, String cls) {
        if (TextUtils.isEmpty(cls)) {
            throw new IllegalArgumentException("class name cannot be empty");
        }
        String fullName;
        if (cls.charAt(0) == '.') {
            fullName = pkg + cls;
        } else {
            fullName = cls;
        }
        return new ComponentName(pkg, fullName);
    }

    public static ComponentName createRelative(Context pkg, String cls) {
        return createRelative(pkg.getPackageName(), cls);
    }

    public ComponentName(String pkg, String cls) {
        if (pkg == null) {
            throw new NullPointerException("package name is null");
        } else if (cls == null) {
            throw new NullPointerException("class name is null");
        } else {
            this.mPackage = pkg;
            this.mClass = cls;
        }
    }

    public ComponentName(Context pkg, String cls) {
        if (cls == null) {
            throw new NullPointerException("class name is null");
        }
        this.mPackage = pkg.getPackageName();
        this.mClass = cls;
    }

    public ComponentName(Context pkg, Class<?> cls) {
        this.mPackage = pkg.getPackageName();
        this.mClass = cls.getName();
    }

    public ComponentName clone() {
        return new ComponentName(this.mPackage, this.mClass);
    }

    public String getPackageName() {
        return this.mPackage;
    }

    public String getClassName() {
        return this.mClass;
    }

    public String getShortClassName() {
        if (this.mClass.startsWith(this.mPackage)) {
            int PN = this.mPackage.length();
            int CN = this.mClass.length();
            if (CN > PN && this.mClass.charAt(PN) == '.') {
                return this.mClass.substring(PN, CN);
            }
        }
        return this.mClass;
    }

    private static void appendShortClassName(StringBuilder sb, String packageName, String className) {
        if (className.startsWith(packageName)) {
            int PN = packageName.length();
            int CN = className.length();
            if (CN > PN && className.charAt(PN) == '.') {
                sb.append(className, PN, CN);
                return;
            }
        }
        sb.append(className);
    }

    private static void printShortClassName(PrintWriter pw, String packageName, String className) {
        if (className.startsWith(packageName)) {
            int PN = packageName.length();
            int CN = className.length();
            if (CN > PN && className.charAt(PN) == '.') {
                pw.write(className, PN, CN - PN);
                return;
            }
        }
        pw.print(className);
    }

    public String flattenToString() {
        return this.mPackage + "/" + this.mClass;
    }

    public String flattenToShortString() {
        StringBuilder sb = new StringBuilder(this.mPackage.length() + this.mClass.length());
        appendShortString(sb, this.mPackage, this.mClass);
        return sb.toString();
    }

    public void appendShortString(StringBuilder sb) {
        appendShortString(sb, this.mPackage, this.mClass);
    }

    public static void appendShortString(StringBuilder sb, String packageName, String className) {
        sb.append(packageName).append('/');
        appendShortClassName(sb, packageName, className);
    }

    public static void printShortString(PrintWriter pw, String packageName, String className) {
        pw.print(packageName);
        pw.print('/');
        printShortClassName(pw, packageName, className);
    }

    public static ComponentName unflattenFromString(String str) {
        int sep = str.indexOf(47);
        if (sep < 0 || sep + 1 >= str.length()) {
            return null;
        }
        String pkg = str.substring(0, sep);
        String cls = str.substring(sep + 1);
        if (cls.length() > 0 && cls.charAt(0) == '.') {
            cls = pkg + cls;
        }
        return new ComponentName(pkg, cls);
    }

    public String toShortString() {
        return "{" + this.mPackage + "/" + this.mClass + "}";
    }

    public String toString() {
        return "ComponentInfo{" + this.mPackage + "/" + this.mClass + "}";
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj != null) {
            try {
                ComponentName other = (ComponentName) obj;
                if (this.mPackage.equals(other.mPackage)) {
                    z = this.mClass.equals(other.mClass);
                }
                return z;
            } catch (ClassCastException e) {
            }
        }
        return false;
    }

    public int hashCode() {
        return this.mPackage.hashCode() + this.mClass.hashCode();
    }

    public int compareTo(ComponentName that) {
        int v = this.mPackage.compareTo(that.mPackage);
        if (v != 0) {
            return v;
        }
        return this.mClass.compareTo(that.mClass);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mPackage);
        out.writeString(this.mClass);
    }

    public static void writeToParcel(ComponentName c, Parcel out) {
        if (c != null) {
            c.writeToParcel(out, 0);
        } else {
            out.writeString(null);
        }
    }

    public static ComponentName readFromParcel(Parcel in) {
        String pkg = in.readString();
        if (pkg != null) {
            return new ComponentName(pkg, in);
        }
        return null;
    }

    public ComponentName(Parcel in) {
        this.mPackage = in.readString();
        if (this.mPackage == null) {
            throw new NullPointerException("package name is null");
        }
        this.mClass = in.readString();
        if (this.mClass == null) {
            throw new NullPointerException("class name is null");
        }
    }

    private ComponentName(String pkg, Parcel in) {
        this.mPackage = pkg;
        this.mClass = in.readString();
    }
}
