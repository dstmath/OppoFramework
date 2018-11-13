package android.media;

import android.app.backup.FullBackup;
import android.app.job.JobInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Hashtable;

public class MiniThumbFile {
    public static final int BYTES_PER_INDEX = 9;
    public static final int BYTES_PER_MINTHUMB = 10000;
    private static final int HEADER_SIZE = 13;
    private static final int INDEX_FILE_VERSION = 5;
    private static final int MINI_THUMB_DATA_FILE_VERSION = 5;
    private static final String TAG = "MiniThumbFile";
    private static final Hashtable<String, MiniThumbFile> sThumbFiles = new Hashtable();
    private ByteBuffer mBuffer = ByteBuffer.allocateDirect(10000);
    private FileChannel mChannel;
    private ByteBuffer mEmptyBuffer = ByteBuffer.allocateDirect(10000);
    private ByteBuffer mIndexBuffer = ByteBuffer.allocateDirect(9);
    private FileChannel mIndexChannel;
    private RandomAccessFile mIndexFile;
    private RandomAccessFile mMiniThumbFile;
    private Uri mUri;

    public static synchronized void reset() {
        synchronized (MiniThumbFile.class) {
            for (MiniThumbFile file : sThumbFiles.values()) {
                file.deactivate();
            }
            sThumbFiles.clear();
        }
    }

    public static synchronized MiniThumbFile instance(Uri uri) {
        MiniThumbFile file;
        synchronized (MiniThumbFile.class) {
            String type = (String) uri.getPathSegments().get(1);
            file = (MiniThumbFile) sThumbFiles.get(type);
            if (file == null) {
                file = new MiniThumbFile(Uri.parse("content://media/external/" + type + "/media"));
                sThumbFiles.put(type, file);
            }
        }
        return file;
    }

    private String randomAccessFilePath(int version) {
        return (Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails") + "/.thumbdata" + version + "-" + this.mUri.hashCode();
    }

    private void removeOldFile() {
        File oldFile = new File(randomAccessFilePath(4));
        if (oldFile.exists()) {
            try {
                oldFile.delete();
            } catch (SecurityException e) {
            }
        }
    }

    private RandomAccessFile miniThumbDataFile() {
        if (this.mMiniThumbFile == null) {
            removeOldFile();
            String path = randomAccessFilePath(5);
            File directory = new File(path).getParentFile();
            if (!(directory.isDirectory() || directory.mkdirs())) {
                Log.e(TAG, "Unable to create .thumbnails directory " + directory.toString());
            }
            File f = new File(path);
            try {
                this.mMiniThumbFile = new RandomAccessFile(f, "rw");
            } catch (IOException e) {
                try {
                    this.mMiniThumbFile = new RandomAccessFile(f, FullBackup.ROOT_TREE_TOKEN);
                } catch (IOException e2) {
                }
            }
            if (this.mMiniThumbFile != null) {
                this.mChannel = this.mMiniThumbFile.getChannel();
            }
        }
        return this.mMiniThumbFile;
    }

    private String indexFilePath(int version) {
        return (Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails") + "/.thumbindex" + version + "-" + this.mUri.hashCode();
    }

    private void removeOldIndexFile() {
        File oldFile = new File(indexFilePath(4));
        if (oldFile.exists()) {
            try {
                oldFile.delete();
            } catch (SecurityException e) {
            }
        }
    }

    private void removeInvalidIndexFile() {
        File oldFile = new File(indexFilePath(5));
        if (oldFile.exists()) {
            try {
                Log.d(TAG, "old index file is deleted");
                oldFile.delete();
            } catch (SecurityException e) {
            }
            if (this.mIndexFile != null) {
                try {
                    this.mIndexFile.close();
                    this.mIndexFile = null;
                } catch (IOException e2) {
                }
            }
        }
    }

    private RandomAccessFile indexFile() {
        if (this.mIndexFile == null) {
            removeOldIndexFile();
            String path = indexFilePath(5);
            File directory = new File(path).getParentFile();
            if (!(directory.isDirectory() || directory.mkdirs())) {
                Log.e(TAG, "Unable to create .thumbnails directory " + directory.toString());
            }
            File f = new File(path);
            try {
                this.mIndexFile = new RandomAccessFile(f, "rw");
            } catch (IOException e) {
                try {
                    this.mIndexFile = new RandomAccessFile(f, FullBackup.ROOT_TREE_TOKEN);
                } catch (IOException e2) {
                }
            }
            if (this.mIndexFile != null) {
                this.mIndexChannel = this.mIndexFile.getChannel();
            }
        }
        return this.mIndexFile;
    }

    private MiniThumbFile(Uri uri) {
        this.mUri = uri;
    }

    public synchronized void deactivate() {
        if (this.mMiniThumbFile != null) {
            try {
                this.mMiniThumbFile.close();
                this.mMiniThumbFile = null;
            } catch (IOException e) {
            }
        }
        if (this.mIndexFile != null) {
            try {
                this.mIndexFile.close();
                this.mIndexFile = null;
            } catch (IOException e2) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x005c A:{SYNTHETIC, Splitter: B:13:0x005c} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00bf  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00bf  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized long getMagic(long id) {
        long pos;
        long j;
        RandomAccessFile r = miniThumbDataFile();
        RandomAccessFile ri = indexFile();
        if (!(r == null || ri == null)) {
            FileLock fileLock = null;
            long index = 9 * id;
            try {
                this.mIndexBuffer.clear();
                this.mIndexBuffer.limit(9);
                fileLock = this.mIndexChannel.lock(index, 9, true);
                if (this.mIndexChannel.read(this.mIndexBuffer, index) == 9) {
                    this.mIndexBuffer.position(0);
                    if (this.mIndexBuffer.get() == (byte) 1) {
                        pos = this.mIndexBuffer.getLong();
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException e) {
                            }
                        }
                        if (pos < 0) {
                            return 0;
                        }
                        fileLock = null;
                        try {
                            this.mBuffer.clear();
                            this.mBuffer.limit(9);
                            fileLock = this.mChannel.lock(pos, 9, true);
                            if (this.mChannel.read(this.mBuffer, pos) == 9) {
                                this.mBuffer.position(0);
                                if (this.mBuffer.get() == (byte) 1) {
                                    j = this.mBuffer.getLong();
                                    if (fileLock != null) {
                                        try {
                                            fileLock.release();
                                        } catch (IOException e2) {
                                        }
                                    }
                                }
                            }
                            if (fileLock != null) {
                                try {
                                    fileLock.release();
                                } catch (IOException e3) {
                                }
                            }
                        } catch (IOException ex) {
                            Log.v(TAG, "Got exception checking file magic: ", ex);
                            if (fileLock != null) {
                                try {
                                    fileLock.release();
                                } catch (IOException e4) {
                                }
                            }
                        } catch (RuntimeException ex2) {
                            Log.e(TAG, "Got exception when reading magic, id = " + id + ", disk full or mount read-only? " + ex2.getClass());
                            if (fileLock != null) {
                                try {
                                    fileLock.release();
                                } catch (IOException e5) {
                                }
                            }
                        } catch (Throwable th) {
                            if (fileLock != null) {
                                try {
                                    fileLock.release();
                                } catch (IOException e6) {
                                }
                            }
                        }
                    }
                }
                pos = -1;
                if (fileLock != null) {
                }
            } catch (IOException ex3) {
                Log.v(TAG, "Got exception checking file position: ", ex3);
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e7) {
                    }
                }
            } catch (RuntimeException ex22) {
                Log.e(TAG, "Got exception when reading position, id = " + id + ", disk full or mount read-only? " + ex22.getClass());
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e8) {
                    }
                }
            } catch (Throwable th2) {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e9) {
                    }
                }
            }
            if (pos < 0) {
            }
        }
        return 0;
        return j;
        pos = -1;
        if (pos < 0) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x005d A:{SYNTHETIC, Splitter: B:13:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0066 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0066 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00dc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void eraseMiniThumb(long id) {
        long pos;
        RandomAccessFile r = miniThumbDataFile();
        RandomAccessFile ri = indexFile();
        if (!(r == null || ri == null)) {
            FileLock fileLock = null;
            long index = 9 * id;
            try {
                this.mIndexBuffer.clear();
                this.mIndexBuffer.limit(9);
                fileLock = this.mIndexChannel.lock(index, 9, true);
                if (this.mIndexChannel.read(this.mIndexBuffer, index) == 9) {
                    this.mIndexBuffer.position(0);
                    if (this.mIndexBuffer.get() == (byte) 1) {
                        pos = this.mIndexBuffer.getLong();
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException e) {
                            }
                        }
                        if (pos >= 0) {
                            fileLock = null;
                            try {
                                this.mBuffer.clear();
                                this.mBuffer.limit(9);
                                fileLock = this.mChannel.lock(pos, JobInfo.MIN_BACKOFF_MILLIS, false);
                                if (this.mChannel.read(this.mBuffer, pos) == 9) {
                                    this.mBuffer.position(0);
                                    if (this.mBuffer.get() == (byte) 1) {
                                        if (this.mBuffer.getLong() == 0) {
                                            Log.i(TAG, "no thumbnail for id " + id);
                                            if (fileLock != null) {
                                                try {
                                                    fileLock.release();
                                                } catch (IOException e2) {
                                                }
                                            }
                                        } else {
                                            this.mChannel.write(this.mEmptyBuffer, pos);
                                        }
                                    }
                                }
                                if (fileLock != null) {
                                    try {
                                        fileLock.release();
                                    } catch (IOException e3) {
                                    }
                                }
                            } catch (IOException ex) {
                                Log.v(TAG, "Got exception checking file magic: ", ex);
                                if (fileLock != null) {
                                    try {
                                        fileLock.release();
                                    } catch (IOException e4) {
                                    }
                                }
                            } catch (RuntimeException ex2) {
                                Log.e(TAG, "Got exception when reading magic, id = " + id + ", disk full or mount read-only? " + ex2.getClass());
                                if (fileLock != null) {
                                    try {
                                        fileLock.release();
                                    } catch (IOException e5) {
                                    }
                                }
                            } catch (Throwable th) {
                                if (fileLock != null) {
                                    try {
                                        fileLock.release();
                                    } catch (IOException e6) {
                                    }
                                }
                            }
                        } else {
                            return;
                        }
                    }
                }
                pos = -1;
                if (fileLock != null) {
                }
            } catch (IOException ex3) {
                Log.w(TAG, "got exception when reading position id=" + id + ", exception: " + ex3);
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e7) {
                    }
                }
            } catch (RuntimeException ex22) {
                Log.e(TAG, "Got exception when reading position, id = " + id + ", disk full or mount read-only? " + ex22.getClass());
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e8) {
                    }
                }
            } catch (Throwable th2) {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e9) {
                    }
                }
            }
            if (pos >= 0) {
            }
        }
        return;
        pos = -1;
        if (pos >= 0) {
        }
    }

    public synchronized void saveMiniThumbToFile(byte[] data, long id, long magic) throws IOException {
        if (miniThumbDataFile() != null) {
            long length = this.mMiniThumbFile.length();
            if (length == 0) {
                Log.d(TAG, "saveMiniThumbToFile MiniThumbFile length: " + length + " remove invalid index file");
                removeInvalidIndexFile();
            }
            if (indexFile() != null) {
                FileLock fileLock = null;
                long index = 9 * id;
                long pos = this.mMiniThumbFile.length();
                boolean writeIndexSuccess = false;
                try {
                    this.mIndexBuffer.clear();
                    this.mIndexBuffer.put((byte) 1);
                    this.mIndexBuffer.putLong(pos);
                    this.mIndexBuffer.flip();
                    fileLock = this.mIndexChannel.lock(index, 9, false);
                    this.mIndexChannel.write(this.mIndexBuffer, index);
                    writeIndexSuccess = true;
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e) {
                        }
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "couldn't save mini thumbnail position for " + id + "; ", ex);
                    throw ex;
                } catch (RuntimeException ex2) {
                    Log.e(TAG, "couldn't save mini thumbnail position for " + id + "; disk full or mount read-only? " + ex2.getClass());
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e2) {
                        }
                    }
                } catch (Throwable th) {
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e3) {
                        }
                    }
                }
                if (writeIndexSuccess) {
                    fileLock = null;
                    if (data != null) {
                        try {
                            if (data.length <= 9987) {
                                this.mBuffer.clear();
                                this.mBuffer.put((byte) 1);
                                this.mBuffer.putLong(magic);
                                this.mBuffer.putInt(data.length);
                                this.mBuffer.put(data);
                                this.mBuffer.flip();
                                fileLock = this.mChannel.lock(pos, JobInfo.MIN_BACKOFF_MILLIS, false);
                                this.mChannel.write(this.mBuffer, pos);
                            } else {
                                return;
                            }
                        } catch (IOException ex3) {
                            Log.e(TAG, "couldn't save mini thumbnail data for " + id + "; ", ex3);
                            throw ex3;
                        } catch (RuntimeException ex22) {
                            Log.e(TAG, "couldn't save mini thumbnail data for " + id + "; disk full or mount read-only? " + ex22.getClass());
                            if (fileLock != null) {
                                try {
                                    fileLock.release();
                                } catch (IOException e4) {
                                }
                            }
                        } catch (Throwable th2) {
                            if (fileLock != null) {
                                try {
                                    fileLock.release();
                                } catch (IOException e5) {
                                }
                            }
                        }
                    }
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e6) {
                        }
                    }
                } else {
                    return;
                }
            }
            return;
        }
        return;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0060 A:{SYNTHETIC, Splitter: B:16:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00e0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized byte[] getMiniThumbFromFile(long id, byte[] data) {
        long pos;
        RandomAccessFile r = miniThumbDataFile();
        RandomAccessFile ri = indexFile();
        if (r == null || ri == null) {
            return null;
        }
        FileLock fileLock = null;
        long index = 9 * id;
        try {
            this.mIndexBuffer.clear();
            this.mIndexBuffer.limit(9);
            fileLock = this.mIndexChannel.lock(index, 9, true);
            if (this.mIndexChannel.read(this.mIndexBuffer, index) == 9) {
                this.mIndexBuffer.position(0);
                if (this.mIndexBuffer.get() == (byte) 1) {
                    pos = this.mIndexBuffer.getLong();
                    if (fileLock != null) {
                        try {
                            fileLock.release();
                        } catch (IOException e) {
                        }
                    }
                    if (pos < 0) {
                        return null;
                    }
                    fileLock = null;
                    try {
                        this.mBuffer.clear();
                        fileLock = this.mChannel.lock(pos, JobInfo.MIN_BACKOFF_MILLIS, true);
                        int size = this.mChannel.read(this.mBuffer, pos);
                        if (size > 13) {
                            this.mBuffer.position(0);
                            byte flag = this.mBuffer.get();
                            long magic = this.mBuffer.getLong();
                            int length = this.mBuffer.getInt();
                            if (size >= length + 13 && length != 0 && magic != 0 && flag == (byte) 1 && data.length >= length) {
                                this.mBuffer.get(data, 0, length);
                                if (fileLock != null) {
                                    try {
                                        fileLock.release();
                                    } catch (IOException e2) {
                                    }
                                }
                            }
                        }
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException e3) {
                            }
                        }
                    } catch (IOException ex) {
                        Log.w(TAG, "got exception when reading thumbnail id=" + id + ", exception: " + ex);
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException e4) {
                            }
                        }
                    } catch (RuntimeException ex2) {
                        Log.e(TAG, "Got exception when reading thumbnail, id = " + id + ", disk full or mount read-only? " + ex2.getClass());
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException e5) {
                            }
                        }
                    } catch (Throwable th) {
                        if (fileLock != null) {
                            try {
                                fileLock.release();
                            } catch (IOException e6) {
                            }
                        }
                    }
                    return null;
                }
            }
            pos = -1;
            if (fileLock != null) {
            }
        } catch (IOException ex3) {
            Log.w(TAG, "got exception when reading position id=" + id + ", exception: " + ex3);
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e7) {
                }
            }
        } catch (RuntimeException ex22) {
            Log.e(TAG, "Got exception when reading position, id = " + id + ", disk full or mount read-only? " + ex22.getClass());
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e8) {
                }
            }
        } catch (Throwable th2) {
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e9) {
                }
            }
        }
        if (pos < 0) {
        }
        return data;
        pos = -1;
        if (pos < 0) {
        }
    }
}
