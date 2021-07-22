package com.synear.service;

import com.synear.pojo.Users;
import com.synear.pojo.bo.UserBO;

public interface UserService {

    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    public boolean queryUsernameIsExist(String username);

    /**
     * 注册
     */
    public Users createUser(UserBO userBO);


    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    public Users queryUserForLogin(String username, String password);

}
