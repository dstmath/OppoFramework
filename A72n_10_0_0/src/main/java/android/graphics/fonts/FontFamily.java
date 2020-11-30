package android.graphics.fonts;

import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.HashSet;
import libcore.util.NativeAllocationRegistry;

public final class FontFamily {
    private static final String TAG = "FontFamily";
    private final ArrayList<Font> mFonts;
    private final long mNativePtr;

    public static final class Builder {
        private static final NativeAllocationRegistry sFamilyRegistory = NativeAllocationRegistry.createMalloced(FontFamily.class.getClassLoader(), nGetReleaseNativeFamily());
        private final ArrayList<Font> mFonts = new ArrayList<>();
        private final HashSet<Integer> mStyleHashSet = new HashSet<>();

        private static native void nAddFont(long j, long j2);

        private static native long nBuild(long j, String str, int i, boolean z);

        private static native long nGetReleaseNativeFamily();

        private static native long nInitBuilder();

        public Builder(Font font) {
            Preconditions.checkNotNull(font, "font can not be null");
            this.mStyleHashSet.add(Integer.valueOf(makeStyleIdentifier(font)));
            this.mFonts.add(font);
        }

        public Builder addFont(Font font) {
            Preconditions.checkNotNull(font, "font can not be null");
            if (this.mStyleHashSet.add(Integer.valueOf(makeStyleIdentifier(font)))) {
                this.mFonts.add(font);
                return this;
            }
            throw new IllegalArgumentException(font + " has already been added");
        }

        public FontFamily build() {
            return build("", 0, true);
        }

        public FontFamily build(String langTags, int variant, boolean isCustomFallback) {
            long builderPtr = nInitBuilder();
            for (int i = 0; i < this.mFonts.size(); i++) {
                nAddFont(builderPtr, this.mFonts.get(i).getNativePtr());
            }
            long ptr = nBuild(builderPtr, langTags, variant, isCustomFallback);
            FontFamily family = new FontFamily(this.mFonts, ptr);
            sFamilyRegistory.registerNativeAllocation(family, ptr);
            return family;
        }

        private static int makeStyleIdentifier(Font font) {
            return font.getStyle().getWeight() | (font.getStyle().getSlant() << 16);
        }
    }

    private FontFamily(ArrayList<Font> fonts, long ptr) {
        this.mFonts = fonts;
        this.mNativePtr = ptr;
    }

    public Font getFont(int index) {
        return this.mFonts.get(index);
    }

    public int getSize() {
        return this.mFonts.size();
    }

    public long getNativePtr() {
        return this.mNativePtr;
    }
}
