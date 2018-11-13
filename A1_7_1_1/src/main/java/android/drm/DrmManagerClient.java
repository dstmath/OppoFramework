package android.drm;

import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.drm.DrmStore.Action;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DrmManagerClient implements AutoCloseable {
    private static final int ACTION_PROCESS_DRM_INFO = 1002;
    private static final int ACTION_REMOVE_ALL_RIGHTS = 1001;
    public static final int ERROR_NONE = 0;
    public static final int ERROR_UNKNOWN = -2000;
    public static final int INVALID_SESSION = -1;
    private static final String TAG = "DrmManagerClient";
    private final CloseGuard mCloseGuard;
    private final AtomicBoolean mClosed;
    private Context mContext;
    private EventHandler mEventHandler;
    HandlerThread mEventThread;
    private InfoHandler mInfoHandler;
    HandlerThread mInfoThread;
    private long mNativeContext;
    private OnErrorListener mOnErrorListener;
    private OnEventListener mOnEventListener;
    private OnInfoListener mOnInfoListener;
    private int mUniqueId;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            DrmEvent event = null;
            DrmErrorEvent error = null;
            HashMap<String, Object> attributes = new HashMap();
            switch (msg.what) {
                case 1001:
                    if (DrmManagerClient.this._removeAllRights(DrmManagerClient.this.mUniqueId) != 0) {
                        error = new DrmErrorEvent(DrmManagerClient.this.mUniqueId, DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED, null);
                        break;
                    } else {
                        event = new DrmEvent(DrmManagerClient.this.mUniqueId, 1001, null);
                        break;
                    }
                case 1002:
                    DrmInfo drmInfo = msg.obj;
                    DrmInfoStatus status = DrmManagerClient.this._processDrmInfo(DrmManagerClient.this.mUniqueId, drmInfo);
                    attributes.put(DrmEvent.DRM_INFO_STATUS_OBJECT, status);
                    attributes.put(DrmEvent.DRM_INFO_OBJECT, drmInfo);
                    if (status != null && 1 == status.statusCode) {
                        event = new DrmEvent(DrmManagerClient.this.mUniqueId, DrmManagerClient.this.getEventType(status.infoType), null, attributes);
                        break;
                    } else {
                        error = new DrmErrorEvent(DrmManagerClient.this.mUniqueId, DrmManagerClient.this.getErrorType(status != null ? status.infoType : drmInfo.getInfoType()), null, attributes);
                        break;
                    }
                    break;
                default:
                    Log.e(DrmManagerClient.TAG, "Unknown message type " + msg.what);
                    return;
            }
            if (!(DrmManagerClient.this.mOnEventListener == null || event == null)) {
                DrmManagerClient.this.mOnEventListener.onEvent(DrmManagerClient.this, event);
            }
            if (!(DrmManagerClient.this.mOnErrorListener == null || error == null)) {
                DrmManagerClient.this.mOnErrorListener.onError(DrmManagerClient.this, error);
            }
        }
    }

    private class InfoHandler extends Handler {
        public static final int INFO_EVENT_TYPE = 1;

        public InfoHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            DrmInfoEvent info = null;
            DrmErrorEvent drmErrorEvent = null;
            switch (msg.what) {
                case 1:
                    int uniqueId = msg.arg1;
                    int infoType = msg.arg2;
                    String message = msg.obj.toString();
                    switch (infoType) {
                        case 1:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 10001:
                            info = new DrmInfoEvent(uniqueId, infoType, message);
                            break;
                        case 2:
                            try {
                                DrmUtils.removeFile(message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            info = new DrmInfoEvent(uniqueId, infoType, message);
                            break;
                        default:
                            drmErrorEvent = new DrmErrorEvent(uniqueId, infoType, message);
                            break;
                    }
                    if (!(DrmManagerClient.this.mOnInfoListener == null || info == null)) {
                        DrmManagerClient.this.mOnInfoListener.onInfo(DrmManagerClient.this, info);
                    }
                    if (!(DrmManagerClient.this.mOnErrorListener == null || drmErrorEvent == null)) {
                        DrmManagerClient.this.mOnErrorListener.onError(DrmManagerClient.this, drmErrorEvent);
                    }
                    return;
                default:
                    Log.e(DrmManagerClient.TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    public interface OnErrorListener {
        void onError(DrmManagerClient drmManagerClient, DrmErrorEvent drmErrorEvent);
    }

    public interface OnEventListener {
        void onEvent(DrmManagerClient drmManagerClient, DrmEvent drmEvent);
    }

    public interface OnInfoListener {
        void onInfo(DrmManagerClient drmManagerClient, DrmInfoEvent drmInfoEvent);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.drm.DrmManagerClient.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.drm.DrmManagerClient.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.drm.DrmManagerClient.<clinit>():void");
    }

    private native DrmInfo _acquireDrmInfo(int i, DrmInfoRequest drmInfoRequest);

    private native boolean _canHandle(int i, String str, String str2);

    private native int _checkRightsStatus(int i, String str, int i2);

    private native DrmConvertedStatus _closeConvertSession(int i, int i2);

    private native DrmConvertedStatus _convertData(int i, int i2, byte[] bArr);

    private native DrmSupportInfo[] _getAllSupportInfo(int i);

    private native ContentValues _getConstraints(int i, String str, int i2);

    private native int _getDrmObjectType(int i, String str, String str2);

    private native ContentValues _getMetadata(int i, String str);

    private native String _getOriginalMimeType(int i, String str, FileDescriptor fileDescriptor);

    private native int _initialize();

    private native void _installDrmEngine(int i, String str);

    private native int _openConvertSession(int i, String str);

    private native DrmInfoStatus _processDrmInfo(int i, DrmInfo drmInfo);

    private native void _release(int i);

    private native int _removeAllRights(int i);

    private native int _removeRights(int i, String str);

    private native int _saveRights(int i, DrmRights drmRights, String str, String str2);

    private native void _setListeners(int i, Object obj);

    public static void notify(Object thisReference, int uniqueId, int infoType, String message) {
        DrmManagerClient instance = (DrmManagerClient) ((WeakReference) thisReference).get();
        if (instance != null && instance.mInfoHandler != null) {
            instance.mInfoHandler.sendMessage(instance.mInfoHandler.obtainMessage(1, uniqueId, infoType, message));
        }
    }

    public DrmManagerClient(Context context) {
        this.mClosed = new AtomicBoolean();
        this.mCloseGuard = CloseGuard.get();
        this.mContext = context;
        Log.d(TAG, "create DrmManagerClient instance & create event threads.");
        createEventThreads();
        this.mUniqueId = _initialize();
        this.mCloseGuard.open("release");
    }

    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize DrmManagerClient instance.");
        try {
            this.mCloseGuard.warnIfOpen();
            close();
        } finally {
            super.finalize();
        }
    }

    public void close() {
        Log.d(TAG, "close DrmManagerClient instance & quit event/info thread.");
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            if (this.mEventHandler != null) {
                Log.v(TAG, "quit event handler thread.");
                this.mEventThread.quit();
                this.mEventThread = null;
            }
            if (this.mInfoHandler != null) {
                Log.v(TAG, "quit info handler thread.");
                this.mInfoThread.quit();
                this.mInfoThread = null;
            }
            this.mEventHandler = null;
            this.mInfoHandler = null;
            this.mOnEventListener = null;
            this.mOnInfoListener = null;
            this.mOnErrorListener = null;
            _release(this.mUniqueId);
        }
    }

    @Deprecated
    public void release() {
        close();
    }

    public synchronized void setOnInfoListener(OnInfoListener infoListener) {
        this.mOnInfoListener = infoListener;
        if (infoListener != null) {
            createListeners();
        }
    }

    public synchronized void setOnEventListener(OnEventListener eventListener) {
        this.mOnEventListener = eventListener;
        if (eventListener != null) {
            createListeners();
        }
    }

    public synchronized void setOnErrorListener(OnErrorListener errorListener) {
        this.mOnErrorListener = errorListener;
        if (errorListener != null) {
            createListeners();
        }
    }

    public String[] getAvailableDrmEngines() {
        DrmSupportInfo[] supportInfos = _getAllSupportInfo(this.mUniqueId);
        ArrayList<String> descriptions = new ArrayList();
        for (DrmSupportInfo descriprition : supportInfos) {
            descriptions.add(descriprition.getDescriprition());
        }
        return (String[]) descriptions.toArray(new String[descriptions.size()]);
    }

    public ContentValues getConstraints(String path, int action) {
        if (path != null && !path.equals("") && Action.isValid(action)) {
            return _getConstraints(this.mUniqueId, path, action);
        }
        throw new IllegalArgumentException("Given usage or path is invalid/null");
    }

    public ContentValues getMetadata(String path) {
        if (path != null && !path.equals("")) {
            return _getMetadata(this.mUniqueId, path);
        }
        throw new IllegalArgumentException("Given path is invalid/null");
    }

    public ContentValues getConstraints(Uri uri, int action) {
        if (uri != null && Uri.EMPTY != uri) {
            return getConstraints(convertUriToPath(uri), action);
        }
        throw new IllegalArgumentException("Uri should be non null");
    }

    public ContentValues getMetadata(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return getMetadata(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Uri should be non null");
    }

    public int saveRights(DrmRights drmRights, String rightsPath, String contentPath) throws IOException {
        if (drmRights == null || !drmRights.isValid()) {
            throw new IllegalArgumentException("Given drmRights or contentPath is not valid");
        }
        if (!(rightsPath == null || rightsPath.equals(""))) {
            DrmUtils.writeToFile(rightsPath, drmRights.getData());
        }
        return _saveRights(this.mUniqueId, drmRights, rightsPath, contentPath);
    }

    public void installDrmEngine(String engineFilePath) {
        if (engineFilePath == null || engineFilePath.equals("")) {
            throw new IllegalArgumentException("Given engineFilePath: " + engineFilePath + "is not valid");
        }
        _installDrmEngine(this.mUniqueId, engineFilePath);
    }

    public boolean canHandle(String path, String mimeType) {
        if ((path != null && !path.equals("")) || (mimeType != null && !mimeType.equals(""))) {
            return _canHandle(this.mUniqueId, path, mimeType);
        }
        throw new IllegalArgumentException("Path or the mimetype should be non null");
    }

    public boolean canHandle(Uri uri, String mimeType) {
        if ((uri != null && Uri.EMPTY != uri) || (mimeType != null && !mimeType.equals(""))) {
            return canHandle(convertUriToPath(uri), mimeType);
        }
        throw new IllegalArgumentException("Uri or the mimetype should be non null");
    }

    public int processDrmInfo(DrmInfo drmInfo) {
        if (drmInfo == null || !drmInfo.isValid()) {
            throw new IllegalArgumentException("Given drmInfo is invalid/null");
        } else if (this.mEventHandler == null) {
            return ERROR_UNKNOWN;
        } else {
            if (this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(1002, drmInfo))) {
                return 0;
            }
            return ERROR_UNKNOWN;
        }
    }

    public DrmInfo acquireDrmInfo(DrmInfoRequest drmInfoRequest) {
        if (drmInfoRequest != null && drmInfoRequest.isValid()) {
            return _acquireDrmInfo(this.mUniqueId, drmInfoRequest);
        }
        throw new IllegalArgumentException("Given drmInfoRequest is invalid/null");
    }

    public int acquireRights(DrmInfoRequest drmInfoRequest) {
        DrmInfo drmInfo = acquireDrmInfo(drmInfoRequest);
        if (drmInfo == null) {
            return ERROR_UNKNOWN;
        }
        return processDrmInfo(drmInfo);
    }

    public int getDrmObjectType(String path, String mimeType) {
        if ((path != null && !path.equals("")) || (mimeType != null && !mimeType.equals(""))) {
            return _getDrmObjectType(this.mUniqueId, path, mimeType);
        }
        throw new IllegalArgumentException("Path or the mimetype should be non null");
    }

    public int getDrmObjectType(Uri uri, String mimeType) {
        if ((uri == null || Uri.EMPTY == uri) && (mimeType == null || mimeType.equals(""))) {
            throw new IllegalArgumentException("Uri or the mimetype should be non null");
        }
        String path = "";
        try {
            path = convertUriToPath(uri);
        } catch (Exception e) {
            Log.w(TAG, "Given Uri could not be found in media store");
        }
        return getDrmObjectType(path, mimeType);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0064 A:{SYNTHETIC, Splitter: B:29:0x0064} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getOriginalMimeType(String path) {
        IOException ioe;
        Throwable th;
        if (path == null || path.equals("")) {
            throw new IllegalArgumentException("Given path should be non null");
        }
        String mime = null;
        FileInputStream is = null;
        FileDescriptor fd = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream is2 = new FileInputStream(file);
                try {
                    fd = is2.getFD();
                    is = is2;
                } catch (IOException e) {
                    ioe = e;
                    is = is2;
                    try {
                        Log.d(TAG, "getOriginalMimeType: File I/O exception: " + ioe.getMessage());
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e2) {
                            }
                        }
                        return mime;
                    } catch (Throwable th2) {
                        th = th2;
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e3) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    is = is2;
                    if (is != null) {
                    }
                    throw th;
                }
            }
            mime = _getOriginalMimeType(this.mUniqueId, path, fd);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
            }
        } catch (IOException e5) {
            ioe = e5;
        }
        return mime;
    }

    public String getOriginalMimeType(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return getOriginalMimeType(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int checkRightsStatus(String path) {
        return checkRightsStatus(path, 0);
    }

    public int checkRightsStatus(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return checkRightsStatus(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int checkRightsStatus(String path, int action) {
        if (path != null && !path.equals("") && Action.isValid(action)) {
            return _checkRightsStatus(this.mUniqueId, path, action);
        }
        throw new IllegalArgumentException("Given path or action is not valid");
    }

    public int checkRightsStatus(Uri uri, int action) {
        if (uri != null && Uri.EMPTY != uri) {
            return checkRightsStatus(convertUriToPath(uri), action);
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int removeRights(String path) {
        if (path != null && !path.equals("")) {
            return _removeRights(this.mUniqueId, path);
        }
        throw new IllegalArgumentException("Given path should be non null");
    }

    public int removeRights(Uri uri) {
        if (uri != null && Uri.EMPTY != uri) {
            return removeRights(convertUriToPath(uri));
        }
        throw new IllegalArgumentException("Given uri is not valid");
    }

    public int removeAllRights() {
        if (this.mEventHandler == null) {
            return ERROR_UNKNOWN;
        }
        if (this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(1001))) {
            return 0;
        }
        return ERROR_UNKNOWN;
    }

    public int openConvertSession(String mimeType) {
        if (mimeType != null && !mimeType.equals("")) {
            return _openConvertSession(this.mUniqueId, mimeType);
        }
        throw new IllegalArgumentException("Path or the mimeType should be non null");
    }

    public DrmConvertedStatus convertData(int convertId, byte[] inputData) {
        if (inputData != null && inputData.length > 0) {
            return _convertData(this.mUniqueId, convertId, inputData);
        }
        throw new IllegalArgumentException("Given inputData should be non null");
    }

    public DrmConvertedStatus closeConvertSession(int convertId) {
        return _closeConvertSession(this.mUniqueId, convertId);
    }

    private int getEventType(int infoType) {
        switch (infoType) {
            case 1:
            case 2:
            case 3:
                return 1002;
            default:
                return -1;
        }
    }

    private int getErrorType(int infoType) {
        switch (infoType) {
            case 1:
            case 2:
            case 3:
                return DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED;
            default:
                return -1;
        }
    }

    private String convertUriToPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("") || scheme.equals("file")) {
            return uri.getPath();
        }
        if (scheme.equals(IntentFilter.SCHEME_HTTP)) {
            return uri.toString();
        }
        if (scheme.equals("content")) {
            String[] projection = new String[1];
            projection[0] = "_data";
            Cursor cursor = null;
            try {
                cursor = this.mContext.getContentResolver().query(uri, projection, null, null, null);
                if (!(cursor == null || cursor.getCount() == 0)) {
                    if (cursor.moveToFirst()) {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                        if (cursor == null) {
                            return path;
                        }
                        cursor.close();
                        return path;
                    }
                }
                throw new IllegalArgumentException("Given Uri could not be found in media store");
            } catch (SQLiteException e) {
                throw new IllegalArgumentException("Given Uri is not formatted in a way so that it can be found in media store.");
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            throw new IllegalArgumentException("Given Uri scheme is not supported");
        }
    }

    private void createEventThreads() {
        if (this.mEventHandler == null && this.mInfoHandler == null) {
            Log.v(TAG, "create info handler thread.");
            this.mInfoThread = new HandlerThread("DrmManagerClient.InfoHandler");
            this.mInfoThread.start();
            this.mInfoHandler = new InfoHandler(this.mInfoThread.getLooper());
            Log.v(TAG, "create event handler thread.");
            this.mEventThread = new HandlerThread("DrmManagerClient.EventHandler");
            this.mEventThread.start();
            this.mEventHandler = new EventHandler(this.mEventThread.getLooper());
        }
    }

    private void createListeners() {
        _setListeners(this.mUniqueId, new WeakReference(this));
    }
}
