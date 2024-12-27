package com.liboshuai.slr.framework.common.validation;

import cn.hutool.core.collection.CollUtil;
import com.liboshuai.slr.framework.common.core.StringArrayValuable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.*;
import java.util.stream.Collectors;

public class InStringEnumCollectionValidator implements ConstraintValidator<InStringEnum, Collection<String>> {

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
    public boolean isValid(Collection<String> list, ConstraintValidatorContext context) {
        // 校验通过
        if (new HashSet<>(values).containsAll(list)) {
            return true;
        }
        // 校验不通过，自定义提示语句（因为，注解上的 value 是枚举类，无法获得枚举类的实际值）
        context.disableDefaultConstraintViolation(); // 禁用默认的 message 的值
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()
                .replaceAll("\\{value}", CollUtil.join(list, ","))).addConstraintViolation(); // 重新添加错误提示语句
        return false;
    }

}
