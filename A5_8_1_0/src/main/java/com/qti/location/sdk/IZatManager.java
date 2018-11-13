package com.qti.location.sdk;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.qti.debugreport.IDebugReportCallback.Stub;
import com.qti.debugreport.IDebugReportService;
import com.qti.flp.IFlpService;
import com.qti.flp.ILocationCallback;
import com.qti.flp.IMaxPowerAllocatedCallback;
import com.qti.flp.ISessionStatusCallback;
import com.qti.flp.ITestService;
import com.qti.geofence.GeofenceData;
import com.qti.geofence.IGeofenceCallback;
import com.qti.geofence.IGeofenceService;
import com.qti.izat.IIzatService;
import com.qti.location.sdk.IZatDebugReportingService.IZatDebugReportCallback;
import com.qti.location.sdk.IZatFlpService.IFlpLocationCallback;
import com.qti.location.sdk.IZatFlpService.IFlpStatusCallback;
import com.qti.location.sdk.IZatFlpService.IZatFlpSessionHandle;
import com.qti.location.sdk.IZatFlpService.IzatFlpBgRequest;
import com.qti.location.sdk.IZatFlpService.IzatFlpBgRequest.IzatFlpBgRequestMode;
import com.qti.location.sdk.IZatFlpService.IzatFlpRequest;
import com.qti.location.sdk.IZatFlpService.IzatFlpStatus;
import com.qti.location.sdk.IZatGeofenceService.IZatGeofenceCallback;
import com.qti.location.sdk.IZatGeofenceService.IZatGeofenceHandle;
import com.qti.location.sdk.IZatGeofenceService.IzatDwellNotify;
import com.qti.location.sdk.IZatGeofenceService.IzatGeofence;
import com.qti.location.sdk.IZatGeofenceService.IzatGeofenceConfidence;
import com.qti.location.sdk.IZatGeofenceService.IzatGeofenceTransitionTypes;
import com.qti.location.sdk.IZatTestService.IFlpMaxPowerAllocatedCallback;
import com.qti.location.sdk.IZatWiFiDBReceiver.IZatAPInfo;
import com.qti.location.sdk.IZatWiFiDBReceiver.IZatAPInfoExtra;
import com.qti.location.sdk.IZatWiFiDBReceiver.IZatAPLocationData;
import com.qti.location.sdk.IZatWiFiDBReceiver.IZatAPSSIDInfo;
import com.qti.location.sdk.IZatWiFiDBReceiver.IZatAPSpecialInfo;
import com.qti.location.sdk.IZatWiFiDBReceiver.IZatCellInfo;
import com.qti.location.sdk.IZatWiFiDBReceiver.IZatCellInfo.IZatCellTypes;
import com.qti.location.sdk.IZatWiFiDBReceiver.IZatWiFiDBReceiverResponseListener;
import com.qti.wifidbreceiver.APInfo;
import com.qti.wifidbreceiver.APLocationData;
import com.qti.wifidbreceiver.APSpecialInfo;
import com.qti.wifidbreceiver.IWiFiDBReceiver;
import com.qti.wifidbreceiver.IWiFiDBReceiverResponseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class IZatManager {
    private static final int FLP_PASSIVE_LISTENER_HW_ID = -1;
    private static final int FLP_RESULT_FAILURE = -1;
    private static final int FLP_RESULT_SUCCESS = 0;
    private static final int FLP_SEESION_BACKGROUND = 1;
    private static final int FLP_SEESION_FOREROUND = 2;
    private static final int FLP_SEESION_PASSIVE = 4;
    private static final int GEOFENCE_DWELL_TYPE_INSIDE = 1;
    private static final int GEOFENCE_DWELL_TYPE_OUTSIDE = 2;
    private static final String REMOTE_IZAT_SERVICE_NAME = "com.qualcomm.location.izat.IzatService";
    private static final String SDK_Version = "4.0.1";
    private static String TAG = "IZatManager";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private static final Object sDebugReportCallbackMapLock = new Object();
    private static final Object sDebugReportServiceLock = new Object();
    private static volatile int sFlpRequestsCnt = FLP_RESULT_SUCCESS;
    private static final Object sFlpServiceLock = new Object();
    private static final Object sGeofenceServiceLock = new Object();
    private static IZatManager sInstance = null;
    private static IIzatService sIzatService = null;
    private static final Object sTestServiceLock = new Object();
    private static final Object sWiFiDBReceiverLock = new Object();
    private final int FEATURE_BIT_ADAPTIVE_BATCHING_IS_SUPPORTED = 4;
    private final int FEATURE_BIT_DISTANCE_BASED_BATCHING_IS_SUPPORTED = 8;
    private final int FEATURE_BIT_DISTANCE_BASED_TRACKING_IS_SUPPORTED = 2;
    private final int FEATURE_BIT_OUTDOOR_TRIP_BATCHING_IS_SUPPORTED = 16;
    private final int FEATURE_BIT_TIME_BASED_BATCHING_IS_SUPPORTED = 1;
    private Context mContext;
    private DebugReportCallbackWrapper mDebugReportCbWrapper = new DebugReportCallbackWrapper(this, null);
    private Map<IZatDebugReportingServiceImpl, IZatDebugReportCallback> mDebugReportClientCallbackMap = createDebugReportClientCallbackMap();
    private int mFlpFeaturMasks = -1;
    private Map<IFlpMaxPowerAllocatedCallback, MaxPowerAllocatedCallbackWrapper> mFlpMaxPowerCbMap = createMaxPowerCbMap();
    private Map<IFlpLocationCallback, LocationCallbackWrapper> mFlpPassiveCbMap = createPassiveCbMap();
    private Map<IZatSessionHandlerImpl, FlpRequestMapItem> mFlpRequestsMap = createIdMap();
    private GeofenceStatusCallbackWrapper mGeofenceCbWrapper = new GeofenceStatusCallbackWrapper(this, null);
    private Map<IZatGeofenceServiceImpl, IZatGeofenceCallback> mGeofenceClientCallbackMap = createGeofenceClientCallbackMap();
    private Map<IZatGeofenceHandleImpl, GeofenceMapItem> mGeofencesMap = createGeofencesMap();
    private IZatWiFiDBReceiverImpl mWiFiDBRecImpl = null;
    private WiFiDBReceiverRespListenerWrapper mWiFiDBReceiverRespListenerWrapper = new WiFiDBReceiverRespListenerWrapper(this, null);

    private class DebugReportCallbackWrapper extends Stub {
        /* synthetic */ DebugReportCallbackWrapper(IZatManager this$0, DebugReportCallbackWrapper -this1) {
            this();
        }

        private DebugReportCallbackWrapper() {
        }

        public void onDebugDataAvailable(Bundle debugReport) {
            if (IZatManager.VERBOSE) {
                Log.v(IZatManager.TAG, "onDebugDataAvailable");
            }
            synchronized (IZatManager.sDebugReportCallbackMapLock) {
                if (!IZatManager.this.mDebugReportClientCallbackMap.isEmpty()) {
                    for (IZatDebugReportingServiceImpl key : IZatManager.this.mDebugReportClientCallbackMap.keySet()) {
                        ((IZatDebugReportCallback) IZatManager.this.mDebugReportClientCallbackMap.get(key)).onDebugReportAvailable(debugReport);
                    }
                }
            }
        }
    }

    private class FlpRequestMapItem {
        public IFlpLocationCallback mCallback = null;
        public LocationCallbackWrapper mCbWrapper = null;
        public int mHwId = -1;
        public int mMaxDistance = -1;
        public long mMaxTime = -1;
        public NotificationType mNotiType = null;
        private NotificationType mPreviousNotifType = null;
        private boolean mRestartOnTripCompleted = false;
        private long mSessionStartTime = -1;
        public FlpStatusCallbackWrapper mStatusCbWrapper = null;
        public long mTripDistance = -1;

        public FlpRequestMapItem(IFlpLocationCallback callback, NotificationType notiType, long maxTime, int maxDistance, long tripDistance, LocationCallbackWrapper cbWrapper, int hwId, long sessionStartTime) {
            this.mCallback = callback;
            this.mNotiType = notiType;
            this.mPreviousNotifType = notiType;
            this.mMaxTime = maxTime;
            this.mMaxDistance = maxDistance;
            this.mTripDistance = tripDistance;
            this.mCbWrapper = cbWrapper;
            this.mHwId = hwId;
            this.mSessionStartTime = sessionStartTime;
            this.mRestartOnTripCompleted = false;
            this.mStatusCbWrapper = null;
        }

        public IFlpLocationCallback getCallback() {
            return this.mCallback;
        }

        public void updateNotifyType(NotificationType type) {
            this.mNotiType = type;
            if (type != NotificationType.NOTIFICATION_WHEN_TRIP_IS_COMPLETED) {
                this.mPreviousNotifType = type;
            }
        }

        public NotificationType getNotifyType() {
            return this.mNotiType;
        }

        public NotificationType getPreviousNotifyType() {
            return this.mPreviousNotifType;
        }

        public long getTimeInterval() {
            return this.mMaxTime;
        }

        public int getDistanceInterval() {
            return this.mMaxDistance;
        }

        public long getTripDistance() {
            return this.mTripDistance;
        }

        public LocationCallbackWrapper getCbWrapper() {
            return this.mCbWrapper;
        }

        public int getHwId() {
            return this.mHwId;
        }

        public long getSessionStartTime() {
            return this.mSessionStartTime;
        }

        public void setSessionStartTime(long sessionStartTime) {
            this.mSessionStartTime = sessionStartTime;
        }

        public void setRestartOnTripCompleted(boolean restart) {
            this.mRestartOnTripCompleted = restart;
        }

        public boolean getRestartOnTripCompleted() {
            return this.mRestartOnTripCompleted;
        }

        public void setStatusCallback(FlpStatusCallbackWrapper cbWrapper) {
            this.mStatusCbWrapper = cbWrapper;
        }

        public FlpStatusCallbackWrapper getStatusCallback() {
            return this.mStatusCbWrapper;
        }
    }

    private class FlpStatusCallbackWrapper extends ISessionStatusCallback.Stub {
        IFlpStatusCallback mCallback;
        IFlpService mService;

        public FlpStatusCallbackWrapper(IFlpStatusCallback callback, IFlpService flpService) {
            this.mCallback = callback;
            this.mService = flpService;
        }

        public void onBatchingStatusCb(int status) {
            if (this.mCallback == null) {
                Log.w(IZatManager.TAG, "mCallback is NULL in FlpStatusCallbackWrapper");
            }
            try {
                synchronized (IZatManager.sFlpServiceLock) {
                    IzatFlpStatus batchStatus = IzatFlpStatus.values()[status];
                    if (batchStatus != IzatFlpStatus.OUTDOOR_TRIP_COMPLETED) {
                        this.mCallback.onBatchingStatusCb(batchStatus);
                        return;
                    }
                    FlpRequestMapItem mapItem = null;
                    IZatSessionHandlerImpl sessionHandler = null;
                    for (IZatSessionHandlerImpl key : IZatManager.this.mFlpRequestsMap.keySet()) {
                        mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(key);
                        if (mapItem.getStatusCallback() == this) {
                            sessionHandler = key;
                            break;
                        }
                    }
                    if (mapItem == null) {
                        Log.w(IZatManager.TAG, "no flp session undergoing");
                        return;
                    }
                    NotificationType notifType = mapItem.getNotifyType();
                    if (batchStatus == IzatFlpStatus.OUTDOOR_TRIP_COMPLETED && notifType == NotificationType.NOTIFICATION_WHEN_TRIP_IS_COMPLETED) {
                        this.mCallback.onBatchingStatusCb(batchStatus);
                        if (!mapItem.getRestartOnTripCompleted()) {
                            this.mService.unregisterCallback(1, mapItem.getCbWrapper());
                            sessionHandler.unregisterForSessionStatus();
                            IZatManager.this.mFlpRequestsMap.remove(sessionHandler);
                        } else if (this.mService.startFlpSession(mapItem.getHwId(), mapItem.getPreviousNotifyType().getCode(), mapItem.getTimeInterval(), mapItem.getDistanceInterval(), mapItem.getTripDistance()) == 0) {
                            mapItem.updateNotifyType(mapItem.getPreviousNotifyType());
                            mapItem.setRestartOnTripCompleted(false);
                            if ((IZatManager.this.mFlpFeaturMasks & 8) > 0 && mapItem.getNotifyType() == NotificationType.NOTIFICATION_ON_EVERY_LOCATION_FIX) {
                                this.mService.unregisterCallback(2, mapItem.getCbWrapper());
                                this.mService.registerCallback(1, mapItem.getHwId(), mapItem.getCbWrapper(), mapItem.getSessionStartTime());
                            }
                        } else {
                            Log.v(IZatManager.TAG, "mService.updateFlpSession on trip completed failed.");
                        }
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException("Failed to handle onBatchingStatusCb for status:" + status);
            }
        }
    }

    private class GeofenceMapItem {
        IZatGeofenceCallback mCallback;
        Object mContext;
        int mHWGeofenceId;

        public GeofenceMapItem(Object context, int geofenceId, IZatGeofenceCallback callback) {
            this.mContext = context;
            this.mHWGeofenceId = geofenceId;
            this.mCallback = callback;
        }

        public Object getContext() {
            return this.mContext;
        }

        public int getHWGeofenceId() {
            return this.mHWGeofenceId;
        }

        public IZatGeofenceCallback getCallback() {
            return this.mCallback;
        }
    }

    private class GeofenceStatusCallbackWrapper extends IGeofenceCallback.Stub {
        /* synthetic */ GeofenceStatusCallbackWrapper(IZatManager this$0, GeofenceStatusCallbackWrapper -this1) {
            this();
        }

        private GeofenceStatusCallbackWrapper() {
        }

        public void onTransitionEvent(int geofenceHwId, int event, Location location) {
            if (IZatManager.VERBOSE) {
                Log.d(IZatManager.TAG, "onTransitionEvent - geofenceHwId is " + geofenceHwId + "; event is " + event);
            }
            if (!IZatManager.this.mGeofencesMap.isEmpty()) {
                for (IZatGeofenceHandleImpl key : IZatManager.this.mGeofencesMap.keySet()) {
                    GeofenceMapItem mapItem = (GeofenceMapItem) IZatManager.this.mGeofencesMap.get(key);
                    if (mapItem.getHWGeofenceId() == geofenceHwId) {
                        mapItem.getCallback().onTransitionEvent(key, event, location);
                        return;
                    }
                }
            }
        }

        public void onRequestResultReturned(int geofenceHwId, int requestType, int result) {
            if (IZatManager.VERBOSE) {
                Log.d(IZatManager.TAG, "onRequestResultReturned - geofenceHwId is " + geofenceHwId + "; requestType is " + requestType + "; result is " + result);
            }
            if ((result != 0 || requestType == 2) && !IZatManager.this.mGeofencesMap.isEmpty()) {
                for (IZatGeofenceHandleImpl key : IZatManager.this.mGeofencesMap.keySet()) {
                    GeofenceMapItem mapItem = (GeofenceMapItem) IZatManager.this.mGeofencesMap.get(key);
                    if (mapItem.getHWGeofenceId() == geofenceHwId) {
                        if (result != 0) {
                            if (requestType == 1) {
                                IZatManager.this.mGeofencesMap.remove(key);
                            }
                            mapItem.getCallback().onRequestFailed(key, requestType, result);
                        } else if (requestType == 2) {
                            IZatManager.this.mGeofencesMap.remove(key);
                        }
                        return;
                    }
                }
            }
        }

        public void onEngineReportStatus(int status, Location location) {
            if (IZatManager.VERBOSE) {
                Log.d(IZatManager.TAG, "onEngineReportStatus - status is " + status);
            }
            if (!IZatManager.this.mGeofenceClientCallbackMap.isEmpty()) {
                for (IZatGeofenceServiceImpl key : IZatManager.this.mGeofenceClientCallbackMap.keySet()) {
                    ((IZatGeofenceCallback) IZatManager.this.mGeofenceClientCallbackMap.get(key)).onEngineReportStatus(status, location);
                }
            }
        }
    }

    public class IZatDebugReportingServiceImpl implements IZatDebugReportingService {
        IDebugReportService mService;

        public IZatDebugReportingServiceImpl(IDebugReportService service) {
            this.mService = service;
        }

        public void registerForDebugReports(IZatDebugReportCallback reportCb) throws IZatIllegalArgumentException {
            if (reportCb == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            synchronized (IZatManager.sDebugReportCallbackMapLock) {
                IZatManager.this.mDebugReportClientCallbackMap.put(this, reportCb);
            }
            synchronized (IZatManager.sDebugReportServiceLock) {
                if (!IZatManager.this.mDebugReportClientCallbackMap.isEmpty()) {
                    try {
                        this.mService.startReporting();
                    } catch (RemoteException e) {
                        throw new RuntimeException("Failed to register for debug reports");
                    }
                }
            }
        }

        public void deregisterForDebugReports(IZatDebugReportCallback reportCb) throws IZatIllegalArgumentException {
            if (reportCb == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            synchronized (IZatManager.sDebugReportCallbackMapLock) {
                if (((IZatDebugReportCallback) IZatManager.this.mDebugReportClientCallbackMap.get(this)) != null) {
                    IZatManager.this.mDebugReportClientCallbackMap.remove(this);
                }
            }
            synchronized (IZatManager.sDebugReportServiceLock) {
                if (IZatManager.this.mDebugReportClientCallbackMap.isEmpty()) {
                    try {
                        this.mService.stopReporting();
                    } catch (RemoteException e) {
                        throw new RuntimeException("Failed to deregister for debug reports");
                    }
                }
            }
        }

        public Bundle getDebugReport() throws IZatIllegalArgumentException {
            Bundle bdlReportObj;
            synchronized (IZatManager.sDebugReportServiceLock) {
                try {
                    bdlReportObj = this.mService.getDebugReport();
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed to get debug report");
                }
            }
            return bdlReportObj;
        }
    }

    private class IZatFlpServiceImpl implements IZatFlpService {
        IFlpService mService;

        private class IZatSessionHandlerImpl implements IZatFlpSessionHandle {
            /* synthetic */ IZatSessionHandlerImpl(IZatFlpServiceImpl this$1, IZatSessionHandlerImpl -this1) {
                this();
            }

            private IZatSessionHandlerImpl() {
            }

            public void pullLocations() {
                try {
                    synchronized (IZatManager.sFlpServiceLock) {
                        FlpRequestMapItem mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(this);
                        if (mapItem == null) {
                            Log.w(IZatManager.TAG, "no flp session undergoing");
                        } else if (mapItem.getCbWrapper() == null) {
                            Log.w(IZatManager.TAG, "no available callback");
                        } else {
                            int result = IZatFlpServiceImpl.this.mService.pullLocations(mapItem.getCbWrapper(), mapItem.getSessionStartTime(), mapItem.getHwId());
                            if (result == 0) {
                                mapItem.setSessionStartTime(System.currentTimeMillis());
                            }
                            if (IZatManager.VERBOSE) {
                                Log.v(IZatManager.TAG, "pullLocations() returning : " + result);
                            }
                        }
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed pullLocations", e);
                }
            }

            public void setToForeground() {
                try {
                    synchronized (IZatManager.sFlpServiceLock) {
                        FlpRequestMapItem mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(this);
                        if (mapItem == null) {
                            Log.w(IZatManager.TAG, "no flp session undergoing");
                        } else if (mapItem.getCbWrapper() == null) {
                            Log.w(IZatManager.TAG, "no available callback");
                        } else {
                            int result = IZatManager.FLP_RESULT_SUCCESS;
                            if (mapItem.getNotifyType() != NotificationType.NOTIFICATION_ON_EVERY_LOCATION_FIX) {
                                result = IZatFlpServiceImpl.this.mService.updateFlpSession(mapItem.getHwId(), NotificationType.NOTIFICATION_ON_EVERY_LOCATION_FIX.getCode(), mapItem.getTimeInterval(), mapItem.getDistanceInterval(), 0);
                                if (result == 0) {
                                    mapItem.updateNotifyType(NotificationType.NOTIFICATION_ON_EVERY_LOCATION_FIX);
                                    IZatManager.this.mFlpRequestsMap.put(this, mapItem);
                                    if ((IZatManager.this.mFlpFeaturMasks & 8) > 0) {
                                        IZatFlpServiceImpl.this.mService.unregisterCallback(1, mapItem.getCbWrapper());
                                        IZatFlpServiceImpl.this.mService.registerCallback(2, mapItem.getHwId(), mapItem.getCbWrapper(), mapItem.getSessionStartTime());
                                    }
                                } else {
                                    Log.v(IZatManager.TAG, "mService.updateFlpSession failed.");
                                }
                            }
                            if (IZatManager.VERBOSE) {
                                Log.v(IZatManager.TAG, "setToForeground() returning : " + result);
                            }
                        }
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed setToForeground", e);
                }
            }

            public void setToBackground() {
                try {
                    synchronized (IZatManager.sFlpServiceLock) {
                        FlpRequestMapItem mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(this);
                        if (mapItem == null) {
                            Log.w(IZatManager.TAG, "no flp session undergoing");
                        } else if (mapItem.getCbWrapper() == null) {
                            Log.w(IZatManager.TAG, "no available callback");
                        } else {
                            int result = IZatManager.FLP_RESULT_SUCCESS;
                            if (mapItem.getNotifyType() != NotificationType.NOTIFICATION_WHEN_BUFFER_IS_FULL) {
                                result = IZatFlpServiceImpl.this.mService.updateFlpSession(mapItem.getHwId(), NotificationType.NOTIFICATION_WHEN_BUFFER_IS_FULL.getCode(), mapItem.getTimeInterval(), mapItem.getDistanceInterval(), 0);
                                if (result == 0) {
                                    mapItem.updateNotifyType(NotificationType.NOTIFICATION_WHEN_BUFFER_IS_FULL);
                                    IZatManager.this.mFlpRequestsMap.put(this, mapItem);
                                    if ((IZatManager.this.mFlpFeaturMasks & 8) > 0) {
                                        IZatFlpServiceImpl.this.mService.unregisterCallback(2, mapItem.getCbWrapper());
                                        IZatFlpServiceImpl.this.mService.registerCallback(1, mapItem.getHwId(), mapItem.getCbWrapper(), mapItem.getSessionStartTime());
                                    }
                                } else {
                                    Log.v(IZatManager.TAG, "mService.updateFlpSession failed.");
                                }
                            }
                            if (IZatManager.VERBOSE) {
                                Log.v(IZatManager.TAG, "setToBackground() returning : " + result);
                            }
                        }
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed setToBackground", e);
                }
            }

            public void setToTripMode() throws IZatFeatureNotSupportedException {
                if ((IZatManager.this.mFlpFeaturMasks & 16) == 0) {
                    throw new IZatFeatureNotSupportedException("Outdoor trip mode is not supported.");
                }
                try {
                    synchronized (IZatManager.sFlpServiceLock) {
                        FlpRequestMapItem mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(this);
                        if (mapItem == null) {
                            Log.w(IZatManager.TAG, "no flp session undergoing");
                        } else if (mapItem.getCbWrapper() == null) {
                            Log.w(IZatManager.TAG, "no available callback");
                        } else if ((IZatManager.this.mFlpFeaturMasks & 16) == 0) {
                            Log.w(IZatManager.TAG, "Outdoor Trip mode not supported");
                        } else {
                            int result = IZatManager.FLP_RESULT_SUCCESS;
                            if (mapItem.getNotifyType() != NotificationType.NOTIFICATION_WHEN_TRIP_IS_COMPLETED) {
                                result = IZatFlpServiceImpl.this.mService.updateFlpSession(mapItem.getHwId(), NotificationType.NOTIFICATION_WHEN_TRIP_IS_COMPLETED.getCode(), mapItem.getTimeInterval(), mapItem.getDistanceInterval(), mapItem.getTripDistance());
                                if (result == 0) {
                                    mapItem.updateNotifyType(NotificationType.NOTIFICATION_WHEN_TRIP_IS_COMPLETED);
                                    mapItem.setRestartOnTripCompleted(true);
                                    IZatManager.this.mFlpRequestsMap.put(this, mapItem);
                                    if ((IZatManager.this.mFlpFeaturMasks & 8) > 0 && mapItem.getPreviousNotifyType() == NotificationType.NOTIFICATION_ON_EVERY_LOCATION_FIX) {
                                        IZatFlpServiceImpl.this.mService.unregisterCallback(2, mapItem.getCbWrapper());
                                        IZatFlpServiceImpl.this.mService.registerCallback(1, mapItem.getHwId(), mapItem.getCbWrapper(), mapItem.getSessionStartTime());
                                    }
                                } else {
                                    Log.v(IZatManager.TAG, "mService.updateFlpSession failed.");
                                }
                            }
                            if (IZatManager.VERBOSE) {
                                Log.v(IZatManager.TAG, "setToTripMode() returning : " + result);
                            }
                        }
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed setToTripMode", e);
                }
            }

            public void registerForSessionStatus(IFlpStatusCallback callback) throws IZatIllegalArgumentException, IZatFeatureNotSupportedException {
                if ((IZatManager.this.mFlpFeaturMasks & 16) == 0) {
                    throw new IZatFeatureNotSupportedException("Session status not supported.");
                } else if (callback == null) {
                    throw new IZatIllegalArgumentException("invalid input parameter");
                } else {
                    try {
                        synchronized (IZatManager.sFlpServiceLock) {
                            FlpRequestMapItem mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(this);
                            if (mapItem == null) {
                                Log.w(IZatManager.TAG, "no flp session undergoing");
                                return;
                            }
                            FlpStatusCallbackWrapper cbWrapper = new FlpStatusCallbackWrapper(callback, IZatFlpServiceImpl.this.mService);
                            mapItem.setStatusCallback(cbWrapper);
                            IZatFlpServiceImpl.this.mService.registerForSessionStatus(mapItem.getHwId(), cbWrapper);
                        }
                    } catch (RemoteException e) {
                        throw new RuntimeException("Failed registerForSessionStatus", e);
                    }
                }
            }

            public void unregisterForSessionStatus() throws IZatIllegalArgumentException, IZatFeatureNotSupportedException {
                if ((IZatManager.this.mFlpFeaturMasks & 16) == 0) {
                    throw new IZatFeatureNotSupportedException("Session status not supported.");
                }
                try {
                    synchronized (IZatManager.sFlpServiceLock) {
                        FlpRequestMapItem mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(this);
                        if (mapItem == null) {
                            Log.w(IZatManager.TAG, "no flp session undergoing");
                            return;
                        }
                        FlpStatusCallbackWrapper cbWrapper = mapItem.getStatusCallback();
                        if (cbWrapper == null) {
                            Log.w(IZatManager.TAG, "no status callback wrapper is registered.");
                        } else {
                            IZatFlpServiceImpl.this.mService.unregisterForSessionStatus(cbWrapper);
                            mapItem.setStatusCallback(null);
                        }
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed unregisterForSessionStatus", e);
                }
            }
        }

        public IZatFlpServiceImpl(IFlpService service) {
            this.mService = service;
        }

        public IZatFlpSessionHandle startFlpSession(IFlpLocationCallback callback, IzatFlpRequest flpRequest) throws IZatIllegalArgumentException {
            if (callback == null || flpRequest == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            } else if (flpRequest.getTimeInterval() > 0 || flpRequest.getDistanceInterval() > 0 || flpRequest.getTripDistance() > 0) {
                try {
                    synchronized (IZatManager.sFlpServiceLock) {
                        NotificationType notifType = NotificationType.NOTIFICATION_ON_EVERY_LOCATION_FIX;
                        if (flpRequest.mIsRunningInBackground && (flpRequest instanceof IzatFlpBgRequest)) {
                            IzatFlpBgRequestMode activeMode = ((IzatFlpBgRequest) flpRequest).getActiveMode();
                            notifType = NotificationType.NOTIFICATION_WHEN_BUFFER_IS_FULL;
                            if (activeMode == IzatFlpBgRequestMode.TRIP_MODE) {
                                if ((IZatManager.this.mFlpFeaturMasks & 16) == 0) {
                                    throw new IZatFeatureNotSupportedException("Outdoor trip mode is not supported.");
                                } else if (flpRequest.getTripDistance() > 0) {
                                    notifType = NotificationType.NOTIFICATION_WHEN_TRIP_IS_COMPLETED;
                                }
                            }
                        }
                        if (!IZatManager.this.mFlpRequestsMap.isEmpty()) {
                            for (IZatSessionHandlerImpl key : IZatManager.this.mFlpRequestsMap.keySet()) {
                                FlpRequestMapItem mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(key);
                                if (mapItem.getCallback() == callback && mapItem.getNotifyType() == notifType && mapItem.getTimeInterval() == flpRequest.getTimeInterval() && mapItem.getDistanceInterval() == flpRequest.getDistanceInterval() && mapItem.getTripDistance() == flpRequest.getTripDistance()) {
                                    throw new IZatIllegalArgumentException("this session started already.");
                                }
                            }
                        }
                        int hwId = IZatManager.sFlpRequestsCnt;
                        IZatManager.sFlpRequestsCnt = hwId + 1;
                        long sessionStartTime = System.currentTimeMillis();
                        LocationCallbackWrapper cbWrapper = new LocationCallbackWrapper(callback);
                        if ((IZatManager.this.mFlpFeaturMasks & 8) <= 0) {
                            this.mService.registerCallback(4, hwId, cbWrapper, sessionStartTime);
                        } else if (flpRequest.mIsRunningInBackground) {
                            this.mService.registerCallback(1, hwId, cbWrapper, sessionStartTime);
                        } else {
                            this.mService.registerCallback(2, hwId, cbWrapper, sessionStartTime);
                        }
                        int result = this.mService.startFlpSession(hwId, notifType.getCode(), flpRequest.getTimeInterval(), flpRequest.getDistanceInterval(), flpRequest.getTripDistance());
                        if (IZatManager.VERBOSE) {
                            Log.v(IZatManager.TAG, "startFlpSession() returning : " + result);
                        }
                        if (result == 0) {
                            IZatSessionHandlerImpl iZatSessionHandlerImpl = new IZatSessionHandlerImpl(this, null);
                            iZatSessionHandlerImpl = iZatSessionHandlerImpl;
                            IZatManager.this.mFlpRequestsMap.put(iZatSessionHandlerImpl, new FlpRequestMapItem(callback, notifType, flpRequest.getTimeInterval(), flpRequest.getDistanceInterval(), flpRequest.getTripDistance(), cbWrapper, hwId, sessionStartTime));
                            return iZatSessionHandlerImpl;
                        }
                        if ((IZatManager.this.mFlpFeaturMasks & 8) <= 0) {
                            this.mService.unregisterCallback(4, cbWrapper);
                        } else if (flpRequest.mIsRunningInBackground) {
                            this.mService.unregisterCallback(1, cbWrapper);
                        } else {
                            this.mService.unregisterCallback(2, cbWrapper);
                        }
                        IZatManager.sFlpRequestsCnt = IZatManager.sFlpRequestsCnt - 1;
                        return null;
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Failed startFlpSession", e);
                }
            } else {
                throw new IZatIllegalArgumentException("Atleast one of the parameters in time, distance interval and trip distance must be valid");
            }
        }

        public void stopFlpSession(IZatFlpSessionHandle handler) throws IZatIllegalArgumentException {
            if (handler == null || ((handler instanceof IZatSessionHandlerImpl) ^ 1) != 0) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            try {
                synchronized (IZatManager.sFlpServiceLock) {
                    FlpRequestMapItem mapItem = (FlpRequestMapItem) IZatManager.this.mFlpRequestsMap.get(handler);
                    if (mapItem == null) {
                        Log.e(IZatManager.TAG, "this request ID is unknown.");
                    } else if (this.mService.stopFlpSession(mapItem.getHwId()) != 0) {
                        Log.e(IZatManager.TAG, "stopFlpSession() failed. ");
                    } else {
                        if ((IZatManager.this.mFlpFeaturMasks & 8) <= 0) {
                            this.mService.unregisterCallback(4, mapItem.getCbWrapper());
                        } else if (mapItem.getNotifyType() == NotificationType.NOTIFICATION_WHEN_BUFFER_IS_FULL || mapItem.getNotifyType() == NotificationType.NOTIFICATION_WHEN_TRIP_IS_COMPLETED) {
                            this.mService.unregisterCallback(1, mapItem.getCbWrapper());
                        } else if (mapItem.getNotifyType() == NotificationType.NOTIFICATION_ON_EVERY_LOCATION_FIX) {
                            this.mService.unregisterCallback(2, mapItem.getCbWrapper());
                        }
                        IZatManager.this.mFlpRequestsMap.remove(handler);
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException("Failed stopFlpSession", e);
            }
        }

        public void registerForPassiveLocations(IFlpLocationCallback callback) throws IZatIllegalArgumentException {
            if (callback == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            try {
                synchronized (IZatManager.sFlpServiceLock) {
                    if (IZatManager.this.mFlpPassiveCbMap.get(callback) == null) {
                        LocationCallbackWrapper cbWrapper = new LocationCallbackWrapper(callback);
                        IZatManager.this.mFlpPassiveCbMap.put(callback, cbWrapper);
                        this.mService.registerCallback(4, -1, cbWrapper, System.currentTimeMillis());
                    } else {
                        Log.w(IZatManager.TAG, "this passive callback is already registered.");
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException("Failed registerForPassiveLocations", e);
            }
        }

        public void deregisterForPassiveLocations(IFlpLocationCallback callback) throws IZatIllegalArgumentException {
            if (callback == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            try {
                synchronized (IZatManager.sFlpServiceLock) {
                    LocationCallbackWrapper cbWrapper = (LocationCallbackWrapper) IZatManager.this.mFlpPassiveCbMap.get(callback);
                    if (cbWrapper == null) {
                        Log.w(IZatManager.TAG, "this passive callback is not registered.");
                    } else {
                        this.mService.unregisterCallback(4, cbWrapper);
                        IZatManager.this.mFlpPassiveCbMap.remove(callback);
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException("Failed deregisterForPassiveLocations", e);
            }
        }
    }

    private class IZatGeofenceServiceImpl implements IZatGeofenceService {
        IGeofenceService mService;

        private class IZatGeofenceHandleImpl implements IZatGeofenceHandle {
            /* synthetic */ IZatGeofenceHandleImpl(IZatGeofenceServiceImpl this$1, IZatGeofenceHandleImpl -this1) {
                this();
            }

            private IZatGeofenceHandleImpl() {
            }

            public Object getContext() {
                GeofenceMapItem mapItem = (GeofenceMapItem) IZatManager.this.mGeofencesMap.get(this);
                if (mapItem != null) {
                    return mapItem.getContext();
                }
                return null;
            }

            public void update(IzatGeofenceTransitionTypes transitionTypes, int notifyResponsiveness) throws IZatIllegalArgumentException {
                if (transitionTypes == null || notifyResponsiveness <= 0) {
                    throw new IZatIllegalArgumentException("invalid input parameter");
                }
                try {
                    synchronized (IZatManager.sGeofenceServiceLock) {
                        GeofenceMapItem mapItem = (GeofenceMapItem) IZatManager.this.mGeofencesMap.get(this);
                        if (mapItem == null) {
                            Log.e(IZatManager.TAG, "this request ID is unknown.");
                            IZatGeofenceCallback cb = (IZatGeofenceCallback) IZatManager.this.mGeofenceClientCallbackMap.get(this);
                            if (cb != null) {
                                cb.onRequestFailed(this, 5, IZatGeofenceService.GEOFENCE_RESULT_ERROR_ID_UNKNOWN);
                                return;
                            }
                            throw new IZatIllegalArgumentException("Invalid Geofence handle");
                        }
                        IZatGeofenceServiceImpl.this.mService.updateGeofence(mapItem.getHWGeofenceId(), transitionTypes.getValue(), notifyResponsiveness);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed removeGeofence", e);
                }
            }

            public void pause() throws IZatIllegalArgumentException {
                try {
                    synchronized (IZatManager.sGeofenceServiceLock) {
                        GeofenceMapItem mapItem = (GeofenceMapItem) IZatManager.this.mGeofencesMap.get(this);
                        if (mapItem == null) {
                            Log.e(IZatManager.TAG, "this request ID is unknown.");
                            IZatGeofenceCallback cb = (IZatGeofenceCallback) IZatManager.this.mGeofenceClientCallbackMap.get(this);
                            if (cb != null) {
                                cb.onRequestFailed(this, 3, IZatGeofenceService.GEOFENCE_RESULT_ERROR_ID_UNKNOWN);
                                return;
                            }
                            throw new IZatIllegalArgumentException("Invalid Geofence handle");
                        }
                        IZatGeofenceServiceImpl.this.mService.pauseGeofence(mapItem.getHWGeofenceId());
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed removeGeofence", e);
                }
            }

            public void resume() throws IZatIllegalArgumentException {
                try {
                    synchronized (IZatManager.sGeofenceServiceLock) {
                        GeofenceMapItem mapItem = (GeofenceMapItem) IZatManager.this.mGeofencesMap.get(this);
                        if (mapItem == null) {
                            Log.e(IZatManager.TAG, "this request ID is unknown.");
                            IZatGeofenceCallback cb = (IZatGeofenceCallback) IZatManager.this.mGeofenceClientCallbackMap.get(this);
                            if (cb != null) {
                                cb.onRequestFailed(this, 4, IZatGeofenceService.GEOFENCE_RESULT_ERROR_ID_UNKNOWN);
                                return;
                            }
                            throw new IZatIllegalArgumentException("Invalid Geofence handle");
                        }
                        IZatGeofenceServiceImpl.this.mService.resumeGeofence(mapItem.getHWGeofenceId());
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed removeGeofence", e);
                }
            }
        }

        public IZatGeofenceServiceImpl(IGeofenceService service) {
            this.mService = service;
        }

        public void registerForGeofenceCallbacks(IZatGeofenceCallback statusCb) throws IZatIllegalArgumentException {
            if (statusCb == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            IZatManager.this.mGeofenceClientCallbackMap.put(this, statusCb);
        }

        public void deregisterForGeofenceCallbacks(IZatGeofenceCallback statusCb) throws IZatIllegalArgumentException {
            if (statusCb == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            } else if (((IZatGeofenceCallback) IZatManager.this.mGeofenceClientCallbackMap.get(this)) != null) {
                IZatManager.this.mGeofenceClientCallbackMap.remove(this);
            }
        }

        public void registerPendingIntent(PendingIntent geofenceIntent) throws IZatIllegalArgumentException {
            if (geofenceIntent == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            synchronized (IZatManager.sGeofenceServiceLock) {
                try {
                    this.mService.registerPendingIntent(geofenceIntent);
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed registerPendingIntent");
                }
            }
        }

        public void unregisterPendingIntent(PendingIntent geofenceIntent) throws IZatIllegalArgumentException {
            if (geofenceIntent == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            synchronized (IZatManager.sGeofenceServiceLock) {
                try {
                    this.mService.unregisterPendingIntent(geofenceIntent);
                } catch (RemoteException e) {
                    throw new RuntimeException("Failed unregisterPendingIntent");
                }
            }
        }

        public Map<IZatGeofenceHandle, IzatGeofence> recoverGeofences() {
            IZatGeofenceCallback cb = (IZatGeofenceCallback) IZatManager.this.mGeofenceClientCallbackMap.get(this);
            if (cb == null) {
                Log.e(IZatManager.TAG, "callback is not registered");
                return null;
            }
            Map<IZatGeofenceHandle, IzatGeofence> gfHandleDataMap = new HashMap();
            ArrayList<GeofenceData> gfList = new ArrayList();
            try {
                synchronized (IZatManager.sGeofenceServiceLock) {
                    this.mService.recoverGeofences(gfList);
                    for (GeofenceData geofence : gfList) {
                        boolean handleExists = false;
                        IzatGeofence gfObj = new IzatGeofence(geofence.getLatitude(), geofence.getLongitude(), geofence.getRadius());
                        gfObj.setNotifyResponsiveness(geofence.getNotifyResponsiveness());
                        gfObj.setTransitionTypes(IzatGeofenceTransitionTypes.values()[geofence.getTransitionType().getValue()]);
                        gfObj.setConfidence(IzatGeofenceConfidence.values()[geofence.getConfidence().getValue() - 1]);
                        gfObj.setDwellNotify(new IzatDwellNotify(geofence.getDwellTime(), geofence.getDwellType().getValue()));
                        for (Entry<IZatGeofenceHandleImpl, GeofenceMapItem> entry : IZatManager.this.mGeofencesMap.entrySet()) {
                            if (((GeofenceMapItem) entry.getValue()).getHWGeofenceId() == geofence.getGeofenceId()) {
                                handleExists = true;
                                gfHandleDataMap.put((IZatGeofenceHandle) entry.getKey(), gfObj);
                                break;
                            }
                        }
                        if (!handleExists) {
                            Object appData;
                            IZatGeofenceHandleImpl iZatGeofenceHandleImpl = new IZatGeofenceHandleImpl(this, null);
                            String appTextData = geofence.getAppTextData();
                            if (appTextData != null) {
                                appData = appTextData;
                            } else {
                                appData = geofence.getAppBundleData();
                            }
                            IZatManager.this.mGeofencesMap.put(iZatGeofenceHandleImpl, new GeofenceMapItem(appData, geofence.getGeofenceId(), cb));
                            gfHandleDataMap.put(iZatGeofenceHandleImpl, gfObj);
                        }
                    }
                }
                return gfHandleDataMap;
            } catch (RemoteException e) {
                throw new RuntimeException("Failed to recover geofences", e);
            }
        }

        public IZatGeofenceHandle addGeofence(Object context, IzatGeofence geofence) throws IZatIllegalArgumentException {
            if (geofence == null) {
                throw new IZatIllegalArgumentException("invalid null geofence");
            } else if (geofence.getLatitude() < -90.0d || geofence.getLatitude() > 90.0d) {
                throw new IZatIllegalArgumentException("invalid geofence latitude. Expected in range -90..90.");
            } else if (geofence.getLongitude() < -180.0d || geofence.getLongitude() > 180.0d) {
                throw new IZatIllegalArgumentException("invalid geofence longitude. Expected in range -180..180.");
            } else {
                IZatGeofenceCallback cb = (IZatGeofenceCallback) IZatManager.this.mGeofenceClientCallbackMap.get(this);
                if (cb == null) {
                    Log.e(IZatManager.TAG, "callback is not registered.");
                    return null;
                }
                try {
                    IZatGeofenceHandleImpl iZatGeofenceHandleImpl;
                    synchronized (IZatManager.sGeofenceServiceLock) {
                        int geofenceId;
                        double latitude = geofence.getLatitude();
                        double longitude = geofence.getLongitude();
                        double radius = geofence.getRadius();
                        int transitionType = geofence.getTransitionTypes().getValue();
                        int responsiveness = geofence.getNotifyResponsiveness();
                        int confidence = geofence.getConfidence().getValue();
                        int dwellTime = IZatManager.FLP_RESULT_SUCCESS;
                        int dwellType = IZatManager.FLP_RESULT_SUCCESS;
                        if (geofence.getDwellNotify() != null) {
                            dwellTime = geofence.getDwellNotify().getDwellTime();
                            dwellType = geofence.getDwellNotify().getDwellType();
                        }
                        if (context == null || !((context instanceof String) || (context instanceof Bundle))) {
                            geofenceId = this.mService.addGeofence(latitude, longitude, radius, transitionType, responsiveness, confidence, dwellTime, dwellType);
                        } else {
                            geofenceId = this.mService.addGeofenceObj(new GeofenceData(responsiveness, latitude, longitude, radius, transitionType, confidence, dwellType, dwellTime, context instanceof String ? context.toString() : null, context instanceof Bundle ? (Bundle) context : null, -1));
                        }
                        iZatGeofenceHandleImpl = new IZatGeofenceHandleImpl(this, null);
                        IZatManager.this.mGeofencesMap.put(iZatGeofenceHandleImpl, new GeofenceMapItem(context, geofenceId, cb));
                    }
                    return iZatGeofenceHandleImpl;
                } catch (Throwable e) {
                    throw new RuntimeException("Failed addGeofence", e);
                }
            }
        }

        public void removeGeofence(IZatGeofenceHandle handler) throws IZatIllegalArgumentException {
            if (handler == null || ((handler instanceof IZatGeofenceHandleImpl) ^ 1) != 0) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            try {
                synchronized (IZatManager.sGeofenceServiceLock) {
                    GeofenceMapItem mapItem = (GeofenceMapItem) IZatManager.this.mGeofencesMap.get(handler);
                    if (mapItem == null) {
                        Log.e(IZatManager.TAG, "this request ID is unknown.");
                        IZatGeofenceCallback cb = (IZatGeofenceCallback) IZatManager.this.mGeofenceClientCallbackMap.get(handler);
                        if (cb != null) {
                            cb.onRequestFailed(handler, 2, IZatGeofenceService.GEOFENCE_RESULT_ERROR_ID_UNKNOWN);
                            return;
                        }
                        throw new IZatIllegalArgumentException("Invalid Geofence handle");
                    }
                    this.mService.removeGeofence(mapItem.getHWGeofenceId());
                    IZatManager.this.mGeofencesMap.remove(handler);
                }
            } catch (RemoteException e) {
                throw new RuntimeException("Failed removeGeofence", e);
            }
        }
    }

    private class IZatTestServiceImpl implements IZatTestService {
        ITestService mService;

        public IZatTestServiceImpl(ITestService service) {
            this.mService = service;
        }

        public void deleteAidingData(long flags) throws IZatIllegalArgumentException {
            if (flags == 0) {
                throw new IZatIllegalArgumentException("invalid input parameter. flags must be filled");
            }
            try {
                this.mService.deleteAidingData(flags);
            } catch (RemoteException e) {
                throw new RuntimeException("Failed deregisterForPassiveLocations", e);
            }
        }

        public void registerForMaxPowerAllocatedChange(IFlpMaxPowerAllocatedCallback callback) throws IZatIllegalArgumentException {
            if (callback == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            try {
                synchronized (IZatManager.sTestServiceLock) {
                    if (IZatManager.this.mFlpMaxPowerCbMap.get(callback) == null) {
                        MaxPowerAllocatedCallbackWrapper cbWrapper = new MaxPowerAllocatedCallbackWrapper(callback);
                        IZatManager.this.mFlpMaxPowerCbMap.put(callback, cbWrapper);
                        this.mService.registerMaxPowerChangeCallback(cbWrapper);
                    } else {
                        Log.w(IZatManager.TAG, "this max power callback is already registered.");
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException("Failed registerForMaxPowerAllocatedChange", e);
            }
        }

        public void deregisterForMaxPowerAllocatedChange(IFlpMaxPowerAllocatedCallback callback) throws IZatIllegalArgumentException {
            if (callback == null) {
                throw new IZatIllegalArgumentException("invalid input parameter");
            }
            try {
                synchronized (IZatManager.sTestServiceLock) {
                    MaxPowerAllocatedCallbackWrapper cbWrapper = (MaxPowerAllocatedCallbackWrapper) IZatManager.this.mFlpMaxPowerCbMap.get(callback);
                    if (cbWrapper == null) {
                        Log.w(IZatManager.TAG, "this passive callback is not registered.");
                    } else {
                        this.mService.unregisterMaxPowerChangeCallback(cbWrapper);
                        IZatManager.this.mFlpMaxPowerCbMap.remove(callback);
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException("Failed deregisterForMaxPowerAllocatedChange", e);
            }
        }
    }

    private class IZatWiFiDBReceiverImpl extends IZatWiFiDBReceiver {
        IWiFiDBReceiver mReceiver;

        public IZatWiFiDBReceiverImpl(IWiFiDBReceiver receiver, IZatWiFiDBReceiverResponseListener listener) throws IZatIllegalArgumentException {
            super(listener);
            if (receiver == null || listener == null) {
                throw new IZatIllegalArgumentException("IZatWiFiDBReceiverImpl: null receiver / listener");
            }
            this.mReceiver = receiver;
        }

        public void requestAPList(int expire_in_days) {
            try {
                this.mReceiver.requestAPList(expire_in_days);
            } catch (RemoteException e) {
                throw new RuntimeException("Failed to request AP LIst", e);
            }
        }

        public void pushWiFiDB(List<IZatAPLocationData> location_data, List<IZatAPSpecialInfo> special_info, int days_valid) {
            List<APLocationData> locationData = new ArrayList();
            List<APSpecialInfo> specialInfo = new ArrayList();
            for (IZatAPLocationData loc_data : location_data) {
                if (loc_data != null) {
                    APLocationData locData = new APLocationData();
                    locData.mMacAddress = loc_data.getMacAddress();
                    locData.mLatitude = loc_data.getLatitude();
                    locData.mLongitude = loc_data.getLongitude();
                    locData.mValidBits = IZatManager.FLP_RESULT_SUCCESS;
                    try {
                        locData.mMaxAntenaRange = loc_data.getMaxAntenaRange();
                        locData.mValidBits |= 1;
                    } catch (IZatStaleDataException e) {
                        Log.e(IZatManager.TAG, "MAR exception ");
                    }
                    try {
                        locData.mHorizontalError = loc_data.getHorizontalError();
                        locData.mValidBits |= 2;
                    } catch (IZatStaleDataException e2) {
                        Log.e(IZatManager.TAG, "HE exception ");
                    }
                    try {
                        locData.mReliability = loc_data.getReliability().ordinal();
                        locData.mValidBits |= 4;
                    } catch (IZatStaleDataException e3) {
                        Log.e(IZatManager.TAG, "REL exception ");
                    }
                    locationData.add(locData);
                }
            }
            for (IZatAPSpecialInfo spl_info : special_info) {
                if (spl_info != null) {
                    APSpecialInfo info = new APSpecialInfo();
                    info.mMacAddress = spl_info.mMacAddress;
                    info.mInfo = spl_info.mInfo.ordinal();
                    specialInfo.add(info);
                }
            }
            try {
                this.mReceiver.pushWiFiDB(locationData, specialInfo, days_valid);
            } catch (RemoteException e4) {
                throw new RuntimeException("Failed to push WiFi DB", e4);
            }
        }
    }

    private class LocationCallbackWrapper extends ILocationCallback.Stub {
        IFlpLocationCallback mCallback;

        public LocationCallbackWrapper(IFlpLocationCallback callback) {
            this.mCallback = callback;
        }

        public void onLocationAvailable(Location[] locations) {
            if (this.mCallback == null) {
                Log.w(IZatManager.TAG, "mCallback is NULL in LocationCallbackWrapper");
            } else {
                this.mCallback.onLocationAvailable(locations);
            }
        }
    }

    private class MaxPowerAllocatedCallbackWrapper extends IMaxPowerAllocatedCallback.Stub {
        IFlpMaxPowerAllocatedCallback mCallback;

        public MaxPowerAllocatedCallbackWrapper(IFlpMaxPowerAllocatedCallback callback) {
            this.mCallback = callback;
        }

        public void onMaxPowerAllocatedChanged(double power_mW) {
            if (this.mCallback == null) {
                Log.w(IZatManager.TAG, "mCallback is NULL in MaxPowerAllocatedCallbackWrapper");
            } else {
                this.mCallback.onMaxPowerAllocatedChanged(power_mW);
            }
        }
    }

    private enum NotificationType {
        NOTIFICATION_WHEN_BUFFER_IS_FULL(1),
        NOTIFICATION_ON_EVERY_LOCATION_FIX(2),
        NOTIFICATION_WHEN_TRIP_IS_COMPLETED(3);
        
        private final int mCode;

        private NotificationType(int c) {
            this.mCode = c;
        }

        public int getCode() {
            return this.mCode;
        }
    }

    private class WiFiDBReceiverRespListenerWrapper extends IWiFiDBReceiverResponseListener.Stub {
        /* synthetic */ WiFiDBReceiverRespListenerWrapper(IZatManager this$0, WiFiDBReceiverRespListenerWrapper -this1) {
            this();
        }

        private WiFiDBReceiverRespListenerWrapper() {
        }

        public void onAPListAvailable(List<APInfo> ap_info) {
            if (IZatManager.VERBOSE) {
                Log.d(IZatManager.TAG, "onAPListAvailable");
            }
            if (IZatManager.this.mWiFiDBRecImpl != null) {
                List<IZatAPInfo> apInfo = new ArrayList();
                for (APInfo ap : ap_info) {
                    IZatCellInfo cellInfo = null;
                    switch (ap.mCellType) {
                        case 1:
                            cellInfo = new IZatCellInfo(ap.mCellRegionID1, ap.mCellRegionID2, ap.mCellRegionID3, ap.mCellRegionID4, IZatCellTypes.GSM);
                            break;
                        case 2:
                            cellInfo = new IZatCellInfo(ap.mCellRegionID1, ap.mCellRegionID2, ap.mCellRegionID3, ap.mCellRegionID4, IZatCellTypes.WCDMA);
                            break;
                        case 3:
                            cellInfo = new IZatCellInfo(ap.mCellRegionID1, ap.mCellRegionID2, ap.mCellRegionID3, ap.mCellRegionID4, IZatCellTypes.CDMA);
                            break;
                        case 4:
                            cellInfo = new IZatCellInfo(ap.mCellRegionID1, ap.mCellRegionID2, ap.mCellRegionID3, ap.mCellRegionID4, IZatCellTypes.LTE);
                            break;
                    }
                    IZatAPSSIDInfo iZatAPSSIDInfo = null;
                    if (ap.mSSID != null && ap.mSSID.length > 0) {
                        iZatAPSSIDInfo = new IZatAPSSIDInfo(ap.mSSID, (short) ap.mSSID.length);
                    }
                    apInfo.add(new IZatAPInfo(ap.mMacAddress, new IZatAPInfoExtra(cellInfo, iZatAPSSIDInfo)));
                }
                IZatManager.this.mWiFiDBRecImpl.mResponseListener.onAPListAvailable(apInfo);
            }
        }

        public void onStatusUpdate(boolean is_success, String error) {
            if (IZatManager.VERBOSE) {
                Log.d(IZatManager.TAG, "onStatusUpdate");
            }
            if (IZatManager.this.mWiFiDBRecImpl != null) {
                IZatManager.this.mWiFiDBRecImpl.mResponseListener.onStatusUpdate(is_success, error);
            }
        }

        public void onServiceRequest() {
            if (IZatManager.VERBOSE) {
                Log.d(IZatManager.TAG, "onServiceRequest");
            }
            if (IZatManager.this.mWiFiDBRecImpl != null) {
                IZatManager.this.mWiFiDBRecImpl.mResponseListener.onServiceRequest();
            }
        }
    }

    private Map<IZatSessionHandlerImpl, FlpRequestMapItem> createIdMap() {
        return Collections.synchronizedMap(new HashMap());
    }

    private Map<IFlpLocationCallback, LocationCallbackWrapper> createPassiveCbMap() {
        return Collections.synchronizedMap(new HashMap());
    }

    private Map<IFlpMaxPowerAllocatedCallback, MaxPowerAllocatedCallbackWrapper> createMaxPowerCbMap() {
        return Collections.synchronizedMap(new HashMap());
    }

    private Map<IZatGeofenceServiceImpl, IZatGeofenceCallback> createGeofenceClientCallbackMap() {
        return Collections.synchronizedMap(new HashMap());
    }

    private Map<IZatGeofenceHandleImpl, GeofenceMapItem> createGeofencesMap() {
        return Collections.synchronizedMap(new HashMap());
    }

    private Map<IZatDebugReportingServiceImpl, IZatDebugReportCallback> createDebugReportClientCallbackMap() {
        return Collections.synchronizedMap(new HashMap());
    }

    public static synchronized IZatManager getInstance(Context context) throws IZatIllegalArgumentException {
        IZatManager iZatManager;
        synchronized (IZatManager.class) {
            if (context == null) {
                throw new IZatIllegalArgumentException("null argument");
            }
            if (sInstance == null) {
                sInstance = new IZatManager(context);
            }
            iZatManager = sInstance;
        }
        return iZatManager;
    }

    private IZatManager(Context context) {
        sFlpRequestsCnt = Process.myTid() << 8;
        if (VERBOSE) {
            Log.v(TAG, "IZatManager ctor sFlpRequestsCnt=" + sFlpRequestsCnt);
        }
        this.mContext = context;
    }

    public String getVersion() throws IZatServiceUnavailableException {
        if (sIzatService == null) {
            connectIzatService();
        }
        try {
            String service_version = sIzatService.getVersion();
            if (service_version == null) {
                service_version = "1.0.0";
            }
            return "4.0.1:" + service_version;
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get IzatService version", e);
        }
    }

    private synchronized void connectIzatService() {
        if (sIzatService == null) {
            if (VERBOSE) {
                Log.d(TAG, "Connecting to Izat service by name [com.qualcomm.location.izat.IzatService]");
            }
            if (this.mContext.getPackageManager().resolveService(new Intent(REMOTE_IZAT_SERVICE_NAME), FLP_RESULT_SUCCESS) == null) {
                Log.e(TAG, "Izat service (com.qualcomm.location.izat.IzatService) not installed");
                throw new IZatServiceUnavailableException("Izat service unavailable.");
            } else if (ServiceManager.getService(REMOTE_IZAT_SERVICE_NAME) == null) {
                Log.e(TAG, "Izat service (com.qualcomm.location.izat.IzatService) is not started");
                throw new IZatServiceUnavailableException("Izat service not started.");
            } else {
                sIzatService = IIzatService.Stub.asInterface(ServiceManager.getService(REMOTE_IZAT_SERVICE_NAME));
                if (sIzatService == null) {
                    Log.e(TAG, "Izat service (com.qualcomm.location.izat.IzatService) not started");
                    throw new IZatServiceUnavailableException("Izat service unavailable.");
                }
            }
        }
    }

    public IZatFlpService connectFlpService() throws IZatServiceUnavailableException {
        if (sIzatService == null) {
            connectIzatService();
        }
        try {
            IFlpService flpService = sIzatService.getFlpService();
            if (flpService == null) {
                throw new IZatServiceUnavailableException("FLP Service is not available.");
            }
            synchronized (sFlpServiceLock) {
                if (this.mFlpFeaturMasks == -1) {
                    this.mFlpFeaturMasks = flpService.getAllSupportedFeatures();
                }
            }
            if ((this.mFlpFeaturMasks & 2) > 0) {
                return new IZatFlpServiceImpl(flpService);
            }
            Log.e(TAG, "Izat FLP positioning is not supported on this device.");
            return null;
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get IFlpService", e);
        }
    }

    public void disconnectFlpService(IZatFlpService service) throws IZatIllegalArgumentException {
        if (service == null || ((service instanceof IZatFlpServiceImpl) ^ 1) != 0) {
            throw new IZatIllegalArgumentException();
        }
        try {
            synchronized (sFlpServiceLock) {
                IFlpService flpService = sIzatService.getFlpService();
                if (flpService == null) {
                    throw new IZatServiceUnavailableException("FLP Service is not available.");
                }
                if (!this.mFlpRequestsMap.isEmpty()) {
                    for (IZatSessionHandlerImpl key : this.mFlpRequestsMap.keySet()) {
                        FlpRequestMapItem mapItem = (FlpRequestMapItem) this.mFlpRequestsMap.get(key);
                        if (flpService.stopFlpSession(mapItem.getHwId()) != 0) {
                            Log.e(TAG, "stopFlpSession failed in disconnecting");
                            return;
                        }
                        if ((this.mFlpFeaturMasks & 8) > 0) {
                            if (mapItem.getNotifyType() == NotificationType.NOTIFICATION_WHEN_BUFFER_IS_FULL) {
                                flpService.unregisterCallback(1, mapItem.getCbWrapper());
                            }
                            if (mapItem.getNotifyType() == NotificationType.NOTIFICATION_ON_EVERY_LOCATION_FIX) {
                                flpService.unregisterCallback(2, mapItem.getCbWrapper());
                            }
                        } else {
                            flpService.unregisterCallback(4, mapItem.getCbWrapper());
                        }
                        if (mapItem.getStatusCallback() != null) {
                            flpService.unregisterForSessionStatus(mapItem.getStatusCallback());
                        }
                        this.mFlpRequestsMap.remove(key);
                    }
                }
                if (!this.mFlpPassiveCbMap.isEmpty()) {
                    for (IFlpLocationCallback key2 : this.mFlpPassiveCbMap.keySet()) {
                        flpService.unregisterCallback(4, (LocationCallbackWrapper) this.mFlpPassiveCbMap.get(key2));
                        this.mFlpPassiveCbMap.remove(key2);
                    }
                }
            }
        } catch (RemoteException e) {
            throw new RuntimeException("Failed stop all flp sessions", e);
        }
    }

    public IZatTestService connectTestService() throws IZatServiceUnavailableException {
        if (sIzatService == null) {
            connectIzatService();
        }
        try {
            ITestService testService = sIzatService.getTestService();
            if (testService != null) {
                return new IZatTestServiceImpl(testService);
            }
            throw new IZatServiceUnavailableException("Test Service is not available.");
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get ITestService", e);
        }
    }

    public void disconnectTestService(IZatTestService service) throws IZatIllegalArgumentException {
        if (service == null || ((service instanceof IZatTestServiceImpl) ^ 1) != 0) {
            throw new IZatIllegalArgumentException();
        } else if (!this.mFlpMaxPowerCbMap.isEmpty()) {
            synchronized (sTestServiceLock) {
                for (IFlpMaxPowerAllocatedCallback key : this.mFlpMaxPowerCbMap.keySet()) {
                    service.deregisterForMaxPowerAllocatedChange(key);
                    this.mFlpMaxPowerCbMap.remove(key);
                }
            }
        }
    }

    public IZatGeofenceService connectGeofenceService() throws IZatServiceUnavailableException {
        if (sIzatService == null) {
            connectIzatService();
        }
        try {
            IGeofenceService geofenceService = sIzatService.getGeofenceService();
            if (geofenceService == null) {
                throw new IZatServiceUnavailableException("Geofence Service is not available.");
            }
            synchronized (sGeofenceServiceLock) {
                geofenceService.registerCallback(this.mGeofenceCbWrapper);
            }
            return new IZatGeofenceServiceImpl(geofenceService);
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get IGeofenceService", e);
        }
    }

    public void disconnectGeofenceService(IZatGeofenceService service) throws IZatIllegalArgumentException {
        if (service == null || ((service instanceof IZatGeofenceServiceImpl) ^ 1) != 0) {
            throw new IZatIllegalArgumentException();
        }
        try {
            synchronized (sGeofenceServiceLock) {
                IGeofenceService geofenceService = sIzatService.getGeofenceService();
                if (geofenceService == null) {
                    throw new IZatServiceUnavailableException("Geofence Service is not available.");
                }
                if (!this.mGeofencesMap.isEmpty()) {
                    for (IZatGeofenceHandleImpl key : this.mGeofencesMap.keySet()) {
                        geofenceService.removeGeofence(((GeofenceMapItem) this.mGeofencesMap.get(key)).getHWGeofenceId());
                        this.mGeofencesMap.remove(key);
                    }
                }
                geofenceService.unregisterCallback(this.mGeofenceCbWrapper);
                if (!this.mGeofenceClientCallbackMap.isEmpty()) {
                    for (IZatGeofenceService key2 : this.mGeofenceClientCallbackMap.keySet()) {
                        if (key2 == service) {
                            this.mGeofenceClientCallbackMap.remove(key2);
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to remove all geofence added", e);
        }
    }

    public IZatDebugReportingService connectDebugReportingService() throws IZatServiceUnavailableException {
        if (sIzatService == null) {
            connectIzatService();
        }
        try {
            IDebugReportService debugReportService = sIzatService.getDebugReportService();
            if (debugReportService == null) {
                throw new IZatServiceUnavailableException("Debug Reporting Service is not available.");
            }
            synchronized (sDebugReportServiceLock) {
                debugReportService.registerForDebugReporting(this.mDebugReportCbWrapper);
            }
            return new IZatDebugReportingServiceImpl(debugReportService);
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get IDebugReportService", e);
        }
    }

    public void disconnectDebugReportingService(IZatDebugReportingService service) {
        if (service == null || ((service instanceof IZatDebugReportingServiceImpl) ^ 1) != 0) {
            throw new IZatIllegalArgumentException();
        }
        try {
            IDebugReportService debugReportService = sIzatService.getDebugReportService();
            if (debugReportService == null) {
                throw new IZatServiceUnavailableException("Debug Report Service is not available.");
            }
            synchronized (sDebugReportServiceLock) {
                debugReportService.unregisterForDebugReporting(this.mDebugReportCbWrapper);
            }
            synchronized (sDebugReportCallbackMapLock) {
                this.mDebugReportClientCallbackMap.clear();
            }
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to disconnect DebugReportService", e);
        }
    }

    public IZatWiFiDBReceiver connectToWiFiDBReceiver(IZatWiFiDBReceiverResponseListener listener) throws IZatServiceUnavailableException {
        if (sIzatService == null) {
            connectIzatService();
        }
        try {
            if (this.mWiFiDBRecImpl == null) {
                IWiFiDBReceiver wifiDBReceiver = sIzatService.getWiFiDBReceiver();
                synchronized (sWiFiDBReceiverLock) {
                    wifiDBReceiver.registerResponseListener(this.mWiFiDBReceiverRespListenerWrapper);
                }
                this.mWiFiDBRecImpl = new IZatWiFiDBReceiverImpl(wifiDBReceiver, listener);
            }
            return this.mWiFiDBRecImpl;
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to get IWiFiDBReceiver", e);
        }
    }

    public void disconnectFromWiFiDBReceiver(IZatWiFiDBReceiver receiver) throws IZatIllegalArgumentException {
        if (receiver == null || ((receiver instanceof IZatWiFiDBReceiverImpl) ^ 1) != 0) {
            throw new IZatIllegalArgumentException();
        }
        try {
            IWiFiDBReceiver wifiDBReceiver = sIzatService.getWiFiDBReceiver();
            synchronized (sWiFiDBReceiverLock) {
                wifiDBReceiver.removeResponseListener(this.mWiFiDBReceiverRespListenerWrapper);
            }
            this.mWiFiDBRecImpl = null;
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to disconnect WiFiDBReceiver", e);
        }
    }
}
