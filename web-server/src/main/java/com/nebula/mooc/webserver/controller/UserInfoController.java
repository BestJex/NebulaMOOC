/*
 * @author Zhanghh
 * @date 2019/5/13
 */
package com.nebula.mooc.webserver.controller;

import com.nebula.mooc.core.Constant;
import com.nebula.mooc.core.entity.Return;
import com.nebula.mooc.core.entity.UserInfo;
import com.nebula.mooc.webserver.service.UserService;
import com.nebula.mooc.webserver.service.VideoService;
import com.nebula.mooc.webserver.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户登录后修改个人信息的控制器
 */
@RestController
@RequestMapping("/api/user/")
public class UserInfoController {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @PostMapping(value = "updateUser")
    public Return updateUser(HttpServletRequest request, HttpServletResponse response,
                             UserInfo userInfo, MultipartFile file) {
        UserInfo oldUserInfo = (UserInfo) request.getSession().getAttribute(Constant.USERINFO);
        // 将Id设置到新的userInfo
        userInfo.setId(oldUserInfo.getId());
        if (!file.isEmpty()) {
            // 有新头像上传
            if (file.getContentType() != null && !file.getContentType().startsWith("image"))
                return new Return(Constant.CLIENT_FILE_ERROR, "头像格式错误！");
            // 上传头像
            if (!videoService.uploadHead(userInfo, file))
                return new Return(Constant.CLIENT_FILE_ERROR, "头像上传失败！");
        }
        // 上传个人信息
        String newToken = userService.updateUser(userInfo);
        if (newToken == null)
            return new Return(Constant.CLIENT_ERROR_CODE, "修改失败，请重试！");
        userInfo.setEmail(oldUserInfo.getEmail());
        request.getSession().setAttribute(Constant.USERINFO, userInfo);
        request.getSession().setAttribute(Constant.TOKEN, newToken);
        CookieUtil.set(response, Constant.TOKEN, newToken);
        return new Return<>(userInfo);
    }

    @PostMapping(value = "getUserInfo")
    public Return getUserInfo(HttpServletRequest request) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute(Constant.USERINFO);
        return new Return<>(userInfo);
    }

}
