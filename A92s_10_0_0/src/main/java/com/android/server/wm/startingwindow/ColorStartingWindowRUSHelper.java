package com.android.server.wm.startingwindow;

import android.os.FileObserver;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.Xml;
import com.google.android.collect.Sets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.xmlpull.v1.XmlPullParser;

public class ColorStartingWindowRUSHelper {
    public static final int FORCE_CLEAR_SPLASH_PACKAGE_START_FROM_LAUNCHER = 4;
    public static final int FORCE_CLEAR_SPLASH_TOKEN_FOR_SYSTEM_APP = 5;
    public static final int FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT = 9;
    private static final Object LOCK = new Object();
    public static final int SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION = 11;
    public static final int SPLASH_BLACK_PACKAGE_FOR_SYSTEM_APP = 2;
    public static final int SPLASH_BLACK_PACKAGE_START_FROM_LAUNCHER = 0;
    public static final int SPLASH_BLACK_TOKEN_FOR_SYSTEM_APP = 3;
    public static final int SPLASH_BLACK_TOKEN_START_FROM_LAUNCHER = 1;
    private static final String SPLASH_SNAPSHOT_SUPPORT_RANGE = "support-range";
    public static final int SPLASH_SNAPSHOT_WHITE_THIRD_PARTY_APP = 8;
    private static final String STARTING_WINDOW_CONFIG_DIR = "/data/oppo/coloros/animation";
    private static final String STARTING_WINDOW_CONFIG_FILE = "/data/oppo/coloros/animation/sys_color_animation_config.xml";
    public static final int STARTING_WINDOW_EXIT_LONG_DURATION_PACKAGE = 10;
    private static final String TAG = "ColorStartingWindowRUSHelper";
    public static final int TASK_SNAPSHOT_BLACK_PACKAGE_START_FROM_LAUNCHER = 6;
    public static final int TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER = 7;
    public static final String VERSION_NAME = "version";
    private static volatile ColorStartingWindowRUSHelper sInstance = null;
    private FileObserverPolicy mFileObserver = null;
    private SparseArray<Set<String>> mStartingWindowList = new SparseArray<>();
    private boolean mSupportSplashSnapshotForAllApps = false;
    private ExecutorService mThreadPool = null;
    private long mVersion = -1;

    private ColorStartingWindowRUSHelper() {
    }

    public static ColorStartingWindowRUSHelper getInstance() {
        if (sInstance == null) {
            synchronized (ColorStartingWindowRUSHelper.class) {
                if (sInstance == null) {
                    sInstance = new ColorStartingWindowRUSHelper();
                }
            }
        }
        return sInstance;
    }

    public void init() {
        initDir();
        initFileObserver();
        updateList();
    }

    private void checkThreadPoolNotNull() {
        if (this.mThreadPool == null) {
            this.mThreadPool = Executors.newSingleThreadExecutor();
        }
    }

    private void initDir() {
        File dir = new File(STARTING_WINDOW_CONFIG_DIR);
        File file = new File(STARTING_WINDOW_CONFIG_FILE);
        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        changeModFile(STARTING_WINDOW_CONFIG_FILE);
    }

    private void initFileObserver() {
        this.mFileObserver = new FileObserverPolicy(STARTING_WINDOW_CONFIG_FILE);
        this.mFileObserver.startWatching();
    }

    private void changeModFile(String fileName) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("chmod 766 " + fileName);
        } catch (IOException e) {
            ColorStartingWindowUtils.logE("Error in changeModFile : " + e);
        }
    }

    /* access modifiers changed from: private */
    public void updateList() {
        checkThreadPoolNotNull();
        synchronized (LOCK) {
            this.mThreadPool.execute(new Runnable() {
                /* class com.android.server.wm.startingwindow.$$Lambda$ColorStartingWindowRUSHelper$SPqCI5DMrW5608IgKSSVnsjpRgo */

                public final void run() {
                    ColorStartingWindowRUSHelper.this.updateListInBackground();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void updateListInBackground() {
        int type;
        ColorStartingWindowUtils.logD("updateListInBackground ******************** ");
        File file = new File(STARTING_WINDOW_CONFIG_FILE);
        if (file.exists()) {
            FileInputStream stream = null;
            try {
                this.mStartingWindowList.clear();
                FileInputStream stream2 = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if (TextUtils.isEmpty(tag)) {
                            continue;
                        } else if (VERSION_NAME.equals(tag)) {
                            long version = Long.parseLong(parser.nextText());
                            if (this.mVersion != -1 && this.mVersion >= version) {
                                try {
                                    stream2.close();
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        } else {
                            boolean z = false;
                            if (SPLASH_SNAPSHOT_SUPPORT_RANGE.equals(tag)) {
                                if (Integer.parseInt(parser.nextText()) == 1) {
                                    z = true;
                                }
                                this.mSupportSplashSnapshotForAllApps = z;
                                continue;
                            } else {
                                char[] charArr = tag.toCharArray();
                                if (charArr.length != 1) {
                                    continue;
                                } else {
                                    int listType = getTypeByChar(charArr[0]);
                                    Set<String> set = this.mStartingWindowList.get(listType);
                                    if (set == null) {
                                        Set<String> set2 = Sets.newHashSet();
                                        set2.add(parser.nextText());
                                        this.mStartingWindowList.put(listType, set2);
                                        continue;
                                    } else {
                                        set.add(parser.nextText());
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                } while (type != 1);
                try {
                    stream2.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            } catch (Exception e3) {
                e3.printStackTrace();
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    private int getTypeByChar(char c) {
        return c - 'a';
    }

    public static Set<String> getStartingWindowListByType(int type) {
        Set<String> set;
        synchronized (LOCK) {
            set = getInstance().mStartingWindowList.get(type);
        }
        return set;
    }

    public static boolean isInStartingWindowList(int type, String value) {
        synchronized (LOCK) {
            Set<String> set = getInstance().mStartingWindowList.get(type);
            if (set == null) {
                return false;
            }
            boolean contains = set.contains(value);
            return contains;
        }
    }

    public static boolean isSupportSplashSnapshotForAllApps() {
        boolean z;
        synchronized (LOCK) {
            z = getInstance().mSupportSplashSnapshotForAllApps;
        }
        return z;
    }

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorStartingWindowRUSHelper.STARTING_WINDOW_CONFIG_FILE)) {
                ColorStartingWindowUtils.logD("on starting window config file changed **********************");
                ColorStartingWindowRUSHelper.this.updateList();
            }
        }
    }
}
