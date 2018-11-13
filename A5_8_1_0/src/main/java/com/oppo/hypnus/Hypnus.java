package com.oppo.hypnus;

import android.app.AppGlobals;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

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
    public static final Boolean HYPNUS_STATICS_ON = Boolean.valueOf(SystemProperties.getBoolean("persist.sys.hypnus.statics", false));
    public static final String NOTIFICATIONINFO = "/sys/kernel/hypnus/notification_info";
    public static final String SCENEINFO = "/sys/kernel/hypnus/scene_info";
    public static final int SCENE_BEAUTY_CAMERA = 21;
    public static final int SCENE_BENCHMARK = 6;
    public static final int SCENE_BOOT = 13;
    public static final int SCENE_BROWSER = 3;
    public static final int SCENE_CAMERA = 2;
    public static final int SCENE_EBOOK = 11;
    public static final int SCENE_GALLERY = 9;
    public static final int SCENE_HEAVY_GAME = 5;
    public static final int SCENE_HEAVY_GPU = 20;
    public static final int SCENE_IO = 14;
    public static final int SCENE_LAUNCHER = 12;
    public static final int SCENE_LIGHT_GAME = 4;
    public static final int SCENE_LISTVIEW = 8;
    public static final int SCENE_MUSIC = 1;
    public static final int SCENE_NAVIGATION = 10;
    public static final int SCENE_NORMAL = 0;
    public static final int SCENE_OPTIMGAME = 19;
    public static final int SCENE_ORIGINAL = 17;
    public static final int SCENE_POWERSAVE = 16;
    public static final int SCENE_SUPERAPP = 15;
    public static final int SCENE_SUPERGAME = 18;
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
    public static final int TIME_LAUNCH = 2000;
    public static final int TIME_MAX = 600000;
    public static final int TIME_PRE_LAUNCH = 150;
    public static final int TIME_SERVICE_DELAY = 100000;
    public static final String VERSION = "M08";
    public static final String VERSIONINFO = "/sys/kernel/hypnus/version";
    private static boolean mHypnusOK = false;
    private static String mName;
    private static int mPid;
    private static String mVersion;
    private static Hypnus sHypnus;
    public static volatile HashMap<String, Long> staticsCount = new HashMap();
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
    /* JADX WARNING: Removed duplicated region for block: B:17:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00bc A:{Catch:{ all -> 0x00da }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c7 A:{SYNTHETIC, Splitter: B:40:0x00c7} */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0099 A:{Catch:{ all -> 0x00da }} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a4 A:{SYNTHETIC, Splitter: B:28:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00dd A:{SYNTHETIC, Splitter: B:48:0x00dd} */
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
        String info = String.format(Locale.US, "%d %d %d %d %d", new Object[]{Integer.valueOf(msg_src), Long.valueOf(msg_time), Integer.valueOf(msg_type), Integer.valueOf(pid), Integer.valueOf(v0), Integer.valueOf(v1)});
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
                        Log.d(TAG, "FileNotFoundException");
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e3) {
                            if (this.DEBUG) {
                                Log.d(TAG, "IOException");
                            }
                        }
                    }
                    if (this.DEBUG) {
                    }
                } catch (IOException e4) {
                    out = out2;
                    try {
                        if (this.DEBUG) {
                            Log.d(TAG, "IOException");
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e5) {
                                if (this.DEBUG) {
                                    Log.d(TAG, "IOException");
                                }
                            }
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
                        } catch (IOException e6) {
                            if (this.DEBUG) {
                                Log.d(TAG, "IOException");
                            }
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                if (this.DEBUG) {
                }
                if (out != null) {
                }
                if (this.DEBUG) {
                }
            } catch (IOException e8) {
                if (this.DEBUG) {
                }
                if (out != null) {
                }
                if (this.DEBUG) {
                }
            }
        }
        if (this.DEBUG) {
            Log.d(TAG, "hypnusSetNotification:" + info);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00ba A:{Catch:{ all -> 0x00d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c5 A:{SYNTHETIC, Splitter: B:42:0x00c5} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0097 A:{Catch:{ all -> 0x00d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a2 A:{SYNTHETIC, Splitter: B:30:0x00a2} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00db A:{SYNTHETIC, Splitter: B:50:0x00db} */
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
        String info = String.format(Locale.US, "%d %d ", new Object[]{Integer.valueOf(0), Integer.valueOf(pid)});
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
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0092 A:{Catch:{ all -> 0x00b0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009d A:{SYNTHETIC, Splitter: B:35:0x009d} */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x006f A:{Catch:{ all -> 0x00b0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x007a A:{SYNTHETIC, Splitter: B:23:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b3 A:{SYNTHETIC, Splitter: B:43:0x00b3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hypnusSendBootComplete() {
        Throwable th;
        File mSceneInfoFile = new File(SCENEINFO);
        String info = String.format(Locale.US, "%d %d 0", new Object[]{Integer.valueOf(13), Integer.valueOf(0)});
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

    public void hypnusSetAction(int action, int timeout) {
        if (HYPNUS_STATICS_ON.booleanValue()) {
            try {
                String pkgnameinfo = AppGlobals.getPackageManager().getNameForUid(Process.myUid());
                if (pkgnameinfo == null) {
                    pkgnameinfo = "nopackagename";
                }
                int splitIndex = pkgnameinfo.indexOf(58);
                if (splitIndex > 0) {
                    hypnusSetAction(action, timeout, pkgnameinfo.substring(0, splitIndex));
                    return;
                } else {
                    hypnusSetAction(action, timeout, pkgnameinfo);
                    return;
                }
            } catch (RemoteException e) {
                hypnusSetAction(action, timeout, "exception");
                e.printStackTrace();
                return;
            }
        }
        hypnusSetAction(action, timeout, null);
    }

    public void recordActionCount(int action, int timeout, String pkgname) {
        if (staticsCount.get(pkgname + "_" + action) != null) {
            staticsCount.put(pkgname + "_" + action, Long.valueOf(((Long) staticsCount.get(pkgname + "_" + action)).longValue() + 1));
        } else {
            staticsCount.put(pkgname + "_" + action, Long.valueOf(1));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f6  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x01b4 A:{SYNTHETIC, Splitter: B:98:0x01b4} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0191 A:{Catch:{ all -> 0x01b1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x019c A:{SYNTHETIC, Splitter: B:90:0x019c} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f6  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x016c A:{Catch:{ all -> 0x01b1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0177 A:{SYNTHETIC, Splitter: B:78:0x0177} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f6  */
    /* JADX WARNING: Missing block: B:32:0x00b8, code:
            r3 = new java.io.File(ACTIONINFO);
     */
    /* JADX WARNING: Missing block: B:33:0x00c4, code:
            if (r3.canWrite() == false) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:34:0x00c6, code:
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            r5 = new java.io.FileOutputStream(r3);
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            r5.write(java.lang.String.format(java.util.Locale.US, "%d %d", new java.lang.Object[]{java.lang.Integer.valueOf(r12), java.lang.Integer.valueOf(r13)}).getBytes());
     */
    /* JADX WARNING: Missing block: B:39:0x00ed, code:
            if (r5 == null) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            r5.close();
     */
    /* JADX WARNING: Missing block: B:43:0x00f4, code:
            if (r11.DEBUG != false) goto L_0x00f6;
     */
    /* JADX WARNING: Missing block: B:44:0x00f6, code:
            android.util.Log.d(TAG, "hypnusSetAction:" + r12 + " timeout:" + r13);
     */
    /* JADX WARNING: Missing block: B:45:0x011b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:65:0x0155, code:
            return;
     */
    /* JADX WARNING: Missing block: B:70:0x015b, code:
            if (r11.DEBUG != false) goto L_0x015d;
     */
    /* JADX WARNING: Missing block: B:71:0x015d, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:75:0x016a, code:
            if (r11.DEBUG != false) goto L_0x016c;
     */
    /* JADX WARNING: Missing block: B:76:0x016c, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:77:0x0175, code:
            if (r4 != null) goto L_0x0177;
     */
    /* JADX WARNING: Missing block: B:79:?, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:82:0x017f, code:
            if (r11.DEBUG != false) goto L_0x0181;
     */
    /* JADX WARNING: Missing block: B:83:0x0181, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:87:0x018f, code:
            if (r11.DEBUG != false) goto L_0x0191;
     */
    /* JADX WARNING: Missing block: B:88:0x0191, code:
            android.util.Log.d(TAG, "FileNotFoundException");
     */
    /* JADX WARNING: Missing block: B:89:0x019a, code:
            if (r4 != null) goto L_0x019c;
     */
    /* JADX WARNING: Missing block: B:91:?, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:94:0x01a4, code:
            if (r11.DEBUG != false) goto L_0x01a6;
     */
    /* JADX WARNING: Missing block: B:95:0x01a6, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:96:0x01b1, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:97:0x01b2, code:
            if (r4 != null) goto L_0x01b4;
     */
    /* JADX WARNING: Missing block: B:99:?, code:
            r4.close();
     */
    /* JADX WARNING: Missing block: B:100:0x01b7, code:
            throw r6;
     */
    /* JADX WARNING: Missing block: B:103:0x01bb, code:
            if (r11.DEBUG != false) goto L_0x01bd;
     */
    /* JADX WARNING: Missing block: B:104:0x01bd, code:
            android.util.Log.d(TAG, "IOException");
     */
    /* JADX WARNING: Missing block: B:105:0x01c7, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:106:0x01c8, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:108:0x01cb, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:110:0x01ce, code:
            r4 = r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hypnusSetAction(int action, int timeout, String pkgname) {
        if (timeout > 180000) {
            Log.e(TAG, "hypnusSetAction: timeout longer than 180s, preven it. timeout value: " + timeout);
            timeout = 180000;
        }
        synchronized (this) {
            if (action > 11) {
                if (HYPNUS_STATICS_ON.booleanValue() && pkgname != null) {
                    recordActionCount(action, timeout, pkgname);
                    if (this.DEBUG) {
                        Log.d(TAG, action + ":" + pkgname);
                    }
                }
            }
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

    /* JADX WARNING: Removed duplicated region for block: B:19:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00d7 A:{Catch:{ all -> 0x00f6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00e2 A:{SYNTHETIC, Splitter: B:45:0x00e2} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00b4 A:{Catch:{ all -> 0x00f6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00bf A:{SYNTHETIC, Splitter: B:33:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00f9 A:{SYNTHETIC, Splitter: B:53:0x00f9} */
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
                    out2.write(String.format(Locale.US, "%d %d %d", new Object[]{Integer.valueOf(act), Integer.valueOf(timeout), Integer.valueOf(tid)}).getBytes());
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

    public static synchronized Hypnus getHypnus() {
        Hypnus hypnus;
        synchronized (Hypnus.class) {
            if (sHypnus == null) {
                sHypnus = new Hypnus();
            }
            if (sHypnus == null) {
                Log.e(TAG, "Hypnus is null");
            }
            hypnus = sHypnus;
        }
        return hypnus;
    }

    public static String getLocalSignature() {
        return "308203633082024ba00302010202040875ec17300d06092a864886f70d01010b05003062310b300906035504061302383631123010060355040813096775616e67646f6e673111300f060355040713087368656e7a68656e310e300c060355040a13056368696e61310e300c060355040b13056368696e61310c300a06035504031303726f6d301e170d3135303130373036343930325a170d3235303130343036343930325a3062310b300906035504061302383631123010060355040813096775616e67646f6e673111300f060355040713087368656e7a68656e310e300c060355040a13056368696e61310e300c060355040b13056368696e61310c300a06035504031303726f6d30820122300d06092a864886f70d01010105000382010f003082010a0282010100a4677dd7cdd8d842b767d4a4";
    }
}
