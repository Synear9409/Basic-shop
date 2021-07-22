package com.synear.service.imp;

import com.synear.enums.CommentLevel;
import com.synear.mapper.*;
import com.synear.pojo.*;
import com.synear.pojo.vo.CommentLevelCountsVO;
import com.synear.pojo.vo.ItemCommentVO;
import com.synear.pojo.vo.SearchItemsVO;
import com.synear.pojo.vo.ShopcartVO;
import com.synear.service.ItemService;
import com.synear.utils.DesensitizationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ItemServiceImp implements ItemService {

    @Autowired
    private ItemsMapper itemsMapper;

    @Autowired
    private ItemsMapperCustom itemsMapperCustom;

    @Autowired
    private ItemsImgMapper itemsImgMapper;

    @Autowired
    private ItemsSpecMapper itemsSpecMapper;

    @Autowired
    private ItemsParamMapper itemsParamMapper;

    @Autowired
    private ItemsCommentsMapper itemsCommentsMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Items queryItemById(String itemId) {
        return itemsMapper.selectByPrimaryKey(itemId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsImg> queryItemImgList(String itemId) {
        Example example = new Example(ItemsImg.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId",itemId);

        return itemsImgMapper.selectByExample(example);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsSpec> queryItemSpecList(String itemId) {
        Example example = new Example(ItemsSpec.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId",itemId);

        return itemsSpecMapper.selectByExample(example);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsParam queryItemParam(String itemId) {
        Example example = new Example(ItemsParam.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId",itemId);

        return itemsParamMapper.selectOneByExample(example);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public CommentLevelCountsVO queryCommentCounts(String itemId) {
        Integer goodCounts = getCommentCounts(itemId, CommentLevel.GOOD.type);
        Integer normalCounts = getCommentCounts(itemId, CommentLevel.NORMAL.type);
        Integer badCounts = getCommentCounts(itemId, CommentLevel.BAD.type);
        Integer totalCounts = goodCounts + normalCounts + badCounts;

        CommentLevelCountsVO countsVO = new CommentLevelCountsVO();
        countsVO.setGoodCounts(goodCounts);
        countsVO.setNormalCounts(normalCounts);
        countsVO.setBadCounts(badCounts);
        countsVO.setTotalCounts(totalCounts);

        return countsVO;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemCommentVO> queryPagedComments(String itemId, Integer level/*, Integer page, Integer pageSize*/) {

        Map<String,Object> params = new HashMap<>();
        params.put("itemId",itemId);
        params.put("level",level);

        List<ItemCommentVO> itemCommentVOList = itemsMapperCustom.queryItemComments(params);

        for (ItemCommentVO commentVO : itemCommentVOList){
            commentVO.setNickname(DesensitizationUtil.commonDisplay(commentVO.getNickname()));
        }

        return itemCommentVOList;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<SearchItemsVO> searchItems(String keywords, String sort) {

        Map<String,Object> params = new HashMap<>();
        params.put("keywords",keywords);
        params.put("sort",sort);

        return itemsMapperCustom.searchItems(params);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<SearchItemsVO> searchItems(Integer catId, String sort) {

        Map<String,Object> params = new HashMap<>();
        params.put("catId",catId);
        params.put("sort",sort);

        return itemsMapperCustom.searchItemsByThirdCat(params);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ShopcartVO> queryItemsBySpecIds(String itemSpecIds) {
        return itemsMapperCustom.queryItemsBySpecIds(itemSpecIds);
    }

    /**
     * 获取各个级别的评论数
     * @param itemId
     * @param level
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    Integer getCommentCounts(String itemId, Integer level){
        ItemsComments commentsCount = new ItemsComments();
        commentsCount.setItemId(itemId);
        if (level != null){
            commentsCount.setCommentLevel(level);
        }
        return itemsCommentsMapper.selectCount(commentsCount);
    }
}
