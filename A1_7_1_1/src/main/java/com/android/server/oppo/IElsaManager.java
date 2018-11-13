package com.android.server.oppo;

import android.os.IInterface;
import android.os.RemoteException;

public interface IElsaManager extends IInterface {
    public static final int ALL_UID = -1;
    public static final String DESCRIPTOR = "neoservice";
    public static final int ELSA_ACCT_LIMIT = 3;
    public static final int ELSA_AUDIO_APP = 10;
    public static final int ELSA_AUDIO_SYS = 11;
    public static final int ELSA_BACKGROUND = 8;
    public static final int ELSA_BINDER_CODE_OFFSET = 100;
    public static final int ELSA_CPUSET_LIMIT = 2;
    public static final int ELSA_CPU_LIMIT = 1;
    public static final int ELSA_FOREGROUND = 7;
    public static final int ELSA_FREEZING1 = 12;
    public static final int ELSA_FREEZING2 = 13;
    public static final int ELSA_FREEZING3 = 14;
    public static final int ELSA_IO_LIMIT = 4;
    public static final int ELSA_MEM_LIMIT = 5;
    public static final int ELSA_NET_LIMIT = 6;
    public static final int ELSA_SYSTEM = 9;
    public static final String EMPTY_PACKAGE = "";
    public static final int TASK_PID = 1;
    public static final int TASK_TID = 2;
    public static final int TASK_UID = 4;
    public static final int TRANSACTION_ELSA_GET_CORE_LIMIT = 104;
    public static final int TRANSACTION_ELSA_GET_CPU_LOAD_LIMIT = 102;
    public static final int TRANSACTION_ELSA_GET_PACKAGE_FREEZING = 106;
    public static final int TRANSACTION_ELSA_SET_CORE_LIMIT = 103;
    public static final int TRANSACTION_ELSA_SET_CPU_LOAD_LIMIT = 101;
    public static final int TRANSACTION_ELSA_SET_PACKAGE_FREEZING = 105;
    public static final int TRANSACTION_ELSA_SET_PACKAGE_PRIORITY = 107;
    public static final int TRANSACTION_NS_REQ_PINGTEST = 1;

    int elsaGetCoreLimit(int i, int i2) throws RemoteException;

    int elsaGetCpuLoadLimit(int i, int i2) throws RemoteException;

    int elsaGetPackageFreezing(int i, int i2) throws RemoteException;

    int elsaSetCoreLimit(int i, String str, int i2, int i3, int i4) throws RemoteException;

    int elsaSetCpuLoadLimit(int i, String str, int i2, int i3, int i4) throws RemoteException;

    int elsaSetPackageFreezing(int i, String str, int i2, int i3) throws RemoteException;

    int elsaSetPackagePriority(int i, String str, int i2, int i3) throws RemoteException;

    int pingTest() throws RemoteException;
}
