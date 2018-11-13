package android.icu.impl;

import android.icu.lang.UCharacter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
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
public class TextTrieMap<V> {
    boolean _ignoreCase;
    private Node _root;

    public interface ResultHandler<V> {
        boolean handlePrefixMatch(int i, Iterator<V> it);
    }

    public static class CharIterator implements Iterator<Character> {
        private boolean _ignoreCase;
        private int _nextIdx;
        private Character _remainingChar;
        private int _startIdx;
        private CharSequence _text;

        CharIterator(CharSequence text, int offset, boolean ignoreCase) {
            this._text = text;
            this._startIdx = offset;
            this._nextIdx = offset;
            this._ignoreCase = ignoreCase;
        }

        public boolean hasNext() {
            if (this._nextIdx == this._text.length() && this._remainingChar == null) {
                return false;
            }
            return true;
        }

        public Character next() {
            if (this._nextIdx == this._text.length() && this._remainingChar == null) {
                return null;
            }
            Character next;
            if (this._remainingChar != null) {
                next = this._remainingChar;
                this._remainingChar = null;
            } else if (this._ignoreCase) {
                int cp = UCharacter.foldCase(Character.codePointAt(this._text, this._nextIdx), true);
                this._nextIdx += Character.charCount(cp);
                char[] chars = Character.toChars(cp);
                next = Character.valueOf(chars[0]);
                if (chars.length == 2) {
                    this._remainingChar = Character.valueOf(chars[1]);
                }
            } else {
                next = Character.valueOf(this._text.charAt(this._nextIdx));
                this._nextIdx++;
            }
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove() not supproted");
        }

        public int nextIndex() {
            return this._nextIdx;
        }

        public int processedLength() {
            if (this._remainingChar == null) {
                return this._nextIdx - this._startIdx;
            }
            throw new IllegalStateException("In the middle of surrogate pair");
        }
    }

    private static class LongestMatchHandler<V> implements ResultHandler<V> {
        private int length;
        private Iterator<V> matches;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.<init>():void, dex: 
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
        private LongestMatchHandler() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TextTrieMap.LongestMatchHandler.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.<init>(android.icu.impl.TextTrieMap$LongestMatchHandler):void, dex: 
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
        /* synthetic */ LongestMatchHandler(android.icu.impl.TextTrieMap.LongestMatchHandler r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.<init>(android.icu.impl.TextTrieMap$LongestMatchHandler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TextTrieMap.LongestMatchHandler.<init>(android.icu.impl.TextTrieMap$LongestMatchHandler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.getMatchLength():int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int getMatchLength() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.getMatchLength():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TextTrieMap.LongestMatchHandler.getMatchLength():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.getMatches():java.util.Iterator<V>, dex: 
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
        public java.util.Iterator<V> getMatches() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.getMatches():java.util.Iterator<V>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TextTrieMap.LongestMatchHandler.getMatches():java.util.Iterator<V>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.handlePrefixMatch(int, java.util.Iterator):boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean handlePrefixMatch(int r1, java.util.Iterator<V> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.TextTrieMap.LongestMatchHandler.handlePrefixMatch(int, java.util.Iterator):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TextTrieMap.LongestMatchHandler.handlePrefixMatch(int, java.util.Iterator):boolean");
        }
    }

    private class Node {
        private List<Node> _children;
        private char[] _text;
        private List<V> _values;
        final /* synthetic */ TextTrieMap this$0;

        /* synthetic */ Node(TextTrieMap this$0, Node node) {
            this(this$0);
        }

        private Node(TextTrieMap this$0) {
            this.this$0 = this$0;
        }

        private Node(TextTrieMap this$0, char[] text, List<V> values, List<Node> children) {
            this.this$0 = this$0;
            this._text = text;
            this._values = values;
            this._children = children;
        }

        public Iterator<V> values() {
            if (this._values == null) {
                return null;
            }
            return this._values.iterator();
        }

        public void add(CharIterator chitr, V value) {
            StringBuilder buf = new StringBuilder();
            while (chitr.hasNext()) {
                buf.append(chitr.next());
            }
            add(TextTrieMap.toCharArray(buf), 0, value);
        }

        public Node findMatch(CharIterator chitr) {
            if (this._children == null || !chitr.hasNext()) {
                return null;
            }
            Node match = null;
            Character ch = chitr.next();
            for (Node child : this._children) {
                if (ch.charValue() < child._text[0]) {
                    break;
                } else if (ch.charValue() == child._text[0]) {
                    if (child.matchFollowing(chitr)) {
                        match = child;
                    }
                }
            }
            return match;
        }

        private void add(char[] text, int offset, V value) {
            if (text.length == offset) {
                this._values = addValue(this._values, value);
            } else if (this._children == null) {
                this._children = new LinkedList();
                this._children.add(new Node(this.this$0, TextTrieMap.subArray(text, offset), addValue(null, value), null));
            } else {
                ListIterator<Node> litr = this._children.listIterator();
                while (litr.hasNext()) {
                    Node next = (Node) litr.next();
                    if (text[offset] < next._text[0]) {
                        litr.previous();
                        break;
                    } else if (text[offset] == next._text[0]) {
                        int matchLen = next.lenMatches(text, offset);
                        if (matchLen == next._text.length) {
                            next.add(text, offset + matchLen, value);
                        } else {
                            next.split(matchLen);
                            next.add(text, offset + matchLen, value);
                        }
                        return;
                    }
                }
                litr.add(new Node(this.this$0, TextTrieMap.subArray(text, offset), addValue(null, value), null));
            }
        }

        private boolean matchFollowing(CharIterator chitr) {
            for (int idx = 1; idx < this._text.length; idx++) {
                if (!chitr.hasNext()) {
                    return false;
                }
                if (chitr.next().charValue() != this._text[idx]) {
                    return false;
                }
            }
            return true;
        }

        private int lenMatches(char[] text, int offset) {
            int textLen = text.length - offset;
            int limit = this._text.length < textLen ? this._text.length : textLen;
            int len = 0;
            while (len < limit && this._text[len] == text[offset + len]) {
                len++;
            }
            return len;
        }

        private void split(int offset) {
            char[] childText = TextTrieMap.subArray(this._text, offset);
            this._text = TextTrieMap.subArray(this._text, 0, offset);
            Node child = new Node(this.this$0, childText, this._values, this._children);
            this._values = null;
            this._children = new LinkedList();
            this._children.add(child);
        }

        private List<V> addValue(List<V> list, V value) {
            if (list == null) {
                list = new LinkedList();
            }
            list.add(value);
            return list;
        }
    }

    public TextTrieMap(boolean ignoreCase) {
        this._root = new Node(this, null);
        this._ignoreCase = ignoreCase;
    }

    public TextTrieMap<V> put(CharSequence text, V val) {
        this._root.add(new CharIterator(text, 0, this._ignoreCase), val);
        return this;
    }

    public Iterator<V> get(String text) {
        return get(text, 0);
    }

    public Iterator<V> get(CharSequence text, int start) {
        return get(text, start, null);
    }

    public Iterator<V> get(CharSequence text, int start, int[] matchLen) {
        ResultHandler handler = new LongestMatchHandler();
        find(text, start, handler);
        if (matchLen != null && matchLen.length > 0) {
            matchLen[0] = handler.getMatchLength();
        }
        return handler.getMatches();
    }

    public void find(CharSequence text, ResultHandler<V> handler) {
        find(text, 0, (ResultHandler) handler);
    }

    public void find(CharSequence text, int offset, ResultHandler<V> handler) {
        find(this._root, new CharIterator(text, offset, this._ignoreCase), (ResultHandler) handler);
    }

    /* JADX WARNING: Missing block: B:13:0x001d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void find(Node node, CharIterator chitr, ResultHandler<V> handler) {
        Iterator<V> values = node.values();
        if (values == null || handler.handlePrefixMatch(chitr.processedLength(), values)) {
            Node nextMatch = node.findMatch(chitr);
            if (nextMatch != null) {
                find(nextMatch, chitr, (ResultHandler) handler);
            }
        }
    }

    private static char[] toCharArray(CharSequence text) {
        char[] array = new char[text.length()];
        for (int i = 0; i < array.length; i++) {
            array[i] = text.charAt(i);
        }
        return array;
    }

    private static char[] subArray(char[] array, int start) {
        if (start == 0) {
            return array;
        }
        char[] sub = new char[(array.length - start)];
        System.arraycopy(array, start, sub, 0, sub.length);
        return sub;
    }

    private static char[] subArray(char[] array, int start, int limit) {
        if (start == 0 && limit == array.length) {
            return array;
        }
        char[] sub = new char[(limit - start)];
        System.arraycopy(array, start, sub, 0, limit - start);
        return sub;
    }
}
