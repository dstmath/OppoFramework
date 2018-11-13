package com.oppo.media;

import android.os.RemoteException;

public interface IOppoAudioManager {
    public static final int GET_OPPO_STREAM_VOLUME = 100002;
    public static final int OPPO_CALL_TRANSACTION_INDEX = 100000;
    public static final int OPPO_FIRST_CALL_TRANSACTION = 100001;
    public static final String descriptor = "com.oppo.media.OppoAudioManager";

    int getOppoStreamVolume(int i) throws RemoteException;
}
