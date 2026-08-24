
package net.mingsoft.basic.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.mingsoft.basic.annotation.Xss;
import net.mingsoft.basic.util.JsoupUtil;

public class XssValidator implements ConstraintValidator<Xss, String> {
    @Override
    public boolean isValid(String fieldValue, ConstraintValidatorContext constraintValidatorContext) {
        if (fieldValue == null) {
            return true;
        }
        return !JsoupUtil.hasXSS(fieldValue);
    }
}
