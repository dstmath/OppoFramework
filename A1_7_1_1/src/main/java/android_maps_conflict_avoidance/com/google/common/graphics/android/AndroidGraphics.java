package android_maps_conflict_avoidance.com.google.common.graphics.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleGraphics;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;

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
public class AndroidGraphics implements GoogleGraphics {
    private static final Rect clipRect = null;
    private static final Rect destRect = null;
    private static final RectF oval = null;
    private static final Rect sourceRect = null;
    private Canvas canvas;
    private final Paint paint;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidGraphics.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidGraphics.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidGraphics.<clinit>():void");
    }

    public AndroidGraphics(Canvas c) {
        this.paint = new Paint();
        this.paint.setStrokeWidth(1.0f);
        this.canvas = c;
    }

    public void setCanvas(Canvas c) {
        this.canvas = c;
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    public void setColor(int color) {
        this.paint.setColor(-16777216 | color);
    }

    public void fillRect(int x, int y, int width, int height) {
        this.paint.setStyle(Style.FILL);
        this.canvas.drawRect((float) x, (float) y, (float) (x + width), (float) (y + height), this.paint);
    }

    public void drawImage(GoogleImage img, int x, int y) {
        if (img != null) {
            img.drawImage(this, x, y);
        }
    }

    public boolean drawScaledImage(GoogleImage image, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh) {
        if (image == null) {
            return false;
        }
        Bitmap bitmap = ((AndroidImage) image).getBitmap();
        if (bitmap == null) {
            return false;
        }
        sourceRect.set(sx, sy, sx + sw, sy + sh);
        destRect.set(dx, dy, dx + dw, dy + dh);
        this.canvas.drawBitmap(bitmap, sourceRect, destRect, null);
        return true;
    }
}
