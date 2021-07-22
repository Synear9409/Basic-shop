package com.synear.service;

import com.synear.pojo.Items;
import com.synear.pojo.ItemsImg;
import com.synear.pojo.ItemsParam;
import com.synear.pojo.ItemsSpec;
import com.synear.pojo.vo.CommentLevelCountsVO;
import com.synear.pojo.vo.ItemCommentVO;
import com.synear.pojo.vo.SearchItemsVO;
import com.synear.pojo.vo.ShopcartVO;

import java.util.List;

public interface ItemService {

    /**
     * 根据商品ID查询详情
     * @param itemId
     */
    public Items queryItemById(String itemId);

    /**
     * 根据商品id查询商品图片列表
     * @param itemId
     */
    public List<ItemsImg> queryItemImgList(String itemId);

    /**
     * 根据商品id查询商品规格
     * @param itemId
     */
    public List<ItemsSpec> queryItemSpecList(String itemId);

    /**
     * 根据商品id查询商品参数
     * @param itemId
     */
    public ItemsParam queryItemParam(String itemId);

    /**
     * 根据商品id查询商品的评价等级数量
     * @param itemId
     */
    public CommentLevelCountsVO queryCommentCounts(String itemId);

    /**
     * 根据商品id查询商品的评价（分页）
     * @param itemId
     * @param level
     */
    public List<ItemCommentVO> queryPagedComments(String itemId, Integer level);

    /**
     * 搜索商品列表
     * @param keywords
     * @param sort
     */
    public List<SearchItemsVO> searchItems(String keywords, String sort);

    /**
     * 根据分类id搜索商品列表
     * @param catId
     * @param sort
     */
    public List<SearchItemsVO> searchItems(Integer catId, String sort);

    /**
     * 根据规格ids查询最新的购物车中商品数据（用于刷新渲染购物车中的商品数据）
     * @param itemSpecIds
     */
    public List<ShopcartVO> queryItemsBySpecIds(String itemSpecIds);

}
