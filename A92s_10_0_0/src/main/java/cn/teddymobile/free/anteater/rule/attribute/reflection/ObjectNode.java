package cn.teddymobile.free.anteater.rule.attribute.reflection;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.attribute.intent.Attribute;
import cn.teddymobile.free.anteater.rule.utils.RegularExpressionUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObjectNode {
    private static final String CLASS_NAME_ARBITRARY = "?";
    private static final String JSON_FIELD_ALIAS = "alias";
    private static final String JSON_FIELD_ATTRIBUTE_LIST = "attribute_list";
    private static final String JSON_FIELD_CAPTURE_PATTERN = "capture_pattern";
    private static final String JSON_FIELD_CLASS_NAME = "class_name";
    private static final String JSON_FIELD_CLASS_NAME_OBFUSCATED = "class_name_obfuscated";
    private static final String JSON_FIELD_FIELD_INDEX = "field_index";
    private static final String JSON_FIELD_FIELD_NAME = "field_name";
    private static final String JSON_FIELD_FIELD_NAME_OBFUSCATED = "field_name_obfuscated";
    private static final String JSON_FIELD_LEAF = "leaf";
    private static final String JSON_FIELD_PARENT_FIELD_COUNT = "parent_field_count";
    private static final String TAG = ObjectNode.class.getSimpleName();
    private static final String TAG_ARRAY_FOREACH = "foreach";
    private static final String TAG_CONTEXT = "context";
    private static final String TAG_MAP = "map";
    private static final String TAG_ON_CLICK_LISTENER = "onClickListener";
    private static final String TAG_SUPER_CLASS = "super";
    private final String mAlias;
    private final List<Attribute> mAttributeList;
    private final String mCapturePattern;
    private final String mClassName;
    private final boolean mClassNameArbitrary = this.mClassName.equals(CLASS_NAME_ARBITRARY);
    private final boolean mClassNameObfuscated;
    private final int mFieldIndex;
    private final String mFieldName;
    private final boolean mFieldNameObfuscated;
    private final List<ObjectNode> mLeafList = new ArrayList();
    private final int mParentFieldCount;

    public ObjectNode(JSONObject nodeObject) throws JSONException {
        this.mFieldName = nodeObject.getString(JSON_FIELD_FIELD_NAME);
        this.mClassName = nodeObject.getString(JSON_FIELD_CLASS_NAME);
        this.mFieldIndex = nodeObject.optInt(JSON_FIELD_FIELD_INDEX, -1);
        this.mParentFieldCount = nodeObject.optInt(JSON_FIELD_PARENT_FIELD_COUNT, -1);
        this.mFieldNameObfuscated = nodeObject.optBoolean(JSON_FIELD_FIELD_NAME_OBFUSCATED, false);
        this.mClassNameObfuscated = nodeObject.optBoolean(JSON_FIELD_CLASS_NAME_OBFUSCATED, false);
        JSONArray leafArray = nodeObject.optJSONArray(JSON_FIELD_LEAF);
        if (leafArray != null) {
            for (int i = 0; i < leafArray.length(); i++) {
                this.mLeafList.add(new ObjectNode(leafArray.getJSONObject(i)));
            }
        }
        this.mAttributeList = new ArrayList();
        JSONArray attributeArray = nodeObject.optJSONArray(JSON_FIELD_ATTRIBUTE_LIST);
        if (attributeArray != null) {
            for (int i2 = 0; i2 < attributeArray.length(); i2++) {
                this.mAttributeList.add(new Attribute(attributeArray.getJSONObject(i2)));
            }
        }
        this.mAlias = nodeObject.optString("alias", null);
        this.mCapturePattern = nodeObject.optString(JSON_FIELD_CAPTURE_PATTERN, null);
    }

    public String toString() {
        return "FieldName = " + this.mFieldName + "\nClassName = " + this.mClassName + "\nFieldIndex = " + this.mFieldIndex + "\nParentFieldCount = " + this.mParentFieldCount + "\nFieldNameObfuscated = " + this.mFieldNameObfuscated + "\nClassNameObfuscated = " + this.mClassNameObfuscated + "\nAlias = " + this.mAlias;
    }

    /* JADX WARNING: Removed duplicated region for block: B:157:0x0606 A[Catch:{ IllegalAccessException | NoSuchFieldException -> 0x06ae }] */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x0661 A[Catch:{ IllegalAccessException | NoSuchFieldException -> 0x06ae }] */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0706  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x0789  */
    public Pair<String, Object> extractAttribute(Object object, Class<?> clazz) throws JSONException {
        List<Pair<Object, Class<?>>> recursiveList;
        ReflectiveOperationException e;
        List<Pair<Object, Class<?>>> recursiveList2;
        Object attribute;
        Object attribute2;
        List<Pair<Object, Class<?>>> recursiveList3;
        if (object == null || clazz == null) {
            return null;
        }
        Logger.i(TAG, "ExtractAttribute for Class " + clazz.getName() + " Expected field = " + this.mFieldName);
        List<Pair<Object, Class<?>>> recursiveList4 = new ArrayList<>();
        if (isCustomTag(this.mFieldName)) {
            String tag = this.mFieldName.substring(1, this.mFieldName.length() - 1);
            if (tag.equals(TAG_SUPER_CLASS)) {
                Class<?> superClass = clazz.getSuperclass();
                if (superClass == null) {
                    Logger.w(TAG, "Super class is null.");
                    Logger.w(TAG, "Current = " + clazz.getName());
                    Logger.w(TAG, "Expected = " + this.mClassName);
                } else if (this.mClassNameObfuscated || this.mClassNameArbitrary) {
                    recursiveList4.add(new Pair<>(object, superClass));
                } else if (superClass.getName().equals(this.mClassName)) {
                    recursiveList4.add(new Pair<>(object, superClass));
                } else {
                    Logger.w(TAG, "Super class is incorrect.");
                    Logger.w(TAG, "Current = " + clazz.getName());
                    Logger.w(TAG, "Expected = " + this.mClassName);
                    Logger.w(TAG, "Actual = " + superClass.getName());
                }
            } else if (tag.equals(TAG_ARRAY_FOREACH)) {
                try {
                    if (clazz.isArray()) {
                        for (int i = 0; i < Array.getLength(object); i++) {
                            Object arrayItem = Array.get(object, i);
                            if (arrayItem != null) {
                                if (!this.mClassNameObfuscated) {
                                    if (!this.mClassNameArbitrary) {
                                        if (arrayItem.getClass().getName().equals(this.mClassName)) {
                                            recursiveList4.add(new Pair<>(arrayItem, arrayItem.getClass()));
                                        } else {
                                            Logger.w(TAG, "ArrayItem class name is incorrect.");
                                            Logger.w(TAG, "Current = " + clazz.getName());
                                            Logger.w(TAG, "Expected = " + this.mClassName);
                                            Logger.w(TAG, "Actual = " + arrayItem.getClass().getName());
                                        }
                                    }
                                }
                                recursiveList4.add(new Pair<>(arrayItem, arrayItem.getClass()));
                            }
                        }
                    } else {
                        Logger.w(TAG, "Current class is not an Array.");
                        Logger.w(TAG, "Current = " + clazz.getName());
                    }
                } catch (IllegalAccessException | NoSuchFieldException e2) {
                    recursiveList = recursiveList4;
                    e = e2;
                    Logger.w(TAG, e.getMessage(), e);
                    if (this.mFieldName.equals("[foreach]")) {
                    }
                }
            } else if (TextUtils.isDigitsOnly(tag)) {
                if (clazz.isArray()) {
                    int arrayIndex = stringToInt(tag);
                    if (arrayIndex < 0 || arrayIndex >= Array.getLength(object)) {
                        Logger.w(TAG, "Array index is out of bounds.");
                        Logger.w(TAG, "Current = " + clazz.getName());
                        Logger.w(TAG, "Index = " + arrayIndex + "/" + Array.getLength(object));
                    } else {
                        Object arrayItem2 = Array.get(object, arrayIndex);
                        if (arrayItem2 != null) {
                            if (!this.mClassNameObfuscated) {
                                if (!this.mClassNameArbitrary) {
                                    if (arrayItem2.getClass().getName().equals(this.mClassName)) {
                                        recursiveList4.add(new Pair<>(arrayItem2, arrayItem2.getClass()));
                                    } else {
                                        Logger.w(TAG, "ArrayItem class name is incorrect.");
                                        Logger.w(TAG, "Current = " + clazz.getName());
                                        Logger.w(TAG, "Expected = " + this.mClassName);
                                        Logger.w(TAG, "Actual = " + arrayItem2.getClass().getName());
                                    }
                                }
                            }
                            recursiveList4.add(new Pair<>(arrayItem2, arrayItem2.getClass()));
                        }
                    }
                } else {
                    Logger.w(TAG, "Current class is not an Array.");
                    Logger.w(TAG, "Current = " + clazz.getName());
                }
            } else if (tag.equals(TAG_CONTEXT)) {
                Field contextField = View.class.getDeclaredField("mContext");
                contextField.setAccessible(true);
                Object contextObject = contextField.get(object);
                if (contextObject instanceof Context) {
                    if ((contextObject instanceof ContextWrapper) && !(contextObject instanceof Activity)) {
                        contextObject = ((ContextWrapper) contextObject).getBaseContext();
                    }
                    if (this.mClassNameObfuscated || this.mClassNameArbitrary) {
                        recursiveList4.add(new Pair<>(contextObject, contextObject.getClass()));
                    } else if (contextObject.getClass().getName().equals(this.mClassName)) {
                        recursiveList4.add(new Pair<>(contextObject, contextObject.getClass()));
                    } else {
                        Logger.w(TAG, "Field class name is incorrect.");
                        Logger.w(TAG, "Current = " + clazz.getName());
                        Logger.w(TAG, "Expected = " + this.mClassName);
                        Logger.w(TAG, "Actual = " + contextObject.getClass().getName());
                    }
                }
            } else if (tag.equals(TAG_ON_CLICK_LISTENER)) {
                Field listenerInfoField = View.class.getDeclaredField("mListenerInfo");
                listenerInfoField.setAccessible(true);
                Object listenerInfo = listenerInfoField.get(object);
                if (listenerInfo != null) {
                    Field onClickListenerField = listenerInfo.getClass().getDeclaredField("mOnClickListener");
                    onClickListenerField.setAccessible(true);
                    Object onClickListener = onClickListenerField.get(listenerInfo);
                    if (onClickListener != null) {
                        if (this.mClassNameObfuscated || this.mClassNameArbitrary) {
                            recursiveList4.add(new Pair<>(onClickListener, onClickListener.getClass()));
                        } else if (onClickListener.getClass().getName().equals(this.mClassName)) {
                            recursiveList4.add(new Pair<>(onClickListener, onClickListener.getClass()));
                        } else {
                            Logger.w(TAG, "Field class name is incorrect.");
                            Logger.w(TAG, "Current = " + clazz.getName());
                            Logger.w(TAG, "Expected = " + this.mClassName);
                            Logger.w(TAG, "Actual = " + onClickListener.getClass().getName());
                        }
                    }
                }
            } else if (tag.startsWith(TAG_MAP)) {
                String keyString = tag.substring(tag.indexOf(SettingsStringUtil.DELIMITER) + 1);
                if (object instanceof Map) {
                    Map map = (Map) object;
                    Object value = null;
                    Iterator it = map.keySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Object key = it.next();
                        if (key.toString().equals(keyString)) {
                            value = map.get(key);
                            break;
                        }
                    }
                    if (value != null) {
                        if (this.mClassNameObfuscated || this.mClassNameArbitrary) {
                            recursiveList4.add(new Pair<>(value, value.getClass()));
                        } else if (value.getClass().getName().equals(this.mClassName)) {
                            recursiveList4.add(new Pair<>(value, value.getClass()));
                        } else {
                            Logger.w(TAG, "Field class name is incorrect.");
                            Logger.w(TAG, "Current = " + clazz.getName());
                            Logger.w(TAG, "Expected = " + this.mClassName);
                            Logger.w(TAG, "Actual = " + value.getClass().getName());
                        }
                    }
                } else {
                    Logger.w(TAG, "Field is not a Map");
                    Logger.w(TAG, "Current = " + clazz.getName());
                }
            } else {
                Logger.w(TAG, "Unknown tag " + tag + ".");
                Logger.w(TAG, "Current = " + clazz.getName());
            }
            recursiveList = recursiveList4;
        } else {
            try {
                Field[] allFields = clazz.getDeclaredFields();
                Object attribute3 = null;
                if (this.mFieldNameObfuscated) {
                    try {
                        if (!this.mClassNameObfuscated) {
                            Logger.i(TAG, "Class name is not obfuscated.");
                            int length = allFields.length;
                            Object matchAttribute = null;
                            Field matchField = null;
                            int matchCount = 0;
                            int matchCount2 = 0;
                            while (matchCount2 < length) {
                                Field field = allFields[matchCount2];
                                field.setAccessible(true);
                                Object tempAttribute = field.get(object);
                                if (tempAttribute != null) {
                                    recursiveList3 = recursiveList4;
                                    try {
                                        if (tempAttribute.getClass().getName().equals(this.mClassName)) {
                                            matchCount++;
                                            matchField = field;
                                            matchAttribute = tempAttribute;
                                        }
                                    } catch (IllegalAccessException | NoSuchFieldException e3) {
                                        e = e3;
                                        recursiveList = recursiveList3;
                                        Logger.w(TAG, e.getMessage(), e);
                                        if (this.mFieldName.equals("[foreach]")) {
                                        }
                                    }
                                } else {
                                    recursiveList3 = recursiveList4;
                                }
                                matchCount2++;
                                attribute3 = attribute3;
                                length = length;
                                recursiveList4 = recursiveList3;
                            }
                            recursiveList2 = recursiveList4;
                            attribute2 = attribute3;
                            if (matchCount > 0) {
                                if (matchCount == 1) {
                                    Logger.i(TAG, "Class name found.");
                                    Logger.i(TAG, "Field = " + matchField.getName());
                                    attribute = matchAttribute;
                                } else {
                                    Logger.i(TAG, "Too many same class name. Use index instead.");
                                    attribute = null;
                                }
                                if (this.mClassNameObfuscated || attribute == null) {
                                    if (allFields.length == this.mParentFieldCount) {
                                        Logger.w(TAG, "Parent field count is incorrect");
                                        Logger.w(TAG, "Current = " + clazz.getName());
                                        Logger.w(TAG, "Expected = " + this.mParentFieldCount);
                                        Logger.w(TAG, "Actual = " + allFields.length);
                                    } else if (this.mFieldIndex < 0 || this.mFieldIndex >= allFields.length) {
                                        Logger.w(TAG, "Field index is out of bounds.");
                                        Logger.w(TAG, "Current = " + clazz.getName());
                                        Logger.w(TAG, "Index = " + this.mFieldIndex + "/" + allFields.length);
                                    } else {
                                        Field field2 = clazz.getDeclaredFields()[this.mFieldIndex];
                                        field2.setAccessible(true);
                                        attribute = field2.get(object);
                                    }
                                }
                            } else {
                                Logger.i(TAG, "Class name not found.");
                            }
                        } else {
                            recursiveList2 = recursiveList4;
                            attribute2 = null;
                        }
                        attribute = attribute2;
                        if (allFields.length == this.mParentFieldCount) {
                        }
                    } catch (IllegalAccessException | NoSuchFieldException e4) {
                        e = e4;
                        recursiveList = recursiveList4;
                        Logger.w(TAG, e.getMessage(), e);
                        if (this.mFieldName.equals("[foreach]")) {
                        }
                    }
                } else {
                    recursiveList2 = recursiveList4;
                    try {
                    } catch (IllegalAccessException | NoSuchFieldException e5) {
                        e = e5;
                        recursiveList = recursiveList2;
                        e = e;
                        Logger.w(TAG, e.getMessage(), e);
                        if (this.mFieldName.equals("[foreach]")) {
                        }
                    }
                    try {
                        Field field3 = clazz.getDeclaredField(this.mFieldName);
                        field3.setAccessible(true);
                        attribute = field3.get(object);
                    } catch (IllegalAccessException | NoSuchFieldException e6) {
                        e = e6;
                        recursiveList = recursiveList2;
                        e = e;
                        Logger.w(TAG, e.getMessage(), e);
                        if (this.mFieldName.equals("[foreach]")) {
                        }
                    }
                }
                if (attribute != null) {
                    recursiveList = recursiveList2;
                    try {
                        recursiveList.add(new Pair<>(attribute, attribute.getClass()));
                    } catch (IllegalAccessException | NoSuchFieldException e7) {
                        e = e7;
                    }
                } else {
                    recursiveList = recursiveList2;
                }
            } catch (IllegalAccessException | NoSuchFieldException e8) {
                recursiveList = recursiveList4;
                e = e8;
                Logger.w(TAG, e.getMessage(), e);
                if (this.mFieldName.equals("[foreach]")) {
                }
            }
        }
        if (this.mFieldName.equals("[foreach]")) {
            JSONArray resultArray = new JSONArray();
            for (Pair<Object, Class<?>> pair : recursiveList) {
                if (this.mLeafList.size() > 0) {
                    JSONObject resultObject = new JSONObject();
                    for (ObjectNode objectNode : this.mLeafList) {
                        Pair<String, Object> attribute4 = objectNode.extractAttribute(pair.first, pair.second);
                        if (attribute4 != null) {
                            if (attribute4.first != null) {
                                resultObject.put(attribute4.first, attribute4.second);
                            } else if (attribute4.second instanceof JSONObject) {
                                JSONObject attributeObject = attribute4.second;
                                Iterator<String> iterator = attributeObject.keys();
                                while (iterator.hasNext()) {
                                    String key2 = iterator.next();
                                    resultObject.put(key2, attributeObject.get(key2));
                                }
                            }
                        }
                    }
                    resultArray.put(resultObject);
                } else {
                    resultArray.put(pair.first);
                }
            }
            return new Pair<>(this.mAlias, resultArray);
        } else if (recursiveList.size() != 1) {
            return null;
        } else {
            Pair<Object, Class<?>> pair2 = recursiveList.get(0);
            if (this.mLeafList.size() > 0) {
                JSONObject resultObject2 = new JSONObject();
                for (ObjectNode objectNode2 : this.mLeafList) {
                    Pair<String, Object> attribute5 = objectNode2.extractAttribute(pair2.first, pair2.second);
                    if (attribute5 != null) {
                        if (attribute5.first != null) {
                            resultObject2.put(attribute5.first, attribute5.second);
                        } else if (attribute5.second instanceof JSONObject) {
                            JSONObject attributeObject2 = attribute5.second;
                            Iterator<String> iterator2 = attributeObject2.keys();
                            while (iterator2.hasNext()) {
                                String key3 = iterator2.next();
                                resultObject2.put(key3, attributeObject2.get(key3));
                            }
                        }
                    }
                }
                if (this.mAttributeList.size() == 0) {
                    return new Pair<>(this.mAlias, resultObject2);
                }
                for (Attribute attribute6 : this.mAttributeList) {
                    Logger.i(TAG, "Create Attribute.\n" + attribute6.toString());
                    Map<String, String> valueMap = new HashMap<>();
                    Iterator<String> iterator3 = resultObject2.keys();
                    while (iterator3.hasNext()) {
                        String key4 = iterator3.next();
                        Object value2 = resultObject2.get(key4);
                        if (value2 != null) {
                            valueMap.put(key4, String.valueOf(value2));
                        }
                    }
                    Pair<String, String> resultPair = attribute6.getResult(valueMap);
                    if (resultPair != null) {
                        Logger.i(TAG, "Attribute created. " + ((String) resultPair.first) + " = " + ((String) resultPair.second));
                        resultObject2.put(resultPair.first, resultPair.second);
                    }
                }
                return new Pair<>(this.mAlias, resultObject2);
            }
            String value3 = String.valueOf(pair2.first);
            Logger.i(TAG, "Attribute found. " + this.mAlias + " = " + value3);
            String str = this.mCapturePattern;
            if (str != null) {
                value3 = RegularExpressionUtils.capture(value3, str);
                Logger.i(TAG, "Capture Attribute = " + value3);
            }
            return new Pair<>(this.mAlias, value3);
        }
    }

    private boolean isCustomTag(String fieldName) {
        return fieldName != null && fieldName.startsWith("[") && fieldName.endsWith("]");
    }

    private int stringToInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            Logger.w(TAG, e.getMessage());
            return -1;
        }
    }

    private Object autoFix(Object object, Class<?> clazz, String className) throws IllegalAccessException {
        Field[] allFields = clazz.getDeclaredFields();
        for (Field field : allFields) {
            field.setAccessible(true);
            Object value = field.get(object);
            if (value != null && value.getClass().getName().equals(className)) {
                return value;
            }
        }
        return null;
    }
}
