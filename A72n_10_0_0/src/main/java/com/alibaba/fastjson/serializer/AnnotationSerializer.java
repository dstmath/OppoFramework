package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

public class AnnotationSerializer implements ObjectSerializer {
    public static AnnotationSerializer instance = new AnnotationSerializer();
    private static volatile Class sun_AnnotationType = null;
    private static volatile boolean sun_AnnotationType_error = false;
    private static volatile Method sun_AnnotationType_getInstance = null;
    private static volatile Method sun_AnnotationType_members = null;

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        String str;
        Class[] interfaces = object.getClass().getInterfaces();
        if (interfaces.length == 1 && interfaces[0].isAnnotation()) {
            Class annotationClass = interfaces[0];
            if (sun_AnnotationType == null && !sun_AnnotationType_error) {
                try {
                    sun_AnnotationType = Class.forName("sun.reflect.annotation.AnnotationType");
                } catch (Throwable ex) {
                    sun_AnnotationType_error = true;
                    throw new JSONException("not support Type Annotation.", ex);
                }
            }
            if (sun_AnnotationType != null) {
                if (sun_AnnotationType_getInstance == null && !sun_AnnotationType_error) {
                    try {
                        sun_AnnotationType_getInstance = sun_AnnotationType.getMethod("getInstance", Class.class);
                    } catch (Throwable ex2) {
                        sun_AnnotationType_error = true;
                        throw new JSONException("not support Type Annotation.", ex2);
                    }
                }
                if (sun_AnnotationType_members == null && !sun_AnnotationType_error) {
                    try {
                        sun_AnnotationType_members = sun_AnnotationType.getMethod("members", new Class[0]);
                    } catch (Throwable ex3) {
                        sun_AnnotationType_error = true;
                        throw new JSONException("not support Type Annotation.", ex3);
                    }
                }
                if (sun_AnnotationType_getInstance == null || sun_AnnotationType_error) {
                    throw new JSONException("not support Type Annotation.");
                }
                try {
                    Object val = null;
                    try {
                        Map<String, Method> members = (Map) sun_AnnotationType_members.invoke(sun_AnnotationType_getInstance.invoke(null, annotationClass), new Object[0]);
                        JSONObject json = new JSONObject(members.size());
                        for (Map.Entry<String, Method> entry : members.entrySet()) {
                            try {
                                try {
                                    val = entry.getValue().invoke(object, new Object[0]);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                }
                            } catch (IllegalAccessException e2) {
                            } catch (InvocationTargetException e3) {
                            }
                            json.put(entry.getKey(), JSON.toJSON(val));
                        }
                        serializer.write(json);
                    } catch (Throwable ex4) {
                        throw new JSONException(str, ex4);
                    }
                } finally {
                    sun_AnnotationType_error = true;
                    JSONException jSONException = new JSONException("not support Type Annotation.", ex4);
                }
            } else {
                throw new JSONException("not support Type Annotation.");
            }
        }
    }
}
