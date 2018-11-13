package com.oppo.hypnus;

import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Hypnus {
    public static final String ACTIONINFO = "/sys/kernel/hypnus/action_info";
    public static final int ACTION_AGAINST_IDLE = 16;
    public static final int ACTION_ANIMATION = 11;
    public static final int ACTION_AUDIO_PLAYBACK = 2;
    public static final int ACTION_BURST_ANR = 19;
    public static final int ACTION_BURST_BM = 20;
    public static final int ACTION_BURST_GC = 17;
    public static final int ACTION_BURST_LM = 18;
    public static final int ACTION_DOWNLOAD = 3;
    public static final int ACTION_IDLE = 0;
    public static final int ACTION_INSTALLATION = 15;
    public static final int ACTION_IO = 12;
    public static final int ACTION_LAUNCH = 13;
    public static final int ACTION_NONE = 1;
    public static final int ACTION_PERFD = 99;
    public static final int ACTION_PREVIEW = 5;
    public static final int ACTION_PRE_LAUNCH = 10;
    public static final int ACTION_RESUME = 9;
    public static final int ACTION_SCROLLING_H = 8;
    public static final int ACTION_SCROLLING_V = 7;
    public static final int ACTION_SNAPSHOT = 14;
    public static final int ACTION_VIDEO_ENCODING = 6;
    public static final int ACTION_VIDEO_PLAYBACK = 4;
    public static final int BURST_TYPE_GC = 1;
    public static final int BURST_TYPE_LM = 2;
    public static final String NOTIFICATIONINFO = "/sys/kernel/hypnus/notification_info";
    public static final String SCENEINFO = "/sys/kernel/hypnus/scene_info";
    public static final int SCENE_BENCHMARK = 6;
    public static final int SCENE_BOOT = 13;
    public static final int SCENE_BROWSER = 3;
    public static final int SCENE_CAMERA = 2;
    public static final int SCENE_EBOOK = 11;
    public static final int SCENE_GALLERY = 9;
    public static final int SCENE_HEAVY_GAME = 5;
    public static final int SCENE_IO = 14;
    public static final int SCENE_LAUNCHER = 12;
    public static final int SCENE_LIGHT_GAME = 4;
    public static final int SCENE_LISTVIEW = 8;
    public static final int SCENE_MUSIC = 1;
    public static final int SCENE_NAVIGATION = 10;
    public static final int SCENE_NORMAL = 0;
    public static final int SCENE_SUPERAPP = 15;
    public static final int SCENE_VIDEO = 7;
    private static final String TAG = "Hypnus";
    public static final int TIME_ANIMATION = 600;
    public static final int TIME_ANIMATION_100 = 100;
    public static final int TIME_ANIMATION_300 = 300;
    public static final int TIME_ANIMATION_500 = 500;
    public static final int TIME_BM = 50;
    public static final int TIME_BURST = 199;
    public static final int TIME_DEX2OAT = 20000;
    public static final int TIME_INSTALLATION = 30000;
    public static final int TIME_LAUNCH = 5000;
    public static final int TIME_MAX = 600000;
    public static final int TIME_PRE_LAUNCH = 150;
    public static final int TIME_SERVICE_DELAY = 100000;
    public static final String VERSION = "M08";
    public static final String VERSIONINFO = "/sys/kernel/hypnus/version";
    private static boolean mHypnusOK = false;
    private static String mName;
    private static int mPid;
    private static String mVersion;
    private boolean DEBUG = false;
    private int mCount = 0;
    private boolean mInIO = false;

    /* JADX WARNING: Removed duplicated region for block: B:41:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0061 A:{SYNTHETIC, Splitter: B:30:0x0061} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0052 A:{SYNTHETIC, Splitter: B:22:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x006d A:{SYNTHETIC, Splitter: B:36:0x006d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Hypnus() {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (SystemProperties.getBoolean("persist.debug.hypnus", false)) {
            this.DEBUG = true;
        }
        FileInputStream in = null;
        try {
            FileInputStream in2 = new FileInputStream(new File(VERSIONINFO));
            try {
                byte[] b = new byte[3];
                in2.read(b);
                mVersion = new String(b);
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                in = in2;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                in = in2;
                e2.printStackTrace();
                if (in != null) {
                }
                if (mVersion == null) {
                }
            } catch (IOException e5) {
                e3 = e5;
                in = in2;
                try {
                    e3.printStackTrace();
                    if (in != null) {
                    }
                    if (mVersion == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in = in2;
                if (in != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            e2.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
            if (mVersion == null) {
            }
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            if (mVersion == null) {
            }
        }
        if (mVersion == null) {
            Log.w(TAG, "Hypnus version is null, is the module there?");
            mHypnusOK = false;
            return;
        }
        mHypnusOK = true;
        if (this.DEBUG) {
            Log.d(TAG, "Hypnus framework module initialized, version:" + mVersion);
        }
        if (!mVersion.equals(VERSION)) {
            Log.i(TAG, "Framework: M08 module: " + mVersion);
        }
    }

    public void hypnusSetNotification(int msg_src, int msg_type) {
        hypnusSetNotification(msg_src, msg_type, 0, 0, 0, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ba A:{Catch:{ all -> 0x00d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c5 A:{SYNTHETIC, Splitter: B:40:0x00c5} */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0097 A:{Catch:{ all -> 0x00d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a2 A:{SYNTHETIC, Splitter: B:28:0x00a2} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00db A:{SYNTHETIC, Splitter: B:48:0x00db} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) {
        Throwable th;
        if (msg_time == 0) {
            msg_time = System.nanoTime();
        }
        if (pid == 0) {
            pid = Process.myPid();
        }
        File mNotificationInfoFile = new File(NOTIFICATIONINFO);
        String info = String.format("%d %d %d %d %d %d", new Object[]{Integer.valueOf(msg_src), Long.valueOf(msg_time), Integer.valueOf(msg_type), Integer.valueOf(pid), Integer.valueOf(v0), Integer.valueOf(v1)});
        if (mNotificationInfoFile.canWrite()) {
            FileOutputStream out = null;
            try {
                FileOutputStream out2 = new FileOutputStream(mNotificationInfoFile);
                try {
                    out2.write(info.getBytes());
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e) {
                            if (this.DEBUG) {
                                Log.d(TAG, "IOException");
                            }
                        }
                    }
                } catch (FileNotFoundException e2) {
                    out = out2;
                    if (this.DEBUG) {
                    }
                    if (out != null) {
                    }
                    if (this.DEBUG) {
                    }
                } catch (IOException e3) {
                    out = out2;
                    try {
                        if (this.DEBUG) {
                        }
                        if (out != null) {
                        }
                        if (this.DEBUG) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e4) {
                                if (this.DEBUG) {
                                    Log.d(TAG, "IOException");
                                }
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                if (this.DEBUG) {
                    Log.d(TAG, "FileNotFoundException");
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e6) {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                    }
                }
                if (this.DEBUG) {
                }
            } catch (IOException e7) {
                if (this.DEBUG) {
                    Log.d(TAG, "IOException");
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e8) {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                    }
                }
                if (this.DEBUG) {
                }
            }
        }
        if (this.DEBUG) {
            Log.d(TAG, "hypnusSetNotification:" + info);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00b8 A:{Catch:{ all -> 0x00d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c3 A:{SYNTHETIC, Splitter: B:42:0x00c3} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0095 A:{Catch:{ all -> 0x00d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a0 A:{SYNTHETIC, Splitter: B:30:0x00a0} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00d9 A:{SYNTHETIC, Splitter: B:50:0x00d9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hypnusSetScene(int pid, String processName) {
        Throwable th;
        if (mPid == pid) {
            if (this.DEBUG) {
                Log.d(TAG, "Same PID ignore");
            }
            return;
        }
        mPid = pid;
        File mSceneInfoFile = new File(SCENEINFO);
        String info = String.format("%d %d ", new Object[]{Integer.valueOf(0), Integer.valueOf(pid)});
        mName = processName;
        info = info + mName;
        if (mSceneInfoFile.canWrite()) {
            FileOutputStream out = null;
            try {
                FileOutputStream out2 = new FileOutputStream(mSceneInfoFile);
                try {
                    out2.write(info.getBytes());
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e) {
                            if (this.DEBUG) {
                                Log.d(TAG, "IOException");
                            }
                        }
                    }
                } catch (FileNotFoundException e2) {
                    out = out2;
                    if (this.DEBUG) {
                    }
                    if (out != null) {
                    }
                    if (this.DEBUG) {
                    }
                } catch (IOException e3) {
                    out = out2;
                    try {
                        if (this.DEBUG) {
                        }
                        if (out != null) {
                        }
                        if (this.DEBUG) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e4) {
                                if (this.DEBUG) {
                                    Log.d(TAG, "IOException");
                                }
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                if (this.DEBUG) {
                    Log.d(TAG, "FileNotFoundException");
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e6) {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                    }
                }
                if (this.DEBUG) {
                }
            } catch (IOException e7) {
                if (this.DEBUG) {
                    Log.d(TAG, "IOException");
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e8) {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                    }
                }
                if (this.DEBUG) {
                }
            }
        }
        if (this.DEBUG) {
            Log.d(TAG, "hypnusSetScene:" + info);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0090 A:{Catch:{ all -> 0x00ae }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009b A:{SYNTHETIC, Splitter: B:35:0x009b} */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x006d A:{Catch:{ all -> 0x00ae }} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0078 A:{SYNTHETIC, Splitter: B:23:0x0078} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b1 A:{SYNTHETIC, Splitter: B:43:0x00b1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hypnusSendBootComplete() {
        Throwable th;
        File mSceneInfoFile = new File(SCENEINFO);
        String info = String.format("%d %d 0", new Object[]{Integer.valueOf(13), Integer.valueOf(0)});
        if (mSceneInfoFile.canWrite()) {
            FileOutputStream out = null;
            try {
                FileOutputStream out2 = new FileOutputStream(mSceneInfoFile);
                try {
                    out2.write(info.getBytes());
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e) {
                            if (this.DEBUG) {
                                Log.d(TAG, "IOException");
                            }
                        }
                    }
                } catch (FileNotFoundException e2) {
                    out = out2;
                    if (this.DEBUG) {
                    }
                    if (out != null) {
                    }
                    if (this.DEBUG) {
                    }
                } catch (IOException e3) {
                    out = out2;
                    try {
                        if (this.DEBUG) {
                        }
                        if (out != null) {
                        }
                        if (this.DEBUG) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e4) {
                            if (this.DEBUG) {
                                Log.d(TAG, "IOException");
                            }
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                if (this.DEBUG) {
                    Log.d(TAG, "FileNotFoundException");
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e6) {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                    }
                }
                if (this.DEBUG) {
                }
            } catch (IOException e7) {
                if (this.DEBUG) {
                    Log.d(TAG, "IOException");
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e8) {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                    }
                }
                if (this.DEBUG) {
                }
            }
        }
        if (this.DEBUG) {
            Log.d(TAG, "hypnusSendBootComplete:" + info);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x015d A:{SYNTHETIC, Splitter: B:88:0x015d} */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x013a A:{Catch:{ all -> 0x015a }} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0145 A:{SYNTHETIC, Splitter: B:80:0x0145} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0115 A:{Catch:{ all -> 0x015a }} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0120 A:{SYNTHETIC, Splitter: B:68:0x0120} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009f  */
    /* JADX WARNING: Missing block: B:22:0x0063, code:
            r3 = new java.io.File(ACTIONINFO);
     */
    /* JADX WARNING: Missing block: B:23:0x006f, code:
            if (r3.canWrite() == false) goto L_0x009b;
     */
    /* JADX WARNING: Missing block: B:24:0x0071, code:
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            r5 = new java.io.FileOutputStream(r3);
     */
    /* JADX WARNING: Missing block: B:28:?, code:
            r5.write(java.lang.String.format("%d %d", new java.lang.Object[]{java.lang.Integer.valueOf(r11), java.lang.Integer.valueOf(r12)}).getBytes());
     */
    /* JADX WARNING: Missing block: B:29:0x0096, code:
            if (r5 == null) goto L_0x009b;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            r5.close();
     */
    /* JADX WARNING: Missing block: B:33:0x009d, code:
            if (r10.DEBUG != false) goto L_0x009f;
     */
    /* JADX WARNING: Missing block: B:34:0x009f, code:
            android.util.Log.d(TAG, "hypnusSetAction:" + r11 + " timeout:" + r12);
     */
    /* JADX WARNING: Missing block: B:35:0x00c4, code:
            return;
     */
    /* JADX WARNING: Missing block: B:55:0x00fe, code:
            return;
     */
    /* JADX WARNING: Missing block: B:60:0x0104, code:
            if (r10.DEBUG != false) goto L_0x0106;
     */
    /* JADX WARNING: Missing block: B:61:0x0106, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:65:0x0113, code:
            if (r10.DEBUG != false) goto L_0x0115;
     */
    /* JADX WARNING: Missing block: B:66:0x0115, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:67:0x011e, code:
            if (r4 != null) goto L_0x0120;
     */
    /* JADX WARNING: Missing block: B:69:?, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:72:0x0128, code:
            if (r10.DEBUG != false) goto L_0x012a;
     */
    /* JADX WARNING: Missing block: B:73:0x012a, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:77:0x0138, code:
            if (r10.DEBUG != false) goto L_0x013a;
     */
    /* JADX WARNING: Missing block: B:78:0x013a, code:
            android.util.Log.d(TAG, "FileNotFoundException");
     */
    /* JADX WARNING: Missing block: B:79:0x0143, code:
            if (r4 != null) goto L_0x0145;
     */
    /* JADX WARNING: Missing block: B:81:?, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:84:0x014d, code:
            if (r10.DEBUG != false) goto L_0x014f;
     */
    /* JADX WARNING: Missing block: B:85:0x014f, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:86:0x015a, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:87:0x015b, code:
            if (r4 != null) goto L_0x015d;
     */
    /* JADX WARNING: Missing block: B:89:?, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:90:0x0160, code:
            throw r6;
     */
    /* JADX WARNING: Missing block: B:93:0x0164, code:
            if (r10.DEBUG != false) goto L_0x0166;
     */
    /* JADX WARNING: Missing block: B:94:0x0166, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:95:0x0170, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:96:0x0171, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:98:0x0174, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:100:0x0177, code:
            r4 = r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hypnusSetAction(int action, int timeout) {
        synchronized (this) {
            if (99 == action) {
                if (mName == null) {
                    return;
                }
                if (mName.indexOf("filemanager") == -1 && mName.indexOf("backuprestore") == -1) {
                    if (mName.indexOf("android.process.media") == -1) {
                        if (this.mInIO) {
                            action = 12;
                            timeout = 0;
                            this.mInIO = false;
                            this.mCount = 0;
                            if (this.DEBUG) {
                                Log.d(TAG, "Handle ACTION_PERFD, name: " + mName + " : " + timeout);
                            }
                        } else {
                            return;
                        }
                    }
                }
                if (timeout != 0) {
                    this.mCount++;
                    if (this.mCount == 1) {
                        action = 12;
                        timeout = TIME_MAX;
                        this.mInIO = true;
                    } else {
                        return;
                    }
                }
                this.mCount--;
                if (this.mCount == 0) {
                    action = 12;
                    timeout = 0;
                    this.mInIO = false;
                } else if (this.mCount < 0) {
                    this.mCount = 0;
                }
                if (this.DEBUG) {
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00d5 A:{Catch:{ all -> 0x00f4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00e0 A:{SYNTHETIC, Splitter: B:45:0x00e0} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00b2 A:{Catch:{ all -> 0x00f4 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00bd A:{SYNTHETIC, Splitter: B:33:0x00bd} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00f7 A:{SYNTHETIC, Splitter: B:53:0x00f7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hypnusSetBurst(int tid, int type, int timeout) {
        int act;
        Throwable th;
        File mActionInfoFile = new File(ACTIONINFO);
        switch (type) {
            case 1:
                act = 17;
                if (tid <= 0) {
                    timeout = 0;
                    break;
                } else {
                    timeout = TIME_BURST;
                    break;
                }
            case 2:
                act = 18;
                break;
            default:
                Log.e(TAG, "hypnusSetBurst: Inavlid burst type:" + type);
                return;
        }
        if (mActionInfoFile.canWrite()) {
            FileOutputStream out = null;
            try {
                FileOutputStream out2 = new FileOutputStream(mActionInfoFile);
                try {
                    out2.write(String.format("%d %d %d", new Object[]{Integer.valueOf(act), Integer.valueOf(timeout), Integer.valueOf(tid)}).getBytes());
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e) {
                            if (this.DEBUG) {
                                Log.d(TAG, "IOException");
                            }
                        }
                    }
                } catch (FileNotFoundException e2) {
                    out = out2;
                    if (this.DEBUG) {
                    }
                    if (out != null) {
                    }
                    if (this.DEBUG) {
                    }
                } catch (IOException e3) {
                    out = out2;
                    try {
                        if (this.DEBUG) {
                        }
                        if (out != null) {
                        }
                        if (this.DEBUG) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e4) {
                                if (this.DEBUG) {
                                    Log.d(TAG, "IOException");
                                }
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                if (this.DEBUG) {
                    Log.d(TAG, "FileNotFoundException");
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e6) {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                    }
                }
                if (this.DEBUG) {
                }
            } catch (IOException e7) {
                if (this.DEBUG) {
                    Log.d(TAG, "IOException");
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e8) {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                    }
                }
                if (this.DEBUG) {
                }
            }
        }
        if (this.DEBUG) {
            Log.d(TAG, "hypnusSetBurst tid:" + tid + " act:" + act + " timeout:" + timeout);
        }
    }

    public boolean isHypnusOK() {
        return mHypnusOK;
    }
}
