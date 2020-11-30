package com.alibaba.fastjson.asm;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.util.ASMUtils;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class TypeCollector {
    private static String JSONType = ASMUtils.desc(JSONType.class);
    private static final Map<String, String> primitives = new HashMap<String, String>() {
        /* class com.alibaba.fastjson.asm.TypeCollector.AnonymousClass1 */

        {
            put("int", "I");
            put("boolean", "Z");
            put("byte", "B");
            put("char", "C");
            put("short", "S");
            put("float", "F");
            put("long", "J");
            put("double", "D");
        }
    };
    protected MethodCollector collector = null;
    protected boolean jsonType;
    private final String methodName;
    private final Class<?>[] parameterTypes;

    public TypeCollector(String methodName2, Class<?>[] parameterTypes2) {
        this.methodName = methodName2;
        this.parameterTypes = parameterTypes2;
    }

    /* access modifiers changed from: protected */
    public MethodCollector visitMethod(int access, String name, String desc) {
        if (!(this.collector == null && name.equals(this.methodName))) {
            return null;
        }
        Type[] argTypes = Type.getArgumentTypes(desc);
        int longOrDoubleQuantity = 0;
        for (Type t : argTypes) {
            String className = t.getClassName();
            if (className.equals("long") || className.equals("double")) {
                longOrDoubleQuantity++;
            }
        }
        if (argTypes.length != this.parameterTypes.length) {
            return null;
        }
        for (int i = 0; i < argTypes.length; i++) {
            if (!correctTypeName(argTypes[i], this.parameterTypes[i].getName())) {
                return null;
            }
        }
        MethodCollector methodCollector = new MethodCollector(!Modifier.isStatic(access) ? 1 : 0, argTypes.length + longOrDoubleQuantity);
        this.collector = methodCollector;
        return methodCollector;
    }

    public void visitAnnotation(String desc) {
        if (JSONType.equals(desc)) {
            this.jsonType = true;
        }
    }

    private boolean correctTypeName(Type type, String paramTypeName) {
        String s = type.getClassName();
        String braces = "";
        while (s.endsWith("[]")) {
            braces = braces + "[";
            s = s.substring(0, s.length() - 2);
        }
        if (!braces.equals("")) {
            if (primitives.containsKey(s)) {
                s = braces + primitives.get(s);
            } else {
                s = braces + "L" + s + ";";
            }
        }
        return s.equals(paramTypeName);
    }

    public String[] getParameterNamesForMethod() {
        if (this.collector == null || !this.collector.debugInfoPresent) {
            return new String[0];
        }
        return this.collector.getResult().split(",");
    }

    public boolean matched() {
        return this.collector != null;
    }

    public boolean hasJsonType() {
        return this.jsonType;
    }
}
