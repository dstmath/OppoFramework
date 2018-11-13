package com.google.android.maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.HashMap;

/* compiled from: MapActivity */
class NetworkConnectivityListener {
    private Context mContext;
    private HashMap<Handler, Integer> mHandlers = new HashMap();
    private boolean mIsFailover;
    private boolean mListening;
    private NetworkInfo mNetworkInfo;
    private NetworkInfo mOtherNetworkInfo;
    private String mReason;
    private ConnectivityBroadcastReceiver mReceiver = new ConnectivityBroadcastReceiver();
    private State mState = State.UNKNOWN;

    /* compiled from: MapActivity */
    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        private ConnectivityBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && NetworkConnectivityListener.this.mListening) {
                if (intent.getBooleanExtra("noConnectivity", false)) {
                    NetworkConnectivityListener.this.mState = State.NOT_CONNECTED;
                } else {
                    NetworkConnectivityListener.this.mState = State.CONNECTED;
                }
                NetworkConnectivityListener.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                NetworkConnectivityListener.this.mOtherNetworkInfo = (NetworkInfo) intent.getParcelableExtra("otherNetwork");
                NetworkConnectivityListener.this.mReason = intent.getStringExtra("reason");
                NetworkConnectivityListener.this.mIsFailover = intent.getBooleanExtra("isFailover", false);
                for (Handler target : NetworkConnectivityListener.this.mHandlers.keySet()) {
                    target.sendMessage(Message.obtain(target, ((Integer) NetworkConnectivityListener.this.mHandlers.get(target)).intValue()));
                }
                return;
            }
            Log.w("NetworkConnectivityListener", "onReceived() called with " + NetworkConnectivityListener.this.mState.toString() + " and " + intent);
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* compiled from: MapActivity */
    public enum State {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.google.android.maps.NetworkConnectivityListener.State.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.google.android.maps.NetworkConnectivityListener.State.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.maps.NetworkConnectivityListener.State.<clinit>():void");
        }
    }

    public synchronized void startListening(Context context) {
        if (!this.mListening) {
            this.mContext = context;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            context.registerReceiver(this.mReceiver, filter);
            this.mListening = true;
        }
    }

    public synchronized void stopListening() {
        if (this.mListening) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mContext = null;
            this.mNetworkInfo = null;
            this.mOtherNetworkInfo = null;
            this.mIsFailover = false;
            this.mReason = null;
            this.mListening = false;
        }
    }

    public void registerHandler(Handler target, int what) {
        this.mHandlers.put(target, Integer.valueOf(what));
    }

    public void unregisterHandler(Handler target) {
        this.mHandlers.remove(target);
    }

    public State getState() {
        return this.mState;
    }
}
