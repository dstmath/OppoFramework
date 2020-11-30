package com.alibaba.fastjson.util;

import java.util.Arrays;

public class IdentityHashMap<K, V> {
    public static final int DEFAULT_SIZE = 8192;
    private final Entry<K, V>[] buckets;
    private final int indexMask;

    public IdentityHashMap() {
        this(DEFAULT_SIZE);
    }

    public IdentityHashMap(int tableSize) {
        this.indexMask = tableSize - 1;
        this.buckets = new Entry[tableSize];
    }

    public final V get(K key) {
        int hash = System.identityHashCode(key);
        for (Entry<K, V> entry = this.buckets[this.indexMask & hash]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                return entry.value;
            }
        }
        return null;
    }

    public Class findClass(String keyString) {
        for (int i = 0; i < this.buckets.length; i++) {
            Entry<K, V> bucket = this.buckets[i];
            if (bucket != null) {
                for (Entry<K, V> entry = bucket; entry != null; entry = entry.next) {
                    Class key = bucket.key;
                    if (key instanceof Class) {
                        Class clazz = key;
                        if (clazz.getName().equals(keyString)) {
                            return clazz;
                        }
                    }
                }
                continue;
            }
        }
        return null;
    }

    public boolean put(K key, V value) {
        int hash = System.identityHashCode(key);
        int bucket = this.indexMask & hash;
        for (Entry<K, V> entry = this.buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                entry.value = value;
                return true;
            }
        }
        this.buckets[bucket] = new Entry<>(key, value, hash, this.buckets[bucket]);
        return false;
    }

    /* access modifiers changed from: protected */
    public static final class Entry<K, V> {
        public final int hashCode;
        public final K key;
        public final Entry<K, V> next;
        public V value;

        public Entry(K key2, V value2, int hash, Entry<K, V> next2) {
            this.key = key2;
            this.value = value2;
            this.next = next2;
            this.hashCode = hash;
        }
    }

    public void clear() {
        Arrays.fill(this.buckets, (Object) null);
    }
}
