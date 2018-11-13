package android.support.v4.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/* compiled from: BackStackRecord */
final class BackStackState implements Parcelable {
    public static final Creator<BackStackState> CREATOR = null;
    final int mBreadCrumbShortTitleRes;
    final CharSequence mBreadCrumbShortTitleText;
    final int mBreadCrumbTitleRes;
    final CharSequence mBreadCrumbTitleText;
    final int mIndex;
    final String mName;
    final int[] mOps;
    final ArrayList<String> mSharedElementSourceNames;
    final ArrayList<String> mSharedElementTargetNames;
    final int mTransition;
    final int mTransitionStyle;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.support.v4.app.BackStackState.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.support.v4.app.BackStackState.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.BackStackState.<clinit>():void");
    }

    public BackStackState(FragmentManagerImpl fm, BackStackRecord bse) {
        Op op;
        int numRemoved = 0;
        for (op = bse.mHead; op != null; op = op.next) {
            if (op.removed != null) {
                numRemoved += op.removed.size();
            }
        }
        this.mOps = new int[((bse.mNumOp * 7) + numRemoved)];
        if (bse.mAddToBackStack) {
            op = bse.mHead;
            int pos = 0;
            while (op != null) {
                int i = pos + 1;
                this.mOps[pos] = op.cmd;
                pos = i + 1;
                this.mOps[i] = op.fragment != null ? op.fragment.mIndex : -1;
                i = pos + 1;
                this.mOps[pos] = op.enterAnim;
                pos = i + 1;
                this.mOps[i] = op.exitAnim;
                i = pos + 1;
                this.mOps[pos] = op.popEnterAnim;
                pos = i + 1;
                this.mOps[i] = op.popExitAnim;
                if (op.removed != null) {
                    int N = op.removed.size();
                    i = pos + 1;
                    this.mOps[pos] = N;
                    int i2 = 0;
                    pos = i;
                    while (i2 < N) {
                        i = pos + 1;
                        this.mOps[pos] = ((Fragment) op.removed.get(i2)).mIndex;
                        i2++;
                        pos = i;
                    }
                    i = pos;
                } else {
                    i = pos + 1;
                    this.mOps[pos] = 0;
                }
                op = op.next;
                pos = i;
            }
            this.mTransition = bse.mTransition;
            this.mTransitionStyle = bse.mTransitionStyle;
            this.mName = bse.mName;
            this.mIndex = bse.mIndex;
            this.mBreadCrumbTitleRes = bse.mBreadCrumbTitleRes;
            this.mBreadCrumbTitleText = bse.mBreadCrumbTitleText;
            this.mBreadCrumbShortTitleRes = bse.mBreadCrumbShortTitleRes;
            this.mBreadCrumbShortTitleText = bse.mBreadCrumbShortTitleText;
            this.mSharedElementSourceNames = bse.mSharedElementSourceNames;
            this.mSharedElementTargetNames = bse.mSharedElementTargetNames;
            return;
        }
        throw new IllegalStateException("Not on back stack");
    }

    public BackStackState(Parcel in) {
        this.mOps = in.createIntArray();
        this.mTransition = in.readInt();
        this.mTransitionStyle = in.readInt();
        this.mName = in.readString();
        this.mIndex = in.readInt();
        this.mBreadCrumbTitleRes = in.readInt();
        this.mBreadCrumbTitleText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mBreadCrumbShortTitleRes = in.readInt();
        this.mBreadCrumbShortTitleText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mSharedElementSourceNames = in.createStringArrayList();
        this.mSharedElementTargetNames = in.createStringArrayList();
    }

    public BackStackRecord instantiate(FragmentManagerImpl fm) {
        BackStackRecord bse = new BackStackRecord(fm);
        int pos = 0;
        int num = 0;
        while (pos < this.mOps.length) {
            Op op = new Op();
            int pos2 = pos + 1;
            op.cmd = this.mOps[pos];
            if (FragmentManagerImpl.DEBUG) {
                Log.v("FragmentManager", "Instantiate " + bse + " op #" + num + " base fragment #" + this.mOps[pos2]);
            }
            pos = pos2 + 1;
            int findex = this.mOps[pos2];
            if (findex >= 0) {
                op.fragment = (Fragment) fm.mActive.get(findex);
            } else {
                op.fragment = null;
            }
            pos2 = pos + 1;
            op.enterAnim = this.mOps[pos];
            pos = pos2 + 1;
            op.exitAnim = this.mOps[pos2];
            pos2 = pos + 1;
            op.popEnterAnim = this.mOps[pos];
            pos = pos2 + 1;
            op.popExitAnim = this.mOps[pos2];
            pos2 = pos + 1;
            int N = this.mOps[pos];
            if (N > 0) {
                op.removed = new ArrayList(N);
                int i = 0;
                while (i < N) {
                    if (FragmentManagerImpl.DEBUG) {
                        Log.v("FragmentManager", "Instantiate " + bse + " set remove fragment #" + this.mOps[pos2]);
                    }
                    pos = pos2 + 1;
                    op.removed.add((Fragment) fm.mActive.get(this.mOps[pos2]));
                    i++;
                    pos2 = pos;
                }
            }
            pos = pos2;
            bse.addOp(op);
            num++;
        }
        bse.mTransition = this.mTransition;
        bse.mTransitionStyle = this.mTransitionStyle;
        bse.mName = this.mName;
        bse.mIndex = this.mIndex;
        bse.mAddToBackStack = true;
        bse.mBreadCrumbTitleRes = this.mBreadCrumbTitleRes;
        bse.mBreadCrumbTitleText = this.mBreadCrumbTitleText;
        bse.mBreadCrumbShortTitleRes = this.mBreadCrumbShortTitleRes;
        bse.mBreadCrumbShortTitleText = this.mBreadCrumbShortTitleText;
        bse.mSharedElementSourceNames = this.mSharedElementSourceNames;
        bse.mSharedElementTargetNames = this.mSharedElementTargetNames;
        bse.bumpBackStackNesting(1);
        return bse;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.mOps);
        dest.writeInt(this.mTransition);
        dest.writeInt(this.mTransitionStyle);
        dest.writeString(this.mName);
        dest.writeInt(this.mIndex);
        dest.writeInt(this.mBreadCrumbTitleRes);
        TextUtils.writeToParcel(this.mBreadCrumbTitleText, dest, 0);
        dest.writeInt(this.mBreadCrumbShortTitleRes);
        TextUtils.writeToParcel(this.mBreadCrumbShortTitleText, dest, 0);
        dest.writeStringList(this.mSharedElementSourceNames);
        dest.writeStringList(this.mSharedElementTargetNames);
    }
}
