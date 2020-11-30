package androidx.lifecycle;

import androidx.lifecycle.Lifecycle;
import com.alibaba.fastjson.parser.JSONToken;

/* access modifiers changed from: package-private */
public class FullLifecycleObserverAdapter implements GenericLifecycleObserver {
    private final FullLifecycleObserver mObserver;

    FullLifecycleObserverAdapter(FullLifecycleObserver observer) {
        this.mObserver = observer;
    }

    /* renamed from: androidx.lifecycle.FullLifecycleObserverAdapter$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$androidx$lifecycle$Lifecycle$Event = new int[Lifecycle.Event.values().length];

        static {
            try {
                $SwitchMap$androidx$lifecycle$Lifecycle$Event[Lifecycle.Event.ON_CREATE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$androidx$lifecycle$Lifecycle$Event[Lifecycle.Event.ON_START.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$androidx$lifecycle$Lifecycle$Event[Lifecycle.Event.ON_RESUME.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$androidx$lifecycle$Lifecycle$Event[Lifecycle.Event.ON_PAUSE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$androidx$lifecycle$Lifecycle$Event[Lifecycle.Event.ON_STOP.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$androidx$lifecycle$Lifecycle$Event[Lifecycle.Event.ON_DESTROY.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$androidx$lifecycle$Lifecycle$Event[Lifecycle.Event.ON_ANY.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    @Override // androidx.lifecycle.GenericLifecycleObserver
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        switch (AnonymousClass1.$SwitchMap$androidx$lifecycle$Lifecycle$Event[event.ordinal()]) {
            case 1:
                this.mObserver.onCreate(source);
                return;
            case 2:
                this.mObserver.onStart(source);
                return;
            case 3:
                this.mObserver.onResume(source);
                return;
            case 4:
                this.mObserver.onPause(source);
                return;
            case 5:
                this.mObserver.onStop(source);
                return;
            case JSONToken.TRUE /* 6 */:
                this.mObserver.onDestroy(source);
                return;
            case JSONToken.FALSE /* 7 */:
                throw new IllegalArgumentException("ON_ANY must not been send by anybody");
            default:
                return;
        }
    }
}
