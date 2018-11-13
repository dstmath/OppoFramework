package android.app.job;

import android.app.Service;
import android.app.job.IJobService.Stub;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.lang.ref.WeakReference;

public abstract class JobService extends Service {
    private static final int MSG_EXECUTE_JOB = 0;
    private static final int MSG_JOB_FINISHED = 2;
    private static final int MSG_STOP_JOB = 1;
    private static final int MSG_UPDATE_OPPO_JOB = 1001;
    public static final String PERMISSION_BIND = "android.permission.BIND_JOB_SERVICE";
    private static final String TAG = "JobService";
    IJobService mBinder;
    @GuardedBy("mHandlerLock")
    JobHandler mHandler;
    private final Object mHandlerLock = new Object();

    class JobHandler extends Handler {
        JobHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            JobParameters params = msg.obj;
            switch (msg.what) {
                case 0:
                    try {
                        ackStartMessage(params, JobService.this.onStartJob(params));
                        return;
                    } catch (Exception e) {
                        Log.e(JobService.TAG, "Error while executing job: " + params.getJobId());
                        throw new RuntimeException(e);
                    }
                case 1:
                    try {
                        ackStopMessage(params, JobService.this.onStopJob(params));
                        return;
                    } catch (Exception e2) {
                        Log.e(JobService.TAG, "Application unable to handle onStopJob.", e2);
                        throw new RuntimeException(e2);
                    }
                case 2:
                    boolean needsReschedule = msg.arg2 == 1;
                    IJobCallback callback = params.getCallback();
                    if (callback != null) {
                        try {
                            callback.jobFinished(params.getJobId(), needsReschedule);
                            return;
                        } catch (RemoteException e3) {
                            Log.e(JobService.TAG, "Error reporting job finish to system: binder has goneaway.");
                            return;
                        }
                    }
                    Log.e(JobService.TAG, "finishJob() called for a nonexistent job id.");
                    return;
                case 1001:
                    try {
                        boolean onUpdateJobParameters = JobService.this.onUpdateJobParameters(params);
                        return;
                    } catch (Exception e22) {
                        Log.e(JobService.TAG, "Application unable to handle onUpdateJobParameters.", e22);
                        throw new RuntimeException(e22);
                    }
                default:
                    Log.e(JobService.TAG, "Unrecognised message received.");
                    return;
            }
        }

        private void ackStartMessage(JobParameters params, boolean workOngoing) {
            IJobCallback callback = params.getCallback();
            int jobId = params.getJobId();
            if (callback != null) {
                try {
                    callback.acknowledgeStartMessage(jobId, workOngoing);
                } catch (RemoteException e) {
                    Log.e(JobService.TAG, "System unreachable for starting job.");
                }
            } else if (Log.isLoggable(JobService.TAG, 3)) {
                Log.d(JobService.TAG, "Attempting to ack a job that has already been processed.");
            }
        }

        private void ackStopMessage(JobParameters params, boolean reschedule) {
            IJobCallback callback = params.getCallback();
            int jobId = params.getJobId();
            if (callback != null) {
                try {
                    callback.acknowledgeStopMessage(jobId, reschedule);
                } catch (RemoteException e) {
                    Log.e(JobService.TAG, "System unreachable for stopping job.");
                }
            } else if (Log.isLoggable(JobService.TAG, 3)) {
                Log.d(JobService.TAG, "Attempting to ack a job that has already been processed.");
            }
        }
    }

    static final class JobInterface extends Stub {
        final WeakReference<JobService> mService;

        JobInterface(JobService service) {
            this.mService = new WeakReference(service);
        }

        public void startJob(JobParameters jobParams) throws RemoteException {
            JobService service = (JobService) this.mService.get();
            if (service != null) {
                service.ensureHandler();
                Message.obtain(service.mHandler, 0, jobParams).sendToTarget();
            }
        }

        public void stopJob(JobParameters jobParams) throws RemoteException {
            JobService service = (JobService) this.mService.get();
            if (service != null) {
                service.ensureHandler();
                Message.obtain(service.mHandler, 1, jobParams).sendToTarget();
            }
        }

        public void updateJobParameters(JobParameters jobParams) throws RemoteException {
            JobService service = (JobService) this.mService.get();
            if (service != null) {
                service.ensureHandler();
                Message.obtain(service.mHandler, 1001, jobParams).sendToTarget();
            }
        }
    }

    public abstract boolean onStartJob(JobParameters jobParameters);

    public abstract boolean onStopJob(JobParameters jobParameters);

    void ensureHandler() {
        synchronized (this.mHandlerLock) {
            if (this.mHandler == null) {
                this.mHandler = new JobHandler(getMainLooper());
            }
        }
    }

    public final IBinder onBind(Intent intent) {
        if (this.mBinder == null) {
            this.mBinder = new JobInterface(this);
        }
        return this.mBinder.asBinder();
    }

    public final void jobFinished(JobParameters params, boolean needsReschedule) {
        ensureHandler();
        Message m = Message.obtain(this.mHandler, 2, params);
        m.arg2 = needsReschedule ? 1 : 0;
        m.sendToTarget();
    }

    public boolean onUpdateJobParameters(JobParameters params) {
        return false;
    }
}
