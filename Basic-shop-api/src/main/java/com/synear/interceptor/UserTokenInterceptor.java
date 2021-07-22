package com.synear.interceptor;

import com.synear.utils.JsonUtils;
import com.synear.utils.RedisUtil;
import com.synear.utils.SYNEARJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

public class UserTokenInterceptor implements HandlerInterceptor {

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 拦截请求，在访问controller调用之前
     * @param request
     * @param response
     * @param handler
     * @return false即表示被拦截  true则表示被放行
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String userId = request.getHeader("headerUserId");
        String userBase64Token = request.getHeader("headerUserToken");
        byte[] bytes = Base64.getDecoder().decode(userBase64Token.getBytes());
        String userToken  = new String(bytes, "UTF-8");

        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {
            String uniqueToken = redisUtil.get(REDIS_USER_TOKEN + ":" + userId);
            if (StringUtils.isBlank(uniqueToken)) {
//                System.out.println("请先登录!!!");
                returnErrorResponse(response, SYNEARJSONResult.errorMsg("请先登录!!!"));
                return false;
            } else if (!uniqueToken.equals(userToken)) {
//                System.out.println("账号异地登录...");
                returnErrorResponse(response, SYNEARJSONResult.errorMsg("账号异地登录..."));
                return false;
            }
        } else {
//            System.out.println("请先登录!!!");
            returnErrorResponse(response, SYNEARJSONResult.errorMsg("请先登录!!!"));
            return false;
        }

        return true;
    }

    /**
     * 请求访问controller之后，渲染视图之前
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 请求访问controller之后，渲染视图之后
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    public void returnErrorResponse(HttpServletResponse response,
                                    SYNEARJSONResult result){
        OutputStream out = null;
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/json");
            out = response.getOutputStream();
            out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
