package com.android.server.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.IRemoteDisplayCallback.Stub;
import android.media.IRemoteDisplayProvider;
import android.media.RemoteDisplayState;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Objects;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
final class RemoteDisplayProviderProxy implements ServiceConnection {
    private static final boolean DEBUG = false;
    private static final String TAG = "RemoteDisplayProvider";
    private Connection mActiveConnection;
    private boolean mBound;
    private final ComponentName mComponentName;
    private boolean mConnectionReady;
    private final Context mContext;
    private int mDiscoveryMode;
    private RemoteDisplayState mDisplayState;
    private Callback mDisplayStateCallback;
    private final Runnable mDisplayStateChanged;
    private final Handler mHandler;
    private boolean mRunning;
    private boolean mScheduledDisplayStateChangedCallback;
    private String mSelectedDisplayId;
    private final int mUserId;

    public interface Callback {
        void onDisplayStateChanged(RemoteDisplayProviderProxy remoteDisplayProviderProxy, RemoteDisplayState remoteDisplayState);
    }

    private final class Connection implements DeathRecipient {
        private final ProviderCallback mCallback = new ProviderCallback(this);
        private final IRemoteDisplayProvider mProvider;

        public Connection(IRemoteDisplayProvider provider) {
            this.mProvider = provider;
        }

        public boolean register() {
            try {
                this.mProvider.asBinder().linkToDeath(this, 0);
                this.mProvider.setCallback(this.mCallback);
                RemoteDisplayProviderProxy.this.mHandler.post(new Runnable() {
                    public void run() {
                        RemoteDisplayProviderProxy.this.onConnectionReady(Connection.this);
                    }
                });
                return true;
            } catch (RemoteException e) {
                binderDied();
                return false;
            }
        }

        public void dispose() {
            this.mProvider.asBinder().unlinkToDeath(this, 0);
            this.mCallback.dispose();
        }

        public void setDiscoveryMode(int mode) {
            try {
                this.mProvider.setDiscoveryMode(mode);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to set discovery mode.", ex);
            }
        }

        public void connect(String id) {
            try {
                this.mProvider.connect(id);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to connect to display.", ex);
            }
        }

        public void disconnect(String id) {
            try {
                this.mProvider.disconnect(id);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to disconnect from display.", ex);
            }
        }

        public void setVolume(String id, int volume) {
            try {
                this.mProvider.setVolume(id, volume);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to set display volume.", ex);
            }
        }

        public void adjustVolume(String id, int volume) {
            try {
                this.mProvider.adjustVolume(id, volume);
            } catch (RemoteException ex) {
                Slog.e(RemoteDisplayProviderProxy.TAG, "Failed to deliver request to adjust display volume.", ex);
            }
        }

        public void binderDied() {
            RemoteDisplayProviderProxy.this.mHandler.post(new Runnable() {
                public void run() {
                    RemoteDisplayProviderProxy.this.onConnectionDied(Connection.this);
                }
            });
        }

        void postStateChanged(final RemoteDisplayState state) {
            RemoteDisplayProviderProxy.this.mHandler.post(new Runnable() {
                public void run() {
                    RemoteDisplayProviderProxy.this.onDisplayStateChanged(Connection.this, state);
                }
            });
        }
    }

    private static final class ProviderCallback extends Stub {
        private final WeakReference<Connection> mConnectionRef;

        public ProviderCallback(Connection connection) {
            this.mConnectionRef = new WeakReference(connection);
        }

        public void dispose() {
            this.mConnectionRef.clear();
        }

        public void onStateChanged(RemoteDisplayState state) throws RemoteException {
            Connection connection = (Connection) this.mConnectionRef.get();
            if (connection != null) {
                connection.postStateChanged(state);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.media.RemoteDisplayProviderProxy.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.media.RemoteDisplayProviderProxy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.media.RemoteDisplayProviderProxy.<clinit>():void");
    }

    public RemoteDisplayProviderProxy(Context context, ComponentName componentName, int userId) {
        this.mDisplayStateChanged = new Runnable() {
            public void run() {
                RemoteDisplayProviderProxy.this.mScheduledDisplayStateChangedCallback = false;
                if (RemoteDisplayProviderProxy.this.mDisplayStateCallback != null) {
                    RemoteDisplayProviderProxy.this.mDisplayStateCallback.onDisplayStateChanged(RemoteDisplayProviderProxy.this, RemoteDisplayProviderProxy.this.mDisplayState);
                }
            }
        };
        this.mContext = context;
        this.mComponentName = componentName;
        this.mUserId = userId;
        this.mHandler = new Handler();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "Proxy");
        pw.println(prefix + "  mUserId=" + this.mUserId);
        pw.println(prefix + "  mRunning=" + this.mRunning);
        pw.println(prefix + "  mBound=" + this.mBound);
        pw.println(prefix + "  mActiveConnection=" + this.mActiveConnection);
        pw.println(prefix + "  mConnectionReady=" + this.mConnectionReady);
        pw.println(prefix + "  mDiscoveryMode=" + this.mDiscoveryMode);
        pw.println(prefix + "  mSelectedDisplayId=" + this.mSelectedDisplayId);
        pw.println(prefix + "  mDisplayState=" + this.mDisplayState);
    }

    public void setCallback(Callback callback) {
        this.mDisplayStateCallback = callback;
    }

    public RemoteDisplayState getDisplayState() {
        return this.mDisplayState;
    }

    public void setDiscoveryMode(int mode) {
        if (this.mDiscoveryMode != mode) {
            this.mDiscoveryMode = mode;
            if (this.mConnectionReady) {
                this.mActiveConnection.setDiscoveryMode(mode);
            }
            updateBinding();
        }
    }

    public void setSelectedDisplay(String id) {
        if (!Objects.equals(this.mSelectedDisplayId, id)) {
            if (this.mConnectionReady && this.mSelectedDisplayId != null) {
                this.mActiveConnection.disconnect(this.mSelectedDisplayId);
            }
            this.mSelectedDisplayId = id;
            if (this.mConnectionReady && id != null) {
                this.mActiveConnection.connect(id);
            }
            updateBinding();
        }
    }

    public void setDisplayVolume(int volume) {
        if (this.mConnectionReady && this.mSelectedDisplayId != null) {
            this.mActiveConnection.setVolume(this.mSelectedDisplayId, volume);
        }
    }

    public void adjustDisplayVolume(int delta) {
        if (this.mConnectionReady && this.mSelectedDisplayId != null) {
            this.mActiveConnection.adjustVolume(this.mSelectedDisplayId, delta);
        }
    }

    public boolean hasComponentName(String packageName, String className) {
        if (this.mComponentName.getPackageName().equals(packageName)) {
            return this.mComponentName.getClassName().equals(className);
        }
        return false;
    }

    public String getFlattenedComponentName() {
        return this.mComponentName.flattenToShortString();
    }

    public void start() {
        if (!this.mRunning) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Starting");
            }
            this.mRunning = true;
            updateBinding();
        }
    }

    public void stop() {
        if (this.mRunning) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Stopping");
            }
            this.mRunning = false;
            updateBinding();
        }
    }

    public void rebindIfDisconnected() {
        if (this.mActiveConnection == null && shouldBind()) {
            unbind();
            bind();
        }
    }

    private void updateBinding() {
        if (shouldBind()) {
            bind();
        } else {
            unbind();
        }
    }

    private boolean shouldBind() {
        if (!this.mRunning || (this.mDiscoveryMode == 0 && this.mSelectedDisplayId == null)) {
            return false;
        }
        return true;
    }

    private void bind() {
        if (!this.mBound) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Binding");
            }
            Intent service = new Intent("com.android.media.remotedisplay.RemoteDisplayProvider");
            service.setComponent(this.mComponentName);
            try {
                this.mBound = this.mContext.bindServiceAsUser(service, this, 67108865, new UserHandle(this.mUserId));
                if (!this.mBound && DEBUG) {
                    Slog.d(TAG, this + ": Bind failed");
                }
            } catch (SecurityException ex) {
                if (DEBUG) {
                    Slog.d(TAG, this + ": Bind failed", ex);
                }
            }
        }
    }

    private void unbind() {
        if (this.mBound) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Unbinding");
            }
            this.mBound = false;
            disconnect();
            this.mContext.unbindService(this);
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        if (DEBUG) {
            Slog.d(TAG, this + ": Connected");
        }
        if (this.mBound) {
            disconnect();
            IRemoteDisplayProvider provider = IRemoteDisplayProvider.Stub.asInterface(service);
            if (provider != null) {
                Connection connection = new Connection(provider);
                if (connection.register()) {
                    this.mActiveConnection = connection;
                    return;
                } else if (DEBUG) {
                    Slog.d(TAG, this + ": Registration failed");
                    return;
                } else {
                    return;
                }
            }
            Slog.e(TAG, this + ": Service returned invalid remote display provider binder");
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        if (DEBUG) {
            Slog.d(TAG, this + ": Service disconnected");
        }
        disconnect();
    }

    private void onConnectionReady(Connection connection) {
        if (this.mActiveConnection == connection) {
            this.mConnectionReady = true;
            if (this.mDiscoveryMode != 0) {
                this.mActiveConnection.setDiscoveryMode(this.mDiscoveryMode);
            }
            if (this.mSelectedDisplayId != null) {
                this.mActiveConnection.connect(this.mSelectedDisplayId);
            }
        }
    }

    private void onConnectionDied(Connection connection) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Slog.d(TAG, this + ": Service connection died");
            }
            disconnect();
        }
    }

    private void onDisplayStateChanged(Connection connection, RemoteDisplayState state) {
        if (this.mActiveConnection == connection) {
            if (DEBUG) {
                Slog.d(TAG, this + ": State changed, state=" + state);
            }
            setDisplayState(state);
        }
    }

    private void disconnect() {
        if (this.mActiveConnection != null) {
            if (this.mSelectedDisplayId != null) {
                this.mActiveConnection.disconnect(this.mSelectedDisplayId);
            }
            this.mConnectionReady = false;
            this.mActiveConnection.dispose();
            this.mActiveConnection = null;
            setDisplayState(null);
        }
    }

    private void setDisplayState(RemoteDisplayState state) {
        if (!Objects.equals(this.mDisplayState, state)) {
            this.mDisplayState = state;
            if (!this.mScheduledDisplayStateChangedCallback) {
                this.mScheduledDisplayStateChangedCallback = true;
                this.mHandler.post(this.mDisplayStateChanged);
            }
        }
    }

    public String toString() {
        return "Service connection " + this.mComponentName.flattenToShortString();
    }
}
