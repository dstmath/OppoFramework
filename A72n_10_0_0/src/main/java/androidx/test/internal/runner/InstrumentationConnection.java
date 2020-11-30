package androidx.test.internal.runner;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import androidx.test.internal.util.Checks;
import androidx.test.internal.util.LogUtil;
import androidx.test.internal.util.ParcelableIBinder;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.MonitoringInstrumentation;
import com.alibaba.fastjson.parser.JSONToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class InstrumentationConnection {
    static final String BUNDLE_BR_NEW_BINDER = "new_instrumentation_binder";
    private static final InstrumentationConnection DEFAULT_INSTANCE = new InstrumentationConnection(InstrumentationRegistry.getInstrumentation().getTargetContext());
    static final int MSG_ADD_CLIENTS_IN_BUNDLE = 6;
    static final int MSG_ADD_INSTRUMENTATION = 4;
    static final int MSG_REMOTE_CLEANUP_REQUEST = 10;
    private static MonitoringInstrumentation.ActivityFinisher activityFinisher;
    private static Instrumentation instrumentation;
    IncomingHandler incomingHandler;
    final BroadcastReceiver messengerReceiver = new MessengerReceiver();
    private Context targetContext;

    InstrumentationConnection(Context context) {
        this.targetContext = (Context) Checks.checkNotNull(context, "Context can't be null");
    }

    public static InstrumentationConnection getInstance() {
        return DEFAULT_INSTANCE;
    }

    public synchronized void init(Instrumentation instrumentation2, MonitoringInstrumentation.ActivityFinisher finisher) {
        LogUtil.logDebugWithProcess("InstrConnection", "init", new Object[0]);
        if (this.incomingHandler == null) {
            instrumentation = instrumentation2;
            activityFinisher = finisher;
            HandlerThread ht = new HandlerThread("InstrumentationConnectionThread");
            ht.start();
            this.incomingHandler = new IncomingHandler(ht.getLooper());
            Intent intent = new Intent("androidx.test.runner.InstrumentationConnection.event");
            Bundle bundle = new Bundle();
            bundle.putParcelable(BUNDLE_BR_NEW_BINDER, new ParcelableIBinder(this.incomingHandler.messengerHandler.getBinder()));
            intent.putExtra(BUNDLE_BR_NEW_BINDER, bundle);
            try {
                this.targetContext.sendBroadcast(intent);
                this.targetContext.registerReceiver(this.messengerReceiver, new IntentFilter("androidx.test.runner.InstrumentationConnection.event"));
            } catch (SecurityException e) {
                Log.i("InstrConnection", "Could not send broadcast or register receiver (isolatedProcess?)");
            }
        }
    }

    public synchronized void terminate() {
        LogUtil.logDebugWithProcess("InstrConnection", "Terminate is called", new Object[0]);
        if (this.incomingHandler != null) {
            this.incomingHandler.runSyncTask(new Callable<Void>() {
                /* class androidx.test.internal.runner.InstrumentationConnection.AnonymousClass1 */

                @Override // java.util.concurrent.Callable
                public Void call() {
                    InstrumentationConnection.this.incomingHandler.doDie();
                    return null;
                }
            });
            this.targetContext.unregisterReceiver(this.messengerReceiver);
            this.incomingHandler = null;
        }
    }

    public synchronized void requestRemoteInstancesActivityCleanup() {
        IncomingHandler incomingHandler2;
        Checks.checkState(this.incomingHandler != null, "Instrumentation Connection in not yet initialized");
        UUID uuid = UUID.randomUUID();
        CountDownLatch latch = new CountDownLatch(1);
        this.incomingHandler.associateLatch(uuid, latch);
        Message msg = this.incomingHandler.obtainMessage(10);
        msg.replyTo = this.incomingHandler.messengerHandler;
        Bundle bundle = msg.getData();
        bundle.putSerializable("instr_uuid", uuid);
        msg.setData(bundle);
        this.incomingHandler.sendMessage(msg);
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                String valueOf = String.valueOf(uuid);
                StringBuilder sb = new StringBuilder(60 + String.valueOf(valueOf).length());
                sb.append("Timed out while attempting to perform activity clean up for ");
                sb.append(valueOf);
                Log.w("InstrConnection", sb.toString());
            }
            incomingHandler2 = this.incomingHandler;
        } catch (InterruptedException e) {
            String valueOf2 = String.valueOf(uuid);
            StringBuilder sb2 = new StringBuilder(61 + String.valueOf(valueOf2).length());
            sb2.append("Interrupted while waiting for response from message with id: ");
            sb2.append(valueOf2);
            Log.e("InstrConnection", sb2.toString(), e);
            incomingHandler2 = this.incomingHandler;
        } catch (Throwable th) {
            this.incomingHandler.disassociateLatch(uuid);
            throw th;
        }
        incomingHandler2.disassociateLatch(uuid);
    }

    class MessengerReceiver extends BroadcastReceiver {
        MessengerReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            LogUtil.logDebugWithProcess("InstrConnection", "Broadcast received", new Object[0]);
            Bundle extras = intent.getBundleExtra(InstrumentationConnection.BUNDLE_BR_NEW_BINDER);
            if (extras == null) {
                Log.w("InstrConnection", "Broadcast intent doesn't contain any extras, ignoring it..");
                return;
            }
            ParcelableIBinder iBinder = (ParcelableIBinder) extras.getParcelable(InstrumentationConnection.BUNDLE_BR_NEW_BINDER);
            if (iBinder != null) {
                Messenger msgr = new Messenger(iBinder.getIBinder());
                Message msg = InstrumentationConnection.this.incomingHandler.obtainMessage(3);
                msg.replyTo = msgr;
                InstrumentationConnection.this.incomingHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class IncomingHandler extends Handler {
        private final Map<UUID, CountDownLatch> latches = new HashMap();
        Messenger messengerHandler = new Messenger(this);
        Set<Messenger> otherInstrumentations = new HashSet();
        Map<String, Set<Messenger>> typedClients = new HashMap();

        public IncomingHandler(Looper looper) {
            super(looper);
            if (Looper.getMainLooper() == looper || Looper.myLooper() == looper) {
                throw new IllegalStateException("This handler should not be using the main thread looper nor the instrumentation thread looper.");
            }
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_REMOTE_ADD_CLIENT)", new Object[0]);
                    registerClient(msg.getData().getString("instr_client_type"), (Messenger) msg.getData().getParcelable("instr_client_msgr"));
                    return;
                case 1:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_REMOTE_REMOVE_CLIENT)", new Object[0]);
                    unregisterClient(msg.getData().getString("instr_client_type"), msg.replyTo);
                    return;
                case 2:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_TERMINATE)", new Object[0]);
                    doDie();
                    return;
                case 3:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_HANDLE_INSTRUMENTATION_FROM_BROADCAST)", new Object[0]);
                    if (this.otherInstrumentations.add(msg.replyTo)) {
                        sendMessageWithReply(msg.replyTo, 4, null);
                        return;
                    } else {
                        Log.w("InstrConnection", "Broadcast with existing binder was received, ignoring it..");
                        return;
                    }
                case 4:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_ADD_INSTRUMENTATION)", new Object[0]);
                    if (this.otherInstrumentations.add(msg.replyTo)) {
                        if (!this.typedClients.isEmpty()) {
                            sendMessageWithReply(msg.replyTo, 6, null);
                        }
                        clientsRegistrationFromBundle(msg.getData(), true);
                        return;
                    }
                    Log.w("InstrConnection", "Message with existing binder was received, ignoring it..");
                    return;
                case 5:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_REMOVE_INSTRUMENTATION)", new Object[0]);
                    if (!this.otherInstrumentations.remove(msg.replyTo)) {
                        Log.w("InstrConnection", "Attempting to remove a non-existent binder!");
                        return;
                    }
                    return;
                case 6:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_ADD_CLIENTS_IN_BUNDLE)", new Object[0]);
                    clientsRegistrationFromBundle(msg.getData(), true);
                    return;
                case JSONToken.FALSE /* 7 */:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_REMOVE_CLIENTS_IN_BUNDLE)", new Object[0]);
                    clientsRegistrationFromBundle(msg.getData(), false);
                    return;
                case JSONToken.NULL /* 8 */:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_REG_CLIENT)", new Object[0]);
                    registerClient(msg.getData().getString("instr_client_type"), (Messenger) msg.getData().getParcelable("instr_client_msgr"));
                    sendMessageToOtherInstr(0, msg.getData());
                    return;
                case 9:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_UN_REG_CLIENT)", new Object[0]);
                    unregisterClient(msg.getData().getString("instr_client_type"), (Messenger) msg.getData().getParcelable("instr_client_msgr"));
                    sendMessageToOtherInstr(1, msg.getData());
                    return;
                case 10:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_REMOTE_CLEANUP_REQUEST)", new Object[0]);
                    if (this.otherInstrumentations.isEmpty()) {
                        Message m = obtainMessage(12);
                        m.setData(msg.getData());
                        sendMessage(m);
                        return;
                    }
                    sendMessageToOtherInstr(11, msg.getData());
                    return;
                case 11:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_PERFORM_CLEANUP)", new Object[0]);
                    InstrumentationConnection.instrumentation.runOnMainSync(InstrumentationConnection.activityFinisher);
                    sendMessageWithReply(msg.replyTo, 12, msg.getData());
                    return;
                case JSONToken.LBRACE /* 12 */:
                    LogUtil.logDebugWithProcess("InstrConnection", "handleMessage(MSG_PERFORM_CLEANUP_FINISHED)", new Object[0]);
                    notifyLatch((UUID) msg.getData().getSerializable("instr_uuid"));
                    return;
                default:
                    int i = msg.what;
                    StringBuilder sb = new StringBuilder(42);
                    sb.append("Unknown message code received: ");
                    sb.append(i);
                    Log.w("InstrConnection", sb.toString());
                    super.handleMessage(msg);
                    return;
            }
        }

        private void notifyLatch(UUID uuid) {
            if (uuid == null || !this.latches.containsKey(uuid)) {
                String valueOf = String.valueOf(uuid);
                StringBuilder sb = new StringBuilder(16 + String.valueOf(valueOf).length());
                sb.append("Latch not found ");
                sb.append(valueOf);
                Log.w("InstrConnection", sb.toString());
                return;
            }
            this.latches.get(uuid).countDown();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void associateLatch(final UUID latchId, final CountDownLatch latch) {
            runSyncTask(new Callable<Void>() {
                /* class androidx.test.internal.runner.InstrumentationConnection.IncomingHandler.AnonymousClass1 */

                @Override // java.util.concurrent.Callable
                public Void call() {
                    IncomingHandler.this.latches.put(latchId, latch);
                    return null;
                }
            });
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void disassociateLatch(final UUID latchId) {
            runSyncTask(new Callable<Void>() {
                /* class androidx.test.internal.runner.InstrumentationConnection.IncomingHandler.AnonymousClass2 */

                @Override // java.util.concurrent.Callable
                public Void call() {
                    IncomingHandler.this.latches.remove(latchId);
                    return null;
                }
            });
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private <T> T runSyncTask(Callable<T> task) {
            FutureTask<T> futureTask = new FutureTask<>(task);
            post(futureTask);
            try {
                return futureTask.get();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.getCause());
            } catch (ExecutionException e2) {
                throw new IllegalStateException(e2.getCause());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void doDie() {
            Log.i("InstrConnection", "terminating process");
            sendMessageToOtherInstr(5, null);
            this.otherInstrumentations.clear();
            this.typedClients.clear();
            LogUtil.logDebugWithProcess("InstrConnection", "quitting looper...", new Object[0]);
            getLooper().quit();
            LogUtil.logDebugWithProcess("InstrConnection", "finishing instrumentation...", new Object[0]);
            InstrumentationConnection.instrumentation.finish(0, null);
            Instrumentation unused = InstrumentationConnection.instrumentation = null;
            MonitoringInstrumentation.ActivityFinisher unused2 = InstrumentationConnection.activityFinisher = null;
        }

        private void sendMessageWithReply(Messenger toMessenger, int what, Bundle data) {
            StringBuilder sb = new StringBuilder(45);
            sb.append("sendMessageWithReply type: ");
            sb.append(what);
            sb.append(" called");
            LogUtil.logDebugWithProcess("InstrConnection", sb.toString(), new Object[0]);
            Message msg = obtainMessage(what);
            msg.replyTo = this.messengerHandler;
            if (data != null) {
                msg.setData(data);
            }
            if (!this.typedClients.isEmpty()) {
                Bundle clientsBundle = msg.getData();
                clientsBundle.putStringArrayList("instr_clients", new ArrayList<>(this.typedClients.keySet()));
                for (Map.Entry<String, Set<Messenger>> entry : this.typedClients.entrySet()) {
                    clientsBundle.putParcelableArray(String.valueOf(entry.getKey()), (Messenger[]) entry.getValue().toArray(new Messenger[entry.getValue().size()]));
                }
                msg.setData(clientsBundle);
            }
            try {
                toMessenger.send(msg);
            } catch (RemoteException e) {
                Log.w("InstrConnection", "The remote process is terminated unexpectedly", e);
                instrBinderDied(toMessenger);
            }
        }

        private void sendMessageToOtherInstr(int what, Bundle data) {
            LogUtil.logDebugWithProcess("InstrConnection", "sendMessageToOtherInstr() called with: what = [%s], data = [%s]", Integer.valueOf(what), data);
            for (Messenger otherInstr : this.otherInstrumentations) {
                sendMessageWithReply(otherInstr, what, data);
            }
        }

        private void clientsRegistrationFromBundle(Bundle clientsBundle, boolean shouldRegister) {
            LogUtil.logDebugWithProcess("InstrConnection", "clientsRegistrationFromBundle called", new Object[0]);
            if (clientsBundle == null) {
                Log.w("InstrConnection", "The client bundle is null, ignoring...");
                return;
            }
            ArrayList<String> clientTypes = clientsBundle.getStringArrayList("instr_clients");
            if (clientTypes == null) {
                Log.w("InstrConnection", "No clients found in the given bundle");
                return;
            }
            Iterator<String> it = clientTypes.iterator();
            while (it.hasNext()) {
                String type = it.next();
                Parcelable[] clientArray = clientsBundle.getParcelableArray(String.valueOf(type));
                if (clientArray != null) {
                    for (Parcelable client : clientArray) {
                        if (shouldRegister) {
                            registerClient(type, (Messenger) client);
                        } else {
                            unregisterClient(type, (Messenger) client);
                        }
                    }
                }
            }
        }

        private void registerClient(String type, Messenger client) {
            LogUtil.logDebugWithProcess("InstrConnection", "registerClient called with type = [%s] client = [%s]", type, client);
            Checks.checkNotNull(type, "type cannot be null!");
            Checks.checkNotNull(client, "client cannot be null!");
            Set<Messenger> clientSet = this.typedClients.get(type);
            if (clientSet == null) {
                Set<Messenger> clientSet2 = new HashSet<>();
                clientSet2.add(client);
                this.typedClients.put(type, clientSet2);
                return;
            }
            clientSet.add(client);
        }

        private void unregisterClient(String type, Messenger client) {
            LogUtil.logDebugWithProcess("InstrConnection", "unregisterClient called with type = [%s] client = [%s]", type, client);
            Checks.checkNotNull(type, "type cannot be null!");
            Checks.checkNotNull(client, "client cannot be null!");
            if (!this.typedClients.containsKey(type)) {
                String valueOf = String.valueOf(type);
                Log.w("InstrConnection", valueOf.length() != 0 ? "There are no registered clients for type: ".concat(valueOf) : new String("There are no registered clients for type: "));
                return;
            }
            Set<Messenger> clientSet = this.typedClients.get(type);
            if (!clientSet.contains(client)) {
                StringBuilder sb = new StringBuilder(78 + String.valueOf(type).length());
                sb.append("Could not unregister client for type ");
                sb.append(type);
                sb.append(" because it doesn't seem to be registered");
                Log.w("InstrConnection", sb.toString());
                return;
            }
            clientSet.remove(client);
            if (clientSet.isEmpty()) {
                this.typedClients.remove(type);
            }
        }

        private void instrBinderDied(Messenger instrMessenger) {
            Message msg = obtainMessage(5);
            msg.replyTo = instrMessenger;
            sendMessage(msg);
        }
    }
}
