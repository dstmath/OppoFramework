package com.android.ims;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsCall.Listener;
import com.android.ims.ImsConfig.ConfigConstants;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener.Stub;
import com.android.ims.internal.IImsService;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.ImsCallSession;
import com.android.internal.telephony.IPhoneSubInfo;
import com.mediatek.common.MPlugin;
import com.mediatek.common.ims.IImsManagerExt;
import com.mediatek.internal.telephony.ITelephonyEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.gsma.joyn.JoynServiceListener;
import org.gsma.joyn.capability.CapabilityService;
import org.gsma.joyn.chat.ChatService;
import org.gsma.joyn.contacts.ContactsService;
import org.gsma.joyn.ft.FileTransferService;
import org.gsma.joyn.gsh.GeolocSharingService;
import org.gsma.joyn.ish.ImageSharingService;
import org.gsma.joyn.vsh.VideoSharingService;

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
public class ImsManager {
    public static final String ACTION_IMS_INCOMING_CALL = "com.android.ims.IMS_INCOMING_CALL";
    public static final String ACTION_IMS_INCOMING_CALL_INDICATION = "com.android.ims.IMS_INCOMING_CALL_INDICATION";
    public static final String ACTION_IMS_RADIO_STATE_CHANGED = "com.android.ims.IMS_RADIO_STATE_CHANGED";
    public static final String ACTION_IMS_REGISTRATION_ERROR = "com.android.ims.REGISTRATION_ERROR";
    public static final String ACTION_IMS_RTP_INFO = "com.android.ims.IMS_RTP_INFO";
    public static final String ACTION_IMS_SERVICE_DEREGISTERED = "com.android.ims.IMS_SERVICE_DEREGISTERED";
    public static final String ACTION_IMS_SERVICE_DOWN = "com.android.ims.IMS_SERVICE_DOWN";
    public static final String ACTION_IMS_SERVICE_UP = "com.android.ims.IMS_SERVICE_UP";
    public static final String ACTION_IMS_STATE_CHANGED = "com.android.ims.IMS_STATE_CHANGED";
    private static final String[] DATA_ENABLED_PROP = null;
    private static final boolean DBG = true;
    public static final String EXTRA_CALL_ID = "android:imsCallID";
    public static final String EXTRA_CALL_MODE = "android:imsCallMode";
    public static final String EXTRA_DIAL_STRING = "android:imsDialString";
    public static final String EXTRA_IMS_DISABLE_CAP_KEY = "android:disablecap";
    public static final String EXTRA_IMS_ENABLE_CAP_KEY = "android:enablecap";
    public static final String EXTRA_IMS_RADIO_STATE = "android:imsRadioState";
    public static final String EXTRA_IMS_REG_ERROR_KEY = "android:regError";
    public static final String EXTRA_IMS_REG_STATE_KEY = "android:regState";
    public static final String EXTRA_IS_UNKNOWN_CALL = "android:isUnknown";
    public static final String EXTRA_PHONE_ID = "android:phone_id";
    public static final String EXTRA_RTP_NETWORK_ID = "android:rtpNetworkId";
    public static final String EXTRA_RTP_PDN_ID = "android:rtpPdnId";
    public static final String EXTRA_RTP_RECV_PKT_LOST = "android:rtpRecvPktLost";
    public static final String EXTRA_RTP_SEND_PKT_LOST = "android:rtpSendPktLost";
    public static final String EXTRA_RTP_TIMER = "android:rtpTimer";
    public static final String EXTRA_SEQ_NUM = "android:imsSeqNum";
    public static final String EXTRA_SERVICE_ID = "android:imsServiceId";
    public static final String EXTRA_USSD = "android:ussd";
    public static final String FALSE = "false";
    public static final String IMS_SERVICE = "ims";
    public static final int INCOMING_CALL_RESULT_CODE = 101;
    private static final String LTE_SUPPORT = "ro.boot.opt_lte_support";
    private static final String MULTI_IMS_SUPPORT = "ro.mtk_multiple_ims_support";
    public static final String PROPERTY_DBG_ALLOW_IMS_OFF_OVERRIDE = "persist.dbg.allow_ims_off";
    public static final int PROPERTY_DBG_ALLOW_IMS_OFF_OVERRIDE_DEFAULT = 0;
    public static final String PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE = "persist.dbg.volte_avail_ovr";
    public static final int PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE_DEFAULT = 0;
    public static final String PROPERTY_DBG_VT_AVAIL_OVERRIDE = "persist.dbg.vt_avail_ovr";
    public static final int PROPERTY_DBG_VT_AVAIL_OVERRIDE_DEFAULT = 0;
    public static final String PROPERTY_DBG_WFC_AVAIL_OVERRIDE = "persist.dbg.wfc_avail_ovr";
    public static final int PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT = 0;
    private static final String PROPERTY_IMS_EVS_ENABLE = "persist.radio.ims_evs_enable";
    public static final int SERVICE_REG_CAPABILITY_EVENT_ADDED = 1;
    public static final int SERVICE_REG_CAPABILITY_EVENT_ECC_NOT_SUPPORT = 4;
    public static final int SERVICE_REG_CAPABILITY_EVENT_ECC_SUPPORT = 2;
    public static final int SERVICE_REG_CAPABILITY_EVENT_REMOVED = 0;
    private static final String TAG = "ImsManager";
    public static final String TRUE = "true";
    private static final String TTY_MODE = "tty_mode";
    private static final String VILTE_SETTING = "vilte_setting";
    private static final String VOLTE_PROVISIONED_PROP = "net.lte.ims.volte.provisioned";
    private static final String VOLTE_SETTING = "volte_setting";
    private static final String VT_PROVISIONED_PROP = "net.lte.ims.vt.provisioned";
    private static final String WFC_MODE_SETTING = "wfc_mode_setting";
    private static final String WFC_PROVISIONED_PROP = "net.lte.ims.wfc.provisioned";
    private static final String WFC_ROAMING_MODE_SETTING = "wfc_roaming_mode_setting";
    private static final String WFC_ROAMING_SETTING = "wfc_roaming_setting";
    private static final String WFC_SETTING = "wfc_setting";
    private static IImsManagerExt mImsManagerExt;
    private static AtomicInteger oppoImsPhoneId;
    private static HashMap<Integer, ImsManager> sImsManagerInstances;
    private CapabilityService mCapabilitiesApi;
    private ChatService mChatApi;
    private ImsConfig mConfig;
    private boolean mConfigUpdated;
    private ContactsService mContactsApi;
    private Context mContext;
    private ImsServiceDeathRecipient mDeathRecipient;
    private ImsEcbm mEcbm;
    private FileTransferService mFileTransferApi;
    private GeolocSharingService mGeolocSharingApi;
    private ImageSharingService mImageSharingApi;
    private ImsConfigListener mImsConfigListener;
    private IImsService mImsService;
    private ImsMultiEndpoint mMultiEndpoint;
    private int mPhoneId;
    private ImsUt mUt;
    private VideoSharingService mVideoSharingApi;

    private class AsyncUpdateProvisionedValues extends AsyncTask<Void, Void, Void> {
        final /* synthetic */ ImsManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.<init>(com.android.ims.ImsManager):void, dex: 
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
        private AsyncUpdateProvisionedValues(com.android.ims.ImsManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.<init>(com.android.ims.ImsManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.<init>(com.android.ims.ImsManager):void");
        }

        /* synthetic */ AsyncUpdateProvisionedValues(ImsManager this$0, AsyncUpdateProvisionedValues asyncUpdateProvisionedValues) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.getProvisionedBool(com.android.ims.ImsConfig, int):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private boolean getProvisionedBool(com.android.ims.ImsConfig r1, int r2) throws com.android.ims.ImsException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.getProvisionedBool(com.android.ims.ImsConfig, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.getProvisionedBool(com.android.ims.ImsConfig, int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected /* bridge */ /* synthetic */ java.lang.Object doInBackground(java.lang.Object[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.doInBackground(java.lang.Object[]):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.doInBackground(java.lang.Object[]):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.doInBackground(java.lang.Void[]):java.lang.Void, dex: 
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
        protected java.lang.Void doInBackground(java.lang.Void... r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.doInBackground(java.lang.Void[]):java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.AsyncUpdateProvisionedValues.doInBackground(java.lang.Void[]):java.lang.Void");
        }
    }

    private class ImsRegistrationListenerProxy extends Stub {
        private ImsConnectionStateListener mListener;
        private int mServiceClass;
        final /* synthetic */ ImsManager this$0;

        public ImsRegistrationListenerProxy(ImsManager this$0, int serviceClass, ImsConnectionStateListener listener) {
            this.this$0 = this$0;
            this.mServiceClass = serviceClass;
            this.mListener = listener;
        }

        public boolean isSameProxy(int serviceClass) {
            return this.mServiceClass == serviceClass;
        }

        @Deprecated
        public void registrationConnected() {
            ImsManager.log("registrationConnected ::");
            if (this.mListener != null) {
                this.mListener.onImsConnected();
            }
        }

        @Deprecated
        public void registrationProgressing() {
            ImsManager.log("registrationProgressing ::");
            if (this.mListener != null) {
                this.mListener.onImsProgressing();
            }
        }

        public void registrationConnectedWithRadioTech(int imsRadioTech) {
            ImsManager.log("registrationConnectedWithRadioTech :: imsRadioTech=" + imsRadioTech);
            if (this.mListener != null) {
                this.mListener.onImsConnected();
            }
        }

        public void registrationProgressingWithRadioTech(int imsRadioTech) {
            ImsManager.log("registrationProgressingWithRadioTech :: imsRadioTech=" + imsRadioTech);
            if (this.mListener != null) {
                this.mListener.onImsProgressing();
            }
        }

        public void registrationDisconnected(ImsReasonInfo imsReasonInfo) {
            ImsManager.log("registrationDisconnected :: imsReasonInfo" + imsReasonInfo);
            if (this.mListener != null) {
                this.mListener.onImsDisconnected(imsReasonInfo);
            }
        }

        public void registrationResumed() {
            ImsManager.log("registrationResumed ::");
            if (this.mListener != null) {
                this.mListener.onImsResumed();
            }
        }

        public void registrationSuspended() {
            ImsManager.log("registrationSuspended ::");
            if (this.mListener != null) {
                this.mListener.onImsSuspended();
            }
        }

        public void registrationServiceCapabilityChanged(int serviceClass, int event) {
            ImsManager.log("registrationServiceCapabilityChanged :: serviceClass=" + serviceClass + ", event=" + event);
            if (this.mListener != null && event == 1) {
                this.mListener.onImsConnected();
            }
            if (this.mListener != null && event == 2) {
                this.mListener.onImsEmergencyCapabilityChanged(true);
            }
            if (this.mListener != null && event == 4) {
                this.mListener.onImsEmergencyCapabilityChanged(false);
            }
        }

        public void registrationFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
            ImsManager.log("registrationFeatureCapabilityChanged :: serviceClass=" + serviceClass);
            if (this.mListener != null) {
                this.mListener.onFeatureCapabilityChanged(serviceClass, enabledFeatures, disabledFeatures);
            }
        }

        public void voiceMessageCountUpdate(int count) {
            ImsManager.log("voiceMessageCountUpdate :: count=" + count);
            if (this.mListener != null) {
                this.mListener.onVoiceMessageCountChanged(count);
            }
        }

        public void registrationAssociatedUriChanged(Uri[] uris) {
            ImsManager.log("registrationAssociatedUriChanged ::");
            if (this.mListener != null) {
                this.mListener.registrationAssociatedUriChanged(uris);
            }
        }
    }

    private class ImsServiceDeathRecipient implements DeathRecipient {
        final /* synthetic */ ImsManager this$0;

        /* synthetic */ ImsServiceDeathRecipient(ImsManager this$0, ImsServiceDeathRecipient imsServiceDeathRecipient) {
            this(this$0);
        }

        private ImsServiceDeathRecipient(ImsManager this$0) {
            this.this$0 = this$0;
        }

        public void binderDied() {
            this.this$0.mImsService = null;
            this.this$0.mUt = null;
            this.this$0.mConfig = null;
            this.this$0.mEcbm = null;
            this.this$0.mMultiEndpoint = null;
            if (this.this$0.mContext != null) {
                Intent intent = new Intent(ImsManager.ACTION_IMS_SERVICE_DOWN);
                intent.putExtra(ImsManager.EXTRA_PHONE_ID, this.this$0.mPhoneId);
                this.this$0.mContext.sendBroadcast(new Intent(intent));
            }
        }
    }

    public class MyServiceListener implements JoynServiceListener {
        final /* synthetic */ ImsManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.ims.ImsManager.MyServiceListener.<init>(com.android.ims.ImsManager):void, dex: 
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
        public MyServiceListener(com.android.ims.ImsManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.ims.ImsManager.MyServiceListener.<init>(com.android.ims.ImsManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.MyServiceListener.<init>(com.android.ims.ImsManager):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsManager.MyServiceListener.onServiceConnected():void, dex: 
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
        public void onServiceConnected() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsManager.MyServiceListener.onServiceConnected():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.MyServiceListener.onServiceConnected():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.ims.ImsManager.MyServiceListener.onServiceDisconnected(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onServiceDisconnected(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.ims.ImsManager.MyServiceListener.onServiceDisconnected(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.MyServiceListener.onServiceDisconnected(int):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.<clinit>():void");
    }

    public static ImsManager getInstance(Context context, int phoneId) {
        log("getInstance() : phoneId" + phoneId);
        synchronized (sImsManagerInstances) {
            if (sImsManagerInstances.containsKey(Integer.valueOf(phoneId))) {
                ImsManager imsManager = (ImsManager) sImsManagerInstances.get(Integer.valueOf(phoneId));
                return imsManager;
            }
            ImsManager mgr = new ImsManager(context, phoneId);
            sImsManagerInstances.put(Integer.valueOf(phoneId), mgr);
            return mgr;
        }
    }

    public static boolean isEnhanced4gLteModeSettingEnabledByUser(Context context) {
        return isEnhanced4gLteModeSettingEnabledByUser(context, getMainCapabilityPhoneId(context));
    }

    public static boolean isEnhanced4gLteModeSettingEnabledByUser(Context context, int phoneId) {
        int enabled;
        boolean z = true;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            if (!getBooleanCarrierConfig(context, "editable_enhanced_4g_lte_bool")) {
                return true;
            }
            int i;
            ContentResolver contentResolver = context.getContentResolver();
            String str = "volte_vt_enabled";
            if (!SystemProperties.get("persist.mtk_ct_volte_support").equals("1") || getBooleanCarrierConfig(context, "default_enhanced_4g_mode_enabled_bool")) {
                i = 1;
            } else {
                i = 0;
            }
            enabled = Global.getInt(contentResolver, str, i);
        } else if (!getBooleanCarrierConfig(context, "editable_enhanced_4g_lte_bool", phoneId)) {
            return true;
        } else {
            enabled = getSettingValueByKey(context, VOLTE_SETTING, phoneId);
        }
        if (enabled != 1) {
            z = false;
        }
        return z;
    }

    public static void setEnhanced4gLteModeSetting(Context context, boolean enabled) {
        setEnhanced4gLteModeSetting(context, enabled, getMainCapabilityPhoneId(context));
    }

    public static void setEnhanced4gLteModeSetting(Context context, boolean enabled, int phoneId) {
        int value = enabled ? 1 : 0;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
            Global.putInt(context.getContentResolver(), "volte_vt_enabled", value);
        } else {
            setSettingValueByKey(context, VOLTE_SETTING, value, phoneId);
        }
        if (isNonTtyOrTtyOnVolteEnabled(context, phoneId)) {
            ImsManager imsManager = getInstance(context, phoneId);
            if (imsManager != null) {
                try {
                    imsManager.setAdvanced4GMode(enabled);
                } catch (ImsException e) {
                }
            }
        }
    }

    public static boolean isNonTtyOrTtyOnVolteEnabled(Context context) {
        return isNonTtyOrTtyOnVolteEnabled(context, getMainCapabilityPhoneId(context));
    }

    public static boolean isNonTtyOrTtyOnVolteEnabled(Context context, int phoneId) {
        boolean z = true;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            if (getBooleanCarrierConfig(context, "carrier_volte_tty_supported_bool")) {
                return true;
            }
            if (Secure.getInt(context.getContentResolver(), "preferred_tty_mode", 0) != 0) {
                z = false;
            }
            return z;
        } else if (getBooleanCarrierConfig(context, "carrier_volte_tty_supported_bool", phoneId)) {
            return true;
        } else {
            if (getSettingValueByKey(context, TTY_MODE, phoneId) != 0) {
                z = false;
            }
            return z;
        }
    }

    public static boolean isVolteEnabledByPlatform(Context context) {
        return isVolteEnabledByPlatform(context, getMainCapabilityPhoneId(context));
    }

    public static boolean isVolteEnabledByPlatform(Context context, int phoneId) {
        boolean z = false;
        if (isTestSim(context, phoneId) || SystemProperties.getInt(PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE, 0) == 1) {
            return true;
        }
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        if (oppoImsPhoneId.get() != -1) {
            phoneId = oppoImsPhoneId.get();
        }
        if (isForceEnableVolte(context, phoneId)) {
            log("isVolteEnabledByPlatform : force true");
            return true;
        }
        boolean isResourceSupport = oppoIsImsResourceSupport(context, 0, phoneId);
        if (SystemProperties.getInt("persist.mtk_volte_support", 0) == 1 && SystemProperties.getInt(LTE_SUPPORT, 0) == 1 && isResourceSupport && getBooleanCarrierConfig(context, "carrier_volte_available_bool", phoneId) && isGbaValid(context, phoneId)) {
            z = isFeatureEnabledByPlatformExt(context, 0);
        }
        return z;
    }

    public static boolean isVolteProvisionedOnDevice(Context context) {
        if (getBooleanCarrierConfig(context, "carrier_volte_provisioning_required_bool")) {
            ImsManager mgr = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
            if (mgr != null) {
                return mgr.isVolteProvisioned();
            }
        }
        return true;
    }

    public static boolean isWfcProvisionedOnDevice(Context context) {
        if (getBooleanCarrierConfig(context, "carrier_volte_provisioning_required_bool")) {
            ImsManager mgr = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
            if (mgr != null) {
                return mgr.isWfcProvisioned();
            }
        }
        return true;
    }

    public static boolean isVtProvisionedOnDevice(Context context) {
        if (getBooleanCarrierConfig(context, "carrier_volte_provisioning_required_bool")) {
            ImsManager mgr = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
            if (mgr != null) {
                return mgr.isVtProvisioned();
            }
        }
        return true;
    }

    public static boolean isVtEnabledByPlatform(Context context) {
        return isVtEnabledByPlatform(context, getMainCapabilityPhoneId(context));
    }

    public static boolean isVtEnabledByPlatform(Context context, int phoneId) {
        boolean z = false;
        if (isTestSim(context, phoneId) || SystemProperties.getInt(PROPERTY_DBG_VT_AVAIL_OVERRIDE, 0) == 1) {
            return true;
        }
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        if (oppoImsPhoneId.get() != -1) {
            phoneId = oppoImsPhoneId.get();
        }
        boolean isResourceSupport = oppoIsImsResourceSupport(context, 1, phoneId);
        if (!SystemProperties.get("ro.mtk_ims_video_call_support", "none").equals("none") && SystemProperties.getInt(LTE_SUPPORT, 0) == 1 && isResourceSupport && getBooleanCarrierConfig(context, "carrier_vt_available_bool", phoneId) && isGbaValid(context, phoneId)) {
            z = isFeatureEnabledByPlatformExt(context, 1);
        }
        return z;
    }

    public static boolean isVtEnabledByUser(Context context) {
        return isVtEnabledByUser(context, getMainCapabilityPhoneId(context));
    }

    public static boolean isVtEnabledByUser(Context context, int phoneId) {
        int enabled;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            enabled = Global.getInt(context.getContentResolver(), "vt_ims_enabled", 1);
        } else {
            enabled = getSettingValueByKey(context, VILTE_SETTING, phoneId);
        }
        if (enabled == 1) {
            return true;
        }
        return false;
    }

    public static void setVtSetting(Context context, boolean enabled) {
        setVtSetting(context, enabled, getMainCapabilityPhoneId(context));
    }

    public static void setVtSetting(Context context, boolean enabled, int phoneId) {
        int i = 1;
        int value = enabled ? 1 : 0;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
            Global.putInt(context.getContentResolver(), "vt_ims_enabled", value);
        } else {
            setSettingValueByKey(context, VILTE_SETTING, value, phoneId);
        }
        ImsManager imsManager = getInstance(context, phoneId);
        if (imsManager != null) {
            try {
                ImsConfig config = imsManager.getConfigInterface();
                if (!enabled) {
                    i = 0;
                }
                config.setFeatureValue(1, 13, i, imsManager.mImsConfigListener);
                if (enabled) {
                    log("setVtSetting() : turnOnIms");
                    imsManager.turnOnIms();
                } else if (!isTurnOffImsAllowedByPlatform(context)) {
                } else {
                    if (!isVolteEnabledByPlatform(context) || !isEnhanced4gLteModeSettingEnabledByUser(context)) {
                        log("setVtSetting() : imsServiceAllowTurnOff -> turnOffIms");
                        imsManager.turnOffIms();
                    }
                }
            } catch (ImsException e) {
                loge("setVtSetting(): ", e);
            }
        }
    }

    private static boolean isTurnOffImsAllowedByPlatform(Context context) {
        if (SystemProperties.getInt(PROPERTY_DBG_ALLOW_IMS_OFF_OVERRIDE, 0) == 1) {
            return true;
        }
        return getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool");
    }

    public static boolean isWfcEnabledByUser(Context context) {
        return isWfcEnabledByUser(context, getMainCapabilityPhoneId(context));
    }

    public static boolean isWfcEnabledByUser(Context context, int phoneId) {
        int enabled;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        int isDefaultWFCIMSEnabledByCarrier = getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool", phoneId) ? 1 : 0;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            enabled = Global.getInt(context.getContentResolver(), "wfc_ims_enabled", isDefaultWFCIMSEnabledByCarrier);
        } else {
            enabled = getSettingValueByKey(context, WFC_SETTING, phoneId);
        }
        log("isWfcEnabledByUser(), phoneId=" + phoneId + " isDefaultWFCIMSEnabledByCarrier=" + isDefaultWFCIMSEnabledByCarrier + " enable=" + enabled);
        if (enabled == 1) {
            return true;
        }
        return false;
    }

    public static void setWfcSetting(Context context, boolean enabled) {
        setWfcSetting(context, enabled, getMainCapabilityPhoneId(context));
    }

    public static void setWfcSetting(Context context, boolean enabled, int phoneId) {
        int i = 1;
        int value = enabled ? 1 : 0;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
            Global.putInt(context.getContentResolver(), "wfc_ims_enabled", value);
        } else {
            setSettingValueByKey(context, WFC_SETTING, value, phoneId);
        }
        ImsManager imsManager = getInstance(context, phoneId);
        if (imsManager != null) {
            try {
                int i2;
                ImsConfig config = imsManager.getConfigInterface();
                if (enabled) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                config.setFeatureValue(2, 18, i2, imsManager.mImsConfigListener);
                if (enabled) {
                    log("setWfcSetting(): turnOnIms, phoneId:" + phoneId);
                    imsManager.turnOnIms();
                } else if (isTurnOffImsAllowedByPlatform(context) && !(isVolteEnabledByPlatform(context) && isEnhanced4gLteModeSettingEnabledByUser(context))) {
                    log("setWfcSetting(): turnOffIms, phoneId:" + phoneId);
                    imsManager.turnOffIms();
                }
                if (enabled) {
                    i = getWfcMode(context);
                }
                setWfcModeInternal(context, i, phoneId);
            } catch (ImsException e) {
                loge("setWfcSetting(): ", e);
            }
        }
    }

    public static int getWfcMode(Context context) {
        return getWfcMode(context, getMainCapabilityPhoneId(context));
    }

    public static int getWfcMode(Context context, int phoneId) {
        int setting;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        int defaultWFCIMSModeByCarrier = getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", phoneId);
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            setting = Global.getInt(context.getContentResolver(), "wfc_ims_mode", defaultWFCIMSModeByCarrier);
        } else {
            setting = getSettingValueByKey(context, WFC_MODE_SETTING, phoneId);
        }
        log("getWfcMode(), phoneId:" + phoneId + ", defaultWFCIMSModeByCarrier:" + defaultWFCIMSModeByCarrier + ", result:" + setting);
        return setting;
    }

    public static void setWfcMode(Context context, int wfcMode) {
        setWfcMode(context, wfcMode, getMainCapabilityPhoneId(context));
    }

    public static void setWfcMode(Context context, int wfcMode, int phoneId) {
        log("setWfcMode(), wfcMode: " + wfcMode + ", phoneId:" + phoneId);
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
            Global.putInt(context.getContentResolver(), "wfc_ims_mode", wfcMode);
        } else {
            setSettingValueByKey(context, WFC_MODE_SETTING, wfcMode, phoneId);
        }
        setWfcModeInternal(context, wfcMode, phoneId);
    }

    public static int getWfcMode(Context context, boolean roaming) {
        return getWfcMode(context, roaming, getMainCapabilityPhoneId(context));
    }

    public static int getWfcMode(Context context, boolean roaming, int phoneId) {
        if (!roaming) {
            return getWfcMode(context, phoneId);
        }
        int setting;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        int defaultWFCIMSRoamingModeByCarrier = getIntCarrierConfig(context, "carrier_default_wfc_ims_roaming_mode_int", phoneId);
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            setting = Global.getInt(context.getContentResolver(), "wfc_ims_roaming_mode", defaultWFCIMSRoamingModeByCarrier);
        } else {
            setting = getSettingValueByKey(context, WFC_ROAMING_MODE_SETTING, phoneId);
        }
        log("getWfcMode(), phoneId:" + phoneId + ", defaultWFCIMSRoamingModeByCarrier:" + defaultWFCIMSRoamingModeByCarrier + ", result:" + setting);
        return setting;
    }

    public static void setWfcMode(Context context, int wfcMode, boolean roaming) {
        setWfcMode(context, wfcMode, roaming, getMainCapabilityPhoneId(context));
    }

    public static void setWfcMode(Context context, int wfcMode, boolean roaming, int phoneId) {
        log("setWfcMode(), wfcMode: " + wfcMode + ", roaming:" + roaming + ", phoneId:" + phoneId);
        if (!roaming) {
            setWfcMode(context, wfcMode, phoneId);
        } else if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
            Global.putInt(context.getContentResolver(), "wfc_ims_roaming_mode", wfcMode);
        } else {
            setSettingValueByKey(context, WFC_ROAMING_MODE_SETTING, wfcMode, phoneId);
        }
        if (roaming == ((TelephonyManager) context.getSystemService("phone")).isNetworkRoaming()) {
            setWfcModeInternal(context, wfcMode, phoneId);
        }
    }

    private static void setWfcModeInternal(Context context, int wfcMode) {
        setWfcModeInternal(context, wfcMode, getMainCapabilityPhoneId(context));
    }

    private static void setWfcModeInternal(Context context, final int wfcMode, int phoneId) {
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        ImsManager imsManager = getInstance(context, phoneId);
        if (imsManager != null) {
            int value = wfcMode;
            try {
                imsManager.getConfigInterface().setWfcMode(wfcMode);
            } catch (ImsException e) {
            }
            new Thread(new Runnable(imsManager) {
                final /* synthetic */ ImsManager val$imsManager;

                public void run() {
                    try {
                        this.val$imsManager.getConfigInterface().setProvisionedValue(27, wfcMode);
                    } catch (ImsException e) {
                    }
                }
            }).start();
        }
    }

    public static boolean isWfcRoamingEnabledByUser(Context context) {
        return isWfcRoamingEnabledByUser(context, getMainCapabilityPhoneId(context));
    }

    public static boolean isWfcRoamingEnabledByUser(Context context, int phoneId) {
        int isRoamingEnableByCarrier;
        int enabled;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", phoneId)) {
            isRoamingEnableByCarrier = 1;
        } else {
            isRoamingEnableByCarrier = 0;
        }
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            enabled = Global.getInt(context.getContentResolver(), "wfc_ims_roaming_enabled", isRoamingEnableByCarrier);
        } else {
            enabled = getSettingValueByKey(context, WFC_ROAMING_SETTING, phoneId);
        }
        log("isWfcRoamingEnabledByUser(), phoneId=" + phoneId + " isRoamingEnableByCarrier=" + isRoamingEnableByCarrier + " enable=" + enabled);
        if (enabled == 1) {
            return true;
        }
        return false;
    }

    public static void setWfcRoamingSetting(Context context, boolean enabled) {
        setWfcRoamingSetting(context, enabled, getMainCapabilityPhoneId(context));
    }

    public static void setWfcRoamingSetting(Context context, boolean enabled, int phoneId) {
        int i = 1;
        log("setWfcRoamingSetting(), enabled: " + enabled + ", phoneId:" + phoneId);
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            ContentResolver contentResolver = context.getContentResolver();
            String str = "wfc_ims_roaming_enabled";
            if (!enabled) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        } else {
            setSettingValueByKey(context, WFC_ROAMING_SETTING, enabled ? 1 : 0, phoneId);
        }
        setWfcRoamingSettingInternal(context, enabled, phoneId);
    }

    private static void setWfcRoamingSettingInternal(Context context, boolean enabled) {
        setWfcRoamingSettingInternal(context, enabled, getMainCapabilityPhoneId(context));
    }

    private static void setWfcRoamingSettingInternal(Context context, boolean enabled, int phoneId) {
        ImsManager imsManager = getInstance(context, phoneId);
        if (imsManager != null) {
            int value;
            if (enabled) {
                value = 1;
            } else {
                value = 0;
            }
            new Thread(new Runnable(imsManager) {
                final /* synthetic */ ImsManager val$imsManager;

                public void run() {
                    try {
                        this.val$imsManager.getConfigInterface().setProvisionedValue(26, value);
                    } catch (ImsException e) {
                    }
                }
            }).start();
        }
    }

    public static boolean isWfcEnabledByPlatform(Context context) {
        return isWfcEnabledByPlatform(context, getMainCapabilityPhoneId(context));
    }

    public static boolean isWfcEnabledByPlatform(Context context, int phoneId) {
        boolean z = false;
        if (SystemProperties.getInt(PROPERTY_DBG_WFC_AVAIL_OVERRIDE, 0) == 1) {
            return true;
        }
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        if (oppoImsPhoneId.get() != -1) {
            phoneId = oppoImsPhoneId.get();
        }
        if (isForceEnableVolte(context, phoneId)) {
            log("isVolteEnabledByPlatform : force true");
            return true;
        }
        boolean isResourceSupport = oppoIsImsResourceSupport(context, 2, phoneId);
        if (SystemProperties.getInt("persist.mtk_wfc_support", 0) == 1 && SystemProperties.getInt(LTE_SUPPORT, 0) == 1 && isResourceSupport && getBooleanCarrierConfig(context, "carrier_wfc_ims_available_bool", phoneId) && isGbaValid(context, phoneId)) {
            z = isFeatureEnabledByPlatformExt(context, 2);
        }
        return z;
    }

    private static boolean isGbaValid(Context context) {
        return isGbaValid(context, getMainCapabilityPhoneId(context));
    }

    private static boolean isGbaValid(Context context, int phoneId) {
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
            if (oppoImsPhoneId.get() != -1) {
                phoneId = oppoImsPhoneId.get();
            }
        }
        int subId = SubscriptionManager.getSubIdUsingPhoneId(phoneId);
        if (!getBooleanCarrierConfig(context, "carrier_ims_gba_required_bool", phoneId)) {
            return true;
        }
        String efIst = null;
        try {
            efIst = getSubscriberInfo().getIsimIstForSubscriber(subId);
        } catch (RemoteException e) {
            loge("remote expcetion for getIsimIstForSubscriber");
        }
        if (efIst == null) {
            loge("ISF is NULL");
            return true;
        }
        boolean result = (efIst == null || efIst.length() <= 1) ? false : (((byte) efIst.charAt(1)) & 2) != 0;
        log("GBA capable=" + result + ", ISF=" + efIst);
        return result;
    }

    public static void onProvisionedValueChanged(Context context, int item, String value) {
        Rlog.d(TAG, "onProvisionedValueChanged: item=" + item + " val=" + value);
        ImsManager mgr = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
        switch (item) {
            case 10:
                mgr.setVolteProvisionedProperty(value.equals("1"));
                Rlog.d(TAG, "isVoLteProvisioned = " + mgr.isVolteProvisioned());
                return;
            case ConfigConstants.LVC_SETTING_ENABLED /*11*/:
                mgr.setVtProvisionedProperty(value.equals("1"));
                Rlog.d(TAG, "isVtProvisioned = " + mgr.isVtProvisioned());
                return;
            case ConfigConstants.VOICE_OVER_WIFI_SETTING_ENABLED /*28*/:
                mgr.setWfcProvisionedProperty(value.equals("1"));
                Rlog.d(TAG, "isWfcProvisioned = " + mgr.isWfcProvisioned());
                return;
            default:
                return;
        }
    }

    private void updateProvisionedValues() {
        if (getBooleanCarrierConfig(this.mContext, "carrier_volte_provisioning_required_bool")) {
            new AsyncUpdateProvisionedValues(this, null).execute(new Void[0]);
        }
    }

    public static void updateImsServiceConfig(Context context, int phoneId, boolean force) {
        if (force || TelephonyManager.getDefault().getSimState() == 5) {
            ImsManager imsManager = getInstance(context, phoneId);
            if (imsManager != null && (!imsManager.mConfigUpdated || force)) {
                log("updateImsServiceConfig() phoneId: " + phoneId + " force: " + force);
                try {
                    imsManager.updateProvisionedValues();
                    if (((imsManager.updateVolteFeatureValue() | imsManager.updateWfcFeatureAndProvisionedValues()) | imsManager.updateVideoCallFeatureValue()) || !isTurnOffImsAllowedByPlatform(context)) {
                        log("updateImsServiceConfig: turnOnIms");
                        imsManager.turnOnIms();
                    } else {
                        log("updateImsServiceConfig: turnOffIms");
                        imsManager.turnOffIms();
                    }
                    imsManager.mConfigUpdated = true;
                } catch (ImsException e) {
                    loge("updateImsServiceConfig: ", e);
                    imsManager.mConfigUpdated = false;
                }
            }
            return;
        }
        log("updateImsServiceConfig: SIM not ready");
    }

    private boolean updateVolteFeatureValue() throws ImsException {
        int i;
        boolean available = isVolteEnabledByPlatform(this.mContext, this.mPhoneId);
        boolean enabled = isEnhanced4gLteModeSettingEnabledByUser(this.mContext, this.mPhoneId);
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled(this.mContext, this.mPhoneId);
        boolean isFeatureOn = (available && enabled) ? isNonTty : false;
        log("updateVolteFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty);
        ImsConfig configInterface = getConfigInterface();
        if (isFeatureOn) {
            i = 1;
        } else {
            i = 0;
        }
        configInterface.setFeatureValue(0, 13, i, this.mImsConfigListener);
        return isFeatureOn;
    }

    private boolean updateVideoCallFeatureValue() throws ImsException {
        boolean enabled;
        int i;
        boolean available = isVtEnabledByPlatform(this.mContext, this.mPhoneId);
        if (isEnhanced4gLteModeSettingEnabledByUser(this.mContext, this.mPhoneId)) {
            enabled = isVtEnabledByUser(this.mContext, this.mPhoneId);
        } else {
            enabled = false;
        }
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled(this.mContext, this.mPhoneId);
        boolean isDataEnabled = isDataEnabled();
        boolean isNonDepOnData = getBooleanCarrierConfig(this.mContext, "vilte_enable_not_dependent_on_data_enable_bool");
        boolean isFeatureOn = (isTestSim(this.mContext, this.mPhoneId) || isNonDepOnData) ? (available && enabled) ? isNonTty : false : (available && enabled && isNonTty) ? isDataEnabled : false;
        log("updateVideoCallFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty + ", data enabled = " + isDataEnabled + ", nonDepOnData = " + isNonDepOnData);
        ImsConfig configInterface = getConfigInterface();
        if (isFeatureOn) {
            i = 1;
        } else {
            i = 0;
        }
        configInterface.setFeatureValue(1, 13, i, this.mImsConfigListener);
        return isFeatureOn;
    }

    private boolean updateWfcFeatureAndProvisionedValues() throws ImsException {
        int i;
        boolean isNetworkRoaming = TelephonyManager.getDefault().isNetworkRoaming();
        boolean available = isWfcEnabledByPlatform(this.mContext);
        boolean enabled = isWfcEnabledByUser(this.mContext);
        int mode = getWfcMode(this.mContext, isNetworkRoaming);
        boolean roaming = isWfcRoamingEnabledByUser(this.mContext);
        boolean isFeatureOn = available ? enabled : false;
        log("updateWfcFeatureAndProvisionedValues: available = " + available + ", enabled = " + enabled + ", mode = " + mode + ", roaming = " + roaming);
        ImsConfig configInterface = getConfigInterface();
        if (isFeatureOn) {
            i = 1;
        } else {
            i = 0;
        }
        configInterface.setFeatureValue(2, 18, i, this.mImsConfigListener);
        if (!isFeatureOn) {
            mode = 1;
            roaming = false;
        }
        setWfcModeInternal(this.mContext, mode);
        setWfcRoamingSettingInternal(this.mContext, roaming);
        return isFeatureOn;
    }

    private ImsManager(Context context, int phoneId) {
        this.mImsService = null;
        this.mDeathRecipient = new ImsServiceDeathRecipient(this, null);
        this.mUt = null;
        this.mConfig = null;
        this.mConfigUpdated = false;
        this.mEcbm = null;
        this.mMultiEndpoint = null;
        this.mContext = context;
        this.mPhoneId = phoneId;
        createImsService(true);
    }

    private static IPhoneSubInfo getSubscriberInfo() {
        return IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
    }

    private static int getSettingValueByKey(Context context, String key, int phoneId) {
        if (key.equals(VOLTE_SETTING)) {
            if (phoneId == 0) {
                return Global.getInt(context.getContentResolver(), "volte_vt_enabled", 1);
            }
            if (phoneId == 1) {
                return Global.getInt(context.getContentResolver(), "volte_vt_enabled_sim2", 1);
            }
            if (phoneId == 2) {
                return Global.getInt(context.getContentResolver(), "volte_vt_enabled_sim3", 1);
            }
            if (phoneId == 3) {
                return Global.getInt(context.getContentResolver(), "volte_vt_enabled_sim4", 1);
            }
        } else if (key.equals(TTY_MODE)) {
            if (phoneId == 0) {
                return Secure.getInt(context.getContentResolver(), "preferred_tty_mode", 0);
            }
            if (phoneId == 1) {
                return Secure.getInt(context.getContentResolver(), "preferred_tty_mode_sim2", 0);
            }
            if (phoneId == 2) {
                return Secure.getInt(context.getContentResolver(), "preferred_tty_mode_sim3", 0);
            }
            if (phoneId == 3) {
                return Secure.getInt(context.getContentResolver(), "preferred_tty_mode_sim4", 0);
            }
        } else if (key.equals(VILTE_SETTING)) {
            if (phoneId == 0) {
                return Global.getInt(context.getContentResolver(), "vt_ims_enabled", 1);
            }
            if (phoneId == 1) {
                return Global.getInt(context.getContentResolver(), "vt_ims_enabled_sim2", 1);
            }
            if (phoneId == 2) {
                return Global.getInt(context.getContentResolver(), "vt_ims_enabled_sim3", 1);
            }
            if (phoneId == 3) {
                return Global.getInt(context.getContentResolver(), "vt_ims_enabled_sim4", 1);
            }
        } else if (key.equals(WFC_SETTING)) {
            int isDefaultWFCIMSEnabledByCarrier;
            if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool", phoneId)) {
                isDefaultWFCIMSEnabledByCarrier = 1;
            } else {
                isDefaultWFCIMSEnabledByCarrier = 0;
            }
            if (phoneId == 0) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_enabled", isDefaultWFCIMSEnabledByCarrier);
            }
            if (phoneId == 1) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_enabled_sim2", isDefaultWFCIMSEnabledByCarrier);
            }
            if (phoneId == 2) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_enabled_sim3", isDefaultWFCIMSEnabledByCarrier);
            }
            if (phoneId == 3) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_enabled_sim4", isDefaultWFCIMSEnabledByCarrier);
            }
        } else if (key.equals(WFC_MODE_SETTING)) {
            int defaultWFCIMSModeByCarrier = getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", phoneId);
            if (phoneId == 0) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_mode", defaultWFCIMSModeByCarrier);
            }
            if (phoneId == 1) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_mode_sim2", defaultWFCIMSModeByCarrier);
            }
            if (phoneId == 2) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_mode_sim3", defaultWFCIMSModeByCarrier);
            }
            if (phoneId == 3) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_mode_sim4", defaultWFCIMSModeByCarrier);
            }
        } else if (key.equals(WFC_ROAMING_SETTING)) {
            int isRoamingEnableByCarrier;
            if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", phoneId)) {
                isRoamingEnableByCarrier = 1;
            } else {
                isRoamingEnableByCarrier = 0;
            }
            if (phoneId == 0) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_roaming_enabled", isRoamingEnableByCarrier);
            }
            if (phoneId == 1) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_roaming_enabled_sim2", isRoamingEnableByCarrier);
            }
            if (phoneId == 2) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_roaming_enabled_sim3", isRoamingEnableByCarrier);
            }
            if (phoneId == 3) {
                return Global.getInt(context.getContentResolver(), "wfc_ims_roaming_enabled_sim4", isRoamingEnableByCarrier);
            }
        }
        return -1;
    }

    private static void setSettingValueByKey(Context context, String key, int value, int phoneId) {
        if (key.equals(VOLTE_SETTING)) {
            if (phoneId == 0) {
                Global.putInt(context.getContentResolver(), "volte_vt_enabled", value);
            } else if (phoneId == 1) {
                Global.putInt(context.getContentResolver(), "volte_vt_enabled_sim2", value);
            } else if (phoneId == 2) {
                Global.putInt(context.getContentResolver(), "volte_vt_enabled_sim3", value);
            } else if (phoneId == 3) {
                Global.putInt(context.getContentResolver(), "volte_vt_enabled_sim4", value);
            }
        } else if (key.equals(VILTE_SETTING)) {
            if (phoneId == 0) {
                Global.putInt(context.getContentResolver(), "vt_ims_enabled", value);
            } else if (phoneId == 1) {
                Global.putInt(context.getContentResolver(), "vt_ims_enabled_sim2", value);
            } else if (phoneId == 2) {
                Global.putInt(context.getContentResolver(), "vt_ims_enabled_sim3", value);
            } else if (phoneId == 3) {
                Global.putInt(context.getContentResolver(), "vt_ims_enabled_sim4", value);
            }
        } else if (key.equals(WFC_SETTING)) {
            if (phoneId == 0) {
                Global.putInt(context.getContentResolver(), "wfc_ims_enabled", value);
            } else if (phoneId == 1) {
                Global.putInt(context.getContentResolver(), "wfc_ims_enabled_sim2", value);
            } else if (phoneId == 2) {
                Global.putInt(context.getContentResolver(), "wfc_ims_enabled_sim3", value);
            } else if (phoneId == 3) {
                Global.putInt(context.getContentResolver(), "wfc_ims_enabled_sim4", value);
            }
        } else if (key.equals(WFC_MODE_SETTING)) {
            if (phoneId == 0) {
                Global.putInt(context.getContentResolver(), "wfc_ims_mode", value);
            } else if (phoneId == 1) {
                Global.putInt(context.getContentResolver(), "wfc_ims_mode_sim2", value);
            } else if (phoneId == 2) {
                Global.putInt(context.getContentResolver(), "wfc_ims_mode_sim3", value);
            } else if (phoneId == 3) {
                Global.putInt(context.getContentResolver(), "wfc_ims_mode_sim4", value);
            }
        } else if (key.equals(WFC_ROAMING_MODE_SETTING)) {
            if (phoneId == 0) {
                Global.putInt(context.getContentResolver(), "wfc_ims_roaming_mode", value);
            } else if (phoneId == 1) {
                Global.putInt(context.getContentResolver(), "wfc_ims_roaming_mode_sim2", value);
            } else if (phoneId == 2) {
                Global.putInt(context.getContentResolver(), "wfc_ims_roaming_mode_sim3", value);
            } else if (phoneId == 3) {
                Global.putInt(context.getContentResolver(), "wfc_ims_roaming_mode_sim4", value);
            }
        } else if (!key.equals(WFC_ROAMING_SETTING)) {
        } else {
            if (phoneId == 0) {
                Global.putInt(context.getContentResolver(), "wfc_ims_roaming_enabled", value);
            } else if (phoneId == 1) {
                Global.putInt(context.getContentResolver(), "wfc_ims_roaming_enabled_sim2", value);
            } else if (phoneId == 2) {
                Global.putInt(context.getContentResolver(), "wfc_ims_roaming_enabled_sim3", value);
            } else if (phoneId == 3) {
                Global.putInt(context.getContentResolver(), "wfc_ims_roaming_enabled_sim4", value);
            }
        }
    }

    private void createTerminalApiServices() {
        Log.d(TAG, "createTerminalApiServices entry");
        this.mCapabilitiesApi = new CapabilityService(this.mContext, new MyServiceListener(this));
        this.mCapabilitiesApi.connect();
        this.mChatApi = new ChatService(this.mContext, new MyServiceListener(this));
        this.mChatApi.connect();
        this.mContactsApi = new ContactsService(this.mContext, new MyServiceListener(this));
        this.mContactsApi.connect();
        this.mFileTransferApi = new FileTransferService(this.mContext, new MyServiceListener(this));
        this.mFileTransferApi.connect();
        this.mGeolocSharingApi = new GeolocSharingService(this.mContext, new MyServiceListener(this));
        this.mGeolocSharingApi.connect();
        this.mImageSharingApi = new ImageSharingService(this.mContext, new MyServiceListener(this));
        this.mImageSharingApi.connect();
        this.mVideoSharingApi = new VideoSharingService(this.mContext, new MyServiceListener(this));
        this.mVideoSharingApi.connect();
    }

    public CapabilityService getCapabilitiesService() {
        return this.mCapabilitiesApi;
    }

    public ChatService getChatService() {
        return this.mChatApi;
    }

    public FileTransferService getFileTransferService() {
        return this.mFileTransferApi;
    }

    public ContactsService getContactsService() {
        return this.mContactsApi;
    }

    public GeolocSharingService getGeolocSharingService() {
        return this.mGeolocSharingApi;
    }

    public ImageSharingService getImageSharingService() {
        return this.mImageSharingApi;
    }

    public VideoSharingService getVideoSharingService() {
        return this.mVideoSharingApi;
    }

    public boolean isServiceAvailable() {
        if (this.mImsService == null && ServiceManager.checkService(getImsServiceName(this.mPhoneId)) == null) {
            return false;
        }
        return true;
    }

    public void setImsConfigListener(ImsConfigListener listener) {
        this.mImsConfigListener = listener;
    }

    public int open(int serviceClass, PendingIntent incomingCallPendingIntent, ImsConnectionStateListener listener) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        log("open: phoneId=" + this.mPhoneId);
        if (incomingCallPendingIntent == null) {
            throw new NullPointerException("incomingCallPendingIntent can't be null");
        } else if (listener == null) {
            throw new NullPointerException("listener can't be null");
        } else {
            try {
                int result = this.mImsService.open(this.mPhoneId, serviceClass, incomingCallPendingIntent, createRegistrationListenerProxy(serviceClass, listener));
                log("open: result=" + result);
                if (result > 0) {
                    return result;
                }
                throw new ImsException("open()", result * -1);
            } catch (RemoteException e) {
                throw new ImsException("open()", e, 106);
            }
        }
    }

    public void addRegistrationListener(int serviceClass, ImsConnectionStateListener listener) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        if (listener == null) {
            throw new NullPointerException("listener can't be null");
        }
        try {
            this.mImsService.addRegistrationListener(this.mPhoneId, serviceClass, createRegistrationListenerProxy(serviceClass, listener));
        } catch (RemoteException e) {
            throw new ImsException("addRegistrationListener()", e, 106);
        }
    }

    public void close(int serviceId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        log("close");
        try {
            this.mImsService.close(serviceId);
            this.mUt = null;
            this.mConfig = null;
            this.mEcbm = null;
            this.mMultiEndpoint = null;
        } catch (RemoteException e) {
            throw new ImsException("close()", e, 106);
        } catch (Throwable th) {
            this.mUt = null;
            this.mConfig = null;
            this.mEcbm = null;
            this.mMultiEndpoint = null;
        }
    }

    public ImsUtInterface getSupplementaryServiceConfiguration(int serviceId) throws ImsException {
        if (this.mUt == null) {
            checkAndThrowExceptionIfServiceUnavailable();
            try {
                IImsUt iUt = this.mImsService.getUtInterface(serviceId);
                if (iUt == null) {
                    throw new ImsException("getSupplementaryServiceConfiguration()", 801);
                }
                this.mUt = new ImsUt(iUt);
            } catch (RemoteException e) {
                throw new ImsException("getSupplementaryServiceConfiguration()", e, 106);
            }
        }
        if (this.mUt != null) {
            this.mUt.updateListener();
        }
        return this.mUt;
    }

    public boolean isConnected(int serviceId, int serviceType, int callType) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.isConnected(serviceId, serviceType, callType);
        } catch (RemoteException e) {
            throw new ImsException("isServiceConnected()", e, 106);
        }
    }

    public boolean isOpened(int serviceId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.isOpened(serviceId);
        } catch (RemoteException e) {
            throw new ImsException("isOpened()", e, 106);
        }
    }

    public ImsCallProfile createCallProfile(int serviceId, int serviceType, int callType) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.createCallProfile(serviceId, serviceType, callType);
        } catch (RemoteException e) {
            throw new ImsException("createCallProfile()", e, 106);
        }
    }

    public ImsCall makeCall(int serviceId, ImsCallProfile profile, String[] callees, Listener listener) throws ImsException {
        log("makeCall :: serviceId=" + serviceId + ", profile=" + profile);
        checkAndThrowExceptionIfServiceUnavailable();
        ImsCall call = new ImsCall(this.mContext, profile);
        call.setListener(listener);
        ImsCallSession session = createCallSession(serviceId, profile);
        if (callees == null || callees.length != 1 || profile.getCallExtraBoolean("conference")) {
            call.start(session, callees);
        } else {
            call.start(session, callees[0]);
        }
        return call;
    }

    public ImsCall takeCall(int serviceId, Intent incomingCallIntent, Listener listener) throws ImsException {
        log("takeCall :: serviceId=" + serviceId + ", incomingCall=" + incomingCallIntent);
        checkAndThrowExceptionIfServiceUnavailable();
        if (incomingCallIntent == null) {
            throw new ImsException("Can't retrieve session with null intent", INCOMING_CALL_RESULT_CODE);
        } else if (serviceId != getServiceId(incomingCallIntent)) {
            throw new ImsException("Service id is mismatched in the incoming call intent", INCOMING_CALL_RESULT_CODE);
        } else {
            String callId = getCallId(incomingCallIntent);
            if (callId == null) {
                throw new ImsException("Call ID missing in the incoming call intent", INCOMING_CALL_RESULT_CODE);
            }
            try {
                IImsCallSession session = this.mImsService.getPendingCallSession(serviceId, callId);
                if (session == null) {
                    throw new ImsException("No pending session for the call", 107);
                }
                ImsCall call = new ImsCall(this.mContext, session.getCallProfile());
                call.attachSession(new ImsCallSession(session));
                call.setListener(listener);
                return call;
            } catch (Throwable t) {
                ImsException imsException = new ImsException("takeCall()", t, 0);
            }
        }
    }

    public ImsConfig getConfigInterface() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            IImsConfig config = this.mImsService.getConfigInterface(this.mPhoneId);
            if (config == null) {
                throw new ImsException("getConfigInterface()", 131);
            }
            this.mConfig = new ImsConfig(config, this.mContext);
            log("getConfigInterface(), mConfig= " + this.mConfig);
            return this.mConfig;
        } catch (RemoteException e) {
            throw new ImsException("getConfigInterface()", e, 106);
        }
    }

    public void setUiTTYMode(Context context, int serviceId, int uiTtyMode, Message onComplete) throws ImsException {
        boolean z = false;
        checkAndThrowExceptionIfServiceUnavailable();
        int phoneId = serviceId - 1;
        try {
            this.mImsService.setUiTTYMode(serviceId, uiTtyMode, onComplete);
            if (!getBooleanCarrierConfig(context, "carrier_volte_tty_supported_bool", phoneId)) {
                log("TTY over VoLTE not supported, ttyMode=" + uiTtyMode);
                if (uiTtyMode == 0) {
                    z = isEnhanced4gLteModeSettingEnabledByUser(context, phoneId);
                }
                setAdvanced4GMode(z);
            }
        } catch (RemoteException e) {
            throw new ImsException("setTTYMode()", e, 106);
        }
    }

    private static boolean getBooleanCarrierConfig(Context context, String key) {
        return getBooleanCarrierConfig(context, key, getMainCapabilityPhoneId(context));
    }

    private static boolean getBooleanCarrierConfig(Context context, String key, int phoneId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        int subId = SubscriptionManager.getSubIdUsingPhoneId(phoneId);
        log("getBooleanCarrierConfig: phoneId=" + phoneId + " subId=" + subId);
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            return b.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }

    private static int getIntCarrierConfig(Context context, String key) {
        return getIntCarrierConfig(context, key, getMainCapabilityPhoneId(context));
    }

    private static int getIntCarrierConfig(Context context, String key, int phoneId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        int subId = SubscriptionManager.getSubIdUsingPhoneId(phoneId);
        log("getIntCarrierConfig: phoneId=" + phoneId + " subId=" + subId);
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            return b.getInt(key);
        }
        return CarrierConfigManager.getDefaultConfig().getInt(key);
    }

    private static String getCallId(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return null;
        }
        return incomingCallIntent.getStringExtra(EXTRA_CALL_ID);
    }

    private static String getCallNum(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return null;
        }
        return incomingCallIntent.getStringExtra(EXTRA_DIAL_STRING);
    }

    private static int getSeqNum(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return -1;
        }
        return incomingCallIntent.getIntExtra(EXTRA_SEQ_NUM, -1);
    }

    private static int getServiceId(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return -1;
        }
        return incomingCallIntent.getIntExtra(EXTRA_SERVICE_ID, -1);
    }

    private void checkAndThrowExceptionIfServiceUnavailable() throws ImsException {
        if (this.mImsService == null) {
            createImsService(true);
            if (this.mImsService == null) {
                throw new ImsException("Service is unavailable", 106);
            }
        }
    }

    private static String getImsServiceName(int phoneId) {
        return IMS_SERVICE;
    }

    private void createImsService(boolean checkService) {
        if (checkService && ServiceManager.checkService(getImsServiceName(this.mPhoneId)) == null) {
            log("createImsService binder is null");
            return;
        }
        IBinder b = ServiceManager.getService(getImsServiceName(this.mPhoneId));
        if (b != null) {
            try {
                b.linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
            }
        }
        this.mImsService = IImsService.Stub.asInterface(b);
        log("mImsService = " + this.mImsService);
    }

    private ImsCallSession createCallSession(int serviceId, ImsCallProfile profile) throws ImsException {
        try {
            return new ImsCallSession(this.mImsService.createCallSession(serviceId, profile, null));
        } catch (RemoteException e) {
            return null;
        }
    }

    private ImsRegistrationListenerProxy createRegistrationListenerProxy(int serviceClass, ImsConnectionStateListener listener) {
        return new ImsRegistrationListenerProxy(this, serviceClass, listener);
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void logw(String s) {
        Rlog.w(TAG, s);
    }

    private static void loge(String s) {
        Rlog.e(TAG, s);
    }

    private static void loge(String s, Throwable t) {
        Rlog.e(TAG, s, t);
    }

    private void turnOnIms() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mImsService.turnOnIms(this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("turnOnIms() ", e, 106);
        }
    }

    private boolean isImsTurnOffAllowed() {
        log("CarrierConfig:" + getBooleanCarrierConfig(this.mContext, "carrier_allow_turnoff_ims_bool", this.mPhoneId) + " wfcendablebyplateform:" + isWfcEnabledByPlatform(this.mContext) + " wfcenablebyUser:" + isWfcEnabledByUser(this.mContext));
        if (!isTurnOffImsAllowedByPlatform(this.mContext)) {
            return false;
        }
        if (isWfcEnabledByPlatform(this.mContext) && isWfcEnabledByUser(this.mContext, this.mPhoneId)) {
            return false;
        }
        return true;
    }

    private void setLteFeatureValues(boolean turnOn) {
        int i = 1;
        log("setLteFeatureValues: " + turnOn);
        try {
            ImsConfig config = getConfigInterface();
            if (config != null) {
                int i2;
                if (turnOn) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                config.setFeatureValue(0, 13, i2, this.mImsConfigListener);
                if (isVtEnabledByPlatform(this.mContext)) {
                    boolean enableViLte = (isTestSim(this.mContext, this.mPhoneId) || getBooleanCarrierConfig(this.mContext, "vilte_enable_not_dependent_on_data_enable_bool")) ? turnOn ? isVtEnabledByUser(this.mContext, this.mPhoneId) : false : (turnOn && isVtEnabledByUser(this.mContext, this.mPhoneId)) ? isDataEnabled() : false;
                    if (!enableViLte) {
                        i = 0;
                    }
                    config.setFeatureValue(1, 13, i, this.mImsConfigListener);
                }
            }
        } catch (ImsException e) {
            loge("setLteFeatureValues: exception ", e);
        }
    }

    private void setAdvanced4GMode(boolean turnOn) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        if (turnOn) {
            setLteFeatureValues(turnOn);
            log("setAdvanced4GMode: turnOnIms");
            turnOnIms();
            return;
        }
        if (isImsTurnOffAllowed()) {
            log("setAdvanced4GMode: turnOffIms");
            turnOffIms();
        }
        setLteFeatureValues(turnOn);
    }

    private void turnOffIms() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mImsService.turnOffIms(this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("turnOffIms() ", e, 106);
        }
    }

    public ImsEcbm getEcbmInterface(int serviceId) throws ImsException {
        if (this.mEcbm == null) {
            checkAndThrowExceptionIfServiceUnavailable();
            try {
                IImsEcbm iEcbm = this.mImsService.getEcbmInterface(serviceId);
                if (iEcbm == null) {
                    throw new ImsException("getEcbmInterface()", 901);
                }
                this.mEcbm = new ImsEcbm(iEcbm);
            } catch (RemoteException e) {
                throw new ImsException("getEcbmInterface()", e, 106);
            }
        }
        return this.mEcbm;
    }

    public ImsMultiEndpoint getMultiEndpointInterface(int serviceId) throws ImsException {
        if (this.mMultiEndpoint == null) {
            checkAndThrowExceptionIfServiceUnavailable();
            try {
                IImsMultiEndpoint iImsMultiEndpoint = this.mImsService.getMultiEndpointInterface(serviceId);
                if (iImsMultiEndpoint == null) {
                    throw new ImsException("getMultiEndpointInterface()", 902);
                }
                this.mMultiEndpoint = new ImsMultiEndpoint(iImsMultiEndpoint);
            } catch (RemoteException e) {
                throw new ImsException("getMultiEndpointInterface()", e, 106);
            }
        }
        return this.mMultiEndpoint;
    }

    public static void factoryReset(Context context) {
        int i = 0;
        ContentResolver contentResolver;
        String str;
        int i2;
        ContentResolver contentResolver2;
        String str2;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            contentResolver = context.getContentResolver();
            str = "volte_vt_enabled";
            if (!SystemProperties.get("persist.mtk_ct_volte_support").equals("1") || getBooleanCarrierConfig(context, "default_enhanced_4g_mode_enabled_bool")) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            Global.putInt(contentResolver, str, i2);
            contentResolver = context.getContentResolver();
            str = "wfc_ims_enabled";
            if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool")) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            Global.putInt(contentResolver, str, i2);
            Global.putInt(context.getContentResolver(), "wfc_ims_mode", getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int"));
            contentResolver2 = context.getContentResolver();
            str2 = "wfc_ims_roaming_enabled";
            if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool")) {
                i = 1;
            }
            Global.putInt(contentResolver2, str2, i);
            Global.putInt(context.getContentResolver(), "vt_ims_enabled", 1);
            updateImsServiceConfig(context, getMainCapabilityPhoneId(context), true);
            return;
        }
        Global.putInt(context.getContentResolver(), "volte_vt_enabled", 1);
        Global.putInt(context.getContentResolver(), "volte_vt_enabled_sim2", 1);
        Global.putInt(context.getContentResolver(), "volte_vt_enabled_sim3", 1);
        Global.putInt(context.getContentResolver(), "volte_vt_enabled_sim4", 1);
        contentResolver = context.getContentResolver();
        str = "wfc_ims_enabled";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool", 0)) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        Global.putInt(contentResolver, str, i2);
        contentResolver = context.getContentResolver();
        str = "wfc_ims_enabled_sim2";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool", 1)) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        Global.putInt(contentResolver, str, i2);
        contentResolver = context.getContentResolver();
        str = "wfc_ims_enabled_sim3";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool", 2)) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        Global.putInt(contentResolver, str, i2);
        contentResolver = context.getContentResolver();
        str = "wfc_ims_enabled_sim4";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool", 3)) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        Global.putInt(contentResolver, str, i2);
        Global.putInt(context.getContentResolver(), "wfc_ims_mode", getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", 0));
        Global.putInt(context.getContentResolver(), "wfc_ims_mode_sim2", getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", 1));
        Global.putInt(context.getContentResolver(), "wfc_ims_mode_sim3", getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", 2));
        Global.putInt(context.getContentResolver(), "wfc_ims_mode_sim4", getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", 3));
        contentResolver = context.getContentResolver();
        str = "wfc_ims_roaming_enabled";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", 0)) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        Global.putInt(contentResolver, str, i2);
        contentResolver = context.getContentResolver();
        str = "wfc_ims_roaming_enabled_sim2";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", 1)) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        Global.putInt(contentResolver, str, i2);
        contentResolver = context.getContentResolver();
        str = "wfc_ims_roaming_enabled_sim3";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", 2)) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        Global.putInt(contentResolver, str, i2);
        contentResolver2 = context.getContentResolver();
        str2 = "wfc_ims_roaming_enabled_sim4";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", 3)) {
            i = 1;
        }
        Global.putInt(contentResolver2, str2, i);
        Global.putInt(context.getContentResolver(), "vt_ims_enabled", 1);
        Global.putInt(context.getContentResolver(), "vt_ims_enabled_sim2", 1);
        Global.putInt(context.getContentResolver(), "vt_ims_enabled_sim3", 1);
        Global.putInt(context.getContentResolver(), "vt_ims_enabled_sim4", 1);
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        for (int i3 = 0; i3 < numPhones; i3++) {
            updateImsServiceConfig(context, i3 + 0, true);
        }
    }

    private boolean isDataEnabled() {
        return SystemProperties.getBoolean(DATA_ENABLED_PROP[this.mPhoneId], true);
    }

    public void setDataEnabled(boolean enabled) {
        log("[" + this.mPhoneId + "] setDataEnabled: " + enabled);
        SystemProperties.set(DATA_ENABLED_PROP[this.mPhoneId], enabled ? TRUE : FALSE);
    }

    private boolean isVolteProvisioned() {
        return SystemProperties.getBoolean(VOLTE_PROVISIONED_PROP, true);
    }

    private void setVolteProvisionedProperty(boolean provisioned) {
        SystemProperties.set(VOLTE_PROVISIONED_PROP, provisioned ? TRUE : FALSE);
    }

    private boolean isWfcProvisioned() {
        return SystemProperties.getBoolean(WFC_PROVISIONED_PROP, true);
    }

    private void setWfcProvisionedProperty(boolean provisioned) {
        SystemProperties.set(WFC_PROVISIONED_PROP, provisioned ? TRUE : FALSE);
    }

    private boolean isVtProvisioned() {
        return SystemProperties.getBoolean(VT_PROVISIONED_PROP, true);
    }

    private void setVtProvisionedProperty(boolean provisioned) {
        SystemProperties.set(VT_PROVISIONED_PROP, provisioned ? TRUE : FALSE);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ImsManager:");
        pw.println("  mPhoneId = " + this.mPhoneId);
        pw.println("  mConfigUpdated = " + this.mConfigUpdated);
        pw.println("  mImsService = " + this.mImsService);
        pw.println("  mDataEnabled = " + isDataEnabled());
        pw.println("  isGbaValid = " + isGbaValid(this.mContext));
        pw.println("  isImsTurnOffAllowed = " + isImsTurnOffAllowed());
        pw.println("  isNonTtyOrTtyOnVolteEnabled = " + isNonTtyOrTtyOnVolteEnabled(this.mContext));
        pw.println("  isVolteEnabledByPlatform = " + isVolteEnabledByPlatform(this.mContext));
        pw.println("  isVolteProvisionedOnDevice = " + isVolteProvisionedOnDevice(this.mContext));
        pw.println("  isEnhanced4gLteModeSettingEnabledByUser = " + isEnhanced4gLteModeSettingEnabledByUser(this.mContext));
        pw.println("  isVtEnabledByPlatform = " + isVtEnabledByPlatform(this.mContext));
        pw.println("  isVtEnabledByUser = " + isVtEnabledByUser(this.mContext));
        pw.println("  isWfcEnabledByPlatform = " + isWfcEnabledByPlatform(this.mContext));
        pw.println("  isWfcEnabledByUser = " + isWfcEnabledByUser(this.mContext));
        pw.println("  getWfcMode = " + getWfcMode(this.mContext));
        pw.println("  isWfcRoamingEnabledByUser = " + isWfcRoamingEnabledByUser(this.mContext));
        pw.println("  isVtProvisionedOnDevice = " + isVtProvisionedOnDevice(this.mContext));
        pw.println("  isWfcProvisionedOnDevice = " + isWfcProvisionedOnDevice(this.mContext));
        pw.flush();
    }

    public void setCallIndication(int serviceId, Intent incomingCallIndication, boolean isAllow) throws ImsException {
        log("setCallIndication :: serviceId=" + serviceId + ", incomingCallIndication=" + incomingCallIndication);
        checkAndThrowExceptionIfServiceUnavailable();
        if (incomingCallIndication == null) {
            throw new ImsException("Can't retrieve session with null intent", INCOMING_CALL_RESULT_CODE);
        } else if (serviceId != getServiceId(incomingCallIndication)) {
            throw new ImsException("Service id is mismatched in the incoming call intent", INCOMING_CALL_RESULT_CODE);
        } else {
            String callId = getCallId(incomingCallIndication);
            if (callId == null) {
                throw new ImsException("Call ID missing in the incoming call intent", INCOMING_CALL_RESULT_CODE);
            }
            String callNum = getCallNum(incomingCallIndication);
            if (callNum == null) {
                throw new ImsException("Call Num missing in the incoming call intent", INCOMING_CALL_RESULT_CODE);
            }
            int seqNum = getSeqNum(incomingCallIndication);
            if (seqNum == -1) {
                throw new ImsException("seqNum missing in the incoming call intent", INCOMING_CALL_RESULT_CODE);
            }
            try {
                this.mImsService.setCallIndication(serviceId, callId, callNum, seqNum, isAllow);
            } catch (RemoteException e) {
                throw new ImsException("setCallIndication()", e, 106);
            }
        }
    }

    public int getImsState() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.getImsState(this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("getImsState()", e, 106);
        }
    }

    public boolean getImsRegInfo() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.getImsRegInfo(this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("getImsRegInfo", e, 106);
        }
    }

    public String getImsExtInfo() throws ImsException {
        String imsExtInfo = "0";
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.getImsExtInfo(this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("getImsExtInfo()", e, 106);
        }
    }

    public void hangupAllCall() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mImsService.hangupAllCall();
        } catch (RemoteException e) {
            throw new ImsException("hangupAll()", e, 106);
        }
    }

    public int getWfcStatusCode() {
        if (this.mImsService == null) {
            return 100;
        }
        try {
            return this.mImsService.getRegistrationStatus();
        } catch (RemoteException e) {
            return 100;
        }
    }

    private static boolean isImsResourceSupport(Context context, int feature, int phoneId) {
        boolean support = true;
        if ("1".equals(SystemProperties.get("persist.mtk_dynamic_ims_switch"))) {
            if (SubscriptionManager.isValidPhoneId(phoneId)) {
                try {
                    ImsConfig config = getConfigInterface(phoneId, context);
                    if (config != null) {
                        support = config.getImsResCapability(feature) == 1;
                    }
                } catch (ImsException e) {
                    loge("isImsResourceSupport() failed!" + e);
                }
                log("isImsResourceSupport(" + feature + ") return " + support + " on phone: " + phoneId);
            } else {
                loge("Invalid main phone " + phoneId + ", return true as don't care");
                return true;
            }
        }
        return support;
    }

    private static ImsConfig getConfigInterface(int phoneId, Context context) throws ImsException {
        try {
            IBinder b = ServiceManager.getService(getImsServiceName(phoneId));
            if (b == null) {
                throw new ImsException("getConfigInterface(), ImsService binder", 131);
            }
            IImsConfig binder = IImsService.Stub.asInterface(b).getConfigInterface(phoneId);
            if (binder != null) {
                return new ImsConfig(binder, context);
            }
            throw new ImsException("getConfigInterface()", 131);
        } catch (RemoteException e) {
            throw new ImsException("getConfigInterface()", e, 106);
        }
    }

    private static int getMainCapabilityPhoneId(Context context) {
        int phoneId = -1;
        ITelephonyEx telephony = ITelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (telephony != null) {
            try {
                return telephony.getMainCapabilityPhoneId();
            } catch (RemoteException e) {
                loge("getMainCapabilityPhoneId: remote exception");
                return phoneId;
            }
        }
        loge("ITelephonyEx service not ready!");
        phoneId = SystemProperties.getInt("persist.radio.simswitch", 1) - 1;
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            phoneId = -1;
        }
        Rlog.d(TAG, "getMainCapabilityPhoneId: phoneId = " + phoneId);
        return phoneId;
    }

    private static boolean isFeatureEnabledByPlatformExt(Context context, int feature) {
        if (context == null) {
            logw("Invalid context, return " + true);
            return true;
        }
        if (mImsManagerExt == null) {
            mImsManagerExt = (IImsManagerExt) MPlugin.createInstance(IImsManagerExt.class.getName(), context);
            if (mImsManagerExt == null) {
                logw("Unable to create imsManagerPlugin, return " + true);
                return true;
            }
        }
        boolean isEnabled = mImsManagerExt.isFeatureEnabledByPlatform(feature);
        log("isFeatureEnabledByPlatformExt(), feature:" + feature + ", isEnabled:" + isEnabled);
        return isEnabled;
    }

    private static boolean isTestSim(Context context, int phoneId) {
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(context);
        }
        switch (phoneId) {
            case 0:
                return "1".equals(SystemProperties.get("gsm.sim.ril.testsim", "0"));
            case 1:
                return "1".equals(SystemProperties.get("gsm.sim.ril.testsim.2", "0"));
            case 2:
                return "1".equals(SystemProperties.get("gsm.sim.ril.testsim.3", "0"));
            case 3:
                return "1".equals(SystemProperties.get("gsm.sim.ril.testsim.4", "0"));
            default:
                return false;
        }
    }

    public static boolean isEvsEnabledByUser(Context context) {
        return SystemProperties.getInt(PROPERTY_IMS_EVS_ENABLE, 0) == 1;
    }

    public static void setEvsSetting(Context context, boolean enabled) {
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        int i = 0;
        while (i < numPhones) {
            int phoneId = i;
            if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
                phoneId = getMainCapabilityPhoneId(context);
                i = numPhones;
            }
            ImsManager imsManager = getInstance(context, phoneId);
            if (imsManager != null) {
                try {
                    imsManager.setEvsEnabled(enabled);
                } catch (ImsException e) {
                }
            }
            i++;
        }
    }

    private void setEvsEnabled(boolean enabled) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mImsService.setEvsEnabled(enabled ? 1 : 0, this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("setEvsEnabled() ", e, 106);
        }
    }

    public static boolean oppoIsVolteEnabledByPlatform(Context context, int phoneId) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return false;
        }
        oppoImsPhoneId.set(phoneId);
        boolean ret = isVolteEnabledByPlatform(context);
        oppoImsPhoneId.set(-1);
        log("oppoIsVolteEnabledByPlatform phoneId " + phoneId + ", ret " + ret);
        return ret;
    }

    public static boolean oppoIsVtEnabledByPlatform(Context context, int phoneId) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return false;
        }
        oppoImsPhoneId.set(phoneId);
        boolean ret = isVtEnabledByPlatform(context);
        oppoImsPhoneId.set(-1);
        log("oppoIsVtEnabledByPlatform phoneId " + phoneId + ", ret " + ret);
        return ret;
    }

    public static boolean oppoIsWfcEnabledByPlatform(Context context, int phoneId) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return false;
        }
        oppoImsPhoneId.set(phoneId);
        boolean ret = isWfcEnabledByPlatform(context);
        oppoImsPhoneId.set(-1);
        log("oppoIsWfcEnabledByPlatform phoneId " + phoneId + ", ret " + ret);
        return ret;
    }

    private static boolean oppoIsImsResourceSupport(Context context, int feature, int phoneId) {
        String operator = ((TelephonyManager) context.getSystemService("phone")).getSimOperatorNumericForPhone(phoneId);
        int mcc = 0;
        int mnc = 0;
        if (operator != null && operator.length() > 3) {
            mcc = Integer.parseInt(operator.substring(0, 3));
            mnc = Integer.parseInt(operator.substring(3, operator.length()));
        }
        Context resc = null;
        try {
            Configuration configuration = new Configuration();
            configuration = context.getResources().getConfiguration();
            configuration.mcc = mcc;
            if (mnc == 0) {
                mnc = 65535;
            }
            configuration.mnc = mnc;
            resc = context.createConfigurationContext(configuration);
        } catch (Exception e) {
            e.printStackTrace();
            loge("getResourcesUsingMccMnc fail");
        }
        boolean ret = false;
        if (resc != null) {
            switch (feature) {
                case 0:
                    ret = resc.getResources().getBoolean(17957004);
                    break;
                case 1:
                    ret = resc.getResources().getBoolean(17957008);
                    break;
                case 2:
                    ret = resc.getResources().getBoolean(17957011);
                    break;
                default:
                    ret = false;
                    break;
            }
        }
        log("oppoIsImsResourceSupport feature " + feature + ", phoneId " + phoneId + ", ret " + ret);
        return ret;
    }

    private static boolean isForceEnableVolte(Context context, int phoneId) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        int[] subId = SubscriptionManager.getSubId(phoneId);
        String iccid = "";
        if (subId != null) {
            iccid = tm.getSimSerialNumber(subId[0]);
        }
        if (TextUtils.isEmpty(iccid) || !iccid.startsWith("8988605")) {
            return false;
        }
        return true;
    }

    private static boolean isForceEnableWfc(Context context, int phoneId) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        int[] subId = SubscriptionManager.getSubId(phoneId);
        String iccid = "";
        if (subId != null) {
            iccid = tm.getSimSerialNumber(subId[0]);
        }
        if (TextUtils.isEmpty(iccid) || !iccid.startsWith("8988605")) {
            return false;
        }
        return true;
    }
}
