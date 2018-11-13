package android.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AppSecurityPermissions {
    private static final String TAG = "AppSecurityPermissions";
    public static final int WHICH_ALL = 65535;
    public static final int WHICH_NEW = 4;
    private static final boolean localLOGV = false;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final CharSequence mNewPermPrefix;
    private String mPackageName;
    private final PermissionInfoComparator mPermComparator;
    private final PermissionGroupInfoComparator mPermGroupComparator;
    private final Map<String, MyPermissionGroupInfo> mPermGroups;
    private final List<MyPermissionGroupInfo> mPermGroupsList;
    private final List<MyPermissionInfo> mPermsList;
    private final PackageManager mPm;

    static class MyPermissionGroupInfo extends PermissionGroupInfo {
        final ArrayList<MyPermissionInfo> mAllPermissions;
        CharSequence mLabel;
        final ArrayList<MyPermissionInfo> mNewPermissions;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.<init>(android.content.pm.PermissionGroupInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        MyPermissionGroupInfo(android.content.pm.PermissionGroupInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.<init>(android.content.pm.PermissionGroupInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.<init>(android.content.pm.PermissionGroupInfo):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.<init>(android.content.pm.PermissionInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        MyPermissionGroupInfo(android.content.pm.PermissionInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.<init>(android.content.pm.PermissionInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.<init>(android.content.pm.PermissionInfo):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.loadGroupIcon(android.content.Context, android.content.pm.PackageManager):android.graphics.drawable.Drawable, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @android.annotation.OppoHook(level = android.annotation.OppoHook.OppoHookType.CHANGE_CODE, note = "Changwei.Li@Plf.SDK : Modify for rom theme. make convertIcon useless here", property = android.annotation.OppoHook.OppoRomType.ROM)
        public android.graphics.drawable.Drawable loadGroupIcon(android.content.Context r1, android.content.pm.PackageManager r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.loadGroupIcon(android.content.Context, android.content.pm.PackageManager):android.graphics.drawable.Drawable, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.MyPermissionGroupInfo.loadGroupIcon(android.content.Context, android.content.pm.PackageManager):android.graphics.drawable.Drawable");
        }
    }

    private static class MyPermissionInfo extends PermissionInfo {
        int mExistingReqFlags;
        CharSequence mLabel;
        boolean mNew;
        int mNewReqFlags;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.AppSecurityPermissions.MyPermissionInfo.<init>(android.content.pm.PermissionInfo):void, dex: 
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
        MyPermissionInfo(android.content.pm.PermissionInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.AppSecurityPermissions.MyPermissionInfo.<init>(android.content.pm.PermissionInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.MyPermissionInfo.<init>(android.content.pm.PermissionInfo):void");
        }
    }

    private static class PermissionGroupInfoComparator implements Comparator<MyPermissionGroupInfo> {
        private final Collator sCollator;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private PermissionGroupInfoComparator() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.<init>():void");
        }

        /* synthetic */ PermissionGroupInfoComparator(PermissionGroupInfoComparator permissionGroupInfoComparator) {
            this();
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.compare(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionGroupInfo):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public final int compare(android.widget.AppSecurityPermissions.MyPermissionGroupInfo r1, android.widget.AppSecurityPermissions.MyPermissionGroupInfo r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.compare(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionGroupInfo):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.compare(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionGroupInfo):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.compare(java.lang.Object, java.lang.Object):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public /* bridge */ /* synthetic */ int compare(java.lang.Object r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.compare(java.lang.Object, java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionGroupInfoComparator.compare(java.lang.Object, java.lang.Object):int");
        }
    }

    private static class PermissionInfoComparator implements Comparator<MyPermissionInfo> {
        private final Collator sCollator;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.PermissionInfoComparator.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        PermissionInfoComparator() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.PermissionInfoComparator.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionInfoComparator.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionInfoComparator.compare(android.widget.AppSecurityPermissions$MyPermissionInfo, android.widget.AppSecurityPermissions$MyPermissionInfo):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public final int compare(android.widget.AppSecurityPermissions.MyPermissionInfo r1, android.widget.AppSecurityPermissions.MyPermissionInfo r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionInfoComparator.compare(android.widget.AppSecurityPermissions$MyPermissionInfo, android.widget.AppSecurityPermissions$MyPermissionInfo):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionInfoComparator.compare(android.widget.AppSecurityPermissions$MyPermissionInfo, android.widget.AppSecurityPermissions$MyPermissionInfo):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.PermissionInfoComparator.compare(java.lang.Object, java.lang.Object):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public /* bridge */ /* synthetic */ int compare(java.lang.Object r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.PermissionInfoComparator.compare(java.lang.Object, java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionInfoComparator.compare(java.lang.Object, java.lang.Object):int");
        }
    }

    public static class PermissionItemView extends LinearLayout implements OnClickListener {
        AlertDialog mDialog;
        MyPermissionGroupInfo mGroup;
        private String mPackageName;
        MyPermissionInfo mPerm;
        private boolean mShowRevokeUI;

        /* renamed from: android.widget.AppSecurityPermissions$PermissionItemView$1 */
        class AnonymousClass1 implements DialogInterface.OnClickListener {
            final /* synthetic */ PermissionItemView this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.PermissionItemView.1.<init>(android.widget.AppSecurityPermissions$PermissionItemView):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            AnonymousClass1(android.widget.AppSecurityPermissions.PermissionItemView r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.PermissionItemView.1.<init>(android.widget.AppSecurityPermissions$PermissionItemView):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.1.<init>(android.widget.AppSecurityPermissions$PermissionItemView):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionItemView.1.onClick(android.content.DialogInterface, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void onClick(android.content.DialogInterface r1, int r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionItemView.1.onClick(android.content.DialogInterface, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.1.onClick(android.content.DialogInterface, int):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.AppSecurityPermissions.PermissionItemView.-get0(android.widget.AppSecurityPermissions$PermissionItemView):android.content.Context, dex:  in method: android.widget.AppSecurityPermissions.PermissionItemView.-get0(android.widget.AppSecurityPermissions$PermissionItemView):android.content.Context, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.AppSecurityPermissions.PermissionItemView.-get0(android.widget.AppSecurityPermissions$PermissionItemView):android.content.Context, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -get0 */
        static /* synthetic */ android.content.Context m181-get0(android.widget.AppSecurityPermissions.PermissionItemView r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.AppSecurityPermissions.PermissionItemView.-get0(android.widget.AppSecurityPermissions$PermissionItemView):android.content.Context, dex:  in method: android.widget.AppSecurityPermissions.PermissionItemView.-get0(android.widget.AppSecurityPermissions$PermissionItemView):android.content.Context, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.-get0(android.widget.AppSecurityPermissions$PermissionItemView):android.content.Context");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionItemView.-get1(android.widget.AppSecurityPermissions$PermissionItemView):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get1 */
        static /* synthetic */ java.lang.String m182-get1(android.widget.AppSecurityPermissions.PermissionItemView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionItemView.-get1(android.widget.AppSecurityPermissions$PermissionItemView):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.-get1(android.widget.AppSecurityPermissions$PermissionItemView):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.widget.AppSecurityPermissions.PermissionItemView.<init>(android.content.Context, android.util.AttributeSet):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public PermissionItemView(android.content.Context r1, android.util.AttributeSet r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.widget.AppSecurityPermissions.PermissionItemView.<init>(android.content.Context, android.util.AttributeSet):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.<init>(android.content.Context, android.util.AttributeSet):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.widget.AppSecurityPermissions.PermissionItemView.addRevokeUIIfNecessary(android.app.AlertDialog$Builder):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void addRevokeUIIfNecessary(android.app.AlertDialog.Builder r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.widget.AppSecurityPermissions.PermissionItemView.addRevokeUIIfNecessary(android.app.AlertDialog$Builder):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.addRevokeUIIfNecessary(android.app.AlertDialog$Builder):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.AppSecurityPermissions.PermissionItemView.onClick(android.view.View):void, dex:  in method: android.widget.AppSecurityPermissions.PermissionItemView.onClick(android.view.View):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.AppSecurityPermissions.PermissionItemView.onClick(android.view.View):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void onClick(android.view.View r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.AppSecurityPermissions.PermissionItemView.onClick(android.view.View):void, dex:  in method: android.widget.AppSecurityPermissions.PermissionItemView.onClick(android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.onClick(android.view.View):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionItemView.onDetachedFromWindow():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void onDetachedFromWindow() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.PermissionItemView.onDetachedFromWindow():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.onDetachedFromWindow():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.PermissionItemView.setPermission(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, java.lang.String, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void setPermission(android.widget.AppSecurityPermissions.MyPermissionGroupInfo r1, android.widget.AppSecurityPermissions.MyPermissionInfo r2, boolean r3, java.lang.CharSequence r4, java.lang.String r5, boolean r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.PermissionItemView.setPermission(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, java.lang.String, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.PermissionItemView.setPermission(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, java.lang.String, boolean):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.<init>(android.content.Context):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private AppSecurityPermissions(android.content.Context r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.<init>(android.content.Context):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.<init>(android.content.Context):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.<init>(android.content.Context, android.content.pm.PackageInfo):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public AppSecurityPermissions(android.content.Context r1, android.content.pm.PackageInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.<init>(android.content.Context, android.content.pm.PackageInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.<init>(android.content.Context, android.content.pm.PackageInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.<init>(android.content.Context, java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public AppSecurityPermissions(android.content.Context r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AppSecurityPermissions.<init>(android.content.Context, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.<init>(android.content.Context, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.addPermToList(java.util.List, android.widget.AppSecurityPermissions$MyPermissionInfo):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void addPermToList(java.util.List<android.widget.AppSecurityPermissions.MyPermissionInfo> r1, android.widget.AppSecurityPermissions.MyPermissionInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.addPermToList(java.util.List, android.widget.AppSecurityPermissions$MyPermissionInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.addPermToList(java.util.List, android.widget.AppSecurityPermissions$MyPermissionInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.displayPermissions(java.util.List, android.widget.LinearLayout, int, boolean):void, dex: 
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
    private void displayPermissions(java.util.List<android.widget.AppSecurityPermissions.MyPermissionGroupInfo> r1, android.widget.LinearLayout r2, int r3, boolean r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.displayPermissions(java.util.List, android.widget.LinearLayout, int, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.displayPermissions(java.util.List, android.widget.LinearLayout, int, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.extractPerms(android.content.pm.PackageInfo, java.util.Set, android.content.pm.PackageInfo):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void extractPerms(android.content.pm.PackageInfo r1, java.util.Set<android.widget.AppSecurityPermissions.MyPermissionInfo> r2, android.content.pm.PackageInfo r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.extractPerms(android.content.pm.PackageInfo, java.util.Set, android.content.pm.PackageInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.extractPerms(android.content.pm.PackageInfo, java.util.Set, android.content.pm.PackageInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getAllUsedPermissions(int, java.util.Set):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void getAllUsedPermissions(int r1, java.util.Set<android.widget.AppSecurityPermissions.MyPermissionInfo> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getAllUsedPermissions(int, java.util.Set):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getAllUsedPermissions(int, java.util.Set):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.getPermissionItemView(android.content.Context, java.lang.CharSequence, java.lang.CharSequence, boolean):android.view.View, dex: 
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
    public static android.view.View getPermissionItemView(android.content.Context r1, java.lang.CharSequence r2, java.lang.CharSequence r3, boolean r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.getPermissionItemView(android.content.Context, java.lang.CharSequence, java.lang.CharSequence, boolean):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionItemView(android.content.Context, java.lang.CharSequence, java.lang.CharSequence, boolean):android.view.View");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.AppSecurityPermissions.getPermissionItemView(android.content.Context, android.view.LayoutInflater, android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, java.lang.String, boolean):android.widget.AppSecurityPermissions$PermissionItemView, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private static android.widget.AppSecurityPermissions.PermissionItemView getPermissionItemView(android.content.Context r1, android.view.LayoutInflater r2, android.widget.AppSecurityPermissions.MyPermissionGroupInfo r3, android.widget.AppSecurityPermissions.MyPermissionInfo r4, boolean r5, java.lang.CharSequence r6, java.lang.String r7, boolean r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.AppSecurityPermissions.getPermissionItemView(android.content.Context, android.view.LayoutInflater, android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, java.lang.String, boolean):android.widget.AppSecurityPermissions$PermissionItemView, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionItemView(android.content.Context, android.view.LayoutInflater, android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, java.lang.String, boolean):android.widget.AppSecurityPermissions$PermissionItemView");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionItemView(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, boolean):android.widget.AppSecurityPermissions$PermissionItemView, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private android.widget.AppSecurityPermissions.PermissionItemView getPermissionItemView(android.widget.AppSecurityPermissions.MyPermissionGroupInfo r1, android.widget.AppSecurityPermissions.MyPermissionInfo r2, boolean r3, java.lang.CharSequence r4, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionItemView(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, boolean):android.widget.AppSecurityPermissions$PermissionItemView, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionItemView(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, android.widget.AppSecurityPermissions$MyPermissionInfo, boolean, java.lang.CharSequence, boolean):android.widget.AppSecurityPermissions$PermissionItemView");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.getPermissionItemViewOld(android.content.Context, android.view.LayoutInflater, java.lang.CharSequence, java.lang.CharSequence, boolean, android.graphics.drawable.Drawable):android.view.View, dex: 
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
    private static android.view.View getPermissionItemViewOld(android.content.Context r1, android.view.LayoutInflater r2, java.lang.CharSequence r3, java.lang.CharSequence r4, boolean r5, android.graphics.drawable.Drawable r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.getPermissionItemViewOld(android.content.Context, android.view.LayoutInflater, java.lang.CharSequence, java.lang.CharSequence, boolean, android.graphics.drawable.Drawable):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionItemViewOld(android.content.Context, android.view.LayoutInflater, java.lang.CharSequence, java.lang.CharSequence, boolean, android.graphics.drawable.Drawable):android.view.View");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionList(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, int):java.util.List<android.widget.AppSecurityPermissions$MyPermissionInfo>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private java.util.List<android.widget.AppSecurityPermissions.MyPermissionInfo> getPermissionList(android.widget.AppSecurityPermissions.MyPermissionGroupInfo r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionList(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, int):java.util.List<android.widget.AppSecurityPermissions$MyPermissionInfo>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionList(android.widget.AppSecurityPermissions$MyPermissionGroupInfo, int):java.util.List<android.widget.AppSecurityPermissions$MyPermissionInfo>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionsForPackage(java.lang.String, java.util.Set):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void getPermissionsForPackage(java.lang.String r1, java.util.Set<android.widget.AppSecurityPermissions.MyPermissionInfo> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionsForPackage(java.lang.String, java.util.Set):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionsForPackage(java.lang.String, java.util.Set):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionsView(int, boolean):android.view.View, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private android.view.View getPermissionsView(int r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionsView(int, boolean):android.view.View, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionsView(int, boolean):android.view.View");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.AppSecurityPermissions.isDisplayablePermission(android.content.pm.PermissionInfo, int, int):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean isDisplayablePermission(android.content.pm.PermissionInfo r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.AppSecurityPermissions.isDisplayablePermission(android.content.pm.PermissionInfo, int, int):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.isDisplayablePermission(android.content.pm.PermissionInfo, int, int):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.AppSecurityPermissions.setPermissions(java.util.List):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void setPermissions(java.util.List<android.widget.AppSecurityPermissions.MyPermissionInfo> r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.AppSecurityPermissions.setPermissions(java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.setPermissions(java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.getPermissionCount():int, dex: 
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
    public int getPermissionCount() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.AppSecurityPermissions.getPermissionCount():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionCount():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionCount(int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public int getPermissionCount(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AppSecurityPermissions.getPermissionCount(int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AppSecurityPermissions.getPermissionCount(int):int");
    }

    public View getPermissionsView() {
        return getPermissionsView(65535, false);
    }

    public View getPermissionsViewWithRevokeButtons() {
        return getPermissionsView(65535, true);
    }

    public View getPermissionsView(int which) {
        return getPermissionsView(which, false);
    }
}
