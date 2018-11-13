package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ParceledListSlice<T extends Parcelable> implements Parcelable {
    public static final ClassLoaderCreator<ParceledListSlice> CREATOR = null;
    private static boolean DEBUG = false;
    private static final int MAX_IPC_SIZE = 65536;
    private static String TAG;
    private final List<T> mList;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.pm.ParceledListSlice.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.pm.ParceledListSlice.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.ParceledListSlice.<clinit>():void");
    }

    /* synthetic */ ParceledListSlice(Parcel p, ClassLoader loader, ParceledListSlice parceledListSlice) {
        this(p, loader);
    }

    public static <T extends Parcelable> ParceledListSlice<T> emptyList() {
        return new ParceledListSlice(Collections.emptyList());
    }

    public ParceledListSlice(List<T> list) {
        this.mList = list;
    }

    private ParceledListSlice(Parcel p, ClassLoader loader) {
        int N = p.readInt();
        this.mList = new ArrayList(N);
        if (DEBUG) {
            Log.d(TAG, "Retrieving " + N + " items");
        }
        if (N > 0) {
            T parcelable;
            Creator<?> creator = p.readParcelableCreator(loader);
            Class listElementClass = null;
            int i = 0;
            while (i < N && p.readInt() != 0) {
                parcelable = p.readCreator(creator, loader);
                if (listElementClass == null) {
                    listElementClass = parcelable.getClass();
                } else {
                    verifySameType(listElementClass, parcelable.getClass());
                }
                this.mList.add(parcelable);
                if (DEBUG) {
                    Log.d(TAG, "Read inline #" + i + ": " + this.mList.get(this.mList.size() - 1));
                }
                i++;
            }
            if (i < N) {
                IBinder retriever = p.readStrongBinder();
                while (i < N) {
                    if (DEBUG) {
                        Log.d(TAG, "Reading more @" + i + " of " + N + ": retriever=" + retriever);
                    }
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInt(i);
                    try {
                        retriever.transact(1, data, reply, 0);
                        while (i < N && reply.readInt() != 0) {
                            parcelable = reply.readCreator(creator, loader);
                            verifySameType(listElementClass, parcelable.getClass());
                            this.mList.add(parcelable);
                            if (DEBUG) {
                                Log.d(TAG, "Read extra #" + i + ": " + this.mList.get(this.mList.size() - 1));
                            }
                            i++;
                        }
                        reply.recycle();
                        data.recycle();
                    } catch (RemoteException e) {
                        Log.w(TAG, "Failure retrieving array; only received " + i + " of " + N, e);
                        return;
                    }
                }
            }
        }
    }

    private static void verifySameType(Class<?> expected, Class<?> actual) {
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException("Can't unparcel type " + actual.getName() + " in list of type " + expected.getName());
        }
    }

    public List<T> getList() {
        return this.mList;
    }

    public int describeContents() {
        int contents = 0;
        for (int i = 0; i < this.mList.size(); i++) {
            contents |= ((Parcelable) this.mList.get(i)).describeContents();
        }
        return contents;
    }

    public void writeToParcel(Parcel dest, final int flags) {
        final int N = this.mList.size();
        int callFlags = flags;
        dest.writeInt(N);
        if (DEBUG) {
            Log.d(TAG, "Writing " + N + " items");
        }
        if (N > 0) {
            final Class<?> listElementClass = ((Parcelable) this.mList.get(0)).getClass();
            dest.writeParcelableCreator((Parcelable) this.mList.get(0));
            int i = 0;
            while (i < N && dest.dataSize() < 65536) {
                dest.writeInt(1);
                Parcelable parcelable = (Parcelable) this.mList.get(i);
                verifySameType(listElementClass, parcelable.getClass());
                parcelable.writeToParcel(dest, flags);
                if (DEBUG) {
                    Log.d(TAG, "Wrote inline #" + i + ": " + this.mList.get(i));
                }
                i++;
            }
            if (i < N) {
                dest.writeInt(0);
                Binder retriever = new Binder() {
                    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                        if (code != 1) {
                            return super.onTransact(code, data, reply, flags);
                        }
                        int i = data.readInt();
                        if (ParceledListSlice.DEBUG) {
                            Log.d(ParceledListSlice.TAG, "Writing more @" + i + " of " + N);
                        }
                        while (i < N && reply.dataSize() < 65536) {
                            reply.writeInt(1);
                            Parcelable parcelable = (Parcelable) ParceledListSlice.this.mList.get(i);
                            ParceledListSlice.verifySameType(listElementClass, parcelable.getClass());
                            parcelable.writeToParcel(reply, flags);
                            if (ParceledListSlice.DEBUG) {
                                Log.d(ParceledListSlice.TAG, "Wrote extra #" + i + ": " + ParceledListSlice.this.mList.get(i));
                            }
                            i++;
                        }
                        if (i < N) {
                            if (ParceledListSlice.DEBUG) {
                                Log.d(ParceledListSlice.TAG, "Breaking @" + i + " of " + N);
                            }
                            reply.writeInt(0);
                        }
                        return true;
                    }
                };
                if (DEBUG) {
                    Log.d(TAG, "Breaking @" + i + " of " + N + ": retriever=" + retriever);
                }
                dest.writeStrongBinder(retriever);
            }
        }
    }
}
