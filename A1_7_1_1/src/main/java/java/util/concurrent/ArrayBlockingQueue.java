package java.util.concurrent;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ArrayBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {
    private static final long serialVersionUID = -817911632652898426L;
    int count;
    final Object[] items;
    transient Itrs itrs;
    final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    int putIndex;
    int takeIndex;

    private class Itr implements Iterator<E> {
        private static final int DETACHED = -3;
        private static final int NONE = -1;
        private static final int REMOVED = -2;
        private int cursor;
        private E lastItem;
        private int lastRet;
        private int nextIndex;
        private E nextItem;
        private int prevCycles;
        private int prevTakeIndex;
        final /* synthetic */ ArrayBlockingQueue this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ArrayBlockingQueue.Itr.<init>(java.util.concurrent.ArrayBlockingQueue):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        Itr(java.util.concurrent.ArrayBlockingQueue r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ArrayBlockingQueue.Itr.<init>(java.util.concurrent.ArrayBlockingQueue):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.<init>(java.util.concurrent.ArrayBlockingQueue):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ArrayBlockingQueue.Itr.detach():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void detach() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ArrayBlockingQueue.Itr.detach():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.detach():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.incCursor(int):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private int incCursor(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.incCursor(int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.incCursor(int):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.concurrent.ArrayBlockingQueue.Itr.incorporateDequeues():void, dex:  in method: java.util.concurrent.ArrayBlockingQueue.Itr.incorporateDequeues():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.concurrent.ArrayBlockingQueue.Itr.incorporateDequeues():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private void incorporateDequeues() {
            /*
            // Can't load method instructions: Load method exception: null in method: java.util.concurrent.ArrayBlockingQueue.Itr.incorporateDequeues():void, dex:  in method: java.util.concurrent.ArrayBlockingQueue.Itr.incorporateDequeues():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.incorporateDequeues():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.noNext():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void noNext() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.noNext():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.noNext():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.hasNext():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean hasNext() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.hasNext():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.hasNext():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ArrayBlockingQueue.Itr.isDetached():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        boolean isDetached() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ArrayBlockingQueue.Itr.isDetached():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.isDetached():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.next():E, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public E next() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.next():E, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.next():E");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.remove():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void remove() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itr.remove():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.remove():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ArrayBlockingQueue.Itr.removedAt(int):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        boolean removedAt(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ArrayBlockingQueue.Itr.removedAt(int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.removedAt(int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.concurrent.ArrayBlockingQueue.Itr.shutdown():void, dex:  in method: java.util.concurrent.ArrayBlockingQueue.Itr.shutdown():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.concurrent.ArrayBlockingQueue.Itr.shutdown():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        void shutdown() {
            /*
            // Can't load method instructions: Load method exception: null in method: java.util.concurrent.ArrayBlockingQueue.Itr.shutdown():void, dex:  in method: java.util.concurrent.ArrayBlockingQueue.Itr.shutdown():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.shutdown():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ArrayBlockingQueue.Itr.takeIndexWrapped():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        boolean takeIndexWrapped() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.ArrayBlockingQueue.Itr.takeIndexWrapped():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itr.takeIndexWrapped():boolean");
        }

        private boolean invalidated(int index, int prevTakeIndex, long dequeues, int length) {
            boolean z = false;
            if (index < 0) {
                return false;
            }
            int distance = index - prevTakeIndex;
            if (distance < 0) {
                distance += length;
            }
            if (dequeues > ((long) distance)) {
                z = true;
            }
            return z;
        }

        private int distance(int index, int prevTakeIndex, int length) {
            int distance = index - prevTakeIndex;
            if (distance < 0) {
                return distance + length;
            }
            return distance;
        }
    }

    class Itrs {
        private static final int LONG_SWEEP_PROBES = 16;
        private static final int SHORT_SWEEP_PROBES = 4;
        int cycles;
        private java.util.concurrent.ArrayBlockingQueue$Itrs.Node head;
        private java.util.concurrent.ArrayBlockingQueue$Itrs.Node sweeper;
        final /* synthetic */ ArrayBlockingQueue this$0;

        private class Node extends WeakReference<Itr> {
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node next;
            final /* synthetic */ Itrs this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.concurrent.ArrayBlockingQueue.Itrs.Node.<init>(java.util.concurrent.ArrayBlockingQueue$Itrs, java.util.concurrent.ArrayBlockingQueue$Itr, java.util.concurrent.ArrayBlockingQueue$Itrs$Node):void, dex:  in method: java.util.concurrent.ArrayBlockingQueue.Itrs.Node.<init>(java.util.concurrent.ArrayBlockingQueue$Itrs, java.util.concurrent.ArrayBlockingQueue$Itr, java.util.concurrent.ArrayBlockingQueue$Itrs$Node):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.concurrent.ArrayBlockingQueue.Itrs.Node.<init>(java.util.concurrent.ArrayBlockingQueue$Itrs, java.util.concurrent.ArrayBlockingQueue$Itr, java.util.concurrent.ArrayBlockingQueue$Itrs$Node):void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
                	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            Node(java.util.concurrent.ArrayBlockingQueue.Itrs r1, java.util.concurrent.ArrayBlockingQueue.Itr r2, java.util.concurrent.ArrayBlockingQueue$Itrs.Node r3) {
                /*
                // Can't load method instructions: Load method exception: null in method: java.util.concurrent.ArrayBlockingQueue.Itrs.Node.<init>(java.util.concurrent.ArrayBlockingQueue$Itrs, java.util.concurrent.ArrayBlockingQueue$Itr, java.util.concurrent.ArrayBlockingQueue$Itrs$Node):void, dex:  in method: java.util.concurrent.ArrayBlockingQueue.Itrs.Node.<init>(java.util.concurrent.ArrayBlockingQueue$Itrs, java.util.concurrent.ArrayBlockingQueue$Itr, java.util.concurrent.ArrayBlockingQueue$Itrs$Node):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itrs.Node.<init>(java.util.concurrent.ArrayBlockingQueue$Itrs, java.util.concurrent.ArrayBlockingQueue$Itr, java.util.concurrent.ArrayBlockingQueue$Itrs$Node):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.<init>(java.util.concurrent.ArrayBlockingQueue, java.util.concurrent.ArrayBlockingQueue$Itr):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        Itrs(java.util.concurrent.ArrayBlockingQueue r1, java.util.concurrent.ArrayBlockingQueue.Itr r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.<init>(java.util.concurrent.ArrayBlockingQueue, java.util.concurrent.ArrayBlockingQueue$Itr):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itrs.<init>(java.util.concurrent.ArrayBlockingQueue, java.util.concurrent.ArrayBlockingQueue$Itr):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.doSomeSweeping(boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void doSomeSweeping(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.doSomeSweeping(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itrs.doSomeSweeping(boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.elementDequeued():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void elementDequeued() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.elementDequeued():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itrs.elementDequeued():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.queueIsEmpty():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void queueIsEmpty() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.queueIsEmpty():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itrs.queueIsEmpty():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.register(java.util.concurrent.ArrayBlockingQueue$Itr):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void register(java.util.concurrent.ArrayBlockingQueue.Itr r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.register(java.util.concurrent.ArrayBlockingQueue$Itr):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itrs.register(java.util.concurrent.ArrayBlockingQueue$Itr):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.removedAt(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void removedAt(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.removedAt(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itrs.removedAt(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.takeIndexWrapped():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void takeIndexWrapped() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.ArrayBlockingQueue.Itrs.takeIndexWrapped():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ArrayBlockingQueue.Itrs.takeIndexWrapped():void");
        }
    }

    final int dec(int i) {
        if (i == 0) {
            i = this.items.length;
        }
        return i - 1;
    }

    final E itemAt(int i) {
        return this.items[i];
    }

    private void enqueue(E x) {
        Object[] items = this.items;
        items[this.putIndex] = x;
        int i = this.putIndex + 1;
        this.putIndex = i;
        if (i == items.length) {
            this.putIndex = 0;
        }
        this.count++;
        this.notEmpty.signal();
    }

    private E dequeue() {
        Object[] items = this.items;
        E x = items[this.takeIndex];
        items[this.takeIndex] = null;
        int i = this.takeIndex + 1;
        this.takeIndex = i;
        if (i == items.length) {
            this.takeIndex = 0;
        }
        this.count--;
        if (this.itrs != null) {
            this.itrs.elementDequeued();
        }
        this.notFull.signal();
        return x;
    }

    void removeAt(int removeIndex) {
        Object[] items = this.items;
        if (removeIndex == this.takeIndex) {
            items[this.takeIndex] = null;
            int i = this.takeIndex + 1;
            this.takeIndex = i;
            if (i == items.length) {
                this.takeIndex = 0;
            }
            this.count--;
            if (this.itrs != null) {
                this.itrs.elementDequeued();
            }
        } else {
            int pred;
            int i2 = removeIndex;
            int putIndex = this.putIndex;
            while (true) {
                pred = i2;
                i2++;
                if (i2 == items.length) {
                    i2 = 0;
                }
                if (i2 == putIndex) {
                    break;
                }
                items[pred] = items[i2];
            }
            items[pred] = null;
            this.putIndex = pred;
            this.count--;
            if (this.itrs != null) {
                this.itrs.removedAt(removeIndex);
            }
        }
        this.notFull.signal();
    }

    public ArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.items = new Object[capacity];
        this.lock = new ReentrantLock(fair);
        this.notEmpty = this.lock.newCondition();
        this.notFull = this.lock.newCondition();
    }

    /* JADX WARNING: Missing block: B:19:?, code:
            r8.count = r4;
     */
    /* JADX WARNING: Missing block: B:20:0x0032, code:
            if (r4 != r9) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:21:0x0034, code:
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:22:0x0035, code:
            r8.putIndex = r6;
     */
    /* JADX WARNING: Missing block: B:23:0x0037, code:
            r5.unlock();
     */
    /* JADX WARNING: Missing block: B:24:0x003a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:25:0x003b, code:
            r6 = r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c) {
        Throwable th;
        this(capacity, fair);
        ReentrantLock lock = this.lock;
        lock.lock();
        int i = 0;
        try {
            Iterator e$iterator = c.iterator();
            while (true) {
                int i2;
                try {
                    i2 = i;
                    if (e$iterator.hasNext()) {
                        E e = e$iterator.next();
                        i = i2 + 1;
                        this.items[i2] = Objects.requireNonNull(e);
                    } else {
                        try {
                            break;
                        } catch (Throwable th2) {
                            th = th2;
                            i = i2;
                            lock.unlock();
                            throw th;
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e2) {
                    i = i2;
                    try {
                        throw new IllegalArgumentException();
                    } catch (Throwable th3) {
                        th = th3;
                        lock.unlock();
                        throw th;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e3) {
        }
    }

    public boolean add(E e) {
        return super.add(e);
    }

    public boolean offer(E e) {
        Objects.requireNonNull(e);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.count == this.items.length) {
                return false;
            }
            enqueue(e);
            lock.unlock();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void put(E e) throws InterruptedException {
        Objects.requireNonNull(e);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (this.count == this.items.length) {
            try {
                this.notFull.await();
            } finally {
                lock.unlock();
            }
        }
        enqueue(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e);
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (this.count == this.items.length) {
            try {
                if (nanos <= 0) {
                    return false;
                }
                nanos = this.notFull.awaitNanos(nanos);
            } finally {
                lock.unlock();
            }
        }
        enqueue(e);
        lock.unlock();
        return true;
    }

    public E poll() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E dequeue = this.count == 0 ? null : dequeue();
            lock.unlock();
            return dequeue;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (this.count == 0) {
            try {
                this.notEmpty.await();
            } finally {
                lock.unlock();
            }
        }
        E dequeue = dequeue();
        return dequeue;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (this.count == 0) {
            try {
                if (nanos <= 0) {
                    return null;
                }
                nanos = this.notEmpty.awaitNanos(nanos);
            } finally {
                lock.unlock();
            }
        }
        E dequeue = dequeue();
        lock.unlock();
        return dequeue;
    }

    public E peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E itemAt = itemAt(this.takeIndex);
            return itemAt;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = this.count;
            return i;
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int length = this.items.length - this.count;
            return length;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.count > 0) {
                Object[] items = this.items;
                int putIndex = this.putIndex;
                int i = this.takeIndex;
                while (!o.equals(items[i])) {
                    i++;
                    if (i == items.length) {
                        i = 0;
                        continue;
                    }
                    if (i == putIndex) {
                    }
                }
                removeAt(i);
                lock.unlock();
                return true;
            }
            lock.unlock();
            return false;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.count > 0) {
                Object[] items = this.items;
                int putIndex = this.putIndex;
                int i = this.takeIndex;
                while (!o.equals(items[i])) {
                    i++;
                    if (i == items.length) {
                        i = 0;
                        continue;
                    }
                    if (i == putIndex) {
                    }
                }
                lock.unlock();
                return true;
            }
            lock.unlock();
            return false;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] items = this.items;
            int end = this.takeIndex + this.count;
            Object[] a = Arrays.copyOfRange(items, this.takeIndex, end);
            if (end != this.putIndex) {
                System.arraycopy(items, 0, a, items.length - this.takeIndex, this.putIndex);
            }
            lock.unlock();
            return a;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] items = this.items;
            int count = this.count;
            int firstLeg = Math.min(items.length - this.takeIndex, count);
            if (a.length < count) {
                a = Arrays.copyOfRange(items, this.takeIndex, this.takeIndex + count, a.getClass());
            } else {
                System.arraycopy(items, this.takeIndex, a, 0, firstLeg);
                if (a.length > count) {
                    a[count] = null;
                }
            }
            if (firstLeg < count) {
                System.arraycopy(items, 0, a, firstLeg, this.putIndex);
            }
            lock.unlock();
            return a;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public String toString() {
        return Helpers.collectionToString(this);
    }

    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = this.count;
            if (k > 0) {
                Object[] items = this.items;
                int putIndex = this.putIndex;
                int i = this.takeIndex;
                do {
                    items[i] = null;
                    i++;
                    if (i == items.length) {
                        i = 0;
                        continue;
                    }
                } while (i != putIndex);
                this.takeIndex = putIndex;
                this.count = 0;
                if (this.itrs != null) {
                    this.itrs.queueIsEmpty();
                }
                while (k > 0 && lock.hasWaiters(this.notFull)) {
                    this.notFull.signal();
                    k--;
                }
            }
            lock.unlock();
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        Objects.requireNonNull(c);
        if (c == this) {
            throw new IllegalArgumentException();
        } else if (maxElements <= 0) {
            return 0;
        } else {
            Object[] items = this.items;
            ReentrantLock lock = this.lock;
            lock.lock();
            int take;
            int i;
            try {
                int n = Math.min(maxElements, this.count);
                take = this.takeIndex;
                i = 0;
                while (i < n) {
                    c.add(items[take]);
                    items[take] = null;
                    take++;
                    if (take == items.length) {
                        take = 0;
                    }
                    i++;
                }
                if (i > 0) {
                    this.count -= i;
                    this.takeIndex = take;
                    if (this.itrs != null) {
                        if (this.count == 0) {
                            this.itrs.queueIsEmpty();
                        } else if (i > take) {
                            this.itrs.takeIndexWrapped();
                        }
                    }
                    while (i > 0 && lock.hasWaiters(this.notFull)) {
                        this.notFull.signal();
                        i--;
                    }
                }
                lock.unlock();
                return n;
            } catch (Throwable th) {
                lock.unlock();
            }
        }
    }

    public Iterator<E> iterator() {
        return new Itr(this);
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 4368);
    }
}
