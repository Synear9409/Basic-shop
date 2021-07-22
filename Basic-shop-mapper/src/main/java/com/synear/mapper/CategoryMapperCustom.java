package com.synear.mapper;

import com.synear.my.mapper.MyMapper;
import com.synear.pojo.Category;
import com.synear.pojo.vo.CategoryVO;
import com.synear.pojo.vo.NewItemsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CategoryMapperCustom extends MyMapper<Category> {

    public List<CategoryVO> getSubCatList(Integer rootCatId);

    public List<NewItemsVO> getSixNewItemsLazy(@Param("params") Map<String,Object> map);

}
