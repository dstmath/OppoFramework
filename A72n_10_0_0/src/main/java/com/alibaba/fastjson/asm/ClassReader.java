package com.alibaba.fastjson.asm;

import com.alibaba.fastjson.parser.JSONToken;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassReader {
    public final byte[] b;
    public final int header;
    private final int[] items;
    private final int maxStringLength;
    private boolean readAnnotations;
    private final String[] strings;

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public ClassReader(InputStream is, boolean readAnnotations2) throws IOException {
        this.readAnnotations = readAnnotations2;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
            int len = is.read(buf);
            if (len == -1) {
                break;
            } else if (len > 0) {
                out.write(buf, 0, len);
            }
        }
        is.close();
        this.b = out.toByteArray();
        this.items = new int[readUnsignedShort(8)];
        int n = this.items.length;
        this.strings = new String[n];
        int index = 10;
        int max = 0;
        int i = 1;
        while (i < n) {
            this.items[i] = index + 1;
            byte b2 = this.b[index];
            int size = 3;
            if (b2 == 1) {
                size = 3 + readUnsignedShort(index + 1);
                if (size > max) {
                    max = size;
                }
            } else if (b2 != 15) {
                if (b2 != 18) {
                    switch (b2) {
                        case 3:
                        case 4:
                            break;
                        case 5:
                        case JSONToken.TRUE /* 6 */:
                            size = 9;
                            i++;
                            break;
                        default:
                            switch (b2) {
                            }
                    }
                }
                size = 5;
            } else {
                size = 4;
            }
            index += size;
            i++;
        }
        this.maxStringLength = max;
        this.header = index;
    }

    public void accept(TypeCollector classVisitor) {
        char[] c = new char[this.maxStringLength];
        int anns = 0;
        if (this.readAnnotations) {
            int u = getAttributes();
            int i = readUnsignedShort(u);
            while (true) {
                if (i <= 0) {
                    break;
                } else if ("RuntimeVisibleAnnotations".equals(readUTF8(u + 2, c))) {
                    anns = u + 8;
                    break;
                } else {
                    u += readInt(u + 4) + 6;
                    i--;
                }
            }
        }
        int u2 = this.header;
        int i2 = this.items[readUnsignedShort(u2 + 4)];
        int len = readUnsignedShort(u2 + 6);
        int u3 = u2 + 8;
        for (int i3 = 0; i3 < len; i3++) {
            u3 += 2;
        }
        int v = u3 + 2;
        for (int i4 = readUnsignedShort(u3); i4 > 0; i4--) {
            v += 8;
            for (int j = readUnsignedShort(v + 6); j > 0; j--) {
                v += readInt(v + 2) + 6;
            }
        }
        int v2 = v + 2;
        for (int i5 = readUnsignedShort(v); i5 > 0; i5--) {
            v2 += 8;
            for (int j2 = readUnsignedShort(v2 + 6); j2 > 0; j2--) {
                v2 += readInt(v2 + 2) + 6;
            }
        }
        int v3 = v2 + 2;
        for (int i6 = readUnsignedShort(v2); i6 > 0; i6--) {
            v3 += readInt(v3 + 2) + 6;
        }
        if (anns != 0) {
            int v4 = anns + 2;
            for (int i7 = readUnsignedShort(anns); i7 > 0; i7--) {
                classVisitor.visitAnnotation(readUTF8(v4, c));
            }
        }
        int u4 = u3 + 2;
        for (int i8 = readUnsignedShort(u3); i8 > 0; i8--) {
            u4 += 8;
            for (int j3 = readUnsignedShort(u4 + 6); j3 > 0; j3--) {
                u4 += readInt(u4 + 2) + 6;
            }
        }
        int u5 = u4 + 2;
        for (int i9 = readUnsignedShort(u4); i9 > 0; i9--) {
            u5 = readMethod(classVisitor, c, u5);
        }
    }

    private int getAttributes() {
        int u = this.header + 8 + (readUnsignedShort(this.header + 6) * 2);
        for (int i = readUnsignedShort(u); i > 0; i--) {
            for (int j = readUnsignedShort(u + 8); j > 0; j--) {
                u += readInt(u + 12) + 6;
            }
            u += 8;
        }
        int u2 = u + 2;
        for (int i2 = readUnsignedShort(u2); i2 > 0; i2--) {
            for (int j2 = readUnsignedShort(u2 + 8); j2 > 0; j2--) {
                u2 += readInt(u2 + 12) + 6;
            }
            u2 += 8;
        }
        return u2 + 2;
    }

    /* JADX INFO: Multiple debug info for r7v5 int[]: [D('w' int), D('typeTable' int[])] */
    /* JADX INFO: Multiple debug info for r3v11 java.lang.String: [D('access' int), D('attrName' java.lang.String)] */
    private int readMethod(TypeCollector classVisitor, char[] c, int u) {
        int access = readUnsignedShort(u);
        String name = readUTF8(u + 2, c);
        String desc = readUTF8(u + 4, c);
        int v = 0;
        int w = 0;
        int u2 = u + 8;
        for (int j = readUnsignedShort(u + 6); j > 0; j--) {
            String attrName = readUTF8(u2, c);
            int attrSize = readInt(u2 + 2);
            int u3 = u2 + 6;
            if (attrName.equals("Code")) {
                v = u3;
            }
            u2 = u3 + attrSize;
        }
        if (0 != 0) {
            w = 0 + 2;
            for (int j2 = 0; j2 < readUnsignedShort(w); j2++) {
                w += 2;
            }
        }
        MethodCollector mv = classVisitor.visitMethod(access, name, desc);
        if (mv != null && v != 0) {
            int codeEnd = v + 8 + readInt(v + 4);
            int v2 = codeEnd + 2;
            for (int j3 = readUnsignedShort(codeEnd); j3 > 0; j3--) {
                v2 += 8;
            }
            int varTable = 0;
            int varTypeTable = 0;
            int j4 = readUnsignedShort(v2);
            int v3 = v2 + 2;
            while (j4 > 0) {
                String attrName2 = readUTF8(v3, c);
                if (attrName2.equals("LocalVariableTable")) {
                    varTable = v3 + 6;
                } else if (attrName2.equals("LocalVariableTypeTable")) {
                    varTypeTable = v3 + 6;
                }
                v3 += 6 + readInt(v3 + 2);
                j4--;
                access = access;
                name = name;
            }
            if (varTable != 0) {
                if (varTypeTable != 0) {
                    int k = readUnsignedShort(varTypeTable) * 3;
                    int w2 = varTypeTable + 2;
                    int[] typeTable = new int[k];
                    while (k > 0) {
                        int k2 = k - 1;
                        typeTable[k2] = w2 + 6;
                        int k3 = k2 - 1;
                        typeTable[k3] = readUnsignedShort(w2 + 8);
                        k = k3 - 1;
                        typeTable[k] = readUnsignedShort(w2);
                        w2 += 10;
                        desc = desc;
                    }
                }
                int w3 = varTable + 2;
                for (int k4 = readUnsignedShort(varTable); k4 > 0; k4--) {
                    mv.visitLocalVariable(readUTF8(w3 + 4, c), readUnsignedShort(w3 + 8));
                    w3 += 10;
                }
            }
        }
        return u2;
    }

    private int readUnsignedShort(int index) {
        byte[] b2 = this.b;
        return ((b2[index] & 255) << 8) | (b2[index + 1] & 255);
    }

    private int readInt(int index) {
        byte[] b2 = this.b;
        return ((b2[index] & 255) << 24) | ((b2[index + 1] & 255) << 16) | ((b2[index + 2] & 255) << 8) | (b2[index + 3] & 255);
    }

    private String readUTF8(int index, char[] buf) {
        int item = readUnsignedShort(index);
        String s = this.strings[item];
        if (s != null) {
            return s;
        }
        int index2 = this.items[item];
        String[] strArr = this.strings;
        String readUTF = readUTF(index2 + 2, readUnsignedShort(index2), buf);
        strArr[item] = readUTF;
        return readUTF;
    }

    /* JADX INFO: Multiple debug info for r2v3 byte: [D('index' int), D('c' int)] */
    private String readUTF(int index, int utfLen, char[] buf) {
        int strLen;
        int endIndex = index + utfLen;
        byte[] b2 = this.b;
        int st = 0;
        int strLen2 = 0;
        int c = index;
        char cc = 0;
        while (c < endIndex) {
            int index2 = c + 1;
            byte b3 = b2[c];
            switch (st) {
                case 0:
                    int c2 = b3 & 255;
                    if (c2 < 128) {
                        strLen = strLen2 + 1;
                        buf[strLen2] = (char) c2;
                        break;
                    } else if (c2 >= 224 || c2 <= 191) {
                        cc = (char) (c2 & 15);
                        st = 2;
                        continue;
                        c = index2;
                    } else {
                        cc = (char) (c2 & 31);
                        st = 1;
                        c = index2;
                    }
                    break;
                case 1:
                    strLen = strLen2 + 1;
                    buf[strLen2] = (char) ((cc << 6) | (b3 & 63));
                    st = 0;
                    break;
                case 2:
                    cc = (char) ((cc << 6) | (b3 & 63));
                    st = 1;
                    continue;
                    c = index2;
                default:
                    c = index2;
            }
            strLen2 = strLen;
            c = index2;
        }
        return new String(buf, 0, strLen2);
    }
}
