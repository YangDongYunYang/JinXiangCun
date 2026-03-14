package com.ydy.erp.domain;

import com.ydy.common.annotation.Excel;
import lombok.*;
import com.ydy.common.core.domain.BaseEntity;

/**
 * 商品对象 goods
 *
 * @author ydy
 * @date 2026-01-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private String goodsId;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String goodsName;

    /** 分类ID */
    @Excel(name = "分类ID")
    private Long sortId;

    /** 分类名称 */
    @Excel(name = "分类名称")
    private String classifyName;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 规格 */
    @Excel(name = "规格")
    private String specs;

    /** 库存 */
    @Excel(name = "库存")
    private Double stock;

    /** 销售价 */
    @Excel(name = "销售价")
    private Double salesPrice;

    /** 库存阈值 */
    @Excel(name = "库存阈值")
    private Double threshold;


}
