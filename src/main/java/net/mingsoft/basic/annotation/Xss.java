
package net.mingsoft.basic.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import net.mingsoft.basic.validator.XssValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Constraint(validatedBy = { XssValidator.class })
public @interface Xss {
    String message() default "Xss验证失败，内容不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
