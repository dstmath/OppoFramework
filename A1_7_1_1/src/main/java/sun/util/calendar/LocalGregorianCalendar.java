package sun.util.calendar;

import java.security.PrivilegedExceptionAction;
import java.util.TimeZone;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class LocalGregorianCalendar extends BaseCalendar {
    private Era[] eras;
    private String name;

    /* renamed from: sun.util.calendar.LocalGregorianCalendar$1 */
    static class AnonymousClass1 implements PrivilegedExceptionAction {
        final /* synthetic */ String val$fname;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: sun.util.calendar.LocalGregorianCalendar.1.<init>(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: sun.util.calendar.LocalGregorianCalendar.1.<init>(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.1.<init>(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: sun.util.calendar.LocalGregorianCalendar.1.run():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.Object run() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: sun.util.calendar.LocalGregorianCalendar.1.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.1.run():java.lang.Object");
        }
    }

    public static class Date extends sun.util.calendar.BaseCalendar.Date {
        private int gregorianYear;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: sun.util.calendar.LocalGregorianCalendar.Date.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected Date() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: sun.util.calendar.LocalGregorianCalendar.Date.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: sun.util.calendar.LocalGregorianCalendar.Date.<init>(java.util.TimeZone):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        protected Date(java.util.TimeZone r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: sun.util.calendar.LocalGregorianCalendar.Date.<init>(java.util.TimeZone):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.<init>(java.util.TimeZone):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.addYear(int):sun.util.calendar.CalendarDate, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ sun.util.calendar.CalendarDate addYear(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.addYear(int):sun.util.calendar.CalendarDate, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.addYear(int):sun.util.calendar.CalendarDate");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: sun.util.calendar.LocalGregorianCalendar.Date.addYear(int):sun.util.calendar.LocalGregorianCalendar$Date, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public sun.util.calendar.LocalGregorianCalendar.Date addYear(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: sun.util.calendar.LocalGregorianCalendar.Date.addYear(int):sun.util.calendar.LocalGregorianCalendar$Date, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.addYear(int):sun.util.calendar.LocalGregorianCalendar$Date");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: sun.util.calendar.LocalGregorianCalendar.Date.getNormalizedYear():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int getNormalizedYear() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: sun.util.calendar.LocalGregorianCalendar.Date.getNormalizedYear():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.getNormalizedYear():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.setEra(sun.util.calendar.Era):sun.util.calendar.CalendarDate, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ sun.util.calendar.CalendarDate setEra(sun.util.calendar.Era r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.setEra(sun.util.calendar.Era):sun.util.calendar.CalendarDate, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.setEra(sun.util.calendar.Era):sun.util.calendar.CalendarDate");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.setEra(sun.util.calendar.Era):sun.util.calendar.LocalGregorianCalendar$Date, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public sun.util.calendar.LocalGregorianCalendar.Date setEra(sun.util.calendar.Era r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.setEra(sun.util.calendar.Era):sun.util.calendar.LocalGregorianCalendar$Date, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.setEra(sun.util.calendar.Era):sun.util.calendar.LocalGregorianCalendar$Date");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.calendar.LocalGregorianCalendar.Date.setLocalEra(sun.util.calendar.Era):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        void setLocalEra(sun.util.calendar.Era r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.calendar.LocalGregorianCalendar.Date.setLocalEra(sun.util.calendar.Era):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.setLocalEra(sun.util.calendar.Era):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.util.calendar.LocalGregorianCalendar.Date.setLocalYear(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        void setLocalYear(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.util.calendar.LocalGregorianCalendar.Date.setLocalYear(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.setLocalYear(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: sun.util.calendar.LocalGregorianCalendar.Date.setNormalizedYear(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void setNormalizedYear(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: sun.util.calendar.LocalGregorianCalendar.Date.setNormalizedYear(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.setNormalizedYear(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.setYear(int):sun.util.calendar.CalendarDate, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ sun.util.calendar.CalendarDate setYear(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.setYear(int):sun.util.calendar.CalendarDate, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.setYear(int):sun.util.calendar.CalendarDate");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.setYear(int):sun.util.calendar.LocalGregorianCalendar$Date, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public sun.util.calendar.LocalGregorianCalendar.Date setYear(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.setYear(int):sun.util.calendar.LocalGregorianCalendar$Date, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.setYear(int):sun.util.calendar.LocalGregorianCalendar$Date");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.util.calendar.LocalGregorianCalendar.Date.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.Date.toString():java.lang.String");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    static sun.util.calendar.LocalGregorianCalendar getLocalGregorianCalendar(java.lang.String r27) {
        /*
        r4 = 0;
        r24 = new sun.security.action.GetPropertyAction;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r25 = "java.home";	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24.<init>(r25);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r16 = java.security.AccessController.doPrivileged(r24);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r16 = (java.lang.String) r16;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24 = new java.lang.StringBuilder;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24.<init>();	 Catch:{ PrivilegedActionException -> 0x0071 }
        r0 = r24;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r1 = r16;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24 = r0.append(r1);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r25 = java.io.File.separator;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24 = r24.append(r25);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r25 = "lib";	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24 = r24.append(r25);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r25 = java.io.File.separator;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24 = r24.append(r25);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r25 = "calendars.properties";	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24 = r24.append(r25);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r15 = r24.toString();	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24 = new sun.util.calendar.LocalGregorianCalendar$1;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r0 = r24;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r0.<init>(r15);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r4 = java.security.AccessController.doPrivileged(r24);	 Catch:{ PrivilegedActionException -> 0x0071 }
        r4 = (java.util.Properties) r4;	 Catch:{ PrivilegedActionException -> 0x0071 }
        r24 = new java.lang.StringBuilder;
        r24.<init>();
        r25 = "calendar.";
        r24 = r24.append(r25);
        r0 = r24;
        r1 = r27;
        r24 = r0.append(r1);
        r25 = ".eras";
        r24 = r24.append(r25);
        r24 = r24.toString();
        r0 = r24;
        r22 = r4.getProperty(r0);
        if (r22 != 0) goto L_0x007c;
    L_0x006e:
        r24 = 0;
        return r24;
    L_0x0071:
        r11 = move-exception;
        r24 = new java.lang.RuntimeException;
        r25 = r11.getException();
        r24.<init>(r25);
        throw r24;
    L_0x007c:
        r14 = new java.util.ArrayList;
        r14.<init>();
        r13 = new java.util.StringTokenizer;
        r24 = ";";
        r0 = r22;
        r1 = r24;
        r13.<init>(r0, r1);
    L_0x008d:
        r24 = r13.hasMoreTokens();
        if (r24 == 0) goto L_0x015f;
    L_0x0093:
        r24 = r13.nextToken();
        r20 = r24.trim();
        r19 = new java.util.StringTokenizer;
        r24 = ",";
        r0 = r19;
        r1 = r20;
        r2 = r24;
        r0.<init>(r1, r2);
        r6 = 0;
        r10 = 1;
        r8 = 0;
        r7 = 0;
    L_0x00ae:
        r24 = r19.hasMoreTokens();
        if (r24 == 0) goto L_0x0155;
    L_0x00b4:
        r18 = r19.nextToken();
        r24 = 61;
        r0 = r18;
        r1 = r24;
        r17 = r0.indexOf(r1);
        r24 = -1;
        r0 = r17;
        r1 = r24;
        if (r0 != r1) goto L_0x00cd;
    L_0x00ca:
        r24 = 0;
        return r24;
    L_0x00cd:
        r24 = 0;
        r0 = r18;
        r1 = r24;
        r2 = r17;
        r21 = r0.substring(r1, r2);
        r24 = r17 + 1;
        r0 = r18;
        r1 = r24;
        r23 = r0.substring(r1);
        r24 = "name";
        r0 = r24;
        r1 = r21;
        r24 = r0.equals(r1);
        if (r24 == 0) goto L_0x00f3;
    L_0x00f0:
        r6 = r23;
        goto L_0x00ae;
    L_0x00f3:
        r24 = "since";
        r0 = r24;
        r1 = r21;
        r24 = r0.equals(r1);
        if (r24 == 0) goto L_0x0126;
    L_0x0100:
        r24 = "u";
        r24 = r23.endsWith(r24);
        if (r24 == 0) goto L_0x0121;
    L_0x0109:
        r10 = 0;
        r24 = r23.length();
        r24 = r24 + -1;
        r25 = 0;
        r0 = r23;
        r1 = r25;
        r2 = r24;
        r24 = r0.substring(r1, r2);
        r8 = java.lang.Long.parseLong(r24);
        goto L_0x00ae;
    L_0x0121:
        r8 = java.lang.Long.parseLong(r23);
        goto L_0x00ae;
    L_0x0126:
        r24 = "abbr";
        r0 = r24;
        r1 = r21;
        r24 = r0.equals(r1);
        if (r24 == 0) goto L_0x0137;
    L_0x0133:
        r7 = r23;
        goto L_0x00ae;
    L_0x0137:
        r24 = new java.lang.RuntimeException;
        r25 = new java.lang.StringBuilder;
        r25.<init>();
        r26 = "Unknown key word: ";
        r25 = r25.append(r26);
        r0 = r25;
        r1 = r21;
        r25 = r0.append(r1);
        r25 = r25.toString();
        r24.<init>(r25);
        throw r24;
    L_0x0155:
        r5 = new sun.util.calendar.Era;
        r5.<init>(r6, r7, r8, r10);
        r14.add(r5);
        goto L_0x008d;
    L_0x015f:
        r24 = r14.size();
        r0 = r24;
        r12 = new sun.util.calendar.Era[r0];
        r14.toArray(r12);
        r24 = new sun.util.calendar.LocalGregorianCalendar;
        r0 = r24;
        r1 = r27;
        r0.<init>(r1, r12);
        return r24;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.calendar.LocalGregorianCalendar.getLocalGregorianCalendar(java.lang.String):sun.util.calendar.LocalGregorianCalendar");
    }

    private LocalGregorianCalendar(String name, Era[] eras) {
        this.name = name;
        this.eras = eras;
        setEras(eras);
    }

    public String getName() {
        return this.name;
    }

    public /* bridge */ /* synthetic */ CalendarDate getCalendarDate() {
        return getCalendarDate();
    }

    public Date getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
    }

    public /* bridge */ /* synthetic */ CalendarDate getCalendarDate(long millis) {
        return getCalendarDate(millis);
    }

    public Date getCalendarDate(long millis) {
        return getCalendarDate(millis, newCalendarDate());
    }

    public /* bridge */ /* synthetic */ CalendarDate getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, zone);
    }

    public Date getCalendarDate(long millis, TimeZone zone) {
        return getCalendarDate(millis, newCalendarDate(zone));
    }

    public /* bridge */ /* synthetic */ CalendarDate getCalendarDate(long millis, CalendarDate date) {
        return getCalendarDate(millis, date);
    }

    public Date getCalendarDate(long millis, CalendarDate date) {
        Date ldate = (Date) super.getCalendarDate(millis, date);
        return adjustYear(ldate, millis, ldate.getZoneOffset());
    }

    private Date adjustYear(Date ldate, long millis, int zoneOffset) {
        int i = this.eras.length - 1;
        while (i >= 0) {
            Era era = this.eras[i];
            long since = era.getSince(null);
            if (era.isLocalTime()) {
                since -= (long) zoneOffset;
            }
            if (millis >= since) {
                ldate.setLocalEra(era);
                ldate.setLocalYear((ldate.getNormalizedYear() - era.getSinceDate().getYear()) + 1);
                break;
            }
            i--;
        }
        if (i < 0) {
            ldate.setLocalEra(null);
            ldate.setLocalYear(ldate.getNormalizedYear());
        }
        ldate.setNormalized(true);
        return ldate;
    }

    public /* bridge */ /* synthetic */ CalendarDate newCalendarDate() {
        return newCalendarDate();
    }

    public Date newCalendarDate() {
        return new Date();
    }

    public /* bridge */ /* synthetic */ CalendarDate newCalendarDate(TimeZone zone) {
        return newCalendarDate(zone);
    }

    public Date newCalendarDate(TimeZone zone) {
        return new Date(zone);
    }

    public boolean validate(CalendarDate date) {
        Date ldate = (Date) date;
        Era era = ldate.getEra();
        if (era == null) {
            ldate.setNormalizedYear(ldate.getYear());
        } else if (!validateEra(era)) {
            return false;
        } else {
            ldate.setNormalizedYear(era.getSinceDate().getYear() + ldate.getYear());
        }
        return super.validate(ldate);
    }

    private boolean validateEra(Era era) {
        for (Era era2 : this.eras) {
            if (era == era2) {
                return true;
            }
        }
        return false;
    }

    public boolean normalize(CalendarDate date) {
        if (date.isNormalized()) {
            return true;
        }
        normalizeYear(date);
        Date ldate = (Date) date;
        super.normalize(ldate);
        boolean hasMillis = false;
        long millis = 0;
        int year = ldate.getNormalizedYear();
        Era era = null;
        int i = this.eras.length - 1;
        while (i >= 0) {
            era = this.eras[i];
            if (!era.isLocalTime()) {
                if (!hasMillis) {
                    millis = super.getTime(date);
                    hasMillis = true;
                }
                if (millis >= era.getSince(date.getZone())) {
                    break;
                }
            } else {
                CalendarDate sinceDate = era.getSinceDate();
                int sinceYear = sinceDate.getYear();
                if (year > sinceYear) {
                    break;
                } else if (year == sinceYear) {
                    int month = ldate.getMonth();
                    int sinceMonth = sinceDate.getMonth();
                    if (month > sinceMonth) {
                        break;
                    } else if (month == sinceMonth) {
                        int day = ldate.getDayOfMonth();
                        int sinceDay = sinceDate.getDayOfMonth();
                        if (day > sinceDay) {
                            break;
                        } else if (day == sinceDay) {
                            if (ldate.getTimeOfDay() < sinceDate.getTimeOfDay()) {
                                i--;
                            }
                        }
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            i--;
        }
        if (i >= 0) {
            ldate.setLocalEra(era);
            ldate.setLocalYear((ldate.getNormalizedYear() - era.getSinceDate().getYear()) + 1);
        } else {
            ldate.setEra(null);
            ldate.setLocalYear(year);
            ldate.setNormalizedYear(year);
        }
        ldate.setNormalized(true);
        return true;
    }

    void normalizeMonth(CalendarDate date) {
        normalizeYear(date);
        super.normalizeMonth(date);
    }

    void normalizeYear(CalendarDate date) {
        Date ldate = (Date) date;
        Era era = ldate.getEra();
        if (era == null || !validateEra(era)) {
            ldate.setNormalizedYear(ldate.getYear());
        } else {
            ldate.setNormalizedYear((era.getSinceDate().getYear() + ldate.getYear()) - 1);
        }
    }

    public boolean isLeapYear(int gregorianYear) {
        return CalendarUtils.isGregorianLeapYear(gregorianYear);
    }

    public boolean isLeapYear(Era era, int year) {
        if (era == null) {
            return isLeapYear(year);
        }
        return isLeapYear((era.getSinceDate().getYear() + year) - 1);
    }

    public void getCalendarDateFromFixedDate(CalendarDate date, long fixedDate) {
        Date ldate = (Date) date;
        super.getCalendarDateFromFixedDate(ldate, fixedDate);
        adjustYear(ldate, (fixedDate - 719163) * 86400000, 0);
    }
}
