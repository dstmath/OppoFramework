package com.color.inner.hardware.camera2.impl;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class CameraMetadataNativeWrapper {
    private static final int DEFAULT_VENDOR_ID_VALUE = 0;
    private static final String KEY_ANDROID = "android";
    private static final String KEY_CAMERA_CHARACTERRISTICS = "mProperties";
    private static final String KEY_GET_VENDOR_TAG = "nativeGetTagFromKey";
    private static final String KEY_LOGICAL_CAMERA_SETTINGS = "mLogicalCameraSettings";
    private static final String KEY_METADATA_PTR = "mMetadataPtr";
    private static final String KEY_METADATA_RESULT = "mResults";
    private static final String KEY_NATIVE_COPY_BUFFER = "nativeCopyBufNative";
    private static final String KEY_NATIVE_GET_BUFFER_SIZE = "nativeGetNativeBufSize";
    private static final String TAG = "CameraMetadataNativeWrapper";

    public static int copyBuf(Object obj, long address) {
        CameraMetadataNative cameraMetadataNative = getCameraMetadataNativeObj(obj);
        if (cameraMetadataNative == null) {
            return -1;
        }
        try {
            Method copyBufMethod = cameraMetadataNative.getClass().getDeclaredMethod(KEY_NATIVE_COPY_BUFFER, Long.TYPE);
            copyBufMethod.setAccessible(true);
            return ((Integer) copyBufMethod.invoke(cameraMetadataNative, Long.valueOf(address))).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int getBufSize(Object obj) {
        CameraMetadataNative cameraMetadataNative = getCameraMetadataNativeObj(obj);
        if (cameraMetadataNative == null) {
            return 0;
        }
        try {
            Method getBufSizeMethod = cameraMetadataNative.getClass().getDeclaredMethod(KEY_NATIVE_GET_BUFFER_SIZE, new Class[0]);
            getBufSizeMethod.setAccessible(true);
            return ((Integer) getBufSizeMethod.invoke(cameraMetadataNative, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public static ConcurrentHashMap<CaptureResult.Key<?>, Integer> getVendorTagId(CaptureResult result) {
        CameraMetadataNative metaDataObj;
        Object[] objArr;
        if (result == null) {
            Log.e(TAG, "getVendorTagId, CaptureResult is null, return");
            return null;
        }
        ConcurrentHashMap<CaptureResult.Key<?>, Integer> vendorKeyMap = new ConcurrentHashMap<>();
        for (CaptureResult.Key<?> key : result.getKeys()) {
            if (!key.getName().contains(KEY_ANDROID)) {
                vendorKeyMap.put(key, 0);
            }
        }
        try {
            CameraMetadataNative metaDataObj2 = getCameraMetadataNativeObj(result);
            if (metaDataObj2 == null) {
                return null;
            }
            int i = 2;
            Method getVendorTagFromKey = metaDataObj2.getClass().getDeclaredMethod(KEY_GET_VENDOR_TAG, String.class, Long.TYPE);
            getVendorTagFromKey.setAccessible(true);
            HashMap<CaptureResult.Key<?>, Integer> temp = new HashMap<>();
            for (CaptureResult.Key<?> key2 : vendorKeyMap.keySet()) {
                try {
                    objArr = new Object[i];
                    objArr[0] = new String(key2.getName());
                } catch (Exception e) {
                    metaDataObj = e;
                    Log.e(TAG, "getVendorTagId error", metaDataObj);
                    return null;
                }
                try {
                    objArr[1] = new Long(key2.getVendorId());
                    temp.put(key2, Integer.valueOf(((Integer) getVendorTagFromKey.invoke(null, objArr)).intValue()));
                    vendorKeyMap = vendorKeyMap;
                    i = 2;
                } catch (Exception e2) {
                    metaDataObj = e2;
                    Log.e(TAG, "getVendorTagId error", metaDataObj);
                    return null;
                }
            }
            ConcurrentHashMap<CaptureResult.Key<?>, Integer> vendorKeyMap2 = vendorKeyMap;
            try {
                for (CaptureResult.Key<?> tempKey : temp.keySet()) {
                    try {
                        vendorKeyMap2.put(tempKey, temp.get(tempKey));
                        vendorKeyMap2 = vendorKeyMap2;
                    } catch (Exception e3) {
                        metaDataObj = e3;
                        Log.e(TAG, "getVendorTagId error", metaDataObj);
                        return null;
                    }
                }
                return vendorKeyMap2;
            } catch (Exception e4) {
                metaDataObj = e4;
                Log.e(TAG, "getVendorTagId error", metaDataObj);
                return null;
            }
        } catch (Exception e5) {
            metaDataObj = e5;
            Log.e(TAG, "getVendorTagId error", metaDataObj);
            return null;
        }
    }

    public static ConcurrentHashMap<CaptureRequest.Key<?>, Integer> getVendorTagId(CaptureRequest request) {
        if (request == null) {
            Log.e(TAG, "getVendorTagId, request is null");
            return null;
        }
        ConcurrentHashMap<CaptureRequest.Key<?>, Integer> vendorKeyMap = new ConcurrentHashMap<>();
        for (CaptureRequest.Key<?> key : request.getKeys()) {
            if (!key.getName().contains(KEY_ANDROID)) {
                vendorKeyMap.put(key, 0);
            }
        }
        try {
            Object metaDataObj = getCameraMetadataNativeObj(request);
            if (metaDataObj != null) {
                int i = 2;
                Method getVendorTagFromKey = metaDataObj.getClass().getDeclaredMethod(KEY_GET_VENDOR_TAG, String.class, Long.TYPE);
                getVendorTagFromKey.setAccessible(true);
                HashMap<CaptureRequest.Key<?>, Integer> temp = new HashMap<>();
                for (CaptureRequest.Key<?> key2 : vendorKeyMap.keySet()) {
                    Object[] objArr = new Object[i];
                    objArr[0] = key2.getName();
                    objArr[1] = new Long(key2.getVendorId());
                    temp = temp;
                    temp.put(key2, Integer.valueOf(((Integer) getVendorTagFromKey.invoke(null, objArr)).intValue()));
                    i = 2;
                }
                for (CaptureRequest.Key<?> key3 : temp.keySet()) {
                    vendorKeyMap.put(key3, temp.get(key3));
                }
                return vendorKeyMap;
            }
        } catch (Exception e) {
            Log.e(TAG, "getVendorTagId error", e);
        }
        return null;
    }

    public static long getMetadataPtr(Object obj) {
        CameraMetadataNative cameraMetadataNative = getCameraMetadataNativeObj(obj);
        if (cameraMetadataNative == null) {
            return 0;
        }
        try {
            Field metadataPtr = cameraMetadataNative.getClass().getDeclaredField(KEY_METADATA_PTR);
            metadataPtr.setAccessible(true);
            return ((Long) metadataPtr.get(cameraMetadataNative)).longValue();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    private static CameraMetadataNative getCameraMetadataNativeObj(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof CameraCharacteristics) {
                Field metaData = obj.getClass().getDeclaredField(KEY_CAMERA_CHARACTERRISTICS);
                metaData.setAccessible(true);
                return (CameraMetadataNative) metaData.get(obj);
            } else if (obj instanceof TotalCaptureResult) {
                Field metaData2 = obj.getClass().getSuperclass().getDeclaredField(KEY_METADATA_RESULT);
                metaData2.setAccessible(true);
                return (CameraMetadataNative) metaData2.get(obj);
            } else if (obj instanceof CaptureResult) {
                Field metaData3 = obj.getClass().getDeclaredField(KEY_METADATA_RESULT);
                metaData3.setAccessible(true);
                return (CameraMetadataNative) metaData3.get(obj);
            } else if (!(obj instanceof CaptureRequest)) {
                return null;
            } else {
                Field fLogicalSetting = obj.getClass().getDeclaredField(KEY_LOGICAL_CAMERA_SETTINGS);
                fLogicalSetting.setAccessible(true);
                return (CameraMetadataNative) fLogicalSetting.get(obj);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
