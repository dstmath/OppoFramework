package com.android.server.pm;

import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Slog;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import libcore.io.Streams;

public class FileHelper {
    private static final String TAG = "FileHelper";

    public static void createFileIfNeeded(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Slog.d(TAG, "create " + path);
            file.mkdirs();
            if (!file.exists()) {
                Slog.e(TAG, "create " + path + " failed.");
            }
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException}
     arg types: [java.io.File, int]
     candidates:
      ClspMth{java.io.FileOutputStream.<init>(java.lang.String, boolean):void throws java.io.FileNotFoundException}
      ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException} */
    public static boolean copyPackageToDir(String srcPath, String dstDirPath) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(dstDirPath)) {
            Slog.e(TAG, srcPath == null ? "srcPath is empty." : "dstDirPath is empty.");
            return false;
        }
        Slog.d(TAG, "copyPackageToDir, copy " + srcPath + " to " + dstDirPath + " directory.");
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            Slog.e(TAG, "copyPackageToDir::" + srcPath + " not exists.");
            return false;
        }
        File dstDir = new File(dstDirPath);
        if (!dstDir.exists()) {
            try {
                Os.mkdir(dstDir.getAbsolutePath(), 505);
                Os.chmod(dstDir.getPath(), 505);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
            if (!dstDir.exists()) {
                Slog.e(TAG, "copyPackageToDir, create " + dstDir.getPath() + " failed.");
                return false;
            }
            Slog.d(TAG, "copyPackageToDir, create " + dstDir.getPath() + " success.");
        }
        File dstFile = new File(dstDir.getPath() + File.separatorChar + srcFile.getName());
        InputStream fileIn = null;
        OutputStream fileOut = null;
        try {
            fileIn = new FileInputStream(srcFile);
            fileOut = new FileOutputStream(dstFile, false);
            Streams.copy(fileIn, fileOut);
            Os.chmod(dstFile.getAbsolutePath(), 420);
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (ErrnoException e3) {
            e3.printStackTrace();
        } catch (Throwable th) {
            close(fileIn);
            close(fileOut);
            throw th;
        }
        close(fileIn);
        close(fileOut);
        boolean success = dstFile.exists();
        if (!success) {
            Slog.e(TAG, "copyPackageToDir from " + srcFile + " to " + dstFile.getPath() + " failed.");
        } else {
            Slog.d(TAG, "copyPackageToDir, copy " + srcFile.getPath() + " to " + dstFile.getPath() + " success.");
        }
        return success;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException}
     arg types: [java.io.File, int]
     candidates:
      ClspMth{java.io.FileOutputStream.<init>(java.lang.String, boolean):void throws java.io.FileNotFoundException}
      ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException} */
    public static boolean copyPackageToFile(String srcPath, String dstPath) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(dstPath)) {
            Slog.e(TAG, srcPath == null ? "srcPath is empty." : "dstPath is empty.");
            return false;
        }
        Slog.d(TAG, "copyPackageToFile, copy " + srcPath + " to " + dstPath);
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            Slog.e(TAG, "copyPackageToFile::" + srcPath + " not exists.");
            return false;
        }
        File dstFile = new File(dstPath);
        File dstParentDir = dstFile.getParentFile();
        if (!dstParentDir.exists()) {
            try {
                Os.mkdir(dstParentDir.getAbsolutePath(), 505);
                Os.chmod(dstParentDir.getPath(), 505);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
            if (!dstParentDir.exists()) {
                Slog.e(TAG, "copyPackageToFile, create " + dstParentDir.getPath() + " failed.");
                return false;
            }
            Slog.d(TAG, "copyPackageToFile, " + dstParentDir.getPath() + " success.");
        }
        InputStream fileIn = null;
        OutputStream fileOut = null;
        try {
            fileIn = new FileInputStream(srcFile);
            fileOut = new FileOutputStream(dstFile, false);
            Streams.copy(fileIn, fileOut);
            Os.chmod(dstFile.getAbsolutePath(), 420);
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (ErrnoException e3) {
            e3.printStackTrace();
        } catch (Throwable th) {
            close(fileIn);
            close(fileOut);
            throw th;
        }
        close(fileIn);
        close(fileOut);
        boolean success = dstFile.exists();
        if (!success) {
            Slog.e(TAG, "copyPackageToFile from " + srcFile + " to " + dstFile.getPath() + " failed.");
        } else {
            Slog.d(TAG, "copyPackageToFile from " + srcFile + " to " + dstFile.getPath() + " success.");
        }
        return success;
    }

    public static void copyFileRecursive(String src, String dst) {
        copyFileRecursive(src, dst, 493, 420);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException}
     arg types: [java.io.File, int]
     candidates:
      ClspMth{java.io.FileOutputStream.<init>(java.lang.String, boolean):void throws java.io.FileNotFoundException}
      ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException} */
    public static void copyFileRecursive(String src, String dst, int dirMode, int filemode) {
        if (src == null || src.isEmpty() || dst == null || dst.isEmpty()) {
            Slog.e(TAG, "invalid path:src[" + src + "], dst[" + dst + "]");
            return;
        }
        Slog.d(TAG, "copy " + src + " to " + dst);
        File srcFile = new File(src);
        if (srcFile.exists()) {
            File dstFile = new File(dst);
            if (srcFile.isDirectory()) {
                if (!dstFile.exists()) {
                    dstFile.mkdirs();
                    try {
                        Os.chmod(dstFile.getAbsolutePath(), dirMode);
                    } catch (ErrnoException e) {
                        e.printStackTrace();
                    }
                }
                if (dstFile.exists()) {
                    String[] subFileList = srcFile.list();
                    if (subFileList != null) {
                        for (String sub : subFileList) {
                            copyFileRecursive(srcFile.getPath() + File.separator + sub, dstFile.getPath() + File.separator + sub);
                        }
                        return;
                    }
                    return;
                }
                Slog.e(TAG, "create " + dstFile.getPath() + " failed.");
            } else if (srcFile.isFile()) {
                if (!srcFile.canRead()) {
                    Slog.e(TAG, src + " can't read.");
                }
                if (!dstFile.canWrite()) {
                    Slog.e(TAG, dst + " can't write.");
                }
                InputStream fileIn = null;
                OutputStream fileOut = null;
                try {
                    fileIn = new FileInputStream(srcFile);
                    fileOut = new FileOutputStream(dstFile, false);
                    Streams.copy(fileIn, fileOut);
                    Os.chmod(dstFile.getAbsolutePath(), 420);
                    Slog.d(TAG, "copy " + src + " to " + dst + "success.");
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (ErrnoException e3) {
                    e3.printStackTrace();
                } catch (Throwable th) {
                    close(fileIn);
                    close(fileOut);
                    throw th;
                }
                close(fileIn);
                close(fileOut);
            }
        } else {
            Slog.e(TAG, srcFile.getPath() + " not exists.");
        }
    }

    private boolean hasValidFile(File file) {
        if (!file.exists()) {
            Slog.e(TAG, file.getPath() + " not exists");
            return false;
        } else if (!file.canRead()) {
            Slog.e(TAG, file.getPath() + " not readable");
            return false;
        } else {
            Slog.d(TAG, file.getPath() + " exist.");
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                if (subFiles != null && subFiles.length > 0) {
                    for (File sub : subFiles) {
                        if (hasValidFile(sub)) {
                            return true;
                        }
                    }
                }
            } else if (file.isFile()) {
                return true;
            }
            return false;
        }
    }

    private void deleteFileOrDir(File file) {
        if (file == null) {
            Slog.e(TAG, "file is null, ignored");
        } else if (!file.exists()) {
            Slog.e(TAG, file.getPath() + " not exists, ignored.");
        } else if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    deleteFileOrDir(subFile);
                }
            }
            file.delete();
            if (file.exists()) {
                Slog.e(TAG, "delete directory " + file.getPath() + " failed.");
            }
        } else if (file.isFile()) {
            file.delete();
            if (file.exists()) {
                Slog.e(TAG, "delete file " + file.getPath() + " failed.");
            }
        }
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
