package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.asm.ClassWriter;
import com.alibaba.fastjson.asm.FieldWriter;
import com.alibaba.fastjson.asm.Label;
import com.alibaba.fastjson.asm.MethodVisitor;
import com.alibaba.fastjson.asm.MethodWriter;
import com.alibaba.fastjson.asm.Opcodes;
import com.alibaba.fastjson.asm.Type;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.ASMClassLoader;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ASMSerializerFactory implements Opcodes {
    static final String JSONSerializer = ASMUtils.type(JSONSerializer.class);
    static final String JavaBeanSerializer = ASMUtils.type(JavaBeanSerializer.class);
    static final String JavaBeanSerializer_desc = ("L" + ASMUtils.type(JavaBeanSerializer.class) + ";");
    static final String ObjectSerializer = ASMUtils.type(ObjectSerializer.class);
    static final String ObjectSerializer_desc = ("L" + ObjectSerializer + ";");
    static final String SerialContext_desc = ASMUtils.desc(SerialContext.class);
    static final String SerializeFilterable_desc = ASMUtils.desc(SerializeFilterable.class);
    static final String SerializeWriter = ASMUtils.type(SerializeWriter.class);
    static final String SerializeWriter_desc = ("L" + SerializeWriter + ";");
    protected final ASMClassLoader classLoader = new ASMClassLoader();
    private final AtomicLong seed = new AtomicLong();

    /* access modifiers changed from: package-private */
    public static class Context {
        static final int features = 5;
        static int fieldName = 6;
        static final int obj = 2;
        static int original = 7;
        static final int paramFieldName = 3;
        static final int paramFieldType = 4;
        static int processValue = 8;
        static final int serializer = 1;
        private final SerializeBeanInfo beanInfo;
        private final String className;
        private final FieldInfo[] getters;
        private final boolean nonContext;
        private int variantIndex = 9;
        private Map<String, Integer> variants = new HashMap();
        private final boolean writeDirect;

        public Context(FieldInfo[] getters2, SerializeBeanInfo beanInfo2, String className2, boolean writeDirect2, boolean nonContext2) {
            this.getters = getters2;
            this.className = className2;
            this.beanInfo = beanInfo2;
            this.writeDirect = writeDirect2;
            this.nonContext = nonContext2 || beanInfo2.beanType.isEnum();
        }

        public int var(String name) {
            if (this.variants.get(name) == null) {
                Map<String, Integer> map = this.variants;
                int i = this.variantIndex;
                this.variantIndex = i + 1;
                map.put(name, Integer.valueOf(i));
            }
            return this.variants.get(name).intValue();
        }

        public int var(String name, int increment) {
            if (this.variants.get(name) == null) {
                this.variants.put(name, Integer.valueOf(this.variantIndex));
                this.variantIndex += increment;
            }
            return this.variants.get(name).intValue();
        }

        public int getFieldOrinal(String name) {
            int size = this.getters.length;
            for (int i = 0; i < size; i++) {
                if (this.getters[i].name.equals(name)) {
                    return i;
                }
            }
            return -1;
        }
    }

    /* JADX INFO: Multiple debug info for r1v43 'classNameFull'  java.lang.String: [D('classNameFull' java.lang.String), D('classNameType' java.lang.String)] */
    public JavaBeanSerializer createJavaBeanSerializer(SerializeBeanInfo beanInfo) throws Exception {
        String classNameType;
        String packageName;
        int i;
        String methodName;
        String methodName2;
        String classNameType2;
        String className;
        JSONType jsonType;
        ClassWriter cw;
        Package pkg;
        String classNameFull;
        int i2;
        Class<?> clazz = beanInfo.beanType;
        if (!clazz.isPrimitive()) {
            JSONType jsonType2 = (JSONType) TypeUtils.getAnnotation(clazz, JSONType.class);
            FieldInfo[] unsortedGetters = beanInfo.fields;
            for (FieldInfo fieldInfo : unsortedGetters) {
                if (fieldInfo.field == null && fieldInfo.method != null && fieldInfo.method.getDeclaringClass().isInterface()) {
                    return new JavaBeanSerializer(beanInfo);
                }
            }
            FieldInfo[] getters = beanInfo.sortedFields;
            boolean nativeSorted = beanInfo.sortedFields == beanInfo.fields;
            if (getters.length > 256) {
                return new JavaBeanSerializer(beanInfo);
            }
            for (FieldInfo getter : getters) {
                if (!ASMUtils.checkName(getter.getMember().getName())) {
                    return new JavaBeanSerializer(beanInfo);
                }
            }
            String className2 = "ASMSerializer_" + this.seed.incrementAndGet() + "_" + clazz.getSimpleName();
            Package pkg2 = ASMSerializerFactory.class.getPackage();
            if (pkg2 != null) {
                String packageName2 = pkg2.getName();
                String classNameType3 = packageName2.replace('.', '/') + "/" + className2;
                packageName = packageName2 + "." + className2;
                classNameType = classNameType3;
            } else {
                packageName = className2;
                classNameType = packageName;
            }
            String classNameFull2 = packageName;
            ASMSerializerFactory.class.getPackage().getName();
            ClassWriter cw2 = new ClassWriter();
            cw2.visit(49, 33, classNameType, JavaBeanSerializer, new String[]{ObjectSerializer});
            int length = getters.length;
            int i3 = 0;
            while (i3 < length) {
                FieldInfo fieldInfo2 = getters[i3];
                if (!fieldInfo2.fieldClass.isPrimitive()) {
                    i2 = length;
                    if (fieldInfo2.fieldClass == String.class) {
                        classNameFull = classNameFull2;
                        pkg = pkg2;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        classNameFull = classNameFull2;
                        sb.append(fieldInfo2.name);
                        sb.append("_asm_fieldType");
                        pkg = pkg2;
                        new FieldWriter(cw2, 1, sb.toString(), "Ljava/lang/reflect/Type;").visitEnd();
                        if (List.class.isAssignableFrom(fieldInfo2.fieldClass)) {
                            new FieldWriter(cw2, 1, fieldInfo2.name + "_asm_list_item_ser_", ObjectSerializer_desc).visitEnd();
                        }
                        new FieldWriter(cw2, 1, fieldInfo2.name + "_asm_ser_", ObjectSerializer_desc).visitEnd();
                    }
                } else {
                    i2 = length;
                    classNameFull = classNameFull2;
                    pkg = pkg2;
                }
                i3++;
                length = i2;
                classNameFull2 = classNameFull;
                pkg2 = pkg;
            }
            String classNameFull3 = classNameFull2;
            Package pkg3 = pkg2;
            MethodVisitor methodWriter = new MethodWriter(cw2, 1, "<init>", "(" + ASMUtils.desc(SerializeBeanInfo.class) + ")V", null, null);
            int i4 = 25;
            methodWriter.visitVarInsn(25, 0);
            methodWriter.visitVarInsn(25, 1);
            methodWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, JavaBeanSerializer, "<init>", "(" + ASMUtils.desc(SerializeBeanInfo.class) + ")V");
            int i5 = 0;
            while (i5 < getters.length) {
                FieldInfo fieldInfo3 = getters[i5];
                if (fieldInfo3.fieldClass.isPrimitive()) {
                    cw = cw2;
                } else if (fieldInfo3.fieldClass == String.class) {
                    cw = cw2;
                } else {
                    methodWriter.visitVarInsn(i4, 0);
                    if (fieldInfo3.method != null) {
                        methodWriter.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo3.declaringClass)));
                        methodWriter.visitLdcInsn(fieldInfo3.method.getName());
                        cw = cw2;
                        methodWriter.visitMethodInsn(Opcodes.INVOKESTATIC, ASMUtils.type(ASMUtils.class), "getMethodType", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Type;");
                    } else {
                        cw = cw2;
                        methodWriter.visitVarInsn(25, 0);
                        methodWriter.visitLdcInsn(Integer.valueOf(i5));
                        methodWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, JavaBeanSerializer, "getFieldType", "(I)Ljava/lang/reflect/Type;");
                    }
                    methodWriter.visitFieldInsn(Opcodes.PUTFIELD, classNameType, fieldInfo3.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
                }
                i5++;
                cw2 = cw;
                i4 = 25;
            }
            ClassWriter cw3 = cw2;
            methodWriter.visitInsn(Opcodes.RETURN);
            methodWriter.visitMaxs(4, 4);
            methodWriter.visitEnd();
            boolean DisableCircularReferenceDetect = false;
            if (jsonType2 != null) {
                SerializerFeature[] serialzeFeatures = jsonType2.serialzeFeatures();
                int length2 = serialzeFeatures.length;
                int i6 = 0;
                while (true) {
                    if (i6 >= length2) {
                        break;
                    } else if (serialzeFeatures[i6] == SerializerFeature.DisableCircularReferenceDetect) {
                        DisableCircularReferenceDetect = true;
                        break;
                    } else {
                        i6++;
                    }
                }
            }
            MethodVisitor mw = methodWriter;
            int i7 = 0;
            while (i7 < 3) {
                boolean nonContext = DisableCircularReferenceDetect;
                boolean writeDirect = false;
                if (i7 == 0) {
                    writeDirect = true;
                    methodName2 = "write";
                } else if (i7 == 1) {
                    methodName2 = "writeNormal";
                } else {
                    nonContext = true;
                    methodName2 = "writeDirectNonContext";
                    writeDirect = true;
                }
                Context context = new Context(getters, beanInfo, classNameType, writeDirect, nonContext);
                MethodVisitor methodWriter2 = new MethodWriter(cw3, 1, methodName2, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V", null, new String[]{"java/io/IOException"});
                Label endIf_ = new Label();
                methodWriter2.visitVarInsn(25, 2);
                methodWriter2.visitJumpInsn(Opcodes.IFNONNULL, endIf_);
                methodWriter2.visitVarInsn(25, 1);
                methodWriter2.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeNull", "()V");
                methodWriter2.visitInsn(Opcodes.RETURN);
                methodWriter2.visitLabel(endIf_);
                methodWriter2.visitVarInsn(25, 1);
                methodWriter2.visitFieldInsn(Opcodes.GETFIELD, JSONSerializer, "out", SerializeWriter_desc);
                methodWriter2.visitVarInsn(58, context.var("out"));
                if (nativeSorted || context.writeDirect) {
                    classNameType2 = classNameType;
                } else if (jsonType2 == null || jsonType2.alphabetic()) {
                    Label _else = new Label();
                    methodWriter2.visitVarInsn(25, context.var("out"));
                    methodWriter2.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isSortField", "()Z");
                    methodWriter2.visitJumpInsn(Opcodes.IFNE, _else);
                    methodWriter2.visitVarInsn(25, 0);
                    methodWriter2.visitVarInsn(25, 1);
                    methodWriter2.visitVarInsn(25, 2);
                    methodWriter2.visitVarInsn(25, 3);
                    methodWriter2.visitVarInsn(25, 4);
                    methodWriter2.visitVarInsn(21, 5);
                    classNameType2 = classNameType;
                    methodWriter2.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType2, "writeUnsorted", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                    methodWriter2.visitInsn(Opcodes.RETURN);
                    methodWriter2.visitLabel(_else);
                } else {
                    classNameType2 = classNameType;
                }
                if (!context.writeDirect || nonContext) {
                    jsonType = jsonType2;
                    className = className2;
                } else {
                    Label _direct = new Label();
                    Label _directElse = new Label();
                    methodWriter2.visitVarInsn(25, 0);
                    methodWriter2.visitVarInsn(25, 1);
                    String str = JavaBeanSerializer;
                    jsonType = jsonType2;
                    StringBuilder sb2 = new StringBuilder();
                    className = className2;
                    sb2.append("(L");
                    sb2.append(JSONSerializer);
                    sb2.append(";)Z");
                    methodWriter2.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "writeDirect", sb2.toString());
                    methodWriter2.visitJumpInsn(Opcodes.IFNE, _directElse);
                    methodWriter2.visitVarInsn(25, 0);
                    methodWriter2.visitVarInsn(25, 1);
                    methodWriter2.visitVarInsn(25, 2);
                    methodWriter2.visitVarInsn(25, 3);
                    methodWriter2.visitVarInsn(25, 4);
                    methodWriter2.visitVarInsn(21, 5);
                    methodWriter2.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType2, "writeNormal", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                    methodWriter2.visitInsn(Opcodes.RETURN);
                    methodWriter2.visitLabel(_directElse);
                    methodWriter2.visitVarInsn(25, context.var("out"));
                    methodWriter2.visitLdcInsn(Integer.valueOf(SerializerFeature.DisableCircularReferenceDetect.mask));
                    methodWriter2.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
                    methodWriter2.visitJumpInsn(Opcodes.IFEQ, _direct);
                    methodWriter2.visitVarInsn(25, 0);
                    methodWriter2.visitVarInsn(25, 1);
                    methodWriter2.visitVarInsn(25, 2);
                    methodWriter2.visitVarInsn(25, 3);
                    methodWriter2.visitVarInsn(25, 4);
                    methodWriter2.visitVarInsn(21, 5);
                    methodWriter2.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType2, "writeDirectNonContext", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                    methodWriter2.visitInsn(Opcodes.RETURN);
                    methodWriter2.visitLabel(_direct);
                }
                methodWriter2.visitVarInsn(25, 2);
                methodWriter2.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(clazz));
                methodWriter2.visitVarInsn(58, context.var("entity"));
                generateWriteMethod(clazz, methodWriter2, getters, context);
                methodWriter2.visitInsn(Opcodes.RETURN);
                methodWriter2.visitMaxs(7, context.variantIndex + 2);
                methodWriter2.visitEnd();
                i7++;
                mw = methodWriter2;
                classNameType = classNameType2;
                pkg3 = pkg3;
                cw3 = cw3;
                classNameFull3 = classNameFull3;
                jsonType2 = jsonType;
                className2 = className;
            }
            if (!nativeSorted) {
                i = 180;
                Context context2 = new Context(getters, beanInfo, classNameType, false, DisableCircularReferenceDetect);
                MethodVisitor mw2 = new MethodWriter(cw3, 1, "writeUnsorted", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V", null, new String[]{"java/io/IOException"});
                mw2.visitVarInsn(25, 1);
                mw2.visitFieldInsn(Opcodes.GETFIELD, JSONSerializer, "out", SerializeWriter_desc);
                mw2.visitVarInsn(58, context2.var("out"));
                mw2.visitVarInsn(25, 2);
                mw2.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(clazz));
                mw2.visitVarInsn(58, context2.var("entity"));
                generateWriteMethod(clazz, mw2, unsortedGetters, context2);
                mw2.visitInsn(Opcodes.RETURN);
                mw2.visitMaxs(7, context2.variantIndex + 2);
                mw2.visitEnd();
            } else {
                i = 180;
            }
            for (int i8 = 0; i8 < 3; i8++) {
                boolean nonContext2 = DisableCircularReferenceDetect;
                boolean writeDirect2 = false;
                if (i8 == 0) {
                    methodName = "writeAsArray";
                    writeDirect2 = true;
                } else if (i8 == 1) {
                    methodName = "writeAsArrayNormal";
                } else {
                    writeDirect2 = true;
                    nonContext2 = true;
                    methodName = "writeAsArrayNonContext";
                }
                Context context3 = new Context(getters, beanInfo, classNameType, writeDirect2, nonContext2);
                MethodVisitor mw3 = new MethodWriter(cw3, 1, methodName, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V", null, new String[]{"java/io/IOException"});
                mw3.visitVarInsn(25, 1);
                mw3.visitFieldInsn(i, JSONSerializer, "out", SerializeWriter_desc);
                mw3.visitVarInsn(58, context3.var("out"));
                mw3.visitVarInsn(25, 2);
                mw3.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(clazz));
                mw3.visitVarInsn(58, context3.var("entity"));
                generateWriteAsArray(clazz, mw3, getters, context3);
                mw3.visitInsn(Opcodes.RETURN);
                mw3.visitMaxs(7, context3.variantIndex + 2);
                mw3.visitEnd();
            }
            byte[] code = cw3.toByteArray();
            return (JavaBeanSerializer) this.classLoader.defineClassPublic(classNameFull3, code, 0, code.length).getConstructor(SerializeBeanInfo.class).newInstance(beanInfo);
        }
        throw new JSONException("unsupportd class " + clazz.getName());
    }

    /* JADX WARNING: Removed duplicated region for block: B:78:0x0574  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x057a  */
    private void generateWriteAsArray(Class<?> cls, MethodVisitor mw, FieldInfo[] getters, Context context) throws Exception {
        int i;
        int size;
        Label nonPropertyFilters_;
        int i2;
        ASMSerializerFactory aSMSerializerFactory;
        int i3;
        int i4;
        char seperator;
        java.lang.reflect.Type elementType;
        Class<?> elementClass;
        char seperator2;
        Label nullEnd_;
        Label nullEnd_2;
        ASMSerializerFactory aSMSerializerFactory2 = this;
        FieldInfo[] fieldInfoArr = getters;
        Label nonPropertyFilters_2 = new Label();
        int i5 = 25;
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 0);
        int i6 = Opcodes.INVOKEVIRTUAL;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "hasPropertyFilters", "(" + SerializeFilterable_desc + ")Z");
        mw.visitJumpInsn(Opcodes.IFNE, nonPropertyFilters_2);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, 3);
        mw.visitVarInsn(25, 4);
        mw.visitVarInsn(21, 5);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, JavaBeanSerializer, "writeNoneASM", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
        mw.visitInsn(Opcodes.RETURN);
        mw.visitLabel(nonPropertyFilters_2);
        mw.visitVarInsn(25, context.var("out"));
        int i7 = 16;
        mw.visitVarInsn(16, 91);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
        int size2 = fieldInfoArr.length;
        char c = ']';
        if (size2 == 0) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 93);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
            return;
        }
        int i8 = 0;
        while (i8 < size2) {
            char seperator3 = i8 == size2 + -1 ? c : ',';
            FieldInfo fieldInfo = fieldInfoArr[i8];
            Class<?> fieldClass = fieldInfo.fieldClass;
            mw.visitLdcInsn(fieldInfo.name);
            mw.visitVarInsn(58, Context.fieldName);
            if (fieldClass == Byte.TYPE || fieldClass == Short.TYPE) {
                aSMSerializerFactory = aSMSerializerFactory2;
                nonPropertyFilters_ = nonPropertyFilters_2;
                size = size2;
                i = i8;
                seperator = seperator3;
            } else if (fieldClass == Integer.TYPE) {
                aSMSerializerFactory = aSMSerializerFactory2;
                nonPropertyFilters_ = nonPropertyFilters_2;
                size = size2;
                i = i8;
                seperator = seperator3;
            } else {
                if (fieldClass == Long.TYPE) {
                    mw.visitVarInsn(i5, context.var("out"));
                    mw.visitInsn(89);
                    aSMSerializerFactory2._get(mw, context, fieldInfo);
                    mw.visitMethodInsn(i6, SerializeWriter, "writeLong", "(J)V");
                    mw.visitVarInsn(i7, seperator3);
                    mw.visitMethodInsn(i6, SerializeWriter, "write", "(I)V");
                } else if (fieldClass == Float.TYPE) {
                    mw.visitVarInsn(i5, context.var("out"));
                    mw.visitInsn(89);
                    aSMSerializerFactory2._get(mw, context, fieldInfo);
                    mw.visitInsn(4);
                    mw.visitMethodInsn(i6, SerializeWriter, "writeFloat", "(FZ)V");
                    mw.visitVarInsn(i7, seperator3);
                    mw.visitMethodInsn(i6, SerializeWriter, "write", "(I)V");
                } else if (fieldClass == Double.TYPE) {
                    mw.visitVarInsn(i5, context.var("out"));
                    mw.visitInsn(89);
                    aSMSerializerFactory2._get(mw, context, fieldInfo);
                    mw.visitInsn(4);
                    mw.visitMethodInsn(i6, SerializeWriter, "writeDouble", "(DZ)V");
                    mw.visitVarInsn(i7, seperator3);
                    mw.visitMethodInsn(i6, SerializeWriter, "write", "(I)V");
                } else if (fieldClass == Boolean.TYPE) {
                    mw.visitVarInsn(i5, context.var("out"));
                    mw.visitInsn(89);
                    aSMSerializerFactory2._get(mw, context, fieldInfo);
                    mw.visitMethodInsn(i6, SerializeWriter, "write", "(Z)V");
                    mw.visitVarInsn(i7, seperator3);
                    mw.visitMethodInsn(i6, SerializeWriter, "write", "(I)V");
                } else {
                    if (fieldClass == Character.TYPE) {
                        mw.visitVarInsn(i5, context.var("out"));
                        aSMSerializerFactory2._get(mw, context, fieldInfo);
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "toString", "(C)Ljava/lang/String;");
                        mw.visitVarInsn(i7, seperator3);
                        mw.visitMethodInsn(i6, SerializeWriter, "writeString", "(Ljava/lang/String;C)V");
                    } else if (fieldClass == String.class) {
                        mw.visitVarInsn(25, context.var("out"));
                        aSMSerializerFactory2._get(mw, context, fieldInfo);
                        mw.visitVarInsn(i7, seperator3);
                        mw.visitMethodInsn(i6, SerializeWriter, "writeString", "(Ljava/lang/String;C)V");
                    } else if (fieldClass.isEnum()) {
                        mw.visitVarInsn(25, context.var("out"));
                        mw.visitInsn(89);
                        aSMSerializerFactory2._get(mw, context, fieldInfo);
                        mw.visitMethodInsn(i6, SerializeWriter, "writeEnum", "(Ljava/lang/Enum;)V");
                        mw.visitVarInsn(i7, seperator3);
                        mw.visitMethodInsn(i6, SerializeWriter, "write", "(I)V");
                    } else if (List.class.isAssignableFrom(fieldClass)) {
                        java.lang.reflect.Type fieldType = fieldInfo.fieldType;
                        if (fieldType instanceof Class) {
                            elementType = Object.class;
                        } else {
                            elementType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                        }
                        Class<?> cls2 = null;
                        if (elementType instanceof Class) {
                            elementClass = (Class) elementType;
                            if (elementClass == Object.class) {
                                cls2 = null;
                            }
                            aSMSerializerFactory2._get(mw, context, fieldInfo);
                            mw.visitTypeInsn(Opcodes.CHECKCAST, "java/util/List");
                            mw.visitVarInsn(58, context.var("list"));
                            if (elementClass == String.class || !context.writeDirect) {
                                nullEnd_ = new Label();
                                Label nullElse_ = new Label();
                                mw.visitVarInsn(25, context.var("list"));
                                mw.visitJumpInsn(Opcodes.IFNONNULL, nullElse_);
                                mw.visitVarInsn(25, context.var("out"));
                                nonPropertyFilters_ = nonPropertyFilters_2;
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeNull", "()V");
                                mw.visitJumpInsn(Opcodes.GOTO, nullEnd_);
                                mw.visitLabel(nullElse_);
                                mw.visitVarInsn(25, context.var("list"));
                                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "size", "()I");
                                mw.visitVarInsn(54, context.var("size"));
                                mw.visitVarInsn(25, context.var("out"));
                                mw.visitVarInsn(16, 91);
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                                Label for_ = new Label();
                                Label forFirst_ = new Label();
                                Label forEnd_ = new Label();
                                mw.visitInsn(3);
                                mw.visitVarInsn(54, context.var("i"));
                                mw.visitLabel(for_);
                                mw.visitVarInsn(21, context.var("i"));
                                mw.visitVarInsn(21, context.var("size"));
                                mw.visitJumpInsn(Opcodes.IF_ICMPGE, forEnd_);
                                mw.visitVarInsn(21, context.var("i"));
                                mw.visitJumpInsn(Opcodes.IFEQ, forFirst_);
                                mw.visitVarInsn(25, context.var("out"));
                                mw.visitVarInsn(16, 44);
                                size = size2;
                                i = i8;
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                                mw.visitLabel(forFirst_);
                                mw.visitVarInsn(25, context.var("list"));
                                mw.visitVarInsn(21, context.var("i"));
                                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;");
                                mw.visitVarInsn(58, context.var("list_item"));
                                Label forItemNullEnd_ = new Label();
                                Label forItemNullElse_ = new Label();
                                mw.visitVarInsn(25, context.var("list_item"));
                                mw.visitJumpInsn(Opcodes.IFNONNULL, forItemNullElse_);
                                mw.visitVarInsn(25, context.var("out"));
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeNull", "()V");
                                mw.visitJumpInsn(Opcodes.GOTO, forItemNullEnd_);
                                mw.visitLabel(forItemNullElse_);
                                Label forItemClassIfEnd_ = new Label();
                                Label forItemClassIfElse_ = new Label();
                                if (elementClass != null || !Modifier.isPublic(elementClass.getModifiers())) {
                                    nullEnd_2 = nullEnd_;
                                    seperator2 = seperator3;
                                } else {
                                    mw.visitVarInsn(25, context.var("list_item"));
                                    seperator2 = seperator3;
                                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                                    mw.visitJumpInsn(Opcodes.IF_ACMPNE, forItemClassIfElse_);
                                    aSMSerializerFactory2._getListFieldItemSer(context, mw, fieldInfo, elementClass);
                                    mw.visitVarInsn(58, context.var("list_item_desc"));
                                    Label instanceOfElse_ = new Label();
                                    Label instanceOfEnd_ = new Label();
                                    if (context.writeDirect) {
                                        mw.visitVarInsn(25, context.var("list_item_desc"));
                                        mw.visitTypeInsn(Opcodes.INSTANCEOF, JavaBeanSerializer);
                                        mw.visitJumpInsn(Opcodes.IFEQ, instanceOfElse_);
                                        mw.visitVarInsn(25, context.var("list_item_desc"));
                                        mw.visitTypeInsn(Opcodes.CHECKCAST, JavaBeanSerializer);
                                        mw.visitVarInsn(25, 1);
                                        mw.visitVarInsn(25, context.var("list_item"));
                                        if (context.nonContext) {
                                            mw.visitInsn(1);
                                            nullEnd_2 = nullEnd_;
                                        } else {
                                            mw.visitVarInsn(21, context.var("i"));
                                            nullEnd_2 = nullEnd_;
                                            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                                        }
                                        mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                                        mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JavaBeanSerializer, "writeAsArrayNonContext", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                                        mw.visitJumpInsn(Opcodes.GOTO, instanceOfEnd_);
                                        mw.visitLabel(instanceOfElse_);
                                    } else {
                                        nullEnd_2 = nullEnd_;
                                    }
                                    mw.visitVarInsn(25, context.var("list_item_desc"));
                                    mw.visitVarInsn(25, 1);
                                    mw.visitVarInsn(25, context.var("list_item"));
                                    if (context.nonContext) {
                                        mw.visitInsn(1);
                                    } else {
                                        mw.visitVarInsn(21, context.var("i"));
                                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                                    }
                                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                                    mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, ObjectSerializer, "write", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                                    mw.visitLabel(instanceOfEnd_);
                                    mw.visitJumpInsn(Opcodes.GOTO, forItemClassIfEnd_);
                                }
                                mw.visitLabel(forItemClassIfElse_);
                                mw.visitVarInsn(25, 1);
                                mw.visitVarInsn(25, context.var("list_item"));
                                if (!context.nonContext) {
                                    mw.visitInsn(1);
                                } else {
                                    mw.visitVarInsn(21, context.var("i"));
                                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                                }
                                if (elementClass != null || !Modifier.isPublic(elementClass.getModifiers())) {
                                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                                } else {
                                    mw.visitLdcInsn(Type.getType(ASMUtils.desc((Class) elementType)));
                                    mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                                }
                                mw.visitLabel(forItemClassIfEnd_);
                                mw.visitLabel(forItemNullEnd_);
                                mw.visitIincInsn(context.var("i"), 1);
                                mw.visitJumpInsn(Opcodes.GOTO, for_);
                                mw.visitLabel(forEnd_);
                                mw.visitVarInsn(25, context.var("out"));
                                mw.visitVarInsn(16, 93);
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                                mw.visitLabel(nullEnd_2);
                            } else {
                                mw.visitVarInsn(25, context.var("out"));
                                mw.visitVarInsn(25, context.var("list"));
                                mw.visitMethodInsn(i6, SerializeWriter, "write", "(Ljava/util/List;)V");
                                nonPropertyFilters_ = nonPropertyFilters_2;
                                size = size2;
                                i = i8;
                                seperator2 = seperator3;
                            }
                            mw.visitVarInsn(25, context.var("out"));
                            mw.visitVarInsn(16, seperator2);
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                            i4 = 16;
                            i3 = 25;
                            aSMSerializerFactory = this;
                            i2 = Opcodes.INVOKEVIRTUAL;
                            i8 = i + 1;
                            i7 = i4;
                            aSMSerializerFactory2 = aSMSerializerFactory;
                            i6 = i2;
                            size2 = size;
                            fieldInfoArr = getters;
                            c = ']';
                            i5 = i3;
                            nonPropertyFilters_2 = nonPropertyFilters_;
                        }
                        elementClass = cls2;
                        aSMSerializerFactory2._get(mw, context, fieldInfo);
                        mw.visitTypeInsn(Opcodes.CHECKCAST, "java/util/List");
                        mw.visitVarInsn(58, context.var("list"));
                        if (elementClass == String.class) {
                        }
                        nullEnd_ = new Label();
                        Label nullElse_2 = new Label();
                        mw.visitVarInsn(25, context.var("list"));
                        mw.visitJumpInsn(Opcodes.IFNONNULL, nullElse_2);
                        mw.visitVarInsn(25, context.var("out"));
                        nonPropertyFilters_ = nonPropertyFilters_2;
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeNull", "()V");
                        mw.visitJumpInsn(Opcodes.GOTO, nullEnd_);
                        mw.visitLabel(nullElse_2);
                        mw.visitVarInsn(25, context.var("list"));
                        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "size", "()I");
                        mw.visitVarInsn(54, context.var("size"));
                        mw.visitVarInsn(25, context.var("out"));
                        mw.visitVarInsn(16, 91);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                        Label for_2 = new Label();
                        Label forFirst_2 = new Label();
                        Label forEnd_2 = new Label();
                        mw.visitInsn(3);
                        mw.visitVarInsn(54, context.var("i"));
                        mw.visitLabel(for_2);
                        mw.visitVarInsn(21, context.var("i"));
                        mw.visitVarInsn(21, context.var("size"));
                        mw.visitJumpInsn(Opcodes.IF_ICMPGE, forEnd_2);
                        mw.visitVarInsn(21, context.var("i"));
                        mw.visitJumpInsn(Opcodes.IFEQ, forFirst_2);
                        mw.visitVarInsn(25, context.var("out"));
                        mw.visitVarInsn(16, 44);
                        size = size2;
                        i = i8;
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                        mw.visitLabel(forFirst_2);
                        mw.visitVarInsn(25, context.var("list"));
                        mw.visitVarInsn(21, context.var("i"));
                        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;");
                        mw.visitVarInsn(58, context.var("list_item"));
                        Label forItemNullEnd_2 = new Label();
                        Label forItemNullElse_2 = new Label();
                        mw.visitVarInsn(25, context.var("list_item"));
                        mw.visitJumpInsn(Opcodes.IFNONNULL, forItemNullElse_2);
                        mw.visitVarInsn(25, context.var("out"));
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeNull", "()V");
                        mw.visitJumpInsn(Opcodes.GOTO, forItemNullEnd_2);
                        mw.visitLabel(forItemNullElse_2);
                        Label forItemClassIfEnd_2 = new Label();
                        Label forItemClassIfElse_2 = new Label();
                        if (elementClass != null) {
                        }
                        nullEnd_2 = nullEnd_;
                        seperator2 = seperator3;
                        mw.visitLabel(forItemClassIfElse_2);
                        mw.visitVarInsn(25, 1);
                        mw.visitVarInsn(25, context.var("list_item"));
                        if (!context.nonContext) {
                        }
                        if (elementClass != null) {
                        }
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                        mw.visitLabel(forItemClassIfEnd_2);
                        mw.visitLabel(forItemNullEnd_2);
                        mw.visitIincInsn(context.var("i"), 1);
                        mw.visitJumpInsn(Opcodes.GOTO, for_2);
                        mw.visitLabel(forEnd_2);
                        mw.visitVarInsn(25, context.var("out"));
                        mw.visitVarInsn(16, 93);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                        mw.visitLabel(nullEnd_2);
                        mw.visitVarInsn(25, context.var("out"));
                        mw.visitVarInsn(16, seperator2);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                        i4 = 16;
                        i3 = 25;
                        aSMSerializerFactory = this;
                        i2 = Opcodes.INVOKEVIRTUAL;
                        i8 = i + 1;
                        i7 = i4;
                        aSMSerializerFactory2 = aSMSerializerFactory;
                        i6 = i2;
                        size2 = size;
                        fieldInfoArr = getters;
                        c = ']';
                        i5 = i3;
                        nonPropertyFilters_2 = nonPropertyFilters_;
                    } else {
                        nonPropertyFilters_ = nonPropertyFilters_2;
                        size = size2;
                        i = i8;
                        Label notNullEnd_ = new Label();
                        Label notNullElse_ = new Label();
                        aSMSerializerFactory = this;
                        aSMSerializerFactory._get(mw, context, fieldInfo);
                        mw.visitInsn(89);
                        mw.visitVarInsn(58, context.var("field_" + fieldInfo.fieldClass.getName()));
                        mw.visitJumpInsn(Opcodes.IFNONNULL, notNullElse_);
                        mw.visitVarInsn(25, context.var("out"));
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeNull", "()V");
                        mw.visitJumpInsn(Opcodes.GOTO, notNullEnd_);
                        mw.visitLabel(notNullElse_);
                        Label classIfEnd_ = new Label();
                        Label classIfElse_ = new Label();
                        mw.visitVarInsn(25, context.var("field_" + fieldInfo.fieldClass.getName()));
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                        mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
                        mw.visitJumpInsn(Opcodes.IF_ACMPNE, classIfElse_);
                        aSMSerializerFactory._getFieldSer(context, mw, fieldInfo);
                        mw.visitVarInsn(58, context.var("fied_ser"));
                        Label instanceOfElse_2 = new Label();
                        Label instanceOfEnd_2 = new Label();
                        if (context.writeDirect && Modifier.isPublic(fieldClass.getModifiers())) {
                            mw.visitVarInsn(25, context.var("fied_ser"));
                            mw.visitTypeInsn(Opcodes.INSTANCEOF, JavaBeanSerializer);
                            mw.visitJumpInsn(Opcodes.IFEQ, instanceOfElse_2);
                            mw.visitVarInsn(25, context.var("fied_ser"));
                            mw.visitTypeInsn(Opcodes.CHECKCAST, JavaBeanSerializer);
                            mw.visitVarInsn(25, 1);
                            mw.visitVarInsn(25, context.var("field_" + fieldInfo.fieldClass.getName()));
                            mw.visitVarInsn(25, Context.fieldName);
                            mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
                            mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JavaBeanSerializer, "writeAsArrayNonContext", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                            mw.visitJumpInsn(Opcodes.GOTO, instanceOfEnd_2);
                            mw.visitLabel(instanceOfElse_2);
                        }
                        mw.visitVarInsn(25, context.var("fied_ser"));
                        mw.visitVarInsn(25, 1);
                        mw.visitVarInsn(25, context.var("field_" + fieldInfo.fieldClass.getName()));
                        mw.visitVarInsn(25, Context.fieldName);
                        mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
                        mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, ObjectSerializer, "write", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                        mw.visitLabel(instanceOfEnd_2);
                        mw.visitJumpInsn(Opcodes.GOTO, classIfEnd_);
                        mw.visitLabel(classIfElse_);
                        String format = fieldInfo.getFormat();
                        mw.visitVarInsn(25, 1);
                        mw.visitVarInsn(25, context.var("field_" + fieldInfo.fieldClass.getName()));
                        if (format != null) {
                            mw.visitLdcInsn(format);
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFormat", "(Ljava/lang/Object;Ljava/lang/String;)V");
                        } else {
                            mw.visitVarInsn(25, Context.fieldName);
                            if (!(fieldInfo.fieldType instanceof Class) || !((Class) fieldInfo.fieldType).isPrimitive()) {
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
                                mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                            } else {
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                            }
                        }
                        mw.visitLabel(classIfEnd_);
                        mw.visitLabel(notNullEnd_);
                        mw.visitVarInsn(25, context.var("out"));
                        mw.visitVarInsn(16, seperator3);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
                        i2 = 182;
                        i4 = 16;
                        i3 = 25;
                        i8 = i + 1;
                        i7 = i4;
                        aSMSerializerFactory2 = aSMSerializerFactory;
                        i6 = i2;
                        size2 = size;
                        fieldInfoArr = getters;
                        c = ']';
                        i5 = i3;
                        nonPropertyFilters_2 = nonPropertyFilters_;
                    }
                    aSMSerializerFactory = aSMSerializerFactory2;
                    nonPropertyFilters_ = nonPropertyFilters_2;
                    i4 = i7;
                    i2 = i6;
                    size = size2;
                    i = i8;
                    i3 = 25;
                    i8 = i + 1;
                    i7 = i4;
                    aSMSerializerFactory2 = aSMSerializerFactory;
                    i6 = i2;
                    size2 = size;
                    fieldInfoArr = getters;
                    c = ']';
                    i5 = i3;
                    nonPropertyFilters_2 = nonPropertyFilters_;
                }
                nonPropertyFilters_ = nonPropertyFilters_2;
                i3 = i5;
                i4 = i7;
                i2 = i6;
                size = size2;
                i = i8;
                aSMSerializerFactory = aSMSerializerFactory2;
                i8 = i + 1;
                i7 = i4;
                aSMSerializerFactory2 = aSMSerializerFactory;
                i6 = i2;
                size2 = size;
                fieldInfoArr = getters;
                c = ']';
                i5 = i3;
                nonPropertyFilters_2 = nonPropertyFilters_;
            }
            i3 = 25;
            mw.visitVarInsn(25, context.var("out"));
            mw.visitInsn(89);
            aSMSerializerFactory._get(mw, context, fieldInfo);
            String str = SerializeWriter;
            i2 = Opcodes.INVOKEVIRTUAL;
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "writeInt", "(I)V");
            i4 = 16;
            mw.visitVarInsn(16, seperator);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
            i8 = i + 1;
            i7 = i4;
            aSMSerializerFactory2 = aSMSerializerFactory;
            i6 = i2;
            size2 = size;
            fieldInfoArr = getters;
            c = ']';
            i5 = i3;
            nonPropertyFilters_2 = nonPropertyFilters_;
        }
    }

    private void generateWriteMethod(Class<?> clazz, MethodVisitor mw, FieldInfo[] getters, Context context) throws Exception {
        String writeAsArrayMethodName;
        int i;
        Class<?> propertyClass;
        FieldInfo property;
        FieldInfo[] fieldInfoArr = getters;
        Label end = new Label();
        int size = fieldInfoArr.length;
        if (!context.writeDirect) {
            Label endSupper_ = new Label();
            Label supper_ = new Label();
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(Integer.valueOf(SerializerFeature.PrettyFormat.mask));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(Opcodes.IFNE, supper_);
            boolean hasMethod = false;
            for (FieldInfo getter : fieldInfoArr) {
                if (getter.method != null) {
                    hasMethod = true;
                }
            }
            if (hasMethod) {
                mw.visitVarInsn(25, context.var("out"));
                mw.visitLdcInsn(Integer.valueOf(SerializerFeature.IgnoreErrorGetter.mask));
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
                mw.visitJumpInsn(Opcodes.IFEQ, endSupper_);
            } else {
                mw.visitJumpInsn(Opcodes.GOTO, endSupper_);
            }
            mw.visitLabel(supper_);
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(21, 5);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, JavaBeanSerializer, "write", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitInsn(Opcodes.RETURN);
            mw.visitLabel(endSupper_);
        }
        if (!context.nonContext) {
            Label endRef_ = new Label();
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(21, 5);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JavaBeanSerializer, "writeReference", "(L" + JSONSerializer + ";Ljava/lang/Object;I)Z");
            mw.visitJumpInsn(Opcodes.IFEQ, endRef_);
            mw.visitInsn(Opcodes.RETURN);
            mw.visitLabel(endRef_);
        }
        if (!context.writeDirect) {
            writeAsArrayMethodName = "writeAsArrayNormal";
        } else if (context.nonContext) {
            writeAsArrayMethodName = "writeAsArrayNonContext";
        } else {
            writeAsArrayMethodName = "writeAsArray";
        }
        if ((context.beanInfo.features & SerializerFeature.BeanToArray.mask) == 0) {
            Label endWriteAsArray_ = new Label();
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(Integer.valueOf(SerializerFeature.BeanToArray.mask));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(Opcodes.IFEQ, endWriteAsArray_);
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(21, 5);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className, writeAsArrayMethodName, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitInsn(Opcodes.RETURN);
            mw.visitLabel(endWriteAsArray_);
        } else {
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(21, 5);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, context.className, writeAsArrayMethodName, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitInsn(Opcodes.RETURN);
        }
        if (!context.nonContext) {
            mw.visitVarInsn(25, 1);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "getContext", "()" + SerialContext_desc);
            mw.visitVarInsn(58, context.var("parent"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("parent"));
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitLdcInsn(Integer.valueOf(context.beanInfo.features));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "setContext", "(" + SerialContext_desc + "Ljava/lang/Object;Ljava/lang/Object;I)V");
        }
        boolean writeClasName = (context.beanInfo.features & SerializerFeature.WriteClassName.mask) != 0;
        char c = '{';
        if (writeClasName || !context.writeDirect) {
            Label end_ = new Label();
            Label else_ = new Label();
            Label writeClass_ = new Label();
            if (!writeClasName) {
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, 4);
                mw.visitVarInsn(25, 2);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "isWriteClassName", "(Ljava/lang/reflect/Type;Ljava/lang/Object;)Z");
                mw.visitJumpInsn(Opcodes.IFEQ, else_);
            }
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(25, 2);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mw.visitJumpInsn(Opcodes.IF_ACMPEQ, else_);
            mw.visitLabel(writeClass_);
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 123);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            if (context.beanInfo.typeKey != null) {
                mw.visitLdcInsn(context.beanInfo.typeKey);
            } else {
                mw.visitInsn(1);
            }
            mw.visitVarInsn(25, 2);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JavaBeanSerializer, "writeClassName", "(L" + JSONSerializer + ";Ljava/lang/String;Ljava/lang/Object;)V");
            mw.visitVarInsn(16, 44);
            mw.visitJumpInsn(Opcodes.GOTO, end_);
            mw.visitLabel(else_);
            c = '{';
            mw.visitVarInsn(16, 123);
            mw.visitLabel(end_);
        } else {
            mw.visitVarInsn(16, 123);
        }
        mw.visitVarInsn(54, context.var("seperator"));
        if (!context.writeDirect) {
            _before(mw, context);
        }
        if (!context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isNotWriteDefaultValue", "()Z");
            mw.visitVarInsn(54, context.var("notWriteDefaultValue"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 0);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "checkValue", "(" + SerializeFilterable_desc + ")Z");
            mw.visitVarInsn(54, context.var("checkValue"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 0);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "hasNameFilters", "(" + SerializeFilterable_desc + ")Z");
            mw.visitVarInsn(54, context.var("hasNameFilters"));
        }
        int i2 = 0;
        while (i2 < size) {
            FieldInfo property2 = fieldInfoArr[i2];
            Class<?> propertyClass2 = property2.fieldClass;
            mw.visitLdcInsn(property2.name);
            mw.visitVarInsn(58, Context.fieldName);
            if (propertyClass2 == Byte.TYPE || propertyClass2 == Short.TYPE) {
                propertyClass = propertyClass2;
                property = property2;
                i = i2;
            } else if (propertyClass2 == Integer.TYPE) {
                propertyClass = propertyClass2;
                property = property2;
                i = i2;
            } else {
                if (propertyClass2 == Long.TYPE) {
                    _long(clazz, mw, property2, context);
                } else if (propertyClass2 == Float.TYPE) {
                    _float(clazz, mw, property2, context);
                } else if (propertyClass2 == Double.TYPE) {
                    _double(clazz, mw, property2, context);
                } else {
                    if (propertyClass2 == Boolean.TYPE) {
                        i = i2;
                        _int(clazz, mw, property2, context, context.var("boolean"), 'Z');
                    } else {
                        i = i2;
                        if (propertyClass2 == Character.TYPE) {
                            _int(clazz, mw, property2, context, context.var("char"), 'C');
                        } else if (propertyClass2 == String.class) {
                            _string(clazz, mw, property2, context);
                        } else if (propertyClass2 == BigDecimal.class) {
                            _decimal(clazz, mw, property2, context);
                        } else if (List.class.isAssignableFrom(propertyClass2)) {
                            _list(clazz, mw, property2, context);
                        } else if (propertyClass2.isEnum()) {
                            _enum(clazz, mw, property2, context);
                        } else {
                            _object(clazz, mw, property2, context);
                        }
                    }
                    i2 = i + 1;
                    c = '{';
                    fieldInfoArr = getters;
                }
                i = i2;
                i2 = i + 1;
                c = '{';
                fieldInfoArr = getters;
            }
            _int(clazz, mw, property, context, context.var(propertyClass.getName()), 'I');
            i2 = i + 1;
            c = '{';
            fieldInfoArr = getters;
        }
        if (!context.writeDirect) {
            _after(mw, context);
        }
        Label _else = new Label();
        Label _end_if = new Label();
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitIntInsn(16, 123);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, _else);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(16, 123);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
        mw.visitLabel(_else);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(16, 125);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
        mw.visitLabel(_end_if);
        mw.visitLabel(end);
        if (!context.nonContext) {
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("parent"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "setContext", "(" + SerialContext_desc + ")V");
        }
    }

    private void _object(Class<?> cls, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        _nameApply(mw, property, context, _end);
        _get(mw, context, property);
        mw.visitVarInsn(58, context.var("object"));
        _filters(mw, property, context, _end);
        _writeObject(mw, property, context, _end);
        mw.visitLabel(_end);
    }

    private void _enum(Class<?> cls, MethodVisitor mw, FieldInfo fieldInfo, Context context) {
        Label _not_null = new Label();
        Label _end_if = new Label();
        Label _end = new Label();
        _nameApply(mw, fieldInfo, context, _end);
        _get(mw, context, fieldInfo);
        mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Enum");
        mw.visitVarInsn(58, context.var("enum"));
        _filters(mw, fieldInfo, context, _end);
        mw.visitVarInsn(25, context.var("enum"));
        mw.visitJumpInsn(Opcodes.IFNONNULL, _not_null);
        _if_write_null(mw, fieldInfo, context);
        mw.visitJumpInsn(Opcodes.GOTO, _end_if);
        mw.visitLabel(_not_null);
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(21, context.var("seperator"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, context.var("enum"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Enum", "name", "()Ljava/lang/String;");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldValueStringWithDoubleQuote", "(CLjava/lang/String;Ljava/lang/String;)V");
        } else {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(21, context.var("seperator"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitInsn(3);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldName", "(Ljava/lang/String;Z)V");
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("enum"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
            mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
        }
        _seperator(mw, context);
        mw.visitLabel(_end_if);
        mw.visitLabel(_end);
    }

    private void _int(Class<?> cls, MethodVisitor mw, FieldInfo property, Context context, int var, char type) {
        Label end_ = new Label();
        _nameApply(mw, property, context, end_);
        _get(mw, context, property);
        mw.visitVarInsn(54, var);
        _filters(mw, property, context, end_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(21, var);
        String str = SerializeWriter;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "writeFieldValue", "(CLjava/lang/String;" + type + ")V");
        _seperator(mw, context);
        mw.visitLabel(end_);
    }

    private void _long(Class<?> cls, MethodVisitor mw, FieldInfo property, Context context) {
        Label end_ = new Label();
        _nameApply(mw, property, context, end_);
        _get(mw, context, property);
        mw.visitVarInsn(55, context.var("long", 2));
        _filters(mw, property, context, end_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(22, context.var("long", 2));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldValue", "(CLjava/lang/String;J)V");
        _seperator(mw, context);
        mw.visitLabel(end_);
    }

    private void _float(Class<?> cls, MethodVisitor mw, FieldInfo property, Context context) {
        Label end_ = new Label();
        _nameApply(mw, property, context, end_);
        _get(mw, context, property);
        mw.visitVarInsn(56, context.var("float"));
        _filters(mw, property, context, end_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(23, context.var("float"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldValue", "(CLjava/lang/String;F)V");
        _seperator(mw, context);
        mw.visitLabel(end_);
    }

    private void _double(Class<?> cls, MethodVisitor mw, FieldInfo property, Context context) {
        Label end_ = new Label();
        _nameApply(mw, property, context, end_);
        _get(mw, context, property);
        mw.visitVarInsn(57, context.var("double", 2));
        _filters(mw, property, context, end_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(24, context.var("double", 2));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldValue", "(CLjava/lang/String;D)V");
        _seperator(mw, context);
        mw.visitLabel(end_);
    }

    private void _get(MethodVisitor mw, Context context, FieldInfo fieldInfo) {
        Method method = fieldInfo.method;
        if (method != null) {
            mw.visitVarInsn(25, context.var("entity"));
            Class<?> declaringClass = method.getDeclaringClass();
            mw.visitMethodInsn(declaringClass.isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, ASMUtils.type(declaringClass), method.getName(), ASMUtils.desc(method));
            if (!method.getReturnType().equals(fieldInfo.fieldClass)) {
                mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldInfo.fieldClass));
                return;
            }
            return;
        }
        mw.visitVarInsn(25, context.var("entity"));
        Field field = fieldInfo.field;
        mw.visitFieldInsn(Opcodes.GETFIELD, ASMUtils.type(fieldInfo.declaringClass), field.getName(), ASMUtils.desc(field.getType()));
        if (!field.getType().equals(fieldInfo.fieldClass)) {
            mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldInfo.fieldClass));
        }
    }

    private void _decimal(Class<?> cls, MethodVisitor mw, FieldInfo property, Context context) {
        Label end_ = new Label();
        _nameApply(mw, property, context, end_);
        _get(mw, context, property);
        mw.visitVarInsn(58, context.var("decimal"));
        _filters(mw, property, context, end_);
        Label if_ = new Label();
        Label else_ = new Label();
        Label endIf_ = new Label();
        mw.visitLabel(if_);
        mw.visitVarInsn(25, context.var("decimal"));
        mw.visitJumpInsn(Opcodes.IFNONNULL, else_);
        _if_write_null(mw, property, context);
        mw.visitJumpInsn(Opcodes.GOTO, endIf_);
        mw.visitLabel(else_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(25, context.var("decimal"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldValue", "(CLjava/lang/String;Ljava/math/BigDecimal;)V");
        _seperator(mw, context);
        mw.visitJumpInsn(Opcodes.GOTO, endIf_);
        mw.visitLabel(endIf_);
        mw.visitLabel(end_);
    }

    private void _string(Class<?> cls, MethodVisitor mw, FieldInfo property, Context context) {
        Label end_ = new Label();
        if (property.name.equals(context.beanInfo.typeKey)) {
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(25, 2);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "isWriteClassName", "(Ljava/lang/reflect/Type;Ljava/lang/Object;)Z");
            mw.visitJumpInsn(Opcodes.IFNE, end_);
        }
        _nameApply(mw, property, context, end_);
        _get(mw, context, property);
        mw.visitVarInsn(58, context.var("string"));
        _filters(mw, property, context, end_);
        Label else_ = new Label();
        Label endIf_ = new Label();
        mw.visitVarInsn(25, context.var("string"));
        mw.visitJumpInsn(Opcodes.IFNONNULL, else_);
        _if_write_null(mw, property, context);
        mw.visitJumpInsn(Opcodes.GOTO, endIf_);
        mw.visitLabel(else_);
        if ("trim".equals(property.format)) {
            mw.visitVarInsn(25, context.var("string"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "trim", "()Ljava/lang/String;");
            mw.visitVarInsn(58, context.var("string"));
        }
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(21, context.var("seperator"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, context.var("string"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldValueStringWithDoubleQuoteCheck", "(CLjava/lang/String;Ljava/lang/String;)V");
        } else {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(21, context.var("seperator"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, context.var("string"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldValue", "(CLjava/lang/String;Ljava/lang/String;)V");
        }
        _seperator(mw, context);
        mw.visitLabel(endIf_);
        mw.visitLabel(end_);
    }

    private void _list(Class<?> cls, MethodVisitor mw, FieldInfo fieldInfo, Context context) {
        Label _end_if_3;
        Label endIf_;
        Label end_;
        int i;
        int i2;
        Label forEnd_;
        java.lang.reflect.Type elementType = TypeUtils.getCollectionItemType(fieldInfo.fieldType);
        Class<?> elementClass = null;
        if (elementType instanceof Class) {
            elementClass = (Class) elementType;
        }
        if (elementClass == Object.class || elementClass == Serializable.class) {
            elementClass = null;
        }
        Label end_2 = new Label();
        Label else_ = new Label();
        Label endIf_2 = new Label();
        _nameApply(mw, fieldInfo, context, end_2);
        _get(mw, context, fieldInfo);
        mw.visitTypeInsn(Opcodes.CHECKCAST, "java/util/List");
        mw.visitVarInsn(58, context.var("list"));
        _filters(mw, fieldInfo, context, end_2);
        mw.visitVarInsn(25, context.var("list"));
        mw.visitJumpInsn(Opcodes.IFNONNULL, else_);
        _if_write_null(mw, fieldInfo, context);
        mw.visitJumpInsn(Opcodes.GOTO, endIf_2);
        mw.visitLabel(else_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
        _writeFieldName(mw, context);
        mw.visitVarInsn(25, context.var("list"));
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "size", "()I");
        mw.visitVarInsn(54, context.var("size"));
        Label _else_3 = new Label();
        Label _end_if_32 = new Label();
        mw.visitVarInsn(21, context.var("size"));
        mw.visitInsn(3);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, _else_3);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitLdcInsn("[]");
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(Ljava/lang/String;)V");
        mw.visitJumpInsn(Opcodes.GOTO, _end_if_32);
        mw.visitLabel(_else_3);
        if (!context.nonContext) {
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("list"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "setContext", "(Ljava/lang/Object;Ljava/lang/Object;)V");
        }
        if (elementType != String.class || !context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 91);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
            Label for_ = new Label();
            Label forFirst_ = new Label();
            Label forEnd_2 = new Label();
            mw.visitInsn(3);
            mw.visitVarInsn(54, context.var("i"));
            mw.visitLabel(for_);
            mw.visitVarInsn(21, context.var("i"));
            mw.visitVarInsn(21, context.var("size"));
            mw.visitJumpInsn(Opcodes.IF_ICMPGE, forEnd_2);
            mw.visitVarInsn(21, context.var("i"));
            mw.visitJumpInsn(Opcodes.IFEQ, forFirst_);
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 44);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
            mw.visitLabel(forFirst_);
            mw.visitVarInsn(25, context.var("list"));
            mw.visitVarInsn(21, context.var("i"));
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;");
            mw.visitVarInsn(58, context.var("list_item"));
            Label forItemNullEnd_ = new Label();
            Label forItemNullElse_ = new Label();
            mw.visitVarInsn(25, context.var("list_item"));
            mw.visitJumpInsn(Opcodes.IFNONNULL, forItemNullElse_);
            mw.visitVarInsn(25, context.var("out"));
            end_ = end_2;
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeNull", "()V");
            mw.visitJumpInsn(Opcodes.GOTO, forItemNullEnd_);
            mw.visitLabel(forItemNullElse_);
            Label forItemClassIfEnd_ = new Label();
            Label forItemClassIfElse_ = new Label();
            if (elementClass == null || !Modifier.isPublic(elementClass.getModifiers())) {
                endIf_ = endIf_2;
                forEnd_ = forEnd_2;
                _end_if_3 = _end_if_32;
            } else {
                mw.visitVarInsn(25, context.var("list_item"));
                endIf_ = endIf_2;
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                mw.visitJumpInsn(Opcodes.IF_ACMPNE, forItemClassIfElse_);
                _getListFieldItemSer(context, mw, fieldInfo, elementClass);
                mw.visitVarInsn(58, context.var("list_item_desc"));
                Label instanceOfElse_ = new Label();
                Label instanceOfEnd_ = new Label();
                if (context.writeDirect) {
                    String writeMethodName = (!context.nonContext || !context.writeDirect) ? "write" : "writeDirectNonContext";
                    mw.visitVarInsn(25, context.var("list_item_desc"));
                    mw.visitTypeInsn(Opcodes.INSTANCEOF, JavaBeanSerializer);
                    mw.visitJumpInsn(Opcodes.IFEQ, instanceOfElse_);
                    mw.visitVarInsn(25, context.var("list_item_desc"));
                    mw.visitTypeInsn(Opcodes.CHECKCAST, JavaBeanSerializer);
                    mw.visitVarInsn(25, 1);
                    mw.visitVarInsn(25, context.var("list_item"));
                    if (context.nonContext) {
                        mw.visitInsn(1);
                        forEnd_ = forEnd_2;
                        _end_if_3 = _end_if_32;
                    } else {
                        mw.visitVarInsn(21, context.var("i"));
                        _end_if_3 = _end_if_32;
                        forEnd_ = forEnd_2;
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                    }
                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                    mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                    String str = JavaBeanSerializer;
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, writeMethodName, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                    mw.visitJumpInsn(Opcodes.GOTO, instanceOfEnd_);
                    mw.visitLabel(instanceOfElse_);
                } else {
                    forEnd_ = forEnd_2;
                    _end_if_3 = _end_if_32;
                }
                mw.visitVarInsn(25, context.var("list_item_desc"));
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, context.var("list_item"));
                if (context.nonContext) {
                    mw.visitInsn(1);
                } else {
                    mw.visitVarInsn(21, context.var("i"));
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                }
                mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                String str2 = ObjectSerializer;
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, str2, "write", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                mw.visitLabel(instanceOfEnd_);
                mw.visitJumpInsn(Opcodes.GOTO, forItemClassIfEnd_);
            }
            mw.visitLabel(forItemClassIfElse_);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("list_item"));
            if (context.nonContext) {
                mw.visitInsn(1);
            } else {
                mw.visitVarInsn(21, context.var("i"));
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            }
            if (elementClass == null || !Modifier.isPublic(elementClass.getModifiers())) {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
            } else {
                mw.visitLdcInsn(Type.getType(ASMUtils.desc((Class) elementType)));
                mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            }
            mw.visitLabel(forItemClassIfEnd_);
            mw.visitLabel(forItemNullEnd_);
            mw.visitIincInsn(context.var("i"), 1);
            mw.visitJumpInsn(Opcodes.GOTO, for_);
            mw.visitLabel(forEnd_);
            i2 = 25;
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 93);
            String str3 = SerializeWriter;
            i = Opcodes.INVOKEVIRTUAL;
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str3, "write", "(I)V");
        } else {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(25, context.var("list"));
            String str4 = SerializeWriter;
            i = Opcodes.INVOKEVIRTUAL;
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str4, "write", "(Ljava/util/List;)V");
            end_ = end_2;
            endIf_ = endIf_2;
            _end_if_3 = _end_if_32;
            i2 = 25;
        }
        mw.visitVarInsn(i2, 1);
        mw.visitMethodInsn(i, JSONSerializer, "popContext", "()V");
        mw.visitLabel(_end_if_3);
        _seperator(mw, context);
        mw.visitLabel(endIf_);
        mw.visitLabel(end_);
    }

    private void _filters(MethodVisitor mw, FieldInfo property, Context context, Label _end) {
        if (property.fieldTransient) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(Integer.valueOf(SerializerFeature.SkipTransientField.mask));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(Opcodes.IFNE, _end);
        }
        _notWriteDefault(mw, property, context, _end);
        if (!context.writeDirect) {
            _apply(mw, property, context);
            mw.visitJumpInsn(Opcodes.IFEQ, _end);
            _processKey(mw, property, context);
            _processValue(mw, property, context, _end);
        }
    }

    private void _nameApply(MethodVisitor mw, FieldInfo property, Context context, Label _end) {
        if (!context.writeDirect) {
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, Context.fieldName);
            String str = JavaBeanSerializer;
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "applyName", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/String;)Z");
            mw.visitJumpInsn(Opcodes.IFEQ, _end);
            _labelApply(mw, property, context, _end);
        }
        if (property.field == null) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(Integer.valueOf(SerializerFeature.IgnoreNonFieldGetter.mask));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(Opcodes.IFNE, _end);
        }
    }

    private void _labelApply(MethodVisitor mw, FieldInfo property, Context context, Label _end) {
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(property.label);
        String str = JavaBeanSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "applyLabel", "(L" + JSONSerializer + ";Ljava/lang/String;)Z");
        mw.visitJumpInsn(Opcodes.IFEQ, _end);
    }

    private void _writeObject(MethodVisitor mw, FieldInfo fieldInfo, Context context, Label _end) {
        String writeMethodName;
        String format = fieldInfo.getFormat();
        Class<?> fieldClass = fieldInfo.fieldClass;
        Label notNull_ = new Label();
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("object"));
        } else {
            mw.visitVarInsn(25, Context.processValue);
        }
        mw.visitInsn(89);
        mw.visitVarInsn(58, context.var("object"));
        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);
        _if_write_null(mw, fieldInfo, context);
        mw.visitJumpInsn(Opcodes.GOTO, _end);
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
        _writeFieldName(mw, context);
        Label classIfEnd_ = new Label();
        Label classIfElse_ = new Label();
        if (Modifier.isPublic(fieldClass.getModifiers()) && !ParserConfig.isPrimitive2(fieldClass)) {
            mw.visitVarInsn(25, context.var("object"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
            mw.visitJumpInsn(Opcodes.IF_ACMPNE, classIfElse_);
            _getFieldSer(context, mw, fieldInfo);
            mw.visitVarInsn(58, context.var("fied_ser"));
            Label instanceOfElse_ = new Label();
            Label instanceOfEnd_ = new Label();
            mw.visitVarInsn(25, context.var("fied_ser"));
            mw.visitTypeInsn(Opcodes.INSTANCEOF, JavaBeanSerializer);
            mw.visitJumpInsn(Opcodes.IFEQ, instanceOfElse_);
            boolean disableCircularReferenceDetect = (fieldInfo.serialzeFeatures & SerializerFeature.DisableCircularReferenceDetect.mask) != 0;
            boolean fieldBeanToArray = (SerializerFeature.BeanToArray.mask & fieldInfo.serialzeFeatures) != 0;
            if (disableCircularReferenceDetect || (context.nonContext && context.writeDirect)) {
                writeMethodName = fieldBeanToArray ? "writeAsArrayNonContext" : "writeDirectNonContext";
            } else {
                writeMethodName = fieldBeanToArray ? "writeAsArray" : "write";
            }
            mw.visitVarInsn(25, context.var("fied_ser"));
            mw.visitTypeInsn(Opcodes.CHECKCAST, JavaBeanSerializer);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("object"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, 0);
            String str = context.className;
            mw.visitFieldInsn(Opcodes.GETFIELD, str, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
            mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
            String str2 = JavaBeanSerializer;
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, writeMethodName, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitJumpInsn(Opcodes.GOTO, instanceOfEnd_);
            mw.visitLabel(instanceOfElse_);
            mw.visitVarInsn(25, context.var("fied_ser"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("object"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, 0);
            String str3 = context.className;
            mw.visitFieldInsn(Opcodes.GETFIELD, str3, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
            mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
            String str4 = ObjectSerializer;
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, str4, "write", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitLabel(instanceOfEnd_);
            mw.visitJumpInsn(Opcodes.GOTO, classIfEnd_);
        }
        mw.visitLabel(classIfElse_);
        mw.visitVarInsn(25, 1);
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("object"));
        } else {
            mw.visitVarInsn(25, Context.processValue);
        }
        if (format != null) {
            mw.visitLdcInsn(format);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFormat", "(Ljava/lang/Object;Ljava/lang/String;)V");
        } else {
            mw.visitVarInsn(25, Context.fieldName);
            if (!(fieldInfo.fieldType instanceof Class) || !((Class) fieldInfo.fieldType).isPrimitive()) {
                if (fieldInfo.fieldClass == String.class) {
                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(String.class)));
                } else {
                    mw.visitVarInsn(25, 0);
                    String str5 = context.className;
                    mw.visitFieldInsn(Opcodes.GETFIELD, str5, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
                }
                mw.visitLdcInsn(Integer.valueOf(fieldInfo.serialzeFeatures));
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            } else {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
            }
        }
        mw.visitLabel(classIfEnd_);
        _seperator(mw, context);
    }

    private void _before(MethodVisitor mw, Context context) {
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(21, context.var("seperator"));
        String str = JavaBeanSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "writeBefore", "(L" + JSONSerializer + ";Ljava/lang/Object;C)C");
        mw.visitVarInsn(54, context.var("seperator"));
    }

    private void _after(MethodVisitor mw, Context context) {
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(21, context.var("seperator"));
        String str = JavaBeanSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "writeAfter", "(L" + JSONSerializer + ";Ljava/lang/Object;C)C");
        mw.visitVarInsn(54, context.var("seperator"));
    }

    private void _notWriteDefault(MethodVisitor mw, FieldInfo property, Context context, Label _end) {
        if (!context.writeDirect) {
            Label elseLabel = new Label();
            mw.visitVarInsn(21, context.var("notWriteDefaultValue"));
            mw.visitJumpInsn(Opcodes.IFEQ, elseLabel);
            Class<?> propertyClass = property.fieldClass;
            if (propertyClass == Boolean.TYPE) {
                mw.visitVarInsn(21, context.var("boolean"));
                mw.visitJumpInsn(Opcodes.IFEQ, _end);
            } else if (propertyClass == Byte.TYPE) {
                mw.visitVarInsn(21, context.var("byte"));
                mw.visitJumpInsn(Opcodes.IFEQ, _end);
            } else if (propertyClass == Short.TYPE) {
                mw.visitVarInsn(21, context.var("short"));
                mw.visitJumpInsn(Opcodes.IFEQ, _end);
            } else if (propertyClass == Integer.TYPE) {
                mw.visitVarInsn(21, context.var("int"));
                mw.visitJumpInsn(Opcodes.IFEQ, _end);
            } else if (propertyClass == Long.TYPE) {
                mw.visitVarInsn(22, context.var("long"));
                mw.visitInsn(9);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFEQ, _end);
            } else if (propertyClass == Float.TYPE) {
                mw.visitVarInsn(23, context.var("float"));
                mw.visitInsn(11);
                mw.visitInsn(Opcodes.FCMPL);
                mw.visitJumpInsn(Opcodes.IFEQ, _end);
            } else if (propertyClass == Double.TYPE) {
                mw.visitVarInsn(24, context.var("double"));
                mw.visitInsn(14);
                mw.visitInsn(Opcodes.DCMPL);
                mw.visitJumpInsn(Opcodes.IFEQ, _end);
            }
            mw.visitLabel(elseLabel);
        }
    }

    private void _apply(MethodVisitor mw, FieldInfo property, Context context) {
        Class<?> propertyClass = property.fieldClass;
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, Context.fieldName);
        if (propertyClass == Byte.TYPE) {
            mw.visitVarInsn(21, context.var("byte"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
        } else if (propertyClass == Short.TYPE) {
            mw.visitVarInsn(21, context.var("short"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
        } else if (propertyClass == Integer.TYPE) {
            mw.visitVarInsn(21, context.var("int"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        } else if (propertyClass == Character.TYPE) {
            mw.visitVarInsn(21, context.var("char"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
        } else if (propertyClass == Long.TYPE) {
            mw.visitVarInsn(22, context.var("long", 2));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        } else if (propertyClass == Float.TYPE) {
            mw.visitVarInsn(23, context.var("float"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
        } else if (propertyClass == Double.TYPE) {
            mw.visitVarInsn(24, context.var("double", 2));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        } else if (propertyClass == Boolean.TYPE) {
            mw.visitVarInsn(21, context.var("boolean"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        } else if (propertyClass == BigDecimal.class) {
            mw.visitVarInsn(25, context.var("decimal"));
        } else if (propertyClass == String.class) {
            mw.visitVarInsn(25, context.var("string"));
        } else if (propertyClass.isEnum()) {
            mw.visitVarInsn(25, context.var("enum"));
        } else if (List.class.isAssignableFrom(propertyClass)) {
            mw.visitVarInsn(25, context.var("list"));
        } else {
            mw.visitVarInsn(25, context.var("object"));
        }
        String str = JavaBeanSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "apply", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Z");
    }

    private void _processValue(MethodVisitor mw, FieldInfo fieldInfo, Context context, Label _end) {
        Label processKeyElse_ = new Label();
        Class<?> fieldClass = fieldInfo.fieldClass;
        if (fieldClass.isPrimitive()) {
            Label checkValueEnd_ = new Label();
            mw.visitVarInsn(21, context.var("checkValue"));
            mw.visitJumpInsn(Opcodes.IFNE, checkValueEnd_);
            mw.visitInsn(1);
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(58, Context.processValue);
            mw.visitJumpInsn(Opcodes.GOTO, processKeyElse_);
            mw.visitLabel(checkValueEnd_);
        }
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 0);
        mw.visitLdcInsn(Integer.valueOf(context.getFieldOrinal(fieldInfo.name)));
        String str = JavaBeanSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "getBeanContext", "(I)" + ASMUtils.desc(BeanContext.class));
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, Context.fieldName);
        if (fieldClass == Byte.TYPE) {
            mw.visitVarInsn(21, context.var("byte"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        } else if (fieldClass == Short.TYPE) {
            mw.visitVarInsn(21, context.var("short"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        } else if (fieldClass == Integer.TYPE) {
            mw.visitVarInsn(21, context.var("int"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        } else if (fieldClass == Character.TYPE) {
            mw.visitVarInsn(21, context.var("char"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        } else if (fieldClass == Long.TYPE) {
            mw.visitVarInsn(22, context.var("long", 2));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        } else if (fieldClass == Float.TYPE) {
            mw.visitVarInsn(23, context.var("float"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        } else if (fieldClass == Double.TYPE) {
            mw.visitVarInsn(24, context.var("double", 2));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        } else if (fieldClass == Boolean.TYPE) {
            mw.visitVarInsn(21, context.var("boolean"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        } else if (fieldClass == BigDecimal.class) {
            mw.visitVarInsn(25, context.var("decimal"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        } else if (fieldClass == String.class) {
            mw.visitVarInsn(25, context.var("string"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        } else if (fieldClass.isEnum()) {
            mw.visitVarInsn(25, context.var("enum"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        } else if (List.class.isAssignableFrom(fieldClass)) {
            mw.visitVarInsn(25, context.var("list"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        } else {
            mw.visitVarInsn(25, context.var("object"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        }
        String str2 = JavaBeanSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, "processValue", "(L" + JSONSerializer + ";" + ASMUtils.desc(BeanContext.class) + "Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitVarInsn(58, Context.processValue);
        mw.visitVarInsn(25, Context.original);
        mw.visitVarInsn(25, Context.processValue);
        mw.visitJumpInsn(Opcodes.IF_ACMPEQ, processKeyElse_);
        _writeObject(mw, fieldInfo, context, _end);
        mw.visitJumpInsn(Opcodes.GOTO, _end);
        mw.visitLabel(processKeyElse_);
    }

    private void _processKey(MethodVisitor mw, FieldInfo property, Context context) {
        Label _else_processKey = new Label();
        mw.visitVarInsn(21, context.var("hasNameFilters"));
        mw.visitJumpInsn(Opcodes.IFEQ, _else_processKey);
        Class<?> propertyClass = property.fieldClass;
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, Context.fieldName);
        if (propertyClass == Byte.TYPE) {
            mw.visitVarInsn(21, context.var("byte"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
        } else if (propertyClass == Short.TYPE) {
            mw.visitVarInsn(21, context.var("short"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
        } else if (propertyClass == Integer.TYPE) {
            mw.visitVarInsn(21, context.var("int"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        } else if (propertyClass == Character.TYPE) {
            mw.visitVarInsn(21, context.var("char"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
        } else if (propertyClass == Long.TYPE) {
            mw.visitVarInsn(22, context.var("long", 2));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        } else if (propertyClass == Float.TYPE) {
            mw.visitVarInsn(23, context.var("float"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
        } else if (propertyClass == Double.TYPE) {
            mw.visitVarInsn(24, context.var("double", 2));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        } else if (propertyClass == Boolean.TYPE) {
            mw.visitVarInsn(21, context.var("boolean"));
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        } else if (propertyClass == BigDecimal.class) {
            mw.visitVarInsn(25, context.var("decimal"));
        } else if (propertyClass == String.class) {
            mw.visitVarInsn(25, context.var("string"));
        } else if (propertyClass.isEnum()) {
            mw.visitVarInsn(25, context.var("enum"));
        } else if (List.class.isAssignableFrom(propertyClass)) {
            mw.visitVarInsn(25, context.var("list"));
        } else {
            mw.visitVarInsn(25, context.var("object"));
        }
        String str = JavaBeanSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "processKey", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;");
        mw.visitVarInsn(58, Context.fieldName);
        mw.visitLabel(_else_processKey);
    }

    private void _if_write_null(MethodVisitor mw, FieldInfo fieldInfo, Context context) {
        int writeNullFeatures;
        Class<?> propertyClass = fieldInfo.fieldClass;
        Label _if = new Label();
        Label _else = new Label();
        Label _write_null = new Label();
        Label _end_if = new Label();
        mw.visitLabel(_if);
        JSONField annotation = fieldInfo.getAnnotation();
        int features = 0;
        if (annotation != null) {
            features = SerializerFeature.of(annotation.serialzeFeatures());
        }
        JSONType jsonType = context.beanInfo.jsonType;
        if (jsonType != null) {
            features |= SerializerFeature.of(jsonType.serialzeFeatures());
        }
        if (propertyClass == String.class) {
            writeNullFeatures = SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullStringAsEmpty.getMask();
        } else if (Number.class.isAssignableFrom(propertyClass)) {
            writeNullFeatures = SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullNumberAsZero.getMask();
        } else if (Collection.class.isAssignableFrom(propertyClass)) {
            writeNullFeatures = SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullListAsEmpty.getMask();
        } else if (Boolean.class == propertyClass) {
            writeNullFeatures = SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullBooleanAsFalse.getMask();
        } else {
            writeNullFeatures = SerializerFeature.WRITE_MAP_NULL_FEATURES;
        }
        if ((features & writeNullFeatures) == 0) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(Integer.valueOf(writeNullFeatures));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(Opcodes.IFEQ, _else);
        }
        mw.visitLabel(_write_null);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
        _writeFieldName(mw, context);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitLdcInsn(Integer.valueOf(features));
        if (propertyClass == String.class || propertyClass == Character.class) {
            mw.visitLdcInsn(Integer.valueOf(SerializerFeature.WriteNullStringAsEmpty.mask));
        } else if (Number.class.isAssignableFrom(propertyClass)) {
            mw.visitLdcInsn(Integer.valueOf(SerializerFeature.WriteNullNumberAsZero.mask));
        } else if (propertyClass == Boolean.class) {
            mw.visitLdcInsn(Integer.valueOf(SerializerFeature.WriteNullBooleanAsFalse.mask));
        } else if (Collection.class.isAssignableFrom(propertyClass) || propertyClass.isArray()) {
            mw.visitLdcInsn(Integer.valueOf(SerializerFeature.WriteNullListAsEmpty.mask));
        } else {
            mw.visitLdcInsn(0);
        }
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeNull", "(II)V");
        _seperator(mw, context);
        mw.visitJumpInsn(Opcodes.GOTO, _end_if);
        mw.visitLabel(_else);
        mw.visitLabel(_end_if);
    }

    private void _writeFieldName(MethodVisitor mw, Context context) {
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldNameDirect", "(Ljava/lang/String;)V");
            return;
        }
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitInsn(3);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SerializeWriter, "writeFieldName", "(Ljava/lang/String;Z)V");
    }

    private void _seperator(MethodVisitor mw, Context context) {
        mw.visitVarInsn(16, 44);
        mw.visitVarInsn(54, context.var("seperator"));
    }

    private void _getListFieldItemSer(Context context, MethodVisitor mw, FieldInfo fieldInfo, Class<?> itemType) {
        Label notNull_ = new Label();
        mw.visitVarInsn(25, 0);
        String str = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str, fieldInfo.name + "_asm_list_item_ser_", ObjectSerializer_desc);
        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(Type.getType(ASMUtils.desc(itemType)));
        String str2 = JSONSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, "getObjectWriter", "(Ljava/lang/Class;)" + ObjectSerializer_desc);
        String str3 = context.className;
        mw.visitFieldInsn(Opcodes.PUTFIELD, str3, fieldInfo.name + "_asm_list_item_ser_", ObjectSerializer_desc);
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, 0);
        String str4 = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str4, fieldInfo.name + "_asm_list_item_ser_", ObjectSerializer_desc);
    }

    private void _getFieldSer(Context context, MethodVisitor mw, FieldInfo fieldInfo) {
        Label notNull_ = new Label();
        mw.visitVarInsn(25, 0);
        String str = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str, fieldInfo.name + "_asm_ser_", ObjectSerializer_desc);
        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
        String str2 = JSONSerializer;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, "getObjectWriter", "(Ljava/lang/Class;)" + ObjectSerializer_desc);
        String str3 = context.className;
        mw.visitFieldInsn(Opcodes.PUTFIELD, str3, fieldInfo.name + "_asm_ser_", ObjectSerializer_desc);
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, 0);
        String str4 = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str4, fieldInfo.name + "_asm_ser_", ObjectSerializer_desc);
    }
}
