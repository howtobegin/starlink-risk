package com.liboshuai.slr.framework.common.enums;

import cn.hutool.core.util.ObjUtil;
import com.liboshuai.slr.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 基础状态枚举
 *
 * @author 李博帅
 */
@Getter
@AllArgsConstructor
public enum BaseStatusEnum implements IntArrayValuable {

    ENABLE(0, "开启"),
    DISABLE(1, "关闭");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BaseStatusEnum::getStatus).toArray();

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    public static boolean isEnable(Integer status) {
        return ObjUtil.equal(ENABLE.status, status);
    }

    public static boolean isDisable(Integer status) {
        return ObjUtil.equal(DISABLE.status, status);
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
