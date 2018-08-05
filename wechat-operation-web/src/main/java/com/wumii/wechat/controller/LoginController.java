package com.wumii.wechat.controller;

import com.wumii.application.controller.ErrorController;
import com.wumii.application.controller.WebResult;
import com.wumii.wechat.service.LoginService;
import com.wumii.wechat.service.WeChatLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class LoginController extends ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginService loginService;

    @RequestMapping(value = "/login/qrcode", method = RequestMethod.GET)
    public void getLoginQrcode(HttpServletResponse response) {
        try {
            response.getOutputStream().write(loginService.getQrcode());
            response.setContentType("image/jpeg");
        } catch (IOException e) {
            logger.error("get login qrcode fail:" + e);
        }
    }

    @RequestMapping(value = "/teacher/logout", method = RequestMethod.POST)
    public WebResult logout(@RequestParam("wxid") String wxid) {
        loginService.logout(wxid);
        return WebResult.success();
    }
}
