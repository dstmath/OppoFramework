package com.mediatek.mtklogger.c2klogger;

import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;

public class C2KLogRecycle implements Runnable {
    private static C2KLogRecycle sInstance = new C2KLogRecycle();
    private long mC2KLogMaxSize = 0;
    private String mC2KLogPath = "";
    private C2KLogRecycleConfig mC2KLogRecycleConfig = null;
    private boolean mIsRecycleDoing = false;
    private boolean mIsStop = false;
    private SharedPreferences mSharedPreferences;
    private Thread mThread;

    public static C2KLogRecycle getInstance() {
        return sInstance;
    }

    private C2KLogRecycle() {
    }

    public void startRecycle(String c2KLogPath, long c2KLogMaxSize, SharedPreferences sharedPreferences) {
        Log.d(C2KLogUtils.TAG_APP, "-->startRecycle()");
        this.mIsStop = false;
        this.mC2KLogPath = c2KLogPath;
        this.mC2KLogMaxSize = c2KLogMaxSize;
        this.mSharedPreferences = sharedPreferences;
        this.mC2KLogRecycleConfig = new C2KLogRecycleConfig(c2KLogPath + "/" + C2KLogUtils.C2KLOG_RECYCLE_CONFIG_FILE);
        if (!this.mIsRecycleDoing) {
            this.mIsRecycleDoing = true;
            this.mThread = new Thread(sInstance);
            this.mThread.start();
        }
    }

    public void stopRecycle() {
        this.mIsStop = true;
        Thread thread = this.mThread;
        if (thread != null) {
            thread.interrupt();
            this.mThread = null;
        }
        this.mC2KLogRecycleConfig.writeContents();
    }

    public void addLogPathToRecycleConfig(String logPath) {
        this.mC2KLogRecycleConfig.addLogpathToLastLine(logPath);
    }

    public void run() {
        Log.d(C2KLogUtils.TAG_APP, "startRecycle->run() mIsRecycleDoing ? " + this.mIsRecycleDoing);
        while (!this.mIsStop) {
            while (isNeedRecycle()) {
                deleteC2KLogFile(this.mC2KLogRecycleConfig.getLogpathFromFirstLine());
                this.mC2KLogRecycleConfig.removeLogpathFromFirstLine();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
        this.mIsRecycleDoing = false;
    }

    private boolean isNeedRecycle() {
        try {
            deleteEmptyC2KLogFolder(new File(this.mSharedPreferences.getString(C2KLogUtils.KEY_C2K_MODEM_LOGGING_PATH, "")).getAbsolutePath());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return getFileSize(this.mC2KLogPath) >= (this.mC2KLogMaxSize * 1024) * 1024;
    }

    private static long getFileSize(String filePath) {
        long size = 0;
        if (filePath == null) {
            return 0;
        }
        File fileRoot = new File(filePath);
        if (!fileRoot.exists()) {
            return 0;
        }
        if (!fileRoot.isDirectory()) {
            return fileRoot.length();
        }
        File[] files = fileRoot.listFiles();
        if (files == null || files.length == 0) {
            Log.v(C2KLogUtils.TAG_APP, "Loop folder [" + filePath + "] get a null/empty list");
            return 0;
        }
        for (File file : files) {
            if (file != null) {
                size += getFileSize(file.getAbsolutePath());
            }
        }
        return size;
    }

    private void deleteEmptyC2KLogFolder(String filterFolder) {
        File[] subFiles;
        File[] files = new File(this.mC2KLogPath).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && !file.getAbsolutePath().equalsIgnoreCase(filterFolder) && ((subFiles = file.listFiles()) == null || subFiles.length == 0)) {
                    Log.w(C2KLogUtils.TAG_APP, "C2KLog folder is empty, delete it :" + file.getAbsolutePath());
                    file.delete();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void deleteC2KLogFile(String logFile) {
        File file = new File(logFile);
        if (file.exists()) {
            Log.w(C2KLogUtils.TAG_APP, "File is auto recycled :" + file.getPath());
            file.delete();
        }
    }
}
