package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.proto.ProtoInputStream;
import android.util.proto.ProtoOutputStream;
import android.util.proto.WireTypeMismatchException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Rect implements Parcelable {
    public static final Parcelable.Creator<Rect> CREATOR = new Parcelable.Creator<Rect>() {
        /* class android.graphics.Rect.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Rect createFromParcel(Parcel in) {
            Rect r = new Rect();
            r.readFromParcel(in);
            return r;
        }

        @Override // android.os.Parcelable.Creator
        public Rect[] newArray(int size) {
            return new Rect[size];
        }
    };
    public int bottom;
    public int left;
    public int right;
    public int top;

    private static final class UnflattenHelper {
        private static final Pattern FLATTENED_PATTERN = Pattern.compile("(-?\\d+) (-?\\d+) (-?\\d+) (-?\\d+)");

        private UnflattenHelper() {
        }

        static Matcher getMatcher(String str) {
            return FLATTENED_PATTERN.matcher(str);
        }
    }

    public Rect() {
    }

    public Rect(int left2, int top2, int right2, int bottom2) {
        this.left = left2;
        this.top = top2;
        this.right = right2;
        this.bottom = bottom2;
    }

    public Rect(Rect r) {
        if (r == null) {
            this.bottom = 0;
            this.right = 0;
            this.top = 0;
            this.left = 0;
            return;
        }
        this.left = r.left;
        this.top = r.top;
        this.right = r.right;
        this.bottom = r.bottom;
    }

    public Rect(Insets r) {
        if (r == null) {
            this.bottom = 0;
            this.right = 0;
            this.top = 0;
            this.left = 0;
            return;
        }
        this.left = r.left;
        this.top = r.top;
        this.right = r.right;
        this.bottom = r.bottom;
    }

    public static Rect copyOrNull(Rect r) {
        if (r == null) {
            return null;
        }
        return new Rect(r);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rect r = (Rect) o;
        if (this.left == r.left && this.top == r.top && this.right == r.right && this.bottom == r.bottom) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((((this.left * 31) + this.top) * 31) + this.right) * 31) + this.bottom;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Rect(");
        sb.append(this.left);
        sb.append(", ");
        sb.append(this.top);
        sb.append(" - ");
        sb.append(this.right);
        sb.append(", ");
        sb.append(this.bottom);
        sb.append(")");
        return sb.toString();
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

    public String flattenToString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append(this.left);
        sb.append(' ');
        sb.append(this.top);
        sb.append(' ');
        sb.append(this.right);
        sb.append(' ');
        sb.append(this.bottom);
        return sb.toString();
    }

    public static Rect unflattenFromString(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        Matcher matcher = UnflattenHelper.getMatcher(str);
        if (!matcher.matches()) {
            return null;
        }
        return new Rect(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
    }

    @UnsupportedAppUsage
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

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.util.proto.ProtoOutputStream.write(long, int):void
     arg types: [int, int]
     candidates:
      android.util.proto.ProtoOutputStream.write(long, double):void
      android.util.proto.ProtoOutputStream.write(long, float):void
      android.util.proto.ProtoOutputStream.write(long, long):void
      android.util.proto.ProtoOutputStream.write(long, java.lang.String):void
      android.util.proto.ProtoOutputStream.write(long, boolean):void
      android.util.proto.ProtoOutputStream.write(long, byte[]):void
      android.util.proto.ProtoOutputStream.write(long, int):void */
    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId) {
        long token = protoOutputStream.start(fieldId);
        protoOutputStream.write(1120986464257L, this.left);
        protoOutputStream.write(1120986464258L, this.top);
        protoOutputStream.write(1120986464259L, this.right);
        protoOutputStream.write(1120986464260L, this.bottom);
        protoOutputStream.end(token);
    }

    public void readFromProto(ProtoInputStream proto, long fieldId) throws IOException, WireTypeMismatchException {
        long token = proto.start(fieldId);
        while (proto.nextField() != -1) {
            try {
                int fieldNumber = proto.getFieldNumber();
                if (fieldNumber == 1) {
                    this.left = proto.readInt(1120986464257L);
                } else if (fieldNumber == 2) {
                    this.top = proto.readInt(1120986464258L);
                } else if (fieldNumber == 3) {
                    this.right = proto.readInt(1120986464259L);
                } else if (fieldNumber == 4) {
                    this.bottom = proto.readInt(1120986464260L);
                }
            } finally {
                proto.end(token);
            }
        }
    }

    public final boolean isEmpty() {
        return this.left >= this.right || this.top >= this.bottom;
    }

    public final int width() {
        return this.right - this.left;
    }

    public final int height() {
        return this.bottom - this.top;
    }

    public final int centerX() {
        return (this.left + this.right) >> 1;
    }

    public final int centerY() {
        return (this.top + this.bottom) >> 1;
    }

    public final float exactCenterX() {
        return ((float) (this.left + this.right)) * 0.5f;
    }

    public final float exactCenterY() {
        return ((float) (this.top + this.bottom)) * 0.5f;
    }

    public void setEmpty() {
        this.bottom = 0;
        this.top = 0;
        this.right = 0;
        this.left = 0;
    }

    public void set(int left2, int top2, int right2, int bottom2) {
        this.left = left2;
        this.top = top2;
        this.right = right2;
        this.bottom = bottom2;
    }

    public void set(Rect src) {
        this.left = src.left;
        this.top = src.top;
        this.right = src.right;
        this.bottom = src.bottom;
    }

    public void offset(int dx, int dy) {
        this.left += dx;
        this.top += dy;
        this.right += dx;
        this.bottom += dy;
    }

    public void offsetTo(int newLeft, int newTop) {
        this.right += newLeft - this.left;
        this.bottom += newTop - this.top;
        this.left = newLeft;
        this.top = newTop;
    }

    public void inset(int dx, int dy) {
        this.left += dx;
        this.top += dy;
        this.right -= dx;
        this.bottom -= dy;
    }

    public void inset(Rect insets) {
        this.left += insets.left;
        this.top += insets.top;
        this.right -= insets.right;
        this.bottom -= insets.bottom;
    }

    public void inset(Insets insets) {
        this.left += insets.left;
        this.top += insets.top;
        this.right -= insets.right;
        this.bottom -= insets.bottom;
    }

    public void inset(int left2, int top2, int right2, int bottom2) {
        this.left += left2;
        this.top += top2;
        this.right -= right2;
        this.bottom -= bottom2;
    }

    public boolean contains(int x, int y) {
        int i;
        int i2;
        int i3 = this.left;
        int i4 = this.right;
        return i3 < i4 && (i = this.top) < (i2 = this.bottom) && x >= i3 && x < i4 && y >= i && y < i2;
    }

    public boolean contains(int left2, int top2, int right2, int bottom2) {
        int i;
        int i2;
        int i3 = this.left;
        int i4 = this.right;
        return i3 < i4 && (i = this.top) < (i2 = this.bottom) && i3 <= left2 && i <= top2 && i4 >= right2 && i2 >= bottom2;
    }

    public boolean contains(Rect r) {
        int i;
        int i2;
        int i3 = this.left;
        int i4 = this.right;
        return i3 < i4 && (i = this.top) < (i2 = this.bottom) && i3 <= r.left && i <= r.top && i4 >= r.right && i2 >= r.bottom;
    }

    public boolean intersect(int left2, int top2, int right2, int bottom2) {
        int i = this.left;
        if (i >= right2 || left2 >= this.right || this.top >= bottom2 || top2 >= this.bottom) {
            return false;
        }
        if (i < left2) {
            this.left = left2;
        }
        if (this.top < top2) {
            this.top = top2;
        }
        if (this.right > right2) {
            this.right = right2;
        }
        if (this.bottom <= bottom2) {
            return true;
        }
        this.bottom = bottom2;
        return true;
    }

    public boolean intersect(Rect r) {
        return intersect(r.left, r.top, r.right, r.bottom);
    }

    public void intersectUnchecked(Rect other) {
        this.left = Math.max(this.left, other.left);
        this.top = Math.max(this.top, other.top);
        this.right = Math.min(this.right, other.right);
        this.bottom = Math.min(this.bottom, other.bottom);
    }

    public boolean setIntersect(Rect a, Rect b) {
        int i;
        int i2 = a.left;
        if (i2 >= b.right || (i = b.left) >= a.right || a.top >= b.bottom || b.top >= a.bottom) {
            return false;
        }
        this.left = Math.max(i2, i);
        this.top = Math.max(a.top, b.top);
        this.right = Math.min(a.right, b.right);
        this.bottom = Math.min(a.bottom, b.bottom);
        return true;
    }

    public boolean intersects(int left2, int top2, int right2, int bottom2) {
        return this.left < right2 && left2 < this.right && this.top < bottom2 && top2 < this.bottom;
    }

    public static boolean intersects(Rect a, Rect b) {
        return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom;
    }

    public void union(int left2, int top2, int right2, int bottom2) {
        if (left2 < right2 && top2 < bottom2) {
            int i = this.left;
            if (i >= this.right || this.top >= this.bottom) {
                this.left = left2;
                this.top = top2;
                this.right = right2;
                this.bottom = bottom2;
                return;
            }
            if (i > left2) {
                this.left = left2;
            }
            if (this.top > top2) {
                this.top = top2;
            }
            if (this.right < right2) {
                this.right = right2;
            }
            if (this.bottom < bottom2) {
                this.bottom = bottom2;
            }
        }
    }

    public void union(Rect r) {
        union(r.left, r.top, r.right, r.bottom);
    }

    public void union(int x, int y) {
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
        int i = this.left;
        int i2 = this.right;
        if (i > i2) {
            int temp = this.left;
            this.left = i2;
            this.right = temp;
        }
        int temp2 = this.top;
        int i3 = this.bottom;
        if (temp2 > i3) {
            int temp3 = this.top;
            this.top = i3;
            this.bottom = temp3;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.left);
        out.writeInt(this.top);
        out.writeInt(this.right);
        out.writeInt(this.bottom);
    }

    public void readFromParcel(Parcel in) {
        this.left = in.readInt();
        this.top = in.readInt();
        this.right = in.readInt();
        this.bottom = in.readInt();
    }

    @UnsupportedAppUsage
    public void scale(float scale) {
        if (scale != 1.0f) {
            this.left = (int) ((((float) this.left) * scale) + 0.5f);
            this.top = (int) ((((float) this.top) * scale) + 0.5f);
            this.right = (int) ((((float) this.right) * scale) + 0.5f);
            this.bottom = (int) ((((float) this.bottom) * scale) + 0.5f);
        }
    }
}
