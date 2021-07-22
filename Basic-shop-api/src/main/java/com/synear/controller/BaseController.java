package com.synear.controller;


import com.github.pagehelper.PageInfo;
import com.synear.pojo.Users;
import com.synear.pojo.vo.UserVO;
import com.synear.utils.PagedGridResult;
import com.synear.utils.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
public class BaseController {

    @Autowired
    private RedisUtil redisUtil;

    public static final String UTF_8 = "UTF-8";
    public static final String FOODIE_SHOPCART = "shopcart";
    public static final String COMMON_PAGE_SIZE = "8";
    public static final String PAGE_SIZE = "20";
    public static final String REDIS_USER_TOKEN = "redis_user_token";

    // 支付中心的调用地址
    String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";		// produce

    // 微信支付成功 -> 支付中心 -> 天天吃货平台
    //                       |-> 回调通知的url
    String payReturnUrl = "http://api.z.mukewang.com/foodie-dev-api/orders/notifyMerchantOrderPaid";


    protected PagedGridResult getPagedGrid(List<?> list, Integer page){
        PagedGridResult gridResult = new PagedGridResult();
        PageInfo<?> pageInfo = new PageInfo<>(list);
        gridResult.setPage(page);
        gridResult.setRows(list);
        gridResult.setTotal(pageInfo.getPages());      //总页数
        gridResult.setRecords(pageInfo.getTotal());
        return gridResult;
    }

    public UserVO convertUserVoAndSetToken(Users user) {
        // 生成用户token，存入redis会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisUtil.set(REDIS_USER_TOKEN + ":" + user.getId(), uniqueToken);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        try {
            byte[] encode = Base64.getEncoder().encode(uniqueToken.getBytes());
            userVO.setUniqueToken(new String(encode,UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userVO;
    }

    /*public static void main(String[] args) throws UnsupportedEncodingException {
        String text = "年号123";
        byte[] encode = Base64.getEncoder().encode(text.getBytes());
        String s = new String(encode, UTF_8);
        System.out.println(s);
        System.out.println(new String(Base64.getDecoder().decode(s.getBytes())));
    }*/

}
