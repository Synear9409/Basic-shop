package com.synear.controller;

import com.synear.pojo.Users;
import com.synear.pojo.vo.UserVO;
import com.synear.service.UserService;
import com.synear.utils.JsonUtils;
import com.synear.utils.MD5Utils;
import com.synear.utils.RedisUtil;
import com.synear.utils.SYNEARJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Controller
public class SSOController {

    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_TICKET = "redis_user_ticket";
    public static final String REDIS_TMP_TICKET = "redis_tmp_ticket";

    public static final String COOKIE_TMP_TICKET = "cookie_user_ticket";

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/login")
    public String login(String returnUrl,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response) {

        model.addAttribute("returnUrl", returnUrl);

        // 第二种情况： （这种情况就是在某个系统(www.mtv.com)已经通过了CAS端的验证，然后再去登录另一个系统(www.music.com)）
        // 获取userTicket，如果cookie中能够获取到，证明登录过，此时签发一个临时凭证tmpTicket

        // 1、获取userTicket
        String userTicketBase64 = getCookie(REDIS_USER_TICKET, request);
        if (StringUtils.isNotBlank(userTicketBase64)) {
            byte[] decode = Base64.getDecoder().decode(userTicketBase64.getBytes());
            String userTicket = new String(decode, StandardCharsets.UTF_8);

            // 2、校验
            boolean isVerified = verifyUserTicket(userTicket);

            if (isVerified) {
                // 3、签发临时凭证
                String tmpTicket = createTmpTicket();
                return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
            }
        }

        // 第一种情况：
        // 用户从未登录，第一次跳转到CAS统一登录页面
        return "login";
    }

    /**
     * CAS统一登录接口
     *          目的：
     *              1、登录后创建用户的全局会话信息       -> uniqueToken
     *              2、创建用户的全局门票，用以表示在CAS段是否登录     -> userTicket
     *              3、创建用户临时凭证(门票)，用于回跳回传      -> tmpTicket
     * @param username
     * @param password
     * @param returnUrl
     * @param model
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping("/doLogin")
    public String doLogin(String username,
                          String password,
                          String returnUrl,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) throws Exception{

        model.addAttribute("returnUrl", returnUrl);

        // 1、判断用户名和密码不能为空
        if(StringUtils.isBlank(username) ||
                StringUtils.isBlank(password)){
            model.addAttribute("errmsg","用户名或密码不能为空");
            return "login";
        }
        // 2、实现登录
        Users result = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));
        if(result == null){
            model.addAttribute("errmsg","用户名或密码不正确");
            return "login";
        }

        // 3、生成用户redis会话--分布式会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(result, userVO);
        userVO.setUniqueToken(uniqueToken);
        redisUtil.set(REDIS_USER_TOKEN + ":" + result.getId(), JsonUtils.objectToJson(userVO));

        // 4、生成全局门票 userTicket，代表用户在CAS端登录过
        String userTicket = UUID.randomUUID().toString().trim();

        // 4.1、将用户全局门票信息存入CAS端Cookie中
        byte[] encode = Base64.getEncoder().encode(userTicket.getBytes());
        setCookie(REDIS_USER_TICKET, new String(encode, StandardCharsets.UTF_8), response);

        // 5、userTicket关联用户id，并且放入redis中，代表这个用户有门票了，可以到各个景区游玩
        redisUtil.set(REDIS_USER_TICKET + ":" + userTicket, result.getId());

        // 6、生成临时凭证，回跳调用网站，这是有CAS端所签发的一次性临时ticket  (有时效性)
        String tmpTicket = createTmpTicket();

        // 回跳调用方网站
        return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
    }

    /**
     * 调用CAS端接收到临时凭证tmpTicket后，向CAS端发起请求验证tmpTicket
     * @param tmpTicket
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/verifyTmpTicket")
    @ResponseBody
    public SYNEARJSONResult verifyTmpTicket(String tmpTicket,
                        HttpServletRequest request,
                        HttpServletResponse response) throws Exception {

        // 使用临时凭证来验证用户是否登录，如果登录过，则将用户会话信息返回给调用网站
        // 使用完毕后，需要销毁临时凭证

        String tmpTicketVal = redisUtil.get(REDIS_TMP_TICKET + ":" + tmpTicket);
        if (StringUtils.isBlank(tmpTicketVal)) {
            return SYNEARJSONResult.errorUserTicket("临时凭证校验异常...");
        }

        // 1、若临时凭证ok，则需要销毁，并且拿到CAS端的cookies中的全局userTicket，以此再获得用户会话信息
        if (!tmpTicketVal.equals(MD5Utils.getMD5Str(tmpTicket))) {
            return SYNEARJSONResult.errorUserTicket("临时凭证校验异常...");
        }
        // 2、删除临时凭证
        redisUtil.del(REDIS_TMP_TICKET + ":" + tmpTicket);

        // 3、获取CAS端的cookie中的全局门票userTicket
        String userTicketBase64 = getCookie(REDIS_USER_TICKET, request);
        if (StringUtils.isBlank(userTicketBase64)) {
            return SYNEARJSONResult.errorUserTicket("临时凭证校验异常...");
        }
        byte[] decode = Base64.getDecoder().decode(userTicketBase64.getBytes());
        String userTicket = new String(decode, "UTF-8");

        String userId = redisUtil.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return SYNEARJSONResult.errorUserTicket("临时凭证校验异常...");
        }

        // 4、验证该全局门票对应的用户会话信息是否存在，并返回给调用方
        String userTips = redisUtil.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userTips)) {
            return SYNEARJSONResult.errorUserTicket("临时凭证校验异常...");
        }
        return SYNEARJSONResult.ok(JsonUtils.jsonToPojo(userTips, UserVO.class));
    }

    @GetMapping("/logout")
    public SYNEARJSONResult logout(String userId,
                        HttpServletRequest request,
                        HttpServletResponse response) {

        // 1、获取CAS中的全局凭证
        String userTicketBase64 = getCookie(REDIS_USER_TICKET, request);
        byte[] decode = Base64.getDecoder().decode(userTicketBase64.getBytes());
        String userTicket = new String(decode, StandardCharsets.UTF_8);

        // 2、清除userTicket凭证，redis|cookie
        deleteCookie(userTicket, response);
        redisUtil.del(REDIS_USER_TICKET + ":" + userTicket);

        // 3、清除用户会话信息
        redisUtil.del(REDIS_USER_TOKEN + ":" + userId);

        return SYNEARJSONResult.ok();
    }

    private String createTmpTicket() {
        String tmpTicket = UUID.randomUUID().toString().trim();
        try {
            redisUtil.set(REDIS_TMP_TICKET + ":" + tmpTicket, MD5Utils.getMD5Str(tmpTicket), 600);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpTicket;
    }

    /**
     * 非首次登录的校验全局凭证、用户会话信息的方法
     * @param userTicket
     * @return
     */
    private boolean verifyUserTicket(String userTicket) {

        // 1、验证CAS全局凭证不能为空
        if (StringUtils.isBlank(userTicket)) {
            return false;
        }

        // 2、验证CAS全局凭证是否有效
        String userId = redisUtil.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return false;
        }

        // 3、验证该CAS全局凭证对应的用户会话信息 userToken是否存在
        String userTips = redisUtil.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userTips)) {
            return false;
        }

        return true;
    }

    private void setCookie(String key, String value, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, value);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String getCookie(String key, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null || StringUtils.isBlank(key)) {
            return null;
        }

        String cookieVal = null;
        for (Cookie c : cookies) {
            if (c.getName().equals(key)) {
                cookieVal = c.getValue();
                break;
            }
        }
        return cookieVal;
    }

    private void deleteCookie(String key, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, null);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }

}
