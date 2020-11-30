package com.alibaba.fastjson.asm;

public class Label {
    int inputStackTop;
    Label next;
    int outputStackMax;
    int position;
    private int referenceCount;
    private int[] srcAndRefPositions;
    int status;
    Label successor;

    /* access modifiers changed from: package-private */
    public void put(MethodWriter owner, ByteVector out, int source) {
        if ((this.status & 2) == 0) {
            addReference(source, out.length);
            out.putShort(-1);
            return;
        }
        out.putShort(this.position - source);
    }

    private void addReference(int sourcePosition, int referencePosition) {
        if (this.srcAndRefPositions == null) {
            this.srcAndRefPositions = new int[6];
        }
        if (this.referenceCount >= this.srcAndRefPositions.length) {
            int[] a = new int[(this.srcAndRefPositions.length + 6)];
            System.arraycopy(this.srcAndRefPositions, 0, a, 0, this.srcAndRefPositions.length);
            this.srcAndRefPositions = a;
        }
        int[] a2 = this.srcAndRefPositions;
        int i = this.referenceCount;
        this.referenceCount = i + 1;
        a2[i] = sourcePosition;
        int[] iArr = this.srcAndRefPositions;
        int i2 = this.referenceCount;
        this.referenceCount = i2 + 1;
        iArr[i2] = referencePosition;
    }

    /* JADX INFO: Multiple debug info for r0v4 int: [D('i' int), D('source' int)] */
    /* JADX INFO: Multiple debug info for r2v1 int: [D('i' int), D('offset' int)] */
    /* access modifiers changed from: package-private */
    public void resolve(MethodWriter owner, int position2, byte[] data) {
        this.status |= 2;
        this.position = position2;
        int i = 0;
        while (i < this.referenceCount) {
            int i2 = i + 1;
            int source = this.srcAndRefPositions[i];
            int i3 = i2 + 1;
            int reference = this.srcAndRefPositions[i2];
            int offset = position2 - source;
            data[reference] = (byte) (offset >>> 8);
            data[reference + 1] = (byte) offset;
            i = i3;
        }
    }
}
