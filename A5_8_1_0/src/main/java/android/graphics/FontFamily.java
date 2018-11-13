package android.graphics;

import android.content.res.AssetManager;
import android.graphics.fonts.FontVariationAxis;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class FontFamily {
    private static String TAG = "FontFamily";
    private long mBuilderPtr;
    public long mNativePtr;

    private static native void nAbort(long j);

    private static native void nAddAxisValue(long j, int i, float f);

    private static native boolean nAddFont(long j, ByteBuffer byteBuffer, int i, int i2, int i3);

    private static native boolean nAddFontFromAssetManager(long j, AssetManager assetManager, String str, int i, boolean z, int i2, int i3, int i4);

    private static native boolean nAddFontWeightStyle(long j, ByteBuffer byteBuffer, int i, int i2, int i3);

    private static native void nAllowUnsupportedFont(long j);

    private static native long nCreateFamily(long j);

    private static native long nInitBuilder(String str, int i);

    private static native void nUnrefFamily(long j);

    public FontFamily() {
        this.mBuilderPtr = nInitBuilder(null, 0);
    }

    public FontFamily(String lang, int variant) {
        this.mBuilderPtr = nInitBuilder(lang, variant);
    }

    public boolean freeze() {
        if (this.mBuilderPtr == 0) {
            throw new IllegalStateException("This FontFamily is already frozen");
        }
        this.mNativePtr = nCreateFamily(this.mBuilderPtr);
        this.mBuilderPtr = 0;
        return this.mNativePtr != 0;
    }

    public void abortCreation() {
        if (this.mBuilderPtr == 0) {
            throw new IllegalStateException("This FontFamily is already frozen or abandoned");
        }
        nAbort(this.mBuilderPtr);
        this.mBuilderPtr = 0;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mNativePtr != 0) {
                nUnrefFamily(this.mNativePtr);
            }
            if (this.mBuilderPtr != 0) {
                nAbort(this.mBuilderPtr);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x008f A:{SYNTHETIC, Splitter: B:32:0x008f} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a2 A:{Catch:{ IOException -> 0x0095 }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0094 A:{SYNTHETIC, Splitter: B:35:0x0094} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean addFont(String path, int ttcIndex, FontVariationAxis[] axes, int weight, int italic) {
        Throwable th;
        Throwable th2;
        if (this.mBuilderPtr == 0) {
            throw new IllegalStateException("Unable to call addFont after freezing.");
        }
        Throwable th3 = null;
        FileInputStream file = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            try {
                FileChannel fileChannel = fileInputStream.getChannel();
                ByteBuffer fontBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
                if (axes != null) {
                    for (FontVariationAxis axis : axes) {
                        nAddAxisValue(this.mBuilderPtr, axis.getOpenTypeTagValue(), axis.getStyleValue());
                    }
                }
                boolean nAddFont = nAddFont(this.mBuilderPtr, fontBuffer, ttcIndex, weight, italic);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                if (th3 == null) {
                    return nAddFont;
                }
                try {
                    throw th3;
                } catch (IOException e) {
                    file = fileInputStream;
                }
            } catch (Throwable th5) {
                th = th5;
                file = fileInputStream;
                th2 = null;
                if (file != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th6) {
            th = th6;
            th2 = null;
            if (file != null) {
                try {
                    file.close();
                } catch (Throwable th7) {
                    if (th2 == null) {
                        th2 = th7;
                    } else if (th2 != th7) {
                        th2.addSuppressed(th7);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (IOException e2) {
                    Log.e(TAG, "Error mapping font file " + path);
                    return false;
                }
            }
            throw th;
        }
    }

    public boolean addFontFromBuffer(ByteBuffer font, int ttcIndex, FontVariationAxis[] axes, int weight, int italic) {
        if (this.mBuilderPtr == 0) {
            throw new IllegalStateException("Unable to call addFontWeightStyle after freezing.");
        }
        if (axes != null) {
            for (FontVariationAxis axis : axes) {
                nAddAxisValue(this.mBuilderPtr, axis.getOpenTypeTagValue(), axis.getStyleValue());
            }
        }
        return nAddFontWeightStyle(this.mBuilderPtr, font, ttcIndex, weight, italic);
    }

    public boolean addFontFromAssetManager(AssetManager mgr, String path, int cookie, boolean isAsset, int ttcIndex, int weight, int isItalic, FontVariationAxis[] axes) {
        if (this.mBuilderPtr == 0) {
            throw new IllegalStateException("Unable to call addFontFromAsset after freezing.");
        }
        if (axes != null) {
            for (FontVariationAxis axis : axes) {
                nAddAxisValue(this.mBuilderPtr, axis.getOpenTypeTagValue(), axis.getStyleValue());
            }
        }
        return nAddFontFromAssetManager(this.mBuilderPtr, mgr, path, cookie, isAsset, ttcIndex, weight, isItalic);
    }

    public void allowUnsupportedFont() {
        if (this.mBuilderPtr == 0) {
            throw new IllegalStateException("Unable to allow unsupported font.");
        }
        nAllowUnsupportedFont(this.mBuilderPtr);
    }

    private static boolean nAddFont(long builderPtr, ByteBuffer font, int ttcIndex) {
        return nAddFont(builderPtr, font, ttcIndex, -1, -1);
    }
}
