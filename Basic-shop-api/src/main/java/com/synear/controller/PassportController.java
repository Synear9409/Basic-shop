package com.synear.controller;


import com.synear.pojo.Users;
import com.synear.pojo.bo.UserBO;
import com.synear.pojo.vo.UserVO;
import com.synear.service.UserService;
import com.synear.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


@Api(value = "注册登录",tags = {"用于注册登录的相关接口"})
@RestController
@RequestMapping("passport")
public class PassportController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    public SYNEARJSONResult usernameIsExist(@RequestParam String username){

        // 判断传入参数不能为空
        if(StringUtils.isBlank(username)){
            return SYNEARJSONResult.errorMsg("用户名不能为空");
        }

        // 查找注册的用户名是否已存在
        boolean isExist = userService.queryUsernameIsExist(username);

        return !isExist ? SYNEARJSONResult.ok() : SYNEARJSONResult.errorMsg("用户名已经存在");
    }

    @ApiOperation(value = "用户注册", notes = "用户注册", httpMethod = "POST")
    @PostMapping("/regist")
    public SYNEARJSONResult regist(@RequestBody UserBO userBO,
                                   HttpServletRequest request,
                                   HttpServletResponse response){

        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPwd = userBO.getConfirmPassword();

        // 输入不能为空
        if(StringUtils.isBlank(username) ||
                StringUtils.isBlank(password) ||
                    StringUtils.isBlank(confirmPwd)){
            return SYNEARJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 防止用户在提示用户名已存在时，忽略提示继续注册（若前端按钮没有特殊处理）
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist){
            return SYNEARJSONResult.errorMsg("用户名已经存在");
        }

        // 密码长度不能少于6
        if(password.length() > 6){
            return SYNEARJSONResult.errorMsg("密码长度不能少于6");
        }

        // 两次密码输入不一致
        if (!password.equals(confirmPwd)) {
            return SYNEARJSONResult.errorMsg("两次密码输入不一致");
        }

        // 注册
        Users user = userService.createUser(userBO);

        if(user == null){
            return SYNEARJSONResult.errorMsg("注册失败，稍后重试");
        }

        // 生成用户token，存入redis会话
        UserVO userVO = convertUserVoAndSetToken(user);

//        setNullProperty(user);
        CookieUtils.setCookie(request,response,"user",
                JsonUtils.objectToJson(userVO),true);

        // TODO 同步购物车数据

        return SYNEARJSONResult.ok();
    }

    private void setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
    }

    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("/login")
    public SYNEARJSONResult login(@RequestBody UserBO userBO,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws Exception {

        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 输入不能为空
        if(StringUtils.isBlank(username) ||
                StringUtils.isBlank(password)){
            return SYNEARJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 登录
        Users result = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));

        if(result == null){
            return SYNEARJSONResult.errorMsg("用户名或密码不正确!");
        }

//        setNullProperty(result);
        // 生成用户token，存入redis会话
        UserVO userVO = convertUserVoAndSetToken(result);

        CookieUtils.setCookie(request,response,"user",
                JsonUtils.objectToJson(userVO),true);

        // TODO 同步购物车数据

        return SYNEARJSONResult.ok(result);

    }

    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod = "POST")
    @PostMapping("/logout")
    public SYNEARJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        // 清除用户的相关信息的cookie
        CookieUtils.deleteCookie(request,response,"user");

        // TODO 用户退出登录，需要清空购物车 COOKIES

        // 分布式会话中需要清除用户数据
        redisUtil.del(REDIS_USER_TOKEN + ":" + userId);

        return SYNEARJSONResult.ok();
    }


}

