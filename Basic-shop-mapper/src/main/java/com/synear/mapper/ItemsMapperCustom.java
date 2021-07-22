package com.synear.mapper;

import com.synear.my.mapper.MyMapper;
import com.synear.pojo.Items;
import com.synear.pojo.vo.ItemCommentVO;
import com.synear.pojo.vo.SearchItemsVO;
import com.synear.pojo.vo.ShopcartVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsMapperCustom extends MyMapper<Items> {

    public List<ItemCommentVO> queryItemComments(@Param("params") Map<String, Object> map);

    public List<SearchItemsVO> searchItems(@Param("params") Map<String, Object> map);

    public List<SearchItemsVO> searchItemsByThirdCat(@Param("params") Map<String, Object> map);

    public List<ShopcartVO> queryItemsBySpecIds(String itemSpecIds);

}
