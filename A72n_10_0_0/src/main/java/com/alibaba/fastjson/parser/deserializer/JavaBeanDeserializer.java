package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONLexerBase;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.JavaBeanInfo;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JavaBeanDeserializer implements ObjectDeserializer {
    private final Map<String, FieldDeserializer> alterNameFieldDeserializers;
    public final JavaBeanInfo beanInfo;
    protected final Class<?> clazz;
    private ConcurrentMap<String, Object> extraFieldDeserializers;
    private Map<String, FieldDeserializer> fieldDeserializerMap;
    private final FieldDeserializer[] fieldDeserializers;
    private transient long[] hashArray;
    private transient short[] hashArrayMapping;
    private transient long[] smartMatchHashArray;
    private transient short[] smartMatchHashArrayMapping;
    protected final FieldDeserializer[] sortedFieldDeserializers;

    public JavaBeanDeserializer(ParserConfig config, Class<?> clazz2) {
        this(config, clazz2, clazz2);
    }

    public JavaBeanDeserializer(ParserConfig config, Class<?> clazz2, Type type) {
        this(config, JavaBeanInfo.build(clazz2, type, config.propertyNamingStrategy, config.fieldBased, config.compatibleWithJavaBean, config.isJacksonCompatible()));
    }

    public JavaBeanDeserializer(ParserConfig config, JavaBeanInfo beanInfo2) {
        this.clazz = beanInfo2.clazz;
        this.beanInfo = beanInfo2;
        Map<String, FieldDeserializer> alterNameFieldDeserializers2 = null;
        this.sortedFieldDeserializers = new FieldDeserializer[beanInfo2.sortedFields.length];
        int size = beanInfo2.sortedFields.length;
        for (int i = 0; i < size; i++) {
            FieldInfo fieldInfo = beanInfo2.sortedFields[i];
            FieldDeserializer fieldDeserializer = config.createFieldDeserializer(config, beanInfo2, fieldInfo);
            this.sortedFieldDeserializers[i] = fieldDeserializer;
            if (size > 128) {
                if (this.fieldDeserializerMap == null) {
                    this.fieldDeserializerMap = new HashMap();
                }
                this.fieldDeserializerMap.put(fieldInfo.name, fieldDeserializer);
            }
            String[] strArr = fieldInfo.alternateNames;
            for (String name : strArr) {
                if (alterNameFieldDeserializers2 == null) {
                    alterNameFieldDeserializers2 = new HashMap<>();
                }
                alterNameFieldDeserializers2.put(name, fieldDeserializer);
            }
        }
        this.alterNameFieldDeserializers = alterNameFieldDeserializers2;
        this.fieldDeserializers = new FieldDeserializer[beanInfo2.fields.length];
        int size2 = beanInfo2.fields.length;
        for (int i2 = 0; i2 < size2; i2++) {
            this.fieldDeserializers[i2] = getFieldDeserializer(beanInfo2.fields[i2].name);
        }
    }

    public FieldDeserializer getFieldDeserializer(String key) {
        return getFieldDeserializer(key, null);
    }

    public FieldDeserializer getFieldDeserializer(String key, int[] setFlags) {
        FieldDeserializer fieldDeserializer;
        if (key == null) {
            return null;
        }
        if (this.fieldDeserializerMap != null && (fieldDeserializer = this.fieldDeserializerMap.get(key)) != null) {
            return fieldDeserializer;
        }
        int low = 0;
        int high = this.sortedFieldDeserializers.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = this.sortedFieldDeserializers[mid].fieldInfo.name.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else if (isSetFlag(mid, setFlags)) {
                return null;
            } else {
                return this.sortedFieldDeserializers[mid];
            }
        }
        if (this.alterNameFieldDeserializers != null) {
            return this.alterNameFieldDeserializers.get(key);
        }
        return null;
    }

    public FieldDeserializer getFieldDeserializer(long hash) {
        if (this.hashArray == null) {
            long[] hashArray2 = new long[this.sortedFieldDeserializers.length];
            for (int i = 0; i < this.sortedFieldDeserializers.length; i++) {
                hashArray2[i] = TypeUtils.fnv1a_64(this.sortedFieldDeserializers[i].fieldInfo.name);
            }
            Arrays.sort(hashArray2);
            this.hashArray = hashArray2;
        }
        int pos = Arrays.binarySearch(this.hashArray, hash);
        if (pos < 0) {
            return null;
        }
        if (this.hashArrayMapping == null) {
            short[] mapping = new short[this.hashArray.length];
            Arrays.fill(mapping, (short) -1);
            for (int i2 = 0; i2 < this.sortedFieldDeserializers.length; i2++) {
                int p = Arrays.binarySearch(this.hashArray, TypeUtils.fnv1a_64(this.sortedFieldDeserializers[i2].fieldInfo.name));
                if (p >= 0) {
                    mapping[p] = (short) i2;
                }
            }
            this.hashArrayMapping = mapping;
        }
        short s = this.hashArrayMapping[pos];
        if (s != -1) {
            return this.sortedFieldDeserializers[s];
        }
        return null;
    }

    static boolean isSetFlag(int i, int[] setFlags) {
        if (setFlags == null) {
            return false;
        }
        int flagIndex = i / 32;
        int bitIndex = i % 32;
        if (flagIndex >= setFlags.length || (setFlags[flagIndex] & (1 << bitIndex)) == 0) {
            return false;
        }
        return true;
    }

    public Object createInstance(DefaultJSONParser parser, Type type) {
        Object object;
        if ((type instanceof Class) && this.clazz.isInterface()) {
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{(Class) type}, new JSONObject());
        } else if (this.beanInfo.defaultConstructor == null && this.beanInfo.factoryMethod == null) {
            return null;
        } else {
            if (this.beanInfo.factoryMethod != null && this.beanInfo.defaultConstructorParameterSize > 0) {
                return null;
            }
            try {
                Constructor<?> constructor = this.beanInfo.defaultConstructor;
                if (this.beanInfo.defaultConstructorParameterSize != 0) {
                    ParseContext context = parser.getContext();
                    if (context == null || context.object == null) {
                        throw new JSONException("can't create non-static inner class instance.");
                    } else if (type instanceof Class) {
                        String typeName = ((Class) type).getName();
                        String parentClassName = typeName.substring(0, typeName.lastIndexOf(36));
                        Object ctxObj = context.object;
                        String parentName = ctxObj.getClass().getName();
                        Object param = null;
                        if (!parentName.equals(parentClassName)) {
                            ParseContext parentContext = context.parent;
                            if (parentContext == null || parentContext.object == null || (!"java.util.ArrayList".equals(parentName) && !"java.util.List".equals(parentName) && !"java.util.Collection".equals(parentName) && !"java.util.Map".equals(parentName) && !"java.util.HashMap".equals(parentName))) {
                                param = ctxObj;
                            } else if (parentContext.object.getClass().getName().equals(parentClassName)) {
                                param = parentContext.object;
                            }
                        } else {
                            param = ctxObj;
                        }
                        if (param == null || ((param instanceof Collection) && ((Collection) param).isEmpty())) {
                            throw new JSONException("can't create non-static inner class instance.");
                        }
                        object = constructor.newInstance(param);
                    } else {
                        throw new JSONException("can't create non-static inner class instance.");
                    }
                } else if (constructor != null) {
                    object = constructor.newInstance(new Object[0]);
                } else {
                    object = this.beanInfo.factoryMethod.invoke(null, new Object[0]);
                }
                if (parser != null && parser.lexer.isEnabled(Feature.InitStringFieldAsEmpty)) {
                    FieldInfo[] fieldInfoArr = this.beanInfo.fields;
                    for (FieldInfo fieldInfo : fieldInfoArr) {
                        if (fieldInfo.fieldClass == String.class) {
                            try {
                                fieldInfo.set(object, "");
                            } catch (Exception e) {
                                throw new JSONException("create instance error, class " + this.clazz.getName(), e);
                            }
                        }
                    }
                }
                return object;
            } catch (JSONException e2) {
                throw e2;
            } catch (Exception e3) {
                throw new JSONException("create instance error, class " + this.clazz.getName(), e3);
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return (T) deserialze(parser, type, fieldName, 0);
    }

    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName, int features) {
        return (T) deserialze(parser, type, fieldName, null, features, null);
    }

    public <T> T deserialzeArrayMapping(DefaultJSONParser parser, Type type, Object fieldName, Object object) {
        Object value;
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 14) {
            T t = (T) createInstance(parser, type);
            int i = 0;
            int size = this.sortedFieldDeserializers.length;
            while (i < size) {
                char seperator = i == size + -1 ? ']' : ',';
                FieldDeserializer fieldDeser = this.sortedFieldDeserializers[i];
                Class<?> fieldClass = fieldDeser.fieldInfo.fieldClass;
                if (fieldClass == Integer.TYPE) {
                    fieldDeser.setValue((Object) t, lexer.scanInt(seperator));
                } else if (fieldClass == String.class) {
                    fieldDeser.setValue((Object) t, lexer.scanString(seperator));
                } else if (fieldClass == Long.TYPE) {
                    fieldDeser.setValue(t, lexer.scanLong(seperator));
                } else if (fieldClass.isEnum()) {
                    char ch = lexer.getCurrent();
                    if (ch == '\"' || ch == 'n') {
                        value = lexer.scanEnum(fieldClass, parser.getSymbolTable(), seperator);
                    } else if (ch < '0' || ch > '9') {
                        value = scanEnum(lexer, seperator);
                    } else {
                        value = ((EnumDeserializer) ((DefaultFieldDeserializer) fieldDeser).getFieldValueDeserilizer(parser.getConfig())).valueOf(lexer.scanInt(seperator));
                    }
                    fieldDeser.setValue(t, value);
                } else if (fieldClass == Boolean.TYPE) {
                    fieldDeser.setValue(t, lexer.scanBoolean(seperator));
                } else if (fieldClass == Float.TYPE) {
                    fieldDeser.setValue(t, Float.valueOf(lexer.scanFloat(seperator)));
                } else if (fieldClass == Double.TYPE) {
                    fieldDeser.setValue(t, Double.valueOf(lexer.scanDouble(seperator)));
                } else if (fieldClass == Date.class && lexer.getCurrent() == '1') {
                    fieldDeser.setValue(t, new Date(lexer.scanLong(seperator)));
                } else if (fieldClass == BigDecimal.class) {
                    fieldDeser.setValue(t, lexer.scanDecimal(seperator));
                } else {
                    lexer.nextToken(14);
                    fieldDeser.setValue(t, parser.parseObject(fieldDeser.fieldInfo.fieldType, fieldDeser.fieldInfo.name));
                    int i2 = 15;
                    if (lexer.token() == 15) {
                        break;
                    }
                    if (seperator != ']') {
                        i2 = 16;
                    }
                    check(lexer, i2);
                }
                i++;
            }
            lexer.nextToken(16);
            return t;
        }
        throw new JSONException("error");
    }

    /* access modifiers changed from: protected */
    public void check(JSONLexer lexer, int token) {
        if (lexer.token() != token) {
            throw new JSONException("syntax error");
        }
    }

    /* access modifiers changed from: protected */
    public Enum<?> scanEnum(JSONLexer lexer, char seperator) {
        throw new JSONException("illegal enum. " + lexer.info());
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:335:0x050a */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r22v1 'fieldValues'  java.util.Map<java.lang.String, java.lang.Object>: [D('fieldIndex' int), D('fieldValues' java.util.Map<java.lang.String, java.lang.Object>)] */
    /* JADX INFO: Multiple debug info for r16v1 'token'  int: [D('token' int), D('childContext' com.alibaba.fastjson.parser.ParseContext)] */
    /* JADX INFO: Multiple debug info for r5v4 'typeKey'  java.lang.String: [D('lexer' com.alibaba.fastjson.parser.JSONLexerBase), D('typeKey' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r4v3 'fieldIndex'  int: [D('fieldIndex' int), D('feildAnnotation' com.alibaba.fastjson.annotation.JSONField)] */
    /* JADX INFO: Multiple debug info for r28v0 int: [D('fieldIndex' int), D('typeKey' java.lang.String)] */
    /* JADX WARN: Type inference failed for: r2v81 */
    /* JADX WARN: Type inference failed for: r2v83 */
    /* JADX WARN: Type inference failed for: r2v116, types: [int] */
    /* JADX WARN: Type inference failed for: r2v117, types: [int] */
    /* JADX WARN: Type inference failed for: r2v119, types: [int] */
    /* JADX WARN: Type inference failed for: r2v120, types: [int] */
    /* JADX WARN: Type inference failed for: r2v122, types: [int] */
    /* JADX WARN: Type inference failed for: r2v123, types: [int] */
    /* JADX WARN: Type inference failed for: r2v127, types: [boolean] */
    /* JADX WARN: Type inference failed for: r2v131 */
    /* JADX WARN: Type inference failed for: r2v132 */
    /* JADX WARN: Type inference failed for: r2v135, types: [int] */
    /* JADX WARN: Type inference failed for: r2v136, types: [int] */
    /* JADX WARN: Type inference failed for: r2v138, types: [int] */
    /* JADX WARN: Type inference failed for: r2v139, types: [int] */
    /* JADX WARN: Type inference failed for: r2v142, types: [int] */
    /* JADX WARN: Type inference failed for: r2v143, types: [int] */
    /* JADX WARN: Type inference failed for: r2v145, types: [int] */
    /* JADX WARN: Type inference failed for: r2v146, types: [int] */
    /* JADX WARN: Type inference failed for: r2v166 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:342:?, code lost:
        r12.nextToken(16);
        r14 = r7;
        r1 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:343:0x0529, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:344:0x052a, code lost:
        r14 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:356:0x0568, code lost:
        r12.nextTokenWithColon(4);
        r15 = r12.token();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:357:0x0571, code lost:
        if (r15 != 4) goto L_0x0660;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:358:0x0573, code lost:
        r14 = r12.stringVal();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:359:0x057d, code lost:
        if ("@".equals(r14) == false) goto L_0x0589;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:360:0x057f, code lost:
        r15 = (T) r7.object;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:361:0x0585, code lost:
        r15 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:363:0x058f, code lost:
        if ("..".equals(r14) == false) goto L_0x05af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:364:0x0591, code lost:
        r15 = r7.parent;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:365:0x0597, code lost:
        if (r15.object == null) goto L_0x059e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:366:0x0599, code lost:
        r0 = r15.object;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:367:0x059e, code lost:
        r54.addResolveTask(new com.alibaba.fastjson.parser.DefaultJSONParser.ResolveTask(r15, r14));
        r54.resolveStatus = 1;
        r0 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:368:0x05ad, code lost:
        r15 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:370:0x05b9, code lost:
        if ("$".equals(r14) == false) goto L_0x05de;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:371:0x05bb, code lost:
        r0 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:373:0x05be, code lost:
        if (r0.parent == null) goto L_0x05c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:374:0x05c0, code lost:
        r0 = r0.parent;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:376:0x05c6, code lost:
        if (r0.object == null) goto L_0x05cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:377:0x05c8, code lost:
        r29 = r0.object;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:378:0x05cd, code lost:
        r54.addResolveTask(new com.alibaba.fastjson.parser.DefaultJSONParser.ResolveTask(r0, r14));
        r54.resolveStatus = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:380:0x05db, code lost:
        r15 = (T) r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:382:0x05e4, code lost:
        if (r14.indexOf(92) <= 0) goto L_0x061a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:383:0x05e6, code lost:
        r4 = new java.lang.StringBuilder();
        r26 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:384:0x05ed, code lost:
        r15 = r26;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:385:0x05f3, code lost:
        if (r15 >= r14.length()) goto L_0x0612;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:386:0x05f5, code lost:
        r0 = r14.charAt(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:387:0x05fd, code lost:
        if (r0 != '\\') goto L_0x0607;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:388:0x05ff, code lost:
        r15 = r15 + 1;
        r0 = r14.charAt(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:389:0x0607, code lost:
        r4.append(r0);
        r26 = r15 + 1;
        r6 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:390:0x0612, code lost:
        r14 = r4.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:392:0x061c, code lost:
        r0 = r54.resolveReference(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:393:0x0620, code lost:
        if (r0 == null) goto L_0x0625;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:394:0x0622, code lost:
        r15 = (T) r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:395:0x0625, code lost:
        r54.addResolveTask(new com.alibaba.fastjson.parser.DefaultJSONParser.ResolveTask(r7, r14));
        r54.resolveStatus = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:398:?, code lost:
        r12.nextToken(13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:399:0x063b, code lost:
        if (r12.token() != 13) goto L_0x064e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:400:0x063d, code lost:
        r12.nextToken(16);
        r54.setContext(r7, r15, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:401:0x0646, code lost:
        if (r3 == null) goto L_0x064a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:402:0x0648, code lost:
        r3.object = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:403:0x064a, code lost:
        r54.setContext(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:404:0x064d, code lost:
        return (T) r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:406:0x0655, code lost:
        throw new com.alibaba.fastjson.JSONException("illegal ref");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:407:0x0656, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:408:0x0657, code lost:
        r14 = r7;
        r1 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:410:0x0680, code lost:
        throw new com.alibaba.fastjson.JSONException("illegal ref, " + com.alibaba.fastjson.parser.JSONToken.name(r15));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:424:0x06c2, code lost:
        r4 = getSeeAlso(r13, r53.beanInfo, r0);
        r6 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:425:0x06c9, code lost:
        if (r4 != null) goto L_0x06ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:428:?, code lost:
        r6 = r13.checkAutoType(r0, com.alibaba.fastjson.util.TypeUtils.getClass(r55), r12.getFeatures());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:429:0x06e4, code lost:
        r4 = r54.getConfig().getDeserializer(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:430:0x06e6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:431:0x06e7, code lost:
        r14 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:434:?, code lost:
        r1 = (T) r4.deserialze(r54, r6, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:435:0x06f6, code lost:
        if ((r4 instanceof com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer) == false) goto L_0x0721;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:438:?, code lost:
        r14 = (com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer) r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:439:0x06fb, code lost:
        if (r5 == null) goto L_0x0721;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:441:0x0701, code lost:
        r46 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:443:?, code lost:
        r14.getFieldDeserializer(r5).setValue((java.lang.Object) r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:444:0x070b, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:445:0x070c, code lost:
        r14 = r7;
        r1 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:446:0x0714, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:447:0x0715, code lost:
        r14 = r7;
        r1 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:449:0x0723, code lost:
        if (r3 == null) goto L_0x072a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:450:0x0725, code lost:
        r3.object = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:452:0x072c, code lost:
        r54.setContext(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:453:0x072f, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:543:?, code lost:
        r12.nextToken(16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:544:0x08de, code lost:
        r1 = r0;
        r3 = r16;
        r29 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:773:0x0c5d, code lost:
        throw new com.alibaba.fastjson.JSONException("syntax error, unexpect token " + com.alibaba.fastjson.parser.JSONToken.name(r5.token()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:784:0x0c8f, code lost:
        r3.object = r1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x04c2 A[Catch:{ all -> 0x0495 }] */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x04c8 A[Catch:{ all -> 0x0495 }] */
    /* JADX WARNING: Removed duplicated region for block: B:335:0x050a A[SYNTHETIC, Splitter:B:335:0x050a] */
    /* JADX WARNING: Removed duplicated region for block: B:468:0x079e A[Catch:{ all -> 0x0730, all -> 0x07d6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:470:0x07ac A[ADDED_TO_REGION, Catch:{ all -> 0x0730, all -> 0x07d6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:490:0x07e7  */
    /* JADX WARNING: Removed duplicated region for block: B:522:0x086e  */
    /* JADX WARNING: Removed duplicated region for block: B:539:0x08cc  */
    /* JADX WARNING: Removed duplicated region for block: B:540:0x08d2  */
    /* JADX WARNING: Removed duplicated region for block: B:714:0x0b24 A[SYNTHETIC, Splitter:B:714:0x0b24] */
    /* JADX WARNING: Removed duplicated region for block: B:784:0x0c8f  */
    /* JADX WARNING: Unknown variable types count: 16 */
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName, Object object, int features, int[] setFlags) {
        ParseContext context;
        Exception e;
        int i;
        int fieldIndex;
        FieldInfo fieldInfo;
        FieldDeserializer fieldDeser;
        Object object2;
        ParserConfig config;
        JSONLexerBase lexer;
        int token;
        int notMatchCount;
        String typeKey;
        char c;
        int fieldIndex2;
        ParseContext childContext;
        Type type2;
        JSONLexerBase lexer2;
        boolean z;
        int intVal;
        Map<String, Object> fieldValues;
        FieldInfo fieldInfo2;
        FieldDeserializer fieldDeser2;
        char c2;
        Object fieldValue;
        Object object3;
        int token2;
        Map<String, Object> fieldValues2;
        Object object4;
        Object[] params;
        Exception e2;
        ParserConfig config2;
        int token3;
        long param;
        Object param2;
        Class<?> fieldClass;
        JSONField feildAnnotation;
        Object object5;
        Object object6;
        Object object7;
        FieldInfo fieldInfo3;
        Class<?> fieldClass2;
        int[] iArr;
        Object fieldValue2;
        Object fieldValue3;
        int i2;
        Object fieldValue4;
        int i3;
        Object fieldValue5;
        Object fieldValue6;
        Object fieldValue7;
        int i4;
        ?? r2;
        Object obj;
        boolean matchField;
        Type type3 = type;
        Object obj2 = fieldName;
        Object object8 = object;
        if (type3 == JSON.class || type3 == JSONObject.class) {
            return (T) parser.parse();
        }
        JSONLexerBase lexer3 = (JSONLexerBase) parser.lexer;
        ParserConfig config3 = parser.getConfig();
        int token4 = lexer3.token();
        int i5 = 16;
        if (token4 == 8) {
            lexer3.nextToken(16);
            return null;
        }
        ParseContext context2 = parser.getContext();
        if (!(object8 == null || context2 == null)) {
            context2 = context2.parent;
        }
        ParseContext context3 = context2;
        ParseContext childContext2 = null;
        Object obj3 = null;
        if (token4 == 13) {
            try {
                lexer3.nextToken(16);
                Object object9 = object8 == null ? (T) createInstance(parser, type) : (T) object8;
                if (0 != 0) {
                    childContext2.object = object9;
                }
                parser.setContext(context3);
                return (T) object9;
            } catch (Throwable th) {
                e = th;
                context = context3;
                object8 = object8;
                if (childContext2 != null) {
                }
                parser.setContext(context);
                throw e;
            }
        } else {
            if (token4 == 14) {
                int mask = Feature.SupportArrayToBean.mask;
                if (((this.beanInfo.parserFeatures & mask) == 0 && !lexer3.isEnabled(Feature.SupportArrayToBean) && (features & mask) == 0) ? false : true) {
                    T t = (T) deserialzeArrayMapping(parser, type, fieldName, object);
                    if (0 != 0) {
                        childContext2.object = object8;
                    }
                    parser.setContext(context3);
                    return t;
                }
            }
            if (token4 == 12 || token4 == 16) {
                Map<String, Object> fieldValues3 = null;
                try {
                    if (parser.resolveStatus == 2) {
                        i = 0;
                        parser.resolveStatus = 0;
                    } else {
                        i = 0;
                    }
                    String typeKey2 = this.beanInfo.typeKey;
                    int fieldIndex3 = 0;
                    int token5 = token4;
                    int notMatchCount2 = i;
                    int[] setFlags2 = setFlags;
                    while (true) {
                        String key = null;
                        Class<?> fieldClass3 = null;
                        JSONField feildAnnotation2 = null;
                        boolean customDeserilizer = false;
                        try {
                            if (fieldIndex3 >= this.sortedFieldDeserializers.length || notMatchCount2 >= i5) {
                                fieldIndex = fieldIndex3;
                                fieldDeser = null;
                                fieldInfo = null;
                            } else {
                                try {
                                    FieldDeserializer fieldDeser3 = this.sortedFieldDeserializers[fieldIndex3];
                                    fieldInfo = fieldDeser3.fieldInfo;
                                    fieldClass3 = fieldInfo.fieldClass;
                                    feildAnnotation2 = fieldInfo.getAnnotation();
                                    if (feildAnnotation2 != null && (fieldDeser3 instanceof DefaultFieldDeserializer)) {
                                        customDeserilizer = ((DefaultFieldDeserializer) fieldDeser3).customDeserilizer;
                                    }
                                    fieldIndex = fieldIndex3;
                                    fieldDeser = fieldDeser3;
                                } catch (Throwable th2) {
                                    e = th2;
                                    context = context3;
                                    if (childContext2 != null) {
                                    }
                                    parser.setContext(context);
                                    throw e;
                                }
                            }
                            Class<?> fieldClass4 = fieldClass3;
                            boolean matchField2 = false;
                            boolean valueParsed = false;
                            Object fieldValue8 = null;
                            object2 = object8;
                            int[] setFlags3 = setFlags2;
                            if (fieldDeser != null) {
                                try {
                                    char[] name_chars = fieldInfo.name_chars;
                                    if (!customDeserilizer || !(matchField = lexer3.matchField(name_chars))) {
                                        if (fieldClass4 == Integer.TYPE) {
                                            fieldDeser2 = fieldDeser;
                                            fieldInfo2 = fieldInfo;
                                        } else if (fieldClass4 == Integer.class) {
                                            fieldDeser2 = fieldDeser;
                                            fieldInfo2 = fieldInfo;
                                        } else {
                                            try {
                                                if (fieldClass4 == Long.TYPE) {
                                                    fieldDeser2 = fieldDeser;
                                                    fieldInfo2 = fieldInfo;
                                                } else if (fieldClass4 == Long.class) {
                                                    fieldDeser2 = fieldDeser;
                                                    fieldInfo2 = fieldInfo;
                                                } else if (fieldClass4 == String.class) {
                                                    fieldValue8 = lexer3.scanFieldString(name_chars);
                                                    ?? r22 = lexer3.matchStat;
                                                    if (r22 > 0) {
                                                        matchField2 = true;
                                                        valueParsed = true;
                                                        obj = r22;
                                                    } else {
                                                        ?? r23 = lexer3.matchStat;
                                                        obj = r23;
                                                        if (r23 == -2) {
                                                            notMatchCount2++;
                                                            notMatchCount = notMatchCount2;
                                                            context = context3;
                                                            config = config3;
                                                            token = token5;
                                                            fieldValues = fieldValues3;
                                                            fieldIndex2 = fieldIndex;
                                                            object8 = object2;
                                                            intVal = 16;
                                                            z = false;
                                                            type2 = type;
                                                            c = 5;
                                                            childContext = childContext2;
                                                            typeKey = typeKey2;
                                                            lexer2 = lexer3;
                                                            int fieldIndex4 = fieldIndex2 + 1;
                                                            fieldValues3 = fieldValues;
                                                            lexer3 = lexer2;
                                                            context3 = context;
                                                            childContext2 = childContext;
                                                            typeKey2 = typeKey;
                                                            notMatchCount2 = notMatchCount;
                                                            token5 = token;
                                                            config3 = config;
                                                            obj2 = fieldName;
                                                            fieldIndex3 = fieldIndex4;
                                                            type3 = type2;
                                                            i5 = intVal;
                                                            setFlags2 = setFlags3;
                                                        }
                                                    }
                                                } else if (fieldClass4 == Date.class && fieldInfo.format == null) {
                                                    fieldValue8 = lexer3.scanFieldDate(name_chars);
                                                    ?? r24 = lexer3.matchStat;
                                                    if (r24 > 0) {
                                                        matchField2 = true;
                                                        valueParsed = true;
                                                        obj = r24;
                                                    } else {
                                                        ?? r25 = lexer3.matchStat;
                                                        obj = r25;
                                                        if (r25 == -2) {
                                                            notMatchCount2++;
                                                            notMatchCount = notMatchCount2;
                                                            context = context3;
                                                            config = config3;
                                                            token = token5;
                                                            fieldValues = fieldValues3;
                                                            fieldIndex2 = fieldIndex;
                                                            object8 = object2;
                                                            intVal = 16;
                                                            z = false;
                                                            type2 = type;
                                                            c = 5;
                                                            childContext = childContext2;
                                                            typeKey = typeKey2;
                                                            lexer2 = lexer3;
                                                            int fieldIndex42 = fieldIndex2 + 1;
                                                            fieldValues3 = fieldValues;
                                                            lexer3 = lexer2;
                                                            context3 = context;
                                                            childContext2 = childContext;
                                                            typeKey2 = typeKey;
                                                            notMatchCount2 = notMatchCount;
                                                            token5 = token;
                                                            config3 = config;
                                                            obj2 = fieldName;
                                                            fieldIndex3 = fieldIndex42;
                                                            type3 = type2;
                                                            i5 = intVal;
                                                            setFlags2 = setFlags3;
                                                        }
                                                    }
                                                } else if (fieldClass4 == BigDecimal.class) {
                                                    fieldValue8 = lexer3.scanFieldDecimal(name_chars);
                                                    ?? r26 = lexer3.matchStat;
                                                    if (r26 > 0) {
                                                        matchField2 = true;
                                                        valueParsed = true;
                                                        obj = r26;
                                                    } else {
                                                        ?? r27 = lexer3.matchStat;
                                                        obj = r27;
                                                        if (r27 == -2) {
                                                            notMatchCount2++;
                                                            notMatchCount = notMatchCount2;
                                                            context = context3;
                                                            config = config3;
                                                            token = token5;
                                                            fieldValues = fieldValues3;
                                                            fieldIndex2 = fieldIndex;
                                                            object8 = object2;
                                                            intVal = 16;
                                                            z = false;
                                                            type2 = type;
                                                            c = 5;
                                                            childContext = childContext2;
                                                            typeKey = typeKey2;
                                                            lexer2 = lexer3;
                                                            int fieldIndex422 = fieldIndex2 + 1;
                                                            fieldValues3 = fieldValues;
                                                            lexer3 = lexer2;
                                                            context3 = context;
                                                            childContext2 = childContext;
                                                            typeKey2 = typeKey;
                                                            notMatchCount2 = notMatchCount;
                                                            token5 = token;
                                                            config3 = config;
                                                            obj2 = fieldName;
                                                            fieldIndex3 = fieldIndex422;
                                                            type3 = type2;
                                                            i5 = intVal;
                                                            setFlags2 = setFlags3;
                                                        }
                                                    }
                                                } else if (fieldClass4 == BigInteger.class) {
                                                    fieldValue8 = lexer3.scanFieldBigInteger(name_chars);
                                                    ?? r28 = lexer3.matchStat;
                                                    if (r28 > 0) {
                                                        matchField2 = true;
                                                        valueParsed = true;
                                                        obj = r28;
                                                    } else {
                                                        ?? r29 = lexer3.matchStat;
                                                        obj = r29;
                                                        if (r29 == -2) {
                                                            notMatchCount2++;
                                                            notMatchCount = notMatchCount2;
                                                            context = context3;
                                                            config = config3;
                                                            token = token5;
                                                            fieldValues = fieldValues3;
                                                            fieldIndex2 = fieldIndex;
                                                            object8 = object2;
                                                            intVal = 16;
                                                            z = false;
                                                            type2 = type;
                                                            c = 5;
                                                            childContext = childContext2;
                                                            typeKey = typeKey2;
                                                            lexer2 = lexer3;
                                                            int fieldIndex4222 = fieldIndex2 + 1;
                                                            fieldValues3 = fieldValues;
                                                            lexer3 = lexer2;
                                                            context3 = context;
                                                            childContext2 = childContext;
                                                            typeKey2 = typeKey;
                                                            notMatchCount2 = notMatchCount;
                                                            token5 = token;
                                                            config3 = config;
                                                            obj2 = fieldName;
                                                            fieldIndex3 = fieldIndex4222;
                                                            type3 = type2;
                                                            i5 = intVal;
                                                            setFlags2 = setFlags3;
                                                        }
                                                    }
                                                } else {
                                                    if (fieldClass4 == Boolean.TYPE) {
                                                        fieldDeser2 = fieldDeser;
                                                        fieldInfo2 = fieldInfo;
                                                    } else if (fieldClass4 == Boolean.class) {
                                                        fieldDeser2 = fieldDeser;
                                                        fieldInfo2 = fieldInfo;
                                                    } else {
                                                        if (fieldClass4 == Float.TYPE) {
                                                            fieldDeser2 = fieldDeser;
                                                            fieldInfo2 = fieldInfo;
                                                        } else if (fieldClass4 == Float.class) {
                                                            fieldDeser2 = fieldDeser;
                                                            fieldInfo2 = fieldInfo;
                                                        } else {
                                                            if (fieldClass4 != Double.TYPE) {
                                                                if (fieldClass4 != Double.class) {
                                                                    if (fieldClass4.isEnum() && (parser.getConfig().getDeserializer(fieldClass4) instanceof EnumDeserializer) && (feildAnnotation2 == null || feildAnnotation2.deserializeUsing() == Void.class)) {
                                                                        ?? r210 = fieldDeser instanceof DefaultFieldDeserializer;
                                                                        if (r210 != 0) {
                                                                            ObjectDeserializer fieldValueDeserilizer = ((DefaultFieldDeserializer) fieldDeser).fieldValueDeserilizer;
                                                                            fieldValue8 = scanEnum(lexer3, name_chars, fieldValueDeserilizer);
                                                                            if (lexer3.matchStat > 0) {
                                                                                valueParsed = true;
                                                                                matchField2 = true;
                                                                                r2 = fieldValueDeserilizer;
                                                                            } else {
                                                                                r2 = -2;
                                                                                if (lexer3.matchStat == -2) {
                                                                                    notMatchCount2++;
                                                                                    notMatchCount = notMatchCount2;
                                                                                    context = context3;
                                                                                    config = config3;
                                                                                    token = token5;
                                                                                    fieldValues = fieldValues3;
                                                                                    fieldIndex2 = fieldIndex;
                                                                                    object8 = object2;
                                                                                    intVal = 16;
                                                                                    z = false;
                                                                                    type2 = type;
                                                                                    c = 5;
                                                                                    childContext = childContext2;
                                                                                    typeKey = typeKey2;
                                                                                    lexer2 = lexer3;
                                                                                    int fieldIndex42222 = fieldIndex2 + 1;
                                                                                    fieldValues3 = fieldValues;
                                                                                    lexer3 = lexer2;
                                                                                    context3 = context;
                                                                                    childContext2 = childContext;
                                                                                    typeKey2 = typeKey;
                                                                                    notMatchCount2 = notMatchCount;
                                                                                    token5 = token;
                                                                                    config3 = config;
                                                                                    obj2 = fieldName;
                                                                                    fieldIndex3 = fieldIndex42222;
                                                                                    type3 = type2;
                                                                                    i5 = intVal;
                                                                                    setFlags2 = setFlags3;
                                                                                }
                                                                            }
                                                                            obj = r2;
                                                                        } else {
                                                                            fieldDeser2 = fieldDeser;
                                                                            fieldInfo2 = fieldInfo;
                                                                            c2 = 5;
                                                                            iArr = r210;
                                                                        }
                                                                    } else if (fieldClass4 == int[].class) {
                                                                        fieldValue8 = lexer3.scanFieldIntArray(name_chars);
                                                                        ?? r211 = lexer3.matchStat;
                                                                        if (r211 > 0) {
                                                                            matchField2 = true;
                                                                            valueParsed = true;
                                                                            obj = r211;
                                                                        } else {
                                                                            ?? r212 = lexer3.matchStat;
                                                                            obj = r212;
                                                                            if (r212 == -2) {
                                                                                notMatchCount2++;
                                                                                notMatchCount = notMatchCount2;
                                                                                context = context3;
                                                                                config = config3;
                                                                                token = token5;
                                                                                fieldValues = fieldValues3;
                                                                                fieldIndex2 = fieldIndex;
                                                                                object8 = object2;
                                                                                intVal = 16;
                                                                                z = false;
                                                                                type2 = type;
                                                                                c = 5;
                                                                                childContext = childContext2;
                                                                                typeKey = typeKey2;
                                                                                lexer2 = lexer3;
                                                                                int fieldIndex422222 = fieldIndex2 + 1;
                                                                                fieldValues3 = fieldValues;
                                                                                lexer3 = lexer2;
                                                                                context3 = context;
                                                                                childContext2 = childContext;
                                                                                typeKey2 = typeKey;
                                                                                notMatchCount2 = notMatchCount;
                                                                                token5 = token;
                                                                                config3 = config;
                                                                                obj2 = fieldName;
                                                                                fieldIndex3 = fieldIndex422222;
                                                                                type3 = type2;
                                                                                i5 = intVal;
                                                                                setFlags2 = setFlags3;
                                                                            }
                                                                        }
                                                                    } else if (fieldClass4 == float[].class) {
                                                                        fieldValue8 = lexer3.scanFieldFloatArray(name_chars);
                                                                        ?? r213 = lexer3.matchStat;
                                                                        if (r213 > 0) {
                                                                            matchField2 = true;
                                                                            valueParsed = true;
                                                                            obj = r213;
                                                                        } else {
                                                                            ?? r214 = lexer3.matchStat;
                                                                            obj = r214;
                                                                            if (r214 == -2) {
                                                                                notMatchCount2++;
                                                                                notMatchCount = notMatchCount2;
                                                                                context = context3;
                                                                                config = config3;
                                                                                token = token5;
                                                                                fieldValues = fieldValues3;
                                                                                fieldIndex2 = fieldIndex;
                                                                                object8 = object2;
                                                                                intVal = 16;
                                                                                z = false;
                                                                                type2 = type;
                                                                                c = 5;
                                                                                childContext = childContext2;
                                                                                typeKey = typeKey2;
                                                                                lexer2 = lexer3;
                                                                                int fieldIndex4222222 = fieldIndex2 + 1;
                                                                                fieldValues3 = fieldValues;
                                                                                lexer3 = lexer2;
                                                                                context3 = context;
                                                                                childContext2 = childContext;
                                                                                typeKey2 = typeKey;
                                                                                notMatchCount2 = notMatchCount;
                                                                                token5 = token;
                                                                                config3 = config;
                                                                                obj2 = fieldName;
                                                                                fieldIndex3 = fieldIndex4222222;
                                                                                type3 = type2;
                                                                                i5 = intVal;
                                                                                setFlags2 = setFlags3;
                                                                            }
                                                                        }
                                                                    } else if (fieldClass4 == float[][].class) {
                                                                        fieldValue8 = lexer3.scanFieldFloatArray2(name_chars);
                                                                        ?? r215 = lexer3.matchStat;
                                                                        if (r215 > 0) {
                                                                            matchField2 = true;
                                                                            valueParsed = true;
                                                                            obj = r215;
                                                                        } else {
                                                                            ?? r216 = lexer3.matchStat;
                                                                            obj = r216;
                                                                            if (r216 == -2) {
                                                                                notMatchCount2++;
                                                                                notMatchCount = notMatchCount2;
                                                                                context = context3;
                                                                                config = config3;
                                                                                token = token5;
                                                                                fieldValues = fieldValues3;
                                                                                fieldIndex2 = fieldIndex;
                                                                                object8 = object2;
                                                                                intVal = 16;
                                                                                z = false;
                                                                                type2 = type;
                                                                                c = 5;
                                                                                childContext = childContext2;
                                                                                typeKey = typeKey2;
                                                                                lexer2 = lexer3;
                                                                                int fieldIndex42222222 = fieldIndex2 + 1;
                                                                                fieldValues3 = fieldValues;
                                                                                lexer3 = lexer2;
                                                                                context3 = context;
                                                                                childContext2 = childContext;
                                                                                typeKey2 = typeKey;
                                                                                notMatchCount2 = notMatchCount;
                                                                                token5 = token;
                                                                                config3 = config;
                                                                                obj2 = fieldName;
                                                                                fieldIndex3 = fieldIndex42222222;
                                                                                type3 = type2;
                                                                                i5 = intVal;
                                                                                setFlags2 = setFlags3;
                                                                            }
                                                                        }
                                                                    } else {
                                                                        boolean matchField3 = lexer3.matchField(name_chars);
                                                                        if (matchField3) {
                                                                            matchField2 = true;
                                                                            obj = matchField3;
                                                                        }
                                                                        notMatchCount = notMatchCount2;
                                                                        context = context3;
                                                                        config = config3;
                                                                        token = token5;
                                                                        fieldValues = fieldValues3;
                                                                        fieldIndex2 = fieldIndex;
                                                                        object8 = object2;
                                                                        intVal = 16;
                                                                        z = false;
                                                                        type2 = type;
                                                                        c = 5;
                                                                        childContext = childContext2;
                                                                        typeKey = typeKey2;
                                                                        lexer2 = lexer3;
                                                                        int fieldIndex422222222 = fieldIndex2 + 1;
                                                                        fieldValues3 = fieldValues;
                                                                        lexer3 = lexer2;
                                                                        context3 = context;
                                                                        childContext2 = childContext;
                                                                        typeKey2 = typeKey;
                                                                        notMatchCount2 = notMatchCount;
                                                                        token5 = token;
                                                                        config3 = config;
                                                                        obj2 = fieldName;
                                                                        fieldIndex3 = fieldIndex422222222;
                                                                        type3 = type2;
                                                                        i5 = intVal;
                                                                        setFlags2 = setFlags3;
                                                                    }
                                                                }
                                                            }
                                                            double doubleVal = lexer3.scanFieldDouble(name_chars);
                                                            fieldDeser2 = fieldDeser;
                                                            fieldInfo2 = fieldInfo;
                                                            if (doubleVal == 0.0d && lexer3.matchStat == 5) {
                                                                fieldValue7 = null;
                                                            } else {
                                                                fieldValue7 = Double.valueOf(doubleVal);
                                                            }
                                                            fieldValue4 = fieldValue7;
                                                            if (lexer3.matchStat > 0) {
                                                                i4 = 1;
                                                                matchField2 = true;
                                                                valueParsed = true;
                                                            } else {
                                                                int i6 = lexer3.matchStat;
                                                                i4 = i6;
                                                                if (i6 == -2) {
                                                                    notMatchCount2++;
                                                                    notMatchCount = notMatchCount2;
                                                                    context = context3;
                                                                    config = config3;
                                                                    token = token5;
                                                                    fieldValues = fieldValues3;
                                                                    fieldIndex2 = fieldIndex;
                                                                    object8 = object2;
                                                                    intVal = 16;
                                                                    z = false;
                                                                    type2 = type;
                                                                    c = 5;
                                                                    childContext = childContext2;
                                                                    typeKey = typeKey2;
                                                                    lexer2 = lexer3;
                                                                    int fieldIndex4222222222 = fieldIndex2 + 1;
                                                                    fieldValues3 = fieldValues;
                                                                    lexer3 = lexer2;
                                                                    context3 = context;
                                                                    childContext2 = childContext;
                                                                    typeKey2 = typeKey;
                                                                    notMatchCount2 = notMatchCount;
                                                                    token5 = token;
                                                                    config3 = config;
                                                                    obj2 = fieldName;
                                                                    fieldIndex3 = fieldIndex4222222222;
                                                                    type3 = type2;
                                                                    i5 = intVal;
                                                                    setFlags2 = setFlags3;
                                                                }
                                                            }
                                                            i3 = i4;
                                                            fieldValue = fieldValue4;
                                                            c2 = 5;
                                                            object3 = i3;
                                                            if (matchField2) {
                                                                try {
                                                                    String key2 = lexer3.scanSymbol(parser.symbolTable);
                                                                    if (key2 == null) {
                                                                        token2 = lexer3.token();
                                                                        if (token2 == 13) {
                                                                            break;
                                                                        }
                                                                        if (token2 == 16 && lexer3.isEnabled(Feature.AllowArbitraryCommas)) {
                                                                            notMatchCount = notMatchCount2;
                                                                            token = token2;
                                                                            childContext = childContext2;
                                                                            config = config3;
                                                                            c = c2;
                                                                            fieldValues = fieldValues3;
                                                                            fieldIndex2 = fieldIndex;
                                                                            object8 = object2;
                                                                            intVal = 16;
                                                                            z = false;
                                                                            type2 = type;
                                                                        }
                                                                        if (!("$ref" == key2 || context3 == null)) {
                                                                            break;
                                                                        }
                                                                        notMatchCount = notMatchCount2;
                                                                        feildAnnotation = feildAnnotation2;
                                                                        fieldClass = fieldClass4;
                                                                        if ((typeKey2 != null || !typeKey2.equals(key2)) && JSON.DEFAULT_TYPE_KEY != key2) {
                                                                            token = token2;
                                                                            object5 = object2;
                                                                            type2 = type;
                                                                            key = key2;
                                                                        } else {
                                                                            lexer3.nextTokenWithColon(4);
                                                                            if (lexer3.token() == 4) {
                                                                                String typeName = lexer3.stringVal();
                                                                                lexer3.nextToken(16);
                                                                                if (!typeName.equals(this.beanInfo.typeName)) {
                                                                                    if (!parser.isEnabled(Feature.IgnoreAutoType)) {
                                                                                        break;
                                                                                    }
                                                                                    token = token2;
                                                                                    object3 = object2;
                                                                                    type2 = type;
                                                                                } else {
                                                                                    token = token2;
                                                                                    object3 = object2;
                                                                                    type2 = type;
                                                                                }
                                                                                try {
                                                                                    if (lexer3.token() == 13) {
                                                                                        lexer3.nextToken();
                                                                                        object2 = object3;
                                                                                        context = context3;
                                                                                        fieldValues2 = fieldValues3;
                                                                                        break;
                                                                                    }
                                                                                    object8 = object3;
                                                                                    childContext = childContext2;
                                                                                    config = config3;
                                                                                    c = c2;
                                                                                    fieldValues = fieldValues3;
                                                                                    fieldIndex2 = fieldIndex;
                                                                                    intVal = 16;
                                                                                    z = false;
                                                                                } catch (Throwable th3) {
                                                                                    e = th3;
                                                                                    object8 = object3;
                                                                                    context = context3;
                                                                                    if (childContext2 != null) {
                                                                                    }
                                                                                    parser.setContext(context);
                                                                                    throw e;
                                                                                }
                                                                            } else {
                                                                                throw new JSONException("syntax error");
                                                                            }
                                                                        }
                                                                    } else {
                                                                        token2 = token5;
                                                                        if ("$ref" == key2) {
                                                                        }
                                                                        notMatchCount = notMatchCount2;
                                                                        feildAnnotation = feildAnnotation2;
                                                                        fieldClass = fieldClass4;
                                                                        if (typeKey2 != null) {
                                                                        }
                                                                        token = token2;
                                                                        object5 = object2;
                                                                        type2 = type;
                                                                        key = key2;
                                                                    }
                                                                    typeKey = typeKey2;
                                                                    context = context3;
                                                                    lexer2 = lexer3;
                                                                    int fieldIndex42222222222 = fieldIndex2 + 1;
                                                                    fieldValues3 = fieldValues;
                                                                    lexer3 = lexer2;
                                                                    context3 = context;
                                                                    childContext2 = childContext;
                                                                    typeKey2 = typeKey;
                                                                    notMatchCount2 = notMatchCount;
                                                                    token5 = token;
                                                                    config3 = config;
                                                                    obj2 = fieldName;
                                                                    fieldIndex3 = fieldIndex42222222222;
                                                                    type3 = type2;
                                                                    i5 = intVal;
                                                                    setFlags2 = setFlags3;
                                                                } catch (Throwable th4) {
                                                                    e = th4;
                                                                    object8 = object2;
                                                                    context = context3;
                                                                    if (childContext2 != null) {
                                                                    }
                                                                    parser.setContext(context);
                                                                    throw e;
                                                                }
                                                            } else {
                                                                notMatchCount = notMatchCount2;
                                                                feildAnnotation = feildAnnotation2;
                                                                fieldClass = fieldClass4;
                                                                object5 = object2;
                                                                type2 = type;
                                                                token = token5;
                                                            }
                                                            if (object5 == null || fieldValues3 != null) {
                                                                object6 = object5;
                                                            } else {
                                                                object8 = createInstance(parser, type);
                                                                if (object8 == null) {
                                                                    try {
                                                                        fieldValues3 = new HashMap<>(this.fieldDeserializers.length);
                                                                    } catch (Throwable th5) {
                                                                        e = th5;
                                                                        context = context3;
                                                                        if (childContext2 != null) {
                                                                        }
                                                                        parser.setContext(context);
                                                                        throw e;
                                                                    }
                                                                }
                                                                childContext2 = parser.setContext(context3, object8, obj2);
                                                                if (setFlags3 == null) {
                                                                    setFlags3 = new int[((this.fieldDeserializers.length / 32) + 1)];
                                                                }
                                                                object6 = object8;
                                                            }
                                                            childContext = childContext2;
                                                            fieldValues = fieldValues3;
                                                            if (!matchField2) {
                                                                if (!valueParsed) {
                                                                    try {
                                                                        fieldDeser2.parseField(parser, object6, type2, fieldValues);
                                                                        object7 = object6;
                                                                        c = c2;
                                                                        fieldIndex2 = fieldIndex;
                                                                    } catch (Throwable th6) {
                                                                        e = th6;
                                                                        object8 = object6;
                                                                        context = context3;
                                                                        childContext2 = childContext;
                                                                        if (childContext2 != null) {
                                                                        }
                                                                        parser.setContext(context);
                                                                        throw e;
                                                                    }
                                                                } else {
                                                                    if (object6 == null) {
                                                                        fieldInfo3 = fieldInfo2;
                                                                        fieldValues.put(fieldInfo3.name, fieldValue);
                                                                        fieldClass2 = fieldClass;
                                                                    } else {
                                                                        fieldInfo3 = fieldInfo2;
                                                                        if (fieldValue == null) {
                                                                            fieldClass2 = fieldClass;
                                                                            if (!(fieldClass2 == Integer.TYPE || fieldClass2 == Long.TYPE || fieldClass2 == Float.TYPE || fieldClass2 == Double.TYPE || fieldClass2 == Boolean.TYPE)) {
                                                                                fieldDeser2.setValue(object6, fieldValue);
                                                                            }
                                                                        } else {
                                                                            fieldClass2 = fieldClass;
                                                                            fieldDeser2.setValue(object6, fieldValue);
                                                                        }
                                                                    }
                                                                    if (setFlags3 != null) {
                                                                        int flagIndex = fieldIndex / 32;
                                                                        setFlags3[flagIndex] = setFlags3[flagIndex] | (1 << (fieldIndex % 32));
                                                                    }
                                                                    if (lexer3.matchStat == 4) {
                                                                        object7 = object6;
                                                                        context = context3;
                                                                        break;
                                                                    }
                                                                    object7 = object6;
                                                                    fieldIndex2 = fieldIndex;
                                                                    c = 5;
                                                                }
                                                                typeKey = typeKey2;
                                                                context = context3;
                                                                intVal = 16;
                                                                if (lexer3.token() != 16) {
                                                                    lexer2 = lexer3;
                                                                    config = config3;
                                                                } else if (lexer3.token() == 13) {
                                                                    try {
                                                                        break;
                                                                    } catch (Throwable th7) {
                                                                        e = th7;
                                                                        childContext2 = childContext;
                                                                        object8 = object7;
                                                                    }
                                                                } else {
                                                                    lexer = lexer3;
                                                                    config = config3;
                                                                    z = false;
                                                                    lexer2 = lexer;
                                                                    try {
                                                                        if (lexer2.token() == 18 || lexer2.token() == 1) {
                                                                        }
                                                                        object8 = object7;
                                                                        int fieldIndex422222222222 = fieldIndex2 + 1;
                                                                        fieldValues3 = fieldValues;
                                                                        lexer3 = lexer2;
                                                                        context3 = context;
                                                                        childContext2 = childContext;
                                                                        typeKey2 = typeKey;
                                                                        notMatchCount2 = notMatchCount;
                                                                        token5 = token;
                                                                        config3 = config;
                                                                        obj2 = fieldName;
                                                                        fieldIndex3 = fieldIndex422222222222;
                                                                        type3 = type2;
                                                                        i5 = intVal;
                                                                        setFlags2 = setFlags3;
                                                                    } catch (Throwable th8) {
                                                                        e = th8;
                                                                        childContext2 = childContext;
                                                                        object8 = object7;
                                                                        if (childContext2 != null) {
                                                                        }
                                                                        parser.setContext(context);
                                                                        throw e;
                                                                    }
                                                                }
                                                            } else {
                                                                fieldIndex2 = fieldIndex;
                                                                typeKey = typeKey2;
                                                                object7 = object6;
                                                                c = 5;
                                                                context = context3;
                                                                try {
                                                                    if (!parseField(parser, key, object6, type2, fieldValues, setFlags3)) {
                                                                        try {
                                                                            if (lexer3.token() == 13) {
                                                                                lexer3.nextToken();
                                                                                break;
                                                                            }
                                                                            lexer2 = lexer3;
                                                                            config = config3;
                                                                            intVal = 16;
                                                                        } catch (Throwable th9) {
                                                                            e = th9;
                                                                            childContext2 = childContext;
                                                                            object8 = object7;
                                                                            if (childContext2 != null) {
                                                                            }
                                                                            parser.setContext(context);
                                                                            throw e;
                                                                        }
                                                                    } else {
                                                                        if (lexer3.token() == 17) {
                                                                            throw new JSONException("syntax error, unexpect token ':'");
                                                                        }
                                                                        intVal = 16;
                                                                        if (lexer3.token() != 16) {
                                                                        }
                                                                    }
                                                                } catch (Throwable th10) {
                                                                    e = th10;
                                                                    childContext2 = childContext;
                                                                    object8 = object7;
                                                                    if (childContext2 != null) {
                                                                    }
                                                                    parser.setContext(context);
                                                                    throw e;
                                                                }
                                                            }
                                                            z = false;
                                                            object8 = object7;
                                                            int fieldIndex4222222222222 = fieldIndex2 + 1;
                                                            fieldValues3 = fieldValues;
                                                            lexer3 = lexer2;
                                                            context3 = context;
                                                            childContext2 = childContext;
                                                            typeKey2 = typeKey;
                                                            notMatchCount2 = notMatchCount;
                                                            token5 = token;
                                                            config3 = config;
                                                            obj2 = fieldName;
                                                            fieldIndex3 = fieldIndex4222222222222;
                                                            type3 = type2;
                                                            i5 = intVal;
                                                            setFlags2 = setFlags3;
                                                        }
                                                        float floatVal = lexer3.scanFieldFloat(name_chars);
                                                        if (floatVal == 0.0f && lexer3.matchStat == 5) {
                                                            fieldValue6 = null;
                                                        } else {
                                                            fieldValue6 = Float.valueOf(floatVal);
                                                        }
                                                        fieldValue4 = fieldValue6;
                                                        if (lexer3.matchStat > 0) {
                                                            matchField2 = true;
                                                            valueParsed = true;
                                                        } else if (lexer3.matchStat == -2) {
                                                            notMatchCount2++;
                                                            notMatchCount = notMatchCount2;
                                                            context = context3;
                                                            config = config3;
                                                            token = token5;
                                                            fieldValues = fieldValues3;
                                                            fieldIndex2 = fieldIndex;
                                                            object8 = object2;
                                                            intVal = 16;
                                                            z = false;
                                                            type2 = type;
                                                            c = 5;
                                                            childContext = childContext2;
                                                            typeKey = typeKey2;
                                                            lexer2 = lexer3;
                                                            int fieldIndex42222222222222 = fieldIndex2 + 1;
                                                            fieldValues3 = fieldValues;
                                                            lexer3 = lexer2;
                                                            context3 = context;
                                                            childContext2 = childContext;
                                                            typeKey2 = typeKey;
                                                            notMatchCount2 = notMatchCount;
                                                            token5 = token;
                                                            config3 = config;
                                                            obj2 = fieldName;
                                                            fieldIndex3 = fieldIndex42222222222222;
                                                            type3 = type2;
                                                            i5 = intVal;
                                                            setFlags2 = setFlags3;
                                                        }
                                                        i3 = floatVal;
                                                        fieldValue = fieldValue4;
                                                        c2 = 5;
                                                        object3 = i3;
                                                        if (matchField2) {
                                                        }
                                                        if (object5 == null) {
                                                        }
                                                        object6 = object5;
                                                        childContext = childContext2;
                                                        fieldValues = fieldValues3;
                                                        if (!matchField2) {
                                                        }
                                                        z = false;
                                                        object8 = object7;
                                                        int fieldIndex422222222222222 = fieldIndex2 + 1;
                                                        fieldValues3 = fieldValues;
                                                        lexer3 = lexer2;
                                                        context3 = context;
                                                        childContext2 = childContext;
                                                        typeKey2 = typeKey;
                                                        notMatchCount2 = notMatchCount;
                                                        token5 = token;
                                                        config3 = config;
                                                        obj2 = fieldName;
                                                        fieldIndex3 = fieldIndex422222222222222;
                                                        type3 = type2;
                                                        i5 = intVal;
                                                        setFlags2 = setFlags3;
                                                    }
                                                    boolean booleanVal = lexer3.scanFieldBoolean(name_chars);
                                                    if (lexer3.matchStat == 5) {
                                                        fieldValue5 = null;
                                                    } else {
                                                        fieldValue5 = Boolean.valueOf(booleanVal);
                                                    }
                                                    fieldValue4 = fieldValue5;
                                                    if (lexer3.matchStat > 0) {
                                                        matchField2 = true;
                                                        valueParsed = true;
                                                        i3 = booleanVal;
                                                    } else {
                                                        i3 = booleanVal;
                                                        if (lexer3.matchStat == -2) {
                                                            notMatchCount2++;
                                                            notMatchCount = notMatchCount2;
                                                            context = context3;
                                                            config = config3;
                                                            token = token5;
                                                            fieldValues = fieldValues3;
                                                            fieldIndex2 = fieldIndex;
                                                            object8 = object2;
                                                            intVal = 16;
                                                            z = false;
                                                            type2 = type;
                                                            c = 5;
                                                            childContext = childContext2;
                                                            typeKey = typeKey2;
                                                            lexer2 = lexer3;
                                                            int fieldIndex4222222222222222 = fieldIndex2 + 1;
                                                            fieldValues3 = fieldValues;
                                                            lexer3 = lexer2;
                                                            context3 = context;
                                                            childContext2 = childContext;
                                                            typeKey2 = typeKey;
                                                            notMatchCount2 = notMatchCount;
                                                            token5 = token;
                                                            config3 = config;
                                                            obj2 = fieldName;
                                                            fieldIndex3 = fieldIndex4222222222222222;
                                                            type3 = type2;
                                                            i5 = intVal;
                                                            setFlags2 = setFlags3;
                                                        }
                                                    }
                                                    fieldValue = fieldValue4;
                                                    c2 = 5;
                                                    object3 = i3;
                                                    if (matchField2) {
                                                    }
                                                    if (object5 == null) {
                                                    }
                                                    object6 = object5;
                                                    childContext = childContext2;
                                                    fieldValues = fieldValues3;
                                                    if (!matchField2) {
                                                    }
                                                    z = false;
                                                    object8 = object7;
                                                    int fieldIndex42222222222222222 = fieldIndex2 + 1;
                                                    fieldValues3 = fieldValues;
                                                    lexer3 = lexer2;
                                                    context3 = context;
                                                    childContext2 = childContext;
                                                    typeKey2 = typeKey;
                                                    notMatchCount2 = notMatchCount;
                                                    token5 = token;
                                                    config3 = config;
                                                    obj2 = fieldName;
                                                    fieldIndex3 = fieldIndex42222222222222222;
                                                    type3 = type2;
                                                    i5 = intVal;
                                                    setFlags2 = setFlags3;
                                                }
                                                long longVal = lexer3.scanFieldLong(name_chars);
                                                if (longVal == 0 && lexer3.matchStat == 5) {
                                                    fieldValue3 = null;
                                                } else {
                                                    fieldValue3 = Long.valueOf(longVal);
                                                }
                                                if (lexer3.matchStat > 0) {
                                                    matchField2 = true;
                                                    i2 = 1;
                                                    valueParsed = true;
                                                } else {
                                                    int i7 = lexer3.matchStat;
                                                    i2 = i7;
                                                    if (i7 == -2) {
                                                        notMatchCount2++;
                                                        notMatchCount = notMatchCount2;
                                                        context = context3;
                                                        config = config3;
                                                        token = token5;
                                                        fieldValues = fieldValues3;
                                                        fieldIndex2 = fieldIndex;
                                                        object8 = object2;
                                                        intVal = 16;
                                                        z = false;
                                                        type2 = type;
                                                        c = 5;
                                                        childContext = childContext2;
                                                        typeKey = typeKey2;
                                                        lexer2 = lexer3;
                                                        int fieldIndex422222222222222222 = fieldIndex2 + 1;
                                                        fieldValues3 = fieldValues;
                                                        lexer3 = lexer2;
                                                        context3 = context;
                                                        childContext2 = childContext;
                                                        typeKey2 = typeKey;
                                                        notMatchCount2 = notMatchCount;
                                                        token5 = token;
                                                        config3 = config;
                                                        obj2 = fieldName;
                                                        fieldIndex3 = fieldIndex422222222222222222;
                                                        type3 = type2;
                                                        i5 = intVal;
                                                        setFlags2 = setFlags3;
                                                    }
                                                }
                                                fieldValue = fieldValue3;
                                                c2 = 5;
                                                object3 = i2;
                                                if (matchField2) {
                                                }
                                                if (object5 == null) {
                                                }
                                                object6 = object5;
                                                childContext = childContext2;
                                                fieldValues = fieldValues3;
                                                if (!matchField2) {
                                                }
                                                z = false;
                                                object8 = object7;
                                                int fieldIndex4222222222222222222 = fieldIndex2 + 1;
                                                fieldValues3 = fieldValues;
                                                lexer3 = lexer2;
                                                context3 = context;
                                                childContext2 = childContext;
                                                typeKey2 = typeKey;
                                                notMatchCount2 = notMatchCount;
                                                token5 = token;
                                                config3 = config;
                                                obj2 = fieldName;
                                                fieldIndex3 = fieldIndex4222222222222222222;
                                                type3 = type2;
                                                i5 = intVal;
                                                setFlags2 = setFlags3;
                                            } catch (Throwable th11) {
                                                e = th11;
                                                context = context3;
                                                object8 = object2;
                                                if (childContext2 != null) {
                                                }
                                                parser.setContext(context);
                                                throw e;
                                            }
                                        }
                                        int intVal2 = lexer3.scanFieldInt(name_chars);
                                        if (intVal2 == 0) {
                                            c2 = 5;
                                            if (lexer3.matchStat == 5) {
                                                fieldValue2 = null;
                                                fieldValue8 = fieldValue2;
                                                if (lexer3.matchStat <= 0) {
                                                    matchField2 = true;
                                                    valueParsed = true;
                                                    iArr = intVal2;
                                                } else {
                                                    iArr = intVal2;
                                                    if (lexer3.matchStat == -2) {
                                                        notMatchCount = notMatchCount2 + 1;
                                                        config = config3;
                                                        c = c2;
                                                        token = token5;
                                                        fieldValues = fieldValues3;
                                                        fieldIndex2 = fieldIndex;
                                                        object8 = object2;
                                                        intVal = 16;
                                                        z = false;
                                                        type2 = type;
                                                        childContext = childContext2;
                                                        typeKey = typeKey2;
                                                        context = context3;
                                                        lexer2 = lexer3;
                                                        int fieldIndex42222222222222222222 = fieldIndex2 + 1;
                                                        fieldValues3 = fieldValues;
                                                        lexer3 = lexer2;
                                                        context3 = context;
                                                        childContext2 = childContext;
                                                        typeKey2 = typeKey;
                                                        notMatchCount2 = notMatchCount;
                                                        token5 = token;
                                                        config3 = config;
                                                        obj2 = fieldName;
                                                        fieldIndex3 = fieldIndex42222222222222222222;
                                                        type3 = type2;
                                                        i5 = intVal;
                                                        setFlags2 = setFlags3;
                                                    }
                                                }
                                            }
                                        } else {
                                            c2 = 5;
                                        }
                                        fieldValue2 = Integer.valueOf(intVal2);
                                        fieldValue8 = fieldValue2;
                                        if (lexer3.matchStat <= 0) {
                                        }
                                    } else {
                                        matchField2 = true;
                                        obj = matchField;
                                    }
                                    fieldDeser2 = fieldDeser;
                                    fieldInfo2 = fieldInfo;
                                    fieldValue = fieldValue8;
                                    c2 = 5;
                                    object3 = obj;
                                    if (matchField2) {
                                    }
                                    if (object5 == null) {
                                    }
                                    object6 = object5;
                                    childContext = childContext2;
                                    fieldValues = fieldValues3;
                                    if (!matchField2) {
                                    }
                                    z = false;
                                    object8 = object7;
                                    int fieldIndex422222222222222222222 = fieldIndex2 + 1;
                                    fieldValues3 = fieldValues;
                                    lexer3 = lexer2;
                                    context3 = context;
                                    childContext2 = childContext;
                                    typeKey2 = typeKey;
                                    notMatchCount2 = notMatchCount;
                                    token5 = token;
                                    config3 = config;
                                    obj2 = fieldName;
                                    fieldIndex3 = fieldIndex422222222222222222222;
                                    type3 = type2;
                                    i5 = intVal;
                                    setFlags2 = setFlags3;
                                } catch (Throwable th12) {
                                    e = th12;
                                    context = context3;
                                    object8 = object2;
                                    if (childContext2 != null) {
                                    }
                                    parser.setContext(context);
                                    throw e;
                                }
                            } else {
                                fieldDeser2 = fieldDeser;
                                fieldInfo2 = fieldInfo;
                                c2 = 5;
                                iArr = setFlags2;
                            }
                            fieldValue = fieldValue8;
                            object3 = iArr;
                            if (matchField2) {
                            }
                            if (object5 == null) {
                            }
                            object6 = object5;
                            childContext = childContext2;
                            fieldValues = fieldValues3;
                            if (!matchField2) {
                            }
                            z = false;
                            object8 = object7;
                            int fieldIndex4222222222222222222222 = fieldIndex2 + 1;
                            fieldValues3 = fieldValues;
                            lexer3 = lexer2;
                            context3 = context;
                            childContext2 = childContext;
                            typeKey2 = typeKey;
                            notMatchCount2 = notMatchCount;
                            token5 = token;
                            config3 = config;
                            obj2 = fieldName;
                            fieldIndex3 = fieldIndex4222222222222222222222;
                            type3 = type2;
                            i5 = intVal;
                            setFlags2 = setFlags3;
                        } catch (Throwable th13) {
                            e = th13;
                            context = context3;
                            if (childContext2 != null) {
                            }
                            parser.setContext(context);
                            throw e;
                        }
                    }
                    token2 = token;
                    if (object2 != null) {
                        object4 = (T) object2;
                    } else if (fieldValues2 == null) {
                        try {
                            T t2 = (T) createInstance(parser, type);
                            if (childContext2 == null) {
                                try {
                                    childContext2 = parser.setContext(context, t2, obj2);
                                } catch (Throwable th14) {
                                    e = th14;
                                    object8 = t2;
                                    if (childContext2 != null) {
                                    }
                                    parser.setContext(context);
                                    throw e;
                                }
                            }
                            if (childContext2 != null) {
                                childContext2.object = t2;
                            }
                            parser.setContext(context);
                            return t2;
                        } catch (Throwable th15) {
                            e = th15;
                            object8 = object2;
                            if (childContext2 != null) {
                            }
                            parser.setContext(context);
                            throw e;
                        }
                    } else {
                        try {
                            String[] paramNames = this.beanInfo.creatorConstructorParameters;
                            if (paramNames != null) {
                                try {
                                    Object[] params2 = new Object[paramNames.length];
                                    int i8 = 0;
                                    while (i8 < paramNames.length) {
                                        Object param3 = fieldValues2.remove(paramNames[i8]);
                                        if (param3 == null) {
                                            Type fieldType = this.beanInfo.creatorConstructorParameterTypes[i8];
                                            token3 = token2;
                                            try {
                                                FieldInfo fieldInfo4 = this.beanInfo.fields[i8];
                                                if (fieldType == Byte.TYPE) {
                                                    try {
                                                        param2 = (byte) 0;
                                                    } catch (Throwable th16) {
                                                        e = th16;
                                                        object8 = object2;
                                                        if (childContext2 != null) {
                                                        }
                                                        parser.setContext(context);
                                                        throw e;
                                                    }
                                                } else if (fieldType == Short.TYPE) {
                                                    param2 = (short) 0;
                                                } else if (fieldType == Integer.TYPE) {
                                                    param2 = 0;
                                                } else {
                                                    if (fieldType == Long.TYPE) {
                                                        lexer = lexer3;
                                                        param = 0L;
                                                    } else {
                                                        lexer = lexer3;
                                                        if (fieldType == Float.TYPE) {
                                                            param = Float.valueOf(0.0f);
                                                        } else if (fieldType == Double.TYPE) {
                                                            param = Double.valueOf(0.0d);
                                                        } else if (fieldType == Boolean.TYPE) {
                                                            param = Boolean.FALSE;
                                                        } else if (fieldType == String.class && (fieldInfo4.parserFeatures & Feature.InitStringFieldAsEmpty.mask) != 0) {
                                                            param3 = "";
                                                        }
                                                    }
                                                    param3 = param;
                                                }
                                                param3 = param2;
                                                lexer = lexer3;
                                            } catch (Throwable th17) {
                                                e = th17;
                                                object8 = object2;
                                                if (childContext2 != null) {
                                                }
                                                parser.setContext(context);
                                                throw e;
                                            }
                                        } else {
                                            token3 = token2;
                                            lexer = lexer3;
                                            if (this.beanInfo.creatorConstructorParameterTypes != null && i8 < this.beanInfo.creatorConstructorParameterTypes.length) {
                                                Type paramType = this.beanInfo.creatorConstructorParameterTypes[i8];
                                                if (paramType instanceof Class) {
                                                    Class paramClass = (Class) paramType;
                                                    if (!paramClass.isInstance(param3) && (param3 instanceof List)) {
                                                        List list = (List) param3;
                                                        if (list.size() == 1 && paramClass.isInstance(list.get(0))) {
                                                            param3 = list.get(0);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        params2[i8] = param3;
                                        i8++;
                                        token2 = token3;
                                        lexer3 = lexer;
                                    }
                                    lexer = lexer3;
                                    params = params2;
                                } catch (Throwable th18) {
                                    e = th18;
                                    object8 = object2;
                                    if (childContext2 != null) {
                                    }
                                    parser.setContext(context);
                                    throw e;
                                }
                            } else {
                                lexer = lexer3;
                                try {
                                    FieldInfo[] fieldInfoList = this.beanInfo.fields;
                                    int size = fieldInfoList.length;
                                    params = new Object[size];
                                    int i9 = 0;
                                    while (i9 < size) {
                                        FieldInfo fieldInfo5 = fieldInfoList[i9];
                                        Object param4 = fieldValues2.get(fieldInfo5.name);
                                        if (param4 == null) {
                                            Type fieldType2 = fieldInfo5.fieldType;
                                            if (fieldType2 == Byte.TYPE) {
                                                param4 = (byte) 0;
                                            } else if (fieldType2 == Short.TYPE) {
                                                param4 = (short) 0;
                                            } else if (fieldType2 == Integer.TYPE) {
                                                param4 = 0;
                                            } else if (fieldType2 == Long.TYPE) {
                                                config2 = config3;
                                                try {
                                                    param4 = 0L;
                                                } catch (Throwable th19) {
                                                    e = th19;
                                                    object8 = object2;
                                                    if (childContext2 != null) {
                                                    }
                                                    parser.setContext(context);
                                                    throw e;
                                                }
                                            } else {
                                                config2 = config3;
                                                if (fieldType2 == Float.TYPE) {
                                                    param4 = Float.valueOf(0.0f);
                                                } else if (fieldType2 == Double.TYPE) {
                                                    param4 = Double.valueOf(0.0d);
                                                } else if (fieldType2 == Boolean.TYPE) {
                                                    param4 = Boolean.FALSE;
                                                } else if (fieldType2 == String.class && (fieldInfo5.parserFeatures & Feature.InitStringFieldAsEmpty.mask) != 0) {
                                                    param4 = "";
                                                }
                                            }
                                            config2 = config3;
                                        } else {
                                            config2 = config3;
                                        }
                                        params[i9] = param4;
                                        i9++;
                                        config3 = config2;
                                    }
                                } catch (Throwable th20) {
                                    e = th20;
                                    object8 = object2;
                                    if (childContext2 != null) {
                                    }
                                    parser.setContext(context);
                                    throw e;
                                }
                            }
                            if (this.beanInfo.creatorConstructor != null) {
                                boolean hasNull = false;
                                if (this.beanInfo.kotlin) {
                                    int i10 = 0;
                                    while (true) {
                                        if (i10 >= params.length) {
                                            break;
                                        } else if (params[i10] != null || this.beanInfo.fields == null || i10 >= this.beanInfo.fields.length) {
                                            i10++;
                                        } else if (this.beanInfo.fields[i10].fieldClass == String.class) {
                                            hasNull = true;
                                        }
                                    }
                                }
                                if (hasNull) {
                                    try {
                                        if (this.beanInfo.kotlinDefaultConstructor != null) {
                                            object4 = (T) this.beanInfo.kotlinDefaultConstructor.newInstance(new Object[0]);
                                            for (int i11 = 0; i11 < params.length; i11++) {
                                                try {
                                                    Object param5 = params[i11];
                                                    if (!(param5 == null || this.beanInfo.fields == null || i11 >= this.beanInfo.fields.length)) {
                                                        this.beanInfo.fields[i11].set(object4, param5);
                                                    }
                                                } catch (Exception e3) {
                                                    e2 = e3;
                                                    throw new JSONException("create instance error, " + paramNames + ", " + this.beanInfo.creatorConstructor.toGenericString(), e2);
                                                }
                                            }
                                            if (paramNames != null) {
                                                try {
                                                    for (Map.Entry<String, Object> entry : fieldValues2.entrySet()) {
                                                        FieldDeserializer fieldDeserializer = getFieldDeserializer(entry.getKey());
                                                        if (fieldDeserializer != null) {
                                                            fieldDeserializer.setValue(object4, entry.getValue());
                                                        }
                                                    }
                                                } catch (Throwable th21) {
                                                    e = th21;
                                                    object8 = object4;
                                                    if (childContext2 != null) {
                                                    }
                                                    parser.setContext(context);
                                                    throw e;
                                                }
                                            }
                                        }
                                    } catch (Exception e4) {
                                        e2 = e4;
                                        throw new JSONException("create instance error, " + paramNames + ", " + this.beanInfo.creatorConstructor.toGenericString(), e2);
                                    }
                                }
                                object4 = (T) this.beanInfo.creatorConstructor.newInstance(params);
                                if (paramNames != null) {
                                }
                            } else if (this.beanInfo.factoryMethod != null) {
                                try {
                                    object4 = (T) this.beanInfo.factoryMethod.invoke(null, params);
                                } catch (Exception e5) {
                                    throw new JSONException("create factory method error, " + this.beanInfo.factoryMethod.toString(), e5);
                                }
                            } else {
                                object4 = (T) object2;
                            }
                            if (childContext2 != null) {
                                childContext2.object = object4;
                            }
                        } catch (Throwable th22) {
                            e = th22;
                            object8 = object2;
                            if (childContext2 != null) {
                            }
                            parser.setContext(context);
                            throw e;
                        }
                    }
                    Method buildMethod = this.beanInfo.buildMethod;
                    if (buildMethod == null) {
                        if (childContext2 != null) {
                            childContext2.object = object4;
                        }
                        parser.setContext(context);
                        return (T) object4;
                    }
                    try {
                        T t3 = (T) buildMethod.invoke(object4, new Object[0]);
                        if (childContext2 != null) {
                            childContext2.object = object4;
                        }
                        parser.setContext(context);
                        return t3;
                    } catch (Exception e6) {
                        throw new JSONException("build object error", e6);
                    }
                } catch (Throwable th23) {
                    e = th23;
                    context = context3;
                    if (childContext2 != null) {
                    }
                    parser.setContext(context);
                    throw e;
                }
            } else if (lexer3.isBlankInput()) {
                if (0 != 0) {
                    childContext2.object = object8;
                }
                parser.setContext(context3);
                return null;
            } else {
                if (token4 == 4) {
                    String strVal = lexer3.stringVal();
                    if (strVal.length() == 0) {
                        lexer3.nextToken();
                        if (0 != 0) {
                            childContext2.object = object8;
                        }
                        parser.setContext(context3);
                        return null;
                    } else if (this.beanInfo.jsonType != null) {
                        Class<?>[] seeAlso = this.beanInfo.jsonType.seeAlso();
                        int length = seeAlso.length;
                        int i12 = 0;
                        while (i12 < length) {
                            Class<?> seeAlsoClass = seeAlso[i12];
                            if (Enum.class.isAssignableFrom(seeAlsoClass)) {
                                try {
                                    T t4 = (T) Enum.valueOf(seeAlsoClass, strVal);
                                    if (0 != 0) {
                                        childContext2.object = object8;
                                    }
                                    parser.setContext(context3);
                                    return t4;
                                } catch (IllegalArgumentException e7) {
                                }
                            } else {
                                i12++;
                                obj3 = obj3;
                            }
                        }
                    }
                } else if (token4 == 5) {
                    lexer3.getCalendar();
                }
                if (token4 == 14 && lexer3.getCurrent() == ']') {
                    lexer3.next();
                    lexer3.nextToken();
                    if (0 != 0) {
                        childContext2.object = object8;
                    }
                    parser.setContext(context3);
                    return null;
                }
                if (this.beanInfo.factoryMethod != null && this.beanInfo.fields.length == 1) {
                    try {
                        FieldInfo field = this.beanInfo.fields[0];
                        if (field.fieldClass == Integer.class) {
                            if (token4 == 2) {
                                int intValue = lexer3.intValue();
                                lexer3.nextToken();
                                T t5 = (T) createFactoryInstance(config3, Integer.valueOf(intValue));
                                if (0 != 0) {
                                    childContext2.object = object8;
                                }
                                parser.setContext(context3);
                                return t5;
                            }
                        } else if (field.fieldClass == String.class && token4 == 4) {
                            String stringVal = lexer3.stringVal();
                            lexer3.nextToken();
                            T t6 = (T) createFactoryInstance(config3, stringVal);
                            if (0 != 0) {
                                childContext2.object = object8;
                            }
                            parser.setContext(context3);
                            return t6;
                        }
                    } catch (Exception ex) {
                        throw new JSONException(ex.getMessage(), ex);
                    }
                }
                StringBuilder buf = new StringBuilder();
                buf.append("syntax error, expect {, actual ");
                buf.append(lexer3.tokenName());
                buf.append(", pos ");
                buf.append(lexer3.pos());
                if (obj2 instanceof String) {
                    buf.append(", fieldName ");
                    buf.append(obj2);
                }
                buf.append(", fastjson-version ");
                buf.append(JSON.VERSION);
                throw new JSONException(buf.toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public Enum scanEnum(JSONLexerBase lexer, char[] name_chars, ObjectDeserializer fieldValueDeserilizer) {
        EnumDeserializer enumDeserializer = null;
        if (fieldValueDeserilizer instanceof EnumDeserializer) {
            enumDeserializer = (EnumDeserializer) fieldValueDeserilizer;
        }
        if (enumDeserializer == null) {
            lexer.matchStat = -1;
            return null;
        }
        long enumNameHashCode = lexer.scanEnumSymbol(name_chars);
        if (lexer.matchStat <= 0) {
            return null;
        }
        Enum e = enumDeserializer.getEnumByHashCode(enumNameHashCode);
        if (e == null) {
            if (enumNameHashCode == -3750763034362895579L) {
                return null;
            }
            if (lexer.isEnabled(Feature.ErrorOnEnumNotMatch)) {
                throw new JSONException("not match enum value, " + enumDeserializer.enumClass);
            }
        }
        return e;
    }

    public boolean parseField(DefaultJSONParser parser, String key, Object object, Type objectType, Map<String, Object> fieldValues) {
        return parseField(parser, key, object, objectType, fieldValues, null);
    }

    /* JADX INFO: Multiple debug info for r3v13 com.alibaba.fastjson.parser.deserializer.FieldDeserializer: [D('fieldDeserializer' com.alibaba.fastjson.parser.deserializer.FieldDeserializer), D('unwrappedFieldDeser' com.alibaba.fastjson.parser.deserializer.FieldDeserializer)] */
    /* JADX INFO: Multiple debug info for r0v38 java.lang.reflect.Field: [D('fieldDeserializer' com.alibaba.fastjson.parser.deserializer.FieldDeserializer), D('field' java.lang.reflect.Field)] */
    /* JADX INFO: Multiple debug info for r5v8 java.lang.String: [D('fields' java.lang.reflect.Field[]), D('fieldName' java.lang.String)] */
    /* JADX WARN: Type inference failed for: r24v0, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r24v4 */
    /* JADX WARN: Type inference failed for: r24v5 */
    /* JADX WARN: Type inference failed for: r24v21 */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0242  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x011a  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public boolean parseField(DefaultJSONParser parser, String key, Object object, Type objectType, Map<String, Object> fieldValues, int[] setFlags) {
        FieldDeserializer fieldDeserializer;
        JSONLexer lexer;
        ?? r24;
        FieldDeserializer fieldDeserializer2;
        FieldDeserializer fieldDeserializer3;
        FieldDeserializer fieldDeserializer4;
        Exception e;
        boolean z;
        FieldDeserializer fieldDeserializer5;
        JSONLexer lexer2 = parser.lexer;
        int disableFieldSmartMatchMask = Feature.DisableFieldSmartMatch.mask;
        if (lexer2.isEnabled(disableFieldSmartMatchMask) || (this.beanInfo.parserFeatures & disableFieldSmartMatchMask) != 0) {
            fieldDeserializer = getFieldDeserializer(key);
        } else {
            fieldDeserializer = smartMatch(key, setFlags);
        }
        int mask = Feature.SupportNonPublicField.mask;
        if (fieldDeserializer != null) {
            fieldDeserializer5 = fieldDeserializer;
            z = true;
            lexer = lexer2;
        } else if (lexer2.isEnabled(mask) || (this.beanInfo.parserFeatures & mask) != 0) {
            if (this.extraFieldDeserializers == null) {
                ConcurrentHashMap extraFieldDeserializers2 = new ConcurrentHashMap(1, 0.75f, 1);
                Class c = this.clazz;
                while (c != null && c != Object.class) {
                    Field[] fields = c.getDeclaredFields();
                    int length = fields.length;
                    int i = 0;
                    while (i < length) {
                        Field field = fields[i];
                        String fieldName = field.getName();
                        if (getFieldDeserializer(fieldName) == null) {
                            int fieldModifiers = field.getModifiers();
                            if ((fieldModifiers & 16) == 0 && (fieldModifiers & 8) == 0) {
                                extraFieldDeserializers2.put(fieldName, field);
                            }
                        }
                        i++;
                        fieldDeserializer = fieldDeserializer;
                        fields = fields;
                    }
                    c = c.getSuperclass();
                }
                fieldDeserializer5 = fieldDeserializer;
                this.extraFieldDeserializers = extraFieldDeserializers2;
            } else {
                fieldDeserializer5 = fieldDeserializer;
            }
            Object deserOrField = this.extraFieldDeserializers.get(key);
            if (deserOrField != null) {
                if (deserOrField instanceof FieldDeserializer) {
                    fieldDeserializer2 = (FieldDeserializer) deserOrField;
                    lexer = lexer2;
                    r24 = 1;
                } else {
                    Field field2 = (Field) deserOrField;
                    field2.setAccessible(true);
                    r24 = 1;
                    lexer = lexer2;
                    FieldDeserializer fieldDeserializer6 = new DefaultFieldDeserializer(parser.getConfig(), this.clazz, new FieldInfo(key, field2.getDeclaringClass(), field2.getType(), field2.getGenericType(), field2, 0, 0, 0));
                    this.extraFieldDeserializers.put(key, fieldDeserializer6);
                    fieldDeserializer2 = fieldDeserializer6;
                }
                if (fieldDeserializer2 == null) {
                    FieldDeserializer fieldDeserializer7 = fieldDeserializer2;
                    int fieldIndex = -1;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= this.sortedFieldDeserializers.length) {
                            fieldDeserializer3 = fieldDeserializer7;
                            break;
                        }
                        fieldDeserializer3 = fieldDeserializer7;
                        if (this.sortedFieldDeserializers[i2] == fieldDeserializer3) {
                            fieldIndex = i2;
                            break;
                        }
                        i2++;
                        fieldDeserializer7 = fieldDeserializer3;
                    }
                    if (fieldIndex == -1 || setFlags == null || !key.startsWith("_") || !isSetFlag(fieldIndex, setFlags)) {
                        lexer.nextTokenWithColon(fieldDeserializer3.getFastMatchToken());
                        fieldDeserializer3.parseField(parser, object, objectType, fieldValues);
                        if (setFlags != null) {
                            int flagIndex = fieldIndex / 32;
                            int i3 = setFlags[flagIndex];
                            int i4 = r24 == true ? 1 : 0;
                            int i5 = r24 == true ? 1 : 0;
                            int i6 = r24 == true ? 1 : 0;
                            int i7 = r24 == true ? 1 : 0;
                            int i8 = r24 == true ? 1 : 0;
                            int i9 = r24 == true ? 1 : 0;
                            int i10 = r24 == true ? 1 : 0;
                            setFlags[flagIndex] = i3 | (i4 << (fieldIndex % 32));
                        }
                        return r24;
                    }
                    parser.parseExtra(object, key);
                    return false;
                } else if (lexer.isEnabled(Feature.IgnoreNotMatch)) {
                    int fieldIndex2 = -1;
                    int i11 = 0;
                    while (i11 < this.sortedFieldDeserializers.length) {
                        FieldDeserializer fieldDeser = this.sortedFieldDeserializers[i11];
                        FieldInfo fieldInfo = fieldDeser.fieldInfo;
                        if (!fieldInfo.unwrapped || !(fieldDeser instanceof DefaultFieldDeserializer)) {
                            fieldDeserializer4 = fieldDeserializer2;
                        } else if (fieldInfo.field != null) {
                            DefaultFieldDeserializer defaultFieldDeserializer = (DefaultFieldDeserializer) fieldDeser;
                            ObjectDeserializer fieldValueDeser = defaultFieldDeserializer.getFieldValueDeserilizer(parser.getConfig());
                            if (fieldValueDeser instanceof JavaBeanDeserializer) {
                                fieldDeserializer4 = fieldDeserializer2;
                                FieldDeserializer unwrappedFieldDeser = ((JavaBeanDeserializer) fieldValueDeser).getFieldDeserializer(key);
                                if (unwrappedFieldDeser != null) {
                                    try {
                                        Object fieldObject = fieldInfo.field.get(object);
                                        if (fieldObject == null) {
                                            try {
                                                fieldObject = ((JavaBeanDeserializer) fieldValueDeser).createInstance(parser, fieldInfo.fieldType);
                                                fieldDeser.setValue(object, fieldObject);
                                            } catch (Exception e2) {
                                                e = e2;
                                                throw new JSONException("parse unwrapped field error.", e);
                                            }
                                        }
                                        lexer.nextTokenWithColon(defaultFieldDeserializer.getFastMatchToken());
                                        unwrappedFieldDeser.parseField(parser, fieldObject, objectType, fieldValues);
                                        fieldIndex2 = i11;
                                    } catch (Exception e3) {
                                        e = e3;
                                        throw new JSONException("parse unwrapped field error.", e);
                                    }
                                }
                            } else {
                                fieldDeserializer4 = fieldDeserializer2;
                                if (fieldValueDeser instanceof MapDeserializer) {
                                    MapDeserializer javaBeanFieldValueDeserializer = (MapDeserializer) fieldValueDeser;
                                    try {
                                        Map fieldObject2 = (Map) fieldInfo.field.get(object);
                                        if (fieldObject2 == null) {
                                            fieldObject2 = javaBeanFieldValueDeserializer.createMap(fieldInfo.fieldType);
                                            fieldDeser.setValue(object, fieldObject2);
                                        }
                                        lexer.nextTokenWithColon();
                                        fieldObject2.put(key, parser.parse(key));
                                        fieldIndex2 = i11;
                                    } catch (Exception e4) {
                                        throw new JSONException("parse unwrapped field error.", e4);
                                    }
                                }
                            }
                        } else {
                            fieldDeserializer4 = fieldDeserializer2;
                            if (fieldInfo.method.getParameterTypes().length == 2) {
                                lexer.nextTokenWithColon();
                                Object fieldValue = parser.parse(key);
                                try {
                                    Method method = fieldInfo.method;
                                    Object[] objArr = new Object[2];
                                    objArr[0] = key;
                                    char c2 = r24 == true ? 1 : 0;
                                    char c3 = r24 == true ? 1 : 0;
                                    char c4 = r24 == true ? 1 : 0;
                                    char c5 = r24 == true ? 1 : 0;
                                    char c6 = r24 == true ? 1 : 0;
                                    char c7 = r24 == true ? 1 : 0;
                                    char c8 = r24 == true ? 1 : 0;
                                    objArr[c2] = fieldValue;
                                    method.invoke(object, objArr);
                                    fieldIndex2 = i11;
                                } catch (Exception e5) {
                                    throw new JSONException("parse unwrapped field error.", e5);
                                }
                            }
                        }
                        i11++;
                        fieldDeserializer2 = fieldDeserializer4;
                    }
                    if (fieldIndex2 != -1) {
                        if (setFlags != null) {
                            int flagIndex2 = fieldIndex2 / 32;
                            setFlags[flagIndex2] = setFlags[flagIndex2] | (r24 << (fieldIndex2 % 32));
                        }
                        return r24;
                    }
                    parser.parseExtra(object, key);
                    return false;
                } else {
                    throw new JSONException("setter not found, class " + this.clazz.getName() + ", property " + key);
                }
            } else {
                lexer = lexer2;
                z = true;
            }
        } else {
            fieldDeserializer5 = fieldDeserializer;
            z = true;
            lexer = lexer2;
        }
        fieldDeserializer2 = fieldDeserializer5;
        r24 = z;
        if (fieldDeserializer2 == null) {
        }
    }

    public FieldDeserializer smartMatch(String key) {
        return smartMatch(key, null);
    }

    public FieldDeserializer smartMatch(String key, int[] setFlags) {
        if (key == null) {
            return null;
        }
        FieldDeserializer fieldDeserializer = getFieldDeserializer(key, setFlags);
        if (fieldDeserializer != null) {
            return fieldDeserializer;
        }
        long smartKeyHash = TypeUtils.fnv1a_64_lower(key);
        if (this.smartMatchHashArray == null) {
            long[] hashArray2 = new long[this.sortedFieldDeserializers.length];
            for (int i = 0; i < this.sortedFieldDeserializers.length; i++) {
                hashArray2[i] = TypeUtils.fnv1a_64_lower(this.sortedFieldDeserializers[i].fieldInfo.name);
            }
            Arrays.sort(hashArray2);
            this.smartMatchHashArray = hashArray2;
        }
        int pos = Arrays.binarySearch(this.smartMatchHashArray, smartKeyHash);
        boolean is = false;
        if (pos < 0) {
            boolean startsWith = key.startsWith("is");
            is = startsWith;
            if (startsWith) {
                pos = Arrays.binarySearch(this.smartMatchHashArray, TypeUtils.fnv1a_64_lower(key.substring(2)));
            }
        }
        if (pos >= 0) {
            if (this.smartMatchHashArrayMapping == null) {
                short[] mapping = new short[this.smartMatchHashArray.length];
                Arrays.fill(mapping, (short) -1);
                for (int i2 = 0; i2 < this.sortedFieldDeserializers.length; i2++) {
                    int p = Arrays.binarySearch(this.smartMatchHashArray, TypeUtils.fnv1a_64_lower(this.sortedFieldDeserializers[i2].fieldInfo.name));
                    if (p >= 0) {
                        mapping[p] = (short) i2;
                    }
                }
                this.smartMatchHashArrayMapping = mapping;
            }
            short s = this.smartMatchHashArrayMapping[pos];
            if (s != -1 && !isSetFlag(s, setFlags)) {
                fieldDeserializer = this.sortedFieldDeserializers[s];
            }
        }
        if (fieldDeserializer == null) {
            return fieldDeserializer;
        }
        FieldInfo fieldInfo = fieldDeserializer.fieldInfo;
        if ((fieldInfo.parserFeatures & Feature.DisableFieldSmartMatch.mask) != 0) {
            return null;
        }
        Class fieldClass = fieldInfo.fieldClass;
        if (!is || fieldClass == Boolean.TYPE || fieldClass == Boolean.class) {
            return fieldDeserializer;
        }
        return null;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 12;
    }

    private Object createFactoryInstance(ParserConfig config, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return this.beanInfo.factoryMethod.invoke(null, value);
    }

    public Object createInstance(Map<String, Object> map, ParserConfig config) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Integer index;
        Object value;
        double doubleValue;
        float floatValue;
        if (this.beanInfo.creatorConstructor == null && this.beanInfo.factoryMethod == null) {
            Object object = createInstance((DefaultJSONParser) null, this.clazz);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value2 = entry.getValue();
                FieldDeserializer fieldDeser = smartMatch(entry.getKey());
                if (fieldDeser != null) {
                    FieldInfo fieldInfo = fieldDeser.fieldInfo;
                    Field field = fieldDeser.fieldInfo.field;
                    Type paramType = fieldInfo.fieldType;
                    if (field != null) {
                        if (paramType == Boolean.TYPE) {
                            if (value2 == Boolean.FALSE) {
                                field.setBoolean(object, false);
                            } else if (value2 == Boolean.TRUE) {
                                field.setBoolean(object, true);
                            }
                        } else if (paramType == Integer.TYPE) {
                            if (value2 instanceof Number) {
                                field.setInt(object, ((Number) value2).intValue());
                            }
                        } else if (paramType == Long.TYPE) {
                            if (value2 instanceof Number) {
                                field.setLong(object, ((Number) value2).longValue());
                            }
                        } else if (paramType == Float.TYPE) {
                            if (value2 instanceof Number) {
                                field.setFloat(object, ((Number) value2).floatValue());
                            } else if (value2 instanceof String) {
                                String strVal = (String) value2;
                                if (strVal.length() <= 10) {
                                    floatValue = TypeUtils.parseFloat(strVal);
                                } else {
                                    floatValue = Float.parseFloat(strVal);
                                }
                                field.setFloat(object, floatValue);
                            }
                        } else if (paramType == Double.TYPE) {
                            if (value2 instanceof Number) {
                                field.setDouble(object, ((Number) value2).doubleValue());
                            } else if (value2 instanceof String) {
                                String strVal2 = (String) value2;
                                if (strVal2.length() <= 10) {
                                    doubleValue = TypeUtils.parseDouble(strVal2);
                                } else {
                                    doubleValue = Double.parseDouble(strVal2);
                                }
                                field.setDouble(object, doubleValue);
                            }
                        } else if (value2 != null && paramType == value2.getClass()) {
                            field.set(object, value2);
                        }
                    }
                    String format = fieldInfo.format;
                    if (format != null && paramType == Date.class) {
                        value = TypeUtils.castToDate(value2, format);
                    } else if (paramType instanceof ParameterizedType) {
                        value = TypeUtils.cast(value2, (ParameterizedType) paramType, config);
                    } else {
                        value = TypeUtils.cast(value2, paramType, config);
                    }
                    fieldDeser.setValue(object, value);
                }
            }
            if (this.beanInfo.buildMethod == null) {
                return object;
            }
            try {
                return this.beanInfo.buildMethod.invoke(object, new Object[0]);
            } catch (Exception e) {
                throw new JSONException("build object error", e);
            }
        } else {
            FieldInfo[] fieldInfoList = this.beanInfo.fields;
            int size = fieldInfoList.length;
            Object[] params = new Object[size];
            Map<String, Integer> missFields = null;
            for (int i = 0; i < size; i++) {
                FieldInfo fieldInfo2 = fieldInfoList[i];
                boolean param = map.get(fieldInfo2.name);
                if (param == null) {
                    Class<?> fieldClass = fieldInfo2.fieldClass;
                    if (fieldClass == Integer.TYPE) {
                        param = 0;
                    } else if (fieldClass == Long.TYPE) {
                        param = 0L;
                    } else if (fieldClass == Short.TYPE) {
                        param = (short) 0;
                    } else if (fieldClass == Byte.TYPE) {
                        param = (byte) 0;
                    } else if (fieldClass == Float.TYPE) {
                        param = Float.valueOf(0.0f);
                    } else if (fieldClass == Double.TYPE) {
                        param = Double.valueOf(0.0d);
                    } else if (fieldClass == Character.TYPE) {
                        param = '0';
                    } else if (fieldClass == Boolean.TYPE) {
                        param = false;
                    }
                    if (missFields == null) {
                        missFields = new HashMap<>();
                    }
                    missFields.put(fieldInfo2.name, Integer.valueOf(i));
                }
                params[i] = param;
            }
            if (missFields != null) {
                for (Map.Entry<String, Object> entry2 : map.entrySet()) {
                    Object value3 = entry2.getValue();
                    FieldDeserializer fieldDeser2 = smartMatch(entry2.getKey());
                    if (!(fieldDeser2 == null || (index = missFields.get(fieldDeser2.fieldInfo.name)) == null)) {
                        params[index.intValue()] = value3;
                    }
                }
            }
            if (this.beanInfo.creatorConstructor != null) {
                boolean hasNull = false;
                if (this.beanInfo.kotlin) {
                    int i2 = 0;
                    while (true) {
                        if (i2 >= params.length) {
                            break;
                        } else if (params[i2] != null || this.beanInfo.fields == null || i2 >= this.beanInfo.fields.length) {
                            i2++;
                        } else if (this.beanInfo.fields[i2].fieldClass == String.class) {
                            hasNull = true;
                        }
                    }
                }
                if (!hasNull || this.beanInfo.kotlinDefaultConstructor == null) {
                    try {
                        return this.beanInfo.creatorConstructor.newInstance(params);
                    } catch (Exception e2) {
                        throw new JSONException("create instance error, " + this.beanInfo.creatorConstructor.toGenericString(), e2);
                    }
                } else {
                    try {
                        Object object2 = this.beanInfo.kotlinDefaultConstructor.newInstance(new Object[0]);
                        for (int i3 = 0; i3 < params.length; i3++) {
                            Object param2 = params[i3];
                            if (!(param2 == null || this.beanInfo.fields == null || i3 >= this.beanInfo.fields.length)) {
                                this.beanInfo.fields[i3].set(object2, param2);
                            }
                        }
                        return object2;
                    } catch (Exception e3) {
                        throw new JSONException("create instance error, " + this.beanInfo.creatorConstructor.toGenericString(), e3);
                    }
                }
            } else if (this.beanInfo.factoryMethod == null) {
                return null;
            } else {
                try {
                    return this.beanInfo.factoryMethod.invoke(null, params);
                } catch (Exception e4) {
                    throw new JSONException("create factory method error, " + this.beanInfo.factoryMethod.toString(), e4);
                }
            }
        }
    }

    public Type getFieldType(int ordinal) {
        return this.sortedFieldDeserializers[ordinal].fieldInfo.fieldType;
    }

    /* access modifiers changed from: protected */
    public Object parseRest(DefaultJSONParser parser, Type type, Object fieldName, Object instance, int features) {
        return parseRest(parser, type, fieldName, instance, features, new int[0]);
    }

    /* access modifiers changed from: protected */
    public Object parseRest(DefaultJSONParser parser, Type type, Object fieldName, Object instance, int features, int[] setFlags) {
        return deserialze(parser, type, fieldName, instance, features, setFlags);
    }

    /* access modifiers changed from: protected */
    public JavaBeanDeserializer getSeeAlso(ParserConfig config, JavaBeanInfo beanInfo2, String typeName) {
        if (beanInfo2.jsonType == null) {
            return null;
        }
        for (Class<?> seeAlsoClass : beanInfo2.jsonType.seeAlso()) {
            ObjectDeserializer seeAlsoDeser = config.getDeserializer(seeAlsoClass);
            if (seeAlsoDeser instanceof JavaBeanDeserializer) {
                JavaBeanDeserializer seeAlsoJavaBeanDeser = (JavaBeanDeserializer) seeAlsoDeser;
                JavaBeanInfo subBeanInfo = seeAlsoJavaBeanDeser.beanInfo;
                if (subBeanInfo.typeName.equals(typeName)) {
                    return seeAlsoJavaBeanDeser;
                }
                JavaBeanDeserializer subSeeAlso = getSeeAlso(config, subBeanInfo, typeName);
                if (subSeeAlso != null) {
                    return subSeeAlso;
                }
            }
        }
        return null;
    }

    protected static void parseArray(Collection collection, ObjectDeserializer deser, DefaultJSONParser parser, Type type, Object fieldName) {
        JSONLexerBase lexer = (JSONLexerBase) parser.lexer;
        int token = lexer.token();
        if (token == 8) {
            lexer.nextToken(16);
            lexer.token();
            return;
        }
        if (token != 14) {
            parser.throwException(token);
        }
        if (lexer.getCurrent() == '[') {
            lexer.next();
            lexer.setToken(14);
        } else {
            lexer.nextToken(14);
        }
        if (lexer.token() == 15) {
            lexer.nextToken();
            return;
        }
        int index = 0;
        while (true) {
            collection.add(deser.deserialze(parser, type, Integer.valueOf(index)));
            index++;
            if (lexer.token() != 16) {
                break;
            } else if (lexer.getCurrent() == '[') {
                lexer.next();
                lexer.setToken(14);
            } else {
                lexer.nextToken(14);
            }
        }
        int token2 = lexer.token();
        if (token2 != 15) {
            parser.throwException(token2);
        }
        if (lexer.getCurrent() == ',') {
            lexer.next();
            lexer.setToken(16);
            return;
        }
        lexer.nextToken(16);
    }
}
