package android.app.servertransaction;

import android.app.ActivityThread;
import android.app.ClientTransactionHandler;
import android.os.IBinder;
import android.util.IntArray;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;

public class TransactionExecutor {
    public static boolean DEBUG_RESOLVER = false;
    private static final String TAG = "TransactionExecutor";
    private TransactionExecutorHelper mHelper = new TransactionExecutorHelper();
    private PendingTransactionActions mPendingActions = new PendingTransactionActions();
    private ClientTransactionHandler mTransactionHandler;

    public TransactionExecutor(ClientTransactionHandler clientTransactionHandler) {
        this.mTransactionHandler = clientTransactionHandler;
    }

    public void execute(ClientTransaction transaction) {
        Map<IBinder, ClientTransactionItem> activitiesToBeDestroyed;
        ClientTransactionItem destroyItem;
        if (DEBUG_RESOLVER) {
            try {
                Slog.d(TAG, TransactionExecutorHelper.tId(transaction) + "Start resolving transaction");
            } catch (Exception e) {
                Slog.e(TAG, "Start resolving transaction Exception : " + e);
            }
        }
        IBinder token = transaction.getActivityToken();
        if (!(token == null || (destroyItem = (activitiesToBeDestroyed = this.mTransactionHandler.getActivitiesToBeDestroyed()).get(token)) == null)) {
            if (transaction.getLifecycleStateRequest() == destroyItem) {
                activitiesToBeDestroyed.remove(token);
            }
            if (this.mTransactionHandler.getActivityClient(token) == null) {
                Slog.w(TAG, TransactionExecutorHelper.tId(transaction) + "Skip pre-destroyed transaction:\n" + TransactionExecutorHelper.transactionToString(transaction, this.mTransactionHandler));
                return;
            }
        }
        if (DEBUG_RESOLVER) {
            Slog.d(TAG, TransactionExecutorHelper.transactionToString(transaction, this.mTransactionHandler));
        }
        executeCallbacks(transaction);
        executeLifecycleState(transaction);
        this.mPendingActions.clear();
        if (DEBUG_RESOLVER) {
            Slog.d(TAG, TransactionExecutorHelper.tId(transaction) + "End resolving transaction");
        }
    }

    @VisibleForTesting
    public void executeCallbacks(ClientTransaction transaction) {
        int finalState;
        List<ClientTransactionItem> callbacks = transaction.getCallbacks();
        if (callbacks != null && !callbacks.isEmpty()) {
            if (DEBUG_RESOLVER) {
                Slog.d(TAG, TransactionExecutorHelper.tId(transaction) + "Resolving callbacks in transaction");
            }
            IBinder token = transaction.getActivityToken();
            ActivityThread.ActivityClientRecord r = this.mTransactionHandler.getActivityClient(token);
            ActivityLifecycleItem finalStateRequest = transaction.getLifecycleStateRequest();
            int i = -1;
            if (finalStateRequest != null) {
                finalState = finalStateRequest.getTargetState();
            } else {
                finalState = -1;
            }
            int lastCallbackRequestingState = TransactionExecutorHelper.lastCallbackRequestingState(transaction);
            int size = callbacks.size();
            int i2 = 0;
            while (i2 < size) {
                ClientTransactionItem item = callbacks.get(i2);
                if (DEBUG_RESOLVER) {
                    Slog.d(TAG, TransactionExecutorHelper.tId(transaction) + "Resolving callback: " + item);
                }
                int postExecutionState = item.getPostExecutionState();
                int closestPreExecutionState = this.mHelper.getClosestPreExecutionState(r, item.getPostExecutionState());
                if (closestPreExecutionState != i) {
                    cycleToPath(r, closestPreExecutionState, transaction);
                }
                item.execute(this.mTransactionHandler, token, this.mPendingActions);
                item.postExecute(this.mTransactionHandler, token, this.mPendingActions);
                if (r == null) {
                    r = this.mTransactionHandler.getActivityClient(token);
                }
                i = -1;
                if (!(postExecutionState == -1 || r == null)) {
                    cycleToPath(r, postExecutionState, i2 == lastCallbackRequestingState && finalState == postExecutionState, transaction);
                }
                i2++;
            }
        }
    }

    private void executeLifecycleState(ClientTransaction transaction) {
        ActivityLifecycleItem lifecycleItem = transaction.getLifecycleStateRequest();
        if (lifecycleItem != null) {
            IBinder token = transaction.getActivityToken();
            ActivityThread.ActivityClientRecord r = this.mTransactionHandler.getActivityClient(token);
            if (DEBUG_RESOLVER) {
                Slog.d(TAG, TransactionExecutorHelper.tId(transaction) + "Resolving lifecycle state: " + lifecycleItem + " for activity: " + TransactionExecutorHelper.getShortActivityName(token, this.mTransactionHandler));
            }
            if (r != null) {
                cycleToPath(r, lifecycleItem.getTargetState(), true, transaction);
                lifecycleItem.execute(this.mTransactionHandler, token, this.mPendingActions);
                lifecycleItem.postExecute(this.mTransactionHandler, token, this.mPendingActions);
            }
        }
    }

    @VisibleForTesting
    public void cycleToPath(ActivityThread.ActivityClientRecord r, int finish, ClientTransaction transaction) {
        cycleToPath(r, finish, false, transaction);
    }

    private void cycleToPath(ActivityThread.ActivityClientRecord r, int finish, boolean excludeLastState, ClientTransaction transaction) {
        int start = r.getLifecycleState();
        if (DEBUG_RESOLVER) {
            Slog.d(TAG, TransactionExecutorHelper.tId(transaction) + "Cycle activity: " + TransactionExecutorHelper.getShortActivityName(r.token, this.mTransactionHandler) + " from: " + TransactionExecutorHelper.getStateName(start) + " to: " + TransactionExecutorHelper.getStateName(finish) + " excludeLastState: " + excludeLastState);
        }
        performLifecycleSequence(r, this.mHelper.getLifecyclePath(start, finish, excludeLastState), transaction);
    }

    private void performLifecycleSequence(ActivityThread.ActivityClientRecord r, IntArray path, ClientTransaction transaction) {
        int size = path.size();
        for (int i = 0; i < size; i++) {
            int state = path.get(i);
            if (DEBUG_RESOLVER) {
                Slog.d(TAG, TransactionExecutorHelper.tId(transaction) + "Transitioning activity: " + TransactionExecutorHelper.getShortActivityName(r.token, this.mTransactionHandler) + " to state: " + TransactionExecutorHelper.getStateName(state));
            }
            switch (state) {
                case 1:
                    this.mTransactionHandler.handleLaunchActivity(r, this.mPendingActions, null);
                    break;
                case 2:
                    this.mTransactionHandler.handleStartActivity(r, this.mPendingActions);
                    break;
                case 3:
                    this.mTransactionHandler.handleResumeActivity(r.token, false, r.isForward, "LIFECYCLER_RESUME_ACTIVITY");
                    break;
                case 4:
                    this.mTransactionHandler.handlePauseActivity(r.token, false, false, 0, this.mPendingActions, "LIFECYCLER_PAUSE_ACTIVITY");
                    break;
                case 5:
                    this.mTransactionHandler.handleStopActivity(r.token, false, 0, this.mPendingActions, false, "LIFECYCLER_STOP_ACTIVITY");
                    break;
                case 6:
                    ClientTransactionHandler clientTransactionHandler = this.mTransactionHandler;
                    IBinder iBinder = r.token;
                    clientTransactionHandler.handleDestroyActivity(iBinder, false, 0, false, "performLifecycleSequence. cycling to:" + path.get(size - 1));
                    break;
                case 7:
                    this.mTransactionHandler.performRestartActivity(r.token, false);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected lifecycle state: " + state);
            }
        }
    }
}
