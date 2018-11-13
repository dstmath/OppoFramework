package android.support.v4.view;

import android.view.ViewGroup.MarginLayoutParams;

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
public class MarginLayoutParamsCompat {
    static final MarginLayoutParamsCompatImpl IMPL = null;

    interface MarginLayoutParamsCompatImpl {
        int getLayoutDirection(MarginLayoutParams marginLayoutParams);

        int getMarginEnd(MarginLayoutParams marginLayoutParams);

        int getMarginStart(MarginLayoutParams marginLayoutParams);

        boolean isMarginRelative(MarginLayoutParams marginLayoutParams);

        void resolveLayoutDirection(MarginLayoutParams marginLayoutParams, int i);

        void setLayoutDirection(MarginLayoutParams marginLayoutParams, int i);

        void setMarginEnd(MarginLayoutParams marginLayoutParams, int i);

        void setMarginStart(MarginLayoutParams marginLayoutParams, int i);
    }

    static class MarginLayoutParamsCompatImplBase implements MarginLayoutParamsCompatImpl {
        MarginLayoutParamsCompatImplBase() {
        }

        public int getMarginStart(MarginLayoutParams lp) {
            return lp.leftMargin;
        }

        public int getMarginEnd(MarginLayoutParams lp) {
            return lp.rightMargin;
        }

        public void setMarginStart(MarginLayoutParams lp, int marginStart) {
            lp.leftMargin = marginStart;
        }

        public void setMarginEnd(MarginLayoutParams lp, int marginEnd) {
            lp.rightMargin = marginEnd;
        }

        public boolean isMarginRelative(MarginLayoutParams lp) {
            return false;
        }

        public int getLayoutDirection(MarginLayoutParams lp) {
            return 0;
        }

        public void setLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
        }

        public void resolveLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
        }
    }

    static class MarginLayoutParamsCompatImplJbMr1 implements MarginLayoutParamsCompatImpl {
        MarginLayoutParamsCompatImplJbMr1() {
        }

        public int getMarginStart(MarginLayoutParams lp) {
            return MarginLayoutParamsCompatJellybeanMr1.getMarginStart(lp);
        }

        public int getMarginEnd(MarginLayoutParams lp) {
            return MarginLayoutParamsCompatJellybeanMr1.getMarginEnd(lp);
        }

        public void setMarginStart(MarginLayoutParams lp, int marginStart) {
            MarginLayoutParamsCompatJellybeanMr1.setMarginStart(lp, marginStart);
        }

        public void setMarginEnd(MarginLayoutParams lp, int marginEnd) {
            MarginLayoutParamsCompatJellybeanMr1.setMarginEnd(lp, marginEnd);
        }

        public boolean isMarginRelative(MarginLayoutParams lp) {
            return MarginLayoutParamsCompatJellybeanMr1.isMarginRelative(lp);
        }

        public int getLayoutDirection(MarginLayoutParams lp) {
            return MarginLayoutParamsCompatJellybeanMr1.getLayoutDirection(lp);
        }

        public void setLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
            MarginLayoutParamsCompatJellybeanMr1.setLayoutDirection(lp, layoutDirection);
        }

        public void resolveLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
            MarginLayoutParamsCompatJellybeanMr1.resolveLayoutDirection(lp, layoutDirection);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.support.v4.view.MarginLayoutParamsCompat.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.support.v4.view.MarginLayoutParamsCompat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.view.MarginLayoutParamsCompat.<clinit>():void");
    }

    public static int getMarginStart(MarginLayoutParams lp) {
        return IMPL.getMarginStart(lp);
    }

    public static int getMarginEnd(MarginLayoutParams lp) {
        return IMPL.getMarginEnd(lp);
    }

    public static void setMarginStart(MarginLayoutParams lp, int marginStart) {
        IMPL.setMarginStart(lp, marginStart);
    }

    public static void setMarginEnd(MarginLayoutParams lp, int marginEnd) {
        IMPL.setMarginEnd(lp, marginEnd);
    }

    public static boolean isMarginRelative(MarginLayoutParams lp) {
        return IMPL.isMarginRelative(lp);
    }

    public static int getLayoutDirection(MarginLayoutParams lp) {
        return IMPL.getLayoutDirection(lp);
    }

    public static void setLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
        IMPL.setLayoutDirection(lp, layoutDirection);
    }

    public static void resolveLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
        IMPL.resolveLayoutDirection(lp, layoutDirection);
    }
}
