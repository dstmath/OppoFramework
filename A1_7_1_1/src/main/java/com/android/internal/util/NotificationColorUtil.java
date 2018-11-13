package com.android.internal.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.VectorDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.Pair;
import com.android.internal.R;
import java.util.Arrays;
import java.util.WeakHashMap;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class NotificationColorUtil {
    private static final boolean DEBUG = false;
    private static final String TAG = "NotificationColorUtil";
    private static NotificationColorUtil sInstance;
    private static final Object sLock = null;
    private final WeakHashMap<Bitmap, Pair<Boolean, Integer>> mGrayscaleBitmapCache;
    private final int mGrayscaleIconMaxSize;
    private final ImageUtils mImageUtils;

    private static class ColorUtilsFromCompat {
        private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
        private static final int MIN_ALPHA_SEARCH_PRECISION = 1;
        private static final ThreadLocal<double[]> TEMP_ARRAY = null;
        private static final double XYZ_EPSILON = 0.008856d;
        private static final double XYZ_KAPPA = 903.3d;
        private static final double XYZ_WHITE_REFERENCE_X = 95.047d;
        private static final double XYZ_WHITE_REFERENCE_Y = 100.0d;
        private static final double XYZ_WHITE_REFERENCE_Z = 108.883d;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.NotificationColorUtil.ColorUtilsFromCompat.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.NotificationColorUtil.ColorUtilsFromCompat.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.NotificationColorUtil.ColorUtilsFromCompat.<clinit>():void");
        }

        private ColorUtilsFromCompat() {
        }

        public static int compositeColors(int foreground, int background) {
            int bgAlpha = Color.alpha(background);
            int fgAlpha = Color.alpha(foreground);
            int a = compositeAlpha(fgAlpha, bgAlpha);
            return Color.argb(a, compositeComponent(Color.red(foreground), fgAlpha, Color.red(background), bgAlpha, a), compositeComponent(Color.green(foreground), fgAlpha, Color.green(background), bgAlpha, a), compositeComponent(Color.blue(foreground), fgAlpha, Color.blue(background), bgAlpha, a));
        }

        private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
            return 255 - (((255 - backgroundAlpha) * (255 - foregroundAlpha)) / 255);
        }

        private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
            if (a == 0) {
                return 0;
            }
            return (((fgC * 255) * fgA) + ((bgC * bgA) * (255 - fgA))) / (a * 255);
        }

        public static double calculateLuminance(int color) {
            double[] result = getTempDouble3Array();
            colorToXYZ(color, result);
            return result[1] / XYZ_WHITE_REFERENCE_Y;
        }

        public static double calculateContrast(int foreground, int background) {
            if (Color.alpha(background) != 255) {
                throw new IllegalArgumentException("background can not be translucent: #" + Integer.toHexString(background));
            }
            if (Color.alpha(foreground) < 255) {
                foreground = compositeColors(foreground, background);
            }
            double luminance1 = calculateLuminance(foreground) + 0.05d;
            double luminance2 = calculateLuminance(background) + 0.05d;
            return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
        }

        public static void colorToLAB(int color, double[] outLab) {
            RGBToLAB(Color.red(color), Color.green(color), Color.blue(color), outLab);
        }

        public static void RGBToLAB(int r, int g, int b, double[] outLab) {
            RGBToXYZ(r, g, b, outLab);
            XYZToLAB(outLab[0], outLab[1], outLab[2], outLab);
        }

        public static void colorToXYZ(int color, double[] outXyz) {
            RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz);
        }

        public static void RGBToXYZ(int r, int g, int b, double[] outXyz) {
            if (outXyz.length != 3) {
                throw new IllegalArgumentException("outXyz must have a length of 3.");
            }
            double sr = ((double) r) / 255.0d;
            sr = sr < 0.04045d ? sr / 12.92d : Math.pow((0.055d + sr) / 1.055d, 2.4d);
            double sg = ((double) g) / 255.0d;
            sg = sg < 0.04045d ? sg / 12.92d : Math.pow((0.055d + sg) / 1.055d, 2.4d);
            double sb = ((double) b) / 255.0d;
            sb = sb < 0.04045d ? sb / 12.92d : Math.pow((0.055d + sb) / 1.055d, 2.4d);
            outXyz[0] = (((0.4124d * sr) + (0.3576d * sg)) + (0.1805d * sb)) * XYZ_WHITE_REFERENCE_Y;
            outXyz[1] = (((0.2126d * sr) + (0.7152d * sg)) + (0.0722d * sb)) * XYZ_WHITE_REFERENCE_Y;
            outXyz[2] = (((0.0193d * sr) + (0.1192d * sg)) + (0.9505d * sb)) * XYZ_WHITE_REFERENCE_Y;
        }

        public static void XYZToLAB(double x, double y, double z, double[] outLab) {
            if (outLab.length != 3) {
                throw new IllegalArgumentException("outLab must have a length of 3.");
            }
            x = pivotXyzComponent(x / XYZ_WHITE_REFERENCE_X);
            y = pivotXyzComponent(y / XYZ_WHITE_REFERENCE_Y);
            z = pivotXyzComponent(z / XYZ_WHITE_REFERENCE_Z);
            outLab[0] = Math.max(0.0d, (116.0d * y) - 16.0d);
            outLab[1] = (x - y) * 500.0d;
            outLab[2] = (y - z) * 200.0d;
        }

        public static void LABToXYZ(double l, double a, double b, double[] outXyz) {
            double fy = (16.0d + l) / 116.0d;
            double fx = (a / 500.0d) + fy;
            double fz = fy - (b / 200.0d);
            double tmp = Math.pow(fx, 3.0d);
            double xr = tmp > XYZ_EPSILON ? tmp : ((116.0d * fx) - 16.0d) / XYZ_KAPPA;
            double yr = l > 7.9996247999999985d ? Math.pow(fy, 3.0d) : l / XYZ_KAPPA;
            tmp = Math.pow(fz, 3.0d);
            double zr = tmp > XYZ_EPSILON ? tmp : ((116.0d * fz) - 16.0d) / XYZ_KAPPA;
            outXyz[0] = XYZ_WHITE_REFERENCE_X * xr;
            outXyz[1] = XYZ_WHITE_REFERENCE_Y * yr;
            outXyz[2] = XYZ_WHITE_REFERENCE_Z * zr;
        }

        public static int XYZToColor(double x, double y, double z) {
            double r = (((3.2406d * x) + (-1.5372d * y)) + (-0.4986d * z)) / XYZ_WHITE_REFERENCE_Y;
            double g = (((-0.9689d * x) + (1.8758d * y)) + (0.0415d * z)) / XYZ_WHITE_REFERENCE_Y;
            double b = (((0.0557d * x) + (-0.204d * y)) + (1.057d * z)) / XYZ_WHITE_REFERENCE_Y;
            return Color.rgb(constrain((int) Math.round(255.0d * (r > 0.0031308d ? (Math.pow(r, 0.4166666666666667d) * 1.055d) - 0.055d : r * 12.92d)), 0, 255), constrain((int) Math.round(255.0d * (g > 0.0031308d ? (Math.pow(g, 0.4166666666666667d) * 1.055d) - 0.055d : g * 12.92d)), 0, 255), constrain((int) Math.round(255.0d * (b > 0.0031308d ? (Math.pow(b, 0.4166666666666667d) * 1.055d) - 0.055d : b * 12.92d)), 0, 255));
        }

        public static int LABToColor(double l, double a, double b) {
            double[] result = getTempDouble3Array();
            LABToXYZ(l, a, b, result);
            return XYZToColor(result[0], result[1], result[2]);
        }

        private static int constrain(int amount, int low, int high) {
            if (amount < low) {
                return low;
            }
            return amount > high ? high : amount;
        }

        private static double pivotXyzComponent(double component) {
            if (component > XYZ_EPSILON) {
                return Math.pow(component, 0.3333333333333333d);
            }
            return ((XYZ_KAPPA * component) + 16.0d) / 116.0d;
        }

        public static double[] getTempDouble3Array() {
            double[] result = (double[]) TEMP_ARRAY.get();
            if (result != null) {
                return result;
            }
            result = new double[3];
            TEMP_ARRAY.set(result);
            return result;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.NotificationColorUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.NotificationColorUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.NotificationColorUtil.<clinit>():void");
    }

    public static NotificationColorUtil getInstance(Context context) {
        NotificationColorUtil notificationColorUtil;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new NotificationColorUtil(context);
            }
            notificationColorUtil = sInstance;
        }
        return notificationColorUtil;
    }

    private NotificationColorUtil(Context context) {
        this.mImageUtils = new ImageUtils();
        this.mGrayscaleBitmapCache = new WeakHashMap();
        this.mGrayscaleIconMaxSize = context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
    }

    /* JADX WARNING: Missing block: B:17:0x0038, code:
            r4 = r7.mImageUtils;
     */
    /* JADX WARNING: Missing block: B:18:0x003a, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            r2 = r7.mImageUtils.isGrayscale(r8);
            r1 = r8.getGenerationId();
     */
    /* JADX WARNING: Missing block: B:21:0x0045, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:22:0x0046, code:
            r4 = sLock;
     */
    /* JADX WARNING: Missing block: B:23:0x0048, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            r7.mGrayscaleBitmapCache.put(r8, android.util.Pair.create(java.lang.Boolean.valueOf(r2), java.lang.Integer.valueOf(r1)));
     */
    /* JADX WARNING: Missing block: B:26:0x005a, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:27:0x005b, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isGrayscaleIcon(Bitmap bitmap) {
        if (bitmap.getWidth() > this.mGrayscaleIconMaxSize || bitmap.getHeight() > this.mGrayscaleIconMaxSize) {
            return false;
        }
        synchronized (sLock) {
            Pair<Boolean, Integer> cached = (Pair) this.mGrayscaleBitmapCache.get(bitmap);
            if (cached == null || ((Integer) cached.second).intValue() != bitmap.getGenerationId()) {
            } else {
                boolean booleanValue = ((Boolean) cached.first).booleanValue();
                return booleanValue;
            }
        }
    }

    public boolean isGrayscaleIcon(Drawable d) {
        boolean z = false;
        if (d == null) {
            return false;
        }
        if (d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) d;
            if (bd.getBitmap() != null) {
                z = isGrayscaleIcon(bd.getBitmap());
            }
            return z;
        } else if (d instanceof AnimationDrawable) {
            AnimationDrawable ad = (AnimationDrawable) d;
            if (ad.getNumberOfFrames() > 0) {
                z = isGrayscaleIcon(ad.getFrame(0));
            }
            return z;
        } else if (d instanceof VectorDrawable) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isGrayscaleIcon(Context context, Icon icon) {
        if (icon == null) {
            return false;
        }
        switch (icon.getType()) {
            case 1:
                return isGrayscaleIcon(icon.getBitmap());
            case 2:
                return isGrayscaleIcon(context, icon.getResId());
            default:
                return false;
        }
    }

    public boolean isGrayscaleIcon(Context context, int drawableResId) {
        if (drawableResId == 0) {
            return false;
        }
        try {
            return isGrayscaleIcon(context.getDrawable(drawableResId));
        } catch (NotFoundException e) {
            Log.e(TAG, "Drawable not found: " + drawableResId);
            return false;
        }
    }

    public CharSequence invertCharSequenceColors(CharSequence charSequence) {
        if (!(charSequence instanceof Spanned)) {
            return charSequence;
        }
        Spanned ss = (Spanned) charSequence;
        Object[] spans = ss.getSpans(0, ss.length(), Object.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
        for (Object span : spans) {
            Object resultSpan = span;
            if (span instanceof TextAppearanceSpan) {
                resultSpan = processTextAppearanceSpan((TextAppearanceSpan) span);
            }
            builder.setSpan(resultSpan, ss.getSpanStart(span), ss.getSpanEnd(span), ss.getSpanFlags(span));
        }
        return builder;
    }

    private TextAppearanceSpan processTextAppearanceSpan(TextAppearanceSpan span) {
        ColorStateList colorStateList = span.getTextColor();
        if (colorStateList != null) {
            int[] colors = colorStateList.getColors();
            boolean changed = false;
            for (int i = 0; i < colors.length; i++) {
                if (ImageUtils.isGrayscale(colors[i])) {
                    if (!changed) {
                        colors = Arrays.copyOf(colors, colors.length);
                    }
                    colors[i] = processColor(colors[i]);
                    changed = true;
                }
            }
            if (changed) {
                return new TextAppearanceSpan(span.getFamily(), span.getTextStyle(), span.getTextSize(), new ColorStateList(colorStateList.getStates(), colors), span.getLinkTextColor());
            }
        }
        return span;
    }

    private int processColor(int color) {
        return Color.argb(Color.alpha(color), 255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color));
    }

    private static int findContrastColor(int color, int other, boolean findFg, double minRatio) {
        int bg;
        int fg = findFg ? color : other;
        if (findFg) {
            bg = other;
        } else {
            bg = color;
        }
        if (ColorUtilsFromCompat.calculateContrast(fg, bg) >= minRatio) {
            return color;
        }
        int i;
        double[] lab = new double[3];
        if (findFg) {
            i = fg;
        } else {
            i = bg;
        }
        ColorUtilsFromCompat.colorToLAB(i, lab);
        double low = 0.0d;
        double high = lab[0];
        double a = lab[1];
        double b = lab[2];
        for (int i2 = 0; i2 < 15 && high - low > 1.0E-5d; i2++) {
            double l = (low + high) / 2.0d;
            if (findFg) {
                fg = ColorUtilsFromCompat.LABToColor(l, a, b);
            } else {
                bg = ColorUtilsFromCompat.LABToColor(l, a, b);
            }
            if (ColorUtilsFromCompat.calculateContrast(fg, bg) > minRatio) {
            }
            low = l;
        }
        return ColorUtilsFromCompat.LABToColor(low, a, b);
    }

    public static int ensureLargeTextContrast(int color, int bg) {
        return findContrastColor(color, bg, true, 3.0d);
    }

    private static int ensureTextContrast(int color, int bg) {
        return findContrastColor(color, bg, true, 4.5d);
    }

    public static int ensureTextBackgroundColor(int color, int textColor, int hintColor) {
        return findContrastColor(findContrastColor(color, hintColor, false, 3.0d), textColor, false, 4.5d);
    }

    private static String contrastChange(int colorOld, int colorNew, int bg) {
        Object[] objArr = new Object[2];
        objArr[0] = Double.valueOf(ColorUtilsFromCompat.calculateContrast(colorOld, bg));
        objArr[1] = Double.valueOf(ColorUtilsFromCompat.calculateContrast(colorNew, bg));
        return String.format("from %.2f:1 to %.2f:1", objArr);
    }

    public static int resolveColor(Context context, int color) {
        if (color == 0) {
            return context.getColor(R.color.notification_icon_default_color);
        }
        return color;
    }

    public static int resolveContrastColor(Context context, int notificationColor) {
        int resolvedColor = resolveColor(context, notificationColor);
        int actionBg = context.getColor(R.color.notification_action_list);
        int i = resolvedColor;
        i = ensureTextContrast(ensureLargeTextContrast(resolvedColor, actionBg), context.getColor(R.color.notification_material_background_color));
        if (i != resolvedColor) {
        }
        return i;
    }

    public static int lightenColor(int baseColor, int amount) {
        double[] result = ColorUtilsFromCompat.getTempDouble3Array();
        ColorUtilsFromCompat.colorToLAB(baseColor, result);
        result[0] = Math.min(100.0d, result[0] + ((double) amount));
        return ColorUtilsFromCompat.LABToColor(result[0], result[1], result[2]);
    }
}
