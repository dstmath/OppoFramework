package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.asm.ClassWriter;
import com.alibaba.fastjson.asm.FieldWriter;
import com.alibaba.fastjson.asm.Label;
import com.alibaba.fastjson.asm.MethodVisitor;
import com.alibaba.fastjson.asm.MethodWriter;
import com.alibaba.fastjson.asm.Opcodes;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONLexerBase;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.SymbolTable;
import com.alibaba.fastjson.util.ASMClassLoader;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.JavaBeanInfo;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ASMDeserializerFactory implements Opcodes {
    static final String DefaultJSONParser = ASMUtils.type(DefaultJSONParser.class);
    static final String JSONLexerBase = ASMUtils.type(JSONLexerBase.class);
    public final ASMClassLoader classLoader;
    protected final AtomicLong seed = new AtomicLong();

    public ASMDeserializerFactory(ClassLoader parentClassLoader) {
        this.classLoader = parentClassLoader instanceof ASMClassLoader ? (ASMClassLoader) parentClassLoader : new ASMClassLoader(parentClassLoader);
    }

    public ObjectDeserializer createJavaBeanDeserializer(ParserConfig config, JavaBeanInfo beanInfo) throws Exception {
        String classNameType;
        String packageName;
        Class<?> clazz = beanInfo.clazz;
        if (!clazz.isPrimitive()) {
            String className = "FastjsonASMDeserializer_" + this.seed.incrementAndGet() + "_" + clazz.getSimpleName();
            Package pkg = ASMDeserializerFactory.class.getPackage();
            if (pkg != null) {
                String packageName2 = pkg.getName();
                classNameType = packageName2.replace('.', '/') + "/" + className;
                packageName = packageName2 + "." + className;
            } else {
                classNameType = className;
                packageName = className;
            }
            ClassWriter cw = new ClassWriter();
            cw.visit(49, 33, classNameType, ASMUtils.type(JavaBeanDeserializer.class), null);
            _init(cw, new Context(classNameType, config, beanInfo, 3));
            _createInstance(cw, new Context(classNameType, config, beanInfo, 3));
            _deserialze(cw, new Context(classNameType, config, beanInfo, 5));
            _deserialzeArrayMapping(cw, new Context(classNameType, config, beanInfo, 4));
            byte[] code = cw.toByteArray();
            return (ObjectDeserializer) this.classLoader.defineClassPublic(packageName, code, 0, code.length).getConstructor(ParserConfig.class, JavaBeanInfo.class).newInstance(config, beanInfo);
        }
        throw new IllegalArgumentException("not support type :" + clazz.getName());
    }

    private void _setFlag(MethodVisitor mw, Context context, int i) {
        String varName = "_asm_flag_" + (i / 32);
        mw.visitVarInsn(21, context.var(varName));
        mw.visitLdcInsn(Integer.valueOf(1 << i));
        mw.visitInsn(Opcodes.IOR);
        mw.visitVarInsn(54, context.var(varName));
    }

    private void _isFlag(MethodVisitor mw, Context context, int i, Label label) {
        mw.visitVarInsn(21, context.var("_asm_flag_" + (i / 32)));
        mw.visitLdcInsn(Integer.valueOf(1 << i));
        mw.visitInsn(Opcodes.IAND);
        mw.visitJumpInsn(Opcodes.IFEQ, label);
    }

    private void _deserialzeArrayMapping(ClassWriter cw, Context context) {
        int fieldListSize;
        FieldInfo[] sortedFieldInfoList;
        char seperator;
        FieldInfo fieldInfo;
        MethodVisitor mw = new MethodWriter(cw, 1, "deserialzeArrayMapping", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        defineVarLexer(context, mw);
        _createInstance(context, mw);
        FieldInfo[] sortedFieldInfoList2 = context.beanInfo.sortedFields;
        int fieldListSize2 = sortedFieldInfoList2.length;
        int i = 0;
        while (i < fieldListSize2) {
            boolean last = i == fieldListSize2 + -1;
            char seperator2 = last ? ']' : ',';
            FieldInfo fieldInfo2 = sortedFieldInfoList2[i];
            Class<?> fieldClass = fieldInfo2.fieldClass;
            Type fieldType = fieldInfo2.fieldType;
            if (fieldClass == Byte.TYPE || fieldClass == Short.TYPE) {
                fieldInfo = fieldInfo2;
                sortedFieldInfoList = sortedFieldInfoList2;
                fieldListSize = fieldListSize2;
                seperator = seperator2;
            } else if (fieldClass == Integer.TYPE) {
                fieldInfo = fieldInfo2;
                sortedFieldInfoList = sortedFieldInfoList2;
                fieldListSize = fieldListSize2;
                seperator = seperator2;
            } else {
                if (fieldClass == Byte.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanInt", "(C)I");
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    Label valueNullEnd_ = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitLdcInsn(5);
                    mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_);
                    mw.visitInsn(1);
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    mw.visitLabel(valueNullEnd_);
                } else if (fieldClass == Short.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanInt", "(C)I");
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    Label valueNullEnd_2 = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitLdcInsn(5);
                    mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_2);
                    mw.visitInsn(1);
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    mw.visitLabel(valueNullEnd_2);
                } else if (fieldClass == Integer.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanInt", "(C)I");
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    Label valueNullEnd_3 = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitLdcInsn(5);
                    mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_3);
                    mw.visitInsn(1);
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    mw.visitLabel(valueNullEnd_3);
                } else if (fieldClass == Long.TYPE) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanLong", "(C)J");
                    mw.visitVarInsn(55, context.var(fieldInfo2.name + "_asm", 2));
                } else if (fieldClass == Long.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanLong", "(C)J");
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    Label valueNullEnd_4 = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitLdcInsn(5);
                    mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_4);
                    mw.visitInsn(1);
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    mw.visitLabel(valueNullEnd_4);
                } else if (fieldClass == Boolean.TYPE) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanBoolean", "(C)Z");
                    mw.visitVarInsn(54, context.var(fieldInfo2.name + "_asm"));
                } else if (fieldClass == Float.TYPE) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFloat", "(C)F");
                    mw.visitVarInsn(56, context.var(fieldInfo2.name + "_asm"));
                } else if (fieldClass == Float.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFloat", "(C)F");
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    Label valueNullEnd_5 = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitLdcInsn(5);
                    mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_5);
                    mw.visitInsn(1);
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    mw.visitLabel(valueNullEnd_5);
                } else if (fieldClass == Double.TYPE) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanDouble", "(C)D");
                    mw.visitVarInsn(57, context.var(fieldInfo2.name + "_asm", 2));
                } else if (fieldClass == Double.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanDouble", "(C)D");
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    Label valueNullEnd_6 = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitLdcInsn(5);
                    mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_6);
                    mw.visitInsn(1);
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    mw.visitLabel(valueNullEnd_6);
                } else if (fieldClass == Character.TYPE) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanString", "(C)Ljava/lang/String;");
                    mw.visitInsn(3);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C");
                    mw.visitVarInsn(54, context.var(fieldInfo2.name + "_asm"));
                } else if (fieldClass == String.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanString", "(C)Ljava/lang/String;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                } else if (fieldClass == BigDecimal.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanDecimal", "(C)Ljava/math/BigDecimal;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                } else if (fieldClass == Date.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanDate", "(C)Ljava/util/Date;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                } else if (fieldClass == UUID.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator2);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanUUID", "(C)Ljava/util/UUID;");
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                } else {
                    if (fieldClass.isEnum()) {
                        Label enumNumIf_ = new Label();
                        Label enumNumErr_ = new Label();
                        Label enumStore_ = new Label();
                        Label enumQuote_ = new Label();
                        sortedFieldInfoList = sortedFieldInfoList2;
                        mw.visitVarInsn(25, context.var("lexer"));
                        fieldListSize = fieldListSize2;
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "getCurrent", "()C");
                        mw.visitInsn(89);
                        mw.visitVarInsn(54, context.var("ch"));
                        mw.visitLdcInsn(110);
                        mw.visitJumpInsn(Opcodes.IF_ICMPEQ, enumQuote_);
                        mw.visitVarInsn(21, context.var("ch"));
                        mw.visitLdcInsn(34);
                        mw.visitJumpInsn(Opcodes.IF_ICMPNE, enumNumIf_);
                        mw.visitLabel(enumQuote_);
                        mw.visitVarInsn(25, context.var("lexer"));
                        mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(fieldClass)));
                        mw.visitVarInsn(25, 1);
                        String str = DefaultJSONParser;
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "getSymbolTable", "()" + ASMUtils.desc(SymbolTable.class));
                        mw.visitVarInsn(16, seperator2);
                        String str2 = JSONLexerBase;
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, "scanEnum", "(Ljava/lang/Class;" + ASMUtils.desc(SymbolTable.class) + "C)Ljava/lang/Enum;");
                        mw.visitJumpInsn(Opcodes.GOTO, enumStore_);
                        mw.visitLabel(enumNumIf_);
                        mw.visitVarInsn(21, context.var("ch"));
                        mw.visitLdcInsn(48);
                        mw.visitJumpInsn(Opcodes.IF_ICMPLT, enumNumErr_);
                        mw.visitVarInsn(21, context.var("ch"));
                        mw.visitLdcInsn(57);
                        mw.visitJumpInsn(Opcodes.IF_ICMPGT, enumNumErr_);
                        _getFieldDeser(context, mw, fieldInfo2);
                        mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(EnumDeserializer.class));
                        mw.visitVarInsn(25, context.var("lexer"));
                        mw.visitVarInsn(16, seperator2);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanInt", "(C)I");
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(EnumDeserializer.class), "valueOf", "(I)Ljava/lang/Enum;");
                        mw.visitJumpInsn(Opcodes.GOTO, enumStore_);
                        mw.visitLabel(enumNumErr_);
                        mw.visitVarInsn(25, 0);
                        mw.visitVarInsn(25, context.var("lexer"));
                        mw.visitVarInsn(16, seperator2);
                        String type = ASMUtils.type(JavaBeanDeserializer.class);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, type, "scanEnum", "(L" + JSONLexerBase + ";C)Ljava/lang/Enum;");
                        mw.visitLabel(enumStore_);
                        mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass));
                        mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                    } else {
                        sortedFieldInfoList = sortedFieldInfoList2;
                        fieldListSize = fieldListSize2;
                        if (Collection.class.isAssignableFrom(fieldClass)) {
                            Class<?> itemClass = TypeUtils.getCollectionItemClass(fieldType);
                            if (itemClass == String.class) {
                                if (fieldClass == List.class || fieldClass == Collections.class || fieldClass == ArrayList.class) {
                                    mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(ArrayList.class));
                                    mw.visitInsn(89);
                                    mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(ArrayList.class), "<init>", "()V");
                                } else {
                                    mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(fieldClass)));
                                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, ASMUtils.type(TypeUtils.class), "createCollection", "(Ljava/lang/Class;)Ljava/util/Collection;");
                                }
                                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, context.var(fieldInfo2.name + "_asm"));
                                mw.visitVarInsn(16, seperator2);
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanStringArray", "(Ljava/util/Collection;C)V");
                                Label valueNullEnd_7 = new Label();
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                                mw.visitLdcInsn(5);
                                mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_7);
                                mw.visitInsn(1);
                                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                                mw.visitLabel(valueNullEnd_7);
                            } else {
                                Label notError_ = new Label();
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
                                mw.visitVarInsn(54, context.var("token"));
                                mw.visitVarInsn(21, context.var("token"));
                                int token = i == 0 ? 14 : 16;
                                mw.visitLdcInsn(Integer.valueOf(token));
                                mw.visitJumpInsn(Opcodes.IF_ICMPEQ, notError_);
                                mw.visitVarInsn(25, 1);
                                mw.visitLdcInsn(Integer.valueOf(token));
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DefaultJSONParser, "throwException", "(I)V");
                                mw.visitLabel(notError_);
                                Label quickElse_ = new Label();
                                Label quickEnd_ = new Label();
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "getCurrent", "()C");
                                mw.visitVarInsn(16, 91);
                                mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElse_);
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "next", "()C");
                                mw.visitInsn(87);
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitLdcInsn(14);
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
                                mw.visitJumpInsn(Opcodes.GOTO, quickEnd_);
                                mw.visitLabel(quickElse_);
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitLdcInsn(14);
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "(I)V");
                                mw.visitLabel(quickEnd_);
                                _newCollection(mw, fieldClass, i, false);
                                mw.visitInsn(89);
                                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                                _getCollectionFieldItemDeser(context, mw, fieldInfo2, itemClass);
                                mw.visitVarInsn(25, 1);
                                mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(itemClass)));
                                mw.visitVarInsn(25, 3);
                                String type2 = ASMUtils.type(JavaBeanDeserializer.class);
                                mw.visitMethodInsn(Opcodes.INVOKESTATIC, type2, "parseArray", "(Ljava/util/Collection;" + ASMUtils.desc(ObjectDeserializer.class) + "L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;)V");
                            }
                        } else if (fieldClass.isArray()) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitLdcInsn(14);
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "(I)V");
                            mw.visitVarInsn(25, 1);
                            mw.visitVarInsn(25, 0);
                            mw.visitLdcInsn(Integer.valueOf(i));
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(JavaBeanDeserializer.class), "getFieldType", "(I)Ljava/lang/reflect/Type;");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DefaultJSONParser, "parseObject", "(Ljava/lang/reflect/Type;)Ljava/lang/Object;");
                            mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass));
                            mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                        } else {
                            Label objElseIf_ = new Label();
                            Label objEndIf_ = new Label();
                            if (fieldClass == Date.class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "getCurrent", "()C");
                                mw.visitLdcInsn(49);
                                mw.visitJumpInsn(Opcodes.IF_ICMPNE, objElseIf_);
                                mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(Date.class));
                                mw.visitInsn(89);
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(16, seperator2);
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanLong", "(C)J");
                                mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(Date.class), "<init>", "(J)V");
                                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                                mw.visitJumpInsn(Opcodes.GOTO, objEndIf_);
                            }
                            mw.visitLabel(objElseIf_);
                            _quickNextToken(context, mw, 14);
                            _deserObject(context, mw, fieldInfo2, fieldClass, i);
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
                            mw.visitLdcInsn(15);
                            mw.visitJumpInsn(Opcodes.IF_ICMPEQ, objEndIf_);
                            mw.visitVarInsn(25, 0);
                            mw.visitVarInsn(25, context.var("lexer"));
                            if (!last) {
                                mw.visitLdcInsn(16);
                            } else {
                                mw.visitLdcInsn(15);
                            }
                            String type3 = ASMUtils.type(JavaBeanDeserializer.class);
                            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, type3, "check", "(" + ASMUtils.desc(JSONLexer.class) + "I)V");
                            mw.visitLabel(objEndIf_);
                        }
                    }
                    i++;
                    sortedFieldInfoList2 = sortedFieldInfoList;
                    fieldListSize2 = fieldListSize;
                }
                sortedFieldInfoList = sortedFieldInfoList2;
                fieldListSize = fieldListSize2;
                i++;
                sortedFieldInfoList2 = sortedFieldInfoList;
                fieldListSize2 = fieldListSize;
            }
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitVarInsn(16, seperator);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanInt", "(C)I");
            mw.visitVarInsn(54, context.var(fieldInfo.name + "_asm"));
            i++;
            sortedFieldInfoList2 = sortedFieldInfoList;
            fieldListSize2 = fieldListSize;
        }
        _batchSet(context, mw, false);
        Label quickElse_2 = new Label();
        Label quickElseIf_ = new Label();
        Label quickElseIfEOI_ = new Label();
        Label quickEnd_2 = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "getCurrent", "()C");
        mw.visitInsn(89);
        mw.visitVarInsn(54, context.var("ch"));
        mw.visitVarInsn(16, 44);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElseIf_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(16);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, quickEnd_2);
        mw.visitLabel(quickElseIf_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 93);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElseIfEOI_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(15);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, quickEnd_2);
        mw.visitLabel(quickElseIfEOI_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 26);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElse_2);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(20);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, quickEnd_2);
        mw.visitLabel(quickElse_2);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(16);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "(I)V");
        mw.visitLabel(quickEnd_2);
        mw.visitVarInsn(25, context.var("instance"));
        mw.visitInsn(Opcodes.ARETURN);
        mw.visitMaxs(5, context.variantIndex);
        mw.visitEnd();
    }

    /* JADX WARNING: Removed duplicated region for block: B:133:0x0f4e  */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x0f81  */
    private void _deserialze(ClassWriter cw, Context context) {
        int i;
        Label return_;
        int fieldListSize;
        Label end_;
        char c;
        Label super_;
        JavaBeanInfo beanInfo;
        if (context.fieldInfoList.length != 0) {
            FieldInfo[] fieldInfoArr = context.fieldInfoList;
            for (FieldInfo fieldInfo : fieldInfoArr) {
                Class<?> fieldClass = fieldInfo.fieldClass;
                Type fieldType = fieldInfo.fieldType;
                if (fieldClass == Character.TYPE) {
                    return;
                }
                if (Collection.class.isAssignableFrom(fieldClass) && (!(fieldType instanceof ParameterizedType) || !(((ParameterizedType) fieldType).getActualTypeArguments()[0] instanceof Class))) {
                    return;
                }
            }
            JavaBeanInfo beanInfo2 = context.beanInfo;
            context.fieldInfoList = beanInfo2.sortedFields;
            MethodVisitor mw = new MethodWriter(cw, 1, "deserialze", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;I)Ljava/lang/Object;", null, null);
            Label reset_ = new Label();
            Label super_2 = new Label();
            Label end_2 = new Label();
            Label end_3 = new Label();
            defineVarLexer(context, mw);
            Label next_ = new Label();
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
            mw.visitLdcInsn(14);
            mw.visitJumpInsn(Opcodes.IF_ICMPNE, next_);
            if ((beanInfo2.parserFeatures & Feature.SupportArrayToBean.mask) == 0) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(21, 4);
                mw.visitLdcInsn(Integer.valueOf(Feature.SupportArrayToBean.mask));
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "isEnabled", "(II)Z");
                mw.visitJumpInsn(Opcodes.IFEQ, next_);
            }
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitInsn(1);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, context.className, "deserialzeArrayMapping", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mw.visitInsn(Opcodes.ARETURN);
            mw.visitLabel(next_);
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitLdcInsn(Integer.valueOf(Feature.SortFeidFastMatch.mask));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "isEnabled", "(I)Z");
            mw.visitJumpInsn(Opcodes.IFEQ, super_2);
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitLdcInsn(context.clazz.getName());
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanType", "(Ljava/lang/String;)I");
            mw.visitLdcInsn(-1);
            mw.visitJumpInsn(Opcodes.IF_ICMPEQ, super_2);
            mw.visitVarInsn(25, 1);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DefaultJSONParser, "getContext", "()" + ASMUtils.desc(ParseContext.class));
            mw.visitVarInsn(58, context.var("mark_context"));
            mw.visitInsn(3);
            mw.visitVarInsn(54, context.var("matchedCount"));
            _createInstance(context, mw);
            mw.visitVarInsn(25, 1);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DefaultJSONParser, "getContext", "()" + ASMUtils.desc(ParseContext.class));
            mw.visitVarInsn(58, context.var("context"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("context"));
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(25, 3);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DefaultJSONParser, "setContext", "(" + ASMUtils.desc(ParseContext.class) + "Ljava/lang/Object;Ljava/lang/Object;)" + ASMUtils.desc(ParseContext.class));
            mw.visitVarInsn(58, context.var("childContext"));
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
            mw.visitLdcInsn(4);
            mw.visitJumpInsn(Opcodes.IF_ICMPEQ, end_2);
            mw.visitInsn(3);
            mw.visitIntInsn(54, context.var("matchStat"));
            int fieldListSize2 = context.fieldInfoList.length;
            for (int i2 = 0; i2 < fieldListSize2; i2 += 32) {
                mw.visitInsn(3);
                mw.visitVarInsn(54, context.var("_asm_flag_" + (i2 / 32)));
            }
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitLdcInsn(Integer.valueOf(Feature.InitStringFieldAsEmpty.mask));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "isEnabled", "(I)Z");
            mw.visitIntInsn(54, context.var("initStringFieldAsEmpty"));
            int i3 = 0;
            while (i3 < fieldListSize2) {
                FieldInfo fieldInfo2 = context.fieldInfoList[i3];
                Class<?> fieldClass2 = fieldInfo2.fieldClass;
                if (fieldClass2 == Boolean.TYPE || fieldClass2 == Byte.TYPE || fieldClass2 == Short.TYPE) {
                    beanInfo = beanInfo2;
                    super_ = super_2;
                } else if (fieldClass2 == Integer.TYPE) {
                    beanInfo = beanInfo2;
                    super_ = super_2;
                } else {
                    if (fieldClass2 == Long.TYPE) {
                        mw.visitInsn(9);
                        mw.visitVarInsn(55, context.var(fieldInfo2.name + "_asm", 2));
                    } else if (fieldClass2 == Float.TYPE) {
                        mw.visitInsn(11);
                        mw.visitVarInsn(56, context.var(fieldInfo2.name + "_asm"));
                    } else if (fieldClass2 == Double.TYPE) {
                        mw.visitInsn(14);
                        mw.visitVarInsn(57, context.var(fieldInfo2.name + "_asm", 2));
                    } else {
                        if (fieldClass2 == String.class) {
                            Label flagEnd_ = new Label();
                            Label flagElse_ = new Label();
                            mw.visitVarInsn(21, context.var("initStringFieldAsEmpty"));
                            mw.visitJumpInsn(Opcodes.IFEQ, flagElse_);
                            _setFlag(mw, context, i3);
                            mw.visitVarInsn(25, context.var("lexer"));
                            beanInfo = beanInfo2;
                            super_ = super_2;
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "stringDefaultValue", "()Ljava/lang/String;");
                            mw.visitJumpInsn(Opcodes.GOTO, flagEnd_);
                            mw.visitLabel(flagElse_);
                            mw.visitInsn(1);
                            mw.visitLabel(flagEnd_);
                        } else {
                            beanInfo = beanInfo2;
                            super_ = super_2;
                            mw.visitInsn(1);
                        }
                        mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass2));
                        mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                        i3++;
                        beanInfo2 = beanInfo;
                        super_2 = super_;
                    }
                    beanInfo = beanInfo2;
                    super_ = super_2;
                    i3++;
                    beanInfo2 = beanInfo;
                    super_2 = super_;
                }
                mw.visitInsn(3);
                mw.visitVarInsn(54, context.var(fieldInfo2.name + "_asm"));
                i3++;
                beanInfo2 = beanInfo;
                super_2 = super_;
            }
            int i4 = 0;
            while (i4 < fieldListSize2) {
                FieldInfo fieldInfo3 = context.fieldInfoList[i4];
                Class<?> fieldClass3 = fieldInfo3.fieldClass;
                Type fieldType2 = fieldInfo3.fieldType;
                Label notMatch_ = new Label();
                if (fieldClass3 == Boolean.TYPE) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(25, 0);
                    mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldBoolean", "([C)Z");
                    mw.visitVarInsn(54, context.var(fieldInfo3.name + "_asm"));
                } else if (fieldClass3 == Byte.TYPE) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(25, 0);
                    mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldInt", "([C)I");
                    mw.visitVarInsn(54, context.var(fieldInfo3.name + "_asm"));
                } else {
                    if (fieldClass3 == Byte.class) {
                        mw.visitVarInsn(25, context.var("lexer"));
                        mw.visitVarInsn(25, 0);
                        mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldInt", "([C)I");
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                        mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                        Label valueNullEnd_ = new Label();
                        mw.visitVarInsn(25, context.var("lexer"));
                        return_ = end_2;
                        mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                        mw.visitLdcInsn(5);
                        mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_);
                        mw.visitInsn(1);
                        mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                        mw.visitLabel(valueNullEnd_);
                    } else {
                        return_ = end_2;
                        if (fieldClass3 == Short.TYPE) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldInt", "([C)I");
                            mw.visitVarInsn(54, context.var(fieldInfo3.name + "_asm"));
                        } else if (fieldClass3 == Short.class) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldInt", "([C)I");
                            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            Label valueNullEnd_2 = new Label();
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            mw.visitLdcInsn(5);
                            mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_2);
                            mw.visitInsn(1);
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            mw.visitLabel(valueNullEnd_2);
                        } else if (fieldClass3 == Integer.TYPE) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldInt", "([C)I");
                            mw.visitVarInsn(54, context.var(fieldInfo3.name + "_asm"));
                        } else if (fieldClass3 == Integer.class) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldInt", "([C)I");
                            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            Label valueNullEnd_3 = new Label();
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            mw.visitLdcInsn(5);
                            mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_3);
                            mw.visitInsn(1);
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            mw.visitLabel(valueNullEnd_3);
                        } else if (fieldClass3 == Long.TYPE) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldLong", "([C)J");
                            mw.visitVarInsn(55, context.var(fieldInfo3.name + "_asm", 2));
                        } else if (fieldClass3 == Long.class) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldLong", "([C)J");
                            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            Label valueNullEnd_4 = new Label();
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            mw.visitLdcInsn(5);
                            mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_4);
                            mw.visitInsn(1);
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            mw.visitLabel(valueNullEnd_4);
                        } else if (fieldClass3 == Float.TYPE) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldFloat", "([C)F");
                            mw.visitVarInsn(56, context.var(fieldInfo3.name + "_asm"));
                        } else if (fieldClass3 == Float.class) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldFloat", "([C)F");
                            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            Label valueNullEnd_5 = new Label();
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            mw.visitLdcInsn(5);
                            mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_5);
                            mw.visitInsn(1);
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            mw.visitLabel(valueNullEnd_5);
                        } else if (fieldClass3 == Double.TYPE) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldDouble", "([C)D");
                            mw.visitVarInsn(57, context.var(fieldInfo3.name + "_asm", 2));
                        } else if (fieldClass3 == Double.class) {
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitVarInsn(25, 0);
                            mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldDouble", "([C)D");
                            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            Label valueNullEnd_6 = new Label();
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            mw.visitLdcInsn(5);
                            mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNullEnd_6);
                            mw.visitInsn(1);
                            mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            mw.visitLabel(valueNullEnd_6);
                        } else {
                            if (fieldClass3 == String.class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldString", "([C)Ljava/lang/String;");
                                c = ':';
                                mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            } else if (fieldClass3 == Date.class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldDate", "([C)Ljava/util/Date;");
                                c = ':';
                                mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            } else if (fieldClass3 == UUID.class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldUUID", "([C)Ljava/util/UUID;");
                                c = ':';
                                mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            } else if (fieldClass3 == BigDecimal.class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldDecimal", "([C)Ljava/math/BigDecimal;");
                                c = ':';
                                mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            } else if (fieldClass3 == BigInteger.class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldBigInteger", "([C)Ljava/math/BigInteger;");
                                c = ':';
                                mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            } else if (fieldClass3 == int[].class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldIntArray", "([C)[I");
                                c = ':';
                                mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            } else if (fieldClass3 == float[].class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldFloatArray", "([C)[F");
                                c = ':';
                                mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            } else if (fieldClass3 == float[][].class) {
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldFloatArray2", "([C)[[F");
                                c = ':';
                                mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                            } else if (fieldClass3.isEnum()) {
                                mw.visitVarInsn(25, 0);
                                mw.visitVarInsn(25, context.var("lexer"));
                                mw.visitVarInsn(25, 0);
                                mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                _getFieldDeser(context, mw, fieldInfo3);
                                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(JavaBeanDeserializer.class), "scanEnum", "(L" + JSONLexerBase + ";[C" + ASMUtils.desc(ObjectDeserializer.class) + ")Ljava/lang/Enum;");
                                mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass3));
                                StringBuilder sb = new StringBuilder();
                                sb.append(fieldInfo3.name);
                                sb.append("_asm");
                                c = ':';
                                mw.visitVarInsn(58, context.var(sb.toString()));
                            } else {
                                if (Collection.class.isAssignableFrom(fieldClass3)) {
                                    mw.visitVarInsn(25, context.var("lexer"));
                                    mw.visitVarInsn(25, 0);
                                    mw.visitFieldInsn(Opcodes.GETFIELD, context.className, fieldInfo3.name + "_asm_prefix__", "[C");
                                    Class<?> itemClass = TypeUtils.getCollectionItemClass(fieldType2);
                                    if (itemClass == String.class) {
                                        mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(fieldClass3)));
                                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "scanFieldStringArray", "([CLjava/lang/Class;)" + ASMUtils.desc(Collection.class));
                                        mw.visitVarInsn(58, context.var(fieldInfo3.name + "_asm"));
                                        mw.visitVarInsn(25, context.var("lexer"));
                                        mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                                        Label flag_ = new Label();
                                        mw.visitJumpInsn(Opcodes.IFLE, flag_);
                                        _setFlag(mw, context, i4);
                                        mw.visitLabel(flag_);
                                        mw.visitVarInsn(25, context.var("lexer"));
                                        mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                                        mw.visitInsn(89);
                                        mw.visitVarInsn(54, context.var("matchStat"));
                                        mw.visitLdcInsn(-1);
                                        mw.visitJumpInsn(Opcodes.IF_ICMPEQ, reset_);
                                        mw.visitVarInsn(25, context.var("lexer"));
                                        mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                                        mw.visitJumpInsn(Opcodes.IFLE, notMatch_);
                                        mw.visitVarInsn(21, context.var("matchedCount"));
                                        mw.visitInsn(4);
                                        mw.visitInsn(96);
                                        mw.visitVarInsn(54, context.var("matchedCount"));
                                        mw.visitVarInsn(25, context.var("lexer"));
                                        mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                                        mw.visitLdcInsn(4);
                                        mw.visitJumpInsn(Opcodes.IF_ICMPEQ, end_3);
                                        mw.visitLabel(notMatch_);
                                        if (i4 != fieldListSize2 - 1) {
                                            mw.visitVarInsn(25, context.var("lexer"));
                                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                                            mw.visitLdcInsn(4);
                                            mw.visitJumpInsn(Opcodes.IF_ICMPNE, reset_);
                                            fieldListSize = fieldListSize2;
                                            end_ = end_3;
                                        } else {
                                            fieldListSize = fieldListSize2;
                                            end_ = end_3;
                                        }
                                    } else {
                                        fieldListSize = fieldListSize2;
                                        end_ = end_3;
                                        _deserialze_list_obj(context, mw, reset_, fieldInfo3, fieldClass3, itemClass, i4);
                                        if (i4 == fieldListSize - 1) {
                                            _deserialize_endCheck(context, mw, reset_);
                                        }
                                    }
                                } else {
                                    fieldListSize = fieldListSize2;
                                    end_ = end_3;
                                    _deserialze_obj(context, mw, reset_, fieldInfo3, fieldClass3, i4);
                                    if (i4 == fieldListSize - 1) {
                                        _deserialize_endCheck(context, mw, reset_);
                                    }
                                }
                                i4++;
                                end_3 = end_;
                                fieldListSize2 = fieldListSize;
                                end_2 = return_;
                            }
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            Label flag_2 = new Label();
                            mw.visitJumpInsn(Opcodes.IFLE, flag_2);
                            _setFlag(mw, context, i4);
                            mw.visitLabel(flag_2);
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            mw.visitInsn(89);
                            mw.visitVarInsn(54, context.var("matchStat"));
                            mw.visitLdcInsn(-1);
                            mw.visitJumpInsn(Opcodes.IF_ICMPEQ, reset_);
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            mw.visitJumpInsn(Opcodes.IFLE, notMatch_);
                            mw.visitVarInsn(21, context.var("matchedCount"));
                            mw.visitInsn(4);
                            mw.visitInsn(96);
                            mw.visitVarInsn(54, context.var("matchedCount"));
                            mw.visitVarInsn(25, context.var("lexer"));
                            mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                            mw.visitLdcInsn(4);
                            mw.visitJumpInsn(Opcodes.IF_ICMPEQ, end_3);
                            mw.visitLabel(notMatch_);
                            if (i4 != fieldListSize2 - 1) {
                            }
                            i4++;
                            end_3 = end_;
                            fieldListSize2 = fieldListSize;
                            end_2 = return_;
                        }
                    }
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    Label flag_22 = new Label();
                    mw.visitJumpInsn(Opcodes.IFLE, flag_22);
                    _setFlag(mw, context, i4);
                    mw.visitLabel(flag_22);
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitInsn(89);
                    mw.visitVarInsn(54, context.var("matchStat"));
                    mw.visitLdcInsn(-1);
                    mw.visitJumpInsn(Opcodes.IF_ICMPEQ, reset_);
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitJumpInsn(Opcodes.IFLE, notMatch_);
                    mw.visitVarInsn(21, context.var("matchedCount"));
                    mw.visitInsn(4);
                    mw.visitInsn(96);
                    mw.visitVarInsn(54, context.var("matchedCount"));
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                    mw.visitLdcInsn(4);
                    mw.visitJumpInsn(Opcodes.IF_ICMPEQ, end_3);
                    mw.visitLabel(notMatch_);
                    if (i4 != fieldListSize2 - 1) {
                    }
                    i4++;
                    end_3 = end_;
                    fieldListSize2 = fieldListSize;
                    end_2 = return_;
                }
                return_ = end_2;
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                Label flag_222 = new Label();
                mw.visitJumpInsn(Opcodes.IFLE, flag_222);
                _setFlag(mw, context, i4);
                mw.visitLabel(flag_222);
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                mw.visitInsn(89);
                mw.visitVarInsn(54, context.var("matchStat"));
                mw.visitLdcInsn(-1);
                mw.visitJumpInsn(Opcodes.IF_ICMPEQ, reset_);
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                mw.visitJumpInsn(Opcodes.IFLE, notMatch_);
                mw.visitVarInsn(21, context.var("matchedCount"));
                mw.visitInsn(4);
                mw.visitInsn(96);
                mw.visitVarInsn(54, context.var("matchedCount"));
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(Opcodes.GETFIELD, JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(4);
                mw.visitJumpInsn(Opcodes.IF_ICMPEQ, end_3);
                mw.visitLabel(notMatch_);
                if (i4 != fieldListSize2 - 1) {
                }
                i4++;
                end_3 = end_;
                fieldListSize2 = fieldListSize;
                end_2 = return_;
            }
            mw.visitLabel(end_3);
            if (!context.clazz.isInterface() && !Modifier.isAbstract(context.clazz.getModifiers())) {
                _batchSet(context, mw);
            }
            mw.visitLabel(end_2);
            _setContext(context, mw);
            mw.visitVarInsn(25, context.var("instance"));
            Method buildMethod = context.beanInfo.buildMethod;
            if (buildMethod != null) {
                i = Opcodes.INVOKEVIRTUAL;
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(context.getInstClass()), buildMethod.getName(), "()" + ASMUtils.desc(buildMethod.getReturnType()));
            } else {
                i = Opcodes.INVOKEVIRTUAL;
            }
            mw.visitInsn(Opcodes.ARETURN);
            mw.visitLabel(reset_);
            _batchSet(context, mw);
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitVarInsn(25, context.var("instance"));
            int i5 = 4;
            mw.visitVarInsn(21, 4);
            int flagSize = fieldListSize2 / 32;
            if (!(fieldListSize2 == 0 || fieldListSize2 % 32 == 0)) {
                flagSize++;
            }
            if (flagSize == 1) {
                mw.visitInsn(4);
            } else {
                mw.visitIntInsn(16, flagSize);
            }
            mw.visitIntInsn(Opcodes.NEWARRAY, 10);
            int i6 = 0;
            while (i6 < flagSize) {
                mw.visitInsn(89);
                if (i6 == 0) {
                    mw.visitInsn(3);
                } else if (i6 == 1) {
                    mw.visitInsn(i5);
                } else {
                    mw.visitIntInsn(16, i6);
                }
                mw.visitVarInsn(21, context.var("_asm_flag_" + i6));
                mw.visitInsn(79);
                i6++;
                i5 = 4;
            }
            mw.visitMethodInsn(i, ASMUtils.type(JavaBeanDeserializer.class), "parseRest", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;Ljava/lang/Object;I[I)Ljava/lang/Object;");
            mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(context.clazz));
            mw.visitInsn(Opcodes.ARETURN);
            mw.visitLabel(super_2);
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitVarInsn(21, 4);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(JavaBeanDeserializer.class), "deserialze", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;I)Ljava/lang/Object;");
            mw.visitInsn(Opcodes.ARETURN);
            mw.visitMaxs(10, context.variantIndex);
            mw.visitEnd();
        }
    }

    private void defineVarLexer(Context context, MethodVisitor mw) {
        mw.visitVarInsn(25, 1);
        mw.visitFieldInsn(Opcodes.GETFIELD, DefaultJSONParser, "lexer", ASMUtils.desc(JSONLexer.class));
        mw.visitTypeInsn(Opcodes.CHECKCAST, JSONLexerBase);
        mw.visitVarInsn(58, context.var("lexer"));
    }

    private void _createInstance(Context context, MethodVisitor mw) {
        Constructor<?> defaultConstructor = context.beanInfo.defaultConstructor;
        if (Modifier.isPublic(defaultConstructor.getModifiers())) {
            mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(context.getInstClass()));
            mw.visitInsn(89);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(defaultConstructor.getDeclaringClass()), "<init>", "()V");
            mw.visitVarInsn(58, context.var("instance"));
            return;
        }
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(Opcodes.GETFIELD, ASMUtils.type(JavaBeanDeserializer.class), "clazz", "Ljava/lang/Class;");
        String type = ASMUtils.type(JavaBeanDeserializer.class);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, type, "createInstance", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;)Ljava/lang/Object;");
        mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(context.getInstClass()));
        mw.visitVarInsn(58, context.var("instance"));
    }

    private void _batchSet(Context context, MethodVisitor mw) {
        _batchSet(context, mw, true);
    }

    private void _batchSet(Context context, MethodVisitor mw, boolean flag) {
        int size = context.fieldInfoList.length;
        for (int i = 0; i < size; i++) {
            Label notSet_ = new Label();
            if (flag) {
                _isFlag(mw, context, i, notSet_);
            }
            _loadAndSet(context, mw, context.fieldInfoList[i]);
            if (flag) {
                mw.visitLabel(notSet_);
            }
        }
    }

    private void _loadAndSet(Context context, MethodVisitor mw, FieldInfo fieldInfo) {
        Class<?> fieldClass = fieldInfo.fieldClass;
        Type fieldType = fieldInfo.fieldType;
        if (fieldClass == Boolean.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(21, context.var(fieldInfo.name + "_asm"));
            _set(context, mw, fieldInfo);
        } else if (fieldClass == Byte.TYPE || fieldClass == Short.TYPE || fieldClass == Integer.TYPE || fieldClass == Character.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(21, context.var(fieldInfo.name + "_asm"));
            _set(context, mw, fieldInfo);
        } else if (fieldClass == Long.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(22, context.var(fieldInfo.name + "_asm", 2));
            if (fieldInfo.method != null) {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(context.getInstClass()), fieldInfo.method.getName(), ASMUtils.desc(fieldInfo.method));
                if (!fieldInfo.method.getReturnType().equals(Void.TYPE)) {
                    mw.visitInsn(87);
                    return;
                }
                return;
            }
            mw.visitFieldInsn(Opcodes.PUTFIELD, ASMUtils.type(fieldInfo.declaringClass), fieldInfo.field.getName(), ASMUtils.desc(fieldInfo.fieldClass));
        } else if (fieldClass == Float.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(23, context.var(fieldInfo.name + "_asm"));
            _set(context, mw, fieldInfo);
        } else if (fieldClass == Double.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(24, context.var(fieldInfo.name + "_asm", 2));
            _set(context, mw, fieldInfo);
        } else if (fieldClass == String.class) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
            _set(context, mw, fieldInfo);
        } else if (fieldClass.isEnum()) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
            _set(context, mw, fieldInfo);
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            mw.visitVarInsn(25, context.var("instance"));
            if (TypeUtils.getCollectionItemClass(fieldType) == String.class) {
                mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
                mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass));
            } else {
                mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
            }
            _set(context, mw, fieldInfo);
        } else {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
            _set(context, mw, fieldInfo);
        }
    }

    private void _set(Context context, MethodVisitor mw, FieldInfo fieldInfo) {
        Method method = fieldInfo.method;
        if (method != null) {
            mw.visitMethodInsn(method.getDeclaringClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, ASMUtils.type(fieldInfo.declaringClass), method.getName(), ASMUtils.desc(method));
            if (!fieldInfo.method.getReturnType().equals(Void.TYPE)) {
                mw.visitInsn(87);
                return;
            }
            return;
        }
        mw.visitFieldInsn(Opcodes.PUTFIELD, ASMUtils.type(fieldInfo.declaringClass), fieldInfo.field.getName(), ASMUtils.desc(fieldInfo.fieldClass));
    }

    private void _setContext(Context context, MethodVisitor mw) {
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var("context"));
        String str = DefaultJSONParser;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "setContext", "(" + ASMUtils.desc(ParseContext.class) + ")V");
        Label endIf_ = new Label();
        mw.visitVarInsn(25, context.var("childContext"));
        mw.visitJumpInsn(Opcodes.IFNULL, endIf_);
        mw.visitVarInsn(25, context.var("childContext"));
        mw.visitVarInsn(25, context.var("instance"));
        mw.visitFieldInsn(Opcodes.PUTFIELD, ASMUtils.type(ParseContext.class), "object", "Ljava/lang/Object;");
        mw.visitLabel(endIf_);
    }

    private void _deserialize_endCheck(Context context, MethodVisitor mw, Label reset_) {
        mw.visitIntInsn(21, context.var("matchedCount"));
        mw.visitJumpInsn(Opcodes.IFLE, reset_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(13);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, reset_);
        _quickNextTokenComma(context, mw);
    }

    private void _deserialze_list_obj(Context context, MethodVisitor mw, Label reset_, FieldInfo fieldInfo, Class<?> fieldClass, Class<?> itemType, int i) {
        Label _end_if = new Label();
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "matchField", "([C)Z");
        mw.visitJumpInsn(Opcodes.IFEQ, _end_if);
        _setFlag(mw, context, i);
        Label valueNotNull_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(8);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, valueNotNull_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(16);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, _end_if);
        mw.visitLabel(valueNotNull_);
        Label storeCollection_ = new Label();
        Label endSet_ = new Label();
        Label lbacketNormal_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(21);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, endSet_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(14);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "(I)V");
        _newCollection(mw, fieldClass, i, true);
        mw.visitJumpInsn(Opcodes.GOTO, storeCollection_);
        mw.visitLabel(endSet_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(14);
        mw.visitJumpInsn(Opcodes.IF_ICMPEQ, lbacketNormal_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(12);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, reset_);
        _newCollection(mw, fieldClass, i, false);
        mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
        _getCollectionFieldItemDeser(context, mw, fieldInfo, itemType);
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(itemType)));
        mw.visitInsn(3);
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        String type = ASMUtils.type(ObjectDeserializer.class);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, type, "deserialze", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitVarInsn(58, context.var("list_item_value"));
        mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
        mw.visitVarInsn(25, context.var("list_item_value"));
        if (fieldClass.isInterface()) {
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, ASMUtils.type(fieldClass), "add", "(Ljava/lang/Object;)Z");
        } else {
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(fieldClass), "add", "(Ljava/lang/Object;)Z");
        }
        mw.visitInsn(87);
        mw.visitJumpInsn(Opcodes.GOTO, _end_if);
        mw.visitLabel(lbacketNormal_);
        _newCollection(mw, fieldClass, i, false);
        mw.visitLabel(storeCollection_);
        mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
        boolean isPrimitive = ParserConfig.isPrimitive2(fieldInfo.fieldClass);
        _getCollectionFieldItemDeser(context, mw, fieldInfo, itemType);
        if (isPrimitive) {
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, ASMUtils.type(ObjectDeserializer.class), "getFastMatchToken", "()I");
            mw.visitVarInsn(54, context.var("fastMatchToken"));
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitVarInsn(21, context.var("fastMatchToken"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "(I)V");
        } else {
            mw.visitInsn(87);
            mw.visitLdcInsn(12);
            mw.visitVarInsn(54, context.var("fastMatchToken"));
            _quickNextToken(context, mw, 12);
        }
        mw.visitVarInsn(25, 1);
        String str = DefaultJSONParser;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str, "getContext", "()" + ASMUtils.desc(ParseContext.class));
        mw.visitVarInsn(58, context.var("listContext"));
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
        mw.visitLdcInsn(fieldInfo.name);
        String str2 = DefaultJSONParser;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, "setContext", "(Ljava/lang/Object;Ljava/lang/Object;)" + ASMUtils.desc(ParseContext.class));
        mw.visitInsn(87);
        Label loop_ = new Label();
        Label loop_end_ = new Label();
        mw.visitInsn(3);
        mw.visitVarInsn(54, context.var("i"));
        mw.visitLabel(loop_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(15);
        mw.visitJumpInsn(Opcodes.IF_ICMPEQ, loop_end_);
        mw.visitVarInsn(25, 0);
        String str3 = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str3, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(itemType)));
        mw.visitVarInsn(21, context.var("i"));
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        String type2 = ASMUtils.type(ObjectDeserializer.class);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, type2, "deserialze", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitVarInsn(58, context.var("list_item_value"));
        mw.visitIincInsn(context.var("i"), 1);
        mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
        mw.visitVarInsn(25, context.var("list_item_value"));
        if (fieldClass.isInterface()) {
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, ASMUtils.type(fieldClass), "add", "(Ljava/lang/Object;)Z");
        } else {
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(fieldClass), "add", "(Ljava/lang/Object;)Z");
        }
        mw.visitInsn(87);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DefaultJSONParser, "checkListResolve", "(Ljava/util/Collection;)V");
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(16);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, loop_);
        if (isPrimitive) {
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitVarInsn(21, context.var("fastMatchToken"));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "(I)V");
        } else {
            _quickNextToken(context, mw, 12);
        }
        mw.visitJumpInsn(Opcodes.GOTO, loop_);
        mw.visitLabel(loop_end_);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var("listContext"));
        String str4 = DefaultJSONParser;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str4, "setContext", "(" + ASMUtils.desc(ParseContext.class) + ")V");
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(15);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, reset_);
        _quickNextTokenComma(context, mw);
        mw.visitLabel(_end_if);
    }

    private void _quickNextToken(Context context, MethodVisitor mw, int token) {
        Label quickElse_ = new Label();
        Label quickEnd_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "getCurrent", "()C");
        if (token == 12) {
            mw.visitVarInsn(16, 123);
        } else if (token == 14) {
            mw.visitVarInsn(16, 91);
        } else {
            throw new IllegalStateException();
        }
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElse_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(Integer.valueOf(token));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, quickEnd_);
        mw.visitLabel(quickElse_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(Integer.valueOf(token));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "(I)V");
        mw.visitLabel(quickEnd_);
    }

    private void _quickNextTokenComma(Context context, MethodVisitor mw) {
        Label quickElse_ = new Label();
        Label quickElseIf0_ = new Label();
        Label quickElseIf1_ = new Label();
        Label quickElseIf2_ = new Label();
        Label quickEnd_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "getCurrent", "()C");
        mw.visitInsn(89);
        mw.visitVarInsn(54, context.var("ch"));
        mw.visitVarInsn(16, 44);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElseIf0_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(16);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, quickEnd_);
        mw.visitLabel(quickElseIf0_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 125);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElseIf1_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(13);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, quickEnd_);
        mw.visitLabel(quickElseIf1_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 93);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElseIf2_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(15);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, quickEnd_);
        mw.visitLabel(quickElseIf2_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 26);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, quickElse_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(20);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(Opcodes.GOTO, quickEnd_);
        mw.visitLabel(quickElse_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "nextToken", "()V");
        mw.visitLabel(quickEnd_);
    }

    private void _getCollectionFieldItemDeser(Context context, MethodVisitor mw, FieldInfo fieldInfo, Class<?> itemType) {
        Label notNull_ = new Label();
        mw.visitVarInsn(25, 0);
        String str = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        String str2 = DefaultJSONParser;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, "getConfig", "()" + ASMUtils.desc(ParserConfig.class));
        mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(itemType)));
        String type = ASMUtils.type(ParserConfig.class);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, type, "getDeserializer", "(Ljava/lang/reflect/Type;)" + ASMUtils.desc(ObjectDeserializer.class));
        String str3 = context.className;
        mw.visitFieldInsn(Opcodes.PUTFIELD, str3, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, 0);
        String str4 = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str4, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
    }

    private void _newCollection(MethodVisitor mw, Class<?> fieldClass, int i, boolean set) {
        if (fieldClass.isAssignableFrom(ArrayList.class) && !set) {
            mw.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            mw.visitInsn(89);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
        } else if (fieldClass.isAssignableFrom(LinkedList.class) && !set) {
            mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(LinkedList.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(LinkedList.class), "<init>", "()V");
        } else if (fieldClass.isAssignableFrom(HashSet.class)) {
            mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(HashSet.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(HashSet.class), "<init>", "()V");
        } else if (fieldClass.isAssignableFrom(TreeSet.class)) {
            mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(TreeSet.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(TreeSet.class), "<init>", "()V");
        } else if (fieldClass.isAssignableFrom(LinkedHashSet.class)) {
            mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(LinkedHashSet.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(LinkedHashSet.class), "<init>", "()V");
        } else if (set) {
            mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(HashSet.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(HashSet.class), "<init>", "()V");
        } else {
            mw.visitVarInsn(25, 0);
            mw.visitLdcInsn(Integer.valueOf(i));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(JavaBeanDeserializer.class), "getFieldType", "(I)Ljava/lang/reflect/Type;");
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, ASMUtils.type(TypeUtils.class), "createCollection", "(Ljava/lang/reflect/Type;)Ljava/util/Collection;");
        }
        mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass));
    }

    private void _deserialze_obj(Context context, MethodVisitor mw, Label reset_, FieldInfo fieldInfo, Class<?> fieldClass, int i) {
        Label matched_ = new Label();
        Label _end_if = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitVarInsn(25, 0);
        String str = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str, fieldInfo.name + "_asm_prefix__", "[C");
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, JSONLexerBase, "matchField", "([C)Z");
        mw.visitJumpInsn(Opcodes.IFNE, matched_);
        mw.visitInsn(1);
        mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
        mw.visitJumpInsn(Opcodes.GOTO, _end_if);
        mw.visitLabel(matched_);
        _setFlag(mw, context, i);
        mw.visitVarInsn(21, context.var("matchedCount"));
        mw.visitInsn(4);
        mw.visitInsn(96);
        mw.visitVarInsn(54, context.var("matchedCount"));
        _deserObject(context, mw, fieldInfo, fieldClass, i);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DefaultJSONParser, "getResolveStatus", "()I");
        mw.visitLdcInsn(1);
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, _end_if);
        mw.visitVarInsn(25, 1);
        String str2 = DefaultJSONParser;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, "getLastResolveTask", "()" + ASMUtils.desc(DefaultJSONParser.ResolveTask.class));
        mw.visitVarInsn(58, context.var("resolveTask"));
        mw.visitVarInsn(25, context.var("resolveTask"));
        mw.visitVarInsn(25, 1);
        String str3 = DefaultJSONParser;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str3, "getContext", "()" + ASMUtils.desc(ParseContext.class));
        mw.visitFieldInsn(Opcodes.PUTFIELD, ASMUtils.type(DefaultJSONParser.ResolveTask.class), "ownerContext", ASMUtils.desc(ParseContext.class));
        mw.visitVarInsn(25, context.var("resolveTask"));
        mw.visitVarInsn(25, 0);
        mw.visitLdcInsn(fieldInfo.name);
        String type = ASMUtils.type(JavaBeanDeserializer.class);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, type, "getFieldDeserializer", "(Ljava/lang/String;)" + ASMUtils.desc(FieldDeserializer.class));
        mw.visitFieldInsn(Opcodes.PUTFIELD, ASMUtils.type(DefaultJSONParser.ResolveTask.class), "fieldDeserializer", ASMUtils.desc(FieldDeserializer.class));
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(0);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DefaultJSONParser, "setResolveStatus", "(I)V");
        mw.visitLabel(_end_if);
    }

    private void _deserObject(Context context, MethodVisitor mw, FieldInfo fieldInfo, Class<?> fieldClass, int i) {
        _getFieldDeser(context, mw, fieldInfo);
        Label instanceOfElse_ = new Label();
        Label instanceOfEnd_ = new Label();
        if ((fieldInfo.parserFeatures & Feature.SupportArrayToBean.mask) != 0) {
            mw.visitInsn(89);
            mw.visitTypeInsn(Opcodes.INSTANCEOF, ASMUtils.type(JavaBeanDeserializer.class));
            mw.visitJumpInsn(Opcodes.IFEQ, instanceOfElse_);
            mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(JavaBeanDeserializer.class));
            mw.visitVarInsn(25, 1);
            if (fieldInfo.fieldType instanceof Class) {
                mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
            } else {
                mw.visitVarInsn(25, 0);
                mw.visitLdcInsn(Integer.valueOf(i));
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(JavaBeanDeserializer.class), "getFieldType", "(I)Ljava/lang/reflect/Type;");
            }
            mw.visitLdcInsn(fieldInfo.name);
            mw.visitLdcInsn(Integer.valueOf(fieldInfo.parserFeatures));
            String type = ASMUtils.type(JavaBeanDeserializer.class);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, type, "deserialze", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;I)Ljava/lang/Object;");
            mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass));
            mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
            mw.visitJumpInsn(Opcodes.GOTO, instanceOfEnd_);
            mw.visitLabel(instanceOfElse_);
        }
        mw.visitVarInsn(25, 1);
        if (fieldInfo.fieldType instanceof Class) {
            mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
        } else {
            mw.visitVarInsn(25, 0);
            mw.visitLdcInsn(Integer.valueOf(i));
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ASMUtils.type(JavaBeanDeserializer.class), "getFieldType", "(I)Ljava/lang/reflect/Type;");
        }
        mw.visitLdcInsn(fieldInfo.name);
        String type2 = ASMUtils.type(ObjectDeserializer.class);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, type2, "deserialze", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass));
        mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
        mw.visitLabel(instanceOfEnd_);
    }

    private void _getFieldDeser(Context context, MethodVisitor mw, FieldInfo fieldInfo) {
        Label notNull_ = new Label();
        mw.visitVarInsn(25, 0);
        String str = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str, fieldInfo.name + "_asm_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        String str2 = DefaultJSONParser;
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, str2, "getConfig", "()" + ASMUtils.desc(ParserConfig.class));
        mw.visitLdcInsn(com.alibaba.fastjson.asm.Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
        String type = ASMUtils.type(ParserConfig.class);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, type, "getDeserializer", "(Ljava/lang/reflect/Type;)" + ASMUtils.desc(ObjectDeserializer.class));
        String str3 = context.className;
        mw.visitFieldInsn(Opcodes.PUTFIELD, str3, fieldInfo.name + "_asm_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, 0);
        String str4 = context.className;
        mw.visitFieldInsn(Opcodes.GETFIELD, str4, fieldInfo.name + "_asm_deser__", ASMUtils.desc(ObjectDeserializer.class));
    }

    /* access modifiers changed from: package-private */
    public static class Context {
        static final int fieldName = 3;
        static final int parser = 1;
        static final int type = 2;
        private final JavaBeanInfo beanInfo;
        private final String className;
        private final Class<?> clazz;
        private FieldInfo[] fieldInfoList;
        private int variantIndex = -1;
        private final Map<String, Integer> variants = new HashMap();

        public Context(String className2, ParserConfig config, JavaBeanInfo beanInfo2, int initVariantIndex) {
            this.className = className2;
            this.clazz = beanInfo2.clazz;
            this.variantIndex = initVariantIndex;
            this.beanInfo = beanInfo2;
            this.fieldInfoList = beanInfo2.fields;
        }

        public Class<?> getInstClass() {
            Class<?> instClass = this.beanInfo.builderClass;
            if (instClass == null) {
                return this.clazz;
            }
            return instClass;
        }

        public int var(String name, int increment) {
            if (this.variants.get(name) == null) {
                this.variants.put(name, Integer.valueOf(this.variantIndex));
                this.variantIndex += increment;
            }
            return this.variants.get(name).intValue();
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
    }

    private void _init(ClassWriter cw, Context context) {
        int size = context.fieldInfoList.length;
        for (int i = 0; i < size; i++) {
            FieldInfo fieldInfo = context.fieldInfoList[i];
            new FieldWriter(cw, 1, fieldInfo.name + "_asm_prefix__", "[C").visitEnd();
        }
        int size2 = context.fieldInfoList.length;
        for (int i2 = 0; i2 < size2; i2++) {
            FieldInfo fieldInfo2 = context.fieldInfoList[i2];
            Class<?> fieldClass = fieldInfo2.fieldClass;
            if (!fieldClass.isPrimitive()) {
                if (Collection.class.isAssignableFrom(fieldClass)) {
                    new FieldWriter(cw, 1, fieldInfo2.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class)).visitEnd();
                } else {
                    new FieldWriter(cw, 1, fieldInfo2.name + "_asm_deser__", ASMUtils.desc(ObjectDeserializer.class)).visitEnd();
                }
            }
        }
        MethodVisitor mw = new MethodWriter(cw, 1, "<init>", "(" + ASMUtils.desc(ParserConfig.class) + ASMUtils.desc(JavaBeanInfo.class) + ")V", null, null);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        String type = ASMUtils.type(JavaBeanDeserializer.class);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, type, "<init>", "(" + ASMUtils.desc(ParserConfig.class) + ASMUtils.desc(JavaBeanInfo.class) + ")V");
        int size3 = context.fieldInfoList.length;
        for (int i3 = 0; i3 < size3; i3++) {
            FieldInfo fieldInfo3 = context.fieldInfoList[i3];
            mw.visitVarInsn(25, 0);
            mw.visitLdcInsn("\"" + fieldInfo3.name + "\":");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C");
            String str = context.className;
            mw.visitFieldInsn(Opcodes.PUTFIELD, str, fieldInfo3.name + "_asm_prefix__", "[C");
        }
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(4, 4);
        mw.visitEnd();
    }

    private void _createInstance(ClassWriter cw, Context context) {
        if (Modifier.isPublic(context.beanInfo.defaultConstructor.getModifiers())) {
            MethodVisitor mw = new MethodWriter(cw, 1, "createInstance", "(L" + DefaultJSONParser + ";Ljava/lang/reflect/Type;)Ljava/lang/Object;", null, null);
            mw.visitTypeInsn(Opcodes.NEW, ASMUtils.type(context.getInstClass()));
            mw.visitInsn(89);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(context.getInstClass()), "<init>", "()V");
            mw.visitInsn(Opcodes.ARETURN);
            mw.visitMaxs(3, 3);
            mw.visitEnd();
        }
    }
}
