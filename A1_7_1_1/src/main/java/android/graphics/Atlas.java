package android.graphics;

import android.util.Log;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class Atlas {
    /* renamed from: -android-graphics-Atlas$TypeSwitchesValues */
    private static final /* synthetic */ int[] f13-android-graphics-Atlas$TypeSwitchesValues = null;
    public static final int FLAG_ADD_PADDING = 2;
    public static final int FLAG_DEFAULTS = 2;
    private final Policy mPolicy;

    public static class Entry {
        public int x;
        public int y;
    }

    private static abstract class Policy {
        /* synthetic */ Policy(Policy policy) {
            this();
        }

        abstract Entry pack(int i, int i2, Entry entry);

        private Policy() {
        }
    }

    private static class SlicePolicy extends Policy {
        private final int mPadding;
        private final Cell mRoot;
        private final SplitDecision mSplitDecision;

        private static class Cell {
            int height;
            Cell next;
            int width;
            int x;
            int y;

            /* synthetic */ Cell(Cell cell) {
                this();
            }

            private Cell() {
            }

            public String toString() {
                return String.format("cell[x=%d y=%d width=%d height=%d", new Object[]{Integer.valueOf(this.x), Integer.valueOf(this.y), Integer.valueOf(this.width), Integer.valueOf(this.height)});
            }
        }

        private interface SplitDecision {
            boolean splitHorizontal(int i, int i2, int i3, int i4);
        }

        private static class LongerFreeAxisSplitDecision implements SplitDecision {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.LongerFreeAxisSplitDecision.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            private LongerFreeAxisSplitDecision() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.LongerFreeAxisSplitDecision.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.graphics.Atlas.SlicePolicy.LongerFreeAxisSplitDecision.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.LongerFreeAxisSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$LongerFreeAxisSplitDecision):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            /* synthetic */ LongerFreeAxisSplitDecision(android.graphics.Atlas.SlicePolicy.LongerFreeAxisSplitDecision r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.LongerFreeAxisSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$LongerFreeAxisSplitDecision):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.graphics.Atlas.SlicePolicy.LongerFreeAxisSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$LongerFreeAxisSplitDecision):void");
            }

            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return freeWidth > freeHeight;
            }
        }

        private static class MaxAreaSplitDecision implements SplitDecision {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.MaxAreaSplitDecision.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            private MaxAreaSplitDecision() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.MaxAreaSplitDecision.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.graphics.Atlas.SlicePolicy.MaxAreaSplitDecision.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.MaxAreaSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$MaxAreaSplitDecision):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            /* synthetic */ MaxAreaSplitDecision(android.graphics.Atlas.SlicePolicy.MaxAreaSplitDecision r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.MaxAreaSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$MaxAreaSplitDecision):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.graphics.Atlas.SlicePolicy.MaxAreaSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$MaxAreaSplitDecision):void");
            }

            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return rectWidth * freeHeight <= freeWidth * rectHeight;
            }
        }

        private static class MinAreaSplitDecision implements SplitDecision {
            /* synthetic */ MinAreaSplitDecision(MinAreaSplitDecision minAreaSplitDecision) {
                this();
            }

            private MinAreaSplitDecision() {
            }

            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return rectWidth * freeHeight > freeWidth * rectHeight;
            }
        }

        private static class ShorterFreeAxisSplitDecision implements SplitDecision {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.ShorterFreeAxisSplitDecision.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            private ShorterFreeAxisSplitDecision() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.ShorterFreeAxisSplitDecision.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.graphics.Atlas.SlicePolicy.ShorterFreeAxisSplitDecision.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.ShorterFreeAxisSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$ShorterFreeAxisSplitDecision):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            /* synthetic */ ShorterFreeAxisSplitDecision(android.graphics.Atlas.SlicePolicy.ShorterFreeAxisSplitDecision r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.SlicePolicy.ShorterFreeAxisSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$ShorterFreeAxisSplitDecision):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.graphics.Atlas.SlicePolicy.ShorterFreeAxisSplitDecision.<init>(android.graphics.Atlas$SlicePolicy$ShorterFreeAxisSplitDecision):void");
            }

            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return freeWidth <= freeHeight;
            }
        }

        SlicePolicy(int width, int height, int flags, SplitDecision splitDecision) {
            int i = 0;
            super();
            this.mRoot = new Cell();
            if ((flags & 2) != 0) {
                i = 1;
            }
            this.mPadding = i;
            Cell first = new Cell();
            i = this.mPadding;
            first.y = i;
            first.x = i;
            first.width = width - (this.mPadding * 2);
            first.height = height - (this.mPadding * 2);
            this.mRoot.next = first;
            this.mSplitDecision = splitDecision;
        }

        Entry pack(int width, int height, Entry entry) {
            Cell prev = this.mRoot;
            for (Cell cell = this.mRoot.next; cell != null; cell = cell.next) {
                if (insert(cell, prev, width, height, entry)) {
                    return entry;
                }
                prev = cell;
            }
            return null;
        }

        private boolean insert(Cell cell, Cell prev, int width, int height, Entry entry) {
            if (cell.width < width || cell.height < height) {
                return false;
            }
            int deltaWidth = cell.width - width;
            int deltaHeight = cell.height - height;
            Cell first = new Cell();
            Cell second = new Cell();
            first.x = (cell.x + width) + this.mPadding;
            first.y = cell.y;
            first.width = deltaWidth - this.mPadding;
            second.x = cell.x;
            second.y = (cell.y + height) + this.mPadding;
            second.height = deltaHeight - this.mPadding;
            if (this.mSplitDecision.splitHorizontal(deltaWidth, deltaHeight, width, height)) {
                first.height = height;
                second.width = cell.width;
            } else {
                first.height = cell.height;
                second.width = width;
                Cell temp = first;
                Cell first2 = second;
                second = first;
                first = first2;
            }
            if (first.width > 0 && first.height > 0) {
                prev.next = first;
                prev = first;
            }
            if (second.width <= 0 || second.height <= 0) {
                prev.next = cell.next;
            } else {
                prev.next = second;
                second.next = cell.next;
            }
            cell.next = null;
            entry.x = cell.x;
            entry.y = cell.y;
            return true;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
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
    public enum Type {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.Type.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Atlas.Type.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Atlas.Type.<clinit>():void");
        }
    }

    /* renamed from: -getandroid-graphics-Atlas$TypeSwitchesValues */
    private static /* synthetic */ int[] m209-getandroid-graphics-Atlas$TypeSwitchesValues() {
        if (f13-android-graphics-Atlas$TypeSwitchesValues != null) {
            return f13-android-graphics-Atlas$TypeSwitchesValues;
        }
        int[] iArr = new int[Type.values().length];
        try {
            iArr[Type.SliceLongAxis.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Type.SliceMaxArea.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Type.SliceMinArea.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Type.SliceShortAxis.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f13-android-graphics-Atlas$TypeSwitchesValues = iArr;
        return iArr;
    }

    public Atlas(Type type, int width, int height) {
        this(type, width, height, 2);
    }

    public Atlas(Type type, int width, int height, int flags) {
        this.mPolicy = findPolicy(type, width, height, flags);
    }

    public Entry pack(int width, int height) {
        return pack(width, height, null);
    }

    public Entry pack(int width, int height, Entry entry) {
        if (entry == null) {
            entry = new Entry();
        }
        if (this.mPolicy == null) {
            return null;
        }
        return this.mPolicy.pack(width, height, entry);
    }

    private static Policy findPolicy(Type type, int width, int height, int flags) {
        try {
            switch (m209-getandroid-graphics-Atlas$TypeSwitchesValues()[type.ordinal()]) {
                case 1:
                    return new SlicePolicy(width, height, flags, new LongerFreeAxisSplitDecision());
                case 2:
                    return new SlicePolicy(width, height, flags, new MaxAreaSplitDecision());
                case 3:
                    return new SlicePolicy(width, height, flags, new MinAreaSplitDecision());
                case 4:
                    return new SlicePolicy(width, height, flags, new ShorterFreeAxisSplitDecision());
            }
        } catch (Exception e) {
            Log.e("Atlas", "Exception when find policy", e);
        }
        Log.w("Atlas", "Incorrect type " + type + " in find policy on " + Thread.currentThread().getName());
        return null;
    }
}
