package com.alibaba.fastjson.asm;

import com.alibaba.fastjson.parser.JSONToken;

public class Type {
    public static final Type BOOLEAN_TYPE = new Type(1, null, 1509950721, 1);
    public static final Type BYTE_TYPE = new Type(3, null, 1107297537, 1);
    public static final Type CHAR_TYPE = new Type(2, null, 1124075009, 1);
    public static final Type DOUBLE_TYPE = new Type(8, null, 1141048066, 1);
    public static final Type FLOAT_TYPE = new Type(6, null, 1174536705, 1);
    public static final Type INT_TYPE = new Type(5, null, 1224736769, 1);
    public static final Type LONG_TYPE = new Type(7, null, 1241579778, 1);
    public static final Type SHORT_TYPE = new Type(4, null, 1392510721, 1);
    public static final Type VOID_TYPE = new Type(0, null, 1443168256, 1);
    private final char[] buf;
    private final int len;
    private final int off;
    protected final int sort;

    private Type(int sort2, char[] buf2, int off2, int len2) {
        this.sort = sort2;
        this.buf = buf2;
        this.off = off2;
        this.len = len2;
    }

    public static Type getType(String typeDescriptor) {
        return getType(typeDescriptor.toCharArray(), 0);
    }

    /* JADX INFO: Multiple debug info for r0v3 char: [D('c' int), D('car' char)] */
    public static int getArgumentsAndReturnSizes(String desc) {
        int c;
        int c2;
        int i = 1;
        int n = 1;
        int c3 = 1;
        while (true) {
            c = c3 + 1;
            char car = desc.charAt(c3);
            if (car == ')') {
                break;
            } else if (car == 'L') {
                while (true) {
                    c2 = c + 1;
                    if (desc.charAt(c) == 59) {
                        break;
                    }
                    c = c2;
                }
                n++;
                c3 = c2;
            } else {
                if (car == 'D' || car == 'J') {
                    n += 2;
                } else {
                    n++;
                }
                c3 = c;
            }
        }
        char car2 = desc.charAt(c);
        int i2 = n << 2;
        if (car2 == 'V') {
            i = 0;
        } else if (car2 == 'D' || car2 == 'J') {
            i = 2;
        }
        return i | i2;
    }

    private static Type getType(char[] buf2, int off2) {
        int len2 = 1;
        switch (buf2[off2]) {
            case 'B':
                return BYTE_TYPE;
            case 'C':
                return CHAR_TYPE;
            case 'D':
                return DOUBLE_TYPE;
            case 'F':
                return FLOAT_TYPE;
            case 'I':
                return INT_TYPE;
            case 'J':
                return LONG_TYPE;
            case 'S':
                return SHORT_TYPE;
            case 'V':
                return VOID_TYPE;
            case 'Z':
                return BOOLEAN_TYPE;
            case '[':
                while (true) {
                    int len3 = len2;
                    if (buf2[off2 + len3] == '[') {
                        len2 = len3 + 1;
                    } else {
                        if (buf2[off2 + len3] == 'L') {
                            while (true) {
                                len3++;
                                if (buf2[off2 + len3] != ';') {
                                }
                            }
                        }
                        return new Type(9, buf2, off2, len3 + 1);
                    }
                }
            default:
                while (buf2[off2 + len2] != ';') {
                    len2++;
                }
                return new Type(10, buf2, off2 + 1, len2 - 1);
        }
    }

    public String getInternalName() {
        return new String(this.buf, this.off, this.len);
    }

    /* access modifiers changed from: package-private */
    public String getDescriptor() {
        return new String(this.buf, this.off, this.len);
    }

    private int getDimensions() {
        int i = 1;
        while (this.buf[this.off + i] == '[') {
            i++;
        }
        return i;
    }

    /* JADX INFO: Multiple debug info for r3v2 char: [D('off' int), D('car' char)] */
    /* JADX INFO: Multiple debug info for r3v3 com.alibaba.fastjson.asm.Type[]: [D('args' com.alibaba.fastjson.asm.Type[]), D('car' char)] */
    static Type[] getArgumentTypes(String methodDescriptor) {
        int off2;
        char[] buf2 = methodDescriptor.toCharArray();
        int off3 = 1;
        int size = 0;
        while (true) {
            int off4 = off3 + 1;
            char car = buf2[off3];
            if (car == ')') {
                break;
            } else if (car == 'L') {
                while (true) {
                    off2 = off4 + 1;
                    if (buf2[off4] == ';') {
                        break;
                    }
                    off4 = off2;
                }
                size++;
                off3 = off2;
            } else {
                if (car != '[') {
                    size++;
                }
                off3 = off4;
            }
        }
        Type[] args = new Type[size];
        int off5 = 1;
        int size2 = 0;
        while (buf2[off5] != ')') {
            args[size2] = getType(buf2, off5);
            off5 += args[size2].len + (args[size2].sort == 10 ? 2 : 0);
            size2++;
        }
        return args;
    }

    /* access modifiers changed from: protected */
    public String getClassName() {
        switch (this.sort) {
            case 0:
                return "void";
            case 1:
                return "boolean";
            case 2:
                return "char";
            case 3:
                return "byte";
            case 4:
                return "short";
            case 5:
                return "int";
            case JSONToken.TRUE /* 6 */:
                return "float";
            case JSONToken.FALSE /* 7 */:
                return "long";
            case JSONToken.NULL /* 8 */:
                return "double";
            case 9:
                StringBuilder b = new StringBuilder(getType(this.buf, this.off + getDimensions()).getClassName());
                for (int i = getDimensions(); i > 0; i--) {
                    b.append("[]");
                }
                return b.toString();
            default:
                return new String(this.buf, this.off, this.len).replace('/', '.');
        }
    }
}
