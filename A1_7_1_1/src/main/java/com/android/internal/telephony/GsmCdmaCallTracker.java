package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telecom.Connection.VideoProvider;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.gsm.GsmCallTrackerHelper;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.cdma.ICdmaCallTrackerExt;
import com.mediatek.internal.telephony.gsm.GsmVTProvider;
import com.mediatek.internal.telephony.gsm.GsmVTProviderUtil;
import com.mediatek.internal.telephony.gsm.GsmVideoCallProviderWrapper;
import com.mediatek.internal.telephony.gsm.IGsmVideoCallProvider;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class GsmCdmaCallTracker extends CallTracker {
    private static final String LOG_TAG = "GsmCdmaCallTracker";
    private static final int MAX_CONNECTIONS_CDMA = 8;
    public static final int MAX_CONNECTIONS_GSM = 19;
    private static final int MAX_CONNECTIONS_PER_CALL_CDMA = 1;
    private static final int MAX_CONNECTIONS_PER_CALL_GSM = 5;
    private static final String PROP_LOG_TAG = "GsmCdmaCallTkr";
    private static final boolean REPEAT_POLLING = false;
    private static final boolean VDBG = false;
    private boolean bAllCallsDisconnectedButNotHandled;
    private boolean hasPendingReplaceRequest;
    private boolean isOemSwitchAccept;
    private int m3WayCallFlashDelay;
    public GsmCdmaCall mBackgroundCall;
    private RegistrantList mCallWaitingRegistrants;
    private ICdmaCallTrackerExt mCdmaCallTrackerExt;
    public GsmCdmaConnection[] mConnections;
    private boolean mDesiredMute;
    private ArrayList<GsmCdmaConnection> mDroppedDuringPoll;
    private BroadcastReceiver mEcmExitReceiver;
    private int[] mEconfSrvccConnectionIds;
    public GsmCdmaCall mForegroundCall;
    private GsmCdmaConnection mHangupConn;
    public boolean mHangupPendingMO;
    boolean mHasPendingSwapRequest;
    GsmCallTrackerHelper mHelper;
    private ArrayList<Connection> mImsConfParticipants;
    private CallTracker mImsPhoneCallTracker;
    private boolean mIsEcmTimerCanceled;
    private boolean mIsInEmergencyCall;
    private int mOemLastMsg;
    private int mPendingCallClirMode;
    private boolean mPendingCallInEcm;
    private GsmCdmaConnection mPendingMO;
    public GsmCdmaPhone mPhone;
    private int mPhoneType;
    public GsmCdmaCall mRingingCall;
    public State mState;
    TelephonyDevController mTelDevController;
    private RegistrantList mVoiceCallEndedRegistrants;
    private RegistrantList mVoiceCallStartedRegistrants;
    WaitForHoldToHangup mWaitForHoldToHangupRequest;
    WaitForHoldToRedial mWaitForHoldToRedialRequest;

    /* renamed from: com.android.internal.telephony.GsmCdmaCallTracker$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ GsmCdmaCallTracker this$0;
        final /* synthetic */ String val$dialString;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaCallTracker.2.<init>(com.android.internal.telephony.GsmCdmaCallTracker, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(com.android.internal.telephony.GsmCdmaCallTracker r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaCallTracker.2.<init>(com.android.internal.telephony.GsmCdmaCallTracker, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.2.<init>(com.android.internal.telephony.GsmCdmaCallTracker, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.2.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.2.run():void");
        }
    }

    class WaitForHoldToHangup {
        private GsmCdmaCall mCall;
        private boolean mHoldDone;
        private boolean mWaitToHangup;
        final /* synthetic */ GsmCdmaCallTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.<init>(com.android.internal.telephony.GsmCdmaCallTracker):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        WaitForHoldToHangup(com.android.internal.telephony.GsmCdmaCallTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.<init>(com.android.internal.telephony.GsmCdmaCallTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.<init>(com.android.internal.telephony.GsmCdmaCallTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.resumeHangupAfterHold():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private boolean resumeHangupAfterHold() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.resumeHangupAfterHold():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.resumeHangupAfterHold():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.isHoldDone():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        boolean isHoldDone() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.isHoldDone():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.isHoldDone():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.isWaitToHangup():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        boolean isWaitToHangup() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.isWaitToHangup():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.isWaitToHangup():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.resetToHangup():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void resetToHangup() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.resetToHangup():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.resetToHangup():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setHoldDone():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void setHoldDone() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setHoldDone():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setHoldDone():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setToHangup():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void setToHangup() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setToHangup():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setToHangup():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setToHangup(com.android.internal.telephony.GsmCdmaCall):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void setToHangup(com.android.internal.telephony.GsmCdmaCall r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setToHangup(com.android.internal.telephony.GsmCdmaCall):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToHangup.setToHangup(com.android.internal.telephony.GsmCdmaCall):void");
        }
    }

    class WaitForHoldToRedial {
        private int mClirMode;
        private String mDialString;
        private UUSInfo mUUSInfo;
        private boolean mWaitToRedial;
        final /* synthetic */ GsmCdmaCallTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.<init>(com.android.internal.telephony.GsmCdmaCallTracker):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        WaitForHoldToRedial(com.android.internal.telephony.GsmCdmaCallTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.<init>(com.android.internal.telephony.GsmCdmaCallTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.<init>(com.android.internal.telephony.GsmCdmaCallTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.resumeDialAfterHold():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private boolean resumeDialAfterHold() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.resumeDialAfterHold():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.resumeDialAfterHold():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.isWaitToRedial():boolean, dex:  in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.isWaitToRedial():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.isWaitToRedial():boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        boolean isWaitToRedial() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.isWaitToRedial():boolean, dex:  in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.isWaitToRedial():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.isWaitToRedial():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.resetToRedial():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void resetToRedial() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.resetToRedial():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.resetToRedial():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial():void, dex:  in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        void setToRedial() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial():void, dex:  in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial(java.lang.String, int, com.android.internal.telephony.UUSInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void setToRedial(java.lang.String r1, int r2, com.android.internal.telephony.UUSInfo r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial(java.lang.String, int, com.android.internal.telephony.UUSInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.WaitForHoldToRedial.setToRedial(java.lang.String, int, com.android.internal.telephony.UUSInfo):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.GsmCdmaCallTracker.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.GsmCdmaCallTracker.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.<clinit>():void");
    }

    private boolean hasC2kOverImsModem() {
        if (this.mTelDevController == null || this.mTelDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasC2kOverImsModem()) {
            return false;
        }
        return true;
    }

    public int getMaxConnections() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return 19;
        }
        return 8;
    }

    public GsmCdmaCallTracker(GsmCdmaPhone phone) {
        this.mImsPhoneCallTracker = null;
        this.mVoiceCallEndedRegistrants = new RegistrantList();
        this.mVoiceCallStartedRegistrants = new RegistrantList();
        this.mDroppedDuringPoll = new ArrayList(19);
        this.mRingingCall = new GsmCdmaCall(this);
        this.mForegroundCall = new GsmCdmaCall(this);
        this.mBackgroundCall = new GsmCdmaCall(this);
        this.mDesiredMute = false;
        this.mState = State.IDLE;
        this.mCallWaitingRegistrants = new RegistrantList();
        this.hasPendingReplaceRequest = false;
        this.mHasPendingSwapRequest = false;
        this.mWaitForHoldToRedialRequest = new WaitForHoldToRedial(this);
        this.mWaitForHoldToHangupRequest = new WaitForHoldToHangup(this);
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mImsConfParticipants = new ArrayList();
        this.mEconfSrvccConnectionIds = null;
        this.bAllCallsDisconnectedButNotHandled = false;
        this.mPhoneType = 0;
        this.mEcmExitReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                    boolean isInEcm = intent.getBooleanExtra("phoneinECMState", false);
                    GsmCdmaCallTracker.this.log("Received ACTION_EMERGENCY_CALLBACK_MODE_CHANGED isInEcm = " + isInEcm);
                    if (!isInEcm) {
                        List<Connection> toNotify = new ArrayList();
                        toNotify.addAll(GsmCdmaCallTracker.this.mRingingCall.getConnections());
                        toNotify.addAll(GsmCdmaCallTracker.this.mForegroundCall.getConnections());
                        toNotify.addAll(GsmCdmaCallTracker.this.mBackgroundCall.getConnections());
                        if (GsmCdmaCallTracker.this.mPendingMO != null) {
                            toNotify.add(GsmCdmaCallTracker.this.mPendingMO);
                        }
                        for (Connection connection : toNotify) {
                            if (connection != null) {
                                connection.onExitedEcmMode();
                            }
                        }
                    }
                }
            }
        };
        this.mHangupConn = null;
        this.isOemSwitchAccept = false;
        this.mOemLastMsg = -1;
        this.mPhone = phone;
        this.mCi = phone.mCi;
        this.mCi.registerForCallStateChanged(this, 2, null);
        this.mCi.registerForOn(this, 9, null);
        this.mCi.registerForNotAvailable(this, 10, null);
        this.mCi.setOnIncomingCallIndication(this, 1000, null);
        this.mCi.registerForOffOrNotAvailable(this, 1001, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mEcmExitReceiver, filter);
        updatePhoneType(true);
        this.mHelper = new GsmCallTrackerHelper(phone.getContext(), this);
        this.mCi.registerForEconfSrvcc(this, 1004, null);
        this.mCdmaCallTrackerExt = (ICdmaCallTrackerExt) MPlugin.createInstance(ICdmaCallTrackerExt.class.getName());
        proprietaryLog("mCdmaCallTrackerExt:" + this.mCdmaCallTrackerExt);
    }

    public void updatePhoneType() {
        updatePhoneType(false);
    }

    private void updatePhoneType(boolean duringInit) {
        if (this.mPhoneType != 2 || this.mPhone.isPhoneTypeGsm()) {
            if (!duringInit) {
                reset();
                if (hasC2kOverImsModem() || this.mPhone.useVzwLogic()) {
                    if (VDBG) {
                        Rlog.d(LOG_TAG, "keep AOSP");
                    }
                    Phone imsPhone = this.mPhone.getImsPhone();
                    if (imsPhone == null || (imsPhone != null && imsPhone.getHandoverConnection() == null)) {
                        pollCallsWhenSafe();
                    } else {
                        Rlog.d(LOG_TAG, "not trigger pollCall since imsCall exists");
                    }
                }
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mConnections = new GsmCdmaConnection[19];
                this.mCi.unregisterForCallWaitingInfo(this);
                this.mCi.unregisterForCallAccepted(this);
                this.mPhoneType = 1;
            } else {
                this.mConnections = new GsmCdmaConnection[8];
                this.mPendingCallInEcm = false;
                this.mIsInEmergencyCall = false;
                this.mPendingCallClirMode = 0;
                this.mIsEcmTimerCanceled = false;
                this.m3WayCallFlashDelay = 0;
                this.mCi.registerForCallWaitingInfo(this, 15, null);
                this.mCi.registerForCallAccepted(this, 1005, null);
                this.mPhoneType = 2;
            }
        }
    }

    private void reset() {
        int i = 0;
        Rlog.d(LOG_TAG, "reset");
        if (!hasC2kOverImsModem() && !this.mPhone.useVzwLogic()) {
            handlePollCalls(new AsyncResult(null, null, new CommandException(Error.RADIO_NOT_AVAILABLE)));
        } else if (this.bAllCallsDisconnectedButNotHandled) {
            handlePollCalls(new AsyncResult(null, null, new CommandException(Error.RADIO_NOT_AVAILABLE)));
        } else if (VDBG) {
            Rlog.d(LOG_TAG, "keep AOSP");
        }
        this.bAllCallsDisconnectedButNotHandled = false;
        clearDisconnected();
        GsmCdmaConnection[] gsmCdmaConnectionArr = this.mConnections;
        int length = gsmCdmaConnectionArr.length;
        while (i < length) {
            GsmCdmaConnection gsmCdmaConnection = gsmCdmaConnectionArr[i];
            if (gsmCdmaConnection != null) {
                gsmCdmaConnection.dispose();
            }
            i++;
        }
        if (this.mPendingMO != null) {
            this.mPendingMO.dispose();
        }
        this.mConnections = null;
        this.mPendingMO = null;
        this.mState = State.IDLE;
    }

    protected void finalize() {
        Rlog.d(LOG_TAG, "GsmCdmaCallTracker finalized");
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceCallStartedRegistrants.add(r);
        if (this.mState != State.IDLE) {
            r.notifyRegistrant(new AsyncResult(null, null, null));
        }
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCallWaitingRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallWaiting(Handler h) {
        this.mCallWaitingRegistrants.remove(h);
    }

    private void fakeHoldForegroundBeforeDial() {
        List<Connection> connCopy = (List) this.mForegroundCall.mConnections.clone();
        int s = connCopy.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) connCopy.get(i)).fakeHoldBeforeDial();
        }
    }

    public synchronized Connection dial(String dialString, int clirMode, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        clearDisconnected();
        if (canDial()) {
            String origNumber = dialString;
            dialString = convertNumberIfNecessary(this.mPhone, dialString);
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                this.mWaitForHoldToRedialRequest.setToRedial();
                switchWaitingOrHoldingAndActive();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                fakeHoldForegroundBeforeDial();
            }
            if (this.mForegroundCall.getState() != Call.State.IDLE) {
                throw new CallStateException("cannot dial in current state");
            }
            boolean isEmergencyCall;
            boolean isEcc;
            if (!hasC2kOverImsModem() || TelephonyManager.getDefault().hasIccCard(this.mPhone.getPhoneId())) {
                isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), this.mPhone.getSubId(), dialString);
                isEcc = PhoneNumberUtils.isEmergencyNumber(this.mPhone.getSubId(), dialString);
            } else {
                isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
                isEcc = PhoneNumberUtils.isEmergencyNumber(dialString);
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyCall);
            this.mHangupPendingMO = false;
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                this.mWaitForHoldToRedialRequest.resetToRedial();
                pollCallsWhenSafe();
            } else {
                setMute(false);
                if (this.mWaitForHoldToRedialRequest.isWaitToRedial()) {
                    this.mWaitForHoldToRedialRequest.setToRedial(this.mPendingMO.getAddress(), clirMode, uusInfo);
                } else if (!PhoneNumberUtils.isEmergencyNumber(this.mPhone.getSubId(), dialString) || (PhoneNumberUtils.isSpecialEmergencyNumber(this.mPhone.getSubId(), dialString) && this.mPhone.getServiceState().getState() == 0)) {
                    this.mCi.dial(this.mPendingMO.getAddress(), clirMode, uusInfo, obtainCompleteMessage(1003));
                } else {
                    this.mCi.setEccServiceCategory(PhoneNumberUtils.getServiceCategoryFromEccBySubId(dialString, this.mPhone.getSubId()));
                    this.mCi.emergencyDial(this.mPendingMO.getAddress(), clirMode, uusInfo, obtainCompleteMessage(1003));
                }
            }
            if (this.mNumberConverted) {
                this.mPendingMO.setConverted(origNumber);
                this.mNumberConverted = false;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case 0:
                this.mIsEcmTimerCanceled = false;
                return;
            case 1:
                this.mIsEcmTimerCanceled = true;
                return;
            default:
                Rlog.e(LOG_TAG, "handleEcmTimer, unsupported action " + action);
                return;
        }
    }

    private void disableDataCallInEmergencyCall(String dialString) {
        boolean isEmergencyCall;
        if (!hasC2kOverImsModem() || TelephonyManager.getDefault().hasIccCard(this.mPhone.getPhoneId())) {
            isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), this.mPhone.getSubId(), dialString);
        } else {
            isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
        }
        if (isEmergencyCall) {
            log("disableDataCallInEmergencyCall");
            setIsInEmergencyCall();
        }
    }

    public void setIsInEmergencyCall() {
        this.mIsInEmergencyCall = true;
        this.mPhone.mDcTracker.setInternalDataEnabled(false);
        this.mPhone.notifyEmergencyCallRegistrants(true);
        this.mPhone.sendEmergencyCallStateChange(true);
    }

    private Connection dial(String dialString, int clirMode) throws CallStateException {
        clearDisconnected();
        if (canDial()) {
            boolean internationalRoaming;
            boolean isEmergencyCall;
            boolean isEcc;
            TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
            String origNumber = dialString;
            String operatorIsoContry = tm.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
            String simIsoContry = tm.getSimCountryIsoForPhone(this.mPhone.getPhoneId());
            if (TextUtils.isEmpty(operatorIsoContry) || TextUtils.isEmpty(simIsoContry)) {
                internationalRoaming = false;
            } else {
                internationalRoaming = !simIsoContry.equals(operatorIsoContry);
            }
            if (internationalRoaming) {
                if ("us".equals(simIsoContry)) {
                    internationalRoaming = internationalRoaming && !"vi".equals(operatorIsoContry);
                } else if ("vi".equals(simIsoContry)) {
                    internationalRoaming = internationalRoaming && !"us".equals(operatorIsoContry);
                }
            }
            if (internationalRoaming) {
                dialString = convertNumberIfNecessary(this.mPhone, dialString);
            }
            boolean isPhoneInEcmMode = TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "ril.cdma.inecmmode", "false").equals("true");
            if (!hasC2kOverImsModem() || TelephonyManager.getDefault().hasIccCard(this.mPhone.getPhoneId())) {
                isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), this.mPhone.getSubId(), dialString);
                isEcc = PhoneNumberUtils.isEmergencyNumber(this.mPhone.getSubId(), dialString);
            } else {
                isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
                isEcc = PhoneNumberUtils.isEmergencyNumber(dialString);
            }
            if (isPhoneInEcmMode && isEmergencyCall) {
                handleEcmTimer(1);
            }
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                return dialThreeWay(dialString);
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyCall);
            this.mHangupPendingMO = false;
            GsmCdmaConnection result = this.mPendingMO;
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
            } else {
                setMute(false);
                disableDataCallInEmergencyCall(dialString);
                if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyCall)) {
                    String tmpStr = this.mPendingMO.getAddress();
                    tmpStr = this.mPendingMO.getAddress() + "," + PhoneNumberUtils.extractNetworkPortionAlt(dialString);
                    if (isEcc) {
                        this.mCi.emergencyDial(tmpStr, clirMode, null, obtainCompleteMessage());
                    } else {
                        this.mCi.dial(tmpStr, clirMode, obtainCompleteMessage());
                    }
                    if (this.mCdmaCallTrackerExt != null) {
                        if (this.mCdmaCallTrackerExt.needToConvert(dialString, GsmCdmaConnection.formatDialString(dialString))) {
                            result.setConverted(PhoneNumberUtils.extractNetworkPortionAlt(dialString));
                        }
                    }
                } else {
                    this.mPhone.exitEmergencyCallbackMode();
                    this.mPhone.setOnEcbModeExitResponse(this, 14, dialString);
                    this.mPendingCallClirMode = clirMode;
                    this.mPendingCallInEcm = true;
                }
            }
            if (this.mNumberConverted) {
                result.setConverted(origNumber);
                this.mNumberConverted = false;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return result;
        }
        throw new CallStateException("cannot dial in current state");
    }

    private Connection dialThreeWay(String dialString) {
        if (this.mForegroundCall.isIdle()) {
            return null;
        }
        disableDataCallInEmergencyCall(dialString);
        this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, this.mIsInEmergencyCall);
        this.m3WayCallFlashDelay = this.mPhone.getContext().getResources().getInteger(17694870);
        if (this.m3WayCallFlashDelay > 0) {
            this.mCi.sendCDMAFeatureCode(UsimPBMemInfo.STRING_NOT_SET, obtainMessage(20, dialString));
        } else {
            String tmpStr = this.mPendingMO.getAddress();
            this.mCi.sendCDMAFeatureCode(this.mPendingMO.getAddress() + "," + PhoneNumberUtils.extractNetworkPortionAlt(dialString), obtainMessage(16));
            if (this.mCdmaCallTrackerExt != null && this.mCdmaCallTrackerExt.needToConvert(dialString, GsmCdmaConnection.formatDialString(dialString))) {
                this.mPendingMO.setConverted(PhoneNumberUtils.extractNetworkPortionAlt(dialString));
            }
        }
        return this.mPendingMO;
    }

    public Connection dial(String dialString) throws CallStateException {
        if (isPhoneTypeGsm()) {
            return dial(dialString, 0, null);
        }
        return dial(dialString, 0);
    }

    public Connection dial(String dialString, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        return dial(dialString, 0, uusInfo, intentExtras);
    }

    private Connection dial(String dialString, int clirMode, Bundle intentExtras) throws CallStateException {
        return dial(dialString, clirMode, null, intentExtras);
    }

    public void acceptCall() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            Rlog.i("phone", "acceptCall: incoming...");
            setMute(false);
            this.mCi.acceptCall(obtainCompleteMessage());
        } else if (this.mRingingCall.getState() == Call.State.WAITING) {
            if (isPhoneTypeGsm()) {
                setMute(false);
            } else {
                GsmCdmaConnection cwConn = (GsmCdmaConnection) this.mRingingCall.getLatestConnection();
                cwConn.updateParent(this.mRingingCall, this.mForegroundCall);
                cwConn.onConnectedInOrOut();
                updatePhoneState();
            }
            switchWaitingOrHoldingAndActive();
        } else {
            throw new CallStateException("phone not ringing");
        }
    }

    public void rejectCall() throws CallStateException {
        if (this.mRingingCall.getState().isRinging()) {
            this.mCi.rejectCall(obtainCompleteMessage());
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    private void flashAndSetGenericTrue() {
        this.mCi.sendCDMAFeatureCode(UsimPBMemInfo.STRING_NOT_SET, obtainMessage(8));
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (isPhoneTypeGsm()) {
            if (!this.mHasPendingSwapRequest) {
                this.mWaitForHoldToHangupRequest.setToHangup();
                this.mCi.switchWaitingOrHoldingAndActive(obtainCompleteMessage(8));
                this.mHasPendingSwapRequest = true;
            }
        } else if (this.mForegroundCall.getConnections().size() > 1) {
            flashAndSetGenericTrue();
        } else {
            this.mCi.sendCDMAFeatureCode(UsimPBMemInfo.STRING_NOT_SET, obtainMessage(8));
        }
    }

    public void conference() {
        if (isPhoneTypeGsm()) {
            this.mCi.conference(obtainCompleteMessage(11));
        } else {
            flashAndSetGenericTrue();
        }
    }

    public void explicitCallTransfer() {
        this.mCi.explicitCallTransfer(obtainCompleteMessage(13));
    }

    public void clearDisconnected() {
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        if (this.mForegroundCall.getState() != Call.State.ACTIVE || this.mBackgroundCall.getState() != Call.State.HOLDING || this.mBackgroundCall.isFull() || this.mForegroundCall.isFull()) {
            return false;
        }
        return true;
    }

    private boolean canDial() {
        boolean ret;
        boolean z;
        boolean z2 = false;
        int serviceState = this.mPhone.getServiceState().getState();
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        if (serviceState == 3 || this.mPendingMO != null || this.mRingingCall.isRinging() || disableCall.equals("true")) {
            ret = false;
        } else {
            z = (this.mForegroundCall.getState().isAlive() && this.mBackgroundCall.getState().isAlive()) ? !isPhoneTypeGsm() ? this.mForegroundCall.getState() == Call.State.ACTIVE : false : true;
            ret = z;
        }
        if (!ret) {
            String str = "canDial is false\n((serviceState=%d) != ServiceState.STATE_POWER_OFF)::=%s\n&& pendingMO == null::=%s\n&& !ringingCall.isRinging()::=%s\n&& !disableCall.equals(\"true\")::=%s\n&& (!foregroundCall.getState().isAlive()::=%s\n   || foregroundCall.getState() == GsmCdmaCall.State.ACTIVE::=%s\n   ||!backgroundCall.getState().isAlive())::=%s)";
            Object[] objArr = new Object[8];
            objArr[0] = Integer.valueOf(serviceState);
            if (serviceState != 3) {
                z = true;
            } else {
                z = false;
            }
            objArr[1] = Boolean.valueOf(z);
            if (this.mPendingMO == null) {
                z = true;
            } else {
                z = false;
            }
            objArr[2] = Boolean.valueOf(z);
            if (this.mRingingCall.isRinging()) {
                z = false;
            } else {
                z = true;
            }
            objArr[3] = Boolean.valueOf(z);
            if (disableCall.equals("true")) {
                z = false;
            } else {
                z = true;
            }
            objArr[4] = Boolean.valueOf(z);
            if (this.mForegroundCall.getState().isAlive()) {
                z = false;
            } else {
                z = true;
            }
            objArr[5] = Boolean.valueOf(z);
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                z = true;
            } else {
                z = false;
            }
            objArr[6] = Boolean.valueOf(z);
            if (!this.mBackgroundCall.getState().isAlive()) {
                z2 = true;
            }
            objArr[7] = Boolean.valueOf(z2);
            log(String.format(str, objArr));
        }
        return ret;
    }

    public boolean canTransfer() {
        boolean z = false;
        if (isPhoneTypeGsm()) {
            if ((this.mForegroundCall.getState() == Call.State.ACTIVE || this.mForegroundCall.getState() == Call.State.ALERTING || this.mForegroundCall.getState() == Call.State.DIALING) && this.mBackgroundCall.getState() == Call.State.HOLDING) {
                z = true;
            }
            return z;
        }
        Rlog.e(LOG_TAG, "canTransfer: not possible in CDMA");
        return false;
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
    }

    public Message obtainCompleteMessage() {
        return obtainCompleteMessage(4);
    }

    public Message obtainCompleteMessage(int what) {
        this.mPendingOperations++;
        this.mLastRelevantPoll = null;
        this.mNeedsPoll = true;
        if (DBG_POLL) {
            log("obtainCompleteMessage: pendingOperations=" + this.mPendingOperations + ", needsPoll=" + this.mNeedsPoll);
        }
        return obtainMessage(what);
    }

    private void operationComplete() {
        this.mPendingOperations--;
        if (DBG_POLL) {
            log("operationComplete: pendingOperations=" + this.mPendingOperations + ", needsPoll=" + this.mNeedsPoll);
        }
        if (this.mPendingOperations == 0 && this.mNeedsPoll) {
            this.mLastRelevantPoll = obtainMessage(1);
            this.mCi.getCurrentCalls(this.mLastRelevantPoll);
        } else if (this.mPendingOperations < 0) {
            Rlog.e(LOG_TAG, "GsmCdmaCallTracker.pendingOperations < 0");
            this.mPendingOperations = 0;
        }
    }

    private void updatePhoneState() {
        State oldState = this.mState;
        if (this.mRingingCall.isRinging()) {
            this.mState = State.RINGING;
        } else if (this.mPendingMO == null && this.mForegroundCall.isIdle() && this.mBackgroundCall.isIdle()) {
            Phone imsPhone = this.mPhone.getImsPhone();
            if (imsPhone != null) {
                imsPhone.callEndCleanupHandOverCallIfAny();
            }
            this.mState = State.IDLE;
        } else {
            this.mState = State.OFFHOOK;
        }
        if (this.mState == State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
            this.isOemSwitchAccept = false;
            this.mOemLastMsg = -1;
        } else if (oldState == State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        log("update phone state, old=" + oldState + " new=" + this.mState);
        if (this.mState != oldState) {
            OemConstant.setOemCallState(oldState, this.mState);
            if (this.mState != State.RINGING) {
                this.mPhone.notifyPhoneStateChanged();
            }
            TelephonyMetrics.getInstance().writePhoneState(this.mPhone.getPhoneId(), this.mState);
        }
    }

    /* JADX WARNING: Missing block: B:411:0x0f6e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void handlePollCalls(AsyncResult ar) {
        List polledCalls;
        Connection conn;
        Connection hoConnection;
        Iterator<Connection> it;
        Connection c;
        int count;
        int n;
        if (VDBG) {
            log("handlePollCalls");
        }
        if (ar.exception == null) {
            polledCalls = ar.result;
        } else {
            if (isCommandExceptionRadioNotAvailable(ar.exception)) {
                polledCalls = new ArrayList();
            } else if (!this.mNeedWaitImsEConfSrvcc || hasParsingCEPCapability()) {
                pollCallsAfterDelay();
                return;
            } else {
                proprietaryLog("SRVCC: +ECONFSRVCC is still not arrival, skip this poll call.");
                return;
            }
        }
        Connection newRinging = null;
        ArrayList<Connection> newUnknownConnectionsGsm = new ArrayList();
        Connection newUnknownConnectionCdma = null;
        boolean hasNonHangupStateChanged = false;
        boolean hasAnyCallDisconnected = false;
        boolean unknownConnectionAppeared = false;
        int handoverConnectionsSize = this.mHandoverConnections.size();
        boolean noConnectionExists = true;
        int i = 0;
        int curDC = 0;
        int dcSize = polledCalls.size();
        while (i < this.mConnections.length) {
            conn = this.mConnections[i];
            DriverCall dc = null;
            if (curDC < dcSize) {
                dc = (DriverCall) polledCalls.get(curDC);
                if (!(isPhoneTypeGsm() || this.mCdmaCallTrackerExt == null)) {
                    dc.number = this.mCdmaCallTrackerExt.processPlusCodeForDriverCall(dc.number, dc.isMT, dc.TOA);
                }
                if (dc.index == i + 1) {
                    curDC++;
                } else {
                    dc = null;
                }
            }
            if (!(conn == null && dc == null)) {
                noConnectionExists = false;
            }
            if (DBG_POLL) {
                log("poll: conn[i=" + i + "]=" + conn + ", dc=" + dc);
            }
            if (conn == null && dc != null) {
                if (DBG_POLL) {
                    log("case 1 : new Call appear");
                }
                if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc)) {
                    log("pendingMo=" + this.mPendingMO + ", dc=" + dc);
                    if (!(this.mPendingMO == null || this.mPendingMO.compareTo(dc))) {
                        proprietaryLog("MO/MT conflict! MO should be hangup by MD");
                    }
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    if (isPhoneTypeGsm()) {
                        this.mHelper.setForwardingAddressToConnection(i, this.mConnections[i]);
                    }
                    hoConnection = getHoConnection(dc);
                    if (hoConnection == null) {
                        if (!OemConstant.isCallInEnable(this.mPhone) && (DriverCall.State.INCOMING == dc.state || DriverCall.State.WAITING == dc.state)) {
                            log("ctmm vi block");
                            try {
                                hangup(this.mConnections[i]);
                            } catch (CallStateException e) {
                                log("Exception in hangup call");
                            }
                        }
                        newRinging = checkMtFindNewRinging(dc, i);
                        if (newRinging == null) {
                            unknownConnectionAppeared = true;
                            if (isPhoneTypeGsm()) {
                                newUnknownConnectionsGsm.add(this.mConnections[i]);
                            } else {
                                newUnknownConnectionCdma = this.mConnections[i];
                            }
                        }
                    } else if (hoConnection.isMultipartyBeforeHandover() && hoConnection.isConfHostBeforeHandover() && !hasParsingCEPCapability()) {
                        Rlog.i(LOG_TAG, "SRVCC: goes to conference case.");
                        this.mConnections[i].mOrigConnection = hoConnection;
                        this.mImsConfParticipants.add(this.mConnections[i]);
                    } else {
                        Rlog.i(LOG_TAG, "SRVCC: goes to normal call case.");
                        this.mImsPhoneCallTracker = null;
                        if (this.mPhone.getImsPhone() != null) {
                            this.mImsPhoneCallTracker = this.mPhone.getImsPhone().getCallTracker();
                        }
                        this.mConnections[i].migrateFrom(hoConnection);
                        if (!(hoConnection.mPreHandoverState == Call.State.ACTIVE || hoConnection.mPreHandoverState == Call.State.HOLDING || dc.state != DriverCall.State.ACTIVE)) {
                            this.mConnections[i].onConnectedInOrOut();
                        }
                        this.mHandoverConnections.remove(hoConnection);
                        if (this.mImsPhoneCallTracker != null && this.mImsPhoneCallTracker.isImsCallHangupPending()) {
                            log("There is pending hangup before SRVCC " + this.mImsPhoneCallTracker.getPendingHangupAddr());
                            String HOConnectionAddr = this.mConnections[i].getAddress();
                            if (HOConnectionAddr != null && HOConnectionAddr.equals(this.mImsPhoneCallTracker.getPendingHangupAddr())) {
                                try {
                                    log("Pending hang up in SRVCC Case");
                                    hangup(this.mConnections[i]);
                                    this.mImsPhoneCallTracker.mPendingHangupCall = null;
                                    this.mImsPhoneCallTracker.mPendingHangupAddr = null;
                                } catch (CallStateException e2) {
                                    Rlog.e(LOG_TAG, "unexpected error on hangup of SRVCC Case");
                                }
                            }
                        }
                        if (isPhoneTypeGsm()) {
                            it = this.mHandoverConnections.iterator();
                            while (it.hasNext()) {
                                c = (Connection) it.next();
                                Rlog.i(LOG_TAG, "HO Conn state is " + c.mPreHandoverState);
                                if (c.mPreHandoverState == this.mConnections[i].getState()) {
                                    Rlog.i(LOG_TAG, "Removing HO conn " + hoConnection + c.mPreHandoverState);
                                    it.remove();
                                }
                            }
                        } else if (this.mIsInEmergencyCall && !this.mIsEcmTimerCanceled && this.mPhone.isInEcm()) {
                            Rlog.i(LOG_TAG, "Ecm timer has been canceled in IMS, so set mIsEcmTimerCanceled=true directly");
                            this.mIsEcmTimerCanceled = true;
                        }
                        this.mPhone.oemMigrateFrom();
                        if (this.mPhone.hasHoRegistrants()) {
                            this.mPhone.notifyHandoverStateChanged(this.mConnections[i]);
                        } else {
                            unknownConnectionAppeared = true;
                            if (isPhoneTypeGsm()) {
                                newUnknownConnectionsGsm.add(this.mConnections[i]);
                            } else {
                                newUnknownConnectionCdma = this.mConnections[i];
                            }
                        }
                    }
                } else {
                    if (DBG_POLL) {
                        proprietaryLog("poll: pendingMO=" + this.mPendingMO);
                    }
                    if (SystemProperties.get("ro.mtk_vt3g324m_support").equals("1") && this.mForegroundCall.mVTProvider != null && dc.isVideo) {
                        this.mForegroundCall.mVTProvider.setId(i + 1);
                    }
                    this.mConnections[i] = this.mPendingMO;
                    this.mPendingMO.mIndex = i;
                    this.mPendingMO.update(dc);
                    this.mPendingMO = null;
                    if (this.mHangupPendingMO) {
                        this.mHangupPendingMO = false;
                        if (!isPhoneTypeGsm() && this.mIsEcmTimerCanceled) {
                            handleEcmTimer(0);
                        }
                        try {
                            log("poll: hangupPendingMO, hangup conn " + i);
                            if (this.mHangupConn == null || this.mHangupConn.mIndex != this.mConnections[i].mIndex) {
                                hangup(this.mConnections[i]);
                            }
                        } catch (CallStateException e3) {
                            Rlog.e(LOG_TAG, "unexpected error on hangup");
                        }
                    }
                }
                hasNonHangupStateChanged = true;
            } else if (conn != null && dc == null) {
                if (DBG_POLL) {
                    proprietaryLog("case 2 : old Call disappear");
                }
                if (isPhoneTypeGsm()) {
                    if (!((conn.getCall() == this.mForegroundCall && this.mForegroundCall.mConnections.size() == 1 && this.mBackgroundCall.isIdle()) || (conn.getCall() == this.mBackgroundCall && this.mBackgroundCall.mConnections.size() == 1 && this.mForegroundCall.isIdle())) || this.mRingingCall.getState() == Call.State.WAITING) {
                    }
                    this.mDroppedDuringPoll.add(conn);
                    this.mConnections[i] = null;
                    this.mHelper.CallIndicationEnd();
                    this.mHelper.clearForwardingAddressVariables(i);
                } else {
                    count = this.mForegroundCall.mConnections.size();
                    for (n = 0; n < count; n++) {
                        log("adding fgCall cn " + n + " to droppedDuringPoll");
                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mForegroundCall.mConnections.get(n));
                    }
                    count = this.mRingingCall.mConnections.size();
                    for (n = 0; n < count; n++) {
                        log("adding rgCall cn " + n + " to droppedDuringPoll");
                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mRingingCall.mConnections.get(n));
                    }
                    if (this.mIsEcmTimerCanceled) {
                        handleEcmTimer(0);
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                    this.mConnections[i] = null;
                }
            } else if (conn != null && dc != null && !conn.compareTo(dc) && isPhoneTypeGsm()) {
                if (DBG_POLL) {
                    proprietaryLog("case 3 : old Call replaced");
                }
                this.mDroppedDuringPoll.add(conn);
                if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc)) {
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                } else {
                    proprietaryLog("ringing disc not updated yet & replaced by pendingMo");
                    if (SystemProperties.get("ro.mtk_vt3g324m_support").equals("1") && this.mForegroundCall.mVTProvider != null && dc.isVideo) {
                        this.mForegroundCall.mVTProvider.setId(i + 1);
                    }
                    this.mConnections[i] = this.mPendingMO;
                    this.mPendingMO.mIndex = i;
                    this.mPendingMO.update(dc);
                    this.mPendingMO = null;
                }
                if (this.mConnections[i].getCall() == this.mRingingCall) {
                    newRinging = this.mConnections[i];
                }
                hasNonHangupStateChanged = true;
            } else if (!(conn == null || dc == null)) {
                if (isPhoneTypeGsm() || conn.isIncoming() == dc.isMT) {
                    log("accept the call,conn:" + conn.getState() + "/dc:" + dc.state);
                    if (this.isOemSwitchAccept && conn.getState() == Call.State.WAITING && dc.state == DriverCall.State.INCOMING) {
                        this.isOemSwitchAccept = false;
                        this.mOemLastMsg = -1;
                        log("accept the call!");
                        setMute(false);
                        this.mCi.acceptCall(obtainCompleteMessage());
                    }
                    if (DBG_POLL) {
                        proprietaryLog("case 4 : old Call update");
                    }
                    hasNonHangupStateChanged = !hasNonHangupStateChanged ? conn.update(dc) : true;
                } else if (dc.isMT) {
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    this.mDroppedDuringPoll.add(conn);
                    newRinging = checkMtFindNewRinging(dc, i);
                    if (newRinging == null) {
                        unknownConnectionAppeared = true;
                        newUnknownConnectionCdma = conn;
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                } else {
                    Rlog.e(LOG_TAG, "Error in RIL, Phantom call appeared " + dc);
                }
            }
            i++;
        }
        if (!isPhoneTypeGsm() && noConnectionExists) {
            checkAndEnableDataCallAfterEmergencyCallDropped();
        }
        if (this.mPendingMO != null) {
            Rlog.d(LOG_TAG, "Pending MO dropped before poll fg state:" + this.mForegroundCall.getState());
            this.mDroppedDuringPoll.add(this.mPendingMO);
            this.mPendingMO = null;
            this.mHangupPendingMO = false;
            if (!isPhoneTypeGsm()) {
                if (this.mPendingCallInEcm) {
                    this.mPendingCallInEcm = false;
                }
                if (this.mIsEcmTimerCanceled) {
                    handleEcmTimer(0);
                }
                checkAndEnableDataCallAfterEmergencyCallDropped();
            }
        }
        if (polledCalls.size() == 0 && this.mConnections.length == 0) {
            if (DBG_POLL) {
                proprietaryLog("check whether fgCall or ringCall have mConnections");
            }
            if (!isPhoneTypeGsm()) {
                count = this.mForegroundCall.mConnections.size();
                for (n = 0; n < count; n++) {
                    log("adding fgCall cn " + n + " to droppedDuringPoll");
                    this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mForegroundCall.mConnections.get(n));
                }
                count = this.mRingingCall.mConnections.size();
                for (n = 0; n < count; n++) {
                    log("adding rgCall cn " + n + " to droppedDuringPoll");
                    this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mRingingCall.mConnections.get(n));
                }
            }
        }
        if (newRinging != null) {
            OemConstant.checkCallState("true");
            this.mPhone.notifyNewRingingConnection(newRinging);
            if (isOemAutoAnswer(this.mPhone)) {
                Rlog.d(LOG_TAG, "acceptCall: for test card...");
                sendEmptyMessageDelayed(900, (long) (isPhoneTypeGsm() ? 3 : ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT));
            }
            if (SystemProperties.get("ro.mtk_vt3g324m_support").equals("1") && newRinging.isVideo()) {
                newRinging.setVideoState(3);
                try {
                    GsmVTProviderUtil.setContext(this.mPhone.getContext());
                    this.mRingingCall.mVTProvider = new GsmVTProvider(((GsmCdmaConnection) newRinging).getGsmCdmaIndex());
                    proprietaryLog("handlePollCalls new GsmVTProvider");
                    IGsmVideoCallProvider gsmVideoCallProvider = this.mRingingCall.mVTProvider.getInterface();
                    if (gsmVideoCallProvider != null) {
                        VideoProvider gsmVideoCallProviderWrapper = new GsmVideoCallProviderWrapper(gsmVideoCallProvider);
                        proprietaryLog("handlePollCalls new GsmVideoCallProviderWrapper");
                        newRinging.setVideoProvider(gsmVideoCallProviderWrapper);
                    }
                } catch (CallStateException e4) {
                } catch (ClassCastException e5) {
                    Rlog.e(PROP_LOG_TAG, "cast to GsmCdmaConnection fail for newRinging " + e5);
                } catch (RemoteException e6) {
                    Rlog.e(PROP_LOG_TAG, "handlePollCalls new GsmVideoCallProviderWrapper failed");
                }
            }
        }
        if (this.isOemSwitchAccept) {
            if (this.mOemLastMsg != -1) {
                this.mPhone.notifySuppServiceFailed(getFailedService(this.mOemLastMsg));
            }
            this.isOemSwitchAccept = false;
            this.mOemLastMsg = -1;
        }
        int mDropSize = this.mDroppedDuringPoll.size();
        for (i = this.mDroppedDuringPoll.size() - 1; i >= 0; i--) {
            conn = (GsmCdmaConnection) this.mDroppedDuringPoll.get(i);
            boolean wasDisconnected = false;
            if (isCommandExceptionRadioNotAvailable(ar.exception)) {
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn.onDisconnect(14);
                wasDisconnected = true;
            } else if (conn.isIncoming() && conn.getConnectTime() == 0 && conn.getState() != Call.State.ACTIVE) {
                int cause;
                if (conn.mCause == 3 || conn.mCause == 16) {
                    cause = 16;
                } else {
                    cause = 1;
                }
                log("missed/rejected call, conn.cause=" + conn.mCause);
                log("setting cause to " + cause);
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn.onDisconnect(cause);
                wasDisconnected = true;
            } else if (conn.mCause == 3 || conn.mCause == 7) {
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn.onDisconnect(conn.mCause);
                wasDisconnected = true;
            }
            if (!isPhoneTypeGsm() && wasDisconnected && unknownConnectionAppeared && conn == newUnknownConnectionCdma) {
                unknownConnectionAppeared = false;
                newUnknownConnectionCdma = null;
            }
        }
        if (this.mImsConfHostConnection != null) {
            ImsPhoneConnection hostConn = (ImsPhoneConnection) this.mImsConfHostConnection;
            if (this.mImsConfParticipants.size() >= 2) {
                restoreConferenceParticipantAddress();
                proprietaryLog("SRVCC: notify new participant connections");
                hostConn.notifyConferenceConnectionsConfigured(this.mImsConfParticipants);
            } else if (this.mImsConfParticipants.size() == 1) {
                Connection participant = (GsmCdmaConnection) this.mImsConfParticipants.get(0);
                String address = hostConn.getConferenceParticipantAddress(0);
                proprietaryLog("SRVCC: restore participant connection with address: " + address);
                participant.updateConferenceParticipantAddress(address);
                proprietaryLog("SRVCC: only one connection, consider it as a normal call SRVCC");
                this.mPhone.notifyHandoverStateChanged(participant);
            } else {
                Rlog.e(PROP_LOG_TAG, "SRVCC: abnormal case, no participant connections.");
            }
            this.mImsConfParticipants.clear();
            this.mImsConfHostConnection = null;
            this.mEconfSrvccConnectionIds = null;
        }
        it = this.mHandoverConnections.iterator();
        while (it.hasNext()) {
            hoConnection = (Connection) it.next();
            log("handlePollCalls - disconnect hoConn= " + hoConnection + " hoConn.State= " + hoConnection.getState());
            if (hoConnection.getState().isRinging()) {
                hoConnection.onDisconnect(1);
            } else {
                hoConnection.onDisconnect(-1);
            }
            it.remove();
        }
        if (mDropSize > 0 && !this.hasPendingReplaceRequest) {
            this.mCi.getLastCallFailCause(obtainNoPollCompleteMessage(5));
        }
        if (false) {
            pollCallsAfterDelay();
        }
        if ((newRinging != null || hasNonHangupStateChanged || hasAnyCallDisconnected) && !this.mHasPendingSwapRequest) {
            internalClearDisconnected();
        }
        if (VDBG) {
            log("handlePollCalls calling updatePhoneState()");
        }
        updatePhoneState();
        if (this.mState == State.IDLE) {
            log("Phone in IDLE State, reset that CRSS msg");
            if (this.mPhone.getCachedCrss() != null) {
                this.mPhone.resetCachedCrss();
            }
        }
        if (unknownConnectionAppeared) {
            if (isPhoneTypeGsm()) {
                for (Connection c2 : newUnknownConnectionsGsm) {
                    log("Notify unknown for " + c2);
                    this.mPhone.notifyUnknownConnection(c2);
                }
            } else {
                this.mPhone.notifyUnknownConnection(newUnknownConnectionCdma);
            }
        }
        if (hasNonHangupStateChanged || newRinging != null || hasAnyCallDisconnected) {
            this.mPhone.notifyPreciseCallStateChanged();
        }
        if (handoverConnectionsSize > 0 && this.mHandoverConnections.size() == 0) {
            Phone imsPhone = this.mPhone.getImsPhone();
            if (imsPhone != null) {
                imsPhone.callEndCleanupHandOverCallIfAny();
            }
        }
        if (isPhoneTypeGsm() && this.mConnections != null && this.mConnections.length == 19 && this.mHelper.getCurrentTotalConnections() == 1 && this.mRingingCall.getState() == Call.State.WAITING) {
            this.mRingingCall.mState = Call.State.INCOMING;
        }
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        int i;
        Rlog.i(LOG_TAG, "Phone State:" + this.mState);
        Rlog.i(LOG_TAG, "Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Foreground call: " + this.mForegroundCall.toString());
        l = this.mForegroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Background call: " + this.mBackgroundCall.toString());
        l = this.mBackgroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        if (isPhoneTypeGsm()) {
            this.mHelper.LogState();
        }
    }

    public void hangup(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner != this) {
            throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
        }
        if (conn == this.mPendingMO) {
            log("hangup: set hangupPendingMO to true");
            this.mHangupPendingMO = true;
        } else if (!isPhoneTypeGsm() && conn.getCall() == this.mRingingCall && this.mRingingCall.getState() == Call.State.WAITING) {
            proprietaryLog("hangup waiting call");
            conn.onLocalDisconnect();
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return;
        } else {
            try {
                this.mCi.hangupConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage(1002));
            } catch (CallStateException e) {
                Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: hangup() on absent connection " + conn);
            }
        }
        conn.onHangupLocal();
    }

    public void separate(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner != this) {
            throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
        }
        try {
            this.mCi.separateConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage(12));
        } catch (CallStateException e) {
            Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: separate() on absent connection " + conn);
        }
    }

    public void setMute(boolean mute) {
        this.mDesiredMute = mute;
        this.mCi.setMute(this.mDesiredMute, null);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void hangup(GsmCdmaCall call) throws CallStateException {
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections in call");
        }
        if (call == this.mRingingCall) {
            log("(ringing) hangup waiting or background");
            this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
        } else if (call == this.mForegroundCall) {
            if (call.isDialingOrAlerting()) {
                log("(foregnd) hangup dialing or alerting...");
                hangup((GsmCdmaConnection) call.getConnections().get(0));
            } else {
                log("(foregnd) hangup active");
                if (isPhoneTypeGsm()) {
                    boolean isEmergencyCall;
                    String address = ((GsmCdmaConnection) call.getConnections().get(0)).getAddress();
                    if (!hasC2kOverImsModem() || TelephonyManager.getDefault().hasIccCard(this.mPhone.getPhoneId())) {
                        isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), this.mPhone.getSubId(), address);
                    } else {
                        isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), address);
                    }
                    if (isEmergencyCall && !PhoneNumberUtils.isSpecialEmergencyNumber(this.mPhone.getSubId(), address)) {
                        proprietaryLog("(foregnd) hangup active ECC call by connection index");
                        hangup((GsmCdmaConnection) call.getConnections().get(0));
                    } else if (this.mWaitForHoldToHangupRequest.isWaitToHangup()) {
                        this.mWaitForHoldToHangupRequest.setToHangup(call);
                    } else {
                        hangupForegroundResumeBackground();
                    }
                } else {
                    hangupForegroundResumeBackground();
                }
            }
        } else if (call != this.mBackgroundCall) {
            throw new RuntimeException("GsmCdmaCall " + call + "does not belong to GsmCdmaCallTracker " + this);
        } else if (this.mRingingCall.isRinging()) {
            log("hangup all conns in background call");
            hangupAllConnections(call);
        } else {
            log("(backgnd) hangup waiting/background");
            if (this.mWaitForHoldToHangupRequest.isWaitToHangup()) {
                this.mWaitForHoldToHangupRequest.setToHangup(call);
            } else {
                hangupWaitingOrBackground();
            }
        }
        call.onHangupLocal();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public void hangupWaitingOrBackground() {
        log("hangupWaitingOrBackground");
        this.mCi.hangupWaitingOrBackground(obtainCompleteMessage(1002));
    }

    public void hangupForegroundResumeBackground() {
        log("hangupForegroundResumeBackground");
        this.mCi.hangupForegroundResumeBackground(obtainCompleteMessage(1002));
    }

    public void hangupConnectionByIndex(GsmCdmaCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            if (cn.getState() == Call.State.DISCONNECTED) {
                proprietaryLog("hangupConnectionByIndex: hangup a DISCONNECTED conn");
            } else if (cn.getGsmCdmaIndex() == index) {
                this.mCi.hangupConnection(index, obtainCompleteMessage());
                return;
            }
        }
        throw new CallStateException("no GsmCdma index found");
    }

    public void hangupAllConnections(GsmCdmaCall call) {
        try {
            int count = call.mConnections.size();
            for (int i = 0; i < count; i++) {
                this.mCi.hangupConnection(((GsmCdmaConnection) call.mConnections.get(i)).getGsmCdmaIndex(), obtainCompleteMessage());
            }
        } catch (CallStateException ex) {
            Rlog.e(LOG_TAG, "hangupConnectionByIndex caught " + ex);
        }
    }

    public GsmCdmaConnection getConnectionByIndex(GsmCdmaCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            if (cn.getGsmCdmaIndex() == index) {
                return cn;
            }
        }
        return null;
    }

    private void notifyCallWaitingInfo(CdmaCallWaitingNotification obj) {
        if (this.mCallWaitingRegistrants != null) {
            this.mCallWaitingRegistrants.notifyRegistrants(new AsyncResult(null, obj, null));
        }
    }

    private void handleCallWaitingInfo(CdmaCallWaitingNotification cw) {
        if (OemConstant.isCallInEnable(this.mPhone) || cw == null) {
            processPlusCodeForWaitingCall(cw);
            if (shouldNotifyWaitingCall(cw)) {
                GsmCdmaConnection gsmCdmaConnection = new GsmCdmaConnection(this.mPhone.getContext(), cw, this, this.mRingingCall);
                updatePhoneState();
                notifyCallWaitingInfo(cw);
                return;
            }
            return;
        }
        log("oppo.leon handleCallWaitingInfo block the second incoming and hangup it ok!" + cw);
        if (cw.isPresent == 1) {
            this.mCi.hangupWaitingOrBackground(null);
        }
    }

    private SuppService getFailedService(int what) {
        switch (what) {
            case 8:
                return SuppService.SWITCH;
            case 11:
                return SuppService.CONFERENCE;
            case 12:
                return SuppService.SEPARATE;
            case 13:
                return SuppService.TRANSFER;
            default:
                return SuppService.UNKNOWN;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void handleMessage(android.os.Message r22) {
        /*
        r21 = this;
        r0 = r21;
        r2 = r0.mHelper;
        r0 = r22;
        r3 = r0.what;
        r2.LogerMessage(r3);
        r0 = r22;
        r2 = r0.what;
        switch(r2) {
            case 1: goto L_0x004a;
            case 2: goto L_0x02a2;
            case 3: goto L_0x02a2;
            case 4: goto L_0x00a7;
            case 5: goto L_0x01db;
            case 8: goto L_0x0126;
            case 9: goto L_0x02bb;
            case 10: goto L_0x02c0;
            case 11: goto L_0x00b6;
            case 12: goto L_0x00c9;
            case 13: goto L_0x00c9;
            case 14: goto L_0x02c5;
            case 15: goto L_0x03a9;
            case 16: goto L_0x0405;
            case 20: goto L_0x0461;
            case 900: goto L_0x0560;
            case 1000: goto L_0x04f2;
            case 1001: goto L_0x0501;
            case 1002: goto L_0x04d6;
            case 1003: goto L_0x04db;
            case 1004: goto L_0x0523;
            case 1005: goto L_0x0549;
            default: goto L_0x0012;
        };
    L_0x0012:
        r2 = new java.lang.RuntimeException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "unexpected event ";
        r3 = r3.append(r4);
        r0 = r22;
        r4 = r0.what;
        r3 = r3.append(r4);
        r4 = " not handled by ";
        r3 = r3.append(r4);
        r4 = "phone type ";
        r3 = r3.append(r4);
        r0 = r21;
        r4 = r0.mPhone;
        r4 = r4.getPhoneType();
        r3 = r3.append(r4);
        r3 = r3.toString();
        r2.<init>(r3);
        throw r2;
    L_0x004a:
        r2 = "GsmCdmaCallTracker";
        r3 = "Event EVENT_POLL_CALLS_RESULT Received";
        android.telephony.Rlog.d(r2, r3);
        r0 = r21;
        r2 = r0.mLastRelevantPoll;
        r0 = r22;
        if (r0 != r2) goto L_0x009b;
    L_0x005b:
        r2 = DBG_POLL;
        if (r2 == 0) goto L_0x0067;
    L_0x005f:
        r2 = "handle EVENT_POLL_CALL_RESULT: set needsPoll=F";
        r0 = r21;
        r0.log(r2);
    L_0x0067:
        r2 = 0;
        r0 = r21;
        r0.mNeedsPoll = r2;
        r2 = 0;
        r0 = r21;
        r0.mLastRelevantPoll = r2;
        r0 = r22;
        r2 = r0.obj;
        r2 = (android.os.AsyncResult) r2;
        r0 = r21;
        r0.handlePollCalls(r2);
        r0 = r21;
        r2 = r0.mWaitForHoldToHangupRequest;
        r2 = r2.isHoldDone();
        if (r2 == 0) goto L_0x0095;
    L_0x0086:
        r2 = "Switch ends, and poll call done, then resume hangup";
        r0 = r21;
        r0.proprietaryLog(r2);
        r0 = r21;
        r2 = r0.mWaitForHoldToHangupRequest;
        r2.resumeHangupAfterHold();
    L_0x0095:
        r2 = 0;
        r0 = r21;
        r0.bAllCallsDisconnectedButNotHandled = r2;
    L_0x009a:
        return;
    L_0x009b:
        r0 = r22;
        r2 = r0.obj;
        r2 = (android.os.AsyncResult) r2;
        r0 = r21;
        r0.CheckIfCallDisconnectButNotHandled(r2);
        goto L_0x009a;
    L_0x00a7:
        r21.operationComplete();
        r0 = r21;
        r2 = r0.hasPendingReplaceRequest;
        if (r2 == 0) goto L_0x009a;
    L_0x00b0:
        r2 = 0;
        r0 = r21;
        r0.hasPendingReplaceRequest = r2;
        goto L_0x009a;
    L_0x00b6:
        r2 = r21.isPhoneTypeGsm();
        if (r2 == 0) goto L_0x00c9;
    L_0x00bc:
        r0 = r21;
        r2 = r0.mForegroundCall;
        r12 = r2.getLatestConnection();
        if (r12 == 0) goto L_0x00c9;
    L_0x00c6:
        r12.onConferenceMergeFailed();
    L_0x00c9:
        r2 = r21.isPhoneTypeGsm();
        if (r2 == 0) goto L_0x00ee;
    L_0x00cf:
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r2 = r8.exception;
        if (r2 == 0) goto L_0x00ea;
    L_0x00d9:
        r0 = r21;
        r2 = r0.mPhone;
        r0 = r22;
        r3 = r0.what;
        r0 = r21;
        r3 = r0.getFailedService(r3);
        r2.notifySuppServiceFailed(r3);
    L_0x00ea:
        r21.operationComplete();
        goto L_0x009a;
    L_0x00ee:
        r2 = new java.lang.RuntimeException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "unexpected event ";
        r3 = r3.append(r4);
        r0 = r22;
        r4 = r0.what;
        r3 = r3.append(r4);
        r4 = " not handled by ";
        r3 = r3.append(r4);
        r4 = "phone type ";
        r3 = r3.append(r4);
        r0 = r21;
        r4 = r0.mPhone;
        r4 = r4.getPhoneType();
        r3 = r3.append(r4);
        r3 = r3.toString();
        r2.<init>(r3);
        throw r2;
    L_0x0126:
        r2 = r21.isPhoneTypeGsm();
        if (r2 == 0) goto L_0x009a;
    L_0x012c:
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r2 = r8.exception;
        if (r2 == 0) goto L_0x01af;
    L_0x0136:
        r0 = r21;
        r2 = r0.mWaitForHoldToRedialRequest;
        r2 = r2.isWaitToRedial();
        if (r2 == 0) goto L_0x016c;
    L_0x0140:
        r0 = r21;
        r2 = r0.mPendingMO;
        if (r2 == 0) goto L_0x0162;
    L_0x0146:
        r0 = r21;
        r2 = r0.mPendingMO;
        r3 = 3;
        r2.mCause = r3;
        r0 = r21;
        r2 = r0.mPendingMO;
        r3 = 3;
        r2.onDisconnect(r3);
        r2 = 0;
        r0 = r21;
        r0.mPendingMO = r2;
        r2 = 0;
        r0 = r21;
        r0.mHangupPendingMO = r2;
        r21.updatePhoneState();
    L_0x0162:
        r21.resumeBackgroundAfterDialFailed();
        r0 = r21;
        r2 = r0.mWaitForHoldToRedialRequest;
        r2.resetToRedial();
    L_0x016c:
        r0 = r21;
        r2 = r0.isOemSwitchAccept;
        if (r2 == 0) goto L_0x019d;
    L_0x0172:
        r0 = r22;
        r2 = r0.what;
        r0 = r21;
        r0.mOemLastMsg = r2;
    L_0x017a:
        r0 = r21;
        r2 = r0.mWaitForHoldToHangupRequest;
        r2 = r2.isWaitToHangup();
        if (r2 == 0) goto L_0x0193;
    L_0x0184:
        r2 = "Switch ends, wait for poll call done to hangup";
        r0 = r21;
        r0.proprietaryLog(r2);
        r0 = r21;
        r2 = r0.mWaitForHoldToHangupRequest;
        r2.setHoldDone();
    L_0x0193:
        r2 = 0;
        r0 = r21;
        r0.mHasPendingSwapRequest = r2;
        r21.operationComplete();
        goto L_0x009a;
    L_0x019d:
        r0 = r21;
        r2 = r0.mPhone;
        r0 = r22;
        r3 = r0.what;
        r0 = r21;
        r3 = r0.getFailedService(r3);
        r2.notifySuppServiceFailed(r3);
        goto L_0x017a;
    L_0x01af:
        r2 = "accept the call 2!";
        r0 = r21;
        r0.log(r2);
        r2 = 0;
        r0 = r21;
        r0.isOemSwitchAccept = r2;
        r2 = -1;
        r0 = r21;
        r0.mOemLastMsg = r2;
        r0 = r21;
        r2 = r0.mWaitForHoldToRedialRequest;
        r2 = r2.isWaitToRedial();
        if (r2 == 0) goto L_0x017a;
    L_0x01cb:
        r2 = "Switch success, then resume dial";
        r0 = r21;
        r0.proprietaryLog(r2);
        r0 = r21;
        r2 = r0.mWaitForHoldToRedialRequest;
        r2.resumeDialAfterHold();
        goto L_0x017a;
    L_0x01db:
        r20 = 0;
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r21.operationComplete();
        r2 = r8.exception;
        if (r2 == 0) goto L_0x0266;
    L_0x01ea:
        r9 = 16;
        r2 = "GsmCdmaCallTracker";
        r3 = "Exception during getLastCallFailCause, assuming normal disconnect";
        android.telephony.Rlog.i(r2, r3);
    L_0x01f5:
        r2 = 34;
        if (r9 == r2) goto L_0x01fd;
    L_0x01f9:
        r2 = 41;
        if (r9 != r2) goto L_0x0271;
    L_0x01fd:
        r0 = r21;
        r2 = r0.mPhone;
        r17 = r2.getCellLocation();
        r10 = -1;
        if (r17 == 0) goto L_0x0214;
    L_0x0208:
        r2 = r21.isPhoneTypeGsm();
        if (r2 == 0) goto L_0x0288;
    L_0x020e:
        r17 = (android.telephony.gsm.GsmCellLocation) r17;
        r10 = r17.getCid();
    L_0x0214:
        r2 = 3;
        r2 = new java.lang.Object[r2];
        r3 = java.lang.Integer.valueOf(r9);
        r4 = 0;
        r2[r4] = r3;
        r3 = java.lang.Integer.valueOf(r10);
        r4 = 1;
        r2[r4] = r3;
        r3 = android.telephony.TelephonyManager.getDefault();
        r0 = r21;
        r4 = r0.mPhone;
        r4 = r4.getSubId();
        r3 = r3.getNetworkType(r4);
        r3 = java.lang.Integer.valueOf(r3);
        r4 = 2;
        r2[r4] = r3;
        r3 = 50106; // 0xc3ba float:7.0213E-41 double:2.47557E-319;
        android.util.EventLog.writeEvent(r3, r2);
    L_0x0242:
        r16 = 0;
        r0 = r21;
        r2 = r0.mDroppedDuringPoll;
        r18 = r2.size();
    L_0x024c:
        r0 = r16;
        r1 = r18;
        if (r0 >= r1) goto L_0x028f;
    L_0x0252:
        r0 = r21;
        r2 = r0.mDroppedDuringPoll;
        r0 = r16;
        r11 = r2.get(r0);
        r11 = (com.android.internal.telephony.GsmCdmaConnection) r11;
        r0 = r20;
        r11.onRemoteDisconnect(r9, r0);
        r16 = r16 + 1;
        goto L_0x024c;
    L_0x0266:
        r15 = r8.result;
        r15 = (com.android.internal.telephony.LastCallFailCause) r15;
        r9 = r15.causeCode;
        r0 = r15.vendorCause;
        r20 = r0;
        goto L_0x01f5;
    L_0x0271:
        r2 = 42;
        if (r9 == r2) goto L_0x01fd;
    L_0x0275:
        r2 = 44;
        if (r9 == r2) goto L_0x01fd;
    L_0x0279:
        r2 = 49;
        if (r9 == r2) goto L_0x01fd;
    L_0x027d:
        r2 = 58;
        if (r9 == r2) goto L_0x01fd;
    L_0x0281:
        r2 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        if (r9 != r2) goto L_0x0242;
    L_0x0286:
        goto L_0x01fd;
    L_0x0288:
        r17 = (android.telephony.cdma.CdmaCellLocation) r17;
        r10 = r17.getBaseStationId();
        goto L_0x0214;
    L_0x028f:
        r21.updatePhoneState();
        r0 = r21;
        r2 = r0.mPhone;
        r2.notifyPreciseCallStateChanged();
        r0 = r21;
        r2 = r0.mDroppedDuringPoll;
        r2.clear();
        goto L_0x009a;
    L_0x02a2:
        r0 = r21;
        r2 = r0.mPhone;
        r2 = r2.isSRVCC();
        if (r2 == 0) goto L_0x02b6;
    L_0x02ac:
        r0 = r21;
        r2 = r0.mPhone;
        r3 = 1;
        r2.setPeningSRVCC(r3);
        goto L_0x009a;
    L_0x02b6:
        r21.pollCallsWhenSafe();
        goto L_0x009a;
    L_0x02bb:
        r21.handleRadioAvailable();
        goto L_0x009a;
    L_0x02c0:
        r21.handleRadioNotAvailable();
        goto L_0x009a;
    L_0x02c5:
        r0 = r21;
        r2 = r0.mPendingCallInEcm;
        if (r2 == 0) goto L_0x0354;
    L_0x02cb:
        r0 = r22;
        r2 = r0.obj;
        r2 = (android.os.AsyncResult) r2;
        r13 = r2.userObj;
        r13 = (java.lang.String) r13;
        r0 = r21;
        r2 = r0.mPendingMO;
        if (r2 != 0) goto L_0x02f5;
    L_0x02db:
        r2 = new com.android.internal.telephony.GsmCdmaConnection;
        r0 = r21;
        r3 = r0.mPhone;
        r0 = r21;
        r4 = r0.checkForTestEmergencyNumber(r13);
        r0 = r21;
        r6 = r0.mForegroundCall;
        r7 = 0;
        r5 = r21;
        r2.<init>(r3, r4, r5, r6, r7);
        r0 = r21;
        r0.mPendingMO = r2;
    L_0x02f5:
        r2 = r21.isPhoneTypeGsm();
        if (r2 != 0) goto L_0x035f;
    L_0x02fb:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r0 = r21;
        r3 = r0.mPendingMO;
        r3 = r3.getAddress();
        r2 = r2.append(r3);
        r3 = ",";
        r2 = r2.append(r3);
        r3 = android.telephony.PhoneNumberUtils.extractNetworkPortionAlt(r13);
        r2 = r2.append(r3);
        r19 = r2.toString();
        r0 = r21;
        r2 = r0.mCi;
        r0 = r21;
        r3 = r0.mPendingCallClirMode;
        r4 = r21.obtainCompleteMessage();
        r0 = r19;
        r2.dial(r0, r3, r4);
        r0 = r21;
        r2 = r0.mCdmaCallTrackerExt;
        if (r2 == 0) goto L_0x034f;
    L_0x0336:
        r0 = r21;
        r2 = r0.mCdmaCallTrackerExt;
        r3 = com.android.internal.telephony.GsmCdmaConnection.formatDialString(r13);
        r2 = r2.needToConvert(r13, r3);
        if (r2 == 0) goto L_0x034f;
    L_0x0344:
        r0 = r21;
        r2 = r0.mPendingMO;
        r3 = android.telephony.PhoneNumberUtils.extractNetworkPortionAlt(r13);
        r2.setConverted(r3);
    L_0x034f:
        r2 = 0;
        r0 = r21;
        r0.mPendingCallInEcm = r2;
    L_0x0354:
        r0 = r21;
        r2 = r0.mPhone;
        r0 = r21;
        r2.unsetOnEcbModeExitResponse(r0);
        goto L_0x009a;
    L_0x035f:
        r2 = "GsmCdmaCallTracker";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "originally unexpected event ";
        r3 = r3.append(r4);
        r0 = r22;
        r4 = r0.what;
        r3 = r3.append(r4);
        r4 = " not handled by phone type ";
        r3 = r3.append(r4);
        r0 = r21;
        r4 = r0.mPhone;
        r4 = r4.getPhoneType();
        r3 = r3.append(r4);
        r3 = r3.toString();
        android.telephony.Rlog.e(r2, r3);
        r0 = r21;
        r2 = r0.mCi;
        r0 = r21;
        r3 = r0.mPendingMO;
        r3 = r3.getAddress();
        r0 = r21;
        r4 = r0.mPendingCallClirMode;
        r5 = r21.obtainCompleteMessage();
        r6 = 0;
        r2.dial(r3, r4, r6, r5);
        goto L_0x034f;
    L_0x03a9:
        r2 = r21.isPhoneTypeGsm();
        if (r2 != 0) goto L_0x03cd;
    L_0x03af:
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r2 = r8.exception;
        if (r2 != 0) goto L_0x009a;
    L_0x03b9:
        r2 = r8.result;
        r2 = (com.android.internal.telephony.cdma.CdmaCallWaitingNotification) r2;
        r0 = r21;
        r0.handleCallWaitingInfo(r2);
        r2 = "GsmCdmaCallTracker";
        r3 = "Event EVENT_CALL_WAITING_INFO_CDMA Received";
        android.telephony.Rlog.d(r2, r3);
        goto L_0x009a;
    L_0x03cd:
        r2 = new java.lang.RuntimeException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "unexpected event ";
        r3 = r3.append(r4);
        r0 = r22;
        r4 = r0.what;
        r3 = r3.append(r4);
        r4 = " not handled by ";
        r3 = r3.append(r4);
        r4 = "phone type ";
        r3 = r3.append(r4);
        r0 = r21;
        r4 = r0.mPhone;
        r4 = r4.getPhoneType();
        r3 = r3.append(r4);
        r3 = r3.toString();
        r2.<init>(r3);
        throw r2;
    L_0x0405:
        r2 = r21.isPhoneTypeGsm();
        if (r2 != 0) goto L_0x0429;
    L_0x040b:
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r2 = r8.exception;
        if (r2 != 0) goto L_0x009a;
    L_0x0415:
        r0 = r21;
        r2 = r0.mPendingMO;
        if (r2 == 0) goto L_0x0422;
    L_0x041b:
        r0 = r21;
        r2 = r0.mPendingMO;
        r2.onConnectedInOrOut();
    L_0x0422:
        r2 = 0;
        r0 = r21;
        r0.mPendingMO = r2;
        goto L_0x009a;
    L_0x0429:
        r2 = new java.lang.RuntimeException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "unexpected event ";
        r3 = r3.append(r4);
        r0 = r22;
        r4 = r0.what;
        r3 = r3.append(r4);
        r4 = " not handled by ";
        r3 = r3.append(r4);
        r4 = "phone type ";
        r3 = r3.append(r4);
        r0 = r21;
        r4 = r0.mPhone;
        r4 = r4.getPhoneType();
        r3 = r3.append(r4);
        r3 = r3.toString();
        r2.<init>(r3);
        throw r2;
    L_0x0461:
        r2 = r21.isPhoneTypeGsm();
        if (r2 != 0) goto L_0x049e;
    L_0x0467:
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r2 = r8.exception;
        if (r2 != 0) goto L_0x048e;
    L_0x0471:
        r0 = r22;
        r2 = r0.obj;
        r2 = (android.os.AsyncResult) r2;
        r13 = r2.userObj;
        r13 = (java.lang.String) r13;
        r2 = new com.android.internal.telephony.GsmCdmaCallTracker$2;
        r0 = r21;
        r2.<init>(r0, r13);
        r0 = r21;
        r3 = r0.m3WayCallFlashDelay;
        r4 = (long) r3;
        r0 = r21;
        r0.postDelayed(r2, r4);
        goto L_0x009a;
    L_0x048e:
        r2 = 0;
        r0 = r21;
        r0.mPendingMO = r2;
        r2 = "GsmCdmaCallTracker";
        r3 = "exception happened on Blank Flash for 3-way call";
        android.telephony.Rlog.w(r2, r3);
        goto L_0x009a;
    L_0x049e:
        r2 = new java.lang.RuntimeException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "unexpected event ";
        r3 = r3.append(r4);
        r0 = r22;
        r4 = r0.what;
        r3 = r3.append(r4);
        r4 = " not handled by ";
        r3 = r3.append(r4);
        r4 = "phone type ";
        r3 = r3.append(r4);
        r0 = r21;
        r4 = r0.mPhone;
        r4 = r4.getPhoneType();
        r3 = r3.append(r4);
        r3 = r3.toString();
        r2.<init>(r3);
        throw r2;
    L_0x04d6:
        r21.operationComplete();
        goto L_0x009a;
    L_0x04db:
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r2 = r8.exception;
        if (r2 == 0) goto L_0x04ed;
    L_0x04e5:
        r2 = "dial call failed!!";
        r0 = r21;
        r0.proprietaryLog(r2);
    L_0x04ed:
        r21.operationComplete();
        goto L_0x009a;
    L_0x04f2:
        r0 = r21;
        r3 = r0.mHelper;
        r0 = r22;
        r2 = r0.obj;
        r2 = (android.os.AsyncResult) r2;
        r3.CallIndicationProcess(r2);
        goto L_0x009a;
    L_0x0501:
        r2 = "Receives EVENT_RADIO_OFF_OR_NOT_AVAILABLE";
        r0 = r21;
        r0.proprietaryLog(r2);
        r2 = new android.os.AsyncResult;
        r3 = new com.android.internal.telephony.CommandException;
        r4 = com.android.internal.telephony.CommandException.Error.RADIO_NOT_AVAILABLE;
        r3.<init>(r4);
        r4 = 0;
        r5 = 0;
        r2.<init>(r4, r5, r3);
        r0 = r21;
        r0.handlePollCalls(r2);
        r2 = 0;
        r0 = r21;
        r0.mLastRelevantPoll = r2;
        goto L_0x009a;
    L_0x0523:
        r2 = "Receives EVENT_ECONF_SRVCC_INDICATION";
        r0 = r21;
        r0.proprietaryLog(r2);
        r2 = r21.hasParsingCEPCapability();
        if (r2 != 0) goto L_0x009a;
    L_0x0531:
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r2 = r8.result;
        r2 = (int[]) r2;
        r0 = r21;
        r0.mEconfSrvccConnectionIds = r2;
        r2 = 0;
        r0 = r21;
        r0.mNeedWaitImsEConfSrvcc = r2;
        r21.pollCallsWhenSafe();
        goto L_0x009a;
    L_0x0549:
        r0 = r22;
        r8 = r0.obj;
        r8 = (android.os.AsyncResult) r8;
        r2 = r8.exception;
        if (r2 != 0) goto L_0x009a;
    L_0x0553:
        r21.handleCallAccepted();
        r2 = "EVENT_CDMA_CALL_ACCEPTED";
        r0 = r21;
        r0.proprietaryLog(r2);
        goto L_0x009a;
    L_0x0560:
        r2 = "GsmCdmaCallTracker";
        r3 = "acceptCall: for test card...OK";
        android.telephony.Rlog.d(r2, r3);
        r21.acceptCall();	 Catch:{ Exception -> 0x056e }
        goto L_0x009a;
    L_0x056e:
        r14 = move-exception;
        goto L_0x009a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaCallTracker.handleMessage(android.os.Message):void");
    }

    private void checkAndEnableDataCallAfterEmergencyCallDropped() {
        if (this.mIsInEmergencyCall) {
            this.mIsInEmergencyCall = false;
            String inEcm = TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "ril.cdma.inecmmode", "false");
            log("checkAndEnableDataCallAfterEmergencyCallDropped,inEcm=" + inEcm);
            if (inEcm.compareTo("false") == 0) {
                this.mPhone.mDcTracker.setInternalDataEnabled(true);
                this.mPhone.notifyEmergencyCallRegistrants(false);
            }
            this.mPhone.sendEmergencyCallStateChange(false);
        }
    }

    private Connection checkMtFindNewRinging(DriverCall dc, int i) {
        if (this.mConnections[i].getCall() == this.mRingingCall) {
            Connection newRinging = this.mConnections[i];
            log("Notify new ring " + dc);
            return newRinging;
        }
        Rlog.e(LOG_TAG, "Phantom call appeared " + dc);
        if (dc.state == DriverCall.State.ALERTING || dc.state == DriverCall.State.DIALING) {
            return null;
        }
        this.mConnections[i].onConnectedInOrOut();
        if (dc.state != DriverCall.State.HOLDING) {
            return null;
        }
        this.mConnections[i].onStartedHolding();
        return null;
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    private boolean isPhoneTypeGsm() {
        return this.mPhone.getPhoneType() == 1;
    }

    public GsmCdmaPhone getPhone() {
        return this.mPhone;
    }

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[GsmCdmaCallTracker] " + msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        Object[] objArr;
        pw.println("GsmCdmaCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println("mConnections: length=" + this.mConnections.length);
        for (i = 0; i < this.mConnections.length; i++) {
            objArr = new Object[2];
            objArr[0] = Integer.valueOf(i);
            objArr[1] = this.mConnections[i];
            pw.printf("  mConnections[%d]=%s\n", objArr);
        }
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        if (!isPhoneTypeGsm()) {
            pw.println(" mCallWaitingRegistrants=" + this.mCallWaitingRegistrants);
        }
        pw.println(" mDroppedDuringPoll: size=" + this.mDroppedDuringPoll.size());
        for (i = 0; i < this.mDroppedDuringPoll.size(); i++) {
            objArr = new Object[2];
            objArr[0] = Integer.valueOf(i);
            objArr[1] = this.mDroppedDuringPoll.get(i);
            pw.printf("  mDroppedDuringPoll[%d]=%s\n", objArr);
        }
        pw.println(" mRingingCall=" + this.mRingingCall);
        pw.println(" mForegroundCall=" + this.mForegroundCall);
        pw.println(" mBackgroundCall=" + this.mBackgroundCall);
        pw.println(" mPendingMO=" + this.mPendingMO);
        pw.println(" mHangupPendingMO=" + this.mHangupPendingMO);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDesiredMute=" + this.mDesiredMute);
        pw.println(" mState=" + this.mState);
        if (!isPhoneTypeGsm()) {
            pw.println(" mPendingCallInEcm=" + this.mPendingCallInEcm);
            pw.println(" mIsInEmergencyCall=" + this.mIsInEmergencyCall);
            pw.println(" mPendingCallClirMode=" + this.mPendingCallClirMode);
            pw.println(" mIsEcmTimerCanceled=" + this.mIsEcmTimerCanceled);
        }
    }

    public State getState() {
        return this.mState;
    }

    public int getMaxConnectionsPerCall() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return 5;
        }
        return 1;
    }

    public void hangupAll() throws CallStateException {
        proprietaryLog("hangupAll");
        this.mCi.hangupAll(obtainCompleteMessage());
        if (!this.mRingingCall.isIdle()) {
            this.mRingingCall.onHangupLocal();
        }
        if (!this.mForegroundCall.isIdle()) {
            this.mForegroundCall.onHangupLocal();
        }
        if (!this.mBackgroundCall.isIdle()) {
            this.mBackgroundCall.onHangupLocal();
        }
    }

    private boolean canVtDial() {
        int networkType = this.mPhone.getServiceState().getVoiceNetworkType();
        proprietaryLog("networkType=" + TelephonyManager.getNetworkTypeName(networkType));
        if (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15 || networkType == 13) {
            return true;
        }
        return false;
    }

    public synchronized Connection vtDial(String dialString, int clirMode, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        clearDisconnected();
        if (!canDial()) {
            throw new CallStateException("cannot dial in current state");
        } else if (canVtDial()) {
            String origNumber = dialString;
            dialString = convertNumberIfNecessary(this.mPhone, dialString);
            if (this.mForegroundCall.getState() != Call.State.IDLE || this.mRingingCall.getState().isRinging()) {
                throw new CallStateException("cannot vtDial since non-IDLE call already exists");
            }
            boolean isEmergencyCall;
            if (!hasC2kOverImsModem() || TelephonyManager.getDefault().hasIccCard(this.mPhone.getPhoneId())) {
                isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), this.mPhone.getSubId(), dialString);
            } else {
                isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyCall);
            this.mHangupPendingMO = false;
            this.mPendingMO.mIsVideo = true;
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
            } else {
                setMute(false);
                this.mCi.vtDial(this.mPendingMO.getAddress(), clirMode, uusInfo, obtainCompleteMessage());
                this.mPendingMO.setVideoState(3);
                GsmVTProviderUtil.setContext(this.mPhone.getContext());
                this.mForegroundCall.mVTProvider = new GsmVTProvider();
                proprietaryLog("vtDial new GsmVTProvider");
                try {
                    IGsmVideoCallProvider gsmVideoCallProvider = this.mForegroundCall.mVTProvider.getInterface();
                    if (gsmVideoCallProvider != null) {
                        GsmVideoCallProviderWrapper gsmVideoCallProviderWrapper = new GsmVideoCallProviderWrapper(gsmVideoCallProvider);
                        proprietaryLog("vtDial new GsmVideoCallProviderWrapper");
                        this.mPendingMO.setVideoProvider(gsmVideoCallProviderWrapper);
                    }
                } catch (RemoteException e) {
                    Rlog.e(PROP_LOG_TAG, "vtDial new GsmVideoCallProviderWrapper failed");
                }
            }
            if (this.mNumberConverted) {
                this.mPendingMO.setConverted(origNumber);
                this.mNumberConverted = false;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot vtDial under non 3/4G network");
        }
        return this.mPendingMO;
    }

    public Connection vtDial(String dialString, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        return vtDial(dialString, 0, uusInfo, intentExtras);
    }

    public void acceptCall(int videoState) throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            Rlog.i("phone", "acceptCall: incoming...");
            setMute(false);
            GsmCdmaConnection cn = (GsmCdmaConnection) this.mRingingCall.mConnections.get(0);
            if (cn.isVideo() && videoState == 0) {
                this.mCi.acceptVtCallWithVoiceOnly(cn.getGsmCdmaIndex(), obtainCompleteMessage());
                cn.setVideoState(0);
                return;
            }
            this.mCi.acceptCall(obtainCompleteMessage());
        } else if (this.mRingingCall.getState() == Call.State.WAITING) {
            if (isPhoneTypeGsm()) {
                setMute(false);
                if (((GsmCdmaConnection) this.mRingingCall.mConnections.get(0)).isVideo()) {
                    GsmCdmaConnection fgCn = (GsmCdmaConnection) this.mForegroundCall.mConnections.get(0);
                    if (fgCn != null && fgCn.isVideo()) {
                        this.hasPendingReplaceRequest = true;
                        this.mCi.replaceVtCall(fgCn.mIndex + 1, obtainCompleteMessage());
                        fgCn.onHangupLocal();
                        return;
                    }
                }
            }
            GsmCdmaConnection cwConn = (GsmCdmaConnection) this.mRingingCall.getLatestConnection();
            cwConn.updateParent(this.mRingingCall, this.mForegroundCall);
            cwConn.onConnectedInOrOut();
            updatePhoneState();
            switchWaitingOrHoldingAndActive();
        } else {
            throw new CallStateException("phone not ringing");
        }
    }

    private void resumeBackgroundAfterDialFailed() {
        List<Connection> connCopy = (List) this.mBackgroundCall.mConnections.clone();
        int s = connCopy.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) connCopy.get(i)).resumeHoldAfterDialFailed();
        }
    }

    private synchronized boolean restoreConferenceParticipantAddress() {
        if (this.mEconfSrvccConnectionIds == null) {
            proprietaryLog("SRVCC: restoreConferenceParticipantAddress():ignore because mEconfSrvccConnectionIds is empty");
            return false;
        }
        boolean finishRestore = false;
        int numOfParticipants = this.mEconfSrvccConnectionIds[0];
        for (int index = 1; index <= numOfParticipants; index++) {
            GsmCdmaConnection participantConnection = this.mConnections[this.mEconfSrvccConnectionIds[index] - 1];
            if (participantConnection != null) {
                proprietaryLog("SRVCC: found conference connections!");
                if (participantConnection.mOrigConnection instanceof ImsPhoneConnection) {
                    ImsPhoneConnection hostConnection = participantConnection.mOrigConnection;
                    if (hostConnection == null) {
                        proprietaryLog("SRVCC: no host, ignore connection: " + participantConnection);
                    } else {
                        String address = hostConnection.getConferenceParticipantAddress(index - 1);
                        participantConnection.updateConferenceParticipantAddress(address);
                        finishRestore = true;
                        proprietaryLog("SRVCC: restore Connection=" + participantConnection + " with address:" + address);
                    }
                } else {
                    proprietaryLog("SRVCC: host is abnormal, ignore connection: " + participantConnection);
                }
            }
        }
        return finishRestore;
    }

    protected Connection getHoConnection(DriverCall dc) {
        if (dc == null) {
            return null;
        }
        if (!(this.mEconfSrvccConnectionIds == null || dc == null)) {
            int numOfParticipants = this.mEconfSrvccConnectionIds[0];
            for (int index = 1; index <= numOfParticipants; index++) {
                if (dc.index == this.mEconfSrvccConnectionIds[index]) {
                    proprietaryLog("SRVCC: getHoConnection for call-id:" + dc.index + " in a conference is found!");
                    if (this.mImsConfHostConnection == null) {
                        proprietaryLog("SRVCC: but mImsConfHostConnection is null, try to find by callState");
                    } else {
                        proprietaryLog("SRVCC: ret= " + this.mImsConfHostConnection);
                        return this.mImsConfHostConnection;
                    }
                }
            }
        }
        return super.getHoConnection(dc);
    }

    boolean hasParsingCEPCapability() {
        HardwareConfig modem = this.mTelDevController.getModem(this.mPhone.getPhoneId());
        if (modem == null) {
            return false;
        }
        return modem.hasParsingCEPCapability();
    }

    private void processPlusCodeForWaitingCall(CdmaCallWaitingNotification cw) {
        String address = cw.number;
        proprietaryLog("processPlusCodeForWaitingCall before format number:" + cw.number);
        if (!(address == null || address.length() <= 0 || this.mCdmaCallTrackerExt == null)) {
            cw.number = this.mCdmaCallTrackerExt.processPlusCodeForWaitingCall(address, cw.numberType);
        }
        proprietaryLog("processPlusCodeForWaitingCall after format number:" + cw.number);
    }

    private boolean shouldNotifyWaitingCall(CdmaCallWaitingNotification cw) {
        String address = cw.number;
        proprietaryLog("shouldNotifyWaitingCall, address:" + address);
        if (address != null && address.length() > 0) {
            GsmCdmaConnection lastRingConn = (GsmCdmaConnection) this.mRingingCall.getLatestConnection();
            if (lastRingConn != null && address.equals(lastRingConn.getAddress())) {
                proprietaryLog("handleCallWaitingInfo, skip duplicate waiting call!");
                return false;
            }
        }
        return true;
    }

    private void handleCallAccepted() {
        List connections = this.mForegroundCall.getConnections();
        int count = connections.size();
        proprietaryLog("handleCallAccepted, fgcall count:" + count);
        if (count == 1 && ((GsmCdmaConnection) connections.get(0)).onCdmaCallAccept()) {
            this.mPhone.notifyCdmaCallAccepted();
        }
    }

    private void CheckIfCallDisconnectButNotHandled(AsyncResult ar) {
        boolean z = false;
        boolean bCallExist = false;
        List polledCalls;
        if (ar.exception == null) {
            polledCalls = ar.result;
        } else {
            polledCalls = new ArrayList();
        }
        for (GsmCdmaConnection conn : this.mConnections) {
            if (conn != null) {
                bCallExist = true;
                break;
            }
        }
        if (bCallExist && polledCalls.size() == 0) {
            z = true;
        }
        this.bAllCallsDisconnectedButNotHandled = z;
    }

    void proprietaryLog(String s) {
        Rlog.d(PROP_LOG_TAG, s);
    }

    public boolean isOemInEcm() {
        return this.mPendingCallInEcm;
    }

    public boolean isOemInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    public void oemClearConn() {
        if (this.mState != State.IDLE) {
            try {
                internalClearDisconnected();
                for (GsmCdmaConnection gsmCdmaConnection : this.mConnections) {
                    if (gsmCdmaConnection != null) {
                        gsmCdmaConnection.dispose();
                    }
                }
                if (this.mPendingMO != null) {
                    this.mPendingMO.dispose();
                }
            } catch (Exception e) {
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mConnections = new GsmCdmaConnection[19];
            } else {
                this.mConnections = new GsmCdmaConnection[8];
            }
            this.mPendingMO = null;
            this.mHangupConn = null;
            this.mHangupPendingMO = false;
            this.mState = State.IDLE;
            Rlog.e(LOG_TAG, "Phantom ims call appeared");
        }
    }
}
