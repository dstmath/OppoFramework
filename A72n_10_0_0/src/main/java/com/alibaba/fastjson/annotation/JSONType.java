package com.alibaba.fastjson.annotation;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONType {
    boolean alphabetic() default true;

    boolean asm() default true;

    Class<?> builder() default Void.class;

    Class<?> deserializer() default Void.class;

    String[] ignores() default {};

    String[] includes() default {};

    Class<?> mappingTo() default Void.class;

    PropertyNamingStrategy naming() default PropertyNamingStrategy.CamelCase;

    String[] orders() default {};

    Feature[] parseFeatures() default {};

    Class<?>[] seeAlso() default {};

    boolean serializeEnumAsJavaBean() default false;

    Class<?> serializer() default Void.class;

    SerializerFeature[] serialzeFeatures() default {};

    Class<? extends SerializeFilter>[] serialzeFilters() default {};

    String typeKey() default "";

    String typeName() default "";
}
