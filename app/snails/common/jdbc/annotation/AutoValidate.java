package snails.common.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动验证
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface AutoValidate {// 验证失败时返回的页面，如果为空则返回JSON
    // 当不是AJAX时,需要一个返回页面给用户
    public String input() default "";
    public boolean isAjax() default false;
    public boolean validate() default true;
}
