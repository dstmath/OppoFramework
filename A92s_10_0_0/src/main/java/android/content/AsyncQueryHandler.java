package android.content;

import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.lang.ref.WeakReference;

public abstract class AsyncQueryHandler extends Handler {
    private static final int EVENT_ARG_DELETE = 4;
    private static final int EVENT_ARG_INSERT = 2;
    private static final int EVENT_ARG_QUERY = 1;
    private static final int EVENT_ARG_UPDATE = 3;
    private static final String TAG = "AsyncQuery";
    private static final boolean localLOGV = false;
    private static Looper sLooper = null;
    final WeakReference<ContentResolver> mResolver;
    private Handler mWorkerThreadHandler;

    protected static final class WorkerArgs {
        public Object cookie;
        public Handler handler;
        public String orderBy;
        public String[] projection;
        public Object result;
        public String selection;
        public String[] selectionArgs;
        public Uri uri;
        public ContentValues values;

        protected WorkerArgs() {
        }
    }

    protected class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Cursor cursor;
            ContentResolver resolver = AsyncQueryHandler.this.mResolver.get();
            if (resolver != null) {
                WorkerArgs args = (WorkerArgs) msg.obj;
                int token = msg.what;
                int event = msg.arg1;
                if (event == 1) {
                    try {
                        cursor = resolver.query(args.uri, args.projection, args.selection, args.selectionArgs, args.orderBy);
                        if (cursor != null) {
                            cursor.getCount();
                        }
                    } catch (Exception e) {
                        Log.w(AsyncQueryHandler.TAG, "Exception thrown during handling EVENT_ARG_QUERY", e);
                        cursor = null;
                    }
                    args.result = cursor;
                } else if (event == 2) {
                    args.result = resolver.insert(args.uri, args.values);
                } else if (event == 3) {
                    args.result = Integer.valueOf(resolver.update(args.uri, args.values, args.selection, args.selectionArgs));
                } else if (event == 4) {
                    args.result = Integer.valueOf(resolver.delete(args.uri, args.selection, args.selectionArgs));
                }
                Message reply = args.handler.obtainMessage(token);
                reply.obj = args;
                reply.arg1 = msg.arg1;
                reply.sendToTarget();
            }
        }
    }

    public AsyncQueryHandler(ContentResolver cr) {
        this.mResolver = new WeakReference<>(cr);
        synchronized (AsyncQueryHandler.class) {
            if (sLooper == null) {
                HandlerThread thread = new HandlerThread("AsyncQueryWorker");
                thread.start();
                sLooper = thread.getLooper();
            }
        }
        this.mWorkerThreadHandler = createHandler(sLooper);
    }

    /* access modifiers changed from: protected */
    public Handler createHandler(Looper looper) {
        return new WorkerHandler(looper);
    }

    public void startQuery(int token, Object cookie, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        Message msg = this.mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = 1;
        WorkerArgs args = new WorkerArgs();
        args.handler = this;
        args.uri = uri;
        args.projection = projection;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        args.orderBy = orderBy;
        args.cookie = cookie;
        msg.obj = args;
        this.mWorkerThreadHandler.sendMessage(msg);
    }

    public final void cancelOperation(int token) {
        this.mWorkerThreadHandler.removeMessages(token);
    }

    public final void startInsert(int token, Object cookie, Uri uri, ContentValues initialValues) {
        Message msg = this.mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = 2;
        WorkerArgs args = new WorkerArgs();
        args.handler = this;
        args.uri = uri;
        args.cookie = cookie;
        args.values = initialValues;
        msg.obj = args;
        this.mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startUpdate(int token, Object cookie, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Message msg = this.mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = 3;
        WorkerArgs args = new WorkerArgs();
        args.handler = this;
        args.uri = uri;
        args.cookie = cookie;
        args.values = values;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        msg.obj = args;
        this.mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startDelete(int token, Object cookie, Uri uri, String selection, String[] selectionArgs) {
        Message msg = this.mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = 4;
        WorkerArgs args = new WorkerArgs();
        args.handler = this;
        args.uri = uri;
        args.cookie = cookie;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        msg.obj = args;
        this.mWorkerThreadHandler.sendMessage(msg);
    }

    /* access modifiers changed from: protected */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
    }

    /* access modifiers changed from: protected */
    public void onInsertComplete(int token, Object cookie, Uri uri) {
    }

    /* access modifiers changed from: protected */
    public void onUpdateComplete(int token, Object cookie, int result) {
    }

    /* access modifiers changed from: protected */
    public void onDeleteComplete(int token, Object cookie, int result) {
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        WorkerArgs args = (WorkerArgs) msg.obj;
        int token = msg.what;
        int event = msg.arg1;
        if (event == 1) {
            onQueryComplete(token, args.cookie, (Cursor) args.result);
        } else if (event == 2) {
            onInsertComplete(token, args.cookie, (Uri) args.result);
        } else if (event == 3) {
            onUpdateComplete(token, args.cookie, ((Integer) args.result).intValue());
        } else if (event == 4) {
            onDeleteComplete(token, args.cookie, ((Integer) args.result).intValue());
        }
    }
}
