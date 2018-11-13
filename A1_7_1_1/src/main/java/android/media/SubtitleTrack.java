package android.media;

import android.graphics.Canvas;
import android.media.MediaTimeProvider.OnMediaTimeListener;
import android.os.Handler;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Vector;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public abstract class SubtitleTrack implements OnMediaTimeListener {
    private static final String TAG = "SubtitleTrack";
    public boolean DEBUG;
    protected final Vector<Cue> mActiveCues;
    protected CueList mCues;
    private MediaFormat mFormat;
    protected Handler mHandler;
    private long mLastTimeMs;
    private long mLastUpdateTimeMs;
    private long mNextScheduledTimeMs;
    private Runnable mRunnable;
    protected final LongSparseArray<Run> mRunsByEndTime;
    protected final LongSparseArray<Run> mRunsByID;
    protected MediaTimeProvider mTimeProvider;
    protected boolean mVisible;

    public interface RenderingWidget {

        public interface OnChangedListener {
            void onChanged(RenderingWidget renderingWidget);
        }

        void draw(Canvas canvas);

        void onAttachedToWindow();

        void onDetachedFromWindow();

        void setOnChangedListener(OnChangedListener onChangedListener);

        void setSize(int i, int i2);

        void setVisible(boolean z);
    }

    /* renamed from: android.media.SubtitleTrack$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ SubtitleTrack this$0;
        final /* synthetic */ long val$thenMs;
        final /* synthetic */ SubtitleTrack val$track;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.SubtitleTrack.1.<init>(android.media.SubtitleTrack, android.media.SubtitleTrack, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(android.media.SubtitleTrack r1, android.media.SubtitleTrack r2, long r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.SubtitleTrack.1.<init>(android.media.SubtitleTrack, android.media.SubtitleTrack, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.1.<init>(android.media.SubtitleTrack, android.media.SubtitleTrack, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.1.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.1.run():void");
        }
    }

    public static class Cue {
        public long mEndTimeMs;
        public long[] mInnerTimesMs;
        public Cue mNextInRun;
        public long mRunID;
        public long mStartTimeMs;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.SubtitleTrack.Cue.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public Cue() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.SubtitleTrack.Cue.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.Cue.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.SubtitleTrack.Cue.onTime(long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public void onTime(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.SubtitleTrack.Cue.onTime(long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.Cue.onTime(long):void");
        }
    }

    static class CueList {
        private static final String TAG = "CueList";
        public boolean DEBUG;
        private SortedMap<Long, Vector<Cue>> mCues;

        /* renamed from: android.media.SubtitleTrack$CueList$1 */
        class AnonymousClass1 implements Iterable<Pair<Long, Cue>> {
            final /* synthetic */ CueList this$1;
            final /* synthetic */ long val$lastTimeMs;
            final /* synthetic */ long val$timeMs;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.SubtitleTrack.CueList.1.<init>(android.media.SubtitleTrack$CueList, long, long):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            AnonymousClass1(android.media.SubtitleTrack.CueList r1, long r2, long r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.SubtitleTrack.CueList.1.<init>(android.media.SubtitleTrack$CueList, long, long):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.1.<init>(android.media.SubtitleTrack$CueList, long, long):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.1.iterator():java.util.Iterator<android.util.Pair<java.lang.Long, android.media.SubtitleTrack$Cue>>, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public java.util.Iterator<android.util.Pair<java.lang.Long, android.media.SubtitleTrack.Cue>> iterator() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.1.iterator():java.util.Iterator<android.util.Pair<java.lang.Long, android.media.SubtitleTrack$Cue>>, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.1.iterator():java.util.Iterator<android.util.Pair<java.lang.Long, android.media.SubtitleTrack$Cue>>");
            }
        }

        class EntryIterator implements Iterator<Pair<Long, Cue>> {
            private long mCurrentTimeMs;
            private boolean mDone;
            private Pair<Long, Cue> mLastEntry;
            private Iterator<Cue> mLastListIterator;
            private Iterator<Cue> mListIterator;
            private SortedMap<Long, Vector<Cue>> mRemainingCues;
            final /* synthetic */ CueList this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.SubtitleTrack.CueList.EntryIterator.<init>(android.media.SubtitleTrack$CueList, java.util.SortedMap):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public EntryIterator(android.media.SubtitleTrack.CueList r1, java.util.SortedMap<java.lang.Long, java.util.Vector<android.media.SubtitleTrack.Cue>> r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.SubtitleTrack.CueList.EntryIterator.<init>(android.media.SubtitleTrack$CueList, java.util.SortedMap):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.EntryIterator.<init>(android.media.SubtitleTrack$CueList, java.util.SortedMap):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.media.SubtitleTrack.CueList.EntryIterator.nextKey():void, dex:  in method: android.media.SubtitleTrack.CueList.EntryIterator.nextKey():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.media.SubtitleTrack.CueList.EntryIterator.nextKey():void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 11 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
                	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 12 more
                */
            private void nextKey() {
                /*
                // Can't load method instructions: Load method exception: null in method: android.media.SubtitleTrack.CueList.EntryIterator.nextKey():void, dex:  in method: android.media.SubtitleTrack.CueList.EntryIterator.nextKey():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.EntryIterator.nextKey():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.media.SubtitleTrack.CueList.EntryIterator.hasNext():boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public boolean hasNext() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.media.SubtitleTrack.CueList.EntryIterator.hasNext():boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.EntryIterator.hasNext():boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.media.SubtitleTrack.CueList.EntryIterator.next():android.util.Pair<java.lang.Long, android.media.SubtitleTrack$Cue>, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.util.Pair<java.lang.Long, android.media.SubtitleTrack.Cue> next() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.media.SubtitleTrack.CueList.EntryIterator.next():android.util.Pair<java.lang.Long, android.media.SubtitleTrack$Cue>, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.EntryIterator.next():android.util.Pair<java.lang.Long, android.media.SubtitleTrack$Cue>");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.SubtitleTrack.CueList.EntryIterator.next():java.lang.Object, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public /* bridge */ /* synthetic */ java.lang.Object next() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.SubtitleTrack.CueList.EntryIterator.next():java.lang.Object, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.EntryIterator.next():java.lang.Object");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.EntryIterator.remove():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void remove() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.EntryIterator.remove():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.EntryIterator.remove():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.-get0(android.media.SubtitleTrack$CueList):java.util.SortedMap, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        /* renamed from: -get0 */
        static /* synthetic */ java.util.SortedMap m362-get0(android.media.SubtitleTrack.CueList r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.-get0(android.media.SubtitleTrack$CueList):java.util.SortedMap, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.-get0(android.media.SubtitleTrack$CueList):java.util.SortedMap");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.SubtitleTrack.CueList.-wrap0(android.media.SubtitleTrack$CueList, android.media.SubtitleTrack$Cue, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        /* renamed from: -wrap0 */
        static /* synthetic */ void m363-wrap0(android.media.SubtitleTrack.CueList r1, android.media.SubtitleTrack.Cue r2, long r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.SubtitleTrack.CueList.-wrap0(android.media.SubtitleTrack$CueList, android.media.SubtitleTrack$Cue, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.-wrap0(android.media.SubtitleTrack$CueList, android.media.SubtitleTrack$Cue, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.media.SubtitleTrack.CueList.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        CueList() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.media.SubtitleTrack.CueList.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.addEvent(android.media.SubtitleTrack$Cue, long):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private boolean addEvent(android.media.SubtitleTrack.Cue r1, long r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.addEvent(android.media.SubtitleTrack$Cue, long):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.addEvent(android.media.SubtitleTrack$Cue, long):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.removeEvent(android.media.SubtitleTrack$Cue, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private void removeEvent(android.media.SubtitleTrack.Cue r1, long r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.removeEvent(android.media.SubtitleTrack$Cue, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.removeEvent(android.media.SubtitleTrack$Cue, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.media.SubtitleTrack.CueList.add(android.media.SubtitleTrack$Cue):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void add(android.media.SubtitleTrack.Cue r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.media.SubtitleTrack.CueList.add(android.media.SubtitleTrack$Cue):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.add(android.media.SubtitleTrack$Cue):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.nextTimeAfter(long):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public long nextTimeAfter(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.CueList.nextTimeAfter(long):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.nextTimeAfter(long):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.media.SubtitleTrack.CueList.remove(android.media.SubtitleTrack$Cue):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void remove(android.media.SubtitleTrack.Cue r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.media.SubtitleTrack.CueList.remove(android.media.SubtitleTrack$Cue):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.remove(android.media.SubtitleTrack$Cue):void");
        }

        /*  JADX ERROR: NullPointerException in pass: ModVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
            	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
            	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
            	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
            	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
            	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public java.lang.Iterable<android.util.Pair<java.lang.Long, android.media.SubtitleTrack.Cue>> entriesBetween(long r8, long r10) {
            /*
            r7 = this;
            r0 = new android.media.SubtitleTrack$CueList$1;
            r1 = r7;
            r2 = r8;
            r4 = r10;
            r0.<init>(r1, r2, r4);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.CueList.entriesBetween(long, long):java.lang.Iterable<android.util.Pair<java.lang.Long, android.media.SubtitleTrack$Cue>>");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class Run {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f20-assertionsDisabled = false;
        public long mEndTimeMs;
        public Cue mFirstCue;
        public Run mNextRunAtEndTimeMs;
        public Run mPrevRunAtEndTimeMs;
        public long mRunID;
        private long mStoredEndTimeMs;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.SubtitleTrack.Run.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.SubtitleTrack.Run.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.Run.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: android.media.SubtitleTrack.Run.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private Run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: android.media.SubtitleTrack.Run.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.Run.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.SubtitleTrack.Run.<init>(android.media.SubtitleTrack$Run):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        /* synthetic */ Run(android.media.SubtitleTrack.Run r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.SubtitleTrack.Run.<init>(android.media.SubtitleTrack$Run):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.Run.<init>(android.media.SubtitleTrack$Run):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.Run.removeAtEndTimeMs():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void removeAtEndTimeMs() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.SubtitleTrack.Run.removeAtEndTimeMs():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.Run.removeAtEndTimeMs():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.media.SubtitleTrack.Run.storeByEndTimeMs(android.util.LongSparseArray):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void storeByEndTimeMs(android.util.LongSparseArray<android.media.SubtitleTrack.Run> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.media.SubtitleTrack.Run.storeByEndTimeMs(android.util.LongSparseArray):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.Run.storeByEndTimeMs(android.util.LongSparseArray):void");
        }
    }

    public abstract RenderingWidget getRenderingWidget();

    public abstract void onData(byte[] bArr, boolean z, long j);

    public abstract void updateView(Vector<Cue> vector);

    public SubtitleTrack(MediaFormat format) {
        this.mRunsByEndTime = new LongSparseArray();
        this.mRunsByID = new LongSparseArray();
        this.mActiveCues = new Vector();
        this.DEBUG = false;
        this.mHandler = new Handler();
        this.mNextScheduledTimeMs = -1;
        this.mFormat = format;
        this.mCues = new CueList();
        clearActiveCues();
        this.mLastTimeMs = -1;
    }

    public final MediaFormat getFormat() {
        return this.mFormat;
    }

    protected void onData(SubtitleData data) {
        long runID = data.getStartTimeUs() + 1;
        onData(data.getData(), true, runID);
        setRunDiscardTimeMs(runID, (data.getStartTimeUs() + data.getDurationUs()) / 1000);
    }

    /* JADX WARNING: Missing block: B:4:0x0007, code:
            if (r8.mLastUpdateTimeMs > r10) goto L_0x0009;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void updateActiveCues(boolean rebuild, long timeMs) {
        if (!rebuild) {
        }
        clearActiveCues();
        Iterator<Pair<Long, Cue>> it = this.mCues.entriesBetween(this.mLastUpdateTimeMs, timeMs).iterator();
        while (it.hasNext()) {
            Pair<Long, Cue> event = (Pair) it.next();
            Cue cue = event.second;
            if (cue.mEndTimeMs == ((Long) event.first).longValue()) {
                if (this.DEBUG) {
                    Log.v(TAG, "Removing " + cue);
                }
                this.mActiveCues.remove(cue);
                if (cue.mRunID == 0) {
                    it.remove();
                }
            } else if (cue.mStartTimeMs == ((Long) event.first).longValue()) {
                if (this.DEBUG) {
                    Log.v(TAG, "Adding " + cue);
                }
                if (cue.mInnerTimesMs != null) {
                    cue.onTime(timeMs);
                }
                this.mActiveCues.add(cue);
            } else if (cue.mInnerTimesMs != null) {
                cue.onTime(timeMs);
            }
        }
        while (this.mRunsByEndTime.size() > 0 && this.mRunsByEndTime.keyAt(0) <= timeMs) {
            removeRunsByEndTimeIndex(0);
        }
        this.mLastUpdateTimeMs = timeMs;
    }

    private void removeRunsByEndTimeIndex(int ix) {
        Run run = (Run) this.mRunsByEndTime.valueAt(ix);
        while (run != null) {
            Cue cue = run.mFirstCue;
            while (cue != null) {
                this.mCues.remove(cue);
                Cue nextCue = cue.mNextInRun;
                cue.mNextInRun = null;
                cue = nextCue;
            }
            this.mRunsByID.remove(run.mRunID);
            Run nextRun = run.mNextRunAtEndTimeMs;
            run.mPrevRunAtEndTimeMs = null;
            run.mNextRunAtEndTimeMs = null;
            run = nextRun;
        }
        this.mRunsByEndTime.removeAt(ix);
    }

    protected void finalize() throws Throwable {
        for (int ix = this.mRunsByEndTime.size() - 1; ix >= 0; ix--) {
            removeRunsByEndTimeIndex(ix);
        }
        super.finalize();
    }

    private synchronized void takeTime(long timeMs) {
        this.mLastTimeMs = timeMs;
    }

    protected synchronized void clearActiveCues() {
        if (this.DEBUG) {
            Log.v(TAG, "Clearing " + this.mActiveCues.size() + " active cues");
        }
        this.mActiveCues.clear();
        this.mLastUpdateTimeMs = -1;
    }

    protected void scheduleTimedEvents() {
        if (this.mTimeProvider != null) {
            this.mNextScheduledTimeMs = this.mCues.nextTimeAfter(this.mLastTimeMs);
            if (this.DEBUG) {
                Log.d(TAG, "sched @" + this.mNextScheduledTimeMs + " after " + this.mLastTimeMs);
            }
            this.mTimeProvider.notifyAt(this.mNextScheduledTimeMs >= 0 ? this.mNextScheduledTimeMs * 1000 : -1, this);
        }
    }

    public void onTimedEvent(long timeUs) {
        if (this.DEBUG) {
            Log.d(TAG, "onTimedEvent " + timeUs);
        }
        synchronized (this) {
            long timeMs = timeUs / 1000;
            updateActiveCues(false, timeMs);
            takeTime(timeMs);
        }
        updateView(this.mActiveCues);
        scheduleTimedEvents();
    }

    public void onSeek(long timeUs) {
        if (this.DEBUG) {
            Log.d(TAG, "onSeek " + timeUs);
        }
        synchronized (this) {
            long timeMs = timeUs / 1000;
            updateActiveCues(true, timeMs);
            takeTime(timeMs);
        }
        updateView(this.mActiveCues);
        scheduleTimedEvents();
    }

    public void onStop() {
        synchronized (this) {
            if (this.DEBUG) {
                Log.d(TAG, "onStop");
            }
            clearActiveCues();
            this.mLastTimeMs = -1;
        }
        updateView(this.mActiveCues);
        this.mNextScheduledTimeMs = -1;
        this.mTimeProvider.notifyAt(-1, this);
    }

    public void show() {
        if (!this.mVisible) {
            this.mVisible = true;
            RenderingWidget renderingWidget = getRenderingWidget();
            if (renderingWidget != null) {
                renderingWidget.setVisible(true);
            }
            if (this.mTimeProvider != null) {
                this.mTimeProvider.scheduleUpdate(this);
            }
        }
    }

    public void hide() {
        if (this.mVisible) {
            if (this.mTimeProvider != null) {
                this.mTimeProvider.cancelNotifications(this);
            }
            RenderingWidget renderingWidget = getRenderingWidget();
            if (renderingWidget != null) {
                renderingWidget.setVisible(false);
            }
            this.mVisible = false;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    protected boolean addCue(android.media.SubtitleTrack.Cue r13) {
        /*
        r12 = this;
        monitor-enter(r12);
        r7 = r12.mCues;	 Catch:{ all -> 0x00e7 }
        r7.add(r13);	 Catch:{ all -> 0x00e7 }
        r8 = r13.mRunID;	 Catch:{ all -> 0x00e7 }
        r10 = 0;	 Catch:{ all -> 0x00e7 }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));	 Catch:{ all -> 0x00e7 }
        if (r7 == 0) goto L_0x0031;	 Catch:{ all -> 0x00e7 }
    L_0x000e:
        r7 = r12.mRunsByID;	 Catch:{ all -> 0x00e7 }
        r8 = r13.mRunID;	 Catch:{ all -> 0x00e7 }
        r1 = r7.get(r8);	 Catch:{ all -> 0x00e7 }
        r1 = (android.media.SubtitleTrack.Run) r1;	 Catch:{ all -> 0x00e7 }
        if (r1 != 0) goto L_0x00d9;	 Catch:{ all -> 0x00e7 }
    L_0x001a:
        r1 = new android.media.SubtitleTrack$Run;	 Catch:{ all -> 0x00e7 }
        r7 = 0;	 Catch:{ all -> 0x00e7 }
        r1.<init>(r7);	 Catch:{ all -> 0x00e7 }
        r7 = r12.mRunsByID;	 Catch:{ all -> 0x00e7 }
        r8 = r13.mRunID;	 Catch:{ all -> 0x00e7 }
        r7.put(r8, r1);	 Catch:{ all -> 0x00e7 }
        r8 = r13.mEndTimeMs;	 Catch:{ all -> 0x00e7 }
        r1.mEndTimeMs = r8;	 Catch:{ all -> 0x00e7 }
    L_0x002b:
        r7 = r1.mFirstCue;	 Catch:{ all -> 0x00e7 }
        r13.mNextInRun = r7;	 Catch:{ all -> 0x00e7 }
        r1.mFirstCue = r13;	 Catch:{ all -> 0x00e7 }
    L_0x0031:
        monitor-exit(r12);
        r2 = -1;
        r7 = r12.mTimeProvider;
        if (r7 == 0) goto L_0x0044;
    L_0x0038:
        r7 = r12.mTimeProvider;	 Catch:{ IllegalStateException -> 0x011d }
        r8 = 0;	 Catch:{ IllegalStateException -> 0x011d }
        r9 = 1;	 Catch:{ IllegalStateException -> 0x011d }
        r8 = r7.getCurrentTimeUs(r8, r9);	 Catch:{ IllegalStateException -> 0x011d }
        r10 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ IllegalStateException -> 0x011d }
        r2 = r8 / r10;	 Catch:{ IllegalStateException -> 0x011d }
    L_0x0044:
        r7 = r12.DEBUG;
        if (r7 == 0) goto L_0x0096;
    L_0x0048:
        r7 = "SubtitleTrack";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "mVisible=";
        r8 = r8.append(r9);
        r9 = r12.mVisible;
        r8 = r8.append(r9);
        r9 = ", ";
        r8 = r8.append(r9);
        r10 = r13.mStartTimeMs;
        r8 = r8.append(r10);
        r9 = " <= ";
        r8 = r8.append(r9);
        r8 = r8.append(r2);
        r9 = ", ";
        r8 = r8.append(r9);
        r10 = r13.mEndTimeMs;
        r8 = r8.append(r10);
        r9 = " >= ";
        r8 = r8.append(r9);
        r10 = r12.mLastTimeMs;
        r8 = r8.append(r10);
        r8 = r8.toString();
        android.util.Log.v(r7, r8);
    L_0x0096:
        monitor-enter(r12);
        r7 = r12.mVisible;	 Catch:{ all -> 0x00f8 }
        if (r7 == 0) goto L_0x00fb;	 Catch:{ all -> 0x00f8 }
    L_0x009b:
        r8 = r13.mStartTimeMs;	 Catch:{ all -> 0x00f8 }
        r7 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1));	 Catch:{ all -> 0x00f8 }
        if (r7 > 0) goto L_0x00fb;	 Catch:{ all -> 0x00f8 }
    L_0x00a1:
        r8 = r13.mEndTimeMs;	 Catch:{ all -> 0x00f8 }
        r10 = r12.mLastTimeMs;	 Catch:{ all -> 0x00f8 }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));	 Catch:{ all -> 0x00f8 }
        if (r7 < 0) goto L_0x00fb;	 Catch:{ all -> 0x00f8 }
    L_0x00a9:
        r7 = r12.mRunnable;	 Catch:{ all -> 0x00f8 }
        if (r7 == 0) goto L_0x00b4;	 Catch:{ all -> 0x00f8 }
    L_0x00ad:
        r7 = r12.mHandler;	 Catch:{ all -> 0x00f8 }
        r8 = r12.mRunnable;	 Catch:{ all -> 0x00f8 }
        r7.removeCallbacks(r8);	 Catch:{ all -> 0x00f8 }
    L_0x00b4:
        r6 = r12;	 Catch:{ all -> 0x00f8 }
        r4 = r2;	 Catch:{ all -> 0x00f8 }
        r7 = new android.media.SubtitleTrack$1;	 Catch:{ all -> 0x00f8 }
        r7.<init>(r12, r12, r4);	 Catch:{ all -> 0x00f8 }
        r12.mRunnable = r7;	 Catch:{ all -> 0x00f8 }
        r7 = r12.mHandler;	 Catch:{ all -> 0x00f8 }
        r8 = r12.mRunnable;	 Catch:{ all -> 0x00f8 }
        r10 = 10;	 Catch:{ all -> 0x00f8 }
        r7 = r7.postDelayed(r8, r10);	 Catch:{ all -> 0x00f8 }
        if (r7 == 0) goto L_0x00ea;	 Catch:{ all -> 0x00f8 }
    L_0x00c9:
        r7 = r12.DEBUG;	 Catch:{ all -> 0x00f8 }
        if (r7 == 0) goto L_0x00d6;	 Catch:{ all -> 0x00f8 }
    L_0x00cd:
        r7 = "SubtitleTrack";	 Catch:{ all -> 0x00f8 }
        r8 = "scheduling update";	 Catch:{ all -> 0x00f8 }
        android.util.Log.v(r7, r8);	 Catch:{ all -> 0x00f8 }
    L_0x00d6:
        r7 = 1;
        monitor-exit(r12);
        return r7;
    L_0x00d9:
        r8 = r1.mEndTimeMs;	 Catch:{ all -> 0x00e7 }
        r10 = r13.mEndTimeMs;	 Catch:{ all -> 0x00e7 }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));	 Catch:{ all -> 0x00e7 }
        if (r7 >= 0) goto L_0x002b;	 Catch:{ all -> 0x00e7 }
    L_0x00e1:
        r8 = r13.mEndTimeMs;	 Catch:{ all -> 0x00e7 }
        r1.mEndTimeMs = r8;	 Catch:{ all -> 0x00e7 }
        goto L_0x002b;
    L_0x00e7:
        r7 = move-exception;
        monitor-exit(r12);
        throw r7;
    L_0x00ea:
        r7 = r12.DEBUG;	 Catch:{ all -> 0x00f8 }
        if (r7 == 0) goto L_0x00d6;	 Catch:{ all -> 0x00f8 }
    L_0x00ee:
        r7 = "SubtitleTrack";	 Catch:{ all -> 0x00f8 }
        r8 = "failed to schedule subtitle view update";	 Catch:{ all -> 0x00f8 }
        android.util.Log.w(r7, r8);	 Catch:{ all -> 0x00f8 }
        goto L_0x00d6;
    L_0x00f8:
        r7 = move-exception;
        monitor-exit(r12);
        throw r7;
    L_0x00fb:
        r7 = r12.mVisible;	 Catch:{ all -> 0x00f8 }
        if (r7 == 0) goto L_0x011a;	 Catch:{ all -> 0x00f8 }
    L_0x00ff:
        r8 = r13.mEndTimeMs;	 Catch:{ all -> 0x00f8 }
        r10 = r12.mLastTimeMs;	 Catch:{ all -> 0x00f8 }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));	 Catch:{ all -> 0x00f8 }
        if (r7 < 0) goto L_0x011a;	 Catch:{ all -> 0x00f8 }
    L_0x0107:
        r8 = r13.mStartTimeMs;	 Catch:{ all -> 0x00f8 }
        r10 = r12.mNextScheduledTimeMs;	 Catch:{ all -> 0x00f8 }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));	 Catch:{ all -> 0x00f8 }
        if (r7 < 0) goto L_0x0117;	 Catch:{ all -> 0x00f8 }
    L_0x010f:
        r8 = r12.mNextScheduledTimeMs;	 Catch:{ all -> 0x00f8 }
        r10 = 0;	 Catch:{ all -> 0x00f8 }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));	 Catch:{ all -> 0x00f8 }
        if (r7 >= 0) goto L_0x011a;	 Catch:{ all -> 0x00f8 }
    L_0x0117:
        r12.scheduleTimedEvents();	 Catch:{ all -> 0x00f8 }
    L_0x011a:
        r7 = 0;
        monitor-exit(r12);
        return r7;
    L_0x011d:
        r0 = move-exception;
        goto L_0x0044;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.SubtitleTrack.addCue(android.media.SubtitleTrack$Cue):boolean");
    }

    /* JADX WARNING: Missing block: B:14:0x001c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setTimeProvider(MediaTimeProvider timeProvider) {
        if (this.mTimeProvider != timeProvider) {
            if (this.mTimeProvider != null) {
                this.mTimeProvider.cancelNotifications(this);
            }
            this.mTimeProvider = timeProvider;
            if (this.mTimeProvider != null) {
                this.mTimeProvider.scheduleUpdate(this);
            }
        }
    }

    protected void finishedRun(long runID) {
        if (runID != 0 && runID != -1) {
            Run run = (Run) this.mRunsByID.get(runID);
            if (run != null) {
                run.storeByEndTimeMs(this.mRunsByEndTime);
            }
        }
    }

    public void setRunDiscardTimeMs(long runID, long timeMs) {
        if (runID != 0 && runID != -1) {
            Run run = (Run) this.mRunsByID.get(runID);
            if (run != null) {
                run.mEndTimeMs = timeMs;
                run.storeByEndTimeMs(this.mRunsByEndTime);
            }
        }
    }

    public int getTrackType() {
        if (getRenderingWidget() == null) {
            return 3;
        }
        return 4;
    }
}
