package android.util;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public final class OppoSafeDbReader {
    private static final String ALLOWED = "allowed";
    private static final String PKG_NAME = "pkg_name";
    private static final int SLEEP_TIME = 100;
    private static final String TAG = "OppoSafeDbReader";
    private static final String TOAST_FILTER_FILE_PATH = "//data//oppo//coloros//toast//toast.xml";
    private static final int USR_OPEN_BIT = 4;
    private static final int WHITE_LIST_BIT = 1;
    private static boolean sDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static volatile OppoSafeDbReader sInstance = null;
    private Context mContext;
    private DataFileListener mDataFileListener;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasInit = false;
    private Object mLock = new Object();
    private SafePPWindowObserver mSafePPWindowObserver;
    private ArrayList<String> mUserAllowPackageName = new ArrayList();
    private ArrayList<String> mUserAllowToastPackageName = new ArrayList();
    private ArrayList<String> mUserClosePackageName = new ArrayList();
    private ArrayList<String> mUserCloseToastPackageName = new ArrayList();
    private Thread thread;
    private Uri uri = Uri.parse("content://com.color.provider.SafeProvider/pp_float_window");

    private class DataFileListener extends FileObserver {
        String observerPath = null;

        public DataFileListener(String path) {
            super(path, 264);
            this.observerPath = path;
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 8:
                    if (this.observerPath != null) {
                        File file = new File(this.observerPath);
                        if (!file.exists()) {
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdirs();
                            }
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (this.observerPath.equals(OppoSafeDbReader.TOAST_FILTER_FILE_PATH)) {
                            OppoSafeDbReader.this.getToastAppMapPri();
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class GetDataFromProviderRunnable implements Runnable {
        public void run() {
            if (OppoSafeDbReader.sDebug) {
                Log.d(OppoSafeDbReader.TAG, "start run ");
            }
            while (!ActivityManagerNative.isSystemReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.w(OppoSafeDbReader.TAG, "sleep 100 ms is Interrupted because of " + e);
                }
            }
            OppoSafeDbReader.this.getData();
            if (OppoSafeDbReader.sDebug) {
                Log.d(OppoSafeDbReader.TAG, "isSystemReady is true  !!!!! ");
            }
        }
    }

    class SafePPWindowObserver extends ContentObserver {
        SafePPWindowObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            Log.d(OppoSafeDbReader.TAG, "change  begin:");
            OppoSafeDbReader.this.getData();
            Log.d(OppoSafeDbReader.TAG, "change end:");
        }

        void observe() {
            OppoSafeDbReader.this.mContext.getContentResolver().registerContentObserver(OppoSafeDbReader.this.uri, true, this);
        }
    }

    public static OppoSafeDbReader getInstance(Context context) {
        if (sInstance == null) {
            synchronized (OppoSafeDbReader.class) {
                if (sInstance == null) {
                    sInstance = new OppoSafeDbReader(context);
                    sInstance.getToastAppMapPri();
                }
            }
        }
        return sInstance;
    }

    private OppoSafeDbReader(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mSafePPWindowObserver = new SafePPWindowObserver(this.mHandler);
        this.mSafePPWindowObserver.observe();
        this.mDataFileListener = new DataFileListener(TOAST_FILTER_FILE_PATH);
        this.mDataFileListener.startWatching();
        this.thread = new Thread(new GetDataFromProviderRunnable(), "get_data");
    }

    private boolean getData() {
        Cursor cursor = null;
        boolean result = true;
        try {
            cursor = this.mContext.getContentResolver().query(this.uri, new String[]{PKG_NAME, ALLOWED}, null, null, null);
            int allowedIndex = cursor.getColumnIndex(ALLOWED);
            int pkgNameIndex = cursor.getColumnIndex(PKG_NAME);
            ArrayList<String> tempAllow = new ArrayList();
            ArrayList<String> tempClose = new ArrayList();
            while (cursor != null && cursor.moveToNext()) {
                int config = cursor.getInt(allowedIndex);
                if ((config & 4) == 0 && (config & 1) == 0) {
                    tempClose.add(cursor.getString(pkgNameIndex));
                    if (sDebug) {
                        Log.d(TAG, "not allow: " + cursor.getString(pkgNameIndex));
                    }
                } else {
                    tempAllow.add(cursor.getString(pkgNameIndex));
                    if (sDebug) {
                        Log.d(TAG, "allow: " + cursor.getString(pkgNameIndex));
                    }
                }
            }
            synchronized (this.mLock) {
                this.mUserAllowPackageName.clear();
                this.mUserClosePackageName.clear();
                this.mUserAllowPackageName = tempAllow;
                this.mUserClosePackageName = tempClose;
            }
            this.mHasInit = true;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "We can not get floatwindow app user config data from provider,because of " + e);
            result = false;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /* JADX WARNING: Missing block: B:19:0x002d, code:
            if (sDebug == false) goto L_0x0049;
     */
    /* JADX WARNING: Missing block: B:20:0x002f, code:
            android.util.Log.i(TAG, "isUserOpen return: " + r0);
     */
    /* JADX WARNING: Missing block: B:21:0x0049, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isUserOpen(String packageName) {
        boolean result = false;
        synchronized (this.mLock) {
            if (!this.mHasInit) {
                Log.i(TAG, "init data error, don't intercept");
                return true;
            } else if (!(this.mUserAllowPackageName == null || this.mUserAllowToastPackageName == null)) {
                result = !this.mUserAllowPackageName.contains(packageName) ? this.mUserAllowToastPackageName.contains(packageName) : true;
            }
        }
    }

    public boolean isUserClose(String packageName) {
        boolean result = false;
        synchronized (this.mLock) {
            if (!(this.mUserClosePackageName == null || this.mUserClosePackageName == null)) {
                result = !this.mUserClosePackageName.contains(packageName) ? this.mUserCloseToastPackageName.contains(packageName) : true;
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x004f A:{SYNTHETIC, Splitter: B:32:0x004f} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0043 A:{SYNTHETIC, Splitter: B:25:0x0043} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getToastAppMapPri() {
        Exception e;
        Throwable th;
        synchronized (this.mLock) {
            FileInputStream inputStream = null;
            try {
                File file = new File(TOAST_FILTER_FILE_PATH);
                if (file.exists()) {
                    FileInputStream inputStream2 = new FileInputStream(file);
                    try {
                        this.mUserCloseToastPackageName.clear();
                        this.mUserAllowToastPackageName.clear();
                        readToastFromXML(inputStream2);
                        inputStream = inputStream2;
                    } catch (Exception e2) {
                        e = e2;
                        inputStream = inputStream2;
                        try {
                            Log.e(TAG, "getToastAppMap() error !");
                            e.printStackTrace();
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (inputStream != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        inputStream = inputStream2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
            } catch (Exception e4) {
                e = e4;
                Log.e(TAG, "getToastAppMap() error !");
                e.printStackTrace();
                if (inputStream != null) {
                }
                return;
            }
        }
        return;
    }

    public void startThread() {
        this.thread.start();
    }

    private void readToastFromXML(FileInputStream stream) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            int type;
            do {
                type = parser.next();
                if (type == 2) {
                    if ("toast".equals(parser.getName())) {
                        Object obj = null;
                        Object text = null;
                        try {
                            obj = parser.getAttributeValue(null, "packagename");
                            text = parser.nextText();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!(obj == null || text == null)) {
                            if ("0".equals(text)) {
                                this.mUserCloseToastPackageName.add(obj);
                            } else if ("1".equals(text)) {
                                this.mUserAllowToastPackageName.add(obj);
                            }
                        }
                    }
                }
            } while (type != 1);
        } catch (Exception e2) {
            Log.e(TAG, "readToastFromXML() error");
            e2.printStackTrace();
        }
    }
}
