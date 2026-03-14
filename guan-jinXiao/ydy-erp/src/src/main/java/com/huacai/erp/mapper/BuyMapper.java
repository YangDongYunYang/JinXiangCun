package com.ydy.erp.mapper;

import java.util.List;
import com.ydy.erp.domain.Buy;
import com.ydy.erp.domain.BuyItems;

/**
 * 采购Mapper接口
 * 
 * @author ydy
 * @date 2026-01-02
 */
public interface BuyMapper 
{
    /**
     * 查询采购
     * 
     * @param buyId 采购主键
     * @return 采购
     */
    public Buy selectBuyByBuyId(String buyId);

    /**
     * 查询采购列表
     * 
     * @param buy 采购
     * @return 采购集合
     */
    public List<Buy> selectBuyList(Buy buy);

    /**
     * 新增采购
     * 
     * @param buy 采购
     * @return 结果
     */
    public int insertBuy(Buy buy);

    /**
     * 修改采购
     * 
     * @param buy 采购
     * @return 结果
     */
    public int updateBuy(Buy buy);

    /**
     * 删除采购
     * 
     * @param buyId 采购主键
     * @return 结果
     */
    public int deleteBuyByBuyId(String buyId);

    /**
     * 批量删除采购
     * 
     * @param buyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBuyByBuyIds(String[] buyIds);

    /**
     * 批量删除采购明细
     * 
     * @param buyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBuyItemsByBuyIds(String[] buyIds);
    
    /**
     * 批量新增采购明细
     * 
     * @param buyItemsList 采购明细列表
     * @return 结果
     */
    public int batchBuyItems(List<BuyItems> buyItemsList);
    

    /**
     * 通过采购主键删除采购明细信息
     * 
     * @param buyId 采购ID
     * @return 结果
     */
    public int deleteBuyItemsByBuyId(String buyId);
}
