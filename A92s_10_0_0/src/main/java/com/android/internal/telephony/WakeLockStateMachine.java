package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.PowerManager;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class WakeLockStateMachine extends StateMachine {
    protected static final boolean DBG = true;
    protected static final int EVENT_BROADCAST_COMPLETE = 2;
    public static final int EVENT_NEW_SMS_MESSAGE = 1;
    static final int EVENT_RELEASE_WAKE_LOCK = 3;
    private static final int WAKE_LOCK_TIMEOUT = 3000;
    @UnsupportedAppUsage
    protected Context mContext;
    protected DefaultState mDefaultState = new DefaultState();
    @UnsupportedAppUsage
    protected IdleState mIdleState = new IdleState();
    @UnsupportedAppUsage
    protected Phone mPhone;
    protected final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.WakeLockStateMachine.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (WakeLockStateMachine.this.mReceiverCount.decrementAndGet() == 0) {
                WakeLockStateMachine.this.sendMessage(2);
            }
        }
    };
    protected AtomicInteger mReceiverCount = new AtomicInteger(0);
    protected WaitingState mWaitingState = new WaitingState();
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mWakeLock;

    /* access modifiers changed from: protected */
    public abstract boolean handleSmsMessage(Message message);

    protected WakeLockStateMachine(String debugTag, Context context, Phone phone) {
        super(debugTag);
        this.mContext = context;
        this.mPhone = phone;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, debugTag);
        this.mWakeLock.acquire();
        addState(this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mWaitingState, this.mDefaultState);
        setInitialState(this.mIdleState);
    }

    protected WakeLockStateMachine(String debugTag, Context context, Phone phone, Object dummy) {
        super(debugTag);
        this.mContext = context;
        this.mPhone = phone;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, debugTag);
        this.mWakeLock.acquire();
    }

    public final void dispose() {
        quit();
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public final void dispatchSmsMessage(Object obj) {
        sendMessage(1, obj);
    }

    public class DefaultState extends State {
        public DefaultState() {
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            WakeLockStateMachine.this.loge("processMessage: unhandled message type " + msg.what);
            return true;
        }
    }

    public class IdleState extends State {
        public IdleState() {
        }

        public void enter() {
            WakeLockStateMachine.this.sendMessageDelayed(3, 3000);
        }

        public void exit() {
            WakeLockStateMachine.this.mWakeLock.acquire();
            WakeLockStateMachine.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                try {
                    if (WakeLockStateMachine.this.handleSmsMessage(msg)) {
                        WakeLockStateMachine.this.transitionTo(WakeLockStateMachine.this.mWaitingState);
                    }
                } catch (Exception e) {
                    WakeLockStateMachine.this.log("handleSmsMessage--exception");
                }
                return true;
            } else if (i != 3) {
                return false;
            } else {
                WakeLockStateMachine.this.mWakeLock.release();
                if (WakeLockStateMachine.this.mWakeLock.isHeld()) {
                    WakeLockStateMachine.this.log("mWakeLock is still held after release");
                } else {
                    WakeLockStateMachine.this.log("mWakeLock released");
                }
                return true;
            }
        }
    }

    public class WaitingState extends State {
        public WaitingState() {
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                WakeLockStateMachine.this.log("deferring message until return to idle");
                WakeLockStateMachine.this.deferMessage(msg);
                return true;
            } else if (i == 2) {
                WakeLockStateMachine.this.log("broadcast complete, returning to idle");
                WakeLockStateMachine wakeLockStateMachine = WakeLockStateMachine.this;
                wakeLockStateMachine.transitionTo(wakeLockStateMachine.mIdleState);
                return true;
            } else if (i != 3) {
                return false;
            } else {
                WakeLockStateMachine.this.mWakeLock.release();
                if (!WakeLockStateMachine.this.mWakeLock.isHeld()) {
                    WakeLockStateMachine.this.loge("mWakeLock released while still in WaitingState!");
                }
                return true;
            }
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void log(String s) {
        OppoRlog.Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        OppoRlog.Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        OppoRlog.Rlog.e(getName(), s, e);
    }
}
