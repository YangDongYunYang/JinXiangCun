package com.ydy.erp.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsToCount {
    private Double goodsCount; //商品数量
    private  Double normalCount; //库存正常数
    private  Double warningCount; //库存警告数

}
//ydy-erp/src/main/java/com/ydy/erp/domain/GoodsToCount.java
