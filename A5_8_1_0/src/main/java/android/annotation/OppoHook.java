package android.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.CLASS)
public @interface OppoHook {

    public enum OppoHookType {
        CHANGE_ACCESS,
        CHANGE_CODE,
        CHANGE_CODE_AND_ACCESS,
        CHANGE_RESOURCE,
        CHANGE_BASE_CLASS,
        CHANGE_PARAMETER,
        NEW_FIELD,
        NEW_METHOD,
        NEW_CLASS
    }

    public enum OppoRomType {
        ROM,
        OPPO,
        QCOM,
        MTK
    }

    OppoHookType level();

    String note() default "null";

    OppoRomType property() default OppoRomType.ROM;
}
