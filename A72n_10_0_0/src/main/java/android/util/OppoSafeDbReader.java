package android.util;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
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
    private Thread mThread;
    private ArrayList<String> mUserAllowPackageName = new ArrayList<>();
    private ArrayList<String> mUserAllowToastPackageName = new ArrayList<>();
    private ArrayList<String> mUserClosePackageName = new ArrayList<>();
    private ArrayList<String> mUserCloseToastPackageName = new ArrayList<>();
    private Uri uri = Uri.parse("content://com.color.provider.SafeProvider/pp_float_window");

    public static OppoSafeDbReader getInstance(Context context) {
        if (sInstance == null) {
            synchronized (OppoSafeDbReader.class) {
                if (sInstance == null) {
                    sInstance = new OppoSafeDbReader(context);
                }
            }
        }
        return sInstance;
    }

    private OppoSafeDbReader(Context context) {
        this.mContext = context;
    }

    private void init() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mSafePPWindowObserver = new SafePPWindowObserver(this.mHandler);
        this.mSafePPWindowObserver.observe();
        this.mDataFileListener = new DataFileListener(TOAST_FILTER_FILE_PATH);
        this.mDataFileListener.startWatching();
        this.mThread = new Thread(new GetDataFromProviderRunnable(), "get_data");
        if (sInstance != null) {
            sInstance.getToastAppMapPri();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00c7, code lost:
        if (0 == 0) goto L_0x00cd;
     */
    private boolean getData() {
        Cursor cursor = null;
        boolean result = true;
        try {
            cursor = this.mContext.getContentResolver().query(this.uri, new String[]{PKG_NAME, ALLOWED}, null, null, null);
            int allowedIndex = cursor.getColumnIndex(ALLOWED);
            int pkgNameIndex = cursor.getColumnIndex(PKG_NAME);
            ArrayList<String> tempAllow = new ArrayList<>();
            ArrayList<String> tempClose = new ArrayList<>();
            while (cursor.moveToNext()) {
                int config = cursor.getInt(allowedIndex);
                if ((config & 4) == 0) {
                    if ((config & 1) == 0) {
                        tempClose.add(cursor.getString(pkgNameIndex));
                        if (sDebug) {
                            Log.d(TAG, "not allow: " + cursor.getString(pkgNameIndex));
                        }
                    }
                }
                tempAllow.add(cursor.getString(pkgNameIndex));
                if (sDebug) {
                    Log.d(TAG, "allow: " + cursor.getString(pkgNameIndex));
                }
            }
            synchronized (this.mLock) {
                this.mUserAllowPackageName.clear();
                this.mUserClosePackageName.clear();
                this.mUserAllowPackageName = tempAllow;
                this.mUserClosePackageName = tempClose;
            }
            this.mHasInit = true;
        } catch (Exception e) {
            Log.w(TAG, "We can not get floatwindow app user config data from provider,because of " + e);
            result = false;
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0031, code lost:
        if (android.util.OppoSafeDbReader.sDebug == false) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        android.util.Log.i(android.util.OppoSafeDbReader.TAG, "isUserOpen return: " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004a, code lost:
        return r0;
     */
    public boolean isUserOpen(String packageName) {
        boolean result = false;
        synchronized (this.mLock) {
            boolean z = true;
            if (!this.mHasInit) {
                Log.i(TAG, "init data error, don't intercept");
                return true;
            } else if (!(this.mUserAllowPackageName == null || this.mUserAllowToastPackageName == null)) {
                if (!this.mUserAllowPackageName.contains(packageName)) {
                    if (!this.mUserAllowToastPackageName.contains(packageName)) {
                        z = false;
                    }
                }
                result = z;
            }
        }
    }

    public boolean isUserClose(String packageName) {
        boolean z;
        boolean result = false;
        synchronized (this.mLock) {
            if (!(this.mUserClosePackageName == null || this.mUserClosePackageName == null)) {
                if (!this.mUserClosePackageName.contains(packageName)) {
                    if (!this.mUserCloseToastPackageName.contains(packageName)) {
                        z = false;
                        result = z;
                    }
                }
                z = true;
                result = z;
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public class SafePPWindowObserver extends ContentObserver {
        SafePPWindowObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Log.d(OppoSafeDbReader.TAG, "change  begin:");
            OppoSafeDbReader.this.getData();
            Log.d(OppoSafeDbReader.TAG, "change end:");
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            OppoSafeDbReader.this.mContext.getContentResolver().registerContentObserver(OppoSafeDbReader.this.uri, true, this);
        }
    }

    /* access modifiers changed from: private */
    public class DataFileListener extends FileObserver {
        String observerPath = null;

        public DataFileListener(String path) {
            super(path, 264);
            this.observerPath = path;
        }

        @Override // android.os.FileObserver
        public void onEvent(int event, String path) {
            String str;
            if (event == 8 && (str = this.observerPath) != null) {
                File file = new File(str);
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
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class GetDataFromProviderRunnable implements Runnable {
        public GetDataFromProviderRunnable() {
        }

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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getToastAppMapPri() {
        synchronized (this.mLock) {
            FileInputStream inputStream = null;
            try {
                File file = new File(TOAST_FILTER_FILE_PATH);
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                    this.mUserCloseToastPackageName.clear();
                    this.mUserAllowToastPackageName.clear();
                    readToastFromXML(inputStream);
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e = e;
                    }
                }
            } catch (Exception e2) {
                Log.e(TAG, "getToastAppMap() error !");
                e2.printStackTrace();
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        e = e3;
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                throw th;
            }
        }
        e.printStackTrace();
    }

    public void startThread() {
        init();
        this.mThread.start();
    }

    private void readToastFromXML(FileInputStream stream) {
        int type;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            do {
                type = parser.next();
                if (type == 2 && "toast".equals(parser.getName())) {
                    String name = null;
                    String text = null;
                    try {
                        name = parser.getAttributeValue(null, "packagename");
                        text = parser.nextText();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!(name == null || text == null)) {
                        if (WifiEnterpriseConfig.ENGINE_DISABLE.equals(text)) {
                            this.mUserCloseToastPackageName.add(name);
                            continue;
                        } else if (WifiEnterpriseConfig.ENGINE_ENABLE.equals(text)) {
                            this.mUserAllowToastPackageName.add(name);
                            continue;
                        } else {
                            continue;
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
