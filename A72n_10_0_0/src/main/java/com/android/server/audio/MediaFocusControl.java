package com.android.server.audio;

import android.app.AppOpsManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.audio.AudioEventLogger;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.pm.CompatibilityHelper;
import com.android.server.slice.SliceClientPermissions;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MediaFocusControl implements PlayerFocusEnforcer {
    static final boolean DEBUG = ("eng".equals(Build.TYPE) || "userdebug".equals(Build.TYPE));
    static final int DUCKING_IN_APP_SDK_LEVEL = 25;
    static final boolean ENFORCE_DUCKING = true;
    static final boolean ENFORCE_DUCKING_FOR_NEW = true;
    static final boolean ENFORCE_MUTING_FOR_RING_OR_CALL = true;
    private static final int MAX_STACK_SIZE = 100;
    private static final int RING_CALL_MUTING_ENFORCEMENT_DELAY_MS = 100;
    private static final String TAG = "MediaFocusControl";
    private static final int[] USAGES_TO_MUTE_IN_RING_OR_CALL = {1, 14};
    private static final Object mAudioFocusLock = new Object();
    private static final AudioEventLogger mEventLogger = new AudioEventLogger(50, "focus commands as seen by MediaFocusControl");
    private final AppOpsManager mAppOps;
    private final Context mContext;
    @GuardedBy({"mExtFocusChangeLock"})
    private long mExtFocusChangeCounter;
    private final Object mExtFocusChangeLock = new Object();
    private PlayerFocusEnforcer mFocusEnforcer;
    private ArrayList<IAudioPolicyCallback> mFocusFollowers = new ArrayList<>();
    private HashMap<String, FocusRequester> mFocusOwnersForFocusPolicy = new HashMap<>();
    @GuardedBy({"mAudioFocusLock"})
    private IAudioPolicyCallback mFocusPolicy = null;
    private final Stack<FocusRequester> mFocusStack = new Stack<>();
    private boolean mNotifyFocusOwnerOnDuck = true;
    @GuardedBy({"mAudioFocusLock"})
    private IAudioPolicyCallback mPreviousFocusPolicy = null;
    private boolean mRingOrCallActive = false;

    protected MediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        this.mContext = cntxt;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mFocusEnforcer = pfe;
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        pw.println("\nMediaFocusControl dump time: " + DateFormat.getTimeInstance().format(new Date()));
        dumpFocusStack(pw);
        pw.println(StringUtils.LF);
        mEventLogger.dump(pw);
    }

    @Override // com.android.server.audio.PlayerFocusEnforcer
    public boolean duckPlayers(FocusRequester winner, FocusRequester loser, boolean forceDuck) {
        return this.mFocusEnforcer.duckPlayers(winner, loser, forceDuck);
    }

    @Override // com.android.server.audio.PlayerFocusEnforcer
    public void unduckPlayers(FocusRequester winner) {
        this.mFocusEnforcer.unduckPlayers(winner);
    }

    @Override // com.android.server.audio.PlayerFocusEnforcer
    public void mutePlayersForCall(int[] usagesToMute) {
        this.mFocusEnforcer.mutePlayersForCall(usagesToMute);
    }

    @Override // com.android.server.audio.PlayerFocusEnforcer
    public void unmutePlayersForCall() {
        this.mFocusEnforcer.unmutePlayersForCall();
    }

    /* access modifiers changed from: package-private */
    public void noFocusForSuspendedApp(String packageName, int uid) {
        synchronized (mAudioFocusLock) {
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            List<String> clientsToRemove = new ArrayList<>();
            while (stackIterator.hasNext()) {
                FocusRequester focusOwner = stackIterator.next();
                if (focusOwner.hasSameUid(uid) && focusOwner.hasSamePackage(packageName)) {
                    clientsToRemove.add(focusOwner.getClientId());
                    AudioEventLogger audioEventLogger = mEventLogger;
                    audioEventLogger.log(new AudioEventLogger.StringEvent("focus owner:" + focusOwner.getClientId() + " in uid:" + uid + " pack: " + packageName + " getting AUDIOFOCUS_LOSS due to app suspension").printLog(TAG));
                    focusOwner.dispatchFocusChange(-1);
                }
            }
            for (String clientToRemove : clientsToRemove) {
                removeFocusStackEntry(clientToRemove, false, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasAudioFocusUsers() {
        boolean z;
        synchronized (mAudioFocusLock) {
            z = !this.mFocusStack.empty();
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void discardAudioFocusOwner() {
        synchronized (mAudioFocusLock) {
            if (!this.mFocusStack.empty()) {
                FocusRequester exFocusOwner = this.mFocusStack.pop();
                exFocusOwner.handleFocusLoss(-1, null, false);
                exFocusOwner.release();
            }
        }
    }

    @GuardedBy({"mAudioFocusLock"})
    private void notifyTopOfAudioFocusStack() {
        if (!this.mFocusStack.empty() && canReassignAudioFocus()) {
            this.mFocusStack.peek().handleFocusGain(1);
        }
    }

    @GuardedBy({"mAudioFocusLock"})
    private void propagateFocusLossFromGain_syncAf(int focusGain, FocusRequester fr, boolean forceDuck) {
        List<String> clientsToRemove = new LinkedList<>();
        Iterator<FocusRequester> it = this.mFocusStack.iterator();
        while (it.hasNext()) {
            FocusRequester focusLoser = it.next();
            if (focusLoser.handleFocusLossFromGain(focusGain, fr, forceDuck)) {
                clientsToRemove.add(focusLoser.getClientId());
            }
        }
        for (String clientToRemove : clientsToRemove) {
            removeFocusStackEntry(clientToRemove, false, true);
        }
    }

    private void dumpFocusStack(PrintWriter pw) {
        pw.println("\nAudio Focus stack entries (last is top of stack):");
        synchronized (mAudioFocusLock) {
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            while (stackIterator.hasNext()) {
                stackIterator.next().dump(pw);
            }
            pw.println(StringUtils.LF);
            if (this.mFocusPolicy == null) {
                pw.println("No external focus policy\n");
            } else {
                pw.println("External focus policy: " + this.mFocusPolicy + ", focus owners:\n");
                dumpExtFocusPolicyFocusOwners(pw);
            }
        }
        pw.println(StringUtils.LF);
        pw.println(" Notify on duck:  " + this.mNotifyFocusOwnerOnDuck + StringUtils.LF);
        pw.println(" In ring or call: " + this.mRingOrCallActive + StringUtils.LF);
    }

    @GuardedBy({"mAudioFocusLock"})
    private void removeFocusStackEntry(String clientToRemove, boolean signal, boolean notifyFocusFollowers) {
        if (this.mFocusStack.empty() || !this.mFocusStack.peek().hasSameClient(clientToRemove)) {
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            while (stackIterator.hasNext()) {
                FocusRequester fr = stackIterator.next();
                if (fr.hasSameClient(clientToRemove)) {
                    Log.i(TAG, "AudioFocus  removeFocusStackEntry(): removing entry for " + clientToRemove);
                    stackIterator.remove();
                    fr.release();
                }
            }
            return;
        }
        FocusRequester fr2 = this.mFocusStack.pop();
        fr2.release();
        if (notifyFocusFollowers) {
            AudioFocusInfo afi = fr2.toAudioFocusInfo();
            afi.clearLossReceived();
            notifyExtPolicyFocusLoss_syncAf(afi, false);
        }
        if (signal) {
            notifyTopOfAudioFocusStack();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mAudioFocusLock"})
    private void removeFocusStackEntryOnDeath(IBinder cb) {
        boolean isTopOfStackForClientToRemove = !this.mFocusStack.isEmpty() && this.mFocusStack.peek().hasSameBinder(cb);
        Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
        while (stackIterator.hasNext()) {
            FocusRequester fr = stackIterator.next();
            if (fr.hasSameBinder(cb)) {
                Log.i(TAG, "AudioFocus  removeFocusStackEntryOnDeath(): removing entry for " + cb);
                stackIterator.remove();
                fr.release();
            }
        }
        if (isTopOfStackForClientToRemove) {
            notifyTopOfAudioFocusStack();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mAudioFocusLock"})
    private void removeFocusEntryForExtPolicy(IBinder cb) {
        if (!this.mFocusOwnersForFocusPolicy.isEmpty()) {
            Iterator<Map.Entry<String, FocusRequester>> ownerIterator = this.mFocusOwnersForFocusPolicy.entrySet().iterator();
            while (ownerIterator.hasNext()) {
                FocusRequester fr = ownerIterator.next().getValue();
                if (fr.hasSameBinder(cb)) {
                    ownerIterator.remove();
                    fr.release();
                    notifyExtFocusPolicyFocusAbandon_syncAf(fr.toAudioFocusInfo());
                    return;
                }
            }
        }
    }

    private boolean canReassignAudioFocus() {
        if (this.mFocusStack.isEmpty() || !isLockedFocusOwner(this.mFocusStack.peek())) {
            return true;
        }
        return false;
    }

    private boolean isLockedFocusOwner(FocusRequester fr) {
        return fr.hasSameClient("AudioFocus_For_Phone_Ring_And_Calls") || fr.isLockedFocusOwner();
    }

    @GuardedBy({"mAudioFocusLock"})
    private int pushBelowLockedFocusOwners(FocusRequester nfr) {
        int lastLockedFocusOwnerIndex = this.mFocusStack.size();
        for (int index = this.mFocusStack.size() - 1; index >= 0; index--) {
            if (isLockedFocusOwner(this.mFocusStack.elementAt(index))) {
                lastLockedFocusOwnerIndex = index;
            }
        }
        if (lastLockedFocusOwnerIndex == this.mFocusStack.size()) {
            Log.e(TAG, "No exclusive focus owner found in propagateFocusLossFromGain_syncAf()", new Exception());
            propagateFocusLossFromGain_syncAf(nfr.getGainRequest(), nfr, false);
            this.mFocusStack.push(nfr);
            return 1;
        }
        this.mFocusStack.insertElementAt(nfr, lastLockedFocusOwnerIndex);
        return 2;
    }

    /* access modifiers changed from: protected */
    public class AudioFocusDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb;

        AudioFocusDeathHandler(IBinder cb) {
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (MediaFocusControl.mAudioFocusLock) {
                if (MediaFocusControl.this.mFocusPolicy != null) {
                    MediaFocusControl.this.removeFocusEntryForExtPolicy(this.mCb);
                } else {
                    MediaFocusControl.this.removeFocusStackEntryOnDeath(this.mCb);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setDuckingInExtPolicyAvailable(boolean available) {
        this.mNotifyFocusOwnerOnDuck = !available;
    }

    /* access modifiers changed from: package-private */
    public boolean mustNotifyFocusOwnerOnDuck() {
        return this.mNotifyFocusOwnerOnDuck;
    }

    /* access modifiers changed from: package-private */
    public void addFocusFollower(IAudioPolicyCallback ff) {
        if (ff != null) {
            synchronized (mAudioFocusLock) {
                boolean found = false;
                Iterator<IAudioPolicyCallback> it = this.mFocusFollowers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (it.next().asBinder().equals(ff.asBinder())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    this.mFocusFollowers.add(ff);
                    notifyExtPolicyCurrentFocusAsync(ff);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeFocusFollower(IAudioPolicyCallback ff) {
        if (ff != null) {
            synchronized (mAudioFocusLock) {
                Iterator<IAudioPolicyCallback> it = this.mFocusFollowers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    IAudioPolicyCallback pcb = it.next();
                    if (pcb.asBinder().equals(ff.asBinder())) {
                        this.mFocusFollowers.remove(pcb);
                        break;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setFocusPolicy(IAudioPolicyCallback policy, boolean isTestFocusPolicy) {
        if (policy != null) {
            synchronized (mAudioFocusLock) {
                if (isTestFocusPolicy) {
                    this.mPreviousFocusPolicy = this.mFocusPolicy;
                }
                this.mFocusPolicy = policy;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unsetFocusPolicy(IAudioPolicyCallback policy, boolean isTestFocusPolicy) {
        if (policy != null) {
            synchronized (mAudioFocusLock) {
                if (this.mFocusPolicy == policy) {
                    if (isTestFocusPolicy) {
                        this.mFocusPolicy = this.mPreviousFocusPolicy;
                    } else {
                        this.mFocusPolicy = null;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyExtPolicyCurrentFocusAsync(final IAudioPolicyCallback pcb) {
        new Thread() {
            /* class com.android.server.audio.MediaFocusControl.AnonymousClass1 */

            public void run() {
                synchronized (MediaFocusControl.mAudioFocusLock) {
                    if (!MediaFocusControl.this.mFocusStack.isEmpty()) {
                        try {
                            pcb.notifyAudioFocusGrant(((FocusRequester) MediaFocusControl.this.mFocusStack.peek()).toAudioFocusInfo(), 1);
                        } catch (RemoteException e) {
                            Log.e(MediaFocusControl.TAG, "Can't call notifyAudioFocusGrant() on IAudioPolicyCallback " + pcb.asBinder(), e);
                        }
                    }
                }
            }
        }.start();
    }

    /* access modifiers changed from: package-private */
    public void notifyExtPolicyFocusGrant_syncAf(AudioFocusInfo afi, int requestResult) {
        Iterator<IAudioPolicyCallback> it = this.mFocusFollowers.iterator();
        while (it.hasNext()) {
            IAudioPolicyCallback pcb = it.next();
            try {
                pcb.notifyAudioFocusGrant(afi, requestResult);
            } catch (RemoteException e) {
                Log.e(TAG, "Can't call notifyAudioFocusGrant() on IAudioPolicyCallback " + pcb.asBinder(), e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyExtPolicyFocusLoss_syncAf(AudioFocusInfo afi, boolean wasDispatched) {
        Iterator<IAudioPolicyCallback> it = this.mFocusFollowers.iterator();
        while (it.hasNext()) {
            IAudioPolicyCallback pcb = it.next();
            try {
                pcb.notifyAudioFocusLoss(afi, wasDispatched);
            } catch (RemoteException e) {
                Log.e(TAG, "Can't call notifyAudioFocusLoss() on IAudioPolicyCallback " + pcb.asBinder(), e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean notifyExtFocusPolicyFocusRequest_syncAf(AudioFocusInfo afi, IAudioFocusDispatcher fd, IBinder cb) {
        boolean keepTrack;
        if (DEBUG) {
            Log.v(TAG, "notifyExtFocusPolicyFocusRequest client=" + afi.getClientId() + " dispatcher=" + fd);
        }
        synchronized (this.mExtFocusChangeLock) {
            long j = this.mExtFocusChangeCounter;
            this.mExtFocusChangeCounter = 1 + j;
            afi.setGen(j);
        }
        FocusRequester existingFr = this.mFocusOwnersForFocusPolicy.get(afi.getClientId());
        if (existingFr == null) {
            keepTrack = true;
        } else if (!existingFr.hasSameDispatcher(fd)) {
            existingFr.release();
            keepTrack = true;
        } else {
            keepTrack = false;
        }
        if (keepTrack) {
            AudioFocusDeathHandler hdlr = new AudioFocusDeathHandler(cb);
            try {
                cb.linkToDeath(hdlr, 0);
                this.mFocusOwnersForFocusPolicy.put(afi.getClientId(), new FocusRequester(afi, fd, cb, hdlr, this));
            } catch (RemoteException e) {
                return false;
            }
        }
        try {
            this.mFocusPolicy.notifyAudioFocusRequest(afi, 1);
            return true;
        } catch (RemoteException e2) {
            Log.e(TAG, "Can't call notifyAudioFocusRequest() on IAudioPolicyCallback " + this.mFocusPolicy.asBinder(), e2);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        r0.dispatchFocusResultFromExtPolicy(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        r0 = r5.mFocusOwnersForFocusPolicy.get(r6.getClientId());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        if (r0 == null) goto L_?;
     */
    public void setFocusRequestResultFromExtPolicy(AudioFocusInfo afi, int requestResult) {
        synchronized (this.mExtFocusChangeLock) {
            if (afi.getGen() > this.mExtFocusChangeCounter) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean notifyExtFocusPolicyFocusAbandon_syncAf(AudioFocusInfo afi) {
        if (this.mFocusPolicy == null) {
            return false;
        }
        FocusRequester fr = this.mFocusOwnersForFocusPolicy.remove(afi.getClientId());
        if (fr != null) {
            fr.release();
        }
        try {
            this.mFocusPolicy.notifyAudioFocusAbandon(afi);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call notifyAudioFocusAbandon() on IAudioPolicyCallback " + this.mFocusPolicy.asBinder(), e);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public int dispatchFocusChange(AudioFocusInfo afi, int focusChange) {
        FocusRequester fr;
        if (DEBUG) {
            Log.v(TAG, "dispatchFocusChange " + focusChange + " to afi client=" + afi.getClientId());
        }
        synchronized (mAudioFocusLock) {
            if (this.mFocusPolicy == null) {
                if (DEBUG) {
                    Log.v(TAG, "> failed: no focus policy");
                }
                return 0;
            }
            if (focusChange == -1) {
                fr = this.mFocusOwnersForFocusPolicy.remove(afi.getClientId());
            } else {
                fr = this.mFocusOwnersForFocusPolicy.get(afi.getClientId());
            }
            if (fr == null) {
                if (DEBUG) {
                    Log.v(TAG, "> failed: no such focus requester known");
                }
                return 0;
            }
            return fr.dispatchFocusChange(focusChange);
        }
    }

    private void dumpExtFocusPolicyFocusOwners(PrintWriter pw) {
        for (Map.Entry<String, FocusRequester> owner : this.mFocusOwnersForFocusPolicy.entrySet()) {
            owner.getValue().dump(pw);
        }
    }

    /* access modifiers changed from: protected */
    public int getCurrentAudioFocus() {
        synchronized (mAudioFocusLock) {
            if (this.mFocusStack.empty()) {
                return 0;
            }
            return this.mFocusStack.peek().getGainRequest();
        }
    }

    protected static int getFocusRampTimeMs(int focusGain, AudioAttributes attr) {
        switch (attr.getUsage()) {
            case 1:
            case 14:
                return 1000;
            case 2:
            case 3:
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
            case 13:
                return 500;
            case 4:
            case 6:
            case 11:
            case 12:
            case 16:
                return CompatibilityHelper.FORCE_DELAY_TO_USE_POST;
            case 15:
            default:
                return 0;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r22v0, types: [com.android.server.audio.MediaFocusControl] */
    /* JADX WARN: Type inference failed for: r25v0, types: [java.lang.Object, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v10 */
    /* JADX WARN: Type inference failed for: r1v6 */
    /* JADX WARN: Type inference failed for: r11v1, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r6v1, types: [int, boolean] */
    /* JADX WARN: Type inference failed for: r6v3 */
    /* JADX WARN: Type inference failed for: r6v4 */
    /* JADX WARN: Type inference failed for: r11v4 */
    /* JADX WARN: Type inference failed for: r11v6 */
    /* JADX WARN: Type inference failed for: r1v23 */
    /* JADX WARN: Type inference failed for: r1v24 */
    /* JADX WARN: Type inference failed for: r0v35 */
    /* JADX WARN: Type inference failed for: r0v36 */
    /* JADX WARN: Type inference failed for: r6v5 */
    /* JADX WARN: Type inference failed for: r1v26 */
    /* JADX WARN: Type inference failed for: r1v27 */
    /* JADX WARN: Type inference failed for: r0v38 */
    /* JADX WARN: Type inference failed for: r0v39 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 8 */
    public int requestAudioFocus(AudioAttributes aa, int focusChangeHint, IBinder iBinder, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, int sdk, boolean forceDuck) {
        int i;
        ?? r11;
        AudioFocusInfo afiForExtPolicy;
        boolean focusGrantDelayed;
        ?? r6;
        mEventLogger.log(new AudioEventLogger.StringEvent("requestAudioFocus() from uid/pid " + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid() + " clientId=" + clientId + " callingPack=" + callingPackageName + " req=" + focusChangeHint + " flags=0x" + Integer.toHexString(flags) + " sdk=" + sdk).printLog(TAG));
        if (!iBinder.pingBinder()) {
            Log.e(TAG, " AudioFocus DOA client for requestAudioFocus(), aborting.");
            return 0;
        } else if (this.mAppOps.noteOp(32, Binder.getCallingUid(), callingPackageName) != 0) {
            return 0;
        } else {
            synchronized (mAudioFocusLock) {
                try {
                    if (this.mFocusStack.size() > 100) {
                        Log.e(TAG, "Max AudioFocus stack size reached, failing requestAudioFocus()");
                        return 0;
                    }
                    ?? r0 = !this.mRingOrCallActive;
                    ?? r1 = "AudioFocus_For_Phone_Ring_And_Calls".compareTo(clientId) == 0;
                    boolean z = r0 == true ? 1 : 0;
                    boolean z2 = r1 == true ? 1 : 0;
                    ?? r12 = r1 == true ? 1 : 0;
                    ?? r13 = r1 == true ? 1 : 0;
                    ?? r02 = r0 == true ? 1 : 0;
                    ?? r03 = r0 == true ? 1 : 0;
                    boolean enteringRingOrCall = z & z2;
                    if (enteringRingOrCall) {
                        this.mRingOrCallActive = true;
                    }
                    if (this.mFocusPolicy != null) {
                        i = 100;
                        r11 = 0;
                        afiForExtPolicy = new AudioFocusInfo(aa, Binder.getCallingUid(), clientId, callingPackageName, focusChangeHint, 0, flags, sdk);
                    } else {
                        i = 100;
                        r11 = 0;
                        afiForExtPolicy = null;
                    }
                    if (canReassignAudioFocus()) {
                        focusGrantDelayed = false;
                    } else if ((flags & 1) == 0) {
                        return r11;
                    } else {
                        focusGrantDelayed = true;
                    }
                    if (this.mFocusPolicy == null) {
                        AudioFocusDeathHandler afdh = new AudioFocusDeathHandler(iBinder);
                        try {
                            iBinder.linkToDeath(afdh, r11);
                        } catch (RemoteException e) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("AudioFocus  requestAudioFocus() could not link to ");
                            sb.append((Object) iBinder);
                            sb.append(" binder death");
                            Log.w(TAG, sb.toString());
                            int i2 = r11 == true ? 1 : 0;
                            int i3 = r11 == true ? 1 : 0;
                            int i4 = r11 == true ? 1 : 0;
                            return i2;
                        } catch (Throwable th) {
                            e = th;
                            throw e;
                        }
                        try {
                            if (this.mFocusStack.empty() || !this.mFocusStack.peek().hasSameClient(clientId)) {
                                r6 = 1;
                            } else {
                                FocusRequester fr = this.mFocusStack.peek();
                                if (fr.getGainRequest() == focusChangeHint && fr.getGrantFlags() == flags) {
                                    iBinder.unlinkToDeath(afdh, r11);
                                    notifyExtPolicyFocusGrant_syncAf(fr.toAudioFocusInfo(), 1);
                                    return 1;
                                }
                                r6 = 1;
                                r6 = 1;
                                if (!focusGrantDelayed) {
                                    this.mFocusStack.pop();
                                    fr.release();
                                }
                            }
                            removeFocusStackEntry(clientId, r11, r11);
                            FocusRequester nfr = new FocusRequester(aa, focusChangeHint, flags, fd, iBinder, clientId, afdh, callingPackageName, Binder.getCallingUid(), this, sdk);
                            if (focusGrantDelayed) {
                                try {
                                    int requestResult = pushBelowLockedFocusOwners(nfr);
                                    if (requestResult != 0) {
                                        notifyExtPolicyFocusGrant_syncAf(nfr.toAudioFocusInfo(), requestResult);
                                    }
                                    return requestResult;
                                } catch (Throwable th2) {
                                    e = th2;
                                    throw e;
                                }
                            } else {
                                if (!this.mFocusStack.empty()) {
                                    propagateFocusLossFromGain_syncAf(focusChangeHint, nfr, forceDuck);
                                }
                                this.mFocusStack.push(nfr);
                                int i5 = r6 == true ? 1 : 0;
                                int i6 = r6 == true ? 1 : 0;
                                int i7 = r6 == true ? 1 : 0;
                                int i8 = r6 == true ? 1 : 0;
                                nfr.handleFocusGainFromRequest(i5);
                                notifyExtPolicyFocusGrant_syncAf(nfr.toAudioFocusInfo(), r6);
                                if ((enteringRingOrCall ? 1 : 0) && true) {
                                    runAudioCheckerForRingOrCallAsync(r6);
                                }
                                return r6;
                            }
                        } catch (Throwable th3) {
                            e = th3;
                            throw e;
                        }
                    } else if (notifyExtFocusPolicyFocusRequest_syncAf(afiForExtPolicy, fd, iBinder)) {
                        return i;
                    } else {
                        return r11;
                    }
                } catch (Throwable th4) {
                    e = th4;
                    throw e;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int abandonAudioFocus(IAudioFocusDispatcher fl, String clientId, AudioAttributes aa, String callingPackageName) {
        AudioEventLogger audioEventLogger = mEventLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("abandonAudioFocus() from uid/pid " + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid() + " clientId=" + clientId).printLog(TAG));
        try {
            synchronized (mAudioFocusLock) {
                if (this.mFocusPolicy != null && notifyExtFocusPolicyFocusAbandon_syncAf(new AudioFocusInfo(aa, Binder.getCallingUid(), clientId, callingPackageName, 0, 0, 0, 0))) {
                    return 1;
                }
                boolean exitingRingOrCall = this.mRingOrCallActive & ("AudioFocus_For_Phone_Ring_And_Calls".compareTo(clientId) == 0);
                if (exitingRingOrCall) {
                    this.mRingOrCallActive = false;
                }
                removeFocusStackEntry(clientId, true, true);
                if (exitingRingOrCall && true) {
                    runAudioCheckerForRingOrCallAsync(false);
                }
            }
        } catch (ConcurrentModificationException cme) {
            Log.e(TAG, "FATAL EXCEPTION AudioFocus  abandonAudioFocus() caused " + cme);
            cme.printStackTrace();
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void unregisterAudioFocusClient(String clientId) {
        synchronized (mAudioFocusLock) {
            removeFocusStackEntry(clientId, false, true);
        }
    }

    private void runAudioCheckerForRingOrCallAsync(final boolean enteringRingOrCall) {
        new Thread() {
            /* class com.android.server.audio.MediaFocusControl.AnonymousClass2 */

            public void run() {
                if (enteringRingOrCall) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (MediaFocusControl.mAudioFocusLock) {
                    if (MediaFocusControl.this.mRingOrCallActive) {
                        MediaFocusControl.this.mFocusEnforcer.mutePlayersForCall(MediaFocusControl.USAGES_TO_MUTE_IN_RING_OR_CALL);
                    } else {
                        MediaFocusControl.this.mFocusEnforcer.unmutePlayersForCall();
                    }
                }
            }
        }.start();
    }
}
