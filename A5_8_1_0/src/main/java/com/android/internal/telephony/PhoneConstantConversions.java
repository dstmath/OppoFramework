package com.android.internal.telephony;

import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneConstants.State;

public class PhoneConstantConversions {
    /* renamed from: -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues */
    private static final /* synthetic */ int[] f136xce0d2696 = null;
    /* renamed from: -com-android-internal-telephony-PhoneConstants$StateSwitchesValues */
    private static final /* synthetic */ int[] f137xb32e9020 = null;

    /* renamed from: -getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues */
    private static /* synthetic */ int[] m47x9dac0c3a() {
        if (f136xce0d2696 != null) {
            return f136xce0d2696;
        }
        int[] iArr = new int[DataState.values().length];
        try {
            iArr[DataState.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataState.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DataState.DISCONNECTED.ordinal()] = 6;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DataState.SUSPENDED.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        f136xce0d2696 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-PhoneConstants$StateSwitchesValues */
    private static /* synthetic */ int[] m48x3549e7c4() {
        if (f137xb32e9020 != null) {
            return f137xb32e9020;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.IDLE.ordinal()] = 6;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.OFFHOOK.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.RINGING.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        f137xb32e9020 = iArr;
        return iArr;
    }

    public static int convertCallState(State state) {
        switch (m48x3549e7c4()[state.ordinal()]) {
            case 1:
                return 2;
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    public static State convertCallState(int state) {
        switch (state) {
            case 1:
                return State.RINGING;
            case 2:
                return State.OFFHOOK;
            default:
                return State.IDLE;
        }
    }

    public static int convertDataState(DataState state) {
        switch (m47x9dac0c3a()[state.ordinal()]) {
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
                return 3;
            default:
                return 0;
        }
    }

    public static DataState convertDataState(int state) {
        switch (state) {
            case 1:
                return DataState.CONNECTING;
            case 2:
                return DataState.CONNECTED;
            case 3:
                return DataState.SUSPENDED;
            default:
                return DataState.DISCONNECTED;
        }
    }
}
