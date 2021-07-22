package com.synear.controller;

import com.synear.pojo.Users;
import com.synear.pojo.vo.UserVO;
import com.synear.resource.FileResource;
import com.synear.service.FdfsService;
import com.synear.utils.CookieUtils;
import com.synear.utils.JsonUtils;
import com.synear.utils.SYNEARJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("fdfs")
public class FastdfsController extends BaseController{

    @Autowired
    private FileResource fileResource;

    /*@Autowired
    private CenterUserService centerUserService;*/


    @Autowired
    private FdfsService fdfsService;

    @PostMapping("/uploadFace")
    public SYNEARJSONResult uploadFace(String userId,
                                       MultipartFile file,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws Exception{

        String path = "";

        // 开始文件上传
        if (file != null) {
            // 获取上传文件的名称
            String fileName = file.getOriginalFilename();
            if (StringUtils.isNotBlank(fileName)) {

                // 文件重命名
                String[] fileNameArr = fileName.split("\\.");

                // 获取文件后缀名
                String suffix = fileNameArr[fileNameArr.length - 1 ];

                if (!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg")) {
                    return SYNEARJSONResult.errorMsg("图片格式不正确！");
                }

                path = fdfsService.upload(file, suffix);

            } else {
                return SYNEARJSONResult.errorMsg("文件名不能为空！");
            }

            if (StringUtils.isNotBlank(path)) {
                String finalUserFaceUrl = fileResource.getHost() + path;
                /*Users userResult = centerUserService.updateUserFace(userId, finalUserFaceUrl);
                UserVO userVO = convertUserVoAndSetToken(userResult);
                CookieUtils.setCookie(request, response,
                        "user", JsonUtils.objectToJson(userVO), true);*/
            } else {
                return SYNEARJSONResult.errorMsg("上传头像失败！");
            }
        }
        return SYNEARJSONResult.ok();
    }


}
