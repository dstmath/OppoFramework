package com.android.internal.inputmethod;

import android.os.IBinder;
import com.android.internal.annotations.GuardedBy;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public final class InputMethodPrivilegedOperationsRegistry {
    private static final Object sLock = new Object();
    private static InputMethodPrivilegedOperations sNop;
    @GuardedBy({"sLock"})
    private static WeakHashMap<IBinder, WeakReference<InputMethodPrivilegedOperations>> sRegistry;

    private InputMethodPrivilegedOperationsRegistry() {
    }

    private static InputMethodPrivilegedOperations getNopOps() {
        if (sNop == null) {
            sNop = new InputMethodPrivilegedOperations();
        }
        return sNop;
    }

    public static void put(IBinder token, InputMethodPrivilegedOperations ops) {
        synchronized (sLock) {
            if (sRegistry == null) {
                sRegistry = new WeakHashMap<>();
            }
            WeakReference<InputMethodPrivilegedOperations> previousOps = sRegistry.put(token, new WeakReference<>(ops));
            if (previousOps != null) {
                throw new IllegalStateException(previousOps.get() + " is already registered for  this token=" + token + " newOps=" + ops);
            }
        }
    }

    public static InputMethodPrivilegedOperations get(IBinder token) {
        synchronized (sLock) {
            if (sRegistry == null) {
                InputMethodPrivilegedOperations nopOps = getNopOps();
                return nopOps;
            }
            WeakReference<InputMethodPrivilegedOperations> wrapperRef = sRegistry.get(token);
            if (wrapperRef == null) {
                InputMethodPrivilegedOperations nopOps2 = getNopOps();
                return nopOps2;
            }
            InputMethodPrivilegedOperations wrapper = wrapperRef.get();
            if (wrapper != null) {
                return wrapper;
            }
            InputMethodPrivilegedOperations nopOps3 = getNopOps();
            return nopOps3;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        return;
     */
    public static void remove(IBinder token) {
        synchronized (sLock) {
            if (sRegistry != null) {
                sRegistry.remove(token);
                if (sRegistry.isEmpty()) {
                    sRegistry = null;
                }
            }
        }
    }

    public static boolean isRegistered(IBinder token) {
        synchronized (sLock) {
            if (sRegistry == null) {
                return false;
            }
            boolean containsKey = sRegistry.containsKey(token);
            return containsKey;
        }
    }
}
