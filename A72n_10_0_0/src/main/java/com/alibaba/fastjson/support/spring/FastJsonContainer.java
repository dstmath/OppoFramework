package com.alibaba.fastjson.support.spring;

public class FastJsonContainer {
    private PropertyPreFilters filters;
    private Object value;

    FastJsonContainer(Object body) {
        this.value = body;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value2) {
        this.value = value2;
    }

    public PropertyPreFilters getFilters() {
        return this.filters;
    }

    public void setFilters(PropertyPreFilters filters2) {
        this.filters = filters2;
    }
}
