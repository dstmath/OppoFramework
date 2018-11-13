package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import libcore.util.EmptyArray;

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
public class CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
    private static final long serialVersionUID = 8673264195747942595L;
    private volatile transient Object[] elements;

    static class CowIterator<E> implements ListIterator<E> {
        private final int from;
        private int index = 0;
        private final Object[] snapshot;
        private final int to;

        CowIterator(Object[] snapshot, int from, int to) {
            this.snapshot = snapshot;
            this.from = from;
            this.to = to;
            this.index = from;
        }

        public void add(E e) {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return this.index < this.to;
        }

        public boolean hasPrevious() {
            return this.index > this.from;
        }

        public E next() {
            if (this.index < this.to) {
                Object[] objArr = this.snapshot;
                int i = this.index;
                this.index = i + 1;
                return objArr[i];
            }
            throw new NoSuchElementException();
        }

        public int nextIndex() {
            return this.index;
        }

        public E previous() {
            if (this.index > this.from) {
                Object[] objArr = this.snapshot;
                int i = this.index - 1;
                this.index = i;
                return objArr[i];
            }
            throw new NoSuchElementException();
        }

        public int previousIndex() {
            return this.index - 1;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] elements = this.snapshot;
            for (int i = this.index; i < this.to; i++) {
                action.accept(elements[i]);
            }
            this.index = this.to;
        }
    }

    class CowSubList extends AbstractList<E> {
        private volatile Slice slice;
        final /* synthetic */ CopyOnWriteArrayList this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.<init>(java.util.concurrent.CopyOnWriteArrayList, java.lang.Object[], int, int):void, dex: 
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
        public CowSubList(java.util.concurrent.CopyOnWriteArrayList r1, java.lang.Object[] r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.<init>(java.util.concurrent.CopyOnWriteArrayList, java.lang.Object[], int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.<init>(java.util.concurrent.CopyOnWriteArrayList, java.lang.Object[], int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.add(int, java.lang.Object):void, dex: 
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
        public void add(int r1, E r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.add(int, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.add(int, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.add(java.lang.Object):boolean, dex: 
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
        public boolean add(E r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.add(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.add(java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.addAll(int, java.util.Collection):boolean, dex: 
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
        public boolean addAll(int r1, java.util.Collection<? extends E> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.addAll(int, java.util.Collection):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.addAll(int, java.util.Collection):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.addAll(java.util.Collection):boolean, dex: 
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
        public boolean addAll(java.util.Collection<? extends E> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.addAll(java.util.Collection):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.addAll(java.util.Collection):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.clear():void, dex: 
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
        public void clear() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.clear():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.clear():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.contains(java.lang.Object):boolean, dex: 
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
        public boolean contains(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.contains(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.contains(java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.containsAll(java.util.Collection):boolean, dex: 
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
        public boolean containsAll(java.util.Collection<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.containsAll(java.util.Collection):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.containsAll(java.util.Collection):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.forEach(java.util.function.Consumer):void, dex: 
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
        public void forEach(java.util.function.Consumer<? super E> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.forEach(java.util.function.Consumer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.forEach(java.util.function.Consumer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.get(int):E, dex: 
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
        public E get(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.get(int):E, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.get(int):E");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.indexOf(java.lang.Object):int, dex: 
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
        public int indexOf(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.indexOf(java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.indexOf(java.lang.Object):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.iterator():java.util.Iterator<E>, dex: 
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
        public java.util.Iterator<E> iterator() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.iterator():java.util.Iterator<E>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.iterator():java.util.Iterator<E>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.lastIndexOf(java.lang.Object):int, dex: 
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
        public int lastIndexOf(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.lastIndexOf(java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.lastIndexOf(java.lang.Object):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.listIterator():java.util.ListIterator<E>, dex: 
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
        public java.util.ListIterator<E> listIterator() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.listIterator():java.util.ListIterator<E>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.listIterator():java.util.ListIterator<E>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.listIterator(int):java.util.ListIterator<E>, dex: 
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
        public java.util.ListIterator<E> listIterator(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.listIterator(int):java.util.ListIterator<E>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.listIterator(int):java.util.ListIterator<E>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.remove(int):E, dex: 
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
        public E remove(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.remove(int):E, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.remove(int):E");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.remove(java.lang.Object):boolean, dex: 
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
        public boolean remove(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.remove(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.remove(java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.removeAll(java.util.Collection):boolean, dex: 
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
        public boolean removeAll(java.util.Collection<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.removeAll(java.util.Collection):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.removeAll(java.util.Collection):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.replaceAll(java.util.function.UnaryOperator):void, dex: 
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
        public void replaceAll(java.util.function.UnaryOperator<E> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.replaceAll(java.util.function.UnaryOperator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.replaceAll(java.util.function.UnaryOperator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.retainAll(java.util.Collection):boolean, dex: 
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
        public boolean retainAll(java.util.Collection<?> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.retainAll(java.util.Collection):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.retainAll(java.util.Collection):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.set(int, java.lang.Object):E, dex: 
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
        public E set(int r1, E r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.set(int, java.lang.Object):E, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.set(int, java.lang.Object):E");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.sort(java.util.Comparator):void, dex: 
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
        public synchronized void sort(java.util.Comparator<? super E> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.sort(java.util.Comparator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.sort(java.util.Comparator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.subList(int, int):java.util.List<E>, dex: 
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
        public java.util.List<E> subList(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.concurrent.CopyOnWriteArrayList.CowSubList.subList(int, int):java.util.List<E>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.CowSubList.subList(int, int):java.util.List<E>");
        }

        public int size() {
            Slice slice = this.slice;
            return Slice.m131-get2(slice) - Slice.m130-get1(slice);
        }

        public boolean isEmpty() {
            Slice slice = this.slice;
            return Slice.m130-get1(slice) == Slice.m131-get2(slice);
        }
    }

    static class Slice {
        private final Object[] expectedElements;
        private final int from;
        private final int to;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.-get0(java.util.concurrent.CopyOnWriteArrayList$Slice):java.lang.Object[], dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ java.lang.Object[] m129-get0(java.util.concurrent.CopyOnWriteArrayList.Slice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.-get0(java.util.concurrent.CopyOnWriteArrayList$Slice):java.lang.Object[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.Slice.-get0(java.util.concurrent.CopyOnWriteArrayList$Slice):java.lang.Object[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.-get1(java.util.concurrent.CopyOnWriteArrayList$Slice):int, dex: 
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
        /* renamed from: -get1 */
        static /* synthetic */ int m130-get1(java.util.concurrent.CopyOnWriteArrayList.Slice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.-get1(java.util.concurrent.CopyOnWriteArrayList$Slice):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.Slice.-get1(java.util.concurrent.CopyOnWriteArrayList$Slice):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.-get2(java.util.concurrent.CopyOnWriteArrayList$Slice):int, dex: 
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
        /* renamed from: -get2 */
        static /* synthetic */ int m131-get2(java.util.concurrent.CopyOnWriteArrayList.Slice r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.-get2(java.util.concurrent.CopyOnWriteArrayList$Slice):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.Slice.-get2(java.util.concurrent.CopyOnWriteArrayList$Slice):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.<init>(java.lang.Object[], int, int):void, dex: 
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
        Slice(java.lang.Object[] r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.<init>(java.lang.Object[], int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.Slice.<init>(java.lang.Object[], int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 0 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkConcurrentModification(java.lang.Object[]):void, dex:  in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkConcurrentModification(java.lang.Object[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 0 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkConcurrentModification(java.lang.Object[]):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: 0
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        void checkConcurrentModification(java.lang.Object[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 0 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkConcurrentModification(java.lang.Object[]):void, dex:  in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkConcurrentModification(java.lang.Object[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.Slice.checkConcurrentModification(java.lang.Object[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkElementIndex(int):void, dex: 
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
        void checkElementIndex(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkElementIndex(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.Slice.checkElementIndex(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkPositionIndex(int):void, dex: 
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
        void checkPositionIndex(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: java.util.concurrent.CopyOnWriteArrayList.Slice.checkPositionIndex(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CopyOnWriteArrayList.Slice.checkPositionIndex(int):void");
        }
    }

    public CopyOnWriteArrayList() {
        this.elements = EmptyArray.OBJECT;
    }

    public CopyOnWriteArrayList(Collection<? extends E> collection) {
        this(collection.toArray());
    }

    public CopyOnWriteArrayList(E[] array) {
        this.elements = Arrays.copyOf(array, array.length, Object[].class);
    }

    public Object clone() {
        try {
            CopyOnWriteArrayList result = (CopyOnWriteArrayList) super.clone();
            result.elements = (Object[]) result.elements.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public int size() {
        return this.elements.length;
    }

    public E get(int index) {
        return this.elements[index];
    }

    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public boolean containsAll(Collection<?> collection) {
        Object[] snapshot = this.elements;
        return containsAll(collection, snapshot, 0, snapshot.length);
    }

    static boolean containsAll(Collection<?> collection, Object[] snapshot, int from, int to) {
        for (Object o : collection) {
            if (indexOf(o, snapshot, from, to) == -1) {
                return false;
            }
        }
        return true;
    }

    public int indexOf(E object, int from) {
        Object[] snapshot = this.elements;
        return indexOf(object, snapshot, from, snapshot.length);
    }

    public int indexOf(Object object) {
        Object[] snapshot = this.elements;
        return indexOf(object, snapshot, 0, snapshot.length);
    }

    public int lastIndexOf(E object, int to) {
        return lastIndexOf(object, this.elements, 0, to);
    }

    public int lastIndexOf(Object object) {
        Object[] snapshot = this.elements;
        return lastIndexOf(object, snapshot, 0, snapshot.length);
    }

    public boolean isEmpty() {
        return this.elements.length == 0;
    }

    public Iterator<E> iterator() {
        Object[] snapshot = this.elements;
        return new CowIterator(snapshot, 0, snapshot.length);
    }

    public ListIterator<E> listIterator(int index) {
        Object[] snapshot = this.elements;
        if (index < 0 || index > snapshot.length) {
            throw new IndexOutOfBoundsException("index=" + index + ", length=" + snapshot.length);
        }
        CowIterator<E> result = new CowIterator(snapshot, 0, snapshot.length);
        result.index = index;
        return result;
    }

    public ListIterator<E> listIterator() {
        Object[] snapshot = this.elements;
        return new CowIterator(snapshot, 0, snapshot.length);
    }

    public List<E> subList(int from, int to) {
        Object[] snapshot = this.elements;
        if (from >= 0 && from <= to && to <= snapshot.length) {
            return new CowSubList(this, snapshot, from, to);
        }
        throw new IndexOutOfBoundsException("from=" + from + ", to=" + to + ", list size=" + snapshot.length);
    }

    public Object[] toArray() {
        return (Object[]) this.elements.clone();
    }

    public <T> T[] toArray(T[] contents) {
        Object[] snapshot = this.elements;
        if (snapshot.length > contents.length) {
            return Arrays.copyOf(snapshot, snapshot.length, contents.getClass());
        }
        System.arraycopy(snapshot, 0, contents, 0, snapshot.length);
        if (snapshot.length < contents.length) {
            contents[snapshot.length] = null;
        }
        return contents;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other instanceof CopyOnWriteArrayList) {
            if (this != other) {
                z = Arrays.equals(this.elements, ((CopyOnWriteArrayList) other).elements);
            }
            return z;
        } else if (!(other instanceof List)) {
            return false;
        } else {
            Object[] snapshot = this.elements;
            Iterator<?> i = ((List) other).iterator();
            for (Object o : snapshot) {
                if (!i.hasNext() || !libcore.util.Objects.equal(o, i.next())) {
                    return false;
                }
            }
            if (i.hasNext()) {
                z = false;
            }
            return z;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.elements);
    }

    public String toString() {
        return Arrays.toString(this.elements);
    }

    public synchronized boolean add(E e) {
        Object[] newElements = new Object[(this.elements.length + 1)];
        System.arraycopy(this.elements, 0, newElements, 0, this.elements.length);
        newElements[this.elements.length] = e;
        this.elements = newElements;
        return true;
    }

    public synchronized void add(int index, E e) {
        Object[] newElements = new Object[(this.elements.length + 1)];
        System.arraycopy(this.elements, 0, newElements, 0, index);
        newElements[index] = e;
        System.arraycopy(this.elements, index, newElements, index + 1, this.elements.length - index);
        this.elements = newElements;
    }

    public synchronized boolean addAll(Collection<? extends E> collection) {
        return addAll(this.elements.length, collection);
    }

    public synchronized boolean addAll(int index, Collection<? extends E> collection) {
        boolean z = false;
        synchronized (this) {
            Object[] toAdd = collection.toArray();
            Object[] newElements = new Object[(this.elements.length + toAdd.length)];
            System.arraycopy(this.elements, 0, newElements, 0, index);
            System.arraycopy(toAdd, 0, newElements, index, toAdd.length);
            System.arraycopy(this.elements, index, newElements, toAdd.length + index, this.elements.length - index);
            this.elements = newElements;
            if (toAdd.length > 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized int addAllAbsent(Collection<? extends E> collection) {
        int addedCount;
        int i = 0;
        synchronized (this) {
            Object[] toAdd = collection.toArray();
            Object[] newElements = new Object[(this.elements.length + toAdd.length)];
            System.arraycopy(this.elements, 0, newElements, 0, this.elements.length);
            int length = toAdd.length;
            addedCount = 0;
            while (i < length) {
                int addedCount2;
                Object o = toAdd[i];
                if (indexOf(o, newElements, 0, this.elements.length + addedCount) == -1) {
                    addedCount2 = addedCount + 1;
                    newElements[this.elements.length + addedCount] = o;
                } else {
                    addedCount2 = addedCount;
                }
                i++;
                addedCount = addedCount2;
            }
            if (addedCount < toAdd.length) {
                newElements = Arrays.copyOfRange(newElements, 0, this.elements.length + addedCount);
            }
            this.elements = newElements;
        }
        return addedCount;
    }

    public synchronized boolean addIfAbsent(E object) {
        if (contains(object)) {
            return false;
        }
        add(object);
        return true;
    }

    public synchronized void clear() {
        this.elements = EmptyArray.OBJECT;
    }

    public synchronized E remove(int index) {
        E removed;
        removed = this.elements[index];
        removeRange(index, index + 1);
        return removed;
    }

    public synchronized boolean remove(Object o) {
        int index = indexOf(o);
        if (index == -1) {
            return false;
        }
        remove(index);
        return true;
    }

    public synchronized boolean removeAll(Collection<?> collection) {
        boolean z = false;
        synchronized (this) {
            if (removeOrRetain(collection, false, 0, this.elements.length) != 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean retainAll(Collection<?> collection) {
        boolean z = true;
        synchronized (this) {
            if (removeOrRetain(collection, true, 0, this.elements.length) == 0) {
                z = false;
            }
        }
        return z;
    }

    public synchronized void replaceAll(UnaryOperator<E> operator) {
        replaceInRange(0, this.elements.length, operator);
    }

    private void replaceInRange(int from, int to, UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        Object[] newElements = new Object[this.elements.length];
        System.arraycopy(this.elements, 0, newElements, 0, newElements.length);
        for (int i = from; i < to; i++) {
            newElements[i] = operator.apply(this.elements[i]);
        }
        this.elements = newElements;
    }

    public synchronized void sort(Comparator<? super E> c) {
        sortInRange(0, this.elements.length, c);
    }

    private synchronized void sortInRange(int from, int to, Comparator<? super E> c) {
        Objects.requireNonNull(c);
        Object[] newElements = new Object[this.elements.length];
        System.arraycopy(this.elements, 0, newElements, 0, newElements.length);
        Arrays.sort(newElements, from, to, c);
        this.elements = newElements;
    }

    public void forEach(Consumer<? super E> action) {
        forInRange(0, this.elements.length, action);
    }

    private void forInRange(int from, int to, Consumer<? super E> action) {
        Objects.requireNonNull(action);
        Object[] newElements = new Object[this.elements.length];
        System.arraycopy(this.elements, 0, newElements, 0, newElements.length);
        for (int i = from; i < to; i++) {
            action.accept(newElements[i]);
        }
    }

    private int removeOrRetain(Collection<?> collection, boolean retain, int from, int to) {
        int i = from;
        while (i < to) {
            if (collection.contains(this.elements[i]) == retain) {
                i++;
            } else {
                int newSize;
                Object[] newElements = new Object[(this.elements.length - 1)];
                System.arraycopy(this.elements, 0, newElements, 0, i);
                int j = i + 1;
                int newSize2 = i;
                while (j < to) {
                    if (collection.contains(this.elements[j]) == retain) {
                        newSize = newSize2 + 1;
                        newElements[newSize2] = this.elements[j];
                    } else {
                        newSize = newSize2;
                    }
                    j++;
                    newSize2 = newSize;
                }
                System.arraycopy(this.elements, to, newElements, newSize2, this.elements.length - to);
                newSize = newSize2 + (this.elements.length - to);
                if (newSize < newElements.length) {
                    newElements = Arrays.copyOfRange(newElements, 0, newSize);
                }
                int removed = this.elements.length - newElements.length;
                this.elements = newElements;
                return removed;
            }
        }
        return 0;
    }

    public synchronized E set(int index, E e) {
        E result;
        Object[] newElements = (Object[]) this.elements.clone();
        result = newElements[index];
        newElements[index] = e;
        this.elements = newElements;
        return result;
    }

    private void removeRange(int from, int to) {
        Object[] newElements = new Object[(this.elements.length - (to - from))];
        System.arraycopy(this.elements, 0, newElements, 0, from);
        System.arraycopy(this.elements, to, newElements, from, this.elements.length - to);
        this.elements = newElements;
    }

    static int lastIndexOf(Object o, Object[] data, int from, int to) {
        int i;
        if (o == null) {
            for (i = to - 1; i >= from; i--) {
                if (data[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = to - 1; i >= from; i--) {
                if (o.equals(data[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    static int indexOf(Object o, Object[] data, int from, int to) {
        int i;
        if (o == null) {
            for (i = from; i < to; i++) {
                if (data[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = from; i < to; i++) {
                if (o.equals(data[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    final Object[] getArray() {
        return this.elements;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Object[] snapshot = this.elements;
        out.defaultWriteObject();
        out.writeInt(snapshot.length);
        for (Object o : snapshot) {
            out.writeObject(o);
        }
    }

    private synchronized void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Object[] snapshot = new Object[in.readInt()];
        for (int i = 0; i < snapshot.length; i++) {
            snapshot[i] = in.readObject();
        }
        this.elements = snapshot;
    }
}
