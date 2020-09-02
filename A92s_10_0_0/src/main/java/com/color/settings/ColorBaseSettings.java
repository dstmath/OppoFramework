package com.color.settings;

import android.app.ActivityThread;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.SystemProperties;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class ColorBaseSettings {
    private static final String BASE_URI = "content://ColorSettings";
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "ColorBaseSettings";

    public static InputStream readConfigAsUser(Context context, String customPath, int userId, int type) throws IOException {
        if (isSystemProcess()) {
            String path = ColorSettingsConfig.getFilePath(type, userId, customPath);
            if (DEBUG) {
                Log.d(TAG, "readConfigAsUser systemUser path=" + path + " type=" + type + " userId=" + userId + " customPath=" + customPath);
            }
            return new FileInputStream(path);
        }
        return context.getContentResolver().openInputStream(ColorSettingsConfig.getUri(BASE_URI, customPath, userId, type));
    }

    public static OutputStream writeConfigAsUser(Context context, String customPath, int userId, int type) throws IOException {
        Uri uri = ColorSettingsConfig.getUri(BASE_URI, customPath, userId, type);
        if (DEBUG) {
            Log.d(TAG, "writeConfigAsUser customPath=" + customPath + " userId=" + userId + " type=" + type + " uri=" + uri.toString());
        }
        if (isSystemProcess()) {
            String path = ColorSettingsConfig.getFilePath(type, userId, customPath);
            File file = new File(path);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            return new ColorFileOutputStream(path, context.getContentResolver(), uri);
        }
        AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(uri, "w", null);
        if (fd == null) {
            return null;
        }
        try {
            if (fd.getDeclaredLength() < 0) {
                return new ParcelFileAutoCloseOutputStream(fd.getParcelFileDescriptor(), context.getContentResolver(), uri);
            }
            return new AssertFileAutoCloseOutputStream(fd, context.getContentResolver(), uri);
        } catch (IOException e) {
            throw new FileNotFoundException("Unable to create stream");
        }
    }

    public static String readConfigStringAsUser(Context context, String customPath, int userId, int type) throws IOException {
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = readConfigAsUser(context, customPath, userId, type);
            if (is == null) {
                Log.e(TAG, "readConfig error is is null");
                return null;
            }
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String lineTxt = br2.readLine();
                if (lineTxt == null) {
                    break;
                }
                sb.append(lineTxt);
            }
            String sb2 = sb.toString();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "readConfig close is error", e);
                }
            }
            try {
                br2.close();
            } catch (IOException e2) {
                Log.e(TAG, "readConfig close br error", e2);
            }
            return sb2;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                    Log.e(TAG, "readConfig close is error", e3);
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e4) {
                    Log.e(TAG, "readConfig close br error", e4);
                }
            }
        }
    }

    public static int writeConfigStringAsUser(Context context, String customPath, int userId, int type, String str) throws IOException {
        OutputStream os = null;
        BufferedWriter bw = null;
        try {
            os = writeConfigAsUser(context, customPath, userId, type);
            if (os == null) {
                Log.e(TAG, "writeConfig error os is null");
                return -2;
            }
            BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(os));
            bw2.write(str);
            bw2.flush();
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "writeConfig close os error", e);
                }
            }
            try {
                bw2.close();
            } catch (IOException e2) {
                Log.e(TAG, "writeConfig close bw error", e2);
            }
            return 0;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e3) {
                    Log.e(TAG, "writeConfig close os error", e3);
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e4) {
                    Log.e(TAG, "writeConfig close bw error", e4);
                }
            }
        }
    }

    public static void registerChangeListenerAsUser(Context context, String customPath, int userId, int type, ColorSettingsChangeListener listener) {
        context.getContentResolver().registerContentObserver(ColorSettingsConfig.getUri(BASE_URI, customPath, userId, type), true, listener);
    }

    public static void registerChangeListenerForAll(Context context, String customPath, int type, ColorSettingsChangeListener listener) {
        context.getContentResolver().registerContentObserver(ColorSettingsConfig.getUri(BASE_URI, customPath, -2, type), true, listener, -1);
    }

    public static boolean isSystemProcess() {
        return ActivityThread.isSystem();
    }

    private static class ColorFileOutputStream extends FileOutputStream {
        private int callCount = 1;
        private final ContentResolver contentResolver;
        private final Uri uri;

        public ColorFileOutputStream(String s, ContentResolver cr, Uri uri2) throws FileNotFoundException {
            super(s);
            this.contentResolver = cr;
            this.uri = uri2;
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.io.FileOutputStream, java.lang.AutoCloseable
        public void close() throws IOException {
            int i;
            super.close();
            ContentResolver contentResolver2 = this.contentResolver;
            if (contentResolver2 != null && (i = this.callCount) >= 1) {
                this.callCount = i - 1;
                contentResolver2.update(this.uri, new ContentValues(), null, null);
            }
        }
    }

    private static class ParcelFileAutoCloseOutputStream extends ParcelFileDescriptor.AutoCloseOutputStream {
        private int callCount = 1;
        private final ContentResolver contentResolver;
        private final Uri uri;

        ParcelFileAutoCloseOutputStream(ParcelFileDescriptor parcelFileDescriptor, ContentResolver cr, Uri uri2) {
            super(parcelFileDescriptor);
            this.contentResolver = cr;
            this.uri = uri2;
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.io.FileOutputStream, android.os.ParcelFileDescriptor.AutoCloseOutputStream, java.lang.AutoCloseable
        public void close() throws IOException {
            int i;
            super.close();
            ContentResolver contentResolver2 = this.contentResolver;
            if (contentResolver2 != null && (i = this.callCount) >= 1) {
                this.callCount = i - 1;
                contentResolver2.update(this.uri, new ContentValues(), null, null);
            }
        }
    }

    private static class AssertFileAutoCloseOutputStream extends AssetFileDescriptor.AutoCloseOutputStream {
        private int callCount = 1;
        private final ContentResolver contentResolver;
        private final Uri uri;

        AssertFileAutoCloseOutputStream(AssetFileDescriptor assetFileDescriptor, ContentResolver cr, Uri uri2) throws IOException {
            super(assetFileDescriptor);
            this.contentResolver = cr;
            this.uri = uri2;
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.io.FileOutputStream, android.os.ParcelFileDescriptor.AutoCloseOutputStream, java.lang.AutoCloseable
        public void close() throws IOException {
            int i;
            super.close();
            ContentResolver contentResolver2 = this.contentResolver;
            if (contentResolver2 != null && (i = this.callCount) >= 1) {
                this.callCount = i - 1;
                contentResolver2.update(this.uri, new ContentValues(), null, null);
            }
        }
    }
}
