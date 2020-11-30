package android.graphics;

import android.os.AsyncTask;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ColorPalette {
    private static final String LOG_TAG = "ColorPalette";
    private List<Integer> mSwatches;

    public interface PaletteAsyncListener {
        void onGenerated(ColorPalette colorPalette);
    }

    ColorPalette(List<Integer> swatches) {
        this.mSwatches = swatches;
    }

    public static Builder from(Bitmap bitmap) {
        return new Builder(bitmap);
    }

    public List<Integer> getSortedColorList() {
        return this.mSwatches;
    }

    public int getTransMaxColor(int defaultColor) {
        float[] hsv = new float[3];
        for (Integer num : this.mSwatches) {
            Color.colorToHSV(num.intValue(), hsv);
            float s = hsv[1];
            float v = hsv[2];
            if ((s < 0.0f || ((double) s) > 0.05d || v < 0.0f || v > 1.0f) && (s < 0.0f || s > 1.0f || v < 0.0f || ((double) v) > 0.05d)) {
                return Color.HSVToColor(hsv);
            }
        }
        return defaultColor;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005a, code lost:
        if (r15 <= 1.0f) goto L_0x0096;
     */
    public int[] getTransTwoColor() {
        int[] transColor;
        int[] transColor2;
        char c = 2;
        int[] transColor3 = new int[2];
        float[] hsv = new float[3];
        boolean hasMax = false;
        boolean hasSecond = false;
        int maxColor = -16777216;
        int secondColor = -1;
        Iterator<Integer> it = this.mSwatches.iterator();
        while (true) {
            if (!it.hasNext()) {
                transColor = transColor3;
                break;
            }
            int color = it.next().intValue();
            Color.colorToHSV(color, hsv);
            if (hasMax && hasSecond) {
                transColor = transColor3;
                break;
            }
            Color.colorToHSV((16777215 & color) | -16777216, hsv);
            float s = hsv[1];
            float v = hsv[c];
            if (s >= 0.0f) {
                transColor2 = transColor3;
                if (((double) s) <= 0.05d) {
                    if (v >= 0.0f) {
                    }
                }
            } else {
                transColor2 = transColor3;
            }
            if (s < 0.0f || s > 1.0f || v < 0.0f || ((double) v) > 0.05d) {
                if (!hasMax) {
                    hsv[1] = 0.2f;
                    hsv[2] = 1.0f;
                    maxColor = Color.HSVToColor(hsv);
                    hasMax = true;
                    transColor3 = transColor2;
                    c = 2;
                } else if (!hasSecond) {
                    hsv[1] = 0.05f;
                    hsv[2] = 1.0f;
                    secondColor = Color.HSVToColor(hsv);
                    hasSecond = true;
                    transColor3 = transColor2;
                    c = 2;
                }
            }
            transColor3 = transColor2;
            c = 2;
        }
        if (!hasSecond && !hasMax) {
            hsv[0] = 0.0f;
            hsv[1] = 0.0f;
            hsv[2] = 0.9f;
            maxColor = Color.HSVToColor(hsv);
            hsv[2] = 0.98f;
            secondColor = Color.HSVToColor(hsv);
            hasSecond = true;
        }
        if (!hasSecond) {
            Color.colorToHSV(maxColor, hsv);
            hsv[1] = 0.05f;
            hsv[2] = 1.0f;
            secondColor = Color.HSVToColor(hsv);
        }
        transColor[0] = maxColor;
        transColor[1] = secondColor;
        return transColor;
    }

    public static final class Builder {
        private final Bitmap mBitmap;

        public Builder(Bitmap bitmap) {
            if (bitmap == null || bitmap.isRecycled()) {
                throw new IllegalArgumentException("Bitmap is not valid");
            }
            this.mBitmap = bitmap;
        }

        private int[] getPixelsInStep(int step) {
            if (step <= 0) {
                return null;
            }
            int pixelWidth = (int) ((((float) this.mBitmap.getWidth()) * 1.0f) / ((float) step));
            int pixelHeight = (int) ((((float) this.mBitmap.getHeight()) * 1.0f) / ((float) step));
            int[] pixels = new int[(pixelWidth * pixelHeight)];
            int x = (int) (((float) step) / 2.0f);
            while (x < this.mBitmap.getWidth()) {
                int y = (int) (((float) step) / 2.0f);
                while (y < this.mBitmap.getHeight()) {
                    int px = x / step;
                    int py = y / step;
                    if (px < pixelWidth && py < pixelHeight) {
                        pixels[(py * pixelWidth) + px] = this.mBitmap.getPixel(x, y);
                    }
                    y += step;
                }
                x += step;
            }
            return pixels;
        }

        private int[] getEdgePixelsInStep(int step, int offset) {
            if (step <= 0 || offset <= 0) {
                return null;
            }
            int pixelWidth = (int) ((((float) this.mBitmap.getWidth()) * 1.0f) / ((float) step));
            int pixelHeight = (int) ((((float) this.mBitmap.getHeight()) * 1.0f) / ((float) step));
            int[] pixels = new int[((pixelWidth + pixelHeight) * 2)];
            int x = (int) (((float) step) / 2.0f);
            while (x < this.mBitmap.getWidth()) {
                int px = x / step;
                if (px < pixelWidth) {
                    pixels[px] = this.mBitmap.getPixel(x, offset);
                }
                x += step;
            }
            int x2 = (int) (((float) step) / 2.0f);
            while (x2 < this.mBitmap.getWidth()) {
                int px2 = x2 / step;
                if (px2 < pixelWidth) {
                    Bitmap bitmap = this.mBitmap;
                    pixels[px2 + pixelWidth] = bitmap.getPixel(x2, bitmap.getHeight() - offset);
                }
                x2 += step;
            }
            int y = (int) (((float) step) / 2.0f);
            while (y < this.mBitmap.getHeight()) {
                int py = y / step;
                if (py < pixelHeight) {
                    pixels[(pixelWidth * 2) + py] = this.mBitmap.getPixel(offset, y);
                }
                y += step;
            }
            int y2 = (int) (((float) step) / 2.0f);
            while (y2 < this.mBitmap.getHeight()) {
                int py2 = y2 / step;
                if (py2 < pixelHeight) {
                    Bitmap bitmap2 = this.mBitmap;
                    pixels[(pixelWidth * 2) + pixelHeight + py2] = bitmap2.getPixel(bitmap2.getWidth() - offset, y2);
                }
                y2 += step;
            }
            return pixels;
        }

        private int[] getEveryPixels() {
            int bitmapWidth = this.mBitmap.getWidth();
            int bitmapHeight = this.mBitmap.getHeight();
            int[] pixels = new int[(bitmapWidth * bitmapHeight)];
            this.mBitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
            return pixels;
        }

        private List<Integer> genetateSwatches(int[] pixels) {
            HashMap<Integer, Integer> hm = new ColorSmartCutQuantizer(pixels).getQuantizedColors();
            List<Map.Entry<Integer, Integer>> list = new ArrayList<>();
            list.addAll(hm.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
                /* class android.graphics.ColorPalette.Builder.AnonymousClass1 */

                public int compare(Map.Entry<Integer, Integer> m, Map.Entry<Integer, Integer> n) {
                    return n.getValue().intValue() - m.getValue().intValue();
                }
            });
            List<Integer> swatches = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                swatches.add(list.get(i).getKey());
            }
            return swatches;
        }

        public ColorPalette generate() {
            return new ColorPalette(genetateSwatches(getEveryPixels()));
        }

        public ColorPalette generateWithStep(int step) {
            return new ColorPalette(genetateSwatches(getPixelsInStep(step)));
        }

        public ColorPalette generateEdageWithStep(int step, int offset) {
            return new ColorPalette(genetateSwatches(getEdgePixelsInStep(step, offset)));
        }

        public AsyncTask<Bitmap, Void, ColorPalette> generate(final PaletteAsyncListener listener) {
            if (listener != null) {
                return new AsyncTask<Bitmap, Void, ColorPalette>() {
                    /* class android.graphics.ColorPalette.Builder.AnonymousClass2 */

                    /* access modifiers changed from: protected */
                    public ColorPalette doInBackground(Bitmap... params) {
                        try {
                            return Builder.this.generate();
                        } catch (Exception e) {
                            Log.e(ColorPalette.LOG_TAG, "Exception thrown during async generate", e);
                            return null;
                        }
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(ColorPalette colorExtractor) {
                        listener.onGenerated(colorExtractor);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.mBitmap);
            }
            throw new IllegalArgumentException("listener can not be null");
        }
    }
}
