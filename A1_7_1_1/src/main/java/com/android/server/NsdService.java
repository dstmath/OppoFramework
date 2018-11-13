package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.nsd.DnsSdTxtRecord;
import android.net.nsd.INsdManager.Stub;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Message;
import android.os.Messenger;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.oppo.IElsaManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

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
public class NsdService extends Stub {
    private static final int BASE = 393216;
    private static final int CMD_TO_STRING_COUNT = 19;
    private static final boolean DBG = true;
    private static final String MDNS_TAG = "mDnsConnector";
    private static final String TAG = "NsdService";
    private static String[] sCmdToString;
    private int INVALID_ID;
    private HashMap<Messenger, ClientInfo> mClients;
    private ContentResolver mContentResolver;
    private Context mContext;
    private SparseArray<ClientInfo> mIdToClientInfoMap;
    private NativeDaemonConnector mNativeConnector;
    private final CountDownLatch mNativeDaemonConnected;
    private NsdStateMachine mNsdStateMachine;
    private AsyncChannel mReplyChannel;
    private int mUniqueId;

    private class ClientInfo {
        private static final int MAX_LIMIT = 10;
        private final AsyncChannel mChannel;
        private SparseArray<Integer> mClientIds;
        private SparseArray<Integer> mClientRequests;
        private final Messenger mMessenger;
        private NsdServiceInfo mResolvedService;

        /* synthetic */ ClientInfo(NsdService this$0, AsyncChannel c, Messenger m, ClientInfo clientInfo) {
            this(c, m);
        }

        private ClientInfo(AsyncChannel c, Messenger m) {
            this.mClientIds = new SparseArray();
            this.mClientRequests = new SparseArray();
            this.mChannel = c;
            this.mMessenger = m;
            Slog.d(NsdService.TAG, "New client, channel: " + c + " messenger: " + m);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("mChannel ").append(this.mChannel).append("\n");
            sb.append("mMessenger ").append(this.mMessenger).append("\n");
            sb.append("mResolvedService ").append(this.mResolvedService).append("\n");
            for (int i = 0; i < this.mClientIds.size(); i++) {
                int clientID = this.mClientIds.keyAt(i);
                sb.append("clientId ").append(clientID).append(" mDnsId ").append(this.mClientIds.valueAt(i)).append(" type ").append(this.mClientRequests.get(clientID)).append("\n");
            }
            return sb.toString();
        }

        private void expungeAllRequests() {
            for (int i = 0; i < this.mClientIds.size(); i++) {
                int clientId = this.mClientIds.keyAt(i);
                int globalId = ((Integer) this.mClientIds.valueAt(i)).intValue();
                NsdService.this.mIdToClientInfoMap.remove(globalId);
                Slog.d(NsdService.TAG, "Terminating client-ID " + clientId + " global-ID " + globalId + " type " + this.mClientRequests.get(clientId));
                switch (((Integer) this.mClientRequests.get(clientId)).intValue()) {
                    case 393217:
                        NsdService.this.stopServiceDiscovery(globalId);
                        break;
                    case 393225:
                        NsdService.this.unregisterService(globalId);
                        break;
                    case 393234:
                        NsdService.this.stopResolveService(globalId);
                        break;
                    default:
                        break;
                }
            }
            this.mClientIds.clear();
            this.mClientRequests.clear();
        }

        private int getClientId(int globalId) {
            int nSize = this.mClientIds.size();
            for (int i = 0; i < nSize; i++) {
                if (globalId == ((Integer) this.mClientIds.valueAt(i)).intValue()) {
                    return this.mClientIds.keyAt(i);
                }
            }
            return -1;
        }
    }

    class NativeCallbackReceiver implements INativeDaemonConnectorCallbacks {
        NativeCallbackReceiver() {
        }

        public void onDaemonConnected() {
            NsdService.this.mNativeDaemonConnected.countDown();
        }

        public boolean onCheckHoldWakeLock(int code) {
            return false;
        }

        public boolean onEvent(int code, String raw, String[] cooked) {
            NsdService.this.mNsdStateMachine.sendMessage(393242, new NativeEvent(code, raw, cooked));
            return true;
        }
    }

    private class NativeEvent {
        final int code;
        final String[] cooked;
        final String raw;

        NativeEvent(int code, String raw, String[] cooked) {
            this.code = code;
            this.raw = raw;
            this.cooked = cooked;
        }
    }

    class NativeResponseCode {
        public static final int SERVICE_DISCOVERY_FAILED = 602;
        public static final int SERVICE_FOUND = 603;
        public static final int SERVICE_GET_ADDR_FAILED = 611;
        public static final int SERVICE_GET_ADDR_SUCCESS = 612;
        public static final int SERVICE_LOST = 604;
        public static final int SERVICE_REGISTERED = 606;
        public static final int SERVICE_REGISTRATION_FAILED = 605;
        public static final int SERVICE_RESOLUTION_FAILED = 607;
        public static final int SERVICE_RESOLVED = 608;
        public static final int SERVICE_UPDATED = 609;
        public static final int SERVICE_UPDATE_FAILED = 610;

        NativeResponseCode() {
        }
    }

    private class NsdStateMachine extends StateMachine {
        private final DefaultState mDefaultState = new DefaultState();
        private final DisabledState mDisabledState = new DisabledState();
        private final EnabledState mEnabledState = new EnabledState();

        class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message msg) {
                Slog.i(NsdService.TAG, "[DefaultState]  processMessage what=" + msg.what);
                switch (msg.what) {
                    case 69632:
                        if (msg.arg1 != 0) {
                            Slog.e(NsdService.TAG, "Client connection failure, error=" + msg.arg1);
                            break;
                        }
                        AsyncChannel c = msg.obj;
                        Slog.d(NsdService.TAG, "New client listening to asynchronous messages");
                        c.sendMessage(69634);
                        if (((ClientInfo) NsdService.this.mClients.get(msg.replyTo)) != null) {
                            for (ClientInfo client : NsdService.this.mClients.values()) {
                                Slog.e(NsdService.TAG, "Client Info:" + client);
                            }
                            Slog.wtf(NsdService.TAG, "cInfo exist");
                        }
                        NsdService.this.mClients.put(msg.replyTo, new ClientInfo(NsdService.this, c, msg.replyTo, null));
                        break;
                    case 69633:
                        new AsyncChannel().connect(NsdService.this.mContext, NsdStateMachine.this.getHandler(), msg.replyTo);
                        break;
                    case 69636:
                        switch (msg.arg1) {
                            case 2:
                                Slog.e(NsdService.TAG, "Send failed, client connection lost");
                                break;
                            case 4:
                                Slog.d(NsdService.TAG, "Client disconnected");
                                break;
                            default:
                                Slog.d(NsdService.TAG, "Client connection lost with reason: " + msg.arg1);
                                break;
                        }
                        ClientInfo cInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (cInfo != null) {
                            cInfo.expungeAllRequests();
                            NsdService.this.mClients.remove(msg.replyTo);
                        }
                        if (NsdService.this.mClients.size() == 0) {
                            NsdService.this.stopMDnsDaemon();
                            break;
                        }
                        break;
                    case 393217:
                        NsdService.this.replyToMessage(msg, 393219, 0);
                        break;
                    case 393222:
                        NsdService.this.replyToMessage(msg, 393223, 0);
                        break;
                    case 393225:
                        NsdService.this.replyToMessage(msg, 393226, 0);
                        break;
                    case 393228:
                        NsdService.this.replyToMessage(msg, 393229, 0);
                        break;
                    case 393234:
                        NsdService.this.replyToMessage(msg, 393235, 0);
                        break;
                    default:
                        Slog.e(NsdService.TAG, "Unhandled " + msg);
                        return false;
                }
                return true;
            }
        }

        class DisabledState extends State {
            DisabledState() {
            }

            public void enter() {
                NsdService.this.sendNsdStateChangeBroadcast(false);
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 393240:
                        NsdStateMachine.this.transitionTo(NsdStateMachine.this.mEnabledState);
                        return true;
                    default:
                        return false;
                }
            }
        }

        class EnabledState extends State {
            EnabledState() {
            }

            public void enter() {
                NsdService.this.sendNsdStateChangeBroadcast(true);
                if (NsdService.this.mClients.size() > 0) {
                    NsdService.this.startMDnsDaemon();
                }
            }

            public void exit() {
                if (NsdService.this.mClients.size() > 0) {
                    NsdService.this.stopMDnsDaemon();
                }
            }

            private boolean requestLimitReached(ClientInfo clientInfo) {
                if (clientInfo.mClientIds.size() < 10) {
                    return false;
                }
                Slog.d(NsdService.TAG, "Exceeded max outstanding requests " + clientInfo);
                return true;
            }

            private void storeRequestMap(int clientId, int globalId, ClientInfo clientInfo, int what) {
                clientInfo.mClientIds.put(clientId, Integer.valueOf(globalId));
                clientInfo.mClientRequests.put(clientId, Integer.valueOf(what));
                NsdService.this.mIdToClientInfoMap.put(globalId, clientInfo);
            }

            private void removeRequestMap(int clientId, int globalId, ClientInfo clientInfo) {
                clientInfo.mClientIds.remove(clientId);
                clientInfo.mClientRequests.remove(clientId);
                NsdService.this.mIdToClientInfoMap.remove(globalId);
            }

            public boolean processMessage(Message msg) {
                Slog.i(NsdService.TAG, "[EnabledState]  processMessage what=" + msg.what);
                NsdServiceInfo servInfo;
                ClientInfo clientInfo;
                int id;
                switch (msg.what) {
                    case 69632:
                        if (msg.arg1 == 0 && NsdService.this.mClients.size() == 0) {
                            NsdService.this.startMDnsDaemon();
                        }
                        return false;
                    case 69636:
                        return false;
                    case 393217:
                        Slog.d(NsdService.TAG, "Discover services");
                        servInfo = msg.obj;
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (requestLimitReached(clientInfo)) {
                            NsdService.this.replyToMessage(msg, 393219, 4);
                            return true;
                        }
                        id = NsdService.this.getUniqueId();
                        if (NsdService.this.discoverServices(id, servInfo.getServiceType())) {
                            Slog.d(NsdService.TAG, "Discover " + msg.arg2 + " " + id + servInfo.getServiceType());
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            NsdService.this.replyToMessage(msg, 393218, (Object) servInfo);
                            return true;
                        }
                        NsdService.this.stopServiceDiscovery(id);
                        NsdService.this.replyToMessage(msg, 393219, 0);
                        return true;
                    case 393222:
                        Slog.d(NsdService.TAG, "Stop service discovery");
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        try {
                            id = ((Integer) clientInfo.mClientIds.get(msg.arg2)).intValue();
                            removeRequestMap(msg.arg2, id, clientInfo);
                            if (NsdService.this.stopServiceDiscovery(id)) {
                                NsdService.this.replyToMessage(msg, 393224);
                                return true;
                            }
                            NsdService.this.replyToMessage(msg, 393223, 0);
                            return true;
                        } catch (NullPointerException e) {
                            NsdService.this.replyToMessage(msg, 393223, 0);
                            return true;
                        }
                    case 393225:
                        Slog.d(NsdService.TAG, "Register service");
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (requestLimitReached(clientInfo)) {
                            NsdService.this.replyToMessage(msg, 393226, 4);
                            return true;
                        }
                        id = NsdService.this.getUniqueId();
                        if (NsdService.this.registerService(id, (NsdServiceInfo) msg.obj)) {
                            Slog.d(NsdService.TAG, "Register " + msg.arg2 + " " + id);
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            return true;
                        }
                        NsdService.this.unregisterService(id);
                        NsdService.this.replyToMessage(msg, 393226, 0);
                        return true;
                    case 393228:
                        Slog.d(NsdService.TAG, "unregister service");
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        try {
                            id = ((Integer) clientInfo.mClientIds.get(msg.arg2)).intValue();
                            removeRequestMap(msg.arg2, id, clientInfo);
                            if (NsdService.this.unregisterService(id)) {
                                NsdService.this.replyToMessage(msg, 393230);
                                return true;
                            }
                            NsdService.this.replyToMessage(msg, 393229, 0);
                            return true;
                        } catch (NullPointerException e2) {
                            NsdService.this.replyToMessage(msg, 393229, 0);
                            return true;
                        }
                    case 393234:
                        Slog.d(NsdService.TAG, "Resolve service");
                        servInfo = (NsdServiceInfo) msg.obj;
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (clientInfo.mResolvedService != null) {
                            NsdService.this.replyToMessage(msg, 393235, 3);
                            return true;
                        }
                        id = NsdService.this.getUniqueId();
                        if (NsdService.this.resolveService(id, servInfo)) {
                            clientInfo.mResolvedService = new NsdServiceInfo();
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            return true;
                        }
                        NsdService.this.replyToMessage(msg, 393235, 0);
                        return true;
                    case 393241:
                        NsdStateMachine.this.transitionTo(NsdStateMachine.this.mDisabledState);
                        return true;
                    case 393242:
                        NativeEvent event = msg.obj;
                        if (handleNativeEvent(event.code, event.raw, event.cooked)) {
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }
            }

            private boolean handleNativeEvent(int code, String raw, String[] cooked) {
                boolean handled = true;
                int id = Integer.parseInt(cooked[1]);
                ClientInfo clientInfo = (ClientInfo) NsdService.this.mIdToClientInfoMap.get(id);
                if (clientInfo == null) {
                    Slog.e(NsdService.TAG, "Unique id with no client mapping: " + id);
                    return false;
                }
                int clientId = clientInfo.getClientId(id);
                if (clientId < 0) {
                    Slog.d(NsdService.TAG, "Notification for a listener that is no longer active: " + id);
                    return false;
                }
                switch (code) {
                    case NativeResponseCode.SERVICE_DISCOVERY_FAILED /*602*/:
                        Slog.d(NsdService.TAG, "SERVICE_DISC_FAILED Raw: " + raw);
                        clientInfo.mChannel.sendMessage(393219, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_FOUND /*603*/:
                        Slog.d(NsdService.TAG, "SERVICE_FOUND Raw: " + raw);
                        clientInfo.mChannel.sendMessage(393220, 0, clientId, new NsdServiceInfo(cooked[2], cooked[3]));
                        break;
                    case NativeResponseCode.SERVICE_LOST /*604*/:
                        Slog.d(NsdService.TAG, "SERVICE_LOST Raw: " + raw);
                        clientInfo.mChannel.sendMessage(393221, 0, clientId, new NsdServiceInfo(cooked[2], cooked[3]));
                        break;
                    case NativeResponseCode.SERVICE_REGISTRATION_FAILED /*605*/:
                        Slog.d(NsdService.TAG, "SERVICE_REGISTER_FAILED Raw: " + raw);
                        clientInfo.mChannel.sendMessage(393226, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_REGISTERED /*606*/:
                        Slog.d(NsdService.TAG, "SERVICE_REGISTERED Raw: " + raw);
                        clientInfo.mChannel.sendMessage(393227, id, clientId, new NsdServiceInfo(cooked[2], null));
                        break;
                    case NativeResponseCode.SERVICE_RESOLUTION_FAILED /*607*/:
                        Slog.d(NsdService.TAG, "SERVICE_RESOLVE_FAILED Raw: " + raw);
                        NsdService.this.stopResolveService(id);
                        removeRequestMap(clientId, id, clientInfo);
                        clientInfo.mResolvedService = null;
                        clientInfo.mChannel.sendMessage(393235, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_RESOLVED /*608*/:
                        Slog.d(NsdService.TAG, "SERVICE_RESOLVED Raw: " + raw);
                        int index = 0;
                        while (index < cooked[2].length() && cooked[2].charAt(index) != '.') {
                            if (cooked[2].charAt(index) == '\\') {
                                index++;
                            }
                            index++;
                        }
                        if (index < cooked[2].length()) {
                            String name = cooked[2].substring(0, index);
                            String type = cooked[2].substring(index).replace(".local.", IElsaManager.EMPTY_PACKAGE);
                            clientInfo.mResolvedService.setServiceName(NsdService.this.unescape(name));
                            clientInfo.mResolvedService.setServiceType(type);
                            clientInfo.mResolvedService.setPort(Integer.parseInt(cooked[4]));
                            clientInfo.mResolvedService.setTxtRecords(cooked[6]);
                            NsdService.this.stopResolveService(id);
                            removeRequestMap(clientId, id, clientInfo);
                            int id2 = NsdService.this.getUniqueId();
                            if (!NsdService.this.getAddrInfo(id2, cooked[3])) {
                                clientInfo.mChannel.sendMessage(393235, 0, clientId);
                                clientInfo.mResolvedService = null;
                                break;
                            }
                            storeRequestMap(clientId, id2, clientInfo, 393234);
                            break;
                        }
                        Slog.e(NsdService.TAG, "Invalid service found " + raw);
                        break;
                        break;
                    case NativeResponseCode.SERVICE_UPDATED /*609*/:
                    case NativeResponseCode.SERVICE_UPDATE_FAILED /*610*/:
                        break;
                    case NativeResponseCode.SERVICE_GET_ADDR_FAILED /*611*/:
                        NsdService.this.stopGetAddrInfo(id);
                        removeRequestMap(clientId, id, clientInfo);
                        clientInfo.mResolvedService = null;
                        Slog.d(NsdService.TAG, "SERVICE_RESOLVE_FAILED Raw: " + raw);
                        clientInfo.mChannel.sendMessage(393235, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_GET_ADDR_SUCCESS /*612*/:
                        Slog.d(NsdService.TAG, "SERVICE_GET_ADDR_SUCCESS Raw: " + raw);
                        try {
                            clientInfo.mResolvedService.setHost(InetAddress.getByName(cooked[4]));
                            clientInfo.mChannel.sendMessage(393236, 0, clientId, clientInfo.mResolvedService);
                        } catch (UnknownHostException e) {
                            clientInfo.mChannel.sendMessage(393235, 0, clientId);
                        }
                        NsdService.this.stopGetAddrInfo(id);
                        removeRequestMap(clientId, id, clientInfo);
                        clientInfo.mResolvedService = null;
                        break;
                    default:
                        handled = false;
                        break;
                }
                return handled;
            }
        }

        protected String getWhatToString(int what) {
            return NsdService.cmdToString(what);
        }

        private void registerForNsdSetting() {
            NsdService.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("nsd_on"), false, new ContentObserver(getHandler()) {
                public void onChange(boolean selfChange) {
                    if (NsdService.this.isNsdEnabled()) {
                        NsdService.this.mNsdStateMachine.sendMessage(393240);
                    } else {
                        NsdService.this.mNsdStateMachine.sendMessage(393241);
                    }
                }
            });
        }

        NsdStateMachine(String name) {
            super(name);
            addState(this.mDefaultState);
            addState(this.mDisabledState, this.mDefaultState);
            addState(this.mEnabledState, this.mDefaultState);
            if (NsdService.this.isNsdEnabled()) {
                setInitialState(this.mEnabledState);
            } else {
                setInitialState(this.mDisabledState);
            }
            setLogRecSize(25);
            registerForNsdSetting();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.NsdService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.NsdService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NsdService.<clinit>():void");
    }

    private static String cmdToString(int cmd) {
        cmd -= BASE;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            return null;
        }
        return sCmdToString[cmd];
    }

    private ArrayMap<String, byte[]> parseTxtRecord(String str, ArrayMap<String, byte[]> txtRecord) {
        Slog.v(TAG, "parseTxtRecord: " + str);
        String[] tokens = str.split("=", 2);
        if (tokens.length >= 2) {
            Slog.v(TAG, "tokens:[0](" + tokens[0] + "), [1](" + tokens[1] + ")");
            byte[] b = new byte[0];
            try {
                b = tokens[1].getBytes("UTF-8");
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
            txtRecord.put(tokens[0], b);
        }
        return txtRecord;
    }

    private String unescape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= s.length()) {
                    Slog.e(TAG, "Unexpected end of escape sequence in: " + s);
                    break;
                }
                c = s.charAt(i);
                if (!(c == '.' || c == '\\')) {
                    if (i + 2 >= s.length()) {
                        Slog.e(TAG, "Unexpected end of escape sequence in: " + s);
                        break;
                    }
                    c = (char) ((((c - 48) * 100) + ((s.charAt(i + 1) - 48) * 10)) + (s.charAt(i + 2) - 48));
                    i += 2;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private NsdService(Context context) {
        this.mClients = new HashMap();
        this.mIdToClientInfoMap = new SparseArray();
        this.mReplyChannel = new AsyncChannel();
        this.INVALID_ID = 0;
        this.mUniqueId = 1;
        this.mNativeDaemonConnected = new CountDownLatch(1);
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mNativeConnector = new NativeDaemonConnector(new NativeCallbackReceiver(), "mdns", 10, MDNS_TAG, 25, null);
        this.mNsdStateMachine = new NsdStateMachine(TAG);
        this.mNsdStateMachine.start();
        new Thread(this.mNativeConnector, MDNS_TAG).start();
    }

    public static NsdService create(Context context) throws InterruptedException {
        NsdService service = new NsdService(context);
        service.mNativeDaemonConnected.await();
        return service;
    }

    public Messenger getMessenger() {
        this.mContext.enforceCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_SEND_MMS_INTERNET, TAG);
        return new Messenger(this.mNsdStateMachine.getHandler());
    }

    public void setEnabled(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Global.putInt(this.mContentResolver, "nsd_on", enable ? 1 : 0);
        if (enable) {
            this.mNsdStateMachine.sendMessage(393240);
        } else {
            this.mNsdStateMachine.sendMessage(393241);
        }
    }

    private void sendNsdStateChangeBroadcast(boolean enabled) {
        Intent intent = new Intent("android.net.nsd.STATE_CHANGED");
        intent.addFlags(67108864);
        if (enabled) {
            intent.putExtra("nsd_state", 2);
        } else {
            intent.putExtra("nsd_state", 1);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean isNsdEnabled() {
        boolean ret = Global.getInt(this.mContentResolver, "nsd_on", 1) == 1;
        Slog.d(TAG, "Network service discovery enabled " + ret);
        return ret;
    }

    private int getUniqueId() {
        int i = this.mUniqueId + 1;
        this.mUniqueId = i;
        if (i != this.INVALID_ID) {
            return this.mUniqueId;
        }
        i = this.mUniqueId + 1;
        this.mUniqueId = i;
        return i;
    }

    private boolean startMDnsDaemon() {
        Slog.d(TAG, "startMDnsDaemon");
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "start-service";
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to start daemon" + e);
            return false;
        }
    }

    private boolean stopMDnsDaemon() {
        Slog.d(TAG, "stopMDnsDaemon");
        try {
            Object[] objArr = new Object[1];
            objArr[0] = "stop-service";
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to start daemon" + e);
            return false;
        }
    }

    private boolean registerService(int regId, NsdServiceInfo service) {
        Slog.d(TAG, "registerService: " + regId + " " + service);
        try {
            Object[] objArr = new Object[6];
            objArr[0] = "register";
            objArr[1] = Integer.valueOf(regId);
            objArr[2] = service.getServiceName();
            objArr[3] = service.getServiceType();
            objArr[4] = Integer.valueOf(service.getPort());
            objArr[5] = Base64.encodeToString(service.getTxtRecord(), 0).replace("\n", IElsaManager.EMPTY_PACKAGE);
            this.mNativeConnector.execute(new Command("mdnssd", objArr));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to execute registerService " + e);
            return false;
        }
    }

    private boolean unregisterService(int regId) {
        Slog.d(TAG, "unregisterService: " + regId);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "stop-register";
            objArr[1] = Integer.valueOf(regId);
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to execute unregisterService " + e);
            return false;
        }
    }

    private boolean updateService(int regId, DnsSdTxtRecord t) {
        Slog.d(TAG, "updateService: " + regId + " " + t);
        if (t == null) {
            return false;
        }
        try {
            Object[] objArr = new Object[4];
            objArr[0] = "update";
            objArr[1] = Integer.valueOf(regId);
            objArr[2] = Integer.valueOf(t.size());
            objArr[3] = t.getRawData();
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to updateServices " + e);
            return false;
        }
    }

    private boolean discoverServices(int discoveryId, String serviceType) {
        Slog.d(TAG, "discoverServices: " + discoveryId + " " + serviceType);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "discover";
            objArr[1] = Integer.valueOf(discoveryId);
            objArr[2] = serviceType;
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to discoverServices " + e);
            return false;
        }
    }

    private boolean stopServiceDiscovery(int discoveryId) {
        Slog.d(TAG, "stopServiceDiscovery: " + discoveryId);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "stop-discover";
            objArr[1] = Integer.valueOf(discoveryId);
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stopServiceDiscovery " + e);
            return false;
        }
    }

    private boolean resolveService(int resolveId, NsdServiceInfo service) {
        Slog.d(TAG, "resolveService: " + resolveId + " " + service);
        try {
            Object[] objArr = new Object[5];
            objArr[0] = "resolve";
            objArr[1] = Integer.valueOf(resolveId);
            objArr[2] = service.getServiceName();
            objArr[3] = service.getServiceType();
            objArr[4] = "local.";
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to resolveService " + e);
            return false;
        }
    }

    private boolean stopResolveService(int resolveId) {
        Slog.d(TAG, "stopResolveService: " + resolveId);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "stop-resolve";
            objArr[1] = Integer.valueOf(resolveId);
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stop resolve " + e);
            return false;
        }
    }

    private boolean getAddrInfo(int resolveId, String hostname) {
        Slog.d(TAG, "getAdddrInfo: " + resolveId);
        try {
            Object[] objArr = new Object[3];
            objArr[0] = "getaddrinfo";
            objArr[1] = Integer.valueOf(resolveId);
            objArr[2] = hostname;
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to getAddrInfo " + e);
            return false;
        }
    }

    private boolean stopGetAddrInfo(int resolveId) {
        Slog.d(TAG, "stopGetAdddrInfo: " + resolveId);
        try {
            Object[] objArr = new Object[2];
            objArr[0] = "stop-getaddrinfo";
            objArr[1] = Integer.valueOf(resolveId);
            this.mNativeConnector.execute("mdnssd", objArr);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stopGetAddrInfo " + e);
            return false;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ServiceDiscoverService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        for (ClientInfo client : this.mClients.values()) {
            pw.println("Client Info");
            pw.println(client);
        }
        this.mNsdStateMachine.dump(fd, pw, args);
    }

    private Message obtainMessage(Message srcMsg) {
        Message msg = Message.obtain();
        msg.arg2 = srcMsg.arg2;
        return msg;
    }

    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            Slog.i(TAG, "replyToMessage what=" + what);
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            dstMsg.arg1 = arg1;
            Slog.i(TAG, "replyToMessage what=" + what);
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private void replyToMessage(Message msg, int what, Object obj) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            dstMsg.obj = obj;
            Slog.i(TAG, "replyToMessage what=" + what);
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }
}
