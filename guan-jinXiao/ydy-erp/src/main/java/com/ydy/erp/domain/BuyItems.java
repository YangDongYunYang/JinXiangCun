package com.ydy.erp.domain;

import com.ydy.common.annotation.Excel;
import lombok.*;
import com.ydy.common.core.domain.BaseEntity;
/**
 * 采购明细对象 buy_items
 *
 * @author ydy
 * @date 2026-01-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyItems extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemsId;

    /** 采购ID */
    @Excel(name = "采购ID")
    private String buyId;

    /** 商品ID */
    @Excel(name = "商品ID")
    private String goodsId;

    /** 采购数量 */
    @Excel(name = "采购数量")
    private Long total;

    /** 采购单价 */
    @Excel(name = "采购单价")
    private Long unitPrice;

      /** 商品名称 */
    @Excel(name = "商品名称")
    private String goodsName;
}
