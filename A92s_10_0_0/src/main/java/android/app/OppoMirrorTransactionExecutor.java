package android.app;

import android.app.servertransaction.TransactionExecutor;
import com.oppo.reflect.RefBoolean;
import com.oppo.reflect.RefClass;

public class OppoMirrorTransactionExecutor {
    public static RefBoolean DEBUG_RESOLVER;
    public static Class<?> TYPE = RefClass.load(OppoMirrorTransactionExecutor.class, TransactionExecutor.class);

    public static void setBooleanValue(RefBoolean refBoolean, boolean value) {
        if (refBoolean != null) {
            refBoolean.set(null, value);
        }
    }
}
