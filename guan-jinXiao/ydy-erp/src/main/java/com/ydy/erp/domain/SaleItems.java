package com.ydy.erp.domain;

import com.ydy.common.annotation.Excel;
import lombok.*;
import com.ydy.common.core.domain.BaseEntity;
/**
 * 销售明细对象 sale_items
 *
 * @author ydy
 * @date 2026-01-04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleItems extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemsId;

    /** 销售ID */
    @Excel(name = "销售ID")
    private String saleId;

    /** 商品ID */
    @Excel(name = "商品ID")
    private String goodsId;

    /** 销售数量 */
    @Excel(name = "销售数量")
    private Double total;

    //商品名称
    private String goodsName;
}
