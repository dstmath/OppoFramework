package com.android.internal.telephony;

import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.hardware.radio.V1_4.LteVopsInfo;
import android.hardware.radio.V1_4.NrIndicators;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.LteVopsSupportInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.NetworkService;
import android.telephony.NetworkServiceCallback;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CellularNetworkService extends NetworkService {
    private static final boolean DBG = false;
    private static final int GET_CS_REGISTRATION_STATE_DONE = 1;
    private static final int GET_PS_REGISTRATION_STATE_DONE = 2;
    private static final int NETWORK_REGISTRATION_STATE_CHANGED = 3;
    private static final String TAG = CellularNetworkService.class.getSimpleName();

    private class CellularNetworkServiceProvider extends NetworkService.NetworkServiceProvider {
        private final ConcurrentHashMap<Message, NetworkServiceCallback> mCallbackMap = new ConcurrentHashMap<>();
        private final Handler mHandler;
        private final HandlerThread mHandlerThread = new HandlerThread(CellularNetworkService.class.getSimpleName());
        private final Looper mLooper;
        private final Phone mPhone = PhoneFactory.getPhone(getSlotIndex());

        CellularNetworkServiceProvider(int slotId) {
            super(CellularNetworkService.this, slotId);
            this.mHandlerThread.start();
            this.mLooper = this.mHandlerThread.getLooper();
            this.mHandler = new Handler(this.mLooper, CellularNetworkService.this) {
                /* class com.android.internal.telephony.CellularNetworkService.CellularNetworkServiceProvider.AnonymousClass1 */

                public void handleMessage(Message message) {
                    int resultCode;
                    NetworkServiceCallback callback = (NetworkServiceCallback) CellularNetworkServiceProvider.this.mCallbackMap.remove(message);
                    int i = message.what;
                    int domain = 2;
                    if (i == 1 || i == 2) {
                        if (callback != null) {
                            AsyncResult ar = (AsyncResult) message.obj;
                            if (message.what == 1) {
                                domain = 1;
                            }
                            NetworkRegistrationInfo netState = CellularNetworkServiceProvider.this.getRegistrationStateFromResult(ar.result, domain);
                            if (ar.exception != null || netState == null) {
                                resultCode = 5;
                            } else {
                                resultCode = 0;
                            }
                            try {
                                callback.onRequestNetworkRegistrationInfoComplete(resultCode, netState);
                            } catch (Exception e) {
                                CellularNetworkService.this.loge("Exception: " + e);
                            }
                        }
                    } else if (i == 3) {
                        CellularNetworkServiceProvider.this.notifyNetworkRegistrationInfoChanged();
                    }
                }
            };
            this.mPhone.mCi.registerForNetworkStateChanged(this.mHandler, 3, null);
        }

        private int getRegStateFromHalRegState(int halRegState) {
            if (halRegState != 0) {
                if (halRegState == 1) {
                    return 1;
                }
                if (halRegState != 2) {
                    if (halRegState != 3) {
                        if (halRegState != 4) {
                            if (halRegState == 5) {
                                return 5;
                            }
                            if (halRegState != 10) {
                                switch (halRegState) {
                                    case 12:
                                        break;
                                    case 13:
                                        break;
                                    case 14:
                                        break;
                                    default:
                                        return 0;
                                }
                            }
                        }
                        return 4;
                    }
                    return 3;
                }
                return 2;
            }
            return 0;
        }

        private boolean isEmergencyOnly(int halRegState) {
            switch (halRegState) {
                case 10:
                case 12:
                case 13:
                case 14:
                    return true;
                case 11:
                default:
                    return false;
            }
        }

        private List<Integer> getAvailableServices(int regState, int domain, boolean emergencyOnly) {
            List<Integer> availableServices = new ArrayList<>();
            if (emergencyOnly) {
                availableServices.add(5);
            } else if (regState == 5 || regState == 1) {
                if (domain == 2) {
                    availableServices.add(2);
                } else if (domain == 1) {
                    availableServices.add(1);
                    availableServices.add(3);
                    availableServices.add(4);
                }
            }
            return availableServices;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private NetworkRegistrationInfo getRegistrationStateFromResult(Object result, int domain) {
            if (result == null) {
                return null;
            }
            if (domain == 1) {
                return createRegistrationStateFromVoiceRegState(result);
            }
            if (domain == 2) {
                return createRegistrationStateFromDataRegState(result);
            }
            return null;
        }

        private NetworkRegistrationInfo createRegistrationStateFromVoiceRegState(Object result) {
            int networkType;
            int networkType2;
            if (result instanceof VoiceRegStateResult) {
                VoiceRegStateResult voiceRegState = (VoiceRegStateResult) result;
                int regState = getRegStateFromHalRegState(voiceRegState.regState);
                int networkType3 = ServiceState.rilRadioTechnologyToNetworkType(voiceRegState.rat);
                if (networkType3 == 19) {
                    networkType2 = 13;
                } else {
                    networkType2 = networkType3;
                }
                int reasonForDenial = voiceRegState.reasonForDenial;
                boolean emergencyOnly = isEmergencyOnly(voiceRegState.regState);
                return new NetworkRegistrationInfo(1, 1, regState, networkType2, reasonForDenial, emergencyOnly, getAvailableServices(regState, 1, emergencyOnly), convertHalCellIdentityToCellIdentity(voiceRegState.cellIdentity), voiceRegState.cssSupported, voiceRegState.roamingIndicator, voiceRegState.systemIsInPrl, voiceRegState.defaultRoamingIndicator);
            } else if (!(result instanceof android.hardware.radio.V1_2.VoiceRegStateResult)) {
                return null;
            } else {
                android.hardware.radio.V1_2.VoiceRegStateResult voiceRegState2 = (android.hardware.radio.V1_2.VoiceRegStateResult) result;
                int regState2 = getRegStateFromHalRegState(voiceRegState2.regState);
                int networkType4 = ServiceState.rilRadioTechnologyToNetworkType(voiceRegState2.rat);
                if (networkType4 == 19) {
                    networkType = 13;
                } else {
                    networkType = networkType4;
                }
                int reasonForDenial2 = voiceRegState2.reasonForDenial;
                boolean emergencyOnly2 = isEmergencyOnly(voiceRegState2.regState);
                return new NetworkRegistrationInfo(1, 1, regState2, networkType, reasonForDenial2, emergencyOnly2, getAvailableServices(regState2, 1, emergencyOnly2), convertHalCellIdentityToCellIdentity(voiceRegState2.cellIdentity), voiceRegState2.cssSupported, voiceRegState2.roamingIndicator, voiceRegState2.systemIsInPrl, voiceRegState2.defaultRoamingIndicator);
            }
        }

        /* JADX INFO: Multiple debug info for r12v7 android.telephony.CellIdentity: [D('cellIdentity' android.telephony.CellIdentity), D('dataRegState' android.hardware.radio.V1_2.DataRegStateResult)] */
        /* JADX INFO: Multiple debug info for r12v10 android.telephony.CellIdentity: [D('cellIdentity' android.telephony.CellIdentity), D('dataRegState' android.hardware.radio.V1_0.DataRegStateResult)] */
        private NetworkRegistrationInfo createRegistrationStateFromDataRegState(Object result) {
            boolean isDcNrRestricted;
            boolean isNrAvailable;
            boolean isEndcAvailable;
            int maxDataCalls;
            int reasonForDenial;
            CellIdentity cellIdentity;
            LteVopsSupportInfo lteVopsSupportInfo;
            int regState;
            boolean emergencyOnly;
            int networkType;
            boolean isUsingCarrierAggregation;
            int networkType2;
            LteVopsSupportInfo lteVopsSupportInfo2;
            LteVopsSupportInfo lteVopsSupportInfo3 = new LteVopsSupportInfo(1, 1);
            if (result instanceof DataRegStateResult) {
                DataRegStateResult dataRegState = (DataRegStateResult) result;
                int regState2 = getRegStateFromHalRegState(dataRegState.regState);
                networkType = ServiceState.rilRadioTechnologyToNetworkType(dataRegState.rat);
                int reasonForDenial2 = dataRegState.reasonDataDenied;
                boolean emergencyOnly2 = isEmergencyOnly(dataRegState.regState);
                regState = regState2;
                reasonForDenial = reasonForDenial2;
                emergencyOnly = emergencyOnly2;
                maxDataCalls = dataRegState.maxDataCalls;
                isEndcAvailable = false;
                isNrAvailable = false;
                isDcNrRestricted = false;
                lteVopsSupportInfo = lteVopsSupportInfo3;
                cellIdentity = convertHalCellIdentityToCellIdentity(dataRegState.cellIdentity);
            } else if (result instanceof android.hardware.radio.V1_2.DataRegStateResult) {
                android.hardware.radio.V1_2.DataRegStateResult dataRegState2 = (android.hardware.radio.V1_2.DataRegStateResult) result;
                int regState3 = getRegStateFromHalRegState(dataRegState2.regState);
                networkType = ServiceState.rilRadioTechnologyToNetworkType(dataRegState2.rat);
                int reasonForDenial3 = dataRegState2.reasonDataDenied;
                boolean emergencyOnly3 = isEmergencyOnly(dataRegState2.regState);
                regState = regState3;
                reasonForDenial = reasonForDenial3;
                emergencyOnly = emergencyOnly3;
                maxDataCalls = dataRegState2.maxDataCalls;
                isEndcAvailable = false;
                isNrAvailable = false;
                isDcNrRestricted = false;
                lteVopsSupportInfo = lteVopsSupportInfo3;
                cellIdentity = convertHalCellIdentityToCellIdentity(dataRegState2.cellIdentity);
            } else if (result instanceof android.hardware.radio.V1_4.DataRegStateResult) {
                android.hardware.radio.V1_4.DataRegStateResult dataRegState3 = (android.hardware.radio.V1_4.DataRegStateResult) result;
                int regState4 = getRegStateFromHalRegState(dataRegState3.base.regState);
                int networkType3 = ServiceState.rilRadioTechnologyToNetworkType(dataRegState3.base.rat);
                int reasonForDenial4 = dataRegState3.base.reasonDataDenied;
                boolean emergencyOnly4 = isEmergencyOnly(dataRegState3.base.regState);
                int maxDataCalls2 = dataRegState3.base.maxDataCalls;
                CellIdentity cellIdentity2 = convertHalCellIdentityToCellIdentity(dataRegState3.base.cellIdentity);
                NrIndicators nrIndicators = dataRegState3.nrIndicators;
                if (dataRegState3.vopsInfo.getDiscriminator() != 1) {
                    reasonForDenial = reasonForDenial4;
                } else if (ServiceState.rilRadioTechnologyToAccessNetworkType(dataRegState3.base.rat) == 3) {
                    LteVopsInfo vopsSupport = dataRegState3.vopsInfo.lteVopsInfo();
                    reasonForDenial = reasonForDenial4;
                    lteVopsSupportInfo2 = convertHalLteVopsSupportInfo(vopsSupport.isVopsSupported, vopsSupport.isEmcBearerSupported);
                    isEndcAvailable = nrIndicators.isEndcAvailable;
                    isNrAvailable = nrIndicators.isNrAvailable;
                    emergencyOnly = emergencyOnly4;
                    maxDataCalls = maxDataCalls2;
                    isDcNrRestricted = nrIndicators.isDcNrRestricted;
                    networkType = networkType3;
                    cellIdentity = cellIdentity2;
                    regState = regState4;
                    lteVopsSupportInfo = lteVopsSupportInfo2;
                } else {
                    reasonForDenial = reasonForDenial4;
                }
                lteVopsSupportInfo2 = new LteVopsSupportInfo(1, 1);
                isEndcAvailable = nrIndicators.isEndcAvailable;
                isNrAvailable = nrIndicators.isNrAvailable;
                emergencyOnly = emergencyOnly4;
                maxDataCalls = maxDataCalls2;
                isDcNrRestricted = nrIndicators.isDcNrRestricted;
                networkType = networkType3;
                cellIdentity = cellIdentity2;
                regState = regState4;
                lteVopsSupportInfo = lteVopsSupportInfo2;
            } else {
                CellularNetworkService.this.loge("Unknown type of DataRegStateResult " + result);
                return null;
            }
            List<Integer> availableServices = getAvailableServices(regState, 2, emergencyOnly);
            if (networkType == 19) {
                isUsingCarrierAggregation = true;
                networkType2 = 13;
            } else {
                networkType2 = networkType;
                isUsingCarrierAggregation = false;
            }
            return new NetworkRegistrationInfo(2, 1, regState, networkType2, reasonForDenial, emergencyOnly, availableServices, cellIdentity, maxDataCalls, isDcNrRestricted, isNrAvailable, isEndcAvailable, lteVopsSupportInfo, isUsingCarrierAggregation);
        }

        private LteVopsSupportInfo convertHalLteVopsSupportInfo(boolean vopsSupport, boolean emcBearerSupport) {
            int vops = 3;
            int emergency = 3;
            if (vopsSupport) {
                vops = 2;
            }
            if (emcBearerSupport) {
                emergency = 2;
            }
            return new LteVopsSupportInfo(vops, emergency);
        }

        private CellIdentity convertHalCellIdentityToCellIdentity(android.hardware.radio.V1_0.CellIdentity cellIdentity) {
            if (cellIdentity == null) {
                return null;
            }
            int i = cellIdentity.cellInfoType;
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i != 4) {
                            if (i == 5 && cellIdentity.cellIdentityTdscdma.size() == 1) {
                                return new CellIdentityTdscdma((android.hardware.radio.V1_0.CellIdentityTdscdma) cellIdentity.cellIdentityTdscdma.get(0));
                            }
                            return null;
                        } else if (cellIdentity.cellIdentityWcdma.size() == 1) {
                            return new CellIdentityWcdma((android.hardware.radio.V1_0.CellIdentityWcdma) cellIdentity.cellIdentityWcdma.get(0));
                        } else {
                            return null;
                        }
                    } else if (cellIdentity.cellIdentityLte.size() == 1) {
                        return new CellIdentityLte((android.hardware.radio.V1_0.CellIdentityLte) cellIdentity.cellIdentityLte.get(0));
                    } else {
                        return null;
                    }
                } else if (cellIdentity.cellIdentityCdma.size() == 1) {
                    return new CellIdentityCdma((android.hardware.radio.V1_0.CellIdentityCdma) cellIdentity.cellIdentityCdma.get(0));
                } else {
                    return null;
                }
            } else if (cellIdentity.cellIdentityGsm.size() == 1) {
                return new CellIdentityGsm((android.hardware.radio.V1_0.CellIdentityGsm) cellIdentity.cellIdentityGsm.get(0));
            } else {
                return null;
            }
        }

        private CellIdentity convertHalCellIdentityToCellIdentity(android.hardware.radio.V1_2.CellIdentity cellIdentity) {
            if (cellIdentity == null) {
                return null;
            }
            int i = cellIdentity.cellInfoType;
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i != 4) {
                            if (i == 5 && cellIdentity.cellIdentityTdscdma.size() == 1) {
                                return new CellIdentityTdscdma((android.hardware.radio.V1_2.CellIdentityTdscdma) cellIdentity.cellIdentityTdscdma.get(0));
                            }
                            return null;
                        } else if (cellIdentity.cellIdentityWcdma.size() == 1) {
                            return new CellIdentityWcdma((android.hardware.radio.V1_2.CellIdentityWcdma) cellIdentity.cellIdentityWcdma.get(0));
                        } else {
                            return null;
                        }
                    } else if (cellIdentity.cellIdentityLte.size() == 1) {
                        return new CellIdentityLte((android.hardware.radio.V1_2.CellIdentityLte) cellIdentity.cellIdentityLte.get(0));
                    } else {
                        return null;
                    }
                } else if (cellIdentity.cellIdentityCdma.size() == 1) {
                    return new CellIdentityCdma((android.hardware.radio.V1_2.CellIdentityCdma) cellIdentity.cellIdentityCdma.get(0));
                } else {
                    return null;
                }
            } else if (cellIdentity.cellIdentityGsm.size() == 1) {
                return new CellIdentityGsm((android.hardware.radio.V1_2.CellIdentityGsm) cellIdentity.cellIdentityGsm.get(0));
            } else {
                return null;
            }
        }

        public void requestNetworkRegistrationInfo(int domain, NetworkServiceCallback callback) {
            if (domain == 1) {
                Message message = Message.obtain(this.mHandler, 1);
                this.mCallbackMap.put(message, callback);
                this.mPhone.mCi.getVoiceRegistrationState(message);
            } else if (domain == 2) {
                Message message2 = Message.obtain(this.mHandler, 2);
                this.mCallbackMap.put(message2, callback);
                this.mPhone.mCi.getDataRegistrationState(message2);
            } else {
                CellularNetworkService cellularNetworkService = CellularNetworkService.this;
                cellularNetworkService.loge("requestNetworkRegistrationInfo invalid domain " + domain);
                callback.onRequestNetworkRegistrationInfoComplete(2, (NetworkRegistrationInfo) null);
            }
        }

        public void close() {
            this.mCallbackMap.clear();
            this.mHandlerThread.quit();
            this.mPhone.mCi.unregisterForNetworkStateChanged(this.mHandler);
        }
    }

    public NetworkService.NetworkServiceProvider onCreateNetworkServiceProvider(int slotIndex) {
        if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
            return new CellularNetworkServiceProvider(slotIndex);
        }
        loge("Tried to Cellular network service with invalid slotId " + slotIndex);
        return null;
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        Rlog.e(TAG, s);
    }
}
