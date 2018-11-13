package com.android.server.policy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.TableMaskFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.android.server.display.OppoBrightUtils;

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
public final class IconUtilities {
    private static final String TAG = "IconUtilities";
    private static final int[] sColors = null;
    private final Paint mBlurPaint;
    private final Canvas mCanvas;
    private int mColorIndex;
    private final DisplayMetrics mDisplayMetrics;
    private final Paint mGlowColorFocusedPaint;
    private final Paint mGlowColorPressedPaint;
    private int mIconHeight;
    private int mIconTextureHeight;
    private int mIconTextureWidth;
    private int mIconWidth;
    private final Rect mOldBounds;
    private final Paint mPaint;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.IconUtilities.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.IconUtilities.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.IconUtilities.<clinit>():void");
    }

    public IconUtilities(Context context) {
        this.mIconWidth = -1;
        this.mIconHeight = -1;
        this.mIconTextureWidth = -1;
        this.mIconTextureHeight = -1;
        this.mPaint = new Paint();
        this.mBlurPaint = new Paint();
        this.mGlowColorPressedPaint = new Paint();
        this.mGlowColorFocusedPaint = new Paint();
        this.mOldBounds = new Rect();
        this.mCanvas = new Canvas();
        this.mColorIndex = 0;
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        this.mDisplayMetrics = metrics;
        float blurPx = 5.0f * metrics.density;
        int dimension = (int) resources.getDimension(17104896);
        this.mIconHeight = dimension;
        this.mIconWidth = dimension;
        dimension = this.mIconWidth + ((int) (2.0f * blurPx));
        this.mIconTextureHeight = dimension;
        this.mIconTextureWidth = dimension;
        this.mBlurPaint.setMaskFilter(new BlurMaskFilter(blurPx, Blur.NORMAL));
        TypedValue value = new TypedValue();
        this.mGlowColorPressedPaint.setColor(context.getTheme().resolveAttribute(16843661, value, true) ? value.data : -15616);
        this.mGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        this.mGlowColorFocusedPaint.setColor(context.getTheme().resolveAttribute(16843663, value, true) ? value.data : -29184);
        this.mGlowColorFocusedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
        new ColorMatrix().setSaturation(0.2f);
        this.mCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
    }

    public Drawable createIconDrawable(Drawable src) {
        Bitmap scaled = createIconBitmap(src);
        StateListDrawable result = new StateListDrawable();
        int[] iArr = new int[1];
        iArr[0] = 16842908;
        result.addState(iArr, new BitmapDrawable(createSelectedBitmap(scaled, false)));
        iArr = new int[1];
        iArr[0] = 16842919;
        result.addState(iArr, new BitmapDrawable(createSelectedBitmap(scaled, true)));
        result.addState(new int[0], new BitmapDrawable(scaled));
        result.setBounds(0, 0, this.mIconTextureWidth, this.mIconTextureHeight);
        return result;
    }

    public Bitmap createIconBitmap(Drawable icon) {
        int width = this.mIconWidth;
        int height = this.mIconHeight;
        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            if (bitmapDrawable.getBitmap().getDensity() == 0) {
                bitmapDrawable.setTargetDensity(this.mDisplayMetrics);
            }
        }
        int sourceWidth = icon.getIntrinsicWidth();
        int sourceHeight = icon.getIntrinsicHeight();
        if (sourceWidth > 0 && sourceHeight > 0) {
            if (width < sourceWidth || height < sourceHeight) {
                float ratio = ((float) sourceWidth) / ((float) sourceHeight);
                if (sourceWidth > sourceHeight) {
                    height = (int) (((float) width) / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (((float) height) * ratio);
                }
            } else if (sourceWidth < width && sourceHeight < height) {
                width = sourceWidth;
                height = sourceHeight;
            }
        }
        int textureWidth = this.mIconTextureWidth;
        int textureHeight = this.mIconTextureHeight;
        Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Config.ARGB_8888);
        Canvas canvas = this.mCanvas;
        canvas.setBitmap(bitmap);
        int left = (textureWidth - width) / 2;
        int top = (textureHeight - height) / 2;
        this.mOldBounds.set(icon.getBounds());
        icon.setBounds(left, top, left + width, top + height);
        icon.draw(canvas);
        icon.setBounds(this.mOldBounds);
        return bitmap;
    }

    private Bitmap createSelectedBitmap(Bitmap src, boolean pressed) {
        Bitmap result = Bitmap.createBitmap(this.mIconTextureWidth, this.mIconTextureHeight, Config.ARGB_8888);
        Canvas dest = new Canvas(result);
        dest.drawColor(0, Mode.CLEAR);
        int[] xy = new int[2];
        Bitmap mask = src.extractAlpha(this.mBlurPaint, xy);
        dest.drawBitmap(mask, (float) xy[0], (float) xy[1], pressed ? this.mGlowColorPressedPaint : this.mGlowColorFocusedPaint);
        mask.recycle();
        dest.drawBitmap(src, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, this.mPaint);
        dest.setBitmap(null);
        return result;
    }
}
