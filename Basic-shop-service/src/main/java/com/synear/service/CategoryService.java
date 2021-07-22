package com.synear.service;

import com.synear.pojo.Category;
import com.synear.pojo.vo.CategoryVO;
import com.synear.pojo.vo.NewItemsVO;

import java.util.List;

public interface CategoryService {

    /**
     * 查询所有一级分类
     */
    public List<Category> queryAllRootLevelCat();

    /**
     * 根据一级分类id查询子分类信息
     * @param rootCatId
     */
    public List<CategoryVO> getSubCatList(Integer rootCatId);

    /**
     * 查询首页每个一级分类下的6条最新商品数据
     * @param rootCatId
     */
    public List<NewItemsVO> getSixNewItemsLazy(Integer rootCatId);

}
