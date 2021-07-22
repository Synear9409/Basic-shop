package com.synear.controller;


import com.github.pagehelper.PageHelper;
import com.synear.pojo.Items;
import com.synear.pojo.ItemsImg;
import com.synear.pojo.ItemsParam;
import com.synear.pojo.ItemsSpec;
import com.synear.pojo.vo.*;
import com.synear.service.ItemService;
import com.synear.utils.SYNEARJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(value = "商品接口", tags = {"商品信息展示的相关接口"})
@RestController
@RequestMapping("items")
public class itemsController extends BaseController{

    @Autowired
    private ItemService itemService;

    @GetMapping("/info/{itemId}")
    public SYNEARJSONResult info(
            @ApiParam(name = "itemId", value = "商品id", required = true)
            @PathVariable String itemId){

        if (StringUtils.isBlank(itemId)) {
            return SYNEARJSONResult.errorMsg("商品不存在");
        }

        Items items = itemService.queryItemById(itemId);
        List<ItemsImg> itemsImgList = itemService.queryItemImgList(itemId);
        List<ItemsSpec> itemsSpecList = itemService.queryItemSpecList(itemId);
        ItemsParam itemsParams = itemService.queryItemParam(itemId);

        /*ItemInfoVO infoVO = new ItemInfoVO();
        infoVO.setItem(items);
        infoVO.setItemImgList(itemsImgList);
        infoVO.setItemSpecList(itemsSpecList);
        infoVO.setItemParams(itemsParams);*/

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("item",items);
        resultMap.put("itemImgList",itemsImgList);
        resultMap.put("itemSpecList",itemsSpecList);
        resultMap.put("itemParams",itemsParams);

        return SYNEARJSONResult.ok(resultMap);

    }

    @ApiOperation(value = "查询商品评价等级", notes = "查询商品评价等级", httpMethod = "GET")
    @GetMapping("/commentLevel")
    public SYNEARJSONResult commentLevel(
            @ApiParam(name = "itemId", value = "商品id", required = true)
            @RequestParam String itemId){

        if (StringUtils.isBlank(itemId)) {
            return SYNEARJSONResult.errorMsg(null);
        }

        CommentLevelCountsVO levelCountsVO = itemService.queryCommentCounts(itemId);
        return SYNEARJSONResult.ok(levelCountsVO);

    }

    @ApiOperation(value = "查询商品评论", notes = "查询商品评论", httpMethod = "GET")
    @GetMapping("/comments")
    public SYNEARJSONResult comments(
            @ApiParam(name = "itemId", value = "商品id", required = true)
            @RequestParam String itemId,
            @ApiParam(name = "level", value = "评价等级", required = false)
            @RequestParam Integer level,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam(required = false, defaultValue = COMMON_PAGE_SIZE) Integer pageSize){

        if (StringUtils.isBlank(itemId)) {
            return SYNEARJSONResult.errorMsg(null);
        }

        PageHelper.startPage(page,pageSize);

        List<ItemCommentVO> itemCommentVOList = itemService.queryPagedComments(itemId, level);
        return SYNEARJSONResult.ok(getPagedGrid(itemCommentVOList,page));
    }

    @ApiOperation(value = "搜索商品列表", notes = "搜索商品列表", httpMethod = "GET")
    @GetMapping("/search")
    public SYNEARJSONResult search(
            @ApiParam(name = "keywords", value = "关键字", required = true)
            @RequestParam String keywords,
            @ApiParam(name = "sort", value = "排序", required = false)
            @RequestParam String sort,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam(required = false, defaultValue = PAGE_SIZE) Integer pageSize){

        if (StringUtils.isBlank(keywords)) {
            return SYNEARJSONResult.errorMsg(null);
        }

        PageHelper.startPage(page,pageSize);

        List<SearchItemsVO> searchItemsVOList = itemService.searchItems(keywords, sort);
        return SYNEARJSONResult.ok(getPagedGrid(searchItemsVOList,page));
    }

    @ApiOperation(value = "通过分类id搜索商品列表", notes = "通过分类id搜索商品列表", httpMethod = "GET")
    @GetMapping("/catItems")
    public SYNEARJSONResult catItems(
            @ApiParam(name = "catId", value = "三级分类id", required = true)
            @RequestParam Integer catId,
            @ApiParam(name = "sort", value = "排序", required = false)
            @RequestParam String sort,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam(required = false, defaultValue = PAGE_SIZE) Integer pageSize){

        if (catId == null) {
            return SYNEARJSONResult.errorMsg(null);
        }

        PageHelper.startPage(page,pageSize);

        List<SearchItemsVO> catItemList = itemService.searchItems(catId, sort);
        return SYNEARJSONResult.ok(getPagedGrid(catItemList,page));
    }

    // 用于用户长时间未登录网站，刷新购物车中的数据（主要是商品价格），类似京东淘宝
    @ApiOperation(value = "根据商品规格ids查找最新的商品数据", notes = "根据商品规格ids查找最新的商品数据", httpMethod = "GET")
    @GetMapping("/refresh")
    public SYNEARJSONResult refresh(
            @ApiParam(name = "itemSpecIds", value = "拼接的规格ids", required = true, example = "1001,1003,1005")
            @RequestParam String itemSpecIds){

        if(StringUtils.isBlank(itemSpecIds)){   //则说明规格并无变化，直接返回成功
            return SYNEARJSONResult.ok();
        }

        List<ShopcartVO> shopCartVOList = itemService.queryItemsBySpecIds(itemSpecIds);
        return SYNEARJSONResult.ok(shopCartVOList);

    }

}
