package com.liboshuai.slr.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fhs.core.trans.vo.TransPojo;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体对象
 *
 * 为什么实现 {@link TransPojo} 接口？
 * 因为使用 Easy-Trans TransType.SIMPLE 模式，集成 MyBatis Plus 查询
 * （相比于 CommonEntity 少了逻辑删除字段）
 *
 * @author 李博帅
 */
@Data
@JsonIgnoreProperties(value = "transMap") // 由于 Easy-Trans 会添加 transMap 属性，避免 Jackson 在 Spring Cache 反序列化报错
public abstract class BaseDO implements Serializable, TransPojo {

    private static final long serialVersionUID = 4330416694254752263L;

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    private String creator;
    /**
     * 更新者，目前使用 SysUser 的 id 编号
     *
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    private String updater;

}
