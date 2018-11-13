package android.graphics;

import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.FastMath;
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
public class RectF implements Parcelable {
    public static final Creator<RectF> CREATOR = null;
    public float bottom;
    public float left;
    public float right;
    public float top;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.RectF.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.RectF.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.RectF.<clinit>():void");
    }

    public RectF(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public RectF(RectF r) {
        if (r == null) {
            this.bottom = TonemapCurve.LEVEL_BLACK;
            this.right = TonemapCurve.LEVEL_BLACK;
            this.top = TonemapCurve.LEVEL_BLACK;
            this.left = TonemapCurve.LEVEL_BLACK;
            return;
        }
        this.left = r.left;
        this.top = r.top;
        this.right = r.right;
        this.bottom = r.bottom;
    }

    public RectF(Rect r) {
        if (r == null) {
            this.bottom = TonemapCurve.LEVEL_BLACK;
            this.right = TonemapCurve.LEVEL_BLACK;
            this.top = TonemapCurve.LEVEL_BLACK;
            this.left = TonemapCurve.LEVEL_BLACK;
            return;
        }
        this.left = (float) r.left;
        this.top = (float) r.top;
        this.right = (float) r.right;
        this.bottom = (float) r.bottom;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RectF r = (RectF) o;
        if (!(this.left == r.left && this.top == r.top && this.right == r.right && this.bottom == r.bottom)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int result;
        int floatToIntBits;
        int i = 0;
        if (this.left != TonemapCurve.LEVEL_BLACK) {
            result = Float.floatToIntBits(this.left);
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.top != TonemapCurve.LEVEL_BLACK) {
            floatToIntBits = Float.floatToIntBits(this.top);
        } else {
            floatToIntBits = 0;
        }
        i2 = (i2 + floatToIntBits) * 31;
        if (this.right != TonemapCurve.LEVEL_BLACK) {
            floatToIntBits = Float.floatToIntBits(this.right);
        } else {
            floatToIntBits = 0;
        }
        floatToIntBits = (i2 + floatToIntBits) * 31;
        if (this.bottom != TonemapCurve.LEVEL_BLACK) {
            i = Float.floatToIntBits(this.bottom);
        }
        return floatToIntBits + i;
    }

    public String toString() {
        return "RectF(" + this.left + ", " + this.top + ", " + this.right + ", " + this.bottom + ")";
    }

    public String toShortString() {
        return toShortString(new StringBuilder(32));
    }

    public String toShortString(StringBuilder sb) {
        sb.setLength(0);
        sb.append('[');
        sb.append(this.left);
        sb.append(',');
        sb.append(this.top);
        sb.append("][");
        sb.append(this.right);
        sb.append(',');
        sb.append(this.bottom);
        sb.append(']');
        return sb.toString();
    }

    public void printShortString(PrintWriter pw) {
        pw.print('[');
        pw.print(this.left);
        pw.print(',');
        pw.print(this.top);
        pw.print("][");
        pw.print(this.right);
        pw.print(',');
        pw.print(this.bottom);
        pw.print(']');
    }

    public final boolean isEmpty() {
        return this.left >= this.right || this.top >= this.bottom;
    }

    public final float width() {
        return this.right - this.left;
    }

    public final float height() {
        return this.bottom - this.top;
    }

    public final float centerX() {
        return (this.left + this.right) * 0.5f;
    }

    public final float centerY() {
        return (this.top + this.bottom) * 0.5f;
    }

    public void setEmpty() {
        this.bottom = TonemapCurve.LEVEL_BLACK;
        this.top = TonemapCurve.LEVEL_BLACK;
        this.right = TonemapCurve.LEVEL_BLACK;
        this.left = TonemapCurve.LEVEL_BLACK;
    }

    public void set(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void set(RectF src) {
        this.left = src.left;
        this.top = src.top;
        this.right = src.right;
        this.bottom = src.bottom;
    }

    public void set(Rect src) {
        this.left = (float) src.left;
        this.top = (float) src.top;
        this.right = (float) src.right;
        this.bottom = (float) src.bottom;
    }

    public void offset(float dx, float dy) {
        this.left += dx;
        this.top += dy;
        this.right += dx;
        this.bottom += dy;
    }

    public void offsetTo(float newLeft, float newTop) {
        this.right += newLeft - this.left;
        this.bottom += newTop - this.top;
        this.left = newLeft;
        this.top = newTop;
    }

    public void inset(float dx, float dy) {
        this.left += dx;
        this.top += dy;
        this.right -= dx;
        this.bottom -= dy;
    }

    public boolean contains(float x, float y) {
        if (this.left >= this.right || this.top >= this.bottom || x < this.left || x >= this.right || y < this.top || y >= this.bottom) {
            return false;
        }
        return true;
    }

    public boolean contains(float left, float top, float right, float bottom) {
        if (this.left >= this.right || this.top >= this.bottom || this.left > left || this.top > top || this.right < right || this.bottom < bottom) {
            return false;
        }
        return true;
    }

    public boolean contains(RectF r) {
        if (this.left >= this.right || this.top >= this.bottom || this.left > r.left || this.top > r.top || this.right < r.right || this.bottom < r.bottom) {
            return false;
        }
        return true;
    }

    public boolean intersect(float left, float top, float right, float bottom) {
        if (this.left >= right || left >= this.right || this.top >= bottom || top >= this.bottom) {
            return false;
        }
        if (this.left < left) {
            this.left = left;
        }
        if (this.top < top) {
            this.top = top;
        }
        if (this.right > right) {
            this.right = right;
        }
        if (this.bottom > bottom) {
            this.bottom = bottom;
        }
        return true;
    }

    public boolean intersect(RectF r) {
        return intersect(r.left, r.top, r.right, r.bottom);
    }

    public boolean setIntersect(RectF a, RectF b) {
        if (a.left >= b.right || b.left >= a.right || a.top >= b.bottom || b.top >= a.bottom) {
            return false;
        }
        this.left = Math.max(a.left, b.left);
        this.top = Math.max(a.top, b.top);
        this.right = Math.min(a.right, b.right);
        this.bottom = Math.min(a.bottom, b.bottom);
        return true;
    }

    public boolean intersects(float left, float top, float right, float bottom) {
        if (this.left >= right || left >= this.right || this.top >= bottom || top >= this.bottom) {
            return false;
        }
        return true;
    }

    public static boolean intersects(RectF a, RectF b) {
        if (a.left >= b.right || b.left >= a.right || a.top >= b.bottom || b.top >= a.bottom) {
            return false;
        }
        return true;
    }

    public void round(Rect dst) {
        dst.set(FastMath.round(this.left), FastMath.round(this.top), FastMath.round(this.right), FastMath.round(this.bottom));
    }

    public void roundOut(Rect dst) {
        dst.set((int) Math.floor((double) this.left), (int) Math.floor((double) this.top), (int) Math.ceil((double) this.right), (int) Math.ceil((double) this.bottom));
    }

    public void union(float left, float top, float right, float bottom) {
        if (left < right && top < bottom) {
            if (this.left >= this.right || this.top >= this.bottom) {
                this.left = left;
                this.top = top;
                this.right = right;
                this.bottom = bottom;
                return;
            }
            if (this.left > left) {
                this.left = left;
            }
            if (this.top > top) {
                this.top = top;
            }
            if (this.right < right) {
                this.right = right;
            }
            if (this.bottom < bottom) {
                this.bottom = bottom;
            }
        }
    }

    public void union(RectF r) {
        union(r.left, r.top, r.right, r.bottom);
    }

    public void union(float x, float y) {
        if (x < this.left) {
            this.left = x;
        } else if (x > this.right) {
            this.right = x;
        }
        if (y < this.top) {
            this.top = y;
        } else if (y > this.bottom) {
            this.bottom = y;
        }
    }

    public void sort() {
        float temp;
        if (this.left > this.right) {
            temp = this.left;
            this.left = this.right;
            this.right = temp;
        }
        if (this.top > this.bottom) {
            temp = this.top;
            this.top = this.bottom;
            this.bottom = temp;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(this.left);
        out.writeFloat(this.top);
        out.writeFloat(this.right);
        out.writeFloat(this.bottom);
    }

    public void readFromParcel(Parcel in) {
        this.left = in.readFloat();
        this.top = in.readFloat();
        this.right = in.readFloat();
        this.bottom = in.readFloat();
    }
}
