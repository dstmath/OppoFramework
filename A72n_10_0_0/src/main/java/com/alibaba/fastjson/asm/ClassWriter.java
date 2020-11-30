package com.alibaba.fastjson.asm;

public class ClassWriter {
    private int access;
    FieldWriter firstField;
    MethodWriter firstMethod;
    int index;
    private int interfaceCount;
    private int[] interfaces;
    Item[] items;
    final Item key;
    final Item key2;
    final Item key3;
    FieldWriter lastField;
    MethodWriter lastMethod;
    private int name;
    final ByteVector pool;
    private int superName;
    String thisName;
    int threshold;
    Item[] typeTable;
    int version;

    public ClassWriter() {
        this(0);
    }

    private ClassWriter(int flags) {
        this.index = 1;
        this.pool = new ByteVector();
        this.items = new Item[256];
        this.threshold = (int) (0.75d * ((double) this.items.length));
        this.key = new Item();
        this.key2 = new Item();
        this.key3 = new Item();
    }

    public void visit(int version2, int access2, String name2, String superName2, String[] interfaces2) {
        this.version = version2;
        this.access = access2;
        this.name = newClassItem(name2).index;
        this.thisName = name2;
        this.superName = superName2 == null ? 0 : newClassItem(superName2).index;
        if (interfaces2 != null && interfaces2.length > 0) {
            this.interfaceCount = interfaces2.length;
            this.interfaces = new int[this.interfaceCount];
            for (int i = 0; i < this.interfaceCount; i++) {
                this.interfaces[i] = newClassItem(interfaces2[i]).index;
            }
        }
    }

    public byte[] toByteArray() {
        int size = 24 + (2 * this.interfaceCount);
        int nbFields = 0;
        for (FieldWriter fb = this.firstField; fb != null; fb = fb.next) {
            nbFields++;
            size += fb.getSize();
        }
        int nbMethods = 0;
        for (MethodWriter mb = this.firstMethod; mb != null; mb = mb.next) {
            nbMethods++;
            size += mb.getSize();
        }
        ByteVector out = new ByteVector(size + this.pool.length);
        out.putInt(-889275714).putInt(this.version);
        out.putShort(this.index).putByteArray(this.pool.data, 0, this.pool.length);
        out.putShort(this.access & (~393216)).putShort(this.name).putShort(this.superName);
        out.putShort(this.interfaceCount);
        for (int i = 0; i < this.interfaceCount; i++) {
            out.putShort(this.interfaces[i]);
        }
        out.putShort(nbFields);
        for (FieldWriter fb2 = this.firstField; fb2 != null; fb2 = fb2.next) {
            fb2.put(out);
        }
        out.putShort(nbMethods);
        for (MethodWriter mb2 = this.firstMethod; mb2 != null; mb2 = mb2.next) {
            mb2.put(out);
        }
        out.putShort(0);
        return out.data;
    }

    /* access modifiers changed from: package-private */
    public Item newConstItem(Object cst) {
        if (cst instanceof Integer) {
            int val = ((Integer) cst).intValue();
            this.key.set(val);
            Item result = get(this.key);
            if (result != null) {
                return result;
            }
            this.pool.putByte(3).putInt(val);
            int i = this.index;
            this.index = i + 1;
            Item result2 = new Item(i, this.key);
            put(result2);
            return result2;
        } else if (cst instanceof String) {
            return newString((String) cst);
        } else {
            if (cst instanceof Type) {
                Type t = (Type) cst;
                return newClassItem(t.sort == 10 ? t.getInternalName() : t.getDescriptor());
            }
            throw new IllegalArgumentException("value " + cst);
        }
    }

    public int newUTF8(String value) {
        this.key.set(1, value, null, null);
        Item result = get(this.key);
        if (result == null) {
            this.pool.putByte(1).putUTF8(value);
            int i = this.index;
            this.index = i + 1;
            result = new Item(i, this.key);
            put(result);
        }
        return result.index;
    }

    public Item newClassItem(String value) {
        this.key2.set(7, value, null, null);
        Item result = get(this.key2);
        if (result != null) {
            return result;
        }
        this.pool.put12(7, newUTF8(value));
        int i = this.index;
        this.index = i + 1;
        Item result2 = new Item(i, this.key2);
        put(result2);
        return result2;
    }

    /* access modifiers changed from: package-private */
    public Item newFieldItem(String owner, String name2, String desc) {
        this.key3.set(9, owner, name2, desc);
        Item result = get(this.key3);
        if (result != null) {
            return result;
        }
        int s1 = newClassItem(owner).index;
        this.pool.put12(9, s1).putShort(newNameTypeItem(name2, desc).index);
        int i = this.index;
        this.index = i + 1;
        Item result2 = new Item(i, this.key3);
        put(result2);
        return result2;
    }

    /* access modifiers changed from: package-private */
    public Item newMethodItem(String owner, String name2, String desc, boolean itf) {
        int type = itf ? 11 : 10;
        this.key3.set(type, owner, name2, desc);
        Item result = get(this.key3);
        if (result != null) {
            return result;
        }
        this.pool.put12(type, newClassItem(owner).index).putShort(newNameTypeItem(name2, desc).index);
        int i = this.index;
        this.index = i + 1;
        Item result2 = new Item(i, this.key3);
        put(result2);
        return result2;
    }

    private Item newString(String value) {
        this.key2.set(8, value, null, null);
        Item result = get(this.key2);
        if (result != null) {
            return result;
        }
        this.pool.put12(8, newUTF8(value));
        int i = this.index;
        this.index = i + 1;
        Item result2 = new Item(i, this.key2);
        put(result2);
        return result2;
    }

    public Item newNameTypeItem(String name2, String desc) {
        this.key2.set(12, name2, desc, null);
        Item result = get(this.key2);
        if (result != null) {
            return result;
        }
        int s1 = newUTF8(name2);
        this.pool.put12(12, s1).putShort(newUTF8(desc));
        int i = this.index;
        this.index = i + 1;
        Item result2 = new Item(i, this.key2);
        put(result2);
        return result2;
    }

    private Item get(Item key4) {
        Item i = this.items[key4.hashCode % this.items.length];
        while (i != null && (i.type != key4.type || !key4.isEqualTo(i))) {
            i = i.next;
        }
        return i;
    }

    private void put(Item i) {
        if (this.index > this.threshold) {
            int ll = this.items.length;
            int nl = (ll * 2) + 1;
            Item[] newItems = new Item[nl];
            for (int l = ll - 1; l >= 0; l--) {
                Item j = this.items[l];
                while (j != null) {
                    int index2 = j.hashCode % newItems.length;
                    Item k = j.next;
                    j.next = newItems[index2];
                    newItems[index2] = j;
                    j = k;
                }
            }
            this.items = newItems;
            this.threshold = (int) (((double) nl) * 0.75d);
        }
        int index3 = i.hashCode % this.items.length;
        i.next = this.items[index3];
        this.items[index3] = i;
    }
}
