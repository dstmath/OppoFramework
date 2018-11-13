package com.android.server;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Atlas;
import android.graphics.Atlas.Entry;
import android.graphics.Atlas.Type;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.GraphicBuffer;
import android.view.IAssetAtlas.Stub;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AssetAtlasService extends Stub {
    public static final String ASSET_ATLAS_SERVICE = "assetatlas";
    private static final int ATLAS_MAP_ENTRY_FIELD_COUNT = 3;
    private static final boolean DEBUG_ATLAS = true;
    private static final boolean DEBUG_ATLAS_TEXTURE = false;
    private static final int GRAPHIC_BUFFER_USAGE = 256;
    private static final String LOG_TAG = "AssetAtlasService";
    private static final int MAX_SIZE = 2048;
    private static final int MIN_SIZE = 512;
    private static final float PACKING_THRESHOLD = 0.8f;
    private static final int STEP = 64;
    private long[] mAtlasMap;
    private final AtomicBoolean mAtlasReady = new AtomicBoolean(false);
    private GraphicBuffer mBuffer;
    private final Context mContext;
    private final String mVersionName;

    private static class ComputeWorker implements Runnable {
        private final List<Bitmap> mBitmaps;
        private final int mEnd;
        private final List<WorkerResult> mResults;
        private final CountDownLatch mSignal;
        private final int mStart;
        private final int mStep;
        private final int mThreshold;

        ComputeWorker(int start, int end, int step, List<Bitmap> bitmaps, int pixelCount, List<WorkerResult> results, CountDownLatch signal) {
            this.mStart = start;
            this.mEnd = end;
            this.mStep = step;
            this.mBitmaps = bitmaps;
            this.mResults = results;
            this.mSignal = signal;
            int threshold = (int) (((float) pixelCount) * AssetAtlasService.PACKING_THRESHOLD);
            while (threshold > 4194304) {
                threshold >>= 1;
            }
            this.mThreshold = threshold;
        }

        public void run() {
            Log.d(AssetAtlasService.LOG_TAG, "Running " + Thread.currentThread().getName());
            Entry entry = new Entry();
            int width = this.mEnd;
            while (width > this.mStart) {
                for (int height = 2048; height > 512; height -= 64) {
                    if (width * height > this.mThreshold) {
                        boolean packSuccess = false;
                        for (Type type : Type.values()) {
                            int count = packBitmaps(type, width, height, entry);
                            if (count > 0) {
                                this.mResults.add(new WorkerResult(type, width, height, count));
                                if (count == this.mBitmaps.size()) {
                                    packSuccess = true;
                                    break;
                                }
                            }
                        }
                        if (!packSuccess) {
                            break;
                        }
                    }
                }
                width -= this.mStep;
            }
            if (this.mSignal != null) {
                this.mSignal.countDown();
            }
        }

        private int packBitmaps(Type type, int width, int height, Entry entry) {
            int total = 0;
            Atlas atlas = new Atlas(type, width, height);
            int count = this.mBitmaps.size();
            for (int i = 0; i < count; i++) {
                Bitmap bitmap = (Bitmap) this.mBitmaps.get(i);
                if (atlas.pack(bitmap.getWidth(), bitmap.getHeight(), entry) != null) {
                    total++;
                }
            }
            return total;
        }
    }

    private static class Configuration {
        final int count;
        final int flags;
        final int height;
        final Type type;
        final int width;

        Configuration(Type type, int width, int height, int count) {
            this(type, width, height, count, 2);
        }

        Configuration(Type type, int width, int height, int count, int flags) {
            this.type = type;
            this.width = width;
            this.height = height;
            this.count = count;
            this.flags = flags;
        }

        public String toString() {
            return this.type.toString() + " (" + this.width + "x" + this.height + ") flags=0x" + Integer.toHexString(this.flags) + " count=" + this.count;
        }
    }

    private class Renderer implements Runnable {
        private final ArrayList<Bitmap> mBitmaps;
        private final int mPixelCount;

        Renderer(ArrayList<Bitmap> bitmaps, int pixelCount) {
            this.mBitmaps = bitmaps;
            this.mPixelCount = pixelCount;
        }

        public void run() {
            Configuration config = AssetAtlasService.this.chooseConfiguration(this.mBitmaps, this.mPixelCount, AssetAtlasService.this.mVersionName);
            Log.d(AssetAtlasService.LOG_TAG, "Loaded configuration: " + config);
            if (config != null) {
                AssetAtlasService.this.mBuffer = GraphicBuffer.create(config.width, config.height, 1, 256);
                if (AssetAtlasService.this.mBuffer != null) {
                    if (renderAtlas(AssetAtlasService.this.mBuffer, new Atlas(config.type, config.width, config.height, config.flags), config.count)) {
                        AssetAtlasService.this.mAtlasReady.set(true);
                    }
                }
            }
        }

        private boolean renderAtlas(GraphicBuffer buffer, Atlas atlas, int packCount) {
            new Paint().setXfermode(new PorterDuffXfermode(Mode.SRC));
            Bitmap atlasBitmap = Bitmap.createBitmap(buffer.getWidth(), buffer.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(atlasBitmap);
            Entry entry = new Entry();
            AssetAtlasService.this.mAtlasMap = new long[(packCount * 3)];
            long[] atlasMap = AssetAtlasService.this.mAtlasMap;
            long startRender = System.nanoTime();
            int count = this.mBitmaps.size();
            int i = 0;
            int mapIndex = 0;
            while (i < count) {
                int i2;
                Bitmap bitmap = (Bitmap) this.mBitmaps.get(i);
                if (atlas.pack(bitmap.getWidth(), bitmap.getHeight(), entry) != null) {
                    if (mapIndex >= AssetAtlasService.this.mAtlasMap.length) {
                        AssetAtlasService.deleteDataFile();
                        break;
                    }
                    canvas.save();
                    canvas.translate((float) entry.x, (float) entry.y);
                    canvas.drawBitmap(bitmap, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, null);
                    canvas.restore();
                    i2 = mapIndex + 1;
                    atlasMap[mapIndex] = bitmap.refSkPixelRef();
                    mapIndex = i2 + 1;
                    atlasMap[i2] = (long) entry.x;
                    i2 = mapIndex + 1;
                    atlasMap[mapIndex] = (long) entry.y;
                } else {
                    i2 = mapIndex;
                }
                i++;
                mapIndex = i2;
            }
            long endRender = System.nanoTime();
            releaseCanvas(canvas, atlasBitmap);
            boolean result = AssetAtlasService.nUploadAtlas(buffer, atlasBitmap);
            atlasBitmap.recycle();
            float renderDuration = (((float) (endRender - startRender)) / 1000.0f) / 1000.0f;
            float uploadDuration = (((float) (System.nanoTime() - endRender)) / 1000.0f) / 1000.0f;
            Log.d(AssetAtlasService.LOG_TAG, String.format("Rendered atlas in %.2fms (%.2f+%.2fms)", new Object[]{Float.valueOf(renderDuration + uploadDuration), Float.valueOf(renderDuration), Float.valueOf(uploadDuration)}));
            return result;
        }

        private void releaseCanvas(Canvas canvas, Bitmap atlasBitmap) {
            canvas.setBitmap(null);
        }
    }

    private static class WorkerResult {
        int count;
        int height;
        Type type;
        int width;

        WorkerResult(Type type, int width, int height, int count) {
            this.type = type;
            this.width = width;
            this.height = height;
            this.count = count;
        }

        public String toString() {
            return String.format("%s %dx%d", new Object[]{this.type.toString(), Integer.valueOf(this.width), Integer.valueOf(this.height)});
        }
    }

    private static native boolean nUploadAtlas(GraphicBuffer graphicBuffer, Bitmap bitmap);

    public AssetAtlasService(Context context) {
        this.mContext = context;
        this.mVersionName = queryVersionName(context);
        Collection<Bitmap> bitmaps = new HashSet(300);
        int totalPixelCount = 0;
        LongSparseArray<ConstantState> drawables = context.getResources().getPreloadedDrawables();
        Log.d(LOG_TAG, "AssetAtlasService: size of preload drawables is " + drawables.size());
        int i = 0;
        while (i < drawables.size()) {
            try {
                totalPixelCount += ((ConstantState) drawables.valueAt(i)).addAtlasableBitmaps(bitmaps);
                i++;
            } catch (Throwable t) {
                Log.e("AssetAtlas", "Failed to fetch preloaded drawable state", t);
            }
        }
        ArrayList<Bitmap> sortedBitmaps = new ArrayList(bitmaps);
        Collections.sort(sortedBitmaps, new Comparator<Bitmap>() {
            public int compare(Bitmap b1, Bitmap b2) {
                if (b1.getWidth() == b2.getWidth()) {
                    return b2.getHeight() - b1.getHeight();
                }
                return b2.getWidth() - b1.getWidth();
            }
        });
        new Thread(new Renderer(sortedBitmaps, totalPixelCount)).start();
    }

    private static String queryVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 268435456).versionName;
        } catch (NameNotFoundException e) {
            Log.w(LOG_TAG, "Could not get package info", e);
            return null;
        }
    }

    public void systemRunning() {
    }

    public boolean isCompatible(int ppid) {
        return ppid == Process.myPpid();
    }

    public GraphicBuffer getBuffer() throws RemoteException {
        return this.mAtlasReady.get() ? this.mBuffer : null;
    }

    public long[] getMap() throws RemoteException {
        return this.mAtlasReady.get() ? this.mAtlasMap : null;
    }

    private static Configuration computeBestConfiguration(ArrayList<Bitmap> bitmaps, int pixelCount) {
        Log.d(LOG_TAG, "Computing best atlas configuration...");
        long begin = System.nanoTime();
        List<WorkerResult> results = Collections.synchronizedList(new ArrayList());
        int cpuCount = Runtime.getRuntime().availableProcessors();
        if (cpuCount == 1) {
            new ComputeWorker(512, 2048, 64, bitmaps, pixelCount, results, null).run();
        } else {
            int start = ((cpuCount - 1) * 64) + 512;
            int end = 2048;
            int step = cpuCount * 64;
            CountDownLatch signal = new CountDownLatch(cpuCount);
            int i = 0;
            while (i < cpuCount) {
                new Thread(new ComputeWorker(start, end, step, bitmaps, pixelCount, results, signal), "Atlas Worker #" + (i + 1)).start();
                i++;
                start -= 64;
                end -= 64;
            }
            try {
                if (!signal.await(15, TimeUnit.SECONDS)) {
                    Log.w(LOG_TAG, "Could not complete configuration computation before timeout.");
                    return null;
                }
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "Could not complete configuration computation");
                return null;
            }
        }
        synchronized (results) {
            Collections.sort(results, new Comparator<WorkerResult>() {
                public int compare(WorkerResult r1, WorkerResult r2) {
                    int delta = r2.count - r1.count;
                    if (delta != 0) {
                        return delta;
                    }
                    return (r1.width * r1.height) - (r2.width * r2.height);
                }
            });
        }
        float delay = ((((float) (System.nanoTime() - begin)) / 1000.0f) / 1000.0f) / 1000.0f;
        Log.d(LOG_TAG, String.format("Found best atlas configuration (out of %d) in %.2fs", new Object[]{Integer.valueOf(results.size()), Float.valueOf(delay)}));
        WorkerResult result = (WorkerResult) results.get(0);
        return new Configuration(result.type, result.width, result.height, result.count);
    }

    private static File getDataFile() {
        return new File(new File(Environment.getDataDirectory(), "system"), "framework_atlas.config");
    }

    private static void deleteDataFile() {
        Log.w(LOG_TAG, "Current configuration inconsistent with assets list");
        if (!getDataFile().delete()) {
            Log.w(LOG_TAG, "Could not delete the current configuration");
        }
    }

    private File getFrameworkResourcesFile() {
        return new File(this.mContext.getApplicationInfo().sourceDir);
    }

    private Configuration chooseConfiguration(ArrayList<Bitmap> bitmaps, int pixelCount, String versionName) {
        Configuration config = null;
        File dataFile = getDataFile();
        if (dataFile.exists()) {
            config = readConfiguration(dataFile, versionName);
        }
        if (config == null) {
            config = computeBestConfiguration(bitmaps, pixelCount);
            if (config != null) {
                writeConfiguration(config, dataFile, versionName);
            }
        }
        return config;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x009f A:{SYNTHETIC, Splitter: B:21:0x009f} */
    /* JADX WARNING: Removed duplicated region for block: B:37:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x007c A:{SYNTHETIC, Splitter: B:14:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a8 A:{SYNTHETIC, Splitter: B:26:0x00a8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeConfiguration(Configuration config, File file, String versionName) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedWriter writer = null;
        try {
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            try {
                writer2.write(getBuildIdentifier(versionName));
                writer2.newLine();
                writer2.write(config.type.toString());
                writer2.newLine();
                writer2.write(String.valueOf(config.width));
                writer2.newLine();
                writer2.write(String.valueOf(config.height));
                writer2.newLine();
                writer2.write(String.valueOf(config.count));
                writer2.newLine();
                writer2.write(String.valueOf(config.flags));
                writer2.newLine();
                if (writer2 != null) {
                    try {
                        writer2.close();
                    } catch (IOException e3) {
                    }
                }
                writer = writer2;
            } catch (FileNotFoundException e4) {
                e = e4;
                writer = writer2;
                Log.w(LOG_TAG, "Could not write " + file, e);
                if (writer == null) {
                    try {
                        writer.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (IOException e6) {
                e2 = e6;
                writer = writer2;
                try {
                    Log.w(LOG_TAG, "Could not write " + file, e2);
                    if (writer == null) {
                        try {
                            writer.close();
                        } catch (IOException e7) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (writer != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                writer = writer2;
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e8) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            Log.w(LOG_TAG, "Could not write " + file, e);
            if (writer == null) {
            }
        } catch (IOException e10) {
            e2 = e10;
            Log.w(LOG_TAG, "Could not write " + file, e2);
            if (writer == null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c7 A:{SYNTHETIC, Splitter: B:36:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00be A:{SYNTHETIC, Splitter: B:31:0x00be} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0099 A:{SYNTHETIC, Splitter: B:24:0x0099} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0073 A:{SYNTHETIC, Splitter: B:16:0x0073} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Configuration readConfiguration(File file, String versionName) {
        IllegalArgumentException e;
        FileNotFoundException e2;
        IOException e3;
        Throwable th;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            try {
                Configuration config;
                if (checkBuildIdentifier(reader2, versionName)) {
                    config = new Configuration(Type.valueOf(reader2.readLine()), readInt(reader2, 512, 2048), readInt(reader2, 512, 2048), readInt(reader2, 0, Integer.MAX_VALUE), readInt(reader2, Integer.MIN_VALUE, Integer.MAX_VALUE));
                } else {
                    config = null;
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e4) {
                    }
                }
                reader = reader2;
                return config;
            } catch (IllegalArgumentException e5) {
                e = e5;
                reader = reader2;
                Log.w(LOG_TAG, "Invalid parameter value in " + file, e);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e6) {
                    }
                }
                return null;
            } catch (FileNotFoundException e7) {
                e2 = e7;
                reader = reader2;
                Log.w(LOG_TAG, "Could not read " + file, e2);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e8) {
                    }
                }
                return null;
            } catch (IOException e9) {
                e3 = e9;
                reader = reader2;
                try {
                    Log.w(LOG_TAG, "Could not read " + file, e3);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e10) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e11) {
                    }
                }
                throw th;
            }
        } catch (IllegalArgumentException e12) {
            e = e12;
            Log.w(LOG_TAG, "Invalid parameter value in " + file, e);
            if (reader != null) {
            }
            return null;
        } catch (FileNotFoundException e13) {
            e2 = e13;
            Log.w(LOG_TAG, "Could not read " + file, e2);
            if (reader != null) {
            }
            return null;
        } catch (IOException e14) {
            e3 = e14;
            Log.w(LOG_TAG, "Could not read " + file, e3);
            if (reader != null) {
            }
            return null;
        }
    }

    private static int readInt(BufferedReader reader, int min, int max) throws IOException {
        return Math.max(min, Math.min(max, Integer.parseInt(reader.readLine())));
    }

    private boolean checkBuildIdentifier(BufferedReader reader, String versionName) throws IOException {
        return getBuildIdentifier(versionName).equals(reader.readLine());
    }

    private String getBuildIdentifier(String versionName) {
        return SystemProperties.get("ro.build.fingerprint", IElsaManager.EMPTY_PACKAGE) + '/' + versionName + '/' + String.valueOf(getFrameworkResourcesFile().length());
    }
}
