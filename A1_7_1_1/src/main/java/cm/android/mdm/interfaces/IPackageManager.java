package cm.android.mdm.interfaces;

import android.net.Uri;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public interface IPackageManager {

    public static abstract class PackageDeleteObserver {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.interfaces.IPackageManager.PackageDeleteObserver.<init>():void, dex: 
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
        public PackageDeleteObserver() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.interfaces.IPackageManager.PackageDeleteObserver.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: cm.android.mdm.interfaces.IPackageManager.PackageDeleteObserver.<init>():void");
        }

        public abstract void packageDeleted(String str, int i);
    }

    public static abstract class PackageInstallObserver {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.interfaces.IPackageManager.PackageInstallObserver.<init>():void, dex: 
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
        public PackageInstallObserver() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.interfaces.IPackageManager.PackageInstallObserver.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: cm.android.mdm.interfaces.IPackageManager.PackageInstallObserver.<init>():void");
        }

        public abstract void packageInstalled(String str, int i);
    }

    void addAppRestriction(int i, List<String> list);

    void addDisallowUninstallApps(List<String> list);

    void clearApplicationUserData(String str);

    void deletePackage(String str, PackageDeleteObserver packageDeleteObserver, int i);

    List<String> getDisallowUninstallApps();

    List<String> getSupportMethods();

    void installPackage(Uri uri, PackageInstallObserver packageInstallObserver, int i, String str);

    void removeAppRestriction(int i);

    void removeAppRestriction(int i, List<String> list);

    void removeDisallowUninstallApps();

    void removeDisallowUninstallApps(List<String> list);

    void setAppRestriction(int i);
}
