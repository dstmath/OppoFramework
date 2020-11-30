package com.oppo.internal.telephony;

import android.content.Context;
import android.util.Log;
import com.android.internal.telephony.AbstractCallTracker;
import com.android.internal.telephony.AbstractIccSmsInterfaceManager;
import com.android.internal.telephony.AbstractImsSmsDispatcher;
import com.android.internal.telephony.AbstractInboundSmsHandler;
import com.android.internal.telephony.AbstractSMSDispatcher;
import com.android.internal.telephony.AbstractSmsDispatchersController;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IOppoAdnRecordCache;
import com.android.internal.telephony.IOppoCallManager;
import com.android.internal.telephony.IOppoCallTracker;
import com.android.internal.telephony.IOppoDataManager;
import com.android.internal.telephony.IOppoGsmCdmaPhone;
import com.android.internal.telephony.IOppoIccPhoneBookInterfaceManager;
import com.android.internal.telephony.IOppoIccSmsInterfaceManager;
import com.android.internal.telephony.IOppoInboundSmsHandler;
import com.android.internal.telephony.IOppoNetworkManager;
import com.android.internal.telephony.IOppoNewNitzStateMachine;
import com.android.internal.telephony.IOppoPhone;
import com.android.internal.telephony.IOppoSMSDispatcher;
import com.android.internal.telephony.IOppoServiceStateTracker;
import com.android.internal.telephony.IOppoSmsDispatchersController;
import com.android.internal.telephony.IOppoSmsManager;
import com.android.internal.telephony.IOppoSubscriptionController;
import com.android.internal.telephony.IOppoUiccManager;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.IccSmsInterfaceManager;
import com.android.internal.telephony.ImsSmsDispatcher;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cdma.AbstractCdmaSMSDispatcher;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import com.android.internal.telephony.common.OppoFeatureManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.IOppoDcTracker;
import com.android.internal.telephony.gsm.AbstractGsmSMSDispatcher;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import com.android.internal.telephony.imsphone.IOppoImsPhone;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.uicc.AbstractRuimRecords;
import com.android.internal.telephony.uicc.AbstractSIMRecords;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IOppoRuimRecords;
import com.android.internal.telephony.uicc.IOppoSIMRecords;
import com.android.internal.telephony.uicc.IOppoUiccController;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.cdma.OppoCdmaSMSDispatcherReference;
import com.oppo.internal.telephony.dataconnection.OppoDcTrackerReference;
import com.oppo.internal.telephony.gsm.OppoGsmSMSDispatcherReference;
import com.oppo.internal.telephony.imsphone.OppoImsPhoneCallTrackerReference;
import com.oppo.internal.telephony.imsphone.OppoImsPhoneReference;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import com.oppo.internal.telephony.phb.OppoAdnRecordCache;
import com.oppo.internal.telephony.phb.OppoIccPhoneBookInterfaceManager;
import com.oppo.internal.telephony.uicc.OppoRuimRecordsReference;
import com.oppo.internal.telephony.uicc.OppoSIMRecordReference;
import com.oppo.internal.telephony.uicc.OppoUiccController;

public class OppoTelephonyFactoryImpl extends OppoTelephonyFactory {
    public <T extends IOppoCommonFeature> T getFeature(T def, Object... vars) {
        verityParams(def);
        if (!OppoFeatureManager.isSupport(def)) {
            return def;
        }
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[def.index().ordinal()]) {
            case 1:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoPhone(vars));
            case 2:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoGsmCdmaPhone(vars));
            case 3:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoImsPhone(vars));
            case 4:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoNetworkManager(vars));
            case 5:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoServiceStateTracker(vars));
            case 6:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoNewNitzStateMachine(vars));
            case 7:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoCallManager(vars));
            case 8:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoCallTracker(vars));
            case 9:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoDataManager(vars));
            case 10:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoDcTracker(vars));
            case 11:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoSmsManager(vars));
            case 12:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoIccSmsInterfaceManager(vars));
            case 13:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoInboundSmsHandler(vars));
            case 14:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoSMSDispatcher(vars));
            case 15:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoSmsDispatchersController(vars));
            case 16:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoUiccManager(vars));
            case 17:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoSIMRecords(vars));
            case 18:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoRuimRecords(vars));
            case 19:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoSubscriptionController(vars));
            case 20:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoUiccController(vars));
            case NetworkDiagnoseUtils.RF_BAND21 /* 21 */:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoAdnRecordCache(vars));
            case 22:
                return (T) OppoFeatureManager.getTraceMonitor(getOppoIccPhoneBookInterfaceManager(vars));
            default:
                Log.i("OppoTelephonyFactory", "Unknow feature:" + def.index().name());
                return def;
        }
    }

    /* renamed from: com.oppo.internal.telephony.OppoTelephonyFactoryImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex = new int[OppoFeatureList.OppoIndex.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoPhone.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoGsmCdmaPhone.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoImsPhone.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoNetworkManager.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoServiceStateTracker.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoNewNitzStateMachine.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoCallManager.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoCallTracker.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoDataManager.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoDcTracker.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoSmsManager.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoIccSmsInterfaceManager.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoInboundSmsHandler.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoSMSDispatcher.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoSmsDispatchersController.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoUiccManager.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoSIMRecords.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoRuimRecords.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoSubscriptionController.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoUiccController.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoAdnRecordCache.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IOppoIccPhoneBookInterfaceManager.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
        }
    }

    public IOppoPhone getOppoPhone(Object... vars) {
        verityParamsType("getOppoPhone", vars, 1, new Class[]{Phone.class});
        return new OppoPhoneReference((Phone) vars[0]);
    }

    public IOppoGsmCdmaPhone getOppoGsmCdmaPhone(Object... vars) {
        verityParamsType("getOppoGsmCdmaPhone", vars, 1, new Class[]{GsmCdmaPhone.class});
        return new OppoGsmCdmaPhoneReference((GsmCdmaPhone) vars[0]);
    }

    public IOppoImsPhone getOppoImsPhone(Object... vars) {
        verityParamsType("getOppoImsPhone", vars, 1, new Class[]{ImsPhone.class});
        return new OppoImsPhoneReference((ImsPhone) vars[0]);
    }

    public IOppoNetworkManager getOppoNetworkManager(Object... vars) {
        return OppoNetworkManagerImpl.getInstance();
    }

    public IOppoServiceStateTracker getOppoServiceStateTracker(Object... vars) {
        verityParamsType("getOppoServiceStateTracker", vars, 2, new Class[]{ServiceStateTracker.class, GsmCdmaPhone.class});
        return new OppoServiceStateTracker((ServiceStateTracker) vars[0], (GsmCdmaPhone) vars[1]);
    }

    public IOppoNewNitzStateMachine getOppoNewNitzStateMachine(Object... vars) {
        verityParamsType("getOppoNitzStateMachine", vars, 1, new Class[]{GsmCdmaPhone.class});
        return new OppoNewNitzStateMachine((GsmCdmaPhone) vars[0]);
    }

    public IOppoCallManager getOppoCallManager(Object... vars) {
        return OppoCallManagerImpl.getInstance();
    }

    public IOppoCallTracker getOppoCallTracker(Object... vars) {
        verityParamsType("getOppoCallTracker", vars, 1, new Class[]{AbstractCallTracker.class});
        AbstractCallTracker callTracker = (AbstractCallTracker) vars[0];
        if (GsmCdmaCallTracker.class.isInstance(callTracker)) {
            return new OppoGsmCdmaCallTrackerReference(callTracker);
        }
        return new OppoImsPhoneCallTrackerReference(callTracker);
    }

    public IOppoDataManager getOppoDataManager(Object... vars) {
        return OppoDataManagerImpl.getInstance();
    }

    public IOppoDcTracker getOppoDcTracker(Object... vars) {
        verityParamsType("getOppoDcTracker", vars, 2, new Class[]{DcTracker.class, Phone.class});
        return new OppoDcTrackerReference((DcTracker) vars[0], (Phone) vars[1]);
    }

    public IOppoSmsManager getOppoSmsManager(Object... vars) {
        return OppoSmsManagerImpl.getInstance();
    }

    public IOppoIccSmsInterfaceManager getOppoIccSmsInterfaceManager(Object... vars) {
        verityParamsType("getOppoIccSmsInterfaceManager", vars, 1, new Class[]{AbstractIccSmsInterfaceManager.class});
        return new OppoIccSmsInterfaceManagerReference((IccSmsInterfaceManager) OemTelephonyUtils.typeCasting(IccSmsInterfaceManager.class, (AbstractIccSmsInterfaceManager) vars[0]));
    }

    public IOppoInboundSmsHandler getOppoInboundSmsHandler(Object... vars) {
        verityParamsType("getOppoInboundSmsHandler", vars, 1, new Class[]{AbstractInboundSmsHandler.class});
        return new OppoInboundSmsHandlerReference((InboundSmsHandler) OemTelephonyUtils.typeCasting(InboundSmsHandler.class, (AbstractInboundSmsHandler) vars[0]));
    }

    public IOppoSMSDispatcher getOppoSMSDispatcher(Object... vars) {
        verityParamsType("getOppoSMSDispatcher", vars, 1, new Class[]{AbstractSMSDispatcher.class});
        AbstractSMSDispatcher ref = (AbstractSMSDispatcher) vars[0];
        if (AbstractCdmaSMSDispatcher.class.isInstance(ref)) {
            return new OppoCdmaSMSDispatcherReference((CdmaSMSDispatcher) OemTelephonyUtils.typeCasting(CdmaSMSDispatcher.class, ref));
        }
        if (AbstractGsmSMSDispatcher.class.isInstance(ref)) {
            return new OppoGsmSMSDispatcherReference((GsmSMSDispatcher) OemTelephonyUtils.typeCasting(GsmSMSDispatcher.class, ref));
        }
        if (AbstractImsSmsDispatcher.class.isInstance(ref)) {
            return new OppoImsSmsDispatcherReference((ImsSmsDispatcher) OemTelephonyUtils.typeCasting(ImsSmsDispatcher.class, ref));
        }
        return new OppoSMSDispatcherReference((SMSDispatcher) OemTelephonyUtils.typeCasting(SMSDispatcher.class, ref));
    }

    public IOppoSmsDispatchersController getOppoSmsDispatchersController(Object... vars) {
        verityParamsType("getOppoSmsDispatchersController", vars, 1, new Class[]{AbstractSmsDispatchersController.class});
        return new OppoSmsDispatchersControllerReference((SmsDispatchersController) OemTelephonyUtils.typeCasting(SmsDispatchersController.class, (AbstractSmsDispatchersController) vars[0]));
    }

    public IOppoUiccManager getOppoUiccManager(Object... vars) {
        return OppoUiccManagerImpl.getInstance();
    }

    public IOppoSIMRecords getOppoSIMRecords(Object... vars) {
        verityParamsType("getOppoSIMRecords", vars, 1, new Class[]{AbstractSIMRecords.class});
        return new OppoSIMRecordReference((AbstractSIMRecords) vars[0]);
    }

    public IOppoRuimRecords getOppoRuimRecords(Object... vars) {
        verityParamsType("getOppoRuimRecords", vars, 1, new Class[]{AbstractRuimRecords.class});
        return new OppoRuimRecordsReference((AbstractRuimRecords) vars[0]);
    }

    public IOppoSubscriptionController getOppoSubscriptionController(Object... vars) {
        verityParamsType("getOppoSubscriptionController", vars, 2, new Class[]{SubscriptionController.class, Context.class});
        return new OppoSubscriptionController((SubscriptionController) vars[0], (Context) vars[1]);
    }

    public IOppoUiccController getOppoUiccController(Object... vars) {
        verityParamsType("getOppoUiccController", vars, 1, new Class[]{UiccController.class});
        return new OppoUiccController((UiccController) vars[0]);
    }

    public IOppoAdnRecordCache getOppoAdnRecordCache(Object... vars) {
        verityParamsType("getOppoAdnRecordCache", vars, 1, new Class[]{AdnRecordCache.class});
        return new OppoAdnRecordCache((AdnRecordCache) vars[0]);
    }

    public IOppoIccPhoneBookInterfaceManager getOppoIccPhoneBookInterfaceManager(Object... vars) {
        verityParamsType("getOppoIccPhoneBookInterfaceManager", vars, 1, new Class[]{IccPhoneBookInterfaceManager.class});
        return new OppoIccPhoneBookInterfaceManager((IccPhoneBookInterfaceManager) vars[0]);
    }
}
