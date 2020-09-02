package com.color.animation;

import android.annotation.TargetApi;
import android.util.FloatProperty;

public abstract class FloatPropertyCompat<T> {
    final String mPropertyName;

    public abstract float getValue(T t);

    public abstract void setValue(T t, float f);

    public FloatPropertyCompat(String name) {
        this.mPropertyName = name;
    }

    @TargetApi(24)
    public static <T> FloatPropertyCompat<T> createFloatPropertyCompat(final FloatProperty<T> property) {
        return new FloatPropertyCompat<T>(property.getName()) {
            /* class com.color.animation.FloatPropertyCompat.AnonymousClass1 */

            @Override // com.color.animation.FloatPropertyCompat
            public float getValue(T object) {
                return ((Float) property.get(object)).floatValue();
            }

            @Override // com.color.animation.FloatPropertyCompat
            public void setValue(T object, float value) {
                property.setValue(object, value);
            }
        };
    }
}
