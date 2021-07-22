package com.synear.controller;

import com.synear.pojo.bo.ShopcartBO;
import com.synear.utils.JsonUtils;
import com.synear.utils.RedisUtil;
import com.synear.utils.SYNEARJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Api(value = "购物车接口controller", tags = {"购物车接口相关的api"})
@RequestMapping("shopcart")
@RestController
public class ShopcatController extends BaseController {

    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @PostMapping("/add")
    public SYNEARJSONResult add(
            @RequestParam String userId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        if (StringUtils.isBlank(userId)) {
            return SYNEARJSONResult.errorMsg("");
        }

        System.out.println(shopcartBO);

        // 前端用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redis缓存
        // 判断当前购物车中包含已经存在的商品， 如果存在则累加数额即可

        String shopCartJson = redisUtil.get(FOODIE_SHOPCART + ":" + userId);
        List<ShopcartBO> shopcartList = null;
        if (StringUtils.isNotBlank(shopCartJson)) {
            // redis中已有购物车
            shopcartList = JsonUtils.jsonToList(shopCartJson, ShopcartBO.class);
            // 判断购物车是否存在已有商品，存在则累加数目
            boolean isHaving = false;
            for (ShopcartBO s : shopcartList) {
                String tmpSpecId = s.getSpecId();
                if (tmpSpecId.equals(shopcartBO.getSpecId())) {
                    s.setBuyCounts(s.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            if (!isHaving) {
                // 循环结束，购物车中无重复商品，直接加入list
                shopcartList.add(shopcartBO);
            }
        } else {
            // redis无购物车
            shopcartList = new ArrayList<>();
            // 直接加入购物车
            shopcartList.add(shopcartBO);
        }

        // 覆盖现有的redis中购物车的数据
        redisUtil.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));

        return SYNEARJSONResult.ok();
    }

    @ApiOperation(value = "从购物车中删除商品", notes = "从购物车中删除商品", httpMethod = "POST")
    @PostMapping("/del")
    public SYNEARJSONResult del(
            @RequestParam String userId,
            @RequestParam String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)) {
            return SYNEARJSONResult.errorMsg("参数不能为空");
        }

        // 用户在页面删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除redis购物车中的商品
        String shopCartJson = redisUtil.get(FOODIE_SHOPCART + ":" + userId);
        if (StringUtils.isNotBlank(shopCartJson)) {
            // redis中已有购物车
            List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopCartJson, ShopcartBO.class);
            // 判断是否有已存在的商品
            for (ShopcartBO s : shopcartList) {
                String tmpSpecId = s.getSpecId();
                if (tmpSpecId.equals(itemSpecId)) {
                    shopcartList.remove(s);  // 配合break使用，是不会出现 <ConcurrentModificationException>并发修改错误
                    break;
                }
            }
            // 覆盖现有redis的购物车数据
            redisUtil.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));
        }


        return SYNEARJSONResult.ok();
    }

}
