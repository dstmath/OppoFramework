package com.alibaba.fastjson.asm;

public class MethodWriter implements MethodVisitor {
    private int access;
    private ByteVector code = new ByteVector();
    final ClassWriter cw;
    private final int desc;
    int exceptionCount;
    int[] exceptions;
    private int maxLocals;
    private int maxStack;
    private final int name;
    MethodWriter next;

    public MethodWriter(ClassWriter cw2, int access2, String name2, String desc2, String signature, String[] exceptions2) {
        if (cw2.firstMethod == null) {
            cw2.firstMethod = this;
        } else {
            cw2.lastMethod.next = this;
        }
        cw2.lastMethod = this;
        this.cw = cw2;
        this.access = access2;
        this.name = cw2.newUTF8(name2);
        this.desc = cw2.newUTF8(desc2);
        if (exceptions2 != null && exceptions2.length > 0) {
            this.exceptionCount = exceptions2.length;
            this.exceptions = new int[this.exceptionCount];
            for (int i = 0; i < this.exceptionCount; i++) {
                this.exceptions[i] = cw2.newClassItem(exceptions2[i]).index;
            }
        }
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitInsn(int opcode) {
        this.code.putByte(opcode);
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitIntInsn(int opcode, int operand) {
        this.code.put11(opcode, operand);
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitVarInsn(int opcode, int var) {
        int opt;
        if (var < 4 && opcode != 169) {
            if (opcode < 54) {
                opt = 26 + ((opcode - 21) << 2) + var;
            } else {
                opt = 59 + ((opcode - 54) << 2) + var;
            }
            this.code.putByte(opt);
        } else if (var >= 256) {
            this.code.putByte(196).put12(opcode, var);
        } else {
            this.code.put11(opcode, var);
        }
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitTypeInsn(int opcode, String type) {
        this.code.put12(opcode, this.cw.newClassItem(type).index);
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitFieldInsn(int opcode, String owner, String name2, String desc2) {
        this.code.put12(opcode, this.cw.newFieldItem(owner, name2, desc2).index);
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitMethodInsn(int opcode, String owner, String name2, String desc2) {
        boolean itf = opcode == 185;
        Item i = this.cw.newMethodItem(owner, name2, desc2, itf);
        int argSize = i.intVal;
        if (itf) {
            if (argSize == 0) {
                argSize = Type.getArgumentsAndReturnSizes(desc2);
                i.intVal = argSize;
            }
            this.code.put12(Opcodes.INVOKEINTERFACE, i.index).put11(argSize >> 2, 0);
            return;
        }
        this.code.put12(opcode, i.index);
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitJumpInsn(int opcode, Label label) {
        if ((label.status & 2) == 0 || label.position - this.code.length >= -32768) {
            this.code.putByte(opcode);
            label.put(this, this.code, this.code.length - 1);
            return;
        }
        throw new UnsupportedOperationException();
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitLabel(Label label) {
        label.resolve(this, this.code.length, this.code.data);
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitLdcInsn(Object cst) {
        Item i = this.cw.newConstItem(cst);
        int index = i.index;
        if (i.type == 5 || i.type == 6) {
            this.code.put12(20, index);
        } else if (index >= 256) {
            this.code.put12(19, index);
        } else {
            this.code.put11(18, index);
        }
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitIincInsn(int var, int increment) {
        this.code.putByte(132).put11(var, increment);
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitMaxs(int maxStack2, int maxLocals2) {
        this.maxStack = maxStack2;
        this.maxLocals = maxLocals2;
    }

    @Override // com.alibaba.fastjson.asm.MethodVisitor
    public void visitEnd() {
    }

    /* access modifiers changed from: package-private */
    public final int getSize() {
        int size = 8;
        if (this.code.length > 0) {
            this.cw.newUTF8("Code");
            size = 8 + 18 + this.code.length + 0;
        }
        if (this.exceptionCount <= 0) {
            return size;
        }
        this.cw.newUTF8("Exceptions");
        return size + 8 + (2 * this.exceptionCount);
    }

    /* access modifiers changed from: package-private */
    public final void put(ByteVector out) {
        out.putShort(this.access & -393217).putShort(this.name).putShort(this.desc);
        int attributeCount = 0;
        if (this.code.length > 0) {
            attributeCount = 0 + 1;
        }
        if (this.exceptionCount > 0) {
            attributeCount++;
        }
        out.putShort(attributeCount);
        if (this.code.length > 0) {
            out.putShort(this.cw.newUTF8("Code")).putInt(12 + this.code.length + 0);
            out.putShort(this.maxStack).putShort(this.maxLocals);
            out.putInt(this.code.length).putByteArray(this.code.data, 0, this.code.length);
            out.putShort(0);
            out.putShort(0);
        }
        if (this.exceptionCount > 0) {
            out.putShort(this.cw.newUTF8("Exceptions")).putInt((this.exceptionCount * 2) + 2);
            out.putShort(this.exceptionCount);
            for (int i = 0; i < this.exceptionCount; i++) {
                out.putShort(this.exceptions[i]);
            }
        }
    }
}
