package com.synear.controller;


import com.synear.enums.YesOrNo;
import com.synear.pojo.Carousel;
import com.synear.pojo.Category;
import com.synear.pojo.vo.CategoryVO;
import com.synear.pojo.vo.NewItemsVO;
import com.synear.service.CarouselService;
import com.synear.service.CategoryService;
import com.synear.utils.JsonUtils;
import com.synear.utils.RedisUtil;
import com.synear.utils.SYNEARJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@Api(value = "首页", tags = {"首页展示的相关接口"})
@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private CarouselService carouselService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisUtil redisUtil;


    @ApiOperation(value = "获取首页轮播图列表", notes = "获取首页轮播图列表", httpMethod = "GET")
    @GetMapping("/carousel")
    public SYNEARJSONResult carousel(){
        List<Carousel> carousels = new ArrayList<>();
        String carouselStr = redisUtil.get("carousel");
        if (StringUtils.isBlank(carouselStr)) {
            carousels = carouselService.queryAll(YesOrNo.YES.type);
            redisUtil.set("carousel", JsonUtils.objectToJson(carousels));
        }else {
            carousels = JsonUtils.jsonToList(carouselStr, Carousel.class);
        }
        return SYNEARJSONResult.ok(carousels);
    }

    /**
     * 首页分类展示需求：
     * 1. 第一次刷新主页查询大分类，渲染展示到首页
     * 2. 如果鼠标上移到大分类，则加载其子分类的内容，如果已经存在子分类，则不需要加载（懒加载）
     */
    @ApiOperation(value = "获取商品分类(一级分类)", notes = "获取商品分类(一级分类)", httpMethod = "GET")
    @GetMapping("/cats")
    public SYNEARJSONResult cats(){
        List<Category> categories = new ArrayList<>();
        String catStr = redisUtil.get("cats");
        if (StringUtils.isBlank(catStr)) {
            categories = categoryService.queryAllRootLevelCat();
            redisUtil.set("cats",JsonUtils.objectToJson(categories));
        }else {
            categories = JsonUtils.jsonToList(catStr, Category.class);
        }

        return SYNEARJSONResult.ok(categories);
    }

    @ApiOperation(value = "获取商品子分类", notes = "获取商品子分类", httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")
    public SYNEARJSONResult subCat(
            @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId){

        if(rootCatId == null){   /// 若用户故意传一个很大的值呢？ 然后恶意多次请求？
            return SYNEARJSONResult.errorMsg("分类不存在");
        }

        List<CategoryVO> subCatList = new ArrayList<>();
        String subCatStr = redisUtil.get("subCat:" + rootCatId);
        if (StringUtils.isBlank(subCatStr)) {
            subCatList = categoryService.getSubCatList(rootCatId);
            redisUtil.set("subCat:" + rootCatId, JsonUtils.objectToJson(subCatList));
        }else {
            subCatList = JsonUtils.jsonToList(subCatStr, CategoryVO.class);
        }

        return SYNEARJSONResult.ok(subCatList);

    }


    @GetMapping("/sixNewItems/{rootCatId}")
    public SYNEARJSONResult sixNewItems(
            @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId){

        if(rootCatId == null){   /// 若用户故意传一个很大的值呢？ 然后恶意多次请求？
            return SYNEARJSONResult.errorMsg("分类不存在");
        }

        List<NewItemsVO> sixNewItemsLazy = categoryService.getSixNewItemsLazy(rootCatId);
        return SYNEARJSONResult.ok(sixNewItemsLazy);

    }






}
