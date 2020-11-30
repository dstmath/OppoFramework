package androidx.test.orchestrator.instrumentationlistener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.test.orchestrator.callback.OrchestratorCallback;
import androidx.test.orchestrator.junit.BundleJUnitUtils;
import androidx.test.orchestrator.listeners.OrchestrationListenerManager;
import java.util.Iterator;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public final class OrchestratedInstrumentationListener extends RunListener {
    private final ServiceConnection connection = new ServiceConnection() {
        /* class androidx.test.orchestrator.instrumentationlistener.OrchestratedInstrumentationListener.AnonymousClass1 */

        public void onServiceConnected(ComponentName className, IBinder service) {
            OrchestratedInstrumentationListener.this.odoCallback = OrchestratorCallback.Stub.asInterface(service);
            Log.i("OrchestrationListener", "OrchestrationListener connected to service");
            OrchestratedInstrumentationListener.this.listener.onOrchestratorConnect();
        }

        public void onServiceDisconnected(ComponentName className) {
            OrchestratedInstrumentationListener.this.odoCallback = null;
            Log.i("OrchestrationListener", "OrchestrationListener disconnected from service");
        }
    };
    private final OnConnectListener listener;
    OrchestratorCallback odoCallback;

    public interface OnConnectListener {
        void onOrchestratorConnect();
    }

    public OrchestratedInstrumentationListener(OnConnectListener listener2) {
        this.listener = listener2;
    }

    public void connect(Context context) {
        Intent intent = new Intent("androidx.test.orchestrator.OrchestratorService");
        intent.setPackage("androidx.test.orchestrator");
        if (!context.bindService(intent, this.connection, 1)) {
            throw new RuntimeException("Cannot connect to androidx.test.orchestrator.OrchestratorService");
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testRunStarted(Description description) {
        try {
            sendTestNotification(OrchestrationListenerManager.TestEvent.TEST_RUN_STARTED, BundleJUnitUtils.getBundleFromDescription(description));
        } catch (RemoteException e) {
            Log.e("OrchestrationListener", "Unable to send TestRunStarted Status to Orchestrator", e);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testRunFinished(Result result) {
        try {
            sendTestNotification(OrchestrationListenerManager.TestEvent.TEST_RUN_FINISHED, BundleJUnitUtils.getBundleFromResult(result));
        } catch (RemoteException e) {
            Log.e("OrchestrationListener", "Unable to send TestRunFinished Status to Orchestrator", e);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testStarted(Description description) {
        try {
            sendTestNotification(OrchestrationListenerManager.TestEvent.TEST_STARTED, BundleJUnitUtils.getBundleFromDescription(description));
        } catch (RemoteException e) {
            Log.e("OrchestrationListener", "Unable to send TestStarted Status to Orchestrator", e);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFinished(Description description) {
        try {
            sendTestNotification(OrchestrationListenerManager.TestEvent.TEST_FINISHED, BundleJUnitUtils.getBundleFromDescription(description));
        } catch (RemoteException e) {
            Log.e("OrchestrationListener", "Unable to send TestFinished Status to Orchestrator", e);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFailure(Failure failure) {
        try {
            sendTestNotification(OrchestrationListenerManager.TestEvent.TEST_FAILURE, BundleJUnitUtils.getBundleFromFailure(failure));
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to send TestFailure status, terminating", e);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testAssumptionFailure(Failure failure) {
        try {
            sendTestNotification(OrchestrationListenerManager.TestEvent.TEST_ASSUMPTION_FAILURE, BundleJUnitUtils.getBundleFromFailure(failure));
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to send TestAssumptionFailure status, terminating", e);
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testIgnored(Description description) {
        try {
            sendTestNotification(OrchestrationListenerManager.TestEvent.TEST_IGNORED, BundleJUnitUtils.getBundleFromDescription(description));
        } catch (RemoteException e) {
            Log.e("OrchestrationListener", "Unable to send TestIgnored Status to Orchestrator", e);
        }
    }

    public void sendTestNotification(OrchestrationListenerManager.TestEvent type, Bundle bundle) throws RemoteException {
        if (this.odoCallback != null) {
            bundle.putString("TestEvent", type.toString());
            this.odoCallback.sendTestNotification(bundle);
            return;
        }
        throw new IllegalStateException("Unable to send notification, callback is null");
    }

    public void addTests(Description description) {
        if (!description.isEmpty()) {
            if (description.isTest()) {
                String className = description.getClassName();
                String methodName = description.getMethodName();
                StringBuilder sb = new StringBuilder(1 + String.valueOf(className).length() + String.valueOf(methodName).length());
                sb.append(className);
                sb.append("#");
                sb.append(methodName);
                addTest(sb.toString());
                return;
            }
            Iterator<Description> it = description.getChildren().iterator();
            while (it.hasNext()) {
                addTests(it.next());
            }
        }
    }

    public void addTest(String test) {
        if (this.odoCallback != null) {
            try {
                this.odoCallback.addTest(test);
            } catch (RemoteException e) {
                Log.e("OrchestrationListener", "Unable to send test", e);
            }
        } else {
            throw new IllegalStateException("Unable to send test, callback is null");
        }
    }
}
