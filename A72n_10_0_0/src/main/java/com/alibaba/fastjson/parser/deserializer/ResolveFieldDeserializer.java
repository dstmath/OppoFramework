package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ResolveFieldDeserializer extends FieldDeserializer {
    private final Collection collection;
    private final int index;
    private final Object key;
    private final List list;
    private final Map map;
    private final DefaultJSONParser parser;

    public ResolveFieldDeserializer(DefaultJSONParser parser2, List list2, int index2) {
        super(null, null);
        this.parser = parser2;
        this.index = index2;
        this.list = list2;
        this.key = null;
        this.map = null;
        this.collection = null;
    }

    public ResolveFieldDeserializer(Map map2, Object index2) {
        super(null, null);
        this.parser = null;
        this.index = -1;
        this.list = null;
        this.key = index2;
        this.map = map2;
        this.collection = null;
    }

    public ResolveFieldDeserializer(Collection collection2) {
        super(null, null);
        this.parser = null;
        this.index = -1;
        this.list = null;
        this.key = null;
        this.map = null;
        this.collection = collection2;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.FieldDeserializer
    public void setValue(Object object, Object value) {
        JSONArray jsonArray;
        Object array;
        Object item;
        if (this.map != null) {
            this.map.put(this.key, value);
        } else if (this.collection != null) {
            this.collection.add(value);
        } else {
            this.list.set(this.index, value);
            if ((this.list instanceof JSONArray) && (array = (jsonArray = (JSONArray) this.list).getRelatedArray()) != null && Array.getLength(array) > this.index) {
                if (jsonArray.getComponentType() != null) {
                    item = TypeUtils.cast(value, jsonArray.getComponentType(), this.parser.getConfig());
                } else {
                    item = value;
                }
                Array.set(array, this.index, item);
            }
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.FieldDeserializer
    public void parseField(DefaultJSONParser parser2, Object object, Type objectType, Map<String, Object> map2) {
    }
}
