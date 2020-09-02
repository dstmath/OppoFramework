package android.os;

public interface IOppoExVibrator {
    public static final int OPPO_CALL_TRANSACTION_INDEX = 10000;
    public static final int OPPO_FIRST_CALL_TRANSACTION = 10001;
    public static final int VIBRATE_NOT_CHECK_TRANSACTION = 10002;

    void vibrateNotCheck(int i, String str, IBinder iBinder) throws RemoteException;
}
