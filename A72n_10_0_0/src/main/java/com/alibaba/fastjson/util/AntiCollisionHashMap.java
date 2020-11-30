package com.alibaba.fastjson.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public class AntiCollisionHashMap<K, V> extends AbstractMap<K, V> implements Serializable, Cloneable, Map<K, V> {
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int KEY = 16777619;
    static final int MAXIMUM_CAPACITY = 1073741824;
    static final int M_MASK = -2023358765;
    static final int SEED = -2128831035;
    private static final long serialVersionUID = 362498820763181265L;
    private transient Set<Map.Entry<K, V>> entrySet;
    volatile transient Set<K> keySet;
    final float loadFactor;
    volatile transient int modCount;
    final int random;
    transient int size;
    transient Entry<K, V>[] table;
    int threshold;
    volatile transient Collection<V> values;

    private int hashString(String key) {
        int hash = SEED * this.random;
        for (int i = 0; i < key.length(); i++) {
            hash = (KEY * hash) ^ key.charAt(i);
        }
        return ((hash >> 1) ^ hash) & M_MASK;
    }

    public AntiCollisionHashMap(int initialCapacity, float loadFactor2) {
        this.keySet = null;
        this.values = null;
        this.random = new Random().nextInt(99999);
        this.entrySet = null;
        if (initialCapacity >= 0) {
            initialCapacity = initialCapacity > MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : initialCapacity;
            if (loadFactor2 <= 0.0f || Float.isNaN(loadFactor2)) {
                throw new IllegalArgumentException("Illegal load factor: " + loadFactor2);
            }
            int capacity = 1;
            while (capacity < initialCapacity) {
                capacity <<= 1;
            }
            this.loadFactor = loadFactor2;
            this.threshold = (int) (((float) capacity) * loadFactor2);
            this.table = new Entry[capacity];
            init();
            return;
        }
        throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    }

    public AntiCollisionHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public AntiCollisionHashMap() {
        this.keySet = null;
        this.values = null;
        this.random = new Random().nextInt(99999);
        this.entrySet = null;
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.threshold = 12;
        this.table = new Entry[16];
        init();
    }

    public AntiCollisionHashMap(Map<? extends K, ? extends V> m) {
        this(Math.max(((int) (((float) m.size()) / DEFAULT_LOAD_FACTOR)) + 1, 16), DEFAULT_LOAD_FACTOR);
        putAllForCreate(m);
    }

    /* access modifiers changed from: package-private */
    public void init() {
    }

    static int hash(int h) {
        int h2 = h * h;
        int h3 = h2 ^ ((h2 >>> 20) ^ (h2 >>> 12));
        return ((h3 >>> 7) ^ h3) ^ (h3 >>> 4);
    }

    static int indexFor(int h, int length) {
        return (length - 1) & h;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public V get(Object key) {
        int hash;
        Object k;
        if (key == null) {
            return getForNullKey();
        }
        if (key instanceof String) {
            hash = hash(hashString((String) key));
        } else {
            hash = hash(key.hashCode());
        }
        for (Entry<K, V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                return e.value;
            }
        }
        return null;
    }

    private V getForNullKey() {
        for (Entry<K, V> e = this.table[0]; e != null; e = e.next) {
            if (e.key == null) {
                return e.value;
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /* access modifiers changed from: package-private */
    public final Entry<K, V> getEntry(Object key) {
        int hash;
        Object k;
        if (key == null) {
            hash = 0;
        } else if (key instanceof String) {
            hash = hash(hashString((String) key));
        } else {
            hash = hash(key.hashCode());
        }
        for (Entry<K, V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                return e;
            }
        }
        return null;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public V put(K key, V value) {
        int hash;
        Object k;
        if (key == null) {
            return putForNullKey(value);
        }
        if (key instanceof String) {
            hash = hash(hashString(key));
        } else {
            hash = hash(key.hashCode());
        }
        int i = indexFor(hash, this.table.length);
        for (Entry<K, V> e = this.table[i]; e != null; e = e.next) {
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        this.modCount++;
        addEntry(hash, key, value, i);
        return null;
    }

    private V putForNullKey(V value) {
        for (Entry<K, V> e = this.table[0]; e != null; e = e.next) {
            if (e.key == null) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        this.modCount++;
        addEntry(0, null, value, 0);
        return null;
    }

    private void putForCreate(K key, V value) {
        int hash;
        Object k;
        if (key == null) {
            hash = 0;
        } else if (key instanceof String) {
            hash = hash(hashString(key));
        } else {
            hash = hash(key.hashCode());
        }
        int i = indexFor(hash, this.table.length);
        for (Entry<K, V> e = this.table[i]; e != null; e = e.next) {
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }
        createEntry(hash, key, value, i);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: com.alibaba.fastjson.util.AntiCollisionHashMap<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    private void putAllForCreate(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            putForCreate(e.getKey(), e.getValue());
        }
    }

    /* access modifiers changed from: package-private */
    public void resize(int newCapacity) {
        if (this.table.length == MAXIMUM_CAPACITY) {
            this.threshold = Integer.MAX_VALUE;
            return;
        }
        Entry<K, V>[] newTable = new Entry[newCapacity];
        transfer(newTable);
        this.table = newTable;
        this.threshold = (int) (((float) newCapacity) * this.loadFactor);
    }

    /* access modifiers changed from: package-private */
    public void transfer(Entry[] newTable) {
        Entry<K, V>[] src = this.table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry<K, V> e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry<K, V> next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.alibaba.fastjson.util.AntiCollisionHashMap<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.AbstractMap, java.util.Map
    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded != 0) {
            if (numKeysToBeAdded > this.threshold) {
                int targetCapacity = (int) ((((float) numKeysToBeAdded) / this.loadFactor) + 1.0f);
                if (targetCapacity > MAXIMUM_CAPACITY) {
                    targetCapacity = MAXIMUM_CAPACITY;
                }
                int newCapacity = this.table.length;
                while (newCapacity < targetCapacity) {
                    newCapacity <<= 1;
                }
                if (newCapacity > this.table.length) {
                    resize(newCapacity);
                }
            }
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    @Override // java.util.AbstractMap, java.util.Map
    public V remove(Object key) {
        Entry<K, V> e = removeEntryForKey(key);
        if (e == null) {
            return null;
        }
        return e.value;
    }

    /* access modifiers changed from: package-private */
    public final Entry<K, V> removeEntryForKey(Object key) {
        int hash;
        Object k;
        if (key == null) {
            hash = 0;
        } else if (key instanceof String) {
            hash = hash(hashString((String) key));
        } else {
            hash = hash(key.hashCode());
        }
        int i = indexFor(hash, this.table.length);
        Entry<K, V> e = this.table[i];
        Entry<K, V> prev = e;
        while (e != null) {
            Entry<K, V> next = e.next;
            if (e.hash != hash || ((k = e.key) != key && (key == null || !key.equals(k)))) {
                prev = e;
                e = next;
            } else {
                this.modCount++;
                this.size--;
                if (prev == e) {
                    this.table[i] = next;
                } else {
                    prev.next = next;
                }
                return e;
            }
        }
        return e;
    }

    /* access modifiers changed from: package-private */
    public final Entry<K, V> removeMapping(Object o) {
        int hash;
        if (!(o instanceof Map.Entry)) {
            return null;
        }
        Map.Entry<K, V> entry = (Map.Entry) o;
        Object key = entry.getKey();
        if (key == null) {
            hash = 0;
        } else if (key instanceof String) {
            hash = hash(hashString((String) key));
        } else {
            hash = hash(key.hashCode());
        }
        int i = indexFor(hash, this.table.length);
        Entry<K, V> e = this.table[i];
        Entry<K, V> prev = e;
        while (e != null) {
            Entry<K, V> next = e.next;
            if (e.hash != hash || !e.equals(entry)) {
                prev = e;
                e = next;
            } else {
                this.modCount++;
                this.size--;
                if (prev == e) {
                    this.table[i] = next;
                } else {
                    prev.next = next;
                }
                return e;
            }
        }
        return e;
    }

    public void clear() {
        this.modCount++;
        Entry[] tab = this.table;
        for (int i = 0; i < tab.length; i++) {
            tab[i] = null;
        }
        this.size = 0;
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            return containsNullValue();
        }
        Entry[] tab = this.table;
        for (Entry e : tab) {
            for (; e != null; e = e.next) {
                if (value.equals(e.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsNullValue() {
        Entry[] tab = this.table;
        for (Entry e : tab) {
            for (; e != null; e = e.next) {
                if (e.value == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // java.util.AbstractMap, java.lang.Object
    public Object clone() {
        AntiCollisionHashMap<K, V> result = null;
        try {
            result = (AntiCollisionHashMap) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        result.table = new Entry[this.table.length];
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        result.putAllForCreate(this);
        return result;
    }

    /* access modifiers changed from: package-private */
    public static class Entry<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        Entry<K, V> next;
        V value;

        Entry(int h, K k, V v, Entry<K, V> n) {
            this.value = v;
            this.next = n;
            this.key = k;
            this.hash = h;
        }

        @Override // java.util.Map.Entry
        public final K getKey() {
            return this.key;
        }

        @Override // java.util.Map.Entry
        public final V getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public final V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2) {
                    return true;
                }
                if (v1 == null || !v1.equals(v2)) {
                    return false;
                }
                return true;
            }
            return false;
        }

        public final int hashCode() {
            int i = 0;
            int hashCode = this.key == null ? 0 : this.key.hashCode();
            if (this.value != null) {
                i = this.value.hashCode();
            }
            return hashCode ^ i;
        }

        public final String toString() {
            return ((Object) getKey()) + "=" + ((Object) getValue());
        }
    }

    /* access modifiers changed from: package-private */
    public void addEntry(int hash, K key, V value, int bucketIndex) {
        this.table[bucketIndex] = new Entry<>(hash, key, value, this.table[bucketIndex]);
        int i = this.size;
        this.size = i + 1;
        if (i >= this.threshold) {
            resize(2 * this.table.length);
        }
    }

    /* access modifiers changed from: package-private */
    public void createEntry(int hash, K key, V value, int bucketIndex) {
        this.table[bucketIndex] = new Entry<>(hash, key, value, this.table[bucketIndex]);
        this.size++;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        Entry<K, V> current;
        int expectedModCount;
        int index;
        Entry<K, V> next;

        HashIterator() {
            this.expectedModCount = AntiCollisionHashMap.this.modCount;
            if (AntiCollisionHashMap.this.size > 0) {
                Entry[] t = AntiCollisionHashMap.this.table;
                while (this.index < t.length) {
                    int i = this.index;
                    this.index = i + 1;
                    Entry entry = t[i];
                    this.next = entry;
                    if (entry != null) {
                        return;
                    }
                }
            }
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        /* access modifiers changed from: package-private */
        public final Entry<K, V> nextEntry() {
            if (AntiCollisionHashMap.this.modCount == this.expectedModCount) {
                Entry<K, V> e = this.next;
                if (e != null) {
                    Entry<K, V> entry = e.next;
                    this.next = entry;
                    if (entry == null) {
                        Entry[] t = AntiCollisionHashMap.this.table;
                        while (this.index < t.length) {
                            int i = this.index;
                            this.index = i + 1;
                            Entry entry2 = t[i];
                            this.next = entry2;
                            if (entry2 != null) {
                                break;
                            }
                        }
                    }
                    this.current = e;
                    return e;
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        public void remove() {
            if (this.current == null) {
                throw new IllegalStateException();
            } else if (AntiCollisionHashMap.this.modCount == this.expectedModCount) {
                Object k = this.current.key;
                this.current = null;
                AntiCollisionHashMap.this.removeEntryForKey(k);
                this.expectedModCount = AntiCollisionHashMap.this.modCount;
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ValueIterator extends AntiCollisionHashMap<K, V>.HashIterator {
        private ValueIterator() {
            super();
        }

        public V next() {
            return nextEntry().value;
        }
    }

    /* access modifiers changed from: private */
    public final class KeyIterator extends AntiCollisionHashMap<K, V>.HashIterator {
        private KeyIterator() {
            super();
        }

        public K next() {
            return (K) nextEntry().getKey();
        }
    }

    /* access modifiers changed from: private */
    public final class EntryIterator extends AntiCollisionHashMap<K, V>.HashIterator {
        private EntryIterator() {
            super();
        }

        public Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    /* access modifiers changed from: package-private */
    public Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }

    /* access modifiers changed from: package-private */
    public Iterator<V> newValueIterator() {
        return new ValueIterator();
    }

    /* access modifiers changed from: package-private */
    public Iterator<Map.Entry<K, V>> newEntryIterator() {
        return new EntryIterator();
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Set<K> keySet() {
        Set<K> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        KeySet keySet2 = new KeySet();
        this.keySet = keySet2;
        return keySet2;
    }

    private final class KeySet extends AbstractSet<K> {
        private KeySet() {
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set, java.lang.Iterable
        public Iterator<K> iterator() {
            return AntiCollisionHashMap.this.newKeyIterator();
        }

        public int size() {
            return AntiCollisionHashMap.this.size;
        }

        public boolean contains(Object o) {
            return AntiCollisionHashMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return AntiCollisionHashMap.this.removeEntryForKey(o) != null;
        }

        public void clear() {
            AntiCollisionHashMap.this.clear();
        }
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Collection<V> values() {
        Collection<V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        Values values2 = new Values();
        this.values = values2;
        return values2;
    }

    private final class Values extends AbstractCollection<V> {
        private Values() {
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable
        public Iterator<V> iterator() {
            return AntiCollisionHashMap.this.newValueIterator();
        }

        public int size() {
            return AntiCollisionHashMap.this.size;
        }

        public boolean contains(Object o) {
            return AntiCollisionHashMap.this.containsValue(o);
        }

        public void clear() {
            AntiCollisionHashMap.this.clear();
        }
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        return entrySet0();
    }

    private Set<Map.Entry<K, V>> entrySet0() {
        Set<Map.Entry<K, V>> es = this.entrySet;
        if (es != null) {
            return es;
        }
        EntrySet entrySet2 = new EntrySet();
        this.entrySet = entrySet2;
        return entrySet2;
    }

    /* access modifiers changed from: private */
    public final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        private EntrySet() {
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set, java.lang.Iterable
        public Iterator<Map.Entry<K, V>> iterator() {
            return AntiCollisionHashMap.this.newEntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<K, V> e = (Map.Entry) o;
            Entry<K, V> candidate = AntiCollisionHashMap.this.getEntry(e.getKey());
            if (candidate == null || !candidate.equals(e)) {
                return false;
            }
            return true;
        }

        public boolean remove(Object o) {
            return AntiCollisionHashMap.this.removeMapping(o) != null;
        }

        public int size() {
            return AntiCollisionHashMap.this.size;
        }

        public void clear() {
            AntiCollisionHashMap.this.clear();
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        Iterator<Map.Entry<K, V>> i = this.size > 0 ? entrySet0().iterator() : null;
        s.defaultWriteObject();
        s.writeInt(this.table.length);
        s.writeInt(this.size);
        if (i != null) {
            while (i.hasNext()) {
                Map.Entry<K, V> e = i.next();
                s.writeObject(e.getKey());
                s.writeObject(e.getValue());
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.alibaba.fastjson.util.AntiCollisionHashMap<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.table = new Entry[s.readInt()];
        init();
        int size2 = s.readInt();
        for (int i = 0; i < size2; i++) {
            putForCreate(s.readObject(), s.readObject());
        }
    }
}
