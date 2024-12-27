package com.liboshuai.slr.framework.common.validation;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InStringEnumValidator implements ConstraintValidator<InStringEnum, String> {

    private List<String> values;

    @Override
    public void initialize(InStringEnum annotation) {
        StringArrayValuable[] enums = annotation.value().getEnumConstants();
        if (enums.length == 0) {
            this.values = Collections.emptyList();
        } else {
            this.values = Arrays.stream(enums[0].array()).collect(Collectors.toList());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时，默认不校验，即认为通过
        if (StringUtils.hasText(value)) {
            return true;
        }
        // 校验通过
        if (values.contains(value)) {
            return true;
        }
        // 校验不通过，自定义提示语句（因为，注解上的 value 是枚举类，无法获得枚举类的实际值）
        context.disableDefaultConstraintViolation(); // 禁用默认的 message 的值
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()
                .replaceAll("\\{value}", values.toString())).addConstraintViolation(); // 重新添加错误提示语句
        return false;
    }

}


