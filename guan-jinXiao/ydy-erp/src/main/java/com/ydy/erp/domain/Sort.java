package com.ydy.erp.domain;

import com.ydy.common.annotation.Excel;
import com.ydy.common.core.domain.TreeEntity;
import lombok.*;
import com.ydy.common.core.domain.BaseEntity;

/**
 * 商品分类对象 sort
 *
 * @author ydy
 * @date 2026-01-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sort extends TreeEntity
{
    private static final long serialVersionUID = 1L;


    //父ID
    private Long parentId;
    /** 分类ID */
    private Long sortId;

    /** 分类名称 */
    @Excel(name = "分类名称")
    private String classifyName;


}
